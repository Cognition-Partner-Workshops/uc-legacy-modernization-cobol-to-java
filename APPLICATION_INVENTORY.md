# APPLICATION INVENTORY — CardDemo COBOL Codebase

> **Generated:** 2026-03-26 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> CardDemo is a mainframe credit-card management application built on COBOL/CICS/VSAM/JCL.
> It simulates account management, card management, transactions, bill payments, and reporting.

---

## Summary Counts

| Artifact Type | Core | Auth Module (IMS/DB2/MQ) | Tran-Type Module (DB2) | VSAM-MQ Module | **Total** |
|---|---|---|---|---|---|
| COBOL Programs | 33 | 8 | 3 | 2 | **46** |
| Copybooks (data) | 32 | 9 | 2 | 0 | **43** |
| BMS Maps | 17 | 2 | 2 | 0 | **21** |
| BMS-Generated Copybooks | 17 | — | — | — | **17** |
| JCL Jobs | 38 | 5 | 3 | 0 | **46** |
| Assembler Programs | 2 | — | — | — | **2** |
| JCL Procedures | 2 | — | — | — | **2** |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program | LOC | CICS Trans | Classification | Description |
|---|---------|-----|------------|----------------|-------------|
| 1 | **COSGN00C** | 260 | CC00 | Security / Auth | Sign-on screen — authenticates users against USRSEC VSAM |
| 2 | **COMEN01C** | 308 | CM00 | Navigation | Main menu — dispatches to functional screens via XCTL |
| 3 | **COADM01C** | 288 | CA00 | Navigation (Admin) | Admin menu — dispatches to user CRUD and DB2 admin screens |
| 4 | **COACTVWC** | 941 | CA01 | Account Mgmt | Account view — reads ACCTDAT, CARDDAT, CUSTDAT (read-only) |
| 5 | **COACTUPC** | 4,236 | CA02 | Account Mgmt | Account update — full CRUD on account, card, customer records |
| 6 | **COCRDLIC** | 1,459 | CC01 | Card Mgmt | Credit card list — browse CARDDAT with pagination (STARTBR/READNEXT) |
| 7 | **COCRDSLC** | 887 | CC02 | Card Mgmt | Credit card view — display single card + customer details |
| 8 | **COCRDUPC** | 1,560 | CC03 | Card Mgmt | Credit card update — modify card attributes |
| 9 | **COTRN00C** | 699 | CT00 | Transaction Mgmt | Transaction list — browse TRANSACT file |
| 10 | **COTRN01C** | 330 | CT01 | Transaction Mgmt | Transaction view — display single transaction detail |
| 11 | **COTRN02C** | 783 | CT02 | Transaction Mgmt | Transaction add — create new transaction with validation |
| 12 | **CORPT00C** | 649 | CR00 | Reporting | Transaction report request — parameters for batch report |
| 13 | **COBIL00C** | 572 | CB00 | Bill Payment | Bill payment — post payment against account balance |
| 14 | **COUSR00C** | 695 | CU00 | User Admin | User list — browse USRSEC VSAM with pagination |
| 15 | **COUSR01C** | 299 | CU01 | User Admin | User add — create new security user |
| 16 | **COUSR02C** | 414 | CU02 | User Admin | User update — modify user attributes/password |
| 17 | **COUSR03C** | 359 | CU03 | User Admin | User delete — remove security user record |
| 18 | **CSUTLDTC** | 157 | — | Utility | Date/time conversion utility — called by CORPT00C, COTRN02C |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program | LOC | Classification | Description |
|---|---------|-----|----------------|-------------|
| 19 | **CBACT01C** | 430 | Data Read | Read account master VSAM → multiple output formats (PS, VBPS, array) |
| 20 | **CBACT02C** | 178 | Data Read | Read card master VSAM sequentially |
| 21 | **CBACT03C** | 178 | Data Read | Read card cross-reference VSAM sequentially |
| 22 | **CBACT04C** | 652 | Interest Calc | Interest calculation — reads ACCTDAT, XREFFILE, DISCGRP; writes TCATBALF, TRANSACT |
| 23 | **CBCUS01C** | 178 | Data Read | Read customer master VSAM sequentially |
| 24 | **CBTRN01C** | 494 | Transaction Processing | Daily transaction reader — joins DALYTRAN with CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE |
| 25 | **CBTRN02C** | 731 | Transaction Posting | Core transaction posting — validates and posts daily transactions; writes rejects |
| 26 | **CBTRN03C** | 649 | Reporting | Transaction report generator — reads TRANFILE, XREF, TRANTYPE, TRANCATG; produces print report |
| 27 | **CBSTM03A** | 924 | Statement Gen | Statement generation driver — produces plain-text and HTML statements |
| 28 | **CBSTM03B** | 230 | Statement Gen | Statement sub-program — called by CBSTM03A to fetch transaction details |
| 29 | **CBEXPORT** | 582 | Data Export | Export all VSAM files to a single sequential export file |
| 30 | **CBIMPORT** | 487 | Data Import | Import sequential export file back into individual output files |

