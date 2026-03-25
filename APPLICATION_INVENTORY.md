# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL on z/OS

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, plus **13 additional programs** in optional extension modules (IMS/DB2/MQ authorization, DB2 transaction types, VSAM-MQ). The system supports two user roles (Regular and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

---

## 1. COBOL Programs (Core — `app/cbl/`)

### 1.1 Online CICS Programs (17 programs)

| # | Program | Lines | CICS Tran | Function | Business Domain | Classification |
|---|---------|-------|-----------|----------|-----------------|----------------|
| 1 | COSGN00C | 260 | CC00 | User Sign-on / Authentication | Security | Entry Point |
| 2 | COMEN01C | 308 | CM00 | Main Menu — Regular User | Navigation | Controller |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation | Controller |
| 4 | COACTVWC | 941 | CA01 | View Account Details | Account Management | Screen Handler |
| 5 | COACTUPC | 4,236 | CA02 | Update Account Details | Account Management | Screen Handler |
| 6 | COCRDLIC | 1,459 | CC01 | List Credit Cards (with browse) | Card Management | Screen Handler |
| 7 | COCRDSLC | 887 | CC02 | View Credit Card Details | Card Management | Screen Handler |
| 8 | COCRDUPC | 1,560 | CC03 | Update Credit Card | Card Management | Screen Handler |
| 9 | COTRN00C | 699 | CT00 | List Transactions (with browse) | Transaction Management | Screen Handler |
| 10 | COTRN01C | 330 | CT01 | View Transaction Details | Transaction Management | Screen Handler |
| 11 | COTRN02C | 783 | CT02 | Add New Transaction | Transaction Management | Screen Handler |
| 12 | COBIL00C | 572 | CB00 | Bill Payment Processing | Bill Payment | Screen Handler |
| 13 | CORPT00C | 649 | CR00 | Transaction Report Request | Reporting | Screen Handler |
| 14 | COUSR00C | 695 | CU00 | List Users (Admin) | User Administration | Screen Handler |
| 15 | COUSR01C | 299 | CU01 | Add New User (Admin) | User Administration | Screen Handler |
| 16 | COUSR02C | 414 | CU02 | Update User (Admin) | User Administration | Screen Handler |
| 17 | COUSR03C | 359 | CU03 | Delete User (Admin) | User Administration | Screen Handler |

### 1.2 Batch Programs (13 programs)

| # | Program | Lines | Function | Business Domain | Classification |
|---|---------|-------|----------|-----------------|----------------|
| 18 | CBTRN02C | 731 | Post Daily Transactions to Master | Transaction Processing | Core Batch |
| 19 | CBACT04C | 652 | Calculate Interest on Accounts | Financial Calculation | Core Batch |
| 20 | CBSTM03A | 924 | Generate Account Statements (text + HTML) | Reporting | Core Batch |
| 21 | CBSTM03B | 230 | Statement Subroutine — File I/O handler | Reporting | Subroutine |
| 22 | CBTRN03C | 649 | Daily Transaction Report Generation | Reporting | Core Batch |
| 23 | CBTRN01C | 494 | Read and Validate Transaction File | Transaction Processing | Core Batch |
| 24 | CBACT01C | 430 | Read Account Master — Multi-format output | Account Processing | Data Utility |
| 25 | CBACT02C | 178 | Read Card Data File | Card Processing | Data Utility |
| 26 | CBACT03C | 178 | Read Cross-Reference File | Reference Data | Data Utility |
| 27 | CBCUS01C | 178 | Read Customer Data File | Customer Processing | Data Utility |
| 28 | CBEXPORT | 582 | Export VSAM Data to Sequential Files | Data Migration | Data Utility |
| 29 | CBIMPORT | 487 | Import Sequential Data to VSAM Files | Data Migration | Data Utility |
| 30 | COBSWAIT | 41 | Wait Utility (calls MVSWAIT assembler) | Infrastructure | Utility |

### 1.3 Shared Utility Programs (1 program)

| # | Program | Lines | Function | Business Domain | Classification |
|---|---------|-------|----------|-----------------|----------------|
| 31 | CSUTLDTC | 157 | Date Validation via CEEDAYS API | Infrastructure | Shared Utility |

---

## 2. Optional Extension Modules

### 2.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 32 | COPAUA0C | MQ Trigger — Authorization Request Listener | CICS + MQ | Online |
| 33 | COPAUS0C | Authorization Summary Screen | CICS + IMS DB | Online |
| 34 | COPAUS1C | Authorization Detail Screen | CICS + IMS DB | Online |
| 35 | COPAUS2C | Mark Transaction as Fraud (DB2 update) | CICS + DB2 | Online |
| 36 | CBPAUP0C | Batch Purge of Old Auth Records | Batch + DB2 | Batch |
| 37 | DBUNLDGS | Unload GSAM Segments | Batch + IMS | Data Utility |
| 38 | PAUDBLOD | Load Authorization DB | Batch + IMS | Data Utility |
| 39 | PAUDBUNL | Unload Authorization DB | Batch + IMS | Data Utility |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 40 | COTRTUPC | Add/Edit Transaction Types | CICS + DB2 | Online |
| 41 | COTRTLIC | List/Delete Transaction Types | CICS + DB2 | Online |
| 42 | COBTUPDT | Batch Update Transaction Types | Batch + DB2 | Batch |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 43 | COACCT01 | Account Inquiry via MQ Request/Response | CICS + MQ | Online |
| 44 | CODATE01 | System Date via MQ Request/Response | CICS + MQ | Online |

---

## 3. Copybooks (Core — `app/cpy/`)

### 3.1 Data Record Layouts (CV* prefix)

| # | Copybook | Record Length | Business Entity | Description |
|---|----------|--------------|-----------------|-------------|
| 1 | CVACT01Y | 300 bytes | Account | Account master record layout |
| 2 | CVACT02Y | 150 bytes | Card | Card data record layout |
| 3 | CVACT03Y | 50 bytes | Card Cross-Reference | Card-to-account cross-reference |
| 4 | CVCRD01Y | ~350 bytes | Card (extended) | Card detail with status fields |
| 5 | CVCUS01Y | 500 bytes | Customer | Customer master record layout |
| 6 | CVTRA01Y | 50 bytes | Trans. Category Balance | Category-level balance accumulation |
| 7 | CVTRA02Y | 50 bytes | Disclosure Group | Interest rate by category |
| 8 | CVTRA03Y | 60 bytes | Transaction Type | Transaction type code and description |
| 9 | CVTRA04Y | 60 bytes | Transaction Category | Transaction category definition |
| 10 | CVTRA05Y | 350 bytes | Transaction | Online transaction record |
| 11 | CVTRA06Y | 350 bytes | Daily Transaction | Daily batch transaction record |
| 12 | CVTRA07Y | varies | Report Structures | Transaction report headers/totals |
| 13 | COSTM01 | 350 bytes | Statement Transaction | Altered layout for statement reporting |
| 14 | CVEXPORT | varies | Export Record | Export file layout with all entity types |
| 15 | CUSTREC | varies | Customer (alt) | Alternate customer record structure |

### 3.2 Common Area / UI Support Copybooks

| # | Copybook | Purpose |
|---|----------|---------|
| 16 | COCOM01Y | Common communication area (COMMAREA) between programs |
| 17 | COMEN02Y | Menu option definitions (program names per menu slot) |
| 18 | COADM02Y | Admin menu option definitions |
| 19 | COTTL01Y | Standard title/header line definition |
| 20 | CSDAT01Y | Date formatting working storage |
| 21 | CSMSG01Y | Standard message area |
| 22 | CSMSG02Y | Extended message area |

### 3.3 Security / Utility Copybooks

| # | Copybook | Purpose |
|---|----------|---------|
| 23 | CSUSR01Y | User security record (80 bytes — ID, name, password, type) |
| 24 | CSSETATY | Screen attribute setting utility |
| 25 | CSSTRPFY | String padding/formatting utility |
| 26 | CSLKPCDY | Lookup code utility |
| 27 | CSUTLDPY | Date utility parameters |
| 28 | CSUTLDWY | Date utility working storage |
| 29 | CODATECN | Date conversion record for assembler call |
| 30 | UNUSED1Y | Unused placeholder record |

### 3.4 Optional Module Copybooks

**Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`):**

| # | Copybook | Purpose |
|---|----------|---------|
| 31 | CCPAUERY | Authorization error record |
| 32 | CCPAURLY | Authorization reply record |
| 33 | CCPAURQY | Authorization request record |
| 34 | CIPAUDTY | Authorization detail record |
| 35 | CIPAUSMY | Authorization summary record |
| 36 | IMSFUNCS | IMS function codes |
| 37 | PADFLPCB | IMS GSAM PCB for data load |
| 38 | PASFLPCB | IMS GSAM PCB for segments |
| 39 | PAUTBPCB | IMS DB PCB for auth table |

**Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`):**

