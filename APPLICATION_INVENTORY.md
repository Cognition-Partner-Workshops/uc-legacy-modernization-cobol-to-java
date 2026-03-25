# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Demo) | **Platform:** IBM Mainframe (z/OS) / CICS / VSAM / JCL

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **31 COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **17 BMS-generated copybooks**, **38 JCL batch jobs**, **2 assembler programs**, **2 JCL procedures**, and **3 optional extension modules** (13 additional programs). The application supports two user roles (Regular and Admin) and covers account management, card management, transactions, bill payments, reporting, and user security administration.

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (17 programs)

| # | Program | Lines | CICS Trans | Function | Domain | User Role |
|---|---------|-------|-----------|----------|--------|-----------|
| 1 | COSGN00C.cbl | 261 | CC00 | Sign-on / Authentication | Security | All |
| 2 | COMEN01C.cbl | 309 | CM00 | Main Menu (Regular User) | Navigation | Regular |
| 3 | COADM01C.cbl | 288 | CA00 | Admin Menu | Navigation | Admin |
| 4 | COACTVWC.cbl | 942 | CAVW | Account View | Account Mgmt | Regular |
| 5 | COACTUPC.cbl | 4,237 | CAUP | Account Update | Account Mgmt | Regular |
| 6 | COCRDLIC.cbl | 1,460 | CCLI | Credit Card List | Card Mgmt | Regular |
| 7 | COCRDSLC.cbl | 888 | CCDL | Credit Card View (Detail) | Card Mgmt | Regular |
| 8 | COCRDUPC.cbl | 1,560 | CCUP | Credit Card Update | Card Mgmt | Regular |
| 9 | COTRN00C.cbl | 699 | CT00 | Transaction List | Transaction Mgmt | Regular |
| 10 | COTRN01C.cbl | 330 | CT01 | Transaction View | Transaction Mgmt | Regular |
| 11 | COTRN02C.cbl | 783 | CT02 | Transaction Add | Transaction Mgmt | Regular |
| 12 | CORPT00C.cbl | 649 | CR00 | Transaction Report Request | Reporting | Regular |
| 13 | COBIL00C.cbl | 572 | CB00 | Bill Payment | Billing | Regular |
| 14 | COUSR00C.cbl | 695 | CU00 | User List (Security) | User Admin | Admin |
| 15 | COUSR01C.cbl | 299 | CU01 | User Add (Security) | User Admin | Admin |
| 16 | COUSR02C.cbl | 414 | CU02 | User Update (Security) | User Admin | Admin |
| 17 | COUSR03C.cbl | 359 | CU03 | User Delete (Security) | User Admin | Admin |

### 1.2 Batch Programs (13 programs)

| # | Program | Lines | Function | Domain | I/O Pattern |
|---|---------|-------|----------|--------|-------------|
| 1 | CBACT01C.cbl | 430 | Account File Refresh (Load/Reload) | Account Mgmt | VSAM Read/Write |
| 2 | CBACT02C.cbl | 178 | Card File Refresh (Load/Reload) | Card Mgmt | VSAM Read/Write |
| 3 | CBACT03C.cbl | 178 | Card Cross-Reference Refresh | Card Mgmt | VSAM Read/Write |
| 4 | CBACT04C.cbl | 652 | Interest Calculation | Financial | VSAM Read/Update |
| 5 | CBCUS01C.cbl | 178 | Customer File Refresh | Customer Mgmt | VSAM Read/Write |
| 6 | CBTRN01C.cbl | 494 | Daily Transaction Validation | Transaction Mgmt | Sequential + VSAM |
| 7 | CBTRN02C.cbl | 731 | Transaction Posting | Transaction Mgmt | Sequential + VSAM |
| 8 | CBTRN03C.cbl | 649 | Transaction Report Generation | Reporting | VSAM Read + Print |
| 9 | CBSTM03A.CBL | 924 | Statement Generation (Driver) | Reporting | VSAM + Sequential |
| 10 | CBSTM03B.CBL | 230 | Statement Generation (Print Sub) | Reporting | Print Output |
| 11 | CBEXPORT.cbl | 582 | Data Export (All Files) | Data Migration | VSAM Read + Sequential Write |
| 12 | CBIMPORT.cbl | 487 | Data Import (All Files) | Data Migration | Sequential Read + VSAM Write |
| 13 | COBSWAIT.cbl | 41 | Wait/Delay Utility | Utility | N/A (Calls ASM) |

