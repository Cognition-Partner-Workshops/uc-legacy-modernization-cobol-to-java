# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> CardDemo is a COBOL/CICS/VSAM mainframe application that simulates credit card account management,
> transaction processing, bill payments, and reporting. It supports two user roles: **Regular** (card
> operations) and **Admin** (user/transaction-type management).

---

## Summary Counts

| Artifact Type        | Count | Location              |
|----------------------|------:|-----------------------|
| COBOL Programs (core)| 31    | `app/cbl/`            |
| Copybooks (data)     | 30    | `app/cpy/`            |
| Copybooks (BMS-gen)  | 17    | `app/cpy-bms/`        |
| BMS Screen Maps      | 17    | `app/bms/`            |
| JCL Jobs             | 38    | `app/jcl/`            |
| Assembler Programs   |  2    | `app/asm/`            |
| JCL Procedures       |  2    | `app/proc/`           |
| **Optional Modules** |       |                       |
| Auth IMS/DB2/MQ Pgms |  5    | `app/app-authorization-ims-db2-mq/cbl/` |
| Tran-Type DB2 Pgms   |  3    | `app/app-transaction-type-db2/cbl/`      |
| VSAM-MQ Pgms         |  2    | `app/app-vsam-mq/cbl/`                  |

**Grand Total: 31 core programs + 10 optional = 41 COBOL programs, 47 copybooks, 17 BMS maps, 38 JCL jobs**

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program    | Lines | CICS Tran | Function                         | Classification        | BMS Map     | Key VSAM Files                 |
|---|------------|------:|-----------|----------------------------------|-----------------------|-------------|-------------------------------|
| 1 | COSGN00C   |   260 | CC00      | User Sign-On                     | Security / Auth       | COSGN00     | USRSEC                        |
| 2 | COMEN01C   |   308 | CM00      | Main Menu                        | Navigation            | COMEN01     | —                             |
| 3 | COADM01C   |   288 | CA00      | Admin Menu                       | Navigation (Admin)    | COADM01     | —                             |
| 4 | COACTVWC   |   941 | —         | Account View                     | Account Mgmt          | COACTVW     | ACCTDAT, CARDXREF, CUSTDAT    |
| 5 | COACTUPC   | 4,236 | —         | Account Update                   | Account Mgmt          | COACTUP     | ACCTDAT, CARDXREF, CUSTDAT    |
| 6 | COCRDLIC   | 1,459 | —         | Card List                        | Card Mgmt             | COCRDLI     | CARDDAT, CARDAIX              |
| 7 | COCRDSLC   |   887 | —         | Card Detail View                 | Card Mgmt             | COCRDSL     | CARDDAT, CUSTDAT              |
| 8 | COCRDUPC   | 1,560 | —         | Card Update                      | Card Mgmt             | COCRDUP     | CARDDAT, CUSTDAT              |
| 9 | COTRN00C   |   699 | —         | Transaction List                 | Transaction Mgmt      | COTRN00     | TRANSACT, CARDXREF            |
|10 | COTRN01C   |   330 | —         | Transaction Detail View          | Transaction Mgmt      | COTRN01     | TRANSACT                      |
|11 | COTRN02C   |   783 | —         | Transaction Add                  | Transaction Mgmt      | COTRN02     | TRANSACT, CARDXREF, ACCTDAT   |
|12 | CORPT00C   |   649 | —         | Transaction Report Selector      | Reporting             | CORPT00     | TRANSACT                      |
|13 | COBIL00C   |   572 | —         | Bill Payment                     | Payments              | COBIL00     | ACCTDAT, TRANSACT, CARDXREF   |
|14 | COUSR00C   |   695 | —         | User List                        | User Admin (Admin)    | COUSR00     | USRSEC                        |
|15 | COUSR01C   |   299 | —         | User Add                         | User Admin (Admin)    | COUSR01     | USRSEC                        |
|16 | COUSR02C   |   414 | —         | User Update                      | User Admin (Admin)    | COUSR02     | USRSEC                        |
|17 | COUSR03C   |   359 | —         | User Delete                      | User Admin (Admin)    | COUSR03     | USRSEC                        |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program    | Lines | Function                              | Classification       | Key Files                                        |
|---|------------|------:|---------------------------------------|----------------------|--------------------------------------------------|
|18 | CBACT01C   |   430 | Account Data Extract (multi-format)   | Data Extraction      | ACCTDAT (in), OUTFILE/ARRFILE/VBRFILE (out)      |
|19 | CBACT02C   |   178 | Card Data Extract                     | Data Extraction      | CARDDAT                                          |
|20 | CBACT03C   |   178 | Cross-Reference Extract               | Data Extraction      | CARDXREF                                         |
|21 | CBACT04C   |   652 | Interest Calculation                  | Financial Processing | TCATBALF, CARDXREF, DISCGRP, ACCTDAT, TRANSACT   |
|22 | CBCUS01C   |   178 | Customer Data Extract                 | Data Extraction      | CUSTDAT                                          |
|23 | CBTRN01C   |   494 | Daily Transaction Validation          | Transaction Posting  | DALYTRAN, CUSTDAT, CARDXREF, CARDDAT, ACCTDAT, TRANSACT |
|24 | CBTRN02C   |   731 | Transaction Posting                   | Transaction Posting  | DALYTRAN, TRANSACT, CARDXREF, DALYREJS, ACCTDAT, TCATBALF |
|25 | CBTRN03C   |   649 | Transaction Report Generator          | Reporting            | TRANSACT, REPTFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM |
|26 | CBSTM03A   |   924 | Statement Generation (driver)         | Reporting            | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE, STMTFILE, HTMLFILE |
|27 | CBSTM03B   |   230 | Statement Generation (I/O sub)        | Reporting (sub)      | called by CBSTM03A                               |
|28 | CBEXPORT   |   582 | Full Data Export                      | Data Migration       | CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT, EXPFILE |
|29 | CBIMPORT   |   487 | Full Data Import                      | Data Migration       | CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT, EXPFILE |
|30 | COBSWAIT   |    41 | Wait Utility (calls ASM MVSWAIT)      | Utility              | —                                                |

