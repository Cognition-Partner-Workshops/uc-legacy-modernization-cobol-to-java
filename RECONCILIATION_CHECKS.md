# CardDemo Reconciliation Checks

Per-job validation specifications for the migration test harness.
Each section describes the reconciliation checks that must pass after
running the corresponding migrated batch job.

---

## Global Invariants (apply to every job)

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| G-1 | **Key uniqueness** | No duplicate primary keys in any output file | exact |
| G-2 | **Record-length conformance** | Every output record matches the copybook RECLN (trailing-filler truncation = WARNING) | exact |
| G-3 | **Referential integrity: XREF → CARD** | Every `XREF-CARD-NUM` in `cardxref` exists in `carddata.CARD-NUM` | exact |
| G-4 | **Referential integrity: XREF → ACCT** | Every `XREF-ACCT-ID` in `cardxref` exists in `acctdata.ACCT-ID` | exact |
| G-5 | **Transaction-type completeness** | Every `TRAN-TYPE-CD` used in transactions exists in `trantype.TRAN-TYPE` | exact |

Implementation: `test-harness/reconciliation.py → run_all_checks()`

---

## POSTTRAN — Daily Transaction Posting (CBTRN01C / CBTRN02C)

Posts records from `dailytran.txt` into the persistent transaction file,
updates account balances, and produces a reject file for invalid
transactions.

### Input Files
- `dailytran.txt` (CVTRA06Y / CVTRA05Y layout, RECLN 350)
- `ACCTFILE` (CVACT01Y, RECLN 300) — read/update
- `CARDFILE` (CVACT02Y, RECLN 150) — read
- `XREFFILE` (CVACT03Y, RECLN 50) — read
- `CUSTFILE` (CVCUS01Y, RECLN 500) — read
- `TRANFILE` (CVTRA05Y, RECLN 350) — write

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| PT-1 | **Row-count parity** | `count(dailytran) = count(posted) + count(rejected)` | exact |
| PT-2 | **Balance delta** | For each account: `ACCT-CURR-BAL(after) − ACCT-CURR-BAL(before) = Σ(TRAN-AMT for posted txns)` | ±0.01 |
| PT-3 | **Cycle credit/debit accumulation** | `ACCT-CURR-CYC-CREDIT(after) = ACCT-CURR-CYC-CREDIT(before) + Σ(credit txns)`; same for debit | ±0.01 |
| PT-4 | **Reject-file validity** | Every rejected record must have a corresponding entry in dailytran; reject reason must be populated | exact |
| PT-5 | **Card-number cross-reference** | Every `TRAN-CARD-NUM` in posted transactions resolves via `XREFFILE` to a valid `ACCT-ID` | exact |
| PT-6 | **Card-status validation** | Only transactions on active cards (`CARD-ACTIVE-STATUS = 'Y'`) are posted | exact |
| PT-7 | **Transaction-ID uniqueness** | No duplicate `TRAN-ID` in the output `TRANFILE` | exact |

---

## INTCALC — Interest Calculation (CBACT04C)

Calculates interest charges per account based on current balance,
disclosure-group rates, and transaction-category balances.

### Input Files
- `ACCTFILE` (CVACT01Y, RECLN 300) — read/update
- `TCATBALF` (CVTRA01Y, RECLN 50) — read
- `DISCGRP` (CVTRA02Y, RECLN 50) — read

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| IC-1 | **Interest formula** | For each `(acct, type, cat)`: `interest = TRAN-CAT-BAL × DIS-INT-RATE / 100 / 365 × days_in_period` | ±0.01 |
| IC-2 | **Disclosure-group coverage** | Every `ACCT-GROUP-ID` in `acctdata` has at least one matching `DIS-ACCT-GROUP-ID` in `discgrp` | exact |
| IC-3 | **Category-balance rollup** | Every `(TRANCAT-TYPE-CD, TRANCAT-CD)` in `tcatbal` has a matching `(TRAN-TYPE-CD, TRAN-CAT-CD)` in `trancatg` | exact |
| IC-4 | **Balance update** | `ACCT-CURR-BAL(after) = ACCT-CURR-BAL(before) + Σ(interest for all categories)` | ±0.01 |
| IC-5 | **Zero-balance skip** | Accounts with `TRAN-CAT-BAL = 0` for all categories generate no interest entry | exact |

