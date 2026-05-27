# CardDemo Application Inventory

> Comprehensive catalog of all programs, copybooks, JCL jobs, BMS maps, and supporting artifacts in the CardDemo mainframe credit card management system.

---

## 1. Online CICS Programs (20 programs)

### Core Application

| Program | Transaction | Type | Function | LOC | Module |
|:--------|:-----------|:-----|:---------|----:|:-------|
| COSGN00C | CC00 | CICS COBOL | Sign-on screen — authenticates users against USRSEC VSAM file | 260 | Core |
| COMEN01C | CM00 | CICS COBOL | Main menu for regular users — routes to 11 functional screens | 308 | Core |
| COADM01C | CA00 | CICS COBOL | Admin menu for admin users — routes to 6 admin screens | 288 | Core |
| COACTVWC | CAVW | CICS COBOL | Account view — reads Account, Card, Customer, and XREF files | 941 | Core |
| COACTUPC | CAUP | CICS COBOL | Account update — full CRUD with 35+ field validations | 4,236 | Core |
| COCRDLIC | CCLI | CICS COBOL | Credit card list — paginated browse of CARDDAT VSAM file | 1,459 | Core |
| COCRDSLC | CCDL | CICS COBOL | Credit card detail view — reads card and customer records | 887 | Core |
| COCRDUPC | CCUP | CICS COBOL | Credit card update — validates and rewrites card records | 1,560 | Core |
| COTRN00C | CT00 | CICS COBOL | Transaction list — paginated browse of TRANSACT VSAM file | 699 | Core |
| COTRN01C | CT01 | CICS COBOL | Transaction view — single transaction detail display | 330 | Core |
| COTRN02C | CT02 | CICS COBOL | Transaction add — inserts new transaction with date validation | 783 | Core |
| CORPT00C | CR00 | CICS COBOL | Transaction reports — submits batch report jobs via TD queue | 649 | Core |
| COBIL00C | CB00 | CICS COBOL | Bill payment — pay account balance in full or partial | 572 | Core |
| COUSR00C | CU00 | CICS COBOL | User list — paginated browse of USRSEC file | 695 | Core |
| COUSR01C | CU01 | CICS COBOL | User add — writes new user record to USRSEC file | 299 | Core |
| COUSR02C | CU02 | CICS COBOL | User update — reads, validates, rewrites user record | 414 | Core |
| COUSR03C | CU03 | CICS COBOL | User delete — reads and deletes user from USRSEC file | 359 | Core |

### Optional: IMS/DB2/MQ Authorization Extension

| Program | Transaction | Type | Function | LOC | Module |
|:--------|:-----------|:-----|:---------|----:|:-------|
| COPAUS0C | CPVS | CICS COBOL IMS BMS | Pending authorization summary view — reads IMS and VSAM | — | IMS-DB2-MQ |
| COPAUS1C | CPVD | CICS COBOL IMS BMS | Pending authorization detail view — updates IMS, inserts DB2 | — | IMS-DB2-MQ |
| COPAUS2C | — | CICS COBOL IMS DB2 | Mark authorization message as fraud — DB2 updates | — | IMS-DB2-MQ |
| COPAUA0C | CP00 | CICS COBOL IMS MQ | Process authorization requests — MQ trigger, IMS insert/update | — | IMS-DB2-MQ |

### Optional: DB2 Transaction Type Management

| Program | Transaction | Type | Function | LOC | Module |
|:--------|:-----------|:-----|:---------|----:|:-------|
| COTRTLIC | CTLI | CICS COBOL DB2 | Transaction type list/update/delete — DB2 cursor, delete | — | DB2 Tran Type |
| COTRTUPC | CTTU | CICS COBOL DB2 | Transaction type add/edit — DB2 insert and update | — | DB2 Tran Type |

### Optional: MQ Integration

| Program | Transaction | Type | Function | LOC | Module |
|:--------|:-----------|:-----|:---------|----:|:-------|
| CODATE01 | CDRD | CICS COBOL MQ | Inquire system date via MQ request/response | — | VSAM-MQ |
| COACCT01 | CDRA | CICS COBOL MQ | Inquire account details via MQ request/response | — | VSAM-MQ |

