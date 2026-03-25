# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Scope:** Full CardDemo COBOL/CICS/VSAM/JCL codebase  
> **Total Artifacts:** 31 COBOL programs + 30 copybooks + 38 JCL jobs + 17 BMS maps + 13 optional-module programs + 2 ASM programs + 2 JCL procedures

---

## 1. COBOL Programs (`app/cbl/`) -- 31 Programs

### 1.1 Online CICS Programs (16 programs)

Programs that run under CICS and handle interactive 3270 terminal sessions.

| # | Program | Lines | Function | CICS Trans | BMS Map | Domain |
|---|---------|------:|----------|------------|---------|--------|
| 1 | COSGN00C | 260 | Signon / Authentication | CC00 | COSGN00 | Security |
| 2 | COMEN01C | 308 | Main Menu (Regular Users) | CM00 | COMEN01 | Navigation |
| 3 | COADM01C | 288 | Admin Menu (Admin Users) | CA00 | COADM01 | Navigation |
| 4 | COACTVWC | 941 | Account View (read-only) | CA01 | COACTVW | Account Mgmt |
| 5 | COACTUPC | 4,236 | Account Update | CA02 | COACTUP | Account Mgmt |
| 6 | COCRDLIC | 1,459 | Credit Card List | CC01 | COCRDLI | Card Mgmt |
| 7 | COCRDSLC | 887 | Credit Card View (detail) | CC02 | COCRDSL | Card Mgmt |
| 8 | COCRDUPC | 1,560 | Credit Card Update | CC03 | COCRDUP | Card Mgmt |
| 9 | COTRN00C | 699 | Transaction List | CT00 | COTRN00 | Transactions |
| 10 | COTRN01C | 330 | Transaction View (detail) | CT01 | COTRN01 | Transactions |
| 11 | COTRN02C | 783 | Transaction Add (new) | CT02 | COTRN02 | Transactions |
| 12 | CORPT00C | 649 | Transaction Report (submit batch via TDQ) | CR00 | CORPT00 | Reporting |
| 13 | COBIL00C | 572 | Bill Payment | CB00 | COBIL00 | Payments |
| 14 | COUSR00C | 695 | User List (Admin) | CU00 | COUSR00 | User Admin |
| 15 | COUSR01C | 299 | User Add (Admin) | CU01 | COUSR01 | User Admin |
| 16 | COUSR02C | 414 | User Update (Admin) | CU02 | COUSR02 | User Admin |

### 1.2 Online CICS Programs -- Admin Only (1 program)

| # | Program | Lines | Function | CICS Trans | BMS Map | Domain |
|---|---------|------:|----------|------------|---------|--------|
| 17 | COUSR03C | 359 | User Delete (Admin) | CU03 | COUSR03 | User Admin |

### 1.3 Batch Programs (12 programs)

Programs invoked by JCL jobs for offline/overnight processing.

| # | Program | Lines | Function | Domain |
|---|---------|------:|----------|--------|
| 18 | CBACT01C | 430 | Read account file and write to output files | Account Mgmt |
| 19 | CBACT02C | 178 | Read and print card data file | Card Mgmt |
| 20 | CBACT03C | 178 | Read and print account cross-reference file | Card Mgmt |
| 21 | CBACT04C | 652 | Interest calculation on accounts | Financial Processing |
| 22 | CBCUS01C | 178 | Read and print customer data file | Customer Mgmt |
| 23 | CBTRN01C | 494 | Post daily transactions (variant 1) | Transaction Processing |
| 24 | CBTRN02C | 731 | Post daily transactions (variant 2 -- primary) | Transaction Processing |
| 25 | CBTRN03C | 649 | Print transaction detail report | Reporting |
| 26 | CBSTM03A | 924 | Generate account statements (text + HTML) | Statement Generation |
| 27 | CBSTM03B | 230 | Subroutine: File I/O for statement generation | Statement Generation |
| 28 | CBEXPORT | 582 | Export customer data for branch migration | Data Migration |
| 29 | CBIMPORT | 487 | Import customer data from branch migration | Data Migration |
| 30 | COBSWAIT | 41 | Utility: Wait for specified centiseconds | Utility |

### 1.4 Shared Utility Programs (1 program)

