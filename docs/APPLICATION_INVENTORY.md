# CardDemo Application Inventory

> **Generated for**: Mainframe-to-Java Modernization Assessment
> **Application**: CardDemo - Credit Card Management System
> **Platform**: IBM z/OS | CICS/VSAM | COBOL | JCL

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [COBOL Programs (Optional Modules)](#cobol-programs-optional-modules)
4. [Copybooks (Data Structures)](#copybooks-data-structures)
5. [BMS Screen Maps](#bms-screen-maps)
6. [JCL Batch Jobs](#jcl-batch-jobs)
7. [Assembler Programs](#assembler-programs)
8. [JCL Procedures](#jcl-procedures)
9. [Supporting Artifacts](#supporting-artifacts)
10. [Asset Summary](#asset-summary)

---

## Executive Summary

CardDemo is a mainframe credit card management application designed as a reference for modernization workshops. It supports two user roles (Regular and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

| Artifact Type             | Count |
|---------------------------|-------|
| COBOL Programs (Core)     | 31    |
| COBOL Programs (Optional) | 13    |
| Copybooks (Core)          | 30    |
| Copybooks (Optional)      | 11    |
| BMS Screen Maps (Core)    | 17    |
| BMS Screen Maps (Optional)| 4     |
| BMS Copybooks (Core)      | 17    |
| JCL Jobs (Core)           | 38    |
| JCL Jobs (Optional)       | 8     |
| Assembler Programs        | 2     |
| JCL Procedures            | 2     |
| **Total Assets**          | **173** |

---

## COBOL Programs (Core)

### Online CICS Programs

| # | Program ID | File | Lines | Type | CICS Txn | Function | Business Domain |
|---|-----------|------|-------|------|----------|----------|-----------------|
| 1 | COSGN00C | COSGN00C.cbl | 260 | Online CICS | CC00 | Sign-on / Login screen | Security & Authentication |
| 2 | COMEN01C | COMEN01C.cbl | 308 | Online CICS | CM00 | Main menu navigation | Navigation |
| 3 | COADM01C | COADM01C.cbl | 288 | Online CICS | CA00 | Admin menu navigation | Administration |
| 4 | COACTVWC | COACTVWC.cbl | 941 | Online CICS | CA01 | View account details | Account Management |
| 5 | COACTUPC | COACTUPC.cbl | 4,236 | Online CICS | CA02 | Update account information | Account Management |
| 6 | COCRDLIC | COCRDLIC.cbl | 1,459 | Online CICS | CC01 | List credit cards for account | Card Management |
| 7 | COCRDSLC | COCRDSLC.cbl | 887 | Online CICS | CC02 | View credit card details | Card Management |
| 8 | COCRDUPC | COCRDUPC.cbl | 1,560 | Online CICS | CC03 | Update credit card information | Card Management |
| 9 | COTRN00C | COTRN00C.cbl | 699 | Online CICS | CT00 | List transactions | Transaction Management |
| 10 | COTRN01C | COTRN01C.cbl | 330 | Online CICS | CT01 | View transaction details | Transaction Management |
| 11 | COTRN02C | COTRN02C.cbl | 783 | Online CICS | CT02 | Add new transaction | Transaction Management |
| 12 | COBIL00C | COBIL00C.cbl | 572 | Online CICS | CB00 | Bill payment processing | Bill Payment |
| 13 | CORPT00C | CORPT00C.cbl | 649 | Online CICS | CR00 | Transaction report request | Reporting |
| 14 | COUSR00C | COUSR00C.cbl | 695 | Online CICS | CU00 | List all users (Admin) | User Administration |
| 15 | COUSR01C | COUSR01C.cbl | 299 | Online CICS | CU01 | Add new user (Admin) | User Administration |
| 16 | COUSR02C | COUSR02C.cbl | 414 | Online CICS | CU02 | Update user (Admin) | User Administration |
| 17 | COUSR03C | COUSR03C.cbl | 359 | Online CICS | CU03 | Delete user (Admin) | User Administration |

### Batch Programs

| # | Program ID | File | Lines | Type | Function | Business Domain |
|---|-----------|------|-------|------|----------|-----------------|
| 18 | CBACT01C | CBACT01C.cbl | 430 | Batch | Read/print account data with date formatting | Account Management |
| 19 | CBACT02C | CBACT02C.cbl | 178 | Batch | Read/print card data file | Card Management |
| 20 | CBACT03C | CBACT03C.cbl | 178 | Batch | Read/print card cross-reference file | Card Management |
| 21 | CBACT04C | CBACT04C.cbl | 652 | Batch | Calculate interest on accounts | Financial Processing |
| 22 | CBCUS01C | CBCUS01C.cbl | 178 | Batch | Read/print customer data file | Customer Management |
| 23 | CBTRN01C | CBTRN01C.cbl | 494 | Batch | Read daily transactions and post to master | Transaction Processing |
| 24 | CBTRN02C | CBTRN02C.cbl | 731 | Batch | Post transactions (core transaction posting) | Transaction Processing |
| 25 | CBTRN03C | CBTRN03C.cbl | 649 | Batch | Generate daily transaction report | Reporting |
| 26 | CBSTM03A | CBSTM03A.CBL | 924 | Batch | Generate account statements (text + HTML) | Statement Generation |
| 27 | CBSTM03B | CBSTM03B.CBL | 230 | Batch Subroutine | File I/O handler for statement generation | Statement Generation |
| 28 | CBEXPORT | CBEXPORT.cbl | 582 | Batch | Export all CardDemo data to flat file | Data Export/Import |
| 29 | CBIMPORT | CBIMPORT.cbl | 487 | Batch | Import data from flat file into CardDemo | Data Export/Import |

### Utility Programs

| # | Program ID | File | Lines | Type | Function | Business Domain |
|---|-----------|------|-------|------|----------|-----------------|
| 30 | CSUTLDTC | CSUTLDTC.cbl | 157 | Subroutine | Date conversion utility (calls CEEDAYS) | Shared Utility |
| 31 | COBSWAIT | COBSWAIT.cbl | 41 | Batch Utility | Wait/delay utility (calls MVSWAIT) | Shared Utility |

**Core COBOL Total**: 31 programs | 17,862 lines of code

---

## COBOL Programs (Optional Modules)

### Authorization Module (IMS/DB2/MQ)

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 1 | COPAUA0C | COPAUA0C.cbl | - | MQ Trigger | MQ trigger for payment authorization |
| 2 | COPAUS0C | COPAUS0C.cbl | - | Online CICS | Payment authorization summary screen |
| 3 | COPAUS1C | COPAUS1C.cbl | - | Online CICS | Payment authorization detail screen |
| 4 | COPAUS2C | COPAUS2C.cbl | - | Online CICS | Fraud marking to DB2 |
| 5 | CBPAUP0C | CBPAUP0C.cbl | - | Batch | Batch purge of authorization records |
| 6 | DBUNLDGS | DBUNLDGS.CBL | - | Batch | Unload GSAM database |
| 7 | PAUDBLOD | PAUDBLOD.CBL | - | Batch | Load payment authorization DB |
| 8 | PAUDBUNL | PAUDBUNL.CBL | - | Batch | Unload payment authorization DB |

### Transaction Type DB2 Module

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 9 | COTRTUPC | COTRTUPC.cbl | - | Online CICS | Add/edit transaction types (DB2 CRUD) |
| 10 | COTRTLIC | COTRTLIC.cbl | - | Online CICS | List/delete transaction types (DB2 CRUD) |
| 11 | COBTUPDT | COBTUPDT.cbl | - | Batch | Batch update transaction types from DB2 |

### VSAM-MQ Module

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 12 | CODATE01 | CODATE01.cbl | - | MQ Server | System date service via MQ |
| 13 | COACCT01 | COACCT01.cbl | - | MQ Server | Account inquiry via MQ request/response |

---

## Copybooks (Data Structures)

### Core Copybooks

| # | Copybook | File | Lines | Record Length | Category | Description |
|---|----------|------|-------|--------------|----------|-------------|
| 1 | CVACT01Y | CVACT01Y.cpy | 20 | 300 bytes | Account | Account master record layout |
| 2 | CVACT02Y | CVACT02Y.cpy | 14 | 150 bytes | Card | Card data record layout |
| 3 | CVACT03Y | CVACT03Y.cpy | 11 | - | Card | Card-to-account cross-reference |
| 4 | CVCRD01Y | CVCRD01Y.cpy | 46 | - | Card | Card detail record with status fields |
| 5 | CVCUS01Y | CVCUS01Y.cpy | 26 | 500 bytes | Customer | Customer master record layout |
| 6 | CUSTREC | CUSTREC.cpy | 26 | - | Customer | Customer record (alternate layout) |
| 7 | CSUSR01Y | CSUSR01Y.cpy | 26 | 80 bytes | Security | User security record (ID, password, type) |
| 8 | CVTRA01Y | CVTRA01Y.cpy | 13 | 50 bytes | Transaction | Transaction category balance record |
| 9 | CVTRA02Y | CVTRA02Y.cpy | 13 | 50 bytes | Transaction | Disclosure group record |
| 10 | CVTRA03Y | CVTRA03Y.cpy | 10 | 60 bytes | Transaction | Transaction type record |
| 11 | CVTRA04Y | CVTRA04Y.cpy | 12 | 60 bytes | Transaction | Transaction category type record |
| 12 | CVTRA05Y | CVTRA05Y.cpy | 21 | 350 bytes | Transaction | Transaction master record |
| 13 | CVTRA06Y | CVTRA06Y.cpy | 21 | 350 bytes | Transaction | Daily transaction record |
| 14 | CVTRA07Y | CVTRA07Y.cpy | 73 | - | Reporting | Transaction report layout structures |
| 15 | COSTM01 | COSTM01.CPY | 38 | - | Reporting | Transaction layout for statement reporting |
| 16 | CVEXPORT | CVEXPORT.cpy | 103 | - | Data I/O | Export/import record structures |
| 17 | COCOM01Y | COCOM01Y.cpy | 47 | - | Common | Common communication area (COMMAREA) |
| 18 | COMEN02Y | COMEN02Y.cpy | 101 | - | Navigation | Menu definition structures |
| 19 | COADM02Y | COADM02Y.cpy | 62 | - | Navigation | Admin menu definition structures |
| 20 | COTTL01Y | COTTL01Y.cpy | 27 | - | UI | Screen title/header layout |
| 21 | CSDAT01Y | CSDAT01Y.cpy | 58 | - | Common | Date handling structures |
| 22 | CSMSG01Y | CSMSG01Y.cpy | 24 | - | Common | Message area layout (short) |
| 23 | CSMSG02Y | CSMSG02Y.cpy | 35 | - | Common | Message area layout (extended) |
| 24 | CSSETATY | CSSETATY.cpy | 30 | - | UI | Screen field attribute setting |
| 25 | CSSTRPFY | CSSTRPFY.cpy | 85 | - | Utility | String processing functions |
| 26 | CSUTLDPY | CSUTLDPY.cpy | 375 | - | Utility | Date utility parameters |
| 27 | CSUTLDWY | CSUTLDWY.cpy | 89 | - | Utility | Date utility working storage |
| 28 | CSLKPCDY | CSLKPCDY.cpy | 1,318 | - | Reference | Lookup code tables (country, state codes) |
| 29 | CODATECN | CODATECN.cpy | 52 | - | Utility | Date conversion record |
| 30 | UNUSED1Y | UNUSED1Y.cpy | 10 | 80 bytes | Deprecated | Unused record layout |

### BMS-Generated Copybooks

| # | Copybook | Maps For | Screen |
|---|----------|----------|--------|
| 1 | COSGN00.CPY | COSGN00.bms | Login Screen |
| 2 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COACTVW.CPY | COACTVW.bms | Account View |
| 5 | COACTUP.CPY | COACTUP.bms | Account Update |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card View |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 12 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 13 | CORPT00.CPY | CORPT00.bms | Transaction Report |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | Add User |
| 16 | COUSR02.CPY | COUSR02.bms | Update User |
| 17 | COUSR03.CPY | COUSR03.bms | Delete User |

### Optional Module Copybooks

| # | Copybook | Module | Description |
|---|----------|--------|-------------|
| 1 | CCPAUERY.cpy | Auth IMS/DB2/MQ | Payment authorization error reply |
| 2 | CCPAURLY.cpy | Auth IMS/DB2/MQ | Payment authorization reply |
| 3 | CCPAURQY.cpy | Auth IMS/DB2/MQ | Payment authorization request |
| 4 | CIPAUDTY.cpy | Auth IMS/DB2/MQ | Payment authorization detail |
| 5 | CIPAUSMY.cpy | Auth IMS/DB2/MQ | Payment authorization summary |
| 6 | IMSFUNCS.cpy | Auth IMS/DB2/MQ | IMS function codes |
| 7 | PADFLPCB.CPY | Auth IMS/DB2/MQ | IMS PCB for detail file |
| 8 | PASFLPCB.CPY | Auth IMS/DB2/MQ | IMS PCB for summary file |
| 9 | PAUTBPCB.CPY | Auth IMS/DB2/MQ | IMS PCB for auth table |
| 10 | CSDB2RPY.cpy | Tran Type DB2 | DB2 reply copybook |
| 11 | CSDB2RWY.cpy | Tran Type DB2 | DB2 working storage copybook |

---

## BMS Screen Maps

### Core BMS Maps

| # | Map Set | File | Screen Title | Associated Program | Function |
|---|---------|------|-------------|-------------------|----------|
| 1 | COSGN00 | COSGN00.bms | Login Screen | COSGN00C | User authentication |
| 2 | COMEN01 | COMEN01.bms | Main Menu | COMEN01C | Navigation hub |
| 3 | COADM01 | COADM01.bms | Admin Menu | COADM01C | Admin navigation |
| 4 | COACTVW | COACTVW.bms | Account View | COACTVWC | Display account details |
| 5 | COACTUP | COACTUP.bms | Account Update | COACTUPC | Edit account fields |
| 6 | COCRDLI | COCRDLI.bms | Card Listing | COCRDLIC | Paginated card list |
| 7 | COCRDSL | COCRDSL.bms | Card Selection | COCRDSLC | Card detail display |
| 8 | COCRDUP | COCRDUP.bms | Card Update | COCRDUPC | Edit card fields |
| 9 | COTRN00 | COTRN00.bms | Transaction List | COTRN00C | Paginated transaction list |
| 10 | COTRN01 | COTRN01.bms | Transaction View | COTRN01C | Transaction detail display |
| 11 | COTRN02 | COTRN02.bms | Transaction Add | COTRN02C | New transaction form |
| 12 | COBIL00 | COBIL00.bms | Bill Payment | COBIL00C | Payment entry form |
| 13 | CORPT00 | CORPT00.bms | Transaction Report | CORPT00C | Report criteria input |
| 14 | COUSR00 | COUSR00.bms | List Users | COUSR00C | User listing display |
| 15 | COUSR01 | COUSR01.bms | Add User | COUSR01C | New user form |
| 16 | COUSR02 | COUSR02.bms | Update User | COUSR02C | Edit user form |
| 17 | COUSR03 | COUSR03.bms | Delete User | COUSR03C | User deletion confirm |

### Optional Module BMS Maps

| # | Map Set | Module | Function |
|---|---------|--------|----------|
| 1 | COPAU00 | Auth IMS/DB2/MQ | Authorization summary screen |
| 2 | COPAU01 | Auth IMS/DB2/MQ | Authorization detail screen |
| 3 | COTRTLI | Tran Type DB2 | Transaction type list |
| 4 | COTRTUP | Tran Type DB2 | Transaction type update |

---

## JCL Batch Jobs

### Data File Management

| # | Job Name | File | Function | VSAM File Managed | Classification |
|---|----------|------|----------|-------------------|----------------|
| 1 | ACCTFILE | ACCTFILE.jcl | Refresh account master VSAM | ACCTDATA.VSAM.KSDS | Data Refresh |
| 2 | CARDFILE | CARDFILE.jcl | Refresh card master VSAM | CARDDATA.VSAM.KSDS | Data Refresh |
| 3 | CUSTFILE | CUSTFILE.jcl | Refresh customer master VSAM | CUSTDATA.VSAM.KSDS | Data Refresh |
| 4 | TRANFILE | TRANFILE.jcl | Refresh transaction master VSAM | TRANSACT.VSAM.KSDS | Data Refresh |
| 5 | XREFFILE | XREFFILE.jcl | Refresh card cross-reference VSAM | CARDXREF.VSAM.KSDS | Data Refresh |
| 6 | DUSRSECJ | DUSRSECJ.jcl | Load user security VSAM | USRSEC.VSAM.KSDS | Data Refresh |
| 7 | TRANTYPE | TRANTYPE.jcl | Define/load transaction types | TRANTYPE.VSAM.KSDS | Data Refresh |
| 8 | TRANCATG | TRANCATG.jcl | Define/load transaction categories | TRANCATG.VSAM.KSDS | Data Refresh |
| 9 | TCATBALF | TCATBALF.jcl | Define/load category balance file | TCATBAL.VSAM.KSDS | Data Refresh |
| 10 | DISCGRP | DISCGRP.jcl | Define/load disclosure groups | DISCGRP.VSAM.KSDS | Data Refresh |
| 11 | REPTFILE | REPTFILE.jcl | Define reporting files | Reporting files | Data Refresh |
| 12 | DALYREJS | DALYREJS.jcl | Define daily rejects file | DALYREJS.VSAM.KSDS | Data Refresh |
| 13 | DEFCUST | DEFCUST.jcl | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS | Data Definition |
| 14 | ESDSRRDS | ESDSRRDS.jcl | Define ESDS and RRDS datasets | ESDS/RRDS variants | Data Definition |

### Batch Processing

| # | Job Name | File | Program(s) Executed | Function | Classification |
|---|----------|------|---------------------|----------|----------------|
| 15 | POSTTRAN | POSTTRAN.jcl | CBTRN02C | Post daily transactions to master | Core Processing |
| 16 | INTCALC | INTCALC.jcl | CBACT04C | Calculate interest on accounts | Core Processing |
| 17 | COMBTRAN | COMBTRAN.jcl | SORT + IDCAMS | Combine/merge transaction files | Core Processing |
| 18 | CREASTMT | CREASTMT.JCL | SORT + CBSTM03A | Generate account statements | Core Processing |
| 19 | TRANREPT | TRANREPT.jcl | SORT + CBTRN03C | Generate transaction reports | Reporting |
| 20 | TRANBKP | TRANBKP.jcl | IDCAMS REPRO | Backup transaction data | Backup |
| 21 | TRANIDX | TRANIDX.jcl | IDCAMS | Define alternate index on transactions | Index Management |
| 22 | PRTCATBL | PRTCATBL.jcl | SORT | Print category balance report | Reporting |

### CICS File Control

| # | Job Name | File | Function | Classification |
|---|----------|------|----------|----------------|
| 23 | CLOSEFIL | CLOSEFIL.jcl | Close CICS files for batch window | CICS Control |
| 24 | OPENFIL | OPENFIL.jcl | Reopen CICS files after batch | CICS Control |

### Data Read/Print Utilities

| # | Job Name | File | Program Executed | Function | Classification |
|---|----------|------|-----------------|----------|----------------|
| 25 | READACCT | READACCT.jcl | CBACT01C | Read and print account data | Utility |
| 26 | READCARD | READCARD.jcl | CBACT02C | Read and print card data | Utility |
| 27 | READCUST | READCUST.jcl | CBCUS01C | Read and print customer data | Utility |
| 28 | READXREF | READXREF.jcl | CBACT03C | Read and print cross-reference data | Utility |

### Data Export/Import

| # | Job Name | File | Program Executed | Function | Classification |
|---|----------|------|-----------------|----------|----------------|
| 29 | CBEXPORT | CBEXPORT.jcl | CBEXPORT | Export all data to flat file | Data Migration |
| 30 | CBIMPORT | CBIMPORT.jcl | CBIMPORT | Import data from flat file | Data Migration |

### GDG and Infrastructure

| # | Job Name | File | Function | Classification |
|---|----------|------|----------|----------------|
| 31 | DEFGDGB | DEFGDGB.jcl | Define GDG base entries | Infrastructure |
| 32 | DEFGDGD | DEFGDGD.jcl | Define GDG datasets and backup | Infrastructure |
| 33 | CBADMCDJ | CBADMCDJ.jcl | Load CICS CSD definitions | Infrastructure |
| 34 | WAITSTEP | WAITSTEP.jcl | Wait/delay job step | Infrastructure |
| 35 | TXT2PDF1 | TXT2PDF1.JCL | Convert text statements to PDF | Utility |
| 36 | FTPJCL | FTPJCL.JCL | FTP file transfer job | Utility |
| 37 | INTRDRJ1 | INTRDRJ1.JCL | Internal reader job chain (step 1) | Infrastructure |
| 38 | INTRDRJ2 | INTRDRJ2.JCL | Internal reader job chain (step 2) | Infrastructure |

### Optional Module JCL

| # | Job Name | Module | Function |
|---|----------|--------|----------|
| 1 | CBPAUP0J | Auth IMS/DB2/MQ | Batch purge authorization records |
| 2 | DBPAUTP0 | Auth IMS/DB2/MQ | Define authorization DB |
| 3 | LOADPADB | Auth IMS/DB2/MQ | Load payment auth database |
| 4 | UNLDGSAM | Auth IMS/DB2/MQ | Unload GSAM data |
| 5 | UNLDPADB | Auth IMS/DB2/MQ | Unload payment auth DB |
| 6 | CREADB21 | Tran Type DB2 | Create DB2 tables |
| 7 | MNTTRDB2 | Tran Type DB2 | Maintain transaction type DB2 |
| 8 | TRANEXTR | Tran Type DB2 | Extract transaction types from DB2 |

---

## Assembler Programs

| # | Program | File | Function |
|---|---------|------|----------|
| 1 | MVSWAIT | MVSWAIT.asm | Wait/delay system call (centiseconds) |
| 2 | COBDATFT | COBDATFT.asm | Date formatting routine for COBOL |

---

## JCL Procedures

| # | Procedure | File | Function |
|---|-----------|------|----------|
| 1 | REPROC | REPROC.prc | Reprocessing procedure (VSAM unload) |
| 2 | TRANREPT | TRANREPT.prc | Transaction report procedure |

---

## Supporting Artifacts

### Scheduler Configurations
- **CardDemo.ca7** - CA-7 scheduler job definitions
- **CardDemo.controlm** - Control-M scheduler definitions

### CSD Definitions
- **CARDDEMO.CSD** - CICS System Definition file (transaction, program, and file definitions)

### Data Files (ASCII)
Sample data for testing modernized applications in `app/data/ASCII/`.

### Data Files (EBCDIC)
Production-format data for mainframe upload in `app/data/EBCDIC/`.

### Shell Scripts
Located in `scripts/` - FTP-based mainframe interaction scripts for compile, submit, and data refresh.

---

## Asset Summary

### By Business Domain

| Business Domain | Online Programs | Batch Programs | Copybooks | BMS Maps | JCL Jobs |
|----------------|----------------|----------------|-----------|----------|----------|
| Security & Auth | 1 | 0 | 1 | 1 | 1 |
| Navigation | 2 | 0 | 2 | 2 | 0 |
| Account Mgmt | 2 | 1 | 1 | 2 | 1 |
| Card Mgmt | 3 | 2 | 3 | 3 | 1 |
| Customer Mgmt | 0 | 1 | 2 | 0 | 1 |
| Transactions | 3 | 2 | 6 | 3 | 5 |
| Bill Payment | 1 | 0 | 0 | 1 | 0 |
| Reporting | 1 | 2 | 2 | 1 | 2 |
| User Admin | 4 | 0 | 0 | 4 | 0 |
| Data Export/Import | 0 | 2 | 1 | 0 | 2 |
| Shared/Utility | 0 | 2 | 8 | 0 | 8 |
| Infrastructure | 0 | 0 | 0 | 0 | 10+ |

### By Technology Stack

| Technology | Components |
|-----------|------------|
| COBOL/CICS | 17 online programs, 17 BMS maps |
| COBOL/Batch | 14 batch programs |
| JCL/IDCAMS | 38 batch jobs |
| VSAM KSDS | 10+ VSAM clusters |
| Assembler | 2 utility routines |
| IMS DB (Optional) | 3 programs |
| DB2 (Optional) | 3 programs |
| MQ (Optional) | 2 programs |

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| CO* | Online CICS program | COSGN00C (Sign-on) |
| CB* | Batch COBOL program | CBTRN02C (Transaction posting) |
| CS* | Shared service/utility | CSUTLDTC (Date utility) |
| CV* | VSAM data copybook | CVACT01Y (Account record) |
| CO*Y | Common area copybook | COCOM01Y (COMMAREA) |
| CS*Y | Shared utility copybook | CSDAT01Y (Date structures) |
