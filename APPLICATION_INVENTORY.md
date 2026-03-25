# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
> **Total Assets:** 31 COBOL Programs | 30 Copybooks | 38 JCL Jobs | 17 BMS Maps | 17 BMS Copybooks | 2 Assembler Programs

---

## 1. COBOL Programs (31)

### 1.1 Online CICS Programs (18)

| # | Program | Lines | CICS Tran | Function | Business Domain | Layer |
|---|---------|------:|-----------|----------|-----------------|-------|
| 1 | COSGN00C | 261 | CC00 | Signon screen / authentication | Security | UI / Auth |
| 2 | COMEN01C | 309 | CM00 | Main menu for regular users | Navigation | UI |
| 3 | COADM01C | 288 | CA00 | Admin menu for admin users | Navigation | UI |
| 4 | COACTVWC | 942 | CAVW | View account details | Account Mgmt | Business Logic |
| 5 | COACTUPC | 4,237 | CAUP | Update account / customer / card data | Account Mgmt | Business Logic |
| 6 | COCRDLIC | 1,460 | CCLI | List credit cards (paginated browse) | Card Mgmt | Business Logic |
| 7 | COCRDSLC | 888 | CCDL | View credit card details | Card Mgmt | Business Logic |
| 8 | COCRDUPC | 1,560 | CCUP | Update credit card details | Card Mgmt | Business Logic |
| 9 | COTRN00C | 699 | CT00 | List transactions (paginated browse) | Transaction Mgmt | Business Logic |
| 10 | COTRN01C | 330 | CT01 | View a single transaction | Transaction Mgmt | Business Logic |
| 11 | COTRN02C | 783 | CT02 | Add a new transaction | Transaction Mgmt | Business Logic |
| 12 | CORPT00C | 649 | CR00 | Submit batch transaction report | Reporting | UI / Batch Bridge |
| 13 | COBIL00C | 572 | CB00 | Bill payment (pay balance in full / partial) | Billing | Business Logic |
| 14 | COUSR00C | 695 | CU00 | List all users (admin) | User Admin | Business Logic |
| 15 | COUSR01C | 299 | CU01 | Add a new user (admin) | User Admin | Business Logic |
| 16 | COUSR02C | 414 | CU02 | Update a user (admin) | User Admin | Business Logic |
| 17 | COUSR03C | 359 | CU03 | Delete a user (admin) | User Admin | Business Logic |
| 18 | CSUTLDTC | 157 | -- | Date validation utility (calls CEEDAYS) | Utility | Shared Service |

> **Note:** Programs in the optional modules (see Section 6) add 13 more online CICS programs.

### 1.2 Batch Programs (13)

| # | Program | Lines | Function | Business Domain |
|---|---------|------:|----------|-----------------|
| 1 | CBTRN01C | 494 | Validate daily transactions against master files | Transaction Processing |
| 2 | CBTRN02C | 731 | Post daily transactions, update accounts & category balances | Transaction Processing |
| 3 | CBTRN03C | 649 | Print daily transaction detail report | Reporting |
| 4 | CBACT01C | 430 | Read account file, write to output formats (PS, array, VB) | Data Management |
| 5 | CBACT02C | 178 | Read and print card data file | Data Management |
| 6 | CBACT03C | 178 | Read and print cross-reference data file | Data Management |
| 7 | CBACT04C | 652 | Calculate interest on accounts using disclosure groups | Financial Processing |
| 8 | CBCUS01C | 178 | Read and print customer data file | Data Management |
| 9 | CBSTM03A | 924 | Generate account statements (text + HTML) -- driver | Statement Generation |
| 10 | CBSTM03B | 230 | Statement generation -- file I/O subroutine | Statement Generation |
| 11 | CBEXPORT | 582 | Export all VSAM data to a single sequential file | Data Management |
| 12 | CBIMPORT | 487 | Import sequential file back into entity-level files | Data Management |
| 13 | COBSWAIT | 41 | Wait utility (PARM in centiseconds, calls MVSWAIT) | Utility |

---

## 2. Copybooks (30)

### 2.1 Data Structure Copybooks (Business Entities)

