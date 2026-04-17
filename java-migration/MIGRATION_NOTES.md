# CBACT01C Migration Notes: COBOL to Java 17+

## Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| **Program** | `CBACT01C.cbl` | `com.carddemo.batch.CBACT01C` |
| **Type** | Batch COBOL (CICS-independent) | Standalone Java 17+ CLI application |
| **Input** | VSAM KSDS (indexed, sequential read) | Flat file (ASCII fixed-width, 300 bytes/record) |
| **Outputs** | 3 sequential files (OUTFILE, ARRYFILE, VBRCFILE) | 3 binary files with identical record layouts |
| **Copybooks** | `CVACT01Y`, `CODATECN` | Java records and utility classes |
| **External calls** | `COBDATFT` (assembler date formatter) | `DateFormatter.java` |

## Business Logic Summary

CBACT01C is a batch program that reads the account master file and produces three derivative files:

1. **OUTFILE** - Account extract with reformatted dates and packed-decimal cycle debit
2. **ARRYFILE** - Array-structured records with 5 repeated balance/debit groups (hardcoded test values)
3. **VBRCFILE** - Two variable-length records per account (short status record + longer financial record)

### Key Transformation Rules

| Rule | Description |
|------|-------------|
| **Date reformatting** | Reissue date converted from `YYYY-MM-DD` to `YYYYMMDD` via `COBDATFT` (type=2 in, type=2 out) |
| **Zero debit substitution** | If `ACCT-CURR-CYC-DEBIT = 0`, output `2525.00` instead |
| **Array population** | Groups 1-2 use actual balance + hardcoded debits (1005.00, 1525.00); Group 3 uses hardcoded negatives (-1025.00, -2500.00); Groups 4-5 are zero-filled |
| **VBR record split** | Each account produces a 12-byte VB1 (ID + status) and a 39-byte VB2 (ID + balance + limit + reissue year) |

## Field Mappings

### Input: CVACT01Y Copybook (ACCOUNT-RECORD, 300 bytes)

| Offset | Length | COBOL Field | PIC Clause | Java Field | Java Type |
|--------|--------|-------------|------------|------------|-----------|
| 0 | 11 | `ACCT-ID` | `9(11)` | `acctId` | `long` |
| 11 | 1 | `ACCT-ACTIVE-STATUS` | `X(01)` | `acctActiveStatus` | `String` |
| 12 | 12 | `ACCT-CURR-BAL` | `S9(10)V99` | `acctCurrBal` | `BigDecimal` |
| 24 | 12 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `acctCreditLimit` | `BigDecimal` |
| 36 | 12 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `acctCashCreditLimit` | `BigDecimal` |
| 48 | 10 | `ACCT-OPEN-DATE` | `X(10)` | `acctOpenDate` | `String` |
| 58 | 10 | `ACCT-EXPIRAION-DATE` | `X(10)` | `acctExpirationDate` | `String` |
| 68 | 10 | `ACCT-REISSUE-DATE` | `X(10)` | `acctReissueDate` | `String` |
| 78 | 12 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `acctCurrCycCredit` | `BigDecimal` |
| 90 | 12 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `acctCurrCycDebit` | `BigDecimal` |
| 102 | 10 | `ACCT-ADDR-ZIP` | `X(10)` | `acctAddrZip` | `String` |
| 112 | 10 | `ACCT-GROUP-ID` | `X(10)` | `acctGroupId` | `String` |
| 122 | 178 | `FILLER` | `X(178)` | *(not mapped)* | - |

### Output 1: OUTFILE Record (107 bytes)

| Offset | Length | COBOL Field | PIC / Usage | Encoding |
|--------|--------|-------------|-------------|----------|
| 0 | 11 | `OUT-ACCT-ID` | `9(11)` | Unsigned numeric |
| 11 | 1 | `OUT-ACCT-ACTIVE-STATUS` | `X(01)` | Character |
| 12 | 12 | `OUT-ACCT-CURR-BAL` | `S9(10)V99` | Zoned decimal |
| 24 | 12 | `OUT-ACCT-CREDIT-LIMIT` | `S9(10)V99` | Zoned decimal |
| 36 | 12 | `OUT-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Zoned decimal |
| 48 | 10 | `OUT-ACCT-OPEN-DATE` | `X(10)` | Character |
| 58 | 10 | `OUT-ACCT-EXPIRAION-DATE` | `X(10)` | Character |
| 68 | 10 | `OUT-ACCT-REISSUE-DATE` | `X(10)` | Character (YYYYMMDD + 2 spaces) |
| 78 | 12 | `OUT-ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Zoned decimal |
| 90 | 7 | `OUT-ACCT-CURR-CYC-DEBIT` | `S9(10)V99 COMP-3` | Packed decimal |
| 97 | 10 | `OUT-ACCT-GROUP-ID` | `X(10)` | Character |

### Output 2: ARRYFILE Record (110 bytes)

| Offset | Length | COBOL Field | PIC / Usage | Notes |
|--------|--------|-------------|-------------|-------|
| 0 | 11 | `ARR-ACCT-ID` | `9(11)` | Unsigned numeric |
| 11 | 95 | `ARR-ACCT-BAL` (x5) | OCCURS 5 TIMES | Each group = 12 (zoned) + 7 (COMP-3) = 19 bytes |
| 106 | 4 | `ARR-FILLER` | `X(04)` | Spaces |

