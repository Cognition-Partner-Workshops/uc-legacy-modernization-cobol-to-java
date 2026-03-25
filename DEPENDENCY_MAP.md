# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
>
> This document maps every inter-program call, copybook inclusion, VSAM file access, and JCL job data flow across the CardDemo application.

---

## Table of Contents

1. [Online Program Call Graph](#online-program-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Program-to-VSAM File Access Matrix](#program-to-vsam-file-access-matrix)
4. [Copybook Inclusion Matrix](#copybook-inclusion-matrix)
5. [JCL Job Data Lineage](#jcl-job-data-lineage)
6. [Batch Cycle Execution Order](#batch-cycle-execution-order)
7. [BMS Map to Program Binding](#bms-map-to-program-binding)
8. [End-to-End Data Flow Diagrams](#end-to-end-data-flow-diagrams)

---

## Online Program Call Graph

All online program transfers use `EXEC CICS XCTL` (transfer control, no return) passing `CARDDEMO-COMMAREA`.

### Navigation Flow (ASCII Diagram)

```
                                    ┌──────────┐
                                    │ COSGN00C │  (CC00 - Sign-On)
                                    │ Entry Pt │
                                    └────┬─────┘
                                         │
                            ┌────────────┴────────────┐
                            │ (Admin)                  │ (Regular User)
                            ▼                          ▼
                     ┌──────────┐               ┌──────────┐
                     │ COADM01C │               │ COMEN01C │
                     │Admin Menu│               │Main Menu │
                     └────┬─────┘               └────┬─────┘
                          │                          │
           ┌──────────────┼──────────────┐           │
           │              │              │           │
           ▼              ▼              ▼           │
    ┌──────────┐   ┌──────────┐   ┌──────────┐      │
    │ COUSR00C │   │ COUSR01C │   │ COUSR02C │      │
    │User List │   │User Add  │   │User Upd  │      │
    └────┬─────┘   └──────────┘   └──────────┘      │
         │                                           │
         ▼                                           │
    ┌──────────┐                                     │
    │ COUSR03C │                                     │
    │User Del  │                                     │
    └──────────┘                                     │
                                                     │
    ┌────────────────────────────────────────────────┘
    │
    ├──► COACTVWC (Account View) ──► COCRDLIC (Card List)
    │
    ├──► COACTUPC (Account Update)
    │
    ├──► COCRDLIC (Card List) ──┬──► COCRDSLC (Card Detail)
    │                           └──► COCRDUPC (Card Update)
    │
    ├──► COCRDSLC (Card Detail View)
    │
    ├──► COCRDUPC (Card Update)
    │
    ├──► COTRN00C (Transaction List) ──► COTRN01C (Transaction View)
    │
    ├──► COTRN01C (Transaction View)
    │
    ├──► COTRN02C (Transaction Add)
    │
    ├──► CORPT00C (Transaction Reports)
    │
    ├──► COBIL00C (Bill Payment)
    │
    └──► COPAUS0C (Pending Auth View)* ──► COPAUS1C (Auth Detail)*
                                         └──► COPAUS2C (Fraud Mark)*

    * = Optional authorization module (IMS/DB2/MQ)
```

### Detailed XCTL Transfer Table

| Source Program | Target Program | Trigger / Condition                               | Direction     |
|----------------|----------------|---------------------------------------------------|---------------|
| COSGN00C       | COADM01C       | Successful login, user type = 'A' (Admin)         | Forward       |
| COSGN00C       | COMEN01C       | Successful login, user type = 'U' (User)          | Forward       |
| COMEN01C       | COSGN00C       | PF3 (Exit) from main menu                         | Back          |
| COMEN01C       | COACTVWC       | Menu option 1 selected                            | Forward       |
| COMEN01C       | COACTUPC       | Menu option 2 selected                            | Forward       |
| COMEN01C       | COCRDLIC       | Menu option 3 selected                            | Forward       |
| COMEN01C       | COCRDSLC       | Menu option 4 selected                            | Forward       |
| COMEN01C       | COCRDUPC       | Menu option 5 selected                            | Forward       |
| COMEN01C       | COTRN00C       | Menu option 6 selected                            | Forward       |
| COMEN01C       | COTRN01C       | Menu option 7 selected                            | Forward       |
| COMEN01C       | COTRN02C       | Menu option 8 selected                            | Forward       |
| COMEN01C       | CORPT00C       | Menu option 9 selected                            | Forward       |
| COMEN01C       | COBIL00C       | Menu option 10 selected                           | Forward       |
| COMEN01C       | COPAUS0C       | Menu option 11 selected (optional module)         | Forward       |
| COADM01C       | COSGN00C       | PF3 (Exit) from admin menu                        | Back          |
| COADM01C       | COUSR00C       | Admin option 1 - User List                        | Forward       |
| COADM01C       | COUSR01C       | Admin option 2 - User Add                         | Forward       |
| COADM01C       | COUSR02C       | Admin option 3 - User Update                      | Forward       |
| COADM01C       | COUSR03C       | Admin option 4 - User Delete                      | Forward       |
| COADM01C       | COTRTLIC       | Admin option 5 - Tran Type List (DB2 module)      | Forward       |
| COADM01C       | COTRTUPC       | Admin option 6 - Tran Type Maint (DB2 module)     | Forward       |
| COACTVWC       | COMEN01C       | PF3 (Exit) or return to caller                    | Back          |
| COACTUPC       | COMEN01C       | PF3 (Exit) or return to caller                    | Back          |
| COCRDLIC       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COCRDLIC       | COCRDSLC       | 'S' (Select) on a card row                        | Forward       |
| COCRDLIC       | COCRDUPC       | 'U' (Update) on a card row                        | Forward       |
| COCRDSLC       | COCRDLIC       | PF3 (Exit) to card list                           | Back          |
| COCRDSLC       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COCRDUPC       | COCRDLIC       | PF3 (Exit) to card list                           | Back          |
| COCRDUPC       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COTRN00C       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COTRN00C       | COTRN01C       | Select transaction for detail view                | Forward       |
| COTRN01C       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COTRN02C       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| CORPT00C       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COBIL00C       | COMEN01C       | PF3 (Exit) to menu                                | Back          |
| COUSR00C       | COADM01C       | PF3 (Exit) to admin menu                          | Back          |
| COUSR00C       | COUSR02C       | 'U' (Update) on a user row                        | Forward       |
| COUSR00C       | COUSR03C       | 'D' (Delete) on a user row                        | Forward       |
| COUSR01C       | COADM01C       | PF3 (Exit) to admin menu                          | Back          |
| COUSR02C       | COADM01C       | PF3 (Exit) to admin menu                          | Back          |
| COUSR03C       | COADM01C       | PF3 (Exit) to admin menu                          | Back          |

---

## Batch Program Call Graph

Batch programs use `CALL` (subroutine) rather than `XCTL` (transfer control).

```
CBSTM03A (Statement Generation)
    └──► CALL 'CBSTM03B'  (Print formatting sub-module, called 13+ times)

CBACT01C (Account File Refresh)
    └──► CALL 'COBDATFT'  (Assembler date formatting utility)

COTRN02C (Transaction Add - Online)
    └──► CALL 'CSUTLDTC'  (Date validation utility, called 2 times)

CORPT00C (Transaction Reports - Online)
    └──► CALL 'CSUTLDTC'  (Date validation utility, called 2 times)

CSUTLDTC (Date Validation Utility)
    └──► CALL 'CEEDAYS'   (IBM LE date conversion service)

COBSWAIT (Wait Utility)
    └──► CALL 'MVSWAIT'   (Assembler MVS wait service)

CBTRN02C (Transaction Posting)
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)

CBTRN03C (Transaction Report)
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)

CBACT04C (Interest Calculation)
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)

CBEXPORT (Data Export)
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)

CBIMPORT (Data Import)
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)

CBACT01C, CBACT02C, CBACT03C, CBCUS01C, CBTRN01C
    └──► CALL 'CEE3ABD'   (IBM LE abnormal termination)
```

### Call Summary Table

| Caller       | Callee       | Mechanism    | Purpose                           | Call Count |
|--------------|-------------|--------------|-----------------------------------|------------|
| CBSTM03A     | CBSTM03B    | CALL (batch) | Statement print formatting        | 13+        |
| CBACT01C     | COBDATFT    | CALL (batch) | Date formatting (assembler)       | 1          |
| COTRN02C     | CSUTLDTC    | CALL (online)| Date validation                   | 2          |
| CORPT00C     | CSUTLDTC    | CALL (online)| Date range validation             | 2          |
| CSUTLDTC     | CEEDAYS     | CALL (LE)    | IBM LE date conversion            | 1          |
| COBSWAIT     | MVSWAIT     | CALL (ASM)   | MVS wait                          | 1          |
| 8 batch pgms | CEE3ABD     | CALL (LE)    | Abnormal termination handler      | 1 each     |

---

## Program-to-VSAM File Access Matrix

### Online Programs (CICS READ/WRITE/REWRITE/STARTBR/READNEXT/READPREV)

| Program    | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | CARDXREF | CXACAIX | TRANSACT |
|------------|--------|---------|---------|---------|---------|----------|---------|----------|
| COSGN00C   | R      |         |         |         |         |          |         |          |
| COACTVWC   |        | R       |         | R       | R       | R        | R       |          |
| COACTUPC   |        | R/W     |         |         | R/W     |          | R       |          |
| COCRDLIC   |        |         | R(brws) | R(brws) |         |          |         |          |
| COCRDSLC   |        |         | R       | R       | R       |          |         |          |
| COCRDUPC   |        |         | R/W     |         | R       |          |         |          |
| COTRN00C   |        |         |         |         |         |          |         | R(brws)  |
| COTRN01C   |        |         |         |         |         |          |         | R        |
| COTRN02C   |        |         |         |         |         | R        | R       | R/W      |
| CORPT00C   |        |         |         |         |         |          |         | R        |
| COBIL00C   |        | R/W     |         |         |         | R        |         | W        |
| COUSR00C   | R(brws)|         |         |         |         |          |         |          |
| COUSR01C   | R/W    |         |         |         |         |          |         |          |
| COUSR02C   | R/W    |         |         |         |         |          |         |          |
| COUSR03C   | R/D    |         |         |         |         |          |         |          |

**Legend:** R = Read, W = Write/Rewrite, D = Delete, (brws) = Browse (STARTBR/READNEXT/READPREV)

### Batch Programs (File I/O)

| Program    | DALYTRAN | TRANSACT | ACCTDAT | CARDXREF | XREFFIL1 | TCATBALF | DISCGRP | DALYREJS | CUSTDAT | CARDDAT | STMTFILE | HTMLFILE | EXPORT |
|------------|----------|----------|---------|----------|----------|----------|---------|----------|---------|---------|----------|----------|--------|
| CBTRN02C   | R        | W        | R/W     | R        |          | R/W      |         | W        |         |         |          |          |        |
| CBACT04C   |          | W        | R/W     |          | R        | R        | R       |          |         |         |          |          |        |
| CBTRN03C   |          |          |         | R        |          |          |         |          |         |         |          |          |        |
| CBSTM03A   |          | R        | R       | R        |          |          |         |          | R       |         | W        | W        |        |
| CBACT01C   |          |          | R       |          |          |          |         |          |         |         |          |          |        |
| CBACT02C   |          |          |         |          |          |          |         |          |         | R       |          |          |        |
| CBACT03C   |          |          |         | R        |          |          |         |          |         |         |          |          |        |
| CBCUS01C   |          |          |         |          |          |          |         |          | R       |         |          |          |        |
| CBEXPORT   |          | R        | R       | R        |          |          |         |          | R       | R       |          |          | W      |
| CBIMPORT   |          |          |         |          |          |          |         |          |         |         |          |          | R      |

**Legend:** R = Read/Open Input, W = Write/Open Output, R/W = Read + Rewrite

---

## Copybook Inclusion Matrix

### Business Entity Copybooks → Programs That Include Them

| Copybook   | Entity          | Online Programs                                                    | Batch Programs                     |
|------------|-----------------|--------------------------------------------------------------------|------------------------------------|
| COCOM01Y   | COMMAREA        | ALL 17 online programs                                             | -                                  |
| CVACT01Y   | Account         | COACTVWC, COACTUPC                                                 | CBACT01C, CBACT04C, CBEXPORT, CBSTM03A, COTRN02C |
| CVACT02Y   | Card            | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                             | CBACT02C, CBEXPORT                 |
| CVACT03Y   | Card Xref       | COACTVWC, COACTUPC                                                 | CBACT03C, CBACT04C, CBTRN03C, CBEXPORT, CBSTM03A |
| CVCUS01Y   | Customer        | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                             | CBEXPORT                           |
| CUSTREC    | Customer (alt)  | -                                                                  | CBSTM03A                           |
| CVTRA05Y   | Transaction     | COTRN00C, COTRN01C, COTRN02C, CORPT00C                            | CBACT04C, CBEXPORT, CBTRN03C       |
| CVTRA06Y   | Daily Tran      | -                                                                  | CBTRN02C                           |
| CSUSR01Y   | User Security   | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | - |
| CVTRA01Y   | Cat Balance     | -                                                                  | CBACT04C                           |
| CVTRA02Y   | Disc Group      | -                                                                  | CBACT04C                           |
| CVTRA03Y   | Tran Type       | -                                                                  | CBTRN03C                           |
| CVTRA04Y   | Tran Category   | -                                                                  | CBTRN03C                           |
| CVTRA07Y   | Report Layout   | -                                                                  | CBTRN03C                           |
| CVEXPORT   | Export Record   | -                                                                  | CBEXPORT                           |
| COSTM01    | Statement Data  | -                                                                  | CBSTM03A                           |
| CVCRD01Y   | Card Work Area  | COCRDLIC, COCRDSLC, COCRDUPC                                       | -                                  |

### UI/System Copybooks → Programs That Include Them

| Copybook   | Purpose           | Included By                                                          |
|------------|-------------------|----------------------------------------------------------------------|
| COTTL01Y   | Screen titles     | All 17 online programs                                               |
| CSDAT01Y   | Date formatting   | All 17 online programs                                               |
| CSMSG01Y   | Common messages   | All 17 online programs                                               |
| CSMSG02Y   | Abend handling    | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                              |
| COMEN02Y   | Menu options      | COMEN01C                                                             |
| COADM02Y   | Admin options     | COADM01C                                                             |
| CSLKPCDY   | Lookup codes      | COACTUPC                                                             |
| CSSETATY   | Field attributes  | COACTUPC (40+ times via REPLACING)                                   |
| CSSTRPFY   | String strip      | COACTUPC, COCRDUPC                                                   |
| CSUTLDWY   | Date edit WS      | COACTUPC                                                             |
| CSUTLDPY   | Date utility parm | COACTUPC                                                             |
| CODATECN   | Date conversion   | CBACT01C                                                             |

---

## JCL Job Data Lineage

### Data Refresh Jobs (File Loading)

```
ASCII Data Files (app/data/ASCII/)          Sequential PS Files
    acctdata.txt  ──► ACCTFILE.jcl ──► IDCAMS REPRO ──► ACCTDAT (VSAM KSDS)
    carddata.txt  ──► CARDFILE.jcl ──► IDCAMS REPRO ──► CARDDAT (VSAM KSDS)
    custdata.txt  ──► CUSTFILE.jcl ──► IDCAMS REPRO ──► CUSTDAT (VSAM KSDS)
    cardxref.txt  ──► XREFFILE.jcl ──► IDCAMS REPRO ──► CARDXREF (VSAM KSDS)
    dailytran.txt ──► TRANFILE.jcl ──► IDCAMS REPRO ──► TRANSACT (VSAM KSDS)
    (usrsec)      ──► DUSRSECJ.jcl ──► IDCAMS REPRO ──► USRSEC (VSAM KSDS)
    tcatbal.txt   ──► TCATBALF.jcl ──► IDCAMS REPRO ──► TCATBALF (VSAM KSDS)
    trancatg.txt  ──► TRANCATG.jcl ──► IDCAMS DEF/REPRO ──► TRANCATG (VSAM KSDS)
    discgrp.txt   ──► DISCGRP.jcl  ──► IDCAMS DEF/REPRO ──► DISCGRP (VSAM KSDS)
    trantype.txt  ──► TRANTYPE.jcl ──► IDCAMS DEF/REPRO ──► TRANTYPE (VSAM KSDS)
```

### Transaction Posting (POSTTRAN.jcl)

```
Input:                           Program:                    Output:
┌──────────────┐                 ┌──────────┐                ┌──────────────┐
│ DALYTRAN     │──── Read ──────►│ CBTRN02C │──── Write ────►│ TRANSACT     │
│ (Daily Trans)│                 │          │                │ (Master)     │
└──────────────┘                 │          │                └──────────────┘
┌──────────────┐                 │          │                ┌──────────────┐
│ CARDXREF     │──── Read ──────►│          │──── Write ────►│ TCATBALF     │
│ (Xref)       │                 │          │                │ (Cat Balance)│
└──────────────┘                 │          │                └──────────────┘
┌──────────────┐                 │          │                ┌──────────────┐
│ ACCTDAT      │──── Read/Upd ──►│          │──── Write ────►│ DALYREJS     │
│ (Accounts)   │                 │          │                │ (Rejects)    │
└──────────────┘                 └──────────┘                └──────────────┘
```

### Interest Calculation (INTCALC.jcl)

```
Input:                           Program:                    Output:
┌──────────────┐                 ┌──────────┐                ┌──────────────┐
│ TCATBALF     │──── Read ──────►│ CBACT04C │──── Write ────►│ SYSTRAN(+1)  │
│ (Cat Balance)│                 │          │                │ (New Trans)  │
└──────────────┘                 │          │                └──────────────┘
┌──────────────┐                 │          │                ┌──────────────┐
│ CARDXREF     │──── Read ──────►│          │──── Update ───►│ ACCTDAT      │
│ (via AIX)    │                 │          │                │ (Accounts)   │
└──────────────┘                 └──────────┘                └──────────────┘
┌──────────────┐                 
│ DISCGRP      │──── Read ──────►
│ (Int Rates)  │                 
└──────────────┘                 
┌──────────────┐                 
│ ACCTDAT      │──── Read ──────►
│ (Accounts)   │                 
└──────────────┘                 
```

### Transaction Backup (TRANBKP.jcl)

```
TRANSACT (VSAM KSDS) ──► SORT ──► TRANSACT.BKUP(+1) (GDG Sequential)
```

### Transaction Combine (COMBTRAN.jcl)

```
TRANSACT.BKUP(0) ──┐
                    ├──► SORT (merge) ──► TRANSACT.COMBINED(+1)
SYSTRAN(0)      ──┘                         │
                                            └──► IDCAMS REPRO ──► TRANSACT (VSAM)
```

### Statement Generation (CREASTMT.JCL)

```
Step 1: TRANSACT (VSAM) ──► SORT ──► TRXFL.SEQ ──► IDCAMS REPRO ──► TRXFL.VSAM.KSDS

Step 2:
┌──────────────┐                 ┌──────────┐                ┌──────────────┐
│ TRXFL.VSAM   │──── Read ──────►│ CBSTM03A │──── Write ────►│ STATEMNT.PS  │
│ (Sorted Trans)                 │    │      │                │ (Text Stmt)  │
└──────────────┘                 │    │      │                └──────────────┘
┌──────────────┐                 │    │      │                ┌──────────────┐
│ CARDXREF     │──── Read ──────►│    ▼      │──── Write ────►│ STATEMNT.HTML│
│              │                 │ CBSTM03B  │                │ (HTML Stmt)  │
└──────────────┘                 │ (submod)  │                └──────────────┘
┌──────────────┐                 │           │
│ ACCTDAT      │──── Read ──────►│           │
│              │                 │           │
└──────────────┘                 │           │
┌──────────────┐                 │           │
│ CUSTDAT      │──── Read ──────►│           │
│              │                 └───────────┘
└──────────────┘
```

### Transaction Report (TRANREPT.jcl)

```
Step 1: TRANSACT.BKUP(+1) ──► SORT ──► TRANSACT.DALY(+1)

Step 2:
┌──────────────┐                 ┌──────────┐                ┌──────────────┐
│ TRANSACT.DALY│──── Read ──────►│ CBTRN03C │──── Write ────►│ TRANREPT     │
│              │                 │          │                │ (Report)     │
└──────────────┘                 │          │                └──────────────┘
┌──────────────┐                 │          │
│ CARDXREF     │──── Read ──────►│          │
└──────────────┘                 │          │
┌──────────────┐                 │          │
│ TRANTYPE     │──── Read ──────►│          │
└──────────────┘                 │          │
┌──────────────┐                 │          │
│ TRANCATG     │──── Read ──────►│          │
└──────────────┘                 └──────────┘
```

### Data Export (CBEXPORT.jcl)

```
┌──────────────┐
│ CUSTDAT      │──── Read ──┐
│ ACCTDAT      │──── Read ──┤    ┌──────────┐     ┌──────────────┐
│ CARDXREF     │──── Read ──┼───►│ CBEXPORT │────►│ EXPORT.DATA  │
│ TRANSACT     │──── Read ──┤    └──────────┘     │ (Sequential) │
│ CARDDAT      │──── Read ──┘                     └──────────────┘
└──────────────┘
```

---

## Batch Cycle Execution Order

The nightly batch cycle runs in strict sequence (defined in scheduler configs):

```
Step  Job        Program/Utility   Depends On    Purpose
────  ─────────  ────────────────  ────────────  ──────────────────────────
 1    CLOSEFIL   IDCAMS            (none)        Close CICS VSAM files
 2    Data Refresh Jobs            CLOSEFIL      Reload VSAM files from PS
      ├── ACCTFILE   IDCAMS REPRO
      ├── CARDFILE   IDCAMS REPRO
      ├── CUSTFILE   IDCAMS REPRO
      ├── XREFFILE   IDCAMS REPRO
      └── TRANFILE   IDCAMS REPRO
 3    POSTTRAN   CBTRN02C          Data Refresh  Post daily transactions
 4    INTCALC    CBACT04C          POSTTRAN      Calculate interest
 5    TRANBKP    SORT + IDCAMS     INTCALC       Backup transaction master
 6    COMBTRAN   SORT + IDCAMS     TRANBKP       Combine all transactions
 7    CREASTMT   CBSTM03A + SORT   COMBTRAN      Generate statements
 8    TRANREPT   CBTRN03C + REPROC TRANBKP       Generate daily report
 9    TRANIDX    IDCAMS            COMBTRAN      Rebuild alternate indexes
10    OPENFIL    IDCAMS            All above     Reopen CICS VSAM files
```

### Dependency Chain (Critical Path)

```
CLOSEFIL ──► Data Refresh ──► POSTTRAN ──► INTCALC ──► TRANBKP ──► COMBTRAN ──► CREASTMT ──► OPENFIL
                                                           │
                                                           └──► TRANREPT (parallel with COMBTRAN)
```

---

## BMS Map to Program Binding

| BMS Map File  | Mapset Name | Map Name  | Bound Program | SEND/RECEIVE Operations |
|---------------|-------------|-----------|---------------|-------------------------|
| COSGN00.bms   | COSGN00     | COSGN0A   | COSGN00C      | SEND MAP, RECEIVE MAP   |
| COMEN01.bms   | COMEN01     | COMEN1A   | COMEN01C      | SEND MAP, RECEIVE MAP   |
| COADM01.bms   | COADM01     | COADM1A   | COADM01C      | SEND MAP, RECEIVE MAP   |
| COACTVW.bms   | COACTVW     | CACTVWA   | COACTVWC      | SEND MAP, RECEIVE MAP   |
| COACTUP.bms   | COACTUP     | CACTUPA   | COACTUPC      | SEND MAP, RECEIVE MAP   |
| COCRDLI.bms   | COCRDLI     | CCRDLIA   | COCRDLIC      | SEND MAP, RECEIVE MAP   |
| COCRDSL.bms   | COCRDSL     | CCRDSLA   | COCRDSLC      | SEND MAP, RECEIVE MAP   |
| COCRDUP.bms   | COCRDUP     | CCRDUPA   | COCRDUPC      | SEND MAP, RECEIVE MAP   |
| COTRN00.bms   | COTRN00     | COTRN0A   | COTRN00C      | SEND MAP, RECEIVE MAP   |
| COTRN01.bms   | COTRN01     | COTRN1A   | COTRN01C      | SEND MAP, RECEIVE MAP   |
| COTRN02.bms   | COTRN02     | COTRN2A   | COTRN02C      | SEND MAP, RECEIVE MAP   |
| CORPT00.bms   | CORPT00     | CORPT0A   | CORPT00C      | SEND MAP, RECEIVE MAP   |
| COBIL00.bms   | COBIL00     | COBIL0A   | COBIL00C      | SEND MAP, RECEIVE MAP   |
| COUSR00.bms   | COUSR00     | COUSR0A   | COUSR00C      | SEND MAP, RECEIVE MAP   |
| COUSR01.bms   | COUSR01     | COUSR1A   | COUSR01C      | SEND MAP, RECEIVE MAP   |
| COUSR02.bms   | COUSR02     | COUSR2A   | COUSR02C      | SEND MAP, RECEIVE MAP   |
| COUSR03.bms   | COUSR03     | COUSR3A   | COUSR03C      | SEND MAP, RECEIVE MAP   |

---

## End-to-End Data Flow Diagrams

### Flow 1: Customer Makes a Purchase (Online → Batch → Report)

```
1. Customer swipes card at POS terminal
   │
2. Transaction arrives as daily feed file (DALYTRAN)
   │
3. CLOSEFIL job closes CICS files for batch
   │
4. POSTTRAN (CBTRN02C):
   ├── Reads DALYTRAN record
   ├── Looks up CARDXREF by card number → gets ACCT-ID
   ├── Validates account exists in ACCTDAT
   ├── Updates ACCTDAT: ACCT-CURR-BAL += TRAN-AMT
   ├── Updates/Creates TCATBALF: category running balance
   ├── Writes to TRANSACT (master)
   └── Rejects invalid → DALYREJS
   │
5. INTCALC (CBACT04C):
   ├── Reads TCATBALF per account
   ├── Looks up DISCGRP for interest rate
   ├── Calculates interest: BAL × RATE / 365
   ├── Creates interest transaction → SYSTRAN(+1)
   └── Updates ACCTDAT with new balance
   │
6. TRANBKP: Backs up TRANSACT to GDG
   │
7. COMBTRAN: Merges backup + new system transactions → TRANSACT
   │
8. CREASTMT (CBSTM03A):
   ├── Reads TRANSACT (sorted by account)
   ├── Reads CUSTDAT for name/address
   ├── Reads ACCTDAT for balance summary
   └── Writes STATEMNT.PS (text) and STATEMNT.HTML
   │
9. OPENFIL reopens CICS files
   │
10. Customer logs in (COSGN00C → COMEN01C)
    └── Views transaction via COTRN00C → COTRN01C
```

### Flow 2: Admin Manages Users (Online Only)

```
1. Admin logs in: COSGN00C (CC00) → validates USRSEC → COADM01C
   │
2. Admin selects "User List": COADM01C → COUSR00C
   ├── Browses USRSEC file (STARTBR/READNEXT)
   ├── Selects 'U' to update → COUSR02C (reads/rewrites USRSEC)
   └── Selects 'D' to delete → COUSR03C (reads/deletes from USRSEC)
   │
3. Admin selects "User Add": COADM01C → COUSR01C
   └── Writes new record to USRSEC
```

### Flow 3: Account Update with Validation (Online)

```
1. User selects "Account Update" from menu
   │
2. COMEN01C → COACTUPC (XCTL)
   │
3. COACTUPC:
   ├── Reads CXACAIX (card xref by account) → gets card/customer info
   ├── Reads ACCTDAT → displays current account data
   ├── Reads CUSTDAT → displays customer info
   ├── Validates all input fields using:
   │   ├── CSSETATY (field attributes, 40+ field validations)
   │   ├── CSSTRPFY (string cleaning)
   │   ├── CSUTLDWY (date validation)
   │   └── CSLKPCDY (state/ZIP/area code lookup)
   ├── On confirmation:
   │   ├── REWRITE ACCTDAT (account changes)
   │   └── REWRITE CUSTDAT (customer changes)
   └── PF3 → returns to COMEN01C
```
