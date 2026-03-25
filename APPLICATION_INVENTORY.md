# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** COBOL / CICS / VSAM / JCL / BMS (IBM z/OS)

---

## Executive Summary

CardDemo is a mainframe credit card management application comprising **44 COBOL programs**, **42 copybooks**, **21 BMS screen maps**, and **43 JCL jobs** across the core system and three optional extension modules. The application handles account management, card management, transaction processing, bill payments, reporting, and administrative user management.

| Category                  | Core | Auth (IMS/DB2/MQ) | Tran Type (DB2) | VSAM-MQ | **Total** |
|---------------------------|------|--------------------|-----------------|---------|-----------|
| COBOL Programs            | 31   | 8                  | 3               | 2       | **44**    |
| Copybooks                 | 32   | 9                  | 2               | 0       | **43**    |
| BMS Screen Maps           | 17   | 2                  | 2               | 0       | **21**    |
| BMS-Generated Copybooks   | 17   | 2                  | 2               | 0       | **21**    |
| JCL Jobs                  | 38   | 5                  | 3               | 0       | **46**    |

---

## 1. COBOL Programs — Core (app/cbl/)

### 1.1 Online CICS Programs (CO* prefix)

| # | Program ID | File | Lines | Function | CICS Trans | Business Domain |
|---|-----------|------|-------|----------|------------|-----------------|
| 1 | COSGN00C | COSGN00C.cbl | 260 | Sign-on / Authentication | CC00 | Security |
| 2 | COMEN01C | COMEN01C.cbl | 308 | Main Menu Navigation | CM00 | Navigation |
| 3 | COADM01C | COADM01C.cbl | 288 | Admin Menu Navigation | CA00 | Administration |
| 4 | COACTVWC | COACTVWC.cbl | 941 | Account View (read-only) | CA01 | Account Mgmt |
| 5 | COACTUPC | COACTUPC.cbl | 4,236 | Account Update | CA02 | Account Mgmt |
| 6 | COCRDLIC | COCRDLIC.cbl | 1,459 | Card List (browse/search) | CC01 | Card Mgmt |
| 7 | COCRDSLC | COCRDSLC.cbl | 887 | Card Detail View | CC02 | Card Mgmt |
| 8 | COCRDUPC | COCRDUPC.cbl | 1,560 | Card Update | CC03 | Card Mgmt |
| 9 | COTRN00C | COTRN00C.cbl | 699 | Transaction List (browse) | CT00 | Transactions |
| 10 | COTRN01C | COTRN01C.cbl | 330 | Transaction Detail View | CT01 | Transactions |
| 11 | COTRN02C | COTRN02C.cbl | 783 | Transaction Add (new) | CT02 | Transactions |
| 12 | CORPT00C | CORPT00C.cbl | 649 | Transaction Report Request | CR00 | Reporting |
| 13 | COBIL00C | COBIL00C.cbl | 572 | Bill Payment Processing | CB00 | Billing |
| 14 | COUSR00C | COUSR00C.cbl | 695 | User List (admin browse) | CU00 | User Mgmt |
| 15 | COUSR01C | COUSR01C.cbl | 299 | User Add (admin) | CU01 | User Mgmt |
| 16 | COUSR02C | COUSR02C.cbl | 414 | User Update (admin) | CU02 | User Mgmt |
| 17 | COUSR03C | COUSR03C.cbl | 359 | User Delete (admin) | CU03 | User Mgmt |

### 1.2 Batch Programs (CB* prefix)

