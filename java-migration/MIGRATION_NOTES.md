# CBACT01C Migration Notes — COBOL to Java 21

## 1. Program Overview

**CBACT01C** is a batch COBOL program from the CardDemo mainframe credit card management system. It reads an indexed VSAM (KSDS) account file sequentially and produces three output files in different formats.

### Data Flow

```
ACCTFILE (KSDS VSAM, 300-byte records)
    │
    ├──► OUTFILE    — Flat sequential: reformatted account records
    ├──► ARRYFILE   — Array-structured: 5 balance/debit pairs per account
    └──► VBRCFILE   — Variable-length: 2 records per account (short + long)
```

### Input Record Layout (CVACT01Y.cpy — 300 bytes)

| Field               | PIC              | Bytes | Java Type    |
|---------------------|------------------|------:|--------------|
| ACCT-ID             | 9(11)            |    11 | `String`     |
| ACCT-ACTIVE-STATUS  | X(01)            |     1 | `String`     |
| ACCT-CURR-BAL       | S9(10)V99        |    12 | `BigDecimal` |
| ACCT-CREDIT-LIMIT   | S9(10)V99        |    12 | `BigDecimal` |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99     |    12 | `BigDecimal` |
| ACCT-OPEN-DATE      | X(10)            |    10 | `String`     |
| ACCT-EXPIRAION-DATE | X(10)            |    10 | `String`     |
| ACCT-REISSUE-DATE   | X(10)            |    10 | `String`     |
| ACCT-CURR-CYC-CREDIT| S9(10)V99       |    12 | `BigDecimal` |
| ACCT-CURR-CYC-DEBIT | S9(10)V99        |    12 | `BigDecimal` |
| ACCT-ADDR-ZIP       | X(10)            |    10 | `String`     |
| ACCT-GROUP-ID       | X(10)            |    10 | `String`     |
| FILLER              | X(178)           |   178 | (discarded)  |

> Note: The COBOL source preserves the typo "EXPIRAION" (missing 'T'). The Java migration keeps the same field semantics but uses `expirationDate` in the record.

## 2. COBOL-to-Java Type Mappings

| COBOL Type         | Meaning                          | Java Type      | Notes |
|--------------------|----------------------------------|----------------|-------|
| `PIC 9(n)`         | Unsigned numeric display          | `String`       | Preserves leading zeros for IDs |
| `PIC X(n)`         | Alphanumeric                      | `String`       | Fixed-width, space-padded |
| `PIC S9(n)V99`     | Signed zoned decimal (DISPLAY)    | `BigDecimal`   | 2 implied decimal places |
| `PIC S9(n)V99 COMP-3` | Packed decimal (BCD)           | `BigDecimal`   | Written as zoned-decimal in ASCII output |
| `PIC S9(9) COMP`   | Binary fullword                   | `int`          | Used for status codes only |
| `PIC S9(9) BINARY` | Binary fullword                   | `int`          | ABEND code / timing |
| `OCCURS n TIMES`   | Fixed-size array                  | `List<T>`      | Bounded, immutable after construction |

### Zoned-Decimal Sign Overpunch (ASCII)

The input data files use the standard COBOL ASCII overpunch convention on the last character:

| Char | Digit | Sign |     | Char | Digit | Sign |
|------|------:|------|-----|------|------:|------|
| `{`  |     0 | +    |     | `}`  |     0 | −    |
| `A`  |     1 | +    |     | `J`  |     1 | −    |
| `B`  |     2 | +    |     | `K`  |     2 | −    |
| ...  |   ... | +    |     | ...  |   ... | −    |
| `I`  |     9 | +    |     | `R`  |     9 | −    |

Implemented in `CobolDecimalParser.java`.

## 3. Business Rules Extracted

### Rule 1: Cycle Debit Default Substitution
```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```
When the input cycle debit is zero, the output record substitutes **2525.00**. This appears to be a sentinel/default value for downstream processing. All 50 records in the test data have zero cycle debit, so every output record receives this substitution.

### Rule 2: Reissue Date Format Conversion
```cobol
CALL 'COBDATFT' USING CODATECN-REC.
```
The COBOL program calls the COBDATFT assembler routine with input type `'2'` (YYYY-MM-DD) and output type `'2'` (YYYYMMDD). This converts the reissue date from `2025-05-20` to `20250520`. In Java, this is handled by `DateConverter.convertDashToCompact()` using `java.time.LocalDate`.

### Rule 3: Array Record Population (Hardcoded Values)
For the ARRYFILE, each account produces a record with 5 balance/debit pairs. Only 3 are populated with meaningful data; the remaining 2 are zeros (from `INITIALIZE`):

