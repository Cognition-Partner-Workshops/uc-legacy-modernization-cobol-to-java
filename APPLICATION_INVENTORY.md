# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo - Credit Card Management System
> **Platform**: IBM Mainframe (z/OS) | **Runtime**: CICS/VSAM/JCL Batch

---

## Executive Summary

| Asset Type              | Count | Location                           |
|-------------------------|------:|-------------------------------------|
| COBOL Programs (Core)   |    31 | `app/cbl/`                          |
| COBOL Programs (Optional) | 13 | `app/app-*/cbl/`                    |
| Copybooks (Core)        |    30 | `app/cpy/`                          |
| Copybooks (BMS-generated)|   17 | `app/cpy-bms/`                      |
| Copybooks (Optional)    |    11 | `app/app-*/cpy/`                    |
| BMS Screen Maps (Core)  |    17 | `app/bms/`                          |
| BMS Screen Maps (Optional)|    4 | `app/app-*/bms/`                    |
| JCL Batch Jobs (Core)   |    38 | `app/jcl/`                          |
| JCL Batch Jobs (Optional)|     8 | `app/app-*/jcl/`                    |
| Assembler Programs      |     2 | `app/asm/`                          |
| JCL Procedures          |     2 | `app/proc/`                         |
| **Total Assets**        | **173** |                                   |

---

## 1. COBOL Programs - Core (`app/cbl/`)

### 1.1 Online CICS Programs

| # | Program    | Lines | CICS Txn | Function                                    | Classification      | BMS Map   |
|---|-----------|------:|----------|----------------------------------------------|---------------------|-----------|
| 1 | COSGN00C  |   302 | CC00     | User sign-on / authentication                | Security            | COSGN00   |
| 2 | COMEN01C  |   434 | CM00     | Main menu - route to sub-functions           | Navigation          | COMEN01   |
| 3 | COADM01C  |   288 | CA00     | Admin menu - admin-only functions            | Navigation / Admin  | COADM01   |
| 4 | COACTVWC  |   941 |          | View account details (read-only)             | Account Management  | COACTVW   |
| 5 | COACTUPC  | 4,236 |          | Update account information                   | Account Management  | COACTUP   |
| 6 | COCRDLIC  | 1,459 |          | List credit cards (paginated browse)         | Card Management     | COCRDLI   |
| 7 | COCRDSLC  |   887 |          | View credit card details                     | Card Management     | COCRDSL   |
| 8 | COCRDUPC  | 1,399 |          | Update credit card information               | Card Management     | COCRDUP   |
| 9 | COTRN00C  |   699 |          | List transactions (paginated browse)         | Transaction Mgmt    | COTRN00   |
|10 | COTRN01C  |   330 |          | View transaction details                     | Transaction Mgmt    | COTRN01   |
|11 | COTRN02C  |   783 |          | Add new transaction                          | Transaction Mgmt    | COTRN02   |
|12 | CORPT00C  |   349 |          | Transaction report selection screen           | Reporting           | CORPT00   |
|13 | COBIL00C  |   572 |          | Bill payment - pay balance or custom amount  | Bill Payment        | COBIL00   |
|14 | COUSR00C  |   695 |          | List users (admin, paginated browse)         | User Administration | COUSR00   |
|15 | COUSR01C  |   299 |          | Add new user (admin)                         | User Administration | COUSR01   |
|16 | COUSR02C  |   414 |          | Update existing user (admin)                 | User Administration | COUSR02   |
|17 | COUSR03C  |   359 |          | Delete user (admin)                          | User Administration | COUSR03   |

### 1.2 Batch Programs

| # | Program    | Lines | Function                                         | Classification          |
|---|-----------|------:|--------------------------------------------------|-------------------------|
|18 | CBACT01C  |   430 | Read account file, write to output/array files   | Data Processing         |
|19 | CBACT02C  |   178 | Read and print card data file                    | Data Reporting          |
|20 | CBACT03C  |   178 | Read and print cross-reference data file         | Data Reporting          |
|21 | CBACT04C  |   652 | Interest calculation on transaction balances      | Financial Calculation   |
|22 | CBCUS01C  |   178 | Read and print customer data file                | Data Reporting          |
|23 | CBTRN01C  |   494 | Post daily transactions to master file           | Transaction Processing  |
|24 | CBTRN02C  |   731 | Validate & post daily transactions with rejects  | Transaction Processing  |
|25 | CBTRN03C  |   649 | Print transaction detail report                  | Reporting               |
|26 | CBSTM03A  |   924 | Generate account statements (main driver)        | Statement Generation    |
|27 | CBSTM03B  |   230 | Statement generation subroutine (file I/O)       | Statement Generation    |
|28 | CBEXPORT  |   582 | Export all VSAM data to sequential file           | Data Export             |
|29 | CBIMPORT  |   487 | Import sequential file back into VSAM files       | Data Import             |
|30 | COBSWAIT  |    41 | Utility - wait for specified centiseconds        | Utility                 |

