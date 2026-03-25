# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Platform:** IBM Mainframe (z/OS) | **Runtime:** CICS/VSAM/JCL Batch

---

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL, CICS, VSAM, and JCL. It provides online transaction processing for card/account management and batch processing for financial operations. The codebase contains **31 COBOL programs**, **30 copybooks**, **38 JCL jobs**, **17 BMS screen maps**, **17 BMS-generated copybooks**, **2 assembler utilities**, **2 JCL procedures**, and **3 optional extension modules** (13 additional programs).

**Two user roles:** Regular User (card operations) and Admin (user/transaction type management).
**Login Transaction:** CC00 | **Admin:** ADMIN001/PASSWORD | **User:** USER0001/PASSWORD

---

## 1. COBOL Programs (31 core + 13 optional)

### 1.1 Online CICS Programs (20 core)

| # | Program | Lines | CICS Trans | Function | Domain | BMS Map | Complexity |
|---|---------|-------|------------|----------|--------|---------|------------|
| 1 | COSGN00C | 261 | CC00 | Signon / Authentication | Security | COSGN00 | Low |
| 2 | COMEN01C | 309 | CM00 | Main Menu (Regular Users) | Navigation | COMEN01 | Low |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation | COADM01 | Low |
| 4 | COACTVWC | 942 | CAVW | Account View | Account Mgmt | COACTVW | Medium |
| 5 | COACTUPC | 4,237 | CAUP | Account Update | Account Mgmt | COACTUP | Very High |
| 6 | COCRDLIC | 1,460 | CCLI | Credit Card List | Card Mgmt | COCRDLI | High |
| 7 | COCRDSLC | 888 | CCDL | Credit Card Detail View | Card Mgmt | COCRDSL | Medium |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | COCRDUP | High |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transaction Mgmt | COTRN00 | Medium |
| 10 | COTRN01C | 330 | CT01 | Transaction View | Transaction Mgmt | COTRN01 | Low |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transaction Mgmt | COTRN02 | Medium |
| 12 | CORPT00C | 649 | CR00 | Transaction Report (submit batch) | Reporting | CORPT00 | Medium |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing | COBIL00 | Medium |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | User Admin | COUSR00 | Medium |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | User Admin | COUSR01 | Low |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | User Admin | COUSR02 | Low |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | User Admin | COUSR03 | Low |
| 18 | CSUTLDTC | 157 | -- | Date Utility (called subroutine) | Utility | -- | Low |

### 1.2 Batch Programs (13 core)

| # | Program | Lines | Function | Domain | Input Files | Output Files | Complexity |
|---|---------|-------|----------|--------|-------------|--------------|------------|
| 1 | CBACT01C | 430 | Read account file, write outputs | Account Mgmt | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE | Medium |
| 2 | CBACT02C | 178 | Read and print card data | Card Mgmt | CARDFILE | SYSOUT | Low |
| 3 | CBACT03C | 178 | Read and print cross-reference data | Card Mgmt | XREFFILE | SYSOUT | Low |
| 4 | CBACT04C | 652 | Interest calculation | Financial | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TRANSACT (updated) | High |
| 5 | CBCUS01C | 178 | Read and print customer data | Customer Mgmt | CUSTFILE | SYSOUT | Low |
| 6 | CBTRN01C | 494 | Post daily transactions (validate) | Transaction Processing | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | Various | Medium |
| 7 | CBTRN02C | 731 | Post daily transactions (core posting) | Transaction Processing | DALYTRAN, TRANFILE, XREFFILE, ACCTFILE | DALYREJS, TRANFILE (updated) | High |
| 8 | CBTRN03C | 649 | Print transaction detail report | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRANCATG | TRANREPT | High |
| 9 | CBSTM03A | 924 | Statement generation (main) | Reporting | XREFFILE, CUSTFILE, ACCTFILE | STMTFILE, HTMLFILE | High |
| 10 | CBSTM03B | 230 | Statement generation (sub-program) | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | -- (returns data) | Medium |
| 11 | CBEXPORT | 582 | Data export (all files to flat file) | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPORTFILE | Medium |
| 12 | CBIMPORT | 487 | Data import (flat file to all files) | Data Migration | EXPORTFILE | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | Medium |
| 13 | COBSWAIT | 41 | Wait utility (parm in centiseconds) | Utility | -- | -- | Trivial |

