# CBACT01C Migration Notes — COBOL to Java 17+

## 1. Program Overview

| Attribute | COBOL (Original) | Java (Migrated) |
|-----------|-------------------|-----------------|
| Program ID | `CBACT01C.cbl` | `Cbact01cBatchJob.java` |
| Type | Batch COBOL | CLI Java application |
| Function | Read accounts from indexed VSAM file, transform, and write to three output files | Same logical function using flat-file I/O |
| Runtime | z/OS CICS / Batch JCL | JVM 17+ (any OS) |

## 2. Architecture Decisions

### 2.1 Project Structure

The Java port uses a standard Maven project layout under `java-migration/`:

```
java-migration/
├── pom.xml
├── MIGRATION_NOTES.md
└── src/
    ├── main/java/com/carddemo/batch/
    │   ├── Cbact01cBatchJob.java      ← Main batch logic (PROCEDURE DIVISION)
    │   ├── BatchResult.java           ← Return code semantics
    │   ├── model/
    │   │   ├── AccountRecord.java     ← CVACT01Y copybook
    │   │   ├── OutputAccountRecord.java ← OUT-ACCT-REC FD
    │   │   ├── ArrayRecord.java       ← ARR-ARRAY-REC FD
    │   │   ├── VbRecord1.java         ← VBRC-REC1 (short VB record)
    │   │   └── VbRecord2.java         ← VBRC-REC2 (long VB record)
    │   └── util/
    │       └── DateConverter.java     ← Replaces COBDATFT assembler
    └── test/java/com/carddemo/batch/
        ├── Cbact01cBatchJobTest.java
        ├── model/AccountRecordTest.java
        └── util/DateConverterTest.java
```

### 2.2 Why Java Records

Java 17 `record` types replace COBOL copybook structures and FD record layouts. Records are immutable value objects with auto-generated `equals()`, `hashCode()`, and `toString()` — a natural fit for fixed-layout data records that are built once and written.

### 2.3 Why Maven (not Gradle)

Maven was chosen for its simpler XML configuration and wide CI/CD compatibility. No Spring Boot or heavyweight framework is required — the program is a straightforward batch job.

## 3. Data-Structure Mapping

### 3.1 CVACT01Y Copybook → `AccountRecord`

| COBOL Field | PIC | Java Field | Java Type | Notes |
|-------------|-----|------------|-----------|-------|
| `ACCT-ID` | `9(11)` | `acctId` | `long` | Numeric ID, zero-padded in serialisation |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `activeStatus` | `String` | Single character |
| `ACCT-CURR-BAL` | `S9(10)V99` | `currBal` | `BigDecimal` | Signed decimal, 2 fractional digits |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `creditLimit` | `BigDecimal` | |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `cashCreditLimit` | `BigDecimal` | |
| `ACCT-OPEN-DATE` | `X(10)` | `openDate` | `String` | `YYYY-MM-DD` format |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `expirationDate` | `String` | Retains original COBOL misspelling in display labels |
| `ACCT-REISSUE-DATE` | `X(10)` | `reissueDate` | `String` | `YYYY-MM-DD` format |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `currCycCredit` | `BigDecimal` | |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `currCycDebit` | `BigDecimal` | |
| `ACCT-ADDR-ZIP` | `X(10)` | `addrZip` | `String` | Present in copybook, not used in output |
| `ACCT-GROUP-ID` | `X(10)` | `groupId` | `String` | |
| `FILLER` | `X(178)` | — | — | Padding; not represented in Java |

### 3.2 CODATECN Copybook → `DateConverter`

The COBOL program calls the `COBDATFT` assembler subroutine, passing the `CODATECN-REC` structure. This structure specifies input/output format types:

| Type Value | Format | Example |
|------------|--------|---------|
| `1` | `YYYYMMDD` | `20250520` |
| `2` | `YYYY-MM-DD` | `2025-05-20` |

In the Java port, `DateConverter.convert(inputDate, inType, outType)` uses `java.time.LocalDate` and `DateTimeFormatter` to replicate the same conversions. The assembler call is replaced by a pure-Java static method.

### 3.3 COMP-3 (Packed Decimal) Fields

The COBOL program uses `USAGE IS COMP-3` for two fields:
- `OUT-ACCT-CURR-CYC-DEBIT` in the output record
- `ARR-ACCT-CURR-CYC-DEBIT` in the array record

COMP-3 is a mainframe-specific packed-decimal encoding (two digits per byte + sign nibble). In the Java port, all numeric fields use `BigDecimal`, which provides arbitrary-precision decimal arithmetic without the binary-encoding concerns. The output files use plain-text decimal representation instead of packed binary.

## 4. I/O Translation

### 4.1 File Access Patterns

| COBOL | Java Equivalent | Notes |
|-------|-----------------|-------|
| VSAM KSDS indexed, sequential read | `BufferedReader.readLine()` | The indexed nature is not needed — records are read sequentially |
| Sequential `WRITE` to flat files | `BufferedWriter.write()` + `newLine()` | |
| Variable-length records (`RECORDING MODE IS V`) | Two separate delimited lines per account in VBRCFILE | Length prefix is replaced by line-based I/O |
| File-status checking (`ACCTFILE-STATUS`) | Java `IOException` handling | COBOL's `00`/`10`/other status codes map to success/EOF/exception |

### 4.2 Fixed-Width vs. Delimited Output

The original COBOL writes binary fixed-width records (including COMP-3 packed fields). The Java port writes **pipe-delimited text** for the three output files. This decision was made because:

1. Modern downstream systems (databases, ETL tools, analytics) consume delimited text more easily than fixed-width binary.
2. COMP-3 packed decimal has no meaning outside a mainframe runtime.
3. Delimited output is human-readable and testable.