### 1.3 Shared Utility Program (1 program)

| # | Program | Lines | Function | Domain |
|---|---------|-------|----------|--------|
| 1 | CSUTLDTC.cbl | 157 | Date Validation Utility (calls CEEDAYS) | Utility |

**Total Core Programs: 31** | **Total Lines of COBOL: ~20,650**

---

## 2. Copybooks (`app/cpy/`) -- 30 Files

### 2.1 Data Structure Copybooks (Business Entities)

| # | Copybook | Record Len | Entity | Description |
|---|----------|-----------|--------|-------------|
| 1 | CVACT01Y.cpy | 300 bytes | Account | Account master record |
| 2 | CVACT02Y.cpy | 150 bytes | Card | Credit card record |
| 3 | CVACT03Y.cpy | 50 bytes | Card Cross-Ref | Card-to-customer-to-account cross-reference |
| 4 | CVCUS01Y.cpy | 500 bytes | Customer | Customer master record |
| 5 | CUSTREC.cpy | 500 bytes | Customer | Alternate customer record layout (DOB format differs) |
| 6 | CSUSR01Y.cpy | 80 bytes | User Security | User authentication record |
| 7 | CVTRA05Y.cpy | 350 bytes | Transaction | Online transaction record |
| 8 | CVTRA06Y.cpy | 350 bytes | Daily Transaction | Daily transaction input record |
| 9 | CVTRA01Y.cpy | 50 bytes | Tran Category Balance | Transaction category balance summary |
| 10 | CVTRA02Y.cpy | 50 bytes | Disclosure Group | Interest rate disclosure group |
| 11 | CVTRA03Y.cpy | 60 bytes | Transaction Type | Transaction type master |
| 12 | CVTRA04Y.cpy | 60 bytes | Transaction Category | Transaction category master |
| 13 | CVTRA07Y.cpy | N/A | Report Layout | Transaction report print layout |
| 14 | COSTM01.CPY | 350 bytes | Statement Transaction | Transaction layout keyed by card+tran-id |
| 15 | CVEXPORT.cpy | 500 bytes | Export Record | Multi-type export record with REDEFINES |
| 16 | UNUSED1Y.cpy | 80 bytes | (Unused) | Placeholder/unused data structure |

### 2.2 Application Framework Copybooks

| # | Copybook | Purpose |
|---|----------|---------|
| 17 | COCOM01Y.cpy | COMMAREA -- inter-program communication area |
| 18 | COMEN02Y.cpy | Main menu option definitions (11 options) |
| 19 | COADM02Y.cpy | Admin menu option definitions (6 options) |
| 20 | COTTL01Y.cpy | Screen title constants |
| 21 | CSDAT01Y.cpy | Current date/time working storage |
| 22 | CSMSG01Y.cpy | Common user messages (thank you, invalid key) |
| 23 | CSMSG02Y.cpy | Abend handling data area |
| 24 | CVCRD01Y.cpy | Credit card work area (AID keys, navigation, errors) |
| 25 | CODATECN.cpy | Date conversion input/output area (for COBDATFT ASM) |
| 26 | CSUTLDWY.cpy | Date validation working storage (CCYYMMDD editing) |
| 27 | CSUTLDPY.cpy | Date validation procedure division (reusable paragraphs) |
| 28 | CSSETATY.cpy | BMS screen attribute setting (error highlighting) |
| 29 | CSSTRPFY.cpy | PF-key mapping procedure (AID-to-CCARD-AID) |
| 30 | CSLKPCDY.cpy | Lookup code repository (phone area codes, state codes, zip prefixes) |

---

## 3. BMS Screen Maps (`app/bms/`) -- 17 Maps

