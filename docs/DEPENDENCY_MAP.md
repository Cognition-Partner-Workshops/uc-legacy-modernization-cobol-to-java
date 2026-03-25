# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This document maps every inter-program call, CICS transfer, copybook inclusion, JCL job-to-program execution, and data file read/write across the entire codebase.

---

## 1. Online Program Call Graph (CICS)

### 1.1 Navigation Flow

```
                            ┌──────────────┐
                            │  COSGN00C    │
                            │  (Sign-on)   │
                            │  Trans: CC00 │
                            └──────┬───────┘
                                   │
                       ┌───────────┴───────────┐
                       │ XCTL                  │ XCTL
                       ▼                       ▼
              ┌──────────────┐        ┌──────────────┐
              │  COMEN01C    │        │  COADM01C    │
              │ (Main Menu)  │        │ (Admin Menu) │
              │ Trans: CM00  │        │ Trans: CA00  │
              └──────┬───────┘        └──────┬───────┘
                     │                       │
     ┌───────┬───────┼───────┬───────┐       ├──── COUSR00C (User List)
     │       │       │       │       │       ├──── COUSR01C (User Add)
     ▼       ▼       ▼       ▼       ▼       ├──── COUSR02C (User Update)
  COACTVWC COACTUPC COCRDLIC COTRN00C COBIL00C├──── COUSR03C (User Delete)
  (AcctVw) (AcctUp) (CardLi) (TranLi) (Bill) ├──── COTRTLIC (Tran Type List)*
     │       │       │       │       │       └──── COTRTUPC (Tran Type Upd)*
     │       │       │       │                     (* = optional DB2 module)
     │       │       │       ├──── COTRN01C (Transaction View)
     │       │       │       └──── COTRN02C (Transaction Add)
     │       │       │
     │       │       ├──── COCRDSLC (Card View)
     │       │       └──── COCRDUPC (Card Update)
     │       │
     │       └──── CORPT00C (Reports)
     │
     └──── COPAUS0C (Pending Auth View)*
              └──── COPAUS1C (Auth Detail)*
                      └──── COPAUS2C (Fraud Marking)*
                            (* = optional Auth module)
```

### 1.2 CICS XCTL (Transfer Control) Targets

Every XCTL represents a permanent transfer of control — the calling program terminates.

| Source Program | Target Program | Condition | COMMAREA |
|---|---|---|---|
| COSGN00C | COADM01C | User type = Admin | CARDDEMO-COMMAREA |
| COSGN00C | COMEN01C | User type = Regular | CARDDEMO-COMMAREA |
| COMEN01C | *(dynamic)* | Menu option selected → `CDEMO-MENU-OPT-PGMNAME(WS-OPTION)` | CARDDEMO-COMMAREA |
| COADM01C | *(dynamic)* | Admin option selected → `CDEMO-ADMIN-OPT-PGMNAME` | CARDDEMO-COMMAREA |
| COACTUPC | *(dynamic)* | Back navigation → `CDEMO-TO-PROGRAM` | CARDDEMO-COMMAREA |
| COACTVWC | *(dynamic)* | Back navigation → `CDEMO-TO-PROGRAM` | CARDDEMO-COMMAREA |
| COCRDLIC | *(dynamic)* | Select card → `CCARD-NEXT-PROG` | CARDDEMO-COMMAREA |
| COCRDLIC | LIT-MENUPGM | Return to menu | CARDDEMO-COMMAREA |
| COCRDSLC | *(dynamic)* | Back navigation → `CDEMO-TO-PROGRAM` | CARDDEMO-COMMAREA |
| COCRDUPC | *(dynamic)* | Back navigation → `CDEMO-TO-PROGRAM` | CARDDEMO-COMMAREA |

### 1.3 Batch Program CALL Targets

CALL invokes a subroutine and returns control to the caller.

