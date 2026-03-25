# MIGRATION_NOTES — CBACT01C (COBOL → Java 17+)

## 1. Program Overview

| Attribute | COBOL | Java |
|---|---|---|
| Program | `CBACT01C.cbl` | `Cbact01cProcessor.java` |
| Type | Batch (sequential read / write) | CLI application (`java -jar`) |
| Language level | COBOL 85 + CICS extensions | Java 17 (records, text blocks) |
| Build | JCL compile step | Maven 3 + JUnit 5 |

**Purpose:** Read an indexed VSAM account master file sequentially, then write
three derivative output files — a flat account file, an array file, and a
variable-length record file.

---

## 2. Data-Structure Mapping

### 2.1 Copybook → Java Record

| Copybook | Java Class | Notes |
|---|---|---|
| `CVACT01Y` (300-byte account) | `AccountRecord` | Java `record`; immutable value type |
| FD `OUT-FILE` | `OutAccountRecord` | Includes business-rule factory method |
| FD `ARRY-FILE` | `ArrayRecord` | 5-element balance/debit array |
| WS `VBRC-REC1` | `VbRecord1` | Short VB record (ID + status) |
| WS `VBRC-REC2` | `VbRecord2` | Long VB record (ID + bal + limit + year) |
| `CODATECN` | `DateFormatter` utility | Stateless utility class |

### 2.2 Numeric Field Handling

| COBOL | Java | Rationale |
|---|---|---|
| `PIC S9(10)V99` (zoned decimal, 12 bytes) | `BigDecimal` | Exact decimal arithmetic; no floating-point rounding |
| `PIC S9(10)V99 USAGE IS COMP-3` | `BigDecimal` | COMP-3 (packed BCD) is a storage optimization; `BigDecimal` preserves the same precision |
| `PIC 9(11)` (account ID) | `long` | Unsigned 11-digit integer fits in a 64-bit signed long |
| `PIC X(n)` (alphanumeric) | `String` | Direct mapping |

### 2.3 Zoned-Decimal Sign Overpunch

COBOL ASCII data uses trailing overpunch characters to encode the sign of
zoned-decimal fields:

| Character | Digit | Sign |
|---|---|---|
| `{` | 0 | + |
| `A`–`I` | 1–9 | + |
| `}` | 0 | − |
| `J`–`R` | 1–9 | − |

The `AccountRecord.parseSignedDecimal()` method implements this decoding,
producing a `BigDecimal` with the correct sign and 2-decimal-place precision.

---

## 3. Business-Rule Translation

### 3.1 Date Formatting (COBDATFT Assembler Routine)

The COBOL program calls the assembler subroutine `COBDATFT` via:

```cobol
MOVE '2'          TO CODATECN-TYPE.
MOVE '2'          TO CODATECN-OUTTYPE.
CALL 'COBDATFT'   USING CODATECN-REC.
```

This strips dashes from a `YYYY-MM-DD` date, producing `YYYYMMDD`.

**Java equivalent:** `DateFormatter.stripDashes(date)` — a one-line
`substring` concatenation. The `DateFormatter` class also supports the
reverse conversion (Type 1 → Type 1) for completeness and mirrors the
assembler's error handling for invalid type combinations.

### 3.2 Zero-Debit Defaulting

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

**Java equivalent:** In `OutAccountRecord.fromAccount()`:

```java
BigDecimal debit = acct.acctCurrCycDebit().compareTo(BigDecimal.ZERO) == 0
        ? DEFAULT_DEBIT   // 2525.00
        : acct.acctCurrCycDebit();
```

### 3.3 Array Population (Hard-Coded Test Data)

The COBOL `1400-POPUL-ARRAY-RECORD` paragraph populates a 5-element array
with a mix of input data and hard-coded constants:

| Slot | Balance | Debit |
|---|---|---|
| 1 | Input `ACCT-CURR-BAL` | 1005.00 |
| 2 | Input `ACCT-CURR-BAL` | 1525.00 |
| 3 | −1025.00 | −2500.00 |
| 4 | 0 | 0 |
| 5 | 0 | 0 |

This appears to be demonstration/test data. The Java `ArrayRecord.fromAccount()`
replicates this logic exactly.

### 3.4 Variable-Length Record Split

Each account produces two VB records:

