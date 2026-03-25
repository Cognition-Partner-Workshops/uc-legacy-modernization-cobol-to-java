# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Scope:** Call Graph, Data Lineage, COPY Dependencies

---

## 1. Program Call Graph

### 1.1 Online CICS Navigation Flow (EXEC CICS XCTL / RETURN)

```
                          ┌──────────────┐
                          │  COSGN00C    │
                          │  (Sign-on)   │
                          └──────┬───────┘
                                 │ XCTL (based on SEC-USR-TYPE)
                    ┌────────────┴────────────┐
                    │                         │
              ┌─────▼─────┐            ┌──────▼──────┐
              │ COMEN01C  │            │  COADM01C   │
              │ (User     │            │  (Admin     │
              │  Menu)    │            │   Menu)     │
              └─────┬─────┘            └──────┬──────┘
                    │                         │
     ┌──────────────┼──────────────┐     ┌────┴────────────────┐
     │              │              │     │                     │
┌────▼───┐   ┌─────▼────┐  ┌──────▼──┐ ┌▼────────┐    ┌──────▼──┐
│Account │   │Card Mgmt │  │Trans    │ │User     │    │Trans    │
│Module  │   │Module    │  │Module   │ │Admin    │    │Type DB2 │
└────┬───┘   └────┬─────┘  └────┬────┘ └┬────────┘   └─────────┘
     │            │             │        │           (optional)
     │            │             │        │
┌────▼───┐  ┌────▼────┐  ┌────▼───┐  ┌─▼───────┐
│COACTVWC│  │COCRDLIC │  │COTRN00C│  │COUSR00C │
│(View)  │  │(List)   │  │(List)  │  │(List)   │
└────┬───┘  └────┬────┘  └────┬───┘  └──┬──────┘
     │           │            │          │
┌────▼───┐  ┌───▼─────┐ ┌───▼────┐  ┌──▼──────┐──►COUSR01C (Add)
│COACTUPC│  │COCRDSLC │ │COTRN01C│  │COUSR02C │──►COUSR03C (Delete)
│(Update)│  │(View)   │ │(View)  │  │(Update) │
└────────┘  └───┬─────┘ └───┬────┘  └─────────┘
                │            │
           ┌────▼────┐  ┌───▼────┐
           │COCRDUPC │  │COTRN02C│──►CSUTLDTC (date util)
           │(Update) │  │(Add)   │
           └─────────┘  └────────┘

Additional online paths:
  CORPT00C (Report) ──CALL──► CSUTLDTC (date validation)
  COBIL00C (Bill Pay) ── standalone, reads TRANSACT/ACCTDATA
```

### 1.2 Batch Program Call Graph

```
CBSTM03A (Statement Gen) ──CALL──► CBSTM03B (File I/O Subroutine)
                          ──CALL──► CEE3ABD  (Abend handler)

CBTRN02C (Trans Posting)  ──CALL──► CEE3ABD  (Abend handler)
CBTRN03C (Trans Report)   ──CALL──► CEE3ABD  (Abend handler)
CBACT01C (Acct Reader)    ──CALL──► COBDATFT (Date format - ASM)
                          ──CALL──► CEE3ABD  (Abend handler)
CBACT02C (Card Reader)    ──CALL──► CEE3ABD  (Abend handler)
CBACT03C (Xref Reader)    ──CALL──► CEE3ABD  (Abend handler)
CBACT04C (Interest Calc)  ──CALL──► CEE3ABD  (Abend handler)
CBCUS01C (Cust Reader)    ──CALL──► CEE3ABD  (Abend handler)
CBEXPORT (Data Export)    ──CALL──► CEE3ABD  (Abend handler)
CBIMPORT (Data Import)    ──CALL──► CEE3ABD  (Abend handler)
COBSWAIT (Wait Utility)   ──CALL──► MVSWAIT  (ASM wait routine)

CORPT00C (Online Report)  ──CALL──► CSUTLDTC (Date utility)
COTRN02C (Online Tran Add)──CALL──► CSUTLDTC (Date utility)
```