### 1.3 Optional Module Programs (13)

#### Authorization Module (IMS/DB2/MQ) -- `app/app-authorization-ims-db2-mq/`

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 1 | COPAUA0C | MQ trigger for authorization requests | CICS/MQ |
| 2 | COPAUS0C | Authorization summary screen | CICS/BMS |
| 3 | COPAUS1C | Authorization detail screen | CICS/BMS |
| 4 | COPAUS2C | Fraud marking to DB2 | CICS/DB2 |
| 5 | CBPAUP0C | Batch purge of authorization records | Batch/DB2 |
| 6 | PAUDBLOD | IMS DB load utility | Batch/IMS |
| 7 | PAUDBUNL | IMS DB unload utility | Batch/IMS |
| 8 | DBUNLDGS | IMS DB unload (GS calls) | Batch/IMS |

#### Transaction Type DB2 Module -- `app/app-transaction-type-db2/`

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 9 | COTRTUPC | Transaction type add/edit (CRUD) | CICS/DB2 |
| 10 | COTRTLIC | Transaction type list/delete | CICS/DB2 |
| 11 | COBTUPDT | Batch update of transaction types | Batch/DB2 |

#### VSAM-MQ Module -- `app/app-vsam-mq/`

| # | Program | Function | Technology |
|---|---------|----------|------------|
| 12 | CODATE01 | MQ request/response for system date | CICS/MQ |
| 13 | COACCT01 | MQ request/response for account inquiry | CICS/MQ |

---

## 2. Copybooks (30 core + 17 BMS-generated)

### 2.1 Data Structure Copybooks

| # | Copybook | Lines | Record Len | Business Entity | Used By |
|---|----------|-------|------------|----------------|---------|
| 1 | CVACT01Y | 24 | 300 bytes | Account Master Record | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COTRN02C, COBIL00C |
| 2 | CVACT02Y | 16 | 150 bytes | Card Data Record | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| 3 | CVACT03Y | 15 | 50 bytes | Card Cross-Reference Record | COACTVWC, COACTUPC, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT, COTRN02C, COBIL00C |
| 4 | CVCUS01Y | 40 | 500 bytes | Customer Master Record | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, CBSTM03A |
| 5 | CVTRA01Y | 13 | 50 bytes | Transaction Category Balance | CBACT04C, CBTRN02C |
| 6 | CVTRA02Y | 13 | 50 bytes | Disclosure Group Record | CBACT04C |
| 7 | CVTRA03Y | 10 | 60 bytes | Transaction Type Record | CBTRN03C |
| 8 | CVTRA04Y | 12 | 60 bytes | Transaction Category Type | CBTRN03C |
| 9 | CVTRA05Y | 21 | 350 bytes | Transaction Record (online) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| 10 | CVTRA06Y | 21 | 350 bytes | Daily Transaction Record | CBTRN01C, CBTRN02C |
| 11 | CVTRA07Y | 73 | -- | Transaction Report Layout | CBTRN03C |
| 12 | CVCRD01Y | -- | -- | Card Work Area | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 13 | CSUSR01Y | 10 | 80 bytes | User Security Record | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C |
| 14 | CUSTREC | -- | -- | Customer Record (alternate layout) | CBSTM03A |
| 15 | CVEXPORT | -- | -- | Export File Record Layout | CBEXPORT, CBIMPORT |
| 16 | COSTM01 | 38 | -- | Statement Transaction Layout | CBSTM03A |
| 17 | UNUSED1Y | 10 | 80 bytes | Unused placeholder record | -- |

### 2.2 Application/Framework Copybooks

| # | Copybook | Lines | Purpose | Used By |
|---|----------|-------|---------|---------|
| 18 | COCOM01Y | ~80 | Common Communication Area (COMMAREA) | All online CICS programs |
| 19 | COMEN02Y | ~50 | Menu Option Definitions (Regular) | COMEN01C |
| 20 | COADM02Y | ~30 | Admin Menu Option Definitions | COADM01C |
| 21 | COTTL01Y | ~15 | Screen Title/Header Constants | All online CICS programs |
| 22 | CSDAT01Y | ~20 | Current Date/Time Working Storage | All online CICS programs |
| 23 | CSMSG01Y | ~15 | Common Application Messages | All online CICS programs |
| 24 | CSMSG02Y | ~10 | Abend Handling Variables | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| 25 | CSSETATY | ~20 | BMS Field Attribute Setting (REPLACING) | COACTUPC |
| 26 | CSSTRPFY | ~15 | String Padding/Formatting | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 27 | CSUTLDPY | ~10 | Date Utility Parameters | CSUTLDTC |
| 28 | CSUTLDWY | ~15 | Date Edit Working Storage | COACTUPC |
| 29 | CSLKPCDY | ~15 | Lookup Code Data | COACTUPC |
| 30 | CODATECN | ~10 | Date Conversion Record | CBACT01C |

