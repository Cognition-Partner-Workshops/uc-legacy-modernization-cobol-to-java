# Application Inventory -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Scope:** `app/` directory of CardDemo  
> **Total Assets:** 31 COBOL programs, 30 copybooks, 38 JCL jobs, 17 BMS maps, 2 assembler modules, 2 JCL procedures  
> **Optional Modules:** Authorization (IMS/DB2/MQ), Transaction-Type (DB2), VSAM-MQ

---

## 1. COBOL Programs (`app/cbl/`) -- 31 Programs

### 1.1 Online CICS Programs (17)

| # | Program | LOC | CICS Trans | Description | Classification |
|---|---------|-----|-----------|-------------|----------------|
| 1 | **COSGN00C** | 260 | CC00 | Sign-on / Authentication | Security / Session |
| 2 | **COMEN01C** | 308 | CM00 | Main Menu -- dispatches to sub-screens | Navigation / Router |
| 3 | **COADM01C** | 288 | CA00 | Admin Menu -- admin-only operations | Navigation / Admin |
| 4 | **COACTVWC** | 941 | CA01 | Account View (read-only) | Account Mgmt |
| 5 | **COACTUPC** | 4,236 | CA02 | Account Update (full CRUD) | Account Mgmt |
| 6 | **COCRDLIC** | 1,459 | CC01 | Card List -- browse cards by account | Card Mgmt |
| 7 | **COCRDSLC** | 887 | CC02 | Card Detail View (read-only) | Card Mgmt |
| 8 | **COCRDUPC** | 1,560 | CC03 | Card Update | Card Mgmt |
| 9 | **COTRN00C** | 699 | CT00 | Transaction List -- paginated browse | Transaction Mgmt |
| 10 | **COTRN01C** | 330 | CT01 | Transaction Detail View | Transaction Mgmt |
| 11 | **COTRN02C** | 783 | CT02 | Transaction Add (new transaction entry) | Transaction Mgmt |
| 12 | **COBIL00C** | 572 | CB00 | Bill Payment -- posts payment transactions | Billing / Payments |
| 13 | **CORPT00C** | 649 | CR00 | Transaction Report Request (online) | Reporting |
| 14 | **COUSR00C** | 695 | CU00 | User List (admin) | User Admin |
| 15 | **COUSR01C** | 299 | CU01 | User Add (admin) | User Admin |
| 16 | **COUSR02C** | 414 | CU02 | User Update (admin) | User Admin |
| 17 | **COUSR03C** | 359 | CU03 | User Delete (admin) | User Admin |

### 1.2 Batch Programs (12)

| # | Program | LOC | Description | Classification |
|---|---------|-----|-------------|----------------|
| 1 | **CBTRN01C** | 494 | Daily transaction validation -- cross-references cards, customers, accounts | Transaction Processing |
| 2 | **CBTRN02C** | 731 | Transaction posting -- validates and posts daily transactions to master, writes rejects | Transaction Processing |
| 3 | **CBTRN03C** | 649 | Transaction report generation -- daily report with type/category lookups | Reporting |
| 4 | **CBACT01C** | 430 | Account file reader/converter -- reads VSAM, writes fixed/array/variable formats | Data Utility |
| 5 | **CBACT02C** | 178 | Card file reader -- reads and displays card VSAM records | Data Utility |
| 6 | **CBACT03C** | 178 | Cross-reference file reader -- reads and displays XREF VSAM records | Data Utility |
| 7 | **CBACT04C** | 652 | Interest calculation -- computes interest/fees per category balance | Financial Calc |
| 8 | **CBCUS01C** | 178 | Customer file reader -- reads and displays customer VSAM records | Data Utility |
| 9 | **CBSTM03A** | 924 | Statement generation (driver) -- iterates XREF, builds HTML+text statements | Statement Gen |
| 10 | **CBSTM03B** | 230 | Statement generation (sub-program) -- file I/O helper called by CBSTM03A | Statement Gen |
| 11 | **CBEXPORT** | 582 | Data export -- exports all VSAM files to a single sequential export file | Data Migration |
| 12 | **CBIMPORT** | 487 | Data import -- imports export file back into VSAM files | Data Migration |

### 1.3 Utility Programs (2)

| # | Program | LOC | Description | Classification |
|---|---------|-----|-------------|----------------|
| 1 | **CSUTLDTC** | 157 | Date utility -- converts dates using LE CEEDAYS callable service | Shared Utility |
| 2 | **COBSWAIT** | 41 | Wait utility -- calls MVSWAIT assembler routine for timed delays | Shared Utility |

---

## 2. Copybooks (`app/cpy/`) -- 30 Copybooks

### 2.1 Data-Structure Copybooks (VSAM Record Layouts)

