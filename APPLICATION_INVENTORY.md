# APPLICATION INVENTORY -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Mainframe Credit Card Management)
> **Total Artifacts:** 31 COBOL Programs + 13 Optional-Module Programs + 30 Copybooks + 11 Optional-Module Copybooks + 17 BMS Maps + 17 BMS Copybooks + 38 JCL Jobs + 2 Assembler Programs + 2 JCL Procedures

---

## 1. COBOL Programs (Core -- `app/cbl/`)

| # | Program | LOC | Type | CICS Trans | Function | VSAM / Batch Files | Classification |
|---|---------|-----|------|------------|----------|--------------------|----------------|
| 1 | **COSGN00C.cbl** | 260 | Online CICS | CC00 | Signon screen -- authenticates user against USRSEC VSAM | USRSEC (R) | Security / Auth |
| 2 | **COMEN01C.cbl** | 308 | Online CICS | CM00 | Main menu for regular users -- dispatches to sub-programs via XCTL | -- | Navigation |
| 3 | **COADM01C.cbl** | 288 | Online CICS | CA00 | Admin menu -- dispatches to user CRUD and admin functions | -- | Navigation / Admin |
| 4 | **COACTVWC.cbl** | 941 | Online CICS | CA01 | View account details (read-only) | ACCTDATA (R), CARDXREF (R), CUSTDATA (R) | Account Mgmt |
| 5 | **COACTUPC.cbl** | 4236 | Online CICS | CA02 | Update account information (largest program) | ACCTDATA (R/W), CARDXREF (R), CUSTDATA (R) | Account Mgmt |
| 6 | **COCRDLIC.cbl** | 1459 | Online CICS | CC01 | List credit cards with paging | CARDDATA (R) | Card Mgmt |
| 7 | **COCRDSLC.cbl** | 887 | Online CICS | CC02 | View credit card details (read-only) | CARDDATA (R), CUSTDATA (R) | Card Mgmt |
| 8 | **COCRDUPC.cbl** | 1560 | Online CICS | CC03 | Update credit card information | CARDDATA (R/W), CUSTDATA (R) | Card Mgmt |
| 9 | **COTRN00C.cbl** | 699 | Online CICS | CT00 | List transactions with browse/paging | TRANSACT (R) | Transaction Mgmt |
| 10 | **COTRN01C.cbl** | 330 | Online CICS | CT01 | View a single transaction (read-only) | TRANSACT (R) | Transaction Mgmt |
| 11 | **COTRN02C.cbl** | 783 | Online CICS | CT02 | Add a new transaction | TRANSACT (R/W), ACCTDATA (R), CARDXREF (R) | Transaction Mgmt |
| 12 | **CORPT00C.cbl** | 649 | Online CICS | CR00 | Submit batch transaction report via TDQ | -- (TDQ write) | Reporting |
| 13 | **COBIL00C.cbl** | 572 | Online CICS | CB00 | Bill payment -- pay account balance, create payment transaction | ACCTDATA (R/W), CARDXREF (R), TRANSACT (W) | Bill Payment |
| 14 | **COUSR00C.cbl** | 695 | Online CICS | CU00 | List all users from USRSEC with paging | USRSEC (R) | User Admin |
| 15 | **COUSR01C.cbl** | 299 | Online CICS | CU01 | Add a new user to USRSEC | USRSEC (W) | User Admin |
| 16 | **COUSR02C.cbl** | 414 | Online CICS | CU02 | Update an existing user in USRSEC | USRSEC (R/W) | User Admin |
| 17 | **COUSR03C.cbl** | 359 | Online CICS | CU03 | Delete a user from USRSEC | USRSEC (R/D) | User Admin |
| 18 | **CSUTLDTC.cbl** | 157 | Subroutine | -- | Date utility -- calls LE CEEDAYS for date conversion | -- | Utility |
| 19 | **CBACT01C.cbl** | 430 | Batch | -- | Read account file, write to multiple output formats | ACCTFILE (R), OUTFILE/ARRYFILE/VBRCFILE (W) | Data Utility |
| 20 | **CBACT02C.cbl** | 178 | Batch | -- | Read and print card data file | CARDFILE (R) | Data Utility |
| 21 | **CBACT03C.cbl** | 178 | Batch | -- | Read and print account cross-reference file | XREFFILE (R) | Data Utility |
| 22 | **CBACT04C.cbl** | 652 | Batch | -- | Interest calculator -- computes interest on transactions by category | TCATBALF (R/W), XREFFILE (R), ACCTFILE (R), DISCGRP (R), TRANSACT (W) | Financial Calc |
| 23 | **CBCUS01C.cbl** | 178 | Batch | -- | Read and print customer data file | CUSTFILE (R) | Data Utility |
| 24 | **CBTRN01C.cbl** | 494 | Batch | -- | Post records from daily transaction file (simple posting) | DALYTRAN (R), CUSTFILE (R), XREFFILE (R), CARDFILE (R), ACCTFILE (R), TRANFILE (W) | Transaction Posting |
| 25 | **CBTRN02C.cbl** | 731 | Batch | -- | Post daily transactions with validation and rejection handling | DALYTRAN (R), TRANFILE (R/W), XREFFILE (R), DALYREJS (W), ACCTFILE (R/W), TCATBALF (R/W) | Transaction Posting |
| 26 | **CBTRN03C.cbl** | 649 | Batch | -- | Print transaction detail report | TRANFILE (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATEPARM (R), TRANREPT (W) | Reporting |
| 27 | **CBSTM03A.CBL** | 924 | Batch | -- | Generate account statements in plain text and HTML | TRNXFILE (R), XREFFILE (R), ACCTFILE (R), CUSTFILE (R), STMTFILE (W), HTMLFILE (W) | Statement Gen |
| 28 | **CBSTM03B.CBL** | 230 | Batch Sub | -- | Subroutine for CBSTM03A -- file I/O for statement generation | TRNXFILE (R), XREFFILE (R), CUSTFILE (R), ACCTFILE (R) | Statement Gen |
| 29 | **CBEXPORT.cbl** | 582 | Batch | -- | Export customer data for branch migration (multi-record export) | CUSTFILE (R), ACCTFILE (R), XREFFILE (R), TRANSACT (R), CARDFILE (R), EXPFILE (W) | Data Migration |
| 30 | **CBIMPORT.cbl** | 487 | Batch | -- | Import customer data from branch migration export with validation | EXPFILE (R), CUSTOUT/ACCTOUT/XREFOUT/TRNXOUT/CARDOUT/ERROUT (W) | Data Migration |
| 31 | **COBSWAIT.cbl** | 41 | Batch Util | -- | Wait utility -- calls ASM MVSWAIT with centisecond parameter | -- | Utility |

**Subtotals:** 19,269 LOC across 31 programs (17 Online CICS + 12 Batch + 2 Subroutines)

---

## 2. Optional Module Programs

### 2a. Authorization Module (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | LOC | Type | Function | Technology |
|---|---------|-----|------|----------|------------|
| 1 | **COPAUA0C.cbl** | 1026 | CICS/MQ | Card authorization decision -- MQ trigger, reads request queue, approves/declines | CICS + MQ + VSAM |
| 2 | **COPAUS0C.cbl** | 1032 | CICS/BMS | Summary view of pending authorization messages | CICS + IMS + BMS |
| 3 | **COPAUS1C.cbl** | 604 | CICS/BMS | Detail view of a single authorization message | CICS + IMS + BMS |
| 4 | **COPAUS2C.cbl** | 244 | CICS/DB2 | Mark authorization message as fraud (DB2 update) | CICS + DB2 |
| 5 | **CBPAUP0C.cbl** | 386 | Batch/IMS | Purge expired pending authorization messages | IMS Batch |
| 6 | **DBUNLDGS.CBL** | 366 | Batch/IMS | Unload IMS segments to GSAM sequential files | IMS + GSAM |
| 7 | **PAUDBLOD.CBL** | 369 | Batch/IMS | Load authorization data into IMS database from flat files | IMS Batch |
| 8 | **PAUDBUNL.CBL** | 317 | Batch/IMS | Unload authorization data from IMS database to flat files | IMS Batch |

**Subtotal:** 4,344 LOC across 8 programs

### 2b. Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program | LOC | Type | Function | Technology |
|---|---------|-----|------|----------|------------|
| 1 | **COTRTLIC.cbl** | 2098 | CICS/DB2 | List transaction types with cursor-based paging; select, delete, update | CICS + DB2 + BMS |
| 2 | **COTRTUPC.cbl** | 1702 | CICS/DB2 | Add/edit transaction type records in DB2 | CICS + DB2 + BMS |
| 3 | **COBTUPDT.cbl** | 237 | Batch/DB2 | Batch update of transaction types from sequential file | Batch + DB2 |

**Subtotal:** 4,037 LOC across 3 programs

### 2c. VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | LOC | Type | Function | Technology |
|---|---------|-----|------|----------|------------|
| 1 | **COACCT01.cbl** | 620 | CICS/MQ | MQ-triggered account inquiry -- reads request from MQ, looks up VSAM, returns response | CICS + MQ + VSAM |
| 2 | **CODATE01.cbl** | 524 | CICS/MQ | MQ-triggered system date service -- returns formatted date via MQ | CICS + MQ |

**Subtotal:** 1,144 LOC across 2 programs

---

## 3. Copybooks (Core -- `app/cpy/`)

| # | Copybook | LOC | Record Length | Function | Business Entity |
|---|----------|-----|---------------|----------|-----------------|
| 1 | **CVACT01Y.cpy** | 17 | 300 bytes | Account master record layout | Account |
| 2 | **CVACT02Y.cpy** | 16 | 150 bytes | Card data record layout | Card |
| 3 | **CVACT03Y.cpy** | 9 | 50 bytes | Card cross-reference (card-num to acct-id) | Card-Account XREF |
| 4 | **CVCUS01Y.cpy** | 33 | 500 bytes | Customer master record | Customer |
| 5 | **CVCRD01Y.cpy** | 15 | -- | Card record internal layout (working storage) | Card |
| 6 | **CVTRA01Y.cpy** | 13 | 50 bytes | Transaction category balance record | Tran Cat Balance |
| 7 | **CVTRA02Y.cpy** | 13 | 50 bytes | Disclosure group (interest rate by tran type) | Disclosure Group |
| 8 | **CVTRA03Y.cpy** | 10 | 60 bytes | Transaction type master record | Transaction Type |
| 9 | **CVTRA04Y.cpy** | 12 | 60 bytes | Transaction category type record | Transaction Category |
| 10 | **CVTRA05Y.cpy** | 21 | 350 bytes | Transaction record (online) | Transaction |
| 11 | **CVTRA06Y.cpy** | 21 | 350 bytes | Daily transaction record (batch input) | Daily Transaction |
| 12 | **CVTRA07Y.cpy** | 73 | -- | Reporting data structures (headers, totals) | Report Layout |
| 13 | **CVEXPORT.cpy** | 25 | -- | Export/import multi-record file layout | Data Migration |
| 14 | **COSTM01.CPY** | 38 | -- | Transaction altered layout for statement reporting (keyed by card+tran-id) | Statement Tran |
| 15 | **CUSTREC.cpy** | 17 | -- | Alternate customer record layout for statement processing | Customer (alt) |
| 16 | **CSUSR01Y.cpy** | 10 | 80 bytes | User security record (user-id, password, type) | User Security |
| 17 | **COCOM01Y.cpy** | 120 | -- | Common area (COMMAREA) -- session state passed between programs | Session / Common |
| 18 | **COMEN02Y.cpy** | 40 | -- | Menu definitions (option names and program names) | Menu Config |
| 19 | **COADM02Y.cpy** | 18 | -- | Admin menu option definitions | Admin Menu Config |
| 20 | **COTTL01Y.cpy** | 8 | -- | Title/header line definitions | UI Header |
| 21 | **CSDAT01Y.cpy** | 20 | -- | Date formatting working-storage variables | Date Utility |
| 22 | **CSMSG01Y.cpy** | 5 | -- | Short message area (info/error messages) | Messaging |
| 23 | **CSMSG02Y.cpy** | 12 | -- | Long message area (extended messages) | Messaging |
| 24 | **CSSETATY.cpy** | 7 | -- | BMS attribute setting (COPY REPLACING pattern) | UI Utility |
| 25 | **CSSTRPFY.cpy** | 42 | -- | String pad/format utility paragraphs | String Utility |
| 26 | **CSUTLDPY.cpy** | 15 | -- | Date utility procedure paragraphs | Date Utility |
| 27 | **CSUTLDWY.cpy** | 18 | -- | Date utility working-storage variables | Date Utility |
| 28 | **CSLKPCDY.cpy** | 30 | -- | Lookup code tables (card status, account status) | Lookup Tables |
| 29 | **CODATECN.cpy** | 12 | -- | Date conversion record (used with ASM COBDATFT) | Date Utility |
| 30 | **UNUSED1Y.cpy** | 10 | 80 bytes | Unused/placeholder data structure | Deprecated |

### Optional Module Copybooks

#### Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | LOC | Function |
|---|----------|-----|----------|
| 1 | **CCPAURQY.cpy** | ~40 | Authorization request MQ message layout |
| 2 | **CCPAURLY.cpy** | ~40 | Authorization reply MQ message layout |
| 3 | **CCPAUERY.cpy** | ~30 | Authorization error MQ message layout |
| 4 | **CIPAUSMY.cpy** | ~25 | IMS authorization summary segment |
| 5 | **CIPAUDTY.cpy** | ~30 | IMS authorization detail segment |
| 6 | **IMSFUNCS.cpy** | ~15 | IMS function code constants (GU, GN, GNP, ISRT, etc.) |
| 7 | **PAUTBPCB.CPY** | 26 | IMS PCB mask for auth database |
| 8 | **PASFLPCB.CPY** | 26 | IMS PCB mask for summary flat file |
| 9 | **PADFLPCB.CPY** | 26 | IMS PCB mask for detail flat file |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | LOC | Function |
|---|----------|-----|----------|
| 1 | **CSDB2RWY.cpy** | 46 | DB2 common working-storage variables (SQLCODE display, DSNTIAC) |
| 2 | **CSDB2RPY.cpy** | 89 | DB2 common procedures (priming query, error formatting via DSNTIAC) |

---

## 4. BMS Maps (`app/bms/`)

| # | BMS Map | Screen | Associated Program | Function |
|---|---------|--------|--------------------|----------|
| 1 | **COSGN00.bms** | Signon | COSGN00C | Login screen (user-id, password) |
| 2 | **COMEN01.bms** | Main Menu | COMEN01C | Regular user main menu |
| 3 | **COADM01.bms** | Admin Menu | COADM01C | Admin user menu |
| 4 | **COACTVW.bms** | Account View | COACTVWC | Display account details |
| 5 | **COACTUP.bms** | Account Update | COACTUPC | Edit account fields |
| 6 | **COCRDLI.bms** | Card List | COCRDLIC | Paginated card listing |
| 7 | **COCRDSL.bms** | Card Detail | COCRDSLC | View card details |
| 8 | **COCRDUP.bms** | Card Update | COCRDUPC | Edit card fields |
| 9 | **COTRN00.bms** | Transaction List | COTRN00C | Paginated transaction listing |
| 10 | **COTRN01.bms** | Transaction View | COTRN01C | View single transaction |
| 11 | **COTRN02.bms** | Transaction Add | COTRN02C | New transaction entry form |
| 12 | **CORPT00.bms** | Report Request | CORPT00C | Select report type and parameters |
| 13 | **COBIL00.bms** | Bill Payment | COBIL00C | Pay account balance |
| 14 | **COUSR00.bms** | User List | COUSR00C | Paginated user listing |
| 15 | **COUSR01.bms** | User Add | COUSR01C | New user entry form |
| 16 | **COUSR02.bms** | User Update | COUSR02C | Edit user fields |
| 17 | **COUSR03.bms** | User Delete | COUSR03C | Confirm user deletion |

#### Optional Module BMS Maps

| # | BMS Map | Module | Associated Program | Function |
|---|---------|--------|--------------------|----------|
| 1 | **COPAU00.bms** | Auth | COPAUS0C | Authorization summary list |
| 2 | **COPAU01.bms** | Auth | COPAUS1C | Authorization detail view |
| 3 | **COTRTLI.bms** | DB2 TranType | COTRTLIC | Transaction type list |
| 4 | **COTRTUP.bms** | DB2 TranType | COTRTUPC | Transaction type add/edit |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 copybooks mirror the 17 core BMS maps, providing symbolic field definitions for COBOL SEND/RECEIVE MAP operations.

---

## 5. JCL Jobs (`app/jcl/`)

| # | JCL Job | LOC | Category | Programs Invoked | Function |
|---|---------|-----|----------|-----------------|----------|
| 1 | **DUSRSECJ.jcl** | 92 | Data Load | IEBGENER, IDCAMS | Load user security VSAM from inline data |
| 2 | **ACCTFILE.jcl** | 106 | Data Refresh | IDCAMS | Delete/define/repro account master VSAM |
| 3 | **CARDFILE.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro card master VSAM |
| 4 | **CUSTFILE.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro customer master VSAM |
| 5 | **XREFFILE.jcl** | 106 | Data Refresh | IDCAMS | Delete/define/repro card cross-reference VSAM |
| 6 | **TRANFILE.jcl** | 125 | Data Refresh | SDSF, IDCAMS | Close CICS files, refresh transaction VSAM, reopen |
| 7 | **CLOSEFIL.jcl** | 22 | Ops Control | SDSF | Close CICS files before batch processing |
| 8 | **OPENFIL.jcl** | 34 | Ops Control | SDSF | Reopen CICS files after batch processing |
| 9 | **POSTTRAN.jcl** | 45 | Batch Core | CBTRN02C | Post daily transactions to master file |
| 10 | **INTCALC.jcl** | 44 | Batch Core | CBACT04C | Calculate interest on account balances |
| 11 | **COMBTRAN.jcl** | 34 | Batch Core | IDCAMS (REPRO) | Combine daily transactions into master |
| 12 | **CREASTMT.JCL** | 97 | Batch Core | SORT, IDCAMS, CBSTM03A | Generate account statements (text + HTML) |
| 13 | **TRANBKP.jcl** | 71 | Batch Core | REPROC, IDCAMS | Backup transaction file via GDG |
| 14 | **TRANIDX.jcl** | 58 | Batch Core | IDCAMS | Define alternate index on transaction VSAM |
| 15 | **TRANREPT.jcl** | 84 | Reporting | REPROC, SORT, CBTRN03C | Generate daily transaction detail report |
| 16 | **READACCT.jcl** | 50 | Data Utility | CBACT01C | Read and dump account file |
| 17 | **READCARD.jcl** | 31 | Data Utility | CBACT02C | Read and dump card file |
| 18 | **READCUST.jcl** | 30 | Data Utility | CBCUS01C | Read and dump customer file |
| 19 | **READXREF.jcl** | 31 | Data Utility | CBACT03C | Read and dump cross-reference file |
| 20 | **TCATBALF.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro tran category balance VSAM |
| 21 | **TRANCATG.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro tran category VSAM |
| 22 | **TRANTYPE.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro transaction type VSAM |
| 23 | **DISCGRP.jcl** | 65 | Data Refresh | IDCAMS | Delete/define/repro disclosure group VSAM |
| 24 | **DALYREJS.jcl** | 65 | Data Refresh | IDCAMS | Delete/define daily rejects VSAM |
| 25 | **DEFGDGB.jcl** | 63 | Data Mgmt | IDCAMS | Define GDG base for backups |
| 26 | **DEFGDGD.jcl** | 94 | Data Mgmt | IDCAMS, IEBGENER | Define GDG + populate tran type/category/discount |
| 27 | **REPTFILE.jcl** | 32 | Data Mgmt | IDCAMS | Define report file cluster |
| 28 | **DEFCUST.jcl** | 65 | Data Mgmt | IDCAMS | Define customer VSAM cluster |
| 29 | **ESDSRRDS.jcl** | 124 | Demo/Test | IEBGENER, IDCAMS | Demo: create ESDS and RRDS VSAM from KSDS data |
| 30 | **PRTCATBL.jcl** | 66 | Reporting | REPROC, SORT | Print category balance report |
| 31 | **WAITSTEP.jcl** | 27 | Utility | COBSWAIT | Wait step (uses centisecond parameter) |
| 32 | **CBADMCDJ.jcl** | 25 | Utility | (JCL only) | Admin card demo placeholder job |
| 33 | **CBEXPORT.jcl** | ~40 | Data Migration | CBEXPORT | Export customer data for migration |
| 34 | **CBIMPORT.jcl** | ~40 | Data Migration | CBIMPORT | Import customer data from migration |
| 35 | **FTPJCL.JCL** | 42 | Connectivity | FTP | FTP file transfer job |
| 36 | **INTRDRJ1.JCL** | 19 | Utility | IDCAMS, IEBGENER | Internal reader job #1 (chain to INTRDRJ2) |
| 37 | **INTRDRJ2.JCL** | 14 | Utility | IDCAMS | Internal reader job #2 |
| 38 | **TXT2PDF1.JCL** | 41 | Utility | IKJEFT1B (TXT2PDF) | Convert text statement to PDF |

#### Optional Module JCL Jobs

| # | JCL Job | Module | Function |
|---|---------|--------|----------|
| 1 | **CBPAUP0J.jcl** | Auth | Purge expired auth messages |
| 2 | **DBPAUTP0.jcl** | Auth | Auth database processing |
| 3 | **LOADPADB.JCL** | Auth | Load auth IMS database |
| 4 | **UNLDGSAM.JCL** | Auth | Unload IMS to GSAM |
| 5 | **UNLDPADB.JCL** | Auth | Unload auth IMS database |
| 6 | **CREADB21.jcl** | DB2 TranType | Create DB2 tables for tran types |
| 7 | **MNTTRDB2.jcl** | DB2 TranType | Maintain tran type DB2 data |
| 8 | **TRANEXTR.jcl** | DB2 TranType | Extract tran types to sequential |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Function |
|---|---------|----------|
| 1 | **MVSWAIT.asm** | Wait utility (SVC WAIT) -- called by COBSWAIT |
| 2 | **COBDATFT.asm** | Date formatting utility -- called by CBACT01C |

---

## 7. Other Artifacts

| Category | Path | Items | Description |
|----------|------|-------|-------------|
| JCL Procedures | `app/proc/` | REPROC.prc, TRANREPT.prc | Reusable JCL procedures for record processing and reporting |
| CSD Definitions | `app/csd/` | CARDDEMO.CSD | CICS resource definitions (transactions, files, programs) |
| Control Files | `app/ctl/` | Various | VSAM cluster control cards |
| Sample Data (ASCII) | `app/data/ASCII/` | Multiple | Test data in ASCII format for converted applications |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | Multiple | Production-format data for mainframe upload |
| Scheduler Configs | `app/scheduler/` | CardDemo.ca7, CardDemo.controlm | CA7 and Control-M job scheduling definitions |
| Catalog Listings | `app/catlg/` | Various | Dataset catalog entries |
| Assembler Macros | `app/maclib/` | Various | Macro library for assembler programs |

---

## 8. Summary Statistics

| Metric | Count |
|--------|-------|
| **Total COBOL Programs** | 44 (31 core + 13 optional) |
| **Total LOC (COBOL)** | ~28,794 |
| **Online CICS Programs** | 22 |
| **Batch Programs** | 18 |
| **Subroutine/Utility Programs** | 4 |
| **Copybooks (all)** | 41 (30 core + 11 optional) |
| **BMS Screen Maps** | 21 (17 core + 4 optional) |
| **JCL Jobs (all)** | 46 (38 core + 8 optional) |
| **Assembler Programs** | 2 |
| **VSAM Datasets Referenced** | ~12 distinct clusters |
| **DB2 Tables (optional)** | 2+ (TRTYP, TRCAT) |
| **IMS Databases (optional)** | 1 (PAUTDB with summary/detail segments) |
| **MQ Queues (optional)** | 3+ (request, reply, error) |

---

## 9. Classification by Business Domain

| Domain | Programs | % of LOC |
|--------|----------|----------|
| **Account Management** | COACTVWC, COACTUPC, CBACT01C, CBACT04C | ~22% |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | ~15% |
| **Transaction Processing** | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C | ~19% |
| **User/Security** | COSGN00C, COUSR00C-03C | ~8% |
| **Reporting/Statements** | CORPT00C, CBSTM03A, CBSTM03B | ~8% |
| **Bill Payment** | COBIL00C | ~2% |
| **Navigation/Menu** | COMEN01C, COADM01C | ~2% |
| **Data Migration** | CBEXPORT, CBIMPORT | ~4% |
| **Utilities** | CSUTLDTC, COBSWAIT | ~1% |
| **Authorization (opt)** | COPAUA0C, COPAUS0C-2C, CBPAUP0C, DB*, PAU* | ~15% |
| **DB2 Tran Type (opt)** | COTRTLIC, COTRTUPC, COBTUPDT | ~14% |
| **MQ Services (opt)** | COACCT01, CODATE01 | ~4% |
