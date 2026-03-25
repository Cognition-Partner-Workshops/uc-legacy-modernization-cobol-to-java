# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This document catalogs every artifact in the CardDemo COBOL codebase: programs, copybooks, JCL jobs, BMS maps, assembler routines, and optional extension modules.

---

## 1. Summary Statistics

| Artifact Type | Core Count | Optional Module Count | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | 44 |
| Copybooks (data) | 30 | — | 30 |
| Copybooks (BMS-generated) | 17 | — | 17 |
| BMS Screen Maps | 17 | — | 17 |
| JCL Batch Jobs | 38 | — | 38 |
| Assembler Programs | 2 | — | 2 |
| JCL Procedures | 2 | — | 2 |
| Scheduler Configs | 2 | — | 2 |
| **Grand Total** | **139** | **13** | **152** |

---

## 2. COBOL Programs — Core (app/cbl/)

### 2.1 Online CICS Programs (CO* prefix)

These programs run under CICS and provide the interactive 3270 terminal interface.

| # | Program ID | File | Lines | Function | CICS Trans | BMS Map | Classification |
|---|---|---|---|---|---|---|---|
| 1 | COSGN00C | COSGN00C.cbl | 260 | User sign-on / authentication | CC00 | COSGN00 | Security — Authentication |
| 2 | COMEN01C | COMEN01C.cbl | 308 | Main menu — regular user | CM00 | COMEN01 | Navigation — Menu |
| 3 | COADM01C | COADM01C.cbl | 288 | Admin menu | CA00 | COADM01 | Navigation — Admin Menu |
| 4 | COACTVWC | COACTVWC.cbl | 941 | View account details | CA01 | COACTVW | Account Management — View |
| 5 | COACTUPC | COACTUPC.cbl | 4,236 | Update account details | CA02 | COACTUP | Account Management — Update |
| 6 | COCRDLIC | COCRDLIC.cbl | 1,459 | List credit cards (browse) | CC01 | COCRDLI | Card Management — List |
| 7 | COCRDSLC | COCRDSLC.cbl | 887 | View single card details | CC02 | COCRDSL | Card Management — View |
| 8 | COCRDUPC | COCRDUPC.cbl | 1,560 | Update card details | CC03 | COCRDUP | Card Management — Update |
| 9 | COTRN00C | COTRN00C.cbl | 699 | List transactions (browse) | CT00 | COTRN00 | Transaction Management — List |
| 10 | COTRN01C | COTRN01C.cbl | 330 | View single transaction | CT01 | COTRN01 | Transaction Management — View |
| 11 | COTRN02C | COTRN02C.cbl | 783 | Add a new transaction | CT02 | COTRN02 | Transaction Management — Add |
| 12 | CORPT00C | CORPT00C.cbl | 649 | Generate transaction reports | CR00 | CORPT00 | Reporting |
| 13 | COBIL00C | COBIL00C.cbl | 572 | Process bill payments | CB00 | COBIL00 | Bill Payment |
| 14 | COUSR00C | COUSR00C.cbl | 695 | List all users | CU00 | COUSR00 | Admin — User List |
| 15 | COUSR01C | COUSR01C.cbl | 299 | Add a new user | CU01 | COUSR01 | Admin — User Add |
| 16 | COUSR02C | COUSR02C.cbl | 414 | Update a user | CU02 | COUSR02 | Admin — User Update |
| 17 | COUSR03C | COUSR03C.cbl | 359 | Delete a user | CU03 | COUSR03 | Admin — User Delete |

### 2.2 Batch Programs (CB* prefix)

These programs run in batch mode (JCL-initiated) for nightly/scheduled processing.

| # | Program ID | File | Lines | Function | Classification |
|---|---|---|---|---|---|
| 18 | CBACT01C | CBACT01C.cbl | 430 | Read/process account master file | Batch — Account Processing |
| 19 | CBACT02C | CBACT02C.cbl | 178 | Read/process card data file | Batch — Card Processing |
| 20 | CBACT03C | CBACT03C.cbl | 178 | Read/process card cross-reference file | Batch — Cross-Reference Processing |
| 21 | CBACT04C | CBACT04C.cbl | 652 | Calculate interest on accounts | Batch — Interest Calculation |
| 22 | CBCUS01C | CBCUS01C.cbl | 178 | Read/process customer data file | Batch — Customer Processing |
| 23 | CBTRN01C | CBTRN01C.cbl | 494 | Validate daily transactions | Batch — Transaction Validation |
| 24 | CBTRN02C | CBTRN02C.cbl | 731 | Post daily transactions to master | Batch — Transaction Posting |
| 25 | CBTRN03C | CBTRN03C.cbl | 649 | Generate daily transaction report | Batch — Transaction Reporting |
| 26 | CBSTM03A | CBSTM03A.CBL | 924 | Generate account statements (text + HTML) | Batch — Statement Generation |
| 27 | CBSTM03B | CBSTM03B.CBL | 230 | Subroutine for statement file I/O | Batch — Statement Subroutine |
| 28 | CBEXPORT | CBEXPORT.cbl | 582 | Export all VSAM data to flat file | Batch — Data Export |
| 29 | CBIMPORT | CBIMPORT.cbl | 487 | Import flat file data into VSAM | Batch — Data Import |

