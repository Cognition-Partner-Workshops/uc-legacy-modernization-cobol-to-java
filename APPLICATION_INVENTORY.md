# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Platform:** IBM z/OS Mainframe (CICS/VSAM/JCL)

---

## Executive Summary

CardDemo is a mainframe credit-card management application built with COBOL, CICS, VSAM, and JCL. It simulates account management, card management, transaction processing, bill payments, and reporting. The application supports two user roles: **Regular** (card operations) and **Admin** (user/transaction-type management).

| Artifact Type | Core Count | Optional Module Count | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | 44 |
| Copybooks (Data) | 30 | 11 | 41 |
| BMS Screen Maps | 17 | 4 | 21 |
| BMS-Generated Copybooks | 17 | 4 | 21 |
| JCL Batch Jobs | 38 | 8 | 46 |
| Assembler Programs | 2 | 0 | 2 |
| JCL Procedures | 2 | 0 | 2 |
| Control Files | 1 | 1 | 2 |
| Assembler Macros | 2 | 0 | 2 |
| Scheduler Configs | 2 | 0 | 2 |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs

| # | Program | Lines | Type | CICS Transaction | Function | Business Domain |
|---|---------|-------|------|-----------------|----------|-----------------|
| 1 | COSGN00C | 260 | Online CICS | CC00 | Signon screen - authenticates users against USRSEC VSAM file | Security |
| 2 | COMEN01C | 308 | Online CICS | CM00 | Main menu for regular users - routes to sub-functions | Navigation |
| 3 | COADM01C | 288 | Online CICS | CA00 | Admin menu - routes to admin sub-functions | Navigation / Admin |
| 4 | COACTVWC | 941 | Online CICS | CA01 | Account view - displays account, card, and customer details | Account Mgmt |
| 5 | COACTUPC | 4,236 | Online CICS | CA02 | Account update - modifies account details with full validation | Account Mgmt |
| 6 | COCRDLIC | 1,459 | Online CICS | CC01 | Card list - browses cards with pagination (STARTBR/READNEXT) | Card Mgmt |
| 7 | COCRDSLC | 887 | Online CICS | CC02 | Card view - displays single card detail with customer info | Card Mgmt |
| 8 | COCRDUPC | 1,560 | Online CICS | CC03 | Card update - modifies card details with validation | Card Mgmt |
| 9 | COTRN00C | 699 | Online CICS | CT00 | Transaction list - browses transactions with pagination | Transaction Mgmt |
| 10 | COTRN01C | 330 | Online CICS | CT01 | Transaction view - displays single transaction detail | Transaction Mgmt |
| 11 | COTRN02C | 783 | Online CICS | CT02 | Transaction add - creates new transaction with validation | Transaction Mgmt |
| 12 | CORPT00C | 649 | Online CICS | CR00 | Transaction report - submits batch report job via TDQ | Reporting |
| 13 | COBIL00C | 572 | Online CICS | CB00 | Bill payment - processes credit card payments | Billing |
| 14 | COUSR00C | 695 | Online CICS | CU00 | User list - browses users with pagination (Admin) | User Admin |
| 15 | COUSR01C | 299 | Online CICS | CU01 | User add - creates new Regular/Admin users (Admin) | User Admin |
| 16 | COUSR02C | 414 | Online CICS | CU02 | User update - modifies user records (Admin) | User Admin |
| 17 | COUSR03C | 359 | Online CICS | CU03 | User delete - removes users from USRSEC file (Admin) | User Admin |

