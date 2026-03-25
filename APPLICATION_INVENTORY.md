# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Platform:** IBM Mainframe (COBOL/CICS/VSAM/JCL)

---

## Summary

| Asset Type | Count | Location |
|---|---|---|
| COBOL Programs (Core) | 31 | `app/cbl/` |
| Copybooks (Data Structures) | 30 | `app/cpy/` |
| BMS Screen Maps | 17 | `app/bms/` |
| BMS-Generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Batch Jobs | 38 | `app/jcl/` |
| Optional Module Programs | 13 | `app/app-*/cbl/` |
| Optional Module Copybooks | 16 | `app/app-*/cpy/` |
| Optional Module BMS Maps | 4 | `app/app-*/bms/` |
| Optional Module JCL Jobs | 8 | `app/app-*/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| **Total Assets** | **176** | |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (CO* prefix)

These programs run under CICS and handle interactive 3270 terminal UI sessions.

| # | Program | Lines | Classification | Function | Transaction ID |
|---|---|---|---|---|---|
| 1 | COSGN00C.cbl | 260 | Security / Authentication | Sign-on screen -- validates user credentials against USRSEC VSAM file | CC00 |
| 2 | COMEN01C.cbl | 308 | Navigation | Main Menu for Regular users -- routes to functional screens | CM00 |
| 3 | COADM01C.cbl | 288 | Navigation / Admin | Admin Menu for Admin users -- routes to admin functions | CA00 |
| 4 | COACTVWC.cbl | 941 | Account Management | View account details (account, cards, customer info) | CAVW |
| 5 | COACTUPC.cbl | 4,236 | Account Management | Update account details (status, limits, credit data) | CAUP |
| 6 | COCRDLIC.cbl | 1,459 | Card Management | List credit cards -- all cards (admin) or by account (user) | CCLI |
| 7 | COCRDSLC.cbl | 887 | Card Management | View credit card details (card number, status, embossed name) | CCDL |
| 8 | COCRDUPC.cbl | 1,560 | Card Management | Update credit card details (status, expiry) | CCUP |
| 9 | COTRN00C.cbl | 699 | Transaction Processing | List transactions from TRANSACT file with browse/page | CT00 |
| 10 | COTRN01C.cbl | 330 | Transaction Processing | View a single transaction's details | CT01 |
| 11 | COTRN02C.cbl | 783 | Transaction Processing | Add a new transaction to the TRANSACT file | CT02 |
| 12 | CORPT00C.cbl | 649 | Reporting | Submit batch transaction report job via TDQ | CR00 |
| 13 | COBIL00C.cbl | 572 | Billing / Payments | Bill Payment -- pay account balance and record payment transaction | CB00 |
| 14 | COUSR00C.cbl | 695 | User Administration | List all users from USRSEC file (admin only) | CU00 |
| 15 | COUSR01C.cbl | 299 | User Administration | Add new Regular or Admin user | CU01 |
| 16 | COUSR02C.cbl | 414 | User Administration | Update existing user (name, password, type) | CU02 |
| 17 | COUSR03C.cbl | 359 | User Administration | Delete a user from USRSEC file | CU03 |

### 1.2 Batch Programs (CB* prefix)

These programs run as JCL batch jobs for overnight/scheduled processing.

| # | Program | Lines | Classification | Function |
|---|---|---|---|---|
| 18 | CBACT01C.cbl | 430 | Data Utility | Read account VSAM file and write to flat files (compressed, array, variable-block) |
| 19 | CBACT02C.cbl | 178 | Data Utility | Read and print card data file |
| 20 | CBACT03C.cbl | 178 | Data Utility | Read and print account cross-reference data file |
| 21 | CBACT04C.cbl | 652 | Financial Calculation | Interest calculation -- compute interest charges per account using discount groups |
| 22 | CBCUS01C.cbl | 178 | Data Utility | Read and print customer data file |
| 23 | CBTRN01C.cbl | 494 | Transaction Processing | Post records from daily transaction file (older version) |
| 24 | CBTRN02C.cbl | 731 | Transaction Processing | Post records from daily transaction file -- core posting engine |
| 25 | CBTRN03C.cbl | 649 | Reporting | Print the transaction detail report (daily transaction report) |
| 26 | CBSTM03A.CBL | 924 | Statement Generation | Print account statements from transaction data (plain text + HTML) |
| 27 | CBSTM03B.CBL | 230 | Statement Generation | Subroutine for CBSTM03A -- handles file I/O for statement report |
| 28 | CBEXPORT.cbl | 582 | Data Migration | Export customer data for branch migration (multi-record export file) |
| 29 | CBIMPORT.cbl | 487 | Data Migration | Import customer data from branch migration export file with validation |
| 30 | COBSWAIT.cbl | 41 | System Utility | Wait utility -- calls assembler MVSWAIT (parm in centiseconds) |

### 1.3 Shared Utility Programs (CS* prefix)

| # | Program | Lines | Classification | Function |
|---|---|---|---|---|
| 31 | CSUTLDTC.cbl | 157 | Date Utility | Date conversion utility -- calls LE CEEDAYS for date formatting |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Business Data Structures (CV* prefix)

| # | Copybook | Lines | Record Length | Entity |
|---|---|---|---|---|
| 1 | CVACT01Y.cpy | 103 | 300 bytes | Account Master Record |
| 2 | CVACT02Y.cpy | 10 | 150 bytes | Card Data Record |
| 3 | CVACT03Y.cpy | 26 | 50 bytes | Card Cross-Reference (card-to-account mapping) |
| 4 | CVCRD01Y.cpy | 14 | N/A | Card File Status & Key Fields |
| 5 | CVCUS01Y.cpy | 38 | 500 bytes | Customer Master Record |
| 6 | CVTRA01Y.cpy | 13 | 50 bytes | Transaction Category Balance |
| 7 | CVTRA02Y.cpy | 13 | 50 bytes | Disclosure Group (interest rates by category) |
| 8 | CVTRA03Y.cpy | 10 | 60 bytes | Transaction Type |
| 9 | CVTRA04Y.cpy | 12 | 60 bytes | Transaction Category Type |
| 10 | CVTRA05Y.cpy | 21 | 350 bytes | Transaction Record (online) |
| 11 | CVTRA06Y.cpy | 21 | 350 bytes | Daily Transaction Record (batch input) |
| 12 | CVTRA07Y.cpy | 73 | N/A | Transaction Report Layout (headers, totals) |
| 13 | CVEXPORT.cpy | 22 | N/A | Export Record Layout (multi-type) |

### 2.2 Common/System Copybooks (CO*/CS* prefix)

| # | Copybook | Lines | Purpose |
|---|---|---|---|
| 14 | COCOM01Y.cpy | 57 | Common communication area (COMMAREA) between programs |
| 15 | COADM02Y.cpy | 24 | Admin menu option definitions |
| 16 | COMEN02Y.cpy | 21 | Regular user menu option definitions |
| 17 | COTTL01Y.cpy | 11 | Title/header line layout |
| 18 | CODATECN.cpy | 15 | Date conversion record (for COBDATFT) |
| 19 | COSTM01.CPY | 38 | Transaction altered layout for statement reporting |
| 20 | CUSTREC.cpy | 38 | Customer record layout (statement generation variant) |
| 21 | CSDAT01Y.cpy | 11 | Date/time working storage fields |
| 22 | CSLKPCDY.cpy | 13 | Lookup code data structure |
| 23 | CSMSG01Y.cpy | 19 | Message area (thank-you, error messages) |
| 24 | CSMSG02Y.cpy | 18 | Extended message area |
| 25 | CSSETATY.cpy | 5 | Set attribute byte (used with COPY REPLACING) |
| 26 | CSSTRPFY.cpy | 5 | String strip/pad function fields |
| 27 | CSUSR01Y.cpy | 11 | User security record (ID, password, type) |
| 28 | CSUTLDPY.cpy | 5 | Date utility parameter area |
| 29 | CSUTLDWY.cpy | 11 | Date utility working storage |
| 30 | UNUSED1Y.cpy | 10 | Unused/placeholder data structure |

### 2.3 BMS-Generated Copybooks (`app/cpy-bms/`)

These are auto-generated from BMS map source and define symbolic field maps for COBOL programs.

| # | Copybook | Corresponding BMS Map | Screen |
|---|---|---|---|
| 1 | COACTUP.CPY | COACTUP.bms | Account Update |
| 2 | COACTVW.CPY | COACTVW.bms | Account View |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 5 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 6 | COCRDSL.CPY | COCRDSL.bms | Card Detail View |
| 7 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 8 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 9 | CORPT00.CPY | CORPT00.bms | Transaction Report |
| 10 | COSGN00.CPY | COSGN00.bms | Sign-On |
| 11 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 12 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 13 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | User Add |
| 16 | COUSR02.CPY | COUSR02.bms | User Update |
| 17 | COUSR03.CPY | COUSR03.bms | User Delete |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map Source | Map Name | Screen Name | Key Fields | Function Keys |
|---|---|---|---|---|---|
| 1 | COSGN00.bms | COSGN0A | Sign-On | User ID, Password | ENTER=Submit |
| 2 | COMEN01.bms | COMEN1A | Main Menu | Option selection | ENTER=Select, F3=Sign Off |
| 3 | COADM01.bms | COADM1A | Admin Menu | Option selection | ENTER=Select, F3=Sign Off |
| 4 | COACTVW.bms | CACTVWA | Account View | Account ID | ENTER=View, F3=Back |
| 5 | COACTUP.bms | CACTUPA | Account Update | Account ID, Status, Limits | ENTER=Fetch, F3/F5=Save, F12=Cancel |
| 6 | COCRDLI.bms | CCRDLIA | Card List | Card selection (S/V/U) | ENTER=Select, F3=Back, F7/F8=Page |
| 7 | COCRDSL.bms | CCRDSLA | Card Detail | Card Number | ENTER=View, F3=Back |
| 8 | COCRDUP.bms | CCRDUPA | Card Update | Card Number, Status | ENTER=Fetch, F3/F5=Save, F12=Cancel |
| 9 | COTRN00.bms | COTRN0A | Transaction List | Account/Card filter | ENTER=Select, F3=Back, F7/F8=Page |
| 10 | COTRN01.bms | COTRN1A | Transaction View | Transaction ID | ENTER=View, F3=Back |
| 11 | COTRN02.bms | COTRN2A | Transaction Add | All transaction fields | ENTER=Confirm, F3=Back, F4=Clear |
| 12 | CORPT00.bms | CORPT0A | Transaction Report | Date range, Report type | ENTER=Submit, F3=Back |
| 13 | COBIL00.bms | COBIL0A | Bill Payment | Account ID, Payment amount | ENTER=Confirm, F3=Back |
| 14 | COUSR00.bms | COUSR0A | User List | User selection (U/D) | ENTER=Select, F3=Back, F7/F8=Page |
| 15 | COUSR01.bms | COUSR1A | User Add | User ID, Name, Password, Type | ENTER=Submit, F3=Back, F4=Clear |
| 16 | COUSR02.bms | COUSR2A | User Update | User ID, Name, Password, Type | ENTER=Fetch, F3/F5=Save, F12=Cancel |
| 17 | COUSR03.bms | COUSR3A | User Delete | User ID | ENTER=Fetch, F3=Back, F5=Delete |

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data Loading / Refresh Jobs

| # | JCL Job | Classification | Function | Key Datasets |
|---|---|---|---|---|
| 1 | DUSRSECJ.jcl | Data Load | Load user security records into VSAM KSDS | USRSEC.PS -> USRSEC.VSAM.KSDS |
| 2 | ACCTFILE.jcl | Data Load | Refresh account master VSAM from flat file | ACCTDATA.PS -> ACCTDATA.VSAM.KSDS |
| 3 | CARDFILE.jcl | Data Load | Refresh card master VSAM from flat file | CARDDATA.PS -> CARDDATA.VSAM.KSDS |
| 4 | CUSTFILE.jcl | Data Load | Refresh customer master VSAM from flat file | CUSTDATA.PS -> CUSTDATA.VSAM.KSDS |
| 5 | XREFFILE.jcl | Data Load | Load card cross-reference with alternate index | CARDXREF.PS -> CARDXREF.VSAM.KSDS + AIX |
| 6 | TRANFILE.jcl | Data Load | Load transaction master VSAM with alternate index | DALYTRAN.PS.INIT -> TRANSACT.VSAM.KSDS |
| 7 | TCATBALF.jcl | Data Load | Load transaction category balance VSAM | TCATBALF.PS -> TCATBALF.VSAM.KSDS |
| 8 | TRANTYPE.jcl | Data Load | Load transaction type reference VSAM | TRANTYPE.PS -> TRANTYPE.VSAM.KSDS |
| 9 | TRANCATG.jcl | Data Load | Load transaction category reference VSAM | TRANCATG.PS -> TRANCATG.VSAM.KSDS |
| 10 | DISCGRP.jcl | Data Load | Load disclosure group (interest rate) VSAM | DISCGRP.PS -> DISCGRP.VSAM.KSDS |
| 11 | DEFCUST.jcl | Data Load | Define and load customer VSAM cluster | CUSTDATA setup |
| 12 | ESDSRRDS.jcl | Data Load | Define ESDS and RRDS VSAM clusters (demo) | ESDS/RRDS variants |

### 4.2 Core Batch Processing Jobs

| # | JCL Job | Classification | Function | Executes Program |
|---|---|---|---|---|
| 13 | POSTTRAN.jcl | Transaction Processing | Post daily transactions to master file | CBTRN02C |
| 14 | INTCALC.jcl | Financial Calculation | Calculate interest charges on accounts | CBACT04C |
| 15 | COMBTRAN.jcl | Transaction Processing | Combine/merge daily transactions | SORT utility |
| 16 | CREASTMT.JCL | Statement Generation | Produce account statements (text + HTML) | CBSTM03A |
| 17 | TRANREPT.jcl | Reporting | Generate daily transaction detail report | CBTRN03C (via SORT) |
| 18 | TRANBKP.jcl | Backup | Backup transaction file via IDCAMS REPRO | IDCAMS |
| 19 | TRANIDX.jcl | Index Maintenance | Define/rebuild alternate indexes on TRANSACT | IDCAMS |
| 20 | WAITSTEP.jcl | System Utility | Insert wait step (calls COBSWAIT) | COBSWAIT |

### 4.3 Data Utility / Read Jobs

| # | JCL Job | Classification | Function | Executes Program |
|---|---|---|---|---|
| 21 | READACCT.jcl | Data Utility | Read account VSAM and write flat files | CBACT01C |
| 22 | READCARD.jcl | Data Utility | Read and print card data | CBACT02C |
| 23 | READCUST.jcl | Data Utility | Read and print customer data | CBCUS01C |
| 24 | READXREF.jcl | Data Utility | Read and print cross-reference data | CBACT03C |
| 25 | CBEXPORT.jcl | Data Migration | Export data for branch migration | CBEXPORT |
| 26 | CBIMPORT.jcl | Data Migration | Import data from branch migration | CBIMPORT |

### 4.4 CICS File Control Jobs

| # | JCL Job | Classification | Function |
|---|---|---|---|
| 27 | CLOSEFIL.jcl | File Control | Close CICS files for batch processing |
| 28 | OPENFIL.jcl | File Control | Reopen CICS files after batch processing |

### 4.5 Administrative / Infrastructure Jobs

| # | JCL Job | Classification | Function |
|---|---|---|---|
| 29 | DEFGDGB.jcl | GDG Setup | Define Generation Data Group bases |
| 30 | DEFGDGD.jcl | GDG Setup | Define GDG bases (daily variant) |
| 31 | DALYREJS.jcl | GDG Setup | Define daily rejects GDG |
| 32 | REPTFILE.jcl | File Setup | Define report output file |
| 33 | PRTCATBL.jcl | Reporting | Print/backup transaction category balance file |
| 34 | CBADMCDJ.jcl | Admin | Administrative card job |
| 35 | FTPJCL.JCL | File Transfer | FTP job to send/receive files to/from mainframe |
| 36 | INTRDRJ1.JCL | Job Chaining | Internal reader job 1 -- triggers INTRDRJ2 |
| 37 | INTRDRJ2.JCL | Job Chaining | Internal reader job 2 -- triggered by INTRDRJ1 |
| 38 | TXT2PDF1.JCL | Conversion | Convert text statement file to PDF |

### 4.6 Standard Batch Cycle Order

```
CLOSEFIL -> Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
         -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT
         -> TRANIDX -> OPENFIL
