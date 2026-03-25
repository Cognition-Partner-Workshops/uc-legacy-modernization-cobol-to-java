# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
> **Platform**: COBOL / CICS / VSAM / JCL / DB2 / IMS / MQ

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **33 COBOL programs**, **32 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, and **13 optional-module programs** across three extension modules. The application supports account management, card management, transaction processing, bill payments, statement generation, and reporting — with two user roles (Regular and Admin).

| Asset Category            | Count |
|---------------------------|-------|
| Core COBOL Programs       | 33    |
| Optional Module Programs  | 13    |
| Core Copybooks            | 32    |
| Optional Module Copybooks | 12    |
| BMS Screen Maps (core)    | 17    |
| BMS Screen Maps (optional)| 4     |
| BMS-Generated Copybooks   | 17    |
| JCL Batch Jobs (core)     | 38    |
| JCL Batch Jobs (optional) | 8     |
| Assembler Programs        | 2     |
| JCL Procedures            | 2     |
| Control Files             | 1     |
| Scheduler Configs         | 2     |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program     | Lines | Trans ID | Function                              | Classification         |
|---|-------------|-------|----------|---------------------------------------|------------------------|
| 1 | COSGN00C    | 260   | CC00     | User Sign-on / Authentication         | Security / Login       |
| 2 | COMEN01C    | 308   | CM00     | Main Menu Navigation                  | Navigation / UI        |
| 3 | COADM01C    | 288   | CA00     | Admin Menu Navigation                 | Navigation / UI (Admin)|
| 4 | COACTVWC    | 941   | CA01     | Account View (read-only)              | Account Management     |
| 5 | COACTUPC    | 4,236 | CA02     | Account Update (full CRUD)            | Account Management     |
| 6 | COCRDLIC    | 1,459 | CC01     | Card List (paginated browse)          | Card Management        |
| 7 | COCRDSLC    | 887   | CC02     | Card Detail View                      | Card Management        |
| 8 | COCRDUPC    | 1,560 | CC03     | Card Update                           | Card Management        |
| 9 | COTRN00C    | 699   | CT00     | Transaction List (paginated browse)   | Transaction Management |
|10 | COTRN01C    | 330   | CT01     | Transaction Detail View               | Transaction Management |
|11 | COTRN02C    | 783   | CT02     | Transaction Add (new transaction)     | Transaction Management |
|12 | CORPT00C    | 649   | CR00     | Transaction Report Request            | Reporting              |
|13 | COBIL00C    | 572   | CB00     | Bill Payment Processing               | Payment Processing     |
|14 | COUSR00C    | 695   | CU00     | User List (Admin — paginated browse)  | User Administration    |
|15 | COUSR01C    | 299   | CU01     | User Add (Admin)                      | User Administration    |
|16 | COUSR02C    | 414   | CU02     | User Update (Admin)                   | User Administration    |
|17 | COUSR03C    | 359   | CU03     | User Delete (Admin)                   | User Administration    |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program     | Lines | Function                                         | Classification           |
|---|-------------|-------|--------------------------------------------------|--------------------------|
|18 | CBACT01C    | 430   | Read Account master VSAM, write sequential output | Data Extract             |
|19 | CBACT02C    | 178   | Read Card data VSAM file                          | Data Extract             |
|20 | CBACT03C    | 178   | Read Card Cross-Reference VSAM file               | Data Extract             |
|21 | CBACT04C    | 652   | Interest Calculation on accounts                  | Financial Calculation    |
|22 | CBCUS01C    | 178   | Read Customer master VSAM file                    | Data Extract             |
|23 | CBTRN01C    | 494   | Daily Transaction validation & enrichment         | Transaction Processing   |
|24 | CBTRN02C    | 731   | Transaction Posting (core batch posting engine)   | Transaction Processing   |
|25 | CBTRN03C    | 649   | Daily Transaction Report generation               | Reporting                |
|26 | CBSTM03A    | 924   | Statement Generation (text + HTML)                | Statement Generation     |
|27 | CBSTM03B    | 230   | Statement File-handling subroutine (called by 03A)| Statement Generation     |
|28 | CBEXPORT    | 582   | Export all VSAM data to flat file                 | Data Export              |
|29 | CBIMPORT    | 487   | Import flat file into VSAM files                  | Data Import              |

