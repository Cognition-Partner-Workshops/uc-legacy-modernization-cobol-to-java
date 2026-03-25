# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL / BMS (3270)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs — Online (CICS)](#cobol-programs--online-cics)
3. [COBOL Programs — Batch](#cobol-programs--batch)
4. [Optional Modules](#optional-modules)
5. [Copybooks — Data Structures](#copybooks--data-structures)
6. [Copybooks — BMS-Generated](#copybooks--bms-generated)
7. [Copybooks — System/Utility](#copybooks--systemutility)
8. [BMS Screen Maps](#bms-screen-maps)
9. [JCL Batch Jobs](#jcl-batch-jobs)
10. [Assembler Programs](#assembler-programs)
11. [JCL Procedures](#jcl-procedures)
12. [Data Files](#data-files)
13. [Summary Statistics](#summary-statistics)

---

## Executive Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It simulates account management, card management, transactions, bill payments, and reporting across two user roles (Regular and Admin).

| Artifact Type             | Count |
|---------------------------|-------|
| COBOL Programs (core)     | 31    |
| COBOL Programs (optional) | 13    |
| Copybooks (data)          | 30    |
| Copybooks (BMS-generated) | 17    |
| BMS Screen Maps           | 17    |
| JCL Batch Jobs            | 38    |
| Assembler Programs        | 2     |
| JCL Procedures            | 2     |
| **Total Artifacts**       | **150** |

---

## COBOL Programs — Online (CICS)

These programs run under CICS and handle interactive 3270 terminal sessions.

| # | Program      | File            | Lines | CICS Txn | Function                                      | Domain           | Classification |
|---|-------------|-----------------|-------|----------|-----------------------------------------------|------------------|----------------|
| 1 | COSGN00C    | `app/cbl/COSGN00C.cbl` | 260   | CC00     | Sign-on / Authentication                       | Security         | Online CICS    |
| 2 | COMEN01C    | `app/cbl/COMEN01C.cbl` | 308   | CM00     | Main Menu — Regular Users                      | Navigation       | Online CICS    |
| 3 | COADM01C    | `app/cbl/COADM01C.cbl` | 288   | CA00     | Admin Menu                                     | Navigation       | Online CICS    |
| 4 | COACTVWC    | `app/cbl/COACTVWC.cbl` | 941   | —        | Account View                                   | Account Mgmt     | Online CICS    |
| 5 | COACTUPC    | `app/cbl/COACTUPC.cbl` | 4,236 | —        | Account Update                                 | Account Mgmt     | Online CICS    |
| 6 | COCRDLIC    | `app/cbl/COCRDLIC.cbl` | 1,459 | —        | Credit Card List                               | Card Mgmt        | Online CICS    |
| 7 | COCRDSLC    | `app/cbl/COCRDSLC.cbl` | 887   | —        | Credit Card Detail View                        | Card Mgmt        | Online CICS    |
| 8 | COCRDUPC    | `app/cbl/COCRDUPC.cbl` | 1,560 | —        | Credit Card Update                             | Card Mgmt        | Online CICS    |
| 9 | COTRN00C    | `app/cbl/COTRN00C.cbl` | 699   | CT00     | Transaction List                               | Transaction Mgmt | Online CICS    |
| 10| COTRN01C    | `app/cbl/COTRN01C.cbl` | 330   | —        | Transaction View                               | Transaction Mgmt | Online CICS    |
| 11| COTRN02C    | `app/cbl/COTRN02C.cbl` | 783   | —        | Transaction Add                                | Transaction Mgmt | Online CICS    |
| 12| CORPT00C    | `app/cbl/CORPT00C.cbl` | 649   | CR00     | Transaction Report — submits batch via TDQ     | Reporting        | Online CICS    |
| 13| COBIL00C    | `app/cbl/COBIL00C.cbl` | 572   | CB00     | Bill Payment                                   | Payments         | Online CICS    |
| 14| COUSR00C    | `app/cbl/COUSR00C.cbl` | 695   | CU00     | User List (Admin)                              | User Admin       | Online CICS    |
| 15| COUSR01C    | `app/cbl/COUSR01C.cbl` | 299   | —        | User Add (Admin)                               | User Admin       | Online CICS    |
| 16| COUSR02C    | `app/cbl/COUSR02C.cbl` | 414   | —        | User Update (Admin)                            | User Admin       | Online CICS    |
| 17| COUSR03C    | `app/cbl/COUSR03C.cbl` | 359   | —        | User Delete (Admin)                            | User Admin       | Online CICS    |

**Naming Convention:** `CO*` = Online CICS programs

---

## COBOL Programs — Batch

These programs run as batch jobs submitted via JCL.

| # | Program      | File            | Lines | Function                                             | Domain              | Classification |
|---|-------------|-----------------|-------|------------------------------------------------------|---------------------|----------------|
| 1 | CBACT01C    | `app/cbl/CBACT01C.cbl` | 430   | Read and display Account master file                 | Account Mgmt        | Batch          |
| 2 | CBACT02C    | `app/cbl/CBACT02C.cbl` | 178   | Read and display Card data file                      | Card Mgmt           | Batch          |
| 3 | CBACT03C    | `app/cbl/CBACT03C.cbl` | 178   | Read and display Card cross-reference file           | Card Mgmt           | Batch          |
| 4 | CBACT04C    | `app/cbl/CBACT04C.cbl` | 652   | Interest calculation on accounts                     | Financial Calc      | Batch          |
| 5 | CBCUS01C    | `app/cbl/CBCUS01C.cbl` | 178   | Read and display Customer master file                | Customer Mgmt       | Batch          |
| 6 | CBTRN01C    | `app/cbl/CBTRN01C.cbl` | 494   | Validate daily transactions                          | Transaction Proc    | Batch          |
| 7 | CBTRN02C    | `app/cbl/CBTRN02C.cbl` | 731   | Post transactions — core batch posting               | Transaction Proc    | Batch          |
| 8 | CBTRN03C    | `app/cbl/CBTRN03C.cbl` | 649   | Transaction detail report generation                 | Reporting           | Batch          |
| 9 | CBSTM03A    | `app/cbl/CBSTM03A.CBL` | 924   | Statement generation (text + HTML), calls CBSTM03B   | Reporting           | Batch          |
| 10| CBSTM03B    | `app/cbl/CBSTM03B.CBL` | 230   | Statement file I/O subroutine (called by CBSTM03A)  | Reporting           | Batch Sub      |
| 11| CBEXPORT    | `app/cbl/CBEXPORT.cbl` | 582   | Export VSAM data to sequential files                 | Data Migration      | Batch          |
| 12| CBIMPORT    | `app/cbl/CBIMPORT.cbl` | 487   | Import sequential files into VSAM                    | Data Migration      | Batch          |
| 13| COBSWAIT    | `app/cbl/COBSWAIT.cbl` | 41    | Wait/sleep utility                                   | Utility             | Batch          |

**Shared Utility:**

| # | Program      | File            | Lines | Function                                  | Classification |
|---|-------------|-----------------|-------|-------------------------------------------|----------------|
| 1 | CSUTLDTC    | `app/cbl/CSUTLDTC.cbl` | 157   | Date conversion utility (calls CEEDAYS)   | Utility        |

**Naming Convention:** `CB*` = Batch programs, `CS*` = Shared utility

---

## Optional Modules

### Authorization Module (IMS/DB2/MQ)

Located in `app/app-authorization-ims-db2-mq/cbl/`

| # | Program      | Lines | Function                                          | Technology      | Classification |
|---|-------------|-------|---------------------------------------------------|-----------------|----------------|
| 1 | COPAUA0C    | 1,026 | Card Authorization Decision (MQ trigger)          | CICS/IMS/MQ     | Online         |
| 2 | COPAUS0C    | 1,032 | Auth Message Summary View                         | CICS/IMS/BMS    | Online         |
| 3 | COPAUS1C    | 604   | Auth Message Detail View                          | CICS/IMS/BMS    | Online         |
| 4 | COPAUS2C    | 244   | Mark Authorization as Fraud (writes to DB2)       | CICS/IMS/DB2    | Online         |
| 5 | CBPAUP0C    | 386   | Purge Expired Pending Auth Messages               | Batch/IMS       | Batch          |
| 6 | DBUNLDGS    | 366   | IMS Database Unload — General Segments            | Batch/IMS       | Batch          |
| 7 | PAUDBLOD    | 369   | IMS Database Load — Auth data                     | Batch/IMS       | Batch          |
| 8 | PAUDBUNL    | 317   | IMS Database Unload — Auth data                   | Batch/IMS       | Batch          |

### Transaction Type DB2 Module

Located in `app/app-transaction-type-db2/cbl/`

| # | Program      | Lines | Function                                          | Technology      | Classification |
|---|-------------|-------|---------------------------------------------------|-----------------|----------------|
| 1 | COTRTLIC    | 2,098 | Transaction Type List (DB2 cursor paging)         | CICS/DB2        | Online         |
| 2 | COTRTUPC    | 1,702 | Transaction Type Add/Edit (DB2)                   | CICS/DB2        | Online         |
| 3 | COBTUPDT    | 237   | Batch Update Transaction Types (DB2)              | Batch/DB2       | Batch          |

### VSAM-MQ Module

Located in `app/app-vsam-mq/cbl/`

| # | Program      | Lines | Function                                          | Technology      | Classification |
|---|-------------|-------|---------------------------------------------------|-----------------|----------------|
| 1 | COACCT01    | 620   | MQ Request/Response — Account Inquiry             | CICS/VSAM/MQ    | Online         |
| 2 | CODATE01    | 524   | MQ Request/Response — System Date                 | CICS/MQ         | Online         |

---

## Copybooks — Data Structures

Located in `app/cpy/`

| # | Copybook     | File            | Record Description                            | Domain              | Record Size |
|---|-------------|-----------------|-----------------------------------------------|---------------------|-------------|
| 1 | CVACT01Y    | `app/cpy/CVACT01Y.cpy` | Account master record                         | Account Mgmt        | 300 bytes   |
| 2 | CVACT02Y    | `app/cpy/CVACT02Y.cpy` | Card data record                              | Card Mgmt           | 150 bytes   |
| 3 | CVACT03Y    | `app/cpy/CVACT03Y.cpy` | Card cross-reference record                   | Card Mgmt           | 50 bytes    |
| 4 | CVCRD01Y    | `app/cpy/CVCRD01Y.cpy` | Card detail internal structure                | Card Mgmt           | Variable    |
| 5 | CVCUS01Y    | `app/cpy/CVCUS01Y.cpy` | Customer master record                        | Customer Mgmt       | 500 bytes   |
| 6 | CVTRA01Y    | `app/cpy/CVTRA01Y.cpy` | Transaction category balance                  | Transaction Mgmt    | 50 bytes    |
| 7 | CVTRA02Y    | `app/cpy/CVTRA02Y.cpy` | Disclosure group record                       | Transaction Mgmt    | 50 bytes    |
| 8 | CVTRA03Y    | `app/cpy/CVTRA03Y.cpy` | Transaction type record                       | Transaction Mgmt    | 60 bytes    |
| 9 | CVTRA04Y    | `app/cpy/CVTRA04Y.cpy` | Transaction category type                     | Transaction Mgmt    | 60 bytes    |
| 10| CVTRA05Y    | `app/cpy/CVTRA05Y.cpy` | Transaction record (main)                     | Transaction Mgmt    | 350 bytes   |
| 11| CVTRA06Y    | `app/cpy/CVTRA06Y.cpy` | Daily transaction record                      | Transaction Mgmt    | 350 bytes   |
| 12| CVTRA07Y    | `app/cpy/CVTRA07Y.cpy` | Transaction report layout/headers             | Reporting           | Variable    |
| 13| CSUSR01Y    | `app/cpy/CSUSR01Y.cpy` | User security record                          | Security            | 80 bytes    |
| 14| COCOM01Y    | `app/cpy/COCOM01Y.cpy` | Common communication area (COMMAREA)          | Infrastructure      | Variable    |
| 15| COMEN02Y    | `app/cpy/COMEN02Y.cpy` | Menu option definitions                       | Navigation          | Variable    |
| 16| COADM02Y    | `app/cpy/COADM02Y.cpy` | Admin menu option definitions                 | Navigation          | Variable    |
| 17| COTTL01Y    | `app/cpy/COTTL01Y.cpy` | Screen title/header definitions               | UI Infrastructure   | Variable    |
| 18| CSDAT01Y    | `app/cpy/CSDAT01Y.cpy` | Date formatting work areas                    | Utility             | Variable    |
| 19| CSMSG01Y    | `app/cpy/CSMSG01Y.cpy` | Message area — primary                        | UI Infrastructure   | Variable    |
| 20| CSMSG02Y    | `app/cpy/CSMSG02Y.cpy` | Message area — secondary                      | UI Infrastructure   | Variable    |
| 21| CSSETATY    | `app/cpy/CSSETATY.cpy` | Set field attribute (REPLACING pattern)       | UI Infrastructure   | Variable    |
| 22| CSSTRPFY    | `app/cpy/CSSTRPFY.cpy` | String PERFORM/page formatting utility        | UI Infrastructure   | Variable    |
| 23| CSUTLDPY    | `app/cpy/CSUTLDPY.cpy` | Date utility — parameters                    | Utility             | Variable    |
| 24| CSUTLDWY    | `app/cpy/CSUTLDWY.cpy` | Date utility — working storage                | Utility             | Variable    |
| 25| CSLKPCDY    | `app/cpy/CSLKPCDY.cpy` | Lookup code definitions                       | Reference Data      | Variable    |
| 26| CODATECN    | `app/cpy/CODATECN.cpy` | Date conversion constants                    | Utility             | Variable    |
| 27| CVEXPORT    | `app/cpy/CVEXPORT.cpy` | Export record layout                          | Data Migration      | Variable    |
| 28| CUSTREC     | `app/cpy/CUSTREC.cpy`  | Customer record (statement generation layout) | Customer Mgmt       | Variable    |
| 29| COSTM01     | `app/cpy/COSTM01.CPY`  | Transaction altered layout for reporting      | Reporting           | 350 bytes   |
| 30| UNUSED1Y    | `app/cpy/UNUSED1Y.cpy` | Unused placeholder record                     | N/A                 | 80 bytes    |

**Naming Convention:** `CV*` = VSAM data structures, `CS*` = Shared/system, `CO*` = Online communication

---

## Copybooks — BMS-Generated

Located in `app/cpy-bms/`. These are auto-generated from BMS maps and define the symbolic map structures used by COBOL programs for 3270 screen I/O.

| # | Copybook     | Corresponding BMS Map | Screen Function          |
|---|-------------|----------------------|--------------------------|
| 1 | COACTUP.CPY | COACTUP.bms          | Account Update           |
| 2 | COACTVW.CPY | COACTVW.bms          | Account View             |
| 3 | COADM01.CPY | COADM01.bms          | Admin Menu               |
| 4 | COBIL00.CPY | COBIL00.bms          | Bill Payment             |
| 5 | COCRDLI.CPY | COCRDLI.bms          | Card List                |
| 6 | COCRDSL.CPY | COCRDSL.bms          | Card Detail View         |
| 7 | COCRDUP.CPY | COCRDUP.bms          | Card Update              |
| 8 | COMEN01.CPY | COMEN01.bms          | Main Menu                |
| 9 | CORPT00.CPY | CORPT00.bms          | Transaction Reports      |
| 10| COSGN00.CPY | COSGN00.bms          | Sign-on                  |
| 11| COTRN00.CPY | COTRN00.bms          | Transaction List         |
| 12| COTRN01.CPY | COTRN01.bms          | Transaction View         |
| 13| COTRN02.CPY | COTRN02.bms          | Transaction Add          |
| 14| COUSR00.CPY | COUSR00.bms          | User List                |
| 15| COUSR01.CPY | COUSR01.bms          | User Add                 |
| 16| COUSR02.CPY | COUSR02.bms          | User Update              |
| 17| COUSR03.CPY | COUSR03.bms          | User Delete              |

---

## BMS Screen Maps

Located in `app/bms/`. Each defines a 3270 terminal screen layout.

| # | BMS Map      | Screen Title                | Used By Program | Domain           | Key Input Fields                          |
|---|-------------|-----------------------------|-----------------|-----------------|--------------------------------------------|
| 1 | COSGN00.bms | Login Screen                | COSGN00C        | Security        | User ID, Password                          |
| 2 | COMEN01.bms | Main Menu                   | COMEN01C        | Navigation      | Menu Option Selection                      |
| 3 | COADM01.bms | Admin Menu                  | COADM01C        | Navigation      | Admin Option Selection                     |
| 4 | COACTVW.bms | Account View                | COACTVWC        | Account Mgmt    | Account ID (display only)                  |
| 5 | COACTUP.bms | Account Update              | COACTUPC        | Account Mgmt    | Account fields (editable)                  |
| 6 | COCRDLI.bms | Card List                   | COCRDLIC        | Card Mgmt       | Account ID filter, selection               |
| 7 | COCRDSL.bms | Card Detail View            | COCRDSLC        | Card Mgmt       | Card Number (display only)                 |
| 8 | COCRDUP.bms | Card Update                 | COCRDUPC        | Card Mgmt       | Card fields (editable)                     |
| 9 | COTRN00.bms | Transaction List            | COTRN00C        | Transaction     | Transaction ID filter, paging              |
| 10| COTRN01.bms | Transaction View            | COTRN01C        | Transaction     | Transaction ID (display only)              |
| 11| COTRN02.bms | Transaction Add             | COTRN02C        | Transaction     | Account, Card, Amount, etc.                |
| 12| CORPT00.bms | Transaction Reports         | CORPT00C        | Reporting       | Monthly/Yearly/Custom date range           |
| 13| COBIL00.bms | Bill Payment                | COBIL00C        | Payments        | Account, Amount                            |
| 14| COUSR00.bms | User List (Admin)           | COUSR00C        | User Admin      | User ID filter, selection                  |
| 15| COUSR01.bms | User Add (Admin)            | COUSR01C        | User Admin      | First/Last Name, User ID, Password, Type   |
| 16| COUSR02.bms | User Update (Admin)         | COUSR02C        | User Admin      | User fields (editable)                     |
| 17| COUSR03.bms | User Delete (Admin)         | COUSR03C        | User Admin      | User ID (confirm delete)                   |

---

## JCL Batch Jobs

Located in `app/jcl/`

### Data File Management

| # | Job          | File            | Function                                      | Executes Program | Classification     |
|---|-------------|-----------------|-----------------------------------------------|------------------|--------------------|
| 1 | ACCTFILE    | `app/jcl/ACCTFILE.jcl` | Define & load Account master VSAM KSDS       | IDCAMS           | VSAM Definition    |
| 2 | CARDFILE    | `app/jcl/CARDFILE.jcl` | Define & load Card data VSAM + alt index     | IDCAMS, SDSF     | VSAM Definition    |
| 3 | CUSTFILE    | `app/jcl/CUSTFILE.jcl` | Define & load Customer master VSAM KSDS      | IDCAMS, SDSF     | VSAM Definition    |
| 4 | TRANFILE    | `app/jcl/TRANFILE.jcl` | Define & load Transaction VSAM + alt index   | IDCAMS, SDSF     | VSAM Definition    |
| 5 | XREFFILE    | `app/jcl/XREFFILE.jcl` | Define & load Card cross-ref VSAM + alt index| IDCAMS           | VSAM Definition    |
| 6 | DUSRSECJ    | `app/jcl/DUSRSECJ.jcl` | Load User Security VSAM (KSDS + ESDS + RRDS)| IEBGENER, IDCAMS | VSAM Definition    |
| 7 | DEFCUST     | `app/jcl/DEFCUST.jcl`  | Define Customer VSAM cluster                 | IDCAMS           | VSAM Definition    |

### Batch Processing Cycle

| # | Job          | File            | Function                                      | Executes Program   | Cycle Order |
|---|-------------|-----------------|-----------------------------------------------|--------------------|-------------|
| 8 | CLOSEFIL    | `app/jcl/CLOSEFIL.jcl` | Close CICS files for batch                   | SDSF               | 1           |
| 9 | POSTTRAN    | `app/jcl/POSTTRAN.jcl` | Post daily transactions                      | **CBTRN02C**       | 2           |
| 10| INTCALC     | `app/jcl/INTCALC.jcl`  | Calculate interest on accounts               | **CBACT04C**       | 3           |
| 11| TRANBKP     | `app/jcl/TRANBKP.jcl`  | Backup transaction file                      | IDCAMS             | 4           |
| 12| COMBTRAN    | `app/jcl/COMBTRAN.jcl`  | Combine/merge daily transactions             | SORT, IDCAMS       | 5           |
| 13| CREASTMT    | `app/jcl/CREASTMT.JCL`  | Generate account statements (text + HTML)    | SORT, **CBSTM03A** | 6           |
| 14| TRANIDX     | `app/jcl/TRANIDX.jcl`  | Rebuild transaction alternate indexes        | IDCAMS             | 7           |
| 15| OPENFIL     | `app/jcl/OPENFIL.jcl`  | Reopen CICS files after batch                | SDSF               | 8           |

### Reporting & Utilities

| # | Job          | File            | Function                                      | Executes Program   | Classification |
|---|-------------|-----------------|-----------------------------------------------|--------------------|----------------|
| 16| TRANREPT    | `app/jcl/TRANREPT.jcl` | Daily transaction report                     | SORT, **CBTRN03C** | Reporting      |
| 17| PRTCATBL    | `app/jcl/PRTCATBL.jcl` | Print category balance report                | SORT               | Reporting      |
| 18| REPTFILE    | `app/jcl/REPTFILE.jcl` | Define report output file                    | IDCAMS             | Reporting      |
| 19| TXT2PDF1    | `app/jcl/TXT2PDF1.JCL` | Convert text statements to PDF               | IKJEFT1B (TSO)     | Utility        |

### Data Read/Verify

| # | Job          | File            | Function                                      | Executes Program   | Classification |
|---|-------------|-----------------|-----------------------------------------------|--------------------|----------------|
| 20| READACCT    | `app/jcl/READACCT.jcl` | Read & display Account file                  | **CBACT01C**       | Verification   |
| 21| READCARD    | `app/jcl/READCARD.jcl` | Read & display Card file                     | **CBACT02C**       | Verification   |
| 22| READCUST    | `app/jcl/READCUST.jcl` | Read & display Customer file                 | **CBCUS01C**       | Verification   |
| 23| READXREF    | `app/jcl/READXREF.jcl` | Read & display Cross-reference file          | **CBACT03C**       | Verification   |

### Export/Import

| # | Job          | File            | Function                                      | Executes Program   | Classification |
|---|-------------|-----------------|-----------------------------------------------|--------------------|----------------|
| 24| CBEXPORT    | `app/jcl/CBEXPORT.jcl` | Export all VSAM data to sequential           | **CBEXPORT**       | Data Migration |
| 25| CBIMPORT    | `app/jcl/CBIMPORT.jcl` | Import sequential data into VSAM             | **CBIMPORT**       | Data Migration |

### Reference Data & GDG

| # | Job          | File            | Function                                      | Executes Program | Classification     |
|---|-------------|-----------------|-----------------------------------------------|------------------|--------------------|
| 26| DISCGRP     | `app/jcl/DISCGRP.jcl`  | Define Disclosure Group VSAM KSDS            | IDCAMS           | VSAM Definition    |
| 27| TRANCATG    | `app/jcl/TRANCATG.jcl` | Define Transaction Category VSAM KSDS        | IDCAMS           | VSAM Definition    |
| 28| TRANTYPE    | `app/jcl/TRANTYPE.jcl` | Define Transaction Type VSAM KSDS            | IDCAMS           | VSAM Definition    |
| 29| TCATBALF    | `app/jcl/TCATBALF.jcl` | Define Transaction Category Balance VSAM     | IDCAMS           | VSAM Definition    |
| 30| DALYREJS    | `app/jcl/DALYREJS.jcl` | Define Daily Rejects file                    | IDCAMS           | VSAM Definition    |
| 31| DEFGDGB     | `app/jcl/DEFGDGB.jcl`  | Define GDG base entries                      | IDCAMS           | GDG Management     |
| 32| DEFGDGD     | `app/jcl/DEFGDGD.jcl`  | Define GDG base + model DSCBs                | IDCAMS, IEBGENER | GDG Management     |
| 33| ESDSRRDS    | `app/jcl/ESDSRRDS.jcl` | Define ESDS/RRDS VSAM clusters (demo)        | IEBGENER, IDCAMS | VSAM Definition    |

### Infrastructure & Admin

| # | Job          | File            | Function                                      | Executes Program | Classification     |
|---|-------------|-----------------|-----------------------------------------------|------------------|--------------------|
| 34| CBADMCDJ    | `app/jcl/CBADMCDJ.jcl` | Load CICS CSD resource definitions           | DFHCSDUP         | CICS Admin         |
| 35| FTPJCL      | `app/jcl/FTPJCL.JCL`   | FTP file transfer to/from mainframe          | FTP              | File Transfer      |
| 36| INTRDRJ1    | `app/jcl/INTRDRJ1.JCL`  | Internal reader — trigger INTRDRJ2           | IDCAMS, IEBGENER | Job Scheduling     |
| 37| INTRDRJ2    | `app/jcl/INTRDRJ2.JCL`  | Internal reader — triggered by INTRDRJ1      | IDCAMS           | Job Scheduling     |
| 38| WAITSTEP    | `app/jcl/WAITSTEP.jcl` | Wait/delay step (calls COBSWAIT)             | **COBSWAIT**     | Utility            |

---

## Assembler Programs

Located in `app/asm/`

| # | Program      | Function                                    |
|---|-------------|---------------------------------------------|
| 1 | MVSWAIT.asm | MVS Wait — system-level wait utility        |
| 2 | COBDATFT.asm| COBOL date formatting — assembler helper    |

---

## JCL Procedures

Located in `app/proc/`

| # | Procedure    | Function                                    |
|---|-------------|---------------------------------------------|
| 1 | REPROC.prc  | Report processing procedure                 |
| 2 | TRANREPT.prc| Transaction report procedure                |

---

## Data Files

Located in `app/data/ASCII/`

| # | File             | Description                         | Related VSAM Dataset                         |
|---|-----------------|-------------------------------------|----------------------------------------------|
| 1 | acctdata.txt    | Account master records              | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS          |
| 2 | carddata.txt    | Card data records                   | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS          |
| 3 | cardxref.txt    | Card cross-reference records        | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS          |
| 4 | custdata.txt    | Customer records                    | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS          |
| 5 | dailytran.txt   | Daily transaction records           | AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS          |
| 6 | discgrp.txt     | Disclosure group records            | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS           |
| 7 | tcatbal.txt     | Transaction category balances       | AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS           |
| 8 | trancatg.txt    | Transaction category definitions    | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS          |
| 9 | trantype.txt    | Transaction type definitions        | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS          |

---

## Summary Statistics

| Metric                          | Value    |
|---------------------------------|----------|
| Total COBOL Programs (core)     | 31       |
| Total COBOL Programs (optional) | 13       |
| Total COBOL Lines of Code       | ~30,000  |
| Online CICS Programs            | 17 core + 7 optional |
| Batch Programs                  | 14 core + 6 optional |
| Copybooks (data structures)     | 30       |
| BMS Screen Maps                 | 17       |
| JCL Jobs                        | 38       |
| VSAM Datasets Referenced        | 12+      |
| Business Domains Covered        | 7 (Security, Account, Card, Transaction, Reporting, Payments, User Admin) |
