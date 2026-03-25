# Application Inventory - CardDemo

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Platform:** IBM z/OS Mainframe | COBOL / CICS / VSAM / JCL / BMS

---

## Executive Summary

CardDemo is a mainframe credit card management application built on COBOL/CICS/VSAM. It implements account management, card management, transaction processing, bill payments, reporting, and user security administration. The system consists of **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, plus **13 programs in optional extension modules** (IMS/DB2/MQ).

---

## 1. COBOL Programs (31 Core + 13 Optional)

### 1.1 Online CICS Programs (16 programs)

| # | Program ID | Lines | Transaction | Business Function | Classification |
|---|-----------|-------|-------------|-------------------|----------------|
| 1 | **COSGN00C** | 260 | CC00 | User sign-on / authentication | Security |
| 2 | **COMEN01C** | 308 | CM00 | Main menu - regular users | Navigation |
| 3 | **COADM01C** | 288 | CA00 | Admin menu | Navigation / Admin |
| 4 | **COACTVWC** | 941 | CA01 | Account view (read-only) | Account Management |
| 5 | **COACTUPC** | 4,236 | CA02 | Account update (full CRUD) | Account Management |
| 6 | **COCRDLIC** | 1,459 | CC01 | Credit card list (browse) | Card Management |
| 7 | **COCRDSLC** | 887 | CC02 | Credit card detail view | Card Management |
| 8 | **COCRDUPC** | 1,560 | CC03 | Credit card update | Card Management |
| 9 | **COTRN00C** | 699 | CT00 | Transaction list (browse) | Transaction Processing |
| 10 | **COTRN01C** | 330 | CT01 | Transaction detail view | Transaction Processing |
| 11 | **COTRN02C** | 783 | CT02 | Transaction add (new entry) | Transaction Processing |
| 12 | **CORPT00C** | 649 | CR00 | Transaction report request | Reporting |
| 13 | **COBIL00C** | 572 | CB00 | Bill payment processing | Billing / Payments |
| 14 | **COUSR00C** | 695 | CU00 | User list (admin browse) | User Administration |
| 15 | **COUSR01C** | 299 | CU01 | User add | User Administration |
| 16 | **COUSR02C** | 414 | CU02 | User update | User Administration |

### 1.2 Online CICS Programs - Additional (1 program)

| # | Program ID | Lines | Business Function | Classification |
|---|-----------|-------|-------------------|----------------|
| 17 | **COUSR03C** | 359 | User delete | User Administration |

### 1.3 Batch Programs (12 programs)

| # | Program ID | Lines | Business Function | Classification |
|---|-----------|-------|-------------------|----------------|
| 18 | **CBACT01C** | 430 | Read account file, write reformatted outputs | Data Processing |
| 19 | **CBACT02C** | 178 | Read and display card file records | Data Processing |
| 20 | **CBACT03C** | 178 | Read and display cross-reference file | Data Processing |
| 21 | **CBACT04C** | 652 | Interest calculation on accounts | Financial Calculation |
| 22 | **CBCUS01C** | 178 | Read and display customer file | Data Processing |
| 23 | **CBTRN01C** | 494 | Daily transaction validation/lookup | Transaction Processing |
| 24 | **CBTRN02C** | 731 | Transaction posting (daily -> master) | Transaction Processing |
| 25 | **CBTRN03C** | 649 | Transaction report generation | Reporting |
| 26 | **CBSTM03A** | 924 | Statement generation - main driver | Reporting |
| 27 | **CBSTM03B** | 230 | Statement generation - file I/O helper | Reporting |
| 28 | **CBEXPORT** | 582 | Data export (all entities to flat file) | Data Migration |
| 29 | **CBIMPORT** | 487 | Data import (flat file to VSAM) | Data Migration |

### 1.4 Utility Programs (2 programs)

| # | Program ID | Lines | Business Function | Classification |
|---|-----------|-------|-------------------|----------------|
| 30 | **CSUTLDTC** | 157 | Date conversion utility (CEEDAYS) | Utility |
| 31 | **COBSWAIT** | 41 | Wait/delay utility (calls MVSWAIT) | Utility |

### 1.5 Optional Module: Authorization IMS/DB2/MQ (8 programs)

