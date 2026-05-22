# Application Inventory — CardDemo Mainframe System

> Generated: 2026-05-22 | Source: `uc-legacy-modernization-cobol-to-java`

## Summary

| Category | Count |
|:---------|------:|
| **COBOL Programs (Core)** | 31 |
| **COBOL Programs (Extensions)** | 13 |
| **Copybooks (Core)** | 30 |
| **Copybooks (Extensions)** | 17 |
| **JCL Jobs (Core)** | 38 |
| **JCL Jobs (Extensions)** | 8 |
| **BMS Maps (Core)** | 17 |
| **BMS Maps (Extensions)** | 4 |
| **Total Artifacts** | **158** |

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online (CICS) Programs

| # | Program | LOC | Transaction | Function | Classification | VSAM Files Accessed |
|:-:|:--------|----:|:------------|:---------|:---------------|:--------------------|
| 1 | `COSGN00C.cbl` | 260 | CC00 | Sign-on / Authentication | Security | USRSEC |
| 2 | `COMEN01C.cbl` | 308 | CM00 | Main Menu (Regular Users) | Navigation | — |
| 3 | `COADM01C.cbl` | 288 | CA00 | Admin Menu | Navigation | — |
| 4 | `COACTVWC.cbl` | 941 | CAVW | Account View | Account Mgmt | ACCTDAT, CARDXREF, CUSTDAT, CARDDAT |
| 5 | `COACTUPC.cbl` | 4,236 | CAUP | Account Update | Account Mgmt | ACCTDAT, CARDXREF, CUSTDAT |
| 6 | `COCRDLIC.cbl` | 1,459 | CCLI | Credit Card List | Card Mgmt | CARDDAT |
| 7 | `COCRDSLC.cbl` | 887 | CCDL | Credit Card View/Detail | Card Mgmt | CARDDAT, CUSTDAT |
| 8 | `COCRDUPC.cbl` | 1,560 | CCUP | Credit Card Update | Card Mgmt | CARDDAT, CUSTDAT |
| 9 | `COTRN00C.cbl` | 699 | CT00 | Transaction List | Transaction Mgmt | TRANSACT |
| 10 | `COTRN01C.cbl` | 330 | CT01 | Transaction View | Transaction Mgmt | TRANSACT |
| 11 | `COTRN02C.cbl` | 783 | CT02 | Transaction Add | Transaction Mgmt | TRANSACT, CARDXREF, CXACAIX |
| 12 | `CORPT00C.cbl` | 649 | CR00 | Transaction Report (submits batch via TDQ) | Reporting | — |
| 13 | `COBIL00C.cbl` | 572 | CB00 | Bill Payment | Payments | ACCTDAT, CXACAIX, TRANSACT |
| 14 | `COUSR00C.cbl` | 695 | CU00 | List Users (Admin) | User Mgmt | USRSEC |
| 15 | `COUSR01C.cbl` | 299 | CU01 | Add User (Admin) | User Mgmt | USRSEC |
| 16 | `COUSR02C.cbl` | 414 | CU02 | Update User (Admin) | User Mgmt | USRSEC |
| 17 | `COUSR03C.cbl` | 359 | CU03 | Delete User (Admin) | User Mgmt | USRSEC |

### 1.2 Batch Programs

