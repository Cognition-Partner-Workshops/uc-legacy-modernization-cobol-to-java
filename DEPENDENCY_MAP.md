# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Source:** Static analysis of CALL, COPY, EXEC CICS, and JCL DD statements

---

## Executive Summary

This document maps the complete call graph (which programs call which) and data lineage (which jobs read/write which files) for the CardDemo application. The core system has **32 programs** communicating via CICS XCTL/RETURN, COBOL CALL, and COMMAREA, accessing **9 VSAM datasets** and producing multiple report/backup outputs.

---

## 1. Online CICS Call Graph (Program-to-Program Navigation)

All online programs communicate via COMMAREA (`COCOM01Y`). Navigation is controlled by `CDEMO-TO-TRANID` / `CDEMO-TO-PROGRAM` fields.

```
                                ┌─────────────┐
                                │  COSGN00C   │
                                │  (Sign-On)  │
                                │  Trans: CC00│
                                └──────┬──────┘
                                       │ XCTL
                          ┌────────────┴────────────┐
                          │                         │
                   ┌──────▼──────┐          ┌───────▼──────┐
                   │  COMEN01C   │          │  COADM01C    │
                   │ (Main Menu) │          │ (Admin Menu) │
                   │ Trans: CM00 │          │ Trans: CA00  │
                   └──────┬──────┘          └───────┬──────┘
                          │                         │
          ┌───────┬───────┼───────┬────────┐        │
          │       │       │       │        │        ├──────────────────┐
          ▼       ▼       ▼       ▼        ▼        ▼                  ▼
      COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C COUSR00C      (+ all below)
      (Acct    (Card   (Tran    (Report) (Bill    (User
       View)    List)   List)             Pay)     List)
          │       │       │                         │
          ▼       ▼       ▼                    ┌────┼────┐
      COACTUPC COCRDSLC COTRN01C           COUSR01C COUSR02C COUSR03C
      (Acct    (Card   (Tran               (Add    (Update  (Delete
       Update)  View)   View)               User)   User)    User)
                │       │
                ▼       ▼
            COCRDUPC COTRN02C
            (Card    (Tran
             Update)  Add)
```

### 1.1 Detailed CICS Navigation Matrix

| Source Program | Target Program(s) | Navigation Method | Condition |
|---------------|-------------------|-------------------|-----------|
| COSGN00C | COMEN01C | XCTL via COMMAREA | User type = Regular |
| COSGN00C | COADM01C | XCTL via COMMAREA | User type = Admin |
| COMEN01C | COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C | XCTL via COMMAREA | Menu selection |
| COADM01C | COUSR00C, COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C | XCTL via COMMAREA | Admin menu selection |
| COACTVWC | COACTUPC | XCTL via COMMAREA | User selects "Update" |
| COCRDLIC | COCRDSLC | XCTL via COMMAREA | User selects a card |
| COCRDSLC | COCRDUPC | XCTL via COMMAREA | User selects "Update" |
| COTRN00C | COTRN01C | XCTL via COMMAREA | User selects a transaction |
| COTRN00C | COTRN02C | XCTL via COMMAREA | User selects "Add" |
| COUSR00C | COUSR01C, COUSR02C, COUSR03C | XCTL via COMMAREA | Admin CRUD selection |
| Any program | COMEN01C / COADM01C | XCTL via COMMAREA | PF3 (Return to menu) |
| CORPT00C | CBTRN03C | CALL (via START) | Initiate batch report |
| COTRN02C | CSUTLDTC | CALL | Date validation |

### 1.2 Shared Utility Calls

| Caller | Callee | Purpose |
|--------|--------|---------|
| CORPT00C | CSUTLDTC | Validate date range for report |
| COTRN02C | CSUTLDTC | Validate transaction date |
| CBSTM03A | CBSTM03B | File I/O delegation for statement generation |
| COBSWAIT | MVSWAIT (ASM) | MVS wait timer |
| CBACT01C | CBACT02C | Read card cross-reference records |
| CBACT01C | CBACT03C | Read card data records |

---

## 2. Batch Program Call Graph

