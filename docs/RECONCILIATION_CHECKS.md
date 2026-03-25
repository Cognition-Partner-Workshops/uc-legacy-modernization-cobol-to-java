# Reconciliation Checks — Per-Job Validation Specifications

## Overview

This document defines the reconciliation checks that validate data integrity during and after the CardDemo COBOL-to-Java migration. Each check specifies what to measure, the golden baseline value, the acceptance criteria, and the batch job or functional area it validates.

Reconciliation checks run in two modes:

| Mode | Description |
|------|-------------|
| **Baseline** | Golden files only — verifies internal consistency of legacy data |
| **Comparison** | Golden vs. modern — verifies migration preserved data integrity |

---

## Check Registry

| Check ID | Name | Type | Scope | Batch Job / Area |
|----------|------|------|-------|------------------|
| RC-001 | Account Record Count | Count | acctdata | ACCTFILE, Data Migration |
| RC-002 | Account Balance Sum | Aggregate | acctdata | ACCTFILE, INTCALC |
| RC-003 | Card Record Count | Count | carddata | CARDFILE |
| RC-004 | Card-Account Referential Integrity | Referential | carddata → acctdata | CARDFILE, ACCTFILE |
| RC-005 | Customer Record Count | Count | custdata | CUSTFILE |
| RC-006 | Daily Transaction Record Count | Count | dailytran | POSTTRAN |
| RC-007 | Transaction Amount Sum | Aggregate | dailytran | POSTTRAN, COMBTRAN |
| RC-008 | Category Balance Consistency | Referential | tcatbal → acctdata | POSTTRAN, INTCALC |
| RC-009 | Cross-Reference Completeness | Referential | cardxref → carddata, acctdata | XREFFILE |

---

## Detailed Check Specifications

### RC-001: Account Record Count

**Purpose:** Verify that all account records were migrated without loss or duplication.

| Property | Value |
|----------|-------|
| Golden File | `golden-files/acctdata.json` |
| Copybook | CVACT01Y |
| Golden Baseline | 50 records |
| SQL Equivalent | `SELECT COUNT(*) FROM accounts` |
| Acceptance | Modern count = golden count (zero tolerance) |
| Batch Jobs | ACCTFILE (data refresh), initial data migration |

**Validation Logic:**
```python
golden_count = golden["metadata"]["record_count"]  # 50
modern_count = db.execute("SELECT COUNT(*) FROM accounts").scalar()
assert golden_count == modern_count
```

**Failure Indicates:**
- Records lost during VSAM → PostgreSQL migration
- Duplicate insertion from retry logic
- Filtering error in migration ETL

---

### RC-002: Account Balance Sum

**Purpose:** Verify that the sum of all account current balances is preserved exactly (financial precision check).

| Property | Value |
|----------|-------|
| Golden File | `golden-files/acctdata.json` |
| Field | `ACCT_CURR_BAL` (PIC S9(10)V99) |
| Golden Baseline | $12,269.00 |
| SQL Equivalent | `SELECT SUM(current_balance) FROM accounts` |
| Acceptance | Exact match (zero tolerance — financial data) |
| Batch Jobs | ACCTFILE, INTCALC (interest calculation modifies balances) |

**Validation Logic:**
```python
golden_sum = sum(Decimal(str(r["ACCT_CURR_BAL"])) for r in golden["records"])
modern_sum = db.execute("SELECT SUM(current_balance) FROM accounts").scalar()
assert golden_sum == modern_sum  # Exact — no tolerance for financial data
```

**Failure Indicates:**
- COBOL overpunch sign decoding error (e.g., negative balance parsed as positive)
- Implied decimal point misalignment (off by factor of 100)
- Precision loss from float instead of Decimal in Java BigDecimal mapping
- Interest calculation divergence between CBACT04C and Java equivalent

---

### RC-003: Card Record Count

**Purpose:** Verify all card records migrated.

| Property | Value |
|----------|-------|
| Golden File | `golden-files/carddata.json` |
| Copybook | CVACT02Y |
| Golden Baseline | 50 records |
| SQL Equivalent | `SELECT COUNT(*) FROM cards` |
| Acceptance | Exact match |
| Batch Jobs | CARDFILE |

**Failure Indicates:**
- Records lost during migration
- Card number deduplication error

