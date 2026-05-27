# CardDemo Application Inventory

> **System**: CardDemo — Mainframe Credit Card Management System
> **Platform**: CICS / Batch (z/OS) · COBOL · VSAM · DB2 · IMS DB · MQ
> **Last Updated**: 2026-05-27

---

## 1. Summary Statistics

| Artifact Type | Count |
|:---|---:|
| COBOL Programs — Online (CICS) | 22 |
| COBOL Programs — Batch | 13 |
| COBOL Programs — Shared Subroutines | 3 |
| Assembler Programs | 2 |
| Copybooks — Data Layouts | 27 |
| Copybooks — BMS-generated | 19 |
| BMS Map Definitions | 17 |
| JCL Jobs | 37 |
| JCL Procedures | 2 |
| CSD Resource Definitions | 3 |
| Control-M Scheduler Definitions | 1 |
| CA-7 Scheduler Definitions | 1 |

**Total Artifacts: 147**

---

## 2. Online Programs (CICS)

### 2.1 Core Online Programs

| # | Program | Lines | Transaction | BMS Map | Function | Domain |
|--:|:--------|------:|:------------|:--------|:---------|:-------|
| 1 | COSGN00C | 260 | CC00 | COSGN00 | Sign-on / Login | Security |
| 2 | COMEN01C | 308 | CM00 | COMEN01 | Main Menu | Navigation |
| 3 | COADM01C | 288 | CA00 | COADM01 | Admin Menu | Navigation |
| 4 | COACTVWC | 941 | CAVW | COACTVW | Account View | Account Mgmt |
| 5 | COACTUPC | 4236 | CAUP | COACTUP | Account Update | Account Mgmt |
| 6 | COCRDLIC | 1459 | CCLI | COCRDLI | Credit Card List | Card Mgmt |
| 7 | COCRDSLC | 887 | CCDL | COCRDSL | Credit Card View | Card Mgmt |
| 8 | COCRDUPC | 1560 | CCUP | COCRDUP | Credit Card Update | Card Mgmt |
| 9 | COTRN00C | 699 | CT00 | COTRN00 | Transaction List | Transaction Mgmt |
| 10 | COTRN01C | 330 | CT01 | COTRN01 | Transaction View | Transaction Mgmt |
| 11 | COTRN02C | 783 | CT02 | COTRN02 | Transaction Add | Transaction Mgmt |
| 12 | CORPT00C | 649 | CR00 | CORPT00 | Transaction Reports | Reporting |
| 13 | COBIL00C | 572 | CB00 | COBIL00 | Bill Payment | Billing |
| 14 | COUSR00C | 695 | CU00 | COUSR00 | User List | User Admin |
| 15 | COUSR01C | 299 | CU01 | COUSR01 | User Add | User Admin |
| 16 | COUSR02C | 414 | CU02 | COUSR02 | User Update | User Admin |
| 17 | COUSR03C | 359 | CU03 | COUSR03 | User Delete | User Admin |

### 2.2 Optional: Pending Authorization Module (IMS-DB2-MQ)

| # | Program | Lines | Transaction | BMS Map | Function | Domain |
|--:|:--------|------:|:------------|:--------|:---------|:-------|
| 18 | COPAUS0C | 1032 | CPVS | COPAU00 | Pending Authorization Summary | Authorization |
| 19 | COPAUS1C | 604 | CPVD | COPAU01 | Pending Authorization Details | Authorization |
| 20 | COPAUS2C | 244 | — | — | Authorization Helper | Authorization |
| 21 | COPAUA0C | 1026 | CP00 | — | Process Authorization Requests (MQ) | Authorization |

### 2.3 Optional: Transaction Type Management (DB2)

| # | Program | Lines | Transaction | BMS Map | Function | Domain |
|--:|:--------|------:|:------------|:--------|:---------|:-------|
| 22 | COTRTLIC | 2098 | CTLI | COTRTLI | Transaction Type List / Update / Delete | Reference Data |
| 23 | COTRTUPC | 1702 | CTTU | COTRTUP | Transaction Type Add / Edit | Reference Data |

