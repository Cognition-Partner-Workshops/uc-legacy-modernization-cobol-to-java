# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: z/OS (CICS/VSAM/JCL)

---

## Executive Summary

CardDemo is a mainframe credit card management application built on COBOL/CICS/VSAM/JCL. It simulates account management, card management, transactions, bill payments, and reporting for a fictional credit card company. The application supports two user roles: **Regular** (card operations) and **Admin** (user/transaction type management).

| Category               | Count | Location                              |
|------------------------|-------|---------------------------------------|
| COBOL Programs (Core)  | 32    | `app/cbl/`                            |
| COBOL Programs (Ext.)  | 13    | `app/app-*/cbl/`                      |
| Copybooks (Core)       | 31    | `app/cpy/`                            |
| BMS Maps               | 17    | `app/bms/`                            |
| BMS Copybooks          | 17    | `app/cpy-bms/`                        |
| JCL Jobs               | 38    | `app/jcl/`                            |
| Assembler Programs     | 2     | `app/asm/`                            |
| JCL Procedures         | 2     | `app/proc/`                           |
| **Total Artifacts**    | **152** |                                     |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs

These programs run under CICS and handle interactive 3270 terminal sessions.

| # | Program    | Lines | Function                        | CICS Trans | BMS Map   | Classification     |
|---|------------|-------|---------------------------------|------------|-----------|--------------------|
| 1 | COSGN00C   | 260   | User Sign-on / Authentication   | CC00       | COSGN00   | Security           |
| 2 | COMEN01C   | 308   | Main Menu Navigation            | CM00       | COMEN01   | Navigation         |
| 3 | COADM01C   | 288   | Admin Menu Navigation           | CA00       | COADM01   | Navigation / Admin |
| 4 | COACTVWC   | 941   | Account View (read-only)        | CA01       | COACTVW   | Account Mgmt       |
| 5 | COACTUPC   | 4,236 | Account Update                  | CA02       | COACTUP   | Account Mgmt       |
| 6 | COCRDLIC   | 1,459 | Credit Card List                | CC01       | COCRDLI   | Card Mgmt          |
| 7 | COCRDSLC   | 887   | Credit Card View (single card)  | CC02       | COCRDSL   | Card Mgmt          |
| 8 | COCRDUPC   | 1,560 | Credit Card Update              | CC03       | COCRDUP   | Card Mgmt          |
| 9 | COTRN00C   | 699   | Transaction List                | CT00       | COTRN00   | Transaction Mgmt   |
|10 | COTRN01C   | 330   | Transaction View (single)       | CT01       | COTRN01   | Transaction Mgmt   |
|11 | COTRN02C   | 783   | Transaction Add (new entry)     | CT02       | COTRN02   | Transaction Mgmt   |
|12 | CORPT00C   | 649   | Transaction Reports (online)    | CR00       | CORPT00   | Reporting          |
|13 | COBIL00C   | 572   | Bill Payment                    | CB00       | COBIL00   | Payments           |
|14 | COUSR00C   | 695   | User List (Admin security)      | CU00       | COUSR00   | Security / Admin   |
|15 | COUSR01C   | 299   | User Add (Admin security)       | CU01       | COUSR01   | Security / Admin   |
|16 | COUSR02C   | 414   | User Update (Admin security)    | CU02       | COUSR02   | Security / Admin   |
|17 | COUSR03C   | 359   | User Delete (Admin security)    | CU03       | COUSR03   | Security / Admin   |

### 1.2 Batch COBOL Programs

These programs run in batch mode via JCL job submission.

