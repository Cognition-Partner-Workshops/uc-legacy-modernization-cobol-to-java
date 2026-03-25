# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL | **Purpose:** Modernization assessment baseline

---

## Executive Summary

| Asset Type              | Count | Location                          |
|-------------------------|------:|-----------------------------------|
| COBOL Programs (Core)   |    31 | `app/cbl/`                        |
| COBOL Programs (Optional)|   10 | `app/app-*/cbl/`                  |
| Copybooks (Core)        |    30 | `app/cpy/`                        |
| Copybooks (BMS-generated)|   17 | `app/cpy-bms/`                    |
| Copybooks (Optional)    |    11 | `app/app-*/cpy/`                  |
| BMS Screen Maps (Core)  |    17 | `app/bms/`                        |
| BMS Screen Maps (Optional)|    4 | `app/app-*/bms/`                  |
| JCL Batch Jobs (Core)   |    38 | `app/jcl/`                        |
| JCL Batch Jobs (Optional)|     8 | `app/app-*/jcl/`                  |
| Assembler Programs      |     2 | `app/asm/`                        |
| JCL Procedures          |     2 | `app/proc/`                       |
| **Total Assets**        |**170**|                                   |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs

| # | Program    | Lines | Trans ID | Function                              | Business Domain    | Classification |
|---|------------|------:|----------|---------------------------------------|--------------------|----------------|
| 1 | COSGN00C   |   261 | CC00     | User signon / authentication          | Security           | UI + Logic     |
| 2 | COMEN01C   |   309 | CM00     | Main menu for regular users           | Navigation         | UI Controller  |
| 3 | COADM01C   |   288 | CA00     | Admin menu for admin users            | Navigation         | UI Controller  |
| 4 | COACTVWC   |   942 | CAVW     | Account view — display account details| Account Mgmt       | UI + Logic     |
| 5 | COACTUPC   | 4,237 | CAUP     | Account update — edit account/customer| Account Mgmt       | UI + Logic     |
| 6 | COCRDLIC   | 1,460 | CCLI     | Credit card list — browse cards       | Card Mgmt          | UI + Logic     |
| 7 | COCRDSLC   |   888 | CCDL     | Credit card detail view               | Card Mgmt          | UI + Logic     |
| 8 | COCRDUPC   | 1,560 | CCUP     | Credit card update                    | Card Mgmt          | UI + Logic     |
| 9 | COTRN00C   |   699 | CT00     | Transaction list — browse transactions| Transaction Mgmt   | UI + Logic     |
|10 | COTRN01C   |   330 | CT01     | Transaction view — single transaction | Transaction Mgmt   | UI + Logic     |
|11 | COTRN02C   |   783 | CT02     | Transaction add — new transaction     | Transaction Mgmt   | UI + Logic     |
|12 | COBIL00C   |   572 | CB00     | Bill payment — pay account balance    | Billing / Payments | UI + Logic     |
|13 | CORPT00C   |   649 | CR00     | Transaction reports — submit batch    | Reporting          | UI + Logic     |
|14 | COUSR00C   |   695 | CU00     | User list — browse users (Admin)      | User Admin         | UI + Logic     |
|15 | COUSR01C   |   299 | CU01     | User add — create user (Admin)        | User Admin         | UI + Logic     |
|16 | COUSR02C   |   414 | CU02     | User update — edit user (Admin)       | User Admin         | UI + Logic     |
|17 | COUSR03C   |   359 | CU03     | User delete — remove user (Admin)     | User Admin         | UI + Logic     |

### 1.2 Batch Programs

