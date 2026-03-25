# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo — Mainframe Credit Card Management System
> **Platform**: z/OS, CICS/VSAM, COBOL, JCL, BMS

---

## Executive Summary

CardDemo is an AWS-provided mainframe credit card management application built with COBOL/CICS/VSAM/JCL. It simulates account management, card management, transactions, bill payments, and reporting through 3270 terminal screens. The system supports two user roles: **Regular** (card operations) and **Admin** (user and transaction-type management).

| Artifact Type           | Core Count | Optional Modules | Total |
|------------------------|:----------:|:----------------:|:-----:|
| COBOL Programs          | 31         | 13               | 44    |
| Copybooks               | 30         | —                | 30    |
| BMS Screen Maps         | 17         | —                | 17    |
| BMS-Generated Copybooks | 17         | —                | 17    |
| JCL Batch Jobs          | 38         | —                | 38    |
| Assembler Programs      | 2          | —                | 2     |
| JCL Procedures          | 2          | —                | 2     |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (17 programs)

These run under CICS and drive the 3270 terminal UI via BMS maps.

| # | Program    | Lines | Trans ID | Function                              | BMS Map   | Domain            | User Role |
|---|-----------|------:|----------|---------------------------------------|-----------|-------------------|-----------|
| 1 | COSGN00C  | 261   | CC00     | Signon / Authentication               | COSGN00   | Security          | All       |
| 2 | COMEN01C  | 309   | CM00     | Main Menu (Regular Users)             | COMEN01   | Navigation        | User      |
| 3 | COADM01C  | 288   | CA00     | Admin Menu                            | COADM01   | Navigation        | Admin     |
| 4 | COACTVWC  | 942   | CAVW     | Account View (read-only)              | COACTVW   | Account Mgmt      | User      |
| 5 | COACTUPC  | 4,237 | CAUP     | Account Update (full CRUD)            | COACTUP   | Account Mgmt      | User      |
| 6 | COCRDLIC  | 1,460 | CCLI     | Credit Card List                      | COCRDLI   | Card Mgmt         | User      |
| 7 | COCRDSLC  | 888   | CCDL     | Credit Card Detail View               | COCRDSL   | Card Mgmt         | User      |
| 8 | COCRDUPC  | 1,560 | CCUP     | Credit Card Update                    | COCRDUP   | Card Mgmt         | User      |
| 9 | COTRN00C  | 699   | CT00     | Transaction List                      | COTRN00   | Transaction Mgmt  | User      |
| 10| COTRN01C  | 330   | CT01     | Transaction Detail View               | COTRN01   | Transaction Mgmt  | User      |
| 11| COTRN02C  | 783   | CT02     | Transaction Add (New)                 | COTRN02   | Transaction Mgmt  | User      |
| 12| CORPT00C  | 649   | CR00     | Transaction Report Request            | CORPT00   | Reporting         | User      |
| 13| COBIL00C  | 572   | CB00     | Bill Payment                          | COBIL00   | Billing           | User      |
| 14| COUSR00C  | 695   | CU00     | User List (Security Admin)            | COUSR00   | User Admin        | Admin     |
| 15| COUSR01C  | 299   | CU01     | User Add                              | COUSR01   | User Admin        | Admin     |
| 16| COUSR02C  | 414   | CU02     | User Update                           | COUSR02   | User Admin        | Admin     |
| 17| COUSR03C  | 359   | CU03     | User Delete                           | COUSR03   | User Admin        | Admin     |

### 1.2 Batch Programs (13 programs)

These run in batch via JCL and process sequential/VSAM files.

