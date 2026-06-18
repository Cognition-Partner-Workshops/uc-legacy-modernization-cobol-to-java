# CardDemo Batch Job Reconciliation Checks

This document describes each batch job in `app/jcl/`, what it reads and writes,
what reconciliation checks should pass after execution, and what business rules
it enforces.

---

## Data-Loading Jobs (IDCAMS/IEBGENER Utilities)

These jobs load flat files into VSAM KSDS clusters. Their reconciliation checks
are identical: the output VSAM must contain exactly the same records as the input
flat file.

### ACCTFILE — Refresh Account Master

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.PS` — flat file, FB LRECL=300 |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` — VSAM KSDS, KEY(11,0) |
| **Copybook** | `CVACT01Y` |

**Reconciliation Checks:**
- Record count: VSAM record count == flat file record count (50)
- Key uniqueness: all `ACCT-ID` values are unique
- Byte-for-byte: every record in VSAM matches the corresponding input record

---

### CARDFILE — Refresh Card Master

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.CARDDATA.PS` — flat file, FB LRECL=150 |
| **Writes** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` — VSAM KSDS, KEY(16,0) |
| **Copybook** | `CVACT02Y` |
| **Also creates** | Alternate index on `CARD-ACCT-ID` (key position 16, length 11) |

**Reconciliation Checks:**
- Record count: VSAM == 50
- Key uniqueness: all `CARD-NUM` values are unique
- AIX integrity: every `CARD-ACCT-ID` resolves via the alternate index

---

### CUSTFILE — Refresh Customer Master

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.CUSTDATA.PS` — flat file, FB LRECL=500 |
| **Writes** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` — VSAM KSDS, KEY(9,0) |
| **Copybook** | `CVCUS01Y` |

**Reconciliation Checks:**
- Record count: VSAM == 50
- Key uniqueness: all `CUST-ID` values are unique

---

### XREFFILE — Refresh Card Cross-Reference

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.PS` — flat file, FB LRECL=50 |
| **Writes** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` — VSAM KSDS, KEY(16,0) |
| **Copybook** | `CVACT03Y` |
| **Also creates** | Alternate index on `XREF-ACCT-ID` (key position 25, length 11) |

**Reconciliation Checks:**
- Record count: VSAM == 50
- Referential integrity:
  - Every `XREF-CARD-NUM` exists in `CARDDATA` as `CARD-NUM`
  - Every `XREF-ACCT-ID` exists in `ACCTDATA` as `ACCT-ID`
  - Every `XREF-CUST-ID` exists in `CUSTDATA` as `CUST-ID`

---

### DUSRSECJ — Load User Security File

| Attribute | Value |
|---|---|
| **Program** | IEBGENER (flat file creation from in-stream), then IDCAMS (REPRO) |
| **Reads** | In-stream data (10 user records) |
| **Writes** | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` — VSAM KSDS, KEY(8,0), LRECL=80 |
| **Copybook** | `CSUSR01Y` |

**Reconciliation Checks:**
- Record count: VSAM == 10 (5 admins + 5 users)
- Key uniqueness: all `SEC-USR-ID` values are unique
- Business rule: `SEC-USR-TYPE` is either `'A'` (admin) or `'U'` (user)

---

### DISCGRP — Load Disclosure Groups

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.DISCGRP.PS` — flat file, FB LRECL=50 |
| **Writes** | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` — VSAM KSDS, KEY(16,0) |
| **Copybook** | `CVTRA02Y` |

**Reconciliation Checks:**
- Record count: VSAM == 51
- Key uniqueness: composite key (`DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`)
- Business rule: `DIS-INT-RATE` is a valid numeric rate (≥ 0)

---

### TCATBALF — Refresh Transaction Category Balance

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.PS` — flat file, FB LRECL=50 |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` — VSAM KSDS, KEY(17,0) |
| **Copybook** | `CVTRA01Y` |

**Reconciliation Checks:**
- Record count: VSAM == 50
- Key uniqueness: composite key (`TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`)
- Referential integrity: every `TRANCAT-ACCT-ID` exists in `ACCTDATA`

---

### TRANCATG — Load Transaction Category Types

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.TRANCATG.PS` — flat file, FB LRECL=60 |
| **Writes** | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` — VSAM KSDS, KEY(6,0) |
| **Copybook** | `CVTRA04Y` |

**Reconciliation Checks:**
- Record count: VSAM == 18
- Key uniqueness: composite key (`TRAN-TYPE-CD` + `TRAN-CAT-CD`)

---

### TRANTYPE — Load Transaction Types

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.TRANTYPE.PS` — flat file, FB LRECL=60 |
| **Writes** | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` — VSAM KSDS, KEY(2,0) |
| **Copybook** | `CVTRA03Y` |

**Reconciliation Checks:**
- Record count: VSAM == 7
- Key uniqueness: all `TRAN-TYPE` values are unique

---

### TRANFILE — Load Initial Transaction Master

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO) |
| **Reads** | `AWS.M2.CARDDEMO.DALYTRAN.PS.INIT` — flat file, FB LRECL=350 |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` — VSAM KSDS, KEY(16,0) |
| **Copybook** | `CVTRA05Y` |
| **Also creates** | Alternate index on `TRAN-PROC-TS` (key position 304, length 26) |