### 1.3 Utility Programs

| # | Program | LOC | Classification | Description |
|---|---------|-----|----------------|-------------|
| 31 | **COBSWAIT** | 52 | Utility | Wait utility — CALLs MVSWAIT assembler routine for JCL wait steps |

---

## 2. Optional Module Programs

### 2.1 Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | LOC | Classification | Description |
|---|---------|-----|----------------|-------------|
| 32 | **COPAUA0C** | 1,026 | MQ Trigger | MQ-triggered authorization processor — reads MQ request, queries VSAM, responds via MQ |
| 33 | **COPAUS0C** | 1,032 | Online CICS | Pending authorization summary — displays list of pending auths from IMS DB |
| 34 | **COPAUS1C** | 604 | Online CICS | Pending authorization detail — drill-down into single auth record |
| 35 | **COPAUS2C** | 244 | Online CICS | Fraud marking — marks authorization as fraudulent in DB2 |
| 36 | **CBPAUP0C** | 386 | Batch | Batch purge — removes expired/processed authorization records |
| 37 | **DBUNLDGS** | 366 | IMS Batch | IMS DB unload to GSAM — extracts auth data from IMS segments |
| 38 | **PAUDBLOD** | 369 | IMS Batch | IMS DB load — bulk load authorization records into IMS DB |
| 39 | **PAUDBUNL** | 317 | IMS Batch | IMS DB unload — sequential extraction of auth records |

### 2.2 Transaction Type Module — DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program | LOC | Classification | Description |
|---|---------|-----|----------------|-------------|
| 40 | **COTRTLIC** | 2,098 | Online CICS/DB2 | Transaction type list — browse DB2 TRANTYPE table with cursor |
| 41 | **COTRTUPC** | 1,702 | Online CICS/DB2 | Transaction type maintenance — add/edit transaction types in DB2 |
| 42 | **COBTUPDT** | 237 | Batch/DB2 | Batch transaction type update — bulk DB2 updates |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | LOC | Classification | Description |
|---|---------|-----|----------------|-------------|
| 43 | **CODATE01** | 524 | MQ Service | System date service — responds to MQ requests with current date |
| 44 | **COACCT01** | 620 | MQ Service | Account inquiry service — responds to MQ requests with account data from VSAM |

---

## 3. Copybooks — Data Structures (`app/cpy/`)

### 3.1 Business Entity Copybooks (prefix `CV*`)

| # | Copybook | Record Len | Classification | Description |
|---|----------|------------|----------------|-------------|
| 1 | **CVACT01Y** | 300 | Account | Account master record — balances, limits, dates, zip, group ID |
| 2 | **CVACT02Y** | 150 | Card | Card master record — number, CVV, embossed name, expiry, status |
| 3 | **CVACT03Y** | 50 | Cross-Reference | Card-to-account/customer cross-reference |
| 4 | **CVCUS01Y** | 500 | Customer | Customer master — name, address, phone, SSN, FICO score |
| 5 | **CVCRD01Y** | — | Screen Work Area | Credit card screen work areas — AID keys, navigation, error messages |
| 6 | **CVTRA01Y** | 50 | Transaction Category Balance | Per-account/type/category running balance |
| 7 | **CVTRA02Y** | 50 | Disclosure Group | Interest rate by account group/transaction type/category |
| 8 | **CVTRA03Y** | 60 | Transaction Type | Transaction type code + description |
| 9 | **CVTRA04Y** | 60 | Transaction Category | Transaction category within a type |
| 10 | **CVTRA05Y** | 350 | Transaction | Full transaction record — amount, merchant, timestamps |
| 11 | **CVTRA06Y** | 350 | Daily Transaction | Daily transaction record (same layout as CVTRA05Y) |
| 12 | **CVTRA07Y** | — | Report Layout | Transaction report headers, detail lines, totals |
| 13 | **CVEXPORT** | — | Export Record | Export/import record structure — multi-entity |