| # | Program ID | Location | Business Function | Classification |
|---|-----------|----------|-------------------|----------------|
| 32 | **COPAUA0C** | app-authorization-ims-db2-mq/ | MQ trigger for authorization | Integration |
| 33 | **COPAUS0C** | app-authorization-ims-db2-mq/ | Authorization summary view | Integration |
| 34 | **COPAUS1C** | app-authorization-ims-db2-mq/ | Authorization detail view | Integration |
| 35 | **COPAUS2C** | app-authorization-ims-db2-mq/ | Fraud marking (DB2 write) | Integration |
| 36 | **CBPAUP0C** | app-authorization-ims-db2-mq/ | Batch purge of auth records | Integration |
| 37 | **DBUNLDGS** | app-authorization-ims-db2-mq/ | DB unload to GSAM | Integration |
| 38 | **PAUDBLOD** | app-authorization-ims-db2-mq/ | Auth DB load | Integration |
| 39 | **PAUDBUNL** | app-authorization-ims-db2-mq/ | Auth DB unload | Integration |

### 1.6 Optional Module: Transaction Type DB2 (3 programs)

| # | Program ID | Location | Business Function | Classification |
|---|-----------|----------|-------------------|----------------|
| 40 | **COTRTLIC** | app-transaction-type-db2/ | Transaction type list/delete (DB2) | Reference Data |
| 41 | **COTRTUPC** | app-transaction-type-db2/ | Transaction type add/edit (DB2) | Reference Data |
| 42 | **COBTUPDT** | app-transaction-type-db2/ | Batch transaction type update (DB2) | Reference Data |

### 1.7 Optional Module: VSAM-MQ (2 programs)

| # | Program ID | Location | Business Function | Classification |
|---|-----------|----------|-------------------|----------------|
| 43 | **CODATE01** | app-vsam-mq/ | MQ system date service | Integration |
| 44 | **COACCT01** | app-vsam-mq/ | MQ account inquiry service | Integration |

---

## 2. Copybooks (30 Core + 11 Optional)

### 2.1 Data Record Layouts

| # | Copybook | Record Length | Business Entity | Used By |
|---|----------|-------------|-----------------|---------|
| 1 | **CVACT01Y** | 300 bytes | Account master record | COACTUPC, COACTVWC, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN02C, COTRN02C |
| 2 | **CVACT02Y** | 150 bytes | Card master record | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT, CBTRN01C |
| 3 | **CVACT03Y** | variable | Card cross-reference record | CBACT03C, CBACT04C, COACTUPC, COACTVWC, COBIL00C, CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C, CBEXPORT, CBIMPORT |
| 4 | **CVCUS01Y** | 500 bytes | Customer master record | CBCUS01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT, CBSTM03A |
| 5 | **CVTRA05Y** | 350 bytes | Transaction master record | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C, CBTRN01C, CBTRN02C, CBACT04C, CBEXPORT, CBIMPORT |
| 6 | **CVTRA06Y** | 350 bytes | Daily transaction record | CBTRN01C, CBTRN02C |
| 7 | **CVTRA01Y** | 50 bytes | Transaction category balance | CBACT04C, CBTRN02C |
| 8 | **CVTRA02Y** | 50 bytes | Disclosure group record | CBACT04C |
| 9 | **CVTRA03Y** | 60 bytes | Transaction type record | CBTRN03C |
| 10 | **CVTRA04Y** | 60 bytes | Transaction category type | CBTRN03C |
| 11 | **CVTRA07Y** | variable | Transaction report layout | CBTRN03C |
| 12 | **CVCRD01Y** | variable | Card detail display record | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 13 | **CSUSR01Y** | 80 bytes | User security record | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 14 | **COSTM01** | variable | Statement transaction layout | CBSTM03A |
| 15 | **CUSTREC** | variable | Customer record (statement) | CBSTM03A |
| 16 | **CVEXPORT** | variable | Export/import record format | CBEXPORT, CBIMPORT |
| 17 | **UNUSED1Y** | 80 bytes | Unused placeholder record | None (deprecated) |

### 2.2 Application Control Copybooks

| # | Copybook | Purpose | Used By |
|---|----------|---------|---------|
| 18 | **COCOM01Y** | COMMAREA - inter-program communication | All 16 online programs |
| 19 | **COMEN02Y** | Main menu option definitions (11 options) | COMEN01C |
| 20 | **COADM02Y** | Admin menu option definitions (6 options) | COADM01C |
| 21 | **COTTL01Y** | Screen title/header constants | All online programs |
| 22 | **CSDAT01Y** | Date/time working storage | All online programs |
| 23 | **CSMSG01Y** | Common user messages | All online programs |
| 24 | **CSMSG02Y** | Abend/error message areas | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| 25 | **CSSTRPFY** | PF-key mapping paragraph | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 26 | **CSSETATY** | Screen attribute setting macro | COACTUPC |
| 27 | **CSLKPCDY** | Lookup code tables (phone, state, zip) | COACTUPC |
| 28 | **CSUTLDPY** | Date utility parameters | CSUTLDTC |
| 29 | **CSUTLDWY** | Date utility working storage | COACTUPC |
| 30 | **CODATECN** | Date conversion record layout | CBACT01C |

