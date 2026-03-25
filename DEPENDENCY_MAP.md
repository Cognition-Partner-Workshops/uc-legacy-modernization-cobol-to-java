# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Scope:** Program call graph, copybook dependencies, JCL data lineage, VSAM file access patterns

---

## Executive Summary

This document maps all inter-component dependencies in the CardDemo application:
- **Program-to-Program call graph** (CICS XCTL / LINK / CALL transfers)
- **Program-to-Copybook** inclusion dependencies
- **JCL Job-to-Program** execution mapping
- **Data lineage** showing which programs and jobs read/write which VSAM files

---

## 1. Program Call Graph (Online CICS)

### 1.1 Navigation Flow (XCTL Transfers)

The online application uses **EXEC CICS XCTL** for screen-to-screen transfers. All programs pass `CARDDEMO-COMMAREA` (defined in `COCOM01Y.cpy`) for state management.

```
                              ┌─────────────┐
                              │  COSGN00C   │  (Sign-on, Trans: CC00)
                              │  Entry Point │
                              └──────┬──────┘
                                     │
                          ┌──────────┴──────────┐
                          │ XCTL based on        │
                          │ SEC-USR-TYPE          │
                          ▼                      ▼
                   ┌─────────────┐        ┌─────────────┐
                   │  COMEN01C   │        │  COADM01C   │
                   │  User Menu  │        │  Admin Menu  │
                   │  (CM00)     │        │  (CA00)      │
                   └──────┬──────┘        └──────┬──────┘
                          │                      │
          ┌───────────────┼───────────────┐      │
          │               │               │      ├── COUSR00C (User List)
          │               │               │      │     └── COUSR02C (User Update)
          │               │               │      │     └── COUSR03C (User Delete)
          │               │               │      ├── COUSR01C (User Add)
          │               │               │      ├── COTRTLIC (Tran Type List) [DB2 opt.]
          │               │               │      └── COTRTUPC (Tran Type Maint) [DB2 opt.]
          │               │               │
          ▼               ▼               ▼
   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
   │  COACTVWC   │ │  COCRDLIC   │ │  COTRN00C   │
   │  Acct View  │ │  Card List  │ │  Tran List  │
   │  (CAVW)     │ │  (CCLI)     │ │  (CT00)     │
   └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
          │               │               │
          │         ┌─────┴─────┐    ┌────┴────┐
          │         ▼           ▼    ▼         ▼
          │   ┌──────────┐ ┌──────────┐ ┌──────────┐
          │   │ COCRDSLC │ │ COCRDUPC │ │ COTRN01C │
          │   │ Card View│ │ Card Upd │ │ Tran View│
          │   │ (CCDL)   │ │ (CCUP)   │ │ (CT01)   │
          │   └──────────┘ └──────────┘ └──────────┘
          │
          ▼
   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
   │  COACTUPC   │  │  COTRN02C   │  │  CORPT00C   │  │  COBIL00C   │
   │  Acct Update│  │  Tran Add   │  │  Tran Report│  │  Bill Pay   │
   │  (CAUP)     │  │  (CT02)     │  │  (CR00)     │  │  (CB00)     │
   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘

   ┌─────────────┐
   │  COPAUS0C   │  (Pending Auth Summary) [IMS/DB2/MQ optional]
   │  (PA00)     │
   └──────┬──────┘
          ├── COPAUS1C (Auth Detail)
          └── COPAUS2C (Fraud Marking)
```

### 1.2 Detailed Program-to-Program Transfer Matrix

