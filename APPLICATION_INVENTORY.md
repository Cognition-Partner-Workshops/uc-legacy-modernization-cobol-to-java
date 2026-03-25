# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
> **Total Artifacts:** 31 COBOL Programs + 30 Copybooks + 38 JCL Jobs + 17 BMS Maps + 2 ASM Programs + 2 JCL Procedures + 13 Optional-Module Programs

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [Optional Module Programs](#optional-module-programs)
4. [Copybooks](#copybooks)
5. [BMS Maps (Screen Definitions)](#bms-maps-screen-definitions)
6. [BMS-Generated Copybooks](#bms-generated-copybooks)
7. [JCL Batch Jobs](#jcl-batch-jobs)
8. [JCL Procedures](#jcl-procedures)
9. [Assembler Programs](#assembler-programs)
10. [Scheduler Configurations](#scheduler-configurations)
11. [Naming Conventions](#naming-conventions)

---

## Executive Summary

CardDemo is a mainframe CICS/COBOL credit card management application designed for modernization workshops. It simulates real-world account management, card operations, transaction processing, bill payments, and reporting. The system supports two user roles: **Regular** (card operations) and **Admin** (user/transaction type management).

| Artifact Type             | Count | Location               |
|---------------------------|------:|------------------------|
| Core COBOL Programs       |    31 | `app/cbl/`             |
| Optional Module Programs  |    13 | `app/app-*/cbl/`       |
| Copybooks (Data)          |    30 | `app/cpy/`             |
| BMS-Generated Copybooks   |    17 | `app/cpy-bms/`         |
| BMS Screen Maps           |    17 | `app/bms/`             |
| JCL Batch Jobs            |    38 | `app/jcl/`             |
| JCL Procedures            |     2 | `app/proc/`            |
| Assembler Programs        |     2 | `app/asm/`             |
| Scheduler Configs         |     2 | `app/scheduler/`       |

---

## COBOL Programs (Core)

### Online CICS Programs (CO* prefix)

These programs run under CICS and handle interactive 3270 terminal sessions.

| # | Program     | Lines | Type        | Function                                   | CICS Txn | BMS Map  | Domain           |
|---|-------------|------:|-------------|--------------------------------------------|----------|----------|------------------|
| 1 | COSGN00C    |   260 | Online CICS | User Sign-on / Authentication              | CC00     | COSGN00  | Security         |
| 2 | COMEN01C    |   308 | Online CICS | Main Menu Navigation                       | CM00     | COMEN01  | Navigation       |
| 3 | COADM01C    |   288 | Online CICS | Admin Menu Navigation                      | CA00     | COADM01  | Admin            |
| 4 | COACTVWC    |   941 | Online CICS | View Account Details                       | CA01     | COACTVW  | Account Mgmt     |
| 5 | COACTUPC    | 4,236 | Online CICS | Update Account Details                     | CA02     | COACTUP  | Account Mgmt     |
| 6 | COCRDLIC    | 1,459 | Online CICS | List Credit Cards (browsable)              | CC01     | COCRDLI  | Card Mgmt        |
| 7 | COCRDSLC    |   887 | Online CICS | View Credit Card Details                   | CC02     | COCRDSL  | Card Mgmt        |
| 8 | COCRDUPC    | 1,560 | Online CICS | Update Credit Card Details                 | CC03     | COCRDUP  | Card Mgmt        |
| 9 | COTRN00C    |   699 | Online CICS | List Transactions (browsable)              | CT00     | COTRN00  | Transaction Mgmt |
|10 | COTRN01C    |   330 | Online CICS | View Transaction Details                   | CT01     | COTRN01  | Transaction Mgmt |
|11 | COTRN02C    |   783 | Online CICS | Add New Transaction                        | CT02     | COTRN02  | Transaction Mgmt |
|12 | CORPT00C    |   649 | Online CICS | Transaction Report Request                 | CR00     | CORPT00  | Reporting        |
|13 | COBIL00C    |   572 | Online CICS | Bill Payment Processing                    | CB00     | COBIL00  | Bill Payment     |
|14 | COUSR00C    |   695 | Online CICS | List Users (Admin, browsable)              | CU00     | COUSR00  | User Admin       |
|15 | COUSR01C    |   299 | Online CICS | Add New User (Admin)                       | CU01     | COUSR01  | User Admin       |
|16 | COUSR02C    |   414 | Online CICS | Update User (Admin)                        | CU02     | COUSR02  | User Admin       |
|17 | COUSR03C    |   359 | Online CICS | Delete User (Admin)                        | CU03     | COUSR03  | User Admin       |

### Batch Programs (CB* prefix)

These programs run as batch jobs, typically scheduled in nightly or periodic processing cycles.

| # | Program     | Lines | Type           | Function                                            | Domain           |
|---|-------------|------:|----------------|-----------------------------------------------------|------------------|
|18 | CBACT01C    |   430 | Batch          | Read Account File & Write to Multiple Output Files  | Account Mgmt     |
|19 | CBACT02C    |   178 | Batch          | Read Card File (data extraction)                    | Card Mgmt        |
|20 | CBACT03C    |   178 | Batch          | Read Card Cross-Reference File                      | Card Mgmt        |
|21 | CBACT04C    |   652 | Batch          | Interest Calculation & Posting                      | Financial Calc   |
|22 | CBCUS01C    |   178 | Batch          | Read Customer File (data extraction)                | Customer Mgmt    |
|23 | CBTRN01C    |   494 | Batch          | Read Daily Transactions                             | Transaction Mgmt |
|24 | CBTRN02C    |   731 | Batch          | Transaction Posting (core batch processing)         | Transaction Mgmt |
|25 | CBTRN03C    |   649 | Batch          | Daily Transaction Report Generation                 | Reporting        |
|26 | CBSTM03A    |   924 | Batch          | Account Statement Generation (text + HTML)          | Reporting        |
|27 | CBSTM03B    |   230 | Batch Subrtn   | File I/O Subroutine for Statement Generation        | Reporting        |
|28 | CBEXPORT    |   582 | Batch          | Export Data from VSAM to Sequential Files            | Data Mgmt        |
|29 | CBIMPORT    |   487 | Batch          | Import Data from Sequential Files to VSAM            | Data Mgmt        |
|30 | COBSWAIT    |    41 | Batch Utility  | Wait/Sleep Utility (calls MVSWAIT ASM)              | Infrastructure   |

### Utility Programs

| # | Program     | Lines | Type           | Function                                            | Domain           |
|---|-------------|------:|----------------|-----------------------------------------------------|------------------|
|31 | CSUTLDTC    |   157 | Utility        | Date Conversion via CEEDAYS LE API                  | Infrastructure   |

---

## Optional Module Programs

### Authorization Module (IMS/DB2/MQ) -- `app/app-authorization-ims-db2-mq/`

| # | Program     | Type                 | Function                                      | Domain          |
|---|-------------|----------------------|-----------------------------------------------|-----------------|
| 1 | COPAUA0C    | CICS IMS MQ          | Card Authorization Decision (MQ trigger)      | Authorization   |
| 2 | COPAUS0C    | CICS IMS BMS         | Authorization Messages Summary View           | Authorization   |
| 3 | COPAUS1C    | CICS IMS BMS         | Authorization Message Detail View             | Authorization   |
| 4 | COPAUS2C    | CICS IMS DB2         | Mark Authorization Message as Fraud (DB2)     | Authorization   |
| 5 | CBPAUP0C    | Batch IMS            | Purge Expired Pending Authorization Messages  | Authorization   |
| 6 | DBUNLDGS    | Batch IMS            | IMS Database Unload Utility                   | Data Mgmt       |
| 7 | PAUDBLOD    | Batch IMS            | IMS Database Load Utility                     | Data Mgmt       |
| 8 | PAUDBUNL    | Batch IMS            | IMS Database Unload (alternate)               | Data Mgmt       |

### Transaction Type DB2 Module -- `app/app-transaction-type-db2/`

| # | Program     | Type                 | Function                                      | Domain          |
|---|-------------|----------------------|-----------------------------------------------|-----------------|
| 9 | COTRTUPC    | CICS DB2 BMS         | Add/Edit Transaction Type (DB2 CRUD)          | Transaction Cfg |
|10 | COTRTLIC    | CICS DB2 BMS         | List/Delete Transaction Types (DB2 cursors)   | Transaction Cfg |
|11 | COBTUPDT    | Batch DB2            | Batch Update Transaction Types                | Transaction Cfg |

### VSAM-MQ Module -- `app/app-vsam-mq/`

| # | Program     | Type                 | Function                                      | Domain          |
|---|-------------|----------------------|-----------------------------------------------|-----------------|
|12 | CODATE01    | CICS MQ              | System Date Request/Response via MQ           | Infrastructure  |
|13 | COACCT01    | CICS MQ              | Account Inquiry Request/Response via MQ       | Account Mgmt    |

---

## Copybooks

### Data Structure Copybooks (CV* prefix = VSAM record layouts)

| # | Copybook    | Record Name              | Record Len | Function                                 | Domain           |
|---|-------------|--------------------------|------------|------------------------------------------|------------------|
| 1 | CVACT01Y    | ACCOUNT-RECORD           | 300 bytes  | Account master record layout             | Account          |
| 2 | CVACT02Y    | CARD-RECORD              | 150 bytes  | Credit card master record layout         | Card             |
| 3 | CVACT03Y    | CARD-XREF-RECORD         | 50 bytes   | Card-to-Account cross-reference          | Card/Account     |
| 4 | CVCRD01Y    | CARD-DETAIL-RECORD       | variable   | Card detail for online display           | Card             |
| 5 | CVCUS01Y    | CUSTOMER-RECORD          | 500 bytes  | Customer master record layout            | Customer         |
| 6 | CVTRA01Y    | TRAN-CAT-BAL-RECORD      | 50 bytes   | Transaction category balance             | Transaction      |
| 7 | CVTRA02Y    | DIS-GROUP-RECORD         | 50 bytes   | Disclosure group / interest rate         | Financial        |
| 8 | CVTRA03Y    | TRAN-TYPE-RECORD         | 60 bytes   | Transaction type master                  | Transaction      |
| 9 | CVTRA04Y    | TRAN-CAT-RECORD          | 60 bytes   | Transaction category type                | Transaction      |
|10 | CVTRA05Y    | TRAN-RECORD              | 350 bytes  | Online transaction record (master)       | Transaction      |
|11 | CVTRA06Y    | DALYTRAN-RECORD          | 350 bytes  | Daily transaction record (input)         | Transaction      |
|12 | CVTRA07Y    | REPORT structures         | variable   | Transaction report layout structures     | Reporting        |
|13 | CVEXPORT    | EXPORT-RECORD            | variable   | Export/import record layout              | Data Mgmt        |
|14 | COSTM01     | TRNX-RECORD              | 350 bytes  | Statement reporting transaction layout   | Reporting        |
|15 | CUSTREC     | CUSTOMER-RECORD (alt)    | variable   | Alternate customer record definition     | Customer         |

### Common / Infrastructure Copybooks (CO*, CS* prefix)

| # | Copybook    | Record Name              | Function                                        | Domain           |
|---|-------------|--------------------------|------------------------------------------------|------------------|
|16 | COCOM01Y    | CARDDEMO-COMMAREA        | CICS Communication Area (screen navigation)    | Navigation       |
|17 | COMEN02Y    | CARDDEMO-MENU-ENTRIES    | Menu option definitions                        | Navigation       |
|18 | COADM02Y    | CARDDEMO-ADMIN-ENTRIES   | Admin menu option definitions                  | Navigation       |
|19 | COTTL01Y    | CARDDEMO-TITLE           | Screen title/header definitions                | UI               |
|20 | CSUSR01Y    | SEC-USER-DATA            | User security record                           | Security         |
|21 | CSDAT01Y    | WS-DATE-DATA             | Date formatting working storage                | Infrastructure   |
|22 | CSMSG01Y    | WS-MSG-DATA              | Standard message area                          | UI               |
|23 | CSMSG02Y    | WS-MISC-MSG-DATA         | Miscellaneous message area                     | UI               |
|24 | CSSETATY    | (inline macro)           | Set field attribute (REPLACING pattern)        | UI               |
|25 | CSSTRPFY    | (inline procedure)       | String strip/format utility                    | Infrastructure   |
|26 | CSUTLDPY    | (inline procedure)       | Date utility procedure                         | Infrastructure   |
|27 | CSUTLDWY    | WS-DATE-UTIL-DATA        | Date utility working storage                   | Infrastructure   |
|28 | CSLKPCDY    | WS-LOOKUP-DATA           | Lookup code data                               | Infrastructure   |
|29 | CODATECN    | WS-DATE-CONVERSION       | Date conversion working storage                | Infrastructure   |
|30 | UNUSED1Y    | UNUSED-DATA              | Unused/placeholder record                      | N/A              |

---

## BMS Maps (Screen Definitions)

Each BMS map defines a 3270 terminal screen layout. These map directly to potential web UI forms in a modernized application.

| # | BMS Map     | Screen Title                    | Used By Program | Domain           |
|---|-------------|---------------------------------|-----------------|------------------|
| 1 | COSGN00.bms | Login Screen                   | COSGN00C        | Security         |
| 2 | COMEN01.bms | Main Menu Screen               | COMEN01C        | Navigation       |
| 3 | COADM01.bms | Admin Menu Screen              | COADM01C        | Admin            |
| 4 | COACTVW.bms | Account View Screen            | COACTVWC        | Account Mgmt     |
| 5 | COACTUP.bms | Account Update Screen          | COACTUPC        | Account Mgmt     |
| 6 | COCRDLI.bms | Card Listing Screen            | COCRDLIC        | Card Mgmt        |
| 7 | COCRDSL.bms | Card Selection/View Screen     | COCRDSLC        | Card Mgmt        |
| 8 | COCRDUP.bms | Card Update Screen             | COCRDUPC        | Card Mgmt        |
| 9 | COTRN00.bms | Transaction List Screen        | COTRN00C        | Transaction Mgmt |
|10 | COTRN01.bms | Transaction View Screen        | COTRN01C        | Transaction Mgmt |
|11 | COTRN02.bms | Transaction Add Screen         | COTRN02C        | Transaction Mgmt |
|12 | CORPT00.bms | Transaction Report Screen      | CORPT00C        | Reporting        |
|13 | COBIL00.bms | Bill Payment Screen            | COBIL00C        | Bill Payment     |
|14 | COUSR00.bms | List Users Screen              | COUSR00C        | User Admin       |
|15 | COUSR01.bms | Add User Screen                | COUSR01C        | User Admin       |
|16 | COUSR02.bms | Update User Screen             | COUSR02C        | User Admin       |
|17 | COUSR03.bms | Delete User Screen             | COUSR03C        | User Admin       |

---

## BMS-Generated Copybooks

These are auto-generated COBOL copybooks from BMS map compilation. They contain the symbolic map data structures used in SEND MAP / RECEIVE MAP operations.

| # | Copybook        | Generated From  |
|---|-----------------|-----------------|
| 1 | COSGN00.CPY     | COSGN00.bms     |
| 2 | COMEN01.CPY     | COMEN01.bms     |
| 3 | COADM01.CPY     | COADM01.bms     |
| 4 | COACTVW.CPY     | COACTVW.bms     |
| 5 | COACTUP.CPY     | COACTUP.bms     |
| 6 | COCRDLI.CPY     | COCRDLI.bms     |
| 7 | COCRDSL.CPY     | COCRDSL.bms     |
| 8 | COCRDUP.CPY     | COCRDUP.bms     |
| 9 | COTRN00.CPY     | COTRN00.bms     |
|10 | COTRN01.CPY     | COTRN01.bms     |
|11 | COTRN02.CPY     | COTRN02.bms     |
|12 | CORPT00.CPY     | CORPT00.bms     |
|13 | COBIL00.CPY     | COBIL00.bms     |
|14 | COUSR00.CPY     | COUSR00.bms     |
|15 | COUSR01.CPY     | COUSR01.bms     |
|16 | COUSR02.CPY     | COUSR02.bms     |
|17 | COUSR03.CPY     | COUSR03.bms     |

---

## JCL Batch Jobs

### Data Refresh / Load Jobs

| # | JCL Job     | Function                                           | Programs/Utilities   |
|---|-------------|----------------------------------------------------|-----------------------|
| 1 | ACCTFILE    | Refresh Account Master VSAM from flat file         | IDCAMS (REPRO)       |
| 2 | CARDFILE    | Refresh Card Master VSAM from flat file            | IDCAMS (REPRO)       |
| 3 | CUSTFILE    | Refresh Customer Master VSAM from flat file        | IDCAMS (REPRO)       |
| 4 | XREFFILE    | Load Card Cross-Reference VSAM + AIX              | IDCAMS (DEFINE/REPRO/BLDINDEX) |
| 5 | TRANFILE    | Load Transaction Master VSAM from flat file        | IDCAMS (REPRO)       |
| 6 | DUSRSECJ    | Load User Security VSAM file                       | IDCAMS (DEFINE/REPRO)|
| 7 | DEFCUST     | Define Customer VSAM cluster                       | IDCAMS (DEFINE)      |

### Batch Processing Jobs

| # | JCL Job     | Function                                           | Programs Executed     |
|---|-------------|----------------------------------------------------|-----------------------|
| 8 | POSTTRAN    | Core transaction posting cycle                     | CBTRN02C             |
| 9 | INTCALC     | Interest calculation cycle                         | CBACT04C             |
|10 | COMBTRAN    | Combine daily transactions into master             | SORT + IDCAMS        |
|11 | CREASTMT    | Generate account statements (text + HTML)          | SORT + CBSTM03A      |
|12 | TRANREPT    | Generate daily transaction report                  | CBTRN03C             |
|13 | TRANBKP     | Backup transaction file                            | IDCAMS (REPRO)       |
|14 | TRANIDX     | Define/build alternate index on transactions       | IDCAMS               |

### CICS File Control Jobs

| # | JCL Job     | Function                                           | Programs/Utilities   |
|---|-------------|----------------------------------------------------|-----------------------|
|15 | CLOSEFIL    | Close CICS files for batch processing              | DFHCSDUP             |
|16 | OPENFIL     | Reopen CICS files after batch processing           | DFHCSDUP             |

### VSAM Definition / Utility Jobs

| # | JCL Job     | Function                                           | Programs/Utilities   |
|---|-------------|----------------------------------------------------|-----------------------|
|17 | DEFGDGB     | Define GDG (Generation Data Group) base            | IDCAMS               |
|18 | DEFGDGD     | Define GDG data clusters                           | IDCAMS               |
|19 | DISCGRP     | Define/load Disclosure Group VSAM                  | IDCAMS               |
|20 | ESDSRRDS    | Define ESDS/RRDS VSAM clusters                     | IDCAMS               |
|21 | TCATBALF    | Define/load Transaction Category Balance VSAM      | IDCAMS               |
|22 | TRANCATG    | Define/load Transaction Category VSAM              | IDCAMS               |
|23 | TRANTYPE    | Define/load Transaction Type VSAM                  | IDCAMS               |
|24 | REPTFILE    | Define Report output file                          | IDCAMS               |
|25 | DALYREJS    | Define Daily Rejects sequential file               | IDCAMS               |
|26 | PRTCATBL    | Print Category Balance records                     | IDCAMS (PRINT)       |

### Data Read / Verification Jobs

| # | JCL Job     | Function                                           | Programs/Utilities   |
|---|-------------|----------------------------------------------------|-----------------------|
|27 | READACCT    | Read/verify Account VSAM file                      | CBACT01C             |
|28 | READCARD    | Read/verify Card VSAM file                         | CBACT02C             |
|29 | READCUST    | Read/verify Customer VSAM file                     | CBCUS01C             |
|30 | READXREF    | Read/verify Card Cross-Reference VSAM file         | CBACT03C             |

### Export / Import Jobs

| # | JCL Job     | Function                                           | Programs Executed     |
|---|-------------|----------------------------------------------------|-----------------------|
|31 | CBEXPORT    | Export VSAM data to sequential files               | CBEXPORT             |
|32 | CBIMPORT    | Import sequential files to VSAM                    | CBIMPORT             |

### Utility / Infrastructure Jobs

| # | JCL Job     | Function                                           | Programs/Utilities   |
|---|-------------|----------------------------------------------------|-----------------------|
|33 | WAITSTEP    | Execute wait/delay step                            | COBSWAIT             |
|34 | TXT2PDF1    | Convert text statement to PDF                      | TXT2PDF (REXX)       |
|35 | FTPJCL      | FTP file transfer to/from mainframe                | FTP                  |
|36 | INTRDRJ1    | Internal Reader - trigger second JCL               | IDCAMS + IEBGENER    |
|37 | INTRDRJ2    | Internal Reader - triggered by INTRDRJ1            | IDCAMS               |
|38 | CBADMCDJ    | Admin card processing job                          | IDCAMS               |

---

## JCL Procedures

| # | Procedure     | Function                                        |
|---|---------------|-------------------------------------------------|
| 1 | REPROC.prc    | Reusable report generation procedure            |
| 2 | TRANREPT.prc  | Transaction report procedure                    |

---

## Assembler Programs

| # | Program       | Function                                        | Called By    |
|---|---------------|-------------------------------------------------|--------------|
| 1 | MVSWAIT.asm   | MVS Wait macro (timer delay)                    | COBSWAIT     |
| 2 | COBDATFT.asm  | Date formatting utility                         | Various      |

---

## Scheduler Configurations

| # | File                  | Scheduler  | Function                                |
|---|----------------------|------------|-----------------------------------------|
| 1 | CardDemo.ca7         | CA7        | Job scheduling definitions for CA7      |
| 2 | CardDemo.controlm    | Control-M  | Job scheduling definitions for Control-M|

---

## Naming Conventions

| Prefix/Pattern | Meaning                                         | Example    |
|----------------|------------------------------------------------|------------|
| `CO*`          | Online CICS program                             | COSGN00C   |
| `CB*`          | Batch COBOL program                             | CBTRN02C   |
| `CS*`          | Common/shared utility copybook or program       | CSUTLDTC   |
| `CV*`          | VSAM record layout copybook                     | CVACT01Y   |
| `*Y` suffix    | Copybook (data structure)                       | CSUSR01Y   |
| `*C` suffix    | COBOL program                                   | COACTUPC   |
| BMS name       | Matches program name minus the trailing `C`     | COACTUP    |
| JCL name       | Often matches the VSAM file or function name    | ACCTFILE   |

### Domain Classification Summary

| Domain              | Online Programs | Batch Programs | Copybooks | JCL Jobs |
|---------------------|----------------:|---------------:|----------:|---------:|
| Security            |               1 |              0 |         1 |        1 |
| Navigation          |               2 |              0 |         2 |        0 |
| Account Management  |               2 |              1 |         1 |        2 |
| Card Management     |               3 |              2 |         3 |        2 |
| Transaction Mgmt    |               3 |              3 |         6 |        6 |
| Reporting           |               1 |              2 |         2 |        3 |
| Bill Payment        |               1 |              0 |         0 |        0 |
| User Admin          |               4 |              0 |         0 |        1 |
| Financial Calc      |               0 |              1 |         1 |        1 |
| Data Management     |               0 |              2 |         1 |        9 |
| Infrastructure      |               0 |              1 |         6 |       10 |
| UI/Common           |               0 |              0 |         5 |        0 |