| # | Program    | Lines | Function                                            | Domain            | Complexity |
|---|-----------|------:|-----------------------------------------------------|-------------------|------------|
| 1 | CBTRN02C  | 731   | **Transaction Posting** — Posts daily transactions to master | Transaction Processing | High |
| 2 | CBTRN01C  | 494   | Transaction File Loader — Loads daily transactions  | Transaction Processing | Medium |
| 3 | CBTRN03C  | 649   | Transaction Detail Report Generation                | Reporting         | Medium |
| 4 | CBACT04C  | 652   | **Interest Calculation** — Computes interest per account | Financial Calc    | High |
| 5 | CBACT01C  | 430   | Account File Reader/Splitter                        | Account Mgmt      | Medium |
| 6 | CBACT02C  | 178   | Card Data File Reader/Printer                       | Card Mgmt         | Low |
| 7 | CBACT03C  | 178   | Cross-Reference File Reader/Printer                 | Reference Data    | Low |
| 8 | CBCUS01C  | 178   | Customer Data File Reader/Printer                   | Customer Mgmt     | Low |
| 9 | CBSTM03A  | 924   | **Statement Generation** — Prints account statements | Reporting         | High |
| 10| CBSTM03B  | 230   | Statement Subroutine — File processing for statements | Reporting        | Low |
| 11| CBEXPORT  | 582   | Data Export for Branch Migration                    | Data Migration    | Medium |
| 12| CBIMPORT  | 487   | Data Import from Branch Migration Export            | Data Migration    | Medium |
| 13| COBSWAIT  | 157   | Wait Utility (calls ASM MVSWAIT)                    | Utility           | Low |

### 1.3 Shared Utility (1 program)

| Program    | Lines | Function                          | Called By         |
|-----------|------:|-----------------------------------|-------------------|
| CSUTLDTC  | 157   | Date Validation Utility           | CORPT00C, COTRN02C |

### 1.4 Optional Module Programs (13 programs)

#### Authorization Module (IMS/DB2/MQ) — `app/app-authorization-ims-db2-mq/`

| Program    | Lines | Function                                   |
|-----------|------:|--------------------------------------------|
| COPAUA0C  | 1,026 | MQ Trigger — Receives authorization requests |
| COPAUS0C  | 1,032 | Pending Authorization Summary View          |
| COPAUS1C  | 604   | Pending Authorization Detail View           |
| COPAUS2C  | 244   | Mark Transaction as Fraud (DB2 update)      |
| CBPAUP0C  | 386   | Batch Purge of Processed Authorizations     |
| DBUNLDGS  | 366   | DB2 Unload to GS (Generic Sequential)       |
| PAUDBLOD  | 369   | Authorization DB Load                       |
| PAUDBUNL  | 317   | Authorization DB Unload                     |

#### Transaction Type DB2 — `app/app-transaction-type-db2/`

| Program    | Lines | Function                                   |
|-----------|------:|--------------------------------------------|
| COTRTLIC  | 2,098 | Transaction Type List/Delete (DB2 cursors)  |
| COTRTUPC  | 1,702 | Transaction Type Add/Edit (DB2 embedded SQL) |
| COBTUPDT  | 237   | Batch Update of Transaction Types           |

#### VSAM-MQ Integration — `app/app-vsam-mq/`

| Program    | Lines | Function                                   |
|-----------|------:|--------------------------------------------|
| COACCT01  | 620   | MQ Request/Response for Account Inquiry     |
| CODATE01  | 524   | MQ Request/Response for System Date         |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (15 copybooks)

| # | Copybook   | Record Name            | Record Len | Business Entity          |
|---|-----------|------------------------|:----------:|--------------------------|
| 1 | CVACT01Y  | ACCT-RECORD            | 300 bytes  | Account Master           |
| 2 | CVACT02Y  | CARD-RECORD            | 150 bytes  | Credit Card Master       |
| 3 | CVACT03Y  | CARD-XREF-RECORD       | 50 bytes   | Account-Card Cross-Ref   |
| 4 | CVCUS01Y  | CUSTOMER-RECORD        | 500 bytes  | Customer Master          |
| 5 | CVCRD01Y  | CC-WORK-AREA           | —          | Card Working Storage     |
| 6 | CVTRA05Y  | TRAN-RECORD            | 350 bytes  | Transaction Master       |
| 7 | CVTRA06Y  | DALYTRAN-RECORD        | 350 bytes  | Daily Transaction        |
| 8 | CVTRA01Y  | TRAN-CAT-BAL-RECORD    | 50 bytes   | Trans Category Balance   |
| 9 | CVTRA02Y  | DIS-GROUP-RECORD       | 50 bytes   | Disclosure Group/Rate    |
| 10| CVTRA03Y  | TRAN-TYPE-RECORD       | 60 bytes   | Transaction Type         |
| 11| CVTRA04Y  | TRAN-CAT-RECORD        | 60 bytes   | Transaction Category     |
| 12| CVTRA07Y  | REPORT structures       | —          | Transaction Report Layout|
| 13| CVEXPORT  | EXPORT-RECORD          | various    | Data Export Record       |
| 14| COSTM01   | TRNX-RECORD            | —          | Statement Trans Layout   |
| 15| CUSTREC   | (Customer subset)      | —          | Customer for Statements  |

