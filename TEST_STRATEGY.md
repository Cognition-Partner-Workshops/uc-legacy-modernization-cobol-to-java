# CardDemo Migration Test Strategy

> **Generated**: 2026-03-25 | **Scope**: Testing framework for COBOL-to-Java migration of CardDemo

## Executive Summary

This document defines a four-dimensional testing strategy for validating the CardDemo COBOL-to-Java migration. Every migrated module must pass all four dimensions before cutover.

| Dimension | Purpose | When Applied | Automation Level |
|---|---|---|---|
| **Golden-File** | Verify Java output exactly matches known-good COBOL output | Per-module, pre-merge | Fully automated |
| **Differential** | Compare live COBOL and Java systems processing identical inputs | Parallel-run periods | Fully automated |
| **Reconciliation** | Validate data integrity across VSAM ↔ PostgreSQL during sync | Continuous during migration | Fully automated |
| **Contract** | Verify API contracts between new microservices | Per-service, pre-merge | Fully automated |

---

## Dimension 1: Golden-File Testing

### Concept

Golden-file testing compares Java program output against pre-captured, known-correct COBOL output. The `golden-files/` directory contains structured JSON representations of every ASCII data file parsed using copybook layouts.

### Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  ASCII Data File │────>│  Copybook Parser │────>│  Golden JSON    │
│  (COBOL output)  │     │  (test-harness/) │     │  (golden-files/)│
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
┌─────────────────┐     ┌──────────────────┐              │
│  Test Input     │────>│  Java Service    │────>│ Compare │
│  (same data)     │     │  (under test)    │     └────┬───┘
└─────────────────┘     └──────────────────┘          │
                                                 ┌────▼───────┐
                                                 │ PASS/FAIL  │
                                                 └────────────┘
```

### Golden File Inventory

| File | Copybook | Record Length | Records | Entity | Golden File |
|---|---|---|---|---|---|
| `acctdata.txt` | CVACT01Y | 300 | 50 | Account | `golden-files/acctdata.json` |
| `carddata.txt` | CVACT02Y | 150 | 50 | Card | `golden-files/carddata.json` |
| `cardxref.txt` | CVACT03Y | 50 | 50 | Card Cross-Reference | `golden-files/cardxref.json` |
| `custdata.txt` | CVCUS01Y | 500 | 50 | Customer | `golden-files/custdata.json` |
| `dailytran.txt` | CVTRA06Y | 350 | 300 | Daily Transaction | `golden-files/dailytran.json` |
| `discgrp.txt` | CVTRA02Y | 50 | 51 | Disclosure Group | `golden-files/discgrp.json` |
| `tcatbal.txt` | CVTRA01Y | 50 | 50 | Trans Category Balance | `golden-files/tcatbal.json` |
| `trancatg.txt` | CVTRA04Y | 60 | 18 | Transaction Category | `golden-files/trancatg.json` |
| `trantype.txt` | CVTRA03Y | 60 | 7 | Transaction Type | `golden-files/trantype.json` |

### Golden-File Test Process

1. **Parse**: Read ASCII data file using copybook layout definitions in `test-harness/copybook_parser.py`
2. **Normalize**: Convert COBOL-encoded values (signed overpunch, packed decimal, zoned decimal) to standard types
3. **Serialize**: Output as structured JSON with field names from copybook
4. **Compare**: When Java service produces output, compare field-by-field against golden JSON
5. **Report**: Flag any field-level differences with expected vs. actual values

### Comparison Rules

| Data Type | COBOL Format | Golden File Format | Comparison Rule |
|---|---|---|---|
| Signed Decimal | `PIC S9(10)V99` with overpunch | `"12345.67"` (string) | Compare as `BigDecimal` with exact scale |
| Unsigned Integer | `PIC 9(11)` | `11` (integer) | Exact integer match |
| Alphanumeric | `PIC X(50)` | `"trimmed string"` | Trailing-space-trimmed comparison |
| Date | `PIC X(10)` | `"2024-11-20"` | ISO-8601 string comparison |
| Filler | `FILLER PIC X(n)` | (omitted) | Not compared |

### Test Execution

```bash
# Parse all data files to golden JSON (one-time setup)
python3 test-harness/generate_golden_files.py

