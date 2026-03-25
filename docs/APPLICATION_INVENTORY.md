# APPLICATION INVENTORY - CardDemo COBOL Application

> **Generated**: 2026-03-25 | **Application**: CardDemo - Credit Card Management System
> **Platform**: IBM z/OS Mainframe | COBOL / CICS / VSAM / JCL

---

## 1. Executive Summary

CardDemo is a mainframe-based credit card management application consisting of **31 core COBOL programs**, **30 copybooks**, **38 JCL jobs**, and **17 BMS screen maps**, plus **13 optional-module programs** for IMS/DB2/MQ integrations. The system supports online transaction processing (OLTP) via CICS and batch processing via JCL-scheduled jobs.

| Asset Type          | Core Count | Optional Modules | Total |
|---------------------|-----------|------------------|-------|
| COBOL Programs      | 31        | 13               | 44    |
| Copybooks           | 30        | (module-specific) | 30+   |
| JCL Jobs            | 38        | (module-specific) | 38+   |
| BMS Screen Maps     | 17        | (module-specific) | 17+   |
| BMS Copybooks       | 17        | --               | 17    |
| Assembler Programs  | 2         | --               | 2     |

---

## 2. COBOL Programs (Core - `app/cbl/`)

### 2.1 Online CICS Programs (Prefix: `CO*`)

| # | Program     | Lines | CICS Trans | Description                        | Domain           | Classification   |
|---|-------------|-------|------------|------------------------------------|------------------|------------------|
| 1 | COSGN00C    | 260   | CC00       | User Sign-On / Authentication      | Security         | Online - Auth    |
| 2 | COMEN01C    | 308   | CM00       | Main Menu Navigation               | Navigation       | Online - Menu    |
| 3 | COADM01C    | 288   | CA00       | Admin Menu                         | Administration   | Online - Menu    |
| 4 | COACTVWC    | 941   | CA01       | Account View (read-only)           | Account Mgmt     | Online - Inquiry |
| 5 | COACTUPC    | 4,236 | CA02       | Account Update                     | Account Mgmt     | Online - Update  |
| 6 | COCRDLIC    | 1,459 | CC01       | Credit Card List                   | Card Mgmt        | Online - List    |
| 7 | COCRDSLC    | 887   | CC02       | Credit Card Detail View            | Card Mgmt        | Online - Inquiry |
| 8 | COCRDUPC    | 1,560 | CC03       | Credit Card Update                 | Card Mgmt        | Online - Update  |
| 9 | COTRN00C    | 699   | CT00       | Transaction List                   | Transactions     | Online - List    |
|10 | COTRN01C    | 330   | CT01       | Transaction Detail View            | Transactions     | Online - Inquiry |
|11 | COTRN02C    | 783   | CT02       | Transaction Add                    | Transactions     | Online - Add     |
|12 | CORPT00C    | 649   | CR00       | Transaction Report Request         | Reporting        | Online - Report  |
|13 | COBIL00C    | 572   | CB00       | Bill Payment                       | Payments         | Online - Update  |
|14 | COUSR00C    | 695   | CU00       | User List (Admin)                  | User Mgmt        | Online - List    |
|15 | COUSR01C    | 299   | CU01       | User Add (Admin)                   | User Mgmt        | Online - Add     |
|16 | COUSR02C    | 414   | CU02       | User Update (Admin)                | User Mgmt        | Online - Update  |
|17 | COUSR03C    | 359   | CU03       | User Delete (Admin)                | User Mgmt        | Online - Delete  |

### 2.2 Batch Programs (Prefix: `CB*`)