### 2.2 Application Infrastructure Copybooks (13 copybooks)

| # | Copybook   | Purpose                                         |
|---|-----------|--------------------------------------------------|
| 1 | COCOM01Y  | **COMMAREA** — Inter-program communication area  |
| 2 | COMEN02Y  | Main Menu option definitions (11 options)        |
| 3 | COADM02Y  | Admin Menu option definitions (6 options)        |
| 4 | COTTL01Y  | Screen titles and header constants               |
| 5 | CSDAT01Y  | Current date/time formatting                     |
| 6 | CSMSG01Y  | Common messages (thank you, invalid key, etc.)   |
| 7 | CSMSG02Y  | Abend/error handling variables                   |
| 8 | CSUSR01Y  | Signed-on user security record layout            |
| 9 | CSSETATY  | Screen attribute setting utilities               |
| 10| CSSTRPFY  | PF key storage/mapping                           |
| 11| CSLKPCDY  | Lookup code utility                              |
| 12| CSUTLDPY  | Date utility parameters                          |
| 13| CSUTLDWY  | Date editing work variables                      |

### 2.3 Special Copybooks (2 copybooks)

| Copybook   | Purpose                                           |
|-----------|---------------------------------------------------|
| CODATECN  | Date conversion routines (used by CBACT01C)       |
| UNUSED1Y  | Placeholder/unused record layout                  |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map File    | Mapset   | Map Name | Lines | Associated Program | Screen Purpose             |
|---|------------|----------|----------|------:|-------------------|---------------------------|
| 1 | COSGN00.bms| COSGN00  | COSGN0A  | 210   | COSGN00C          | Login / Signon Screen      |
| 2 | COMEN01.bms| COMEN01  | COMEN1A  | 167   | COMEN01C          | Main Menu (Regular User)   |
| 3 | COADM01.bms| COADM01  | COADM1A  | 167   | COADM01C          | Admin Menu                 |
| 4 | COACTVW.bms| COACTVW  | CACTVWA  | 378   | COACTVWC          | Account View               |
| 5 | COACTUP.bms| COACTUP  | CACTUPA  | 512   | COACTUPC          | Account Update             |
| 6 | COCRDLI.bms| COCRDLI  | CCRDLIA  | 344   | COCRDLIC          | Credit Card List           |
| 7 | COCRDSL.bms| COCRDSL  | CCRDSLA  | 157   | COCRDSLC          | Credit Card Detail View    |
| 8 | COCRDUP.bms| COCRDUP  | CCRDUPA  | 172   | COCRDUPC          | Credit Card Update         |
| 9 | COTRN00.bms| COTRN00  | COTRN0A  | 464   | COTRN00C          | Transaction List           |
| 10| COTRN01.bms| COTRN01  | COTRN1A  | 273   | COTRN01C          | Transaction Detail View    |
| 11| COTRN02.bms| COTRN02  | COTRN2A  | 307   | COTRN02C          | Transaction Add            |
| 12| CORPT00.bms| CORPT00  | CORPT0A  | 231   | CORPT00C          | Transaction Report Request |
| 13| COBIL00.bms| COBIL00  | COBIL0A  | 141   | COBIL00C          | Bill Payment               |
| 14| COUSR00.bms| COUSR00  | COUSR0A  | 463   | COUSR00C          | User List                  |
| 15| COUSR01.bms| COUSR01  | COUSR1A  | 164   | COUSR01C          | User Add                   |
| 16| COUSR02.bms| COUSR02  | COUSR2A  | 169   | COUSR02C          | User Update                |
| 17| COUSR03.bms| COUSR03  | COUSR3A  | 153   | COUSR03C          | User Delete                |

