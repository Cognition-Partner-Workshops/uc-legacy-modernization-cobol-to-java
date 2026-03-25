# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Platform:** IBM z/OS Mainframe | COBOL / CICS / VSAM / JCL / BMS

---

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL/CICS/VSAM.
It simulates account management, card management, transactions, bill payments, and reporting
across **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, and **38 JCL batch jobs**.
Three optional extension modules add **13 additional programs** for IMS/DB2/MQ integration.

| Category               | Core | Optional Modules | Total |
|------------------------|------|-------------------|-------|
| COBOL Programs         | 31   | 13                | 44    |
| Copybooks              | 30   | 11                | 41    |
| BMS Screen Maps        | 17   | 4                 | 21    |
| BMS-Generated Copybooks| 17   | 4                 | 21    |
| JCL Batch Jobs         | 38   | 8                 | 46    |
| Assembler Programs     | 2    | 0                 | 2     |
| JCL Procedures         | 2    | 0                 | 2     |
| Scheduler Configs      | 2    | 0                 | 2     |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (prefix: `CO*`)

| # | Program    | Lines | Trans ID | Function                        | Business Domain    | Classification       |
|---|------------|-------|----------|---------------------------------|--------------------|----------------------|
| 1 | COSGN00C   | 261   | CC00     | User Sign-on / Authentication   | Security           | Entry Point          |
| 2 | COMEN01C   | 309   | CM00     | Main Menu (Regular Users)       | Navigation         | Menu / Router        |
| 3 | COADM01C   | 288   | CA00     | Admin Menu                      | Navigation         | Menu / Router        |
| 4 | COACTVWC   | 942   | CAVW     | Account View (Read-Only)        | Account Mgmt       | Inquiry              |
| 5 | COACTUPC   | 4,237 | CAUP     | Account Update                  | Account Mgmt       | CRUD (Update)        |
| 6 | COCRDLIC   | 1,459 | CCLI     | Credit Card List                | Card Mgmt          | List / Browse        |
| 7 | COCRDSLC   | 887   | CCDL     | Credit Card View (Detail)       | Card Mgmt          | Inquiry              |
| 8 | COCRDUPC   | 1,560 | CCUP     | Credit Card Update              | Card Mgmt          | CRUD (Update)        |
| 9 | COTRN00C   | 699   | CT00     | Transaction List                | Transaction Mgmt   | List / Browse        |
|10 | COTRN01C   | 330   | CT01     | Transaction View (Detail)       | Transaction Mgmt   | Inquiry              |
|11 | COTRN02C   | 784   | CT02     | Transaction Add                 | Transaction Mgmt   | CRUD (Create)        |
|12 | CORPT00C   | 649   | CR00     | Transaction Report Request      | Reporting          | Report UI            |
|13 | COBIL00C   | 572   | CB00     | Bill Payment                    | Payments           | Business Process     |
|14 | COUSR00C   | 695   | CU00     | User List (Admin)               | User Security      | List / Browse        |
|15 | COUSR01C   | 299   | CU01     | User Add (Admin)                | User Security      | CRUD (Create)        |
|16 | COUSR02C   | 414   | CU02     | User Update (Admin)             | User Security      | CRUD (Update)        |
|17 | COUSR03C   | 359   | CU03     | User Delete (Admin)             | User Security      | CRUD (Delete)        |

### 1.2 Batch Programs (prefix: `CB*`)

| # | Program    | Lines | Function                                      | Business Domain     | Classification      |
|---|------------|-------|-----------------------------------------------|---------------------|---------------------|
|18 | CBACT01C   | 430   | Read & Display Account Master (sequential)    | Account Mgmt        | Utility / Read      |
|19 | CBACT02C   | 178   | Read & Display Card Master (sequential)       | Card Mgmt           | Utility / Read      |
|20 | CBACT03C   | 178   | Read & Display Card Cross-Reference           | Card Mgmt           | Utility / Read      |
|21 | CBACT04C   | 652   | Interest Calculation (batch)                  | Financial Calc      | Business Process    |
|22 | CBCUS01C   | 178   | Read & Display Customer Master                | Customer Mgmt       | Utility / Read      |
|23 | CBTRN01C   | 494   | Read & Display Transaction File               | Transaction Mgmt    | Utility / Read      |
|24 | CBTRN02C   | 732   | Transaction Posting (daily batch)             | Transaction Mgmt    | Business Process    |
|25 | CBTRN03C   | 649   | Transaction Report Generation (batch)         | Reporting           | Report Generation   |
|26 | CBSTM03A   | 924   | Statement Generation (batch - main)           | Statements          | Report Generation   |
|27 | CBSTM03B   | 230   | Statement Generation (batch - subroutine)     | Statements          | Subroutine          |
|28 | CBEXPORT   | 582   | Multi-record Data Export                      | Data Migration      | ETL / Export        |
|29 | CBIMPORT   | 487   | Multi-record Data Import                      | Data Migration      | ETL / Import        |
|30 | COBSWAIT   | 41    | Wait / Delay Utility                          | Infrastructure      | Utility             |