### 1.3 Shared Utility Programs

| # | Program    | Lines | Function                              | Classification |
|---|-----------|------:|----------------------------------------|----------------|
|31 | CSUTLDTC  |   157 | Date conversion utility (calls CEEDAYS)| Utility        |

---

## 2. COBOL Programs - Optional Modules

### 2.1 Authorization Module - IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program    | Lines | Type         | Function                                       |
|---|-----------|------:|--------------|-------------------------------------------------|
| 1 | COPAUA0C  | 1,026 | CICS/IMS/MQ  | Card authorization decision engine (MQ trigger) |
| 2 | COPAUS0C  | 1,032 | CICS/IMS/BMS | Summary view of authorization messages          |
| 3 | COPAUS1C  |   604 | CICS/IMS/BMS | Detail view of authorization message            |
| 4 | COPAUS2C  |   244 | CICS/DB2     | Mark authorization as fraud (DB2 update)        |
| 5 | CBPAUP0C  |   386 | Batch/IMS    | Purge expired pending authorization messages    |
| 6 | PAUDBLOD  |   --- | Batch/IMS    | Load authorization IMS database                 |
| 7 | PAUDBUNL  |   --- | Batch/IMS    | Unload authorization IMS database               |
| 8 | DBUNLDGS  |   --- | Batch/IMS    | Unload IMS DB to GSAM                           |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program    | Lines | Type      | Function                                    |
|---|-----------|------:|-----------|----------------------------------------------|
| 9 | COTRTLIC  | 2,098 | CICS/DB2  | List transaction types (DB2 cursor paging)   |
|10 | COTRTUPC  | 1,702 | CICS/DB2  | Add/update transaction type (DB2 CRUD)       |
|11 | COBTUPDT  |   237 | Batch/DB2 | Batch update transaction types from input    |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program    | Lines | Type      | Function                                      |
|---|-----------|------:|-----------|------------------------------------------------|
|12 | COACCT01  |   620 | CICS/MQ   | MQ request/response for account inquiry        |
|13 | CODATE01  |   524 | CICS/MQ   | MQ request/response for system date            |

---

## 3. Copybooks - Core (`app/cpy/`)

### 3.1 Business Data Record Layouts

| # | Copybook   | Lines | Record Size | Business Entity            | Used By                              |
|---|-----------|------:|------------:|----------------------------|---------------------------------------|
| 1 | CVACT01Y  |    20 |    300 bytes | Account Master             | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN02C, CBEXPORT, CBIMPORT |
| 2 | CVACT02Y  |    14 |    150 bytes | Credit Card                | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT |
| 3 | CVACT03Y  |    11 |     50 bytes | Card-Account Cross-Reference| COACTUPC, COACTVWC, CBACT03C, CBACT04C, CBTRN02C, CBEXPORT |
| 4 | CVCUS01Y  |    26 |    500 bytes | Customer Master            | COCRDSLC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT |
| 5 | CVTRA05Y  |    21 |    350 bytes | Transaction (Online)       | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBACT04C, CBTRN02C, CBEXPORT |
| 6 | CVTRA06Y  |    21 |    350 bytes | Daily Transaction          | CBTRN01C, CBTRN02C                    |
| 7 | CVTRA01Y  |    13 |     50 bytes | Transaction Category Balance| CBACT04C, CBTRN02C                   |
| 8 | CVTRA02Y  |    13 |     50 bytes | Discount Group / Interest Rate | CBACT04C                          |
| 9 | CVTRA03Y  |    10 |     60 bytes | Transaction Type           | CBTRN03C                              |
|10 | CVTRA04Y  |    12 |     60 bytes | Transaction Category Type  | CBTRN03C                              |
|11 | CVTRA07Y  |    73 |        ---   | Transaction Report Layout  | CBTRN03C                              |
|12 | CUSTREC   |    26 |    500 bytes | Customer Record (alternate)| CBSTM03A                              |
|13 | CVEXPORT  |   103 |    500 bytes | Export/Import Record       | CBEXPORT, CBIMPORT                    |
|14 | COSTM01   |    38 |    350 bytes | Statement Transaction      | CBSTM03A                              |
|15 | CSUSR01Y  |    26 |     80 bytes | User Security Record       | COSGN00C, COUSR00C-03C, COADM01C     |
|16 | UNUSED1Y  |    10 |     80 bytes | Unused placeholder         | (None - dead code)                    |