### 2.4 Optional: VSAM-MQ Integration

| # | Program | Lines | Transaction | BMS Map | Function | Domain |
|--:|:--------|------:|:------------|:--------|:---------|:-------|
| 24 | COACCT01 | 620 | CDRA | — | Account Details Inquiry via MQ | Account Mgmt |
| 25 | CODATE01 | 524 | CDRD | — | System Date Inquiry via MQ | Utility |

---

## 3. Batch Programs

### 3.1 Core Batch Programs

| # | Program | Lines | JCL Job(s) | Function | Domain |
|--:|:--------|------:|:-----------|:---------|:-------|
| 26 | CBACT01C | 430 | READACCT | Read accounts, write to multiple output formats (FB, VB, array) | Account Mgmt |
| 27 | CBACT02C | 178 | READCARD | Read and print card data | Card Mgmt |
| 28 | CBACT03C | 178 | READXREF | Read and print cross-reference data | Cross-Reference |
| 29 | CBACT04C | 652 | INTCALC | Interest calculation on accounts | Billing |
| 30 | CBCUS01C | 178 | READCUST | Read and print customer data | Customer Mgmt |
| 31 | CBTRN01C | 494 | — | Transaction validation (pre-posting) | Transaction Mgmt |
| 32 | CBTRN02C | 731 | POSTTRAN | Transaction posting — daily batch | Transaction Mgmt |
| 33 | CBTRN03C | 649 | TRANREPT | Transaction report generation | Reporting |
| 34 | CBSTM03A | 924 | CREASTMT | Statement generation — main program | Billing |
| 35 | CBSTM03B | 230 | — | Statement generation — file I/O subroutine | Billing |
| 36 | CBEXPORT | 582 | CBEXPORT | Export data for branch migration | Data Migration |
| 37 | CBIMPORT | 487 | CBIMPORT | Import data from export file | Data Migration |

### 3.2 Optional: Authorization Batch (IMS-DB2-MQ)

| # | Program | Lines | JCL Job(s) | Function | Domain |
|--:|:--------|------:|:-----------|:---------|:-------|
| 38 | CBPAUP0C | 386 | CBPAUP0J | Purge expired authorizations | Authorization |
| 39 | DBUNLDGS | 366 | UNLDGSAM | Unload IMS data via DL/I (generic) | Data Utility |
| 40 | PAUDBLOD | 369 | LOADPADB | Load IMS authorization database | Authorization |
| 41 | PAUDBUNL | 317 | UNLDPADB | Unload IMS authorization database | Authorization |

### 3.3 Optional: Transaction Type Batch (DB2)

| # | Program | Lines | JCL Job(s) | Function | Domain |
|--:|:--------|------:|:-----------|:---------|:-------|
| 42 | COBTUPDT | 237 | MNTTRDB2 | Batch maintain transaction type table | Reference Data |

### 3.4 Shared Subroutines / Utilities

| # | Program | Lines | Type | Function | Domain |
|--:|:--------|------:|:-----|:---------|:-------|
| 43 | CSUTLDTC | 157 | COBOL | Date validation utility (calls CEEDAYS) | Utility |
| 44 | COBSWAIT | 41 | COBOL | Batch wait step (calls MVSWAIT ASM) | Utility |

### 3.5 Assembler Programs

| # | Program | Source | Function | Domain |
|--:|:--------|:-------|:---------|:-------|
| 45 | COBDATFT | app/asm/COBDATFT.asm | Date format conversion (YYYYMMDD ↔ YYYY-MM-DD) | Utility |
| 46 | MVSWAIT | app/asm/MVSWAIT.asm | Timer control — STIMER macro for batch waits | Utility |

---

## 4. Copybooks — Data Layouts

