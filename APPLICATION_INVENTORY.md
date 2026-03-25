# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM z/OS, CICS, VSAM, JCL Batch
>
> This document catalogs every artifact in the CardDemo mainframe credit card management application.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs (31 Core + 13 Optional)](#cobol-programs)
3. [Copybooks (30)](#copybooks)
4. [BMS Screen Maps (17)](#bms-screen-maps)
5. [JCL Batch Jobs (38)](#jcl-batch-jobs)
6. [Assembler Programs (2)](#assembler-programs)
7. [Optional Modules (3 Extensions)](#optional-modules)
8. [Supporting Artifacts](#supporting-artifacts)

---

## Executive Summary

| Artifact Type           | Count | Location          |
|------------------------|-------|-------------------|
| COBOL Programs (core)  | 31    | `app/cbl/`        |
| Copybooks (data)       | 30    | `app/cpy/`        |
| BMS-Generated Copybooks| 17    | `app/cpy-bms/`    |
| BMS Screen Maps        | 17    | `app/bms/`        |
| JCL Batch Jobs         | 38    | `app/jcl/`        |
| Assembler Programs     | 2     | `app/asm/`        |
| JCL Procedures         | 2     | `app/proc/`       |
| Optional Module Programs| 13   | `app/app-*/cbl/`  |
| **Total Artifacts**    | **150** |                 |

### Business Domains

| Domain                  | Online Programs | Batch Programs | Key Data Stores |
|------------------------|----------------|----------------|-----------------|
| **Security / Signon**  | COSGN00C       | -              | USRSEC (VSAM)   |
| **Account Management** | COACTVWC, COACTUPC | CBACT01C-04C | ACCTDAT (VSAM)  |
| **Card Management**    | COCRDLIC, COCRDSLC, COCRDUPC | - | CARDDAT, CARDXREF (VSAM) |
| **Transaction Processing** | COTRN00C-02C | CBTRN01C-03C | TRANSACT, DALYTRAN (VSAM) |
| **Bill Payment**       | COBIL00C       | -              | TRANSACT, ACCTDAT |
| **Reporting**          | CORPT00C       | CBSTM03A/B, CBTRN03C | Statement files |
| **User Administration**| COUSR00C-03C, COADM01C | - | USRSEC (VSAM) |
| **Data Export/Import** | -              | CBEXPORT, CBIMPORT | Multi-file export |
| **Navigation / Utility**| COMEN01C      | COBSWAIT, CSUTLDTC | - |

---

## COBOL Programs

### Online CICS Programs (20 programs)

These programs run under CICS and handle interactive 3270 terminal sessions.

| Program    | Lines | Description                          | CICS Trans | Business Domain       | Copybook Deps | CICS Cmds |
|-----------|-------|--------------------------------------|-----------|----------------------|---------------|-----------|
| COSGN00C  | 260   | User sign-on / authentication        | CC00      | Security             | 9             | 10        |
| COMEN01C  | 308   | Main menu navigation                 | CM00      | Navigation           | 9             | 7         |
| COADM01C  | 288   | Admin menu navigation                | CA00      | Administration       | 9             | 7         |
| COACTVWC  | 941   | Account view (read-only)             | CA01      | Account Management   | 15            | 15        |
| COACTUPC  | 4,236 | Account update (full CRUD)           | CA02      | Account Management   | 18            | 17        |
| COCRDLIC  | 1,459 | Credit card list with selection       | CC01      | Card Management      | 13            | 18        |
| COCRDSLC  | 887   | Credit card detail view              | CC02      | Card Management      | 15            | 14        |
| COCRDUPC  | 1,560 | Credit card update                   | CC03      | Card Management      | 15            | 12        |
| COTRN00C  | 699   | Transaction list browser             | CT00      | Transactions         | 8             | 10        |
| COTRN01C  | 330   | Transaction detail view              | CT01      | Transactions         | 8             | 5         |
| COTRN02C  | 783   | Transaction add (new entry)          | CT02      | Transactions         | 10            | 11        |
| CORPT00C  | 649   | Report request (submits batch)       | CR00      | Reporting            | 8             | 7         |
| COBIL00C  | 572   | Bill payment processing              | CB00      | Bill Payment         | 10            | 13        |
| COUSR00C  | 695   | User list (admin security)           | CU00      | User Admin           | 8             | 11        |
| COUSR01C  | 299   | User add (admin security)            | CU01      | User Admin           | 9             | 5         |
| COUSR02C  | 414   | User update (admin security)         | CU02      | User Admin           | 8             | 6         |
| COUSR03C  | 359   | User delete (admin security)         | CU03      | User Admin           | 8             | 6         |

### Batch COBOL Programs (11 programs)

These programs run in JCL batch jobs for overnight/scheduled processing.

| Program    | Lines | Description                           | Business Domain       | Copybook Deps | File I/O |
|-----------|-------|---------------------------------------|----------------------|---------------|----------|
| CBACT01C  | 430   | Read account master (VSAM to PS)      | Account Management   | 2             | 2 files  |
| CBACT02C  | 178   | Read card data file                   | Card Management      | 1             | 1 file   |
| CBACT03C  | 178   | Read card cross-reference file        | Card Management      | 1             | 1 file   |
| CBACT04C  | 652   | Interest calculation engine           | Financial Processing | 5             | 5 files  |
| CBCUS01C  | 178   | Read customer data file               | Customer Management  | 1             | 1 file   |
| CBTRN01C  | 494   | Daily transaction combination         | Transactions         | 6             | 6 files  |
| CBTRN02C  | 731   | Transaction posting (daily post)      | Transactions         | 5             | 5 files  |
| CBTRN03C  | 649   | Transaction detail report generator   | Reporting            | 5             | 5 files  |
| CBSTM03A  | 924   | Statement generation (text + HTML)    | Reporting            | 4             | 7 files  |
| CBSTM03B  | 230   | Statement generation subroutine       | Reporting            | 0             | 0 files  |
| CBEXPORT  | 582   | Multi-record data export for migration| Data Migration       | 6             | 5 files  |
| CBIMPORT  | 487   | Multi-record data import              | Data Migration       | 6             | 5 files  |

### Utility Programs (2 programs)

| Program    | Lines | Description                         | Type     |
|-----------|-------|-------------------------------------|----------|
| CSUTLDTC  | 157   | Date validation utility (calls CEEDAYS) | Shared utility |
| COBSWAIT  | 41    | Wait/sleep utility (calls MVSWAIT)  | System utility |

---

## Copybooks

### Data Record Copybooks (VSAM file layouts)

| Copybook   | Lines | Description                              | Business Entity     | Record Size |
|-----------|-------|------------------------------------------|--------------------|-----------  |
| CVACT01Y  | 20    | Account master record                    | Account            | ~300 bytes  |
| CVACT02Y  | 14    | Card data record                         | Credit Card        | ~150 bytes  |
| CVACT03Y  | 11    | Card-to-account cross-reference          | Card Cross-Ref     | ~36 bytes   |
| CVCUS01Y  | 26    | Customer data record                     | Customer           | ~500 bytes  |
| CVCRD01Y  | 46    | Card detail record (internal working)    | Credit Card Detail | Variable    |
| CSUSR01Y  | 26    | User security record                     | User Security      | ~80 bytes   |
| CVTRA05Y  | 21    | Online transaction record                | Transaction        | ~350 bytes  |
| CVTRA06Y  | 21    | Daily transaction record                 | Daily Transaction  | ~350 bytes  |
| CVTRA01Y  | 13    | Transaction category balance record      | Tran Cat Balance   | ~50 bytes   |
| CVTRA02Y  | 13    | Disclosure group record                  | Disclosure Group   | ~50 bytes   |
| CVTRA03Y  | 10    | Transaction type record                  | Transaction Type   | ~60 bytes   |
| CVTRA04Y  | 12    | Transaction category type record         | Tran Category      | ~60 bytes   |
| CVTRA07Y  | 73    | Transaction report data structures       | Report Layout      | Variable    |
| CUSTREC   | 26    | Customer record (statement generation)   | Customer (Stmt)    | Variable    |
| COSTM01   | 38    | Statement generation working storage     | Statement          | Variable    |
| CVEXPORT  | 103   | Export record (multi-type REDEFINES)     | Export Record      | ~506 bytes  |
| UNUSED1Y  | 10    | Unused data structure (placeholder)      | N/A                | ~80 bytes   |

### UI / Communication Copybooks

| Copybook   | Lines | Description                             | Used By                    |
|-----------|-------|-----------------------------------------|----------------------------|
| COCOM01Y  | 47    | Common communication area (COMMAREA)    | All online programs        |
| COMEN02Y  | 101   | Main menu option definitions            | COMEN01C                   |
| COADM02Y  | 62    | Admin menu option definitions           | COADM01C                   |
| COTTL01Y  | 27    | Title/header line definitions           | All online programs        |
| CSDAT01Y  | 58    | Date display working storage            | All online programs        |
| CSMSG01Y  | 24    | Message area (single message)           | All online programs        |
| CSMSG02Y  | 35    | Message area (dual message)             | Programs with confirmation |
| CSLKPCDY  | 1,318 | Lookup code table (state/country codes) | COACTUPC                   |

### Utility Copybooks

| Copybook   | Lines | Description                               | Used By            |
|-----------|-------|-------------------------------------------|-------------------|
| CSSETATY  | 30    | Set attribute bytes (COPY REPLACING)      | COACTUPC          |
| CSSTRPFY  | 85    | Strip/pad field utility procedures        | Multiple programs |
| CSUTLDPY  | 375   | Date validation procedures (inline COPY)  | COACTUPC          |
| CSUTLDWY  | 89    | Date validation working storage           | COACTUPC          |
| CODATECN  | 52    | Date format conversion record             | CBACT01C          |

### BMS-Generated Copybooks (17)

These are auto-generated from BMS maps and define the symbolic map data structures.

| Copybook   | Source BMS | Description                |
|-----------|-----------|----------------------------|
| COACTUP   | COACTUP.bms | Account update map fields  |
| COACTVW   | COACTVW.bms | Account view map fields    |
| COADM01   | COADM01.bms | Admin menu map fields      |
| COBIL00   | COBIL00.bms | Bill payment map fields    |
| COCRDLI   | COCRDLI.bms | Card list map fields       |
| COCRDSL   | COCRDSL.bms | Card detail map fields     |
| COCRDUP   | COCRDUP.bms | Card update map fields     |
| COMEN01   | COMEN01.bms | Main menu map fields       |
| CORPT00   | CORPT00.bms | Report request map fields  |
| COSGN00   | COSGN00.bms | Sign-on map fields         |
| COTRN00   | COTRN00.bms | Transaction list map fields|
| COTRN01   | COTRN01.bms | Transaction view map fields|
| COTRN02   | COTRN02.bms | Transaction add map fields |
| COUSR00   | COUSR00.bms | User list map fields       |
| COUSR01   | COUSR01.bms | User add map fields        |
| COUSR02   | COUSR02.bms | User update map fields     |
| COUSR03   | COUSR03.bms | User delete map fields     |

---

## BMS Screen Maps

| Map File    | Lines | Screen Name                | Associated Program | User Role |
|------------|-------|----------------------------|--------------------|-----------|
| COSGN00.bms| 210   | Sign-On Screen             | COSGN00C           | All       |
| COMEN01.bms| 167   | Main Menu                  | COMEN01C           | Regular   |
| COADM01.bms| 167   | Admin Menu                 | COADM01C           | Admin     |
| COACTVW.bms| 378   | Account View               | COACTVWC           | Regular   |
| COACTUP.bms| 512   | Account Update             | COACTUPC           | Regular   |
| COCRDLI.bms| 344   | Credit Card List           | COCRDLIC           | Regular   |
| COCRDSL.bms| 157   | Credit Card View           | COCRDSLC           | Regular   |
| COCRDUP.bms| 172   | Credit Card Update         | COCRDUPC           | Regular   |
| COTRN00.bms| 464   | Transaction List           | COTRN00C           | Regular   |
| COTRN01.bms| 273   | Transaction View           | COTRN01C           | Regular   |
| COTRN02.bms| 307   | Transaction Add            | COTRN02C           | Regular   |
| CORPT00.bms| 231   | Report Request             | CORPT00C           | Regular   |
| COBIL00.bms| 141   | Bill Payment               | COBIL00C           | Regular   |
| COUSR00.bms| 463   | User List (Admin)          | COUSR00C           | Admin     |
| COUSR01.bms| 164   | User Add (Admin)           | COUSR01C           | Admin     |
| COUSR02.bms| 169   | User Update (Admin)        | COUSR02C           | Admin     |
| COUSR03.bms| 153   | User Delete (Admin)        | COUSR03C           | Admin     |

---

## JCL Batch Jobs

### Data Refresh Jobs (Load/Reload VSAM files from sequential data)

| JCL Job     | Lines | Description                                | Programs Executed | Key Datasets |
|------------|-------|--------------------------------------------|-------------------|-------------|
| ACCTFILE   | 65    | Refresh account master VSAM                | IDCAMS             | ACCTDATA.VSAM.KSDS |
| CARDFILE   | 128   | Refresh card data VSAM                     | IDCAMS             | CARDDATA.VSAM.KSDS |
| CUSTFILE   | 84    | Refresh customer data VSAM                 | IDCAMS             | CUSTDATA.VSAM.KSDS |
| XREFFILE   | 106   | Load card cross-reference VSAM             | IDCAMS             | CARDXREF.VSAM.KSDS |
| TRANFILE   | 125   | Load transaction master VSAM               | IDCAMS, SDSF       | TRANSACT.VSAM.KSDS |
| DUSRSECJ   | 92    | Load user security VSAM                    | IDCAMS             | USRSEC.VSAM.KSDS |
| REPTFILE   | 32    | Define/load report file                    | IDCAMS             | Report datasets |
| DEFCUST    | 47    | Define customer VSAM cluster               | IDCAMS             | CUSTDATA.VSAM.KSDS |

### Batch Processing Jobs (Core nightly cycle)

| JCL Job     | Lines | Description                                | Programs Executed  | Key Datasets |
|------------|-------|--------------------------------------------|-------------------|-------------|
| CLOSEFIL   | 34    | Close CICS files for batch processing      | DFHCSDUP           | CICS files |
| POSTTRAN   | 45    | Post daily transactions                    | CBTRN02C           | DALYTRAN, TRANSACT, XREF |
| INTCALC    | 44    | Calculate interest on accounts             | CBACT04C           | TCATBALF, DISCGRP, TRANSACT |
| TRANBKP    | 71    | Backup transaction file (GDG)             | REPROC.prc, IDCAMS | TRANSACT.BKUP(+1) |
| COMBTRAN   | 52    | Combine daily + master transactions        | CBTRN01C           | DALYTRAN, TRANSACT |
| CREASTMT   | 97    | Create account statements (text + HTML)    | SORT, IDCAMS, CBSTM03A | STATEMNT.HTML, STATEMNT.PS |
| TRANIDX    | 58    | Define alternate indexes on transactions   | IDCAMS             | TRANSACT AIX |
| OPENFIL    | 34    | Reopen CICS files after batch              | DFHCSDUP           | CICS files |

### Reporting Jobs

| JCL Job     | Lines | Description                                | Programs Executed  | Key Datasets |
|------------|-------|--------------------------------------------|-------------------|-------------|
| TRANREPT   | 84    | Transaction detail report (sort + print)   | REPROC.prc, SORT, CBTRN03C | TRANSACT.DALY(+1) |
| DALYREJS   | 32    | Daily rejection report                     | SORT/Utility       | DALYREJE |
| PRTCATBL   | 66    | Print catalog balance report               | Utility            | TCATBALF.REPT |
| TXT2PDF1   | 41    | Convert text statements to PDF             | IKJEFT1B, TXT2PDF  | STATEMNT.PS |

### Data Read/Verification Jobs

| JCL Job     | Lines | Description                      | Programs Executed | Key Datasets |
|------------|-------|----------------------------------|-------------------|-------------|
| READACCT   | 50    | Read/dump account VSAM file      | CBACT01C          | ACCTDATA.VSAM.KSDS |
| READCARD   | 31    | Read/dump card data file         | CBACT02C          | CARDDATA.VSAM.KSDS |
| READCUST   | 30    | Read/dump customer data file     | CBCUS01C          | CUSTDATA.VSAM.KSDS |
| READXREF   | 31    | Read/dump card cross-reference   | CBACT03C          | CARDXREF.VSAM.KSDS |

### Data Export/Import Jobs

| JCL Job     | Lines | Description                      | Programs Executed | Key Datasets |
|------------|-------|----------------------------------|-------------------|-------------|
| CBEXPORT   | 72    | Export data for branch migration  | CBEXPORT          | Multi-file export |
| CBIMPORT   | 68    | Import data from export file      | CBIMPORT          | Multi-file import |

### Infrastructure / VSAM Definition Jobs

| JCL Job     | Lines | Description                              | Programs Executed |
|------------|-------|------------------------------------------|-------------------|
| DEFGDGB    | 63    | Define GDG bases (backup datasets)       | IDCAMS            |
| DEFGDGD    | 94    | Define GDG bases (daily datasets)        | IDCAMS            |
| ESDSRRDS   | 124   | Define ESDS/RRDS VSAM clusters           | IDCAMS            |
| TCATBALF   | 65    | Define/load transaction category balance | IDCAMS            |
| TRANCATG   | 65    | Define/load transaction category types   | IDCAMS            |
| TRANTYPE   | 65    | Define/load transaction type codes       | IDCAMS            |
| DISCGRP    | 65    | Define/load disclosure group file        | IDCAMS            |
| WAITSTEP   | 27    | Wait step (uses COBSWAIT)               | COBSWAIT          |

### Administrative / Utility Jobs

| JCL Job     | Lines | Description                              | Programs Executed |
|------------|-------|------------------------------------------|-------------------|
| CBADMCDJ  | 167   | CardDemo admin job (compile/link)        | Compiler/Linker   |
| FTPJCL    | 42    | FTP file transfer job                    | FTP               |
| INTRDRJ1  | 19    | Internal reader job (chain submission)   | IDCAMS, IEBGENER  |
| INTRDRJ2  | 14    | Internal reader job (chained)            | IDCAMS            |

---

## Assembler Programs

| Program     | Location   | Description                     |
|------------|------------|---------------------------------|
| MVSWAIT    | `app/asm/` | Wait/delay utility (called by COBSWAIT) |
| COBDATFT   | `app/asm/` | Date format translation utility |

---

## Optional Modules

### 1. Authorization Module (IMS/DB2/MQ)

**Location**: `app/app-authorization-ims-db2-mq/`

| Program    | Type    | Description                                |
|-----------|---------|-------------------------------------------|
| COPAUA0C  | Online  | MQ trigger - authorization request handler |
| COPAUS0C  | Online  | Authorization summary view                 |
| COPAUS1C  | Online  | Authorization detail view                  |
| COPAUS2C  | Online  | Fraud marking (writes to DB2)              |
| CBPAUP0C  | Batch   | Batch purge of old authorizations          |
| PAUDBLOD  | Batch   | Load authorization DB2 tables              |
| PAUDBUNL  | Batch   | Unload authorization DB2 tables            |
| DBUNLDGS  | Batch   | Generic DB2 unload utility                 |

### 2. Transaction Type DB2 Module

**Location**: `app/app-transaction-type-db2/`

| Program    | Type    | Description                                |
|-----------|---------|-------------------------------------------|
| COTRTLIC  | Online  | Transaction type list/delete (DB2 cursor)  |
| COTRTUPC  | Online  | Transaction type add/edit (DB2 CRUD)       |
| COBTUPDT  | Batch   | Batch transaction type update              |

### 3. VSAM-MQ Module

**Location**: `app/app-vsam-mq/`

| Program    | Type    | Description                                |
|-----------|---------|-------------------------------------------|
| CODATE01  | Online  | System date inquiry (MQ request/response)  |
| COACCT01  | Online  | Account inquiry via MQ                     |

---

## Supporting Artifacts

### JCL Procedures (`app/proc/`)

| Procedure   | Description                              |
|------------|------------------------------------------|
| REPROC.prc | Reusable procedure for sequential copy   |
| TRANREPT.prc | Transaction report procedure           |

### CICS Resource Definitions (`app/csd/`)

| File           | Description                             |
|---------------|------------------------------------------|
| CARDDEMO.CSD  | CICS System Definition file (programs, transactions, files, TDQs) |

### Job Scheduler Configurations (`app/scheduler/`)

| File                | Description                       |
|--------------------|-----------------------------------|
| CardDemo.ca7       | CA-7 job scheduling definitions   |
| CardDemo.controlm  | Control-M job scheduling definitions |

### Sample Data (`app/data/`)

| Directory   | Description                              |
|------------|------------------------------------------|
| ASCII/     | 9 sample data files in ASCII format (acctdata, carddata, custdata, cardxref, dailytran, tcatbal, discgrp, trancatg, trantype) |
| EBCDIC/    | Same data files in EBCDIC for mainframe upload |

### Naming Conventions

| Prefix | Meaning                                    |
|--------|-------------------------------------------|
| CO*    | Online CICS program                        |
| CB*    | Batch COBOL program                        |
| CS*    | Shared utility (copybook or program)       |
| CV*    | Copybook - VSAM data record layout         |
| CO*Y   | Copybook - online communication/UI         |
| CS*Y   | Copybook - shared utility data             |
