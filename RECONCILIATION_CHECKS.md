# Reconciliation Checks

## Overview

This document specifies the per-job validation checks that run after each batch job completes in the migrated Java system. Each check verifies a specific data integrity invariant that must hold true to confirm correct migration behavior.

---

## Job: POSTTRAN (Post-Transaction Processing)

**JCL Source:** `app/jcl/POSTTRAN.jcl`  
**Purpose:** Posts daily transactions to accounts, updating balances and category totals.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `balance_update` | Account `ACCT-CURR-BAL` = previous balance + credits − debits | Exact match | ±0.00 |
| 2 | `cycle_credit_accumulation` | `ACCT-CURR-CYC-CREDIT` incremented by sum of credit transactions | Exact match | ±0.00 |
| 3 | `cycle_debit_accumulation` | `ACCT-CURR-CYC-DEBIT` incremented by sum of debit transactions | Exact match | ±0.00 |
| 4 | `category_balance_rollup` | Sum of `TRAN-CAT-BAL` for each account = net change in `ACCT-CURR-BAL` | Exact match | ±0.01 |
| 5 | `balance_account_reference` | All `TRANCAT-ACCT-ID` values in tcatbal reference valid accounts | Zero orphans | — |
| 6 | `no_negative_credit_limit` | `ACCT-CREDIT-LIMIT` never goes negative after posting | ≥ 0 | — |

### Implementation Details
- **Input files:** `dailytran.txt`, `acctdata.txt`, `tcatbal.txt`
- **Output files:** Updated `acctdata.txt`, updated `tcatbal.txt`
- **Key formula:** `new_balance = old_balance + Σ(credits) - Σ(debits)` where credits have `TRAN-TYPE-CD = '03'` and debits have `TRAN-TYPE-CD IN ('01', '02')`

---

## Job: INTCALC (Interest Calculation)

**JCL Source:** `app/jcl/INTCALC.jcl`  
**Purpose:** Calculates daily interest charges based on disclosure group rates.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `interest_formula` | Interest = `TRAN-CAT-BAL` × (`DIS-INT-RATE` / 36500) × days | Calculated value | ±0.01 |
| 2 | `rate_lookup` | Each account's group ID maps to valid disclosure group entry | Zero unmatched | — |
| 3 | `category_rate_match` | Interest rate applied matches the transaction category and type | Exact match | — |
| 4 | `zero_balance_no_interest` | Accounts with zero/negative category balance accrue no interest | 0.00 | — |

### Implementation Details
- **Input files:** `acctdata.txt`, `tcatbal.txt`, `discgrp.txt`
- **Output files:** Interest transaction records appended to daily transactions
- **Key formula:** `daily_interest = category_balance * (annual_rate / 36500)` (rate is stored as `S9(04)V99`)
- **Rate lookup:** `ACCT-GROUP-ID` + `TRAN-TYPE-CD` + `TRAN-CAT-CD` → `DIS-INT-RATE`

---

## Job: TRANBKP (Transaction Backup)

**JCL Source:** `app/jcl/TRANBKP.jcl`  
**Purpose:** Creates backup copies of processed transaction files (GDG management).

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `record_count_match` | Backup file record count = source transaction file count | Exact match | — |
| 2 | `byte_equivalence` | Backup content is byte-identical to source (pre-processing) | Byte match | — |
| 3 | `no_data_loss` | Every source record appears in backup | Zero missing | — |
| 4 | `record_order_preserved` | Records appear in identical sequence in backup | Sequential match | — |

### Implementation Details
- **Input files:** `dailytran.txt`
- **Output files:** Backup copy in GDG (Generation Data Group)
- **Validation approach:** SHA-256 hash comparison between source and backup

---

## Job: CBEXPORT (Branch Export)

**JCL Source:** `app/jcl/CBEXPORT.jcl`  
**Purpose:** Exports consolidated account/customer/card data for branch migration.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `export_completeness` | All active accounts appear in export file | Zero missing | — |
| 2 | `customer_inclusion` | Every exported account has corresponding customer record | Zero orphans | — |
| 3 | `card_inclusion` | Every exported account has at least one card record | Zero missing | — |
| 4 | `xref_consistency` | Export cross-references match source xref file | Exact match | — |
| 5 | `record_type_distribution` | Export contains correct proportion of C/A/T/X record types | Matches input | — |

### Implementation Details
- **Input files:** `acctdata.txt`, `custdata.txt`, `carddata.txt`, `cardxref.txt`, `dailytran.txt`
- **Output files:** Export file with multi-record layout (CVEXPORT.cpy)
- **Record types:** `C` = Customer, `A` = Account, `T` = Transaction, `X` = Cross-reference

---

## Job: CBIMPORT (Branch Import)

**JCL Source:** `app/jcl/CBIMPORT.jcl`  
**Purpose:** Imports branch data from export file into local VSAM datasets.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `import_record_count` | Records imported = records in export file | Exact match | — |
| 2 | `no_duplicate_keys` | No duplicate VSAM keys created during import | Zero duplicates | — |
| 3 | `referential_post_import` | Post-import xref references are all valid | Zero orphans | — |
| 4 | `data_fidelity` | Imported field values match export source exactly | Field-by-field match | — |

### Implementation Details
- **Input files:** Export file (from CBEXPORT)
- **Output files:** Updated VSAM datasets (accounts, customers, cards, xref)
- **Validation approach:** Round-trip test: export → import → re-export → compare

---

## Job: DALYREJS (Daily Rejection Processing)