### 1.3 Utility Programs

| # | Program     | Lines | Function                                         | Classification       |
|---|-------------|-------|--------------------------------------------------|----------------------|
|30 | COBSWAIT    | 41    | Wait utility (batch timer/delay)                 | Utility              |
|31 | CSUTLDTC    | 157   | Date validation via CEEDAYS API                  | Utility / Date       |

### 1.4 Assembler Programs (`app/asm/`)

| # | Program     | Function                                         | Classification       |
|---|-------------|--------------------------------------------------|----------------------|
|32 | MVSWAIT     | Assembler wait routine (MVS STIMER)              | Utility              |
|33 | COBDATFT    | COBOL date format transformation helper          | Utility / Date       |

---

## 2. Optional Module Programs

### 2.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program     | Lines | Function                                         | Classification             |
|---|-------------|-------|--------------------------------------------------|----------------------------|
| 1 | COPAUA0C    | 210   | MQ Trigger — Authorization request listener      | MQ Integration / Trigger   |
| 2 | COPAUS0C    | 548   | Authorization Summary display (CICS)             | Authorization / UI         |
| 3 | COPAUS1C    | 465   | Authorization Detail display (CICS)              | Authorization / UI         |
| 4 | COPAUS2C    | 515   | Fraud Marking to DB2 (CICS)                      | Fraud Detection / DB2      |
| 5 | CBPAUP0C    | 198   | Batch Purge of processed authorizations           | Batch / DB2 Maintenance    |
| 6 | PAUDBLOD    | 369   | IMS DB Load utility                              | IMS DB Administration      |
| 7 | PAUDBUNL    | 317   | IMS DB Unload utility                            | IMS DB Administration      |
| 8 | DBUNLDGS    | 366   | IMS DB Unload (GS call variant)                  | IMS DB Administration      |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program     | Lines | Function                                         | Classification             |
|---|-------------|-------|--------------------------------------------------|----------------------------|
| 9 | COTRTUPC    | 1,702 | Transaction Type Add/Edit (CICS + DB2 cursors)   | Reference Data / DB2 CRUD  |
|10 | COTRTLIC    | 2,098 | Transaction Type List/Delete (CICS + DB2 cursors)| Reference Data / DB2 CRUD  |
|11 | COBTUPDT    | 237   | Batch Transaction Type update                    | Reference Data / Batch     |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program     | Lines | Function                                         | Classification             |
|---|-------------|-------|--------------------------------------------------|----------------------------|
|12 | COACCT01    | 620   | MQ Request/Response — Account Inquiry            | MQ Integration / Account   |
|13 | CODATE01    | 524   | MQ Request/Response — System Date Service        | MQ Integration / Utility   |

---

## 3. Copybooks — Core (`app/cpy/`)

### 3.1 Data Structure Copybooks (prefix `CV*` — VSAM Record Layouts)

