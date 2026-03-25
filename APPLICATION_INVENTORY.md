# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Technology Stack:** COBOL/CICS/VSAM/JCL/BMS | **Platform:** IBM z/OS

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (31)](#cobol-programs-31)
3. [Copybooks (30)](#copybooks-30)
4. [BMS Screen Maps (17)](#bms-screen-maps-17)
5. [JCL Batch Jobs (38)](#jcl-batch-jobs-38)
6. [Supporting Artifacts](#supporting-artifacts)
7. [Optional Extension Modules](#optional-extension-modules)
8. [Summary Statistics](#summary-statistics)

---

## Executive Summary

CardDemo is a mainframe credit card management application simulating account management, card management, transaction processing, bill payments, and reporting. It supports two user roles: **Regular** (card operations) and **Admin** (user/transaction type management). The application consists of **31 COBOL programs**, **30 copybooks**, **17 BMS screen maps**, and **38 JCL batch jobs**, totaling approximately **20,650 lines of COBOL** and **2,100+ lines of JCL**.

---

## COBOL Programs (31)

### Online CICS Programs (17)

| # | Program ID | Lines | Description | CICS Transaction | Business Domain | Classification |
|---|-----------|-------|-------------|-----------------|-----------------|---------------|
| 1 | COSGN00C | 260 | Signon screen — authenticates users and routes to admin or regular menu | CC00 | Security | Authentication |
| 2 | COMEN01C | 308 | Main menu for regular users — dispatches to functional screens | CM00 | Navigation | Menu/Router |
| 3 | COADM01C | 288 | Admin menu for admin users — dispatches to admin functional screens | CA00 | Navigation | Menu/Router |
| 4 | COACTVWC | 941 | Account view — displays account details (balance, limits, dates) | — | Account Mgmt | Read-Only View |
| 5 | COACTUPC | 4,236 | Account update — modifies account details with full validation | — | Account Mgmt | CRUD (Update) |
| 6 | COCRDLIC | 1,459 | Credit card list — paginated browsing of card records | — | Card Mgmt | List/Browse |
| 7 | COCRDSLC | 887 | Credit card view — displays individual card details | — | Card Mgmt | Read-Only View |
| 8 | COCRDUPC | 1,560 | Credit card update — modifies card details with validation | — | Card Mgmt | CRUD (Update) |
| 9 | COTRN00C | 699 | Transaction list — paginated browsing of transactions | — | Transaction Mgmt | List/Browse |
| 10 | COTRN01C | 330 | Transaction view — displays individual transaction details | — | Transaction Mgmt | Read-Only View |
| 11 | COTRN02C | 783 | Transaction add — creates new transactions with validation | — | Transaction Mgmt | CRUD (Create) |
| 12 | CORPT00C | 649 | Transaction report — submits batch report generation via TD queue | — | Reporting | Report Trigger |
| 13 | COBIL00C | 572 | Bill payment — pays account balance in full or partial amount | — | Billing | Financial Processing |
| 14 | COUSR00C | 695 | User list — paginated browsing of user security records (Admin) | — | User Admin | List/Browse |
| 15 | COUSR01C | 299 | User add — creates new regular/admin user records (Admin) | — | User Admin | CRUD (Create) |
| 16 | COUSR02C | 414 | User update — modifies user records (Admin) | — | User Admin | CRUD (Update) |
| 17 | COUSR03C | 359 | User delete — removes user records (Admin) | — | User Admin | CRUD (Delete) |

### Batch Programs (13)

| # | Program ID | Lines | Description | Business Domain | Classification |
|---|-----------|-------|-------------|-----------------|---------------|
| 18 | CBACT01C | 430 | Read account file, write to flat/array/variable-length output files | Account Mgmt | Data Extract |
| 19 | CBACT02C | 178 | Read and print card data file | Card Mgmt | Data Validation |
| 20 | CBACT03C | 178 | Read and print account cross-reference file | Card Mgmt | Data Validation |
| 21 | CBACT04C | 652 | Interest calculator — computes interest/fees by transaction category | Financial Processing | Business Logic |
| 22 | CBCUS01C | 178 | Read and print customer data file | Customer Mgmt | Data Validation |
| 23 | CBTRN01C | 494 | Post records from daily transaction file (validation pass) | Transaction Processing | Data Loading |
| 24 | CBTRN02C | 731 | Post daily transactions — core posting with reject handling | Transaction Processing | Business Logic |
| 25 | CBTRN03C | 649 | Print transaction detail report with totals | Reporting | Report Generation |
| 26 | CBSTM03A | 924 | Print account statements from transaction data (main driver) | Reporting | Report Generation |
| 27 | CBSTM03B | 230 | Statement file processing subroutine (called by CBSTM03A) | Reporting | Subroutine |
| 28 | CBEXPORT | 582 | Export customer data for branch migration (multi-record format) | Data Migration | Data Export |
| 29 | CBIMPORT | 487 | Import customer data from branch migration export file | Data Migration | Data Import |
| 30 | COBSWAIT | 41 | Wait utility — pauses execution for specified centiseconds | Utility | System Utility |

### Utility Program (1)

| # | Program ID | Lines | Description | Business Domain | Classification |
|---|-----------|-------|-------------|-----------------|---------------|
| 31 | CSUTLDTC | 157 | Date/time conversion utility (CICS online) | Utility | Date Handling |

---

## Copybooks (30)

### Data-Structure Copybooks (19)

| # | Copybook | Record Name | Rec Len | Business Entity | Used By |
|---|---------|-------------|---------|-----------------|---------|
| 1 | CVACT01Y | ACCOUNT-RECORD | 300 | Account Master | CBACT01C, CBACT04C, CBEXPORT, CBSTM03B |
| 2 | CVACT02Y | CARD-RECORD | 150 | Card Data | CBACT02C, CBEXPORT |
| 3 | CVACT03Y | CARD-XREF-RECORD | 50 | Card Cross-Reference | CBACT03C, CBACT04C, CBEXPORT |
| 4 | CVCUS01Y | CUSTOMER-RECORD | 500 | Customer Master | CBCUS01C, CBEXPORT, CBSTM03B |
| 5 | CVCRD01Y | (Card detail fields) | — | Card Detail (online) | COCRDSLC, COCRDUPC |
| 6 | CVTRA01Y | TRAN-CAT-BAL-RECORD | 50 | Transaction Category Balance | CBACT04C |
| 7 | CVTRA02Y | DIS-GROUP-RECORD | 50 | Disclosure/Interest Rate Group | CBACT04C |
| 8 | CVTRA03Y | TRAN-TYPE-RECORD | 60 | Transaction Type | CBTRN03C |
| 9 | CVTRA04Y | TRAN-CAT-RECORD | 60 | Transaction Category | CBTRN03C |
| 10 | CVTRA05Y | TRAN-RECORD | 350 | Transaction Master | CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT |
| 11 | CVTRA06Y | DALYTRAN-RECORD | 350 | Daily Transaction | CBTRN01C, CBTRN02C |
| 12 | CVTRA07Y | TRANSACTION-DETAIL-REPORT | — | Report Layout | CBTRN03C |
| 13 | CVEXPORT | EXPORT-RECORD | 500 | Export Data (multi-type) | CBEXPORT, CBIMPORT |
| 14 | COSTM01 | TRNX-RECORD | 350 | Statement Transaction Layout | CBSTM03A, CBSTM03B |
| 15 | CSUSR01Y | SEC-USER-DATA | 80 | User Security Record | COSGN00C, COUSR00C-03C |
| 16 | CUSTREC | (Customer fields) | — | Customer Record (alt layout) | Various |
| 17 | UNUSED1Y | UNUSED-DATA | 80 | Unused/Reserved | None |
| 18 | CODATECN | CODATECN-REC | — | Date Conversion Params | CBACT01C |
| 19 | CSUTLDPY | (Date utility params) | — | Date Utility Parameters | CSUTLDTC |

### Application Control Copybooks (11)

| # | Copybook | Purpose | Used By |
|---|---------|---------|---------|
| 20 | COCOM01Y | Common area / COMMAREA structure for inter-program communication | All online CICS programs |
| 21 | COMEN02Y | Menu option definitions and program-name mappings | COMEN01C, COADM01C |
| 22 | COTTL01Y | Title/header line definitions for screens | All online CICS programs |
| 23 | COADM02Y | Admin menu option definitions | COADM01C |
| 24 | CSDAT01Y | Date data areas for CICS programs | Various online programs |
| 25 | CSLKPCDY | Lookup code definitions | Various |
| 26 | CSMSG01Y | System message area (primary) | All online CICS programs |
| 27 | CSMSG02Y | System message area (secondary) | Various online programs |
| 28 | CSSETATY | Screen attribute settings (BMS field attributes) | Various online programs |
| 29 | CSSTRPFY | String/padding/formatting utility fields | Various |
| 30 | CSUTLDWY | Date utility working-storage areas | CSUTLDTC |

---

## BMS Screen Maps (17)

Each BMS map defines a 3270 terminal screen layout. A corresponding generated copybook exists in `app/cpy-bms/`.

| # | BMS Map | Screen Title | Lines | Associated Program | Purpose |
|---|---------|-------------|-------|--------------------|---------|
| 1 | COSGN00 | Login Screen | 210 | COSGN00C | User authentication (User ID + Password) |
| 2 | COMEN01 | Main Menu Screen | 167 | COMEN01C | Regular user main menu navigation |
| 3 | COADM01 | Admin Menu Screen | 167 | COADM01C | Admin user main menu navigation |
| 4 | COACTVW | Account View Screen | 166 | COACTVWC | Display account details |
| 5 | COACTUP | Account Update Screen | 163 | COACTUPC | Edit account details |
| 6 | COCRDLI | Card Listing Screen | 344 | COCRDLIC | Paginated list of credit cards |
| 7 | COCRDSL | Card Selection Screen | 157 | COCRDSLC | View selected card details |
| 8 | COCRDUP | Card Update Screen | 172 | COCRDUPC | Edit card details |
| 9 | COTRN00 | Transaction List | 464 | COTRN00C | Paginated list of transactions |
| 10 | COTRN01 | Transaction View | 273 | COTRN01C | View individual transaction |
| 11 | COTRN02 | Transaction Add | 307 | COTRN02C | Add a new transaction |
| 12 | CORPT00 | Transaction Reports | 231 | CORPT00C | Request report generation |
| 13 | COBIL00 | Bill Payment | 141 | COBIL00C | Pay account balance |
| 14 | COUSR00 | List Users | 463 | COUSR00C | Paginated list of users (Admin) |
| 15 | COUSR01 | Add User | 164 | COUSR01C | Create new user (Admin) |
| 16 | COUSR02 | Update User | 169 | COUSR02C | Edit user details (Admin) |
| 17 | COUSR03 | Delete User | 153 | COUSR03C | Delete user (Admin) |

---

## JCL Batch Jobs (38)

### Data File Definition & Loading (12)

| # | JCL Job | Lines | Description | Program(s) Executed | VSAM Files Affected |
|---|---------|-------|-------------|--------------------|--------------------|
| 1 | ACCTFILE | 63 | Refresh account master VSAM file from PS | IDCAMS, IEBGENER, IDCAMS | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | 66 | Refresh card master VSAM file from PS | IDCAMS, IEBGENER, IDCAMS | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | 66 | Refresh customer master VSAM file from PS | IDCAMS, IEBGENER, IDCAMS | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | 106 | Define/load card cross-reference + AIX | IDCAMS (×6) | CARDXREF.VSAM.KSDS + AIX |
| 5 | TRANFILE | 125 | Define transaction master VSAM + AIX | SDSF, IDCAMS (×6) | TRANSACT.VSAM.KSDS + AIX |
| 6 | DUSRSECJ | 92 | Load user security VSAM file from PS | IEFBR14, IEBGENER, IDCAMS (×3) | USRSEC.VSAM.KSDS |
| 7 | DISCGRP | 65 | Define/load disclosure group VSAM | IDCAMS (×3) | DISCGRP.VSAM.KSDS |
| 8 | TCATBALF | 65 | Define transaction category balance VSAM | IDCAMS (×3) | TCATBALF.VSAM.KSDS |
| 9 | TRANCATG | 65 | Define transaction category VSAM | IDCAMS (×3) | TRANCATG.VSAM.KSDS |
| 10 | TRANTYPE | 65 | Define transaction type VSAM | IDCAMS (×3) | TRANTYPE.VSAM.KSDS |
| 11 | ESDSRRDS | 124 | Define ESDS/RRDS alternate file formats | IEFBR14, IEBGENER, IDCAMS (×4) | USRSEC.VSAM.ESDS/RRDS |
| 12 | DEFCUST | — | Define customer-related structures | IDCAMS | Various |

### Batch Processing (8)

| # | JCL Job | Lines | Description | Program(s) Executed | Input → Output |
|---|---------|-------|-------------|--------------------|--------------------|
| 13 | POSTTRAN | 45 | Core transaction posting | CBTRN02C | DALYTRAN.PS → TRANSACT.VSAM, TCATBALF.VSAM |
| 14 | INTCALC | 44 | Interest calculation | CBACT04C | TCATBALF → ACCTDATA (updated), SYSTRAN |
| 15 | COMBTRAN | 52 | Combine transaction backups | SORT, IDCAMS | TRANSACT.BKUP + SYSTRAN → TRANSACT.COMBINED |
| 16 | TRANBKP | 71 | Backup and clear transaction master | REPROC, IDCAMS | TRANSACT.VSAM → TRANSACT.BKUP GDG |
| 17 | TRANREPT | 84 | Generate transaction detail report | REPROC, SORT, CBTRN03C | TRANSACT.VSAM → TRANREPT (report) |
| 18 | CREASTMT | 97 | Create account statements | IDCAMS, SORT, CBSTM03A | TRANSACT + ACCT + CUST → Statement HTML/PS |
| 19 | CBEXPORT | 42 | Export all data for migration | CBEXPORT | All VSAM files → EXPORT.DATA |
| 20 | CBIMPORT | 42 | Import migration data | CBIMPORT | EXPORT.DATA → Individual import files |

### File Operations (4)

| # | JCL Job | Lines | Description | Program(s) Executed |
|---|---------|-------|-------------|---------------------|
| 21 | CLOSEFIL | 34 | Close CICS-managed VSAM files for batch | SDSF |
| 22 | OPENFIL | 34 | Reopen CICS-managed VSAM files after batch | SDSF |
| 23 | DALYREJS | — | Process daily rejects | IDCAMS |
| 24 | DEFGDGB | 63 | Define GDG bases for transaction backups | IDCAMS |

### Read/Validation Jobs (5)

| # | JCL Job | Lines | Description | Program(s) Executed |
|---|---------|-------|-------------|---------------------|
| 25 | READACCT | 50 | Read/validate account file | IEFBR14, CBACT01C |
| 26 | READCARD | 31 | Read/validate card file | CBACT02C |
| 27 | READCUST | 30 | Read/validate customer file | CBCUS01C |
| 28 | READXREF | 31 | Read/validate cross-reference file | CBACT03C |
| 29 | PRTCATBL | 66 | Print transaction category balance file | REPROC, SORT, IEBPTPCH |

### Index & GDG Management (4)

| # | JCL Job | Lines | Description | Program(s) Executed |
|---|---------|-------|-------------|---------------------|
| 30 | TRANIDX | 58 | Define alternate index on transaction master | IDCAMS (×3) |
| 31 | DEFGDGD | 94 | Define GDG for disclosure/tran type backups | IDCAMS, IEBGENER |
| 32 | REPTFILE | 32 | Define GDG for report file | IDCAMS |
| 33 | CBADMCDJ | — | Admin card demo job | Various |

### Utility & Miscellaneous (5)

| # | JCL Job | Lines | Description | Program(s) Executed |
|---|---------|-------|-------------|---------------------|
| 34 | WAITSTEP | 27 | Execute wait utility | COBSWAIT |
| 35 | FTPJCL | 42 | FTP file transfer job | FTP |
| 36 | INTRDRJ1 | 19 | Internal reader — trigger INTRDRJ2 | IDCAMS, IEBGENER |
| 37 | INTRDRJ2 | 14 | Internal reader — triggered by INTRDRJ1 | IDCAMS |
| 38 | TXT2PDF1 | 41 | Convert text output to PDF | IKJEFT1B |

---

## Supporting Artifacts

### Assembler Programs (2)

| Program | Description |
|---------|-------------|
| COBDATFT.asm | Date formatting — called by CBACT01C for date conversion |
| MVSWAIT.asm | MVS wait service — underlying wait implementation for COBSWAIT |

### JCL Procedures (2)

| Procedure | Description |
|-----------|-------------|
| REPROC.prc | Reusable REPRO procedure for VSAM-to-sequential unloads |
| TRANREPT.prc | Transaction report generation procedure |

### Scheduler Configurations (2)

| File | Description |
|------|-------------|
| CardDemo.ca7 | CA-7 job scheduling definitions for batch cycle |
| CardDemo.controlm | Control-M job scheduling definitions for batch cycle |

### Other

| Directory | Contents |
|-----------|----------|
| app/ctl/ | REPROCT.ctl — Control file for REPRO operations |
| app/csd/ | CARDDEMO.CSD — CICS resource definitions (programs, files, transactions) |
| app/catlg/ | Catalog listings for VSAM file structures |
| app/maclib/ | Assembler macros |
| app/data/ASCII/ | Sample test data (acctdata, carddata, custdata, dailytran, etc.) |
| app/data/EBCDIC/ | EBCDIC-encoded data for mainframe upload |

---

## Optional Extension Modules

### 1. Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for authorization processing.

| Program | Description |
|---------|-------------|
| COPAUA0C | MQ trigger monitor for authorization requests |
| COPAUS0C | Authorization summary display |
| COPAUS1C | Authorization detail display |
| COPAUS2C | Fraud marking — writes to DB2 |
| CBPAUP0C | Batch purge of processed authorizations |
| + 3 more | Additional authorization support programs |

### 2. Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based CRUD for transaction type management.

| Program | Description |
|---------|-------------|
| COTRTUPC | Add/edit transaction types (DB2 embedded SQL) |
| COTRTLIC | List/delete transaction types (DB2 cursors) |
| COBTUPDT | Batch update of transaction types |

### 3. VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response for system queries.

| Program | Description |
|---------|-------------|
| CODATE01 | System date request via MQ (channel CDRD) |
| COACCT01 | Account inquiry via MQ (channel CDRA) |

---

## Summary Statistics

| Category | Count | Total Lines |
|----------|-------|-------------|
| COBOL Programs (core) | 31 | ~20,650 |
| Copybooks (data) | 30 | ~2,500 |
| BMS Maps | 17 | ~4,100 |
| BMS-Generated Copybooks | 17 | — |
| JCL Jobs | 38 | ~2,100 |
| Assembler Programs | 2 | ~200 |
| JCL Procedures | 2 | ~350 |
| Optional Module Programs | ~13 | — |
| **Total Core Artifacts** | **137** | **~30,000** |

### Classification Breakdown

| Classification | Programs |
|---------------|----------|
| Authentication | 1 (COSGN00C) |
| Menu/Router | 2 (COMEN01C, COADM01C) |
| CRUD Operations | 8 (Account, Card, Transaction, User ops) |
| List/Browse | 4 (COCRDLIC, COTRN00C, COUSR00C, COACTVWC) |
| Financial Processing | 2 (COBIL00C, CBACT04C) |
| Transaction Processing | 3 (CBTRN01C, CBTRN02C, CORPT00C) |
| Report Generation | 3 (CBTRN03C, CBSTM03A, CBSTM03B) |
| Data Extract/Validate | 5 (CBACT01C-03C, CBCUS01C, CBEXPORT) |
| Data Import | 1 (CBIMPORT) |
| Utility | 2 (COBSWAIT, CSUTLDTC) |
