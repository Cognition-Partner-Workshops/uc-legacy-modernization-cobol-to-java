# MIGRATION_NOTES.md — CBACT01C.cbl → Java 17+

## 1. Program Overview

**CBACT01C** is a COBOL batch program from the AWS CardDemo mainframe application.
It reads an indexed VSAM KSDS account master file sequentially and produces three
output files:

| Output          | COBOL DD   | Description |
|-----------------|------------|-------------|
| Flat extract    | `OUTFILE`  | Selected account fields with a reformatted reissue date |
| Array file      | `ARRYFILE` | 5-slot balance/debit array per account (3 populated) |
| Variable-length | `VBRCFILE` | Two records per account: a 12-byte status record and a 39-byte balance record |

---

## 2. Structural Mapping

| COBOL Construct | Java Equivalent | Notes |
|---|---|---|
| `IDENTIFICATION DIVISION` / `PROGRAM-ID` | `Cbact01cBatch` class | Entry point is `main()` |
| `WORKING-STORAGE SECTION` | Class fields / local variables | Constants extracted to `static final` fields |
| `COPY CVACT01Y` (copybook) | `AccountRecord` record | Java 17 `record` for immutable DTO |
| `COPY CODATECN` (copybook) | `DateFormatter` utility | Pure-Java replacement for COBDATFT |
| `FD OUT-FILE` record | `OutputAccountRecord` record | |
| `FD ARRY-FILE` record | `ArrayRecord` class | Mutable due to array slots |
| `VBRC-REC1` / `VBRC-REC2` | `VariableLengthRecord1` / `VariableLengthRecord2` records | |
| `PERFORM` paragraphs | Private methods | 1:1 mapping, names preserved in Javadoc |
| `EVALUATE` / `IF` | `switch` expressions / `if` | Java 17 pattern matching where applicable |
| `GOBACK` | `return` / `System.exit()` | |
| `CALL 'CEE3ABD'` (abend) | `System.exit(999)` + `IOException` | |

---

## 3. Data Type Translations

### 3.1 Numeric Fields

| COBOL PIC | Java Type | Rationale |
|---|---|---|
| `PIC 9(11)` (unsigned integer) | `long` | Max value 99,999,999,999 fits in `long` |
| `PIC S9(10)V99` (signed decimal, DISPLAY) | `BigDecimal` | Exact decimal arithmetic; no floating-point drift |
| `PIC S9(10)V99 COMP-3` (packed decimal) | `BigDecimal` | Same Java type; COMP-3 is a storage optimization irrelevant in Java |
| `PIC X(n)` (alphanumeric) | `String` | |
| `PIC 9(4) BINARY` | `int` | Only used for I/O status conversion |

### 3.2 Signed Display (Overpunch) Encoding

COBOL DISPLAY-format signed fields use **trailing overpunch** where the sign is
encoded in the last byte:

```
Positive: { = +0,  A = +1,  B = +2, ... I = +9
Negative: } = -0,  J = -1,  K = -2, ... R = -9
```

The `CobolNumericParser` utility class handles parsing and formatting of these
fields, enabling round-trip fidelity with the original COBOL data files.

### 3.3 Implied Decimal (`V`)

COBOL `PIC S9(10)V99` stores 12 digits with an implied decimal between the 10th
and 11th digits. No decimal point character appears in the file. The parser uses
`BigDecimal.movePointLeft(2)` to restore the decimal position.

---

## 4. I/O Translation Decisions

### 4.1 VSAM KSDS → Flat File Reader

The original program opens a VSAM KSDS file with `ACCESS MODE IS SEQUENTIAL`.
Since Java has no native VSAM support, the migration reads the ASCII flat-file
export (`acctdata.txt`) line-by-line. Each line is a fixed 300-byte record
matching the CVACT01Y copybook layout.

### 4.2 File Status Codes

COBOL file-status checks (`'00'` = OK, `'10'` = EOF) are replaced by:
- `Optional.empty()` for EOF
- `IOException` for errors
- The `9910-DISPLAY-IO-STATUS` diagnostic paragraph is replaced by exception
  messages.

### 4.3 RECORDING MODE V (Variable-Length Records)

The COBOL `VBRC-FILE` uses `RECORDING MODE IS V` with records from 10 to 80
bytes. In the Java version, each logical record is written as a newline-delimited
line. The record length is implicit in the line content (12 chars for type 1,
39 chars for type 2).

### 4.4 COMP-3 (Packed Decimal) Output

The `OUT-ACCT-CURR-CYC-DEBIT` field is `COMP-3` in the COBOL FD, meaning it
would be stored as packed BCD on the mainframe. In the Java migration, all output
is written in human-readable signed-display format. This is a deliberate decision
to produce text files usable without mainframe-specific tooling.

---

## 5. Business Logic Preservation

### 5.1 Date Reformatting (COBDATFT Replacement)

