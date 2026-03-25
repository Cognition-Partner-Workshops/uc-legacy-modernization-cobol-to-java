# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
>
> This document maps the call graph (which programs call which) and data lineage
> (which jobs read/write which files) across the entire CardDemo application.

---

## 1. Online CICS Program Call Graph

### 1.1 Navigation Flow

```
                              ┌─────────────┐
                              │  COSGN00C   │
                              │  (Sign On)  │
                              │  Tran: CC00 │
                              └──────┬──────┘
                                     │ XCTL (on successful login)
                              ┌──────┴──────┐
                   ┌──────────│  COMEN01C   │──────────┐
                   │          │ (Main Menu) │          │
                   │          │  Tran: CM00 │          │
                   │          └──────┬──────┘          │
                   │                 │                  │
          (User Options)      (Admin Option)    (Back to Sign On)
        ┌────┬────┬────┐           │
        │    │    │    │    ┌──────┴──────┐
        │    │    │    │    │  COADM01C   │
        │    │    │    │    │(Admin Menu) │
        │    │    │    │    │  Tran: CA00 │
        │    │    │    │    └──────┬──────┘
        │    │    │    │           │
        │    │    │    │    ┌──────┴──────┐
        │    │    │    │    │  COUSR00C   │──→ COUSR01C (Add)
        │    │    │    │    │ (User List) │──→ COUSR02C (Update)
        │    │    │    │    │  Tran: CU00 │──→ COUSR03C (Delete)
        │    │    │    │    └─────────────┘
        │    │    │    │
        │    │    │    └──→ COBIL00C  (Bill Payment, Tran: CB00)
        │    │    │
        │    │    └───────→ CORPT00C  (Reports, Tran: CR00)
        │    │
        │    └────────────→ COTRN00C  (Trans List, Tran: CT00)
        │                       │
        │                       ├──→ COTRN01C (Trans View, Tran: CT01)
        │                       └──→ COTRN02C (Trans Add, Tran: CT02)
        │
        └─────────────────→ COCRDLIC  (Card List, Tran: CC01)
                                │
                                ├──→ COCRDSLC (Card View, Tran: CC02)
                                └──→ COCRDUPC (Card Update, Tran: CC03)
                                         │
                                  ┌──────┴──────┐
                                  │  COACTVWC   │ (Account View, Tran: CA01)
                                  │  COACTUPC   │ (Account Update, Tran: CA02)
                                  └─────────────┘
```

### 1.2 Program-to-Program Transfer Details

All online navigation uses **EXEC CICS XCTL** (transfer control — no return) via `CDEMO-TO-PROGRAM` in the COMMAREA.

| Source Program | Target Program(s) | Transfer Mechanism |
|---|---|---|
| COSGN00C | COMEN01C or COADM01C | XCTL based on user type (A → Admin, U → User menu) |
| COMEN01C | COACTVWC, COCRDLIC, COTRN00C, COTRN02C, COBIL00C, CORPT00C, COADM01C | XCTL via `CDEMO-MENU-OPT-PGMNAME(WS-OPTION)` table |
| COADM01C | COUSR00C (+ others via option table) | XCTL via `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)` |
| COUSR00C | COUSR01C, COUSR02C, COUSR03C | XCTL based on user selection (Add/Update/Delete) |
| COCRDLIC | COCRDSLC, COCRDUPC | XCTL based on selection (View/Update) |
| COTRN00C | COTRN01C | XCTL to view transaction detail |
| All programs | COMEN01C or COADM01C | XCTL back (F3 = return to calling menu) |

### 1.3 Subroutine CALL Dependencies (COBOL CALL)

| Caller | Callee | Purpose |
|--------|--------|---------|
| COTRN02C | CSUTLDTC | Date validation (transaction date entry) |
| CORPT00C | CSUTLDTC | Date validation (report date range) |
| COBSWAIT | MVSWAIT (ASM) | Mainframe sleep/wait |
| CBACT01C | COBDATFT (ASM) | Date formatting for display |
| CBSTM03A | CBSTM03B | File I/O subroutine (open/read/close VSAM files) |
| CSUTLDTC | CEEDAYS (LE) | Language Environment date conversion service |
| All batch `CB*` | CEE3ABD (LE) | Language Environment abnormal termination |

---

## 2. Copybook Inclusion Map

### 2.1 Which Programs Include Which Copybooks