```

---

## 5. Optional Modules

### 5.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for payment authorization.

| Program | Lines | Type | Function |
|---|---|---|---|
| COPAUA0C.cbl | N/A | Online CICS | MQ trigger -- receive authorization request |
| COPAUS0C.cbl | N/A | Online CICS | Authorization summary display |
| COPAUS1C.cbl | N/A | Online CICS | Authorization detail display |
| COPAUS2C.cbl | N/A | Online CICS | Fraud marking -- write to DB2 |
| CBPAUP0C.cbl | N/A | Batch | Purge old authorization records |
| DBUNLDGS.CBL | N/A | Batch | IMS DB unload utility (general) |
| PAUDBLOD.CBL | N/A | Batch | IMS DB load for authorization |
| PAUDBUNL.CBL | N/A | Batch | IMS DB unload for authorization |

Additional assets: 2 BMS maps, 9 copybooks, 5 JCL jobs, 8 IMS DBD/PSB definitions, 1 DDL, 1 DCL.

### 5.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based CRUD for transaction type reference data.

| Program | Lines | Type | Function |
|---|---|---|---|
| COTRTLIC.cbl | N/A | Online CICS | List/delete transaction types (DB2 cursor) |
| COTRTUPC.cbl | N/A | Online CICS | Add/edit transaction types (DB2 INSERT/UPDATE) |
| COBTUPDT.cbl | N/A | Batch | Batch update of transaction types |

Additional assets: 2 BMS maps, 2 copybooks, 3 JCL jobs, 7 control files, 2 DDLs, 2 DCLs.

### 5.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response patterns for system queries.

| Program | Lines | Type | Function |
|---|---|---|---|
| CODATE01.cbl | N/A | Online CICS | MQ-based system date retrieval (channel CDRD) |
| COACCT01.cbl | N/A | Online CICS | MQ-based account inquiry (channel CDRA) |

Additional assets: 1 CSD resource definition.

---

## 6. Supporting Assets

### 6.1 Assembler Programs (`app/asm/`)

| Program | Function |
|---|---|
| MVSWAIT | Assembler wait routine (called by COBSWAIT) |
| COBDATFT | Date formatting assembler routine (called by CBACT01C) |

### 6.2 Scripts (`scripts/`)

| Script | Function |
|---|---|
| run_full_batch.sh | Submit full batch cycle via FTP |
| run_posting.sh | Submit posting cycle |
| run_interest_calc.sh | Submit interest calculation cycle |
| remote_compile.sh | Compile COBOL on mainframe via FTP |
| remote_refresh.sh | Refresh all data files on mainframe |
| remote_submit.sh | Submit a single JCL job |
| upld_module.sh | Upload source module to mainframe PDS |
| local_compile.sh | Local compile with GnuCOBOL |

### 6.3 Other Assets

| Directory | Contents |
|---|---|
| `app/data/ASCII/` | Sample data files in ASCII format |
| `app/data/EBCDIC/` | Sample data files in EBCDIC format |
| `app/csd/` | CICS resource definitions (CARDDEMO.CSD) |
| `app/proc/` | 2 JCL procedures |
| `app/ctl/` | Control files |
| `app/catlg/` | Catalog listings |
| `app/maclib/` | Assembler macros |
| `app/scheduler/` | Job scheduler configs (CA7, Control-M) |
