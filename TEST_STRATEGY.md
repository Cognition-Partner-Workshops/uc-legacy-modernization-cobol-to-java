# Migration Test Strategy

This document defines the four-dimensional testing approach for validating the CardDemo COBOL-to-Java migration. Each dimension targets a distinct class of migration defect and together they provide end-to-end confidence that the converted system is functionally equivalent to the legacy mainframe application.

---

## 1. Golden-File Testing

**Goal:** Prove that the Java system can ingest legacy flat files and produce byte-identical (or semantically identical) structured output compared to a known-good reference.

### How It Works

1. **Parse** each ASCII data file (`acctdata.txt`, `custdata.txt`, `carddata.txt`, etc.) using the COBOL copybook layout definitions.
2. **Produce** structured JSON "golden reference" files that capture every field, its raw value, and its parsed/typed representation.
3. **Compare** the output of the migrated Java parsers against these golden references using field-by-field JSON diff.

### What It Catches

| Defect Class | Example |
|---|---|
| Field offset drift | Account balance shifted by 1 byte after code change |
| Sign-nibble misparse | COBOL `S9(10)V99` overpunch sign read incorrectly |
| Filler corruption | Data bleeding into FILLER area or vice-versa |
| Encoding errors | EBCDIC-to-ASCII conversion artifacts |

### Artifacts

- `golden-files/*.json` -- One JSON file per data file, containing an array of parsed records.
- Copybook-to-field-map embedded in each golden file's `_metadata` header.

### Pass Criteria

- **Strict mode:** Byte-for-byte JSON match (ignoring whitespace).
- **Tolerant mode:** All typed values match after normalization (e.g., trimmed strings, normalized dates). Differences are logged but do not fail the test if they fall within documented tolerance rules.

---

## 2. Differential Testing

**Goal:** Run the same business operation on both the legacy COBOL system (or a reference simulator) and the migrated Java system, then compare outputs to detect behavioral divergence.

### How It Works

1. **Seed** both systems with identical input data (the ASCII flat files).
2. **Execute** equivalent operations:
   - Legacy: JCL batch job (e.g., `POSTTRAN`, `INTCALC`, `CREASTMT`)
   - Java: Corresponding Spring Batch job or service method
3. **Capture** output files / database state from both systems.
4. **Diff** the outputs field-by-field, record-by-record.

### Test Matrix

| Legacy Job | Java Equivalent | Input Files | Output Comparison |
|---|---|---|---|
| `POSTTRAN` | TransactionPostingJob | `dailytran.txt`, `acctdata.txt` | Updated account balances, transaction status |
| `INTCALC` | InterestCalculationJob | `acctdata.txt`, `discgrp.txt` | Interest amounts, balance adjustments |
| `CREASTMT` | StatementGenerationJob | `acctdata.txt`, `custdata.txt` | Statement records, totals |
| `CBTRN03C` | TransactionReportJob | `dailytran.txt`, `trancatg.txt` | Report output, category totals |
| `COMBTRAN` | CombineTransactionsJob | `dailytran.txt` | Merged transaction file |

### What It Catches

| Defect Class | Example |
|---|---|
| Business logic divergence | Interest calculated with different rounding |
| Missing edge-case handling | Negative balance not handled in Java |
| Transaction ordering | Records processed in different sequence |
| Accumulator drift | Running totals diverge due to precision differences |

### Pass Criteria

- All output fields match within defined tolerance (e.g., monetary amounts within 0.01 for rounding differences).
- Record counts are identical.
- Any differences are logged with full context for manual review.

---

## 3. Reconciliation Testing

**Goal:** Validate data integrity invariants that must hold after migration -- row counts, financial totals, referential integrity, and cross-entity consistency.

### How It Works

1. **Load** the golden-file data into the test harness.
2. **Run** a suite of reconciliation checks that verify aggregate properties of the data.
3. **Report** pass/fail for each check with actual vs. expected values.

### Check Categories