# Compare Java output against golden files
python3 test-harness/golden_file_comparator.py \
  --expected golden-files/acctdata.json \
  --actual /path/to/java/output/accounts.json \
  --entity account
```

### Pass Criteria

- **Financial fields** (`*-BAL`, `*-AMT`, `*-CREDIT-LIMIT`, `*-INT-RATE`): Exact match to 2 decimal places
- **Identifier fields** (`*-ID`, `*-NUM`, `*-CD`): Exact match
- **Text fields** (`*-NAME`, `*-DESC`, `*-ADDR-*`): Match after trimming trailing spaces
- **Date fields**: ISO-8601 format match
- **Record count**: Exact match (zero records lost or duplicated)

---

## Dimension 2: Differential Testing

### Concept

During parallel-run periods, both COBOL and Java systems process the **same input** simultaneously. A differential comparator detects any output divergence in real time.

### Architecture

```
                    ┌──────────────┐
  Input ───────────>│   COBOL      │──── Output A ────┐
  (CICS/Batch)      │   (Legacy)   │                  │
                    └──────────────┘                  ▼
                                              ┌──────────────┐
                                              │ Differential │──── Report
                                              │  Comparator  │
                    ┌──────────────┐          └──────────────┘
  Same Input ──────>│   Java       │──── Output B ────┘
  (REST/Batch)      │   (New)      │
                    └──────────────┘
```

### Differential Test Matrix

| Phase | Module | Input Source | COBOL Output | Java Output | Comparison Point |
|---|---|---|---|---|---|
| 1 | Authentication | Login credentials | COMMAREA (user record) | JWT + user JSON | User ID, type, name fields |
| 3 | Card List | Account ID | BMS screen fields | REST JSON response | Card records, count, sort order |
| 3 | Card Detail | Card number | BMS screen fields | REST JSON response | All card fields |
| 4 | Transaction List | Card number + date range | BMS screen fields | REST JSON response | Transaction records, amounts, dates |
| 4 | Transaction Report | Report parameters | Print spool (fixed-width) | PDF/CSV | Grand totals, subtotals, record counts |
| 5 | Card Update | Card fields | Updated VSAM record | Updated DB row | All modified fields + audit trail |
| 6 | Transaction Add | Transaction fields | New VSAM record | New DB row | Transaction ID, amount, timestamps |
| 6 | Bill Payment | Payment amount | Updated balances | Updated DB balances | Account balance, payment record |
| 7 | Account Update | Account fields | Updated VSAM record | Updated DB row | All 17 validation rules |
| 8 | Batch Posting | Daily transaction file | Posted SYSTRAN records | Posted DB rows | Every posted transaction + category balances |
| 8 | Interest Calc | Account balances | Updated balances + interest | Updated DB balances | Interest amounts to 2 decimal places |
| 8 | Statement Gen | Account + transactions | Print spool | PDF | Statement totals, transaction list |

### Differential Comparison Modes

| Mode | Use Case | Implementation |
|---|---|---|
| **Synchronous** | Online transactions (CICS programs) | API Gateway sends request to both, compares responses before returning Java response |
| **Asynchronous** | Batch jobs | Run both, compare output files/tables after completion |
| **Shadow** | High-risk modules (Phase 7-8) | Java processes but result is discarded; COBOL result is authoritative |

### Divergence Handling

```python
# Severity classification for differential test divergences
DIVERGENCE_SEVERITY = {
    "financial_amount":  "CRITICAL",   # Any difference in monetary values
    "record_count":      "CRITICAL",   # Missing or extra records
    "identifier":        "HIGH",       # Wrong account/card/transaction ID
    "date_timestamp":    "MEDIUM",     # Timestamp differences (processing time)
    "text_formatting":   "LOW",        # Trailing spaces, case differences
    "sort_order":        "LOW",        # Different but valid ordering
}
```

### Pass Criteria

- **Zero** CRITICAL divergences for 30 consecutive days before cutover
- **Zero** HIGH divergences for 14 consecutive days before cutover
- MEDIUM/LOW divergences documented and accepted by product owner

---

## Dimension 3: Reconciliation Testing

### Concept

Reconciliation validates data consistency between VSAM files and PostgreSQL during the dual-run period. See `RECONCILIATION_CHECKS.md` for per-job specifications.

### Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  VSAM Files  │────>│ Reconciler   │<────│  PostgreSQL  │
│  (Source)     │     │  Engine      │     │  (Target)    │
└──────────────┘     └──────┬───────┘     └──────────────┘
                            │
                     ┌──────▼───────┐
                     │ Recon Report │
                     │ - Counts     │
                     │ - Checksums  │
                     │ - Balances   │
                     │ - Missing    │
                     └──────────────┘
```

