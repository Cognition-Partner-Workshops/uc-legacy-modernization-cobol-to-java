# CardDemo Reconciliation Checks

Per-job validation specifications for the CardDemo mainframe-to-Java migration.
Each check is tied to a specific batch job (JCL) and verifies a domain invariant
that must hold after the job completes.

---

## Job: POSTTRAN — Post Daily Transactions (`CBTRN02C`)

**JCL:** `app/jcl/POSTTRAN.jcl`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| P1 | Row-Count Balance | `count(DALYTRAN) == count(posted to TRANFILE) + count(DALYREJS)` | `dailytran.txt`, transaction VSAM output, rejects GDG | `reconciliation.check_row_count_balance` |
| P2 | No Duplicate Transactions | Every `TRAN-ID` in the posted output is unique | Transaction VSAM output | Distinct-count assertion on `TRAN-ID` |
| P3 | Account Balance Updated | For each posted transaction, `ACCT-CURR-BAL` on the account changes by exactly `TRAN-AMT` (debit) or `-TRAN-AMT` (credit depending on type) | Pre/post `acctdata`, posted transactions | Delta comparison per account |
| P4 | Category Balance Updated | For each posted transaction, the matching `TRAN-CAT-BAL` row in `tcatbal` is incremented by `TRAN-AMT` | Pre/post `tcatbal.txt`, posted transactions | Delta comparison per `(ACCT-ID, TYPE-CD, CAT-CD)` |
| P5 | Reject Reason Present | Every record in `DALYREJS` contains a non-blank reason field | Rejects GDG | Non-empty assertion on reject reason bytes |
| P6 | Referential Completeness | Every `DALYTRAN-CARD-NUM` either maps to an `XREF-CARD-NUM` or appears in rejects | `dailytran.txt`, `cardxref.txt`, rejects | `reconciliation.check_referential_completeness` |

---

## Job: INTCALC — Interest Calculation (`CBACT04C`)

**JCL:** `app/jcl/INTCALC.jcl`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| I1 | Interest Rate Applied | For every account with a positive `TRAN-CAT-BAL`, a system transaction is generated with `TRAN-TYPE-CD` matching the interest transaction type and amount = `balance × rate / 365` (within tolerance) | `tcatbal.txt`, `discgrp.txt`, system transaction output | Computed assertion per `(ACCT-GROUP-ID, TRAN-TYPE-CD, TRAN-CAT-CD)` |
| I2 | Rate Lookup Consistency | The interest rate used equals `DIS-INT-RATE` from `discgrp` for the account's group and transaction category | `acctdata.txt`, `discgrp.txt`, generated transactions | Cross-file lookup validation |
| I3 | No Interest on Zero Balance | Accounts where all `TRAN-CAT-BAL` entries are zero produce no interest transactions | `tcatbal.txt`, system transaction output | Absence assertion |
| I4 | System Transaction Output | Every generated interest transaction has `TRAN-SOURCE = 'SYSTEM'` and a valid `TRAN-ORIG-TS` | System transaction output | Field-value assertion |
| I5 | Account Update | `ACCT-CURR-BAL` is updated by the sum of all interest transactions for that account | Pre/post `acctdata.txt`, system transactions | Delta comparison per account |

---

## Job: TRANREPT — Transaction Report (`CBTRN03C`)

**JCL:** `app/jcl/TRANREPT.jcl`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| R1 | Report Grand Total | The `Grand Total` line in the report equals the sum of all `TRAN-AMT` values in the filtered transaction input | Transaction input (filtered by date), report output | Aggregate sum comparison |
| R2 | Account Totals | Each `Account Total` line equals the sum of transaction amounts for that account | Report output, transaction input | Per-account sum comparison |
| R3 | Page Totals | Each `Page Total` line equals the sum of detail-line amounts on that page | Report output (self-consistent) | Per-page sum validation |
| R4 | Transaction Count | Number of detail lines in the report = number of transactions in the filtered input | Transaction input, report output | Row-count comparison |
| R5 | Type/Category Descriptions | `TRAN-REPORT-TYPE-DESC` and `TRAN-REPORT-CAT-DESC` match the descriptions in `trantype.txt` and `trancatg.txt` | Report output, `trantype.txt`, `trancatg.txt` | Lookup validation |
| R6 | Date Range Filter | No transaction in the report has a `TRAN-PROC-TS` outside the configured date range | Report output, DATEPARM | Range-bound assertion |

---

## Job: CREASTMT — Statement Generation (`CBSTM03A`)