### 2.3 BMS-Generated Copybooks (17) -- `app/cpy-bms/`

| # | Copybook | Source BMS | Screen |
|---|----------|-----------|--------|
| 1 | COACTUP.CPY | COACTUP.bms | Account Update |
| 2 | COACTVW.CPY | COACTVW.bms | Account View |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 5 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 6 | COCRDSL.CPY | COCRDSL.bms | Card Detail View |
| 7 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 8 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 9 | CORPT00.CPY | CORPT00.bms | Transaction Report |
| 10 | COSGN00.CPY | COSGN00.bms | Signon |
| 11 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 12 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 13 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | User Add |
| 16 | COUSR02.CPY | COUSR02.bms | User Update |
| 17 | COUSR03.CPY | COUSR03.bms | User Delete |

---

## 3. JCL Jobs (38)

### 3.1 Data File Management (12)

| # | JCL Job | Lines | Function | VSAM Dataset | Category |
|---|---------|-------|----------|-------------|----------|
| 1 | ACCTFILE | 96 | Delete/define/load Account Master VSAM | ACCTDAT.VSAM.KSDS | Data Refresh |
| 2 | CARDFILE | 89 | Delete/define/load Card Master VSAM | CARDDAT.VSAM.KSDS | Data Refresh |
| 3 | CUSTFILE | 92 | Delete/define/load Customer Master VSAM | CUSTDAT.VSAM.KSDS | Data Refresh |
| 4 | XREFFILE | 106 | Delete/define/load Cross-Reference VSAM + AIX | CARDXREF.VSAM.KSDS | Data Refresh |
| 5 | TRANFILE | 125 | Delete/define/load Transaction Master VSAM | TRANSACT.VSAM.KSDS | Data Refresh |
| 6 | DUSRSECJ | 63 | Delete/define/load User Security VSAM | USRSEC.VSAM.KSDS | Data Refresh |
| 7 | REPTFILE | 75 | Delete/define/load Report data files | Various | Data Refresh |
| 8 | DEFCUST | 42 | Define Customer VSAM cluster | CUSTDAT.VSAM.KSDS | VSAM Admin |
| 9 | DEFGDGB | 30 | Define GDG base (backup) | GDG base | VSAM Admin |
| 10 | DEFGDGD | 30 | Define GDG base (daily) | GDG base | VSAM Admin |
| 11 | ESDSRRDS | 31 | Define ESDS/RRDS datasets | Various | VSAM Admin |
| 12 | TRANTYPE | 65 | Define Transaction Type VSAM | TRANTYPE.VSAM.KSDS | Data Refresh |

### 3.2 Batch Processing (8)

| # | JCL Job | Lines | Function | Programs Executed | Category |
|---|---------|-------|----------|-------------------|----------|
| 13 | POSTTRAN | 95 | Core transaction posting | CBTRN01C, CBTRN02C | Transaction Processing |
| 14 | INTCALC | 107 | Interest calculation cycle | CBACT04C | Financial Processing |
| 15 | COMBTRAN | 75 | Combine daily + master transactions | SORT/MERGE | Transaction Processing |
| 16 | CREASTMT | 97 | Generate customer statements | CBSTM03A, CBSTM03B | Reporting |
| 17 | TRANREPT | 84 | Print transaction detail report | CBTRN03C | Reporting |
| 18 | TRANBKP | 43 | Backup transaction file (to GDG) | IDCAMS REPRO | Backup |
| 19 | CBEXPORT | 43 | Export all VSAM files to flat file | CBEXPORT | Data Migration |
| 20 | CBIMPORT | 46 | Import flat file to all VSAM files | CBIMPORT | Data Migration |

### 3.3 CICS File Operations (2)