---

## TRANBKP — Transaction Backup

Creates a sequential backup copy of the indexed transaction file.

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| TB-1 | **Row-count match** | `count(TRANFILE) = count(backup)` | exact |
| TB-2 | **Byte-exact copy** | Every record in backup matches the source TRANFILE byte-for-byte | exact |
| TB-3 | **Key ordering** | Backup records are in ascending `TRAN-ID` order | exact |

---

## COMBTRAN — Combine System + Daily Transactions

Merges system-generated transactions with the daily transaction file
into the main `TRANFILE`.

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| CT-1 | **Row-count sum** | `count(combined) = count(system_txns) + count(daily_txns)` | exact |
| CT-2 | **No lost records** | Every `TRAN-ID` from both inputs appears in the combined output | exact |
| CT-3 | **Sort order** | Combined file is in ascending `TRAN-ID` order | exact |
| CT-4 | **No duplicates** | No duplicate `TRAN-ID` in the combined output | exact |

---

## CREASTMT — Statement Generation (CBSTM03A / CBSTM03B)

Produces formatted account statements for all active accounts with
transactions in the current cycle.

### Input Files
- `TRANFILE` (CVTRA05Y, RECLN 350) — read
- `ACCTFILE` (CVACT01Y, RECLN 300) — read
- `CUSTFILE` (CVCUS01Y, RECLN 500) — read
- `XREFFILE` (CVACT03Y, RECLN 50) — read

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| ST-1 | **Statement completeness** | Every active account with ≥1 transaction in the cycle has a statement | exact |
| ST-2 | **Transaction coverage** | Σ(transactions listed per statement) = Σ(transactions in TRANFILE for that account) | exact |
| ST-3 | **Amount totals** | Statement total per account = Σ(`TRAN-AMT`) for that account's transactions | ±0.01 |
| ST-4 | **Customer data** | Statement header matches `CUSTFILE` name and address fields | exact |
| ST-5 | **Account data** | Statement shows correct `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT` | ±0.01 |

---

## CBTRN03C — Transaction Detail Report

Prints a formatted daily transaction report with type and category
descriptions, grouped by account.

### Input Files
- `TRANFILE` (CVTRA05Y) — read
- `TRANTYPE` (CVTRA03Y) — type descriptions
- `TRANCATG` (CVTRA04Y) — category descriptions
- Report layout: `CVTRA07Y`

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| TR-1 | **Transaction coverage** | Every transaction in `TRANFILE` appears in the report | exact |
| TR-2 | **Type-description fidelity** | `TRAN-REPORT-TYPE-DESC` matches `TRAN-TYPE-DESC` from `trantype` for the same `TRAN-TYPE-CD` | exact |
| TR-3 | **Category-description fidelity** | `TRAN-REPORT-CAT-DESC` matches `TRAN-CAT-TYPE-DESC` from `trancatg` for the same `(TRAN-TYPE-CD, TRAN-CAT-CD)` | exact |
| TR-4 | **Page totals** | Each page total = Σ(`TRAN-AMT`) for transactions on that page | ±0.01 |
| TR-5 | **Account totals** | Each account total = Σ(`TRAN-AMT`) for that account's transactions | ±0.01 |
| TR-6 | **Amount formatting** | Report amount matches PIC `-ZZZ,ZZZ,ZZZ.ZZ` format | exact |

---

## CBEXPORT — Branch Migration Export

Exports all VSAM data into a single sequential file using the
multi-record CVEXPORT copybook layout (COMP, COMP-3, REDEFINES, OCCURS).