| # | Copybook | Path | Record Name | Record Length | Domain |
|--:|:---------|:-----|:------------|:-------------|:-------|
| 1 | CVACT01Y | app/cpy/ | ACCOUNT-RECORD | 300 bytes (FB) | Account Master |
| 2 | CVACT02Y | app/cpy/ | CARD-RECORD | 150 bytes (FB) | Card Master |
| 3 | CVACT03Y | app/cpy/ | CARD-XREF-RECORD | 50 bytes (FB) | Customer-Account-Card Cross-Reference |
| 4 | CVCUS01Y | app/cpy/ | CUSTOMER-RECORD | 500 bytes (FB) | Customer Master |
| 5 | CVCRD01Y | app/cpy/ | CC-ACCT-ID / CC-CARD-NUM | — | Card Redefinitions for online use |
| 6 | CVTRA01Y | app/cpy/ | TRAN-CAT-BAL-RECORD | 50 bytes (FB) | Transaction Category Balance |
| 7 | CVTRA02Y | app/cpy/ | DIS-GROUP-RECORD | 50 bytes (FB) | Disclosure Group |
| 8 | CVTRA03Y | app/cpy/ | TRAN-TYPE-RECORD | 60 bytes (FB) | Transaction Type |
| 9 | CVTRA04Y | app/cpy/ | TRAN-CAT-RECORD | 60 bytes (FB) | Transaction Category |
| 10 | CVTRA05Y | app/cpy/ | TRAN-RECORD | 350 bytes (FB) | Transaction (Online VSAM KSDS) |
| 11 | CVTRA06Y | app/cpy/ | DALYTRAN-RECORD | 350 bytes (FB) | Daily Transaction (Batch Input) |
| 12 | CVTRA07Y | app/cpy/ | REPORT-NAME-HEADER | — | Report Header layout |
| 13 | CVEXPORT | app/cpy/ | EXPORT-RECORD | ~500 bytes | Branch Migration Export Record |
| 14 | CUSTREC | app/cpy/ | CUSTOMER-RECORD | 500 bytes (FB) | Customer (alternate layout for statements) |
| 15 | CSUSR01Y | app/cpy/ | SEC-USER-DATA | 80 bytes (FB) | User Security Record |
| 16 | COCOM01Y | app/cpy/ | CARDDEMO-COMMAREA | ~120 bytes | CICS Communication Area |
| 17 | COMEN02Y | app/cpy/ | CARDDEMO-MAIN-MENU-OPTIONS | — | Main Menu Configuration |
| 18 | COADM02Y | app/cpy/ | CARDDEMO-ADMIN-MENU-OPTIONS | — | Admin Menu Configuration |
| 19 | COTTL01Y | app/cpy/ | CCDA-SCREEN-TITLE | — | Screen Title Constants |
| 20 | CSDAT01Y | app/cpy/ | WS-DATE-TIME | — | Date/Time Working Storage |
| 21 | CSMSG01Y | app/cpy/ | CCDA-COMMON-MESSAGES | — | Common Messages |
| 22 | CSMSG02Y | app/cpy/ | — | — | Extended Messages |
| 23 | CSSETATY | app/cpy/ | — | — | Set Attribute Bytes |
| 24 | CSSTRPFY | app/cpy/ | — | — | String Parse Functions |
| 25 | CSLKPCDY | app/cpy/ | WS-US-PHONE-AREA-CODE-TO-EDIT | — | Validation: Phone Area Codes, State Codes, Zip Codes |
| 26 | CSUTLDWY | app/cpy/ | WS-EDIT-DATE-CCYYMMDD | — | Date Validation Working Storage |
| 27 | CSUTLDPY | app/cpy/ | — | — | Date Utility Parameters |
| 28 | CODATECN | app/cpy/ | CODATECN-REC | — | ASM Date Conversion I/O Area |
| 29 | COSTM01 | app/cpy/ | — | — | Statement Constants |
| 30 | UNUSED1Y | app/cpy/ | — | — | Placeholder (unused) |

