# CBACT01C Migration Notes — COBOL to Java 17+

## Overview

This document records the translation decisions made when migrating the COBOL batch program **CBACT01C.cbl** from the CardDemo mainframe application to a modern Java 17+ implementation.

| Aspect | COBOL | Java |
|---|---|---|
| Language level | COBOL 85 / Enterprise COBOL | Java 17 (LTS) — records, sealed types, text blocks |
| Build tool | JCL / mainframe compile | Apache Maven 3 |
| Test framework | (none) | JUnit 5 |
| File I/O | VSAM KSDS indexed + sequential FDs | `java.nio.file` buffered readers/writers |
| Numeric model | Zoned decimal (DISPLAY) / packed decimal (COMP-3) | `java.math.BigDecimal` |

---

## 1. Data-Structure Mapping

### 1.1 CVACT01Y — ACCOUNT-RECORD (300 bytes)

| COBOL Field | PIC | Java Field | Java Type | Notes |
|---|---|---|---|---|
| `ACCT-ID` | `9(11)` | `acctId` | `long` | Numeric ID, fits in `long` |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `activeStatus` | `String` | Single character |
| `ACCT-CURR-BAL` | `S9(10)V99` | `currBal` | `BigDecimal` | Zoned-decimal parsed via `ZonedDecimalParser` |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `creditLimit` | `BigDecimal` | |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `cashCreditLimit` | `BigDecimal` | |
| `ACCT-OPEN-DATE` | `X(10)` | `openDate` | `String` | ISO format `YYYY-MM-DD` |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `expirationDate` | `String` | Original misspelling preserved in display |
| `ACCT-REISSUE-DATE` | `X(10)` | `reissueDate` | `String` | Input format; reformatted for output |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `currCycCredit` | `BigDecimal` | |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `currCycDebit` | `BigDecimal` | Business rule: replaced with 2525.00 when zero |
| `ACCT-ADDR-ZIP` | `X(10)` | `addrZip` | `String` | Not used in output; kept for completeness |
| `ACCT-GROUP-ID` | `X(10)` | `groupId` | `String` | |
| `FILLER` | `X(178)` | *(omitted)* | — | Padding bytes, not meaningful |

**Decision**: Java `record` types are used for all data structures. Records are immutable, concise, and auto-generate `equals`/`hashCode`/`toString` — a natural fit for COBOL copybook structures which define pure data layouts.

### 1.2 CODATECN — Date Conversion Record

The COBOL program calls the assembler routine `COBDATFT` to reformat dates. The input/output type codes are:

| Type Code | Format |
|---|---|
| `1` | `YYYYMMDD` |
| `2` | `YYYY-MM-DD` |

CBACT01C always calls with `CODATECN-TYPE = '2'` and `CODATECN-OUTTYPE = '2'`, meaning the conversion is **YYYY-MM-DD → YYYYMMDD** (strip hyphens).

**Decision**: The assembler call is replaced by `DateFormatter.convert()`, a pure-Java utility that handles all four type-code combinations. No external date library is needed for this simple transformation.

---

## 2. I/O Translations

### 2.1 Input: ACCTFILE (VSAM KSDS)

| COBOL | Java |
|---|---|
| `SELECT ACCTFILE-FILE … ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL` | `BufferedReader` reading lines from a flat text file |
| `READ ACCTFILE-FILE INTO ACCOUNT-RECORD` | `reader.readLine()` + `parseAccountRecord()` |
| File status `'00'` = OK, `'10'` = EOF | `readLine() == null` for EOF; `IOException` for errors |

**Decision**: The VSAM indexed file is represented as a newline-delimited flat file (the format already used in `app/data/ASCII/acctdata.txt`). Sequential access in COBOL maps naturally to line-by-line reading. No indexing capability is needed because CBACT01C only performs sequential reads.

### 2.2 Output: OUTFILE (Sequential)

| COBOL | Java |
|---|---|
| Fixed-width binary record with COMP-3 field | Pipe-delimited text (`\|` separator) |
| `OUT-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 USAGE IS COMP-3` | `BigDecimal.toPlainString()` |

**Decision**: Output uses pipe-delimited text rather than COBOL fixed-width binary. COMP-3 (packed decimal) encoding is a mainframe-specific binary format with no modern Java equivalent. The pipe-delimited format preserves all field values with exact precision while being human-readable and easily consumed by downstream Java/SQL systems.

### 2.3 Output: ARRYFILE (Array Records)

| COBOL | Java |
|---|---|
| `ARR-ACCT-BAL OCCURS 5 TIMES` with nested COMP-3 | Account ID + 5 × (balance, debit) pipe-delimited pairs |

**Decision**: The COBOL `OCCURS` array is mapped to a `List<BalanceEntry>` of exactly 5 elements. Only entries 1–3 are populated with data (matching the COBOL `1400-POPUL-ARRAY-RECORD` paragraph); entries 4–5 contain zeros (matching COBOL `INITIALIZE`).

### 2.4 Output: VBRCFILE (Variable-Length Records)

| COBOL | Java |
|---|---|
| `RECORDING MODE IS V` with `RECORD IS VARYING … FROM 10 TO 80` | Two separate line types: `VB1\|…` (12-byte equivalent) and `VB2\|…` (39-byte equivalent) |
| `MOVE 12 TO WS-RECD-LEN` / `MOVE 39 TO WS-RECD-LEN` | Line prefix `VB1` or `VB2` distinguishes record types |

