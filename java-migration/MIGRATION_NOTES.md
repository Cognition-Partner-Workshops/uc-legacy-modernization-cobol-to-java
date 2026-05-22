# CBACT01C Migration Notes — COBOL to Java 17+

## 1. Program Overview

**CBACT01C** is a batch COBOL program in the CardDemo mainframe application. It reads
an indexed VSAM KSDS account file sequentially and writes transformed data to three
output files:

| COBOL File      | Type                        | Java Equivalent       |
|-----------------|-----------------------------|-----------------------|
| `ACCTFILE`      | VSAM KSDS (indexed, input)  | Line-delimited text   |
| `OUTFILE`       | Sequential (output)         | Pipe-delimited text   |
| `ARRYFILE`      | Sequential (output)         | Pipe-delimited text   |
| `VBRCFILE`      | Variable-length (output)    | Pipe-delimited text   |

## 2. Data Structure Mapping

### 2.1 CVACT01Y — Account Record (300 bytes)

| COBOL Field              | PIC              | Java Type      | Notes                                   |
|--------------------------|------------------|----------------|-----------------------------------------|
| `ACCT-ID`                | `9(11)`          | `long`         | 11-digit numeric account identifier     |
| `ACCT-ACTIVE-STATUS`     | `X(01)`          | `String`       | "Y" or "N"                              |
| `ACCT-CURR-BAL`          | `S9(10)V99`      | `BigDecimal`   | Signed, 2 implied decimal places        |
| `ACCT-CREDIT-LIMIT`      | `S9(10)V99`      | `BigDecimal`   |                                         |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`      | `BigDecimal`   |                                         |
| `ACCT-OPEN-DATE`         | `X(10)`          | `String`       | `YYYY-MM-DD` format                     |
| `ACCT-EXPIRAION-DATE`    | `X(10)`          | `String`       | Typo preserved from original            |
| `ACCT-REISSUE-DATE`      | `X(10)`          | `String`       | `YYYY-MM-DD` format                     |
| `ACCT-CURR-CYC-CREDIT`   | `S9(10)V99`      | `BigDecimal`   |                                         |
| `ACCT-CURR-CYC-DEBIT`    | `S9(10)V99`      | `BigDecimal`   |                                         |
| `ACCT-ADDR-ZIP`          | `X(10)`          | `String`       |                                         |
| `ACCT-GROUP-ID`          | `X(10)`          | `String`       |                                         |
| `FILLER`                 | `X(178)`         | *(omitted)*    | Padding to reach 300-byte record length |

### 2.2 CODATECN — Date Conversion Record

The COBOL program calls an assembler routine `COBDATFT` via the `CODATECN` copybook to
convert between `YYYY-MM-DD` (type 2) and `YYYYMMDD` (type 2 output) date formats.

**Java replacement:** `DateConverter` utility class with a static `convert()` method
supporting both format types.

### 2.3 Output Record Structures

| COBOL Structure    | Java Record            | Key Difference                              |
|--------------------|------------------------|---------------------------------------------|
| `OUT-ACCT-REC`     | `OutputAccountRecord`  | COMP-3 debit → `BigDecimal`                 |
| `ARR-ARRAY-REC`    | `ArrayRecord`          | `OCCURS 5 TIMES` → `BalanceSlot[5]` array   |
| `VBRC-REC1`        | `VbRecord1`            | 12-byte variable-length → plain record      |
| `VBRC-REC2`        | `VbRecord2`            | 39-byte variable-length → plain record      |

## 3. Translation Decisions

### 3.1 File I/O

| Decision | Rationale |
|----------|-----------|
| VSAM KSDS → line-delimited text file | Java has no native VSAM support; sequential reading maps directly to `BufferedReader.readLine()` |
| Fixed-width binary output → pipe-delimited text | Eliminates EBCDIC/COMP-3 encoding concerns; human-readable and easily consumed by downstream Java/ETL processes |
| Variable-length records (VBRC-FILE) → two separate lines per account | The COBOL `RECORDING MODE V` with different `WS-RECD-LEN` values is replaced by distinct lines; the VB1/VB2 split is preserved |
| `try-with-resources` for all file handles | Replaces manual OPEN/CLOSE with guaranteed cleanup; mirrors the COBOL open-process-close pattern |

### 3.2 Numeric Types

| Decision | Rationale |
|----------|-----------|
| `PIC S9(10)V99` → `BigDecimal` | Preserves exact decimal arithmetic without floating-point rounding, matching COBOL's fixed-point behavior |
| `COMP-3` (packed decimal) → `BigDecimal` | Java has no native packed-decimal type; `BigDecimal` provides identical precision |
| `PIC 9(11)` (account ID) → `long` | 11-digit integers fit within `long` range; avoids `BigDecimal` overhead for non-monetary values |

### 3.3 Data Structures

| Decision | Rationale |
|----------|-----------|
| Java `record` types for all data structures | Immutable value objects matching COBOL's pass-by-copy semantics; concise, equals/hashCode/toString auto-generated |
| `OCCURS 5 TIMES` → `BalanceSlot[]` array of records | Preserves the COBOL array structure with named sub-fields instead of parallel arrays |
| `REDEFINES` → separate parsing logic | Java lacks union types; the `CODATECN` redefines are handled by explicit substring parsing in `DateConverter` |
| `FILLER` fields omitted | No semantic value; Java serialization uses explicit field lists |

### 3.4 Business Logic

| COBOL Behavior | Java Implementation | Notes |
|----------------|---------------------|-------|
| If `ACCT-CURR-CYC-DEBIT = ZERO` then set to `2525.00` | `BigDecimal.ZERO` comparison → substitute `DEFAULT_CYCLE_DEBIT` | Exact replication of the zero-substitution rule |
| Array slots 1-2 use account balance; slot 3 hardcoded negatives; slots 4-5 zeroed via `INITIALIZE` | Explicit assignment in `buildArrayRecord()` with named constants | Constants `ARR_DEBIT_SLOT1`, `ARR_DEBIT_SLOT2`, `ARR_BAL_SLOT3`, `ARR_DEBIT_SLOT3` document the hardcoded values |
| Date conversion via `CALL 'COBDATFT'` | `DateConverter.convert(reissueDate, "2", "2")` | Assembler call replaced by pure Java; type/outtype codes preserved |
| VB2 reissue year extracted from `WS-ACCT-REISSUE-YYYY` | `reissueDate.substring(0, 4)` | First 4 chars of YYYY-MM-DD date |
| `DISPLAY` statements for logging | `PrintStream` injection (defaults to `System.out`) | Enables capture in tests; matches COBOL SYSOUT behavior |

### 3.5 Error Handling

| COBOL Behavior | Java Implementation |
|----------------|---------------------|
| File status checks after every I/O, `PERFORM 9999-ABEND-PROGRAM` on failure | `IOException` propagation; `main()` catches and exits with code 999 |
| `9910-DISPLAY-IO-STATUS` decodes binary file status | Stack traces provide equivalent diagnostic information |
| `CALL 'CEE3ABD'` for abend | `System.exit(999)` in CLI; `IOException` throw in library use |

### 3.6 What Was Not Migrated

| Item | Reason |
|------|--------|
| EBCDIC encoding | Input assumed to be ASCII/UTF-8; EBCDIC conversion is an infrastructure concern handled at the data-migration layer |
| VSAM indexed access (random/dynamic) | CBACT01C only uses sequential access; indexed lookup not exercised |
| JCL job control | Replaced by CLI arguments or orchestration framework (e.g., Spring Batch) |
| `COBSWAIT` / `CEE3ABD` system calls | Timer waits and LE abend services have no direct Java equivalent; replaced by standard exception handling |

## 4. Project Structure

```
java-migration/
├── pom.xml                                    # Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md                         # This document
└── src/
    ├── main/java/com/cardemo/batch/
    │   ├── Cbact01cProcessor.java             # Main batch processor (PROCEDURE DIVISION)
    │   ├── model/
    │   │   ├── AccountRecord.java             # CVACT01Y copybook
    │   │   ├── OutputAccountRecord.java       # OUT-ACCT-REC file descriptor
    │   │   ├── ArrayRecord.java               # ARR-ARRAY-REC with OCCURS
    │   │   ├── VbRecord1.java                 # VBRC-REC1 (short VB record)
    │   │   └── VbRecord2.java                 # VBRC-REC2 (long VB record)
    │   └── util/
    │       └── DateConverter.java             # COBDATFT assembler replacement
    └── test/
        ├── java/com/cardemo/batch/
        │   ├── Cbact01cProcessorTest.java     # Integration & unit tests (14 tests)
        │   ├── model/
        │   │   └── AccountRecordTest.java     # Record parsing tests (5 tests)
        │   └── util/
        │       └── DateConverterTest.java     # Date conversion tests (10 tests)
        └── resources/
            └── sample-accounts.dat            # 3-record sample input file
```

## 5. Running

```bash
# Build and test
cd java-migration
mvn clean test

# Run standalone
mvn package -DskipTests
java -jar target/cbact01c-migration-1.0.0.jar <acctFile> <outFile> <arrFile> <vbFile>
```

## 6. Test Coverage Summary

| Test Class               | Tests | Coverage Focus                                    |
|--------------------------|-------|---------------------------------------------------|
| `Cbact01cProcessorTest`  | 14    | End-to-end processing, zero-debit substitution, date conversion, negative balances, file I/O, console output, error handling |
| `AccountRecordTest`      | 5     | Field parsing, negative values, zero values, short lines, display formatting |
| `DateConverterTest`      | 10    | YYYY-MM-DD↔YYYYMMDD, null/blank input, unknown types |
| **Total**                | **29**|                                                   |
