# Application Inventory — CardDemo COBOL Codebase

> **Generated from:** `uc-legacy-modernization-cobol-to-java`
> **Total Artifacts:** 31 COBOL programs + 13 optional-module programs, 30 copybooks, 17 BMS-generated copybooks, 38 JCL jobs, 17 BMS maps, 2 assembler programs, 2 JCL procedures

---

## Table of Contents

1. [COBOL Programs (Core)](#1-cobol-programs--core-appcbl)
2. [COBOL Programs (Optional Modules)](#2-cobol-programs--optional-modules)
3. [Copybooks (Data Structures)](#3-copybooks--data-structures-appcpy)
4. [BMS Screen Maps](#4-bms-screen-maps-appbms)
5. [BMS-Generated Copybooks](#5-bms-generated-copybooks-appcpy-bms)
6. [JCL Batch Jobs](#6-jcl-batch-jobs-appjcl)
7. [Assembler Programs](#7-assembler-programs-appasm)
8. [JCL Procedures](#8-jcl-procedures-appproc)
9. [Summary Statistics](#9-summary-statistics)

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs

| # | Program | Lines | CICS Trans | Function | Business Domain | Classification |
|---|---------|------:|------------|----------|-----------------|----------------|
| 1 | COSGN00C.cbl | 260 | CC00 | User sign-on / authentication | Security | Entry Point |
| 2 | COMEN01C.cbl | 308 | CM00 | Main menu navigation | Navigation | Controller |
| 3 | COADM01C.cbl | 288 | CA00 | Admin menu navigation | Administration | Controller |
| 4 | COACTVWC.cbl | 941 | CA01 | View account details | Account Mgmt | Read / Display |
| 5 | COACTUPC.cbl | 4,236 | CA02 | Update account details | Account Mgmt | Read / Write |
| 6 | COCRDLIC.cbl | 1,459 | CC01 | List credit cards (paginated) | Card Mgmt | Read / Display |
| 7 | COCRDSLC.cbl | 887 | CC02 | View single card details | Card Mgmt | Read / Display |
| 8 | COCRDUPC.cbl | 1,560 | CC03 | Update credit card | Card Mgmt | Read / Write |
| 9 | COTRN00C.cbl | 699 | CT00 | List transactions (paginated) | Transaction Mgmt | Read / Display |
| 10 | COTRN01C.cbl | 330 | CT01 | View single transaction | Transaction Mgmt | Read / Display |
| 11 | COTRN02C.cbl | 783 | CT02 | Add new transaction | Transaction Mgmt | Write |
| 12 | CORPT00C.cbl | 649 | CR00 | Request transaction reports | Reporting | Controller |
| 13 | COBIL00C.cbl | 572 | CB00 | Process bill payments | Bill Payment | Read / Write |
| 14 | COUSR00C.cbl | 695 | CU00 | List all users (paginated) | User Admin | Read / Display |
| 15 | COUSR01C.cbl | 299 | CU01 | Add new user | User Admin | Write |
| 16 | COUSR02C.cbl | 414 | CU02 | Update existing user | User Admin | Read / Write |
| 17 | COUSR03C.cbl | 359 | CU03 | Delete user | User Admin | Delete |

### 1.2 Batch Programs

| # | Program | Lines | Function | Business Domain | Classification |
|---|---------|------:|----------|-----------------|----------------|
| 18 | CBACT01C.cbl | 430 | Read account file, write to output files | Account Mgmt | Data Extract |
| 19 | CBACT02C.cbl | 178 | Read and print card data file | Card Mgmt | Data Extract |
| 20 | CBACT03C.cbl | 178 | Read and print cross-reference file | Cross-Reference | Data Extract |
| 21 | CBACT04C.cbl | 652 | Interest calculation on accounts | Financial Processing | Computation |
| 22 | CBCUS01C.cbl | 178 | Read and print customer data file | Customer Mgmt | Data Extract |
| 23 | CBTRN01C.cbl | 494 | Post daily transactions (validation pipeline) | Transaction Processing | ETL / Write |
| 24 | CBTRN02C.cbl | 731 | Post daily transactions (posting + reject) | Transaction Processing | ETL / Write |
| 25 | CBTRN03C.cbl | 649 | Print transaction detail report | Reporting | Report Generation |
| 26 | CBSTM03A.CBL | 924 | Statement generation (main driver) | Reporting | Report Generation |
| 27 | CBSTM03B.CBL | 230 | Statement generation (subroutine — file I/O) | Reporting | Subroutine |
| 28 | CBEXPORT.cbl | 582 | Export all VSAM data to sequential file | Data Migration | Data Extract |
| 29 | CBIMPORT.cbl | 487 | Import sequential file into VSAM datasets | Data Migration | Data Load |

### 1.3 Utility Programs

| # | Program | Lines | Function | Classification |
|---|---------|------:|----------|----------------|
| 30 | CSUTLDTC.cbl | 157 | Date conversion utility (calls CEEDAYS) | Utility / Shared |
| 31 | COBSWAIT.cbl | 41 | Wait / pause utility for batch scheduling | Utility |

---

## 2. COBOL Programs — Optional Modules

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

| # | Program | Lines | Function | Technology | Classification |
|---|---------|------:|----------|------------|----------------|
| 32 | COPAUA0C.cbl | 1,026 | Card authorization decision program | CICS + IMS + MQ | Business Logic |
| 33 | COPAUS0C.cbl | 1,032 | Summary view of authorization messages | CICS + IMS + BMS | Read / Display |
| 34 | COPAUS1C.cbl | 604 | Detail view of authorization message | CICS + IMS + BMS | Read / Display |
| 35 | COPAUS2C.cbl | 244 | Mark authorization message as fraud | CICS + IMS + DB2 | Write |
| 36 | CBPAUP0C.cbl | 386 | Delete expired pending authorizations | Batch + IMS | Maintenance |
| 37 | PAUDBLOD.CBL | 369 | Load IMS database from sequential file | Batch + IMS | Data Load |
| 38 | PAUDBUNL.CBL | 317 | Unload IMS database to sequential file | Batch + IMS | Data Extract |
| 39 | DBUNLDGS.CBL | 366 | Unload IMS DB using generalized segments | Batch + IMS | Data Extract |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Function | Technology | Classification |
|---|---------|------:|----------|------------|----------------|
| 40 | COTRTLIC.cbl | 2,098 | List transaction types (paged, DB2 cursors) | CICS + DB2 | Read / Display |
| 41 | COTRTUPC.cbl | 1,702 | Add/edit transaction type | CICS + DB2 | Write |
| 42 | COBTUPDT.cbl | 237 | Batch update transaction types from DB2 | Batch + DB2 | ETL |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Function | Technology | Classification |
|---|---------|------:|----------|------------|----------------|
| 43 | COACCT01.cbl | 620 | Account inquiry via MQ request/response | CICS + MQ + VSAM | Service |
| 44 | CODATE01.cbl | 524 | System date inquiry via MQ request/response | CICS + MQ | Service |

---

## 3. Copybooks — Data Structures (`app/cpy/`)

| # | Copybook | Lines | Business Entity | Description |
|---|----------|------:|-----------------|-------------|
| 1 | CVACT01Y.cpy | 20 | Account | Account master record (300 bytes) |
| 2 | CVACT02Y.cpy | 14 | Card | Card master record (150 bytes) |
| 3 | CVACT03Y.cpy | 11 | Cross-Reference | Card-to-account cross-reference |
| 4 | CVCUS01Y.cpy | 26 | Customer | Customer master record (500 bytes) |
| 5 | CVCRD01Y.cpy | 46 | Card | Card cross-reference data |
| 6 | CVTRA01Y.cpy | 13 | Transaction Category Balance | Category-level balance record |
| 7 | CVTRA02Y.cpy | 13 | Discount Group | Discount/interest rate grouping |
| 8 | CVTRA03Y.cpy | 10 | Transaction Type | Transaction type code + description |
| 9 | CVTRA04Y.cpy | 12 | Transaction Category Type | Category + type description |
| 10 | CVTRA05Y.cpy | 21 | Transaction | Online transaction record (350 bytes) |
| 11 | CVTRA06Y.cpy | 21 | Daily Transaction | Daily transaction record |
| 12 | CVTRA07Y.cpy | 73 | Transaction Report | Report layout and formatting |
| 13 | CVEXPORT.cpy | 103 | Export Record | Unified export record with all entity types |
| 14 | CSUSR01Y.cpy | 26 | User Security | User ID, name, password, type |
| 15 | COCOM01Y.cpy | 47 | Common Area | COMMAREA for CICS program communication |
| 16 | COMEN02Y.cpy | 101 | Menu Definition | Menu options and navigation targets |
| 17 | COADM02Y.cpy | 62 | Admin Menu | Admin menu options |
| 18 | COTTL01Y.cpy | 27 | Title/Header | Screen title and header definitions |
| 19 | CSDAT01Y.cpy | 58 | Date Work Area | Date formatting work fields |
| 20 | CSMSG01Y.cpy | 24 | Message Area | User message display area |
| 21 | CSMSG02Y.cpy | 35 | Message Area 2 | Extended message area |
| 22 | CSSETATY.cpy | 30 | Set Attribute | Field attribute setting helper |
| 23 | CSSTRPFY.cpy | 85 | String Prefix | String manipulation helper |
| 24 | CSLKPCDY.cpy | 1,318 | Lookup Code | US phone area code + state code lookup tables |
| 25 | CSUTLDPY.cpy | 375 | Utility Display | Display formatting and validation utility |
| 26 | CSUTLDWY.cpy | 89 | Utility Work | Date editing and validation work area |
| 27 | CODATECN.cpy | 52 | Date Conversion | Date conversion record for assembler call |
| 28 | CUSTREC.cpy | 26 | Customer (Statement) | Customer record used in statement generation |
| 29 | COSTM01.CPY | 38 | Statement Transaction | Transaction record for statement processing |
| 30 | UNUSED1Y.cpy | 10 | (Unused) | Placeholder / deprecated record |

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map | Lines | Screen Name | Associated Program | Function |
|---|-----|------:|-------------|-------------------|----------|
| 1 | COSGN00.bms | 210 | Sign-on | COSGN00C | User login screen |
| 2 | COMEN01.bms | 156 | Main Menu | COMEN01C | Navigation menu |
| 3 | COADM01.bms | 153 | Admin Menu | COADM01C | Admin navigation |
| 4 | COACTVW.bms | 215 | Account View | COACTVWC | Display account details |
| 5 | COACTUP.bms | 294 | Account Update | COACTUPC | Edit account fields |
| 6 | COCRDLI.bms | 462 | Card List | COCRDLIC | Paginated card list |
| 7 | COCRDSL.bms | 262 | Card Detail | COCRDSLC | Display card details |
| 8 | COCRDUP.bms | 264 | Card Update | COCRDUPC | Edit card fields |
| 9 | COTRN00.bms | 464 | Transaction List | COTRN00C | Paginated transaction list |
| 10 | COTRN01.bms | 273 | Transaction View | COTRN01C | Display transaction details |
| 11 | COTRN02.bms | 307 | Transaction Add | COTRN02C | Enter new transaction |
| 12 | CORPT00.bms | 231 | Report Request | CORPT00C | Configure report parameters |
| 13 | COBIL00.bms | 220 | Bill Payment | COBIL00C | Bill payment form |
| 14 | COUSR00.bms | 463 | User List | COUSR00C | Paginated user list |
| 15 | COUSR01.bms | 164 | User Add | COUSR01C | Add new user form |
| 16 | COUSR02.bms | 169 | User Update | COUSR02C | Edit user form |
| 17 | COUSR03.bms | 153 | User Delete | COUSR03C | Confirm user deletion |

---

## 5. BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map generates a corresponding copybook containing symbolic field definitions used by COBOL programs to send/receive 3270 screen data.

| # | Copybook | Source Map | Purpose |
|---|----------|-----------|---------|
| 1 | COSGN00.CPY | COSGN00.bms | Sign-on screen fields |
| 2 | COMEN01.CPY | COMEN01.bms | Main menu fields |
| 3 | COADM01.CPY | COADM01.bms | Admin menu fields |
| 4 | COACTVW.CPY | COACTVW.bms | Account view fields |
| 5 | COACTUP.CPY | COACTUP.bms | Account update fields |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card list fields |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card detail fields |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card update fields |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction list fields |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction view fields |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction add fields |
| 12 | CORPT00.CPY | CORPT00.bms | Report request fields |
| 13 | COBIL00.CPY | COBIL00.bms | Bill payment fields |
| 14 | COUSR00.CPY | COUSR00.bms | User list fields |
| 15 | COUSR01.CPY | COUSR01.bms | User add fields |
| 16 | COUSR02.CPY | COUSR02.bms | User update fields |
| 17 | COUSR03.CPY | COUSR03.bms | User delete fields |

---

## 6. JCL Batch Jobs (`app/jcl/`)

### 6.1 Data Refresh Jobs

| # | Job | Lines | Function | Executes Program | Datasets Involved |
|---|-----|------:|----------|-----------------|-------------------|
| 1 | ACCTFILE.jcl | 107 | Refresh account master VSAM | IDCAMS | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE.jcl | 106 | Refresh card master VSAM | IDCAMS | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE.jcl | 99 | Refresh customer master VSAM | IDCAMS | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE.jcl | 106 | Refresh cross-reference VSAM | IDCAMS | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE.jcl | 125 | Refresh transaction VSAM | IDCAMS | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ.jcl | 77 | Load user security VSAM | IDCAMS | USRSEC.VSAM.KSDS |

### 6.2 Core Batch Processing Jobs

| # | Job | Lines | Function | Executes Program | Classification |
|---|-----|------:|----------|-----------------|----------------|
| 7 | POSTTRAN.jcl | 45 | Post daily transactions | CBTRN02C | Transaction Processing |
| 8 | INTCALC.jcl | 44 | Calculate interest on accounts | CBACT04C | Financial Processing |
| 9 | COMBTRAN.jcl | 31 | Combine transaction files | SORT / IDCAMS | Data Consolidation |
| 10 | CREASTMT.JCL | 97 | Generate customer statements | SORT + CBSTM03A | Statement Generation |
| 11 | TRANBKP.jcl | 71 | Backup transaction data | REPROC + IDCAMS | Backup |
| 12 | TRANREPT.jcl | 84 | Generate transaction reports | REPROC + SORT + CBTRN03C | Reporting |

### 6.3 VSAM Definition / Maintenance Jobs

| # | Job | Lines | Function | Classification |
|---|-----|------:|----------|----------------|
| 13 | DEFGDGB.jcl | 25 | Define GDG base for backups | Infrastructure |
| 14 | DEFGDGD.jcl | 25 | Define GDG base for daily files | Infrastructure |
| 15 | TRANIDX.jcl | 58 | Define alternate indexes on transaction VSAM | Infrastructure |
| 16 | REPTFILE.jcl | 32 | Define report output VSAM | Infrastructure |
| 17 | TCATBALF.jcl | 65 | Define transaction category balance VSAM | Infrastructure |
| 18 | TRANCATG.jcl | 65 | Define transaction category VSAM | Infrastructure |
| 19 | TRANTYPE.jcl | 65 | Define transaction type VSAM | Infrastructure |
| 20 | DEFCUST.jcl | 25 | Define customer VSAM cluster | Infrastructure |
| 21 | DISCGRP.jcl | 65 | Define discount group VSAM | Infrastructure |
| 22 | ESDSRRDS.jcl | 77 | Load ESDS/RRDS VSAM datasets | Infrastructure |

### 6.4 Data Read / Print Jobs

| # | Job | Lines | Function | Executes Program |
|---|-----|------:|----------|-----------------|
| 23 | READACCT.jcl | 50 | Read and export account data | CBACT01C |
| 24 | READCARD.jcl | 31 | Read and print card data | CBACT02C |
| 25 | READCUST.jcl | 30 | Read and print customer data | CBCUS01C |
| 26 | READXREF.jcl | 31 | Read and print cross-reference | CBACT03C |
| 27 | PRTCATBL.jcl | 66 | Print category balance report | REPROC + SORT |
| 28 | DALYREJS.jcl | 25 | Define daily rejects file | Infrastructure |

### 6.5 CICS File Control Jobs

| # | Job | Lines | Function | Classification |
|---|-----|------:|----------|----------------|
| 29 | CLOSEFIL.jcl | 34 | Close CICS-managed files for batch | Operations |
| 30 | OPENFIL.jcl | 34 | Open CICS-managed files after batch | Operations |

### 6.6 Data Export / Import Jobs

| # | Job | Lines | Function | Executes Program |
|---|-----|------:|----------|-----------------|
| 31 | CBEXPORT.jcl | 58 | Export VSAM data to sequential | CBEXPORT |
| 32 | CBIMPORT.jcl | 47 | Import sequential data to VSAM | CBIMPORT |
| 33 | CBADMCDJ.jcl | 47 | Admin card data job | IDCAMS |

### 6.7 Utility / Infrastructure Jobs

| # | Job | Lines | Function | Classification |
|---|-----|------:|----------|----------------|
| 34 | WAITSTEP.jcl | 27 | Wait step for job scheduling | Utility |
| 35 | FTPJCL.JCL | 42 | FTP file transfer | Utility |
| 36 | INTRDRJ1.JCL | 19 | Internal reader job 1 (IDCAMS + IEBGENER) | Utility |
| 37 | INTRDRJ2.JCL | 14 | Internal reader job 2 (IDCAMS) | Utility |
| 38 | TXT2PDF1.JCL | 41 | Convert text statements to PDF | Utility |

---

## 7. Assembler Programs (`app/asm/`)

| # | Program | Function | Called By |
|---|---------|----------|-----------|
| 1 | COBDATFT.asm | Date formatting conversion | CBACT01C (via CALL) |
| 2 | MVSWAIT.asm | MVS wait / timer | COBSWAIT |

---

## 8. JCL Procedures (`app/proc/`)

| # | Procedure | Function | Used By |
|---|-----------|----------|---------|
| 1 | REPROC.prc | Reusable REPRO (VSAM copy) procedure | TRANBKP, TRANREPT, PRTCATBL |
| 2 | TRANREPT.prc | Transaction report procedure | TRANREPT.jcl |

---

## 9. Summary Statistics

| Category | Count | Total Lines |
|----------|------:|------------:|
| Core COBOL Programs | 31 | 20,650 |
| Optional Module Programs | 13 | 9,145 |
| Copybooks (data) | 30 | 2,786 |
| BMS-Generated Copybooks | 17 | ~2,500 |
| BMS Screen Maps | 17 | ~4,500 |
| JCL Batch Jobs | 38 | ~2,200 |
| Assembler Programs | 2 | ~200 |
| JCL Procedures | 2 | ~50 |
| **Total** | **150** | **~39,845** |

### Classification Breakdown

| Classification | Program Count |
|----------------|-------------:|
| Online CICS (UI-driven) | 17 |
| Batch Processing | 12 |
| Utility / Shared | 2 |
| Authorization (IMS/DB2/MQ) | 8 |
| Transaction Type (DB2) | 3 |
| VSAM-MQ Services | 2 |