### 2.3 Utility Programs

| # | Program ID | File | Lines | Function | Classification |
|---|---|---|---|---|---|
| 30 | CSUTLDTC | CSUTLDTC.cbl | 157 | Date conversion utility (CEEDAYS) | Utility — Date Handling |
| 31 | COBSWAIT | COBSWAIT.cbl | 41 | Wait/delay utility | Utility — System Wait |

**Core Total: 31 programs · 20,650 lines of COBOL**

---

## 3. COBOL Programs — Optional Modules

### 3.1 Authorization Module (app/app-authorization-ims-db2-mq/)

IMS DB + DB2 + MQ integration for card authorization workflow.

| # | Program ID | File | Lines | Function | Technology | Classification |
|---|---|---|---|---|---|---|
| 32 | COPAUA0C | COPAUA0C.cbl | 1,026 | Card authorization decision (MQ trigger) | CICS/IMS/MQ | Authorization — Decision |
| 33 | COPAUS0C | COPAUS0C.cbl | 1,032 | Summary view of auth messages | CICS/IMS/BMS | Authorization — Summary View |
| 34 | COPAUS1C | COPAUS1C.cbl | 604 | Detail view of auth message | CICS/IMS/BMS | Authorization — Detail View |
| 35 | COPAUS2C | COPAUS2C.cbl | 244 | Mark auth message as fraud (DB2) | CICS/IMS/DB2 | Authorization — Fraud Marking |
| 36 | CBPAUP0C | CBPAUP0C.cbl | 386 | Batch purge expired auth messages | Batch/IMS | Authorization — Batch Purge |
| 37 | DBUNLDGS | DBUNLDGS.CBL | 366 | IMS DB unload segments | Batch/IMS | Authorization — DB Unload |
| 38 | PAUDBLOD | PAUDBLOD.CBL | 369 | IMS DB load for auth data | Batch/IMS | Authorization — DB Load |
| 39 | PAUDBUNL | PAUDBUNL.CBL | 317 | IMS DB unload for auth data | Batch/IMS | Authorization — DB Unload |

### 3.2 Transaction Type DB2 Module (app/app-transaction-type-db2/)

DB2 CRUD operations for transaction type management with embedded SQL.

| # | Program ID | File | Lines | Function | Technology | Classification |
|---|---|---|---|---|---|---|
| 40 | COTRTUPC | COTRTUPC.cbl | 1,702 | Add/edit transaction types (DB2) | CICS/DB2 | Transaction Type — Add/Edit |
| 41 | COTRTLIC | COTRTLIC.cbl | 2,098 | List/delete transaction types (DB2) | CICS/DB2 | Transaction Type — List/Delete |
| 42 | COBTUPDT | COBTUPDT.cbl | 237 | Batch update of transaction types | Batch/DB2 | Transaction Type — Batch Update |

### 3.3 VSAM-MQ Module (app/app-vsam-mq/)

MQ request/response service programs for date and account inquiry.

| # | Program ID | File | Lines | Function | Technology | Classification |
|---|---|---|---|---|---|---|
| 43 | CODATE01 | CODATE01.cbl | 524 | MQ service — system date | CICS/MQ | Service — Date Query |
| 44 | COACCT01 | COACCT01.cbl | 620 | MQ service — account inquiry | CICS/MQ | Service — Account Inquiry |

**Optional Total: 13 programs · 9,525 lines of COBOL**

---

## 4. Copybooks — Data Structures (app/cpy/)

Copybooks define reusable record layouts (data structures) included via COPY statements.

### 4.1 Business Entity Copybooks (CV* prefix)