### Optional Module Copybooks

| # | Copybook | Path | Record Name | Domain |
|--:|:---------|:-----|:------------|:-------|
| 31 | CCPAURQY | app/app-authorization-ims-db2-mq/cpy/ | PA-RQ-* | Authorization Request |
| 32 | CCPAURLY | app/app-authorization-ims-db2-mq/cpy/ | PA-RL-* | Authorization Reply |
| 33 | CCPAUERY | app/app-authorization-ims-db2-mq/cpy/ | ERR-* | Authorization Error Record |
| 34 | CIPAUDTY | app/app-authorization-ims-db2-mq/cpy/ | PA-* | Authorization Detail (IMS Segment) |
| 35 | CIPAUSMY | app/app-authorization-ims-db2-mq/cpy/ | PA-ACCT-ID / PA-CUST-ID | Authorization Summary (IMS) |
| 36 | IMSFUNCS | app/app-authorization-ims-db2-mq/cpy/ | — | IMS DL/I Function Constants |
| 37 | PADFLPCB | app/app-authorization-ims-db2-mq/cpy/ | — | IMS PCB - Detail Full |
| 38 | PASFLPCB | app/app-authorization-ims-db2-mq/cpy/ | — | IMS PCB - Summary Full |
| 39 | PAUTBPCB | app/app-authorization-ims-db2-mq/cpy/ | — | IMS PCB - Auth Batch |
| 40 | CSDB2RPY | app/app-transaction-type-db2/cpy/ | — | DB2 Reply Area |
| 41 | CSDB2RWY | app/app-transaction-type-db2/cpy/ | WS-DSNTIAC-* | DB2 Working Storage / DSNTIAC |

---

## 5. BMS Map Definitions

### 5.1 Core Maps

| # | Map File | Screen Title | Associated Program |
|--:|:---------|:-------------|:-------------------|
| 1 | COSGN00.bms | Login Screen | COSGN00C |
| 2 | COMEN01.bms | Main Menu Screen | COMEN01C |
| 3 | COADM01.bms | Admin Menu Screen | COADM01C |
| 4 | COACTUP.bms | Account Update Screen | COACTUPC |
| 5 | COACTVW.bms | Account Viewer Screen | COACTVWC |
| 6 | COCRDLI.bms | Card Listing Screen | COCRDLIC |
| 7 | COCRDSL.bms | Card Selection Screen | COCRDSLC |
| 8 | COCRDUP.bms | Card Update Screen | COCRDUPC |
| 9 | COTRN00.bms | Transaction List | COTRN00C |
| 10 | COTRN01.bms | Transaction View | COTRN01C |
| 11 | COTRN02.bms | Transaction Add | COTRN02C |
| 12 | CORPT00.bms | Transaction Reports | CORPT00C |
| 13 | COBIL00.bms | Bill Payment | COBIL00C |
| 14 | COUSR00.bms | List Users | COUSR00C |
| 15 | COUSR01.bms | Add User | COUSR01C |
| 16 | COUSR02.bms | Update User | COUSR02C |
| 17 | COUSR03.bms | Delete User | COUSR03C |

### 5.2 Optional Module Maps

| # | Map File | Path | Associated Program |
|--:|:---------|:-----|:-------------------|
| 18 | COPAU00.bms | app/app-authorization-ims-db2-mq/bms/ | COPAUS0C |
| 19 | COPAU01.bms | app/app-authorization-ims-db2-mq/bms/ | COPAUS1C |
| 20 | COTRTLI.bms | app/app-transaction-type-db2/bms/ | COTRTLIC |
| 21 | COTRTUP.bms | app/app-transaction-type-db2/bms/ | COTRTUPC |

### 5.3 BMS-Generated Copybooks

Each BMS map produces a symbolic description copybook used by its COBOL program. These reside in `app/cpy-bms/` (core) and `app/app-*/cpy-bms/` (optional modules):