| # | Program ID | File | Lines | Function | Business Domain |
|---|-----------|------|-------|----------|-----------------|
| 18 | CBACT01C | CBACT01C.cbl | 430 | Read/Print Account File | Data Utility |
| 19 | CBACT02C | CBACT02C.cbl | 178 | Read/Print Card File | Data Utility |
| 20 | CBACT03C | CBACT03C.cbl | 178 | Read/Print Cross-Reference File | Data Utility |
| 21 | CBACT04C | CBACT04C.cbl | 652 | Interest Calculation (batch) | Financial Processing |
| 22 | CBCUS01C | CBCUS01C.cbl | 178 | Read/Print Customer File | Data Utility |
| 23 | CBTRN01C | CBTRN01C.cbl | 494 | Read/Print Transaction File | Data Utility |
| 24 | CBTRN02C | CBTRN02C.cbl | 731 | Transaction Posting (batch) | Financial Processing |
| 25 | CBTRN03C | CBTRN03C.cbl | 649 | Daily Transaction Report (batch) | Reporting |
| 26 | CBSTM03A | CBSTM03A.CBL | 924 | Statement Generation (main) | Reporting |
| 27 | CBSTM03B | CBSTM03B.CBL | 230 | Statement Generation (subroutine) | Reporting |
| 28 | CBEXPORT | CBEXPORT.cbl | 582 | Data Export (VSAM → flat file) | Data Utility |
| 29 | CBIMPORT | CBIMPORT.cbl | 487 | Data Import (flat file → VSAM) | Data Utility |

### 1.3 Utility Programs

| # | Program ID | File | Lines | Function | Business Domain |
|---|-----------|------|-------|----------|-----------------|
| 30 | CSUTLDTC | CSUTLDTC.cbl | 157 | Date Validation Utility (CEEDAYS) | Shared Utility |
| 31 | COBSWAIT | COBSWAIT.cbl | 41 | Wait/Delay Utility | Job Scheduling |

---

## 2. COBOL Programs — Optional Modules

### 2.1 Authorization Module (app/app-authorization-ims-db2-mq/)

| # | Program ID | File | Lines | Function | Technology |
|---|-----------|------|-------|----------|------------|
| 32 | COPAUA0C | COPAUA0C.cbl | 1,026 | MQ Trigger — Authorization Processing | COBOL + MQ |
| 33 | COPAUS0C | COPAUS0C.cbl | 1,032 | Authorization Summary View | CICS + IMS |
| 34 | COPAUS1C | COPAUS1C.cbl | 604 | Authorization Detail View | CICS + IMS |
| 35 | COPAUS2C | COPAUS2C.cbl | 244 | Mark Transaction as Fraud (DB2) | CICS + DB2 |
| 36 | CBPAUP0C | CBPAUP0C.cbl | 386 | Batch Purge of Auth Records | Batch + IMS |
| 37 | DBUNLDGS | DBUNLDGS.CBL | 366 | IMS DB Unload to GSAM | Batch + IMS |
| 38 | PAUDBLOD | PAUDBLOD.CBL | 369 | IMS DB Load from File | Batch + IMS |
| 39 | PAUDBUNL | PAUDBUNL.CBL | 317 | IMS DB Unload to File | Batch + IMS |

### 2.2 Transaction Type DB2 Module (app/app-transaction-type-db2/)

| # | Program ID | File | Lines | Function | Technology |
|---|-----------|------|-------|----------|------------|
| 40 | COTRTLIC | COTRTLIC.cbl | 2,098 | Transaction Type List (DB2 cursors) | CICS + DB2 |
| 41 | COTRTUPC | COTRTUPC.cbl | 1,702 | Transaction Type Add/Edit | CICS + DB2 |
| 42 | COBTUPDT | COBTUPDT.cbl | 237 | Batch Transaction Type Update | Batch + DB2 |

### 2.3 VSAM-MQ Module (app/app-vsam-mq/)

| # | Program ID | File | Lines | Function | Technology |
|---|-----------|------|-------|----------|------------|
| 43 | COACCT01 | COACCT01.cbl | 620 | MQ Account Inquiry (request/reply) | CICS + MQ + VSAM |
| 44 | CODATE01 | CODATE01.cbl | 524 | MQ System Date Service | CICS + MQ |

---

## 3. Copybooks — Core (app/cpy/)

### 3.1 Data Record Layouts (CV* prefix)

