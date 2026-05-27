# CBACT01C Migration Notes — COBOL to Java 17+

## Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| Program | `CBACT01C.CBL` | `AccountBatchProcessor.java` |
| Type | Batch COBOL (CICS-less) | Standalone CLI application |
| Language standard | COBOL 85 / Enterprise COBOL | Java 17 (LTS) |
| Build tool | JCL / compile & link | Maven 3 |
| Test framework | — | JUnit 5 |

## Business Logic Summary

CBACT01C reads a VSAM KSDS account file sequentially and produces three output files:

1. **OUT-FILE** — Flat fixed-width account summary with date reformatting and a
   zero-debit defaulting rule.
2. **ARRY-FILE** — Array-structured balance records (OCCURS 5 TIMES), three
   slots populated per account plus two zero-filled slots.
3. **VBRC-FILE** — Two variable-length records per account: a short status
   record (12 bytes) and a longer balance record (39 bytes).

All business rules are preserved identically in the Java version.

---

## Translation Decisions

### 1. Data Structures → Java Records

| COBOL | Java |
|-------|------|
| `ACCOUNT-RECORD` (copybook `CVACT01Y`) | `AccountRecord` record |
| `OUT-ACCT-REC` (FD) | `OutAccountRecord` record |
| `ARR-ARRAY-REC` (FD) | `ArrayAccountRecord` record |
| `ARR-ACCT-BAL` (OCCURS) | `BalanceEntry` record |
| `VBRC-REC1` / `VBRC-REC2` | `VarLengthRecord` sealed interface with `StatusRecord` and `BalanceRecord` permits |

Java `record` types were chosen because:
- They are immutable value objects — matching the read-once/write-once nature of
  each COBOL record movement.
- They provide `equals()`, `hashCode()`, and `toString()` for free, simplifying
  test assertions.
- The `sealed interface` for variable-length records models the two-variant
  discriminated union with compile-time exhaustiveness checking.

### 2. Numeric Representation

| COBOL | Java |
|-------|------|
| `PIC S9(10)V99` (DISPLAY, sign overpunch) | `BigDecimal` |
| `PIC S9(10)V99 USAGE IS COMP-3` | `BigDecimal` |
| `PIC 9(11)` | `long` |

- **`BigDecimal`** is used for all monetary fields to avoid floating-point
  rounding errors, matching COBOL's fixed-point arithmetic exactly.
- The overpunch sign encoding (`{`, `A-I`, `}`, `J-R`) is handled by
  `CobolDataParser.parseSignedDecimal()` and `formatSignedDecimal()` for
  round-trip fidelity with the original data files.
- **COMP-3 (packed decimal)** fields in the COBOL FD records are stored as
  binary packed BCD on the mainframe. In the Java version all output is written
  in human-readable display (overpunch) format. This is a deliberate
  modernization choice — the original binary format is losslessly representable
  as display text and easier to inspect/debug.

### 3. Date Conversion — COBDATFT Replacement

The COBOL program calls the assembler routine `COBDATFT` via the `CODATECN`
copybook to convert dates between `YYYY-MM-DD` and `YYYYMMDD` formats.

The Java replacement is `DateConverter`, a pure-Java utility that:
- Accepts the same input/output format codes (type 1 = `YYYYMMDD`,
  type 2 = `YYYY-MM-DD`).
- Uses simple `String.substring()` operations — no `java.time` parsing overhead
  needed since the conversion is purely structural reformatting.
- Is fully unit-tested for round-trip correctness.

### 4. File I/O

| COBOL | Java |
|-------|------|
| VSAM KSDS (indexed, sequential access) | `BufferedReader` line-by-line |
| Sequential output (FB records) | `BufferedWriter` with newlines |
| Variable-length (RECORDING MODE V) | Two text lines per account |

- The VSAM indexed file is represented as a flat text file (one 300-character
  record per line), matching the ASCII export format already present in the
  repository at `app/data/ASCII/acctdata.txt`.
- COBOL's `FILE STATUS` checking is replaced by Java's `IOException` mechanism.
  Any I/O error causes an exception to propagate, equivalent to the COBOL
  `9999-ABEND-PROGRAM` paragraph.
- Variable-length records (RECORDING MODE V with `RECORD IS VARYING`) are
  modeled as separate text lines. The original COBOL uses different
  `WS-RECD-LEN` values (12 and 39) to control record size; in Java each
  record type has a natural length based on its formatted string.

### 5. Error Handling

