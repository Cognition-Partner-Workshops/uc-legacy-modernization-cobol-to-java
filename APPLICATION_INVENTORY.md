# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Platform:** IBM z/OS Mainframe | COBOL / CICS / VSAM / JCL / BMS

---

## Executive Summary

CardDemo is a mainframe-based credit card management application comprising **44 COBOL programs**, **30 copybooks**, **38 JCL jobs**, **17 BMS screen maps**, **2 assembler modules**, **2 JCL procedures**, and **3 optional extension modules** (IMS/DB2/MQ authorization, DB2 transaction types, VSAM-MQ integration). The system handles account management, card management, transaction processing, bill payments, reporting, and user administration through both online CICS transactions and batch processing.

---

## 1. COBOL Programs (44 Total)

### 1.1 Core Online CICS Programs (18 programs)

| # | Program | Lines | CICS Tran | Function | Business Domain | Screen |
|---|---------|-------|-----------|----------|-----------------|--------|
| 1 | COSGN00C | 260 | CC00 | User sign-on / authentication | Security | COSGN00 |
| 2 | COMEN01C | 308 | CM00 | Main menu navigation | Navigation | COMEN01 |
| 3 | COACTVWC | 941 | CA00 | View account details | Account Mgmt | COACTVW |
| 4 | COACTUPC | 4,236 | CA01 | Update account / customer info | Account Mgmt | COACTUP |
| 5 | COCRDLIC | 1,459 | CC01 | List credit cards (paginated) | Card Mgmt | COCRDLI |
| 6 | COCRDSLC | 887 | CC02 | View credit card details | Card Mgmt | COCRDSL |
| 7 | COCRDUPC | 1,560 | CC03 | Update credit card information | Card Mgmt | COCRDUP |
| 8 | COTRN00C | 699 | CT00 | List transactions (paginated) | Transactions | COTRN00 |
| 9 | COTRN01C | 330 | CT01 | View transaction details | Transactions | COTRN01 |
| 10 | COTRN02C | 783 | CT02 | Add new transaction | Transactions | COTRN02 |
| 11 | CORPT00C | 649 | CR00 | Generate transaction reports | Reporting | CORPT00 |
| 12 | COBIL00C | 572 | CB00 | Bill payment processing | Billing | COBIL00 |
| 13 | COADM01C | 288 | CA90 | Admin menu | Administration | COADM01 |
| 14 | COUSR00C | 695 | CU00 | List users (paginated) | User Admin | COUSR00 |
| 15 | COUSR01C | 299 | CU01 | Add new user | User Admin | COUSR01 |
| 16 | COUSR02C | 414 | CU02 | Update user details | User Admin | COUSR02 |
| 17 | COUSR03C | 359 | CU03 | Delete user | User Admin | COUSR03 |
| 18 | CSUTLDTC | 157 | -- | Date validation utility (calls CEEDAYS) | Utility | -- |

### 1.2 Core Batch Programs (13 programs)

| # | Program | Lines | Function | Business Domain | I/O Pattern |
|---|---------|-------|----------|-----------------|-------------|
| 19 | CBACT01C | 430 | Read accounts, write to multiple output formats | Account Mgmt | Read VSAM, Write Sequential |
| 20 | CBACT02C | 178 | Read and display card file records | Card Mgmt | Read VSAM |
| 21 | CBACT03C | 178 | Read and display cross-reference file | Card Mgmt | Read VSAM |
| 22 | CBACT04C | 652 | Calculate interest on account balances | Financial Calc | Read/Write VSAM |
| 23 | CBCUS01C | 178 | Read and display customer file records | Customer Mgmt | Read VSAM |
| 24 | CBTRN01C | 494 | Read and display transaction file records | Transactions | Read VSAM |
| 25 | CBTRN02C | 731 | Post daily transactions to master file | Transactions | Read Seq, Read/Write VSAM |
| 26 | CBTRN03C | 649 | Generate daily transaction report | Reporting | Read VSAM, Write Sequential |
| 27 | CBSTM03A | 924 | Generate account statements (text + HTML) | Reporting | Read VSAM, Write Sequential |
| 28 | CBSTM03B | 230 | File I/O subroutine for statement generation | Reporting | Subroutine |
| 29 | CBEXPORT | 582 | Export all VSAM data to sequential file | Data Migration | Read VSAM, Write Sequential |
| 30 | CBIMPORT | 487 | Import data from sequential file to VSAM | Data Migration | Read Sequential, Write VSAM |
| 31 | COBSWAIT | 41 | Wait/sleep utility program | Utility | N/A |

