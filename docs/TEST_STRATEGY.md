# CardDemo Migration Test Strategy

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Define the four testing dimensions for validating COBOL-to-Java migration correctness.

---

## Table of Contents

1. [Testing Principles](#1-testing-principles)
2. [Dimension 1 — Golden-File Testing](#2-dimension-1--golden-file-testing)
3. [Dimension 2 — Differential Testing](#3-dimension-2--differential-testing)
4. [Dimension 3 — Reconciliation Testing](#4-dimension-3--reconciliation-testing)
5. [Dimension 4 — Contract Testing](#5-dimension-4--contract-testing)
6. [Test Data Management](#6-test-data-management)
7. [Test Harness Architecture](#7-test-harness-architecture)
8. [Coverage Matrix](#8-coverage-matrix)

---

## 1. Testing Principles

| Principle                          | Description                                                                                           |
|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| **Legacy is the oracle**          | The existing COBOL system defines correct behavior. Any deviation in the Java system is a defect until proven otherwise. |
| **Byte-level fidelity for data**  | Numeric fields must match to the exact decimal place. Rounding differences are bugs, not acceptable variance. |
| **Test at every layer**           | Unit tests for business logic, integration tests for data access, end-to-end tests for complete workflows. |
| **Automate everything**           | Every test must be executable without manual intervention. No "check the screen" validation.          |
| **Fail fast, fail loud**          | Tests produce structured output (JSON diffs, reconciliation reports) that clearly identify which field in which record diverged. |

---

## 2. Dimension 1 — Golden-File Testing

### Purpose

Validate that the Java system produces **identical output** for known inputs by comparing against pre-captured reference data ("golden files").

### How It Works

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  ASCII Data      │────▶│  Copybook-Aware  │────▶│  Golden JSON    │
│  Files           │     │  Parser          │     │  Files          │
│  (app/data/      │     │  (test-harness/  │     │  (golden-files/ │
│   ASCII/)        │     │   parser.py)     │     │   *.json)       │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                          │
                                                          │ compare
                                                          ▼
                                              ┌─────────────────┐
                                              │  Java System     │
                                              │  Output (API     │
                                              │  responses or    │
                                              │  DB queries)     │
                                              └─────────────────┘
```

### Golden Files Produced

| Golden File                | Source Data File      | Copybook Layout | Records | Purpose                                |
|---------------------------|-----------------------|-----------------|---------|----------------------------------------|
| `acctdata.json`           | acctdata.txt          | CVACT01Y (300B) | 50      | Account master records                 |
| `carddata.json`           | carddata.txt          | CVACT02Y (150B) | 50      | Card records                           |
| `cardxref.json`           | cardxref.txt          | CVACT03Y (50B)  | 50      | Card-to-account cross-references       |
| `custdata.json`           | custdata.txt          | CVCUS01Y (500B) | 50      | Customer master records                |
| `dailytran.json`          | dailytran.txt         | CVTRA06Y (350B) | 300     | Daily transaction staging              |
| `discgrp.json`            | discgrp.txt           | CVTRA02Y (50B)  | 51      | Disclosure group interest rates        |
| `tcatbal.json`            | tcatbal.txt           | CVTRA01Y (50B)  | 50      | Transaction category balances          |
| `trancatg.json`           | trancatg.txt          | CVTRA04Y (60B)  | 18      | Transaction category definitions       |
| `trantype.json`           | trantype.txt          | CVTRA03Y (60B)  | 7       | Transaction type definitions           |

### Usage in Testing

```python
# Example: Verify Java Account entity matches golden file
import json

with open("golden-files/acctdata.json") as f:
    golden = json.load(f)

for record in golden["records"]:
    java_account = api_client.get(f"/api/accounts/{record['ACCT_ID']}")
    assert java_account["currentBalance"] == record["ACCT_CURR_BAL"]
    assert java_account["creditLimit"] == record["ACCT_CREDIT_LIMIT"]
    assert java_account["status"] == record["ACCT_ACTIVE_STATUS"]
```

### When to Use

- **Phase 2 (Read-Only Services):** Verify every API response matches golden file data
- **Phase 3 (Write Services):** Verify POST/PUT operations produce records matching expected golden output
- **Phase 4 (Batch):** Verify batch job output files match golden reference outputs

---

## 3. Dimension 2 — Differential Testing

### Purpose

Run the **same input** through both legacy COBOL and modern Java systems simultaneously, then compare outputs field-by-field. Catches behavioral differences that golden files alone might miss (e.g., edge cases in date handling, rounding).

### How It Works

```
                    ┌──────────────────┐
                    │   Test Input     │
                    │   (known data)   │
                    └────────┬─────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
┌──────────────────┐           ┌──────────────────┐
│  Legacy COBOL    │           │  Modern Java     │
│  System          │           │  System          │
│  (mainframe or   │           │  (Spring Boot)   │
│   GnuCOBOL)     │           │                  │
└────────┬─────────┘           └────────┬─────────┘
         │                              │
         ▼                              ▼
┌──────────────────┐           ┌──────────────────┐
│  Legacy Output   │           │  Modern Output   │
│  (VSAM records   │           │  (DB records     │
│   or flat files) │           │   or API JSON)   │
└────────┬─────────┘           └────────┬─────────┘
         │                              │
         └──────────────┬───────────────┘
                        │
                        ▼
              ┌──────────────────┐
              │  Differential    │
              │  Comparator      │
              │  (test-harness/  │
              │   comparator.py) │
              └────────┬─────────┘
                       │
                       ▼
              ┌──────────────────┐
              │  Diff Report     │
              │  (field-level    │
              │   differences)   │
              └──────────────────┘
```

### Comparison Rules

| Field Type         | Comparison Method                                                   |
|-------------------|---------------------------------------------------------------------|
| Alphanumeric (X)  | Exact string match (trailing spaces trimmed)                        |
| Numeric (9)       | Exact integer match                                                 |
| Signed Decimal (S9V99) | Match to 2 decimal places using `Decimal` comparison           |
| Date (X(10))      | Parse both as ISO date, compare as dates                            |
| Timestamp (X(26)) | Parse both, compare with 1-second tolerance                         |

### Differential Test Categories

| Category                 | Input                                    | Legacy Path                    | Modern Path                    |
|-------------------------|------------------------------------------|--------------------------------|--------------------------------|
| Account View            | Account ID from golden data              | COACTVWC BMS output            | GET /api/accounts/{id}         |
| Card List               | Account ID                               | COCRDLIC BMS output            | GET /api/cards?accountId={id}  |
| Transaction Add         | New transaction JSON                     | COTRN02C via CICS              | POST /api/transactions         |
| Account Update          | Modified account fields                  | COACTUPC via CICS              | PUT /api/accounts/{id}         |
| Transaction Posting     | Daily transaction file                   | CBTRN02C batch + JCL           | TransactionPostingJob          |
| Interest Calculation    | Account + disclosure group data          | CBACT04C batch + JCL           | InterestCalculationJob         |
| Statement Generation    | Transaction + account + customer data    | CBSTM03A/B batch + JCL        | StatementGenerationJob         |

### When to Use

- **Phase 3 (Write Services):** Shadow-write mode — run both systems, compare before committing modern writes
- **Phase 4 (Batch):** Parallel batch runs — both JCL and Spring Batch execute nightly, outputs compared

---

## 4. Dimension 3 — Reconciliation Testing

### Purpose

Verify **aggregate data integrity** across systems — record counts, balance totals, cross-reference integrity. Catches systematic issues (e.g., dropped records, double-counting) that per-record tests might miss.

### Reconciliation Checks

| Check ID | Check Name                           | Query / Verification                                              | Threshold |
|----------|--------------------------------------|-------------------------------------------------------------------|-----------|
| RC-001   | Account Record Count                 | DB `COUNT(*)` = VSAM ACCTDATA record count                       | 100%      |
| RC-002   | Account Balance Sum                  | `SUM(current_balance)` matches across systems                     | Exact     |
| RC-003   | Card Record Count                    | DB `COUNT(*)` = VSAM CARDDATA record count                       | 100%      |
| RC-004   | Card-Account Referential Integrity   | Every card → valid account (no orphans)                           | 100%      |
| RC-005   | Customer Record Count                | DB `COUNT(*)` = VSAM CUSTDATA record count                       | 100%      |
| RC-006   | Transaction Record Count             | DB `COUNT(*)` = VSAM TRANSACT record count                       | 100%      |
| RC-007   | Transaction Amount Sum               | `SUM(transaction_amount)` matches across systems                  | Exact     |
| RC-008   | Category Balance Consistency         | Sum of transactions per category = category balance record        | Exact     |
| RC-009   | Cross-Reference Completeness         | Every CARDXREF entry links to existing card AND account           | 100%      |
| RC-010   | Daily Transaction Posting Count      | DALYTRAN records processed = TRANSACT records created             | 100%      |
| RC-011   | Interest Calculation Balance Impact  | Pre-batch balance + interest = post-batch balance (per account)   | Exact     |
| RC-012   | Statement Record Coverage            | Every active account has a statement generated                    | 100%      |

See [RECONCILIATION_CHECKS.md](./RECONCILIATION_CHECKS.md) for detailed per-job specifications.

### Reconciliation Schedule

| Phase | Frequency                    | Checks Run                          |
|-------|------------------------------|-------------------------------------|
| 2     | After each data migration    | RC-001 through RC-009               |
| 3     | Every 5 minutes (continuous) | RC-001, RC-003, RC-006, RC-007      |
| 4     | After each batch run         | RC-010, RC-011, RC-012 + all others |
| 5     | Daily for 14 days            | All checks                          |

---

## 5. Dimension 4 — Contract Testing

### Purpose

Verify that the Java API endpoints conform to their **API contract** — correct HTTP status codes, response schemas, field names, data types, and pagination behavior. Ensures the modern system is not just data-accurate but also API-correct.

### Contract Test Specifications

| Endpoint                           | Method | Contract Checks                                                    |
|-----------------------------------|--------|---------------------------------------------------------------------|
| `GET /api/accounts/{id}`          | GET    | 200 + account JSON schema; 404 for unknown ID; all golden fields present |
| `PUT /api/accounts/{id}`          | PUT    | 200 on valid update; 400 on invalid field; 404 on unknown; idempotent |
| `GET /api/cards?accountId={id}`   | GET    | 200 + paginated array; page/size/total fields; card JSON schema     |
| `GET /api/cards/{cardNumber}`     | GET    | 200 + card JSON; 404 for unknown; PAN masking in response          |
| `PUT /api/cards/{cardNumber}`     | PUT    | 200 on valid; 400 on invalid CVV/expiry; PCI field handling        |
| `GET /api/transactions`           | GET    | 200 + paginated; sort by date descending; filter by account        |
| `GET /api/transactions/{id}`      | GET    | 200 + transaction JSON; 404 for unknown                            |
| `POST /api/transactions`          | POST   | 201 on success; 400 on invalid; 409 on duplicate (idempotency key) |
| `POST /api/payments`              | POST   | 201 on success; 400 on invalid amount; 409 on duplicate; audit trail |
| `POST /api/auth/login`            | POST   | 200 + JWT on success; 401 on bad credentials; 429 on rate limit    |
| `GET /api/users`                  | GET    | 200 + paginated user list (admin only); 403 for non-admin          |
| `POST /api/reports/generate`      | POST   | 202 (accepted) + job ID; async generation                          |

### Schema Validation

Each golden file defines the expected JSON schema. Contract tests validate:

1. **Field presence:** All fields from the copybook layout are present in the API response
2. **Field types:** Numeric fields are numbers (not strings), dates are ISO 8601
3. **Field names:** COBOL field names map to camelCase Java names consistently
4. **Null handling:** FILLER bytes are not exposed; empty COBOL fields map to `null` or empty string

### COBOL-to-Java Field Name Mapping Convention

| COBOL Pattern        | Java Convention                | Example                              |
|---------------------|--------------------------------|--------------------------------------|
| `ACCT-ID`           | `accountId`                    | `ACCT-ID` → `accountId`             |
| `ACCT-CURR-BAL`     | `currentBalance`               | Drop prefix, camelCase               |
| `CARD-NUM`          | `cardNumber`                   | Expand abbreviations                 |
| `TRAN-AMT`          | `transactionAmount`            | Full words in Java                   |
| `CUST-ADDR-LINE-1`  | `addressLine1`                 | Drop entity prefix                   |
| `CUST-SSN`          | `ssn` (masked in response)     | Security-sensitive, mask in API      |

---

## 6. Test Data Management

### Data Sources

| Source                    | Location                         | Records | Format      | Usage                          |
|--------------------------|----------------------------------|---------|-------------|--------------------------------|
| Sample ASCII data        | `app/data/ASCII/*.txt`           | 626     | Fixed-width | Golden file generation         |
| Golden JSON files        | `golden-files/*.json`            | 626     | JSON        | Test assertions                |
| Synthetic edge cases     | `test-harness/edge-cases/`       | TBD     | JSON        | Boundary testing               |

### Edge Case Categories

| Category                  | Examples                                                           |
|--------------------------|---------------------------------------------------------------------|
| **Numeric boundaries**   | Zero balance, max balance (S9(10)V99 max = 9999999999.99), negative|
| **String boundaries**    | Empty names, max-length names, special characters                  |
| **Date boundaries**      | Leap year dates, month-end, year-end, far-future expiry dates      |
| **Referential edges**    | Card with no cross-reference, account with no cards                |
| **Financial precision**  | Amounts requiring rounding: 0.005, 0.015, 99999999.995            |

---

## 7. Test Harness Architecture

```
test-harness/
├── parser.py               # Copybook-aware fixed-width file parser
├── comparator.py           # Field-level comparison engine
├── reconciliation.py       # Aggregate reconciliation checks
├── layouts.py              # Copybook layout definitions (Python dicts)
├── run_golden_tests.py     # Golden-file test runner
├── run_reconciliation.py   # Reconciliation check runner
└── README.md               # Usage instructions

golden-files/
├── acctdata.json           # 50 account records
├── carddata.json           # 50 card records
├── cardxref.json           # 50 card cross-references
├── custdata.json           # 50 customer records
├── dailytran.json          # 300 daily transaction records
├── discgrp.json            # 51 disclosure group records
├── tcatbal.json            # 50 category balance records
├── trancatg.json           # 18 transaction category records
└── trantype.json           # 7 transaction type records
```

### Tool Stack

| Tool               | Purpose                                           |
|-------------------|---------------------------------------------------|
| Python 3.x        | Parser, comparator, reconciliation scripts         |
| JSON               | Golden file format (human-readable, diffable)      |
| pytest (optional)  | Test runner for automated validation               |

---

## 8. Coverage Matrix

| Functional Area         | Golden-File | Differential | Reconciliation | Contract |
|------------------------|-------------|-------------|----------------|----------|
| Authentication          |             | ✓           |                | ✓        |
| Account View            | ✓           | ✓           | ✓              | ✓        |
| Account Update          | ✓           | ✓           | ✓              | ✓        |
| Card List               | ✓           | ✓           | ✓              | ✓        |
| Card View/Update        | ✓           | ✓           | ✓              | ✓        |
| Transaction List/View   | ✓           | ✓           | ✓              | ✓        |
| Transaction Add         | ✓           | ✓           | ✓              | ✓        |
| Bill Payment            | ✓           | ✓           | ✓              | ✓        |
| Transaction Posting     | ✓           | ✓           | ✓              |          |
| Interest Calculation    | ✓           | ✓           | ✓              |          |
| Statement Generation    | ✓           | ✓           | ✓              |          |
| Daily Reports           | ✓           | ✓           | ✓              |          |
| User Administration     |             | ✓           |                | ✓        |
| Data Export/Import      | ✓           |             | ✓              |          |
