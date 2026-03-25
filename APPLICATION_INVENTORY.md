# CardDemo Application Inventory

> **Generated from**: Static analysis of the CardDemo COBOL/CICS/VSAM codebase
>
> **Scope**: All programs, copybooks, BMS maps, JCL jobs, assembler modules, and optional extension modules

---

## Summary

| Artifact Type | Core Count | Optional-Module Count | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | 44 |
| Copybooks (data) | 30 | 9 | 39 |
| BMS Screen Maps | 17 | 4 | 21 |
| BMS-generated Copybooks | 17 | 4 | 21 |
| JCL Jobs | 38 | 8 | 46 |
| Assembler Programs | 2 | 0 | 2 |
| JCL Procedures | 2 | 0 | 2 |

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

These programs run under CICS and implement the interactive 3270 terminal application.

| Program | Lines | CICS Txn | Screen | Description | Business Domain |
|---|---|---|---|---|---|
| `COSGN00C.cbl` | 260 | CC00 | COSGN00 | **Sign-on / Authentication** - Validates user credentials against USRSEC VSAM file; routes admins to COADM01C, regular users to COMEN01C | Security |
| `COMEN01C.cbl` | 308 | CM00 | COMEN01 | **Main Menu** - Presents 11 menu options to regular users; dispatches via XCTL to selected program | Navigation |
| `COADM01C.cbl` | 288 | CA00 | COADM01 | **Admin Menu** - Presents 6 admin options (user CRUD + DB2 transaction type mgmt); dispatches via XCTL | Navigation / Admin |
| `COACTVWC.cbl` | 941 | CA01 | COACTVW | **Account View** - Read-only display of account, customer, and card details from ACCTDAT, CUSTDAT, CARDDAT VSAM files | Account Mgmt |
| `COACTUPC.cbl` | 4,236 | CA02 | COACTUP | **Account Update** - Full update of account master, customer data, and card cross-reference with field-level validation | Account Mgmt |
| `COCRDLIC.cbl` | 1,459 | CC01 | COCRDLI | **Credit Card List** - Browse/page through cards in CARDDAT VSAM file with forward/backward scrolling | Card Mgmt |
| `COCRDSLC.cbl` | 887 | CC02 | COCRDSL | **Credit Card View** - Display individual card details from CARDDAT | Card Mgmt |
| `COCRDUPC.cbl` | 1,560 | CC03 | COCRDUP | **Credit Card Update** - Modify card attributes (status, expiration, embossed name) and rewrite to CARDDAT | Card Mgmt |
| `COTRN00C.cbl` | 699 | CT00 | COTRN00 | **Transaction List** - Browse posted transactions in TRANSACT VSAM with scrollable list | Transaction Mgmt |
| `COTRN01C.cbl` | 330 | CT01 | COTRN01 | **Transaction View** - Display individual transaction details from TRANSACT | Transaction Mgmt |
| `COTRN02C.cbl` | 783 | CT02 | COTRN02 | **Transaction Add** - Add new transactions; validates against ACCTDAT and CCXREF/CXACAIX; writes to TRANSACT | Transaction Mgmt |
| `CORPT00C.cbl` | 649 | CR00 | CORPT00 | **Transaction Reports** - Prompts for date range/account criteria; writes report requests to TDQ for batch processing | Reporting |
| `COBIL00C.cbl` | 572 | CB00 | COBIL00 | **Bill Payment** - Processes bill payments; reads/rewrites TRANSACT and ACCTDAT; creates payment transactions | Billing |
| `COUSR00C.cbl` | 695 | CU00 | COUSR00 | **User List (Admin)** - Browse user security records in USRSEC VSAM with pagination | User Admin |
| `COUSR01C.cbl` | 299 | CU01 | COUSR01 | **User Add (Admin)** - Create new user security records in USRSEC | User Admin |
| `COUSR02C.cbl` | 414 | CU02 | COUSR02 | **User Update (Admin)** - Modify existing user security records (password, type) | User Admin |
| `COUSR03C.cbl` | 359 | CU03 | COUSR03 | **User Delete (Admin)** - Delete user security records from USRSEC | User Admin |

### 1.2 Batch Programs (prefix `CB*`)

These programs run as batch jobs invoked by JCL.