| # | Copybook | Record Length | Entity | Description |
|---|---|---|---|---|
| 1 | CVACT01Y.cpy | 300 bytes | Account | Account master record layout |
| 2 | CVACT02Y.cpy | 150 bytes | Card | Card data record layout |
| 3 | CVACT03Y.cpy | 50 bytes | Card Cross-Ref | Card-to-account cross-reference |
| 4 | CVCUS01Y.cpy | 500 bytes | Customer | Customer master record layout |
| 5 | CVCRD01Y.cpy | — | Card (detail) | Card detail fields for screen use |
| 6 | CVTRA01Y.cpy | 50 bytes | Tran Cat Balance | Transaction category balance |
| 7 | CVTRA02Y.cpy | 50 bytes | Disclosure Group | Interest rate disclosure groups |
| 8 | CVTRA03Y.cpy | 60 bytes | Transaction Type | Transaction type code/description |
| 9 | CVTRA04Y.cpy | 60 bytes | Transaction Category | Transaction category code/description |
| 10 | CVTRA05Y.cpy | 350 bytes | Transaction | Online transaction record |
| 11 | CVTRA06Y.cpy | 350 bytes | Daily Transaction | Daily transaction record (batch input) |
| 12 | CVTRA07Y.cpy | — | Report Layout | Transaction report print layout |
| 13 | CVEXPORT.cpy | — | Export Record | Combined export record layout |

### 4.2 Common / System Copybooks (CO*/CS* prefix)

| # | Copybook | Description |
|---|---|---|
| 14 | COCOM01Y.cpy | COMMAREA — common data area passed between CICS programs |
| 15 | COMEN02Y.cpy | Menu option definitions (program names, transaction IDs) |
| 16 | COADM02Y.cpy | Admin menu option definitions |
| 17 | COTTL01Y.cpy | Screen title/header layout |
| 18 | CODATECN.cpy | Date conversion constants |
| 19 | COSTM01.CPY | Altered transaction layout for statement reporting |
| 20 | CUSTREC.cpy | Customer record layout (statement generation) |

### 4.3 System Service Copybooks

| # | Copybook | Description |
|---|---|---|
| 21 | CSUSR01Y.cpy | User security record layout (80 bytes) |
| 22 | CSDAT01Y.cpy | Date/time fields for screen display |
| 23 | CSMSG01Y.cpy | Screen message area (primary) |
| 24 | CSMSG02Y.cpy | Screen message area (secondary/confirmation) |
| 25 | CSSETATY.cpy | Set attribute bytes utility |
| 26 | CSSTRPFY.cpy | Strip PF key utility |
| 27 | CSLKPCDY.cpy | Lookup code utility |
| 28 | CSUTLDPY.cpy | Date processing utility fields |
| 29 | CSUTLDWY.cpy | Date working fields |
| 30 | UNUSED1Y.cpy | Unused placeholder record |

---

## 5. Copybooks — BMS-Generated (app/cpy-bms/)

These are auto-generated by the BMS assembler from BMS map definitions. Each corresponds to a BMS map and provides the symbolic field names used by programs to read/write screen data.

| # | BMS Copybook | Source Map | Used By Program |
|---|---|---|---|
| 1 | COACTUP.CPY | COACTUP.bms | COACTUPC |
| 2 | COACTVW.CPY | COACTVW.bms | COACTVWC |
| 3 | COADM01.CPY | COADM01.bms | COADM01C |
| 4 | COBIL00.CPY | COBIL00.bms | COBIL00C |
| 5 | COCRDLI.CPY | COCRDLI.bms | COCRDLIC |
| 6 | COCRDSL.CPY | COCRDSL.bms | COCRDSLC |
| 7 | COCRDUP.CPY | COCRDUP.bms | COCRDUPC |
| 8 | COMEN01.CPY | COMEN01.bms | COMEN01C |
| 9 | CORPT00.CPY | CORPT00.bms | CORPT00C |
| 10 | COSGN00.CPY | COSGN00.bms | COSGN00C |
| 11 | COTRN00.CPY | COTRN00.bms | COTRN00C |
| 12 | COTRN01.CPY | COTRN01.bms | COTRN01C |
| 13 | COTRN02.CPY | COTRN02.bms | COTRN02C |
| 14 | COUSR00.CPY | COUSR00.bms | COUSR00C |
| 15 | COUSR01.CPY | COUSR01.bms | COUSR01C |
| 16 | COUSR02.CPY | COUSR02.bms | COUSR02C |
| 17 | COUSR03.CPY | COUSR03.bms | COUSR03C |

---

## 6. BMS Screen Maps (app/bms/)

BMS (Basic Mapping Support) maps define the 3270 terminal screen layouts.