| # | JCL Job | Lines | Function | Category |
|---|---------|-------|----------|----------|
| 21 | CLOSEFIL | 40 | Close CICS files for batch | CICS Operations |
| 22 | OPENFIL | 40 | Open CICS files after batch | CICS Operations |

### 3.4 Index Management (2)

| # | JCL Job | Lines | Function | Category |
|---|---------|-------|----------|----------|
| 23 | TRANIDX | 58 | Define alternate index on Transaction | Index Admin |
| 24 | TRANCATG | 62 | Define Transaction Category VSAM | Index Admin |

### 3.5 Utility / Diagnostic Jobs (10)

| # | JCL Job | Lines | Function | Category |
|---|---------|-------|----------|----------|
| 25 | READACCT | 37 | Read/display account file | Diagnostic |
| 26 | READCARD | 37 | Read/display card file | Diagnostic |
| 27 | READCUST | 37 | Read/display customer file | Diagnostic |
| 28 | READXREF | 37 | Read/display cross-reference file | Diagnostic |
| 29 | TCATBALF | 65 | Define Transaction Category Balance VSAM | VSAM Admin |
| 30 | DISCGRP | 62 | Define Disclosure Group VSAM | VSAM Admin |
| 31 | DALYREJS | 36 | Define Daily Rejects dataset | VSAM Admin |
| 32 | PRTCATBL | 30 | Print category balance file | Diagnostic |
| 33 | WAITSTEP | 27 | Wait step (calls COBSWAIT) | Utility |
| 34 | CBADMCDJ | 45 | Admin card management job | Admin |

### 3.6 FTP / Remote Jobs (4)

| # | JCL Job | Lines | Function | Category |
|---|---------|-------|----------|----------|
| 35 | FTPJCL | 42 | FTP file transfer | Remote Operations |
| 36 | INTRDRJ1 | 19 | Internal reader trigger (Job 1) | Job Chaining |
| 37 | INTRDRJ2 | 14 | Internal reader triggered job (Job 2) | Job Chaining |
| 38 | TXT2PDF1 | 41 | Convert text file to PDF | Utility |

---

## 4. BMS Screen Maps (17)

| # | BMS Map | Lines | Screen Title | Associated Program | Fields |
|---|---------|-------|--------------|--------------------|--------|
| 1 | COSGN00 | 210 | Login Screen | COSGN00C | UserID, Password |
| 2 | COMEN01 | 167 | Main Menu | COMEN01C | Option (1-12) |
| 3 | COADM01 | 167 | Admin Menu | COADM01C | Option (1-4) |
| 4 | COACTVW | 378 | Account View | COACTVWC | AcctID, CustID, Name, Status, Balances |
| 5 | COACTUP | 512 | Account Update | COACTUPC | All account/customer fields (editable) |
| 6 | COCRDLI | 344 | Card List | COCRDLIC | Filter, 7-row card list with select |
| 7 | COCRDSL | 157 | Card Detail View | COCRDSLC | Card#, AcctID, Name, Status, Expiry |
| 8 | COCRDUP | 172 | Card Update | COCRDUPC | Card fields (editable) |
| 9 | COTRN00 | 464 | Transaction List | COTRN00C | Filter, multi-row transaction list |
| 10 | COTRN01 | 273 | Transaction View | COTRN01C | All transaction fields (read-only) |
| 11 | COTRN02 | 307 | Transaction Add | COTRN02C | Transaction fields (input) |
| 12 | CORPT00 | 231 | Transaction Report | CORPT00C | Date range, monthly/yearly toggle |
| 13 | COBIL00 | 141 | Bill Payment | COBIL00C | Account, Amount, Payment confirmation |
| 14 | COUSR00 | 463 | User List | COUSR00C | Multi-row user list with select |
| 15 | COUSR01 | 164 | User Add | COUSR01C | UserID, Name, Password, Type |
| 16 | COUSR02 | 169 | User Update | COUSR02C | User fields (editable) |
| 17 | COUSR03 | 153 | User Delete | COUSR03C | User fields (confirm delete) |

---

## 5. Assembler Programs (2)

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | MVSWAIT | app/asm/ | Wait utility (assembler implementation) |
| 2 | COBDATFT | app/asm/ | Date format conversion utility |

---

## 6. JCL Procedures (2)

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | REPROC | app/proc/ | Reusable REPRO (IDCAMS) procedure for VSAM unload |
| 2 | TRANREPT | app/proc/ | Transaction report procedure |

