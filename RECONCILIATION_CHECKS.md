# CardDemo Reconciliation Checks

Per-job validation specifications for the CardDemo batch migration.
Each check must pass after the corresponding Java batch job completes.

---

## Notation

| Symbol | Meaning |
|---|---|
| **SUM(field)** | Sum of a numeric field across all records in a dataset |
| **COUNT(dataset)** | Number of records in a dataset |
| **LOOKUP(key, dataset)** | Record retrieval by key from a dataset |
| **PRE(dataset)** | Dataset state before job execution |
| **POST(dataset)** | Dataset state after job execution |

---

## 1. POSTTRAN - Daily Transaction Posting

**COBOL Program:** `CBTRN02C`
**JCL:** `POSTTRAN.jcl`

**Inputs:**
| Dataset | Copybook | Key |
|---|---|---|
| DALYTRAN (daily transactions) | CVTRA05Y | DALYTRAN-ID |
| XREFFILE (card cross-reference) | CVACT03Y | XREF-CARD-NUM |
| ACCTFILE (account master) | CVACT01Y | ACCT-ID |
| TCATBALF (category balances) | CVTRA01Y | TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD |

**Outputs:**
| Dataset | Copybook | Key |
|---|---|---|
| TRANFILE (transaction master) | CVTRA05Y | TRAN-ID |
| DALYREJS (rejected transactions) | - | - |
| ACCTFILE (updated accounts) | CVACT01Y | ACCT-ID |
| TCATBALF (updated balances) | CVTRA01Y | TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD |

### Reconciliation Checks

#### 1.1 Record-Count Balance
```
COUNT(DALYTRAN) == COUNT(accepted_to_TRANFILE) + COUNT(DALYREJS)
```
Every input daily transaction must either be posted to the transaction master
or written to the rejects file. No records may be silently dropped.

#### 1.2 Transaction Amount Conservation
```
SUM(DALYTRAN.TRAN-AMT) == SUM(accepted.TRAN-AMT) + SUM(DALYREJS.TRAN-AMT)
```
The total monetary value of input transactions must equal the sum of
posted plus rejected amounts.

#### 1.3 Account Balance Update
```
For each account A where transactions were posted:
  POST(ACCTFILE[A].ACCT-CURR-BAL)
    == PRE(ACCTFILE[A].ACCT-CURR-BAL) + SUM(posted_transactions_for_A.TRAN-AMT)
```
Each account's current balance must reflect the net of all posted transactions.

#### 1.4 Cycle Debit/Credit Update
```
For each account A:
  POST(ACCTFILE[A].ACCT-CURR-CYC-DEBIT) >= PRE(ACCTFILE[A].ACCT-CURR-CYC-DEBIT)
  POST(ACCTFILE[A].ACCT-CURR-CYC-CREDIT) >= PRE(ACCTFILE[A].ACCT-CURR-CYC-CREDIT)
```
Cycle totals are cumulative within a billing cycle; they must not decrease.

#### 1.5 Category Balance Update
```
For each (account, type, category) triple:
  POST(TCATBALF[key].TRAN-CAT-BAL)
    == PRE(TCATBALF[key].TRAN-CAT-BAL) + SUM(posted_transactions_for_key.TRAN-AMT)
```
Category balances must reflect the posted transaction amounts.

#### 1.6 Card-to-Account Resolution
```
For each accepted transaction T:
  LOOKUP(T.TRAN-CARD-NUM, XREFFILE) must return a valid XREF record
  XREFFILE[T.TRAN-CARD-NUM].XREF-ACCT-ID must exist in ACCTFILE
```
Every posted transaction must trace back to a valid card -> account linkage.

#### 1.7 Reject Reason Completeness
```
For each record R in DALYREJS:
  R must contain the original DALYTRAN fields plus a reject-reason indicator
```
Rejected records must be preserved with an explanation of why they were rejected.

---

## 2. INTCALC - Interest Calculation