| Copybook | Included By (Programs) |
|----------|----------------------|
| **COCOM01Y** (COMMAREA) | All 17 online CICS programs |
| **COTTL01Y** (Title/Header) | All 17 online CICS programs |
| **CSDAT01Y** (Date format) | Most online CICS programs |
| **CSMSG01Y** (Messages) | Most online CICS programs |
| **CSMSG02Y** (Ext Messages) | COTRN00C, COUSR00C, COCRDLIC |
| **CSSETATY** (Set Attributes) | COTRN00C, COUSR00C, COCRDLIC |
| **CSSTRPFY** (Store PFKey) | COACTUPC, COACTVWC, COCRDUPC, COCRDSLC, COCRDLIC |
| **CSUSR01Y** (User Security) | COSGN00C, COUSR00C-03C |
| **CVACT01Y** (Account) | CBACT01C, CBACT02C, CBTRN01C, CBTRN02C, CBACT04C, COACTUPC, COACTVWC |
| **CVACT02Y** (Card) | CBACT03C, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC |
| **CVACT03Y** (XREF) | CBACT01C, CBACT03C, CBTRN01C, CBTRN02C, CBACT04C |
| **CVCUS01Y** (Customer) | CBCUS01C, CBTRN01C, COACTVWC, COACTUPC |
| **CVTRA05Y** (Transaction) | CBTRN01C, CBTRN02C, COTRN00C, COTRN01C, COTRN02C |
| **CVTRA06Y** (Daily Trans) | CBTRN01C, CBTRN02C, CBTRN03C |
| **CVTRA01Y** (Cat Balance) | CBACT04C, CBTRN02C |
| **CVTRA02Y** (Disc Group) | CBACT04C |
| **CVTRA03Y** (Tran Type) | CBTRN03C |
| **CVTRA04Y** (Tran Category) | CBTRN03C |
| **CVTRA07Y** (Report struct) | CBTRN03C |
| **CODATECN** (Date convert) | CBACT01C |
| **COSTM01** (Trnx record) | CBSTM03A |
| **COMEN02Y** (Menu defs) | COMEN01C |
| **COADM02Y** (Admin defs) | COADM01C |
| **CSUTLDPY** (Date util params) | CSUTLDTC, COTRN02C, CORPT00C |
| **CSUTLDWY** (Date util WS) | CSUTLDTC |
| **CVCRD01Y** (Card detail) | COCRDLIC, COCRDSLC, COCRDUPC |
| **CVEXPORT** (Export layout) | CBEXPORT, CBIMPORT |
| **CSLKPCDY** (Lookup codes) | COACTUPC |

---

## 3. VSAM File Access by Online Programs

| VSAM File (DD Name) | Programs That Access It | Operations |
|---|---|---|
| **USRSEC** (User Security) | COSGN00C, COUSR00C-03C | READ, WRITE, REWRITE, DELETE, STARTBR, READNEXT |
| **ACCTDAT** (Account) | COACTVWC, COACTUPC, COBIL00C, COTRN02C | READ, REWRITE |
| **CARDDAT** (Card) | COCRDLIC, COCRDSLC, COCRDUPC | READ, REWRITE, STARTBR, READNEXT, READPREV |
| **CARDXREF** (XREF) | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC | READ, STARTBR, READNEXT |
| **TRANSACT** (Transaction) | COTRN00C, COTRN01C, COTRN02C, COBIL00C | READ, WRITE, STARTBR, READNEXT, READPREV |
| **CUSTDAT** (Customer) | COACTVWC, COACTUPC | READ |
| **CXREF** (XREF AIX Path) | COCRDLIC | READ via AIX path (by Account ID) |

---

## 4. Batch Job Data Lineage

### 4.1 Batch Cycle Order (Nightly Processing)

```
Step 1: CLOSEFIL ──→ Close all CICS-managed VSAM files
                      │
Step 2: Data Refresh ─┤──→ ACCTFILE  (reload Account VSAM)
                      ├──→ CARDFILE  (reload Card VSAM)
                      ├──→ CUSTFILE  (reload Customer VSAM)
                      ├──→ XREFFILE  (reload XREF VSAM)
                      └──→ TRANFILE  (reload Transaction VSAM)
                      │
Step 3: POSTTRAN ────→ Post daily transactions to master
                      │
Step 4: INTCALC ─────→ Calculate interest on accounts
                      │
Step 5: TRANBKP ─────→ Backup transaction file to GDG
                      │
Step 6: COMBTRAN ────→ Combine backup + system transactions
                      │
Step 7: CREASTMT ────→ Produce customer statements
                      │
Step 8: TRANREPT ────→ Generate transaction reports
                      │
Step 9: TRANIDX ─────→ Rebuild alternate indexes
                      │
Step 10: OPENFIL ────→ Reopen CICS files
```

