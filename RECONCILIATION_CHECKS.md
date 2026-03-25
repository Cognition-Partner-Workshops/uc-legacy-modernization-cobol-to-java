# CardDemo Reconciliation Checks

Per-job validation specifications for the COBOL-to-Java migration.

---

## Overview

Each batch job in the CardDemo system transforms data in specific, auditable
ways. This document specifies the exact reconciliation checks to run after
each migrated Java batch job to confirm data integrity.

All checks are implemented in `test-harness/reconciliation.py` and use the
golden-reference JSON files in `golden-files/` as the baseline.

---

## Level 1: Record Count Checks

Record counts must match exactly between COBOL source and Java output.

| Data File | Copybook | Baseline Count | Check |
|-----------|----------|---------------|-------|
| `acctdata.txt` | `CVACT01Y.cpy` | 50 | `record_count:acctdata` |
| `carddata.txt` | `CVACT02Y.cpy` | 50 | `record_count:carddata` |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 | `record_count:cardxref` |
| `custdata.txt` | `CVCUS01Y.cpy` | 50 | `record_count:custdata` |
| `dailytran.txt` | `CVTRA06Y.cpy` | 300 | `record_count:dailytran` |
| `discgrp.txt` | `CVTRA02Y.cpy` | 51 | `record_count:discgrp` |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 | `record_count:tcatbal` |
| `trancatg.txt` | `CVTRA04Y.cpy` | 18 | `record_count:trancatg` |
| `trantype.txt` | `CVTRA03Y.cpy` | 7 | `record_count:trantype` |

---

## Level 2: Control Total Checks

### 2.1 Account Balances

| Control Total | Baseline Value | Tolerance | Formula |
|---------------|---------------|-----------|---------|
| Sum of `ACCT-CURR-BAL` | 12,269.00 | 0.01 | SUM(acctdata.ACCT-CURR-BAL) |
| Sum of `ACCT-CREDIT-LIMIT` | 233,711.00 | 0.01 | SUM(acctdata.ACCT-CREDIT-LIMIT) |
| Sum of `ACCT-CASH-CREDIT-LIMIT` | 122,148.00 | 0.01 | SUM(acctdata.ACCT-CASH-CREDIT-LIMIT) |
| Sum of `ACCT-CURR-CYC-CREDIT` | 0.00 | 0.01 | SUM(acctdata.ACCT-CURR-CYC-CREDIT) |
| Sum of `ACCT-CURR-CYC-DEBIT` | 0.00 | 0.01 | SUM(acctdata.ACCT-CURR-CYC-DEBIT) |

### 2.2 Transaction Totals

| Control Total | Baseline Value | Tolerance | Formula |
|---------------|---------------|-----------|---------|
| Sum of daily transaction amounts | 104,801.54 | 0.01 | SUM(dailytran.TRAN-AMT) |
| Sum of category balances | 0.00 | 0.01 | SUM(tcatbal.TRAN-CAT-BAL) |

### 2.3 Interest Rates

| Control Total | Baseline Value | Tolerance | Formula |
|---------------|---------------|-----------|---------|
| Sum of disclosure interest rates | 375.00 | 0.01 | SUM(discgrp.DIS-INT-RATE) |

---

## Level 3: Referential Integrity Checks

### 3.1 Card-to-Account (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:card->account` |
| **Parent** | `acctdata.ACCT-ID` |
| **Child** | `carddata.CARD-ACCT-ID` |
| **Constraint** | Every card must reference a valid account |
| **Baseline** | 0 orphans / 50 cards |
| **Status** | PASS |

### 3.2 Cross-Reference-to-Customer (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:xref->customer` |
| **Parent** | `custdata.CUST-ID` |
| **Child** | `cardxref.XREF-CUST-ID` |
| **Constraint** | Every cross-reference must link to a valid customer |
| **Baseline** | 0 orphans / 50 xrefs |
| **Status** | PASS |

### 3.3 Cross-Reference-to-Account (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:xref->account` |
| **Parent** | `acctdata.ACCT-ID` |
| **Child** | `cardxref.XREF-ACCT-ID` |
| **Constraint** | Every cross-reference must link to a valid account |
| **Baseline** | 0 orphans / 50 xrefs |
| **Status** | PASS |

