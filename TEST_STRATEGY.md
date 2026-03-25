# CardDemo Migration Test Strategy

## Overview

This document defines the testing strategy for validating the migration of the
CardDemo COBOL/CICS/VSAM mainframe application to Java. The strategy covers
four complementary testing dimensions that together provide comprehensive
assurance that the migrated system preserves the semantics, data integrity, and
contractual behaviour of the original COBOL application.

---

## 1. Golden-File Testing

### Purpose
Verify that the migrated Java code produces **byte-for-byte identical** structured
output when given the same input data that the COBOL system processes.

### Approach

| Step | Description |
|------|-------------|
| **Parse** | Read each ASCII fixed-width data file (`app/data/ASCII/*.txt`) using the corresponding COBOL copybook layout (`app/cpy/*.cpy`). |
| **Produce** | Generate structured JSON golden-reference files in `golden-files/` with every field extracted, typed, and named according to the copybook. |
| **Compare** | Run the equivalent Java component (batch job, service, DTO deserializer) against the same input and diff the output against the golden JSON. |

### Golden Files Produced

| Source File | Copybook | Golden File | Record Length |
|-------------|----------|-------------|---------------|
| `acctdata.txt` | `CVACT01Y.cpy` | `acctdata.golden.json` | 300 |
| `carddata.txt` | `CVACT02Y.cpy` | `carddata.golden.json` | 150 |
| `cardxref.txt` | `CVACT03Y.cpy` | `cardxref.golden.json` | 50 |
| `custdata.txt` | `CVCUS01Y.cpy` | `custdata.golden.json` | 500 |
| `dailytran.txt` | `CVTRA06Y.cpy` | `dailytran.golden.json` | 350 |
| `discgrp.txt` | `CVTRA02Y.cpy` | `discgrp.golden.json` | 50 |
| `tcatbal.txt` | `CVTRA01Y.cpy` | `tcatbal.golden.json` | 50 |
| `trancatg.txt` | `CVTRA04Y.cpy` | `trancatg.golden.json` | 60 |
| `trantype.txt` | `CVTRA03Y.cpy` | `trantype.golden.json` | 60 |

### Data-Type Handling

| COBOL PIC Clause | Storage | Java Mapping | Parser Handling |
|------------------|---------|--------------|-----------------|
| `PIC 9(n)` | Zoned decimal (unsigned) | `long` / `BigDecimal` | Extract n characters, parse as integer |
| `PIC S9(n)V99` | Signed zoned with implied decimal | `BigDecimal` | Detect trailing sign overpunch (`{`=+0, `A`-`I`=+1..+9, `}`=-0, `J`-`R`=-1..-9), insert decimal point |
| `PIC X(n)` | Alphanumeric | `String` | Extract n characters, right-trim spaces |
| `FILLER` | Padding | *(skipped)* | Advance offset by filler length |

### Pass Criteria
- Every field in the golden JSON matches the Java-produced output.
- Numeric precision is preserved (no floating-point drift).
- Alphanumeric fields match after right-trimming whitespace.

---

## 2. Differential Testing

### Purpose
Detect **behavioural regressions** by running the COBOL and Java implementations
side-by-side on identical inputs and comparing their outputs field-by-field.

### Approach

```
                  +-----------+
  Input Data ---->| COBOL App |----> Output A
                  +-----------+
                                        |
                                     diff(A, B)
                                        |
                  +-----------+         v
  Input Data ---->| Java App  |----> Output B
                  +-----------+
```

| Phase | Action |
|-------|--------|
| **Baseline capture** | Record COBOL outputs for each batch job using the golden-file parser. |
| **Java execution** | Run the equivalent Java batch job / service against the same input files. |
| **Field-level diff** | Use `test-harness/comparator.py` to compare outputs field-by-field, respecting type-aware tolerances. |
| **Report** | Produce a differential report listing every mismatched field with COBOL vs Java values. |

### Tolerance Rules

| Field Type | Tolerance | Rationale |
|------------|-----------|-----------|
| Integer / Zoned decimal | Exact match | No precision loss expected |
| Signed decimal (implied V) | `|delta| < 0.005` | Rounding during decimal conversion |
| Alphanumeric | Exact after trim | Whitespace normalization only |
| Date fields (`YYYY-MM-DD`) | Exact match | Format is already ISO-8601 in ASCII data |
| Timestamps | `|delta| <= 1s` | Clock skew tolerance for proc timestamps |

### Batch Jobs to Diff

| JCL Job | COBOL Program | Java Equivalent | Key Outputs |
|---------|---------------|-----------------|-------------|
| `POSTTRAN` | `CBTRN02C` | Transaction posting service | Updated account balances, transaction records |
| `INTCALC` | `CBACT04C` | Interest calculation service | Updated `ACCT-CURR-BAL` with accrued interest |
| `CREASTMT` | `CBSTM03A/B` | Statement generation service | Statement output records |
| `COMBTRAN` | *(combine)* | Transaction combiner | Merged transaction file |
| `TRANREPT` | `CBTRN03C` | Transaction report service | Report output matching `CVTRA07Y` layout |

---

## 3. Reconciliation Testing

### Purpose
Validate **aggregate data integrity** across the migration boundary. Even if
individual records look correct, reconciliation catches systemic issues such as
missing records, double-counting, or broken referential integrity.

### Approach

Reconciliation checks operate at three levels:

#### Level 1 -- Record Counts
Verify that every migrated dataset has the same number of records as the source.

