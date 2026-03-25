# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Purpose:** Call graph, copybook dependencies, data lineage, and screen navigation flow

---

## Table of Contents

1. [Online Program Call Graph (CICS Navigation)](#online-program-call-graph-cics-navigation)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Copybook Dependency Matrix](#copybook-dependency-matrix)
4. [VSAM File Access Map](#vsam-file-access-map)
5. [JCL Job-to-Program Execution Map](#jcl-job-to-program-execution-map)
6. [Batch Job Sequencing (Data Flow)](#batch-job-sequencing-data-flow)
7. [End-to-End Data Lineage](#end-to-end-data-lineage)
8. [Screen Navigation Flow](#screen-navigation-flow)

---

## Online Program Call Graph (CICS Navigation)

Online programs communicate via `EXEC CICS XCTL` (transfer control) passing a shared COMMAREA (`COCOM01Y`). The menu copybooks (`COMEN02Y`, `COADM02Y`) define which programs are reachable from each menu.

```
                              ┌──────────────┐
                              │  COSGN00C    │
                              │  (Sign-On)   │
                              └──────┬───────┘
                                     │
                          ┌──────────┴──────────┐
                          │ Admin?              │ Regular?
                          ▼                      ▼
                   ┌──────────────┐      ┌──────────────┐
                   │  COADM01C    │      │  COMEN01C    │
                   │  (Admin Menu)│      │  (Main Menu) │
                   └──────┬───────┘      └──────┬───────┘
                          │                      │
         ┌────────────────┤        ┌─────────────┼─────────────────┐
         │                │        │             │                 │
         ▼                ▼        ▼             ▼                 ▼
  ┌────────────┐  ┌────────────┐ ... (All 11 menu options below)
  │ COUSR00C   │  │ COUSR01C   │
  │ (User List)│  │ (User Add) │
  └────────────┘  └────────────┘
```

### Main Menu (COMEN01C) -- Dispatches to 11 Programs

| Menu # | Target Program | Function                    | User Type |
|--------|---------------|-----------------------------|-----------|
| 1      | COACTVWC      | Account View                | U (any)   |
| 2      | COACTUPC      | Account Update              | U (any)   |
| 3      | COCRDLIC      | Card List                   | U (any)   |
| 4      | COCRDSLC      | Card Detail View            | U (any)   |
| 5      | COCRDUPC      | Card Update                 | U (any)   |
| 6      | COTRN00C      | Transaction List            | U (any)   |
| 7      | COTRN01C      | Transaction Detail View     | U (any)   |
| 8      | COTRN02C      | Transaction Add             | U (any)   |
| 9      | CORPT00C      | Transaction Report          | U (any)   |
| 10     | COBIL00C      | Bill Payment                | U (any)   |
| 11     | COPAUS0C      | Authorization Summary (opt) | U (any)   |

### Admin Menu (COADM01C) -- Dispatches to 6 Programs

| Menu # | Target Program | Function                    |
|--------|---------------|-----------------------------|
| 1      | COUSR00C      | User List / Browse          |
| 2      | COUSR01C      | User Add                    |
| 3      | COUSR02C      | User Update                 |
| 4      | COUSR03C      | User Delete                 |
| 5      | COTRTLIC      | Transaction Type List (opt) |
| 6      | COTRTUPC      | Transaction Type Update(opt)|

### Inter-Program XCTL Transfers

| Source Program | Target Program          | Trigger / Condition                  |
|----------------|-------------------------|--------------------------------------|
| COSGN00C       | COADM01C                | Admin user login (SEC-USR-TYPE='A')  |
| COSGN00C       | COMEN01C                | Regular user login (SEC-USR-TYPE='U')|
| COMEN01C       | (menu option program)   | User selects menu option 1-11        |
| COADM01C       | (admin option program)  | Admin selects option 1-6             |
| COACTVWC       | CDEMO-TO-PROGRAM        | Back to calling program              |
| COACTUPC       | CDEMO-TO-PROGRAM        | Back to calling program              |
| COCRDLIC       | LIT-MENUPGM (COMEN01C) | Return to menu                       |
| COCRDLIC       | CCARD-NEXT-PROG         | Drill into card detail/update        |
| COCRDSLC       | CDEMO-TO-PROGRAM        | Back to calling program              |
| COCRDUPC       | CDEMO-TO-PROGRAM        | Back to calling program              |

### CALL Statements (Subroutine Calls within Online Programs)

| Caller Program | Called Program | Purpose                    |
|----------------|---------------|----------------------------|
| CORPT00C       | CSUTLDTC      | Date/time validation       |
| COTRN02C       | CSUTLDTC      | Date/time validation       |
| COBSWAIT       | MVSWAIT (ASM) | MVS wait/delay             |

---

## Batch Program Call Graph

```
 ┌─────────────────────────────────────────────────────────┐
 │                   JCL POSTTRAN                          │
 │  ┌──────────┐         ┌──────────┐                      │
 │  │ CBTRN01C │────────>│ CBTRN02C │                      │
 │  │ (Daily   │ feeds   │ (Master  │                      │
 │  │  Post)   │         │  Update) │                      │
 │  └──────────┘         └──────────┘                      │
 └─────────────────────────────────────────────────────────┘

 ┌─────────────────────────────────────────────────────────┐
 │                   JCL INTCALC                           │
 │  ┌──────────┐                                           │
 │  │ CBACT04C │  Reads: TRANSACT, ACCTDATA, TCATBAL,     │
 │  │ (Interest│         DISCGRP, CARDXREF                 │
 │  │  Calc)   │  Writes: ACCTDATA, TCATBAL, TRANSACT     │
 │  └──────────┘                                           │
 └─────────────────────────────────────────────────────────┘

 ┌─────────────────────────────────────────────────────────┐
 │                   JCL CREASTMT                          │
 │  SORT ──> IDCAMS ──> ┌──────────┐                       │
 │                      │ CBSTM03A │──── CALL ────> CBSTM03B│
 │                      │ (Driver) │   (13 calls)  (Format) │
 │                      └──────────┘                        │
 │  Reads: TRXFL, XREFFILE, ACCTFILE, CUSTFILE             │
 │  Writes: STATEMNT.PS, STATEMNT.HTML                     │
 └─────────────────────────────────────────────────────────┘

 ┌─────────────────────────────────────────────────────────┐
 │                   JCL TRANREPT                          │
 │  SORT ──> ┌──────────┐                                  │
 │           │ CBTRN03C │  Reads: TRANFILE, CARDXREF,      │
 │           │ (Report  │         TRANTYPE, TRANCATG       │
 │           │  Gen)    │  Writes: TRANREPT (GDG)          │
 │           └──────────┘                                  │
 └─────────────────────────────────────────────────────────┘
```

### Complete Batch CALL Graph

| Caller       | Callee       | # of CALLs | Purpose                             |
|--------------|--------------|------------|-------------------------------------|
| CBSTM03A     | CBSTM03B     | 13         | Format statement line sections      |
| CBSTM03A     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBACT01C     | COBDATFT     | 1          | Date format conversion (ASM)        |
| CBACT01C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBACT02C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBACT03C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBACT04C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBCUS01C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBTRN01C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBTRN02C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBTRN03C     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBEXPORT     | CEE3ABD      | 1          | Abnormal termination (error)        |
| CBIMPORT     | CEE3ABD      | 1          | Abnormal termination (error)        |

> **Note:** `CEE3ABD` is the LE (Language Environment) abend routine -- used for controlled abnormal program termination on error conditions. All batch programs use this as a last-resort error handler.

---

## Copybook Dependency Matrix

Each row is a COBOL program; each column is a copybook. An `X` means the program contains a `COPY` statement for that copybook.

### Online Programs vs. Core Copybooks

| Program    | COCOM01Y | COTTL01Y | CSDAT01Y | CSMSG01Y | CSMSG02Y | CSUSR01Y | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CVCRD01Y | DFHAID | DFHBMSCA | CSSTRPFY | CSSETATY |
|------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:------:|:--------:|:--------:|:--------:|
| COSGN00C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COMEN01C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COADM01C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COACTVWC   |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |          |    X     |   X    |    X     |    X     |          |
| COACTUPC   |    X     |    X     |    X     |    X     |    X     |    X     |    X     |          |    X     |    X     |          |    X     |   X    |    X     |    X     |    X     |
| COCRDLIC   |    X     |    X     |    X     |    X     |    X     |    X     |          |    X     |          |          |          |    X     |   X    |    X     |    X     |          |
| COCRDSLC   |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |          |    X     |   X    |    X     |    X     |          |
| COCRDUPC   |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |          |    X     |   X    |    X     |    X     |          |
| COTRN00C   |    X     |    X     |    X     |    X     |          |          |          |          |          |          |    X     |          |   X    |    X     |          |          |
| COTRN01C   |    X     |    X     |    X     |    X     |          |          |          |          |          |          |    X     |          |   X    |    X     |          |          |
| COTRN02C   |    X     |    X     |    X     |    X     |          |          |    X     |          |    X     |          |    X     |          |   X    |    X     |          |          |
| CORPT00C   |    X     |    X     |    X     |    X     |          |          |          |          |          |          |    X     |          |   X    |    X     |          |          |
| COBIL00C   |    X     |    X     |    X     |    X     |          |          |    X     |          |    X     |          |    X     |          |   X    |    X     |          |          |
| COUSR00C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COUSR01C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COUSR02C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |
| COUSR03C   |    X     |    X     |    X     |    X     |          |    X     |          |          |          |          |          |          |   X    |    X     |          |          |

### Batch Programs vs. Data Copybooks

| Program    | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CVTRA06Y | CVTRA01Y | CVTRA02Y | CVTRA03Y | CVTRA04Y | CVTRA07Y | CVEXPORT | COSTM01 | CUSTREC | CODATECN |
|------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:-------:|:--------:|
| CBACT01C   |    X     |          |          |          |          |          |          |          |          |          |          |          |         |         |    X     |
| CBACT02C   |          |    X     |          |          |          |          |          |          |          |          |          |          |         |         |          |
| CBACT03C   |          |          |    X     |          |          |          |          |          |          |          |          |          |         |         |          |
| CBACT04C   |    X     |          |    X     |          |    X     |          |    X     |    X     |          |          |          |          |         |         |          |
| CBCUS01C   |          |          |          |    X     |          |          |          |          |          |          |          |          |         |         |          |
| CBTRN01C   |    X     |    X     |    X     |    X     |    X     |    X     |          |          |          |          |          |          |         |         |          |
| CBTRN02C   |    X     |          |    X     |          |    X     |    X     |    X     |          |          |          |          |          |         |         |          |
| CBTRN03C   |          |          |    X     |          |    X     |          |          |          |    X     |    X     |    X     |          |         |         |          |
| CBSTM03A   |    X     |          |    X     |          |          |          |          |          |          |          |          |          |    X    |    X    |          |
| CBEXPORT   |    X     |    X     |    X     |    X     |    X     |          |          |          |          |          |          |    X     |         |         |          |
| CBIMPORT   |    X     |    X     |    X     |    X     |    X     |          |          |          |          |          |          |    X     |         |         |          |

### Most-Referenced Copybooks (Fanout)

| Copybook   | Used By # Programs | Classification        |
|------------|-------------------:|-----------------------|
| COCOM01Y   |                 17 | Universal (online)    |
| COTTL01Y   |                 17 | Universal (online)    |
| CSDAT01Y   |                 17 | Universal (online)    |
| CSMSG01Y   |                 17 | Universal (online)    |
| DFHAID     |                 17 | Universal (CICS)      |
| DFHBMSCA   |                 17 | Universal (CICS)      |
| CSUSR01Y   |                 11 | Security-related pgms |
| CVACT01Y   |                 10 | Account-related pgms  |
| CVACT03Y   |                 10 | Xref-related pgms     |
| CVTRA05Y   |                  9 | Transaction pgms      |
| CVCUS01Y   |                  7 | Customer pgms         |
| CVACT02Y   |                  7 | Card pgms             |

---

## VSAM File Access Map

Shows which programs read from and write to each VSAM file (online via CICS and batch via sequential I/O).

### Online CICS File Access

| VSAM File (Logical Name)       | Read By                                        | Write / Rewrite By                    |
|---------------------------------|------------------------------------------------|---------------------------------------|
| USRSEC (User Security)          | COSGN00C, COUSR00C, COUSR02C, COUSR03C        | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| ACCTDAT (Account Data)          | COACTVWC, COACTUPC, COBIL00C                   | COACTUPC (REWRITE), COBIL00C (REWRITE) |
| CARDDAT (Card Data)             | COCRDLIC, COCRDSLC, COCRDUPC                   | COCRDUPC (REWRITE)                    |
| CUSTDAT (Customer Data)         | COACTVWC, COCRDSLC, COACTUPC                   | COACTUPC (REWRITE)                    |
| CARDXREF (Cross-Reference)      | COACTVWC, COACTUPC, COBIL00C, COTRN02C        | -                                     |
| CXACAIX (Xref Alternate Index)  | COBIL00C, COTRN02C                              | -                                     |
| TRANSACT (Transaction)          | COTRN00C, COTRN01C, COTRN02C, COBIL00C        | COTRN02C (WRITE), COBIL00C (WRITE)    |

### Batch File Access

| VSAM / Sequential File          | Read By                              | Write By                              |
|----------------------------------|--------------------------------------|---------------------------------------|
| ACCTDATA.VSAM.KSDS              | CBSTM03A, CBACT04C, CBEXPORT        | CBACT01C (load), CBACT04C (update), CBIMPORT |
| CARDDATA.VSAM.KSDS              | CBEXPORT                              | CBACT02C (load), CBIMPORT             |
| CARDXREF.VSAM.KSDS              | CBSTM03A, CBTRN03C, CBTRN01C, CBTRN02C, CBACT04C, CBEXPORT | CBACT03C (load), CBIMPORT |
| CUSTDATA.VSAM.KSDS              | CBSTM03A, CBEXPORT                   | CBCUS01C (load), CBIMPORT            |
| TRANSACT.VSAM.KSDS              | CBTRN02C, CBACT04C, CBEXPORT, CBTRN03C | CBTRN02C (update), CBACT04C (update), CBIMPORT |
| DALYTRAN.PS                      | CBTRN01C                              | (external feed / JCL REPRO)          |
| TRANTYPE.VSAM.KSDS              | CBTRN03C                              | (JCL TRANTYPE load)                  |
| TRANCATG.VSAM.KSDS              | CBTRN03C                              | (JCL TRANCATG load)                  |
| TCATBAL.VSAM.KSDS               | CBACT04C                              | CBACT04C (update)                    |
| DISCGRP.VSAM.KSDS               | CBACT04C                              | (JCL DISCGRP load)                   |
| USRSEC.VSAM.KSDS                | (online only)                         | (JCL DUSRSECJ load)                  |
| STATEMNT.PS                      | -                                     | CBSTM03A (write)                     |
| STATEMNT.HTML                    | -                                     | CBSTM03A (write)                     |
| TRANREPT (GDG)                   | -                                     | CBTRN03C (write)                     |
| TRXFL.VSAM.KSDS (temp sorted)   | CBSTM03A                              | JCL CREASTMT SORT step               |
| TRANSACT.BKUP (GDG)             | -                                     | JCL TRANBKP (IDCAMS REPRO)          |

---

## JCL Job-to-Program Execution Map

| JCL Job    | Step(s)                | Program(s) Executed                    | Input Files                          | Output Files                         |
|------------|------------------------|----------------------------------------|--------------------------------------|--------------------------------------|
| ACCTFILE   | STEP05-STEP30          | IDCAMS (DELETE, DEFINE, REPRO)         | acctdata.txt (PS)                    | ACCTDATA.VSAM.KSDS                   |
| CARDFILE   | STEP05-STEP30          | IDCAMS                                 | carddata.txt (PS)                    | CARDDATA.VSAM.KSDS                   |
| CUSTFILE   | STEP05-STEP30          | IDCAMS                                 | custdata.txt (PS)                    | CUSTDATA.VSAM.KSDS                   |
| XREFFILE   | STEP05-STEP30          | IDCAMS                                 | cardxref.txt (PS)                    | CARDXREF.VSAM.KSDS                   |
| TRANFILE   | CLCIFIL-OPCIFIL        | SDSF, IDCAMS                           | dailytran.txt (PS)                   | TRANSACT.VSAM.KSDS, DALYTRAN.PS     |
| DUSRSECJ   | Multiple               | IDCAMS                                 | usrsec.txt (PS)                      | USRSEC.VSAM.KSDS                     |
| TRANTYPE   | STEP05-STEP15          | IDCAMS                                 | trantype.txt (PS)                    | TRANTYPE.VSAM.KSDS                   |
| TRANCATG   | STEP05-STEP15          | IDCAMS                                 | trancatg.txt (PS)                    | TRANCATG.VSAM.KSDS                   |
| DISCGRP    | STEP05-STEP15          | IDCAMS                                 | discgrp.txt (PS)                     | DISCGRP.VSAM.KSDS                    |
| TCATBALF   | STEP05-STEP15          | IDCAMS                                 | tcatbal.txt (PS)                     | TCATBAL.VSAM.KSDS                    |
| CLOSEFIL   | single step            | SDSF                                   | -                                    | (closes CICS files)                  |
| OPENFIL    | single step            | SDSF                                   | -                                    | (opens CICS files)                   |
| POSTTRAN   | STEP1, STEP2           | CBTRN01C, CBTRN02C                     | DALYTRAN.PS, TRANSACT, CARDXREF, ACCTDATA | TRANSACT (updated)            |
| INTCALC    | single step            | CBACT04C                               | TRANSACT, ACCTDATA, TCATBAL, DISCGRP, CARDXREF | ACCTDATA, TCATBAL, TRANSACT  |
| TRANBKP    | single step            | IDCAMS (REPRO)                         | TRANSACT.VSAM.KSDS                   | TRANSACT.BKUP(+1) (GDG)             |
| COMBTRAN   | single step            | SORT                                   | TRANSACT.BKUP, DALYTRAN              | TRANSACT.DALY(+1) (GDG)             |
| CREASTMT   | DELDEF-STEP040         | IDCAMS, SORT, IEFBR14, CBSTM03A       | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML       |
| TRANREPT   | STEP05R, STEP10R       | SORT, CBTRN03C                         | TRANSACT.BKUP, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT(+1) (GDG) |
| TRANIDX    | STEP20-STEP30          | IDCAMS                                 | TRANSACT.VSAM.KSDS                   | Alternate indexes                    |
| DALYREJS   | Multiple               | SORT, IDCAMS                           | DALYTRAN, TRANSACT                   | Reject report                        |
| CBEXPORT   | single step            | CBEXPORT                               | All VSAM master files                | Export flat file                     |
| CBIMPORT   | single step            | CBIMPORT                               | Export flat file                     | All VSAM master files                |
| TXT2PDF1   | TXT2PDF                | IKJEFT1B (TXT2PDF REXX)               | STATEMNT.PS                          | PDF output                           |
| WAITSTEP   | WAIT                   | COBSWAIT                               | -                                    | -                                    |

---

## Batch Job Sequencing (Data Flow)

The nightly batch cycle runs in this specific order. Files must be closed for CICS before batch runs, then reopened after.

```
 ┌──────────┐
 │ CLOSEFIL │  Close CICS files
 └────┬─────┘
      │
      ▼
 ┌──────────────────────────────────────────┐
 │ Data Refresh (parallel - load from PS)   │
 │ ACCTFILE, CARDFILE, CUSTFILE,            │
 │ XREFFILE, TRANFILE, DUSRSECJ,           │
 │ TRANTYPE, TRANCATG, DISCGRP, TCATBALF   │
 └────┬─────────────────────────────────────┘
      │
      ▼
 ┌──────────┐                    ┌──────────┐
 │ POSTTRAN │───────────────────>│ INTCALC  │
 │ CBTRN01C │ Post daily txns    │ CBACT04C │ Calculate interest
 │ CBTRN02C │                    └────┬─────┘
 └──────────┘                         │
                                      ▼
                                ┌──────────┐
                                │ TRANBKP  │  Backup transactions (GDG)
                                └────┬─────┘
                                     │
                          ┌──────────┴──────────┐
                          ▼                      ▼
                   ┌──────────┐           ┌──────────┐
                   │ COMBTRAN │           │ TRANREPT │
                   │  (SORT)  │           │ CBTRN03C │ Generate reports
                   └────┬─────┘           └──────────┘
                        │
                        ▼
                   ┌──────────┐
                   │ CREASTMT │
                   │ CBSTM03A │  Generate statements
                   └────┬─────┘
                        │
                        ▼
                   ┌──────────┐
                   │ TRANIDX  │  Rebuild alternate indexes
                   └────┬─────┘
                        │
                        ▼
                   ┌──────────┐
                   │ OPENFIL  │  Reopen CICS files
                   └──────────┘
```

### Data Flow Through Batch Cycle

```
External Feed                  VSAM Master Files              Output
─────────────                  ─────────────────              ──────

 dailytran.txt ──> DALYTRAN.PS
                       │
                       ▼
                   CBTRN01C ──> validates & enriches
                       │
                       ▼
                   CBTRN02C ──> TRANSACT.VSAM.KSDS (updated)
                                ACCTDATA.VSAM.KSDS (balances updated)
                                TCATBAL.VSAM.KSDS  (cat balances updated)
                       │
                       ▼
                   CBACT04C ──> ACCTDATA.VSAM.KSDS (interest added)
                                TCATBAL.VSAM.KSDS  (interest cat updated)
                                TRANSACT.VSAM.KSDS (interest txns added)
                       │
                       ├──────> TRANBKP ──> TRANSACT.BKUP(+1) [GDG]
                       │
                       ├──────> CBTRN03C ──> TRANREPT(+1) [report output]
                       │
                       └──────> CBSTM03A ──> STATEMNT.PS [text statements]
                                             STATEMNT.HTML [HTML statements]
                                               │
                                               ▼
                                           TXT2PDF1 ──> PDF statements
```

---

## End-to-End Data Lineage

### Transaction Lifecycle

```
1. ORIGIN      POS Terminal / Online (COTRN02C)
                    │
2. CAPTURE     Written to TRANSACT.VSAM.KSDS (or DALYTRAN.PS for batch)
                    │
3. POSTING     CBTRN01C reads DALYTRAN ──> validates ──> CBTRN02C updates TRANSACT
                    │
4. INTEREST    CBACT04C reads TRANSACT + DISCGRP ──> calculates ──> updates ACCTDATA + TCATBAL
                    │
5. REPORTING   CBTRN03C reads TRANSACT + TRANTYPE + TRANCATG ──> TRANREPT
                    │
6. STATEMENTS  CBSTM03A reads TRANSACT + XREF + ACCT + CUST ──> STATEMNT.PS/HTML
                    │
7. ARCHIVE     TRANBKP ──> TRANSACT.BKUP (GDG rolling)
```

### Customer Data Lineage

```
1. ENTRY       CUSTDATA.VSAM.KSDS (loaded via CUSTFILE JCL or CBIMPORT)
                    │
2. ONLINE VIEW COACTVWC, COCRDSLC (read via CARDXREF lookup)
                    │
3. ONLINE UPD  COACTUPC (update customer address, phone, etc.)
                    │
4. STATEMENTS  CBSTM03A (customer name/address for mailing)
                    │
5. EXPORT      CBEXPORT (extract to flat file for external systems)
```

### Account Balance Lineage

```
1. INITIAL     ACCTDATA.VSAM.KSDS (loaded via ACCTFILE JCL)
                    │
2. POSTING     CBTRN02C updates ACCT-CURR-CYC-CREDIT / ACCT-CURR-CYC-DEBIT
                    │
3. INTEREST    CBACT04C adds interest charges to ACCT-CURR-BAL
                    │
4. PAYMENT     COBIL00C (online bill payment updates balance)
                    │
5. VIEW        COACTVWC (display current balance)
                    │
6. STATEMENT   CBSTM03A (opening/closing balance on statement)
```

---

## Screen Navigation Flow

```
                    ┌─────────────────┐
                    │   COSGN00C      │
                    │   Sign-On       │
                    │   (Map:COSGN00) │
                    └────────┬────────┘
                             │
                   ┌─────────┴─────────┐
                   │                   │
            Admin (Type A)      Regular (Type U)
                   │                   │
                   ▼                   ▼
           ┌──────────────┐    ┌──────────────┐
           │  COADM01C    │    │  COMEN01C    │
           │  Admin Menu  │    │  Main Menu   │
           │  (COADM01)   │    │  (COMEN01)   │
           └──────┬───────┘    └──────┬───────┘
                  │                    │
    ┌─────────────┤         ┌──────────┼────────────┬──────────────┐
    │      │      │         │          │            │              │
    ▼      ▼      ▼         ▼          ▼            ▼              ▼
 COUSR00 COUSR01 COUSR02  COACTVW  COACTUPC     COCRDLIC      COTRN00C
 (List)  (Add)   (Update) (Acct    (Acct        (Card ──>     (Tran
    │              │      View)    Update)      COCRDSLC/      List)
    │              │                            COCRDUPC)        │
 COUSR03                                                    ┌────┴────┐
 (Delete)                                                   ▼         ▼
                                                         COTRN01C  COTRN02C
                                                         (View)    (Add)

           Additional screens:
           CORPT00C (Reports)  COBIL00C (Bill Pay)
           COPAUS0C/1C (Auth)  COTRTLIC/COTRTUPC (Tran Types)
```

All screens share a common layout:
- **Row 1-3:** Title bar (COTTL01Y), date (CSDAT01Y), program name
- **Body:** Screen-specific content (BMS map fields)
- **Last 2 rows:** Message area (CSMSG01Y) and function key guide
