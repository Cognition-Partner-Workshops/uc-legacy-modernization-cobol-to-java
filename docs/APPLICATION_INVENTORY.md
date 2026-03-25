# CardDemo Application Inventory

> **Generated from:** static analysis of the CardDemo COBOL/CICS/VSAM codebase
>
> **Application:** CardDemo -- Mainframe Credit Card Management System
>
> **Runtime Stack:** COBOL / CICS / VSAM / JCL / BMS / DB2 / IMS / MQ

---

## Summary

| Artifact Type          | Count |
|------------------------|-------|
| COBOL Programs (Core)  | 31    |
| COBOL Programs (Optional Modules) | 13 |
| Copybooks (Core)       | 30    |
| Copybooks (Optional Modules) | 11 |
| BMS Screen Maps (Core) | 17    |
| BMS Screen Maps (Optional Modules) | 4 |
| BMS-Generated Copybooks| 17    |
| JCL Jobs (Core)        | 38    |
| JCL Jobs (Optional Modules) | 8 |
| Assembler Programs     | 2     |
| JCL Procedures         | 2     |
| **Total Artifacts**    | **173** |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs

These programs run under CICS and drive the 3270-terminal user interface.

| Program   | Lines | Function | CICS Trans | BMS Map | Business Domain |
|-----------|------:|----------|------------|---------|-----------------|
| COSGN00C  | 260   | User sign-on / authentication | CC00 | COSGN00 | Security |
| COMEN01C  | 308   | Main menu navigation | CM00 | COMEN01 | Navigation |
| COACTVWC  | 941   | View account details | CA00 | COACTVW | Account Management |
| COACTUPC  | 4,236 | Update account information | CA01 | COACTUP | Account Management |
| COCRDLIC  | 1,459 | List credit cards for an account | CC01 | COCRDLI | Card Management |
| COCRDSLC  | 887   | View credit card details | CC02 | COCRDSL | Card Management |
| COCRDUPC  | 1,560 | Update credit card information | CC03 | COCRDUP | Card Management |
| COTRN00C  | 699   | List transactions | CT00 | COTRN00 | Transaction Management |
| COTRN01C  | 330   | View transaction details | CT01 | COTRN01 | Transaction Management |
| COTRN02C  | 783   | Add a new transaction | CT02 | COTRN02 | Transaction Management |
| CORPT00C  | 649   | Generate transaction reports | CR00 | CORPT00 | Reporting |
| COBIL00C  | 572   | Process bill payments | CB00 | COBIL00 | Bill Payment |
| COADM01C  | 288   | Admin menu navigation | CA90 | COADM01 | Administration |
| COUSR00C  | 695   | List users (admin) | CU00 | COUSR00 | User Administration |
| COUSR01C  | 299   | Add a new user (admin) | CU01 | COUSR01 | User Administration |
| COUSR02C  | 414   | Update user details (admin) | CU02 | COUSR02 | User Administration |
| COUSR03C  | 359   | Delete a user (admin) | CU03 | COUSR03 | User Administration |

### 1.2 Batch Programs

These programs run as scheduled batch jobs via JCL.

| Program   | Lines | Function | Business Domain |
|-----------|------:|----------|-----------------|
| CBACT01C  | 430   | Read and validate account master file | Account Management |
| CBACT02C  | 178   | Read and validate card master file | Card Management |
| CBACT03C  | 178   | Read and validate card cross-reference file | Card Management |
| CBACT04C  | 652   | Calculate interest on account balances | Financial Processing |
| CBCUS01C  | 178   | Read and validate customer master file | Customer Management |
| CBTRN01C  | 494   | Read and validate transaction file | Transaction Management |
| CBTRN02C  | 731   | Post daily transactions to master files | Transaction Management |
| CBTRN03C  | 649   | Generate daily transaction report | Reporting |
| CBSTM03A  | 924   | Generate account statements (text + HTML) | Reporting |
| CBSTM03B  | 230   | Statement file-processing subroutine (called by CBSTM03A) | Reporting |
| CBEXPORT  | 582   | Export VSAM data to flat files | Data Migration |
| CBIMPORT  | 487   | Import flat files into VSAM datasets | Data Migration |

### 1.3 Utility Programs

| Program   | Lines | Function | Business Domain |
|-----------|------:|----------|-----------------|
| CSUTLDTC  | 157   | Date validation utility (calls CEEDAYS) | Shared Utility |
| COBSWAIT  | 41    | Wait/delay utility (calls MVSWAIT assembler) | Shared Utility |

---

## 2. COBOL Programs -- Optional Modules

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ-based card authorization processing.