**JCL:** `app/jcl/CREASTMT.JCL`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| S1 | One Statement Per Card | The number of statement sections equals the number of distinct cards in `cardxref` that have transactions | `cardxref.txt`, transactions, statement output | Count comparison |
| S2 | Statement Balance | Each statement's balance total equals the sum of `TRAN-AMT` for that card | Statement output, transactions | Per-card sum comparison |
| S3 | Customer Data Correct | Customer name and address in each statement match `custdata.txt` for the associated `XREF-CUST-ID` | Statement output, `custdata.txt`, `cardxref.txt` | Field-value cross-reference |
| S4 | Account Data Correct | Account number and credit limit in the statement header match `acctdata.txt` | Statement output, `acctdata.txt` | Field-value cross-reference |
| S5 | HTML Output Present | An HTML statement file is generated alongside the text statement | Statement HTML output | File-existence check |

---

## Job: CBEXPORT — Data Export (`CBEXPORT`)

**JCL:** `app/jcl/CBEXPORT.jcl`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| E1 | Record Count | `count(export records) == count(customers) + count(accounts) + count(xrefs) + count(transactions) + count(cards)` | All input files, export file | Sum-of-counts comparison |
| E2 | Record Type Distribution | Export file contains the correct number of each record type (`C`=customer, `A`=account, `X`=xref, `T`=transaction, `D`=card) | Export file, input files | Per-type count comparison |
| E3 | Customer Data Fidelity | Customer fields in export records match the source `custdata.txt` | Export file, `custdata.txt` | Field-level comparison (accounting for COMP-3/COMP encoding) |
| E4 | Account Data Fidelity | Account fields in export records match `acctdata.txt` | Export file, `acctdata.txt` | Field-level comparison |
| E5 | Sequence Numbering | `EXPORT-SEQUENCE-NUM` values are contiguous within each record type | Export file | Monotonicity assertion |
| E6 | Timestamp Consistency | All `EXPORT-TIMESTAMP` values fall within the job execution window | Export file | Range-bound assertion |

---

## Job: CBIMPORT — Data Import (`CBIMPORT`)

**JCL:** `app/jcl/CBIMPORT.jcl`

| # | Check | Assertion | Inputs | Implementation |
|---|-------|-----------|--------|----------------|
| M1 | Round-Trip Fidelity | Importing the export file produces normalized files identical to the originals (customer, account, xref, transaction) | Export file, original VSAM data | Field-level comparison of import output vs source files |
| M2 | Error File Empty | The `ERROUT` file is empty when the export file is well-formed | Import error output | File-size = 0 assertion |
| M3 | Record Length Compliance | Each output file has the correct record length (`CUSTOUT`=500, `ACCTOUT`=300, `XREFOUT`=50, `TRNXOUT`=350) | Import output files | Contract validation per file |
| M4 | No Data Loss | `count(import output records)` per type == `count(export input records)` per type | Export file, import output files | Per-type count comparison |
| M5 | Referential Integrity Post-Import | After import, all cross-reference, card-account, and customer linkages are valid | Import output files | `reconciliation.check_xref_integrity`, `check_card_account_linkage`, `check_customer_xref_linkage` |

---

## Cross-Job Referential Integrity Checks

These checks validate data consistency across the entire dataset, independent of
any specific job execution.

| # | Check | Assertion | Implementation |
|---|-------|-----------|----------------|
| X1 | Cross-Reference Integrity | Every `XREF-CARD-NUM` exists in `carddata` and every `XREF-ACCT-ID` exists in `acctdata` | `reconciliation.check_xref_integrity` |
| X2 | Card → Account Linkage | Every `CARD-ACCT-ID` references a valid `ACCT-ID` in `acctdata` | `reconciliation.check_card_account_linkage` |
| X3 | Cross-Reference → Customer | Every `XREF-CUST-ID` references a valid `CUST-ID` in `custdata` | `reconciliation.check_customer_xref_linkage` |
| X4 | Account Balance vs Category Balance | `sum(TRAN-CAT-BAL) for account` ≈ `ACCT-CURR-BAL` (after POSTTRAN and INTCALC have run) | `reconciliation.check_account_balance_vs_tcatbal` |
| X5 | Transaction Type Validity | Every `DALYTRAN-TYPE-CD` exists in `trantype.txt` | Lookup validation |
| X6 | Transaction Category Validity | Every `(DALYTRAN-TYPE-CD, DALYTRAN-CAT-CD)` pair exists in `trancatg.txt` | Composite-key lookup validation |

---

## Running Reconciliation Checks

```bash
# Run all cross-job integrity checks against golden-file data:
python test-harness/reconciliation.py

# Expected output: per-check PASS/FAIL with detail on failures.
# Note: Check X4 (balance vs category balance) is expected to FAIL on seed
# data because the tcatbal file is initialized with zero balances (pre-batch
# state). It should PASS after POSTTRAN has been executed.
```
