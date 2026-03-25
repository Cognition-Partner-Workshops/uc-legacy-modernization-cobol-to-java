# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Platform:** IBM z/OS, CICS, VSAM, JCL Batch
>
> This document catalogs every artifact in the CardDemo mainframe credit card management application, organized by type and functional area.

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [COBOL Programs (Optional Modules)](#cobol-programs-optional-modules)
4. [Copybooks (Core)](#copybooks-core)
5. [Copybooks (Optional Modules)](#copybooks-optional-modules)
6. [BMS Screen Maps (Core)](#bms-screen-maps-core)
7. [BMS Screen Maps (Optional Modules)](#bms-screen-maps-optional-modules)
8. [BMS-Generated Copybooks](#bms-generated-copybooks)
9. [JCL Batch Jobs (Core)](#jcl-batch-jobs-core)
10. [JCL Batch Jobs (Optional Modules)](#jcl-batch-jobs-optional-modules)
11. [Assembler Programs](#assembler-programs)
12. [JCL Procedures](#jcl-procedures)
13. [Supporting Artifacts](#supporting-artifacts)
14. [Naming Conventions](#naming-conventions)

---

## Summary Statistics

| Artifact Type               | Core | Optional Modules | Total |
|-----------------------------|------|-------------------|-------|
| COBOL Programs              | 31   | 13                | 44    |
| Copybooks (Data)            | 30   | 11                | 41    |
| BMS Screen Maps             | 17   | 4                 | 21    |
| BMS-Generated Copybooks     | 17   | 4                 | 21    |
| JCL Batch Jobs              | 38   | 8                 | 46    |
| Assembler Programs          | 2    | 0                 | 2     |
| JCL Procedures              | 2    | 0                 | 2     |
| Scheduler Configs           | 2    | 0                 | 2     |
| **Total Artifacts**         | **139** | **40**         | **179** |

---

## COBOL Programs (Core)

### Online CICS Programs (16 programs)

| # | Program    | Lines | CICS Tran | Function                          | Domain           | Classification       |
|---|------------|-------|-----------|-----------------------------------|------------------|----------------------|
| 1 | COSGN00C   | 261   | CC00      | User sign-on / authentication     | Security         | Online - Auth        |
| 2 | COMEN01C   | 309   | CM00      | Main menu for regular users       | Navigation       | Online - Menu        |
| 3 | COADM01C   | 288   | CA00      | Admin menu                        | Navigation       | Online - Menu        |
| 4 | COACTVWC   | 942   | CAVW      | Account view (read-only)          | Account Mgmt     | Online - Inquiry     |
| 5 | COACTUPC   | 4,237 | CAUP      | Account update (full CRUD)        | Account Mgmt     | Online - Update      |
| 6 | COCRDLIC   | 1,460 | CCLI      | Credit card list / browse         | Card Mgmt        | Online - List        |
| 7 | COCRDSLC   | 888   | CCDL      | Credit card detail view           | Card Mgmt        | Online - Inquiry     |
| 8 | COCRDUPC   | 1,560 | CCUP      | Credit card update                | Card Mgmt        | Online - Update      |
| 9 | COTRN00C   | 699   | CT00      | Transaction list / browse         | Transaction Mgmt | Online - List        |
| 10| COTRN01C   | 330   | CT01      | Transaction detail view           | Transaction Mgmt | Online - Inquiry     |
| 11| COTRN02C   | 783   | CT02      | Transaction add (new entry)       | Transaction Mgmt | Online - Update      |
| 12| CORPT00C   | 649   | CR00      | Transaction report request        | Reporting        | Online - Report      |
| 13| COBIL00C   | 572   | CB00      | Bill payment processing           | Payments         | Online - Update      |
| 14| COUSR00C   | 695   | CU00      | User list (admin security)        | User Admin       | Online - List        |
| 15| COUSR01C   | 299   | CU01      | User add (admin security)         | User Admin       | Online - Update      |
| 16| COUSR02C   | 414   | CU02      | User update (admin security)      | User Admin       | Online - Update      |

### Online CICS Programs - Admin (1 program)

| # | Program    | Lines | CICS Tran | Function                          | Domain           | Classification       |
|---|------------|-------|-----------|-----------------------------------|------------------|----------------------|
| 17| COUSR03C   | 359   | CU03      | User delete (admin security)      | User Admin       | Online - Delete      |

### Batch Programs (13 programs)

| # | Program    | Lines | Function                              | Domain                | Classification     |
|---|------------|-------|---------------------------------------|-----------------------|--------------------|
| 18| CBACT01C   | 430   | Account file refresh / date format    | Account Mgmt          | Batch - Data Load  |
| 19| CBACT02C   | 178   | Card data file processing             | Card Mgmt             | Batch - Data Load  |
| 20| CBACT03C   | 178   | Card cross-reference file processing  | Card Mgmt             | Batch - Data Load  |
| 21| CBACT04C   | 652   | Interest calculation                  | Financial Processing  | Batch - Calc       |
| 22| CBCUS01C   | 178   | Customer data file processing         | Customer Mgmt         | Batch - Data Load  |
| 23| CBTRN01C   | 494   | Transaction file validation           | Transaction Mgmt      | Batch - Validation |
| 24| CBTRN02C   | 731   | Transaction posting (core)            | Transaction Mgmt      | Batch - Posting    |
| 25| CBTRN03C   | 649   | Daily transaction report generation   | Reporting             | Batch - Report     |
| 26| CBSTM03A   | 924   | Statement generation (main driver)    | Reporting             | Batch - Report     |
| 27| CBSTM03B   | 230   | Statement generation (sub-module)     | Reporting             | Batch - Report     |
| 28| CBEXPORT   | 582   | Multi-entity data export              | Data Migration        | Batch - Export     |
| 29| CBIMPORT   | 487   | Data import from external source      | Data Migration        | Batch - Import     |
| 30| COBSWAIT   | 41    | Wait utility (calls MVSWAIT ASM)      | Utility               | Batch - Utility    |

### Utility Programs (1 program)

| # | Program    | Lines | Function                              | Domain           | Classification       |
|---|------------|-------|---------------------------------------|------------------|----------------------|
| 31| CSUTLDTC   | 157   | Date validation utility (calls CEEDAYS)| Utility          | Shared - Utility     |

---

## COBOL Programs (Optional Modules)

### Authorization Module (IMS/DB2/MQ) - 8 programs

| # | Program    | Lines | Function                                    | Domain              | Classification         |
|---|------------|-------|---------------------------------------------|---------------------|------------------------|
| 1 | COPAUA0C   | -     | MQ trigger for authorization requests       | Authorization       | Online - MQ Trigger    |
| 2 | COPAUS0C   | -     | Pending authorization summary view          | Authorization       | Online - Inquiry       |
| 3 | COPAUS1C   | -     | Pending authorization detail view           | Authorization       | Online - Inquiry       |
| 4 | COPAUS2C   | -     | Fraud marking (writes to DB2)               | Authorization       | Online - Update        |
| 5 | CBPAUP0C   | -     | Batch purge of processed authorizations     | Authorization       | Batch - Purge          |
| 6 | DBUNLDGS   | -     | IMS DB unload to GSAM                       | Authorization       | Batch - Utility        |
| 7 | PAUDBLOD   | -     | IMS DB reload / load                        | Authorization       | Batch - Data Load      |
| 8 | PAUDBUNL   | -     | IMS DB unload                               | Authorization       | Batch - Utility        |

### Transaction Type DB2 Module - 3 programs

| # | Program    | Lines | Function                                    | Domain              | Classification         |
|---|------------|-------|---------------------------------------------|---------------------|------------------------|
| 9 | COTRTLIC   | -     | Transaction type list/delete (DB2 cursors)  | Reference Data      | Online - List          |
| 10| COTRTUPC   | -     | Transaction type add/update (DB2)           | Reference Data      | Online - Update        |
| 11| COBTUPDT   | -     | Batch transaction type update (DB2)         | Reference Data      | Batch - Update         |

### VSAM-MQ Module - 2 programs

| # | Program    | Lines | Function                                    | Domain              | Classification         |
|---|------------|-------|---------------------------------------------|---------------------|------------------------|
| 12| COACCT01   | -     | MQ-based account inquiry (VSAM read)        | Account Mgmt        | Online - MQ Service    |
| 13| CODATE01   | -     | MQ-based system date service                | Utility             | Online - MQ Service    |

---

## Copybooks (Core)

### Application Communication (3 copybooks)

| # | Copybook   | Lines | Record Len | Purpose                                     | Used By               |
|---|------------|-------|------------|---------------------------------------------|-----------------------|
| 1 | COCOM01Y   | 48    | ~145 bytes | COMMAREA - inter-program communication      | All online programs   |
| 2 | COMEN02Y   | 102   | Variable   | Main menu option definitions (11 options)   | COMEN01C              |
| 3 | COADM02Y   | 63    | Variable   | Admin menu option definitions (6 options)   | COADM01C              |

### Business Entity Records (8 copybooks)

| # | Copybook   | Lines | Record Len | Entity                                      | VSAM File             |
|---|------------|-------|------------|---------------------------------------------|-----------------------|
| 4 | CVACT01Y   | 17    | 300 bytes  | Account master record                       | ACCTDAT               |
| 5 | CVACT02Y   | 11    | 150 bytes  | Card (credit card) record                   | CARDDAT               |
| 6 | CVACT03Y   | 8     | 50 bytes   | Card-to-Account cross-reference             | CARDXREF              |
| 7 | CVCUS01Y   | 27    | 500 bytes  | Customer master record                      | CUSTDAT               |
| 8 | CUSTREC    | 23    | 500 bytes  | Customer record (alternate layout)          | CUSTDAT (batch)       |
| 9 | CVTRA05Y   | 22    | 350 bytes  | Transaction record (online)                 | TRANSACT              |
| 10| CVTRA06Y   | 22    | 350 bytes  | Daily transaction record                    | DALYTRAN              |
| 11| CSUSR01Y   | 23    | 80 bytes   | User security record                        | USRSEC                |

### Transaction Reference Data (4 copybooks)

| # | Copybook   | Lines | Record Len | Entity                                      | VSAM File             |
|---|------------|-------|------------|---------------------------------------------|-----------------------|
| 12| CVTRA01Y   | 14    | 50 bytes   | Transaction category balance                | TCATBALF              |
| 13| CVTRA02Y   | 14    | 50 bytes   | Disclosure group / interest rate            | DISCGRP               |
| 14| CVTRA03Y   | 11    | 60 bytes   | Transaction type definition                 | TRANTYPE              |
| 15| CVTRA04Y   | 13    | 60 bytes   | Transaction category definition             | TRANCATG              |

### Reporting & Export (3 copybooks)

| # | Copybook   | Lines | Record Len | Purpose                                     | Used By               |
|---|------------|-------|------------|---------------------------------------------|-----------------------|
| 16| CVTRA07Y   | 74    | Variable   | Transaction report headers/detail/totals    | CBTRN03C              |
| 17| COSTM01    | -     | Variable   | Statement generation data structures        | CBSTM03A              |
| 18| CVEXPORT   | 104   | 500 bytes  | Multi-entity export record (REDEFINES)      | CBEXPORT              |

### UI and Screen Support (5 copybooks)

| # | Copybook   | Lines | Purpose                                     | Used By                        |
|---|------------|-------|---------------------------------------------|--------------------------------|
| 19| COTTL01Y   | -     | Screen title / header definitions           | All online programs            |
| 20| CSDAT01Y   | -     | Current date/time formatting                | All online programs            |
| 21| CSMSG01Y   | -     | Common user messages                        | All online programs            |
| 22| CSMSG02Y   | -     | Abend handling variables                    | Most online programs           |
| 23| CVCRD01Y   | 42    | Credit card work area                       | COCRDLIC, COCRDSLC, COCRDUPC   |

### Utility and Validation (7 copybooks)

| # | Copybook   | Lines | Purpose                                     | Used By                        |
|---|------------|-------|---------------------------------------------|--------------------------------|
| 24| CSUTLDWY   | 84    | Date edit/validation working storage        | COACTUPC                       |
| 25| CSUTLDPY   | -     | Date utility parameters                     | COACTUPC                       |
| 26| CSSETATY   | -     | Screen field attribute setting              | COACTUPC (used 40+ times)      |
| 27| CSSTRPFY   | -     | String strip function                       | COACTUPC, COCRDUPC             |
| 28| CSLKPCDY   | 1314  | US area code + state/ZIP code lookup        | COACTUPC                       |
| 29| CODATECN   | -     | Date conversion record                      | CBACT01C                       |
| 30| UNUSED1Y   | -     | Placeholder (unused)                        | None                           |

---

## Copybooks (Optional Modules)

### Authorization Module (IMS/DB2/MQ) - 9 copybooks

| # | Copybook   | Purpose                                         |
|---|------------|--------------------------------------------------|
| 1 | CCPAUERY   | Authorization error response layout              |
| 2 | CCPAURLY   | Authorization reply layout                       |
| 3 | CCPAURQY   | Authorization request layout                     |
| 4 | CIPAUDTY   | Pending authorization detail layout              |
| 5 | CIPAUSMY   | Pending authorization summary layout             |
| 6 | IMSFUNCS   | IMS function code definitions                    |
| 7 | PADFLPCB   | IMS PCB for detail segment                       |
| 8 | PASFLPCB   | IMS PCB for summary segment                      |
| 9 | PAUTBPCB   | IMS PCB for authorization table                  |

### Transaction Type DB2 Module - 2 copybooks

| # | Copybook   | Purpose                                         |
|---|------------|--------------------------------------------------|
| 10| CSDB2RPY   | DB2 read parameter area                          |
| 11| CSDB2RWY   | DB2 read/write parameter area                    |

---

## BMS Screen Maps (Core)

| # | Map Name  | Mapset   | Associated Program | Screen Function                      |
|---|-----------|----------|--------------------|--------------------------------------|
| 1 | COSGN00   | COSGN00  | COSGN00C           | Sign-on screen                       |
| 2 | COMEN01   | COMEN01  | COMEN01C           | Main menu                            |
| 3 | COADM01   | COADM01  | COADM01C           | Admin menu                           |
| 4 | COACTVW   | COACTVW  | COACTVWC           | Account view                         |
| 5 | COACTUP   | COACTUP  | COACTUPC           | Account update                       |
| 6 | COCRDLI   | COCRDLI  | COCRDLIC           | Credit card list                     |
| 7 | COCRDSL   | COCRDSL  | COCRDSLC           | Credit card detail view              |
| 8 | COCRDUP   | COCRDUP  | COCRDUPC           | Credit card update                   |
| 9 | COTRN00   | COTRN00  | COTRN00C           | Transaction list                     |
| 10| COTRN01   | COTRN01  | COTRN01C           | Transaction detail view              |
| 11| COTRN02   | COTRN02  | COTRN02C           | Transaction add                      |
| 12| CORPT00   | CORPT00  | CORPT00C           | Transaction reports                  |
| 13| COBIL00   | COBIL00  | COBIL00C           | Bill payment                         |
| 14| COUSR00   | COUSR00  | COUSR00C           | User list                            |
| 15| COUSR01   | COUSR01  | COUSR01C           | User add                             |
| 16| COUSR02   | COUSR02  | COUSR02C           | User update                          |
| 17| COUSR03   | COUSR03  | COUSR03C           | User delete                          |

---

## BMS Screen Maps (Optional Modules)

### Authorization Module

| # | Map Name  | Associated Program | Screen Function                      |
|---|-----------|---------------------|--------------------------------------|
| 1 | COPAU00   | COPAUS0C            | Pending authorization summary        |
| 2 | COPAU01   | COPAUS1C            | Pending authorization detail         |

### Transaction Type DB2 Module

| # | Map Name  | Associated Program | Screen Function                      |
|---|-----------|---------------------|--------------------------------------|
| 3 | COTRTLI   | COTRTLIC            | Transaction type list                |
| 4 | COTRTUP   | COTRTUPC            | Transaction type maintenance         |

---

## BMS-Generated Copybooks

Each BMS map generates a corresponding copybook in `app/cpy-bms/` containing the symbolic field definitions (input/output maps) used by the COBOL programs to SEND/RECEIVE data:

| Generated Copybook | Source BMS Map | Contains                                |
|--------------------|----------------|-----------------------------------------|
| COSGN00.CPY        | COSGN00.bms    | COSGN0AI (input), COSGN0AO (output)    |
| COMEN01.CPY        | COMEN01.bms    | COMEN1AI (input), COMEN1AO (output)    |
| COADM01.CPY        | COADM01.bms    | COADM1AI (input), COADM1AO (output)    |
| COACTVW.CPY        | COACTVW.bms    | CACTVWAI (input), CACTVWAO (output)    |
| COACTUP.CPY        | COACTUP.bms    | CACTUPAI (input), CACTUPAO (output)    |
| COCRDLI.CPY        | COCRDLI.bms    | CCRDLIAI (input), CCRDLIAO (output)    |
| COCRDSL.CPY        | COCRDSL.bms    | CCRDSLAI (input), CCRDSLAO (output)    |
| COCRDUP.CPY        | COCRDUP.bms    | CCRDUPAI (input), CCRDUPAO (output)    |
| COTRN00.CPY        | COTRN00.bms    | COTRN0AI (input), COTRN0AO (output)    |
| COTRN01.CPY        | COTRN01.bms    | COTRN1AI (input), COTRN1AO (output)    |
| COTRN02.CPY        | COTRN02.bms    | COTRN2AI (input), COTRN2AO (output)    |
| CORPT00.CPY        | CORPT00.bms    | CORPT0AI (input), CORPT0AO (output)    |
| COBIL00.CPY        | COBIL00.bms    | COBIL0AI (input), COBIL0AO (output)    |
| COUSR00.CPY        | COUSR00.bms    | COUSR0AI (input), COUSR0AO (output)    |
| COUSR01.CPY        | COUSR01.bms    | COUSR1AI (input), COUSR1AO (output)    |
| COUSR02.CPY        | COUSR02.bms    | COUSR2AI (input), COUSR2AO (output)    |
| COUSR03.CPY        | COUSR03.bms    | COUSR3AI (input), COUSR3AO (output)    |

---

## JCL Batch Jobs (Core)

### Data Loading & Refresh (8 jobs)

| # | Job        | Executes          | Function                                        | Cycle Position |
|---|------------|-------------------|-------------------------------------------------|----------------|
| 1 | ACCTFILE   | IDCAMS REPRO      | Refresh account master VSAM from sequential     | Data Refresh   |
| 2 | CARDFILE   | IDCAMS REPRO      | Refresh card master VSAM from sequential        | Data Refresh   |
| 3 | CUSTFILE   | IDCAMS REPRO      | Refresh customer master VSAM from sequential    | Data Refresh   |
| 4 | XREFFILE   | IDCAMS REPRO      | Load card cross-reference VSAM                  | Data Refresh   |
| 5 | TRANFILE   | IDCAMS REPRO      | Load transaction master VSAM                    | Data Refresh   |
| 6 | DUSRSECJ   | IDCAMS REPRO      | Load user security VSAM                         | Data Refresh   |
| 7 | TCATBALF   | IDCAMS REPRO      | Load transaction category balance VSAM          | Data Refresh   |
| 8 | TRANCATG   | IDCAMS REPRO/DEF  | Define and load transaction category VSAM       | Data Refresh   |

### Core Batch Processing (6 jobs)

| # | Job        | Executes          | Function                                        | Cycle Position |
|---|------------|-------------------|-------------------------------------------------|----------------|
| 9 | POSTTRAN   | CBTRN02C          | Post daily transactions to master               | Step 3         |
| 10| INTCALC    | CBACT04C          | Calculate interest on accounts                  | Step 4         |
| 11| TRANBKP    | SORT + IDCAMS     | Backup transaction master to GDG                | Step 5         |
| 12| COMBTRAN   | SORT + IDCAMS     | Combine backup + new transactions               | Step 6         |
| 13| CREASTMT   | CBSTM03A + SORT   | Generate customer statements (text + HTML)      | Step 7         |
| 14| TRANREPT   | CBTRN03C + REPROC | Generate daily transaction report               | Step 8         |

### VSAM File Management (7 jobs)

| # | Job        | Executes          | Function                                        |
|---|------------|-------------------|-------------------------------------------------|
| 15| CLOSEFIL   | IDCAMS            | Close CICS VSAM files for batch access          |
| 16| OPENFIL    | IDCAMS            | Reopen CICS VSAM files after batch              |
| 17| DEFGDGB    | IDCAMS            | Define GDG base for backups                     |
| 18| DEFGDGD    | IDCAMS            | Define GDG base for daily transactions          |
| 19| TRANIDX    | IDCAMS            | Define alternate index for transaction VSAM     |
| 20| DEFCUST    | IDCAMS            | Define customer VSAM cluster                    |
| 21| ESDSRRDS   | IDCAMS            | Define ESDS/RRDS datasets                       |

### Reporting & Read Jobs (8 jobs)

| # | Job        | Executes          | Function                                        |
|---|------------|-------------------|-------------------------------------------------|
| 22| READACCT   | IDCAMS PRINT      | Print/read account data                         |
| 23| READCARD   | IDCAMS PRINT      | Print/read card data                            |
| 24| READCUST   | IDCAMS PRINT      | Print/read customer data                        |
| 25| READXREF   | IDCAMS PRINT      | Print/read cross-reference data                 |
| 26| REPTFILE   | IDCAMS PRINT      | Print report files                              |
| 27| PRTCATBL   | IDCAMS PRINT      | Print transaction category balance data         |
| 28| DALYREJS   | IDCAMS DEFINE     | Define daily rejects file                       |
| 29| TRANTYPE   | IDCAMS REPRO/DEF  | Define and load transaction type VSAM           |

### Export/Import & Utility Jobs (6 jobs)

| # | Job        | Executes          | Function                                        |
|---|------------|-------------------|-------------------------------------------------|
| 30| CBEXPORT   | CBEXPORT (COBOL)  | Export all entities to sequential file           |
| 31| CBIMPORT   | CBIMPORT (COBOL)  | Import data from sequential file                 |
| 32| DISCGRP    | IDCAMS REPRO/DEF  | Define and load disclosure group VSAM            |
| 33| TXT2PDF1   | IKJEFT1B          | Convert statement text to PDF                    |
| 34| WAITSTEP   | COBSWAIT          | Wait utility step for job scheduling             |
| 35| FTPJCL     | FTP               | FTP-based file transfer job                      |

### Internal Reader / Misc (3 jobs)

| # | Job        | Executes          | Function                                        |
|---|------------|-------------------|-------------------------------------------------|
| 36| INTRDRJ1   | Internal Reader   | Submit jobs via internal reader (method 1)       |
| 37| INTRDRJ2   | Internal Reader   | Submit jobs via internal reader (method 2)       |
| 38| CBADMCDJ   | IDCAMS DEFINE     | Admin card-related VSAM definition               |

---

## JCL Batch Jobs (Optional Modules)

### Authorization Module (5 jobs)

| # | Job        | Function                                              |
|---|------------|-------------------------------------------------------|
| 1 | CBPAUP0J   | Purge processed pending authorizations                |
| 2 | DBPAUTP0   | DB2 authorization table maintenance                   |
| 3 | LOADPADB   | Load IMS pending authorization database               |
| 4 | UNLDGSAM   | Unload IMS DB to GSAM sequential                      |
| 5 | UNLDPADB   | Unload pending authorization database                 |

### Transaction Type DB2 Module (3 jobs)

| # | Job        | Function                                              |
|---|------------|-------------------------------------------------------|
| 6 | CREADB21   | Create DB2 tables for transaction types               |
| 7 | MNTTRDB2   | Maintain DB2 transaction type data                    |
| 8 | TRANEXTR   | Extract transaction type data from DB2                |

---

## Assembler Programs

| # | Program    | Location  | Function                                          |
|---|------------|-----------|---------------------------------------------------|
| 1 | MVSWAIT    | app/asm/  | MVS wait service (called by COBSWAIT)             |
| 2 | COBDATFT   | app/asm/  | Date formatting utility (called by CBACT01C)      |

---

## JCL Procedures

| # | Procedure  | Location   | Function                                         |
|---|------------|------------|--------------------------------------------------|
| 1 | REPROC     | app/proc/  | IDCAMS REPRO procedure (reusable copy utility)   |
| 2 | TRANREPT   | app/proc/  | Transaction report procedure                     |

---

## Supporting Artifacts

### Scheduler Configurations

| File               | Type      | Purpose                                        |
|--------------------|-----------|------------------------------------------------|
| CardDemo.ca7       | CA-7      | CA-7 job scheduler definitions                 |
| CardDemo.controlm  | Control-M | Control-M job scheduler definitions            |

### CICS Resource Definitions

| File               | Location   | Purpose                                       |
|--------------------|------------|-----------------------------------------------|
| CARDDEMO.CSD       | app/csd/   | CICS System Definition - program/transaction  |

### Control Files

| File               | Location   | Purpose                                       |
|--------------------|------------|-----------------------------------------------|
| REPROCT.ctl        | app/ctl/   | REPRO control template                        |

### Data Files

| Directory          | Format   | Contents                                       |
|--------------------|----------|------------------------------------------------|
| app/data/ASCII/    | ASCII    | 9 sample data files for testing                |
| app/data/EBCDIC/   | EBCDIC   | Same data in mainframe-uploadable format       |

**ASCII Data Files:** `acctdata.txt`, `carddata.txt`, `cardxref.txt`, `custdata.txt`, `dailytran.txt`, `discgrp.txt`, `tcatbal.txt`, `trancatg.txt`, `trantype.txt`

---

## Naming Conventions

| Prefix   | Meaning                                          | Examples                    |
|----------|--------------------------------------------------|-----------------------------|
| `CO`     | Online CICS program                              | COSGN00C, COMEN01C          |
| `CB`     | Batch COBOL program                              | CBTRN02C, CBACT04C          |
| `CS`     | Shared/common/system copybook or utility         | CSUTLDTC, CSUSR01Y          |
| `CV`     | VSAM record layout copybook                      | CVACT01Y, CVTRA05Y          |
| `CC`     | Communication/card-related copybook              | COCOM01Y, CVCRD01Y          |
| `Y` suffix | Copybook indicator                             | COCOM01Y, CVACT01Y          |
| `C` suffix | COBOL program indicator                        | COSGN00C, CBTRN02C          |
| `00`-`03`  | Sequence number within a function              | COUSR00C through COUSR03C   |

### CICS Transaction ID Pattern

| Prefix | Domain             | Example |
|--------|--------------------|---------|
| CC     | Card Demo (main)   | CC00    |
| CM     | Menu               | CM00    |
| CA     | Account / Admin    | CAVW    |
| CC     | Credit Card        | CCLI    |
| CT     | Transaction        | CT00    |
| CR     | Report             | CR00    |
| CB     | Bill Payment       | CB00    |
| CU     | User Security      | CU00    |
