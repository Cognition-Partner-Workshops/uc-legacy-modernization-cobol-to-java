# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Purpose:** Program call graph, screen navigation flow, copybook dependencies, JCL job chains, and data lineage

---

## Table of Contents

1. [Online Program Call Graph (XCTL / LINK)](#online-program-call-graph-xctl--link)
2. [Screen Navigation Flow](#screen-navigation-flow)
3. [Batch Program Call Graph](#batch-program-call-graph)
4. [Copybook Dependency Matrix](#copybook-dependency-matrix)
5. [VSAM File Access Matrix](#vsam-file-access-matrix)
6. [JCL Job → Program Mapping](#jcl-job--program-mapping)
7. [JCL Job Data Lineage](#jcl-job-data-lineage)
8. [Batch Processing Sequence](#batch-processing-sequence)
9. [Optional Module Dependencies](#optional-module-dependencies)
10. [External Subroutine Calls](#external-subroutine-calls)

---

## Online Program Call Graph (XCTL / LINK)

All online program transfers use `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` via the COMMAREA.

```
COSGN00C (Sign-On / CC00)
├── [Admin user] ──XCTL──→ COADM01C (Admin Menu / CA00)
│   ├── Option 1 ──XCTL──→ COUSR00C (User List / CU00)
│   │   ├── Select (Add) ──XCTL──→ COUSR01C (User Add / CU01)
│   │   │   └── PF3 (Back) ──XCTL──→ COADM01C
│   │   ├── Select (Update) ──XCTL──→ COUSR02C (User Update / CU02)
│   │   │   └── PF3 (Back) ──XCTL──→ COADM01C
│   │   └── Select (Delete) ──XCTL──→ COUSR03C (User Delete / CU03)
│   │       └── PF3 (Back) ──XCTL──→ COADM01C
│   └── PF3 (Back) ──XCTL──→ COSGN00C
│
└── [Regular user] ──XCTL──→ COMEN01C (Main Menu / CM00)
    ├── Option 1 ──XCTL──→ COACTVWC (Account View / CA01)
    │   └── PF3 (Back) ──XCTL──→ COMEN01C
    ├── Option 2 ──XCTL──→ COACTUPC (Account Update / CA02)
    │   └── PF3 (Back) ──XCTL──→ COMEN01C
    ├── Option 3 ──XCTL──→ COCRDLIC (Card List / CC01)
    │   ├── Select (View) ──XCTL──→ COCRDSLC (Card Detail / CC02)
    │   │   └── PF3 (Back) ──XCTL──→ COCRDLIC
    │   └── Select (Update) ──XCTL──→ COCRDUPC (Card Update / CC03)
    │       └── PF3 (Back) ──XCTL──→ COCRDLIC
    ├── Option 4 ──XCTL──→ COTRN00C (Transaction List / CT00)
    │   ├── Select ──XCTL──→ COTRN01C (Transaction View / CT01)
    │   │   └── PF3 (Back) ──XCTL──→ COTRN00C
    │   └── PF4 (Add) ──XCTL──→ COTRN02C (Transaction Add / CT02)
    │       └── PF3 (Back) ──XCTL──→ COTRN00C
    ├── Option 5 ──XCTL──→ COBIL00C (Bill Payment / CB00)
    │   └── PF3 (Back) ──XCTL──→ COMEN01C
    ├── Option 6 ──XCTL──→ CORPT00C (Transaction Report / CR00)
    │   └── PF3 (Back) ──XCTL──→ COMEN01C
    └── PF3 (Back) ──XCTL──→ COSGN00C
```

### Transfer Rules

| Source Program | PF3 Target | Default Fallback |
|---|---|---|
| All menu-launched programs | COMEN01C or COADM01C (via `CDEMO-FROM-PROGRAM`) | COSGN00C |
| COTRN01C | COTRN00C | COMEN01C |
| COUSR01C/02C/03C | COADM01C | COSGN00C |
| COCRDSLC/COCRDUPC | COCRDLIC | COMEN01C |

---

## Screen Navigation Flow

```
┌──────────────┐
│  COSGN00C    │
│  Login       │
│  (COSGN00)   │
└──────┬───────┘
       │
  ┌────┴─────┐
  │          │
  ▼          ▼
┌─────────┐  ┌──────────┐
│COMEN01C │  │COADM01C  │
│Main Menu│  │Admin Menu│
│(COMEN01)│  │(COADM01) │
└────┬────┘  └────┬─────┘
     │            │
     ├──→ COACTVWC (Account View)     ├──→ COUSR00C (User List)
     │    (COACTVW)                    │    (COUSR00)
     │                                 │    ├──→ COUSR01C (Add User)
     ├──→ COACTUPC (Account Update)   │    │    (COUSR01)
     │    (COACTUP)                    │    ├──→ COUSR02C (Update User)
     │                                 │    │    (COUSR02)
     ├──→ COCRDLIC (Card List)        │    └──→ COUSR03C (Delete User)
     │    (COCRDLI)                    │         (COUSR03)
     │    ├──→ COCRDSLC (Card View)   │
     │    │    (COCRDSL)               └──→ Back to COSGN00C
     │    └──→ COCRDUPC (Card Update) 
     │         (COCRDUP)              
     │                                
     ├──→ COTRN00C (Txn List)        
     │    (COTRN00)                   
     │    ├──→ COTRN01C (Txn View)   
     │    │    (COTRN01)              
     │    └──→ COTRN02C (Txn Add)    
     │         (COTRN02)              
     │                                
     ├──→ COBIL00C (Bill Payment)    
     │    (COBIL00)                   
     │                                
     ├──→ CORPT00C (Report Request)  
     │    (CORPT00)                   
     │                                
     └──→ Back to COSGN00C           
```

---

## Batch Program Call Graph

```
CBSTM03A (Statement Generator - Main)
└── CALL 'CBSTM03B' ×11 (Statement Generator - File I/O Sub)

CBACT01C (Account Reader)
└── CALL 'COBDATFT' (Assembler: Date Formatting)

COTRN02C (Transaction Add - Online)
└── CALL 'CSUTLDTC' ×2 (Date Validation Utility)
    └── CALL 'CEEDAYS' (LE: Date API)

CORPT00C (Report Request - Online)
└── CALL 'CSUTLDTC' ×2 (Date Validation Utility)
    └── CALL 'CEEDAYS' (LE: Date API)

COBSWAIT (Wait Utility)
└── CALL 'MVSWAIT' (Assembler: Wait Routine)

CBACT02C, CBACT03C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C
└── CALL 'CEE3ABD' (LE: Abnormal End)

CBEXPORT (Data Export)
└── CALL 'CEE3ABD' (LE: Abnormal End)

CBIMPORT (Data Import)
└── CALL 'CEE3ABD' (LE: Abnormal End)
```

### Call Summary Table

| Caller | Callee | Method | Frequency | Purpose |
|---|---|---|---|---|
| CBSTM03A | CBSTM03B | `CALL` | 11 times | File open/close/read for statements |
| CBACT01C | COBDATFT | `CALL` | 1 | Assembler date formatting |
| COTRN02C | CSUTLDTC | `CALL` | 2 | Validate start/end dates |
| CORPT00C | CSUTLDTC | `CALL` | 2 | Validate report date range |
| CSUTLDTC | CEEDAYS | `CALL` | 1 | LE date validation API |
| COBSWAIT | MVSWAIT | `CALL` | 1 | Assembler wait delay |
| 8 programs | CEE3ABD | `CALL` | 1 each | LE abnormal termination |

---

## Copybook Dependency Matrix

Shows which copybooks are included (`COPY`) by which programs.

| Copybook | Core Online Programs | Batch Programs | Optional Programs |
|---|---|---|---|
| **COCOM01Y** (COMMAREA) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C, COUSR00C–03C | — | COTRTLIC, COTRTUPC |
| **COTTL01Y** (Title) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **CSDAT01Y** (Date) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **CSMSG01Y** (Message) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **CSUSR01Y** (User Sec) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **DFHAID** (AID keys) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **DFHBMSCA** (BMS attr) | All 17 online programs | — | COTRTLIC, COTRTUPC |
| **CVACT01Y** (Account) | COACTVWC, COACTUPC | CBACT01C, CBACT04C, CBTRN01C, CBEXPORT, CBIMPORT | COACCT01 |
| **CVACT02Y** (Card) | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBEXPORT, CBIMPORT | COTRTLIC |
| **CVACT03Y** (Xref) | COACTVWC, COACTUPC | CBACT03C, CBACT04C, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | — |
| **CVCUS01Y** (Customer) | COACTUPC | CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | — |
| **CVCRD01Y** (Ext Card) | COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | — | COTRTLIC, COTRTUPC |
| **CVTRA05Y** (Transaction) | COTRN02C | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | — |
| **CVTRA06Y** (Daily Txn) | — | CBTRN01C, CBTRN02C | — |
| **CVTRA01Y** (Cat Bal) | — | CBACT04C, CBTRN02C | — |
| **CVTRA02Y** (Disc Grp) | — | CBACT04C | — |
| **CVTRA03Y** (Txn Type) | — | CBTRN03C | — |
| **CVTRA04Y** (Txn Cat) | — | CBTRN03C | — |
| **CVTRA07Y** (Report) | — | CBTRN03C | — |
| **CSMSG02Y** (Long Msg) | COACTUPC, COCRDUPC | — | COTRTUPC |
| **CSLKPCDY** (Lookup) | COACTUPC | — | — |
| **CSUTLDWY** (Date WS) | COACTUPC | — | COTRTUPC |
| **CSSETATY** (Attr Set) | COACTUPC | — | COTRTUPC |
| **CSSTRPFY** (Str Parse) | — | — | COTRTLIC, COTRTUPC |
| **CVEXPORT** (Export) | — | CBEXPORT, CBIMPORT | — |
| **COSTM01** (Stmt Txn) | — | CBSTM03A | — |
| **CUSTREC** (Cust Alt) | — | CBSTM03A | — |
| **CODATECN** (Date Conv) | — | CBACT01C | — |
| **COMEN02Y** (Menu Def) | COMEN01C | — | — |
| **COADM02Y** (Admin Def) | COADM01C | — | — |

---

## VSAM File Access Matrix

Shows which programs access which VSAM files and the type of access.

| VSAM File | Online Read | Online Write/Update | Batch Read | Batch Write |
|---|---|---|---|---|
| **USRSEC** (User Security) | COSGN00C(R), COUSR00C(BR), COUSR02C(R), COUSR03C(R) | COUSR01C(W), COUSR02C(RW), COUSR03C(D) | — | DUSRSECJ(REPRO) |
| **ACCTDATA** (Accounts) | COACTVWC(R), COACTUPC(R,RW), COBIL00C(R,RW) | COACTUPC(RW), COBIL00C(RW) | CBACT01C(R), CBACT04C(R,RW), CBTRN01C(R), CBTRN02C(R), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) | ACCTFILE(REPRO) |
| **CARDDATA** (Cards) | COCRDLIC(BR), COCRDSLC(R), COCRDUPC(R,RW) | COCRDUPC(RW) | CBACT02C(R), CBTRN01C(R), CBEXPORT(R), CBIMPORT(W) | CARDFILE(REPRO) |
| **CARDXREF** (Cross-Ref) | COACTVWC(R), COACTUPC(R) | — | CBACT03C(R), CBACT04C(R), CBTRN01C(R), CBTRN02C(R), CBTRN03C(R), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) | XREFFILE(REPRO) |
| **CUSTDATA** (Customers) | COACTUPC(R), COACTVWC(R) | — | CBCUS01C(R), CBTRN01C(R), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) | CUSTFILE(REPRO) |
| **TRANSACT** (Transactions) | COTRN00C(BR), COTRN01C(R), COTRN02C(R,W) | COTRN02C(W) | CBTRN01C(RW), CBTRN02C(RW), CBTRN03C(R), CBSTM03A(R), CBEXPORT(R) | TRANFILE(REPRO) |
| **DALYTRAN** (Daily Txns) | — | — | CBTRN01C(R), CBTRN02C(R) | — |
| **TCATBALF** (Cat Balance) | — | — | CBACT04C(R), CBTRN02C(RW) | TCATBALF(REPRO) |
| **DISCGRP** (Interest) | — | — | CBACT04C(R) | DISCGRP(REPRO) |
| **TRANTYPE** (Txn Types) | — | — | CBTRN03C(R) | TRANTYPE(REPRO) |
| **TRANCATG** (Txn Cats) | — | — | CBTRN03C(R) | TRANCATG(REPRO) |
| **DALYREJS** (Rejects) | — | — | — | CBTRN02C(W) |

**Legend:** R = Read, W = Write, RW = Read/Rewrite (Update), D = Delete, BR = Browse (STARTBR/READNEXT/READPREV), REPRO = IDCAMS REPRO load

---

## JCL Job → Program Mapping

| JCL Job | Step | Program Executed | Purpose |
|---|---|---|---|
| POSTTRAN | STEP10 | **CBTRN02C** | Post daily transactions to master |
| INTCALC | STEP10 | **CBACT04C** | Calculate interest on balances |
| CREASTMT | STEP040 | **CBSTM03A** (→CBSTM03B) | Generate statements |
| TRANREPT | STEP10R | **CBTRN03C** | Generate transaction report |
| CBEXPORT | STEP10 | **CBEXPORT** | Export customer data |
| CBIMPORT | STEP10 | **CBIMPORT** | Import customer data |
| WAITSTEP | WAIT | **COBSWAIT** (→MVSWAIT) | Timed wait |
| READACCT | STEP10 | **CBACT01C** | Read/verify account file |
| READCARD | STEP10 | **CBACT02C** | Read/verify card file |
| READCUST | STEP10 | **CBCUS01C** | Read/verify customer file |
| READXREF | STEP10 | **CBACT03C** | Read/verify xref file |
| TRANBKP | STEP05R | **REPROC** (proc) | Backup transaction file |
| TXT2PDF1 | TXT2PDF | **IKJEFT1B** (TXT2PDF) | Convert statements to PDF |
| All data-load jobs | Various | **IDCAMS** (REPRO/DEFINE) | Load VSAM from flat files |
| CLOSEFIL / OPENFIL | Various | **SDSF** | Close/open CICS files |

---

## JCL Job Data Lineage

### Data Flow: Flat Files → VSAM Files

```
app/data/ASCII/*.txt (Source Data)
        │
        ▼ (FTP to mainframe + EBCDIC conversion)
*.PS (Flat Sequential Files)
        │
        ▼ (IDCAMS REPRO in JCL)
┌───────────────────────────────────────────────────┐
│ VSAM KSDS Files (Online + Batch Access)           │
│                                                   │
│  ACCTDATA.VSAM.KSDS  ←── ACCTFILE.jcl            │
│  CARDDATA.VSAM.KSDS  ←── CARDFILE.jcl            │
│  CUSTDATA.VSAM.KSDS  ←── CUSTFILE.jcl            │
│  CARDXREF.VSAM.KSDS  ←── XREFFILE.jcl            │
│  TRANSACT.VSAM.KSDS  ←── TRANFILE.jcl            │
│  USRSEC.VSAM.KSDS    ←── DUSRSECJ.jcl            │
│  TRANTYPE.VSAM.KSDS  ←── TRANTYPE.jcl            │
│  TRANCATG.VSAM.KSDS  ←── TRANCATG.jcl            │
│  TCATBALF.VSAM.KSDS  ←── TCATBALF.jcl            │
│  DISCGRP.VSAM.KSDS   ←── DISCGRP.jcl             │
└───────────────────────────────────────────────────┘
```

### Data Flow: Batch Processing Cycle

```
DALYTRAN.PS (Daily Transactions - sequential input)
        │
        ▼
┌──────────────────────────────────────────────────────────────────┐
│ POSTTRAN.jcl → CBTRN02C                                         │
│   Reads:  DALYTRAN (daily txns), CARDXREF, ACCTDATA             │
│   Writes: TRANSACT (master), TCATBALF (category balances),      │
│           DALYREJS (rejected transactions)                       │
│   Updates: ACCTDATA (account balances)                           │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ INTCALC.jcl → CBACT04C                                          │
│   Reads:  TCATBALF, CARDXREF, DISCGRP, ACCTDATA, TRANSACT      │
│   Updates: ACCTDATA (interest/fee amounts)                       │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ TRANBKP.jcl → REPROC (REPRO)                                    │
│   Reads:  TRANSACT.VSAM.KSDS                                    │
│   Writes: TRANSACT.BKUP(+1) (GDG backup)                        │
└──────────────┬───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│ COMBTRAN.jcl → IDCAMS REPRO                                     │
│   Reads:  TRANSACT.BKUP(+1)                                     │
│   Writes: TRANSACT.VSAM.KSDS (consolidated)                     │
└──────────────┬───────────────────────────────────────────────────┘
               │
        ┌──────┴──────┐
        ▼             ▼
┌──────────────┐  ┌──────────────────────────────────────────────┐
│ CREASTMT.JCL │  │ TRANREPT.jcl → SORT + CBTRN03C              │
│ SORT +       │  │   Reads:  TRANSACT.DALY(+1), CARDXREF,      │
│ CBSTM03A     │  │           TRANTYPE, TRANCATG, DATEPARM       │
│ →CBSTM03B    │  │   Writes: TRANREPT(+1) (report GDG)         │
│              │  └──────────────────────────────────────────────┘
│ Reads:       │
│  TRXFL.VSAM, │
│  CARDXREF,   │
│  ACCTDATA,   │
│  CUSTDATA    │
│              │
│ Writes:      │
│  STATEMNT.PS │
│  STATEMNT.HTML│
└──────────────┘
```

---

## Batch Processing Sequence

The complete nightly batch cycle executes in this order:

```
1. CLOSEFIL.jcl          Close CICS files for exclusive batch access
       │
2. Data Refresh Jobs      Reload reference data from flat files
   ├── ACCTFILE.jcl       (Account master)
   ├── CARDFILE.jcl       (Card master + AIX)
   ├── CUSTFILE.jcl       (Customer master)
   ├── XREFFILE.jcl       (Cross-reference + AIX)
   ├── TRANFILE.jcl       (Transaction master + AIX)
   ├── TCATBALF.jcl       (Category balances)
   ├── DISCGRP.jcl        (Disclosure groups)
   ├── TRANTYPE.jcl       (Transaction types)
   └── TRANCATG.jcl       (Transaction categories)
       │
3. POSTTRAN.jcl           Post daily transactions → CBTRN02C
       │
4. INTCALC.jcl            Calculate interest → CBACT04C
       │
5. TRANBKP.jcl            Backup transaction file to GDG
       │
6. COMBTRAN.jcl           Combine daily transactions into master
       │
7. CREASTMT.JCL           Generate statements → CBSTM03A
       │
8. TRANREPT.jcl           Generate daily report → CBTRN03C
       │
9. TRANIDX.jcl            Rebuild transaction alternate indexes
       │
10. OPENFIL.jcl           Re-open CICS files for online access
```

> **Scheduler Configs:** CA7 (`app/scheduler/CardDemo.ca7`) and Control-M (`app/scheduler/CardDemo.controlm`) define this sequence with dependencies.

---

## Optional Module Dependencies

### Authorization Module (IMS/DB2/MQ)

```
COPAUA0C (MQ Trigger)
├── MQ Queues: Request Queue, Response Queue, Error Queue
├── CICS RETRIEVE (triggered by MQ)
└── MQGET / MQPUT calls

COPAUS0C (Auth Summary)
├── IMS DL/I calls (GU, GN, GNP) via PCB
└── Uses: CIPAUSMY (summary segment)

COPAUS1C (Auth Details)
├── IMS DL/I calls via PCB
└── Uses: CIPAUDTY (detail segment)

COPAUS2C (Fraud Marking)
├── EXEC SQL (DB2) — UPDATE fraud flags
└── Uses: CIPAUDTY

CBPAUP0C (Batch Purge)
├── IMS DL/I calls (GU, GN, DLET)
└── Purges expired authorizations from IMS DB
```

### Transaction Type DB2 Module

```
COTRTLIC (List/Delete)
├── EXEC SQL: SELECT with cursor (paging)
├── EXEC SQL: DELETE
├── EXEC CICS XCTL → COTRTUPC (for update)
└── Uses: CSDB2RWY, CSDB2RPY (DB2 common code)

COTRTUPC (Add/Update)
├── EXEC SQL: INSERT, UPDATE
├── EXEC CICS SYNCPOINT
└── Uses: CSDB2RWY, CSDB2RPY, DCLTRTYP, DCLTRCAT

COBTUPDT (Batch Update)
├── EXEC SQL: UPDATE
└── Standalone batch DB2 program
```

### VSAM-MQ Module

```
COACCT01 (Account Inquiry)
├── MQ: MQOPEN, MQGET, MQPUT, MQCLOSE
├── EXEC CICS READ (ACCTDATA VSAM)
└── Request/Response message pattern via MQ

CODATE01 (System Date)
├── MQ: MQOPEN, MQGET, MQPUT, MQCLOSE
├── EXEC CICS ASKTIME / FORMATTIME
└── Returns current system date/time via MQ
```

---

## External Subroutine Calls

Programs that call external subroutines (not other CardDemo programs):

| External Routine | Type | Called By | Purpose |
|---|---|---|---|
| `CEEDAYS` | LE Runtime | CSUTLDTC | Convert date to Lilian format for validation |
| `CEE3ABD` | LE Runtime | CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT | Abnormal end (abend) |
| `COBDATFT` | Assembler | CBACT01C | Date formatting |
| `MVSWAIT` | Assembler | COBSWAIT | Timed wait/delay |
| `MQOPEN` | MQ API | COACCT01, CODATE01 | Open MQ queue |
| `MQGET` | MQ API | COACCT01, CODATE01 | Get message from queue |
| `MQPUT` | MQ API | COACCT01, CODATE01 | Put message to queue |
| `MQCLOSE` | MQ API | COACCT01, CODATE01 | Close MQ queue |
| `DSNTIAC` | DB2 Utility | CSDB2RPY (copybook) | Format DB2 error messages |
| `IKJEFT1B` | TSO | TXT2PDF1.JCL | TSO batch for TXT2PDF REXX |
