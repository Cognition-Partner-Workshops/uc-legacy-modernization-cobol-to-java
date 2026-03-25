# Application Inventory - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Platform:** IBM Mainframe (COBOL/CICS/VSAM/JCL) | **Architecture:** Online CICS + Batch

---

## Summary

| Artifact Type          | Count | Location               |
|------------------------|------:|------------------------|
| COBOL Programs (Core)  |    31 | `app/cbl/`             |
| COBOL Programs (Ext.)  |    13 | `app/app-*/cbl/`       |
| Copybooks (Core)       |    30 | `app/cpy/`             |
| Copybooks (BMS-gen.)   |    17 | `app/cpy-bms/`         |
| Copybooks (Ext.)       |    11 | `app/app-*/cpy/`       |
| BMS Maps (Core)        |    17 | `app/bms/`             |
| BMS Maps (Ext.)        |     4 | `app/app-*/bms/`       |
| JCL Jobs (Core)        |    38 | `app/jcl/`             |
| JCL Jobs (Ext.)        |     8 | `app/app-*/jcl/`       |
| Assembler Programs     |     2 | `app/asm/`             |
| JCL Procedures         |     2 | `app/proc/`            |
| Scheduler Configs      |     2 | `app/scheduler/`       |
| **Total Artifacts**    |**175**|                        |

---

## 1. COBOL Programs - Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

These programs run under CICS and implement the interactive 3270 terminal interface.

| # | Program      | Lines | CICS Txn | Function                                      | Classification         |
|---|-------------|------:|----------|-----------------------------------------------|------------------------|
| 1 | COSGN00C    |   260 | CC00     | User sign-on / authentication                 | Security               |
| 2 | COMEN01C    |   308 | CM00     | Main menu for regular users                   | Navigation             |
| 3 | COADM01C    |   288 | CA00     | Admin menu for admin users                    | Navigation / Admin     |
| 4 | COACTVWC    |   941 | CA01     | View account details                          | Account Management     |
| 5 | COACTUPC    | 4,236 | CA02     | Update account details                        | Account Management     |
| 6 | COCRDLIC    | 1,459 | CC01     | List credit cards for an account              | Card Management        |
| 7 | COCRDSLC    |   887 | CC02     | View credit card details                      | Card Management        |
| 8 | COCRDUPC    | 1,560 | CC03     | Update credit card details                    | Card Management        |
| 9 | COTRN00C    |   699 | CT00     | List transactions                             | Transaction Management |
|10 | COTRN01C    |   330 | CT01     | View a single transaction                     | Transaction Management |
|11 | COTRN02C    |   783 | CT02     | Add a new transaction                         | Transaction Management |
|12 | CORPT00C    |   649 | CR00     | Submit batch transaction reports               | Reporting              |
|13 | COBIL00C    |   572 | CB00     | Bill payment (full / partial)                 | Billing / Payments     |
|14 | COUSR00C    |   695 | CU00     | List all users (admin security)               | User Administration    |
|15 | COUSR01C    |   299 | CU01     | Add a new user                                | User Administration    |
|16 | COUSR02C    |   414 | CU02     | Update an existing user                       | User Administration    |
|17 | COUSR03C    |   359 | CU03     | Delete a user                                 | User Administration    |

### 1.2 Batch Programs (prefix `CB*`)

These programs run as batch jobs submitted through JCL.

| # | Program      | Lines | Function                                            | Classification            |
|---|-------------|------:|-----------------------------------------------------|---------------------------|
|18 | CBACT01C    |   430 | Read account file, split into multiple output files  | Account Processing        |
|19 | CBACT02C    |   178 | Read and print card data file                        | Card Processing           |
|20 | CBACT03C    |   178 | Read and print card cross-reference file             | Cross-Reference Processing|
|21 | CBACT04C    |   652 | Interest calculation on accounts                     | Financial Calculation     |
|22 | CBCUS01C    |   178 | Read and print customer data file                    | Customer Processing       |
|23 | CBTRN01C    |   494 | Post daily transaction records (combine into master) | Transaction Processing    |
|24 | CBTRN02C    |   731 | Post daily transaction records (with category bal.)  | Transaction Processing    |
|25 | CBTRN03C    |   649 | Print transaction detail report                      | Reporting                 |
|26 | CBSTM03A    |   924 | Print account statements from transaction data       | Statement Generation      |
|27 | CBSTM03B    |   230 | Statement file I/O subroutine (called by CBSTM03A)  | Statement Generation      |
|28 | CBEXPORT    |   582 | Export data for branch migration (multi-record)      | Data Migration            |
|29 | CBIMPORT    |   487 | Import data from branch migration export             | Data Migration            |