| # | Program | Lines | Function | Called By |
|---|---------|------:|----------|-----------|
| 31 | CSUTLDTC | 157 | Date validation (calls LE CEEDAYS) | COTRN02C, CORPT00C |

---

## 2. Copybooks (`app/cpy/`) -- 30 Copybooks

### 2.1 Data-Structure Copybooks (Record Layouts)

| # | Copybook | Record Length | Business Entity | Used By |
|---|----------|-------------:|-----------------|---------|
| 1 | CVACT01Y | 300 bytes | Account Master Record | 12 programs |
| 2 | CVACT02Y | 150 bytes | Card Data Record | 8 programs |
| 3 | CVACT03Y | 50 bytes | Card-to-Account Cross-Reference | 10 programs |
| 4 | CVCUS01Y | 500 bytes | Customer Master Record | 7 programs |
| 5 | CVCRD01Y | -- | Card Display Record (screen working data) | 5 programs |
| 6 | CVTRA01Y | 50 bytes | Transaction Category Balance | 2 programs |
| 7 | CVTRA02Y | 50 bytes | Disclosure Group (interest rates) | 1 program |
| 8 | CVTRA03Y | 60 bytes | Transaction Type Reference | 1 program |
| 9 | CVTRA04Y | 60 bytes | Transaction Category Type | 1 program |
| 10 | CVTRA05Y | 350 bytes | Transaction Record (master) | 10 programs |
| 11 | CVTRA06Y | 350 bytes | Daily Transaction Record (input) | 2 programs |
| 12 | CVTRA07Y | -- | Transaction Report Headers/Totals | 1 program |
| 13 | CVEXPORT | -- | Export/Import Multi-Record Layout | 2 programs |
| 14 | CUSTREC | -- | Customer Record (alternate layout for statements) | 1 program |
| 15 | COSTM01 | 350 bytes | Transaction Record (statement-reporting layout, keyed by card+tran) | 1 program |
| 16 | CSUSR01Y | 80 bytes | User Security Record | 12 programs |
| 17 | UNUSED1Y | 80 bytes | Unused placeholder record | 0 programs |

### 2.2 Common/Infrastructure Copybooks

| # | Copybook | Purpose | Used By |
|---|----------|---------|---------|
| 18 | COCOM01Y | Common communication area (COMMAREA) | 15 programs |
| 19 | COTTL01Y | Title/Header display fields | 14 programs |
| 20 | CSDAT01Y | Date display fields | 14 programs |
| 21 | CSMSG01Y | Message line 1 display fields | 14 programs |
| 22 | CSMSG02Y | Message line 2 display fields | 4 programs |
| 23 | COMEN02Y | Menu option definitions (program names, tran codes) | 1 program |
| 24 | COADM02Y | Admin menu option definitions | 1 program |
| 25 | CSSETATY | Set field attributes (COPY REPLACING pattern) | 2 programs |
| 26 | CSSTRPFY | Strip/format PIC field utility | 5 programs |
| 27 | CSUTLDPY | Date utility parameter area | 1 program |
| 28 | CSUTLDWY | Date utility working storage | 2 programs |
| 29 | CSLKPCDY | Lookup code working storage | 1 program |
| 30 | CODATECN | Date conversion record (for ASM call) | 1 program |

---

## 3. BMS Screen Maps (`app/bms/`) -- 17 Maps

| # | Map Set | Physical Map | Screen Title | Owning Program | Type |
|---|---------|-------------|--------------|----------------|------|
| 1 | COSGN00 | COSGN0A | Signon | COSGN00C | Login |
| 2 | COMEN01 | COMEN1A | Main Menu | COMEN01C | Menu |
| 3 | COADM01 | COADM1A | Admin Menu | COADM01C | Menu |
| 4 | COACTVW | CACTVWA | Account View | COACTVWC | View |
| 5 | COACTUP | CACTUPA | Account Update | COACTUPC | Update |
| 6 | COCRDLI | CCRDLIA | Card List | COCRDLIC | List |
| 7 | COCRDSL | CCRDSIA | Card Detail | COCRDSLC | View |
| 8 | COCRDUP | CCRDUPA | Card Update | COCRDUPC | Update |
| 9 | COTRN00 | COTRN0A | Transaction List | COTRN00C | List |
| 10 | COTRN01 | COTRN1A | Transaction View | COTRN01C | View |
| 11 | COTRN02 | COTRN2A | Transaction Add | COTRN02C | Entry |
| 12 | CORPT00 | CORPT0A | Transaction Report | CORPT00C | Report |
| 13 | COBIL00 | COBIL0A | Bill Payment | COBIL00C | Entry |
| 14 | COUSR00 | COUSR0A | User List | COUSR00C | List |
| 15 | COUSR01 | COUSR1A | User Add | COUSR01C | Entry |
| 16 | COUSR02 | COUSR2A | User Update | COUSR02C | Update |
| 17 | COUSR03 | COUSR3A | User Delete | COUSR03C | Delete |

