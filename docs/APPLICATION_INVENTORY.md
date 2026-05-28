# CardDemo Application Inventory

> **System**: CardDemo – Mainframe Credit Card Management System  
> **Generated**: 2026-05-28  
> **Artifact Count**: 44 COBOL programs · 30 copybooks · 46 JCL jobs · 21 BMS maps · 2 ASM modules · 2 JCL procedures · 3 CSD definitions · 2 scheduler definitions

---

## 1 — Online COBOL Programs (CICS)

Programs that execute under CICS, accessed through 3270 terminal transactions.

| # | Program | Lines | Transaction | Function | Domain | Optional Module | Technologies |
|---|---------|------:|-------------|----------|--------|-----------------|--------------|
| 1 | COSGN00C | 260 | CC00 | Sign-on Screen | Security | — | CICS, VSAM |
| 2 | COMEN01C | 308 | CM00 | Main Menu (User) | Navigation | — | CICS |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation | Db2: Tran Type Mgmt | CICS |
| 4 | COACTVWC | 941 | CAVW | Account View | Account Mgmt | — | CICS, VSAM |
| 5 | COACTUPC | 4,236 | CAUP | Account Update | Account Mgmt | — | CICS, VSAM |
| 6 | COCRDLIC | 1,459 | CCLI | Credit Card List | Card Mgmt | — | CICS, VSAM |
| 7 | COCRDSLC | 887 | CCDL | Credit Card View | Card Mgmt | — | CICS, VSAM |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | — | CICS, VSAM |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transaction Mgmt | — | CICS, VSAM |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transaction Mgmt | — | CICS, VSAM |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transaction Mgmt | — | CICS, VSAM |
| 12 | CORPT00C | 649 | CR00 | Transaction Reports | Reporting | — | CICS |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing | — | CICS, VSAM |
| 14 | COUSR00C | 695 | CU00 | User List (Security) | User Admin | — | CICS, VSAM |
| 15 | COUSR01C | 299 | CU01 | User Add (Security) | User Admin | — | CICS, VSAM |
| 16 | COUSR02C | 414 | CU02 | User Update (Security) | User Admin | — | CICS, VSAM |
| 17 | COUSR03C | 359 | CU03 | User Delete (Security) | User Admin | — | CICS, VSAM |
| 18 | COPAUS0C | 1,032 | CPVS | Pending Authorization Summary | Authorization | IMS-DB2-MQ | CICS, IMS, VSAM |
| 19 | COPAUS1C | 604 | CPVD | Pending Authorization Details | Authorization | IMS-DB2-MQ | CICS, IMS, DB2 |
| 20 | COPAUS2C | 244 | CPVD | Authorization – DB2 Fraud Insert | Authorization | IMS-DB2-MQ | CICS, DB2 |
| 21 | COPAUA0C | 1,026 | CP00 | Process Authorization Requests (MQ trigger) | Authorization | IMS-DB2-MQ | CICS, IMS, MQ, VSAM |
| 22 | COTRTLIC | 2,098 | CTLI | Transaction Type List / Update / Delete | Tran Type Mgmt | Db2: Tran Type Mgmt | CICS, DB2 |
| 23 | COTRTUPC | 1,702 | CTTU | Transaction Type Add / Edit | Tran Type Mgmt | Db2: Tran Type Mgmt | CICS, DB2 |
| 24 | COACCT01 | 620 | CDRA | Account Inquiry via MQ | MQ Integration | MQ Integration | CICS, MQ, VSAM |
| 25 | CODATE01 | 524 | CDRD | System Date Inquiry via MQ | MQ Integration | MQ Integration | CICS, MQ |

---

## 2 — Batch COBOL Programs

Programs executed via JCL in batch mode (no CICS).