### 1.3 Utility Programs

| # | Program      | Lines | Function                                           | Classification    |
|---|-------------|------:|----------------------------------------------------|-------------------|
|30 | CSUTLDTC    |   157 | Date validation utility (calls CEEDAYS LE service) | Shared Utility    |
|31 | COBSWAIT    |    41 | Wait utility (calls assembler MVSWAIT)             | Shared Utility    |

---

## 2. COBOL Programs - Optional Extension Modules

### 2.1 Authorization Module - IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program      | Function                                                  | Classification              |
|---|-------------|-----------------------------------------------------------|-----------------------------|
|32 | COPAUA0C    | MQ trigger monitor - receives authorization requests      | Authorization / MQ          |
|33 | COPAUS0C    | Online - View pending authorization summary               | Authorization / Online      |
|34 | COPAUS1C    | Online - View pending authorization details               | Authorization / Online      |
|35 | COPAUS2C    | Online - Mark authorization as fraud (writes to DB2)      | Authorization / Fraud Mgmt  |
|36 | CBPAUP0C    | Batch - Purge old pending authorizations                  | Authorization / Batch       |
|37 | DBUNLDGS    | Batch - Unload IMS GSAM data                              | Data Utility                |
|38 | PAUDBLOD    | Batch - Load pending authorization DB                     | Data Utility                |
|39 | PAUDBUNL    | Batch - Unload pending authorization DB                   | Data Utility                |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program      | Function                                                  | Classification              |
|---|-------------|-----------------------------------------------------------|-----------------------------|
|40 | COTRTLIC    | Online - List/delete transaction types (DB2 cursor)       | Reference Data / Online     |
|41 | COTRTUPC    | Online - Add/edit transaction types (DB2 insert/update)   | Reference Data / Online     |
|42 | COBTUPDT    | Batch - Update transaction types in DB2                   | Reference Data / Batch      |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program      | Function                                                  | Classification              |
|---|-------------|-----------------------------------------------------------|-----------------------------|
|43 | CODATE01    | MQ request/response - Return system date                  | Integration / MQ            |
|44 | COACCT01    | MQ request/response - Account inquiry from VSAM           | Integration / MQ            |

---

## 3. Copybooks - Core (`app/cpy/`)

### 3.1 Data Record Layouts (prefix `CV*`)

| # | Copybook     | Record Name               | Rec Len | Business Entity                        |
|---|-------------|---------------------------|--------:|----------------------------------------|
| 1 | CVACT01Y    | ACCOUNT-RECORD            |     300 | Account master                         |
| 2 | CVACT02Y    | CARD-RECORD               |     150 | Credit card master                     |
| 3 | CVACT03Y    | CARD-XREF-RECORD          |      50 | Card-to-customer cross-reference       |
| 4 | CVCUS01Y    | CUSTOMER-RECORD           |     500 | Customer master                        |
| 5 | CVCRD01Y    | CC-WORK-AREAS             |     var | Card list working area / navigation    |
| 6 | CVTRA01Y    | TRAN-CAT-BAL-RECORD       |      50 | Transaction category balance           |
| 7 | CVTRA02Y    | DIS-GROUP-RECORD          |      50 | Disclosure group (interest rates)      |
| 8 | CVTRA03Y    | TRAN-TYPE-RECORD          |      60 | Transaction type reference             |
| 9 | CVTRA04Y    | TRAN-CAT-RECORD           |      60 | Transaction category type reference    |
|10 | CVTRA05Y    | TRAN-RECORD               |     350 | Transaction master                     |
|11 | CVTRA06Y    | DALYTRAN-RECORD           |     350 | Daily transaction (input)              |
|12 | CVTRA07Y    | REPORT structures          |     var | Transaction report headers/totals      |
|13 | CVEXPORT    | EXPORT-RECORD             |     500 | Multi-record export layout (REDEFINES) |

### 3.2 Common/Shared Copybooks (prefix `CO*`, `CS*`, `CU*`)