If byte-for-byte COBOL-compatible output is ever required, the `toFixedWidth()` method on `AccountRecord` demonstrates the fixed-width serialisation pattern that could be extended to output records.

### 4.3 Input File Format

The input file uses the CVACT01Y 300-character fixed-width layout (ASCII). `AccountRecord.parse()` reads positionally:

```
Positions  0-10:  ACCT-ID (11 chars, numeric)
Position  11:     ACCT-ACTIVE-STATUS (1 char)
Positions 12-23:  ACCT-CURR-BAL (12 chars, signed decimal)
...etc
```

## 5. Business-Logic Translation

### 5.1 Default Cycle-Debit Rule (paragraph 1300)

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF
```

Java equivalent:
```java
BigDecimal cycDebit = acct.currCycDebit().signum() == 0
        ? DEFAULT_CYC_DEBIT   // 2525.00
        : acct.currCycDebit();
```

### 5.2 Array Record Population (paragraph 1400)

The COBOL program fills 3 of 5 array entries with a mix of real and hard-coded values:

| Index | Balance | Cycle Debit |
|-------|---------|-------------|
| 1 | `ACCT-CURR-BAL` | `1005.00` |
| 2 | `ACCT-CURR-BAL` | `1525.00` |
| 3 | `-1025.00` | `-2500.00` |
| 4 | `0` (INITIALIZE) | `0` |
| 5 | `0` (INITIALIZE) | `0` |

The Java code uses `List<BalanceEntry>` with explicit zero entries for indices 4-5.

### 5.3 Variable-Length Record Split (paragraphs 1550/1575)

Each account produces two records in the VB file:
- **VB1** (12 bytes): Account ID + active status
- **VB2** (39 bytes): Account ID + current balance + credit limit + reissue year

In COBOL, `WS-RECD-LEN` controls the variable record length. In Java, these are simply two separate lines with different field counts.

### 5.4 Date Reformatting (paragraph 1300 + COBDATFT)

The reissue date is converted from `YYYY-MM-DD` to `YYYYMMDD` using the date converter. The year is also extracted (first 4 characters) for the VB2 record via `WS-ACCT-REISSUE-YYYY`.

### 5.5 Reissue Year Extraction (paragraph 1500)

```cobol
MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY.
```

The COBOL program saves the reissue date to `WS-REISSUE-DATE` (a 10-char field redefined with `WS-ACCT-REISSUE-YYYY` covering positions 1-4). The Java equivalent is `acct.reissueDate().substring(0, 4)`.

## 6. Error Handling

| COBOL Mechanism | Java Equivalent |
|-----------------|-----------------|
| `FILE STATUS IS` + manual checking | `IOException` propagation |
| `9910-DISPLAY-IO-STATUS` (status code formatting) | Exception message in `BatchResult` |
| `9999-ABEND-PROGRAM` → `CEE3ABD` with ABCODE 999 | `BatchResult` with `RC_ABEND = 999` |
| `APPL-RESULT` / `88-level` conditions (`APPL-AOK`, `APPL-EOF`) | `BatchResult` return codes (`RC_OK`, `RC_EOF`, `RC_ERROR`) |

The COBOL program calls `CEE3ABD` (Language Environment abnormal termination) on unrecoverable errors. The Java port returns a `BatchResult` record with the appropriate return code, allowing the caller to decide how to handle failures.

## 7. What Was Not Migrated

| COBOL Feature | Reason for Omission |
|---------------|---------------------|
| EBCDIC data files | Java uses UTF-8; original EBCDIC data would need a one-time conversion |
| VSAM KSDS indexed access | Input is read sequentially; indexing is unnecessary for this batch pattern |
| COMP-3 binary output | Replaced with plain-text decimals (see §4.2) |
| JCL job control (`DEFCDFLT.jcl`) | Replaced by CLI arguments or external scheduler |
| `CEE3ABD` / `MVSWAIT` assembler calls | Replaced by Java return codes and standard JVM mechanisms |
| BMS screen maps | Not applicable — CBACT01C is a batch program with no terminal I/O |

## 8. Testing Strategy

37 JUnit 5 tests verify:

- **Field-level equivalence**: Each COBOL MOVE/transformation is tested independently
- **End-to-end batch runs**: Multi-record input produces correct record counts in all three output files
- **Round-trip serialisation**: `parse()` → `toDelimited()`/`toFixedWidth()` → re-parse preserves values
- **Default-value rule**: Zero cycle-debit replaced with 2525.00; non-zero preserved
- **Date conversion**: Parameterised tests covering all format combinations
- **Error paths**: Missing input file returns ABEND code; empty file returns RC_OK with zero records

## 9. How to Build and Run

```bash
# Build and test
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64   # or any JDK 17+
cd java-migration
mvn clean package

# Run the batch job
java -jar target/cbact01c-migration-1.0.0.jar \
    /path/to/ACCTFILE /path/to/OUTFILE /path/to/ARRYFILE /path/to/VBRCFILE
```

## 10. Future Considerations

- **Database-backed I/O**: Replace flat-file reads with JDBC queries against a relational database holding the migrated account data.
- **Spring Batch**: For production workloads, wrap `Cbact01cBatchJob` in a Spring Batch `Tasklet` to gain restart/retry, chunk processing, and monitoring.
- **Schema evolution**: The `AccountRecord` record can be extended with a `@JsonProperty` annotation layer for JSON/REST-based data exchange.
- **EBCDIC conversion**: If the original mainframe VSAM data must be consumed directly, a pre-processing step using `java.nio.charset.Charset` with an EBCDIC code page (e.g., `IBM1047`) can be added.
