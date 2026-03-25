# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: CardDemo - Mainframe Credit Card Management System
> **Scope**: Program-to-program call graph, CICS navigation flow, copybook usage, and batch data lineage

---

## Table of Contents

- [1. CICS Online Navigation Flow](#1-cics-online-navigation-flow)
  - [1.1 User Flow](#11-user-flow)
  - [1.2 Admin Flow](#12-admin-flow)
- [2. Program Call Graph](#2-program-call-graph)
  - [2.1 CICS XCTL (Transfer Control) Calls](#21-cics-xctl-transfer-control-calls)
  - [2.2 COBOL CALL (Subroutine) Calls](#22-cobol-call-subroutine-calls)
  - [2.3 External System Calls](#23-external-system-calls)
- [3. Copybook Usage Matrix](#3-copybook-usage-matrix)
  - [3.1 Data Record Copybooks](#31-data-record-copybooks)
  - [3.2 System/Common Copybooks](#32-systemcommon-copybooks)
  - [3.3 BMS Copybooks](#33-bms-copybooks)
- [4. VSAM File Access Map](#4-vsam-file-access-map)
  - [4.1 Online (CICS) File Access](#41-online-cics-file-access)
  - [4.2 Batch File Access](#42-batch-file-access)
- [5. Batch Job Data Lineage](#5-batch-job-data-lineage)
  - [5.1 Daily Batch Processing Flow](#51-daily-batch-processing-flow)
  - [5.2 Job-to-Dataset Matrix](#52-job-to-dataset-matrix)
  - [5.3 Environment Setup Flow](#53-environment-setup-flow)
- [6. Optional Module Dependencies](#6-optional-module-dependencies)
- [7. Full Adjacency List](#7-full-adjacency-list)

---

## 1. CICS Online Navigation Flow

### 1.1 User Flow

```
CC00 (COSGN00C) ── Signon
  │
  ├─ [Admin User] ──> CA00 (COADM01C) ── Admin Menu
  │                     ├──> CU00 (COUSR00C) ── List Users
  │                     │      ├──> CU01 (COUSR01C) ── Add User
  │                     │      ├──> CU02 (COUSR02C) ── Update User
  │                     │      └──> CU03 (COUSR03C) ── Delete User
  │                     ├──> CTLI (COTRTLIC) ── Tran Type List *[DB2]
  │                     └──> CTTU (COTRTUPC) ── Tran Type Edit *[DB2]
  │
  └─ [Regular User] ──> CM00 (COMEN01C) ── Main Menu
                          ├──> CAVW (COACTVWC) ── Account View
                          │      └──> CAUP (COACTUPC) ── Account Update
                          ├──> CCLI (COCRDLIC) ── Card List
                          │      ├──> CCDL (COCRDSLC) ── Card Detail
                          │      └──> CCUP (COCRDUPC) ── Card Update
                          ├──> CT00 (COTRN00C) ── Transaction List
                          │      ├──> CT01 (COTRN01C) ── Transaction View
                          │      └──> CT02 (COTRN02C) ── Transaction Add
                          ├──> CR00 (CORPT00C) ── Transaction Report
                          ├──> CB00 (COBIL00C) ── Bill Payment
                          ├──> CPVS (COPAUS0C) ── Pending Auth Summary *[IMS]
                          │      └──> CPVD (COPAUS1C) ── Pending Auth Detail *[IMS]
                          ├──> CDRD (CODATE01) ── System Date via MQ *[MQ]
                          └──> CDRA (COACCT01) ── Account via MQ *[MQ]
```

*Items marked with `*[DB2]`, `*[IMS]`, `*[MQ]` are optional modules.*

### 1.2 Admin Flow

```
COSGN00C ─── validates user against USRSEC ───> routes by SEC-USR-TYPE
  │
  ├─ Type 'A' ──> XCTL to COADM01C (Admin Menu)
  │                 Uses COADM02Y to map menu options to program names
  │                 Options: COUSR00C, COTRTLIC, COTRTUPC
  │
  └─ Type 'U' ──> XCTL to COMEN01C (Main Menu)
                    Uses COMEN02Y to map menu options to program names
                    Options 1-11 mapped to respective programs
```

**Navigation Mechanism**: All screen-to-screen navigation uses `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` where the target program name is stored in the COMMAREA (`COCOM01Y`). Each program sets `CDEMO-TO-PROGRAM` before issuing XCTL.

---

## 2. Program Call Graph

### 2.1 CICS XCTL (Transfer Control) Calls

| Source Program | Target Program | Trigger | Direction |
|---------------|---------------|---------|-----------|
| COSGN00C | COMEN01C | Successful user login | Forward |
| COSGN00C | COADM01C | Successful admin login | Forward |
| COMEN01C | *menu-option-pgm* | User selects menu option (via COMEN02Y lookup) | Forward |
| COMEN01C | COSGN00C | PF3 (Return to signon) | Backward |
| COADM01C | *admin-option-pgm* | Admin selects option (via COADM02Y lookup) | Forward |
| COADM01C | COSGN00C | PF3 (Return to signon) | Backward |
| COACTVWC | COACTUPC | User requests account update | Forward |
| COACTVWC | COMEN01C | PF3 (Back to menu) | Backward |
| COACTUPC | COMEN01C | PF3 (Back to menu) | Backward |
| COCRDLIC | COCRDSLC | User selects card for detail view | Forward |
| COCRDLIC | COCRDUPC | User selects card for update | Forward |
| COCRDLIC | COMEN01C | PF3 (Back to menu) | Backward |
| COCRDSLC | COCRDLIC | PF3 (Back to list) | Backward |
| COCRDUPC | COCRDLIC | PF3 (Back to list) | Backward |
| COTRN00C | COTRN01C | User selects transaction to view | Forward |
| COTRN00C | COTRN02C | User requests add transaction | Forward |
| COTRN00C | COMEN01C | PF3 (Back to menu) | Backward |
| COTRN01C | COMEN01C | PF3 (Back to menu) | Backward |
| COTRN02C | COMEN01C | PF3 (Back to menu) | Backward |
| CORPT00C | COMEN01C | PF3 (Back to menu) | Backward |
| COBIL00C | COMEN01C | PF3 (Back to menu) | Backward |
| COUSR00C | COUSR01C | Admin adds new user | Forward |
| COUSR00C | COUSR02C | Admin selects user to update | Forward |
| COUSR00C | COUSR03C | Admin selects user to delete | Forward |
| COUSR00C | COADM01C | PF3 (Back to admin menu) | Backward |
| COUSR01C | COADM01C | PF3 (Back to admin menu) | Backward |
| COUSR02C | COADM01C | PF3 (Back to admin menu) | Backward |
| COUSR03C | COADM01C | PF3 (Back to admin menu) | Backward |

### 2.2 COBOL CALL (Subroutine) Calls

| Caller | Callee | Purpose | Interface |
|--------|--------|---------|-----------|
| CBSTM03A | CBSTM03B | File I/O operations for statement generation | WS-M03B-AREA (open/close/read files) |
| COTRN02C | CSUTLDTC | Validate transaction dates | CSUTLDTC-DATE parameter |
| CORPT00C | CSUTLDTC | Validate report date range | CSUTLDTC-DATE parameter |
| CSUTLDTC | CEEDAYS | LE date conversion API (external) | Vstring date, format, Lilian |
| CBACT01C | COBDATFT | Date format conversion (ASM) | CODATECN-REC |
| COBSWAIT | MVSWAIT | Timer wait (ASM) | MVSWAIT-TIME |
| CBACT01C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBACT02C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBACT03C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBACT04C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBCUS01C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBTRN01C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBTRN02C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBTRN03C | CEE3ABD | Abnormal termination | ABCODE, TIMING |
| CBSTM03A | CEE3ABD | Abnormal termination | (no params) |
| CBEXPORT | CEE3ABD | Abnormal termination | (no params) |
| CBIMPORT | CEE3ABD | Abnormal termination | (no params) |

### 2.3 External System Calls

| Program | External System | Protocol | Direction | Purpose |
|---------|----------------|----------|-----------|---------|
| COPAUA0C | MQ | MQGET/MQPUT | Bidirectional | Auth request/response processing |
| COACCT01 | MQ | MQGET/MQPUT | Bidirectional | Account inquiry via MQ |
| CODATE01 | MQ | MQGET/MQPUT | Bidirectional | System date inquiry via MQ |
| COPAUS0C | IMS DB | DL/I (GU, GN) | Read | Retrieve auth summary segments |
| COPAUS1C | IMS DB | DL/I (GU, ISRT) | Read/Write | Retrieve/update auth details |
| COPAUS2C | DB2 | SQL INSERT | Write | Log fraud decisions |
| COTRTLIC | DB2 | SQL SELECT, DELETE | Read/Write | List/delete transaction types |
| COTRTUPC | DB2 | SQL INSERT, UPDATE | Write | Add/update transaction types |
| COBTUPDT | DB2 | SQL UPDATE | Write | Batch maintain transaction types |
| CORPT00C | CICS TD Queue | WRITEQ TD | Write | Submit report job via transient data |

---

## 3. Copybook Usage Matrix

### 3.1 Data Record Copybooks

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C | CBACT01C | CBACT04C | CBTRN02C | CBTRN03C | CBSTM03A |
|----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y | | | | x | x | | | | | | x | | x | | | | | x | x | x | | |
| CVACT02Y | | | | x | | x | x | x | | | | | | | | | | | | | | |
| CVACT03Y | | | | x | | | | | | | x | | x | | | | | | x | x | | |
| CVCUS01Y | | | | x | | | x | x | | | | | | | | | | | | | | |
| CVCRD01Y | | | | | | x | x | x | | | | | | | | | | | | | | |
| CVTRA05Y | | | | | | | | | x | x | x | x | x | | | | | | | | | |
| CVTRA06Y | | | | | | | | | | | | | | | | | | | | x | | |
| CVTRA01Y | | | | | | | | | | | | | | | | | | | x | x | | |
| CVTRA02Y | | | | | | | | | | | | | | | | | | | x | | | |
| CVTRA03Y | | | | | | | | | | | | | | | | | | | | | x | |
| CVTRA04Y | | | | | | | | | | | | | | | | | | | | | x | |
| CVTRA07Y | | | | | | | | | | | | | | | | | | | | | x | |
| CSUSR01Y | x | x | x | x | | x | x | x | | | | | | x | x | x | x | | | | | |
| COSTM01 | | | | | | | | | | | | | | | | | | | | | | x |
| CVEXPORT | | | | | | | | | | | | | | | | | | | | | | |

### 3.2 System/Common Copybooks

| Copybook | Purpose | Used By (program count) |
|----------|---------|------------------------|
| COCOM01Y | COMMAREA structure | All 17 CICS online programs |
| COTTL01Y | Screen title layout | All 17 CICS online programs |
| CSDAT01Y | Date working storage | All 17 CICS online programs |
| CSMSG01Y | Short message area | All 17 CICS online programs |
| DFHAID | AID key definitions | All 17 CICS online programs |
| DFHBMSCA | BMS attribute constants | All 17 CICS online programs |
| CSMSG02Y | Long message area | COCRDSLC, COCRDUPC |
| CSSTRPFY | String pad/format utility | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSLKPCDY | Lookup code definitions | COACTUPC |
| CSSETATY | Screen attribute settings | COACTUPC |
| CODATECN | Date conversion record | CBACT01C |
| CSUTLDPY | Date utility params | COTRN02C, CORPT00C |
| CSUTLDWY | Date utility WS | CSUTLDTC |
| COMEN02Y | Menu option map | COMEN01C |
| COADM02Y | Admin option map | COADM01C |

### 3.3 BMS Copybooks

Each BMS copybook is used exclusively by its corresponding program:

| BMS Copybook | Program | Screen |
|-------------|---------|--------|
| COSGN00 | COSGN00C | Signon |
| COMEN01 | COMEN01C | Main Menu |
| COADM01 | COADM01C | Admin Menu |
| COACTVW | COACTVWC | Account View |
| COACTUP | COACTUPC | Account Update |
| COCRDLI | COCRDLIC | Card List |
| COCRDSL | COCRDSLC | Card Detail |
| COCRDUP | COCRDUPC | Card Update |
| COTRN00 | COTRN00C | Transaction List |
| COTRN01 | COTRN01C | Transaction View |
| COTRN02 | COTRN02C | Transaction Add |
| CORPT00 | CORPT00C | Transaction Report |
| COBIL00 | COBIL00C | Bill Payment |
| COUSR00 | COUSR00C | User List |
| COUSR01 | COUSR01C | User Add |
| COUSR02 | COUSR02C | User Update |
| COUSR03 | COUSR03C | User Delete |

---

## 4. VSAM File Access Map

### 4.1 Online (CICS) File Access

| VSAM File (CICS DD) | R | W | RW | DEL | BRW | Programs |
|---------------------|:-:|:-:|:--:|:---:|:---:|----------|
| USRSEC (User Security) | x | x | x | x | x | COSGN00C(R), COUSR00C(BRW), COUSR01C(W), COUSR02C(RW), COUSR03C(R,DEL) |
| ACCTDAT (Account Data) | x | | x | | | COACTVWC(R), COACTUPC(R,RW), COTRN02C(R), COBIL00C(R) |
| CARDDAT (Card Data) | x | | | | x | COCRDLIC(BRW), COCRDSLC(R), COCRDUPC(R) |
| CARDAIX (Card AIX) | x | | | | x | COCRDLIC(BRW) |
| CUSTDAT (Customer Data) | x | | | | | COACTVWC(R), COCRDSLC(R), COCRDUPC(R) |
| CARDXREF (Cross-Reference) | x | | | | | COACTVWC(R), COTRN02C(R), COBIL00C(R) |
| TRANSACT (Transactions) | x | x | | | x | COTRN00C(BRW), COTRN01C(R), COTRN02C(R,W), COBIL00C(R,W,BRW), CORPT00C(R) |
| TCATBALF (Cat Balance) | | | | | | (batch only) |
| DISCGRP (Disclosure Groups) | | | | | | (batch only) |
| TRANTYPE (Tran Types) | | | | | | (batch only, or DB2 online) |
| TRANCATG (Tran Categories) | | | | | | (batch only, or DB2 online) |

*R=Read, W=Write, RW=Rewrite, DEL=Delete, BRW=Browse (STARTBR/READNEXT/READPREV)*

### 4.2 Batch File Access

| Program | Input Files | Output Files | I-O Files |
|---------|------------|-------------|-----------|
| CBACT01C | ACCTFILE (account data) | OUTFILE, ARRYFILE, VBRCFILE | - |
| CBACT02C | CARDFILE (card data) | - | - |
| CBACT03C | XREFFILE (cross-reference) | - | - |
| CBACT04C | TCATBALF, XREFFILE, DISCGRP | TRANSACT | ACCTFILE (I-O) |
| CBCUS01C | CUSTFILE (customer data) | - | - |
| CBTRN01C | DALYTRAN, CUSTOMER, XREF, CARD, ACCOUNT, TRANSACT | - | - |
| CBTRN02C | DALYTRAN, XREFFILE | TRANFILE, DALYREJS | ACCTFILE (I-O), TCATBALF (I-O) |
| CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report) | - |
| CBSTM03A | (via CBSTM03B) | STMTFILE, HTMLFILE | - |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | - | - |
| CBEXPORT | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE | EXPFILE | - |
| CBIMPORT | IMPFILE | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE | - |

---

## 5. Batch Job Data Lineage

### 5.1 Daily Batch Processing Flow

```
                          ┌─────────────┐
                          │  CLOSEFIL   │  Close VSAM files from CICS
                          └──────┬──────┘
                                 │
              ┌──────────────────┼──────────────────┐
              v                  v                  v
       ┌──────────┐      ┌──────────┐       ┌──────────┐
       │ ACCTFILE │      │ CARDFILE │       │ CUSTFILE │
       │(refresh) │      │(refresh) │       │(refresh) │
       └────┬─────┘      └────┬─────┘       └────┬─────┘
            │                  │                   │
            v                  v                   v
       ┌──────────┐      ┌──────────┐       ┌──────────┐
       │ XREFFILE │      │ TRANBKP  │       │ DUSRSECJ │
       │  (load)  │      │ (backup) │       │(security)│
       └────┬─────┘      └────┬─────┘       └──────────┘
            │                  │
            │    ┌─────────────┤
            │    │             │
            v    v             v
       ┌───────────────┐  ┌──────────┐
       │   POSTTRAN    │  │ TRANCATG │  Load reference tables
       │  (CBTRN02C)   │  │ TRANTYPE │
       │ Core posting  │  │ DISCGRP  │
       └───────┬───────┘  │ TCATBALF │
               │          └──────────┘
               │
               v
       ┌───────────────┐
       │   INTCALC     │
       │  (CBACT04C)   │
       │   Interest    │
       │  calculation  │
       └───────┬───────┘
               │
               v
       ┌───────────────┐
       │   TRANBKP     │  Backup after processing
       │  (post-run)   │
       └───────┬───────┘
               │
               v
       ┌───────────────┐
       │   COMBTRAN    │
       │  (SORT)       │
       │Combine trans  │
       └───────┬───────┘
               │
       ┌───────┴───────┐
       v               v
┌──────────────┐ ┌──────────────┐
│  CREASTMT    │ │  TRANREPT    │
│ (CBSTM03A)  │ │ (CBTRN03C)  │
│  Statements  │ │Daily Report  │
│ (Text+HTML)  │ │              │
└──────────────┘ └──────────────┘
       │
       v
┌──────────────┐
│   TRANIDX    │  Define AIX on transaction file
└──────┬───────┘
       │
       v
┌──────────────┐
│   OPENFIL    │  Re-open VSAM files in CICS
└──────────────┘
```

### 5.2 Job-to-Dataset Matrix

| Job | Reads | Writes | Deletes/Defines |
|-----|-------|--------|----------------|
| **POSTTRAN** | DALYTRAN.PS, CARDXREF.VSAM | TRANSACT.VSAM, DALYREJS, TCATBALF.VSAM, ACCTDATA.VSAM | - |
| **INTCALC** | TCATBALF.VSAM, CARDXREF.VSAM, DISCGRP.VSAM | TRANSACT.VSAM | ACCTDATA.VSAM (I-O) |
| **COMBTRAN** | TRANSACT.VSAM, DALYTRAN.PS | Combined output (SORT) | - |
| **CREASTMT** | TRANSACT.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, CUSTDATA.VSAM | STATEMNT.PS, STATEMNT.HTML, TRXFL.VSAM | TRXFL.SEQ (temp) |
| **TRANREPT** | TRANSACT.VSAM, CARDXREF.VSAM, TRANTYPE.VSAM, TRANCATG.VSAM, DATEPARM | TRANREPT (report) | - |
| **ACCTFILE** | ACCTDATA.PS | ACCTDATA.VSAM.KSDS | Defines VSAM cluster |
| **CARDFILE** | CARDDATA.PS | CARDDATA.VSAM.KSDS | Defines VSAM cluster |
| **CUSTFILE** | CUSTDATA.PS | CUSTDATA.VSAM.KSDS | Defines VSAM cluster |
| **XREFFILE** | CARDXREF.PS | CARDXREF.VSAM.KSDS + AIX | Defines VSAM + AIX + PATH |
| **TRANFILE** | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS | Defines VSAM cluster |
| **TRANBKP** | TRANSACT.VSAM | TRANSACT.BKUP | Backup copy |
| **DUSRSECJ** | USRSEC.PS | USRSEC.VSAM.KSDS | Load via IEBGENER |
| **DISCGRP** | DISCGRP.PS | DISCGRP.VSAM.KSDS | Defines VSAM cluster |
| **TCATBALF** | TCATBALF.PS | TCATBALF.VSAM.KSDS | Defines VSAM cluster |
| **TRANCATG** | TRANCATG.PS | TRANCATG.VSAM.KSDS | Defines VSAM cluster |
| **TRANTYPE** | TRANTYPE.PS | TRANTYPE.VSAM.KSDS | Defines VSAM cluster |
| **CBEXPORT** | All VSAM files | EXPORT.DATA.PS | - |
| **CBIMPORT** | Import flat file | All VSAM files | - |
| **READACCT** | ACCTDATA.VSAM | SYSOUT (display) | - |
| **READCARD** | CARDDATA.VSAM | SYSOUT (display) | - |
| **READCUST** | CUSTDATA.VSAM | SYSOUT (display) | - |
| **READXREF** | CARDXREF.VSAM | SYSOUT (display) | - |

### 5.3 Environment Setup Flow

```
DEFGDGB ──> DUSRSECJ ──> ACCTFILE ──> CARDFILE ──> CUSTFILE ──> XREFFILE
    │                                                                │
    v                                                                v
DEFGDGD ──> CREADB21 *[DB2]                                    TRANFILE
                                                                    │
                                                                    v
                                              DISCGRP ──> TCATBALF ──> TRANCATG ──> TRANTYPE
                                                                                        │
                                                                                        v
                                                                                   OPENFIL
```

---

## 6. Optional Module Dependencies

### IMS-DB2-MQ: Pending Authorizations

```
MQ Queue ──> COPAUA0C ──> IMS DB (PSBPAUTB)
                              │
                    ┌─────────┴─────────┐
                    v                   v
              COPAUS0C            COPAUS1C
            (Summary View)     (Detail View)
                                    │
                                    v
                              COPAUS2C ──> DB2 (AUTHFRDS table)
                              (Fraud Flag)

CBPAUP0J (JCL) ──> CBPAUP0C ──> IMS DB (purge expired)
LOADPADB (JCL) ──> PAUDBLOD ──> IMS DB (load)
UNLDPADB (JCL) ──> PAUDBUNL ──> IMS DB (unload)
UNLDGSAM (JCL) ──> DBUNLDGS ──> IMS GSAM (generic unload)
```

**Additional copybooks**: CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, IMSFUNCS, PAUTBPCB, PADFLPCB, PASFLPCB

### DB2: Transaction Type Management

```
COTRTLIC (CTLI) ──> DB2 TRNTYPE/TRNTYCAT tables
    │                  (SELECT with cursor, DELETE)
    v
COTRTUPC (CTTU) ──> DB2 TRNTYPE/TRNTYCAT tables
                       (INSERT, UPDATE)

MNTTRDB2 (JCL) ──> COBTUPDT ──> DB2 TRNTYPE table (batch UPDATE)
CREADB21 (JCL) ──> DSNTEP4  ──> Creates DB2 schema + loads initial data
TRANEXTR (JCL) ──> DSNTIAUL ──> Extracts DB2 data to flat files
```

**Additional copybooks**: CSDB2RPY, CSDB2RWY, DCLTRTYP, DCLTRCAT

### MQ: Account & Date Inquiry

```
COACCT01 (CDRA) ──> MQ request ──> reads ACCTDATA.VSAM ──> MQ response
CODATE01 (CDRD) ──> MQ request ──> reads system date  ──> MQ response
```

---

## 7. Full Adjacency List

### Program-to-Program Dependencies

```
COSGN00C    --> COMEN01C, COADM01C
COMEN01C    --> COSGN00C, COACTVWC, COCRDLIC, COTRN00C, COTRN02C,
                CORPT00C, COBIL00C, COPAUS0C*, COACCT01*, CODATE01*
COADM01C    --> COSGN00C, COUSR00C, COTRTLIC*, COTRTUPC*
COACTVWC    --> COMEN01C, COACTUPC
COACTUPC    --> COMEN01C
COCRDLIC    --> COMEN01C, COCRDSLC, COCRDUPC
COCRDSLC    --> COCRDLIC
COCRDUPC    --> COCRDLIC
COTRN00C    --> COMEN01C, COTRN01C, COTRN02C
COTRN01C    --> COMEN01C
COTRN02C    --> COMEN01C, CSUTLDTC
CORPT00C    --> COMEN01C, CSUTLDTC
COBIL00C    --> COMEN01C
COUSR00C    --> COADM01C, COUSR01C, COUSR02C, COUSR03C
COUSR01C    --> COADM01C
COUSR02C    --> COADM01C
COUSR03C    --> COADM01C
CBSTM03A    --> CBSTM03B, CEE3ABD
CBACT01C    --> COBDATFT (ASM), CEE3ABD
CBACT04C    --> CEE3ABD
CBTRN02C    --> CEE3ABD
CBTRN03C    --> CEE3ABD
COBSWAIT    --> MVSWAIT (ASM)
CSUTLDTC    --> CEEDAYS (LE)

* = Optional module
```

### JCL Job Sequencing Dependencies

```
CLOSEFIL --> ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANBKP,
             DUSRSECJ, DISCGRP, TCATBALF, TRANCATG, TRANTYPE
POSTTRAN --> depends on: CLOSEFIL, ACCTFILE, CARDFILE, XREFFILE, CUSTFILE, TRANBKP
INTCALC  --> depends on: POSTTRAN
TRANBKP  --> depends on: INTCALC (post-run backup)
COMBTRAN --> depends on: TRANBKP
CREASTMT --> depends on: COMBTRAN
TRANREPT --> can run after COMBTRAN (parallel with CREASTMT)
TRANIDX  --> depends on: CREASTMT or COMBTRAN
OPENFIL  --> depends on: all above complete
```
