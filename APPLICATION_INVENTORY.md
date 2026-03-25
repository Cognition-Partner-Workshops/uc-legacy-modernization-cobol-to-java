# Application Inventory - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Total Artifacts:** 31 COBOL programs + 13 optional-module programs + 30 copybooks + 17 BMS maps + 38 JCL jobs + 2 ASM programs + 2 JCL procedures

---

## 1. COBOL Programs (Core Application)

### 1.1 Online CICS Programs (19 programs)

| # | Program ID | File | Lines | CICS Trans | Function | Business Domain | Classification |
|---|-----------|------|-------|------------|----------|----------------|----------------|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | 260 | CC00 | User sign-on / authentication | Security | Entry Point |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | 308 | CM00 | Main menu for regular users | Navigation | Controller |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | 288 | CA00 | Admin menu for admin users | Navigation | Controller |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | 941 | CAVW | Account view (read-only display) | Account Mgmt | Read |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | 4,236 | CAUP | Account update (edit account + customer) | Account Mgmt | CRUD |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | 1,459 | CCLI | List credit cards (paginated) | Card Mgmt | Read/List |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | 887 | CCDL | View credit card details | Card Mgmt | Read |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | 1,560 | CCUP | Update credit card details | Card Mgmt | CRUD |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | 699 | CT00 | List transactions (paginated) | Transaction Mgmt | Read/List |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | 330 | CT01 | View transaction details | Transaction Mgmt | Read |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | 783 | CT02 | Add a new transaction | Transaction Mgmt | Create |
| 12 | CORPT00C | `app/cbl/CORPT00C.cbl` | 649 | CR00 | Submit batch transaction reports via TDQ | Reporting | Trigger |
| 13 | COBIL00C | `app/cbl/COBIL00C.cbl` | 572 | CB00 | Bill payment (pay balance in full) | Billing | CRUD |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | 695 | CU00 | List users from USRSEC file | User Admin | Read/List |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | 299 | CU01 | Add a new user | User Admin | Create |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | 414 | CU02 | Update a user | User Admin | Update |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | 359 | CU03 | Delete a user | User Admin | Delete |
| 18 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | 157 | -- | Date validation utility (calls CEEDAYS) | Utility | Shared |
| 19 | COBSWAIT | `app/cbl/COBSWAIT.cbl` | 41 | -- | Wait utility (parm in centiseconds) | Utility | Shared |

### 1.2 Batch Programs (12 programs)

| # | Program ID | File | Lines | Function | Business Domain | Classification |
|---|-----------|------|-------|----------|----------------|----------------|
| 1 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | 494 | Post daily transactions to master | Transaction Processing | Core Batch |
| 2 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | 731 | Post daily transactions (enhanced) with rejects | Transaction Processing | Core Batch |
| 3 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | 649 | Print transaction detail report | Reporting | Report |
| 4 | CBACT01C | `app/cbl/CBACT01C.cbl` | 430 | Read account file and write output | Account Mgmt | Data Extract |
| 5 | CBACT02C | `app/cbl/CBACT02C.cbl` | 178 | Read and print card data file | Card Mgmt | Data Extract |
| 6 | CBACT03C | `app/cbl/CBACT03C.cbl` | 178 | Read and print cross-reference data | Card Mgmt | Data Extract |
| 7 | CBACT04C | `app/cbl/CBACT04C.cbl` | 652 | Interest calculation | Financial Calc | Core Batch |
| 8 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | 178 | Read and print customer data file | Customer Mgmt | Data Extract |
| 9 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | 924 | Generate account statements (text + HTML) | Statements | Core Batch |
| 10 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | 230 | Subroutine: file I/O for statement report | Statements | Subroutine |
| 11 | CBEXPORT | `app/cbl/CBEXPORT.cbl` | 582 | Export customer data for branch migration | Data Migration | ETL |
| 12 | CBIMPORT | `app/cbl/CBIMPORT.cbl` | 487 | Import customer data from branch migration | Data Migration | ETL |

---

## 2. Optional Module Programs (13 programs)

> **Note:** These programs reside in separate subdirectories (`app/app-authorization-ims-db2-mq/`, `app/app-transaction-type-db2/`, `app/app-vsam-mq/`) outside the main `app/cbl/` directory. They require IMS, DB2, or MQ middleware and are not part of the core CardDemo deployment.