### 1.3 Complete Call Matrix

| Caller | Callee | Call Type | Purpose |
|--------|--------|-----------|---------|
| COSGN00C | COMEN01C | EXEC CICS XCTL | Route regular user to main menu |
| COSGN00C | COADM01C | EXEC CICS XCTL | Route admin user to admin menu |
| COMEN01C | (various) | EXEC CICS XCTL | Route to selected function |
| COADM01C | (various) | EXEC CICS XCTL | Route to selected admin function |
| COCRDLIC | COCRDSLC | EXEC CICS XCTL | View selected card |
| COCRDLIC | COCRDUPC | EXEC CICS XCTL | Update selected card |
| COCRDLIC | COACTVWC | EXEC CICS XCTL | View account for selected card |
| CORPT00C | CSUTLDTC | CALL | Validate report date parameters |
| COTRN02C | CSUTLDTC | CALL | Validate transaction dates |
| CBSTM03A | CBSTM03B | CALL | Delegate file I/O operations |
| CBSTM03A | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBACT01C | COBDATFT | CALL | Format dates in account records |
| CBACT01C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBACT02C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBACT03C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBACT04C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBCUS01C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBTRN01C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBTRN02C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBTRN03C | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBEXPORT | CEE3ABD | CALL | Terminate on unrecoverable error |
| CBIMPORT | CEE3ABD | CALL | Terminate on unrecoverable error |
| COBSWAIT | MVSWAIT | CALL | Assembler timed wait |

---

## 2. COPY (Copybook) Dependencies

### 2.1 Copybook Usage Matrix

| Copybook | Used By Programs | Usage Count |
|----------|-----------------|-------------|
| **COCOM01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C | **17** |
| **COTTL01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **16** |
| **CSDAT01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **16** |
| **CSMSG01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **16** |
| **DFHAID** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C | **17** |
| **DFHBMSCA** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C | **17** |
| **CSUSR01Y** | COSGN00C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COMEN01C, COADM01C | **12** |
| **CSMSG02Y** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | **4** |
| **CVACT01Y** | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A | **11** |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT | **8** |
| **CVACT03Y** | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A | **12** |
| **CVCUS01Y** | COCRDSLC, COCRDUPC, COACTVWC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | **7** |
| **CVCRD01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | **5** |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | **11** |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | **2** |
| **CVTRA01Y** | CBACT04C, CBTRN02C | **2** |
| **CVTRA02Y** | CBACT04C | **1** |
| **CVTRA03Y** | CBTRN03C | **1** |
| **CVTRA04Y** | CBTRN03C | **1** |
| **CVTRA07Y** | CBTRN03C | **1** |
| **CVEXPORT** | CBEXPORT, CBIMPORT | **2** |
| **COSTM01** | CBSTM03A | **1** |
| **CUSTREC** | CBSTM03A | **1** |
| **CSSETATY** | COACTUPC (x38 via REPLACING) | **1** (38 copies) |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | **5** |
| **CSUTLDPY** | COACTUPC | **1** |
| **CSUTLDWY** | COACTUPC | **1** |
| **CSLKPCDY** | COACTUPC | **1** |
| **CODATECN** | CBACT01C | **1** |
| **COADM02Y** | COADM01C | **1** |
| **COMEN02Y** | COMEN01C | **1** |

### 2.2 Per-Program Copybook Dependencies

| Program | Copybooks Included |
|---------|-------------------|
| **COACTUPC** | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY(x38), CSSTRPFY, CSUTLDPY |
| **COACTVWC** | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| **COCRDLIC** | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| **COCRDSLC** | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| **COCRDUPC** | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| **COTRN02C** | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| **COBIL00C** | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| **CBTRN02C** | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **CBACT04C** | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **CBTRN03C** | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **CBSTM03A** | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **CBEXPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| **CBIMPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |

---

## 3. VSAM File Data Lineage

