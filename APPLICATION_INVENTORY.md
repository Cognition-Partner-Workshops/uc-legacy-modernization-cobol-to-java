# Application Inventory -- CardDemo COBOL Codebase

> **Generated:** 2026-03-27 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This document catalogs every artifact in the CardDemo mainframe credit-card
> management application: COBOL programs, copybooks, BMS screen maps, JCL batch
> jobs, assembler modules, JCL procedures, and optional extension modules.

---

## Summary Statistics

| Artifact Type | Core | Optional Modules | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks (data) | 30 | 11 | **41** |
| BMS Screen Maps | 17 | 4 | **21** |
| BMS-Generated Copybooks | 17 | 4 | **21** |
| JCL Batch Jobs | 38 | 8 | **46** |
| Assembler Programs | 2 | 0 | **2** |
| JCL Procedures | 2 | 0 | **2** |
| Scheduler Configs | 2 | 0 | **2** |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

These programs run under CICS and handle interactive 3270 terminal sessions.

| # | Program | File | Lines | Transaction | Description | Classification |
|---|---------|------|-------|-------------|-------------|----------------|
| 1 | COSGN00C | COSGN00C.cbl | 260 | CC00 | **Sign-on / Authentication** -- Validates user credentials against USRSEC VSAM file; routes admins to COADM01C and regular users to COMEN01C. | Security / Auth |
| 2 | COMEN01C | COMEN01C.cbl | 308 | CM00 | **Main Menu** -- Displays the 11-option user menu (defined in COMEN02Y); transfers control (XCTL) to the selected program. | Navigation |
| 3 | COADM01C | COADM01C.cbl | 288 | CA00 | **Admin Menu** -- Displays the 6-option admin menu (defined in COADM02Y); transfers control to user-management or DB2 programs. | Navigation / Admin |
| 4 | COACTVWC | COACTVWC.cbl | 941 | CA01 | **Account View** -- Reads ACCTDAT, CARDXREF, CUSTDAT VSAM files to display account details, card numbers, and customer info. | Account Mgmt |
| 5 | COACTUPC | COACTUPC.cbl | 4,236 | CA02 | **Account Update** -- Full CRUD on account records; validates field edits, rewrites ACCTDAT VSAM. Largest online program. | Account Mgmt |
| 6 | COCRDLIC | COCRDLIC.cbl | 1,459 | CC01 | **Credit Card List** -- Browses CARDDAT VSAM with forward/backward paging; allows drill-down to card view or card update. | Card Mgmt |
| 7 | COCRDSLC | COCRDSLC.cbl | 887 | CC02 | **Credit Card View** -- Reads CARDDAT and CUSTDAT to display card details and cardholder info. | Card Mgmt |
| 8 | COCRDUPC | COCRDUPC.cbl | 1,560 | CC03 | **Credit Card Update** -- Edits card records; validates status, expiry, and cardholder fields; rewrites CARDDAT VSAM. | Card Mgmt |
| 9 | COTRN00C | COTRN00C.cbl | 699 | CT00 | **Transaction List** -- Browses TRANSACT VSAM with paging; displays transaction summaries. | Transaction Mgmt |
| 10 | COTRN01C | COTRN01C.cbl | 330 | CT01 | **Transaction View** -- Reads a single transaction from TRANSACT VSAM and displays full details. | Transaction Mgmt |
| 11 | COTRN02C | COTRN02C.cbl | 783 | CT02 | **Transaction Add** -- Adds new transactions to TRANSACT VSAM; validates account/card via CARDXREF and ACCTDAT; calls CSUTLDTC for date validation. | Transaction Mgmt |
| 12 | CORPT00C | CORPT00C.cbl | 649 | CR00 | **Transaction Reports** -- Accepts date range; writes report records to CICS Transient Data queue; calls CSUTLDTC for date validation. | Reporting |
| 13 | COBIL00C | COBIL00C.cbl | 572 | CB00 | **Bill Payment** -- Reads account balance, accepts payment amount, writes payment transaction to TRANSACT VSAM, updates account balance. | Payments |
| 14 | COUSR00C | COUSR00C.cbl | 695 | CU00 | **User List (Admin)** -- Browses USRSEC VSAM with paging; displays user security records. | User / Security |
| 15 | COUSR01C | COUSR01C.cbl | 299 | CU01 | **User Add (Admin)** -- Creates new user security records in USRSEC VSAM. | User / Security |
| 16 | COUSR02C | COUSR02C.cbl | 414 | CU02 | **User Update (Admin)** -- Reads and rewrites user security records in USRSEC VSAM. | User / Security |
| 17 | COUSR03C | COUSR03C.cbl | 359 | CU03 | **User Delete (Admin)** -- Reads and deletes user security records from USRSEC VSAM. | User / Security |