| # | Program | LOC | Function | Classification | Files Read | Files Written |
|:-:|:--------|----:|:---------|:---------------|:-----------|:--------------|
| 18 | `CBACT01C.cbl` | 430 | Read account file, write multiple output formats | Data Utility | ACCTFILE | OUT-ACCT, ARR-ARRAY, VBR (VB) |
| 19 | `CBACT02C.cbl` | 178 | Read and print card data | Data Utility | CARDFILE | — (DISPLAY) |
| 20 | `CBACT03C.cbl` | 178 | Read and print cross-reference data | Data Utility | XREFFILE | — (DISPLAY) |
| 21 | `CBACT04C.cbl` | 652 | Interest calculator (daily posting) | Financial Processing | TCATBALF, ACCTFILE, XREFFILE, DISCGRP | TRANFILE |
| 22 | `CBCUS01C.cbl` | 178 | Read and print customer data | Data Utility | CUSTFILE | — (DISPLAY) |
| 23 | `CBTRN01C.cbl` | 494 | Post daily transactions (reader) | Transaction Processing | DALYTRAN, XREFFILE, ACCTFILE | — |
| 24 | `CBTRN02C.cbl` | 731 | Post daily transactions (writer/validator) | Transaction Processing | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF | TRANFILE, DALYREJS, TCATBALF, ACCTFILE |
| 25 | `CBTRN03C.cbl` | 649 | Print transaction detail report | Reporting | TRANSACT, DATEPARM | RPTFILE |
| 26 | `CBSTM03A.CBL` | 924 | Print account statements (text + HTML) | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | STMTFILE, HTMLFILE |
| 27 | `CBSTM03B.CBL` | 230 | File I/O subroutine for statement generation | Subroutine | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — |
| 28 | `CSUTLDTC.cbl` | 157 | Date validation utility (calls CEEDAYS) | Utility | — | — |
| 29 | `COBSWAIT.cbl` | 41 | Wait utility (calls MVSWAIT assembler) | Utility | — | — |
| 30 | `CBEXPORT.cbl` | 582 | Export customer data for branch migration | Data Migration | CUSTFILE, ACCTFILE, XREFFILE, TRANFILE, CARDFILE | EXPORTFILE |
| 31 | `CBIMPORT.cbl` | 487 | Import customer data from branch export | Data Migration | EXPORTFILE | CUSTFILE, ACCTFILE, XREFFILE, TRANFILE, CARDFILE, ERRORFILE |

---

## 2. COBOL Programs — Extensions

### 2.1 IMS-DB2-MQ Pending Authorization (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | LOC | Type | Function |
|:-:|:--------|----:|:-----|:---------|
| 32 | `COPAUA0C.cbl` | 1,026 | CICS/IMS/MQ | Card Authorization Decision — MQ trigger, IMS read/write |
| 33 | `COPAUS0C.cbl` | 1,032 | CICS/IMS/BMS | Pending Authorization Summary View |
| 34 | `COPAUS1C.cbl` | 604 | CICS/IMS/BMS | Pending Authorization Detail View |
| 35 | `COPAUS2C.cbl` | 244 | CICS/IMS/DB2 | Mark Authorization as Fraud |
| 36 | `CBPAUP0C.cbl` | 386 | Batch/IMS | Purge Expired Authorizations |
| 37 | `DBUNLDGS.CBL` | 366 | Batch/IMS | IMS DB Generic Segment Unload |
| 38 | `PAUDBLOD.CBL` | 369 | Batch/IMS | IMS DB Authorization Load |
| 39 | `PAUDBUNL.CBL` | 317 | Batch/IMS | IMS DB Authorization Unload |

### 2.2 DB2 Transaction Type (`app/app-transaction-type-db2/cbl/`)

| # | Program | LOC | Type | Function |
|:-:|:--------|----:|:-----|:---------|
| 40 | `COTRTLIC.cbl` | 2,098 | CICS/DB2 | Transaction Type List (cursor-based) with update/delete |
| 41 | `COTRTUPC.cbl` | 1,702 | CICS/DB2 | Transaction Type Add/Edit |
| 42 | `COBTUPDT.cbl` | 237 | Batch/DB2 | Batch maintenance of transaction type table |

### 2.3 VSAM-MQ Integration (`app/app-vsam-mq/cbl/`)

| # | Program | LOC | Type | Function |
|:-:|:--------|----:|:-----|:---------|
| 43 | `CODATE01.cbl` | 524 | CICS/MQ | System date inquiry via MQ request/response |
| 44 | `COACCT01.cbl` | 620 | CICS/MQ | Account details inquiry via MQ request/response |

---

## 3. Copybooks — Core (`app/cpy/`)

### 3.1 Data Record Layouts

