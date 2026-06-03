# Reconciliation Checks

## Overview

Per-job validation specifications for the CardDemo COBOL-to-Java migration. Each batch job has a defined set of reconciliation checks that must pass regardless of whether the job is executed by COBOL or Java.

---

## Job: POSTTRAN (Post Daily Transactions)

**JCL:** `POSTTRAN.jcl`  
**Program:** `CBTRN01C.cbl`  
**Function:** Posts daily transactions from `dailytran.txt` to account balances in `acctdata.txt`.

### Pre-conditions
- `dailytran.txt` loaded and parseable per `CVTRA06Y.cpy`
- `acctdata.txt` loaded and parseable per `CVACT01Y.cpy`
- `cardxref.txt` loaded for card→account resolution

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `account_count_preserved` | Record count of accounts after = before | Δ = 0 |
| 2 | `balance_change_equals_transactions` | Σ(ACCT-CURR-BAL after) − Σ(ACCT-CURR-BAL before) = Σ(DALYTRAN-AMT) | Tolerance ±0.01 |
| 3 | `no_overlimit_accounts` | No ACCT-CURR-BAL > ACCT-CREDIT-LIMIT after posting | Count = 0 |
| 4 | `cycle_debit_updated` | ACCT-CURR-CYC-DEBIT increases by sum of debits for each account | Per-account |
| 5 | `cycle_credit_updated` | ACCT-CURR-CYC-CREDIT increases by sum of credits for each account | Per-account |
| 6 | `transaction_cards_in_xref` | Every DALYTRAN-CARD-NUM exists in XREF-CARD-NUM | Orphans = 0 |

### Failure Actions
- If check 2 fails: investigate rounding in signed-decimal decoding
- If check 3 fails: verify credit-limit enforcement logic in Java
- If check 6 fails: data integrity issue in test input

---

## Job: DAILYTRAN_INPUT (Daily Transaction Input Validation)

**JCL:** N/A (input-stage validation)  
**Function:** Validates referential integrity of daily transaction file before processing.

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `transaction_cards_in_xref` | All DALYTRAN-CARD-NUM values exist in card cross-reference | Orphans = 0 |
| 2 | `xref_accounts_exist` | All XREF-ACCT-ID values exist in account master | Missing = 0 |
| 3 | `no_zero_amount_transactions` | DALYTRAN-AMT ≠ 0 for all records | Zeros = 0 |

### Failure Actions
- If check 1 fails: cards exist without XREF entries — data generation issue
- If check 2 fails: XREF references non-existent accounts — FK violation
- If check 3 fails: zero-amount transaction slipped through validation

---

## Job: INTCALC (Interest Calculation)

**JCL:** `INTCALC.jcl`  
**Function:** Calculates interest charges based on transaction category balances and disclosure group rates.

### Pre-conditions
- `tcatbal.txt` loaded per `CVTRA01Y.cpy`
- `discgrp.txt` loaded per `CVTRA02Y.cpy`
- `acctdata.txt` loaded per `CVACT01Y.cpy`

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `account_count_preserved` | Account count unchanged after interest posting | Δ = 0 |
| 2 | `positive_interest_rates` | All DIS-INT-RATE values ≥ 0 | Negatives = 0 |
| 3 | `category_balances_have_accounts` | All TRANCAT-ACCT-ID values exist in account master | Orphans = 0 |
| 4 | `interest_amount_non_negative` | Computed interest charges ≥ 0 | Negatives = 0 |
| 5 | `balance_increases_by_interest` | ACCT-CURR-BAL after = before + computed interest | Tolerance ±0.01 |

### Failure Actions
- If check 2 fails: disclosure group data corruption
- If check 5 fails: interest calculation formula differs between COBOL and Java

---

## Job: CREASTMT (Create Account Statements)

**JCL:** `CREASTMT.JCL`  
**Programs:** `CBSTM03A.CBL`, `CBSTM03B.CBL`  
**Function:** Generates monthly statements for all active accounts.

