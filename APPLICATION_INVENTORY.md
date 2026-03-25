# Application Inventory - CardDemo COBOL Codebase

> **Generated**: March 2026  
> **Application**: CardDemo - Mainframe Credit Card Management System  
> **Platform**: COBOL / CICS / VSAM / JCL / BMS (IBM Mainframe)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (31 core + 13 optional)](#cobol-programs)
3. [Copybooks (30 core + 9 optional)](#copybooks)
4. [BMS Screen Maps (17 core + 4 optional)](#bms-screen-maps)
5. [JCL Batch Jobs (38 core + 8 optional)](#jcl-batch-jobs)
6. [Assembler Programs (2)](#assembler-programs)
7. [JCL Procedures (2)](#jcl-procedures)
8. [Optional Extension Modules](#optional-extension-modules)
9. [Classification Legend](#classification-legend)

---

## Executive Summary

| Asset Type             | Core Count | Optional Count | Total |
|------------------------|-----------|----------------|-------|
| COBOL Programs         | 31        | 13             | 44    |
| Copybooks (Data)       | 30        | 9              | 39    |
| BMS Copybooks (Screen) | 17        | 4              | 21    |
| BMS Maps               | 17        | 4              | 21    |
| JCL Jobs               | 38        | 8              | 46    |
| Assembler Programs     | 2         | 0              | 2     |
| JCL Procedures         | 2         | 0              | 2     |
| **Total Assets**       | **137**   | **38**         | **175** |

---

## COBOL Programs

### Online CICS Programs (18 programs)

These programs run inside the CICS transaction processing region and handle real-time user interactions via 3270 terminal screens.

| # | Program ID  | File                | LOC   | Type        | Business Function                                | CICS Trans | Classification |
|---|------------|---------------------|-------|-------------|--------------------------------------------------|-----------|----------------|
| 1 | COSGN00C   | `app/cbl/COSGN00C.cbl` | 260   | CICS Online | Sign-on / authentication screen                  | CC00      | Security       |
| 2 | COMEN01C   | `app/cbl/COMEN01C.cbl` | 308   | CICS Online | Main menu for regular users                      | CM00      | Navigation     |
| 3 | COADM01C   | `app/cbl/COADM01C.cbl` | 288   | CICS Online | Admin menu for admin users                       | CA00      | Navigation     |
| 4 | COACTVWC   | `app/cbl/COACTVWC.cbl` | 941   | CICS Online | View account details                             | CA01      | Account Mgmt   |
| 5 | COACTUPC   | `app/cbl/COACTUPC.cbl` | 4,236 | CICS Online | Update account details                           | CA02      | Account Mgmt   |
| 6 | COCRDLIC   | `app/cbl/COCRDLIC.cbl` | 1,459 | CICS Online | List credit cards (all or filtered)              | CC01      | Card Mgmt      |
| 7 | COCRDSLC   | `app/cbl/COCRDSLC.cbl` | 887   | CICS Online | View credit card details                         | CC02      | Card Mgmt      |
| 8 | COCRDUPC   | `app/cbl/COCRDUPC.cbl` | 1,560 | CICS Online | Update credit card details                       | CC03      | Card Mgmt      |
| 9 | COTRN00C   | `app/cbl/COTRN00C.cbl` | 699   | CICS Online | List transactions from TRANSACT file             | CT00      | Transaction    |
| 10| COTRN01C   | `app/cbl/COTRN01C.cbl` | 330   | CICS Online | View a single transaction                        | CT01      | Transaction    |
| 11| COTRN02C   | `app/cbl/COTRN02C.cbl` | 783   | CICS Online | Add a new transaction                            | CT02      | Transaction    |
| 12| CORPT00C   | `app/cbl/CORPT00C.cbl` | 649   | CICS Online | Submit batch transaction reports                 | CR00      | Reporting      |
| 13| COBIL00C   | `app/cbl/COBIL00C.cbl` | 572   | CICS Online | Bill payment (full/partial balance)              | CB00      | Billing        |
| 14| COUSR00C   | `app/cbl/COUSR00C.cbl` | 695   | CICS Online | List all users from USRSEC file                  | CU00      | User Admin     |
| 15| COUSR01C   | `app/cbl/COUSR01C.cbl` | 299   | CICS Online | Add a new user (Regular/Admin)                   | CU01      | User Admin     |
| 16| COUSR02C   | `app/cbl/COUSR02C.cbl` | 414   | CICS Online | Update an existing user                          | CU02      | User Admin     |
| 17| COUSR03C   | `app/cbl/COUSR03C.cbl` | 359   | CICS Online | Delete a user                                    | CU03      | User Admin     |
| 18| CSUTLDTC   | `app/cbl/CSUTLDTC.cbl` | 157   | Shared Util  | Date validation utility (calls CEEDAYS)          | N/A       | Utility        |

### Batch Programs (13 programs)

These programs run in batch mode (scheduled via JCL) and handle overnight processing, reporting, and data maintenance.

| # | Program ID  | File                    | LOC  | Type   | Business Function                                     | Classification      |
|---|------------|-------------------------|------|--------|-------------------------------------------------------|---------------------|
| 1 | CBACT01C   | `app/cbl/CBACT01C.cbl`  | 430  | Batch  | Read account file, write to output files              | Account Processing  |
| 2 | CBACT02C   | `app/cbl/CBACT02C.cbl`  | 178  | Batch  | Read and print card data file                         | Card Processing     |
| 3 | CBACT03C   | `app/cbl/CBACT03C.cbl`  | 178  | Batch  | Read and print account cross-reference file           | Cross-Ref Processing|
| 4 | CBACT04C   | `app/cbl/CBACT04C.cbl`  | 652  | Batch  | Interest calculation engine                           | Financial Calc      |
| 5 | CBCUS01C   | `app/cbl/CBCUS01C.cbl`  | 178  | Batch  | Read and print customer data file                     | Customer Processing |
| 6 | CBTRN01C   | `app/cbl/CBTRN01C.cbl`  | 494  | Batch  | Post records from daily transaction file              | Transaction Posting |
| 7 | CBTRN02C   | `app/cbl/CBTRN02C.cbl`  | 731  | Batch  | Post daily transactions (core posting engine)         | Transaction Posting |
| 8 | CBTRN03C   | `app/cbl/CBTRN03C.cbl`  | 649  | Batch  | Print transaction detail report                       | Reporting           |
| 9 | CBSTM03A   | `app/cbl/CBSTM03A.CBL`  | 924  | Batch  | Statement generation (main driver)                    | Statement Gen       |
| 10| CBSTM03B   | `app/cbl/CBSTM03B.CBL`  | 230  | Batch  | Statement generation (I/O subroutine)                 | Statement Gen       |
| 11| CBEXPORT   | `app/cbl/CBEXPORT.cbl`  | 582  | Batch  | Export all data (customer, account, card, transaction) | Data Export         |
| 12| CBIMPORT   | `app/cbl/CBIMPORT.cbl`  | 487  | Batch  | Import data from export file into individual files    | Data Import         |
| 13| COBSWAIT   | `app/cbl/COBSWAIT.cbl`  | 41   | Batch  | Wait utility (accepts parm in centiseconds)           | Utility             |

---

## Copybooks

### Data Structure Copybooks (30 files in `app/cpy/`)

| # | Copybook ID | File                  | Business Domain       | Description                                       | Record Size |
|---|------------|-----------------------|-----------------------|---------------------------------------------------|-------------|
| 1 | CVACT01Y   | `app/cpy/CVACT01Y.cpy`| Account               | Account master record layout                      | 300 bytes   |
| 2 | CVACT02Y   | `app/cpy/CVACT02Y.cpy`| Card                  | Card data record layout                           | 150 bytes   |
| 3 | CVACT03Y   | `app/cpy/CVACT03Y.cpy`| Cross-Reference       | Card-to-account cross-reference record            | 50 bytes    |
| 4 | CVCUS01Y   | `app/cpy/CVCUS01Y.cpy`| Customer              | Customer master record layout                     | 500 bytes   |
| 5 | CVCRD01Y   | `app/cpy/CVCRD01Y.cpy`| Card (internal)       | Internal card data structure for CICS programs     | Variable    |
| 6 | CVTRA01Y   | `app/cpy/CVTRA01Y.cpy`| Transaction           | Transaction category balance record               | 50 bytes    |
| 7 | CVTRA02Y   | `app/cpy/CVTRA02Y.cpy`| Transaction           | Disclosure group record (interest rates)          | 50 bytes    |
| 8 | CVTRA03Y   | `app/cpy/CVTRA03Y.cpy`| Transaction           | Transaction type master record                    | 60 bytes    |
| 9 | CVTRA04Y   | `app/cpy/CVTRA04Y.cpy`| Transaction           | Transaction category type record                  | 60 bytes    |
| 10| CVTRA05Y   | `app/cpy/CVTRA05Y.cpy`| Transaction           | Transaction detail record (online)                | 350 bytes   |
| 11| CVTRA06Y   | `app/cpy/CVTRA06Y.cpy`| Transaction           | Daily transaction record (batch input)            | 350 bytes   |
| 12| CVTRA07Y   | `app/cpy/CVTRA07Y.cpy`| Reporting             | Transaction report data structures/headers        | Variable    |
| 13| CVEXPORT   | `app/cpy/CVEXPORT.cpy`| Data Exchange         | Export/import record layout                       | Variable    |
| 14| COSTM01    | `app/cpy/COSTM01.CPY` | Statement             | Transaction layout for statement reporting        | 355 bytes   |
| 15| CUSTREC    | `app/cpy/CUSTREC.cpy` | Customer (Statement)  | Customer record for statement generation          | Variable    |
| 16| COCOM01Y   | `app/cpy/COCOM01Y.cpy`| Communication         | Common communication area (COMMAREA)              | Variable    |
| 17| COMEN02Y   | `app/cpy/COMEN02Y.cpy`| Navigation            | Menu definition data structure                    | Variable    |
| 18| COADM02Y   | `app/cpy/COADM02Y.cpy`| Navigation            | Admin menu definition data structure              | Variable    |
| 19| COTTL01Y   | `app/cpy/COTTL01Y.cpy`| UI                    | Title/header line layout for screens              | Variable    |
| 20| CSUSR01Y   | `app/cpy/CSUSR01Y.cpy`| Security              | User security record layout                       | 80 bytes    |
| 21| CSDAT01Y   | `app/cpy/CSDAT01Y.cpy`| Utility               | Date handling data structures                     | Variable    |
| 22| CSMSG01Y   | `app/cpy/CSMSG01Y.cpy`| UI                    | Message area layout (primary)                     | Variable    |
| 23| CSMSG02Y   | `app/cpy/CSMSG02Y.cpy`| UI                    | Message area layout (secondary)                   | Variable    |
| 24| CODATECN   | `app/cpy/CODATECN.cpy`| Utility               | Date conversion record for assembler call         | Variable    |
| 25| CSUTLDPY   | `app/cpy/CSUTLDPY.cpy`| Utility               | Date utility parameter area                       | Variable    |
| 26| CSUTLDWY   | `app/cpy/CSUTLDWY.cpy`| Utility               | Date utility working storage                      | Variable    |
| 27| CSLKPCDY   | `app/cpy/CSLKPCDY.cpy`| Validation            | Lookup code validation data                       | Variable    |
| 28| CSSETATY   | `app/cpy/CSSETATY.cpy`| UI                    | Screen attribute setting (REPLACING pattern)      | Variable    |
| 29| CSSTRPFY   | `app/cpy/CSSTRPFY.cpy`| Utility               | String stripping/padding function                 | Variable    |
| 30| UNUSED1Y   | `app/cpy/UNUSED1Y.cpy`| Deprecated            | Unused data record (legacy placeholder)           | 80 bytes    |

### BMS-Generated Copybooks (17 files in `app/cpy-bms/`)

| # | Copybook ID | File                        | Maps To Screen | Description                         |
|---|------------|-----------------------------|----------------|-------------------------------------|
| 1 | COACTUP    | `app/cpy-bms/COACTUP.CPY`  | Account Update | Screen field definitions             |
| 2 | COACTVW    | `app/cpy-bms/COACTVW.CPY`  | Account View   | Screen field definitions             |
| 3 | COADM01    | `app/cpy-bms/COADM01.CPY`  | Admin Menu     | Screen field definitions             |
| 4 | COBIL00    | `app/cpy-bms/COBIL00.CPY`  | Bill Payment   | Screen field definitions             |
| 5 | COCRDLI    | `app/cpy-bms/COCRDLI.CPY`  | Card List      | Screen field definitions             |
| 6 | COCRDSL    | `app/cpy-bms/COCRDSL.CPY`  | Card Detail    | Screen field definitions             |
| 7 | COCRDUP    | `app/cpy-bms/COCRDUP.CPY`  | Card Update    | Screen field definitions             |
| 8 | COMEN01    | `app/cpy-bms/COMEN01.CPY`  | Main Menu      | Screen field definitions             |
| 9 | CORPT00    | `app/cpy-bms/CORPT00.CPY`  | Reports        | Screen field definitions             |
| 10| COSGN00    | `app/cpy-bms/COSGN00.CPY`  | Sign-on        | Screen field definitions             |
| 11| COTRN00    | `app/cpy-bms/COTRN00.CPY`  | Tran List      | Screen field definitions             |
| 12| COTRN01    | `app/cpy-bms/COTRN01.CPY`  | Tran View      | Screen field definitions             |
| 13| COTRN02    | `app/cpy-bms/COTRN02.CPY`  | Tran Add       | Screen field definitions             |
| 14| COUSR00    | `app/cpy-bms/COUSR00.CPY`  | User List      | Screen field definitions             |
| 15| COUSR01    | `app/cpy-bms/COUSR01.CPY`  | User Add       | Screen field definitions             |
| 16| COUSR02    | `app/cpy-bms/COUSR02.CPY`  | User Update    | Screen field definitions             |
| 17| COUSR03    | `app/cpy-bms/COUSR03.CPY`  | User Delete    | Screen field definitions             |

---

## BMS Screen Maps

### Core BMS Maps (17 files in `app/bms/`)

| # | Map ID   | File                   | Screen Title          | Used By Program | User Role | Classification |
|---|----------|------------------------|-----------------------|----------------|-----------|----------------|
| 1 | COSGN00  | `app/bms/COSGN00.bms` | Sign-on Screen        | COSGN00C       | All       | Security       |
| 2 | COMEN01  | `app/bms/COMEN01.bms` | Main Menu             | COMEN01C       | Regular   | Navigation     |
| 3 | COADM01  | `app/bms/COADM01.bms` | Admin Menu            | COADM01C       | Admin     | Navigation     |
| 4 | COACTVW  | `app/bms/COACTVW.bms` | Account View          | COACTVWC       | Regular   | Account Mgmt   |
| 5 | COACTUP  | `app/bms/COACTUP.bms` | Account Update        | COACTUPC       | Regular   | Account Mgmt   |
| 6 | COCRDLI  | `app/bms/COCRDLI.bms` | Credit Card List      | COCRDLIC       | All       | Card Mgmt      |
| 7 | COCRDSL  | `app/bms/COCRDSL.bms` | Credit Card Detail    | COCRDSLC       | Regular   | Card Mgmt      |
| 8 | COCRDUP  | `app/bms/COCRDUP.bms` | Credit Card Update    | COCRDUPC       | Regular   | Card Mgmt      |
| 9 | COTRN00  | `app/bms/COTRN00.bms` | Transaction List      | COTRN00C       | Regular   | Transaction    |
| 10| COTRN01  | `app/bms/COTRN01.bms` | Transaction View      | COTRN01C       | Regular   | Transaction    |
| 11| COTRN02  | `app/bms/COTRN02.bms` | Transaction Add       | COTRN02C       | Regular   | Transaction    |
| 12| CORPT00  | `app/bms/CORPT00.bms` | Transaction Reports   | CORPT00C       | Regular   | Reporting      |
| 13| COBIL00  | `app/bms/COBIL00.bms` | Bill Payment          | COBIL00C       | Regular   | Billing        |
| 14| COUSR00  | `app/bms/COUSR00.bms` | User List             | COUSR00C       | Admin     | User Admin     |
| 15| COUSR01  | `app/bms/COUSR01.bms` | Add User              | COUSR01C       | Admin     | User Admin     |
| 16| COUSR02  | `app/bms/COUSR02.bms` | Update User           | COUSR02C       | Admin     | User Admin     |
| 17| COUSR03  | `app/bms/COUSR03.bms` | Delete User           | COUSR03C       | Admin     | User Admin     |

---

## JCL Batch Jobs

### Data Refresh Jobs (8 jobs)

| # | Job Name  | File                    | Purpose                                       | Key Program/Utility | Classification       |
|---|----------|-------------------------|-----------------------------------------------|---------------------|----------------------|
| 1 | ACCTFILE | `app/jcl/ACCTFILE.jcl`  | Refresh account master VSAM from PS           | IDCAMS REPRO        | Data Refresh         |
| 2 | CARDFILE | `app/jcl/CARDFILE.jcl`  | Refresh card master VSAM from PS              | IDCAMS REPRO        | Data Refresh         |
| 3 | CUSTFILE | `app/jcl/CUSTFILE.jcl`  | Refresh customer master VSAM from PS          | IDCAMS REPRO        | Data Refresh         |
| 4 | XREFFILE | `app/jcl/XREFFILE.jcl`  | Load card cross-reference VSAM + alt index    | IDCAMS REPRO/DEFINE | Data Refresh         |
| 5 | TRANFILE | `app/jcl/TRANFILE.jcl`  | Load transaction master VSAM + alt index      | IDCAMS REPRO/DEFINE | Data Refresh         |
| 6 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl`  | Load user security VSAM file                  | IDCAMS REPRO        | Data Refresh         |
| 7 | DEFCUST  | `app/jcl/DEFCUST.jcl`   | Define customer VSAM cluster                  | IDCAMS DEFINE       | Data Refresh         |
| 8 | REPTFILE | `app/jcl/REPTFILE.jcl`  | Refresh reporting files                       | IDCAMS REPRO        | Data Refresh         |

### Core Batch Processing Jobs (9 jobs)

| # | Job Name   | File                     | Purpose                                      | Key Program/Utility  | Classification       |
|---|-----------|--------------------------|----------------------------------------------|----------------------|----------------------|
| 1 | CLOSEFIL  | `app/jcl/CLOSEFIL.jcl`  | Close CICS files before batch processing     | IDCAMS (CICS)        | Batch Lifecycle      |
| 2 | OPENFIL   | `app/jcl/OPENFIL.jcl`   | Open CICS files after batch processing       | IDCAMS (CICS)        | Batch Lifecycle      |
| 3 | POSTTRAN  | `app/jcl/POSTTRAN.jcl`  | Core transaction posting (daily cycle)       | CBTRN02C             | Transaction Posting  |
| 4 | INTCALC   | `app/jcl/INTCALC.jcl`   | Interest calculation on accounts             | CBACT04C             | Financial Processing |
| 5 | TRANBKP   | `app/jcl/TRANBKP.jcl`   | Backup transaction file (GDG)                | REPRO + IDCAMS       | Backup               |
| 6 | COMBTRAN  | `app/jcl/COMBTRAN.jcl`  | Combine daily transactions into master       | SORT + IDCAMS        | Data Consolidation   |
| 7 | CREASTMT  | `app/jcl/CREASTMT.JCL`  | Generate customer statements (text + HTML)   | CBSTM03A             | Statement Gen        |
| 8 | TRANIDX   | `app/jcl/TRANIDX.jcl`   | Define/build transaction alternate indexes   | IDCAMS DEFINE        | Index Maintenance    |
| 9 | TRANREPT  | `app/jcl/TRANREPT.jcl`  | Produce daily transaction report             | SORT + CBTRN03C      | Reporting            |

### Reference Data Jobs (6 jobs)

| # | Job Name   | File                     | Purpose                                      | Key Program/Utility  | Classification       |
|---|-----------|--------------------------|----------------------------------------------|----------------------|----------------------|
| 1 | TRANTYPE  | `app/jcl/TRANTYPE.jcl`  | Load transaction type VSAM from PS           | IDCAMS REPRO         | Reference Data       |
| 2 | TRANCATG  | `app/jcl/TRANCATG.jcl`  | Load transaction category VSAM from PS       | IDCAMS REPRO         | Reference Data       |
| 3 | TCATBALF  | `app/jcl/TCATBALF.jcl`  | Load transaction category balance VSAM       | IDCAMS REPRO         | Reference Data       |
| 4 | DISCGRP   | `app/jcl/DISCGRP.jcl`   | Load disclosure group (interest rates) VSAM  | IDCAMS REPRO         | Reference Data       |
| 5 | DALYREJS  | `app/jcl/DALYREJS.jcl`  | Define daily rejects VSAM cluster            | IDCAMS DEFINE        | Reference Data       |
| 6 | DEFGDGB   | `app/jcl/DEFGDGB.jcl`   | Define GDG base for backups                  | IDCAMS DEFINE        | Infrastructure       |

### Utility / Read / Print Jobs (10 jobs)

| # | Job Name   | File                      | Purpose                                     | Key Program/Utility  | Classification       |
|---|-----------|---------------------------|---------------------------------------------|----------------------|----------------------|
| 1 | READACCT  | `app/jcl/READACCT.jcl`   | Read and print account data (diagnostic)    | CBACT01C             | Diagnostic           |
| 2 | READCARD  | `app/jcl/READCARD.jcl`   | Read and print card data (diagnostic)       | CBACT02C             | Diagnostic           |
| 3 | READCUST  | `app/jcl/READCUST.jcl`   | Read and print customer data (diagnostic)   | CBCUS01C             | Diagnostic           |
| 4 | READXREF  | `app/jcl/READXREF.jcl`   | Read and print cross-ref data (diagnostic)  | CBACT03C             | Diagnostic           |
| 5 | PRTCATBL  | `app/jcl/PRTCATBL.jcl`   | Print category balance data                 | IDCAMS PRINT         | Diagnostic           |
| 6 | ESDSRRDS  | `app/jcl/ESDSRRDS.jcl`   | ESDS/RRDS VSAM utilities                    | IDCAMS               | Utility              |
| 7 | WAITSTEP  | `app/jcl/WAITSTEP.jcl`   | Wait step (calls COBSWAIT)                  | COBSWAIT             | Utility              |
| 8 | CBEXPORT  | `app/jcl/CBEXPORT.jcl`   | Export all CardDemo data to single file     | CBEXPORT             | Data Exchange        |
| 9 | CBIMPORT  | `app/jcl/CBIMPORT.jcl`   | Import data from export file                | CBIMPORT             | Data Exchange        |
| 10| CBADMCDJ  | `app/jcl/CBADMCDJ.jcl`  | Admin card data job                         | IDCAMS               | Admin                |

### Infrastructure / Miscellaneous Jobs (5 jobs)

| # | Job Name   | File                      | Purpose                                     | Key Program/Utility  | Classification       |
|---|-----------|---------------------------|---------------------------------------------|----------------------|----------------------|
| 1 | DEFGDGD   | `app/jcl/DEFGDGD.jcl`   | Define GDG base for daily data              | IDCAMS DEFINE        | Infrastructure       |
| 2 | FTPJCL    | `app/jcl/FTPJCL.JCL`    | FTP file transfer job                       | FTP                  | Infrastructure       |
| 3 | INTRDRJ1  | `app/jcl/INTRDRJ1.JCL`  | Internal reader job (chain submission)      | IDCAMS + IEBGENER    | Infrastructure       |
| 4 | INTRDRJ2  | `app/jcl/INTRDRJ2.JCL`  | Internal reader job (chained target)        | IDCAMS               | Infrastructure       |
| 5 | TXT2PDF1  | `app/jcl/TXT2PDF1.JCL`  | Convert text statements to PDF              | TXT2PDF REXX         | Utility              |

---

## Assembler Programs

| # | Program ID | File                   | Purpose                                    | Called By      |
|---|-----------|------------------------|--------------------------------------------|----------------|
| 1 | COBDATFT  | `app/asm/COBDATFT.asm` | Date formatting for COBOL programs         | CBACT01C       |
| 2 | MVSWAIT   | `app/asm/MVSWAIT.asm`  | MVS WAIT SVC (centisecond timer)           | COBSWAIT       |

---

## JCL Procedures

| # | Proc Name  | File                   | Purpose                                    | Used By Jobs         |
|---|-----------|------------------------|--------------------------------------------|----------------------|
| 1 | REPROC    | `app/proc/REPROC.prc`  | Standard REPRO copy procedure              | TRANBKP, TRANREPT   |
| 2 | TRANREPT  | `app/proc/TRANREPT.prc`| Transaction report procedure               | TRANREPT             |

---

## Optional Extension Modules

### Authorization Module (IMS/DB2/MQ) - `app/app-authorization-ims-db2-mq/`

| # | Program ID | Type        | Purpose                                         |
|---|-----------|-------------|--------------------------------------------------|
| 1 | COPAUA0C  | CICS Online | MQ trigger monitor for authorization requests    |
| 2 | COPAUS0C  | CICS Online | Payment authorization summary screen             |
| 3 | COPAUS1C  | CICS Online | Payment authorization detail screen              |
| 4 | COPAUS2C  | CICS Online | Fraud marking (writes to DB2)                    |
| 5 | CBPAUP0C  | Batch       | Batch purge of old authorization records         |
| 6 | DBUNLDGS  | Batch       | Unload GSAM database                             |
| 7 | PAUDBLOD  | Batch       | Load payment authorization DB                    |
| 8 | PAUDBUNL  | Batch       | Unload payment authorization DB                  |

**Additional Assets**: 9 copybooks, 2 BMS maps (COPAU00, COPAU01), 5 JCL jobs, IMS DBD/PSB definitions, DB2 DDL

### Transaction Type DB2 Module - `app/app-transaction-type-db2/`

| # | Program ID | Type        | Purpose                                         |
|---|-----------|-------------|--------------------------------------------------|
| 1 | COTRTUPC  | CICS Online | Add/edit transaction types (DB2 CRUD)            |
| 2 | COTRTLIC  | CICS Online | List/delete transaction types (DB2 cursors)      |
| 3 | COBTUPDT  | Batch       | Batch update of transaction types                |

**Additional Assets**: 2 copybooks (CSDB2RPY, CSDB2RWY), 2 BMS maps (COTRTLI, COTRTUP), 3 JCL jobs, DB2 DDL

### VSAM-MQ Module - `app/app-vsam-mq/`

| # | Program ID | Type        | Purpose                                         |
|---|-----------|-------------|--------------------------------------------------|
| 1 | CODATE01  | CICS Online | MQ request/response for system date (CDRD)       |
| 2 | COACCT01  | CICS Online | MQ request/response for account inquiry (CDRA)   |

---

## Classification Legend

| Classification       | Description                                                    |
|---------------------|----------------------------------------------------------------|
| **Security**        | Authentication, authorization, user credential management       |
| **Navigation**      | Menu screens and program routing                               |
| **Account Mgmt**    | Account view, update, and maintenance operations               |
| **Card Mgmt**       | Credit card CRUD operations                                    |
| **Transaction**     | Transaction listing, viewing, and creation                     |
| **Reporting**       | Report generation and submission                               |
| **Billing**         | Bill payment processing                                        |
| **User Admin**      | Administrative user CRUD operations                            |
| **Financial Calc**  | Interest calculation and financial processing                   |
| **Statement Gen**   | Customer statement generation (text + HTML)                    |
| **Transaction Posting** | Daily batch transaction posting and validation             |
| **Data Refresh**    | VSAM file initialization and data loading                      |
| **Data Exchange**   | Cross-system data export/import                                |
| **Reference Data**  | Lookup/reference table maintenance                             |
| **Utility**         | Infrastructure utilities (wait, date, FTP)                     |
| **Diagnostic**      | Read/print jobs for data verification                          |
| **Infrastructure**  | GDG definitions, internal reader, file management              |

---

## Naming Conventions

| Prefix | Meaning                                    | Example    |
|--------|--------------------------------------------|------------|
| `CO*`  | CICS Online program                        | COSGN00C   |
| `CB*`  | Batch COBOL program                        | CBTRN02C   |
| `CS*`  | Shared/common service module               | CSUTLDTC   |
| `CV*`  | Copybook - VSAM/data record layout         | CVACT01Y   |
| `*Y`   | Copybook suffix (data structure)           | CSUSR01Y   |
| `*C`   | Program suffix (COBOL source)              | COSGN00C   |