### 1.3 Shared Utility Programs

| # | Program    | Lines | Function                    | Business Domain | Classification |
|---|------------|-------|-----------------------------|-----------------|----------------|
|31 | CSUTLDTC   | 157   | Date Validation Utility     | Cross-cutting   | Utility        |

### 1.4 Optional Module: Authorization (IMS/DB2/MQ) (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program    | Lines | Function                                   | Classification       |
|---|------------|-------|--------------------------------------------|----------------------|
|32 | COPAUA0C   | 1,026 | MQ Trigger - Authorization Request Handler | MQ Integration       |
|33 | COPAUS0C   | 1,032 | Pending Auth Summary View                  | Inquiry              |
|34 | COPAUS1C   | 604   | Pending Auth Detail View                   | Inquiry              |
|35 | COPAUS2C   | 244   | Fraud Marking (write to DB2)               | Business Process     |
|36 | CBPAUP0C   | 386   | Batch Purge of Authorizations              | Batch Maintenance    |
|37 | PAUDBLOD   | 369   | IMS DB Load Utility                        | Utility / Load       |
|38 | PAUDBUNL   | 317   | IMS DB Unload Utility                      | Utility / Unload     |
|39 | DBUNLDGS   | 366   | GSAM Unload Utility                        | Utility / Unload     |

### 1.5 Optional Module: Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program    | Lines | Function                                  | Classification     |
|---|------------|-------|-------------------------------------------|--------------------|
|40 | COTRTLIC   | 2,098 | Transaction Type List/Delete (DB2 CRUD)   | List / CRUD        |
|41 | COTRTUPC   | 1,702 | Transaction Type Add/Edit (DB2 CRUD)      | CRUD (Create/Update)|
|42 | COBTUPDT   | 237   | Batch Transaction Type Update (DB2)       | Batch CRUD         |

### 1.6 Optional Module: VSAM-MQ (`app/app-vsam-mq/cbl/`)

| # | Program    | Lines | Function                         | Classification   |
|---|------------|-------|----------------------------------|------------------|
|43 | COACCT01   | 620   | MQ Account Inquiry Service       | MQ Service       |
|44 | CODATE01   | 524   | MQ System Date Service           | MQ Service       |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (prefix: `CV*` - VSAM Record Structures)

| # | Copybook   | Lines | Record Length | Entity                        | Used By (count) |
|---|------------|-------|---------------|-------------------------------|-----------------|
| 1 | CVACT01Y   | 21    | 300 bytes     | Account Master Record         | 11 programs      |
| 2 | CVACT02Y   | 15    | 150 bytes     | Card Master Record            | 8 programs       |
| 3 | CVACT03Y   | 12    | 50 bytes      | Card Cross-Reference Record   | 12 programs      |
| 4 | CVCUS01Y   | 27    | 500 bytes     | Customer Master Record        | 8 programs       |
| 5 | CVTRA05Y   | 22    | 350 bytes     | Transaction Record (Master)   | 11 programs      |
| 6 | CVTRA06Y   | 22    | 350 bytes     | Daily Transaction Record      | 2 programs       |
| 7 | CVTRA01Y   | 14    | 50 bytes      | Transaction Category Balance  | 2 programs       |
| 8 | CVTRA02Y   | 14    | 50 bytes      | Disclosure Group Record       | 1 program        |
| 9 | CVTRA03Y   | 11    | 60 bytes      | Transaction Type Record       | 1 program        |
|10 | CVTRA04Y   | 13    | 60 bytes      | Transaction Category Record   | 1 program        |
|11 | CVTRA07Y   | 74    | N/A           | Transaction Report Layout     | 1 program        |
|12 | CVCRD01Y   | 47    | N/A           | Card Work Area (AID/NAV)      | 4 programs       |
|13 | CVEXPORT   | 104   | 500 bytes     | Multi-Record Export Layout     | 2 programs       |
|14 | CUSTREC    | 27    | 500 bytes     | Customer Record (alternate)   | 1 program        |
|15 | COSTM01    | 38    | N/A           | Statement Transaction Layout  | 1 program        |

