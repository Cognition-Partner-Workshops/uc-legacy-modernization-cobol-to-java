# Reconciliation Checks

## Overview

This document specifies per-job validation rules for the CardDemo batch processing pipeline. Each batch job has pre-conditions, post-conditions, and cross-file reconciliation invariants that must hold after migration to Java.

---

## Job: POSTTRAN (Post Daily Transactions)

**JCL**: `POSTTRAN.jcl`  
**Program**: `CBTRN02C`  
**Purpose**: Process daily transaction file, update transaction master VSAM, and maintain category balances.

### Input Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| DALYTRAN | `CARDDEMO.DALYTRAN.PS` | CVTRA05Y (350 bytes) |
| XREFFILE | `CARDDEMO.CARDXREF.VSAM.KSDS` | CVACT03Y (50 bytes) |
| ACCTFILE | `CARDDEMO.ACCTDATA.VSAM.KSDS` | CVACT01Y (300 bytes) |
| TCATBALF | `CARDDEMO.TCATBALF.VSAM.KSDS` | CVTRA01Y (50 bytes) |
| TRANFILE | `CARDDEMO.TRANSACT.VSAM.KSDS` | CVTRA05Y (350 bytes) |

### Output Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| TRANFILE | Updated transaction master | CVTRA05Y (350 bytes) |
| ACCTFILE | Updated account records | CVACT01Y (300 bytes) |
| TCATBALF | Updated category balances | CVTRA01Y (50 bytes) |
| DALYREJS | Rejected transactions | Custom (430 bytes) |

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| POSTTRAN-PRE-001 | All cards in DALYTRAN exist in XREFFILE | BLOCK |
| POSTTRAN-PRE-002 | All transaction type codes in DALYTRAN exist in TRANTYPE | BLOCK |
| POSTTRAN-PRE-003 | DALYTRAN file is non-empty | WARN |
| POSTTRAN-PRE-004 | ACCTFILE accounts referenced by XREF are active (status = 'Y') | WARN |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| POSTTRAN-POST-001 | For each processed transaction: `ACCT-CURR-BAL(after) = ACCT-CURR-BAL(before) + TRAN-AMT` (debits positive, credits negative) | BLOCK |
| POSTTRAN-POST-002 | For each processed transaction: corresponding TRAN-CAT-BAL updated by TRAN-AMT | BLOCK |
| POSTTRAN-POST-003 | Each processed transaction appears in TRANFILE with TRAN-PROC-TS populated | BLOCK |
| POSTTRAN-POST-004 | Rejected transactions written to DALYREJS with rejection reason | BLOCK |
| POSTTRAN-POST-005 | `count(DALYTRAN) = count(processed) + count(DALYREJS)` | BLOCK |
| POSTTRAN-POST-006 | Sum of all TRAN-AMT for processed transactions = net change in aggregate ACCT-CURR-BAL | BLOCK |

### Reconciliation Invariants

```
SUM(TRAN-CAT-BAL) WHERE TRANCAT-ACCT-ID = X 
  == ACCT-CURR-BAL WHERE ACCT-ID = X
  (after POSTTRAN completes, tolerance ±0.01)

COUNT(TRANFILE after) == COUNT(TRANFILE before) + COUNT(processed from DALYTRAN)
```

---

## Job: INTCALC (Interest Calculation)

**JCL**: `INTCALC.jcl`  
**Program**: `CBACT04C`  
**Purpose**: Compute interest charges and fees based on transaction category balances and disclosure group rates.

### Input Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| TCATBALF | `CARDDEMO.TCATBALF.VSAM.KSDS` | CVTRA01Y (50 bytes) |
| XREFFILE | `CARDDEMO.CARDXREF.VSAM.KSDS` | CVACT03Y (50 bytes) |
| XREFFIL1 | `CARDDEMO.CARDXREF.VSAM.AIX.PATH` | CVACT03Y (50 bytes) |
| ACCTFILE | `CARDDEMO.ACCTDATA.VSAM.KSDS` | CVACT01Y (300 bytes) |
| DISCGRP | `CARDDEMO.DISCGRP.VSAM.KSDS` | CVTRA02Y (50 bytes) |