**Array group contents:**

| Group | `ARR-ACCT-CURR-BAL` | `ARR-ACCT-CURR-CYC-DEBIT` |
|-------|---------------------|---------------------------|
| 1 | `ACCT-CURR-BAL` (from input) | `1005.00` (hardcoded) |
| 2 | `ACCT-CURR-BAL` (from input) | `1525.00` (hardcoded) |
| 3 | `-1025.00` (hardcoded) | `-2500.00` (hardcoded) |
| 4 | `0.00` (initialized) | `0.00` (initialized) |
| 5 | `0.00` (initialized) | `0.00` (initialized) |

### Output 3: VBRCFILE (Variable-Length Records)

Two records per account:

**VB1 Record (12 bytes):**

| Offset | Length | COBOL Field | PIC |
|--------|--------|-------------|-----|
| 0 | 11 | `VB1-ACCT-ID` | `9(11)` |
| 11 | 1 | `VB1-ACCT-ACTIVE-STATUS` | `X(01)` |

**VB2 Record (39 bytes):**

| Offset | Length | COBOL Field | PIC |
|--------|--------|-------------|-----|
| 0 | 11 | `VB2-ACCT-ID` | `9(11)` |
| 11 | 12 | `VB2-ACCT-CURR-BAL` | `S9(10)V99` |
| 23 | 12 | `VB2-ACCT-CREDIT-LIMIT` | `S9(10)V99` |
| 35 | 4 | `VB2-ACCT-REISSUE-YYYY` | `X(04)` |

## Data Encoding Reference

### Zoned Decimal (DISPLAY format)

Signed numeric fields use ASCII sign-overpunch on the last byte:

| Digit | Positive | Negative |
|-------|----------|----------|
| 0 | `{` | `}` |
| 1 | `A` | `J` |
| 2 | `B` | `K` |
| 3 | `C` | `L` |
| 4 | `D` | `M` |
| 5 | `E` | `N` |
| 6 | `F` | `O` |
| 7 | `G` | `P` |
| 8 | `H` | `Q` |
| 9 | `I` | `R` |

Example: `00000001940{` = `+000000194.00` (with implied V99 decimal)

### Packed Decimal (COMP-3)

Each byte holds two BCD digits, except the last byte whose low nibble is the sign (`C` = positive, `D` = negative).

- `PIC S9(10)V99 COMP-3` occupies 7 bytes (13 nibbles for digits + 1 nibble for sign = 14 nibbles = 7 bytes)
- Example: `2525.00` → `00 00 00 02 52 50 0C`

## Java Class Mapping

| COBOL Artifact | Java Class | Purpose |
|----------------|------------|---------|
| `CBACT01C.cbl` (main program) | `CBACT01C.java` | Batch orchestrator with `execute()` method |
| `CVACT01Y.cpy` (account copybook) | `AccountRecord.java` | Java `record` for the 300-byte account layout |
| `CODATECN.cpy` + `COBDATFT` (date routine) | `DateFormatter.java` | Date format conversion (YYYY-MM-DD ↔ YYYYMMDD) |
| Zoned/packed decimal handling | `CobolDecimalUtils.java` | Parse/format zoned decimal; encode COMP-3 |
| `READ ACCTFILE-FILE INTO` | `AccountFileParser.java` | Fixed-width file parser |
| `WRITE OUT-ACCT-REC` / `ARR-ARRAY-REC` / `VBR-REC` | `OutputRecordWriter.java` | Binary output writer for all 3 file types |

## Behavioral Notes

1. **Console output**: The Java version reproduces the exact same `DISPLAY` statements as the COBOL program, including the field labels, separator lines, and start/end messages.
2. **File status handling**: COBOL uses file status codes (`00`, `10`, `12`) for I/O control. The Java version uses standard `IOException` handling. Non-recoverable errors that would trigger `9999-ABEND-PROGRAM` in COBOL are propagated as exceptions.
3. **COBDATFT**: The assembler date formatting subroutine is replaced by `DateFormatter.java`, which implements the same type-code logic defined in the `CODATECN` copybook.
4. **Variable-length records**: The COBOL `RECORDING MODE IS V` with `RECORD IS VARYING` is simplified in Java to sequential writes of the exact byte lengths (12 and 39 bytes), without the RDW (Record Descriptor Word) that would be present in true mainframe VB files.

## Test Coverage

47 JUnit 5 tests verify:
- Zoned decimal parsing and formatting (positive, negative, zero, all sign characters)
- COMP-3 packed decimal encoding
- Date format conversion
- Full record parsing from the 50-record sample dataset (`app/data/ASCII/acctdata.txt`)
- Output file sizes and record-level field verification for all three output files
- Console output matching COBOL `DISPLAY` statements
- Zero-debit substitution logic (2525.00)
- End-to-end execution with all 50 sample records

## How to Build and Run

```bash
# Build and test (requires Java 17+)
cd java-migration
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package

# Run the batch job
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    output/outfile.dat \
    output/arryfile.dat \
    output/vbrcfile.dat
```
