# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Codebase Version:** CardDemo v2.0  
> **Total Assets:** 31 COBOL programs + 13 optional-module programs + 30 copybooks + 11 optional-module copybooks + 38 JCL jobs + 17 BMS maps + 2 assembler programs + 2 JCL procedures

---

## 1. COBOL Programs — Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program ID | File | Lines | CICS Trans | Function | Business Domain | Complexity |
|---|-----------|------|-------|------------|----------|-----------------|------------|
| 1 | COSGN00C | COSGN00C.cbl | 261 | CC00 | Sign-on / Authentication | Security | Low |
| 2 | COMEN01C | COMEN01C.cbl | 309 | CM00 | Main Menu (Regular Users) | Navigation | Low |
| 3 | COADM01C | COADM01C.cbl | 288 | CA00 | Admin Menu (Admin Users) | Navigation / Admin | Low |
| 4 | COACTVWC | COACTVWC.cbl | 942 | CAVW | Account View | Account Mgmt | Medium |
| 5 | COACTUPC | COACTUPC.cbl | 4,237 | CAUP | Account Update | Account Mgmt | **Very High** |
| 6 | COCRDLIC | COCRDLIC.cbl | 1,460 | CCLI | Credit Card List | Card Mgmt | High |
| 7 | COCRDSLC | COCRDSLC.cbl | 888 | CCDL | Credit Card Detail View | Card Mgmt | Medium |
| 8 | COCRDUPC | COCRDUPC.cbl | 1,560 | CCUP | Credit Card Update | Card Mgmt | High |
| 9 | COTRN00C | COTRN00C.cbl | 699 | CT00 | Transaction List | Transactions | Medium |
| 10 | COTRN01C | COTRN01C.cbl | 330 | CT01 | Transaction View | Transactions | Low |
| 11 | COTRN02C | COTRN02C.cbl | 783 | CT02 | Transaction Add | Transactions | Medium |
| 12 | CORPT00C | CORPT00C.cbl | 649 | CR00 | Transaction Reports (submit batch) | Reporting | Medium |
| 13 | COBIL00C | COBIL00C.cbl | 572 | CB00 | Bill Payment | Payments | Medium |
| 14 | COUSR00C | COUSR00C.cbl | 695 | CU00 | User List (Admin) | User Admin | Medium |
| 15 | COUSR01C | COUSR01C.cbl | 299 | CU01 | User Add (Admin) | User Admin | Low |
| 16 | COUSR02C | COUSR02C.cbl | 414 | CU02 | User Update (Admin) | User Admin | Low |
| 17 | COUSR03C | COUSR03C.cbl | 359 | CU03 | User Delete (Admin) | User Admin | Low |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program ID | File | Lines | Function | Business Domain | Complexity |
|---|-----------|------|-------|----------|-----------------|------------|
| 18 | CBACT01C | CBACT01C.cbl | 430 | Read & print account data file | Account Mgmt / Utility | Low |
| 19 | CBACT02C | CBACT02C.cbl | 178 | Read & print card data file | Card Mgmt / Utility | Low |
| 20 | CBACT03C | CBACT03C.cbl | 178 | Read & print account cross-reference file | Card Mgmt / Utility | Low |
| 21 | CBACT04C | CBACT04C.cbl | 652 | Interest calculation | Financial / Core Batch | Medium |
| 22 | CBCUS01C | CBCUS01C.cbl | 178 | Read & print customer data file | Customer Mgmt / Utility | Low |
| 23 | CBTRN01C | CBTRN01C.cbl | 494 | Validate daily transactions | Transactions / Core Batch | Medium |
| 24 | CBTRN02C | CBTRN02C.cbl | 731 | Post daily transactions to master | Transactions / Core Batch | **High** |
| 25 | CBTRN03C | CBTRN03C.cbl | 649 | Print transaction detail report | Reporting | Medium |
| 26 | CBSTM03A | CBSTM03A.CBL | 924 | Generate account statements | Statements / Core Batch | **High** |
| 27 | CBSTM03B | CBSTM03B.CBL | 230 | Statement file-processing subroutine | Statements / Subroutine | Low |
| 28 | CBEXPORT | CBEXPORT.cbl | 582 | Export all data for branch migration | Data Migration | Medium |
| 29 | CBIMPORT | CBIMPORT.cbl | 487 | Import data from branch migration | Data Migration | Medium |
| 30 | COBSWAIT | COBSWAIT.cbl | 41 | Wait utility (calls ASM MVSWAIT) | Utility | Low |