| Category | Description | Example Check |
|---|---|---|
| **Row Counts** | Record counts per file must match expected | `acctdata.txt` has exactly 50 records |
| **Financial Totals** | Sum of monetary fields must match control totals | Sum of all account balances = expected total |
| **Referential Integrity** | Foreign key relationships must be valid | Every `cardxref` account ID exists in `acctdata` |
| **Cross-File Consistency** | Related files must agree on shared data | Card count per account matches `cardxref` entries |
| **Status Distribution** | Status code frequencies match expected | Active account percentage within expected range |
| **Date Validity** | All dates parse correctly and fall in valid ranges | No account open dates in the future |
| **Uniqueness** | Primary keys and unique fields have no duplicates | All account IDs are unique |

### Artifacts

- `RECONCILIATION_CHECKS.md` -- Detailed per-job validation specifications.
- `test-harness/reconciliation.py` -- Executable reconciliation check suite.

### Pass Criteria

- All reconciliation checks pass (zero failures).
- Any new check added must document its rationale and expected value source.

---

## 4. Contract Testing

**Goal:** Ensure the migrated Java application exposes the same external interfaces (API endpoints, file formats, message schemas) as the legacy system, preserving backward compatibility for downstream consumers.

### How It Works

1. **Define** contracts for each external interface:
   - CICS transaction request/response shapes (map to REST API contracts)
   - Batch job input/output file formats (fixed-width record layouts)
   - Inter-program communication areas (COMMAREA structures)
2. **Generate** contract test cases from the COBOL copybook definitions.
3. **Validate** that the Java implementation satisfies each contract.

### Contract Inventory

| Legacy Interface | Java Equivalent | Contract Type |
|---|---|---|
| CICS `CC00` (Signon) | `POST /api/auth/login` | Request/response JSON schema |
| CICS `CM00` (Main Menu) | `GET /api/menu` | Response JSON schema |
| Account View (`COACTVWC`) | `GET /api/accounts/{id}` | Response matches copybook `CVACT01Y` fields |
| Card List (`COCRDLIC`) | `GET /api/cards` | Response matches copybook `CVACT02Y` fields |
| Transaction List (`COTRN00C`) | `GET /api/transactions` | Response matches copybook `CVTRA05Y` fields |
| Batch file output | `*.txt` flat files | Fixed-width layout per copybook |
| VSAM record format | Database row | Column types match copybook field types |

### What It Catches

| Defect Class | Example |
|---|---|
| Missing fields | Java DTO drops a copybook field |
| Type changes | Numeric field returned as string in JSON |
| Format changes | Date format changed from `YYYY-MM-DD` to ISO 8601 |
| Field name drift | `ACCT-ID` renamed to `accountId` without mapping |

### Pass Criteria

- All contract tests pass.
- Any intentional contract changes are documented in a migration change log.
- Backward-compatible: existing consumers can parse the new output without modification.

---

## Test Execution Order

The recommended execution order ensures that lower-level checks pass before higher-level integration checks run:

```
1. Golden-File Tests      (data parsing correctness)
       |
       v
2. Contract Tests         (interface shape correctness)
       |
       v
3. Differential Tests     (behavioral equivalence)
       |
       v
4. Reconciliation Tests   (data integrity invariants)
```

## Directory Structure

```
TEST_STRATEGY.md              <-- This document
RECONCILIATION_CHECKS.md      <-- Per-job validation specs
golden-files/
  acctdata.json               <-- Golden reference: account records
  carddata.json               <-- Golden reference: card records
  cardxref.json               <-- Golden reference: card cross-reference
  custdata.json               <-- Golden reference: customer records
  dailytran.json              <-- Golden reference: daily transactions
  discgrp.json                <-- Golden reference: disclosure groups
  tcatbal.json                <-- Golden reference: transaction category balances
  trancatg.json               <-- Golden reference: transaction categories
  trantype.json               <-- Golden reference: transaction types
test-harness/
  copybook_parser.py          <-- Generic COBOL copybook-aware parser
  golden_file_generator.py    <-- Generates golden JSON from ASCII data
  comparator.py               <-- Field-by-field JSON comparison engine
  reconciliation.py           <-- Reconciliation check suite
  contract_validator.py       <-- Contract schema validator
  run_all_tests.py            <-- Orchestrator for all test dimensions
  requirements.txt            <-- Python dependencies
```

## Tooling

- **Language:** Python 3.10+ (lightweight, no build system needed)
- **Dependencies:** Standard library only (`json`, `os`, `re`, `decimal`, `datetime`); no external packages required.
- **Execution:** `python test-harness/run_all_tests.py` from the repository root.