### 3.1 File Access by Program

| VSAM Dataset | Programs That READ | Programs That WRITE/UPDATE | JCL Jobs That Load |
|---|---|---|---|
| **ACCTDATA** (Account) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT | COACTUPC (REWRITE), COBIL00C (REWRITE), CBACT04C (REWRITE), CBIMPORT | ACCTFILE |
| **CARDDATA** (Card) | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT | COCRDUPC (REWRITE), CBIMPORT | CARDFILE |
| **CARDXREF** (Cross-Ref) | COACTVWC, COACTUPC, COCRDLIC, COTRN02C, COBIL00C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | CBIMPORT | XREFFILE |
| **CUSTDATA** (Customer) | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBSTM03A, CBEXPORT | CBIMPORT | CUSTFILE |
| **TRANSACT** (Transaction) | COTRN00C, COTRN01C, COBIL00C, CBTRN01C, CBTRN03C, CBEXPORT | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE), CBIMPORT | TRANFILE |
| **USRSEC** (User Security) | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) | DUSRSECJ |
| **TCATBALF** (Cat Balance) | CBACT04C, CBTRN02C | CBACT04C (REWRITE), CBTRN02C (WRITE/REWRITE) | TCATBALF |
| **TRANTYPE** (Tran Type) | CBTRN03C | -- | TRANTYPE |
| **TRANCATG** (Tran Category) | CBTRN03C | -- | TRANCATG |
| **DISCGRP** (Disclosure) | CBACT04C | -- | DISCGRP |
| **DALYTRAN** (Daily Tran) | CBTRN01C, CBTRN02C | (loaded externally) | TRANFILE |

### 3.2 File Flow Diagram

```
                    ┌──────────────────────────────────┐
                    │       DATA LOADING PHASE          │
                    │  (ACCTFILE, CARDFILE, CUSTFILE,   │
                    │   XREFFILE, TRANFILE, DUSRSECJ,  │
                    │   TRANTYPE, TRANCATG, TCATBALF,  │
                    │   DISCGRP)                        │
                    └───────────────┬──────────────────┘
                                    │ Flat files (PS) ──► VSAM (KSDS)
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                    ONLINE CICS PHASE                           │
│                                                                │
│  COSGN00C ─reads─► USRSEC                                     │
│  COACTVWC ─reads─► ACCTDATA, CARDDATA, CARDXREF, CUSTDATA     │
│  COACTUPC ─reads/writes─► ACCTDATA, CARDXREF                  │
│  COCRDLIC ─reads─► CARDDATA, CARDXREF                         │
│  COCRDSLC ─reads─► CARDDATA, CARDXREF, CUSTDATA               │
│  COCRDUPC ─reads/writes─► CARDDATA, CARDXREF, CUSTDATA        │
│  COTRN00C ─reads─► TRANSACT                                   │
│  COTRN01C ─reads─► TRANSACT                                   │
│  COTRN02C ─reads/writes─► TRANSACT, ACCTDATA, CARDXREF        │
│  COBIL00C ─reads/writes─► TRANSACT, ACCTDATA, CARDXREF        │
│  COUSR00C-03C ─reads/writes─► USRSEC                          │
│  CORPT00C ─writes─► TDQ (triggers batch report)               │
└────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                    BATCH PROCESSING PHASE                       │
│                                                                │
│  CBTRN02C: DALYTRAN ──► TRANSACT + TCATBALF + DALYREJS(GDG)   │
│  CBACT04C: TCATBALF + CARDXREF + ACCTDATA + DISCGRP           │
│            ──► ACCTDATA(updated) + SYSTRAN(GDG)                │
│  CBTRN03C: TRANSACT + CARDXREF + TRANTYPE + TRANCATG          │
│            + DATEPARM ──► TRANREPT(GDG)                        │
│  CBSTM03A: TRXFL + CARDXREF + ACCTDATA + CUSTDATA             │
│            ──► STATEMNT.PS + STATEMNT.HTML                     │
│  CBEXPORT: All VSAM files ──► Single export sequential file    │
│  CBIMPORT: Export file ──► All VSAM files                      │
└────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                    OUTPUT / REPORTING                           │
│                                                                │
│  TRANREPT(GDG)    ─ Daily transaction report                   │
│  STATEMNT.PS      ─ Account statements (text)                  │
│  STATEMNT.HTML    ─ Account statements (HTML)                  │
│  STATEMNT.PS.PDF  ─ Account statements (PDF via TXT2PDF)       │
│  DALYREJS(GDG)    ─ Rejected daily transactions                │
│  TRANSACT.BKUP    ─ Transaction backup (GDG)                   │
│  SYSTRAN(GDG)     ─ Interest calculation audit trail           │
└────────────────────────────────────────────────────────────────┘
```