| # | Copybook | Purpose |
|---|----------|---------|
| 40 | CSDB2RPY | DB2 reply working storage |
| 41 | CSDB2RWY | DB2 read/write working storage |

---

## 4. BMS Screen Maps

### 4.1 Core Maps (`app/bms/`)

| # | BMS Map | Map Set | Screen Title | Associated Program | Domain |
|---|---------|---------|-------------|-------------------|--------|
| 1 | COSGN00.bms | COSGN00 | User Sign-on | COSGN00C | Security |
| 2 | COMEN01.bms | COMEN01 | Main Menu | COMEN01C | Navigation |
| 3 | COADM01.bms | COADM01 | Admin Menu | COADM01C | Navigation |
| 4 | COACTVW.bms | COACTVW | View Account | COACTVWC | Account Mgmt |
| 5 | COACTUP.bms | COACTUP | Update Account | COACTUPC | Account Mgmt |
| 6 | COCRDLI.bms | COCRDLI | Card List | COCRDLIC | Card Mgmt |
| 7 | COCRDSL.bms | COCRDSL | Card View | COCRDSLC | Card Mgmt |
| 8 | COCRDUP.bms | COCRDUP | Card Update | COCRDUPC | Card Mgmt |
| 9 | COTRN00.bms | COTRN00 | Transaction List | COTRN00C | Transaction Mgmt |
| 10 | COTRN01.bms | COTRN01 | Transaction View | COTRN01C | Transaction Mgmt |
| 11 | COTRN02.bms | COTRN02 | Add Transaction | COTRN02C | Transaction Mgmt |
| 12 | COBIL00.bms | COBIL00 | Bill Payment | COBIL00C | Bill Payment |
| 13 | CORPT00.bms | CORPT00 | Reports | CORPT00C | Reporting |
| 14 | COUSR00.bms | COUSR00 | User List | COUSR00C | User Admin |
| 15 | COUSR01.bms | COUSR01 | Add User | COUSR01C | User Admin |
| 16 | COUSR02.bms | COUSR02 | Update User | COUSR02C | User Admin |
| 17 | COUSR03.bms | COUSR03 | Delete User | COUSR03C | User Admin |