| # | Program     | Lines | Description                                  | Domain            | Classification    |
|---|-------------|-------|----------------------------------------------|-------------------|-------------------|
|18 | CBTRN01C    | 494   | Daily Transaction Validation                 | Transactions      | Batch - Validate  |
|19 | CBTRN02C    | 731   | Transaction Posting (daily-to-master)        | Transactions      | Batch - Posting   |
|20 | CBTRN03C    | 649   | Daily Transaction Report Generation          | Reporting         | Batch - Report    |
|21 | CBACT01C    | 430   | Account File Read / Export to multiple formats| Account Mgmt     | Batch - Extract   |
|22 | CBACT02C    | 178   | Card File Reader / Display                   | Card Mgmt         | Batch - Utility   |
|23 | CBACT03C    | 178   | Cross-Reference File Reader / Display        | Account Mgmt      | Batch - Utility   |
|24 | CBACT04C    | 652   | Interest Calculation & Account Update        | Financial Calc    | Batch - Calc      |
|25 | CBSTM03A    | 924   | Statement Generation (main driver)           | Statements        | Batch - Report    |
|26 | CBSTM03B    | 230   | Statement Generation (file I/O subroutine)   | Statements        | Batch - Subroutine|
|27 | CBCUS01C    | 178   | Customer File Reader / Display               | Customer Mgmt     | Batch - Utility   |
|28 | CBEXPORT    | 582   | Multi-entity Data Export                     | Data Migration    | Batch - Export    |
|29 | CBIMPORT    | 487   | Multi-entity Data Import                     | Data Migration    | Batch - Import    |
|30 | COBSWAIT    | 41    | Wait/Sleep Utility                           | Infrastructure    | Batch - Utility   |

### 2.3 Shared Utility Programs

| # | Program     | Lines | Description                              | Domain           | Classification   |
|---|-------------|-------|------------------------------------------|------------------|------------------|
|31 | CSUTLDTC    | 157   | Date Validation (calls CEEDAYS)          | Utility          | Shared - Utility |

### 2.4 Assembler Programs (`app/asm/`)

| # | Program     | Description                              |
|---|-------------|------------------------------------------|
| 1 | MVSWAIT     | MVS Wait macro implementation            |
| 2 | COBDATFT    | Date formatting utility (called by COBOL)|

---

## 3. Optional Module Programs

### 3.1 Authorization Module - IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program     | Lines  | Description                                   | Technologies        |
|---|-------------|--------|-----------------------------------------------|---------------------|
| 1 | COPAUA0C    | 1,026  | MQ Trigger - Authorization request processing | CICS + MQ + IMS DLI |
| 2 | COPAUS0C    | 1,032  | Authorization Summary Screen                  | CICS + IMS DLI      |
| 3 | COPAUS1C    | 604    | Authorization Detail Screen                   | CICS + IMS DLI      |
| 4 | COPAUS2C    | 244    | Fraud Marking (write to DB2)                  | CICS + DB2          |
| 5 | CBPAUP0C    | 386    | Batch Purge of Authorizations                 | Batch + IMS DLI     |
| 6 | PAUDBLOD    | --     | IMS DB Load utility                           | IMS DLI             |
| 7 | PAUDBUNL    | --     | IMS DB Unload utility                         | IMS DLI             |
| 8 | DBUNLDGS    | --     | IMS DB Unload (GS variant)                    | IMS DLI             |

### 3.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program     | Lines  | Description                                  | Technologies   |
|---|-------------|--------|----------------------------------------------|----------------|
| 1 | COTRTUPC    | 1,702  | Transaction Type Add/Edit (Online)           | CICS + DB2     |
| 2 | COTRTLIC    | 2,098  | Transaction Type List/Delete (Online)        | CICS + DB2     |
| 3 | COBTUPDT    | 237    | Transaction Type Batch Update                | Batch + DB2    |

### 3.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program     | Lines  | Description                                  | Technologies   |
|---|-------------|--------|----------------------------------------------|----------------|
| 1 | COACCT01    | 620    | Account Inquiry via MQ Request/Response      | CICS + MQ      |
| 2 | CODATE01    | 524    | System Date via MQ Request/Response          | CICS + MQ      |

---

## 4. Copybooks (`app/cpy/`)

### 4.1 Data Structure Copybooks (Prefix: `CV*`)