| Source Program | Target Program(s)                          | Transfer Method | Condition                           |
|----------------|-------------------------------------------|-----------------|-------------------------------------|
| COSGN00C       | COADM01C                                   | XCTL (literal)  | User type = 'A' (Admin)            |
| COSGN00C       | COMEN01C                                   | XCTL (literal)  | User type = 'U' (Regular)          |
| COMEN01C       | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | XCTL (dynamic via COMEN02Y table) | Based on menu option selected |
| COMEN01C       | COSGN00C                                   | XCTL (dynamic)  | PF3 (Exit to sign-on)              |
| COADM01C       | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | XCTL (dynamic via COADM02Y table) | Based on admin menu option |
| COADM01C       | COSGN00C                                   | XCTL (dynamic)  | PF3 (Exit to sign-on)              |
| COACTVWC       | COMEN01C                                   | XCTL (dynamic)  | PF3 (Return to menu)               |
| COACTUPC       | COMEN01C                                   | XCTL (dynamic)  | PF3 (Return to menu)               |
| COCRDLIC       | COCRDSLC, COCRDUPC                         | XCTL (dynamic)  | Card selected for view/update       |
| COCRDLIC       | COMEN01C                                   | XCTL (dynamic)  | PF3 (Return to menu)               |
| COCRDSLC       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COCRDUPC       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COTRN00C       | COTRN01C                                   | XCTL (dynamic)  | Transaction selected for view       |
| COTRN00C       | COMEN01C                                   | XCTL (dynamic)  | PF3 (Return to menu)               |
| COTRN01C       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COTRN02C       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| CORPT00C       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COBIL00C       | COMEN01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COUSR00C       | COUSR02C, COUSR03C                         | XCTL (dynamic)  | User selected for update/delete     |
| COUSR00C       | COADM01C or caller                         | XCTL (dynamic)  | PF3 (Return to admin menu)          |
| COUSR01C       | COADM01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COUSR02C       | COADM01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |
| COUSR03C       | COADM01C or caller                         | XCTL (dynamic)  | PF3 (Return)                        |

### 1.3 Utility Program Dependencies

| Calling Program | Called Program | Call Method      | Purpose                    |
|-----------------|---------------|------------------|----------------------------|
| COACTUPC        | CSUTLDTC      | EXEC CICS LINK   | Date validation            |
| CBSTM03A        | CBSTM03B      | CALL (batch)     | Statement sub-processing   |

---

## 2. Copybook Dependency Matrix

### 2.1 Programs to Copybooks (Core)

Legend: **D** = Data record layout, **C** = Communication/infrastructure, **B** = BMS screen, **I** = IBM-supplied

| Program    | COCOM01Y | COTTL01Y | CSDAT01Y | CSMSG01Y | CSUSR01Y | CVACT01Y | CVACT02Y | CVACT03Y | CVTRA05Y | CVCUS01Y | BMS Copy | DFHAID | DFHBMSCA |
|------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:------:|:--------:|
| COSGN00C   | C        | C        | C        | C        | D        |          |          |          |          |          | COSGN00  | I      | I        |
| COMEN01C   | C        | C        | C        | C        | D        |          |          |          |          |          | COMEN01  | I      | I        |
| COADM01C   | C        | C        | C        | C        | D        |          |          |          |          |          | COADM01  | I      | I        |
| COACTVWC   | C        | C        | C        | C        | D        | D        | D        | D        |          | D        | COACTVW  | I      | I        |
| COACTUPC   | C        | C        | C        | C        | D        | D        |          | D        |          | D        | COACTUP  | I      | I        |
| COCRDLIC   | C        | C        | C        | C        | D        |          | D        |          |          |          | COCRDLI  | I      | I        |
| COCRDSLC   | C        | C        | C        | C        | D        |          | D        |          |          | D        | COCRDSL  | I      | I        |
| COCRDUPC   | C        | C        | C        | C        | D        |          | D        |          |          | D        | COCRDUP  | I      | I        |
| COTRN00C   | C        | C        | C        | C        |          |          |          |          | D        |          | COTRN00  | I      | I        |
| COTRN01C   | C        | C        | C        | C        |          |          |          |          | D        |          | COTRN01  | I      | I        |
| COTRN02C   | C        | C        | C        | C        |          | D        |          | D        | D        |          | COTRN02  | I      | I        |
| CORPT00C   | C        | C        | C        | C        |          |          |          |          | D        |          | CORPT00  | I      | I        |
| COBIL00C   | C        | C        | C        | C        |          | D        |          | D        | D        |          | COBIL00  | I      | I        |
| COUSR00C   | C        | C        | C        | C        | D        |          |          |          |          |          | COUSR00  | I      | I        |
| COUSR01C   | C        | C        | C        | C        | D        |          |          |          |          |          | COUSR01  | I      | I        |
| COUSR02C   | C        | C        | C        | C        | D        |          |          |          |          |          | COUSR02  | I      | I        |
| COUSR03C   | C        | C        | C        | C        | D        |          |          |          |          |          | COUSR03  | I      | I        |

