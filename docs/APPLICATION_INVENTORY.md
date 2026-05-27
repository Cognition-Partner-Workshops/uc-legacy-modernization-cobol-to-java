# Application Inventory — CardDemo Mainframe System

> **Generated**: 2026-05-27 | **Source**: `uc-legacy-modernization-cobol-to-java`

## Summary

| Artifact Type | Count |
|:---|---:|
| COBOL Programs (Core) | 31 |
| COBOL Programs (Optional Modules) | 13 |
| Copybooks (Core) | 30 |
| Copybooks (Optional Modules) | 15 |
| BMS Maps (Core) | 17 |
| BMS Maps (Optional Modules) | 4 |
| JCL Jobs (Core) | 38 |
| JCL Jobs (Optional Modules) | 8 |
| Assembler Modules | 2 |
| Procedures | 2 |
| Scheduler Definitions | 2 |
| **Total Artifacts** | **162** |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online (CICS) Programs

| # | Program | LOC | Transaction | Function | Classification |
|---|:--------|----:|:------------|:---------|:---------------|
| 1 | COSGN00C | 260 | CC00 | Sign-on / Authentication | Security |
| 2 | COMEN01C | 308 | CM00 | Main Menu Navigation | Navigation |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation |
| 4 | COACTVWC | 941 | CAVW | Account View | Account Mgmt |
| 5 | COACTUPC | 4,236 | CAUP | Account Update | Account Mgmt |
| 6 | COCRDLIC | 1,459 | CCLI | Credit Card List | Card Mgmt |
| 7 | COCRDSLC | 887 | CCDL | Credit Card Detail View | Card Mgmt |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transaction Mgmt |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transaction Mgmt |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transaction Mgmt |
| 12 | CORPT00C | 649 | CR00 | Transaction Report Request | Reporting |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing |
| 14 | COUSR00C | 695 | CU00 | List Users | User Admin |
| 15 | COUSR01C | 299 | CU01 | Add User | User Admin |
| 16 | COUSR02C | 414 | CU02 | Update User | User Admin |
| 17 | COUSR03C | 359 | CU03 | Delete User | User Admin |

### 1.2 Batch Programs

| # | Program | LOC | Job(s) | Function | Classification |
|---|:--------|----:|:-------|:---------|:---------------|
| 18 | CBACT01C | 430 | READACCT | Account File Processing & Reformatting | Data Processing |
| 19 | CBACT02C | 178 | READCARD | Card File Reader | Data Processing |
| 20 | CBACT03C | 178 | READXREF | Cross-Reference File Reader | Data Processing |
| 21 | CBACT04C | 652 | INTCALC | Interest Rate Calculation | Financial Calc |
| 22 | CBCUS01C | 178 | READCUST | Customer File Reader | Data Processing |
| 23 | CBTRN01C | 494 | — | Transaction File Reader | Data Processing |
| 24 | CBTRN02C | 731 | POSTTRAN | Daily Transaction Posting | Transaction Mgmt |
| 25 | CBTRN03C | 649 | TRANREPT | Transaction Report Generator | Reporting |
| 26 | CBSTM03A | 924 | CREASTMT | Statement Generation (Main) | Reporting |
| 27 | CBSTM03B | 230 | CREASTMT | Statement Generation (Sub-module) | Reporting |
| 28 | CBEXPORT | 582 | CBEXPORT | Branch Data Export | Data Migration |
| 29 | CBIMPORT | 487 | CBIMPORT | Branch Data Import | Data Migration |
| 30 | COBSWAIT | 41 | WAITSTEP | Timer Wait (calls MVSWAIT) | Utility |

### 1.3 Utility Programs

| # | Program | LOC | Function | Classification |
|---|:--------|----:|:---------|:---------------|
| 31 | CSUTLDTC | 157 | Date Validation (calls CEEDAYS) | Utility |

---

## 2. COBOL Programs — Optional Modules