### 3.2 Screen / Navigation / Utility Copybooks

| # | Copybook   | Lines | Function                                   | Used By                       |
|---|-----------|------:|--------------------------------------------|-------------------------------|
|17 | COCOM01Y  |    47 | Common communication area (COMMAREA)       | All online programs           |
|18 | COMEN02Y  |   101 | Main menu option definitions (11 options)  | COMEN01C                      |
|19 | COADM02Y  |    62 | Admin menu option definitions (6 options)  | COADM01C                      |
|20 | COTTL01Y  |    27 | Application title/header constants         | All online programs           |
|21 | CSDAT01Y  |    58 | Date/time working storage fields           | All online programs           |
|22 | CSMSG01Y  |    24 | Standard message constants                 | All online programs           |
|23 | CSMSG02Y  |    35 | Abend message area                         | Programs with error handling  |
|24 | CVCRD01Y  |    46 | Card detail context block                  | COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC |
|25 | CSLKPCDY  | 1,318 | US phone area codes & state/ZIP lookups    | COACTUPC                      |
|26 | CSSETATY  |    30 | Screen attribute settings                  | Online programs               |
|27 | CSSTRPFY  |    85 | String strip/pad functions                 | Online programs               |
|28 | CSUTLDPY  |   375 | Date utility display paragraphs            | Date utility programs         |
|29 | CSUTLDWY  |    89 | Date edit/validation working storage       | COACTUPC, COTRTUPC            |
|30 | CODATECN  |    52 | Date conversion record (assembler call)    | CBACT01C                      |

---

## 4. BMS Screen Maps

### 4.1 Core Maps (`app/bms/`)

| # | Map File   | Lines | Map Name | Screen Function                | Associated Program |
|---|-----------|------:|----------|--------------------------------|--------------------|
| 1 | COSGN00   |   210 | COSGN0A  | Sign-on screen                 | COSGN00C           |
| 2 | COMEN01   |   478 | COMEN1A  | Main menu                      | COMEN01C           |
| 3 | COADM01   |   258 | COADM1A  | Admin menu                     | COADM01C           |
| 4 | COACTVW   |   337 | CACTVWA  | Account view                   | COACTVWC           |
| 5 | COACTUP   |   426 | CACTUPA  | Account update                 | COACTUPC           |
| 6 | COCRDLI   |   509 | CCRDLIA  | Card list                      | COCRDLIC           |
| 7 | COCRDSL   |   291 | CCRDSLA  | Card detail view               | COCRDSLC           |
| 8 | COCRDUP   |   371 | CCRDUPA  | Card update                    | COCRDUPC           |
| 9 | COTRN00   |   464 | COTRN0A  | Transaction list               | COTRN00C           |
|10 | COTRN01   |   273 | COTRN1A  | Transaction detail view        | COTRN01C           |
|11 | COTRN02   |   307 | COTRN2A  | Transaction add                | COTRN02C           |
|12 | CORPT00   |   231 | CORPT0A  | Report selection               | CORPT00C           |
|13 | COBIL00   |   190 | COBIL0A  | Bill payment                   | COBIL00C           |
|14 | COUSR00   |   463 | COUSR0A  | User list                      | COUSR00C           |
|15 | COUSR01   |   164 | COUSR1A  | User add                       | COUSR01C           |
|16 | COUSR02   |   169 | COUSR2A  | User update                    | COUSR02C           |
|17 | COUSR03   |   153 | COUSR3A  | User delete                    | COUSR03C           |

### 4.2 BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map generates a symbolic description copybook (DSECT) used in the corresponding COBOL program:

| BMS Source | Generated Copybook | Purpose                             |
|------------|-------------------|-------------------------------------|
| COSGN00    | COSGN00.CPY       | Sign-on screen field definitions    |
| COMEN01    | COMEN01.CPY       | Main menu screen field definitions  |
| COADM01    | COADM01.CPY       | Admin menu screen field definitions |
| COACTUP    | COACTUP.CPY       | Account update field definitions    |
| COACTVW    | COACTVW.CPY       | Account view field definitions      |
| COCRDLI    | COCRDLI.CPY       | Card list field definitions         |
| COCRDSL    | COCRDSL.CPY       | Card detail field definitions       |
| COCRDUP    | COCRDUP.CPY       | Card update field definitions       |
| COTRN00    | COTRN00.CPY       | Transaction list field definitions  |
| COTRN01    | COTRN01.CPY       | Transaction view field definitions  |
| COTRN02    | COTRN02.CPY       | Transaction add field definitions   |
| CORPT00    | CORPT00.CPY       | Report selection field definitions  |
| COBIL00    | COBIL00.CPY       | Bill payment field definitions      |
| COUSR00    | COUSR00.CPY       | User list field definitions         |
| COUSR01    | COUSR01.CPY       | User add field definitions          |
| COUSR02    | COUSR02.CPY       | User update field definitions       |
| COUSR03    | COUSR03.CPY       | User delete field definitions       |

### 4.3 Optional Module BMS Maps

| # | Module                      | Map File  | Screen Function                  |
|---|----------------------------|-----------|----------------------------------|
| 1 | Authorization (IMS/DB2/MQ) | COPAU00   | Authorization summary list       |
| 2 | Authorization (IMS/DB2/MQ) | COPAU01   | Authorization detail view        |
| 3 | Transaction Type (DB2)     | COTRTLI   | Transaction type list            |
| 4 | Transaction Type (DB2)     | COTRTUP   | Transaction type add/update      |

---

## 5. JCL Batch Jobs

### 5.1 Core JCL (`app/jcl/`)

#### Data Refresh Jobs

| # | Job        | Lines | Function                                   | Programs Executed      |
|---|-----------|------:|--------------------------------------------|------------------------|
| 1 | ACCTFILE  |   102 | Delete/define and reload account VSAM KSDS | IDCAMS                 |
| 2 | CARDFILE  |   102 | Delete/define and reload card VSAM KSDS    | IDCAMS                 |
| 3 | CUSTFILE  |    78 | Delete/define and reload customer VSAM KSDS| IDCAMS                 |
| 4 | XREFFILE  |   106 | Delete/define and reload cross-ref VSAM KSDS + AIX | IDCAMS         |
| 5 | TRANFILE  |   125 | Delete/define and reload transaction VSAM KSDS + AIX | IDCAMS       |
| 6 | DUSRSECJ  |    84 | Delete/define and reload user security VSAM KSDS | IDCAMS           |
| 7 | TRANTYPE  |    65 | Load transaction type reference data       | IDCAMS                 |
| 8 | TRANCATG  |    65 | Load transaction category reference data   | IDCAMS                 |
| 9 | TCATBALF  |    65 | Load transaction category balance data     | IDCAMS                 |
|10 | DISCGRP   |    65 | Load discount group / interest rate data   | IDCAMS                 |

#### Batch Processing Jobs

| # | Job        | Lines | Function                                     | Programs Executed        |
|---|-----------|------:|----------------------------------------------|--------------------------|
|11 | POSTTRAN  |    87 | Core transaction posting cycle               | CBTRN01C, CBTRN02C       |
|12 | INTCALC   |    72 | Interest calculation                         | CBACT04C                 |
|13 | COMBTRAN  |    42 | Combine daily transactions into master       | SORT, IDCAMS             |
|14 | CREASTMT  |    97 | Generate account statements (HTML + text)    | SORT, IDCAMS, CBSTM03A   |
|15 | TRANBKP   |    71 | Backup transaction file to GDG              | REPRO (proc), IDCAMS     |
|16 | TRANREPT  |    84 | Generate transaction detail report           | REPRO (proc), SORT, CBTRN03C |
|17 | TRANIDX   |    58 | Define alternate indexes on transaction file | IDCAMS                   |

#### File Management Jobs

| # | Job        | Lines | Function                                | Programs Executed |
|---|-----------|------:|-----------------------------------------|-------------------|
|18 | CLOSEFIL  |    30 | Close CICS-owned VSAM files for batch   | SDSF (CEMT)       |
|19 | OPENFIL   |    30 | Reopen CICS-owned VSAM files            | SDSF (CEMT)       |

#### Data Read/Print Jobs

| # | Job        | Lines | Function                         | Programs Executed |
|---|-----------|------:|----------------------------------|-------------------|
|20 | READACCT  |    31 | Read and print account records   | CBACT01C          |
|21 | READCARD  |    31 | Read and print card records      | CBACT02C          |
|22 | READCUST  |    31 | Read and print customer records  | CBCUS01C          |
|23 | READXREF  |    31 | Read and print cross-ref records | CBACT03C          |

