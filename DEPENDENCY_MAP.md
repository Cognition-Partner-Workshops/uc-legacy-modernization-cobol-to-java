# CardDemo Dependency Map

> **System**: CardDemo — Mainframe Credit Card Management System
> **Last Updated**: 2026-05-27

---

## 1. Online Call Graph (CICS)

The online application uses **EXEC CICS XCTL** (transfer control) to navigate between programs, passing the COMMAREA (COCOM01Y). All navigation flows through the menu programs.

```
                          ┌──────────────────┐
                          │    COSGN00C      │
                          │  Sign-on (CC00)  │
                          └────────┬─────────┘
                       ┌───────────┴───────────┐
                       │  (Admin?)             │  (User?)
                       ▼                       ▼
                 ┌───────────┐          ┌───────────┐
                 │ COADM01C  │          │ COMEN01C  │
                 │Admin Menu │          │Main Menu  │
                 │  (CA00)   │          │  (CM00)   │
                 └─────┬─────┘          └─────┬─────┘
                       │                      │
        ┌──────────────┤         ┌────────────┼────────────────┐
        │              │         │            │                │
        ▼              ▼         ▼            ▼                ▼
   ┌─────────┐   ┌─────────┐ ┌─────────┐ ┌─────────┐   ┌─────────┐
   │COUSR00C │   │COUSR01C │ │COACTVWC │ │COACTUPC │   │COCRDLIC │
   │User List│   │User Add │ │Acct View│ │Acct Upd │   │Card List│
   │ (CU00)  │   │ (CU01)  │ │ (CAVW)  │ │ (CAUP)  │   │ (CCLI)  │
   └─────────┘   └─────────┘ └─────────┘ └─────────┘   └────┬────┘
        │              │                                      │
        ▼              ▼                          ┌───────────┼───────────┐
   ┌─────────┐   ┌─────────┐                     ▼           ▼           │
   │COUSR02C │   │COUSR03C │                ┌─────────┐ ┌─────────┐      │
   │User Upd │   │User Del │                │COCRDSLC │ │COCRDUPC │      │
   │ (CU02)  │   │ (CU03)  │                │Card View│ │Card Upd │      │
   └─────────┘   └─────────┘                │ (CCDL)  │ │ (CCUP)  │      │
                                             └─────────┘ └─────────┘      │
                                                                          │
   ┌──────────────────────────────────────────────────────────────────────┘
   │
   │  (Also from COMEN01C Main Menu:)
   │
   ├──►┌─────────┐   ┌─────────┐   ┌─────────┐
   │   │COTRN00C │──►│COTRN01C │   │COTRN02C │
   │   │Txn List │   │Txn View │   │Txn Add  │
   │   │ (CT00)  │   │ (CT01)  │   │ (CT02)  │
   │   └─────────┘   └─────────┘   └────┬────┘
   │                                     │
   │                                     │ CALL
   │                                     ▼
   │                               ┌──────────┐
   │                               │CSUTLDTC  │
   │                               │Date Valid.│
   │                               └──────────┘
   │
   ├──►┌─────────┐          ┌─────────┐
   │   │CORPT00C │          │COBIL00C │
   │   │Reports  │          │Bill Pay │
   │   │ (CR00)  │          │ (CB00)  │
   │   └────┬────┘          └─────────┘
   │        │ CALL
   │        ▼
   │  ┌──────────┐
   │  │CSUTLDTC  │
   │  │Date Valid.│
   │  └──────────┘
   │
   └──►┌─────────┐   ┌─────────┐   ┌─────────┐
       │COPAUS0C │──►│COPAUS1C │   │COPAUS2C │
       │Auth Sum │   │Auth Det │   │Auth Help│
       │ (CPVS)  │   │ (CPVD)  │   │         │
       └─────────┘   └─────────┘   └─────────┘
                     (Optional: IMS-DB2-MQ)
```

### 1.1 XCTL Transfer Details

