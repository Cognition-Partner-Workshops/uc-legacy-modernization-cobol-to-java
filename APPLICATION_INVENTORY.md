# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM z/OS Mainframe (CICS/VSAM/JCL)

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL, CICS, VSAM, and JCL. It simulates account management, card management, transactions, bill payments, and reporting. The application consists of **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, plus **13 optional module programs** across three extension modules.

| Asset Type | Count | Location |
|---|---|---|
| COBOL Programs (Core) | 31 | `app/cbl/` |
| Copybooks (Core) | 30 | `app/cpy/` |
| BMS Screen Maps | 17 | `app/bms/` |
| BMS-Generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Batch Jobs | 38 | `app/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| Assembler Macros | 2 | `app/maclib/` |
| Scheduler Configs | 2 | `app/scheduler/` |
| Optional Module Programs | 13 | `app/app-*/cbl/` |
| Optional Module Copybooks | 11 | `app/app-*/cpy/` |
| Optional Module BMS Maps | 4 | `app/app-*/bms/` |
| Optional Module JCL Jobs | 8 | `app/app-*/jcl/` |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs (17)

These programs run under CICS and handle interactive 3270 terminal sessions. Naming convention: `CO*` prefix.

| # | Program | Lines | CICS Tran | Function | Domain | BMS Map |
|---|---------|-------|-----------|----------|--------|---------|
| 1 | **COSGN00C** | 260 | CC00 | Signon / Authentication | Security | COSGN00 |
| 2 | **COMEN01C** | 308 | CM00 | Main Menu (Regular Users) | Navigation | COMEN01 |
| 3 | **COADM01C** | 288 | CA00 | Admin Menu (Admin Users) | Navigation | COADM01 |
| 4 | **COACTVWC** | 941 | -- | Account View (read-only) | Account Mgmt | COACTVW |
| 5 | **COACTUPC** | 4,236 | -- | Account Update | Account Mgmt | COACTUP |
| 6 | **COCRDLIC** | 1,459 | -- | Credit Card List | Card Mgmt | COCRDLI |
| 7 | **COCRDSLC** | 887 | -- | Credit Card Detail View | Card Mgmt | COCRDSL |
| 8 | **COCRDUPC** | 1,560 | -- | Credit Card Update | Card Mgmt | COCRDUP |
| 9 | **COTRN00C** | 699 | -- | Transaction List | Transaction Mgmt | COTRN00 |
| 10 | **COTRN01C** | 330 | -- | Transaction Detail View | Transaction Mgmt | COTRN01 |
| 11 | **COTRN02C** | 783 | -- | Transaction Add (new) | Transaction Mgmt | COTRN02 |
| 12 | **CORPT00C** | 649 | -- | Transaction Reports (submit batch) | Reporting | CORPT00 |
| 13 | **COBIL00C** | 572 | -- | Bill Payment | Billing | COBIL00 |
| 14 | **COUSR00C** | 695 | -- | User List (Admin) | User Admin | COUSR00 |
| 15 | **COUSR01C** | 299 | -- | User Add (Admin) | User Admin | COUSR01 |
| 16 | **COUSR02C** | 414 | -- | User Update (Admin) | User Admin | COUSR02 |
| 17 | **COUSR03C** | 359 | -- | User Delete (Admin) | User Admin | COUSR03 |

### 1.2 Batch Programs (12)

These programs run as batch jobs invoked via JCL. Naming convention: `CB*` prefix.

| # | Program | Lines | Function | Domain | Called By JCL |
|---|---------|-------|----------|--------|---------------|
| 1 | **CBACT01C** | 430 | Read account file, write to multiple outputs | Account Mgmt | READACCT |
| 2 | **CBACT02C** | 178 | Read and print card data | Card Mgmt | READCARD |
| 3 | **CBACT03C** | 178 | Read and print cross-reference data | Card Mgmt | READXREF |
| 4 | **CBACT04C** | 652 | Interest calculation engine | Financial | INTCALC |
| 5 | **CBCUS01C** | 178 | Read and print customer data | Customer Mgmt | READCUST |
| 6 | **CBTRN01C** | 494 | Combine daily transactions | Transaction Mgmt | -- |
| 7 | **CBTRN02C** | 731 | Post daily transaction records | Transaction Mgmt | POSTTRAN |
| 8 | **CBTRN03C** | 649 | Print transaction detail report | Reporting | TRANREPT |
| 9 | **CBSTM03A** | 924 | Generate account statements (text + HTML) | Reporting | CREASTMT |
| 10 | **CBSTM03B** | 230 | File I/O subroutine for statement generation | Reporting | (called by CBSTM03A) |
| 11 | **CBEXPORT** | 582 | Export customer data for branch migration | Data Migration | CBEXPORT |
| 12 | **CBIMPORT** | 487 | Import customer data from branch migration | Data Migration | CBIMPORT |

### 1.3 Utility Programs (2)

| # | Program | Lines | Function | Domain |
|---|---------|-------|----------|--------|
| 1 | **CSUTLDTC** | 157 | Date validation utility (CEEDAYS API) | Utility |
| 2 | **COBSWAIT** | 41 | Wait timer utility (PARM in centiseconds) | Utility |

---

## 2. Optional Module Programs

### 2.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | **COPAUA0C** | 1,026 | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| 2 | **COPAUS0C** | 1,032 | CICS/IMS/BMS | Authorization message summary view |
| 3 | **COPAUS1C** | 604 | CICS/IMS/BMS | Authorization message detail view |
| 4 | **COPAUS2C** | 244 | CICS/IMS/DB2 | Mark authorization message as fraud |
| 5 | **CBPAUP0C** | 386 | Batch/IMS | Purge expired pending authorization messages |
| 6 | **DBUNLDGS** | 366 | Batch/IMS | Unload IMS GSAM database |
| 7 | **PAUDBLOD** | 369 | Batch/IMS | Load authorization IMS database |
| 8 | **PAUDBUNL** | 317 | Batch/IMS | Unload authorization IMS database |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | **COTRTLIC** | 2,098 | CICS/DB2 | List transaction types (update/delete) |
| 2 | **COTRTUPC** | 1,702 | CICS/DB2 | Add/edit transaction type |
| 3 | **COBTUPDT** | 237 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | **COACCT01** | 620 | CICS/MQ | MQ request/response for account inquiry |
| 2 | **CODATE01** | 524 | CICS/MQ | MQ request/response for system date |

---

## 3. Copybooks -- Core (`app/cpy/`)

### 3.1 Data Record Layouts (CV* -- VSAM file structures)

| # | Copybook | Record Len | Description | Business Entity |
|---|----------|------------|-------------|-----------------|
| 1 | **CVACT01Y** | 300 | Account master record | Account |
| 2 | **CVACT02Y** | 150 | Card data record | Credit Card |
| 3 | **CVACT03Y** | 50 | Card-to-account cross-reference | Card-Account Link |
| 4 | **CVCRD01Y** | -- | Card detail record (online screens) | Credit Card |
| 5 | **CVCUS01Y** | 500 | Customer master record | Customer |
| 6 | **CVTRA01Y** | 50 | Transaction category balance | Transaction Balance |
| 7 | **CVTRA02Y** | 50 | Disclosure group record | Disclosure |
| 8 | **CVTRA03Y** | 60 | Transaction type record | Transaction Type |
| 9 | **CVTRA04Y** | 60 | Transaction category type record | Transaction Category |
| 10 | **CVTRA05Y** | 350 | Transaction record (online) | Transaction |
| 11 | **CVTRA06Y** | 350 | Daily transaction record | Daily Transaction |
| 12 | **CVTRA07Y** | -- | Transaction report data structure | Report Layout |
| 13 | **CVEXPORT** | -- | Export/import record layout | Migration Data |

### 3.2 Common Area & UI Copybooks (CO*)

| # | Copybook | Description |
|---|----------|-------------|
| 1 | **COCOM01Y** | Common communication area (COMMAREA) |
| 2 | **COADM02Y** | Admin menu option definitions |
| 3 | **COMEN02Y** | Main menu option definitions |
| 4 | **COTTL01Y** | Title/header line definitions |
| 5 | **COSTM01** | Transaction layout for statement reporting |
| 6 | **CUSTREC** | Customer record for statement generation |

### 3.3 System & Utility Copybooks (CS*)

| # | Copybook | Description |
|---|----------|-------------|
| 1 | **CSUSR01Y** | User security record (signon) |
| 2 | **CSDAT01Y** | Date-related working storage |
| 3 | **CSMSG01Y** | Message area (single message) |
| 4 | **CSMSG02Y** | Message area (multiple messages) |
| 5 | **CSLKPCDY** | Look-up code definitions |
| 6 | **CSSETATY** | Set attribute utility |
| 7 | **CSSTRPFY** | String prefix utility |
| 8 | **CSUTLDPY** | Date utility parameters |
| 9 | **CSUTLDWY** | Date utility working storage |
| 10 | **CODATECN** | Date conversion routines |

### 3.4 Other

| # | Copybook | Description |
|---|----------|-------------|
| 1 | **UNUSED1Y** | Placeholder / unused data structure |

### 3.5 Optional Module Copybooks

**Authorization Module** (`app/app-authorization-ims-db2-mq/cpy/`):
CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB

**Transaction Type DB2 Module** (`app/app-transaction-type-db2/cpy/`):
CSDB2RPY, CSDB2RWY

---

## 4. BMS Screen Maps (`app/bms/`)

| # | Map | Map Set | Screen Title | Associated Program |
|---|-----|---------|--------------|-------------------|
| 1 | **COSGN00** | COSGN0A | Signon | COSGN00C |
| 2 | **COMEN01** | COMEN1A | Main Menu | COMEN01C |
| 3 | **COADM01** | COADM1A | Admin Menu | COADM01C |
| 4 | **COACTVW** | COACT1A | Account View | COACTVWC |
| 5 | **COACTUP** | COACT2A | Account Update | COACTUPC |
| 6 | **COCRDLI** | COCRD1A | Card List | COCRDLIC |
| 7 | **COCRDSL** | COCRD2A | Card Detail | COCRDSLC |
| 8 | **COCRDUP** | COCRD3A | Card Update | COCRDUPC |
| 9 | **COTRN00** | COTRN0A | Transaction List | COTRN00C |
| 10 | **COTRN01** | COTRN1A | Transaction View | COTRN01C |
| 11 | **COTRN02** | COTRN2A | Transaction Add | COTRN02C |
| 12 | **CORPT00** | CORPT0A | Transaction Reports | CORPT00C |
| 13 | **COBIL00** | COBIL0A | Bill Payment | COBIL00C |
| 14 | **COUSR00** | COUSR0A | User List | COUSR00C |
| 15 | **COUSR01** | COUSR1A | User Add | COUSR01C |
| 16 | **COUSR02** | COUSR2A | User Update | COUSR02C |
| 17 | **COUSR03** | COUSR3A | User Delete | COUSR03C |

**Optional Module BMS Maps:**
- `app/app-authorization-ims-db2-mq/bms/`: COPAU00, COPAU01
- `app/app-transaction-type-db2/bms/`: COTRTLI, COTRTUP

---

## 5. JCL Batch Jobs (`app/jcl/`)

### 5.1 Data Refresh Jobs (VSAM file load/reload)

| # | Job | Function | Executes | Key Datasets |
|---|-----|----------|----------|--------------|
| 1 | **ACCTFILE** | Refresh account master VSAM | IDCAMS | ACCTDATA.PS -> ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE** | Refresh card master VSAM | IDCAMS, SDSF | CARDDATA.PS -> CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE** | Refresh customer master VSAM | IDCAMS, SDSF | CUSTDATA.PS -> CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE** | Load card cross-reference + alt index | IDCAMS | CARDXREF.PS -> CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE** | Load transaction master VSAM | IDCAMS, SDSF | DALYTRAN.PS.INIT -> TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ** | Load user security VSAM | IDCAMS, IEBGENER, IEFBR14 | USRSEC.PS -> USRSEC.VSAM.KSDS |
| 7 | **DISCGRP** | Load disclosure group VSAM | IDCAMS | DISCGRP.PS -> DISCGRP.VSAM.KSDS |
| 8 | **TCATBALF** | Load transaction category balance | IDCAMS | TCATBALF.PS -> TCATBALF.VSAM.KSDS |
| 9 | **TRANCATG** | Load transaction category VSAM | IDCAMS | TRANCATG.PS -> TRANCATG.VSAM.KSDS |
| 10 | **TRANTYPE** | Load transaction type VSAM | IDCAMS | TRANTYPE.PS -> TRANTYPE.VSAM.KSDS |

### 5.2 Batch Processing Jobs

| # | Job | Function | Executes | Key Datasets |
|---|-----|----------|----------|--------------|
| 1 | **POSTTRAN** | Post daily transactions | CBTRN02C | DALYTRAN, TRANSACT, ACCTDATA, CARDXREF |
| 2 | **INTCALC** | Calculate interest on accounts | CBACT04C | ACCTDATA, CARDXREF, DISCGRP, TCATBALF |
| 3 | **TRANBKP** | Backup transaction file | IDCAMS | TRANSACT.VSAM -> TRANSACT.BKUP |
| 4 | **COMBTRAN** | Combine transactions | SORT, IDCAMS | TRANSACT.BKUP + SYSTRAN -> COMBINED |
| 5 | **CREASTMT** | Create account statements | CBSTM03A, SORT, IDCAMS | TRANSACT, XREF, ACCT, CUST -> STATEMNT |
| 6 | **TRANREPT** | Generate transaction report | CBTRN03C, SORT | TRANSACT, XREF, TYPES, CATG -> TRANREPT |

### 5.3 Data Read/Print Jobs

| # | Job | Function | Executes |
|---|-----|----------|----------|
| 1 | **READACCT** | Read and print account data | CBACT01C |
| 2 | **READCARD** | Read and print card data | CBACT02C |
| 3 | **READCUST** | Read and print customer data | CBCUS01C |
| 4 | **READXREF** | Read and print cross-reference data | CBACT03C |
| 5 | **PRTCATBL** | Print category balance report | SORT |

### 5.4 CICS File Control Jobs

| # | Job | Function |
|---|-----|----------|
| 1 | **CLOSEFIL** | Close CICS files for batch processing |
| 2 | **OPENFIL** | Open CICS files after batch processing |

### 5.5 Export/Import Jobs

| # | Job | Function | Executes |
|---|-----|----------|----------|
| 1 | **CBEXPORT** | Export all data for branch migration | CBEXPORT |
| 2 | **CBIMPORT** | Import data from branch migration | CBIMPORT |

### 5.6 VSAM Definition & Utility Jobs

| # | Job | Function |
|---|-----|----------|
| 1 | **DEFGDGB** | Define GDG base clusters |
| 2 | **DEFGDGD** | Define GDG datasets with backup |
| 3 | **DEFCUST** | Define customer VSAM cluster |
| 4 | **DALYREJS** | Define daily rejects VSAM |
| 5 | **ESDSRRDS** | Define ESDS/RRDS VSAM files |
| 6 | **REPTFILE** | Define report file |
| 7 | **TRANIDX** | Define/build transaction alternate indexes |
| 8 | **WAITSTEP** | Execute wait utility (COBSWAIT) |

### 5.7 Miscellaneous/Infrastructure Jobs

| # | Job | Function |
|---|-----|----------|
| 1 | **CBADMCDJ** | CICS CSD administration (resource definitions) |
| 2 | **FTPJCL** | FTP file transfer to/from mainframe |
| 3 | **INTRDRJ1** | Internal reader -- trigger INTRDRJ2 |
| 4 | **INTRDRJ2** | Internal reader -- secondary processing |
| 5 | **TXT2PDF1** | Convert text statements to PDF |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Function |
|---|---------|----------|
| 1 | **COBDATFT** | Date format conversion (called by CBACT01C) |
| 2 | **MVSWAIT** | MVS wait service (called by COBSWAIT) |

## 7. JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | **REPROC** | Reusable REPRO (copy) procedure |
| 2 | **TRANREPT** | Transaction report procedure |

## 8. Scheduler Configurations (`app/scheduler/`)

| # | Config | Function |
|---|--------|----------|
| 1 | **CardDemo.ca7** | CA-7 job scheduler definitions |
| 2 | **CardDemo.controlm** | Control-M job scheduler definitions |

---

## 9. Classification Summary

### By Domain

| Domain | Online Programs | Batch Programs | Total |
|--------|----------------|----------------|-------|
| Security / Auth | 1 (COSGN00C) | -- | 1 |
| Navigation | 2 (COMEN01C, COADM01C) | -- | 2 |
| Account Mgmt | 2 (COACTVWC, COACTUPC) | 1 (CBACT01C) | 3 |
| Card Mgmt | 3 (COCRDLIC, COCRDSLC, COCRDUPC) | 2 (CBACT02C, CBACT03C) | 5 |
| Customer Mgmt | -- | 1 (CBCUS01C) | 1 |
| Transaction Mgmt | 3 (COTRN00C, COTRN01C, COTRN02C) | 3 (CBTRN01C, CBTRN02C, CBTRN03C) | 6 |
| Billing | 1 (COBIL00C) | -- | 1 |
| Financial | -- | 1 (CBACT04C) | 1 |
| Reporting | 1 (CORPT00C) | 2 (CBSTM03A, CBSTM03B) | 3 |
| User Admin | 4 (COUSR00C-03C) | -- | 4 |
| Data Migration | -- | 2 (CBEXPORT, CBIMPORT) | 2 |
| Utility | -- | 2 (CSUTLDTC, COBSWAIT) | 2 |

### By Technology Stack

| Technology | Programs |
|---|---|
| COBOL + CICS + BMS + VSAM | 17 online programs |
| COBOL + VSAM (batch) | 12 batch programs |
| COBOL + IMS + DB2 + MQ | 8 authorization module programs |
| COBOL + DB2 (CICS) | 3 transaction type module programs |
| COBOL + VSAM + MQ | 2 VSAM-MQ module programs |
| Assembler | 2 utility programs |
| JCL + IDCAMS | 38 batch jobs |

### Batch Processing Cycle

The recommended nightly batch cycle order is:

```
1. CLOSEFIL    -- Close CICS files
2. ACCTFILE    -- Refresh account master
3. CARDFILE    -- Refresh card master
4. CUSTFILE    -- Refresh customer master
5. XREFFILE    -- Load cross-reference
6. POSTTRAN    -- Post daily transactions (CBTRN02C)
7. INTCALC     -- Calculate interest (CBACT04C)
8. TRANBKP     -- Backup transactions
9. COMBTRAN    -- Combine transactions
10. CREASTMT   -- Create statements (CBSTM03A)
11. TRANIDX    -- Rebuild alternate indexes
12. OPENFIL    -- Reopen CICS files
```
