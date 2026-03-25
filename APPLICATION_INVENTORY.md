# CardDemo Application Inventory

> Comprehensive catalog of all programs, copybooks, JCL jobs, BMS maps, and supporting artifacts in the CardDemo mainframe credit card management application.

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [COBOL Programs (Optional Modules)](#cobol-programs-optional-modules)
4. [Copybooks (Data Structures)](#copybooks-data-structures)
5. [BMS Screen Maps](#bms-screen-maps)
6. [BMS-Generated Copybooks](#bms-generated-copybooks)
7. [JCL Batch Jobs](#jcl-batch-jobs)
8. [JCL Procedures](#jcl-procedures)
9. [Assembler Programs](#assembler-programs)
10. [Supporting Artifacts](#supporting-artifacts)

---

## Summary Statistics

| Artifact Type              | Count |
|----------------------------|-------|
| Core COBOL Programs        | 31    |
| Optional Module Programs   | 13    |
| Copybooks (Data)           | 30    |
| Copybooks (BMS-Generated)  | 17    |
| BMS Screen Maps            | 17    |
| JCL Batch Jobs             | 38    |
| JCL Procedures             | 2     |
| Assembler Programs         | 2     |
| **Total Artifacts**        | **150** |

---

## COBOL Programs (Core)

### Online CICS Programs (17 programs)

These programs run under CICS and handle interactive 3270 terminal sessions. Naming convention: `CO*` prefix.

| # | Program    | Lines | Function                                  | CICS Trans | Business Domain    | Layer             |
|---|------------|-------|-------------------------------------------|------------|--------------------|-------------------|
| 1 | COSGN00C   | 260   | Sign-on / Authentication                  | CC00       | Security           | Presentation      |
| 2 | COMEN01C   | 308   | Main Menu (Regular Users)                 | CM00       | Navigation         | Presentation      |
| 3 | COADM01C   | 288   | Admin Menu                                | CA00       | Navigation         | Presentation      |
| 4 | COACTVWC   | 941   | View Account Details                      | CA01       | Account Mgmt       | Business Logic    |
| 5 | COACTUPC   | 4,236 | Update Account / Customer Details         | CA02       | Account Mgmt       | Business Logic    |
| 6 | COCRDLIC   | 1,459 | List Credit Cards (with paging)           | CC01       | Card Mgmt          | Business Logic    |
| 7 | COCRDSLC   | 887   | View Credit Card Details                  | CC02       | Card Mgmt          | Business Logic    |
| 8 | COCRDUPC   | 1,560 | Update Credit Card Details                | CC03       | Card Mgmt          | Business Logic    |
| 9 | COTRN00C   | 699   | List Transactions (with paging)           | CT00       | Transactions       | Business Logic    |
| 10| COTRN01C   | 330   | View Transaction Detail                   | CT01       | Transactions       | Business Logic    |
| 11| COTRN02C   | 783   | Add New Transaction                       | CT02       | Transactions       | Business Logic    |
| 12| CORPT00C   | 649   | Transaction Reports (submits batch via TDQ)| CR00      | Reporting          | Business Logic    |
| 13| COBIL00C   | 572   | Bill Payment Processing                   | CB00       | Billing            | Business Logic    |
| 14| COUSR00C   | 695   | List Users (Admin)                        | CU00       | User Admin         | Business Logic    |
| 15| COUSR01C   | 299   | Add User (Admin)                          | CU01       | User Admin         | Business Logic    |
| 16| COUSR02C   | 414   | Update User (Admin)                       | CU02       | User Admin         | Business Logic    |
| 17| COUSR03C   | 359   | Delete User (Admin)                       | CU03       | User Admin         | Business Logic    |

### Batch Programs (13 programs)

Batch programs run in JCL-submitted jobs for scheduled processing. Naming convention: `CB*` prefix.

| # | Program    | Lines | Function                                      | Business Domain    | I/O Pattern          |
|---|------------|-------|-----------------------------------------------|--------------------|----------------------|
| 1 | CBACT01C   | 430   | Read & Display Account File                   | Account Mgmt       | Sequential Read      |
| 2 | CBACT02C   | 178   | Read & Display Card Data File                 | Card Mgmt          | Sequential Read      |
| 3 | CBACT03C   | 178   | Read & Display Card Cross-Reference File      | Card Mgmt          | Sequential Read      |
| 4 | CBACT04C   | 652   | Interest Calculation on Accounts              | Financial Calc     | Read/Update          |
| 5 | CBCUS01C   | 178   | Read & Display Customer File                  | Customer Mgmt      | Sequential Read      |
| 6 | CBTRN01C   | 494   | Read & Display Transaction File               | Transactions       | Sequential Read      |
| 7 | CBTRN02C   | 731   | Post Daily Transactions to Master             | Transactions       | Read/Write/Update    |
| 8 | CBTRN03C   | 649   | Generate Daily Transaction Report             | Reporting          | Read/Write           |
| 9 | CBSTM03A   | 924   | Generate Account Statements (Text + HTML)     | Reporting          | Multi-file Read/Write|
| 10| CBSTM03B   | 230   | Statement File I/O Subroutine (called by 03A) | Reporting          | Subroutine           |
| 11| CBEXPORT   | 582   | Export All VSAM Data to Flat File             | Data Migration     | Multi-file Read/Write|
| 12| CBIMPORT   | 487   | Import Flat File Data into VSAM               | Data Migration     | Read/Multi-Write     |
| 13| COBSWAIT   | 41    | Wait/Delay Utility                            | Utility            | None                 |

### Utility Programs (1 program)

| # | Program    | Lines | Function                                      | Business Domain    |
|---|------------|-------|-----------------------------------------------|--------------------|
| 1 | CSUTLDTC   | 157   | Date Conversion Utility (calls CEEDAYS/CEEDATM) | Utility          |

---

## COBOL Programs (Optional Modules)

### Authorization Module (IMS/DB2/MQ) -- 8 programs

Located in `app/app-authorization-ims-db2-mq/cbl/`. Integrates IMS DB, DB2, and MQ Series for card authorization.

| # | Program    | Type                   | Function                                        |
|---|------------|------------------------|-------------------------------------------------|
| 1 | COPAUA0C   | CICS + IMS + MQ        | Card Authorization Decision (MQ Trigger)        |
| 2 | COPAUS0C   | CICS + IMS + BMS       | Summary View of Authorization Messages          |
| 3 | COPAUS1C   | CICS + IMS + BMS       | Detail View of Authorization Message            |
| 4 | COPAUS2C   | CICS + IMS + DB2       | Mark Authorization Message as Fraud (to DB2)    |
| 5 | CBPAUP0C   | Batch + IMS            | Purge Expired Pending Authorization Messages    |
| 6 | DBUNLDGS   | Batch                  | IMS Database Unload                             |
| 7 | PAUDBLOD   | Batch                  | IMS Database Load                               |
| 8 | PAUDBUNL   | Batch                  | IMS Database Unload (alternate)                 |

### Transaction Type DB2 Module -- 3 programs

Located in `app/app-transaction-type-db2/cbl/`. Demonstrates DB2 cursors and embedded SQL.

| # | Program    | Type            | Function                                          |
|---|------------|-----------------|---------------------------------------------------|
| 1 | COTRTUPC   | CICS + DB2      | Add/Update Transaction Type (DB2 CRUD)            |
| 2 | COTRTLIC   | CICS + DB2      | List/Delete Transaction Types (DB2 cursor paging) |
| 3 | COBTUPDT   | Batch + DB2     | Batch Update Transaction Types                    |

### VSAM-MQ Module -- 2 programs

Located in `app/app-vsam-mq/cbl/`. MQ-based request/response for system date and account inquiry.

| # | Program    | Type            | Function                                          |
|---|------------|-----------------|---------------------------------------------------|
| 1 | CODATE01   | CICS + MQ       | System Date Service (CDRD queue)                  |
| 2 | COACCT01   | CICS + MQ       | Account Inquiry Service (CDRA queue)              |

---

## Copybooks (Data Structures)

All located in `app/cpy/`. Naming convention: `CV*` = VSAM data structures, `CS*` = system/utility, `CO*` = common areas.

### Business Entity Copybooks

| # | Copybook   | Record Length | Business Entity              | Key Fields                          |
|---|------------|--------------|------------------------------|-------------------------------------|
| 1 | CVACT01Y   | 300 bytes    | Account Master Record        | ACCT-ID (11 digits)                |
| 2 | CVACT02Y   | 150 bytes    | Card Data Record             | CARD-NUM (16 chars)                |
| 3 | CVACT03Y   | 50 bytes     | Card Cross-Reference         | XREF-CARD-NUM / XREF-ACCT-ID      |
| 4 | CVCUS01Y   | 500 bytes    | Customer Master Record       | CUST-ID (9 digits)                 |
| 5 | CVCRD01Y   | 150 bytes    | Card Record (alternate)      | CARD-NUM (16 chars)                |
| 6 | CVTRA01Y   | 50 bytes     | Transaction Category Balance | ACCT-ID + TYPE-CD + CAT-CD         |
| 7 | CVTRA02Y   | 50 bytes     | Disclosure Group             | GROUP-ID + TRAN-TYPE + CAT-CD      |
| 8 | CVTRA03Y   | 60 bytes     | Transaction Type             | TRAN-TYPE (2 chars)                |
| 9 | CVTRA04Y   | 60 bytes     | Transaction Category Type    | TRAN-TYPE-CD + TRAN-CAT-CD         |
| 10| CVTRA05Y   | 350 bytes    | Transaction Record (Online)  | TRAN-ID (16 chars)                 |
| 11| CVTRA06Y   | 350 bytes    | Daily Transaction Record     | DALYTRAN-ID (16 chars)             |
| 12| CVTRA07Y   | N/A          | Transaction Report Layout    | Report headers/detail/totals        |
| 13| CUSTREC    | 500 bytes    | Customer Record (alternate)  | CUST-ID                            |
| 14| COSTM01    | 350 bytes    | Transaction for Statements   | CARD-NUM + TRAN-ID (composite key) |
| 15| CVEXPORT   | varies       | Export/Import Data Record    | Multi-entity export format          |

### System/Utility Copybooks

| # | Copybook   | Function                                          |
|---|------------|---------------------------------------------------|
| 16| COCOM01Y   | Common Communication Area (COMMAREA / DFHCOMMAREA) |
| 17| COADM02Y   | Admin Menu Option Definitions                     |
| 18| COMEN02Y   | Regular User Menu Option Definitions              |
| 19| COTTL01Y   | Screen Title and Header Definitions               |
| 20| CSUSR01Y   | User Security Record (authentication)             |
| 21| CSDAT01Y   | Date Work Areas                                   |
| 22| CSLKPCDY   | Lookup Code Table                                 |
| 23| CSMSG01Y   | System Messages (type 1)                          |
| 24| CSMSG02Y   | System Messages (type 2)                          |
| 25| CSSETATY   | Screen Attribute Setting Definitions              |
| 26| CSSTRPFY   | String Manipulation Utility Definitions           |
| 27| CSUTLDPY   | Date Utility Parameters                           |
| 28| CSUTLDWY   | Date Utility Working Storage                      |
| 29| CODATECN   | Date Conversion Routines                          |
| 30| UNUSED1Y   | Unused/Legacy Data Structure (candidate for removal) |

---

## BMS Screen Maps

All located in `app/bms/`. Each defines a 3270 terminal screen layout (24x80 characters).

| # | Map Name | Map Set  | Associated Program | Screen Title           | Fields |
|---|----------|----------|--------------------|------------------------|--------|
| 1 | COSGN00  | COSGN00  | COSGN00C           | Sign On                | User ID, Password |
| 2 | COMEN01  | COMEN01  | COMEN01C           | Main Menu              | Menu Options (1-10) |
| 3 | COADM01  | COADM01  | COADM01C           | Admin Menu             | Admin Menu Options |
| 4 | COACTVW  | COACTVW  | COACTVWC           | Account View           | Account/Customer details (read-only) |
| 5 | COACTUP  | COACTUP  | COACTUPC           | Account Update         | Account/Customer fields (editable) |
| 6 | COCRDLI  | COCRDLI  | COCRDLIC           | Card List              | Card number, status (pageable list) |
| 7 | COCRDSL  | COCRDSL  | COCRDSLC           | Card Detail View       | Card details (read-only) |
| 8 | COCRDUP  | COCRDUP  | COCRDUPC           | Card Update            | Card fields (editable) |
| 9 | COTRN00  | COTRN00  | COTRN00C           | Transaction List       | Transaction list (pageable) |
| 10| COTRN01  | COTRN01  | COTRN01C           | Transaction View       | Transaction details (read-only) |
| 11| COTRN02  | COTRN02  | COTRN02C           | Transaction Add        | New transaction fields |
| 12| CORPT00  | CORPT00  | CORPT00C           | Transaction Report     | Date range, report options |
| 13| COBIL00  | COBIL00  | COBIL00C           | Bill Payment           | Payment amount, account |
| 14| COUSR00  | COUSR00  | COUSR00C           | User List (Admin)      | User list (pageable) |
| 15| COUSR01  | COUSR01  | COUSR01C           | Add User (Admin)       | User ID, name, password, type |
| 16| COUSR02  | COUSR02  | COUSR02C           | Update User (Admin)    | User fields (editable) |
| 17| COUSR03  | COUSR03  | COUSR03C           | Delete User (Admin)    | User details (confirm delete) |

---

## BMS-Generated Copybooks

Located in `app/cpy-bms/`. Auto-generated from BMS maps; contain symbolic field definitions for COBOL programs.

| # | Copybook   | Source BMS Map | Contains                                     |
|---|------------|----------------|----------------------------------------------|
| 1 | COSGN00.CPY | COSGN00.bms   | Sign-on screen field definitions             |
| 2 | COMEN01.CPY | COMEN01.bms   | Main menu field definitions                  |
| 3 | COADM01.CPY | COADM01.bms   | Admin menu field definitions                 |
| 4 | COACTVW.CPY | COACTVW.bms   | Account view field definitions               |
| 5 | COACTUP.CPY | COACTUP.bms   | Account update field definitions             |
| 6 | COCRDLI.CPY | COCRDLI.bms   | Card list field definitions                  |
| 7 | COCRDSL.CPY | COCRDSL.bms   | Card detail view field definitions           |
| 8 | COCRDUP.CPY | COCRDUP.bms   | Card update field definitions                |
| 9 | COTRN00.CPY | COTRN00.bms   | Transaction list field definitions           |
| 10| COTRN01.CPY | COTRN01.bms   | Transaction view field definitions           |
| 11| COTRN02.CPY | COTRN02.bms   | Transaction add field definitions            |
| 12| CORPT00.CPY | CORPT00.bms   | Report screen field definitions              |
| 13| COBIL00.CPY | COBIL00.bms   | Bill payment field definitions               |
| 14| COUSR00.CPY | COUSR00.bms   | User list field definitions                  |
| 15| COUSR01.CPY | COUSR01.bms   | Add user field definitions                   |
| 16| COUSR02.CPY | COUSR02.bms   | Update user field definitions                |
| 17| COUSR03.CPY | COUSR03.bms   | Delete user field definitions                |

---

## JCL Batch Jobs

All located in `app/jcl/`. Organized by functional category.

### Data File Refresh Jobs (Load flat files into VSAM)

| # | Job Name   | Function                                          | Program/Utility     | Key Datasets                          |
|---|------------|---------------------------------------------------|---------------------|---------------------------------------|
| 1 | ACCTFILE   | Define & load Account Master VSAM                 | IDCAMS              | ACCTDATA.VSAM.KSDS                   |
| 2 | CARDFILE   | Define & load Card Data VSAM                      | IDCAMS              | CARDDATA.VSAM.KSDS                   |
| 3 | CUSTFILE   | Define & load Customer Master VSAM                | IDCAMS              | CUSTDATA.VSAM.KSDS                   |
| 4 | XREFFILE   | Define & load Card Cross-Reference VSAM + AIX     | IDCAMS              | CARDXREF.VSAM.KSDS + AIX             |
| 5 | TRANFILE   | Define & load Transaction Master VSAM             | IDCAMS              | TRANSACT.VSAM.KSDS                   |
| 6 | DUSRSECJ   | Define & load User Security VSAM                  | IDCAMS              | USRSEC.VSAM.KSDS                     |
| 7 | DISCGRP    | Define & load Disclosure Group VSAM               | IDCAMS              | DISCGRP.VSAM.KSDS                    |
| 8 | TRANTYPE   | Define & load Transaction Type VSAM               | IDCAMS              | TRANTYPE.VSAM.KSDS                   |
| 9 | TRANCATG   | Define & load Transaction Category VSAM           | IDCAMS              | TRANCATG.VSAM.KSDS                   |
| 10| TCATBALF   | Define & load Transaction Category Balance VSAM   | IDCAMS              | TCATBALF.VSAM.KSDS                   |
| 11| DEFCUST    | Define Customer VSAM cluster only (no load)       | IDCAMS              | CUSTDATA.VSAM.KSDS                   |

### CICS File Management Jobs

| # | Job Name   | Function                                          | Program/Utility     |
|---|------------|---------------------------------------------------|---------------------|
| 12| CLOSEFIL   | Close all CICS-managed VSAM files                 | DFHCSDUP            |
| 13| OPENFIL    | Open all CICS-managed VSAM files                  | DFHCSDUP            |

### Batch Processing Jobs (Core Business Logic)

| # | Job Name   | Function                                          | Program Executed    | Key Input/Output                      |
|---|------------|---------------------------------------------------|---------------------|---------------------------------------|
| 14| POSTTRAN   | Post daily transactions to master                 | CBTRN02C            | DALYTRAN -> TRANSACT                  |
| 15| INTCALC    | Calculate interest on accounts                    | CBACT04C            | TRANSACT + DISCGRP -> TCATBALF        |
| 16| TRANREPT   | Generate daily transaction report                 | CBTRN03C (via proc) | TRANSACT -> TRANREPT report           |
| 17| CREASTMT   | Generate account statements (text + HTML)         | CBSTM03A            | TRANSACT + XREF + ACCT + CUST -> STATEMNT |
| 18| COMBTRAN   | Combine backed-up transactions with master        | SORT + IDCAMS       | TRANSACT.BKUP + SYSTRAN -> COMBINED   |
| 19| TRANBKP    | Backup transaction file                           | IDCAMS              | TRANSACT -> TRANSACT.BKUP GDG         |

### Report & Print Jobs

| # | Job Name   | Function                                          | Program/Utility     |
|---|------------|---------------------------------------------------|---------------------|
| 20| PRTCATBL   | Print transaction category balance report         | IDCAMS (PRINT)      |
| 21| REPTFILE   | Print reports from report file                    | IDCAMS (PRINT)      |
| 22| DALYREJS   | Print daily rejection report                      | IDCAMS (PRINT)      |
| 23| TXT2PDF1   | Convert text statement to PDF                     | TXT2PDF (REXX)      |

### Data Read/Verification Jobs

| # | Job Name   | Function                                          | Program Executed    |
|---|------------|---------------------------------------------------|---------------------|
| 24| READACCT   | Read and display Account records                  | CBACT01C            |
| 25| READCARD   | Read and display Card records                     | CBACT02C            |
| 26| READCUST   | Read and display Customer records                 | CBCUS01C            |
| 27| READXREF   | Read and display Cross-Reference records          | CBACT03C            |

### Export/Import Jobs

| # | Job Name   | Function                                          | Program Executed    |
|---|------------|---------------------------------------------------|---------------------|
| 28| CBEXPORT   | Export all VSAM data to single flat file          | CBEXPORT            |
| 29| CBIMPORT   | Import flat file data back into VSAM              | CBIMPORT            |

### Infrastructure/Utility Jobs

| # | Job Name   | Function                                          | Program/Utility     |
|---|------------|---------------------------------------------------|---------------------|
| 30| DEFGDGB    | Define GDG bases (generation data groups)         | IDCAMS              |
| 31| DEFGDGD    | Delete GDG bases                                  | IDCAMS              |
| 32| TRANIDX    | Define alternate index on Transaction file        | IDCAMS              |
| 33| ESDSRRDS   | Create ESDS/RRDS versions of User Security file   | IDCAMS + IEBGENER   |
| 34| WAITSTEP   | Wait step utility (calls COBSWAIT)                | COBSWAIT            |
| 35| FTPJCL     | FTP file transfer to/from mainframe               | FTP                 |
| 36| INTRDRJ1   | Internal Reader - triggers second JCL job         | IEBGENER            |
| 37| INTRDRJ2   | Internal Reader - triggered by INTRDRJ1           | IDCAMS              |
| 38| CBADMCDJ   | Admin card management batch job                   | (varies)            |

---

## JCL Procedures

Located in `app/proc/`.

| # | Procedure  | Function                                          |
|---|------------|---------------------------------------------------|
| 1 | REPROC     | Reusable report generation procedure              |
| 2 | TRANREPT   | Transaction report procedure (used by TRANREPT.jcl) |

---

## Assembler Programs

Located in `app/asm/`.

| # | Program    | Function                                          |
|---|------------|---------------------------------------------------|
| 1 | MVSWAIT    | MVS Wait utility (STIMER macro)                   |
| 2 | COBDATFT   | Date format conversion (assembler helper)         |

---

## Supporting Artifacts

### Scheduler Configurations (`app/scheduler/`)

| File                | Scheduler    | Function                                |
|---------------------|--------------|-----------------------------------------|
| CardDemo.ca7        | CA7          | Job scheduling definitions              |
| CardDemo.controlm   | Control-M    | Job scheduling definitions              |

### CSD Definitions (`app/csd/`)

| File                | Function                                            |
|---------------------|-----------------------------------------------------|
| CARDDEMO.CSD        | CICS Resource Definitions (transactions, programs, files, maps) |

### Control Files (`app/ctl/`)

Control card files for batch job parameters.

### Sample Data (`app/data/`)

| Directory     | Format   | Purpose                                     |
|---------------|----------|---------------------------------------------|
| data/ASCII/   | ASCII    | Sample data for testing converted apps      |
| data/EBCDIC/  | EBCDIC   | Sample data for mainframe upload            |

---

## Classification Legend

| Classification    | Description                                                |
|-------------------|------------------------------------------------------------|
| **Online CICS**   | Interactive program running under CICS transaction server  |
| **Batch**         | Scheduled program running in JCL batch jobs                |
| **Utility**       | Reusable service/helper program                            |
| **Subroutine**    | Called by other programs (CALL or CICS LINK)               |
| **IDCAMS**        | IBM utility for VSAM file management                       |
| **SORT**          | IBM utility for sorting/merging datasets                   |
| **IEBGENER**      | IBM utility for dataset copy                               |
| **DFHCSDUP**      | CICS System Definition utility                             |

## Batch Cycle Execution Order

The recommended nightly batch cycle runs in this sequence:

```
1. CLOSEFIL    -- Close CICS files for batch access
2. ACCTFILE    -- Refresh Account master
3. CARDFILE    -- Refresh Card master
4. CUSTFILE    -- Refresh Customer master
5. XREFFILE    -- Refresh Cross-Reference
6. TRANFILE    -- Refresh Transaction master
7. POSTTRAN    -- Post daily transactions
8. INTCALC     -- Calculate interest
9. TRANBKP     -- Backup transactions
10. COMBTRAN   -- Combine transactions
11. CREASTMT   -- Generate statements
12. TRANIDX    -- Rebuild alternate indexes
13. OPENFIL    -- Reopen CICS files
```
