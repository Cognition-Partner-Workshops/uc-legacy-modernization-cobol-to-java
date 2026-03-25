# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM Mainframe (COBOL/CICS/VSAM/JCL)

## Executive Summary

| Asset Type             | Count | Notes                                    |
|------------------------|-------|------------------------------------------|
| COBOL Programs         | 31    | 16 online CICS + 15 batch                |
| Copybooks              | 30    | 13 data + 8 utility + 1 unused + 8 BMS-linked |
| BMS Screen Maps        | 17    | 3270 terminal UI definitions             |
| BMS-Generated Copybooks| 17    | Auto-generated from BMS maps             |
| JCL Batch Jobs         | 38    | Data load, processing, reporting, utility |
| Assembler Programs     | 2     | MVSWAIT, COBDATFT                        |
| Optional Modules       | 3     | IMS/DB2/MQ auth, DB2 tran-type, VSAM-MQ |

---

## 1. COBOL Programs (31)

### 1.1 Online CICS Programs (16)

| # | Program   | Lines | Transaction | Function                        | Domain            | BMS Map  |
|---|-----------|------:|-------------|----------------------------------|-------------------|----------|
| 1 | COSGN00C  |   260 | CC00        | Sign-on / Authentication         | Security          | COSGN00  |
| 2 | COMEN01C  |   308 | CM00        | Main Menu (regular users)        | Navigation        | COMEN01  |
| 3 | COADM01C  |   288 | CA00        | Admin Menu                       | Navigation        | COADM01  |
| 4 | COACTVWC  |   941 | CA01        | Account View                     | Account Mgmt      | COACTVW  |
| 5 | COACTUPC  | 4,236 | CA02        | Account Update                   | Account Mgmt      | COACTUP  |
| 6 | COCRDLIC  | 1,459 | CC01        | Card List                        | Card Mgmt         | COCRDLI  |
| 7 | COCRDSLC  |   887 | CC02        | Card Detail View                 | Card Mgmt         | COCRDSL  |
| 8 | COCRDUPC  | 1,560 | CC03        | Card Update                      | Card Mgmt         | COCRDUP  |
| 9 | COTRN00C  |   699 | CT00        | Transaction List                 | Transaction Mgmt  | COTRN00  |
|10 | COTRN01C  |   330 | CT01        | Transaction View                 | Transaction Mgmt  | COTRN01  |
|11 | COTRN02C  |   783 | CT02        | Transaction Add                  | Transaction Mgmt  | COTRN02  |
|12 | COBIL00C  |   572 | CB00        | Bill Payment                     | Billing           | COBIL00  |
|13 | CORPT00C  |   649 | CR00        | Transaction Report (submit batch)| Reporting          | CORPT00  |
|14 | COUSR00C  |   695 | CU00        | User List (Admin)                | User Admin        | COUSR00  |
|15 | COUSR01C  |   299 | CU01        | User Add (Admin)                 | User Admin        | COUSR01  |
|16 | COUSR02C  |   414 | CU02        | User Update (Admin)              | User Admin        | COUSR02  |

> **Note**: COUSR03C (User Delete, 359 lines, map COUSR03) is also online CICS but listed below as part of the full CRUD set.

### 1.2 Batch Programs (14)

