# CardDemo Application Inventory

> **Application:** AWS CardDemo &mdash; Mainframe Credit Card Management System  
> **Generated:** 2026-03-25 | **Source Repo:** `uc-legacy-modernization-cobol-to-java`

---

## Executive Summary

| Asset Type | Count | Location |
|---|---|---|
| COBOL Programs (Core) | 31 | `app/cbl/` |
| COBOL Programs (Optional Modules) | 10 | `app/app-*/cbl/` |
| Copybooks (Data) | 30 | `app/cpy/` |
| Copybooks (BMS-generated) | 17 | `app/cpy-bms/` |
| BMS Screen Maps | 17 | `app/bms/` |
| JCL Batch Jobs | 38 | `app/jcl/` |
| JCL Procedures | 2 | `app/proc/` |
| Assembler Programs | 2 | `app/asm/` |
| Scheduler Configs | 2 | `app/scheduler/` |
| **Total Artifacts** | **149** | |

---

## 1. COBOL Programs &mdash; Core (`app/cbl/`)

### 1.1 Online CICS Programs (17 programs)

These programs run under CICS and handle real-time user interactions via 3270 terminal screens.

| # | Program ID | Lines | Function | CICS Cmds | BMS Map | Transaction |
|---|---|---|---|---|---|---|
| 1 | **COSGN00C** | 260 | Signon / Authentication | 10 | COSGN00 | CC00 |
| 2 | **COMEN01C** | 308 | Main Menu (Regular Users) | 7 | COMEN01 | CM00 |
| 3 | **COADM01C** | 288 | Admin Menu | 7 | COADM01 | CA00 |
| 4 | **COACTVWC** | 941 | Account View | 15 | COACTVW | CA01 |
| 5 | **COACTUPC** | 4,236 | Account Update | 17 | COACTUP | CA02 |
| 6 | **COCRDLIC** | 1,459 | Credit Card List | 18 | COCRDLI | CC01 |
| 7 | **COCRDSLC** | 887 | Credit Card View (Detail) | 14 | COCRDSL | CC02 |
| 8 | **COCRDUPC** | 1,560 | Credit Card Update | 12 | COCRDUP | CC03 |
| 9 | **COTRN00C** | 699 | Transaction List | 10 | COTRN00 | CT00 |
| 10 | **COTRN01C** | 330 | Transaction View | 5 | COTRN01 | CT01 |
| 11 | **COTRN02C** | 783 | Transaction Add | 11 | COTRN02 | CT02 |
| 12 | **CORPT00C** | 649 | Transaction Report Request | 7 | CORPT00 | CR00 |
| 13 | **COBIL00C** | 572 | Bill Payment | 13 | COBIL00 | CB00 |
| 14 | **COUSR00C** | 695 | User List (Admin) | 11 | COUSR00 | CU00 |
| 15 | **COUSR01C** | 299 | User Add (Admin) | 5 | COUSR01 | CU01 |
| 16 | **COUSR02C** | 414 | User Update (Admin) | 6 | COUSR02 | CU02 |
| 17 | **COUSR03C** | 359 | User Delete (Admin) | 6 | COUSR03 | CU03 |

### 1.2 Batch Programs (14 programs)

These programs run in batch (JCL-invoked) for end-of-day / periodic processing.