```
POSTTRAN (JCL)
    └──► CBTRN02C ──► reads DALYTRAN.PS
                  ──► reads/writes TRANSACT.VSAM.KSDS
                  ──► reads CARDXREF.VSAM.KSDS
                  ──► writes DALYREJS (GDG)
                  ──► reads/writes ACCTDATA.VSAM.KSDS
                  ──► reads/writes TCATBALF.VSAM.KSDS

INTCALC (JCL)
    └──► CBACT04C ──► reads TCATBALF.VSAM.KSDS
                  ──► reads CARDXREF.VSAM.KSDS (+ AIX)
                  ──► reads/writes ACCTDATA.VSAM.KSDS
                  ──► reads DISCGRP.VSAM.KSDS
                  ──► writes SYSTRAN (GDG)

CREASTMT (JCL)
    └──► SORT (rekey transactions)
    └──► CBSTM03A ──► CALL CBSTM03B (file I/O)
                  ──► reads TRXFL.VSAM.KSDS (re-keyed copy)
                  ──► reads CARDXREF.VSAM.KSDS
                  ──► reads ACCTDATA.VSAM.KSDS
                  ──► reads CUSTDATA.VSAM.KSDS
                  ──► writes STATEMNT.PS (text)
                  ──► writes STATEMNT.HTML

TRANREPT (JCL)
    └──► CBTRN03C ──► reads TRANSACT.DALY (GDG)
                  ──► reads CARDXREF.VSAM.KSDS
                  ──► reads TRANTYPE.VSAM.KSDS
                  ──► reads TRANCATG.VSAM.KSDS
                  ──► reads DATEPARM
                  ──► writes TRANREPT (GDG)

CBEXPORT (JCL)
    └──► CBEXPORT ──► reads CUSTDATA.VSAM.KSDS
                  ──► reads ACCTDATA.VSAM.KSDS
                  ──► reads CARDXREF.VSAM.KSDS
                  ──► reads TRANSACT.VSAM.KSDS
                  ──► reads CARDDATA.VSAM.KSDS
                  ──► writes EXPORT.DATA

CBIMPORT (JCL)
    └──► CBIMPORT ──► reads EXPORT.DATA
                  ──► writes CUSTDATA.IMPORT
                  ──► writes ACCTDATA.IMPORT
                  ──► writes CARDXREF.IMPORT
                  ──► writes TRANSACT.IMPORT
                  ──► writes IMPORT.ERRORS
```

---

## 3. Copybook Dependency Matrix

Shows which programs include which copybooks (core programs only).

| Copybook | Used By Programs | Domain |
|----------|-----------------|--------|
| COCOM01Y | All 18 online CICS programs | Common COMMAREA |
| COTTL01Y | All 18 online CICS programs | Screen title/header |
| CSDAT01Y | All 18 online CICS programs | Date working storage |
| CSMSG01Y | All online + CORPT00C, COTRN02C | Message area |
| CSMSG02Y | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COBIL00C | Long messages |
| CSUSR01Y | COSGN00C, COUSR00C-03C | User security record |
| DFHAID | All 18 online CICS programs | CICS AID keys |
| DFHBMSCA | All 18 online CICS programs | BMS attributes |
| CVACT01Y | COACTUPC, COACTVWC, COBIL00C, CBACT01C, CBACT04C, CBSTM03A | Account data |
| CVACT02Y | COCRDLIC, COCRDSLC, COCRDUPC, CBACT03C | Card data |
| CVACT03Y | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, CBSTM03A | Card cross-ref |
| CVCUS01Y | COACTUPC, COACTVWC, CBCUS01C, CBSTM03A | Customer data |
| CVCRD01Y | COCRDLIC, COCRDSLC, COCRDUPC | Card detail display |
| CVTRA05Y | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | Transaction record |
| CVTRA06Y | CBTRN02C | Daily transaction |
| CVTRA01Y | CBACT04C, CBTRN02C | Category balance |
| CVTRA02Y | CBACT04C | Disclosure/interest |
| CVTRA03Y | CBTRN03C | Transaction type |
| CVTRA04Y | CBTRN03C | Transaction category |
| CVTRA07Y | CBTRN03C | Report layout |
| CVEXPORT | CBEXPORT, CBIMPORT | Export record |
| COSTM01 | CBSTM03A | Statement layout |
| CUSTREC | CBSTM03A | Customer record |
| COMEN02Y | COMEN01C | Main menu items |
| COADM02Y | COADM01C | Admin menu items |
| CSSETATY | COACTUPC, COCRDLIC, COCRDUPC, COBIL00C | BMS field attributes |
| CSSTRPFY | COACTUPC, COCRDLIC, COCRDUPC, COBIL00C | String processing |
| CSLKPCDY | COACTUPC | State/country lookup |
| CSUTLDPY | CORPT00C, COTRN02C, CBACT04C, CBTRN02C, CBTRN03C | Date utility procedures |
| CSUTLDWY | CORPT00C, COTRN02C, CBACT04C, CBTRN02C, CBTRN03C | Date utility working storage |
| CODATECN | CSUTLDTC | Date conversion constants |
| UNUSED1Y | (none active) | Deprecated |

---

## 4. VSAM File Data Lineage

### 4.1 File Access Matrix (Read/Write/Define)