| # | Copybook    | Lines | Record Len | Description                                |
|---|-------------|-------|------------|--------------------------------------------|
| 1 | CVACT01Y    | 20    | 300 bytes  | Account Master Record                      |
| 2 | CVACT02Y    | 14    | 150 bytes  | Card Master Record                         |
| 3 | CVACT03Y    | 11    | 50 bytes   | Card-to-Account Cross-Reference            |
| 4 | CVCUS01Y    | 26    | 500 bytes  | Customer Master Record                     |
| 5 | CVCRD01Y    | 46    | --         | Card Data Structure (extended)             |
| 6 | CVTRA01Y    | 13    | 50 bytes   | Transaction Category Balance               |
| 7 | CVTRA02Y    | 13    | 50 bytes   | Disclosure Group Record                    |
| 8 | CVTRA03Y    | 10    | 60 bytes   | Transaction Type Record                    |
| 9 | CVTRA04Y    | 12    | 60 bytes   | Transaction Category Type Record           |
|10 | CVTRA05Y    | 21    | 350 bytes  | Transaction Master Record (online)         |
|11 | CVTRA06Y    | 21    | 350 bytes  | Daily Transaction Record                   |
|12 | CVTRA07Y    | 73    | --         | Transaction Report Layout                  |
|13 | CVEXPORT    | 103   | --         | Export/Import Record Layout                |

### 4.2 Common / Infrastructure Copybooks (Prefix: `CO*`, `CS*`)

| # | Copybook    | Lines | Description                                    |
|---|-------------|-------|------------------------------------------------|
|14 | COCOM01Y    | 47    | CICS Common Communication Area (COMMAREA)      |
|15 | COMEN02Y    | 101   | Menu Option Definitions                        |
|16 | COADM02Y    | 62    | Admin Menu Definitions                         |
|17 | CODATECN    | 52    | Date Conversion Control Block                  |
|18 | COTTL01Y    | 27    | Screen Title / Header Constants                |
|19 | COSTM01     | 38    | Statement Transaction Altered Layout           |
|20 | CUSTREC     | 26    | Customer Record (alternate layout)             |
|21 | CSUSR01Y    | 26    | User Security Record                           |
|22 | CSDAT01Y    | 58    | Date Handling Working-Storage                  |
|23 | CSMSG01Y    | 24    | Message Constants (set 1)                      |
|24 | CSMSG02Y    | 35    | Message Constants (set 2)                      |
|25 | CSLKPCDY    | 1,318 | Lookup Code Tables (large reference data)      |
|26 | CSSETATY    | 30    | Screen Attribute Setting                       |
|27 | CSSTRPFY    | 85    | String Processing Functions                    |
|28 | CSUTLDPY    | 375   | Utility Data Parameters                        |
|29 | CSUTLDWY    | 89    | Utility Working-Storage Variables              |
|30 | UNUSED1Y    | 10    | Unused / Deprecated Placeholder                |

### 4.3 BMS-Generated Copybooks (`app/cpy-bms/`)

| # | Copybook    | Source Map   | Description                  |
|---|-------------|--------------|------------------------------|
| 1 | COACTUP.CPY | COACTUP.bms  | Account Update Screen I/O    |
| 2 | COACTVW.CPY | COACTVW.bms  | Account View Screen I/O      |
| 3 | COADM01.CPY | COADM01.bms  | Admin Menu Screen I/O        |
| 4 | COBIL00.CPY | COBIL00.bms  | Bill Payment Screen I/O      |
| 5 | COCRDLI.CPY | COCRDLI.bms  | Card List Screen I/O         |
| 6 | COCRDSL.CPY | COCRDSL.bms  | Card Detail Screen I/O       |
| 7 | COCRDUP.CPY | COCRDUP.bms  | Card Update Screen I/O       |
| 8 | COMEN01.CPY | COMEN01.bms  | Main Menu Screen I/O         |
| 9 | CORPT00.CPY | CORPT00.bms  | Report Request Screen I/O    |
|10 | COSGN00.CPY | COSGN00.bms  | Sign-On Screen I/O           |
|11 | COTRN00.CPY | COTRN00.bms  | Transaction List Screen I/O  |
|12 | COTRN01.CPY | COTRN01.bms  | Transaction View Screen I/O  |
|13 | COTRN02.CPY | COTRN02.bms  | Transaction Add Screen I/O   |
|14 | COUSR00.CPY | COUSR00.bms  | User List Screen I/O         |
|15 | COUSR01.CPY | COUSR01.bms  | User Add Screen I/O          |
|16 | COUSR02.CPY | COUSR02.bms  | User Update Screen I/O       |
|17 | COUSR03.CPY | COUSR03.bms  | User Delete Screen I/O       |

---

## 5. BMS Screen Maps (`app/bms/`)

