# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL / BMS (3270 Terminal)

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [Online CICS Programs](#online-cics-programs)
3. [Batch COBOL Programs](#batch-cobol-programs)
4. [Utility / Subroutine Programs](#utility--subroutine-programs)
5. [Optional Module: Authorization (IMS/DB2/MQ)](#optional-module-authorization-imsdb2mq)
6. [Optional Module: Transaction Type (DB2)](#optional-module-transaction-type-db2)
7. [Optional Module: VSAM-MQ](#optional-module-vsam-mq)
8. [Copybooks (Data Structures)](#copybooks-data-structures)
9. [BMS Maps (Screen Definitions)](#bms-maps-screen-definitions)
10. [BMS-Generated Copybooks](#bms-generated-copybooks)
11. [JCL Batch Jobs](#jcl-batch-jobs)
12. [Assembler Programs](#assembler-programs)
13. [JCL Procedures](#jcl-procedures)
14. [Support Artifacts](#support-artifacts)

---

## Summary Statistics

| Artifact Type | Count | Location |
|---|---|---|
| Online CICS Programs | 16 | `app/cbl/` |
| Batch COBOL Programs | 13 | `app/cbl/` |
| Utility / Subroutine Programs | 2 | `app/cbl/` |
| Optional Module Programs | 10 | `app/app-*/cbl/` |
| Copybooks (data structures) | 32 | `app/cpy/` |
| Optional Module Copybooks | 8+ | `app/app-*/cpy/` |
| BMS Screen Maps | 17 | `app/bms/` |
| BMS-Generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Batch Jobs | 38 | `app/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| **Total Source Artifacts** | **~157** | |

Total COBOL LOC (core): **~20,650 lines** | Optional modules: **~8,473 lines** | Grand total: **~29,123 lines**

---

## Online CICS Programs

These programs handle interactive 3270 terminal sessions via CICS transactions.

| # | Program ID | File | Lines | Business Function | CICS Trans | BMS Map | Classification |
|---|---|---|---|---|---|---|---|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | 260 | **Sign-On / Login** — Authenticates users against USRSEC VSAM file, routes to Main Menu (regular) or Admin Menu (admin) | CC00 | COSGN00 | Security |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | 308 | **Main Menu** — Displays menu options, dispatches to selected function via XCTL | CM00 | COMEN01 | Navigation |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | 288 | **Admin Menu** — Admin-only menu for user management functions | CA00 | COADM01 | Navigation / Admin |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | 941 | **Account View** — Displays account details by reading Account, Card-Xref, and Customer VSAM files | CA01 | COACTVW | Account Mgmt |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | 4,236 | **Account Update** — Full account editing with field validation, reads/writes Account, Card, Customer, Xref files | CA02 | COACTUP | Account Mgmt |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | 1,459 | **Card List** — Browse/page through credit cards using STARTBR/READNEXT/READPREV | CC01 | COCRDLI | Card Mgmt |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | 887 | **Card Detail View** — Display detailed card information from Card and Account files | CC02 | COCRDSL | Card Mgmt |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | 1,560 | **Card Update** — Edit card data with validation, REWRITE to VSAM | CC03 | COCRDUP | Card Mgmt |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | 699 | **Transaction List** — Browse daily transactions with page-forward/backward | CT00 | COTRN00 | Transaction Mgmt |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | 330 | **Transaction View** — Display a single transaction record detail | CT01 | COTRN01 | Transaction Mgmt |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | 783 | **Transaction Add** — Add a new transaction with date validation (calls CSUTLDTC) and VSAM WRITE | CT02 | COTRN02 | Transaction Mgmt |
| 12 | COBIL00C | `app/cbl/COBIL00C.cbl` | 572 | **Bill Payment** — Process bill payments, updates account balance via REWRITE | CB00 | COBIL00 | Billing |
| 13 | CORPT00C | `app/cbl/CORPT00C.cbl` | 649 | **Transaction Report Request** — Submit report generation with date range, validates dates via CSUTLDTC, writes to TDQ | CR00 | CORPT00 | Reporting |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | 695 | **User List** — Admin: browse/page through users in USRSEC file | CU00 | COUSR00 | User Admin |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | 299 | **User Add** — Admin: add a new Regular/Admin user to USRSEC file | CU01 | COUSR01 | User Admin |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | 414 | **User Update** — Admin: modify existing user record | CU02 | COUSR02 | User Admin |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | 359 | **User Delete** — Admin: remove user from USRSEC file | CU03 | COUSR03 | User Admin |

---

## Batch COBOL Programs

These programs run as JCL-submitted batch jobs for overnight/scheduled processing.

| # | Program ID | File | Lines | Business Function | Classification |
|---|---|---|---|---|---|
| 1 | CBACT01C | `app/cbl/CBACT01C.cbl` | 430 | **Account File Reader** — Reads ACCTDATA VSAM, writes flat files (fixed, array, variable-length). Calls COBDATFT for date formatting | Data Utility |
| 2 | CBACT02C | `app/cbl/CBACT02C.cbl` | 178 | **Card File Reader** — Reads and prints CARDDATA VSAM file contents | Data Utility |
| 3 | CBACT03C | `app/cbl/CBACT03C.cbl` | 178 | **Cross-Reference Reader** — Reads and prints CARDXREF VSAM file | Data Utility |
| 4 | CBACT04C | `app/cbl/CBACT04C.cbl` | 652 | **Interest Calculator** — Computes interest and fees per transaction category balance using disclosure group rates; updates account records | Financial Core |
| 5 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | 178 | **Customer File Reader** — Reads and prints CUSTDATA VSAM file | Data Utility |
| 6 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | 494 | **Transaction Posting (v1)** — Posts daily transactions to master file after cross-reference validation | Transaction Processing |
| 7 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | 731 | **Transaction Posting (v2)** — Enhanced posting with reject file, account balance updates, and category balance tracking | Transaction Processing |
| 8 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | 649 | **Transaction Report** — Generates daily transaction detail report with account/page/grand totals | Reporting |
| 9 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | 924 | **Statement Generator (Main)** — Produces account statements in plain text and HTML format. Calls CBSTM03B for file I/O | Reporting |
| 10 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | 230 | **Statement Generator (Sub)** — Subroutine called by CBSTM03A; handles file open/close/read operations | Reporting |
| 11 | CBEXPORT | `app/cbl/CBEXPORT.cbl` | 582 | **Data Export** — Exports customer, account, card, xref, and transaction data into multi-record export file for branch migration | Data Migration |
| 12 | CBIMPORT | `app/cbl/CBIMPORT.cbl` | 487 | **Data Import** — Imports multi-record export file, splits into normalized target files with validation | Data Migration |

---

## Utility / Subroutine Programs

| # | Program ID | File | Lines | Business Function | Classification |
|---|---|---|---|---|---|
| 1 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | 157 | **Date Validation Utility** — Calls LE CEEDAYS API to validate dates; used by COTRN02C and CORPT00C | Shared Utility |
| 2 | COBSWAIT | `app/cbl/COBSWAIT.cbl` | 41 | **Wait Utility** — Calls assembler MVSWAIT to introduce a timed pause in batch job steps | Shared Utility |

---

## Optional Module: Authorization (IMS/DB2/MQ)

Location: `app/app-authorization-ims-db2-mq/`

| # | Program ID | Lines | Business Function | Technology |
|---|---|---|---|---|
| 1 | COPAUA0C | 1,026 | **MQ Trigger for Authorization** — Receives MQ messages, processes pending authorizations | CICS + MQ |
| 2 | COPAUS0C | 1,032 | **Authorization Summary** — Displays pending authorization summary from IMS DB | CICS + IMS DB |
| 3 | COPAUS1C | 604 | **Authorization Details** — Shows detailed authorization records | CICS + IMS DB |
| 4 | COPAUS2C | 244 | **Fraud Marking** — Marks transactions as fraudulent in DB2 | CICS + DB2 |
| 5 | CBPAUP0C | 386 | **Batch Purge** — Purges expired pending authorizations | Batch + IMS DB |

Additional copybooks: `CIPAUATY.cpy`, `CIPAUDTY.cpy`, `CIPAUSMY.cpy`, `IMSFUNCS.cpy`

---

## Optional Module: Transaction Type (DB2)

Location: `app/app-transaction-type-db2/`

| # | Program ID | Lines | Business Function | Technology |
|---|---|---|---|---|
| 1 | COTRTLIC | 2,098 | **Transaction Type List** — List/page/select/delete transaction types using DB2 cursors | CICS + DB2 |
| 2 | COTRTUPC | 1,702 | **Transaction Type Update** — Add/edit transaction types in DB2 with SYNCPOINT | CICS + DB2 |
| 3 | COBTUPDT | 237 | **Batch Transaction Type Update** — Batch DB2 update of transaction types | Batch + DB2 |

Additional copybooks: `CSDB2RPY.cpy`, `CSDB2RWY.cpy`

---

## Optional Module: VSAM-MQ

Location: `app/app-vsam-mq/`

| # | Program ID | Lines | Business Function | Technology |
|---|---|---|---|---|
| 1 | COACCT01 | 620 | **Account Inquiry via MQ** — Receives MQ request, reads account VSAM, returns MQ response | CICS + MQ + VSAM |
| 2 | CODATE01 | 524 | **System Date via MQ** — Receives MQ request, returns current system date/time | CICS + MQ |

---

## Copybooks (Data Structures)

| # | Copybook | File | Purpose | Used By |
|---|---|---|---|---|
| 1 | CVACT01Y | `app/cpy/CVACT01Y.cpy` | Account master record (300 bytes) | CBACT01C, CBACT04C, COACTUPC, COACTVWC, CBTRN01C, CBEXPORT, CBIMPORT, COACCT01 |
| 2 | CVACT02Y | `app/cpy/CVACT02Y.cpy` | Card data record (150 bytes) | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT, COTRTLIC |
| 3 | CVACT03Y | `app/cpy/CVACT03Y.cpy` | Card cross-reference record | CBACT03C, CBACT04C, COACTUPC, COACTVWC, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| 4 | CVCUS01Y | `app/cpy/CVCUS01Y.cpy` | Customer data record (500 bytes) | CBCUS01C, COACTUPC, CBTRN01C, CBEXPORT, CBIMPORT |
| 5 | CVCRD01Y | `app/cpy/CVCRD01Y.cpy` | Extended card data record | COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC |
| 6 | CVTRA01Y | `app/cpy/CVTRA01Y.cpy` | Transaction category balance (50 bytes) | CBACT04C, CBTRN02C |
| 7 | CVTRA02Y | `app/cpy/CVTRA02Y.cpy` | Disclosure group record (50 bytes) | CBACT04C |
| 8 | CVTRA03Y | `app/cpy/CVTRA03Y.cpy` | Transaction type record (60 bytes) | CBTRN03C |
| 9 | CVTRA04Y | `app/cpy/CVTRA04Y.cpy` | Transaction category type (60 bytes) | CBTRN03C |
| 10 | CVTRA05Y | `app/cpy/CVTRA05Y.cpy` | Transaction record (350 bytes) | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, COTRN02C |
| 11 | CVTRA06Y | `app/cpy/CVTRA06Y.cpy` | Daily transaction record (350 bytes) | CBTRN01C, CBTRN02C |
| 12 | CVTRA07Y | `app/cpy/CVTRA07Y.cpy` | Transaction report layout | CBTRN03C |
| 13 | CVEXPORT | `app/cpy/CVEXPORT.cpy` | Export/import multi-record layout | CBEXPORT, CBIMPORT |
| 14 | CSUSR01Y | `app/cpy/CSUSR01Y.cpy` | User security record | COSGN00C, all COUSR*, all online programs |
| 15 | COCOM01Y | `app/cpy/COCOM01Y.cpy` | Common communication area (COMMAREA) | All online CICS programs |
| 16 | COMEN02Y | `app/cpy/COMEN02Y.cpy` | Menu option definitions | COMEN01C |
| 17 | COADM02Y | `app/cpy/COADM02Y.cpy` | Admin menu option definitions | COADM01C |
| 18 | COTTL01Y | `app/cpy/COTTL01Y.cpy` | Screen title/header layout | All online CICS programs |
| 19 | CSDAT01Y | `app/cpy/CSDAT01Y.cpy` | Date handling data structures | All online CICS programs |
| 20 | CSMSG01Y | `app/cpy/CSMSG01Y.cpy` | Short message area | All online CICS programs |
| 21 | CSMSG02Y | `app/cpy/CSMSG02Y.cpy` | Long message area | COACTUPC, COCRDUPC, COTRTUPC |
| 22 | CSSETATY | `app/cpy/CSSETATY.cpy` | Screen attribute setting (REPLACING) | COACTUPC, COTRTUPC |
| 23 | CSSTRPFY | `app/cpy/CSSTRPFY.cpy` | String parsing functions | COTRTLIC, COTRTUPC |
| 24 | CSLKPCDY | `app/cpy/CSLKPCDY.cpy` | Lookup code table (51 KB) | COACTUPC |
| 25 | CSUTLDPY | `app/cpy/CSUTLDPY.cpy` | Date utility parameters | CSUTLDTC |
| 26 | CSUTLDWY | `app/cpy/CSUTLDWY.cpy` | Date utility working storage | COACTUPC, COTRTUPC |
| 27 | CODATECN | `app/cpy/CODATECN.cpy` | Date conversion record for assembler | CBACT01C |
| 28 | COSTM01 | `app/cpy/COSTM01.CPY` | Statement transaction layout | CBSTM03A |
| 29 | CUSTREC | `app/cpy/CUSTREC.cpy` | Alternate customer record layout | CBSTM03A |
| 30 | UNUSED1Y | `app/cpy/UNUSED1Y.cpy` | Unused/placeholder record | None (dead code) |

---

## BMS Maps (Screen Definitions)

Each BMS map defines a 3270 terminal screen layout for a CICS online program.

| # | Map Name | File | Lines | Screen Title | Associated Program |
|---|---|---|---|---|---|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | 210 | Login Screen | COSGN00C |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | 167 | Main Menu | COMEN01C |
| 3 | COADM01 | `app/bms/COADM01.bms` | 167 | Admin Menu | COADM01C |
| 4 | COACTVW | `app/bms/COACTVW.bms` | 378 | Account View | COACTVWC |
| 5 | COACTUP | `app/bms/COACTUP.bms` | 512 | Account Update | COACTUPC |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | 344 | Card List | COCRDLIC |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | 157 | Card Detail View | COCRDSLC |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | 172 | Card Update | COCRDUPC |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | 464 | Transaction List | COTRN00C |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | 273 | Transaction View | COTRN01C |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | 307 | Transaction Add | COTRN02C |
| 12 | COBIL00 | `app/bms/COBIL00.bms` | 141 | Bill Payment | COBIL00C |
| 13 | CORPT00 | `app/bms/CORPT00.bms` | 231 | Transaction Report | CORPT00C |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | 463 | User List | COUSR00C |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | 164 | Add User | COUSR01C |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | 169 | Update User | COUSR02C |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | 153 | Delete User | COUSR03C |

---

## BMS-Generated Copybooks

Auto-generated COBOL copybooks from BMS map assembly. One per BMS map.

| # | Copybook | File | Size | Source BMS |
|---|---|---|---|---|
| 1 | COSGN00 | `app/cpy-bms/COSGN00.CPY` | 6,302 B | COSGN00.bms |
| 2 | COMEN01 | `app/cpy-bms/COMEN01.CPY` | 10,506 B | COMEN01.bms |
| 3 | COADM01 | `app/cpy-bms/COADM01.CPY` | 10,506 B | COADM01.bms |
| 4 | COACTVW | `app/cpy-bms/COACTVW.CPY` | 18,399 B | COACTVW.bms |
| 5 | COACTUP | `app/cpy-bms/COACTUP.CPY` | 26,048 B | COACTUP.bms |
| 6 | COCRDLI | `app/cpy-bms/COCRDLI.CPY` | 22,016 B | COCRDLI.bms |
| 7 | COCRDSL | `app/cpy-bms/COCRDSL.CPY` | 8,172 B | COCRDSL.bms |
| 8 | COCRDUP | `app/cpy-bms/COCRDUP.CPY` | 9,074 B | COCRDUP.bms |
| 9 | COTRN00 | `app/cpy-bms/COTRN00.CPY` | 28,494 B | COTRN00.bms |
| 10 | COTRN01 | `app/cpy-bms/COTRN01.CPY` | 10,784 B | COTRN01.bms |
| 11 | COTRN02 | `app/cpy-bms/COTRN02.CPY` | 10,802 B | COTRN02.bms |
| 12 | COBIL00 | `app/cpy-bms/COBIL00.CPY` | 5,886 B | COBIL00.bms |
| 13 | CORPT00 | `app/cpy-bms/CORPT00.CPY` | 9,012 B | CORPT00.bms |
| 14 | COUSR00 | `app/cpy-bms/COUSR00.CPY` | 28,472 B | COUSR00.bms |
| 15 | COUSR01 | `app/cpy-bms/COUSR01.CPY` | 6,756 B | COUSR01.bms |
| 16 | COUSR02 | `app/cpy-bms/COUSR02.CPY` | 6,766 B | COUSR02.bms |
| 17 | COUSR03 | `app/cpy-bms/COUSR03.CPY` | 6,316 B | COUSR03.bms |

---

## JCL Batch Jobs

### Data Loading & Refresh Jobs

| # | Job Name | File | Lines | Purpose | Key Datasets |
|---|---|---|---|---|---|
| 1 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | 63 | Refresh account master VSAM from flat file | `ACCTDATA.VSAM.KSDS`, `ACCTDATA.PS` |
| 2 | CARDFILE | `app/jcl/CARDFILE.jcl` | 127 | Refresh card master VSAM + alternate indexes | `CARDDATA.VSAM.KSDS`, `CARDDATA.PS` |
| 3 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | 83 | Refresh customer master VSAM | `CUSTDATA.VSAM.KSDS`, `CUSTDATA.PS` |
| 4 | XREFFILE | `app/jcl/XREFFILE.jcl` | 106 | Load card-account cross-reference VSAM + AIX | `CARDXREF.VSAM.KSDS`, `CARDXREF.PS` |
| 5 | TRANFILE | `app/jcl/TRANFILE.jcl` | 125 | Load transaction master VSAM + alternate indexes | `TRANSACT.VSAM.KSDS`, `DALYTRAN.PS.INIT` |
| 6 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | 54 | Load user security VSAM from flat file | `USRSEC.VSAM.KSDS`, `USRSEC.PS` |
| 7 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | 65 | Load transaction type reference VSAM | `TRANTYPE.VSAM.KSDS`, `TRANTYPE.PS` |
| 8 | TRANCATG | `app/jcl/TRANCATG.jcl` | 65 | Load transaction category reference VSAM | `TRANCATG.VSAM.KSDS`, `TRANCATG.PS` |
| 9 | TCATBALF | `app/jcl/TCATBALF.jcl` | 65 | Load transaction category balance VSAM | `TCATBALF.VSAM.KSDS`, `TCATBALF.PS` |
| 10 | DISCGRP | `app/jcl/DISCGRP.jcl` | 65 | Load disclosure group (interest rate) VSAM | `DISCGRP.VSAM.KSDS`, `DISCGRP.PS` |
| 11 | DEFCUST | `app/jcl/DEFCUST.jcl` | 35 | Define customer VSAM cluster | `CUSTDATA.VSAM.KSDS` |

### Core Batch Processing Jobs

| # | Job Name | File | Lines | Purpose | Program Executed |
|---|---|---|---|---|---|
| 12 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | 43 | Post daily transactions to master file | CBTRN02C |
| 13 | INTCALC | `app/jcl/INTCALC.jcl` | 39 | Calculate interest on category balances | CBACT04C |
| 14 | TRANBKP | `app/jcl/TRANBKP.jcl` | 71 | Backup transaction file to GDG | REPROC (proc) |
| 15 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | 48 | Combine daily transactions into master | IDCAMS REPRO |
| 16 | CREASTMT | `app/jcl/CREASTMT.JCL` | 97 | Generate account statements (text + HTML) | SORT + CBSTM03A |
| 17 | TRANREPT | `app/jcl/TRANREPT.jcl` | 84 | Produce daily transaction report | SORT + CBTRN03C |
| 18 | TRANIDX | `app/jcl/TRANIDX.jcl` | 58 | Define/build alternate indexes on TRANSACT | IDCAMS |
| 19 | CBEXPORT | `app/jcl/CBEXPORT.jcl` | 39 | Export customer data for migration | CBEXPORT |
| 20 | CBIMPORT | `app/jcl/CBIMPORT.jcl` | 36 | Import customer data from export file | CBIMPORT |

### CICS File Control Jobs

| # | Job Name | File | Lines | Purpose |
|---|---|---|---|---|
| 21 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | 30 | Close CICS files before batch processing |
| 22 | OPENFIL | `app/jcl/OPENFIL.jcl` | 30 | Re-open CICS files after batch processing |

### VSAM Definition & Utility Jobs

| # | Job Name | File | Lines | Purpose |
|---|---|---|---|---|
| 23 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | 63 | Define GDG bases for backup datasets |
| 24 | DEFGDGD | `app/jcl/DEFGDGD.jcl` | 50 | Define GDG bases for daily datasets |
| 25 | ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | 67 | Define ESDS and RRDS VSAM clusters |
| 26 | DALYREJS | `app/jcl/DALYREJS.jcl` | 30 | Define daily rejects sequential dataset |
| 27 | REPTFILE | `app/jcl/REPTFILE.jcl` | 30 | Define transaction report output dataset |
| 28 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | 83 | Administrative batch card processing |

### Data Reader/Verification Jobs

| # | Job Name | File | Lines | Purpose |
|---|---|---|---|---|
| 29 | READACCT | `app/jcl/READACCT.jcl` | 33 | Read/verify account VSAM file |
| 30 | READCARD | `app/jcl/READCARD.jcl` | 28 | Read/verify card VSAM file |
| 31 | READCUST | `app/jcl/READCUST.jcl` | 21 | Read/verify customer VSAM file |
| 32 | READXREF | `app/jcl/READXREF.jcl` | 28 | Read/verify cross-reference VSAM file |
| 33 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | 57 | Print IDCAMS catalog listings |

### Miscellaneous Jobs

| # | Job Name | File | Lines | Purpose |
|---|---|---|---|---|
| 34 | WAITSTEP | `app/jcl/WAITSTEP.jcl` | 27 | Execute COBSWAIT for timed delay |
| 35 | FTPJCL | `app/jcl/FTPJCL.JCL` | 42 | FTP file transfer job |
| 36 | INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | 19 | Internal reader job chain (step 1) |
| 37 | INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | 14 | Internal reader job chain (step 2) |
| 38 | TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | 41 | Convert statement text to PDF |

---

## Assembler Programs

| # | Program | File | Purpose |
|---|---|---|---|
| 1 | MVSWAIT | `app/asm/MVSWAIT.asm` | Wait/delay subroutine called by COBSWAIT |
| 2 | COBDATFT | `app/asm/COBDATFT.asm` | Date formatting subroutine called by CBACT01C |

---

## JCL Procedures

| # | Procedure | File | Purpose |
|---|---|---|---|
| 1 | REPROC | `app/proc/REPROC.prc` | Reusable REPRO (copy) procedure for VSAM file backup |
| 2 | TRANREPT | `app/proc/TRANREPT.prc` | Transaction report generation procedure |

---

## Support Artifacts

| Category | Location | Contents |
|---|---|---|
| Control Files | `app/ctl/REPROCT.ctl` | REPRO control cards |
| Catalog Listings | `app/catlg/LISTCAT.txt` | IDCAMS LISTCAT output (196 KB) |
| Assembler Macros | `app/maclib/` | ASMWAIT.mac, COCDATFT.mac |
| Scheduler Configs | `app/scheduler/` | CardDemo.ca7 (CA7), CardDemo.controlm (Control-M) |
| CSD Definitions | `app/csd/` | CARDDEMO.CSD — CICS resource definitions |
| Sample Data (ASCII) | `app/data/ASCII/` | Test data files in ASCII format |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | Test data files in EBCDIC for mainframe upload |
| Scripts | `scripts/` | Shell scripts for mainframe FTP interaction |
| Diagrams | `diagrams/` | Architecture diagrams and screen captures |