| VSAM Dataset | Loaded By (JCL) | Read By (Programs) | Written By (Programs) | Backed Up By |
|-------------|-----------------|--------------------|-----------------------|-------------|
| ACCTDATA.VSAM.KSDS | ACCTFILE | COACTVWC, COACTUPC, COBIL00C, CBACT01C, CBACT04C, CBSTM03A, CBTRN02C, CBEXPORT | COACTUPC, COBIL00C, CBACT04C, CBTRN02C | -- |
| CARDDATA.VSAM.KSDS | CARDFILE | COCRDLIC, COCRDSLC, COCRDUPC, CBACT03C, CBEXPORT | COCRDUPC | -- |
| CUSTDATA.VSAM.KSDS | CUSTFILE | COACTVWC, COACTUPC, CBCUS01C, CBSTM03A, CBEXPORT | -- | -- |
| CARDXREF.VSAM.KSDS | XREFFILE | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, CBACT04C, CBSTM03A, CBTRN02C, CBTRN03C, CBEXPORT | -- | -- |
| TRANSACT.VSAM.KSDS | TRANFILE | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBEXPORT | COTRN02C, CBTRN02C | TRANBKP |
| USRSEC.VSAM.KSDS | DUSRSECJ | COSGN00C, COUSR00C-03C | COUSR01C, COUSR02C, COUSR03C | -- |
| TCATBALF.VSAM.KSDS | TCATBALF | CBACT04C, CBTRN02C | CBACT04C, CBTRN02C | PRTCATBL |
| TRANTYPE.VSAM.KSDS | TRANTYPE | CBTRN03C | -- | DEFGDGD |
| TRANCATG.VSAM.KSDS | TRANCATG | CBTRN03C | -- | DEFGDGD |
| DISCGRP.VSAM.KSDS | DISCGRP | CBACT04C | -- | DEFGDGD |
| DALYTRAN.PS | (external feed) | CBTRN02C | -- | -- |

### 4.2 Output File Lineage

| Output File | Produced By | Content | Consumed By |
|------------|------------|---------|-------------|
| TRANSACT.BKUP (GDG) | TRANBKP (IDCAMS) | Transaction backup | COMBTRAN, TRANREPT |
| SYSTRAN (GDG) | CBACT04C via INTCALC | System-generated interest transactions | COMBTRAN |
| TRANSACT.COMBINED (GDG) | COMBTRAN (SORT) | Merged backup + system transactions | (archive) |
| DALYREJS (GDG) | CBTRN02C via POSTTRAN | Rejected daily transactions | Manual review |
| STATEMNT.PS | CBSTM03A via CREASTMT | Text account statements | TXT2PDF1 |
| STATEMNT.HTML | CBSTM03A via CREASTMT | HTML account statements | (distribution) |
| TRANREPT (GDG) | CBTRN03C via TRANREPT | Daily transaction report | (management review) |
| TCATBALF.REPT | CBTRN01C via PRTCATBL | Category balance report | (audit) |
| TCATBALF.BKUP (GDG) | PRTCATBL (IDCAMS) | Category balance backup | (recovery) |
| EXPORT.DATA | CBEXPORT | Combined export file | CBIMPORT |
| STATEMNT.PS.PDF | TXT2PDF1 | PDF version of statements | (distribution) |

---

## 5. Batch Job Dependency Chain

### 5.1 Nightly Batch Cycle (Critical Path)

```
Phase 1: CICS Quiesce
    CLOSEFIL ──────────────────────────► (close all CICS files)

Phase 2: Data Refresh (parallel)
    ┌── ACCTFILE ──► ACCTDATA.VSAM.KSDS
    ├── CARDFILE ──► CARDDATA.VSAM.KSDS
    ├── CUSTFILE ──► CUSTDATA.VSAM.KSDS
    ├── XREFFILE ──► CARDXREF.VSAM.KSDS
    └── TRANFILE ──► TRANSACT.VSAM.KSDS

Phase 3: Transaction Processing (sequential)
    POSTTRAN ──► (CBTRN02C: post daily transactions)
        │
        ▼
    INTCALC ──► (CBACT04C: calculate interest)
        │
        ▼
    TRANBKP ──► (backup transaction master)
        │
        ▼
    COMBTRAN ──► (merge backup + system transactions)

Phase 4: Reporting (parallel after Phase 3)
    ┌── CREASTMT ──► (CBSTM03A: generate statements)
    │       └──► TXT2PDF1 (optional: convert to PDF)
    └── TRANREPT ──► (CBTRN03C: daily transaction report)

Phase 5: Maintenance
    TRANIDX ──► (rebuild alternate indexes)

Phase 6: CICS Resume
    OPENFIL ──────────────────────────► (re-open all CICS files)
```

