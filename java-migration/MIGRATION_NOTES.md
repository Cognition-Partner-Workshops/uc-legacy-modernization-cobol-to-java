# CBACT01C Migration Notes — COBOL to Java 17+

## Overview

This document records the translation decisions made when migrating the **CBACT01C** batch COBOL program to Java 17+. CBACT01C reads an indexed VSAM account file sequentially and writes three output files containing different projections of the account data.

---

## Source Program Summary

| Attribute       | Value                                        |
|-----------------|----------------------------------------------|
| Program ID      | CBACT01C                                     |
| Type            | Batch COBOL                                  |
| Application     | CardDemo (AWS mainframe modernization demo)  |
| Input           | VSAM KSDS account file (300-byte records)    |
| Outputs         | OUT-FILE, ARRY-FILE, VBRC-FILE               |
| Copybooks used  | CVACT01Y (account record), CODATECN (date conversion) |
| External calls  | COBDATFT (assembler date formatter)          |

---

## Data Structure Translations

### CVACT01Y Copybook → `AccountRecord` (Java record)

| COBOL Field             | PIC              | Java Type      | Notes                                    |
|-------------------------|------------------|----------------|------------------------------------------|
| ACCT-ID                 | 9(11)            | `long`         | Zero-padded 11-digit string for display  |
| ACCT-ACTIVE-STATUS      | X(01)            | `String`       |                                          |
| ACCT-CURR-BAL           | S9(10)V99        | `BigDecimal`   | Trailing overpunch, implied 2-decimal    |
| ACCT-CREDIT-LIMIT       | S9(10)V99        | `BigDecimal`   |                                          |
| ACCT-CASH-CREDIT-LIMIT  | S9(10)V99        | `BigDecimal`   |                                          |
| ACCT-OPEN-DATE          | X(10)            | `String`       | Format: YYYY-MM-DD                       |
| ACCT-EXPIRAION-DATE     | X(10)            | `String`       | Typo preserved from COBOL source         |
| ACCT-REISSUE-DATE       | X(10)            | `String`       |                                          |
| ACCT-CURR-CYC-CREDIT    | S9(10)V99        | `BigDecimal`   |                                          |
| ACCT-CURR-CYC-DEBIT     | S9(10)V99        | `BigDecimal`   |                                          |
| ACCT-ADDR-ZIP           | X(10)            | `String`       |                                          |
| ACCT-GROUP-ID           | X(10)            | `String`       |                                          |
| FILLER                  | X(178)           | *(discarded)*  | Padding to 300 bytes, not needed in Java |

**Decision**: Java `record` types are used for all data structures. Records are immutable, concise, and automatically provide `equals()`, `hashCode()`, and `toString()` — a good match for COBOL copybook structures which are pure data containers.

### CODATECN Copybook → `DateConverter` utility

The CODATECN copybook defines an asymmetric type-code system:

| Code | Input Meaning | Output Meaning |
|------|---------------|----------------|
| `1`  | YYYYMMDD      | YYYY-MM-DD     |
| `2`  | YYYY-MM-DD    | YYYYMMDD       |

CBACT01C uses input type `2` and output type `2`, converting `YYYY-MM-DD → YYYYMMDD`.

---

## Key Translation Decisions

### 1. Numeric Representation: COBOL Display → `BigDecimal`

COBOL `PIC S9(10)V99 DISPLAY` stores numbers as zoned-decimal with a trailing sign overpunch:

- `{` = +0, `A`–`I` = +1 to +9
- `}` = -0, `J`–`R` = -1 to -9

**Decision**: All monetary/numeric fields use `java.math.BigDecimal` to preserve exact decimal arithmetic, matching COBOL's fixed-point semantics. The `CobolDecimalParser` utility handles the overpunch decoding.

### 2. COMP-3 (Packed Decimal) Fields

The COBOL output records use `USAGE IS COMP-3` for some fields (`OUT-ACCT-CURR-CYC-DEBIT`, `ARR-ACCT-CURR-CYC-DEBIT`). COMP-3 is a binary packed-decimal storage format.

**Decision**: In Java, all values are represented as `BigDecimal` regardless of COBOL storage format. The COMP-3 distinction only affects on-disk binary layout which is not relevant when using text-based output in Java. The output files use pipe-delimited text instead of fixed-width binary to improve readability and interoperability.

### 3. File I/O: VSAM KSDS → Sequential Text File

COBOL reads the account file as a VSAM KSDS (Key-Sequenced Data Set) with `ACCESS MODE IS SEQUENTIAL`.

**Decision**: The Java version reads a flat text file with fixed-width 300-byte records (one per line), matching the ASCII data files provided in `app/data/ASCII/acctdata.txt`. This is the standard approach for batch migration where the data has been exported from VSAM to flat files.

### 4. Output File Format: Fixed-Width Binary → Pipe-Delimited Text

COBOL writes fixed-width records with binary (COMP-3) fields directly to sequential files.

**Decision**: Java writes pipe-delimited (`|`) text files. This trades binary compactness for human readability and platform independence. Each record type provides `toDelimitedLine()` and `fromDelimitedLine()` methods for serialization/deserialization.

### 5. Variable-Length Records (VBRC-FILE)

COBOL uses `RECORDING MODE IS V` with `RECORD IS VARYING IN SIZE FROM 10 TO 80` to write two different record types to a single file:
- VBRC-REC1: 12 bytes (account ID + active status)
- VBRC-REC2: 39 bytes (account ID + balance + credit limit + reissue year)

**Decision**: In Java, both record types are written as separate lines to the same file, alternating REC1/REC2 for each account. The record type is implicitly identified by field count (2 fields vs 4 fields in the pipe-delimited format).