| Copybook | Record Name | Description | Record Length |
|:---------|:------------|:------------|-------------:|
| `CVACT01Y.cpy` | ACCOUNT-RECORD | Account master | 300 |
| `CVACT02Y.cpy` | CARD-RECORD | Credit card master | 150 |
| `CVACT03Y.cpy` | CARD-XREF-RECORD | Customer-Account-Card cross-reference | 50 |
| `CVCUS01Y.cpy` | CUSTOMER-RECORD | Customer master | 500 |
| `CSUSR01Y.cpy` | SEC-USER-DATA | User security record | 80 |
| `CVTRA01Y.cpy` | TRAN-CAT-BAL-RECORD | Transaction category balance | 50 |
| `CVTRA02Y.cpy` | DIS-GROUP-RECORD | Disclosure group (interest rates) | 50 |
| `CVTRA03Y.cpy` | TRAN-TYPE-RECORD | Transaction type reference | 60 |
| `CVTRA04Y.cpy` | TRAN-CAT-RECORD | Transaction category type | 60 |
| `CVTRA05Y.cpy` | TRAN-RECORD | Transaction master | 350 |
| `CVTRA06Y.cpy` | DALYTRAN-RECORD | Daily transaction (batch input) | 350 |
| `CVTRA07Y.cpy` | REPORT-NAME-HEADER / TRANSACTION-DETAIL-REPORT | Report layout definitions | — |
| `COSTM01.CPY` | TRNX-RECORD | Transaction file (keyed by card+tran ID) | 350 |
| `CUSTREC.cpy` | CUSTOMER-RECORD | Alternate customer layout (statement pgm) | 500 |
| `CVEXPORT.cpy` | EXPORT-RECORD | Branch migration export record (multi-type) | 505 |
| `UNUSED1Y.cpy` | UNUSED-DATA | Unused/placeholder record | 80 |

### 3.2 Application Control Structures

| Copybook | Description |
|:---------|:------------|
| `COCOM01Y.cpy` | CICS Commarea — general info, customer, account, card, navigation |
| `COADM02Y.cpy` | Admin menu option definitions (6 options, including DB2) |
| `COMEN02Y.cpy` | Main menu option definitions (11 options) |
| `CVCRD01Y.cpy` | Screen work areas — AID key handling, program routing, messages |
| `COTTL01Y.cpy` | Screen title constants |
| `CSMSG01Y.cpy` | Common UI messages (thank-you, invalid key) |
| `CSMSG02Y.cpy` | Abend data work areas |
| `CSDAT01Y.cpy` | Date/time work areas (current date, timestamp) |
| `CSSETATY.cpy` | BMS field attribute setter (COPY REPLACING pattern) |
| `CSSTRPFY.cpy` | PF-key storage routine (EVALUATE EIBAID) |
| `CSUTLDPY.cpy` | Date validation paragraphs (EDIT-DATE-CCYYMMDD) |
| `CSUTLDWY.cpy` | Date validation working-storage fields |
| `CODATECN.cpy` | Assembler date format conversion I/O record |
| `CSLKPCDY.cpy` | Lookup code repository (phone area codes, US states, zip prefixes) |

### 3.3 Extension Copybooks

**IMS-DB2-MQ (`app/app-authorization-ims-db2-mq/cpy/`):**

| Copybook | Description |
|:---------|:------------|
| `CIPAUSMY.cpy` | IMS Pending Authorization Summary segment |
| `CIPAUDTY.cpy` | IMS Pending Authorization Detail segment (COMP-3 keys) |
| `CCPAURQY.cpy` | Authorization Request message layout (MQ) |
| `CCPAURLY.cpy` | Authorization Reply message layout (MQ) |
| `CCPAUERY.cpy` | Error Log record layout |
| `IMSFUNCS.cpy` | IMS function code constants |
| `PAUTBPCB.CPY` | IMS PCB masks (authorization DB) |
| `PADFLPCB.CPY` | IMS PCB mask (detail flat DB) |
| `PASFLPCB.CPY` | IMS PCB mask (summary flat DB) |

**DB2 Transaction Type (`app/app-transaction-type-db2/cpy/`):**

| Copybook | Description |
|:---------|:------------|
| `CSDB2RPY.cpy` | DB2 SQLCA return code handling |
| `CSDB2RWY.cpy` | DB2 working storage for SQL operations |

---

