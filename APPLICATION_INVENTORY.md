# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Platform:** z/OS Mainframe | COBOL / CICS / VSAM / JCL / BMS

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **32 COBOL programs**, **31 copybooks**, **38 JCL batch jobs**, and **17 BMS screen maps** in the core module, plus **13 additional programs** across three optional extension modules. The system supports two user roles (Regular User and Admin) and covers account management, card management, transaction processing, billing, reporting, and user administration.

---

## 1. COBOL Programs (Core: 32 programs)

### 1.1 Online CICS Programs (20 programs)

| # | Program ID | Lines | Type | Function | CICS Trans | Business Domain |
|---|-----------|-------|------|----------|------------|-----------------|
| 1 | COSGN00C | 260 | Online CICS | User sign-on / authentication | CC00 | Security |
| 2 | COMEN01C | 308 | Online CICS | Main menu navigation | CM00 | Navigation |
| 3 | COADM01C | 288 | Online CICS | Admin menu navigation | CA00 | Administration |
| 4 | COACTVWC | 941 | Online CICS | Account detail viewer | CA01 | Account Mgmt |
| 5 | COACTUPC | 4,236 | Online CICS | Account update (credit limit, status) | CA02 | Account Mgmt |
| 6 | COCRDLIC | 1,459 | Online CICS | Card listing with pagination | CC01 | Card Mgmt |
| 7 | COCRDSLC | 887 | Online CICS | Card detail/selection viewer | CC02 | Card Mgmt |
| 8 | COCRDUPC | 1,560 | Online CICS | Card update (status, embossed name) | CC03 | Card Mgmt |
| 9 | COTRN00C | 699 | Online CICS | Transaction list with pagination | CT00 | Transaction Mgmt |
| 10 | COTRN01C | 330 | Online CICS | Transaction detail viewer | CT01 | Transaction Mgmt |
| 11 | COTRN02C | 783 | Online CICS | Add new transaction | CT02 | Transaction Mgmt |
| 12 | CORPT00C | 649 | Online CICS | Transaction report generation | CR00 | Reporting |
| 13 | COBIL00C | 572 | Online CICS | Bill payment processing | CB00 | Billing |
| 14 | COUSR00C | 695 | Online CICS | User list (Admin) with pagination | CU00 | User Admin |
| 15 | COUSR01C | 299 | Online CICS | Add new user (Admin) | CU01 | User Admin |
| 16 | COUSR02C | 414 | Online CICS | Update user details (Admin) | CU02 | User Admin |
| 17 | COUSR03C | 359 | Online CICS | Delete user (Admin) | CU03 | User Admin |
| 18 | CSUTLDTC | 157 | Utility | Date validation via CEEDAYS | -- | Shared Utility |

### 1.2 Batch Programs (14 programs)

| # | Program ID | Lines | Type | Function | Business Domain |
|---|-----------|-------|------|----------|-----------------|
| 19 | CBTRN01C | 494 | Batch | Read and list transaction records | Transaction Mgmt |
| 20 | CBTRN02C | 731 | Batch | Post daily transactions to master file | Transaction Processing |
| 21 | CBTRN03C | 649 | Batch | Generate daily transaction report | Reporting |
| 22 | CBACT01C | 430 | Batch | Read and list account records | Account Mgmt |
| 23 | CBACT02C | 178 | Batch | Read card cross-reference records | Card Mgmt |
| 24 | CBACT03C | 178 | Batch | Read card data records | Card Mgmt |
| 25 | CBACT04C | 652 | Batch | Calculate interest on accounts | Financial Processing |
| 26 | CBCUS01C | 178 | Batch | Read and list customer records | Customer Mgmt |
| 27 | CBSTM03A | 924 | Batch | Generate account statements (text + HTML) | Reporting |
| 28 | CBSTM03B | 230 | Batch Subroutine | File I/O handler for statement generation | Reporting |
| 29 | CBEXPORT | 582 | Batch | Export VSAM data to sequential file | Data Migration |
| 30 | CBIMPORT | 487 | Batch | Import sequential data into VSAM | Data Migration |
| 31 | COBSWAIT | 41 | Batch Utility | Wait/delay utility (calls MVSWAIT ASM) | Infrastructure |

**Naming Convention:**
- `CO*` = Online CICS programs
- `CB*` = Batch programs
- `CS*` = Shared utility/subroutine programs