| Source Program | Target Program | Mechanism | Condition |
|:---------------|:---------------|:----------|:----------|
| COSGN00C | COADM01C | `EXEC CICS XCTL PROGRAM('COADM01C')` | User type = Admin ('A') |
| COSGN00C | COMEN01C | `EXEC CICS XCTL PROGRAM('COMEN01C')` | User type = Regular ('U') |
| COMEN01C | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME)` | Menu option 1–11 |
| COADM01C | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-ADMIN-OPT-PGMNAME)` | Admin option 1–6 |
| COACTVWC | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to caller |
| COACTUPC | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to caller |
| COCRDLIC | LIT-MENUPGM | `EXEC CICS XCTL PROGRAM(LIT-MENUPGM)` | PF3 — return to menu |
| COCRDLIC | CCARD-NEXT-PROG | `EXEC CICS XCTL PROGRAM(CCARD-NEXT-PROG)` | Select card → view/update |
| COCRDSLC | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to caller |
| COCRDUPC | *(dynamic)* | `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to caller |

### 1.2 CALL Dependencies (Subroutine Calls)

| Caller | Callee | Mechanism | Purpose |
|:-------|:-------|:----------|:--------|
| CORPT00C | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation for report date range |
| COTRN02C | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation for new transaction |
| CSUTLDTC | CEEDAYS | `CALL 'CEEDAYS'` | LE date intrinsic (Lilian date conversion) |
| CBACT01C | COBDATFT | `CALL 'COBDATFT'` | ASM date format conversion |
| COBSWAIT | MVSWAIT | `CALL 'MVSWAIT'` | ASM timer wait |
| CBSTM03A | CBSTM03B | `CALL 'CBSTM03B'` | File I/O subroutine (×12 calls) |
| All batch | CEE3ABD | `CALL 'CEE3ABD'` | LE abnormal termination handler |

---

## 2. Batch Call Graph

```
                    ┌───────────────────────────────────────────┐
                    │           Control-M Scheduler             │
                    └─────────────────┬─────────────────────────┘
                                      │
           ┌──────────────────────────┼──────────────────────────┐
           │                          │                          │
     DAILY Workflow            WEEKLY Workflow           MONTHLY Workflow
           │                          │                          │
     ┌─────┴─────┐            ┌───────┴──────┐          ┌───────┴───────┐
     │CLOSEFIL   │            │MNTTRDB2      │          │CLOSEFIL       │
     │(SDSF)     │            │(COBTUPDT)    │          │(SDSF)         │
     └─────┬─────┘            └───────┬──────┘          └───────┬───────┘
           ▼                          │                         ▼
     ┌───────────┐           ┌────────┴────────┐        ┌───────────┐
     │TRANBKP    │           │                 │        │INTCALC    │
     │(IDCAMS)   │           ▼                 ▼        │(CBACT04C) │
     └─────┬─────┘     ┌──────────┐    ┌──────────┐    └─────┬─────┘
           ▼           │DISCGRP   │    │TRANEXTR  │          ▼
     ┌───────────┐     │Refresh   │    │(DSNTIAUL)│    ┌───────────┐
     │WAITSTEP   │     │(Smart    │    └──────────┘    │COMBTRAN   │
     │(COBSWAIT) │     │ Folder)  │                    │(SORT)     │
     └─────┬─────┘     └──────────┘                    └─────┬─────┘
           ▼                                                  ▼
     ┌───────────┐                                     ┌───────────┐
     │OPENFIL    │                                     │WAITSTEP   │
     │(SDSF)     │                                     │(COBSWAIT) │
     └───────────┘                                     └─────┬─────┘
                                                             ▼
                                                       ┌───────────┐
                                                       │OPENFIL    │
                                                       │(SDSF)     │
                                                       └───────────┘