COACTUP.CPY, COACTVW.CPY, COADM01.CPY, COBIL00.CPY, COCRDLI.CPY, COCRDSL.CPY, COCRDUP.CPY, COMEN01.CPY, CORPT00.CPY, COSGN00.CPY, COTRN00.CPY, COTRN01.CPY, COTRN02.CPY, COUSR00.CPY, COUSR01.CPY, COUSR02.CPY, COUSR03.CPY, COPAU00.cpy, COPAU01.cpy, COTRTLI.cpy, COTRTUP.cpy

---

## 6. JCL Jobs

### 6.1 Data Initialization Jobs

| # | Job Name | Program(s) | Purpose | Optional Module |
|--:|:---------|:-----------|:--------|:----------------|
| 1 | DUSRSECJ | IEBGENER, IDCAMS | Load user security VSAM file from flat file | — |
| 2 | ACCTFILE | IDCAMS | Delete / define / load Account VSAM KSDS | — |
| 3 | CARDFILE | IDCAMS, SDSF | Delete / define / load Card VSAM KSDS + AIX | — |
| 4 | CUSTFILE | IDCAMS, SDSF | Delete / define / load Customer VSAM KSDS | — |
| 5 | XREFFILE | IDCAMS | Delete / define / load Card-Account-Customer XREF VSAM + AIX | — |
| 6 | TRANFILE | IDCAMS | Load Transaction master VSAM from flat file | — |
| 7 | DISCGRP | IDCAMS | Load Disclosure Group VSAM KSDS | — |
| 8 | TCATBALF | IDCAMS | Load Transaction Category Balance VSAM | — |
| 9 | TRANCATG | IDCAMS | Load Transaction Category types VSAM | — |
| 10 | TRANTYPE | IDCAMS | Load Transaction Type VSAM | — |
| 11 | DEFGDGB | IDCAMS | Define GDG base entries | — |
| 12 | DEFGDGD | IDCAMS, IEBGENER | Define GDG bases for DB2 datasets | DB2 |
| 13 | CREADB21 | DSNTEP4 | Create DB2 database and load tables | DB2 |

### 6.2 Operational / Batch Processing Jobs

| # | Job Name | Program(s) | Purpose | Optional Module |
|--:|:---------|:-----------|:--------|:----------------|
| 14 | CLOSEFIL | SDSF | Close VSAM files in CICS region | — |
| 15 | OPENFIL | SDSF | Open VSAM files in CICS region | — |
| 16 | POSTTRAN | CBTRN02C | Daily transaction posting | — |
| 17 | INTCALC | CBACT04C | Monthly interest calculation | — |
| 18 | TRANBKP | IDCAMS | Backup / refresh Transaction Master (GDG) | — |
| 19 | COMBTRAN | SORT | Combine system + daily transactions | — |
| 20 | CREASTMT | CBSTM03A | Produce transaction statements | — |
| 21 | TRANREPT | CBTRN03C | Generate transaction report (from CICS) | — |
| 22 | TRANIDX | IDCAMS | Define alternate index on transaction file | — |
| 23 | WAITSTEP | COBSWAIT | Timer wait step for job scheduling | — |
| 24 | TRANEXTR | DSNTIAUL | Extract transaction types/categories from DB2 | DB2 |
| 25 | MNTTRDB2 | COBTUPDT | Maintain transaction type DB2 table | DB2 |
| 26 | CBPAUP0J | CBPAUP0C | Purge expired authorizations | IMS-DB2-MQ |

### 6.3 Data Utility / Diagnostic Jobs

