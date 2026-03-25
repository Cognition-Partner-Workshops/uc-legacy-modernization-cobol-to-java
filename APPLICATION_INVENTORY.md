# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
>
> This document catalogs every artifact in the CardDemo COBOL/CICS/VSAM application,
> covering programs, copybooks, BMS screen maps, JCL batch jobs, assembler modules,
> JCL procedures, and optional extension modules.

---

## Summary Counts

| Artifact Type | Core | Auth (IMS/DB2/MQ) | Tran-Type (DB2) | VSAM-MQ | **Total** |
|---|---|---|---|---|---|
| COBOL Programs | 31 | 8 | 3 | 2 | **44** |
| Copybooks | 30 | 7 | 2 | 0 | **39** |
| BMS Maps | 17 | 2 | 2 | 0 | **21** |
| BMS-Generated Copybooks | 17 | 0 | 0 | 0 | **17** |
| JCL Jobs | 38 | 5 | 3 | 0 | **46** |
| Assembler Programs | 2 | 0 | 0 | 0 | **2** |
| JCL Procedures | 2 | 0 | 0 | 0 | **2** |

---

## 1. Core COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program | Lines | CICS Tran | BMS Map | Description | Classification |
|---|---------|-------|-----------|---------|-------------|----------------|
| 1 | COSGN00C.cbl | 260 | CC00 | COSGN00 | User sign-on / authentication | Security |
| 2 | COMEN01C.cbl | 308 | CM00 | COMEN01 | Main menu — dispatches to sub-programs | Navigation |
| 3 | COADM01C.cbl | 288 | CA00 | COADM01 | Admin menu — dispatches to admin sub-programs | Navigation / Admin |
| 4 | COACTVWC.cbl | 941 | CA01 | COACTVW | Account view (read-only detail) | Account Mgmt |
| 5 | COACTUPC.cbl | 4236 | CA02 | COACTUP | Account update (edit & save) | Account Mgmt |
| 6 | COCRDLIC.cbl | 1459 | CC01 | COCRDLI | Card list — browse cards for an account | Card Mgmt |
| 7 | COCRDSLC.cbl | 887 | CC02 | COCRDSL | Card detail view (read-only) | Card Mgmt |
| 8 | COCRDUPC.cbl | 1560 | CC03 | COCRDUP | Card update (edit & save) | Card Mgmt |
| 9 | COTRN00C.cbl | 699 | CT00 | COTRN00 | Transaction list — browse transactions | Transaction Mgmt |
| 10 | COTRN01C.cbl | 330 | CT01 | COTRN01 | Transaction detail view | Transaction Mgmt |
| 11 | COTRN02C.cbl | 783 | CT02 | COTRN02 | Transaction add (new transaction entry) | Transaction Mgmt |
| 12 | COBIL00C.cbl | 572 | CB00 | COBIL00 | Bill payment processing | Billing / Payment |
| 13 | CORPT00C.cbl | 649 | CR00 | CORPT00 | Transaction report request (online) | Reporting |
| 14 | COUSR00C.cbl | 695 | CU00 | COUSR00 | User list — browse user records | User Admin |
| 15 | COUSR01C.cbl | 299 | CU01 | COUSR01 | User add (create new user) | User Admin |
| 16 | COUSR02C.cbl | 414 | CU02 | COUSR02 | User update (edit existing user) | User Admin |
| 17 | COUSR03C.cbl | 359 | CU03 | COUSR03 | User delete | User Admin |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program | Lines | Description | Classification |
|---|---------|-------|-------------|----------------|
| 18 | CBACT01C.cbl | 430 | Read & display account data with date formatting | Account Mgmt |
| 19 | CBACT02C.cbl | 178 | Read account file records sequentially | Account Mgmt |
| 20 | CBACT03C.cbl | 178 | Read card cross-reference file records | Card Mgmt |
| 21 | CBACT04C.cbl | 652 | Interest calculation on accounts | Financial / Interest |
| 22 | CBCUS01C.cbl | 178 | Read customer file records sequentially | Customer Mgmt |
| 23 | CBTRN01C.cbl | 494 | Post daily transactions — validate card via XREF, read account | Transaction Posting |
| 24 | CBTRN02C.cbl | 731 | Transaction posting — full posting with updates | Transaction Posting |
| 25 | CBTRN03C.cbl | 649 | Transaction report generation (batch) | Reporting |
| 26 | CBSTM03A.CBL | 924 | Statement generation — main driver, calls CBSTM03B | Statement / Reporting |
| 27 | CBSTM03B.CBL | 230 | Statement generation — file I/O subroutine | Statement / Reporting |
| 28 | CBEXPORT.cbl | 582 | Data export — reads all VSAM files, writes unified export | Data Migration |
| 29 | CBIMPORT.cbl | 487 | Data import — reads export file, distributes to target files | Data Migration |