### 4.2 BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map has a corresponding generated copybook (17 total) providing symbolic field maps for COBOL programs. Named identically to their BMS source (e.g., `COSGN00.CPY` from `COSGN00.bms`).

### 4.3 Optional Module BMS Maps

| # | BMS Map | Module | Screen Title |
|---|---------|--------|-------------|
| 18 | COPAU00.bms | Authorization (IMS/DB2/MQ) | Auth Summary |
| 19 | COPAU01.bms | Authorization (IMS/DB2/MQ) | Auth Detail |
| 20 | COTRTLI.bms | Transaction Type (DB2) | Trans Type List |
| 21 | COTRTUP.bms | Transaction Type (DB2) | Trans Type Update |

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data File Initialization Jobs

| # | JCL Job | Purpose | Key Program/Utility | Target Dataset(s) |
|---|---------|---------|--------------------|--------------------|
| 1 | DUSRSECJ | Load User Security VSAM file | IEBGENER, IDCAMS | USRSEC.VSAM.KSDS |
| 2 | ACCTFILE | Define & load Account Master VSAM | IDCAMS | ACCTDATA.VSAM.KSDS |
| 3 | CARDFILE | Define & load Card Master VSAM | IDCAMS | CARDDATA.VSAM.KSDS |
| 4 | CUSTFILE | Define & load Customer Master VSAM | IDCAMS | CUSTDATA.VSAM.KSDS |
| 5 | XREFFILE | Define & load Card Cross-Ref VSAM + AIX | IDCAMS | CARDXREF.VSAM.KSDS |
| 6 | TRANFILE | Define & load Transaction Master VSAM + AIX | IDCAMS | TRANSACT.VSAM.KSDS |
| 7 | TRANTYPE | Define & load Transaction Type VSAM | IDCAMS | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | Define & load Transaction Category VSAM | IDCAMS | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | Define & load Trans Category Balance VSAM | IDCAMS | TCATBAL.VSAM.KSDS |
| 10 | DISCGRP | Define & load Disclosure Group VSAM | IDCAMS | DISCGRP.VSAM.KSDS |
| 11 | REPTFILE | Define Transaction Report VSAM | IDCAMS | TRANSACT.VSAM.KSDS (report) |
| 12 | DALYREJS | Define Daily Rejects File | IDCAMS | DALYREJS dataset |
| 13 | DEFCUST | Define Customer VSAM cluster | IDCAMS | CUSTDATA.VSAM.KSDS |
| 14 | ESDSRRDS | Load ESDS/RRDS format security file | IDCAMS | USRSEC.VSAM.ESDS / RRDS |

