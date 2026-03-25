# APPLICATION INVENTORY -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25  
> **Scope:** All artifacts in `app/` including optional modules  
> **Total Artifacts:** 31 COBOL programs + 13 optional-module programs + 30 copybooks + 11 optional-module copybooks + 38 JCL jobs + 8 optional-module JCL + 17 BMS maps + 4 optional-module BMS maps + 2 assembler programs + 2 JCL procedures + 2 scheduler configs

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program | Lines | CICS Tran | Function | Business Domain | BMS Map | VSAM Files Accessed |
|---|---------|-------|-----------|----------|-----------------|---------|---------------------|
| 1 | **COSGN00C** | 261 | CC00 | User sign-on / authentication | Security | COSGN00 | USRSEC |
| 2 | **COMEN01C** | 309 | CM00 | Main menu for regular users | Navigation | COMEN01 | USRSEC |
| 3 | **COADM01C** | 288 | CA00 | Admin menu | Navigation (Admin) | COADM01 | -- |
| 4 | **COACTVWC** | 942 | CAVW | View account details | Account Mgmt | COACTVW | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT |
| 5 | **COACTUPC** | 4,237 | CAUP | Update account details | Account Mgmt | COACTUP | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT |
| 6 | **COCRDLIC** | 1,460 | CCLI | List credit cards | Card Mgmt | COCRDLI | CARDDAT, CARDAIX |
| 7 | **COCRDSLC** | 888 | CCDL | View credit card details | Card Mgmt | COCRDSL | CARDDAT, CARDAIX |
| 8 | **COCRDUPC** | 1,560 | CCUP | Update credit card | Card Mgmt | COCRDUP | CARDDAT, CARDAIX |
| 9 | **COTRN00C** | 699 | CT00 | List transactions | Transaction Mgmt | COTRN00 | TRANSACT |
| 10 | **COTRN01C** | 330 | CT01 | View transaction details | Transaction Mgmt | COTRN01 | TRANSACT |
| 11 | **COTRN02C** | 783 | CT02 | Add new transaction | Transaction Mgmt | COTRN02 | TRANSACT, ACCTDAT, CCXREF, CXACAIX |
| 12 | **CORPT00C** | 649 | CR00 | Submit transaction report (batch) | Reporting | CORPT00 | TRANSACT |
| 13 | **COBIL00C** | 572 | CB00 | Bill payment (pay balance) | Payments | COBIL00 | TRANSACT, ACCTDAT, CXACAIX |
| 14 | **COUSR00C** | 695 | CU00 | List users (admin) | User Admin | COUSR00 | USRSEC |
| 15 | **COUSR01C** | 299 | CU01 | Add user (admin) | User Admin | COUSR01 | USRSEC |
| 16 | **COUSR02C** | 414 | CU02 | Update user (admin) | User Admin | COUSR02 | USRSEC |
| 17 | **COUSR03C** | 359 | CU03 | Delete user (admin) | User Admin | COUSR03 | USRSEC |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program | Lines | Function | Business Domain | Files Read | Files Written |
|---|---------|-------|----------|-----------------|------------|---------------|
| 18 | **CBTRN02C** | 731 | Post daily transactions to master | Transaction Processing | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF | TRANSACT, DALYREJS |
| 19 | **CBACT04C** | 652 | Calculate interest on accounts | Interest Calc | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | TRANSACT |
| 20 | **CBTRN03C** | 649 | Print transaction detail report | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT |
| 21 | **CBSTM03A** | 924 | Generate account statements (driver) | Statement Gen | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE |
| 22 | **CBSTM03B** | 230 | Statement generation (data reader) | Statement Gen | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | -- |
| 23 | **CBTRN01C** | 494 | Transaction enrichment (pre-post) | Transaction Processing | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE |
| 24 | **CBEXPORT** | 582 | Export data for branch migration | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| 25 | **CBIMPORT** | 487 | Import data from branch migration | Data Migration | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT |
| 26 | **CBACT01C** | 430 | Read and dump account file | Utility | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE |
| 27 | **CBACT02C** | 178 | Read and print card file | Utility | CARDFILE | -- (DISPLAY) |
| 28 | **CBACT03C** | 178 | Read and print xref file | Utility | XREFFILE | -- (DISPLAY) |
| 29 | **CBCUS01C** | 178 | Read and print customer file | Utility | CUSTFILE | -- (DISPLAY) |