### 1.3 Utility Programs

| # | Program | Lines | Description | Classification |
|---|---------|-------|-------------|----------------|
| 30 | CSUTLDTC.cbl | 157 | Date conversion utility (calls CEEDAYS LE service) | Utility |
| 31 | COBSWAIT.cbl | 41 | Wait/sleep utility (calls MVSWAIT assembler) | Utility |

---

## 2. Core Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (prefix `CV*`)

| # | Copybook | Lines | Record Length | Description |
|---|----------|-------|---------------|-------------|
| 1 | CVACT01Y.cpy | 20 | 300 bytes | Account master record |
| 2 | CVACT02Y.cpy | 14 | 150 bytes | Card data record |
| 3 | CVACT03Y.cpy | 11 | 50 bytes | Card-to-account cross-reference |
| 4 | CVCUS01Y.cpy | 26 | 500 bytes | Customer master record |
| 5 | CVCRD01Y.cpy | 46 | — | Card detail record (extended) |
| 6 | CVTRA01Y.cpy | 13 | 50 bytes | Transaction category balance |
| 7 | CVTRA02Y.cpy | 13 | 50 bytes | Disclosure group (interest rate) |
| 8 | CVTRA03Y.cpy | 10 | 60 bytes | Transaction type master |
| 9 | CVTRA04Y.cpy | 12 | 60 bytes | Transaction category type |
| 10 | CVTRA05Y.cpy | 21 | 350 bytes | Transaction record (online) |
| 11 | CVTRA06Y.cpy | 21 | 350 bytes | Daily transaction record |
| 12 | CVTRA07Y.cpy | 73 | — | Transaction report data structures |
| 13 | CVEXPORT.cpy | 103 | — | Export/import unified record layout |
| 14 | COSTM01.CPY | 38 | 350 bytes | Transaction altered layout for reporting |
| 15 | CUSTREC.cpy | 26 | — | Customer record (alternate layout) |
| 16 | UNUSED1Y.cpy | 10 | 80 bytes | Unused placeholder record |