### 4.2 Job-Level File Read/Write Matrix

| JCL Job | Program | Reads (Input) | Writes (Output) |
|---|---|---|---|
| **POSTTRAN** | CBTRN02C | DALYTRAN (daily trans), CARDXREF, ACCTDATA, TRANSACT, CARDDATA, TCATBALF, TRANTYPE, TRANCATG | TRANSACT (updated), ACCTDATA (updated), TCATBALF (updated) |
| **INTCALC** | CBACT04C | TCATBALF, CARDXREF (AIX), ACCTDATA, DISCGRP | SYSTRAN (+1 GDG — interest transactions) |
| **TRANBKP** | IDCAMS | TRANSACT.VSAM.KSDS | TRANSACT.BKUP (+1 GDG) |
| **COMBTRAN** | SORT, IDCAMS | TRANSACT.BKUP(0), SYSTRAN(0) | TRANSACT.COMBINED (+1 GDG), TRANSACT.VSAM.KSDS (reloaded) |
| **CREASTMT** | SORT, CBSTM03A→CBSTM03B | TRANSACT.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML |
| **TRANREPT** | SORT, CBTRN03C | TRANSACT.VSAM.KSDS, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANSACT.DALY (+1 GDG), TRANREPT (+1 GDG) |
| **CBEXPORT** | CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPORT.DATA (unified flat file) |
| **CBIMPORT** | CBIMPORT | EXPORT.DATA | CUSTDATA.IMPORT, ACCTDATA.IMPORT, CARDXREF.IMPORT, TRANSACT.IMPORT, IMPORT.ERRORS |
| **ACCTFILE** | IDCAMS | ACCTDATA.PS (flat) | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | IDCAMS | CARDDATA.PS (flat) | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | IDCAMS | CUSTDATA.PS (flat) | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | IDCAMS | CARDXREF.PS (flat) | CARDXREF.VSAM.KSDS + AIX |
| **TRANFILE** | IDCAMS | DALYTRAN.PS.INIT (flat) | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | IEBGENER, IDCAMS | USRSEC.PS (flat) | USRSEC.VSAM.KSDS |
| **PRTCATBL** | IDCAMS, SORT | TCATBALF.VSAM.KSDS | TCATBALF.REPT, TCATBALF.BKUP (+1 GDG) |
| **TXT2PDF1** | TXT2PDF | STATEMNT.PS | STATEMNT.PS.PDF |

### 4.3 GDG (Generation Data Group) Datasets

| GDG Base | Written By | Purpose |
|---|---|---|
| CARDDEMO.TRANSACT.BKUP | TRANBKP | Transaction file backup generations |
| CARDDEMO.SYSTRAN | INTCALC | System-generated interest transactions |
| CARDDEMO.TRANSACT.COMBINED | COMBTRAN | Combined transaction file |
| CARDDEMO.TRANSACT.DALY | TRANREPT | Daily transaction extract for reporting |
| CARDDEMO.TRANREPT | TRANREPT | Transaction report output |
| CARDDEMO.TCATBALF.BKUP | PRTCATBL | Category balance backup |

---

## 5. Data Flow Diagram — Transaction Lifecycle