| # | Copybook   | Record Len | Business Entity                       | Classification            |
|---|------------|------------|---------------------------------------|---------------------------|
| 1 | CVACT01Y   | 300 bytes  | Account Master Record                 | Account Data              |
| 2 | CVACT02Y   | 150 bytes  | Card Data Record                      | Card Data                 |
| 3 | CVACT03Y   | 50 bytes   | Card-to-Account Cross-Reference       | Cross-Reference           |
| 4 | CVCRD01Y   | ~350 bytes | Card Detail Record (extended)         | Card Data (Extended)      |
| 5 | CVCUS01Y   | 500 bytes  | Customer Master Record                | Customer Data             |
| 6 | CVTRA01Y   | 50 bytes   | Transaction Category Balance          | Transaction Aggregate     |
| 7 | CVTRA02Y   | 50 bytes   | Disclosure Group Record               | Reference Data            |
| 8 | CVTRA03Y   | 60 bytes   | Transaction Type Record               | Reference Data            |
| 9 | CVTRA04Y   | 60 bytes   | Transaction Category Type Record      | Reference Data            |
|10 | CVTRA05Y   | 350 bytes  | Transaction Record (online)           | Transaction Data          |
|11 | CVTRA06Y   | 350 bytes  | Daily Transaction Record              | Transaction Data (Daily)  |
|12 | CVTRA07Y   | varies     | Transaction Report Detail layout      | Report Layout             |
|13 | CVEXPORT   | varies     | Export Record (multi-entity)          | Data Export Layout        |

### 3.2 Common / Infrastructure Copybooks (prefix `CO*`, `CS*`, `C*`)

| # | Copybook   | Function                                         | Classification            |
|---|------------|--------------------------------------------------|---------------------------|
|14 | COCOM01Y   | COMMAREA — inter-program communication area      | Infrastructure            |
|15 | COMEN02Y   | Menu option definitions & program mappings        | Navigation Config         |
|16 | COADM02Y   | Admin menu option definitions                     | Navigation Config (Admin) |
|17 | CODATECN   | Date conversion constants / formats               | Utility / Date            |
|18 | COSTM01    | Transaction altered layout for statement reports  | Report Layout             |
|19 | COTTL01Y   | Screen title / header definition                  | UI / Header               |
|20 | CSDAT01Y   | Date formatting working-storage fields            | Utility / Date            |
|21 | CSLKPCDY   | Lookup code table (large — 51 KB)                | Reference Data / Lookup   |
|22 | CSMSG01Y   | Short message area (user messages)                | UI / Messaging            |
|23 | CSMSG02Y   | Long message area (detail messages)               | UI / Messaging            |
|24 | CSSETATY   | Screen field attribute-setting macro              | UI / Field Attributes     |
|25 | CSSTRPFY   | String parsing / manipulation functions            | Utility / String          |
|26 | CSUSR01Y   | User Security Record (80 bytes)                   | Security / User           |
|27 | CSUTLDPY   | Date utility procedures (display formats)         | Utility / Date            |
|28 | CSUTLDWY   | Date utility working-storage                      | Utility / Date            |
|29 | CUSTREC    | Customer record layout (for batch statement)      | Customer Data (Batch)     |
|30 | UNUSED1Y   | Unused / placeholder record                       | Deprecated                |

### 3.3 BMS-Generated Copybooks (`app/cpy-bms/`)

| # | Copybook   | Associated Map | Screen                               |
|---|------------|----------------|--------------------------------------|
| 1 | COACTUP    | COACTUP.bms    | Account Update Screen                |
| 2 | COACTVW    | COACTVW.bms    | Account View Screen                  |
| 3 | COADM01    | COADM01.bms    | Admin Menu Screen                    |
| 4 | COBIL00    | COBIL00.bms    | Bill Payment Screen                  |
| 5 | COCRDLI    | COCRDLI.bms    | Card List Screen                     |
| 6 | COCRDSL    | COCRDSL.bms    | Card Detail View Screen              |
| 7 | COCRDUP    | COCRDUP.bms    | Card Update Screen                   |
| 8 | COMEN01    | COMEN01.bms    | Main Menu Screen                     |
| 9 | CORPT00    | CORPT00.bms    | Report Request Screen                |
|10 | COSGN00    | COSGN00.bms    | Sign-on Screen                       |
|11 | COTRN00    | COTRN00.bms    | Transaction List Screen              |
|12 | COTRN01    | COTRN01.bms    | Transaction Detail Screen            |
|13 | COTRN02    | COTRN02.bms    | Transaction Add Screen               |
|14 | COUSR00    | COUSR00.bms    | User List Screen (Admin)             |
|15 | COUSR01    | COUSR01.bms    | User Add Screen (Admin)              |
|16 | COUSR02    | COUSR02.bms    | User Update Screen (Admin)           |
|17 | COUSR03    | COUSR03.bms    | User Delete Screen (Admin)           |