| # | Copybook     | Structure                  | Purpose                                  |
|---|-------------|----------------------------|------------------------------------------|
|14 | COCOM01Y    | CARDDEMO-COMMAREA          | CICS communication area between programs |
|15 | COMEN02Y    | CARDDEMO-MAIN-MENU-OPTIONS | Main menu option definitions (11 items)  |
|16 | COADM02Y    | CARDDEMO-ADMIN-MENU-OPTIONS| Admin menu option definitions (6 items)  |
|17 | COTTL01Y    | CCDA-SCREEN-TITLE          | Screen title / header text               |
|18 | CSDAT01Y    | WS-DATE-TIME               | Date/time working storage fields         |
|19 | CSMSG01Y    | CCDA-COMMON-MESSAGES       | Common user messages                     |
|20 | CSMSG02Y    | ABEND-DATA                 | Abend handling data area                 |
|21 | CSUSR01Y    | SEC-USER-DATA              | User security record (80 bytes)          |
|22 | CSLKPCDY    | Lookup code tables          | Phone area codes, state codes, zip codes |
|23 | CSSETATY    | (inline code)              | Set screen attribute (color/flag)        |
|24 | CSSTRPFY    | (inline code)              | Store PF-key from EIBAID into COMMAREA   |
|25 | CSUTLDPY    | (inline code)              | Date validation procedure division       |
|26 | CSUTLDWY    | WS-EDIT-DATE-*             | Date validation working storage          |
|27 | CODATECN    | CODATECN-REC               | Date conversion record layout            |
|28 | COSTM01     | TRNX-RECORD                | Transaction altered layout for reporting |
|29 | CUSTREC     | CUSTOMER-RECORD            | Alternate customer record layout         |
|30 | UNUSED1Y    | UNUSED-DATA                | Unused placeholder copybook              |

### 3.3 BMS-Generated Copybooks (`app/cpy-bms/`)

| #  | Copybook     | Associated BMS Map | Screen                     |
|----|-------------|-------------------|----------------------------|
| 1  | COSGN00.CPY | COSGN00.bms       | Sign-on screen             |
| 2  | COMEN01.CPY | COMEN01.bms       | Main menu                  |
| 3  | COADM01.CPY | COADM01.bms       | Admin menu                 |
| 4  | COACTVW.CPY | COACTVW.bms       | Account view               |
| 5  | COACTUP.CPY | COACTUP.bms       | Account update             |
| 6  | COCRDLI.CPY | COCRDLI.bms       | Card list                  |
| 7  | COCRDSL.CPY | COCRDSL.bms       | Card detail view           |
| 8  | COCRDUP.CPY | COCRDUP.bms       | Card update                |
| 9  | COTRN00.CPY | COTRN00.bms       | Transaction list           |
| 10 | COTRN01.CPY | COTRN01.bms       | Transaction view           |
| 11 | COTRN02.CPY | COTRN02.bms       | Transaction add            |
| 12 | CORPT00.CPY | CORPT00.bms       | Transaction report params  |
| 13 | COBIL00.CPY | COBIL00.bms       | Bill payment               |
| 14 | COUSR00.CPY | COUSR00.bms       | User list                  |
| 15 | COUSR01.CPY | COUSR01.bms       | User add                   |
| 16 | COUSR02.CPY | COUSR02.bms       | User update                |
| 17 | COUSR03.CPY | COUSR03.bms       | User delete                |

### 3.4 Extension Module Copybooks

**Authorization module** (`app/app-authorization-ims-db2-mq/cpy/`):

| Copybook     | Purpose                                         |
|-------------|--------------------------------------------------|
| CCPAUERY    | Pending authorization error reply layout         |
| CCPAURLY    | Pending authorization reply layout               |
| CCPAURQY    | Pending authorization request layout             |
| CIPAUDTY    | Pending authorization detail layout              |
| CIPAUSMY    | Pending authorization summary layout             |
| IMSFUNCS    | IMS function codes / constants                   |
| PADFLPCB    | IMS PCB - detail database                        |
| PASFLPCB    | IMS PCB - summary database                       |
| PAUTBPCB    | IMS PCB - authorization table                    |

**Transaction Type DB2 module** (`app/app-transaction-type-db2/cpy/`):

| Copybook     | Purpose                                         |
|-------------|--------------------------------------------------|
| CSDB2RPY    | DB2 reply area for transaction type operations   |
| CSDB2RWY    | DB2 working storage for transaction type ops     |

---

## 4. BMS Maps (`app/bms/`)