### 2.1 IMS-DB2-MQ: Pending Authorizations (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | LOC | Transaction/Job | Function | Classification |
|---|:--------|----:|:----------------|:---------|:---------------|
| 32 | COPAUS0C | 1,032 | CPVS | Pending Authorization Summary | Authorization |
| 33 | COPAUS1C | 604 | CPVD | Pending Authorization Details | Authorization |
| 34 | COPAUS2C | 244 | — | Authorization Helper | Authorization |
| 35 | COPAUA0C | 1,026 | CP00 | Process Authorization Requests (MQ) | Authorization |
| 36 | CBPAUP0C | 386 | CBPAUP0J | Purge Expired Authorizations | Authorization |
| 37 | PAUDBLOD | 369 | LOADPADB | Load Pending Auth DB2 Table | Data Loading |
| 38 | PAUDBUNL | 317 | UNLDPADB | Unload Pending Auth DB2 Table | Data Unload |
| 39 | DBUNLDGS | 366 | UNLDGSAM | Unload GSAM to Sequential | Data Unload |

### 2.2 DB2: Transaction Type Management (`app/app-transaction-type-db2/cbl/`)

| # | Program | LOC | Transaction/Job | Function | Classification |
|---|:--------|----:|:----------------|:---------|:---------------|
| 40 | COTRTLIC | 2,098 | CTLI | Transaction Type List/Delete (DB2 cursor) | Reference Data |
| 41 | COTRTUPC | 1,702 | CTTU | Transaction Type Add/Edit (DB2) | Reference Data |
| 42 | COBTUPDT | 237 | MNTTRDB2 | Batch Transaction Type Maintenance | Reference Data |

### 2.3 MQ Integration: VSAM-MQ (`app/app-vsam-mq/cbl/`)

| # | Program | LOC | Transaction | Function | Classification |
|---|:--------|----:|:------------|:---------|:---------------|
| 43 | CODATE01 | 524 | CDRD | System Date Inquiry via MQ | Integration |
| 44 | COACCT01 | 620 | CDRA | Account Details Inquiry via MQ | Integration |

---

## 3. Copybooks — Core (`app/cpy/`)

| # | Copybook | Record Len | Entity / Purpose | Domain |
|---|:---------|:-----------|:-----------------|:-------|
| 1 | CVACT01Y | 300 | Account Master Record | Account |
| 2 | CVACT02Y | 150 | Card Record | Card |
| 3 | CVACT03Y | 50 | Card-Customer-Account Cross-Reference | Cross-Reference |
| 4 | CVCUS01Y | 500 | Customer Master Record | Customer |
| 5 | CVCRD01Y | — | Credit Card Work Areas (online) | Card |
| 6 | CVTRA01Y | 50 | Transaction Category Balance | Transaction |
| 7 | CVTRA02Y | 50 | Disclosure Group | Billing/Rates |
| 8 | CVTRA03Y | 60 | Transaction Type | Transaction |
| 9 | CVTRA04Y | 60 | Transaction Category Type | Transaction |
| 10 | CVTRA05Y | 350 | Transaction Record (online VSAM) | Transaction |
| 11 | CVTRA06Y | 350 | Daily Transaction Record (batch) | Transaction |
| 12 | CVTRA07Y | — | Transaction Report Headers/Detail | Reporting |
| 13 | CSUSR01Y | 80 | User Security Record | Security |
| 14 | COCOM01Y | — | CICS Communication Area (Commarea) | Infrastructure |
| 15 | CSDAT01Y | — | System Date Fields | Infrastructure |
| 16 | CSMSG01Y | — | Message Area (informational) | Infrastructure |
| 17 | CSMSG02Y | — | Message Area (error) | Infrastructure |
| 18 | COTTL01Y | — | Title/Header Lines | UI/Reports |
| 19 | CSSETATY | — | Screen Attribute Settings | UI |
| 20 | CSSTRPFY | — | String Processing Functions | Utility |
| 21 | COADM02Y | — | Admin Menu Options Table | Navigation |
| 22 | COMEN02Y | — | Main Menu Options Table | Navigation |
| 23 | CODATECN | — | Date Conversion Parameters (COBDATFT) | Utility |
| 24 | CSUTLDPY | — | Date Utility Parameters | Utility |
| 25 | CSUTLDWY | — | Date Utility Work Areas | Utility |
| 26 | CSLKPCDY | — | Lookup Code Table | Reference |
| 27 | COSTM01 | — | Statement Generation Work Areas | Reporting |
| 28 | CVEXPORT | 500 | Branch Migration Export Record (multi-type) | Migration |
| 29 | CUSTREC | — | Customer Record (alternate layout) | Customer |
| 30 | UNUSED1Y | — | Unused/Placeholder | Legacy |