| # | Copybook | Lines | Record Name | Rec Len | Business Entity |
|---|----------|-------|-------------|---------|-----------------|
| 1 | CVACT01Y.cpy | 16 | ACCT-RECORD | 300 | Account Master |
| 2 | CVACT02Y.cpy | 10 | CARD-RECORD | 150 | Card Master |
| 3 | CVACT03Y.cpy | 10 | CARD-XREF-RECORD | 50 | Card Cross-Reference |
| 4 | CVCRD01Y.cpy | 46 | CDEMO-CU-CARD-* | — | Card Display Structure |
| 5 | CVCUS01Y.cpy | 21 | CUSTOMER-RECORD | 500 | Customer Master |
| 6 | CVTRA01Y.cpy | 13 | TRAN-CAT-BAL-RECORD | 50 | Transaction Category Balance |
| 7 | CVTRA02Y.cpy | 13 | DIS-GROUP-RECORD | 50 | Disclosure Group |
| 8 | CVTRA03Y.cpy | 10 | TRAN-TYPE-RECORD | 60 | Transaction Type |
| 9 | CVTRA04Y.cpy | 12 | TRAN-CAT-RECORD | 60 | Transaction Category |
| 10 | CVTRA05Y.cpy | 21 | TRAN-RECORD | 350 | Transaction Master |
| 11 | CVTRA06Y.cpy | 21 | DALYTRAN-RECORD | 350 | Daily Transaction |
| 12 | CVTRA07Y.cpy | 73 | REPORT-* | — | Transaction Report Layout |
| 13 | CVEXPORT.cpy | 84 | FD-* | — | Export File Descriptors |
| 14 | COSTM01.CPY | 38 | TRNX-RECORD | 350 | Statement Transaction Layout |
| 15 | CUSTREC.cpy | 22 | CUSTOMER-RECORD | — | Customer Record (alt layout) |
| 16 | CSUSR01Y.cpy | 20 | SEC-USER-DATA | 80 | User Security Record |
| 17 | UNUSED1Y.cpy | 10 | UNUSED-DATA | 80 | Unused/Placeholder |

### 3.2 Common Area & UI Support Copybooks (CO*/CS*/CM* prefix)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 18 | COCOM01Y.cpy | 41 | Common communication area (DFHCOMMAREA) |
| 19 | COMEN02Y.cpy | 73 | Menu option definitions |
| 20 | COADM02Y.cpy | 68 | Admin menu option definitions |
| 21 | COTTL01Y.cpy | 24 | Screen title/header layout |
| 22 | CSDAT01Y.cpy | 44 | Date conversion working storage |
| 23 | CSMSG01Y.cpy | 20 | Message area (short) |
| 24 | CSMSG02Y.cpy | 28 | Message area (long) |
| 25 | CSSETATY.cpy | 26 | Set attribute bytes utility |
| 26 | CSSTRPFY.cpy | 90 | String processing functions |
| 27 | CSLKPCDY.cpy | 718 | Country/state lookup codes |
| 28 | CSUTLDPY.cpy | 179 | Date utility parameter structures |
| 29 | CSUTLDWY.cpy | 71 | Date utility working storage |
| 30 | CODATECN.cpy | 36 | Date conversion routines (COBOL intrinsic) |

### 3.3 Authorization Module Copybooks (app/app-authorization-ims-db2-mq/cpy/)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 31 | CCPAUERY.cpy | 11 | Auth error message structure |
| 32 | CCPAURLY.cpy | 24 | Auth MQ reply message layout |
| 33 | CCPAURQY.cpy | 22 | Auth MQ request message layout |
| 34 | CIPAUSMY.cpy | 38 | Auth IMS summary segment |
| 35 | CIPAUDTY.cpy | 31 | Auth IMS detail segment |
| 36 | IMSFUNCS.cpy | 22 | IMS DL/I function codes |
| 37 | PAUTBPCB.CPY | 26 | IMS PCB for auth DB |
| 38 | PASFLPCB.CPY | 26 | IMS PCB for summary flat file |
| 39 | PADFLPCB.CPY | 26 | IMS PCB for detail flat file |

### 3.4 Transaction Type DB2 Copybooks (app/app-transaction-type-db2/cpy/)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 40 | CSDB2RPY.cpy | 89 | DB2 common procedures (DSNTIAC) |
| 41 | CSDB2RWY.cpy | 46 | DB2 common working storage |

---

## 4. BMS Screen Maps (app/bms/)