| # | Program    | Lines | Function                                        | Business Domain    | Classification  |
|---|------------|------:|-------------------------------------------------|--------------------|-----------------|
|18 | CBACT01C   |   430 | Read account file and write to output files      | Account Mgmt       | Data Processing |
|19 | CBACT02C   |   178 | Read and print card data file                    | Card Mgmt          | Report / Print  |
|20 | CBACT03C   |   178 | Read and print account cross-reference file      | Card Mgmt          | Report / Print  |
|21 | CBACT04C   |   652 | Interest calculation on accounts                 | Financial Calc     | Core Business   |
|22 | CBCUS01C   |   178 | Read and print customer data file                | Customer Mgmt      | Report / Print  |
|23 | CBTRN01C   |   494 | Post records from daily transaction file (v1)    | Transaction Mgmt   | Core Business   |
|24 | CBTRN02C   |   731 | Post records from daily transaction file (v2)    | Transaction Mgmt   | Core Business   |
|25 | CBTRN03C   |   649 | Print transaction detail report                  | Reporting          | Report / Print  |
|26 | CBSTM03A   |   924 | Statement generation — main driver               | Statements         | Core Business   |
|27 | CBSTM03B   |   230 | Statement generation — I/O sub-program            | Statements         | Sub-program     |
|28 | CBEXPORT   |   582 | Export all VSAM data to flat file                 | Data Migration     | Utility         |
|29 | CBIMPORT   |   487 | Import flat file data into VSAM                  | Data Migration     | Utility         |

### 1.3 Utility Programs

| # | Program    | Lines | Function                                        | Business Domain    | Classification |
|---|------------|------:|-------------------------------------------------|--------------------|----------------|
|30 | CSUTLDTC   |   157 | Date validation utility (calls CEEDAYS)          | Cross-cutting      | Utility        |
|31 | COBSWAIT   |    41 | Wait utility (parm in centiseconds)              | Cross-cutting      | Utility        |

---

## 2. COBOL Programs — Optional Modules

### 2.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program    | Lines | Function                                        | Technology         | Classification |
|---|------------|------:|-------------------------------------------------|--------------------|----------------|
|32 | COPAUA0C   | 1,026 | Card authorization decision (MQ trigger)         | CICS + IMS + MQ    | Core Business  |
|33 | COPAUS0C   | 1,032 | Summary view of authorization messages           | CICS + IMS + BMS   | UI + Logic     |
|34 | COPAUS1C   |   604 | Detail view of authorization message             | CICS + IMS + BMS   | UI + Logic     |
|35 | COPAUS2C   |   244 | Mark authorization message as fraud (DB2)        | CICS + IMS + DB2   | Core Business  |
|36 | CBPAUP0C   |   386 | Batch purge expired pending authorizations       | Batch + IMS        | Maintenance    |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program    | Lines | Function                                        | Technology         | Classification |
|---|------------|------:|-------------------------------------------------|--------------------|----------------|
|37 | COTRTLIC   | 2,098 | List transaction types for update/delete         | CICS + DB2         | UI + Logic     |
|38 | COTRTUPC   | 1,702 | Transaction type add/update                      | CICS + DB2         | UI + Logic     |
|39 | COBTUPDT   |   237 | Batch update of transaction types                | Batch + DB2        | Maintenance    |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program    | Lines | Function                                        | Technology         | Classification |
|---|------------|------:|-------------------------------------------------|--------------------|----------------|
|40 | CODATE01   |   524 | MQ request/response — system date service        | CICS + MQ          | Service        |
|41 | COACCT01   |   620 | MQ request/response — account inquiry service    | CICS + MQ          | Service        |

---

## 3. Copybooks — Core (`app/cpy/`)

### 3.1 Data Record Layouts (CV* — VSAM Record Structures)