| # | Copybook | Record | Key Fields | Len | Business Entity | Used By (count) |
|---|----------|--------|------------|----:|-----------------|-----------------|
| 1 | CVACT01Y | ACCOUNT-RECORD | ACCT-ID (11) | 300 | Account Master | 13 programs |
| 2 | CVACT02Y | CARD-RECORD | CARD-NUM (16) | 150 | Card Master | 8 programs |
| 3 | CVACT03Y | CARD-XREF-RECORD | XREF-CARD-NUM (16) | 50 | Card-Account Cross-Reference | 14 programs |
| 4 | CVCUS01Y | CUSTOMER-RECORD | CUST-ID (9) | 500 | Customer Master | 8 programs |
| 5 | CVTRA05Y | TRAN-RECORD | TRAN-ID (16) | 350 | Transaction Master | 11 programs |
| 6 | CVTRA06Y | DALYTRAN-RECORD | DALYTRAN-ID (16) | 350 | Daily Transaction (input) | 2 programs |
| 7 | CVTRA01Y | TRAN-CAT-BAL-RECORD | TRANCAT-ACCT-ID + TYPE + CD | 50 | Transaction Category Balance | 2 programs |
| 8 | CVTRA02Y | DIS-GROUP-RECORD | DIS-ACCT-GROUP-ID + TYPE + CAT | 50 | Disclosure / Interest Rate Group | 1 program |
| 9 | CVTRA03Y | TRAN-TYPE-RECORD | TRAN-TYPE (2) | 60 | Transaction Type | 1 program |
| 10 | CVTRA04Y | TRAN-CAT-RECORD | TRAN-TYPE-CD + TRAN-CAT-CD | 60 | Transaction Category | 1 program |
| 11 | CVTRA07Y | REPORT structures | -- | -- | Transaction Report Layout | 1 program |
| 12 | CVCRD01Y | CC-WORK-AREA | -- | -- | Credit Card Work Area (screen I/O) | 5 programs |
| 13 | CSUSR01Y | SEC-USER-DATA | SEC-USR-ID (8) | 80 | User Security Record | 12 programs |
| 14 | CVEXPORT | EXPORT-RECORD | EXP-TABLE-NAME (10) | varies | Export/Import Record Layout | 2 programs |
| 15 | CUSTREC | (customer for statements) | -- | -- | Customer Data (statement variant) | 1 program |
| 16 | COSTM01 | TRNX-RECORD | TRNX-CARD-NUM + TRNX-ID | 350 | Transaction (statement variant) | 1 program |
| 17 | UNUSED1Y | UNUSED-DATA | UNUSED-ID (8) | 80 | Unused / placeholder | 0 programs |

### 2.2 Application Framework Copybooks

| # | Copybook | Purpose | Used By (count) |
|---|----------|---------|-----------------|
| 1 | COCOM01Y | Common communication area (COMMAREA) for CICS programs | 17 programs |
| 2 | COTTL01Y | Screen title constants (CCDA-TITLE01, CCDA-TITLE02) | 17 programs |
| 3 | CSDAT01Y | Current date/time working storage fields | 17 programs |
| 4 | CSMSG01Y | Common application messages | 17 programs |
| 5 | CSMSG02Y | Abend handling variables | 5 programs |
| 6 | COMEN02Y | Menu option definitions (names, programs, user types) | 1 program |
| 7 | COADM02Y | Admin menu option definitions | 1 program |
| 8 | CSLKPCDY | Lookup code tables | 1 program |
| 9 | CSUTLDPY | Date utility parameters | 1 program |
| 10 | CSUTLDWY | Date edit working storage | 1 program |
| 11 | CSSETATY | Screen field attribute setting (REPLACING) | 1 program (39 uses) |
| 12 | CSSTRPFY | String padding/formatting utility | 5 programs |
| 13 | CODATECN | Date format conversion record | 1 program |

---

## 3. BMS Screen Maps (17)

| # | Map | Mapset | Screen | Associated Program |
|---|-----|--------|--------|--------------------|
| 1 | COSGN00 | COSGN00 | COSGN0A | Sign-on Screen | COSGN00C |
| 2 | COMEN01 | COMEN01 | COMEN1A | Main Menu | COMEN01C |
| 3 | COADM01 | COADM01 | COADM1A | Admin Menu | COADM01C |
| 4 | COACTVW | COACTVW | CACTVWA | Account View | COACTVWC |
| 5 | COACTUP | COACTUP | CACTUPA | Account Update | COACTUPC |
| 6 | COCRDLI | COCRDLI | CCRDLIA | Card List | COCRDLIC |
| 7 | COCRDSL | COCRDSL | CCRDSLA | Card Detail View | COCRDSLC |
| 8 | COCRDUP | COCRDUP | CCRDUPA | Card Update | COCRDUPC |
| 9 | COTRN00 | COTRN00 | COTRN0A | Transaction List | COTRN00C |
| 10 | COTRN01 | COTRN01 | COTRN1A | Transaction View | COTRN01C |
| 11 | COTRN02 | COTRN02 | COTRN2A | Transaction Add | COTRN02C |
| 12 | CORPT00 | CORPT00 | CORPT0A | Report Request | CORPT00C |
| 13 | COBIL00 | COBIL00 | COBIL0A | Bill Payment | COBIL00C |
| 14 | COUSR00 | COUSR00 | COUSR0A | User List | COUSR00C |
| 15 | COUSR01 | COUSR01 | COUSR1A | User Add | COUSR01C |
| 16 | COUSR02 | COUSR02 | COUSR2A | User Update | COUSR02C |
| 17 | COUSR03 | COUSR03 | COUSR3A | User Delete | COUSR03C |