| Program | Lines | Description | Business Domain |
|---|---|---|---|
| `CBACT01C.cbl` | 430 | **Account File Reader** - Reads ACCTDAT VSAM and writes to sequential output files (flat, array, variable-length) | Account Mgmt |
| `CBACT02C.cbl` | 178 | **Card File Reader** - Reads CARDDAT VSAM sequentially and displays records | Card Mgmt |
| `CBACT03C.cbl` | 178 | **Cross-Reference Reader** - Reads CARDXREF VSAM sequentially | Card Mgmt |
| `CBACT04C.cbl` | 652 | **Interest Calculation** - Computes interest on account balances using TCATBALF rates and DISCGRP disclosure groups; rewrites ACCTDAT; creates interest transactions in TRANSACT | Finance |
| `CBCUS01C.cbl` | 178 | **Customer File Reader** - Reads CUSTDAT VSAM sequentially and displays records | Customer Mgmt |
| `CBTRN01C.cbl` | 494 | **Daily Transaction Validation** - Reads DALYTRAN file, validates against XREF/ACCT/CUST/CARD, prepares for posting | Transaction Mgmt |
| `CBTRN02C.cbl` | 731 | **Transaction Posting** - Core batch posting engine: reads daily transactions, validates cross-references, posts to TRANSACT, updates ACCTDAT balances and TCATBALF category balances, writes rejects to DALYREJS | Transaction Mgmt |
| `CBTRN03C.cbl` | 649 | **Transaction Report Generator** - Reads sorted transactions, joins with CARDXREF/TRANTYPE/TRANCATG, produces formatted daily transaction report | Reporting |
| `CBSTM03A.CBL` | 924 | **Statement Generator (Driver)** - Orchestrates statement creation: reads TRNXFILE sorted transactions, joins XREFFILE/CUSTFILE/ACCTFILE, produces text and HTML statements | Reporting |
| `CBSTM03B.CBL` | 230 | **Statement Generator (File I/O)** - Subroutine called by CBSTM03A to handle OPEN/READ/CLOSE of TRNX, XREF, CUST, ACCT files | Reporting |
| `CBEXPORT.cbl` | 582 | **Data Export** - Reads all 5 master VSAM files (CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT) and writes tagged records to a single export file | Data Migration |
| `CBIMPORT.cbl` | 487 | **Data Import** - Reads export file and distributes records back to 5 individual sequential output files with error handling | Data Migration |
| `CSUTLDTC.cbl` | 157 | **Date Utility** - Calls LE CEEDAYS/CEEDATM to convert dates between formats | Utility |
| `COBSWAIT.cbl` | 41 | **Wait Utility** - Calls assembler MVSWAIT to pause job execution for inter-step timing | Utility |

### 1.3 Optional Module Programs

#### Authorization Module (`app/app-authorization-ims-db2-mq/cbl/`)

| Program | Lines | Description | Technology |
|---|---|---|---|
| `COPAUA0C.cbl` | -- | MQ trigger monitor for pending authorization requests | CICS + MQ |
| `COPAUS0C.cbl` | -- | Authorization summary display - lists pending authorizations | CICS + IMS DB |
| `COPAUS1C.cbl` | -- | Authorization detail display - shows individual authorization | CICS + IMS DB |
| `COPAUS2C.cbl` | -- | Fraud marking - flags transactions as fraudulent in DB2 | CICS + DB2 |
| `CBPAUP0C.cbl` | -- | Batch purge of processed authorization records | Batch + DB2 |
| `PAUDBLOD.CBL` | -- | IMS database load utility for authorization segments | Batch + IMS DB |
| `PAUDBUNL.CBL` | -- | IMS database unload utility | Batch + IMS DB |
| `DBUNLDGS.CBL` | -- | GSAM unload for IMS database | Batch + IMS DB |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| Program | Lines | Description | Technology |
|---|---|---|---|
| `COTRTLIC.cbl` | -- | Transaction type list/delete - DB2 cursor-based browsing with delete | CICS + DB2 |
| `COTRTUPC.cbl` | -- | Transaction type add/edit - DB2 INSERT/UPDATE with validation | CICS + DB2 |
| `COBTUPDT.cbl` | -- | Batch transaction type update from sequential input | Batch + DB2 |

#### VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| Program | Lines | Description | Technology |
|---|---|---|---|
| `CODATE01.cbl` | -- | MQ request/response for system date inquiry (queue CDRD) | CICS + MQ |
| `COACCT01.cbl` | -- | MQ request/response for account inquiry (queue CDRA) | CICS + MQ |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data-Structure Copybooks (prefix `CV*` - VSAM record layouts)

