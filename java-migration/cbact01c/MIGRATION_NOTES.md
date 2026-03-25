# MIGRATION_NOTES.md — CBACT01C to Java 17+

## Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| **Program** | `CBACT01C.cbl` (431 lines) | `AccountFileProcessor.java` |
| **Type** | Batch (JCL-invoked) | Standalone CLI / embeddable in Spring Batch |
| **Language** | COBOL 85 + Enterprise extensions | Java 17+ (records, sealed classes, var) |
| **I/O** | VSAM KSDS (input), sequential (output) | `java.nio.file` buffered I/O |
| **External calls** | `COBDATFT` (assembler), `CEE3ABD` (LE abort) | `DateFormatter` utility class |
| **Copybooks** | `CVACT01Y`, `CODATECN` | Java `record` types |

## 1. Data Structure Translations

### 1.1 Account Record (CVACT01Y → `AccountRecord.java`)

| COBOL Field | PIC Clause | Bytes | Java Field | Java Type | Notes |
|-------------|-----------|-------|------------|-----------|-------|
| `ACCT-ID` | `9(11)` | 11 | `acctId` | `long` | Unsigned numeric |
| `ACCT-ACTIVE-STATUS` | `X(1)` | 1 | `activeStatus` | `String` | 'Y'/'N' |
| `ACCT-CURR-BAL` | `S9(10)V99` | 12 | `currentBalance` | `BigDecimal` | Overpunch sign encoding |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 | `creditLimit` | `BigDecimal` | Overpunch sign encoding |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 12 | `cashCreditLimit` | `BigDecimal` | Overpunch sign encoding |
| `ACCT-OPEN-DATE` | `X(10)` | 10 | `openDate` | `String` | YYYY-MM-DD format |
| `ACCT-EXPIRAION-DATE` | `X(10)` | 10 | `expirationDate` | `String` | Original typo preserved |
| `ACCT-REISSUE-DATE` | `X(10)` | 10 | `reissueDate` | `String` | YYYY-MM-DD format |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 12 | `currentCycleCredit` | `BigDecimal` | Overpunch sign encoding |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 12 | `currentCycleDebit` | `BigDecimal` | Overpunch sign encoding |
| `ACCT-ADDR-ZIP` | `X(10)` | 10 | `addressZip` | `String` | |
| `ACCT-GROUP-ID` | `X(10)` | 10 | `groupId` | `String` | |
| `FILLER` | `X(178)` | 178 | *(omitted)* | — | Padding not needed in Java |

**Key decision:** Used Java `record` types for immutable value semantics. All financial amounts use `BigDecimal` to avoid floating-point precision issues — critical for preserving COBOL's exact decimal arithmetic.

### 1.2 Output Account Record (FD OUT-FILE → `OutAccountRecord.java`)

The COBOL output record includes `OUT-ACCT-CURR-CYC-DEBIT` as `USAGE IS COMP-3` (packed decimal). In Java, all decimal fields use `BigDecimal` uniformly — the packed-decimal encoding is a storage optimization that has no semantic meaning in Java.

### 1.3 Array Record (FD ARRY-FILE → `ArrayRecord.java`)

The COBOL `OCCURS 5 TIMES` clause maps to `List<BalanceEntry>` with exactly 5 elements. The `ARR-ACCT-CURR-CYC-DEBIT` COMP-3 fields are again `BigDecimal` in Java.

### 1.4 Variable-Length Records (FD VBRC-FILE → `VbrRecord1.java`, `VbrRecord2.java`)

| COBOL | Java | Size |
|-------|------|------|
| `VBRC-REC1` (ID + status) | `VbrRecord1` record | 12 chars |
| `VBRC-REC2` (ID + bal + credit + year) | `VbrRecord2` record | 39 chars |

COBOL's `RECORDING MODE IS V` with `RECORD IS VARYING IN SIZE FROM 10 TO 80` is replaced by writing two separate lines per account to the output file. The variable-length semantics are inherent in the text output.

## 2. Signed Decimal Parsing (Overpunch Encoding)

### The Problem

COBOL `PIC S9(10)V99` in DISPLAY format stores the sign by "overpunching" the last byte:

| Last Char | Digit | Sign |
|-----------|-------|------|
| `{` | 0 | + |
| `A`–`I` | 1–9 | + |
| `}` | 0 | − |
| `J`–`R` | 1–9 | − |

The `V` is an *implied* decimal point — no decimal character exists in the data. A 12-byte field `S9(10)V99` represents 12 digits with 2 implied decimal places.

### The Solution

`CobolDecimalParser.parseSignedDecimal()` implements a lookup table for all 20 overpunch characters, extracts the digit and sign, and shifts the decimal point left by the specified number of places.