| # | Program ID | Lines | Function | External CALLs | Key Copybooks |
|---|---|---|---|---|---|
| 18 | **CBACT01C** | 430 | Account file reader &mdash; read VSAM and write to flat files | COBDATFT, CEE3ABD | CVACT01Y, CODATECN |
| 19 | **CBACT02C** | 178 | Card data file reader &mdash; read and print | CEE3ABD | CVACT02Y |
| 20 | **CBACT03C** | 178 | Cross-reference file reader &mdash; read and print | CEE3ABD | CVACT03Y |
| 21 | **CBACT04C** | 652 | **Interest calculator** &mdash; compute interest and fees per account | CEE3ABD | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 22 | **CBCUS01C** | 178 | Customer data file reader &mdash; read and print | CEE3ABD | CVCUS01Y |
| 23 | **CBTRN01C** | 494 | Daily transaction posting (simple) | CEE3ABD | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| 24 | **CBTRN02C** | 731 | **Daily transaction posting** (with validation, reject handling) | CEE3ABD | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| 25 | **CBTRN03C** | 649 | **Transaction detail report** &mdash; formatted print | CEE3ABD | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| 26 | **CBSTM03A** | 924 | **Statement generation** &mdash; text and HTML output | CBSTM03B, CEE3ABD | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| 27 | **CBSTM03B** | 230 | Statement subroutine &mdash; file I/O helper for CBSTM03A | (none) | (file definitions) |
| 28 | **CBEXPORT** | 582 | **Data export** &mdash; branch migration multi-record export | CEE3ABD | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 29 | **CBIMPORT** | 487 | **Data import** &mdash; branch migration import with validation | CEE3ABD | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 30 | **COBSWAIT** | 41 | Utility &mdash; wait timer (calls ASM MVSWAIT) | MVSWAIT | (none) |
| 31 | **CSUTLDTC** | 157 | Utility &mdash; date validation via LE CEEDAYS | CEEDAYS | (none) |

---

## 2. Optional Module Programs

### 2.1 Authorization Module &mdash; IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program ID | Lines | Type | Function |
|---|---|---|---|---|
| 32 | **COPAUA0C** | 1,026 | CICS/IMS/MQ | Card authorization decision &mdash; MQ trigger, reads request queue, writes response |
| 33 | **COPAUS0C** | 1,032 | CICS/IMS/BMS | Authorization summary view |
| 34 | **COPAUS1C** | 604 | CICS/IMS/BMS | Authorization detail view |
| 35 | **COPAUS2C** | 244 | CICS/IMS/DB2 | Mark authorization as fraud (writes to DB2) |
| 36 | **CBPAUP0C** | 386 | Batch/IMS | Purge expired pending authorization messages |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program ID | Lines | Type | Function |
|---|---|---|---|---|
| 37 | **COTRTLIC** | 2,098 | CICS/DB2 | List transaction types (with update/delete) |
| 38 | **COTRTUPC** | 1,702 | CICS/DB2 | Add/edit transaction types |
| 39 | **COBTUPDT** | 237 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program ID | Lines | Type | Function |
|---|---|---|---|---|
| 40 | **COACCT01** | 620 | CICS/MQ | Account inquiry via MQ request/response |
| 41 | **CODATE01** | 524 | CICS/MQ | System date service via MQ request/response |

---

## 3. Copybooks &mdash; Data Structures (`app/cpy/`)

