# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Platform:** IBM Mainframe (z/OS) | **Runtime:** CICS/VSAM/JCL Batch

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **31 COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **17 BMS-generated copybooks**, and **38 JCL batch jobs** in the core module, plus **13 additional programs** across three optional extension modules. The application supports two user roles (Regular and Admin) and covers account management, card management, transactions, bill payments, reporting, and user administration.

---

## 1. COBOL Programs (31 Core + 13 Optional)

### 1.1 Online CICS Programs (17)

| # | Program | LOC | CICS Tran | Function | Business Domain | Screen Map |
|---|---------|-----|-----------|----------|-----------------|------------|
| 1 | COSGN00C | 261 | CC00 | Signon / Authentication | Security | COSGN00 |
| 2 | COMEN01C | 309 | CM00 | Main Menu (Regular Users) | Navigation | COMEN01 |
| 3 | COADM01C | 288 | CA00 | Admin Menu (Admin Users) | Navigation | COADM01 |
| 4 | COACTVWC | 942 | CAVW | Account View | Account Mgmt | COACTVW |
| 5 | COACTUPC | 4,237 | CAUP | Account Update | Account Mgmt | COACTUP |
| 6 | COCRDLIC | 1,460 | CCLI | Credit Card List | Card Mgmt | COCRDLI |
| 7 | COCRDSLC | 888 | CCDL | Credit Card Detail View | Card Mgmt | COCRDSL |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | COCRDUP |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transactions | COTRN00 |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transactions | COTRN01 |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transactions | COTRN02 |
| 12 | CORPT00C | 649 | CR00 | Transaction Report Submission | Reporting | CORPT00 |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing | COBIL00 |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | User Admin | COUSR00 |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | User Admin | COUSR01 |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | User Admin | COUSR02 |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | User Admin | COUSR03 |

### 1.2 Batch Programs (13)

| # | Program | LOC | Function | Business Domain | Key Files |
|---|---------|-----|----------|-----------------|-----------|
| 1 | CBACT01C | 430 | Read account file, write to output files | Account Mgmt | ACCTFILE |
| 2 | CBACT02C | 178 | Read and print card data file | Card Mgmt | CARDFILE |
| 3 | CBACT03C | 178 | Read and print account cross-reference data | Card Mgmt | XREFFILE |
| 4 | CBACT04C | 652 | Interest calculation on accounts | Financial Calc | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT |
| 5 | CBCUS01C | 178 | Read and print customer data file | Customer Mgmt | CUSTFILE |
| 6 | CBTRN01C | 494 | Post records from daily transaction file (validation) | Transaction Processing | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE |
| 7 | CBTRN02C | 731 | Post records from daily transaction file (posting) | Transaction Processing | DALYTRAN, TRANFILE, XREFFILE, DALYREJS, ACCTFILE, TCATBALF |
| 8 | CBTRN03C | 649 | Print transaction detail report | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, TRANREPT, DATEPARM |
| 9 | CBSTM03A | 924 | Print account statements from transaction data | Statement Generation | STMTFILE, HTMLFILE |
| 10 | CBSTM03B | 230 | File processing related to transaction report | Statement Generation | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE |
| 11 | CBEXPORT | 582 | Export customer data for branch migration | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE |
| 12 | CBIMPORT | 487 | Import customer data from branch migration export | Data Migration | EXPFILE, CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |
| 13 | COBSWAIT | 41 | Utility: wait (parm in centiseconds) | Utility | N/A |

### 1.3 Shared Utility Program (1)

| # | Program | LOC | Function | Business Domain |
|---|---------|-----|----------|-----------------|
| 1 | CSUTLDTC | 157 | Date validation utility (calls CEEDAYS) | Utility |