| Program   | Lines | Type | Function |
|-----------|------:|------|----------|
| COPAUA0C  | 1,026 | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| COPAUS0C  | 1,032 | CICS/IMS/BMS | Summary view of authorization messages |
| COPAUS1C  | 604   | CICS/IMS/BMS | Detail view of authorization message |
| COPAUS2C  | 244   | CICS/IMS/DB2 | Mark authorization message as fraud |
| CBPAUP0C  | 386   | Batch/IMS | Purge expired pending authorization messages |
| DBUNLDGS  | 366   | Batch | Unload GSAM database to flat file |
| PAUDBLOD  | 369   | Batch | Load authorization data into IMS database |
| PAUDBUNL  | 317   | Batch | Unload authorization data from IMS database |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-backed CRUD for transaction type reference data.

| Program   | Lines | Type | Function |
|-----------|------:|------|----------|
| COTRTUPC  | 1,702 | CICS/DB2 | Add/edit transaction types |
| COTRTLIC  | 2,098 | CICS/DB2 | List/delete transaction types |
| COBTUPDT  | 237   | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response services for external integration.

| Program   | Lines | Type | Function |
|-----------|------:|------|----------|
| CODATE01  | 524   | CICS/MQ | Return system date via MQ |
| COACCT01  | 620   | CICS/MQ | Account inquiry via MQ |

---

## 3. Copybooks -- Core (`app/cpy/`)

### 3.1 Business Data Record Layouts

| Copybook  | Lines | Record Length | Business Entity |
|-----------|------:|:------------:|-----------------|
| CVACT01Y  | 20    | 300 bytes | Account master record |
| CVACT02Y  | 14    | 150 bytes | Card master record |
| CVACT03Y  | 11    | 50 bytes  | Card cross-reference record |
| CVCUS01Y  | 26    | 500 bytes | Customer master record |
| CVCRD01Y  | 46    | N/A       | Card display record (screen layout) |
| CVTRA01Y  | 13    | 50 bytes  | Transaction category balance record |
| CVTRA02Y  | 13    | 50 bytes  | Disclosure group record |
| CVTRA03Y  | 10    | 60 bytes  | Transaction type record |
| CVTRA04Y  | 12    | 60 bytes  | Transaction category type record |
| CVTRA05Y  | 21    | 350 bytes | Transaction record (online) |
| CVTRA06Y  | 21    | 350 bytes | Daily transaction record |
| CVTRA07Y  | 73    | N/A       | Transaction report layout |
| CUSTREC   | 26    | N/A       | Customer record (statement format) |
| COSTM01   | 38    | N/A       | Transaction record (statement report format) |
| CVEXPORT  | 103   | N/A       | Export/import record layouts |
| UNUSED1Y  | 10    | 80 bytes  | Unused placeholder record |

### 3.2 Security & User Records

| Copybook  | Lines | Business Entity |
|-----------|------:|-----------------|
| CSUSR01Y  | 26    | User security record (ID, name, password, type) |

### 3.3 Common / Shared Copybooks

| Copybook  | Lines | Purpose |
|-----------|------:|---------|
| COCOM01Y  | 47    | Common communication area (COMMAREA) |
| COTTL01Y  | 27    | Screen title/header layout |
| CSDAT01Y  | 58    | Date handling working storage |
| CSMSG01Y  | 24    | Message area (short messages) |
| CSMSG02Y  | 35    | Message area (long messages) |
| CSMEN02Y  | 101   | Menu option definitions |
| COADM02Y  | 62    | Admin menu option definitions |
| CODATECN  | 52    | Date conversion working storage |

### 3.4 Utility / Procedural Copybooks

| Copybook  | Lines | Purpose |
|-----------|------:|---------|
| CSLKPCDY  | 1,318 | Lookup code table (transaction types, categories) |
| CSSETATY  | 30    | Set field attribute utility |
| CSSTRPFY  | 85    | String-processing / field-strip utility |
| CSUTLDPY  | 375   | Date utility procedure division code |
| CSUTLDWY  | 89    | Date utility working storage |

### 3.5 BMS-Generated Copybooks (`app/cpy-bms/`)

One generated copybook per BMS map, defining screen I/O fields:

| Copybook | Corresponding BMS Map | Screen |
|----------|-----------------------|--------|
| COSGN00.CPY | COSGN00.bms | Sign-On |
| COMEN01.CPY | COMEN01.bms | Main Menu |
| COACTVW.CPY | COACTVW.bms | Account View |
| COACTUP.CPY | COACTUP.bms | Account Update |
| COCRDLI.CPY | COCRDLI.bms | Card List |
| COCRDSL.CPY | COCRDSL.bms | Card Detail View |
| COCRDUP.CPY | COCRDUP.bms | Card Update |
| COTRN00.CPY | COTRN00.bms | Transaction List |
| COTRN01.CPY | COTRN01.bms | Transaction View |
| COTRN02.CPY | COTRN02.bms | Transaction Add |
| CORPT00.CPY | CORPT00.bms | Report Request |
| COBIL00.CPY | COBIL00.bms | Bill Payment |
| COADM01.CPY | COADM01.bms | Admin Menu |
| COUSR00.CPY | COUSR00.bms | User List |
| COUSR01.CPY | COUSR01.bms | User Add |
| COUSR02.CPY | COUSR02.bms | User Update |
| COUSR03.CPY | COUSR03.bms | User Delete |