### 3.2 UI / Control Copybooks

| # | Copybook | Classification | Description |
|---|----------|----------------|-------------|
| 14 | **COCOM01Y** | Communication Area | CICS COMMAREA — user context, navigation state, customer/account/card IDs |
| 15 | **COMEN02Y** | Menu Definition | Main menu options — maps option numbers to program names (11 options) |
| 16 | **COADM02Y** | Menu Definition | Admin menu options — maps option numbers to program names (6 options) |
| 17 | **COTTL01Y** | Screen Title | Application title and thank-you messages |
| 18 | **CSMSG01Y** | Messages | Common messages — "Thank you", "Invalid key" |
| 19 | **CSMSG02Y** | Messages | Abend work areas — error code, culprit, reason, message |
| 20 | **CSSETATY** | Screen Attribute | Template for setting field colors on error (red/asterisk) |

### 3.3 Utility Copybooks

| # | Copybook | Classification | Description |
|---|----------|----------------|-------------|
| 21 | **CSDAT01Y** | Date/Time | Current date/time work areas and formatted timestamp |
| 22 | **CODATECN** | Date Conversion | Date format conversion (YYYYMMDD ↔ YYYY-MM-DD) |
| 23 | **CSLKPCDY** | Lookup Codes | Large lookup table — likely postal/state codes |
| 24 | **CSSTRPFY** | String Processing | String processing utility procedures |
| 25 | **CSUTLDPY** | Utility Procedures | Common utility PERFORM procedures |
| 26 | **CSUTLDWY** | Utility Work Storage | Working-storage for utility routines |
| 27 | **CSUSR01Y** | Security Record | User security record — ID, name, password, type (80 bytes) |
| 28 | **COSTM01** | Statement Layout | Transaction record re-keyed layout for statement reporting |
| 29 | **CUSTREC** | Customer (alt) | Alternate customer record layout (identical to CVCUS01Y) |
| 30 | **UNUSED1Y** | Unused | Placeholder/legacy unused record structure |

### 3.4 Authorization Module Copybooks (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Classification | Description |
|---|----------|----------------|-------------|
| 31 | **CIPAUDTY** | Auth Detail | Pending authorization detail record — account, card, amount, timestamps |
| 32 | **CIPAUSMY** | Auth Summary | Pending authorization summary record |
| 33 | **CCPAUERY** | Auth Error | Authorization error response structure |
| 34 | **CCPAURLY** | Auth Reply | Authorization reply message structure |
| 35 | **CCPAURQY** | Auth Request | Authorization request message structure |
| 36 | **IMSFUNCS** | IMS Functions | IMS DL/I function code constants (GU, GN, ISRT, DLET) |
| 37 | **PADFLPCB** | IMS PCB | IMS PCB for PADFL database |
| 38 | **PASFLPCB** | IMS PCB | IMS PCB for PASFL database |
| 39 | **PAUTBPCB** | IMS PCB | IMS PCB for PAUTB database |

### 3.5 Transaction Type Module Copybooks (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Classification | Description |
|---|----------|----------------|-------------|
| 40 | **CSDB2RPY** | DB2 Procedures | Common DB2 procedures — priming query, DSNTIAC message formatting |
| 41 | **CSDB2RWY** | DB2 Work Storage | DB2 common working-storage variables — SQLCODE display, error flags |

---

## 4. BMS Maps — 3270 Terminal Screens (`app/bms/`)