### BMS-Generated Copybooks (`app/cpy-bms/`) -- 17 files
Each BMS map generates a corresponding copybook (e.g., `COSGN00.CPY`) containing the symbolic map data structures used by COBOL programs for SEND MAP / RECEIVE MAP operations.

---

## 4. JCL Batch Jobs (`app/jcl/`) -- 38 Jobs

### 4.1 Data Refresh / File Load Jobs (8 jobs)

| # | Job | Function | Target Dataset |
|---|-----|----------|----------------|
| 1 | ACCTFILE | Delete/redefine/reload Account VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | Delete/redefine/reload Card VSAM + AIX | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | Delete/redefine/reload Customer VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | Delete/redefine/reload Card Xref VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | Delete/redefine/reload Transaction VSAM | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | Define/load User Security VSAM from inline data | USRSEC.VSAM.KSDS |
| 7 | DEFCUST | Define Customer VSAM cluster (no data load) | CUSTDATA.VSAM.KSDS |
| 8 | REPTFILE | Define Transaction Report VSAM cluster | TRANSACT.REPORT.VSAM.KSDS |

### 4.2 Batch Processing Jobs (7 jobs)

| # | Job | Program | Function |
|---|-----|---------|----------|
| 9 | POSTTRAN | CBTRN02C | Post daily transactions to master file |
| 10 | INTCALC | CBACT04C | Calculate interest on account balances |
| 11 | COMBTRAN | (SORT) | Combine/sort transaction files |
| 12 | CREASTMT | CBSTM03A | Generate account statements (text + HTML) |
| 13 | TRANBKP | (IDCAMS) | Backup transaction VSAM to sequential |
| 14 | TRANIDX | (IDCAMS) | Build alternate index on transaction file |
| 15 | WAITSTEP | COBSWAIT | Execute wait utility between steps |

### 4.3 Data Export/Import Jobs (2 jobs)

| # | Job | Program | Function |
|---|-----|---------|----------|
| 16 | CBEXPORT | CBEXPORT | Export customer data for branch migration |
| 17 | CBIMPORT | CBIMPORT | Import customer data from branch migration |

### 4.4 Reference Data Definition Jobs (6 jobs)

| # | Job | Function |
|---|-----|----------|
| 18 | TRANTYPE | Define/load Transaction Type VSAM |
| 19 | TRANCATG | Define/load Transaction Category VSAM |
| 20 | TCATBALF | Define/load Transaction Category Balance VSAM |
| 21 | DISCGRP | Define/load Disclosure Group VSAM |
| 22 | DEFGDGB | Define GDG base for backups |
| 23 | DEFGDGD | Define GDG base for daily files |

### 4.5 Reporting Jobs (2 jobs)

| # | Job | Function |
|---|-----|----------|
| 24 | TRANREPT | Print transaction detail report (uses TRANREPT.prc) |
| 25 | DALYREJS | Define Daily Transaction Reject VSAM |

### 4.6 Data Verification Jobs (4 jobs)

| # | Job | Program | Function |
|---|-----|---------|----------|
| 26 | READACCT | CBACT01C | Read/verify account data file |
| 27 | READCARD | CBACT02C | Read/verify card data file |
| 28 | READCUST | CBCUS01C | Read/verify customer data file |
| 29 | READXREF | CBACT03C | Read/verify cross-reference file |

### 4.7 CICS File Management Jobs (2 jobs)

| # | Job | Function |
|---|-----|----------|
| 30 | CLOSEFIL | Close CICS files for batch processing |
| 31 | OPENFIL | Open CICS files after batch completes |