### 6. COBDATFT Assembler Call → `DateConverter` Utility

CBACT01C calls the assembler program `COBDATFT` to convert dates between formats.

**Decision**: Replaced with a pure Java utility class `DateConverter` that performs the same string manipulation. The asymmetric type-code semantics from the `CODATECN` copybook are preserved exactly (input type "2" = YYYY-MM-DD, output type "2" = YYYYMMDD).

### 7. COBOL INITIALIZE → Java Constructor Defaults

COBOL `INITIALIZE ARR-ARRAY-REC` zeros all numeric fields and spaces all alphanumeric fields.

**Decision**: The `ArrayRecord.of(acctId, entries...)` varargs factory creates an immutable array record, auto-padding unfilled slots with `BigDecimal.ZERO` to match COBOL INITIALIZE behavior. The record stores an unmodifiable `List<BalanceEntry>` via `List.copyOf()`.

### 8. Error Handling: Abend → Exception

COBOL calls `CEE3ABD` (Language Environment abend) with an abend code on any I/O error.

**Decision**: An `AbendException` (unchecked) is thrown with the same abend code (999). The main application catches it and calls `System.exit()` with the abend code, mirroring the COBOL behavior. Individual I/O operations are wrapped in try-catch blocks that log the error status before throwing.

### 9. Business Rule: Zero Debit Substitution

COBOL paragraph `1300-POPUL-ACCT-RECORD` contains:
```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF
```

**Decision**: This rule is preserved exactly in `Cbact01cProcessor.populateOutRecord()`. The magic number `2525.00` is extracted as a named constant `DEFAULT_CYCLE_DEBIT` for clarity.

### 10. Array Population (Fixed Values)

COBOL paragraph `1400-POPUL-ARRAY-RECORD` populates a 5-element array with hardcoded values:
- Entry 1: `ACCT-CURR-BAL`, `1005.00`
- Entry 2: `ACCT-CURR-BAL`, `1525.00`
- Entry 3: `-1025.00`, `-2500.00`
- Entries 4–5: zeros (from INITIALIZE)

**Decision**: These values are preserved as named constants (`ARR_DEBIT_1`, `ARR_DEBIT_2`, `ARR_BAL_3`, `ARR_DEBIT_3`) in the processor class.

### 11. DISPLAY Statements → SLF4J Logging

All COBOL `DISPLAY` statements are mapped to `log.info()` calls using SLF4J with Logback. Debug-level logging is used for VBRC record display (matching the diagnostic nature of those displays in COBOL).

### 12. Duplicate Logic Reduction

The COBOL program contains repeated patterns for file open/close with status checking and error handling. In Java, these are consolidated into reusable helper methods (`openOutputFile()`, `writeRecord()`) that accept the file path and name as parameters, eliminating the duplicated open/close/status-check paragraphs.

---

## Project Structure

```
java-migration/
├── pom.xml                          Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md               This document
└── src/
    ├── main/java/com/carddemo/batch/
    │   ├── Cbact01cApplication.java  CLI entry point
    │   ├── Cbact01cProcessor.java    Core business logic
    │   ├── exception/
    │   │   └── AbendException.java   COBOL abend equivalent
    │   ├── io/
    │   │   ├── AccountFileReader.java Fixed-width record parser
    │   │   ├── CobolDecimalParser.java Overpunch decoder
    │   │   └── DateConverter.java     COBDATFT replacement
    │   └── model/
    │       ├── AccountRecord.java     CVACT01Y copybook
    │       ├── OutAccountRecord.java  OUT-FILE record
    │       ├── ArrayRecord.java       ARRY-FILE record
    │       ├── VbrcRecord1.java       VBRC short record
    │       └── VbrcRecord2.java       VBRC long record
    └── test/
        ├── java/com/carddemo/batch/
        │   ├── Cbact01cProcessorTest.java  Integration tests (40 tests)
        │   └── io/
        │       ├── AccountFileReaderTest.java
        │       ├── CobolDecimalParserTest.java
        │       └── DateConverterTest.java
        └── resources/
            └── sample-acctdata.txt    3-record test fixture

```

---

## Test Coverage

| Test Class                 | Tests | Coverage Focus                                    |
|----------------------------|-------|---------------------------------------------------|
| CobolDecimalParserTest     | 17    | Overpunch decoding: positive, negative, edge cases |
| DateConverterTest          | 7     | All 4 type-code combinations, blank input, errors  |
| AccountFileReaderTest      | 3     | Fixed-width parsing of real data records            |
| Cbact01cProcessorTest      | 13    | End-to-end: all 3 output files, 50-record dataset, business rules |
| **Total**                  | **40**|                                                    |

Key verification: The `processFullDatasetFromResources` test processes all 50 records from the original `app/data/ASCII/acctdata.txt` and verifies output counts, the zero-debit substitution rule, and array initialization across the entire dataset.

---

## How to Build and Run

```bash
cd java-migration
mvn clean package

# Run with 4 file arguments (matching COBOL DD names):
java -jar target/cbact01c-java-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    /tmp/outfile.txt \
    /tmp/arryfile.txt \
    /tmp/vbrcfile.txt
```

---

## Future Considerations

1. **Database target**: Replace flat-file output with JPA/JDBC writes to a relational database.
2. **Spring Batch**: Wrap the processor as a Spring Batch step for production job scheduling, retry, and monitoring.
3. **Input format evolution**: Support CSV or database-sourced input in addition to fixed-width COBOL exports.
4. **Binary compatibility**: If downstream systems require COMP-3 packed-decimal output, add a binary writer alongside the text writer.