---

## 2. Batch COBOL Programs (16 programs)

### Core Batch

| Program | Function | LOC | Key I/O | Module |
|:--------|:---------|----:|:--------|:-------|
| CBACT01C | Read account file, format dates, write output | 430 | ACCTFILE → ACCTOUT (CALL COBDATFT) | Core |
| CBACT02C | Read and print card data file | 178 | CARDFILE → SYSOUT | Core |
| CBACT03C | Read and print account cross-reference file | 178 | XREFFILE → SYSOUT | Core |
| CBACT04C | Interest calculator — compute interest on accounts | 652 | ACCTFILE, TCATBALF, DISCGRP, TRANSACT → ACCTFILE | Core |
| CBCUS01C | Read and print customer data file | 178 | CUSTFILE → SYSOUT | Core |
| CBTRN01C | Post daily transactions — initial posting pass | 494 | DALYTRAN, XREFFILE, CARDFILE, ACCTFILE → TRANSACT | Core |
| CBTRN02C | Post daily transactions — full posting with balance updates | 731 | DALYTRAN, XREFFILE, ACCTFILE, TRANSACT, TCATBALF → updated files | Core |
| CBTRN03C | Print transaction detail report by date range | 649 | TRANSACT, XREFFILE, TRANTYPE, TRANCATG → TRANREPT | Core |
| CBSTM03A | Print account statements in plain text and HTML | 924 | XREFFILE, CUSTFILE, ACCTFILE, TRANSACT → STMTFILE (CALL CBSTM03B) | Core |
| CBSTM03B | Statement print subroutine — formatting helper for CBSTM03A | 230 | Called by CBSTM03A | Core |
| CBEXPORT | Export customer data for branch migration — multi-record output | 582 | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE → EXPFILE | Core |
| CBIMPORT | Import customer data from branch migration export file | 487 | EXPFILE → CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT | Core |
| COBSWAIT | Utility wait program (PARM in centiseconds, calls MVSWAIT ASM) | 41 | n/a | Core |
| CSUTLDTC | Date validation utility — calls CEEDAYS LE routine | 157 | Called by CORPT00C, COTRN02C | Core |

### Optional: IMS/DB2/MQ Authorization Extension

| Program | Function | LOC | Module |
|:--------|:---------|----:|:-------|
| CBPAUP0C | Purge expired pending authorization messages — IMS batch | — | IMS-DB2-MQ |
| PAUDBLOD | Load pending authorization DB2 table | — | IMS-DB2-MQ |
| PAUDBUNL | Unload pending authorization DB2 table | — | IMS-DB2-MQ |
| DBUNLDGS | Unload GSAM data | — | IMS-DB2-MQ |

### Optional: DB2 Transaction Type Management

| Program | Function | LOC | Module |
|:--------|:---------|----:|:-------|
| COBTUPDT | Update transaction type table in DB2 based on input | — | DB2 Tran Type |

---

## 3. Assembler Programs (2 programs)

| Program | File | Function |
|:--------|:-----|:---------|
| COBDATFT | `app/asm/COBDATFT.asm` | Date format conversion utility — called by CBACT01C |
| MVSWAIT | `app/asm/MVSWAIT.asm` | Timer wait utility — called by COBSWAIT |

---

## 4. Copybooks (30 core + 13 extension)

### Core Copybooks (`app/cpy/`)