### 2.2 Application Communication / Infrastructure (prefix: `CO*`, `CS*`, `CC*`)

| # | Copybook   | Lines | Function                                   | Used By (count) |
|---|------------|-------|--------------------------------------------|-----------------|
|16 | COCOM01Y   | 48    | COMMAREA - Inter-program Communication     | 17 programs      |
|17 | COMEN02Y   | 102   | Main Menu Options Definition (11 options)  | 1 program        |
|18 | COADM02Y   | 63    | Admin Menu Options Definition (6 options)  | 1 program        |
|19 | COTTL01Y   | 28    | Screen Title Constants                     | 17 programs      |
|20 | CSDAT01Y   | 59    | Current Date/Time Working Storage          | 17 programs      |
|21 | CSMSG01Y   | 25    | Common Messages (Thank You, Invalid Key)   | 17 programs      |
|22 | CSMSG02Y   | 36    | Abend Handling Work Areas                  | 4 programs       |
|23 | CSUSR01Y   | 27    | User Security Record (80 bytes)            | 12 programs      |
|24 | CSSETATY   | 30    | Set Attribute Bytes (BMS field attributes) | 1 program        |
|25 | CSSTRPFY   | 85    | Store PF Key Mapping                       | 5 programs       |
|26 | CSUTLDPY   | 375   | Date Utility Parameters                    | 1 program        |
|27 | CSUTLDWY   | 89    | Date Edit Working Storage                  | 1 program        |
|28 | CSLKPCDY   | 1,318 | Lookup Code Tables                         | 1 program        |
|29 | CODATECN   | 52    | Date Conversion Constants                  | 1 program        |
|30 | UNUSED1Y   | 11    | Unused / Placeholder Record                | 0 programs       |

### 2.3 Optional Module Copybooks

**Authorization (IMS/DB2/MQ)** - `app/app-authorization-ims-db2-mq/cpy/`:

| # | Copybook   | Lines | Function                            |
|---|------------|-------|-------------------------------------|
|31 | CCPAUERY   | -     | Pending Auth Error Reply            |
|32 | CCPAURLY   | -     | Pending Auth Reply                  |
|33 | CCPAURQY   | -     | Pending Auth Request                |
|34 | CIPAUDTY   | -     | Pending Auth Detail                 |
|35 | CIPAUSMY   | -     | Pending Auth Summary                |
|36 | IMSFUNCS   | -     | IMS Function Codes                  |
|37 | PADFLPCB   | -     | IMS PCB - Detail Full               |
|38 | PASFLPCB   | -     | IMS PCB - Summary Full              |
|39 | PAUTBPCB   | -     | IMS PCB - Auth Table                |

**Transaction Type DB2** - `app/app-transaction-type-db2/cpy/`:

| # | Copybook   | Lines | Function                    |
|---|------------|-------|-----------------------------|
|40 | CSDB2RPY   | -     | DB2 Reply Structure         |
|41 | CSDB2RWY   | -     | DB2 Row Working Storage     |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map Name  | Associated Program | Screen Function                   |
|---|-----------|--------------------|------------------------------------|
| 1 | COSGN00   | COSGN00C           | Sign-on Screen                     |
| 2 | COMEN01   | COMEN01C           | Main Menu                          |
| 3 | COADM01   | COADM01C           | Admin Menu                         |
| 4 | COACTVW   | COACTVWC           | Account View                       |
| 5 | COACTUP   | COACTUPC           | Account Update                     |
| 6 | COCRDLI   | COCRDLIC           | Credit Card List                   |
| 7 | COCRDSL   | COCRDSLC           | Credit Card Detail View            |
| 8 | COCRDUP   | COCRDUPC           | Credit Card Update                 |
| 9 | COTRN00   | COTRN00C           | Transaction List                   |
|10 | COTRN01   | COTRN01C           | Transaction Detail View            |
|11 | COTRN02   | COTRN02C           | Transaction Add                    |
|12 | CORPT00   | CORPT00C           | Transaction Report Request         |
|13 | COBIL00   | COBIL00C           | Bill Payment                       |
|14 | COUSR00   | COUSR00C           | User List (Security)               |
|15 | COUSR01   | COUSR01C           | User Add (Security)                |
|16 | COUSR02   | COUSR02C           | User Update (Security)             |
|17 | COUSR03   | COUSR03C           | User Delete (Security)             |

**Optional Module BMS Maps:**