| # | Map | Screen Title | Function | Target UI Form |
|---|---|---|---|---|
| 1 | COSGN00.bms | Login Screen | User authentication | Login form |
| 2 | COMEN01.bms | Main Menu Screen | Regular user navigation | Menu selector |
| 3 | COADM01.bms | Admin Menu Screen | Admin navigation | Admin menu selector |
| 4 | COACTVW.bms | Account Viewer Screen | Display account info | Account detail view |
| 5 | COACTUP.bms | Account Update Screen | Edit account info | Account edit form |
| 6 | COCRDLI.bms | Card Listing Screen | Browse credit cards | Card list/table |
| 7 | COCRDSL.bms | Card Selection Screen | View card details | Card detail view |
| 8 | COCRDUP.bms | Card Update Screen | Edit card details | Card edit form |
| 9 | COTRN00.bms | Transaction List | Browse transactions | Transaction list/table |
| 10 | COTRN01.bms | Transaction View | View transaction details | Transaction detail view |
| 11 | COTRN02.bms | Transaction Add | Enter new transaction | Transaction entry form |
| 12 | CORPT00.bms | Report Screen | Report parameters/display | Report parameter form |
| 13 | COBIL00.bms | Bill Payment Screen | Process bill payments | Payment form |
| 14 | COUSR00.bms | List Users | Browse user list | User list/table |
| 15 | COUSR01.bms | Add User | Create new user | User creation form |
| 16 | COUSR02.bms | Update User | Modify user | User edit form |
| 17 | COUSR03.bms | Delete User | Remove user | User deletion confirmation |

---

## 7. JCL Batch Jobs (app/jcl/)

### 7.1 Data Refresh / VSAM Load Jobs

These jobs load flat-file data into VSAM KSDS files.

| # | Job Name | File | Function | Target VSAM Dataset |
|---|---|---|---|---|
| 1 | ACCTFILE | ACCTFILE.jcl | Load account master data | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | CARDFILE.jcl | Load card data | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | CUSTFILE.jcl | Load customer data | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | XREFFILE.jcl | Load card cross-reference + alt index | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | TRANFILE.jcl | Load transaction data | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | DUSRSECJ.jcl | Load user security data | USRSEC.VSAM.KSDS |
| 7 | TRANTYPE | TRANTYPE.jcl | Load transaction type codes | TRANTYPE.VSAM.KSDS |
| 8 | TRANCATG | TRANCATG.jcl | Load transaction categories | TRANCATG.VSAM.KSDS |
| 9 | TCATBALF | TCATBALF.jcl | Load transaction category balances | TCATBALF.VSAM.KSDS |
| 10 | DISCGRP | DISCGRP.jcl | Load disclosure/interest rate groups | DISCGRP.VSAM.KSDS |
| 11 | DEFCUST | DEFCUST.jcl | Define customer VSAM cluster | CUSTDATA.VSAM.KSDS |

### 7.2 Core Batch Processing Jobs

| # | Job Name | File | Function | Program Executed |
|---|---|---|---|---|
| 12 | POSTTRAN | POSTTRAN.jcl | Post daily transactions to master | CBTRN02C |
| 13 | INTCALC | INTCALC.jcl | Calculate interest on accounts | CBACT04C |
| 14 | CREASTMT | CREASTMT.JCL | Generate account statements | CBSTM03A |
| 15 | TRANREPT | TRANREPT.jcl | Generate daily transaction report | CBTRN03C |

### 7.3 Backup / Archive Jobs

| # | Job Name | File | Function |
|---|---|---|---|
| 16 | TRANBKP | TRANBKP.jcl | Backup transaction master file |
| 17 | COMBTRAN | COMBTRAN.jcl | Combine backed-up transactions with system transactions |
| 18 | DEFGDGB | DEFGDGB.jcl | Define GDG (Generation Data Group) base clusters |
| 19 | DEFGDGD | DEFGDGD.jcl | Define GDG bases + backup reference data |

### 7.4 CICS File Management Jobs

| # | Job Name | File | Function |
|---|---|---|---|
| 20 | CLOSEFIL | CLOSEFIL.jcl | Close CICS-managed files for batch access |
| 21 | OPENFIL | OPENFIL.jcl | Re-open CICS-managed files after batch |

### 7.5 Utility / Support Jobs

| # | Job Name | File | Function |
|---|---|---|---|
| 22 | READACCT | READACCT.jcl | Print/verify account VSAM data (CBACT01C) |
| 23 | READCARD | READCARD.jcl | Print/verify card VSAM data (CBACT02C) |
| 24 | READCUST | READCUST.jcl | Print/verify customer VSAM data (CBCUS01C) |
| 25 | READXREF | READXREF.jcl | Print/verify cross-ref VSAM data (CBACT03C) |
| 26 | REPTFILE | REPTFILE.jcl | Define report file datasets |
| 27 | DALYREJS | DALYREJS.jcl | Define daily rejection GDG |
| 28 | TRANIDX | TRANIDX.jcl | Define/build alternate index on transactions |
| 29 | PRTCATBL | PRTCATBL.jcl | Print category balance report |
| 30 | ESDSRRDS | ESDSRRDS.jcl | Define ESDS/RRDS VSAM clusters (demo) |
| 31 | WAITSTEP | WAITSTEP.jcl | Execute wait utility step (COBSWAIT) |
| 32 | TXT2PDF1 | TXT2PDF1.JCL | Convert statement text to PDF |

