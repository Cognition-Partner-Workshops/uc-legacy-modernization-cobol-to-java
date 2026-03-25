# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Source:** Static analysis of EXEC CICS XCTL, CALL, COPY, and DATASET references

---

## Executive Summary

This document maps the complete call graph (which programs invoke which) and data lineage (which programs and jobs read/write which VSAM files) for the CardDemo application. The system has a **hub-and-spoke online architecture** centered on the signon and menu programs, plus a **pipeline batch architecture** that processes daily transactions through a defined sequence.

---

## 1. Online Program Call Graph (CICS XCTL)

The online programs use `EXEC CICS XCTL` (transfer control) to navigate between screens. The COMMAREA (COCOM01Y) carries session state across all transfers.

### 1.1 Visual Call Graph

```
                            ┌─────────────┐
                            │  COSGN00C   │
                            │  (Signon)   │
                            │  Trans: CC00│
                            └──────┬──────┘
                                   │
                      ┌────────────┼────────────┐
                      │ (Admin)    │             │ (Regular User)
                      ▼            │             ▼
               ┌─────────────┐    │      ┌─────────────┐
               │  COADM01C   │    │      │  COMEN01C   │
               │ (Admin Menu)│    │      │ (Main Menu) │
               │  Trans: CA00│    │      │  Trans: CM00│
               └──────┬──────┘    │      └──────┬──────┘
                      │           │             │
         ┌────────────┤           │    ┌────────┼────────┬──────────┬──────────┐
         │            │           │    │        │        │          │          │
         ▼            ▼           │    ▼        ▼        ▼          ▼          ▼
   ┌──────────┐ ┌──────────┐     │  ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
   │COUSR00C  │ │COUSR01C  │     │  │COACTVWC││COACTUPC││COCRDLIC││COTRN00C││CORPT00C│
   │User List │ │User Add  │     │  │Acct Viw││Acct Upd││Card Lst││Tran Lst││Reports │
   └────┬─────┘ └──────────┘     │  └────────┘└────────┘└───┬────┘└───┬────┘└────────┘
        │                        │                          │         │
   ┌────┼────┐                   │              ┌───────────┤    ┌────┤
   │         │                   │              │           │    │    │
   ▼         ▼                   │              ▼           ▼    ▼    ▼
┌────────┐┌────────┐             │        ┌────────┐ ┌────────┐┌────────┐
│COUSR02C││COUSR03C│             │        │COCRDSLC│ │COCRDUPC││COTRN01C│
│User Upd││User Del│             │        │Card Viw│ │Card Upd││Tran Viw│
└────────┘└────────┘             │        └────────┘ └────────┘└────────┘
                                 │
                                 │              ┌────────┐  ┌────────┐
                                 │              │COTRN02C│  │COBIL00C│
                                 │              │Tran Add│  │Bill Pay│
                                 │              └────────┘  └────────┘
                                 │
                                 │        ┌────────┐
                                 │        │COPAUS0C│ (Optional: Auth Summary)
                                 │        └────────┘
                                 │
                            All programs return to
                            menu via PF3 (XCTL back)
```

### 1.2 Detailed XCTL Transfer Table