### Output Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| TRANSACT | Interest/fee transactions | CVTRA05Y (350 bytes) |
| ACCTFILE | Updated account balances | CVACT01Y (300 bytes) |
| TCATBALF | Updated category balances | CVTRA01Y (50 bytes) |

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| INTCALC-PRE-001 | PARM date is valid format (YYYYMMDDXX) | BLOCK |
| INTCALC-PRE-002 | DISCGRP has rate entries for all account group + type combinations | WARN |
| INTCALC-PRE-003 | All accounts in TCATBALF exist in ACCTFILE | BLOCK |
| INTCALC-PRE-004 | TCATBALF has non-zero balances to process | WARN |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| INTCALC-POST-001 | For each account with balance: interest transaction generated where `interest_amt = TRAN-CAT-BAL * DIS-INT-RATE / 1200` (monthly) | BLOCK |
| INTCALC-POST-002 | Generated interest transactions have TRAN-TYPE-CD matching the category type | BLOCK |
| INTCALC-POST-003 | ACCT-CURR-BAL updated by sum of generated interest amounts | BLOCK |
| INTCALC-POST-004 | No interest generated for zero-balance categories | BLOCK |
| INTCALC-POST-005 | All generated transactions have TRAN-PROC-TS = PARM date | BLOCK |

### Reconciliation Invariants

```
For each account X:
  SUM(interest_tran.TRAN-AMT) WHERE account = X
    == ACCT-CURR-BAL(after) - ACCT-CURR-BAL(before) WHERE ACCT-ID = X

interest_amt = TRAN-CAT-BAL * rate / 1200
  WHERE rate = DIS-INT-RATE 
  FROM DISCGRP 
  WHERE DIS-ACCT-GROUP-ID = ACCT-GROUP-ID 
    AND DIS-TRAN-TYPE-CD = TRANCAT-TYPE-CD
    AND DIS-TRAN-CAT-CD = TRANCAT-CD
```

---

## Job: TRANREPT (Transaction Report)

**JCL**: `TRANREPT.jcl`  
**Program**: `CBTRN03C` (report generation step)  
**Purpose**: Produce formatted daily transaction report filtered by date range, sorted by card number.

### Input Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| TRANFILE | `CARDDEMO.TRANSACT.VSAM.KSDS` | CVTRA05Y (350 bytes) |
| XREFFILE | `CARDDEMO.CARDXREF.VSAM.KSDS` | CVACT03Y (50 bytes) |
| TRTEFIL | Transaction type reference | CVTRA03Y (60 bytes) |
| TCTEFIL | Transaction category reference | CVTRA04Y (60 bytes) |

### Output Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| RPTFILE | Formatted report | CVTRA07Y (133 bytes/line) |

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| TRANREPT-PRE-001 | PARM date range is valid (start ≤ end) | BLOCK |
| TRANREPT-PRE-002 | TRANFILE contains transactions within date range | WARN |
| TRANREPT-PRE-003 | TRTEFIL and TCTEFIL contain reference data for all codes in TRANFILE | WARN |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| TRANREPT-POST-001 | Report contains one line per transaction within date range | BLOCK |
| TRANREPT-POST-002 | Transactions sorted by TRAN-CARD-NUM within report | BLOCK |
| TRANREPT-POST-003 | Page totals = sum of transaction amounts on that page | BLOCK |
| TRANREPT-POST-004 | Account totals = sum of all transactions for that account | BLOCK |
| TRANREPT-POST-005 | Grand total = sum of all account totals | BLOCK |
| TRANREPT-POST-006 | Report header contains correct date range | BLOCK |
| TRANREPT-POST-007 | No transactions outside date range appear in report | BLOCK |

### Reconciliation Invariants