## 4. JCL Jobs — Core (`app/jcl/`)

### 4.1 Data Loading / VSAM Management

| Job | Program | Function | Files |
|:----|:--------|:---------|:------|
| `ACCTFILE.jcl` | IDCAMS | Refresh Account Master VSAM | ACCTDATA.PS → ACCTDAT |
| `CARDFILE.jcl` | IDCAMS | Refresh Card Master VSAM | CARDDATA.PS → CARDDAT |
| `CUSTFILE.jcl` | IDCAMS | Refresh Customer Master VSAM | CUSTDATA.PS → CUSTDAT |
| `XREFFILE.jcl` | IDCAMS | Load Card-Account-Customer cross-reference | CARDXREF.PS → CXREF |
| `TRANFILE.jcl` | IDCAMS | Load Transaction Master | DALYTRAN.PS → TRANSACT |
| `DISCGRP.jcl` | IDCAMS | Load Disclosure Groups | DISCGRP.PS → DISCGRP |
| `TRANCATG.jcl` | IDCAMS | Load Transaction Category Types | TRANCATG.PS → TRANCATG |
| `TRANTYPE.jcl` | IDCAMS | Load Transaction Types | TRANTYPE.PS → TRANTYPE |
| `TCATBALF.jcl` | IDCAMS | Refresh Transaction Category Balance | TCATBALF.PS → TCATBALF |
| `TRANBKP.jcl` | IDCAMS | Backup/Refresh Transaction Master | TRANSACT → backup |
| `TRANIDX.jcl` | IDCAMS | Define AIX on Transaction file | TRANSACT (AIX) |
| `ESDSRRDS.jcl` | IDCAMS | Create ESDS and RRDS VSAM files | — |
| `DEFGDGB.jcl` | IDCAMS | Define GDG bases | — |
| `DEFGDGD.jcl` | IDCAMS | Define additional GDG bases (for DB2) | — |
| `DEFCUST.jcl` | IDCAMS | Define customer VSAM cluster | — |

### 4.2 CICS File Control

| Job | Program | Function |
|:----|:--------|:---------|
| `CLOSEFIL.jcl` | IEFBR14 | Close VSAM files in CICS |
| `OPENFIL.jcl` | IEFBR14 | Open files in CICS |

### 4.3 Batch Processing

| Job | Program | Function |
|:----|:--------|:---------|
| `POSTTRAN.jcl` | CBTRN02C | Core transaction posting |
| `INTCALC.jcl` | CBACT04C | Interest calculations |
| `COMBTRAN.jcl` | SORT | Combine system + daily transactions |
| `CREASTMT.JCL` | CBSTM03A | Produce account statements |
| `TRANREPT.jcl` | CBTRN03C | Transaction detail report |
| `DALYREJS.jcl` | — | Daily rejected transactions |
| `WAITSTEP.jcl` | COBSWAIT | Timer wait step |

### 4.4 Data Reader/Printer Jobs

| Job | Program | Function |
|:----|:--------|:---------|
| `READACCT.jcl` | CBACT01C | Read/print account data |
| `READCARD.jcl` | CBACT02C | Read/print card data |
| `READCUST.jcl` | CBCUS01C | Read/print customer data |
| `READXREF.jcl` | CBACT03C | Read/print cross-reference data |
| `REPTFILE.jcl` | — | Report file definitions |
| `PRTCATBL.jcl` | — | Print category balance |

### 4.5 Data Migration

| Job | Program | Function |
|:----|:--------|:---------|
| `CBEXPORT.jcl` | CBEXPORT | Branch migration data export |
| `CBIMPORT.jcl` | CBIMPORT | Branch migration data import |

### 4.6 Security

| Job | Program | Function |
|:----|:--------|:---------|
| `DUSRSECJ.jcl` | IEBGENER | Initial load of user security file |
| `CBADMCDJ.jcl` | — | Admin card definition |

### 4.7 Utility / Advanced

| Job | Program | Function |
|:----|:--------|:---------|
| `FTPJCL.JCL` | FTP | FTP file transfer |
| `TXT2PDF1.JCL` | TXT2PDF | Text to PDF conversion |
| `INTRDRJ1.JCL` | — | Internal reader job (1) |
| `INTRDRJ2.JCL` | — | Internal reader job (2) |

