# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Platform:** IBM Mainframe (COBOL / CICS / VSAM / JCL / BMS)

---

## Table of Contents

1. [Summary](#summary)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [COBOL Programs (Optional Modules)](#cobol-programs-optional-modules)
4. [Copybooks (Core)](#copybooks-core)
5. [Copybooks (Optional Modules)](#copybooks-optional-modules)
6. [BMS Screen Maps](#bms-screen-maps)
7. [BMS-Generated Copybooks](#bms-generated-copybooks)
8. [JCL Batch Jobs (Core)](#jcl-batch-jobs-core)
9. [JCL Batch Jobs (Optional Modules)](#jcl-batch-jobs-optional-modules)
10. [Assembler Programs](#assembler-programs)
11. [Supporting Assets](#supporting-assets)

---

## Summary

| Asset Type                        | Count |
|-----------------------------------|------:|
| COBOL Programs (Core)             |    31 |
| COBOL Programs (Optional Modules) |    13 |
| Copybooks (Core)                  |    30 |
| Copybooks (Optional Modules)      |    11 |
| BMS Screen Maps (Core)            |    17 |
| BMS Screen Maps (Optional)        |     4 |
| BMS-Generated Copybooks (Core)    |    17 |
| BMS-Generated Copybooks (Optional)|     4 |
| JCL Batch Jobs (Core)             |    38 |
| JCL Batch Jobs (Optional)         |     8 |
| Assembler Programs                |     2 |
| JCL Procedures                    |     2 |
| **Total Artifacts**               | **177** |

---

## COBOL Programs (Core)

### Online CICS Programs (prefix `CO*`)

| # | Program    | LOC  | Description                       | CICS Cmds | IFs | EVALs | PERFORMs | Copybooks Used | Classification        |
|---|------------|-----:|-----------------------------------|----------:|----:|------:|---------:|---------------:|-----------------------|
| 1 | COSGN00C   |  260 | User sign-on / authentication     |        10 |   4 |     3 |       11 |              9 | Security / Login      |
| 2 | COMEN01C   |  308 | Main menu navigation              |         7 |   7 |     3 |       13 |              9 | Navigation            |
| 3 | COADM01C   |  288 | Admin menu                        |         7 |  11 |     4 |       14 |              9 | Admin / Navigation    |
| 4 | COACTVWC   |  941 | Account view (read-only)          |        15 |  57 |    10 |       21 |             15 | Account Management    |
| 5 | COACTUPC   | 4236 | Account update                    |        17 | 168 |    10 |       64 |             56 | Account Management    |
| 6 | COCRDLIC   | 1459 | Card list / browse                |        18 | 122 |    18 |       34 |             13 | Card Management       |
| 7 | COCRDSLC   |  887 | Card detail view                  |        14 |  68 |     8 |       19 |             15 | Card Management       |
| 8 | COCRDUPC   | 1560 | Card update                       |        12 | 148 |    16 |       26 |             15 | Card Management       |
| 9 | COTRN00C   |  699 | Transaction list / browse         |        10 |  26 |     8 |       43 |              8 | Transaction Mgmt      |
|10 | COTRN01C   |  330 | Transaction detail view           |         5 |   7 |     3 |       17 |              8 | Transaction Mgmt      |
|11 | COTRN02C   |  783 | Transaction add (new)             |        11 |  14 |    13 |       61 |             10 | Transaction Mgmt      |
|12 | CORPT00C   |  649 | Transaction report request        |         7 |  20 |     5 |       34 |              8 | Reporting             |
|13 | COBIL00C   |  572 | Bill payment                      |        13 |  10 |     9 |       38 |             10 | Bill Payment          |
|14 | COUSR00C   |  695 | User list / browse (Admin)        |        11 |  25 |     8 |       41 |              8 | User Administration   |
|15 | COUSR01C   |  299 | User add (Admin)                  |         5 |   4 |     3 |       20 |              9 | User Administration   |
|16 | COUSR02C   |  414 | User update (Admin)               |         6 |  13 |     5 |       31 |              8 | User Administration   |
|17 | COUSR03C   |  359 | User delete (Admin)               |         6 |   8 |     5 |       26 |              8 | User Administration   |

### Batch Programs (prefix `CB*`)

| # | Program    | LOC | Description                              | CALLs | IFs | PERFORMs | Copybooks Used | Classification          |
|---|------------|----:|------------------------------------------|------:|----:|---------:|---------------:|-------------------------|
|18 | CBACT01C   | 430 | Account file refresh / load               |     3 |  22 |       35 |              2 | Data Loading            |
|19 | CBACT02C   | 178 | Card data file load                       |     1 |  22 |       10 |              1 | Data Loading            |
|20 | CBACT03C   | 178 | Cross-reference file load                 |     1 |  22 |       10 |              1 | Data Loading            |
|21 | CBACT04C   | 652 | Interest calculation                      |     1 |  86 |       56 |              5 | Financial Calculation   |
|22 | CBCUS01C   | 178 | Customer data file load                   |     1 |  11 |       10 |              1 | Data Loading            |
|23 | CBTRN01C   | 494 | Daily transaction posting                 |     1 |  33 |       42 |              6 | Transaction Processing  |
|24 | CBTRN02C   | 731 | Transaction posting (master update)       |     1 |  93 |       61 |              5 | Transaction Processing  |
|25 | CBTRN03C   | 649 | Transaction report generation             |     1 |  75 |       72 |              5 | Reporting               |
|26 | CBSTM03A   | 924 | Statement generation (main driver)        |    14 |  15 |       29 |              4 | Statement Generation    |
|27 | CBSTM03B   | 230 | Statement generation (formatting sub)     |     0 |  12 |        4 |              0 | Statement Generation    |
|28 | CBEXPORT   | 582 | Data export to flat files                 |     1 |  16 |       45 |              6 | Data Export             |
|29 | CBIMPORT   | 487 | Data import from flat files               |     1 |  14 |       29 |              6 | Data Import             |

### Utility Programs

| # | Program    | LOC | Description                      | Classification     |
|---|------------|----:|----------------------------------|--------------------|
|30 | CSUTLDTC   | 157 | Date/time conversion utility     | Shared Utility     |
|31 | COBSWAIT   |  41 | Wait/delay utility (calls ASM)   | System Utility     |

---

## COBOL Programs (Optional Modules)

### Authorization Module (IMS/DB2/MQ) -- `app/app-authorization-ims-db2-mq/`

| # | Program    | LOC  | Description                               | Classification              |
|---|------------|-----:|-------------------------------------------|-----------------------------|
|32 | COPAUA0C   | 1026 | MQ trigger - authorization request intake | MQ / Authorization          |
|33 | COPAUS0C   | 1032 | Authorization summary display (online)    | Authorization UI            |
|34 | COPAUS1C   |  604 | Authorization detail display (online)     | Authorization UI            |
|35 | COPAUS2C   |  244 | Fraud marking to DB2                      | Fraud Management            |
|36 | CBPAUP0C   |  386 | Batch authorization purge                 | Authorization Maintenance   |
|37 | DBUNLDGS   |  366 | IMS DB GSAM unload utility                | IMS Utility                 |
|38 | PAUDBLOD   |  369 | IMS DB load utility                       | IMS Utility                 |
|39 | PAUDBUNL   |  317 | IMS DB unload utility                     | IMS Utility                 |

### Transaction Type DB2 Module -- `app/app-transaction-type-db2/`

| # | Program    | LOC  | Description                              | Classification            |
|---|------------|-----:|------------------------------------------|---------------------------|
|40 | COTRTLIC   | 2098 | Transaction type list / delete (online)  | Transaction Type Mgmt     |
|41 | COTRTUPC   | 1702 | Transaction type add / update (online)   | Transaction Type Mgmt     |
|42 | COBTUPDT   |  237 | Batch transaction type update            | Transaction Type Mgmt     |

### VSAM-MQ Module -- `app/app-vsam-mq/`

| # | Program    | LOC | Description                              | Classification          |
|---|------------|----:|------------------------------------------|-------------------------|
|43 | COACCT01   | 620 | MQ account inquiry (request/response)    | MQ / Account Inquiry    |
|44 | CODATE01   | 524 | MQ system date service (request/response)| MQ / Date Service       |

---

## Copybooks (Core)

### Data Record Layouts (`CV*` prefix -- VSAM record structures)

| # | Copybook   | LOC | Record Size | Description                            | Business Entity   |
|---|------------|----:|------------:|----------------------------------------|-------------------|
| 1 | CVACT01Y   |  20 |   ~300 bytes| Account master record                  | Account           |
| 2 | CVACT02Y   |  14 |   ~150 bytes| Card master record                     | Card              |
| 3 | CVACT03Y   |  11 |    ~50 bytes| Card-Account cross-reference           | Cross-Reference   |
| 4 | CVCUS01Y   |  26 |   ~500 bytes| Customer master record                 | Customer          |
| 5 | CUSTREC    |  26 |   ~500 bytes| Customer record (statement use)        | Customer          |
| 6 | CVTRA01Y   |  13 |    ~50 bytes| Transaction category balance           | Tran Category Bal |
| 7 | CVTRA02Y   |  13 |    ~50 bytes| Discount group / interest rate         | Discount Group    |
| 8 | CVTRA03Y   |  10 |    ~60 bytes| Transaction type lookup                | Transaction Type  |
| 9 | CVTRA04Y   |  12 |    ~60 bytes| Transaction category type              | Tran Cat Type     |
|10 | CVTRA05Y   |  21 |   ~350 bytes| Transaction record (online)            | Transaction       |
|11 | CVTRA06Y   |  21 |   ~350 bytes| Daily transaction record               | Daily Transaction |
|12 | CVTRA07Y   |  73 |       varies| Transaction report layout/headers      | Report Layout     |
|13 | CVEXPORT   | 103 |   ~460 bytes| Export/import record (multi-type)      | Export Record     |
|14 | COSTM01    |  38 |   ~350 bytes| Statement transaction record           | Statement Tran    |

### Communication / Session Copybooks

| # | Copybook   | LOC | Description                              | Classification        |
|---|------------|----:|------------------------------------------|-----------------------|
|15 | COCOM01Y   |  47 | Common communication area (COMMAREA)     | Session / Navigation  |
|16 | CVCRD01Y   |  46 | Card detail communication area           | Card UI Context       |
|17 | COMEN02Y   | 101 | Main menu option definitions             | Menu Configuration    |
|18 | COADM02Y   |  62 | Admin menu option definitions            | Menu Configuration    |
|19 | COTTL01Y   |  27 | Screen title/header constants            | UI Constants          |
|20 | CSMSG01Y   |  24 | Common user messages                     | UI Messages           |
|21 | CSMSG02Y   |  35 | Abend/error message structure            | Error Handling        |

### Utility / Support Copybooks

| # | Copybook   | LOC  | Description                             | Classification      |
|---|------------|-----:|-----------------------------------------|---------------------|
|22 | CSDAT01Y   |   58 | Date/time working storage fields        | Date Utility        |
|23 | CSUTLDWY   |   89 | Date editing working storage            | Date Utility        |
|24 | CSUTLDPY   |  375 | Date utility procedure division         | Date Utility        |
|25 | CODATECN   |   52 | Date conversion parameters              | Date Utility        |
|26 | CSLKPCDY   | 1318 | US phone area code / state / zip lookup | Lookup Tables       |
|27 | CSSETATY   |   30 | Set attribute (BMS field attributes)    | UI Utility          |
|28 | CSSTRPFY   |   85 | String padding/formatting utility       | String Utility      |
|29 | CSUSR01Y   |   26 | User security record layout             | Security            |
|30 | UNUSED1Y   |   10 | Unused / placeholder record             | Deprecated          |

---

## Copybooks (Optional Modules)

### Authorization Module Copybooks

| # | Copybook   | LOC | Description                               | Business Entity       |
|---|------------|----:|-------------------------------------------|-----------------------|
|31 | CIPAUDTY   |  54 | Authorization detail record (IMS/DB2)     | Auth Detail           |
|32 | CIPAUSMY   |  31 | Authorization summary record              | Auth Summary          |
|33 | CCPAURQY   |  36 | Authorization request (MQ message)        | Auth Request          |
|34 | CCPAURLY   |  24 | Authorization reply (MQ message)          | Auth Reply            |
|35 | CCPAUERY   |  40 | Authorization error record                | Auth Error            |
|36 | IMSFUNCS   |  27 | IMS DL/I function codes                   | IMS Utility           |
|37 | PADFLPCB   |  26 | IMS PCB - Detail file                     | IMS PCB               |
|38 | PASFLPCB   |  26 | IMS PCB - Summary file                    | IMS PCB               |
|39 | PAUTBPCB   |  26 | IMS PCB - Auth table                      | IMS PCB               |

### Transaction Type DB2 Copybooks

| # | Copybook   | LOC | Description                              | Business Entity        |
|---|------------|----:|------------------------------------------|------------------------|
|40 | CSDB2RPY   |   - | DB2 return code processing (procedure)   | DB2 Utility            |
|41 | CSDB2RWY   |  46 | DB2 working storage / SQLCA fields       | DB2 Utility            |

---

## BMS Screen Maps

### Core BMS Maps (`app/bms/`)

| # | Map Name | Map ID  | Screen Title                    | Associated Program | Input Fields | Classification      |
|---|----------|---------|--------------------------------|--------------------|-------------:|---------------------|
| 1 | COSGN00  | COSGN0A | Sign-On                         | COSGN00C           |            3 | Security            |
| 2 | COMEN01  | COMEN1A | Main Menu                       | COMEN01C           |            1 | Navigation          |
| 3 | COADM01  | COADM1A | Admin Menu                      | COADM01C           |            1 | Admin Navigation    |
| 4 | COACTVW  | COACT0A | Account View                    | COACTVWC           |            1 | Account Mgmt        |
| 5 | COACTUP  | COACU0A | Account Update                  | COACTUPC           |           10 | Account Mgmt        |
| 6 | COCRDLI  | COCRD0A | Card List                       | COCRDLIC           |            2 | Card Mgmt           |
| 7 | COCRDSL  | COCRDS0 | Card Detail                     | COCRDSLC           |            1 | Card Mgmt           |
| 8 | COCRDUP  | COCRDU0 | Card Update                     | COCRDUPC           |            5 | Card Mgmt           |
| 9 | COTRN00  | COTRN0A | Transaction List                | COTRN00C           |            2 | Transaction Mgmt    |
|10 | COTRN01  | COTRN1A | Transaction Detail              | COTRN01C           |            1 | Transaction Mgmt    |
|11 | COTRN02  | COTRN2A | Transaction Add                 | COTRN02C           |            8 | Transaction Mgmt    |
|12 | CORPT00  | CORPT0A | Report Request                  | CORPT00C           |            4 | Reporting           |
|13 | COBIL00  | COBIL0A | Bill Payment                    | COBIL00C           |            2 | Bill Payment        |
|14 | COUSR00  | COUSR0A | User List (Admin)               | COUSR00C           |            2 | User Admin          |
|15 | COUSR01  | COUSR1A | User Add (Admin)                | COUSR01C           |            5 | User Admin          |
|16 | COUSR02  | COUSR2A | User Update (Admin)             | COUSR02C           |            5 | User Admin          |
|17 | COUSR03  | COUSR3A | User Delete (Admin)             | COUSR03C           |            1 | User Admin          |

### Optional Module BMS Maps

| # | Map Name | Module                        | Associated Program | Classification      |
|---|----------|-------------------------------|--------------------|---------------------|
|18 | COPAU00  | Authorization (IMS/DB2/MQ)    | COPAUS0C           | Auth Summary        |
|19 | COPAU01  | Authorization (IMS/DB2/MQ)    | COPAUS1C           | Auth Detail         |
|20 | COTRTLI  | Transaction Type (DB2)        | COTRTLIC           | Tran Type List      |
|21 | COTRTUP  | Transaction Type (DB2)        | COTRTUPC           | Tran Type Update    |

---

## BMS-Generated Copybooks

| # | Copybook     | Source Map | Location     |
|---|--------------|------------|--------------|
| 1 | COSGN00.CPY  | COSGN00    | app/cpy-bms/ |
| 2 | COMEN01.CPY  | COMEN01    | app/cpy-bms/ |
| 3 | COADM01.CPY  | COADM01    | app/cpy-bms/ |
| 4 | COACTVW.CPY  | COACTVW    | app/cpy-bms/ |
| 5 | COACTUP.CPY  | COACTUP    | app/cpy-bms/ |
| 6 | COCRDLI.CPY  | COCRDLI    | app/cpy-bms/ |
| 7 | COCRDSL.CPY  | COCRDSL    | app/cpy-bms/ |
| 8 | COCRDUP.CPY  | COCRDUP    | app/cpy-bms/ |
| 9 | COTRN00.CPY  | COTRN00    | app/cpy-bms/ |
|10 | COTRN01.CPY  | COTRN01    | app/cpy-bms/ |
|11 | COTRN02.CPY  | COTRN02    | app/cpy-bms/ |
|12 | CORPT00.CPY  | CORPT00    | app/cpy-bms/ |
|13 | COBIL00.CPY  | COBIL00    | app/cpy-bms/ |
|14 | COUSR00.CPY  | COUSR00    | app/cpy-bms/ |
|15 | COUSR01.CPY  | COUSR01    | app/cpy-bms/ |
|16 | COUSR02.CPY  | COUSR02    | app/cpy-bms/ |
|17 | COUSR03.CPY  | COUSR03    | app/cpy-bms/ |
|18 | COPAU00.CPY  | COPAU00    | app/app-authorization-ims-db2-mq/cpy-bms/ |
|19 | COPAU01.CPY  | COPAU01    | app/app-authorization-ims-db2-mq/cpy-bms/ |
|20 | COTRTLI.CPY  | COTRTLI    | app/app-transaction-type-db2/cpy-bms/ |
|21 | COTRTUP.CPY  | COTRTUP    | app/app-transaction-type-db2/cpy-bms/ |

---

## JCL Batch Jobs (Core)

### Data File Management

| # | JCL Job    | Description                                    | Programs Executed         | Classification     |
|---|------------|------------------------------------------------|---------------------------|--------------------|
| 1 | ACCTFILE   | Define/refresh account master VSAM             | IDCAMS                    | Data Provisioning  |
| 2 | CARDFILE   | Define/refresh card master VSAM                | IDCAMS                    | Data Provisioning  |
| 3 | CUSTFILE   | Define/refresh customer master VSAM            | IDCAMS                    | Data Provisioning  |
| 4 | XREFFILE   | Define/load card cross-reference VSAM          | IDCAMS                    | Data Provisioning  |
| 5 | TRANFILE   | Define/load transaction master VSAM            | IDCAMS, SDSF              | Data Provisioning  |
| 6 | DUSRSECJ   | Load user security VSAM file                   | IDCAMS                    | Security Setup     |
| 7 | TRANTYPE   | Define/load transaction type VSAM              | IDCAMS                    | Data Provisioning  |
| 8 | TRANCATG   | Define/load transaction category VSAM          | IDCAMS                    | Data Provisioning  |
| 9 | DISCGRP    | Define/load discount group VSAM                | IDCAMS                    | Data Provisioning  |
|10 | TCATBALF   | Define/load transaction category balance VSAM  | IDCAMS                    | Data Provisioning  |
|11 | REPTFILE   | Define report output file                      | IDCAMS                    | Data Provisioning  |
|12 | DEFCUST    | Define customer VSAM cluster                   | IDCAMS                    | Data Provisioning  |
|13 | ESDSRRDS   | Define ESDS/RRDS experimental clusters         | IDCAMS                    | Data Provisioning  |
|14 | READACCT   | Read/print account VSAM records                | IDCAMS                    | Data Verification  |
|15 | READCARD   | Read/print card VSAM records                   | IDCAMS                    | Data Verification  |
|16 | READCUST   | Read/print customer VSAM records               | IDCAMS                    | Data Verification  |
|17 | READXREF   | Read/print cross-reference VSAM records        | IDCAMS                    | Data Verification  |
|18 | PRTCATBL   | Print catalog listing                          | IDCAMS                    | Data Verification  |

### Batch Processing Cycle

| # | JCL Job    | Description                                    | Programs Executed         | Classification          |
|---|------------|------------------------------------------------|---------------------------|-------------------------|
|19 | CLOSEFIL   | Close CICS files for batch processing          | SDSF                      | Batch Cycle Control     |
|20 | OPENFIL    | Open CICS files after batch processing         | SDSF                      | Batch Cycle Control     |
|21 | POSTTRAN   | Post daily transactions to master              | CBTRN01C, CBTRN02C        | Transaction Processing  |
|22 | INTCALC    | Calculate interest charges                     | CBACT04C                  | Financial Calculation   |
|23 | TRANBKP    | Backup transaction file (GDG)                  | IDCAMS                    | Backup / Recovery       |
|24 | COMBTRAN   | Combine transactions from backup               | SORT                      | Transaction Processing  |
|25 | CREASTMT   | Generate customer statements                   | SORT, IDCAMS, CBSTM03A   | Statement Generation    |
|26 | TRANIDX    | Define/build alternate indexes                 | IDCAMS                    | Index Management        |
|27 | TRANREPT   | Generate transaction reports                   | SORT, CBTRN03C            | Reporting               |
|28 | DALYREJS   | Process daily rejects                          | SORT, IDCAMS              | Error Processing        |

### GDG / Infrastructure

| # | JCL Job    | Description                                    | Programs Executed         | Classification     |
|---|------------|------------------------------------------------|---------------------------|--------------------|
|29 | DEFGDGB    | Define GDG base for transaction backup         | IDCAMS                    | Infrastructure     |
|30 | DEFGDGD    | Define GDG base for daily transactions         | IDCAMS                    | Infrastructure     |

### Export / Import

| # | JCL Job    | Description                                    | Programs Executed         | Classification     |
|---|------------|------------------------------------------------|---------------------------|--------------------|
|31 | CBEXPORT   | Export data to flat files                      | CBEXPORT                  | Data Export        |
|32 | CBIMPORT   | Import data from flat files                    | CBIMPORT                  | Data Import        |

### Utility / Administrative

| # | JCL Job    | Description                                    | Programs Executed         | Classification     |
|---|------------|------------------------------------------------|---------------------------|--------------------|
|33 | WAITSTEP   | Wait step (uses COBSWAIT)                      | COBSWAIT                  | Utility            |
|34 | CBADMCDJ   | Admin card maintenance batch                   | IDCAMS                    | Admin Utility      |
|35 | TXT2PDF1   | Convert statement text to PDF                  | IKJEFT1B (TXT2PDF)        | Post-Processing    |
|36 | FTPJCL     | FTP file transfer                              | FTP                       | File Transfer      |
|37 | INTRDRJ1   | Internal reader job 1 (chain submission)       | IDCAMS, IEBGENER          | Job Scheduling     |
|38 | INTRDRJ2   | Internal reader job 2 (chained)                | IDCAMS                    | Job Scheduling     |

---

## JCL Batch Jobs (Optional Modules)

### Authorization Module JCL

| # | JCL Job    | Description                                    | Programs Executed         |
|---|------------|------------------------------------------------|---------------------------|
|39 | CBPAUP0J   | Batch purge old authorizations                 | DFSRRC00 (CBPAUP0C)      |
|40 | DBPAUTP0   | Unload/export auth IMS DB                      | DFSRRC00                  |
|41 | LOADPADB   | Load auth data into IMS DB                     | DFSRRC00 (PAUDBLOD)      |
|42 | UNLDGSAM   | Unload IMS GSAM data                           | DFSRRC00 (DBUNLDGS)      |
|43 | UNLDPADB   | Unload auth IMS DB                             | DFSRRC00 (PAUDBUNL)      |

### Transaction Type DB2 Module JCL

| # | JCL Job    | Description                                    | Programs Executed           |
|---|------------|------------------------------------------------|-----------------------------|
|44 | CREADB21   | Create DB2 tables and load initial data        | IKJEFT01                    |
|45 | MNTTRDB2   | Maintain transaction type DB2 data             | IKJEFT01                    |
|46 | TRANEXTR   | Extract transactions for DB2 processing        | IEBGENER, IKJEFT01          |

---

## Assembler Programs

| # | Program    | Location  | Description                       | Called By   |
|---|------------|-----------|-----------------------------------|-------------|
| 1 | MVSWAIT    | app/asm/  | Wait/delay service (MVS STIMER)   | COBSWAIT    |
| 2 | COBDATFT   | app/asm/  | Date format conversion            | CBACT01C    |

---

## Supporting Assets

### JCL Procedures (`app/proc/`)

| Procedure   | Description                          |
|-------------|--------------------------------------|
| REPROC.prc  | Reprocessing procedure               |
| TRANREPT.prc| Transaction report procedure         |

### Data Files (`app/data/ASCII/`)

| File           | Description                      |
|----------------|----------------------------------|
| acctdata.txt   | Account master sample data       |
| carddata.txt   | Card master sample data          |
| cardxref.txt   | Card cross-reference data        |
| custdata.txt   | Customer master sample data      |
| dailytran.txt  | Daily transaction sample data    |
| discgrp.txt    | Discount group data              |
| tcatbal.txt    | Transaction category balances    |
| trancatg.txt   | Transaction category data        |
| trantype.txt   | Transaction type data            |

### Scheduler Configurations (`app/scheduler/`)

| File               | Description                          |
|--------------------|--------------------------------------|
| CardDemo.ca7       | CA-7 scheduler configuration         |
| CardDemo.controlm  | Control-M scheduler configuration    |

### Other Assets

| Asset              | Location      | Description                       |
|--------------------|---------------|-----------------------------------|
| REPROCT.ctl        | app/ctl/      | Reprocessing control file         |
| LISTCAT.txt        | app/catlg/    | VSAM catalog listing              |
| ASMWAIT.mac        | app/maclib/   | Assembler wait macro              |
| COCDATFT.mac       | app/maclib/   | Date format assembler macro       |
| CARDDEMO.CSD       | app/csd/      | CICS resource definitions         |
