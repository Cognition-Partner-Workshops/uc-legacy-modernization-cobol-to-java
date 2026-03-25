# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL | **Total Artifacts:** 130+

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [COBOL Programs (31 Core + 13 Optional)](#2-cobol-programs)
3. [Copybooks (30 Core + 11 Optional)](#3-copybooks)
4. [BMS Screen Maps (17 Core + 4 Optional)](#4-bms-screen-maps)
5. [JCL Batch Jobs (38 Core + 8 Optional)](#5-jcl-batch-jobs)
6. [Assembler Programs (2)](#6-assembler-programs)
7. [JCL Procedures (2)](#7-jcl-procedures)
8. [Supporting Artifacts](#8-supporting-artifacts)

---

## 1. Executive Summary

CardDemo is a mainframe credit card management application simulating account management, card management, transactions, bill payments, and reporting. It is designed for mainframe modernization workshops.

| Category              | Core | Optional Modules | Total |
|-----------------------|------|-------------------|-------|
| COBOL Programs        | 31   | 13                | 44    |
| Copybooks             | 30   | 11                | 41    |
| BMS Screen Maps       | 17   | 4                 | 21    |
| BMS-Generated Copybooks | 17 | —                 | 17    |
| JCL Batch Jobs        | 38   | 8                 | 46    |
| Assembler Programs    | 2    | —                 | 2     |
| JCL Procedures        | 2    | —                 | 2     |
| **Grand Total**       |      |                   | **173** |

**User Roles:** Regular User (`USER0001`) and Admin (`ADMIN001`)
**Login Transaction:** `CC00`

---

## 2. COBOL Programs

### 2.1 Online CICS Programs (17 programs)

Programs prefixed with `CO` run under CICS and manage the 3270 terminal-based UI.

| # | Program    | Lines | CICS Trans | Function                              | Business Domain     | Classification |
|---|------------|-------|------------|---------------------------------------|---------------------|----------------|
| 1 | COSGN00C   | 260   | CC00       | User sign-on / authentication         | Security            | Online — Auth  |
| 2 | COMEN01C   | 308   | CM00       | Main menu for regular users           | Navigation          | Online — Menu  |
| 3 | COADM01C   | 288   | CA00       | Admin menu for admin users            | Navigation          | Online — Menu  |
| 4 | COACTVWC   | 941   | CAVW       | View account details                  | Account Management  | Online — Inquiry |
| 5 | COACTUPC   | 4,236 | CAUP       | Update account and customer details   | Account Management  | Online — Update |
| 6 | COCRDLIC   | 1,459 | CCLI       | List credit cards (with paging)       | Card Management     | Online — List  |
| 7 | COCRDSLC   | 887   | CCDL       | View credit card details              | Card Management     | Online — Inquiry |
| 8 | COCRDUPC   | 1,560 | CCUP       | Update credit card details            | Card Management     | Online — Update |
| 9 | COTRN00C   | 699   | CT00       | List transactions (with paging)       | Transactions        | Online — List  |
| 10| COTRN01C   | 330   | CT01       | View a single transaction             | Transactions        | Online — Inquiry |
| 11| COTRN02C   | 783   | CT02       | Add a new transaction                 | Transactions        | Online — Update |
| 12| CORPT00C   | 649   | CR00       | Submit batch transaction reports      | Reporting           | Online — Report |
| 13| COBIL00C   | 572   | CB00       | Bill payment — pay balance in full    | Billing / Payments  | Online — Update |
| 14| COUSR00C   | 695   | CU00       | List all users (Admin)                | User Management     | Online — List  |
| 15| COUSR01C   | 299   | CU01       | Add a new user (Admin)                | User Management     | Online — Update |
| 16| COUSR02C   | 414   | CU02       | Update a user (Admin)                 | User Management     | Online — Update |
| 17| COUSR03C   | 359   | CU03       | Delete a user (Admin)                 | User Management     | Online — Update |

### 2.2 Batch Programs (14 programs)

Programs prefixed with `CB` run in batch mode, typically invoked via JCL.

| # | Program    | Lines | Function                                          | Business Domain     | Classification      |
|---|------------|-------|---------------------------------------------------|---------------------|---------------------|
| 1 | CBACT01C   | 430   | Read account file, write to sequential outputs    | Account Management  | Batch — Extract     |
| 2 | CBACT02C   | 178   | Read and print card data file                     | Card Management     | Batch — Report      |
| 3 | CBACT03C   | 178   | Read and print account cross-reference file       | Card Management     | Batch — Report      |
| 4 | CBACT04C   | 652   | Interest calculation on accounts                  | Financial Processing| Batch — Calculation |
| 5 | CBCUS01C   | 178   | Read and print customer data file                 | Customer Management | Batch — Report      |
| 6 | CBTRN01C   | 494   | Post records from daily transaction file          | Transactions        | Batch — Posting     |
| 7 | CBTRN02C   | 731   | Post daily transactions (core posting engine)     | Transactions        | Batch — Posting     |
| 8 | CBTRN03C   | 649   | Print the transaction detail report               | Reporting           | Batch — Report      |
| 9 | CBSTM03A   | 924   | Generate account statements (main driver)         | Reporting           | Batch — Report      |
| 10| CBSTM03B   | 230   | Generate account statements (report writer)       | Reporting           | Batch — Report      |
| 11| CBEXPORT   | 582   | Export VSAM data to sequential file               | Data Migration      | Batch — Utility     |
| 12| CBIMPORT   | 487   | Import sequential data back into VSAM             | Data Migration      | Batch — Utility     |
| 13| COBSWAIT   | 41    | Wait utility (parameter in centiseconds)          | Infrastructure      | Batch — Utility     |
| 14| CSUTLDTC   | 157   | Date validation utility (calls CEEDAYS)           | Infrastructure      | Batch — Utility     |

### 2.3 Optional Module: Authorization (IMS/DB2/MQ) — 8 programs

Located in `app/app-authorization-ims-db2-mq/cbl/`

| # | Program    | Lines | Function                                          | Classification       |
|---|------------|-------|---------------------------------------------------|----------------------|
| 1 | COPAUA0C   | 1,026 | Card authorization decision (MQ trigger)          | Online — MQ Trigger  |
| 2 | COPAUS0C   | 1,032 | Summary view of authorization messages            | Online — IMS Inquiry |
| 3 | COPAUS1C   | 604   | Detail view of authorization message              | Online — IMS Inquiry |
| 4 | COPAUS2C   | 244   | Mark authorization message as fraud (DB2)         | Online — DB2 Update  |
| 5 | CBPAUP0C   | 386   | Batch purge of expired authorization messages     | Batch — IMS Purge    |
| 6 | DBUNLDGS   | 366   | Unload IMS GSAM data                              | Batch — IMS Utility  |
| 7 | PAUDBLOD   | 369   | Load pending authorization DB                     | Batch — IMS Load     |
| 8 | PAUDBUNL   | 317   | Unload pending authorization DB                   | Batch — IMS Unload   |

### 2.4 Optional Module: Transaction Type DB2 — 3 programs

Located in `app/app-transaction-type-db2/cbl/`

| # | Program    | Lines | Function                                          | Classification       |
|---|------------|-------|---------------------------------------------------|----------------------|
| 1 | COTRTUPC   | 1,702 | Add/edit transaction types (DB2 CRUD)             | Online — DB2 Update  |
| 2 | COTRTLIC   | 2,098 | List/delete transaction types (DB2 cursor)        | Online — DB2 List    |
| 3 | COBTUPDT   | 237   | Batch update of transaction types                 | Batch — DB2 Update   |

### 2.5 Optional Module: VSAM-MQ — 2 programs

Located in `app/app-vsam-mq/cbl/`

| # | Program    | Lines | Function                                          | Classification       |
|---|------------|-------|---------------------------------------------------|----------------------|
| 1 | COACCT01   | 620   | MQ request/response for account inquiry           | Online — MQ Service  |
| 2 | CODATE01   | 524   | MQ request/response for system date               | Online — MQ Service  |

---

## 3. Copybooks

### 3.1 Data Record Layouts (Business Entity Copybooks)

| # | Copybook   | Lines | Description                                | Business Entity      | Record Size |
|---|------------|-------|--------------------------------------------|----------------------|-------------|
| 1 | CVACT01Y   | 20    | Account master record layout               | Account              | ~300 bytes  |
| 2 | CVACT02Y   | 14    | Card data record layout                    | Card                 | ~150 bytes  |
| 3 | CVACT03Y   | 11    | Card-to-account cross-reference layout     | Card Cross-Reference | ~36 bytes   |
| 4 | CVCUS01Y   | 26    | Customer master record layout              | Customer             | ~500 bytes  |
| 5 | CUSTREC    | 26    | Customer record (alternate layout)         | Customer             | 500 bytes   |
| 6 | CVTRA01Y   | 13    | Transaction record layout (batch input)    | Transaction          | ~350 bytes  |
| 7 | CVTRA02Y   | 13    | Transaction record (interest calc variant) | Transaction          | ~350 bytes  |
| 8 | CVTRA03Y   | 10    | Transaction type lookup                    | Transaction Type     | ~20 bytes   |
| 9 | CVTRA04Y   | 12    | Transaction category lookup                | Transaction Category | ~40 bytes   |
| 10| CVTRA05Y   | 21    | Online transaction record layout           | Transaction          | ~350 bytes  |
| 11| CVTRA06Y   | 21    | Daily transaction record layout            | Transaction          | ~350 bytes  |
| 12| CVTRA07Y   | 73    | Transaction report detail layout           | Report               | ~133 bytes  |
| 13| CVEXPORT   | 103   | Multi-record export layout (REDEFINES)     | Data Migration       | 500 bytes   |
| 14| CSUSR01Y   | 26    | User security record layout                | User Security        | ~80 bytes   |
| 15| COSTM01    | 38    | Transaction layout for statement reporting | Statement            | ~355 bytes  |

### 3.2 Application Infrastructure Copybooks

| # | Copybook   | Lines | Description                                |
|---|------------|-------|--------------------------------------------|
| 1 | COCOM01Y   | 47    | Common COMMAREA — inter-program communication area |
| 2 | COMEN02Y   | 101   | Menu option definitions (regular users)    |
| 3 | COADM02Y   | 62    | Admin menu option definitions              |
| 4 | COTTL01Y   | 27    | Screen title/header constants              |
| 5 | CSDAT01Y   | 58    | Current date/time work area                |
| 6 | CSMSG01Y   | 24    | Common user messages                       |
| 7 | CSMSG02Y   | 35    | Abend handling work area                   |
| 8 | CSSETATY   | 30    | Field attribute-setting template (COPY REPLACING) |
| 9 | CSSTRPFY   | 85    | PF-key mapping/storage utility             |
| 10| CVCRD01Y   | 46    | Credit card work area (screen data)        |
| 11| CSLKPCDY   | 1,318 | Lookup code tables (state, country codes)  |
| 12| CSUTLDPY   | 375   | Date utility parameters (CEEDAYS linkage)  |
| 13| CSUTLDWY   | 89    | Date editing working-storage fields        |
| 14| CODATECN   | 52    | Date conversion record (assembler linkage) |
| 15| UNUSED1Y   | 10    | Unused / deprecated placeholder            |

### 3.3 Optional Module Copybooks

**Authorization (IMS/DB2/MQ):** `app/app-authorization-ims-db2-mq/cpy/`

| # | Copybook   | Description                                          |
|---|------------|------------------------------------------------------|
| 1 | CCPAUERY   | Authorization error record layout                    |
| 2 | CCPAURLY   | Authorization reply record layout                    |
| 3 | CCPAURQY   | Authorization request record layout                  |
| 4 | CIPAUDTY   | Authorization detail IMS segment layout              |
| 5 | CIPAUSMY   | Authorization summary IMS segment layout             |
| 6 | IMSFUNCS   | IMS function code constants                          |
| 7 | PADFLPCB   | IMS PCB for authorization detail database            |
| 8 | PASFLPCB   | IMS PCB for authorization summary database           |
| 9 | PAUTBPCB   | IMS PCB for authorization table database             |

**Transaction Type DB2:** `app/app-transaction-type-db2/cpy/`

| # | Copybook   | Description                                          |
|---|------------|------------------------------------------------------|
| 1 | CSDB2RPY   | DB2 reply/result area for transaction types          |
| 2 | CSDB2RWY   | DB2 read/write working-storage for transaction types |

---

## 4. BMS Screen Maps

### 4.1 Core BMS Maps (17 maps)

Located in `app/bms/` with generated copybooks in `app/cpy-bms/`

| # | BMS Map    | Screen Name                  | Used By    | User Role |
|---|------------|------------------------------|------------|-----------|
| 1 | COSGN00    | Login / Sign-on Screen       | COSGN00C   | All       |
| 2 | COMEN01    | Main Menu                    | COMEN01C   | Regular   |
| 3 | COADM01    | Admin Menu                   | COADM01C   | Admin     |
| 4 | COACTVW    | Account View                 | COACTVWC   | Regular   |
| 5 | COACTUP    | Account Update               | COACTUPC   | Regular   |
| 6 | COCRDLI    | Card Listing                 | COCRDLIC   | All       |
| 7 | COCRDSL    | Card Detail / Selection      | COCRDSLC   | All       |
| 8 | COCRDUP    | Card Update                  | COCRDUPC   | All       |
| 9 | COTRN00    | Transaction List             | COTRN00C   | Regular   |
| 10| COTRN01    | Transaction View             | COTRN01C   | Regular   |
| 11| COTRN02    | Transaction Add              | COTRN02C   | Regular   |
| 12| CORPT00    | Transaction Report Request   | CORPT00C   | Regular   |
| 13| COBIL00    | Bill Payment                 | COBIL00C   | Regular   |
| 14| COUSR00    | User List                    | COUSR00C   | Admin     |
| 15| COUSR01    | User Add                     | COUSR01C   | Admin     |
| 16| COUSR02    | User Update                  | COUSR02C   | Admin     |
| 17| COUSR03    | User Delete                  | COUSR03C   | Admin     |

### 4.2 Optional Module BMS Maps (4 maps)

| # | BMS Map    | Module          | Screen Name                      |
|---|------------|-----------------|----------------------------------|
| 1 | COPAU00    | Authorization   | Pending Authorization Summary    |
| 2 | COPAU01    | Authorization   | Pending Authorization Detail     |
| 3 | COTRTLI    | Tran Type DB2   | Transaction Type Listing         |
| 4 | COTRTUP    | Tran Type DB2   | Transaction Type Update          |

---

## 5. JCL Batch Jobs

### 5.1 Data Loading / Refresh Jobs (10 jobs)

| # | JCL Job    | Function                                         | COBOL PGM  | Key Datasets                  |
|---|------------|--------------------------------------------------|------------|-------------------------------|
| 1 | ACCTFILE   | Refresh account master VSAM from sequential      | IDCAMS     | ACCTDAT (VSAM KSDS)          |
| 2 | CARDFILE   | Refresh card master VSAM (+ alternate indexes)   | IDCAMS     | CARDDAT (VSAM KSDS)          |
| 3 | CUSTFILE   | Refresh customer master VSAM from sequential     | IDCAMS     | CUSTDAT (VSAM KSDS)          |
| 4 | XREFFILE   | Load card cross-reference VSAM (+ alt indexes)   | IDCAMS     | CARDXREF (VSAM KSDS)         |
| 5 | TRANFILE   | Load transaction master VSAM (+ alt indexes)     | IDCAMS     | TRANSACT (VSAM KSDS)         |
| 6 | DUSRSECJ   | Load user security VSAM file                     | IDCAMS     | USRSEC (VSAM KSDS)           |
| 7 | TRANTYPE   | Load transaction type reference VSAM             | IDCAMS     | TRANTYPE (VSAM KSDS)         |
| 8 | TRANCATG   | Load transaction category reference VSAM         | IDCAMS     | TRANCATG (VSAM KSDS)         |
| 9 | TCATBALF   | Load transaction category balance VSAM           | IDCAMS     | TCATBAL (VSAM KSDS)          |
| 10| DISCGRP    | Load discount group reference VSAM               | IDCAMS     | DISCGRP (VSAM KSDS)          |

### 5.2 Core Batch Processing Jobs (8 jobs)

| # | JCL Job    | Function                                         | COBOL PGM  | Key Datasets                  |
|---|------------|--------------------------------------------------|------------|-------------------------------|
| 1 | POSTTRAN   | Post daily transactions to accounts              | CBTRN02C   | TRANFILE, DALYTRAN, ACCTFILE  |
| 2 | INTCALC    | Calculate interest on accounts                   | CBACT04C   | ACCTFILE, XREFFILE, DISCGRP   |
| 3 | COMBTRAN   | Combine/merge daily transactions into master     | SORT       | TRANSACT (VSAM)               |
| 4 | CREASTMT   | Generate account statements                      | CBSTM03A   | TRANSACT, XREFFILE, ACCTFILE, CUSTFILE |
| 5 | TRANBKP    | Backup transaction file                          | REPROC     | TRANSACT (VSAM → SEQ)         |
| 6 | TRANREPT   | Generate transaction detail report               | CBTRN03C   | TRANFILE, CARDXREF, TRANTYPE  |
| 7 | TRANIDX    | Define/build alternate indexes on TRANSACT       | IDCAMS     | TRANSACT (AIX)                |
| 8 | WAITSTEP   | Wait step utility (delays between batch steps)   | COBSWAIT   | —                             |

### 5.3 CICS File Control Jobs (2 jobs)

| # | JCL Job    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | CLOSEFIL   | Close CICS files before batch processing         |
| 2 | OPENFIL    | Open CICS files after batch processing           |

### 5.4 Data Read / Print / Utility Jobs (10 jobs)

| # | JCL Job    | Function                                         | COBOL PGM  |
|---|------------|--------------------------------------------------|------------|
| 1 | READACCT   | Read and extract account data to sequential      | CBACT01C   |
| 2 | READCARD   | Read and print card data file                    | CBACT02C   |
| 3 | READCUST   | Read and print customer data file                | CBCUS01C   |
| 4 | READXREF   | Read and print cross-reference data file         | CBACT03C   |
| 5 | CBEXPORT   | Export all VSAM data to sequential export file   | CBEXPORT   |
| 6 | CBIMPORT   | Import sequential data back into VSAM files      | CBIMPORT   |
| 7 | PRTCATBL   | Print transaction category balance report        | SORT/REPROC|
| 8 | REPTFILE   | Define/manage report output datasets             | IDCAMS     |
| 9 | TXT2PDF1   | Convert text report to PDF                       | IKJEFT1B   |
| 10| FTPJCL     | FTP file transfer utility job                    | FTP        |

### 5.5 Dataset Definition / Maintenance Jobs (5 jobs)

| # | JCL Job    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | DEFGDGB    | Define GDG base entries for backup datasets      |
| 2 | DEFGDGD    | Define GDG data entries + backup transaction data|
| 3 | DEFCUST    | Define customer VSAM cluster                     |
| 4 | DALYREJS   | Define daily rejects dataset                     |
| 5 | ESDSRRDS   | Define ESDS/RRDS VSAM datasets (testing)         |

### 5.6 Administrative / Misc Jobs (3 jobs)

| # | JCL Job    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | CBADMCDJ   | Load CICS CSD resource definitions               |
| 2 | INTRDRJ1   | Internal reader test job 1                        |
| 3 | INTRDRJ2   | Internal reader test job 2                        |

### 5.7 Optional Module JCL Jobs (8 jobs)

**Authorization (IMS/DB2/MQ):** `app/app-authorization-ims-db2-mq/jcl/`

| # | JCL Job    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | CBPAUP0J   | Batch purge expired authorization messages       |
| 2 | DBPAUTP0   | Process authorization database                   |
| 3 | LOADPADB   | Load pending authorization IMS database          |
| 4 | UNLDGSAM   | Unload GSAM data                                 |
| 5 | UNLDPADB   | Unload pending authorization IMS database        |

**Transaction Type DB2:** `app/app-transaction-type-db2/jcl/`

| # | JCL Job    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | CREADB21   | Create DB2 tables for transaction types          |
| 2 | MNTTRDB2   | Maintain transaction type DB2 data               |
| 3 | TRANEXTR   | Extract transaction types from DB2               |

---

## 6. Assembler Programs

Located in `app/asm/`

| # | Program    | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | COBDATFT   | Date formatting utility (called by CBACT01C)     |
| 2 | MVSWAIT    | Wait/delay utility (called by COBSWAIT)          |

---

## 7. JCL Procedures

Located in `app/proc/`

| # | Procedure  | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | REPROC     | Reusable procedure for sequential file copy      |
| 2 | TRANREPT   | Transaction report generation procedure          |

---

## 8. Supporting Artifacts

### 8.1 Data Files

| Directory              | Format  | Description                            |
|------------------------|---------|----------------------------------------|
| `app/data/ASCII/`      | ASCII   | Sample data for local testing          |
| `app/data/EBCDIC/`     | EBCDIC  | Sample data for mainframe upload       |

### 8.2 CICS Resource Definitions

| File                   | Description                              |
|------------------------|------------------------------------------|
| `app/csd/CARDDEMO.CSD` | CICS System Definition file             |

### 8.3 Scheduler Configurations

| File                        | Description                          |
|-----------------------------|--------------------------------------|
| `app/scheduler/CardDemo.ca7`      | CA7 job scheduling definitions |
| `app/scheduler/CardDemo.controlm` | Control-M scheduling definitions |

### 8.4 Batch Cycle Execution Order

```
CLOSEFIL → Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
         → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX
         → OPENFIL
```

---

*End of Application Inventory*
