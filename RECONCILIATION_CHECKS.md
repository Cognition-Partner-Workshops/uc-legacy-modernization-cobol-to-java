# Reconciliation Checks: Per-Job Validation Specifications

This document defines the reconciliation checks for each CardDemo batch job.
Each check specifies the invariant, the formula, the data sources involved,
and the expected result.

---

## 1. Data Refresh Jobs

These jobs load ASCII flat files into VSAM KSDS files. Each refresh job has its
own JCL (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, etc.).

### 1.1 ACCTFILE - Account Master Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-ACCT-01 | Record count preservation | `count(VSAM) == count(acctdata.txt)` | `acctdata.txt` -> `ACCTDATA.VSAM.KSDS` |
| DR-ACCT-02 | Key uniqueness | `count(DISTINCT ACCT-ID) == count(ACCT-ID)` | `ACCTDATA.VSAM.KSDS` |
| DR-ACCT-03 | Active status valid | `ACCT-ACTIVE-STATUS IN ('Y', 'N')` for all records | `ACCTDATA.VSAM.KSDS` |
| DR-ACCT-04 | Balance non-negative check | `ACCT-CURR-BAL >= 0` for active accounts | `ACCTDATA.VSAM.KSDS` |
| DR-ACCT-05 | Credit limit positive | `ACCT-CREDIT-LIMIT > 0` for all records | `ACCTDATA.VSAM.KSDS` |

### 1.2 CARDFILE - Card Master Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-CARD-01 | Record count preservation | `count(VSAM) == count(carddata.txt)` | `carddata.txt` -> `CARDDATA.VSAM.KSDS` |
| DR-CARD-02 | Key uniqueness | `count(DISTINCT CARD-NUM) == count(CARD-NUM)` | `CARDDATA.VSAM.KSDS` |
| DR-CARD-03 | Account reference validity | `CARD-ACCT-ID IN (SELECT ACCT-ID FROM ACCTDATA)` | `CARDDATA`, `ACCTDATA` |
| DR-CARD-04 | CVV format | `CARD-CVV-CD` is 3-digit numeric | `CARDDATA.VSAM.KSDS` |

### 1.3 CUSTFILE - Customer Master Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-CUST-01 | Record count preservation | `count(VSAM) == count(custdata.txt)` | `custdata.txt` -> `CUSTDATA.VSAM.KSDS` |
| DR-CUST-02 | Key uniqueness | `count(DISTINCT CUST-ID) == count(CUST-ID)` | `CUSTDATA.VSAM.KSDS` |
| DR-CUST-03 | SSN format | `CUST-SSN` is 9-digit numeric | `CUSTDATA.VSAM.KSDS` |
| DR-CUST-04 | FICO score range | `0 <= CUST-FICO-CREDIT-SCORE <= 850` | `CUSTDATA.VSAM.KSDS` |

### 1.4 XREFFILE - Card Cross-Reference Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-XREF-01 | Record count preservation | `count(VSAM) == count(cardxref.txt)` | `cardxref.txt` -> `CARDXREF.VSAM.KSDS` |
| DR-XREF-02 | Key uniqueness | `count(DISTINCT XREF-CARD-NUM) == count(XREF-CARD-NUM)` | `CARDXREF.VSAM.KSDS` |
| DR-XREF-03 | Customer ref validity | `XREF-CUST-ID IN (SELECT CUST-ID FROM CUSTDATA)` | `CARDXREF`, `CUSTDATA` |
| DR-XREF-04 | Account ref validity | `XREF-ACCT-ID IN (SELECT ACCT-ID FROM ACCTDATA)` | `CARDXREF`, `ACCTDATA` |
| DR-XREF-05 | Card coverage | Every `CARD-NUM` in CARDDATA has a corresponding `XREF-CARD-NUM` | `CARDXREF`, `CARDDATA` |

