# Application Inventory -- CardDemo COBOL Codebase

> Generated from static analysis of the `app/` directory tree.
> Counts: **31 COBOL programs** | **30 copybooks** | **38 JCL jobs** | **17 BMS maps** | **2 ASM programs** | **2 JCL procedures** | **13 optional-module programs**

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online (CICS) Programs

| # | Program | Lines | CICS Txn | Purpose | VSAM Files Accessed | Classification |
|---|---------|-------|----------|---------|---------------------|----------------|
| 1 | COSGN00C | 260 | CC00 | Sign-on / Authentication | USRSEC (R) | Security / Entry Point |
| 2 | COMEN01C | 308 | CM00 | Main Menu (Regular User) | -- | Navigation |
| 3 | COADM01C | 288 | CA00 | Admin Menu | USRSEC (R) | Navigation / Admin |
| 4 | COACTVWC | 941 | CA01 | Account View | ACCTDAT (R), CUSTDAT (R), CXACAIX (R) | Account Mgmt |
| 5 | COACTUPC | 4236 | CA02 | Account Update | ACCTDAT (R/W), CUSTDAT (R/W), CXACAIX (R) | Account Mgmt |
| 6 | COCRDLIC | 1459 | CC01 | Credit Card List (Browse) | CARDDAT (R) | Card Mgmt |
| 7 | COCRDSLC | 887 | CC02 | Credit Card View (Detail) | CARDDAT (R), CUSTDAT (R) | Card Mgmt |
| 8 | COCRDUPC | 1560 | CC03 | Credit Card Update | CARDDAT (R/W) | Card Mgmt |
| 9 | COTRN00C | 699 | CT00 | Transaction List (Browse) | TRANSACT (R) | Transaction Mgmt |
| 10 | COTRN01C | 330 | CT01 | Transaction View (Detail) | TRANSACT (R) | Transaction Mgmt |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | TRANSACT (W), ACCTDAT (R), CCXREF (R), CXACAIX (R) | Transaction Mgmt |
| 12 | CORPT00C | 649 | CR00 | Transaction Reports (submit batch) | -- (submits JCL via TDQ) | Reporting |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | ACCTDAT (R/W), CXACAIX (R), TRANSACT (R/W) | Payments |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | USRSEC (R) | User Admin |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | USRSEC (W) | User Admin |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | USRSEC (R/W) | User Admin |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | USRSEC (R/D) | User Admin |

### 1.2 Batch Programs

| # | Program | Lines | Purpose | Files (Input) | Files (Output) | Classification |
|---|---------|-------|---------|---------------|----------------|----------------|
| 18 | CBTRN02C | 731 | Post daily transactions | DALYTRAN (seq) | TRANSACT (idx), DALYREJS (seq) ; also reads XREF, ACCTDAT, TCATBAL | Core Batch |
| 19 | CBACT04C | 652 | Interest calculation | TCATBAL (seq), XREF (rnd), ACCTDAT (rnd), DISCGRP (rnd) | TRANSACT (seq) | Core Batch |
| 20 | CBTRN03C | 649 | Transaction report generation | TRANSACT, XREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report) | Reporting |
| 21 | CBSTM03A | 924 | Statement generation (driver) | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | STMTFILE (text), HTMLFILE | Reporting |
| 22 | CBSTM03B | 230 | Statement generation (sub-pgm) | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | -- (called by CBSTM03A) | Reporting |
| 23 | CBTRN01C | 494 | Daily transaction validation | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANSACT | -- | Core Batch |
| 24 | CBACT01C | 430 | Account file reader / splitter | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE | Utility |
| 25 | CBACT02C | 178 | Card file reader | CARDFILE | -- (display) | Utility |
| 26 | CBACT03C | 178 | Cross-reference file reader | XREFFILE | -- (display) | Utility |
| 27 | CBCUS01C | 178 | Customer file reader | CUSTFILE | -- (display) | Utility |
| 28 | CBEXPORT | 582 | Multi-entity data export | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (seq) | Data Migration |
| 29 | CBIMPORT | 487 | Multi-entity data import | EXPFILE (seq) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | Data Migration |
| 30 | CSUTLDTC | 157 | Date conversion utility | -- | -- (callable) | Utility |
| 31 | COBSWAIT | 41 | Wait / sleep utility | -- | -- | Utility |

