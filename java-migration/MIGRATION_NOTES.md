# CBACT01C Migration Notes — COBOL to Java 17+

## 1. Program Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| **Program** | `CBACT01C.CBL` | `com.carddemo.batch.AccountFileProcessor` |
| **Type** | Batch COBOL (non-CICS) | Standalone Java 17 CLI application |
| **Purpose** | Read an indexed VSAM account file and write transformed data to three output files | Same |
| **Build** | Mainframe compiler + link-edit | Maven (`mvn clean package`) |

## 2. Architecture Mapping

```
COBOL                           Java
─────────────────────────────── ───────────────────────────────
IDENTIFICATION DIVISION         Class declaration
ENVIRONMENT DIVISION            Constructor (file paths)
FILE SECTION (FDs)              Record classes (Java records)
WORKING-STORAGE SECTION         Class fields / local variables
COPY <copybook>                 Separate model classes
PROCEDURE DIVISION              execute() method
PERFORM <paragraph>             Private method calls
CALL 'COBDATFT'                 DateConverter utility class
CALL 'CEE3ABD' (abend)          throw IOException / System.exit
DISPLAY                         Logging (display log list)
```

## 3. Data Structure Translation

### 3.1 Copybook CVACT01Y → `AccountRecord` (Java record)

The 300-byte fixed-width VSAM record is parsed from the ASCII data file.
Each field maps directly to a record component:

| COBOL Field | PIC | Java Type | Notes |
|------------|-----|-----------|-------|
| `ACCT-ID` | `9(11)` | `long` | Numeric ID, no sign |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `String` | Single character |
| `ACCT-CURR-BAL` | `S9(10)V99` | `BigDecimal` | Zoned decimal with sign overpunch |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal` | Same |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal` | Same |
| `ACCT-OPEN-DATE` | `X(10)` | `String` | `YYYY-MM-DD` |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `String` | Preserves COBOL typo |
| `ACCT-REISSUE-DATE` | `X(10)` | `String` | `YYYY-MM-DD` |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `BigDecimal` | Zoned decimal |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `BigDecimal` | Zoned decimal |
| `ACCT-ADDR-ZIP` | `X(10)` | `String` | Trimmed |
| `ACCT-GROUP-ID` | `X(10)` | `String` | Kept as-is (may be spaces) |
| `FILLER` | `X(178)` | — | Discarded during parse |

### 3.2 Zoned Decimal Encoding

COBOL `PIC S9(10)V99` uses **zoned decimal** with a sign overpunch on the last
byte. In the ASCII data files the last character encodes both a digit and the
sign:

| Last char | Digit | Sign |
|-----------|-------|------|
| `{` | 0 | + |
| `A`–`I` | 1–9 | + |
| `}` | 0 | − |
| `J`–`R` | 1–9 | − |

The `V99` implies two decimal places. Example: `00000001940{` → `+19400` → `194.00`.

This is handled by `AccountRecord.parseSignedDecimal()`.

### 3.3 Output Records

| COBOL FD | Java Class | Notes |
|----------|-----------|-------|
| `OUT-ACCT-REC` | `OutputAccountRecord` | Flat output; COMP-3 debit replaced with plain `BigDecimal` |
| `ARR-ARRAY-REC` | `ArrayRecord` | 5-slot array of (balance, debit) pairs |
| `VBRC-REC1` / `VBRC-REC2` | `VariableLengthRecord.Type1` / `.Type2` | Sealed interface with two record implementations |

## 4. Business Logic Translation Decisions

### 4.1 Default Debit Substitution

**COBOL:**
```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

**Java:**
```java
BigDecimal debit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
        ? DEFAULT_DEBIT    // 2525.00
        : acct.currCycDebit();
