# CardDemo Application Inventory

> **Generated from:** `uc-legacy-modernization-cobol-to-java` codebase analysis
> **Total Artifact Count:** 31 COBOL programs + 30 copybooks + 38 JCL jobs + 17 BMS maps + 17 BMS copybooks + 2 ASM modules + 13 optional-module programs = **148 artifacts**

---

## 1. COBOL Programs (`app/cbl/`) &mdash; 31 Programs

### 1.1 Online CICS Programs (17)

These programs run under CICS and provide the interactive 3270 terminal UI.

| # | Program | Lines | CICS Tran | Function | Business Domain | BMS Map |
|---|---------|------:|-----------|----------|-----------------|---------|
| 1 | **COSGN00C** | 260 | CC00 | Sign-on / Authentication | Security | COSGN00 |
| 2 | **COMEN01C** | 308 | CM00 | Main Menu (regular users) | Navigation | COMEN01 |
| 3 | **COADM01C** | 288 | CA00 | Admin Menu | Navigation / Admin | COADM01 |
| 4 | **COACTVWC** | 941 | CA01 | Account View (read-only) | Account Mgmt | COACTVW |
| 5 | **COACTUPC** | 4,236 | CA02 | Account Update | Account Mgmt | COACTUP |
| 6 | **COCRDLIC** | 1,459 | CC01 | Credit Card List (browse) | Card Mgmt | COCRDLI |
| 7 | **COCRDSLC** | 887 | CC02 | Credit Card View (detail) | Card Mgmt | COCRDSL |
| 8 | **COCRDUPC** | 1,560 | CC03 | Credit Card Update | Card Mgmt | COCRDUP |
| 9 | **COTRN00C** | 699 | CT00 | Transaction List (browse) | Transactions | COTRN00 |
| 10 | **COTRN01C** | 330 | CT01 | Transaction View (detail) | Transactions | COTRN01 |
| 11 | **COTRN02C** | 783 | CT02 | Transaction Add (new entry) | Transactions | COTRN02 |
| 12 | **CORPT00C** | 649 | CR00 | Transaction Reports (online) | Reporting | CORPT00 |
| 13 | **COBIL00C** | 572 | CB00 | Bill Payment | Billing | COBIL00 |
| 14 | **COUSR00C** | 695 | CU00 | User List (admin) | Security / Admin | COUSR00 |
| 15 | **COUSR01C** | 299 | CU01 | User Add (admin) | Security / Admin | COUSR01 |
| 16 | **COUSR02C** | 414 | CU02 | User Update (admin) | Security / Admin | COUSR02 |
| 17 | **COUSR03C** | 359 | CU03 | User Delete (admin) | Security / Admin | COUSR03 |

### 1.2 Batch Programs (13)

These programs run in batch JCL jobs for overnight / scheduled processing.

| # | Program | Lines | Function | Business Domain |
|---|---------|------:|----------|-----------------|
| 1 | **CBACT01C** | 430 | Account file read/update utility | Account Mgmt |
| 2 | **CBACT02C** | 178 | Card data file read utility | Card Mgmt |
| 3 | **CBACT03C** | 178 | Card cross-reference file read utility | Card Mgmt |
| 4 | **CBACT04C** | 652 | Interest calculation on accounts | Interest / Finance |
| 5 | **CBCUS01C** | 178 | Customer data file read utility | Customer Mgmt |
| 6 | **CBTRN01C** | 494 | Daily transaction file read/validation | Transactions |
| 7 | **CBTRN02C** | 731 | Transaction posting to master file | Transactions |
| 8 | **CBTRN03C** | 649 | Daily transaction report generation | Reporting |
| 9 | **CBSTM03A** | 924 | Statement generation (text + HTML) | Statements |
| 10 | **CBSTM03B** | 230 | Statement file I/O subroutine | Statements |
| 11 | **CBEXPORT** | 582 | Data export (all VSAM to flat) | Data Migration |
| 12 | **CBIMPORT** | 487 | Data import (flat to VSAM) | Data Migration |
| 13 | **COBSWAIT** | 41 | Wait/delay utility (calls MVSWAIT) | Utility |