### Input Files
- All VSAM files (accounts, cards, customers, transactions, xrefs)

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| EX-1 | **Record-type distribution** | Count of each `EXPORT-REC-TYPE` matches source file row counts | exact |
| EX-2 | **COMP round-trip** | `EXP-ACCT-CURR-CYC-DEBIT` (COMP) decoded = original `ACCT-CURR-CYC-DEBIT` | ±0.01 |
| EX-3 | **COMP-3 round-trip** | `EXP-ACCT-CURR-BAL` (COMP-3) decoded = original `ACCT-CURR-BAL` | ±0.01 |
| EX-4 | **OCCURS fidelity** | All 3 `EXP-CUST-ADDR-LINE` entries and both `EXP-CUST-PHONE-NUM` entries present | exact |
| EX-5 | **Sequence numbering** | `EXPORT-SEQUENCE-NUM` is monotonically increasing | exact |
| EX-6 | **Total record length** | Every export record = 500 bytes (per CVEXPORT.cpy) | exact |

---

## CBIMPORT — Branch Migration Import

Reads the sequential export file and loads records back into VSAM files.

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| IM-1 | **Round-trip parity** | After export→import, every VSAM file matches its pre-export state | exact |
| IM-2 | **Row counts** | `count(imported.acctdata) = count(original.acctdata)` (and same for card, cust, xref, tran) | exact |
| IM-3 | **Field-level match** | Every field in every record matches (after COMP/COMP-3 decode) | ±0.01 for monetary |

---

## File-Loader Jobs (ACCTFILE, CARDFILE, XREFFILE, CUSTFILE, TRANFILE, etc.)

These jobs load source data into VSAM from sequential inputs.

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| FL-1 | **Row-count match** | `count(loaded VSAM) = count(input sequential file)` | exact |
| FL-2 | **Key ordering** | Loaded file maintains ascending primary-key order | exact |
| FL-3 | **No data loss** | Every input record appears in the output with identical field values | exact |

---

## Lookup-Table Loaders (TRANCATG, TRANTYPE, DISCGRP, TCATBALF)

### Reconciliation Checks

| # | Check | Formula / Rule | Tolerance |
|---|-------|----------------|-----------|
| LT-1 | **Row-count match** | `count(loaded) = count(input)` | exact |
| LT-2 | **Composite-key uniqueness** | No duplicate composite keys after load | exact |
| LT-3 | **Description fidelity** | All description fields match input exactly | exact |

---

## Summary Matrix

| Check ID | Job(s) | Automated | Implementation |
|----------|--------|-----------|----------------|
| G-1..G-5 | All | ✓ | `reconciliation.py → run_all_checks()` |
| PT-1..PT-7 | POSTTRAN | ✓ (partial — full requires pre/post state) | `reconciliation.py → check_balance_integrity()` |
| IC-1..IC-5 | INTCALC | ✓ (IC-2, IC-3 automated) | `reconciliation.py → check_disclosure_group_coverage(), check_category_balance_rollup()` |
| TB-1..TB-3 | TRANBKP | ✓ (via comparator) | `comparator.py → compare_record_sets()` |
| CT-1..CT-4 | COMBTRAN | ✓ (via comparator) | `comparator.py → compare_record_sets()` |
| ST-1..ST-5 | CREASTMT | Spec only (report parsing TBD) | — |
| TR-1..TR-6 | CBTRN03C | Spec only (report parsing TBD) | — |
| EX-1..EX-6 | CBEXPORT | Spec only (COMP/COMP-3 decode TBD) | — |
| IM-1..IM-3 | CBIMPORT | Spec only (round-trip TBD) | — |
| FL-1..FL-3 | File loaders | ✓ (via comparator + contract) | `comparator.py`, `contract_validator.py` |
| LT-1..LT-3 | Lookup loaders | ✓ | `reconciliation.py → check_key_uniqueness()` |
