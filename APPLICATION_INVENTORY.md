# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo (AWS Mainframe Credit Card Management)
> **Total Assets**: 31 COBOL programs + 30 copybooks + 17 BMS maps + 17 BMS copybooks + 38 JCL jobs + 13 optional-module programs + 2 assembler programs + 2 JCL procedures

---

## 1. COBOL Programs (Core - `app/cbl/`)

### 1.1 Online CICS Programs (CO* prefix)

| # | Program | LOC | Function | BMS Map | Transaction | Business Domain |
|---|---------|-----|----------|---------|-------------|-----------------|
| 1 | COSGN00C | 260 | Signon / Authentication | COSGN00 | CC00 | Security |
| 2 | COMEN01C | 308 | Main Menu (Regular Users) | COMEN01 | CM00 | Navigation |
| 3 | COADM01C | 288 | Admin Menu | COADM01 | CA00 | Administration |
| 4 | COACTVWC | 941 | Account View | COACTVW | CA01 | Account Mgmt |
| 5 | COACTUPC | 4236 | Account Update | COACTUP | CA02 | Account Mgmt |
| 6 | COCRDLIC | 1459 | Card List | COCRDLI | CC01 | Card Mgmt |
| 7 | COCRDSLC | 887 | Card Detail View | COCRDSL | CC02 | Card Mgmt |
| 8 | COCRDUPC | 1560 | Card Update | COCRDUP | CC03 | Card Mgmt |
| 9 | COTRN00C | 699 | Transaction List | COTRN00 | CT00 | Transactions |
| 10 | COTRN01C | 330 | Transaction View | COTRN01 | CT01 | Transactions |
| 11 | COTRN02C | 783 | Transaction Add | COTRN02 | CT02 | Transactions |
| 12 | CORPT00C | 649 | Transaction Report (submits batch via TDQ) | CORPT00 | CR00 | Reporting |
| 13 | COBIL00C | 572 | Bill Payment | COBIL00 | CB00 | Billing |
| 14 | COUSR00C | 695 | User List (Admin) | COUSR00 | CU00 | User Mgmt |
| 15 | COUSR01C | 299 | User Add (Admin) | COUSR01 | CU01 | User Mgmt |
| 16 | COUSR02C | 414 | User Update (Admin) | COUSR02 | CU02 | User Mgmt |
| 17 | COUSR03C | 359 | User Delete (Admin) | COUSR03 | CU03 | User Mgmt |

### 1.2 Batch Programs (CB* prefix)

| # | Program | LOC | Function | Business Domain | Classification |
|---|---------|-----|----------|-----------------|----------------|
| 18 | CBACT01C | 430 | Read account file, write to multiple output files | Account Mgmt | Data Extract |
| 19 | CBACT02C | 178 | Read and print card data file | Card Mgmt | Data Report |
| 20 | CBACT03C | 178 | Read and print cross-reference data | Card Mgmt | Data Report |
| 21 | CBACT04C | 652 | Interest calculation on accounts | Financial Processing | Core Batch |
| 22 | CBCUS01C | 178 | Read and print customer data file | Customer Mgmt | Data Report |
| 23 | CBTRN01C | 494 | Post records from daily transaction file (variant 1) | Transactions | Core Batch |
| 24 | CBTRN02C | 731 | Post daily transactions (core posting engine) | Transactions | Core Batch |
| 25 | CBTRN03C | 649 | Print transaction detail report | Reporting | Report |
| 26 | CBSTM03A | 924 | Generate account statements (plain text + HTML) | Statements | Core Batch |
| 27 | CBSTM03B | 230 | File-processing subroutine for statement generation | Statements | Subroutine |
| 28 | CBEXPORT | 582 | Export customer data for branch migration | Data Migration | ETL |
| 29 | CBIMPORT | 487 | Import customer data from branch migration export | Data Migration | ETL |

### 1.3 Utility Programs

| # | Program | LOC | Function | Classification |
|---|---------|-----|----------|----------------|
| 30 | COBSWAIT | 41 | Wait utility (calls ASM MVSWAIT) | Utility |
| 31 | CSUTLDTC | 157 | Date conversion utility (calls CEEDAYS) | Utility |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Structure Copybooks (CV* prefix - VSAM record layouts)

