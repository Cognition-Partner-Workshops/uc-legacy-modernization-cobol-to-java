# CBACT01C Migration Notes: COBOL → Java 17+

## 1. Program Overview

| Attribute       | COBOL (Original)                | Java (Modernized)                          |
|-----------------|----------------------------------|--------------------------------------------|
| **Program**     | `CBACT01C.CBL`                   | `AccountBatchProcessor.java`               |
| **Type**        | Batch COBOL (JCL-invoked)        | Standalone Java CLI application             |
| **Function**    | Read VSAM account file, write 3 output files | Same logical function with flat-file I/O  |
| **Runtime**     | z/OS CICS batch region           | JVM 17+                                    |

## 2. Architecture Decisions

### 2.1 Project Structure
The Java project uses Maven with a standard `src/main/java` / `src/test/java` layout. All classes reside under `com.cardemo.batch` with sub-packages for `model`, `io`, and `util`.

### 2.2 Java Records for Data Structures
COBOL copybooks and FD record layouts are mapped to **Java `record` types** (immutable value objects):

| COBOL Structure     | Java Record                | Notes                                    |
|---------------------|----------------------------|------------------------------------------|
| `CVACT01Y` (ACCOUNT-RECORD) | `AccountRecord`     | 300-byte input record with zoned-decimal parsing |
| `OUT-ACCT-REC`      | `OutputAccountRecord`      | Flat output with reformatted dates        |
| `ARR-ARRAY-REC`     | `ArrayRecord`              | Contains `List<BalanceEntry>` (OCCURS 5)  |
| `VBRC-REC1`         | `VbRecord1`                | Short VB record (ID + status)             |
| `VBRC-REC2`         | `VbRecord2`                | Long VB record (ID + financials + year)   |

### 2.3 Output Format: CSV Instead of Fixed-Width Binary
The COBOL program writes fixed-width records including **COMP-3 (packed decimal)** fields. The Java version writes **CSV with headers**, which is:
- Human-readable and debuggable
- Portable across platforms (no EBCDIC/packed-decimal concerns)
- Compatible with modern data pipelines, databases, and spreadsheets

Numeric values use `BigDecimal.toPlainString()` for lossless decimal representation.

## 3. Translation Decisions

### 3.1 Zoned Decimal Parsing (PIC S9(n)V99)
COBOL stores signed numerics in **zoned decimal** format with the sign "overpunched" on the last character:

| Last Char | Digit | Sign     |
|-----------|-------|----------|
| `{`       | 0     | Positive |
| `A`–`I`   | 1–9   | Positive |
| `}`       | 0     | Negative |
| `J`–`R`   | 1–9   | Negative |

The `AccountRecord.parseSignedDecimal()` method replicates this decoding and applies `movePointLeft(2)` for the implied `V99` decimal position.

### 3.2 COBDATFT Assembler Replacement → `DateConverter`
The COBOL program calls assembler routine `COBDATFT` via `CALL 'COBDATFT' USING CODATECN-REC` to convert dates. The `CODATECN` copybook defines:
- **Input type "1"** = `YYYYMMDD`, **"2"** = `YYYY-MM-DD`
- **Output type "1"** = `YYYY-MM-DD`, **"2"** = `YYYYMMDD`

CBACT01C always calls with `TYPE=2, OUTTYPE=2` (i.e., `YYYY-MM-DD` → `YYYYMMDD`).

The Java `DateConverter` uses `java.time.LocalDate` with `DateTimeFormatter` to achieve the same conversion, supporting all four type combinations for future use.

### 3.3 Default Debit Business Rule
```cobol
IF  ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF.
```
Preserved as:
```java
BigDecimal cycDebit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
        ? DEFAULT_CYC_DEBIT   // 2525.00
        : acct.currCycDebit();
```

### 3.4 Array Record Population (OCCURS 5 TIMES)
The COBOL `1400-POPUL-ARRAY-RECORD` paragraph fills 5 balance slots with a mix of actual data and hardcoded test values:

| Slot | `CURR-BAL`           | `CURR-CYC-DEBIT`    |
|------|----------------------|----------------------|
| 1    | Actual account bal   | 1005.00 (hardcoded)  |
| 2    | Actual account bal   | 1525.00 (hardcoded)  |
| 3    | -1025.00 (hardcoded) | -2500.00 (hardcoded) |
| 4    | 0.00 (INITIALIZE)    | 0.00 (INITIALIZE)    |
| 5    | 0.00 (INITIALIZE)    | 0.00 (INITIALIZE)    |