| Copybook | Record Length | Business Entity / Purpose |
|:---------|:------------|:--------------------------|
| CVACT01Y | 300 | **Account Record** — ID, status, balances, dates, limits |
| CVACT02Y | 150 | **Card Record** — number, CVV, embossed name, expiry, status |
| CVACT03Y | 50 | **Card Cross-Reference** — links Card ↔ Customer ↔ Account |
| CVCUS01Y | 500 | **Customer Record** — name, address, SSN, DOB, FICO score |
| CVTRA01Y | 50 | **Transaction Category Balance** — running balance per category |
| CVTRA02Y | 50 | **Disclosure Group** — interest rates per account group |
| CVTRA03Y | 60 | **Transaction Type** — type code + description |
| CVTRA04Y | 60 | **Transaction Category Type** — type + category code + description |
| CVTRA05Y | 350 | **Transaction Record** — full transaction detail with merchant info |
| CVTRA06Y | 350 | **Daily Transaction Record** — mirrors CVTRA05Y for batch posting |
| CVTRA07Y | — | **Transaction Report Layout** — formatted report headers/detail |
| CVEXPORT | 500 | **Multi-Record Export Layout** — REDEFINES for Customer/Account/Transaction/XREF |
| CUSTREC | 500 | **Customer Record** (alternate copy) — used by CBSTM03A |
| COSTM01 | 350 | **Transaction Altered Layout** — re-keyed by Card+Tran for reporting |
| COCOM01Y | — | **COMMAREA** — inter-program communication area |
| COADM02Y | — | **Admin Menu Options** — 6 options with program names |
| COMEN02Y | — | **Main Menu Options** — 11 options with program names |
| COTTL01Y | — | **Screen Title** — application title strings |
| CSDAT01Y | — | **Date/Time Working Storage** — formatted date/timestamp fields |
| CSMSG01Y | — | **Common Messages** — thank-you and invalid-key messages |
| CSMSG02Y | — | **Abend Data** — abend code, culprit, reason, message |
| CSUSR01Y | 80 | **User Security Record** — ID, name, password, user type |
| CVCRD01Y | — | **Card Work Areas** — AID keys, card/account/customer filter IDs |
| CSLKPCDY | — | **Lookup Code Repository** — phone area codes, state codes, zip prefixes |
| CSSETATY | — | **Set Attribute** — reusable COPY REPLACING for field error highlighting |
| CSSTRPFY | — | **Strip PF-Key** — reusable paragraph for AID key translation |
| CSUTLDPY | — | **Date Utility Parameters** — parameters for CSUTLDTC |
| CSUTLDWY | — | **Date Utility Working Storage** — working storage for CSUTLDTC |
| CODATECN | — | **Date Conversion Record** — input/output date format conversion |
| UNUSED1Y | — | **Unused placeholder** — reserved for future use |

### Extension Copybooks

#### IMS-DB2-MQ Authorization (`app/app-authorization-ims-db2-mq/cpy/`)

| Copybook | Purpose |
|:---------|:--------|
| CCPAURQY | Pending Authorization Request — card, merchant, amount fields |
| CCPAURLY | Pending Authorization Response — card, auth code, approved amount |
| CCPAUERY | Pending Authorization Error Log — date, program, level, codes |
| CIPAUDTY | IMS Segment — Pending Authorization Detail |
| CIPAUSMY | IMS Segment — Pending Authorization Summary |
| IMSFUNCS | IMS Function Code Constants |
| PADFLPCB | IMS PCB — Pending Auth Detail Fullfunction |
| PASFLPCB | IMS PCB — Pending Auth Summary Fullfunction |
| PAUTBPCB | IMS PCB — Pending Auth Table |

#### DB2 Transaction Type (`app/app-transaction-type-db2/cpy/`)

| Copybook | Purpose |
|:---------|:--------|
| CSDB2RPY | DB2 Common Procedures — priming query, error formatting |
| CSDB2RWY | DB2 Working Storage — SQL communication area, status flags |

---

## 5. BMS Maps (22 maps)

### Core Maps (`app/bms/`)