**Reconciliation Checks:**
- Record count: VSAM == input flat file count
- Key uniqueness: all `TRAN-ID` values are unique

---

## Processing Jobs (COBOL Programs)

### POSTTRAN — Post Daily Transactions (`CBTRN02C`)

| Attribute | Value |
|---|---|
| **Program** | `CBTRN02C` |
| **Reads** | `DALYTRAN.PS` (daily transactions, CVTRA06Y, LRECL=350) |
| | `CARDXREF.VSAM.KSDS` (card cross-reference, CVACT03Y) |
| | `ACCTDATA.VSAM.KSDS` (account master, CVACT01Y) |
| | `TCATBALF.VSAM.KSDS` (transaction category balance, CVTRA01Y) |
| **Writes** | `TRANSACT.VSAM.KSDS` (posted transactions, CVTRA05Y) |
| | `DALYREJS(+1)` GDG (rejected transactions, LRECL=430 = 350 data + 80 trailer) |
| **Updates** | `ACCTDATA.VSAM.KSDS` (account balances: `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT`) |
| | `TCATBALF.VSAM.KSDS` (category balances: `TRAN-CAT-BAL`) |

**Reconciliation Checks:**
1. **Record count conservation**: `input_daily_count == posted_count + rejected_count`
2. **Amount conservation**: `SUM(DALYTRAN-AMT) == SUM(posted TRAN-AMT) + SUM(rejected DALYTRAN-AMT)`
3. **TCATBAL balance**: for each (ACCT-ID, TYPE-CD, CAT-CD), the post-run `TRAN-CAT-BAL` == pre-run balance + SUM of posted transaction amounts for that combination
4. **Account balance**: for each account, `post-ACCT-CURR-CYC-DEBIT` and `post-ACCT-CURR-CYC-CREDIT` reflect the net effect of posted transactions
5. **No orphan transactions**: every posted `TRAN-CARD-NUM` exists in `CARDXREF`
6. **Return code**: RC=4 if any rejects, RC=0 if none

**Business Rules Enforced:**
- **Card validation** (code 100): `DALYTRAN-CARD-NUM` must exist in `CARDXREF`
- **Account lookup** (code 101): the account referenced by the XREF must exist in `ACCTDATA`
- **Credit limit** (code 102): `ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT` must not exceed `ACCT-CREDIT-LIMIT`
- **Expiration** (code 103): transaction date (`DALYTRAN-ORIG-TS[1:10]`) must be on or before `ACCT-EXPIRAION-DATE`
- **Timestamp**: posted transactions get a system-generated `TRAN-PROC-TS` in DB2 timestamp format
- **TCATBAL creation**: if no TCATBAL record exists for a transaction's (acct, type, category) tuple, one is created

---

### INTCALC — Interest Calculation (`CBACT04C`)

| Attribute | Value |
|---|---|
| **Program** | `CBACT04C` |
| **Parameter** | Processing date (`PARM='YYYYMMDD00'`) |
| **Reads** | `TCATBALF.VSAM.KSDS` (sequential read of all category balances) |
| | `CARDXREF.VSAM.KSDS` (lookup via alternate index by account ID) |
| | `ACCTDATA.VSAM.KSDS` (account master for group ID and balance) |
| | `DISCGRP.VSAM.KSDS` (disclosure group for interest rates) |
| **Writes** | `SYSTRAN(+1)` GDG (system-generated interest transactions, CVTRA05Y, LRECL=350) |
| **Updates** | `ACCTDATA.VSAM.KSDS` (adds total interest to `ACCT-CURR-BAL`, resets cycle fields) |