### 1.2 Batch Programs (prefix `CB*`)

These programs run as batch jobs submitted via JCL.

| # | Program | File | Lines | Description | Classification |
|---|---------|------|-------|-------------|----------------|
| 18 | CBACT01C | CBACT01C.cbl | 430 | **Account File Read** -- Sequentially reads ACCTDAT VSAM; calls COBDATFT (assembler) for date formatting. | Data Utility |
| 19 | CBACT02C | CBACT02C.cbl | 178 | **Card File Read** -- Sequentially reads CARDDAT VSAM and displays card records. | Data Utility |
| 20 | CBACT03C | CBACT03C.cbl | 178 | **Cross-Reference File Read** -- Sequentially reads CARDXREF VSAM and displays cross-reference records. | Data Utility |
| 21 | CBACT04C | CBACT04C.cbl | 652 | **Interest Calculation** -- Reads TRANSACT, CARDXREF, ACCTDAT, DISCGRP, and TCATBAL VSAM files; computes interest on transaction categories; updates account balances. | Financial / Core Batch |
| 22 | CBCUS01C | CBCUS01C.cbl | 178 | **Customer File Read** -- Sequentially reads CUSTDAT VSAM and displays customer records. | Data Utility |
| 23 | CBTRN01C | CBTRN01C.cbl | 494 | **Transaction File Read** -- Sequentially reads TRANSACT VSAM and displays transaction records. | Data Utility |
| 24 | CBTRN02C | CBTRN02C.cbl | 731 | **Transaction Posting** -- Reads daily transactions (DALYTRAN), validates against CARDXREF, posts to TRANSACT VSAM, updates account balances in ACCTDAT. Core batch processing. | Financial / Core Batch |
| 25 | CBTRN03C | CBTRN03C.cbl | 649 | **Transaction Report Generation** -- Reads sorted transactions, looks up CARDXREF and TRANTYPE for descriptions, writes formatted report to REPORTFL. | Reporting |
| 26 | CBSTM03A | CBSTM03A.CBL | 924 | **Statement Generation (Main)** -- Reads TRANSACT, CARDXREF, CUSTDAT, ACCTDAT; generates customer statements; calls CBSTM03B for output formatting. | Reporting / Core Batch |
| 27 | CBSTM03B | CBSTM03B.CBL | 230 | **Statement Generation (Output)** -- Sub-program called by CBSTM03A; writes formatted statement lines to output files. | Reporting |
| 28 | CBEXPORT | CBEXPORT.cbl | 582 | **Data Export** -- Reads CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT and writes export records to a sequential file. | Data Migration |
| 29 | CBIMPORT | CBIMPORT.cbl | 487 | **Data Import** -- Reads export sequential file and loads records into CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT VSAM files. | Data Migration |
| 30 | COBSWAIT | COBSWAIT.cbl | 41 | **Wait Utility** -- Calls MVSWAIT assembler program to pause execution for a specified duration. | Utility |

### 1.3 Shared Sub-Programs

| # | Program | File | Lines | Description | Classification |
|---|---------|------|-------|-------------|----------------|
| 31 | CSUTLDTC | CSUTLDTC.cbl | 157 | **Date Validation Utility** -- Validates and converts dates using LE callable service CEEDAYS. Called by COTRN02C and CORPT00C. | Utility |

---

## 2. COBOL Programs -- Optional Modules

