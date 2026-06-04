# CBACT01C Migration Notes: COBOL to Java 17+

## Overview

| Attribute | COBOL | Java |
|-----------|-------|------|
| Program | `CBACT01C.cbl` | `Cbact01cApplication.java` |
| Type | Batch (JCL-invoked) | CLI application (JAR) |
| Language Level | COBOL-85 | Java 17+ (records, sealed, text blocks) |
| Data Access | VSAM KSDS sequential | `java.nio.file` BufferedReader |
| Build | JCL COMPILE/LINK | Maven 3.x |

## Business Logic Preserved

The COBOL program reads an indexed VSAM account file sequentially and writes three output files. All business rules are faithfully reproduced:

1. **Zero-Debit Substitution**: When `ACCT-CURR-CYC-DEBIT = 0`, the output substitutes `2525.00`. This rule is preserved in `AccountFileProcessor.buildOutputRecord()`.

2. **Date Reformatting**: The COBOL program calls assembler subroutine `COBDATFT` via the `CODATECN` copybook to convert reissue dates from `YYYY-MM-DD` to `YYYYMMDD`. This is replaced by `DateFormatService.convert()`.

3. **Array Record Population**: The `OCCURS 5 TIMES` array is populated only at indices 1-3 (0-based in Java):
   - [0]: account balance + hardcoded debit 1005.00
   - [1]: account balance + hardcoded debit 1525.00
   - [2]: hardcoded balance -1025.00 + hardcoded debit -2500.00
   - [3-4]: zeroed (from COBOL `INITIALIZE`)

4. **Variable-Length Records**: Two records per account written to VBRC file:
   - VB1 (12-byte): account ID + active status
   - VB2 (39-byte): account ID + balance + credit limit + reissue year

## Translation Decisions

### Data Structures

| COBOL Construct | Java Equivalent | Rationale |
|----------------|-----------------|-----------|
| Copybook `CVACT01Y` (01 level) | `record AccountRecord` | Immutable value semantics match copybook intent |
| `PIC 9(11)` | `long` | Sufficient range for 11-digit account IDs |
| `PIC S9(10)V99` | `BigDecimal` | Exact decimal arithmetic, no floating-point drift |
| `PIC X(n)` | `String` | Natural mapping for alphanumeric fields |
| `COMP-3` (packed decimal) | `BigDecimal` | Java has no packed decimal; BigDecimal is semantically equivalent |
| `OCCURS 5 TIMES` | `List<BalanceEntry>` with fixed size 5 | Type-safe, bounds-checked collection |
| `REDEFINES` | Separate record types | No memory aliasing in Java; each view is its own type |
| `RECORDING MODE V` | Line-based output | Variable-length records expressed via delimited format |

### I/O Strategy

| COBOL | Java | Notes |
|-------|------|-------|
| VSAM KSDS (indexed, sequential access) | Flat file, line-per-record | VSAM sequential read = reading line-by-line |
| `OPEN INPUT / OUTPUT` | `try-with-resources` | Automatic resource management replaces explicit OPEN/CLOSE |
| `FILE STATUS` checks | `IOException` | Java exception model replaces status-code checking |
| `CALL 'CEE3ABD'` (abend) | `System.exit(999)` / `IOException` | Abnormal termination mapped to non-zero exit + exception |
| Fixed-width output records | Pipe-delimited output | More portable; fixed-width can be added if needed |
| `WRITE ... FROM` | `BufferedWriter.write()` | Buffered I/O for performance |

### Date Conversion (COBDATFT Replacement)

The assembler program `COBDATFT` accepts a `CODATECN-REC` structure with:
- Input type (`1` = YYYYMMDD, `2` = YYYY-MM-DD)
- Output type (`1` = YYYY-MM-DD, `2` = YYYYMMDD)

`DateFormatService` is a pure-function replacement that handles both directions. The COBOL program specifically uses input type `2` → output type `2` (YYYY-MM-DD → YYYYMMDD) for the reissue date.

### Error Handling

| COBOL Pattern | Java Pattern |
|---------------|--------------|
| Check `FILE STATUS`, DISPLAY error, PERFORM 9999-ABEND | Throw `IOException` with descriptive message |
| `9910-DISPLAY-IO-STATUS` (binary status decode) | Stack trace + exception message |
| `GOBACK` (normal termination) | Method return / `System.exit(0)` |

### Naming Conventions

- COBOL paragraph names (e.g., `1300-POPUL-ACCT-RECORD`) → Java method names (e.g., `buildOutputRecord`)
- COBOL data names with hyphens → Java camelCase (e.g., `ACCT-CURR-BAL` → `currBal`)
- Program name `CBACT01C` preserved in class name `Cbact01cApplication`

## What Is NOT Migrated

1. **JCL**: Job control (file DD assignments, step execution) is replaced by CLI arguments
2. **EBCDIC encoding**: Java uses UTF-8; no character set translation needed for text-mode files
3. **CICS integration**: This is a batch program; no transaction monitor involvement
4. **Physical VSAM dataset attributes**: Block sizes, CI/CA splits, etc. are infrastructure concerns

## Testing Strategy

JUnit 5 tests verify parity by:
1. Constructing fixed-width input records matching the CVACT01Y copybook layout
2. Running the full processing pipeline
3. Asserting each business rule produces identical outputs:
   - Zero-debit substitution → 2525.00
   - Date reformatting → YYYYMMDD
   - Array population pattern (indices 0-2 populated, 3-4 zero)
   - VB record structure (alternating short/long records)
4. Edge cases: empty file, missing file (abend equivalent), malformed input

## Running

```bash
cd cbact01c-java
mvn clean package
java -jar target/cbact01c-batch-1.0.0.jar <acctfile> <outfile> <arryfile> <vbrcfile>
```

## Future Considerations

- **Spring Batch**: For production workloads, wrap in Spring Batch with chunk-oriented processing
- **Database output**: Replace file writers with JPA/JDBC for relational storage
- **Observability**: Add structured logging (SLF4J) and metrics
- **Schema validation**: Add Bean Validation annotations to record fields