### 1.3 Assembler Programs (`app/asm/`)

| # | Program | Purpose | Classification |
|---|---------|---------|----------------|
| 1 | MVSWAIT.asm | MVS wait (stall) utility | System Utility |
| 2 | COBDATFT.asm | Date formatting utility | System Utility |

---

## 2. Optional Module Programs

### 2.1 Authorization -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | Lines | Purpose | Classification |
|---|---------|-------|---------|----------------|
| 1 | COPAUA0C | 1026 | MQ trigger monitor for authorization requests | Integration |
| 2 | COPAUS0C | 1032 | Pending Authorization Summary screen | Online / CICS |
| 3 | COPAUS1C | 604 | Pending Authorization Detail screen | Online / CICS |
| 4 | COPAUS2C | 244 | Fraud mark to DB2 | Online / CICS |
| 5 | CBPAUP0C | 386 | Batch purge of processed authorizations | Batch |
| 6 | PAUDBLOD | 369 | Load authorization data into DB2 | Batch / DB2 |
| 7 | DBUNLDGS | 366 | Unload DB2 data to sequential file | Batch / DB2 |
| 8 | PAUDBUNL | 317 | Unload authorization DB2 table | Batch / DB2 |

### 2.2 Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program | Lines | Purpose | Classification |
|---|---------|-------|---------|----------------|
| 1 | COTRTLIC | 2098 | Transaction type list / delete (DB2 cursors) | Online / CICS / DB2 |
| 2 | COTRTUPC | 1702 | Transaction type add / update (DB2) | Online / CICS / DB2 |
| 3 | COBTUPDT | 237 | Batch transaction type update | Batch / DB2 |

### 2.3 VSAM-MQ (`app/app-vsam-mq/cbl/`)

| # | Program | Lines | Purpose | Classification |
|---|---------|-------|---------|----------------|
| 1 | COACCT01 | 620 | MQ request/response for account inquiry | Integration / MQ |
| 2 | CODATE01 | 524 | MQ request/response for system date | Integration / MQ |

---

## 3. Copybooks (`app/cpy/`)

| # | Copybook | Lines | Record Len | Purpose | Classification |
|---|----------|-------|------------|---------|----------------|
| 1 | COCOM01Y | 47 | ~150 | COMMAREA -- inter-program communication area | Shared / Navigation |
| 2 | COMEN02Y | 101 | -- | Main menu option definitions (11 options) | Configuration |
| 3 | COADM02Y | 62 | -- | Admin menu option definitions (6 options) | Configuration |
| 4 | COTTL01Y | 27 | -- | Screen title / header constants | UI |
| 5 | CSMSG01Y | 24 | -- | Common messages (thank-you, invalid-key) | UI |
| 6 | CSMSG02Y | 35 | -- | Abend (abnormal end) data structure | Error Handling |
| 7 | CSDAT01Y | 58 | -- | Date/time working-storage structure | Utility |
| 8 | CSUSR01Y | 26 | 80 | User security record (USRSEC) | Security |
| 9 | CVACT01Y | 20 | 300 | Account record layout | Core Entity |
| 10 | CVACT02Y | 14 | 150 | Card record layout | Core Entity |
| 11 | CVACT03Y | 11 | 50 | Card cross-reference record | Core Entity |
| 12 | CVCUS01Y | 26 | 500 | Customer record layout | Core Entity |
| 13 | CUSTREC | 26 | 500 | Customer record (alternate layout) | Core Entity |
| 14 | CVTRA05Y | 21 | 350 | Online transaction record | Core Entity |
| 15 | CVTRA06Y | 21 | 350 | Daily transaction record (batch input) | Core Entity |
| 16 | CVTRA01Y | 13 | 50 | Transaction category balance record | Core Entity |
| 17 | CVTRA02Y | 13 | 50 | Disclosure group / interest rate record | Core Entity |
| 18 | CVTRA03Y | 10 | 60 | Transaction type record | Reference Data |
| 19 | CVTRA04Y | 12 | 60 | Transaction category type record | Reference Data |
| 20 | CVTRA07Y | 73 | -- | Transaction report layout (headers, detail, totals) | Reporting |
| 21 | COSTM01 | 37 | -- | Statement transaction layout (card-num keyed) | Reporting |
| 22 | CVCRD01Y | 46 | -- | Card work areas and AID key definitions | UI / Navigation |
| 23 | CVEXPORT | 103 | 500 | Multi-record export layout (customer/account/transaction/card/xref) | Data Migration |
| 24 | CSLKPCDY | 1318 | -- | Lookup code tables (large reference data) | Reference Data |
| 25 | CSUTLDPY | 375 | -- | Utility data structures (date parsing) | Utility |
| 26 | CSUTLDWY | 89 | -- | Utility working-storage (date routines) | Utility |
| 27 | CSSTRPFY | 85 | -- | String formatting paragraph (COPY REPLACING) | Utility |
| 28 | CSSETATY | 30 | -- | Set attribute byte macro (COPY REPLACING) | UI |
| 29 | CODATECN | 52 | -- | Date conversion constants | Utility |
| 30 | UNUSED1Y | 10 | -- | Placeholder / unused copybook | Unused |

