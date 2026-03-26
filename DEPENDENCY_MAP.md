# DEPENDENCY MAP — CardDemo COBOL Codebase

> **Generated:** 2026-03-26 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This document maps every inter-program call, CICS transfer, copybook inclusion,
> and data-file access across the entire CardDemo application.

---

## 1. Online Program Call Graph (CICS XCTL / LINK)

The online CICS programs navigate via `EXEC CICS XCTL` (transfer control) and
`EXEC CICS RETURN TRANSID` (pseudo-conversational return). The COMMAREA (`COCOM01Y`)
carries session state between all programs.

```
                        ┌─────────────┐
                        │  COSGN00C   │  (CC00 — Sign-on)
                        │  Sign-on    │
                        └──────┬──────┘
                               │ XCTL (on success)
                               ▼
                   ┌───────────────────────┐
                   │      COMEN01C         │  (CM00 — Main Menu)
                   │  Main Menu Router     │
                   └───────────┬───────────┘
                               │ XCTL via CDEMO-MENU-OPT-PGMNAME
           ┌───────────────────┼───────────────────────────────────┐
           │                   │                                   │
           ▼                   ▼                                   ▼
   ┌───────────────┐  ┌───────────────┐                   ┌───────────────┐
   │  COACTVWC     │  │  COACTUPC     │    ...            │  COADM01C     │
   │  Acct View    │  │  Acct Update  │                   │  Admin Menu   │
   └───────────────┘  └───────────────┘                   └───────┬───────┘
                                                                  │ XCTL via CDEMO-ADMIN-OPT-PGMNAME
                                                    ┌─────────────┼──────────────┐
                                                    ▼             ▼              ▼
                                             ┌──────────┐  ┌──────────┐   ┌──────────┐
                                             │ COUSR00C │  │ COUSR01C │   │ COUSR02C │ ...
                                             │ User List│  │ User Add │   │ User Upd │
                                             └──────────┘  └──────────┘   └──────────┘
```

### 1.1 Complete XCTL Navigation Table

Every online program can XCTL back to the menu or to another screen via `CDEMO-TO-PROGRAM`:

| Source Program | XCTL Target(s) | Mechanism |
|----------------|----------------|-----------|
| **COSGN00C** | COMEN01C | Direct XCTL on successful login |
| **COMEN01C** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | XCTL via `CDEMO-MENU-OPT-PGMNAME(WS-OPTION)` from COMEN02Y menu table |
| **COADM01C** | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | XCTL via `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)` from COADM02Y menu table |
| **COACTVWC** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` (return to menu) |
| **COACTUPC** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COCRDLIC** | (stays on same screen — pagination) | No XCTL; RETURN TRANSID for pagination |
| **COCRDSLC** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COCRDUPC** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COTRN00C** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COTRN01C** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COTRN02C** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **CORPT00C** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COBIL00C** | COMEN01C (or COADM01C) | XCTL via `CDEMO-TO-PROGRAM` |
| **COUSR00C** | COADM01C | XCTL via `CDEMO-TO-PROGRAM` |
| **COUSR01C** | COADM01C | XCTL via `CDEMO-TO-PROGRAM` |
| **COUSR02C** | COADM01C | XCTL via `CDEMO-TO-PROGRAM` |
| **COUSR03C** | COADM01C | XCTL via `CDEMO-TO-PROGRAM` |

### 1.2 COBOL CALL Statements (Sub-program Calls)

| Caller | Callee | Call Type | Purpose |
|--------|--------|-----------|---------|
| **CORPT00C** | CSUTLDTC | CALL 'CSUTLDTC' | Date/time conversion for report parameters |
| **COTRN02C** | CSUTLDTC | CALL 'CSUTLDTC' | Date/time conversion for transaction timestamp |
| **CBSTM03A** | CBSTM03B | CALL 'CBSTM03B' | Fetch transaction details for statement line items |
| **CBSTM03A** | CEE3ABD | CALL 'CEE3ABD' | LE abend routine |
| **CBACT01C** | COBDATFT | CALL 'COBDATFT' | Date format transformation (assembler) |
| **CBACT01C–04C, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C–02C** | CEE3ABD | CALL 'CEE3ABD' | LE abend routine (all batch programs) |
| **COBSWAIT** | MVSWAIT | CALL 'MVSWAIT' | MVS WAIT SVC (assembler) |
| **COPAUA0C** | MQOPEN, MQGET, MQPUT1, MQCLOSE | MQ API Calls | MQ message processing |
| **COACCT01** | MQOPEN, MQGET, MQPUT, MQCLOSE | MQ API Calls | Account inquiry via MQ |
| **CODATE01** | MQOPEN, MQGET, MQPUT, MQCLOSE | MQ API Calls | Date service via MQ |
| **DBUNLDGS, PAUDBLOD, PAUDBUNL** | CBLTDLI | DL/I Call | IMS database operations |

---

## 2. Batch Program Data Flow (File I/O)

### 2.1 Batch Program → File Access Matrix

| Program | Input Files (READ) | Output Files (WRITE) | File Operations |
|---------|-------------------|---------------------|-----------------|
| **CBACT01C** | ACCTFILE (VSAM KSDS) | OUTFILE (PS), ARRYFILE (PS), VBRCFILE (VB PS) | Sequential read → multi-format output |
| **CBACT02C** | CARDFILE (VSAM KSDS) | — (display only) | Sequential read |
| **CBACT03C** | XREFFILE (VSAM KSDS) | — (display only) | Sequential read |
| **CBACT04C** | ACCTFILE, XREFFILE, DISCGRP (VSAM) | TCATBALF (VSAM), TRANSACT (VSAM) | Interest calculation; updates balances |
| **CBCUS01C** | CUSTFILE (VSAM KSDS) | — (display only) | Sequential read |
| **CBTRN01C** | DALYTRAN (PS), CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE (VSAM) | — (display/verify) | Multi-file join reader |
| **CBTRN02C** | DALYTRAN (PS), TRANFILE, XREFFILE, ACCTFILE (VSAM) | DALYREJS (PS), TCATBALF (VSAM), TRANSACT (VSAM) | Core posting: validates, posts, rejects |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG (VSAM) | TRANREPT (print), DATEPARM (PS) | Report generation |
| **CBSTM03A** | ACCTFILE, XREFFILE, CUSTFILE, TRANSACT (VSAM) | STMTFILE (PS text), HTMLFILE (PS HTML) | Statement generation |
| **CBSTM03B** | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (VSAM) | — (returns data to caller) | Sub-program data fetch |
| **CBEXPORT** | ACCTFILE, CARDFILE, XREFFILE, CUSTFILE, TRANSACT (VSAM) | EXPFILE (PS) | Full VSAM export |
| **CBIMPORT** | EXPFILE (PS) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT (PS), ERROUT (PS) | Import with error handling |
| **COBSWAIT** | — | — | Calls MVSWAIT (no file I/O) |

### 2.2 Batch Data Flow Diagram

```
                    ┌─────────────────────────────────────────┐
                    │          DAILY TRANSACTION INPUT         │
                    │            (DALYTRAN.PS)                 │
                    └───────────────────┬─────────────────────┘
                                        │
                                        ▼
                               ┌─────────────────┐
                               │    CBTRN02C     │  (POSTTRAN job)
                               │  Transaction    │
                               │    Posting      │
                               └───┬─────┬───┬───┘
                                   │     │   │
                    ┌──────────────┘     │   └──────────────┐
                    ▼                    ▼                   ▼
           ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
           │  TRANSACT    │    │  TCATBALF    │    │  DALYREJS    │
           │  (VSAM KSDS) │    │  (VSAM KSDS) │    │  (PS)        │
           │  Posted Txns │    │  Cat Balances │    │  Rejects     │
           └──────┬───────┘    └──────┬───────┘    └──────────────┘
                  │                   │
         ┌────────┤                   │
         │        │                   │
         ▼        ▼                   ▼
  ┌────────────┐ ┌────────────┐ ┌────────────┐
  │  CBACT04C  │ │  CBTRN03C  │ │  CBSTM03A  │
  │  Interest  │ │  Tran Rept │ │  Statements │
  │  Calc      │ │  Generator │ │  Generator  │
  └─────┬──────┘ └─────┬──────┘ └──────┬──────┘
        │               │               │
        ▼               ▼               ▼
  ┌──────────┐   ┌──────────┐   ┌──────────────┐
  │ SYSTRAN  │   │ TRANREPT │   │ STMTFILE     │
  │ (GDG)    │   │ (print)  │   │ HTMLFILE     │
  └──────────┘   └──────────┘   └──────────────┘
