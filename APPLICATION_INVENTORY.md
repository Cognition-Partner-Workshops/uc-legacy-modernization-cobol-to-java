# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo (Credit Card Management System)
> **Platform**: IBM Mainframe (COBOL / CICS / VSAM / JCL / BMS)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs — Core Online (CICS)](#cobol-programs--core-online-cics)
3. [COBOL Programs — Core Batch](#cobol-programs--core-batch)
4. [COBOL Programs — Optional Modules](#cobol-programs--optional-modules)
5. [Copybooks — Data Structure](#copybooks--data-structure)
6. [Copybooks — Common/Utility](#copybooks--commonutility)
7. [BMS Screen Maps](#bms-screen-maps)
8. [JCL Batch Jobs](#jcl-batch-jobs)
9. [Assembler Programs](#assembler-programs)
10. [JCL Procedures](#jcl-procedures)
11. [Scheduler Definitions](#scheduler-definitions)
12. [Summary Counts](#summary-counts)

---

## Executive Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It simulates account management, card management, transactions, bill payments, and reporting through a 3270 terminal UI (CICS/BMS) with batch processing for end-of-day operations.

**Two user roles**: Regular User (card operations) and Admin (user/transaction type management).

---

## COBOL Programs -- Core Online (CICS)

| # | Program | Lines | Tran ID | Function | Domain | VSAM Files Accessed | Classification |
|---|---------|-------|---------|----------|--------|---------------------|----------------|
| 1 | COSGN00C | 261 | CC00 | User Sign-on / Authentication | Security | USRSEC | Entry Point |
| 2 | COMEN01C | 309 | CM00 | Main Menu — Regular Users | Navigation | — | Navigation Hub |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation | — | Navigation Hub |
| 4 | COACTVWC | 942 | CAVW | Account View (read-only) | Account Mgmt | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT | Inquiry |
| 5 | COACTUPC | 4,237 | CAUP | Account Update | Account Mgmt | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT | CRUD — Update |
| 6 | COCRDLIC | 1,460 | CCLI | Credit Card List (paginated browse) | Card Mgmt | CARDDAT, CARDAIX | List / Browse |
| 7 | COCRDSLC | 888 | CCDL | Credit Card Detail View | Card Mgmt | CARDDAT, CARDAIX | Inquiry |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | CARDDAT | CRUD — Update |
| 9 | COTRN00C | 699 | CT00 | Transaction List (paginated browse) | Transactions | TRANSACT | List / Browse |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transactions | TRANSACT | Inquiry |
| 11 | COTRN02C | 783 | CT02 | Transaction Add (new transaction) | Transactions | TRANSACT, CXACAIX, CARDXREF | CRUD — Create |
| 12 | CORPT00C | 649 | CR00 | Transaction Report (submit batch) | Reporting | — | Report Trigger |
| 13 | COBIL00C | 572 | CB00 | Bill Payment (pay balance) | Billing | ACCTDAT, CXACAIX, TRANSACT | Financial |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | User Admin | USRSEC | List / Browse |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | User Admin | USRSEC | CRUD — Create |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | User Admin | USRSEC | CRUD — Update |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | User Admin | USRSEC | CRUD — Delete |

**Subtotal**: 17 online CICS programs | 14,745 lines

---

## COBOL Programs -- Core Batch

| # | Program | Lines | Function | Domain | Files Read | Files Written | Classification |
|---|---------|-------|----------|--------|------------|---------------|----------------|
| 18 | CBACT01C | 430 | Read account file, write to output files | Account Mgmt | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE | Data Extract |
| 19 | CBACT02C | 178 | Read and print card data | Card Mgmt | CARDFILE | SYSOUT | Data Dump |
| 20 | CBACT03C | 178 | Read and print cross-reference data | Card Mgmt | XREFFILE | SYSOUT | Data Dump |
| 21 | CBACT04C | 652 | Interest calculation | Financial | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | TRANSACT | Core Batch |
| 22 | CBCUS01C | 178 | Read and print customer data | Customer Mgmt | CUSTFILE | SYSOUT | Data Dump |
| 23 | CBTRN01C | 494 | Post daily transactions (variant 1) | Transactions | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE | Core Batch |
| 24 | CBTRN02C | 731 | Post daily transactions (variant 2) | Transactions | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF | Core Batch |
| 25 | CBTRN03C | 649 | Print transaction detail report | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT | Report Gen |
| 26 | CBSTM03A | 924 | Statement generation (main) | Statements | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE | Report Gen |
| 27 | CBSTM03B | 230 | Statement generation (subroutine) | Statements | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — | Subroutine |
| 28 | CBEXPORT | 582 | Export all data to flat file | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | ETL |
| 29 | CBIMPORT | 487 | Import data from flat file | Data Migration | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | ETL |
| 30 | COBSWAIT | 41 | Wait utility (centiseconds) | Utility | — | — | Utility |
| 31 | CSUTLDTC | 157 | Date validation utility | Utility | — | — | Utility |

**Subtotal**: 14 batch programs | 5,911 lines

---

## COBOL Programs -- Optional Modules

### Authorization Module (IMS / DB2 / MQ)

| # | Program | Lines | Function | Technology | Classification |
|---|---------|-------|----------|------------|----------------|
| 32 | COPAUA0C | 1,026 | Card authorization decision via MQ | CICS + MQ | Core — Auth |
| 33 | COPAUS0C | 1,032 | Authorization summary view (BMS screen) | CICS + IMS | Inquiry |
| 34 | COPAUS1C | 604 | Authorization detail view | CICS + IMS | Inquiry |
| 35 | COPAUS2C | 244 | Mark authorization as fraud (DB2 update) | CICS + DB2 | CRUD — Update |
| 36 | CBPAUP0C | 386 | Purge expired authorization messages | Batch + IMS | Maintenance |
| 37 | DBUNLDGS | 366 | Unload IMS DB to sequential file | Batch + IMS | ETL |
| 38 | PAUDBLOD | 369 | Load sequential file into IMS DB | Batch + IMS | ETL |
| 39 | PAUDBUNL | 317 | Unload IMS DB (alternate) | Batch + IMS | ETL |

**Subtotal**: 8 programs | 4,344 lines

### Transaction Type Module (DB2)

| # | Program | Lines | Function | Technology | Classification |
|---|---------|-------|----------|------------|----------------|
| 40 | COTRTLIC | 2,098 | List/delete transaction types | CICS + DB2 | List / CRUD |
| 41 | COTRTUPC | 1,702 | Add/edit transaction types | CICS + DB2 | CRUD |
| 42 | COBTUPDT | 237 | Batch update transaction types | Batch + DB2 | Batch CRUD |

**Subtotal**: 3 programs | 4,037 lines

### VSAM-MQ Module

| # | Program | Lines | Function | Technology | Classification |
|---|---------|-------|----------|------------|----------------|
| 43 | COACCT01 | 620 | Account inquiry via MQ request/response | Batch + MQ | MQ Service |
| 44 | CODATE01 | 524 | System date request via MQ | Batch + MQ | MQ Service |

**Subtotal**: 2 programs | 1,144 lines

---

## Copybooks -- Data Structure

These define the record layouts for business entities (VSAM files, working storage).

| # | Copybook | Record Size | Description | Business Entity | Used By (count) |
|---|----------|-------------|-------------|-----------------|-----------------|
| 1 | CVACT01Y | 300 bytes | Account master record | Account | 16 |
| 2 | CVACT02Y | 150 bytes | Card data record | Credit Card | 10 |
| 3 | CVACT03Y | ~36 bytes | Card-to-account cross-reference | Card-Account XREF | 16 |
| 4 | CVCUS01Y | 500 bytes | Customer master record | Customer | 10 |
| 5 | CVTRA05Y | 350 bytes | Online transaction record | Transaction | 11 |
| 6 | CVTRA06Y | 350 bytes | Daily transaction record (batch input) | Daily Transaction | 2 |
| 7 | CVTRA01Y | 50 bytes | Transaction category balance | Category Balance | 2 |
| 8 | CVTRA02Y | 50 bytes | Disclosure group (interest rates) | Disclosure Group | 1 |
| 9 | CVTRA03Y | 60 bytes | Transaction type master | Transaction Type | 1 |
| 10 | CVTRA04Y | 60 bytes | Transaction category type | Transaction Category | 1 |
| 11 | CVTRA07Y | N/A | Transaction report layout (headers, totals) | Report Structure | 1 |
| 12 | CSUSR01Y | ~80 bytes | User security record | User / Security | 14 |
| 13 | CVCRD01Y | N/A | Card work area (common card variables) | Card (working) | 7 |
| 14 | CUSTREC | N/A | Customer record (alternate layout) | Customer | 1 |
| 15 | CVEXPORT | N/A | Export record layout | Export Record | 2 |
| 16 | COSTM01 | N/A | Transaction altered layout for statements | Statement Record | 1 |
| 17 | UNUSED1Y | ~80 bytes | Unused/placeholder data structure | — | 0 |

### Optional Module Copybooks

| # | Copybook | Module | Description |
|---|----------|--------|-------------|
| 18 | CIPAUSMY | Auth (IMS) | Authorization summary message |
| 19 | CIPAUDTY | Auth (IMS) | Authorization detail message |
| 20 | CCPAURQY | Auth (MQ) | Authorization MQ request |
| 21 | CCPAURLY | Auth (MQ) | Authorization MQ reply |
| 22 | CCPAUERY | Auth (MQ) | Authorization MQ error |
| 23 | IMSFUNCS | Auth (IMS) | IMS DL/I function codes |
| 24 | PAUTBPCB | Auth (IMS) | IMS PCB — Auth DB |
| 25 | PASFLPCB | Auth (IMS) | IMS PCB — Secondary |
| 26 | PADFLPCB | Auth (IMS) | IMS PCB — Detail |
| 27 | CSDB2RPY | TranType (DB2) | DB2 common procedures |
| 28 | CSDB2RWY | TranType (DB2) | DB2 common working storage |

---

## Copybooks -- Common/Utility

| # | Copybook | Description | Used By (count) |
|---|----------|-------------|-----------------|
| 1 | COCOM01Y | Common COMMAREA (inter-program communication) | 21 |
| 2 | COTTL01Y | Screen title/header constants | 21 |
| 3 | CSDAT01Y | Current date/time formatting variables | 21 |
| 4 | CSMSG01Y | Common message definitions | 21 |
| 5 | CSMSG02Y | Abend handling variables | 8 |
| 6 | COMEN02Y | Menu option definitions (programs, labels, access levels) | 1 |
| 7 | COADM02Y | Admin menu option definitions | 1 |
| 8 | CSSTRPFY | String strip/format utility | 7 |
| 9 | CSSETATY | Set attribute utility | 2 |
| 10 | CSUTLDWY | Date edit working storage (CCYYMMDD validation) | 2 |
| 11 | CSUTLDPY | Date utility parameters | 1 |
| 12 | CODATECN | Date conversion record (assembler interface) | 1 |
| 13 | CSLKPCDY | Lookup code utility | 1 |

---

## BMS Screen Maps

### Core Application Maps

| # | BMS Map | Screen Name | Associated Program | Function |
|---|---------|-------------|--------------------|----------|
| 1 | COSGN00 | Login Screen | COSGN00C | User sign-on |
| 2 | COMEN01 | Main Menu | COMEN01C | Regular user navigation |
| 3 | COADM01 | Admin Menu | COADM01C | Admin user navigation |
| 4 | COACTVW | Account View | COACTVWC | Display account details |
| 5 | COACTUP | Account Update | COACTUPC | Edit account fields |
| 6 | COCRDLI | Card List | COCRDLIC | Paginated card browse |
| 7 | COCRDSL | Card Detail | COCRDSLC | Display card details |
| 8 | COCRDUP | Card Update | COCRDUPC | Edit card fields |
| 9 | COTRN00 | Transaction List | COTRN00C | Paginated transaction browse |
| 10 | COTRN01 | Transaction View | COTRN01C | Display transaction details |
| 11 | COTRN02 | Transaction Add | COTRN02C | Enter new transaction |
| 12 | CORPT00 | Report Request | CORPT00C | Request transaction reports |
| 13 | COBIL00 | Bill Payment | COBIL00C | Pay account balance |
| 14 | COUSR00 | User List | COUSR00C | List users (Admin) |
| 15 | COUSR01 | Add User | COUSR01C | Add user (Admin) |
| 16 | COUSR02 | Update User | COUSR02C | Update user (Admin) |
| 17 | COUSR03 | Delete User | COUSR03C | Delete user (Admin) |

### Optional Module Maps

| # | BMS Map | Module | Associated Program | Function |
|---|---------|--------|--------------------|----------|
| 18 | COPAU00 | Auth (IMS) | COPAUS0C | Authorization summary |
| 19 | COPAU01 | Auth (IMS) | COPAUS1C | Authorization detail |
| 20 | COTRTLI | TranType (DB2) | COTRTLIC | Transaction type list |
| 21 | COTRTUP | TranType (DB2) | COTRTUPC | Transaction type update |

**Total**: 17 core + 4 optional = **21 BMS maps**

Each BMS map has a corresponding generated copybook in `app/cpy-bms/` (17 core) and the optional module `cpy-bms/` directories (4 optional).

---

## JCL Batch Jobs

### Data Loading / Refresh Jobs

| # | JCL Job | Function | Programs Executed | Datasets |
|---|---------|----------|-------------------|----------|
| 1 | ACCTFILE | Refresh account master VSAM | IDCAMS | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | Refresh card master VSAM | IDCAMS | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | Refresh customer master VSAM | IDCAMS | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | Load card cross-reference VSAM + alt indexes | IDCAMS | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | Load transaction master VSAM + alt indexes | IDCAMS, SDSF | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | Load user security VSAM | IEBGENER, IDCAMS | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | Load transaction type VSAM | IDCAMS | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | Load transaction category VSAM | IDCAMS | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | Load transaction category balance VSAM | IDCAMS | TCATBAL.VSAM.KSDS |
| 10 | DISCGRP | Load disclosure group VSAM | IDCAMS | DISCGRP.VSAM.KSDS |

### Core Batch Processing Jobs

| # | JCL Job | Function | Programs Executed | Input Files | Output Files |
|---|---------|----------|-------------------|-------------|--------------|
| 11 | POSTTRAN | Post daily transactions | CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF |
| 12 | INTCALC | Calculate interest | CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | TRANSACT |
| 13 | CREASTMT | Generate statements (sort + process) | SORT, IDCAMS, CBSTM03A | TRANSACT.VSAM, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE |
| 14 | TRANREPT | Transaction detail report | SORT, REPROC, CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG | TRANREPT |
| 15 | COMBTRAN | Combine transactions (backup cycle) | IDCAMS | TRANSACT | Combined output |
| 16 | TRANBKP | Backup transactions | REPROC, IDCAMS | TRANSACT | Backup GDG |
| 17 | TRANIDX | Define/rebuild alternate indexes | IDCAMS | TRANSACT | Alt indexes |

### Utility / Infrastructure Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 18 | CLOSEFIL | Close CICS files for batch | SDSF |
| 19 | OPENFIL | Open CICS files after batch | SDSF |
| 20 | WAITSTEP | Wait utility step | COBSWAIT |
| 21 | DEFGDGB | Define GDG bases | IDCAMS |
| 22 | DEFGDGD | Define GDG + load reference data | IDCAMS, IEBGENER |
| 23 | DEFCUST | Define customer VSAM clusters | IDCAMS |
| 24 | ESDSRRDS | Create ESDS/RRDS test VSAM files | IEFBR14, IEBGENER, IDCAMS |
| 25 | DALYREJS | Define daily rejects VSAM | IDCAMS |
| 26 | REPTFILE | Define report VSAM | IDCAMS |
| 27 | PRTCATBL | Print category balance (sorted) | REPROC, SORT |

### Data Read / Dump Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 28 | READACCT | Read and dump account data | CBACT01C |
| 29 | READCARD | Read and dump card data | CBACT02C |
| 30 | READCUST | Read and dump customer data | CBCUS01C |
| 31 | READXREF | Read and dump cross-reference data | CBACT03C |

### Export / Import Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 32 | CBEXPORT | Export all data to flat file | CBEXPORT |
| 33 | CBIMPORT | Import data from flat file | CBIMPORT |

### Admin / Miscellaneous Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 34 | CBADMCDJ | Admin card demo job | — |
| 35 | FTPJCL | FTP utility job | FTP |
| 36 | INTRDRJ1 | Internal reader job 1 (chain to J2) | IDCAMS, IEBGENER |
| 37 | INTRDRJ2 | Internal reader job 2 | IDCAMS |
| 38 | TXT2PDF1 | Convert text statements to PDF | IKJEFT1B (TXT2PDF REXX) |

### Optional Module JCL Jobs

| # | JCL Job | Module | Function |
|---|---------|--------|----------|
| 39 | CBPAUP0J | Auth (IMS) | Purge expired auth messages |
| 40 | DBPAUTP0 | Auth (IMS) | Process auth DB |
| 41 | LOADPADB | Auth (IMS) | Load auth IMS DB |
| 42 | UNLDGSAM | Auth (IMS) | Unload to GSAM |
| 43 | UNLDPADB | Auth (IMS) | Unload auth IMS DB |
| 44 | CREADB21 | TranType (DB2) | Create DB2 tables |
| 45 | MNTTRDB2 | TranType (DB2) | Maintain transaction types in DB2 |
| 46 | TRANEXTR | TranType (DB2) | Extract transaction types |

**Total**: 38 core + 8 optional = **46 JCL jobs**

---

## Assembler Programs

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | MVSWAIT | app/asm/ | Wait routine (called by COBSWAIT) |
| 2 | COBDATFT | app/asm/ | Date formatting routine (called by CBACT01C) |

---

## JCL Procedures

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | REPROC | app/proc/ | Reprocessing procedure (copy file with record length conversion) |
| 2 | TRANREPT | app/proc/ | Transaction report procedure |

---

## Scheduler Definitions

| # | File | Scheduler | Function |
|---|------|-----------|----------|
| 1 | CardDemo.ca7 | CA-7 | Batch job scheduling definitions |
| 2 | CardDemo.controlm | Control-M | Batch job scheduling definitions |

---

## Summary Counts

| Artifact Type | Core | Optional Modules | Total |
|---------------|------|-------------------|-------|
| COBOL Programs (Online) | 17 | 8 | 25 |
| COBOL Programs (Batch) | 14 | 5 | 19 |
| **COBOL Programs Total** | **31** | **13** | **44** |
| Copybooks (Data) | 17 | 11 | 28 |
| Copybooks (Common/Utility) | 13 | — | 13 |
| **Copybooks Total** | **30** | **11** | **41** |
| BMS Screen Maps | 17 | 4 | 21 |
| BMS Generated Copybooks | 17 | 4 | 21 |
| JCL Jobs | 38 | 8 | 46 |
| Assembler Programs | 2 | — | 2 |
| JCL Procedures | 2 | — | 2 |
| Scheduler Definitions | 2 | — | 2 |
| **Total Source Lines (COBOL)** | **20,656** | **9,525** | **30,181** |