---

## 4. Optional Module Copybooks

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook   | Function                                         |
|---|------------|--------------------------------------------------|
| 1 | CCPAUERY   | Authorization Error Record layout                |
| 2 | CCPAURLY   | Authorization Reply Record layout                |
| 3 | CCPAURQY   | Authorization Request Record layout              |
| 4 | CIPAUSMY   | Authorization Summary IMS segment layout         |
| 5 | CIPAUDTY   | Authorization Detail IMS segment layout          |
| 6 | IMSFUNCS   | IMS DL/I function codes (GU, GN, ISRT, etc.)    |
| 7 | PADFLPCB   | IMS PCB for PADFL database                       |
| 8 | PASFLPCB   | IMS PCB for PASFL database                       |
| 9 | PAUTBPCB   | IMS PCB for PAUTB database                       |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook   | Function                                         |
|---|------------|--------------------------------------------------|
|10 | CSDB2RPY   | DB2 Common Procedures (DSNTIAC, error handling)  |
|11 | CSDB2RWY   | DB2 Common Working-Storage (SQLCODE vars)        |

---

## 5. BMS Screen Maps — Core (`app/bms/`)

| # | Map File    | Size (KB) | Screen                         | Programs Using It    |
|---|-------------|-----------|--------------------------------|----------------------|
| 1 | COSGN00.bms | 13.3      | Sign-on / Login                | COSGN00C             |
| 2 | COMEN01.bms | 10.3      | Main Menu                      | COMEN01C             |
| 3 | COADM01.bms | 10.3      | Admin Menu                     | COADM01C             |
| 4 | COACTVW.bms | 22.3      | Account View                   | COACTVWC             |
| 5 | COACTUP.bms | 30.7      | Account Update                 | COACTUPC             |
| 6 | COCRDLI.bms | 21.0      | Card List                      | COCRDLIC             |
| 7 | COCRDSL.bms | 9.5       | Card Detail View               | COCRDSLC             |
| 8 | COCRDUP.bms | 10.4      | Card Update                    | COCRDUPC             |
| 9 | COTRN00.bms | 28.7      | Transaction List               | COTRN00C             |
|10 | COTRN01.bms | 16.7      | Transaction Detail View        | COTRN01C             |
|11 | COTRN02.bms | 18.9      | Transaction Add                | COTRN02C             |
|12 | CORPT00.bms | 14.3      | Report Request                 | CORPT00C             |
|13 | COBIL00.bms | 8.6       | Bill Payment                   | COBIL00C             |
|14 | COUSR00.bms | 28.7      | User List (Admin)              | COUSR00C             |
|15 | COUSR01.bms | 10.1      | User Add (Admin)               | COUSR01C             |
|16 | COUSR02.bms | 10.4      | User Update (Admin)            | COUSR02C             |
|17 | COUSR03.bms | 9.4       | User Delete (Admin)            | COUSR03C             |

### 5.1 Optional Module BMS Maps

| # | Map File                                         | Screen                       |
|---|--------------------------------------------------|------------------------------|
|18 | app-authorization-ims-db2-mq/bms/COPAU00.bms     | Authorization Summary Screen |
|19 | app-authorization-ims-db2-mq/bms/COPAU01.bms     | Authorization Detail Screen  |
|20 | app-transaction-type-db2/bms/COTRTLI.bms          | Transaction Type List Screen |
|21 | app-transaction-type-db2/bms/COTRTUP.bms          | Transaction Type Update Screen|