### Reconciliation Dimensions

| Check Type | What It Validates | Frequency | Tolerance |
|---|---|---|---|
| **Record Count** | Same number of records in VSAM and PostgreSQL | Every sync cycle | Zero tolerance |
| **Checksum** | Byte-level data integrity for critical fields | Nightly | Zero tolerance |
| **Balance** | Sum of all monetary fields matches | Nightly | Zero tolerance (exact to penny) |
| **Referential Integrity** | Every CARDXREF card exists in CARDDATA, every CARDXREF account exists in ACCTDATA | Nightly | Zero tolerance |
| **Orphan Detection** | No records in PostgreSQL without VSAM counterpart (and vice versa) | Nightly | Zero tolerance |
| **Cross-Entity** | Batch pipeline outputs (category balances, statements) match | After batch cycle | Zero tolerance for amounts |

### Reconciliation Implementation

```bash
# Run all reconciliation checks
python3 test-harness/reconciliation_runner.py \
  --vsam-dir app/data/ASCII/ \
  --db-connection postgresql://localhost/carddemo \
  --report-dir reports/recon/

# Run a specific check
python3 test-harness/reconciliation_runner.py \
  --check account_balance \
  --vsam-file app/data/ASCII/acctdata.txt \
  --db-table accounts
```

### Alert Thresholds

| Metric | Warning | Critical | Action |
|---|---|---|---|
| Record count delta | > 0 | > 0 | Block sync, investigate |
| Balance mismatch | > $0.00 | > $0.00 | Block cutover, investigate |
| Sync lag | > 5 min | > 15 min | Alert team, check CDC pipeline |
| Orphan records | > 0 | > 10 | Investigate source, repair |

---

## Dimension 4: Contract Testing

### Concept

Contract tests verify that each Java microservice's API adheres to its published contract. This prevents breaking changes when services are developed and deployed independently.

### Service Contract Inventory

| Service | Consumers | Contract Format | Key Contracts |
|---|---|---|---|
| `identity-service` | All services, API Gateway | OpenAPI 3.0 | POST /auth/login, GET /users/{id} |
| `account-service` | card-service, transaction-service, payment-service | OpenAPI 3.0 | GET /accounts/{id}, PUT /accounts/{id} |
| `card-service` | transaction-service, payment-service, batch-service | OpenAPI 3.0 | GET /cards, GET /cards/{number}, PUT /cards/{number} |
| `transaction-service` | reporting-service, batch-service | OpenAPI 3.0 | GET /transactions, POST /transactions |
| `payment-service` | account-service (via saga) | OpenAPI 3.0 + AsyncAPI | POST /payments |
| `batch-service` | N/A (scheduled) | Spring Batch job contracts | Job parameters, exit codes |
| `reporting-service` | N/A (consumer-only) | OpenAPI 3.0 | GET /reports/transactions |
| `reference-data-service` | All services | OpenAPI 3.0 | GET /transaction-types, GET /categories |

### Contract Test Types

| Type | Description | Tool |
|---|---|---|
| **Provider** | Service verifies it satisfies all consumer expectations | Pact / Spring Cloud Contract |
| **Consumer** | Consumer verifies its expectations match provider contract | Pact / Spring Cloud Contract |
| **Schema** | API response matches OpenAPI schema | Spectral / Swagger Validator |
| **Backward Compatibility** | New API version doesn't break existing consumers | OpenAPI diff |

### Contract Test Examples

#### Account Service Provider Contract

```yaml
# account-service-contract.yaml
provider: account-service
consumer: card-service
interactions:
  - description: "Get account by ID"
    request:
      method: GET
      path: /accounts/00000000001
    response:
      status: 200
      body:
        acct_id: 1
        active_status: "Y"
        curr_bal: "19400.00"
        credit_limit: "202000.00"
        cash_credit_limit: "102000.00"
      matchingRules:
        curr_bal: { match: "decimal" }
        credit_limit: { match: "decimal" }
```

#### Cross-Reference Lookup Contract