### 2.2 Batch Programs to Copybooks

| Program    | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CVTRA06Y | CVTRA01Y | CVTRA02Y | Other              |
|------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------------------|
| CBACT01C   | D        |          |          |          |          |          |          |          | CODATECN           |
| CBACT02C   |          | D        |          |          |          |          |          |          |                    |
| CBACT03C   |          |          | D        |          |          |          |          |          |                    |
| CBACT04C   | D        |          | D        |          | D        |          | D        | D        | CVTRA02Y           |
| CBCUS01C   |          |          |          | D        |          |          |          |          |                    |
| CBTRN01C   | D        | D        | D        | D        | D        | D        |          |          |                    |
| CBTRN02C   | D        |          | D        |          | D        | D        | D        |          |                    |
| CBTRN03C   |          |          | D        |          | D        |          |          |          | CVTRA03Y, CVTRA04Y, CVTRA07Y |
| CBSTM03A   | D        |          | D        |          |          |          |          |          | COSTM01, CUSTREC   |
| CBEXPORT   | D        | D        | D        | D        | D        |          |          |          | CVEXPORT           |
| CBIMPORT   | D        | D        | D        | D        | D        |          |          |          | CVEXPORT           |

### 2.3 Additional Copybook Dependencies

| Copybook   | Additional Users                                        | Purpose                            |
|------------|--------------------------------------------------------|------------------------------------|
| CSMEN02Y   | COMEN01C (COMEN02Y)                                    | Main menu option definitions       |
| COADM02Y   | COADM01C                                               | Admin menu option definitions      |
| CSMSG02Y   | COACTVWC, COCRDSLC, COCRDUPC, COACTUPC                | Abend handling                     |
| CVCRD01Y   | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC      | Card work area / AID handling      |
| CSSTRPFY   | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC      | PF key mapping                     |
| CSSETATY   | COACTUPC (39 replacements)                              | BMS field attribute bytes          |
| CSLKPCDY   | COACTUPC                                               | Lookup code tables (1,318 lines)   |
| CSUTLDPY   | COACTUPC                                               | Date utility parameters            |
| CSUTLDWY   | COACTUPC                                               | Date edit working storage          |

---

## 3. VSAM File Access Map (Online CICS Programs)

### 3.1 File-to-Program Access Matrix

| VSAM File    | DD Name    | Key Field          | Programs that READ          | Programs that WRITE/UPDATE    |
|--------------|------------|--------------------|-----------------------------|-------------------------------|
| USRSEC       | USRSEC     | SEC-USR-ID         | COSGN00C                    | COUSR01C, COUSR02C, COUSR03C  |
| ACCTDAT      | ACCTDAT    | ACCT-ID            | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC, COBIL00C  |
| CARDDAT      | CARDDAT    | CARD-NUM           | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC             |
| CCXREF       | CCXREF     | XREF-CARD-NUM      | COACTVWC, COCRDLIC, COTRN02C, COBIL00C | —                    |
| CXACAIX      | CXACAIX    | XREF-ACCT-ID (AIX) | COACTVWC, COTRN02C          | —                              |
| CARDAIX      | CARDAIX    | CARD-ACCT-ID (AIX) | COACTVWC                     | —                              |
| CUSTDAT      | CUSTDAT    | CUST-ID            | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | —                   |
| TRANSACT     | TRANSACT   | TRAN-ID            | COTRN00C, COTRN01C, CORPT00C | COTRN02C, COBIL00C            |
| USRSEC       | USRSEC     | SEC-USR-ID         | COUSR00C                     | COUSR00C (delete), COUSR01C (add), COUSR02C (update) |

### 3.2 File-to-Program Access Matrix (Batch Programs)