### 3.1 BMS-Generated Copybooks (`app/cpy-bms/`)

| # | Copybook | Associated Map | Purpose |
|---|:---------|:---------------|:--------|
| 1 | COACTUP.CPY | COACTUP | Account Update screen fields |
| 2 | COACTVW.CPY | COACTVW | Account View screen fields |
| 3 | COADM01.CPY | COADM01 | Admin Menu screen fields |
| 4 | COBIL00.CPY | COBIL00 | Bill Payment screen fields |
| 5 | COCRDLI.CPY | COCRDLI | Card List screen fields |
| 6 | COCRDSL.CPY | COCRDSL | Card Detail screen fields |
| 7 | COCRDUP.CPY | COCRDUP | Card Update screen fields |
| 8 | COMEN01.CPY | COMEN01 | Main Menu screen fields |
| 9 | CORPT00.CPY | CORPT00 | Report screen fields |
| 10 | COSGN00.CPY | COSGN00 | Sign-on screen fields |
| 11 | COTRN00.CPY | COTRN00 | Transaction List screen fields |
| 12 | COTRN01.CPY | COTRN01 | Transaction View screen fields |
| 13 | COTRN02.CPY | COTRN02 | Transaction Add screen fields |
| 14 | COUSR00.CPY | COUSR00 | User List screen fields |
| 15 | COUSR01.CPY | COUSR01 | User Add screen fields |
| 16 | COUSR02.CPY | COUSR02 | User Update screen fields |
| 17 | COUSR03.CPY | COUSR03 | User Delete screen fields |

---

## 4. Copybooks — Optional Modules

### 4.1 IMS-DB2-MQ Copybooks (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Purpose |
|---|:---------|:--------|
| 1 | CCPAUERY | Pending Auth Inquiry Request |
| 2 | CCPAURLY | Pending Auth Response Layout |
| 3 | CCPAURQY | Pending Auth MQ Request |
| 4 | CIPAUDTY | Pending Auth Detail Structure |
| 5 | CIPAUSMY | Pending Auth Summary Structure |
| 6 | IMSFUNCS | IMS Function Codes |
| 7 | PADFLPCB | IMS PCB (Detail Segment) |
| 8 | PASFLPCB | IMS PCB (Summary Segment) |
| 9 | PAUTBPCB | IMS PCB (Auth Table) |

### 4.2 IMS-DB2-MQ BMS Copybooks (`app/app-authorization-ims-db2-mq/cpy-bms/`)

| # | Copybook | Purpose |
|---|:---------|:--------|
| 10 | COPAU00.cpy | Auth Summary screen fields |
| 11 | COPAU01.cpy | Auth Detail screen fields |

### 4.3 DB2 Transaction Type Copybooks (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Purpose |
|---|:---------|:--------|
| 12 | CSDB2RPY | DB2 Result Parameters |
| 13 | CSDB2RWY | DB2 Return/Reason Work Areas |

### 4.4 DB2 Transaction Type BMS Copybooks (`app/app-transaction-type-db2/cpy-bms/`)

| # | Copybook | Purpose |
|---|:---------|:--------|
| 14 | COTRTLI.cpy | Tran Type List screen fields |
| 15 | COTRTUP.cpy | Tran Type Update screen fields |

---

## 5. BMS Maps — Core (`app/bms/`)