| Caller | Callee | Purpose |
|---|---|---|
| CBSTM03A | CBSTM03B | File I/O handling for statement generation |
| CBACT01C | CSUTLDTC | Date conversion (CEEDAYS) |
| CBTRN01C | CSUTLDTC | Date conversion (CEEDAYS) |
| CBTRN02C | CSUTLDTC | Date conversion (CEEDAYS) |

---

## 2. Copybook Inclusion Map

### 2.1 Business Entity Copybooks — Who Uses What

| Copybook | Entity | Used By Programs |
|---|---|---|
| CVACT01Y | Account | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBSTM03A |
| CVACT02Y | Card | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C |
| CVACT03Y | Card Xref | COACTVWC, COTRN02C, CBACT03C, CBACT04C, CBSTM03A, CBTRN03C |
| CVCUS01Y | Customer | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A |
| CVCRD01Y | Card Detail | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CVTRA05Y | Transaction | COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBACT04C, CBTRN03C |
| CVTRA06Y | Daily Tran | CBTRN01C, CBTRN02C |
| CVTRA01Y | Cat Balance | CBACT04C |
| CVTRA02Y | Disclosure | CBACT04C |
| CVTRA03Y | Tran Type | CBTRN03C |
| CVTRA04Y | Tran Category | CBTRN03C |
| CVTRA07Y | Report Layout | CBTRN03C |
| CSUSR01Y | User Security | COSGN00C, COUSR00C–03C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COTRN00C–02C, CORPT00C, COBIL00C |
| CVEXPORT | Export Record | CBEXPORT, CBIMPORT |
| COSTM01 | Stmt Tran | CBSTM03A |
| CUSTREC | Customer (stmt) | CBSTM03A |

### 2.2 System/UI Copybooks — Who Uses What

| Copybook | Purpose | Used By Programs |
|---|---|---|
| COCOM01Y | COMMAREA | All 17 online CICS programs |
| COTTL01Y | Screen title | All 17 online CICS programs |
| CSDAT01Y | Date display | All 17 online CICS programs |
| CSMSG01Y | Messages | All 17 online CICS programs |
| CSMSG02Y | Messages (secondary) | COACTVWC, COCRDSLC, COCRDUPC, COACTUPC, COBIL00C |
| COMEN02Y | Menu options | COMEN01C |
| COADM02Y | Admin options | COADM01C |
| CODATECN | Date conversion | CBACT01C |
| CSSTRPFY | Strip PF key | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COBIL00C |
| CSSETATY | Set attributes | *(included via inline)* |
| DFHAID | CICS AID keys | All 17 online CICS programs |
| DFHBMSCA | BMS attributes | All 17 online CICS programs |

### 2.3 BMS Map Copybooks — Program Mapping

| BMS Copybook | BMS Map Source | Program |
|---|---|---|
| COSGN00 | COSGN00.bms | COSGN00C |
| COMEN01 | COMEN01.bms | COMEN01C |
| COADM01 | COADM01.bms | COADM01C |
| COACTVW | COACTVW.bms | COACTVWC |
| COACTUP | COACTUP.bms | COACTUPC |
| COCRDLI | COCRDLI.bms | COCRDLIC |
| COCRDSL | COCRDSL.bms | COCRDSLC |
| COCRDUP | COCRDUP.bms | COCRDUPC |
| COTRN00 | COTRN00.bms | COTRN00C |
| COTRN01 | COTRN01.bms | COTRN01C |
| COTRN02 | COTRN02.bms | COTRN02C |
| CORPT00 | CORPT00.bms | CORPT00C |
| COBIL00 | COBIL00.bms | COBIL00C |
| COUSR00 | COUSR00.bms | COUSR00C |
| COUSR01 | COUSR01.bms | COUSR01C |
| COUSR02 | COUSR02.bms | COUSR02C |
| COUSR03 | COUSR03.bms | COUSR03C |

---

## 3. CICS File (VSAM) Access Map

### 3.1 Online Programs — VSAM File Access

