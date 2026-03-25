# CardDemo Application Inventory

> Comprehensive catalog of all programs, copybooks, JCL jobs, and BMS maps in the CardDemo COBOL mainframe application.

---

## Summary

| Asset Type | Count | Location |
|-----------|-------|----------|
| COBOL Programs (Online CICS) | 17 | `app/cbl/` |
| COBOL Programs (Batch) | 14 | `app/cbl/` |
| Copybooks | 30 | `app/cpy/` |
| BMS Screen Maps | 17 | `app/bms/` |
| BMS-Generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Batch Jobs | 38 | `app/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| Optional Module Programs | 13 | `app/app-*/` |

---

## 1. COBOL Programs — Online CICS (`app/cbl/`)

These programs run under CICS and handle real-time user interactions via 3270 terminal screens.

| # | Program | Lines | Function | Classification | CICS Transaction |
|---|---------|-------|----------|----------------|-----------------|
| 1 | COSGN00C | 260 | **Sign-On** — Authenticates users, reads USRSEC VSAM, routes Admin vs. Regular users | Core | CC00 |
| 2 | COMEN01C | 308 | **Main Menu** — Navigation hub for Regular users; dispatches to sub-programs via XCTL | Core | CM00 |
| 3 | COADM01C | 288 | **Admin Menu** — Navigation hub for Admin users; links to user management functions | Core | CA00 |
| 4 | COACTVWC | 941 | **Account View** — Display account details with customer and card cross-reference lookups | Core | CA01 |
| 5 | COACTUPC | 4,236 | **Account Update** — Full account edit with field-level validation, VSAM REWRITE, attribute control | Core | CA02 |
| 6 | COCRDLIC | 1,459 | **Card List** — Browse credit cards; admin sees all, regular users see only their account's cards | Core | CC01 |
| 7 | COCRDSLC | 887 | **Card Detail View** — Display full card details (number, name, expiry, status) read-only | Core | CC02 |
| 8 | COCRDUPC | 1,560 | **Card Update** — Edit card details with validation and VSAM REWRITE | Core | CC03 |
| 9 | COTRN00C | 699 | **Transaction List** — Browse transactions with forward/backward paging from TRANSACT VSAM | Core | CT00 |
| 10 | COTRN01C | 330 | **Transaction View** — Display single transaction detail (read-only) | Core | CT01 |
| 11 | COTRN02C | 783 | **Transaction Add** — Create new transaction with card/account validation, auto-generate ID | Core | CT02 |
| 12 | CORPT00C | 649 | **Transaction Report** — Submit batch report job via CICS extra-partition TDQ (internal reader) | Core | CR00 |
| 13 | COBIL00C | 572 | **Bill Payment** — Pay account balance in full; creates payment transaction, updates account balance | Core | CB00 |
| 14 | COUSR00C | 695 | **User List** — Browse all users from USRSEC file with forward/backward paging (Admin only) | Core | CU00 |
| 15 | COUSR01C | 299 | **User Add** — Create new Regular or Admin user in USRSEC VSAM (Admin only) | Core | CU01 |
| 16 | COUSR02C | 414 | **User Update** — Modify existing user record (Admin only) | Core | CU02 |
| 17 | COUSR03C | 359 | **User Delete** — Remove user from USRSEC VSAM with confirmation (Admin only) | Core | CU03 |

## 2. COBOL Programs — Batch (`app/cbl/`)

These programs run as batch jobs submitted via JCL, processing files without user interaction.

| # | Program | Lines | Function | Classification |
|---|---------|-------|----------|----------------|
| 1 | CBACT01C | 430 | **Account File Reader** — Read ACCTFILE VSAM and produce printed report; calls COBDATFT for date formatting | Utility |
| 2 | CBACT02C | 178 | **Card File Reader** — Read and print CARDFILE VSAM contents | Utility |
| 3 | CBACT03C | 178 | **Cross-Reference Reader** — Read and print XREFFILE VSAM contents | Utility |
| 4 | CBACT04C | 652 | **Interest Calculator** — Calculate interest on accounts using transaction category balances and disclosure group rates | Core |
| 5 | CBCUS01C | 178 | **Customer File Reader** — Read and print CUSTFILE VSAM contents | Utility |
| 6 | CBTRN01C | 494 | **Transaction Poster (v1)** — Post daily transactions to master transaction file; update account balances and cross-references | Core |
| 7 | CBTRN02C | 731 | **Transaction Poster (v2)** — Enhanced transaction posting with category balance tracking | Core |
| 8 | CBTRN03C | 649 | **Transaction Reporter** — Generate detailed transaction report with type/category descriptions, page/account/grand totals | Core |
| 9 | CBSTM03A | 924 | **Statement Generator** — Produce account statements in plain text and HTML formats; calls CBSTM03B for file I/O | Core |
| 10 | CBSTM03B | 230 | **Statement File Handler** — Subroutine called by CBSTM03A for statement file write operations | Core |
| 11 | CBEXPORT | 582 | **Data Export** — Export customer data for branch migration; reads normalized files, creates multi-record export | Utility |
| 12 | CBIMPORT | 487 | **Data Import** — Import customer data from export file; splits into normalized target files with validation | Utility |
| 13 | COBSWAIT | 41 | **Wait Utility** — Pause execution for specified centiseconds; calls assembler MVSWAIT | Utility |
| 14 | CSUTLDTC | 157 | **Date Utility** — Date conversion using LE CEEDAYS API; validates and converts date formats | Utility |

## 3. Copybooks (`app/cpy/`)

Copybooks define shared data structures included by COBOL programs via COPY statements.

### Business Data Structures

| # | Copybook | Lines | Record Length | Purpose | Classification |
|---|----------|-------|--------------|---------|----------------|
| 1 | CVACT01Y | 20 | 300 bytes | **Account Master Record** — Account ID, status, balances (current, credit limit, cash advance), open/expiry dates | Core |
| 2 | CVACT02Y | 14 | 150 bytes | **Card Data Record** — Card number, account ID, CVV, embossed name, expiration, active status | Core |
| 3 | CVACT03Y | 10 | 50 bytes | **Card Cross-Reference** — Maps card numbers to account IDs | Core |
| 4 | CVCUS01Y | 28 | 500 bytes | **Customer Master Record** — Personal info (name, DOB, SSN), address, phone, FICO score, dates | Core |
| 5 | CVTRA05Y | 21 | 350 bytes | **Transaction Record** — Transaction ID, type, amount, merchant info, card number, timestamps | Core |
| 6 | CVTRA06Y | 21 | 350 bytes | **Daily Transaction Record** — Same layout as CVTRA05Y for daily batch input | Core |
| 7 | CVTRA01Y | 13 | 50 bytes | **Transaction Category Balance** — Account-level balance by transaction type and category | Core |
| 8 | CVTRA02Y | 13 | 50 bytes | **Disclosure Group** — Interest rate by account group, transaction type, and category | Core |
| 9 | CVTRA03Y | 10 | 60 bytes | **Transaction Type** — Type code and description | Core |
| 10 | CVTRA04Y | 12 | 60 bytes | **Transaction Category** — Type and category code with description | Core |
| 11 | CSUSR01Y | 10 | 80 bytes | **User Security Record** — User ID, password, first/last name, user type (Admin/Regular) | Core |
| 12 | COSTM01 | 38 | 350 bytes | **Statement Transaction Layout** — Altered transaction record keyed by card+transaction ID for reporting | Core |
| 13 | CUSTREC | varies | varies | **Customer Record (alt)** — Alternate customer record layout for statement generation | Core |
| 14 | CVEXPORT | varies | varies | **Export Record Layout** — Multi-record export format with header/customer/account/card/transaction record types | Utility |

### Application Infrastructure Copybooks

| # | Copybook | Lines | Purpose | Classification |
|---|----------|-------|---------|----------------|
| 15 | COCOM01Y | varies | **Common Communication Area** — Shared COMMAREA structure for inter-program data passing via CICS | Infrastructure |
| 16 | COMEN02Y | varies | **Menu Definitions** — Menu option structures for main menu navigation | Infrastructure |
| 17 | COADM02Y | varies | **Admin Menu Definitions** — Menu option structures for admin menu navigation | Infrastructure |
| 18 | COTTL01Y | varies | **Title/Header** — Screen title and header line definitions | Infrastructure |
| 19 | CSDAT01Y | varies | **Date Display** — Date formatting structures for screen display | Infrastructure |
| 20 | CSMSG01Y | varies | **Message Area (1)** — User message display structures | Infrastructure |
| 21 | CSMSG02Y | varies | **Message Area (2)** — Extended message display structures | Infrastructure |
| 22 | CVCRD01Y | varies | **Card Constants** — Card-related constants and literals | Infrastructure |
| 23 | CSLKPCDY | varies | **Lookup Codes** — Code lookup and validation structures | Infrastructure |
| 24 | CSSETATY | varies | **Set Attribute** — BMS field attribute setting (used with COPY REPLACING) | Infrastructure |
| 25 | CSSTRPFY | varies | **String Parse Function** — String parsing utility copybook | Infrastructure |
| 26 | CSUTLDPY | varies | **Utility Display** — Utility display formatting | Infrastructure |
| 27 | CSUTLDWY | varies | **Utility Working Storage** — Working storage for utility functions | Infrastructure |
| 28 | CODATECN | varies | **Date Conversion** — Date conversion record for assembler COBDATFT call | Infrastructure |
| 29 | CVTRA07Y | 73 | **Transaction Report Layout** — Report headers, detail lines, totals for daily transaction report | Infrastructure |
| 30 | UNUSED1Y | 10 | **Unused** — Placeholder/legacy unused data structure | Deprecated |

## 4. BMS Screen Maps (`app/bms/`)

BMS (Basic Mapping Support) maps define 3270 terminal screen layouts for CICS programs.

| # | Map | Lines | Screen Title | Associated Program | Classification |
|---|-----|-------|-------------|-------------------|----------------|
| 1 | COSGN00 | 210 | Login Screen | COSGN00C | Core |
| 2 | COMEN01 | 167 | Main Menu Screen | COMEN01C | Core |
| 3 | COADM01 | 167 | Admin Menu Screen | COADM01C | Core |
| 4 | COACTVW | 378 | Account Viewer Screen | COACTVWC | Core |
| 5 | COACTUP | 512 | Account Update Screen | COACTUPC | Core |
| 6 | COCRDLI | 344 | Card Listing Screen | COCRDLIC | Core |
| 7 | COCRDSL | 157 | Card Selection/Detail Screen | COCRDSLC | Core |
| 8 | COCRDUP | 172 | Card Update Screen | COCRDUPC | Core |
| 9 | COTRN00 | 464 | Transaction List Screen | COTRN00C | Core |
| 10 | COTRN01 | 273 | Transaction View Screen | COTRN01C | Core |
| 11 | COTRN02 | 307 | Transaction Add Screen | COTRN02C | Core |
| 12 | CORPT00 | 231 | Transaction Report Screen | CORPT00C | Core |
| 13 | COBIL00 | 141 | Bill Payment Screen | COBIL00C | Core |
| 14 | COUSR00 | 463 | List Users Screen | COUSR00C | Core |
| 15 | COUSR01 | 164 | Add User Screen | COUSR01C | Core |
| 16 | COUSR02 | 169 | Update User Screen | COUSR02C | Core |
| 17 | COUSR03 | 153 | Delete User Screen | COUSR03C | Core |

## 5. JCL Batch Jobs (`app/jcl/`)

### Data File Management Jobs

| # | Job | Purpose | Programs Executed | Classification |
|---|-----|---------|-------------------|----------------|
| 1 | ACCTFILE | Refresh account master VSAM — delete, define, load from sequential data | IDCAMS, SORT | Core |
| 2 | CARDFILE | Refresh card master VSAM — delete, define, load from sequential data | IDCAMS | Core |
| 3 | CUSTFILE | Refresh customer master VSAM — delete, define, load | IDCAMS | Core |
| 4 | XREFFILE | Refresh card cross-reference VSAM — delete, define, load, build alternate index | IDCAMS | Core |
| 5 | TRANFILE | Refresh transaction master VSAM — delete, define, load | IDCAMS | Core |
| 6 | DUSRSECJ | Load user security VSAM — delete, define, load user credentials | IDCAMS | Core |
| 7 | TRANTYPE | Define transaction type VSAM — delete, define, load type codes | IDCAMS | Core |
| 8 | DEFCUST | Define customer VSAM cluster | IDCAMS | Setup |
| 9 | TRANCATG | Define transaction category VSAM | IDCAMS | Setup |

### Batch Processing Jobs

| # | Job | Purpose | Programs Executed | Classification |
|---|-----|---------|-------------------|----------------|
| 10 | POSTTRAN | Post daily transactions to master file | CBTRN01C, CBTRN02C | Core |
| 11 | INTCALC | Calculate interest on account balances | CBACT04C | Core |
| 12 | COMBTRAN | Combine daily transactions into master | SORT, IDCAMS | Core |
| 13 | CREASTMT | Generate account statements (text + HTML) | SORT, IDCAMS, IEFBR14, CBSTM03A | Core |
| 14 | TRANREPT | Generate daily transaction detail report | SORT, CBTRN03C | Core |
| 15 | TRANBKP | Backup transaction file | IDCAMS (REPRO) | Core |
| 16 | TRANIDX | Define alternate index on transaction file | IDCAMS | Core |

### CICS File Control Jobs

| # | Job | Purpose | Programs Executed | Classification |
|---|-----|---------|-------------------|----------------|
| 17 | CLOSEFIL | Close CICS files for batch processing | DFHCSDUP | Infrastructure |
| 18 | OPENFIL | Re-open CICS files after batch | DFHCSDUP | Infrastructure |

### Utility / Infrastructure Jobs

| # | Job | Purpose | Programs Executed | Classification |
|---|-----|---------|-------------------|----------------|
| 19 | CBADMCDJ | Admin card operations batch job | Various | Utility |
| 20 | CBEXPORT | Run data export for branch migration | CBEXPORT | Utility |
| 21 | CBIMPORT | Run data import from migration export | CBIMPORT | Utility |
| 22 | DALYREJS | Process daily rejected transactions | SORT, IDCAMS | Utility |
| 23 | DISCGRP | Define disclosure group VSAM | IDCAMS | Setup |
| 24 | ESDSRRDS | Define ESDS and RRDS VSAM files | IDCAMS | Setup |
| 25 | DEFGDGB | Define GDG (Generation Data Group) base entries | IDCAMS | Setup |
| 26 | DEFGDGD | Delete GDG base definitions | IDCAMS | Setup |
| 27 | PRTCATBL | Print transaction category balance file | IDCAMS | Utility |
| 28 | READACCT | Read and display account file | IDCAMS | Utility |
| 29 | READCARD | Read and display card file | IDCAMS | Utility |
| 30 | READCUST | Read and display customer file | IDCAMS | Utility |
| 31 | READXREF | Read and display cross-reference file | IDCAMS | Utility |
| 32 | REPTFILE | Define report output file | IDCAMS | Setup |
| 33 | TCATBALF | Define transaction category balance VSAM | IDCAMS | Setup |
| 34 | WAITSTEP | Pause execution for specified duration | COBSWAIT | Utility |
| 35 | FTPJCL | FTP file transfer to/from mainframe | FTP | Utility |
| 36 | INTRDRJ1 | Internal reader trigger — submits INTRDRJ2 | IDCAMS, IEBGENER | Infrastructure |
| 37 | INTRDRJ2 | Internal reader target — copies FTP test backup | IDCAMS | Infrastructure |
| 38 | TXT2PDF1 | Convert text statement output to PDF | IKJEFT1B (TXT2PDF REXX) | Utility |

## 6. Assembler Programs (`app/asm/`)

| # | Program | Lines | Function | Called By |
|---|---------|-------|----------|----------|
| 1 | COBDATFT | 84 | Date formatting — converts dates between COBOL and display formats | CBACT01C |
| 2 | MVSWAIT | 30 | Wait/sleep — pauses execution for specified centiseconds via SVC | COBSWAIT |

## 7. Optional Modules

### Authorization Module (`app/app-authorization-ims-db2-mq/`)
IMS DB + DB2 + MQ Series integration for transaction authorization.

| Program | Function |
|---------|----------|
| COPAUA0C | MQ trigger monitor for authorization requests |
| COPAUS0C | Authorization summary display |
| COPAUS1C | Authorization details display |
| COPAUS2C | Fraud marking — writes fraud flags to DB2 |
| CBPAUP0C | Batch purge of old authorization records |

### Transaction Type DB2 Module (`app/app-transaction-type-db2/`)
DB2-backed transaction type management replacing VSAM.

| Program | Function |
|---------|----------|
| COTRTUPC | Online add/edit transaction types via DB2 cursors |
| COTRTLIC | Online list/delete transaction types from DB2 |
| COBTUPDT | Batch update transaction types in DB2 |

### VSAM-MQ Module (`app/app-vsam-mq/`)
MQ request/response for inquiries.

| Program | Transaction | Function |
|---------|------------|----------|
| CODATE01 | CDRD | System date inquiry via MQ |
| COACCT01 | CDRA | Account inquiry via MQ |

---

## Naming Conventions

| Prefix | Meaning |
|--------|---------|
| `CO*` | Online CICS program |
| `CB*` | Batch COBOL program |
| `CV*` | Copybook — VSAM data structure |
| `CS*` | Copybook — System/shared utility |
| `CO*` (cpy) | Copybook — Communication/common area |

## Batch Cycle Order

The standard nightly batch cycle runs in this sequence:

```
CLOSEFIL → ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE/DUSRSECJ
         → POSTTRAN → INTCALC → TRANBKP → COMBTRAN
         → CREASTMT → TRANIDX → OPENFIL
```
