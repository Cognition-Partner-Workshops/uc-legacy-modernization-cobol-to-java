# CBACT01C Migration Notes — COBOL to Java 17

## Overview

| Attribute          | COBOL                          | Java                                    |
|--------------------|--------------------------------|-----------------------------------------|
| Program            | `CBACT01C.cbl`                 | `Cbact01cApplication.java`              |
| Type               | Batch (COBOL + CICS/JCL)       | Standalone CLI (Java 17, Maven)         |
| Input              | VSAM KSDS indexed file         | Flat-file (ASCII, one record per line)  |
| Output files       | 3 sequential files             | 3 text files (UTF-8)                    |
| Date conversion    | ASM routine `COBDATFT`         | `DateConverter.java`                    |
| Abort mechanism    | `CEE3ABD` (Language Env abend) | `IOException` / `System.exit(1)`        |

---

## Copybook → Java Record Mappings

### CVACT01Y.cpy → `AccountRecord.java`

| COBOL Field               | PIC Clause        | Bytes | Java Field              | Java Type      | Notes                                      |
|---------------------------|--------------------|-------|--------------------------|----------------|---------------------------------------------|
| `ACCT-ID`                 | `9(11)`            | 11    | `acctId`                 | `long`         | Unsigned numeric, zero-padded               |
| `ACCT-ACTIVE-STATUS`      | `X(01)`            | 1     | `acctActiveStatus`       | `String`       |                                              |
| `ACCT-CURR-BAL`           | `S9(10)V99`        | 12    | `acctCurrBal`            | `BigDecimal`   | Signed zoned decimal, implied 2 dec places  |
| `ACCT-CREDIT-LIMIT`       | `S9(10)V99`        | 12    | `acctCreditLimit`        | `BigDecimal`   | Same encoding                                |
| `ACCT-CASH-CREDIT-LIMIT`  | `S9(10)V99`        | 12    | `acctCashCreditLimit`    | `BigDecimal`   | Same encoding                                |
| `ACCT-OPEN-DATE`          | `X(10)`            | 10    | `acctOpenDate`           | `String`       | `YYYY-MM-DD` format                         |
| `ACCT-EXPIRAION-DATE`     | `X(10)`            | 10    | `acctExpiraionDate`      | `String`       | Preserves original COBOL spelling            |
| `ACCT-REISSUE-DATE`       | `X(10)`            | 10    | `acctReissueDate`        | `String`       | `YYYY-MM-DD` format                         |
| `ACCT-CURR-CYC-CREDIT`    | `S9(10)V99`        | 12    | `acctCurrCycCredit`      | `BigDecimal`   | Signed zoned decimal                         |
| `ACCT-CURR-CYC-DEBIT`     | `S9(10)V99`        | 12    | `acctCurrCycDebit`       | `BigDecimal`   | Signed zoned decimal                         |
| `ACCT-ADDR-ZIP`           | `X(10)`            | 10    | `acctAddrZip`            | `String`       | Not used by CBACT01C logic                   |
| `ACCT-GROUP-ID`           | `X(10)`            | 10    | `acctGroupId`            | `String`       |                                              |
| `FILLER`                  | `X(178)`           | 178   | *(not mapped)*           | —              | Padding to reach 300-byte record length      |

**Total record length: 300 bytes**

### CODATECN.cpy → `DateConverter.java`

| COBOL Field            | Purpose                    | Java Equivalent                |
|------------------------|----------------------------|--------------------------------|
| `CODATECN-TYPE`        | Input format selector      | `inputType` parameter          |
| `CODATECN-INP-DATE`    | Input date string          | `inputDate` parameter          |
| `CODATECN-OUTTYPE`     | Output format selector     | `outputType` parameter         |
| `CODATECN-0UT-DATE`    | Output date string         | Return value                   |

Format codes:

| Code | Input Meaning  | Output Meaning |
|------|----------------|----------------|
| `1`  | `YYYYMMDD`     | `YYYY-MM-DD`   |
| `2`  | `YYYY-MM-DD`   | `YYYYMMDD`     |

CBACT01C always calls with input type `2`, output type `2` (i.e., `YYYY-MM-DD` → `YYYYMMDD`).