| # | Map | Mapset | Associated Program | Screen Function |
|---|-----|--------|-------------------|-----------------|
| 1 | COSGN00.bms | COSGN00 | COSGN00C | Sign-on Screen |
| 2 | COMEN01.bms | COMEN01 | COMEN01C | Main Menu |
| 3 | COADM01.bms | COADM01 | COADM01C | Admin Menu |
| 4 | COACTVW.bms | COACTVW | COACTVWC | Account View |
| 5 | COACTUP.bms | COACTUP | COACTUPC | Account Update |
| 6 | COCRDLI.bms | COCRDLI | COCRDLIC | Credit Card List |
| 7 | COCRDSL.bms | COCRDSL | COCRDSLC | Credit Card Detail |
| 8 | COCRDUP.bms | COCRDUP | COCRDUPC | Credit Card Update |
| 9 | COTRN00.bms | COTRN00 | COTRN00C | Transaction List |
| 10 | COTRN01.bms | COTRN01 | COTRN01C | Transaction View |
| 11 | COTRN02.bms | COTRN02 | COTRN02C | Transaction Add |
| 12 | CORPT00.bms | CORPT00 | CORPT00C | Transaction Report |
| 13 | COBIL00.bms | COBIL00 | COBIL00C | Bill Payment |
| 14 | COUSR00.bms | COUSR00 | COUSR00C | User List |
| 15 | COUSR01.bms | COUSR01 | COUSR01C | User Add |
| 16 | COUSR02.bms | COUSR02 | COUSR02C | User Update |
| 17 | COUSR03.bms | COUSR03 | COUSR03C | User Delete |

**BMS-Generated Copybooks (`app/cpy-bms/`):** 17 files -- one per BMS map, auto-generated symbolic map definitions used in COBOL programs for screen I/O.

---

## 4. JCL Batch Jobs (`app/jcl/`) -- 38 Jobs

### 4.1 Data File Maintenance Jobs

| # | JCL Job | Function | Files Affected |
|---|---------|----------|----------------|
| 1 | ACCTFILE.jcl | Refresh account master VSAM | ACCTDAT |
| 2 | CARDFILE.jcl | Refresh card master VSAM | CARDDAT |
| 3 | CUSTFILE.jcl | Refresh customer master VSAM | CUSTDAT |
| 4 | XREFFILE.jcl | Load card cross-reference VSAM | CARDXREF |
| 5 | TRANFILE.jcl | Load transaction master VSAM | TRANSACT |
| 6 | DUSRSECJ.jcl | Load user security VSAM | USRSEC |

### 4.2 Core Batch Processing Jobs

| # | JCL Job | Function | Programs Invoked |
|---|---------|----------|-----------------|
| 7 | POSTTRAN.jcl | Transaction posting cycle | CBTRN01C, CBTRN02C |
| 8 | INTCALC.jcl | Interest calculation | CBACT04C |
| 9 | COMBTRAN.jcl | Combine/merge transactions | (SORT/MERGE) |
| 10 | CREASTMT.JCL | Create account statements | CBSTM03A |
| 11 | TRANBKP.jcl | Backup transaction file | (IDCAMS REPRO) |
| 12 | TRANREPT.jcl | Transaction report generation | CBTRN03C |

### 4.3 Infrastructure / VSAM Management Jobs

| # | JCL Job | Function |
|---|---------|----------|
| 13 | CLOSEFIL.jcl | Close CICS-managed VSAM files for batch |
| 14 | OPENFIL.jcl | Re-open CICS-managed VSAM files after batch |
| 15 | DEFGDGB.jcl | Define GDG base for backups |
| 16 | DEFGDGD.jcl | Define GDG base for daily files |
| 17 | TRANIDX.jcl | Define/build alternate index on transactions |
| 18 | DEFCUST.jcl | Define customer VSAM cluster |
| 19 | ESDSRRDS.jcl | Define ESDS/RRDS clusters |

### 4.4 Data Load / Utility Jobs

| # | JCL Job | Function |
|---|---------|----------|
| 20 | READACCT.jcl | Read/print account data |
| 21 | READCARD.jcl | Read/print card data |
| 22 | READCUST.jcl | Read/print customer data |
| 23 | READXREF.jcl | Read/print cross-reference data |
| 24 | REPTFILE.jcl | Report file maintenance |
| 25 | TCATBALF.jcl | Transaction category balance file load |
| 26 | TRANCATG.jcl | Transaction category file load |
| 27 | TRANTYPE.jcl | Transaction type file load |
| 28 | DISCGRP.jcl | Disclosure group file load |
| 29 | DALYREJS.jcl | Daily rejects file definition |
| 30 | PRTCATBL.jcl | Print category balance report |

### 4.5 Export/Import Jobs

| # | JCL Job | Function |
|---|---------|----------|
| 31 | CBEXPORT.jcl | Export all data to sequential file |
| 32 | CBIMPORT.jcl | Import data from sequential file |

### 4.6 Compilation / Admin Jobs

