# APPLICATION INVENTORY - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
> **Total Artifacts:** 31 COBOL Programs + 30 Copybooks + 17 BMS Maps + 38 JCL Jobs + 13 Optional-Module Programs

---

## Table of Contents

1. [COBOL Programs (Online CICS)](#1-cobol-programs--online-cics)
2. [COBOL Programs (Batch)](#2-cobol-programs--batch)
3. [Copybooks (Data Structures)](#3-copybooks--data-structures)
4. [BMS Screen Maps](#4-bms-screen-maps)
5. [BMS-Generated Copybooks](#5-bms-generated-copybooks)
6. [JCL Batch Jobs](#6-jcl-batch-jobs)
7. [Optional Modules](#7-optional-modules)
8. [Supporting Artifacts](#8-supporting-artifacts)
9. [Summary Statistics](#9-summary-statistics)

---

## 1. COBOL Programs -- Online CICS

These programs run under CICS and serve the interactive 3270 terminal UI.

| # | Program | File | Lines | Transaction ID | Function | Business Domain | Complexity |
|---|---------|------|------:|----------------|----------|-----------------|------------|
| 1 | **COSGN00C** | `app/cbl/COSGN00C.cbl` | 260 | CC00 | Sign-on / Authentication | Security | Low |
| 2 | **COMEN01C** | `app/cbl/COMEN01C.cbl` | 308 | CM00 | Main Menu (Regular Users) | Navigation | Low |
| 3 | **COADM01C** | `app/cbl/COADM01C.cbl` | 288 | CA00 | Admin Menu | Navigation / Admin | Low |
| 4 | **COACTVWC** | `app/cbl/COACTVWC.cbl` | 941 | CAVW | Account View (read-only) | Account Mgmt | Medium |
| 5 | **COACTUPC** | `app/cbl/COACTUPC.cbl` | 4,236 | CAUP | Account Update (full CRUD) | Account Mgmt | **Very High** |
| 6 | **COCRDLIC** | `app/cbl/COCRDLIC.cbl` | 1,459 | CCLI | Credit Card List | Card Mgmt | High |
| 7 | **COCRDSLC** | `app/cbl/COCRDSLC.cbl` | 887 | CCDL | Credit Card Detail View | Card Mgmt | Medium |
| 8 | **COCRDUPC** | `app/cbl/COCRDUPC.cbl` | 1,560 | CCUP | Credit Card Update | Card Mgmt | High |
| 9 | **COTRN00C** | `app/cbl/COTRN00C.cbl` | 699 | CT00 | Transaction List | Transactions | Medium |
| 10 | **COTRN01C** | `app/cbl/COTRN01C.cbl` | 330 | CT01 | Transaction View (single) | Transactions | Low |
| 11 | **COTRN02C** | `app/cbl/COTRN02C.cbl` | 783 | CT02 | Transaction Add (new) | Transactions | Medium |
| 12 | **CORPT00C** | `app/cbl/CORPT00C.cbl` | 649 | CR00 | Transaction Report Submission | Reporting | Medium |
| 13 | **COBIL00C** | `app/cbl/COBIL00C.cbl` | 572 | CB00 | Bill Payment | Billing | Medium |
| 14 | **COUSR00C** | `app/cbl/COUSR00C.cbl` | 695 | CU00 | User List (Admin) | User Admin | Medium |
| 15 | **COUSR01C** | `app/cbl/COUSR01C.cbl` | 299 | CU01 | User Add (Admin) | User Admin | Low |
| 16 | **COUSR02C** | `app/cbl/COUSR02C.cbl` | 414 | CU02 | User Update (Admin) | User Admin | Medium |
| 17 | **COUSR03C** | `app/cbl/COUSR03C.cbl` | 359 | CU03 | User Delete (Admin) | User Admin | Low |

**Online CICS Subtotal:** 17 programs, 14,739 lines

---

## 2. COBOL Programs -- Batch

These programs run as batch jobs via JCL, processing files and generating reports.

| # | Program | File | Lines | Function | Business Domain | Complexity |
|---|---------|------|------:|----------|-----------------|------------|
| 18 | **CBACT01C** | `app/cbl/CBACT01C.cbl` | 430 | Read account file, write outputs | Account Mgmt | Medium |
| 19 | **CBACT02C** | `app/cbl/CBACT02C.cbl` | 178 | Read and print card data | Card Mgmt | Low |
| 20 | **CBACT03C** | `app/cbl/CBACT03C.cbl` | 178 | Read and print cross-reference data | Card Mgmt | Low |
| 21 | **CBACT04C** | `app/cbl/CBACT04C.cbl` | 652 | Interest calculation engine | **Financial Calc** | **High** |
| 22 | **CBCUS01C** | `app/cbl/CBCUS01C.cbl` | 178 | Read and print customer data | Customer Mgmt | Low |
| 23 | **CBTRN01C** | `app/cbl/CBTRN01C.cbl` | 494 | Post daily transactions (phase 1) | Transaction Processing | Medium |
| 24 | **CBTRN02C** | `app/cbl/CBTRN02C.cbl` | 731 | Post daily transactions (core posting) | **Transaction Processing** | **High** |
| 25 | **CBTRN03C** | `app/cbl/CBTRN03C.cbl` | 649 | Print transaction detail report | Reporting | Medium |
| 26 | **CBSTM03A** | `app/cbl/CBSTM03A.CBL` | 924 | Statement generation (main driver) | **Statements** | **High** |
| 27 | **CBSTM03B** | `app/cbl/CBSTM03B.CBL` | 230 | Statement generation (file I/O helper) | Statements | Low |
| 28 | **CBEXPORT** | `app/cbl/CBEXPORT.cbl` | 582 | Data export (VSAM to sequential) | Data Migration | Medium |
| 29 | **CBIMPORT** | `app/cbl/CBIMPORT.cbl` | 487 | Data import (sequential to VSAM) | Data Migration | Medium |
| 30 | **COBSWAIT** | `app/cbl/COBSWAIT.cbl` | 41 | Wait utility (centiseconds) | Utility | Trivial |
| 31 | **CSUTLDTC** | `app/cbl/CSUTLDTC.cbl` | 157 | Date validation (calls CEEDAYS) | Utility | Low |

**Batch Subtotal:** 14 programs, 5,911 lines

---

## 3. Copybooks -- Data Structures

Copybooks define shared record layouts included via `COPY` in programs.

| # | Copybook | File | Domain | Record Length | Description |
|---|----------|------|--------|-------------:|-------------|
| 1 | **CVACT01Y** | `app/cpy/CVACT01Y.cpy` | Account | 300 | Account master record layout |
| 2 | **CVACT02Y** | `app/cpy/CVACT02Y.cpy` | Card | 150 | Card data record layout |
| 3 | **CVACT03Y** | `app/cpy/CVACT03Y.cpy` | Card | 50 | Card-to-account cross-reference |
| 4 | **CVCUS01Y** | `app/cpy/CVCUS01Y.cpy` | Customer | 500 | Customer master record |
| 5 | **CVCRD01Y** | `app/cpy/CVCRD01Y.cpy` | Card | -- | Card work area (screen processing) |
| 6 | **CVTRA01Y** | `app/cpy/CVTRA01Y.cpy` | Transaction | 50 | Transaction category balance record |
| 7 | **CVTRA02Y** | `app/cpy/CVTRA02Y.cpy` | Transaction | 50 | Disclosure group record |
| 8 | **CVTRA03Y** | `app/cpy/CVTRA03Y.cpy` | Transaction | 60 | Transaction type record |
| 9 | **CVTRA04Y** | `app/cpy/CVTRA04Y.cpy` | Transaction | 60 | Transaction category type record |
| 10 | **CVTRA05Y** | `app/cpy/CVTRA05Y.cpy` | Transaction | 350 | Online transaction record (main) |
| 11 | **CVTRA06Y** | `app/cpy/CVTRA06Y.cpy` | Transaction | 350 | Daily transaction record |
| 12 | **CVTRA07Y** | `app/cpy/CVTRA07Y.cpy` | Reporting | -- | Transaction report headers/totals |
| 13 | **COSTM01** | `app/cpy/COSTM01.CPY` | Statement | 350 | Transaction record (statement key layout) |
| 14 | **CVEXPORT** | `app/cpy/CVEXPORT.cpy` | Export | -- | Export record layout (multi-entity) |
| 15 | **CUSTREC** | `app/cpy/CUSTREC.cpy` | Customer | -- | Alternate customer record definition |
| 16 | **COCOM01Y** | `app/cpy/COCOM01Y.cpy` | Infrastructure | -- | Application COMMAREA (inter-program communication) |
| 17 | **COADM02Y** | `app/cpy/COADM02Y.cpy` | Navigation | -- | Admin menu option definitions |
| 18 | **COMEN02Y** | `app/cpy/COMEN02Y.cpy` | Navigation | -- | Main menu option definitions |
| 19 | **COTTL01Y** | `app/cpy/COTTL01Y.cpy` | UI | -- | Screen title/header layout |
| 20 | **CSDAT01Y** | `app/cpy/CSDAT01Y.cpy` | Utility | -- | Current date work area |
| 21 | **CSLKPCDY** | `app/cpy/CSLKPCDY.cpy` | Utility | -- | Lookup code definitions |
| 22 | **CSMSG01Y** | `app/cpy/CSMSG01Y.cpy` | UI | -- | Common message definitions |
| 23 | **CSMSG02Y** | `app/cpy/CSMSG02Y.cpy` | Error | -- | Abend/error message definitions |
| 24 | **CSUSR01Y** | `app/cpy/CSUSR01Y.cpy` | Security | 80 | User security record layout |
| 25 | **CSSETATY** | `app/cpy/CSSETATY.cpy` | UI | -- | Screen attribute settings |
| 26 | **CSSTRPFY** | `app/cpy/CSSTRPFY.cpy` | Utility | -- | String strip/padding function |
| 27 | **CSUTLDPY** | `app/cpy/CSUTLDPY.cpy` | Utility | -- | Date utility parameters |
| 28 | **CSUTLDWY** | `app/cpy/CSUTLDWY.cpy` | Utility | -- | Date edit work variables |
| 29 | **CODATECN** | `app/cpy/CODATECN.cpy` | Utility | -- | Date conversion record |
| 30 | **UNUSED1Y** | `app/cpy/UNUSED1Y.cpy` | Unused | 80 | Unused/placeholder record |

---

## 4. BMS Screen Maps

BMS (Basic Mapping Support) maps define the 3270 terminal screen layouts.

| # | Map | File | Associated Program | Screen Purpose |
|---|-----|------|--------------------|----------------|
| 1 | **COSGN00** | `app/bms/COSGN00.bms` | COSGN00C | Sign-on screen |
| 2 | **COMEN01** | `app/bms/COMEN01.bms` | COMEN01C | Main menu |
| 3 | **COADM01** | `app/bms/COADM01.bms` | COADM01C | Admin menu |
| 4 | **COACTVW** | `app/bms/COACTVW.bms` | COACTVWC | Account view |
| 5 | **COACTUP** | `app/bms/COACTUP.bms` | COACTUPC | Account update form |
| 6 | **COCRDLI** | `app/bms/COCRDLI.bms` | COCRDLIC | Credit card list |
| 7 | **COCRDSL** | `app/bms/COCRDSL.bms` | COCRDSLC | Credit card detail |
| 8 | **COCRDUP** | `app/bms/COCRDUP.bms` | COCRDUPC | Credit card update form |
| 9 | **COTRN00** | `app/bms/COTRN00.bms` | COTRN00C | Transaction list |
| 10 | **COTRN01** | `app/bms/COTRN01.bms` | COTRN01C | Transaction view |
| 11 | **COTRN02** | `app/bms/COTRN02.bms` | COTRN02C | Transaction add form |
| 12 | **CORPT00** | `app/bms/CORPT00.bms` | CORPT00C | Report submission |
| 13 | **COBIL00** | `app/bms/COBIL00.bms` | COBIL00C | Bill payment |
| 14 | **COUSR00** | `app/bms/COUSR00.bms` | COUSR00C | User list |
| 15 | **COUSR01** | `app/bms/COUSR01.bms` | COUSR01C | User add form |
| 16 | **COUSR02** | `app/bms/COUSR02.bms` | COUSR02C | User update form |
| 17 | **COUSR03** | `app/bms/COUSR03.bms` | COUSR03C | User delete confirmation |

---

## 5. BMS-Generated Copybooks

Auto-generated copybooks from BMS map compilation (located in `app/cpy-bms/`).

| # | Copybook | Used By | Purpose |
|---|----------|---------|---------|
| 1 | COSGN00.CPY | COSGN00C | Sign-on screen field mappings |
| 2 | COMEN01.CPY | COMEN01C | Main menu field mappings |
| 3 | COADM01.CPY | COADM01C | Admin menu field mappings |
| 4 | COACTVW.CPY | COACTVWC | Account view field mappings |
| 5 | COACTUP.CPY | COACTUPC | Account update field mappings |
| 6 | COCRDLI.CPY | COCRDLIC | Card list field mappings |
| 7 | COCRDSL.CPY | COCRDSLC | Card detail field mappings |
| 8 | COCRDUP.CPY | COCRDUPC | Card update field mappings |
| 9 | COTRN00.CPY | COTRN00C | Transaction list field mappings |
| 10 | COTRN01.CPY | COTRN01C | Transaction view field mappings |
| 11 | COTRN02.CPY | COTRN02C | Transaction add field mappings |
| 12 | CORPT00.CPY | CORPT00C | Report screen field mappings |
| 13 | COBIL00.CPY | COBIL00C | Bill payment field mappings |
| 14 | COUSR00.CPY | COUSR00C | User list field mappings |
| 15 | COUSR01.CPY | COUSR01C | User add field mappings |
| 16 | COUSR02.CPY | COUSR02C | User update field mappings |
| 17 | COUSR03.CPY | COUSR03C | User delete field mappings |

---

## 6. JCL Batch Jobs

| # | Job | File | Classification | Function | Programs Executed |
|---|-----|------|---------------|----------|-------------------|
| 1 | **DUSRSECJ** | `app/jcl/DUSRSECJ.jcl` | Data Load | Load user security VSAM from PS | IEBGENER, IDCAMS |
| 2 | **ACCTFILE** | `app/jcl/ACCTFILE.jcl` | Data Load | Define and load account master VSAM | IDCAMS |
| 3 | **CARDFILE** | `app/jcl/CARDFILE.jcl` | Data Load | Define and load card master VSAM (with AIX) | SDSF, IDCAMS |
| 4 | **CUSTFILE** | `app/jcl/CUSTFILE.jcl` | Data Load | Define and load customer master VSAM | SDSF, IDCAMS |
| 5 | **XREFFILE** | `app/jcl/XREFFILE.jcl` | Data Load | Define card cross-reference VSAM (with AIX) | IDCAMS |
| 6 | **TRANFILE** | `app/jcl/TRANFILE.jcl` | Data Load | Define and load transaction master VSAM (with AIX) | SDSF, IDCAMS |
| 7 | **TRANTYPE** | `app/jcl/TRANTYPE.jcl` | Data Load | Define transaction type VSAM | IDCAMS |
| 8 | **TRANCATG** | `app/jcl/TRANCATG.jcl` | Data Load | Define transaction category VSAM | IDCAMS |
| 9 | **TCATBALF** | `app/jcl/TCATBALF.jcl` | Data Load | Define transaction category balance VSAM | IDCAMS |
| 10 | **DISCGRP** | `app/jcl/DISCGRP.jcl` | Data Load | Define disclosure group VSAM | IDCAMS |
| 11 | **REPTFILE** | `app/jcl/REPTFILE.jcl` | Data Load | Define daily rejection report VSAM | IDCAMS |
| 12 | **DEFCUST** | `app/jcl/DEFCUST.jcl` | Data Load | Define alternate customer VSAM | IDCAMS |
| 13 | **ESDSRRDS** | `app/jcl/ESDSRRDS.jcl` | Data Load | Define ESDS/RRDS user security VSAM variants | IEBGENER, IDCAMS |
| 14 | **DEFGDGB** | `app/jcl/DEFGDGB.jcl` | Infrastructure | Define GDG base catalogs | IDCAMS |
| 15 | **DEFGDGD** | `app/jcl/DEFGDGD.jcl` | Infrastructure | Define GDG data sets (transaction backups) | IDCAMS, IEBGENER |
| 16 | **CLOSEFIL** | `app/jcl/CLOSEFIL.jcl` | Batch Cycle | Close CICS files for batch processing | SDSF |
| 17 | **OPENFIL** | `app/jcl/OPENFIL.jcl` | Batch Cycle | Open CICS files after batch processing | SDSF |
| 18 | **POSTTRAN** | `app/jcl/POSTTRAN.jcl` | **Core Processing** | Post daily transactions to master | **CBTRN02C** |
| 19 | **INTCALC** | `app/jcl/INTCALC.jcl` | **Core Processing** | Calculate interest on accounts | **CBACT04C** |
| 20 | **COMBTRAN** | `app/jcl/COMBTRAN.jcl` | Batch Cycle | Combine daily + master transactions | SORT, IDCAMS |
| 21 | **CREASTMT** | `app/jcl/CREASTMT.JCL` | **Core Processing** | Generate customer statements | SORT, IDCAMS, **CBSTM03A** |
| 22 | **TRANBKP** | `app/jcl/TRANBKP.jcl` | Batch Cycle | Backup transaction data (GDG) | IDCAMS |
| 23 | **TRANIDX** | `app/jcl/TRANIDX.jcl` | Batch Cycle | Define/build transaction alternate index | IDCAMS |
| 24 | **TRANREPT** | `app/jcl/TRANREPT.jcl` | Reporting | Generate transaction detail report | SORT, **CBTRN03C** |
| 25 | **DALYREJS** | `app/jcl/DALYREJS.jcl` | Reporting | Define daily rejection report file | IDCAMS |
| 26 | **PRTCATBL** | `app/jcl/PRTCATBL.jcl` | Reporting | Print category balance report | SORT |
| 27 | **READACCT** | `app/jcl/READACCT.jcl` | Diagnostic | Read and print account file | **CBACT01C** |
| 28 | **READCARD** | `app/jcl/READCARD.jcl` | Diagnostic | Read and print card file | **CBACT02C** |
| 29 | **READCUST** | `app/jcl/READCUST.jcl` | Diagnostic | Read and print customer file | **CBCUS01C** |
| 30 | **READXREF** | `app/jcl/READXREF.jcl` | Diagnostic | Read and print cross-ref file | **CBACT03C** |
| 31 | **CBEXPORT** | `app/jcl/CBEXPORT.jcl` | Data Migration | Export VSAM data to sequential | IDCAMS, **CBEXPORT** |
| 32 | **CBIMPORT** | `app/jcl/CBIMPORT.jcl` | Data Migration | Import sequential data to VSAM | **CBIMPORT** |
| 33 | **CBADMCDJ** | `app/jcl/CBADMCDJ.jcl` | Infrastructure | Administer CICS CSD definitions | DFHCSDUP |
| 34 | **WAITSTEP** | `app/jcl/WAITSTEP.jcl` | Utility | Wait for specified centiseconds | **COBSWAIT** |
| 35 | **FTPJCL** | `app/jcl/FTPJCL.JCL` | Utility | FTP file transfer | FTP |
| 36 | **INTRDRJ1** | `app/jcl/INTRDRJ1.JCL` | Utility | Internal reader job trigger (chain) | IDCAMS, IEBGENER |
| 37 | **INTRDRJ2** | `app/jcl/INTRDRJ2.JCL` | Utility | Internal reader chained job | IDCAMS |
| 38 | **TXT2PDF1** | `app/jcl/TXT2PDF1.JCL` | Utility | Convert text statement to PDF | IKJEFT1B |

---

## 7. Optional Modules

### 7a. Authorization Module (IMS/DB2/MQ) -- `app/app-authorization-ims-db2-mq/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | **COPAUA0C** | CICS | MQ trigger -- receive authorization request |
| 2 | **COPAUS0C** | CICS | Display payment authorization summary |
| 3 | **COPAUS1C** | CICS | Display payment authorization details |
| 4 | **COPAUS2C** | CICS | Mark transaction as fraud (writes to DB2) |
| 5 | **CBPAUP0C** | Batch | Batch purge of old authorization records |
| 6 | **DBUNLDGS** | Batch | DB2 unload utility |
| 7 | **PAUDBLOD** | Batch | DB2 load for payment authorization |
| 8 | **PAUDBUNL** | Batch | DB2 unload for payment authorization |

### 7b. Transaction Type DB2 Module -- `app/app-transaction-type-db2/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 9 | **COTRTUPC** | CICS | Add/edit transaction types (DB2 CRUD) |
| 10 | **COTRTLIC** | CICS | List/delete transaction types (DB2 cursors) |
| 11 | **COBTUPDT** | Batch | Batch update of transaction types in DB2 |

### 7c. VSAM-MQ Module -- `app/app-vsam-mq/`

| # | Program | Type | Function |
|---|---------|------|----------|
| 12 | **CODATE01** | CICS | MQ request/response for system date |
| 13 | **COACCT01** | CICS | MQ request/response for account inquiry |

---

## 8. Supporting Artifacts

### Assembler Programs (`app/asm/`)

| Program | Function |
|---------|----------|
| **MVSWAIT** | Low-level wait routine (called by COBSWAIT) |
| **COBDATFT** | Date formatting assembler routine (called by CBACT01C) |

### JCL Procedures (`app/proc/`)

| Procedure | Function |
|-----------|----------|
| **REPROC.prc** | Reusable report procedure |
| **TRANREPT.prc** | Transaction report procedure |

### Scheduler Configurations (`app/scheduler/`)

| File | Platform | Function |
|------|----------|----------|
| **CardDemo.ca7** | CA-7 | Job scheduling definitions |
| **CardDemo.controlm** | Control-M | Job scheduling definitions |

### Other Supporting Files

| Directory | Contents |
|-----------|----------|
| `app/csd/` | CICS resource definitions (CARDDEMO.CSD) |
| `app/ctl/` | Control file (REPROCT.ctl) |
| `app/catlg/` | VSAM catalog listing (LISTCAT.txt) |
| `app/maclib/` | Assembler macros (ASMWAIT.mac, COCDATFT.mac) |
| `app/data/ASCII/` | Sample data files in ASCII format |
| `app/data/EBCDIC/` | Sample data files in EBCDIC format |

---

## 9. Summary Statistics

| Category | Count | Total Lines |
|----------|------:|------------:|
| Online CICS Programs | 17 | 14,739 |
| Batch Programs | 14 | 5,911 |
| **Core Programs Total** | **31** | **20,650** |
| Copybooks (data structures) | 30 | ~1,500 |
| BMS Maps | 17 | -- |
| BMS-Generated Copybooks | 17 | -- |
| JCL Jobs | 38 | -- |
| Optional Module Programs | 13 | -- |
| Assembler Programs | 2 | -- |
| JCL Procedures | 2 | -- |
| **Grand Total Artifacts** | **150+** | -- |

### Classification Breakdown

| Classification | Programs |
|---------------|---------|
| Security / Auth | COSGN00C |
| Navigation | COMEN01C, COADM01C |
| Account Management | COACTVWC, COACTUPC, CBACT01C |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C |
| Transaction Processing | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C |
| Financial Calculations | CBACT04C |
| Billing | COBIL00C |
| Reporting / Statements | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B |
| User Administration | COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| Data Migration | CBEXPORT, CBIMPORT |
| Utility | COBSWAIT, CSUTLDTC, CBCUS01C |