---

## Output File Field Mappings

### OUTFILE → `OutputAccountRecord.java`

| COBOL Field                     | PIC Clause             | Java Field              | Notes                                           |
|---------------------------------|------------------------|--------------------------|--------------------------------------------------|
| `OUT-ACCT-ID`                   | `9(11)`                | `acctId`                 | Direct copy from input                           |
| `OUT-ACCT-ACTIVE-STATUS`        | `X(01)`                | `acctActiveStatus`       | Direct copy                                      |
| `OUT-ACCT-CURR-BAL`             | `S9(10)V99`            | `acctCurrBal`            | Direct copy                                      |
| `OUT-ACCT-CREDIT-LIMIT`         | `S9(10)V99`            | `acctCreditLimit`        | Direct copy                                      |
| `OUT-ACCT-CASH-CREDIT-LIMIT`    | `S9(10)V99`            | `acctCashCreditLimit`    | Direct copy                                      |
| `OUT-ACCT-OPEN-DATE`            | `X(10)`                | `acctOpenDate`           | Direct copy                                      |
| `OUT-ACCT-EXPIRAION-DATE`       | `X(10)`                | `acctExpiraionDate`      | Direct copy                                      |
| `OUT-ACCT-REISSUE-DATE`         | `X(10)`                | `acctReissueDate`        | **Converted** from `YYYY-MM-DD` to `YYYYMMDD`   |
| `OUT-ACCT-CURR-CYC-CREDIT`      | `S9(10)V99`            | `acctCurrCycCredit`      | Direct copy                                      |
| `OUT-ACCT-CURR-CYC-DEBIT`       | `S9(10)V99 COMP-3`     | `acctCurrCycDebit`       | **Default 2525.00 if input is zero**; COMP-3     |
| `OUT-ACCT-GROUP-ID`             | `X(10)`                | `acctGroupId`            | Direct copy                                      |

### ARRYFILE → `ArrayRecord.java`

| COBOL Field                          | PIC Clause         | Slot | Java Field       | Value Source                    |
|--------------------------------------|--------------------|------|-------------------|---------------------------------|
| `ARR-ACCT-ID`                        | `9(11)`            | —    | `acctId`          | Direct copy from input          |
| `ARR-ACCT-CURR-BAL(1)`              | `S9(10)V99`        | 1    | `balances[0]`     | Input `ACCT-CURR-BAL`           |
| `ARR-ACCT-CURR-CYC-DEBIT(1)`        | `S9(10)V99 COMP-3` | 1    | `debits[0]`       | Hard-coded `1005.00`            |
| `ARR-ACCT-CURR-BAL(2)`              | `S9(10)V99`        | 2    | `balances[1]`     | Input `ACCT-CURR-BAL`           |
| `ARR-ACCT-CURR-CYC-DEBIT(2)`        | `S9(10)V99 COMP-3` | 2    | `debits[1]`       | Hard-coded `1525.00`            |
| `ARR-ACCT-CURR-BAL(3)`              | `S9(10)V99`        | 3    | `balances[2]`     | Hard-coded `-1025.00`           |
| `ARR-ACCT-CURR-CYC-DEBIT(3)`        | `S9(10)V99 COMP-3` | 3    | `debits[2]`       | Hard-coded `-2500.00`           |
| `ARR-ACCT-CURR-BAL(4)`              | `S9(10)V99`        | 4    | `balances[3]`     | Zero (INITIALIZE)               |
| `ARR-ACCT-CURR-CYC-DEBIT(4)`        | `S9(10)V99 COMP-3` | 4    | `debits[3]`       | Zero (INITIALIZE)               |
| `ARR-ACCT-CURR-BAL(5)`              | `S9(10)V99`        | 5    | `balances[4]`     | Zero (INITIALIZE)               |
| `ARR-ACCT-CURR-CYC-DEBIT(5)`        | `S9(10)V99 COMP-3` | 5    | `debits[4]`       | Zero (INITIALIZE)               |
| `ARR-FILLER`                         | `X(04)`            | —    | *(4 spaces)*      | Padding                         |