| # | JCL Job | Function |
|---|---------|----------|
| 33 | CBADMCDJ.jcl | Compile CardDemo batch programs |
| 34 | FTPJCL.JCL | FTP file transfer job |
| 35 | INTRDRJ1.JCL | Internal reader job submission (1) |
| 36 | INTRDRJ2.JCL | Internal reader job submission (2) |
| 37 | TXT2PDF1.JCL | Convert text reports to PDF |
| 38 | WAITSTEP.jcl | Wait/delay step (uses COBSWAIT) |

---

## 5. Assembler Programs (`app/asm/`) -- 2 Programs

| # | Program | Function |
|---|---------|----------|
| 1 | COBDATFT.asm | Date format conversion utility (called by CBACT01C) |
| 2 | MVSWAIT.asm | MVS wait/delay routine (called by COBSWAIT) |

---

## 6. JCL Procedures (`app/proc/`) -- 2 Procedures

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Report processing procedure |
| 2 | TRANREPT.prc | Transaction report procedure |

---

## 7. Optional Extension Modules

### 7.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for payment authorization.

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | COPAUA0C.cbl | Online | MQ trigger program for authorization |
| 2 | COPAUS0C.cbl | Online | Authorization summary view |
| 3 | COPAUS1C.cbl | Online | Authorization detail view |
| 4 | COPAUS2C.cbl | Online | Fraud marking (writes to DB2) |
| 5 | CBPAUP0C.cbl | Batch | Purge old authorizations |
| 6 | DBUNLDGS.CBL | Batch | Unload GSAM database |
| 7 | PAUDBLOD.CBL | Batch | Load authorization IMS database |
| 8 | PAUDBUNL.CBL | Batch | Unload authorization IMS database |

**Additional Artifacts:** 9 copybooks, 2 BMS maps, 5 JCL jobs, DCL/DDL definitions, IMS DBD/PSB.

### 7.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based CRUD for transaction type management.

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | COTRTLIC.cbl | Online | Transaction type list/delete |
| 2 | COTRTUPC.cbl | Online | Transaction type add/update |
| 3 | COBTUPDT.cbl | Batch | Batch transaction type update |

**Additional Artifacts:** 2 copybooks, 2 BMS maps, 3 JCL jobs, DCL/DDL definitions.

### 7.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response for system date and account inquiry.

| # | Program | Type | Function |
|---|---------|------|----------|
| 1 | CODATE01.cbl | Online | System date via MQ (channel CDRD) |
| 2 | COACCT01.cbl | Online | Account inquiry via MQ (channel CDRA) |

---

## 8. Supporting Assets

| Category | Path | Count | Description |
|----------|------|-------|-------------|
| Sample Data (ASCII) | `app/data/ASCII/` | 9 files | Test data: acctdata, carddata, cardxref, custdata, dailytran, discgrp, tcatbal, trancatg, trantype |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | -- | Mainframe-format data for upload |
| CSD Definitions | `app/csd/` | 1 file | CICS resource definitions (CARDDEMO.CSD) |
| Control Files | `app/ctl/` | -- | Control files for batch processing |
| Catalog Listings | `app/catlg/` | -- | Catalog listings |
| Assembler Macros | `app/maclib/` | -- | Assembler macro library |
| Scheduler Configs | `app/scheduler/` | 2 files | CA7 and Control-M job schedules |
| Shell Scripts | `scripts/` | 10+ files | FTP-based mainframe interaction scripts |
| Diagrams | `diagrams/` | -- | Architecture diagrams and screen captures |
| Samples | `samples/` | -- | Sample JCL, procs, and M2 configs |

---

## 9. Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C |
| `CB*` | Batch program | CBTRN02C |
| `CV*` | Copybook -- data structure (VSAM) | CVACT01Y |
| `CS*` | Copybook -- shared/system utility | CSDAT01Y |
| `CO*` (cpy) | Copybook -- COMMAREA/menu | COCOM01Y |
| `*Y` suffix | Copybook file | CVACT01Y |
| `*C` suffix | COBOL program | COACTVWC |

---

## 10. Grand Totals

| Artifact Type | Core Count | Optional Module Count | Grand Total |
|---------------|------------|----------------------|-------------|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks | 30 | 11 | **41** |
| BMS Maps | 17 | 4 | **21** |
| BMS Copybooks | 17 | 4 | **21** |
| JCL Jobs | 38 | 8 | **46** |
| Assembler Programs | 2 | 0 | **2** |
| JCL Procedures | 2 | 0 | **2** |
| **Total Artifacts** | **137** | **40** | **177** |