### 1.3 Utility Programs

| # | Program    | Lines | Function                              | Classification       |
|---|------------|------:|---------------------------------------|----------------------|
|31 | CSUTLDTC   |   157 | Date Conversion Utility (CEEDAYS)     | Shared Utility       |

---

## 2. Optional Module Programs

### 2.1 Authorization — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program    | Lines | Function                              | Classification       | Tech           |
|---|------------|------:|---------------------------------------|----------------------|----------------|
| 1 | COPAUA0C   | 1,026 | Authorization MQ Trigger / Processor  | Auth Processing      | CICS + MQ      |
| 2 | COPAUS0C   | 1,032 | Auth Summary Screen                   | Auth UI              | CICS + VSAM    |
| 3 | COPAUS1C   |   604 | Auth Detail Screen                    | Auth UI              | CICS           |
| 4 | COPAUS2C   |   244 | Fraud Marking (writes to DB2)         | Auth Processing      | CICS + DB2     |
| 5 | CBPAUP0C   |   386 | Batch Auth Purge                      | Auth Maintenance     | Batch          |

### 2.2 Transaction Type — DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program    | Lines | Function                              | Classification       | Tech           |
|---|------------|------:|---------------------------------------|----------------------|----------------|
| 6 | COTRTLIC   | 2,098 | Transaction Type List/Delete          | Reference Data Admin | CICS + DB2     |
| 7 | COTRTUPC   | 1,702 | Transaction Type Add/Edit             | Reference Data Admin | CICS + DB2     |
| 8 | COBTUPDT   |   237 | Batch Transaction Type Update         | Reference Data Admin | Batch + DB2    |

### 2.3 VSAM-MQ Integration (`app/app-vsam-mq/cbl/`)

| # | Program    | Lines | Function                              | Classification       | Tech           |
|---|------------|------:|---------------------------------------|----------------------|----------------|
| 9 | COACCT01   |   620 | MQ Account Inquiry (request/reply)    | Integration          | CICS + MQ + VSAM |
|10 | CODATE01   |   524 | MQ System Date Service                | Integration          | CICS + MQ        |

---

## 3. Copybooks — Data Structures (`app/cpy/`)