### 1.3 Optional Module: Authorization (IMS/DB2/MQ) -- 8 programs

| # | Program | Lines | Function | Business Domain | Technology |
|---|---------|-------|----------|-----------------|------------|
| 32 | COPAUA0C | 1,026 | MQ trigger: receive authorization requests | Authorization | CICS + MQ |
| 33 | COPAUS0C | 1,032 | Display pending authorization summary | Authorization | CICS + DB2 |
| 34 | COPAUS1C | 604 | View authorization details | Authorization | CICS + DB2 |
| 35 | COPAUS2C | 244 | Mark transaction as fraud in DB2 | Authorization | CICS + DB2 |
| 36 | CBPAUP0C | 386 | Batch purge old authorizations | Authorization | Batch + DB2 |
| 37 | DBUNLDGS | 366 | Unload IMS database segments | Authorization | Batch + IMS |
| 38 | PAUDBLOD | 369 | Load authorization data to IMS DB | Authorization | Batch + IMS |
| 39 | PAUDBUNL | 317 | Unload authorization data from IMS DB | Authorization | Batch + IMS |

### 1.4 Optional Module: Transaction Type DB2 -- 3 programs

| # | Program | Lines | Function | Business Domain | Technology |
|---|---------|-------|----------|-----------------|------------|
| 40 | COTRTLIC | 2,098 | List/delete transaction types | Config Mgmt | CICS + DB2 |
| 41 | COTRTUPC | 1,702 | Add/update transaction types | Config Mgmt | CICS + DB2 |
| 42 | COBTUPDT | 237 | Batch update transaction types | Config Mgmt | Batch + DB2 |

### 1.5 Optional Module: VSAM-MQ -- 2 programs

| # | Program | Lines | Function | Business Domain | Technology |
|---|---------|-------|----------|-----------------|------------|
| 43 | COACCT01 | 620 | MQ-based account inquiry service | Account Mgmt | CICS + MQ + VSAM |
| 44 | CODATE01 | 524 | MQ-based system date service | Utility | CICS + MQ |

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C (Sign-on) |
| `CB*` | Batch COBOL program | CBTRN02C (Post transactions) |
| `CS*` | Shared utility/subroutine | CSUTLDTC (Date utility) |
| `CV*` | Copybook (data structure) | CVACT01Y (Account record) |
| `COP*` | Optional module CICS program | COPAUA0C (Auth MQ trigger) |

---

## 2. Copybooks (30 Total)

### 2.1 Business Data Record Layouts (16 copybooks)

| # | Copybook | Lines | Record | Size | Business Entity |
|---|----------|-------|--------|------|-----------------|
| 1 | CVACT01Y | 20 | ACCOUNT-RECORD | 300 bytes | Account master |
| 2 | CVACT02Y | 14 | CARD-RECORD | 150 bytes | Credit card master |
| 3 | CVACT03Y | 11 | CARD-XREF-RECORD | 50 bytes | Card-to-account cross-reference |
| 4 | CVCUS01Y | 26 | CUSTOMER-RECORD | 500 bytes | Customer master |
| 5 | CVCRD01Y | 46 | CARD-DATA-RECORD | ~500 bytes | Card detail (online display) |
| 6 | CVTRA01Y | 13 | TRAN-CAT-BAL-RECORD | 50 bytes | Transaction category balance |
| 7 | CVTRA02Y | 13 | DIS-GROUP-RECORD | 50 bytes | Disclosure/interest rate group |
| 8 | CVTRA03Y | 10 | TRAN-TYPE-RECORD | 60 bytes | Transaction type reference |
| 9 | CVTRA04Y | 12 | TRAN-CAT-RECORD | 60 bytes | Transaction category reference |
| 10 | CVTRA05Y | 21 | TRAN-RECORD | 350 bytes | Transaction master |
| 11 | CVTRA06Y | 21 | DALYTRAN-RECORD | 350 bytes | Daily transaction (pending) |
| 12 | CVTRA07Y | 73 | Report headers/detail lines | N/A | Transaction report layout |
| 13 | COSTM01 | 38 | TRNX-RECORD | 350 bytes | Statement transaction layout |
| 14 | CUSTREC | 26 | Customer record (alternate) | ~500 bytes | Customer (alt layout) |
| 15 | CVEXPORT | 103 | Export record layouts | Variable | Data export/import format |
| 16 | UNUSED1Y | 10 | UNUSED-DATA | 80 bytes | Unused placeholder |