### 1.3 Shared Utility Programs

| # | Program | Lines | Function | Called By |
|---|---------|-------|----------|-----------|
| 30 | **CSUTLDTC** | 157 | Date validation utility (calls CEEDAYS) | COTRN02C, CORPT00C |
| 31 | **COBSWAIT** | 41 | Wait/sleep utility for JCL scheduling | WAITSTEP.jcl |

---

## 2. Optional Module Programs

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)
IMS DB + DB2 + MQ integration for card authorization.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 32 | **COPAUA0C** | 1,026 | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| 33 | **COPAUS0C** | 1,032 | CICS/IMS/BMS | Summary view of authorization messages |
| 34 | **COPAUS1C** | 604 | CICS/IMS/BMS | Detail view of authorization message |
| 35 | **COPAUS2C** | 244 | CICS/IMS/DB2 | Mark authorization as fraud |
| 36 | **CBPAUP0C** | 386 | Batch/IMS | Purge expired pending authorizations |
| 37 | **DBUNLDGS** | 366 | Batch/IMS | Unload IMS DB to GSAM |
| 38 | **PAUDBLOD** | 369 | Batch/IMS | Load data into IMS DB |
| 39 | **PAUDBUNL** | 317 | Batch/IMS | Unload IMS DB segments |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 40 | **COTRTLIC** | 2,098 | CICS/DB2 | List/delete transaction types |
| 41 | **COTRTUPC** | 1,702 | CICS/DB2 | Add/edit transaction types |
| 42 | **COBTUPDT** | 237 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 43 | **COACCT01** | 620 | Batch/MQ | Account inquiry via MQ request/response |
| 44 | **CODATE01** | 524 | Batch/MQ | System date inquiry via MQ request/response |

---

## 3. Copybooks (`app/cpy/`)

### 3.1 Business Data Record Layouts (prefix `CV*`)

| # | Copybook | Lines | Record | Key | Length | Description |
|---|----------|-------|--------|-----|--------|-------------|
| 1 | **CVACT01Y** | 21 | ACCOUNT-RECORD | ACCT-ID (9(11)) | 300 bytes | Account master record |
| 2 | **CVACT02Y** | 15 | CARD-RECORD | CARD-NUM (X(16)) | 150 bytes | Credit card record |
| 3 | **CVACT03Y** | 12 | CARD-XREF-RECORD | XREF-CARD-NUM (X(16)) | 50 bytes | Card-to-Account-Customer cross-reference |
| 4 | **CVCUS01Y** | 26 | CUSTOMER-RECORD | CUST-ID (9(09)) | 500 bytes | Customer master record |
| 5 | **CVTRA05Y** | 21 | TRAN-RECORD | TRAN-ID (X(16)) | 350 bytes | Online transaction record |
| 6 | **CVTRA06Y** | 21 | DALYTRAN-RECORD | DALYTRAN-ID (X(16)) | 350 bytes | Daily transaction feed |
| 7 | **CVTRA01Y** | 13 | TRANCAT-RECORD | TRANCAT-ACCT-ID | ~50 bytes | Transaction category balance |
| 8 | **CVTRA02Y** | 13 | DISCGRP-RECORD | DIS-ACCT-GROUP-ID | ~50 bytes | Discount/interest rate group |
| 9 | **CVTRA03Y** | 10 | TRANTYPE-RECORD | TRAN-TYPE (X(02)) | ~60 bytes | Transaction type lookup |
| 10 | **CVTRA04Y** | 12 | TRANCATG-RECORD | TRAN-TYPE-CD + TRAN-CAT-CD | ~60 bytes | Transaction category lookup |
| 11 | **CVTRA07Y** | 73 | Report headers/lines | -- | Variable | Transaction report formatting |
| 12 | **CVCRD01Y** | 46 | CC-WORK-AREA | -- | -- | Credit card screen work area |
| 13 | **CVEXPORT** | 103 | EXPORT-RECORD | EXPORT-SEQUENCE-NUM | ~500 bytes | Data export/import record |