| # | Copybook   | Lines | Record Name            | Record Len | Business Entity          |
|---|------------|------:|------------------------|------------|--------------------------|
| 1 | CVACT01Y   |    25 | ACCOUNT-RECORD         | 300 bytes  | Account Master           |
| 2 | CVACT02Y   |    21 | CARD-RECORD            | 150 bytes  | Credit Card              |
| 3 | CVACT03Y   |    14 | CARD-XREF-RECORD       | 50 bytes   | Card–Account Cross-Ref   |
| 4 | CVCUS01Y   |    46 | CUSTOMER-RECORD        | 500 bytes  | Customer Master          |
| 5 | CVCRD01Y   |    21 | CC-WORK-AREA           | N/A        | Card Work Area           |
| 6 | CVTRA01Y   |    13 | TRAN-CAT-BAL-RECORD    | 50 bytes   | Transaction Category Bal |
| 7 | CVTRA02Y   |    13 | DIS-GROUP-RECORD       | 50 bytes   | Disclosure Group         |
| 8 | CVTRA03Y   |    10 | TRAN-TYPE-RECORD       | 60 bytes   | Transaction Type         |
| 9 | CVTRA04Y   |    12 | TRAN-CAT-RECORD        | 60 bytes   | Transaction Category     |
|10 | CVTRA05Y   |    21 | TRAN-RECORD            | 350 bytes  | Transaction Master       |
|11 | CVTRA06Y   |    21 | DALYTRAN-RECORD        | 350 bytes  | Daily Transaction        |
|12 | CVTRA07Y   |    73 | TRANSACTION-DETAIL-RPT | N/A        | Report Layout            |
|13 | CVEXPORT   |    41 | EXPORT-RECORD          | 500 bytes  | Export/Import Layout     |
|14 | CUSTREC    |    28 | CUSTOMER-RECORD (alt)  | N/A        | Customer (Statement use) |
|15 | COSTM01    |    38 | TRNX-RECORD            | 350 bytes  | Transaction (Statements) |

### 3.2 Application Control Copybooks (CO* / CS*)

| # | Copybook   | Lines | Structure              | Purpose                           |
|---|------------|------:|------------------------|---------------------------------  |
|16 | COCOM01Y   |    52 | CARDDEMO-COMMAREA      | Application communication area    |
|17 | COMEN02Y   |    45 | CDEMO-MENU-*           | Menu definitions and options       |
|18 | COADM02Y   |    35 | CDEMO-ADMIN-OPT-*      | Admin menu option definitions      |
|19 | COTTL01Y   |    14 | CCDA-TITLE*            | Screen title constants             |
|20 | CSDAT01Y   |    18 | WS-CURDATE-DATA        | Current date/time formatting       |
|21 | CSMSG01Y   |    14 | CCDA-MSG-*             | Common application messages        |
|22 | CSMSG02Y   |    10 | ABEND-*                | Abend handling variables           |
|23 | CSUSR01Y   |    17 | SEC-USER-DATA          | User security record layout        |
|24 | CSLKPCDY   |     9 | LOOK-UP-*              | Lookup code table (states)         |
|25 | CSSETATY   |    14 | N/A                    | BMS attribute setting (REPLACING)  |
|26 | CSSTRPFY   |    12 | YYYY-STORE-PFKEY       | PF-key mapping paragraph           |
|27 | CSUTLDPY   |    10 | CSUTLDTC-*             | Date utility parameter area        |
|28 | CSUTLDWY   |    10 | WS-EDIT-DATE-*         | Date edit working storage          |
|29 | CODATECN   |     8 | CODATECN-REC           | Date conversion record             |
|30 | UNUSED1Y   |    10 | UNUSED-DATA            | Unused / placeholder copybook      |

### 3.3 BMS-Generated Copybooks (`app/cpy-bms/`)

| # | Copybook   | Associated BMS | Screen                             |
|---|------------|----------------|------------------------------------|
| 1 | COSGN00    | COSGN00.bms    | Signon Screen                      |
| 2 | COMEN01    | COMEN01.bms    | Main Menu                          |
| 3 | COADM01    | COADM01.bms    | Admin Menu                         |
| 4 | COACTVW    | COACTVW.bms    | Account View                       |
| 5 | COACTUP    | COACTUP.bms    | Account Update                     |
| 6 | COCRDLI    | COCRDLI.bms    | Credit Card List                   |
| 7 | COCRDSL    | COCRDSL.bms    | Credit Card Detail (View)          |
| 8 | COCRDUP    | COCRDUP.bms    | Credit Card Update                 |
| 9 | COTRN00    | COTRN00.bms    | Transaction List                   |
|10 | COTRN01    | COTRN01.bms    | Transaction View                   |
|11 | COTRN02    | COTRN02.bms    | Transaction Add                    |
|12 | COBIL00    | COBIL00.bms    | Bill Payment                       |
|13 | CORPT00    | CORPT00.bms    | Transaction Report                 |
|14 | COUSR00    | COUSR00.bms    | User List                          |
|15 | COUSR01    | COUSR01.bms    | User Add                           |
|16 | COUSR02    | COUSR02.bms    | User Update                        |
|17 | COUSR03    | COUSR03.bms    | User Delete                        |