### BMS-Generated Copybooks (17)

Each BMS map generates a copybook in `app/cpy-bms/` with symbolic field names for both input (suffix `I`) and output (suffix `O`):
`COACTUP.CPY`, `COACTVW.CPY`, `COADM01.CPY`, `COBIL00.CPY`, `COCRDLI.CPY`, `COCRDSL.CPY`, `COCRDUP.CPY`, `COMEN01.CPY`, `CORPT00.CPY`, `COSGN00.CPY`, `COTRN00.CPY`, `COTRN01.CPY`, `COTRN02.CPY`, `COUSR00.CPY`, `COUSR01.CPY`, `COUSR02.CPY`, `COUSR03.CPY`

---

## 4. JCL Batch Jobs (38)

### 4.1 Data Refresh Jobs (load flat files into VSAM)

| # | JCL Job | Lines | Function | VSAM Dataset |
|---|---------|------:|----------|-------------|
| 1 | ACCTFILE | 65 | Refresh account master VSAM KSDS | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | 128 | Refresh card master VSAM KSDS + define AIX | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | 84 | Refresh customer master VSAM KSDS | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | 106 | Load card cross-reference VSAM KSDS + define AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | 125 | Load initial daily transaction data + define VSAM KSDS | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | 92 | Load user security VSAM KSDS from flat file | USRSEC.VSAM.KSDS |
| 7 | TCATBALF | 65 | Load transaction category balance VSAM | TCATBALF.VSAM.KSDS |
| 8 | TRANCATG | 65 | Load transaction category VSAM | TRANCATG.VSAM.KSDS |
| 9 | TRANTYPE | 65 | Load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 10 | DISCGRP | 65 | Load disclosure/interest rate groups VSAM | DISCGRP.VSAM.KSDS |

### 4.2 Core Batch Processing Jobs

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|------:|----------|-----------------|
| 1 | POSTTRAN | 45 | Post daily transactions to master files | CBTRN02C |
| 2 | INTCALC | 44 | Calculate interest on accounts | CBACT04C |
| 3 | TRANBKP | 71 | Backup transaction file to GDG | IDCAMS REPRO |
| 4 | COMBTRAN | 52 | Combine backup + system transactions | SORT + IDCAMS |
| 5 | CREASTMT | 97 | Sort transactions, create statements (text + HTML) | SORT + CBSTM03A |
| 6 | TRANREPT | 84 | Backup transactions + generate daily report | IDCAMS + CBTRN03C |

### 4.3 CICS File Management Jobs

| # | JCL Job | Lines | Function |
|---|---------|------:|----------|
| 1 | CLOSEFIL | 34 | Close CICS-managed VSAM files for batch |
| 2 | OPENFIL | 34 | Reopen CICS-managed VSAM files after batch |

### 4.4 Data Utility / Read Jobs

| # | JCL Job | Lines | Function |
|---|---------|------:|----------|
| 1 | READACCT | 50 | Read account VSAM, write to PS/array/VB formats (CBACT01C) |
| 2 | READCARD | 31 | Read and print card data (CBACT02C) |
| 3 | READCUST | 30 | Read and print customer data (CBCUS01C) |
| 4 | READXREF | 31 | Read and print cross-reference data (CBACT03C) |
| 5 | PRTCATBL | 66 | Print category balance file + backup to GDG |
| 6 | REPTFILE | 32 | Report file utility |
| 7 | CBEXPORT | 72 | Export all VSAM files to single sequential dataset |
| 8 | CBIMPORT | 68 | Import sequential dataset into individual files |

### 4.5 Infrastructure / Definition Jobs