### 3.2 Application Infrastructure Copybooks (prefix `CO*`, `CS*`, `CU*`)

| # | Copybook | Lines | Description |
|---|----------|-------|-------------|
| 14 | **COCOM01Y** | 48 | CARDDEMO-COMMAREA -- Communication area passed between all programs |
| 15 | **COMEN02Y** | 102 | Main menu option definitions (11 options) |
| 16 | **COADM02Y** | 63 | Admin menu option definitions (6 options) |
| 17 | **COTTL01Y** | 27 | Screen title/header constants |
| 18 | **CSDAT01Y** | 59 | Current date/time formatting structures |
| 19 | **CSMSG01Y** | 25 | Common messages (thank you, invalid key) |
| 20 | **CSMSG02Y** | 35 | Abend handling message structures |
| 21 | **CSUSR01Y** | 27 | SEC-USER-DATA -- User security record (80 bytes) |
| 22 | **CSUTLDPY** | 375 | Date utility parameters (for CSUTLDTC) |
| 23 | **CSUTLDWY** | 89 | Date edit working storage variables |
| 24 | **CSSETATY** | 30 | Screen attribute setting utility |
| 25 | **CSSTRPFY** | 85 | String strip/pad function utility |
| 26 | **CSLKPCDY** | 1,318 | Lookup code tables (states, countries, etc.) |
| 27 | **CODATECN** | 52 | Date conversion record for COBDATFT |
| 28 | **COSTM01** | 38 | Statement transaction record layout |
| 29 | **CUSTREC** | 26 | Alternate customer record layout |
| 30 | **UNUSED1Y** | 10 | Unused/placeholder record |

### 3.3 Optional Module Copybooks

#### Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 31 | **CCPAUERY** | Authorization error reply |
| 32 | **CCPAURLY** | Authorization reply message |
| 33 | **CCPAURQY** | Authorization request message |
| 34 | **CIPAUSMY** | IMS authorization summary segment |
| 35 | **CIPAUDTY** | IMS authorization detail segment |
| 36 | **IMSFUNCS** | IMS DL/I function codes |
| 37 | **PADFLPCB** | IMS PCB for detail file |
| 38 | **PASFLPCB** | IMS PCB for summary file |
| 39 | **PAUTBPCB** | IMS PCB for auth table |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 40 | **CSDB2RPY** | DB2 reply structure |
| 41 | **CSDB2RWY** | DB2 read/write working storage |

---

## 4. BMS Maps (`app/bms/` and `app/cpy-bms/`)

Each BMS source generates a corresponding copybook in `cpy-bms/` with field definitions.

| # | Map Source | Mapset | Map Name | Associated Program | Screen Purpose |
|---|-----------|--------|----------|-------------------|----------------|
| 1 | **COSGN00.bms** | COSGN00 | COSGN0A | COSGN00C | Sign-on screen (user ID + password) |
| 2 | **COMEN01.bms** | COMEN01 | COMEN1A | COMEN01C | Main menu (regular user) |
| 3 | **COADM01.bms** | COADM01 | COADM1A | COADM01C | Admin menu |
| 4 | **COACTVW.bms** | COACTVW | CACTVWA | COACTVWC | Account view detail |
| 5 | **COACTUP.bms** | COACTUP | CACTUPA | COACTUPC | Account update form |
| 6 | **COCRDLI.bms** | COCRDLI | CCRDLIA | COCRDLIC | Credit card list |
| 7 | **COCRDSL.bms** | COCRDSL | CCRDSLA | COCRDSLC | Credit card detail view |
| 8 | **COCRDUP.bms** | COCRDUP | CCRDUPA | COCRDUPC | Credit card update form |
| 9 | **COTRN00.bms** | COTRN00 | COTRN0A | COTRN00C | Transaction list |
| 10 | **COTRN01.bms** | COTRN01 | COTRN1A | COTRN01C | Transaction detail view |
| 11 | **COTRN02.bms** | COTRN02 | COTRN2A | COTRN02C | Transaction add form |
| 12 | **CORPT00.bms** | CORPT00 | CORPT0A | CORPT00C | Transaction report request |
| 13 | **COBIL00.bms** | COBIL00 | COBIL0A | COBIL00C | Bill payment screen |
| 14 | **COUSR00.bms** | COUSR00 | COUSR0A | COUSR00C | User list (admin) |
| 15 | **COUSR01.bms** | COUSR01 | COUSR1A | COUSR01C | User add (admin) |
| 16 | **COUSR02.bms** | COUSR02 | COUSR2A | COUSR02C | User update (admin) |
| 17 | **COUSR03.bms** | COUSR03 | COUSR3A | COUSR03C | User delete (admin) |