---

## 4. Copybooks — Optional Modules

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook   | Purpose                                         |
|---|------------|-------------------------------------------------|
| 1 | CCPAUERY   | Authorization query record                       |
| 2 | CCPAURQY   | Authorization request record                     |
| 3 | CCPAURLY   | Authorization reply record                       |
| 4 | CIPAUDTY   | Authorization detail IMS segment                 |
| 5 | CIPAUSMY   | Authorization summary IMS segment                |
| 6 | IMSFUNCS   | IMS function codes                               |
| 7 | PADFLPCB   | IMS PCB — PADFL database                         |
| 8 | PASFLPCB   | IMS PCB — PASFL database                         |
| 9 | PAUTBPCB   | IMS PCB — PAUTB database                         |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook   | Purpose                                         |
|---|------------|-------------------------------------------------|
|10 | CSDB2RPY   | DB2 reply/result area                            |
|11 | CSDB2RWY   | DB2 read/write working storage                  |

---

## 5. BMS Screen Maps

### 5.1 Core Maps (`app/bms/`)

| # | Map File   | Lines | Mapset    | Screen Title          | Business Function            |
|---|------------|------:|-----------|-----------------------|------------------------------|
| 1 | COSGN00    |   210 | COSGN00   | Login Screen          | User authentication          |
| 2 | COMEN01    |   167 | COMEN01   | Main Menu             | Regular user navigation      |
| 3 | COADM01    |   115 | COADM01   | Admin Menu            | Admin user navigation        |
| 4 | COACTVW    |   156 | COACTVW   | Account View          | Display account details      |
| 5 | COACTUP    |   338 | COACTUP   | Account Update        | Edit account/customer info   |
| 6 | COCRDLI    |   344 | COCRDLI   | Card Listing          | Browse credit cards          |
| 7 | COCRDSL    |   157 | COCRDSL   | Card Selection        | View card details            |
| 8 | COCRDUP    |   172 | COCRDUP   | Card Update           | Edit card details            |
| 9 | COTRN00    |   464 | COTRN00   | Transaction List      | Browse transactions          |
|10 | COTRN01    |   273 | COTRN01   | Transaction View      | Display single transaction   |
|11 | COTRN02    |   307 | COTRN02   | Transaction Add       | Create new transaction       |
|12 | COBIL00    |   141 | COBIL00   | Bill Payment          | Pay account balance          |
|13 | CORPT00    |   231 | CORPT00   | Transaction Report    | Report selection criteria    |
|14 | COUSR00    |   463 | COUSR00   | User List             | Browse user accounts         |
|15 | COUSR01    |   164 | COUSR01   | Add User              | Create new user              |
|16 | COUSR02    |   169 | COUSR02   | Update User           | Modify user record           |
|17 | COUSR03    |   153 | COUSR03   | Delete User           | Remove user record           |

### 5.2 Optional Module Maps

| # | Map File   | Module          | Screen Title                    |
|---|------------|-----------------|---------------------------------|
|18 | COPAU00    | Authorization   | Authorization Summary           |
|19 | COPAU01    | Authorization   | Authorization Detail            |
|20 | COTRTLI    | Tran Type DB2   | Transaction Type List           |
|21 | COTRTUP    | Tran Type DB2   | Transaction Type Update         |

---

## 6. JCL Batch Jobs — Core (`app/jcl/`)

### 6.1 Data File Management