| # | Program | Lines | Function | Domain | Optional Module | Technologies |
|---|---------|------:|----------|--------|-----------------|--------------|
| 1 | CBACT01C | 430 | Read Account File & Write to multiple output formats | Account Mgmt | — | Batch COBOL, ASM (COBDATFT) |
| 2 | CBACT02C | 178 | Read & Print Card Data File | Card Mgmt | — | Batch COBOL |
| 3 | CBACT03C | 178 | Read & Print Account Cross-Reference File | Account Mgmt | — | Batch COBOL |
| 4 | CBACT04C | 652 | Interest Calculator | Finance | — | Batch COBOL |
| 5 | CBCUS01C | 178 | Read & Print Customer Data File | Customer Mgmt | — | Batch COBOL |
| 6 | CBTRN01C | 494 | Post Daily Transactions | Transaction Processing | — | Batch COBOL |
| 7 | CBTRN02C | 731 | Core Transaction Posting (balances & categories) | Transaction Processing | — | Batch COBOL |
| 8 | CBTRN03C | 649 | Transaction Report Generation | Reporting | — | Batch COBOL |
| 9 | CBSTM03A | 924 | Account Statement Generation (plain text + HTML) | Reporting | — | Batch COBOL |
| 10 | CBSTM03B | 230 | Statement Subroutine – File I/O for Statement Report | Reporting | — | Batch COBOL (sub-program) |
| 11 | CBEXPORT | 582 | Branch Migration Export (all entities to single file) | Data Migration | — | Batch COBOL |
| 12 | CBIMPORT | 487 | Branch Migration Import (single file → entity files) | Data Migration | — | Batch COBOL |
| 13 | COBSWAIT | 41 | Wait / Sleep Utility | Utility | — | Batch COBOL, ASM (MVSWAIT) |
| 14 | CSUTLDTC | 157 | Date Conversion Utility | Utility | — | COBOL (callable sub-program) |
| 15 | CBPAUP0C | 386 | Purge Expired Pending Authorizations | Authorization | IMS-DB2-MQ | Batch COBOL, IMS |
| 16 | COBTUPDT | 237 | Maintain Transaction Type Table (Batch DB2) | Tran Type Mgmt | Db2: Tran Type Mgmt | Batch COBOL, DB2 |
| 17 | DBUNLDGS | 366 | Unload GSAM (IMS) Data | Data Migration | IMS-DB2-MQ | Batch COBOL, IMS |
| 18 | PAUDBLOD | 369 | Load Pending Auth IMS Database | Authorization | IMS-DB2-MQ | Batch COBOL, IMS |
| 19 | PAUDBUNL | 317 | Unload Pending Auth IMS Database | Authorization | IMS-DB2-MQ | Batch COBOL, IMS |

---

## 3 — Copybooks

Shared data structure definitions (`COPY` members).

### 3.1 Core Copybooks (`app/cpy/`)

| # | Copybook | Purpose | Key Entity | Used By |
|---|----------|---------|------------|---------|
| 1 | CVACT01Y | Account Master Record layout | Account | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COCRDLIC, COCRDUPC, COTRN02C, COPAUA0C, COPAUS0C, COACCT01 |
| 2 | CVACT02Y | Card Master Record layout | Card | CBACT02C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C |
| 3 | CVACT03Y | Account-Card-Customer Cross-Reference layout | Cross-Reference | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COACTUPC, COACTVWC, COCRDLIC, COTRN02C, COPAUA0C, COPAUS0C |
| 4 | CVCUS01Y | Customer Master Record layout | Customer | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, CBSTM03A, COPAUA0C, COPAUS0C |
| 5 | CVCRD01Y | Card Extended Record layout | Card | COCRDSLC, COCRDUPC |
| 6 | CVTRA01Y | Transaction Category Balance layout | Tran Category Balance | CBACT04C, CBTRN02C |
| 7 | CVTRA02Y | Disclosure Group layout | Disclosure | CBACT04C, CBTRN02C |
| 8 | CVTRA03Y | Transaction Type layout | Transaction Type | CBTRN02C |
| 9 | CVTRA04Y | Transaction Category Type layout | Transaction Category | CBTRN02C |
| 10 | CVTRA05Y | Online Transaction Record layout | Transaction | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C |
| 11 | CVTRA06Y | Daily Transaction Record layout | Daily Transaction | CBTRN01C, CBTRN02C, CBTRN03C |
| 12 | CVTRA07Y | Transaction Report Record layout | Report | CBTRN03C |
| 13 | CVEXPORT | Branch Migration Export Record layout | Export Composite | CBEXPORT, CBIMPORT |
| 14 | CUSTREC | Customer Record (statement variant) | Customer | CBSTM03A |
| 15 | COSTM01 | Statement Processing Work Area | Statement | CBSTM03A |
| 16 | COCOM01Y | CICS Commarea – inter-program communication | Commarea | All online programs |
| 17 | COMEN02Y | Main Menu Option Table | Navigation | COMEN01C |
| 18 | COADM02Y | Admin Menu Option Table | Navigation | COADM01C |
| 19 | COTTL01Y | Screen Title Line layout | UI | All online programs with BMS |
| 20 | CSDAT01Y | Date Display Fields | UI | All online programs with BMS |
| 21 | CSMSG01Y | Message Line 1 layout | UI | All online programs with BMS |
| 22 | CSMSG02Y | Message Line 2 layout | UI | All online programs with BMS |
| 23 | CSUSR01Y | User Security Record layout | User Security | COSGN00C, COUSR00C–03C |
| 24 | CSUTLDPY | Date Utility Parameters | Utility | CSUTLDTC |
| 25 | CSUTLDWY | Date Utility Work Area | Utility | CSUTLDTC |
| 26 | CODATECN | Date Conversion Record for ASM call | Utility | CBACT01C |
| 27 | CSSETATY | Set Attribute Bytes | UI | Online programs |
| 28 | CSSTRPFY | String Pad/Format Work Area | Utility | Online programs |
| 29 | CSLKPCDY | Lookup Code Work Area | Utility | Online programs |
| 30 | UNUSED1Y | Unused / Placeholder Copybook | — | None |