#### Optional Module BMS Maps

| # | Map Source | Module | Screen Purpose |
|---|-----------|--------|----------------|
| 18 | **COPAU00.bms** | Auth IMS/DB2/MQ | Authorization summary list |
| 19 | **COPAU01.bms** | Auth IMS/DB2/MQ | Authorization detail view |
| 20 | **COTRTLI.bms** | Tran Type DB2 | Transaction type list |
| 21 | **COTRTUP.bms** | Tran Type DB2 | Transaction type update form |

---

## 5. JCL Jobs (`app/jcl/`)

### 5.1 Data Load / Refresh Jobs

| # | JCL | Steps | Purpose | Key Datasets |
|---|-----|-------|---------|--------------|
| 1 | **ACCTFILE** | IDCAMS x3 | Define + refresh account VSAM | ACCTDATA, ACCTVSAM |
| 2 | **CARDFILE** | IDCAMS x5, SDSF x2 | Define + refresh card VSAM + AIX | CARDDATA, CARDVSAM |
| 3 | **CUSTFILE** | IDCAMS x3, SDSF x2 | Define + refresh customer VSAM | CUSTDATA, CUSTVSAM |
| 4 | **TRANFILE** | IDCAMS x6, SDSF x2 | Define + refresh transaction VSAM + AIX | TRANSACT, TRANVSAM |
| 5 | **XREFFILE** | IDCAMS x6 | Define + refresh card xref VSAM + AIX | XREFDATA, XREFVSAM |
| 6 | **DUSRSECJ** | IEBGENER, IDCAMS x2 | Load user security VSAM from inline data | USRSEC.PS, USRSEC.VSAM.KSDS |
| 7 | **TRANTYPE** | IDCAMS x3 | Load transaction type VSAM | TRANTYPE |
| 8 | **TRANCATG** | IDCAMS x3 | Load transaction category VSAM | TRANCATG |
| 9 | **TCATBALF** | IDCAMS x3 | Load category balance VSAM | TCATBAL |
| 10 | **DISCGRP** | IDCAMS x3 | Load discount group VSAM | DISCGRP |

### 5.2 Batch Processing Jobs

| # | JCL | Program Executed | Purpose |
|---|-----|-----------------|---------|
| 11 | **POSTTRAN** | CBTRN02C | Post daily transactions to master file |
| 12 | **INTCALC** | CBACT04C | Calculate interest on accounts |
| 13 | **COMBTRAN** | SORT + IDCAMS | Sort and combine transaction files |
| 14 | **CREASTMT** | SORT + IDCAMS + CBSTM03A | Generate account statements |
| 15 | **TRANREPT** | REPROC + SORT + CBTRN03C | Print transaction detail report |
| 16 | **TRANBKP** | REPROC + IDCAMS | Backup transaction VSAM to sequential |
| 17 | **TRANIDX** | IDCAMS x3 | Rebuild transaction alternate index |

### 5.3 CICS File Control Jobs

| # | JCL | Purpose |
|---|-----|---------|
| 18 | **CLOSEFIL** | Close CICS-managed VSAM files for batch |
| 19 | **OPENFIL** | Reopen CICS-managed VSAM files after batch |

### 5.4 Utility / Data Inspection Jobs

| # | JCL | Program | Purpose |
|---|-----|---------|---------|
| 20 | **READACCT** | CBACT01C | Dump account file to PS/ARRY/VB formats |
| 21 | **READCARD** | CBACT02C | Print card file contents |
| 22 | **READCUST** | CBCUS01C | Print customer file contents |
| 23 | **READXREF** | CBACT03C | Print xref file contents |
| 24 | **CBEXPORT** | CBEXPORT | Export all data to migration file |
| 25 | **CBIMPORT** | CBIMPORT | Import migration file into split outputs |
| 26 | **PRTCATBL** | REPROC + SORT | Print category balance report |

