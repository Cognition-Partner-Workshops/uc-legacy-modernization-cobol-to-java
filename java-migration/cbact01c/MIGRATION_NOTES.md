# CBACT01C Migration Notes

## Overview

This document records the translation decisions made when migrating the COBOL batch
program **CBACT01C.cbl** (431 lines) to a Java 17+ application.  The Java version
lives under `java-migration/cbact01c/` and is built with Maven.

| Attribute | COBOL | Java |
|-----------|-------|------|
| Source | `app/cbl/CBACT01C.cbl` | `java-migration/cbact01c/` |
| Copybooks | `CVACT01Y`, `CODATECN` | Java records |
| External call | `COBDATFT` (assembler) | `DateFormatter` utility |
| Input | VSAM KSDS (`ACCTFILE`) | Flat ASCII file (`acctdata.txt`) |
| Outputs | Sequential (`OUTFILE`), array (`ARRYFILE`), variable-length (`VBRCFILE`) | Pipe-delimited text files |
| Language level | COBOL-85 | Java 17 (records, switch expressions, streams) |

---

## 1. Program Structure

### COBOL paragraphs to Java methods

| COBOL Paragraph | Java Method / Class | Notes |
|-----------------|---------------------|-------|
| `PROCEDURE DIVISION` (main loop) | `AccountFileProcessor.execute()` | PERFORM UNTIL → while loop |
| `0000-ACCTFILE-OPEN` | Constructor validates input path | File status check → `IOException` |
| `1000-ACCTFILE-GET-NEXT` | `BufferedReader.readLine()` in loop | `ACCTFILE-STATUS '10'` → null return |
| `1100-DISPLAY-ACCT-RECORD` | `displayRecord()` with `System.out` | Preserved identical DISPLAY format |
| `1300-POPUL-ACCT-RECORD` | `OutputAccountRecord.fromAccountRecord()` | Record transformation as factory method |
| `1350-WRITE-ACCT-RECORD` | `RecordWriter.writeOutputRecord()` | Sequential WRITE → `BufferedWriter.write()` |
| `1400-POPUL-ARRAY-RECORD` | `ArrayAccountRecord.fromAccountRecord()` | OCCURS 5 TIMES → `List<BalanceEntry>` |
| `1450-WRITE-ARRY-RECORD` | `RecordWriter.writeArrayRecord()` | |
| `1500-POPUL-VBRC-RECORD` | `VbrcRecord1/VbrcRecord2.fromAccountRecord()` | Variable-length split into two record types |
| `1550-WRITE-VB1-RECORD` | `RecordWriter.writeVbrcRecord1()` | `WS-RECD-LEN = 12` → fixed-length record |
| `1575-WRITE-VB2-RECORD` | `RecordWriter.writeVbrcRecord2()` | `WS-RECD-LEN = 39` → fixed-length record |
| `2000-OUTFILE-OPEN` | `BufferedWriter` creation in `execute()` | |
| `3000-ARRFILE-OPEN` | `BufferedWriter` creation in `execute()` | |
| `4000-VBRFILE-OPEN` | `BufferedWriter` creation in `execute()` | |
| `9000-ACCTFILE-CLOSE` | try-with-resources auto-close | |
| `9910-DISPLAY-IO-STATUS` | `IOException` message | |
| `9999-ABEND-PROGRAM` | `throw new RuntimeException()` | ABEND → unchecked exception |

---

## 2. Data Structures

### CVACT01Y copybook → `AccountRecord` (Java record)

The 300-byte fixed-width ACCOUNT-RECORD is mapped to an immutable Java record:

| COBOL Field | PIC Clause | Offset | Java Field | Java Type | Notes |
|-------------|-----------|--------|------------|-----------|-------|
| `ACCT-ID` | `9(11)` | 0 | `acctId` | `long` | Numeric, leading zeros stripped |
| `ACCT-ACTIVE-STATUS` | `X(01)` | 11 | `activeStatus` | `String` | Single character |
| `ACCT-CURR-BAL` | `S9(10)V99` | 12 | `currBal` | `BigDecimal` | Signed zoned decimal with EBCDIC overpunch |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 24 | `creditLimit` | `BigDecimal` | |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 36 | `cashCreditLimit` | `BigDecimal` | |
| `ACCT-OPEN-DATE` | `X(10)` | 48 | `openDate` | `String` | YYYY-MM-DD format in data |
| `ACCT-EXPIRAION-DATE` | `X(10)` | 58 | `expirationDate` | `String` | Typo preserved from COBOL |
| `ACCT-REISSUE-DATE` | `X(10)` | 68 | `reissueDate` | `String` | |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 78 | `currCycCredit` | `BigDecimal` | |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 90 | `currCycDebit` | `BigDecimal` | |
| `ACCT-ADDR-ZIP` | `X(10)` | 102 | `addrZip` | `String` | Not used in output but parsed |
| `ACCT-GROUP-ID` | `X(10)` | 112 | `groupId` | `String` | |
| `FILLER` | `X(178)` | 122 | — | — | Ignored |