- **VB1** (12 bytes in COBOL): Account ID + Active Status
- **VB2** (39 bytes in COBOL): Account ID + Current Balance + Credit Limit + Reissue Year (4 chars)

The reissue year is extracted from the `WS-ACCT-REISSUE-YYYY` field, which is
the first 4 characters of the `YYYY-MM-DD` reissue date (set during
`1300-POPUL-ACCT-RECORD` via `MOVE ACCT-REISSUE-DATE TO WS-REISSUE-DATE`).

---

## 4. I/O Translation

| COBOL | Java | Notes |
|---|---|---|
| `SELECT … ORGANIZATION IS INDEXED` | `BufferedReader` | Input file read sequentially; no keyed access needed |
| `SELECT … ORGANIZATION IS SEQUENTIAL` | `BufferedWriter` | Sequential output |
| `RECORDING MODE IS V` (variable-length) | `BufferedWriter` (line-oriented) | Variable-length semantics handled by writing two distinct line formats |
| `FILE STATUS` checks + `CEE3ABD` abend | `IOException` propagation | Java exception model replaces status-code checks |
| `DISPLAY` statements | `PrintStream` (injectable) | Console output for logging; testable via injection |

### 4.1 Output Format

The COBOL program writes binary fixed-width records. The Java version writes
**pipe-delimited text** instead, which is:

- Human-readable and debuggable
- Easily consumed by downstream Java/Python/SQL tools
- Free of EBCDIC/ASCII encoding concerns

If binary-compatible output is required for mainframe interop, a
`FixedWidthWriter` could be added as an alternative serializer.

---

## 5. Duplicate-Logic Consolidation

The original COBOL contains four nearly identical file-open paragraphs
(`0000-ACCTFILE-OPEN`, `2000-OUTFILE-OPEN`, `3000-ARRFILE-OPEN`,
`4000-VBRFILE-OPEN`) that differ only in the file name and status variable.
Similarly, three write paragraphs follow the same check-status-and-abend
pattern.

In the Java version, all file I/O is handled by `java.nio.file.Files` and
try-with-resources blocks, eliminating the repetitive open/close/status-check
boilerplate entirely.

---

## 6. Error Handling

| COBOL | Java |
|---|---|
| `MOVE status TO IO-STATUS` / `PERFORM 9910-DISPLAY-IO-STATUS` | Stack trace via exception |
| `CALL 'CEE3ABD'` (Language Environment abend) | `IOException` propagation / `System.exit(1)` |
| File status `'10'` (end-of-file) | `BufferedReader.readLine()` returns `null` |

---

## 7. Testing Strategy

| Test Class | Scope | Count |
|---|---|---|
| `DateFormatterTest` | COBDATFT assembler replacement | 9 |
| `AccountRecordTest` | Fixed-width parsing + overpunch decoding | 22 |
| `OutAccountRecordTest` | Business rules (date reformat, debit default) | 5 |
| `ArrayRecordTest` | Array population logic | 10 |
| `VbRecordTest` | VB record construction | 8 |
| `Cbact01cProcessorTest` | End-to-end with 3-record sample file | 10 |
| **Total** | | **64** |

Sample input data (`acctdata_sample.txt`) is derived from the first 3 records
of `app/data/ASCII/acctdata.txt`.

---

## 8. Build & Run

```bash
# Build and test
cd java-migration/cbact01c
mvn clean verify

# Run against the full sample data
mvn package -DskipTests
java -jar target/cbact01c-1.0.0.jar \
    ../../app/data/ASCII/acctdata.txt \
    /tmp/outfile.txt \
    /tmp/arryfile.txt \
    /tmp/vbrcfile.txt
```

---

## 9. Risks & Future Work

1. **Binary compatibility** — If downstream consumers expect COBOL fixed-width
   or COMP-3 packed-decimal output, a binary serializer should be added.
2. **Character encoding** — The Java version assumes UTF-8. If processing
   EBCDIC data directly, an `IBM-1047` charset decoder would be needed.
3. **Date validation** — `DateFormatter` mirrors the assembler's minimal
   validation. A `LocalDate.parse()` validation layer could be added.
4. **Batch framework** — For production use, wrapping `Cbact01cProcessor`
   in Spring Batch would add restart/skip/retry capabilities.
