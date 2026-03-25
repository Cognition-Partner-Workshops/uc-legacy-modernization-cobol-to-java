# CardDemo Migration Test Strategy

This document defines the four-dimensional testing approach used to validate the
migration of the CardDemo COBOL/CICS/VSAM mainframe application to Java.

---

## 1. Golden-File Testing

**Purpose:** Verify that the migrated Java code produces byte-for-byte identical
output to the original COBOL programs when given the same input data.

### Approach

| Step | Detail |
|------|--------|
| **Source of truth** | The fixed-width ASCII data files shipped in `app/data/ASCII/` are parsed using the COBOL copybook layouts in `app/cpy/` to produce structured JSON reference files stored in `golden-files/`. |
| **Parse method** | `test-harness/cobol_field_parser.py` reads each data file according to its copybook field definitions (PIC clauses) and emits one JSON document per file. |
| **Comparison** | `test-harness/golden_file_comparator.py` loads the golden JSON and the output of the equivalent Java component, then performs a field-by-field comparison with configurable tolerances for numeric rounding. |
| **Coverage** | Every VSAM data entity has a golden file: accounts, cards, customers, cross-references, transactions, daily transactions, transaction types, transaction categories, disclosure groups, and transaction category balances. |

### Golden-File Directory Layout

```
golden-files/
  acctdata.json        -- CVACT01Y (Account record, 300 bytes)
  carddata.json        -- CVACT02Y (Card record, 150 bytes)
  custdata.json        -- CVCUS01Y (Customer record, 500 bytes)
  cardxref.json        -- CVACT03Y (Card cross-reference, 50 bytes)
  dailytran.json       -- CVTRA06Y (Daily transaction, 350 bytes)
  discgrp.json         -- CVTRA02Y (Disclosure group, 50 bytes)
  tcatbal.json         -- CVTRA01Y (Tran category balance, 50 bytes)
  trancatg.json        -- CVTRA04Y (Transaction category, 60 bytes)
  trantype.json        -- CVTRA03Y (Transaction type, 60 bytes)
```

### When to Run

- After every code change that touches data parsing, I/O, or entity mapping.
- As a gate in CI before merging to `main`.

---

## 2. Differential Testing

**Purpose:** Detect unintended behavioural drift between the COBOL and Java
implementations by running both systems against the same inputs and comparing
their outputs.

### Approach

| Step | Detail |
|------|--------|
| **Dual execution** | For batch jobs (JCL -> Spring Batch), run the original COBOL batch program *and* the Java equivalent against the same input dataset. |
| **Output capture** | Capture both outputs in a normalised format (JSON or CSV). |
| **Diff engine** | `test-harness/differential_comparator.py` performs a structural diff that classifies differences as **field-value mismatch**, **missing record**, or **extra record**. |
| **Tolerance rules** | Numeric fields allow configurable epsilon (default `0.01` for currency). Timestamp fields allow configurable window (default `0` seconds). Trailing whitespace in alphanumeric fields is stripped before comparison. |

### Key Batch Jobs to Cover

| JCL Job | COBOL Program | Java Equivalent | Description |
|---------|---------------|-----------------|-------------|
| POSTTRAN | CBTRN02C | `PostTransactionJob` | Transaction posting |
| INTCALC | CBACT04C | `InterestCalcJob` | Interest calculation |
| CREASTMT | CBSTM03A/B | `StatementGenJob` | Statement generation |
| TRANREPT | CBTRN03C | `TransactionReportJob` | Transaction report |
| COMBTRAN | (combine logic) | `CombineTransJob` | Combine transactions |

### When to Run

- Nightly or on-demand during active migration development.
- Before each milestone review to confirm behavioural parity.

---

## 3. Reconciliation Testing

**Purpose:** Validate data integrity invariants that must hold true after each
batch processing cycle, regardless of whether the system under test is COBOL or
Java.

### Approach

