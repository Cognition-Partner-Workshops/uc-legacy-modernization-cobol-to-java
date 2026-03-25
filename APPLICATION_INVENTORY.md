# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Runtime:** CICS/VSAM on z/OS | **Languages:** COBOL, BMS, JCL, Assembler

---

## Table of Contents

1. [Summary](#summary)
2. [COBOL Programs — Core (app/cbl/)](#cobol-programs--core)
3. [COBOL Programs — Optional Modules](#cobol-programs--optional-modules)
4. [Copybooks — Data Structures (app/cpy/)](#copybooks--data-structures)
5. [Copybooks — BMS-Generated (app/cpy-bms/)](#copybooks--bms-generated)
6. [BMS Maps — Screen Definitions (app/bms/)](#bms-maps--screen-definitions)
7. [JCL Jobs (app/jcl/)](#jcl-jobs)
8. [Assembler Programs (app/asm/)](#assembler-programs)
9. [JCL Procedures (app/proc/)](#jcl-procedures)
10. [Scheduler Configurations (app/scheduler/)](#scheduler-configurations)
11. [Asset Counts](#asset-counts)

---

## Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It supports two user roles (**Regular** and **Admin**) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

| Asset Type | Core | Optional Modules | Total |
|---|---|---|---|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks (Data) | 30 | 11 | **41** |
| BMS Maps | 17 | 4 | **21** |
| BMS Copybooks | 17 | 4 | **21** |
| JCL Jobs | 38 | 8 | **46** |
| Assembler Programs | 2 | 0 | **2** |
| JCL Procedures | 2 | 0 | **2** |
| Scheduler Configs | 2 | 0 | **2** |

---

## COBOL Programs — Core

### Naming Convention

- **CO\*C** — Online CICS programs (interactive, screen-driven)
- **CB\*C** — Batch programs (file-based, scheduled execution)
- **CS\*C** — Shared utility/service programs

### Online CICS Programs (17 programs)

| # | Program | Lines | Tran ID | Function | Domain | BMS Map |
|---|---------|-------|---------|----------|--------|---------|
| 1 | COSGN00C | 261 | CC00 | Sign-on / Authentication | Security | COSGN00 |
| 2 | COMEN01C | 309 | CM00 | Main Menu (Regular Users) | Navigation | COMEN01 |
| 3 | COADM01C | 288 | CA00 | Admin Menu | Navigation | COADM01 |
| 4 | COACTVWC | 942 | CAVW | Account View | Account Mgmt | COACTVW |
| 5 | COACTUPC | 4,237 | CAUP | Account Update | Account Mgmt | COACTUP |
| 6 | COCRDLIC | 1,460 | CCLI | Credit Card List | Card Mgmt | COCRDLI |
| 7 | COCRDSLC | 888 | CCDL | Credit Card Detail View | Card Mgmt | COCRDSL |
| 8 | COCRDUPC | 1,560 | CCUP | Credit Card Update | Card Mgmt | COCRDUP |
| 9 | COTRN00C | 699 | CT00 | Transaction List | Transaction Mgmt | COTRN00 |
| 10 | COTRN01C | 330 | CT01 | Transaction Detail View | Transaction Mgmt | COTRN01 |
| 11 | COTRN02C | 783 | CT02 | Transaction Add | Transaction Mgmt | COTRN02 |
| 12 | CORPT00C | 649 | CR00 | Transaction Report Request | Reporting | CORPT00 |
| 13 | COBIL00C | 572 | CB00 | Bill Payment | Billing | COBIL00 |
| 14 | COUSR00C | 695 | CU00 | User List (Admin) | User Admin | COUSR00 |
| 15 | COUSR01C | 299 | CU01 | User Add (Admin) | User Admin | COUSR01 |
| 16 | COUSR02C | 414 | CU02 | User Update (Admin) | User Admin | COUSR02 |
| 17 | COUSR03C | 359 | CU03 | User Delete (Admin) | User Admin | COUSR03 |

### Batch Programs (13 programs)

| # | Program | Lines | Function | Domain | Key Files |
|---|---------|-------|----------|--------|-----------|
| 1 | CBACT01C | 430 | Account File List/Print | Account Mgmt | ACCTDAT |
| 2 | CBACT02C | 178 | Card Data File List/Print | Card Mgmt | CARDDAT |
| 3 | CBACT03C | 178 | Card Cross-Reference List | Card Mgmt | XREFFILE |
| 4 | CBACT04C | 652 | Interest Calculation | Financial Processing | ACCTDAT, DISCGRP, TRANTYPE, TCATBALF |
| 5 | CBCUS01C | 178 | Customer File List/Print | Customer Mgmt | CUSTFILE |
| 6 | CBTRN01C | 494 | Daily Transaction File List | Transaction Mgmt | DALYTRAN |
| 7 | CBTRN02C | 731 | Transaction Posting | Transaction Mgmt | DALYTRAN, TRANSACT, XREFFILE, ACCTFILE, TCATBALF, DALYREJS |
| 8 | CBTRN03C | 649 | Daily Transaction Report | Reporting | DALYTRAN, TRANSACT, XREFFILE, TRANTYPE, TRANCATG |
| 9 | CBSTM03A | 924 | Statement Generation (Main) | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE, STMTFILE, HTMLFILE |
| 10 | CBSTM03B | 230 | Statement Generation (Sub) | Reporting | Called by CBSTM03A |
| 11 | CBEXPORT | 582 | Data Export (All Files to Sequential) | Data Utilities | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE |
| 12 | CBIMPORT | 487 | Data Import (Sequential to VSAM) | Data Utilities | IMPFILE, CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE |
| 13 | COBSWAIT | 41 | Wait/Delay Utility | Utility | — |

### Utility Programs (1 program)

| # | Program | Lines | Function | Domain |
|---|---------|-------|----------|--------|
| 1 | CSUTLDTC | 157 | Date Validation Utility | Shared Utility |

---

## COBOL Programs — Optional Modules

### Authorization Module — IMS/DB2/MQ (app/app-authorization-ims-db2-mq/)

| # | Program | Lines | Type | Function | Domain |
|---|---------|-------|------|----------|--------|
| 1 | COPAUA0C | 1,026 | Online | MQ Trigger — Payment Auth Listener | Authorization |
| 2 | COPAUS0C | 1,032 | Online | Payment Auth Summary | Authorization |
| 3 | COPAUS1C | 604 | Online | Payment Auth Details | Authorization |
| 4 | COPAUS2C | 244 | Online | Fraud Marking (DB2 Update) | Authorization |
| 5 | CBPAUP0C | 386 | Batch | Batch Purge Auth Records | Authorization |
| 6 | DBUNLDGS | 366 | Batch | Unload GSAM Database | Data Utilities |
| 7 | PAUDBLOD | 369 | Batch | Load IMS Database | Data Utilities |
| 8 | PAUDBUNL | 317 | Batch | Unload IMS Database | Data Utilities |

### Transaction Type DB2 Module (app/app-transaction-type-db2/)

| # | Program | Lines | Type | Function | Domain |
|---|---------|-------|------|----------|--------|
| 1 | COTRTUPC | 1,702 | Online | Transaction Type Add/Edit (DB2) | Configuration |
| 2 | COTRTLIC | 2,098 | Online | Transaction Type List/Delete (DB2) | Configuration |
| 3 | COBTUPDT | 237 | Batch | Batch Transaction Type Update (DB2) | Configuration |

### VSAM-MQ Module (app/app-vsam-mq/)

| # | Program | Lines | Type | Function | Domain |
|---|---------|-------|------|----------|--------|
| 1 | CODATE01 | 524 | Online | MQ System Date Service | Utility |
| 2 | COACCT01 | 620 | Online | MQ Account Inquiry Service | Account Mgmt |

---

## Copybooks — Data Structures

### Naming Convention

- **CV\*Y** — VSAM data record layouts (business entities)
- **CO\*Y** — Common/online data areas
- **CS\*Y** — Shared service structures (messages, dates, utilities)
- **CU\*** — Miscellaneous record layouts

### Business Entity Copybooks (15)

| # | Copybook | Record Size | Entity | Description |
|---|----------|------------|--------|-------------|
| 1 | CVACT01Y | 300 bytes | Account | Account master record layout |
| 2 | CVACT02Y | 150 bytes | Card | Credit card data record layout |
| 3 | CVACT03Y | 50 bytes | Card Cross-Ref | Card-to-Account-to-Customer cross-reference |
| 4 | CVCUS01Y | 500 bytes | Customer | Customer master record layout |
| 5 | CVCRD01Y | Variable | Card (Work) | Card working-storage area |
| 6 | CVTRA01Y | 50 bytes | Tran Cat Balance | Transaction category balance record |
| 7 | CVTRA02Y | 50 bytes | Disclosure Group | Disclosure group / interest rate record |
| 8 | CVTRA03Y | 60 bytes | Transaction Type | Transaction type master record |
| 9 | CVTRA04Y | 60 bytes | Tran Category | Transaction category type record |
| 10 | CVTRA05Y | 350 bytes | Transaction | Transaction (processed) record |
| 11 | CVTRA06Y | 350 bytes | Daily Transaction | Daily transaction input record |
| 12 | CVTRA07Y | Variable | Report Headers | Transaction report layout structures |
| 13 | CVEXPORT | Variable | Export Record | Unified export/import record layout |
| 14 | CUSTREC | Variable | Customer (Alt) | Alternate customer record layout |
| 15 | COSTM01 | 350 bytes | Statement Tran | Transaction layout for statement reporting |

### Application / Communication Copybooks (7)

| # | Copybook | Description |
|---|----------|-------------|
| 1 | COCOM01Y | Application COMMAREA — inter-program communication area |
| 2 | COMEN02Y | Main menu option definitions (names, programs, access levels) |
| 3 | COADM02Y | Admin menu option definitions |
| 4 | COTTL01Y | Screen title/header constants |
| 5 | CSUSR01Y | User security record (login credentials, user type) |
| 6 | CSMSG01Y | Common application messages |
| 7 | CSMSG02Y | Abend/error message variables |

### Utility / Service Copybooks (8)

| # | Copybook | Description |
|---|----------|-------------|
| 1 | CSDAT01Y | Current date/time working-storage |
| 2 | CODATECN | Date conversion record (for COBDATFT assembler call) |
| 3 | CSUTLDPY | Date validation utility parameters |
| 4 | CSUTLDWY | Date edit working-storage variables |
| 5 | CSSETATY | Screen attribute setting utility |
| 6 | CSSTRPFY | PF-key storage/mapping utility |
| 7 | CSLKPCDY | Lookup code validation utility |
| 8 | UNUSED1Y | Unused/placeholder record layout |

### Optional Module Copybooks (11)

**Authorization Module (9):**

| # | Copybook | Description |
|---|----------|-------------|
| 1 | CCPAUERY | Payment authorization error record |
| 2 | CCPAURLY | Payment authorization reply record |
| 3 | CCPAURQY | Payment authorization request record |
| 4 | CIPAUDTY | IMS payment authorization detail segment |
| 5 | CIPAUSMY | IMS payment authorization summary segment |
| 6 | IMSFUNCS | IMS function codes |
| 7 | PADFLPCB | IMS PCB — detail database |
| 8 | PASFLPCB | IMS PCB — summary database |
| 9 | PAUTBPCB | IMS PCB — auth table |

**Transaction Type DB2 Module (2):**

| # | Copybook | Description |
|---|----------|-------------|
| 1 | CSDB2RPY | DB2 reply record for transaction types |
| 2 | CSDB2RWY | DB2 read/write working variables |

---

## Copybooks — BMS-Generated

These are auto-generated from BMS map definitions and contain field-level I/O structures.

| # | Copybook | Source BMS | Screen |
|---|----------|-----------|--------|
| 1 | COSGN00.CPY | COSGN00.bms | Sign-on |
| 2 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COACTVW.CPY | COACTVW.bms | Account View |
| 5 | COACTUP.CPY | COACTUP.bms | Account Update |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card Detail/Selection |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 12 | CORPT00.CPY | CORPT00.bms | Transaction Report |
| 13 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | User Add |
| 16 | COUSR02.CPY | COUSR02.bms | User Update |
| 17 | COUSR03.CPY | COUSR03.bms | User Delete |

---

## BMS Maps — Screen Definitions

| # | BMS Map | Screen Name | Associated Program | Domain |
|---|---------|-------------|-------------------|--------|
| 1 | COSGN00.bms | Login Screen | COSGN00C | Security |
| 2 | COMEN01.bms | Main Menu | COMEN01C | Navigation |
| 3 | COADM01.bms | Admin Menu | COADM01C | Navigation |
| 4 | COACTVW.bms | Account Viewer | COACTVWC | Account Mgmt |
| 5 | COACTUP.bms | Account Update | COACTUPC | Account Mgmt |
| 6 | COCRDLI.bms | Card Listing | COCRDLIC | Card Mgmt |
| 7 | COCRDSL.bms | Card Selection/Detail | COCRDSLC | Card Mgmt |
| 8 | COCRDUP.bms | Card Update | COCRDUPC | Card Mgmt |
| 9 | COTRN00.bms | Transaction List | COTRN00C | Transaction Mgmt |
| 10 | COTRN01.bms | Transaction View | COTRN01C | Transaction Mgmt |
| 11 | COTRN02.bms | Transaction Add | COTRN02C | Transaction Mgmt |
| 12 | CORPT00.bms | Transaction Report | CORPT00C | Reporting |
| 13 | COBIL00.bms | Bill Payment | COBIL00C | Billing |
| 14 | COUSR00.bms | User List | COUSR00C | User Admin |
| 15 | COUSR01.bms | User Add | COUSR01C | User Admin |
| 16 | COUSR02.bms | User Update | COUSR02C | User Admin |
| 17 | COUSR03.bms | User Delete | COUSR03C | User Admin |

### Optional Module BMS Maps (4)

| # | BMS Map | Module | Screen Name |
|---|---------|--------|-------------|
| 1 | COPAU00.bms | Authorization | Payment Auth Summary |
| 2 | COPAU01.bms | Authorization | Payment Auth Details |
| 3 | COTRTLI.bms | Tran Type DB2 | Transaction Type List |
| 4 | COTRTUP.bms | Tran Type DB2 | Transaction Type Update |

---

## JCL Jobs

### Classification Legend

- **Data Refresh** — Delete/define/load VSAM files with fresh data
- **Batch Processing** — Execute COBOL batch programs for business logic
- **File Management** — Open/close CICS files, define GDGs, indexes
- **Reporting** — Generate reports from transaction data
- **Utility** — Infrastructure tasks (FTP, wait, PDF conversion)

### Core JCL Jobs (38)

| # | JCL Job | Classification | Description | Key Programs/Utilities |
|---|---------|---------------|-------------|----------------------|
| 1 | ACCTFILE.jcl | Data Refresh | Refresh account master VSAM | IDCAMS, SDSF |
| 2 | CARDFILE.jcl | Data Refresh | Refresh card master VSAM | IDCAMS, SDSF |
| 3 | CUSTFILE.jcl | Data Refresh | Refresh customer master VSAM | IDCAMS, SDSF |
| 4 | XREFFILE.jcl | Data Refresh | Delete/define card cross-reference VSAM + AIX | IDCAMS |
| 5 | TRANFILE.jcl | Data Refresh | Refresh transaction master VSAM | IDCAMS, SDSF |
| 6 | DUSRSECJ.jcl | Data Refresh | Load user security VSAM file | IDCAMS |
| 7 | TRANTYPE.jcl | Data Refresh | Define transaction type VSAM | IDCAMS |
| 8 | DISCGRP.jcl | Data Refresh | Define disclosure group VSAM | IDCAMS |
| 9 | DEFCUST.jcl | Data Refresh | Define customer VSAM (alternate) | IDCAMS |
| 10 | CLOSEFIL.jcl | File Management | Close CICS files before batch | SDSF (CEMT commands) |
| 11 | OPENFIL.jcl | File Management | Open CICS files after batch | SDSF (CEMT commands) |
| 12 | DEFGDGB.jcl | File Management | Define GDG base for backups | IDCAMS |
| 13 | DEFGDGD.jcl | File Management | Define GDG base for daily data | IDCAMS |
| 14 | TRANIDX.jcl | File Management | Define alternate index on transactions | IDCAMS |
| 15 | ESDSRRDS.jcl | File Management | Define ESDS/RRDS VSAM clusters | IDCAMS |
| 16 | POSTTRAN.jcl | Batch Processing | Core transaction posting | CBTRN02C |
| 17 | INTCALC.jcl | Batch Processing | Interest calculation | CBACT04C |
| 18 | COMBTRAN.jcl | Batch Processing | Combine/merge transactions | SORT/DFSORT |
| 19 | CREASTMT.JCL | Batch Processing | Generate account statements | CBSTM03A |
| 20 | TRANBKP.jcl | Batch Processing | Backup transaction file | IDCAMS REPRO |
| 21 | TRANREPT.jcl | Reporting | Generate daily transaction report | CBTRN03C (via REPROC proc) |
| 22 | REPTFILE.jcl | Reporting | Define report output files | IDCAMS |
| 23 | READACCT.jcl | Reporting | List/print account master | CBACT01C |
| 24 | READCARD.jcl | Reporting | List/print card data | CBACT02C |
| 25 | READCUST.jcl | Reporting | List/print customer data | CBCUS01C |
| 26 | READXREF.jcl | Reporting | List/print cross-reference | CBACT03C |
| 27 | PRTCATBL.jcl | Reporting | Print category balances | IDCAMS PRINT |
| 28 | TCATBALF.jcl | Data Refresh | Define transaction category balance VSAM | IDCAMS |
| 29 | TRANCATG.jcl | Data Refresh | Define transaction category VSAM | IDCAMS |
| 30 | DALYREJS.jcl | Data Refresh | Define daily rejects file | IDCAMS |
| 31 | CBEXPORT.jcl | Batch Processing | Export all VSAM data to sequential | CBEXPORT |
| 32 | CBIMPORT.jcl | Batch Processing | Import sequential data to VSAM | CBIMPORT |
| 33 | CBADMCDJ.jcl | Batch Processing | Admin card operations | Various |
| 34 | WAITSTEP.jcl | Utility | Wait/delay step (centiseconds) | COBSWAIT |
| 35 | FTPJCL.JCL | Utility | FTP file transfer | FTP |
| 36 | TXT2PDF1.JCL | Utility | Convert text to PDF | TXT2PDF |
| 37 | INTRDRJ1.JCL | Utility | Internal reader — trigger job | IEBGENER |
| 38 | INTRDRJ2.JCL | Utility | Internal reader — triggered job | IDCAMS |

### Batch Processing Cycle Order

```
CLOSEFIL → Data Refresh Jobs → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

### Optional Module JCL Jobs (8)

**Authorization Module (5):**

| # | JCL Job | Description |
|---|---------|-------------|
| 1 | CBPAUP0J.jcl | Batch purge authorization records |
| 2 | DBPAUTP0.jcl | DB2 authorization table provisioning |
| 3 | LOADPADB.JCL | Load IMS payment auth database |
| 4 | UNLDGSAM.JCL | Unload GSAM to sequential |
| 5 | UNLDPADB.JCL | Unload payment auth IMS database |

**Transaction Type DB2 Module (3):**

| # | JCL Job | Description |
|---|---------|-------------|
| 1 | CREADB21.jcl | Create DB2 tables for transaction types |
| 2 | MNTTRDB2.jcl | Maintain transaction type DB2 data |
| 3 | TRANEXTR.jcl | Extract transaction types from DB2 |

---

## Assembler Programs

| # | Program | Function | Called By |
|---|---------|----------|-----------|
| 1 | COBDATFT.asm | Date formatting utility | CBACT01C |
| 2 | MVSWAIT.asm | Programmable wait/delay | COBSWAIT |

---

## JCL Procedures

| # | Procedure | Function |
|---|-----------|----------|
| 1 | REPROC.prc | Reusable REPRO (VSAM unload to sequential) |
| 2 | TRANREPT.prc | Transaction report generation procedure |

---

## Scheduler Configurations

| # | Config | Scheduler | Description |
|---|--------|-----------|-------------|
| 1 | CardDemo.ca7 | CA-7 | Job scheduling definitions for CA-7 |
| 2 | CardDemo.controlm | Control-M | Job scheduling definitions for Control-M |

---

## Asset Counts

| Category | Count |
|----------|-------|
| **Core COBOL Programs** | 31 |
| **Optional Module COBOL Programs** | 13 |
| **Core Copybooks (Data)** | 30 |
| **Optional Module Copybooks** | 11 |
| **Core BMS Maps** | 17 |
| **Optional Module BMS Maps** | 4 |
| **Core BMS Copybooks** | 17 |
| **Core JCL Jobs** | 38 |
| **Optional Module JCL Jobs** | 8 |
| **Assembler Programs** | 2 |
| **JCL Procedures** | 2 |
| **Scheduler Configs** | 2 |
| **Total Lines of COBOL** | ~30,175 |
| **Largest Program** | COACTUPC (4,237 lines) |
| **Smallest Program** | COBSWAIT (41 lines) |