### 2.1 Authorization Module (IMS/DB2/MQ) - 8 programs

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 1 | COPAUA0C | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | 1,026 | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| 2 | COPAUS0C | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | 1,032 | CICS/IMS/BMS | Summary view of authorization messages |
| 3 | COPAUS1C | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | 604 | CICS/IMS/BMS | Detail view of authorization message |
| 4 | COPAUS2C | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | 244 | CICS/IMS/DB2 | Mark authorization message as fraud |
| 5 | CBPAUP0C | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | 386 | Batch/IMS | Delete expired pending auth messages |
| 6 | DBUNLDGS | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | 366 | Batch | Unload GSAM database |
| 7 | PAUDBLOD | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | 369 | Batch/IMS | Load authorization database |
| 8 | PAUDBUNL | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | 317 | Batch/IMS | Unload authorization database |

### 2.2 Transaction Type DB2 Module - 3 programs

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 1 | COTRTLIC | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | 2,098 | CICS/DB2 | List transaction types (cursor paging) |
| 2 | COTRTUPC | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | 1,702 | CICS/DB2 | Add/edit transaction types |
| 3 | COBTUPDT | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` | 237 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module - 2 programs

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 1 | COACCT01 | `app/app-vsam-mq/cbl/COACCT01.cbl` | 620 | MQ/VSAM | MQ request/response for account inquiry |
| 2 | CODATE01 | `app/app-vsam-mq/cbl/CODATE01.cbl` | 524 | MQ/VSAM | MQ request/response for system date |

---

## 3. Copybooks (30 files)

### 3.1 Business Data Layouts

| # | Copybook | File | Description | Record Size | Key Entity |
|---|----------|------|-------------|-------------|------------|
| 1 | CVACT01Y | `app/cpy/CVACT01Y.cpy` | Account master record | 300 bytes | Account |
| 2 | CVACT02Y | `app/cpy/CVACT02Y.cpy` | Card data record | 150 bytes | Card |
| 3 | CVACT03Y | `app/cpy/CVACT03Y.cpy` | Card-account cross-reference | 50 bytes | Card-Xref |
| 4 | CVCUS01Y | `app/cpy/CVCUS01Y.cpy` | Customer data record | 500 bytes | Customer |
| 5 | CVCRD01Y | `app/cpy/CVCRD01Y.cpy` | Card working-storage area | Variable | Card (WS) |
| 6 | CVTRA01Y | `app/cpy/CVTRA01Y.cpy` | Transaction category balance | 50 bytes | Tran Cat Balance |
| 7 | CVTRA02Y | `app/cpy/CVTRA02Y.cpy` | Disclosure group record | 50 bytes | Disclosure Group |
| 8 | CVTRA03Y | `app/cpy/CVTRA03Y.cpy` | Transaction type record | 60 bytes | Tran Type |
| 9 | CVTRA04Y | `app/cpy/CVTRA04Y.cpy` | Transaction category type | 60 bytes | Tran Category |
| 10 | CVTRA05Y | `app/cpy/CVTRA05Y.cpy` | Transaction record (online) | 350 bytes | Transaction |
| 11 | CVTRA06Y | `app/cpy/CVTRA06Y.cpy` | Daily transaction record | 350 bytes | Daily Transaction |
| 12 | CVTRA07Y | `app/cpy/CVTRA07Y.cpy` | Transaction report headers/totals | Variable | Report Layout |
| 13 | CVEXPORT | `app/cpy/CVEXPORT.cpy` | Export record layout | Variable | Export Data |
| 14 | COSTM01 | `app/cpy/COSTM01.CPY` | Statement transaction layout (rekey) | 350 bytes | Statement Tran |
| 15 | CUSTREC | `app/cpy/CUSTREC.cpy` | Customer record (alternate) | Variable | Customer |
| 16 | UNUSED1Y | `app/cpy/UNUSED1Y.cpy` | Unused/legacy data structure | 80 bytes | Deprecated |

### 3.2 Application Infrastructure Copybooks

| # | Copybook | File | Description | Purpose |
|---|----------|------|-------------|---------|
| 17 | COCOM01Y | `app/cpy/COCOM01Y.cpy` | Common communication area (COMMAREA) | Screen-to-screen data passing |
| 18 | COMEN02Y | `app/cpy/COMEN02Y.cpy` | Menu definitions (options table) | Navigation config |
| 19 | COADM02Y | `app/cpy/COADM02Y.cpy` | Admin menu definitions | Admin navigation config |
| 20 | COTTL01Y | `app/cpy/COTTL01Y.cpy` | Title/header constants | Screen branding |
| 21 | CSUSR01Y | `app/cpy/CSUSR01Y.cpy` | User security record layout | Authentication |
| 22 | CSDAT01Y | `app/cpy/CSDAT01Y.cpy` | Current date/time variables | Date handling |
| 23 | CSMSG01Y | `app/cpy/CSMSG01Y.cpy` | Common message definitions | User messages |
| 24 | CSMSG02Y | `app/cpy/CSMSG02Y.cpy` | Abend message variables | Error handling |
| 25 | CSLKPCDY | `app/cpy/CSLKPCDY.cpy` | Lookup code data | Reference data |
| 26 | CSSETATY | `app/cpy/CSSETATY.cpy` | Set attribute utility | Screen formatting |
| 27 | CSSTRPFY | `app/cpy/CSSTRPFY.cpy` | String strip/pad functions | Data cleansing |
| 28 | CSUTLDPY | `app/cpy/CSUTLDPY.cpy` | Date utility parameters | Date validation |
| 29 | CSUTLDWY | `app/cpy/CSUTLDWY.cpy` | Date edit working storage | Date validation |
| 30 | CODATECN | `app/cpy/CODATECN.cpy` | Date conversion constants | Date formatting |

---

## 4. BMS Maps (17 maps)

| # | Map Name | File | Corresponding Program | Screen Function |
|---|----------|------|-----------------------|-----------------|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | COSGN00C | Sign-on screen |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | COMEN01C | Main menu |
| 3 | COADM01 | `app/bms/COADM01.bms` | COADM01C | Admin menu |
| 4 | COACTVW | `app/bms/COACTVW.bms` | COACTVWC | Account view |
| 5 | COACTUP | `app/bms/COACTUP.bms` | COACTUPC | Account update |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | COCRDLIC | Card list |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | COCRDSLC | Card detail view |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | COCRDUPC | Card update |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | COTRN00C | Transaction list |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | COTRN01C | Transaction view |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | COTRN02C | Transaction add |
| 12 | CORPT00 | `app/bms/CORPT00.bms` | CORPT00C | Transaction report request |
| 13 | COBIL00 | `app/bms/COBIL00.bms` | COBIL00C | Bill payment |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | COUSR00C | User list |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | COUSR01C | User add |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | COUSR02C | User update |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | COUSR03C | User delete |

BMS-generated copybooks are in `app/cpy-bms/` (17 files matching each map above).

---

## 5. JCL Jobs (38 jobs)

### 5.1 Data File Management (VSAM load/refresh)

| # | Job Name | File | Function | Key Datasets |
|---|----------|------|----------|-------------|
| 1 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | Refresh account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | `app/jcl/CARDFILE.jcl` | Refresh card master VSAM | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | Refresh customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | `app/jcl/XREFFILE.jcl` | Load card cross-reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | `app/jcl/TRANFILE.jcl` | Load transaction master VSAM + AIX | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | Load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | Load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | `app/jcl/TRANCATG.jcl` | Load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | `app/jcl/TCATBALF.jcl` | Load transaction category balance VSAM | TCATBALF.VSAM.KSDS |
| 10 | DISCGRP | `app/jcl/DISCGRP.jcl` | Load disclosure group VSAM | DISCGRP.VSAM.KSDS |
| 11 | REPTFILE | `app/jcl/REPTFILE.jcl` | Load report data file | REPTDATA.VSAM |
| 12 | DEFCUST | `app/jcl/DEFCUST.jcl` | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |

### 5.2 Batch Processing

| # | Job Name | File | Function | Executes Program |
|---|----------|------|----------|------------------|
| 13 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | Core transaction posting | CBTRN02C |
| 14 | INTCALC | `app/jcl/INTCALC.jcl` | Interest calculation | CBACT04C |
| 15 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | Combine transactions | SORT + IDCAMS |
| 16 | CREASTMT | `app/jcl/CREASTMT.JCL` | Generate account statements | CBSTM03A |
| 17 | TRANREPT | `app/jcl/TRANREPT.jcl` | Transaction detail report | CBTRN03C |
| 18 | CBEXPORT | `app/jcl/CBEXPORT.jcl` | Data export | CBEXPORT |
| 19 | CBIMPORT | `app/jcl/CBIMPORT.jcl` | Data import | CBIMPORT |
| 20 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | Admin card processing | IDCAMS |

### 5.3 Infrastructure / Utility

| # | Job Name | File | Function | Type |
|---|----------|------|----------|------|
| 21 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | Close CICS files for batch | CICS Control |
| 22 | OPENFIL | `app/jcl/OPENFIL.jcl` | Open CICS files after batch | CICS Control |
| 23 | TRANBKP | `app/jcl/TRANBKP.jcl` | Backup transaction VSAM to GDG | Backup |
| 24 | TRANIDX | `app/jcl/TRANIDX.jcl` | Define transaction alternate index | Index Mgmt |
| 25 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | Define GDG base for backups | GDG Setup |
| 26 | DEFGDGD | `app/jcl/DEFGDGD.jcl` | Define GDG base for daily data | GDG Setup |
| 27 | DALYREJS | `app/jcl/DALYREJS.jcl` | Daily rejects processing | Data Quality |
| 28 | ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | ESDS/RRDS VSAM operations | VSAM Utility |
| 29 | WAITSTEP | `app/jcl/WAITSTEP.jcl` | Job step wait (COBSWAIT) | Utility |
| 30 | FTPJCL | `app/jcl/FTPJCL.JCL` | FTP file transfer | File Transfer |
| 31 | INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | Internal reader job submission (step 1) | Job Control |
| 32 | INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | Internal reader job submission (step 2) | Job Control |
| 33 | TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | Convert text statements to PDF | Document |
| 34 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | Print catalog listing | Utility |
| 35 | READACCT | `app/jcl/READACCT.jcl` | Read/display account data | Diagnostic |
| 36 | READCARD | `app/jcl/READCARD.jcl` | Read/display card data | Diagnostic |
| 37 | READCUST | `app/jcl/READCUST.jcl` | Read/display customer data | Diagnostic |
| 38 | READXREF | `app/jcl/READXREF.jcl` | Read/display xref data | Diagnostic |

### 5.4 Batch Cycle Order (Production Run Sequence)
```
CLOSEFIL → ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE (data refresh)
         → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX
         → OPENFIL