### 1.3 Shared Utility

| # | Program ID | File | Lines | Function | Domain |
|---|-----------|------|-------|----------|--------|
| 31 | CSUTLDTC | CSUTLDTC.cbl | 157 | Date conversion utility | Utility |

---

## 2. Optional Module Programs

### 2.1 Authorization — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 32 | COPAUA0C | COPAUA0C.cbl | ~350 | CICS/IMS/MQ | Card authorization decision (MQ trigger) |
| 33 | COPAUS0C | COPAUS0C.cbl | ~700 | CICS/IMS/BMS | Summary view of authorization messages |
| 34 | COPAUS1C | COPAUS1C.cbl | ~400 | CICS/IMS/BMS | Detail view of authorization message |
| 35 | COPAUS2C | COPAUS2C.cbl | ~150 | CICS/IMS/DB2 | Mark authorization message as fraud |
| 36 | CBPAUP0C | CBPAUP0C.cbl | ~250 | Batch/IMS | Purge expired pending authorizations |
| 37 | DBUNLDGS | DBUNLDGS.CBL | ~200 | Batch/IMS | Unload IMS database to GSAM |
| 38 | PAUDBLOD | PAUDBLOD.CBL | ~200 | Batch/IMS | Load IMS authorization database |
| 39 | PAUDBUNL | PAUDBUNL.CBL | ~200 | Batch/IMS | Unload IMS authorization database |

### 2.2 Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 40 | COTRTLIC | COTRTLIC.cbl | ~2,100 | CICS/DB2 | List/delete transaction types |
| 41 | COTRTUPC | COTRTUPC.cbl | ~1,700 | CICS/DB2 | Add/edit transaction types |
| 42 | COBTUPDT | COBTUPDT.cbl | ~300 | Batch/DB2 | Batch update transaction types |

### 2.3 VSAM-MQ (`app/app-vsam-mq/cbl/`)

| # | Program ID | File | Lines | Type | Function |
|---|-----------|------|-------|------|----------|
| 43 | COACCT01 | COACCT01.cbl | ~400 | MQ Server | Account inquiry via MQ request/response |
| 44 | CODATE01 | CODATE01.cbl | ~350 | MQ Server | System date inquiry via MQ request/response |

---

## 3. Copybooks — Core (`app/cpy/`)

### 3.1 Data Record Layouts (prefix `CV*`)

| # | Copybook | Record Length | Function | Business Entity |
|---|----------|-------------|----------|-----------------|
| 1 | CVACT01Y.cpy | 300 bytes | Account master record | Account |
| 2 | CVACT02Y.cpy | 150 bytes | Card data record | Credit Card |
| 3 | CVACT03Y.cpy | 50 bytes | Card-to-account cross-reference | Card-Account XREF |
| 4 | CVCUS01Y.cpy | 500 bytes | Customer master record | Customer |
| 5 | CVCRD01Y.cpy | — | Credit card working-storage area | Card (work area) |
| 6 | CVTRA01Y.cpy | 50 bytes | Transaction category balance | Tran Category Balance |
| 7 | CVTRA02Y.cpy | 50 bytes | Disclosure group record | Disclosure Group |
| 8 | CVTRA03Y.cpy | 60 bytes | Transaction type record | Transaction Type |
| 9 | CVTRA04Y.cpy | 60 bytes | Transaction category type | Transaction Category |
| 10 | CVTRA05Y.cpy | 350 bytes | Online transaction record | Transaction |
| 11 | CVTRA06Y.cpy | 350 bytes | Daily transaction record | Daily Transaction |
| 12 | CVTRA07Y.cpy | — | Transaction report headers/totals | Report Layout |
| 13 | CVEXPORT.cpy | — | Export/import record layout | Data Migration |
| 14 | COSTM01.CPY | — | Statement transaction re-keyed layout | Statement |
| 15 | CUSTREC.cpy | — | Alternative customer record | Customer (alt) |

