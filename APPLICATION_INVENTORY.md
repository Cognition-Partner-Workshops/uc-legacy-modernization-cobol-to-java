# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Scope:** Full CardDemo COBOL/CICS/VSAM/JCL mainframe application

---

## Executive Summary

CardDemo is a mainframe credit card management application built with **COBOL/CICS/VSAM/JCL**. It supports account management, card operations, transaction processing, bill payments, reporting, and administrative user management. The codebase contains **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, plus **13 programs in 3 optional extension modules** (IMS/DB2/MQ).

---

## 1. COBOL Programs (31 Core + 13 Optional)

### 1.1 Online CICS Programs (18 programs)

These run under CICS and drive the 3270 terminal UI.

| Program | Lines | Description | CICS Transaction | Functional Area |
|---------|-------|-------------|-----------------|-----------------|
| COSGN00C | 260 | User sign-on / authentication | CC00 | Security |
| COMEN01C | 308 | Main menu navigation | CM00 | Navigation |
| COACTVWC | 941 | Account view (read-only) | CA00 | Account Mgmt |
| COACTUPC | 4,236 | Account update (full CRUD) | CA01 | Account Mgmt |
| COCRDLIC | 1,459 | Card list with browse/paging | CC01 | Card Mgmt |
| COCRDSLC | 887 | Card detail view | CC02 | Card Mgmt |
| COCRDUPC | 1,560 | Card update | CC03 | Card Mgmt |
| COTRN00C | 699 | Transaction list with browse | CT00 | Transactions |
| COTRN01C | 330 | Transaction detail view | CT01 | Transactions |
| COTRN02C | 783 | Transaction add (new entry) | CT02 | Transactions |
| CORPT00C | 649 | Transaction report request | CR00 | Reporting |
| COBIL00C | 572 | Bill payment processing | CB00 | Billing |
| COADM01C | 288 | Admin menu | CA90 | Admin |
| COUSR00C | 695 | User list with browse | CU00 | Admin - User Mgmt |
| COUSR01C | 299 | Add new user | CU01 | Admin - User Mgmt |
| COUSR02C | 414 | Update existing user | CU02 | Admin - User Mgmt |
| COUSR03C | 359 | Delete user | CU03 | Admin - User Mgmt |
| CSUTLDTC | 157 | Date utility (CEEDAYS wrapper) | — (called) | Shared Utility |

### 1.2 Batch Programs (13 programs)

These execute via JCL and process VSAM/sequential files.

| Program | Lines | Description | Functional Area |
|---------|-------|-------------|-----------------|
| CBACT01C | 430 | Read account file, write output variants (PS, array, VB) | Account Mgmt |
| CBACT02C | 178 | Read and print card data file | Card Mgmt |
| CBACT03C | 178 | Read and print cross-reference data | Card Mgmt |
| CBACT04C | 652 | Interest calculation on accounts | Financial Processing |
| CBCUS01C | 178 | Read and print customer data file | Customer Mgmt |
| CBTRN01C | 494 | Read and print transaction file | Transactions |
| CBTRN02C | 731 | Transaction posting (daily → master) | Transactions |
| CBTRN03C | 649 | Transaction report generation | Reporting |
| CBSTM03A | 924 | Statement generation (main driver) | Statements |
| CBSTM03B | 230 | Statement I/O subroutine (called by CBSTM03A) | Statements |
| CBEXPORT | 582 | Export all VSAM files to single sequential file | Data Migration |
| CBIMPORT | 487 | Import sequential export file back into VSAM files | Data Migration |
| COBSWAIT | 41 | Wait utility (calls assembler MVSWAIT) | Utility |

### 1.3 Optional Module — Authorization (IMS/DB2/MQ) (8 programs)

Located in `app/app-authorization-ims-db2-mq/cbl/`.

| Program | Lines | Description | Technology |
|---------|-------|-------------|------------|
| COPAUA0C | 1,026 | Card authorization decision (MQ trigger program) | CICS + MQ + IMS |
| COPAUS0C | 1,032 | Authorization summary view (online) | CICS + IMS |
| COPAUS1C | 604 | Authorization detail view (online) | CICS + IMS |
| COPAUS2C | 244 | Mark authorization as fraud (online) | CICS + DB2 |
| CBPAUP0C | 386 | Batch purge of expired authorizations | IMS (batch) |
| DBUNLDGS | 366 | Unload IMS database to GSAM file | IMS + GSAM |
| PAUDBLOD | 369 | Load IMS authorization database | IMS (batch) |
| PAUDBUNL | 317 | Unload IMS authorization database | IMS (batch) |

### 1.4 Optional Module — Transaction Type DB2 (3 programs)