**Reconciliation Checks:**
1. **One interest transaction per non-zero TCATBAL record**: count of system transactions == count of TCATBAL records with non-zero `DIS-INT-RATE` in their disclosure group
2. **Interest formula**: for each TCATBAL record, `interest = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200`
3. **Account balance update**: `post-ACCT-CURR-BAL == pre-ACCT-CURR-BAL + SUM(monthly_interest for account)`
4. **Cycle reset**: after processing, `ACCT-CURR-CYC-CREDIT = 0` and `ACCT-CURR-CYC-DEBIT = 0`
5. **Transaction ID format**: each system transaction ID = `PARM-DATE || sequence_number` (16 chars)
6. **Transaction metadata**: `TRAN-TYPE-CD = '01'`, `TRAN-CAT-CD = '05'`, `TRAN-SOURCE = 'System'`
7. **Sum of interest transactions**: `SUM(TRAN-AMT) == SUM((TRAN-CAT-BAL * DIS-INT-RATE) / 1200)` across all TCATBAL records

**Business Rules Enforced:**
- Interest is calculated as monthly rate: `annual_rate / 12 / 100`
- If the disclosure group record for an account's group+type+category is missing, the system falls back to the `DEFAULT` group
- Zero interest rate disclosure groups produce no interest transactions
- Each account is updated once after all its category balances are processed

---

### TRANBKP — Backup Transaction Master

| Attribute | Value |
|---|---|
| **Program** | IDCAMS (REPRO), then delete+redefine |
| **Reads** | `TRANSACT.VSAM.KSDS` |
| **Writes** | `TRANSACT.BKUP(+1)` GDG — sequential backup, FB LRECL=350 |
| **Then** | Deletes and redefines `TRANSACT.VSAM.KSDS` (empty cluster) |

**Reconciliation Checks:**
1. **Backup completeness**: record count in backup == record count in VSAM before delete
2. **Byte-for-byte**: every record in the backup matches the VSAM source
3. **Post-delete**: VSAM cluster is empty after redefine

---

### COMBTRAN — Combine Transaction Files

| Attribute | Value |
|---|---|
| **Program** | SORT (merge+sort), then IDCAMS (REPRO) |
| **Reads** | `TRANSACT.BKUP(0)` — latest transaction backup |
| | `SYSTRAN(0)` — latest system-generated transactions (from INTCALC) |
| **Writes** | `TRANSACT.COMBINED(+1)` GDG — sorted combined file, FB LRECL=350 |
| **Then loads** | `TRANSACT.VSAM.KSDS` via REPRO from the combined file |

**Reconciliation Checks:**
1. **Record count**: `combined_count == backup_count + systran_count`
2. **Sort order**: output is sorted ascending by `TRAN-ID` (bytes 1-16)
3. **No data loss**: every `TRAN-ID` from both inputs appears in the output
4. **No duplicates**: all `TRAN-ID` values in the combined output are unique
5. **VSAM load**: VSAM record count == combined file record count

---

### CREASTMT — Create Transaction Statements (`CBSTM03A`/`CBSTM03B`)

| Attribute | Value |
|---|---|
| **Program** | CBSTM03A (main), calls CBSTM03B (HTML subroutine) |
| **Reads** | `TRXFL.VSAM.KSDS` — sorted copy of transactions (card+tran ID key) |
| | `CARDXREF.VSAM.KSDS` — cross-reference for card→account→customer |
| | `ACCTDATA.VSAM.KSDS` — account details for statements |
| | `CUSTDATA.VSAM.KSDS` — customer names and addresses |
| **Writes** | `STATEMNT.PS` — text statement file, FB LRECL=80 |
| | `STATEMNT.HTML` — HTML statement file, FB LRECL=100 |
| **Pre-steps** | SORT: re-keys transactions by (CARD-NUM, TRAN-ID) into `TRXFL` |

**Reconciliation Checks:**
1. **Coverage**: at least one statement section per distinct card in `CARDXREF`
2. **Transaction totals**: the grand total in the report == `SUM(TRAN-AMT)` across all transaction records
3. **Account totals**: each account's reported total == sum of that account's transactions
4. **No missing data**: every transaction in `TRXFL` appears in the statement output
5. **Cross-reference resolution**: every card in the report has valid customer and account lookups

**Business Rules Enforced:**
- Transactions are grouped by card number, then by account
- Page totals, account totals, and grand total are computed and printed
- Both text and HTML output are generated from the same data pass

---

### CBEXPORT — Export Data for Branch Migration