---

### RC-004: Card-Account Referential Integrity

**Purpose:** Verify every card references a valid account (foreign key constraint equivalent).

| Property | Value |
|----------|-------|
| Golden Files | `golden-files/carddata.json`, `golden-files/acctdata.json` |
| Join Condition | `carddata.CARD_ACCT_ID ∈ acctdata.ACCT_ID` |
| Golden Baseline | 0 orphans (all 50 cards reference valid accounts) |
| SQL Equivalent | `SELECT COUNT(*) FROM cards c LEFT JOIN accounts a ON c.account_id = a.id WHERE a.id IS NULL` |
| Acceptance | Zero orphans |
| Mode | Runs in both baseline and comparison modes |

**Validation Logic:**
```python
account_ids = {r["ACCT_ID"] for r in acctdata["records"]}
orphans = [c for c in carddata["records"] if c["CARD_ACCT_ID"] not in account_ids]
assert len(orphans) == 0
```

**Failure Indicates:**
- Foreign key constraint violation in PostgreSQL schema
- Account records migrated out of order (cards before accounts)
- Data corruption during VSAM key restructuring

---

### RC-005: Customer Record Count

**Purpose:** Verify all customer master records migrated.

| Property | Value |
|----------|-------|
| Golden File | `golden-files/custdata.json` |
| Copybook | CVCUS01Y |
| Golden Baseline | 50 records |
| SQL Equivalent | `SELECT COUNT(*) FROM customers` |
| Acceptance | Exact match |
| Batch Jobs | CUSTFILE |

**Failure Indicates:**
- Customer records dropped during PII-aware migration
- SSN field masking causing record rejection

---

### RC-006: Daily Transaction Record Count

**Purpose:** Verify all daily transaction records migrated.

| Property | Value |
|----------|-------|
| Golden File | `golden-files/dailytran.json` |
| Copybook | CVTRA06Y |
| Golden Baseline | 300 records |
| SQL Equivalent | `SELECT COUNT(*) FROM daily_transactions` |
| Acceptance | Exact match |
| Batch Jobs | POSTTRAN (transaction posting) |

**Failure Indicates:**
- Transaction records lost during batch posting migration
- Duplicate filtering too aggressive
- Timestamp parsing error causing records to be rejected

---

### RC-007: Transaction Amount Sum

**Purpose:** Verify total transaction amount is preserved (critical financial reconciliation).

| Property | Value |
|----------|-------|
| Golden File | `golden-files/dailytran.json` |
| Field | `DALYTRAN_AMT` (PIC S9(09)V99) |
| Golden Baseline | $104,801.54 |
| SQL Equivalent | `SELECT SUM(amount) FROM daily_transactions` |
| Acceptance | Exact match (zero tolerance) |
| Batch Jobs | POSTTRAN, COMBTRAN (combine transactions) |

**Validation Logic:**
```python
golden_sum = sum(Decimal(str(r["DALYTRAN_AMT"])) for r in golden["records"])
modern_sum = db.execute("SELECT SUM(amount) FROM daily_transactions").scalar()
assert golden_sum == modern_sum
```

**Failure Indicates:**
- Sign encoding error (COBOL overpunch misinterpretation)
- Implied decimal misalignment
- Transaction amount truncation in Java BigDecimal mapping
- Batch combining logic (COMBTRAN) producing different totals

---

### RC-008: Category Balance Consistency

**Purpose:** Verify every category balance record references a valid account.

| Property | Value |
|----------|-------|
| Golden Files | `golden-files/tcatbal.json`, `golden-files/acctdata.json` |
| Join Condition | `tcatbal.TRANCAT_ACCT_ID ∈ acctdata.ACCT_ID` |
| Golden Baseline | 0 orphan account IDs (all 50 category balance records reference valid accounts) |
| SQL Equivalent | `SELECT COUNT(*) FROM category_balances cb LEFT JOIN accounts a ON cb.account_id = a.id WHERE a.id IS NULL` |
| Acceptance | Zero orphans |
| Mode | Runs in both baseline and comparison modes |
| Batch Jobs | POSTTRAN, INTCALC |

**Failure Indicates:**
- Category balance records migrated without corresponding accounts
- Foreign key constraint violation
- Transaction posting creating orphan category records