```

---

## 6. Assembler Programs (2 files)

| # | Program | File | Function |
|---|---------|------|----------|
| 1 | MVSWAIT | `app/asm/MVSWAIT.asm` | MVS wait routine (system-level timer) |
| 2 | COBDATFT | `app/asm/COBDATFT.asm` | COBOL date format utility |

---

## 7. JCL Procedures (2 files)

| # | Procedure | File | Function |
|---|-----------|------|----------|
| 1 | REPROC | `app/proc/REPROC.prc` | Reprocessing procedure (VSAM repro) |
| 2 | TRANREPT | `app/proc/TRANREPT.prc` | Transaction report procedure |

---

## 8. Supporting Artifacts

| Category | Path | Contents |
|----------|------|----------|
| Sample Data (ASCII) | `app/data/ASCII/` | 9 data files: acctdata, carddata, cardxref, custdata, dailytran, discgrp, tcatbal, trancatg, trantype |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | Mainframe-format copies of above |
| CSD Definitions | `app/csd/` | CICS resource definitions (CARDDEMO.CSD) |
| Control Files | `app/ctl/` | REPROCT.ctl - reprocessing control |
| Scheduler Configs | `app/scheduler/` | CardDemo.ca7, CardDemo.controlm |
| Maclib | `app/maclib/` | Assembler macros |
| Catalog Listings | `app/catlg/` | VSAM catalog listings |

---

## 9. Summary Statistics

| Metric | Count |
|--------|-------|
| **Core COBOL Programs** | 31 |
| **Optional Module Programs** | 13 |
| **Total COBOL Programs** | 44 |
| **Copybooks (data layouts)** | 30 |
| **BMS Screen Maps** | 17 |
| **BMS-Generated Copybooks** | 17 |
| **JCL Jobs** | 38 |
| **JCL Procedures** | 2 |
| **Assembler Programs** | 2 |
| **Total Lines of COBOL** | ~30,175 (core: 20,650 + optional: 9,525) |
| **CICS Transactions** | 17 |
| **VSAM Datasets** | ~12 |

### Naming Conventions
- `CO*` = Online CICS programs
- `CB*` = Batch programs
- `CV*` = Copybook data structures (VSAM record layouts)
- `CS*` = Copybook shared/system utilities
- `CO*` (in cpy) = Copybook communication/control areas
- `COPA*` = Authorization module programs
- `COTR*` (in DB2 module) = Transaction type DB2 programs