| # | Copybook | Lines | Record Layout | Record Len | Business Domain |
|---|---|---|---|---|---|
| 1 | **CVACT01Y** | 24 | ACCOUNT-RECORD | 300 bytes | Account master |
| 2 | **CVACT02Y** | 11 | CARD-RECORD | 150 bytes | Card master |
| 3 | **CVACT03Y** | 10 | CARD-XREF-RECORD | 50 bytes | Card-to-account cross-reference |
| 4 | **CVCUS01Y** | 29 | CUSTOMER-RECORD | 500 bytes | Customer master |
| 5 | **CVCRD01Y** | 14 | CARD-RECORD (detail) | 150 bytes | Card detail for online |
| 6 | **CVTRA01Y** | 13 | TRAN-CAT-BAL-RECORD | 50 bytes | Transaction category balance |
| 7 | **CVTRA02Y** | 13 | DIS-GROUP-RECORD | 50 bytes | Disclosure group (interest rates) |
| 8 | **CVTRA03Y** | 10 | TRAN-TYPE-RECORD | 60 bytes | Transaction type reference |
| 9 | **CVTRA04Y** | 12 | TRAN-CAT-RECORD | 60 bytes | Transaction category type |
| 10 | **CVTRA05Y** | 21 | TRAN-RECORD | 350 bytes | Transaction master |
| 11 | **CVTRA06Y** | 21 | DALYTRAN-RECORD | 350 bytes | Daily transaction (input) |
| 12 | **CVTRA07Y** | 73 | Report headers/totals | N/A | Transaction report formatting |
| 13 | **COSTM01** | 38 | TRNX-RECORD | 350 bytes | Statement-oriented transaction layout |
| 14 | **CVEXPORT** | 100 | Multi-record export layout | 500 bytes | Export/import data migration |
| 15 | **CSUSR01Y** | 12 | SEC-USER-DATA | 80 bytes | User security record |
| 16 | **COCOM01Y** | ~200 | CARDDEMO-COMMAREA | Variable | Inter-program communication area |
| 17 | **COMEN02Y** | ~30 | Menu definitions | N/A | Menu option tables |
| 18 | **COADM02Y** | ~30 | Admin menu definitions | N/A | Admin menu option tables |
| 19 | **COTTL01Y** | ~20 | Title/header area | N/A | Screen title constants |
| 20 | **CSDAT01Y** | ~15 | Date work area | N/A | Date formatting fields |
| 21 | **CSMSG01Y** | ~15 | Message area | N/A | Standard message fields |
| 22 | **CSMSG02Y** | ~15 | Extended message area | N/A | Extended message fields |
| 23 | **CODATECN** | ~10 | Date conversion area | N/A | Date format conversion |
| 24 | **CSSETATY** | ~10 | Screen attribute settings | N/A | BMS attribute bytes |
| 25 | **CSSTRPFY** | ~30 | String/pad functions | N/A | Utility string operations |
| 26 | **CSLKPCDY** | ~15 | Lookup code area | N/A | Code lookup definitions |
| 27 | **CSUTLDPY** | ~10 | Utility date parameters | N/A | Date utility parameters |
| 28 | **CSUTLDWY** | ~10 | Utility date work area | N/A | Date utility work fields |
| 29 | **CUSTREC** | ~30 | Customer record (alt layout) | 500 bytes | Alternate customer layout |
| 30 | **UNUSED1Y** | 10 | UNUSED-DATA | 80 bytes | Deprecated/unused record |

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map Name | Map Set | Lines | Screen Title | Function |
|---|---|---|---|---|---|
| 1 | **COSGN00** | COSGN0A | ~180 | Sign On | User login screen |
| 2 | **COMEN01** | COMEN1A | ~250 | Main Menu | Navigation menu (regular users) |
| 3 | **COADM01** | COADM1A | ~200 | Admin Menu | Navigation menu (admin users) |
| 4 | **COACTVW** | COACTVA | ~300 | Account View | Display account details |
| 5 | **COACTUP** | COACTUA | ~500 | Account Update | Edit account fields |
| 6 | **COCRDLI** | COCRDIA | ~300 | Card List | Scrollable list of credit cards |
| 7 | **COCRDSL** | COCRDSA | ~250 | Card Detail | View credit card details |
| 8 | **COCRDUP** | COCRDUA | ~350 | Card Update | Edit credit card fields |
| 9 | **COTRN00** | COTRN0A | ~250 | Transaction List | Scrollable transaction list |
| 10 | **COTRN01** | COTRN1A | ~200 | Transaction View | View transaction details |
| 11 | **COTRN02** | COTRN2A | ~350 | Transaction Add | Add new transaction |
| 12 | **CORPT00** | CORPT0A | ~200 | Transaction Report | Report date range selection |
| 13 | **COBIL00** | COBIL0A | ~200 | Bill Payment | Pay account balance |
| 14 | **COUSR00** | COUSR0A | ~250 | User List | Admin: list all users |
| 15 | **COUSR01** | COUSR1A | ~200 | Add User | Admin: create new user |
| 16 | **COUSR02** | COUSR2A | ~200 | Update User | Admin: modify user |
| 17 | **COUSR03** | COUSR3A | ~150 | Delete User | Admin: remove user |

### BMS-Generated Copybooks (`app/cpy-bms/`)

17 corresponding `.CPY` files are auto-generated from the BMS maps above. Each provides symbolic field names for SEND MAP / RECEIVE MAP operations in the COBOL programs (e.g., `COSGN00.CPY` for the sign-on screen).

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data Load / Refresh Jobs