| VSAM/Sequential File      | Programs that READ (INPUT) | Programs that WRITE (OUTPUT/I-O) |
|---------------------------|---------------------------|----------------------------------|
| DALYTRAN (Sequential)     | CBTRN02C                  | —                                |
| TRANSACT (VSAM KSDS)      | CBTRN03C, CBSTM03A, CBEXPORT | CBTRN02C                     |
| ACCTDAT (VSAM KSDS)       | CBACT01C, CBSTM03A, CBEXPORT | CBTRN02C (I-O), CBACT04C (I-O) |
| CARDDAT (VSAM KSDS)       | CBACT02C, CBEXPORT        | —                                |
| CCXREF (VSAM KSDS)        | CBACT03C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | — |
| CUSTDAT (VSAM KSDS)       | CBCUS01C, CBSTM03A, CBEXPORT | —                            |
| DALYREJS (Sequential)     | —                          | CBTRN02C                         |
| TCATBALF (VSAM KSDS)      | —                          | CBTRN02C (I-O), CBACT04C (I-O)  |
| DISCGRP (VSAM KSDS)       | CBACT04C                   | —                                |
| SYSTRAN (GDG)              | —                          | CBACT04C                         |
| TRANTYPE (VSAM KSDS)      | CBTRN03C                   | —                                |
| TRANCATG (VSAM KSDS)      | CBTRN03C                   | —                                |
| EXPORT.DATA (Sequential)   | CBIMPORT                   | CBEXPORT                         |

---

## 4. JCL Job-to-Program Execution Map

### 4.1 Jobs Executing Application Programs

| JCL Job    | Application Program | Input Files                                    | Output Files                                    |
|------------|---------------------|------------------------------------------------|-------------------------------------------------|
| POSTTRAN   | CBTRN02C            | DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM     | TRANSACT.VSAM, TCATBALF.VSAM, DALYREJS(+1)     |
| INTCALC    | CBACT04C            | ACCTDATA.VSAM, CARDXREF.VSAM, DISCGRP.VSAM    | ACCTDATA.VSAM (update), TCATBALF.VSAM, SYSTRAN(+1) |
| TRANREPT   | CBTRN03C + SORT     | TRANSACT.VSAM, CARDXREF.VSAM, TRANTYPE.VSAM, TRANCATG.VSAM | TRANREPT(+1), TRANSACT.DALY(+1) |
| CREASTMT   | CBSTM03A + SORT     | ACCTDATA.VSAM, CUSTDATA.VSAM, CARDXREF.VSAM, TRANSACT.VSAM | STATEMNT.PS, STATEMNT.HTML |
| READACCT   | CBACT01C            | ACCTDATA.VSAM                                  | SYSOUT (display)                                |
| READCARD   | CBACT02C            | CARDDATA.VSAM                                  | SYSOUT (display)                                |
| READCUST   | CBCUS01C            | CUSTDATA.VSAM                                  | SYSOUT (display)                                |
| READXREF   | CBACT03C            | CARDXREF.VSAM                                  | SYSOUT (display)                                |
| CBEXPORT   | CBEXPORT + IDCAMS   | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TRANSACT (all VSAM) | EXPORT.DATA (sequential) |
| CBIMPORT   | CBIMPORT            | EXPORT.DATA (sequential)                       | Import error file, reconstituted VSAM files      |
| WAITSTEP   | COBSWAIT            | —                                              | —                                                |

### 4.2 Jobs Using System Utilities Only