```
 ┌────────────────────────────────────────────────────────────────────┐
 │                    ONLINE (CICS) PHASE                             │
 │                                                                    │
 │  User → COTRN02C ──WRITE──→ TRANSACT (VSAM)                      │
 │         (Add Tran)          ┌─READ──→ CARDXREF (validate card)    │
 │                             ├─READ──→ ACCTDATA (get account)      │
 │                             └─WRITE─→ TRANSACT (add new record)   │
 │                                                                    │
 │  User → COBIL00C ──WRITE──→ TRANSACT (bill payment as tran)      │
 │         (Pay Bill)          ├─READ──→ ACCTDATA                    │
 │                             └─REWRITE→ ACCTDATA (update balance)  │
 └────────────────────────────────────────────────────────────────────┘
                                    │
                              (End of Day)
                                    ▼
 ┌────────────────────────────────────────────────────────────────────┐
 │                    BATCH PHASE                                     │
 │                                                                    │
 │  1. CLOSEFIL ──→ Closes CICS files                                │
 │                                                                    │
 │  2. POSTTRAN (CBTRN02C batch)                                     │
 │     DALYTRAN ──READ──→ Validate via CARDXREF                      │
 │                       → Update ACCTDATA balances                   │
 │                       → Update TCATBALF (category balances)        │
 │                       → WRITE to TRANSACT (master)                 │
 │                                                                    │
 │  3. INTCALC (CBACT04C)                                            │
 │     TCATBALF ──READ──→ For each category balance                  │
 │     DISCGRP  ──READ──→ Look up interest rate                      │
 │     ACCTDATA ──READ──→ Get account group                          │
 │                       → Calculate interest                         │
 │                       → WRITE to SYSTRAN (GDG)                     │
 │                                                                    │
 │  4. TRANBKP ──→ TRANSACT → TRANSACT.BKUP (GDG)                   │
 │                                                                    │
 │  5. COMBTRAN ──→ TRANSACT.BKUP + SYSTRAN → TRANSACT (reloaded)   │
 │                                                                    │
 │  6. CREASTMT (CBSTM03A → CBSTM03B)                               │
 │     TRANSACT + CARDXREF + ACCTDATA + CUSTDATA                     │
 │     ──→ STATEMNT.PS (text) + STATEMNT.HTML                        │
 │                                                                    │
 │  7. TRANREPT (CBTRN03C)                                           │
 │     TRANSACT + CARDXREF + TRANTYPE + TRANCATG                     │
 │     ──→ TRANREPT (GDG report output)                              │
 │                                                                    │
 │  8. OPENFIL ──→ Reopens CICS files                                │
 └────────────────────────────────────────────────────────────────────┘
```

---

## 6. Optional Module Dependencies

### 6.1 Authorization Module (IMS/DB2/MQ)

```
MQ Queue (CDPA) ──trigger──→ COPAUA0C ──→ IMS DB (PAUTSUM0/PAUTDTL1)
                                         ──→ DB2 (fraud flag update via COPAUS2C)

COPAUS0C ──READ──→ IMS DB (summary segments)
COPAUS1C ──READ──→ IMS DB (detail segments)
COPAUS2C ──UPDATE──→ DB2 (CARDDEMO.PENDING_AUTH)

CBPAUP0C (batch) ──DELETE──→ IMS DB (expired authorizations)

PAUDBUNL (batch) ──READ──→ IMS DB ──WRITE──→ Flat files (OPFILE1, OPFILE2)
PAUDBLOD (batch) ──READ──→ Flat file ──WRITE──→ IMS DB
DBUNLDGS (batch) ──READ──→ IMS DB ──WRITE──→ GSAM file
```

### 6.2 Transaction Type DB2 Module

```
COTRTLIC (online) ──SELECT──→ DB2 CARDDEMO.TRANSACTION_TYPE (cursor-based list)
COTRTUPC (online) ──SELECT/INSERT/UPDATE/DELETE──→ DB2 CARDDEMO.TRANSACTION_TYPE
COBTUPDT (batch)  ──UPDATE──→ DB2 CARDDEMO.TRANSACTION_TYPE
```

### 6.3 VSAM-MQ Module

```
MQ Queue (CDRD) ──trigger──→ CODATE01 ──→ Returns system date/time via MQ reply
MQ Queue (CDRA) ──trigger──→ COACCT01 ──READ──→ ACCTDATA (VSAM) ──→ MQ reply
```

---

## 7. External Interface Points

| Interface | Protocol | Programs | Direction | Description |
|-----------|----------|----------|-----------|-------------|
| 3270 Terminal | CICS BMS | All CO* programs | In/Out | User interaction via terminal screens |
| MQ Series | MQ API | COPAUA0C, CODATE01, COACCT01 | In/Out | Message-based integration |
| IMS DB | DL/I | PAUDBUNL, PAUDBLOD, DBUNLDGS, COPAUS0C-2C | In/Out | Hierarchical database access |
| DB2 | Embedded SQL | COTRTLIC, COTRTUPC, COBTUPDT, COPAUS2C | In/Out | Relational database access |
| FTP | TCP/IP | FTPJCL.JCL | Out | File transfer to/from mainframe |
| VSAM Files | File I/O | All batch and online programs | In/Out | Primary data storage |
| GDG Datasets | Sequential | Batch jobs | Out | Generational backup/archive |
| Spool (SYSOUT) | JES | Batch jobs | Out | Report and log output |