Located in `app/app-transaction-type-db2/cbl/`.

| Program | Lines | Description | Technology |
|---------|-------|-------------|------------|
| COTRTLIC | 2,098 | Transaction type list/delete (online) | CICS + DB2 |
| COTRTUPC | 1,702 | Transaction type add/update (online) | CICS + DB2 |
| COBTUPDT | 237 | Batch update of transaction types | DB2 (batch) |

### 1.5 Optional Module — VSAM-MQ (2 programs)

Located in `app/app-vsam-mq/cbl/`.

| Program | Lines | Description | Technology |
|---------|-------|-------------|------------|
| COACCT01 | 620 | MQ request/response for account inquiry | CICS + MQ + VSAM |
| CODATE01 | 524 | MQ request/response for system date | CICS + MQ |

---

## 2. Copybooks (30 Core + 11 Optional)

### 2.1 VSAM Record Layouts (CV* prefix)

| Copybook | Lines | Record | Record Length | Business Entity |
|----------|-------|--------|--------------|-----------------|
| CVACT01Y | 20 | ACCT-RECORD | 300 bytes | Account Master |
| CVACT02Y | 14 | CARD-RECORD | 150 bytes | Card Master |
| CVACT03Y | 11 | CARD-XREF-RECORD | 50 bytes | Card Cross-Reference |
| CVCRD01Y | 46 | FD-CRDDAT-REC | — | Card Detail (extended) |
| CVCUS01Y | 26 | CUSTOMER-RECORD | 500 bytes | Customer Master |
| CVTRA01Y | 13 | TRAN-CAT-BAL-RECORD | 50 bytes | Transaction Category Balance |
| CVTRA02Y | 13 | DIS-GROUP-RECORD | 50 bytes | Disclosure Group |
| CVTRA03Y | 10 | TRAN-TYPE-RECORD | 60 bytes | Transaction Type |
| CVTRA04Y | 12 | TRAN-CAT-RECORD | 60 bytes | Transaction Category |
| CVTRA05Y | 21 | TRAN-RECORD | 350 bytes | Transaction Master |
| CVTRA06Y | 21 | DALYTRAN-RECORD | 350 bytes | Daily Transaction |
| CVTRA07Y | 73 | Report data structures | — | Transaction Report Layout |
| CVEXPORT | 103 | EXPORT-RECORD | varies | Data Export Record |
| CUSTREC | 26 | CUSTOMER-RECORD (alt) | — | Customer Record (statement use) |
| COSTM01 | 38 | TRNX-RECORD | — | Transaction (keyed by card+ID) |

### 2.2 Common/Shared Copybooks (CO*/CS* prefix)

| Copybook | Lines | Description |
|----------|-------|-------------|
| COCOM01Y | 47 | Common communication area (COMMAREA) — passed between programs |
| COADM02Y | 62 | Admin menu option definitions |
| CODATECN | 52 | Date conversion work areas |
| COMEN02Y | 101 | Main menu option definitions and routing table |
| COTTL01Y | 27 | Title/header line layout for screens |
| CSDAT01Y | 58 | Date/time formatting work areas |
| CSLKPCDY | 1,318 | Lookup code tables (country, state, card type codes) |
| CSMSG01Y | 24 | Standard message area |
| CSMSG02Y | 35 | Extended message area |
| CSSETATY | 30 | Screen field attribute setting |
| CSSTRPFY | 85 | String parse/strip function utility |
| CSUSR01Y | 26 | User security record layout |
| CSUTLDPY | 375 | Date utility parameter block (CEEDAYS interface) |
| CSUTLDWY | 89 | Date utility working storage |
| UNUSED1Y | 10 | Unused placeholder record |

### 2.3 BMS-Generated Copybooks (17 files)

Located in `app/cpy-bms/`. These are **auto-generated** from BMS maps and define the symbolic map structures used by COBOL programs.

| Copybook | Corresponding BMS Map | Screen |
|----------|----------------------|--------|
| COACTUP.CPY | COACTUP.bms | Account Update |
| COACTVW.CPY | COACTVW.bms | Account View |
| COADM01.CPY | COADM01.bms | Admin Menu |
| COBIL00.CPY | COBIL00.bms | Bill Payment |
| COCRDLI.CPY | COCRDLI.bms | Card List |
| COCRDSL.CPY | COCRDSL.bms | Card Detail View |
| COCRDUP.CPY | COCRDUP.bms | Card Update |
| COMEN01.CPY | COMEN01.bms | Main Menu |
| CORPT00.CPY | CORPT00.bms | Report Request |
| COSGN00.CPY | COSGN00.bms | Sign-on |
| COTRN00.CPY | COTRN00.bms | Transaction List |
| COTRN01.CPY | COTRN01.bms | Transaction View |
| COTRN02.CPY | COTRN02.bms | Transaction Add |
| COUSR00.CPY | COUSR00.bms | User List |
| COUSR01.CPY | COUSR01.bms | User Add |
| COUSR02.CPY | COUSR02.bms | User Update |
| COUSR03.CPY | COUSR03.bms | User Delete |