### Output record → `OutputAccountRecord`

Mirrors the COBOL `OUT-ACCT-REC` FD definition.  Key differences from input:

- `ACCT-ADDR-ZIP` is **excluded** (not in the COBOL output record)
- `OUT-ACCT-CURR-CYC-DEBIT` uses `USAGE IS COMP-3` in COBOL; in Java we write the
  plain decimal string
- Reissue date is reformatted from `YYYY-MM-DD` to `YYYYMMDD` via `DateFormatter`

### Array record → `ArrayAccountRecord`

Maps `ARR-ARRAY-REC` with `OCCURS 5 TIMES`:

```
ARR-ACCT-ID          PIC 9(11)
ARR-ACCT-BAL(1..5)
  ARR-ACCT-CURR-BAL      PIC S9(10)V99
  ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3
ARR-FILLER           PIC X(04)
```

Java uses `List<BalanceEntry>` with a nested record.  Slots 0-2 are populated with
hardcoded values per the COBOL logic; slots 3-4 remain zero (from `INITIALIZE`).

### Variable-length records → `VbrcRecord1`, `VbrcRecord2`

The COBOL `RECORDING MODE IS V` file alternates between two record layouts per account:

| Record | Length | Fields |
|--------|--------|--------|
| VB1 | 12 bytes | `ACCT-ID`, `ACCT-ACTIVE-STATUS` |
| VB2 | 39 bytes | `ACCT-ID`, `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`, `REISSUE-YYYY` |

In Java, each is a separate record type written as pipe-delimited lines to the same
file (VB1 on odd lines, VB2 on even lines).

---

## 3. Numeric Parsing: EBCDIC Overpunch

COBOL `S9(10)V99` fields use **zoned decimal** encoding where the last byte carries the
sign via EBCDIC overpunch:

| Last Byte | Sign | Digit |
|-----------|------|-------|
| `{` | + | 0 |
| `A`–`I` | + | 1–9 |
| `}` | - | 0 |
| `J`–`R` | - | 1–9 |

The `CobolDataParser.parseSignedDecimal()` method:
1. Inspects the last character for overpunch encoding
2. Extracts the digit value and sign
3. Constructs the full numeric string
4. Applies `movePointLeft(decimalPlaces)` for the implied `V99` decimal

**Example**: `00000001940{` → digits `000000019400` → `194.00` (positive, `V99` shifts 2)

This is the ASCII representation of what would be EBCDIC overpunch on the mainframe.
The sample data in `app/data/ASCII/` already uses this encoding.

---

## 4. Date Formatting

### COBOL approach

CBACT01C calls the assembler program `COBDATFT` via:
```cobol
MOVE ACCT-REISSUE-DATE TO CODATECN-INP-DATE WS-REISSUE-DATE.
MOVE '2' TO CODATECN-TYPE.
MOVE '2' TO CODATECN-OUTTYPE.
CALL 'COBDATFT' USING CODATECN-REC.
MOVE CODATECN-0UT-DATE TO OUT-ACCT-REISSUE-DATE.
```

The `CODATECN` copybook defines:
- `CODATECN-TYPE = '2'`: input is `YYYY-MM-DD`
- `CODATECN-OUTTYPE = '2'`: output is `YYYYMMDD`

### Java approach

`DateFormatter.toCompact(String hyphenated)` performs the equivalent transformation:
- Input: `"2025-05-20"` (YYYY-MM-DD)
- Output: `"20250520"` (YYYYMMDD)

This is a simple substring operation, matching the assembler's behavior without
requiring date parsing or validation (the COBOL version also does not validate).

For the VB2 record, only the 4-character year is extracted:
```java
input.reissueDate().substring(0, 4)  // "2025"
```

This maps to the COBOL:
```cobol
MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```

---

## 5. Business Logic

### Debit Override Rule

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

In Java:
```java
BigDecimal cycDebit = input.currCycDebit().compareTo(BigDecimal.ZERO) == 0
        ? new BigDecimal("2525.00")
        : input.currCycDebit();
```

This replaces zero cycle debits with a hardcoded value of 2525.00 in the output file.
When the input debit is non-zero, it is preserved as-is.

### Array Population

The COBOL `INITIALIZE ARR-ARRAY-REC` zeros all 5 slots, then populates slots 1-3:

| Slot | Balance | Debit |
|------|---------|-------|
| 1 | `ACCT-CURR-BAL` | 1005.00 |
| 2 | `ACCT-CURR-BAL` | 1525.00 |
| 3 | -1025.00 | -2500.00 |
| 4 | 0 | 0 |
| 5 | 0 | 0 |

Java mirrors this exactly with `ArrayAccountRecord.fromAccountRecord()`.

---

## 6. I/O Translation

### Input

| COBOL | Java |
|-------|------|
| VSAM KSDS (`SELECT ... ORGANIZATION IS INDEXED`) | `BufferedReader` on flat ASCII file |
| `READ ACCTFILE-FILE INTO ACCOUNT-RECORD` | `reader.readLine()` → `CobolDataParser.parseLine()` |
| File status `'00'` = OK, `'10'` = EOF | `null` return = EOF |
| File status other = ABEND | `IOException` → `RuntimeException` |