### 2.2 Common / UI / Utility Copybooks (prefix `CO*`, `CS*`)

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 17 | COCOM01Y.cpy | 47 | Common communication area (COMMAREA) |
| 18 | COADM02Y.cpy | 62 | Admin menu option definitions |
| 19 | COMEN02Y.cpy | 101 | Main menu option definitions |
| 20 | COTTL01Y.cpy | 27 | Screen title / header layout |
| 21 | CODATECN.cpy | 52 | Date conversion work area |
| 22 | CSDAT01Y.cpy | 58 | Date formatting helper fields |
| 23 | CSLKPCDY.cpy | 1318 | Lookup codes — country/state codes table |
| 24 | CSMSG01Y.cpy | 24 | Message area (error/info messages) |
| 25 | CSMSG02Y.cpy | 35 | Extended message area |
| 26 | CSSETATY.cpy | 30 | Screen attribute setting helper |
| 27 | CSSTRPFY.cpy | 85 | Store PFKey value common routine |
| 28 | CSUSR01Y.cpy | 26 | User security record (login credentials) |
| 29 | CSUTLDPY.cpy | 375 | Date utility parameters & work fields |
| 30 | CSUTLDWY.cpy | 89 | Date utility working storage |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map | Map Set | Screen Name | Associated Program |
|---|-----|---------|-------------|-------------------|
| 1 | COSGN00.bms | COSGN00 | Sign On | COSGN00C |
| 2 | COMEN01.bms | COMEN01 | Main Menu | COMEN01C |
| 3 | COADM01.bms | COADM01 | Admin Menu | COADM01C |
| 4 | COACTVW.bms | COACTVW | Account View | COACTVWC |
| 5 | COACTUP.bms | COACTUP | Account Update | COACTUPC |
| 6 | COCRDLI.bms | COCRDLI | Card List | COCRDLIC |
| 7 | COCRDSL.bms | COCRDSL | Card Detail View | COCRDSLC |
| 8 | COCRDUP.bms | COCRDUP | Card Update | COCRDUPC |
| 9 | COTRN00.bms | COTRN00 | Transaction List | COTRN00C |
| 10 | COTRN01.bms | COTRN01 | Transaction View | COTRN01C |
| 11 | COTRN02.bms | COTRN02 | Transaction Add | COTRN02C |
| 12 | COBIL00.bms | COBIL00 | Bill Payment | COBIL00C |
| 13 | CORPT00.bms | CORPT00 | Report Request | CORPT00C |
| 14 | COUSR00.bms | COUSR00 | User List | COUSR00C |
| 15 | COUSR01.bms | COUSR01 | Add User | COUSR01C |
| 16 | COUSR02.bms | COUSR02 | Update User | COUSR02C |
| 17 | COUSR03.bms | COUSR03 | Delete User | COUSR03C |

### BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map has a corresponding generated COBOL copybook (e.g., `COSGN00.CPY`) that defines
the symbolic map fields used by the COBOL programs for SEND MAP / RECEIVE MAP operations.
17 copybooks total — one per BMS map.

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data File Load / Refresh Jobs

| # | JCL Job | Programs Executed | Description |
|---|---------|-------------------|-------------|
| 1 | ACCTFILE.jcl | IDCAMS | Define & load Account VSAM KSDS from flat file |
| 2 | CARDFILE.jcl | IDCAMS | Define & load Card VSAM KSDS from flat file |
| 3 | CUSTFILE.jcl | IDCAMS | Define & load Customer VSAM KSDS from flat file |
| 4 | XREFFILE.jcl | IDCAMS | Define & load Card Cross-Reference VSAM KSDS + AIX |
| 5 | TRANFILE.jcl | IDCAMS | Define & load Transaction VSAM KSDS from flat file |
| 6 | DUSRSECJ.jcl | IEBGENER, IDCAMS | Load User Security VSAM KSDS from flat file |
| 7 | TRANTYPE.jcl | IDCAMS | Define & load Transaction Type VSAM KSDS |
| 8 | TRANCATG.jcl | IDCAMS | Define & load Transaction Category VSAM KSDS |
| 9 | TCATBALF.jcl | IDCAMS | Define & load Transaction Category Balance VSAM KSDS |
| 10 | DISCGRP.jcl | IDCAMS | Define & load Disclosure Group VSAM KSDS |
| 11 | REPTFILE.jcl | IDCAMS | Define & load Report (daily trans) VSAM KSDS |
| 12 | DEFCUST.jcl | IDCAMS | Define Customer VSAM cluster only (no load) |

### 4.2 Batch Processing Jobs

| # | JCL Job | Programs Executed | Description |
|---|---------|-------------------|-------------|
| 13 | POSTTRAN.jcl | CBTRN02C | Core transaction posting (daily → master) |
| 14 | INTCALC.jcl | CBACT04C | Interest calculation batch run |
| 15 | TRANREPT.jcl | SORT, CBTRN03C | Transaction report — sort + report generation |
| 16 | CREASTMT.JCL | SORT, IDCAMS, CBSTM03A | Statement creation — sort, reorg, produce statements |
| 17 | CBEXPORT.jcl | CBEXPORT | Export all VSAM data to unified flat file |
| 18 | CBIMPORT.jcl | CBIMPORT | Import unified flat file into individual VSAM files |