| # | Program    | Lines | Function                                    | Classification          |
|---|------------|-------|---------------------------------------------|-------------------------|
|18 | CBTRN01C   | 494   | Daily transaction posting (validation pass) | Transaction Processing  |
|19 | CBTRN02C   | 731   | Daily transaction posting (main posting)    | Transaction Processing  |
|20 | CBTRN03C   | 649   | Transaction detail report generation        | Reporting               |
|21 | CBACT01C   | 430   | Read account file, write to multiple outputs| Data Processing         |
|22 | CBACT02C   | 178   | Read and print card data file               | Data Processing         |
|23 | CBACT03C   | 178   | Read and print cross-reference data file    | Data Processing         |
|24 | CBACT04C   | 652   | Interest calculation engine                 | Financial Calculation   |
|25 | CBCUS01C   | 178   | Read and print customer data file           | Data Processing         |
|26 | CBSTM03A   | 924   | Statement generation (text + HTML)          | Statement Generation    |
|27 | CBSTM03B   | 230   | Statement file processing subroutine        | Statement Generation    |
|28 | CBEXPORT   | 582   | Customer data export for branch migration   | Data Migration          |
|29 | CBIMPORT   | 487   | Customer data import from branch migration  | Data Migration          |

### 1.3 Utility Programs

| # | Program    | Lines | Function                          | Classification  |
|---|------------|-------|-----------------------------------|-----------------|
|30 | CSUTLDTC   | 157   | Date conversion utility (CEEDAYS) | Utility         |
|31 | COBSWAIT   | 41    | Wait/delay utility                | Utility         |

### 1.4 Assembler Programs (`app/asm/`)

| # | Program    | Function                              | Classification  |
|---|------------|---------------------------------------|-----------------|
|32 | COBDATFT   | Date formatting (called by CBACT01C)  | Utility         |
|33 | MVSWAIT    | MVS wait (system-level delay)         | Utility         |

---

## 2. COBOL Programs -- Optional Extension Modules

### 2.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program    | Function                                        | Technology     |
|---|------------|-------------------------------------------------|----------------|
|34 | COPAUA0C   | MQ trigger: receive authorization requests      | CICS + MQ      |
|35 | COPAUS0C   | Authorization summary view                      | CICS + IMS DB  |
|36 | COPAUS1C   | Authorization detail view                       | CICS + IMS DB  |
|37 | COPAUS2C   | Mark authorization as fraud (write to DB2)      | CICS + DB2     |
|38 | CBPAUP0C   | Batch purge of authorization messages           | Batch + IMS DB |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program    | Function                                        | Technology     |
|---|------------|-------------------------------------------------|----------------|
|39 | COTRTLIC   | Transaction type list with DB2 cursor paging    | CICS + DB2     |
|40 | COTRTUPC   | Transaction type add/edit (CRUD)                | CICS + DB2     |
|41 | COBTUPDT   | Batch update of transaction types               | Batch + DB2    |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program    | Function                                        | Technology     |
|---|------------|-------------------------------------------------|----------------|
|42 | COACCT01   | MQ request/response for account inquiry (CDRA)  | CICS + MQ      |
|43 | CODATE01   | MQ request/response for system date (CDRD)      | CICS + MQ      |

---

## 3. Copybooks (`app/cpy/`)

### 3.1 Data Structure Copybooks (VSAM Record Layouts)

| # | Copybook   | Record Length | Business Entity              | Classification       |
|---|------------|--------------|------------------------------|----------------------|
| 1 | CVACT01Y   | 300 bytes    | Account Master Record        | Account Data         |
| 2 | CVACT02Y   | 150 bytes    | Card Data Record             | Card Data            |
| 3 | CVACT03Y   | 50 bytes     | Card Cross-Reference Record  | Cross-Reference      |
| 4 | CVCUS01Y   | 500 bytes    | Customer Data Record         | Customer Data        |
| 5 | CVTRA01Y   | 50 bytes     | Transaction Category Balance | Transaction Data     |
| 6 | CVTRA02Y   | 50 bytes     | Disclosure Group Record      | Transaction Data     |
| 7 | CVTRA03Y   | 60 bytes     | Transaction Type Record      | Reference Data       |
| 8 | CVTRA04Y   | 60 bytes     | Transaction Category Type    | Reference Data       |
| 9 | CVTRA05Y   | 350 bytes    | Transaction Record (master)  | Transaction Data     |
|10 | CVTRA06Y   | 350 bytes    | Daily Transaction Record     | Transaction Data     |
|11 | CVTRA07Y   | N/A          | Transaction Report Layout    | Reporting            |
|12 | CVCRD01Y   | N/A          | Card Detail (extended)       | Card Data            |
|13 | COSTM01    | N/A          | Statement Transaction Layout | Statement / Report   |
|14 | CUSTREC    | N/A          | Customer Record (alt layout) | Customer Data        |
|15 | CVEXPORT   | N/A          | Export Multi-Record Layout   | Data Migration       |
|16 | UNUSED1Y   | 80 bytes     | Unused / deprecated data     | Deprecated           |