| Copybook | Record Length | Description | Business Entity |
|---|---|---|---|
| `CVACT01Y.cpy` | 300 bytes | Account master record layout | Account |
| `CVACT02Y.cpy` | 150 bytes | Card master record layout | Card |
| `CVACT03Y.cpy` | 50 bytes | Card-to-account cross-reference | Card Cross-Reference |
| `CVCUS01Y.cpy` | 500 bytes | Customer master record layout | Customer |
| `CVCRD01Y.cpy` | -- | Card detail navigation structure (CCARD-NEXT-PROG) | Card (UI context) |
| `CVTRA01Y.cpy` | 50 bytes | Transaction category balance record | Transaction Category Balance |
| `CVTRA02Y.cpy` | 50 bytes | Disclosure group record (interest rates) | Disclosure Group |
| `CVTRA03Y.cpy` | 60 bytes | Transaction type reference | Transaction Type |
| `CVTRA04Y.cpy` | 60 bytes | Transaction category type reference | Transaction Category |
| `CVTRA05Y.cpy` | 350 bytes | Transaction record (online/master) | Transaction |
| `CVTRA06Y.cpy` | 350 bytes | Daily transaction record (batch input) | Daily Transaction |
| `CVTRA07Y.cpy` | -- | Report header/detail structures for transaction report | Transaction Report |
| `CVEXPORT.cpy` | -- | Export/import tagged record structure | Data Migration |

### 2.2 Common/UI Copybooks (prefix `CO*`, `CS*`, `CU*`)

| Copybook | Description | Usage |
|---|---|---|
| `COCOM01Y.cpy` | CARDDEMO-COMMAREA - communication area passed between programs via CICS COMMAREA | Inter-program communication |
| `COMEN02Y.cpy` | Main menu option definitions (11 options with program names) | Menu navigation |
| `COADM02Y.cpy` | Admin menu option definitions (6 options with program names) | Admin navigation |
| `COTTL01Y.cpy` | Screen title/header area layout | UI headers |
| `CSDAT01Y.cpy` | Date formatting working storage | Date handling |
| `CSMSG01Y.cpy` | Message area for screen output (short messages) | UI messaging |
| `CSMSG02Y.cpy` | Extended message area for screen output | UI messaging |
| `CSUSR01Y.cpy` | User security record layout (80 bytes) | Security |
| `CSSETATY.cpy` | Set-attribute utility (COPY REPLACING for BMS attribute bytes) | UI field attributes |
| `CSSTRPFY.cpy` | String-strip-field utility paragraph | String manipulation |
| `CSUTLDPY.cpy` | Date utility parameter layout for CSUTLDTC calls | Date utility |
| `CSUTLDWY.cpy` | Date utility working-storage definitions | Date utility |
| `CSLKPCDY.cpy` | Lookup code table definitions | Reference data |
| `CODATECN.cpy` | Date conversion constants and formats | Date handling |
| `COSTM01.CPY` | Statement transaction record layout (keyed by card+transaction ID) | Statement |
| `CUSTREC.cpy` | Alternate customer record layout used in statement generation | Customer (Statement) |
| `UNUSED1Y.cpy` | Unused/placeholder data structure | Deprecated |

### 2.3 Optional Module Copybooks

#### Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| Copybook | Description |
|---|---|
| `CCPAUERY.cpy` | Authorization error response layout |
| `CCPAURLY.cpy` | Authorization response list layout |
| `CCPAURQY.cpy` | Authorization request layout |
| `CIPAUDTY.cpy` | Authorization detail IMS segment |
| `CIPAUSMY.cpy` | Authorization summary IMS segment |
| `IMSFUNCS.cpy` | IMS function code constants |
| `PADFLPCB.CPY` | IMS PSB - PCB for authorization flat file |
| `PASFLPCB.CPY` | IMS PSB - PCB for authorization summary |
| `PAUTBPCB.CPY` | IMS PSB - PCB for authorization table |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| Copybook | Description |
|---|---|
| `CSDB2RPY.cpy` | DB2 response/return code area |
| `CSDB2RWY.cpy` | DB2 read/write working-storage area |

---

## 3. BMS Screen Maps (`app/bms/`)