### 1.3 Shared Utility (1)

| # | Program | Lines | Function | Business Domain |
|---|---------|------:|----------|-----------------|
| 1 | **CSUTLDTC** | 157 | Date validation (calls CEEDAYS) | Utility |

---

## 2. Copybooks (`app/cpy/`) &mdash; 30 Copybooks

### 2.1 Data Record Layouts (CV-prefix, 14 files)

| # | Copybook | Record Length | Business Entity | Description |
|---|----------|-------------:|-----------------|-------------|
| 1 | **CVACT01Y** | 300 bytes | Account Master | Account ID, status, balances, limits, dates |
| 2 | **CVACT02Y** | 150 bytes | Card Data | Card number, CVV, expiry, embossed name, status |
| 3 | **CVACT03Y** | 50 bytes | Card Cross-Reference | Card-to-account mapping |
| 4 | **CVCRD01Y** | varies | Card Detail (online) | Internal card processing structure |
| 5 | **CVCUS01Y** | 500 bytes | Customer | Name, address, SSN, phone, dates |
| 6 | **CVTRA01Y** | 50 bytes | Transaction Category Balance | Category-level running totals |
| 7 | **CVTRA02Y** | 50 bytes | Disclosure Group | Interest rate by group/type |
| 8 | **CVTRA03Y** | 60 bytes | Transaction Type | Type code + description |
| 9 | **CVTRA04Y** | 60 bytes | Transaction Category | Category code + description |
| 10 | **CVTRA05Y** | 350 bytes | Transaction Record | Full transaction detail |
| 11 | **CVTRA06Y** | 350 bytes | Daily Transaction | Daily transaction input record |
| 12 | **CVTRA07Y** | varies | Transaction Report Layout | Print layout for daily report |
| 13 | **CVEXPORT** | varies | Export Record | Combined export data structure |
| 14 | **CUSTREC** | varies | Customer Record (alt) | Alternate customer layout |

### 2.2 Communication / Common Areas (CO/CS-prefix, 12 files)

| # | Copybook | Purpose |
|---|----------|---------|
| 1 | **COCOM01Y** | Common communication area (COMMAREA) between CICS programs |
| 2 | **COMEN02Y** | Main menu option definitions (11 options, program names) |
| 3 | **COADM02Y** | Admin menu option definitions (6 options, program names) |
| 4 | **COTTL01Y** | Screen title/header definitions |
| 5 | **CSMSG01Y** | Standard message definitions |
| 6 | **CSMSG02Y** | Extended message definitions |
| 7 | **CSDAT01Y** | Date formatting work areas |
| 8 | **CSUSR01Y** | User security record (ID, password, name, type) |
| 9 | **CSLKPCDY** | Lookup code definitions |
| 10 | **CSSETATY** | Screen field attribute setting (COPY REPLACING pattern) |
| 11 | **CSSTRPFY** | String processing utility functions |
| 12 | **CSUTLDPY** | Date utility parameter block |

### 2.3 Other Copybooks (4 files)

| # | Copybook | Purpose |
|---|----------|---------|
| 1 | **CSUTLDWY** | Date utility working storage |
| 2 | **CODATECN** | Date conversion constants |
| 3 | **COSTM01** | Transaction altered layout for statement reporting (keyed by card+tran ID) |
| 4 | **UNUSED1Y** | Deprecated/unused data structure |

---

## 3. BMS Screen Maps (`app/bms/`) &mdash; 17 Maps