---

### RC-009: Cross-Reference Completeness

**Purpose:** Verify every cross-reference record points to both a valid card and a valid account.

| Property | Value |
|----------|-------|
| Golden Files | `golden-files/cardxref.json`, `golden-files/carddata.json`, `golden-files/acctdata.json` |
| Join Conditions | `cardxref.XREF_CARD_NUM ∈ carddata.CARD_NUM` AND `cardxref.XREF_ACCT_ID ∈ acctdata.ACCT_ID` |
| Golden Baseline | 0 missing card refs, 0 missing account refs |
| SQL Equivalent | Two-way join check on card_xref table |
| Acceptance | Zero missing references in both directions |
| Mode | Runs in both baseline and comparison modes |
| Batch Jobs | XREFFILE |

**Failure Indicates:**
- Cross-reference table out of sync with card/account tables
- Migration order dependency (xref loaded before cards/accounts)
- Card number format mismatch between tables

---

## Batch Job Validation Matrix

This matrix maps each batch job to the reconciliation checks that validate its output.

| Batch Job | Description | Checks | Critical? |
|-----------|-------------|--------|-----------|
| ACCTFILE | Refresh account master VSAM | RC-001, RC-002 | Yes |
| CARDFILE | Refresh card master VSAM | RC-003, RC-004 | Yes |
| CUSTFILE | Refresh customer master VSAM | RC-005 | Yes |
| XREFFILE | Load cross-reference VSAM | RC-009 | Yes |
| POSTTRAN | Core transaction posting | RC-006, RC-007, RC-008 | Yes |
| INTCALC | Interest calculations | RC-002, RC-008 | Yes |
| COMBTRAN | Combine transactions | RC-007 | Yes |
| CREASTMT | Produce statements | (output validation — not in RC scope) | No |
| TRANBKP | Backup transactions | (backup verification — not in RC scope) | No |

---

## Running the Checks

### Baseline Mode (Golden Files Only)

Verifies internal data consistency without needing the modern system:

```bash
cd test-harness
python run_reconciliation.py ../golden-files/
```

Expected output:
```
[BASELINE] RC-001: Account Record Count           (golden: 50)
[BASELINE] RC-002: Account Balance Sum             (golden: $12,269.00)
[BASELINE] RC-003: Card Record Count               (golden: 50)
[PASS    ] RC-004: Card-Account Referential Integrity (0 orphans)
[BASELINE] RC-005: Customer Record Count           (golden: 50)
[BASELINE] RC-006: Daily Transaction Record Count  (golden: 300)
[BASELINE] RC-007: Transaction Amount Sum          (golden: $104,801.54)
[PASS    ] RC-008: Category Balance Consistency    (0 orphans)
[PASS    ] RC-009: Cross-Reference Completeness    (0 missing refs)
```

### Comparison Mode (Golden vs. Modern)

After the Java migration, provide modern system counts:

```bash
cd test-harness
python run_reconciliation.py ../golden-files/ \
    --modern-counts '{"RC-001": 50, "RC-002": 12269.00, "RC-003": 50, "RC-005": 50, "RC-006": 300, "RC-007": 104801.54}'
```

---

## Success Criteria

| Criterion | Requirement |
|-----------|-------------|
| All count checks (RC-001, 003, 005, 006) | Exact match (zero tolerance) |
| All sum checks (RC-002, 007) | Exact match (zero tolerance — financial data) |
| All referential checks (RC-004, 008, 009) | Zero orphans / missing references |
| Overall | ALL checks must pass before cutover approval |

---

## Golden Baseline Summary

| File | Copybook | Records | Key Aggregate |
|------|----------|---------|---------------|
| acctdata.json | CVACT01Y | 50 | Balance sum: $12,269.00 |
| carddata.json | CVACT02Y | 50 | — |
| cardxref.json | CVACT03Y | 50 | — |
| custdata.json | CVCUS01Y | 50 | — |
| dailytran.json | CVTRA06Y | 300 | Amount sum: $104,801.54 |
| discgrp.json | CVTRA02Y | 51 | — |
| tcatbal.json | CVTRA01Y | 50 | — |
| trancatg.json | CVTRA04Y | 18 | — |
| trantype.json | CVTRA03Y | 7 | — |