| # | Program   | Lines | Function                                | Domain             | Called By JCL |
|---|-----------|------:|-----------------------------------------|--------------------|---------------|
| 1 | CBTRN01C  |   494 | Post daily transactions (simple)        | Transaction Posting| (dev/test)    |
| 2 | CBTRN02C  |   731 | Post daily transactions (production)    | Transaction Posting| POSTTRAN      |
| 3 | CBTRN03C  |   649 | Print transaction detail report         | Reporting          | TRANREPT      |
| 4 | CBACT01C  |   430 | Read account file, write derived files  | Account Processing | READACCT      |
| 5 | CBACT02C  |   178 | Read & print card data                  | Card Processing    | READCARD      |
| 6 | CBACT03C  |   178 | Read & print cross-reference data       | XREF Processing    | READXREF      |
| 7 | CBACT04C  |   652 | Interest calculation                    | Financial Calc     | INTCALC       |
| 8 | CBCUS01C  |   178 | Read & print customer data              | Customer Processing| READCUST      |
| 9 | CBSTM03A  |   924 | Generate account statements (text+HTML) | Statement Gen      | CREASTMT      |
|10 | CBSTM03B  |   230 | Subroutine for statement file I/O       | Statement Gen      | (called by CBSTM03A) |
|11 | CBEXPORT  |   582 | Export customer data for migration       | Data Migration     | CBEXPORT      |
|12 | CBIMPORT  |   487 | Import customer data from migration      | Data Migration     | CBIMPORT      |
|13 | COBSWAIT  |    41 | Wait utility (calls MVSWAIT assembler)  | Utility            | WAITSTEP      |
|14 | COUSR03C  |   359 | Delete user from USRSEC file            | User Admin         | (online CICS) |

### 1.3 Utility Programs (1)

| # | Program   | Lines | Function                     | Domain  |
|---|-----------|------:|------------------------------|---------|
| 1 | CSUTLDTC  |   157 | Date validation (CEEDAYS)    | Utility |

### 1.4 Assembler Programs (2)

| # | Program  | Location | Function                        |
|---|----------|----------|---------------------------------|
| 1 | MVSWAIT  | app/asm/ | Wait/sleep utility              |
| 2 | COBDATFT | app/asm/ | Date formatting utility         |

**Total SLOC (COBOL only)**: ~20,650 lines

---

## 2. Copybooks (30)

### 2.1 Business Data Structures (13)

| # | Copybook  | Lines | Record Len | Entity                      | Used By                            |
|---|-----------|------:|-----------:|-----------------------------|------------------------------------|
| 1 | CVACT01Y  |    22 |        300 | Account Master Record       | CBACT01C, CBACT04C, COACTUPC, COACTVWC, COTRN02C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT |
| 2 | CVACT02Y  |    14 |        150 | Card Data Record            | CBACT02C, COACTVWC, CBTRN01C, CBEXPORT, CBIMPORT |
| 3 | CVACT03Y  |    13 |         50 | Card Cross-Reference        | CBACT03C, CBACT04C, COACTUPC, COACTVWC, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT |
| 4 | CVCUS01Y  |    32 |        500 | Customer Master Record      | CBCUS01C, COACTUPC, COACTVWC, CBTRN01C, CBSTM03A, CBEXPORT, CBIMPORT |
| 5 | CVCRD01Y  |    17 |          - | Card Detail (online view)   | COACTUPC, COACTVWC                 |
| 6 | CSUSR01Y  |    10 |         80 | User Security Record        | COSGN00C, COUSR00C-03C, COACTUPC, COACTVWC |
| 7 | CVTRA01Y  |    13 |         50 | Transaction Category Balance| CBACT04C, CBTRN02C                 |
| 8 | CVTRA02Y  |    13 |         50 | Disclosure Group / Int Rate | CBACT04C                           |
| 9 | CVTRA03Y  |    10 |         60 | Transaction Type             | CBTRN03C                           |
|10 | CVTRA04Y  |    12 |         60 | Transaction Category Type    | CBTRN03C                           |
|11 | CVTRA05Y  |    21 |        350 | Transaction Record (master)  | COTRN00C-02C, CORPT00C, CBACT04C, CBTRN01C-02C, CBTRN03C, CBEXPORT, CBIMPORT |
|12 | CVTRA06Y  |    21 |        350 | Daily Transaction Record     | CBTRN01C, CBTRN02C                 |
|13 | CVTRA07Y  |    73 |          - | Transaction Report Layout    | CBTRN03C                           |

### 2.2 UI & Communication Structures (8)