| # | Map File    | Mapset   | Map Name  | Screen Function              | User Role |
|---|-------------|----------|-----------|------------------------------|-----------|
| 1 | COSGN00.bms | COSGN00 | COSGN0A   | Sign-On                     | All       |
| 2 | COMEN01.bms | COMEN01 | COMEN1A   | Main Menu                   | All       |
| 3 | COADM01.bms | COADM01 | COADM1A   | Admin Menu                  | Admin     |
| 4 | COACTVW.bms | COACTVW | CACTVWA   | Account View                | User      |
| 5 | COACTUP.bms | COACTUP | CACTUPA   | Account Update              | User      |
| 6 | COCRDLI.bms | COCRDLI | CCRDLIA   | Card List                   | User      |
| 7 | COCRDSL.bms | COCRDSL | CCRDSIA   | Card Detail View            | User      |
| 8 | COCRDUP.bms | COCRDUP | CCRDUPA   | Card Update                 | User      |
| 9 | COTRN00.bms | COTRN00 | COTRN0A   | Transaction List            | User      |
|10 | COTRN01.bms | COTRN01 | COTRN1A   | Transaction View            | User      |
|11 | COTRN02.bms | COTRN02 | COTRN2A   | Transaction Add             | User      |
|12 | CORPT00.bms | CORPT00 | CORPT0A   | Report Request              | User      |
|13 | COBIL00.bms | COBIL00 | COBIL0A   | Bill Payment                | User      |
|14 | COUSR00.bms | COUSR00 | COUSR0A   | User List                   | Admin     |
|15 | COUSR01.bms | COUSR01 | COUSR1A   | User Add                    | Admin     |
|16 | COUSR02.bms | COUSR02 | COUSR2A   | User Update                 | Admin     |
|17 | COUSR03.bms | COUSR03 | COUSR3A   | User Delete                 | Admin     |

---

## 6. JCL Jobs (`app/jcl/`)

### 6.1 Data File Management

| # | Job         | Description                              | Category           |
|---|-------------|------------------------------------------|-------------------|
| 1 | ACCTFILE    | Refresh Account Master VSAM file         | Data Refresh       |
| 2 | CARDFILE    | Refresh Card Master VSAM file            | Data Refresh       |
| 3 | CUSTFILE    | Refresh Customer Master VSAM file        | Data Refresh       |
| 4 | XREFFILE    | Load Card Cross-Reference VSAM + AIX     | Data Refresh       |
| 5 | TRANFILE    | Load Transaction Master VSAM file        | Data Refresh       |
| 6 | DUSRSECJ    | Load User Security VSAM file             | Data Refresh       |
| 7 | DEFCUST     | Define Customer VSAM Cluster             | VSAM Definition    |
| 8 | REPTFILE    | Define Report VSAM file                  | VSAM Definition    |

### 6.2 Batch Processing Cycle

| # | Job         | Description                                      | Category        |
|---|-------------|--------------------------------------------------|-----------------|
| 9 | CLOSEFIL    | Close CICS-managed VSAM files for batch          | Batch Cycle     |
|10 | OPENFIL     | Re-open CICS-managed VSAM files after batch      | Batch Cycle     |
|11 | POSTTRAN    | Post daily transactions (runs CBTRN01C+CBTRN02C) | Batch - Core    |
|12 | INTCALC     | Calculate interest (runs CBACT04C)               | Batch - Core    |
|13 | COMBTRAN    | Combine transaction files                        | Batch - Merge   |
|14 | CREASTMT    | Generate statements (runs CBSTM03A)              | Batch - Report  |
|15 | TRANBKP     | Backup transaction master file                   | Batch - Backup  |
|16 | TRANIDX     | Define/build alternate index on transactions     | Batch - Index   |

### 6.3 Reporting & Catalog

| # | Job         | Description                                  | Category        |
|---|-------------|----------------------------------------------|-----------------|
|17 | TRANREPT    | Generate daily transaction report (CBTRN03C) | Reporting       |
|18 | TRANCATG    | Catalog transaction type/category files      | Data Catalog    |
|19 | TCATBALF    | Load transaction category balance file       | Data Load       |
|20 | DALYREJS    | Define daily rejects VSAM file               | Data Definition |
|21 | TRANTYPE    | Load transaction type reference file         | Data Load       |
|22 | PRTCATBL    | Print catalog listing                        | Utility         |
|23 | TXT2PDF1    | Convert text statements to PDF               | Utility         |