| # | Map File     | Mapset   | Screen Purpose                    | Associated Program |
|---|-------------|----------|-----------------------------------|--------------------|
| 1 | COSGN00.bms | COSGN00  | Sign-on / Login                   | COSGN00C           |
| 2 | COMEN01.bms | COMEN01  | Main Menu (Regular Users)         | COMEN01C           |
| 3 | COADM01.bms | COADM01  | Admin Menu                        | COADM01C           |
| 4 | COACTVW.bms | COACTVW  | Account View                      | COACTVWC           |
| 5 | COACTUP.bms | COACTUP  | Account Update                    | COACTUPC           |
| 6 | COCRDLI.bms | COCRDLI  | Credit Card List                  | COCRDLIC           |
| 7 | COCRDSL.bms | COCRDSL  | Credit Card Detail View           | COCRDSLC           |
| 8 | COCRDUP.bms | COCRDUP  | Credit Card Update                | COCRDUPC           |
| 9 | COTRN00.bms | COTRN00  | Transaction List                  | COTRN00C           |
|10 | COTRN01.bms | COTRN01  | Transaction View                  | COTRN01C           |
|11 | COTRN02.bms | COTRN02  | Transaction Add                   | COTRN02C           |
|12 | CORPT00.bms | CORPT00  | Transaction Report Parameters     | CORPT00C           |
|13 | COBIL00.bms | COBIL00  | Bill Payment                      | COBIL00C           |
|14 | COUSR00.bms | COUSR00  | User List (Admin)                 | COUSR00C           |
|15 | COUSR01.bms | COUSR01  | User Add (Admin)                  | COUSR01C           |
|16 | COUSR02.bms | COUSR02  | User Update (Admin)               | COUSR02C           |
|17 | COUSR03.bms | COUSR03  | User Delete (Admin)               | COUSR03C           |

**Extension module BMS maps:**

| Map File          | Module                          | Screen Purpose                 |
|-------------------|---------------------------------|--------------------------------|
| COPAU00.bms       | Authorization (IMS/DB2/MQ)      | Pending Authorization Summary  |
| COPAU01.bms       | Authorization (IMS/DB2/MQ)      | Pending Authorization Detail   |
| COTRTLI.bms       | Transaction Type (DB2)          | Transaction Type List          |
| COTRTUP.bms       | Transaction Type (DB2)          | Transaction Type Add/Edit      |

---

## 5. JCL Jobs - Core (`app/jcl/`)

### 5.1 VSAM File Definition & Data Loading

| # | Job          | Purpose                                              | Category            |
|---|-------------|------------------------------------------------------|---------------------|
| 1 | ACCTFILE    | Define and load Account master VSAM KSDS             | Data Setup          |
| 2 | CARDFILE    | Define and load Card master VSAM KSDS                | Data Setup          |
| 3 | CUSTFILE    | Define and load Customer master VSAM KSDS            | Data Setup          |
| 4 | XREFFILE    | Define and load Card cross-reference VSAM            | Data Setup          |
| 5 | TRANFILE    | Define and load Transaction master VSAM              | Data Setup          |
| 6 | DUSRSECJ    | Define and load User security VSAM KSDS              | Data Setup          |
| 7 | TRANTYPE    | Define Transaction type VSAM                         | Data Setup          |
| 8 | TRANCATG    | Define Transaction category VSAM                     | Data Setup          |
| 9 | TCATBALF    | Define Transaction category balance VSAM             | Data Setup          |
|10 | DISCGRP     | Define Disclosure group VSAM                         | Data Setup          |
|11 | DALYREJS    | Define Daily rejection sequential file               | Data Setup          |
|12 | REPTFILE    | Define GDG for report output file                    | Data Setup          |
|13 | DEFGDGB     | Define GDG base for backups                          | Data Setup          |
|14 | DEFGDGD     | Define GDG base for daily data                       | Data Setup          |
|15 | DEFCUST     | Define customer VSAM cluster                         | Data Setup          |
|16 | ESDSRRDS    | Define ESDS/RRDS example clusters                    | Data Setup          |

### 5.2 Batch Processing (Daily Cycle)

| # | Job          | Purpose                                              | Category            |
|---|-------------|------------------------------------------------------|---------------------|
|17 | CLOSEFIL    | Close CICS files before batch processing             | Batch Cycle         |
|18 | OPENFIL     | Open CICS files after batch processing               | Batch Cycle         |
|19 | POSTTRAN    | Core transaction posting (runs CBTRN02C)             | Transaction Posting |
|20 | INTCALC     | Interest calculation (runs CBACT04C)                 | Financial Calc      |
|21 | TRANBKP     | Backup transaction master (REPRO + delete)           | Backup              |
|22 | COMBTRAN    | Combine daily transactions into master               | Transaction Merge   |
|23 | CREASTMT    | Create account statements (runs CBSTM03A)            | Statement Gen       |
|24 | TRANIDX     | Define alternate index on transaction processed TS   | Index Maintenance   |
|25 | TRANREPT    | Generate transaction detail report (runs CBTRN03C)   | Reporting           |

### 5.3 Utility & Read Jobs

