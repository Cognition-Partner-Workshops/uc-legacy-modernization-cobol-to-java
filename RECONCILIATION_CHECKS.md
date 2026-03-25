# CardDemo Reconciliation Checks

Per-job validation specifications for the CardDemo COBOL-to-Java migration. Each check defines an invariant that must hold after a specific batch job completes.

---

## POSTTRAN -- Transaction Posting (CBTRN02C)

**JCL**: `POSTTRAN.jcl`
**Program**: `CBTRN02C`
**Input files**: `DALYTRAN` (daily transactions), `XREFFILE` (card cross-reference), `ACCTFILE` (account master), `TCATBALF` (transaction category balance)
**Output files**: Updated `TRANSACT` (transaction master), updated `TCATBALF`, `DALYREJS` (rejected transactions)

| Check ID | Invariant | Formula | Tolerance |
|---|---|---|---|
| RECON-01 | Transaction master record count after posting | `count(TRANSACT_after) == count(TRANSACT_before) + count(DALYTRAN) - count(DALYREJS)` | Exact (0) |
| RECON-02 | Transaction category balance reconciles to transaction amounts | `sum(TCATBALF.TRAN-CAT-BAL) == sum(TRANSACT.TRAN-AMT)` grouped by `(TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD)` | 0.01 |
| RECON-03 | Rejected transactions have invalid card references | Every `DALYTRAN.DALYTRAN-CARD-NUM` exists in `CARDXREF.XREF-CARD-NUM` OR the transaction appears in `DALYREJS` | Exact |

### Detailed Check Descriptions

**RECON-01: Transaction Count Balance**
- Before POSTTRAN: snapshot `count(TRANSACT)` as `N_before`.
- After POSTTRAN: count the updated `TRANSACT`, `DALYTRAN` input, and `DALYREJS` output.
- Assert: `count(TRANSACT_after) = N_before + count(DALYTRAN) - count(DALYREJS)`.
- Rationale: Every daily transaction either gets posted to the master or rejected. No records should be lost or duplicated.

**RECON-02: Category Balance Consistency**
- After POSTTRAN: for each unique `(ACCT-ID, TYPE-CD, CAT-CD)` group, the `TRAN-CAT-BAL` in `TCATBALF` must equal the sum of `TRAN-AMT` in `TRANSACT` for records matching that group.
- Tolerance: 0.01 (one cent) to account for floating-point rounding.

**RECON-03: Rejection Completeness**
- Every record in `DALYTRAN` whose `DALYTRAN-CARD-NUM` does NOT exist in `CARDXREF.XREF-CARD-NUM` must appear in `DALYREJS`.
- Every record in `DALYREJS` must have a `DALYTRAN-CARD-NUM` that is NOT in `CARDXREF`.
- No valid transactions should be rejected; no invalid transactions should be posted.

---

## INTCALC -- Interest Calculation (CBACT04C)

**JCL**: `INTCALC.jcl`
**Program**: `CBACT04C`
**Parameter**: Billing date (e.g., `2022071800`)
**Input files**: `TCATBALF`, `XREFFILE`, `XREFFIL1` (AIX path), `ACCTFILE`, `DISCGRP` (disclosure groups)
**Output files**: `SYSTRAN` (system-generated interest transactions)

| Check ID | Invariant | Formula | Tolerance |
|---|---|---|---|
| RECON-04 | Interest transaction generated for every non-zero balance | Every account with `TRAN-CAT-BAL != 0` in `TCATBALF` has at least one record in `SYSTRAN` | Exact |
| RECON-05 | Interest amount matches rate calculation | `interest = TRAN-CAT-BAL * DIS-INT-RATE / 1200` per category per account | 0.01 |

### Detailed Check Descriptions

**RECON-04: Interest Coverage**
- For each unique `TRANCAT-ACCT-ID` in `TCATBALF` where `TRAN-CAT-BAL != 0`:
  - At least one interest transaction must exist in `SYSTRAN` for that account.
- Accounts with zero balances across all categories should NOT generate interest transactions.

**RECON-05: Interest Amount Accuracy**
- For each `(TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD)` in `TCATBALF`:
  1. Look up the matching `DIS-INT-RATE` from `DISCGRP` using `(ACCT-GROUP-ID, TRAN-TYPE-CD, TRAN-CAT-CD)`.
  2. Expected interest = `TRAN-CAT-BAL * DIS-INT-RATE / 1200` (monthly rate = annual rate / 12, rate stored as percentage * 100).
  3. Compare with the `TRAN-AMT` of the corresponding `SYSTRAN` record.
- Tolerance: 0.01.

---

## CREASTMT -- Statement Generation (CBSTM03A/B)

**JCL**: `CREASTMT.JCL`
**Programs**: `SORT`, `IDCAMS`, `CBSTM03A` (calls `CBSTM03B`)
**Input files**: `TRANSACT` (transaction master), `XREFFILE`, `ACCTFILE`, `CUSTFILE`
**Output files**: `STATEMNT.PS` (text statement), `STATEMNT.HTML` (HTML statement)