### 2.2 System/Infrastructure Copybooks (8 copybooks)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 17 | COCOM01Y | 47 | COMMAREA - inter-program communication area |
| 18 | COMEN02Y | 101 | Menu option definitions and screen routing |
| 19 | COTTL01Y | 27 | Standard screen title/header layout |
| 20 | COADM02Y | 62 | Admin menu option definitions |
| 21 | CSMSG01Y | 24 | Standard message area (short) |
| 22 | CSMSG02Y | 35 | Standard message area (extended) |
| 23 | CSSETATY | 30 | Screen attribute setting utilities |
| 24 | CSSTRPFY | 85 | String parsing/formatting utility |

### 2.3 Utility/Processing Copybooks (6 copybooks)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 25 | CSUSR01Y | 26 | User security record layout |
| 26 | CSDAT01Y | 58 | Date conversion/formatting structures |
| 27 | CODATECN | 52 | Date conversion (assembler interface) |
| 28 | CSLKPCDY | 1,318 | Lookup code tables (country, state, etc.) |
| 29 | CSUTLDPY | 375 | Date utility parameter block |
| 30 | CSUTLDWY | 89 | Date utility working storage |

---

## 3. BMS Screen Maps (17 Total)

| # | Map | Screen Title | Associated Program | Function |
|---|-----|-------------|-------------------|----------|
| 1 | COSGN00 | Login Screen | COSGN00C | User authentication |
| 2 | COMEN01 | Main Menu Screen | COMEN01C | Application navigation |
| 3 | COACTVW | Account View | COACTVWC | Display account details |
| 4 | COACTUP | Account Update | COACTUPC | Edit account/customer info |
| 5 | COCRDLI | Card List | COCRDLIC | Paginated card listing |
| 6 | COCRDSL | Card Detail View | COCRDSLC | Display card details |
| 7 | COCRDUP | Card Update | COCRDUPC | Edit card information |
| 8 | COTRN00 | Transaction List | COTRN00C | Paginated transaction listing |
| 9 | COTRN01 | Transaction View | COTRN01C | Display transaction details |
| 10 | COTRN02 | Transaction Add | COTRN02C | Enter new transaction |
| 11 | CORPT00 | Transaction Reports | CORPT00C | Report parameter entry |
| 12 | COBIL00 | Bill Payment | COBIL00C | Process bill payments |
| 13 | COADM01 | Admin Menu | COADM01C | Admin navigation |
| 14 | COUSR00 | List Users | COUSR00C | Paginated user listing |
| 15 | COUSR01 | Add User | COUSR01C | New user entry form |
| 16 | COUSR02 | Update User | COUSR02C | Edit user details |
| 17 | COUSR03 | Delete User | COUSR03C | Confirm user deletion |

**BMS-Generated Copybooks:** Each map has a corresponding copybook in `app/cpy-bms/` (e.g., `COSGN00.CPY`) containing the symbolic map data structures used by programs.

---

## 4. JCL Batch Jobs (38 Total)

### 4.1 Data Loading/Refresh Jobs (9 jobs)

| # | JCL Job | Function | VSAM Dataset(s) |
|---|---------|----------|-----------------|
| 1 | ACCTFILE | Define + load account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | Define + load card master VSAM | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | Define + load customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | Define + load card cross-reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | Define + load transaction master VSAM | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | Define + load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | Define + load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| 8 | REPTFILE | Define report file VSAM | DALYREPT.VSAM.KSDS |
| 9 | DEFCUST | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |

### 4.2 Batch Processing Jobs (8 jobs)

| # | JCL Job | Program | Function |
|---|---------|---------|----------|
| 10 | POSTTRAN | CBTRN02C | Post daily transactions to master |
| 11 | INTCALC | CBACT04C | Calculate interest on balances |
| 12 | COMBTRAN | SORT | Combine/merge transaction files |
| 13 | CREASTMT | CBSTM03A | Generate account statements (text + HTML) |
| 14 | TRANREPT | CBTRN03C | Generate daily transaction report |
| 15 | TRANBKP | IDCAMS | Backup transaction VSAM to sequential |
| 16 | CBEXPORT | CBEXPORT | Export all data to flat file |
| 17 | CBIMPORT | CBIMPORT | Import data from flat file |

### 4.3 CICS File Management Jobs (2 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 18 | CLOSEFIL | Close CICS files for batch processing |
| 19 | OPENFIL | Reopen CICS files after batch completes |

### 4.4 VSAM Utility/Definition Jobs (9 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 20 | DEFGDGB | Define GDG bases for backup datasets |
| 21 | DEFGDGD | Define GDG bases for daily datasets |
| 22 | TRANIDX | Define/build alternate index on transactions |
| 23 | TRANCATG | Define transaction category balance VSAM |
| 24 | TCATBALF | Define transaction category balance file |
| 25 | DISCGRP | Define disclosure group VSAM |
| 26 | ESDSRRDS | Define ESDS/RRDS VSAM clusters |
| 27 | DALYREJS | Define daily rejects sequential dataset |
| 28 | PRTCATBL | Print transaction category balance |

### 4.5 Data Display/Diagnostic Jobs (4 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 29 | READACCT | Read and display account records |
| 30 | READCARD | Read and display card records |
| 31 | READCUST | Read and display customer records |
| 32 | READXREF | Read and display cross-reference records |

### 4.6 Utility/Infrastructure Jobs (6 jobs)

| # | JCL Job | Function |
|---|---------|----------|
| 33 | CBADMCDJ | Compile/link CardDemo batch programs |
| 34 | FTPJCL | FTP file transfer job |
| 35 | INTRDRJ1 | Internal reader - trigger INTRDRJ2 |
| 36 | INTRDRJ2 | Internal reader - copy/backup files |
| 37 | TXT2PDF1 | Convert text statement to PDF |
| 38 | WAITSTEP | Wait step utility |

---

## 5. Assembler Modules (2 Total)

| # | Module | Function |
|---|--------|----------|
| 1 | COBDATFT | Date formatting (called from COBOL via CALL) |
| 2 | MVSWAIT | MVS wait/delay utility |

---

## 6. JCL Procedures (2 Total)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC | Reusable reporting procedure |
| 2 | TRANREPT | Transaction report procedure |

---

## 7. Other Artifacts

### Scheduler Configurations
| File | Type | Purpose |
|------|------|---------|
| CardDemo.ca7 | CA-7 | Batch job scheduling definitions |
| CardDemo.controlm | Control-M | Batch job scheduling definitions |

### CICS Resource Definitions
| File | Purpose |
|------|---------|
| app/csd/CARDDEMO.CSD | CICS System Definition - maps, programs, transactions, files |

### Data Files
| Directory | Format | Purpose |
|-----------|--------|---------|
| app/data/ASCII/ | ASCII | Sample test data for development |
| app/data/EBCDIC/ | EBCDIC | Production-format data for mainframe upload |

---

## 8. Codebase Metrics Summary

| Category | Count | Total Lines |
|----------|-------|-------------|
| Core COBOL Programs | 31 | 20,650 |
| Optional Module Programs | 13 | 9,525 |
| **Total COBOL Programs** | **44** | **30,175** |
| Copybooks (data) | 30 | 2,786 |
| BMS Maps | 17 | -- |
| BMS-Generated Copybooks | 17 | -- |
| JCL Jobs | 38 | 2,429 |
| Assembler Modules | 2 | -- |
| JCL Procedures | 2 | -- |
| **Grand Total Artifacts** | **150+** | **35,000+** |