| COBOL | Java |
|-------|------|
| `PERFORM 9999-ABEND-PROGRAM` → `CALL 'CEE3ABD'` | `IOException` propagation (uncaught = process exit) |
| `9910-DISPLAY-IO-STATUS` (extended status decode) | Stack trace + exception message |

The COBOL program abends with code 999 via the Language Environment `CEE3ABD`
service on any file I/O error. The Java equivalent is to let `IOException`
propagate to the caller; if running from `main()`, this terminates the JVM with
a non-zero exit code and a stack trace.

### 6. DISPLAY Statements → Logging

All COBOL `DISPLAY` statements are reproduced via `System.out.println()` and
captured in an internal `displayLog` list. The log can be inspected in tests to
verify that the Java program produces the same console output as the COBOL
program would on the mainframe SYSOUT.

### 7. Specific Business Rules Preserved

1. **Zero-debit defaulting** (paragraph `1300-POPUL-ACCT-RECORD`):
   If `ACCT-CURR-CYC-DEBIT` equals zero, the output `OUT-ACCT-CURR-CYC-DEBIT`
   is set to `2525.00`. This rule is implemented identically in
   `AccountBatchProcessor.buildOutRecord()`.

2. **Array slot population** (paragraph `1400-POPUL-ARRAY-RECORD`):
   - Slot 1: `currBal` = account balance, `currCycDebit` = 1005.00
   - Slot 2: `currBal` = account balance, `currCycDebit` = 1525.00
   - Slot 3: `currBal` = -1025.00, `currCycDebit` = -2500.00
   - Slots 4-5: zeroed (COBOL `INITIALIZE` equivalent)

3. **Variable-length record content** (paragraph `1500-POPUL-VBRC-RECORD`):
   - VB1 (12 bytes): account ID + active status
   - VB2 (39 bytes): account ID + current balance + credit limit + reissue year
     (4-char substring of the YYYY-MM-DD reissue date)

---

## Project Structure

```
java-migration/
├── pom.xml                          # Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md               # This file
├── src/main/java/com/cardemo/batch/
│   ├── AccountBatchProcessor.java   # Main batch logic (CBACT01C equivalent)
│   ├── model/
│   │   ├── AccountRecord.java       # CVACT01Y copybook
│   │   ├── OutAccountRecord.java    # OUT-FILE record
│   │   ├── ArrayAccountRecord.java  # ARRY-FILE record
│   │   ├── BalanceEntry.java        # OCCURS entry
│   │   └── VarLengthRecord.java     # Sealed interface for VB1/VB2
│   ├── io/
│   │   └── CobolDataParser.java     # Overpunch encoding, fixed-width parsing
│   └── util/
│       └── DateConverter.java       # COBDATFT replacement
└── src/test/
    ├── java/com/cardemo/batch/
    │   ├── AccountBatchProcessorTest.java  # Integration tests (10 tests)
    │   ├── io/CobolDataParserTest.java     # Parser unit tests (19 tests)
    │   └── util/DateConverterTest.java     # Date conversion tests (6 tests)
    └── resources/
        └── sample-acctdata.txt      # 5-record subset of real account data
```

## Running

```bash
# Build
cd java-migration
mvn clean package

# Run against the real account data
java -jar target/cbact01c-java-migration-1.0.0.jar \
  ../app/data/ASCII/acctdata.txt \
  /tmp/outfile.txt \
  /tmp/arryfile.txt \
  /tmp/vbrfile.txt

# Run tests
mvn test
```

## Test Coverage

35 tests covering:
- **CobolDataParser**: Sign-overpunch parsing/formatting, round-trip fidelity,
  full account record parsing, error handling.
- **DateConverter**: Both conversion directions, round-trip verification.
- **AccountBatchProcessor**: Single/multiple record processing, zero-debit
  defaulting, non-zero debit preservation, array file slot population,
  variable-length record generation, display log verification, real sample
  data end-to-end, empty input handling.

## Known Differences from Mainframe Behavior

1. **COMP-3 output encoding**: The COBOL `OUT-ACCT-CURR-CYC-DEBIT` field uses
   `USAGE IS COMP-3` (packed decimal). The Java version writes this field in
   display (overpunch) format instead of binary packed BCD. This is intentional
   for portability and readability.

2. **File organization**: VSAM KSDS indexing is not emulated. The Java version
   reads a flat text file sequentially, which is functionally equivalent since
   CBACT01C only uses sequential access mode.

3. **ABEND behavior**: The COBOL `CEE3ABD` call produces a Language Environment
   abend with formatted diagnostics. The Java version throws an exception with
   a stack trace, which serves the same diagnostic purpose in the Java
   ecosystem.