### 3.2 Application Control Copybooks

| # | Copybook   | Function                                  | Classification       |
|---|------------|-------------------------------------------|----------------------|
|17 | COCOM01Y   | Common communication area (inter-program) | Framework            |
|18 | COMEN02Y   | Main menu option definitions (11 options) | Navigation           |
|19 | COADM02Y   | Admin menu option definitions (6 options) | Navigation / Admin   |
|20 | COTTL01Y   | Screen title/header definitions           | UI Framework         |
|21 | CSMSG01Y   | Standard message area                     | UI Framework         |
|22 | CSMSG02Y   | Extended message area                     | UI Framework         |
|23 | CSDAT01Y   | Date display fields                       | UI Framework         |
|24 | CSUSR01Y   | User security record layout               | Security             |
|25 | CODATECN   | Date conversion data structure            | Utility              |

### 3.3 Utility Copybooks

| # | Copybook   | Function                                  | Classification       |
|---|------------|-------------------------------------------|----------------------|
|26 | CSUTLDPY   | Date utility procedures                   | Utility              |
|27 | CSUTLDWY   | Date utility working storage              | Utility              |
|28 | CSLKPCDY   | Lookup code tables (large, 51KB)          | Reference Data       |
|29 | CSSETATY   | Screen attribute setting macros           | UI Framework         |
|30 | CSSTRPFY   | String/field processing utilities         | Utility              |

---

## 4. BMS Maps (`app/bms/`) and BMS Copybooks (`app/cpy-bms/`)

Each BMS map defines a 3270 terminal screen. The BMS-generated copybooks provide the COBOL data structures.

| # | BMS Map    | BMS Copybook | Screen Name             | Used By    | Fields (approx.) |
|---|------------|--------------|-------------------------|------------|------------------|
| 1 | COSGN00    | COSGN00.CPY  | Sign-on Screen          | COSGN00C   | User ID, Password |
| 2 | COMEN01    | COMEN01.CPY  | Main Menu               | COMEN01C   | 11 menu options  |
| 3 | COADM01    | COADM01.CPY  | Admin Menu              | COADM01C   | 6 admin options  |
| 4 | COACTVW    | COACTVW.CPY  | Account View            | COACTVWC   | Acct details, balances |
| 5 | COACTUP    | COACTUP.CPY  | Account Update          | COACTUPC   | Editable acct fields |
| 6 | COCRDLI    | COCRDLI.CPY  | Credit Card List        | COCRDLIC   | Card list grid   |
| 7 | COCRDSL    | COCRDSL.CPY  | Credit Card View        | COCRDSLC   | Card details     |
| 8 | COCRDUP    | COCRDUP.CPY  | Credit Card Update      | COCRDUPC   | Editable card fields |
| 9 | COTRN00    | COTRN00.CPY  | Transaction List        | COTRN00C   | Transaction grid |
|10 | COTRN01    | COTRN01.CPY  | Transaction View        | COTRN01C   | Transaction details |
|11 | COTRN02    | COTRN02.CPY  | Transaction Add         | COTRN02C   | New transaction form |
|12 | CORPT00    | CORPT00.CPY  | Transaction Reports     | CORPT00C   | Report parameters |
|13 | COBIL00    | COBIL00.CPY  | Bill Payment            | COBIL00C   | Payment form     |
|14 | COUSR00    | COUSR00.CPY  | User List               | COUSR00C   | User list grid   |
|15 | COUSR01    | COUSR01.CPY  | User Add                | COUSR01C   | New user form    |
|16 | COUSR02    | COUSR02.CPY  | User Update             | COUSR02C   | Edit user form   |
|17 | COUSR03    | COUSR03.CPY  | User Delete             | COUSR03C   | Delete confirm   |