### 1.4 Optional Module: Authorization (IMS/DB2/MQ) -- 8 Programs

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 1 | COPAUA0C | MQ trigger for authorization | CICS/MQ |
| 2 | COPAUS0C | Authorization summary display | CICS/IMS-DB |
| 3 | COPAUS1C | Authorization details display | CICS/IMS-DB |
| 4 | COPAUS2C | Fraud marking to DB2 | CICS/DB2 |
| 5 | CBPAUP0C | Batch purge of authorizations | Batch/DB2 |
| 6 | PAUDBLOD | IMS database load utility | Batch/IMS |
| 7 | PAUDBUNL | IMS database unload utility | Batch/IMS |
| 8 | DBUNLDGS | IMS database unload (GS calls) | Batch/IMS |

### 1.5 Optional Module: Transaction Type DB2 -- 3 Programs

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 1 | COTRTUPC | Add/edit transaction types | CICS/DB2 |
| 2 | COTRTLIC | List/delete transaction types | CICS/DB2 |
| 3 | COBTUPDT | Batch update transaction types | Batch/DB2 |

### 1.6 Optional Module: VSAM-MQ -- 2 Programs

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 1 | CODATE01 | MQ request/response for system date | CICS/MQ |
| 2 | COACCT01 | MQ request/response for account inquiry | CICS/MQ |

---

## 2. Copybooks (30 Core)

### 2.1 Data Record Layouts (Business Entity Copybooks)

| # | Copybook | Record Length | Description | Used By |
|---|----------|--------------|-------------|---------|
| 1 | CVACT01Y | 300 bytes | Account master record | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COTRN02C |
| 2 | CVACT02Y | 150 bytes | Card data record | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC |
| 3 | CVACT03Y | ~50 bytes | Card cross-reference record | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COTRN02C |
| 4 | CVCUS01Y | 500 bytes | Customer master record | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC |
| 5 | CVTRA01Y | 50 bytes | Transaction category balance | CBACT04C, CBTRN02C |
| 6 | CVTRA02Y | 50 bytes | Disclosure group record | CBACT04C |
| 7 | CVTRA03Y | 60 bytes | Transaction type record | CBTRN03C |
| 8 | CVTRA04Y | 60 bytes | Transaction category type | CBTRN03C |
| 9 | CVTRA05Y | 350 bytes | Online transaction record | CBACT04C, CBEXPORT, CBIMPORT, CBTRN02C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C |
| 10 | CVTRA06Y | 350 bytes | Daily transaction record | CBTRN01C, CBTRN02C |
| 11 | CVTRA07Y | Variable | Transaction report layout | CBTRN03C |
| 12 | CVCRD01Y | Variable | Credit card work area | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 13 | CUSTREC | Variable | Customer record (statement) | CBSTM03A |
| 14 | COSTM01 | Variable | Transaction altered layout (reporting) | CBSTM03A |
| 15 | CVEXPORT | Variable | Export/import record layout | CBEXPORT, CBIMPORT |
| 16 | CSUSR01Y | ~80 bytes | User security record | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00-03C |
| 17 | UNUSED1Y | ~80 bytes | Unused data placeholder | None (deprecated) |

### 2.2 Application Infrastructure Copybooks

| # | Copybook | Description | Used By |
|---|----------|-------------|---------|
| 18 | COCOM01Y | Common communication area (COMMAREA) | All 17 online programs |
| 19 | COTTL01Y | Screen title/header definitions | All 17 online programs |
| 20 | CSDAT01Y | Current date/time work fields | All 17 online programs |
| 21 | CSMSG01Y | Common user messages | All 17 online programs |
| 22 | CSMSG02Y | Abend/error message variables | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| 23 | COADM02Y | Admin menu option definitions | COADM01C |
| 24 | COMEN02Y | Regular user menu option definitions | COMEN01C |
| 25 | CSLKPCDY | Lookup code definitions | COACTUPC |
| 26 | CSSETATY | Screen attribute setting (REPLACING) | COACTUPC |
| 27 | CSSTRPFY | String processing functions | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC |
| 28 | CSUTLDPY | Date utility parameter structure | COACTUPC |
| 29 | CSUTLDWY | Date edit work variables | COACTUPC |
| 30 | CODATECN | Date conversion parameters | CBACT01C |

