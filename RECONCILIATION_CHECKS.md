# CardDemo Reconciliation Checks

Per-job validation specifications for the CardDemo batch processing cycle.
These checks are executed by `test-harness/reconciliation_runner.py` against
the post-execution data to ensure data integrity invariants hold after each
batch step.

---

## Global Checks (apply after every batch step)

| Check ID | Check Name | Rule | Severity |
|----------|-----------|------|----------|
| G-01 | Record counts | Every master file has >= 1 record after load | FAIL |
| G-02 | Key uniqueness -- accounts | No duplicate `ACCT-ID` in account master | FAIL |
| G-03 | Key uniqueness -- cards | No duplicate `CARD-NUM` in card master | FAIL |
| G-04 | Key uniqueness -- customers | No duplicate `CUST-ID` in customer master | FAIL |
| G-05 | Key uniqueness -- xrefs | No duplicate `XREF-CARD-NUM` in cross-reference file | FAIL |
| G-06 | Key uniqueness -- tran types | No duplicate `TRAN-TYPE` in transaction type master | FAIL |

---

## Data Refresh Jobs

### ACCTFILE -- Refresh Account Master

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| ACCT-01 | Record count preserved | Count of accounts after refresh >= count before refresh | FAIL |
| ACCT-02 | No negative credit limits | `ACCT-CREDIT-LIMIT >= 0` for all records | WARN |
| ACCT-03 | No negative cash credit limits | `ACCT-CASH-CREDIT-LIMIT >= 0` for all records | WARN |
| ACCT-04 | Valid date formats | `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE` match `YYYY-MM-DD` | FAIL |
| ACCT-05 | Active status valid | `ACCT-ACTIVE-STATUS` is `Y` or `N` | FAIL |
| ACCT-06 | Group ID present | `ACCT-GROUP-ID` is non-blank for every account | WARN |

### CARDFILE -- Refresh Card Master

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| CARD-01 | Record count preserved | Count of cards after refresh >= count before refresh | FAIL |
| CARD-02 | Account reference valid | Every `CARD-ACCT-ID` exists in account master | FAIL |
| CARD-03 | CVV non-zero | `CARD-CVV-CD > 0` for all active cards | WARN |
| CARD-04 | Expiration date valid | `CARD-EXPIRAION-DATE` matches `YYYY-MM-DD` | FAIL |
| CARD-05 | Active status valid | `CARD-ACTIVE-STATUS` is `Y` or `N` | FAIL |

### CUSTFILE -- Refresh Customer Master

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| CUST-01 | Record count preserved | Count of customers after refresh >= count before refresh | FAIL |
| CUST-02 | SSN format | `CUST-SSN` is 9 digits, non-zero | WARN |
| CUST-03 | DOB valid | `CUST-DOB-YYYY-MM-DD` matches `YYYY-MM-DD` | FAIL |
| CUST-04 | State code present | `CUST-ADDR-STATE-CD` is non-blank | WARN |
| CUST-05 | FICO range | `CUST-FICO-CREDIT-SCORE` is between 0 and 850 | WARN |

### XREFFILE -- Load Cross-Reference

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| XREF-01 | Account reference valid | Every `XREF-ACCT-ID` exists in account master | FAIL |
| XREF-02 | Customer reference valid | Every `XREF-CUST-ID` exists in customer master | FAIL |
| XREF-03 | Card number in card master | Every `XREF-CARD-NUM` exists in card master | FAIL |
| XREF-04 | Account-card parity | Every account in account master has >= 1 cross-reference entry | WARN |

### TRANFILE -- Load Transaction Master

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| TRAN-01 | Card reference valid | Every `TRAN-CARD-NUM` exists in card master | FAIL |
| TRAN-02 | Type code valid | Every `TRAN-TYPE-CD` exists in transaction type master | FAIL |
| TRAN-03 | Category code valid | Every (`TRAN-TYPE-CD`, `TRAN-CAT-CD`) pair exists in trancatg | FAIL |
| TRAN-04 | Amount non-zero | `TRAN-AMT != 0` for non-authorization transactions | WARN |
| TRAN-05 | Timestamp format | `TRAN-ORIG-TS` matches `YYYY-MM-DD HH:MM:SS.ffffff` | FAIL |

---

## Batch Processing Jobs

### POSTTRAN -- Transaction Posting (CBTRN02C)

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| POST-01 | All daily transactions consumed | Count of posted transactions = count of daily transaction input records | FAIL |
| POST-02 | Balance update equation | For each account: `new_curr_bal = old_curr_bal + sum(posted_debits) - sum(posted_credits)` | FAIL |
| POST-03 | Cycle debit accumulation | `ACCT-CURR-CYC-DEBIT` increases by the sum of debit transactions posted in this cycle | FAIL |
| POST-04 | Cycle credit accumulation | `ACCT-CURR-CYC-CREDIT` increases by the sum of credit transactions posted in this cycle | FAIL |
| POST-05 | Category balance update | For each (`ACCT-ID`, `TYPE-CD`, `CAT-CD`): `TRAN-CAT-BAL` reflects the sum of all posted transactions for that combination | FAIL |
| POST-06 | No orphan transactions | Every posted transaction references a valid account (via card -> xref -> account chain) | FAIL |
| POST-07 | Rejected transactions logged | Transactions with invalid card numbers are written to the daily reject file | WARN |