```
COUNT(report lines) == COUNT(TRANFILE records WHERE TRAN-PROC-TS IN date_range)

SUM(report amounts) == SUM(TRAN-AMT WHERE TRAN-PROC-TS IN date_range)

For each card C:
  Account_Total(C) == SUM(TRAN-AMT WHERE TRAN-CARD-NUM = C AND TRAN-PROC-TS IN range)
```

---

## Job: CBEXPORT (Data Export for Branch Migration)

**JCL**: `CBEXPORT.jcl`  
**Program**: `CBEXPORT`  
**Purpose**: Export all customer, account, card, transaction, and xref data into a multi-record sequential file for branch migration.

### Input Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| CUSTFILE | `CARDDEMO.CUSTDATA.VSAM.KSDS` | CVCUS01Y (500 bytes) |
| ACCTFILE | `CARDDEMO.ACCTDATA.VSAM.KSDS` | CVACT01Y (300 bytes) |
| XREFFILE | `CARDDEMO.CARDXREF.VSAM.KSDS` | CVACT03Y (50 bytes) |
| TRANSACT | `CARDDEMO.TRANSACT.VSAM.KSDS` | CVTRA05Y (350 bytes) |
| CARDFILE | `CARDDEMO.CARDDATA.VSAM.KSDS` | CVACT02Y (150 bytes) |

### Output Files

| DD Name | Dataset | Layout |
|---------|---------|--------|
| EXPFILE | `CARDDEMO.EXPORT.DATA` | CVEXPORT (500 bytes) |

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| CBEXPORT-PRE-001 | All input VSAM files are accessible and non-empty | BLOCK |
| CBEXPORT-PRE-002 | Export VSAM cluster is defined and empty | WARN |
| CBEXPORT-PRE-003 | Referential integrity holds across input files (RC-001, RC-002, RC-010) | BLOCK |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| CBEXPORT-POST-001 | Export record count = sum of all input record counts | BLOCK |
| CBEXPORT-POST-002 | EXPORT-REC-TYPE correctly identifies record type ('C'=Customer, 'A'=Account, 'T'=Transaction, 'X'=Xref, 'D'=Card) | BLOCK |
| CBEXPORT-POST-003 | All export records are 500 bytes (RECLN 500) | BLOCK |
| CBEXPORT-POST-004 | EXPORT-SEQUENCE-NUM is sequential within each record type | BLOCK |
| CBEXPORT-POST-005 | EXPORT-TIMESTAMP is populated with valid ISO timestamp | BLOCK |
| CBEXPORT-POST-006 | Customer data in export matches source CUSTFILE records | BLOCK |
| CBEXPORT-POST-007 | Account data in export matches source ACCTFILE records | BLOCK |
| CBEXPORT-POST-008 | No data loss: every source record appears exactly once in export | BLOCK |

### Reconciliation Invariants

```
COUNT(EXPFILE WHERE REC-TYPE='C') == COUNT(CUSTFILE)
COUNT(EXPFILE WHERE REC-TYPE='A') == COUNT(ACCTFILE)
COUNT(EXPFILE WHERE REC-TYPE='T') == COUNT(TRANSACT)
COUNT(EXPFILE WHERE REC-TYPE='X') == COUNT(XREFFILE)
COUNT(EXPFILE WHERE REC-TYPE='D') == COUNT(CARDFILE)

For each customer record in EXPFILE:
  EXP-CUST-ID, EXP-CUST-FIRST-NAME, EXP-CUST-LAST-NAME 
    == CUST-ID, CUST-FIRST-NAME, CUST-LAST-NAME FROM CUSTFILE
```

---

## Job: COMBTRAN (Combine Transactions)