```yaml
# card-service-xref-contract.yaml
provider: card-service
consumer: transaction-service
interactions:
  - description: "Lookup account for card"
    request:
      method: GET
      path: /cards/0500024453765740/account
    response:
      status: 200
      body:
        card_num: "0500024453765740"
        cust_id: 50
        acct_id: 50
```

### Contract Test Pipeline

```
  Developer Push
       │
       ▼
  ┌────────────┐     ┌────────────┐     ┌────────────┐
  │ Unit Tests │────>│ Contract   │────>│ Integration│
  │            │     │ Tests      │     │ Tests      │
  └────────────┘     └────────────┘     └────────────┘
       │                   │                   │
       │              Provider +           End-to-End
       │              Consumer             Flows
       │              Verified
       ▼
  ┌────────────┐
  │ Golden-File│
  │ Tests      │
  └────────────┘
```

### Pass Criteria

- All provider contract tests pass before deployment
- All consumer contract tests pass before deployment
- No breaking schema changes (checked by CI/CD)
- Backward compatibility maintained for at least 1 version

---

## Test Execution Order Per Phase

Each migration phase follows this test execution order:

```
1. Contract Tests (pre-deployment)
   └─ Verify API schemas and consumer expectations
        │
2. Golden-File Tests (post-implementation)
   └─ Compare Java output against golden JSON references
        │
3. Differential Tests (parallel-run)
   └─ Route traffic to both systems, compare outputs
        │
4. Reconciliation Checks (continuous)
   └─ Validate VSAM ↔ PostgreSQL data integrity
        │
5. Cutover Decision
   └─ All 4 dimensions passing → approve cutover
```

### Phase-Specific Test Requirements

| Phase | Golden-File | Differential | Reconciliation | Contract |
|---|---|---|---|---|
| **1: Identity** | USRSEC records | Login responses | User record counts | identity-service |
| **2: Navigation** | N/A (UI only) | Screen navigation | N/A | N/A |
| **3: Card Reads** | carddata, cardxref | Card list/detail | Card + xref counts | card-service |
| **4: Txn Reads** | dailytran | Transaction list/detail | Transaction counts | transaction-service, reporting-service |
| **5: Card Writes** | carddata (updated) | Card update | Card data sync | card-service (write) |
| **6: Txn Add + Payment** | dailytran (new records) | Transaction add, payment | Balance reconciliation | transaction-service, payment-service |
| **7: Account Mgmt** | acctdata (updated) | Account CRUD | Account data sync | account-service |
| **8: Batch Pipeline** | All batch outputs | Full batch cycle | All entity balances | batch-service |

---

## Test Infrastructure

### Required Components

| Component | Purpose | Technology |
|---|---|---|
| Golden File Generator | Parse COBOL data → JSON | Python (`test-harness/generate_golden_files.py`) |
| Copybook Parser | Interpret COBOL PIC clauses | Python (`test-harness/copybook_parser.py`) |
| Golden File Comparator | Compare expected vs actual | Python (`test-harness/golden_file_comparator.py`) |
| Reconciliation Runner | Cross-system data checks | Python (`test-harness/reconciliation_runner.py`) |
| Differential Comparator | Compare COBOL vs Java output | Python (`test-harness/differential_comparator.py`) |
| Contract Test Suite | API contract verification | Spring Cloud Contract / Pact |
| CI/CD Integration | Automated test execution | GitHub Actions / Jenkins |

### Test Data Management

- **Golden files** (`golden-files/`): Committed to repository, versioned with code
- **Test inputs**: Use `app/data/ASCII/` files as canonical test data
- **Synthetic data**: Generated by `test-harness/generate_golden_files.py` for edge cases
- **Production-like data**: Sanitized production snapshots for load/performance testing (not committed)

---

## Acceptance Criteria Summary

| Gate | Criteria | Required For |
|---|---|---|
| **Unit** | 80%+ code coverage, all tests pass | PR merge |
| **Golden-File** | Zero field-level differences | PR merge |
| **Contract** | All provider + consumer tests pass | PR merge |
| **Differential** | Zero CRITICAL/HIGH divergences for 14-30 days | Cutover approval |
| **Reconciliation** | Zero count/balance mismatches | Cutover approval |
| **Performance** | Response times within 120% of COBOL baseline | Cutover approval |