### 1.2 Batch Programs

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 18 | CBTRN01C | 494 | Batch | Daily transaction validation - reads DALYTRAN, validates against master files | Transaction Processing |
| 19 | CBTRN02C | 731 | Batch | Transaction posting - posts daily transactions to TRANSACT master, updates category balances | Transaction Processing |
| 20 | CBTRN03C | 649 | Batch | Transaction report generation - produces daily transaction report with totals | Reporting |
| 21 | CBACT01C | 430 | Batch | Account data reader - reads ACCTDATA VSAM and writes to flat files (PS, array, VB) | Data Utilities |
| 22 | CBACT02C | 178 | Batch | Card data reader - reads CARDDATA VSAM and displays records | Data Utilities |
| 23 | CBACT03C | 178 | Batch | Card cross-reference reader - reads CARDXREF VSAM and displays records | Data Utilities |
| 24 | CBACT04C | 652 | Batch | Interest calculation - computes interest on accounts using disclosure group rates | Financial Processing |
| 25 | CBCUS01C | 178 | Batch | Customer data reader - reads CUSTDATA VSAM and displays records | Data Utilities |
| 26 | CBSTM03A | 924 | Batch | Statement generation - produces account statements in text and HTML formats | Reporting |
| 27 | CBSTM03B | 230 | Batch Subroutine | Statement file I/O subroutine - called by CBSTM03A for file processing | Reporting |
| 28 | CBEXPORT | 582 | Batch | Data export - exports all VSAM files to a single sequential export file | Data Migration |
| 29 | CBIMPORT | 487 | Batch | Data import - imports sequential export file back into VSAM files | Data Migration |
| 30 | COBSWAIT | 41 | Batch Utility | Wait utility - calls assembler MVSWAIT for timed delays | System Utility |

### 1.3 Shared Subroutines

| # | Program | Lines | Type | Function | Business Domain |
|---|---------|-------|------|----------|-----------------|
| 31 | CSUTLDTC | 157 | Subroutine | Date conversion utility - calls CEEDAYS for date validation/conversion | Shared Utility |

---

## 2. Optional Module Programs

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

| # | Program | Lines | Type | Function | Technology |
|---|---------|-------|------|----------|------------|
| 32 | COPAUA0C | 1,026 | Online CICS/IMS/MQ | Card authorization decision - processes MQ auth requests against IMS DB | CICS + IMS + MQ |
| 33 | COPAUS0C | 1,032 | Online CICS/IMS/BMS | Summary view of authorization messages with browsing | CICS + IMS + BMS |
| 34 | COPAUS1C | 604 | Online CICS/IMS/BMS | Detail view of individual authorization message | CICS + IMS + BMS |
| 35 | COPAUS2C | 244 | Online CICS/IMS/DB2 | Mark authorization message as fraud (writes to DB2) | CICS + IMS + DB2 |
| 36 | CBPAUP0C | 386 | Batch IMS | Purge expired pending authorization messages from IMS DB | Batch IMS |
| 37 | PAUDBLOD | 369 | Batch IMS | Load authorization data into IMS database | Batch IMS |
| 38 | PAUDBUNL | 317 | Batch IMS | Unload authorization data from IMS database | Batch IMS |
| 39 | DBUNLDGS | 366 | Batch IMS | Unload IMS GSAM segments for authorization data | Batch IMS |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Type | Function | Technology |
|---|---------|-------|------|----------|------------|
| 40 | COTRTLIC | 2,098 | Online CICS/DB2 | List transaction types - demonstrates paging with DB2 cursors | CICS + DB2 |
| 41 | COTRTUPC | 1,702 | Online CICS/DB2 | Add/edit transaction types - DB2 CRUD operations | CICS + DB2 |
| 42 | COBTUPDT | 237 | Batch DB2 | Batch update transaction types from input file | Batch + DB2 |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Type | Function | Technology |
|---|---------|-------|------|----------|------------|
| 43 | CODATE01 | 524 | Online MQ | System date service - responds to MQ date requests | CICS + MQ |
| 44 | COACCT01 | 620 | Online MQ | Account inquiry service - responds to MQ account lookup requests | CICS + MQ |

---

## 3. Copybooks (`app/cpy/`)

### 3.1 Data Record Layouts (CV* prefix)

