# CBACT01C Migration Notes: COBOL to Java 17+

## Overview

| Attribute         | COBOL                              | Java                                              |
|-------------------|------------------------------------|----------------------------------------------------|
| Program           | `CBACT01C.cbl` (431 LOC)          | `Cbact01cApplication.java` + 8 supporting classes  |
| Language level    | COBOL-85 with IBM extensions       | Java 17 (records, sealed classes, pattern matching) |
| Build system      | JCL + mainframe compiler           | Maven 3 (`pom.xml`)                                |
| Test framework    | None (manual verification)         | JUnit 5 (38 tests)                                 |
| Input             | VSAM KSDS (indexed sequential)     | Flat text file (one record per line)                |
| Output            | 3 sequential files                 | 3 text files                                       |

## Business Logic Preserved

CBACT01C is a batch program that reads every record from the account master VSAM file and writes each record into three output files in different formats:

1. **OUT-FILE** -- Fixed-format account record with date reformatting and debit substitution
2. **ARRY-FILE** -- Array-format record with 5 balance-entry slots (only 3 populated)
3. **VBRC-FILE** -- Two variable-length records per account (VB1: 12 bytes, VB2: 39 bytes)

### Business Rules Carried Forward

| Rule | COBOL Source | Java Implementation |
|------|-------------|---------------------|
| Reissue date reformatting | `CALL 'COBDATFT'` converts YYYY-MM-DD to YYYYMMDD | `DateConverter.convertYyyyMmDdToCompact()` |
| Zero-debit substitution | `IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00` | `if (cycDebit.compareTo(BigDecimal.ZERO) == 0) cycDebit = new BigDecimal("2525.00")` |
| Array slot 1 | `MOVE ACCT-CURR-BAL TO ARR-ACCT-CURR-BAL(1)`, `MOVE 1005.00 TO ARR-ACCT-CURR-CYC-DEBIT(1)` | `entries[0] = new BalanceEntry(acct.currBal(), new BigDecimal("1005.00"))` |
| Array slot 2 | `MOVE ACCT-CURR-BAL TO ARR-ACCT-CURR-BAL(2)`, `MOVE 1525.00 TO ARR-ACCT-CURR-CYC-DEBIT(2)` | `entries[1] = new BalanceEntry(acct.currBal(), new BigDecimal("1525.00"))` |
| Array slot 3 | `MOVE -1025.00 TO ARR-ACCT-CURR-BAL(3)`, `MOVE -2500.00 TO ARR-ACCT-CURR-CYC-DEBIT(3)` | `entries[2] = new BalanceEntry(new BigDecimal("-1025.00"), new BigDecimal("-2500.00"))` |
| Array slots 4-5 | `INITIALIZE ARR-ARRAY-REC` (zeroed) | `entries[3] = entries[4] = BalanceEntry.ZERO` |
| VB1 record | Account ID + active status (12 bytes) | `VbrRecord1(acctId, activeStatus)` |
| VB2 record | Account ID + balance + credit limit + reissue year (39 bytes) | `VbrRecord2(acctId, currBal, creditLimit, reissueYear)` |
| Display output | `DISPLAY` statements for each field | `System.out.println()` mirroring exact COBOL labels |
| Error handling | `PERFORM 9999-ABEND-PROGRAM` | `throw IOException` (caught in `main()`, exits with code 999) |

## Translation Decisions

### 1. COBOL Data Types to Java Types

| COBOL PIC Clause | Storage | Java Type | Rationale |
|------------------|---------|-----------|-----------|
| `PIC 9(11)` | 11-digit unsigned integer | `long` | Fits in 64-bit long; avoids BigInteger overhead |
| `PIC X(n)` | Fixed-length string | `String` | Naturally maps; padding handled in I/O layer |
| `PIC S9(10)V99` | 12-char zoned decimal with sign overpunch | `BigDecimal` | Exact decimal arithmetic; no floating-point rounding |
| `PIC S9(10)V99 COMP-3` | Packed BCD | `BigDecimal` | In text migration, COMP-3 writes as zoned decimal (see note below) |
| `PIC 9(4) BINARY` | 2-byte binary | Not needed | Used only for file status inspection in COBOL |

### 2. COBOL Zoned-Decimal (Sign Overpunch) Encoding

The most complex translation challenge was COBOL's zoned-decimal DISPLAY format. In ASCII text exports of VSAM data, signed numeric fields use **sign overpunch** encoding on the last character:

```
Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
```

**Critical detail**: `PIC S9(10)V99` stores 12 characters total. The `V` is an _implied_ decimal point -- it is **not stored**. The last 2 digits represent cents, so `"00000001940{"` decodes as:
- Digits: `000000019400` (replace `{` with `0`)
- Sign: positive (overpunch `{`)
- Move decimal left 2: **194.00**

This is implemented in `CobolDecimalParser.java` with bidirectional `parse()` and `format()` methods.

### 3. VSAM File I/O to Java NIO

| COBOL Construct | Java Replacement |
|----------------|------------------|
| `SELECT ... ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL` | `BufferedReader` reading line-by-line |
| `READ ACCTFILE-FILE INTO ACCOUNT-RECORD` | `AccountFileReader.readNext()` returning `Optional<AccountRecord>` |
| `WRITE OUT-ACCT-REC` | `AccountFileWriter.writeOutRecord()` |
| `RECORDING MODE IS V` (variable-length) | Fixed-length lines per record type (12 or 39 chars) |
| File status checks (`ACCTFILE-STATUS`) | `IOException` propagation |
| `OPEN INPUT / OPEN OUTPUT` | Constructor opens; `AutoCloseable.close()` closes |
| `PERFORM UNTIL END-OF-FILE = 'Y'` | `while (reader.readNext().isPresent())` |

**Key difference**: COBOL VSAM KSDS is a keyed dataset accessed sequentially. The Java version reads from a flat text file (the ASCII export found in `app/data/ASCII/acctdata.txt`). Record boundaries are newlines rather than fixed-length blocks.

### 4. COBDATFT Assembler Replacement

The COBOL program calls an assembler subroutine `COBDATFT` via `CALL 'COBDATFT' USING CODATECN-REC` for date format conversion. The CODATECN copybook defines:
- `CODATECN-TYPE = '2'` (input format: YYYY-MM-DD)
- `CODATECN-OUTTYPE = '2'` (output format: YYYYMMDD)

Since CBACT01C always uses type 2 -> type 2, the Java `DateConverter` simply strips dashes and pads to 10 characters:
```
"2025-05-20" -> "20250520  "
```

If other programs use different COBDATFT type codes, `DateConverter` would need to be extended.

### 5. COMP-3 (Packed Decimal) in Text Output

The COBOL OUT-FILE and ARRY-FILE define `CURR-CYC-DEBIT` as `USAGE IS COMP-3` (packed BCD). In a binary VSAM environment, COMP-3 stores two digits per byte. However, since our migration targets **text file output**, we write all numeric fields as zoned-decimal strings. This is a deliberate deviation -- the output is human-readable text rather than binary-compatible VSAM records.

### 6. Copybook to Java Record Mapping

| COBOL Copybook | Java Record | Notes |
|---------------|-------------|-------|
| `CVACT01Y` (ACCOUNT-RECORD) | `AccountRecord` | Input record; 300 bytes with 178-byte FILLER |
| FD OUT-ACCT-REC | `OutAccountRecord` | Output fixed-format; reissue date reformatted |
| FD ARR-ARRAY-REC | `ArrayRecord` + nested `BalanceEntry` | OCCURS 5 TIMES maps to array |
| VBRC-REC1 (Working-Storage) | `VbrRecord1` | 12-byte variable-length record |
| VBRC-REC2 (Working-Storage) | `VbrRecord2` | 39-byte variable-length record |

Java 17 `record` classes provide:
- Immutable value semantics (like COBOL copybook structures)
- Auto-generated `equals()`, `hashCode()`, `toString()`
- Compact syntax matching COBOL's declarative field definitions

### 7. Error Handling

| COBOL | Java |
|-------|------|
| File status check after every I/O | `IOException` propagation via try-with-resources |
| `PERFORM 9910-DISPLAY-IO-STATUS` | Exception message includes context |
| `PERFORM 9999-ABEND-PROGRAM` (sets ABCODE, calls ABEND) | `System.exit(999)` in main; tests verify via `IOException` |
| `APPL-RESULT` / `APPL-EOF` / `APPL-AOK` flags | `Optional.empty()` for EOF; exceptions for errors |

## Modern Java Idioms Used