| # | JCL Job    | Lines | Function                                      | VSAM Dataset                                |
|---|------------|------:|-----------------------------------------------|---------------------------------------------|
| 1 | ACCTFILE   |   119 | Delete/define/load Account master VSAM        | CARDDEMO.ACCTDATA.VSAM.KSDS                |
| 2 | CARDFILE   |    88 | Delete/define/load Card master VSAM           | CARDDEMO.CARDDATA.VSAM.KSDS                |
| 3 | CUSTFILE   |    73 | Delete/define/load Customer master VSAM       | CARDDEMO.CUSTDATA.VSAM.KSDS                |
| 4 | XREFFILE   |   106 | Delete/define/load Card cross-reference VSAM  | CARDDEMO.CARDXREF.VSAM.KSDS                |
| 5 | TRANFILE   |    96 | Delete/define/load Transaction master VSAM    | CARDDEMO.TRANSACT.VSAM.KSDS                |
| 6 | DUSRSECJ   |    80 | Delete/define/load User security VSAM         | CARDDEMO.USRSEC.VSAM.KSDS                  |
| 7 | TRANTYPE   |    65 | Define transaction type VSAM                  | CARDDEMO.TRANTYPE.VSAM.KSDS                |
| 8 | DEFCUST    |    49 | Define customer file                          | CARDDEMO.CUSTDATA.VSAM.KSDS                |

### 6.2 Core Batch Processing

| # | JCL Job    | Lines | Function                                      | Program Executed |
|---|------------|------:|-----------------------------------------------|------------------|
| 9 | POSTTRAN   |    66 | Post daily transactions to master              | CBTRN02C         |
|10 | INTCALC    |    55 | Calculate interest on accounts                 | CBACT04C         |
|11 | COMBTRAN   |    47 | Combine/merge transaction files                | SORT             |
|12 | CREASTMT   |    97 | Create customer statements (text + HTML)       | CBSTM03A         |
|13 | TRANBKP    |    46 | Backup transaction VSAM to GDG                 | IDCAMS/REPRO     |
|14 | TRANIDX    |    35 | Define alternate index for transactions        | IDCAMS           |
|15 | TRANREPT   |    94 | Generate transaction detail report              | CBTRN03C + SORT  |

### 6.3 Reporting & Print

| # | JCL Job    | Lines | Function                                      | Program Executed |
|---|------------|------:|-----------------------------------------------|------------------|
|16 | READACCT   |    35 | Read and print account data                    | CBACT01C         |
|17 | READCARD   |    33 | Read and print card data                       | CBACT02C         |
|18 | READCUST   |    34 | Read and print customer data                   | CBCUS01C         |
|19 | READXREF   |    33 | Read and print cross-reference data            | CBACT03C         |
|20 | REPTFILE   |    39 | Define report output file                      | IDCAMS           |
|21 | PRTCATBL   |    35 | Print category balance file                    | N/A              |
|22 | TXT2PDF1   |    41 | Convert statement text to PDF                  | IKJEFT1B/TXT2PDF |

### 6.4 CICS File Control

| # | JCL Job    | Lines | Function                                      |
|---|------------|------:|-----------------------------------------------|
|23 | CLOSEFIL   |    73 | Close CICS files for batch processing          |
|24 | OPENFIL    |    73 | Reopen CICS files after batch processing       |

### 6.5 GDG / Index / Infrastructure

| # | JCL Job    | Lines | Function                                      |
|---|------------|------:|-----------------------------------------------|
|25 | DEFGDGB    |    29 | Define GDG base for transaction backups        |
|26 | DEFGDGD    |    29 | Define GDG base for daily transactions         |
|27 | DALYREJS   |    41 | Define daily rejection file                    |
|28 | DISCGRP    |    78 | Define disclosure group VSAM                   |
|29 | TCATBALF   |    60 | Define transaction category balance VSAM       |
|30 | TRANCATG   |    64 | Define transaction category VSAM               |
|31 | ESDSRRDS   |    48 | Define ESDS and RRDS sample datasets           |

### 6.6 Data Migration & Utilities

| # | JCL Job    | Lines | Function                                      | Program Executed |
|---|------------|------:|-----------------------------------------------|------------------|
|32 | CBEXPORT   |    55 | Export all VSAM data to flat file              | CBEXPORT         |
|33 | CBIMPORT   |    46 | Import flat file data into VSAM                | CBIMPORT         |
|34 | CBADMCDJ   |    31 | Admin card batch job                           | N/A              |
|35 | WAITSTEP   |    27 | Wait for specified centiseconds                | COBSWAIT         |