| # | BMS Map | Mapset | Associated Program | Screen Title |
|---|---------|--------|--------------------|-------------|
| 1 | **COSGN00** | COSGN00 | COSGN00C | Sign On |
| 2 | **COMEN01** | COMEN01 | COMEN01C | Main Menu |
| 3 | **COADM01** | COADM01 | COADM01C | Admin Menu |
| 4 | **COACTVW** | COACTVW | COACTVWC | Account View |
| 5 | **COACTUP** | COACTUP | COACTUPC | Account Update |
| 6 | **COCRDLI** | COCRDLI | COCRDLIC | Credit Card List |
| 7 | **COCRDSL** | COCRDSL | COCRDSLC | Credit Card Detail |
| 8 | **COCRDUP** | COCRDUP | COCRDUPC | Credit Card Update |
| 9 | **COTRN00** | COTRN00 | COTRN00C | Transaction List |
| 10 | **COTRN01** | COTRN01 | COTRN01C | Transaction View |
| 11 | **COTRN02** | COTRN02 | COTRN02C | Transaction Add |
| 12 | **CORPT00** | CORPT00 | CORPT00C | Transaction Reports |
| 13 | **COBIL00** | COBIL00 | COBIL00C | Bill Payment |
| 14 | **COUSR00** | COUSR00 | COUSR00C | User List |
| 15 | **COUSR01** | COUSR01 | COUSR01C | User Add |
| 16 | **COUSR02** | COUSR02 | COUSR02C | User Update |
| 17 | **COUSR03** | COUSR03 | COUSR03C | User Delete |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 corresponding `.CPY` files, one per BMS map, auto-generated symbolic field maps used in COBOL programs.

---

## 4. JCL Batch Jobs (`app/jcl/`) &mdash; 38 Jobs

### 4.1 Data Refresh / File Load (9 jobs)

| # | JCL Job | Function | Target VSAM File |
|---|---------|----------|------------------|
| 1 | **ACCTFILE** | Define & load account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | Define & load card data VSAM | CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | Define & load customer VSAM | CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | Define & load card cross-reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | Load daily transaction flat file to VSAM | TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ** | Define & load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | **TRANTYPE** | Define & load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 8 | **TRANCATG** | Define & load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 9 | **DISCGRP** | Define & load disclosure group VSAM | DISCGRP.VSAM.KSDS |

### 4.2 Core Batch Processing (6 jobs)

| # | JCL Job | Program Executed | Function |
|---|---------|-----------------|----------|
| 1 | **POSTTRAN** | CBTRN02C | Post daily transactions to master |
| 2 | **INTCALC** | CBACT04C | Calculate interest on accounts |
| 3 | **TRANBKP** | SORT | Backup transaction master file |
| 4 | **COMBTRAN** | SORT + IDCAMS | Combine backup + system transactions |
| 5 | **CREASTMT** | CBSTM03A | Generate account statements (text + HTML) |
| 6 | **TRANREPT** | CBTRN03C | Generate daily transaction report |

### 4.3 CICS File Control (2 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 1 | **CLOSEFIL** | Close CICS files for batch processing |
| 2 | **OPENFIL** | Open CICS files after batch completes |

### 4.4 Data Export / Import (2 jobs)

| # | JCL Job | Program Executed | Function |
|---|---------|-----------------|----------|
| 1 | **CBEXPORT** | CBEXPORT | Export all VSAM data to flat file |
| 2 | **CBIMPORT** | CBIMPORT | Import flat file back to VSAM files |

### 4.5 VSAM Definition / GDG (6 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 1 | **DEFGDGB** | Define GDG bases for backups |
| 2 | **DEFGDGD** | Define GDG bases + backup reference data |
| 3 | **DEFCUST** | Define customer VSAM cluster |
| 4 | **TCATBALF** | Define & load transaction category balance VSAM |
| 5 | **TRANIDX** | Define alternate indexes on transaction files |
| 6 | **ESDSRRDS** | Define ESDS/RRDS VSAM (demo/testing) |

### 4.6 Reporting / Print (4 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 1 | **DALYREJS** | Process daily rejected transactions |
| 2 | **REPTFILE** | Define report output files |
| 3 | **PRTCATBL** | Print transaction category balance report |
| 4 | **TXT2PDF1** | Convert text statements to PDF |

### 4.7 Utility / Miscellaneous (5 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 1 | **READACCT** | Read/dump account data (diagnostic) |
| 2 | **READCARD** | Read/dump card data (diagnostic) |
| 3 | **READCUST** | Read/dump customer data (diagnostic) |
| 4 | **READXREF** | Read/dump cross-reference data (diagnostic) |
| 5 | **WAITSTEP** | Wait/delay step (uses COBSWAIT) |