**Decision**: Variable-length records are replaced with tagged text lines. The `VB1`/`VB2` prefix serves the same purpose as the COBOL record-length indicator in `RECORDING MODE V`.

---

## 3. Business Logic Translation

### 3.1 Zero-Debit Substitution

```cobol
IF  ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```

→ Java:

```java
BigDecimal cycDebit = acct.currCycDebit().signum() == 0
        ? DEFAULT_CYC_DEBIT   // 2525.00
        : acct.currCycDebit();
```

The constant `2525.00` is preserved exactly. All 50 sample records have zero debit, so all output records show 2525.00.

### 3.2 Array Population (Hard-Coded Test Values)

The COBOL program populates array slots with hard-coded constants:

| Slot | Balance | Debit |
|---|---|---|
| 1 | Actual `ACCT-CURR-BAL` | 1005.00 |
| 2 | Actual `ACCT-CURR-BAL` | 1525.00 |
| 3 | −1025.00 | −2500.00 |
| 4–5 | 0 (INITIALIZE) | 0 (INITIALIZE) |

These appear to be test/demo values in the original program. They are preserved exactly in the Java migration.

### 3.3 Reissue-Year Extraction for VB2 Record

```cobol
MOVE   WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```

The COBOL working-storage `WS-ACCT-REISSUE-DATE` redefines the 10-char reissue date, and `WS-ACCT-REISSUE-YYYY` is the first 4 characters (the year). In Java this is:

```java
String reissueYear = acct.reissueDate().substring(0, 4);
```

---

## 4. Error Handling

| COBOL | Java |
|---|---|
| `PERFORM 9999-ABEND-PROGRAM` → `CALL 'CEE3ABD'` with abend code 999 | `IOException` propagation; `main()` calls `System.exit(999)` |
| `9910-DISPLAY-IO-STATUS` — binary status code formatting | Standard Java exception messages (stack traces provide equivalent detail) |
| File-status checks after every I/O operation | Try-with-resources ensures files are closed; `IOException` on any failure |

**Decision**: The COBOL abend mechanism is replaced with Java exception handling. The exit code 999 is preserved for compatibility with JCL return-code checking if the batch is orchestrated by a scheduler.

---

## 5. Numeric Precision

### Zoned Decimal Parsing

COBOL `DISPLAY` (zoned decimal) fields encode the sign in the trailing byte:

| Positive | Negative |
|---|---|
| `{` = 0, `A` = 1, … `I` = 9 | `}` = 0, `J` = 1, … `R` = 9 |

The `ZonedDecimalParser` utility handles this encoding. All numeric values use `BigDecimal` to preserve the exact two-decimal-place precision of `PIC S9(10)V99`.

### COMP-3 (Packed Decimal)

COBOL COMP-3 stores two digits per byte with a trailing sign nibble. The original `OUT-ACCT-CURR-CYC-DEBIT` and `ARR-ACCT-CURR-CYC-DEBIT` use COMP-3 in the output files. Since the Java output uses text format, COMP-3 encoding is not needed — values are written as plain decimal strings with no precision loss.

---

## 6. What Is Not Migrated

| COBOL Feature | Reason |
|---|---|
| VSAM KSDS indexed access | CBACT01C only reads sequentially; flat file suffices |
| BMS screen maps | CBACT01C is a batch program with no terminal I/O |
| CICS transaction support | Batch-only program |
| JCL job control (`ACCTFILE DD`) | Replaced by command-line arguments |
| `CEE3ABD` abend routine | Replaced by `System.exit(999)` |
| `COBDATFT` assembler program | Replaced by `DateFormatter.java` |

---

## 7. Testing Strategy

- **35 JUnit 5 tests** covering:
  - Zoned-decimal parsing (positive, negative, zero, edge cases)
  - Date formatting (all 4 type-code combinations)
  - Record parsing from fixed-width lines
  - Output record building (zero-debit rule, date reformatting)
  - Array record population (5-slot verification)
  - Variable-length record building (year extraction)
  - Full batch execution against all 50 sample records
  - Output file content verification (field values, line counts)
  - Edge cases (empty file, single record)

- Sample data (`acctdata.txt`) is bundled in `src/test/resources/` so tests are self-contained.

---

## 8. Project Structure

```
java-migration/
├── pom.xml                                     Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md                          This document
├── src/main/java/com/carddemo/batch/
│   ├── Cbact01cBatch.java                      Main batch processor
│   ├── model/
│   │   ├── AccountRecord.java                  CVACT01Y copybook → Java record
│   │   ├── OutputAccountRecord.java            OUT-FILE FD → Java record
│   │   ├── ArrayAccountRecord.java             ARRY-FILE FD → Java record
│   │   ├── VbRecord1.java                      VBRC-REC1 → Java record
│   │   └── VbRecord2.java                      VBRC-REC2 → Java record
│   └── util/
│       ├── DateFormatter.java                  COBDATFT replacement
│       └── ZonedDecimalParser.java             Zoned-decimal → BigDecimal
└── src/test/
    ├── java/com/carddemo/batch/
    │   ├── Cbact01cBatchTest.java              Batch + integration tests
    │   └── util/
    │       ├── DateFormatterTest.java
    │       └── ZonedDecimalParserTest.java
    └── resources/
        └── acctdata.txt                        50-record sample data
```

---

## 9. Running the Application

```bash
# Build
cd java-migration
mvn clean package

# Execute
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    output/outfile.txt \
    output/arryfile.txt \
    output/vbrcfile.txt

# Run tests
mvn test
```