| # | Job          | Purpose                                              | Category            |
|---|-------------|------------------------------------------------------|---------------------|
|26 | READACCT    | Read and print account master file                   | Utility / Debug     |
|27 | READCARD    | Read and print card master file                      | Utility / Debug     |
|28 | READCUST    | Read and print customer master file                  | Utility / Debug     |
|29 | READXREF    | Read and print cross-reference file                  | Utility / Debug     |
|30 | PRTCATBL    | Print category balance file                          | Utility / Debug     |
|31 | WAITSTEP    | Wait for specified centiseconds (runs COBSWAIT)      | Utility             |
|32 | CBEXPORT    | Export data for branch migration                     | Data Migration      |
|33 | CBIMPORT    | Import data from branch migration                    | Data Migration      |

### 5.4 Administrative & Infrastructure Jobs

| # | Job          | Purpose                                              | Category            |
|---|-------------|------------------------------------------------------|---------------------|
|34 | CBADMCDJ    | Admin card management batch job                      | Admin               |
|35 | FTPJCL      | FTP JCL for remote file transfer                     | Infrastructure      |
|36 | INTRDRJ1    | Internal reader - trigger another JCL job            | Infrastructure      |
|37 | INTRDRJ2    | Internal reader - create IMS VSAM files              | Infrastructure      |
|38 | TXT2PDF1    | Convert text report to PDF                           | Utility             |

### 5.5 Extension Module JCL

**Authorization module** (`app/app-authorization-ims-db2-mq/jcl/`):

| Job          | Purpose                                                |
|-------------|--------------------------------------------------------|
| CBPAUP0J    | Batch purge of old pending authorizations               |
| DBPAUTP0    | Purge pending authorization DB                          |
| LOADPADB    | Load pending authorization IMS database                 |
| UNLDGSAM    | Unload GSAM data                                        |
| UNLDPADB    | Unload pending authorization database                   |

**Transaction Type DB2 module** (`app/app-transaction-type-db2/jcl/`):

| Job          | Purpose                                                |
|-------------|--------------------------------------------------------|
| CREADB21    | Create DB2 table for transaction types                  |
| MNTTRDB2    | Maintain transaction type DB2 data                      |
| TRANEXTR    | Extract transaction types from DB2                      |

---

## 6. Assembler Programs (`app/asm/`)

| Program      | Purpose                                                   |
|-------------|-----------------------------------------------------------|
| MVSWAIT     | Assembler wait routine (called by COBSWAIT)                |
| COBDATFT    | Assembler date formatting routine (called by CBACT01C)     |

---

## 7. JCL Procedures (`app/proc/`)

| Procedure    | Purpose                                                   |
|-------------|-----------------------------------------------------------|
| REPROC.prc  | Reusable compile/link procedure                            |
| TRANREPT.prc| Procedure for transaction report generation                |

---

## 8. Scheduler Configurations (`app/scheduler/`)

| Config File         | Scheduler   | Purpose                              |
|--------------------|-------------|--------------------------------------|
| CardDemo.ca7       | CA-7        | Job scheduling definitions            |
| CardDemo.controlm  | Control-M   | Job scheduling definitions            |

---

## 9. Functional Domain Classification

| Domain                    | Online Programs                                  | Batch Programs                          |
|---------------------------|--------------------------------------------------|-----------------------------------------|
| **Security / Auth**       | COSGN00C                                         | -                                       |
| **Navigation**            | COMEN01C, COADM01C                               | -                                       |
| **Account Management**    | COACTVWC, COACTUPC                               | CBACT01C, CBACT04C                      |
| **Card Management**       | COCRDLIC, COCRDSLC, COCRDUPC                     | CBACT02C, CBACT03C                      |
| **Transaction Processing**| COTRN00C, COTRN01C, COTRN02C                     | CBTRN01C, CBTRN02C                      |
| **Billing / Payments**    | COBIL00C                                         | -                                       |
| **Reporting**             | CORPT00C                                         | CBTRN03C, CBSTM03A, CBSTM03B           |
| **User Administration**   | COUSR00C, COUSR01C, COUSR02C, COUSR03C           | -                                       |
| **Data Migration**        | -                                                | CBEXPORT, CBIMPORT                      |
| **Customer Processing**   | -                                                | CBCUS01C                                |
| **Authorization (Ext.)**  | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C           | CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| **Reference Data (Ext.)** | COTRTLIC, COTRTUPC                               | COBTUPDT                                |
| **Integration (Ext.)**    | CODATE01, COACCT01                               | -                                       |
| **Utilities**             | -                                                | CSUTLDTC, COBSWAIT                      |