```

### 2.1 Batch CALL Chain Details

| JCL Job | Program | Calls | Purpose |
|:--------|:--------|:------|:--------|
| POSTTRAN | CBTRN02C | CEE3ABD | Post daily transactions to VSAM |
| INTCALC | CBACT04C | CEE3ABD | Calculate interest on accounts |
| CREASTMT | CBSTM03A → CBSTM03B | CEE3ABD | Generate statements; CBSTM03B handles file I/O |
| TRANREPT | CBTRN03C | CEE3ABD | Generate transaction reports |
| READACCT | CBACT01C → COBDATFT | CEE3ABD | Read accounts with date conversion |
| WAITSTEP | COBSWAIT → MVSWAIT | — | Timer wait via assembler |
| CBEXPORT | CBEXPORT | CEE3ABD | Export data for branch migration |
| CBIMPORT | CBIMPORT | CEE3ABD | Import data from export file |

---

## 3. Copybook Dependency Matrix

Shows which programs include which copybooks (COPY statements).

| Copybook | Used By (Programs) |
|:---------|:-------------------|
| **COCOM01Y** (Commarea) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** (Titles) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** (Date/Time) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** (Messages) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **DFHAID** (AID Keys) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **DFHBMSCA** (BMS Attrs) | COACTVWC, COACTUPC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** (User Rec) | COADM01C, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CVCRD01Y** (Card Fields) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CVACT01Y** (Account Rec) | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN02C, COBIL00C, COTRN02C |
| **CVACT02Y** (Card Rec) | CBACT02C, CBEXPORT, CBIMPORT |
| **CVACT03Y** (XREF Rec) | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN02C |
| **CVCUS01Y** (Customer) | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C |
| **CVTRA05Y** (Transaction) | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C |
| **CVTRA06Y** (Daily Tran) | CBTRN01C, CBTRN02C |
| **CVTRA01Y** (Cat Balance) | CBACT04C, CBTRN02C |
| **CVTRA02Y** (Disclosure) | CBACT04C |
| **CVTRA03Y** (Tran Type) | CBTRN03C |
| **CVTRA04Y** (Tran Cat) | CBTRN03C |
| **CVEXPORT** (Export Rec) | CBEXPORT, CBIMPORT |
| **COMEN02Y** (Menu Opts) | COMEN01C |
| **COADM02Y** (Admin Opts) | COADM01C |
| **CSLKPCDY** (Validation) | COACTUPC |
| **CSUTLDWY** (Date Edit) | COACTUPC |
| **CODATECN** (Date Conv) | CBACT01C |
| **COSTM01** (Statement) | CBSTM03A |
| **CUSTREC** (Customer Alt) | CBSTM03A |
| **CVTRA07Y** (Report Hdr) | CBTRN03C |
| **CSMSG02Y** (Ext Msgs) | COACTVWC, COCRDSLC, COCRDUPC |

---

## 4. Data Lineage — VSAM File Access

### 4.1 Online (CICS) File Access

| VSAM File (CICS Name) | Operation | Programs |
|:-----------------------|:----------|:---------|
| **USRSEC** (User Security) | READ | COSGN00C |
| | READ / BROWSE | COUSR00C |
| | WRITE | COUSR01C |
| | READ / REWRITE | COUSR02C |
| | READ / DELETE | COUSR03C |
| **ACCTDAT** (Account) | READ | COACTVWC, COBIL00C, COTRN02C |
| | READ / REWRITE | COACTUPC, COBIL00C |
| **CARDDAT** (Card) | READ / BROWSE | COCRDLIC |
| | READ | COCRDSLC, COCRDUPC, COACTVWC |
| | READ / REWRITE | COCRDUPC |
| **CARDAIX** (Card AIX by Acct) | READ (via AIX) | COACTVWC, COACTUPC |
| **CUSTDAT** (Customer) | READ | COACTVWC |
| | READ / REWRITE | COACTUPC |
| **TRANSACT** (Transaction) | READ / BROWSE | COTRN00C, COTRN01C, COBIL00C |
| | READ / WRITE | COTRN02C |
| | BROWSE (STARTBR) | COBIL00C |
| **XREFDAT** (Cross-Reference) | READ | COBIL00C, COTRN02C |
| **JOBS** (TDQ) | WRITEQ TD | CORPT00C (submits batch report) |

### 4.2 Batch File Access

| Dataset | Mode | Programs / JCL Jobs | Description |
|:--------|:-----|:--------------------|:------------|
| `ACCTDATA.VSAM.KSDS` | Input / I-O | CBACT01C (READACCT), CBACT04C (INTCALC), CBTRN02C (POSTTRAN), CBSTM03A (CREASTMT) | Account master |
| `ACCTDATA.PS` | Input | ACCTFILE (IDCAMS load) | Flat file seed data |
| `CARDDATA.VSAM.KSDS` | Input | CBACT02C (READCARD) | Card master |
| `CARDDATA.PS` | Input | CARDFILE (IDCAMS load) | Flat file seed data |
| `CUSTDATA.VSAM.KSDS` | Input | CBCUS01C (READCUST), CBSTM03A (CREASTMT), CBTRN01C | Customer master |
| `CUSTDATA.PS` | Input | CUSTFILE (IDCAMS load) | Flat file seed data |
| `CARDXREF.VSAM.KSDS` | Input | CBACT03C (READXREF), CBACT04C (INTCALC), CBTRN01C, CBTRN02C (POSTTRAN), CBSTM03A, CBTRN03C | Cross-reference |
| `CARDXREF.PS` | Input | XREFFILE (IDCAMS load) | Flat file seed data |
| `DALYTRAN.PS` | Input | CBTRN01C, CBTRN02C (POSTTRAN) | Daily transactions for posting |
| `DALYTRAN.PS.INIT` | Input | TRANFILE (IDCAMS) | Transaction initialization record |
| `TRANSACT.VSAM.KSDS` | Output / I-O | CBTRN02C (POSTTRAN), CBACT04C (INTCALC), CBTRN03C (TRANREPT), CBSTM03A (CREASTMT) | Posted transactions |
| `TCATBALF.VSAM.KSDS` | Input / I-O | CBACT04C (INTCALC), CBTRN02C (POSTTRAN) | Category balance |
| `DISCGRP.VSAM.KSDS` | Input | CBACT04C (INTCALC) | Disclosure group / interest rates |
| `TRANTYPE.VSAM.KSDS` | Input | CBTRN03C (TRANREPT) | Transaction types |
| `TRANCATG.VSAM.KSDS` | Input | CBTRN03C (TRANREPT) | Transaction categories |
| `USRSEC.PS` | Input | DUSRSECJ (IEBGENER load) | User security seed data |
| `DALYREJS(+n)` | Output | CBTRN02C (POSTTRAN) | Rejected transactions (GDG) |
| `DATEPARM` | Input | CBTRN03C (TRANREPT) | Date parameter for reports |
| `EXPORT.DATA` | Output / Input | CBEXPORT / CBIMPORT | Branch migration data |

---

## 5. Data Flow Diagram — Daily Batch Cycle

```
  ┌──────────────┐          ┌──────────────────────┐
  │  DALYTRAN.PS │          │   CLOSEFIL (SDSF)    │
  │ (Daily Input)│          │ Close VSAM in CICS   │
  └──────┬───────┘          └──────────┬───────────┘
         │                             │
         │                             ▼
         │                  ┌──────────────────────┐
         │                  │   TRANBKP (IDCAMS)   │
         │                  │ Backup TRANSACT to    │
         │                  │ GDG generation        │
         │                  └──────────┬───────────┘
         │                             │
         ▼                             ▼
  ┌──────────────┐          ┌──────────────────────┐
  │ CBTRN02C     │◄─────────│   POSTTRAN (JCL)     │
  │ (Post Trans) │          └──────────────────────┘
  └──────┬───────┘
         │
    ┌────┴────────────────┐
    │                     │
    ▼                     ▼