**COBOL Program:** `CBACT04C`
**JCL:** `INTCALC.jcl`

**Inputs:**
| Dataset | Copybook | Key |
|---|---|---|
| TCATBALF (category balances) | CVTRA01Y | TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD |
| XREFFILE (card cross-reference) | CVACT03Y | XREF-CARD-NUM |
| ACCTFILE (account master) | CVACT01Y | ACCT-ID |
| DISCGRP (discount/rate groups) | CVTRA02Y | DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD |

**Outputs:**
| Dataset | Copybook | Key |
|---|---|---|
| TRANSACT (system-generated interest transactions) | CVTRA05Y | TRAN-ID |

### Reconciliation Checks

#### 2.1 Interest Applied to Non-Zero Balances Only
```
For each account A:
  IF PRE(ACCTFILE[A].ACCT-CURR-BAL) == 0:
    No interest transaction should be generated for A
  ELSE:
    An interest transaction MAY be generated (depending on rate)
```

#### 2.2 Rate Lookup Consistency
```
For each interest transaction T generated for account A:
  rate = LOOKUP(ACCTFILE[A].ACCT-GROUP-ID + T.TRAN-TYPE-CD + T.TRAN-CAT-CD, DISCGRP).DIS-INT-RATE
  IF rate == 0:
    T should NOT have been generated
  ELSE:
    T.TRAN-AMT == TCATBALF[A, type, cat].TRAN-CAT-BAL * rate / 1200
    (monthly interest = balance * annual_rate / 12 / 100)
```

#### 2.3 Interest Transaction Type
```
For each generated interest transaction T:
  T.TRAN-TYPE-CD == '01'  (Purchase type)
  T.TRAN-CAT-CD == 0005   (Interest Amount category)
  T.TRAN-SOURCE == 'SYSTEM' or similar system indicator
```

#### 2.4 No Duplicate Interest
```
COUNT(TRANSACT where TRAN-CAT-CD == 0005 and TRAN-CARD-NUM == card)
  <= 1 per (account, type, category) combination per run
```

---

## 3. CREASTMT - Statement Generation

**COBOL Program:** `CBSTM03A` (calls `CBSTM03B`)
**JCL:** `CREASTMT.JCL`

**Inputs:**
| Dataset | Copybook | Key |
|---|---|---|
| TRNXFILE (sorted transactions) | CVTRA05Y | TRAN-CARD-NUM + TRAN-ID |
| XREFFILE (card cross-reference) | CVACT03Y | XREF-CARD-NUM |
| ACCTFILE (account master) | CVACT01Y | ACCT-ID |
| CUSTFILE (customer master) | CVCUS01Y | CUST-ID |

**Outputs:**
| Dataset | Format |
|---|---|
| STMTFILE (text statements) | LRECL=80, FB |
| HTMLFILE (HTML statements) | LRECL=100, FB |

### Reconciliation Checks

#### 3.1 Statement Completeness
```
Number of distinct card statements generated == COUNT(XREFFILE)
```
Every card in the cross-reference file must have a statement produced.

#### 3.2 Customer Name on Statement
```
For each statement S for card C:
  LOOKUP(XREFFILE[C].XREF-CUST-ID, CUSTFILE).CUST-FIRST-NAME
    and CUST-LAST-NAME must appear in S
```

#### 3.3 Account Balance on Statement
```
For each statement S for card C:
  account = LOOKUP(XREFFILE[C].XREF-ACCT-ID, ACCTFILE)
  S must display account.ACCT-CURR-BAL
```

#### 3.4 Transaction Detail Completeness
```
For each statement S for card C:
  All transactions in TRNXFILE where TRAN-CARD-NUM == C
    must appear as line items in S
```

#### 3.5 Statement Totals
```
For each statement S:
  SUM(displayed transaction amounts) == displayed account total
```

---

## 4. COMBTRAN - Combine Transactions