#### Level 2 -- Control Totals
Sum key numeric columns (balances, transaction amounts) in both systems and
compare totals. See `RECONCILIATION_CHECKS.md` for per-job specifications.

#### Level 3 -- Referential Integrity
Validate cross-file relationships:

| Parent File | Child File | Join Key | Constraint |
|-------------|------------|----------|------------|
| `acctdata` | `carddata` | `ACCT-ID` = `CARD-ACCT-ID` | Every card references a valid account |
| `custdata` | `cardxref` | `CUST-ID` = `XREF-CUST-ID` | Every xref references a valid customer |
| `acctdata` | `cardxref` | `ACCT-ID` = `XREF-ACCT-ID` | Every xref references a valid account |
| `carddata` | `cardxref` | `CARD-NUM` = `XREF-CARD-NUM` | Every xref references a valid card |
| `carddata` | `dailytran` | `CARD-NUM` = `DALYTRAN-CARD-NUM` | Every transaction references a valid card |
| `acctdata` | `tcatbal` | `ACCT-ID` = `TRANCAT-ACCT-ID` | Every category balance references a valid account |
| `trantype` | `trancatg` | `TRAN-TYPE` = `TRAN-TYPE-CD` | Every category references a valid type |
| `acctdata` | `discgrp` | `ACCT-GROUP-ID` = `DIS-ACCT-GROUP-ID` | Every disclosure group references a valid account group |

### Implementation
The reconciliation engine is implemented in `test-harness/reconciliation.py` and
reads the golden JSON files to perform all checks. Results are written to
`reconciliation-report.json`.

---

## 4. Contract Testing

### Purpose
Ensure that the **external interfaces** of the migrated Java system conform to
the contracts defined by the original COBOL application -- specifically the
record layouts (copybooks), CICS transaction codes, and batch job
input/output specifications.

### Approach

#### 4a. Data Contract (Record Layout) Tests
Each COBOL copybook defines a data contract. The Java DTOs / POJOs that replace
these copybooks must:

| Check | Description |
|-------|-------------|
| **Field count** | Same number of business fields (excluding FILLER) |
| **Field names** | Semantic equivalence (e.g., `ACCT-ID` -> `acctId` or `accountId`) |
| **Field types** | Correct Java type for each PIC clause |
| **Field order** | Serialization order matches copybook order |
| **Record length** | Serialized fixed-width output matches COBOL record length |

#### 4b. Transaction Contract Tests
Each CICS transaction maps to a REST endpoint or service method in the Java app:

| COBOL Transaction | Expected Java Endpoint | Contract |
|-------------------|----------------------|----------|
| `CC00` (Signon) | `POST /api/auth/login` | Accepts user-id + password, returns session |
| `CM00` (Main Menu) | `GET /api/menu` | Returns menu items for user role |
| `COTRN02` (Add Transaction) | `POST /api/transactions` | Accepts transaction record, returns confirmation |
| `COBIL00` (Bill Payment) | `POST /api/payments` | Accepts payment details, updates balance |
| `CORPT00` (Reports) | `GET /api/reports/transactions` | Returns report matching `CVTRA07Y` layout |

#### 4c. Batch Job Contract Tests
Each JCL job defines an input/output contract:

| Job | Input Files | Output Files | Contract Check |
|-----|-------------|--------------|----------------|
| `POSTTRAN` | `dailytran`, `acctdata`, `cardxref` | Updated `acctdata`, transaction log | Balance changes = sum of posted transactions |
| `INTCALC` | `acctdata`, `discgrp` | Updated `acctdata` | Interest = balance * rate / 365 * days |
| `CREASTMT` | `acctdata`, `custdata`, `dailytran` | Statement records | One statement per active account with activity |
| `COMBTRAN` | `dailytran`, existing transactions | Combined transaction file | Union of both files, no duplicates |

### Implementation
Contract test specifications are defined as JSON schemas in
`test-harness/contracts/` and validated using `test-harness/contract_validator.py`.

---

## Test Execution Order

```
Phase 1: Golden-File Generation
  $ python test-harness/generate_golden_files.py

Phase 2: Golden-File Comparison (after Java migration)
  $ python test-harness/comparator.py --mode golden

Phase 3: Differential Testing (requires both COBOL and Java outputs)
  $ python test-harness/comparator.py --mode differential

Phase 4: Reconciliation
  $ python test-harness/reconciliation.py

Phase 5: Contract Validation
  $ python test-harness/contract_validator.py
```

---

## Directory Structure

```
TEST_STRATEGY.md                  # This document
RECONCILIATION_CHECKS.md          # Per-job reconciliation specifications
golden-files/                     # Generated JSON golden references
  acctdata.golden.json
  carddata.golden.json
  ...
test-harness/
  generate_golden_files.py        # Parses ASCII data -> golden JSON
  comparator.py                   # Field-level comparison engine
  reconciliation.py               # Aggregate integrity checks
  contract_validator.py           # Contract conformance checks
  copybook_parser.py              # COBOL copybook layout definitions
  field_parser.py                 # PIC clause field extraction utilities
  contracts/                      # JSON schema contract definitions
    acctdata_contract.json
    carddata_contract.json
    ...
```

---

## Success Criteria

| Dimension | Gate |
|-----------|------|
| Golden-File | 100% field match on all 9 data files |
| Differential | Zero field-level mismatches beyond tolerance |
| Reconciliation | All record counts, control totals, and referential integrity checks pass |
| Contract | All Java DTOs and endpoints conform to copybook and transaction contracts |