```

---

## 3. Online Program → VSAM File Access

### 3.1 CICS File Access Matrix

| Program | ACCTDAT | CARDDAT | CUSTDAT | TRANSACT | USRSEC | CARDXREF | TCATBALF | Operations |
|---------|---------|---------|---------|----------|--------|----------|----------|------------|
| **COSGN00C** | | | | | READ | | | Authenticate user |
| **COMEN01C** | | | | | READ | | | Verify user type for menu |
| **COADM01C** | | | | | READ | | | Verify admin access |
| **COACTVWC** | READ | READ | READ | | | READ | | Display account details |
| **COACTUPC** | READ/REWRITE | READ | READ/REWRITE | | | READ | | Update account + customer |
| **COCRDLIC** | | STARTBR/READNEXT/READPREV | | | | | | Browse card list |
| **COCRDSLC** | | READ | READ | | | | | Display card details |
| **COCRDUPC** | | READ/REWRITE | | | | | | Update card |
| **COTRN00C** | | | | STARTBR/READNEXT/READPREV | | | | Browse transactions |
| **COTRN01C** | | | | READ | | | | View single transaction |
| **COTRN02C** | READ | | | READ/WRITE | | READ | | Add new transaction |
| **CORPT00C** | | | | (parameters only) | | | | Report parameter screen |
| **COBIL00C** | READ/REWRITE | | | STARTBR/READPREV/WRITE | | READ | | Post bill payment |
| **COUSR00C** | | | | | STARTBR/READNEXT/READPREV | | | Browse user list |
| **COUSR01C** | | | | | WRITE | | | Add new user |
| **COUSR02C** | | | | | READ/REWRITE | | | Update user |
| **COUSR03C** | | | | | READ/DELETE | | | Delete user |

### 3.2 VSAM File → Program Access (Reverse View)

| VSAM File | Readers | Writers | Deleters |
|-----------|---------|---------|----------|
| **ACCTDAT** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC (REWRITE), COBIL00C (REWRITE) | — |
| **CARDDAT** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC (REWRITE) | — |
| **CUSTDAT** | COACTVWC, COACTUPC, COCRDSLC | COACTUPC (REWRITE) | — |
| **TRANSACT** | COTRN00C, COTRN01C, COTRN02C, COBIL00C | COTRN02C (WRITE), COBIL00C (WRITE) | — |
| **USRSEC** | COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE) | COUSR03C (DELETE) |
| **CARDXREF** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | — | — |

---

## 4. Copybook Inclusion Map

### 4.1 Copybook → Program Usage Matrix

| Copybook | Used By (Programs) | Category |
|----------|--------------------|----------|
| **COCOM01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C–03C, COPAUS0C, COPAUS1C | Shared by ALL online programs |
| **COTTL01Y** | (same as COCOM01Y) | Screen titles — all online |
| **CSDAT01Y** | (same as COCOM01Y) | Date/time — all online |
| **CSMSG01Y** | (same as COCOM01Y) | Messages — all online |
| **DFHAID** | (same as COCOM01Y) | CICS AID keys — all online |
| **DFHBMSCA** | (same as COCOM01Y) | BMS attributes — all online |
| **CSUSR01Y** | COACTUPC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C–03C | Programs that check user auth |
| **CVACT01Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, COPAUA0C, COPAUS0C, COACCT01 | Account record — most programs |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COPAUS0C | Card record |
| **CVACT03Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COPAUA0C, COPAUS0C | Cross-reference |
| **CVCUS01Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COPAUA0C, COPAUS0C | Customer record |
| **CVTRA05Y** | COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C | Transaction record |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | Daily transaction record |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Screen work areas |
| **COMEN02Y** | COMEN01C | Main menu option table |
| **COADM02Y** | COADM01C | Admin menu option table |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C | Abend work areas |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | String processing procedures |
| **CSUTLDPY** | COACTUPC | Utility procedures |
| **CSUTLDWY** | COACTUPC | Utility working-storage |
| **CODATECN** | CBACT01C | Date conversion record |
| **COSTM01** | CBSTM03A | Statement transaction layout |
| **CUSTREC** | CBSTM03A | Customer record (alt layout) |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export/import record |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Category balance record |
| **CVTRA02Y** | CBACT04C | Disclosure group record |
| **CVTRA03Y** | CBTRN03C | Transaction type record |
| **CVTRA04Y** | CBTRN03C | Transaction category record |
| **CVTRA07Y** | CBTRN03C | Report layout record |

### 4.2 Program → Copybook Count

| Program | # Copybooks | Observation |
|---------|-------------|-------------|
| COACTUPC | 18 | Highest inclusion count — touches all entities |
| COACTVWC | 15 | Second highest — reads most entities |
| COCRDLIC | 11 | Card + common copybooks |
| COCRDUPC | 12 | Card + customer + common |
| COCRDSLC | 13 | Card detail view |
| CBEXPORT | 6 | All data entity copybooks |
| CBIMPORT | 6 | All data entity copybooks |
| COPAUA0C | 14 | Auth module — MQ + VSAM + entity copybooks |
| COPAUS0C | 14 | Auth summary — CICS + entity copybooks |

---

## 5. JCL Job → Program / Dataset Lineage

### 5.1 JCL Job Execution Map

| JCL Job | Programs Executed | Input Datasets | Output Datasets |
|---------|------------------|----------------|-----------------|
| **POSTTRAN** | CBTRN02C | DALYTRAN.PS, TRANFILE, XREFFILE, ACCTFILE | TRANSACT.VSAM, TCATBALF.VSAM, DALYREJS |
| **INTCALC** | CBACT04C | ACCTFILE, XREFFILE.AIX.PATH, DISCGRP | SYSTRAN (GDG), TCATBALF |
| **CREASTMT** | CBSTM03A, SORT, IDCAMS | ACCTFILE, XREFFILE, CUSTFILE, TRANSACT | STATEMNT.PS, STATEMNT.HTML, TRXFL.SEQ |
| **TRANREPT** | CBTRN03C, SORT | TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (GDG), TRANSACT.DALY |
| **COMBTRAN** | SORT, IDCAMS | SYSTRAN (GDG), TRANSACT.BKUP (GDG) | TRANSACT.COMBINED |
| **TRANBKP** | IDCAMS | TRANSACT.VSAM | TRANSACT.BKUP (GDG) |
| **ACCTFILE** | IDCAMS | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | IDCAMS, SDSF | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | IDCAMS, SDSF | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | IDCAMS | CARDXREF.PS | CARDXREF.VSAM.KSDS |
| **TRANFILE** | IDCAMS, SDSF | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | IDCAMS, IEBGENER, IEFBR14 | USRSEC.PS | USRSEC.VSAM.KSDS |
| **CBEXPORT** | CBEXPORT, IDCAMS | All VSAM files | EXPORT.DATA |
| **CBIMPORT** | CBIMPORT | EXPORT.DATA | Individual import files + IMPORT.ERRORS |
| **READACCT** | CBACT01C | ACCTDATA.VSAM | ACCTDATA.PSCOMP, .ARRYPS, .VBPS |
| **READCARD** | CBACT02C | CARDDATA.VSAM | (display) |
| **READCUST** | CBCUS01C | CUSTDATA.VSAM | (display) |
| **READXREF** | CBACT03C | CARDXREF.VSAM | (display) |
| **WAITSTEP** | COBSWAIT | — | — |
| **PRTCATBL** | SORT | TCATBALF.VSAM | TCATBALF.REPT, TCATBALF.BKUP (GDG) |
| **TXT2PDF1** | IKJEFT1B | STATEMNT.PS | (PDF output) |

### 5.2 Dataset Lineage — Producers and Consumers

| Dataset (VSAM / PS) | Produced By (JCL/Program) | Consumed By (JCL/Program) |
|---------------------|--------------------------|--------------------------|
| **ACCTDATA.VSAM.KSDS** | ACCTFILE (IDCAMS load) | POSTTRAN, INTCALC, CREASTMT, READACCT, CBEXPORT; Online: COACTVWC, COACTUPC, COTRN02C, COBIL00C |
| **CARDDATA.VSAM.KSDS** | CARDFILE (IDCAMS load) | CBEXPORT, READCARD; Online: COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CUSTDATA.VSAM.KSDS** | CUSTFILE (IDCAMS load) | CREASTMT, READCUST, CBEXPORT; Online: COACTVWC, COACTUPC, COCRDSLC |
| **CARDXREF.VSAM.KSDS** | XREFFILE (IDCAMS load) | INTCALC, CREASTMT, TRANREPT, CBEXPORT, READXREF; Online: COACTVWC, COACTUPC, COTRN02C, COBIL00C |
| **TRANSACT.VSAM.KSDS** | TRANFILE (IDCAMS load), POSTTRAN (CBTRN02C writes) | TRANREPT, CREASTMT, TRANBKP, COMBTRAN, CBEXPORT; Online: COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C |
| **USRSEC.VSAM.KSDS** | DUSRSECJ (IDCAMS load) | Online: COSGN00C, COMEN01C, COADM01C, COUSR00C–03C |
| **TCATBALF.VSAM.KSDS** | TCATBALF (IDCAMS load), POSTTRAN (CBTRN02C), INTCALC (CBACT04C) | PRTCATBL; Online: COBIL00C |
| **DISCGRP.VSAM.KSDS** | DISCGRP (IDCAMS load) | INTCALC (CBACT04C) |
| **TRANTYPE.VSAM.KSDS** | TRANTYPE (IDCAMS load) | TRANREPT (CBTRN03C) |
| **TRANCATG.VSAM.KSDS** | TRANCATG (IDCAMS load) | TRANREPT (CBTRN03C) |
| **DALYTRAN.PS** | External input (daily feed) | POSTTRAN (CBTRN02C) |
| **DALYREJS** | POSTTRAN (CBTRN02C) | DALYREJS (IDCAMS define) |
| **TRANSACT.BKUP (GDG)** | TRANBKP (IDCAMS) | COMBTRAN (SORT) |
| **SYSTRAN (GDG)** | INTCALC (CBACT04C) | COMBTRAN (SORT) |
| **STATEMNT.PS / .HTML** | CREASTMT (CBSTM03A) | TXT2PDF1 |
| **TRANREPT (GDG)** | TRANREPT (CBTRN03C) | (printed output) |
| **EXPORT.DATA** | CBEXPORT | CBIMPORT |

---

## 6. BMS Map → Program → Copybook Chain

Each BMS map generates a copybook that the associated program includes:

| BMS Source | Generated Copybook | Program | Data Copybooks Also Included |
|------------|-------------------|---------|------------------------------|
| COSGN00.bms | COSGN00.CPY | COSGN00C | CSUSR01Y, COCOM01Y |
| COMEN01.bms | COMEN01.CPY | COMEN01C | COMEN02Y, CSUSR01Y, COCOM01Y |
| COADM01.bms | COADM01.CPY | COADM01C | COADM02Y, CSUSR01Y, COCOM01Y |
| COACTVW.bms | COACTVW.CPY | COACTVWC | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y |
| COACTUP.bms | COACTUP.CPY | COACTUPC | CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC | CVACT02Y, CVCRD01Y |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC | CVACT02Y, CVCUS01Y, CVCRD01Y |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC | CVACT02Y, CVCUS01Y, CVCRD01Y |
| COTRN00.bms | COTRN00.CPY | COTRN00C | CVTRA05Y |
| COTRN01.bms | COTRN01.CPY | COTRN01C | CVTRA05Y |
| COTRN02.bms | COTRN02.CPY | COTRN02C | CVACT01Y, CVACT03Y, CVTRA05Y |
| CORPT00.bms | CORPT00.CPY | CORPT00C | CVTRA05Y |
| COBIL00.bms | COBIL00.CPY | COBIL00C | CVACT01Y, CVACT03Y, CVTRA05Y |
| COUSR00.bms | COUSR00.CPY | COUSR00C | CSUSR01Y |
| COUSR01.bms | COUSR01.CPY | COUSR01C | CSUSR01Y |
| COUSR02.bms | COUSR02.CPY | COUSR02C | CSUSR01Y |
| COUSR03.bms | COUSR03.CPY | COUSR03C | CSUSR01Y |

---

## 7. Technology Integration Points

### 7.1 CICS Services Used

| CICS Command | Programs Using It | Purpose |
|--------------|------------------|---------|
| `EXEC CICS READ` | COACTVWC, COACTUPC, COBIL00C, COCRDSLC, COCRDUPC, COSGN00C, COTRN01C, COTRN02C, COUSR02C, COUSR03C | Read VSAM record by key |
| `EXEC CICS WRITE` | COBIL00C, COTRN02C, COUSR01C | Write new VSAM record |
| `EXEC CICS REWRITE` | COACTUPC, COBIL00C, COCRDUPC, COUSR02C | Update existing VSAM record |
| `EXEC CICS DELETE` | COUSR03C | Delete VSAM record |
| `EXEC CICS STARTBR` | COCRDLIC, COTRN00C, COUSR00C, COBIL00C | Start browse for pagination |
| `EXEC CICS READNEXT` | COCRDLIC, COTRN00C, COUSR00C | Forward pagination |
| `EXEC CICS READPREV` | COCRDLIC, COTRN00C, COUSR00C, COBIL00C | Backward pagination |
| `EXEC CICS SEND MAP` | All online programs (17) | Display BMS screen |
| `EXEC CICS RECEIVE MAP` | All online programs (17) | Read user input from screen |
| `EXEC CICS XCTL` | COMEN01C, COADM01C, and all returning programs | Transfer control between programs |
| `EXEC CICS RETURN TRANSID` | All online programs | Pseudo-conversational return |

### 7.2 External Technology Dependencies

| Technology | Programs | Dependency Type |
|-----------|----------|----------------|
| **IBM MQ** | COPAUA0C, COACCT01, CODATE01 | Message queue API (MQOPEN, MQGET, MQPUT) |
| **IMS DB** | DBUNLDGS, PAUDBLOD, PAUDBUNL | DL/I calls (CBLTDLI) for hierarchical DB |
| **DB2** | COTRTLIC, COTRTUPC, COBTUPDT, COPAUS2C | Embedded SQL (EXEC SQL) |
| **LE Runtime** | All CB* batch programs | CEE3ABD abend handler |
| **SORT** | COMBTRAN, CREASTMT, TRANREPT, PRTCATBL | DFSORT/SYNCSORT utility |
| **IDCAMS** | 20+ JCL jobs | VSAM define/delete/repro utility |
| **SDSF** | CLOSEFIL, OPENFIL, CARDFILE, CUSTFILE, TRANFILE | CICS file open/close via console |
| **DSNTIAC** | COTRTLIC, COTRTUPC (via CSDB2RPY) | DB2 error message formatting |