| Source Program | Target Program | Trigger | Direction |
|---------------|---------------|---------|-----------|
| COSGN00C | COADM01C | Successful admin login | Forward |
| COSGN00C | COMEN01C | Successful user login | Forward |
| COMEN01C | COSGN00C | PF3 (Exit) | Backward |
| COMEN01C | COACTVWC | Menu Option 1 | Forward |
| COMEN01C | COACTUPC | Menu Option 2 | Forward |
| COMEN01C | COCRDLIC | Menu Option 3 | Forward |
| COMEN01C | COCRDSLC | Menu Option 4 | Forward |
| COMEN01C | COCRDUPC | Menu Option 5 | Forward |
| COMEN01C | COTRN00C | Menu Option 6 | Forward |
| COMEN01C | COTRN01C | Menu Option 7 | Forward |
| COMEN01C | COTRN02C | Menu Option 8 | Forward |
| COMEN01C | CORPT00C | Menu Option 9 | Forward |
| COMEN01C | COBIL00C | Menu Option 10 | Forward |
| COMEN01C | COPAUS0C | Menu Option 11 (optional) | Forward |
| COADM01C | COSGN00C | PF3 (Exit) | Backward |
| COADM01C | COUSR00C | Admin Option 1 | Forward |
| COADM01C | COUSR01C | Admin Option 2 | Forward |
| COADM01C | COUSR02C | Admin Option 3 | Forward |
| COADM01C | COUSR03C | Admin Option 4 | Forward |
| COADM01C | COTRTLIC | Admin Option 5 (optional) | Forward |
| COADM01C | COTRTUPC | Admin Option 6 (optional) | Forward |
| COACTVWC | COMEN01C | PF3 (Back to menu) | Backward |
| COACTUPC | COMEN01C | PF3 (Back to menu) | Backward |
| COCRDLIC | COMEN01C | PF3 (Back to menu) | Backward |
| COCRDLIC | COCRDSLC | Select 'S' on row (View) | Forward |
| COCRDLIC | COCRDUPC | Select 'U' on row (Update) | Forward |
| COCRDSLC | COCRDLIC | PF3 (Back to list) | Backward |
| COCRDUPC | COCRDLIC | PF3 (Back to list) | Backward |
| COTRN00C | COMEN01C | PF3 (Back to menu) | Backward |
| COTRN00C | COTRN01C | Select row (View) | Forward |
| COTRN01C | COMEN01C | PF3 (Back) | Backward |
| COTRN02C | COMEN01C | PF3 (Back) | Backward |
| CORPT00C | COMEN01C | PF3 (Back) | Backward |
| COBIL00C | COMEN01C | PF3 (Back) | Backward |
| COUSR00C | COADM01C | PF3 (Back to admin) | Backward |
| COUSR00C | COUSR02C | Select 'U' on row (Update) | Forward |
| COUSR00C | COUSR03C | Select 'D' on row (Delete) | Forward |
| COUSR01C | COADM01C | PF3 (Back) | Backward |
| COUSR02C | COUSR00C | PF3 (Back to list) | Backward |
| COUSR03C | COUSR00C | PF3 (Back to list) | Backward |

### 1.3 CALL Dependencies (Subroutine Calls)

| Caller | Called Program | Purpose |
|--------|---------------|---------|
| CORPT00C | CSUTLDTC | Date validation/formatting |
| COTRN02C | CSUTLDTC | Date validation/formatting |
| CBSTM03A | CBSTM03B | Statement line item retrieval |
| CBACT01C | COBDATFT (ASM) | Date format conversion |
| COBSWAIT | MVSWAIT (ASM) | Assembler wait routine |
| All batch programs | CEE3ABD | LE abend handler (system) |
| CSUTLDTC | CEEDAYS | LE date conversion (system) |

---

## 2. Batch Job Execution Flow

### 2.1 Nightly Batch Cycle (Recommended Order)

```
Phase 1: PREPARE
  ┌──────────┐
  │ CLOSEFIL │──── Close CICS files (CEMT SET FIL CLO)
  └────┬─────┘
       │
Phase 2: DATA REFRESH (parallel, as needed)
       ├──► ACCTFILE ──── Refresh Account Master VSAM
       ├──► CARDFILE ──── Refresh Card Master VSAM
       ├──► CUSTFILE ──── Refresh Customer Master VSAM
       ├──► XREFFILE ──── Refresh Cross-Reference VSAM
       ├──► TRANFILE ──── Refresh Transaction Master VSAM
       └──► DUSRSECJ ──── Refresh User Security VSAM
       │
Phase 3: TRANSACTION PROCESSING
  ┌──────────┐
  │ POSTTRAN │──── Post daily transactions
  │          │     Step 1: CBTRN01C (validate daily trans)
  │          │     Step 2: CBTRN02C (post to master)
  └────┬─────┘
       │
Phase 4: FINANCIAL PROCESSING
  ┌──────────┐
  │ INTCALC  │──── Calculate interest
  │          │     Program: CBACT04C
  └────┬─────┘
       │
Phase 5: BACKUP
  ┌──────────┐
  │ TRANBKP  │──── Backup transaction file to GDG
  └────┬─────┘
       │
Phase 6: COMBINE & REPORT
  ┌──────────┐     ┌──────────┐
  │ COMBTRAN │──── │ CREASTMT │──── Generate statements
  │(Sort/Mrg)│     │CBSTM03A/B│
  └────┬─────┘     └────┬─────┘
       │                │
  ┌──────────┐          │
  │ TRANREPT │──── Transaction detail report (CBTRN03C)
  └────┬─────┘
       │
Phase 7: INDEX MAINTENANCE
  ┌──────────┐
  │ TRANIDX  │──── Rebuild alternate index on TRANSACT
  └────┬─────┘
       │
Phase 8: RESUME
  ┌──────────┐
  │ OPENFIL  │──── Reopen CICS files (CEMT SET FIL OPE)
  └──────────┘
```

