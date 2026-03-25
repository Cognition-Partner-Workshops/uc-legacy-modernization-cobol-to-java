# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Platform:** IBM z/OS (CICS/VSAM/JCL)

## Executive Summary

CardDemo is a mainframe credit card management application built on COBOL/CICS/VSAM/JCL. It simulates account management, card management, transactions, bill payments, and reporting. The system supports two user roles: **Regular** (card operations) and **Admin** (user/transaction-type management).

| Category | Core | Optional Modules | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks | 30 | 11 | **41** |
| BMS Maps | 17 | 4 | **21** |
| BMS Copybooks | 17 | 4 | **21** |
| JCL Jobs | 38 | - | **38** |
| Assembler Programs | 2 | - | **2** |
| JCL Procedures | 2 | - | **2** |
| Scheduler Configs | 2 | - | **2** |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (CO* prefix)

| # | Program | Lines | Type | CICS Txn | Function | Business Domain | Screen Map |
|---|---------|-------|------|----------|----------|-----------------|------------|
| 1 | COSGN00C.cbl | 260 | Online | CC00 | Sign-on / Login | Security | COSGN00 |
| 2 | COMEN01C.cbl | 308 | Online | CM00 | Main Menu Navigation | Navigation | COMEN01 |
| 3 | COADM01C.cbl | 288 | Online | CA00 | Admin Menu | Administration | COADM01 |
| 4 | COACTVWC.cbl | 941 | Online | - | Account View (detail) | Account Mgmt | COACTVW |
| 5 | COACTUPC.cbl | 4,236 | Online | - | Account Update | Account Mgmt | COACTUP |
| 6 | COCRDLIC.cbl | 1,459 | Online | - | Card List (browse) | Card Mgmt | COCRDLI |
| 7 | COCRDSLC.cbl | 887 | Online | - | Card Detail View | Card Mgmt | COCRDSL |
| 8 | COCRDUPC.cbl | 1,560 | Online | - | Card Update | Card Mgmt | COCRDUP |
| 9 | COTRN00C.cbl | 699 | Online | - | Transaction List (browse) | Transactions | COTRN00 |
| 10 | COTRN01C.cbl | 330 | Online | - | Transaction View (detail) | Transactions | COTRN01 |
| 11 | COTRN02C.cbl | 783 | Online | - | Transaction Add (new) | Transactions | COTRN02 |
| 12 | CORPT00C.cbl | 649 | Online | - | Transaction Report Request | Reporting | CORPT00 |
| 13 | COBIL00C.cbl | 572 | Online | - | Bill Payment Processing | Billing | COBIL00 |
| 14 | COUSR00C.cbl | 695 | Online | - | User List (admin browse) | User Admin | COUSR00 |
| 15 | COUSR01C.cbl | 299 | Online | - | User Add (admin) | User Admin | COUSR01 |
| 16 | COUSR02C.cbl | 414 | Online | - | User Update (admin) | User Admin | COUSR02 |
| 17 | COUSR03C.cbl | 359 | Online | - | User Delete (admin) | User Admin | COUSR03 |

### 1.2 Batch Programs (CB* prefix)

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 18 | CBACT01C.cbl | 430 | Batch | Read account file, write multi-format output | Account Mgmt |
| 19 | CBACT02C.cbl | 178 | Batch | Read card file (validation) | Card Mgmt |
| 20 | CBACT03C.cbl | 178 | Batch | Read cross-reference file (validation) | Card Mgmt |
| 21 | CBACT04C.cbl | 652 | Batch | Interest calculation on accounts | Financial Processing |
| 22 | CBCUS01C.cbl | 178 | Batch | Read customer file (validation) | Customer Mgmt |
| 23 | CBTRN01C.cbl | 494 | Batch | Daily transaction validation | Transaction Processing |
| 24 | CBTRN02C.cbl | 731 | Batch | Transaction posting (core batch) | Transaction Processing |
| 25 | CBTRN03C.cbl | 649 | Batch | Transaction report generation | Reporting |
| 26 | CBSTM03A.CBL | 924 | Batch | Statement generation (text + HTML) | Reporting |
| 27 | CBSTM03B.CBL | 230 | Batch (sub) | File I/O subroutine for CBSTM03A | Reporting |
| 28 | CBEXPORT.cbl | 582 | Batch | Export all VSAM data to flat file | Data Migration |
| 29 | CBIMPORT.cbl | 487 | Batch | Import flat file data into VSAM files | Data Migration |