### VBRCFILE → `VbrcRecord1.java` + `VbrcRecord2.java`

The COBOL program writes two variable-length records per account.

**VB1 (12 bytes):**

| COBOL Field              | PIC Clause | Java Field          |
|--------------------------|------------|----------------------|
| `VB1-ACCT-ID`            | `9(11)`    | `acctId`             |
| `VB1-ACCT-ACTIVE-STATUS` | `X(01)`    | `acctActiveStatus`   |

**VB2 (39 bytes):**

| COBOL Field              | PIC Clause     | Java Field          | Notes                           |
|--------------------------|----------------|----------------------|---------------------------------|
| `VB2-ACCT-ID`            | `9(11)`        | `acctId`             |                                  |
| `VB2-ACCT-CURR-BAL`      | `S9(10)V99`    | `acctCurrBal`        |                                  |
| `VB2-ACCT-CREDIT-LIMIT`  | `S9(10)V99`    | `acctCreditLimit`    |                                  |
| `VB2-ACCT-REISSUE-YYYY`  | `X(04)`        | `acctReissueYyyy`    | First 4 chars of reissue date   |

---

## Signed Zoned-Decimal Encoding

The sample data uses ASCII zoned-decimal representation with a trailing sign overpunch:

| Last Char | Digit | Sign     |
|-----------|-------|----------|
| `{`       | 0     | Positive |
| `A`–`I`   | 1–9   | Positive |
| `}`       | 0     | Negative |
| `J`–`R`   | 1–9   | Negative |

Example: `00000001940{` → digits `000000019400`, positive → `+0000000194.00`

The Java implementation provides full round-trip support via:
- `AccountRecord.parseSignedZonedDecimal()` — decoding
- `OutputAccountRecord.formatSignedZoned()` — encoding

---

## Business Logic Transformations

1. **Date reformatting**: The reissue date is converted from `YYYY-MM-DD` to `YYYYMMDD` via the `COBDATFT` assembler routine (Java: `DateConverter.convert()`).

2. **Default debit substitution**: If `ACCT-CURR-CYC-DEBIT` equals zero, the output record's debit is set to `2525.00`. All 50 sample records have zero debit, so this default always applies for the sample data.

3. **Array record hard-coded values**: The OCCURS array has 5 slots but only 3 are populated with non-zero values. Slots 4–5 remain zeroed from the COBOL `INITIALIZE` statement.

4. **Variable-length record split**: Each account produces two VBRC records — a short (12-byte) summary with ID + status, and a longer (39-byte) record with ID + balance + credit limit + reissue year.

---

## COMP-3 Representation

COBOL `USAGE IS COMP-3` (packed decimal) stores two digits per byte in binary.  In this Java modernization, COMP-3 fields are rendered as human-readable signed strings (`+`/`-` prefix + 12 digits) for portability and test comparison. A binary-compatible COMP-3 encoder/decoder can be added if downstream systems require the packed format.

---

## Files and Structure

```
java-modernization/
├── pom.xml                          # Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md               # This file
└── src/
    ├── main/java/com/carddemo/batch/
    │   ├── Cbact01cApplication.java   # Main batch program
    │   ├── model/
    │   │   ├── AccountRecord.java     # Input record (CVACT01Y)
    │   │   ├── OutputAccountRecord.java # OUTFILE record
    │   │   ├── ArrayRecord.java       # ARRYFILE record
    │   │   ├── VbrcRecord1.java       # VBRCFILE short record
    │   │   └── VbrcRecord2.java       # VBRCFILE long record
    │   └── util/
    │       └── DateConverter.java     # COBDATFT replacement
    └── test/
        ├── java/com/carddemo/batch/
        │   └── Cbact01cApplicationTest.java  # 25 JUnit 5 tests
        └── resources/
            └── acctdata.txt           # Sample data (50 accounts)
```

## Running

```bash
# Build
cd java-modernization
export JAVA_HOME=/path/to/jdk17
mvn clean package

# Run
java -jar target/cbact01c-java-1.0.0.jar <acctfile> <outfile> <arryfile> <vbrcfile>

# Tests
mvn test
```