### 2.2 JCL Job to Program Mapping

| JCL Job | Step(s) | Program(s) Executed | Input DD | Output DD |
|---------|---------|--------------------|---------|---------:|
| POSTTRAN | STEP01, STEP02 | CBTRN01C, CBTRN02C | DALYTRAN, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | DALYREJS, TRANFILE |
| INTCALC | STEP01 | CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TRANSACT |
| CREASTMT | STEP01, STEP02 | CBSTM03A, CBSTM03B | XREFFILE, CUSTFILE, ACCTFILE, TRNXFILE | STMTFILE, HTMLFILE |
| TRANREPT | STEP05R, STEP10 | REPROC, CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG | TRANREPT |
| COMBTRAN | STEP01 | SORT/MERGE | DALYTRAN, TRANFILE | Combined output |
| TRANBKP | STEP01 | IDCAMS REPRO | TRANSACT | GDG backup |
| CBEXPORT | STEP01 | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPORTFILE |
| CBIMPORT | STEP01 | CBIMPORT | EXPORTFILE | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE |
| WAITSTEP | STEP01 | COBSWAIT | -- | -- |

---

## 3. Data Lineage (File Access by Program)

### 3.1 Online Program File Access

| Program | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | TRANSACT | CXACAIX | CCXREF |
|---------|--------|---------|---------|---------|---------|----------|---------|--------|
| COSGN00C | **R** | | | | | | | |
| COMEN01C | | | | | | | | |
| COADM01C | | | | | | | | |
| COACTVWC | | **R** | | **R** | **R** | | **R** | |
| COACTUPC | | **RW** | | **R** | **R** | | **R** | |
| COCRDLIC | | | **R** | **R** | | | | |
| COCRDSLC | | | **R** | **R** | **R** | | | |
| COCRDUPC | | | **RW** | **R** | **R** | | | |
| COTRN00C | | | | | | **R** | | |
| COTRN01C | | | | | | **R** | | |
| COTRN02C | | **R** | | | | **RW** | **R** | **R** |
| CORPT00C | | | | | | **R** | | |
| COBIL00C | | **RW** | | | | **RW** | **R** | |
| COUSR00C | **R** | | | | | | | |
| COUSR01C | **W** | | | | | | | |
| COUSR02C | **RW** | | | | | | | |
| COUSR03C | **RW** | | | | | | | |

**Legend:** R = Read, W = Write, RW = Read/Write

### 3.2 Batch Program File Access

| Program | ACCTFILE | CARDFILE | CUSTFILE | XREFFILE | TRANFILE | DALYTRAN | DALYREJS | TCATBALF | DISCGRP | TRANTYPE | TRANCATG | TRANREPT |
|---------|----------|----------|----------|----------|----------|----------|----------|----------|---------|----------|----------|----------|
| CBACT01C | **R** | | | | | | | | | | | |
| CBACT02C | | **R** | | | | | | | | | | |
| CBACT03C | | | | **R** | | | | | | | | |
| CBACT04C | **RW** | | | **R** | **RW** | | | **R** | **R** | | | |
| CBCUS01C | | | **R** | | | | | | | | | |
| CBTRN01C | **R** | **R** | **R** | **R** | | **R** | | | | | | |
| CBTRN02C | **R** | | | **R** | **RW** | **R** | **W** | | | | | |
| CBTRN03C | | | | **R** | **R** | | | | | **R** | **R** | **W** |
| CBSTM03A | **R** | | **R** | **R** | | | | | | | | |
| CBSTM03B | **R** | | **R** | **R** | **R** | | | | | | | |
| CBEXPORT | **R** | **R** | **R** | **R** | **R** | | | | | | | |
| CBIMPORT | **W** | **W** | **W** | **W** | **W** | | | | | | | |

