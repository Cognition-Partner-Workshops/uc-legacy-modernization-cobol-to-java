# CardDemo Application Inventory

> **Generated from:** `uc-legacy-modernization-cobol-to-java` repository
> **Application:** AWS CardDemo -- Credit Card Management System (COBOL/CICS/VSAM/JCL)
> **Total Artifacts:** 31 COBOL programs + 30 copybooks + 38 JCL jobs + 17 BMS maps + 13 optional-module programs + 2 ASM programs + 2 JCL procedures + 2 scheduler configs

---

## 1. COBOL Programs (Core -- `app/cbl/`)

### 1.1 Online CICS Programs (CO* prefix)

| # | Program | Lines | CICS Tran | Description | Business Domain | Classification |
|---|---------|------:|-----------|-------------|-----------------|----------------|
| 1 | **COSGN00C.cbl** | 260 | CC00 | User sign-on / authentication | Security | Authentication |
| 2 | **COMEN01C.cbl** | 308 | CM00 | Main menu dispatcher | Navigation | Menu / Router |
| 3 | **COADM01C.cbl** | 288 | CA00 | Admin menu dispatcher | Administration | Menu / Router |
| 4 | **COACTVWC.cbl** | 941 | CA01 | Account view (read-only) | Account Mgmt | Inquiry |
| 5 | **COACTUPC.cbl** | 4,236 | CA02 | Account update (full CRUD) | Account Mgmt | Maintenance |
| 6 | **COCRDLIC.cbl** | 1,459 | CC01 | Credit card list / search | Card Mgmt | List / Browse |
| 7 | **COCRDSLC.cbl** | 887 | CC02 | Credit card detail view | Card Mgmt | Inquiry |
| 8 | **COCRDUPC.cbl** | 1,560 | CC03 | Credit card update | Card Mgmt | Maintenance |
| 9 | **COTRN00C.cbl** | 699 | CT00 | Transaction list / browse | Transactions | List / Browse |
| 10 | **COTRN01C.cbl** | 330 | CT01 | Transaction detail view | Transactions | Inquiry |
| 11 | **COTRN02C.cbl** | 783 | CT02 | Transaction add (new) | Transactions | Data Entry |
| 12 | **CORPT00C.cbl** | 649 | CR00 | Transaction reports (submit JCL) | Reporting | Report Launch |
| 13 | **COBIL00C.cbl** | 572 | CB00 | Bill payment processing | Payments | Transaction Processing |
| 14 | **COUSR00C.cbl** | 695 | CU00 | User security list | User Admin | List / Browse |
| 15 | **COUSR01C.cbl** | 299 | CU01 | User add (security) | User Admin | Data Entry |
| 16 | **COUSR02C.cbl** | 414 | CU02 | User update (security) | User Admin | Maintenance |
| 17 | **COUSR03C.cbl** | 359 | CU03 | User delete (security) | User Admin | Maintenance |

### 1.2 Batch Programs (CB* prefix)

| # | Program | Lines | Description | Business Domain | Classification |
|---|---------|------:|-------------|-----------------|----------------|
| 18 | **CBACT01C.cbl** | 430 | Read account file, write output files | Account Mgmt | Data Extract |
| 19 | **CBACT02C.cbl** | 178 | Read and display card file | Card Mgmt | Data Extract |
| 20 | **CBACT03C.cbl** | 178 | Read and display cross-reference file | Card Mgmt | Data Extract |
| 21 | **CBACT04C.cbl** | 652 | Interest calculation & fee computation | Financial Calc | Business Logic |
| 22 | **CBCUS01C.cbl** | 178 | Read and display customer file | Customer Mgmt | Data Extract |
| 23 | **CBTRN01C.cbl** | 494 | Daily transaction validation & lookup | Transactions | Validation |
| 24 | **CBTRN02C.cbl** | 731 | Transaction posting (daily to master) | Transactions | Posting |
| 25 | **CBTRN03C.cbl** | 649 | Transaction report generation | Reporting | Report Gen |
| 26 | **CBSTM03A.CBL** | 924 | Statement generation (main driver) | Statements | Report Gen |
| 27 | **CBSTM03B.CBL** | 230 | Statement generation (file I/O helper) | Statements | I/O Utility |
| 28 | **CBEXPORT.cbl** | 582 | Multi-entity data export to flat file | Data Migration | ETL Export |
| 29 | **CBIMPORT.cbl** | 487 | Import from flat file to VSAM files | Data Migration | ETL Import |