| # | Copybook | LOC | Record | Description |
|---|----------|-----|--------|-------------|
| 1 | **CVACT01Y** | 20 | ACCT-RECORD (300 bytes) | Account master -- balances, limits, dates, status |
| 2 | **CVACT02Y** | 14 | CARD-RECORD (150 bytes) | Card master -- card number, account link, embossed name, expiry, CVV, status |
| 3 | **CVACT03Y** | 11 | CARD-XREF-RECORD (50 bytes) | Card-to-account cross-reference |
| 4 | **CVCUS01Y** | 26 | CUSTOMER-RECORD (500 bytes) | Customer master -- name, address, SSN, DOB, FICO, phone |
| 5 | **CVCRD01Y** | 46 | FD-CRDDAT-REC / working storage | Card data with extended fields (online view) |
| 6 | **CVTRA01Y** | 13 | TRAN-CAT-BAL-RECORD (50 bytes) | Transaction category balance -- running totals by type |
| 7 | **CVTRA02Y** | 13 | DIS-GROUP-RECORD (50 bytes) | Disclosure group -- interest rates by transaction type |
| 8 | **CVTRA03Y** | 10 | TRAN-TYPE-RECORD (60 bytes) | Transaction type reference -- type code + description |
| 9 | **CVTRA04Y** | 12 | TRAN-CAT-RECORD (60 bytes) | Transaction category -- type/category code + description |
| 10 | **CVTRA05Y** | 21 | TRAN-RECORD (350 bytes) | Transaction master -- full transaction with merchant, amount, timestamps |
| 11 | **CVTRA06Y** | 21 | DALYTRAN-RECORD (350 bytes) | Daily transaction -- same layout as CVTRA05Y for daily intake |
| 12 | **CVTRA07Y** | 73 | Report structures | Transaction report headers, detail lines, totals |
| 13 | **COSTM01** | 38 | TRNX-RECORD | Statement-altered transaction layout (keyed by card+tran) |
| 14 | **CUSTREC** | 26 | CUSTOMER-RECORD | Alternative customer record (for statement generation) |
| 15 | **CVEXPORT** | 103 | Export record layouts | Export/import record structures for all entity types |
| 16 | **UNUSED1Y** | 10 | UNUSED-DATA | Deprecated/unused data structure |

### 2.2 Common/UI Copybooks

| # | Copybook | LOC | Description |
|---|----------|-----|-------------|
| 1 | **COCOM01Y** | 47 | Common communication area -- passed between CICS programs (CDEMO-CDA-*) |
| 2 | **COMEN02Y** | 101 | Menu option definitions -- program names, transaction IDs per menu item |
| 3 | **COADM02Y** | 62 | Admin menu option definitions -- admin-specific program/transaction map |
| 4 | **COTTL01Y** | 27 | Title/header line definitions -- screen title bars |
| 5 | **CSDAT01Y** | 58 | Date/time working storage -- ASKTIME/FORMATTIME result fields |
| 6 | **CSMSG01Y** | 24 | Message area (single) -- WS-MESSAGE for screen messages |
| 7 | **CSMSG02Y** | 35 | Message area (dual) -- two-line message support |
| 8 | **CSUSR01Y** | 26 | User security record -- SEC-USR-ID, password, first/last name, type |

### 2.3 Utility Copybooks

| # | Copybook | LOC | Description |
|---|----------|-----|-------------|
| 1 | **CSLKPCDY** | 1,318 | Lookup code table -- state codes, country codes, and validation arrays |
| 2 | **CSSETATY** | 30 | Set attribute utility -- COPY REPLACING pattern for BMS field attributes |
| 3 | **CSSTRPFY** | 85 | String pad/fill utility -- pad/trim string routines |
| 4 | **CSUTLDPY** | 375 | Date utility parameters -- working storage for CSUTLDTC date conversion |
| 5 | **CSUTLDWY** | 89 | Date utility working storage -- additional date fields |
| 6 | **CODATECN** | 52 | Date conversion record -- COBDATFT assembler interface |

---

## 3. BMS Maps (`app/bms/`) -- 17 Maps