| JCL Job    | Utility Programs    | Function                                       | Files Affected                             |
|------------|---------------------|-------------------------------------------------|--------------------------------------------|
| ACCTFILE   | IDCAMS              | REPRO: Load Account VSAM from PS flat file      | ACCTDATA.PS -> ACCTDATA.VSAM.KSDS         |
| CARDFILE   | IDCAMS, SDSF        | REPRO: Load Card VSAM from PS flat file         | CARDDATA.PS -> CARDDATA.VSAM.KSDS         |
| CUSTFILE   | IDCAMS, SDSF        | REPRO: Load Customer VSAM from PS flat file     | CUSTDATA.PS -> CUSTDATA.VSAM.KSDS         |
| XREFFILE   | IDCAMS              | REPRO: Load Cross-Ref VSAM from PS flat file    | CARDXREF.PS -> CARDXREF.VSAM.KSDS         |
| TRANFILE   | IDCAMS, SDSF        | REPRO: Load Transaction VSAM + init daily       | DALYTRAN.PS.INIT -> TRANSACT.VSAM.KSDS    |
| DUSRSECJ   | IDCAMS, IEBGENER    | REPRO: Load User Security VSAM                  | USRSEC.PS -> USRSEC.VSAM.KSDS             |
| DISCGRP    | IDCAMS              | REPRO: Load Disclosure Group VSAM               | DISCGRP.PS -> DISCGRP.VSAM.KSDS           |
| TRANCATG   | IDCAMS              | REPRO: Load Transaction Category VSAM           | TRANCATG.PS -> TRANCATG.VSAM.KSDS         |
| TRANTYPE   | IDCAMS              | REPRO: Load Transaction Type VSAM               | TRANTYPE.PS -> TRANTYPE.VSAM.KSDS         |
| TCATBALF   | IDCAMS              | REPRO: Load Transaction Cat Balance VSAM        | TCATBALF.PS -> TCATBALF.VSAM.KSDS         |
| TRANBKP    | IDCAMS              | REPRO: Backup Transaction VSAM to GDG           | TRANSACT.VSAM.KSDS -> TRANSACT.BKUP(+1)   |
| COMBTRAN   | IDCAMS, SORT        | Merge: Combine backups with daily transactions  | SYSTRAN(0) + TRANSACT.BKUP(0) -> TRANSACT.COMBINED(+1) |
| CLOSEFIL   | SDSF                | Close CICS files for batch window               | Multiple VSAM files (close)                |
| OPENFIL    | SDSF                | Reopen CICS files after batch window            | Multiple VSAM files (open)                 |
| DEFGDGB    | IDCAMS              | Define GDG base entries                         | GDG catalog entries                        |
| DEFGDGD    | IDCAMS, IEBGENER    | Define GDG data + init backup datasets          | Multiple GDG datasets                      |
| TRANIDX    | IDCAMS              | Define alternate index on CARDXREF              | CARDXREF.VSAM.AIX                          |
| PRTCATBL   | SORT                | Print transaction category balance report       | TCATBALF.REPT                              |

---

## 5. Batch Processing Data Flow (End-to-End)

### 5.1 Nightly Batch Cycle Order

```
Phase 1: Preparation
  CLOSEFIL ──────────────> Close CICS files for exclusive batch access

Phase 2: Data Refresh (parallel-capable)
  ACCTFILE ──────────────> Refresh Account Master VSAM
  CARDFILE ──────────────> Refresh Card Master VSAM
  CUSTFILE ──────────────> Refresh Customer Master VSAM
  XREFFILE ──────────────> Refresh Card Cross-Reference VSAM
  DUSRSECJ ──────────────> Refresh User Security VSAM
  TRANFILE ──────────────> Load Transaction Master VSAM

Phase 3: Transaction Processing (sequential - ORDER MATTERS)
  POSTTRAN ──────────────> Post daily transactions
       │                     Input:  DALYTRAN.PS
       │                     Update: TRANSACT.VSAM, ACCTDATA.VSAM, TCATBALF.VSAM
       │                     Output: DALYREJS(+1)
       ▼
  INTCALC  ──────────────> Calculate interest charges
       │                     Input:  ACCTDATA.VSAM, CARDXREF.VSAM, DISCGRP.VSAM
       │                     Update: ACCTDATA.VSAM, TCATBALF.VSAM
       │                     Output: SYSTRAN(+1)
       ▼
  TRANBKP  ──────────────> Backup transaction master
       │                     Input:  TRANSACT.VSAM.KSDS
       │                     Output: TRANSACT.BKUP(+1)
       ▼
  COMBTRAN ──────────────> Combine transactions
       │                     Input:  SYSTRAN(0), TRANSACT.BKUP(0)
       │                     Output: TRANSACT.COMBINED(+1)
       ▼
  CREASTMT ──────────────> Generate statements
       │                     Input:  ACCTDATA, CUSTDATA, CARDXREF, TRANSACT
       │                     Output: STATEMNT.PS, STATEMNT.HTML
       ▼
  TRANREPT ──────────────> Generate transaction reports
       │                     Input:  TRANSACT, CARDXREF, TRANTYPE, TRANCATG
       │                     Output: TRANREPT(+1), TRANSACT.DALY(+1)
       ▼
  TRANIDX  ──────────────> Rebuild alternate indexes

Phase 4: Reopening
  OPENFIL  ──────────────> Reopen CICS files for online access
```