| # | Map | Mapset | Associated Program | Screen Description |
|---|-----|--------|-------------------|-------------------|
| 1 | **COSGN00** | COSGN00 | COSGN00C | Sign-on screen — user ID and password entry |
| 2 | **COMEN01** | COMEN01 | COMEN01C | Main menu — 11 function options |
| 3 | **COADM01** | COADM01 | COADM01C | Admin menu — 6 admin function options |
| 4 | **COACTVW** | COACTVW | COACTVWC | Account view — account details display |
| 5 | **COACTUP** | COACTUP | COACTUPC | Account update — editable account fields |
| 6 | **COCRDLI** | COCRDLI | COCRDLIC | Credit card list — paginated card browser |
| 7 | **COCRDSL** | COCRDSL | COCRDSLC | Credit card view — single card detail |
| 8 | **COCRDUP** | COCRDUP | COCRDUPC | Credit card update — editable card fields |
| 9 | **COTRN00** | COTRN00 | COTRN00C | Transaction list — paginated transaction browser |
| 10 | **COTRN01** | COTRN01 | COTRN01C | Transaction view — single transaction detail |
| 11 | **COTRN02** | COTRN02 | COTRN02C | Transaction add — new transaction entry form |
| 12 | **CORPT00** | CORPT00 | CORPT00C | Report request — date range and parameters |
| 13 | **COBIL00** | COBIL00 | COBIL00C | Bill payment — payment entry form |
| 14 | **COUSR00** | COUSR00 | COUSR00C | User list — paginated user browser |
| 15 | **COUSR01** | COUSR01 | COUSR01C | User add — new user entry form |
| 16 | **COUSR02** | COUSR02 | COUSR02C | User update — editable user fields |
| 17 | **COUSR03** | COUSR03 | COUSR03C | User delete — confirmation screen |

### 4.1 BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map produces a corresponding copybook defining symbolic field names:

`COACTUP.CPY`, `COACTVW.CPY`, `COADM01.CPY`, `COBIL00.CPY`, `COCRDLI.CPY`, `COCRDSL.CPY`, `COCRDUP.CPY`, `COMEN01.CPY`, `CORPT00.CPY`, `COSGN00.CPY`, `COTRN00.CPY`, `COTRN01.CPY`, `COTRN02.CPY`, `COUSR00.CPY`, `COUSR01.CPY`, `COUSR02.CPY`, `COUSR03.CPY`

### 4.2 Authorization Module BMS Maps (`app/app-authorization-ims-db2-mq/bms/`)

| # | Map | Associated Program | Screen Description |
|---|-----|-------------------|-------------------|
| 18 | **COPAU00** | COPAUS0C | Pending authorization summary list |
| 19 | **COPAU01** | COPAUS1C | Pending authorization detail view |

### 4.3 Transaction Type Module BMS Maps (`app/app-transaction-type-db2/bms/`)

| # | Map | Associated Program | Screen Description |
|---|-----|-------------------|-------------------|
| 20 | **COTRTLI** | COTRTLIC | Transaction type list (DB2) |
| 21 | **COTRTUP** | COTRTUPC | Transaction type maintenance (DB2) |

---

## 5. JCL Jobs (`app/jcl/`)

### 5.1 Data Refresh / VSAM Load Jobs

| # | JCL | Programs Executed | Classification | Description |
|---|-----|------------------|----------------|-------------|
| 1 | **ACCTFILE** | IDCAMS | VSAM Load | Delete/define/load account master VSAM KSDS from PS |
| 2 | **CARDFILE** | IDCAMS, SDSF | VSAM Load | Delete/define/load card master VSAM KSDS from PS |
| 3 | **CUSTFILE** | IDCAMS, SDSF | VSAM Load | Delete/define/load customer master VSAM KSDS from PS |
| 4 | **XREFFILE** | IDCAMS | VSAM Load | Delete/define/load card cross-reference VSAM KSDS from PS |
| 5 | **TRANFILE** | IDCAMS, SDSF | VSAM Load | Delete/define/load transaction master VSAM KSDS |
| 6 | **DUSRSECJ** | IDCAMS, IEBGENER, IEFBR14 | VSAM Load | Delete/define/load user security VSAM KSDS from PS |
| 7 | **DISCGRP** | IDCAMS | VSAM Load | Delete/define/load disclosure group VSAM KSDS |
| 8 | **TCATBALF** | IDCAMS | VSAM Load | Delete/define/load transaction category balance VSAM |
| 9 | **TRANCATG** | IDCAMS | VSAM Load | Delete/define/load transaction category VSAM |
| 10 | **TRANTYPE** | IDCAMS | VSAM Load | Delete/define/load transaction type VSAM |

### 5.2 Core Batch Processing Jobs