| # | Copybook | Record Length | Business Entity | Description |
|---|----------|--------------|-----------------|-------------|
| 1 | CVACT01Y | 300 bytes | Account Master | Account data - ID, status, balances, credit limits, dates |
| 2 | CVACT02Y | 150 bytes | Card Master | Card data - number, account ID, CVV, embossed name, expiry, status |
| 3 | CVACT03Y | 50 bytes | Card Cross-Reference | Maps card numbers to account IDs |
| 4 | CVCUS01Y | 500 bytes | Customer Master | Full customer record - demographics, address, credit score, FICO |
| 5 | CVCRD01Y | -- | Card Display | Card detail working-storage structure for online screens |
| 6 | CVTRA01Y | 50 bytes | Transaction Category Balance | Running balance per account/transaction-type/category |
| 7 | CVTRA02Y | 50 bytes | Disclosure Group | Interest rates per account group/transaction-type/category |
| 8 | CVTRA03Y | 60 bytes | Transaction Type | Transaction type code and description |
| 9 | CVTRA04Y | 60 bytes | Transaction Category | Transaction category code within a type |
| 10 | CVTRA05Y | 350 bytes | Transaction Record | Full transaction - ID, amounts, merchant info, timestamps |
| 11 | CVTRA06Y | 350 bytes | Daily Transaction | Daily pending transaction (same layout as CVTRA05Y) |
| 12 | CVTRA07Y | -- | Transaction Report | Report formatting structures (headers, totals, detail lines) |
| 13 | CVEXPORT | -- | Export Record | Combined export/import record layout for data migration |
| 14 | CUSTREC | -- | Customer (Alternate) | Alternate customer record layout used by statement generation |
| 15 | COSTM01 | 350 bytes | Statement Transaction | Transaction layout re-keyed by card number for statement generation |

### 3.2 Common/Shared Copybooks (CO*/CS* prefix)

| # | Copybook | Description |
|---|----------|-------------|
| 16 | COCOM01Y | Common communication area (COMMAREA) - passed between CICS programs |
| 17 | COMEN02Y | Menu option definitions for main menu routing |
| 18 | COADM02Y | Admin menu option definitions |
| 19 | COTTL01Y | Screen title/header definitions |
| 20 | CSDAT01Y | Date formatting working-storage |
| 21 | CSMSG01Y | Message area definitions (error/info messages) |
| 22 | CSMSG02Y | Extended message area definitions |
| 23 | CSUSR01Y | User security record - ID, first/last name, password, type (A/U) |
| 24 | CSUTLDPY | Date utility parameters |
| 25 | CSUTLDWY | Date utility working-storage |
| 26 | CSLKPCDY | Lookup code definitions |
| 27 | CSSETATY | Screen field attribute setting (COPY REPLACING pattern) |
| 28 | CSSTRPFY | String strip/formatting utility procedure |
| 29 | CODATECN | Date conversion record for COBDATFT assembler call |
| 30 | UNUSED1Y | Unused placeholder copybook |

### 3.3 Optional Module Copybooks

#### Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 31 | CCPAUERY | Pending authorization error log record |
| 32 | CCPAURLY | Pending authorization response (card, auth code, amounts) |
| 33 | CCPAURQY | Pending authorization request (card, merchant, amounts) |
| 34 | CIPAUDTY | IMS segment - pending authorization detail |
| 35 | CIPAUSMY | IMS segment - pending authorization summary |
| 36 | IMSFUNCS | IMS DL/I function codes (GU, GN, ISRT, DLET, etc.) |
| 37 | PADFLPCB | IMS PCB for authorization detail database |
| 38 | PASFLPCB | IMS PCB for authorization summary database |
| 39 | PAUTBPCB | IMS PCB for authorization base database |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 40 | CSDB2RWY | DB2 common working-storage variables (SQLCODE, DSNTIAC) |
| 41 | CSDB2RPY | DB2 common procedures (priming query, error formatting) |

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map | Map Set | Screen Name | Associated Program | Function |
|---|-----|---------|-------------|-------------------|----------|
| 1 | COSGN00 | COSGN00 | COSGN0A | COSGN00C | Sign-on screen (User ID / Password) |
| 2 | COMEN01 | COMEN01 | COMEN1A | COMEN01C | Main menu (regular user navigation) |
| 3 | COADM01 | COADM01 | COADM1A | COADM01C | Admin menu (admin user navigation) |
| 4 | COACTVW | COACTVW | CACTVWA | COACTVWC | Account view (read-only display) |
| 5 | COACTUP | COACTUP | CACTUPA | COACTUPC | Account update (editable form) |
| 6 | COCRDLI | COCRDLI | CCRDLIA | COCRDLIC | Credit card list (paginated browse) |
| 7 | COCRDSL | COCRDSL | CCRDLA | COCRDSLC | Credit card detail view |
| 8 | COCRDUP | COCRDUP | CCRDUPA | COCRDUPC | Credit card update |
| 9 | COTRN00 | COTRN00 | COTRN0A | COTRN00C | Transaction list (paginated browse) |
| 10 | COTRN01 | COTRN01 | COTRN1A | COTRN01C | Transaction view (read-only) |
| 11 | COTRN02 | COTRN02 | COTRN2A | COTRN02C | Transaction add (input form) |
| 12 | CORPT00 | CORPT00 | CORPT0A | CORPT00C | Report selection (date range, type) |
| 13 | COBIL00 | COBIL00 | COBIL0A | COBIL00C | Bill payment (payment entry) |
| 14 | COUSR00 | COUSR00 | COUSR0A | COUSR00C | User list (paginated browse - Admin) |
| 15 | COUSR01 | COUSR01 | COUSR1A | COUSR01C | User add (Admin) |
| 16 | COUSR02 | COUSR02 | COUSR2A | COUSR02C | User update (Admin) |
| 17 | COUSR03 | COUSR03 | COUSR3A | COUSR03C | User delete (Admin) |

