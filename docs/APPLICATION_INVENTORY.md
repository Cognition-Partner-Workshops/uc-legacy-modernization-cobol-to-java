# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM z/OS (CICS/VSAM/JCL)

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **31 COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **17 BMS-generated copybooks**, and **38 JCL batch jobs** in its core module. Three optional extension modules add **13 additional programs** with IMS/DB2/MQ capabilities. The application supports two user roles (Regular and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (16 programs)

| # | Program | Lines | CICS Txn | Function | Business Domain |
|---|---------|-------|----------|----------|-----------------|
| 1 | **COSGN00C** | 260 | CC00 | User sign-on / authentication | Security |
| 2 | **COMEN01C** | 308 | CM00 | Main menu for regular users | Navigation |
| 3 | **COADM01C** | 288 | CA00 | Admin menu for admin users | Navigation |
| 4 | **COACTVWC** | 941 | CA01 | View account details | Account Mgmt |
| 5 | **COACTUPC** | 4,236 | CA02 | Update account information | Account Mgmt |
| 6 | **COCRDLIC** | 1,459 | CC01 | List credit cards (with paging) | Card Mgmt |
| 7 | **COCRDSLC** | 887 | CC02 | View credit card details | Card Mgmt |
| 8 | **COCRDUPC** | 1,560 | CC03 | Update credit card information | Card Mgmt |
| 9 | **COTRN00C** | 699 | CT00 | List transactions (with paging) | Transaction Mgmt |
| 10 | **COTRN01C** | 330 | CT01 | View transaction details | Transaction Mgmt |
| 11 | **COTRN02C** | 783 | CT02 | Add new transaction | Transaction Mgmt |
| 12 | **CORPT00C** | 649 | CR00 | Submit transaction report (via TDQ) | Reporting |
| 13 | **COBIL00C** | 572 | CB00 | Bill payment processing | Bill Payment |
| 14 | **COUSR00C** | 695 | CU00 | List users (admin only, with paging) | User Admin |
| 15 | **COUSR01C** | 299 | CU01 | Add new user | User Admin |
| 16 | **COUSR02C** | 414 | CU02 | Update existing user | User Admin |

### 1.2 Online CICS Programs - Additional

| # | Program | Lines | CICS Txn | Function | Business Domain |
|---|---------|-------|----------|----------|-----------------|
| 17 | **COUSR03C** | 359 | CU03 | Delete user | User Admin |

### 1.3 Batch Programs (13 programs)

| # | Program | Lines | Function | Business Domain |
|---|---------|-------|----------|-----------------|
| 18 | **CBACT01C** | 430 | Read account file and write outputs | Account Processing |
| 19 | **CBACT02C** | 178 | Read and print card data file | Card Processing |
| 20 | **CBACT03C** | 178 | Read and print cross-reference file | Data Validation |
| 21 | **CBACT04C** | 652 | Interest calculation engine | Financial Calc |
| 22 | **CBCUS01C** | 178 | Read and print customer data file | Customer Processing |
| 23 | **CBTRN01C** | 494 | Post daily transactions (simple) | Transaction Posting |
| 24 | **CBTRN02C** | 731 | Post daily transactions (with validation & reject) | Transaction Posting |
| 25 | **CBTRN03C** | 649 | Print transaction detail report | Reporting |
| 26 | **CBSTM03A** | 924 | Generate account statements (text + HTML) | Statement Gen |
| 27 | **CBSTM03B** | 230 | Subroutine for statement file I/O | Statement Gen |
| 28 | **CBEXPORT** | 582 | Export customer data for branch migration | Data Migration |
| 29 | **CBIMPORT** | 487 | Import customer data from export file | Data Migration |
| 30 | **COBSWAIT** | 41 | Utility: wait (parameter in centiseconds) | Utility |

### 1.4 Shared Utility Programs

| # | Program | Lines | Function | Business Domain |
|---|---------|-------|----------|-----------------|
| 31 | **CSUTLDTC** | 157 | Date validation via CEEDAYS API call | Utility |

### 1.5 Assembler Programs (`app/asm/`)

| # | Program | Function | Called By |
|---|---------|----------|-----------|
| 1 | **COBDATFT** | Date formatting utility | CBACT01C |
| 2 | **MVSWAIT** | System wait (centisecond precision) | COBSWAIT |

---

## 2. Optional Extension Modules

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ Series integration for card authorization.

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | **COPAUA0C** | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| 2 | **COPAUS0C** | CICS/IMS/BMS | Authorization messages summary view |
| 3 | **COPAUS1C** | CICS/IMS/BMS | Authorization message detail view |
| 4 | **COPAUS2C** | CICS/IMS/DB2 | Mark authorization as fraud (writes to DB2) |
| 5 | **CBPAUP0C** | Batch/IMS | Delete expired pending authorization messages |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2 CRUD operations for transaction types with embedded SQL.

| # | Program | Type | Function |
|---|---------|------|----------|
| 6 | **COTRTLIC** | CICS/DB2 | List transaction types (cursor-based paging) |
| 7 | **COTRTUPC** | CICS/DB2 | Add/update transaction type |
| 8 | **COBTUPDT** | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response integration for system inquiries.

| # | Program | Type | Function |
|---|---------|------|----------|
| 9 | **COACCT01** | CICS/MQ | Account inquiry via MQ (CDRA trigger) |
| 10 | **CODATE01** | CICS/MQ | System date inquiry via MQ (CDRD trigger) |

---

## 3. Copybooks (`app/cpy/`)

### 3.1 Data Structure Copybooks (VSAM Record Layouts)

| # | Copybook | Lines | Record Length | Business Entity | Key Fields |
|---|----------|-------|--------------|-----------------|------------|
| 1 | **CVACT01Y** | 30 | 300 bytes | Account Master | ACCT-ID (11) |
| 2 | **CVACT02Y** | 17 | 150 bytes | Card Data | CARD-NUM (16) |
| 3 | **CVACT03Y** | 16 | 50 bytes | Card Cross-Reference | XREF-CARD-NUM (16), XREF-ACCT-ID (11) |
| 4 | **CVCUS01Y** | 44 | 500 bytes | Customer Master | CUST-ID (9) |
| 5 | **CVTRA01Y** | 15 | 60 bytes | Tran Category Balance | TRANCAT-ACCT-ID (11) |
| 6 | **CVTRA02Y** | 13 | 50 bytes | Disclosure Group | DIS-ACCT-GROUP-ID (10) |
| 7 | **CVTRA03Y** | 10 | 60 bytes | Transaction Type | TRAN-TYPE (2) |
| 8 | **CVTRA04Y** | 12 | 60 bytes | Transaction Category | TRAN-TYPE-CD (2), TRAN-CAT-CD (4) |
| 9 | **CVTRA05Y** | 21 | 350 bytes | Transaction Record | TRAN-ID (16) |
| 10 | **CVTRA06Y** | 21 | 350 bytes | Daily Transaction | DALYTRAN-ID (16) |
| 11 | **CVTRA07Y** | 73 | varies | Transaction Report Layout | Report formatting |
| 12 | **CVCRD01Y** | varies | varies | Card Internal Record | Card detail fields |
| 13 | **CUSTREC** | varies | varies | Customer Record (alt layout) | Statement generation |
| 14 | **COSTM01** | 38 | 350+ bytes | Transaction (keyed by card+tran) | TRNX-CARD-NUM, TRNX-ID |
| 15 | **CVEXPORT** | varies | varies | Export/Import Record Layout | Multi-record export format |

### 3.2 Security & System Copybooks

| # | Copybook | Lines | Business Entity |
|---|----------|-------|-----------------|
| 16 | **CSUSR01Y** | varies | User Security Record (user ID, password, type) |
| 17 | **COCOM01Y** | varies | Common Communication Area (COMMAREA) |
| 18 | **CSMEN02Y / COMEN02Y** | varies | Menu option definitions |
| 19 | **COADM02Y** | varies | Admin menu option definitions |
| 20 | **COTTL01Y** | varies | Screen title/header layout |

### 3.3 Utility & Helper Copybooks

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 21 | **CSDAT01Y** | varies | Date formatting fields |
| 22 | **CSMSG01Y** | varies | Message area (single message) |
| 23 | **CSMSG02Y** | varies | Message area (multiple messages) |
| 24 | **CSSETATY** | varies | Screen attribute settings |
| 25 | **CSSTRPFY** | varies | String processing functions |
| 26 | **CSLKPCDY** | varies | Lookup code definitions |
| 27 | **CSUTLDPY** | varies | Utility date parameters |
| 28 | **CSUTLDWY** | varies | Utility date working storage |
| 29 | **CODATECN** | varies | Date conversion record |
| 30 | **UNUSED1Y** | 10 | Unused placeholder record |

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map Set | Screen ID | Lines | Function | Used By Program |
|---|---------|-----------|-------|----------|-----------------|
| 1 | **COSGN00** | COSGN0A | 210 | Login screen | COSGN00C |
| 2 | **COMEN01** | COMEN1A | 167 | Main menu (regular users) | COMEN01C |
| 3 | **COADM01** | COADM1A | 132 | Admin menu | COADM01C |
| 4 | **COACTVW** | COACVWA | 190 | Account view | COACTVWC |
| 5 | **COACTUP** | COACUPA | 245 | Account update | COACTUPC |
| 6 | **COCRDLI** | CCRDLIA | 313 | Card list | COCRDLIC |
| 7 | **COCRDSL** | CCRDSL | 220 | Card detail view | COCRDSLC |
| 8 | **COCRDUP** | CCRDUPA | 252 | Card update | COCRDUPC |
| 9 | **COTRN00** | COTRN0A | 464 | Transaction list | COTRN00C |
| 10 | **COTRN01** | COTRN1A | 273 | Transaction view | COTRN01C |
| 11 | **COTRN02** | COTRN2A | 307 | Transaction add | COTRN02C |
| 12 | **CORPT00** | CORPT0A | 231 | Report request | CORPT00C |
| 13 | **COBIL00** | COBIL0A | 166 | Bill payment | COBIL00C |
| 14 | **COUSR00** | COUSR0A | 463 | User list | COUSR00C |
| 15 | **COUSR01** | COUSR1A | 164 | Add user | COUSR01C |
| 16 | **COUSR02** | COUSR2A | 169 | Update user | COUSR02C |
| 17 | **COUSR03** | COUSR3A | 153 | Delete user | COUSR03C |

### BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map has a corresponding generated copybook (17 total) that provides symbolic field names for COBOL programs to reference screen fields.

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data File Refresh Jobs

| # | JCL Job | Lines | Function | Target VSAM Dataset |
|---|---------|-------|----------|---------------------|
| 1 | **ACCTFILE** | 65 | Refresh account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | 128 | Refresh card master VSAM | CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | 84 | Refresh customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | 106 | Load card cross-reference VSAM | CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | 125 | Load transaction master VSAM | TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ** | 92 | Load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | **DISCGRP** | 65 | Load disclosure group VSAM | DISCGRP.VSAM.KSDS |
| 8 | **TRANTYPE** | 65 | Load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 9 | **TRANCATG** | 65 | Load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 10 | **TCATBALF** | 65 | Load tran category balance VSAM | TCATBALF.VSAM.KSDS |
| 11 | **REPTFILE** | 32 | Define/refresh report file VSAM | REPTFILE |
| 12 | **DALYREJS** | 32 | Define daily rejects GDG | DALYREJS GDG |

### 5.2 Core Batch Processing Jobs

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|-------|----------|------------------|
| 13 | **POSTTRAN** | 45 | Post daily transactions | CBTRN02C |
| 14 | **INTCALC** | 44 | Calculate interest on accounts | CBACT04C |
| 15 | **TRANBKP** | 71 | Backup transaction VSAM to GDG | REPROC (IDCAMS) |
| 16 | **COMBTRAN** | 52 | Combine backup + system transactions | SORT + IDCAMS |
| 17 | **CREASTMT** | 97 | Generate account statements | CBSTM03A |
| 18 | **TRANREPT** | 84 | Generate transaction detail report | CBTRN03C |
| 19 | **PRTCATBL** | 66 | Print category balance report | REPROC + SORT |
| 20 | **CBEXPORT** | 72 | Export data for branch migration | CBEXPORT |
| 21 | **CBIMPORT** | 68 | Import data from branch migration | CBIMPORT |

### 5.3 CICS File Control Jobs

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 22 | **CLOSEFIL** | 34 | Close CICS files for batch processing |
| 23 | **OPENFIL** | 34 | Re-open CICS files after batch |

### 5.4 Infrastructure & Definition Jobs

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 24 | **DEFGDGB** | 63 | Define GDG base entries |
| 25 | **DEFGDGD** | 94 | Define GDG data entries with backup |
| 26 | **TRANIDX** | 58 | Define/rebuild alternate indexes on TRANSACT |
| 27 | **DEFCUST** | 47 | Define customer VSAM cluster |
| 28 | **ESDSRRDS** | 124 | Define ESDS/RRDS VSAM clusters |
| 29 | **CBADMCDJ** | 167 | CICS CSD batch update (transaction defs) |
| 30 | **WAITSTEP** | 27 | Job wait step (calls COBSWAIT) |

### 5.5 Diagnostic & Utility Jobs

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 31 | **READACCT** | 34 | Read/print account file (CBACT01C) |
| 32 | **READCARD** | 31 | Read/print card file (CBACT02C) |
| 33 | **READCUST** | 31 | Read/print customer file (CBCUS01C) |
| 34 | **READXREF** | 31 | Read/print cross-reference (CBACT03C) |
| 35 | **FTPJCL** | 42 | FTP file transfer utility |
| 36 | **INTRDRJ1** | 19 | Internal reader job #1 |
| 37 | **INTRDRJ2** | 14 | Internal reader job #2 |
| 38 | **TXT2PDF1** | 41 | Convert text statements to PDF |

### 5.6 JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | **REPROC** | Reusable VSAM-to-sequential copy procedure |
| 2 | **TRANREPT** | Transaction report generation procedure |

---

## 6. Data Files (`app/data/ASCII/`)

| # | File | Business Entity | Format |
|---|------|-----------------|--------|
| 1 | **acctdata.txt** | Account records | Fixed-length |
| 2 | **carddata.txt** | Card records | Fixed-length |
| 3 | **cardxref.txt** | Card-account cross-reference | Fixed-length |
| 4 | **custdata.txt** | Customer records | Fixed-length |
| 5 | **dailytran.txt** | Daily transaction feed | Fixed-length |
| 6 | **discgrp.txt** | Disclosure group rates | Fixed-length |
| 7 | **tcatbal.txt** | Transaction category balances | Fixed-length |
| 8 | **trancatg.txt** | Transaction categories | Fixed-length |
| 9 | **trantype.txt** | Transaction types | Fixed-length |

---

## 7. Scheduler Configurations (`app/scheduler/`)

| # | File | Scheduler | Purpose |
|---|------|-----------|---------|
| 1 | **CardDemo.ca7** | CA-7 | Batch job scheduling definitions |
| 2 | **CardDemo.controlm** | Control-M | Batch job scheduling definitions |

---

## 8. Classification Summary

| Category | Count | Percentage |
|----------|-------|-----------|
| Online CICS Programs | 17 | 35% |
| Batch Programs | 13 | 27% |
| Utility/Shared Programs | 1 | 2% |
| Copybooks (Data) | 30 | - |
| Copybooks (BMS-generated) | 17 | - |
| BMS Screen Maps | 17 | - |
| JCL Batch Jobs | 38 | - |
| JCL Procedures | 2 | - |
| Assembler Programs | 2 | - |
| Optional Module Programs | 10 | - |
| **Total Source Artifacts** | **147** | - |

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C |
| `CB*` | Batch COBOL program | CBTRN02C |
| `CS*` | Shared/common copybook | CSUSR01Y |
| `CV*` | VSAM record layout copybook | CVACT01Y |
| `*Y` | Copybook (suffix) | CVACT01Y |

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | COBOL 85 |
| Online TP | CICS TS |
| Data Storage | VSAM (KSDS, ESDS, RRDS) |
| Batch Scheduler | JES2/JCL, CA-7, Control-M |
| Screen Presentation | BMS (3270 terminals) |
| Utilities | IDCAMS, SORT, IEBGENER, SDSF |
| Optional: Database | IBM DB2 |
| Optional: Messaging | IBM MQ Series |
| Optional: Hierarchical DB | IMS DB |