---

## 4. BMS Maps (`app/bms/`)

| # | Map | Screen Name | Associated Program | Purpose |
|---|-----|------------|-------------------|---------|
| 1 | COSGN00.bms | COSGN0A | COSGN00C | Sign-on screen |
| 2 | COMEN01.bms | COMEN1A | COMEN01C | Main menu screen |
| 3 | COADM01.bms | COADM1A | COADM01C | Admin menu screen |
| 4 | COACTVW.bms | CACTVWA | COACTVWC | Account view screen |
| 5 | COACTUP.bms | CACTUPA | COACTUPC | Account update screen |
| 6 | COCRDLI.bms | CCRDLIA | COCRDLIC | Card list screen |
| 7 | COCRDSL.bms | CCRDSLA | COCRDSLC | Card detail screen |
| 8 | COCRDUP.bms | CCRDUPA | COCRDUPC | Card update screen |
| 9 | COTRN00.bms | COTRN0A | COTRN00C | Transaction list screen |
| 10 | COTRN01.bms | COTRN1A | COTRN01C | Transaction detail screen |
| 11 | COTRN02.bms | COTRN2A | COTRN02C | Transaction add screen |
| 12 | CORPT00.bms | CORPT0A | CORPT00C | Report request screen |
| 13 | COBIL00.bms | COBIL0A | COBIL00C | Bill payment screen |
| 14 | COUSR00.bms | COUSR0A | COUSR00C | User list screen |
| 15 | COUSR01.bms | COUSR1A | COUSR01C | User add screen |
| 16 | COUSR02.bms | COUSR2A | COUSR02C | User update screen |
| 17 | COUSR03.bms | COUSR3A | COUSR03C | User delete screen |

BMS-generated copybooks in `app/cpy-bms/` mirror each map (17 files).

---

## 5. JCL Jobs (`app/jcl/`)

### 5.1 Data Loading / Refresh Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 1 | ACCTFILE.jcl | IDCAMS REPRO | Refresh account master VSAM from sequential | Data Load |
| 2 | CARDFILE.jcl | IDCAMS REPRO | Refresh card master VSAM from sequential | Data Load |
| 3 | CUSTFILE.jcl | IDCAMS REPRO | Refresh customer master VSAM | Data Load |
| 4 | XREFFILE.jcl | IDCAMS REPRO | Load card cross-reference VSAM | Data Load |
| 5 | TRANFILE.jcl | IDCAMS REPRO | Load transaction master VSAM | Data Load |
| 6 | DUSRSECJ.jcl | IDCAMS REPRO | Load user security VSAM | Data Load |
| 7 | DISCGRP.jcl | IDCAMS REPRO | Load disclosure group VSAM | Data Load |
| 8 | TCATBALF.jcl | IDCAMS REPRO | Load transaction category balance VSAM | Data Load |
| 9 | TRANTYPE.jcl | IDCAMS REPRO | Load transaction type VSAM | Data Load |
| 10 | TRANCATG.jcl | IDCAMS REPRO | Load transaction category VSAM | Data Load |
| 11 | DEFCUST.jcl | IDCAMS DEFINE | Define customer VSAM cluster | Data Definition |
| 12 | DALYREJS.jcl | IDCAMS DEFINE | Define daily rejects file | Data Definition |
| 13 | PRTCATBL.jcl | IDCAMS PRINT | Print category balance records | Utility |
| 14 | ESDSRRDS.jcl | IDCAMS DEFINE | Define ESDS/RRDS datasets | Data Definition |
| 15 | REPTFILE.jcl | IDCAMS DEFINE | Define report output file | Data Definition |