### 7.6 Data Export / Import Jobs

| # | Job Name | File | Function |
|---|---|---|---|
| 33 | CBEXPORT | CBEXPORT.jcl | Export all VSAM data to flat file (CBEXPORT) |
| 34 | CBIMPORT | CBIMPORT.jcl | Import flat file to VSAM datasets (CBIMPORT) |

### 7.7 Administration / Infrastructure Jobs

| # | Job Name | File | Function |
|---|---|---|---|
| 35 | CBADMCDJ | CBADMCDJ.jcl | Define CICS CSD resources (DFHCSDUP) |
| 36 | FTPJCL | FTPJCL.JCL | FTP file transfer job |
| 37 | INTRDRJ1 | INTRDRJ1.JCL | Internal reader — trigger INTRDRJ2 |
| 38 | INTRDRJ2 | INTRDRJ2.JCL | Internal reader — copy FTP backup |

---

## 8. Assembler Programs (app/asm/)

| # | Program | Function |
|---|---|---|
| 1 | MVSWAIT.asm | Wait/delay macro for batch job timing |
| 2 | COBDATFT.asm | Date formatting assembler subroutine |

---

## 9. JCL Procedures (app/proc/)

| # | Procedure | Function |
|---|---|---|
| 1 | REPROC.prc | Reprocessing procedure |
| 2 | TRANREPT.prc | Transaction report procedure |

---

## 10. Scheduler Configurations (app/scheduler/)

| # | File | Platform | Function |
|---|---|---|---|
| 1 | CardDemo.ca7 | CA-7 (Broadcom) | Batch job scheduling definitions |
| 2 | CardDemo.controlm | Control-M (BMC) | Batch job scheduling definitions |

---

## 11. Functional Domain Classification

| Domain | Online Programs | Batch Programs | Total Programs |
|---|---|---|---|
| Account Management | COACTVWC, COACTUPC | CBACT01C, CBACT04C | 4 |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBACT03C | 5 |
| Transaction Management | COTRN00C, COTRN01C, COTRN02C | CBTRN01C, CBTRN02C, CBTRN03C | 6 |
| Bill Payment | COBIL00C | — | 1 |
| Reporting | CORPT00C | CBSTM03A, CBSTM03B | 3 |
| User Administration | COUSR00C–COUSR03C | — | 4 |
| Security | COSGN00C | — | 1 |
| Navigation | COMEN01C, COADM01C | — | 2 |
| Data Management | — | CBEXPORT, CBIMPORT | 2 |
| Utilities | — | CSUTLDTC, COBSWAIT | 2 |
| Authorization (optional) | COPAUA0C–COPAUS2C | CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | 8 |
| Tran Type DB2 (optional) | COTRTUPC, COTRTLIC | COBTUPDT | 3 |
| VSAM-MQ (optional) | COACCT01, CODATE01 | — | 2 |

---

## 12. Sample Data Files (app/data/ASCII/)

| File | Entity | Description |
|---|---|---|
| acctdata.txt | Account | Sample account records |
| carddata.txt | Card | Sample card records |
| custdata.txt | Customer | Sample customer records |
| cardxref.txt | Cross-Reference | Card-to-account mappings |
| dailytran.txt | Daily Transaction | Sample daily transactions |
| tcatbal.txt | Category Balance | Transaction category balances |
| trancatg.txt | Transaction Category | Transaction category codes |
| trantype.txt | Transaction Type | Transaction type codes |
| discgrp.txt | Disclosure Group | Interest rate groups |

---

## 13. Naming Conventions

| Prefix/Pattern | Meaning |
|---|---|
| `CO*` | Online CICS program |
| `CB*` | Batch program |
| `CS*` | Common service copybook/utility |
| `CV*` | VSAM record layout copybook |
| `*Y` suffix (copybooks) | Standard data structure copybook |
| `LIT-*` | Literal/constant (inline in program) |
| `WS-*` | Working-storage variable |
| `CDEMO-*` | COMMAREA field prefix |
| `DFHAID` / `DFHBMSCA` | CICS system copybooks (attention IDs, BMS attributes) |