### 1.5 TRANFILE - Transaction Master Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-TRAN-01 | Key uniqueness | `count(DISTINCT TRAN-ID) == count(TRAN-ID)` | `TRANSACT.VSAM.KSDS` |
| DR-TRAN-02 | Type code validity | `TRAN-TYPE-CD IN (SELECT TRAN-TYPE FROM TRANTYPE)` | `TRANSACT`, `TRANTYPE` |
| DR-TRAN-03 | Card ref validity | `TRAN-CARD-NUM IN (SELECT XREF-CARD-NUM FROM CARDXREF)` | `TRANSACT`, `CARDXREF` |

### 1.6 TRANTYPE / TRANCATG - Reference Data Refresh

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| DR-TYPE-01 | Type code uniqueness | `count(DISTINCT TRAN-TYPE) == count(TRAN-TYPE)` | `trantype.txt` |
| DR-TYPE-02 | Record count | `count(VSAM) == count(trantype.txt)` | `trantype.txt` |
| DR-CAT-01 | Category key uniqueness | `count(DISTINCT TRAN-TYPE-CD + TRAN-CAT-CD) == count(records)` | `trancatg.txt` |
| DR-CAT-02 | Type code ref validity | `TRAN-TYPE-CD IN (SELECT TRAN-TYPE FROM TRANTYPE)` | `TRANCATG`, `TRANTYPE` |

---

## 2. POSTTRAN - Transaction Posting (CBTRN02C)

The POSTTRAN job reads daily transactions, validates them against the card
cross-reference, posts valid transactions to the transaction master, updates
account balances, and updates category balance records. Rejected transactions
are written to DALYREJS.

### Input Files
- `DALYTRAN.PS` (daily transactions)
- `CARDXREF.VSAM.KSDS` (card cross-reference for validation)
- `ACCTDATA.VSAM.KSDS` (account master - updated)
- `TRANSACT.VSAM.KSDS` (transaction master - updated)
- `TCATBALF.VSAM.KSDS` (category balance - updated)

### Output Files
- `DALYREJS(+1)` (rejected daily transactions)

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| PT-01 | Transaction disposition completeness | `count(DALYTRAN) == count(posted) + count(rejected)` | `DALYTRAN`, `TRANSACT`, `DALYREJS` |
| PT-02 | Posted transaction ID uniqueness | No duplicate TRAN-IDs in TRANSACT after posting | `TRANSACT.VSAM.KSDS` |
| PT-03 | Reject reason present | Every record in DALYREJS has a non-blank rejection reason | `DALYREJS` |
| PT-04 | Account balance update | For each account: `post_balance == pre_balance + sum(posted_amounts for account)` | `ACCTDATA` pre/post |
| PT-05 | Category balance update | For each (ACCT-ID, TYPE-CD, CAT-CD): `post_cat_bal == pre_cat_bal + sum(posted_amounts)` | `TCATBALF` pre/post |
| PT-06 | Card validation | Every posted transaction's CARD-NUM exists in CARDXREF | `TRANSACT`, `CARDXREF` |
| PT-07 | Debit/credit sign convention | Purchase (01) amounts are positive; Payment (02) amounts are positive (credit to account); Refund (05) amounts are positive (credit) | `TRANSACT` |
| PT-08 | Cycle debit/credit accumulation | `ACCT-CURR-CYC-DEBIT` incremented by debit transactions; `ACCT-CURR-CYC-CREDIT` incremented by credit transactions | `ACCTDATA` pre/post |

---

## 3. INTCALC - Interest Calculation (CBACT04C)

The INTCALC job reads category balances, looks up interest rates from the
disclosure group file, calculates interest for each balance, and generates
interest transactions written to SYSTRAN.

### Input Files
- `TCATBALF.VSAM.KSDS` (category balances)
- `DISCGRP.VSAM.KSDS` (disclosure group - interest rates)
- `CARDXREF.VSAM.KSDS` (card cross-reference)
- `ACCTDATA.VSAM.KSDS` (account master - updated with interest)