**JCL**: `COMBTRAN.jcl`  
**Program**: Utility (SORT/MERGE)  
**Purpose**: Combine multiple daily transaction files into the master transaction file.

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| COMBTRAN-PRE-001 | All input transaction files have RECLN 350 | BLOCK |
| COMBTRAN-PRE-002 | No duplicate TRAN-ID values across input files | WARN |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| COMBTRAN-POST-001 | Output record count = sum of all input record counts | BLOCK |
| COMBTRAN-POST-002 | Output file is sorted by TRAN-CARD-NUM | BLOCK |
| COMBTRAN-POST-003 | No records lost or duplicated during merge | BLOCK |

---

## Job: CREASTMT (Create Statements)

**JCL**: `CREASTMT.JCL`  
**Program**: Statement generation  
**Purpose**: Generate customer statements from transaction data.

### Pre-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| CREASTMT-PRE-001 | ACCTFILE and CUSTFILE are synchronized (all accounts have customers) | BLOCK |
| CREASTMT-PRE-002 | Statement period dates are valid | BLOCK |

### Post-Conditions

| Check | Rule | Severity |
|-------|------|----------|
| CREASTMT-POST-001 | One statement generated per active account | BLOCK |
| CREASTMT-POST-002 | Statement balance = ACCT-CURR-BAL for that account | BLOCK |
| CREASTMT-POST-003 | Statement lists all transactions for the account within period | BLOCK |

---

## Cross-Job Reconciliation (Pipeline-Level)

These checks validate consistency across the entire batch processing pipeline.

### End-of-Day Pipeline

```
POSTTRAN → INTCALC → TRANREPT → CREASTMT
```

| Check ID | Rule | After Job |
|----------|------|-----------|
| PIPE-001 | `ACCT-CURR-BAL(end) = ACCT-CURR-BAL(start) + net_transactions + interest_charges` | INTCALC |
| PIPE-002 | All processed DALYTRAN records appear in TRANREPT output | TRANREPT |
| PIPE-003 | Statement balances match final ACCT-CURR-BAL | CREASTMT |
| PIPE-004 | No records in TRANFILE with empty TRAN-PROC-TS after POSTTRAN | POSTTRAN |
| PIPE-005 | DALYREJS + processed count = original DALYTRAN count | POSTTRAN |

### Data Integrity Invariants (Must Hold At All Times)

| Check ID | Invariant |
|----------|-----------|
| INV-001 | Every XREF-CARD-NUM in CARDXREF exists in CARDDATA |
| INV-002 | Every XREF-ACCT-ID in CARDXREF exists in ACCTDATA |
| INV-003 | Every XREF-CUST-ID in CARDXREF exists in CUSTDATA |
| INV-004 | ACCT-CURR-BAL never exceeds ACCT-CREDIT-LIMIT (post all processing) |
| INV-005 | Active accounts (status='Y') have valid expiration dates > today |
| INV-006 | No duplicate ACCT-ID in ACCTDATA |
| INV-007 | No duplicate CUST-ID in CUSTDATA |
| INV-008 | No duplicate TRAN-ID in TRANSACT |

---

## Running Reconciliation Checks

### All Checks
```bash
python -m test-harness.reconciliation --data-dir app/data/ASCII --checks all
```

### Specific Checks
```bash
python -m test-harness.reconciliation --data-dir app/data/ASCII --checks RC-001 RC-002 RC-004
```

### With JSON Report Output
```bash
python -m test-harness.reconciliation --data-dir app/data/ASCII --output report.json
```

### Post-Job Validation (Example)
```bash
# After POSTTRAN runs on migrated Java system:
python -m test-harness.reconciliation \
  --data-dir output/post-posttran/ \
  --checks RC-001 RC-003 RC-004 RC-005 RC-006
```

---

## Severity Levels

| Level | Meaning | Action |
|-------|---------|--------|
| BLOCK | Migration cannot proceed if violated | Fix required before promotion |
| WARN | Known acceptable deviation in initial data | Document and monitor |

## Status Codes

| Code | Meaning |
|------|---------|
| PASS | Check passed with no violations |
| FAIL | Check failed with blocking violations |
| WARN | Check found non-blocking issues |
| SKIP | Check could not run (missing data) |
