# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** z/OS · CICS · VSAM · COBOL · JCL · BMS (3270)

---

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL/CICS/VSAM. It supports two user roles (Regular and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user security administration. The codebase contains **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, **2 assembler utilities**, **2 JCL procedures**, and **3 optional extension modules** (13 additional programs).

| Category | Count |
|---|---|
| Core COBOL Programs (`app/cbl/`) | 31 |
| Copybooks (`app/cpy/`) | 30 |
| BMS Screen Maps (`app/bms/`) | 17 |
| BMS-Generated Copybooks (`app/cpy-bms/`) | 17 |
| JCL Batch Jobs (`app/jcl/`) | 38 |
| Assembler Programs (`app/asm/`) | 2 |
| JCL Procedures (`app/proc/`) | 2 |
| Optional Module Programs | 13 |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs (16 programs)

| # | Program | Lines | Trans ID | Function | Business Domain | BMS Map |
|---|---------|-------|----------|----------|----------------|---------|
| 1 | COSGN00C | 261 | CC00 | User Sign-on / Authentication | Security | COSGN00 |
| 2 | COMEN01C | 309 | CM00 | Main Menu (Regular Users) | Navigation | COMEN01 |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation / Admin | COADM01 |
| 4 | COACTVWC | 942 | CAVW | Account View | Account Mgmt | COACTVW |
| 5 | COACTUPC | 4,237 | CAUP | Account Update | Account Mgmt | COACTUP |
| 6 | COCRDLIC | 1,460 | CCLI | Credit Card List | Card Mgmt | COCRDLI |
| 7 | COCRDSLC | 888 | CCDL | Credit Card Detail View | Card Mgmt | COCRDSL |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | COCRDUP |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transaction Mgmt | COTRN00 |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transaction Mgmt | COTRN01 |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transaction Mgmt | COTRN02 |
| 12 | CORPT00C | 649 | CR00 | Transaction Report (submit batch) | Reporting | CORPT00 |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing | COBIL00 |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | User Admin | COUSR00 |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | User Admin | COUSR01 |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | User Admin | COUSR02 |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | User Admin | COUSR03 |

### 1.2 Batch COBOL Programs (13 programs)

| # | Program | Lines | Function | Business Domain |
|---|---------|-------|----------|----------------|
| 1 | CBACT01C | 430 | Read account file, write output formats | Data Utility |
| 2 | CBACT02C | 178 | Read and print card data file | Data Utility |
| 3 | CBACT03C | 178 | Read and print cross-reference data | Data Utility |
| 4 | CBACT04C | 652 | Interest calculation on accounts | Financial Processing |
| 5 | CBCUS01C | 178 | Read and print customer data file | Data Utility |
| 6 | CBTRN01C | 494 | Post daily transaction records | Transaction Processing |
| 7 | CBTRN02C | 731 | Post daily transactions (enhanced) | Transaction Processing |
| 8 | CBTRN03C | 649 | Print transaction detail report | Reporting |
| 9 | CBSTM03A | 924 | Generate account statements (text + HTML) | Statement Generation |
| 10 | CBSTM03B | 230 | Statement file I/O subroutine | Statement Generation |
| 11 | CBEXPORT | 582 | Export customer data for branch migration | Data Migration |
| 12 | CBIMPORT | 487 | Import customer data from branch export | Data Migration |
| 13 | COBSWAIT | 41 | Wait utility (centisecond delay) | System Utility |

### 1.3 Shared Subroutine (1 program)

| # | Program | Lines | Function | Called By |
|---|---------|-------|----------|-----------|
| 1 | CSUTLDTC | 157 | Date validation via LE (CEEDAYS) | CORPT00C, COTRN02C, COACTUPC (via copybook) |

---

## 2. Optional Extension Modules

### 2.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | COPAUA0C | 1,026 | CICS/IMS/MQ | Card Authorization Decision (MQ trigger) |
| 2 | COPAUS0C | 1,032 | CICS/IMS/BMS | Authorization Message Summary View |
| 3 | COPAUS1C | 604 | CICS/IMS/BMS | Authorization Message Detail View |
| 4 | COPAUS2C | 244 | CICS/IMS/DB2 | Mark Authorization as Fraud (writes to DB2) |
| 5 | CBPAUP0C | 386 | Batch IMS | Purge Expired Pending Authorizations |
| 6 | DBUNLDGS | 366 | Batch IMS | IMS Database Unload utility |
| 7 | PAUDBLOD | 369 | Batch IMS | IMS Database Load utility |
| 8 | PAUDBUNL | 317 | Batch IMS | IMS Database Unload utility |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | COTRTLIC | 2,098 | CICS/DB2 | Transaction Type List with paging cursors |
| 2 | COTRTUPC | 1,702 | CICS/DB2 | Transaction Type Add/Update |
| 3 | COBTUPDT | 237 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | CODATE01 | 524 | CICS/MQ | System Date MQ request/response |
| 2 | COACCT01 | 620 | CICS/MQ | Account Inquiry via MQ request/response |

---

## 3. Copybooks (`app/cpy/`) — 30 Files

### 3.1 Business Data Record Layouts (13 copybooks)

| # | Copybook | Lines | Record | Description |
|---|----------|-------|--------|-------------|
| 1 | CVACT01Y | 21 | ACCOUNT-RECORD (300 bytes) | Account master data |
| 2 | CVACT02Y | 15 | CARD-RECORD (150 bytes) | Credit card data |
| 3 | CVACT03Y | 13 | CARD-XREF-RECORD (50 bytes) | Card-to-account cross-reference |
| 4 | CVCUS01Y | 26 | CUSTOMER-RECORD (500 bytes) | Customer master data |
| 5 | CVTRA05Y | 21 | TRAN-RECORD (350 bytes) | Online transaction record |
| 6 | CVTRA06Y | 21 | DALYTRAN-RECORD (350 bytes) | Daily transaction record |
| 7 | CVTRA01Y | 13 | TRAN-CAT-BAL-RECORD | Transaction category balance |
| 8 | CVTRA02Y | 13 | DIS-INT-RATE-RECORD | Discount/interest rate group |
| 9 | CVTRA03Y | 10 | TRAN-TYPE-RECORD (60 bytes) | Transaction type lookup |
| 10 | CVTRA04Y | 12 | TRAN-CAT-TYPE-RECORD | Transaction category type |
| 11 | CVTRA07Y | 73 | Report line definitions | Transaction report formatting |
| 12 | CVEXPORT | 103 | EXPORT-RECORD (500 bytes) | Multi-record export layout |
| 13 | CUSTREC | varies | Alternate customer record | Statement customer record |

### 3.2 Application Infrastructure Copybooks (11 copybooks)

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 1 | COCOM01Y | 48 | CARDDEMO-COMMAREA — application communication area |
| 2 | COMEN02Y | 102 | Main menu option definitions (11 options) |
| 3 | COADM02Y | 63 | Admin menu option definitions (6 options) |
| 4 | COTTL01Y | varies | Screen title/header constants |
| 5 | CSUSR01Y | 27 | SEC-USER-DATA — user security record (80 bytes) |
| 6 | CSDAT01Y | varies | Current date/time working storage |
| 7 | CSMSG01Y | varies | Common message constants |
| 8 | CSMSG02Y | varies | Abend handling variables |
| 9 | CSLKPCDY | varies | Lookup code utility variables |
| 10 | COSTM01 | 38 | Statement transaction record layout |
| 11 | UNUSED1Y | 10 | Unused/placeholder record |

### 3.3 Utility/Processing Copybooks (6 copybooks)

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 1 | CSUTLDWY | varies | Date edit working storage (CCYYMMDD validation) |
| 2 | CSUTLDPY | varies | Date validation procedure logic |
| 3 | CSSTRPFY | varies | PF key mapping/storage procedures |
| 4 | CSSETATY | varies | Screen attribute setting procedures |
| 5 | CODATECN | varies | Date format conversion record |
| 6 | CVCRD01Y | varies | Card data work area for online programs |

---

## 4. BMS Screen Maps (`app/bms/`) — 17 Maps

| # | Map File | Mapset | Map Name | Lines | Associated Program | Screen Purpose |
|---|----------|--------|----------|-------|--------------------|----------------|
| 1 | COSGN00.bms | COSGN00 | COSGN0A | 210 | COSGN00C | Sign-on screen |
| 2 | COMEN01.bms | COMEN01 | COMEN1A | 167 | COMEN01C | Main menu |
| 3 | COADM01.bms | COADM01 | COADM1A | 167 | COADM01C | Admin menu |
| 4 | COACTVW.bms | COACTVW | CACTVWA | 378 | COACTVWC | Account view |
| 5 | COACTUP.bms | COACTUP | CACTUPA | 512 | COACTUPC | Account update form |
| 6 | COCRDLI.bms | COCRDLI | CCRDLIA | 344 | COCRDLIC | Credit card list |
| 7 | COCRDSL.bms | COCRDSL | CCRDSLA | 157 | COCRDSLC | Credit card detail |
| 8 | COCRDUP.bms | COCRDUP | CCRDUPA | 172 | COCRDUPC | Credit card update |
| 9 | COTRN00.bms | COTRN00 | COTRN0A | 464 | COTRN00C | Transaction list |
| 10 | COTRN01.bms | COTRN01 | COTRN1A | 273 | COTRN01C | Transaction view |
| 11 | COTRN02.bms | COTRN02 | COTRN2A | 307 | COTRN02C | Transaction add form |
| 12 | CORPT00.bms | CORPT00 | CORPT0A | 231 | CORPT00C | Report selection |
| 13 | COBIL00.bms | COBIL00 | COBIL0A | 141 | COBIL00C | Bill payment |
| 14 | COUSR00.bms | COUSR00 | COUSR0A | 463 | COUSR00C | User list |
| 15 | COUSR01.bms | COUSR01 | COUSR1A | 164 | COUSR01C | User add form |
| 16 | COUSR02.bms | COUSR02 | COUSR2A | 169 | COUSR02C | User update form |
| 17 | COUSR03.bms | COUSR03 | COUSR3A | 153 | COUSR03C | User delete confirm |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 matching `.CPY` files are auto-generated symbolic maps from the BMS definitions above.

---

## 5. JCL Batch Jobs (`app/jcl/`) — 38 Jobs

### 5.1 Data File Management (12 jobs)

| # | JCL Job | Lines | Function | VSAM Dataset(s) |
|---|---------|-------|----------|-----------------|
| 1 | ACCTFILE | 65 | Refresh account master VSAM | ACCTDAT |
| 2 | CARDFILE | 128 | Refresh card master VSAM + alt indexes | CARDDAT, CARDAIX |
| 3 | CUSTFILE | 84 | Refresh customer master VSAM | CUSTDAT |
| 4 | XREFFILE | 106 | Refresh card cross-reference VSAM + indexes | CARDXREF, CXACAIX |
| 5 | TRANFILE | 125 | Refresh transaction master VSAM + indexes | TRANSACT |
| 6 | DUSRSECJ | 92 | Load user security VSAM from inline data | USRSEC |
| 7 | DISCGRP | 65 | Load discount group VSAM | DISCGRP |
| 8 | TCATBALF | 65 | Load transaction category balance VSAM | TCATBALF |
| 9 | TRANCATG | 65 | Load transaction category VSAM | TRANCATG |
| 10 | TRANTYPE | 65 | Load transaction type VSAM | TRANTYPE |
| 11 | DALYREJS | 32 | Define daily rejects VSAM | DALYREJS |
| 12 | REPTFILE | 32 | Define report file VSAM | REPTFILE |

### 5.2 Core Batch Processing (7 jobs)

| # | JCL Job | Lines | Program Exec | Function |
|---|---------|-------|-------------|----------|
| 1 | POSTTRAN | 45 | CBTRN02C | Post daily transactions to master |
| 2 | INTCALC | 44 | CBACT04C | Calculate interest on accounts |
| 3 | COMBTRAN | 52 | SORT + IDCAMS | Combine/merge transaction files |
| 4 | CREASTMT | 97 | SORT + CBSTM03A | Generate account statements |
| 5 | TRANREPT | 84 | SORT + CBTRN03C | Print transaction detail report |
| 6 | TRANBKP | 71 | IDCAMS + REPROC | Backup transaction files |
| 7 | PRTCATBL | 66 | SORT + REPROC | Print transaction category balances |

### 5.3 Data Utility Jobs (5 jobs)

| # | JCL Job | Lines | Program Exec | Function |
|---|---------|-------|-------------|----------|
| 1 | READACCT | 50 | CBACT01C | Read accounts, write multiple formats |
| 2 | READCARD | 31 | CBACT02C | Read and display card data |
| 3 | READCUST | 30 | CBCUS01C | Read and display customer data |
| 4 | READXREF | 31 | CBACT03C | Read and display cross-reference data |
| 5 | WAITSTEP | 27 | COBSWAIT | Timed wait step utility |

### 5.4 Export/Import Jobs (2 jobs)

| # | JCL Job | Lines | Program Exec | Function |
|---|---------|-------|-------------|----------|
| 1 | CBEXPORT | 72 | CBEXPORT | Export all data for branch migration |
| 2 | CBIMPORT | 68 | CBIMPORT | Import data from branch migration export |

### 5.5 CICS File Control & Infrastructure (6 jobs)

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 1 | CLOSEFIL | 34 | Close CICS-managed VSAM files |
| 2 | OPENFIL | 34 | Open CICS-managed VSAM files |
| 3 | TRANIDX | 58 | Define/rebuild alternate indexes on TRANSACT |
| 4 | DEFGDGB | 63 | Define GDG base entries |
| 5 | DEFGDGD | 94 | Define GDG data entries |
| 6 | DEFCUST | 47 | Define customer VSAM cluster |

### 5.6 Specialized / Miscellaneous (6 jobs)

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 1 | CBADMCDJ | 167 | CICS CSD resource definitions (DFHCSDUP) |
| 2 | ESDSRRDS | 124 | Define ESDS and RRDS VSAM datasets |
| 3 | FTPJCL | 42 | FTP file transfer job |
| 4 | INTRDRJ1 | 19 | Internal reader job chaining (step 1) |
| 5 | INTRDRJ2 | 14 | Internal reader job chaining (step 2) |
| 6 | TXT2PDF1 | 41 | Convert text report to PDF |

---

## 6. Assembler Programs (`app/asm/`) — 2 Files

| # | Program | Function |
|---|---------|----------|
| 1 | COBDATFT.asm | Date formatting utility (called by CBACT01C) |
| 2 | MVSWAIT.asm | MVS wait/delay utility (called by COBSWAIT) |

---

## 7. JCL Procedures (`app/proc/`) — 2 Files

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Reusable report processing procedure |
| 2 | TRANREPT.prc | Transaction report processing procedure |

---

## 8. Classification Summary

### By Business Domain

| Domain | Programs | Percentage |
|--------|----------|-----------|
| Account Management | COACTVWC, COACTUPC, CBACT01C, CBACT04C | 13% |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | 16% |
| Transaction Processing | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | 16% |
| Reporting / Statements | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B | 13% |
| Billing | COBIL00C | 3% |
| User Security Admin | COUSR00C-03C, COSGN00C | 16% |
| Navigation | COMEN01C, COADM01C | 6% |
| Data Migration | CBEXPORT, CBIMPORT | 6% |
| Utilities | CSUTLDTC, COBSWAIT, CBCUS01C | 10% |

### By Technology Pattern

| Pattern | Count | Programs |
|---------|-------|----------|
| CICS Online (BMS screens) | 17 | CO* programs |
| Batch (sequential file I/O) | 13 | CB* programs |
| CICS + IMS/DB2/MQ | 8 | COPA*, COPAUS*, CBPAUP*, DB*, PAU* |
| CICS + DB2 (cursors) | 3 | COTRTLIC, COTRTUPC, COBTUPDT |
| CICS + MQ (request/response) | 2 | CODATE01, COACCT01 |

### Total Lines of COBOL (Core)

| Category | Total Lines |
|----------|-------------|
| Online CICS programs | ~13,935 |
| Batch programs | ~5,754 |
| Shared subroutine | 157 |
| **Grand Total (core)** | **~19,846** |
| Optional modules | ~9,525 |
| **Grand Total (all)** | **~29,371** |

---

## 9. Batch Processing Cycle

The recommended nightly batch sequence is:

```
CLOSEFIL → ACCTFILE → CARDFILE → CUSTFILE → XREFFILE → TRANFILE
    → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT
    → TRANIDX → OPENFIL
```

This cycle closes CICS files, refreshes data, processes transactions, calculates interest, backs up, merges, generates statements, rebuilds indexes, and reopens files for online access.