### Output Files
- `SYSTRAN(+1)` (system-generated interest transactions)

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| IC-01 | Interest transaction count | One interest transaction per non-zero category balance | `TCATBALF`, `SYSTRAN` |
| IC-02 | Interest rate lookup | Interest rate applied = `DIS-INT-RATE` from DISCGRP for matching `(ACCT-GROUP-ID, TRAN-TYPE-CD, TRAN-CAT-CD)` | `DISCGRP`, `SYSTRAN` |
| IC-03 | Interest amount calculation | `interest_amount == TRAN-CAT-BAL * DIS-INT-RATE / 365 * days_in_period` (daily accrual) | `TCATBALF`, `DISCGRP`, `SYSTRAN` |
| IC-04 | Account balance update | `post_acct_bal == pre_acct_bal + sum(interest_for_account)` | `ACCTDATA` pre/post |
| IC-05 | Interest transaction type | All SYSTRAN records have `TRAN-TYPE-CD = '01'` and `TRAN-CAT-CD = 0005` (Interest Amount) | `SYSTRAN` |
| IC-06 | Zero-rate accounts | Accounts in ZEROAPR group produce zero-amount interest transactions or no transactions | `DISCGRP`, `SYSTRAN` |
| IC-07 | Interest sign | Interest on positive balances is positive (debit to cardholder) | `SYSTRAN` |

---

## 4. TRANBKP - Transaction Backup

The TRANBKP job copies the current transaction master VSAM file to a
sequential backup (GDG), then deletes and redefines the VSAM cluster.

### Input Files
- `TRANSACT.VSAM.KSDS` (transaction master)

### Output Files
- `TRANSACT.BKUP(+1)` (sequential backup)
- `TRANSACT.VSAM.KSDS` (empty, redefined)

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| TB-01 | No data loss | `count(BKUP) == count(pre_TRANSACT)` | `TRANSACT` pre, `BKUP` |
| TB-02 | Content fidelity | Every record in BKUP byte-for-byte matches the source TRANSACT record | `TRANSACT` pre, `BKUP` |
| TB-03 | VSAM empty after redefine | `count(post_TRANSACT) == 0` | `TRANSACT` post |
| TB-04 | VSAM cluster attributes | Redefined VSAM has KEYS(16 0), RECORDSIZE(350 350), INDEXED | `TRANSACT` VSAM definition |

---

## 5. COMBTRAN - Combine Transactions

The COMBTRAN job sorts and merges the transaction backup with system-generated
transactions (from INTCALC), then loads the combined result into the
transaction master VSAM.

### Input Files
- `TRANSACT.BKUP(0)` (latest transaction backup)
- `SYSTRAN(0)` (latest system-generated transactions)

### Output Files
- `TRANSACT.COMBINED(+1)` (sorted combined sequential)
- `TRANSACT.VSAM.KSDS` (loaded combined transactions)

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| CT-01 | Count preservation | `count(COMBINED) == count(BKUP) + count(SYSTRAN)` | `BKUP`, `SYSTRAN`, `COMBINED` |
| CT-02 | No duplicates | `count(DISTINCT TRAN-ID in COMBINED) == count(COMBINED)` | `COMBINED` |
| CT-03 | Sort order | Records in COMBINED are sorted ascending by TRAN-ID | `COMBINED` |
| CT-04 | VSAM load completeness | `count(TRANSACT.VSAM) == count(COMBINED)` | `COMBINED`, `TRANSACT.VSAM` |
| CT-05 | Source traceability | Every record in COMBINED exists in either BKUP or SYSTRAN | `BKUP`, `SYSTRAN`, `COMBINED` |

---

## 6. CREASTMT - Statement Generation (CBSTM03A/B)

The CREASTMT job generates statements for each card. It sorts transactions by
card number, then produces text and HTML statement output files.