---

## 5. JCL Jobs (`app/jcl/`)

### 5.1 Data Initialization / Refresh Jobs

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
| 1 | ACCTFILE   | Define + load Account master VSAM KSDS           | IDCAMS                     |
| 2 | CARDFILE   | Define + load Card data VSAM KSDS + AIX          | IDCAMS, SDSF               |
| 3 | CUSTFILE   | Define + load Customer data VSAM KSDS            | IDCAMS, SDSF               |
| 4 | XREFFILE   | Define + load Card cross-reference VSAM KSDS+AIX | IDCAMS                     |
| 5 | TRANFILE   | Define + load Transaction master VSAM KSDS+AIX   | IDCAMS, SDSF               |
| 6 | TCATBALF   | Define + load Transaction category balance VSAM  | IDCAMS                     |
| 7 | TRANTYPE   | Define + load Transaction type reference VSAM    | IDCAMS                     |
| 8 | TRANCATG   | Define + load Transaction category reference VSAM| IDCAMS                     |
| 9 | DISCGRP    | Define + load Disclosure group VSAM KSDS         | IDCAMS                     |
|10 | DUSRSECJ   | Define + load User security VSAM KSDS            | IEFBR14, IEBGENER, IDCAMS  |
|11 | DEFCUST    | Define customer VSAM cluster                     | IDCAMS                     |
|12 | ESDSRRDS   | Define ESDS and RRDS VSAM clusters               | IEFBR14, IEBGENER, IDCAMS  |

### 5.2 Batch Processing Jobs (Daily Cycle)

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
|13 | CLOSEFIL   | Close CICS files for batch processing             | SDSF                       |
|14 | POSTTRAN   | Post daily transactions to master file            | CBTRN02C                   |
|15 | INTCALC    | Calculate interest on accounts                    | CBACT04C                   |
|16 | TRANBKP    | Backup transaction data                           | IDCAMS                     |
|17 | COMBTRAN   | Combine/merge daily transactions                  | SORT, IDCAMS               |
|18 | CREASTMT   | Generate account statements (text + HTML)         | SORT, IDCAMS, CBSTM03A    |
|19 | TRANIDX    | Define/rebuild transaction alternate indexes      | IDCAMS                     |
|20 | OPENFIL    | Re-open CICS files after batch                    | SDSF                       |

### 5.3 Reporting Jobs

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
|21 | TRANREPT   | Generate daily transaction detail report          | SORT, CBTRN03C             |
|22 | PRTCATBL   | Print transaction category balance report         | IEFBR14, SORT              |
|23 | REPTFILE   | Define report output VSAM file                    | IDCAMS                     |
|24 | TXT2PDF1   | Convert text statement to PDF                     | IKJEFT1B (TXT2PDF REXX)   |
|25 | DALYREJS   | Define daily rejects file                         | IDCAMS                     |

### 5.4 Data Read / Validation Jobs

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
|26 | READACCT   | Read and print account data                       | CBACT01C                   |
|27 | READCARD   | Read and print card data                          | CBACT02C                   |
|28 | READCUST   | Read and print customer data                      | CBCUS01C                   |
|29 | READXREF   | Read and print cross-reference data               | CBACT03C                   |

