# CardDemo Migration Test Strategy

This document defines the testing strategy for validating the migration of CardDemo
from COBOL/CICS/VSAM on the mainframe to Java. It covers four complementary
dimensions that together ensure functional equivalence, data integrity, and
interface compatibility between the legacy system and the modernized application.

---

## 1. Golden-File Testing

### Purpose

Verify that the migrated Java application produces **byte-for-byte identical**
(or semantically equivalent) output when given the same input as the original
COBOL programs. Golden files are pre-approved reference outputs derived from the
legacy system's known-good ASCII data files.

### Approach

| Step | Detail |
|------|--------|
| **Parse legacy data** | Fixed-width ASCII data files (`app/data/ASCII/*.txt`) are parsed using copybook layouts (`app/cpy/*.cpy`) to produce structured JSON golden references stored in `golden-files/`. |
| **Run Java equivalent** | Execute the migrated Java batch job or service with the same input data. |
| **Compare outputs** | Diff the Java-produced JSON against the golden-file JSON using the test harness comparison utilities. |
| **Tolerance rules** | Numeric fields use configurable absolute/relative tolerance (e.g., `1e-2` for currency rounding). Timestamp fields allow format-only differences. Filler/padding bytes are ignored. |

### Golden-File Inventory

| Source File | Copybook | Record Length | Golden File |
|-------------|----------|---------------|-------------|
| `acctdata.txt` | `CVACT01Y.cpy` | 300 | `golden-files/acctdata.json` |
| `carddata.txt` | `CVACT02Y.cpy` | 150 | `golden-files/carddata.json` |
| `custdata.txt` | `CVCUS01Y.cpy` | 500 | `golden-files/custdata.json` |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 | `golden-files/cardxref.json` |
| `dailytran.txt` | `CVTRA06Y.cpy` | 350 | `golden-files/dailytran.json` |
| `trantype.txt` | `CVTRA03Y.cpy` | 60 | `golden-files/trantype.json` |
| `trancatg.txt` | `CVTRA04Y.cpy` | 60 | `golden-files/trancatg.json` |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 | `golden-files/tcatbal.json` |
| `discgrp.txt` | `CVTRA02Y.cpy` | 50 | `golden-files/discgrp.json` |

### Pass/Fail Criteria

- **PASS**: All fields match within tolerance; record counts are identical.
- **FAIL**: Any field outside tolerance, missing records, or extra records.

---

## 2. Differential Testing

### Purpose

Run the **same logical operation** in both COBOL and Java, then compare outputs
field-by-field. This catches semantic drift that golden-file tests alone cannot
detect (e.g., when the golden file itself must be regenerated after a legitimate
business-rule change).

### Approach

| Step | Detail |
|------|--------|
| **Dual execution** | For each batch job (POSTTRAN, INTCALC, CREASTMT, etc.), run both the COBOL program and its Java equivalent against the same input dataset. |
| **Capture outputs** | Serialize both outputs to normalized JSON using the copybook parser (COBOL side) and the Java application's own serializer (Java side). |
| **Structural diff** | Use `test-harness/comparator.py` to produce a field-level diff report showing added, removed, and changed values. |
| **Classification** | Differences are classified as: *breaking* (logic error), *cosmetic* (whitespace/padding), or *expected* (intentional migration change). |

### Key Batch Jobs for Differential Testing

| JCL Job | COBOL Program | Java Equivalent | What It Tests |
|---------|---------------|-----------------|---------------|
| `POSTTRAN.jcl` | `CBTRN02C` | Transaction posting service | Debit/credit posting, balance updates |
| `INTCALC.jcl` | `CBACT04C` | Interest calculation service | Interest accrual per disclosure group |
| `CREASTMT.JCL` | `CBSTM03A/B` | Statement generation service | Statement line items, totals |
| `TRANREPT.jcl` | `CBTRN03C` | Transaction report service | Report layout, page/account/grand totals |
| `COMBTRAN.jcl` | Combine transactions | Transaction merge service | Daily-to-master merge, dedup |
| `TRANBKP.jcl` | Transaction backup | Backup service | Full data fidelity after backup/restore |

### Pass/Fail Criteria

- **PASS**: Zero *breaking* differences; all *cosmetic* and *expected* diffs documented.
- **FAIL**: Any *breaking* difference.

---

## 3. Reconciliation Testing

### Purpose

Validate **cross-entity data integrity invariants** that the legacy system
maintains. These checks ensure that referential relationships, running balances,
and aggregate totals are preserved after migration.

### Approach

| Step | Detail |
|------|--------|
| **Define invariants** | Document every cross-file relationship (see `RECONCILIATION_CHECKS.md`). |
| **Load migrated data** | Parse the Java-produced data (or database exports) into structured form. |
| **Execute checks** | Run `test-harness/reconciliation.py` which validates each invariant. |
| **Report** | Produce a pass/fail summary with details on any broken invariant. |

### Core Invariants

