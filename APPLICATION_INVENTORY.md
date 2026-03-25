# APPLICATION INVENTORY — CardDemo COBOL Application

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Platform:** CICS/VSAM/JCL on z/OS | **Language:** COBOL 85+

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [COBOL Programs (31)](#2-cobol-programs-31)
3. [Copybooks (30)](#3-copybooks-30)
4. [BMS Maps (17)](#4-bms-maps-17)
5. [BMS-Generated Copybooks (17)](#5-bms-generated-copybooks-17)
6. [JCL Batch Jobs (38)](#6-jcl-batch-jobs-38)
7. [Assembler Programs (2)](#7-assembler-programs-2)
8. [JCL Procedures (2)](#8-jcl-procedures-2)
9. [Optional Extension Modules (13)](#9-optional-extension-modules-13)
10. [Classification Summary](#10-classification-summary)

---

## 1. Executive Summary

| Asset Type | Count |
|---|---|
| COBOL Programs (core) | 31 |
| Copybooks (data structures) | 30 |
| BMS Screen Maps | 17 |
| BMS-Generated Copybooks | 17 |
| JCL Batch Jobs | 38 |
| Assembler Programs | 2 |
| JCL Procedures | 2 |
| Optional Module Programs | 13 |
| **Total Assets** | **150** |

The CardDemo application is a mainframe credit card management system with two user roles (Regular User and Admin). It supports account management, card management, transaction processing, bill payments, reporting, and user administration. The system uses CICS for online processing, VSAM KSDS files for data storage, and JCL-driven batch jobs for nightly processing cycles.

---

## 2. COBOL Programs (31)

### 2.1 Online CICS Programs (20)

| # | Program | Lines | Type | CICS Txn | Function | Business Domain |
|---|---------|-------|------|----------|----------|-----------------|
| 1 | COSGN00C | 260 | Online | CC00 | Sign-on screen — authenticates users against USRSEC file | Security |
| 2 | COMEN01C | 308 | Online | CM00 | Main menu — routes regular users to functional areas | Navigation |
| 3 | COADM01C | 288 | Online | CA00 | Admin menu — routes admin users to management functions | Administration |
| 4 | COACTVWC | 941 | Online | CA01 | Account view — displays account details with card/customer cross-ref | Account Mgmt |
| 5 | COACTUPC | 4,236 | Online | CA02 | Account update — modifies account details with validation | Account Mgmt |
| 6 | COCRDLIC | 1,459 | Online | CC01 | Card list — browses card records with scrolling/pagination | Card Mgmt |
| 7 | COCRDSLC | 887 | Online | CC02 | Card detail view — displays individual card information | Card Mgmt |
| 8 | COCRDUPC | 1,560 | Online | CC03 | Card update — modifies card details with field validation | Card Mgmt |
| 9 | COTRN00C | 699 | Online | CT00 | Transaction list — browses transaction records with scrolling | Transaction Mgmt |
| 10 | COTRN01C | 330 | Online | CT01 | Transaction view — displays individual transaction details | Transaction Mgmt |
| 11 | COTRN02C | 783 | Online | CT02 | Transaction add — creates new transactions with validation | Transaction Mgmt |
| 12 | CORPT00C | 649 | Online | CR00 | Transaction reports — submits batch report jobs via TDQ | Reporting |
| 13 | COBIL00C | 572 | Online | CB00 | Bill payment — processes credit card payments | Payments |
| 14 | COUSR00C | 695 | Online | CU00 | User list — browses user security records (Admin) | User Admin |
| 15 | COUSR01C | 299 | Online | CU01 | User add — creates new Regular/Admin users (Admin) | User Admin |
| 16 | COUSR02C | 414 | Online | CU02 | User update — modifies user records (Admin) | User Admin |
| 17 | COUSR03C | 359 | Online | CU03 | User delete — removes users from USRSEC file (Admin) | User Admin |
| 18 | CSUTLDTC | 157 | Utility | — | Date conversion utility — calls CEEDAYS for date arithmetic | Shared Utility |

> **Note:** COCRDLIC also references the COCRDSL BMS map for dual-screen card selection workflow.

### 2.2 Batch Programs (11)

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 19 | CBACT01C | 430 | Batch | Read and list account master records | Account Mgmt |
| 20 | CBACT02C | 178 | Batch | Read and list card data records | Card Mgmt |
| 21 | CBACT03C | 178 | Batch | Read and list card cross-reference records | Card Mgmt |
| 22 | CBACT04C | 652 | Batch | Calculate interest on accounts — core financial computation | Finance |
| 23 | CBCUS01C | 178 | Batch | Read and list customer data records | Customer Mgmt |
| 24 | CBTRN01C | 494 | Batch | Validate daily transactions against master data | Transaction Mgmt |
| 25 | CBTRN02C | 731 | Batch | Post daily transactions — updates balances and master files | Transaction Mgmt |
| 26 | CBTRN03C | 649 | Batch | Generate daily transaction report with totals | Reporting |
| 27 | CBSTM03A | 924 | Batch | Generate account statements in text and HTML format | Reporting |
| 28 | CBSTM03B | 230 | Batch Sub | File I/O subroutine for CBSTM03A — handles OPEN/READ/CLOSE | Reporting |
| 29 | CBEXPORT | 582 | Batch | Export all VSAM data to sequential flat file | Data Migration |
| 30 | CBIMPORT | 487 | Batch | Import data from sequential flat file to VSAM | Data Migration |
| 31 | COBSWAIT | 41 | Batch Util | Wait/delay utility — calls assembler MVSWAIT | Utility |

### 2.3 Program Classification Matrix

| Classification | Programs | Count |
|---|---|---|
| **Security** | COSGN00C | 1 |
| **Navigation** | COMEN01C, COADM01C | 2 |
| **Account Mgmt** | COACTVWC, COACTUPC, CBACT01C | 3 |
| **Card Mgmt** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | 5 |
| **Transaction Mgmt** | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | 5 |
| **Payments** | COBIL00C | 1 |
| **Reporting** | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B | 4 |
| **User Admin** | COUSR00C, COUSR01C, COUSR02C, COUSR03C | 4 |
| **Finance** | CBACT04C | 1 |
| **Data Migration** | CBEXPORT, CBIMPORT | 2 |
| **Utility** | CSUTLDTC, COBSWAIT | 2 |
| **Customer Mgmt** | CBCUS01C | 1 |

---

## 3. Copybooks (30)

### 3.1 Data Record Copybooks (CV-prefix — VSAM record layouts)

| # | Copybook | Lines | Record Len | Description | Used By |
|---|----------|-------|------------|-------------|---------|
| 1 | CVACT01Y | 20 | 300 bytes | Account master record — balances, limits, status | 10 programs |
| 2 | CVACT02Y | 14 | 150 bytes | Card data record — number, expiry, status | 6 programs |
| 3 | CVACT03Y | 11 | 50 bytes | Card-to-account cross-reference record | 8 programs |
| 4 | CVCRD01Y | 46 | — | Card detail record — extended card attributes | 5 programs |
| 5 | CVCUS01Y | 26 | 500 bytes | Customer data record — name, address, SSN, DOB | 7 programs |
| 6 | CVTRA01Y | 13 | 50 bytes | Transaction category balance record | 2 programs |
| 7 | CVTRA02Y | 13 | 50 bytes | Disclosure group record — interest rates by category | 1 program |
| 8 | CVTRA03Y | 10 | 60 bytes | Transaction type master record | 1 program |
| 9 | CVTRA04Y | 12 | 60 bytes | Transaction category type record | 1 program |
| 10 | CVTRA05Y | 21 | 350 bytes | Transaction record — core transaction data | 9 programs |
| 11 | CVTRA06Y | 21 | 350 bytes | Daily transaction record — staging for batch posting | 3 programs |
| 12 | CVTRA07Y | 73 | — | Transaction report layout — headers, detail lines, totals | 1 program |
| 13 | CVEXPORT | 103 | — | Export/import data record layout | 2 programs |
| 14 | CUSTREC | 26 | — | Customer record (alternate layout for statements) | 1 program |
| 15 | COSTM01 | 38 | — | Transaction altered layout for statement reporting | 1 program |

### 3.2 Common/Shared Copybooks (CO/CS-prefix)

| # | Copybook | Lines | Description | Used By |
|---|----------|-------|-------------|---------|
| 16 | COCOM01Y | 47 | Common communication area — passed between programs | 14 programs |
| 17 | COTTL01Y | 27 | Title/header line definitions for 3270 screens | 13 programs |
| 18 | COMEN02Y | 101 | Menu option definitions and routing tables | 1 program |
| 19 | COADM02Y | 62 | Admin menu option definitions | 1 program |
| 20 | CODATECN | 52 | Date conversion constants and work areas | 1 program |
| 21 | CSDAT01Y | 58 | Date formatting data structures | 14 programs |
| 22 | CSMSG01Y | 24 | Standard message area (thank you, error messages) | 14 programs |
| 23 | CSMSG02Y | 35 | Extended message area (info/warning/error messages) | 6 programs |
| 24 | CSUSR01Y | 26 | User security record — user ID, password, type | 11 programs |
| 25 | CSSETATY | 30 | Screen attribute settings (BRT, DARK, etc.) | 1 program |
| 26 | CSSTRPFY | 85 | String processing functions/work areas | 5 programs |
| 27 | CSUTLDPY | 375 | Date utility parameter block (large) | 1 program |
| 28 | CSUTLDWY | 89 | Date utility working storage | 1 program |
| 29 | CSLKPCDY | 1,318 | Lookup code tables — large reference data set | 1 program |
| 30 | UNUSED1Y | 10 | Unused/deprecated record layout | 0 programs |

---

## 4. BMS Maps (17)

| # | Map | Mapset | Screen | Associated Program | Function |
|---|-----|--------|--------|--------------------|----------|
| 1 | COSGN00 | COSGN00 | COSGN0A | COSGN00C | Sign-on screen — user ID / password entry |
| 2 | COMEN01 | COMEN01 | COMEN1A | COMEN01C | Main menu — option selection for regular users |
| 3 | COADM01 | COADM01 | COADM1A | COADM01C | Admin menu — option selection for administrators |
| 4 | COACTVW | COACTVW | CACTVWA | COACTVWC | Account view — read-only account details |
| 5 | COACTUP | COACTUP | CACTUPA | COACTUPC | Account update — editable account fields |
| 6 | COCRDLI | COCRDLI | CCRDLIA | COCRDLIC | Card list — scrollable card directory |
| 7 | COCRDSL | COCRDSL | CCRDSLA | COCRDSLC | Card detail view — individual card display |
| 8 | COCRDUP | COCRDUP | CCRDUPA | COCRDUPC | Card update — editable card fields |
| 9 | COTRN00 | COTRN00 | COTRN0A | COTRN00C | Transaction list — scrollable transaction browser |
| 10 | COTRN01 | COTRN01 | COTRN1A | COTRN01C | Transaction view — individual transaction display |
| 11 | COTRN02 | COTRN02 | COTRN2A | COTRN02C | Transaction add — new transaction entry form |
| 12 | CORPT00 | CORPT00 | CORPT0A | CORPT00C | Report parameters — date range, selection criteria |
| 13 | COBIL00 | COBIL00 | COBIL0A | COBIL00C | Bill payment — payment amount and confirmation |
| 14 | COUSR00 | COUSR00 | COUSR0A | COUSR00C | User list — scrollable user directory |
| 15 | COUSR01 | COUSR01 | COUSR1A | COUSR01C | User add — new user registration form |
| 16 | COUSR02 | COUSR02 | COUSR2A | COUSR02C | User update — editable user fields |
| 17 | COUSR03 | COUSR03 | COUSR3A | COUSR03C | User delete — confirmation screen |

---

## 5. BMS-Generated Copybooks (17)

| # | Copybook | Source BMS | Description |
|---|----------|-----------|-------------|
| 1 | COACTUP.CPY | COACTUP.bms | Account update symbolic map |
| 2 | COACTVW.CPY | COACTVW.bms | Account view symbolic map |
| 3 | COADM01.CPY | COADM01.bms | Admin menu symbolic map |
| 4 | COBIL00.CPY | COBIL00.bms | Bill payment symbolic map |
| 5 | COCRDLI.CPY | COCRDLI.bms | Card list symbolic map |
| 6 | COCRDSL.CPY | COCRDSL.bms | Card detail symbolic map |
| 7 | COCRDUP.CPY | COCRDUP.bms | Card update symbolic map |
| 8 | COMEN01.CPY | COMEN01.bms | Main menu symbolic map |
| 9 | CORPT00.CPY | CORPT00.bms | Report screen symbolic map |
| 10 | COSGN00.CPY | COSGN00.bms | Sign-on screen symbolic map |
| 11 | COTRN00.CPY | COTRN00.bms | Transaction list symbolic map |
| 12 | COTRN01.CPY | COTRN01.bms | Transaction view symbolic map |
| 13 | COTRN02.CPY | COTRN02.bms | Transaction add symbolic map |
| 14 | COUSR00.CPY | COUSR00.bms | User list symbolic map |
| 15 | COUSR01.CPY | COUSR01.bms | User add symbolic map |
| 16 | COUSR02.CPY | COUSR02.bms | User update symbolic map |
| 17 | COUSR03.CPY | COUSR03.bms | User delete symbolic map |

---

## 6. JCL Batch Jobs (38)

### 6.1 Data Refresh Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 1 | ACCTFILE | 65 | IDCAMS | Delete/define and reload account master VSAM from PS |
| 2 | CARDFILE | 128 | SDSF, IDCAMS | Close CICS, delete/define and reload card VSAM from PS |
| 3 | CUSTFILE | 84 | SDSF, IDCAMS | Close CICS, delete/define and reload customer VSAM from PS |
| 4 | XREFFILE | 106 | IDCAMS | Delete/define and reload card cross-reference VSAM + alternate index |
| 5 | TRANFILE | 125 | SDSF, IDCAMS | Close CICS, delete/define and reload transaction VSAM + alt indexes |
| 6 | DUSRSECJ | 92 | IEFBR14, IEBGENER, IDCAMS | Load user security VSAM from sequential file |
| 7 | TRANTYPE | 65 | IDCAMS | Delete/define and reload transaction type VSAM |
| 8 | TRANCATG | 65 | IDCAMS | Delete/define and reload transaction category VSAM |
| 9 | TCATBALF | 65 | IDCAMS | Delete/define and reload category balance VSAM |
| 10 | DISCGRP | 65 | IDCAMS | Delete/define and reload disclosure group VSAM |

### 6.2 Core Batch Processing Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 11 | POSTTRAN | 45 | CBTRN02C | Post daily transactions — core nightly processing |
| 12 | INTCALC | 44 | CBACT04C | Calculate interest on account balances |
| 13 | TRANBKP | 71 | REPROC, IDCAMS | Backup transactions to GDG, clear VSAM |
| 14 | COMBTRAN | 52 | SORT, IDCAMS | Merge system + backup transactions for combined view |
| 15 | CREASTMT | 97 | IDCAMS, SORT, IEFBR14, CBSTM03A | Generate account statements (text + HTML) |
| 16 | TRANREPT | 84 | REPROC, SORT, CBTRN03C | Sort transactions and generate daily report |

### 6.3 CICS Control Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 17 | CLOSEFIL | 34 | SDSF | Close CICS files for batch processing window |
| 18 | OPENFIL | 34 | SDSF | Re-open CICS files after batch processing |

### 6.4 Utility / Setup Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 19 | DEFGDGB | 63 | IDCAMS | Define GDG base entries for backup datasets |
| 20 | DEFGDGD | 94 | IDCAMS, IEBGENER | Define GDG bases and seed initial generation data |
| 21 | DEFCUST | 47 | IDCAMS | Define customer VSAM cluster |
| 22 | DALYREJS | 32 | IDCAMS | Define daily rejects dataset |
| 23 | REPTFILE | 32 | IDCAMS | Define report output dataset |
| 24 | TRANIDX | 58 | IDCAMS | Define alternate indexes on transaction VSAM |
| 25 | ESDSRRDS | — | IDCAMS | Define ESDS and RRDS variants of user security |
| 26 | PRTCATBL | 66 | IEFBR14, SORT | Print category balance report |
| 27 | WAITSTEP | 27 | COBSWAIT | Execute wait/delay step |
| 28 | TXT2PDF1 | 41 | IKJEFT1B | Convert text statement to PDF format |

### 6.5 Data Export/Import Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 29 | CBEXPORT | 72 | IDCAMS, CBEXPORT | Export all VSAM files to sequential export dataset |
| 30 | CBIMPORT | 68 | CBIMPORT | Import sequential data into VSAM files |

### 6.6 Read/Diagnostic Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 31 | READACCT | 31 | CBACT01C | Read and list account records |
| 32 | READCARD | 31 | CBACT02C | Read and list card records |
| 33 | READCUST | 30 | CBCUS01C | Read and list customer records |
| 34 | READXREF | 31 | CBACT03C | Read and list cross-reference records |

### 6.7 CICS Administration / Miscellaneous Jobs

| # | Job | Lines | Programs Executed | Description |
|---|-----|-------|-------------------|-------------|
| 35 | CBADMCDJ | 167 | DFHCSDUP | CICS CSD batch utility — resource definitions |
| 36 | FTPJCL | 42 | FTP | FTP file transfer job template |
| 37 | INTRDRJ1 | 19 | IDCAMS, IEBGENER | Internal reader job 1 — triggers INTRDRJ2 |
| 38 | INTRDRJ2 | 14 | IDCAMS | Internal reader job 2 — copies backup data |

### 6.8 Nightly Batch Cycle Sequence

```
CLOSEFIL → ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE → POSTTRAN → INTCALC
    → TRANBKP → COMBTRAN → CREASTMT → TRANREPT → TRANIDX → OPENFIL
```

---

## 7. Assembler Programs (2)

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | MVSWAIT | app/asm/MVSWAIT.asm | Low-level wait/delay routine (called by COBSWAIT) |
| 2 | COBDATFT | app/asm/COBDATFT.asm | Date formatting routine (called by CBACT01C) |

---

## 8. JCL Procedures (2)

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | REPROC | app/proc/REPROC.prc | Reusable VSAM-to-sequential repro procedure |
| 2 | TRANREPT | app/proc/TRANREPT.prc | Transaction report generation procedure |

---

## 9. Optional Extension Modules (13)

### 9.1 Authorization Module — IMS/DB2/MQ (8 programs)

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | COPAUA0C | Online | MQ trigger — initiates authorization processing |
| 2 | COPAUS0C | Online | Authorization summary display |
| 3 | COPAUS1C | Online | Authorization detail display |
| 4 | COPAUS2C | Online | Fraud marking — writes to DB2 |
| 5 | CBPAUP0C | Batch | Batch purge of aged authorization records |
| 6 | PAUDBLOD | Batch | DB2 table load utility |
| 7 | PAUDBUNL | Batch | DB2 table unload utility |
| 8 | DBUNLDGS | Batch | DB2 unload with GDG support |

### 9.2 Transaction Type DB2 Module (3 programs)

| # | Program | Type | Function |
|---|---------|------|----------|
| 9 | COTRTUPC | Online | Transaction type add/edit via embedded SQL |
| 10 | COTRTLIC | Online | Transaction type list/delete via DB2 cursors |
| 11 | COBTUPDT | Batch | Batch transaction type update |

### 9.3 VSAM-MQ Module (2 programs)

| # | Program | Type | Function |
|---|---------|------|----------|
| 12 | CODATE01 | Online | MQ request/response for system date |
| 13 | COACCT01 | Online | MQ request/response for account inquiry |

---

## 10. Classification Summary

### By Processing Mode

| Mode | Count | Programs |
|---|---|---|
| Online (CICS) | 18 | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C–03C, CSUTLDTC |
| Batch | 11 | CBACT01C–04C, CBCUS01C, CBTRN01C–03C, CBSTM03A/B, CBEXPORT, CBIMPORT |
| Batch Utility | 2 | COBSWAIT, CSUTLDTC |

### By CRUD Operation

| Operation | Online Programs | Batch Programs |
|---|---|---|
| **Create** | COUSR01C, COTRN02C | — |
| **Read** | COACTVWC, COCRDSLC, COTRN01C, COUSR00C, COTRN00C, COCRDLIC | CBACT01C–03C, CBCUS01C, CBTRN01C |
| **Update** | COACTUPC, COCRDUPC, COUSR02C, COBIL00C | CBTRN02C, CBACT04C |
| **Delete** | COUSR03C | — |
| **Report** | CORPT00C | CBTRN03C, CBSTM03A/B |
| **Export/Import** | — | CBEXPORT, CBIMPORT |

### Naming Conventions

| Prefix | Meaning | Example |
|---|---|---|
| `CO` | Online CICS program | COSGN00C |
| `CB` | Batch COBOL program | CBTRN02C |
| `CS` | Common/shared utility | CSUTLDTC |
| `CV` | Copybook — VSAM record layout | CVACT01Y |
| `CO` (cpy) | Copybook — common/online area | COCOM01Y |
| `CS` (cpy) | Copybook — shared/system | CSDAT01Y |
| `Y` suffix | Copybook indicator | CVACT01Y |