---

## 6. JCL Batch Jobs — Core (`app/jcl/`)

### 6.1 Data Refresh / File Definition Jobs

| # | Job Name    | Function                                               | Programs Invoked       |
|---|-------------|--------------------------------------------------------|------------------------|
| 1 | ACCTFILE    | Define & refresh Account Master VSAM KSDS              | IDCAMS, SDSF           |
| 2 | CARDFILE    | Define & refresh Card Data VSAM KSDS + alt index       | IDCAMS, SDSF           |
| 3 | CUSTFILE    | Define & refresh Customer Master VSAM KSDS             | IDCAMS, SDSF           |
| 4 | XREFFILE    | Define & load Card Cross-Reference VSAM + alt index    | IDCAMS                 |
| 5 | TRANFILE    | Define & refresh Transaction Master VSAM KSDS + alt idx| IDCAMS, SDSF           |
| 6 | DUSRSECJ    | Load User Security VSAM KSDS from flat file            | IEBGENER, IDCAMS       |
| 7 | TRANTYPE    | Define & load Transaction Type reference VSAM          | IDCAMS                 |
| 8 | TRANCATG    | Define & load Transaction Category reference VSAM      | IDCAMS                 |
| 9 | TCATBALF    | Define & load Transaction Category Balance VSAM        | IDCAMS                 |
|10 | DISCGRP     | Define & load Disclosure Group reference VSAM          | IDCAMS                 |
|11 | DEFCUST     | Define Customer VSAM cluster                           | IDCAMS                 |

### 6.2 Batch Processing Jobs

| # | Job Name    | Function                                               | Programs Invoked       |
|---|-------------|--------------------------------------------------------|------------------------|
|12 | POSTTRAN    | Core Transaction Posting (daily batch)                 | CBTRN02C               |
|13 | INTCALC     | Interest Calculation on accounts                       | CBACT04C               |
|14 | COMBTRAN    | Combine/merge daily transactions into master            | SORT, IDCAMS           |
|15 | CREASTMT    | Generate account statements (text + HTML)              | SORT, IDCAMS, CBSTM03A |
|16 | TRANBKP     | Backup transaction files                               | IDCAMS                 |
|17 | TRANIDX     | Define/build alternate index on transactions           | IDCAMS                 |
|18 | TRANREPT    | Generate Daily Transaction Report                      | SORT, CBTRN03C         |

### 6.3 Data Extract / Read Jobs

| # | Job Name    | Function                                               | Programs Invoked       |
|---|-------------|--------------------------------------------------------|------------------------|
|19 | READACCT    | Read/extract Account data to sequential files          | CBACT01C               |
|20 | READCARD    | Read/extract Card data                                 | CBACT02C               |
|21 | READCUST    | Read/extract Customer data                             | CBCUS01C               |
|22 | READXREF    | Read/extract Cross-Reference data                      | CBACT03C               |
|23 | CBEXPORT    | Export all VSAM files to unified flat file             | IDCAMS, CBEXPORT       |
|24 | CBIMPORT    | Import unified flat file into VSAM files               | CBIMPORT               |
|25 | PRTCATBL    | Print transaction category balance report              | SORT                   |
|26 | REPTFILE    | Define report output file                              | IDCAMS                 |

### 6.4 CICS / Infrastructure Jobs

| # | Job Name    | Function                                               | Programs Invoked       |
|---|-------------|--------------------------------------------------------|------------------------|
|27 | CLOSEFIL    | Close CICS-managed files for batch                     | SDSF                   |
|28 | OPENFIL     | Re-open CICS-managed files after batch                 | SDSF                   |
|29 | CBADMCDJ    | CICS CSD resource definition update                    | DFHCSDUP               |
|30 | WAITSTEP    | Batch wait/delay step                                  | COBSWAIT               |

### 6.5 Utility / GDG / Miscellaneous Jobs