### 3.2 Optional Module Copybooks

#### IMS-DB2-MQ Authorization (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Purpose |
|---|----------|---------|
| 1 | CCPAUERY | Authorization Error Record |
| 2 | CCPAURLY | Authorization Reply Record |
| 3 | CCPAURQY | Authorization Request Record |
| 4 | CIPAUDTY | Pending Auth Detail (IMS segment) |
| 5 | CIPAUSMY | Pending Auth Summary (IMS segment) |
| 6 | IMSFUNCS | IMS Function Codes |
| 7 | PADFLPCB | IMS PCB – Pending Auth Detail Full |
| 8 | PASFLPCB | IMS PCB – Pending Auth Summary Full |
| 9 | PAUTBPCB | IMS PCB – Pending Auth Batch |

#### IMS-DB2-MQ Authorization – BMS Copybooks (`app/app-authorization-ims-db2-mq/cpy-bms/`)

| # | Copybook | Purpose |
|---|----------|---------|
| 1 | COPAU00 | BMS-generated copybook for Authorization Summary map |
| 2 | COPAU01 | BMS-generated copybook for Authorization Details map |

#### DB2 Transaction Type (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Purpose |
|---|----------|---------|
| 1 | CSDB2RWY | DB2 Read Work Area |
| 2 | CSDB2RPY | DB2 Reply Work Area |
| 3 | DCLTRTYP | DCL for TRTYP DB2 table (Transaction Type) |
| 4 | DCLTRCAT | DCL for TRCAT DB2 table (Transaction Category) |

---

## 4 — BMS Maps (Screen Definitions)

### 4.1 Core BMS Maps (`app/bms/`)

| # | Map | Program | Screen Name |
|---|-----|---------|-------------|
| 1 | COSGN00 | COSGN00C | Sign-on |
| 2 | COMEN01 | COMEN01C | Main Menu |
| 3 | COADM01 | COADM01C | Admin Menu |
| 4 | COACTVW | COACTVWC | Account View |
| 5 | COACTUP | COACTUPC | Account Update |
| 6 | COCRDLI | COCRDLIC | Credit Card List |
| 7 | COCRDSL | COCRDSLC | Credit Card Detail |
| 8 | COCRDUP | COCRDUPC | Credit Card Update |
| 9 | COTRN00 | COTRN00C | Transaction List |
| 10 | COTRN01 | COTRN01C | Transaction View |
| 11 | COTRN02 | COTRN02C | Transaction Add |
| 12 | CORPT00 | CORPT00C | Transaction Report |
| 13 | COBIL00 | COBIL00C | Bill Payment |
| 14 | COUSR00 | COUSR00C | User List |
| 15 | COUSR01 | COUSR01C | User Add |
| 16 | COUSR02 | COUSR02C | User Update |
| 17 | COUSR03 | COUSR03C | User Delete |

### 4.2 Optional Module BMS Maps

| # | Map | Module | Program | Screen Name |
|---|-----|--------|---------|-------------|
| 1 | COPAU00 | IMS-DB2-MQ | COPAUS0C | Auth Summary |
| 2 | COPAU01 | IMS-DB2-MQ | COPAUS1C | Auth Details |
| 3 | COTRTLI | Db2 Tran Type | COTRTLIC | Tran Type List |
| 4 | COTRTUP | Db2 Tran Type | COTRTUPC | Tran Type Add/Edit |

---

## 5 — JCL Jobs

### 5.1 Core JCL Jobs (`app/jcl/`)