| # | Copybook   | Record Length | Description                          | Used By (Key Programs)                |
|---|------------|:------------:|--------------------------------------|---------------------------------------|
| 1 | CVACT01Y   |     300      | Account Master Record                | CBACT01C, CBACT04C, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT, COBIL00C |
| 2 | CVACT02Y   |     150      | Card Master Record                   | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT |
| 3 | CVACT03Y   |      50      | Card Cross-Reference Record          | CBACT03C, CBACT04C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C |
| 4 | CVCUS01Y   |     500      | Customer Master Record               | CBCUS01C, COCRDSLC, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT |
| 5 | CVCRD01Y   |      —       | Card Detail Record (UI work area)    | COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC |
| 6 | CVTRA01Y   |      50      | Transaction Category Balance         | CBACT04C, CBTRN02C                   |
| 7 | CVTRA02Y   |      50      | Disclosure Group / Interest Rate     | CBACT04C                             |
| 8 | CVTRA03Y   |      60      | Transaction Type Reference           | CBTRN03C                             |
| 9 | CVTRA04Y   |      60      | Transaction Category Type            | CBTRN03C                             |
|10 | CVTRA05Y   |     350      | Transaction Record                   | CBTRN02C, CBTRN01C, CBEXPORT, CBIMPORT, COBIL00C, COTRN00C |
|11 | CVTRA06Y   |     350      | Daily Transaction Record             | CBTRN01C, CBTRN02C                   |
|12 | CVTRA07Y   |      —       | Transaction Report Layout            | CBTRN03C                             |
|13 | CVEXPORT   |      —       | Export/Import Record Layout          | CBEXPORT, CBIMPORT                   |
|14 | COCOM01Y   |      —       | Common Communication Area            | All online programs                  |
|15 | COMEN02Y   |      —       | Menu Option Definitions              | COMEN01C                             |
|16 | COADM02Y   |      —       | Admin Menu Option Definitions        | COADM01C                             |
|17 | COTTL01Y   |      —       | Title / Header Line                  | All online programs                  |
|18 | CSUSR01Y   |      80      | User Security Record                 | COSGN00C, COUSR00C–03C, all online   |
|19 | CSDAT01Y   |      —       | Date Work Area                       | All online programs                  |
|20 | CSMSG01Y   |      —       | Message Area (primary)               | All online programs                  |
|21 | CSMSG02Y   |      —       | Message Area (secondary)             | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COTRN02C |
|22 | CSSETATY   |      —       | Set-Attribute Macro (COPY REPLACING) | COACTUPC, COCRDLIC, COCRDUPC         |
|23 | CSSTRPFY   |      —       | Abend / Error Display Routine        | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
|24 | CSLKPCDY   |      —       | Lookup Code Table                    | COACTUPC                             |
|25 | CSUTLDPY   |      —       | Date Utility Paragraph               | COACTUPC                             |
|26 | CSUTLDWY   |      —       | Date Utility Working Storage         | COACTUPC, COCRDUPC, COTRTUPC         |
|27 | CODATECN   |      —       | Date Conversion Record               | CBACT01C                             |
|28 | CUSTREC    |      —       | Customer Record (statement variant)  | CBSTM03A                             |
|29 | COSTM01    |      —       | Statement Transaction Layout         | CBSTM03A                             |
|30 | UNUSED1Y   |      80      | Unused / Placeholder Record          | None (dead code)                     |

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map File   | Map Set   | Screen        | Function                    | Associated Program |
|---|------------|-----------|---------------|-----------------------------|--------------------|
| 1 | COSGN00    | COSGN00   | COSGN0A       | Sign-On                     | COSGN00C           |
| 2 | COMEN01    | COMEN01   | COMEN1A       | Main Menu                   | COMEN01C           |
| 3 | COADM01    | COADM01   | COADM1A       | Admin Menu                  | COADM01C           |
| 4 | COACTVW    | COACTVW   | CACTVWA       | Account View                | COACTVWC           |
| 5 | COACTUP    | COACTUP   | CACTUPA       | Account Update              | COACTUPC           |
| 6 | COCRDLI    | COCRDLI   | CCRDLIA       | Card List                   | COCRDLIC           |
| 7 | COCRDSL    | COCRDSL   | CCRDSLA       | Card Detail                 | COCRDSLC           |
| 8 | COCRDUP    | COCRDUP   | CCRDUPA       | Card Update                 | COCRDUPC           |
| 9 | COTRN00    | COTRN00   | COTRN0A       | Transaction List            | COTRN00C           |
|10 | COTRN01    | COTRN01   | COTRN1A       | Transaction Detail          | COTRN01C           |
|11 | COTRN02    | COTRN02   | COTRN2A       | Transaction Add             | COTRN02C           |
|12 | CORPT00    | CORPT00   | CORPT0A       | Report Date Selection       | CORPT00C           |
|13 | COBIL00    | COBIL00   | COBIL0A       | Bill Payment                | COBIL00C           |
|14 | COUSR00    | COUSR00   | COUSR0A       | User List                   | COUSR00C           |
|15 | COUSR01    | COUSR01   | COUSR1A       | User Add                    | COUSR01C           |
|16 | COUSR02    | COUSR02   | COUSR2A       | User Update                 | COUSR02C           |
|17 | COUSR03    | COUSR03   | COUSR3A       | User Delete                 | COUSR03C           |

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data Load / Refresh Jobs