| Program | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | USRSEC |
|---|---|---|---|---|---|---|
| COSGN00C | | | | | | R |
| COACTVWC | R | R | R | R | | |
| COACTUPC | RW | | RW | RW | | |
| COCRDLIC | | R | | | | |
| COCRDSLC | | R | R | R | | |
| COCRDUPC | | RW | | | | |
| COTRN00C | | | | | R | |
| COTRN01C | | | | | R | |
| COTRN02C | | | R | | RW | |
| CORPT00C | | | | | R | |
| COBIL00C | R | | R | | RW | |
| COUSR00C | | | | | | R |
| COUSR01C | | | | | | W |
| COUSR02C | | | | | | RW |
| COUSR03C | | | | | | RD |

**Legend:** R = Read, W = Write, RW = Read+Rewrite, RD = Read+Delete

### 3.2 Batch Programs — File Access

| Program | Files Read | Files Written |
|---|---|---|
| CBACT01C | ACCTFILE (account master) | OUTFILE, ARRYFILE, VBRCFILE |
| CBACT02C | CARDFILE (card data) | *(display only)* |
| CBACT03C | XREFFILE (cross-reference) | *(display only)* |
| CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TCATBALF (update), TRANSACT (sys tran) |
| CBCUS01C | CUSTFILE (customer data) | *(display only)* |
| CBTRN01C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | *(validation only)* |
| CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF |
| CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) |
| CBSTM03A | *(delegates to CBSTM03B)* | STMTFILE, HTMLFILE |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | *(via CBSTM03A)* |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (export flat file) |
| CBIMPORT | EXPFILE (import flat file) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

---

## 4. JCL Job → Program Execution Map

### 4.1 Jobs That Execute Application Programs

| JCL Job | Step | Program Executed | Purpose |
|---|---|---|---|
| POSTTRAN | STEP01 | CBTRN02C | Post daily transactions to master |
| INTCALC | STEP01 | CBACT04C | Calculate interest on all accounts |
| CREASTMT | STEP040 | CBSTM03A | Generate statements (text + HTML) |
| TRANREPT | STEP040 | CBTRN03C | Generate daily transaction report |
| READACCT | STEP01 | CBACT01C | Print/verify account data |
| READCARD | STEP01 | CBACT02C | Print/verify card data |
| READCUST | STEP01 | CBCUS01C | Print/verify customer data |
| READXREF | STEP01 | CBACT03C | Print/verify cross-reference data |
| WAITSTEP | STEP01 | COBSWAIT | Execute wait utility |
| CBEXPORT | STEP01 | CBEXPORT | Export VSAM to flat file |
| CBIMPORT | STEP01 | CBIMPORT | Import flat file to VSAM |

### 4.2 Jobs That Execute System Utilities

| JCL Job | Programs Used | Purpose |
|---|---|---|
| ACCTFILE | IDCAMS | Delete/define VSAM + REPRO load account data |
| CARDFILE | IDCAMS | Delete/define VSAM + REPRO load card data |
| CUSTFILE | IDCAMS | Delete/define VSAM + REPRO load customer data |
| XREFFILE | IDCAMS | Delete/define VSAM + REPRO load xref + build AIX |
| TRANFILE | IDCAMS | Delete/define VSAM + REPRO load transaction data |
| DUSRSECJ | IDCAMS, IEBGENER, SORT | Define VSAM + load user security data |
| TRANTYPE | IDCAMS | Load transaction type reference data |
| TRANCATG | IDCAMS | Load transaction category reference data |
| TCATBALF | IDCAMS | Load category balance data |
| DISCGRP | IDCAMS | Load disclosure group data |
| CLOSEFIL | DFHCSDUP | Close CICS-managed files |
| OPENFIL | DFHCSDUP | Open CICS-managed files |
| CBADMCDJ | DFHCSDUP | Define CICS CSD resources |
| DEFGDGB | IDCAMS | Define GDG base clusters |
| DEFGDGD | IDCAMS, IEBGENER | Define GDG bases + backup reference data |
| TRANBKP | IDCAMS, SORT | Backup transaction master |
| COMBTRAN | SORT, IDCAMS | Combine transaction backups |
| TRANIDX | IDCAMS | Define/build alternate index on transactions |
| DALYREJS | IDCAMS | Define daily rejection GDG |
| ESDSRRDS | IDCAMS, IEBGENER, SORT | Define ESDS/RRDS clusters (demo) |
| DEFCUST | IDCAMS | Define customer VSAM cluster |
| REPTFILE | IEFBR14 | Allocate report file datasets |
| PRTCATBL | IDCAMS, IEBGENER, SORT | Print category balance report |
| CREASTMT | SORT, IDCAMS, IEFBR14 | (pre-steps before CBSTM03A) |
| TRANREPT | IDCAMS, IEBGENER, SORT | (pre-steps before CBTRN03C) |
| FTPJCL | FTP | FTP file transfer |
| INTRDRJ1 | IDCAMS, IEBGENER | Internal reader trigger chain |
| INTRDRJ2 | IDCAMS | Internal reader copy |
| TXT2PDF1 | IKJEFT1B (TSO) | Convert statement text to PDF |

