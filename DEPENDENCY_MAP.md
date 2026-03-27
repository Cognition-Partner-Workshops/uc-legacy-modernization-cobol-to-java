# Dependency Map -- CardDemo COBOL Codebase

> **Generated:** 2026-03-27 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This document maps every inter-program call, CICS transfer, copybook inclusion,
> VSAM file access, and JCL job data lineage in the CardDemo application.

---

## 1. Program Call Graph

### 1.1 Online CICS Navigation Flow

The CICS online system uses `EXEC CICS XCTL` (transfer control) to navigate
between screens and `EXEC CICS RETURN TRANSID` to set the next transaction.

```
                           ┌─────────────┐
                           │  COSGN00C   │  (CC00 - Sign-on)
                           │  Login      │
                           └──────┬──────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │ Admin (type='A')           │ Regular User (type='U')
                    ▼                            ▼
            ┌───────────────┐           ┌───────────────┐
            │  COADM01C     │           │  COMEN01C     │
            │  Admin Menu   │           │  Main Menu    │
            │  (CA00)       │           │  (CM00)       │
            └───────┬───────┘           └───────┬───────┘
                    │                           │
     ┌──────────────┼──────────────┐            │  (XCTL to CDEMO-MENU-OPT-PGMNAME)
     │              │              │            │
     ▼              ▼              ▼            ├──► COACTVWC  (Opt 1 - Account View)
  COUSR00C      COUSR01C      COUSR02C         ├──► COACTUPC  (Opt 2 - Account Update)
  User List     User Add      User Update      ├──► COCRDLIC  (Opt 3 - Card List)
  (CU00)        (CU01)        (CU02)           ├──► COCRDSLC  (Opt 4 - Card View)
     │              │              │            ├──► COCRDUPC  (Opt 5 - Card Update)
     ▼              ▼              ▼            ├──► COTRN00C  (Opt 6 - Transaction List)
  COUSR03C      COTRTLIC*     COTRTUPC*        ├──► COTRN01C  (Opt 7 - Transaction View)
  User Delete   Tran Type     Tran Type        ├──► COTRN02C  (Opt 8 - Transaction Add)
  (CU03)        List (DB2)    Maint (DB2)      ├──► CORPT00C  (Opt 9 - Reports)
                                               ├──► COBIL00C  (Opt 10 - Bill Payment)
  * = Optional DB2 module                      └──► COPAUS0C* (Opt 11 - Pending Auth)
```

### 1.2 CICS XCTL (Transfer Control) Edges

| Source Program | Target Program | Trigger / Condition |
|---|---|---|
| COSGN00C | **COADM01C** | User type = 'A' (admin) |
| COSGN00C | **COMEN01C** | User type = 'U' (regular) |
| COMEN01C | **(variable)** | XCTL to `CDEMO-MENU-OPT-PGMNAME(WS-OPTION)` -- dynamic dispatch from menu table in COMEN02Y |
| COADM01C | **(variable)** | XCTL to `CDEMO-ADMIN-OPT-PGMNAME` -- dynamic dispatch from admin menu table in COADM02Y |
| COCRDLIC | **COMEN01C** | PF3 (Exit) -- XCTL to `LIT-MENUPGM` |
| COCRDLIC | **COCRDSLC** | Select card -- XCTL to `CCARD-NEXT-PROG` = COCRDSLC |
| COCRDLIC | **COCRDUPC** | Update card -- XCTL to `CCARD-NEXT-PROG` = COCRDUPC |
| COCRDSLC | **COCRDLIC** | PF3 (Back) -- XCTL to `CDEMO-TO-PROGRAM` |
| COCRDUPC | **COCRDLIC** | PF3 (Back) -- XCTL to `CDEMO-TO-PROGRAM` |
| COACTVWC | **COMEN01C** | PF3 (Exit) -- XCTL to `CDEMO-TO-PROGRAM` |
| COACTUPC | **(variable)** | PF3/navigation -- XCTL to `CDEMO-TO-PROGRAM` |
| COTRTLIC* | **COMEN01C** | PF3 -- XCTL to menu |
| COTRTLIC* | **COTRTUPC** | Select type -- XCTL |
| COTRTUPC* | **COTRTLIC** | PF3 -- XCTL back to list |
| COPAUS1C* | **COPAUS0C** | PF3 -- back to summary |