---

## 4. JCL Job Dependency Chain

### 4.1 Nightly Batch Cycle Sequence

```
Step 1:  CLOSEFIL ────────────── Close CICS files for batch access
             │
Step 2:  Data Refresh (parallel) ── Reload VSAM master files
             │  ACCTFILE    (ACCTDATA)
             │  CARDFILE    (CARDDATA)
             │  CUSTFILE    (CUSTDATA)
             │  XREFFILE    (CARDXREF)
             │  TRANFILE    (TRANSACT + DALYTRAN)
             │
Step 3:  POSTTRAN ───────────── Post daily transactions
             │                   PGM=CBTRN02C
             │                   IN: DALYTRAN, CARDXREF, ACCTDATA
             │                   OUT: TRANSACT, TCATBALF, DALYREJS(GDG)
             │
Step 4:  INTCALC ────────────── Calculate interest
             │                   PGM=CBACT04C
             │                   IN: TCATBALF, CARDXREF, ACCTDATA, DISCGRP
             │                   OUT: ACCTDATA(updated), SYSTRAN(GDG)
             │
Step 5:  TRANBKP ────────────── Backup transaction master
             │                   PGM=IDCAMS REPRO
             │                   IN: TRANSACT
             │                   OUT: TRANSACT.BKUP(GDG)
             │
Step 6:  COMBTRAN ───────────── Backup reference data
             │                   PGM=IEBGENER
             │                   IN: TRANTYPE, TRANCATG, DISCGRP
             │                   OUT: GDG backups
             │
Step 7:  CREASTMT ───────────── Generate statements
             │                   PGM=SORT + CBSTM03A
             │                   IN: TRANSACT, CARDXREF, ACCTDATA, CUSTDATA
             │                   OUT: STATEMNT.PS, STATEMNT.HTML
             │
Step 8:  TRANIDX ────────────── Rebuild alternate indexes
             │                   PGM=IDCAMS
             │
Step 9:  OPENFIL ────────────── Reopen CICS files
```

### 4.2 JCL-to-Program Mapping

| JCL Job | COBOL Program(s) Executed | Utility Programs |
|---------|--------------------------|------------------|
| POSTTRAN | CBTRN02C | -- |
| INTCALC | CBACT04C | -- |
| TRANREPT | CBTRN03C | SORT |
| CREASTMT | CBSTM03A (→ CBSTM03B) | SORT, IDCAMS, IEFBR14 |
| READACCT | CBACT01C | IEFBR14 |
| READCARD | CBACT02C | -- |
| READCUST | CBCUS01C | -- |
| READXREF | CBACT03C | -- |
| CBEXPORT | CBEXPORT | -- |
| CBIMPORT | CBIMPORT | -- |
| WAITSTEP | COBSWAIT | -- |
| TXT2PDF1 | -- | IKJEFT1B (TXT2PDF) |
| ACCTFILE | -- | IDCAMS |
| CARDFILE | -- | IDCAMS |
| CUSTFILE | -- | IDCAMS |
| XREFFILE | -- | IDCAMS |
| TRANFILE | -- | IDCAMS, SDSF |
| DUSRSECJ | -- | IEFBR14, IEBGENER, IDCAMS |
| CLOSEFIL | -- | SDSF |
| OPENFIL | -- | SDSF |