---

## 5. Data Lineage — Batch Processing Pipeline

### 5.1 Nightly Batch Cycle (in order)

```
Step 1: CLOSEFIL ─── Close CICS files for exclusive batch access
           │
Step 2: Data Refresh (parallel)
           ├── ACCTFILE ─── PS → ACCTDATA.VSAM.KSDS
           ├── CARDFILE ─── PS → CARDDATA.VSAM.KSDS
           ├── CUSTFILE ─── PS → CUSTDATA.VSAM.KSDS
           ├── XREFFILE ─── PS → CARDXREF.VSAM.KSDS (+ AIX)
           ├── TRANFILE ─── PS → TRANSACT.VSAM.KSDS
           └── DUSRSECJ ── PS → USRSEC.VSAM.KSDS
           │
Step 3: POSTTRAN (CBTRN02C)
           │  Reads:  DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM
           │  Writes: TRANSACT.VSAM, DALYREJS(+1), TCATBALF.VSAM
           │
Step 4: INTCALC (CBACT04C)
           │  Reads:  TCATBALF.VSAM, CARDXREF.VSAM(+AIX),
           │          ACCTDATA.VSAM, DISCGRP.VSAM
           │  Writes: SYSTRAN(+1), updates TCATBALF & ACCTDATA
           │
Step 5: TRANBKP
           │  Reads:  TRANSACT.VSAM
           │  Writes: TRANSACT.BKUP(+1)
           │
Step 6: COMBTRAN
           │  Reads:  TRANSACT.BKUP(0), SYSTRAN(0)
           │  Writes: TRANSACT.COMBINED(+1) → TRANSACT.VSAM
           │
Step 7: CREASTMT (CBSTM03A → CBSTM03B)
           │  Reads:  TRXFL.VSAM (sorted copy), CARDXREF.VSAM,
           │          ACCTDATA.VSAM, CUSTDATA.VSAM
           │  Writes: STATEMNT.PS, STATEMNT.HTML
           │
Step 8: TRANREPT (CBTRN03C)
           │  Reads:  TRANSACT.DALY(+1), CARDXREF.VSAM,
           │          TRANTYPE.VSAM, TRANCATG.VSAM, DATEPARM
           │  Writes: TRANREPT(+1)
           │
Step 9: TRANIDX ─── Build/rebuild alternate indexes
           │
Step 10: OPENFIL ── Re-open CICS files for online access
```

### 5.2 Data Flow Diagram — Key Datasets