### 1.3 CALL Graph (Subroutine Calls)

| Caller | Callee | Parameters | Purpose |
|---|---|---|---|
| COTRN02C | **CSUTLDTC** | CSUTLDTC-DATE | Validate transaction date (start/end) |
| CORPT00C | **CSUTLDTC** | CSUTLDTC-DATE | Validate report date range (start/end) |
| CSUTLDTC | **CEEDAYS** | (LE service) | Convert date to Lilian format for validation |
| CBSTM03A | **CBSTM03B** | WS-M03B-AREA | Write formatted statement output lines (called 11+ times) |
| CBACT01C | **COBDATFT** (ASM) | CODATECN-REC | Format dates from account records |
| COBSWAIT | **MVSWAIT** (ASM) | MVSWAIT-TIME | Pause execution for specified duration |
| CBACT01C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination (error handler) |
| CBACT02C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination |
| CBACT03C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination |
| CBACT04C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination |
| CBCUS01C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination |
| CBTRN02C | **CEE3ABD** | ABCODE, TIMING | Abnormal termination |
| CBEXPORT | **CEE3ABD** | -- | Abnormal termination |
| CBIMPORT | **CEE3ABD** | -- | Abnormal termination |
| CBSTM03A | **CEE3ABD** | -- | Abnormal termination |
| COPAUA0C* | **MQOPEN** | HCONN, OD, OPTS | Open MQ request/reply queues |
| COPAUA0C* | **MQGET** | HCONN, HOBJ, MD | Get message from request queue |
| COPAUA0C* | **MQPUT** | HCONN, HOBJ, MD | Put response to reply queue |
| COACCT01* | **MQOPEN/MQGET/MQPUT** | (MQ handles) | Account inquiry via MQ |
| CODATE01* | **MQOPEN/MQGET/MQPUT** | (MQ handles) | Date service via MQ |
| DBUNLDGS* | **CBLTDLI** | FUNC-GN, FUNC-GNP, FUNC-ISRT | IMS DL/I calls (unload segments) |
| PAUDBLOD* | **CBLTDLI** | FUNC-ISRT, FUNC-GU | IMS DL/I calls (load segments) |
| PAUDBUNL* | **CBLTDLI** | FUNC-GN, FUNC-GNP | IMS DL/I calls (unload segments) |
| COPAUS1C* | **COPAUS2C** | (COMMAREA) | EXEC CICS LINK -- invoke DB2 fraud marking |

### 1.4 CICS LINK Calls

| Caller | Callee | Purpose |
|---|---|---|
| COPAUS1C | **COPAUS2C** | Link to DB2 fraud-marking sub-program |

---

## 2. Copybook Inclusion Map

This table shows which programs include which copybooks (via `COPY` statements).

### 2.1 Core Data Copybooks

| Copybook | Type | Included By |
|---|---|---|
| **CVACT01Y** | Account Record | COACTUPC, COACTVWC, COTRN02C, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN02C, COPAUA0C*, COPAUS0C*, COACCT01* |
| **CVACT02Y** | Card Record | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, CBEXPORT, CBIMPORT, COPAUS0C*, COTRTLIC* |
| **CVACT03Y** | Cross-Reference | COACTUPC, COACTVWC, COTRN02C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN02C, COPAUA0C* |
| **CVCUS01Y** | Customer Record | COCRDSLC, COCRDUPC, COACTVWC, CBEXPORT, CBIMPORT, CBCUS01C, COPAUA0C*, COPAUS0C* |
| **CVTRA05Y** | Transaction | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN02C |
| **CVTRA06Y** | Daily Transaction | CBTRN02C |
| **CVTRA01Y** | Category Balance | CBACT04C, CBTRN02C |
| **CVTRA02Y** | Discount Group | CBACT04C |
| **CVTRA03Y** | Transaction Type | *(referenced via file reads)* |
| **CVTRA04Y** | Trans Category | *(referenced via file reads)* |
| **CVTRA07Y** | Report Layout | CBTRN03C |
| **CSUSR01Y** | User Security | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C, COMEN01C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COPAUS0C*, COPAUS1C*, COTRTLIC*, COTRTUPC* |
| **CUSTREC** | Customer (alt) | CBSTM03A |
| **COSTM01** | Statement Work | CBSTM03A |
| **CVEXPORT** | Export Layout | CBEXPORT, CBIMPORT |
| **CODATECN** | Date Conversion | CBACT01C |