| # | Job | Utility / Program | Purpose | Category |
|---|-----|-------------------|---------|----------|
| 1 | ACCTFILE | IDCAMS | Delete / Define / Load Account VSAM KSDS | Data Setup |
| 2 | CARDFILE | IDCAMS | Delete / Define / Load Card VSAM KSDS | Data Setup |
| 3 | CUSTFILE | IDCAMS | Delete / Define / Load Customer VSAM KSDS | Data Setup |
| 4 | XREFFILE | IDCAMS | Delete / Define / Load Cross-Reference VSAM KSDS | Data Setup |
| 5 | TRANFILE | IDCAMS | Load Transaction Master VSAM from PS | Data Setup |
| 6 | DISCGRP | IDCAMS | Load Disclosure Group VSAM | Data Setup |
| 7 | TCATBALF | IDCAMS | Load Transaction Category Balance VSAM | Data Setup |
| 8 | TRANCATG | IDCAMS | Load Transaction Category Types VSAM | Data Setup |
| 9 | TRANTYPE | IDCAMS | Load Transaction Types VSAM | Data Setup |
| 10 | DUSRSECJ | IEBGENER | Load User Security Flat File to VSAM | Data Setup |
| 11 | DEFGDGB | IDCAMS | Define GDG Bases | Data Setup |
| 12 | DEFGDGD | IDCAMS | Define additional GDG Bases (DB2 extension) | Data Setup |
| 13 | DEFCUST | IDCAMS | Define Customer AIX (Alternate Index) | Data Setup |
| 14 | TRANIDX | IDCAMS | Define AIX on Transaction File | Data Setup |
| 15 | ESDSRRDS | IDCAMS | Create ESDS and RRDS VSAM files | Data Setup |
| 16 | CLOSEFIL | IEFBR14 | Close VSAM files for CICS | Operations |
| 17 | OPENFIL | IEFBR14 | Open VSAM files for CICS | Operations |
| 18 | WAITSTEP | COBSWAIT | Wait / delay step | Operations |
| 19 | POSTTRAN | CBTRN02C | Core transaction posting (daily → master) | Transaction Processing |
| 20 | INTCALC | CBACT04C | Monthly interest calculation | Finance |
| 21 | COMBTRAN | SORT | Combine system + daily transaction files | Transaction Processing |
| 22 | TRANBKP | IDCAMS/REPROC | Backup Transaction VSAM to GDG | Backup |
| 23 | TRANREPT | CBTRN03C | Transaction Report (submitted from CICS) | Reporting |
| 24 | CREASTMT | CBSTM03A | Produce account statements (text + HTML) | Reporting |
| 25 | REPTFILE | IDCAMS | Define / Load Report output files | Reporting |
| 26 | PRTCATBL | IDCAMS | Print Transaction Category Balance | Reporting |
| 27 | DALYREJS | SORT | Extract daily rejection records | Transaction Processing |
| 28 | CBEXPORT | CBEXPORT | Export all data for branch migration | Data Migration |
| 29 | CBIMPORT | CBIMPORT | Import consolidated branch data | Data Migration |
| 30 | CBADMCDJ | CBACT01C | Read Account file (admin batch) | Administration |
| 31 | READACCT | CBACT01C | Read & print Account data | Reporting / Validation |
| 32 | READCARD | CBACT02C | Read & print Card data | Reporting / Validation |
| 33 | READCUST | CBCUS01C | Read & print Customer data | Reporting / Validation |
| 34 | READXREF | CBACT03C | Read & print Cross-Reference data | Reporting / Validation |
| 35 | FTPJCL | FTP | File transfer to/from remote host | Utility |
| 36 | TXT2PDF1 | TXT2PDF | Convert text file to PDF | Utility |
| 37 | INTRDRJ1 | INTRDR | Submit JCL via Internal Reader (job 1) | Utility |
| 38 | INTRDRJ2 | INTRDR | Submit JCL via Internal Reader (job 2) | Utility |

### 5.2 Optional Module JCL Jobs

#### IMS-DB2-MQ Authorization (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job | Program / Utility | Purpose |
|---|-----|-------------------|---------|
| 1 | CBPAUP0J | CBPAUP0C | Purge Expired Pending Authorizations |
| 2 | DBPAUTP0 | IDCAMS | Define Pending Auth IMS databases |
| 3 | LOADPADB | PAUDBLOD | Load Pending Auth IMS DB |
| 4 | UNLDPADB | PAUDBUNL | Unload Pending Auth IMS DB |
| 5 | UNLDGSAM | DBUNLDGS | Unload GSAM data |