### 1.3 Utility Programs

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 30 | CSUTLDTC.cbl | 157 | Utility | Date conversion via CEEDAYS LE callable | Shared Utility |
| 31 | COBSWAIT.cbl | 41 | Utility | Wait/sleep utility for batch scheduling | Infrastructure |

### 1.4 Optional Module: Authorization (IMS/DB2/MQ) (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 32 | COPAUA0C.cbl | 1,026 | CICS/IMS/MQ | Card authorization decision (MQ trigger) | Authorization |
| 33 | COPAUS0C.cbl | 1,032 | CICS/IMS/BMS | Auth message summary view | Authorization |
| 34 | COPAUS1C.cbl | 604 | CICS/IMS/BMS | Auth message detail view | Authorization |
| 35 | COPAUS2C.cbl | 244 | CICS/IMS/DB2 | Mark auth message as fraud (DB2) | Fraud Detection |
| 36 | CBPAUP0C.cbl | 386 | Batch/IMS | Purge expired auth messages | Authorization |
| 37 | DBUNLDGS.CBL | 366 | Batch/IMS | Unload IMS database segments | Data Utility |
| 38 | PAUDBLOD.CBL | 369 | Batch/IMS | Load IMS auth database | Data Utility |
| 39 | PAUDBUNL.CBL | 317 | Batch/IMS | Unload IMS auth database | Data Utility |

### 1.5 Optional Module: Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 40 | COTRTLIC.cbl | 2,098 | CICS/DB2 | Transaction type list (DB2 cursors) | Config Admin |
| 41 | COTRTUPC.cbl | 1,702 | CICS/DB2 | Transaction type add/edit/delete | Config Admin |
| 42 | COBTUPDT.cbl | 237 | Batch/DB2 | Batch update of transaction types | Config Admin |

### 1.6 Optional Module: VSAM-MQ (`app/app-vsam-mq/cbl/`)

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 43 | COACCT01.cbl | 620 | CICS/MQ | MQ request/response for account inquiry | Account Inquiry |
| 44 | CODATE01.cbl | 524 | CICS/MQ | MQ request/response for system date | System Services |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (CV* prefix = VSAM record structures)

| # | Copybook | Lines | Record Length | Function | Primary Entity |
|---|----------|-------|---------------|----------|----------------|
| 1 | CVACT01Y.cpy | 20 | 300 bytes | Account master record | Account |
| 2 | CVACT02Y.cpy | 14 | 150 bytes | Card master record | Card |
| 3 | CVACT03Y.cpy | 11 | 50 bytes | Card-to-account cross-reference | Card-Account Xref |
| 4 | CVCUS01Y.cpy | 26 | 500 bytes | Customer master record | Customer |
| 5 | CVCRD01Y.cpy | 46 | - | Card detail record (extended) | Card |
| 6 | CVTRA01Y.cpy | 13 | 50 bytes | Transaction category balance | Tran Cat Balance |
| 7 | CVTRA02Y.cpy | 13 | 50 bytes | Disclosure group record | Disclosure Group |
| 8 | CVTRA03Y.cpy | 10 | 60 bytes | Transaction type master | Transaction Type |
| 9 | CVTRA04Y.cpy | 12 | 60 bytes | Transaction category type | Transaction Category |
| 10 | CVTRA05Y.cpy | 21 | 350 bytes | Transaction record (online) | Transaction |
| 11 | CVTRA06Y.cpy | 21 | 350 bytes | Daily transaction record (batch input) | Daily Transaction |
| 12 | CVTRA07Y.cpy | 73 | - | Transaction report layout | Report Layout |
| 13 | CUSTREC.cpy | 26 | - | Customer record (alternate layout) | Customer |
| 14 | CVEXPORT.cpy | 103 | - | Export/import record layout | Data Migration |
| 15 | COSTM01.CPY | 38 | 350 bytes | Statement transaction layout (key=card+tran) | Statement |
| 16 | UNUSED1Y.cpy | 10 | 80 bytes | Unused placeholder record | N/A |

### 2.2 Communication / UI Copybooks (CO*/CS*/CO* prefix)