| # | Job        | Function                                     | Programs / Utilities      | Key Datasets                    |
|---|------------|----------------------------------------------|---------------------------|---------------------------------|
| 1 | DUSRSECJ   | Load User Security VSAM from flat file        | IDCAMS                    | USRSEC VSAM KSDS               |
| 2 | ACCTFILE   | Refresh Account Master VSAM                   | IDCAMS                    | ACCTDATA VSAM KSDS             |
| 3 | CARDFILE   | Refresh Card Master VSAM                      | IDCAMS                    | CARDDATA VSAM KSDS             |
| 4 | CUSTFILE   | Refresh Customer Master VSAM                  | IDCAMS                    | CUSTDATA VSAM KSDS             |
| 5 | XREFFILE   | Load Card Cross-Reference + AIX               | IDCAMS                    | CARDXREF VSAM KSDS + AIX       |
| 6 | TRANFILE   | Load Transaction Master VSAM                  | IDCAMS                    | TRANSACT VSAM KSDS             |

### 5.2 Batch Processing Cycle Jobs

| # | Job        | Function                                     | Programs / Utilities      | Key Datasets                    |
|---|------------|----------------------------------------------|---------------------------|---------------------------------|
| 7 | CLOSEFIL   | Close CICS files for batch                    | DFHCSDUP                  | CICS CSD                       |
| 8 | OPENFIL    | Open CICS files after batch                   | DFHCSDUP                  | CICS CSD                       |
| 9 | POSTTRAN   | Core transaction posting                      | CBTRN01C, CBTRN02C        | DALYTRAN, TRANSACT, ACCTDATA, CARDXREF, TCATBALF |
|10 | INTCALC    | Interest & fee calculations                   | CBACT04C                  | TCATBALF, CARDXREF, DISCGRP, ACCTDATA, TRANSACT |
|11 | COMBTRAN   | Combine daily + master transactions           | SORT, IDCAMS              | DALYTRAN, TRANSACT             |
|12 | CREASTMT   | Generate statements (text + HTML)             | SORT, CBSTM03A            | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA, STMTFILE, HTMLFILE |
|13 | TRANBKP    | Backup transaction file                       | IDCAMS                    | TRANSACT → TRANSACT.BKUP       |
|14 | TRANIDX    | Define/build alternate indexes on transactions| IDCAMS                    | TRANSACT AIX                   |
|15 | TRANREPT   | Daily transaction report                      | CBTRN03C                  | TRANSACT, REPTFILE, CARDXREF, TRANTYPE, TRANCATG |
|16 | DALYREJS   | Process daily rejected transactions           | SORT                      | DALYREJS                       |

### 5.3 GDG / VSAM Definition Jobs

| # | Job        | Function                                     | Programs / Utilities      |
|---|------------|----------------------------------------------|---------------------------|
|17 | DEFGDGB    | Define GDG base for backups                   | IDCAMS                    |
|18 | DEFGDGD    | Define GDG base for daily files               | IDCAMS                    |
|19 | DEFCUST    | Define Customer VSAM cluster                  | IDCAMS                    |
|20 | DISCGRP    | Define Disclosure Group VSAM                  | IDCAMS                    |
|21 | TCATBALF   | Define Transaction Category Balance VSAM      | IDCAMS                    |
|22 | TRANCATG   | Define Transaction Category VSAM              | IDCAMS                    |
|23 | TRANTYPE   | Define Transaction Type VSAM                  | IDCAMS                    |
|24 | ESDSRRDS   | Define ESDS/RRDS sample clusters              | IDCAMS                    |