The COBOL program calls assembler routine `COBDATFT` to convert the reissue date:
- **Input type 2** (`YYYY-MM-DD`) → **Output type 2** (`YYYYMMDD`)

The assembler was reverse-engineered from `app/asm/COBDATFT.asm` and the
`CODATECN.cpy` copybook. The `DateFormatter.convert()` method implements both
supported conversions:

| Input Type | Output Type | Conversion |
|---|---|---|
| `1` (YYYYMMDD) | `1` (YYYY-MM-DD) | Add dashes |
| `2` (YYYY-MM-DD) | `2` (YYYYMMDD) | Strip dashes |

Invalid combinations (1→2 or 2→1) throw `IllegalArgumentException`, matching the
assembler's `GOTOERR` branch.

### 5.2 Default Debit Substitution

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF
```

This hardcoded business rule is preserved exactly in `populateOutputRecord()`.
The constant `2525.00` is extracted to `DEFAULT_CYC_DEBIT`.

### 5.3 Array Record Hardcoded Values

The COBOL program populates only 3 of 5 array slots with hardcoded values:

| Slot (0-based) | Balance | Debit |
|---|---|---|
| 0 | Account's current balance | 1005.00 |
| 1 | Account's current balance | 1525.00 |
| 2 | -1025.00 | -2500.00 |
| 3 | 0.00 (INITIALIZE) | 0.00 |
| 4 | 0.00 (INITIALIZE) | 0.00 |

These values are preserved as named constants in the Java code.

---

## 6. Testing Strategy

### 6.1 Unit Tests (19 tests)

| Category | Tests | What They Verify |
|---|---|---|
| CobolNumericParser | 7 | Overpunch encoding/decoding, round-trip fidelity |
| DateFormatter | 3 | Both conversion directions + invalid-combo rejection |
| AccountFileReader | 1 | Correct parsing of 3-record sample file |
| populateOutputRecord | 2 | Zero-debit substitution + non-zero passthrough |
| populateArrayRecord | 1 | All 5 slots match COBOL hardcoded logic |
| End-to-end | 5 | Full pipeline: file counts, field content, console output, empty file, full 50-record dataset |

### 6.2 Test Data

- `src/test/resources/sample_acctdata.txt` — 3 records extracted from the
  original `app/data/ASCII/acctdata.txt`
- The full 50-record dataset is tested when running from the project root
  (the `endToEnd_fullDataset_50records` test resolves `../app/data/ASCII/acctdata.txt`).

---

## 7. What Is NOT Migrated

| COBOL Feature | Reason |
|---|---|
| JCL job wrapper | Not applicable; the Java program is invoked via `java -jar` or Maven |
| CICS transaction integration | CBACT01C is a batch program; no CICS dependency |
| EBCDIC encoding | Input data is assumed to already be in ASCII (from `app/data/ASCII/`) |
| Mainframe ABEND codes (`CEE3ABD`) | Replaced by `System.exit(999)` and exceptions |
| File-status display (`9910-DISPLAY-IO-STATUS`) | Replaced by exception messages with equivalent diagnostic information |

---

## 8. Project Structure

```
java-migration/
├── pom.xml                              # Maven build (Java 17, JUnit 5)
├── MIGRATION_NOTES.md                   # This document
└── src/
    ├── main/java/com/carddemo/
    │   ├── batch/
    │   │   └── Cbact01cBatch.java       # Main batch program (PROCEDURE DIVISION)
    │   ├── io/
    │   │   ├── AccountFileReader.java   # VSAM KSDS → flat-file reader
    │   │   ├── OutputAccountWriter.java # OUTFILE writer
    │   │   ├── ArrayRecordWriter.java   # ARRYFILE writer
    │   │   └── VariableLengthRecordWriter.java  # VBRCFILE writer
    │   ├── model/
    │   │   ├── AccountRecord.java       # CVACT01Y copybook (300-byte record)
    │   │   ├── OutputAccountRecord.java # FD OUT-FILE record
    │   │   ├── ArrayRecord.java         # FD ARRY-FILE record
    │   │   ├── VariableLengthRecord1.java # VBRC-REC1 (12 bytes)
    │   │   └── VariableLengthRecord2.java # VBRC-REC2 (39 bytes)
    │   └── util/
    │       ├── CobolNumericParser.java  # Signed display (overpunch) parser
    │       └── DateFormatter.java       # COBDATFT assembler replacement
    └── test/
        ├── java/com/carddemo/batch/
        │   └── Cbact01cBatchTest.java   # 19 JUnit 5 tests
        └── resources/
            └── sample_acctdata.txt      # 3-record test fixture
```

---

## 9. How to Run

```bash
# Build
cd java-migration
mvn clean package

# Run tests
mvn test

# Execute the batch program
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    output/outfile.dat \
    output/arryfile.dat \
    output/vbrcfile.dat
```