| # | Copybook | Record Length | Business Entity | Used By |
|---|----------|--------------|-----------------|---------|
| 1 | CVACT01Y | 300 bytes | Account Master Record | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, COACTUPC, COACTVWC, COTRN02C, CBEXPORT, CBIMPORT |
| 2 | CVACT02Y | 150 bytes | Card Data Record | CBACT02C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT |
| 3 | CVACT03Y | ~50 bytes | Card-Account Cross-Reference | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COACTUPC, COACTVWC, COTRN02C, CBEXPORT, CBIMPORT |
| 4 | CVCUS01Y | 500 bytes | Customer Master Record | CBCUS01C, CBTRN01C, CBSTM03A, COACTUPC, COCRDSLC, COCRDUPC, COACTVWC, CBEXPORT, CBIMPORT |
| 5 | CVCRD01Y | variable | Card Detail Record (online) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 6 | CVTRA01Y | 50 bytes | Transaction Category Balance | CBACT04C, CBTRN02C |
| 7 | CVTRA02Y | 50 bytes | Disclosure Group | CBACT04C |
| 8 | CVTRA03Y | 60 bytes | Transaction Type | CBTRN03C |
| 9 | CVTRA04Y | 60 bytes | Transaction Category Type | CBTRN03C |
| 10 | CVTRA05Y | 350 bytes | Transaction Record (online) | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBEXPORT, CBIMPORT |
| 11 | CVTRA06Y | 350 bytes | Daily Transaction Record | CBTRN01C, CBTRN02C |
| 12 | CVTRA07Y | variable | Transaction Report Layout | CBTRN03C |
| 13 | CVEXPORT | variable | Export File Record Layout | CBEXPORT, CBIMPORT |

### 2.2 Common/Shared Copybooks (CO*, CS*, CU* prefix)

| # | Copybook | Function | Used By |
|---|----------|----------|---------|
| 14 | COCOM01Y | Common communication area (COMMAREA) | All online programs |
| 15 | COMEN02Y | Menu item definitions | COMEN01C |
| 16 | COADM02Y | Admin menu definitions | COADM01C |
| 17 | COTTL01Y | Title/header line definitions | Most online programs |
| 18 | CSDAT01Y | Date formatting data structures | Most online programs |
| 19 | CSMSG01Y | Message area definitions | Most online programs |
| 20 | CSMSG02Y | Extended message definitions | COACTUPC, COCRDSLC, COCRDUPC |
| 21 | CSUSR01Y | User security record (80 bytes) | COSGN00C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00-03C |
| 22 | CSUTLDPY | Date utility parameter layout | COACTUPC |
| 23 | CSUTLDWY | Working storage for utility | COACTUPC |
| 24 | CSLKPCDY | Lookup code data | COACTUPC |
| 25 | CSSETATY | Attribute-setting copybook (COPY REPLACING) | COACTUPC |
| 26 | CSSTRPFY | String processing functions | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 27 | CODATECN | Date conversion record | CBACT01C |
| 28 | CUSTREC | Customer record (alternate layout) | CBSTM03A |
| 29 | COSTM01 | Transaction altered layout for reporting | CBSTM03A |
| 30 | UNUSED1Y | Unused placeholder record (80 bytes) | None |

---

## 3. BMS Maps (`app/bms/`) and BMS Copybooks (`app/cpy-bms/`)

| # | BMS Map | BMS Copybook | Screen Name | Fields | LOC | Associated Program |
|---|---------|-------------|-------------|--------|-----|--------------------|
| 1 | COSGN00.bms | COSGN00.CPY | Login Screen | 37 | 210 | COSGN00C |
| 2 | COMEN01.bms | COMEN01.CPY | Main Menu | 28 | 167 | COMEN01C |
| 3 | COADM01.bms | COADM01.CPY | Admin Menu | 28 | 167 | COADM01C |
| 4 | COACTVW.bms | COACTVW.CPY | Account View | 100 | 378 | COACTVWC |
| 5 | COACTUP.bms | COACTUP.CPY | Account Update | 128 | 512 | COACTUPC |
| 6 | COCRDLI.bms | COCRDLI.CPY | Card List | 72 | 344 | COCRDLIC |
| 7 | COCRDSL.bms | COCRDSL.CPY | Card Detail View | 31 | 157 | COCRDSLC |
| 8 | COCRDUP.bms | COCRDUP.CPY | Card Update | 34 | 172 | COCRDUPC |
| 9 | COTRN00.bms | COTRN00.CPY | Transaction List | 89 | 464 | COTRN00C |
| 10 | COTRN01.bms | COTRN01.CPY | Transaction View | 56 | 273 | COTRN01C |
| 11 | COTRN02.bms | COTRN02.CPY | Transaction Add | 61 | 307 | COTRN02C |
| 12 | CORPT00.bms | CORPT00.CPY | Transaction Report | 42 | 231 | CORPT00C |
| 13 | COBIL00.bms | COBIL00.CPY | Bill Payment | 24 | 141 | COBIL00C |
| 14 | COUSR00.bms | COUSR00.CPY | User List | 89 | 463 | COUSR00C |
| 15 | COUSR01.bms | COUSR01.CPY | User Add | 28 | 164 | COUSR01C |
| 16 | COUSR02.bms | COUSR02.CPY | User Update | 29 | 169 | COUSR02C |
| 17 | COUSR03.bms | COUSR03.CPY | User Delete | 26 | 153 | COUSR03C |

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data Refresh / File Management Jobs

