# CardDemo Dependency Map

> **System**: CardDemo – Mainframe Credit Card Management System  
> **Generated**: 2026-05-28  
> **Scope**: Program call graph, CICS navigation flow, data lineage, copybook usage, and batch job file I/O

---

## 1 — CICS Online Navigation Graph

The sign-on screen is the entry point. From there, users navigate through menus to functional programs. All online programs communicate via the COMMAREA (`COCOM01Y`), and navigation uses `EXEC CICS XCTL`.

```
                        ┌─────────────────┐
           CC00 ───────▶│   COSGN00C      │
                        │   Sign-on       │
                        └────┬───────┬────┘
                  Admin      │       │     User
              ┌──────────────┘       └──────────────┐
              ▼                                      ▼
     ┌────────────────┐                    ┌────────────────┐
     │  COADM01C      │                    │  COMEN01C      │
     │  Admin Menu    │                    │  Main Menu     │
     │  CA00          │                    │  CM00          │
     └──┬──┬──┬──┬────┘                    └──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┐
        │  │  │  │                            │  │  │  │  │  │  │  │  │  │  │
        │  │  │  │  ┌─────────────────────────┘  │  │  │  │  │  │  │  │  │  │
        │  │  │  │  │  ┌─────────────────────────┘  │  │  │  │  │  │  │  │  │
        │  │  │  │  │  │  ┌─────────────────────────┘  │  │  │  │  │  │  │  │
        ▼  │  │  │  ▼  ▼  ▼                            │  │  │  │  │  │  │  │
  ┌──────┐ │  │  │  COACTVWC COACTUPC COCRDLIC         │  │  │  │  │  │  │  │
  │USR00C│ │  │  │  (View)   (Update)  (List)          │  │  │  │  │  │  │  │
  │(List)│ │  │  │  CAVW     CAUP      CCLI            │  │  │  │  │  │  │  │
  └──────┘ │  │  │                 │                    │  │  │  │  │  │  │  │
           ▼  │  │                 ▼                    │  │  │  │  │  │  │  │
     USR01C   │  │          ┌──────────┐               │  │  │  │  │  │  │  │
     (Add)    │  │          │ COCRDSLC │               │  │  │  │  │  │  │  │
     CU01     │  │          │ (Detail) │               │  │  │  │  │  │  │  │
              ▼  │          │ CCDL     │               │  │  │  │  │  │  │  │
        USR02C   │          └────┬─────┘               │  │  │  │  │  │  │  │
        (Update) │               ▼                     │  │  │  │  │  │  │  │
        CU02     │          COCRDUPC                   │  │  │  │  │  │  │  │
                 ▼          (Update)                   │  │  │  │  │  │  │  │
           USR03C           CCUP                       │  │  │  │  │  │  │  │
           (Delete)                                    │  │  │  │  │  │  │  │
           CU03                                        │  │  │  │  │  │  │  │
                                                       ▼  ▼  ▼  │  │  │  │  │
                                                   COTRN00C      │  │  │  │  │
                                                   COTRN01C      │  │  │  │  │
                                                   COTRN02C      │  │  │  │  │
                                                   (List/View/Add)│  │  │  │
                                                   CT00/01/02    │  │  │  │
                                                                  ▼  │  │  │
                                                             CORPT00C│  │  │
                                                             (Reports)│  │  │
                                                             CR00    │  │  │
                                                                     ▼  │  │
                                                                COBIL00C│  │
                                                                (Bill)  │  │
                                                                CB00    │  │
                                                                        ▼  │
                                                                  COPAUS0C │
                                                                  (Auth   ▼
                                                                  Summary)
                                                                  CPVS  COPAUS1C
                                                                        (Auth
                                                                        Detail)
                                                                        CPVD
```

### Admin Menu Sub-Navigation (from `COADM01C`)

| Option | Target Program | Function |
|-------:|----------------|----------|
| 1 | COUSR00C | User List |
| 2 | COUSR01C | User Add |
| 3 | COUSR02C | User Update |
| 4 | COUSR03C | User Delete |
| 5 | COTRTLIC | Transaction Type List (DB2) |
| 6 | COTRTUPC | Transaction Type Maintenance (DB2) |

### Main Menu Sub-Navigation (from `COMEN01C`)