---

## 5. BMS Map-to-Program Mapping

| BMS Map | COBOL Program | BMS Copybook | Screen Function |
|---------|---------------|-------------|-----------------|
| COSGN00 | COSGN00C | COSGN00.CPY | Sign-on |
| COMEN01 | COMEN01C | COMEN01.CPY | Main menu |
| COADM01 | COADM01C | COADM01.CPY | Admin menu |
| COACTVW | COACTVWC | COACTVW.CPY | Account view |
| COACTUP | COACTUPC | COACTUP.CPY | Account update |
| COCRDLI | COCRDLIC | COCRDLI.CPY | Card list |
| COCRDSL | COCRDSLC | COCRDSL.CPY | Card view |
| COCRDUP | COCRDUPC | COCRDUP.CPY | Card update |
| COTRN00 | COTRN00C | COTRN00.CPY | Transaction list |
| COTRN01 | COTRN01C | COTRN01.CPY | Transaction view |
| COTRN02 | COTRN02C | COTRN02.CPY | Transaction add |
| CORPT00 | CORPT00C | CORPT00.CPY | Report selection |
| COBIL00 | COBIL00C | COBIL00.CPY | Bill payment |
| COUSR00 | COUSR00C | COUSR00.CPY | User list |
| COUSR01 | COUSR01C | COUSR01.CPY | User add |
| COUSR02 | COUSR02C | COUSR02.CPY | User update |
| COUSR03 | COUSR03C | COUSR03.CPY | User delete |

---

## 6. Cross-Module Dependencies (Optional Modules)

### 6.1 Authorization Module Dependencies

```
COPAUA0C (Auth Decision)
  ├── reads: IMS DB (CIPAUSMY, CIPAUDTY segments)
  ├── reads/writes: MQ queues (authorization requests/responses)
  ├── copybooks: CCPAURQY, CCPAURLY, CIPAUDTY, CIPAUSMY, IMSFUNCS
  └── links to core: CARDXREF, ACCTDATA (via IMS summary segment)

COPAUS0C (Auth Summary View)
  ├── reads: IMS DB (CIPAUSMY summary)
  ├── BMS: COPAU00
  └── copybooks: CIPAUSMY, COPAU00

COPAUS1C (Auth Detail View)
  ├── reads: IMS DB (CIPAUDTY detail)
  ├── BMS: COPAU01
  └── copybooks: CIPAUDTY, COPAU01

COPAUS2C (Fraud Marking)
  ├── reads/writes: IMS DB (CIPAUDTY) + DB2 fraud table
  └── copybooks: CIPAUDTY

CBPAUP0C (Batch Purge)
  ├── reads/deletes: IMS DB (expired auth records)
  └── JCL: CBPAUP0J
```

### 6.2 Transaction Type DB2 Module Dependencies

```
COTRTLIC (Trans Type List)
  ├── reads: DB2 TRAN_TYPE table (cursor-based paging)
  ├── BMS: COTRTLI
  └── copybooks: CSDB2RWY, CSDB2RPY, COTRTLI

COTRTUPC (Trans Type Update)
  ├── reads/writes: DB2 TRAN_TYPE table
  ├── BMS: COTRTUP
  └── copybooks: CSDB2RWY, CSDB2RPY, COTRTUP

COBTUPDT (Batch Trans Type Update)
  ├── reads/writes: DB2 TRAN_TYPE table
  └── JCL: MNTTRDB2
```

### 6.3 VSAM-MQ Module Dependencies

```
CODATE01 (Date Service)
  ├── reads: MQ request queue (CDRD)
  └── writes: MQ response queue

COACCT01 (Account Inquiry)
  ├── reads: MQ request queue (CDRA)
  ├── reads: VSAM ACCTDATA
  └── writes: MQ response queue
```