### 5.5 Data Migration Jobs

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
|30 | CBEXPORT   | Export customer data for branch migration          | IDCAMS, CBEXPORT           |
|31 | CBIMPORT   | Import customer data from branch migration         | CBIMPORT                   |

### 5.6 Infrastructure / Utility Jobs

| # | JCL Job    | Function                                         | Programs Executed          |
|---|------------|--------------------------------------------------|----------------------------|
|32 | DEFGDGB    | Define GDG (Generation Data Group) bases          | IDCAMS                     |
|33 | DEFGDGD    | Define GDG data entries + backup reference data   | IDCAMS, IEBGENER           |
|34 | CBADMCDJ   | CSD batch admin (CICS resource definitions)       | DFHCSDUP                   |
|35 | WAITSTEP   | Wait step utility (delay between jobs)            | COBSWAIT                   |
|36 | FTPJCL     | FTP file transfer job                             | FTP                        |
|37 | INTRDRJ1   | Internal reader job 1 (trigger chain)             | IDCAMS, IEBGENER           |
|38 | INTRDRJ2   | Internal reader job 2 (triggered by INTRDRJ1)     | IDCAMS                     |

---

## 6. Supporting Artifacts

### 6.1 JCL Procedures (`app/proc/`)

| Procedure  | Function                                   |
|------------|--------------------------------------------|
| REPROC     | Reusable REPRO (IDCAMS copy) procedure     |
| TRANREPT   | Transaction report procedure               |

### 6.2 Control Files (`app/ctl/`)

| File       | Function                                   |
|------------|--------------------------------------------|
| REPROCT    | REPRO control card definitions             |

### 6.3 Scheduler Configurations (`app/scheduler/`)

| File               | Function                              |
|--------------------|---------------------------------------|
| CardDemo.ca7       | CA-7 job scheduling definitions       |
| CardDemo.controlm  | Control-M job scheduling definitions  |

### 6.4 Assembler Macros (`app/maclib/`)

| Macro      | Function                                   |
|------------|--------------------------------------------|
| ASMWAIT    | Wait macro for assembler programs          |
| COCDATFT   | Date formatting macro                      |

### 6.5 Sample Data Files (`app/data/ASCII/`)

| File           | Business Entity                   |
|----------------|-----------------------------------|
| acctdata.txt   | Account master records            |
| carddata.txt   | Card data records                 |
| cardxref.txt   | Card cross-reference records      |
| custdata.txt   | Customer data records             |
| dailytran.txt  | Daily transaction records         |
| discgrp.txt    | Disclosure group records          |
| tcatbal.txt    | Transaction category balances     |
| trancatg.txt   | Transaction category types        |
| trantype.txt   | Transaction types                 |

---

## 7. Naming Conventions

| Prefix | Meaning                                    |
|--------|--------------------------------------------|
| CO*    | Online CICS programs                       |
| CB*    | Batch COBOL programs                       |
| CS*    | Common/shared utility copybooks            |
| CV*    | VSAM record layout copybooks               |
| CO*Y   | Copybooks used by online programs          |
| *C     | COBOL program suffix                       |
| *Y     | Copybook suffix (data structure)           |

---

## 8. Technology Stack

| Layer          | Technology                                |
|----------------|-------------------------------------------|
| Language       | COBOL (Enterprise COBOL for z/OS)         |
| Online TP      | CICS Transaction Server                   |
| Batch          | JCL / JES2                                |
| Data Storage   | VSAM (KSDS, ESDS, RRDS)                  |
| Database (Ext) | DB2 (optional modules)                    |
| Messaging (Ext)| IBM MQ (optional modules)                 |
| Hierarchical DB| IMS DB (optional modules)                 |
| Screens        | BMS (Basic Mapping Support) - 3270        |
| Utilities      | IDCAMS, SORT, IEBGENER, DFSORT           |
| Scheduling     | CA-7, Control-M                           |