| # | Job Name | Lines | Function | Key Datasets |
|---|---|---|---|---|
| 1 | **DUSRSECJ** | ~80 | Load user security VSAM file | USRSEC.VSAM.KSDS |
| 2 | **ACCTFILE** | ~60 | Define & load account master VSAM | ACCTDATA.VSAM.KSDS |
| 3 | **CARDFILE** | ~60 | Define & load card master VSAM | CARDDATA.VSAM.KSDS |
| 4 | **CUSTFILE** | ~70 | Define & load customer master VSAM | CUSTDATA.VSAM.KSDS |
| 5 | **XREFFILE** | ~80 | Define & load card cross-reference + alt index | CARDXREF.VSAM.KSDS |
| 6 | **TRANFILE** | ~70 | Load transaction master VSAM | TRANSACT.VSAM.KSDS |
| 7 | **TCATBALF** | ~60 | Load transaction category balance VSAM | TCATBALF.VSAM.KSDS |
| 8 | **TRANTYPE** | ~60 | Load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 9 | **TRANCATG** | ~60 | Load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 10 | **DISCGRP** | ~60 | Load disclosure group VSAM | DISCGRP.VSAM.KSDS |
| 11 | **DEFCUST** | ~50 | Define customer master cluster | CUSTDATA.VSAM.KSDS |
| 12 | **REPTFILE** | ~60 | Define transaction report VSAM | TRANSACT.REPORT |

### 5.2 Core Batch Processing Jobs

| # | Job Name | Lines | Function | Programs Invoked |
|---|---|---|---|---|
| 13 | **POSTTRAN** | ~80 | **Core transaction posting** | CBTRN02C |
| 14 | **INTCALC** | ~70 | **Interest calculation** | CBACT04C |
| 15 | **TRANBKP** | ~40 | Backup transaction file | IDCAMS (REPRO) |
| 16 | **COMBTRAN** | ~50 | Combine transaction backups | SORT, IDCAMS |
| 17 | **CREASTMT** | ~97 | **Statement generation** | SORT, CBSTM03A |
| 18 | **TRANREPT** | ~80 | **Transaction detail report** | SORT, CBTRN03C |
| 19 | **CBEXPORT** | ~65 | Export data for migration | CBEXPORT |
| 20 | **CBIMPORT** | ~60 | Import data from migration export | CBIMPORT |

### 5.3 CICS File Management Jobs

| # | Job Name | Lines | Function |
|---|---|---|---|
| 21 | **CLOSEFIL** | ~130 | Close CICS VSAM files for batch processing |
| 22 | **OPENFIL** | ~130 | Reopen CICS VSAM files after batch |

### 5.4 Infrastructure / Utility Jobs

| # | Job Name | Lines | Function |
|---|---|---|---|
| 23 | **DEFGDGB** | ~100 | Define GDG bases for transaction backup/report |
| 24 | **DEFGDGD** | ~90 | Define GDG bases + initial data load for ref tables |
| 25 | **TRANIDX** | ~60 | Define alternate index on transaction file |
| 26 | **DALYREJS** | ~60 | Define daily rejects file |
| 27 | **ESDSRRDS** | ~120 | Create ESDS/RRDS VSAM files (user security variants) |
| 28 | **PRTCATBL** | ~65 | Print and backup category balance file |
| 29 | **WAITSTEP** | ~25 | Wait step (calls COBSWAIT) |
| 30 | **TXT2PDF1** | ~41 | Convert text statement to PDF |
| 31 | **FTPJCL** | ~42 | FTP file transfer job |
| 32 | **INTRDRJ1** | ~19 | Internal reader job 1 (triggers INTRDRJ2) |
| 33 | **INTRDRJ2** | ~14 | Internal reader job 2 (copies FTP backup) |
| 34 | **READACCT** | ~40 | Read and print account file (uses CBACT01C) |
| 35 | **READCARD** | ~30 | Read and print card file (uses CBACT02C) |
| 36 | **READCUST** | ~30 | Read and print customer file (uses CBCUS01C) |
| 37 | **READXREF** | ~30 | Read and print cross-reference file (uses CBACT03C) |
| 38 | **CBADMCDJ** | ~40 | Admin card management batch job |