---

## 3. BMS Screen Maps (17)

| # | Map | Lines | Screen Title | Associated Program | Function |
|---|-----|-------|-------------|-------------------|----------|
| 1 | COSGN00.bms | 210 | Login Screen | COSGN00C | User authentication |
| 2 | COMEN01.bms | 167 | Main Menu | COMEN01C | Regular user navigation |
| 3 | COADM01.bms | 167 | Admin Menu | COADM01C | Admin user navigation |
| 4 | COACTVW.bms | 378 | Account Viewer | COACTVWC | Display account details |
| 5 | COACTUP.bms | 512 | Account Update | COACTUPC | Edit account fields |
| 6 | COCRDLI.bms | 344 | Card Listing | COCRDLIC | List credit cards |
| 7 | COCRDSL.bms | 157 | Card Selection/Detail | COCRDSLC | View card details |
| 8 | COCRDUP.bms | 172 | Card Update | COCRDUPC | Edit card fields |
| 9 | COTRN00.bms | 464 | Transaction List | COTRN00C | List transactions |
| 10 | COTRN01.bms | 273 | Transaction View | COTRN01C | View transaction detail |
| 11 | COTRN02.bms | 307 | Transaction Add | COTRN02C | Add new transaction |
| 12 | CORPT00.bms | 231 | Report Selection | CORPT00C | Select/submit reports |
| 13 | COBIL00.bms | 141 | Bill Payment | COBIL00C | Pay account balance |
| 14 | COUSR00.bms | 463 | List Users | COUSR00C | Admin user list |
| 15 | COUSR01.bms | 164 | Add User | COUSR01C | Add new user |
| 16 | COUSR02.bms | 169 | Update User | COUSR02C | Update existing user |
| 17 | COUSR03.bms | 153 | Delete User | COUSR03C | Delete user |

### BMS-Generated Copybooks (17)

Each BMS map generates a corresponding copybook in `app/cpy-bms/` used by the COBOL program to reference screen fields:
COACTUP.CPY, COACTVW.CPY, COADM01.CPY, COBIL00.CPY, COCRDLI.CPY, COCRDSL.CPY, COCRDUP.CPY, COMEN01.CPY, CORPT00.CPY, COSGN00.CPY, COTRN00.CPY, COTRN01.CPY, COTRN02.CPY, COUSR00.CPY, COUSR01.CPY, COUSR02.CPY, COUSR03.CPY

---

## 4. JCL Batch Jobs (38)

### 4.1 Data File Management (Define/Load/Refresh)

| # | JCL Job | Description | Key Datasets |
|---|---------|-------------|--------------|
| 1 | ACCTFILE.jcl | Refresh account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE.jcl | Refresh card master VSAM | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE.jcl | Refresh customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE.jcl | Define/load card cross-reference | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE.jcl | Define/load transaction master | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ.jcl | Load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | DEFCUST.jcl | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |
| 8 | DEFGDGB.jcl | Define GDG base datasets | Various GDG bases |
| 9 | DEFGDGD.jcl | Define GDG data datasets | Various GDG datasets |
| 10 | TRANTYPE.jcl | Define/load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 11 | TRANCATG.jcl | Define/load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 12 | REPTFILE.jcl | Define report file | Report datasets |
| 13 | TCATBALF.jcl | Define/load transaction category balance | TCATBALF.VSAM.KSDS |
| 14 | ESDSRRDS.jcl | Define ESDS/RRDS datasets | Various ESDS/RRDS |
| 15 | READACCT.jcl | Read/print account file | ACCTDATA |
| 16 | READCARD.jcl | Read/print card file | CARDDATA |
| 17 | READCUST.jcl | Read/print customer file | CUSTDATA |
| 18 | READXREF.jcl | Read/print cross-reference file | CARDXREF |
| 19 | PRTCATBL.jcl | Print category balance file | TCATBALF |

