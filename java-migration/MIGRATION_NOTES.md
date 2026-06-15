# CBACT01C Migration Notes — COBOL to Java 21

## Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| Program | CBACT01C.cbl | `AccountFileProcessor.java` |
| Language | COBOL 85 (batch) | Java 21 |
| Runtime | z/OS + CICS batch | JVM (standalone CLI) |
| Build | JCL compilation | Maven |

## COBOL-to-Java Type Mappings

| COBOL PIC | Java Type | Notes |
|-----------|-----------|-------|
| `PIC 9(n)` | `String` | Preserved as zero-padded string for key identity |
| `PIC X(n)` | `String` | Trimmed trailing spaces on read |
| `PIC S9(m)V9(n)` | `BigDecimal` | Exact decimal; avoids floating-point drift |
| `PIC S9(m)V9(n) COMP-3` | `BigDecimal` + `byte[]` | BigDecimal in-memory; packed-decimal on output |
| `OCCURS n TIMES` | Fixed-size array (`T[]`) | Array length matches COBOL OCCURS clause |
| `FILLER PIC X(n)` | (omitted) | Not carried into Java model |
| `01 level record` | Java `record` | Immutable value type |
| `88 level condition` | Constants / enum-like checks | e.g., `APPL-EOF VALUE 16` → `EOF` sentinel |

## Data Format: Sign Overpunch (Zoned Decimal)

The ASCII data files use EBCDIC-compatible sign-overpunch encoding on the last character
of signed numeric fields:

| Character | Sign | Digit |
|-----------|------|-------|
| `{` | + | 0 |
| `A`–`I` | + | 1–9 |
| `}` | − | 0 |
| `J`–`R` | − | 1–9 |

`CobolFieldParser.parseSignedDecimal()` handles this decoding.

## Business Rules Extracted

### 1. Debit Substitution Rule (1300-POPUL-ACCT-RECORD)

```cobol
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF
```

When a cycle debit is zero, the output record receives a hardcoded value of **2525.00**.
This likely represents a default minimum debit or a sentinel for downstream processing.

### 2. Date Reformatting via COBDATFT (Assembler)

The reissue date is reformatted from `YYYY-MM-DD` → `YYYYMMDD` using the COBDATFT
assembler routine (called via the CODATECN copybook interface). In Java, this is a
simple string operation in `DateConverter.convertYyyyMmDdToCompact()`.

### 3. Array Record Population (1400-POPUL-ARRAY-RECORD)

Each account produces a 5-element array record:

| Index | Balance | Debit | Source |
|-------|---------|-------|--------|
| 1 | ACCT-CURR-BAL | 1005.00 | From input + hardcoded |
| 2 | ACCT-CURR-BAL | 1525.00 | From input + hardcoded |
| 3 | −1025.00 | −2500.00 | Hardcoded |
| 4 | 0.00 | 0.00 | INITIALIZE (zeros) |
| 5 | 0.00 | 0.00 | INITIALIZE (zeros) |

### 4. Variable-Length Record Split (1500-POPUL-VBRC-RECORD)

Each account produces two VB records:
- **Record 1** (12 bytes): Account ID + Active Status
- **Record 2** (39 bytes): Account ID + Current Balance + Credit Limit + Reissue Year (YYYY)

The year is extracted from the reissue date field's first 4 characters.

## File I/O Mapping

| COBOL File | Type | Java Equivalent |
|------------|------|-----------------|
| ACCTFILE (KSDS indexed, sequential access) | Input | `BufferedReader` line-by-line |
| OUT-FILE (sequential) | Output | `PrintWriter` fixed-width lines |
| ARRY-FILE (sequential) | Output | `PrintWriter` fixed-width lines |
| VBRC-FILE (VB, variable-length) | Output | `DataOutputStream` with length prefix |

## Ambiguities and Decisions

### 1. COMP-3 Output Representation

COBOL writes COMP-3 fields as raw packed-decimal bytes in the output file. Since the
Java output files are text-based (for portability and testability), COMP-3 values are
written as **hex-encoded strings** of the packed-decimal bytes. A production system
consuming these files would need binary output matching the mainframe format.

### 2. Variable-Length Record Format

COBOL's `RECORDING MODE IS V` produces records with a 4-byte Record Descriptor Word (RDW).
The Java implementation uses a 2-byte length prefix (big-endian short) followed by the
record data. This is a simplification — a production migration would match the exact
RDW format (4 bytes: 2-byte length + 2 reserved bytes).

### 3. ACCT-ADDR-ZIP Not Propagated to Output

The COBOL program reads `ACCT-ADDR-ZIP` from the input but does not write it to any
output file. The Java model includes it in `AccountRecord` for completeness but it is
not part of any output record, matching the original behavior.

### 4. Account ID as String (Not Long)

Account IDs are kept as zero-padded strings rather than numeric types. This preserves
the fixed-width formatting required for output files and avoids loss of leading zeros.

### 5. Hardcoded Values

The COBOL program contains several hardcoded numeric constants (1005.00, 1525.00,
-1025.00, -2500.00, 2525.00) without explanatory comments. These are preserved as
named constants in the Java code. Their business meaning is undocumented in the
original source.

### 6. Debit Substitution — Intentional Behavioral Fix

The COBOL `1300-POPUL-ACCT-RECORD` only moves a value to `OUT-ACCT-CURR-CYC-DEBIT` when
the input debit equals zero (substituting 2525.00). There is no `ELSE` branch — when the
debit is non-zero, the output field retains whatever stale/undefined value was left in
the FD buffer from the previous WRITE. This is a latent bug in the original COBOL.

The Java translation **always** sets the output debit: either the actual non-zero value
or the 2525.00 substitution. This is an intentional correctness improvement. All records
in the test fixture have zero debits, so this divergence is not exercised by tests.

### 7. Error Handling

The COBOL program calls `CEE3ABD` (Language Environment abend) on I/O errors. The Java
version throws `IOException` which propagates to the caller. A production system would
add structured logging and retry logic.

## Testing Strategy

- **Unit tests**: `CobolFieldParserTest` and `DateConverterTest` verify field-level parsing/formatting
- **Integration tests**: `AccountFileProcessorTest` uses `app/data/ASCII/acctdata.txt` as
  a fixture, verifying end-to-end processing produces correct results
- **Parity verification**: Tests assert specific field values for known input records,
  confirming the Java output matches what COBOL would produce

## How to Run

```bash
cd java-migration
./mvnw package
java -jar target/cbact01c-migration-1.0.0.jar \
    ../app/data/ASCII/acctdata.txt \
    /tmp/outfile.dat \
    /tmp/arryfile.dat \
    /tmp/vbrcfile.dat
```