| Option | Target Program | Function |
|-------:|----------------|----------|
| 1 | COACTVWC | Account View |
| 2 | COACTUPC | Account Update |
| 3 | COCRDLIC | Credit Card List |
| 4 | COCRDSLC | Credit Card View |
| 5 | COCRDUPC | Credit Card Update |
| 6 | COTRN00C | Transaction List |
| 7 | COTRN01C | Transaction View |
| 8 | COTRN02C | Transaction Add |
| 9 | CORPT00C | Transaction Reports |
| 10 | COBIL00C | Bill Payment |
| 11 | COPAUS0C | Pending Authorization View |

---

## 2 — Program-to-Program Call Graph

### 2.1 CICS XCTL (Transfer Control) Calls

All online programs use `EXEC CICS XCTL` to transfer control back to the calling menu or to sub-screens. The target is stored dynamically in `CDEMO-TO-PROGRAM` (from `COCOM01Y` commarea).

| Source Program | Target | Mechanism | Context |
|---------------|--------|-----------|---------|
| COSGN00C | COMEN01C or COADM01C | XCTL | Routes user to User Menu or Admin Menu based on SEC-USR-TYPE |
| COMEN01C | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | XCTL | Dynamic dispatch via CDEMO-MENU-OPT-PGMNAME table |
| COADM01C | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | XCTL | Dynamic dispatch via CDEMO-ADMIN-OPT-PGMNAME table |
| COACTVWC | CDEMO-TO-PROGRAM (menu) | XCTL | Return to calling menu |
| COACTUPC | CDEMO-TO-PROGRAM (menu) | XCTL | Return to calling menu |
| COCRDLIC | COCRDSLC or COCRDUPC | XCTL | Drill-down from card list to detail/update |
| COCRDSLC | CDEMO-TO-PROGRAM | XCTL | Return to card list or menu |
| COCRDUPC | CDEMO-TO-PROGRAM | XCTL | Return to card list or menu |
| COTRN00C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| COTRN01C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| COTRN02C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| CORPT00C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| COBIL00C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| COUSR00C | CDEMO-TO-PROGRAM | XCTL | Return to admin menu |
| COUSR01C | CDEMO-TO-PROGRAM | XCTL | Return to admin menu |
| COUSR02C | CDEMO-TO-PROGRAM | XCTL | Return to admin menu |
| COUSR03C | CDEMO-TO-PROGRAM | XCTL | Return to admin menu |
| COPAUS0C | CDEMO-TO-PROGRAM | XCTL | Return to menu |
| COPAUS1C | CDEMO-TO-PROGRAM | XCTL | Return to summary |
| COTRTLIC | COTRTUPC or CDEMO-TO-PROGRAM | XCTL | Edit selected type or return |
| COTRTUPC | CDEMO-TO-PROGRAM | XCTL | Return to list |

### 2.2 CICS LINK Calls

| Source Program | Target | Context |
|---------------|--------|---------|
| COPAUS1C | COPAUS2C (WS-PGM-AUTH-FRAUD) | Link to DB2 fraud insert subroutine |

### 2.3 Batch CALL Statements

| Source Program | Target | Mechanism | Purpose |
|---------------|--------|-----------|---------|
| CBACT01C | COBDATFT (ASM) | CALL | Date format conversion |
| CBACT01C | CEE3ABD | CALL | Abnormal termination handler |
| CBACT02C | CEE3ABD | CALL | Abnormal termination handler |
| CBACT03C | CEE3ABD | CALL | Abnormal termination handler |
| CBACT04C | CEE3ABD | CALL | Abnormal termination handler |
| CBCUS01C | CEE3ABD | CALL | Abnormal termination handler |
| CBTRN01C | CEE3ABD | CALL | Abnormal termination handler |
| CBTRN02C | CEE3ABD | CALL | Abnormal termination handler |
| CBTRN03C | CEE3ABD | CALL | Abnormal termination handler |
| CBEXPORT | CEE3ABD | CALL | Abnormal termination handler |
| CBIMPORT | CEE3ABD | CALL | Abnormal termination handler |
| CBSTM03A | CBSTM03B | CALL | Statement file I/O subroutine (called 13 times) |
| CBSTM03A | CEE3ABD | CALL | Abnormal termination handler |
| COTRN02C | CSUTLDTC | CALL | Date validation utility |
| CSUTLDTC | CEEDAYS | CALL | LE date conversion (system) |