---

## 2. Optional Extension Modules

### 2.1 Authorization Module (IMS/DB2/MQ) -- 8 programs

| # | Program ID | Lines | Type | Function |
|---|-----------|-------|------|----------|
| 1 | COPAUA0C | 1,026 | CICS/IMS/MQ | Card authorization decision engine (MQ trigger) |
| 2 | COPAUS0C | 1,032 | CICS/IMS/BMS | Authorization message summary view |
| 3 | COPAUS1C | 604 | CICS/IMS/BMS | Authorization message detail view |
| 4 | COPAUS2C | 244 | CICS/IMS/DB2 | Mark authorization as fraud (writes to DB2) |
| 5 | CBPAUP0C | 386 | Batch/IMS | Purge expired pending authorization messages |
| 6 | DBUNLDGS | 366 | Batch/IMS | Unload GSAM database segments |
| 7 | PAUDBLOD | 369 | Batch/IMS | Load authorization IMS database |
| 8 | PAUDBUNL | 317 | Batch/IMS | Unload authorization IMS database |

### 2.2 Transaction Type DB2 Module -- 3 programs

| # | Program ID | Lines | Type | Function |
|---|-----------|-------|------|----------|
| 1 | COTRTUPC | 1,702 | CICS/DB2 | Add/edit transaction types (DB2 CRUD) |
| 2 | COTRTLIC | 2,098 | CICS/DB2 | List/delete transaction types with DB2 cursors |
| 3 | COBTUPDT | 237 | Batch/DB2 | Batch update of transaction types |

### 2.3 VSAM-MQ Module -- 2 programs

| # | Program ID | Lines | Type | Function |
|---|-----------|-------|------|----------|
| 1 | CODATE01 | 524 | MQ Service | System date request/response via MQ |
| 2 | COACCT01 | 620 | MQ Service | Account inquiry request/response via MQ |

---

## 3. Copybooks (31 in core + 12 in optional modules)

### 3.1 Core Copybooks (app/cpy/)

| # | Copybook | Record Len | Domain | Description |
|---|----------|-----------|--------|-------------|
| 1 | CVACT01Y | 300 bytes | Account | Account master data record |
| 2 | CVACT02Y | 150 bytes | Card | Card data record |
| 3 | CVACT03Y | 50 bytes | Card | Card-to-account cross-reference |
| 4 | CVCUS01Y | 500 bytes | Customer | Customer master data record |
| 5 | CVCRD01Y | -- | Card | Card detail fields (online display) |
| 6 | CVTRA01Y | 50 bytes | Transaction | Transaction category balance |
| 7 | CVTRA02Y | 50 bytes | Transaction | Disclosure group / interest rate |
| 8 | CVTRA03Y | 60 bytes | Transaction | Transaction type code + description |
| 9 | CVTRA04Y | 60 bytes | Transaction | Transaction category type |
| 10 | CVTRA05Y | 350 bytes | Transaction | Online transaction record |
| 11 | CVTRA06Y | 350 bytes | Transaction | Daily transaction record |
| 12 | CVTRA07Y | -- | Reporting | Transaction report layout and headers |
| 13 | CVEXPORT | -- | Data Migration | Export record layout (customer, account, card, xref, transaction) |
| 14 | CSUSR01Y | 80 bytes | Security | User security record (userid, password, type) |
| 15 | COCOM01Y | -- | Common | COMMAREA (inter-program communication) |
| 16 | COMEN02Y | -- | Navigation | Menu item definitions |
| 17 | COADM02Y | -- | Navigation | Admin menu item definitions |
| 18 | COTTL01Y | -- | UI | Screen title / header layout |
| 19 | CSDAT01Y | -- | Utility | Date conversion working storage |
| 20 | CSMSG01Y | -- | UI | Short message area |
| 21 | CSMSG02Y | -- | UI | Long message area |
| 22 | CSSETATY | -- | UI | Set attribute bytes for BMS fields |
| 23 | CSSTRPFY | -- | Utility | String processing functions |
| 24 | CSLKPCDY | -- | Lookup | US state/country code lookup table |
| 25 | CSUTLDPY | -- | Utility | Date utility procedure code |
| 26 | CSUTLDWY | -- | Utility | Date utility working storage |
| 27 | CODATECN | -- | Utility | Date conversion constants |
| 28 | COSTM01 | 350 bytes | Reporting | Transaction altered layout for statements |
| 29 | CUSTREC | -- | Customer | Customer record (alternate layout) |
| 30 | UNUSED1Y | 80 bytes | Deprecated | Unused data record (candidate for removal) |