| # | Copybook | Lines | Function | Used By |
|---|----------|-------|----------|---------|
| 17 | COCOM01Y.cpy | 47 | Common communication area (COMMAREA) | All online programs |
| 18 | COADM02Y.cpy | 62 | Admin menu option definitions | COADM01C |
| 19 | COMEN02Y.cpy | 101 | Main menu option definitions | COMEN01C |
| 20 | COTTL01Y.cpy | 27 | Screen title/header layout | All online programs |
| 21 | CSDAT01Y.cpy | 58 | Date/time display fields | All online programs |
| 22 | CSMSG01Y.cpy | 24 | Message display area (line 1) | All online programs |
| 23 | CSMSG02Y.cpy | 35 | Message display area (line 2) | Complex programs |
| 24 | CSUSR01Y.cpy | 26 | User security record | Signon, user admin |
| 25 | CODATECN.cpy | 52 | Date conversion fields (assembler interface) | CBACT01C |
| 26 | CSLKPCDY.cpy | 1,318 | Lookup code tables (country, state, etc.) | COACTUPC |
| 27 | CSSETATY.cpy | 30 | Set attribute byte (REPLACING pattern) | COACTUPC, COCRDUPC |
| 28 | CSSTRPFY.cpy | 85 | String padding/formatting utility | Account view/update |
| 29 | CSUTLDPY.cpy | 375 | Date utility working storage | COACTUPC |
| 30 | CSUTLDWY.cpy | 89 | Date utility fields | COACTUPC, COCRDUPC |

### 2.3 Optional Module Copybooks

**Authorization (IMS/DB2/MQ)** (`app/app-authorization-ims-db2-mq/cpy/`):

| # | Copybook | Function |
|---|----------|----------|
| 31 | CIPAUSMY.cpy | Auth summary IMS segment layout |
| 32 | CIPAUDTY.cpy | Auth detail IMS segment layout |
| 33 | CCPAUERY.cpy | Auth error response layout |
| 34 | CCPAURQY.cpy | Auth request message layout |
| 35 | CCPAURLY.cpy | Auth reply message layout |
| 36 | IMSFUNCS.cpy | IMS DL/I function codes |
| 37 | PAUTBPCB.CPY | IMS PCB masks for auth DB |
| 38 | PADFLPCB.CPY | IMS PCB masks (detail) |
| 39 | PASFLPCB.CPY | IMS PCB masks (summary) |

**Transaction Type DB2** (`app/app-transaction-type-db2/cpy/`):

| # | Copybook | Function |
|---|----------|----------|
| 40 | CSDB2RWY.cpy | DB2 SQLCA/return code working storage |
| 41 | CSDB2RPY.cpy | DB2 return code paragraph (error handler) |

---

## 3. BMS Maps (`app/bms/`)

| # | Map File | Map Set | Map Name | Lines | Screen Title | Associated Program |
|---|----------|---------|----------|-------|--------------|-------------------|
| 1 | COSGN00.bms | COSGN00 | COSGN0A | 210 | Login Screen | COSGN00C |
| 2 | COMEN01.bms | COMEN01 | COMEN1A | 167 | Main Menu | COMEN01C |
| 3 | COADM01.bms | COADM01 | COADM1A | 167 | Admin Menu | COADM01C |
| 4 | COACTVW.bms | COACTVW | CACTVWA | 378 | Account View | COACTVWC |
| 5 | COACTUP.bms | COACTUP | CACTUPA | 512 | Account Update | COACTUPC |
| 6 | COCRDLI.bms | COCRDLI | CCRDLIA | 344 | Card List | COCRDLIC |
| 7 | COCRDSL.bms | COCRDSL | CCRDSL | 157 | Card Detail | COCRDSLC |
| 8 | COCRDUP.bms | COCRDUP | CCRDUPA | 172 | Card Update | COCRDUPC |
| 9 | COTRN00.bms | COTRN00 | COTRN0A | 464 | Transaction List | COTRN00C |
| 10 | COTRN01.bms | COTRN01 | COTRN1A | 273 | Transaction View | COTRN01C |
| 11 | COTRN02.bms | COTRN02 | COTRN2A | 307 | Transaction Add | COTRN02C |
| 12 | CORPT00.bms | CORPT00 | CORPT0A | 231 | Transaction Report | CORPT00C |
| 13 | COBIL00.bms | COBIL00 | COBIL0A | 141 | Bill Payment | COBIL00C |
| 14 | COUSR00.bms | COUSR00 | COUSR0A | 463 | User List | COUSR00C |
| 15 | COUSR01.bms | COUSR01 | COUSR1A | 164 | Add User | COUSR01C |
| 16 | COUSR02.bms | COUSR02 | COUSR2A | 169 | Update User | COUSR02C |
| 17 | COUSR03.bms | COUSR03 | COUSR3A | 153 | Delete User | COUSR03C |

### BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map has a corresponding generated copybook in `app/cpy-bms/` (17 files), providing symbolic field definitions for COBOL programs to reference screen fields.

### Optional Module BMS Maps

**Authorization** (`app/app-authorization-ims-db2-mq/bms/`):

| # | Map File | Screen Title |
|---|----------|--------------|
| 18 | COPAU00.bms | Auth Summary View |
| 19 | COPAU01.bms | Auth Detail View |

**Transaction Type DB2** (`app/app-transaction-type-db2/bms/`):

| # | Map File | Screen Title |
|---|----------|--------------|
| 20 | COTRTLI.bms | Transaction Type List |
| 21 | COTRTUP.bms | Transaction Type Update |

---

## 4. JCL Jobs (`app/jcl/`)

### 4.1 Data File Management

| # | Job | Function | Category |
|---|-----|----------|----------|
| 1 | ACCTFILE.jcl | Define and load Account Master VSAM KSDS | Data Load |
| 2 | CARDFILE.jcl | Define and load Card Master VSAM KSDS | Data Load |
| 3 | CUSTFILE.jcl | Define and load Customer Master VSAM KSDS | Data Load |
| 4 | XREFFILE.jcl | Define and load Card Cross-Reference VSAM KSDS + AIX | Data Load |
| 5 | TRANFILE.jcl | Define and load Transaction Master VSAM KSDS | Data Load |
| 6 | DUSRSECJ.jcl | Define and load User Security VSAM KSDS | Data Load |
| 7 | DEFCUST.jcl | Define Customer VSAM cluster only | VSAM Define |
| 8 | DEFGDGB.jcl | Define GDG base for transaction backup | VSAM Define |
| 9 | DEFGDGD.jcl | Define GDG base for daily transactions | VSAM Define |
| 10 | ESDSRRDS.jcl | Define ESDS and RRDS VSAM files | VSAM Define |

### 4.2 Batch Processing Cycle

| # | Job | Program | Function | Category |
|---|-----|---------|----------|----------|
| 11 | CLOSEFIL.jcl | - | Close CICS files for batch processing | Batch Cycle |
| 12 | OPENFIL.jcl | - | Re-open CICS files after batch | Batch Cycle |
| 13 | POSTTRAN.jcl | CBTRN02C | Post daily transactions to master | Transaction Processing |
| 14 | INTCALC.jcl | CBACT04C | Calculate interest on accounts | Financial Processing |
| 15 | TRANBKP.jcl | - | Backup transaction file (GDG roll) | Backup |
| 16 | COMBTRAN.jcl | - | Combine daily + master transactions | Transaction Processing |
| 17 | CREASTMT.JCL | CBSTM03A | Generate account statements (text+HTML) | Reporting |
| 18 | TRANIDX.jcl | - | Define/build alternate index on transactions | Indexing |

### 4.3 Reporting and Utilities

| # | Job | Program | Function | Category |
|---|-----|---------|----------|----------|
| 19 | TRANREPT.jcl | CBTRN03C | Generate daily transaction report | Reporting |
| 20 | REPTFILE.jcl | - | Define report output VSAM file | VSAM Define |
| 21 | DALYREJS.jcl | - | Define daily rejects sequential file | VSAM Define |
| 22 | DISCGRP.jcl | - | Define disclosure group VSAM file | VSAM Define |
| 23 | TRANCATG.jcl | - | Define transaction category VSAM file | VSAM Define |
| 24 | TRANTYPE.jcl | - | Define transaction type VSAM file | VSAM Define |
| 25 | TCATBALF.jcl | - | Define transaction category balance file | VSAM Define |
| 26 | TXT2PDF1.JCL | TXT2PDF | Convert statement text to PDF | Utility |
| 27 | PRTCATBL.jcl | - | Print catalog listing | Utility |