### 3.2 Application Infrastructure Copybooks

| # | Copybook | Function |
|---|----------|----------|
| 16 | COCOM01Y.cpy | Common COMMAREA structure (shared across all online programs) |
| 17 | COMEN02Y.cpy | Main menu option definitions (11 options) |
| 18 | COADM02Y.cpy | Admin menu option definitions (6 options) |
| 19 | COTTL01Y.cpy | Screen title/header constants |
| 20 | CSDAT01Y.cpy | Current-date working-storage fields |
| 21 | CSMSG01Y.cpy | Common message constants |
| 22 | CSMSG02Y.cpy | Abend handling variables |
| 23 | CSUSR01Y.cpy | Signed-on user security record |
| 24 | CSSETATY.cpy | Set field attributes utility |
| 25 | CSSTRPFY.cpy | String strip/format utility |
| 26 | CSLKPCDY.cpy | Lookup code utility |
| 27 | CSUTLDPY.cpy | Date utility procedure |
| 28 | CSUTLDWY.cpy | Date utility working-storage |
| 29 | CODATECN.cpy | Date conversion constants |
| 30 | UNUSED1Y.cpy | Unused placeholder record |

### 3.3 Optional Module Copybooks

**Authorization (IMS/DB2/MQ) — 9 copybooks:**
CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB

**Transaction Type (DB2) — 2 copybooks:**
CSDB2RPY (DB2 procedures), CSDB2RWY (DB2 working-storage)

---

## 4. BMS Screen Maps

### 4.1 Core Maps (`app/bms/`) — 17 maps

| # | Map | Programs Using It | Screen Function |
|---|-----|-------------------|-----------------|
| 1 | COSGN00.bms | COSGN00C | Sign-on screen |
| 2 | COMEN01.bms | COMEN01C | Main menu |
| 3 | COADM01.bms | COADM01C | Admin menu |
| 4 | COACTVW.bms | COACTVWC | Account view |
| 5 | COACTUP.bms | COACTUPC | Account update |
| 6 | COCRDLI.bms | COCRDLIC | Credit card list |
| 7 | COCRDSL.bms | COCRDSLC | Credit card detail |
| 8 | COCRDUP.bms | COCRDUPC | Credit card update |
| 9 | COTRN00.bms | COTRN00C | Transaction list |
| 10 | COTRN01.bms | COTRN01C | Transaction view |
| 11 | COTRN02.bms | COTRN02C | Transaction add |
| 12 | CORPT00.bms | CORPT00C | Transaction reports |
| 13 | COBIL00.bms | COBIL00C | Bill payment |
| 14 | COUSR00.bms | COUSR00C | User list |
| 15 | COUSR01.bms | COUSR01C | User add |
| 16 | COUSR02.bms | COUSR02C | User update |
| 17 | COUSR03.bms | COUSR03C | User delete |

### 4.2 Optional Module Maps

| # | Map | Module | Screen Function |
|---|-----|--------|-----------------|
| 18 | COPAU00.bms | Auth IMS/DB2/MQ | Authorization summary |
| 19 | COPAU01.bms | Auth IMS/DB2/MQ | Authorization detail |
| 20 | COTRTLI.bms | Tran Type DB2 | Transaction type list |
| 21 | COTRTUP.bms | Tran Type DB2 | Transaction type update |

**BMS-generated copybooks** (`app/cpy-bms/`): 17 core + maps mirror each BMS map above.