### 3.4 Cross-Reference-to-Card (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:xref->card` |
| **Parent** | `carddata.CARD-NUM` |
| **Child** | `cardxref.XREF-CARD-NUM` |
| **Constraint** | Every cross-reference must link to a valid card |
| **Baseline** | 0 orphans / 50 xrefs |
| **Status** | PASS |

### 3.5 Daily Transactions-to-Card (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:dailytran->card` |
| **Parent** | `carddata.CARD-NUM` |
| **Child** | `dailytran.TRAN-CARD-NUM` |
| **Constraint** | Every transaction must reference a valid card |
| **Baseline** | 0 orphans / 300 transactions |
| **Status** | PASS |

### 3.6 Category Balance-to-Account (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:tcatbal->account` |
| **Parent** | `acctdata.ACCT-ID` |
| **Child** | `tcatbal.TRANCAT-ACCT-ID` |
| **Constraint** | Every category balance must reference a valid account |
| **Baseline** | 0 orphans / 50 records |
| **Status** | PASS |

### 3.7 Transaction Category-to-Type (PASS)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:trancatg->trantype` |
| **Parent** | `trantype.TRAN-TYPE` |
| **Child** | `trancatg.TRAN-TYPE-CD` |
| **Constraint** | Every transaction category must reference a valid type |
| **Baseline** | 0 orphans / 18 categories |
| **Status** | PASS |

### 3.8 Disclosure Group-to-Account Group (KNOWN GAP)

| Property | Value |
|----------|-------|
| **Check ID** | `ref_integrity:discgrp->acctgroup` |
| **Parent** | `acctdata.ACCT-GROUP-ID` |
| **Child** | `discgrp.DIS-ACCT-GROUP-ID` |
| **Constraint** | Every disclosure group should reference a valid account group |
| **Baseline** | 51 orphans / 51 records |
| **Status** | KNOWN GAP |
| **Notes** | Account group IDs (`ACCT-GROUP-ID`) are empty in the sample data while disclosure groups use distinct IDs (`A000000000`, `DEFAULT`, `ZEROAPR`). This is a pre-existing data condition in the COBOL source, not a migration defect. The migrated Java system should preserve this same relationship (or lack thereof). |

---

## Per-Job Reconciliation Specifications

### Job: POSTTRAN (Transaction Posting)

**COBOL Program:** `CBTRN02C`
**Input:** `dailytran.txt`, `acctdata.txt`, `cardxref.txt`
**Output:** Updated `acctdata.txt` (modified balances), transaction log

| Check | Specification |
|-------|---------------|
| **Pre-condition** | Record counts for all input files match baseline |
| **Balance equation** | `POST_ACCT_BAL = PRE_ACCT_BAL + SUM(posted_credits) - SUM(posted_debits)` per account |
| **Cross-check** | `SUM(POST_ACCT_CURR_BAL) - SUM(PRE_ACCT_CURR_BAL) = SUM(TRAN-AMT)` for posted transactions |
| **Cycle totals** | `ACCT-CURR-CYC-CREDIT` += sum of credit transactions; `ACCT-CURR-CYC-DEBIT` += sum of debit transactions |
| **Record count** | Account count unchanged (posting modifies, not creates/deletes) |
| **Referential** | All posted transactions reference valid cards (via `cardxref`) |

### Job: INTCALC (Interest Calculation)

**COBOL Program:** `CBACT04C`
**Input:** `acctdata.txt`, `discgrp.txt`
**Output:** Updated `acctdata.txt` (accrued interest applied)

| Check | Specification |
|-------|---------------|
| **Interest formula** | `interest = ACCT-CURR-BAL * DIS-INT-RATE / 365 * days_in_period` |
| **Balance equation** | `POST_BAL = PRE_BAL + calculated_interest` per account |
| **Rate lookup** | Interest rate determined by `ACCT-GROUP-ID` -> `discgrp.DIS-ACCT-GROUP-ID` + transaction type/category |
| **Total check** | `SUM(POST_BAL) - SUM(PRE_BAL) = SUM(interest_applied)` |
| **Record count** | Account count unchanged |
| **Zero-balance** | Accounts with zero balance should accrue zero interest |