### 4.4 Data Export/Import

| # | Job | Program | Function | Category |
|---|-----|---------|----------|----------|
| 28 | CBEXPORT.jcl | CBEXPORT | Export all VSAM data to flat file | Data Migration |
| 29 | CBIMPORT.jcl | CBIMPORT | Import flat file data back into VSAM | Data Migration |

### 4.5 Validation/Read Jobs

| # | Job | Program | Function | Category |
|---|-----|---------|----------|----------|
| 30 | READACCT.jcl | CBACT01C | Read and validate account file | Validation |
| 31 | READCARD.jcl | CBACT02C | Read and validate card file | Validation |
| 32 | READCUST.jcl | CBCUS01C | Read and validate customer file | Validation |
| 33 | READXREF.jcl | CBACT03C | Read and validate cross-reference file | Validation |
| 34 | CBADMCDJ.jcl | - | Admin card demo job | Admin |

### 4.6 Infrastructure / FTP / Internal Reader

| # | Job | Function | Category |
|---|-----|----------|----------|
| 35 | FTPJCL.JCL | FTP file transfer to/from mainframe | Infrastructure |
| 36 | INTRDRJ1.JCL | Internal reader - trigger another JCL | Infrastructure |
| 37 | INTRDRJ2.JCL | Internal reader - triggered VSAM copy | Infrastructure |
| 38 | WAITSTEP.jcl | Wait/sleep step for job scheduling | Infrastructure |

---

## 5. JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Reusable report processing procedure |
| 2 | TRANREPT.prc | Transaction report procedure |

## 6. Assembler Programs (`app/asm/`)

| # | Program | Function |
|---|---------|----------|
| 1 | COBDATFT.asm | Date formatting assembler routine (called by CBACT01C) |
| 2 | MVSWAIT.asm | MVS WAIT macro assembler routine |

## 7. Scheduler Configurations (`app/scheduler/`)

| # | Config | Function |
|---|--------|----------|
| 1 | CardDemo.ca7 | CA-7 job scheduler definitions |
| 2 | CardDemo.controlm | Control-M job scheduler definitions |

## 8. CICS Resource Definitions (`app/csd/`)

The `CARDDEMO.CSD` file defines CICS system definitions including program definitions, transaction definitions, file definitions, and map set definitions for the online CICS environment.

---

## Classification Summary

### By Business Domain

| Domain | Programs | Criticality |
|--------|----------|-------------|
| Transaction Processing | CBTRN01C, CBTRN02C, COTRN00C, COTRN01C, COTRN02C | **Critical** |
| Account Management | COACTVWC, COACTUPC, CBACT01C, CBACT04C | **Critical** |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | **High** |
| Security / Authentication | COSGN00C, COUSR00C-03C | **Critical** |
| Billing & Payments | COBIL00C | **High** |
| Reporting | CORPT00C, CBTRN03C, CBSTM03A/B | **Medium** |
| Data Migration | CBEXPORT, CBIMPORT | **Medium** |
| Authorization (optional) | COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C | **High** |
| Config Admin (optional) | COTRTLIC, COTRTUPC, COBTUPDT | **Medium** |
| MQ Integration (optional) | COACCT01, CODATE01 | **Medium** |
| Navigation | COMEN01C, COADM01C | **Low** |
| Utilities | CSUTLDTC, COBSWAIT | **Low** |

### By Technology Stack

| Technology | Programs |
|------------|----------|
| COBOL + CICS + VSAM | 17 core online programs |
| COBOL Batch + VSAM | 14 core batch programs |
| COBOL + CICS + IMS + DB2 + MQ | 8 auth module programs |
| COBOL + CICS + DB2 | 3 tran-type module programs |
| COBOL + CICS + MQ | 2 VSAM-MQ module programs |
| Assembler | 2 utility programs |

### Total Lines of Code

| Category | LOC |
|----------|-----|
| Core COBOL Programs (31) | 20,650 |
| Optional Module Programs (13) | ~9,145 |
| Copybooks (30 core) | 2,786 |
| BMS Maps (17 core) | 4,472 |
| **Total** | **~37,053** |