| # | Job Name    | Function                                               | Programs Invoked       |
|---|-------------|--------------------------------------------------------|------------------------|
|31 | DEFGDGB     | Define GDG base clusters                               | IDCAMS                 |
|32 | DEFGDGD     | Define GDG data entries + backup copies                | IDCAMS, IEBGENER       |
|33 | DALYREJS    | Define daily rejection file                            | IDCAMS                 |
|34 | ESDSRRDS    | Define ESDS/RRDS VSAM clusters (demo)                  | IEBGENER, IDCAMS       |
|35 | FTPJCL      | FTP file transfer job                                  | FTP                    |
|36 | INTRDRJ1    | Internal Reader — trigger chained job                  | IDCAMS, IEBGENER       |
|37 | INTRDRJ2    | Internal Reader — chained target job                   | IDCAMS                 |
|38 | TXT2PDF1    | Convert text statement to PDF                          | IKJEFT1B (TXT2PDF)     |

### 6.6 Optional Module JCL Jobs

#### Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job Name    | Function                                               |
|---|-------------|--------------------------------------------------------|
|39 | CBPAUP0J    | Batch purge of processed authorizations                |
|40 | DBPAUTP0    | IMS DB utility processing                              |
|41 | LOADPADB    | Load IMS authorization database                        |
|42 | UNLDGSAM    | Unload IMS DB (GS call)                                |
|43 | UNLDPADB    | Unload IMS authorization database                      |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`)

| # | Job Name    | Function                                               |
|---|-------------|--------------------------------------------------------|
|44 | CREADB21    | Create DB2 tables for transaction types                |
|45 | MNTTRDB2    | Maintain/update transaction type DB2 data              |
|46 | TRANEXTR    | Extract transaction type data from DB2                 |

---

## 7. Supporting Assets

### 7.1 JCL Procedures (`app/proc/`)

| Procedure    | Function                                    |
|-------------|---------------------------------------------|
| REPROC.prc  | Reprocessing procedure template             |
| TRANREPT.prc| Transaction report procedure template       |

### 7.2 Scheduler Configurations (`app/scheduler/`)

| Config               | Function                              |
|----------------------|---------------------------------------|
| CardDemo.ca7         | CA7 job scheduling definitions        |
| CardDemo.controlm    | Control-M job scheduling definitions  |

### 7.3 Data Files (`app/data/ASCII/`)

| File           | Business Entity                        |
|----------------|----------------------------------------|
| acctdata.txt   | Account master seed data               |
| carddata.txt   | Card master seed data                  |
| cardxref.txt   | Card-to-account cross-reference data   |
| custdata.txt   | Customer master seed data              |
| dailytran.txt  | Daily transaction seed data            |
| discgrp.txt    | Disclosure group reference data        |
| tcatbal.txt    | Transaction category balance data      |
| trancatg.txt   | Transaction category reference data    |
| trantype.txt   | Transaction type reference data        |

---

## 8. Naming Conventions

| Prefix | Meaning                                        |
|--------|------------------------------------------------|
| `CO*`  | Online CICS program                            |
| `CB*`  | Batch COBOL program                            |
| `CS*`  | Common/shared copybook or utility              |
| `CV*`  | VSAM record layout copybook                    |
| `CC*`  | Communication/request/reply copybook           |
| `CI*`  | IMS segment layout copybook                    |
| `PA*`  | IMS DB load/unload programs & PCBs             |
| `*Y`   | Copybook (data definition) suffix convention   |

---

## 9. Batch Processing Cycle

The standard nightly batch cycle runs in this order:

```
CLOSEFIL → Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
         → POSTTRAN (transaction posting)
         → INTCALC (interest calculation)
         → TRANBKP (backup)
         → COMBTRAN (combine transactions)
         → CREASTMT (statement generation)
         → TRANIDX (rebuild indexes)
         → OPENFIL
```