### 3.3 VSAM File Data Flow Diagram

```
                    ┌─────────────────────────────────────────────┐
                    │           ONLINE (CICS) PROGRAMS            │
                    │                                             │
  User Login ──►    │  COSGN00C ──R──► USRSEC                    │
                    │                                             │
  Account Ops ──►   │  COACTVWC ──R──► ACCTDAT, CUSTDAT, CXACAIX │
                    │  COACTUPC ──RW─► ACCTDAT, CUSTDAT, CXACAIX  │
                    │                                             │
  Card Ops ──►      │  COCRDLIC ──R──► CARDDAT, CARDAIX           │
                    │  COCRDSLC ──R──► CARDDAT, CARDAIX, CUSTDAT  │
                    │  COCRDUPC ──RW─► CARDDAT                    │
                    │                                             │
  Transaction ──►   │  COTRN00C ──R──► TRANSACT                   │
                    │  COTRN01C ──R──► TRANSACT                   │
                    │  COTRN02C ──RW─► TRANSACT, CXACAIX, CCXREF  │
                    │                                             │
  Billing ──►       │  COBIL00C ──RW─► ACCTDAT, TRANSACT, CXACAIX │
                    │                                             │
  User Admin ──►    │  COUSR00C-03C ──RW──► USRSEC                │
                    └─────────────────────────────────────────────┘

                                    │
                              CLOSEFIL / OPENFIL
                                    │
                                    ▼

                    ┌─────────────────────────────────────────────┐
                    │            BATCH PROCESSING                 │
                    │                                             │
  Daily Input ──►   │  DALYTRAN (sequential)                      │
                    │      │                                      │
                    │      ▼                                      │
                    │  CBTRN01C ──R──► DALYTRAN + CUSTFILE +      │
                    │                  XREFFILE + CARDFILE +      │
                    │                  ACCTFILE (validation)       │
                    │      │                                      │
                    │      ▼                                      │
                    │  CBTRN02C ──R──► DALYTRAN                   │
                    │            ──RW─► TRANFILE                  │
                    │            ──W──► DALYREJS (rejects)        │
                    │      │                                      │
                    │      ▼                                      │
                    │  CBACT04C ──R──► TCATBALF + XREFFILE +     │
                    │                  DISCGRP                    │
                    │            ──RW─► ACCTFILE + TRANSACT       │
                    │                  (interest calculation)      │
                    │      │                                      │
                    │      ▼                                      │
                    │  CBSTM03A/B ──R──► XREFFILE + CUSTFILE +   │
                    │                    ACCTFILE + TRNXFILE       │
                    │              ──W──► STMTFILE + HTMLFILE      │
                    │                    (statement generation)    │
                    │      │                                      │
                    │      ▼                                      │
                    │  CBTRN03C ──R──► TRANFILE + CARDXREF +     │
                    │                  TRANTYPE + TRANCATG        │
                    │            ──W──► TRANREPT (report output)  │
                    └─────────────────────────────────────────────┘
```

---

## 4. Copybook Dependency Matrix

### 4.1 Which Programs Include Which Copybooks