---

## 4. Copybooks -- Optional Modules

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| Copybook   | Purpose |
|------------|---------|
| CCPAUERY.cpy | Authorization error record layout |
| CCPAURLY.cpy | Authorization reply record layout |
| CCPAURQY.cpy | Authorization request record layout |
| CIPAUSMY.cpy | Authorization summary record layout |
| CIPAUDTY.cpy | Authorization detail record layout |
| IMSFUNCS.cpy | IMS DL/I function codes and parameter counts |
| PADFLPCB.CPY | IMS PCB for PADFL database |
| PASFLPCB.CPY | IMS PCB for PASFL database |
| PAUTBPCB.CPY | IMS PCB for PAUT database |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| Copybook   | Purpose |
|------------|---------|
| CSDB2RPY.cpy | DB2 common procedures (DSNTIAC message formatting) |
| CSDB2RWY.cpy | DB2 common working storage (SQLCODE, DSNTIAC areas) |

---

## 5. BMS Screen Maps

### 5.1 Core Maps (`app/bms/`)

| Map File  | Mapset  | Map Name | Fields | Screen Purpose |
|-----------|---------|----------|-------:|----------------|
| COSGN00.bms | COSGN00 | COSGN0A | 37 | Sign-On Screen |
| COMEN01.bms | COMEN01 | COMEN1A | 28 | Main Menu |
| COACTVW.bms | COACTVW | CACTVWA | 100 | Account View |
| COACTUP.bms | COACTUP | CACTUPA | 128 | Account Update |
| COCRDLI.bms | COCRDLI | CCRDLIA | 72 | Card List |
| COCRDSL.bms | COCRDSL | CCRDSLA | 31 | Card Detail View |
| COCRDUP.bms | COCRDUP | CCRDUPA | 34 | Card Update |
| COTRN00.bms | COTRN00 | COTRN0A | 89 | Transaction List |
| COTRN01.bms | COTRN01 | COTRN1A | 56 | Transaction View |
| COTRN02.bms | COTRN02 | COTRN2A | 61 | Transaction Add |
| CORPT00.bms | CORPT00 | CORPT0A | 42 | Report Request |
| COBIL00.bms | COBIL00 | COBIL0A | 24 | Bill Payment |
| COADM01.bms | COADM01 | COADM1A | 28 | Admin Menu |
| COUSR00.bms | COUSR00 | COUSR0A | 89 | User List (Admin) |
| COUSR01.bms | COUSR01 | COUSR1A | 28 | User Add (Admin) |
| COUSR02.bms | COUSR02 | COUSR2A | 29 | User Update (Admin) |
| COUSR03.bms | COUSR03 | COUSR3A | 26 | User Delete (Admin) |

### 5.2 Optional Module Maps

| Map File  | Module | Screen Purpose |
|-----------|--------|----------------|
| COPAU00.bms | Authorization | Authorization Summary Screen |
| COPAU01.bms | Authorization | Authorization Detail Screen |
| COTRTLI.bms | Transaction Type DB2 | Transaction Type List |
| COTRTUP.bms | Transaction Type DB2 | Transaction Type Update |

---

## 6. JCL Batch Jobs (`app/jcl/`)

### 6.1 Data Loading / Refresh Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| DUSRSECJ  | IDCAMS, IEBGENER, IEFBR14 | Load user security VSAM file from flat file |
| ACCTFILE  | IDCAMS | Define and load account master VSAM KSDS |
| CARDFILE  | IDCAMS, SDSF | Define and load card master VSAM KSDS |
| CUSTFILE  | IDCAMS, SDSF | Define and load customer master VSAM KSDS |
| XREFFILE  | IDCAMS | Define card cross-reference VSAM with alternate index |
| TRANFILE  | IDCAMS, SDSF | Define and load transaction master VSAM KSDS |
| DEFCUST   | IDCAMS | Define customer VSAM cluster |
| DISCGRP   | IDCAMS | Define and load disclosure group VSAM |
| TRANTYPE  | IDCAMS | Define and load transaction type VSAM |
| TRANCATG  | IDCAMS | Define and load transaction category VSAM |
| TCATBALF  | IDCAMS | Define and load transaction category balance VSAM |
| REPTFILE  | IDCAMS | Define daily transaction report VSAM |