### 2.3 Optional Module Copybooks (11 total)

| # | Copybook | Module | Purpose |
|---|----------|--------|---------|
| 31 | **CCPAUERY** | Auth IMS/DB2/MQ | Authorization error reply |
| 32 | **CCPAURLY** | Auth IMS/DB2/MQ | Authorization reply layout |
| 33 | **CCPAURQY** | Auth IMS/DB2/MQ | Authorization request layout |
| 34 | **CIPAUDTY** | Auth IMS/DB2/MQ | Authorization detail record |
| 35 | **CIPAUSMY** | Auth IMS/DB2/MQ | Authorization summary record |
| 36 | **IMSFUNCS** | Auth IMS/DB2/MQ | IMS function codes |
| 37 | **PADFLPCB** | Auth IMS/DB2/MQ | IMS PCB - detail segment |
| 38 | **PASFLPCB** | Auth IMS/DB2/MQ | IMS PCB - summary segment |
| 39 | **PAUTBPCB** | Auth IMS/DB2/MQ | IMS PCB - auth table |
| 40 | **CSDB2RPY** | Tran Type DB2 | DB2 read parameters |
| 41 | **CSDB2RWY** | Tran Type DB2 | DB2 write parameters |

---

## 3. BMS Screen Maps (17 Core + 4 Optional)

### 3.1 Core BMS Maps

| # | Map File | Map Name | Screen Purpose | Associated Program |
|---|----------|----------|---------------|-------------------|
| 1 | **COSGN00.bms** | COSGN0A | Sign-on screen | COSGN00C |
| 2 | **COMEN01.bms** | COMEN1A | Main menu | COMEN01C |
| 3 | **COADM01.bms** | COADM1A | Admin menu | COADM01C |
| 4 | **COACTVW.bms** | CACTVWA | Account view | COACTVWC |
| 5 | **COACTUP.bms** | CACTUPA | Account update | COACTUPC |
| 6 | **COCRDLI.bms** | CCRDLIA | Card list | COCRDLIC |
| 7 | **COCRDSL.bms** | CCRDSLA | Card detail | COCRDSLC |
| 8 | **COCRDUP.bms** | CCRDUPA | Card update | COCRDUPC |
| 9 | **COTRN00.bms** | COTRN0A | Transaction list | COTRN00C |
| 10 | **COTRN01.bms** | COTRN1A | Transaction view | COTRN01C |
| 11 | **COTRN02.bms** | COTRN2A | Transaction add | COTRN02C |
| 12 | **CORPT00.bms** | CORPT0A | Report request | CORPT00C |
| 13 | **COBIL00.bms** | COBIL0A | Bill payment | COBIL00C |
| 14 | **COUSR00.bms** | COUSR0A | User list | COUSR00C |
| 15 | **COUSR01.bms** | COUSR1A | User add | COUSR01C |
| 16 | **COUSR02.bms** | COUSR2A | User update | COUSR02C |
| 17 | **COUSR03.bms** | COUSR3A | User delete | COUSR03C |

### 3.2 BMS-Generated Copybooks (17 in `cpy-bms/`)

Each BMS map generates a corresponding copybook containing symbolic field definitions used by the COBOL program for SEND MAP / RECEIVE MAP operations. These reside in `app/cpy-bms/` and mirror the BMS map names (e.g., `COSGN00.CPY`, `COMEN01.CPY`, etc.).

### 3.3 Optional Module BMS Maps (4 total)

| # | Map File | Module | Screen Purpose |
|---|----------|--------|---------------|
| 18 | **COPAU00.bms** | Auth IMS/DB2/MQ | Authorization summary |
| 19 | **COPAU01.bms** | Auth IMS/DB2/MQ | Authorization detail |
| 20 | **COTRTLI.bms** | Tran Type DB2 | Transaction type list |
| 21 | **COTRTUP.bms** | Tran Type DB2 | Transaction type update |