| Check ID | Invariant | Formula | Tolerance |
|---|---|---|---|
| RECON-06 | Statement coverage matches cards with transactions | Number of statement blocks == number of distinct cards in `XREFFILE` that have transactions in `TRANSACT` | Exact |

### Detailed Check Descriptions

**RECON-06: Statement Completeness**
- Step 1: Identify every `XREF-CARD-NUM` in `CARDXREF` that has at least one matching `TRAN-CARD-NUM` in `TRANSACT`.
- Step 2: Count the number of distinct statement blocks in the output (each block starts with a card/account header).
- Assert: The two counts are equal.
- Additionally: every transaction for a given card number should appear in that card's statement block.

---

## COMBTRAN -- Combine Transactions (SORT + IDCAMS)

**JCL**: `COMBTRAN.jcl`
**Programs**: `SORT`, `IDCAMS`
**Input files**: `TRANSACT.BKUP` (backup from TRANBKP), `SYSTRAN` (from INTCALC)
**Output files**: `TRANSACT.COMBINED`, updated `TRANSACT.VSAM.KSDS`

| Check ID | Invariant | Formula | Tolerance |
|---|---|---|---|
| RECON-07 | Combined file record count | `count(COMBINED) == count(BACKUP) + count(SYSTRAN)` assuming no duplicate TRAN-IDs | Exact |

### Detailed Check Descriptions

**RECON-07: Merge Completeness**
- The SORT step merges the backup and system-generated transactions, sorted by `TRAN-ID`.
- Assert: `count(COMBINED) = count(BACKUP) + count(SYSTRAN)`.
- If duplicate `TRAN-ID` values exist between BACKUP and SYSTRAN, the count may differ. In that case, verify: `count(COMBINED) = count(distinct TRAN-IDs across both inputs)`.
- The IDCAMS REPRO step loads the combined file into the VSAM KSDS. Verify: `count(TRANSACT.VSAM) == count(COMBINED)`.

---

## TRANBKP -- Transaction Backup (REPROC + IDCAMS)

**JCL**: `TRANBKP.jcl`
**Programs**: `REPROC` (proc), `IDCAMS`
**Input files**: `TRANSACT.VSAM.KSDS` (transaction master)
**Output files**: `TRANSACT.BKUP` (sequential backup)

| Check ID | Invariant | Formula | Tolerance |
|---|---|---|---|
| RECON-08 | Backup record count matches master | `count(BACKUP) == count(TRANSACT_before)` | Exact |

### Detailed Check Descriptions

**RECON-08: Backup Integrity**
- Before TRANBKP: snapshot `count(TRANSACT)` as `N_before`.
- After TRANBKP: the sequential backup file must contain exactly `N_before` records.
- Additionally: the IDCAMS step deletes and redefines the TRANSACT VSAM cluster, so the cluster should be empty after this job.
- Verify: `count(TRANSACT_after) == 0` (empty cluster ready for COMBTRAN reload).

---

## Data Load -- Baseline File Integrity

These checks validate the initial data load before any batch processing begins. They are implemented in `test-harness/run_data_load_checks.py`.

| Check ID | Invariant | Status |
|---|---|---|
| RECON-09 | Every `CARD.CARD-ACCT-ID` exists in `ACCOUNT.ACCT-ID` | Automated |
| RECON-10 | Every `XREF.XREF-ACCT-ID` exists in `ACCOUNT.ACCT-ID` | Automated |
| RECON-11 | Every `XREF.XREF-CARD-NUM` exists in `CARD.CARD-NUM` | Automated |
| RECON-12 | Every `XREF.XREF-CUST-ID` exists in `CUSTOMER.CUST-ID` | Automated |
| RECON-13 | All master files have non-zero record counts | Automated |
| RECON-14 | Card file and cross-reference file have equal record counts | Automated |
| RECON-15 | Account IDs in acctdata match distinct account IDs in tcatbal | Automated |
| RECON-16 | Every `DAILYTRAN.DALYTRAN-CARD-NUM` exists in `CARDXREF.XREF-CARD-NUM` | Automated |
| RECON-17 | Every `TRANCATG.TRAN-TYPE-CD` exists in `TRANTYPE.TRAN-TYPE` | Automated |

### Running Data Load Checks

```bash
cd test-harness
python run_data_load_checks.py --data-dir ../app/data/ASCII
```

Expected output: all 9 checks pass.

---

## Batch Cycle Execution Order

The reconciliation checks should be executed following the standard batch cycle:

```
1. CLOSEFIL  -- Close CICS files (no data checks)
2. Data Load -- Load/refresh master files (RECON-09 through RECON-17)
3. POSTTRAN  -- Post daily transactions (RECON-01, RECON-02, RECON-03)
4. INTCALC   -- Interest calculation (RECON-04, RECON-05)
5. TRANBKP   -- Backup transaction master (RECON-08)
6. COMBTRAN  -- Combine transactions (RECON-07)
7. CREASTMT  -- Generate statements (RECON-06)
8. OPENFIL   -- Reopen CICS files (no data checks)
```

Each check should be run immediately after its corresponding job completes, before the next job in the cycle begins.