| Step | Detail |
|------|--------|
| **Invariant catalogue** | Every batch job has a set of reconciliation checks defined in `RECONCILIATION_CHECKS.md`. |
| **Check runner** | `test-harness/reconciliation_runner.py` loads the post-execution data and evaluates each check, reporting PASS / FAIL / WARN with details. |
| **Check types** | Record-count checks, cross-reference integrity, balance equations, key uniqueness, referential integrity, and business-rule validations. |

### Core Invariants

1. **Record counts** -- The number of account records must equal the number
   of entries in the account master after a refresh.
2. **Cross-reference integrity** -- Every card in `cardxref` must reference a
   valid account and a valid customer.
3. **Balance equations** -- After posting, the sum of transaction amounts per
   account must equal the change in the account current-cycle debit/credit
   fields.
4. **Key uniqueness** -- No duplicate primary keys in any master file
   (account ID, card number, customer ID).
5. **Referential integrity** -- Every transaction's card number must exist in
   the card master.

### When to Run

- After every batch cycle execution (CLOSEFIL -> data refresh -> POSTTRAN ->
  INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL).
- As smoke tests after initial data load in the Java environment.

---

## 4. Contract Testing

**Purpose:** Ensure the Java REST API layer (replacing CICS transactions)
honours the data contracts implied by the COBOL copybook structures and BMS
screen maps.

### Approach

| Step | Detail |
|------|--------|
| **Contract source** | Each copybook defines the canonical field names, types, and sizes. These are codified in `test-harness/contracts/` as JSON Schema files. |
| **Schema generation** | `test-harness/contract_schema_generator.py` reads copybook definitions and produces JSON Schema documents. |
| **Validation** | `test-harness/contract_validator.py` fetches a Java API response and validates it against the corresponding JSON Schema. |
| **Coverage** | Every CICS transaction that becomes a REST endpoint must have a contract test. |

### CICS Transaction -> REST Endpoint Mapping

| CICS Txn | COBOL Program | REST Endpoint | Contract Schema |
|-----------|---------------|---------------|-----------------|
| CC00 | COSGN00C | `POST /api/auth/login` | `sec_user_data.schema.json` |
| CM00 | COMEN01C | `GET /api/menu` | `menu.schema.json` |
| (account view) | COACTVWC | `GET /api/accounts/{id}` | `account_record.schema.json` |
| (card list) | COCRDLIC | `GET /api/cards` | `card_record.schema.json` |
| (transaction list) | COTRN00C | `GET /api/transactions` | `tran_record.schema.json` |
| (transaction add) | COTRN02C | `POST /api/transactions` | `tran_record.schema.json` |
| (bill payment) | COBIL00C | `POST /api/payments` | `payment.schema.json` |

### When to Run

- On every PR that modifies Java API controllers or DTOs.
- As part of integration test suites.

---

## Test Execution Summary

| Dimension | Tooling | Frequency | Blocks Merge? |
|-----------|---------|-----------|---------------|
| Golden-file | `golden_file_comparator.py` | Every PR | Yes |
| Differential | `differential_comparator.py` | Nightly / milestone | Advisory |
| Reconciliation | `reconciliation_runner.py` | Post-batch-cycle | Yes |
| Contract | `contract_validator.py` | Every PR | Yes |

---

## Directory Structure

```
TEST_STRATEGY.md                 -- This document
RECONCILIATION_CHECKS.md         -- Per-job reconciliation specifications
golden-files/                    -- JSON golden reference files
  *.json
test-harness/                    -- Python test utilities
  cobol_field_parser.py          -- Copybook-aware fixed-width parser
  golden_file_comparator.py      -- Golden-file comparison engine
  differential_comparator.py     -- Dual-output diff engine
  reconciliation_runner.py       -- Reconciliation check executor
  contract_schema_generator.py   -- Copybook -> JSON Schema generator
  contract_validator.py          -- API response schema validator
  contracts/                     -- Generated JSON Schema files
    account_record.schema.json
    card_record.schema.json
    customer_record.schema.json
    card_xref_record.schema.json
    tran_record.schema.json
    daily_tran_record.schema.json
    tran_type_record.schema.json
    tran_cat_record.schema.json
    dis_group_record.schema.json
    tran_cat_bal_record.schema.json
    sec_user_data.schema.json
```