| # | Map Name | File | Size (bytes) | Associated Program | Screen Function |
|---|----------|------|--------------|-------------------|-----------------|
| 1 | COSGN00 | COSGN00.bms | 13,622 | COSGN00C | Sign-On Screen |
| 2 | COMEN01 | COMEN01.bms | 10,499 | COMEN01C | Main Menu |
| 3 | COADM01 | COADM01.bms | 10,501 | COADM01C | Admin Menu |
| 4 | COACTVW | COACTVW.bms | 22,872 | COACTVWC | Account View |
| 5 | COACTUP | COACTUP.bms | 31,388 | COACTUPC | Account Update |
| 6 | COCRDLI | COCRDLI.bms | 21,476 | COCRDLIC | Card List |
| 7 | COCRDSL | COCRDSL.bms | 9,688 | COCRDSLC | Card Detail |
| 8 | COCRDUP | COCRDUP.bms | 10,656 | COCRDUPC | Card Update |
| 9 | COTRN00 | COTRN00.bms | 29,377 | COTRN00C | Transaction List |
| 10 | COTRN01 | COTRN01.bms | 17,059 | COTRN01C | Transaction View |
| 11 | COTRN02 | COTRN02.bms | 19,333 | COTRN02C | Transaction Add |
| 12 | CORPT00 | CORPT00.bms | 14,631 | CORPT00C | Report Request |
| 13 | COBIL00 | COBIL00.bms | 8,776 | COBIL00C | Bill Payment |
| 14 | COUSR00 | COUSR00.bms | 29,351 | COUSR00C | User List |
| 15 | COUSR01 | COUSR01.bms | 10,338 | COUSR01C | User Add |
| 16 | COUSR02 | COUSR02.bms | 10,635 | COUSR02C | User Update |
| 17 | COUSR03 | COUSR03.bms | 9,630 | COUSR03C | User Delete |

### Optional Module BMS Maps

| # | Map Name | File | Module | Screen Function |
|---|----------|------|--------|-----------------|
| 18 | COPAU00 | app/app-authorization-ims-db2-mq/bms/COPAU00.bms | Auth | Auth Summary View |
| 19 | COPAU01 | app/app-authorization-ims-db2-mq/bms/COPAU01.bms | Auth | Auth Detail View |
| 20 | COTRTLI | app/app-transaction-type-db2/bms/COTRTLI.bms | DB2 Tran Type | Tran Type List |
| 21 | COTRTUP | app/app-transaction-type-db2/bms/COTRTUP.bms | DB2 Tran Type | Tran Type Update |

---

## 5. JCL Batch Jobs (app/jcl/)

### 5.1 Data Refresh / Load Jobs

| # | Job Name | File | Lines | Function | Programs Used |
|---|----------|------|-------|----------|---------------|
| 1 | ACCTFILE | ACCTFILE.jcl | 60 | Define & load Account VSAM from flat file | IDCAMS |
| 2 | CARDFILE | CARDFILE.jcl | 130 | Define & load Card VSAM (with AIX) | IDCAMS, SDSF |
| 3 | CUSTFILE | CUSTFILE.jcl | 81 | Define & load Customer VSAM | IDCAMS, SDSF |
| 4 | XREFFILE | XREFFILE.jcl | 107 | Define & load Card Cross-Ref VSAM (with AIX) | IDCAMS |
| 5 | TRANFILE | TRANFILE.jcl | 121 | Define & load Transaction VSAM (with AIX) | IDCAMS, SDSF |
| 6 | DUSRSECJ | DUSRSECJ.jcl | 88 | Define & load User Security VSAM | IEBGENER, IDCAMS |
| 7 | TRANTYPE | TRANTYPE.jcl | 61 | Define & load Transaction Type VSAM | IDCAMS |
| 8 | TRANCATG | TRANCATG.jcl | 61 | Define & load Transaction Category VSAM | IDCAMS |
| 9 | TCATBALF | TCATBALF.jcl | 61 | Define & load Tran Category Balance VSAM | IDCAMS |
| 10 | DISCGRP | DISCGRP.jcl | 61 | Define & load Disclosure Group VSAM | IDCAMS |
| 11 | DEFCUST | DEFCUST.jcl | 36 | Define Customer VSAM cluster | IDCAMS |
| 12 | DALYREJS | DALYREJS.jcl | 27 | Define Daily Rejects VSAM | IDCAMS |