### 2.2 UI/Control Copybooks

| Copybook | Type | Included By |
|---|---|---|
| **COCOM01Y** | CICS Commarea | All online CO* programs (17+) |
| **COMEN02Y** | Menu Options | COMEN01C |
| **COADM02Y** | Admin Options | COADM01C |
| **COTTL01Y** | Title/Header | All online CO* programs |
| **CSDAT01Y** | Date Display | All online CO* programs |
| **CSMSG01Y** | Status Messages | All online CO* programs |
| **CSMSG02Y** | Abend Work Areas | COCRDSLC, COCRDUPC, COACTVWC, COPAUS0C*, COPAUS1C*, COTRTUPC* |
| **CVCRD01Y** | Card Work Areas | COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COTRTLIC*, COTRTUPC* |
| **CSLKPCDY** | Lookup Codes | COACTUPC |
| **CSSETATY** | Set Attributes | COACTUPC |
| **CSSTRPFY** | String Strip | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSUTLDPY** | Date Util Params | *(included via CSUTLDTC)* |
| **CSUTLDWY** | Date Util Work | COACTUPC, COTRTUPC* |
| **DFHAID** | CICS AID Keys | All online programs |
| **DFHBMSCA** | BMS Attributes | All online programs |

### 2.3 BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map generates a copybook included by its associated program:

| BMS Copybook | Included By |
|---|---|
| COSGN00 | COSGN00C |
| COMEN01 | COMEN01C |
| COADM01 | COADM01C |
| COACTVW | COACTVWC |
| COACTUP | COACTUPC |
| COCRDLI | COCRDLIC |
| COCRDSL | COCRDSLC |
| COCRDUP | COCRDUPC |
| COTRN00 | COTRN00C |
| COTRN01 | COTRN01C |
| COTRN02 | COTRN02C |
| CORPT00 | CORPT00C |
| COBIL00 | COBIL00C |
| COUSR00 | COUSR00C |
| COUSR01 | COUSR01C |
| COUSR02 | COUSR02C |
| COUSR03 | COUSR03C |

### 2.4 Optional Module Copybooks

| Copybook | Module | Included By |
|---|---|---|
| CIPAUSMY | Auth IMS/DB2/MQ | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| CIPAUDTY | Auth IMS/DB2/MQ | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| CCPAURQY | Auth IMS/DB2/MQ | COPAUA0C |
| CCPAURLY | Auth IMS/DB2/MQ | COPAUA0C |
| CCPAUERY | Auth IMS/DB2/MQ | COPAUA0C |
| IMSFUNCS | Auth IMS/DB2/MQ | DBUNLDGS, PAUDBLOD, PAUDBUNL |
| PAUTBPCB | Auth IMS/DB2/MQ | DBUNLDGS, PAUDBLOD, PAUDBUNL |
| PASFLPCB | Auth IMS/DB2/MQ | DBUNLDGS |
| PADFLPCB | Auth IMS/DB2/MQ | DBUNLDGS |
| CSDB2RWY | Tran Type DB2 | COTRTLIC |
| CSDB2RPY | Tran Type DB2 | *(COTRTLIC indirectly)* |
| CMQxxxx | MQ headers | COPAUA0C, COACCT01, CODATE01 |

---

## 3. VSAM File Access Map

### 3.1 Programs That Read/Write Each VSAM File