### 4.3 File Maintenance / Utility Jobs

| # | JCL Job | Programs Executed | Description |
|---|---------|-------------------|-------------|
| 19 | CLOSEFIL.jcl | DFHCSDUP | Close CICS files before batch processing |
| 20 | OPENFIL.jcl | DFHCSDUP | Open CICS files after batch processing |
| 21 | TRANBKP.jcl | IDCAMS | Backup transaction VSAM to GDG |
| 22 | COMBTRAN.jcl | SORT, IDCAMS | Combine backed-up + system transactions into master |
| 23 | TRANIDX.jcl | IDCAMS | Define/rebuild transaction alternate index |
| 24 | DEFGDGB.jcl | IDCAMS | Define GDG base for transaction backups |
| 25 | DEFGDGD.jcl | IDCAMS | Define GDG base for daily transactions |
| 26 | PRTCATBL.jcl | IDCAMS, SORT | Print category balance file + backup to GDG |
| 27 | WAITSTEP.jcl | COBSWAIT | Execute wait/pause utility |
| 28 | ESDSRRDS.jcl | IDCAMS | Define ESDS & RRDS VSAM datasets (example) |

### 4.4 Read / Diagnostic Jobs

| # | JCL Job | Programs Executed | Description |
|---|---------|-------------------|-------------|
| 29 | READACCT.jcl | CBACT02C | Read & display account file records |
| 30 | READCARD.jcl | CBACT03C | Read & display card xref records |
| 31 | READCUST.jcl | CBCUS01C | Read & display customer records |
| 32 | READXREF.jcl | CBACT01C | Read xref with date formatting |
| 33 | DALYREJS.jcl | IDCAMS | Process daily rejection records |
| 34 | CBADMCDJ.jcl | — | Admin card demo JCL (utility) |

### 4.5 Infrastructure / Connectivity Jobs

| # | JCL Job | Programs Executed | Description |
|---|---------|-------------------|-------------|
| 35 | FTPJCL.JCL | FTP | FTP file transfer to/from mainframe |
| 36 | INTRDRJ1.JCL | IDCAMS, IEBGENER | Internal reader — triggers INTRDRJ2 |
| 37 | INTRDRJ2.JCL | IDCAMS | Internal reader — triggered by INTRDRJ1 |
| 38 | TXT2PDF1.JCL | IKJEFT1B (TXT2PDF) | Convert text statement file to PDF |

---

## 5. Assembler Programs (`app/asm/`)

| # | Program | Description |
|---|---------|-------------|
| 1 | MVSWAIT.asm | Mainframe wait/sleep — issues STIMER macro |
| 2 | COBDATFT.asm | Date formatting — converts dates for display |

---

## 6. JCL Procedures (`app/proc/`)

| # | Procedure | Description |
|---|-----------|-------------|
| 1 | REPROC.prc | Reprocessing procedure (re-run batch steps) |
| 2 | TRANREPT.prc | Transaction report generation procedure |

---

## 7. Optional Extension Modules

### 7.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/`)

**Purpose:** Pending authorization management via IMS DB, DB2, and MQ Series integration.