| # | Map File | LOC | Map Name | Screen | Description |
|---|----------|-----|----------|--------|-------------|
| 1 | **COSGN00.bms** | 210 | COSGN0A | Sign-on | User ID, password, error message fields |
| 2 | **COMEN01.bms** | 167 | COMEN1A | Main Menu | Numbered menu options, selection field |
| 3 | **COADM01.bms** | 167 | COADM1A | Admin Menu | Admin-specific menu options |
| 4 | **COACTVW.bms** | 378 | CACTVW | Account View | Account details display (read-only) |
| 5 | **COACTUP.bms** | 512 | CACTUP | Account Update | Editable account fields -- largest BMS map |
| 6 | **COCRDLI.bms** | 344 | CCRDLI | Card List | 7-row card browse with selection column |
| 7 | **COCRDSL.bms** | 157 | CCRDSL | Card Detail | Card detail display |
| 8 | **COCRDUP.bms** | 172 | CCRDUP | Card Update | Editable card fields |
| 9 | **COTRN00.bms** | 464 | COTRN0A | Transaction List | 10-row transaction browse |
| 10 | **COTRN01.bms** | 273 | COTRN1A | Transaction View | Transaction detail display |
| 11 | **COTRN02.bms** | 307 | COTRN2A | Transaction Add | New transaction entry form |
| 12 | **COBIL00.bms** | 141 | COBIL0A | Bill Payment | Payment entry with account/amount fields |
| 13 | **CORPT00.bms** | 231 | CORPT0A | Report Request | Report type selection + date range |
| 14 | **COUSR00.bms** | 463 | COUSR0A | User List | 10-row user browse (admin) |
| 15 | **COUSR01.bms** | 164 | COUSR1A | User Add | New user entry form (admin) |
| 16 | **COUSR02.bms** | 169 | COUSR2A | User Update | Edit user form (admin) |
| 17 | **COUSR03.bms** | 153 | COUSR3A | User Delete | Confirm user deletion (admin) |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 matching copybooks -- one per BMS map -- containing DFHMDF-generated input/output field definitions (e.g., `COSGN00.CPY`, `COACTUP.CPY`).

---

## 4. JCL Jobs (`app/jcl/`) -- 38 Jobs

### 4.1 Data Load / Refresh Jobs (10)

| # | JCL Job | LOC | Description | Key Datasets |
|---|---------|-----|-------------|--------------|
| 1 | **ACCTFILE** | 65 | Refresh account master VSAM from PS | ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | 128 | Refresh card master VSAM from PS | CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | 84 | Refresh customer master VSAM from PS | CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | 106 | Load card cross-reference VSAM | CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | 125 | Load daily transactions into VSAM | TRANSACT.VSAM.KSDS |
| 6 | **TRANTYPE** | 65 | Load transaction type reference | TRANTYPE.VSAM.KSDS |
| 7 | **TRANCATG** | 65 | Load transaction category reference | TRANCATG.VSAM.KSDS |
| 8 | **DUSRSECJ** | 92 | Load user security VSAM | USRSEC.VSAM.KSDS |
| 9 | **REPTFILE** | 32 | Refresh report parameter file | DATEPARM |
| 10 | **DEFCUST** | 47 | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |

### 4.2 Batch Processing Jobs (7)

| # | JCL Job | LOC | Description | Program(s) |
|---|---------|-----|-------------|------------|
| 1 | **POSTTRAN** | 45 | Core transaction posting | CBTRN02C |
| 2 | **INTCALC** | 44 | Interest calculation | CBACT04C |
| 3 | **COMBTRAN** | 52 | Combine daily with master transactions | CBTRN01C |
| 4 | **CREASTMT** | 97 | Statement generation (sort + CBSTM03A) | SORT, CBSTM03A |
| 5 | **TRANREPT** | 84 | Transaction report generation | SORT, CBTRN03C |
| 6 | **TRANBKP** | 71 | Backup transaction VSAM to GDG | IDCAMS REPRO |
| 7 | **WAITSTEP** | 27 | Timed wait step | COBSWAIT |

### 4.3 File Management / Utility Jobs (10)

| # | JCL Job | LOC | Description |
|---|---------|-----|-------------|
| 1 | **CLOSEFIL** | 34 | Close CICS files for batch processing |
| 2 | **OPENFIL** | 34 | Re-open CICS files after batch |
| 3 | **TRANIDX** | 58 | Define/build alternate index on transactions |
| 4 | **DEFGDGB** | 63 | Define GDG base for backups |
| 5 | **DEFGDGD** | 94 | Define GDG base for daily data |
| 6 | **DISCGRP** | 65 | Load disclosure group / interest rate reference |
| 7 | **TCATBALF** | 65 | Load transaction category balance file |
| 8 | **DALYREJS** | 32 | Define daily rejects sequential file |
| 9 | **ESDSRRDS** | 124 | Define ESDS/RRDS VSAM examples |
| 10 | **PRTCATBL** | 66 | Print catalog listing |

### 4.4 Read/Verify Jobs (4)

| # | JCL Job | LOC | Description |
|---|---------|-----|-------------|
| 1 | **READACCT** | 50 | Read/verify account VSAM |
| 2 | **READCARD** | 31 | Read/verify card VSAM |
| 3 | **READCUST** | 30 | Read/verify customer VSAM |
| 4 | **READXREF** | 31 | Read/verify cross-reference VSAM |