| # | Map Name  | Module         | Associated Program | Screen Function              |
|---|-----------|----------------|--------------------|-------------------------------|
|18 | COPAU00   | Auth IMS/DB2/MQ| COPAUS0C           | Pending Auth Summary          |
|19 | COPAU01   | Auth IMS/DB2/MQ| COPAUS1C           | Pending Auth Detail           |
|20 | COTRTLI   | Tran Type DB2  | COTRTLIC           | Transaction Type List         |
|21 | COTRTUP   | Tran Type DB2  | COTRTUPC           | Transaction Type Update       |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 auto-generated copybooks mirror the 17 core BMS maps above. Each provides symbolic field definitions (e.g., `COSGN00.CPY` maps to `COSGN00.bms`).

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data Refresh / Load Jobs

| # | Job        | Programs Executed | Function                                       |
|---|------------|-------------------|-------------------------------------------------|
| 1 | ACCTFILE   | IDCAMS            | Refresh Account Master VSAM from PS flat file   |
| 2 | CARDFILE   | IDCAMS, SDSF      | Refresh Card Master VSAM from PS flat file      |
| 3 | CUSTFILE   | IDCAMS, SDSF      | Refresh Customer Master VSAM from PS flat file  |
| 4 | XREFFILE   | IDCAMS            | Load Card Cross-Reference VSAM from PS flat file|
| 5 | TRANFILE   | IDCAMS, SDSF      | Load Transaction Master VSAM + init daily tran  |
| 6 | DUSRSECJ   | IDCAMS, IEBGENER  | Load User Security VSAM from PS flat file       |
| 7 | DISCGRP    | IDCAMS            | Load Disclosure Group VSAM from PS              |
| 8 | TRANCATG   | IDCAMS            | Load Transaction Category VSAM from PS          |
| 9 | TRANTYPE   | IDCAMS            | Load Transaction Type VSAM from PS              |
|10 | TCATBALF   | IDCAMS            | Load Transaction Category Balance VSAM from PS  |
|11 | DEFCUST    | IDCAMS            | Define Customer VSAM cluster                    |

### 4.2 Core Batch Processing Jobs

| # | Job        | Programs Executed         | Function                                    |
|---|------------|---------------------------|---------------------------------------------|
|12 | CLOSEFIL   | SDSF                      | Close CICS files for batch processing       |
|13 | OPENFIL    | SDSF                      | Re-open CICS files after batch processing   |
|14 | POSTTRAN   | CBTRN02C                  | Post daily transactions to master files     |
|15 | INTCALC    | CBACT04C                  | Calculate interest on accounts              |
|16 | TRANBKP    | IDCAMS                    | Backup transaction master VSAM to GDG       |
|17 | COMBTRAN   | IDCAMS, SORT              | Combine backed up + daily transactions      |
|18 | CREASTMT   | CBSTM03A, SORT, IDCAMS    | Generate account statements (HTML + PS)     |
|19 | TRANREPT   | CBTRN03C, SORT            | Generate daily transaction reports           |

### 4.3 Data Export / Import Jobs

| # | Job        | Programs Executed | Function                                       |
|---|------------|-------------------|-------------------------------------------------|
|20 | CBEXPORT   | CBEXPORT, IDCAMS  | Export all VSAM files to sequential export file |
|21 | CBIMPORT   | CBIMPORT          | Import sequential file back into VSAM files     |

### 4.4 Utility / Read Jobs

| # | Job        | Programs Executed | Function                                      |
|---|------------|-------------------|------------------------------------------------|
|22 | READACCT   | CBACT01C          | Read and display Account Master records        |
|23 | READCARD   | CBACT02C          | Read and display Card Master records           |
|24 | READCUST   | CBCUS01C          | Read and display Customer Master records       |
|25 | READXREF   | CBACT03C          | Read and display Card Cross-Reference records  |
|26 | PRTCATBL   | SORT              | Print Transaction Category Balance file        |
|27 | WAITSTEP   | COBSWAIT          | Wait / delay step for job scheduling           |

### 4.5 Infrastructure / Definition Jobs

| # | Job        | Programs Executed    | Function                                     |
|---|------------|----------------------|----------------------------------------------|
|28 | DEFGDGB    | IDCAMS              | Define GDG base entries                       |
|29 | DEFGDGD    | IDCAMS, IEBGENER    | Define GDG data + backup reference datasets   |
|30 | TRANIDX    | IDCAMS              | Define alternate index for transaction VSAM   |
|31 | DALYREJS   | IDCAMS              | Define daily rejects file                     |
|32 | REPTFILE   | IDCAMS              | Define report output file                     |
|33 | ESDSRRDS   | IDCAMS              | Define ESDS and RRDS test clusters            |
|34 | CBADMCDJ   | DFHCSDUP            | CICS CSD resource definitions                 |
|35 | TXT2PDF1   | IKJEFT1B            | Convert statement text to PDF                 |

