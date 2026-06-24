# CBACT01C COBOL-to-Java Mapping Analysis

## Overview

This document describes the translation of **CBACT01C.cbl** (CardDemo batch program) from COBOL to Java 17+. The program reads an indexed VSAM account file and writes transformed records to three output files.

## Source Artifacts

| COBOL Artifact | Purpose | Java Equivalent |
|---|---|---|
| `CBACT01C.cbl` | Main batch program | `AccountFileProcessor.java` |
| `CVACT01Y.cpy` | Account record layout (300 bytes) | `AccountRecord.java` |
| `CODATECN.cpy` | Date conversion record structure | `DateConverter.java` |
| COBDATFT (assembler) | Date format conversion | `DateConverter.convert()` |
| CEE3ABD (LE routine) | Abend with code 999 | `BatchAbendException` |

## Data Type Mapping

| COBOL Type | Field Example | Java Type | Notes |
|---|---|---|---|
| `PIC 9(11)` | ACCT-ID | `long` | 11-digit account identifier |
| `PIC X(01)` | ACCT-ACTIVE-STATUS | `char` | Single character status flag |
| `PIC S9(10)V99` | ACCT-CURR-BAL | `BigDecimal` (scale 2) | Signed zoned decimal, 12 bytes |
| `PIC S9(10)V99 COMP-3` | OUT-ACCT-CURR-CYC-DEBIT | `BigDecimal` (scale 2) | Packed decimal → same Java type |
| `PIC X(10)` | ACCT-OPEN-DATE | `String` | Date stored as text |
| `PIC X(178)` | FILLER | (not stored) | Padding bytes, not semantically meaningful |

## File I/O Mapping

| COBOL File | Organization | Java Approach |
|---|---|---|
| ACCTFILE (VSAM KSDS) | Indexed, sequential access | `BufferedReader` reading fixed-width lines |
| OUTFILE | Sequential | `BufferedWriter` writing pipe-delimited records |
| ARRYFILE | Sequential | `BufferedWriter` writing pipe-delimited records |
| VBRCFILE | Variable-length (RECORDING MODE V) | `BufferedWriter` writing fixed-length lines per record |

### File format decisions

- **Input**: Read as fixed-width text (300-char lines), parsed positionally per CVACT01Y layout.
- **Output (OUTFILE/ARRYFILE)**: Pipe-delimited for readability and downstream consumption. COBOL wrote binary/zoned fields; Java uses human-readable decimal strings.
- **Variable-length (VBRCFILE)**: VB1 records are exactly 12 characters; VB2 records are exactly 39 characters, matching COBOL's `WS-RECD-LEN` settings.

## Business Logic Preservation

### 1. Date Conversion (COBDATFT)
- COBOL calls assembler `COBDATFT` with `CODATECN-REC` parameters: input type `'2'` (YYYY-MM-DD) → output type `'2'` (YYYYMMDD).
- Java: `DateConverter.convert(date, '2', '2')` strips dashes.
- All four type combinations (1→1, 1→2, 2→1, 2→2) are implemented.

### 2. Zero-Debit Substitution Rule
```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```
- Java: `if (debit.compareTo(BigDecimal.ZERO) == 0) → 2525.00`
- **Note**: The COBOL source lacks an `ELSE` branch, meaning `OUT-ACCT-CURR-CYC-DEBIT` would retain its previous value for non-zero debits (an FD-level field is not re-initialised per iteration). The Java translation explicitly sets the non-zero value for correctness, treating the missing `ELSE` as an omission.

### 3. Array Record Population
Mirrors COBOL's fixed-value logic for the 5-element `OCCURS` array:

| Index | Balance | Debit |
|---|---|---|
| 0 | `ACCT-CURR-BAL` | 1005.00 |
| 1 | `ACCT-CURR-BAL` | 1525.00 |
| 2 | −1025.00 | −2500.00 |
| 3 | 0.00 (INITIALIZE) | 0.00 |
| 4 | 0.00 (INITIALIZE) | 0.00 |

### 4. Variable-Length Records
- **VB1** (12 bytes): `ACCT-ID (11)` + `ACCT-ACTIVE-STATUS (1)`
- **VB2** (39 bytes): `ACCT-ID (11)` + `ACCT-CURR-BAL (12)` + `ACCT-CREDIT-LIMIT (12)` + `REISSUE-YYYY (4)`

## Error Handling Mapping

| COBOL Pattern | Java Equivalent |
|---|---|
| File status checks (`ACCTFILE-STATUS`) | `IOException` handling in try-with-resources |
| `9910-DISPLAY-IO-STATUS` | `Logger.error()` with exception message |
| `CEE3ABD` abend with code 999 | `throw new BatchAbendException(999, ...)` |
| `DISPLAY` statements | SLF4J `log.info()` / `log.error()` |

## COBOL Idioms → Java Idioms

| COBOL | Java |
|---|---|
| `PERFORM ... UNTIL END-OF-FILE = 'Y'` | `while ((line = reader.readLine()) != null)` |
| `OPEN INPUT / OUTPUT` | `Files.newBufferedReader()` / `Files.newBufferedWriter()` |
| `CLOSE` | try-with-resources auto-close |
| `INITIALIZE` record | `new ArrayRecord()` (constructor fills zeros) |
| `REDEFINES` (CODATECN copybook) | Substring parsing in `DateConverter` |
| `OCCURS 5 TIMES` | `List<BalanceDebitPair>` with size 5 |
| `COMP-3` packed decimal | `BigDecimal` (same as zoned decimal in Java) |
| 88-level conditions | Not needed — direct type/value comparison |

## Test Coverage

| Test Class | Tests | Coverage Area |
|---|---|---|
| `DateConverterTest` | 17 | All type combinations, edge cases, error cases |
| `AccountRecordTest` | 6 | Fixed-width parsing, round-trip, negative values |
| `ArrayRecordTest` | 4 | Initialisation, CBACT01C population pattern |
| `AccountFileProcessorTest` | 12 | Zero-debit rule, pipeline, error handling, VB sizing |
