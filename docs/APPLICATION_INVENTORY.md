# CardDemo Application Inventory

> **System**: CardDemo -- Credit Card Management System
> **Platform**: IBM z/OS, CICS, VSAM, JCL Batch
> **Version**: CardDemo_v2.0
> **Date**: 2026-03-25

---

## Table of Contents

1. [Summary](#summary)
2. [COBOL Programs -- Online (CICS)](#cobol-programs----online-cics)
3. [COBOL Programs -- Batch](#cobol-programs----batch)
4. [Copybooks -- Data Structures](#copybooks----data-structures)
5. [Copybooks -- BMS Generated](#copybooks----bms-generated)
6. [BMS Maps (Screen Definitions)](#bms-maps-screen-definitions)
7. [JCL Jobs](#jcl-jobs)
8. [Assembler Programs](#assembler-programs)
9. [JCL Procedures](#jcl-procedures)
10. [Optional Modules](#optional-modules)
11. [Supporting Assets](#supporting-assets)

---

## Summary

| Asset Type | Count | Location |
|---|---|---|
| COBOL Programs -- Online (CICS) | 18 | `app/cbl/` |
| COBOL Programs -- Batch | 13 | `app/cbl/` |
| Copybooks -- Data Structures | 30 | `app/cpy/` |
| Copybooks -- BMS Generated | 17 | `app/cpy-bms/` |
| BMS Maps (Screen Definitions) | 17 | `app/bms/` |
| JCL Jobs | 38 | `app/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| Optional Module Programs | 13 | `app/app-*/cbl/` |
| **Total Assets** | **150** | |

---

## COBOL Programs -- Online (CICS)

These programs run under CICS and are invoked via 3270 terminal transactions. They use EXEC CICS commands, BMS maps, and COMMAREA for inter-program communication.

| # | Program | Lines | Transaction | Description | Classification |
|---|---------|-------|-------------|-------------|----------------|
| 1 | `COSGN00C.cbl` | 260 | CC00 | **Sign-On / Authentication** -- Validates user credentials against USRSEC VSAM file; routes to main menu or admin menu based on user type (A/U) | Security |
| 2 | `COMEN01C.cbl` | 308 | CM00 | **Main Menu** -- Displays menu options for regular users; dispatches to selected program via XCTL based on COMEN02Y menu table | Navigation |
| 3 | `COACTVWC.cbl` | 941 | CA00 | **Account View** -- Displays account details, card list, and customer info for a given account ID; read-only | Account Mgmt |
| 4 | `COACTUPC.cbl` | 4,236 | CU00 | **Account Update** -- Full account update with field-level validation (phone, zip, state, dates); most complex program in system | Account Mgmt |
| 5 | `COCRDLIC.cbl` | 1,459 | CL00 | **Credit Card List** -- Paginated card listing with account filter; supports forward/backward scrolling | Card Mgmt |
| 6 | `COCRDSLC.cbl` | 887 | CS00 | **Credit Card View** -- Displays card details and associated account/customer info | Card Mgmt |
| 7 | `COCRDUPC.cbl` | 1,560 | CU01 | **Credit Card Update** -- Updates card status and details with validation | Card Mgmt |
| 8 | `COTRN00C.cbl` | 699 | CT00 | **Transaction List** -- Paginated transaction listing by account/card with date range filter | Transaction Mgmt |
| 9 | `COTRN01C.cbl` | 330 | CT01 | **Transaction View** -- Displays individual transaction details (read-only) | Transaction Mgmt |
| 10 | `COTRN02C.cbl` | 783 | CT02 | **Transaction Add** -- Adds new transactions with full validation of type, category, amount, merchant | Transaction Mgmt |
| 11 | `CORPT00C.cbl` | 649 | CR00 | **Transaction Reports** -- Generates monthly/yearly transaction reports; initiates batch report via START TRANSID | Reporting |
| 12 | `COBIL00C.cbl` | 572 | CB00 | **Bill Payment** -- Processes bill payments; creates debit transactions and updates account balances | Payment |
| 13 | `COADM01C.cbl` | 288 | CA01 | **Admin Menu** -- Admin-only menu; dispatches to user management programs based on COADM02Y menu table | Navigation / Admin |
| 14 | `COUSR00C.cbl` | 695 | CU00 | **User List (Security)** -- Paginated user listing for admin; supports scrolling through USRSEC file | User Admin |
| 15 | `COUSR01C.cbl` | 299 | CU01 | **User Add (Security)** -- Adds new user records to USRSEC VSAM file | User Admin |
| 16 | `COUSR02C.cbl` | 414 | CU02 | **User Update (Security)** -- Updates existing user records (name, password, type) | User Admin |
| 17 | `COUSR03C.cbl` | 359 | CU03 | **User Delete (Security)** -- Deletes user records from USRSEC VSAM file with confirmation | User Admin |
| 18 | `CSUTLDTC.cbl` | 157 | -- | **Date Validation Utility** -- Reusable date validation called via LINK from other CICS programs | Utility |

---

## COBOL Programs -- Batch

These programs run as batch jobs scheduled via JCL. They process sequential and VSAM files for end-of-day / end-of-cycle operations.

| # | Program | Lines | Description | Classification |
|---|---------|-------|-------------|----------------|
| 1 | `CBTRN02C.cbl` | 731 | **Transaction Posting** -- Posts daily transactions to account master; updates balances, categorizes debits/credits | Core Batch |
| 2 | `CBACT04C.cbl` | 652 | **Interest Calculation** -- Calculates interest on account balances using disclosure group rates; updates transaction category balances | Core Batch |
| 3 | `CBSTM03A.cbl` | 924 | **Statement Generation (Primary)** -- Generates customer statements with transaction details, page formatting, totals; writes to print spool | Core Batch |
| 4 | `CBSTM03B.cbl` | 230 | **Statement Generation (Overflow)** -- Handles statement page overflow and continuation for CBSTM03A | Core Batch |
| 5 | `CBTRN03C.cbl` | 649 | **Daily Transaction Report** -- Generates detailed daily transaction reports with account-level and grand totals | Reporting |
| 6 | `CBEXPORT.cbl` | 582 | **Data Export** -- Exports customer, account, card, transaction, and cross-reference data from VSAM to sequential file for branch migration | Data Transfer |
| 7 | `CBIMPORT.cbl` | 487 | **Data Import** -- Imports multi-record data from sequential file into VSAM datasets; validates record types | Data Transfer |
| 8 | `CBTRN01C.cbl` | 494 | **Transaction File Processing** -- Reads daily transaction file, validates, and writes to transaction master VSAM | Core Batch |
| 9 | `CBACT01C.cbl` | 430 | **Account File Refresh** -- Loads/refreshes account master VSAM from sequential input | Data Load |
| 10 | `CBACT02C.cbl` | 178 | **Card File Refresh** -- Loads/refreshes card master VSAM from sequential input | Data Load |
| 11 | `CBACT03C.cbl` | 178 | **Cross-Reference Refresh** -- Loads/refreshes card cross-reference VSAM from sequential input | Data Load |
| 12 | `CBCUS01C.cbl` | 178 | **Customer File Refresh** -- Loads/refreshes customer master VSAM from sequential input | Data Load |
| 13 | `COBSWAIT.cbl` | 41 | **Wait Utility** -- Calls assembler MVSWAIT to introduce timed delays in batch processing | Utility |

---

## Copybooks -- Data Structures

| # | Copybook | Lines | Record Size | Description | Classification |
|---|----------|-------|-------------|-------------|----------------|
| 1 | `CVACT01Y.cpy` | 17 | 300 bytes | **Account Record** -- Account master with balances, limits, dates, status | Entity |
| 2 | `CVACT02Y.cpy` | 11 | 150 bytes | **Card Record** -- Credit card with number, CVV, embossed name, status | Entity |
| 3 | `CVACT03Y.cpy` | 8 | 50 bytes | **Card Cross-Reference** -- Links card number to customer and account IDs | Entity |
| 4 | `CVCUS01Y.cpy` | 23 | 500 bytes | **Customer Record** -- Full customer profile with address, SSN, FICO score | Entity |
| 5 | `CVTRA05Y.cpy` | 18 | 350 bytes | **Online Transaction Record** -- Transaction with merchant, amount, timestamps | Entity |
| 6 | `CVTRA06Y.cpy` | 18 | 350 bytes | **Daily Transaction Record** -- Batch variant of transaction record (DALYTRAN prefix) | Entity |
| 7 | `CVTRA01Y.cpy` | 14 | 50 bytes | **Transaction Category Balance** -- Running balance by account/type/category | Entity |
| 8 | `CVTRA02Y.cpy` | 14 | 50 bytes | **Disclosure Group** -- Interest rate by account group/transaction type | Entity |
| 9 | `CVTRA03Y.cpy` | 11 | 60 bytes | **Transaction Type** -- Transaction type code and description | Reference |
| 10 | `CVTRA04Y.cpy` | 13 | 60 bytes | **Transaction Category** -- Category code and description by type | Reference |
| 11 | `CSUSR01Y.cpy` | 23 | 80 bytes | **User Security Record** -- User authentication (ID, password, type A/U) | Security |
| 12 | `COCOM01Y.cpy` | 44 | ~150 bytes | **COMMAREA** -- Inter-program communication area (general info, customer, account, card, navigation) | Infrastructure |
| 13 | `CVCRD01Y.cpy` | 47 | ~200 bytes | **Card Work Areas** -- AID key mapping, navigation, error/return messages, current context IDs | Infrastructure |
| 14 | `COMEN02Y.cpy` | ~60 | -- | **Regular User Menu Options** -- 11 menu items mapping option numbers to program names | Navigation |
| 15 | `COADM02Y.cpy` | 63 | -- | **Admin Menu Options** -- 6 admin menu items (user CRUD + DB2 transaction type mgmt) | Navigation |
| 16 | `COTTL01Y.cpy` | ~30 | -- | **Title/Header Line** -- Common screen title and header text | UI |
| 17 | `CSDAT01Y.cpy` | 59 | -- | **Date/Time Working Storage** -- Current date, time, and timestamp formatting fields | Utility |
| 18 | `CSUTLDWY.cpy` | 90 | -- | **Date Validation Working Storage** -- Date editing/validation fields with month/day/year flags | Utility |
| 19 | `CSUTLDPY.cpy` | ~200 | -- | **Date Validation Procedures** -- Reusable date validation logic (paragraphs) | Utility |
| 20 | `CSSTRPFY.cpy` | 86 | -- | **PFKey Storage** -- Maps EIBAID to CCARD-AID values for function key handling | Utility |
| 21 | `CSSETATY.cpy` | 31 | -- | **Field Error Highlighting** -- Sets screen field color to red on validation error | Utility |
| 22 | `CSLKPCDY.cpy` | 1,318 | -- | **Lookup Code Repository** -- Phone area codes, US state codes, state+zip validation tables | Reference |
| 23 | `CSMSG01Y.cpy` | 25 | -- | **Common Messages** -- "Thank you" and "Invalid key" messages | UI |
| 24 | `CSMSG02Y.cpy` | 36 | -- | **Abend Data** -- Abend code, culprit program, reason, message fields | Error Handling |
| 25 | `CODATECN.cpy` | 53 | -- | **Date Conversion** -- Input/output date conversion (YYYYMMDD <-> YYYY-MM-DD) | Utility |
| 26 | `COSTM01.CPY` | 37 | 340 bytes | **Statement Transaction Record** -- Transaction layout keyed by card+ID for reporting | Reporting |
| 27 | `CVTRA07Y.cpy` | 74 | -- | **Transaction Report Layout** -- Report headers, detail lines, page/account/grand totals | Reporting |
| 28 | `CVEXPORT.cpy` | 104 | 500 bytes | **Multi-Record Export Layout** -- Export record with REDEFINES for customer, account, transaction, card, xref | Data Transfer |
| 29 | `CUSTREC.cpy` | ~20 | 500 bytes | **Customer Record (Alternate)** -- Alternate customer record layout | Entity |
| 30 | `UNUSED1Y.cpy` | 11 | 80 bytes | **Unused/Placeholder** -- Mirrors CSUSR01Y structure; not referenced by any program | Deprecated |

---

## Copybooks -- BMS Generated

These copybooks are auto-generated from the corresponding BMS map definitions. Each provides COBOL field definitions for the 3270 screen.

| # | Copybook | Source BMS | Description |
|---|----------|-----------|-------------|
| 1 | `COACTUP.CPY` | `COACTUP.bms` | Account Update screen fields |
| 2 | `COACTVW.CPY` | `COACTVW.bms` | Account View screen fields |
| 3 | `COADM01.CPY` | `COADM01.bms` | Admin Menu screen fields |
| 4 | `COBIL00.CPY` | `COBIL00.bms` | Bill Payment screen fields |
| 5 | `COCRDLI.CPY` | `COCRDLI.bms` | Credit Card List screen fields |
| 6 | `COCRDSL.CPY` | `COCRDSL.bms` | Credit Card View screen fields |
| 7 | `COCRDUP.CPY` | `COCRDUP.bms` | Credit Card Update screen fields |
| 8 | `COMEN01.CPY` | `COMEN01.bms` | Main Menu screen fields |
| 9 | `CORPT00.CPY` | `CORPT00.bms` | Reports screen fields |
| 10 | `COSGN00.CPY` | `COSGN00.bms` | Sign-On screen fields |
| 11 | `COTRN00.CPY` | `COTRN00.bms` | Transaction List screen fields |
| 12 | `COTRN01.CPY` | `COTRN01.bms` | Transaction View screen fields |
| 13 | `COTRN02.CPY` | `COTRN02.bms` | Transaction Add screen fields |
| 14 | `COUSR00.CPY` | `COUSR00.bms` | User List screen fields |
| 15 | `COUSR01.CPY` | `COUSR01.bms` | User Add screen fields |
| 16 | `COUSR02.CPY` | `COUSR02.bms` | User Update screen fields |
| 17 | `COUSR03.CPY` | `COUSR03.bms` | User Delete screen fields |

---

## BMS Maps (Screen Definitions)

Each BMS map defines a 3270 terminal screen layout with labeled fields, attributes, and positioning.

| # | Map | Screen | Used By | Description |
|---|-----|--------|---------|-------------|
| 1 | `COSGN00.bms` | Sign-On | COSGN00C | User ID and password input with error messages |
| 2 | `COMEN01.bms` | Main Menu | COMEN01C | Numbered option list for regular users |
| 3 | `COACTVW.bms` | Account View | COACTVWC | Account details, balances, card list display |
| 4 | `COACTUP.bms` | Account Update | COACTUPC | Editable account fields with validation indicators |
| 5 | `COCRDLI.bms` | Card List | COCRDLIC | Paginated card listing with selection |
| 6 | `COCRDSL.bms` | Card View | COCRDSLC | Card details display (read-only) |
| 7 | `COCRDUP.bms` | Card Update | COCRDUPC | Editable card fields |
| 8 | `COTRN00.bms` | Transaction List | COTRN00C | Paginated transaction listing with filters |
| 9 | `COTRN01.bms` | Transaction View | COTRN01C | Transaction detail display |
| 10 | `COTRN02.bms` | Transaction Add | COTRN02C | New transaction input form |
| 11 | `CORPT00.bms` | Reports | CORPT00C | Report parameter input (date range, type) |
| 12 | `COBIL00.bms` | Bill Payment | COBIL00C | Payment amount and account input |
| 13 | `COADM01.bms` | Admin Menu | COADM01C | Admin option list |
| 14 | `COUSR00.bms` | User List | COUSR00C | Paginated user listing |
| 15 | `COUSR01.bms` | User Add | COUSR01C | New user input form |
| 16 | `COUSR02.bms` | User Update | COUSR02C | User edit form |
| 17 | `COUSR03.bms` | User Delete | COUSR03C | User delete confirmation |

---

## JCL Jobs

### Data Load / Refresh Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 1 | `ACCTFILE.jcl` | CBACT01C | Refresh account master VSAM from sequential file |
| 2 | `CARDFILE.jcl` | CBACT02C | Refresh card master VSAM from sequential file |
| 3 | `CUSTFILE.jcl` | CBCUS01C | Refresh customer master VSAM from sequential file |
| 4 | `XREFFILE.jcl` | CBACT03C | Load card cross-reference VSAM |
| 5 | `TRANFILE.jcl` | CBTRN01C | Load daily transactions into transaction master |
| 6 | `DUSRSECJ.jcl` | (IDCAMS/REPRO) | Load user security VSAM from sequential data |
| 7 | `DEFCUST.jcl` | (IDCAMS) | Define customer VSAM cluster |

### Core Batch Processing Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 8 | `POSTTRAN.jcl` | CBTRN02C | Post daily transactions to accounts |
| 9 | `INTCALC.jcl` | CBACT04C | Calculate interest on account balances |
| 10 | `COMBTRAN.jcl` | (SORT/MERGE) | Combine daily transactions with master file |
| 11 | `CREASTMT.JCL` | CBSTM03A | Generate customer statements |
| 12 | `TRANBKP.jcl` | (IDCAMS/REPRO) | Backup transaction file |
| 13 | `TRANIDX.jcl` | (IDCAMS) | Define alternate index on transaction file |

### Reporting Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 14 | `TRANREPT.jcl` | CBTRN03C | Generate daily transaction report |
| 15 | `PRTCATBL.jcl` | (utility) | Print transaction category balance report |
| 16 | `TCATBALF.jcl` | (utility) | Transaction category balance file processing |
| 17 | `REPTFILE.jcl` | (utility) | Report file processing |

### File Management Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 18 | `CLOSEFIL.jcl` | (CICS CEMT) | Close CICS files for batch processing window |
| 19 | `OPENFIL.jcl` | (CICS CEMT) | Reopen CICS files after batch processing |
| 20 | `DEFGDGB.jcl` | (IDCAMS) | Define Generation Data Group base for backups |
| 21 | `DEFGDGD.jcl` | (IDCAMS) | Define Generation Data Group for daily files |
| 22 | `ESDSRRDS.jcl` | (IDCAMS) | Define ESDS/RRDS VSAM datasets |

### Data Export/Import Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 23 | `CBEXPORT.jcl` | CBEXPORT | Export all VSAM data to sequential file |
| 24 | `CBIMPORT.jcl` | CBIMPORT | Import sequential data into VSAM datasets |

### Utility and Read Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 25 | `READACCT.jcl` | (utility) | Read and display account VSAM records |
| 26 | `READCARD.jcl` | (utility) | Read and display card VSAM records |
| 27 | `READCUST.jcl` | (utility) | Read and display customer VSAM records |
| 28 | `READXREF.jcl` | (utility) | Read and display cross-reference VSAM records |
| 29 | `WAITSTEP.jcl` | COBSWAIT | Execute wait utility for batch timing |
| 30 | `TXT2PDF1.JCL` | (utility) | Convert text reports to PDF |
| 31 | `FTPJCL.JCL` | (FTP) | FTP file transfer job |

### Administrative / Special Purpose Jobs

| # | JCL | Executes | Description |
|---|-----|----------|-------------|
| 32 | `TRANCATG.jcl` | (utility) | Transaction category maintenance |
| 33 | `TRANTYPE.jcl` | (utility) | Transaction type maintenance |
| 34 | `DISCGRP.jcl` | (utility) | Disclosure group maintenance |
| 35 | `DALYREJS.jcl` | (utility) | Daily rejects processing |
| 36 | `CBADMCDJ.jcl` | (utility) | Admin card demo job |
| 37 | `INTRDRJ1.JCL` | (internal reader) | Internal reader job submission (1) |
| 38 | `INTRDRJ2.JCL` | (internal reader) | Internal reader job submission (2) |

---

## Assembler Programs

| # | Program | Description |
|---|---------|-------------|
| 1 | `MVSWAIT.asm` | **MVS Wait** -- Assembler routine that issues STIMER WAIT for timed delays; called by COBSWAIT |
| 2 | `COBDATFT.asm` | **COBOL Date Format** -- Assembler date formatting utility |

---

## JCL Procedures

| # | Procedure | Description |
|---|-----------|-------------|
| 1 | `REPROC.prc` | Reusable procedure for REPRO (IDCAMS copy) operations |
| 2 | `TRANREPT.prc` | Procedure for transaction report generation |

---

## Optional Modules

### Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for pending authorization management.

| # | Program | Type | Description |
|---|---------|------|-------------|
| 1 | `COPAUA0C.cbl` | Online | MQ trigger monitor for authorization requests |
| 2 | `COPAUS0C.cbl` | Online | Pending authorization summary screen |
| 3 | `COPAUS1C.cbl` | Online | Pending authorization detail view |
| 4 | `COPAUS2C.cbl` | Online | Fraud marking -- writes to DB2 |
| 5 | `CBPAUP0C.cbl` | Batch | Batch purge of processed authorizations |
| 6 | `DBUNLDGS.CBL` | Batch | Database unload utility |
| 7 | `PAUDBLOD.CBL` | Batch | Pending authorization DB load |
| 8 | `PAUDBUNL.CBL` | Batch | Pending authorization DB unload |

### Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based transaction type management with embedded SQL.

| # | Program | Type | Description |
|---|---------|------|-------------|
| 1 | `COTRTLIC.cbl` | Online | Transaction type list with DB2 cursor |
| 2 | `COTRTUPC.cbl` | Online | Transaction type add/edit with DB2 INSERT/UPDATE |
| 3 | `COBTUPDT.cbl` | Batch | Batch transaction type update |

### VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response for system date and account inquiry.

| # | Program | Type | Description |
|---|---------|------|-------------|
| 1 | `CODATE01.cbl` | Online | MQ-based system date service (CDRD transaction) |
| 2 | `COACCT01.cbl` | Online | MQ-based account inquiry service (CDRA transaction) |

---

## Supporting Assets

| Asset | Location | Description |
|-------|----------|-------------|
| CSD Definitions | `app/csd/CARDDEMO.CSD` | CICS resource definitions (programs, transactions, files, mapsets) |
| Sample Data (ASCII) | `app/data/ASCII/` | Test data files in ASCII format |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | Test data files in EBCDIC for mainframe upload |
| Control Files | `app/ctl/` | IDCAMS and utility control statements |
| Catalog Listings | `app/catlg/` | Dataset catalog listings |
| Assembler Macros | `app/maclib/` | Macro library for assembler programs |
| CA7 Schedule | `app/scheduler/CardDemo.ca7` | CA7 job scheduler configuration |
| Control-M Schedule | `app/scheduler/CardDemo.controlm` | Control-M job scheduler configuration |
| Shell Scripts | `scripts/` | FTP-based mainframe interaction scripts |
| Architecture Diagrams | `diagrams/` | Visual architecture documentation |

---

## Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | `COSGN00C` (Sign-On) |
| `CB*` | Batch program | `CBTRN02C` (Transaction Posting) |
| `CV*` | Copybook -- data structure | `CVACT01Y` (Account Record) |
| `CS*` | Copybook -- shared/system | `CSDAT01Y` (Date/Time) |
| `CO*` (cpy) | Copybook -- COMMAREA/navigation | `COCOM01Y` (Communication Area) |
| `*Y` suffix | Copybook (data) | `CVACT01Y` |
| `*C` suffix | COBOL program | `COSGN00C` |