### BMS-Generated Copybooks (`app/cpy-bms/`)

17 generated copybooks matching each BMS map above (COSGN00.CPY, COMEN01.CPY, etc.). These contain the symbolic DFHMDI/DFHMDF field definitions used by COBOL programs to reference screen fields.

### Optional Module BMS Maps

| Map | Module | Function |
|-----|--------|----------|
| COPAU00 | Authorization | Authorization summary list screen |
| COPAU01 | Authorization | Authorization detail screen |
| COTRTLI | Trans Type DB2 | Transaction type list screen |
| COTRTUP | Trans Type DB2 | Transaction type update screen |

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data Loading / Refresh Jobs

| # | Job | Programs Used | Function | VSAM Files Affected |
|---|-----|--------------|----------|-------------------|
| 1 | ACCTFILE | IDCAMS | Delete/define/load account master VSAM from flat file | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | IDCAMS | Delete/define/load card master VSAM from flat file | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | IDCAMS | Delete/define/load customer master VSAM from flat file | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | IDCAMS | Delete/define/load card cross-reference VSAM with alternate index | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | IDCAMS, SDSF | Close CICS files, load transaction VSAM with alt index, reopen | TRANSACT.VSAM.KSDS, DALYTRAN |
| 6 | DUSRSECJ | IEFBR14, IEBGENER, IDCAMS | Load user security VSAM from flat file | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | IDCAMS | Delete/define/load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | IDCAMS | Delete/define/load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | IDCAMS | Delete/define/load transaction category balance VSAM | TCATBALF.VSAM.KSDS |
| 10 | DISCGRP | IDCAMS | Delete/define/load disclosure group VSAM | DISCGRP.VSAM.KSDS |
| 11 | ESDSRRDS | IDCAMS, IEBGENER | Load ESDS/RRDS format VSAM files (demo) | USRSEC.VSAM.ESDS/RRDS |

### 5.2 Core Batch Processing Jobs

| # | Job | Programs Used | Function | Input/Output Files |
|---|-----|--------------|----------|-------------------|
| 12 | POSTTRAN | CBTRN02C | Post daily transactions to master | DALYTRAN (in), TRANSACT (out), DALYREJS GDG (out) |
| 13 | INTCALC | CBACT04C | Calculate interest on accounts | TCATBALF, CARDXREF, ACCTDATA, DISCGRP, SYSTRAN GDG |
| 14 | TRANBKP | IDCAMS | Backup transaction master to GDG | TRANSACT (in), TRANSACT.BKUP GDG (out) |
| 15 | COMBTRAN | IEBGENER | Backup transaction type/category/disclosure data to GDGs | TRANTYPE, TRANCATG, DISCGRP (in), GDGs (out) |
| 16 | TRANREPT | SORT, CBTRN03C | Generate daily transaction report | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM (in), TRANREPT GDG (out) |
| 17 | CREASTMT | SORT, IDCAMS, CBSTM03A | Generate account statements (text + HTML) | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA (in), STATEMNT.PS/HTML (out) |
| 18 | TRANIDX | IDCAMS | Rebuild alternate indexes on TRANSACT VSAM | TRANSACT.VSAM.KSDS |

### 5.3 File Management Jobs