### 6.2 Core Batch Processing Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| POSTTRAN  | CBTRN02C | Post daily transactions to account balances |
| INTCALC   | CBACT04C | Calculate interest charges on accounts |
| COMBTRAN  | IDCAMS, SORT | Combine daily transactions into master file |
| CREASTMT  | CBSTM03A, IDCAMS, IEFBR14, SORT | Generate account statements (text + HTML) |
| TRANREPT  | CBTRN03C, SORT | Generate daily transaction report |
| TRANBKP   | IDCAMS | Backup transaction files |
| TRANIDX   | IDCAMS | Rebuild transaction alternate indexes |

### 6.3 CICS File Control Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| CLOSEFIL  | SDSF | Close CICS-managed VSAM files for batch |
| OPENFIL   | SDSF | Reopen CICS-managed VSAM files after batch |

### 6.4 Data Validation / Read Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| READACCT  | CBACT01C (implied) | Read/validate account file |
| READCARD  | CBACT02C | Read/validate card file |
| READCUST  | CBCUS01C | Read/validate customer file |
| READXREF  | CBACT03C | Read/validate cross-reference file |

### 6.5 Export / Import Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| CBEXPORT  | CBEXPORT, IDCAMS | Export all VSAM data to flat files |
| CBIMPORT  | CBIMPORT | Import flat files into VSAM datasets |

### 6.6 GDG / Infrastructure Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| DEFGDGB   | IDCAMS | Define GDG base for backup datasets |
| DEFGDGD   | IDCAMS, IEBGENER | Define GDG base for daily datasets + seed |
| ESDSRRDS  | (unnamed) | Define ESDS and RRDS VSAM clusters |
| DALYREJS  | IDCAMS | Define daily rejects VSAM file |
| PRTCATBL  | IEFBR14, SORT | Print/sort catalog balance data |

### 6.7 Utility / Special-Purpose Jobs

| Job       | Program(s) Executed | Purpose |
|-----------|---------------------|---------|
| WAITSTEP  | COBSWAIT | Wait step for job scheduling |
| CBADMCDJ  | DFHCSDUP | Load CICS CSD resource definitions |
| FTPJCL    | FTP | FTP file transfer job |
| INTRDRJ1  | IDCAMS, IEBGENER | Internal reader -- trigger chained job |
| INTRDRJ2  | IDCAMS | Internal reader -- chained job target |
| TXT2PDF1  | IKJEFT1B | Convert text statement to PDF |

### 6.8 Optional Module JCL Jobs

**Authorization Module** (`app/app-authorization-ims-db2-mq/jcl/`):

| Job       | Purpose |
|-----------|---------|
| CBPAUP0J  | Run authorization purge batch |
| DBPAUTP0  | Process authorization database |
| LOADPADB  | Load authorization IMS database |
| UNLDGSAM  | Unload GSAM authorization data |
| UNLDPADB  | Unload authorization IMS database |

**Transaction Type DB2 Module** (`app/app-transaction-type-db2/jcl/`):

| Job       | Purpose |
|-----------|---------|
| CREADB21  | Create DB2 transaction type table |
| MNTTRDB2  | Maintain transaction type DB2 data |
| TRANEXTR  | Extract transaction type data from DB2 |

---

## 7. Assembler Programs (`app/asm/`)

| Program   | Function |
|-----------|----------|
| MVSWAIT   | Low-level MVS WAIT macro (called by COBSWAIT) |
| COBDATFT  | Date formatting assembler routine (called by CBACT01C) |

---

## 8. JCL Procedures (`app/proc/`)

| Procedure | Purpose |
|-----------|---------|
| REPROC.prc | Reprocessing procedure for batch reruns |
| TRANREPT.prc | Transaction report generation procedure |

---

## 9. Naming Conventions

| Prefix | Meaning |
|--------|---------|
| `CO*`  | Online CICS program |
| `CB*`  | Batch COBOL program |
| `CV*`  | VSAM data copybook |
| `CS*`  | Common/shared copybook |
| `CO*Y` | Online-related copybook |
| `*FILE`| Data loading JCL job |
| `READ*`| Data validation JCL job |
| `DEF*` | VSAM/GDG definition job |

---

## 10. Technology Stack Classification

| Technology | Usage |
|------------|-------|
| COBOL | All 44 programs |
| CICS | 22 online programs (screen I/O, transaction control) |
| VSAM KSDS | Primary data storage (accounts, cards, customers, transactions) |
| BMS | 21 screen maps for 3270 terminal UI |
| JCL | 46 batch jobs for data loading, processing, and reporting |
| DB2 | 4 programs in optional modules (fraud marking, transaction types) |
| IMS DL/I | 5 programs in authorization module |
| MQ Series | 3 programs (authorization trigger, date service, account inquiry) |
| SORT | Used in COMBTRAN, CREASTMT, TRANREPT, PRTCATBL for data sorting |
| IDCAMS | Used extensively for VSAM cluster definition and data operations |
| Assembler | 2 utility routines (wait, date formatting) |
