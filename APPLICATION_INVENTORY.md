# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo - Mainframe Credit Card Management System
> **Total Assets**: 44 COBOL programs, 42 copybooks, 38 JCL jobs, 21 BMS maps, 2 ASM modules, 2 macros, 2 procs, 2 schedulers, 1 CSD

---

## Table of Contents

- [1. COBOL Programs](#1-cobol-programs)
  - [1.1 Online (CICS) Programs](#11-online-cics-programs)
  - [1.2 Batch Programs](#12-batch-programs)
  - [1.3 Utility / Subroutine Programs](#13-utility--subroutine-programs)
  - [1.4 Optional Module Programs](#14-optional-module-programs)
- [2. Copybooks](#2-copybooks)
  - [2.1 Data Record Copybooks](#21-data-record-copybooks)
  - [2.2 System / Common Copybooks](#22-system--common-copybooks)
  - [2.3 BMS-Generated Copybooks](#23-bms-generated-copybooks)
  - [2.4 Optional Module Copybooks](#24-optional-module-copybooks)
- [3. JCL Jobs](#3-jcl-jobs)
  - [3.1 Core Batch Jobs](#31-core-batch-jobs)
  - [3.2 Utility / Infrastructure Jobs](#32-utility--infrastructure-jobs)
  - [3.3 Optional Module Jobs](#33-optional-module-jobs)
- [4. BMS Maps](#4-bms-maps)
  - [4.1 Core BMS Maps](#41-core-bms-maps)
  - [4.2 Optional Module BMS Maps](#42-optional-module-bms-maps)
- [5. Assembler Modules](#5-assembler-modules)
- [6. Supporting Assets](#6-supporting-assets)
- [7. Classification Summary](#7-classification-summary)

---

## 1. COBOL Programs

### 1.1 Online (CICS) Programs

| # | Program | Source Path | Lines | Transaction | Function | Domain | Technologies |
|---|---------|-------------|------:|-------------|----------|--------|-------------|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | 260 | CC00 | Signon Screen | Security | CICS, VSAM |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | 308 | CM00 | Main Menu Navigation | Navigation | CICS |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | 288 | CA00 | Admin Menu | Admin | CICS |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | 941 | CAVW | Account View | Account | CICS, VSAM |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | 4236 | CAUP | Account Update | Account | CICS, VSAM |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | 1459 | CCLI | Credit Card List | Card | CICS, VSAM |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | 887 | CCDL | Credit Card Detail View | Card | CICS, VSAM |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | 1560 | CCUP | Credit Card Update | Card | CICS, VSAM |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | 699 | CT00 | Transaction List | Transaction | CICS, VSAM |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | 330 | CT01 | Transaction View | Transaction | CICS, VSAM |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | 783 | CT02 | Transaction Add | Transaction | CICS, VSAM |
| 12 | CORPT00C | `app/cbl/CORPT00C.cbl` | 649 | CR00 | Transaction Report Request | Report | CICS, TD Queue |
| 13 | COBIL00C | `app/cbl/COBIL00C.cbl` | 572 | CB00 | Bill Payment | Payment | CICS, VSAM |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | 695 | CU00 | List Users | Admin / User Mgmt | CICS, VSAM |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | 299 | CU01 | Add User | Admin / User Mgmt | CICS, VSAM |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | 414 | CU02 | Update User | Admin / User Mgmt | CICS, VSAM |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | 359 | CU03 | Delete User | Admin / User Mgmt | CICS, VSAM |

### 1.2 Batch Programs

| # | Program | Source Path | Lines | Invoked By JCL | Function | Domain | Technologies |
|---|---------|-------------|------:|-----------------|----------|--------|-------------|
| 18 | CBACT01C | `app/cbl/CBACT01C.cbl` | 430 | READACCT | Read & Report Account Data | Account | Batch, VSAM, ASM Call |
| 19 | CBACT02C | `app/cbl/CBACT02C.cbl` | 178 | READCARD | Read Card Data | Card | Batch, VSAM |
| 20 | CBACT03C | `app/cbl/CBACT03C.cbl` | 178 | READXREF | Read Card Cross-Reference | Cross-Reference | Batch, VSAM |
| 21 | CBACT04C | `app/cbl/CBACT04C.cbl` | 652 | INTCALC | Interest Rate Calculation | Financial / Interest | Batch, VSAM |
| 22 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | 178 | READCUST | Read Customer Data | Customer | Batch, VSAM |
| 23 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | 494 | PRTCATBL | Print Transaction Category Balance | Report / Transaction | Batch, VSAM |
| 24 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | 731 | POSTTRAN | Post Daily Transactions | Transaction Processing | Batch, VSAM |
| 25 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | 649 | TRANREPT | Daily Transaction Report | Report | Batch, VSAM |
| 26 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | 924 | CREASTMT | Account Statement Generation (Text + HTML) | Statement / Report | Batch, VSAM |
| 27 | CBEXPORT | `app/cbl/CBEXPORT.cbl` | 582 | CBEXPORT | Export Data to Sequential Files | Data Export | Batch, VSAM |
| 28 | CBIMPORT | `app/cbl/CBIMPORT.cbl` | 487 | CBIMPORT | Import Data from Sequential Files | Data Import | Batch, VSAM |

### 1.3 Utility / Subroutine Programs

| # | Program | Source Path | Lines | Function | Called By | Technologies |
|---|---------|-------------|------:|----------|-----------|-------------|
| 29 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | 230 | File I/O subroutine for statement processing | CBSTM03A | Batch, VSAM |
| 30 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | 157 | Date validation via CEEDAYS API | COTRN02C, CORPT00C | LE Callable Services |
| 31 | COBSWAIT | `app/cbl/COBSWAIT.cbl` | 41 | Timer wait (calls MVSWAIT ASM) | WAITSTEP JCL | Batch, ASM Call |

### 1.4 Optional Module Programs

#### IMS-DB2-MQ: Pending Authorizations

| # | Program | Source Path | Lines | Transaction | Function | Technologies |
|---|---------|-------------|------:|-------------|----------|-------------|
| 32 | COPAUS0C | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | 1032 | CPVS | Pending Authorization Summary | CICS, IMS, VSAM, BMS |
| 33 | COPAUS1C | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | 604 | CPVD | Pending Authorization Details | CICS, IMS, DB2, BMS |
| 34 | COPAUS2C | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | 244 | - | Mark Authorization as Fraud | CICS, IMS, DB2 |
| 35 | COPAUA0C | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | 1026 | CP00 | Process Authorization Requests | CICS, IMS, MQ |
| 36 | CBPAUP0C | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | 386 | - | Purge Expired Authorizations | Batch, IMS |
| 37 | PAUDBLOD | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | 369 | - | Load IMS Pending Auth Database | Batch, IMS |
| 38 | PAUDBUNL | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | 317 | - | Unload IMS Pending Auth Database | Batch, IMS |
| 39 | DBUNLDGS | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | 366 | - | Generic IMS GSAM DB Unload | Batch, IMS, GSAM |

#### DB2: Transaction Type Management

| # | Program | Source Path | Lines | Transaction | Function | Technologies |
|---|---------|-------------|------:|-------------|----------|-------------|
| 40 | COTRTLIC | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | 2098 | CTLI | Transaction Type List/Update/Delete | CICS, DB2, BMS |
| 41 | COTRTUPC | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | 1702 | CTTU | Transaction Type Add/Edit | CICS, DB2, BMS |
| 42 | COBTUPDT | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` | 237 | - | Batch Transaction Type Maintenance | Batch, DB2 |

#### MQ: Account & Date Inquiry

| # | Program | Source Path | Lines | Transaction | Function | Technologies |
|---|---------|-------------|------:|-------------|----------|-------------|
| 43 | COACCT01 | `app/app-vsam-mq/cbl/COACCT01.cbl` | 620 | CDRA | Account Details Inquiry via MQ | CICS, MQ, VSAM |
| 44 | CODATE01 | `app/app-vsam-mq/cbl/CODATE01.cbl` | 524 | CDRD | System Date Inquiry via MQ | CICS, MQ |

---

## 2. Copybooks

### 2.1 Data Record Copybooks

| # | Copybook | Source Path | Record Name | Record Length | Domain | Description |
|---|----------|-------------|-------------|-------------:|--------|-------------|
| 1 | CVACT01Y | `app/cpy/CVACT01Y.cpy` | ACCT-RECORD | 300 | Account | Account master record |
| 2 | CVACT02Y | `app/cpy/CVACT02Y.cpy` | CARD-RECORD | 150 | Card | Credit card record |
| 3 | CVACT03Y | `app/cpy/CVACT03Y.cpy` | CARD-XREF-RECORD | 50 | Cross-Reference | Card-Account-Customer cross-reference |
| 4 | CVCUS01Y | `app/cpy/CVCUS01Y.cpy` | CUSTOMER-RECORD | 500 | Customer | Customer master record |
| 5 | CVCRD01Y | `app/cpy/CVCRD01Y.cpy` | CARD-RECORD (detail) | 150 | Card | Credit card detail layout |
| 6 | CVTRA01Y | `app/cpy/CVTRA01Y.cpy` | TRAN-CAT-BAL-RECORD | 50 | Transaction | Transaction category balance |
| 7 | CVTRA02Y | `app/cpy/CVTRA02Y.cpy` | DIS-GROUP-RECORD | 50 | Disclosure | Disclosure group (interest rates) |
| 8 | CVTRA03Y | `app/cpy/CVTRA03Y.cpy` | TRAN-TYPE-RECORD | 60 | Transaction | Transaction type reference |
| 9 | CVTRA04Y | `app/cpy/CVTRA04Y.cpy` | TRAN-CAT-RECORD | 60 | Transaction | Transaction category type |
| 10 | CVTRA05Y | `app/cpy/CVTRA05Y.cpy` | TRAN-RECORD | 350 | Transaction | Online transaction record |
| 11 | CVTRA06Y | `app/cpy/CVTRA06Y.cpy` | DALYTRAN-RECORD | 350 | Transaction | Daily transaction record |
| 12 | CVTRA07Y | `app/cpy/CVTRA07Y.cpy` | REPORT-NAME-HEADER / TRANSACTION-DETAIL-REPORT | - | Report | Report layout structures |
| 13 | CVEXPORT | `app/cpy/CVEXPORT.cpy` | EXPORT-RECORD | 500+ | Data Export | Export record with REDEFINES for multiple entity types |
| 14 | COSTM01 | `app/cpy/COSTM01.CPY` | TRNX-RECORD | 350 | Statement | Transaction layout reordered for statement processing (key=CARD+TRAN-ID) |
| 15 | CSUSR01Y | `app/cpy/CSUSR01Y.cpy` | SEC-USER-DATA | 80 | Security | User security record |
| 16 | CUSTREC | `app/cpy/CUSTREC.cpy` | CUSTOMER-RECORD (alt) | 500 | Customer | Alternate customer record layout |
| 17 | UNUSED1Y | `app/cpy/UNUSED1Y.cpy` | UNUSED-DATA | 80 | - | Unused/deprecated data structure |

### 2.2 System / Common Copybooks

| # | Copybook | Source Path | Description |
|---|----------|-------------|-------------|
| 18 | COCOM01Y | `app/cpy/COCOM01Y.cpy` | Common communication area (COMMAREA) between CICS programs |
| 19 | COMEN02Y | `app/cpy/COMEN02Y.cpy` | Menu option definitions (program names per menu choice) |
| 20 | COADM02Y | `app/cpy/COADM02Y.cpy` | Admin menu option definitions |
| 21 | COTTL01Y | `app/cpy/COTTL01Y.cpy` | Screen title/header layout |
| 22 | CSDAT01Y | `app/cpy/CSDAT01Y.cpy` | Date-related working storage variables |
| 23 | CSMSG01Y | `app/cpy/CSMSG01Y.cpy` | Message area (short messages) |
| 24 | CSMSG02Y | `app/cpy/CSMSG02Y.cpy` | Message area (long messages) |
| 25 | CSSETATY | `app/cpy/CSSETATY.cpy` | Screen attribute settings |
| 26 | CSSTRPFY | `app/cpy/CSSTRPFY.cpy` | String processing functions (padding/formatting) |
| 27 | CSLKPCDY | `app/cpy/CSLKPCDY.cpy` | Lookup code definitions |
| 28 | CODATECN | `app/cpy/CODATECN.cpy` | Date conversion record for ASM call (COBDATFT) |
| 29 | CSUTLDPY | `app/cpy/CSUTLDPY.cpy` | Date utility parameter structure (CSUTLDTC) |
| 30 | CSUTLDWY | `app/cpy/CSUTLDWY.cpy` | Date utility working storage (CSUTLDTC) |

### 2.3 BMS-Generated Copybooks

| # | Copybook | Source Path | Associated BMS | Screen |
|---|----------|-------------|----------------|--------|
| 31 | COSGN00 | `app/cpy-bms/COSGN00.CPY` | COSGN00.bms | Signon |
| 32 | COMEN01 | `app/cpy-bms/COMEN01.CPY` | COMEN01.bms | Main Menu |
| 33 | COADM01 | `app/cpy-bms/COADM01.CPY` | COADM01.bms | Admin Menu |
| 34 | COACTVW | `app/cpy-bms/COACTVW.CPY` | COACTVW.bms | Account View |
| 35 | COACTUP | `app/cpy-bms/COACTUP.CPY` | COACTUP.bms | Account Update |
| 36 | COCRDLI | `app/cpy-bms/COCRDLI.CPY` | COCRDLI.bms | Credit Card List |
| 37 | COCRDSL | `app/cpy-bms/COCRDSL.CPY` | COCRDSL.bms | Credit Card Detail |
| 38 | COCRDUP | `app/cpy-bms/COCRDUP.CPY` | COCRDUP.bms | Credit Card Update |
| 39 | COTRN00 | `app/cpy-bms/COTRN00.CPY` | COTRN00.bms | Transaction List |
| 40 | COTRN01 | `app/cpy-bms/COTRN01.CPY` | COTRN01.bms | Transaction View |
| 41 | COTRN02 | `app/cpy-bms/COTRN02.CPY` | COTRN02.bms | Transaction Add |
| 42 | CORPT00 | `app/cpy-bms/CORPT00.CPY` | CORPT00.bms | Transaction Report |
| 43 | COBIL00 | `app/cpy-bms/COBIL00.CPY` | COBIL00.bms | Bill Payment |
| 44 | COUSR00 | `app/cpy-bms/COUSR00.CPY` | COUSR00.bms | User List |
| 45 | COUSR01 | `app/cpy-bms/COUSR01.CPY` | COUSR01.bms | User Add |
| 46 | COUSR02 | `app/cpy-bms/COUSR02.CPY` | COUSR02.bms | User Update |
| 47 | COUSR03 | `app/cpy-bms/COUSR03.CPY` | COUSR03.bms | User Delete |

### 2.4 Optional Module Copybooks

#### IMS-DB2-MQ: Pending Authorizations

| # | Copybook | Source Path | Description |
|---|----------|-------------|-------------|
| 48 | CCPAUERY | `app/app-authorization-ims-db2-mq/cpy/CCPAUERY.cpy` | Pending authorization error handling |
| 49 | CCPAURLY | `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy` | Pending authorization reply structure |
| 50 | CCPAURQY | `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy` | Pending authorization request structure |
| 51 | CIPAUDTY | `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy` | IMS pending auth detail segment |
| 52 | CIPAUSMY | `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy` | IMS pending auth summary segment |
| 53 | IMSFUNCS | `app/app-authorization-ims-db2-mq/cpy/IMSFUNCS.cpy` | IMS function codes (GU, GN, ISRT, DLET) |
| 54 | PADFLPCB | `app/app-authorization-ims-db2-mq/cpy/PADFLPCB.CPY` | IMS PCB for detail flat file DB |
| 55 | PASFLPCB | `app/app-authorization-ims-db2-mq/cpy/PASFLPCB.CPY` | IMS PCB for summary flat file DB |
| 56 | PAUTBPCB | `app/app-authorization-ims-db2-mq/cpy/PAUTBPCB.CPY` | IMS PCB for auth base DB |
| 57 | COPAU00 | `app/app-authorization-ims-db2-mq/cpy-bms/COPAU00.cpy` | BMS copybook for auth summary screen |
| 58 | COPAU01 | `app/app-authorization-ims-db2-mq/cpy-bms/COPAU01.cpy` | BMS copybook for auth detail screen |

#### DB2: Transaction Type Management

| # | Copybook | Source Path | Description |
|---|----------|-------------|-------------|
| 59 | CSDB2RPY | `app/app-transaction-type-db2/cpy/CSDB2RPY.cpy` | DB2 common procedures (DSNTIAC) |
| 60 | CSDB2RWY | `app/app-transaction-type-db2/cpy/CSDB2RWY.cpy` | DB2 common working storage |
| 61 | COTRTLI | `app/app-transaction-type-db2/cpy-bms/COTRTLI.cpy` | BMS copybook for tran type list |
| 62 | COTRTUP | `app/app-transaction-type-db2/cpy-bms/COTRTUP.cpy` | BMS copybook for tran type update |

---

## 3. JCL Jobs

### 3.1 Core Batch Jobs

| # | Job Name | Source Path | Program | Function | Category |
|---|----------|-------------|---------|----------|----------|
| 1 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | CBTRN02C | Post daily transactions to master file | Transaction Processing |
| 2 | INTCALC | `app/jcl/INTCALC.jcl` | CBACT04C | Calculate interest on accounts | Financial Calculation |
| 3 | CREASTMT | `app/jcl/CREASTMT.JCL` | CBSTM03A | Generate account statements (text + HTML) | Reporting |
| 4 | TRANREPT | `app/jcl/TRANREPT.jcl` | CBTRN03C | Generate daily transaction report | Reporting |
| 5 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | SORT | Combine system + daily transaction files | Data Management |
| 6 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | CBTRN01C | Print transaction category balance | Reporting |
| 7 | REPTFILE | `app/jcl/REPTFILE.jcl` | - | Report file utility | Reporting |
| 8 | READACCT | `app/jcl/READACCT.jcl` | CBACT01C | Read and display account data | Data Validation |
| 9 | READCARD | `app/jcl/READCARD.jcl` | CBACT02C | Read and display card data | Data Validation |
| 10 | READXREF | `app/jcl/READXREF.jcl` | CBACT03C | Read and display cross-reference data | Data Validation |
| 11 | READCUST | `app/jcl/READCUST.jcl` | CBCUS01C | Read and display customer data | Data Validation |
| 12 | CBEXPORT | `app/jcl/CBEXPORT.jcl` | CBEXPORT | Export VSAM data to sequential files | Data Export |
| 13 | CBIMPORT | `app/jcl/CBIMPORT.jcl` | CBIMPORT | Import sequential files to VSAM | Data Import |
| 14 | DALYREJS | `app/jcl/DALYREJS.jcl` | - | Process daily rejects | Transaction Processing |
| 15 | WAITSTEP | `app/jcl/WAITSTEP.jcl` | COBSWAIT | Wait/delay step for job scheduling | Scheduling |

### 3.2 Utility / Infrastructure Jobs

| # | Job Name | Source Path | Program | Function | Category |
|---|----------|-------------|---------|----------|----------|
| 16 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | IEBGENER | Load user security VSAM file | Security Setup |
| 17 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | IDCAMS | Define & load Account VSAM file | Data Setup |
| 18 | CARDFILE | `app/jcl/CARDFILE.jcl` | IDCAMS | Define & load Card VSAM file | Data Setup |
| 19 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | IDCAMS | Define & load Customer VSAM file | Data Setup |
| 20 | XREFFILE | `app/jcl/XREFFILE.jcl` | IDCAMS | Define & load Card-Account cross-reference VSAM + AIX | Data Setup |
| 21 | TRANFILE | `app/jcl/TRANFILE.jcl` | IDCAMS | Load Transaction master VSAM file | Data Setup |
| 22 | TRANBKP | `app/jcl/TRANBKP.jcl` | IDCAMS | Backup Transaction master | Data Backup |
| 23 | TRANIDX | `app/jcl/TRANIDX.jcl` | IDCAMS | Define alternate index on transaction file | Data Setup |
| 24 | DISCGRP | `app/jcl/DISCGRP.jcl` | IDCAMS | Load Disclosure Group VSAM file | Data Setup |
| 25 | TCATBALF | `app/jcl/TCATBALF.jcl` | IDCAMS | Load Transaction Category Balance VSAM | Data Setup |
| 26 | TRANCATG | `app/jcl/TRANCATG.jcl` | IDCAMS | Load Transaction Category Type VSAM | Data Setup |
| 27 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | IDCAMS | Load Transaction Type VSAM | Data Setup |
| 28 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | IDCAMS | Define GDG base clusters | Infrastructure |
| 29 | DEFGDGD | `app/jcl/DEFGDGD.jcl` | IDCAMS | Define GDG bases for DB2 module | Infrastructure |
| 30 | DEFCUST | `app/jcl/DEFCUST.jcl` | IDCAMS | Define Customer VSAM cluster | Data Setup |
| 31 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | IEFBR14 | Close VSAM files in CICS | Operations |
| 32 | OPENFIL | `app/jcl/OPENFIL.jcl` | IEFBR14 | Open VSAM files in CICS | Operations |
| 33 | ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | IDCAMS | Create ESDS and RRDS VSAM files | Data Setup |
| 34 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | - | Admin card demo job | Administration |
| 35 | FTPJCL | `app/jcl/FTPJCL.JCL` | FTP | FTP file transfer | Utility |
| 36 | TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | IKJEFT1B | Convert text report to PDF | Utility |
| 37 | INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | IDCAMS/IEBGENER | Internal reader - triggers INTRDRJ2 | Scheduling |
| 38 | INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | IDCAMS | Internal reader - triggered by INTRDRJ1 | Scheduling |

### 3.3 Optional Module Jobs

| # | Job Name | Source Path | Program | Function | Module |
|---|----------|-------------|---------|----------|--------|
| 39 | CBPAUP0J | `app/app-authorization-ims-db2-mq/jcl/CBPAUP0J.jcl` | CBPAUP0C | Purge expired authorizations | IMS-DB2-MQ |
| 40 | DBPAUTP0 | `app/app-authorization-ims-db2-mq/jcl/DBPAUTP0.jcl` | - | IMS DB unload for pending auth | IMS-DB2-MQ |
| 41 | LOADPADB | `app/app-authorization-ims-db2-mq/jcl/LOADPADB.JCL` | PAUDBLOD | Load IMS pending auth database | IMS-DB2-MQ |
| 42 | UNLDPADB | `app/app-authorization-ims-db2-mq/jcl/UNLDPADB.JCL` | PAUDBUNL | Unload IMS pending auth database | IMS-DB2-MQ |
| 43 | UNLDGSAM | `app/app-authorization-ims-db2-mq/jcl/UNLDGSAM.JCL` | DBUNLDGS | Generic GSAM DB unload | IMS-DB2-MQ |
| 44 | CREADB21 | `app/app-transaction-type-db2/jcl/CREADB21.jcl` | DSNTEP4 | Create DB2 database & load tables | DB2 Tran Type |
| 45 | TRANEXTR | `app/app-transaction-type-db2/jcl/TRANEXTR.jcl` | DSNTIAUL | Extract DB2 tran type/category data | DB2 Tran Type |
| 46 | MNTTRDB2 | `app/app-transaction-type-db2/jcl/MNTTRDB2.jcl` | COBTUPDT | Maintain DB2 transaction type table | DB2 Tran Type |

---

## 4. BMS Maps

### 4.1 Core BMS Maps

| # | Map Name | Source Path | Screen | Used By Program |
|---|----------|-------------|--------|-----------------|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | Signon | COSGN00C |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | Main Menu | COMEN01C |
| 3 | COADM01 | `app/bms/COADM01.bms` | Admin Menu | COADM01C |
| 4 | COACTVW | `app/bms/COACTVW.bms` | Account View | COACTVWC |
| 5 | COACTUP | `app/bms/COACTUP.bms` | Account Update | COACTUPC |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | Credit Card List | COCRDLIC |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | Credit Card Detail | COCRDSLC |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | Credit Card Update | COCRDUPC |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | Transaction List | COTRN00C |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | Transaction View | COTRN01C |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | Transaction Add | COTRN02C |
| 12 | CORPT00 | `app/bms/CORPT00.bms` | Transaction Report | CORPT00C |
| 13 | COBIL00 | `app/bms/COBIL00.bms` | Bill Payment | COBIL00C |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | User List (Admin) | COUSR00C |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | User Add (Admin) | COUSR01C |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | User Update (Admin) | COUSR02C |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | User Delete (Admin) | COUSR03C |

### 4.2 Optional Module BMS Maps

| # | Map Name | Source Path | Screen | Used By Program | Module |
|---|----------|-------------|--------|-----------------|--------|
| 18 | COPAU00 | `app/app-authorization-ims-db2-mq/bms/COPAU00.bms` | Auth Summary | COPAUS0C | IMS-DB2-MQ |
| 19 | COPAU01 | `app/app-authorization-ims-db2-mq/bms/COPAU01.bms` | Auth Details | COPAUS1C | IMS-DB2-MQ |
| 20 | COTRTLI | `app/app-transaction-type-db2/bms/COTRTLI.bms` | Tran Type List | COTRTLIC | DB2 Tran Type |
| 21 | COTRTUP | `app/app-transaction-type-db2/bms/COTRTUP.bms` | Tran Type Update | COTRTUPC | DB2 Tran Type |

---

## 5. Assembler Modules

| # | Module | Source Path | Function | Called By |
|---|--------|-------------|----------|-----------|
| 1 | MVSWAIT | `app/asm/MVSWAIT.asm` | Timer wait (SVC WAIT) | COBSWAIT |
| 2 | COBDATFT | `app/asm/COBDATFT.asm` | Date format conversion | CBACT01C |

**Macros:**

| # | Macro | Source Path | Used By |
|---|-------|-------------|---------|
| 1 | ASMWAIT | `app/maclib/ASMWAIT.mac` | MVSWAIT |
| 2 | COCDATFT | `app/maclib/COCDATFT.mac` | COBDATFT |

---

## 6. Supporting Assets

| # | Asset | Source Path | Type | Description |
|---|-------|-------------|------|-------------|
| 1 | CARDDEMO.CSD | `app/csd/CARDDEMO.CSD` | CICS CSD | CICS resource definitions (programs, transactions, files, mapsets) |
| 2 | CRDDEMO2.csd | `app/app-authorization-ims-db2-mq/csd/CRDDEMO2.csd` | CICS CSD | CSD for IMS-DB2-MQ module |
| 3 | CRDDEMOD.csd | `app/app-transaction-type-db2/csd/CRDDEMOD.csd` | CICS CSD | CSD for DB2 Transaction Type module |
| 4 | CRDDEMOM.csd | `app/app-vsam-mq/csd/CRDDEMOM.csd` | CICS CSD | CSD for MQ module |
| 5 | REPROCT.ctl | `app/ctl/REPROCT.ctl` | Control File | Report control parameters |
| 6 | REPROC.prc | `app/proc/REPROC.prc` | JCL Procedure | Reusable report procedure |
| 7 | TRANREPT.prc | `app/proc/TRANREPT.prc` | JCL Procedure | Transaction report procedure |
| 8 | CardDemo.ca7 | `app/scheduler/CardDemo.ca7` | CA-7 Schedule | Job scheduling definitions |
| 9 | CardDemo.controlm | `app/scheduler/CardDemo.controlm` | Control-M Schedule | Job scheduling definitions |
| 10 | LISTCAT.txt | `app/catlg/LISTCAT.txt` | Catalog Listing | VSAM catalog listing reference |
| 11 | AUTHFRDS.dcl | `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl` | DB2 DCL | Auth fraud table declaration |
| 12 | AUTHFRDS.ddl | `app/app-authorization-ims-db2-mq/ddl/AUTHFRDS.ddl` | DB2 DDL | Auth fraud table definition |
| 13 | XAUTHFRD.ddl | `app/app-authorization-ims-db2-mq/ddl/XAUTHFRD.ddl` | DB2 DDL | Auth fraud index |
| 14 | DCLTRCAT.dcl | `app/app-transaction-type-db2/dcl/DCLTRCAT.dcl` | DB2 DCL | Transaction category declaration |
| 15 | DCLTRTYP.dcl | `app/app-transaction-type-db2/dcl/DCLTRTYP.dcl` | DB2 DCL | Transaction type declaration |
| 16 | TRNTYCAT.ddl | `app/app-transaction-type-db2/ddl/TRNTYCAT.ddl` | DB2 DDL | Transaction category table |
| 17 | TRNTYPE.ddl | `app/app-transaction-type-db2/ddl/TRNTYPE.ddl` | DB2 DDL | Transaction type table |

---

## 7. Classification Summary

| Category | Count | Details |
|----------|------:|---------|
| **COBOL Programs** | **44** | 17 Online CICS + 11 Batch + 3 Utility + 13 Optional Module |
| **Copybooks** | **62** | 17 Data Record + 13 System/Common + 17 BMS-Generated + 15 Optional Module |
| **JCL Jobs** | **46** | 15 Core Batch + 23 Utility/Infrastructure + 8 Optional Module |
| **BMS Maps** | **21** | 17 Core + 4 Optional Module |
| **Assembler** | **2** | MVSWAIT, COBDATFT |
| **Macros** | **2** | ASMWAIT, COCDATFT |
| **JCL Procedures** | **2** | REPROC, TRANREPT |
| **CSD Definitions** | **4** | Base + 3 Optional Modules |
| **DB2 DDL/DCL** | **6** | 3 DDL + 3 DCL (Optional Modules) |
| **IMS DBDs/PSBs** | **7** | 4 DBDs + 3 PSBs (IMS-DB2-MQ module) |
| **Schedulers** | **2** | CA-7, Control-M |
| **Data Files** | **22** | 9 ASCII + 13 EBCDIC sample data |
| **Total Assets** | **~220** | |

### Technology Stack

| Technology | Usage |
|-----------|-------|
| COBOL | Primary language for all business logic |
| CICS | Online transaction processing (17 programs) |
| VSAM (KSDS + AIX) | Primary data storage for all domains |
| JCL | Batch job control |
| BMS | 3270 screen definitions |
| DB2 | Optional relational database (transaction types, fraud logging) |
| IMS DB | Optional hierarchical database (pending authorizations) |
| MQ | Optional message queuing (authorization requests, account/date inquiries) |
| SORT | Data file merge/sort operations |
| IDCAMS | VSAM cluster management |
| LE Services | CEEDAYS date validation |
| Assembler | Low-level utilities (timer, date formatting) |