#### DB2 Transaction Type (`app/app-transaction-type-db2/jcl/`)

| # | Job | Program / Utility | Purpose |
|---|-----|-------------------|---------|
| 1 | CREADB21 | DSNTEP4 | Create DB2 database and load tables |
| 2 | MNTTRDB2 | COBTUPDT | Maintain Transaction Type table via batch DB2 |
| 3 | TRANEXTR | DSNTIAUL | Extract latest DB2 tran type/category data to flat files |

---

## 6 — Assembler Modules (`app/asm/`)

| # | Module | Function |
|---|--------|----------|
| 1 | COBDATFT | Date format conversion utility (called by CBACT01C) |
| 2 | MVSWAIT | Timer / wait control for batch jobs (called by COBSWAIT) |

---

## 7 — JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC | Generic REPRO utility – load or unload a VSAM file |
| 2 | TRANREPT | Transaction Report procedure – unload VSAM, SORT, run CBTRN03C |

---

## 8 — CSD Resource Definitions

| # | CSD File | Module | Resources Defined |
|---|----------|--------|-------------------|
| 1 | CRDDEMO2 | IMS-DB2-MQ Auth | Mapsets COPAU00/01, Programs COPAUA0C/COPAUS0C/COPAUS1C/COPAUS2C, Transactions CP00/CPVD/CPVS, DB2ENTRY AWS01PLN |
| 2 | CRDDEMOD | Db2 Tran Type | Mapsets COTRTLI/COTRTUP, Programs COTRTLIC/COTRTUPC/COBTUPDT, Transactions CTLI/CTTU |
| 3 | CRDDEMOM | MQ Integration | Programs COACCT01/CODATE01, Transactions CDRA/CDRD |

---

## 9 — Control-M Scheduler Definitions (`app/scheduler/`)

| # | Folder | Schedule | Job Chain |
|---|--------|----------|-----------|
| 1 | DAILY-TransactionBackup | Daily, All Days | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL |
| 2 | WEEKLY-TransactionTypesDBRefresh | Weekly | MNTTRDB2 |
| 3 | MONTHLY-InterestCalculation | Monthly (1st of month) | CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL |

---

## 10 — Data Files (`app/data/EBCDIC/`)

| # | Dataset Name | Copybook | Format | Rec Len | Description |
|---|-------------|----------|--------|--------:|-------------|
| 1 | AWS.M2.CARDDEMO.USRSEC.PS | CSUSR01Y | FB | 80 | User Security |
| 2 | AWS.M2.CARDDEMO.ACCTDATA.PS | CVACT01Y | FB | 300 | Account Data |
| 3 | AWS.M2.CARDDEMO.CARDDATA.PS | CVACT02Y | FB | 150 | Card Data |
| 4 | AWS.M2.CARDDEMO.CUSTDATA.PS | CVCUS01Y | FB | 500 | Customer Data |
| 5 | AWS.M2.CARDDEMO.CARDXREF.PS | CVACT03Y | FB | 50 | Card-Account-Customer XREF |
| 6 | AWS.M2.CARDDEMO.DALYTRAN.PS | CVTRA06Y | FB | 350 | Daily Transactions |
| 7 | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | CVTRA05Y | FB | 350 | Online Transaction Master |
| 8 | AWS.M2.CARDDEMO.DISCGRP.PS | CVTRA02Y | FB | 50 | Disclosure Groups |
| 9 | AWS.M2.CARDDEMO.TRANCATG.PS | CVTRA04Y | FB | 60 | Transaction Category Types |
| 10 | AWS.M2.CARDDEMO.TRANTYPE.PS | CVTRA03Y | FB | 60 | Transaction Types |
| 11 | AWS.M2.CARDDEMO.TCATBALF.PS | CVTRA01Y | FB | 50 | Transaction Category Balance |

---

## 11 — Classification Summary

| Classification | Count |
|----------------|------:|
| Online COBOL Programs (CICS) | 25 |
| Batch COBOL Programs | 19 |
| Core Copybooks | 30 |
| Optional Module Copybooks | 15 |
| Core BMS Maps | 17 |
| Optional BMS Maps | 4 |
| Core JCL Jobs | 38 |
| Optional JCL Jobs | 8 |
| Assembler Modules | 2 |
| JCL Procedures | 2 |
| CSD Resource Defs | 3 |
| Scheduler Definitions | 2 |
| **Total Artifacts** | **165** |