### 4.8 Utility / Infrastructure Jobs (6 jobs)

| # | Job | Function |
|---|-----|----------|
| 32 | ESDSRRDS | Define ESDS/RRDS sample VSAM clusters |
| 33 | PRTCATBL | Print VSAM catalog entries |
| 34 | FTPJCL | FTP file transfer job |
| 35 | INTRDRJ1 | Internal reader trigger job (triggers INTRDRJ2) |
| 36 | INTRDRJ2 | Internal reader target job |
| 37 | TXT2PDF1 | Convert text statement to PDF |
| 38 | CBADMCDJ | Administrative card job |

---

## 5. Optional Extension Modules -- 13 Additional Programs

### 5.1 Authorization Module (`app/app-authorization-ims-db2-mq/`) -- IMS/DB2/MQ

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 1 | COPAUA0C | 313 | MQ trigger -- receive authorization requests | CICS + MQ |
| 2 | COPAUS0C | 266 | Authorization summary display | CICS |
| 3 | COPAUS1C | 235 | Authorization detail display | CICS |
| 4 | COPAUS2C | 244 | Mark fraud to DB2 | CICS + DB2 |
| 5 | CBPAUP0C | 176 | Batch purge of authorization records | Batch + DB2 |
| 6 | PAUDBLOD | 369 | IMS database load utility | Batch + IMS |
| 7 | PAUDBUNL | 317 | IMS database unload utility | Batch + IMS |
| 8 | DBUNLDGS | 366 | IMS generalized segment unload | Batch + IMS |

### 5.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 9 | COTRTUPC | 1,702 | Transaction type add/edit (CRUD) | CICS + DB2 |
| 10 | COTRTLIC | 2,098 | Transaction type list with paging | CICS + DB2 |
| 11 | COBTUPDT | 237 | Batch update of transaction types | Batch + DB2 |

### 5.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 12 | COACCT01 | 620 | MQ request/response: account inquiry | CICS + MQ |
| 13 | CODATE01 | 524 | MQ request/response: system date | CICS + MQ |

---

## 6. Assembler Programs (`app/asm/`) -- 2 Programs

| # | Program | Function |
|---|---------|----------|
| 1 | COBDATFT | Date formatting utility (called by CBACT01C) |
| 2 | MVSWAIT | Wait/delay utility (called by COBSWAIT) |

---

## 7. JCL Procedures (`app/proc/`) -- 2 Procedures

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC | Reprocessing procedure template |
| 2 | TRANREPT | Transaction report procedure (invoked by TRANREPT.jcl) |

---

## 8. Supporting Artifacts

| Category | Location | Count | Description |
|----------|----------|------:|-------------|
| CSD Definitions | `app/csd/` | 1 | CICS resource definitions (CARDDEMO.CSD) |
| Scheduler Configs | `app/scheduler/` | 2 | CA7 and Control-M job schedules |
| Sample Data (ASCII) | `app/data/ASCII/` | 9 | Test data files for all VSAM entities |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | 9 | Mainframe-uploadable data files |
| Shell Scripts | `scripts/` | 11 | FTP-based mainframe interaction scripts |
| Control Files | `app/ctl/` | -- | Batch control files |
| Macro Library | `app/maclib/` | -- | Assembler macros |

---

## 9. Classification Summary

| Classification | Count | Key Characteristic |
|----------------|------:|---------------------|
| Online CICS (core) | 17 | Interactive 3270 terminal programs |
| Batch (core) | 14 | JCL-invoked batch processing |
| Utility/Shared | 2 | Called subroutines (CSUTLDTC, CBSTM03B) |
| Assembler | 2 | Low-level system utilities |
| Optional -- IMS/DB2/MQ Auth | 8 | IMS database + DB2 + MQ integration |
| Optional -- DB2 Tran Type | 3 | DB2 CRUD for transaction types |
| Optional -- VSAM-MQ | 2 | MQ-based inquiry services |
| **Total Programs** | **48** | |
| Copybooks (core) | 30 | Record layouts + infrastructure |
| BMS Maps | 17 | 3270 screen definitions |
| JCL Jobs | 38 | Batch job definitions |
| JCL Procedures | 2 | Reusable JCL steps |