---

## 4. JCL Batch Jobs (38 Core + 8 Optional)

### 4.1 Data Loading / VSAM Refresh Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 1 | **ACCTFILE.jcl** | Refresh account master VSAM | IDCAMS REPRO |
| 2 | **CARDFILE.jcl** | Refresh card master VSAM | IDCAMS REPRO |
| 3 | **CUSTFILE.jcl** | Refresh customer master VSAM | IDCAMS REPRO |
| 4 | **XREFFILE.jcl** | Load card cross-reference VSAM | IDCAMS REPRO |
| 5 | **TRANFILE.jcl** | Load transaction master VSAM | IDCAMS REPRO |
| 6 | **DUSRSECJ.jcl** | Load user security VSAM | IDCAMS REPRO |

### 4.2 Core Batch Processing Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 7 | **POSTTRAN.jcl** | Post daily transactions | CBTRN02C |
| 8 | **INTCALC.jcl** | Calculate interest charges | CBACT04C |
| 9 | **COMBTRAN.jcl** | Combine/merge transactions | SORT/MERGE |
| 10 | **CREASTMT.JCL** | Generate customer statements | CBSTM03A |
| 11 | **TRANREPT.jcl** | Generate transaction reports | CBTRN03C |
| 12 | **TRANBKP.jcl** | Backup transaction file | IDCAMS REPRO |
| 13 | **TRANIDX.jcl** | Build alternate index on transactions | IDCAMS DEFINE AIX |

### 4.3 File Management Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 14 | **CLOSEFIL.jcl** | Close CICS files for batch | DFHCSDUP |
| 15 | **OPENFIL.jcl** | Reopen CICS files after batch | DFHCSDUP |

### 4.4 Utility / Definition Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 16 | **DEFGDGB.jcl** | Define GDG base (backup) | IDCAMS DEFINE GDG |
| 17 | **DEFGDGD.jcl** | Define GDG base (daily) | IDCAMS DEFINE GDG |
| 18 | **DEFCUST.jcl** | Define customer VSAM cluster | IDCAMS DEFINE |
| 19 | **ESDSRRDS.jcl** | Define ESDS/RRDS clusters | IDCAMS DEFINE |
| 20 | **DISCGRP.jcl** | Load disclosure group data | IDCAMS REPRO |
| 21 | **TCATBALF.jcl** | Load transaction category balance | IDCAMS REPRO |
| 22 | **TRANCATG.jcl** | Load transaction category types | IDCAMS REPRO |
| 23 | **TRANTYPE.jcl** | Load transaction type reference | IDCAMS REPRO |
| 24 | **DALYREJS.jcl** | Define daily rejects file | IDCAMS DEFINE |

### 4.5 Read / Validation Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 25 | **READACCT.jcl** | Read/validate account file | CBACT01C |
| 26 | **READCARD.jcl** | Read/validate card file | CBACT02C |
| 27 | **READCUST.jcl** | Read/validate customer file | CBCUS01C |
| 28 | **READXREF.jcl** | Read/validate cross-ref file | CBACT03C |
| 29 | **REPTFILE.jcl** | Read report file | Utility |
| 30 | **PRTCATBL.jcl** | Print catalog balance file | Utility |

### 4.6 Data Export/Import Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 31 | **CBEXPORT.jcl** | Export all data to flat file | CBEXPORT |
| 32 | **CBIMPORT.jcl** | Import data from flat file | CBIMPORT |

### 4.7 Infrastructure / Miscellaneous Jobs

| # | JCL Job | Purpose | Executes Program |
|---|---------|---------|-----------------|
| 33 | **WAITSTEP.jcl** | Wait/delay step | COBSWAIT |
| 34 | **CBADMCDJ.jcl** | Admin card demo job | Various |
| 35 | **FTPJCL.JCL** | FTP file transfer job | FTP |
| 36 | **INTRDRJ1.JCL** | Internal reader job 1 | INTRDR |
| 37 | **INTRDRJ2.JCL** | Internal reader job 2 | INTRDR |
| 38 | **TXT2PDF1.JCL** | Convert text to PDF | TXT2PDF |

### 4.8 Optional Module JCL Jobs (8 total)