### Pre-conditions
- Active accounts identified by `ACCT-ACTIVE-STATUS = 'Y'`
- Customer data available via XREF linkage
- Transaction history available for statement period

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `active_accounts_have_xref` | Every active account has ≥1 XREF entry | Missing = 0 |
| 2 | `xref_customers_exist` | All XREF-CUST-ID values exist in customer master | Orphans = 0 |
| 3 | `active_accounts_have_open_date` | ACCT-OPEN-DATE not blank for active accounts | Blanks = 0 |
| 4 | `statement_count_equals_active_accounts` | Number of statements generated = active account count | Δ = 0 |
| 5 | `statement_balance_matches_account` | Statement closing balance = ACCT-CURR-BAL | Per-account |

### Failure Actions
- If check 1 fails: orphaned active accounts without card linkage
- If check 2 fails: XREF points to deleted customers
- If check 4 fails: statement generation skipped some accounts

---

## Job: TRANREPT (Transaction Report)

**JCL:** `TRANREPT.jcl`  
**Program:** `CORPT00C.cbl`  
**Function:** Generates daily transaction report with category and type descriptions.

### Pre-conditions
- `dailytran.txt` loaded
- `trancatg.txt` loaded per `CVTRA04Y.cpy` (category descriptions)
- `trantype.txt` loaded per `CVTRA03Y.cpy` (type descriptions)

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `report_line_count` | Report detail lines = transaction count | Δ = 0 |
| 2 | `all_types_resolved` | Every DALYTRAN-TYPE-CD maps to a TRAN-TYPE-DESC | Unresolved = 0 |
| 3 | `all_categories_resolved` | Every DALYTRAN-CAT-CD maps to a TRAN-CAT-TYPE-DESC | Unresolved = 0 |
| 4 | `report_total_matches_sum` | Report grand total = Σ(DALYTRAN-AMT) | Tolerance ±0.01 |

### Failure Actions
- If check 2 fails: unknown transaction type code in daily file
- If check 4 fails: report aggregation logic differs in Java

---

## Job: COMBTRAN (Combine Transactions)

**JCL:** `COMBTRAN.jcl`  
**Function:** Merges daily transactions into the master transaction file, sorted by card number and timestamp.

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `output_count` | Output records = existing master + daily transactions | Δ = 0 |
| 2 | `sort_order_valid` | Output sorted by TRAN-CARD-NUM, TRAN-ORIG-TS ascending | Violations = 0 |
| 3 | `no_duplicate_transactions` | No duplicate (TRAN-ID + TRAN-ORIG-TS) combinations | Duplicates = 0 |
| 4 | `all_daily_included` | Every DALYTRAN-ID appears in output | Missing = 0 |

### Failure Actions
- If check 2 fails: sort collation differs (EBCDIC vs ASCII ordering)
- If check 3 fails: deduplication logic missing in Java

---

## Job: CBEXPORT (Data Export / Branch Migration)

**JCL:** `CBEXPORT.jcl`  
**Program:** `CBEXPORT.cbl`  
**Function:** Exports consolidated account/customer/card data for branch migration.

### Reconciliation Checks

| # | Check | Rule | Expected |
|---|-------|------|----------|
| 1 | `export_accounts_complete` | Exported account count = source account count | Δ = 0 |
| 2 | `export_customers_complete` | Exported customer count = source customer count | Δ = 0 |
| 3 | `export_cards_complete` | Exported card count = source card count | Δ = 0 |
| 4 | `export_xrefs_valid` | All exported XREF entries resolve within export set | Orphans = 0 |

### Failure Actions
- If any count check fails: export filter logic may differ in Java
- If check 4 fails: partial export broke referential integrity

---

## Running Checks

```bash
# Run all reconciliation checks
cd test-harness
python3 run_tests.py --reconcile

# Check exit code
echo $?  # 0 = all passed, 1 = failures detected
```

## Report Format

Results are written to `golden-files/reconciliation_report.json`:

```json
[
  {
    "job_name": "DAILYTRAN_INPUT",
    "passed": true,
    "summary": "DAILYTRAN_INPUT: 3/3 checks passed",
    "checks": [
      {
        "name": "transaction_cards_in_xref",
        "description": "All transaction card numbers exist in cross-reference",
        "passed": true,
        "expected_value": 0,
        "actual_value": 0,
        "details": ""
      }
    ]
  }
]
```

## Adding New Job Checks

1. Define the reconciliation function in `test-harness/reconciliation.py`
2. Follow the pattern: load pre/post data → run checks → return `ReconciliationReport`
3. Register in `run_all_reconciliation()` 
4. Document in this file with the table format above
