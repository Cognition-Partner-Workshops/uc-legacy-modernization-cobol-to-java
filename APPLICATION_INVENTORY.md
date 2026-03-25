# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
>
> CardDemo is a CICS/COBOL mainframe application simulating credit card account management,
> transaction processing, bill payments, reporting, and user administration.
> Two user roles: **Regular** (card operations) and **Admin** (user/transaction-type management).

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [COBOL Programs (Core)](#cobol-programs-core)
3. [COBOL Programs (Optional Modules)](#cobol-programs-optional-modules)
4. [Copybooks — Data Structures](#copybooks--data-structures)
5. [Copybooks — BMS-Generated](#copybooks--bms-generated)
6. [BMS Screen Maps](#bms-screen-maps)
7. [JCL Batch Jobs](#jcl-batch-jobs)
8. [Assembler Programs](#assembler-programs)
9. [JCL Procedures](#jcl-procedures)
10. [Shell Scripts](#shell-scripts)
11. [Naming Conventions](#naming-conventions)

---

## Summary Statistics

| Asset Type                     | Count |
| ------------------------------ | ----: |
| Core COBOL Programs            |    31 |
| Optional Module COBOL Programs |    13 |
| Copybooks (Data Structures)    |    30 |
| Copybooks (BMS-Generated)      |    17 |
| BMS Screen Maps                |    17 |
| JCL Batch Jobs                 |    38 |
| Assembler Programs             |     2 |
| JCL Procedures                 |     2 |
| Shell Scripts                  |    10 |
| **Total Artifacts**            | **160** |

---

## COBOL Programs (Core)

### Online CICS Programs (16 programs)

These programs run under CICS and implement the interactive 3270 terminal UI.

| # | Program    | Lines | Function                              | Domain            | Trans ID | BMS Map   | Classification       |
|---|------------|------:|---------------------------------------|-------------------|----------|-----------|----------------------|
| 1 | COSGN00C   |   260 | Sign-on / Authentication              | Security          | CC00     | COSGN00   | Online — Security    |
| 2 | COMEN01C   |   308 | Main Menu (Regular Users)             | Navigation        | CM00     | COMEN01   | Online — Navigation  |
| 3 | COADM01C   |   288 | Admin Menu                            | Navigation        | CA00     | COADM01   | Online — Navigation  |
| 4 | COACTVWC   |   941 | Account View                          | Account Mgmt      | —        | COACTVW   | Online — Inquiry     |
| 5 | COACTUPC   | 4,236 | Account Update                        | Account Mgmt      | —        | COACTUP   | Online — Maintenance |
| 6 | COCRDLIC   | 1,459 | Credit Card List                      | Card Mgmt         | —        | COCRDLI   | Online — Inquiry     |
| 7 | COCRDSLC   |   887 | Credit Card Detail View               | Card Mgmt         | —        | COCRDSL   | Online — Inquiry     |
| 8 | COCRDUPC   | 1,560 | Credit Card Update                    | Card Mgmt         | —        | COCRDUP   | Online — Maintenance |
| 9 | COTRN00C   |   699 | Transaction List                      | Transaction Mgmt  | —        | COTRN00   | Online — Inquiry     |
|10 | COTRN01C   |   330 | Transaction Detail View               | Transaction Mgmt  | —        | COTRN01   | Online — Inquiry     |
|11 | COTRN02C   |   783 | Transaction Add                       | Transaction Mgmt  | —        | COTRN02   | Online — Maintenance |
|12 | CORPT00C   |   649 | Transaction Report (submits batch)    | Reporting         | —        | CORPT00   | Online — Reporting   |
|13 | COBIL00C   |   572 | Bill Payment                          | Billing           | —        | COBIL00   | Online — Maintenance |
|14 | COUSR00C   |   695 | User List (Admin)                     | User Admin        | —        | COUSR00   | Online — Inquiry     |
|15 | COUSR01C   |   299 | User Add (Admin)                      | User Admin        | —        | COUSR01   | Online — Maintenance |
|16 | COUSR02C   |   414 | User Update (Admin)                   | User Admin        | —        | COUSR02   | Online — Maintenance |

### Online CICS Programs — continued

| # | Program    | Lines | Function                              | Domain            | Trans ID | BMS Map   | Classification       |
|---|------------|------:|---------------------------------------|-------------------|----------|-----------|----------------------|
|17 | COUSR03C   |   359 | User Delete (Admin)                   | User Admin        | —        | COUSR03   | Online — Maintenance |

### Batch Programs (13 programs)

These programs run as standalone batch jobs scheduled via JCL.

| # | Program    | Lines | Function                              | Domain            | Classification        |
|---|------------|------:|---------------------------------------|-------------------|-----------------------|
|18 | CBTRN01C   |   494 | Daily Transaction Validation          | Transaction Mgmt  | Batch — Validation    |
|19 | CBTRN02C   |   731 | Transaction Posting & Balance Update  | Transaction Mgmt  | Batch — Core          |
|20 | CBTRN03C   |   649 | Transaction Report Generation         | Reporting         | Batch — Reporting     |
|21 | CBACT01C   |   430 | Account File Reader / Splitter        | Account Mgmt      | Batch — Utility       |
|22 | CBACT02C   |   178 | Card File Reader                      | Card Mgmt         | Batch — Utility       |
|23 | CBACT03C   |   178 | Cross-Reference File Reader           | Card Mgmt         | Batch — Utility       |
|24 | CBACT04C   |   652 | Interest Calculation                  | Account Mgmt      | Batch — Core          |
|25 | CBCUS01C   |   178 | Customer File Reader                  | Customer Mgmt     | Batch — Utility       |
|26 | CBSTM03A   |   924 | Statement Generation (Main)           | Reporting         | Batch — Reporting     |
|27 | CBSTM03B   |   230 | Statement Generation (Subroutine)     | Reporting         | Batch — Subroutine    |
|28 | CBEXPORT   |   582 | Data Export (all files → single file) | Data Migration    | Batch — Utility       |
|29 | CBIMPORT   |   487 | Data Import (single file → all files) | Data Migration    | Batch — Utility       |
|30 | COBSWAIT   |    41 | Wait Utility (calls MVSWAIT ASM)      | Utility           | Batch — Utility       |

### Shared Subroutine (1 program)

| # | Program    | Lines | Function                              | Domain            | Classification        |
|---|------------|------:|---------------------------------------|-------------------|-----------------------|
|31 | CSUTLDTC   |   157 | Date Conversion (calls CEEDAYS LE)    | Utility           | Shared — Subroutine   |

---

## COBOL Programs (Optional Modules)

### Authorization Module — IMS/DB2/MQ (8 programs)

Located in `app/app-authorization-ims-db2-mq/cbl/`.

| # | Program    | Lines | Function                              | Technology        | Classification         |
|---|------------|------:|---------------------------------------|-------------------|------------------------|
| 1 | COPAUA0C   | 1,026 | Card Authorization Decision           | CICS + IMS + MQ   | Online — Authorization |
| 2 | COPAUS0C   | 1,032 | Auth Message Summary View             | CICS + IMS + BMS  | Online — Inquiry       |
| 3 | COPAUS1C   |   604 | Auth Message Detail View              | CICS + IMS + BMS  | Online — Inquiry       |
| 4 | COPAUS2C   |   244 | Mark Auth Message as Fraud            | CICS + IMS + DB2  | Online — Maintenance   |
| 5 | CBPAUP0C   |   386 | Purge Expired Auth Messages           | Batch + IMS       | Batch — Maintenance    |
| 6 | PAUDBLOD   |   369 | Load Auth IMS Database                | Batch + IMS       | Batch — Utility        |
| 7 | PAUDBUNL   |   317 | Unload Auth IMS Database              | Batch + IMS       | Batch — Utility        |
| 8 | DBUNLDGS   |   366 | Unload IMS DB (Generic Segments)      | Batch + IMS       | Batch — Utility        |

### Transaction Type DB2 Module (3 programs)

Located in `app/app-transaction-type-db2/cbl/`.

| # | Program    | Lines | Function                              | Technology        | Classification         |
|---|------------|------:|---------------------------------------|-------------------|------------------------|
| 1 | COTRTLIC   | 2,098 | List/Delete Transaction Types         | CICS + DB2        | Online — Maintenance   |
| 2 | COTRTUPC   | 1,702 | Add/Edit Transaction Types            | CICS + DB2        | Online — Maintenance   |
| 3 | COBTUPDT   |   237 | Batch Update Transaction Types        | Batch + DB2       | Batch — Maintenance    |

### VSAM-MQ Module (2 programs)

Located in `app/app-vsam-mq/cbl/`.

| # | Program    | Lines | Function                              | Technology        | Classification         |
|---|------------|------:|---------------------------------------|-------------------|------------------------|
| 1 | COACCT01   |   620 | Account Inquiry via MQ                | CICS + VSAM + MQ  | Online — Inquiry       |
| 2 | CODATE01   |   524 | System Date via MQ                    | CICS + MQ         | Online — Utility       |

---

## Copybooks — Data Structures

Located in `app/cpy/`. These define VSAM record layouts used across programs.

| # | Copybook   | Lines | Record Layout                         | Record Len | Domain            | Key Fields                          |
|---|------------|------:|---------------------------------------|------------|-------------------|-------------------------------------|
| 1 | CVACT01Y   |   103 | Account Master Record                 | 300 bytes  | Account Mgmt      | ACCT-ID (11)                        |
| 2 | CVACT02Y   |    37 | Card Data Record                      | 150 bytes  | Card Mgmt         | CARD-NUM (16)                       |
| 3 | CVACT03Y   |    16 | Card Cross-Reference Record           | 50 bytes   | Card Mgmt         | XREF-CARD-NUM (16), XREF-ACCT-ID   |
| 4 | CVCRD01Y   |    11 | Card/Account Cross-Ref (Alt Layout)   | 50 bytes   | Card Mgmt         | FD-CARD-NUM (16)                    |
| 5 | CVCUS01Y   |    67 | Customer Master Record                | 500 bytes  | Customer Mgmt     | CUST-ID (09)                        |
| 6 | CVTRA01Y   |    13 | Transaction Category Balance          | 50 bytes   | Transaction Mgmt  | TRANCAT-ACCT-ID, TRANCAT-TYPE-CD    |
| 7 | CVTRA02Y   |    13 | Disclosure Group Record               | 50 bytes   | Interest/Billing  | DIS-ACCT-GROUP-ID, DIS-TRAN-TYPE-CD |
| 8 | CVTRA03Y   |    10 | Transaction Type Record               | 60 bytes   | Transaction Mgmt  | TRAN-TYPE (02)                      |
| 9 | CVTRA04Y   |    12 | Transaction Category Record           | 60 bytes   | Transaction Mgmt  | TRAN-TYPE-CD, TRAN-CAT-CD           |
|10 | CVTRA05Y   |    21 | Transaction Master Record             | 350 bytes  | Transaction Mgmt  | TRAN-ID (16)                        |
|11 | CVTRA06Y   |    21 | Daily Transaction Record              | 350 bytes  | Transaction Mgmt  | DALYTRAN-ID (16)                    |
|12 | CVTRA07Y   |    73 | Transaction Report Data Structures    | Variable   | Reporting         | (Report formatting)                 |
|13 | CVEXPORT   |    40 | Export/Import Record Layout           | Variable   | Data Migration    | EXPORT-REC-TYPE                     |
|14 | COSTM01    |    38 | Statement Transaction Layout          | 350 bytes  | Reporting         | TRNX-CARD-NUM, TRNX-ID             |
|15 | CUSTREC    |    23 | Customer Record (Statement variant)   | Variable   | Reporting         | CUST-ACCT-ID                        |
|16 | CSUSR01Y   |    26 | User Security Record                  | 80 bytes   | Security          | SEC-USR-ID (08)                     |
|17 | COCOM01Y   |   182 | Common Communication Area (COMMAREA)  | Variable   | Infrastructure    | CDEMO-* fields                      |
|18 | COMEN02Y   |    39 | Menu Option Definitions               | Variable   | Navigation        | CDEMO-MENU-OPT-*                    |
|19 | COADM02Y   |    28 | Admin Menu Option Definitions         | Variable   | Navigation        | CDEMO-ADMIN-OPT-*                   |
|20 | COTTL01Y   |    37 | Screen Title/Header Layout            | Variable   | UI Infrastructure | CCDA-TITLE*                         |
|21 | CSDAT01Y   |    28 | Date Display Formatting               | Variable   | Utility           | WS-CURDATE-DATA                     |
|22 | CSMSG01Y   |    12 | Message Area (Single-line)            | Variable   | UI Infrastructure | WS-MESSAGE                          |
|23 | CSMSG02Y   |    15 | Message Area (Multi-line)             | Variable   | UI Infrastructure | WS-MESSAGE-1/2                      |
|24 | CSSETATY   |     8 | Screen Attribute Setting              | Variable   | UI Infrastructure | (Attribute bytes)                   |
|25 | CSSTRPFY   |    42 | String Padding/Formatting Utility     | Variable   | Utility           | WS-SSTRIPF-*                        |
|26 | CSLKPCDY   |     3 | Lookup Code Utility                   | Variable   | Utility           | WS-LOOKUP-CODE                      |
|27 | CSUTLDPY   |    33 | Date Utility Parameters               | Variable   | Utility           | CSUTLDTC-DATE                       |
|28 | CSUTLDWY   |    11 | Date Utility Work Area                | Variable   | Utility           | WS-DATE-WORK-*                      |
|29 | CODATECN   |    19 | Date Conversion Copybook              | Variable   | Utility           | WS-CONVERT-DATE                     |
|30 | UNUSED1Y   |    10 | Unused / Placeholder Record           | 80 bytes   | —                 | UNUSED-ID                           |

---

## Copybooks — BMS-Generated

Located in `app/cpy-bms/`. Auto-generated from BMS map definitions; one per screen map.

| # | Copybook   | Source BMS | Function                       |
|---|------------|------------|--------------------------------|
| 1 | COACTUP    | COACTUP    | Account Update Screen Fields   |
| 2 | COACTVW    | COACTVW    | Account View Screen Fields     |
| 3 | COADM01    | COADM01    | Admin Menu Screen Fields       |
| 4 | COBIL00    | COBIL00    | Bill Payment Screen Fields     |
| 5 | COCRDLI    | COCRDLI    | Card List Screen Fields        |
| 6 | COCRDSL    | COCRDSL    | Card Detail Screen Fields      |
| 7 | COCRDUP    | COCRDUP    | Card Update Screen Fields      |
| 8 | COMEN01    | COMEN01    | Main Menu Screen Fields        |
| 9 | CORPT00    | CORPT00    | Report Selection Screen Fields |
|10 | COSGN00    | COSGN00    | Sign-on Screen Fields          |
|11 | COTRN00    | COTRN00    | Transaction List Screen Fields |
|12 | COTRN01    | COTRN01    | Transaction View Screen Fields |
|13 | COTRN02    | COTRN02    | Transaction Add Screen Fields  |
|14 | COUSR00    | COUSR00    | User List Screen Fields        |
|15 | COUSR01    | COUSR01    | User Add Screen Fields         |
|16 | COUSR02    | COUSR02    | User Update Screen Fields      |
|17 | COUSR03    | COUSR03    | User Delete Screen Fields      |

---

## BMS Screen Maps

Located in `app/bms/`. Each defines a CICS 3270 terminal screen layout.

| # | BMS Map    | Lines | Screen Title                  | Domain            | Key Fields on Screen                          |
|---|------------|------:|-------------------------------|-------------------|-----------------------------------------------|
| 1 | COSGN00    |   210 | Login Screen                  | Security          | User ID, Password                             |
| 2 | COMEN01    |   167 | Main Menu                     | Navigation        | Menu Options (1-12)                           |
| 3 | COADM01    |   114 | Admin Menu                    | Navigation        | Admin Options (1-4)                           |
| 4 | COACTVW    |   266 | Account View                  | Account Mgmt      | Acct ID, Status, Balances, Limits             |
| 5 | COACTUP    |   376 | Account Update                | Account Mgmt      | All account fields (editable)                 |
| 6 | COCRDLI    |   456 | Card List                     | Card Mgmt         | Card #, Account, Status (scrollable list)     |
| 7 | COCRDSL    |   219 | Card Detail View              | Card Mgmt         | Card #, Name, Expiry, Status                  |
| 8 | COCRDUP    |   310 | Card Update                   | Card Mgmt         | Card fields (editable)                        |
| 9 | COTRN00    |   464 | Transaction List              | Transaction Mgmt  | Trans ID, Amount, Date (scrollable list)      |
|10 | COTRN01    |   273 | Transaction Detail View       | Transaction Mgmt  | Full transaction details                      |
|11 | COTRN02    |   307 | Transaction Add               | Transaction Mgmt  | New transaction input fields                  |
|12 | CORPT00    |   231 | Report Selection              | Reporting         | Date range, report type selection             |
|13 | COBIL00    |   332 | Bill Payment                  | Billing           | Account, amount, confirmation                 |
|14 | COUSR00    |   463 | User List (Admin)             | User Admin        | User ID, Name, Type (scrollable list)         |
|15 | COUSR01    |   164 | User Add (Admin)              | User Admin        | User ID, Password, First/Last Name, Type      |
|16 | COUSR02    |   169 | User Update (Admin)           | User Admin        | User fields (editable)                        |
|17 | COUSR03    |   153 | User Delete (Admin)           | User Admin        | User fields (read-only) + confirmation        |

---

## JCL Batch Jobs

Located in `app/jcl/`. These define batch job streams for the z/OS Job Entry Subsystem.

### Data Loading / Refresh Jobs

| # | JCL Job    | Lines | Function                              | Programs Invoked         | Classification      |
|---|------------|------:|---------------------------------------|--------------------------|---------------------|
| 1 | DUSRSECJ   |    90 | Load User Security VSAM file          | IDCAMS, IEBGENER, SORT   | Data Load           |
| 2 | ACCTFILE   |    72 | Refresh Account Master VSAM           | IDCAMS                   | Data Load           |
| 3 | CARDFILE   |    84 | Refresh Card Master VSAM              | IDCAMS                   | Data Load           |
| 4 | CUSTFILE   |    76 | Refresh Customer Master VSAM          | IDCAMS                   | Data Load           |
| 5 | XREFFILE   |   106 | Load Card Cross-Reference + AIX       | IDCAMS                   | Data Load           |
| 6 | TRANFILE   |    71 | Load Transaction Master VSAM          | IDCAMS                   | Data Load           |
| 7 | DISCGRP    |    68 | Load Disclosure Group VSAM            | IDCAMS                   | Data Load           |
| 8 | TCATBALF   |    82 | Load Trans Category Balance VSAM      | IDCAMS                   | Data Load           |
| 9 | TRANCATG   |    62 | Load Transaction Category VSAM        | IDCAMS                   | Data Load           |
|10 | TRANTYPE   |    62 | Load Transaction Type VSAM            | IDCAMS                   | Data Load           |
|11 | REPTFILE   |    39 | Load Report Date Parameters           | IEBGENER                 | Data Load           |

### Core Batch Processing Jobs

| # | JCL Job    | Lines | Function                              | Programs Invoked         | Classification      |
|---|------------|------:|---------------------------------------|--------------------------|---------------------|
|12 | POSTTRAN   |    40 | Transaction Posting                   | CBTRN02C                 | Core Processing     |
|13 | INTCALC    |    43 | Interest Calculation                  | CBACT04C                 | Core Processing     |
|14 | COMBTRAN   |    52 | Combine Transactions (GDG merge)      | SORT, IDCAMS             | Core Processing     |
|15 | CREASTMT   |    97 | Generate Account Statements           | SORT, IDCAMS, CBSTM03A  | Core Processing     |
|16 | TRANREPT   |    37 | Transaction Report                    | CBTRN03C                 | Reporting           |

### Backup & Maintenance Jobs

| # | JCL Job    | Lines | Function                              | Programs Invoked         | Classification      |
|---|------------|------:|---------------------------------------|--------------------------|---------------------|
|17 | TRANBKP    |    36 | Backup Transaction File               | IDCAMS                   | Backup              |
|18 | TRANIDX    |    44 | Define Transaction Alternate Index    | IDCAMS                   | Maintenance         |
|19 | CLOSEFIL   |    58 | Close CICS Files for Batch            | DFHCSDUP (CICS utility)  | File Control        |
|20 | OPENFIL    |    43 | Open CICS Files after Batch           | DFHCSDUP (CICS utility)  | File Control        |
|21 | DEFGDGB    |    61 | Define GDG Bases (Transactions)       | IDCAMS                   | Maintenance         |
|22 | DEFGDGD    |   106 | Define GDG Bases (Reference Data)     | IEBGENER, IDCAMS         | Maintenance         |

### Utility / Special Purpose Jobs

| # | JCL Job    | Lines | Function                              | Programs Invoked         | Classification      |
|---|------------|------:|---------------------------------------|--------------------------|---------------------|
|23 | CBEXPORT   |    68 | Export All Data to Single File        | CBEXPORT (COBOL)         | Data Migration      |
|24 | CBIMPORT   |    66 | Import Data from Single File          | CBIMPORT (COBOL)         | Data Migration      |
|25 | DALYREJS   |    39 | Process Daily Rejection Records       | IEBGENER                 | Exception Handling  |
|26 | READACCT   |    28 | Read/Print Account File               | CBACT01C                 | Diagnostic          |
|27 | READCARD   |    28 | Read/Print Card File                  | CBACT02C                 | Diagnostic          |
|28 | READCUST   |    27 | Read/Print Customer File              | CBCUS01C                 | Diagnostic          |
|29 | READXREF   |    28 | Read/Print Cross-Reference File       | CBACT03C                 | Diagnostic          |
|30 | DEFCUST    |   108 | Define Customer VSAM Cluster          | IDCAMS                   | Setup               |
|31 | ESDSRRDS   |   124 | Define ESDS & RRDS VSAM Clusters      | IDCAMS, IEBGENER, SORT   | Setup               |
|32 | PRTCATBL   |    43 | Print Category Balance File           | IDCAMS                   | Diagnostic          |
|33 | CBADMCDJ   |    14 | Admin Utility                         | —                        | Admin               |
|34 | FTPJCL     |    42 | FTP File Transfer                     | FTP                      | File Transfer       |
|35 | INTRDRJ1   |    19 | Internal Reader Job 1 (Trigger)       | IDCAMS, IEBGENER         | Job Chaining        |
|36 | INTRDRJ2   |    14 | Internal Reader Job 2 (Triggered)     | IDCAMS                   | Job Chaining        |
|37 | TXT2PDF1   |    41 | Convert Text Statement to PDF         | IKJEFT1B (TXT2PDF)       | Utility             |
|38 | WAITSTEP   |    10 | Wait Step (calls COBSWAIT)            | COBSWAIT                 | Utility             |

---

## Assembler Programs

Located in `app/asm/`.

| # | Program    | Function                              | Called By      |
|---|------------|---------------------------------------|----------------|
| 1 | MVSWAIT    | Timed wait / sleep utility            | COBSWAIT       |
| 2 | COBDATFT   | Date formatting utility               | (Available)    |

---

## JCL Procedures

Located in `app/proc/`.

| # | Procedure  | Function                              |
|---|------------|---------------------------------------|
| 1 | REPROC     | Reusable compile/link procedure       |
| 2 | TRANREPT   | Transaction report procedure          |

---

## Shell Scripts

Located in `scripts/`.

| # | Script                       | Function                              |
|---|------------------------------|---------------------------------------|
| 1 | run_full_batch.sh            | Submit full batch cycle via FTP       |
| 2 | run_posting.sh               | Submit posting cycle                  |
| 3 | run_interest_calc.sh         | Submit interest calculation cycle     |
| 4 | remote_compile.sh            | Compile COBOL on mainframe via FTP    |
| 5 | remote_refresh.sh            | Refresh all data files on mainframe   |
| 6 | remote_submit.sh             | Submit a single JCL job               |
| 7 | upld_module.sh               | Upload source module to mainframe PDS |
| 8 | local_compile.sh             | Local compile with GnuCOBOL          |
| 9 | git-addSrcVersionInfo.sh     | Insert version info into source       |
|10 | pad.awk                      | Pad records to 80 chars (mainframe)   |

---

## Naming Conventions

| Prefix | Meaning                                    | Example    |
|--------|--------------------------------------------|------------|
| `CO*`  | Online CICS program                        | COSGN00C   |
| `CB*`  | Batch program                              | CBTRN02C   |
| `CS*`  | Shared subroutine / utility copybook       | CSUTLDTC   |
| `CV*`  | VSAM data-structure copybook               | CVACT01Y   |
| `*Y`   | Copybook (data structure) suffix           | CSUSR01Y   |
| `LIT-*`| Literal constants for CICS file names      | LIT-ACCTFILENAME |
| `CDEMO-*` | COMMAREA field prefix                   | CDEMO-TO-PROGRAM |
| `WS-*` | Working-Storage variable prefix            | WS-OPTION  |
| `FD-*` | File Description record prefix             | FD-ACCTFILE-REC |

---

*End of Application Inventory*