---

## 7. Supporting Artifacts

| Category | Location | Contents |
|----------|----------|----------|
| Control Files | app/ctl/ | REPROCT.ctl - REPRO control statements |
| CSD Definitions | app/csd/ | CARDDEMO.CSD - CICS resource definitions |
| Macros | app/maclib/ | Assembler macros |
| Scheduler | app/scheduler/ | CA7 and Control-M job scheduling configs |
| Sample Data (ASCII) | app/data/ASCII/ | Test data files in ASCII format |
| Sample Data (EBCDIC) | app/data/EBCDIC/ | EBCDIC data for mainframe upload |
| Scripts | scripts/ | Shell scripts for mainframe FTP interaction |
| Diagrams | diagrams/ | Architecture diagrams and screen captures |
| Samples | samples/ | Sample JCL, procs, AWS M2 configs |

---

## 8. Classification Summary

### By Domain

| Domain | Online Programs | Batch Programs | Copybooks | JCL Jobs |
|--------|----------------|----------------|-----------|----------|
| Security / Auth | 1 (COSGN00C) | 0 | 1 (CSUSR01Y) | 1 (DUSRSECJ) |
| Navigation | 2 (COMEN01C, COADM01C) | 0 | 2 (COMEN02Y, COADM02Y) | 0 |
| Account Mgmt | 2 (COACTVWC, COACTUPC) | 1 (CBACT01C) | 1 (CVACT01Y) | 1 (ACCTFILE) |
| Card Mgmt | 3 (COCRDLIC, COCRDSLC, COCRDUPC) | 2 (CBACT02C, CBACT03C) | 2 (CVACT02Y, CVACT03Y) | 2 (CARDFILE, XREFFILE) |
| Transaction Mgmt | 3 (COTRN00C-02C) | 3 (CBTRN01C-03C) | 5 (CVTRA01Y-06Y) | 4 (POSTTRAN, TRANFILE, COMBTRAN, TRANIDX) |
| Customer Mgmt | 0 | 1 (CBCUS01C) | 1 (CVCUS01Y) | 1 (CUSTFILE) |
| Reporting | 1 (CORPT00C) | 2 (CBSTM03A/B) | 2 (CVTRA07Y, COSTM01) | 2 (CREASTMT, TRANREPT) |
| Billing | 1 (COBIL00C) | 0 | 0 | 0 |
| User Admin | 4 (COUSR00C-03C) | 0 | 0 | 0 |
| Financial | 0 | 1 (CBACT04C) | 1 (CVTRA02Y) | 1 (INTCALC) |
| Data Migration | 0 | 2 (CBEXPORT, CBIMPORT) | 1 (CVEXPORT) | 2 (CBEXPORT, CBIMPORT) |
| Utility | 1 (CSUTLDTC) | 1 (COBSWAIT) | 5 (framework) | 10+ (VSAM admin, diag) |

### By Technology

| Technology | Count | Examples |
|------------|-------|---------|
| CICS/COBOL Online | 18 programs | COSGN00C, COMEN01C, COACTVWC |
| Batch COBOL | 13 programs | CBTRN02C, CBACT04C, CBSTM03A |
| VSAM (KSDS) | 7 master files | ACCTDAT, CARDDAT, CUSTDAT, TRANSACT, USRSEC, CARDXREF, TRANTYPE |
| BMS (3270 UI) | 17 maps | All CO*.bms files |
| JCL (IDCAMS) | 15+ jobs | VSAM define/delete/repro |
| Assembler | 2 programs | MVSWAIT, COBDATFT |
| IMS DB (optional) | 3 programs | PAUDBLOD, PAUDBUNL, DBUNLDGS |
| DB2 (optional) | 4 programs | COPAUS2C, COTRTUPC, COTRTLIC, COBTUPDT |
| MQ (optional) | 3 programs | COPAUA0C, CODATE01, COACCT01 |

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| CO* | Online CICS program | COSGN00C |
| CB* | Batch COBOL program | CBTRN02C |
| CS* | Common/shared copybook | CSUSR01Y |
| CV* | VSAM data structure copybook | CVACT01Y |
| *Y suffix | Copybook (data structure) | CVACT01Y |
| DFHAID | IBM-supplied AID key definitions | (system) |
| DFHBMSCA | IBM-supplied BMS constants | (system) |