| # | Copybook  | Lines | Purpose                                    |
|---|-----------|------:|--------------------------------------------|
| 1 | COCOM01Y  |    50 | Common communication area (COMMAREA)       |
| 2 | COMEN02Y  |    25 | Menu option definitions                    |
| 3 | COADM02Y  |    25 | Admin menu option definitions              |
| 4 | COTTL01Y  |    15 | Screen title/header area                   |
| 5 | CSDAT01Y  |    12 | Date display area                          |
| 6 | CSMSG01Y  |    10 | Message display area (single line)         |
| 7 | CSMSG02Y  |    10 | Extended message area                      |
| 8 | COSTM01   |    38 | Transaction record layout for reporting    |

### 2.3 Utility Copybooks (5)

| # | Copybook  | Lines | Purpose                              |
|---|-----------|------:|--------------------------------------|
| 1 | CSSETATY  |     6 | BMS attribute setting (COPY REPLACING) |
| 2 | CSSTRPFY  |    20 | String strip/pad functions           |
| 3 | CSUTLDPY  |    15 | Date utility parameters              |
| 4 | CSUTLDWY  |    20 | Date utility working storage         |
| 5 | CSLKPCDY  |    20 | Lookup code tables                   |

### 2.4 Data Format & Conversion (3)

| # | Copybook  | Lines | Purpose                          |
|---|-----------|------:|----------------------------------|
| 1 | CODATECN  |    10 | Date conversion record           |
| 2 | CUSTREC   |    32 | Customer record (statement gen)  |
| 3 | CVEXPORT  |    45 | Export/Import multi-record layout |

### 2.5 Unused / Placeholder (1)

| # | Copybook  | Lines | Purpose          |
|---|-----------|------:|------------------|
| 1 | UNUSED1Y  |    10 | Unused structure |

---

## 3. BMS Screen Maps (17)

| # | BMS Map  | Screen       | Associated Program | Function                  |
|---|----------|--------------|-------------------|---------------------------|
| 1 | COSGN00  | Signon       | COSGN00C          | User login                |
| 2 | COMEN01  | Main Menu    | COMEN01C          | Navigation menu           |
| 3 | COADM01  | Admin Menu   | COADM01C          | Admin navigation          |
| 4 | COACTVW  | Account View | COACTVWC          | Display account details   |
| 5 | COACTUP  | Account Update| COACTUPC         | Edit account fields       |
| 6 | COCRDLI  | Card List    | COCRDLIC          | Paginated card listing    |
| 7 | COCRDSL  | Card View    | COCRDSLC          | Card detail display       |
| 8 | COCRDUP  | Card Update  | COCRDUPC          | Edit card fields          |
| 9 | COTRN00  | Tran List    | COTRN00C          | Paginated tran listing    |
|10 | COTRN01  | Tran View    | COTRN01C          | Transaction detail view   |
|11 | COTRN02  | Tran Add     | COTRN02C          | Add new transaction       |
|12 | COBIL00  | Bill Payment | COBIL00C          | Process bill payment      |
|13 | CORPT00  | Report       | CORPT00C          | Submit batch report       |
|14 | COUSR00  | User List    | COUSR00C          | Admin user listing        |
|15 | COUSR01  | User Add     | COUSR01C          | Add new user              |
|16 | COUSR02  | User Update  | COUSR02C          | Edit existing user        |
|17 | COUSR03  | User Delete  | COUSR03C          | Delete user confirmation  |

Each BMS map has a corresponding auto-generated copybook in `app/cpy-bms/`.

---

## 4. JCL Batch Jobs (38)

### 4.1 Data Initialization & Refresh (11)