| VSAM File | Dataset Name | Read By | Written By | Updated By |
|---|---|---|---|---|
| **ACCTDAT** | CARDDEMO.ACCTDATA | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBSTM03A, CBEXPORT, COPAUA0C*, COACCT01* | CBIMPORT | COACTUPC (REWRITE), COBIL00C (REWRITE), CBACT04C (REWRITE), CBTRN02C (REWRITE) |
| **CARDDAT** | CARDDEMO.CARDDATA | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT | CBIMPORT | COCRDUPC (REWRITE) |
| **CUSTDAT** | CARDDEMO.CUSTDATA | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT, COPAUA0C* | CBIMPORT | -- |
| **CARDXREF** | CARDDEMO.CARDXREF | COACTVWC, COACTUPC, COTRN02C, CBACT03C, CBACT04C, CBSTM03A, CBTRN02C, CBTRN03C, CBEXPORT, COPAUA0C* | CBIMPORT | -- |
| **TRANSACT** | CARDDEMO.TRANSACT | COTRN00C, COTRN01C, COBIL00C, CBTRN01C, CBACT04C, CBSTM03A, CBEXPORT | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE), CBIMPORT | -- |
| **USRSEC** | CARDDEMO.USRSEC | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE) | COUSR02C (REWRITE), COUSR03C (DELETE) |
| **DALYTRAN** | CARDDEMO.DALYTRAN.PS | CBTRN02C | *(loaded by JCL)* | -- |
| **TRANTYPE** | CARDDEMO.TRANTYPE | CBTRN03C | *(loaded by JCL)* | -- |
| **TRANCATG** | CARDDEMO.TRANCATG | CBTRN03C | *(loaded by JCL)* | -- |
| **TCATBALF** | CARDDEMO.TCATBALF | CBACT04C | CBACT04C (WRITE/REWRITE) | -- |
| **DISCGRP** | CARDDEMO.DISCGRP | CBACT04C | *(loaded by JCL)* | -- |
| **REPORTFL** | CARDDEMO.REPORTFL | -- | CBTRN03C (WRITE) | -- |

### 3.2 Online Program to VSAM Access Matrix

| Program | ACCTDAT | CARDDAT | CUSTDAT | CARDXREF | TRANSACT | USRSEC |
|---|---|---|---|---|---|---|
| COSGN00C | | | | | | R |
| COACTVWC | R | | R | R | | |
| COACTUPC | R/W | | | R | | |
| COCRDLIC | | R | | | | |
| COCRDSLC | | R | R | | | |
| COCRDUPC | | R/W | R | | | |
| COTRN00C | | | | | R | |
| COTRN01C | | | | | R | |
| COTRN02C | R | | | R | W | |
| CORPT00C | | | | | | |
| COBIL00C | R/W | | | | R/W | |
| COUSR00C | | | | | | R |
| COUSR01C | | | | | | W |
| COUSR02C | | | | | | R/W |
| COUSR03C | | | | | | R/D |

> R = Read, W = Write (new), R/W = Read + Rewrite (update), R/D = Read + Delete

---

## 4. JCL Job Data Lineage

### 4.1 Batch Cycle Flow

The standard nightly batch cycle processes data in this order:

```
CLOSEFIL ──► Data Refresh ──► POSTTRAN ──► INTCALC ──► TRANBKP ──► COMBTRAN ──► CREASTMT ──► TRANIDX ──► OPENFIL
   │              │               │            │           │            │             │            │          │
   │         Load VSAM files     │        Calculate    Backup to    Merge daily   Generate    Rebuild    Reopen
   │         from sequential     │        interest     GDG          into master   statements  alt index  CICS
   │         PS sources          │        rates                                                          files
   ▼              ▼              ▼            ▼           ▼            ▼             ▼            ▼          ▼
 SDSF         IDCAMS         CBTRN02C     CBACT04C    REPROC      SORT+IDCAMS   CBSTM03A     IDCAMS      SDSF
                                                      +IDCAMS                   +CBSTM03B
```

### 4.2 Job-Level Data Lineage