### 4.2 Batch Processing Jobs

| # | JCL Job | Description | Executes Program |
|---|---------|-------------|-----------------|
| 20 | POSTTRAN.jcl | Core transaction posting cycle | CBTRN01C, CBTRN02C |
| 21 | INTCALC.jcl | Interest calculation | CBACT04C |
| 22 | COMBTRAN.jcl | Combine daily transactions | SORT/MERGE |
| 23 | CREASTMT.JCL | Create account statements | CBSTM03A |
| 24 | TRANBKP.jcl | Backup transaction master | IDCAMS REPRO |
| 25 | TRANIDX.jcl | Define alternate index on transactions | IDCAMS |
| 26 | TRANREPT.jcl | Generate transaction report | CBTRN03C |
| 27 | CBEXPORT.jcl | Export data for migration | CBEXPORT |
| 28 | CBIMPORT.jcl | Import data from migration | CBIMPORT |
| 29 | DALYREJS.jcl | Process daily rejects | IDCAMS |
| 30 | DISCGRP.jcl | Define disclosure group | IDCAMS |
| 31 | WAITSTEP.jcl | Wait utility step | COBSWAIT |
| 32 | CBADMCDJ.jcl | Admin card job | IDCAMS |

### 4.3 CICS File Operations

| # | JCL Job | Description |
|---|---------|-------------|
| 33 | CLOSEFIL.jcl | Close CICS files for batch processing |
| 34 | OPENFIL.jcl | Open CICS files after batch processing |

### 4.4 Utility / Infrastructure Jobs

| # | JCL Job | Description |
|---|---------|-------------|
| 35 | FTPJCL.JCL | FTP file transfer |
| 36 | INTRDRJ1.JCL | Internal reader job 1 (chain submission) |
| 37 | INTRDRJ2.JCL | Internal reader job 2 |
| 38 | TXT2PDF1.JCL | Convert text to PDF (statements) |

### 4.5 Standard Batch Cycle Order

```
CLOSEFIL -> ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE (data refresh)
         -> POSTTRAN (transaction posting)
         -> INTCALC  (interest calculation)
         -> TRANBKP  (backup transactions)
         -> COMBTRAN (combine transactions)
         -> CREASTMT (create statements)
         -> TRANIDX  (rebuild alternate indexes)
         -> OPENFIL  (reopen CICS files)
```

---

## 5. Other Artifacts

### 5.1 Assembler Programs (2)
| Program | Location | Function |
|---------|----------|----------|
| MVSWAIT | app/asm/ | Low-level wait routine (called by COBSWAIT) |
| COBDATFT | app/asm/ | Date format conversion routine (called by CBACT01C) |

### 5.2 JCL Procedures (2)
| Procedure | Location | Function |
|-----------|----------|----------|
| REPROC | app/proc/ | Reusable REPRO procedure for VSAM copy |
| (other) | app/proc/ | Compilation/link-edit procedure |

### 5.3 CICS Resource Definitions
| File | Location | Function |
|------|----------|----------|
| CARDDEMO.CSD | app/csd/ | CICS system definition for all transactions, programs, files |

### 5.4 Data Files
| Directory | Format | Description |
|-----------|--------|-------------|
| app/data/ASCII/ | ASCII | Sample data for local testing |
| app/data/EBCDIC/ | EBCDIC | Sample data for mainframe upload |

---

## 6. Summary Statistics

| Category | Count |
|----------|-------|
| **Core COBOL Programs** | 31 |
| **Optional Module Programs** | 13 |
| **Total COBOL Programs** | **44** |
| **Core Copybooks** | 30 |
| **BMS Screen Maps** | 17 |
| **BMS-Generated Copybooks** | 17 |
| **JCL Batch Jobs** | 38 |
| **Assembler Programs** | 2 |
| **JCL Procedures** | 2 |
| **Total Lines of COBOL** | ~20,700 (core) |
| **CICS Transactions** | 17 |
| **VSAM Datasets** | 10+ |