### 4.5 Export/Import & Infrastructure Jobs (5)

| # | JCL Job | LOC | Description |
|---|---------|-----|-------------|
| 1 | **CBEXPORT** | 72 | Run data export batch program |
| 2 | **CBIMPORT** | 68 | Run data import batch program |
| 3 | **FTPJCL** | 42 | FTP file transfer template |
| 4 | **TXT2PDF1** | 41 | Convert text statements to PDF |
| 5 | **CBADMCDJ** | 167 | Master admin job -- compiles all COBOL programs |

### 4.6 Internal Reader / Chaining Jobs (2)

| # | JCL Job | LOC | Description |
|---|---------|-----|-------------|
| 1 | **INTRDRJ1** | 19 | Internal reader -- submits INTRDRJ2 |
| 2 | **INTRDRJ2** | 14 | Internal reader -- chained from INTRDRJ1 |

---

## 5. Assembler Modules (`app/asm/`) -- 2 Programs

| # | Module | Description |
|---|--------|-------------|
| 1 | **MVSWAIT.asm** | Timed wait -- accepts interval and issues z/OS WAIT SVC |
| 2 | **COBDATFT.asm** | Date format conversion -- assembler date utility for COBOL |

---

## 6. JCL Procedures (`app/proc/`) -- 2 Procedures

| # | Procedure | Description |
|---|-----------|-------------|
| 1 | **REPROC.prc** | Reprocessing procedure template |
| 2 | **TRANREPT.prc** | Transaction report procedure -- called by TRANREPT.jcl |

---

## 7. Optional Extension Modules

### 7.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

| Type | Asset | Description |
|------|-------|-------------|
| COBOL | COPAUA0C | MQ trigger program -- receives authorization requests |
| COBOL | COPAUS0C | Authorization summary display |
| COBOL | COPAUS1C | Authorization detail display |
| COBOL | COPAUS2C | Fraud marking -- writes to DB2 |
| COBOL | CBPAUP0C | Batch purge of old authorizations |
| COBOL | PAUDBLOD | DB2 authorization table loader |
| COBOL | PAUDBUNL | DB2 authorization table unloader |
| COBOL | DBUNLDGS | GSAM unload utility |
| BMS | COPAU00, COPAU01 | Authorization screens |
| Copybook | CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB | IMS/DB2/MQ data structures and PCBs |
| JCL | CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB | Authorization batch jobs |

### 7.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| Type | Asset | Description |
|------|-------|-------------|
| COBOL | COTRTUPC | Transaction type add/update (DB2 embedded SQL) |
| COBOL | COTRTLIC | Transaction type list/delete (DB2 cursors) |
| COBOL | COBTUPDT | Batch transaction type update |
| BMS | COTRTLI, COTRTUP | Transaction type screens |
| Copybook | CSDB2RPY, CSDB2RWY | DB2 SQLCA and host variable structures |
| JCL | CREADB21, MNTTRDB2, TRANEXTR | DB2 setup and maintenance jobs |

### 7.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| Type | Asset | Description |
|------|-------|-------------|
| COBOL | CODATE01 | System date inquiry via MQ request/response |
| COBOL | COACCT01 | Account inquiry via MQ request/response |

---

## 8. Summary Statistics

| Category | Count | Total LOC |
|----------|-------|-----------|
| Online CICS Programs | 17 | 13,929 |
| Batch Programs | 12 | 6,113 |
| Utility Programs | 2 | 198 |
| **All Core Programs** | **31** | **20,240** |
| Copybooks (data) | 16 | 1,832 |
| Copybooks (common/UI) | 8 | 380 |
| Copybooks (utility) | 6 | 1,949 |
| **All Copybooks** | **30** | **4,161** |
| BMS Maps | 17 | 4,812 |
| BMS-Generated Copybooks | 17 | -- |
| JCL Jobs | 38 | 2,427 |
| Assembler Modules | 2 | -- |
| JCL Procedures | 2 | -- |
| Optional Module Programs | 13 | -- |
| **Grand Total Assets** | **150+** | **31,640+** |

---

## 9. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C (Sign-on) |
| `CB*` | Batch COBOL program | CBTRN02C (Transaction posting) |
| `CS*` | Common/shared copybook | CSDAT01Y (Date fields) |
| `CV*` | VSAM record layout copybook | CVACT01Y (Account record) |
| `CO*` (cpy) | Communication area copybook | COCOM01Y (Common area) |
| `*Y` suffix | Copybook (data structure) | CSUSR01Y |
| `*C` suffix | COBOL program | COACTVWC |