### 3.2 BMS-Generated Copybooks (app/cpy-bms/) -- 17 files

These are auto-generated from BMS map definitions and define the symbolic field maps for each screen. One copybook per BMS map.

### 3.3 Optional Module Copybooks

**Authorization Module (9 copybooks):**
CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB

**Transaction Type DB2 Module (2 copybooks):**
CSDB2RPY (DB2 common procedures), CSDB2RWY (DB2 common working storage)

---

## 4. BMS Screen Maps (17 maps in core + 4 in optional)

### 4.1 Core BMS Maps (app/bms/)

| # | Map Name | Lines | Screen Title | Associated Program |
|---|---------|-------|-------------|-------------------|
| 1 | COSGN00 | 210 | Login Screen | COSGN00C |
| 2 | COMEN01 | 167 | Main Menu | COMEN01C |
| 3 | COADM01 | 167 | Admin Menu | COADM01C |
| 4 | COACTVW | 378 | Account Viewer | COACTVWC |
| 5 | COACTUP | 512 | Account Update | COACTUPC |
| 6 | COCRDLI | 344 | Card Listing | COCRDLIC |
| 7 | COCRDSL | 157 | Card Selection/View | COCRDSLC |
| 8 | COCRDUP | 172 | Card Update | COCRDUPC |
| 9 | COTRN00 | 464 | Transaction List | COTRN00C |
| 10 | COTRN01 | 273 | Transaction View | COTRN01C |
| 11 | COTRN02 | 307 | Transaction Add | COTRN02C |
| 12 | CORPT00 | 231 | Transaction Reports | CORPT00C |
| 13 | COBIL00 | 141 | Bill Payment | COBIL00C |
| 14 | COUSR00 | 463 | User List (Admin) | COUSR00C |
| 15 | COUSR01 | 164 | Add User (Admin) | COUSR01C |
| 16 | COUSR02 | 169 | Update User (Admin) | COUSR02C |
| 17 | COUSR03 | 153 | Delete User (Admin) | COUSR03C |

### 4.2 Optional Module BMS Maps

| # | Map Name | Module | Screen Title |
|---|---------|--------|-------------|
| 1 | COPAU00 | Authorization | Authorization Summary View |
| 2 | COPAU01 | Authorization | Authorization Detail View |
| 3 | COTRTLI | Transaction Type DB2 | Transaction Type List |
| 4 | COTRTUP | Transaction Type DB2 | Transaction Type Update |

---

## 5. JCL Batch Jobs (38 jobs in core + 8 in optional)

### 5.1 Data Load / Refresh Jobs

| # | Job Name | Function | VSAM File Managed |
|---|---------|----------|-------------------|
| 1 | ACCTFILE | Define + load Account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | Define + load Card data VSAM + alt index | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | Define + load Customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | Define + load Card cross-reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | Define + load Transaction master VSAM | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | Define + load User security VSAM | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | Define + load Transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | Define + load Transaction category VSAM | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | Define + load Category balance VSAM | TCATBALF.VSAM.KSDS |
| 10 | DISCGRP | Define + load Disclosure/interest group VSAM | DISCGRP.VSAM.KSDS |
| 11 | DEFCUST | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |
| 12 | ESDSRRDS | Load ESDS and RRDS demo files | USRSEC.VSAM.ESDS/RRDS |

### 5.2 Batch Processing Jobs

| # | Job Name | Function | Program Executed |
|---|---------|----------|-----------------|
| 13 | POSTTRAN | Post daily transactions to master | CBTRN02C |
| 14 | INTCALC | Calculate interest on accounts | CBACT04C |
| 15 | CREASTMT | Generate account statements (text + HTML) | CBSTM03A |
| 16 | TRANREPT | Generate daily transaction report | CBTRN03C |
| 17 | COMBTRAN | Combine transaction backup + system transactions | SORT/IDCAMS |
| 18 | TRANBKP | Backup transaction master to GDG | IDCAMS |
| 19 | TRANIDX | Build alternate index on transactions | IDCAMS |
| 20 | PRTCATBL | Print/backup category balance file | CBTRN01C |
| 21 | CBEXPORT | Export all VSAM data to flat file | CBEXPORT |
| 22 | CBIMPORT | Import flat file data to VSAM | CBIMPORT |