| # | Job | Programs Used | Function |
|---|-----|--------------|----------|
| 19 | CLOSEFIL | SDSF | Close CICS-managed VSAM files for batch processing |
| 20 | OPENFIL | SDSF | Reopen CICS-managed VSAM files after batch processing |
| 21 | DEFGDGB | IDCAMS | Define GDG base entries for backup datasets |
| 22 | DEFGDGD | IDCAMS | Define GDG base entries for daily datasets |
| 23 | WAITSTEP | COBSWAIT | Execute timed wait (used between batch steps) |

### 5.4 Data Reader / Utility Jobs

| # | Job | Programs Used | Function |
|---|-----|--------------|----------|
| 24 | READACCT | CBACT01C | Read and dump account VSAM to flat files |
| 25 | READCARD | CBACT02C | Read and display card VSAM records |
| 26 | READCUST | CBCUS01C | Read and display customer VSAM records |
| 27 | READXREF | CBACT03C | Read and display card cross-reference VSAM records |
| 28 | REPTFILE | IDCAMS | Display/print report file contents |
| 29 | PRTCATBL | SORT | Print/backup transaction category balance file |
| 30 | DALYREJS | IDCAMS | Define GDG for daily rejection files |

### 5.5 Data Export / Import Jobs

| # | Job | Programs Used | Function |
|---|-----|--------------|----------|
| 31 | CBEXPORT | CBEXPORT | Export all VSAM files to single sequential file |
| 32 | CBIMPORT | CBIMPORT | Import sequential file back to VSAM files |

### 5.6 Utility / Infrastructure Jobs

| # | Job | Programs Used | Function |
|---|-----|--------------|----------|
| 33 | FTPJCL | FTP | FTP file transfer to/from mainframe |
| 34 | INTRDRJ1 | IDCAMS, IEBGENER | Internal reader job - triggers INTRDRJ2 |
| 35 | INTRDRJ2 | IDCAMS | Internal reader job - secondary step |
| 36 | TXT2PDF1 | IKJEFT1B (TXT2PDF) | Convert text statement to PDF |
| 37 | DEFCUST | IDCAMS | Define customer VSAM cluster |
| 38 | CBADMCDJ | -- | Admin card job (placeholder) |

### 5.7 Optional Module JCL

| # | Job | Module | Function |
|---|-----|--------|----------|
| 39 | CBPAUP0J | Authorization | Purge expired pending authorizations |
| 40 | DBPAUTP0 | Authorization | Pending authorization batch processing |
| 41 | LOADPADB | Authorization | Load pending authorization IMS database |
| 42 | UNLDGSAM | Authorization | Unload GSAM authorization data |
| 43 | UNLDPADB | Authorization | Unload pending authorization IMS database |
| 44 | CREADB21 | Trans Type DB2 | Create DB2 tables for transaction types |
| 45 | MNTTRDB2 | Trans Type DB2 | Maintain transaction type DB2 data |
| 46 | TRANEXTR | Trans Type DB2 | Extract transaction type data |

---

## 6. Assembler Programs (`app/asm/`)

| Program | Function |
|---------|----------|
| MVSWAIT | Timed wait routine (called by COBSWAIT) |
| COBDATFT | Date format/conversion routine (called by CBACT01C) |

## 7. JCL Procedures (`app/proc/`)

| Procedure | Function |
|-----------|----------|
| REPROC | Report processing procedure |
| TRANREPT | Transaction report procedure |

## 8. Scheduler Configurations (`app/scheduler/`)

| Config | Scheduler | Function |
|--------|-----------|----------|
| CardDemo.ca7 | CA-7 | Batch job scheduling definitions |
| CardDemo.controlm | Control-M | Batch job scheduling definitions |

## 9. Batch Processing Cycle

The recommended nightly batch cycle executes in this order:

```
CLOSEFIL  -->  Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
    |
    v
POSTTRAN  -->  INTCALC  -->  TRANBKP  -->  COMBTRAN  -->  CREASTMT  -->  TRANIDX
    |
    v
OPENFIL
```

---

## 10. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| CO* | Online CICS program | COSGN00C |
| CB* | Batch COBOL program | CBTRN02C |
| CS* | Common shared module/copybook | CSUTLDTC |
| CV* | Copybook - VSAM record layout | CVACT01Y |
| COPA* | Authorization module (online) | COPAUA0C |
| COTR* | Transaction type module | COTRTLIC |
| *Y | Copybook suffix | CVACT01Y |
| *C | COBOL program suffix | COSGN00C |
