# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Source:** `uc-legacy-modernization-cobol-to-java`
> **Application:** CardDemo -- Mainframe Credit Card Management System (COBOL/CICS/VSAM/JCL)

---

## Executive Summary

| Asset Type              | Count | Location              |
|-------------------------|-------|-----------------------|
| COBOL Programs (Core)   | 31    | `app/cbl/`            |
| Copybooks (Core)        | 30    | `app/cpy/`            |
| BMS Screen Maps         | 17    | `app/bms/`            |
| BMS-Generated Copybooks | 17    | `app/cpy-bms/`        |
| JCL Batch Jobs          | 38    | `app/jcl/`            |
| Assembler Programs      | 2     | `app/asm/`            |
| JCL Procedures          | 2     | `app/proc/`           |
| Scheduler Configs       | 2     | `app/scheduler/`      |
| **Optional Modules**    |       |                       |
| Auth (IMS/DB2/MQ)       | 8 pgm, 8 cpy, 2 bms, 5 jcl | `app/app-authorization-ims-db2-mq/` |
| Tran-Type (DB2)         | 3 pgm, 2 cpy, 2 bms, 3 jcl  | `app/app-transaction-type-db2/`     |
| VSAM-MQ                 | 2 pgm | `app/app-vsam-mq/`   |
| **Grand Total**         | **~170 artifacts** |       |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program    | Lines | CICS Tran | Business Function               | Classification         |
|---|------------|-------|-----------|----------------------------------|------------------------|
| 1 | COSGN00C   | 260   | CC00      | User sign-on / authentication    | Security               |
| 2 | COMEN01C   | 308   | CM00      | Main menu navigation             | Navigation / UI        |
| 3 | COADM01C   | 288   | CA00      | Admin menu                       | Administration         |
| 4 | COACTVWC   | 941   | CA01      | Account view (read-only)         | Account Management     |
| 5 | COACTUPC   | 4,236 | CA02      | Account update                   | Account Management     |
| 6 | COCRDLIC   | 1,459 | CC01      | Card list / browse               | Card Management        |
| 7 | COCRDSLC   | 887   | CC02      | Card detail view                 | Card Management        |
| 8 | COCRDUPC   | 1,560 | CC03      | Card update                      | Card Management        |
| 9 | COTRN00C   | 699   | CT00      | Transaction list / browse        | Transaction Management |
| 10| COTRN01C   | 330   | CT01      | Transaction detail view          | Transaction Management |
| 11| COTRN02C   | 783   | CT02      | Transaction add (new)            | Transaction Management |
| 12| CORPT00C   | 649   | CR00      | Transaction report request       | Reporting              |
| 13| COBIL00C   | 572   | CB00      | Bill payment                     | Payments               |
| 14| COUSR00C   | 695   | CU00      | User list (admin)                | User Administration    |
| 15| COUSR01C   | 299   | CU01      | User add (admin)                 | User Administration    |
| 16| COUSR02C   | 414   | CU02      | User update (admin)              | User Administration    |
| 17| COUSR03C   | 359   | CU03      | User delete (admin)              | User Administration    |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program    | Lines | Business Function                        | Classification          |
|---|------------|-------|------------------------------------------|-------------------------|
| 18| CBACT01C   | 430   | Account file data reader / reporter      | Data Utility            |
| 19| CBACT02C   | 178   | Card file data reader                    | Data Utility            |
| 20| CBACT03C   | 178   | Cross-reference file reader              | Data Utility            |
| 21| CBACT04C   | 652   | Interest calculation                     | Financial Processing    |
| 22| CBCUS01C   | 178   | Customer file reader                     | Data Utility            |
| 23| CBTRN01C   | 494   | Daily transaction validation / loading   | Transaction Processing  |
| 24| CBTRN02C   | 731   | Transaction posting (core batch)         | Transaction Processing  |
| 25| CBTRN03C   | 649   | Transaction report generation            | Reporting               |
| 26| CBSTM03A   | 924   | Statement generation (main driver)       | Statement Processing    |
| 27| CBSTM03B   | 230   | Statement generation (file I/O sub)      | Statement Processing    |
| 28| CBEXPORT   | 582   | Data export (all entities to flat file)  | Data Migration          |
| 29| CBIMPORT   | 487   | Data import (flat file to VSAM)          | Data Migration          |
| 30| COBSWAIT   | 41    | Wait utility (calls ASM MVSWAIT)         | System Utility          |

### 1.3 Utility Programs

| # | Program    | Lines | Business Function                        | Classification    |
|---|------------|-------|------------------------------------------|-------------------|
| 31| CSUTLDTC   | 157   | Date conversion utility (CEE3ABD/CEEDAYS)| Shared Utility    |

---

## 2. Copybooks -- Core (`app/cpy/`)