┌──────────┐       ┌───────────┐
│TRANSACT  │       │ DALYREJS  │
│VSAM KSDS │       │  GDG(+1)  │
│(Posted)  │       │(Rejects)  │
└──────────┘       └───────────┘
    │
    ▼
┌──────────────────────────────┐
│   WAITSTEP → OPENFIL         │
│  (Timer wait, reopen CICS)   │
└──────────────────────────────┘
```

---

## 6. Data Flow Diagram — Monthly Interest Cycle

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  ACCTDATA    │   │   DISCGRP    │   │   TCATBALF   │
│  VSAM KSDS   │   │  VSAM KSDS   │   │  VSAM KSDS   │
│  (Accounts)  │   │(Int. Rates)  │   │(Cat Balance) │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                  │                   │
       └─────────┬────────┘                   │
                 │                            │
                 ▼                            │
         ┌───────────────┐                    │
         │   CBACT04C    │◄───────────────────┘
         │   (INTCALC)   │
         └───────┬───────┘
                 │
          ┌──────┴──────┐
          ▼             ▼
   ┌──────────┐   ┌──────────┐
   │ ACCTDATA │   │TRANSACT  │
   │ (Updated │   │(Interest │
   │ Balances)│   │ Entries) │
   └──────────┘   └──────────┘
                       │
                       ▼
                ┌──────────────┐
                │   COMBTRAN   │
                │   (SORT)     │◄── System + Daily transactions
                └──────┬───────┘
                       ▼
                ┌──────────────┐
                │  CBSTM03A    │ → CBSTM03B (file I/O)
                │  (CREASTMT)  │
                └──────┬───────┘
                       ▼
                ┌──────────────┐
                │  Statements  │
                │  (Output)    │
                └──────────────┘
```