| # | Map | Mapset | Associated Program | Screen Function |
|---|:----|:-------|:-------------------|:----------------|
| 1 | COSGN00 | COSGN00 | COSGN00C | Sign-on |
| 2 | COMEN01 | COMEN01 | COMEN01C | Main Menu |
| 3 | COADM01 | COADM01 | COADM01C | Admin Menu |
| 4 | COACTVW | COACTVW | COACTVWC | Account View |
| 5 | COACTUP | COACTUP | COACTUPC | Account Update |
| 6 | COCRDLI | COCRDLI | COCRDLIC | Card List |
| 7 | COCRDSL | COCRDSL | COCRDSLC | Card Detail |
| 8 | COCRDUP | COCRDUP | COCRDUPC | Card Update |
| 9 | COTRN00 | COTRN00 | COTRN00C | Transaction List |
| 10 | COTRN01 | COTRN01 | COTRN01C | Transaction View |
| 11 | COTRN02 | COTRN02 | COTRN02C | Transaction Add |
| 12 | CORPT00 | CORPT00 | CORPT00C | Report Parameters |
| 13 | COBIL00 | COBIL00 | COBIL00C | Bill Payment |
| 14 | COUSR00 | COUSR00 | COUSR00C | User List |
| 15 | COUSR01 | COUSR01 | COUSR01C | User Add |
| 16 | COUSR02 | COUSR02 | COUSR02C | User Update |
| 17 | COUSR03 | COUSR03 | COUSR03C | User Delete |

### 5.1 BMS Maps — Optional Modules

| # | Map | Module | Associated Program | Screen Function |
|---|:----|:-------|:-------------------|:----------------|
| 18 | COPAU00 | IMS-DB2-MQ | COPAUS0C | Pending Auth Summary |
| 19 | COPAU01 | IMS-DB2-MQ | COPAUS1C | Pending Auth Detail |
| 20 | COTRTLI | DB2 Tran Type | COTRTLIC | Tran Type List |
| 21 | COTRTUP | DB2 Tran Type | COTRTUPC | Tran Type Update |

---

## 6. JCL Jobs — Core (`app/jcl/`)

| # | Job | Primary Program | Function | Category |
|---|:----|:----------------|:---------|:---------|
| 1 | ACCTFILE | IDCAMS | Load/Refresh Account VSAM file | Data Init |
| 2 | CARDFILE | IDCAMS | Load/Refresh Card VSAM file | Data Init |
| 3 | CUSTFILE | IDCAMS | Load/Refresh Customer VSAM file | Data Init |
| 4 | XREFFILE | IDCAMS | Load Cross-Reference VSAM | Data Init |
| 5 | TRANFILE | IDCAMS | Load Transaction Master VSAM | Data Init |
| 6 | TRANCATG | IDCAMS | Load Transaction Category Types | Data Init |
| 7 | TRANTYPE | IDCAMS | Load Transaction Types | Data Init |
| 8 | DISCGRP | IDCAMS | Load Disclosure Groups | Data Init |
| 9 | TCATBALF | IDCAMS | Load Tran Category Balance | Data Init |
| 10 | DUSRSECJ | IEBGENER | Load User Security File | Data Init |
| 11 | DEFGDGB | IDCAMS | Define GDG Bases | Data Init |
| 12 | DEFGDGD | IDCAMS | Define GDG Bases (DB2 ext) | Data Init |
| 13 | DEFCUST | IDCAMS | Define Customer VSAM Cluster | Data Init |
| 14 | CLOSEFIL | IEFBR14 | Close VSAM Files in CICS | Lifecycle |
| 15 | OPENFIL | IEFBR14 | Open VSAM Files in CICS | Lifecycle |
| 16 | POSTTRAN | CBTRN02C | Post Daily Transactions | Core Batch |
| 17 | INTCALC | CBACT04C | Interest Rate Calculation | Core Batch |
| 18 | COMBTRAN | SORT | Combine Transaction Files | Core Batch |
| 19 | CREASTMT | CBSTM03A | Generate Statements | Core Batch |
| 20 | TRANREPT | CBTRN03C | Transaction Report | Reporting |
| 21 | TRANBKP | IDCAMS | Backup Transaction Master | Backup |
| 22 | TRANIDX | IDCAMS | Define Alternate Index on Trans | Indexing |
| 23 | DALYREJS | — | Daily Rejects Processing | Error Handling |
| 24 | ESDSRRDS | IDCAMS | Create ESDS/RRDS VSAM Files | Data Init |
| 25 | WAITSTEP | COBSWAIT | Timer Wait Step | Utility |
| 26 | PRTCATBL | — | Print Category Balance | Reporting |
| 27 | REPTFILE | — | Report File Utility | Reporting |
| 28 | READACCT | CBACT01C | Read/Reformat Account File | Data Processing |
| 29 | READCARD | CBACT02C | Read Card File | Data Processing |
| 30 | READCUST | CBCUS01C | Read Customer File | Data Processing |
| 31 | READXREF | CBACT03C | Read Cross-Reference File | Data Processing |
| 32 | CBEXPORT | CBEXPORT | Branch Data Export | Migration |
| 33 | CBIMPORT | CBIMPORT | Branch Data Import | Migration |
| 34 | CBADMCDJ | — | Admin Card Job | Admin |
| 35 | FTPJCL | — | FTP Transfer | Utility |
| 36 | INTRDRJ1 | — | Internal Reader Job 1 | Utility |
| 37 | INTRDRJ2 | — | Internal Reader Job 2 | Utility |
| 38 | TXT2PDF1 | — | Text to PDF Conversion | Utility |