### 2.1 Data-Structure Copybooks (prefix `CV*`)

| # | Copybook   | Lines | Record Len | Business Entity                    |
|---|------------|-------|------------|------------------------------------|
| 1 | CVACT01Y   | 20    | 300 bytes  | Account master record              |
| 2 | CVACT02Y   | 14    | 150 bytes  | Card master record                 |
| 3 | CVACT03Y   | 11    | 50 bytes   | Card-to-account cross-reference    |
| 4 | CVCRD01Y   | 46    | ~550 bytes | Card detail (extended)             |
| 5 | CVCUS01Y   | 26    | 500 bytes  | Customer master record             |
| 6 | CVTRA01Y   | 13    | 50 bytes   | Transaction category balance       |
| 7 | CVTRA02Y   | 13    | 50 bytes   | Disclosure group / interest rate   |
| 8 | CVTRA03Y   | 10    | 60 bytes   | Transaction type                   |
| 9 | CVTRA04Y   | 12    | 60 bytes   | Transaction category type          |
| 10| CVTRA05Y   | 21    | 350 bytes  | Transaction record (online)        |
| 11| CVTRA06Y   | 21    | 350 bytes  | Daily transaction record           |
| 12| CVTRA07Y   | 73    | N/A        | Report layout structures           |
| 13| CVEXPORT   | 103   | Variable   | Export/import record layout        |
| 14| CUSTREC    | 26    | ~500 bytes | Customer record (alternate layout) |
| 15| COSTM01    | 38    | 350 bytes  | Transaction for statements (keyed) |

### 2.2 Common / UI Copybooks (prefix `CO*`, `CS*`, `CM*`)

| # | Copybook   | Lines | Purpose                                    |
|---|------------|-------|--------------------------------------------|
| 16| COCOM01Y   | 47    | Common communication area (DFHCOMMAREA)    |
| 17| COMEN02Y   | 101   | Menu item definitions and navigation map   |
| 18| COADM02Y   | 62    | Admin menu item definitions                |
| 19| COTTL01Y   | 27    | Screen title / header area                 |
| 20| CSUSR01Y   | 26    | User security record (sign-on data)        |
| 21| CSDAT01Y   | 58    | Date utility working storage               |
| 22| CSMSG01Y   | 24    | Short message area (1 line)                |
| 23| CSMSG02Y   | 35    | Long message area (multi-line)             |
| 24| CSSETATY   | 30    | Set attribute utility (BMS field attrs)    |
| 25| CSSTRPFY   | 85    | String strip/format utility procedure      |
| 26| CSUTLDPY   | 375   | Date utility procedures (PERFORM code)     |
| 27| CSUTLDWY   | 89    | Date utility working-storage variables     |
| 28| CSLKPCDY   | 1,318 | Lookup code tables (state, country codes)  |
| 29| CODATECN   | 52    | Date conversion record layout              |
| 30| UNUSED1Y   | 10    | Unused / placeholder record                |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map Name   | Lines | Screen ID  | Screen Title                    | Associated Program |
|---|------------|-------|------------|---------------------------------|--------------------|
| 1 | COSGN00    | 210   | COSGN0A    | Sign-On Screen                  | COSGN00C           |
| 2 | COMEN01    | 167   | COMEN1A    | Main Menu                       | COMEN01C           |
| 3 | COADM01    | 167   | COADM1A    | Admin Menu                      | COADM01C           |
| 4 | COACTVW    | 378   | COACT0A    | Account View                    | COACTVWC           |
| 5 | COACTUP    | 512   | COACT1A    | Account Update                  | COACTUPC           |
| 6 | COCRDLI    | 344   | COCRD0A    | Card List                       | COCRDLIC           |
| 7 | COCRDSL    | 157   | COCRD1A    | Card Detail View                | COCRDSLC           |
| 8 | COCRDUP    | 172   | COCRD2A    | Card Update                     | COCRDUPC           |
| 9 | COTRN00    | 464   | COTRN0A    | Transaction List                | COTRN00C           |
| 10| COTRN01    | 273   | COTRN1A    | Transaction Detail View         | COTRN01C           |
| 11| COTRN02    | 307   | COTRN2A    | Transaction Add                 | COTRN02C           |
| 12| CORPT00    | 231   | CORPT0A    | Transaction Report              | CORPT00C           |
| 13| COBIL00    | 141   | COBIL0A    | Bill Payment                    | COBIL00C           |
| 14| COUSR00    | 463   | COUSR0A    | User List                       | COUSR00C           |
| 15| COUSR01    | 164   | COUSR1A    | User Add                        | COUSR01C           |
| 16| COUSR02    | 169   | COUSR2A    | User Update                     | COUSR02C           |
| 17| COUSR03    | 153   | COUSR3A    | User Delete                     | COUSR03C           |

### BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map generates a symbolic map copybook (same name with `.CPY` extension) containing field definitions used by the corresponding COBOL program for SEND MAP / RECEIVE MAP operations. Total: **17 copybooks**.

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data Loading / Refresh Jobs

| # | Job Name   | Lines | Purpose                                     | Key Program / Utility |
|---|------------|-------|---------------------------------------------|-----------------------|
| 1 | ACCTFILE   | 65    | Refresh account master VSAM file            | IDCAMS (REPRO)        |
| 2 | CARDFILE   | 128   | Refresh card master VSAM (+ alt index)      | IDCAMS                |
| 3 | CUSTFILE   | 84    | Refresh customer master VSAM file           | IDCAMS                |
| 4 | XREFFILE   | 106   | Load card cross-reference VSAM (+ AIX)      | IDCAMS                |
| 5 | TRANFILE   | 125   | Load transaction master VSAM file           | IDCAMS                |
| 6 | DUSRSECJ   | 92    | Load user security VSAM file                | IDCAMS                |

### 4.2 Core Batch Processing Jobs

| # | Job Name   | Lines | Purpose                                     | Key Program           |
|---|------------|-------|---------------------------------------------|-----------------------|
| 7 | POSTTRAN   | 45    | Core transaction posting                    | CBTRN02C              |
| 8 | INTCALC    | 44    | Interest calculation                        | CBACT04C              |
| 9 | COMBTRAN   | 52    | Combine daily + master transactions         | SORT / ICETOOL        |
| 10| CREASTMT   | 97    | Create account statements (text + HTML)     | CBSTM03A, SORT        |
| 11| TRANBKP    | 71    | Backup transaction files (GDG)              | IDCAMS                |
| 12| TRANIDX    | 58    | Build transaction alternate index           | IDCAMS                |

### 4.3 Report / Read Jobs

| # | Job Name   | Lines | Purpose                                     | Key Program           |
|---|------------|-------|---------------------------------------------|-----------------------|
| 13| TRANREPT   | 84    | Transaction report (daily)                  | CBTRN03C              |
| 14| READACCT   | 50    | Read/display account file                   | CBACT01C              |
| 15| READCARD   | 31    | Read/display card file                      | CBACT02C              |
| 16| READCUST   | 30    | Read/display customer file                  | CBCUS01C              |
| 17| READXREF   | 31    | Read/display cross-reference file           | CBACT03C              |
| 18| REPTFILE   | 32    | Copy report files                           | IEBGENER              |
| 19| PRTCATBL   | 66    | Print transaction category balance          | CBACT01C              |

### 4.4 VSAM Definition / Utility Jobs

| # | Job Name   | Lines | Purpose                                     | Key Utility           |
|---|------------|-------|---------------------------------------------|-----------------------|
| 20| DEFGDGB    | 63    | Define GDG bases for backups                | IDCAMS                |
| 21| DEFGDGD    | 94    | Define GDG datasets                         | IDCAMS                |
| 22| DEFCUST    | 47    | Define customer VSAM cluster                | IDCAMS                |
| 23| ESDSRRDS   | 124   | Define ESDS/RRDS VSAM files                 | IDCAMS                |
| 24| DISCGRP    | 65    | Load disclosure group data                  | IDCAMS                |
| 25| TRANCATG   | 65    | Load transaction category data              | IDCAMS                |
| 26| TRANTYPE   | 65    | Load transaction type data                  | IDCAMS                |
| 27| TCATBALF   | 65    | Load transaction category balance           | IDCAMS                |
| 28| DALYREJS   | 32    | Define daily rejects VSAM file              | IDCAMS                |

### 4.5 Operational / Lifecycle Jobs

| # | Job Name   | Lines | Purpose                                     | Key Utility           |
|---|------------|-------|---------------------------------------------|-----------------------|
| 29| CLOSEFIL   | 34    | Close CICS files for batch processing       | DFHCSDUP              |
| 30| OPENFIL    | 34    | Open CICS files after batch cycle           | DFHCSDUP              |
| 31| CBEXPORT   | 72    | Data export job                             | CBEXPORT              |
| 32| CBIMPORT   | 68    | Data import job                             | CBIMPORT              |
| 33| CBADMCDJ   | 167   | Admin card demo JCL (full compile/link)     | COBOL Compiler        |
| 34| WAITSTEP   | 27    | Wait step (delay between batch steps)       | COBSWAIT              |
| 35| FTPJCL     | 42    | FTP file transfer job                       | FTP                   |
| 36| INTRDRJ1   | 19    | Internal reader trigger job 1               | IEBGENER              |
| 37| INTRDRJ2   | 14    | Internal reader trigger job 2               | IDCAMS                |
| 38| TXT2PDF1   | 41    | Convert text statements to PDF              | TXT2PDF               |