| # | Job Name | Program(s) | Purpose | Optional Module |
|--:|:---------|:-----------|:--------|:----------------|
| 27 | READACCT | CBACT01C | Read and dump account data | — |
| 28 | READCARD | CBACT02C | Read and dump card data | — |
| 29 | READCUST | CBCUS01C | Read and dump customer data | — |
| 30 | READXREF | CBACT03C | Read and dump cross-reference data | — |
| 31 | REPTFILE | IDCAMS | Define report output VSAM file | — |
| 32 | PRTCATBL | SORT | Print transaction category balance | — |
| 33 | DALYREJS | IDCAMS | Define daily rejects GDG file | — |
| 34 | DEFCUST | IDCAMS | Alternate customer file definition | — |
| 35 | ESDSRRDS | IDCAMS, IEBGENER | Create ESDS and RRDS VSAM demo files | — |
| 36 | CBEXPORT | CBEXPORT | Export customer data for branch migration | — |
| 37 | CBIMPORT | CBIMPORT | Import customer data from export file | — |

### 6.4 Specialty / Utility JCL

| # | Job Name | Program(s) | Purpose |
|--:|:---------|:-----------|:--------|
| 38 | CBADMCDJ | DFHCSDUP | CSD batch update for CICS resource definitions |
| 39 | FTPJCL | FTP | FTP file transfer utility |
| 40 | TXT2PDF1 | TXT2PDF | Convert text files to PDF |
| 41 | INTRDRJ1 | — | Internal reader job submission (example 1) |
| 42 | INTRDRJ2 | IDCAMS | Internal reader job submission (example 2) |

### 6.5 Optional Module JCL

| # | Job Name | Path | Program(s) | Purpose |
|--:|:---------|:-----|:-----------|:--------|
| 43 | CBPAUP0J | app/app-authorization-ims-db2-mq/jcl/ | CBPAUP0C | Purge expired authorizations |
| 44 | DBPAUTP0 | app/app-authorization-ims-db2-mq/jcl/ | — | IMS DB definition for authorization |
| 45 | LOADPADB | app/app-authorization-ims-db2-mq/jcl/ | PAUDBLOD | Load IMS authorization database |
| 46 | UNLDGSAM | app/app-authorization-ims-db2-mq/jcl/ | DBUNLDGS | Generic IMS unload |
| 47 | UNLDPADB | app/app-authorization-ims-db2-mq/jcl/ | PAUDBUNL | Unload IMS authorization database |
| 48 | CREADB21 | app/app-transaction-type-db2/jcl/ | DSNTEP4 | Create DB2 database |
| 49 | MNTTRDB2 | app/app-transaction-type-db2/jcl/ | COBTUPDT | Batch transaction type maintenance |
| 50 | TRANEXTR | app/app-transaction-type-db2/jcl/ | DSNTIAUL | Extract DB2 transaction types |

---

## 7. Supporting Artifacts

### 7.1 Procedures

| File | Path | Purpose |
|:-----|:-----|:--------|
| REPROC.prc | app/proc/ | Reusable REPRO procedure for VSAM-to-flat copies |
| TRANREPT.prc | app/proc/ | Transaction report cataloged procedure |

### 7.2 Scheduler Definitions

| File | Path | Format | Purpose |
|:-----|:-----|:-------|:--------|
| CardDemo.controlm | app/scheduler/ | Control-M XML | Production scheduling — 3 workflows (Daily, Weekly, Monthly) |
| CardDemo.ca7 | app/scheduler/ | CA-7 | Legacy scheduling definitions |

### 7.3 CSD Resource Definitions

| File | Path | Purpose |
|:-----|:-----|:--------|
| CARDDEMO.CSD | app/csd/ | Core CICS resource definitions |
| CRDDEMO2.csd | app/app-authorization-ims-db2-mq/csd/ | Authorization module CICS resources |
| CRDDEMOD.csd | app/app-transaction-type-db2/csd/ | DB2 transaction type CICS resources |
| CRDDEMOM.csd | app/app-vsam-mq/csd/ | MQ integration CICS resources |

### 7.4 DB2 Artifacts

