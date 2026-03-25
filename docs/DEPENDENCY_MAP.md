# DEPENDENCY MAP — CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Analysis:** Static call-graph extraction, COPY statement tracing, JCL DD/DSN lineage

---

## Table of Contents

1. [Program Call Graph](#1-program-call-graph)
2. [CICS Program Transfer Map](#2-cics-program-transfer-map)
3. [Copybook Usage Matrix](#3-copybook-usage-matrix)
4. [VSAM File Access Map (Online Programs)](#4-vsam-file-access-map-online-programs)
5. [Batch File I/O Map](#5-batch-file-io-map)
6. [JCL Job → Program Execution Map](#6-jcl-job--program-execution-map)
7. [JCL Job → Dataset Lineage](#7-jcl-job--dataset-lineage)
8. [Batch Processing Data Flow](#8-batch-processing-data-flow)
9. [End-to-End Batch Cycle](#9-end-to-end-batch-cycle)

---

## 1. Program Call Graph

### CALL Statements (Subroutine Calls)

```
CBSTM03A ──CALL──► CBSTM03B        (Statement generation calls file-processing subroutine)
CBTRN01C ──CALL──► CSUTLDTC        (Transaction processing calls date utility)
CBTRN02C ──CALL──► CSUTLDTC        (Transaction posting calls date utility)
CBACT04C ──CALL──► CSUTLDTC        (Interest calculation calls date utility)
CSUTLDTC ──CALL──► CEEDAYS         (Date utility calls LE runtime for date conversion)
CSUTLDTC ──CALL──► CEEDATM         (Date utility calls LE runtime for date formatting)
```

### CICS XCTL Transfers (Program-to-Program Navigation)

```
COSGN00C ──XCTL──► COMEN01C        (Sign-on → Main Menu, for regular users)
COSGN00C ──XCTL──► COADM01C        (Sign-on → Admin Menu, for admin users)

COMEN01C ──XCTL──► COACTVWC        (Menu option 1: Account View)
COMEN01C ──XCTL──► COACTUPC        (Menu option 2: Account Update)
COMEN01C ──XCTL──► COCRDLIC        (Menu option 3: Credit Card List)
COMEN01C ──XCTL──► COCRDSLC        (Menu option 4: Credit Card View)
COMEN01C ──XCTL──► COCRDUPC        (Menu option 5: Credit Card Update)
COMEN01C ──XCTL──► COTRN00C        (Menu option 6: Transaction List)
COMEN01C ──XCTL──► COTRN01C        (Menu option 7: Transaction View)
COMEN01C ──XCTL──► COTRN02C        (Menu option 8: Transaction Add)
COMEN01C ──XCTL──► CORPT00C        (Menu option 9: Transaction Reports)
COMEN01C ──XCTL──► COBIL00C        (Menu option 10: Bill Payment)
COMEN01C ──XCTL──► COPAUS0C        (Menu option 11: Pending Auth View — optional module)

COADM01C ──XCTL──► COUSR00C        (Admin option 1: User List)
COADM01C ──XCTL──► COUSR01C        (Admin option 2: User Add)
COADM01C ──XCTL──► COUSR02C        (Admin option 3: User Update)
COADM01C ──XCTL──► COUSR03C        (Admin option 4: User Delete)
COADM01C ──XCTL──► COTRTLIC        (Admin option 5: Tran Type List — optional DB2 module)
COADM01C ──XCTL──► COTRTUPC        (Admin option 6: Tran Type Maint — optional DB2 module)

(All online programs can XCTL back to their parent menu via CDEMO-TO-PROGRAM)
```

---

## 2. CICS Program Transfer Map

```
                                    ┌──────────┐
                                    │ COSGN00C │  (CC00 - Entry Point)
                                    │ Sign-on  │
                                    └────┬─────┘
                            ┌────────────┴────────────┐
                            ▼                         ▼
                     ┌──────────┐              ┌──────────┐
                     │ COMEN01C │              │ COADM01C │
                     │Main Menu │              │Admin Menu│
                     └────┬─────┘              └────┬─────┘
          ┌───┬───┬───┬───┼───┬───┬───┬───┬───┐    │
          ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼    ├──► COUSR00C (User List)
       COACTVWC  COCRDLIC  COTRN00C  CORPT00C  │    ├──► COUSR01C (User Add)
       COACTUPC  COCRDSLC  COTRN01C  COBIL00C  │    ├──► COUSR02C (User Update)
                 COCRDUPC  COTRN02C         COPAUS0C └──► COUSR03C (User Delete)
```

---

## 3. Copybook Usage Matrix

| Copybook | COSGN | COMEN | COADM | COACTV | COACTU | COCRDL | COCRDS | COCRDU | COTRN0 | COTRN1 | COTRN2 | CORPT | COBIL | COUSR0 | COUSR1 | COUSR2 | COUSR3 |
|----------|:-----:|:-----:|:-----:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:-----:|:-----:|:------:|:------:|:------:|:------:|
| COCOM01Y |   x   |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| COTTL01Y |       |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| CSDAT01Y |       |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| CSMSG01Y |       |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| CSMSG02Y |       |       |       |   x    |   x    |        |   x    |   x    |        |        |        |       |       |        |        |        |        |
| CSUSR01Y |   x   |   x   |   x   |   x    |   x    |   x    |   x    |   x    |        |        |        |       |       |   x    |   x    |   x    |   x    |
| DFHAID   |       |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| DFHBMSCA |       |   x   |   x   |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x    |   x   |   x   |   x    |   x    |   x    |   x    |
| CVACT01Y |       |       |       |   x    |   x    |        |        |        |        |        |   x    |       |       |        |        |        |        |
| CVACT02Y |       |       |       |   x    |   x    |   x    |   x    |   x    |        |        |        |       |       |        |        |        |        |
| CVACT03Y |       |       |       |   x    |   x    |        |        |        |        |        |   x    |       |       |        |        |        |        |
| CVCUS01Y |       |       |       |   x    |   x    |        |   x    |   x    |        |        |        |       |       |        |        |        |        |
| CVCRD01Y |       |       |       |   x    |        |   x    |   x    |        |        |        |        |       |       |        |        |        |        |
| CVTRA05Y |       |       |       |        |        |        |        |        |   x    |   x    |   x    |   x   |   x   |        |        |        |        |
| COMEN02Y |       |   x   |       |        |        |        |        |        |        |        |        |       |       |        |        |        |        |
| COADM02Y |       |       |   x   |        |        |        |        |        |        |        |        |       |       |        |        |        |        |

**Legend:** `x` = COPY statement present in program. Columns are truncated program names (e.g., COSGN = COSGN00C).

### Batch Program Copybook Usage

| Copybook | CBTRN01 | CBTRN02 | CBTRN03 | CBACT01 | CBACT02 | CBACT03 | CBACT04 | CBCUS01 | CBSTM03A | CBEXPORT | CBIMPORT |
|----------|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|
| CVACT01Y |    x    |         |         |    x    |         |         |    x    |         |    x     |    x     |          |
| CVACT02Y |         |         |         |         |    x    |         |         |         |          |    x     |          |
| CVACT03Y |    x    |         |    x    |         |         |    x    |    x    |         |    x     |    x     |          |
| CVCUS01Y |    x    |         |         |         |         |         |         |    x    |          |    x     |          |
| CVTRA05Y |    x    |    x    |    x    |         |         |         |    x    |         |          |    x     |          |
| CVTRA06Y |    x    |    x    |         |         |         |         |         |         |          |          |          |
| CVTRA01Y |         |         |         |         |         |         |    x    |         |          |          |          |
| CVTRA02Y |         |         |         |         |         |         |    x    |         |          |          |          |
| CVTRA03Y |         |         |    x    |         |         |         |         |         |          |          |          |
| CVTRA04Y |         |         |    x    |         |         |         |         |         |          |          |          |
| CVTRA07Y |         |         |    x    |         |         |         |         |         |          |          |          |
| CVEXPORT |         |         |         |         |         |         |         |         |          |    x     |          |
| COSTM01  |         |         |         |         |         |         |         |         |    x     |          |          |
| CUSTREC  |         |         |         |         |         |         |         |         |    x     |          |          |
| CODATECN |         |         |         |    x    |         |         |         |         |          |          |          |

---

## 4. VSAM File Access Map (Online Programs)

| Program   | USRSEC | ACCTDAT | CARDDATA | CUSTDATA | TRANSACT | CARDXREF | CXACAIX |
|-----------|:------:|:-------:|:--------:|:--------:|:--------:|:--------:|:-------:|
| COSGN00C  |  R     |         |          |          |          |          |         |
| COMEN01C  |        |         |          |          |          |          |         |
| COADM01C  |        |         |          |          |          |          |         |
| COACTVWC  |        |  R      |          |  R       |          |  R(AIX)  |         |
| COACTUPC  |        |  R/W    |          |  R       |          |  R(AIX)  |         |
| COCRDLIC  |        |         |  R       |          |          |          |         |
| COCRDSLC  |        |         |  R       |  R       |          |          |         |
| COCRDUPC  |        |         |  R/W     |  R       |          |          |         |
| COTRN00C  |        |         |          |          |  R(browse)|          |         |
| COTRN01C  |        |         |          |          |  R       |          |         |
| COTRN02C  |        |         |          |          |  R/W     |  R       | R       |
| COBIL00C  |        |  R/W    |          |          |  R/W     |          | R       |
| CORPT00C  |        |         |          |          |          |          |         |
| COUSR00C  |  R(browse)|      |          |          |          |          |         |
| COUSR01C  |  W     |         |          |          |          |          |         |
| COUSR02C  |  R/W   |         |          |          |          |          |         |
| COUSR03C  |  R/D   |         |          |          |          |          |         |

**Legend:** `R` = Read, `W` = Write, `D` = Delete, `R/W` = Read + Rewrite, `R(browse)` = Browse (STARTBR/READNEXT/READPREV/ENDBR), `R(AIX)` = Read via Alternate Index

---

## 5. Batch File I/O Map

| Program   | DALYTRAN | TRANSACT | ACCTDATA | CARDDATA | CUSTDATA | CARDXREF | TCATBAL | DALYREJS | DISCGRP | TRANTYPE | TRANCATG | Export |
|-----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:--------:|:-------:|:--------:|:--------:|:------:|
| CBTRN01C  | R        | R        | R        | R        | R        | R        |         |          |         |          |          |        |
| CBTRN02C  | R        | W        | R/W      |          |          | R        | R/W     | W        |         |          |          |        |
| CBTRN03C  |          | R        |          |          |          | R        |         |          |         | R        | R        |        |
| CBACT01C  |          |          | R        |          |          |          |         |          |         |          |          |        |
| CBACT02C  |          |          |          | R        |          |          |         |          |         |          |          |        |
| CBACT03C  |          |          |          |          |          | R        |         |          |         |          |          |        |
| CBACT04C  |          | R        | R/W      |          |          | R        | R/W     |          | R       |          |          |        |
| CBCUS01C  |          |          |          |          | R        |          |         |          |         |          |          |        |
| CBSTM03A  |          | R(TRXFL) | R        |          | R        | R        |         |          |         |          |          |        |
| CBEXPORT  |          | R        | R        | R        | R        | R        |         |          |         |          |          | W      |
| CBIMPORT  |          | W        | W        | W        |  W       | W        |         |          |         |          |          | R      |

**Legend:** `R` = Read, `W` = Write, `R/W` = Read + Rewrite

---

## 6. JCL Job → Program Execution Map

| JCL Job    | Step(s) | Program(s) Executed          | Purpose                          |
|------------|---------|------------------------------|----------------------------------|
| POSTTRAN   | STEP15  | **CBTRN02C**                 | Post daily transactions          |
| INTCALC    | STEP15  | **CBACT04C**                 | Calculate interest               |
| CREASTMT   | STEP040 | **CBSTM03A** (→ CBSTM03B)   | Generate account statements      |
| TRANREPT   | STEP05R, STEP10R | SORT, **CBTRN03C**   | Generate transaction reports     |
| READACCT   | STEP05  | **CBACT01C**                 | Read/validate account file       |
| READCARD   | STEP05  | **CBACT02C**                 | Read/validate card file          |
| READXREF   | STEP05  | **CBACT03C**                 | Read/validate xref file          |
| READCUST   | STEP05  | **CBCUS01C**                 | Read/validate customer file      |
| CBEXPORT   | STEP02  | **CBEXPORT**                 | Export VSAM to sequential        |
| CBIMPORT   | STEP01  | **CBIMPORT**                 | Import sequential to VSAM        |
| WAITSTEP   | WAIT    | **COBSWAIT**                 | Timed wait utility               |
| COMBTRAN   | STEP05R, STEP10 | SORT, IDCAMS          | Combine daily+master transactions|
| TRANBKP    | STEP05, STEP10 | IDCAMS                 | Backup transaction file          |
| CLOSEFIL   | CLCIFIL | SDSF                         | Close CICS files                 |
| OPENFIL    | OPCIFIL | SDSF                         | Open CICS files                  |
| All data loads | various | IDCAMS (DEFINE, REPRO)   | Define VSAM clusters & load data |

---

## 7. JCL Job → Dataset Lineage

### Data Flow: Which Jobs Read/Write Which Datasets

```
DATASET                          LOADED BY        READ BY (Batch)        READ BY (Online)
─────────────────────────────────────────────────────────────────────────────────────────
ACCTDATA.VSAM.KSDS               ACCTFILE         CBTRN02C, CBACT04C,    COACTVWC, COACTUPC,
                                                  CBSTM03A, CBEXPORT     COBIL00C
                                                  READACCT(CBACT01C)

CARDDATA.VSAM.KSDS               CARDFILE         CBEXPORT               COCRDLIC, COCRDSLC,
                                                  READCARD(CBACT02C)     COCRDUPC

CUSTDATA.VSAM.KSDS               CUSTFILE         CBSTM03A, CBEXPORT     COACTVWC, COACTUPC,
                                                  READCUST(CBCUS01C)     COCRDSLC, COCRDUPC

TRANSACT.VSAM.KSDS               TRANFILE         CBTRN02C(W),CBTRN03C,  COTRN00C, COTRN01C,
                                                  CBACT04C, CBSTM03A,    COTRN02C, COBIL00C
                                                  CBEXPORT

CARDXREF.VSAM.KSDS               XREFFILE         CBTRN01C, CBTRN02C,    COACTVWC, COACTUPC,
                                                  CBTRN03C, CBACT04C,    COTRN02C, COBIL00C
                                                  CBSTM03A, CBEXPORT

USRSEC.VSAM.KSDS                 DUSRSECJ         —                      COSGN00C, COUSR00C-03C

TCATBAL.VSAM.KSDS                TCATBALF         CBTRN02C(R/W),         —
                                                  CBACT04C(R/W)

DISCGRP.VSAM.KSDS                DISCGRP          CBACT04C               —

TRANTYPE.VSAM.KSDS               TRANTYPE         CBTRN03C               —

TRANCATG.VSAM.KSDS               TRANCATG         CBTRN03C               —

DAILYTRAN (sequential)            (external)       CBTRN01C, CBTRN02C     —

DALYREJS.VSAM.KSDS               DALYREJS         CBTRN02C(W)            —

STATEMNT.PS / STATEMNT.HTML       CREASTMT         —                      —
```

---

## 8. Batch Processing Data Flow

### Transaction Posting Flow (POSTTRAN → CBTRN02C)

```
                    ┌─────────────┐
                    │  DAILYTRAN  │  (Daily transaction input file)
                    │ (Sequential)│
                    └──────┬──────┘
                           │ READ
                           ▼
                    ┌─────────────┐     READ      ┌──────────────┐
                    │  CBTRN02C   │──────────────►│  CARDXREF    │
                    │  (Posting)  │               │  (Lookup)    │
                    └──┬──┬──┬────┘               └──────────────┘
                       │  │  │
              WRITE    │  │  │ READ/REWRITE
              ┌────────┘  │  └────────┐
              ▼           ▼           ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ TRANSACT │ │ ACCTDATA │ │ TCATBAL  │
        │ (Master) │ │(Balances)│ │(Cat Bal) │
        └──────────┘ └──────────┘ └──────────┘
              │
              │  REJECTED
              ▼
        ┌──────────┐
        │ DALYREJS │
        │ (Rejects)│
        └──────────┘
```

### Interest Calculation Flow (INTCALC → CBACT04C)

```
        ┌──────────┐    ┌──────────┐    ┌──────────┐
        │ TCATBAL  │    │ DISCGRP  │    │ CARDXREF │
        │(Cat Bal) │    │(Int Rate)│    │ (Lookup) │
        └────┬─────┘    └────┬─────┘    └────┬─────┘
             │ READ          │ READ          │ READ
             └───────┬───────┘───────┬───────┘
                     ▼               │
              ┌─────────────┐        │
              │  CBACT04C   │◄───────┘
              │ (Interest)  │
              └──┬──────┬───┘
                 │      │
        REWRITE  │      │ WRITE (interest txn)
                 ▼      ▼
           ┌──────────┐ ┌──────────┐
           │ ACCTDATA │ │ TRANSACT │
           │(Balances)│ │ (Master) │
           └──────────┘ └──────────┘
```

### Statement Generation Flow (CREASTMT → CBSTM03A)

```
        ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
        │  TRXFL   │    │ CARDXREF │    │ ACCTDATA │    │ CUSTDATA │
        │(Sorted)  │    │ (Lookup) │    │(Account) │    │(Customer)│
        └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
             │ READ          │ READ          │ READ          │ READ
             └───────┬───────┘───────┬───────┘───────┬───────┘
                     ▼                                │
              ┌─────────────┐                         │
              │  CBSTM03A   │◄────────────────────────┘
              │ (Statement) │
              │      │      │
              │ CALL ▼      │
              │  CBSTM03B   │
              └──┬──────┬───┘
                 │      │
                 ▼      ▼
           ┌──────────┐ ┌──────────┐
           │STATEMNT  │ │STATEMNT  │
           │  .PS     │ │ .HTML    │
           │(Text)    │ │ (HTML)   │
           └──────────┘ └──────────┘
```

---

## 9. End-to-End Batch Cycle

The nightly batch cycle runs in a specific order, managed by CA-7 or Control-M scheduler:

```
Phase 1: CLOSE CICS FILES
  ┌──────────┐
  │ CLOSEFIL │  Close all VSAM files from CICS
  └────┬─────┘
       ▼
Phase 2: DATA REFRESH (parallel)
  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ ACCTFILE │  │ CARDFILE │  │ CUSTFILE │  │ XREFFILE │  │ TRANFILE │
  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
       └──────┬──────┘──────┬──────┘──────┬──────┘──────┬──────┘
              ▼             ▼             ▼             ▼
Phase 3: TRANSACTION POSTING
  ┌──────────┐
  │ POSTTRAN │  CBTRN02C: Post daily transactions to master
  └────┬─────┘
       ▼
Phase 4: INTEREST CALCULATION
  ┌──────────┐
  │ INTCALC  │  CBACT04C: Calculate interest charges
  └────┬─────┘
       ▼
Phase 5: BACKUP
  ┌──────────┐
  │ TRANBKP  │  Backup transaction master file
  └────┬─────┘
       ▼
Phase 6: COMBINE TRANSACTIONS
  ┌──────────┐
  │ COMBTRAN │  Merge daily transactions into master
  └────┬─────┘
       ▼
Phase 7: STATEMENT GENERATION
  ┌──────────┐
  │ CREASTMT │  CBSTM03A: Produce text + HTML statements
  └────┬─────┘
       ▼
Phase 8: REBUILD INDEXES
  ┌──────────┐
  │ TRANIDX  │  Rebuild alternate indexes on TRANSACT
  └────┬─────┘
       ▼
Phase 9: OPEN CICS FILES
  ┌──────────┐
  │ OPENFIL  │  Reopen VSAM files for CICS online access
  └──────────┘
```

### Optional Post-Batch Jobs

```
  ┌──────────┐
  │ TRANREPT │  Generate daily transaction report (CBTRN03C)
  └──────────┘
  ┌──────────┐
  │ TXT2PDF1 │  Convert statement text file to PDF
  └──────────┘
  ┌──────────┐
  │ PRTCATBL │  Print transaction category balance report
  └──────────┘
```