### 1.3 Utility Programs

| # | Program | Lines | Description | Business Domain | Classification |
|---|---------|------:|-------------|-----------------|----------------|
| 30 | **CSUTLDTC.cbl** | 157 | Date validation utility (calls CEEDAYS) | Shared Utility | Date Processing |
| 31 | **COBSWAIT.cbl** | 41 | Wait/delay utility (calls MVSWAIT ASM) | Shared Utility | System Utility |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Structure Copybooks (CV* prefix -- VSAM record layouts)

| # | Copybook | Record Name | Record Length | Business Entity | Key Fields |
|---|----------|-------------|-------------:|-----------------|------------|
| 1 | **CVACT01Y.cpy** | ACCOUNT-RECORD | 300 | Account Master | ACCT-ID (11-digit) |
| 2 | **CVACT02Y.cpy** | CARD-RECORD | 150 | Card Master | CARD-NUM (16-digit) |
| 3 | **CVACT03Y.cpy** | CARD-XREF-RECORD | 50 | Card Cross-Reference | XREF-CARD-NUM -> XREF-ACCT-ID |
| 4 | **CVCUS01Y.cpy** | CUSTOMER-RECORD | 500 | Customer Master | CUST-ID (9-digit) |
| 5 | **CVCRD01Y.cpy** | FD-CARDFILE-REC | varies | Card File (FD) | CARD-NUM |
| 6 | **CVTRA01Y.cpy** | TRAN-CAT-BAL-RECORD | 50 | Tran Category Balance | ACCT-ID + TYPE-CD + CAT-CD |
| 7 | **CVTRA02Y.cpy** | DIS-GROUP-RECORD | 50 | Disclosure Group (Interest) | GROUP-ID + TRAN-TYPE + CAT |
| 8 | **CVTRA03Y.cpy** | TRAN-TYPE-RECORD | 60 | Transaction Type | TRAN-TYPE (2-char) |
| 9 | **CVTRA04Y.cpy** | TRAN-CAT-RECORD | 60 | Transaction Category | TYPE-CD + CAT-CD |
| 10 | **CVTRA05Y.cpy** | TRAN-RECORD | 350 | Transaction Master | TRAN-ID (16-char) |
| 11 | **CVTRA06Y.cpy** | DALYTRAN-RECORD | 350 | Daily Transaction | DALYTRAN-ID (16-char) |
| 12 | **CVTRA07Y.cpy** | TRANSACTION-DETAIL-REPORT | varies | Report Layout | (print line) |
| 13 | **CVEXPORT.cpy** | EXPORT-RECORD | varies | Export Record | EXP-REC-TYPE |
| 14 | **CUSTREC.cpy** | (Customer alt layout) | varies | Customer (alt) | -- |
| 15 | **COSTM01.CPY** | TRNX-RECORD | 350+ | Statement Tran Layout | TRNX-CARD-NUM + TRNX-ID |
| 16 | **UNUSED1Y.cpy** | UNUSED-DATA | 80 | (Unused placeholder) | -- |

### 2.2 Application Copybooks (CO*/CS*/CM* prefix)