### 2.4 MQ API Calls

| Source Program | MQ API | Purpose |
|---------------|--------|---------|
| COPAUA0C | MQOPEN, MQGET, MQPUT1, MQCLOSE | Process auth request from MQ, send reply |
| COACCT01 | MQOPEN ×3, MQGET, MQPUT ×2, MQCLOSE ×3 | Account inquiry via MQ request/response |
| CODATE01 | MQOPEN ×3, MQGET, MQPUT ×2, MQCLOSE ×3 | Date inquiry via MQ request/response |

---

## 3 — Copybook Usage Matrix

Shows which programs include which copybooks via COPY statements.

| Copybook | Programs Using It |
|----------|------------------|
| COCOM01Y | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COSGN00C, COUSR00C–03C, COPAUS0C, COPAUS1C |
| CVACT01Y | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBSTM03A, COACTUPC, COACTVWC, COCRDLIC, COCRDUPC, COTRN02C, COPAUA0C, COPAUS0C, COACCT01 |
| CVACT02Y | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C |
| CVACT03Y | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COACTUPC, COACTVWC, COCRDLIC, COTRN02C, COPAUA0C, COPAUS0C |
| CVCUS01Y | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, CBSTM03A, COPAUA0C, COPAUS0C |
| CVTRA05Y | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C |
| CVTRA06Y | CBTRN01C, CBTRN02C, CBTRN03C |
| CSUSR01Y | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| COTTL01Y | All 17 core online programs with BMS |
| CSDAT01Y | All 17 core online programs with BMS |
| CSMSG01Y | All 17 core online programs with BMS |
| CSMSG02Y | Selected online programs |
| DFHAID | All CICS programs (IBM system copybook) |
| DFHBMSCA | All CICS programs with BMS maps (IBM system copybook) |

---

## 4 — BMS Map-to-Program Binding

Each BMS map defines one or more screen layouts. Programs send and receive these maps.

| BMS Map | SEND MAP By | RECEIVE MAP By |
|---------|-------------|----------------|
| COSGN00 | COSGN00C | COSGN00C |
| COMEN01 | COMEN01C | COMEN01C |
| COADM01 | COADM01C | COADM01C |
| COACTVW | COACTVWC | COACTVWC |
| COACTUP | COACTUPC | COACTUPC |
| COCRDLI | COCRDLIC | COCRDLIC |
| COCRDSL | COCRDSLC | COCRDSLC |
| COCRDUP | COCRDUPC | COCRDUPC |
| COTRN00 | COTRN00C | COTRN00C |
| COTRN01 | COTRN01C | COTRN01C |
| COTRN02 | COTRN02C | COTRN02C |
| CORPT00 | CORPT00C | CORPT00C |
| COBIL00 | COBIL00C | COBIL00C |
| COUSR00 | COUSR00C | COUSR00C |
| COUSR01 | COUSR01C | COUSR01C |
| COUSR02 | COUSR02C | COUSR02C |
| COUSR03 | COUSR03C | COUSR03C |
| COPAU00 | COPAUS0C | COPAUS0C |
| COPAU01 | COPAUS1C | COPAUS1C |
| COTRTLI | COTRTLIC | COTRTLIC |
| COTRTUP | COTRTUPC | COTRTUPC |

---

## 5 — VSAM File Access by Program

Shows which CICS programs perform CRUD operations on each VSAM dataset.

| VSAM Dataset | Read | Write | Rewrite | Delete | StartBr/ReadNext/Prev |
|-------------|------|-------|---------|--------|----------------------|
| **USRSEC** (User Security) | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C | COUSR03C | COUSR00C |
| **ACCTDATA** (Account) | COACTVWC, COACTUPC, COCRDLIC, COTRN02C, COPAUA0C, COPAUS0C | — | COACTUPC | — | — |
| **CARDDATA** (Card) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C | — | COCRDUPC | — | COCRDLIC |
| **CUSTDATA** (Customer) | COACTVWC, COACTUPC, COPAUA0C, COPAUS0C | — | — | — | — |
| **CARDXREF** (Cross-Ref) | COACTVWC, COACTUPC, COCRDLIC, COTRN02C, COPAUA0C, COPAUS0C | — | — | — | — |
| **TRANSACT** (Transactions) | COTRN00C, COTRN01C, COTRN02C | COTRN02C | — | — | COTRN00C |
| **TCATBALF** (Cat Balance) | COBIL00C | — | COBIL00C | — | COBIL00C |