**JCL Source:** `app/jcl/DALYREJS.jcl`  
**Purpose:** Processes and logs rejected transactions from daily batch.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `rejection_accounting` | Rejected + processed = total input transactions | Exact match | — |
| 2 | `rejection_reason_code` | Every rejected transaction has a valid reason code | Zero invalid | — |
| 3 | `no_valid_rejections` | No transaction with valid data appears in rejection file | Zero false rejects | — |
| 4 | `rejection_file_format` | Rejection records conform to transaction layout (350 bytes) | Format valid | — |

### Implementation Details
- **Input files:** `dailytran.txt`
- **Output files:** Rejection file, processed transaction file
- **Rejection criteria:** Invalid card number, expired card, over credit limit, invalid merchant

---

## Job: XREFFILE (Cross-Reference Maintenance)

**JCL Source:** `app/jcl/XREFFILE.jcl`  
**Purpose:** Maintains card/account/customer cross-reference linkages.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `xref_card_reference` | All `XREF-CARD-NUM` values exist in card file | Zero orphans | — |
| 2 | `xref_account_reference` | All `XREF-ACCT-ID` values exist in account file | Zero orphans | — |
| 3 | `xref_customer_reference` | All `XREF-CUST-ID` values exist in customer file | Zero orphans | — |
| 4 | `xref_uniqueness` | No duplicate card numbers in cross-reference | Zero duplicates | — |
| 5 | `bidirectional_link` | Every card record's `CARD-ACCT-ID` has matching xref entry | Zero unlinked | — |

### Implementation Details
- **Input files:** `cardxref.txt`, `carddata.txt`, `acctdata.txt`, `custdata.txt`
- **Output files:** Updated cross-reference file
- **Key:** `XREF-CARD-NUM` (16 bytes) is the primary key

---

## Job: TRANFILE (Transaction File Maintenance)

**JCL Source:** `app/jcl/TRANFILE.jcl`  
**Purpose:** Manages daily transaction file loading and validation.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `transaction_type_reference` | All `TRAN-TYPE-CD` values exist in transaction type file | Zero invalid | — |
| 2 | `transaction_category_reference` | All type/category combinations exist in trancatg file | Zero invalid | — |
| 3 | `card_number_valid` | All `TRAN-CARD-NUM` values exist in card file | Zero orphans | — |
| 4 | `amount_sign_correct` | Debits are positive, credits/returns are negative per type | Sign correct | — |
| 5 | `timestamp_format` | `TRAN-ORIG-TS` follows `YYYY-MM-DD HH:MM:SS.ffffff` format | Format valid | — |

### Implementation Details
- **Input files:** `dailytran.txt`, `trantype.txt`, `trancatg.txt`, `carddata.txt`
- **Output files:** Validated transaction file ready for posting
- **Record length:** 350 bytes per CVTRA05Y.cpy

---

## Job: DATA_INTEGRITY (Cross-Dataset Validation)

**Purpose:** Validates overall dataset consistency across all files.

| # | Check Name | Validation | Expected | Tolerance |
|---|-----------|-----------|----------|-----------|
| 1 | `record_count_accounts` | Account file has expected record count | 50 records | — |
| 2 | `record_count_cards` | Card file has expected record count | 50 records | — |
| 3 | `record_count_customers` | Customer file has expected record count | 50 records | — |
| 4 | `record_count_transactions` | Transaction file has expected record count | 300 records | — |
| 5 | `record_count_xref` | Cross-reference file has expected record count | 50 records | — |
| 6 | `card_to_account_link` | Every card's `CARD-ACCT-ID` references a valid account | Zero orphans | — |
| 7 | `account_uniqueness` | No duplicate `ACCT-ID` values | Zero duplicates | — |
| 8 | `customer_uniqueness` | No duplicate `CUST-ID` values | Zero duplicates | — |

### Implementation Details
- **Scope:** All data files simultaneously
- **Trigger:** Run after any batch job that modifies datasets
- **Exit criteria:** Zero failures required for migration sign-off

---

## Execution Matrix

| Job | Pre-conditions | Check Count | Critical Path |
|-----|---------------|-------------|---------------|
| POSTTRAN | Daily transactions loaded | 6 | Yes |
| INTCALC | Balances current, disclosure groups loaded | 4 | Yes |
| TRANBKP | Transaction file exists | 4 | No |
| CBEXPORT | All master files current | 5 | Yes |
| CBIMPORT | Valid export file available | 4 | Yes |
| DALYREJS | Raw transactions loaded | 4 | Yes |
| XREFFILE | Card/Account/Customer files loaded | 5 | Yes |
| TRANFILE | Transaction type/category refs loaded | 5 | Yes |
| DATA_INTEGRITY | All files available | 8 | Yes |

---

## Failure Handling

When a reconciliation check fails:

1. **Log** the failure with full context (check name, expected vs actual, affected records)
2. **Halt** dependent downstream jobs (if on critical path)
3. **Alert** the migration validation team with failure details
4. **Preserve** both input and output files for forensic analysis
5. **Report** the failure in the summary report with severity classification

### Severity Levels

| Level | Criteria | Action |
|-------|----------|--------|
| **CRITICAL** | Data loss or corruption detected | Immediate halt, manual investigation |
| **HIGH** | Referential integrity broken | Halt dependent jobs, auto-retry once |
| **MEDIUM** | Count mismatch or precision error | Log and continue, flag for review |
| **LOW** | Cosmetic or format difference | Log only, does not block |
