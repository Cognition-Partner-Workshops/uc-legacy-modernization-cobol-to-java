# Migration Notes: CBACT01C (COBOL → Java 17)

This document records every translation decision made while migrating the COBOL batch program `CBACT01C.cbl` to Java 17+.

---

## 1. Program Overview

**CBACT01C** is a batch COBOL program that reads the CardDemo account master VSAM file sequentially and writes three output files with transformed data. It demonstrates fixed-width I/O, COMP-3 packed decimal output, OCCURS arrays, variable-length records, and an external assembler date-formatting routine.

| COBOL Artifact | Java Equivalent | Notes |
|---|---|---|
| `CBACT01C.cbl` | `AccountFileProcessor.java` | Main batch orchestrator |
| `CVACT01Y.cpy` (copybook) | `AccountRecord.java` (record) | Domain model |
| `CODATECN.cpy` (copybook) | `DateFormatter.java` (utility) | Date conversion data structure |
| `COBDATFT.asm` (assembler) | `DateFormatter.java` (utility) | Date formatting logic |
| VSAM KSDS file I/O | `AccountFileReader.java` | Sequential file reader |
| FD OUT-FILE / ARRY-FILE / VBRC-FILE | `OutputFileWriter.java` | Output file writer |

---

## 2. Data Structure Decisions

### 2.1 Account Record → Java Record

The COBOL copybook `CVACT01Y` defines a 300-byte fixed-width record. This maps to a Java `record` type (`AccountRecord`), which provides immutability, automatic `equals`/`hashCode`, and compact syntax.

```
COBOL: 01 ACCOUNT-RECORD.
           05 ACCT-ID             PIC 9(11).
           05 ACCT-ACTIVE-STATUS  PIC X(01).
           ...

Java:  public record AccountRecord(
           long acctId,
           String activeStatus,
           ...
       ) {}
```

**Decision:** Use `long` for the 11-digit account ID (fits within `long` range). Use `BigDecimal` for all monetary fields to preserve exact decimal arithmetic, matching COBOL's fixed-point behavior.

### 2.2 Signed Fields (Zoned Decimal with Overpunch)

COBOL `PIC S9(10)V99` fields are 12 bytes in DISPLAY format: 10 integer digits + 2 decimal digits, with the sign encoded via overpunch in the last byte's zone nibble.

| ASCII Char | Digit | Sign |
|---|---|---|
| `{` | 0 | + |
| `A`–`I` | 1–9 | + |
| `}` | 0 | − |
| `J`–`R` | 1–9 | − |

**Decision:** Created `ZonedDecimalParser.decode()` to handle this. Returns `BigDecimal` to preserve exact decimal values. The implied decimal point (`V99`) is handled by shifting 2 places left.

### 2.3 COMP-3 (Packed Decimal) Output

The COBOL program writes `OUT-ACCT-CURR-CYC-DEBIT` and array debit fields as `COMP-3` (packed BCD). This is a binary encoding that stores two digits per byte with the sign in the last nibble.

**Decision:** The Java version writes pipe-delimited text with `BigDecimal` values formatted to 2 decimal places. This is a deliberate modernization: binary encodings are replaced with human-readable text while preserving exact values. The field semantics and business logic are identical.

### 2.4 FILLER Fields

COBOL FILLER pads records to fixed widths. These are skipped during parsing and not present in the Java model.

### 2.5 OCCURS Array

The COBOL `ARR-ACCT-BAL OCCURS 5 TIMES` creates a repeating group. This maps to a Java array of `BalanceEntry` records inside `ArrayRecord`.

---

## 3. Business Logic Decisions

### 3.1 Zero-Debit Substitution

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

**Java translation:**
```java
if (cycleDebit.compareTo(BigDecimal.ZERO) == 0) {
    cycleDebit = new BigDecimal("2525.00");
}
```

**Decision:** Preserved exactly. The hardcoded `2525.00` substitution is a business rule. Using `compareTo` instead of `equals` ensures `0.00` is treated as zero regardless of scale.

### 3.2 Array Record Population (Hardcoded Values)

The COBOL program populates array entries with hardcoded test values:

| Entry | Balance | Debit |
|---|---|---|
| [1] | Account balance | 1005.00 |
| [2] | Account balance | 1525.00 |
| [3] | -1025.00 | -2500.00 |
| [4] | 0 (initialized) | 0 (initialized) |
| [5] | 0 (initialized) | 0 (initialized) |

**Decision:** Preserved exactly as `BigDecimal` constants. These appear to be test/demo values rather than production business logic, but they are faithfully reproduced.

### 3.3 Date Reformatting (COBDATFT)

The COBOL program calls the assembler routine `COBDATFT` to convert the reissue date from `YYYY-MM-DD` to `YYYYMMDD` format (Type 2 input → Type 2 output).

**Assembler logic (VALIDIN2):**
```asm
MVC COOUTDT(4),COINPDT         * Copy YYYY
MVC COOUTDT+4(2),COINPDT+5     * Copy MM (skip dash)
MVC COOUTDT+6(2),COINPDT+8     * Copy DD (skip dash)
```

**Java translation:**
```java
public static String toCompact(String date) {
    return date.substring(0, 4)
            + date.substring(5, 7)
            + date.substring(8, 10);
}
```

**Decision:** Direct translation. The assembler routine also supports Type 1 (YYYYMMDD → YYYY-MM-DD) conversion; we implement both for completeness (`toCompact` and `toReadable`), although only `toCompact` is used by CBACT01C.

### 3.4 Reissue Year Extraction

```cobol
MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```