### 5.5 Infrastructure / Setup Jobs

| # | JCL | Purpose |
|---|-----|---------|
| 27 | **DEFGDGB** | Define GDG base entries |
| 28 | **DEFGDGD** | Define GDG data + load transaction types/categories/discount groups |
| 29 | **DEFCUST** | Define customer VSAM clusters |
| 30 | **DALYREJS** | Define daily rejects file |
| 31 | **REPTFILE** | Define report output file |
| 32 | **ESDSRRDS** | Load ESDS and RRDS demo files |
| 33 | **CBADMCDJ** | Update CICS CSD resource definitions |
| 34 | **WAITSTEP** | Execute COBSWAIT for job scheduling delays |
| 35 | **FTPJCL** | FTP file transfer |
| 36 | **INTRDRJ1** | Internal reader test job 1 |
| 37 | **INTRDRJ2** | Internal reader test job 2 |
| 38 | **TXT2PDF1** | Convert text statement to PDF |

### 5.6 Optional Module JCL

#### Authorization Module

| # | JCL | Purpose |
|---|-----|---------|
| 39 | **CBPAUP0J** | Purge expired authorizations |
| 40 | **DBPAUTP0** | Define IMS DB for authorizations |
| 41 | **LOADPADB** | Load authorization IMS database |
| 42 | **UNLDGSAM** | Unload IMS DB to GSAM |
| 43 | **UNLDPADB** | Unload IMS DB segments |

#### Transaction Type DB2 Module

| # | JCL | Purpose |
|---|-----|---------|
| 44 | **CREADB21** | Create DB2 tables for transaction types |
| 45 | **MNTTRDB2** | Maintain transaction type DB2 data |
| 46 | **TRANEXTR** | Extract transaction type data from DB2 |

---

## 6. Other Artifacts

### 6.1 Assembler Programs (`app/asm/`)

| Program | Purpose |
|---------|---------|
| **COBDATFT** | Date format conversion routine (called by CBACT01C) |
| **MVSWAIT** | MVS STIMER wait macro (used by COBSWAIT) |

### 6.2 JCL Procedures (`app/proc/`)

| Procedure | Purpose |
|-----------|---------|
| **REPROC** | Reusable VSAM-to-sequential repro procedure |
| **TRANREPT** | Transaction report procedure |

### 6.3 Scheduler Configurations (`app/scheduler/`)

| Config | Purpose |
|--------|---------|
| **CardDemo.ca7** | CA-7 scheduler job definitions |
| **CardDemo.controlm** | Control-M scheduler job definitions |

### 6.4 Control Files (`app/ctl/`)

| File | Purpose |
|------|---------|
| **REPROCT.ctl** | REPRO control statements |

### 6.5 CSD Definitions (`app/csd/`)

| File | Purpose |
|------|---------|
| **CARDDEMO.CSD** | CICS System Definition file (transactions, programs, files, maps) |

---

## 7. Classification Summary

| Category | Count | Technology |
|----------|-------|------------|
| Online CICS programs | 17 | COBOL + CICS + BMS |
| Batch programs | 14 | COBOL + sequential/VSAM I/O |
| Optional online programs | 7 | COBOL + CICS + IMS/DB2/MQ |
| Optional batch programs | 6 | COBOL + IMS/DB2/MQ |
| Utility programs | 3 | COBOL + LE callable services |
| Business data copybooks | 13 | COBOL record layouts |
| Infrastructure copybooks | 17 | COBOL working storage |
| Optional module copybooks | 11 | COBOL + IMS/DB2 |
| BMS screen maps (core) | 17 | BMS macro language |
| BMS screen maps (optional) | 4 | BMS macro language |
| JCL batch jobs (core) | 38 | JCL + IDCAMS/SORT/IEBGENER |
| JCL batch jobs (optional) | 8 | JCL + IMS/DB2 utilities |
| Assembler programs | 2 | z/OS Assembler |
| JCL procedures | 2 | JCL PROC |
| **Total artifacts** | **159** | |