### 5.2 Complete Data Lineage Diagram

```
Source Data (PS flat files)
  ACCTDATA.PS ─────┐
  CARDDATA.PS ─────┤
  CUSTDATA.PS ─────┤  IDCAMS REPRO
  CARDXREF.PS ─────┤  ──────────>  VSAM KSDS Files
  USRSEC.PS ───────┤                │
  DALYTRAN.PS ─────┘                ▼

                          ┌──────────────────────┐
DALYTRAN.PS ──────────>   │      CBTRN02C         │
CARDXREF.VSAM ────────>   │   (Transaction        │──> TRANSACT.VSAM (write)
ACCTDATA.VSAM ────────>   │    Posting)            │──> ACCTDATA.VSAM (update bal)
                          │                        │──> TCATBALF.VSAM (update)
                          │                        │──> DALYREJS(+1) (rejects)
                          └──────────────────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
ACCTDATA.VSAM ────────>   │      CBACT04C         │
CARDXREF.VSAM ────────>   │   (Interest           │──> ACCTDATA.VSAM (update)
DISCGRP.VSAM ─────────>   │    Calculation)        │──> TCATBALF.VSAM (update)
                          │                        │──> SYSTRAN(+1) (interest txns)
                          └──────────────────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
TRANSACT.VSAM ────────>   │      IDCAMS            │
                          │   (Backup)             │──> TRANSACT.BKUP(+1)
                          └──────────────────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
ACCTDATA.VSAM ────────>   │      CBSTM03A         │
CUSTDATA.VSAM ────────>   │   (Statement           │──> STATEMNT.PS
CARDXREF.VSAM ────────>   │    Generation)          │──> STATEMNT.HTML
TRANSACT.VSAM ────────>   │                        │
                          └──────────────────────┘
                                     │
                                     ▼
                          ┌──────────────────────┐
TRANSACT.VSAM ────────>   │      CBTRN03C         │
CARDXREF.VSAM ────────>   │   (Transaction         │──> TRANREPT(+1)
TRANTYPE.VSAM ────────>   │    Reporting)           │──> TRANSACT.DALY(+1)
TRANCATG.VSAM ────────>   │                        │
                          └──────────────────────┘
```

---

## 6. Modernization Dependency Implications

### 6.1 Shared Data Coupling (High Risk for Migration)

| VSAM File    | Online Writers       | Batch Writers                | Conflict Risk |
|--------------|---------------------|------------------------------|---------------|
| ACCTDATA     | COACTUPC, COBIL00C  | CBTRN02C, CBACT04C           | **HIGH** — requires batch window coordination |
| TRANSACT     | COTRN02C, COBIL00C  | CBTRN02C                     | **HIGH** — online adds + batch posting |
| TCATBALF     | —                   | CBTRN02C, CBACT04C           | Medium — batch-only writers |
| CARDDAT      | COCRDUPC            | —                            | Low — single online writer |
| USRSEC       | COUSR01C-03C        | —                            | Low — admin-only access |

### 6.2 Program Coupling Groups (Natural Microservice Boundaries)

| Service Domain        | Programs                                          | Shared Data            |
|-----------------------|---------------------------------------------------|------------------------|
| Authentication        | COSGN00C                                          | USRSEC                 |
| User Management       | COUSR00C, COUSR01C, COUSR02C, COUSR03C            | USRSEC                 |
| Account Service       | COACTVWC, COACTUPC, CBACT01C, CBACT04C            | ACCTDAT, CUSTDAT       |
| Card Service          | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C  | CARDDAT, CCXREF        |
| Transaction Service   | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C  | TRANSACT, DALYTRAN     |
| Payment Service       | COBIL00C                                           | ACCTDAT, TRANSACT      |
| Reporting Service     | CORPT00C, CBTRN03C, CBSTM03A/B                    | TRANSACT (read-only)   |
| Data Migration        | CBEXPORT, CBIMPORT                                 | All VSAM files         |