| File | Path | Type | Purpose |
|:-----|:-----|:-----|:--------|
| AUTHFRDS.ddl / .dcl | app/app-authorization-ims-db2-mq/ | DDL/DCL | Authorization fraud detection table |
| XAUTHFRD.ddl | app/app-authorization-ims-db2-mq/ | DDL | Authorization fraud index |
| TRNTYPE.ddl / DCLTRTYP.dcl | app/app-transaction-type-db2/ | DDL/DCL | Transaction type table |
| TRNTYCAT.ddl / DCLTRCAT.dcl | app/app-transaction-type-db2/ | DDL/DCL | Transaction category table |
| XTRNTYPE.ddl / XTRNTYCAT.ddl | app/app-transaction-type-db2/ | DDL | Indexes on transaction tables |

### 7.5 IMS Artifacts

| File | Path | Type | Purpose |
|:-----|:-----|:-----|:--------|
| DBPAUTP0.dbd | app/app-authorization-ims-db2-mq/ims/ | DBD | Primary authorization database |
| DBPAUTX0.dbd | app/app-authorization-ims-db2-mq/ims/ | DBD | Authorization index database |
| PADFLDBD.DBD | app/app-authorization-ims-db2-mq/ims/ | DBD | Detail full-function database |
| PASFLDBD.DBD | app/app-authorization-ims-db2-mq/ims/ | DBD | Summary full-function database |
| PSBPAUTB.psb | app/app-authorization-ims-db2-mq/ims/ | PSB | Batch authorization access |
| PSBPAUTL.psb | app/app-authorization-ims-db2-mq/ims/ | PSB | Online authorization access |
| DLIGSAMP.PSB | app/app-authorization-ims-db2-mq/ims/ | PSB | Generic DL/I sample |
| PAUTBUNL.PSB | app/app-authorization-ims-db2-mq/ims/ | PSB | Authorization unload access |

### 7.6 Assembler Macros

| File | Path | Purpose |
|:-----|:-----|:--------|
| ASMWAIT.mac | app/maclib/ | Wait macro definition |
| COCDATFT.mac | app/maclib/ | Date format conversion macro |

### 7.7 Data Files

| Dataset | Copybook | Format | Description |
|:--------|:---------|:-------|:------------|
| AWS.M2.CARDDEMO.USRSEC.PS | CSUSR01Y | FB 80 | User Security |
| AWS.M2.CARDDEMO.ACCTDATA.PS | CVACT01Y | FB 300 | Account Data |
| AWS.M2.CARDDEMO.CARDDATA.PS | CVACT02Y | FB 150 | Card Data |
| AWS.M2.CARDDEMO.CUSTDATA.PS | CVCUS01Y | FB 500 | Customer Data |
| AWS.M2.CARDDEMO.CARDXREF.PS | CVACT03Y | FB 50 | Cross-Reference |
| AWS.M2.CARDDEMO.DALYTRAN.PS | CVTRA06Y | FB 350 | Daily Transactions |
| AWS.M2.CARDDEMO.DALYTRAN.PS.INIT | CVTRA06Y | FB 350 | Transaction Init Record |
| AWS.M2.CARDDEMO.DISCGRP.PS | CVTRA02Y | FB 50 | Disclosure Groups |
| AWS.M2.CARDDEMO.TRANCATG.PS | CVTRA04Y | FB 60 | Transaction Categories |
| AWS.M2.CARDDEMO.TRANTYPE.PS | CVTRA03Y | FB 60 | Transaction Types |
| AWS.M2.CARDDEMO.TCATBALF.PS | CVTRA01Y | FB 50 | Category Balance |

---

## 8. Classification Matrix

| Category | Core | Optional (DB2) | Optional (IMS-DB2-MQ) | Optional (VSAM-MQ) |
|:---------|:-----|:---------------|:----------------------|:--------------------|
| **Online Programs** | 17 | 2 | 4 | 2 |
| **Batch Programs** | 12 | 1 | 4 | — |
| **Subroutines** | 3 | — | — | — |
| **Assembler** | 2 | — | — | — |
| **Total Programs** | **34** | **3** | **8** | **2** |
| **Total LOC** | 18,005 | 4,037 | 4,348 | 1,144 |