---

## 5. JCL Batch Jobs (`app/jcl/`) — 38 jobs

### 5.1 Core Batch Processing Cycle

| # | Job | COBOL Program | Function | Category |
|---|-----|--------------|----------|----------|
| 1 | CLOSEFIL.jcl | — (SDSF) | Close CICS files for batch window | Operations |
| 2 | ACCTFILE.jcl | — (IDCAMS) | Refresh account master VSAM | Data Refresh |
| 3 | CARDFILE.jcl | — (IDCAMS) | Refresh card master VSAM | Data Refresh |
| 4 | CUSTFILE.jcl | — (IDCAMS) | Refresh customer master VSAM | Data Refresh |
| 5 | XREFFILE.jcl | — (IDCAMS) | Load card-account cross-reference | Data Refresh |
| 6 | TRANFILE.jcl | — (IDCAMS) | Load transaction master VSAM | Data Refresh |
| 7 | DUSRSECJ.jcl | — (IDCAMS/IEBGENER) | Load user security VSAM | Security Setup |
| 8 | POSTTRAN.jcl | CBTRN02C | Post daily transactions | **Core Processing** |
| 9 | INTCALC.jcl | CBACT04C | Calculate interest on accounts | **Core Processing** |
| 10 | TRANBKP.jcl | — (IDCAMS) | Backup transaction file | Backup |
| 11 | COMBTRAN.jcl | — (SORT) | Combine/sort transactions | Data Processing |
| 12 | CREASTMT.JCL | CBSTM03A | Generate account statements | **Core Processing** |
| 13 | TRANIDX.jcl | — (IDCAMS) | Define alternate index on transactions | Index Mgmt |
| 14 | OPENFIL.jcl | — (SDSF) | Reopen CICS files after batch | Operations |

### 5.2 Reporting & Utility Jobs

| # | Job | COBOL Program | Function | Category |
|---|-----|--------------|----------|----------|
| 15 | TRANREPT.jcl | CBTRN03C | Transaction detail report | Reporting |
| 16 | READACCT.jcl | CBACT01C | Read/print account data | Utility |
| 17 | READCARD.jcl | CBACT02C | Read/print card data | Utility |
| 18 | READXREF.jcl | CBACT03C | Read/print cross-reference | Utility |
| 19 | READCUST.jcl | CBCUS01C | Read/print customer data | Utility |
| 20 | PRTCATBL.jcl | — (SORT) | Print category balances | Reporting |
| 21 | REPTFILE.jcl | — (IDCAMS) | Define report file | Setup |
| 22 | TXT2PDF1.JCL | — (IKJEFT1B) | Convert text report to PDF | Utility |

### 5.3 Data Export/Import

| # | Job | COBOL Program | Function | Category |
|---|-----|--------------|----------|----------|
| 23 | CBEXPORT.jcl | CBEXPORT | Export all data for migration | Data Migration |
| 24 | CBIMPORT.jcl | CBIMPORT | Import data from migration export | Data Migration |

### 5.4 VSAM Definition & Maintenance

| # | Job | Function | Category |
|---|-----|----------|----------|
| 25 | DEFGDGB.jcl | Define GDG base clusters | VSAM Setup |
| 26 | DEFGDGD.jcl | Define GDG data & backup generations | VSAM Setup |
| 27 | TCATBALF.jcl | Define transaction category balance VSAM | VSAM Setup |
| 28 | TRANTYPE.jcl | Define transaction type VSAM | VSAM Setup |
| 29 | TRANCATG.jcl | Define transaction category VSAM | VSAM Setup |
| 30 | DISCGRP.jcl | Define disclosure group VSAM | VSAM Setup |
| 31 | DALYREJS.jcl | Define daily rejects VSAM | VSAM Setup |
| 32 | DEFCUST.jcl | Define customer VSAM (alternate) | VSAM Setup |
| 33 | ESDSRRDS.jcl | ESDS/RRDS VSAM demo definitions | VSAM Demo |