| # | Copybook | Purpose | Used By |
|---|----------|---------|---------|
| 17 | **COCOM01Y.cpy** | COMMAREA -- inter-program communication | All online programs |
| 18 | **COMEN02Y.cpy** | Main menu option definitions (11 items) | COMEN01C |
| 19 | **COADM02Y.cpy** | Admin menu option definitions (6 items) | COADM01C |
| 20 | **COTTL01Y.cpy** | Screen title/header literals | All online programs |
| 21 | **CSDAT01Y.cpy** | Date/time working storage fields | All online programs |
| 22 | **CSMSG01Y.cpy** | Message area definitions | All online programs |
| 23 | **CSMSG02Y.cpy** | Extended message area | Select online programs |
| 24 | **CSUSR01Y.cpy** | User security record layout | Auth/User programs |
| 25 | **CSSETATY.cpy** | Screen attribute setting utility | Online programs |
| 26 | **CSSTRPFY.cpy** | String/PF-key processing utility | Online programs |
| 27 | **CSUTLDPY.cpy** | Date utility parameters | CSUTLDTC |
| 28 | **CSUTLDWY.cpy** | Date utility working storage | COACTUPC |
| 29 | **CSLKPCDY.cpy** | Lookup codes (phone, state, zip) | COACTUPC |
| 30 | **CODATECN.cpy** | Date conversion record layout | CBACT01C, date formatting |

---

## 3. BMS Maps (`app/bms/` -- 17 maps)

| # | Map | Mapset | Screen Purpose | Associated Program |
|---|-----|--------|----------------|--------------------|
| 1 | **COSGN00.bms** | COSGN00 | Sign-on screen | COSGN00C |
| 2 | **COMEN01.bms** | COMEN01 | Main menu | COMEN01C |
| 3 | **COADM01.bms** | COADM01 | Admin menu | COADM01C |
| 4 | **COACTVW.bms** | COACTVW | Account view | COACTVWC |
| 5 | **COACTUP.bms** | COACTUP | Account update | COACTUPC |
| 6 | **COCRDLI.bms** | COCRDLI | Card list | COCRDLIC |
| 7 | **COCRDSL.bms** | COCRDSL | Card detail view | COCRDSLC |
| 8 | **COCRDUP.bms** | COCRDUP | Card update | COCRDUPC |
| 9 | **COTRN00.bms** | COTRN00 | Transaction list | COTRN00C |
| 10 | **COTRN01.bms** | COTRN01 | Transaction view | COTRN01C |
| 11 | **COTRN02.bms** | COTRN02 | Transaction add | COTRN02C |
| 12 | **CORPT00.bms** | CORPT00 | Transaction reports | CORPT00C |
| 13 | **COBIL00.bms** | COBIL00 | Bill payment | COBIL00C |
| 14 | **COUSR00.bms** | COUSR00 | User list | COUSR00C |
| 15 | **COUSR01.bms** | COUSR01 | User add | COUSR01C |
| 16 | **COUSR02.bms** | COUSR02 | User update | COUSR02C |
| 17 | **COUSR03.bms** | COUSR03 | User delete | COUSR03C |

> **BMS-Generated Copybooks** (`app/cpy-bms/`): 17 corresponding `.CPY` files are auto-generated from the BMS maps and included in the online programs for screen field definitions.

---

## 4. JCL Batch Jobs (`app/jcl/` -- 38 jobs)

### 4.1 Data Refresh Jobs

| # | JCL Job | Purpose | Key Program/Utility | VSAM Files Affected |
|---|---------|---------|---------------------|---------------------|
| 1 | **ACCTFILE.jcl** | Refresh account master VSAM | IDCAMS REPRO | ACCTFILE |
| 2 | **CARDFILE.jcl** | Refresh card master VSAM | IDCAMS REPRO | CARDFILE |
| 3 | **CUSTFILE.jcl** | Refresh customer master VSAM | IDCAMS REPRO | CUSTFILE |
| 4 | **XREFFILE.jcl** | Load card cross-reference | IDCAMS REPRO | CARDXREF |
| 5 | **TRANFILE.jcl** | Load transaction master | IDCAMS REPRO | TRANSACT |
| 6 | **DUSRSECJ.jcl** | Load user security VSAM | IDCAMS REPRO | USRSEC |
| 7 | **DEFCUST.jcl** | Define customer VSAM cluster | IDCAMS DEFINE | CUSTFILE |