| # | JCL Job   | Lines | Function                                      | Primary Dataset(s)                 |
|---|-----------|------:|-----------------------------------------------|------------------------------------|
| 1 | ACCTFILE  |    46 | Define & load Account Master VSAM             | ACCTDATA.VSAM.KSDS                |
| 2 | CARDFILE  |   123 | Define & load Card Data VSAM + alt indexes    | CARDDATA.VSAM.KSDS                |
| 3 | CUSTFILE  |    80 | Define & load Customer Master VSAM            | CUSTDATA.VSAM.KSDS                |
| 4 | XREFFILE  |   100 | Define & load Card Cross-Reference VSAM + AIX | CARDXREF.VSAM.KSDS                |
| 5 | TRANFILE  |   110 | Define & load Transaction Master VSAM + AIX   | TRANSACT.VSAM.KSDS                |
| 6 | DUSRSECJ  |    60 | Load User Security VSAM from flat file        | USRSEC.VSAM.KSDS                  |
| 7 | TCATBALF  |    56 | Define & load Tran Category Balance VSAM      | TCATBALF.VSAM.KSDS                |
| 8 | DISCGRP   |    56 | Define & load Disclosure Group VSAM           | DISCGRP.VSAM.KSDS                 |
| 9 | TRANTYPE  |    56 | Define & load Transaction Type VSAM           | TRANTYPE.VSAM.KSDS                |
|10 | TRANCATG  |    56 | Define & load Transaction Category VSAM       | TRANCATG.VSAM.KSDS                |
|11 | DALYREJS  |    26 | Define Daily Rejects VSAM cluster             | DALYREJS                           |

### 4.2 Core Batch Processing (6)

| # | JCL Job   | Lines | Program    | Function                                       |
|---|-----------|------:|------------|-------------------------------------------------|
| 1 | POSTTRAN  |    35 | CBTRN02C   | Post daily transactions to master               |
| 2 | INTCALC   |    38 | CBACT04C   | Calculate interest on category balances         |
| 3 | CREASTMT  |    97 | CBSTM03A   | Generate account statements (text + HTML)       |
| 4 | TRANREPT  |    54 | CBTRN03C   | Generate daily transaction detail report        |
| 5 | COMBTRAN  |    45 | SORT       | Combine daily + backup transactions             |
| 6 | TRANBKP   |    30 | IDCAMS     | Backup transaction master to GDG                |

### 4.3 Data Read / Print Utilities (4)

| # | JCL Job   | Lines | Program    | Function                         |
|---|-----------|------:|------------|----------------------------------|
| 1 | READACCT  |    52 | CBACT01C   | Read & print account data        |
| 2 | READCARD  |    24 | CBACT02C   | Read & print card data           |
| 3 | READCUST  |    33 | CBCUS01C   | Read & print customer data       |
| 4 | READXREF  |    24 | CBACT03C   | Read & print cross-reference     |

### 4.4 Export / Import (2)

| # | JCL Job   | Lines | Program    | Function                         |
|---|-----------|------:|------------|----------------------------------|
| 1 | CBEXPORT  |    47 | CBEXPORT   | Export data for branch migration |
| 2 | CBIMPORT  |    42 | CBIMPORT   | Import data from export file     |

### 4.5 CICS File Management (2)

| # | JCL Job   | Lines | Function                           |
|---|-----------|------:|------------------------------------|
| 1 | CLOSEFIL  |    19 | Close CICS-owned VSAM files        |
| 2 | OPENFIL   |    19 | Open CICS-owned VSAM files         |

### 4.6 Index & GDG Management (4)

| # | JCL Job   | Lines | Function                                      |
|---|-----------|------:|-----------------------------------------------|
| 1 | TRANIDX   |    49 | Define alternate indexes on TRANSACT           |
| 2 | DEFGDGB   |    24 | Define GDG bases (backup catalogs)             |
| 3 | DEFGDGD   |    87 | Define GDG bases + seed initial generations    |
| 4 | DEFCUST   |    37 | Define customer VSAM clusters                  |

### 4.7 Reporting & Output (4)