| # | JCL Job | Lines | Function |
|---|---------|------:|----------|
| 1 | DEFGDGB | 63 | Define GDG base clusters |
| 2 | DEFGDGD | 94 | Backup reference data (trantype, trancatg, discgrp) to GDGs |
| 3 | DEFCUST | 47 | Define customer VSAM cluster |
| 4 | TRANIDX | 58 | Define alternate index on transaction file |
| 5 | ESDSRRDS | 124 | Load ESDS and RRDS test clusters |
| 6 | DALYREJS | 32 | Process daily rejection records |
| 7 | CBADMCDJ | 167 | CICS CSD administration (define resources) |
| 8 | WAITSTEP | 27 | Execute wait utility (COBSWAIT) |

### 4.6 Miscellaneous / FTP Jobs

| # | JCL Job | Lines | Function |
|---|---------|------:|----------|
| 1 | FTPJCL | 42 | FTP file transfer JCL |
| 2 | INTRDRJ1 | 19 | Internal reader -- copy + submit INTRDRJ2 |
| 3 | INTRDRJ2 | 14 | Internal reader -- secondary copy step |
| 4 | TXT2PDF1 | 41 | Convert text statement to PDF |

---

## 5. Assembler Programs (2)

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | COBDATFT | app/asm/ | Date formatting utility (called by CBACT01C) |
| 2 | MVSWAIT | app/asm/ | Wait/sleep utility (called by COBSWAIT) |

---

## 6. Optional Extension Modules

### 6.1 Authorization Module (IMS/DB2/MQ) -- `app/app-authorization-ims-db2-mq/`

| Program | Type | Function |
|---------|------|----------|
| COPAUA0C | Online | MQ trigger -- start authorization |
| COPAUS0C | Online | Authorization summary display |
| COPAUS1C | Online | Authorization detail display |
| COPAUS2C | Online | Mark fraud in DB2 |
| CBPAUP0C | Batch | Purge processed authorizations |
| + 3 more | -- | Supporting programs |

### 6.2 Transaction Type DB2 Module -- `app/app-transaction-type-db2/`

| Program | Type | Function |
|---------|------|----------|
| COTRTUPC | Online | Add/edit transaction types (DB2) |
| COTRTLIC | Online | List/delete transaction types (DB2) |
| COBTUPDT | Batch | Batch update transaction types |

### 6.3 VSAM-MQ Module -- `app/app-vsam-mq/`

| Program | Type | Function |
|---------|------|----------|
| CODATE01 | Online | MQ request/response for system date |
| COACCT01 | Online | MQ request/response for account inquiry |

---

## 7. Supporting Assets

| Category | Location | Contents |
|----------|----------|----------|
| JCL Procedures | `app/proc/` | REPROC.prc (REPRO procedure), TRANREPT.prc (report procedure) |
| Control Files | `app/ctl/` | REPROCT.ctl (REPRO control) |
| Catalog Listings | `app/catlg/` | LISTCAT.txt |
| Macros | `app/maclib/` | ASMWAIT.mac, COCDATFT.mac |
| CSD Definitions | `app/csd/` | CARDDEMO.CSD (CICS resource definitions) |
| Job Scheduling | `app/scheduler/` | CardDemo.ca7 (CA7), CardDemo.controlm (Control-M) |
| Sample Data | `app/data/ASCII/` | ASCII test data files |
| Sample Data | `app/data/EBCDIC/` | EBCDIC mainframe-ready data files |
| Shell Scripts | `scripts/` | 10 scripts for mainframe FTP interaction, compilation, batch submission |
| Diagrams | `diagrams/` | Architecture diagrams and screen captures |

---

## 8. Classification Summary

| Classification | Count | Notes |
|----------------|------:|-------|
| Online CICS Programs | 18 | Core user-facing screens |
| Batch COBOL Programs | 13 | Nightly/periodic processing |
| Shared Utility Programs | 2 | CSUTLDTC (date), COBSWAIT (wait) |
| Copybooks -- Data Structures | 17 | Business entity record layouts |
| Copybooks -- Framework | 13 | Common areas, messages, attributes |
| BMS Screen Maps | 17 | 3270 terminal UI definitions |
| BMS Generated Copybooks | 17 | Auto-generated symbolic maps |
| JCL Jobs | 38 | Batch orchestration |
| Assembler Utilities | 2 | Low-level system services |
| Optional Module Programs | ~13 | IMS/DB2/MQ extensions |