### 5.2 Batch Processing Jobs (Daily Cycle)

| # | JCL Job | Purpose | Key Program/Utility | Cycle Order |
|---|---------|---------|--------------------|----|
| 15 | CLOSEFIL | Close CICS files for batch | SDSF | 1 |
| 16 | POSTTRAN | Post daily transactions to master | CBTRN02C | 2 |
| 17 | INTCALC | Calculate interest on accounts | CBACT04C | 3 |
| 18 | TRANBKP | Backup transaction file | IDCAMS | 4 |
| 19 | COMBTRAN | Combine/sort transactions | SORT, IDCAMS | 5 |
| 20 | CREASTMT | Generate account statements (text + HTML) | SORT, CBSTM03A | 6 |
| 21 | TRANREPT | Generate daily transaction report | SORT, CBTRN03C | 7 |
| 22 | TRANIDX | Rebuild transaction alternate index | IDCAMS | 8 |
| 23 | OPENFIL | Reopen CICS files after batch | SDSF | 9 |

### 5.3 Data Utility Jobs

| # | JCL Job | Purpose | Key Program/Utility |
|---|---------|---------|---------------------|
| 24 | READACCT | Read account master (multi-format) | CBACT01C |
| 25 | READCARD | Read card data file | CBACT02C |
| 26 | READCUST | Read customer data file | CBCUS01C |
| 27 | READXREF | Read cross-reference file | CBACT03C |
| 28 | CBEXPORT | Export VSAM to sequential | CBEXPORT |
| 29 | CBIMPORT | Import sequential to VSAM | CBIMPORT |
| 30 | PRTCATBL | Print category balance report | SORT |
| 31 | WAITSTEP | Wait step utility | COBSWAIT |