### 6.7 FTP & Internal Reader Jobs

| # | JCL Job    | Lines | Function                                      |
|---|------------|------:|-----------------------------------------------|
|36 | FTPJCL     |    42 | FTP file transfer to/from mainframe            |
|37 | INTRDRJ1   |    19 | Internal reader — trigger dependent job         |
|38 | INTRDRJ2   |    14 | Internal reader — dependent file copy           |

---

## 7. JCL Batch Jobs — Optional Modules

### 7.1 Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`)

| # | JCL Job    | Function                                      |
|---|------------|-----------------------------------------------|
|39 | CBPAUP0J   | Purge expired authorization records            |
|40 | DBPAUTP0   | Create IMS authorization database              |
|41 | LOADPADB   | Load IMS authorization data                    |
|42 | UNLDGSAM   | Unload GSAM data                               |
|43 | UNLDPADB   | Unload IMS authorization database              |

### 7.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`)

| # | JCL Job    | Function                                      |
|---|------------|-----------------------------------------------|
|44 | CREADB21   | Create DB2 tables for transaction types        |
|45 | MNTTRDB2   | Maintain transaction type DB2 data             |
|46 | TRANEXTR   | Extract transaction types from DB2             |

---

## 8. Assembler Programs (`app/asm/`)

| # | Program    | Function                                      |
|---|------------|-----------------------------------------------|
| 1 | COBDATFT   | Date formatting assembler routine              |
| 2 | MVSWAIT    | MVS wait assembler routine (centiseconds)      |

---

## 9. JCL Procedures (`app/proc/`)

| # | Procedure  | Function                                      |
|---|------------|-----------------------------------------------|
| 1 | REPROC     | IDCAMS REPRO utility procedure                 |
| 2 | TRANREPT   | Transaction report procedure                   |

---

## 10. Classification Summary

### By Business Domain

| Domain               | Online Programs | Batch Programs | Total |
|----------------------|----------------:|---------------:|------:|
| Security / Auth      |               1 |              0 |     1 |
| Navigation           |               2 |              0 |     2 |
| Account Management   |               2 |              1 |     3 |
| Card Management      |               3 |              1 |     4 |
| Transaction Mgmt     |               3 |              2 |     5 |
| Billing / Payments   |               1 |              0 |     1 |
| Reporting            |               1 |              2 |     3 |
| Statements           |               0 |              2 |     2 |
| User Admin           |               4 |              0 |     4 |
| Data Migration       |               0 |              2 |     2 |
| Cross-cutting Utils  |               0 |              2 |     2 |
| Authorization (Opt)  |               4 |              1 |     5 |
| Tran Type DB2 (Opt)  |               2 |              1 |     3 |
| VSAM-MQ (Opt)        |               2 |              0 |     2 |
| **Totals**           |          **25** |         **14** |**39** |

### By Technology Stack

| Technology           | Programs |
|----------------------|---------:|
| COBOL + CICS + VSAM  |       17 |
| COBOL Batch + VSAM   |       14 |
| COBOL + CICS + IMS   |        4 |
| COBOL + CICS + DB2   |        2 |
| COBOL Batch + DB2    |        1 |
| COBOL + CICS + MQ    |        2 |
| Assembler            |        2 |

### Naming Conventions

| Prefix | Meaning                         | Examples            |
|--------|---------------------------------|---------------------|
| CO*    | Online CICS program             | COSGN00C, COMEN01C  |
| CB*    | Batch COBOL program             | CBTRN02C, CBACT04C  |
| CS*    | Common service / utility        | CSUTLDTC, CSDAT01Y  |
| CV*    | Copybook — VSAM record layout   | CVACT01Y, CVTRA05Y  |
| CC*    | Copybook — work area / control  | CVCRD01Y            |
| *Y     | Copybook suffix                 | COCOM01Y, CSUSR01Y  |
