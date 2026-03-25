# CardDemo Application Inventory

> **Generated from:** `Cognition-Partner-Workshops/uc-legacy-modernization-cobol-to-java`
> **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Technology Stack:** COBOL / CICS / VSAM / JCL / BMS (3270)

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [COBOL Programs — Core (`app/cbl/`)](#cobol-programs--core)
3. [COBOL Programs — Optional Modules](#cobol-programs--optional-modules)
4. [Copybooks — Data Structures (`app/cpy/`)](#copybooks--data-structures)
5. [Copybooks — Optional Modules](#copybooks--optional-modules)
6. [BMS Maps — Screen Definitions (`app/bms/`)](#bms-maps--screen-definitions)
7. [BMS Maps — Optional Modules](#bms-maps--optional-modules)
8. [BMS-Generated Copybooks (`app/cpy-bms/`)](#bms-generated-copybooks)
9. [JCL Jobs — Core (`app/jcl/`)](#jcl-jobs--core)
10. [JCL Jobs — Optional Modules](#jcl-jobs--optional-modules)
11. [Assembler Programs (`app/asm/`)](#assembler-programs)
12. [JCL Procedures (`app/proc/`)](#jcl-procedures)
13. [Supporting Artifacts](#supporting-artifacts)

---

## Summary Statistics

| Artifact Type              | Core | Optional Modules | Total |
|----------------------------|------|-------------------|-------|
| COBOL Programs             | 31   | 13                | 44    |
| Copybooks (Data)           | 30   | 11                | 41    |
| BMS Screen Maps            | 17   | 4                 | 21    |
| BMS-Generated Copybooks    | 17   | 4                 | 21    |
| JCL Jobs                   | 38   | 8                 | 46    |
| Assembler Programs         | 2    | —                 | 2     |
| JCL Procedures             | 2    | —                 | 2     |
| **Grand Total**            |      |                   | **177** |

---

## COBOL Programs — Core

### Online CICS Programs (Prefix: `CO*`)

These run under CICS and serve 3270 terminal users in real time.

| # | Program | Lines | Type | Function | Transaction | BMS Map | Business Domain |
|---|---------|-------|------|----------|-------------|---------|-----------------|
| 1 | **COSGN00C.cbl** | 260 | Online | Sign-on / Authentication | CC00 | COSGN00 | Security |
| 2 | **COMEN01C.cbl** | 308 | Online | Main Menu (Regular Users) | CM00 | COMEN01 | Navigation |
| 3 | **COADM01C.cbl** | 288 | Online | Admin Menu | CA00 | COADM01 | Navigation / Admin |
| 4 | **COACTVWC.cbl** | 941 | Online | Account View | CA01 | COACTVW | Account Mgmt |
| 5 | **COACTUPC.cbl** | 4,236 | Online | Account Update | CA02 | COACTUP | Account Mgmt |
| 6 | **COCRDLIC.cbl** | 1,459 | Online | Credit Card List | CC01 | COCRDLI | Card Mgmt |
| 7 | **COCRDSLC.cbl** | 887 | Online | Credit Card View (Detail) | CC02 | COCRDSL | Card Mgmt |
| 8 | **COCRDUPC.cbl** | 1,560 | Online | Credit Card Update | CC03 | COCRDUP | Card Mgmt |
| 9 | **COTRN00C.cbl** | 699 | Online | Transaction List | CT00 | COTRN00 | Transactions |
| 10 | **COTRN01C.cbl** | 330 | Online | Transaction View | CT01 | COTRN01 | Transactions |
| 11 | **COTRN02C.cbl** | 783 | Online | Transaction Add | CT02 | COTRN02 | Transactions |
| 12 | **CORPT00C.cbl** | 649 | Online | Transaction Reports (submit batch via TDQ) | CR00 | CORPT00 | Reporting |
| 13 | **COBIL00C.cbl** | 572 | Online | Bill Payment | CB00 | COBIL00 | Billing / Payments |
| 14 | **COUSR00C.cbl** | 695 | Online | User List (Admin) | CU00 | COUSR00 | User Mgmt / Security |
| 15 | **COUSR01C.cbl** | 299 | Online | User Add (Admin) | CU01 | COUSR01 | User Mgmt / Security |
| 16 | **COUSR02C.cbl** | 414 | Online | User Update (Admin) | CU02 | COUSR02 | User Mgmt / Security |
| 17 | **COUSR03C.cbl** | 359 | Online | User Delete (Admin) | CU03 | COUSR03 | User Mgmt / Security |

### Batch Programs (Prefix: `CB*`)

These run in JCL batch jobs for scheduled or background processing.

| # | Program | Lines | Type | Function | Invoking JCL | Business Domain |
|---|---------|-------|------|----------|-------------|-----------------|
| 18 | **CBACT01C.cbl** | 430 | Batch | Read / Dump Account File | READACCT | Account Mgmt |
| 19 | **CBACT02C.cbl** | 178 | Batch | Read / Dump Card File | READCARD | Card Mgmt |
| 20 | **CBACT03C.cbl** | 178 | Batch | Read / Dump Cross-Reference File | READXREF | Card / Account Link |
| 21 | **CBACT04C.cbl** | 652 | Batch | Interest Calculation | INTCALC | Financial Calculation |
| 22 | **CBCUS01C.cbl** | 178 | Batch | Read / Dump Customer File | READCUST | Customer Mgmt |
| 23 | **CBTRN01C.cbl** | 494 | Batch | Daily Transaction File Processing | — | Transactions |
| 24 | **CBTRN02C.cbl** | 731 | Batch | Transaction Posting (Daily → Master) | POSTTRAN | Transactions |
| 25 | **CBTRN03C.cbl** | 649 | Batch | Transaction Report Generation | TRANREPT | Reporting |
| 26 | **CBSTM03A.CBL** | 924 | Batch | Account Statement Generation (Text + HTML) | CREASTMT | Reporting / Statements |
| 27 | **CBSTM03B.CBL** | 230 | Batch Sub | File-processing subroutine for CBSTM03A | CREASTMT | Reporting / Statements |
| 28 | **CBEXPORT.cbl** | 582 | Batch | Data Export (VSAM → Sequential) | CBEXPORT | Data Migration |
| 29 | **CBIMPORT.cbl** | 487 | Batch | Data Import (Sequential → VSAM) | CBIMPORT | Data Migration |

### Utility Programs

| # | Program | Lines | Type | Function | Invoking JCL | Business Domain |
|---|---------|-------|------|----------|-------------|-----------------|
| 30 | **CSUTLDTC.cbl** | 157 | Utility | Date Conversion (calls CEEDAYS) | — | Cross-cutting |
| 31 | **COBSWAIT.cbl** | 41 | Utility | Wait/Timer (calls MVSWAIT) | WAITSTEP | Cross-cutting |

---

## COBOL Programs — Optional Modules

### Authorization Module — IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program | Type | Function | Business Domain |
|---|---------|------|----------|-----------------|
| 32 | **COPAUA0C.cbl** | Online | MQ Trigger — Authorization Request Router | Authorization |
| 33 | **COPAUS0C.cbl** | Online | Pending Authorization Summary View | Authorization |
| 34 | **COPAUS1C.cbl** | Online | Pending Authorization Detail View | Authorization |
| 35 | **COPAUS2C.cbl** | Online | Fraud Marking (writes to DB2) | Fraud Detection |
| 36 | **CBPAUP0C.cbl** | Batch | Purge Processed Authorizations | Authorization |
| 37 | **PAUDBLOD.CBL** | Batch | Load Authorization Data to IMS DB | Authorization |
| 38 | **PAUDBUNL.CBL** | Batch | Unload Authorization Data from IMS DB | Authorization |
| 39 | **DBUNLDGS.CBL** | Batch | Unload GSAM (Generalized Sequential Access Method) | Authorization |

### Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program | Type | Function | Business Domain |
|---|---------|------|----------|-----------------|
| 40 | **COTRTLIC.cbl** | Online | Transaction Type List / Delete (DB2 cursor) | Reference Data |
| 41 | **COTRTUPC.cbl** | Online | Transaction Type Add / Edit (DB2 CRUD) | Reference Data |
| 42 | **COBTUPDT.cbl** | Batch | Batch Update Transaction Types in DB2 | Reference Data |

### VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program | Type | Function | Business Domain |
|---|---------|------|----------|-----------------|
| 43 | **CODATE01.cbl** | MQ Server | System Date Service (request/reply via MQ) | Cross-cutting |
| 44 | **COACCT01.cbl** | MQ Server | Account Inquiry Service (request/reply via MQ) | Account Mgmt |

---

## Copybooks — Data Structures

### VSAM Record Layouts (Prefix: `CV*`)

| # | Copybook | Record Length | Business Entity | Description |
|---|----------|-------------|-----------------|-------------|
| 1 | **CVACT01Y.cpy** | 300 bytes | Account | Account master record |
| 2 | **CVACT02Y.cpy** | 150 bytes | Card | Card data record |
| 3 | **CVACT03Y.cpy** | 50 bytes | Card Cross-Reference | Links cards ↔ accounts |
| 4 | **CVCUS01Y.cpy** | 500 bytes | Customer | Customer demographic record |
| 5 | **CVCRD01Y.cpy** | — | Card (Internal) | Card data for internal programs |
| 6 | **CVTRA01Y.cpy** | 50 bytes | Transaction Cat Balance | Transaction category balance |
| 7 | **CVTRA02Y.cpy** | 50 bytes | Disclosure Group | Disclosure group / interest rates |
| 8 | **CVTRA03Y.cpy** | 60 bytes | Transaction Type | Transaction type master |
| 9 | **CVTRA04Y.cpy** | 60 bytes | Transaction Category | Transaction category type |
| 10 | **CVTRA05Y.cpy** | 350 bytes | Transaction | Online transaction record |
| 11 | **CVTRA06Y.cpy** | 350 bytes | Daily Transaction | Daily transaction (incoming) |
| 12 | **CVTRA07Y.cpy** | — | Report Layout | Transaction report data structures |
| 13 | **CVEXPORT.cpy** | 500+ bytes | Export Record | Export flat-file record layout |

### Application Control / Communication Copybooks (Prefix: `CO*`, `CS*`, `CM*`)

| # | Copybook | Business Entity | Description |
|---|----------|-----------------|-------------|
| 14 | **COCOM01Y.cpy** | Communication Area | CICS COMMAREA — passes context between programs |
| 15 | **COMEN02Y.cpy** | Menu Options | Main menu option definitions (11 options) |
| 16 | **COADM02Y.cpy** | Admin Menu Options | Admin menu option definitions (6 options) |
| 17 | **COTTL01Y.cpy** | Screen Title | Application title / header text |
| 18 | **CODATECN.cpy** | Date Conversion | Date conversion input/output record |
| 19 | **COSTM01.CPY** | Statement Transaction | Altered transaction layout for statement reporting |
| 20 | **CUSTREC.cpy** | Customer (Statement) | Customer record layout used in statement generation |

### Shared Service Copybooks

| # | Copybook | Function | Description |
|---|----------|----------|-------------|
| 21 | **CSUSR01Y.cpy** | User Security Record | User ID, name, password, type (Admin/Regular) |
| 22 | **CSDAT01Y.cpy** | Date/Time Work Area | Working storage for date formatting |
| 23 | **CSMSG01Y.cpy** | Message Area | User message / info message display |
| 24 | **CSMSG02Y.cpy** | Message Area (Alt) | Extended message area |
| 25 | **CSSETATY.cpy** | Set Attribute | BMS field attribute setting (COPY REPLACING) |
| 26 | **CSSTRPFY.cpy** | String Padding/Formatting | String padding utility |
| 27 | **CSUTLDPY.cpy** | Date Utility Parms | Parameters for CSUTLDTC date utility |
| 28 | **CSUTLDWY.cpy** | Date Utility Work | Working storage for date utility |
| 29 | **CSLKPCDY.cpy** | Lookup Code | Code lookup working storage |
| 30 | **UNUSED1Y.cpy** | (Unused) | Legacy placeholder — not actively used |

---

## Copybooks — Optional Modules

### Authorization IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 31 | **CCPAUERY.cpy** | Authorization query record |
| 32 | **CCPAURLY.cpy** | Authorization reply record |
| 33 | **CCPAURQY.cpy** | Authorization request record |
| 34 | **CIPAUDTY.cpy** | Authorization detail record |
| 35 | **CIPAUSMY.cpy** | Authorization summary record |
| 36 | **IMSFUNCS.cpy** | IMS function codes |
| 37 | **PADFLPCB.CPY** | IMS PCB (Program Communication Block) — flat file |
| 38 | **PASFLPCB.CPY** | IMS PCB — sequential flat file |
| 39 | **PAUTBPCB.CPY** | IMS PCB — authorization table |

### Transaction Type DB2 (`app/app-transaction-type-db2/cpy/`)

| # | Copybook | Description |
|---|----------|-------------|
| 40 | **CSDB2RPY.cpy** | DB2 reply record for transaction types |
| 41 | **CSDB2RWY.cpy** | DB2 read/write record for transaction types |

---

## BMS Maps — Screen Definitions

Each BMS map defines a 3270 terminal screen layout (24×80 characters).

| # | BMS File | Map Set | Map Name | Associated Program | Screen Function |
|---|----------|---------|----------|--------------------|-----------------|
| 1 | **COSGN00.bms** | COSGN00 | COSGN0A | COSGN00C | Sign-On Screen |
| 2 | **COMEN01.bms** | COMEN01 | COMEN1A | COMEN01C | Main Menu |
| 3 | **COADM01.bms** | COADM01 | COADM1A | COADM01C | Admin Menu |
| 4 | **COACTVW.bms** | COACTVW | COATVWA | COACTVWC | Account View |
| 5 | **COACTUP.bms** | COACTUP | COATUPA | COACTUPC | Account Update |
| 6 | **COCRDLI.bms** | COCRDLI | COCRLIA | COCRDLIC | Credit Card List |
| 7 | **COCRDSL.bms** | COCRDSL | COCRSLA | COCRDSLC | Credit Card Detail View |
| 8 | **COCRDUP.bms** | COCRDUP | COCRUPA | COCRDUPC | Credit Card Update |
| 9 | **COTRN00.bms** | COTRN00 | COTRN0A | COTRN00C | Transaction List |
| 10 | **COTRN01.bms** | COTRN01 | COTRN1A | COTRN01C | Transaction View |
| 11 | **COTRN02.bms** | COTRN02 | COTRN2A | COTRN02C | Transaction Add |
| 12 | **CORPT00.bms** | CORPT00 | CORPT0A | CORPT00C | Transaction Reports |
| 13 | **COBIL00.bms** | COBIL00 | COBIL0A | COBIL00C | Bill Payment |
| 14 | **COUSR00.bms** | COUSR00 | COUSR0A | COUSR00C | User List (Admin) |
| 15 | **COUSR01.bms** | COUSR01 | COUSR1A | COUSR01C | User Add (Admin) |
| 16 | **COUSR02.bms** | COUSR02 | COUSR2A | COUSR02C | User Update (Admin) |
| 17 | **COUSR03.bms** | COUSR03 | COUSR3A | COUSR03C | User Delete (Admin) |

---

## BMS Maps — Optional Modules

| # | BMS File | Module | Associated Program | Screen Function |
|---|----------|--------|--------------------|-----------------|
| 18 | **COPAU00.bms** | Auth IMS/DB2/MQ | COPAUS0C | Pending Authorization Summary |
| 19 | **COPAU01.bms** | Auth IMS/DB2/MQ | COPAUS1C | Pending Authorization Detail |
| 20 | **COTRTLI.bms** | Tran Type DB2 | COTRTLIC | Transaction Type List |
| 21 | **COTRTUP.bms** | Tran Type DB2 | COTRTUPC | Transaction Type Maintenance |

---

## BMS-Generated Copybooks

Located in `app/cpy-bms/`, these are auto-generated from BMS maps and provide COBOL data structures for screen I/O:

`COACTUP.CPY`, `COACTVW.CPY`, `COADM01.CPY`, `COBIL00.CPY`, `COCRDLI.CPY`, `COCRDSL.CPY`, `COCRDUP.CPY`, `COMEN01.CPY`, `CORPT00.CPY`, `COSGN00.CPY`, `COTRN00.CPY`, `COTRN01.CPY`, `COTRN02.CPY`, `COUSR00.CPY`, `COUSR01.CPY`, `COUSR02.CPY`, `COUSR03.CPY`

> **Note:** These mirror the BMS map names and should NOT be manually edited.

---

## JCL Jobs — Core

### Data File Refresh / Definition Jobs

| # | JCL Job | Steps (PGM) | Function | Classification |
|---|---------|-------------|----------|----------------|
| 1 | **ACCTFILE.jcl** | IDCAMS (×3) | Delete/define/reload Account VSAM KSDS | Data Setup |
| 2 | **CARDFILE.jcl** | SDSF, IDCAMS (×5), SDSF | Close CICS, refresh Card VSAM, reopen | Data Setup |
| 3 | **CUSTFILE.jcl** | SDSF, IDCAMS (×3), SDSF | Close CICS, refresh Customer VSAM, reopen | Data Setup |
| 4 | **XREFFILE.jcl** | IDCAMS (×6) | Delete/define/reload Card Cross-Ref VSAM + Alt Index | Data Setup |
| 5 | **TRANFILE.jcl** | SDSF, IDCAMS (×5), SDSF | Close CICS, refresh Transaction VSAM, reopen | Data Setup |
| 6 | **DUSRSECJ.jcl** | IEFBR14, IEBGENER, IDCAMS (×2) | Load User Security VSAM from inline data | Data Setup |
| 7 | **TRANTYPE.jcl** | IDCAMS (×3) | Delete/define/reload Transaction Type VSAM | Data Setup |
| 8 | **TRANCATG.jcl** | IDCAMS (×3) | Delete/define/reload Transaction Category VSAM | Data Setup |
| 9 | **DISCGRP.jcl** | IDCAMS (×3) | Delete/define/reload Disclosure Group VSAM | Data Setup |
| 10 | **TCATBALF.jcl** | IDCAMS (×3) | Delete/define/reload Transaction Cat Balance VSAM | Data Setup |
| 11 | **DALYREJS.jcl** | IDCAMS | Delete Daily Rejects file | Data Setup |
| 12 | **DEFCUST.jcl** | IDCAMS (×2) | Define Customer VSAM clusters | Data Setup |
| 13 | **DEFGDGB.jcl** | IDCAMS | Define GDG Base entries | Data Setup |
| 14 | **DEFGDGD.jcl** | IDCAMS, IEBGENER (×3), IDCAMS (×2) | Define GDG Data entries | Data Setup |
| 15 | **REPTFILE.jcl** | IDCAMS | Define Report VSAM file | Data Setup |
| 16 | **ESDSRRDS.jcl** | IEFBR14, IEBGENER, IDCAMS (×4) | Define ESDS and RRDS test datasets | Data Setup |

### Batch Processing Jobs

| # | JCL Job | Steps (PGM) | Function | Classification |
|---|---------|-------------|----------|----------------|
| 17 | **POSTTRAN.jcl** | **CBTRN02C** | Post daily transactions to master | Core Processing |
| 18 | **INTCALC.jcl** | **CBACT04C** | Calculate interest on accounts | Core Processing |
| 19 | **TRANBKP.jcl** | IDCAMS (×2) | Backup transaction file (REPRO) | Operational |
| 20 | **COMBTRAN.jcl** | SORT, IDCAMS | Combine / sort transactions | Core Processing |
| 21 | **CREASTMT.JCL** | IDCAMS, SORT, IDCAMS, IEFBR14, **CBSTM03A** | Create account statements (text + HTML) | Reporting |
| 22 | **TRANREPT.jcl** | REPROC, SORT, **CBTRN03C** | Generate daily transaction report | Reporting |
| 23 | **PRTCATBL.jcl** | IEFBR14, SORT | Print transaction category balances | Reporting |
| 24 | **TRANIDX.jcl** | IDCAMS (×3) | Define transaction alternate indexes | Operational |

### Utility / Diagnostic Jobs

| # | JCL Job | Steps (PGM) | Function | Classification |
|---|---------|-------------|----------|----------------|
| 25 | **READACCT.jcl** | IEFBR14, **CBACT01C** | Read/dump account VSAM to PS | Diagnostic |
| 26 | **READCARD.jcl** | **CBACT02C** | Read/dump card VSAM | Diagnostic |
| 27 | **READXREF.jcl** | **CBACT03C** | Read/dump cross-reference VSAM | Diagnostic |
| 28 | **READCUST.jcl** | **CBCUS01C** | Read/dump customer VSAM | Diagnostic |
| 29 | **CBEXPORT.jcl** | IDCAMS, **CBEXPORT** | Export all VSAM files to sequential | Data Migration |
| 30 | **CBIMPORT.jcl** | **CBIMPORT** | Import sequential files to VSAM | Data Migration |

### CICS Control Jobs

| # | JCL Job | Steps (PGM) | Function | Classification |
|---|---------|-------------|----------|----------------|
| 31 | **CLOSEFIL.jcl** | SDSF | Close CICS-managed files for batch | Operational |
| 32 | **OPENFIL.jcl** | SDSF | Reopen CICS-managed files after batch | Operational |
| 33 | **CBADMCDJ.jcl** | DFHCSDUP | CICS CSD administration (define resources) | Operational |
| 34 | **WAITSTEP.jcl** | **COBSWAIT** | Timer wait step (inter-job delay) | Operational |

### Miscellaneous / Infrastructure Jobs

| # | JCL Job | Steps (PGM) | Function | Classification |
|---|---------|-------------|----------|----------------|
| 35 | **FTPJCL.JCL** | FTP | FTP transfer between systems | Infrastructure |
| 36 | **INTRDRJ1.JCL** | IDCAMS, IEBGENER | Internal reader test job 1 | Infrastructure |
| 37 | **INTRDRJ2.JCL** | IDCAMS | Internal reader test job 2 | Infrastructure |
| 38 | **TXT2PDF1.JCL** | IKJEFT1B (TXT2PDF REXX) | Convert text statements to PDF | Reporting |

---

## JCL Jobs — Optional Modules

### Authorization IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/jcl/`)

| # | JCL Job | Function |
|---|---------|----------|
| 39 | **CBPAUP0J.jcl** | Purge processed authorizations |
| 40 | **DBPAUTP0.jcl** | Authorization DB2 processing |
| 41 | **LOADPADB.JCL** | Load authorization IMS database |
| 42 | **UNLDGSAM.JCL** | Unload GSAM data |
| 43 | **UNLDPADB.JCL** | Unload authorization IMS database |

### Transaction Type DB2 (`app/app-transaction-type-db2/jcl/`)

| # | JCL Job | Function |
|---|---------|----------|
| 44 | **CREADB21.jcl** | Create DB2 tables for transaction types |
| 45 | **MNTTRDB2.jcl** | Maintain transaction types in DB2 |
| 46 | **TRANEXTR.jcl** | Extract transaction types from DB2 |

---

## Assembler Programs

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | **MVSWAIT.asm** | `app/asm/` | MVS WAIT service — called by COBSWAIT |
| 2 | **COBDATFT.asm** | `app/asm/` | Date formatting utility in Assembler |

---

## JCL Procedures

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | **REPROC.prc** | `app/proc/` | Generic REPRO procedure (VSAM file copy) |
| 2 | **TRANREPT.prc** | `app/proc/` | Transaction report procedure |

---

## Supporting Artifacts

| Artifact | Location | Description |
|----------|----------|-------------|
| CSD Definitions | `app/csd/CARDDEMO.CSD` | CICS resource definitions (transactions, programs, files) |
| Control Files | `app/ctl/REPROCT.ctl` | REPRO control file |
| Scheduler — CA7 | `app/scheduler/CardDemo.ca7` | CA7 job scheduling definitions |
| Scheduler — Control-M | `app/scheduler/CardDemo.controlm` | Control-M job scheduling definitions |
| Sample Data (ASCII) | `app/data/ASCII/` | ASCII test data files for modernized app testing |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | EBCDIC data files for mainframe upload |
| Assembler Macros | `app/maclib/` | Assembler macro library |
| Catalog Listings | `app/catlg/` | Dataset catalog listings |

---

## Classification Legend

| Classification | Description |
|----------------|-------------|
| **Online** | CICS real-time interactive program |
| **Batch** | JCL-driven scheduled/background program |
| **Batch Sub** | Subroutine called by a batch program |
| **MQ Server** | MQ-triggered service program |
| **Utility** | Reusable service / utility |
| **Core Processing** | Business-critical batch processing |
| **Data Setup** | Data file definition / refresh |
| **Operational** | CICS file control / infrastructure |
| **Reporting** | Report generation |
| **Diagnostic** | Debug / data dump utility |
| **Data Migration** | Export / import between formats |
| **Infrastructure** | System-level utility |