| Map | Mapset | Used By | Screen |
|:----|:-------|:--------|:-------|
| COSGN00 | COSGN00 | COSGN00C | Sign-on screen |
| COMEN01 | COMEN01 | COMEN01C | Main menu |
| COADM01 | COADM01 | COADM01C | Admin menu |
| COACTVW | COACTVW | COACTVWC | Account view |
| COACTUP | COACTUP | COACTUPC | Account update |
| COCRDLI | COCRDLI | COCRDLIC | Credit card list |
| COCRDSL | COCRDSL | COCRDSLC | Credit card detail |
| COCRDUP | COCRDUP | COCRDUPC | Credit card update |
| COTRN00 | COTRN00 | COTRN00C | Transaction list |
| COTRN01 | COTRN01 | COTRN01C | Transaction view |
| COTRN02 | COTRN02 | COTRN02C | Transaction add |
| CORPT00 | CORPT00 | CORPT00C | Transaction reports |
| COBIL00 | COBIL00 | COBIL00C | Bill payment |
| COUSR00 | COUSR00 | COUSR00C | User list |
| COUSR01 | COUSR01 | COUSR01C | User add |
| COUSR02 | COUSR02 | COUSR02C | User update |
| COUSR03 | COUSR03 | COUSR03C | User delete |

### BMS-Generated Copybooks (`app/cpy-bms/`) — 17 files matching each core map

### Extension Maps

| Map | Module | Used By |
|:----|:-------|:--------|
| COPAU00 | IMS-DB2-MQ | COPAUS0C — Pending auth summary |
| COPAU01 | IMS-DB2-MQ | COPAUS1C — Pending auth detail |
| COTRTLI | DB2 Tran Type | COTRTLIC — Tran type list |
| COTRTUP | DB2 Tran Type | COTRTUPC — Tran type update |

---

## 6. JCL Jobs (38 jobs)

### Environment Setup / VSAM Management

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| DUSRSECJ | Define and load user security VSAM file | IEBGENER | Core |
| ACCTFILE | Delete/define/load Account VSAM KSDS | IDCAMS | Core |
| CARDFILE | Delete/define/load Card VSAM KSDS | IDCAMS | Core |
| CUSTFILE | Delete/define/load Customer VSAM KSDS | IDCAMS | Core |
| XREFFILE | Delete/define/load Card-Xref VSAM KSDS | IDCAMS | Core |
| TRANFILE | Copy initial transaction data to VSAM | IDCAMS | Core |
| DISCGRP | Copy disclosure group data to VSAM | IDCAMS | Core |
| TRANCATG | Copy transaction category types to VSAM | IDCAMS | Core |
| TRANTYPE | Copy transaction type data to VSAM | IDCAMS | Core |
| TCATBALF | Copy transaction category balance to VSAM | IDCAMS | Core |
| DEFGDGB | Define GDG bases for backups | IDCAMS | Core |
| DEFGDGD | Define additional GDG bases for DB2 data | IDCAMS | DB2 |
| DEFCUST | Define customer data file (alternate) | IDCAMS | Core |
| ESDSRRDS | Create ESDS and RRDS VSAM files | IDCAMS | Core |
| TRANIDX | Define alternate index on transaction file | IDCAMS | Core |

### Batch Processing

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| POSTTRAN | Core transaction processing — post daily transactions | CBTRN02C | Core |
| INTCALC | Interest calculation on accounts | CBACT04C | Core |
| COMBTRAN | Combine system transactions with daily transactions | SORT | Core |
| CREASTMT | Produce account statements (text + HTML) | CBSTM03A | Core |
| TRANREPT | Transaction detail report — sort, process, print | CBTRN03C | Core |
| PRTCATBL | Print transaction category balance file | Proc/Sort | Core |

### File Operations

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| CLOSEFIL | Close CICS-opened VSAM files | IEFBR14 | Core |
| OPENFIL | Open/make VSAM files available to CICS | IEFBR14 | Core |
| TRANBKP | Backup (REPRO) and delete transaction master | IDCAMS | Core |
| DALYREJS | Define GDG for daily rejection files | IDCAMS | Core |
| REPTFILE | Define GDG for report output files | IDCAMS | Core |
| WAITSTEP | Timer delay job (calls COBSWAIT) | COBSWAIT | Core |

### Data Read/Validation

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| READACCT | Read and display account VSAM records | CBACT01C | Core |
| READCARD | Read and display card VSAM records | CBACT02C | Core |
| READCUST | Read and display customer VSAM records | CBCUS01C | Core |
| READXREF | Read and display cross-reference records | CBACT03C | Core |