| Job | Step | Program | Input Files (Read) | Output Files (Written) |
|---|---|---|---|---|
| **CLOSEFIL** | -- | SDSF | -- | Closes CICS VSAM files |
| **ACCTFILE** | STEP05-15 | IDCAMS | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | STEP05-15 | IDCAMS | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | STEP05-15 | IDCAMS | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | STEP05-30 | IDCAMS | CARDXREF.PS | CARDXREF.VSAM.KSDS + alt indexes |
| **TRANFILE** | STEP05-20 | IDCAMS | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | STEP05-15 | IDCAMS | USRSEC.PS | USRSEC.VSAM.KSDS |
| **TRANTYPE** | STEP05-15 | IDCAMS | TRANTYPE.PS | TRANTYPE.VSAM.KSDS |
| **TRANCATG** | STEP05-15 | IDCAMS | TRANCATG.PS | TRANCATG.VSAM.KSDS |
| **TCATBALF** | STEP05-15 | IDCAMS | TCATBALF.PS | TCATBALF.VSAM.KSDS |
| **POSTTRAN** | STEP05 | CBTRN02C | DALYTRAN (input), CARDXREF, TRANSACT, ACCTDAT | TRANSACT (write), ACCTDAT (rewrite), DALYREJS (rejects) |
| **INTCALC** | STEP05 | CBACT04C | TRANSACT, CARDXREF, ACCTDAT, DISCGRP, TCATBALF | ACCTDAT (rewrite), TCATBALF (rewrite) |
| **TRANBKP** | PRC001 | REPROC | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) GDG |
| **TRANBKP** | STEP05-10 | IDCAMS | -- | Delete/redefine TRANSACT.VSAM.KSDS |
| **COMBTRAN** | STEP05 | SORT | TRANSACT.VSAM.KSDS, DALYTRAN | Merged TRANSACT |
| **CREASTMT** | STEP010 | SORT | TRANSACT.VSAM.KSDS | TRXFL.SEQ (sorted) |
| **CREASTMT** | STEP020 | IDCAMS | TRXFL.SEQ | TRXFL.VSAM.KSDS |
| **CREASTMT** | STEP040 | CBSTM03A | TRXFL.VSAM, CARDXREF, CUSTDAT, ACCTDAT | STATEMNT.HTML, STATEMNT.PS |
| **TRANREPT** | PRC001 | REPROC | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) |
| **TRANREPT** | STEP05R | SORT | TRANSACT.BKUP(+1) | TRANSACT.DALY(+1) (sorted by date) |
| **TRANREPT** | STEP10R | CBTRN03C | TRANSACT.DALY(+1), CARDXREF, TRANTYPE, TRANCATG | REPORTFL |
| **TRANIDX** | STEP20-30 | IDCAMS | -- | Alt indexes on TRANSACT.VSAM |
| **OPENFIL** | -- | SDSF | -- | Reopens CICS VSAM files |

### 4.3 Data Read/Validation Jobs

| Job | Program | Input File | Purpose |
|---|---|---|---|
| READACCT | CBACT01C | ACCTDATA.VSAM.KSDS | Validate account file contents |
| READCARD | CBACT02C | CARDDATA.VSAM.KSDS | Validate card file contents |
| READXREF | CBACT03C | CARDXREF.VSAM.KSDS | Validate cross-reference contents |
| READCUST | CBCUS01C | CUSTDATA.VSAM.KSDS | Validate customer file contents |

### 4.4 Export/Import Data Flow

```
┌──────────────────────────────────────────────────────────┐
│  CBEXPORT Job                                            │
│                                                          │
│  CUSTDAT ──┐                                             │
│  ACCTDAT ──┤                                             │
│  CARDXREF ─┼──► CBEXPORT program ──► EXPORT.SEQ.FILE     │
│  TRANSACT ─┤    (reads all VSAM,     (sequential PS      │
│  CARDDAT ──┘     writes envelope      with record type    │
│                  records)             C/A/X/T/D)          │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  CBIMPORT Job                                            │
│                                                          │
│  EXPORT.SEQ.FILE ──► CBIMPORT program ──► CUSTDAT        │
│  (sequential PS       (reads envelope,    ACCTDAT        │
│   with record type     dispatches by      CARDXREF       │
│   C/A/X/T/D)          type code)         TRANSACT       │
│                                           CARDDAT        │
└──────────────────────────────────────────────────────────┘
```

