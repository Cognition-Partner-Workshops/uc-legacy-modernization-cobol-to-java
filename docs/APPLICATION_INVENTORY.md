# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Codebase Version:** CardDemo v2.0  
> **Total Artifacts:** 31 COBOL Programs + 30 Copybooks + 38 JCL Jobs + 17 BMS Maps + 2 ASM Programs + 2 JCL Procedures + 13 Optional-Module Programs

---

## Table of Contents

1. [Online CICS Programs (17)](#1-online-cics-programs)
2. [Batch COBOL Programs (13)](#2-batch-cobol-programs)
3. [Shared Utility Programs (1)](#3-shared-utility-programs)
4. [Copybooks - Data Structures (30)](#4-copybooks---data-structures)
5. [BMS Screen Maps (17)](#5-bms-screen-maps)
6. [BMS-Generated Copybooks (17)](#6-bms-generated-copybooks)
7. [JCL Batch Jobs (38)](#7-jcl-batch-jobs)
8. [JCL Procedures (2)](#8-jcl-procedures)
9. [Assembler Programs (2)](#9-assembler-programs)
10. [Optional Module: Authorization IMS/DB2/MQ (8 programs)](#10-optional-module-authorization-imsdb2mq)
11. [Optional Module: Transaction Type DB2 (3 programs)](#11-optional-module-transaction-type-db2)
12. [Optional Module: VSAM-MQ (2 programs)](#12-optional-module-vsam-mq)
13. [Summary Statistics](#13-summary-statistics)

---

## 1. Online CICS Programs

These programs run under CICS and handle interactive 3270 terminal screens. Naming convention: `CO*` prefix.

| # | Program | Lines | CICS Txn | Function | Domain | BMS Map | Classification |
|---|---------|-------|----------|----------|--------|---------|----------------|
| 1 | **COSGN00C.cbl** | 260 | CC00 | Sign-on / Authentication | Security | COSGN00 | Entry Point |
| 2 | **COMEN01C.cbl** | 308 | CM00 | Main Menu Router | Navigation | COMEN01 | Controller |
| 3 | **COADM01C.cbl** | 288 | CA00 | Admin Menu Router | Admin | COADM01 | Controller |
| 4 | **COACTVWC.cbl** | 941 | CA01 | Account View (read-only) | Account Mgmt | COACTVW | View |
| 5 | **COACTUPC.cbl** | 4,236 | CA02 | Account Update (edit) | Account Mgmt | COACTUP | CRUD - Update |
| 6 | **COCRDLIC.cbl** | 1,459 | CC01 | Credit Card List | Card Mgmt | COCRDLI | List/Browse |
| 7 | **COCRDSLC.cbl** | 887 | CC02 | Credit Card View | Card Mgmt | COCRDSL | View |
| 8 | **COCRDUPC.cbl** | 1,560 | CC03 | Credit Card Update | Card Mgmt | COCRDUP | CRUD - Update |
| 9 | **COTRN00C.cbl** | 699 | CT00 | Transaction List | Transaction Mgmt | COTRN00 | List/Browse |
| 10 | **COTRN01C.cbl** | 330 | CT01 | Transaction View | Transaction Mgmt | COTRN01 | View |
| 11 | **COTRN02C.cbl** | 783 | CT02 | Transaction Add | Transaction Mgmt | COTRN02 | CRUD - Create |
| 12 | **CORPT00C.cbl** | 649 | CR00 | Transaction Reports | Reporting | CORPT00 | Report |
| 13 | **COBIL00C.cbl** | 572 | CB00 | Bill Payment | Payments | COBIL00 | Transaction |
| 14 | **COUSR00C.cbl** | 695 | CU00 | User List (Admin) | User Security | COUSR00 | List/Browse |
| 15 | **COUSR01C.cbl** | 299 | CU01 | User Add (Admin) | User Security | COUSR01 | CRUD - Create |
| 16 | **COUSR02C.cbl** | 414 | CU02 | User Update (Admin) | User Security | COUSR02 | CRUD - Update |
| 17 | **COUSR03C.cbl** | 359 | CU03 | User Delete (Admin) | User Security | COUSR03 | CRUD - Delete |

**Online Program Total:** 14,739 lines across 17 programs

---

## 2. Batch COBOL Programs

These programs run in batch mode via JCL. Naming convention: `CB*` prefix.

| # | Program | Lines | Function | Domain | Classification |
|---|---------|-------|----------|--------|----------------|
| 1 | **CBACT01C.cbl** | 430 | Read account file with date formatting | Account Mgmt | File I/O |
| 2 | **CBACT02C.cbl** | 178 | Read card data file | Card Mgmt | File I/O |
| 3 | **CBACT03C.cbl** | 178 | Read card cross-reference file | Card Mgmt | File I/O |
| 4 | **CBACT04C.cbl** | 652 | Interest calculation on accounts | Financial | Calculation |
| 5 | **CBCUS01C.cbl** | 178 | Read customer data file | Customer Mgmt | File I/O |
| 6 | **CBTRN01C.cbl** | 494 | Read/process daily transactions | Transaction Mgmt | File I/O |
| 7 | **CBTRN02C.cbl** | 731 | Transaction posting (core batch) | Transaction Mgmt | Business Logic |
| 8 | **CBTRN03C.cbl** | 649 | Transaction report generation | Reporting | Report |
| 9 | **CBSTM03A.CBL** | 924 | Statement generation (main driver) | Reporting | Report Driver |
| 10 | **CBSTM03B.CBL** | 230 | Statement generation (page formatter) | Reporting | Report Sub |
| 11 | **CBEXPORT.cbl** | 582 | Multi-record data export to sequential file | Data Migration | ETL - Export |
| 12 | **CBIMPORT.cbl** | 487 | Multi-record data import from sequential file | Data Migration | ETL - Import |
| 13 | **COBSWAIT.cbl** | 41 | Wait utility (calls MVSWAIT assembler) | Utility | System Utility |

**Batch Program Total:** 5,754 lines across 13 programs

---

## 3. Shared Utility Programs

| # | Program | Lines | Function | Domain | Classification |
|---|---------|-------|----------|--------|----------------|
| 1 | **CSUTLDTC.cbl** | 157 | Date validation utility (calls CEEDAYS LE service) | Utility | Shared Service |

---

## 4. Copybooks - Data Structures

### 4a. Business Entity Copybooks (`CV*` prefix)

| # | Copybook | Lines | Record Layout | Record Length | Entity |
|---|----------|-------|---------------|---------------|--------|
| 1 | **CVACT01Y.cpy** | 21 | ACCOUNT-RECORD | 300 bytes | Account Master |
| 2 | **CVACT02Y.cpy** | 15 | CARD-RECORD | 150 bytes | Card Master |
| 3 | **CVACT03Y.cpy** | 12 | CARD-XREF-RECORD | 50 bytes | Card Cross-Reference |
| 4 | **CVCUS01Y.cpy** | 27 | CUSTOMER-RECORD | 500 bytes | Customer Master |
| 5 | **CVTRA01Y.cpy** | 14 | TRAN-CAT-BAL-RECORD | 50 bytes | Transaction Category Balance |
| 6 | **CVTRA02Y.cpy** | 14 | DIS-GROUP-RECORD | 50 bytes | Disclosure Group |
| 7 | **CVTRA03Y.cpy** | 11 | TRAN-TYPE-RECORD | 60 bytes | Transaction Type |
| 8 | **CVTRA04Y.cpy** | 13 | TRAN-CAT-RECORD | 60 bytes | Transaction Category |
| 9 | **CVTRA05Y.cpy** | 22 | TRAN-RECORD | 350 bytes | Transaction (Online) |
| 10 | **CVTRA06Y.cpy** | 22 | DALYTRAN-RECORD | 350 bytes | Daily Transaction |
| 11 | **CVTRA07Y.cpy** | 74 | TRANSACTION-DETAIL-REPORT | N/A | Transaction Report Layout |
| 12 | **CVEXPORT.cpy** | 104 | EXPORT-RECORD | 500 bytes | Multi-Record Export (REDEFINES) |
| 13 | **CVCRD01Y.cpy** | 47 | CC-WORK-AREAS | N/A | Card Work Areas / Navigation |
| 14 | **CUSTREC.cpy** | 27 | CUSTOMER-RECORD | 500 bytes | Customer (alternate layout) |

### 4b. Application Control Copybooks (`CO*` / `CS*` / `CM*` prefix)

| # | Copybook | Lines | Record Layout | Purpose |
|---|----------|-------|---------------|---------|
| 15 | **COCOM01Y.cpy** | 48 | CARDDEMO-COMMAREA | Inter-program communication area |
| 16 | **COMEN02Y.cpy** | 102 | CARDDEMO-MAIN-MENU-OPTIONS | Main menu option definitions (11 options) |
| 17 | **COADM02Y.cpy** | 63 | CARDDEMO-ADMIN-MENU-OPTIONS | Admin menu option definitions (6 options) |
| 18 | **COSTM01.CPY** | 37 | TRNX-RECORD | Transaction altered layout for reporting |
| 19 | **COTTL01Y.cpy** | - | Title/Header | Screen title definitions |
| 20 | **CSDAT01Y.cpy** | 59 | WS-DATE-TIME | Date/time working storage |
| 21 | **CSMSG01Y.cpy** | 25 | CCDA-COMMON-MESSAGES | Common application messages |
| 22 | **CSMSG02Y.cpy** | - | Messages (extended) | Additional message definitions |
| 23 | **CSSETATY.cpy** | - | Screen attribute settings | BMS field attribute control |
| 24 | **CSSTRPFY.cpy** | - | String processing | String manipulation paragraphs |
| 25 | **CSUSR01Y.cpy** | 27 | SEC-USER-DATA | User security record (80 bytes) |
| 26 | **CSUTLDPY.cpy** | 376 | Date validation procedures | Reusable date edit paragraphs |
| 27 | **CSUTLDWY.cpy** | 90 | Date validation working storage | Working storage for date edits |
| 28 | **CSLKPCDY.cpy** | 1,318 | Lookup code tables | Phone area codes, state codes, ZIP prefixes |
| 29 | **CODATECN.cpy** | - | Date conversion | Assembler date format interface |
| 30 | **UNUSED1Y.cpy** | - | (Unused placeholder) | Reserved |

---

## 5. BMS Screen Maps

| # | Map File | Screen ID | Associated Program | Screen Purpose |
|---|----------|-----------|-------------------|----------------|
| 1 | **COSGN00.bms** | COSGN0A | COSGN00C | Sign-on screen |
| 2 | **COMEN01.bms** | COMEN1A | COMEN01C | Main menu screen |
| 3 | **COADM01.bms** | COADM1A | COADM01C | Admin menu screen |
| 4 | **COACTVW.bms** | CACTVWA | COACTVWC | Account view screen |
| 5 | **COACTUP.bms** | CACTUPA | COACTUPC | Account update screen |
| 6 | **COCRDLI.bms** | CCRDLIA | COCRDLIC | Card list screen |
| 7 | **COCRDSL.bms** | CCRDSLA | COCRDSLC | Card detail/view screen |
| 8 | **COCRDUP.bms** | CCRDUPA | COCRDUPC | Card update screen |
| 9 | **COTRN00.bms** | COTRN0A | COTRN00C | Transaction list screen |
| 10 | **COTRN01.bms** | COTRN1A | COTRN01C | Transaction view screen |
| 11 | **COTRN02.bms** | COTRN2A | COTRN02C | Transaction add screen |
| 12 | **CORPT00.bms** | CORPT0A | CORPT00C | Report parameters screen |
| 13 | **COBIL00.bms** | COBIL0A | COBIL00C | Bill payment screen |
| 14 | **COUSR00.bms** | COUSR0A | COUSR00C | User list screen |
| 15 | **COUSR01.bms** | COUSR1A | COUSR01C | User add screen |
| 16 | **COUSR02.bms** | COUSR2A | COUSR02C | User update screen |
| 17 | **COUSR03.bms** | COUSR3A | COUSR03C | User delete screen |

---

## 6. BMS-Generated Copybooks

Located in `app/cpy-bms/`. Each mirrors a BMS map and contains the symbolic field definitions generated by the BMS macro assembler. These are COPY'd into the corresponding online program.

| # | Copybook | Generated From |
|---|----------|---------------|
| 1 | COSGN00.CPY | COSGN00.bms |
| 2 | COMEN01.CPY | COMEN01.bms |
| 3 | COADM01.CPY | COADM01.bms |
| 4 | COACTVW.CPY | COACTVW.bms |
| 5 | COACTUP.CPY | COACTUP.bms |
| 6 | COCRDLI.CPY | COCRDLI.bms |
| 7 | COCRDSL.CPY | COCRDSL.bms |
| 8 | COCRDUP.CPY | COCRDUP.bms |
| 9 | COTRN00.CPY | COTRN00.bms |
| 10 | COTRN01.CPY | COTRN01.bms |
| 11 | COTRN02.CPY | COTRN02.bms |
| 12 | CORPT00.CPY | CORPT00.bms |
| 13 | COBIL00.CPY | COBIL00.bms |
| 14 | COUSR00.CPY | COUSR00.bms |
| 15 | COUSR01.CPY | COUSR01.bms |
| 16 | COUSR02.CPY | COUSR02.bms |
| 17 | COUSR03.CPY | COUSR03.bms |

---

## 7. JCL Batch Jobs

### 7a. Data File Loading / Refresh

| # | JCL Job | Function | Key Program/Utility | Target Dataset |
|---|---------|----------|--------------------|--------------------|
| 1 | **ACCTFILE.jcl** | Refresh account master VSAM | SDSF / IDCAMS | ACCTDATA VSAM KSDS |
| 2 | **CARDFILE.jcl** | Refresh card master VSAM | SDSF / IDCAMS | CARDDATA VSAM KSDS |
| 3 | **CUSTFILE.jcl** | Refresh customer master VSAM | SDSF / IDCAMS | CUSTDATA VSAM KSDS |
| 4 | **XREFFILE.jcl** | Load card cross-reference VSAM | IDCAMS | CARDXREF VSAM KSDS |
| 5 | **TRANFILE.jcl** | Load transaction master VSAM | SDSF / IDCAMS | TRANDATA VSAM KSDS |
| 6 | **DUSRSECJ.jcl** | Load user security VSAM | IEBGENER / IDCAMS | USRSEC VSAM KSDS |
| 7 | **DEFCUST.jcl** | Define customer VSAM cluster | IDCAMS | CUSTDATA cluster |
| 8 | **ESDSRRDS.jcl** | Define ESDS/RRDS VSAM datasets | IDCAMS | ESDS/RRDS clusters |

### 7b. Batch Processing Cycle

| # | JCL Job | Function | Key Program | Cycle Order |
|---|---------|----------|-------------|-------------|
| 9 | **CLOSEFIL.jcl** | Close CICS files for batch | SDSF | 1 - Pre-batch |
| 10 | **POSTTRAN.jcl** | Core transaction posting | CBTRN02C | 2 - Processing |
| 11 | **INTCALC.jcl** | Interest calculation | CBACT04C | 3 - Calculation |
| 12 | **TRANBKP.jcl** | Backup transaction file | IDCAMS REPRO | 4 - Backup |
| 13 | **COMBTRAN.jcl** | Combine daily + master transactions | SORT/MERGE | 5 - Combine |
| 14 | **CREASTMT.JCL** | Create account statements | CBSTM03A | 6 - Statements |
| 15 | **TRANIDX.jcl** | Define alternate index on transactions | IDCAMS | 7 - Index |
| 16 | **OPENFIL.jcl** | Reopen CICS files after batch | SDSF | 8 - Post-batch |

### 7c. Reporting and Utilities

| # | JCL Job | Function | Key Program |
|---|---------|----------|-------------|
| 17 | **TRANREPT.jcl** | Transaction report | CBTRN03C |
| 18 | **REPTFILE.jcl** | Report file processing | SORT/utility |
| 19 | **PRTCATBL.jcl** | Print category balance | IDCAMS PRINT |
| 20 | **TCATBALF.jcl** | Transaction category balance file | IDCAMS |
| 21 | **TRANCATG.jcl** | Transaction catalog maintenance | IDCAMS |
| 22 | **TRANTYPE.jcl** | Transaction type file maintenance | IDCAMS |
| 23 | **DISCGRP.jcl** | Disclosure group file maintenance | IDCAMS |
| 24 | **DALYREJS.jcl** | Daily rejects processing | SORT/utility |

### 7d. Data Read / Verification

| # | JCL Job | Function | Key Program |
|---|---------|----------|-------------|
| 25 | **READACCT.jcl** | Read/verify account file | CBACT01C |
| 26 | **READCARD.jcl** | Read/verify card file | CBACT02C |
| 27 | **READCUST.jcl** | Read/verify customer file | CBCUS01C |
| 28 | **READXREF.jcl** | Read/verify cross-reference file | CBACT03C |

### 7e. Data Export / Import

| # | JCL Job | Function | Key Program |
|---|---------|----------|-------------|
| 29 | **CBEXPORT.jcl** | Export multi-record data | CBEXPORT |
| 30 | **CBIMPORT.jcl** | Import multi-record data | CBIMPORT |

### 7f. GDG and Infrastructure

| # | JCL Job | Function | Key Utility |
|---|---------|----------|------------|
| 31 | **DEFGDGB.jcl** | Define GDG base entries | IDCAMS |
| 32 | **DEFGDGD.jcl** | Define GDG data entries + backup | IEBGENER |
| 33 | **WAITSTEP.jcl** | Wait step utility | COBSWAIT |
| 34 | **CBADMCDJ.jcl** | Admin card demo job | IKJEFT01 |
| 35 | **FTPJCL.JCL** | FTP file transfer | FTP |
| 36 | **INTRDRJ1.JCL** | Internal reader job 1 | IDCAMS |
| 37 | **INTRDRJ2.JCL** | Internal reader job 2 | IDCAMS |
| 38 | **TXT2PDF1.JCL** | Text to PDF conversion | IEBGENER |

---

## 8. JCL Procedures

| # | Procedure | Function |
|---|-----------|----------|
| 1 | **REPROC.prc** | Reusable report processing procedure |
| 2 | **TRANREPT.prc** | Transaction report procedure |

---

## 9. Assembler Programs

| # | Program | Function | Called By |
|---|---------|----------|----------|
| 1 | **MVSWAIT.asm** | System wait/delay utility | COBSWAIT.cbl |
| 2 | **COBDATFT.asm** | Date formatting (EBCDIC conversion) | CBACT01C.cbl |

---

## 10. Optional Module: Authorization IMS/DB2/MQ

Located in `app/app-authorization-ims-db2-mq/`. Adds pending authorization functionality using IMS DB, DB2, and MQ Series.

### Programs

| # | Program | Lines | Function | Classification |
|---|---------|-------|----------|----------------|
| 1 | **COPAUA0C.cbl** | 1,026 | MQ trigger monitor for authorization requests | MQ Integration |
| 2 | **COPAUS0C.cbl** | 1,032 | Authorization summary view (CICS online) | Online - View |
| 3 | **COPAUS1C.cbl** | 604 | Authorization detail view (CICS online) | Online - View |
| 4 | **COPAUS2C.cbl** | 244 | Mark transaction as fraud (DB2 update) | Online - Update |
| 5 | **CBPAUP0C.cbl** | 386 | Batch purge of processed authorizations | Batch |
| 6 | **DBUNLDGS.CBL** | 366 | Unload IMS DB to GSAM sequential file | Batch - ETL |
| 7 | **PAUDBLOD.CBL** | 369 | Load data into IMS DB | Batch - ETL |
| 8 | **PAUDBUNL.CBL** | 317 | Unload IMS DB segments | Batch - ETL |

### Copybooks (9)

CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB

### BMS Maps (2)

COPAU00.bms (Summary screen), COPAU01.bms (Detail screen)

### JCL Jobs (5)

CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB

---

## 11. Optional Module: Transaction Type DB2

Located in `app/app-transaction-type-db2/`. Adds DB2-based transaction type management.

### Programs

| # | Program | Lines | Function | Classification |
|---|---------|-------|----------|----------------|
| 1 | **COTRTLIC.cbl** | 2,098 | Transaction type list / delete (CICS + DB2) | Online - List |
| 2 | **COTRTUPC.cbl** | 1,702 | Transaction type add / edit (CICS + DB2) | Online - CRUD |
| 3 | **COBTUPDT.cbl** | 237 | Batch update of transaction types via DB2 | Batch |

### Copybooks (2)

CSDB2RPY (DB2 reply), CSDB2RWY (DB2 working storage)

### BMS Maps (2)

COTRTLI.bms (List screen), COTRTUP.bms (Update screen)

### JCL Jobs (3)

CREADB21 (Create DB2 objects), MNTTRDB2 (Maintain trans types), TRANEXTR (Extract transactions)

---

## 12. Optional Module: VSAM-MQ

Located in `app/app-vsam-mq/`. Provides MQ-based request/response services for system date and account inquiry.

### Programs

| # | Program | Lines | Function | Classification |
|---|---------|-------|----------|----------------|
| 1 | **CODATE01.cbl** | 524 | MQ service: system date request/response | MQ Service |
| 2 | **COACCT01.cbl** | 620 | MQ service: account inquiry via VSAM | MQ Service |

---

## 13. Summary Statistics

| Category | Count | Total Lines |
|----------|-------|-------------|
| Online CICS Programs | 17 | 14,739 |
| Batch COBOL Programs | 13 | 5,754 |
| Shared Utilities | 1 | 157 |
| **Core Programs Subtotal** | **31** | **20,650** |
| Optional Module Programs | 13 | 9,525 |
| **All Programs Total** | **44** | **30,175** |
| Copybooks (core) | 30 | ~2,400+ |
| Copybooks (optional modules) | 11 | ~400+ |
| BMS Maps (core) | 17 | - |
| BMS Maps (optional) | 4 | - |
| BMS-Generated Copybooks | 17 | - |
| JCL Jobs (core) | 38 | - |
| JCL Jobs (optional) | 8 | - |
| JCL Procedures | 2 | - |
| Assembler Programs | 2 | - |

### Domain Distribution

| Business Domain | Programs | Classification |
|----------------|----------|----------------|
| Account Management | 4 | Core |
| Card Management | 5 | Core |
| Transaction Management | 7 | Core |
| Customer Management | 1 | Core |
| User Security / Admin | 6 | Core |
| Reporting / Statements | 4 | Core |
| Bill Payment | 1 | Core |
| Data Migration (Export/Import) | 2 | Core |
| Utility | 2 | Core |
| Authorization (IMS/DB2/MQ) | 8 | Optional |
| Transaction Type (DB2) | 3 | Optional |
| VSAM-MQ Services | 2 | Optional |

### Technology Stack

| Technology | Usage |
|-----------|-------|
| COBOL | Primary language (all programs) |
| CICS | Online transaction processing |
| VSAM KSDS | Primary data storage (accounts, cards, customers, transactions) |
| BMS | 3270 terminal screen definitions |
| JCL | Batch job control |
| IMS DB | Hierarchical database (optional authorization module) |
| DB2 | Relational database (optional transaction type module) |
| MQ Series | Message queuing (optional authorization + VSAM-MQ modules) |
| SORT/MERGE | Batch data sorting and merging |
| IDCAMS | VSAM utility (define, repro, print, delete) |
| Assembler | System utilities (wait, date formatting) |
| GDG | Generation data groups for backup/versioning |