| Copybook | Programs That COPY It |
|----------|----------------------|
| **COCOM01Y** (COMMAREA) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** (Titles) | All 17 online CICS programs |
| **CSDAT01Y** (Date) | All 17 online CICS programs |
| **CSMSG01Y** (Messages) | All 17 online CICS programs |
| **CSUSR01Y** (User Sec) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C |
| **CVACT01Y** (Account) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT |
| **CVACT02Y** (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| **CVACT03Y** (Xref) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT |
| **CVCUS01Y** (Customer) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBSTM03A, CBEXPORT, CBIMPORT |
| **CVTRA05Y** (Transaction) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| **CVTRA06Y** (Daily Trans) | CBTRN01C, CBTRN02C |
| **CVCRD01Y** (Card Work) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSMSG02Y** (Abend) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| **CSSTRPFY** (String) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **COMEN02Y** (Menu Opts) | COMEN01C |
| **COADM02Y** (Admin Opts) | COADM01C |
| **CSUTLDWY** (Date Edit) | COACTUPC |
| **CSLKPCDY** (Lookup) | COACTUPC |
| **CODATECN** (Date Conv) | CBACT01C |
| **CVEXPORT** (Export) | CBEXPORT, CBIMPORT |
| **COSTM01** (Stmt Tran) | CBSTM03A |
| **CVTRA01Y** (Cat Bal) | CBACT04C, CBTRN02C |
| **CVTRA02Y** (Disc Grp) | CBACT04C |
| **CVTRA03Y** (Tran Type) | CBTRN03C |
| **CVTRA04Y** (Tran Cat) | CBTRN03C |
| **CVTRA07Y** (Report) | CBTRN03C |
| **CSSETATY** (Attributes) | COACTUPC |

### 4.2 Shared Data Structure Hubs

The most connected copybooks (highest coupling):

1. **COCOM01Y** -- 17 programs (all online) -- COMMAREA is the universal session contract
2. **CVACT03Y** -- 12 programs -- Cross-reference is the central join table
3. **CVACT01Y** -- 11 programs -- Account master is the core business entity
4. **CVTRA05Y** -- 11 programs -- Transaction record is the most accessed data
5. **CVCUS01Y** -- 9 programs -- Customer is widely referenced
6. **CVACT02Y** -- 8 programs -- Card data accessed for display and update
7. **CSUSR01Y** -- 11 programs -- Security record for authentication

---

## 5. Modernization Dependency Impact

### 5.1 Change Impact Analysis

If you modify a data structure, these programs are affected:

| Entity Changed | Programs Impacted | Risk Level |
|---------------|------------------|------------|
| Account (CVACT01Y) | 11 programs | **HIGH** -- Core entity, financial data |
| Transaction (CVTRA05Y) | 11 programs | **HIGH** -- Most accessed, financial data |
| Cross-Reference (CVACT03Y) | 12 programs | **HIGH** -- Central linkage table |
| Customer (CVCUS01Y) | 9 programs | **MEDIUM** -- PII concerns, fewer writers |
| Card (CVACT02Y) | 8 programs | **MEDIUM** -- Sensitive data (CVV) |
| User Security (CSUSR01Y) | 11 programs | **MEDIUM** -- Auth changes affect all |
| COMMAREA (COCOM01Y) | 17 programs | **CRITICAL** -- Any change affects all online |
| Daily Transaction (CVTRA06Y) | 2 programs | **LOW** -- Isolated to posting batch |
| Reference data (CVTRA03Y/04Y) | 1 program each | **LOW** -- Isolated lookup tables |

### 5.2 Recommended Migration Sequence

Based on dependency analysis, migrate in this order to minimize risk:

```
Wave 1: Foundation (Low coupling)
  ├── User Security (CSUSR01Y) + COUSR00C-03C + COSGN00C
  ├── Reference Tables (CVTRA03Y, CVTRA04Y, CVTRA02Y)
  └── Utility programs (CSUTLDTC, COBSWAIT)

Wave 2: Core Entities (Read-only first)
  ├── Customer (CVCUS01Y) + CBCUS01C
  ├── Account View (CVACT01Y) + COACTVWC
  ├── Card View (CVACT02Y) + COCRDSLC
  └── Transaction View (CVTRA05Y) + COTRN00C, COTRN01C

Wave 3: Core Write Operations
  ├── Account Update (COACTUPC) -- largest, most complex
  ├── Card Update (COCRDUPC)
  ├── Transaction Add (COTRN02C)
  └── Bill Payment (COBIL00C)

Wave 4: Batch Processing
  ├── Transaction Posting (CBTRN01C, CBTRN02C)
  ├── Interest Calculation (CBACT04C)
  ├── Statement Generation (CBSTM03A/B)
  └── Reporting (CBTRN03C)

Wave 5: Data Migration & Optional
  ├── Export/Import (CBEXPORT, CBIMPORT)
  └── Optional modules (Auth, DB2, MQ)
```