### 4.8 Administrative / System (4 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 1 | **CBADMCDJ** | CICS CSD batch admin (DFHCSDUP) |
| 2 | **FTPJCL** | FTP file transfer to/from mainframe |
| 3 | **INTRDRJ1** | Internal reader job 1 (triggers INTRDRJ2) |
| 4 | **INTRDRJ2** | Internal reader job 2 (chained from INTRDRJ1) |

---

## 5. Assembler Modules (`app/asm/`) &mdash; 2 Modules

| # | Module | Function |
|---|--------|----------|
| 1 | **MVSWAIT** | Wait/delay ASM routine (called by COBSWAIT) |
| 2 | **COBDATFT** | Date formatting ASM routine (called by CBACT01C) |

---

## 6. Optional Extension Modules

### 6.1 Authorization (IMS/DB2/MQ) &mdash; `app/app-authorization-ims-db2-mq/`

| Type | Artifact | Function |
|------|----------|----------|
| COBOL | **COPAUA0C** | MQ trigger monitor for authorization requests |
| COBOL | **COPAUS0C** | Authorization summary view (online) |
| COBOL | **COPAUS1C** | Authorization detail view (online) |
| COBOL | **COPAUS2C** | Fraud marking to DB2 (online) |
| COBOL | **CBPAUP0C** | Batch purge of old authorizations |
| COBOL | **PAUDBLOD** | Load authorization IMS database |
| COBOL | **PAUDBUNL** | Unload authorization IMS database |
| COBOL | **DBUNLDGS** | Unload GSAM segments |
| BMS | **COPAU00** / **COPAU01** | Authorization screens |
| JCL | CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB | Batch jobs |
| Copybook | 7 copybooks | IMS PCBs, MQ structures, DB2 declarations |

### 6.2 Transaction Type DB2 &mdash; `app/app-transaction-type-db2/`

| Type | Artifact | Function |
|------|----------|----------|
| COBOL | **COTRTLIC** | Transaction type list/delete (online, DB2 cursors) |
| COBOL | **COTRTUPC** | Transaction type add/edit (online, embedded SQL) |
| COBOL | **COBTUPDT** | Batch update of transaction types |
| BMS | **COTRTLI** / **COTRTUP** | Transaction type screens |
| JCL | CREADB21, MNTTRDB2, TRANEXTR | DB2 DDL, maintenance, extraction |
| Copybook | CSDB2RPY, CSDB2RWY | DB2 SQLCA / result set areas |

### 6.3 VSAM-MQ &mdash; `app/app-vsam-mq/`

| Type | Artifact | Function |
|------|----------|----------|
| COBOL | **CODATE01** | MQ request/response for system date |
| COBOL | **COACCT01** | MQ request/response for account inquiry |

---

## 7. Classification Summary

| Category | Count | Key Technologies |
|----------|------:|------------------|
| Online CICS Programs | 17 | CICS, BMS, VSAM KSDS |
| Batch COBOL Programs | 13 | Sequential I/O, VSAM, SORT |
| Utility Programs | 1 | LE callable services (CEEDAYS) |
| Copybooks (data layouts) | 14 | PIC clauses, record structures |
| Copybooks (common/utility) | 16 | COMMunication areas, messages, attributes |
| BMS Screen Maps | 17 | 3270 terminal UI, DFHMSD/DFHMDI/DFHMDF |
| BMS-Generated Copybooks | 17 | Auto-generated symbolic maps |
| JCL Jobs | 38 | IDCAMS, SORT, IEBGENER, IEFBR14 |
| Assembler Modules | 2 | Date formatting, wait routine |
| Optional Module Programs | 13 | IMS DB, DB2 SQL, MQ messaging |
| **Total** | **148** | |

### Business Domain Distribution

| Domain | Programs | Percentage |
|--------|----------|------------|
| Account Management | 4 | 13% |
| Card Management | 6 | 19% |
| Transaction Processing | 8 | 26% |
| Security / User Admin | 5 | 16% |
| Billing / Statements | 3 | 10% |
| Reporting | 3 | 10% |
| Utilities / Infrastructure | 2 | 6% |