| # | JCL Job | Function | Programs Executed | Key Datasets |
|---|---------|----------|-------------------|-------------|
| 1 | ACCTFILE.jcl | Delete/define/reload account VSAM | IDCAMS | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE.jcl | Delete/define/reload card VSAM | IDCAMS, SDSF | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE.jcl | Delete/define/reload customer VSAM | IDCAMS, SDSF | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE.jcl | Delete/define/reload cross-reference VSAM | IDCAMS | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE.jcl | Load transaction master from daily PS | IDCAMS, SDSF | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ.jcl | Define/load user security VSAM | IEBGENER, IDCAMS | USRSEC.VSAM.KSDS |
| 7 | DEFCUST.jcl | Define customer data VSAM cluster | IDCAMS | CUSTDATA |
| 8 | REPTFILE.jcl | Define transaction report GDG | IDCAMS | TRANREPT GDG |

### 4.2 Batch Processing Cycle Jobs

| # | JCL Job | Function | Programs Executed | Key Datasets |
|---|---------|----------|-------------------|-------------|
| 9 | CLOSEFIL.jcl | Close CICS files for batch | SDSF | - |
| 10 | OPENFIL.jcl | Re-open CICS files after batch | SDSF | - |
| 11 | POSTTRAN.jcl | Core transaction posting cycle | SORT, CBTRN02C | DALYTRAN, TRANSACT, ACCTDATA, TCATBALF |
| 12 | INTCALC.jcl | Interest calculation cycle | CBACT04C | TCATBALF, ACCTDATA, DISCGRP |
| 13 | TRANBKP.jcl | Backup transaction master | SORT, IDCAMS | TRANSACT.BKUP GDG |
| 14 | COMBTRAN.jcl | Combine backup + system transactions | SORT, IDCAMS | TRANSACT.COMBINED GDG |
| 15 | CREASTMT.JCL | Generate account statements | SORT, IDCAMS, CBSTM03A | STATEMNT.PS, STATEMNT.HTML |
| 16 | TRANREPT.jcl | Generate transaction detail report | SORT, CBTRN03C (via TRANREPT.prc) | TRANREPT GDG |
| 17 | TRANIDX.jcl | Define alternate index on transactions | IDCAMS | TRANSACT.VSAM.KSDS |

### 4.3 VSAM Cluster / GDG Definition Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 18 | DEFGDGB.jcl | Define GDG bases for backups | IDCAMS |
| 19 | DEFGDGD.jcl | Define GDG bases for DB2-related datasets | IDCAMS, IEBGENER |
| 20 | DALYREJS.jcl | Define GDG for daily rejects | IDCAMS |
| 21 | DISCGRP.jcl | Define disclosure group VSAM file | IDCAMS |
| 22 | TRANTYPE.jcl | Define transaction type VSAM file | IDCAMS |
| 23 | TRANCATG.jcl | Define transaction category VSAM file | IDCAMS |
| 24 | TCATBALF.jcl | Define transaction category balance VSAM | IDCAMS |
| 25 | ESDSRRDS.jcl | Define ESDS/RRDS experimental files | IDCAMS |

### 4.4 Data Utility / Read Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 26 | READACCT.jcl | Read/dump account data | CBACT01C |
| 27 | READCARD.jcl | Read/dump card data | CBACT02C |
| 28 | READCUST.jcl | Read/dump customer data | CBCUS01C |
| 29 | READXREF.jcl | Read/dump cross-reference data | CBACT03C |
| 30 | PRTCATBL.jcl | Print catalog / balance data | IDCAMS |
| 31 | CBEXPORT.jcl | Export all customer data for migration | CBEXPORT |
| 32 | CBIMPORT.jcl | Import migration data | CBIMPORT |

### 4.5 CSD / Infrastructure Jobs

| # | JCL Job | Function | Programs Executed |
|---|---------|----------|-------------------|
| 33 | CBADMCDJ.jcl | Install CICS CSD resource definitions | DFHCSDUP |
| 34 | WAITSTEP.jcl | Wait step utility | COBSWAIT |
| 35 | FTPJCL.JCL | FTP transfer utility | FTP |
| 36 | INTRDRJ1.JCL | Internal reader chain (triggers INTRDRJ2) | IDCAMS, IEBGENER |
| 37 | INTRDRJ2.JCL | Internal reader target job | IDCAMS |
| 38 | TXT2PDF1.JCL | Convert text statement to PDF | IKJEFT1B |