### 5.4 Infrastructure / Maintenance Jobs

| # | JCL Job | Purpose | Key Program/Utility |
|---|---------|---------|---------------------|
| 32 | DEFGDGB | Define GDG base clusters | IDCAMS |
| 33 | DEFGDGD | Backup GDG data (types, categories, disclosure) | IDCAMS, IEBGENER |
| 34 | CBADMCDJ | Load CICS CSD definitions | DFHCSDUP |
| 35 | FTPJCL | FTP file transfer job | FTP |
| 36 | INTRDRJ1 | Internal reader — trigger job chain | IDCAMS, IEBGENER |
| 37 | INTRDRJ2 | Internal reader — triggered job | IDCAMS |
| 38 | TXT2PDF1 | Convert statement text to PDF | IKJEFT1B (TXT2PDF) |

### 5.5 Optional Module JCL Jobs

**Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`):**

| # | JCL Job | Purpose |
|---|---------|---------|
| 39 | CBPAUP0J | Batch purge old authorization records |
| 40 | DBPAUTP0 | Initialize authorization IMS DB |
| 41 | LOADPADB | Load authorization database |
| 42 | UNLDGSAM | Unload GSAM segments |
| 43 | UNLDPADB | Unload authorization database |

**Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`):**

| # | JCL Job | Purpose |
|---|---------|---------|
| 44 | CREADB21 | Create DB2 objects for transaction types |
| 45 | MNTTRDB2 | Maintain transaction type DB2 table |
| 46 | TRANEXTR | Extract transaction types from DB2 |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Function |
|---|---------|----------|
| 1 | MVSWAIT.asm | MVS WAIT SVC — used by COBSWAIT for timed delays |
| 2 | COBDATFT.asm | Date formatting — converts between date formats |

---

## 7. JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Reusable report procedure |
| 2 | TRANREPT.prc | Transaction report procedure (used by TRANREPT.jcl) |

---

## 8. Other Artifacts

| Category | Path | Count | Description |
|----------|------|-------|-------------|
| CSD Definitions | `app/csd/` | 1 | CICS resource definitions (CARDDEMO.CSD) |
| Control Files | `app/ctl/` | varies | Control parameters for batch jobs |
| Catalog Listings | `app/catlg/` | varies | VSAM catalog listings |
| Assembler Macros | `app/maclib/` | varies | Macro library for assembler programs |
| Scheduler Configs | `app/scheduler/` | 2 | CA7 and Control-M job scheduling definitions |
| Sample Data (ASCII) | `app/data/ASCII/` | varies | Test data in ASCII format |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | varies | Test data in EBCDIC for mainframe upload |
| Shell Scripts | `scripts/` | 10+ | FTP-based mainframe interaction scripts |
| Sample JCL | `samples/` | varies | Compilation JCL and M2 configs |

---

## 9. Summary Statistics

| Artifact Type | Core | Optional Modules | Total |
|---------------|------|------------------|-------|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks | 30 | 11 | **41** |
| BMS Maps | 17 | 4 | **21** |
| BMS Copybooks | 17 | — | **17** |
| JCL Jobs | 38 | 8 | **46** |
| Assembler Programs | 2 | — | **2** |
| JCL Procedures | 2 | — | **2** |
| Total Lines of COBOL | 20,650 | ~3,000 est. | **~23,650** |

---

## 10. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C (Sign-on) |
| `CB*` | Batch COBOL program | CBTRN02C (Transaction posting) |
| `CS*` | Common/Shared utility | CSUTLDTC (Date utility) |
| `CV*` | Copybook — VSAM record layout | CVACT01Y (Account record) |
| `CO*Y` | Copybook — common/communication area | COCOM01Y (COMMAREA) |
| `CS*Y` | Copybook — shared utilities/security | CSUSR01Y (User security) |
| `*C` suffix | COBOL program (vs. copybook `*Y`) | COACTVWC |