| Type | Artifact | Lines | Description |
|------|----------|-------|-------------|
| COBOL | COPAUA0C.cbl | 1026 | MQ trigger — receives auth requests from MQ queue |
| COBOL | COPAUS0C.cbl | 1032 | Authorization summary display (online CICS) |
| COBOL | COPAUS1C.cbl | 604 | Authorization detail display (online CICS) |
| COBOL | COPAUS2C.cbl | 244 | Fraud marking — updates DB2 fraud flag |
| COBOL | CBPAUP0C.cbl | 386 | Batch purge of expired authorizations |
| COBOL | DBUNLDGS.CBL | 366 | IMS DB unload to GSAM (General Sequential) |
| COBOL | PAUDBLOD.CBL | 369 | IMS DB load from sequential file |
| COBOL | PAUDBUNL.CBL | 317 | IMS DB unload to flat files |
| Copybook | CCPAUERY.cpy | — | Authorization error record |
| Copybook | CCPAURLY.cpy | — | Authorization reply record |
| Copybook | CCPAURQY.cpy | — | Authorization request record |
| Copybook | CIPAUDTY.cpy | — | Pending authorization detail (IMS segment) |
| Copybook | CIPAUSMY.cpy | — | Pending authorization summary (IMS segment) |
| Copybook | IMSFUNCS.cpy | — | IMS DL/I function codes |
| Copybook | PADFLPCB.CPY | — | IMS PCB mask (flat file) |
| Copybook | PASFLPCB.CPY | — | IMS PCB mask (sequential) |
| Copybook | PAUTBPCB.CPY | — | IMS PCB mask (auth DB) |
| BMS | COPAU00.bms | — | Authorization summary screen |
| BMS | COPAU01.bms | — | Authorization detail screen |
| JCL | CBPAUP0J.jcl | — | Purge expired authorizations |
| JCL | DBPAUTP0.jcl | — | IMS DB utility job |
| JCL | LOADPADB.JCL | — | Load pending auth IMS DB |
| JCL | UNLDGSAM.JCL | — | Unload IMS DB to GSAM |
| JCL | UNLDPADB.JCL | — | Unload pending auth DB to flat files |

### 7.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

**Purpose:** DB2-backed CRUD for transaction type master data, replacing VSAM-based storage.

| Type | Artifact | Lines | Description |
|------|----------|-------|-------------|
| COBOL | COTRTLIC.cbl | 2098 | Transaction type list (online CICS + DB2 cursors) |
| COBOL | COTRTUPC.cbl | 1702 | Transaction type add/edit (online CICS + embedded SQL) |
| COBOL | COBTUPDT.cbl | 237 | Batch update of transaction types (DB2) |
| Copybook | CSDB2RPY.cpy | — | DB2 reply/status fields |
| Copybook | CSDB2RWY.cpy | — | DB2 working storage fields |
| BMS | COTRTLI.bms | — | Transaction type list screen |
| BMS | COTRTUP.bms | — | Transaction type update screen |
| JCL | CREADB21.jcl | — | Create DB2 tables for transaction types |
| JCL | MNTTRDB2.jcl | — | Maintain transaction type DB2 data |
| JCL | TRANEXTR.jcl | — | Extract transaction type data from DB2 |

### 7.3 VSAM-MQ Module (`app/app-vsam-mq/`)

**Purpose:** MQ Series request/reply services for system date and account inquiry.

| Type | Artifact | Lines | Description |
|------|----------|-------|-------------|
| COBOL | CODATE01.cbl | 524 | MQ service — returns system date/time (queue CDRD) |
| COBOL | COACCT01.cbl | 620 | MQ service — account inquiry via VSAM (queue CDRA) |

---

## 8. Supporting Artifacts

| Directory | Contents |
|-----------|----------|
| `app/data/ASCII/` | Sample data files in ASCII format for local testing |
| `app/data/EBCDIC/` | Sample data files in EBCDIC format for mainframe upload |
| `app/csd/` | CICS resource definitions (CSD file for defining transactions, programs, files) |
| `app/ctl/` | Control files |
| `app/catlg/` | Catalog listings |
| `app/maclib/` | Assembler macro libraries |
| `app/scheduler/` | Job scheduler configurations (CA7, Control-M) |
| `scripts/` | Shell scripts for remote mainframe interaction (FTP-based compile, submit, refresh) |
| `samples/` | Sample JCL, procedures, and AWS M2 configurations |
| `diagrams/` | Architecture diagrams and screen captures |

---

## Naming Conventions

| Prefix | Meaning |
|--------|---------|
| `CO*` | Online CICS program |
| `CB*` | Batch COBOL program |
| `CS*` | Common/shared utility copybook |
| `CV*` | VSAM data record layout copybook |
| `CC*` | Communication / common area copybook |
| `CI*` | IMS segment layout copybook |
| `*Y` suffix | Copybook (data structure) |