### 5.2 Core Batch Processing Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 16 | POSTTRAN.jcl | CBTRN02C | Post daily transactions to master | Core Batch |
| 17 | INTCALC.jcl | CBACT04C | Calculate interest on balances | Core Batch |
| 18 | COMBTRAN.jcl | SORT + IDCAMS | Combine/merge transaction files | Core Batch |
| 19 | CREASTMT.JCL | SORT + CBSTM03A | Generate account statements (text + HTML) | Reporting |
| 20 | TRANREPT.jcl | CBTRN03C | Generate transaction report | Reporting |
| 21 | TRANBKP.jcl | IDCAMS REPRO | Backup transaction VSAM to sequential | Backup |
| 22 | TRANIDX.jcl | IDCAMS DEFINE AIX | Define/build alternate index on transactions | Index Mgmt |

### 5.3 File Management Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 23 | CLOSEFIL.jcl | CICS CLOSE | Close CICS files for batch window | Operations |
| 24 | OPENFIL.jcl | CICS OPEN | Open CICS files after batch window | Operations |
| 25 | DEFGDGB.jcl | IDCAMS DEFINE GDG | Define GDG base for backups | Data Definition |
| 26 | DEFGDGD.jcl | IDCAMS DEFINE GDG | Define GDG base for daily files | Data Definition |

### 5.4 Data Export / Import Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 27 | CBEXPORT.jcl | CBEXPORT | Export all entities to sequential file | Data Migration |
| 28 | CBIMPORT.jcl | CBIMPORT | Import entities from sequential file | Data Migration |

### 5.5 Read / Diagnostic Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 29 | READACCT.jcl | CBACT01C | Read and display account records | Diagnostic |
| 30 | READCARD.jcl | CBACT02C | Read and display card records | Diagnostic |
| 31 | READCUST.jcl | CBCUS01C | Read and display customer records | Diagnostic |
| 32 | READXREF.jcl | CBACT03C | Read and display cross-reference records | Diagnostic |

### 5.6 Utility / Infrastructure Jobs

| # | Job | Executes | Purpose | Classification |
|---|-----|----------|---------|----------------|
| 33 | CBADMCDJ.jcl | CBTRN01C | Admin card job (daily validation) | Utility |
| 34 | WAITSTEP.jcl | COBSWAIT/MVSWAIT | Wait step (job scheduling) | Utility |
| 35 | INTRDRJ1.JCL | SORT | Internal reader job 1 (report prep) | Reporting |
| 36 | INTRDRJ2.JCL | SORT | Internal reader job 2 (report prep) | Reporting |
| 37 | TXT2PDF1.JCL | IEBGENER | Convert text report to PDF format | Utility |
| 38 | FTPJCL.JCL | FTP | FTP data transfer to/from mainframe | Utility |

---

## 6. JCL Procedures (`app/proc/`)

| # | Procedure | Purpose |
|---|-----------|---------|
| 1 | REPROC.prc | Reprocessing procedure (resubmit failed steps) |
| 2 | TRANREPT.prc | Transaction report procedure (called by TRANREPT.jcl) |

---

## 7. Scheduler Configurations (`app/scheduler/`)

| # | File | Purpose |
|---|------|---------|
| 1 | CardDemo.ca7 | CA-7 job scheduling definitions |
| 2 | CardDemo.controlm | Control-M job scheduling definitions |

---

## 8. Summary Statistics

| Category | Count | Total Lines |
|----------|-------|-------------|
| Online CICS Programs | 17 | ~13,300 |
| Batch Programs | 14 | ~5,300 |
| Assembler Programs | 2 | ~200 |
| Optional Module Programs | 13 | ~9,100 |
| Copybooks | 30 | ~2,600 |
| BMS Maps | 17 | -- |
| JCL Jobs | 38 | -- |
| JCL Procedures | 2 | -- |
| **Grand Total Artifacts** | **133** | **~30,500** |