### Input Files
- `TRANSACT.VSAM.KSDS` (transaction master)
- `CARDXREF.VSAM.KSDS` (card cross-reference)
- `ACCTDATA.VSAM.KSDS` (account master)
- `CUSTDATA.VSAM.KSDS` (customer master)

### Output Files
- `STATEMNT.PS` (text statements)
- `STATEMNT.HTML` (HTML statements)
- `TRXFL.VSAM.KSDS` (working copy of transactions sorted by card+tran-id)

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| CS-01 | Card coverage | One statement section per card in CARDXREF | `CARDXREF`, `STATEMNT` |
| CS-02 | Transaction inclusion | Every transaction in TRANSACT appears in exactly one card's statement | `TRANSACT`, `STATEMNT` |
| CS-03 | Statement balance | Per-card statement total = `sum(TRAN-AMT)` for all transactions with that card number | `TRANSACT`, `STATEMNT` |
| CS-04 | Customer name accuracy | Customer name on statement matches `CUST-FIRST-NAME + CUST-LAST-NAME` from CUSTDATA via XREF | `CUSTDATA`, `CARDXREF`, `STATEMNT` |
| CS-05 | Account info accuracy | Account balance shown on statement matches `ACCT-CURR-BAL` from ACCTDATA | `ACCTDATA`, `STATEMNT` |
| CS-06 | Working file sort | `TRXFL.VSAM.KSDS` records sorted by `TRNX-CARD-NUM, TRNX-ID` ascending | `TRXFL` |
| CS-07 | Grand total | Sum of all per-card totals in statement equals sum of all transaction amounts | `STATEMNT` |
| CS-08 | Dual output consistency | Text and HTML statements contain the same data (amounts, names, dates) | `STATEMNT.PS`, `STATEMNT.HTML` |

---

## 7. DUSRSECJ - User Security Load

| Check ID | Invariant | Formula | Sources |
|----------|-----------|---------|---------|
| US-01 | Record count | `count(VSAM) == count(source)` | User security file |
| US-02 | User ID uniqueness | `count(DISTINCT SEC-USR-ID) == count(SEC-USR-ID)` | `USRSEC.VSAM.KSDS` |
| US-03 | User type valid | `SEC-USR-TYPE IN ('A', 'U')` (Admin or User) | `USRSEC.VSAM.KSDS` |
| US-04 | Password non-blank | `SEC-USR-PWD != SPACES` for all records | `USRSEC.VSAM.KSDS` |

---

## Batch Cycle End-to-End Reconciliation

After a complete batch cycle (CLOSEFIL -> refresh -> POSTTRAN -> INTCALC ->
TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL), the following
end-to-end invariants must hold:

| Check ID | Invariant | Formula |
|----------|-----------|---------|
| E2E-01 | Transaction completeness | Final TRANSACT count = pre-cycle TRANSACT count + posted daily transactions + interest transactions |
| E2E-02 | Account balance reconciliation | For each account: `final_balance == initial_balance + sum(all_posted_debits) - sum(all_posted_credits) + interest` |
| E2E-03 | Category balance reconciliation | For each category: `final_cat_bal == initial_cat_bal + sum(posted_in_category) + interest_in_category` |
| E2E-04 | No orphan transactions | Every TRAN-CARD-NUM in final TRANSACT has a matching XREF-CARD-NUM in CARDXREF |
| E2E-05 | Statement coverage | Number of statement sections = number of distinct cards in CARDXREF |
| E2E-06 | Zero reject reprocessing | Records in DALYREJS from current cycle do not appear in TRANSACT |

---

## Implementation

All checks are implemented in `test-harness/reconciliation/checks.py` as
composable functions. The `reconciliation_runner.py` script orchestrates
execution and produces a JSON report. Each check returns:

```json
{
  "check": "Human-readable label",
  "pass": true,
  "variance": 0,
  ...additional context fields...
}
```

Checks can be run individually or as a full suite. The runner exits with code 0
if all checks pass, or code 1 if any check fails.