---

## 6 — Batch Job Data Lineage

### 6.1 Data Setup Jobs (IDCAMS)

These jobs define VSAM clusters and load data from flat files.

```
AWS.M2.CARDDEMO.ACCTDATA.PS ──[ACCTFILE]──▶ ACCTDATA.VSAM.KSDS
AWS.M2.CARDDEMO.CARDDATA.PS ──[CARDFILE]──▶ CARDDATA.VSAM.KSDS
AWS.M2.CARDDEMO.CUSTDATA.PS ──[CUSTFILE]──▶ CUSTDATA.VSAM.KSDS
AWS.M2.CARDDEMO.CARDXREF.PS ──[XREFFILE]──▶ CARDXREF.VSAM.KSDS
AWS.M2.CARDDEMO.DALYTRAN.PS ──[TRANFILE]──▶ TRANSACT.VSAM.KSDS
AWS.M2.CARDDEMO.DISCGRP.PS  ──[DISCGRP] ──▶ DISCGRP.VSAM.KSDS
AWS.M2.CARDDEMO.TCATBALF.PS ──[TCATBALF]──▶ TCATBALF.VSAM.KSDS
AWS.M2.CARDDEMO.TRANCATG.PS ──[TRANCATG]──▶ TRANCATG.VSAM.KSDS
AWS.M2.CARDDEMO.TRANTYPE.PS ──[TRANTYPE]──▶ TRANTYPE.VSAM.KSDS
AWS.M2.CARDDEMO.USRSEC.PS   ──[DUSRSECJ]──▶ USRSEC.VSAM.KSDS
```

### 6.2 Daily Transaction Posting Flow

```
                 ┌──────────────┐
                 │ DALYTRAN.PS  │  (daily incoming transactions)
                 └──────┬───────┘
                        │
                  ┌─────▼──────┐
   CLOSEFIL ─────│ CBTRN01C   │  Step 1: Validate daily transactions
   (close VSAM)  │ (POSTTRAN  │     - Read DALYTRAN, CUSTOMER, XREF, CARD, ACCOUNT
                 │  step 1)   │     - Validate card, account, customer
                 └─────┬──────┘     - Write valid transactions to TRANSACT
                       │
                 ┌─────▼──────┐
                 │ CBTRN02C   │  Step 2: Post transactions & update balances
                 │ (POSTTRAN  │     - Read TRANSACT, XREF, ACCOUNT
                 │  step 2)   │     - Update TCATBALF, DISCGRP balances
                 └─────┬──────┘     - Write to DALYTRAN (reject file)
                       │
                 ┌─────▼──────┐
                 │ TRANBKP    │  Step 3: Backup transaction file (GDG)
                 │ (IDCAMS)   │     TRANSACT.VSAM.KSDS → TRANSACT.BKUP(+1)
                 └─────┬──────┘
                       │
                 ┌─────▼──────┐
                 │ OPENFIL    │  Step 4: Reopen VSAM for CICS
                 └────────────┘
```

### 6.3 Monthly Interest Calculation Flow

```
   CLOSEFIL ──▶ CBACT04C (INTCALC)
                   │  Reads: TCATBALF, XREF, ACCOUNT, DISCGRP, TRANSACT
                   │  Calculates interest per account per category
                   │  Writes: interest transactions to TRANSACT
                   ▼
               SORT (COMBTRAN)
                   │  Combines TRANSACT.VSAM + DALYTRAN.PS
                   │  Merged output → new TRANSACT
                   ▼
               WAITSTEP ──▶ OPENFIL
```

### 6.4 Statement Generation Flow

```
   CBSTM03A ──▶ CBSTM03B (subroutine, called 13×)
      │              │
      │              │  Reads: TRNXFILE (transactions), XREFFILE, CUSTFILE, ACCTFILE
      │              │
      ▼              ▼
   STMTFILE      HTMLFILE
   (plain text)  (HTML statements)
```

### 6.5 Transaction Report Flow (via `TRANREPT.prc`)