### 2.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | File | Lines | Description | Classification |
|---|---------|------|-------|-------------|----------------|
| 32 | COPAUA0C | COPAUA0C.cbl | 1,026 | **MQ Trigger Handler** -- Triggered by MQ message arrival; reads request from queue, looks up account/customer via VSAM, sends authorization response via MQ. | Integration / MQ |
| 33 | COPAUS0C | COPAUS0C.cbl | 1,032 | **Pending Authorization Summary** -- CICS online program; displays list of pending authorizations from IMS database. | Authorization |
| 34 | COPAUS1C | COPAUS1C.cbl | 604 | **Pending Authorization Detail** -- CICS online program; displays detail of a pending auth record; links to COPAUS2C for DB2 fraud marking. | Authorization |
| 35 | COPAUS2C | COPAUS2C.cbl | 244 | **DB2 Fraud Marking** -- Linked sub-program; inserts/updates fraud flags in DB2 PAUTDB table using embedded SQL. | Authorization / DB2 |
| 36 | CBPAUP0C | CBPAUP0C.cbl | 386 | **Batch Purge** -- Batch program to purge processed authorization records from IMS database. | Batch / IMS |
| 37 | DBUNLDGS | DBUNLDGS.CBL | 366 | **IMS DB Unload (GSAM)** -- Unloads IMS segments to a GSAM sequential dataset using DL/I calls. | Data Utility / IMS |
| 38 | PAUDBLOD | PAUDBLOD.CBL | 369 | **IMS DB Load** -- Loads authorization records into IMS database from sequential input using DL/I ISRT calls. | Data Utility / IMS |
| 39 | PAUDBUNL | PAUDBUNL.CBL | 317 | **IMS DB Unload** -- Unloads authorization records from IMS database to sequential output using DL/I GN/GNP calls. | Data Utility / IMS |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program | File | Lines | Description | Classification |
|---|---------|------|-------|-------------|----------------|
| 40 | COTRTLIC | COTRTLIC.cbl | 2,098 | **Transaction Type List (DB2)** -- CICS online; lists and deletes transaction types and categories via DB2 cursors. Complex screen navigation. | Admin / DB2 |
| 41 | COTRTUPC | COTRTUPC.cbl | 1,702 | **Transaction Type Maintenance (DB2)** -- CICS online; add/edit transaction types and categories via DB2 embedded SQL. | Admin / DB2 |
| 42 | COBTUPDT | COBTUPDT.cbl | 237 | **Batch Transaction Type Update** -- Batch program; reads sequential input and updates DB2 transaction type table. | Batch / DB2 |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | File | Lines | Description | Classification |
|---|---------|------|-------|-------------|----------------|
| 43 | COACCT01 | COACCT01.cbl | 620 | **Account Inquiry via MQ** -- CICS program triggered by MQ; reads request message, looks up ACCTDAT VSAM, sends response via MQ. | Integration / MQ |
| 44 | CODATE01 | CODATE01.cbl | 524 | **System Date via MQ** -- CICS program triggered by MQ; reads request message, returns current system date/time via MQ response. | Integration / MQ |

---

## 3. Copybooks -- Core Data Structures (`app/cpy/`)