### 4.2 Batch Processing Jobs

| # | JCL Job | Purpose | Key Program | Input Files | Output Files |
|---|---------|---------|-------------|-------------|--------------|
| 8 | **POSTTRAN.jcl** | Post daily transactions | CBTRN02C | DALYTRAN | TRANSACT, ACCTFILE |
| 9 | **INTCALC.jcl** | Interest calculation | CBACT04C | TCATBALF, XREF, DISCGRP, ACCTFILE | TRANSACT |
| 10 | **COMBTRAN.jcl** | Combine daily + master trans | SORT/MERGE | DALYTRAN, TRANSACT | COMBINED |
| 11 | **CREASTMT.JCL** | Create account statements | CBSTM03A/B | TRANSACT, XREF, CUST, ACCT | STMTFILE |
| 12 | **TRANBKP.jcl** | Backup transaction file | IDCAMS REPRO | TRANSACT | BACKUP |
| 13 | **TRANIDX.jcl** | Define alternate index on trans | IDCAMS AIX | TRANSACT | ALT-INDEX |
| 14 | **TRANREPT.jcl** | Daily transaction report | CBTRN03C | TRANSACT, XREF, TYPES, CATS | REPORT |

### 4.3 CICS File Management Jobs

| # | JCL Job | Purpose |
|---|---------|---------|
| 15 | **CLOSEFIL.jcl** | Close CICS files for batch window |
| 16 | **OPENFIL.jcl** | Reopen CICS files after batch |

### 4.4 Utility / Infrastructure Jobs

| # | JCL Job | Purpose |
|---|---------|---------|
| 17 | **CBEXPORT.jcl** | Run data export (CBEXPORT) |
| 18 | **CBIMPORT.jcl** | Run data import (CBIMPORT) |
| 19 | **DALYREJS.jcl** | Process daily rejection file |
| 20 | **DEFGDGB.jcl** | Define GDG base for backups |
| 21 | **DEFGDGD.jcl** | Define GDG base for daily files |
| 22 | **DISCGRP.jcl** | Define disclosure group VSAM |
| 23 | **ESDSRRDS.jcl** | Define ESDS/RRDS clusters |
| 24 | **PRTCATBL.jcl** | Print catalog listing |
| 25 | **TCATBALF.jcl** | Define tran category balance VSAM |
| 26 | **TRANCATG.jcl** | Define tran category type VSAM |
| 27 | **TRANTYPE.jcl** | Define transaction type VSAM |
| 28 | **REPTFILE.jcl** | Define report output dataset |
| 29 | **CBADMCDJ.jcl** | Admin batch job |
| 30 | **FTPJCL.JCL** | FTP file transfer JCL |
| 31 | **INTRDRJ1.JCL** | Internal reader job submission #1 |
| 32 | **INTRDRJ2.JCL** | Internal reader job submission #2 |
| 33 | **TXT2PDF1.JCL** | Convert text reports to PDF |
| 34 | **WAITSTEP.jcl** | Wait step for job scheduling |

### 4.5 File Read/Display Jobs

| # | JCL Job | Purpose | Program |
|---|---------|---------|---------|
| 35 | **READACCT.jcl** | Read & display account file | CBACT01C |
| 36 | **READCARD.jcl** | Read & display card file | CBACT02C |
| 37 | **READCUST.jcl** | Read & display customer file | CBCUS01C |
| 38 | **READXREF.jcl** | Read & display xref file | CBACT03C |

---

## 5. Optional Modules

### 5.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