The VSAM indexed file is read sequentially in COBOL (`ACCESS MODE IS SEQUENTIAL`), so
the flat-file approach in Java is functionally equivalent.

### Outputs

All three output files use **pipe-delimited** format instead of COBOL's fixed-width
binary records.  This decision was made because:

1. Java pipe-delimited output is human-readable and easier to validate
2. The COBOL `USAGE IS COMP-3` (packed decimal) fields have no direct file equivalent
   in Java without a binary format library
3. Downstream consumers can easily parse pipe-delimited files

| COBOL File | Java File | Format |
|------------|-----------|--------|
| `OUTFILE` (sequential) | `outFile` | `acctId\|status\|bal\|...` |
| `ARRYFILE` (sequential) | `arrayFile` | `acctId\|bal1\|deb1\|bal2\|...` |
| `VBRCFILE` (variable-length) | `vbrcFile` | Alternating VB1/VB2 lines |

---

## 7. Error Handling

| COBOL Pattern | Java Pattern |
|---------------|-------------|
| File status checks after every I/O | `IOException` propagation |
| `PERFORM 9910-DISPLAY-IO-STATUS` | Error message in exception |
| `PERFORM 9999-ABEND-PROGRAM` | `throw new RuntimeException(msg)` |
| `MOVE ACCTFILE-STATUS TO IO-STATUS` | Exception includes status info |
| `GOBACK` | Method return |

The COBOL program uses a two-byte file status code and abends on any unexpected
status.  In Java, `IOException` serves the same purpose and is either handled or
propagated as a `RuntimeException`.

---

## 8. Key Differences and Risks

### Faithful translations

- Record layout byte offsets match the CVACT01Y copybook exactly
- EBCDIC overpunch decoding handles all 20 sign/digit combinations
- Date reformatting logic matches COBDATFT assembler behavior
- Debit override rule (zero → 2525.00) is identical
- Array slot population matches COBOL logic precisely
- Display output format matches COBOL DISPLAY statements

### Intentional deviations

| Area | COBOL | Java | Rationale |
|------|-------|------|-----------|
| Output format | Fixed-width binary | Pipe-delimited text | Human-readable, no COMP-3 dependency |
| VSAM access | Indexed KSDS | Flat file reader | Sequential-only access makes this equivalent |
| ABEND handling | System ABEND code | RuntimeException | Java standard error handling |
| Date validation | None (COBDATFT) | None (substring) | Preserves COBOL behavior |
| COMP-3 in output | Packed decimal on disk | Plain decimal string | No binary format needed |

### Risks to monitor

1. **COMP-3 compatibility**: If downstream programs expect packed-decimal bytes in the
   output file, the pipe-delimited format will not be compatible.  A binary writer
   would be needed for byte-level compatibility.

2. **EBCDIC vs ASCII**: The sample data uses ASCII overpunch characters.  If processing
   actual EBCDIC data from a mainframe, a character set conversion step is needed
   before parsing.

3. **Decimal precision**: `BigDecimal` preserves exact decimal arithmetic, matching
   COBOL's fixed-point behavior.  No floating-point is used anywhere.

4. **Record padding**: Lines shorter than 300 characters are right-padded with spaces
   to match the COBOL fixed-width record length.  This handles variable-length lines
   in the ASCII data file.

---

## 9. File Inventory

```
java-migration/cbact01c/
  pom.xml                                          Maven project (Java 17, JUnit 5)
  MIGRATION_NOTES.md                               This document
  src/main/java/com/carddemo/batch/
    AccountFileProcessor.java                      Main batch processor
    io/
      CobolDataParser.java                         Fixed-width record parser + overpunch
      RecordWriter.java                            Pipe-delimited output writer
    model/
      AccountRecord.java                           Input record (CVACT01Y mapping)
      OutputAccountRecord.java                     Output record with business logic
      ArrayAccountRecord.java                      Array record (OCCURS 5 TIMES)
      VbrcRecord1.java                             Variable-length record type 1
      VbrcRecord2.java                             Variable-length record type 2
    util/
      DateFormatter.java                           COBDATFT replacement
  src/test/java/com/carddemo/batch/
    AccountFileProcessorTest.java                  Integration tests (17 tests)
    CobolDataParserTest.java                       Parser unit tests (15 tests)
    DateFormatterTest.java                         Date utility tests (11 tests)
```

**Total: 43 tests, all passing.**

---

## 10. How to Build and Run

```bash
cd java-migration/cbact01c

# Build and run tests
mvn clean verify

# Run the processor against sample data
mvn exec:java -Dexec.mainClass="com.carddemo.batch.AccountFileProcessor" \
  -Dexec.args="../../app/data/ASCII/acctdata.txt out.txt array.txt vbrc.txt"
```

The processor reads the account file and writes three output files, printing each
record's fields to stdout in the same format as the COBOL DISPLAY statements.