### 4.8 Extension JCL

| Job | Module | Program | Function |
|:----|:-------|:--------|:---------|
| `CBPAUP0J.jcl` | IMS-DB2-MQ | CBPAUP0C | Purge expired authorizations |
| `DBPAUTP0.jcl` | IMS-DB2-MQ | — | Authorization DB provisioning |
| `LOADPADB.JCL` | IMS-DB2-MQ | PAUDBLOD | Load authorization IMS DB |
| `UNLDPADB.JCL` | IMS-DB2-MQ | PAUDBUNL | Unload authorization IMS DB |
| `UNLDGSAM.JCL` | IMS-DB2-MQ | DBUNLDGS | Generic IMS segment unload |
| `CREADB21.jcl` | DB2 Tran Type | DSNTEP4 | Create DB2 database and tables |
| `TRANEXTR.jcl` | DB2 Tran Type | DSNTIAUL | Extract DB2 transaction type data |
| `MNTTRDB2.jcl` | DB2 Tran Type | COBTUPDT | Batch maintain transaction types |

---

## 5. BMS Maps (`app/bms/`)

### 5.1 Core Maps

| Map | Mapset | Associated Program | Screen |
|:----|:-------|:-------------------|:-------|
| `COSGN00.bms` | COSGN00 | COSGN00C | Sign-on |
| `COMEN01.bms` | COMEN01 | COMEN01C | Main Menu |
| `COADM01.bms` | COADM01 | COADM01C | Admin Menu |
| `COACTVW.bms` | COACTVW | COACTVWC | Account View |
| `COACTUP.bms` | COACTUP | COACTUPC | Account Update |
| `COCRDLI.bms` | COCRDLI | COCRDLIC | Credit Card List |
| `COCRDSL.bms` | COCRDSL | COCRDSLC | Credit Card View |
| `COCRDUP.bms` | COCRDUP | COCRDUPC | Credit Card Update |
| `COTRN00.bms` | COTRN00 | COTRN00C | Transaction List |
| `COTRN01.bms` | COTRN01 | COTRN01C | Transaction View |
| `COTRN02.bms` | COTRN02 | COTRN02C | Transaction Add |
| `CORPT00.bms` | CORPT00 | CORPT00C | Transaction Report |
| `COBIL00.bms` | COBIL00 | COBIL00C | Bill Payment |
| `COUSR00.bms` | COUSR00 | COUSR00C | User List |
| `COUSR01.bms` | COUSR01 | COUSR01C | User Add |
| `COUSR02.bms` | COUSR02 | COUSR02C | User Update |
| `COUSR03.bms` | COUSR03 | COUSR03C | User Delete |

### 5.2 Extension Maps

| Map | Module | Associated Program | Screen |
|:----|:-------|:-------------------|:-------|
| `COPAU00.bms` | IMS-DB2-MQ | COPAUS0C | Pending Authorization Summary |
| `COPAU01.bms` | IMS-DB2-MQ | COPAUS1C | Pending Authorization Detail |
| `COTRTLI.bms` | DB2 Tran Type | COTRTLIC | Transaction Type List |
| `COTRTUP.bms` | DB2 Tran Type | COTRTUPC | Transaction Type Add/Edit |

---

## 6. Other Artifacts

### Assembler Programs (`app/asm/`)
- **MVSWAIT** — Timer control for batch wait steps
- **COBDATFT** — Date format conversion utility (called from COBOL)

### Control-M Scheduler (`app/scheduler/`)
- Batch job orchestration definitions for the daily cycle

### CSD Definitions (`app/csd/`, extension `csd/` dirs)
- CICS resource definitions for programs, mapsets, transactions, and files

### Data Files (`app/data/`)
- `ASCII/` — ASCII-encoded sample data
- `EBCDIC/` — EBCDIC-encoded production sample data (accounts, cards, customers, transactions, cross-references)

### Samples (`samples/m2/`)
- AWS Mainframe Modernization (M2) runtime artifacts
- UniKix rehosting environment configurations