| ID | Invariant | Entities |
|----|-----------|----------|
| R-01 | Every card references an existing account | `carddata` -> `acctdata` |
| R-02 | Every cross-reference links a valid card, customer, and account | `cardxref` -> `carddata`, `custdata`, `acctdata` |
| R-03 | Every daily transaction references a valid card number | `dailytran` -> `carddata` |
| R-04 | Transaction category balance records reference valid accounts | `tcatbal` -> `acctdata` |
| R-05 | Disclosure group transaction types are valid and account groups reference existing disclosure groups | `discgrp` -> `trantype`, `acctdata` -> `discgrp` |
| R-06 | Transaction category codes exist in the category master | `dailytran` -> `trancatg` |
| R-07 | Transaction type codes exist in the type master | `dailytran` -> `trantype` |
| R-08 | Record counts are preserved across migration | all files |
| R-09 | Account active status is consistent (active accounts have valid dates) | `acctdata` |
| R-10 | Customer-card relationship is symmetric via cross-reference | `custdata` <-> `cardxref` <-> `carddata` |

### Pass/Fail Criteria

- **PASS**: All invariants hold; record counts match; no orphan references.
- **FAIL**: Any broken invariant.

---

## 4. Contract Testing

### Purpose

Ensure that the **external interfaces** of the migrated Java application match
the behavioral contracts of the original COBOL/CICS transactions. This is
especially important for online transactions that will be exposed as REST APIs.

### Approach

| Step | Detail |
|------|--------|
| **Define contracts** | For each CICS transaction, document request/response schemas, field mappings, and error codes. |
| **Generate test cases** | Derive positive and negative test cases from BMS map definitions and COBOL program logic. |
| **Execute against Java API** | Send HTTP requests to the migrated Java REST endpoints. |
| **Validate responses** | Assert response schema, field values, status codes, and error messages match the contract. |

### CICS Transaction to REST API Contract Map

| Transaction | COBOL Program | Expected REST Endpoint | Method | Key Fields |
|-------------|---------------|------------------------|--------|------------|
| CC00 (Signon) | `COSGN00C` | `POST /api/auth/login` | POST | userId, password, userType |
| CM00 (Main Menu) | `COMEN01C` | `GET /api/menu` | GET | menuItems, userRole |
| Account View | `COACTVWC` | `GET /api/accounts/{id}` | GET | acctId, balance, creditLimit, status |
| Account Update | `COACTUPC` | `PUT /api/accounts/{id}` | PUT | acctId, updatedFields |
| Card List | `COCRDLIC` | `GET /api/cards?acctId={id}` | GET | cards[], pagination |
| Card View | `COCRDSLC` | `GET /api/cards/{num}` | GET | cardNum, embossedName, expDate, status |
| Card Update | `COCRDUPC` | `PUT /api/cards/{num}` | PUT | cardNum, updatedFields |
| Transaction List | `COTRN00C` | `GET /api/transactions` | GET | transactions[], filters, pagination |
| Transaction View | `COTRN01C` | `GET /api/transactions/{id}` | GET | tranId, amount, merchant, timestamps |
| Transaction Add | `COTRN02C` | `POST /api/transactions` | POST | cardNum, amount, merchantInfo, type |
| Bill Payment | `COBIL00C` | `POST /api/payments` | POST | acctId, amount, paymentMethod |
| Reports | `CORPT00C` | `GET /api/reports/transactions` | GET | dateRange, format |

### Error Contract Validation

| Scenario | Expected Behavior |
|----------|-------------------|
| Invalid credentials | 401 with error message matching COBOL CSMSG01Y patterns |
| Account not found | 404 with structured error |
| Insufficient credit | 400 with business rule violation |
| Invalid card number | 400 with validation error |
| Duplicate transaction | 409 with conflict details |

### Pass/Fail Criteria

- **PASS**: All contract tests pass; response schemas validate; error codes match.
- **FAIL**: Any schema mismatch, wrong status code, or missing field.

---

## Test Execution Order

For a complete migration validation cycle, execute tests in this order:

```
1. Golden-File Tests     (data parsing fidelity)
2. Reconciliation Tests  (cross-entity integrity)
3. Differential Tests    (COBOL vs Java equivalence)
4. Contract Tests        (API interface compatibility)
```

## Directory Structure

```
TEST_STRATEGY.md              # This document
RECONCILIATION_CHECKS.md      # Per-job validation specifications
golden-files/                 # JSON golden reference files
  acctdata.json
  carddata.json
  custdata.json
  cardxref.json
  dailytran.json
  trantype.json
  trancatg.json
  tcatbal.json
  discgrp.json
test-harness/
  copybook_parser.py          # Fixed-width parser driven by copybook layouts
  comparator.py               # Field-level diff and comparison utilities
  reconciliation.py           # Cross-entity reconciliation checks
  generate_golden_files.py    # Script to regenerate golden files from ASCII data
  conftest.py                 # Shared pytest fixtures
  test_golden_files.py        # Golden-file test suite
  test_reconciliation.py      # Reconciliation test suite
  requirements.txt            # Python dependencies
  README.md                   # Test harness usage guide
```