### 5.3 CICS File Control Jobs

| # | Job Name | Function |
|---|---------|----------|
| 23 | CLOSEFIL | Close CICS files for batch processing |
| 24 | OPENFIL | Re-open CICS files after batch |
| 25 | CBADMCDJ | CICS CSD batch update (resource definitions) |

### 5.4 GDG / Infrastructure Jobs

| # | Job Name | Function |
|---|---------|----------|
| 26 | DEFGDGB | Define GDG base clusters (backups) |
| 27 | DEFGDGD | Define GDG bases + initial data copy |
| 28 | WAITSTEP | Execute wait utility (COBSWAIT) |
| 29 | DALYREJS | Define daily rejects GDG |
| 30 | REPTFILE | Define report GDG |

### 5.5 Utility / Diagnostic Jobs

| # | Job Name | Function |
|---|---------|----------|
| 31 | READACCT | Read and dump account file contents |
| 32 | READCARD | Read and dump card file contents |
| 33 | READCUST | Read and dump customer file contents |
| 34 | READXREF | Read and dump cross-reference file contents |
| 35 | FTPJCL | FTP file transfer (mainframe <-> server) |
| 36 | INTRDRJ1 | Internal reader: trigger INTRDRJ2 |
| 37 | INTRDRJ2 | Internal reader: secondary trigger |
| 38 | TXT2PDF1 | Convert text statement to PDF |

### 5.6 Optional Module JCL Jobs

**Authorization Module (5 jobs):**
CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB

**Transaction Type DB2 Module (3 jobs):**
CREADB21, MNTTRDB2, TRANEXTR

---

## 6. Assembler Programs (2 programs)

| # | Program ID | Function |
|---|-----------|----------|
| 1 | MVSWAIT | MVS wait macro (called by COBSWAIT) |
| 2 | COBDATFT | Date format conversion utility |

---

## 7. Supporting Artifacts

| Category | Path | Count | Description |
|----------|------|-------|-------------|
| JCL Procedures | app/proc/ | 2 | REPROC.prc, TRANREPT.prc |
| Control Files | app/ctl/ | 1 | REPROCT.ctl |
| CSD Definitions | app/csd/ | 1 | CARDDEMO.CSD (CICS resource defs) |
| Scheduler Configs | app/scheduler/ | 2 | CardDemo.ca7, CardDemo.controlm |
| Assembler Macros | app/maclib/ | -- | Macro library |
| Sample Data (ASCII) | app/data/ASCII/ | -- | Test data for converted apps |
| Sample Data (EBCDIC) | app/data/EBCDIC/ | -- | Mainframe upload data |
| Shell Scripts | scripts/ | 10 | FTP, compile, refresh, batch scripts |
| Sample JCL/Configs | samples/ | -- | Compilation JCL, M2 configs |

---

## 8. Classification Summary

| Classification | Core | Auth Module | TranType DB2 | VSAM-MQ | Total |
|---------------|------|------------|--------------|---------|-------|
| Online CICS Programs | 18 | 3 | 2 | 0 | 23 |
| Batch Programs | 12 | 5 | 1 | 0 | 18 |
| MQ Service Programs | 0 | 1 | 0 | 2 | 3 |
| Utility Programs | 2 | 0 | 0 | 0 | 2 |
| **Total Programs** | **32** | **9** | **3** | **2** | **46** |
| Copybooks | 31 | 9 | 2 | 0 | 42 |
| BMS Maps | 17 | 2 | 2 | 0 | 21 |
| JCL Jobs | 38 | 5 | 3 | 0 | 46 |
| Assembler | 2 | 0 | 0 | 0 | 2 |

**Total Source Artifacts: 157**

---

## 9. Batch Processing Cycle

The recommended nightly batch sequence:

```
CLOSEFIL --> ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE (data refresh)
         --> POSTTRAN (transaction posting)
         --> INTCALC (interest calculation)
         --> TRANBKP (backup transactions)
         --> COMBTRAN (combine transactions)
         --> CREASTMT (generate statements)
         --> TRANREPT (daily transaction report)
         --> TRANIDX (rebuild alternate indexes)
         --> OPENFIL
```
