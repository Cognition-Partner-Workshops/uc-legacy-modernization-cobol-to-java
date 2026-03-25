# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL / BMS (IBM z/OS)

---

## Table of Contents

- [1. Executive Summary](#1-executive-summary)
- [2. COBOL Programs (31 Core + 13 Optional)](#2-cobol-programs-31-core--13-optional)
  - [2.1 Online CICS Programs](#21-online-cics-programs)
  - [2.2 Batch Programs](#22-batch-programs)
  - [2.3 Utility Programs](#23-utility-programs)
  - [2.4 Optional Module — Authorization (IMS/DB2/MQ)](#24-optional-module--authorization-imsdb2mq)
  - [2.5 Optional Module — Transaction Type (DB2)](#25-optional-module--transaction-type-db2)
  - [2.6 Optional Module — VSAM-MQ](#26-optional-module--vsam-mq)
- [3. Copybooks (30 Core + 17 BMS-Generated)](#3-copybooks-30-core--17-bms-generated)
  - [3.1 Data Structure Copybooks](#31-data-structure-copybooks)
  - [3.2 UI / Common Copybooks](#32-ui--common-copybooks)
  - [3.3 Utility Copybooks](#33-utility-copybooks)
  - [3.4 BMS-Generated Copybooks](#34-bms-generated-copybooks)
- [4. JCL Batch Jobs (38)](#4-jcl-batch-jobs-38)
  - [4.1 Data Refresh / Load Jobs](#41-data-refresh--load-jobs)
  - [4.2 Transaction Processing Jobs](#42-transaction-processing-jobs)
  - [4.3 Reporting Jobs](#43-reporting-jobs)
  - [4.4 VSAM Administration Jobs](#44-vsam-administration-jobs)
  - [4.5 Utility / Infrastructure Jobs](#45-utility--infrastructure-jobs)
- [5. BMS Screen Maps (17)](#5-bms-screen-maps-17)
- [6. Assembler Programs (2)](#6-assembler-programs-2)
- [7. JCL Procedures (2)](#7-jcl-procedures-2)
- [8. Classification Summary](#8-classification-summary)

---

## 1. Executive Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It simulates core banking operations including account management, card management, transaction processing, bill payments, and reporting.

| Artifact Type | Count |
|---|---|
| COBOL Programs (Core) | 31 |
| COBOL Programs (Optional Modules) | 13 |
| Copybooks (Data Structure) | 30 |
| Copybooks (BMS-Generated) | 17 |
| JCL Jobs | 38 |
| BMS Screen Maps | 17 |
| Assembler Programs | 2 |
| JCL Procedures | 2 |
| **Total Artifacts** | **150** |

**Naming Conventions:**
- `CO*` — Online CICS programs
- `CB*` — Batch programs
- `CV*` — Copybook data structures (VSAM records)
- `CS*` — Copybook shared/system utilities
- `CO*` (in cpy) — Copybook UI/common areas

---

## 2. COBOL Programs (31 Core + 13 Optional)

### 2.1 Online CICS Programs

| # | Program | Lines | Function | Domain | CICS Trans | BMS Map |
|---|---------|-------|----------|--------|------------|---------|
| 1 | **COSGN00C** | 260 | Sign-on / Authentication | Security | CC00 | COSGN00 |
| 2 | **COMEN01C** | 308 | Main Menu Navigation | Navigation | CM00 | COMEN01 |
| 3 | **COADM01C** | 288 | Admin Menu | Admin | CA00 | COADM01 |
| 4 | **COACTVWC** | 941 | Account View (read-only) | Account Mgmt | CA01 | COACTVW |
| 5 | **COACTUPC** | 4,236 | Account Update | Account Mgmt | CA02 | COACTUP |
| 6 | **COCRDLIC** | 1,459 | Credit Card List (browse) | Card Mgmt | CC01 | COCRDLI |
| 7 | **COCRDSLC** | 887 | Credit Card Detail View | Card Mgmt | CC02 | COCRDSL |
| 8 | **COCRDUPC** | 1,560 | Credit Card Update | Card Mgmt | CC03 | COCRDUP |
| 9 | **COTRN00C** | 699 | Transaction List (browse) | Transactions | CT00 | COTRN00 |
| 10 | **COTRN01C** | 330 | Transaction View (read-only) | Transactions | CT01 | COTRN01 |
| 11 | **COTRN02C** | 783 | Transaction Add (new entry) | Transactions | CT02 | COTRN02 |
| 12 | **CORPT00C** | 649 | Transaction Report Request | Reporting | CR00 | CORPT00 |
| 13 | **COBIL00C** | 572 | Bill Payment Processing | Billing | CB00 | COBIL00 |
| 14 | **COUSR00C** | 695 | User List (Admin browse) | User Admin | CU00 | COUSR00 |
| 15 | **COUSR01C** | 299 | User Add (Admin) | User Admin | CU01 | COUSR01 |
| 16 | **COUSR02C** | 414 | User Update (Admin) | User Admin | CU02 | COUSR02 |
| 17 | **COUSR03C** | 359 | User Delete (Admin) | User Admin | CU03 | COUSR03 |

### 2.2 Batch Programs

| # | Program | Lines | Function | Domain | Input Files | Output Files |
|---|---------|-------|----------|--------|-------------|--------------|
| 18 | **CBACT01C** | 430 | Account File Read/Validate | Account Mgmt | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE |
| 19 | **CBACT02C** | 178 | Card File Read/Validate | Card Mgmt | CARDFILE | — |
| 20 | **CBACT03C** | 178 | Cross-Reference File Read/Validate | Card Mgmt | XREFFILE | — |
| 21 | **CBACT04C** | 652 | Interest Calculation | Finance | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | TRANSACT |
| 22 | **CBCUS01C** | 178 | Customer File Read/Validate | Customer Mgmt | CUSTFILE | — |
| 23 | **CBTRN01C** | 494 | Daily Transaction Validation | Transactions | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | — |
| 24 | **CBTRN02C** | 731 | Transaction Posting (core) | Transactions | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF |
| 25 | **CBTRN03C** | 649 | Transaction Report Generation | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT |
| 26 | **CBSTM03A** | 924 | Statement Generation (main) | Reporting | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE |
| 27 | **CBSTM03B** | 230 | Statement Generation (subroutine) | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — |
| 28 | **CBEXPORT** | 582 | Data Export (all files → single) | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| 29 | **CBIMPORT** | 487 | Data Import (single → all files) | Data Migration | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

### 2.3 Utility Programs

| # | Program | Lines | Function | Domain |
|---|---------|-------|----------|--------|
| 30 | **CSUTLDTC** | 157 | Date Validation (CEEDAYS wrapper) | Utility |
| 31 | **COBSWAIT** | 41 | Wait Timer (calls MVSWAIT) | Utility |

### 2.4 Optional Module — Authorization (IMS/DB2/MQ)

Located in `app/app-authorization-ims-db2-mq/cbl/`

| # | Program | Function | Type | Technology |
|---|---------|----------|------|------------|
| 32 | **COPAUA0C** | Card Authorization Decision | CICS Online | IMS + MQ |
| 33 | **COPAUS0C** | Authorization Summary View | CICS Online | IMS + BMS |
| 34 | **COPAUS1C** | Authorization Detail View | CICS Online | IMS + BMS |
| 35 | **COPAUS2C** | Mark Authorization as Fraud | CICS Online | IMS + DB2 |
| 36 | **CBPAUP0C** | Purge Expired Auth Messages | Batch | IMS |
| 37 | **DBUNLDGS** | IMS Database Unload | Batch | IMS |
| 38 | **PAUDBLOD** | IMS Database Load | Batch | IMS + Sequential |
| 39 | **PAUDBUNL** | IMS Database Unload to File | Batch | IMS + Sequential |

### 2.5 Optional Module — Transaction Type (DB2)

Located in `app/app-transaction-type-db2/cbl/`

| # | Program | Function | Type | Technology |
|---|---------|----------|------|------------|
| 40 | **COTRTUPC** | Transaction Type Add/Edit | CICS Online | DB2 + BMS |
| 41 | **COTRTLIC** | Transaction Type List/Delete | CICS Online | DB2 + BMS + Cursors |
| 42 | **COBTUPDT** | Transaction Type Batch Update | Batch | DB2 |

### 2.6 Optional Module — VSAM-MQ

Located in `app/app-vsam-mq/cbl/`

| # | Program | Function | Type | Technology |
|---|---------|----------|------|------------|
| 43 | **COACCT01** | Account Inquiry via MQ | CICS Online | VSAM + MQ |
| 44 | **CODATE01** | System Date Service via MQ | CICS Online | MQ |

---

## 3. Copybooks (30 Core + 17 BMS-Generated)

### 3.1 Data Structure Copybooks

| # | Copybook | Lines | Record Len | Entity | Description |
|---|----------|-------|------------|--------|-------------|
| 1 | **CVACT01Y** | 20 | 300 bytes | Account | Account master record |
| 2 | **CVACT02Y** | 14 | 150 bytes | Card | Card detail record |
| 3 | **CVACT03Y** | 11 | 50 bytes | Card X-Ref | Card-to-account cross-reference |
| 4 | **CVCRD01Y** | 46 | — | Card (Expanded) | Extended card data with embossed name, CVV, status |
| 5 | **CVCUS01Y** | 26 | 500 bytes | Customer | Customer demographic data |
| 6 | **CUSTREC** | 26 | — | Customer (Alt) | Alternate customer record layout |
| 7 | **CVTRA01Y** | 13 | 50 bytes | Tran Cat Balance | Transaction category balance |
| 8 | **CVTRA02Y** | 13 | 50 bytes | Disclosure Group | Interest rate disclosure |
| 9 | **CVTRA03Y** | 10 | 60 bytes | Transaction Type | Transaction type master |
| 10 | **CVTRA04Y** | 12 | 60 bytes | Transaction Category | Transaction category type |
| 11 | **CVTRA05Y** | 21 | 350 bytes | Transaction | Online transaction record |
| 12 | **CVTRA06Y** | 21 | 350 bytes | Daily Transaction | Daily transaction record |
| 13 | **CVTRA07Y** | 73 | — | Transaction Report | Report headers, detail lines, totals |
| 14 | **CVEXPORT** | 103 | — | Export Record | Unified export/import record layout |
| 15 | **COSTM01** | 38 | 350 bytes | Statement Tran | Altered transaction layout for statements (card+tran key) |
| 16 | **CSUSR01Y** | 26 | 80 bytes | User Security | User authentication record |
| 17 | **UNUSED1Y** | 10 | 80 bytes | (Unused) | Deprecated/placeholder record |

### 3.2 UI / Common Copybooks

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 18 | **COCOM01Y** | 47 | Common communication area (COMMAREA) — shared state between programs |
| 19 | **COMEN02Y** | 101 | Menu option definitions and labels |
| 20 | **COADM02Y** | 62 | Admin menu option definitions |
| 21 | **COTTL01Y** | 27 | Screen title/header definitions |
| 22 | **CSMSG01Y** | 24 | Standard message area (thank you, info, error) |
| 23 | **CSMSG02Y** | 35 | Extended message area |
| 24 | **CSSETATY** | 30 | Screen attribute settings (BMS field attributes) |

### 3.3 Utility Copybooks

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 25 | **CSDAT01Y** | 58 | Date/time working storage fields |
| 26 | **CODATECN** | 52 | Date conversion constants and formats |
| 27 | **CSLKPCDY** | 1,318 | Lookup code tables (country codes, state codes, etc.) |
| 28 | **CSSTRPFY** | 85 | String/paragraph formatting utility |
| 29 | **CSUTLDPY** | 375 | Date utility parameter area (for CSUTLDTC) |
| 30 | **CSUTLDWY** | 89 | Date utility working storage |

### 3.4 BMS-Generated Copybooks

Located in `app/cpy-bms/` — auto-generated from BMS maps, one per screen:

| # | Copybook | Corresponding BMS | Screen |
|---|----------|-------------------|--------|
| 1 | COACTUP.CPY | COACTUP.bms | Account Update |
| 2 | COACTVW.CPY | COACTVW.bms | Account View |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 5 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 6 | COCRDSL.CPY | COCRDSL.bms | Card Detail View |
| 7 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 8 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 9 | CORPT00.CPY | CORPT00.bms | Transaction Report |
| 10 | COSGN00.CPY | COSGN00.bms | Sign-on |
| 11 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 12 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 13 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | User Add |
| 16 | COUSR02.CPY | COUSR02.bms | User Update |
| 17 | COUSR03.CPY | COUSR03.bms | User Delete |

---

## 4. JCL Batch Jobs (38)

### 4.1 Data Refresh / Load Jobs

| # | Job | Lines | Function | Key Datasets |
|---|-----|-------|----------|-------------|
| 1 | **ACCTFILE** | 65 | Refresh Account Master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | 128 | Refresh Card Master VSAM + Alt Index | CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | 84 | Refresh Customer Master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | 106 | Load Card Cross-Reference VSAM + Alt Index | CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | 125 | Load Transaction Master VSAM | TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ** | 92 | Load User Security VSAM | USRSEC.VSAM.KSDS |
| 7 | **DEFCUST** | 47 | Define Customer VSAM Cluster | CUSTDATA.VSAM.KSDS |
| 8 | **REPTFILE** | 32 | Refresh Report Date Parameters | DATEPARM |

### 4.2 Transaction Processing Jobs

| # | Job | Lines | Function | Programs Invoked |
|---|-----|-------|----------|-----------------|
| 9 | **POSTTRAN** | 45 | Post Daily Transactions | CBTRN02C |
| 10 | **INTCALC** | 44 | Calculate Interest on Balances | CBACT04C |
| 11 | **COMBTRAN** | 52 | Combine Daily → Master Transactions | SORT + IDCAMS |
| 12 | **TRANBKP** | 71 | Backup Transaction Files | IDCAMS REPRO |

### 4.3 Reporting Jobs

| # | Job | Lines | Function | Programs Invoked |
|---|-----|-------|----------|-----------------|
| 13 | **CREASTMT** | 97 | Generate Account Statements (text + HTML) | CBSTM03A |
| 14 | **TRANREPT** | 84 | Generate Daily Transaction Report | CBTRN03C |
| 15 | **TXT2PDF1** | 41 | Convert Text Statements to PDF | TXT2PDF (REXX) |
| 16 | **PRTCATBL** | 66 | Print Catalog Balance Report | IDCAMS |

### 4.4 VSAM Administration Jobs

| # | Job | Lines | Function |
|---|-----|-------|----------|
| 17 | **DEFGDGB** | 63 | Define GDG Base for Backups |
| 18 | **DEFGDGD** | 94 | Define GDG Base for Daily Files |
| 19 | **TRANIDX** | 58 | Define Transaction Alternate Index |
| 20 | **ESDSRRDS** | 124 | Define ESDS/RRDS VSAM Clusters |
| 21 | **CLOSEFIL** | 34 | Close CICS VSAM Files (pre-batch) |
| 22 | **OPENFIL** | 34 | Open CICS VSAM Files (post-batch) |

### 4.5 Utility / Infrastructure Jobs

| # | Job | Lines | Function |
|---|-----|-------|----------|
| 23 | **CBEXPORT** | 72 | Run Data Export Program |
| 24 | **CBIMPORT** | 68 | Run Data Import Program |
| 25 | **CBADMCDJ** | 167 | Admin Card Demo Job (multi-step setup) |
| 26 | **READACCT** | 50 | Read/Print Account File |
| 27 | **READCARD** | 31 | Read/Print Card File |
| 28 | **READCUST** | 30 | Read/Print Customer File |
| 29 | **READXREF** | 31 | Read/Print Cross-Reference File |
| 30 | **DALYREJS** | 32 | View Daily Rejection File |
| 31 | **DISCGRP** | 65 | Load Disclosure Group File |
| 32 | **TCATBALF** | 65 | Load Transaction Category Balance File |
| 33 | **TRANCATG** | 65 | Load Transaction Category File |
| 34 | **TRANTYPE** | 65 | Load Transaction Type File |
| 35 | **FTPJCL** | 42 | FTP File Transfer Job |
| 36 | **INTRDRJ1** | 19 | Internal Reader — Trigger Job Chain |
| 37 | **INTRDRJ2** | 14 | Internal Reader — Chained Job |
| 38 | **WAITSTEP** | 27 | Wait Step (calls COBSWAIT) |

---

## 5. BMS Screen Maps (17)

| # | Map | Lines | Screen Title | Associated Program |
|---|-----|-------|-------------|-------------------|
| 1 | **COSGN00** | 210 | Login Screen | COSGN00C |
| 2 | **COMEN01** | 167 | Main Menu | COMEN01C |
| 3 | **COADM01** | 167 | Admin Menu | COADM01C |
| 4 | **COACTVW** | 378 | Account View | COACTVWC |
| 5 | **COACTUP** | 512 | Account Update | COACTUPC |
| 6 | **COCRDLI** | 344 | Card List | COCRDLIC |
| 7 | **COCRDSL** | 157 | Card Detail View | COCRDSLC |
| 8 | **COCRDUP** | 172 | Card Update | COCRDUPC |
| 9 | **COTRN00** | 464 | Transaction List | COTRN00C |
| 10 | **COTRN01** | 273 | Transaction View | COTRN01C |
| 11 | **COTRN02** | 307 | Transaction Add | COTRN02C |
| 12 | **CORPT00** | 231 | Transaction Report | CORPT00C |
| 13 | **COBIL00** | 141 | Bill Payment | COBIL00C |
| 14 | **COUSR00** | 463 | User List (Admin) | COUSR00C |
| 15 | **COUSR01** | 164 | User Add (Admin) | COUSR01C |
| 16 | **COUSR02** | 169 | User Update (Admin) | COUSR02C |
| 17 | **COUSR03** | 153 | User Delete (Admin) | COUSR03C |

---

## 6. Assembler Programs (2)

Located in `app/asm/`

| # | Program | Function |
|---|---------|----------|
| 1 | **MVSWAIT** | MVS Wait Service (called by COBSWAIT) |
| 2 | **COBDATFT** | COBOL Date Format Utility |

---

## 7. JCL Procedures (2)

Located in `app/proc/`

| # | Procedure | Function |
|---|-----------|----------|
| 1 | **REPROC** | Reprocessing Procedure (reusable JCL steps) |
| 2 | **TRANREPT** | Transaction Report Procedure |

---

## 8. Classification Summary

### By Business Domain

| Domain | Online Programs | Batch Programs | Total |
|--------|----------------|----------------|-------|
| Security / Auth | 1 | — | 1 |
| Navigation | 2 | — | 2 |
| Account Management | 2 | 1 | 3 |
| Card Management | 3 | 2 | 5 |
| Transaction Processing | 3 | 3 | 6 |
| Billing / Payments | 1 | — | 1 |
| Reporting | 1 | 3 | 4 |
| User Administration | 4 | — | 4 |
| Data Migration | — | 2 | 2 |
| Utilities | — | 2 | 2 |
| **Core Subtotal** | **17** | **13** (+1 subroutine) | **31** |
| Auth Module (Optional) | 4 | 4 | 8 |
| TranType Module (Optional) | 2 | 1 | 3 |
| VSAM-MQ Module (Optional) | 2 | — | 2 |
| **Grand Total** | **25** | **18** (+1 subroutine) | **44** |

### By Technology Stack

| Technology | Programs Using It |
|---|---|
| CICS (terminal I/O) | 17 core online + 8 optional online |
| VSAM (file I/O) | All 17 online + most batch |
| Batch (sequential files) | 13 core batch |
| BMS (3270 screens) | 17 online programs |
| DB2 (SQL) | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT |
| IMS (hierarchical DB) | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| MQ (messaging) | COPAUA0C, COACCT01, CODATE01 |
| LE (Language Environment) | CSUTLDTC (CEEDAYS), CBSTM03A (CEE3ABD) |

### By Modernization Priority

| Priority | Category | Count | Rationale |
|---|---|---|---|
| **P0 — Critical** | Core Transaction Processing | 6 | Revenue-impacting: CBTRN02C, CBACT04C, COBIL00C, COACTUPC, COTRN02C, CBSTM03A |
| **P1 — High** | Account & Card CRUD | 8 | Customer-facing: COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, CBTRN01C, CBTRN03C |
| **P2 — Medium** | Security & Navigation | 7 | Foundation: COSGN00C, COMEN01C, COADM01C, COUSR00C-03C |
| **P3 — Low** | Utilities & Data Migration | 10 | Infrastructure: CBACT01C-03C, CBCUS01C, CBEXPORT, CBIMPORT, CSUTLDTC, COBSWAIT, CBSTM03B |
| **P4 — Optional** | Extension Modules | 13 | Not in core path: all optional module programs |