### 5.2 Batch Processing Jobs

| # | Job Name | File | Lines | Function | Programs Used |
|---|----------|------|-------|----------|---------------|
| 13 | POSTTRAN | POSTTRAN.jcl | 44 | Post daily transactions | CBTRN02C |
| 14 | INTCALC | INTCALC.jcl | 44 | Calculate interest charges | CBACT04C |
| 15 | TRANBKP | TRANBKP.jcl | 62 | Backup transaction file | IDCAMS |
| 16 | COMBTRAN | COMBTRAN.jcl | 50 | Combine daily + master transactions | SORT, IDCAMS |
| 17 | TRANREPT | TRANREPT.jcl | 73 | Generate daily transaction report | SORT, CBTRN03C |
| 18 | CREASTMT | CREASTMT.JCL | 97 | Create account statements (text + HTML) | SORT, IDCAMS, CBSTM03A |
| 19 | PRTCATBL | PRTCATBL.jcl | 52 | Print category balance report | SORT |

### 5.3 Data Utility Jobs

| # | Job Name | File | Lines | Function | Programs Used |
|---|----------|------|-------|----------|---------------|
| 20 | READACCT | READACCT.jcl | 40 | Read/dump Account file | CBACT01C |
| 21 | READCARD | READCARD.jcl | 26 | Read/dump Card file | CBACT02C |
| 22 | READXREF | READXREF.jcl | 26 | Read/dump Cross-Ref file | CBACT03C |
| 23 | READCUST | READCUST.jcl | 23 | Read/dump Customer file | CBCUS01C |
| 24 | CBEXPORT | CBEXPORT.jcl | 54 | Export VSAM data to flat files | IDCAMS, CBEXPORT |
| 25 | CBIMPORT | CBIMPORT.jcl | 63 | Import flat file data to VSAM | CBIMPORT |
| 26 | REPTFILE | REPTFILE.jcl | 28 | Define report output file | IDCAMS |

### 5.4 Infrastructure / Administration Jobs

| # | Job Name | File | Lines | Function | Programs Used |
|---|----------|------|-------|----------|---------------|
| 27 | CLOSEFIL | CLOSEFIL.jcl | 27 | Close CICS files for batch | SDSF |
| 28 | OPENFIL | OPENFIL.jcl | 27 | Open CICS files after batch | SDSF |
| 29 | TRANIDX | TRANIDX.jcl | 55 | Rebuild transaction AIX | IDCAMS |
| 30 | DEFGDGB | DEFGDGB.jcl | 57 | Define GDG base entries | IDCAMS |
| 31 | DEFGDGD | DEFGDGD.jcl | 93 | Define GDG data entries | IDCAMS, IEBGENER |
| 32 | ESDSRRDS | ESDSRRDS.jcl | 118 | Define ESDS/RRDS VSAM files | IEBGENER, IDCAMS |
| 33 | CBADMCDJ | CBADMCDJ.jcl | 75 | Load CICS CSD definitions | DFHCSDUP |
| 34 | WAITSTEP | WAITSTEP.jcl | 24 | Wait step for job scheduling | COBSWAIT |
| 35 | FTPJCL | FTPJCL.JCL | 42 | FTP file transfer | FTP |
| 36 | INTRDRJ1 | INTRDRJ1.JCL | 19 | Internal reader job trigger (step 1) | IDCAMS, IEBGENER |
| 37 | INTRDRJ2 | INTRDRJ2.JCL | 14 | Internal reader job trigger (step 2) | IDCAMS |
| 38 | TXT2PDF1 | TXT2PDF1.JCL | 41 | Convert text statement to PDF | IKJEFT1B (TXT2PDF) |

### 5.5 Optional Module JCL Jobs