```

The hardcoded `2525.00` default is preserved exactly. This appears to be test/demo
data logic in the original COBOL.

### 4.2 Date Formatting (COBDATFT Replacement)

The COBOL program calls an assembler subroutine `COBDATFT` with:
- Input type `2` (YYYY-MM-DD)
- Output type `2` (YYYYMMDD)

This is replaced by `DateConverter.toCompactDate()` which simply strips hyphens.
The `DateConverter` class also supports the inverse conversion for completeness.

### 4.3 Array Record Population

The COBOL `OCCURS 5 TIMES` clause is mapped to parallel `BigDecimal[]` arrays of
length 5. Only slots 1–3 are populated (matching COBOL behavior):

| Slot | Balance | Debit |
|------|---------|-------|
| 1 | Account current balance | 1005.00 (hardcoded) |
| 2 | Account current balance | 1525.00 (hardcoded) |
| 3 | −1025.00 (hardcoded) | −2500.00 (hardcoded) |
| 4 | 0.00 (INITIALIZE) | 0.00 (INITIALIZE) |
| 5 | 0.00 (INITIALIZE) | 0.00 (INITIALIZE) |

### 4.4 Variable-Length Records

COBOL's `RECORDING MODE IS V` with `RECORD IS VARYING` is replaced by a sealed
interface `VariableLengthRecord` with two record implementations:
- `Type1` (short): account ID + active status
- `Type2` (long): account ID + balance + credit limit + reissue year

Each type prefixes its output line with `VB1|` or `VB2|` to distinguish record
types, replacing the COBOL variable-length record descriptor word (RDW).

### 4.5 Reissue Year Extraction

**COBOL:**
```cobol
MOVE ACCT-REISSUE-DATE TO WS-REISSUE-DATE.
MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```

This uses a `REDEFINES` to extract the first 4 characters (year) from the
10-character date. In Java: `acct.reissueDate().substring(0, 4)`.

## 5. I/O Translation

### 5.1 File Organization

| COBOL | Java | Rationale |
|-------|------|-----------|
| VSAM KSDS (indexed, sequential access) | `BufferedReader` (line-by-line) | ASCII data file is already sequential |
| Sequential output (OUTFILE) | `BufferedWriter` | Pipe-delimited text |
| Sequential output (ARRYFILE) | `BufferedWriter` | Pipe-delimited text |
| Variable-length sequential (VBRCFILE) | `BufferedWriter` | Prefixed text lines (`VB1\|` / `VB2\|`) |

### 5.2 Output Format Change

The COBOL program writes binary fixed-width records (including COMP-3 packed
decimal fields). The Java version writes **pipe-delimited text** for several
reasons:

1. **Readability** — output is human-inspectable
2. **Portability** — no endianness or EBCDIC concerns
3. **Testability** — easy to assert in JUnit
4. **COMP-3 elimination** — packed decimal has no direct Java equivalent; BigDecimal
   provides arbitrary-precision decimal arithmetic

### 5.3 Error Handling

| COBOL | Java |
|-------|------|
| File status check (`'00'`, `'10'`) | IOException from `Files` API |
| `PERFORM 9999-ABEND-PROGRAM` → `CALL 'CEE3ABD'` | `throw IOException` / `System.exit(1)` |
| `9910-DISPLAY-IO-STATUS` | Exception message + stack trace |

## 6. Java Modernization Idioms

| Pattern | Usage |
|---------|-------|
| **Records** (`record`) | All data structures are immutable Java 17 records |
| **Sealed interfaces** | `VariableLengthRecord` is a sealed interface with permitted implementations |
| **Pattern matching switch** | Zoned-decimal sign overpunch decoding uses enhanced switch expressions |
| **`BigDecimal`** | All monetary amounts — avoids floating-point rounding |
| **Try-with-resources** | All file I/O in `execute()` |
| **`Path` / `Files` API** | Modern NIO.2 file handling |
| **`List.copyOf()`** | Defensive copies for accessor methods |

## 7. Test Coverage

38 JUnit 5 tests organized in nested classes:

| Test Class | Count | Coverage |
|-----------|-------|----------|
| `AccountRecordParsingTest` | 6 | Zoned-decimal parsing, field extraction, error handling |
| `DateConverterTest` | 5 | All format conversions, edge cases |
| `OutputRecordTest` | 4 | Default debit logic, date reformatting, output format |
| `ArrayRecordTest` | 5 | All 5 slots, hardcoded values, zero initialization |
| `VbrRecordTest` | 4 | Both record types, field propagation, output format |
| `EndToEndTest` | 12 | Full 50-record processing, file line counts, record alternation |
| `EdgeCaseTest` | 2 | Empty file, single record |

## 8. How to Build and Run

```bash
# Build and test
cd java-migration
mvn clean package

# Run against the sample data
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    /tmp/outfile.dat \
    /tmp/arryfile.dat \
    /tmp/vbrcfile.dat
```

## 9. Known Limitations

1. **COMP-3 fidelity** — The COBOL output uses packed decimal (COMP-3) for some
   fields. The Java version uses `BigDecimal` in pipe-delimited text. If
   byte-for-byte binary compatibility is required, a packed-decimal encoder
   would need to be added.

2. **EBCDIC data** — The parser handles ASCII zoned-decimal only. For EBCDIC
   input files (in `app/data/EBCDIC/`), an EBCDIC-to-ASCII conversion step
   would be needed upstream.

3. **COBDATFT edge cases** — The assembler date routine may handle additional
   date formats or validation. The Java `DateConverter` covers the two types
   used by CBACT01C but does not replicate full COBDATFT behavior.

4. **Abend behavior** — COBOL `CEE3ABD` sets a system abend code visible to
   JCL. The Java version throws an `IOException` or calls `System.exit(1)`,
   which is the closest equivalent for batch scheduling.