---

## 5. Optional Extension Modules

### 5.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for real-time card authorization.

| # | Program | LOC | Function | Type |
|---|---------|-----|----------|------|
| 1 | COPAUA0C | 1026 | Card Authorization Decision (MQ trigger) | CICS/IMS/MQ |
| 2 | COPAUS0C | 1032 | Authorization Summary View | CICS/IMS/BMS |
| 3 | COPAUS1C | 604 | Authorization Detail View | CICS/IMS/BMS |
| 4 | COPAUS2C | 244 | Mark Authorization as Fraud (DB2) | CICS/IMS/DB2 |
| 5 | CBPAUP0C | 386 | Purge Expired Authorizations (batch) | Batch/IMS |
| 6 | DBUNLDGS | 366 | IMS Database Unload | Batch/IMS |
| 7 | PAUDBLOD | 369 | IMS Database Load | Batch/IMS |
| 8 | PAUDBUNL | 317 | IMS Database Unload (alternate) | Batch/IMS |

### 5.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-backed CRUD for transaction type management with cursor-based paging.

| # | Program | LOC | Function | Type |
|---|---------|-----|----------|------|
| 9 | COTRTLIC | 2098 | List/Delete Transaction Types (DB2 cursors) | CICS/DB2 |
| 10 | COTRTUPC | 1702 | Add/Edit Transaction Type (DB2) | CICS/DB2 |
| 11 | COBTUPDT | 237 | Batch Update Transaction Types (DB2) | Batch/DB2 |

### 5.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response for account inquiry and date services.

| # | Program | LOC | Function | Type |
|---|---------|-----|----------|------|
| 12 | COACCT01 | 620 | Account Inquiry via MQ | CICS/MQ |
| 13 | CODATE01 | 524 | System Date Service via MQ | CICS/MQ |

---

## 6. Supporting Assets

### 6.1 Assembler Programs (`app/asm/`)

| Program | Function |
|---------|----------|
| MVSWAIT.asm | Low-level wait/sleep routine (called by COBSWAIT) |
| COBDATFT.asm | Date formatting assembler routine (called by CBACT01C) |

### 6.2 JCL Procedures (`app/proc/`)

| Procedure | Function |
|-----------|----------|
| REPROC.prc | Reprocessing procedure |
| TRANREPT.prc | Transaction report procedure (used by TRANREPT.jcl) |

### 6.3 Other Supporting Files

| Directory | Contents |
|-----------|----------|
| `app/csd/` | CARDDEMO.CSD - CICS System Definition file |
| `app/ctl/` | REPROCT.ctl - Reprocessing control file |
| `app/maclib/` | ASMWAIT.mac, COCDATFT.mac - Assembler macros |
| `app/data/ASCII/` | Sample data files in ASCII format |
| `app/data/EBCDIC/` | Sample data files in EBCDIC format |
| `app/catlg/` | LISTCAT.txt - VSAM catalog listing |
| `app/scheduler/` | CardDemo.ca7, CardDemo.controlm - Job scheduler configs |

---

## 7. Summary Statistics

| Category | Count |
|----------|-------|
| Core COBOL Programs | 31 |
| Optional Module Programs | 13 |
| Copybooks (data) | 30 |
| BMS Maps | 17 |
| BMS Copybooks | 17 |
| JCL Jobs | 38 |
| JCL Procedures | 2 |
| Assembler Programs | 2 |
| **Total Source Assets** | **150** |

### LOC Distribution

| Category | Total LOC | Avg LOC |
|----------|-----------|---------|
| Online CICS Programs (17) | 13,429 | 790 |
| Batch Programs (12) | 5,713 | 476 |
| Utility Programs (2) | 198 | 99 |
| Optional Module Programs (13) | 9,525 | 733 |
| **All COBOL** | **28,865** | ~**658** |

### Business Domain Classification

| Domain | Online Programs | Batch Programs | JCL Jobs |
|--------|----------------|----------------|----------|
| Account Management | 2 | 2 | 2 |
| Card Management | 3 | 2 | 2 |
| Transaction Processing | 3 | 4 | 6 |
| Billing / Payments | 1 | 0 | 0 |
| Reporting / Statements | 1 | 3 | 3 |
| User / Security Management | 5 | 0 | 1 |
| Navigation / Menus | 2 | 0 | 0 |
| Data Migration (ETL) | 0 | 2 | 2 |
| Infrastructure / Utility | 0 | 1 | 20 |