### 4.6 FTP / Submission Jobs

| # | Job        | Programs Executed   | Function                                      |
|---|------------|---------------------|------------------------------------------------|
|36 | FTPJCL     | FTP                 | FTP transfer template                          |
|37 | INTRDRJ1   | IDCAMS, IEBGENER    | Internal reader job submission (step 1)        |
|38 | INTRDRJ2   | IDCAMS              | Internal reader job submission (step 2)        |

### 4.7 Optional Module JCL

**Authorization (IMS/DB2/MQ)** - `app/app-authorization-ims-db2-mq/jcl/`:

| # | Job        | Function                                  |
|---|------------|-------------------------------------------|
|39 | CBPAUP0J   | Batch purge of pending authorizations     |
|40 | DBPAUTP0   | IMS DB provisioning                       |
|41 | LOADPADB   | Load pending auth DB                      |
|42 | UNLDGSAM   | Unload via GSAM                           |
|43 | UNLDPADB   | Unload pending auth DB                    |

**Transaction Type DB2** - `app/app-transaction-type-db2/jcl/`:

| # | Job        | Function                                  |
|---|------------|-------------------------------------------|
|44 | CREADB21   | Create DB2 tables for transaction types   |
|45 | MNTTRDB2   | Maintain transaction type DB2 data        |
|46 | TRANEXTR   | Extract transaction type data from DB2    |

---

## 5. Assembler Programs (`app/asm/`)

| # | Program    | Function                                          |
|---|------------|---------------------------------------------------|
| 1 | MVSWAIT    | z/OS wait macro utility (used by COBSWAIT)        |
| 2 | COBDATFT   | Date format translation utility                   |

---

## 6. JCL Procedures (`app/proc/`)

| # | Procedure  | Function                                          |
|---|------------|---------------------------------------------------|
| 1 | REPROC     | Reusable report processing procedure              |
| 2 | TRANREPT   | Transaction report procedure                      |

---

## 7. Scheduler Configurations (`app/scheduler/`)

| # | Config          | Scheduler   | Function                                |
|---|-----------------|-------------|-----------------------------------------|
| 1 | CardDemo.ca7    | CA-7        | CA-7 job scheduling definitions         |
| 2 | CardDemo.controlm| Control-M  | Control-M job scheduling definitions    |

---

## 8. Data Files

### ASCII (`app/data/ASCII/`) - For testing modernized applications
### EBCDIC (`app/data/EBCDIC/`) - For mainframe upload

Sample data covers all entity types: accounts, cards, customers, transactions, cross-references, user security records, disclosure groups, transaction types, and categories.

---

## 9. Classification Summary

### By Business Domain

| Domain              | Online Programs | Batch Programs | Total |
|---------------------|-----------------|----------------|-------|
| Account Management  | 2               | 1              | 3     |
| Card Management     | 3               | 2              | 5     |
| Customer Management | 0               | 1              | 1     |
| Transaction Mgmt    | 3               | 3              | 6     |
| Payments            | 1               | 0              | 1     |
| Reporting           | 1               | 2              | 3     |
| Statements          | 0               | 2              | 2     |
| User Security       | 4               | 0              | 4     |
| Navigation          | 3               | 0              | 3     |
| Data Migration      | 0               | 2              | 2     |
| Infrastructure      | 0               | 1              | 1     |

### By Execution Mode

| Mode          | Count | Programs                                                    |
|---------------|-------|-------------------------------------------------------------|
| Online (CICS) | 17    | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, etc.    |
| Batch         | 13    | CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN03C, CBEXPORT, etc.  |
| Utility       | 1     | CSUTLDTC (called by online programs)                        |

### By Complexity Tier

| Tier     | LOC Range   | Count | Examples                                    |
|----------|-------------|-------|---------------------------------------------|
| High     | > 1,000     | 5     | COACTUPC, COCRDUPC, COCRDLIC, COPAUS0C, COTRTLIC |
| Medium   | 500-1,000   | 11    | COACTVWC, CBSTM03A, COCRDSLC, COTRN02C, etc.    |
| Low      | < 500       | 15    | COSGN00C, COMEN01C, COUSR01C, CBACT02C, etc.    |