| # | JCL Job | Module | Purpose |
|---|---------|--------|---------|
| 39 | **CBPAUP0J.jcl** | Auth IMS/DB2/MQ | Batch purge authorizations |
| 40 | **DBPAUTP0.jcl** | Auth IMS/DB2/MQ | Auth DB processing |
| 41 | **LOADPADB.JCL** | Auth IMS/DB2/MQ | Load auth DB |
| 42 | **UNLDGSAM.JCL** | Auth IMS/DB2/MQ | Unload GSAM |
| 43 | **UNLDPADB.JCL** | Auth IMS/DB2/MQ | Unload auth DB |
| 44 | **CREADB21.jcl** | Tran Type DB2 | Create DB2 tables |
| 45 | **MNTTRDB2.jcl** | Tran Type DB2 | Maintain tran types in DB2 |
| 46 | **TRANEXTR.jcl** | Tran Type DB2 | Extract tran types from DB2 |

---

## 5. Assembler Programs (2)

| # | Program | Purpose |
|---|---------|---------|
| 1 | **MVSWAIT.asm** | MVS wait macro (called by COBSWAIT) |
| 2 | **COBDATFT.asm** | Date formatting routine (called by CBACT01C) |

---

## 6. JCL Procedures (2)

| # | Procedure | Purpose |
|---|-----------|---------|
| 1 | **REPROC.prc** | Report generation procedure |
| 2 | **TRANREPT.prc** | Transaction report procedure |

---

## 7. Scheduler Configurations (2)

| # | File | Scheduler | Purpose |
|---|------|-----------|---------|
| 1 | **CardDemo.ca7** | CA-7 | Batch job scheduling definitions |
| 2 | **CardDemo.controlm** | Control-M | Batch job scheduling definitions |

---

## 8. Data Files

### 8.1 ASCII Test Data (`app/data/ASCII/`)

| # | File | Business Entity |
|---|------|-----------------|
| 1 | acctdata.txt | Account master records |
| 2 | carddata.txt | Card master records |
| 3 | cardxref.txt | Card cross-reference records |
| 4 | custdata.txt | Customer master records |
| 5 | dailytran.txt | Daily transaction records |
| 6 | discgrp.txt | Disclosure group records |
| 7 | tcatbal.txt | Transaction category balance |
| 8 | trancatg.txt | Transaction category types |
| 9 | trantype.txt | Transaction type reference |

### 8.2 EBCDIC Data (`app/data/EBCDIC/`)

Mainframe-format versions of the same data files for direct upload to z/OS.

---

## 9. Asset Summary

| Asset Type | Core Count | Optional Count | Total |
|-----------|-----------|---------------|-------|
| COBOL Programs | 31 | 13 | 44 |
| Copybooks | 30 | 11 | 41 |
| BMS Maps | 17 | 4 | 21 |
| BMS-Generated Copybooks | 17 | 0 | 17 |
| JCL Jobs | 38 | 8 | 46 |
| Assembler Programs | 2 | 0 | 2 |
| JCL Procedures | 2 | 0 | 2 |
| Scheduler Configs | 2 | 0 | 2 |
| Data Files (ASCII) | 9 | 0 | 9 |
| **Total** | **148** | **36** | **184** |

---

## 10. Functional Domain Classification

| Domain | Online Programs | Batch Programs | Copybooks | BMS Maps |
|--------|---------------|---------------|-----------|----------|
| **Account Management** | COACTVWC, COACTUPC | CBACT01C, CBACT04C | CVACT01Y, CVACT03Y | COACTVW, COACTUP |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBACT03C | CVACT02Y, CVCRD01Y | COCRDLI, COCRDSL, COCRDUP |
| **Transaction Processing** | COTRN00C, COTRN01C, COTRN02C | CBTRN01C, CBTRN02C | CVTRA05Y, CVTRA06Y, CVTRA01Y | COTRN00, COTRN01, COTRN02 |
| **Billing / Payments** | COBIL00C | -- | CVTRA05Y, CVACT01Y | COBIL00 |
| **Reporting** | CORPT00C | CBTRN03C, CBSTM03A/B | CVTRA07Y, COSTM01 | CORPT00 |
| **User Administration** | COUSR00C-03C | -- | CSUSR01Y | COUSR00-03 |
| **Security** | COSGN00C | -- | CSUSR01Y, COCOM01Y | COSGN00 |
| **Navigation** | COMEN01C, COADM01C | -- | COMEN02Y, COADM02Y | COMEN01, COADM01 |
| **Data Migration** | -- | CBEXPORT, CBIMPORT | CVEXPORT | -- |
| **Utilities** | -- | CSUTLDTC, COBSWAIT | CSUTLDPY, CSUTLDWY | -- |