| # | JCL | Programs Executed | Classification | Description |
|---|-----|------------------|----------------|-------------|
| 11 | **CLOSEFIL** | SDSF | CICS Control | Close CICS files before batch processing |
| 12 | **OPENFIL** | SDSF | CICS Control | Reopen CICS files after batch processing |
| 13 | **POSTTRAN** | CBTRN02C | Transaction Posting | Post daily transactions — core batch cycle step |
| 14 | **INTCALC** | CBACT04C | Interest Calc | Calculate interest on accounts |
| 15 | **TRANBKP** | IDCAMS | Backup | Backup transaction VSAM to GDG |
| 16 | **COMBTRAN** | IDCAMS, SORT | Data Merge | Combine daily and backed-up transactions |
| 17 | **CREASTMT** | CBSTM03A, IDCAMS, IEFBR14, SORT | Statement Gen | Generate customer statements (text + HTML) |
| 18 | **TRANREPT** | CBTRN03C, SORT | Reporting | Generate daily transaction report |
| 19 | **TRANIDX** | IDCAMS | Index Mgmt | Define/build alternate indexes for transaction VSAM |

### 5.3 Data Read / Utility Jobs

| # | JCL | Programs Executed | Classification | Description |
|---|-----|------------------|----------------|-------------|
| 20 | **READACCT** | CBACT01C, IEFBR14 | Data Read | Read account VSAM → multiple output formats |
| 21 | **READCARD** | CBACT02C | Data Read | Read card VSAM sequentially |
| 22 | **READCUST** | CBCUS01C | Data Read | Read customer VSAM sequentially |
| 23 | **READXREF** | CBACT03C | Data Read | Read cross-reference VSAM sequentially |
| 24 | **CBEXPORT** | CBEXPORT, IDCAMS | Data Export | Export all VSAM data to sequential file |
| 25 | **CBIMPORT** | CBIMPORT | Data Import | Import sequential file to individual output files |
| 26 | **WAITSTEP** | COBSWAIT | Utility | Wait/delay step for job scheduling |

### 5.4 Infrastructure / Definition Jobs

| # | JCL | Programs Executed | Classification | Description |
|---|-----|------------------|----------------|-------------|
| 27 | **DEFGDGB** | IDCAMS | GDG Define | Define GDG base entries for backup datasets |
| 28 | **DEFGDGD** | IDCAMS, IEBGENER | GDG Define | Define GDG + initial data |
| 29 | **DEFCUST** | IDCAMS | VSAM Define | Define customer VSAM cluster |
| 30 | **ESDSRRDS** | IDCAMS, IEBGENER, IEFBR14 | VSAM Define | Define ESDS/RRDS alternate VSAM organizations |
| 31 | **REPTFILE** | IDCAMS | VSAM Define | Define report file VSAM |
| 32 | **DALYREJS** | IDCAMS | VSAM Define | Define daily rejects VSAM |
| 33 | **PRTCATBL** | IEFBR14, SORT | Reporting | Print/sort transaction category balance report |
| 34 | **CBADMCDJ** | DFHCSDUP | CICS Admin | Load CICS CSD resource definitions |
| 35 | **FTPJCL** | FTP | File Transfer | FTP transfer JCL template |
| 36 | **INTRDRJ1** | IDCAMS, IEBGENER | Internal Reader | Internal reader job — chains INTRDRJ2 |
| 37 | **INTRDRJ2** | IDCAMS | Internal Reader | Chained internal reader cleanup |
| 38 | **TXT2PDF1** | IKJEFT1B | Utility | Convert text statement to PDF |

### 5.5 Authorization Module JCL (`app/app-authorization-ims-db2-mq/jcl/`)

| # | JCL | Classification | Description |
|---|-----|----------------|-------------|
| 39 | **CBPAUP0J** | Batch Purge | Purge expired authorization records |
| 40 | **DBPAUTP0** | DB2 Define | Create authorization DB2 tables |
| 41 | **LOADPADB** | IMS Load | Load authorization IMS database |
| 42 | **UNLDGSAM** | IMS Unload | Unload IMS DB to GSAM |
| 43 | **UNLDPADB** | IMS Unload | Unload authorization IMS database |

### 5.6 Transaction Type Module JCL (`app/app-transaction-type-db2/jcl/`)

