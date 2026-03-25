# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Platform:** IBM z/OS Mainframe | **Runtime:** CICS/VSAM/JCL Batch

---

## Executive Summary

CardDemo is a mainframe credit-card management application comprising **31 COBOL programs** (20,650 LOC), **30 copybooks** (2,786 LOC), **17 BMS screen maps**, and **38 JCL batch jobs**. The system supports two user roles (Regular User and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user security administration.

| Artifact Type         | Count | Total LOC |
|-----------------------|------:|----------:|
| COBOL Programs (core) |    31 |    20,650 |
| Copybooks (core)      |    30 |     2,786 |
| BMS Screen Maps       |    17 |        -- |
| BMS-Generated Copybooks | 17  |        -- |
| JCL Batch Jobs        |    38 |        -- |
| Assembler Programs    |     2 |        -- |
| JCL Procedures        |     2 |        -- |
| Scheduler Configs     |     2 |        -- |
| Optional Module Programs | 13 |        -- |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (17 programs)

These programs run under CICS and implement the interactive 3270 terminal UI.

| # | Program    | LOC  | Function                          | CICS Trans | User Role | Classification         |
|--:|------------|-----:|-----------------------------------|------------|-----------|------------------------|
| 1 | COSGN00C   |  260 | User sign-on / authentication     | CC00       | All       | Security / Auth        |
| 2 | COMEN01C   |  308 | Main menu (regular user)          | CM00       | User      | Navigation             |
| 3 | COADM01C   |  288 | Admin menu                        | CA00       | Admin     | Navigation             |
| 4 | COACTVWC   |  941 | Account view (read-only)          | CA01       | User      | Account Management     |
| 5 | COACTUPC   | 4,236| Account update                    | CA02       | User      | Account Management     |
| 6 | COCRDLIC   | 1,459| Credit card list                  | CC01       | User      | Card Management        |
| 7 | COCRDSLC   |  887 | Credit card view (single card)    | CC02       | User      | Card Management        |
| 8 | COCRDUPC   | 1,560| Credit card update                | CC03       | User      | Card Management        |
| 9 | COTRN00C   |  699 | Transaction list                  | CT00       | User      | Transaction Processing |
|10 | COTRN01C   |  330 | Transaction view (single)         | CT01       | User      | Transaction Processing |
|11 | COTRN02C   |  783 | Transaction add (new transaction) | CT02       | User      | Transaction Processing |
|12 | CORPT00C   |  649 | Transaction reports (date range)  | CR00       | User      | Reporting              |
|13 | COBIL00C   |  572 | Bill payment processing           | CB00       | User      | Bill Payment           |
|14 | COUSR00C   |  695 | User list (security admin)        | CU00       | Admin     | User Administration    |
|15 | COUSR01C   |  299 | User add                          | CU01       | Admin     | User Administration    |
|16 | COUSR02C   |  414 | User update                       | CU02       | Admin     | User Administration    |
|17 | COUSR03C   |  359 | User delete                       | CU03       | Admin     | User Administration    |

### 1.2 Batch Programs (13 programs)

These programs run as z/OS batch jobs, invoked via JCL.

| # | Program    | LOC  | Function                                  | Classification           |
|--:|------------|-----:|-------------------------------------------|--------------------------|
| 1 | CBACT01C   |  430 | Read & list account master file            | Account Management       |
| 2 | CBACT02C   |  178 | Read & list card master file               | Card Management          |
| 3 | CBACT03C   |  178 | Read & list card cross-reference file      | Card Management          |
| 4 | CBACT04C   |  652 | Interest calculation on accounts           | Financial Processing     |
| 5 | CBCUS01C   |  178 | Read & list customer master file           | Customer Management      |
| 6 | CBTRN01C   |  494 | Read daily transaction file                | Transaction Processing   |
| 7 | CBTRN02C   |  731 | Post daily transactions to master          | Transaction Processing   |
| 8 | CBTRN03C   |  649 | Transaction report generation              | Reporting                |
| 9 | CBSTM03A   |  924 | Statement generation (text + HTML)         | Reporting / Statements   |
|10 | CBSTM03B   |  230 | File I/O subroutine for statement gen      | Reporting / Statements   |
|11 | CBEXPORT   |  582 | Export VSAM data to sequential files       | Data Export/Import       |
|12 | CBIMPORT   |  487 | Import sequential data into VSAM files     | Data Export/Import       |
|13 | COBSWAIT   |   41 | Wait utility (calls assembler MVSWAIT)     | Utility                  |

### 1.3 Shared Utility Program (1 program)

| # | Program    | LOC  | Function                                  | Classification |
|--:|------------|-----:|-------------------------------------------|----------------|
| 1 | CSUTLDTC   |  157 | Date validation (calls LE CEEDAYS API)    | Utility        |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (CV* prefix -- VSAM record structures)

| # | Copybook   | LOC | Record Name            | Record Length | Business Entity              |
|--:|------------|----:|------------------------|:--------------|------------------------------|
| 1 | CVACT01Y   |  20 | ACCT-RECORD            | 300 bytes     | Account Master               |
| 2 | CVACT02Y   |  14 | CARD-RECORD            | 150 bytes     | Card Master                  |
| 3 | CVACT03Y   |  11 | CARD-XREF-RECORD       | 50 bytes      | Card-to-Account Cross-Ref    |
| 4 | CVCRD01Y   |  46 | CARD-DETAIL-RECORD     | N/A           | Card Detail (expanded)       |
| 5 | CVCUS01Y   |  26 | CUSTOMER-RECORD        | 500 bytes     | Customer Master              |
| 6 | CVTRA01Y   |  13 | TRAN-CAT-BAL-RECORD    | 50 bytes      | Transaction Category Balance |
| 7 | CVTRA02Y   |  13 | DIS-GROUP-RECORD       | 50 bytes      | Disclosure Group             |
| 8 | CVTRA03Y   |  10 | TRAN-TYPE-RECORD       | 60 bytes      | Transaction Type             |
| 9 | CVTRA04Y   |  12 | TRAN-CAT-RECORD        | 60 bytes      | Transaction Category         |
|10 | CVTRA05Y   |  21 | TRAN-RECORD            | 350 bytes     | Transaction Master           |
|11 | CVTRA06Y   |  21 | DALYTRAN-RECORD        | 350 bytes     | Daily Transaction            |
|12 | CVTRA07Y   |  73 | TRANSACTION-DETAIL-REPORT | N/A        | Report Layout                |
|13 | CVEXPORT   | 103 | (export record layouts) | Various       | Export File Records          |
|14 | COSTM01    |  38 | TRNX-RECORD            | 350+ bytes    | Statement Transaction Layout |

### 2.2 Communication & Control Copybooks (CO*/CS* prefix)

| # | Copybook   | LOC | Purpose                                        |
|--:|------------|----:|------------------------------------------------|
| 1 | COCOM01Y   |  47 | CICS COMMAREA - inter-program communication    |
| 2 | COMEN02Y   | 101 | Main menu option definitions (11 options)      |
| 3 | COADM02Y   |  62 | Admin menu option definitions (6 options)      |
| 4 | COTTL01Y   |  27 | Screen title/header fields                     |
| 5 | CSMSG01Y   |  24 | Message area for screen display                |
| 6 | CSMSG02Y   |  35 | Extended message area                          |
| 7 | CSDAT01Y   |  58 | Date/time working storage fields               |
| 8 | CSSETATY   |  30 | Set attribute byte utility                     |
| 9 | CSSTRPFY   |  85 | String manipulation utility fields             |
|10 | CSLKPCDY   |1,318| Lookup code tables (states, countries, etc.)   |
|11 | CSUTLDPY   | 375 | Date utility parameters                        |
|12 | CSUTLDWY   |  89 | Date utility working storage                   |
|13 | CODATECN   |  52 | Date conversion record (assembler interface)   |

### 2.3 Security Copybooks

| # | Copybook   | LOC | Purpose                              |
|--:|------------|----:|--------------------------------------|
| 1 | CSUSR01Y   |  26 | User security record (USRSEC file)   |

### 2.4 Other Copybooks

| # | Copybook   | LOC | Purpose                                   |
|--:|------------|----:|-------------------------------------------|
| 1 | CUSTREC    |  26 | Alternate customer record layout           |
| 2 | UNUSED1Y   |  10 | Unused/deprecated data structure           |

---

## 3. BMS Screen Maps (`app/bms/`)

BMS (Basic Mapping Support) maps define the 3270 terminal screen layouts.

| # | Map File   | Map Name     | Associated Program | Screen Purpose            |
|--:|------------|-------------|--------------------|-----------------------------|
| 1 | COSGN00.bms | COSGN0A   | COSGN00C           | Sign-on screen              |
| 2 | COMEN01.bms | COMEN1A   | COMEN01C           | Main menu                   |
| 3 | COADM01.bms | COADM1A   | COADM01C           | Admin menu                  |
| 4 | COACTVW.bms | CACTVWA   | COACTVWC           | Account view                |
| 5 | COACTUP.bms | CACTUPA   | COACTUPC           | Account update              |
| 6 | COCRDLI.bms | CCRDLIA   | COCRDLIC           | Card list                   |
| 7 | COCRDSL.bms | CCRDSLA   | COCRDSLC           | Card detail view            |
| 8 | COCRDUP.bms | CCRDUPA   | COCRDUPC           | Card update                 |
| 9 | COTRN00.bms | COTRN0A   | COTRN00C           | Transaction list            |
|10 | COTRN01.bms | COTRN1A   | COTRN01C           | Transaction detail view     |
|11 | COTRN02.bms | COTRN2A   | COTRN02C           | Transaction add             |
|12 | CORPT00.bms | CORPT0A   | CORPT00C           | Report parameters           |
|13 | COBIL00.bms | COBIL0A   | COBIL00C           | Bill payment                |
|14 | COUSR00.bms | COUSR0A   | COUSR00C           | User list                   |
|15 | COUSR01.bms | COUSR1A   | COUSR01C           | User add                    |
|16 | COUSR02.bms | COUSR2A   | COUSR02C           | User update                 |
|17 | COUSR03.bms | COUSR3A   | COUSR03C           | User delete                 |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 matching copybooks auto-generated from BMS maps, providing symbolic field names used by COBOL programs for SEND MAP / RECEIVE MAP operations.

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data File Management (Define, Load, Refresh)

| # | Job        | Purpose                                              | Key Program/Utility |
|--:|------------|------------------------------------------------------|---------------------|
| 1 | ACCTFILE   | Delete/define/load Account Master VSAM KSDS          | IDCAMS, SDSF        |
| 2 | CARDFILE   | Delete/define/load Card Master VSAM KSDS             | IDCAMS, SDSF        |
| 3 | CUSTFILE   | Delete/define/load Customer Master VSAM KSDS         | IDCAMS, SDSF        |
| 4 | XREFFILE   | Delete/define/load Card Cross-Reference VSAM KSDS    | IDCAMS              |
| 5 | TRANFILE   | Delete/define/load Transaction Master VSAM KSDS      | IDCAMS, SDSF        |
| 6 | TRANTYPE   | Delete/define/load Transaction Type VSAM KSDS        | IDCAMS              |
| 7 | TRANCATG   | Delete/define/load Transaction Category VSAM KSDS    | IDCAMS              |
| 8 | TCATBALF   | Delete/define/load Tran Category Balance VSAM KSDS   | IDCAMS              |
| 9 | DISCGRP    | Delete/define/load Disclosure Group VSAM KSDS        | IDCAMS              |
|10 | DALYREJS   | Delete/define Daily Rejects VSAM KSDS                | IDCAMS              |
|11 | REPTFILE   | Delete/define Report VSAM KSDS                       | IDCAMS              |
|12 | DUSRSECJ   | Load User Security VSAM file from sequential data    | IEBGENER, IDCAMS    |
|13 | DEFCUST    | Define customer VSAM cluster                         | IDCAMS              |
|14 | DEFGDGB    | Define GDG base for backups                          | IDCAMS              |
|15 | DEFGDGD    | Define GDG base for daily files                      | IDCAMS              |

### 4.2 CICS File Control

| # | Job        | Purpose                                    | Key Utility |
|--:|------------|---------------------------------------------|-------------|
| 1 | CLOSEFIL   | Close VSAM files in CICS region             | SDSF/CEMT   |
| 2 | OPENFIL    | Open VSAM files in CICS region              | SDSF/CEMT   |

### 4.3 Batch Processing (Core Business Logic)

| # | Job        | Purpose                                    | Key Program      |
|--:|------------|---------------------------------------------|-------------------|
| 1 | POSTTRAN   | Post daily transactions to master           | CBTRN02C          |
| 2 | INTCALC    | Calculate interest on accounts              | CBACT04C          |
| 3 | TRANBKP    | Backup transaction file (GDG roll)          | IDCAMS            |
| 4 | COMBTRAN   | Combine/sort transactions                   | SORT, IDCAMS      |
| 5 | CREASTMT   | Create account statements (text + HTML)     | CBSTM03A, SORT    |
| 6 | TRANREPT   | Generate transaction report                 | CBTRN03C, SORT    |
| 7 | TRANIDX    | Define alternate index on transaction file  | IDCAMS            |

### 4.4 Read/Print Utilities

| # | Job        | Purpose                                    | Key Program |
|--:|------------|---------------------------------------------|-------------|
| 1 | READACCT   | Read & print account file                  | CBACT01C    |
| 2 | READCARD   | Read & print card file                     | CBACT02C    |
| 3 | READCUST   | Read & print customer file                 | CBCUS01C    |
| 4 | READXREF   | Read & print cross-reference file          | CBACT03C    |
| 5 | PRTCATBL   | Print category balance report              | SORT        |

### 4.5 Data Export/Import

| # | Job        | Purpose                                    | Key Program |
|--:|------------|---------------------------------------------|-------------|
| 1 | CBEXPORT   | Export VSAM data to sequential files       | CBEXPORT    |
| 2 | CBIMPORT   | Import sequential data into VSAM files     | CBIMPORT    |

### 4.6 Infrastructure & Utility Jobs

| # | Job        | Purpose                                     | Key Utility    |
|--:|------------|----------------------------------------------|----------------|
| 1 | CBADMCDJ   | CSD batch admin (define CICS resources)      | DFHCSDUP       |
| 2 | ESDSRRDS   | Define ESDS/RRDS VSAM clusters               | IDCAMS         |
| 3 | WAITSTEP   | Wait step utility (calls COBSWAIT)           | COBSWAIT       |
| 4 | FTPJCL     | FTP file transfer job                        | FTP            |
| 5 | INTRDRJ1   | Internal reader job (trigger chained jobs)    | IDCAMS/IEBGENER|
| 6 | INTRDRJ2   | Chained internal reader job                   | IDCAMS         |
| 7 | TXT2PDF1   | Convert text report to PDF                    | IKJEFT1B       |

---

## 5. Assembler Programs (`app/asm/`)

| # | Program    | Purpose                                     |
|--:|------------|----------------------------------------------|
| 1 | COBDATFT   | Date formatting routine (called by CBACT01C) |
| 2 | MVSWAIT    | MVS wait routine (called by COBSWAIT)        |

---

## 6. JCL Procedures (`app/proc/`)

| # | Procedure  | Purpose                                        |
|--:|------------|------------------------------------------------|
| 1 | REPROC     | Reusable procedure for VSAM-to-sequential unload |
| 2 | TRANREPT   | Procedure used by TRANREPT job                  |

---

## 7. Scheduler Configurations (`app/scheduler/`)

| # | Config File        | Scheduler     | Purpose                       |
|--:|--------------------|---------------|-------------------------------|
| 1 | CardDemo.ca7       | CA-7          | Batch job scheduling           |
| 2 | CardDemo.controlm  | Control-M     | Batch job scheduling           |

---

## 8. Optional Extension Modules

### 8.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ-based authorization processing.

| # | Program    | Type    | Function                                |
|--:|------------|---------|------------------------------------------|
| 1 | COPAUA0C   | Online  | MQ trigger for authorization requests    |
| 2 | COPAUS0C   | Online  | Authorization summary view               |
| 3 | COPAUS1C   | Online  | Authorization detail view                |
| 4 | COPAUS2C   | Online  | Mark transaction as fraud (DB2 write)    |
| 5 | CBPAUP0C   | Batch   | Purge aged authorization records         |
| 6 | DBUNLDGS   | Batch   | Unload IMS DB segments                   |
| 7 | PAUDBLOD   | Batch   | Load authorization data into IMS DB      |
| 8 | PAUDBUNL   | Batch   | Unload authorization data from IMS DB    |

### 8.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based transaction type CRUD operations.

| # | Program    | Type    | Function                                |
|--:|------------|---------|------------------------------------------|
| 1 | COTRTLIC   | Online  | Transaction type list / delete (DB2)     |
| 2 | COTRTUPC   | Online  | Transaction type add / update (DB2)      |
| 3 | COBTUPDT   | Batch   | Batch update transaction types in DB2    |

### 8.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response for VSAM queries.

| # | Program    | Type    | Function                                |
|--:|------------|---------|------------------------------------------|
| 1 | CODATE01   | Online  | MQ system date service                   |
| 2 | COACCT01   | Online  | MQ account inquiry service               |

---

## 9. Classification Summary

### By Business Domain

| Domain                   | Online Programs | Batch Programs | Total |
|--------------------------|:--------------:|:--------------:|:-----:|
| Account Management       |       2        |       1        |   3   |
| Card Management          |       3        |       2        |   5   |
| Transaction Processing   |       3        |       2        |   5   |
| Reporting / Statements   |       1        |       3        |   4   |
| Bill Payment             |       1        |       0        |   1   |
| User Administration      |       4        |       0        |   4   |
| Security / Auth          |       1        |       0        |   1   |
| Navigation (Menus)       |       2        |       0        |   2   |
| Data Export/Import       |       0        |       2        |   2   |
| Utilities                |       0        |       2        |   2   |
| **Subtotals (core)**     |     **17**     |     **14**     | **31**|

### By Technology Stack

| Technology         | Usage                                         |
|--------------------|-----------------------------------------------|
| COBOL              | All 31 core programs + 13 optional             |
| CICS               | 17 online programs (SEND/RECEIVE MAP, XCTL)    |
| VSAM KSDS          | Primary data store (accounts, cards, etc.)      |
| JCL/JES2           | 38 batch jobs                                   |
| BMS                | 17 screen maps for 3270 terminal UI             |
| IDCAMS             | VSAM file management in JCL                     |
| SORT               | Data sorting in batch processing                |
| Assembler          | 2 utility routines (date format, wait)          |
| LE (CEEDAYS)       | Date validation via Language Environment API     |
| IMS DB (optional)  | Authorization module data store                 |
| DB2 (optional)     | Transaction type CRUD module                    |
| MQ (optional)      | Message-driven authorization & inquiry           |
