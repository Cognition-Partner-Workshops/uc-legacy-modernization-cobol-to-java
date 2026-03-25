# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo (Credit Card Demo) | **Platform**: IBM z/OS, CICS, VSAM, JCL

---

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL/CICS/VSAM/JCL. It simulates account management, card management, transactions, bill payments, and reporting. The application has two user roles: **Regular** (card operations) and **Admin** (user/transaction type management).

| Category | Count | Location |
|---|---|---|
| COBOL Programs (Core) | 31 | `app/cbl/` |
| COBOL Programs (Optional Modules) | 13 | `app/app-*/cbl/` |
| Copybooks (Core) | 30 | `app/cpy/` |
| Copybooks (Optional Modules) | 11 | `app/app-*/cpy/` |
| BMS Screen Maps (Core) | 17 | `app/bms/` |
| BMS Screen Maps (Optional) | 4 | `app/app-*/bms/` |
| BMS-Generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Batch Jobs (Core) | 38 | `app/jcl/` |
| JCL Batch Jobs (Optional) | 8 | `app/app-*/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| **Total Artifacts** | **173** | |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs (17)

These programs run under CICS and implement the interactive 3270 terminal interface.

| # | Program | Lines | CICS Tran | Function | Business Domain | Classification |
|---|---------|-------|-----------|----------|-----------------|----------------|
| 1 | **COSGN00C.cbl** | 261 | CC00 | Sign-on screen / authentication | Security | Entry Point |
| 2 | **COMEN01C.cbl** | 309 | CM00 | Main menu for regular users | Navigation | Controller |
| 3 | **COADM01C.cbl** | 288 | CA00 | Admin menu for admin users | Navigation | Controller |
| 4 | **COACTVWC.cbl** | 942 | CAVW | Account view (read-only) | Account Mgmt | Read |
| 5 | **COACTUPC.cbl** | 4,237 | CAUP | Account update (with customer data) | Account Mgmt | Read/Write |
| 6 | **COCRDLIC.cbl** | 1,460 | CCLI | Credit card list (browse/filter) | Card Mgmt | Read |
| 7 | **COCRDSLC.cbl** | 888 | CCDL | Credit card detail view | Card Mgmt | Read |
| 8 | **COCRDUPC.cbl** | 1,560 | CCUP | Credit card update | Card Mgmt | Read/Write |
| 9 | **COTRN00C.cbl** | 699 | CT00 | Transaction list (browse/filter) | Transactions | Read |
| 10 | **COTRN01C.cbl** | 330 | CT01 | Transaction detail view | Transactions | Read |
| 11 | **COTRN02C.cbl** | 783 | CT02 | Transaction add (new transaction) | Transactions | Write |
| 12 | **CORPT00C.cbl** | 649 | CR00 | Transaction reports (submit batch) | Reporting | Trigger |
| 13 | **COBIL00C.cbl** | 572 | CB00 | Bill payment (pay balance) | Billing | Read/Write |
| 14 | **COUSR00C.cbl** | 695 | CU00 | User list (admin security) | User Admin | Read |
| 15 | **COUSR01C.cbl** | 299 | CU01 | User add (admin security) | User Admin | Write |
| 16 | **COUSR02C.cbl** | 414 | CU02 | User update (admin security) | User Admin | Read/Write |
| 17 | **COUSR03C.cbl** | 359 | CU03 | User delete (admin security) | User Admin | Delete |

### 1.2 Batch Programs (13)

These programs run as JCL batch jobs for overnight/periodic processing.

| # | Program | Lines | Function | Business Domain | Classification |
|---|---------|-------|----------|-----------------|----------------|
| 18 | **CBACT01C.cbl** | 430 | Read and print account data file | Account Mgmt | Report/Utility |
| 19 | **CBACT02C.cbl** | 178 | Read and print card data file | Card Mgmt | Report/Utility |
| 20 | **CBACT03C.cbl** | 178 | Read and print card cross-reference data | Card Mgmt | Report/Utility |
| 21 | **CBACT04C.cbl** | 652 | Interest calculation on accounts | Financial Calc | Processing |
| 22 | **CBCUS01C.cbl** | 178 | Read and print customer data file | Customer Mgmt | Report/Utility |
| 23 | **CBTRN01C.cbl** | 494 | Post records from daily transaction file (validation) | Transactions | Processing |
| 24 | **CBTRN02C.cbl** | 731 | Post records from daily transaction file (posting) | Transactions | Processing |
| 25 | **CBTRN03C.cbl** | 649 | Print the transaction detail report | Reporting | Report |
| 26 | **CBSTM03A.CBL** | 924 | Print account statements from transaction data | Statements | Report |
| 27 | **CBSTM03B.CBL** | 230 | File processing subroutine for statement report | Statements | Subroutine |
| 28 | **CBEXPORT.cbl** | 582 | Export customer data for branch migration | Data Migration | Export |
| 29 | **CBIMPORT.cbl** | 487 | Import customer data from branch migration export | Data Migration | Import |
| 30 | **COBSWAIT.cbl** | 41 | Wait utility (calls MVSWAIT assembler) | Utility | Utility |

### 1.3 Shared Utility (1)

| # | Program | Lines | Function | Business Domain | Classification |
|---|---------|-------|----------|-----------------|----------------|
| 31 | **CSUTLDTC.cbl** | 157 | Date validation utility (calls CEEDAYS) | Utility | Subroutine |

---

## 2. COBOL Programs -- Optional Modules

### 2.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 32 | **COPAUA0C.cbl** | MQ trigger for authorization | CICS/MQ | Event Handler |
| 33 | **COPAUS0C.cbl** | Authorization summary view | CICS/IMS DB | Read |
| 34 | **COPAUS1C.cbl** | Authorization detail view | CICS/IMS DB | Read |
| 35 | **COPAUS2C.cbl** | Mark fraud transaction to DB2 | CICS/DB2 | Write |
| 36 | **CBPAUP0C.cbl** | Batch purge of authorization records | Batch/DB2 | Processing |
| 37 | **DBUNLDGS.CBL** | Unload GSAM database | Batch/IMS | Utility |
| 38 | **PAUDBLOD.CBL** | Load authorization database | Batch/IMS | Utility |
| 39 | **PAUDBUNL.CBL** | Unload authorization database | Batch/IMS | Utility |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 40 | **COTRTUPC.cbl** | Transaction type add/edit (DB2 CRUD) | CICS/DB2 | Read/Write |
| 41 | **COTRTLIC.cbl** | Transaction type list/delete (DB2) | CICS/DB2 | Read/Delete |
| 42 | **COBTUPDT.cbl** | Batch update of transaction types | Batch/DB2 | Processing |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | Function | Technology | Classification |
|---|---------|----------|------------|----------------|
| 43 | **COACCT01.cbl** | MQ request/response for account inquiry | CICS/MQ/VSAM | Read |
| 44 | **CODATE01.cbl** | MQ request/response for system date | CICS/MQ | Utility |

---

## 3. Copybooks -- Core (`app/cpy/`)

### 3.1 Business Entity Record Layouts (10)

| # | Copybook | Record Length | Entity | Used By |
|---|----------|-------------|--------|---------|
| 1 | **CVACT01Y.cpy** | 300 bytes | Account master record | COACTVWC, COACTUPC, CBACT04C, CBTRN02C, COBIL00C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, COTRN02C |
| 2 | **CVACT02Y.cpy** | 150 bytes | Card master record | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C |
| 3 | **CVACT03Y.cpy** | 50 bytes | Card-Account-Customer cross-reference | COACTVWC, COACTUPC, CBACT03C, COBIL00C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, COTRN02C, CBSTM03A |
| 4 | **CVCUS01Y.cpy** | 500 bytes | Customer master record | COACTVWC, COACTUPC, COCRDSLC, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C |
| 5 | **CSUSR01Y.cpy** | 80 bytes | User security record | COSGN00C, COMEN01C, COADM01C, COUSR00C-03C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 6 | **CVTRA05Y.cpy** | 350 bytes | Transaction record (online) | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C |
| 7 | **CVTRA06Y.cpy** | 350 bytes | Daily transaction record (batch input) | CBTRN01C, CBTRN02C |
| 8 | **CVTRA01Y.cpy** | 50 bytes | Transaction category balance | CBTRN02C |
| 9 | **CVTRA02Y.cpy** | 50 bytes | Disclosure group (interest rates) | CBACT04C |
| 10 | **CVTRA03Y.cpy** | 60 bytes | Transaction type reference | CBTRN03C |

### 3.2 Application Infrastructure Copybooks (10)

| # | Copybook | Function | Classification |
|---|----------|----------|----------------|
| 11 | **COCOM01Y.cpy** | Communication area (COMMAREA) shared by all CICS programs | COMMAREA |
| 12 | **COMEN02Y.cpy** | Regular user main menu option definitions (11 options) | Menu Config |
| 13 | **COADM02Y.cpy** | Admin menu option definitions (6 options) | Menu Config |
| 14 | **COTTL01Y.cpy** | Screen title/header constants | UI Constants |
| 15 | **CSDAT01Y.cpy** | Current date/time working storage | Date/Time |
| 16 | **CSMSG01Y.cpy** | Common application messages | Messages |
| 17 | **CSMSG02Y.cpy** | Abend/error handling data areas | Error Handling |
| 18 | **CVCRD01Y.cpy** | Credit card work areas (AID keys, navigation) | Work Areas |
| 19 | **CVTRA04Y.cpy** | Transaction category type reference (60 bytes) | Reference Data |
| 20 | **CVTRA07Y.cpy** | Transaction report layout structures | Report Layout |

### 3.3 Utility/Validation Copybooks (7)

| # | Copybook | Function | Classification |
|---|----------|----------|----------------|
| 21 | **CSUTLDWY.cpy** | Date edit working storage variables | Date Validation |
| 22 | **CSUTLDPY.cpy** | Date validation procedure division paragraphs | Date Validation |
| 23 | **CSSTRPFY.cpy** | PF-key mapping (AID to CCARD-AID) paragraph | Key Handling |
| 24 | **CSSETATY.cpy** | Screen field attribute setting (error highlighting) | UI Utility |
| 25 | **CSLKPCDY.cpy** | Lookup code repository (phone area codes, state codes, zip) | Validation Data |
| 26 | **CODATECN.cpy** | Date format conversion record layout | Date Conversion |
| 27 | **COSTM01.CPY** | Altered transaction layout for statement reporting | Report Layout |

### 3.4 Special-Purpose Copybooks (3)

| # | Copybook | Function | Classification |
|---|----------|----------|----------------|
| 28 | **CUSTREC.cpy** | Customer record (duplicate of CVCUS01Y for batch) | Entity Duplicate |
| 29 | **CVEXPORT.cpy** | Multi-record export layout (500 bytes, REDEFINES) | Export Layout |
| 30 | **UNUSED1Y.cpy** | Unused/placeholder record layout | Deprecated |

---

## 4. Copybooks -- Optional Modules (11)

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Function |
|---|----------|----------|
| 1 | **CCPAUERY.cpy** | Authorization error response layout |
| 2 | **CCPAURLY.cpy** | Authorization reply layout |
| 3 | **CCPAURQY.cpy** | Authorization request layout |
| 4 | **CIPAUDTY.cpy** | Authorization detail IMS segment |
| 5 | **CIPAUSMY.cpy** | Authorization summary IMS segment |
| 6 | **IMSFUNCS.cpy** | IMS function codes |
| 7 | **PADFLPCB.CPY** | IMS PCB for PADFL database |
| 8 | **PASFLPCB.CPY** | IMS PCB for PASFL database |
| 9 | **PAUTBPCB.CPY** | IMS PCB for PAUTB database |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Function |
|---|----------|----------|
| 10 | **CSDB2RPY.cpy** | DB2 reply/result layout |
| 11 | **CSDB2RWY.cpy** | DB2 read/write working storage |

---

## 5. BMS Screen Maps

### 5.1 Core BMS Maps (`app/bms/`) + Generated Copybooks (`app/cpy-bms/`)

| # | BMS Map | Generated CPY | Screen Function | Associated Program |
|---|---------|---------------|-----------------|-------------------|
| 1 | **COSGN00.bms** | COSGN00.CPY | Sign-on screen | COSGN00C |
| 2 | **COMEN01.bms** | COMEN01.CPY | Main menu | COMEN01C |
| 3 | **COADM01.bms** | COADM01.CPY | Admin menu | COADM01C |
| 4 | **COACTVW.bms** | COACTVW.CPY | Account view | COACTVWC |
| 5 | **COACTUP.bms** | COACTUP.CPY | Account update | COACTUPC |
| 6 | **COCRDLI.bms** | COCRDLI.CPY | Credit card list | COCRDLIC |
| 7 | **COCRDSL.bms** | COCRDSL.CPY | Credit card detail | COCRDSLC |
| 8 | **COCRDUP.bms** | COCRDUP.CPY | Credit card update | COCRDUPC |
| 9 | **COTRN00.bms** | COTRN00.CPY | Transaction list | COTRN00C |
| 10 | **COTRN01.bms** | COTRN01.CPY | Transaction view | COTRN01C |
| 11 | **COTRN02.bms** | COTRN02.CPY | Transaction add | COTRN02C |
| 12 | **CORPT00.bms** | CORPT00.CPY | Transaction reports | CORPT00C |
| 13 | **COBIL00.bms** | COBIL00.CPY | Bill payment | COBIL00C |
| 14 | **COUSR00.bms** | COUSR00.CPY | User list | COUSR00C |
| 15 | **COUSR01.bms** | COUSR01.CPY | User add | COUSR01C |
| 16 | **COUSR02.bms** | COUSR02.CPY | User update | COUSR02C |
| 17 | **COUSR03.bms** | COUSR03.CPY | User delete | COUSR03C |

### 5.2 Optional Module BMS Maps

| # | BMS Map | Module | Screen Function |
|---|---------|--------|-----------------|
| 18 | **COPAU00.bms** | Authorization | Authorization summary screen |
| 19 | **COPAU01.bms** | Authorization | Authorization detail screen |
| 20 | **COTRTLI.bms** | Transaction Type DB2 | Transaction type list screen |
| 21 | **COTRTUP.bms** | Transaction Type DB2 | Transaction type update screen |

---

## 6. JCL Batch Jobs

### 6.1 Core JCL Jobs (`app/jcl/`) -- 38 Jobs

#### Data Refresh Jobs (8)

| # | JCL Job | Function | Files Affected |
|---|---------|----------|---------------|
| 1 | **ACCTFILE.jcl** | Refresh account master VSAM file | ACCTDAT |
| 2 | **CARDFILE.jcl** | Refresh card master VSAM file | CARDDAT |
| 3 | **CUSTFILE.jcl** | Refresh customer master VSAM file | CUSTDAT |
| 4 | **XREFFILE.jcl** | Load card-account-customer cross-reference | CARDXREF |
| 5 | **TRANFILE.jcl** | Load transaction master VSAM file | TRANSACT |
| 6 | **DUSRSECJ.jcl** | Load user security VSAM file | USRSEC |
| 7 | **REPTFILE.jcl** | Refresh report data file | REPTDATA |
| 8 | **TRANTYPE.jcl** | Load transaction type reference file | TRANTYPE |

#### Batch Processing Jobs (6)

| # | JCL Job | Function | Program Executed |
|---|---------|----------|-----------------|
| 9 | **POSTTRAN.jcl** | Core transaction posting | CBTRN01C, CBTRN02C |
| 10 | **INTCALC.jcl** | Interest calculations | CBACT04C |
| 11 | **COMBTRAN.jcl** | Combine daily transactions into master | Utility |
| 12 | **CREASTMT.JCL** | Produce account statements | CBSTM03A |
| 13 | **TRANREPT.jcl** | Generate transaction detail reports | CBTRN03C |
| 14 | **TRANBKP.jcl** | Backup transaction data | Utility |

#### VSAM Maintenance Jobs (6)

| # | JCL Job | Function |
|---|---------|----------|
| 15 | **CLOSEFIL.jcl** | Close CICS files for batch processing |
| 16 | **OPENFIL.jcl** | Open CICS files after batch processing |
| 17 | **TRANIDX.jcl** | Define/build alternate index on transactions |
| 18 | **DEFGDGB.jcl** | Define GDG base for backups |
| 19 | **DEFGDGD.jcl** | Define GDG data for backups |
| 20 | **DEFCUST.jcl** | Define customer VSAM cluster |

#### Data Export/Import Jobs (2)

| # | JCL Job | Function | Program Executed |
|---|---------|----------|-----------------|
| 21 | **CBEXPORT.jcl** | Export all data for branch migration | CBEXPORT |
| 22 | **CBIMPORT.jcl** | Import data from branch migration export | CBIMPORT |

#### Utility/Print Jobs (8)

| # | JCL Job | Function |
|---|---------|----------|
| 23 | **READACCT.jcl** | Read and print account file | 
| 24 | **READCARD.jcl** | Read and print card file |
| 25 | **READCUST.jcl** | Read and print customer file |
| 26 | **READXREF.jcl** | Read and print cross-reference file |
| 27 | **PRTCATBL.jcl** | Print category balance file |
| 28 | **TCATBALF.jcl** | Transaction category balance file operations |
| 29 | **TXT2PDF1.JCL** | Convert text reports to PDF |
| 30 | **WAITSTEP.jcl** | Wait step (uses COBSWAIT) |

#### Infrastructure/Special Jobs (8)

| # | JCL Job | Function |
|---|---------|----------|
| 31 | **DALYREJS.jcl** | Process daily rejects |
| 32 | **DISCGRP.jcl** | Disclosure group operations |
| 33 | **ESDSRRDS.jcl** | ESDS/RRDS VSAM operations |
| 34 | **TRANCATG.jcl** | Transaction catalog operations |
| 35 | **CBADMCDJ.jcl** | Admin card operations batch |
| 36 | **FTPJCL.JCL** | FTP-based file transfer |
| 37 | **INTRDRJ1.JCL** | Internal reader job 1 |
| 38 | **INTRDRJ2.JCL** | Internal reader job 2 |

### 6.2 Optional Module JCL Jobs (8)

#### Authorization Module (`app/app-authorization-ims-db2-mq/jcl/`)

| # | JCL Job | Function |
|---|---------|----------|
| 39 | **CBPAUP0J.jcl** | Purge authorization records (batch) |
| 40 | **DBPAUTP0.jcl** | Authorization database operations |
| 41 | **LOADPADB.JCL** | Load authorization IMS database |
| 42 | **UNLDGSAM.JCL** | Unload GSAM authorization data |
| 43 | **UNLDPADB.JCL** | Unload authorization IMS database |

#### Transaction Type DB2 Module (`app/app-transaction-type-db2/jcl/`)

| # | JCL Job | Function |
|---|---------|----------|
| 44 | **CREADB21.jcl** | Create DB2 tables for transaction types |
| 45 | **MNTTRDB2.jcl** | Maintain transaction type DB2 data |
| 46 | **TRANEXTR.jcl** | Extract transaction type data from DB2 |

---

## 7. Supporting Artifacts

### 7.1 Assembler Programs (`app/asm/`)

| Program | Function |
|---------|----------|
| **COBDATFT.asm** | Date formatting assembler routine (called by CBACT01C) |
| **MVSWAIT.asm** | Wait/delay assembler routine (called by COBSWAIT) |

### 7.2 JCL Procedures (`app/proc/`)

| Procedure | Function |
|-----------|----------|
| **REPROC.prc** | Reusable reporting procedure |
| **TRANREPT.prc** | Transaction report procedure |

### 7.3 Scheduler Configurations (`app/scheduler/`)

| File | Function |
|------|----------|
| **CardDemo.ca7** | CA-7 job scheduling definitions |
| **CardDemo.controlm** | Control-M job scheduling definitions |

### 7.4 CICS Resource Definitions (`app/csd/`)

| File | Function |
|------|----------|
| **CARDDEMO.CSD** | CICS System Definition file (program/transaction/file definitions) |

---

## 8. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C, COACTVWC |
| `CB*` | Batch program | CBTRN02C, CBACT04C |
| `CV*` | Copybook - data view/entity | CVACT01Y, CVTRA05Y |
| `CS*` | Copybook - shared/utility | CSDAT01Y, CSMSG01Y |
| `CO*` (cpy) | Copybook - communication/menu | COCOM01Y, COMEN02Y |
| `*Y` suffix | Copybook (general convention) | COTTL01Y |
| `*C` suffix | COBOL program | COSGN00C |

---

## 9. Batch Processing Cycle

The standard nightly batch cycle executes in this order:

```
1. CLOSEFIL    -- Close CICS files for batch access
2. ACCTFILE    -- Refresh account master
3. CARDFILE    -- Refresh card master  
4. CUSTFILE    -- Refresh customer master
5. XREFFILE    -- Load cross-reference
6. TRANFILE    -- Load transaction master
7. POSTTRAN    -- Core transaction posting (CBTRN01C + CBTRN02C)
8. INTCALC     -- Interest calculations (CBACT04C)
9. TRANBKP     -- Backup transactions
10. COMBTRAN   -- Combine daily transactions
11. CREASTMT   -- Produce statements (CBSTM03A + CBSTM03B)
12. TRANIDX    -- Rebuild alternate indexes
13. OPENFIL    -- Reopen CICS files
```