| # | JCL | Classification | Description |
|---|-----|----------------|-------------|
| 44 | **CREADB21** | DB2 Define | Create DB2 tables for transaction types |
| 45 | **MNTTRDB2** | Batch DB2 | Batch maintenance of transaction types in DB2 |
| 46 | **TRANEXTR** | Data Extract | Extract transaction type data from DB2 |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Description |
|---|---------|-------------|
| 1 | **COBDATFT** | Date format transformation — converts between date formats; called by CBACT01C |
| 2 | **MVSWAIT** | MVS WAIT SVC — suspends execution for specified interval; called by COBSWAIT |

---

## 7. JCL Procedures (`app/proc/`)

| # | Procedure | Used By | Description |
|---|-----------|---------|-------------|
| 1 | **REPROC** | PRTCATBL, TRANBKP | Reusable procedure for report/backup processing |
| 2 | **TRANREPT** | TRANREPT | Transaction report procedure — SORT + CBTRN03C |

---

## 8. Classification Summary

### By Business Domain

| Domain | Programs | Copybooks | BMS Maps | JCL Jobs |
|--------|----------|-----------|----------|----------|
| **Account Management** | COACTVWC, COACTUPC, CBACT01C, CBACT04C | CVACT01Y | COACTVW, COACTUP | ACCTFILE, READACCT, INTCALC |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C | CVACT02Y, CVACT03Y, CVCRD01Y | COCRDLI, COCRDSL, COCRDUP | CARDFILE, READCARD, XREFFILE, READXREF |
| **Customer Management** | CBCUS01C | CVCUS01Y, CUSTREC | — | CUSTFILE, READCUST, DEFCUST |
| **Transactions** | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C | CVTRA01Y–07Y, COSTM01 | COTRN00, COTRN01, COTRN02 | POSTTRAN, COMBTRAN, TRANREPT, TRANBKP |
| **Bill Payment** | COBIL00C | — | COBIL00 | — |
| **Statements** | CBSTM03A, CBSTM03B | COSTM01 | — | CREASTMT, TXT2PDF1 |
| **User Security** | COSGN00C, COUSR00C–03C | CSUSR01Y | COSGN00, COUSR00–03 | DUSRSECJ |
| **Authorization (Optional)** | COPAUA0C, COPAUS0C–2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | CIPAUDTY, CIPAUSMY, CCPAU* | COPAU00, COPAU01 | CBPAUP0J, DBPAUTP0, LOADPADB, UNLD* |
| **Tran Type Admin (Optional)** | COTRTLIC, COTRTUPC, COBTUPDT | CSDB2RPY, CSDB2RWY | COTRTLI, COTRTUP | CREADB21, MNTTRDB2, TRANEXTR |
| **MQ Services (Optional)** | CODATE01, COACCT01 | — | — | — |

### By Technology Platform

| Platform | Component Count | Key Programs |
|----------|----------------|--------------|
| CICS/VSAM (Online) | 18 programs | CO* prefix |
| Batch/VSAM | 13 programs | CB* prefix |
| IMS DB | 3 programs | DBUNLDGS, PAUDBLOD, PAUDBUNL |
| DB2 | 4 programs | COTRTLIC, COTRTUPC, COBTUPDT, COPAUS2C |
| MQ Series | 3 programs | COPAUA0C, CODATE01, COACCT01 |
| Assembler | 2 programs | COBDATFT, MVSWAIT |

---

## 9. Batch Cycle Execution Order

The standard nightly batch cycle runs in the following sequence:

```
1. CLOSEFIL    — Close CICS files for exclusive batch access
2. ACCTFILE    — Refresh account master VSAM
3. CARDFILE    — Refresh card master VSAM
4. CUSTFILE    — Refresh customer master VSAM
5. XREFFILE    — Refresh cross-reference VSAM
6. TRANFILE    — Refresh transaction master VSAM
7. POSTTRAN    — Post daily transactions (CBTRN02C)
8. INTCALC     — Calculate interest (CBACT04C)
9. TRANBKP     — Backup transaction VSAM to GDG
10. COMBTRAN   — Combine daily + backup transactions
11. CREASTMT   — Generate customer statements (CBSTM03A)
12. TRANREPT   — Generate transaction report (CBTRN03C)
13. TRANIDX    — Rebuild alternate indexes
14. OPENFIL    — Reopen CICS files for online access
```