Each BMS map has a corresponding generated copybook in `app/cpy-bms/` (e.g., `COSGN00.CPY`).

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Batch Processing Cycle Jobs (Core Workflow)

Execution order: `CLOSEFIL → Data Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL`

| # | JCL Job     | Lines | Executes Program | Purpose                                         |
|---|------------|------:|-----------------|--------------------------------------------------|
| 1 | CLOSEFIL   | 34    | SDSF             | Close CICS files for batch processing            |
| 2 | POSTTRAN   | 45    | CBTRN02C         | **Core** — Post daily transactions to master      |
| 3 | INTCALC    | 44    | CBACT04C         | **Core** — Calculate interest on accounts         |
| 4 | TRANBKP    | 71    | IDCAMS           | Backup transaction file (REPRO to GDG)           |
| 5 | COMBTRAN   | 52    | SORT             | Combine and sort transaction files                |
| 6 | CREASTMT   | 97    | CBSTM03A + SORT  | **Core** — Generate account statements            |
| 7 | TRANIDX    | 58    | IDCAMS           | Define alternate index on transaction file        |
| 8 | OPENFIL    | 34    | SDSF             | Reopen CICS files after batch processing         |

### 4.2 Data Refresh Jobs

| # | JCL Job     | Lines | Executes Program | Purpose                                   |
|---|------------|------:|-----------------|-------------------------------------------|
| 9 | ACCTFILE   | 65    | IDCAMS           | Refresh account master VSAM file          |
| 10| CARDFILE   | 128   | IDCAMS + SDSF    | Refresh card master VSAM file             |
| 11| CUSTFILE   | 84    | IDCAMS + SDSF    | Refresh customer master VSAM file         |
| 12| TRANFILE   | 125   | IDCAMS + SDSF    | Refresh transaction master VSAM file      |
| 13| XREFFILE   | 106   | IDCAMS           | Load card-account cross-reference file    |
| 14| DUSRSECJ   | 92    | IDCAMS + IEBGENER| Load user security VSAM file              |

### 4.3 Data Definition Jobs

| # | JCL Job     | Lines | Purpose                                           |
|---|------------|------:|---------------------------------------------------|
| 15| DEFGDGB    | 63    | Define GDG base for backups                       |
| 16| DEFGDGD    | 94    | Define GDG data for backups                       |
| 17| DEFCUST    | 47    | Define customer VSAM cluster                      |
| 18| DISCGRP    | 65    | Define/load disclosure group VSAM                 |
| 19| TRANTYPE   | 65    | Define/load transaction type VSAM                 |
| 20| TRANCATG   | 65    | Define/load transaction category VSAM             |
| 21| TCATBALF   | 65    | Define/load transaction category balance VSAM     |
| 22| REPTFILE   | 32    | Define report file VSAM cluster                   |
| 23| ESDSRRDS   | 124   | Define ESDS/RRDS sample datasets                  |
| 24| DALYREJS   | 32    | Define daily rejects file                         |

### 4.4 Reporting and Utility Jobs

| # | JCL Job     | Lines | Executes Program | Purpose                                   |
|---|------------|------:|-----------------|-------------------------------------------|
| 25| TRANREPT   | 84    | CBTRN03C + SORT  | Generate transaction detail report         |
| 26| READACCT   | 50    | CBACT01C         | Read and print account data                |
| 27| READCARD   | 31    | CBACT02C         | Read and print card data                   |
| 28| READCUST   | 30    | CBCUS01C         | Read and print customer data               |
| 29| READXREF   | 31    | CBACT03C         | Read and print cross-reference data        |
| 30| PRTCATBL   | 66    | SORT             | Print transaction category balance file    |
| 31| TXT2PDF1   | 41    | IKJEFT1B (TSO)   | Convert text report to PDF                 |

### 4.5 Data Migration Jobs

| # | JCL Job     | Lines | Executes Program | Purpose                                   |
|---|------------|------:|-----------------|-------------------------------------------|
| 32| CBEXPORT   | 72    | CBEXPORT         | Export all data for branch migration       |
| 33| CBIMPORT   | 68    | CBIMPORT         | Import data from branch migration export   |

