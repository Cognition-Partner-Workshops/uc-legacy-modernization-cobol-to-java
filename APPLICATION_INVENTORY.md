# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL / BMS (3270 Terminals)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [COBOL Programs — Online (CICS)](#cobol-programs--online-cics)
3. [COBOL Programs — Batch](#cobol-programs--batch)
4. [COBOL Programs — Utility / Shared](#cobol-programs--utility--shared)
5. [Optional Extension Modules](#optional-extension-modules)
6. [Copybooks — Data Structures](#copybooks--data-structures)
7. [Copybooks — BMS Screen Maps](#copybooks--bms-screen-maps)
8. [BMS Maps — Terminal Screen Definitions](#bms-maps--terminal-screen-definitions)
9. [JCL Jobs — Batch Processing](#jcl-jobs--batch-processing)
10. [Assembler Programs](#assembler-programs)
11. [JCL Procedures](#jcl-procedures)
12. [Summary Counts](#summary-counts)

---

## Executive Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It simulates account management, card management, transactions, bill payments, and reporting. The system supports two user roles: **Regular** (card operations) and **Admin** (user and transaction type management).

| Category | Count |
|---|---|
| COBOL Programs (core) | 31 |
| COBOL Programs (optional modules) | 13 |
| Copybooks (data structures) | 30 |
| Copybooks (BMS-generated) | 17 |
| BMS Screen Maps | 17 |
| JCL Batch Jobs (core) | 38 |
| JCL Batch Jobs (optional modules) | 8 |
| Assembler Programs | 2 |
| JCL Procedures | 2 |

---

## COBOL Programs — Online (CICS)

These programs run under CICS and handle interactive 3270 terminal sessions. Naming convention: `CO*` prefix.

| # | Program | Lines | Function | CICS Trans | Domain | BMS Map |
|---|---------|-------|----------|------------|--------|---------|
| 1 | **COSGN00C.cbl** | 260 | User sign-on / authentication | CC00 | Security | COSGN00 |
| 2 | **COMEN01C.cbl** | 308 | Main menu — dispatches to sub-menus | CM00 | Navigation | COMEN01 |
| 3 | **COADM01C.cbl** | 288 | Admin menu — dispatches to admin functions | CA00 | Admin | COADM01 |
| 4 | **COACTVWC.cbl** | 941 | View account details (read-only) | — | Accounts | COACTVW |
| 5 | **COACTUPC.cbl** | 4,236 | Update account and customer information | — | Accounts | COACTUP |
| 6 | **COCRDLIC.cbl** | 1,459 | List credit cards (browse with pagination) | — | Cards | COCRDLI |
| 7 | **COCRDSLC.cbl** | 887 | View credit card details (read-only) | — | Cards | COCRDSL |
| 8 | **COCRDUPC.cbl** | 1,560 | Update credit card details | — | Cards | COCRDUP |
| 9 | **COTRN00C.cbl** | 699 | List transactions (browse with pagination) | — | Transactions | COTRN00 |
| 10 | **COTRN01C.cbl** | 330 | View a single transaction (read-only) | — | Transactions | COTRN01 |
| 11 | **COTRN02C.cbl** | 783 | Add a new transaction | — | Transactions | COTRN02 |
| 12 | **CORPT00C.cbl** | 649 | Generate transaction reports (date range) | — | Reporting | CORPT00 |
| 13 | **COBIL00C.cbl** | 572 | Process bill payments | — | Billing | COBIL00 |
| 14 | **COUSR00C.cbl** | 695 | List all users (admin, browse w/ pagination) | — | User Mgmt | COUSR00 |
| 15 | **COUSR01C.cbl** | 299 | Add a new user (admin) | — | User Mgmt | COUSR01 |
| 16 | **COUSR02C.cbl** | 414 | Update an existing user (admin) | — | User Mgmt | COUSR02 |
| 17 | **COUSR03C.cbl** | 359 | Delete a user (admin) | — | User Mgmt | COUSR03 |

---

## COBOL Programs — Batch

Batch programs run via JCL. Naming convention: `CB*` prefix.

| # | Program | Lines | Function | Domain | Input Files | Output Files |
|---|---------|-------|----------|--------|-------------|--------------|
| 18 | **CBACT01C.cbl** | 430 | Read and validate account master file | Accounts | ACCTFILE | — |
| 19 | **CBACT02C.cbl** | 178 | Read and validate card data file | Cards | CARDFILE | — |
| 20 | **CBACT03C.cbl** | 178 | Read and validate card cross-reference | Cards | XREFFILE | — |
| 21 | **CBACT04C.cbl** | 652 | Calculate interest on account balances | Finance | ACCTFILE, DISCGRP, TCATBALF, TRANFILE, XREFFILE, DALYTRAN | ACCTFILE (update), TRANFILE (write) |
| 22 | **CBCUS01C.cbl** | 178 | Read and validate customer master file | Customers | CUSTFILE | — |
| 23 | **CBTRN01C.cbl** | 494 | Read and validate daily transaction file | Transactions | DALYTRAN | — |
| 24 | **CBTRN02C.cbl** | 731 | Post daily transactions — core processing | Transactions | DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF | TRANSACT (write), ACCTFILE (rewrite), TCATBALF (rewrite) |
| 25 | **CBTRN03C.cbl** | 649 | Generate daily transaction report | Reporting | TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM | TRANREPT |
| 26 | **CBSTM03A.CBL** | 924 | Print account statements (text + HTML) | Reporting | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE |
| 27 | **CBSTM03B.CBL** | 230 | Subroutine — file processing for statements | Reporting | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — (called by CBSTM03A) |
| 28 | **CBEXPORT.cbl** | 582 | Export VSAM data to sequential files | Data Mgmt | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE | EXPACCT, EXPCARD, EXPCUST, EXPXREF, EXPTRAN |
| 29 | **CBIMPORT.cbl** | 487 | Import sequential files to VSAM | Data Mgmt | IMPACCT, IMPCARD, IMPCUST, IMPXREF, IMPTRAN | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE |

---

## COBOL Programs — Utility / Shared

| # | Program | Lines | Function | Domain |
|---|---------|-------|----------|--------|
| 30 | **CSUTLDTC.cbl** | 157 | Date validation utility (calls CEEDAYS) | Utility |
| 31 | **COBSWAIT.cbl** | 41 | Wait/delay utility (calls MVSWAIT) | Utility |

---

## Optional Extension Modules

### Authorization Module (`app/app-authorization-ims-db2-mq/`)
IMS DB + DB2 + MQ integration for payment authorization.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 32 | **COPAUA0C.cbl** | — | Online | MQ trigger — initiates authorization |
| 33 | **COPAUS0C.cbl** | — | Online | Authorization summary display |
| 34 | **COPAUS1C.cbl** | — | Online | Authorization detail display |
| 35 | **COPAUS2C.cbl** | — | Online | Mark transaction as fraud (DB2 write) |
| 36 | **CBPAUP0C.cbl** | — | Batch | Batch purge of old authorizations |
| 37 | **DBUNLDGS.CBL** | — | Batch | Unload IMS database (generic sample) |
| 38 | **PAUDBLOD.CBL** | — | Batch | Load payment authorization IMS DB |
| 39 | **PAUDBUNL.CBL** | — | Batch | Unload payment authorization IMS DB |

### Transaction Type DB2 Module (`app/app-transaction-type-db2/`)
DB2 CRUD operations for transaction type management.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 40 | **COTRTUPC.cbl** | — | Online | Add/edit transaction types (DB2 embedded SQL) |
| 41 | **COTRTLIC.cbl** | — | Online | List/delete transaction types (DB2 cursor) |
| 42 | **COBTUPDT.cbl** | — | Batch | Batch update transaction types (DB2) |

### VSAM-MQ Module (`app/app-vsam-mq/`)
MQ-based request/response services.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 43 | **CODATE01.cbl** | — | Online | MQ service — system date request (CDRD queue) |
| 44 | **COACCT01.cbl** | — | Online | MQ service — account inquiry (CDRA queue) |

---

## Copybooks — Data Structures

Located in `app/cpy/`. Naming convention: `CV*` = VSAM record layouts, `CS*` = shared/system, `CO*` = online-specific.

| # | Copybook | Lines | Description | Record Size | Key Fields |
|---|----------|-------|-------------|-------------|------------|
| 1 | **CVACT01Y.cpy** | 20 | Account master record | 300 bytes | ACCT-ID (11 digits) |
| 2 | **CVACT02Y.cpy** | 14 | Card data record | 150 bytes | CARD-NUM (16 chars) |
| 3 | **CVACT03Y.cpy** | 11 | Card-to-account cross-reference | 50 bytes | XREF-CARD-NUM → XREF-ACCT-ID |
| 4 | **CVCUS01Y.cpy** | 26 | Customer master record | 500 bytes | CUST-ID (9 digits) |
| 5 | **CVCRD01Y.cpy** | 46 | Card detail record (extended) | — | Card number, status, dates |
| 6 | **CVTRA01Y.cpy** | 13 | Transaction category balance | 50 bytes | ACCT-ID + TYPE-CD + CAT-CD |
| 7 | **CVTRA02Y.cpy** | 13 | Disclosure group (interest rates) | 50 bytes | GROUP-ID + TYPE-CD + CAT-CD |
| 8 | **CVTRA03Y.cpy** | 10 | Transaction type lookup | 60 bytes | TRAN-TYPE (2 chars) |
| 9 | **CVTRA04Y.cpy** | 12 | Transaction category type | 60 bytes | TYPE-CD + CAT-CD |
| 10 | **CVTRA05Y.cpy** | 21 | Transaction record (online) | 350 bytes | TRAN-ID (16 chars) |
| 11 | **CVTRA06Y.cpy** | 21 | Daily transaction record | 350 bytes | DALYTRAN-ID (16 chars) |
| 12 | **CVTRA07Y.cpy** | 73 | Transaction report layout (headers/totals) | — | Report structures |
| 13 | **CVEXPORT.cpy** | 103 | Export record layouts (all entities) | — | Composite export records |
| 14 | **COCOM01Y.cpy** | 47 | Common communication area | — | CCARD-NEXT-PROG, flags, context |
| 15 | **COADM02Y.cpy** | 62 | Admin menu option definitions | — | Menu options → program names |
| 16 | **COMEN02Y.cpy** | 101 | Main menu option definitions | — | Menu options → program names |
| 17 | **COTTL01Y.cpy** | 27 | Title/header display fields | — | Screen title, date, time |
| 18 | **CSUSR01Y.cpy** | 26 | User security record | 80 bytes | SEC-USR-ID (8 chars) |
| 19 | **CSDAT01Y.cpy** | 58 | Date/time working storage | — | Date formatting fields |
| 20 | **CSMSG01Y.cpy** | 24 | User message area (line 1) | — | Error/info messages |
| 21 | **CSMSG02Y.cpy** | 35 | User message area (line 2) | — | Error/info messages |
| 22 | **CSSETATY.cpy** | 30 | Screen field attribute setter | — | DFHBMSCA attribute helper |
| 23 | **CSSTRPFY.cpy** | 85 | String strip/pad utility | — | String manipulation routines |
| 24 | **CSLKPCDY.cpy** | 1,318 | Lookup code tables (countries, states) | — | Code → description mappings |
| 25 | **CSUTLDPY.cpy** | 375 | Date utility parameters | — | CSUTLDTC linkage area |
| 26 | **CSUTLDWY.cpy** | 89 | Date utility working storage | — | Working fields for CSUTLDTC |
| 27 | **CODATECN.cpy** | 52 | Date conversion record | — | Date format conversion fields |
| 28 | **COSTM01.CPY** | 38 | Statement transaction layout (card+tran key) | 350 bytes | TRNX-CARD-NUM + TRNX-ID |
| 29 | **CUSTREC.cpy** | 26 | Customer record (alternate layout) | — | Customer fields |
| 30 | **UNUSED1Y.cpy** | 10 | Unused placeholder record | 80 bytes | Legacy/deprecated |

---

## Copybooks — BMS Screen Maps

Located in `app/cpy-bms/`. Auto-generated from BMS map definitions. Each defines the symbolic map data structure used by its paired COBOL program.

| # | Copybook | Paired BMS Map | Paired Program | Screen Function |
|---|----------|---------------|----------------|-----------------|
| 1 | **COSGN00.CPY** | COSGN00.bms | COSGN00C | Sign-on screen fields |
| 2 | **COMEN01.CPY** | COMEN01.bms | COMEN01C | Main menu screen fields |
| 3 | **COADM01.CPY** | COADM01.bms | COADM01C | Admin menu screen fields |
| 4 | **COACTVW.CPY** | COACTVW.bms | COACTVWC | Account view screen fields |
| 5 | **COACTUP.CPY** | COACTUP.bms | COACTUPC | Account update screen fields |
| 6 | **COCRDLI.CPY** | COCRDLI.bms | COCRDLIC | Card list screen fields |
| 7 | **COCRDSL.CPY** | COCRDSL.bms | COCRDSLC | Card detail screen fields |
| 8 | **COCRDUP.CPY** | COCRDUP.bms | COCRDUPC | Card update screen fields |
| 9 | **COTRN00.CPY** | COTRN00.bms | COTRN00C | Transaction list screen fields |
| 10 | **COTRN01.CPY** | COTRN01.bms | COTRN01C | Transaction view screen fields |
| 11 | **COTRN02.CPY** | COTRN02.bms | COTRN02C | Transaction add screen fields |
| 12 | **CORPT00.CPY** | CORPT00.bms | CORPT00C | Report parameters screen fields |
| 13 | **COBIL00.CPY** | COBIL00.bms | COBIL00C | Bill payment screen fields |
| 14 | **COUSR00.CPY** | COUSR00.bms | COUSR00C | User list screen fields |
| 15 | **COUSR01.CPY** | COUSR01.bms | COUSR01C | User add screen fields |
| 16 | **COUSR02.CPY** | COUSR02.bms | COUSR02C | User update screen fields |
| 17 | **COUSR03.CPY** | COUSR03.bms | COUSR03C | User delete screen fields |

---

## BMS Maps — Terminal Screen Definitions

Located in `app/bms/`. Each map defines 3270 terminal screen layout (field positions, attributes, labels).

| # | BMS Map | Lines | Screen Name | Domain | Key Fields on Screen |
|---|---------|-------|-------------|--------|---------------------|
| 1 | **COSGN00.bms** | 210 | Sign On | Security | User ID, Password |
| 2 | **COMEN01.bms** | 167 | Main Menu | Navigation | Menu option selection |
| 3 | **COADM01.bms** | 167 | Admin Menu | Admin | Admin option selection |
| 4 | **COACTVW.bms** | 378 | Account View | Accounts | Account ID, Name, Balances, Status |
| 5 | **COACTUP.bms** | 512 | Account Update | Accounts | Editable account + customer fields |
| 6 | **COCRDLI.bms** | 344 | Card List | Cards | Card number list, selection, pagination |
| 7 | **COCRDSL.bms** | 157 | Card Detail | Cards | Card number, Name, Expiry, Status |
| 8 | **COCRDUP.bms** | 172 | Card Update | Cards | Editable card fields |
| 9 | **COTRN00.bms** | 464 | Transaction List | Transactions | Transaction list, filters, pagination |
| 10 | **COTRN01.bms** | 273 | Transaction View | Transactions | Transaction details (read-only) |
| 11 | **COTRN02.bms** | 307 | Transaction Add | Transactions | New transaction entry fields |
| 12 | **CORPT00.bms** | 231 | Report Request | Reporting | Date range, report type |
| 13 | **COBIL00.bms** | 141 | Bill Payment | Billing | Account, Amount, Confirmation |
| 14 | **COUSR00.bms** | 463 | User List | User Mgmt | User list, selection, pagination |
| 15 | **COUSR01.bms** | 164 | User Add | User Mgmt | User ID, Name, Type, Password |
| 16 | **COUSR02.bms** | 169 | User Update | User Mgmt | Editable user fields |
| 17 | **COUSR03.bms** | 153 | User Delete | User Mgmt | User details, delete confirmation |

---

## JCL Jobs — Batch Processing

Located in `app/jcl/`. Organized by function.

### Data File Management

| # | JCL Job | Lines | Function | Key Datasets |
|---|---------|-------|----------|--------------|
| 1 | **ACCTFILE.jcl** | 65 | Define & load Account VSAM KSDS | ACCTDATA.VSAM.KSDS |
| 2 | **CARDFILE.jcl** | 128 | Define & load Card VSAM KSDS + Alternate Index | CARDDATA.VSAM.KSDS |
| 3 | **CUSTFILE.jcl** | 84 | Define & load Customer VSAM KSDS | CUSTDATA.VSAM.KSDS |
| 4 | **XREFFILE.jcl** | 106 | Define & load Card Cross-Reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | **TRANFILE.jcl** | 125 | Define & load Transaction VSAM KSDS + AIX | TRANSACT.VSAM.KSDS |
| 6 | **DUSRSECJ.jcl** | 92 | Define & load User Security VSAM KSDS | USRSEC.VSAM.KSDS |
| 7 | **DEFCUST.jcl** | 47 | Define Customer VSAM cluster only | CUSTDATA.VSAM.KSDS |

### Batch Processing Cycle

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|-------|----------|-----------------|
| 8 | **CLOSEFIL.jcl** | 34 | Close CICS files before batch | DFHCSDUP |
| 9 | **POSTTRAN.jcl** | 45 | Post daily transactions | CBTRN02C |
| 10 | **INTCALC.jcl** | 44 | Calculate interest on accounts | CBACT04C |
| 11 | **TRANBKP.jcl** | 71 | Backup transaction file | IDCAMS REPRO |
| 12 | **COMBTRAN.jcl** | 52 | Combine daily + master transactions | SORT + IDCAMS |
| 13 | **CREASTMT.JCL** | 97 | Create account statements (text + HTML) | CBSTM03A |
| 14 | **TRANIDX.jcl** | 58 | Rebuild transaction alternate index | IDCAMS BLDINDEX |
| 15 | **OPENFIL.jcl** | 34 | Reopen CICS files after batch | DFHCSDUP |

### Reporting

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|-------|----------|-----------------|
| 16 | **TRANREPT.jcl** | 84 | Generate daily transaction report | CBTRN03C |
| 17 | **REPTFILE.jcl** | 32 | Define report output dataset | IDCAMS |
| 18 | **PRTCATBL.jcl** | 66 | Print catalog balance report | SORT/IDCAMS |
| 19 | **TXT2PDF1.JCL** | 41 | Convert text statement to PDF | TXT2PDF |

### Data Export / Import

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|-------|----------|-----------------|
| 20 | **CBEXPORT.jcl** | 72 | Export all VSAM files to sequential | CBEXPORT |
| 21 | **CBIMPORT.jcl** | 68 | Import sequential files to VSAM | CBIMPORT |

### VSAM Definitions / Utilities

| # | JCL Job | Lines | Function | Utility |
|---|---------|-------|----------|---------|
| 22 | **DEFGDGB.jcl** | 63 | Define GDG base for backups | IDCAMS |
| 23 | **DEFGDGD.jcl** | 94 | Define GDG base for daily transactions | IDCAMS |
| 24 | **ESDSRRDS.jcl** | 124 | Define ESDS/RRDS demo clusters | IDCAMS |
| 25 | **DALYREJS.jcl** | 32 | Define daily rejects dataset | IDCAMS |
| 26 | **DISCGRP.jcl** | 65 | Define & load disclosure groups | IDCAMS |
| 27 | **TCATBALF.jcl** | 65 | Define & load transaction category balances | IDCAMS |
| 28 | **TRANCATG.jcl** | 65 | Define & load transaction categories | IDCAMS |
| 29 | **TRANTYPE.jcl** | 65 | Define & load transaction types | IDCAMS |

### Data Read / Validation

| # | JCL Job | Lines | Function | Program Executed |
|---|---------|-------|----------|-----------------|
| 30 | **READACCT.jcl** | 50 | Read/dump account VSAM file | CBACT01C |
| 31 | **READCARD.jcl** | 31 | Read/dump card VSAM file | CBACT02C |
| 32 | **READCUST.jcl** | 30 | Read/dump customer VSAM file | CBCUS01C |
| 33 | **READXREF.jcl** | 31 | Read/dump cross-reference VSAM file | CBACT03C |

### Admin & Infrastructure

| # | JCL Job | Lines | Function |
|---|---------|-------|----------|
| 34 | **CBADMCDJ.jcl** | 167 | Compile & link COBOL programs |
| 35 | **FTPJCL.JCL** | 42 | FTP file transfer job |
| 36 | **WAITSTEP.jcl** | 27 | Wait step (calls COBSWAIT) |
| 37 | **INTRDRJ1.JCL** | 19 | Internal reader — trigger job chain (step 1) |
| 38 | **INTRDRJ2.JCL** | 14 | Internal reader — trigger job chain (step 2) |

---

## Assembler Programs

Located in `app/asm/`.

| # | Program | Function |
|---|---------|----------|
| 1 | **COBDATFT.asm** | Date formatting — called by CBACT01C |
| 2 | **MVSWAIT.asm** | MVS WAIT SVC — called by COBSWAIT |

---

## JCL Procedures

Located in `app/proc/`.

| # | Procedure | Function |
|---|-----------|----------|
| 1 | **REPROC.prc** | Reprocessing procedure (shared JCL steps) |
| 2 | **TRANREPT.prc** | Transaction report procedure |

---

## Summary Counts

| Artifact Type | Core | Optional Modules | Total |
|---------------|------|-----------------|-------|
| COBOL Programs | 31 | 13 | **44** |
| Copybooks (data) | 30 | 11 | **41** |
| Copybooks (BMS) | 17 | 4 | **21** |
| BMS Maps | 17 | 4 | **21** |
| JCL Jobs | 38 | 8 | **46** |
| Assembler | 2 | 0 | **2** |
| JCL Procedures | 2 | 0 | **2** |
| **Grand Total** | **137** | **40** | **177** |

### Domain Breakdown (Core Programs)

| Business Domain | Online Programs | Batch Programs | Total |
|----------------|----------------|----------------|-------|
| Security / Auth | 1 | 0 | 1 |
| Navigation / Menus | 2 | 0 | 2 |
| Accounts | 2 | 2 | 4 |
| Cards | 3 | 2 | 5 |
| Transactions | 3 | 3 | 6 |
| Billing | 1 | 0 | 1 |
| Reporting | 1 | 2 | 3 |
| User Management | 4 | 0 | 4 |
| Customers | 0 | 1 | 1 |
| Data Export/Import | 0 | 2 | 2 |
| Utility | 0 | 2 | 2 |