### Batch Processing Cycle (Typical Nightly Run Order)

```
CLOSEFIL --> ACCTFILE --> CARDFILE --> CUSTFILE --> XREFFILE --> TRANFILE
    --> DUSRSECJ --> POSTTRAN --> INTCALC --> TRANBKP --> COMBTRAN
    --> CREASTMT --> TRANREPT --> TRANIDX --> OPENFIL
```

---

## 5. Assembler Programs (`app/asm/`)

| # | Program    | Purpose                                      |
|---|------------|----------------------------------------------|
| 1 | MVSWAIT    | Mainframe wait routine (called by COBSWAIT)  |
| 2 | COBDATFT   | Date formatting utility (called by CBACT01C) |

---

## 6. JCL Procedures (`app/proc/`)

| # | Procedure  | Purpose                                      |
|---|------------|----------------------------------------------|
| 1 | REPROC     | Reprocessing procedure (REPRO utility)       |
| 2 | TRANREPT   | Transaction report procedure                 |

---

## 7. Scheduler Configurations (`app/scheduler/`)

| # | Config           | Scheduler   | Purpose                          |
|---|------------------|-------------|----------------------------------|
| 1 | CardDemo.ca7     | CA-7        | Job scheduling definitions       |
| 2 | CardDemo.controlm| Control-M   | Job scheduling definitions       |

---

## 8. Optional Extension Modules

### 8.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/`)

| # | Program    | Lines | Type    | Business Function                        |
|---|------------|-------|---------|------------------------------------------|
| 1 | COPAUA0C   | ~1,020| Online  | MQ trigger -- authorization request handler |
| 2 | COPAUS0C   | ~920  | Online  | Authorization summary screen             |
| 3 | COPAUS1C   | ~570  | Online  | Authorization detail screen              |
| 4 | COPAUS2C   | ~220  | Online  | Fraud marking (writes to DB2)            |
| 5 | CBPAUP0C   | ~150  | Batch   | Batch purge of old authorizations        |
| 6 | DBUNLDGS   | ~330  | Batch   | IMS DB unload (GSAM)                     |
| 7 | PAUDBLOD   | ~340  | Batch   | IMS DB load from flat files              |
| 8 | PAUDBUNL   | ~290  | Batch   | IMS DB unload to flat files              |

**Copybooks:** CCPAUERY, CCPAURLY, CCPAURQY, CIPAUSMY, CIPAUDTY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB (8 + 3 PCB)
**BMS Maps:** COPAU00 (summary), COPAU01 (detail)
**JCL Jobs:** CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB

### 8.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program    | Type    | Business Function                        |
|---|------------|---------|------------------------------------------|
| 1 | COTRTUPC   | Online  | Transaction type add/edit (DB2 CRUD)     |
| 2 | COTRTLIC   | Online  | Transaction type list/delete (DB2)       |
| 3 | COBTUPDT   | Batch   | Batch transaction type update (DB2)      |

**Copybooks:** CSDB2RPY (DB2 procedures), CSDB2RWY (DB2 working storage)
**BMS Maps:** COTRTUP (add/edit screen), COTRTLI (list screen)
**JCL Jobs:** CREADB21, MNTTRDB2, TRANEXTR

### 8.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program    | Type    | Business Function                        |
|---|------------|---------|------------------------------------------|
| 1 | COACCT01   | Online  | MQ-based account inquiry (request/reply) |
| 2 | CODATE01   | Online  | MQ-based system date service             |

---

## 9. Naming Conventions

| Prefix  | Meaning                                         |
|---------|-------------------------------------------------|
| `CO*`   | Online CICS program                             |
| `CB*`   | Batch program                                   |
| `CV*`   | Copybook -- VSAM data structure                 |
| `CS*`   | Copybook -- shared/common utility               |
| `CO*Y`  | Copybook -- online communication area           |
| `CM*`   | Copybook -- menu definitions                    |
| `*C`    | Program suffix indicating COBOL                 |
| `*Y`    | Copybook suffix (data layout)                   |

---

## 10. Technology Stack

| Layer            | Technology                                 |
|------------------|--------------------------------------------|
| Language         | COBOL-85, Assembler (ASM)                  |
| Online TP        | CICS/TS                                    |
| Data Access      | VSAM (KSDS, ESDS, RRDS), Sequential Files |
| Screen UI        | BMS Maps (3270 Terminal)                   |
| Batch Scheduler  | JCL, CA-7, Control-M                       |
| Database (opt)   | IMS DB, DB2                                |
| Messaging (opt)  | IBM MQ                                     |
| Utilities        | IDCAMS, SORT/ICETOOL, IEBGENER, DFHCSDUP  |