### 6.1 JCL Jobs — Optional Modules

| # | Job | Module | Primary Program | Function |
|---|:----|:-------|:----------------|:---------|
| 39 | CBPAUP0J | IMS-DB2-MQ | CBPAUP0C | Purge Expired Authorizations |
| 40 | DBPAUTP0 | IMS-DB2-MQ | — | Auth DB2 Table Processing |
| 41 | LOADPADB | IMS-DB2-MQ | PAUDBLOD | Load Pending Auth DB2 |
| 42 | UNLDPADB | IMS-DB2-MQ | PAUDBUNL | Unload Pending Auth DB2 |
| 43 | UNLDGSAM | IMS-DB2-MQ | DBUNLDGS | Unload GSAM to Sequential |
| 44 | CREADB21 | DB2 Tran Type | DSNTEP4 | Create DB2 Database/Tables |
| 45 | TRANEXTR | DB2 Tran Type | DSNTIAUL | Extract DB2 Tran Type Data |
| 46 | MNTTRDB2 | DB2 Tran Type | COBTUPDT | Maintain Tran Type Table |

---

## 7. Assembler Modules (`app/asm/`)

| # | Module | Function |
|---|:-------|:---------|
| 1 | MVSWAIT | Timer control — provides timed wait for batch jobs |
| 2 | COBDATFT | Date format conversion — converts date formats for COBOL programs |

---

## 8. Procedures (`app/proc/`)

| # | Procedure | Function |
|---|:----------|:---------|
| 1 | REPROC.prc | Report generation cataloged procedure |
| 2 | TRANREPT.prc | Transaction report cataloged procedure |

---

## 9. Scheduler Definitions (`app/scheduler/`)

| # | Definition | Platform | Function |
|---|:-----------|:---------|:---------|
| 1 | CardDemo.ca7 | CA-7 | Batch scheduling definitions |
| 2 | CardDemo.controlm | Control-M | Batch workflow orchestration |

---

## 10. Classification Summary

| Classification | Online | Batch | Total |
|:---------------|-------:|------:|------:|
| Account Management | 2 | 2 | 4 |
| Card Management | 3 | 1 | 4 |
| Transaction Management | 3 | 3 | 6 |
| Reporting | 1 | 3 | 4 |
| Billing | 1 | 1 | 2 |
| User Administration | 4 | 0 | 4 |
| Security/Sign-on | 1 | 0 | 1 |
| Navigation | 2 | 0 | 2 |
| Authorization (Optional) | 3 | 4 | 7 |
| Reference Data (Optional) | 0 | 3 | 3 |
| Integration (Optional) | 2 | 0 | 2 |
| Data Migration | 0 | 2 | 2 |
| Utility | 0 | 3 | 3 |
| **Total** | **22** | **22** | **44** |