### INTCALC -- Interest Calculation (CBACT04C)

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| INT-01 | Interest rate lookup | For each account, the applicable interest rate is determined by its `ACCT-GROUP-ID` in the disclosure group file | FAIL |
| INT-02 | Interest amount formula | `interest = category_balance * (annual_rate / 365) * days_in_period` for each transaction category | FAIL |
| INT-03 | Interest transaction created | A transaction with `TRAN-TYPE-CD = '01'` and `TRAN-CAT-CD = '0005'` is created for each account that has a non-zero category balance | FAIL |
| INT-04 | Disclosure group coverage | Every (`ACCT-GROUP-ID`, `TRAN-TYPE-CD`, `TRAN-CAT-CD`) used in posting has a matching disclosure group entry | WARN |
| INT-05 | Zero-APR group | Accounts with `ACCT-GROUP-ID = 'ZEROAPR'` should have interest = 0 | FAIL |

### TRANBKP -- Transaction Backup

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| BKP-01 | Backup count match | Count of records in backup = count of records in transaction master before backup | FAIL |
| BKP-02 | Backup content integrity | Every record in backup is byte-identical to source | FAIL |

### COMBTRAN -- Combine Transactions

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| COMB-01 | No record loss | `count(combined_output) = count(existing_transactions) + count(new_daily_transactions)` | FAIL |
| COMB-02 | Key preservation | All transaction IDs from both inputs appear in the combined output | FAIL |
| COMB-03 | Sort order | Combined output is sorted by transaction ID (ascending) | WARN |

### CREASTMT -- Create Statements (CBSTM03A/B)

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| STMT-01 | One statement per active account | Every account with `ACCT-ACTIVE-STATUS = 'Y'` and non-zero current balance gets a statement | FAIL |
| STMT-02 | Statement balance = account balance | Opening balance + sum of transactions in statement period = closing balance shown on statement | FAIL |
| STMT-03 | Customer name on statement | Each statement includes `CUST-FIRST-NAME` and `CUST-LAST-NAME` from customer master (via xref) | FAIL |
| STMT-04 | Statement date range | Statement covers the correct billing cycle period | WARN |

### TRANREPT -- Transaction Report (CBTRN03C)

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| REPT-01 | Report record count | Number of detail lines = number of transactions in reporting period | FAIL |
| REPT-02 | Page totals | Each page total = sum of transaction amounts on that page | FAIL |
| REPT-03 | Account totals | Each account total = sum of transaction amounts for that account | FAIL |
| REPT-04 | Grand total | Grand total = sum of all account totals = sum of all transaction amounts | FAIL |
| REPT-05 | Type description lookup | `TRAN-REPORT-TYPE-DESC` matches `TRAN-TYPE-DESC` from transaction type master | FAIL |
| REPT-06 | Category description lookup | `TRAN-REPORT-CAT-DESC` matches `TRAN-CAT-TYPE-DESC` from transaction category master | FAIL |

---

## Cross-Job Invariants

These invariants must hold across the full batch cycle
(CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL):

| Check ID | Rule | Detail | Severity |
|----------|------|--------|----------|
| XJ-01 | Conservation of funds | Total money in = total money out across all accounts (debits = credits + fees + interest) | FAIL |
| XJ-02 | Transaction master growth | After COMBTRAN: `count(tran_master) >= count(tran_master_before) + count(daily_tran)` | FAIL |
| XJ-03 | Referential closure | After full cycle, no dangling references exist between accounts, cards, customers, and cross-references | FAIL |
| XJ-04 | Category balance consistency | Sum of all `TRAN-CAT-BAL` for an account = `ACCT-CURR-BAL` | FAIL |
| XJ-05 | Idempotent refresh | Running the data refresh jobs twice with the same input produces identical output | FAIL |

---

## Running the Checks

```bash
# Generate golden files (if not already present)
python3 test-harness/cobol_field_parser.py

# Run all reconciliation checks against golden files
python3 test-harness/reconciliation_runner.py golden-files/

# Run against Java output (after batch execution)
python3 test-harness/reconciliation_runner.py /path/to/java/output/
```

The runner exits with code 0 if all checks pass, 1 if any check fails.
Warnings do not cause a non-zero exit but are reported for review.

---

## Adding New Checks

1. Implement the check function in `test-harness/reconciliation_runner.py`,
   following the existing pattern (accept `data_dir: Path`, return
   `list[CheckResult]`).
2. Register the function in the `ALL_CHECKS` list.
3. Document the check in this file under the appropriate job section.