### 2.4 Optional Module Copybooks

**Authorization (IMS/DB2/MQ) — 9 files** in `app/app-authorization-ims-db2-mq/cpy/`:
`CCPAUERY`, `CCPAURLY`, `CCPAURQY`, `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, `PADFLPCB.CPY`, `PASFLPCB.CPY`, `PAUTBPCB.CPY`

**Transaction Type DB2 — 2 files** in `app/app-transaction-type-db2/cpy/`:
`CSDB2RPY`, `CSDB2RWY`

---

## 3. BMS Screen Maps (17 Core + 4 Optional)

### 3.1 Core BMS Maps

| BMS Map | Map Set | Map Name | Screen Title | Associated Program |
|---------|---------|----------|-------------|-------------------|
| COSGN00.bms | COSGN00 | COSGN0A | Login Screen | COSGN00C |
| COMEN01.bms | COMEN01 | COMEN1A | Main Menu | COMEN01C |
| COACTVW.bms | COACTVW | CACTVWA | Account View | COACTVWC |
| COACTUP.bms | COACTUP | CACTUPA | Account Update | COACTUPC |
| COCRDLI.bms | COCRDLI | CCRDLIA | Card List | COCRDLIC |
| COCRDSL.bms | COCRDSL | CCRDSLA | Card Detail View | COCRDSLC |
| COCRDUP.bms | COCRDUP | CCRDUPA | Card Update | COCRDUPC |
| COTRN00.bms | COTRN00 | COTRN0A | Transaction List | COTRN00C |
| COTRN01.bms | COTRN01 | COTRN1A | Transaction View | COTRN01C |
| COTRN02.bms | COTRN02 | COTRN2A | Transaction Add | COTRN02C |
| CORPT00.bms | CORPT00 | CORPT0A | Report Request | CORPT00C |
| COBIL00.bms | COBIL00 | COBIL0A | Bill Payment | COBIL00C |
| COADM01.bms | COADM01 | COADM1A | Admin Menu | COADM01C |
| COUSR00.bms | COUSR00 | COUSR0A | User List | COUSR00C |
| COUSR01.bms | COUSR01 | COUSR1A | Add User | COUSR01C |
| COUSR02.bms | COUSR02 | COUSR2A | Update User | COUSR02C |
| COUSR03.bms | COUSR03 | COUSR3A | Delete User | COUSR03C |

### 3.2 Optional Module BMS Maps

| BMS Map | Module | Screen Title |
|---------|--------|-------------|
| COPAU00.bms | Authorization | Auth Summary View |
| COPAU01.bms | Authorization | Auth Detail View |
| COTRTLI.bms | Transaction Type DB2 | Tran Type List |
| COTRTUP.bms | Transaction Type DB2 | Tran Type Update |

---

## 4. JCL Batch Jobs (38 Core + 8 Optional)

### 4.1 Data Initialization Jobs

| JCL Job | Description | Programs/Utilities Invoked |
|---------|-------------|---------------------------|
| DUSRSECJ | Load user security VSAM from inline data | IEFBR14, IEBGENER, IDCAMS |
| ACCTFILE | Refresh account master VSAM | IDCAMS |
| CARDFILE | Refresh card master VSAM | IDCAMS |
| CUSTFILE | Refresh customer master VSAM | IDCAMS |
| XREFFILE | Load card cross-reference VSAM + alt indexes | IDCAMS |
| TRANFILE | Load transaction master VSAM + alt indexes | IDCAMS, SDSF |
| ESDSRRDS | Load ESDS and RRDS demo datasets | IEFBR14, IEBGENER, IDCAMS |
| DEFGDGB | Define GDG bases for reporting | IDCAMS, IEBGENER |
| DEFGDGD | Define GDG bases + load tran type/cat/disc data | IDCAMS, IEBGENER |
| TCATBALF | Load transaction category balance VSAM | IDCAMS |
| TRANCATG | Load transaction category VSAM | IDCAMS |
| TRANTYPE | Load transaction type VSAM | IDCAMS |
| DISCGRP | Load disclosure group VSAM | IDCAMS |

### 4.2 Batch Processing Jobs

| JCL Job | Description | COBOL Program |
|---------|-------------|---------------|
| POSTTRAN | Post daily transactions to master | CBTRN02C |
| INTCALC | Calculate interest on accounts | CBACT04C |
| CREASTMT | Generate account statements | SORT + IDCAMS + CBSTM03A |
| TRANREPT | Generate transaction reports | REPROC + SORT + CBTRN03C |
| COMBTRAN | Combine transaction files | (SORT/IDCAMS) |
| TRANBKP | Backup transaction VSAM to GDG | REPROC + IDCAMS |

### 4.3 File Management Jobs

| JCL Job | Description | Programs/Utilities |
|---------|-------------|-------------------|
| CLOSEFIL | Close CICS files for batch processing | SDSF |
| OPENFIL | Open CICS files after batch | SDSF |
| TRANIDX | Define/rebuild transaction alternate indexes | IDCAMS |

### 4.4 Report/Print Jobs

| JCL Job | Description | Programs/Utilities |
|---------|-------------|-------------------|
| READACCT | Read/print account data | CBACT01C |
| READCARD | Read/print card data | CBACT02C |
| READCUST | Read/print customer data | CBCUS01C |
| READXREF | Read/print cross-reference data | CBACT03C |
| DALYREJS | Daily rejection report | (SORT/utility) |
| PRTCATBL | Print category balance report | REPROC + SORT |
| REPTFILE | Report file utility | IDCAMS |
| TXT2PDF1 | Convert text report to PDF | IKJEFT1B (TXT2PDF REXX) |

### 4.5 Data Migration Jobs

| JCL Job | Description | COBOL Program |
|---------|-------------|---------------|
| CBEXPORT | Export all VSAM files | CBEXPORT |
| CBIMPORT | Import from export file | CBIMPORT |

### 4.6 Infrastructure/Utility Jobs

| JCL Job | Description |
|---------|-------------|
| DEFCUST | Define customer VSAM cluster |
| FTPJCL | FTP file transfer utility |
| INTRDRJ1 | Internal reader test job 1 |
| INTRDRJ2 | Internal reader test job 2 |
| WAITSTEP | Wait step (calls COBSWAIT) |
| CBADMCDJ | Admin card demo utility |

### 4.7 Optional Module JCL Jobs

**Authorization (IMS/DB2/MQ) — 5 jobs:**
`CBPAUP0J` (purge expired auths), `DBPAUTP0` (IMS DB utility), `LOADPADB.JCL` (load auth DB), `UNLDGSAM.JCL` (unload GSAM), `UNLDPADB.JCL` (unload auth DB)

**Transaction Type DB2 — 3 jobs:**
`CREADB21` (create DB2 tables), `MNTTRDB2` (maintain tran types), `TRANEXTR` (extract tran types)

---

## 5. Assembler Programs & Macros

| File | Location | Description |
|------|----------|-------------|
| MVSWAIT.asm | app/asm/ | Assembler wait routine (called by COBSWAIT) |
| COBDATFT.asm | app/asm/ | Assembler date formatting routine |
| ASMWAIT.mac | app/maclib/ | Wait macro definition |
| COCDATFT.mac | app/maclib/ | Date formatting macro definition |

---

## 6. Procedures, Control Files & Scheduler

| File | Location | Description |
|------|----------|-------------|
| REPROC.prc | app/proc/ | Reprocessing procedure (IDCAMS REPRO) |
| TRANREPT.prc | app/proc/ | Transaction report procedure |
| REPROCT.ctl | app/ctl/ | REPRO control parameters |
| CardDemo.ca7 | app/scheduler/ | CA7 scheduler definitions |
| CardDemo.controlm | app/scheduler/ | Control-M scheduler definitions |

---

## 7. Classification Summary

| Category | Core | Optional | Total |
|----------|------|----------|-------|
| Online COBOL Programs | 18 | 7 | 25 |
| Batch COBOL Programs | 13 | 6 | 19 |
| Copybooks (data) | 30 | 11 | 41 |
| BMS-Generated Copybooks | 17 | 4 | 21 |
| BMS Screen Maps | 17 | 4 | 21 |
| JCL Batch Jobs | 38 | 8 | 46 |
| Assembler Programs | 2 | 0 | 2 |
| Macros | 2 | 0 | 2 |
| Procedures | 2 | 0 | 2 |
| **Total Artifacts** | **139** | **40** | **179** |

---

## 8. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| CO* | Online CICS program | COSGN00C (sign-on) |
| CB* | Batch COBOL program | CBTRN02C (tran posting) |
| CS* | Common/shared utility or copybook | CSUTLDTC (date utility) |
| CV* | VSAM record layout copybook | CVACT01Y (account record) |
| *Y | Copybook suffix | CSUSR01Y |
| *C | COBOL program suffix | COSGN00C |