#### Utility / Infrastructure Jobs

| # | Job        | Lines | Function                                    | Programs Executed   |
|---|-----------|------:|---------------------------------------------|---------------------|
|24 | DEFGDGB   |    19 | Define GDG base for transaction backups     | IDCAMS              |
|25 | DEFGDGD   |    23 | Define GDG base for daily transactions      | IDCAMS              |
|26 | DEFCUST   |    24 | Define customer VSAM cluster (alternate)    | IDCAMS              |
|27 | ESDSRRDS  |    77 | Define ESDS/RRDS VSAM clusters (demo)       | IDCAMS              |
|28 | PRTCATBL  |    14 | Print catalog listing                       | IDCAMS              |
|29 | REPTFILE  |    32 | Define report output sequential file        | IDCAMS              |
|30 | DALYREJS  |    63 | Define daily rejects sequential file        | IDCAMS              |
|31 | WAITSTEP  |    27 | Wait step (calls COBSWAIT)                  | COBSWAIT            |
|32 | CBEXPORT  |    50 | Export all VSAM to sequential file          | CBEXPORT            |
|33 | CBIMPORT  |    50 | Import sequential file into VSAM            | CBIMPORT            |
|34 | FTPJCL    |    42 | FTP file transfer job                       | FTP                 |
|35 | INTRDRJ1  |    19 | Internal reader job chain (step 1)          | IDCAMS, IEBGENER    |
|36 | INTRDRJ2  |    14 | Internal reader job chain (step 2)          | IDCAMS              |
|37 | TXT2PDF1  |    41 | Convert text statement to PDF               | IKJEFT1B (TXT2PDF)  |
|38 | CBADMCDJ  |    --- | Admin card maintenance batch                | (Various)           |

### 5.2 Optional Module JCL

#### Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job        | Function                                  |
|---|-----------|-------------------------------------------|
| 1 | CBPAUP0J  | Purge expired authorization messages      |
| 2 | DBPAUTP0  | Authorization DB2 table processing        |
| 3 | LOADPADB  | Load authorization IMS database           |
| 4 | UNLDPADB  | Unload authorization IMS database         |
| 5 | UNLDGSAM  | Unload IMS DB to GSAM sequential file     |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`)

| # | Job        | Function                                   |
|---|-----------|---------------------------------------------|
| 6 | CREADB21  | Create DB2 tables for transaction types     |
| 7 | MNTTRDB2  | Maintain transaction type DB2 data          |
| 8 | TRANEXTR  | Extract transaction types from DB2          |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program    | Function                                              |
|---|-----------|-------------------------------------------------------|
| 1 | COBDATFT  | Date formatting assembler subroutine (called by CBACT01C) |
| 2 | MVSWAIT   | Wait utility assembler subroutine (called by COBSWAIT)    |

---

## 7. JCL Procedures (`app/proc/`)

| # | Procedure  | Function                                   |
|---|-----------|---------------------------------------------|
| 1 | REPROC    | Reusable REPRO (IDCAMS copy) procedure      |
| 2 | TRANREPT  | Transaction report generation procedure     |

---

## 8. Naming Conventions

| Prefix | Meaning                                |
|--------|----------------------------------------|
| `CO*`  | Online CICS program                    |
| `CB*`  | Batch COBOL program                    |
| `CS*`  | Common/shared service copybook         |
| `CV*`  | VSAM data record layout copybook       |
| `CC*`  | Card/control context copybook          |
| `CI*`  | IMS-related copybook                   |
| `PA*`  | Pending Authorization (IMS module)     |
| `*Y`   | Copybook suffix (data definitions)     |

---

## 9. Technology Stack

| Layer           | Technology                                |
|-----------------|-------------------------------------------|
| Language        | COBOL (Enterprise COBOL for z/OS)         |
| Online TP       | CICS Transaction Server                   |
| Screen UI       | BMS (Basic Mapping Support) - 3270 maps   |
| File System     | VSAM (KSDS, with AIX alternate indexes)   |
| Batch Scheduler | JCL / JES2, CA7, Control-M                |
| Database (opt)  | IBM DB2 (Transaction Type module)         |
| Messaging (opt) | IBM MQ Series (Authorization, VSAM-MQ)    |
| Hierarchical DB | IMS DB (Authorization module)             |
| Utilities       | IDCAMS, SORT, IEBGENER, FTP              |
| Assembler       | z/OS Assembler (date format, wait)        |