```
                    ┌─────────────┐
                    │ DALYTRAN.PS │ (Daily feed from external source)
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  POSTTRAN   │ (CBTRN02C)
                    │  validates  │
                    │  & posts    │
                    └──┬────┬──┬──┘
                       │    │  │
              ┌────────┘    │  └────────┐
              ▼             ▼           ▼
     ┌──────────────┐ ┌──────────┐ ┌──────────────┐
     │ TRANSACT.VSAM│ │DALYREJS  │ │TCATBALF.VSAM │
     │ (master)     │ │(rejects) │ │(cat balances)│
     └──────┬───────┘ └──────────┘ └──────┬───────┘
            │                              │
     ┌──────▼───────┐              ┌──────▼───────┐
     │   TRANBKP    │              │   INTCALC    │
     │  (backup)    │              │  (CBACT04C)  │
     └──────┬───────┘              └──────┬───────┘
            │                              │
            ▼                              ▼
     ┌──────────────┐              ┌──────────────┐
     │TRANSACT.BKUP │              │ SYSTRAN      │
     │ (GDG backup) │              │(interest trx)│
     └──────┬───────┘              └──────┬───────┘
            │                              │
            └────────────┬─────────────────┘
                         ▼
                  ┌──────────────┐
                  │  COMBTRAN    │
                  │  (combine)   │
                  └──────┬───────┘
                         ▼
                  ┌──────────────┐
                  │TRANSACT.VSAM │ (refreshed master)
                  └──────┬───────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
       ┌──────────────┐      ┌──────────────┐
       │  CREASTMT    │      │  TRANREPT    │
       │ (statements) │      │ (daily rept) │
       └──────┬───────┘      └──────┬───────┘
              │                      │
              ▼                      ▼
       ┌──────────────┐      ┌──────────────┐
       │ STATEMNT.PS  │      │ TRANREPT     │
       │ STATEMNT.HTML│      │ (GDG output) │
       └──────────────┘      └──────────────┘
```

---

## 6. VSAM Dataset Inventory

| Dataset Name | Type | Key | Record Len | Used By (Online) | Used By (Batch) |
|---|---|---|---|---|---|
| ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID(11) | 300 | COACTVWC, COACTUPC, COBIL00C | CBACT01C, CBACT04C, CBTRN02C, CBSTM03B, CBEXPORT |
| CARDDATA.VSAM.KSDS | KSDS | CARD-NUM(16) | 150 | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBTRN01C, CBEXPORT |
| CARDXREF.VSAM.KSDS | KSDS | CARD-NUM(16) | 50 | COACTVWC, COACTUPC, COCRDSLC, COTRN02C, COBIL00C | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT |
| CARDXREF.VSAM.AIX | AIX | ACCT-ID(11) | — | — | CBACT04C |
| CUSTDATA.VSAM.KSDS | KSDS | CUST-ID(9) | 500 | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | CBCUS01C, CBTRN01C, CBSTM03B, CBEXPORT |
| TRANSACT.VSAM.KSDS | KSDS | TRAN-ID(16) | 350 | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | CBACT04C, CBTRN02C, CBTRN03C, CBEXPORT |
| USRSEC.VSAM.KSDS | KSDS | USR-ID(8) | 80 | COSGN00C, COUSR00C–03C | DUSRSECJ (load) |
| TCATBALF.VSAM.KSDS | KSDS | ACCT+TYPE+CAT(17) | 50 | — | CBTRN02C, CBACT04C |
| DISCGRP.VSAM.KSDS | KSDS | GRP+TYPE+CAT(16) | 50 | — | CBACT04C |
| TRANTYPE.VSAM.KSDS | KSDS | TYPE(2) | 60 | — | CBTRN03C |
| TRANCATG.VSAM.KSDS | KSDS | TYPE+CAT(6) | 60 | — | CBTRN03C |
| DALYTRAN.PS | Sequential | — | 350 | — | CBTRN01C, CBTRN02C |
| TRXFL.VSAM.KSDS | KSDS | CARD+TRAN(32) | 350 | — | CBSTM03A/B |

---

## 7. Program-to-Program Dependency Matrix

### 7.1 Online Program Dependencies (who needs who)