**Example:** `"00000001940{"` → digits `000000019400` → `19400` → ÷100 → `194.00` (positive)

## 3. External Call Replacements

### 3.1 COBDATFT → `DateFormatter.java`

The COBOL program calls assembler subroutine `COBDATFT` for date format conversion:

```cobol
MOVE ACCT-REISSUE-DATE TO CODATECN-INP-DATE WS-REISSUE-DATE.
MOVE '2' TO CODATECN-TYPE.       *> input is YYYY-MM-DD
MOVE '2' TO CODATECN-OUTTYPE.    *> output is YYYYMMDD
CALL 'COBDATFT' USING CODATECN-REC.
MOVE CODATECN-0UT-DATE TO OUT-ACCT-REISSUE-DATE.
```

The assembler program (`COBDATFT.asm`) performs simple string manipulation:
- **Type 1→1:** `YYYYMMDD` → `YYYY-MM-DD` (insert dashes)
- **Type 2→2:** `YYYY-MM-DD` → `YYYYMMDD` (remove dashes)
- **Type 1→2 or 2→1:** Returns `INVALID INPUT` error

`DateFormatter.convert()` replicates this exact logic, including the error cases, using `String.substring()` operations. No `java.time` API is needed since this is pure string reformatting, not date validation.

### 3.2 CEE3ABD → `RuntimeException`

The COBOL `CALL 'CEE3ABD'` (LE abnormal termination with abend code 999) is replaced by throwing an `IOException` or allowing the program to exit with a non-zero status. The Java version does not replicate the mainframe abend mechanism.

## 4. Business Logic Preservation

### 4.1 Zero-Debit Override

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

Java equivalent:
```java
if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
    cycDebit = new BigDecimal("2525.00");
}
```

This business rule overrides a zero current-cycle debit with a hardcoded value of 2525.00. The purpose is unclear from the code alone — it may be a default processing fee or test data artifact. The rule is preserved exactly.

### 4.2 Hardcoded Array Values

```cobol
MOVE ACCT-CURR-BAL  TO ARR-ACCT-CURR-BAL(1).
MOVE 1005.00        TO ARR-ACCT-CURR-CYC-DEBIT(1).
MOVE ACCT-CURR-BAL  TO ARR-ACCT-CURR-BAL(2).
MOVE 1525.00        TO ARR-ACCT-CURR-CYC-DEBIT(2).
MOVE -1025.00       TO ARR-ACCT-CURR-BAL(3).
MOVE -2500.00       TO ARR-ACCT-CURR-CYC-DEBIT(3).
```

These hardcoded values (1005.00, 1525.00, −1025.00, −2500.00) are preserved as `static final BigDecimal` constants. Array indices 4–5 (COBOL indices 4–5) remain zero-initialized, matching the COBOL `INITIALIZE ARR-ARRAY-REC` behavior.

### 4.3 Reissue Year Extraction

The COBOL program stores the reissue date in `WS-REISSUE-DATE` (PIC X(10)) *before* calling COBDATFT, then extracts `WS-ACCT-REISSUE-YYYY` (first 4 bytes) for the VBR record 2. The Java version replicates this by taking `substring(0, 4)` from the original `reissueDate` field (YYYY-MM-DD format).

## 5. I/O Translation

### 5.1 Input: VSAM KSDS → Flat File

| Aspect | COBOL | Java |
|--------|-------|------|
| File organization | VSAM KSDS (indexed) | Line-delimited text file |
| Access mode | Sequential | `BufferedReader.readLine()` |
| Record key | `FD-ACCT-ID PIC 9(11)` | Parsed from fixed-width positions |
| Record length | 300 bytes (fixed) | 300 chars per line (padded if short) |
| EOF detection | File status `'10'` | `readLine()` returns `null` |
| Error handling | Status `'12'` → ABEND 999 | `IOException` propagation |

### 5.2 Output: Three Files

The Java version writes pipe-delimited text rather than COBOL fixed-width binary. This is intentional — the modernized system will consume structured text or JSON, not mainframe-format records.

| COBOL File | Java File | Format |
|------------|-----------|--------|
| OUT-FILE (sequential, fixed) | `outfile.txt` | Pipe-delimited (11 fields) |
| ARRY-FILE (sequential, fixed) | `arryfile.txt` | Pipe-delimited (ID + 5×2 values) |
| VBRC-FILE (variable-length) | `vbrcfile.txt` | 2 lines per account (short + long) |

## 6. Duplicate Logic Elimination

The COBOL source contains repeated patterns for file open/close/error handling across four files (paragraphs 0000, 2000, 3000, 4000, 9000). Each follows the same pattern:

```cobol
MOVE 8 TO APPL-RESULT.
OPEN {INPUT|OUTPUT} {FILE}.
IF {STATUS} = '00'
    MOVE 0 TO APPL-RESULT
ELSE
    MOVE 12 TO APPL-RESULT
END-IF
IF APPL-AOK
    CONTINUE
ELSE
    DISPLAY 'ERROR ...'
    PERFORM 9910-DISPLAY-IO-STATUS
    PERFORM 9999-ABEND-PROGRAM
END-IF
```

In Java, this is replaced by a single `try-with-resources` block in `AccountFileProcessor.process()` that handles all file open/close/error logic. The `AccountFileReader` and `AccountFileWriter` classes implement `AutoCloseable`, eliminating the duplicated open/close paragraphs entirely.

Similarly, the four write-error-check paragraphs (1350, 1450, 1550, 1575) are consolidated into the `AccountFileWriter` methods, which propagate `IOException` rather than checking status codes.

## 7. Test Strategy

### 7.1 Test Coverage Summary

| Test Class | Tests | Scope |
|------------|-------|-------|
| `CobolDecimalParserTest` | 14 | Overpunch parsing with real data values |
| `DateFormatterTest` | 9 | All 4 type combinations + edge cases |
| `AccountFileReaderTest` | 3 | Fixed-width parsing against actual record data |
| `AccountFileWriterTest` | 4 | Output format verification |
| `AccountFileProcessorTest` | 16 | Business logic + end-to-end processing |
| **Total** | **49** | |

### 7.2 Golden-File Verification

The `AccountFileProcessorTest.EndToEnd` tests use actual data from `app/data/ASCII/acctdata.txt` (5-record sample) and verify:
- Correct record count in all 3 output files
- OUT-FILE: debit override (0 → 2525.00) applied
- OUT-FILE: reissue date converted (YYYY-MM-DD → YYYYMMDD)
- VBRC-FILE: correct VBR1/VBR2 formats and field values
- Empty input produces empty output (boundary condition)

### 7.3 Sample Data Traceability

Test values come directly from the first records in `acctdata.txt`:

| Field | Raw Data | Parsed Value |
|-------|----------|-------------|
| Account ID | `00000000001` | 1 |
| Current Balance | `00000001940{` | 194.00 |
| Credit Limit | `00000020200{` | 2020.00 |
| Cash Credit Limit | `00000010200{` | 1020.00 |
| Current Cycle Debit | `00000000000{` | 0.00 → **2525.00** (overridden) |

## 8. Project Structure

```
java-migration/cbact01c/
├── pom.xml                          # Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md               # This document
└── src/
    ├── main/java/com/carddemo/batch/
    │   ├── model/
    │   │   ├── AccountRecord.java       # CVACT01Y copybook
    │   │   ├── OutAccountRecord.java    # FD OUT-FILE
    │   │   ├── ArrayRecord.java         # FD ARRY-FILE
    │   │   ├── VbrRecord1.java          # VBRC-REC1 (12-byte)
    │   │   └── VbrRecord2.java          # VBRC-REC2 (39-byte)
    │   ├── util/
    │   │   ├── CobolDecimalParser.java  # Overpunch encoding
    │   │   └── DateFormatter.java       # COBDATFT replacement
    │   ├── io/
    │   │   ├── AccountFileReader.java   # VSAM KSDS → BufferedReader
    │   │   └── AccountFileWriter.java   # 3 output files
    │   └── processor/
    │       └── AccountFileProcessor.java # Main batch program
    └── test/
        ├── java/com/carddemo/batch/
        │   ├── util/
        │   │   ├── CobolDecimalParserTest.java
        │   │   └── DateFormatterTest.java
        │   ├── io/
        │   │   ├── AccountFileReaderTest.java
        │   │   └── AccountFileWriterTest.java
        │   └── processor/
        │       └── AccountFileProcessorTest.java
        └── resources/
            └── acctdata_sample.txt      # 5-record golden sample
```

## 9. Future Considerations

1. **Spring Batch Integration**: The `AccountFileProcessor` can be wrapped as a Spring Batch `Tasklet` or broken into `ItemReader`/`ItemProcessor`/`ItemWriter` steps for better observability and restart capability.

2. **Database Target**: Replace file output with JPA/JDBC writes to PostgreSQL tables — the `OutAccountRecord`, `ArrayRecord`, and VBR records map naturally to database entities.

3. **Configuration Externalization**: The hardcoded values (2525.00, 1005.00, 1525.00, etc.) should be externalized to application properties or a configuration table once business owners confirm their purpose.

4. **Input Format Migration**: Currently reads the COBOL fixed-width ASCII format. Future iterations should read from a database or structured API instead.