| Map | Fields | Screen Name | Associated Program | Description |
|---|---|---|---|---|
| `COSGN00.bms` | 37 | Sign-on | COSGN00C | Login screen with User ID and Password fields |
| `COMEN01.bms` | 28 | Main Menu | COMEN01C | 11-option main menu for regular users |
| `COADM01.bms` | 28 | Admin Menu | COADM01C | 6-option admin menu |
| `COACTVW.bms` | 100 | Account View | COACTVWC | Read-only account/customer/card display |
| `COACTUP.bms` | 128 | Account Update | COACTUPC | Editable account/customer fields with validation |
| `COCRDLI.bms` | 72 | Card List | COCRDLIC | Scrollable card listing with 10 rows |
| `COCRDSL.bms` | 31 | Card View | COCRDSLC | Individual card detail display |
| `COCRDUP.bms` | 34 | Card Update | COCRDUPC | Editable card fields |
| `COTRN00.bms` | 89 | Transaction List | COTRN00C | Scrollable transaction listing |
| `COTRN01.bms` | 56 | Transaction View | COTRN01C | Individual transaction detail |
| `COTRN02.bms` | 61 | Transaction Add | COTRN02C | New transaction entry form |
| `CORPT00.bms` | 42 | Reports | CORPT00C | Report criteria input (date range, account) |
| `COBIL00.bms` | 24 | Bill Payment | COBIL00C | Bill payment entry form |
| `COUSR00.bms` | 89 | User List | COUSR00C | Scrollable user security listing |
| `COUSR01.bms` | 28 | User Add | COUSR01C | New user entry form |
| `COUSR02.bms` | 29 | User Update | COUSR02C | User modification form |
| `COUSR03.bms` | 26 | User Delete | COUSR03C | User deletion confirmation |

### BMS-Generated Copybooks (`app/cpy-bms/`)

Each BMS map produces a corresponding symbolic map copybook (17 total) used in COBOL programs for SEND MAP / RECEIVE MAP operations.

---

## 4. JCL Jobs (`app/jcl/`)

### 4.1 Data Refresh Jobs (VSAM file load/reload)

| Job | Description | Target VSAM Dataset |
|---|---|---|
| `ACCTFILE.jcl` | Refresh account master VSAM from sequential PS | `ACCTDATA.VSAM.KSDS` |
| `CARDFILE.jcl` | Refresh card master VSAM from sequential PS | `CARDDATA.VSAM.KSDS` |
| `CUSTFILE.jcl` | Refresh customer master VSAM from sequential PS | `CUSTDATA.VSAM.KSDS` |
| `XREFFILE.jcl` | Load card cross-reference VSAM; define AIX and PATH | `CARDXREF.VSAM.KSDS` |
| `TRANFILE.jcl` | Load transaction master VSAM from daily PS; define AIX and PATH | `TRANSACT.VSAM.KSDS` |
| `DUSRSECJ.jcl` | Load user security VSAM from sequential PS | `USRSEC.VSAM.KSDS` |
| `TCATBALF.jcl` | Load transaction category balance VSAM from PS | `TCATBALF.VSAM.KSDS` |
| `TRANCATG.jcl` | Load transaction category type VSAM from PS | `TRANCATG.VSAM.KSDS` |
| `TRANTYPE.jcl` | Load transaction type reference VSAM from PS | `TRANTYPE.VSAM.KSDS` |
| `DISCGRP.jcl` | Load disclosure group VSAM from PS | `DISCGRP.VSAM.KSDS` |
| `REPTFILE.jcl` | Load report parameters file | `DATEPARM` |

### 4.2 Batch Processing Jobs

| Job | Executes Program | Description |
|---|---|---|
| `POSTTRAN.jcl` | CBTRN02C | **Core transaction posting** - Posts daily transactions to master, updates balances |
| `INTCALC.jcl` | CBACT04C | **Interest calculation** - Computes and posts interest on account balances |
| `COMBTRAN.jcl` | (SORT + IDCAMS) | Combines new daily transactions with master transaction file |
| `CREASTMT.JCL` | CBSTM03A | **Statement generation** - Sorts transactions, produces text + HTML statements |
| `TRANREPT.jcl` | CBTRN03C (+ REPROC + SORT) | **Daily transaction report** - Sorts and formats daily transaction report |
| `TRANBKP.jcl` | (REPROC + IDCAMS) | **Transaction backup** - Copies transaction VSAM to GDG backup |
| `CBEXPORT.jcl` | CBEXPORT | **Data export** - Exports all master files to single tagged file |
| `CBIMPORT.jcl` | CBIMPORT | **Data import** - Imports tagged file into individual sequential files |
| `WAITSTEP.jcl` | COBSWAIT | Wait/pause utility step |

### 4.3 CICS File Management Jobs

| Job | Description |
|---|---|
| `CLOSEFIL.jcl` | Close all CICS-managed VSAM files (required before batch updates) |
| `OPENFIL.jcl` | Re-open all CICS-managed VSAM files (after batch updates complete) |

### 4.4 Infrastructure / Utility Jobs