### 5.2 Job Predecessor/Successor Table

| Job | Must Run After | Must Run Before | Frequency |
|-----|---------------|-----------------|-----------|
| CLOSEFIL | -- | All data refresh jobs | Daily |
| ACCTFILE | CLOSEFIL | POSTTRAN | Daily |
| CARDFILE | CLOSEFIL | POSTTRAN | Daily |
| CUSTFILE | CLOSEFIL | CREASTMT | Daily |
| XREFFILE | CLOSEFIL | POSTTRAN | Daily |
| TRANFILE | CLOSEFIL | POSTTRAN | Daily |
| POSTTRAN | All data refresh | INTCALC | Daily |
| INTCALC | POSTTRAN | TRANBKP | Daily |
| TRANBKP | INTCALC | COMBTRAN | Daily |
| COMBTRAN | TRANBKP | OPENFIL | Daily |
| CREASTMT | COMBTRAN | TXT2PDF1, OPENFIL | Daily |
| TRANREPT | COMBTRAN | OPENFIL | Daily |
| TRANIDX | COMBTRAN | OPENFIL | Daily |
| TXT2PDF1 | CREASTMT | OPENFIL | Daily |
| OPENFIL | All batch jobs | -- | Daily |
| DEFGDGB | -- | First-time data load | One-time |
| DEFGDGD | DEFGDGB | Data refresh jobs | One-time |
| DUSRSECJ | -- | COSGN00C access | On-demand |
| CBEXPORT | CLOSEFIL | CBIMPORT (on target) | On-demand |
| CBIMPORT | CBEXPORT (on source) | OPENFIL | On-demand |
| PRTCATBL | INTCALC | -- | On-demand |

---

## 6. Optional Module Dependencies

### 6.1 Authorization Module (IMS/DB2/MQ)

```
MQ Queue (CDRA) ──► COPAUA0C ──► IMS DB (PAUTDB)
                                 ──► DB2 Table (PAUTHDTL)
                                 ──► MQ Response Queue

COPAUS0C ──► IMS DB (PAUTDB) ──► BMS Screen (COPAU00)
COPAUS1C ──► IMS DB (PAUTDB) ──► BMS Screen (COPAU01)
COPAUS2C ──► DB2 Table (PAUTHDTL) ──► Mark fraud flag

CBPAUP0C (batch) ──► IMS DB (PAUTDB) ──► Purge expired records

LOADPADB (JCL) ──► PAUDBLOD ──► IMS DB (PAUTDB)
UNLDPADB (JCL) ──► PAUDBUNL ──► Flat file
UNLDGSAM (JCL) ──► DBUNLDGS ──► GSAM unload
```

### 6.2 Transaction Type DB2 Module

```
COTRTUPC ──► DB2 TRANTYPD table (INSERT/UPDATE)
         ──► BMS Screen (COTRTUP)

COTRTLIC ──► DB2 TRANTYPD table (SELECT/DELETE with cursors)
         ──► BMS Screen (COTRTLI)

COBTUPDT (batch) ──► DB2 TRANTYPD table (batch UPDATE)
MNTTRDB2 (JCL) ──► COBTUPDT
CREADB21 (JCL) ──► DDL to create DB2 objects
TRANEXTR (JCL) ──► Extract transaction types
```

### 6.3 VSAM-MQ Module

```
MQ Queue (CDRD) ──► CODATE01 ──► System date response via MQ
MQ Queue (CDRA) ──► COACCT01 ──► ACCTDATA.VSAM.KSDS
                               ──► Account inquiry response via MQ
```

---

## 7. Cross-Cutting Concerns

### 7.1 Shared Resources

| Resource | Type | Used By (Count) | Impact if Changed |
|----------|------|-----------------|-------------------|
| COCOM01Y (COMMAREA) | Copybook | 18 online programs | All navigation breaks |
| DFHAID / DFHBMSCA | CICS Copybooks | 18 online programs | All screen handling breaks |
| CARDXREF.VSAM.KSDS | VSAM File | 10+ programs | Card-Account linking breaks |
| ACCTDATA.VSAM.KSDS | VSAM File | 8+ programs | Account operations break |
| TRANSACT.VSAM.KSDS | VSAM File | 7+ programs | Transaction processing breaks |
| CSUTLDTC (Date Utility) | Program | 2+ callers | Date validation breaks |

### 7.2 Circular / Bidirectional Dependencies

- **COMEN01C ↔ All Online Programs:** Menu sends to programs via XCTL; programs return to menu via PF3
- **CBTRN02C ↔ ACCTDATA/TCATBALF:** Reads and writes during transaction posting (update-in-place)
- **CBACT04C ↔ ACCTDATA:** Reads balances, calculates interest, writes updated balances