```
COSGN00C ──→ COMEN01C, COADM01C          (authentication gateway)
COMEN01C ──→ COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC,
              COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C,
              COPAUS0C                      (menu dispatcher)
COADM01C ──→ COUSR00C, COUSR01C, COUSR02C, COUSR03C,
              COTRTLIC, COTRTUPC            (admin dispatcher)
COCRDLIC ──→ COCRDSLC, COCRDUPC            (list → detail/edit)
COTRN00C ──→ COTRN01C, COTRN02C            (list → detail/add)
```

### 7.2 Batch Program Dependencies

```
CBSTM03A ──→ CBSTM03B                      (CALL subroutine)
CBACT01C ──→ CSUTLDTC                       (CALL date utility)
CBTRN01C ──→ CSUTLDTC                       (CALL date utility)
CBTRN02C ──→ CSUTLDTC                       (CALL date utility)
```

---

## 8. Shared Copybook Coupling Analysis

This shows how tightly coupled programs are through shared data structures.

| Copybook | # Programs Using | Coupling Level | Risk if Changed |
|---|---|---|---|
| COCOM01Y | 17 | **Very High** | All online programs affected |
| COTTL01Y | 17 | High | All screens affected |
| CSDAT01Y | 17 | High | All screen date displays |
| CSMSG01Y | 17 | High | All screen messages |
| CSUSR01Y | 17 | High | All programs needing user context |
| DFHAID | 17 | High | All CICS programs (system) |
| DFHBMSCA | 17 | High | All BMS programs (system) |
| CVACT01Y | 5 | Medium | Account-related programs |
| CVACT02Y | 5 | Medium | Card-related programs |
| CVACT03Y | 6 | Medium | Cross-reference consumers |
| CVCUS01Y | 5 | Medium | Customer-related programs |
| CVTRA05Y | 6 | Medium | Transaction-related programs |
| CSSTRPFY | 6 | Medium | Programs with PF key handling |
| CSMSG02Y | 5 | Low-Medium | Programs with confirmation msgs |
| CVCRD01Y | 4 | Low | Card screen programs |

---

## 9. Modernization Dependency Clusters

Programs that share data and should be migrated together:

### Cluster 1: Account & Card Management (migrate together)
- **Programs:** COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC
- **Shared Data:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y
- **VSAM Files:** ACCTDATA, CARDDATA, CARDXREF, CUSTDATA
- **Estimated LOC:** 9,083

### Cluster 2: Transaction Processing (migrate together)
- **Programs:** COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C
- **Shared Data:** CVTRA05Y, CVTRA06Y, CVACT03Y
- **VSAM Files:** TRANSACT, DALYTRAN, CARDXREF, TCATBALF
- **Estimated LOC:** 3,686

### Cluster 3: Reporting & Statements (migrate together)
- **Programs:** CORPT00C, CBSTM03A, CBSTM03B
- **Shared Data:** CVTRA05Y, CVTRA07Y, COSTM01, CUSTREC
- **VSAM Files:** TRANSACT, TRXFL, CARDXREF, ACCTDATA, CUSTDATA
- **Estimated LOC:** 1,803

### Cluster 4: User Administration (migrate independently)
- **Programs:** COUSR00C, COUSR01C, COUSR02C, COUSR03C
- **Shared Data:** CSUSR01Y
- **VSAM Files:** USRSEC
- **Estimated LOC:** 1,767

### Cluster 5: Bill Payment (migrate independently)
- **Programs:** COBIL00C
- **Shared Data:** CVTRA05Y, CVACT03Y
- **VSAM Files:** TRANSACT, ACCTDATA, CARDXREF
- **Estimated LOC:** 572

### Cluster 6: Security & Navigation (migrate first — foundation)
- **Programs:** COSGN00C, COMEN01C, COADM01C
- **Shared Data:** COCOM01Y, COMEN02Y, COADM02Y, CSUSR01Y
- **VSAM Files:** USRSEC
- **Estimated LOC:** 856