| Job | Description |
|---|---|
| `DEFGDGB.jcl` | Define GDG base for transaction backups |
| `DEFGDGD.jcl` | Define GDG base for daily transaction files |
| `TRANIDX.jcl` | Define/rebuild alternate indexes on TRANSACT VSAM |
| `DEFCUST.jcl` | Define customer VSAM cluster |
| `ESDSRRDS.jcl` | Define ESDS/RRDS VSAM clusters |
| `READACCT.jcl` | Diagnostic - read and display account records |
| `READCARD.jcl` | Diagnostic - read and display card records |
| `READCUST.jcl` | Diagnostic - read and display customer records |
| `READXREF.jcl` | Diagnostic - read and display cross-reference records |
| `DALYREJS.jcl` | Define daily rejects dataset |
| `PRTCATBL.jcl` | Print transaction category balance file |
| `CBADMCDJ.jcl` | Administrative card processing utility |
| `FTPJCL.JCL` | FTP file transfer utility |
| `INTRDRJ1.JCL` | Internal reader job 1 - submits INTRDRJ2 |
| `INTRDRJ2.JCL` | Internal reader job 2 - chained from INTRDRJ1 |
| `TXT2PDF1.JCL` | Convert text statements to PDF using TXT2PDF utility |

### 4.5 Batch Cycle Execution Order

```
CLOSEFIL -> Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ)
         -> POSTTRAN (transaction posting)
         -> INTCALC (interest calculation)
         -> TRANBKP (transaction backup)
         -> COMBTRAN (combine transactions)
         -> CREASTMT (statement generation)
         -> TRANREPT (transaction report)
         -> TRANIDX (rebuild alternate indexes)
         -> OPENFIL
```

---

## 5. Assembler Programs (`app/asm/`)

| Program | Description |
|---|---|
| `MVSWAIT.asm` | MVS WAIT macro - pauses execution for a specified interval (called by COBSWAIT) |
| `COBDATFT.asm` | Date format conversion assembler routine |

---

## 6. JCL Procedures (`app/proc/`)

| Procedure | Description |
|---|---|
| `REPROC.prc` | Reusable REPRO procedure - copies VSAM KSDS to sequential backup via IDCAMS |
| `TRANREPT.prc` | Transaction report procedure - wrapper for CBTRN03C execution |

---

## 7. Classification Summary

### By Business Domain

| Domain | Online Programs | Batch Programs | Total |
|---|---|---|---|
| Account Management | COACTVWC, COACTUPC | CBACT01C, CBACT04C | 4 |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBACT03C | 5 |
| Transaction Management | COTRN00C, COTRN01C, COTRN02C | CBTRN01C, CBTRN02C | 5 |
| Billing / Payments | COBIL00C | -- | 1 |
| Reporting | CORPT00C | CBTRN03C, CBSTM03A, CBSTM03B | 4 |
| User Administration | COUSR00C-03C, COADM01C | -- | 5 |
| Security / Auth | COSGN00C | -- | 1 |
| Navigation | COMEN01C | -- | 1 |
| Customer Management | -- | CBCUS01C | 1 |
| Data Migration | -- | CBEXPORT, CBIMPORT | 2 |
| Utilities | -- | CSUTLDTC, COBSWAIT | 2 |

### By Technology Stack

| Technology | Count | Programs |
|---|---|---|
| COBOL + CICS + VSAM | 17 | All CO* online programs |
| COBOL + Batch + Sequential/VSAM | 14 | All CB* batch programs + CSUTLDTC |
| COBOL + CICS + IMS DB + DB2 + MQ | 8 | Authorization module |
| COBOL + CICS + DB2 | 3 | Transaction type DB2 module |
| COBOL + CICS + MQ | 2 | VSAM-MQ module |
| Assembler | 2 | MVSWAIT, COBDATFT |

### By Modernization Priority

| Priority | Rationale | Programs |
|---|---|---|
| **P0 - Critical Path** | Core transaction processing and account management | CBTRN02C, CBACT04C, COACTUPC, COTRN02C, COBIL00C |
| **P1 - High** | Primary user-facing screens and data access | COSGN00C, COMEN01C, COACTVWC, COCRDLIC, COTRN00C, CORPT00C |
| **P2 - Medium** | Secondary CRUD screens and batch reporting | COCRDSLC, COCRDUPC, COTRN01C, COUSR00C-03C, CBTRN03C, CBSTM03A/B |
| **P3 - Low** | Utilities, diagnostics, and data migration tools | CBACT01-03C, CBCUS01C, CBEXPORT, CBIMPORT, CSUTLDTC, COBSWAIT |
| **P4 - Optional** | Extension modules (IMS/DB2/MQ) - separate modernization track | All optional module programs |