The reissue date is also stored in a working-storage area `WS-ACCT-REISSUE-DATE` which has the year as a sub-field. The VB2 record gets only the year portion.

**Decision:** `DateFormatter.extractYear()` returns `substring(0, 4)`.

---

## 4. I/O Translation Decisions

### 4.1 VSAM KSDS → Flat File Reader

The COBOL program opens an indexed VSAM file (`ORGANIZATION IS INDEXED, ACCESS MODE IS SEQUENTIAL`). Since VSAM is a mainframe file system, we read the ASCII flat-file export directly.

**Decision:** `AccountFileReader` reads the ASCII `acctdata.txt` line by line. It implements `Iterable<AccountRecord>` and `AutoCloseable` for idiomatic Java resource management. This replaces the COBOL `READ ... INTO ...` / `FILE STATUS` pattern.

### 4.2 File Status Checking → Exception Handling

COBOL uses two-byte file status codes:
- `'00'` = success
- `'10'` = end of file
- Anything else = error → ABEND

**Decision:** Java's `IOException` and iterator pattern handle this naturally. EOF is detected when `BufferedReader.readLine()` returns `null`. I/O errors become `IOException` or `UncheckedIOException`, replacing the `9999-ABEND-PROGRAM` / `CEE3ABD` abend.

### 4.3 Output Format Modernization

| COBOL | Java | Rationale |
|---|---|---|
| Fixed-width binary (COMP-3) | Pipe-delimited text | Human-readable, debuggable, testable |
| Variable-length records | Prefixed text lines (`VB1\|...`, `VB2\|...`) | Self-describing format |
| 300-byte padded records | Trimmed fields | No wasted space |

### 4.4 DISPLAY Statements → System.out

All COBOL `DISPLAY` statements map to `System.out.printf()` calls with matching field labels (including the original COBOL typo `ACCT-EXPIRAION-DATE`).

---

## 5. Type Mapping Summary

| COBOL PIC | Java Type | Notes |
|---|---|---|
| `PIC 9(11)` | `long` | Unsigned integer, fits in `long` |
| `PIC X(n)` | `String` | Trimmed of trailing spaces |
| `PIC S9(10)V99` | `BigDecimal` | Exact decimal, overpunch decoded |
| `PIC S9(10)V99 COMP-3` | `BigDecimal` | Same value, different storage |
| `PIC X(10)` (date) | `String` | Kept as string; could be `LocalDate` |
| FILLER | (omitted) | Not modeled |
| OCCURS n TIMES | Array (`T[]`) | Fixed-size Java array |
| 01-level group | `record` | Java 16+ record type |

---

## 6. Testing Strategy

### 6.1 Unit Tests (53 tests)

- **ZonedDecimalParserTest** (30 tests): All overpunch characters, positive/negative values, edge cases (null, blank, no decimal places)
- **DateFormatterTest** (11 tests): Compact/readable conversion, round-trip, null/short input handling, year extraction
- **AccountFileProcessorTest** (12 tests): Record parsing, business logic (zero-debit substitution, date reformatting, array population, VB1/VB2 construction), end-to-end file processing, empty file handling, real data validation

### 6.2 Golden-File Verification

The test `processRealData_allRecordsParsedSuccessfully` reads all 50 records from the actual `acctdata.txt` and verifies:
- Correct record count (50)
- All account IDs are positive
- Output files have correct line counts (50 out, 50 array, 100 vbrc)

### 6.3 Test Data

Synthetic test records are embedded directly in the test class with known expected values, including:
- Standard positive balances with `{` overpunch
- Negative balance with `N` overpunch
- Zero cycle debit (triggers 2525.00 substitution)
- Non-zero cycle debit (preserved as-is)

---

## 7. Risks and Limitations

1. **COMP-3 binary fidelity**: The Java output uses text format instead of packed decimal. Any downstream system expecting binary COMP-3 output would need an adapter.

2. **EBCDIC data**: The parser handles ASCII-encoded overpunch. If processing EBCDIC files directly, additional character set conversion would be needed.

3. **Date validation**: The COBDATFT routine does not validate dates (it's a pure reformatter). The Java version preserves this behavior — invalid dates are reformatted without validation.

4. **VSAM key access**: The COBOL program opens the file as INDEXED with SEQUENTIAL access. The Java version reads a flat file sequentially. Random-access by key is not implemented.

5. **ABEND behavior**: The COBOL program calls `CEE3ABD` with abend code 999 on error. The Java version throws exceptions instead. If abend-code-aware monitoring is needed, a custom exception hierarchy could be added.

---

## 8. Project Structure

```
java-migration/
├── pom.xml                          Maven build (Java 17, JUnit 5)
├── src/main/java/com/carddemo/
│   ├── batch/
│   │   └── AccountFileProcessor.java    Main batch program
│   ├── model/
│   │   ├── AccountRecord.java           Input record (CVACT01Y)
│   │   ├── OutputAccountRecord.java     Output record (FD OUT-FILE)
│   │   ├── ArrayRecord.java             Array record (FD ARRY-FILE)
│   │   └── VariableLengthRecord.java    VB records (FD VBRC-FILE)
│   ├── io/
│   │   ├── AccountFileReader.java       Fixed-width file reader
│   │   └── OutputFileWriter.java        Output file writer
│   └── util/
│       ├── ZonedDecimalParser.java      Overpunch sign decoder
│       └── DateFormatter.java           COBDATFT replacement
└── src/test/java/com/carddemo/
    ├── batch/
    │   └── AccountFileProcessorTest.java
    └── util/
        ├── ZonedDecimalParserTest.java
        └── DateFormatterTest.java
```