### 4.6 Infrastructure/Admin Jobs

| # | JCL Job     | Lines | Purpose                                           |
|---|------------|------:|---------------------------------------------------|
| 34| CBADMCDJ   | 167   | CICS CSD resource definitions (DFHCSDUP)          |
| 35| WAITSTEP   | 27    | Wait step utility (calls COBSWAIT)                |
| 36| FTPJCL     | 42    | FTP file transfer utility                         |
| 37| INTRDRJ1   | 19    | Internal reader — submit JCL dynamically          |
| 38| INTRDRJ2   | 14    | Internal reader — backup and submit               |

---

## 5. Assembler Programs (`app/asm/`)

| Program     | Purpose                                             |
|------------|-----------------------------------------------------|
| MVSWAIT.asm | System wait utility (called by COBSWAIT)           |
| COBDATFT.asm| Date format conversion (called by CBACT01C)        |

---

## 6. JCL Procedures (`app/proc/`)

| Procedure      | Purpose                                          |
|---------------|--------------------------------------------------|
| REPROC.prc    | Reusable procedure for IDCAMS REPRO operations   |
| TRANREPT.prc  | Reusable procedure for transaction report steps  |

---

## 7. Job Scheduler Configurations (`app/scheduler/`)

| Config                | Scheduler   | Purpose                              |
|----------------------|-------------|--------------------------------------|
| CardDemo.ca7         | CA-7        | Job scheduling definitions           |
| CardDemo.controlm    | Control-M   | Job scheduling definitions           |

---

## 8. Classification Summary

### By Business Domain

| Domain                 | Online Programs | Batch Programs | JCL Jobs |
|-----------------------|:---------------:|:--------------:|:--------:|
| Security / User Admin  | 5 (SGN, USR×4) | 0              | 1        |
| Account Management     | 2 (VW, UP)     | 1 (ACT01)     | 1        |
| Card Management        | 3 (LI, SL, UP) | 1 (ACT02)     | 1        |
| Transaction Processing | 3 (00, 01, 02) | 3 (TRN×3)     | 2        |
| Reporting              | 1 (RPT00)      | 2 (TRN03, STM)| 3        |
| Billing                | 1 (BIL00)      | 0              | 0        |
| Financial Calculation  | 0               | 1 (ACT04)     | 1        |
| Data Migration         | 0               | 2 (EXP, IMP)  | 2        |
| Navigation (Menus)     | 2 (MEN, ADM)   | 0              | 0        |
| Utility                | 0               | 1 (WAIT)       | 3        |
| Data Refresh           | 0               | 0              | 6        |
| Data Definition        | 0               | 0              | 10       |
| Infrastructure         | 0               | 0              | 4        |

### By Technology Stack

| Technology        | Components                                          |
|------------------|-----------------------------------------------------|
| CICS / BMS       | 17 online programs, 17 BMS maps, COMMAREA-based nav |
| VSAM KSDS        | Account, Card, Customer, Transaction, User Security  |
| VSAM Alternate IX| Card-Account cross-reference (CARDAIX, CXACAIX)     |
| Sequential Files | Daily transactions, reports, exports                 |
| GDG (Generations)| Transaction backups                                  |
| SORT (DFSORT)    | Transaction combining, report sorting                |
| IDCAMS           | VSAM cluster definition, REPRO, DELETE               |
| Assembler        | MVSWAIT (wait), COBDATFT (date format)               |
| IMS DB (optional)| Authorization module                                 |
| DB2 (optional)   | Transaction type management, fraud marking           |
| MQ (optional)    | Authorization triggers, VSAM-MQ request/response     |

### Naming Conventions

| Prefix  | Meaning                                |
|---------|----------------------------------------|
| `CO*`   | Online CICS program                    |
| `CB*`   | Batch COBOL program                    |
| `CS*`   | Shared/common utility copybook         |
| `CV*`   | VSAM/data record layout copybook       |
| `CO*Y`  | Online infrastructure copybook         |
| `DFHAID` | IBM-supplied AID key definitions      |
| `DFHBMSCA`| IBM-supplied BMS character attributes|