### Data Migration

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| CBEXPORT | Export customer data for branch migration | CBEXPORT | Core |
| CBIMPORT | Import customer data from branch migration | CBIMPORT | Core |

### Optional Module Jobs

| Job | Purpose | Runs Program | Module |
|:----|:--------|:-------------|:-------|
| CBADMCDJ | Admin CSD definitions — batch resource setup | DFHCSDUP | Core |
| CBPAUP0J | Purge expired pending authorizations | CBPAUP0C | IMS-DB2-MQ |
| MNTTRDB2 | Maintain transaction type table in DB2 | COBTUPDT | DB2 Tran Type |
| CREADB21 | Create CardDemo DB2 database and load tables | DSNTEP4 | DB2 Tran Type |
| TRANEXTR | Extract latest DB2 data for transaction types | DSNTIAUL | DB2 Tran Type |
| DBPAUTP0 | Pending auth DB2 table maintenance | — | IMS-DB2-MQ |
| LOADPADB | Load pending auth DB2 table | PAUDBLOD | IMS-DB2-MQ |
| UNLDPADB | Unload pending auth DB2 table | PAUDBUNL | IMS-DB2-MQ |
| UNLDGSAM | Unload GSAM data | DBUNLDGS | IMS-DB2-MQ |

### Utility Jobs

| Job | Purpose | Module |
|:----|:--------|:-------|
| FTPJCL | FTP file transfer | Utility |
| TXT2PDF1 | Convert text to PDF | Utility |
| INTRDRJ1 | Internal reader job 1 | Utility |
| INTRDRJ2 | Internal reader job 2 | Utility |

---

## 7. Scheduler Definitions

### Control-M (`app/scheduler/CardDemo.controlm`)

| Folder | Sequence | Jobs | Frequency |
|:-------|:---------|:-----|:----------|
| DAILY-TransactionBackup | 1→2→3→4 | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL | Daily |
| WEEKLY-TransactionTypesDBRefresh | 1→2 | MNTTRDB2 → TRANEXTR | Weekly (Sunday) |

### CA7 (`app/scheduler/CardDemo.ca7`) — Equivalent definitions for CA7 scheduler.

---

## 8. Supporting Artifacts

| Type | Path | Purpose |
|:-----|:-----|:--------|
| CSD Definitions | `app/csd/CARDDEMO.CSD` | CICS resource definitions |
| Macro Library | `app/maclib/ASMWAIT.mac` | Macro for ASM wait utility |
| Macro Library | `app/maclib/COCDATFT.mac` | Macro for date format conversion |
| JCL Procedure | `app/proc/REPROC.prc` | Reusable JCL PROC for report processing |
| JCL Procedure | `app/proc/TRANREPT.prc` | Reusable JCL PROC for transaction reports |
| Sort Control | `app/ctl/REPROCT.ctl` | SORT control statements for reporting |
| EBCDIC Data | `app/data/EBCDIC/` | Sample data files in mainframe-native encoding |
| ASCII Data | `app/data/ASCII/` | Sample data in ASCII format |

---

## 9. Summary Statistics

| Category | Count |
|:---------|------:|
| Online CICS Programs (Core) | 17 |
| Online CICS Programs (Extensions) | 8 |
| Batch COBOL Programs (Core) | 14 |
| Batch COBOL Programs (Extensions) | 5 |
| Assembler Programs | 2 |
| **Total Programs** | **46** |
| Core Copybooks | 30 |
| Extension Copybooks | 11 |
| BMS-Generated Copybooks | 17 |
| **Total Copybooks** | **58** |
| Core BMS Maps | 17 |
| Extension BMS Maps | 4 |
| **Total BMS Maps** | **21** |
| Core JCL Jobs | 30 |
| Extension JCL Jobs | 8 |
| **Total JCL Jobs** | **38** |
| Scheduler Definitions | 2 |
| JCL Procedures | 2 |
| Assembler Macros | 2 |
| **Total Artifacts** | **169** |
