# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Scope:** Program-to-program call graph, copybook dependencies, VSAM data lineage, JCL job flows

---

## Table of Contents

1. [Online Program Call Graph (CICS XCTL)](#1-online-program-call-graph)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [Program → Copybook Dependency Matrix](#3-program--copybook-dependency-matrix)
4. [Program → VSAM File Access Matrix](#4-program--vsam-file-access-matrix)
5. [JCL Job → Program → Dataset Lineage](#5-jcl-job--program--dataset-lineage)
6. [Batch Cycle Data Flow](#6-batch-cycle-data-flow)
7. [BMS Map → Program Mapping](#7-bms-map--program-mapping)
8. [Full Dependency Adjacency List](#8-full-dependency-adjacency-list)

---

## 1. Online Program Call Graph (CICS XCTL)

Online programs transfer control via `EXEC CICS XCTL`. The COMMAREA (`COCOM01Y`) carries session context between programs.

```
                            ┌─────────┐
                            │ COSGN00C│  (CC00 — Sign-on)
                            │ Login   │
                            └────┬────┘
                                 │ XCTL (based on SEC-USR-TYPE)
                    ┌────────────┴────────────┐
                    │                         │
               ┌────┴────┐              ┌─────┴────┐
               │COMEN01C │              │ COADM01C │
               │User Menu│              │Admin Menu│
               │  (CM00) │              │  (CA00)  │
               └────┬────┘              └─────┬────┘
                    │                         │
        ┌───────────┼──────────┐    ┌─────────┼──────────┐
        │           │          │    │         │          │
   ┌────┴───┐ ┌────┴───┐ ┌───┴────┴──┐ ┌───┴────┐ ┌──┴──────┐
   │COACTVWC│ │COCRDLIC│ │ COTRN00C  │ │COUSR00C│ │COPAUS0C │
   │Acct Viw│ │Card Lst│ │ Tran List │ │User Lst│ │Auth Summ│
   │ (CAVW) │ │ (CCLI) │ │  (CT00)   │ │ (CU00) │ │(opt mod)│
   └────┬───┘ └───┬────┘ └─┬───┬────┘ └──┬─────┘ └────┬────┘
        │         │         │   │         │             │
   ┌────┴───┐  ┌──┴───┐    │   │    ┌────┴───┐   ┌────┴────┐
   │COACTUPC│  │Select │    │   │    │COUSR01C│   │COPAUS1C │
   │Acct Upd│  │  ↓    │    │   │    │Add User│   │Auth Detl│
   │ (CAUP) │  ├───────┤    │   │    │ (CU01) │   └────┬────┘
   └────────┘  │COCRDSLC│   │   │    ├────────┤        │
               │Card Dtl│   │   │    │COUSR02C│   ┌────┴────┐
               │ (CCDL) │   │   │    │Upd User│   │COPAUS2C │
               ├────────┤   │   │    │ (CU02) │   │Fraud Mrk│
               │COCRDUPC│   │   │    ├────────┤   └─────────┘
               │Card Upd│   │   │    │COUSR03C│
               │ (CCUP) │   │   │    │Del User│
               └────────┘   │   │    │ (CU03) │
                             │   │    └────────┘
                        ┌────┴┐ ┌┴───────┐
                        │COTRN│ │COTRN02C│
                        │01C  │ │Tran Add│
                        │View │ │ (CT02) │
                        │(CT01│ └────────┘
                        └─────┘
                             │
                        ┌────┴────┐        ┌────────┐
                        │CORPT00C │        │COBIL00C│
                        │Tran Rept│        │Bill Pay│
                        │ (CR00)  │        │ (CB00) │
                        └─────────┘        └────────┘
```

### Detailed XCTL Relationships

| Source Program | Target Program(s)              | Condition / Trigger                        |
|----------------|--------------------------------|--------------------------------------------|
| COSGN00C       | COADM01C                       | User type = 'A' (Admin)                    |
| COSGN00C       | COMEN01C                       | User type = 'U' (Regular)                  |
| COMEN01C       | COSGN00C                       | PF3 (Exit to sign-on)                      |
| COMEN01C       | COACTVWC                       | Menu option 1 (Account View)               |
| COMEN01C       | COCRDLIC                       | Menu option 2 (Card List)                  |
| COMEN01C       | COTRN00C                       | Menu option 3 (Transaction List)           |
| COMEN01C       | COBIL00C                       | Menu option 4 (Bill Payment)               |
| COMEN01C       | CORPT00C                       | Menu option 5 (Transaction Reports)        |
| COMEN01C       | COPAUS0C                       | Menu option (Auth Summary — optional)      |
| COADM01C       | COSGN00C                       | PF3 (Exit to sign-on)                      |
| COADM01C       | COUSR00C                       | Admin option 1 (User List)                 |
| COADM01C       | Dynamic target                 | Based on CDEMO-ADMIN-OPT-PGMNAME table    |
| COACTVWC       | COMEN01C                       | PF3 (Return to menu)                       |
| COACTUPC       | COMEN01C                       | PF3 (Return to menu)                       |
| COCRDLIC       | COMEN01C                       | PF3 (Return to menu)                       |
| COCRDLIC       | COCRDSLC                       | Select 'S' on a card row (View detail)     |
| COCRDLIC       | COCRDUPC                       | Select 'U' on a card row (Update card)     |
| COCRDSLC       | Return to caller               | PF3 (Return to calling program)            |
| COCRDUPC       | Return to caller               | PF3 (Return to calling program)            |
| COTRN00C       | COMEN01C                       | PF3 (Return to menu)                       |
| COTRN00C       | COTRN01C                       | Select transaction for viewing             |
| COTRN01C       | Return to caller               | PF3 (Return to calling program)            |
| COTRN02C       | Return to caller               | PF3 (Return to calling program)            |
| COBIL00C       | Return to caller               | PF3 (Return to calling program)            |
| CORPT00C       | Return to caller               | PF3 (Return to calling program)            |
| COUSR00C       | COMEN01C or COADM01C           | PF3 (Return to menu)                       |
| COUSR00C       | COUSR01C                       | Select 'A' (Add user)                      |
| COUSR00C       | COUSR02C                       | Select 'U' (Update user)                   |
| COUSR00C       | COUSR03C                       | Select 'D' (Delete user)                   |
| COUSR01C       | Return to caller               | PF3 (Return to user list)                  |
| COUSR02C       | Return to caller               | PF3 (Return to user list)                  |
| COUSR03C       | Return to caller               | PF3 (Return to user list)                  |

---

## 2. Batch Program Call Graph

Batch programs use `CALL` statements for subroutine calls and are invoked by JCL.

```
┌──────────────────────────────────────────────────────────────────┐
│                     BATCH CALL GRAPH                             │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  CBACT01C ──CALL──→ COBDATFT (ASM date formatter)               │
│           ──CALL──→ CEE3ABD  (LE abend handler)                 │
│                                                                  │
│  CBACT02C ──CALL──→ CEE3ABD                                     │
│  CBACT03C ──CALL──→ CEE3ABD                                     │
│  CBACT04C ──CALL──→ CEE3ABD  (interest calculation)             │
│  CBCUS01C ──CALL──→ CEE3ABD                                     │
│                                                                  │
│  CBTRN01C ──CALL──→ CEE3ABD                                     │
│  CBTRN02C ──CALL──→ CEE3ABD  (transaction posting)              │
│  CBTRN03C ──CALL──→ CEE3ABD  (transaction report)               │
│                                                                  │
│  CBSTM03A ──CALL──→ CBSTM03B (report writer subroutine)        │
│           ──CALL──→ CEE3ABD                                     │
│                                                                  │
│  CBEXPORT ──CALL──→ CEE3ABD                                     │
│  CBIMPORT ──CALL──→ CEE3ABD                                     │
│                                                                  │
│  COBSWAIT ──CALL──→ MVSWAIT  (ASM wait utility)                │
│                                                                  │
│  CSUTLDTC ──CALL──→ CEEDAYS  (LE date conversion)              │
│                                                                  │
│  COTRN02C ──CALL──→ CSUTLDTC (online, date validation)         │
│  CORPT00C ──CALL──→ CSUTLDTC (online, date validation)         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### External Dependencies

| External Module | Type        | Called By                | Purpose                      |
|-----------------|-------------|--------------------------|------------------------------|
| CEE3ABD         | LE Runtime  | All batch programs       | Abnormal termination handler |
| CEEDAYS         | LE Runtime  | CSUTLDTC                 | Date conversion/validation   |
| COBDATFT        | Assembler   | CBACT01C                 | Date formatting              |
| MVSWAIT         | Assembler   | COBSWAIT                 | Wait/delay (centiseconds)    |

---

## 3. Program → Copybook Dependency Matrix

### 3.1 Online Programs

| Copybook ↓ \ Program → | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|-------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COCOM01Y (Commarea)     | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| COTTL01Y (Titles)       | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |          | ●        | ●        | ●        | ●        |
| CSDAT01Y (Date)         | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |          | ●        | ●        | ●        | ●        |
| CSMSG01Y (Messages)     | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |          | ●        | ●        | ●        | ●        |
| CSUSR01Y (User Sec)     | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          | ●        | ●        | ●        | ●        |
| DFHAID (AID keys)       | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| DFHBMSCA (BMS attrs)    | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| CSMSG02Y (Abend)        |          |          |          | ●        | ●        |          | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVCRD01Y (Card WA)      |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVACT01Y (Account)      |          |          |          | ●        | ●        |          |          |          |          |          | ●        |          | ●        |          |          |          |          |
| CVACT02Y (Card)         |          |          |          | ●        |          | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVACT03Y (Xref)         |          |          |          | ●        | ●        |          |          |          |          |          | ●        |          | ●        |          |          |          |          |
| CVCUS01Y (Customer)     |          |          |          | ●        | ●        |          | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVTRA05Y (Transaction)  |          |          |          |          |          |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |
| COMEN02Y (Menu Opts)    |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| COADM02Y (Admin Opts)   |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| CSSTRPFY (PF-Key)       |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CSLKPCDY (Lookups)      |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSSETATY (Attr Set)     |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSUTLDWY (Date Edit)    |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSUTLDPY (Date Util)    |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |

### 3.2 Batch Programs

| Copybook ↓ \ Program →  | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|--------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y (Account)       | ●        |          |          | ●        |          | ●        | ●        |          | ●        |          | ●        | ●        |
| CVACT02Y (Card)          |          | ●        |          |          |          | ●        |          |          |          |          | ●        | ●        |
| CVACT03Y (Xref)          |          |          | ●        | ●        |          | ●        | ●        | ●        | ●        |          | ●        | ●        |
| CVCUS01Y (Customer)      |          |          |          |          | ●        | ●        |          |          |          |          | ●        | ●        |
| CVTRA01Y (Cat Bal)       |          |          |          | ●        |          |          | ●        |          |          |          |          |          |
| CVTRA02Y (Disc Grp)      |          |          |          | ●        |          |          |          |          |          |          |          |          |
| CVTRA03Y (Tran Type)     |          |          |          |          |          |          |          | ●        |          |          |          |          |
| CVTRA04Y (Tran Cat)      |          |          |          |          |          |          |          | ●        |          |          |          |          |
| CVTRA05Y (Transaction)   |          |          |          | ●        |          | ●        | ●        | ●        |          |          | ●        | ●        |
| CVTRA06Y (Daily Tran)    |          |          |          |          |          | ●        | ●        |          |          |          |          |          |
| CVTRA07Y (Report Dtl)    |          |          |          |          |          |          |          | ●        |          |          |          |          |
| CVEXPORT (Export Rec)     |          |          |          |          |          |          |          |          |          |          | ●        | ●        |
| COSTM01 (Stmt Tran)      |          |          |          |          |          |          |          |          | ●        |          |          |          |
| CUSTREC (Cust Rec)        |          |          |          |          |          |          |          |          | ●        |          |          |          |
| CODATECN (Date Conv)      | ●        |          |          |          |          |          |          |          |          |          |          |          |

---

## 4. Program → VSAM File Access Matrix

### 4.1 Online Programs — CICS File Access

| VSAM File ↓ \ Program → | COSGN00C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|--------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| USRSEC (User Security)   | R        |          |          |          |          |          |          |          |          |          | R/B      | W        | R/W      | R/D      |
| ACCTDAT (Accounts)       |          | R        | R/W      |          |          |          |          |          | R        | R/W      |          |          |          |          |
| CARDDAT (Cards)          |          |          |          | R/B      | R        | R/W      |          |          |          |          |          |          |          |          |
| CARDAIX (Card AIX)       |          | R        | R        |          | R        |          |          |          |          |          |          |          |          |          |
| CXACAIX (Xref AIX)       |          | R        | R        |          |          |          |          |          | R        | R        |          |          |          |          |
| CUSTDAT (Customers)      |          | R        | R/W      |          | R        | R        |          |          |          |          |          |          |          |          |
| TRANSACT (Transactions)  |          |          |          |          |          |          | R/B      | R        | R/W      | R/W      |          |          |          |          |

**Legend:** R = Read, W = Write/Rewrite, B = Browse (sequential read), D = Delete

### 4.2 Batch Programs — File Access

| File ↓ \ Program →       | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|---------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| ACCTDAT / ACCTFILE        | R        |          |          | R/W      |          | R        | R/W      |          | R        | R        |          |
| CARDDAT / CARDFILE        |          | R        |          |          |          | R        |          |          |          | R        |          |
| CUSTDAT / CUSTFILE        |          |          |          |          | R        | R        |          |          | R        | R        |          |
| CARDXREF / XREFFILE       |          |          | R        | R        |          | R        | R        | R        | R        | R        |          |
| TRANSACT / TRANFILE       |          |          |          |          |          |          | R        | R        | R        | R        |          |
| DALYTRAN                  |          |          |          |          |          | R        | R        |          |          |          |          |
| TCATBAL / TCATBALF        |          |          |          | R/W      |          |          | R/W      |          |          |          |          |
| DISCGRP                   |          |          |          | R        |          |          |          |          |          |          |          |
| TRANTYPE                  |          |          |          |          |          |          |          | R        |          |          |          |
| TRANCATG                  |          |          |          |          |          |          |          | R        |          |          |          |
| DALYREJS                  |          |          |          |          |          |          | W        |          |          |          |          |
| EXPFILE (Export Seq)      |          |          |          |          |          |          |          |          |          | W        | R        |
| OUTPUT files              | W        | W        |          |          | W        |          |          | W        | W        |          | W        |

---

## 5. JCL Job → Program → Dataset Lineage

### 5.1 Data Loading Jobs

```
ACCTFILE.jcl    ─── IDCAMS ──→ ACCTDAT (VSAM KSDS)        ← ACCTDATA (Sequential)
CARDFILE.jcl    ─── IDCAMS ──→ CARDDAT (VSAM KSDS + AIX)  ← CARDDATA (Sequential)
CUSTFILE.jcl    ─── IDCAMS ──→ CUSTDAT (VSAM KSDS)        ← CUSTDATA (Sequential)
XREFFILE.jcl    ─── IDCAMS ──→ CARDXREF (VSAM KSDS + AIX) ← XREFDATA (Sequential)
TRANFILE.jcl    ─── IDCAMS ──→ TRANSACT (VSAM KSDS + AIX) ← TRANSACT (Sequential)
DUSRSECJ.jcl    ─── IDCAMS ──→ USRSEC (VSAM KSDS)         ← USRSEC.PS (Sequential)
TRANTYPE.jcl    ─── IDCAMS ──→ TRANTYPE (VSAM KSDS)       ← TRANTYPE (Sequential)
TRANCATG.jcl    ─── IDCAMS ──→ TRANCATG (VSAM KSDS)       ← TRANCATG (Sequential)
TCATBALF.jcl    ─── IDCAMS ──→ TCATBAL (VSAM KSDS)        ← TCATBAL (Sequential)
DISCGRP.jcl     ─── IDCAMS ──→ DISCGRP (VSAM KSDS)        ← DISCGRP (Sequential)
```

### 5.2 Core Batch Processing Flow

```
POSTTRAN.jcl:
  CBTRN02C ← TRANFILE (Daily Trans), DALYTRAN, XREFFILE, ACCTFILE, TCATBALF
           → ACCTFILE (updated balances), TCATBALF (updated), DALYREJS (rejects)

INTCALC.jcl:
  CBACT04C ← TCATBALF, XREFFILE, ACCTFILE, DISCGRP
           → TRANSACT (interest transactions)

COMBTRAN.jcl:
  SORT     ← TRANSACT (existing), new transactions
           → TRANSACT (VSAM — merged/combined)

CREASTMT.JCL:
  SORT     ← TRANSACT (VSAM) → TRXFL.SEQ (sorted sequential)
  IDCAMS   ← TRXFL.SEQ → TRXFL.VSAM.KSDS
  CBSTM03A ← TRXFL.VSAM, XREFFILE, ACCTFILE, CUSTFILE
             → CBSTM03B → STMTFILE (statements), HTMLFILE (HTML statements)

TRANBKP.jcl:
  REPROC   ← TRANSACT (VSAM) → TRANSACT.BKUP (Sequential backup)

TRANREPT.jcl:
  REPROC   ← TRANSACT (VSAM) → Sequential copy
  SORT     ← Sequential → Sorted sequential
  CBTRN03C ← Sorted TRANFILE, CARDXREF, TRANTYPE, TRANCATG
           → TRANREPT (Report output)
```

### 5.3 Read / Export / Import Jobs

```
READACCT.jcl:   CBACT01C ← ACCTFILE (VSAM) → OUTFILE, ARRYFILE, VBRCFILE (Sequential)
READCARD.jcl:   CBACT02C ← CARDFILE (VSAM) → SYSPRINT (Report)
READCUST.jcl:   CBCUS01C ← CUSTFILE (VSAM) → SYSPRINT (Report)
READXREF.jcl:   CBACT03C ← XREFFILE (VSAM) → SYSPRINT (Report)
CBEXPORT.jcl:   CBEXPORT ← CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE → EXPFILE
CBIMPORT.jcl:   CBIMPORT ← EXPFILE → CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT
```

---

## 6. Batch Cycle Data Flow

The full batch cycle runs in the following sequence, with CICS files closed and reopened around it:

```
 Phase 1: CICS Quiesce
 ┌──────────────────────────────────────────────────────────┐
 │  CLOSEFIL ── Close all CICS VSAM files                  │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 2: Data Refresh (can run in parallel)
 ┌──────────────────────────────────────────────────────────┐
 │  ACCTFILE ── Refresh account data                       │
 │  CARDFILE ── Refresh card data                          │
 │  CUSTFILE ── Refresh customer data                      │
 │  XREFFILE ── Refresh cross-reference                    │
 │  TRANFILE ── Refresh transaction master                 │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 3: Transaction Posting
 ┌──────────────────────────────────────────────────────────┐
 │  POSTTRAN ── Post daily transactions                    │
 │    Reads: DALYTRAN, XREFFILE, ACCTFILE, TCATBALF        │
 │    Updates: ACCTFILE (balances), TCATBALF                │
 │    Writes: DALYREJS (rejected transactions)             │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 4: Interest Calculation
 ┌──────────────────────────────────────────────────────────┐
 │  INTCALC ── Calculate and post interest charges         │
 │    Reads: TCATBALF, XREFFILE, ACCTFILE, DISCGRP         │
 │    Writes: TRANSACT (new interest transactions)         │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 5: Transaction Backup
 ┌──────────────────────────────────────────────────────────┐
 │  TRANBKP ── Backup current transaction file             │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 6: Transaction Merge
 ┌──────────────────────────────────────────────────────────┐
 │  COMBTRAN ── Merge daily + existing transactions        │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 7: Statement Generation
 ┌──────────────────────────────────────────────────────────┐
 │  CREASTMT ── Generate account statements                │
 │    Sort transactions → Call CBSTM03A/CBSTM03B           │
 │    Output: STMTFILE (text), HTMLFILE (HTML)             │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 8: Index Rebuild
 ┌──────────────────────────────────────────────────────────┐
 │  TRANIDX ── Rebuild alternate indexes on TRANSACT       │
 └──────────────────────┬───────────────────────────────────┘
                        ▼
 Phase 9: CICS Resume
 ┌──────────────────────────────────────────────────────────┐
 │  OPENFIL ── Reopen all CICS VSAM files                  │
 └──────────────────────────────────────────────────────────┘
```

---

## 7. BMS Map → Program Mapping

| BMS Mapset | BMS Map Name | COBOL Program | Screen Function                |
|------------|-------------|---------------|--------------------------------|
| COSGN00    | COSGN0A     | COSGN00C      | Login / Sign-on                |
| COMEN01    | COMEN1A     | COMEN01C      | Main Menu (Regular)            |
| COADM01    | COADM1A     | COADM01C      | Admin Menu                     |
| COACTVW    | CACTVWA     | COACTVWC      | Account View                   |
| COACTUP    | CACTUPA     | COACTUPC      | Account Update                 |
| COCRDLI    | CCRDLIA     | COCRDLIC      | Card List                      |
| COCRDSL    | CCRDSLA     | COCRDSLC      | Card Detail                    |
| COCRDUP    | CCRDUPA     | COCRDUPC      | Card Update                    |
| COTRN00    | COTRN0A     | COTRN00C      | Transaction List               |
| COTRN01    | COTRN1A     | COTRN01C      | Transaction View               |
| COTRN02    | COTRN2A     | COTRN02C      | Transaction Add                |
| CORPT00    | CORPT0A     | CORPT00C      | Transaction Report             |
| COBIL00    | COBIL0A     | COBIL00C      | Bill Payment                   |
| COUSR00    | COUSR0A     | COUSR00C      | User List                      |
| COUSR01    | COUSR1A     | COUSR01C      | User Add                       |
| COUSR02    | COUSR2A     | COUSR02C      | User Update                    |
| COUSR03    | COUSR3A     | COUSR03C      | User Delete                    |

---

## 8. Full Dependency Adjacency List

For tooling / graph analysis, here is every dependency as a flat list:

```
# Format: SOURCE -> TARGET [type]

# Online XCTL transfers
COSGN00C -> COADM01C [xctl]
COSGN00C -> COMEN01C [xctl]
COMEN01C -> COSGN00C [xctl]
COMEN01C -> COACTVWC [xctl]
COMEN01C -> COCRDLIC [xctl]
COMEN01C -> COTRN00C [xctl]
COMEN01C -> COBIL00C [xctl]
COMEN01C -> CORPT00C [xctl]
COMEN01C -> COPAUS0C [xctl]
COADM01C -> COSGN00C [xctl]
COADM01C -> COUSR00C [xctl]
COACTVWC -> COMEN01C [xctl]
COACTUPC -> COMEN01C [xctl]
COCRDLIC -> COMEN01C [xctl]
COCRDLIC -> COCRDSLC [xctl]
COCRDLIC -> COCRDUPC [xctl]
COTRN00C -> COMEN01C [xctl]
COTRN00C -> COTRN01C [xctl]
COTRN02C -> COMEN01C [xctl]
COBIL00C -> COMEN01C [xctl]
CORPT00C -> COMEN01C [xctl]
COUSR00C -> COMEN01C [xctl]
COUSR00C -> COUSR01C [xctl]
COUSR00C -> COUSR02C [xctl]
COUSR00C -> COUSR03C [xctl]

# Batch CALL relationships
CBACT01C -> COBDATFT [call]
CBSTM03A -> CBSTM03B [call]
COBSWAIT -> MVSWAIT [call]
CSUTLDTC -> CEEDAYS [call]
COTRN02C -> CSUTLDTC [call]
CORPT00C -> CSUTLDTC [call]

# JCL invocations
POSTTRAN.jcl -> CBTRN02C [exec]
INTCALC.jcl -> CBACT04C [exec]
CREASTMT.JCL -> CBSTM03A [exec]
TRANREPT.jcl -> CBTRN03C [exec]
READACCT.jcl -> CBACT01C [exec]
READCARD.jcl -> CBACT02C [exec]
READCUST.jcl -> CBCUS01C [exec]
READXREF.jcl -> CBACT03C [exec]
CBEXPORT.jcl -> CBEXPORT [exec]
CBIMPORT.jcl -> CBIMPORT [exec]
WAITSTEP.jcl -> COBSWAIT [exec]

# Data reads (program -> file)
COSGN00C -> USRSEC [read]
COACTVWC -> ACCTDAT [read]
COACTVWC -> CUSTDAT [read]
COACTVWC -> CXACAIX [read]
COACTUPC -> ACCTDAT [read/write]
COACTUPC -> CUSTDAT [read/write]
COACTUPC -> CXACAIX [read]
COCRDLIC -> CARDDAT [browse]
COCRDSLC -> CARDDAT [read]
COCRDUPC -> CARDDAT [read/write]
COTRN00C -> TRANSACT [browse]
COTRN01C -> TRANSACT [read]
COTRN02C -> TRANSACT [read/write]
COTRN02C -> CXACAIX [read]
COBIL00C -> ACCTDAT [read/write]
COBIL00C -> TRANSACT [read/write]
COBIL00C -> CXACAIX [read]
COUSR00C -> USRSEC [browse]
COUSR01C -> USRSEC [write]
COUSR02C -> USRSEC [read/write]
COUSR03C -> USRSEC [read/delete]
CBTRN02C -> DALYTRAN [read]
CBTRN02C -> ACCTDAT [read/write]
CBTRN02C -> XREFFILE [read]
CBTRN02C -> TCATBAL [read/write]
CBACT04C -> ACCTDAT [read/write]
CBACT04C -> XREFFILE [read]
CBACT04C -> DISCGRP [read]
CBACT04C -> TCATBAL [read/write]
CBSTM03A -> TRANSACT [read]
CBSTM03A -> XREFFILE [read]
CBSTM03A -> ACCTDAT [read]
CBSTM03A -> CUSTDAT [read]
CBTRN03C -> TRANSACT [read]
CBTRN03C -> XREFFILE [read]
CBTRN03C -> TRANTYPE [read]
CBTRN03C -> TRANCATG [read]
CBEXPORT -> CUSTDAT [read]
CBEXPORT -> ACCTDAT [read]
CBEXPORT -> XREFFILE [read]
CBEXPORT -> TRANSACT [read]
CBEXPORT -> CARDDAT [read]
```

---

*End of Dependency Map*