| Entry | Balance          | Cycle Debit |
|------:|-----------------|-------------|
|     1 | Actual balance  | 1005.00     |
|     2 | Actual balance  | 1525.00     |
|     3 | −1025.00        | −2500.00    |
|   4–5 | 0.00            | 0.00        |

The hardcoded values (1005.00, 1525.00, −1025.00, −2500.00) appear to be test scaffolding demonstrating the `OCCURS` clause and COMP-3 handling rather than production business logic.

### Rule 4: Variable-Length Record Split
Each account produces two VB records:
- **VB1** (12 bytes): Account ID + Active Status — a "header" record
- **VB2** (39 bytes): Account ID + Balance + Credit Limit + Reissue Year — a "detail" record

### Rule 5: Error Handling → ABEND
Any file I/O error triggers an immediate abnormal end (`CEE3ABD` with code 999). In Java, this maps to throwing an `IOException` which propagates to the caller. The COBOL status-code display logic (`9910-DISPLAY-IO-STATUS`) is replaced by Java's exception message.

## 4. Ambiguities and Design Decisions

### A1: COMP-3 in Output File
The COBOL output record declares `OUT-ACCT-CURR-CYC-DEBIT` as `USAGE IS COMP-3` (packed decimal), which would produce a binary-encoded field in the mainframe output. Since the Java version writes ASCII text files (not EBCDIC binary datasets), we serialize all numeric fields as zoned-decimal text. This preserves readability and parseability at the cost of not being binary-identical to the COBOL output.

### A2: ADDR-ZIP Not Carried to Output
The input record contains `ACCT-ADDR-ZIP` (PIC X(10)), but none of the three output files include it. The COBOL program reads it but never moves it to any output structure. The Java `AccountRecord` preserves the field for completeness.

### A3: GROUP-ID Whitespace
In the test data, GROUP-ID is always spaces. The program copies it verbatim to the output record. The Java version preserves this behavior.

### A4: Reissue Date in VB2 Record
The VB2 record stores only the 4-digit year of the reissue date (`WS-ACCT-REISSUE-YYYY`), extracted before the COBDATFT call. The Java version uses `DateConverter.extractYear()` for clarity.

### A5: Record Length in Variable-Length File
In COBOL, `WS-RECD-LEN` controls the actual bytes written via `RECORDING MODE IS V`. In the Java ASCII output, each VB record is simply a variable-length line terminated by a newline. The line lengths match the COBOL record lengths (12 and 39 characters respectively).

### A6: FILLER Handling
The 178-byte FILLER in the input record is parsed but discarded. Output records do not include trailing filler — the COBOL program does not write it either.

## 5. Java Project Structure

```
java-migration/
├── pom.xml                               # Maven build (Java 21, JUnit 5)
├── MIGRATION_NOTES.md                    # This file
└── src/
    ├── main/java/com/carddemo/batch/
    │   ├── AccountFileProcessor.java     # Main batch processor (≡ CBACT01C)
    │   ├── io/
    │   │   ├── AccountFileReader.java    # Parses 300-byte fixed-width records
    │   │   └── CobolDecimalParser.java   # Zoned-decimal ↔ BigDecimal
    │   ├── model/
    │   │   ├── AccountRecord.java        # ≡ CVACT01Y copybook
    │   │   ├── OutputAccountRecord.java  # ≡ OUT-ACCT-REC (OUTFILE)
    │   │   ├── ArrayAccountRecord.java   # ≡ ARR-ARRAY-REC (ARRYFILE)
    │   │   ├── VbRecord1.java            # ≡ VBRC-REC1 (12-byte short)
    │   │   └── VbRecord2.java            # ≡ VBRC-REC2 (39-byte long)
    │   └── util/
    │       └── DateConverter.java        # ≡ COBDATFT assembler call
    └── test/
        ├── java/com/carddemo/batch/
        │   ├── AccountFileProcessorTest.java  # End-to-end parity tests
        │   ├── CobolDecimalParserTest.java    # Zoned-decimal unit tests
        │   └── DateConverterTest.java         # Date conversion unit tests
        └── resources/
            └── acctdata.txt                   # Fixture from app/data/ASCII/
```

## 6. Running

```bash
# Build
cd java-migration
JAVA_HOME=/path/to/jdk-21 mvn clean package

# Run
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    /tmp/outfile.txt \
    /tmp/arryfile.txt \
    /tmp/vbrcfile.txt

# Tests only
mvn test
```