### Job: CREASTMT (Statement Generation)

**COBOL Programs:** `CBSTM03A`, `CBSTM03B`
**Input:** `acctdata.txt`, `custdata.txt`, `dailytran.txt`
**Output:** Statement records

| Check | Specification |
|-------|---------------|
| **Statement count** | One statement per active account with transaction activity |
| **Amount match** | Statement total per account = SUM(TRAN-AMT) for that account's transactions |
| **Customer data** | Statement includes correct customer name and address from `custdata` |
| **Date range** | Statement covers the correct billing cycle period |
| **Active only** | No statements for inactive accounts (`ACCT-ACTIVE-STATUS != 'Y'`) |

### Job: COMBTRAN (Combine Transactions)

**Input:** `dailytran.txt`, existing transaction master
**Output:** Combined transaction file

| Check | Specification |
|-------|---------------|
| **Record count** | `OUTPUT_COUNT = DAILY_COUNT + MASTER_COUNT` (no dropped records) |
| **No duplicates** | `TRAN-ID` values are unique in combined output |
| **Amount total** | `SUM(output.TRAN-AMT) = SUM(daily.TRAN-AMT) + SUM(master.TRAN-AMT)` |
| **Ordering** | Records sorted by account/card/timestamp (verify sort order) |
| **Field preservation** | All source fields preserved without truncation or corruption |

### Job: TRANREPT (Transaction Report)

**COBOL Program:** `CBTRN03C`
**Input:** Transaction file, `trantype.txt`, `trancatg.txt`
**Output:** Report matching `CVTRA07Y.cpy` layout

| Check | Specification |
|-------|---------------|
| **Grand total** | Report grand total = SUM(all transaction amounts) |
| **Account totals** | Per-account subtotals = SUM(TRAN-AMT) grouped by account |
| **Page totals** | Sum of page totals = grand total |
| **Type descriptions** | Transaction type descriptions match `trantype.TRAN-TYPE-DESC` |
| **Category descriptions** | Category descriptions match `trancatg.TRAN-CAT-TYPE-DESC` |
| **Record coverage** | Every transaction in input appears in report |

### Job: TRANBKP (Transaction Backup)

**Input:** Transaction master file
**Output:** Backup copy

| Check | Specification |
|-------|---------------|
| **Byte-identical** | Output file is byte-for-byte identical to input |
| **Record count** | Exact match |
| **Checksum** | MD5/SHA-256 hash match between source and backup |

### Job: ACCTFILE / CARDFILE / CUSTFILE / XREFFILE / TRANFILE (Data Refresh)

**Input:** Source data files
**Output:** Refreshed VSAM files (or database tables in Java)

| Check | Specification |
|-------|---------------|
| **Record count** | Output count = input count per file |
| **Field fidelity** | Every field value in output matches input (golden-file comparison) |
| **Key uniqueness** | Primary keys are unique: `ACCT-ID`, `CARD-NUM`, `CUST-ID`, `XREF-CARD-NUM` |
| **Encoding** | No EBCDIC/ASCII corruption (all printable characters preserved) |

---

## Execution

```bash
# Generate golden-reference baselines
cd test-harness
python generate_golden_files.py

# Run reconciliation (baseline only, no Java output yet)
python reconciliation.py

# Run reconciliation with Java output
python reconciliation.py --java-dir /path/to/java/output

# Generate and validate contracts
python contract_validator.py --generate
python contract_validator.py --java-dir /path/to/java/output
```

---

## Automation Notes

1. **CI Integration**: All reconciliation checks should run as part of the CI
   pipeline after each Java batch job integration test.

2. **Threshold Alerts**: Any control total delta exceeding 0.01 should fail
   the build and trigger review.

3. **Referential Integrity**: The `discgrp->acctgroup` check is a known gap
   in the source data. The Java migration should preserve this same state.
   Once account group IDs are populated, this check will activate.

4. **Regression Guard**: Golden files serve as immutable baselines. Any
   change to golden files requires explicit review and re-approval.
