# APPLICATION INVENTORY — CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Technology Stack:** COBOL / CICS / VSAM / JCL / BMS (3270)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs — Core (31)](#cobol-programs--core-31)
3. [COBOL Programs — Optional Modules (13)](#cobol-programs--optional-modules-13)
4. [Copybooks — Data Structures (30)](#copybooks--data-structures-30)
5. [BMS Screen Maps (17)](#bms-screen-maps-17)
6. [BMS-Generated Copybooks (17)](#bms-generated-copybooks-17)
7. [JCL Batch Jobs — Core (38)](#jcl-batch-jobs--core-38)
8. [Assembler Programs (2)](#assembler-programs-2)
9. [Supporting Assets](#supporting-assets)

---

## Executive Summary

| Asset Type                | Count |
|---------------------------|-------|
| Core COBOL Programs       | 31    |
| Optional Module Programs  | 13    |
| Copybooks (data layouts)  | 30    |
| BMS Screen Maps           | 17    |
| BMS-Generated Copybooks   | 17    |
| JCL Batch Jobs (core)     | 38    |
| JCL Jobs (optional)       | 8     |
| Assembler Programs        | 2     |
| JCL Procedures            | 2     |
| Sample Data Files (ASCII) | 9     |
| Scheduler Configs         | 2     |
| **Total Artifacts**       | **169** |

---

## COBOL Programs — Core (31)

### Online CICS Programs (18)

| # | Program    | Lines | Function                          | CICS Tran | Business Domain      | Classification |
|---|------------|------:|-----------------------------------|-----------|----------------------|----------------|
| 1 | COSGN00C   |   260 | Sign-on / Authentication          | CC00      | Security             | Online-CICS    |
| 2 | COMEN01C   |   308 | Main Menu (Regular Users)         | CM00      | Navigation           | Online-CICS    |
| 3 | COADM01C   |   288 | Admin Menu                        | CA00      | Navigation (Admin)   | Online-CICS    |
| 4 | COACTVWC   |   941 | Account View                      | —         | Account Mgmt         | Online-CICS    |
| 5 | COACTUPC   | 4,236 | Account Update                    | —         | Account Mgmt         | Online-CICS    |
| 6 | COCRDLIC   | 1,459 | Credit Card List                  | —         | Card Mgmt            | Online-CICS    |
| 7 | COCRDSLC   |   887 | Credit Card View (Detail)         | —         | Card Mgmt            | Online-CICS    |
| 8 | COCRDUPC   | 1,560 | Credit Card Update                | —         | Card Mgmt            | Online-CICS    |
| 9 | COTRN00C   |   699 | Transaction List                  | —         | Transaction Mgmt     | Online-CICS    |
|10 | COTRN01C   |   330 | Transaction View                  | —         | Transaction Mgmt     | Online-CICS    |
|11 | COTRN02C   |   783 | Transaction Add                   | —         | Transaction Mgmt     | Online-CICS    |
|12 | CORPT00C   |   649 | Transaction Reports (submits batch via TDQ) | — | Reporting         | Online-CICS    |
|13 | COBIL00C   |   572 | Bill Payment                      | —         | Payments             | Online-CICS    |
|14 | COUSR00C   |   695 | User List (Admin)                 | —         | User Administration  | Online-CICS    |
|15 | COUSR01C   |   299 | User Add (Admin)                  | —         | User Administration  | Online-CICS    |
|16 | COUSR02C   |   414 | User Update (Admin)               | —         | User Administration  | Online-CICS    |
|17 | COUSR03C   |   359 | User Delete (Admin)               | —         | User Administration  | Online-CICS    |
|18 | CSUTLDTC   |   157 | Date Utility (calls CEEDAYS LE)   | —         | Utility              | Online-CICS    |

### Batch Programs (13)

| # | Program    | Lines | Function                                   | Business Domain       | Classification |
|---|------------|------:|--------------------------------------------|------------------------|----------------|
| 1 | CBTRN02C   |   731 | Transaction Posting (daily → master)       | Transaction Processing | Batch          |
| 2 | CBTRN01C   |   494 | Transaction Processing (daily file read)   | Transaction Processing | Batch          |
| 3 | CBTRN03C   |   649 | Transaction Report Generation              | Reporting              | Batch          |
| 4 | CBACT04C   |   652 | Interest Calculation                       | Financial Calc         | Batch          |
| 5 | CBSTM03A   |   924 | Statement Generation (text + HTML)         | Statement Gen          | Batch          |
| 6 | CBSTM03B   |   230 | Statement Subroutine (file processing)     | Statement Gen          | Batch-Sub      |
| 7 | CBACT01C   |   430 | Account File Read/Validate                 | Account Mgmt           | Batch          |
| 8 | CBACT02C   |   178 | Card File Read/Validate                    | Card Mgmt              | Batch          |
| 9 | CBACT03C   |   178 | Cross-Reference File Read/Validate         | Card Mgmt              | Batch          |
|10 | CBCUS01C   |   178 | Customer File Read/Validate                | Customer Mgmt          | Batch          |
|11 | CBEXPORT   |   582 | Data Export (VSAM → sequential)            | Data Migration         | Batch          |
|12 | CBIMPORT   |   487 | Data Import (sequential → VSAM)            | Data Migration         | Batch          |
|13 | COBSWAIT   |    41 | Wait Utility (delay step)                  | Utility                | Batch          |

**Naming Conventions:**
- `CO*` → Online CICS programs
- `CB*` → Batch programs
- `CS*` → Shared utility/subroutine programs

---

## COBOL Programs — Optional Modules (13)

### Authorization Module — IMS/DB2/MQ (8 programs)

| # | Program    | Lines | Function                                      | Technology     |
|---|------------|------:|-----------------------------------------------|----------------|
| 1 | COPAUA0C   | 1,026 | MQ Trigger — Authorization request handler    | CICS + MQ      |
| 2 | COPAUS0C   | 1,032 | Pending Authorization Summary screen          | CICS + IMS DB  |
| 3 | COPAUS1C   |   604 | Pending Authorization Detail screen           | CICS + IMS DB  |
| 4 | COPAUS2C   |   244 | Fraud Marking (write to DB2)                  | CICS + DB2     |
| 5 | CBPAUP0C   |   386 | Batch Purge of processed authorizations       | Batch + DB2    |
| 6 | PAUDBLOD   |   369 | IMS DB Load utility                           | Batch + IMS    |
| 7 | PAUDBUNL   |   317 | IMS DB Unload utility                         | Batch + IMS    |
| 8 | DBUNLDGS   |   366 | GSAM Unload utility                           | Batch + IMS    |

### Transaction Type DB2 Module (3 programs)

| # | Program    | Lines | Function                                      | Technology     |
|---|------------|------:|-----------------------------------------------|----------------|
| 1 | COTRTLIC   | 2,098 | Transaction Type List / Delete (DB2 cursors)  | CICS + DB2     |
| 2 | COTRTUPC   | 1,702 | Transaction Type Add / Edit (embedded SQL)    | CICS + DB2     |
| 3 | COBTUPDT   |   237 | Batch Update of transaction types             | Batch + DB2    |

### VSAM-MQ Module (2 programs)

| # | Program    | Lines | Function                                      | Technology     |
|---|------------|------:|-----------------------------------------------|----------------|
| 1 | CODATE01   |   524 | MQ Request/Response — System Date service     | CICS + MQ      |
| 2 | COACCT01   |   620 | MQ Request/Response — Account Inquiry service | CICS + MQ      |

---

## Copybooks — Data Structures (30)

| # | Copybook   | Lines | Purpose                                | Primary Entity        |
|---|------------|------:|----------------------------------------|-----------------------|
| 1 | COCOM01Y   |    48 | Communication area (COMMAREA)          | Inter-program context |
| 2 | COMEN02Y   |   104 | Main menu option definitions           | Menu configuration    |
| 3 | COADM02Y   |    58 | Admin menu option definitions          | Menu configuration    |
| 4 | COTTL01Y   |    50 | Screen title/header layout             | UI layout             |
| 5 | CSDAT01Y   |    62 | Date/time data structure               | Utility               |
| 6 | CSMSG01Y   |    35 | Message area (long message)            | UI messaging          |
| 7 | CSMSG02Y   |    35 | Message area (short/info message)      | UI messaging          |
| 8 | CSUSR01Y   |    46 | User security record                   | User / Security       |
| 9 | CSSETATY   |    88 | Screen attribute set definitions       | UI utility            |
| 10| CSLKPCDY   |    45 | Lookup code table                      | Reference data        |
| 11| CSSTRPFY   |    43 | String processing functions            | Utility               |
| 12| CSUTLDPY   |    32 | Utility display parameters             | Utility               |
| 13| CSUTLDWY   |    36 | Utility work area                      | Utility               |
| 14| CODATECN   |    23 | Date conversion constants              | Utility               |
| 15| CVACT01Y   |    30 | Account master record (300 bytes)      | Account               |
| 16| CVACT02Y   |    28 | Card data record (150 bytes)           | Card                  |
| 17| CVACT03Y   |    23 | Card cross-reference record            | Card ↔ Account        |
| 18| CVCRD01Y   |    40 | Card display structure                 | Card (UI)             |
| 19| CVCUS01Y   |    45 | Customer master record (500 bytes)     | Customer              |
| 20| CVTRA01Y   |    13 | Transaction category balance           | Tran Category Bal     |
| 21| CVTRA02Y   |    13 | Disclosure group record                | Disclosure            |
| 22| CVTRA03Y   |    10 | Transaction type record                | Transaction Type      |
| 23| CVTRA04Y   |    12 | Transaction category type record       | Tran Category         |
| 24| CVTRA05Y   |    21 | Transaction master record (350 bytes)  | Transaction           |
| 25| CVTRA06Y   |    21 | Daily transaction record (350 bytes)   | Daily Transaction     |
| 26| CVTRA07Y   |    73 | Transaction report layout              | Report output         |
| 27| CVEXPORT   |   103 | Export/import record layout            | Data Migration        |
| 28| CUSTREC    |    31 | Customer record (statement reporting)  | Customer (Report)     |
| 29| COSTM01    |    38 | Transaction altered layout (reporting) | Transaction (Report)  |
| 30| UNUSED1Y   |    10 | Unused placeholder record              | N/A (dead code)       |

**Naming Conventions:**
- `CV*` → VSAM file data structures
- `CS*` → Shared/system copybooks
- `CO*` → Communication/menu/screen copybooks

---

## BMS Screen Maps (17)

| # | Map        | Lines | Screen Title              | Paired Program | Domain             |
|---|------------|------:|---------------------------|----------------|--------------------|
| 1 | COSGN00    |   128 | Sign-on                   | COSGN00C       | Security           |
| 2 | COMEN01    |   183 | Main Menu                 | COMEN01C       | Navigation         |
| 3 | COADM01    |   149 | Admin Menu                | COADM01C       | Navigation (Admin) |
| 4 | COACTVW    |   388 | Account View              | COACTVWC       | Account Mgmt       |
| 5 | COACTUP    |   460 | Account Update            | COACTUPC       | Account Mgmt       |
| 6 | COCRDLI    |   394 | Credit Card List          | COCRDLIC       | Card Mgmt          |
| 7 | COCRDSL    |   311 | Credit Card Detail View   | COCRDSLC       | Card Mgmt          |
| 8 | COCRDUP    |   323 | Credit Card Update        | COCRDUPC       | Card Mgmt          |
| 9 | COTRN00    |   378 | Transaction List          | COTRN00C       | Transaction Mgmt   |
|10 | COTRN01    |   191 | Transaction View          | COTRN01C       | Transaction Mgmt   |
|11 | COTRN02    |   239 | Transaction Add           | COTRN02C       | Transaction Mgmt   |
|12 | CORPT00    |   152 | Transaction Reports       | CORPT00C       | Reporting          |
|13 | COBIL00    |   295 | Bill Payment              | COBIL00C       | Payments           |
|14 | COUSR00    |   311 | User List                 | COUSR00C       | User Admin         |
|15 | COUSR01    |   152 | Add User                  | COUSR01C       | User Admin         |
|16 | COUSR02    |   169 | Update User               | COUSR02C       | User Admin         |
|17 | COUSR03    |   153 | Delete User               | COUSR03C       | User Admin         |

---

## BMS-Generated Copybooks (17)

These are auto-generated COBOL data structures from BMS map compilation. One per BMS map.

| Copybook     | Generated From | Used By   |
|--------------|----------------|-----------|
| COSGN00.CPY  | COSGN00.bms    | COSGN00C  |
| COMEN01.CPY  | COMEN01.bms    | COMEN01C  |
| COADM01.CPY  | COADM01.bms    | COADM01C  |
| COACTVW.CPY  | COACTVW.bms    | COACTVWC  |
| COACTUP.CPY  | COACTUP.bms    | COACTUPC  |
| COCRDLI.CPY  | COCRDLI.bms    | COCRDLIC  |
| COCRDSL.CPY  | COCRDSL.bms    | COCRDSLC  |
| COCRDUP.CPY  | COCRDUP.bms    | COCRDUPC  |
| COTRN00.CPY  | COTRN00.bms    | COTRN00C  |
| COTRN01.CPY  | COTRN01.bms    | COTRN01C  |
| COTRN02.CPY  | COTRN02.bms    | COTRN02C  |
| CORPT00.CPY  | CORPT00.bms    | CORPT00C  |
| COBIL00.CPY  | COBIL00.bms    | COBIL00C  |
| COUSR00.CPY  | COUSR00.bms    | COUSR00C  |
| COUSR01.CPY  | COUSR01.bms    | COUSR01C  |
| COUSR02.CPY  | COUSR02.bms    | COUSR02C  |
| COUSR03.CPY  | COUSR03.bms    | COUSR03C  |

---

## JCL Batch Jobs — Core (38)

### Data Load / Refresh Jobs (12)

| # | Job        | Function                                   | COBOL Program   | Key Datasets                           |
|---|------------|-------------------------------------------|-----------------|----------------------------------------|
| 1 | DUSRSECJ   | Load user security VSAM from flat file    | (IDCAMS/IEBGENER) | USRSEC.PS → USRSEC.VSAM.KSDS       |
| 2 | ACCTFILE   | Define & load account master VSAM         | (IDCAMS)        | ACCTDATA.PS → ACCTDATA.VSAM.KSDS      |
| 3 | CARDFILE   | Define & load card master VSAM            | (IDCAMS)        | CARDDATA.PS → CARDDATA.VSAM.KSDS      |
| 4 | CUSTFILE   | Define & load customer master VSAM        | (IDCAMS)        | CUSTDATA.PS → CUSTDATA.VSAM.KSDS      |
| 5 | XREFFILE   | Load card cross-reference + alt index     | (IDCAMS)        | CARDXREF.PS → CARDXREF.VSAM.KSDS      |
| 6 | TRANFILE   | Define & load transaction VSAM + alt idx  | (IDCAMS)        | TRANSACT.PS → TRANSACT.VSAM.KSDS      |
| 7 | TRANTYPE   | Define & load transaction type VSAM       | (IDCAMS)        | TRANTYPE.PS → TRANTYPE.VSAM.KSDS      |
| 8 | TRANCATG   | Define & load transaction category VSAM   | (IDCAMS)        | TRANCATG.PS → TRANCATG.VSAM.KSDS      |
| 9 | TCATBALF   | Define & load tran category balance VSAM  | (IDCAMS)        | TCATBAL.PS → TCATBAL.VSAM.KSDS        |
|10 | DISCGRP    | Define & load disclosure group VSAM       | (IDCAMS)        | DISCGRP.PS → DISCGRP.VSAM.KSDS        |
|11 | DALYREJS   | Define daily rejects VSAM file            | (IDCAMS)        | DALYREJS.VSAM.KSDS                     |
|12 | DEFCUST    | Define customer VSAM cluster              | (IDCAMS)        | CUSTDATA.VSAM.KSDS                     |

### Batch Processing Jobs (8)

| # | Job        | Function                                   | COBOL Program   | Key Datasets                           |
|---|------------|-------------------------------------------|-----------------|----------------------------------------|
| 1 | POSTTRAN   | Core transaction posting                  | CBTRN02C        | DALYTRAN → TRANSACT, ACCTDATA          |
| 2 | INTCALC    | Interest calculation                      | CBACT04C        | TCATBAL, ACCTDATA, DISCGRP, TRANSACT   |
| 3 | TRANBKP    | Backup transaction file                   | (IDCAMS)        | TRANSACT.VSAM → TRANSACT.BKUP          |
| 4 | COMBTRAN   | Combine daily + master transactions       | (SORT+IDCAMS)   | DAILYTRAN + TRANSACT                   |
| 5 | CREASTMT   | Create account statements (text + HTML)   | CBSTM03A        | TRANSACT, XREF, ACCT, CUST → STATEMNT |
| 6 | TRANREPT   | Transaction report generation             | CBTRN03C        | TRANSACT, TRANTYPE, TRANCATG           |
| 7 | TRANIDX    | Rebuild transaction alternate indexes     | (IDCAMS)        | TRANSACT.VSAM.KSDS                     |
| 8 | PRTCATBL   | Print category balance file               | (SORT)          | TCATBAL.VSAM.KSDS → report             |

### File Operations Jobs (4)

| # | Job        | Function                                   |
|---|------------|-------------------------------------------|
| 1 | CLOSEFIL   | Close CICS files (before batch)           |
| 2 | OPENFIL    | Open CICS files (after batch)             |
| 3 | CBEXPORT   | Export VSAM data to sequential file       |
| 4 | CBIMPORT   | Import sequential data into VSAM          |

### Read / Validate Jobs (4)

| # | Job        | Function                                   | COBOL Program |
|---|------------|-------------------------------------------|---------------|
| 1 | READACCT   | Read and validate account file            | CBACT01C      |
| 2 | READCARD   | Read and validate card file               | CBACT02C      |
| 3 | READXREF   | Read and validate cross-reference file    | CBACT03C      |
| 4 | READCUST   | Read and validate customer file           | CBCUS01C      |

### Infrastructure / Utility Jobs (8)

| # | Job        | Function                                   |
|---|------------|-------------------------------------------|
| 1 | DEFGDGB    | Define GDG (Generation Data Group) bases  |
| 2 | DEFGDGD    | Define GDG + backup data files            |
| 3 | REPTFILE   | Define report output VSAM cluster         |
| 4 | ESDSRRDS   | Define ESDS and RRDS test VSAM files      |
| 5 | CBADMCDJ   | Load CICS CSD resource definitions        |
| 6 | WAITSTEP   | Wait step utility (calls COBSWAIT)        |
| 7 | FTPJCL     | FTP file transfer job                     |
| 8 | TXT2PDF1   | Convert text statements to PDF            |

### Internal Reader / Chain Jobs (2)

| # | Job        | Function                                   |
|---|------------|-------------------------------------------|
| 1 | INTRDRJ1   | Internal reader — triggers INTRDRJ2       |
| 2 | INTRDRJ2   | Internal reader — triggered by INTRDRJ1   |

---

## Assembler Programs (2)

| Program    | Function                              |
|------------|---------------------------------------|
| MVSWAIT    | MVS wait macro (used by COBSWAIT)     |
| COBDATFT   | Date formatting assembler routine     |

---

## Supporting Assets

### JCL Procedures (2)

| Procedure     | Function                              |
|---------------|---------------------------------------|
| REPROC.prc    | Reprocessing procedure                |
| TRANREPT.prc  | Transaction report procedure          |

### Sample Data Files (ASCII) — 9 files

| File            | Entity               |
|-----------------|----------------------|
| acctdata.txt    | Account records      |
| carddata.txt    | Card records         |
| cardxref.txt    | Card cross-reference |
| custdata.txt    | Customer records     |
| dailytran.txt   | Daily transactions   |
| discgrp.txt     | Disclosure groups    |
| tcatbal.txt     | Category balances    |
| trancatg.txt    | Transaction categories|
| trantype.txt    | Transaction types    |

### Scheduler Configurations (2)

| File                 | Platform    |
|----------------------|-------------|
| CardDemo.ca7         | CA-7        |
| CardDemo.controlm    | Control-M   |

### Control Files

| File         | Purpose            |
|--------------|--------------------|
| REPROCT.ctl  | Reprocessing control |