### 5.5 Other

| # | Job | Function | Category |
|---|-----|----------|----------|
| 34 | CBADMCDJ.jcl | Load CICS CSD resource definitions | CICS Admin |
| 35 | WAITSTEP.jcl | Wait step (calls COBSWAIT) | Utility |
| 36 | FTPJCL.JCL | FTP file transfer | Utility |
| 37 | INTRDRJ1.JCL | Internal reader job chain (part 1) | Utility |
| 38 | INTRDRJ2.JCL | Internal reader job chain (part 2) | Utility |

---

## 6. Assembler Programs (`app/asm/`)

| # | Program | Function |
|---|---------|----------|
| 1 | MVSWAIT.asm | MVS WAIT SVC — sleep utility called by COBSWAIT |
| 2 | COBDATFT.asm | Date formatting utility |

---

## 7. JCL Procedures (`app/proc/`)

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Reprocessing procedure (used by TRANBKP, PRTCATBL, TRANREPT) |
| 2 | TRANREPT.prc | Transaction report procedure |

---

## 8. Supporting Assets

| Asset | Location | Description |
|-------|----------|-------------|
| CICS CSD definitions | `app/csd/CARDDEMO.CSD` | Transaction and program resource definitions |
| CA7 scheduler config | `app/scheduler/CardDemo.ca7` | Job scheduling (CA7 format) |
| Control-M config | `app/scheduler/CardDemo.controlm` | Job scheduling (Control-M format) |
| Control file | `app/ctl/REPROCT.ctl` | Report control file |
| Assembler macros | `app/maclib/ASMWAIT.mac`, `COCDATFT.mac` | Macro libraries for ASM programs |
| ASCII sample data | `app/data/ASCII/` | Test data in ASCII format |
| EBCDIC sample data | `app/data/EBCDIC/` | Production-format test data |

---

## 9. Classification Summary

| Category | Count | Key Programs |
|----------|-------|-------------|
| Online CICS (core) | 17 | COSGN00C, COMEN01C, COACTUPC, COCRDLIC, COTRN00C |
| Batch (core) | 13 | CBTRN02C, CBACT04C, CBSTM03A, CBEXPORT |
| Utility | 1 | CSUTLDTC |
| Optional — Auth IMS/DB2/MQ | 8 | COPAUA0C, COPAUS0C, CBPAUP0C |
| Optional — Tran Type DB2 | 3 | COTRTLIC, COTRTUPC, COBTUPDT |
| Optional — VSAM-MQ | 2 | COACCT01, CODATE01 |
| **Total Programs** | **44** | |
| Core Copybooks | 30 | CVACT01Y, CVCUS01Y, CVTRA05Y, COCOM01Y |
| Optional Copybooks | 11 | CIPAUDTY, CSDB2RWY |
| BMS Maps (core) | 17 | COSGN00, COMEN01, COACTUP |
| BMS Maps (optional) | 4 | COPAU00, COTRTLI |
| JCL Jobs | 38 | POSTTRAN, INTCALC, CREASTMT |
| JCL Procedures | 2 | REPROC, TRANREPT |
| Assembler Programs | 2 | MVSWAIT, COBDATFT |

---

## 10. Naming Conventions

| Prefix | Meaning | Examples |
|--------|---------|---------|
| `CO*` | Online CICS program | COSGN00C, COMEN01C |
| `CB*` | Batch COBOL program | CBTRN02C, CBACT04C |
| `CS*` | Common shared copybook/utility | CSDAT01Y, CSMSG01Y |
| `CV*` | VSAM record layout copybook | CVACT01Y, CVTRA05Y |
| `CC*` | Common COMMAREA / card-related | COCOM01Y |
| `*Y` (suffix) | Copybook (data definition) | CVACT01Y, CSUSR01Y |
| `*C` (suffix) | COBOL program | COSGN00C, CBTRN02C |