```
   TRANSACT.VSAM.KSDS ──[REPROC unload]──▶ TRANSACT.BKUP(+1)
         │
         ▼
   SORT ──[filter by date, sort by card]──▶ sorted temp file
         │
         ▼
   CBTRN03C ──[format report]──▶ Report output (SYSOUT)
         │
         │  Reads: TRANTYPE, TRANCATG (for descriptions)
         └──────────────────────────────────
```

### 6.6 Branch Migration (Export / Import)

```
   EXPORT:
   CUSTFILE  ──┐
   ACCTFILE  ──┤
   XREFFILE  ──┼──[CBEXPORT]──▶ EXPFILE (single composite sequential file)
   TRANSACT  ──┤                  (uses CVEXPORT multi-record layout)
   CARDFILE  ──┘

   IMPORT:
   EXPFILE ──[CBIMPORT]──▶ CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT
                            (+ ERROUT for invalid records)
```

### 6.7 Authorization Flow (IMS-DB2-MQ)

```
   MQ Request Queue ──[COPAUA0C trigger]──▶ Read from MQ
         │
         │  Reads: CARDXREF (VSAM), ACCTDATA (VSAM), CUSTDATA (VSAM)
         │  Reads/Writes: IMS DB (CIPAUSMY summary, CIPAUDTY detail)
         │
         ▼
   MQ Reply Queue ◀──[COPAUA0C]──── Authorization decision

   Online:
   COPAUS0C ── Reads IMS (summary + detail), displays on COPAU00 map
   COPAUS1C ── Reads IMS detail, updates IMS, LINKs to COPAUS2C
   COPAUS2C ── INSERT to DB2 AUTHFRDS table (fraud record)

   Batch:
   CBPAUP0C ── Purges expired IMS segments
```

---

## 7 — Control-M Scheduler Dependency Chains

### 7.1 DAILY-TransactionBackup (runs every day)

```
CLOSEFIL ──▶ TRANBKP ──▶ WAITSTEP ──▶ OPENFIL
```

### 7.2 WEEKLY-TransactionTypesDBRefresh (runs weekly)

```
MNTTRDB2  (standalone – runs COBTUPDT to maintain DB2 tran type table)
```

### 7.3 MONTHLY-InterestCalculation (runs 1st of month)

```
CLOSEFIL ──▶ INTCALC ──▶ COMBTRAN ──▶ WAITSTEP ──▶ OPENFIL
```

---

## 8 — DB2 Access Map (Optional Modules)

| Program | DB2 Table | Operations | SQL Patterns |
|---------|----------|------------|-------------|
| COTRTLIC | TRTYP (Tran Type) | SELECT, UPDATE, DELETE | Cursor-based listing, positioned update/delete |
| COTRTUPC | TRTYP, TRCAT | SELECT, INSERT, UPDATE | Single-row fetch, insert, update |
| COPAUS2C | AUTHFRDS (Auth Fraud) | INSERT, SELECT | Insert fraud record, query by key |
| COBTUPDT | TRTYP | INSERT, UPDATE, DELETE | Batch maintenance of tran types |
| CREADB21 | TRTYP, TRCAT | DDL + INSERT | Create tables and load initial data |
| TRANEXTR | TRTYP, TRCAT | UNLOAD (DSNTIAUL) | Extract DB2 data to flat files |

---

## 9 — Technology Dependency Summary

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CardDemo System                              │
├──────────┬──────────┬───────────┬──────────┬────────┬──────────────┤
│  COBOL   │   CICS   │   VSAM    │   JCL    │  ASM   │  Scheduler   │
│ 44 pgms  │ 25 txns  │ 10 KSDS  │ 46 jobs  │ 2 mods │  Control-M   │
├──────────┴──────────┴───────────┴──────────┴────────┴──────────────┤
│                     Optional Extensions                             │
├──────────┬──────────┬───────────┬──────────────────────────────────┤
│  DB2     │  IMS DB  │  IBM MQ   │  JCL Utilities                   │
│ 2 tables │ 2 DBDs   │ 3 queues  │  FTP, TXT2PDF, INTRDR            │
│ 6 pgms   │ 5 pgms   │ 3 pgms   │  4 jobs                          │
└──────────┴──────────┴───────────┴──────────────────────────────────┘
```