### Batch Cycle Execution Order

```
CLOSEFIL ──> Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, TCATBALF)
         ──> POSTTRAN (CBTRN02C)
         ──> INTCALC (CBACT04C)
         ──> TRANBKP
         ──> COMBTRAN
         ──> CREASTMT (CBSTM03A)
         ──> TRANREPT (CBTRN03C)
         ──> TRANIDX
         ──> OPENFIL
```

---

## 6. JCL Procedures (`app/proc/`)

| Procedure | Function |
|---|---|
| **REPROC.prc** | Reusable procedure for REPRO (IDCAMS copy) operations |
| **TRANREPT.prc** | Reusable procedure for transaction report generation |

---

## 7. Assembler Programs (`app/asm/`)

| Program | Function |
|---|---|
| **MVSWAIT.asm** | Low-level wait timer (called by COBSWAIT) |
| **COBDATFT.asm** | Date formatting utility (called by CBACT01C) |

---

## 8. Scheduler Configurations (`app/scheduler/`)

| File | Tool | Function |
|---|---|---|
| **CardDemo.ca7** | CA-7 | Batch job scheduling definitions |
| **CardDemo.controlm** | Control-M | Batch job scheduling definitions |

---

## 9. Other Assets

| Asset | Location | Function |
|---|---|---|
| Control file | `app/ctl/REPROCT.ctl` | Control card for REPRO operations |
| Catalog listing | `app/catlg/LISTCAT.txt` | VSAM catalog listing reference |
| CSD definition | `app/csd/CARDDEMO.CSD` | CICS resource definitions |
| Sample data (ASCII) | `app/data/ASCII/` | Test data in ASCII format |
| Sample data (EBCDIC) | `app/data/EBCDIC/` | Mainframe-ready EBCDIC data |

---

## 10. Classification Summary

### By Execution Environment

| Environment | Count | Programs |
|---|---|---|
| CICS Online | 17 | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C-03C |
| Batch | 12 | CBACT01C-04C, CBCUS01C, CBTRN01C-03C, CBSTM03A/B, CBEXPORT, CBIMPORT |
| Batch Utility | 2 | COBSWAIT, CSUTLDTC |
| CICS + IMS/DB2/MQ (optional) | 8 | COPAUA0C, COPAUS0C-2C, COTRTLIC, COTRTUPC, COACCT01, CODATE01 |
| Batch + IMS/DB2 (optional) | 2 | CBPAUP0C, COBTUPDT |

### By Business Domain

| Domain | Programs |
|---|---|
| **Authentication & Security** | COSGN00C, COUSR00C-03C |
| **Account Management** | COACTVWC, COACTUPC, CBACT01C, CBACT04C |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C |
| **Transaction Processing** | COTRN00C-02C, CBTRN01C-02C, COBIL00C |
| **Reporting & Statements** | CORPT00C, CBTRN03C, CBSTM03A/B |
| **Data Migration** | CBEXPORT, CBIMPORT |
| **Navigation** | COMEN01C, COADM01C |
| **Authorization (opt.)** | COPAUA0C, COPAUS0C-2C, CBPAUP0C |
| **Reference Data (opt.)** | COTRTLIC, COTRTUPC, COBTUPDT |
| **MQ Services (opt.)** | COACCT01, CODATE01 |
| **Utilities** | COBSWAIT, CSUTLDTC, CBCUS01C |

### Naming Convention

| Prefix | Meaning | Example |
|---|---|---|
| `CO*` | Online CICS program | COSGN00C |
| `CB*` | Batch program | CBTRN02C |
| `CV*` | Copybook (VSAM data structure) | CVACT01Y |
| `CS*` | Copybook (shared/system) | CSUSR01Y |
| `CO*.bms` | BMS screen map | COSGN00.bms |