| # | Copybook | File | Record Name | Size | Description | Classification |
|---|----------|------|-------------|------|-------------|----------------|
| 1 | CVACT01Y | CVACT01Y.cpy | ACCOUNT-RECORD | 300 bytes | Account master -- balance, credit limits, dates, zip, group | Account Entity |
| 2 | CVACT02Y | CVACT02Y.cpy | CARD-RECORD | 150 bytes | Credit card -- card number, account ID, status, expiry | Card Entity |
| 3 | CVACT03Y | CVACT03Y.cpy | CARD-XREF-RECORD | 50 bytes | Card-to-account cross-reference lookup | Cross-Reference |
| 4 | CVCUS01Y | CVCUS01Y.cpy | CUSTOMER-RECORD | 500 bytes | Customer demographics -- name, address, SSN, DOB, FICO | Customer Entity |
| 5 | CVTRA05Y | CVTRA05Y.cpy | TRAN-RECORD | 350 bytes | Transaction master -- ID, type, amount, merchant, timestamps | Transaction Entity |
| 6 | CVTRA06Y | CVTRA06Y.cpy | DALYTRAN-RECORD | 350 bytes | Daily transaction -- same layout as TRAN-RECORD for daily batch | Transaction Entity |
| 7 | CVTRA01Y | CVTRA01Y.cpy | TRAN-CAT-BAL-RECORD | ~50 bytes | Transaction category balance -- per-account category totals | Transaction Category |
| 8 | CVTRA02Y | CVTRA02Y.cpy | DIS-GROUP-RECORD | ~50 bytes | Discount/interest group -- rate by group and transaction type | Interest / Discount |
| 9 | CVTRA03Y | CVTRA03Y.cpy | TRAN-TYPE-RECORD | 60 bytes | Transaction type -- code and description | Reference Data |
| 10 | CVTRA04Y | CVTRA04Y.cpy | TRAN-CAT-RECORD | 60 bytes | Transaction category -- type+category code and description | Reference Data |
| 11 | CVTRA07Y | CVTRA07Y.cpy | REPORT-NAME-HEADER | ~133 bytes | Report headers, detail line, and total line layouts | Report Layout |
| 12 | CSUSR01Y | CSUSR01Y.cpy | SEC-USER-DATA | 80 bytes | User security record -- ID, password, name, user type | Security Entity |
| 13 | COCOM01Y | COCOM01Y.cpy | CARDDEMO-COMMAREA | variable | CICS communication area -- passed between programs for navigation state | Control Structure |
| 14 | COMEN02Y | COMEN02Y.cpy | CARDDEMO-MAIN-MENU-OPTIONS | variable | Main menu option table -- 11 entries mapping options to program names | Menu Config |
| 15 | COADM02Y | COADM02Y.cpy | CARDDEMO-ADMIN-MENU-OPTIONS | variable | Admin menu option table -- 6 entries mapping admin options to programs | Menu Config |
| 16 | COTTL01Y | COTTL01Y.cpy | -- | variable | Screen title/header area layout | UI Layout |
| 17 | CSDAT01Y | CSDAT01Y.cpy | -- | variable | Date display area for screen headers | UI Layout |
| 18 | CSMSG01Y | CSMSG01Y.cpy | -- | variable | User message/status display area | UI Layout |
| 19 | CSMSG02Y | CSMSG02Y.cpy | -- | variable | Abend handling work areas | Error Handling |
| 20 | CVCRD01Y | CVCRD01Y.cpy | CC-WORK-AREAS | variable | Credit card screen work areas -- AID keys, navigation state | Control Structure |
| 21 | CSSETATY | CSSETATY.cpy | -- | variable | Screen attribute setting utility | UI Utility |
| 22 | CSSTRPFY | CSSTRPFY.cpy | -- | variable | String manipulation/strip utility paragraph (COPY'd into procedure division) | Utility |
| 23 | CSLKPCDY | CSLKPCDY.cpy | -- | variable | Lookup code validation areas | Utility |
| 24 | CSUTLDPY | CSUTLDPY.cpy | -- | variable | Date utility parameters for CSUTLDTC | Utility |
| 25 | CSUTLDWY | CSUTLDWY.cpy | -- | variable | Date utility work areas / edit masks | Utility |
| 26 | CODATECN | CODATECN.cpy | CODATECN-REC | variable | Date conversion record for COBDATFT assembler call | Utility |
| 27 | CUSTREC | CUSTREC.cpy | CUSTOMER-RECORD | ~500 bytes | Alternate customer record layout (used by CBSTM03A) | Customer Entity |
| 28 | COSTM01 | COSTM01.CPY | -- | variable | Statement processing work areas -- transaction detail for statement output | Report Layout |
| 29 | CVEXPORT | CVEXPORT.cpy | -- | variable | Export record layout -- envelope with type, timestamp, and embedded entity data | Data Migration |
| 30 | UNUSED1Y | UNUSED1Y.cpy | UNUSED-DATA | 80 bytes | Unused/deprecated record layout | Deprecated |

---

## 4. Copybooks -- Optional Modules

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 31 | CCPAURQY | Authorization request message layout (MQ) |
| 32 | CCPAURLY | Authorization reply message layout (MQ) |
| 33 | CCPAUERY | Authorization error message layout (MQ) |
| 34 | CIPAUSMY | Authorization summary record (IMS segment) |
| 35 | CIPAUDTY | Authorization detail record (IMS segment) |
| 36 | IMSFUNCS | IMS DL/I function code constants (GN, GNP, ISRT, GU, etc.) |
| 37 | PAUTBPCB | IMS PCB (Program Communication Block) for auth database |
| 38 | PASFLPCB | IMS PCB for summary segment |
| 39 | PADFLPCB | IMS PCB for detail segment |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 40 | CSDB2RPY | DB2 reply/result work areas |
| 41 | CSDB2RWY | DB2 read/write work areas |

---

## 5. BMS Screen Maps

### 5.1 Core BMS Maps (`app/bms/`)

| # | Map | BMS File | Generated Copybook | Associated Program | Description |
|---|-----|----------|-------------------|-------------------|-------------|
| 1 | COSGN00 | COSGN00.bms | COSGN00.CPY | COSGN00C | Sign-on screen |
| 2 | COMEN01 | COMEN01.bms | COMEN01.CPY | COMEN01C | Main menu screen |
| 3 | COADM01 | COADM01.bms | COADM01.CPY | COADM01C | Admin menu screen |
| 4 | COACTVW | COACTVW.bms | COACTVW.CPY | COACTVWC | Account view screen |
| 5 | COACTUP | COACTUP.bms | COACTUP.CPY | COACTUPC | Account update screen |
| 6 | COCRDLI | COCRDLI.bms | COCRDLI.CPY | COCRDLIC | Credit card list screen |
| 7 | COCRDSL | COCRDSL.bms | COCRDSL.CPY | COCRDSLC | Credit card detail screen |
| 8 | COCRDUP | COCRDUP.bms | COCRDUP.CPY | COCRDUPC | Credit card update screen |
| 9 | COTRN00 | COTRN00.bms | COTRN00.CPY | COTRN00C | Transaction list screen |
| 10 | COTRN01 | COTRN01.bms | COTRN01.CPY | COTRN01C | Transaction detail screen |
| 11 | COTRN02 | COTRN02.bms | COTRN02.CPY | COTRN02C | Transaction add screen |
| 12 | CORPT00 | CORPT00.bms | CORPT00.CPY | CORPT00C | Report selection screen |
| 13 | COBIL00 | COBIL00.bms | COBIL00.CPY | COBIL00C | Bill payment screen |
| 14 | COUSR00 | COUSR00.bms | COUSR00.CPY | COUSR00C | User list screen |
| 15 | COUSR01 | COUSR01.bms | COUSR01.CPY | COUSR01C | User add screen |
| 16 | COUSR02 | COUSR02.bms | COUSR02.CPY | COUSR02C | User update screen |
| 17 | COUSR03 | COUSR03.bms | COUSR03.CPY | COUSR03C | User delete screen |

### 5.2 Optional Module BMS Maps

| # | Map | Module | Generated Copybook | Associated Program | Description |
|---|-----|--------|-------------------|-------------------|-------------|
| 18 | COPAU00 | Auth IMS/DB2/MQ | COPAU00.cpy | COPAUS0C | Pending authorization summary screen |
| 19 | COPAU01 | Auth IMS/DB2/MQ | COPAU01.cpy | COPAUS1C | Pending authorization detail screen |
| 20 | COTRTLI | Tran Type DB2 | COTRTLI.cpy | COTRTLIC | Transaction type list screen |
| 21 | COTRTUP | Tran Type DB2 | COTRTUP.cpy | COTRTUPC | Transaction type maintenance screen |

---

## 6. JCL Batch Jobs (`app/jcl/`)

### 6.1 Data Refresh / File Load Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 1 | ACCTFILE | ACCTFILE.jcl | IDCAMS | Delete/define and reload ACCTDAT VSAM KSDS from sequential PS |
| 2 | CARDFILE | CARDFILE.jcl | IDCAMS | Delete/define and reload CARDDAT VSAM KSDS from sequential PS |
| 3 | CUSTFILE | CUSTFILE.jcl | IDCAMS | Delete/define and reload CUSTDAT VSAM KSDS from sequential PS |
| 4 | XREFFILE | XREFFILE.jcl | IDCAMS | Delete/define and reload CARDXREF VSAM KSDS; define alternate indexes |
| 5 | TRANFILE | TRANFILE.jcl | IDCAMS | Delete/define and reload TRANSACT VSAM KSDS from daily transaction PS |
| 6 | DUSRSECJ | DUSRSECJ.jcl | IDCAMS | Delete/define and reload USRSEC VSAM KSDS (user security) |
| 7 | TRANTYPE | TRANTYPE.jcl | IDCAMS | Delete/define and reload TRANTYPE VSAM KSDS |
| 8 | TRANCATG | TRANCATG.jcl | IDCAMS | Delete/define and reload TRANCATG VSAM KSDS (transaction categories) |
| 9 | TCATBALF | TCATBALF.jcl | IDCAMS | Delete/define and reload TCATBALF VSAM KSDS (category balances) |
| 10 | DISCGRP | DISCGRP.jcl | IDCAMS | Define DISCGRP VSAM KSDS (discount/interest groups) |
| 11 | DEFCUST | DEFCUST.jcl | IDCAMS | Define CUSTDAT VSAM cluster |
| 12 | REPTFILE | REPTFILE.jcl | IDCAMS | Define REPORTFL sequential dataset for report output |

### 6.2 Core Batch Processing Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 13 | POSTTRAN | POSTTRAN.jcl | CBTRN02C | Post daily transactions to master transaction file |
| 14 | INTCALC | INTCALC.jcl | CBACT04C | Calculate interest on account balances by category |
| 15 | COMBTRAN | COMBTRAN.jcl | SORT+IDCAMS | Combine/merge daily transactions into master |
| 16 | CREASTMT | CREASTMT.JCL | SORT+IDCAMS+CBSTM03A | Generate customer statements (sort, load VSAM, run statement program) |
| 17 | TRANREPT | TRANREPT.jcl | REPROC+SORT+CBTRN03C | Backup transactions, sort by date, generate transaction report |
| 18 | TRANBKP | TRANBKP.jcl | REPROC+IDCAMS | Backup transaction VSAM to GDG, then delete/redefine |
| 19 | TRANIDX | TRANIDX.jcl | IDCAMS | Define/rebuild alternate indexes on TRANSACT VSAM |

### 6.3 CICS File Control Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 20 | CLOSEFIL | CLOSEFIL.jcl | SDSF | Close CICS files for batch processing |
| 21 | OPENFIL | OPENFIL.jcl | SDSF | Reopen CICS files after batch processing |

### 6.4 Data Read/Validation Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 22 | READACCT | READACCT.jcl | CBACT01C | Read and display account file records |
| 23 | READCARD | READCARD.jcl | CBACT02C | Read and display card file records |
| 24 | READXREF | READXREF.jcl | CBACT03C | Read and display cross-reference file records |
| 25 | READCUST | READCUST.jcl | CBCUS01C | Read and display customer file records |

### 6.5 Export/Import Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 26 | CBEXPORT | CBEXPORT.jcl | CBEXPORT | Export all VSAM data to sequential file |
| 27 | CBIMPORT | CBIMPORT.jcl | CBIMPORT | Import sequential file data to VSAM files |

### 6.6 Utility / Infrastructure Jobs

| # | Job | File | Executes | Description |
|---|-----|------|----------|-------------|
| 28 | DEFGDGB | DEFGDGB.jcl | IDCAMS | Define GDG base for transaction backups |
| 29 | DEFGDGD | DEFGDGD.jcl | IDCAMS | Define GDG base for daily transactions |
| 30 | DALYREJS | DALYREJS.jcl | IDCAMS | Define daily rejects dataset |
| 31 | ESDSRRDS | ESDSRRDS.jcl | IDCAMS | Define ESDS and RRDS VSAM clusters |
| 32 | PRTCATBL | PRTCATBL.jcl | IDCAMS | Print catalog entries |
| 33 | WAITSTEP | WAITSTEP.jcl | COBSWAIT | Execute wait utility (job scheduling spacer) |
| 34 | FTPJCL | FTPJCL.JCL | FTP | FTP file transfer job |
| 35 | INTRDRJ1 | INTRDRJ1.JCL | IDCAMS+IEBGENER | Internal reader job -- copies and submits INTRDRJ2 |
| 36 | INTRDRJ2 | INTRDRJ2.JCL | IDCAMS | Internal reader job -- secondary backup copy |
| 37 | TXT2PDF1 | TXT2PDF1.JCL | IKJEFT1B | Convert statement text to PDF |
| 38 | CBADMCDJ | CBADMCDJ.jcl | (compile) | Compile CardDemo admin programs |

### 6.7 Optional Module JCL Jobs

#### Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job | File | Description |
|---|-----|------|-------------|
| 39 | CBPAUP0J | CBPAUP0J.jcl | Run batch purge of processed authorizations |
| 40 | DBPAUTP0 | DBPAUTP0.jcl | IMS DB utilities for auth database |
| 41 | LOADPADB | LOADPADB.JCL | Load authorization IMS database |
| 42 | UNLDGSAM | UNLDGSAM.JCL | Unload auth IMS DB via GSAM |
| 43 | UNLDPADB | UNLDPADB.JCL | Unload authorization IMS database |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`)

| # | Job | File | Description |
|---|-----|------|-------------|
| 44 | CREADB21 | CREADB21.jcl | Create DB2 tables for transaction types |
| 45 | MNTTRDB2 | MNTTRDB2.jcl | Batch maintain transaction types in DB2 |
| 46 | TRANEXTR | TRANEXTR.jcl | Extract transaction types from DB2 |

---

## 7. Assembler Programs (`app/asm/`)

| # | Program | File | Description |
|---|---------|------|-------------|
| 1 | COBDATFT | COBDATFT.asm | Date formatting utility -- called by CBACT01C to format dates |
| 2 | MVSWAIT | MVSWAIT.asm | Wait/sleep utility -- called by COBSWAIT to pause execution |

---

## 8. JCL Procedures (`app/proc/`)

| # | Procedure | File | Description |
|---|-----------|------|-------------|
| 1 | REPROC | REPROC.prc | Reusable REPRO (copy) procedure for VSAM-to-sequential backup |
| 2 | TRANREPT | TRANREPT.prc | Transaction report generation procedure |

---

## 9. Scheduler Configurations (`app/scheduler/`)

| # | Config | File | Description |
|---|--------|------|-------------|
| 1 | CA7 | CardDemo.ca7 | CA7 job scheduler definitions for the batch cycle |
| 2 | Control-M | CardDemo.controlm | Control-M scheduler definitions for the batch cycle |

---

## 10. Classification Summary

| Domain | Programs | Key Files |
|--------|----------|-----------|
| **Account Management** | COACTVWC, COACTUPC, CBACT01C, CBACT04C | ACCTDAT VSAM |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C | CARDDAT VSAM |
| **Transaction Processing** | COTRN00C-02C, CBTRN01C-02C | TRANSACT, DALYTRAN VSAM |
| **Reporting / Statements** | CORPT00C, CBTRN03C, CBSTM03A/B | REPORTFL, STATEMNT |
| **Bill Payment** | COBIL00C | TRANSACT, ACCTDAT VSAM |
| **User Security** | COUSR00C-03C, COSGN00C | USRSEC VSAM |
| **Navigation** | COMEN01C, COADM01C | -- |
| **Data Migration** | CBEXPORT, CBIMPORT | Sequential export file |
| **Authorization (IMS/MQ)** | COPAUA0C, COPAUS0C-2C, CBPAUP0C | IMS DB, MQ queues |
| **Tran Type Admin (DB2)** | COTRTLIC, COTRTUPC, COBTUPDT | DB2 TRTYP/TRCAT tables |
| **MQ Integration** | COACCT01, CODATE01 | MQ request/reply queues |
| **Utilities** | CSUTLDTC, COBSWAIT, COBDATFT, MVSWAIT | -- |