**JCL:** `COMBTRAN.jcl` (SORT + IDCAMS)

**Inputs:**
| Dataset | Description |
|---|---|
| TRANSACT.BKUP(0) | Backed-up transaction master |
| SYSTRAN(0) | System-generated transactions (from INTCALC) |

**Output:**
| Dataset | Description |
|---|---|
| TRANSACT.COMBINED(+1) | Merged and sorted transaction file |

### Reconciliation Checks

#### 4.1 Record-Count Merge
```
COUNT(TRANSACT.COMBINED) == COUNT(TRANSACT.BKUP) + COUNT(SYSTRAN)
```
No records should be lost or duplicated during the merge.

#### 4.2 Sort Order
```
TRANSACT.COMBINED must be sorted ascending by TRAN-ID
```

#### 4.3 Amount Conservation
```
SUM(TRANSACT.COMBINED.TRAN-AMT)
  == SUM(TRANSACT.BKUP.TRAN-AMT) + SUM(SYSTRAN.TRAN-AMT)
```

---

## 5. TRANBKP - Transaction Backup

**JCL:** `TRANBKP.jcl` (REPRO + DELETE + DEFINE)

**Input:**
| Dataset | Description |
|---|---|
| TRANSACT.VSAM.KSDS | Current transaction master |

**Output:**
| Dataset | Description |
|---|---|
| TRANSACT.BKUP(+1) | Sequential backup copy |

### Reconciliation Checks

#### 5.1 Exact Copy
```
COUNT(TRANSACT.BKUP) == COUNT(TRANSACT.VSAM.KSDS)
```

#### 5.2 Byte-Level Fidelity
```
For each record index i:
  TRANSACT.BKUP[i] == TRANSACT.VSAM.KSDS[i]  (byte-for-byte)
```

#### 5.3 Post-Backup State
```
After backup and VSAM redefine:
  COUNT(TRANSACT.VSAM.KSDS) == 0  (empty, ready for reload by COMBTRAN)
```

---

## 6. TRANREPT - Transaction Report

**COBOL Program:** `CBTRN03C`
**JCL:** `TRANREPT.jcl`

### Reconciliation Checks

#### 6.1 Grand Total
```
Report Grand Total == SUM(all transactions in report.TRAN-AMT)
```

#### 6.2 Page Totals
```
SUM(all Page Totals) == Report Grand Total
```

#### 6.3 Account Totals
```
For each account section in report:
  Account Total == SUM(transactions listed under that account.TRAN-AMT)
```

---

## 7. Data-Load Jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, etc.)

These jobs load ASCII data files into VSAM datasets.

### Reconciliation Checks

#### 7.1 Record Count Parity
```
For each load job:
  COUNT(output VSAM) == COUNT(input ASCII file)
```

#### 7.2 Key Uniqueness
```
For each KSDS dataset:
  All primary keys must be unique (no duplicates)
```

#### 7.3 Referential Integrity (post-load)
```
For each XREF record:
  XREF-ACCT-ID must exist in ACCTFILE
  XREF-CUST-ID must exist in CUSTFILE

For each CARD record:
  CARD-ACCT-ID must exist in ACCTFILE
```

---

## Summary Matrix

| Job | Record Count | Amount Balance | Referential Integrity | Business Rule |
|---|---|---|---|---|
| POSTTRAN | 1.1 | 1.2 | 1.6 | 1.3, 1.4, 1.5, 1.7 |
| INTCALC | - | - | 2.2 | 2.1, 2.3, 2.4 |
| CREASTMT | 3.1 | 3.5 | 3.2, 3.3 | 3.4 |
| COMBTRAN | 4.1 | 4.3 | - | 4.2 |
| TRANBKP | 5.1 | - | - | 5.2, 5.3 |
| TRANREPT | - | 6.1, 6.2 | - | 6.3 |
| Data-Load | 7.1 | - | 7.3 | 7.2 |