| Attribute | Value |
|---|---|
| **Program** | `CBEXPORT` |
| **Reads** | `CUSTDATA.VSAM.KSDS`, `ACCTDATA.VSAM.KSDS`, `CARDXREF.VSAM.KSDS`, `TRANSACT.VSAM.KSDS`, `CARDDATA.VSAM.KSDS` |
| **Writes** | `EXPORT.DATA` — VSAM KSDS, KEY(4,28), LRECL=500, multi-record format |
| **Copybook** | `CVEXPORT` |

**Reconciliation Checks:**
1. **Total record count**: `export_count == cust_count + acct_count + card_count + xref_count + tran_count`
2. **Record type distribution**: count of each `EXPORT-REC-TYPE` matches the source file counts
3. **Round-trip**: exporting then importing should reproduce the original data
4. **Sequence numbers**: `EXPORT-SEQUENCE-NUM` is monotonically increasing

**Business Rules Enforced:**
- Record type byte (`EXPORT-REC-TYPE`) identifies the source entity
- Uses COMP/COMP-3 fields for numeric storage optimization
- REDEFINES structure maps the generic 460-byte data area to entity-specific layouts
- Branch ID and region code are populated for each exported record

---

### CBIMPORT — Import Data from Export File

| Attribute | Value |
|---|---|
| **Program** | `CBIMPORT` |
| **Reads** | `EXPORT.DATA` — multi-record export VSAM |
| **Writes** | `CUSTDATA.IMPORT` (FB LRECL=500), `ACCTDATA.IMPORT` (FB LRECL=300), `CARDXREF.IMPORT` (FB LRECL=50), `TRANSACT.IMPORT` (FB LRECL=350) |
| | `IMPORT.ERRORS` (FB LRECL=132) — error/reject file |

**Reconciliation Checks:**
1. **Record count**: `SUM(output file counts) + error_count == export_record_count`
2. **Type routing**: customer records → CUSTOUT, accounts → ACCTOUT, xrefs → XREFOUT, transactions → TRNXOUT
3. **Data fidelity**: imported records match original source data (round-trip verification)
4. **Error handling**: malformed or unrecognized record types appear in ERROUT

---

## Infrastructure Jobs

### CLOSEFIL / OPENFIL — CICS File Management

| Attribute | Value |
|---|---|
| **Program** | IEFBR14 (dummy step triggers CICS commands) |
| **Purpose** | Close/reopen VSAM files in the CICS region before/after batch |

**Reconciliation Checks:**
- No data checks; operational verification only (CICS file status)

---

### DEFGDGB / DEFGDGD — Define GDG Bases

| Attribute | Value |
|---|---|
| **Program** | IDCAMS |
| **Purpose** | Define Generation Data Group bases for versioned datasets |

**Reconciliation Checks:**
- GDG base exists with correct generation limit after execution

---

### TRANIDX — Define Alternate Index on Transaction File

| Attribute | Value |
|---|---|
| **Program** | IDCAMS |
| **Creates** | Alternate index on `TRANSACT.VSAM.KSDS` keyed by `TRAN-PROC-TS` (position 304, length 26) |

**Reconciliation Checks:**
- AIX build completes successfully
- AIX path is defined and accessible
- All records in base cluster are accessible via the AIX

---

### WAITSTEP — Batch Wait Timer

| Attribute | Value |
|---|---|
| **Program** | MVSWAIT (assembler) |
| **Purpose** | Introduce a configurable delay between batch steps |

**Reconciliation Checks:**
- None (timing utility only)

---

## Full Batch Cycle Reconciliation Summary

After running the complete batch sequence (CLOSEFIL → ACCTFILE → CARDFILE →
XREFFILE → CUSTFILE → TRANBKP → TRANCATG → TRANTYPE → DISCGRP → TCATBALF →
DUSRSECJ → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX →
OPENFIL), the following end-to-end checks must pass:

| Check | Formula |
|---|---|
| Transaction conservation | `daily_input + system_generated == final_transact_count + reject_count` |
| Amount conservation | `SUM(daily_AMT) + SUM(interest_AMT) == SUM(final_TRAN-AMT) + SUM(reject_AMT)` |
| Account balance integrity | `initial_BAL + interest - cycle_activity == final_BAL` per account |
| XREF integrity | All xref foreign keys still resolve after batch |
| Statement completeness | Every card with transactions has a statement section |
| Key uniqueness | No duplicate keys in any VSAM KSDS after batch |
| GDG versioning | New generations created for BKUP, SYSTRAN, COMBINED, DALYREJS |