This pattern is faithfully reproduced in `populateArrayRecord()`.

### 3.5 Variable-Length (VB) Records
COBOL writes two variable-length records per account to `VBRC-FILE`:
- **VB1** (12 bytes): Account ID + active status
- **VB2** (39 bytes): Account ID + balance + credit limit + reissue year

In Java, both are written as tagged CSV rows (`VB1,…` / `VB2,…`) to a single output file, preserving the one-to-many relationship.

### 3.6 Reissue Year Extraction
```cobol
MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```
The COBOL code copies the first 4 characters of the reissue date (already in `YYYY-MM-DD` format from the working-storage copy). The Java equivalent uses `substring(0, 4)`.

### 3.7 VSAM KSDS → Flat File
The COBOL program reads from an **indexed VSAM KSDS** (`ORGANIZATION IS INDEXED, ACCESS MODE IS SEQUENTIAL`). The Java version reads from a **flat text file** with one record per line (the standard ASCII export format used by `acctdata.txt`).

### 3.8 Error Handling
| COBOL Pattern                | Java Equivalent                        |
|------------------------------|----------------------------------------|
| `FILE STATUS` checks         | `IOException` from `java.nio.file`     |
| `9999-ABEND-PROGRAM` (CEE3ABD, code 999) | `System.exit(999)` in main, `IOException` propagation in processor |
| `9910-DISPLAY-IO-STATUS`     | Exception message includes file status  |

### 3.9 COMP-3 Fields
Two fields in the COBOL output use `USAGE IS COMP-3` (packed decimal):
- `OUT-ACCT-CURR-CYC-DEBIT`
- `ARR-ACCT-CURR-CYC-DEBIT`

In the Java CSV output, these are written as plain decimal strings. The packed-decimal storage format is a mainframe optimization that has no equivalent need in modern Java.

## 4. What Is NOT Migrated

| COBOL Feature               | Reason for Omission                        |
|-----------------------------|--------------------------------------------|
| JCL job control             | Replaced by CLI arguments / shell scripts  |
| DD name resolution          | File paths passed as command-line args      |
| EBCDIC encoding             | Java uses UTF-8 natively                   |
| VSAM catalog integration    | Not applicable to file-based I/O           |
| CEE3ABD abend               | Replaced by JVM exit codes and exceptions  |
| MVSWAIT (timer control)     | Not used by CBACT01C                       |

## 5. Testing Strategy

### 5.1 Test Classes
| Test Class                    | Scope                                     | Tests |
|-------------------------------|-------------------------------------------|-------|
| `AccountRecordTest`           | Zoned-decimal parsing, record construction | 13    |
| `DateConverterTest`           | All COBDATFT conversion combinations       | 9     |
| `AccountBatchProcessorTest`   | End-to-end processing, business rules      | 16    |

### 5.2 Equivalence Verification
Tests verify that:
1. **Zoned decimal** values parse identically to COBOL display format
2. **Date conversion** matches COBDATFT output for all type combinations
3. **Default debit** (2525.00) is applied when cycle debit is zero
4. **Array slot population** matches the hardcoded COBOL pattern exactly
5. **VB record splitting** produces the correct number and type of records
6. **Field-by-field output comparison** against expected COBOL results

### 5.3 Sample Data
`src/test/resources/sample_acctdata.txt` contains 3 records extracted from the production-format `acctdata.txt` for integration testing.

## 6. Running the Application

```bash
# Build
cd java-modernized
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package

# Run
java -jar target/cbact01c-modernized-1.0.0.jar \
  ../app/data/ASCII/acctdata.txt \
  output/outfile.csv \
  output/arryfile.csv \
  output/vbrcfile.csv

# Run tests
mvn test
```

## 7. File Mapping

| COBOL DD Name  | Java CLI Arg | Description                     |
|----------------|-------------|---------------------------------|
| `ACCTFILE`     | arg[0]       | Input account file (VSAM → flat)|
| `OUTFILE`      | arg[1]       | Output account CSV              |
| `ARRYFILE`     | arg[2]       | Array records CSV               |
| `VBRCFILE`     | arg[3]       | Variable-length records CSV     |