| Idiom | Where Used | COBOL Equivalent |
|-------|-----------|------------------|
| `record` types (Java 16+) | All 5 model classes | COBOL copybook / FD record definitions |
| `Optional<T>` | `AccountFileReader.readNext()` | END-OF-FILE flag + APPL-EOF condition |
| Try-with-resources | `Cbact01cApplication.execute()` | OPEN / CLOSE paragraph pairs |
| `BigDecimal` for exact arithmetic | All monetary fields | `PIC S9(10)V99` DISPLAY/COMP-3 |
| `AutoCloseable` | `AccountFileReader`, `AccountFileWriter` | CLOSE FILE paragraphs |
| `var` (local type inference) | Throughout application and tests | N/A |
| Static factory methods | `BalanceEntry.ZERO` | `INITIALIZE` verb |

## Project Structure

```
java-migration/cbact01c/
  pom.xml                          -- Maven build (Java 17, JUnit 5)
  MIGRATION_NOTES.md               -- This document
  src/main/java/.../
    Cbact01cApplication.java       -- Main batch logic (213 LOC)
    model/
      AccountRecord.java           -- Input record (CVACT01Y)
      OutAccountRecord.java        -- Fixed-format output (FD OUT-ACCT-REC)
      ArrayRecord.java             -- Array-format output (FD ARR-ARRAY-REC)
      VbrRecord1.java              -- Variable-length type 1 (VBRC-REC1)
      VbrRecord2.java              -- Variable-length type 2 (VBRC-REC2)
    io/
      AccountFileReader.java       -- VSAM sequential read (91 LOC)
      AccountFileWriter.java       -- 3-file output writer (115 LOC)
    util/
      CobolDecimalParser.java      -- Zoned-decimal parse/format (103 LOC)
      DateConverter.java           -- COBDATFT replacement (59 LOC)
  src/test/java/.../
    Cbact01cApplicationTest.java   -- Integration + unit tests (11 tests)
    CobolDecimalParserTest.java    -- Parser/formatter tests (19 tests)
    DateConverterTest.java         -- Date conversion tests (8 tests)
```

## Test Coverage

**38 total tests** across 3 test classes:

### CobolDecimalParserTest (19 tests)
- Positive/negative overpunch parsing for all characters (`{`, `A`-`I`, `}`, `J`-`R`)
- Round-trip (parse -> format -> parse) verification
- Edge cases: zero, null, blank input, plain digits without overpunch

### DateConverterTest (8 tests)
- Standard YYYY-MM-DD to YYYYMMDD conversion
- Null and short input handling
- Year extraction for VB2 records

### Cbact01cApplicationTest (11 tests)
- **End-to-end**: Single record with non-zero debit, zero debit (triggers 2525.00 substitution), multiple records, empty file
- **Unit**: `buildOutRecord`, `buildOutRecordSubstitutesZeroDebit`, `buildArrayRecord`, `buildVbrRecord1`, `buildVbrRecord2`
- **Real data**: Processes first record from `acctdata.txt` and verifies exact byte offsets
- **Full sample**: Processes all 50 records from `app/data/ASCII/acctdata.txt` and validates record counts and line lengths

## Assumptions and Limitations

1. **Text-mode only**: Output files are text (newline-delimited) rather than binary VSAM. COMP-3 fields are written as zoned-decimal text. A production migration targeting an actual VSAM replacement (e.g., a database) would store values natively.

2. **Single date conversion path**: Only COBDATFT type 2 -> type 2 is implemented (YYYY-MM-DD -> YYYYMMDD). Other type combinations would require extending `DateConverter`.

3. **ASCII encoding assumed**: The COBOL sign overpunch characters (`{`, `}`, `A`-`R`, `J`-`R`) use ASCII values. EBCDIC-encoded files would need a different overpunch table.

4. **No JCL integration**: The COBOL program is invoked via JCL with DD statements mapping file names. The Java version accepts file paths as CLI arguments. A production deployment would use a job scheduler or Spring Batch.

5. **178-byte FILLER ignored**: The AccountRecord FILLER field (bytes 122-299) is read but discarded. If downstream programs rely on FILLER content, this would need to be preserved.

6. **Record padding**: Input lines shorter than 300 bytes are right-padded with spaces to match the COBOL fixed-length record behavior.

## How to Build and Run

```bash
# Build and run tests
cd java-migration/cbact01c
mvn clean test

# Run against sample data
mvn package -DskipTests
java -jar target/cbact01c-1.0.0.jar \
  ../../app/data/ASCII/acctdata.txt \
  /tmp/outfile.txt \
  /tmp/arryfile.txt \
  /tmp/vbrcfile.txt
```