| # | Program | Lines | Description | Technologies |
|---|---------|------:|-------------|--------------|
| 1 | **COPAUA0C.cbl** | -- | MQ trigger for authorization | MQ |
| 2 | **COPAUS0C.cbl** | -- | Auth summary view | CICS/IMS |
| 3 | **COPAUS1C.cbl** | -- | Auth details view | CICS/IMS |
| 4 | **COPAUS2C.cbl** | -- | Fraud marking (write to DB2) | CICS/DB2 |
| 5 | **CBPAUP0C.cbl** | -- | Batch purge old auths | Batch/DB2 |
| 6 | **DBUNLDGS.CBL** | -- | DB unload to GSAM | IMS/GSAM |
| 7 | **PAUDBLOD.CBL** | -- | Load auth DB | IMS DB |
| 8 | **PAUDBUNL.CBL** | -- | Unload auth DB | IMS DB |

**Copybooks:** CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB
**BMS Maps:** COPAU00, COPAU01
**JCL Jobs:** CBPAUP0J, DBPAUTP0, LOADPADB, UNLDGSAM, UNLDPADB

### 5.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Description | Technologies |
|---|---------|-------------|--------------|
| 9 | **COTRTUPC.cbl** | Transaction type add/edit | CICS/DB2 |
| 10 | **COTRTLIC.cbl** | Transaction type list/delete | CICS/DB2 |
| 11 | **COBTUPDT.cbl** | Batch transaction type update | Batch/DB2 |

**Copybooks:** CSDB2RPY, CSDB2RWY
**BMS Maps:** COTRTLI, COTRTUP
**JCL Jobs:** CREADB21, MNTTRDB2, TRANEXTR

### 5.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Description | Technologies |
|---|---------|-------------|--------------|
| 12 | **CODATE01.cbl** | System date via MQ request/response | CICS/MQ |
| 13 | **COACCT01.cbl** | Account inquiry via MQ | CICS/MQ |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Purpose |
|---|---------|---------|
| 1 | **COBDATFT.asm** | Date formatting (called by CBACT01C) |
| 2 | **MVSWAIT.asm** | MVS wait utility (called by COBSWAIT) |

---

## 7. Supporting Artifacts

| Category | Path | Count | Description |
|----------|------|------:|-------------|
| JCL Procedures | `app/proc/` | 2 | REPROC.prc, TRANREPT.prc |
| Scheduler Configs | `app/scheduler/` | 2 | CA7 + Control-M definitions |
| CSD Definitions | `app/csd/` | 1 | CARDDEMO.CSD (CICS resource defs) |
| Control Files | `app/ctl/` | varies | VSAM control files |
| Catalog Listings | `app/catlg/` | varies | VSAM catalog listings |
| Macros | `app/maclib/` | varies | Assembler macros |
| ASCII Data | `app/data/ASCII/` | varies | Test data (ASCII format) |
| EBCDIC Data | `app/data/EBCDIC/` | varies | Test data (EBCDIC format) |
| Shell Scripts | `scripts/` | 10+ | FTP-based mainframe interaction |
| Diagrams | `diagrams/` | varies | Architecture diagrams & screenshots |

---

## 8. Summary Statistics

| Artifact Type | Core Count | Optional Count | Total |
|---------------|----------:|---------------:|------:|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks | 30 | 11 | **41** |
| BMS Maps | 17 | 4 | **21** |
| BMS Copybooks | 17 | 4 | **21** |
| JCL Jobs | 38 | 8 | **46** |
| ASM Programs | 2 | 0 | **2** |
| JCL Procedures | 2 | 0 | **2** |
| **Total LOC (core COBOL)** | | | **~20,650** |

### Domain Distribution

| Business Domain | Program Count | % of Total |
|-----------------|-------------:|------------|
| Account Management | 6 | 14% |
| Card Management | 6 | 14% |
| Transaction Processing | 8 | 18% |
| Customer Management | 2 | 5% |
| User/Security Admin | 5 | 11% |
| Reporting & Statements | 4 | 9% |
| Data Migration (ETL) | 2 | 5% |
| Shared Utilities | 2 | 5% |
| Navigation/Menus | 2 | 5% |
| Financial Calculations | 1 | 2% |
| Authorization (Optional) | 8 | 18% (opt) |
| DB2 Trans Types (Optional) | 3 | 7% (opt) |
| VSAM-MQ (Optional) | 2 | 5% (opt) |