| # | Job Name | File | Module | Function |
|---|----------|------|--------|----------|
| 39 | CBPAUP0J | app/app-authorization-ims-db2-mq/jcl/CBPAUP0J.jcl | Auth | Batch purge auth records |
| 40 | DBPAUTP0 | app/app-authorization-ims-db2-mq/jcl/DBPAUTP0.jcl | Auth | Auth DB processing |
| 41 | LOADPADB | app/app-authorization-ims-db2-mq/jcl/LOADPADB.JCL | Auth | Load auth IMS DB |
| 42 | UNLDGSAM | app/app-authorization-ims-db2-mq/jcl/UNLDGSAM.JCL | Auth | Unload IMS DB to GSAM |
| 43 | UNLDPADB | app/app-authorization-ims-db2-mq/jcl/UNLDPADB.JCL | Auth | Unload auth IMS DB |
| 44 | CREADB21 | app/app-transaction-type-db2/jcl/CREADB21.jcl | DB2 TT | Create DB2 tran type table |
| 45 | MNTTRDB2 | app/app-transaction-type-db2/jcl/MNTTRDB2.jcl | DB2 TT | Maintain DB2 tran types |
| 46 | TRANEXTR | app/app-transaction-type-db2/jcl/TRANEXTR.jcl | DB2 TT | Extract tran types from DB2 |

---

## 6. Other Artifacts

### 6.1 Assembler Programs (app/asm/)

| Program | Function |
|---------|----------|
| MVSWAIT | Wait macro implementation for batch scheduling |
| COBDATFT | Date format conversion utility |

### 6.2 CSD Definitions (app/csd/)

| File | Contents |
|------|----------|
| CARDDEMO.CSD | CICS resource definitions for all online programs, transactions, files, and maps |

### 6.3 JCL Procedures (app/proc/)

| File | Purpose |
|------|---------|
| Various | Reusable JCL procedures for compilation and execution |

### 6.4 Sample Data (app/data/)

| Directory | Format | Contents |
|-----------|--------|----------|
| app/data/ASCII/ | ASCII | Sample data files for local testing |
| app/data/EBCDIC/ | EBCDIC | Mainframe-ready data for upload |

---

## 7. Classification Summary

### By Business Domain

| Domain | Programs | Criticality |
|--------|----------|-------------|
| Account Management | COACTVWC, COACTUPC, CBACT01C, CBACT04C | **High** |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | **High** |
| Transaction Processing | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | **Critical** |
| Billing & Payments | COBIL00C | **Critical** |
| Reporting & Statements | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B | **High** |
| User / Security Admin | COSGN00C, COUSR00C–03C | **High** |
| Navigation | COMEN01C, COADM01C | Medium |
| Data Utilities | CBEXPORT, CBIMPORT, CBCUS01C | Medium |
| Infrastructure | COBSWAIT, CSUTLDTC | Low |
| Authorization (opt.) | COPAUA0C, COPAUS0C–2C, CBPAUP0C, DB*, PAU* | **High** |
| Tran Type Mgmt (opt.) | COTRTLIC, COTRTUPC, COBTUPDT | Medium |
| MQ Services (opt.) | COACCT01, CODATE01 | Medium |

### By Technology Stack

| Technology | Count | Programs |
|------------|-------|----------|
| COBOL + CICS + VSAM | 17 | All CO* core online programs |
| COBOL Batch + VSAM | 12 | All CB* core batch programs |
| COBOL + CICS + IMS | 3 | COPAUS0C, COPAUS1C, CBPAUP0C |
| COBOL + CICS + DB2 | 3 | COPAUS2C, COTRTLIC, COTRTUPC |
| COBOL + MQ | 3 | COPAUA0C, COACCT01, CODATE01 |
| COBOL + IMS (Batch) | 3 | DBUNLDGS, PAUDBLOD, PAUDBUNL |
| COBOL + DB2 (Batch) | 1 | COBTUPDT |
| COBOL Utility | 2 | CSUTLDTC, COBSWAIT |

### Naming Conventions

| Prefix | Meaning | Example |
|--------|---------|---------|
| CO* | Online CICS program | COSGN00C |
| CB* | Batch program | CBTRN02C |
| CV* | Copybook — VSAM record layout | CVACT01Y |
| CS* | Copybook — shared/utility | CSDAT01Y |
| CO*Y | Copybook — common area | COCOM01Y |
| *Y | Copybook suffix convention | All app/cpy/ |