### 5.4 Data Read / Validation Jobs

| # | Job        | Function                                     | Programs / Utilities      |
|---|------------|----------------------------------------------|---------------------------|
|25 | READACCT   | Read / print Account VSAM records             | CBACT01C                  |
|26 | READCARD   | Read / print Card VSAM records                | CBACT02C                  |
|27 | READCUST   | Read / print Customer VSAM records            | CBCUS01C                  |
|28 | READXREF   | Read / print Cross-Reference VSAM records     | CBACT03C                  |
|29 | REPTFILE   | Define Report output file                     | IDCAMS                    |
|30 | PRTCATBL   | Print catalog / define catalog listing         | IDCAMS                    |

### 5.5 Data Export / Import Jobs

| # | Job        | Function                                     | Programs / Utilities      |
|---|------------|----------------------------------------------|---------------------------|
|31 | CBEXPORT   | Export all data to portable format             | CBEXPORT                  |
|32 | CBIMPORT   | Import data from export file                   | CBIMPORT                  |

### 5.6 Utility / Infrastructure Jobs

| # | Job        | Function                                     | Programs / Utilities      |
|---|------------|----------------------------------------------|---------------------------|
|33 | WAITSTEP   | Wait step (calls COBSWAIT)                    | COBSWAIT                  |
|34 | CBADMCDJ   | Admin card definition job                      | IDCAMS                    |
|35 | FTPJCL     | FTP file transfer to/from mainframe            | FTP                       |
|36 | INTRDRJ1   | Internal reader — triggers INTRDRJ2            | IDCAMS, IEBGENER          |
|37 | INTRDRJ2   | Internal reader — secondary (FTP test backup)  | IDCAMS                    |
|38 | TXT2PDF1   | Convert text statement to PDF                  | TXT2PDF (REXX)            |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program    | Function                              |
|---|------------|---------------------------------------|
| 1 | COBDATFT   | Date formatting routine (called by CBACT01C) |
| 2 | MVSWAIT    | Wait/delay routine (called by COBSWAIT)      |

---

## 7. JCL Procedures (`app/proc/`)

| # | Procedure  | Function                              |
|---|------------|---------------------------------------|
| 1 | REPROC     | Reprocessing procedure template       |
| 2 | TRANREPT   | Transaction report procedure          |

---

## 8. Batch Processing Cycle (Recommended Order)

```
1. CLOSEFIL    — Close CICS files
2. ACCTFILE    — Refresh Account master
3. CARDFILE    — Refresh Card master
4. CUSTFILE    — Refresh Customer master
5. XREFFILE    — Refresh Cross-reference
6. TRANFILE    — Refresh Transaction master
7. POSTTRAN    — Post daily transactions (CBTRN01C → CBTRN02C)
8. INTCALC     — Calculate interest & fees (CBACT04C)
9. TRANBKP     — Backup transactions
10. COMBTRAN   — Combine daily + master transactions
11. CREASTMT   — Generate statements (CBSTM03A + CBSTM03B)
12. TRANIDX    — Rebuild alternate indexes
13. OPENFIL    — Re-open CICS files
```

---

## 9. Naming Conventions

| Prefix/Pattern | Meaning                                |
|----------------|----------------------------------------|
| `CO*`          | Online CICS program                    |
| `CB*`          | Batch program                          |
| `CV*Y`         | Copybook — VSAM data structure         |
| `CS*Y`         | Copybook — shared/system structure     |
| `CO*Y`         | Copybook — online-specific structure   |
| `DFHAID`       | CICS-supplied AID key definitions      |
| `DFHBMSCA`     | CICS-supplied BMS attribute constants  |
| `CMQ*`         | MQ Series copybooks                    |
| `CIP*Y`        | IMS/DB2 auth module copybooks          |
| `CCP*Y`        | Auth request/reply copybooks           |