---

## 7. Control-M Scheduler Workflows

### 7.1 DAILY-TransactionBackup
**Schedule**: Every day
**Sequence**: `CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL`

### 7.2 WEEKLY-TransactionTypesDBRefresh
**Schedule**: Every Saturday
**Sequence**: `MNTTRDB2` → Smart Folder splits into:
- **DisclosureGroupsRefresh**: `CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL`
- **TransactionTypesDBRefresh**: `TRANEXTR`

### 7.3 MONTHLY-InterestCalculation
**Schedule**: Monthly (all months enabled)
**Sequence**: `CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL`

---

## 8. Cross-Module Integration Points

| Integration | Source | Target | Mechanism | Data |
|:------------|:-------|:-------|:----------|:-----|
| Online → Batch Report | CORPT00C | TRANREPT (CBTRN03C) | TDQ WRITEQ (JOBS queue) | Report parameters → Internal Reader |
| MQ Auth Request | External | COPAUA0C | MQ GET (trigger) | Authorization request message |
| MQ Auth Response | COPAUA0C | External | MQ PUT | Authorization response message |
| MQ Date Inquiry | CODATE01 | External | MQ GET/PUT | System date request/response |
| MQ Account Inquiry | COACCT01 | External | MQ GET/PUT | Account details request/response |
| IMS Read | COPAUS0C, COPAUS1C | IMS DB (DBPAUTP0) | DL/I GU/GN calls | Authorization records |
| IMS Write | COPAUA0C | IMS DB (DBPAUTP0) | DL/I ISRT/REPL | New/updated authorizations |
| DB2 Read/Write | COTRTLIC, COTRTUPC | TRNTYPE, TRNTYCAT | Embedded SQL | Transaction type reference data |
| DB2 Extract | TRANEXTR (DSNTIAUL) | VSAM flat files | DB2 UNLOAD | Refresh VSAM from DB2 tables |
| Branch Export | CBEXPORT | EXPORT.DATA | Sequential WRITE | Customer + Account + Card + Transaction |
| Branch Import | CBIMPORT | Normalized files | Sequential READ/WRITE | Split export into entity files |
