# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo (AWS Mainframe Credit Card Management)
> **Total Assets**: 31 COBOL Programs + 30 Copybooks + 38 JCL Jobs + 17 BMS Maps + 2 ASM Programs + 2 JCL Procedures + 13 Optional-Module Programs

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (31)](#cobol-programs-31)
3. [Copybooks (30)](#copybooks-30)
4. [JCL Batch Jobs (38)](#jcl-batch-jobs-38)
5. [BMS Screen Maps (17)](#bms-screen-maps-17)
6. [Assembler Programs (2)](#assembler-programs-2)
7. [JCL Procedures (2)](#jcl-procedures-2)
8. [Optional Extension Modules (13)](#optional-extension-modules-13)
9. [Supporting Assets](#supporting-assets)

---

## Executive Summary

CardDemo is an AWS-provided mainframe credit card management application built on COBOL/CICS/VSAM/JCL. It simulates account management, card management, transactions, bill payments, and reporting with two user roles (Regular and Admin). The codebase is designed as a reference for mainframe-to-Java modernization workshops.

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | `COSGN00C` (Signon) |
| `CB*` | Batch COBOL program | `CBTRN02C` (Transaction posting) |
| `CV*` | Copybook - VSAM data structure | `CVACT01Y` (Account record) |
| `CS*` | Copybook - Shared/system utility | `CSDAT01Y` (Date utility) |
| `CO*` (cpy) | Copybook - Common/online area | `COCOM01Y` (Common area) |

### Asset Summary

| Category | Count | Total LOC | Location |
|----------|-------|-----------|----------|
| COBOL Programs (Core) | 31 | 20,650 | `app/cbl/` |
| Copybooks (Data) | 30 | 2,786 | `app/cpy/` |
| BMS-Generated Copybooks | 17 | N/A | `app/cpy-bms/` |
| JCL Batch Jobs | 38 | 2,429 | `app/jcl/` |
| BMS Screen Maps | 17 | 4,472 | `app/bms/` |
| Assembler Programs | 2 | N/A | `app/asm/` |
| JCL Procedures | 2 | N/A | `app/proc/` |
| Optional Module Programs | 13 | N/A | `app/app-*/cbl/` |

---

## COBOL Programs (31)

### Online CICS Programs (20)

| # | Program | LOC | Function | Business Domain | CICS Trans | BMS Map | Complexity |
|---|---------|-----|----------|-----------------|------------|---------|------------|
| 1 | **COSGN00C** | 260 | User Signon / Authentication | Security | CC00 | COSGN00 | Low |
| 2 | **COMEN01C** | 308 | Main Menu Navigation | Navigation | CM00 | COMEN01 | Low |
| 3 | **COADM01C** | 288 | Admin Menu Navigation | Administration | CA00 | COADM01 | Low |
| 4 | **COACTVWC** | 941 | Account View (read-only) | Account Mgmt | CA01 | COACTVW | Medium |
| 5 | **COACTUPC** | 4,236 | Account Update (full CRUD) | Account Mgmt | CA02 | COACTUP | **Very High** |
| 6 | **COCRDLIC** | 1,459 | Card List with Pagination | Card Mgmt | CC01 | COCRDLI | High |
| 7 | **COCRDSLC** | 887 | Card Detail View/Selection | Card Mgmt | CC02 | COCRDSL | Medium |
| 8 | **COCRDUPC** | 1,560 | Card Update | Card Mgmt | CC03 | COCRDUP | **High** |
| 9 | **COTRN00C** | 699 | Transaction List with Browse | Transaction Mgmt | CT00 | COTRN00 | Medium |
| 10 | **COTRN01C** | 330 | Transaction View (single) | Transaction Mgmt | CT01 | COTRN01 | Low |
| 11 | **COTRN02C** | 783 | Transaction Add (new entry) | Transaction Mgmt | CT02 | COTRN02 | Medium |
| 12 | **CORPT00C** | 649 | Transaction Report Request | Reporting | CR00 | CORPT00 | Medium |
| 13 | **COBIL00C** | 572 | Bill Payment Processing | Billing | CB00 | COBIL00 | Medium |
| 14 | **COUSR00C** | 695 | User List (Admin) | User Admin | CU00 | COUSR00 | Medium |
| 15 | **COUSR01C** | 299 | User Add (Admin) | User Admin | CU01 | COUSR01 | Low |
| 16 | **COUSR02C** | 414 | User Update (Admin) | User Admin | CU02 | COUSR02 | Medium |
| 17 | **COUSR03C** | 359 | User Delete (Admin) | User Admin | CU03 | COUSR03 | Low |
| 18 | **CSUTLDTC** | 157 | Date Utility (CEEDAYS wrapper) | Utility | N/A | N/A | Low |
| 19 | **COBSWAIT** | 41 | Wait/Pause Utility | Utility | N/A | N/A | Trivial |

### Batch Programs (12)

| # | Program | LOC | Function | Business Domain | Called By JCL | Complexity |
|---|---------|-----|----------|-----------------|---------------|------------|
| 20 | **CBACT01C** | 430 | Read Account Data (with date formatting) | Account Mgmt | READACCT | Medium |
| 21 | **CBACT02C** | 178 | Read Card Data | Card Mgmt | READCARD | Low |
| 22 | **CBACT03C** | 178 | Read Cross-Reference Data | Card Mgmt | READXREF | Low |
| 23 | **CBACT04C** | 652 | Interest Calculation | Financial | INTCALC | **High** |
| 24 | **CBCUS01C** | 178 | Read Customer Data | Customer Mgmt | READCUST | Low |
| 25 | **CBTRN01C** | 494 | Read Daily Transactions | Transaction Mgmt | (internal) | Medium |
| 26 | **CBTRN02C** | 731 | Transaction Posting (daily) | Transaction Mgmt | POSTTRAN | **High** |
| 27 | **CBTRN03C** | 649 | Transaction Report Generation | Reporting | TRANREPT | Medium |
| 28 | **CBSTM03A** | 924 | Statement Generation (main) | Reporting | CREASTMT | **High** |
| 29 | **CBSTM03B** | 230 | Statement File I/O (subroutine) | Reporting | (called by CBSTM03A) | Low |
| 30 | **CBEXPORT** | 582 | Data Export to Flat File | Data Mgmt | CBEXPORT | Medium |
| 31 | **CBIMPORT** | 487 | Data Import from Flat File | Data Mgmt | CBIMPORT | Medium |

---

## Copybooks (30)

### Data Structure Copybooks (app/cpy/)

| # | Copybook | Lines | Record Type | Business Entity | Record Length | Used By |
|---|----------|-------|-------------|-----------------|---------------|---------|
| 1 | **CVACT01Y** | 20 | VSAM Record | Account Master | ~300 bytes | 9 programs |
| 2 | **CVACT02Y** | 14 | VSAM Record | Card Master | ~150 bytes | 6 programs |
| 3 | **CVACT03Y** | 11 | VSAM Record | Card Cross-Reference | ~50 bytes | 8 programs |
| 4 | **CVCUS01Y** | 26 | VSAM Record | Customer Master | ~500 bytes | 6 programs |
| 5 | **CVCRD01Y** | 46 | Screen Data | Card Detail (display) | N/A | 5 programs |
| 6 | **CVTRA01Y** | 13 | VSAM Record | Tran Category Balance | 50 bytes | 2 programs |
| 7 | **CVTRA02Y** | 13 | VSAM Record | Disclosure Group | 50 bytes | 1 program |
| 8 | **CVTRA03Y** | 10 | VSAM Record | Transaction Type | 60 bytes | 1 program |
| 9 | **CVTRA04Y** | 12 | VSAM Record | Transaction Category | 60 bytes | 1 program |
| 10 | **CVTRA05Y** | 21 | VSAM Record | Transaction Record | 350 bytes | 8 programs |
| 11 | **CVTRA06Y** | 21 | VSAM Record | Daily Transaction | 350 bytes | 2 programs |
| 12 | **CVTRA07Y** | 73 | Report Layout | Transaction Report | 133 bytes | 1 program |
| 13 | **CVEXPORT** | 103 | Flat File | Export/Import Data | Variable | 2 programs |
| 14 | **COSTM01** | 38 | Altered Layout | Statement Transaction | 350 bytes | 1 program |
| 15 | **CUSTREC** | 26 | Record | Customer (alternate) | ~500 bytes | 1 program |
| 16 | **UNUSED1Y** | 10 | (Unused) | Placeholder | 80 bytes | 0 programs |

### System/Utility Copybooks

| # | Copybook | Lines | Purpose | Used By |
|---|----------|-------|---------|---------|
| 17 | **COCOM01Y** | 47 | CICS Common Area (COMMAREA) | 16 online programs |
| 18 | **COMEN02Y** | 101 | Menu Definitions & Options | 1 program |
| 19 | **COADM02Y** | 62 | Admin Menu Definitions | 1 program |
| 20 | **COTTL01Y** | 27 | Title/Header Line Layout | 16 programs |
| 21 | **CSDAT01Y** | 58 | Date Formatting Utilities | 16 programs |
| 22 | **CSLKPCDY** | 1,318 | Lookup Code Tables | 1 program |
| 23 | **CSMSG01Y** | 24 | Message Area (short) | 16 programs |
| 24 | **CSMSG02Y** | 35 | Message Area (extended) | 6 programs |
| 25 | **CSSETATY** | 30 | Screen Attribute Setting | 1 program (39 COPY REPLACING) |
| 26 | **CSSTRPFY** | 85 | String Processing Functions | 4 programs |
| 27 | **CSUSR01Y** | 26 | User Security Record | 12 programs |
| 28 | **CSUTLDPY** | 375 | Date Utility Parameters | 1 program |
| 29 | **CSUTLDWY** | 89 | Date Utility Working Storage | 1 program |
| 30 | **CODATECN** | 52 | Date Conversion Record | 1 program |

### BMS-Generated Copybooks (17) (app/cpy-bms/)

| # | Copybook | Corresponding BMS Map | Screen |
|---|----------|-----------------------|--------|
| 1 | COSGN00.CPY | COSGN00.bms | Login Screen |
| 2 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COACTVW.CPY | COACTVW.bms | Account View |
| 5 | COACTUP.CPY | COACTUP.bms | Account Update |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card Selection |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 12 | CORPT00.CPY | CORPT00.bms | Transaction Reports |
| 13 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | Add User |
| 16 | COUSR02.CPY | COUSR02.bms | Update User |
| 17 | COUSR03.CPY | COUSR03.bms | Delete User |

---

## JCL Batch Jobs (38)

### Data Refresh Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked | Key Datasets |
|---|---------|-------|---------|------------------|--------------|
| 1 | **ACCTFILE** | 65 | Refresh Account VSAM from flat file | IDCAMS | ACCTDATA.PS -> ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | 128 | Refresh Card VSAM from flat file | IDCAMS, SDSF | CARDDATA.PS -> CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | 84 | Refresh Customer VSAM from flat file | IDCAMS, SDSF | CUSTDATA.PS -> CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | 106 | Load Card Cross-Reference + Alt Index | IDCAMS | CARDXREF.PS -> CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | 125 | Refresh Transaction VSAM | IDCAMS, SDSF | DALYTRAN.PS.INIT -> TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ** | 92 | Load User Security VSAM | IDCAMS, IEBGENER, IEFBR14 | USRSEC.PS -> USRSEC.VSAM.KSDS |
| 7 | **DISCGRP** | 65 | Load Disclosure Group VSAM | IDCAMS | DISCGRP.PS -> DISCGRP.VSAM.KSDS |
| 8 | **TRANCATG** | 65 | Load Transaction Category VSAM | IDCAMS | TRANCATG.PS -> TRANCATG.VSAM.KSDS |
| 9 | **TRANTYPE** | 65 | Load Transaction Type VSAM | IDCAMS | TRANTYPE.PS -> TRANTYPE.VSAM.KSDS |
| 10 | **TCATBALF** | 65 | Load Category Balance VSAM | IDCAMS | TCATBALF.PS -> TCATBALF.VSAM.KSDS |

### Core Batch Processing Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked | Key Datasets |
|---|---------|-------|---------|------------------|--------------|
| 11 | **POSTTRAN** | 45 | Daily Transaction Posting | CBTRN02C | DALYTRAN, TRANSACT, ACCTDATA, CARDXREF, TCATBALF |
| 12 | **INTCALC** | 44 | Interest Calculation | CBACT04C | ACCTDATA, CARDXREF, DISCGRP, TCATBALF, SYSTRAN |
| 13 | **TRANBKP** | 71 | Backup Transaction File | IDCAMS | TRANSACT.VSAM -> TRANSACT.BKUP GDG |
| 14 | **COMBTRAN** | 52 | Combine Transactions | IDCAMS, SORT | TRANSACT.BKUP + SYSTRAN -> TRANSACT.COMBINED |
| 15 | **CREASTMT** | 97 | Create Account Statements | CBSTM03A, IDCAMS, SORT | TRANSACT, XREF, ACCT, CUST -> STATEMNT.PS/.HTML |
| 16 | **TRANREPT** | 84 | Daily Transaction Report | CBTRN03C, SORT | TRANSACT, XREF, TRANCATG, TRANTYPE -> TRANREPT GDG |

### CICS File Management Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked |
|---|---------|-------|---------|------------------|
| 17 | **CLOSEFIL** | 34 | Close CICS Files for Batch | SDSF |
| 18 | **OPENFIL** | 34 | Reopen CICS Files After Batch | SDSF |

### Infrastructure / GDG Definition Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked |
|---|---------|-------|---------|------------------|
| 19 | **DEFGDGB** | 63 | Define GDG Bases | IDCAMS |
| 20 | **DEFGDGD** | 94 | Define GDG + Backup Reference Data | IDCAMS, IEBGENER |
| 21 | **DEFCUST** | 47 | Define Customer VSAM Cluster | IDCAMS |
| 22 | **TRANIDX** | 58 | Define Transaction Alternate Index | IDCAMS |
| 23 | **REPTFILE** | 32 | Define Report File | IDCAMS |
| 24 | **DALYREJS** | 32 | Define Daily Rejects GDG | IDCAMS |

### Data Read/Utility Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked |
|---|---------|-------|---------|------------------|
| 25 | **READACCT** | 50 | Read & Print Account Data | CBACT01C |
| 26 | **READCARD** | 31 | Read & Print Card Data | CBACT02C |
| 27 | **READCUST** | 30 | Read & Print Customer Data | CBCUS01C |
| 28 | **READXREF** | 31 | Read & Print Cross-Reference | CBACT03C |
| 29 | **PRTCATBL** | 66 | Print Category Balance Report | SORT, IEFBR14 |
| 30 | **WAITSTEP** | 27 | Wait Step (pause utility) | COBSWAIT |

### Data Export/Import Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked |
|---|---------|-------|---------|------------------|
| 31 | **CBEXPORT** | 72 | Export All VSAM to Flat File | CBEXPORT, IDCAMS |
| 32 | **CBIMPORT** | 68 | Import Flat File to VSAM | CBIMPORT |

### Administrative / Misc Jobs

| # | JCL Job | Lines | Purpose | Programs Invoked |
|---|---------|-------|---------|------------------|
| 33 | **CBADMCDJ** | 167 | Load CICS CSD Definitions | DFHCSDUP |
| 34 | **ESDSRRDS** | 124 | Create ESDS/RRDS Alternate Files | IDCAMS, IEBGENER, IEFBR14 |
| 35 | **FTPJCL** | 42 | FTP File Transfer | FTP |
| 36 | **INTRDRJ1** | 19 | Internal Reader - Trigger Job | IDCAMS, IEBGENER |
| 37 | **INTRDRJ2** | 14 | Internal Reader - Triggered Job | IDCAMS |
| 38 | **TXT2PDF1** | 41 | Convert Text Statement to PDF | IKJEFT1B (TXT2PDF REXX) |

---

## BMS Screen Maps (17)

| # | BMS Map | Lines | Screen Title | Associated Program | Screen Type |
|---|---------|-------|-------------|--------------------|----|
| 1 | **COSGN00** | 210 | Login Screen | COSGN00C | Authentication |
| 2 | **COMEN01** | 167 | Main Menu | COMEN01C | Navigation |
| 3 | **COADM01** | 167 | Admin Menu | COADM01C | Navigation |
| 4 | **COACTVW** | 378 | Account View | COACTVWC | Display |
| 5 | **COACTUP** | 512 | Account Update | COACTUPC | Data Entry |
| 6 | **COCRDLI** | 344 | Card Listing | COCRDLIC | List/Browse |
| 7 | **COCRDSL** | 157 | Card Selection | COCRDSLC | Display |
| 8 | **COCRDUP** | 172 | Card Update | COCRDUPC | Data Entry |
| 9 | **COTRN00** | 464 | Transaction List | COTRN00C | List/Browse |
| 10 | **COTRN01** | 273 | Transaction View | COTRN01C | Display |
| 11 | **COTRN02** | 307 | Transaction Add | COTRN02C | Data Entry |
| 12 | **CORPT00** | 231 | Transaction Reports | CORPT00C | Report Request |
| 13 | **COBIL00** | 141 | Bill Payment | COBIL00C | Data Entry |
| 14 | **COUSR00** | 463 | User List (Admin) | COUSR00C | List/Browse |
| 15 | **COUSR01** | 164 | Add User (Admin) | COUSR01C | Data Entry |
| 16 | **COUSR02** | 169 | Update User (Admin) | COUSR02C | Data Entry |
| 17 | **COUSR03** | 153 | Delete User (Admin) | COUSR03C | Confirmation |

---

## Assembler Programs (2)

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | **MVSWAIT** | `app/asm/MVSWAIT.asm` | System wait/pause (called by COBSWAIT) |
| 2 | **COBDATFT** | `app/asm/COBDATFT.asm` | Date formatting (called by CBACT01C) |

---

## JCL Procedures (2)

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | **REPROC** | `app/proc/REPROC.prc` | Reprocessing procedure |
| 2 | **TRANREPT** | `app/proc/TRANREPT.prc` | Transaction report procedure |

---

## Optional Extension Modules (13)

### Authorization Module (IMS/DB2/MQ) - `app/app-authorization-ims-db2-mq/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | **COPAUA0C** | CICS/IMS/MQ | Card Authorization Decision (MQ trigger) |
| 2 | **COPAUS0C** | CICS/IMS/BMS | Summary View of Authorization Messages |
| 3 | **COPAUS1C** | CICS/IMS/BMS | Detail View of Authorization Message |
| 4 | **COPAUS2C** | CICS/IMS/DB2 | Mark Authorization Message as Fraud |
| 5 | **CBPAUP0C** | Batch/IMS | Delete Expired Pending Authorization Messages |
| 6 | **PAUDBLOD** | Batch/IMS | IMS Database Load Utility |
| 7 | **PAUDBUNL** | Batch/IMS | IMS Database Unload Utility |
| 8 | **DBUNLDGS** | Batch/IMS | IMS Database Unload (general segments) |

### Transaction Type DB2 Module - `app/app-transaction-type-db2/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 9 | **COTRTUPC** | CICS/DB2 | Transaction Type Add/Edit (DB2 CRUD) |
| 10 | **COTRTLIC** | CICS/DB2 | Transaction Type List/Delete (DB2 cursors) |
| 11 | **COBTUPDT** | Batch/DB2 | Batch Update Transaction Types |

### VSAM-MQ Module - `app/app-vsam-mq/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 12 | **CODATE01** | MQ Service | System Date Request/Response via MQ |
| 13 | **COACCT01** | MQ Service | Account Inquiry Request/Response via MQ |

---

## Supporting Assets

### Data Files

| Location | Format | Files |
|----------|--------|-------|
| `app/data/ASCII/` | ASCII (testing) | acctdata, carddata, cardxref, custdata, dailytran, discgrp, tcatbal, trancatg, trantype |
| `app/data/EBCDIC/` | EBCDIC (mainframe) | 13 files matching VSAM datasets |

### Configuration & Definitions

| Asset | Location | Purpose |
|-------|----------|---------|
| CICS CSD | `app/csd/CARDDEMO.CSD` | CICS Resource Definitions |
| Control File | `app/ctl/REPROCT.ctl` | Reprocessing control |
| LISTCAT | `app/catlg/LISTCAT.txt` | VSAM catalog listing |
| Assembler Macros | `app/maclib/` | ASMWAIT.mac, COCDATFT.mac |
| CA7 Schedule | `app/scheduler/CardDemo.ca7` | Job scheduling (CA7) |
| Control-M Schedule | `app/scheduler/CardDemo.controlm` | Job scheduling (Control-M) |

### Scripts

| Script | Purpose |
|--------|---------|
| `scripts/run_full_batch.sh` | Submit full batch cycle via FTP |
| `scripts/run_posting.sh` | Submit posting cycle |
| `scripts/run_interest_calc.sh` | Submit interest calculation |
| `scripts/remote_compile.sh` | Compile COBOL on mainframe |
| `scripts/remote_refresh.sh` | Refresh all data files |
| `scripts/remote_submit.sh` | Submit a single JCL job |
| `scripts/upld_module.sh` | Upload source to mainframe PDS |
| `scripts/local_compile.sh` | Local compile with GnuCOBOL |

---

## Classification Summary

### By Business Domain

| Domain | Online Programs | Batch Programs | JCL Jobs |
|--------|----------------|----------------|----------|
| Account Management | 2 (COACTVWC, COACTUPC) | 1 (CBACT01C) | 2 (ACCTFILE, READACCT) |
| Card Management | 3 (COCRDLIC, COCRDSLC, COCRDUPC) | 2 (CBACT02C, CBACT03C) | 3 (CARDFILE, READCARD, READXREF) |
| Transaction Management | 3 (COTRN00C, COTRN01C, COTRN02C) | 3 (CBTRN01C, CBTRN02C, CBTRN03C) | 5 (POSTTRAN, COMBTRAN, TRANREPT, TRANBKP, TRANFILE) |
| Customer Management | 0 | 1 (CBCUS01C) | 2 (CUSTFILE, READCUST) |
| Billing / Payments | 1 (COBIL00C) | 0 | 0 |
| Reporting | 1 (CORPT00C) | 2 (CBSTM03A, CBSTM03B) | 3 (CREASTMT, PRTCATBL, TXT2PDF1) |
| Financial (Interest) | 0 | 1 (CBACT04C) | 1 (INTCALC) |
| User Administration | 4 (COUSR00C-03C) | 0 | 1 (DUSRSECJ) |
| Security / Auth | 1 (COSGN00C) | 0 | 0 |
| Navigation | 2 (COMEN01C, COADM01C) | 0 | 0 |
| Data Export/Import | 0 | 2 (CBEXPORT, CBIMPORT) | 2 (CBEXPORT, CBIMPORT) |
| Utility | 2 (CSUTLDTC, COBSWAIT) | 0 | 1 (WAITSTEP) |
| Infrastructure | 0 | 0 | 15 (file mgmt, GDG defs, etc.) |

### By Modernization Priority

| Priority | Programs | Rationale |
|----------|----------|-----------|
| **P0 - Critical** | COACTUPC, CBTRN02C, CBACT04C, COSGN00C | Core business logic, highest complexity, financial impact |
| **P1 - High** | COCRDLIC, COCRDUPC, CBSTM03A, CBTRN03C, COBIL00C | Key user-facing features, medium-high complexity |
| **P2 - Medium** | COACTVWC, COTRN00C, COTRN02C, COUSR00C, CBEXPORT, CBIMPORT | Supporting features, moderate complexity |
| **P3 - Low** | COMEN01C, COADM01C, COSGN00C, COUSR01-03C, CSUTLDTC | Navigation, simple CRUD, utilities |
| **P4 - Utility** | COBSWAIT, CBACT02C, CBACT03C, CBCUS01C, CBSTM03B | Simple readers, subroutines |