| # | JCL Job   | Lines | Function                                |
|---|-----------|------:|-----------------------------------------|
| 1 | REPTFILE  |    26 | Define report output VSAM cluster       |
| 2 | PRTCATBL  |    49 | Print/backup category balance file      |
| 3 | TXT2PDF1  |    41 | Convert text statements to PDF          |
| 4 | ESDSRRDS  |    70 | Demo: ESDS and RRDS VSAM types          |

### 4.8 Utility & Infrastructure (5)

| # | JCL Job   | Lines | Function                               |
|---|-----------|------:|----------------------------------------|
| 1 | WAITSTEP  |    12 | Execute wait utility (COBSWAIT)        |
| 2 | FTPJCL    |    42 | FTP file transfer to/from mainframe    |
| 3 | INTRDRJ1  |    19 | Internal reader - trigger INTRDRJ2     |
| 4 | INTRDRJ2  |    14 | Internal reader - chained job          |
| 5 | CBADMCDJ  |    19 | Load CICS CSD definitions              |

---

## 5. Optional Extension Modules

### 5.1 Authorization Module (IMS/DB2/MQ)
**Location**: `app/app-authorization-ims-db2-mq/`

Demonstrates IMS DB, DB2, and MQ integration for payment authorization with fraud detection.

| Program   | Type   | Function                               |
|-----------|--------|----------------------------------------|
| COPAUA0C  | Online | MQ trigger for authorization           |
| COPAUS0C  | Online | Authorization summary                  |
| COPAUS1C  | Online | Authorization details                  |
| COPAUS2C  | Online | Fraud marking (writes to DB2)          |
| CBPAUP0C  | Batch  | Batch purge of old authorizations      |

### 5.2 Transaction Type DB2 Module
**Location**: `app/app-transaction-type-db2/`

DB2 CRUD operations for transaction type management.

| Program   | Type   | Function                               |
|-----------|--------|----------------------------------------|
| COTRTUPC  | Online | Add/Edit transaction type (DB2)        |
| COTRTLIC  | Online | List/Delete transaction types (DB2)    |
| COBTUPDT  | Batch  | Batch update transaction types         |

### 5.3 VSAM-MQ Module
**Location**: `app/app-vsam-mq/`

MQ request/response for system queries.

| Program   | Type   | Function                               |
|-----------|--------|----------------------------------------|
| CODATE01  | Online | System date inquiry via MQ             |
| COACCT01  | Online | Account inquiry via MQ                 |

---

## 6. Classification Summary

### By Business Domain

| Domain              | Online Programs | Batch Programs | Total |
|---------------------|----------------:|---------------:|------:|
| Account Management  |               2 |              1 |     3 |
| Card Management     |               3 |              1 |     4 |
| Transaction Mgmt    |               3 |              3 |     6 |
| Billing & Payments  |               1 |              0 |     1 |
| Reporting           |               1 |              3 |     4 |
| User Admin/Security |               4 |              0 |     4 |
| Navigation/Menus    |               2 |              0 |     2 |
| Data Migration      |               0 |              2 |     2 |
| Utility             |               0 |              2 |     2 |
| **Totals**          |          **16** |         **12** |**28** |

### By Modernization Wave (Recommended)

| Wave | Programs | Rationale |
|------|----------|-----------|
| **Wave 1 - Foundation** | COSGN00C, COMEN01C, COADM01C, CSUTLDTC, COBSWAIT | Auth, navigation, utilities - shared dependencies |
| **Wave 2 - Account & Card** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | Core account/card CRUD - high business value |
| **Wave 3 - Transactions** | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN01C, CBTRN02C | Transaction lifecycle - complex business logic |
| **Wave 4 - Reporting & Batch** | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B, CBACT04C | Report generation, interest calc - batch focus |
| **Wave 5 - Data & Utility** | CBACT01C-03C, CBCUS01C, CBEXPORT, CBIMPORT | Data read/print, migration utilities |
| **Wave 6 - Extensions** | COUSR00C-03C, optional modules | Admin CRUD, optional IMS/DB2/MQ |