### 6.4 GDG & Data Management

| # | Job         | Description                                  | Category        |
|---|-------------|----------------------------------------------|-----------------|
|24 | DEFGDGB     | Define GDG base for backups                  | GDG Management  |
|25 | DEFGDGD     | Define GDG base for daily data               | GDG Management  |
|26 | DISCGRP     | Load disclosure group data                   | Data Load       |
|27 | ESDSRRDS    | Define ESDS/RRDS VSAM files                  | VSAM Definition |

### 6.5 Data Reader / Verification Jobs

| # | Job         | Description                                  | Category        |
|---|-------------|----------------------------------------------|-----------------|
|28 | READACCT    | Read/verify account file (CBACT01C)          | Verification    |
|29 | READCARD    | Read/verify card file (CBACT02C)             | Verification    |
|30 | READCUST    | Read/verify customer file (CBCUS01C)         | Verification    |
|31 | READXREF    | Read/verify cross-reference file (CBACT03C)  | Verification    |

### 6.6 Export / Import & FTP

| # | Job         | Description                                  | Category        |
|---|-------------|----------------------------------------------|-----------------|
|32 | CBEXPORT    | Export all data entities to flat file         | Data Migration  |
|33 | CBIMPORT    | Import data from export file                  | Data Migration  |
|34 | FTPJCL      | FTP file transfer job                         | File Transfer   |

### 6.7 Infrastructure / Admin

| # | Job         | Description                                  | Category        |
|---|-------------|----------------------------------------------|-----------------|
|35 | CBADMCDJ    | Admin batch job                              | Admin           |
|36 | WAITSTEP    | Wait/pause step (uses COBSWAIT)              | Utility         |
|37 | INTRDRJ1    | Internal reader - trigger chain job 1        | Job Scheduling  |
|38 | INTRDRJ2    | Internal reader - trigger chain job 2        | Job Scheduling  |

---

## 7. Batch Processing Cycle Order

The canonical batch cycle runs in this sequence:

```
CLOSEFIL          (Close CICS files)
   |
   v
ACCTFILE          \
CARDFILE           |  Data Refresh
CUSTFILE           |  (parallel OK)
XREFFILE           |
TRANFILE          /
DUSRSECJ         /
   |
   v
POSTTRAN          (CBTRN01C -> CBTRN02C: Validate & Post daily transactions)
   |
   v
INTCALC           (CBACT04C: Interest calculation & account updates)
   |
   v
TRANBKP           (Backup transaction master)
   |
   v
COMBTRAN          (Merge transaction files)
   |
   v
CREASTMT          (CBSTM03A -> CBSTM03B: Statement generation)
   |
   v
TRANREPT          (CBTRN03C: Daily transaction report)
   |
   v
TRANIDX           (Rebuild alternate indexes)
   |
   v
OPENFIL           (Re-open CICS files)
```

---

## 8. Naming Conventions

| Prefix | Meaning                              | Example    |
|--------|--------------------------------------|------------|
| CO*    | Online CICS program                  | COSGN00C   |
| CB*    | Batch COBOL program                  | CBTRN02C   |
| CS*    | Common/Shared utility copybook       | CSUTLDTC   |
| CV*    | Copybook - VSAM record layout        | CVACT01Y   |
| *Y     | Copybook (data definition) suffix    | CVACT01Y   |
| *C     | COBOL program suffix                 | COACTUPC   |

---

## 9. Technology Stack Summary

| Layer              | Technology                    |
|--------------------|-------------------------------|
| Language           | COBOL 85 / Enterprise COBOL   |
| TP Monitor         | CICS TS                       |
| Data Storage       | VSAM KSDS / ESDS / RRDS      |
| Screen Maps        | BMS (3270 Terminal)           |
| Batch Scheduler    | JCL / CA7 / Control-M        |
| Optional: Database | DB2                           |
| Optional: Messaging| IBM MQ                        |
| Optional: IMS      | IMS DB (DL/I)                 |
| Assembler          | HLASM (utilities)             |