---

## 5. Data Flow by Business Domain

### 5.1 Account Domain

```
                    ┌─────────────┐
  ACCTFILE (JCL) ──►│   ACCTDAT   │◄── CBIMPORT
                    │   (VSAM)    │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────────┐
         │                 │                     │
    ┌────▼────┐      ┌────▼────┐          ┌─────▼─────┐
    │COACTVWC │      │COACTUPC │          │ CBACT04C  │
    │(View)   │      │(Update) │          │(Interest) │
    └─────────┘      └─────────┘          └───────────┘
                           │                     │
                     Updates VSAM          Updates balances
                     (REWRITE)             (REWRITE)
```

### 5.2 Transaction Domain

```
                DALYTRAN.PS
                    │
                    ▼
              ┌───────────┐        ┌──────────┐
              │ CBTRN02C  │───────►│TRANSACT  │◄──── COTRN02C (online add)
              │ (Posting) │        │ (VSAM)   │◄──── COBIL00C (payment)
              └───────────┘        └────┬─────┘
                    │                   │
              Updates ACCTDAT    ┌──────┼──────┐
              (balance adj)      │      │      │
                           ┌────▼┐  ┌──▼──┐ ┌─▼──────┐
                           │TR00C│  │TR01C│ │CBTRN03C│
                           │List │  │View │ │Report  │
                           └─────┘  └─────┘ └────────┘
                                                │
                                          ┌─────▼──────┐
                                          │  REPORTFL  │
                                          │ (output)   │
                                          └────────────┘
```

### 5.3 Statement Generation Domain

```
  TRANSACT (VSAM) ──► SORT ──► TRXFL.VSAM ──► CBSTM03A ──► STATEMNT.HTML
  CARDXREF (VSAM) ─────────────────────────────┤              STATEMNT.PS
  CUSTDAT  (VSAM) ─────────────────────────────┤                │
  ACCTDAT  (VSAM) ─────────────────────────────┘                ▼
                                                           TXT2PDF1 (JCL)
                                                                │
                                                           PDF statements
```

---

## 6. Cross-Cutting Dependency Summary

### 6.1 Most-Depended-On Artifacts

| Artifact | Depended On By (count) | Type |
|---|---|---|
| **COCOM01Y** (Commarea) | 17+ online programs | Copybook |
| **CVACT01Y** (Account) | 12 programs | Copybook |
| **CVACT03Y** (Cross-Ref) | 10 programs | Copybook |
| **CSUSR01Y** (User Security) | 15+ programs | Copybook |
| **COTTL01Y** (Title) | 17+ programs | Copybook |
| **CSDAT01Y** (Date Display) | 17+ programs | Copybook |
| **CSMSG01Y** (Messages) | 17+ programs | Copybook |
| **CARDXREF** (VSAM file) | 10 programs | VSAM File |
| **ACCTDAT** (VSAM file) | 10+ programs | VSAM File |
| **TRANSACT** (VSAM file) | 9 programs | VSAM File |

### 6.2 Programs With No External Dependents

These programs are leaf nodes (only called, never call others):

- CBSTM03B (only called by CBSTM03A)
- COBSWAIT (only called by WAITSTEP JCL)
- CSUTLDTC (called by COTRN02C, CORPT00C only)
- COPAUS2C (linked by COPAUS1C only)
- All `CB*` file-read utilities (CBACT01C-03C, CBCUS01C, CBTRN01C)

### 6.3 Shared COMMAREA Navigation State

All online programs share the `CARDDEMO-COMMAREA` (COCOM01Y) which carries:
- Current program name and transaction ID
- Target program for navigation (CDEMO-TO-PROGRAM)
- User session data (user ID, type, name)
- Screen-specific flags and data

This is the primary inter-program communication mechanism and must be modeled
as session state or a navigation context object in the modernized application.
