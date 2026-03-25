# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Platform:** z/OS Mainframe | COBOL / CICS / VSAM / JCL / BMS

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs](#cobol-programs)
   - [Online (CICS) Programs](#online-cics-programs)
   - [Batch Programs](#batch-programs)
   - [Utility / Subroutine Programs](#utility--subroutine-programs)
3. [Copybooks](#copybooks)
   - [Data Structure Copybooks](#data-structure-copybooks)
   - [UI / Presentation Copybooks](#ui--presentation-copybooks)
   - [Utility Copybooks](#utility-copybooks)
4. [BMS Maps](#bms-maps)
5. [JCL Jobs](#jcl-jobs)
   - [Data Load / Refresh Jobs](#data-load--refresh-jobs)
   - [Batch Processing Jobs](#batch-processing-jobs)
   - [Utility / Infrastructure Jobs](#utility--infrastructure-jobs)
6. [Optional Extension Modules](#optional-extension-modules)
   - [Authorization (IMS/DB2/MQ)](#authorization-imsdb2mq)
   - [Transaction Type (DB2)](#transaction-type-db2)
   - [VSAM-MQ Integration](#vsam-mq-integration)
7. [Other Assets](#other-assets)

---

## Executive Summary

| Asset Type | Count | Location |
|---|---|---|
| COBOL Programs (core) | 31 | `app/cbl/` |
| Copybooks (data) | 30 | `app/cpy/` |
| BMS Maps | 17 | `app/bms/` |
| BMS-generated Copybooks | 17 | `app/cpy-bms/` |
| JCL Jobs | 38 | `app/jcl/` |
| Assembler Programs | 2 | `app/asm/` |
| JCL Procedures | 2 | `app/proc/` |
| Extension Programs | 13 | `app/app-*/cbl/` |
| **Total Source Artifacts** | **~150** | |

CardDemo is a credit card management demonstration application designed for mainframe modernization workshops. It supports two user roles (**Admin** and **Regular User**) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

---

## COBOL Programs

### Online (CICS) Programs

These programs run under CICS and handle interactive 3270 terminal sessions. Naming convention: `CO*` prefix.

| # | Program ID | File | LOC | Transaction ID | Function | Business Domain | Screen Map |
|---|---|---|---|---|---|---|---|
| 1 | COSGN00C | `COSGN00C.cbl` | 260 | CC00 | Sign-on / Authentication | Security | COSGN00 |
| 2 | COMEN01C | `COMEN01C.cbl` | 308 | CM00 | Main Menu (Regular User) | Navigation | COMEN01 |
| 3 | COADM01C | `COADM01C.cbl` | 288 | CA00 | Admin Menu | Navigation (Admin) | COADM01 |
| 4 | COACTVWC | `COACTVWC.cbl` | 941 | CA01 | Account View | Account Mgmt | COACTVW |
| 5 | COACTUPC | `COACTUPC.cbl` | 4,236 | CA02 | Account Update | Account Mgmt | COACTUP |
| 6 | COCRDLIC | `COCRDLIC.cbl` | 1,459 | CC01 | Card List (Browse) | Card Mgmt | COCRDLI |
| 7 | COCRDSLC | `COCRDSLC.cbl` | 887 | CC02 | Card Detail View | Card Mgmt | COCRDSL |
| 8 | COCRDUPC | `COCRDUPC.cbl` | 1,560 | CC03 | Card Update | Card Mgmt | COCRDUP |
| 9 | COTRN00C | `COTRN00C.cbl` | 699 | CT00 | Transaction List (Browse) | Transaction Mgmt | COTRN00 |
| 10 | COTRN01C | `COTRN01C.cbl` | 330 | CT01 | Transaction Detail View | Transaction Mgmt | COTRN01 |
| 11 | COTRN02C | `COTRN02C.cbl` | 783 | CT02 | Transaction Add | Transaction Mgmt | COTRN02 |
| 12 | CORPT00C | `CORPT00C.cbl` | 649 | CR00 | Transaction Report Request | Reporting | CORPT00 |
| 13 | COBIL00C | `COBIL00C.cbl` | 572 | CB00 | Bill Payment | Bill Payment | COBIL00 |
| 14 | COUSR00C | `COUSR00C.cbl` | 695 | CU00 | User List (Admin) | User Admin | COUSR00 |
| 15 | COUSR01C | `COUSR01C.cbl` | 299 | CU01 | User Add (Admin) | User Admin | COUSR01 |
| 16 | COUSR02C | `COUSR02C.cbl` | 414 | CU02 | User Update (Admin) | User Admin | COUSR02 |
| 17 | COUSR03C | `COUSR03C.cbl` | 359 | CU03 | User Delete (Admin) | User Admin | COUSR03 |

### Batch Programs

These programs run in batch mode via JCL. Naming convention: `CB*` prefix.

| # | Program ID | File | LOC | Function | Business Domain | Key Files Accessed |
|---|---|---|---|---|---|---|
| 18 | CBACT01C | `CBACT01C.cbl` | 430 | Read Account Data (multi-format export) | Account Mgmt | ACCTDATA VSAM |
| 19 | CBACT02C | `CBACT02C.cbl` | 178 | Read Card Data | Card Mgmt | CARDDATA VSAM |
| 20 | CBACT03C | `CBACT03C.cbl` | 178 | Read Cross-Reference Data | Card Mgmt | CARDXREF VSAM |
| 21 | CBACT04C | `CBACT04C.cbl` | 652 | Interest Calculation | Financial Processing | TCATBALF, CARDXREF, ACCTDATA, DISCGRP |
| 22 | CBCUS01C | `CBCUS01C.cbl` | 178 | Read Customer Data | Customer Mgmt | CUSTDATA VSAM |
| 23 | CBTRN01C | `CBTRN01C.cbl` | 494 | Read Transaction Data (sorted output) | Transaction Mgmt | TRANSACT VSAM |
| 24 | CBTRN02C | `CBTRN02C.cbl` | 731 | Transaction Posting (daily to master) | Transaction Mgmt | TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF |
| 25 | CBTRN03C | `CBTRN03C.cbl` | 649 | Daily Transaction Report | Reporting | TRANSACT, CARDXREF, TRANTYPE, TRANCATG |
| 26 | CBSTM03A | `CBSTM03A.CBL` | 924 | Statement Generation (text + HTML) | Reporting | TRXFL, XREFFILE, ACCTFILE, CUSTFILE |
| 27 | CBSTM03B | `CBSTM03B.CBL` | 230 | Statement Subroutine (file I/O handler) | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE |
| 28 | CBEXPORT | `CBEXPORT.cbl` | 582 | Data Export (VSAM to sequential) | Data Management | Multiple VSAM files |
| 29 | CBIMPORT | `CBIMPORT.cbl` | 487 | Data Import (sequential to VSAM) | Data Management | Multiple VSAM files |

### Utility / Subroutine Programs

| # | Program ID | File | LOC | Function | Called By |
|---|---|---|---|---|---|
| 30 | CSUTLDTC | `CSUTLDTC.cbl` | 157 | Date Validation (CEEDAYS wrapper) | CORPT00C, COTRN02C |
| 31 | COBSWAIT | `COBSWAIT.cbl` | 41 | Wait Utility (calls MVSWAIT) | WAITSTEP JCL |

---

## Copybooks

### Data Structure Copybooks

These define VSAM record layouts and business data structures. Naming convention: `CV*` = VSAM data, `CS*` = system/shared.

| # | Copybook | Record Length | Function | Business Entity | Key Fields |
|---|---|---|---|---|---|
| 1 | CVACT01Y | 300 bytes | Account master record | Account | ACCT-ID(11), STATUS, LIMITS, BALANCES |
| 2 | CVACT02Y | 150 bytes | Card data record | Card | CARD-NUM(16), ACCT-ID(11), CVV, STATUS |
| 3 | CVACT03Y | 50 bytes | Card cross-reference | Card-Account Link | XREF-CARD-NUM(16), XREF-ACCT-ID(11) |
| 4 | CVCUS01Y | 500 bytes | Customer master record | Customer | CUST-ID(9), NAME, ADDRESS, SSN, DOB |
| 5 | CVCRD01Y | -- | Card detail (display format) | Card | Used by online card programs |
| 6 | CVTRA01Y | 50 bytes | Transaction category balance | Tran Cat Balance | ACCT-ID, TYPE-CD, CAT-CD, BALANCE |
| 7 | CVTRA02Y | 50 bytes | Disclosure group | Disclosure | GROUP-ID, TRAN-TYPE, INT-RATE |
| 8 | CVTRA03Y | 60 bytes | Transaction type | Transaction Type | TYPE(2), DESCRIPTION(50) |
| 9 | CVTRA04Y | 60 bytes | Transaction category type | Transaction Category | TYPE-CD(2), CAT-CD(4), DESCRIPTION |
| 10 | CVTRA05Y | 350 bytes | Transaction record (online) | Transaction | TRAN-ID(16), CARD-NUM(16), AMT, MERCHANT |
| 11 | CVTRA06Y | 350 bytes | Daily transaction record | Daily Transaction | Same layout as CVTRA05Y |
| 12 | CVTRA07Y | -- | Transaction report layout | Report | Headers, detail lines, totals |
| 13 | COSTM01 | 350 bytes | Altered transaction layout (for statements) | Transaction (stmt) | CARD-NUM+TRAN-ID key, rest same |
| 14 | CUSTREC | -- | Customer record (alt layout) | Customer | Used by statement programs |
| 15 | CVEXPORT | -- | Export record layouts | Export/Import | Multi-file export structures |
| 16 | CSUSR01Y | 80 bytes | User security record | User Security | USER-ID(8), NAME, PASSWORD, TYPE |
| 17 | UNUSED1Y | 80 bytes | Unused placeholder | (Unused) | Placeholder data structure |

### UI / Presentation Copybooks

| # | Copybook | Function | Used By |
|---|---|---|---|
| 18 | COCOM01Y | Common communication area (COMMAREA) | All online programs |
| 19 | COMEN02Y | Menu option definitions | COMEN01C |
| 20 | COADM02Y | Admin menu option definitions | COADM01C |
| 21 | COTTL01Y | Screen title/header definitions | All online programs |
| 22 | CSDAT01Y | Date formatting for screens | All online programs |
| 23 | CSMSG01Y | Message area definitions (line 1) | All online programs |
| 24 | CSMSG02Y | Message area definitions (line 2) | Most online programs |

### Utility Copybooks

| # | Copybook | Function | Used By |
|---|---|---|---|
| 25 | CODATECN | Date conversion record | CBACT01C (COBDATFT call) |
| 26 | CSLKPCDY | Lookup code definitions | COACTUPC |
| 27 | CSSETATY | Set attribute utility (REPLACING) | COACTUPC (field-level attr control) |
| 28 | CSSTRPFY | String/strip function utility | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 29 | CSUTLDPY | Date utility parameters | COACTUPC |
| 30 | CSUTLDWY | Date utility working storage | COACTUPC |

---

## BMS Maps

BMS (Basic Mapping Support) maps define 3270 terminal screen layouts. Each map has a corresponding generated copybook in `app/cpy-bms/`.

| # | Map Name | File | Map Set | Screen Title | Fields | Associated Program |
|---|---|---|---|---|---|---|
| 1 | COSGN00 | `COSGN00.bms` | COSGN00 | Sign On | User ID, Password | COSGN00C |
| 2 | COMEN01 | `COMEN01.bms` | COMEN01 | Main Menu | 14 menu options | COMEN01C |
| 3 | COADM01 | `COADM01.bms` | COADM01 | Admin Menu | Admin options | COADM01C |
| 4 | COACTVW | `COACTVW.bms` | COACTVW | View Account | Account details (read-only) | COACTVWC |
| 5 | COACTUP | `COACTUP.bms` | COACTUP | Update Account | Editable account fields | COACTUPC |
| 6 | COCRDLI | `COCRDLI.bms` | COCRDLI | Card List | Scrollable card list | COCRDLIC |
| 7 | COCRDSL | `COCRDSL.bms` | COCRDSL | View Card | Card details (read-only) | COCRDSLC |
| 8 | COCRDUP | `COCRDUP.bms` | COCRDUP | Update Card | Editable card fields | COCRDUPC |
| 9 | COTRN00 | `COTRN00.bms` | COTRN00 | Transaction List | Scrollable transaction list | COTRN00C |
| 10 | COTRN01 | `COTRN01.bms` | COTRN01 | View Transaction | Transaction details (read-only) | COTRN01C |
| 11 | COTRN02 | `COTRN02.bms` | COTRN02 | Add Transaction | New transaction entry form | COTRN02C |
| 12 | CORPT00 | `CORPT00.bms` | CORPT00 | Transaction Report | Date range, report type | CORPT00C |
| 13 | COBIL00 | `COBIL00.bms` | COBIL00 | Bill Payment | Account, amount, confirmation | COBIL00C |
| 14 | COUSR00 | `COUSR00.bms` | COUSR00 | User List | Scrollable user list (Admin) | COUSR00C |
| 15 | COUSR01 | `COUSR01.bms` | COUSR01 | Add User | New user form (Admin) | COUSR01C |
| 16 | COUSR02 | `COUSR02.bms` | COUSR02 | Update User | Edit user form (Admin) | COUSR02C |
| 17 | COUSR03 | `COUSR03.bms` | COUSR03 | Delete User | Confirm delete (Admin) | COUSR03C |

---

## JCL Jobs

### Data Load / Refresh Jobs

These jobs define, load, and refresh VSAM datasets from flat files.

| # | Job Name | File | Function | Target Dataset(s) | Key Utility |
|---|---|---|---|---|---|
| 1 | ACCTFILE | `ACCTFILE.jcl` | Refresh Account Master VSAM | ACCTDATA.VSAM.KSDS | IDCAMS REPRO |
| 2 | CARDFILE | `CARDFILE.jcl` | Refresh Card Master VSAM | CARDDATA.VSAM.KSDS | IDCAMS REPRO |
| 3 | CUSTFILE | `CUSTFILE.jcl` | Refresh Customer Master VSAM | CUSTDATA.VSAM.KSDS | IDCAMS REPRO |
| 4 | XREFFILE | `XREFFILE.jcl` | Load Card Cross-Reference + AIX | CARDXREF.VSAM.KSDS + AIX | IDCAMS DEFINE/REPRO/BLDINDEX |
| 5 | TRANFILE | `TRANFILE.jcl` | Load Transaction Master VSAM | TRANSACT.VSAM.KSDS + AIX | IDCAMS DEFINE/REPRO/BLDINDEX |
| 6 | DUSRSECJ | `DUSRSECJ.jcl` | Load User Security VSAM | USRSEC.VSAM.KSDS | IEBGENER + IDCAMS |
| 7 | TCATBALF | `TCATBALF.jcl` | Load Tran Category Balance VSAM | TCATBALF.VSAM.KSDS | IDCAMS |
| 8 | TRANTYPE | `TRANTYPE.jcl` | Load Transaction Type VSAM | TRANTYPE.VSAM.KSDS | IDCAMS |
| 9 | TRANCATG | `TRANCATG.jcl` | Load Transaction Category VSAM | TRANCATG.VSAM.KSDS | IDCAMS |
| 10 | DISCGRP | `DISCGRP.jcl` | Load Disclosure Group VSAM | DISCGRP.VSAM.KSDS | IDCAMS |
| 11 | ESDSRRDS | `ESDSRRDS.jcl` | Load ESDS/RRDS test files | USRSEC.VSAM.ESDS/RRDS | IDCAMS |
| 12 | DEFCUST | `DEFCUST.jcl` | Define Customer VSAM cluster | CUSTDATA.VSAM.KSDS | IDCAMS DEFINE |
| 13 | REPTFILE | `REPTFILE.jcl` | Define Report VSAM file | Report dataset | IDCAMS |

### Batch Processing Jobs

These jobs execute COBOL batch programs for business processing.

| # | Job Name | File | Function | COBOL Program | Key Datasets |
|---|---|---|---|---|---|
| 14 | POSTTRAN | `POSTTRAN.jcl` | Post daily transactions to master | CBTRN02C | TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF |
| 15 | INTCALC | `INTCALC.jcl` | Calculate interest on balances | CBACT04C | TCATBALF, CARDXREF, ACCTDATA, DISCGRP |
| 16 | TRANREPT | `TRANREPT.jcl` | Generate daily transaction report | CBTRN03C (+ SORT) | TRANSACT, CARDXREF, TRANTYPE, TRANCATG |
| 17 | CREASTMT | `CREASTMT.JCL` | Create account statements (text+HTML) | CBSTM03A | TRXFL, CARDXREF, ACCTDATA, CUSTDATA |
| 18 | READACCT | `READACCT.jcl` | Export account data (multi-format) | CBACT01C | ACCTDATA VSAM |
| 19 | READCARD | `READCARD.jcl` | Export card data | CBACT02C | CARDDATA VSAM |
| 20 | READCUST | `READCUST.jcl` | Export customer data | CBCUS01C | CUSTDATA VSAM |
| 21 | READXREF | `READXREF.jcl` | Export cross-reference data | CBACT03C | CARDXREF VSAM |
| 22 | CBEXPORT | `CBEXPORT.jcl` | Batch data export | CBEXPORT | Multiple VSAM |
| 23 | CBIMPORT | `CBIMPORT.jcl` | Batch data import | CBIMPORT | Multiple VSAM |
| 24 | WAITSTEP | `WAITSTEP.jcl` | Wait step (scheduling delay) | COBSWAIT | -- |

### Utility / Infrastructure Jobs

| # | Job Name | File | Function | Key Utility |
|---|---|---|---|---|
| 25 | CLOSEFIL | `CLOSEFIL.jcl` | Close CICS files for batch | SDSF |
| 26 | OPENFIL | `OPENFIL.jcl` | Re-open CICS files after batch | SDSF |
| 27 | TRANBKP | `TRANBKP.jcl` | Backup transaction VSAM to GDG | IDCAMS REPRO |
| 28 | COMBTRAN | `COMBTRAN.jcl` | Combine daily + master transactions | IDCAMS/SORT |
| 29 | TRANIDX | `TRANIDX.jcl` | Rebuild transaction alternate indexes | IDCAMS BLDINDEX |
| 30 | DEFGDGB | `DEFGDGB.jcl` | Define GDG base (backup) | IDCAMS DEFINE GDG |
| 31 | DEFGDGD | `DEFGDGD.jcl` | Define GDG base (daily) | IDCAMS DEFINE GDG |
| 32 | DALYREJS | `DALYREJS.jcl` | Define daily rejects GDG | IDCAMS DEFINE GDG |
| 33 | PRTCATBL | `PRTCATBL.jcl` | Print category balance report | SORT |
| 34 | FTPJCL | `FTPJCL.JCL` | FTP file transfer to/from mainframe | FTP |
| 35 | INTRDRJ1 | `INTRDRJ1.JCL` | Internal reader job trigger (step 1) | IEBGENER + INTRDR |
| 36 | INTRDRJ2 | `INTRDRJ2.JCL` | Internal reader job trigger (step 2) | IDCAMS |
| 37 | TXT2PDF1 | `TXT2PDF1.JCL` | Convert statement text to PDF | TXT2PDF (REXX) |
| 38 | CBADMCDJ | `CBADMCDJ.jcl` | Admin card demo utility | -- |

### Batch Cycle Execution Order

```
CLOSEFIL --> Data Refresh (ACCTFILE, CARDFILE, etc.)
         --> POSTTRAN (transaction posting)
         --> INTCALC (interest calculation)
         --> TRANBKP (backup)
         --> COMBTRAN (combine)
         --> CREASTMT (statements)
         --> TRANIDX (rebuild indexes)
         --> OPENFIL
```

---

## Optional Extension Modules

### Authorization (IMS/DB2/MQ)

Location: `app/app-authorization-ims-db2-mq/`

| # | Program | LOC | Type | Function |
|---|---|---|---|---|
| 1 | COPAUA0C | -- | Online | MQ trigger for authorization |
| 2 | COPAUS0C | -- | Online | Authorization summary display |
| 3 | COPAUS1C | -- | Online | Authorization detail display |
| 4 | COPAUS2C | -- | Online | Mark fraud to DB2 |
| 5 | CBPAUP0C | -- | Batch | Purge old authorizations |
| 6 | PAUDBLOD | -- | Batch | Load authorization DB |
| 7 | PAUDBUNL | -- | Batch | Unload authorization DB |
| 8 | DBUNLDGS | -- | Batch | Unload GSAM |

Additional assets: 2 BMS maps, 7 copybooks, 5 JCL jobs, IMS DBD/PSB definitions, DB2 DDL.

### Transaction Type (DB2)

Location: `app/app-transaction-type-db2/`

| # | Program | Type | Function |
|---|---|---|---|
| 1 | COTRTUPC | Online | Add/Edit transaction types (DB2) |
| 2 | COTRTLIC | Online | List/Delete transaction types (DB2) |
| 3 | COBTUPDT | Batch | Batch update transaction types |

Additional assets: 2 BMS maps, 2 copybooks, 3 JCL jobs, DB2 DDL.

### VSAM-MQ Integration

Location: `app/app-vsam-mq/`

| # | Program | Type | Function |
|---|---|---|---|
| 1 | CODATE01 | Online | System date via MQ request/response |
| 2 | COACCT01 | Online | Account inquiry via MQ request/response |

---

## Other Assets

| Asset Type | Location | Count | Description |
|---|---|---|---|
| Assembler Programs | `app/asm/` | 2 | MVSWAIT (wait utility), COBDATFT (date formatting) |
| JCL Procedures | `app/proc/` | 2 | Reusable JCL procedures |
| CSD Definitions | `app/csd/` | 1 | CICS resource definitions (CARDDEMO.CSD) |
| Control Files | `app/ctl/` | -- | Control/parameter files |
| Catalog Listings | `app/catlg/` | -- | Dataset catalog information |
| Assembler Macros | `app/maclib/` | -- | Macro library |
| Scheduler Configs | `app/scheduler/` | -- | CA7 and Control-M job scheduling definitions |
| Sample Data (ASCII) | `app/data/ASCII/` | -- | Test data in ASCII format |
| Sample Data (EBCDIC) | `app/data/EBCDIC/` | -- | Test data in EBCDIC for mainframe upload |
| Shell Scripts | `scripts/` | 10 | FTP-based mainframe interaction scripts |
| Sample JCL/Config | `samples/` | -- | Compilation JCL and AWS M2 configs |
| Architecture Diagrams | `diagrams/` | -- | System diagrams and screen captures |
