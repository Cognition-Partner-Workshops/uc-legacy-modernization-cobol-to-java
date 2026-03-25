# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** CardDemo - Credit Card Management System
> **Platform:** IBM Mainframe (COBOL / CICS / VSAM / JCL)

---

## Executive Summary

CardDemo is a mainframe-based credit card management application comprising **31 COBOL programs**, **30 copybooks**, **38 JCL jobs**, **17 BMS screen maps**, **17 BMS-generated copybooks**, **2 assembler programs**, **2 JCL procedures**, and **3 optional extension modules** (13 additional programs). The system supports two user roles (Regular User and Admin) and covers account management, card management, transactions, bill payments, reporting, and batch processing.

---

## 1. COBOL Programs (31 core + 13 optional)

### 1.1 Online CICS Programs (17 programs)

These programs run under CICS and handle interactive 3270 terminal sessions.

| # | Program | File | Lines | Function | Business Domain | Transaction |
|---|---------|------|-------|----------|-----------------|-------------|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | 260 | Sign-on / Authentication | Security | CC00 |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | 308 | Main Menu (Regular Users) | Navigation | CM00 |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | 288 | Admin Menu | Navigation (Admin) | CA00 |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | 941 | Account View | Account Mgmt | CA01 |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | 4,236 | Account Update | Account Mgmt | CA02 |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | 1,459 | Credit Card List | Card Mgmt | CC01 |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | 887 | Credit Card View (Detail) | Card Mgmt | CC02 |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | 1,560 | Credit Card Update | Card Mgmt | CC03 |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | 699 | Transaction List | Transactions | CT00 |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | 330 | Transaction View | Transactions | CT01 |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | 783 | Transaction Add | Transactions | CT02 |
| 12 | CORPT00C | `app/cbl/CORPT00C.cbl` | 649 | Transaction Report Submit | Reporting | CR00 |
| 13 | COBIL00C | `app/cbl/COBIL00C.cbl` | 572 | Bill Payment | Billing | CB00 |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | 695 | User List (Admin) | User Mgmt | CU00 |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | 299 | User Add (Admin) | User Mgmt | CU01 |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | 414 | User Update (Admin) | User Mgmt | CU02 |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | 359 | User Delete (Admin) | User Mgmt | CU03 |

### 1.2 Batch COBOL Programs (13 programs)

These programs run as batch jobs via JCL.

| # | Program | File | Lines | Function | Business Domain |
|---|---------|------|-------|----------|-----------------|
| 18 | CBACT01C | `app/cbl/CBACT01C.cbl` | 430 | Read/split account file | Account Mgmt |
| 19 | CBACT02C | `app/cbl/CBACT02C.cbl` | 178 | Print card data | Card Mgmt |
| 20 | CBACT03C | `app/cbl/CBACT03C.cbl` | 178 | Print cross-reference data | Card Mgmt |
| 21 | CBACT04C | `app/cbl/CBACT04C.cbl` | 652 | Interest calculation | Financial |
| 22 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | 178 | Print customer data | Customer Mgmt |
| 23 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | 494 | Post daily transactions (basic) | Transactions |
| 24 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | 731 | Post daily transactions (full) | Transactions |
| 25 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | 649 | Transaction detail report | Reporting |
| 26 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | 924 | Statement generation (main) | Reporting |
| 27 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | 230 | Statement generation (HTML sub) | Reporting |
| 28 | CBEXPORT | `app/cbl/CBEXPORT.cbl` | 582 | Data export (all files to flat) | Data Migration |
| 29 | CBIMPORT | `app/cbl/CBIMPORT.cbl` | 487 | Data import (flat to VSAM) | Data Migration |
| 30 | COBSWAIT | `app/cbl/COBSWAIT.cbl` | 41 | Wait utility (parm in centiseconds) | Utility |

### 1.3 Shared Utility Programs (1 program)

| # | Program | File | Lines | Function | Business Domain |
|---|---------|------|-------|----------|-----------------|
| 31 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | 157 | Date conversion utility (CEEDAYS) | Utility |

### 1.4 Optional Module Programs (13 programs)

#### Authorization Module (IMS/DB2/MQ) - `app/app-authorization-ims-db2-mq/`

| # | Program | File | Function |
|---|---------|------|----------|
| 32 | COPAUA0C | `cbl/COPAUA0C.cbl` | MQ trigger for authorization |
| 33 | COPAUS0C | `cbl/COPAUS0C.cbl` | Authorization summary view |
| 34 | COPAUS1C | `cbl/COPAUS1C.cbl` | Authorization detail view |
| 35 | COPAUS2C | `cbl/COPAUS2C.cbl` | Fraud marking (DB2) |
| 36 | CBPAUP0C | `cbl/CBPAUP0C.cbl` | Batch purge of old authorizations |
| 37 | PAUDBLOD | `cbl/PAUDBLOD.CBL` | Load authorization DB |
| 38 | PAUDBUNL | `cbl/PAUDBUNL.CBL` | Unload authorization DB |
| 39 | DBUNLDGS | `cbl/DBUNLDGS.CBL` | Unload GSAM segments |

#### Transaction Type DB2 Module - `app/app-transaction-type-db2/`

| # | Program | File | Function |
|---|---------|------|----------|
| 40 | COTRTUPC | `cbl/COTRTUPC.cbl` | Transaction type add/edit (DB2) |
| 41 | COTRTLIC | `cbl/COTRTLIC.cbl` | Transaction type list/delete (DB2) |
| 42 | COBTUPDT | `cbl/COBTUPDT.cbl` | Batch transaction type update (DB2) |

#### VSAM-MQ Module - `app/app-vsam-mq/`

| # | Program | File | Function |
|---|---------|------|----------|
| 43 | CODATE01 | `cbl/CODATE01.cbl` | MQ date inquiry service |
| 44 | COACCT01 | `cbl/COACCT01.cbl` | MQ account inquiry service |

---

## 2. Copybooks (30 core + 17 BMS-generated)

### 2.1 Data Structure Copybooks (`app/cpy/`)

| # | Copybook | Record Length | Description | Business Entity |
|---|----------|--------------|-------------|-----------------|
| 1 | CVACT01Y | 300 bytes | Account master record | Account |
| 2 | CVACT02Y | 150 bytes | Card data record | Card |
| 3 | CVACT03Y | 50 bytes | Card cross-reference record | Card-Account Link |
| 4 | CVCUS01Y | 500 bytes | Customer data record | Customer |
| 5 | CVCRD01Y | 50 bytes | Card/account cross-reference (alternate) | Card |
| 6 | CVTRA01Y | 50 bytes | Transaction category balance | Transaction Balance |
| 7 | CVTRA02Y | 50 bytes | Disclosure group record | Disclosure/Interest |
| 8 | CVTRA03Y | 60 bytes | Transaction type record | Reference Data |
| 9 | CVTRA04Y | 60 bytes | Transaction category type | Reference Data |
| 10 | CVTRA05Y | 350 bytes | Transaction record (online) | Transaction |
| 11 | CVTRA06Y | 350 bytes | Daily transaction record | Transaction |
| 12 | CVTRA07Y | N/A | Transaction report layout | Report |
| 13 | CSUSR01Y | 80 bytes | User security record | User/Security |
| 14 | COCOM01Y | N/A | Common communication area (COMMAREA) | Infrastructure |
| 15 | COMEN02Y | N/A | Menu option definitions | Navigation |
| 16 | COADM02Y | N/A | Admin menu option definitions | Navigation |
| 17 | COTTL01Y | N/A | Screen title/header | UI Infrastructure |
| 18 | CSDAT01Y | N/A | Date fields | Utility |
| 19 | CSMSG01Y | N/A | Message area (standard) | UI Infrastructure |
| 20 | CSMSG02Y | N/A | Message area (extended) | UI Infrastructure |
| 21 | CSSETATY | N/A | Set attribute bytes | UI Infrastructure |
| 22 | CSSTRPFY | N/A | String parsing functions | Utility |
| 23 | CSLKPCDY | N/A | Lookup code definitions | Reference Data |
| 24 | CSUTLDPY | N/A | Date utility parameters | Utility |
| 25 | CSUTLDWY | N/A | Date utility working storage | Utility |
| 26 | CODATECN | N/A | Date conversion constants | Utility |
| 27 | CUSTREC | N/A | Alternate customer record layout | Customer |
| 28 | CVEXPORT | N/A | Export record layout | Data Migration |
| 29 | COSTM01 | N/A | Statement transaction layout (re-keyed) | Reporting |
| 30 | UNUSED1Y | 80 bytes | Unused/deprecated record | Deprecated |

### 2.2 BMS-Generated Copybooks (`app/cpy-bms/`)

| # | Copybook | Corresponding BMS Map | Screen Purpose |
|---|----------|----------------------|----------------|
| 1 | COSGN00.CPY | COSGN00.bms | Sign-on screen |
| 2 | COMEN01.CPY | COMEN01.bms | Main menu screen |
| 3 | COADM01.CPY | COADM01.bms | Admin menu screen |
| 4 | COACTVW.CPY | COACTVW.bms | Account view screen |
| 5 | COACTUP.CPY | COACTUP.bms | Account update screen |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card list screen |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card detail screen |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card update screen |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction list screen |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction view screen |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction add screen |
| 12 | CORPT00.CPY | CORPT00.bms | Report parameters screen |
| 13 | COBIL00.CPY | COBIL00.bms | Bill payment screen |
| 14 | COUSR00.CPY | COUSR00.bms | User list screen |
| 15 | COUSR01.CPY | COUSR01.bms | User add screen |
| 16 | COUSR02.CPY | COUSR02.bms | User update screen |
| 17 | COUSR03.CPY | COUSR03.bms | User delete screen |

---

## 3. BMS Screen Maps (17 maps)

| # | Map | File | Screen | Fields | Function Keys |
|---|-----|------|--------|--------|---------------|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | Sign-on | User ID, Password | Enter, F3 |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | Main Menu | 12 menu options | Enter, F3 |
| 3 | COADM01 | `app/bms/COADM01.bms` | Admin Menu | Admin options | Enter, F3 |
| 4 | COACTVW | `app/bms/COACTVW.bms` | Account View | Account details, cards, balances | F3, F7/F8 (page) |
| 5 | COACTUP | `app/bms/COACTUP.bms` | Account Update | Editable account fields | Enter, F3, F5 |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | Card List | 7 card rows with selection | Enter, F3, F7/F8 |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | Card Detail | Card #, name, expiry, status | F3, F12 |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | Card Update | Editable card fields | Enter, F3, F5 |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | Transaction List | 10 transaction rows | Enter, F3, F7/F8 |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | Transaction View | Transaction details (read-only) | F3, F12 |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | Transaction Add | New transaction fields | Enter, F3, F5 |
| 12 | CORPT00 | `app/bms/CORPT00.bms` | Report Params | Date range, account, type | Enter, F3, F5 |
| 13 | COBIL00 | `app/bms/COBIL00.bms` | Bill Payment | Account, amount, confirmation | Enter, F3, F5 |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | User List | User rows with selection | Enter, F3, F7/F8 |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | User Add | First/Last, Password, Type | Enter, F3, F5 |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | User Update | Editable user fields | Enter, F3-F5, F12 |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | User Delete | User detail (confirm delete) | Enter, F3-F5 |

---

## 4. JCL Batch Jobs (38 jobs)

### 4.1 Data File Refresh Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 1 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | Refresh account VSAM from flat file | IDCAMS (REPRO) |
| 2 | CARDFILE | `app/jcl/CARDFILE.jcl` | Refresh card VSAM from flat file | IDCAMS (REPRO) |
| 3 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | Refresh customer VSAM from flat file | IDCAMS (REPRO) |
| 4 | XREFFILE | `app/jcl/XREFFILE.jcl` | Load cross-reference VSAM + alt index | IDCAMS |
| 5 | TRANFILE | `app/jcl/TRANFILE.jcl` | Load transaction VSAM from flat file | IDCAMS (REPRO) |
| 6 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | Load user security VSAM | IDCAMS |
| 7 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | Load transaction types VSAM | IDCAMS |
| 8 | TRANCATG | `app/jcl/TRANCATG.jcl` | Load transaction categories VSAM | IDCAMS |
| 9 | DISCGRP | `app/jcl/DISCGRP.jcl` | Load disclosure groups VSAM | IDCAMS |
| 10 | TCATBALF | `app/jcl/TCATBALF.jcl` | Load category balance VSAM | IDCAMS |

### 4.2 Batch Processing Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 11 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | Post daily transactions | CBTRN02C |
| 12 | INTCALC | `app/jcl/INTCALC.jcl` | Calculate interest on balances | CBACT04C |
| 13 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | Combine transaction backups | SORT + IDCAMS |
| 14 | CREASTMT | `app/jcl/CREASTMT.JCL` | Generate statements (text + HTML) | CBSTM03A |
| 15 | TRANREPT | `app/jcl/TRANREPT.jcl` | Transaction detail report | CBTRN03C |
| 16 | CBEXPORT | `app/jcl/CBEXPORT.jcl` | Export all data to flat file | CBEXPORT |
| 17 | CBIMPORT | `app/jcl/CBIMPORT.jcl` | Import data from flat file | CBIMPORT |

### 4.3 CICS File Management Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 18 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | Close CICS files for batch | DFHCSDUP |
| 19 | OPENFIL | `app/jcl/OPENFIL.jcl` | Open CICS files after batch | DFHCSDUP |

### 4.4 Backup and Archive Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 20 | TRANBKP | `app/jcl/TRANBKP.jcl` | Backup transaction VSAM to GDG | IDCAMS (REPRO) |
| 21 | TRANIDX | `app/jcl/TRANIDX.jcl` | Rebuild transaction alternate index | IDCAMS |

### 4.5 VSAM Definition Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 22 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | Define GDG bases for backups | IDCAMS |
| 23 | DEFGDGD | `app/jcl/DEFGDGD.jcl` | Define GDG bases + seed data | IEBGENER, IDCAMS |
| 24 | DEFCUST | `app/jcl/DEFCUST.jcl` | Define customer VSAM cluster | IDCAMS |
| 25 | ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | Define ESDS/RRDS VSAM examples | IDCAMS |

### 4.6 Reporting / Print Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 26 | READACCT | `app/jcl/READACCT.jcl` | Print account data | CBACT01C |
| 27 | READCARD | `app/jcl/READCARD.jcl` | Print card data | CBACT02C |
| 28 | READCUST | `app/jcl/READCUST.jcl` | Print customer data | CBCUS01C |
| 29 | READXREF | `app/jcl/READXREF.jcl` | Print cross-reference data | CBACT03C |
| 30 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | Print category balances + backup | IDCAMS |
| 31 | DALYREJS | `app/jcl/DALYREJS.jcl` | Define daily rejects GDG | IDCAMS |
| 32 | REPTFILE | `app/jcl/REPTFILE.jcl` | Report file definition | IDCAMS |
| 33 | TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | Convert statement text to PDF | TXT2PDF (REXX) |

### 4.7 Utility / Infrastructure Jobs

| # | Job | File | Function | Program Executed |
|---|-----|------|----------|-----------------|
| 34 | WAITSTEP | `app/jcl/WAITSTEP.jcl` | Wait step using COBSWAIT | COBSWAIT |
| 35 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | CICS CSD batch administration | DFHCSDUP |
| 36 | FTPJCL | `app/jcl/FTPJCL.JCL` | FTP file transfer | FTP |
| 37 | INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | Internal reader - triggers INTRDRJ2 | IDCAMS, IEBGENER |
| 38 | INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | Internal reader - chained job | IDCAMS |

---

## 5. Assembler Programs (2)

| # | Program | File | Function |
|---|---------|------|----------|
| 1 | MVSWAIT | `app/asm/MVSWAIT.asm` | MVS wait service (native) |
| 2 | COBDATFT | `app/asm/COBDATFT.asm` | COBOL date formatting utility |

---

## 6. JCL Procedures (2)

| # | Procedure | File | Function |
|---|-----------|------|----------|
| 1 | REPROC | `app/proc/REPROC.prc` | Reusable reporting procedure |
| 2 | TRANREPT | `app/proc/TRANREPT.prc` | Transaction report procedure |

---

## 7. Data Files (9 ASCII reference files)

| # | File | Description | Related VSAM |
|---|------|-------------|-------------|
| 1 | acctdata.txt | Account master data | ACCTDATA.VSAM.KSDS |
| 2 | carddata.txt | Card master data | CARDDATA.VSAM.KSDS |
| 3 | cardxref.txt | Card cross-reference | CARDXREF.VSAM.KSDS |
| 4 | custdata.txt | Customer data | CUSTDATA.VSAM.KSDS |
| 5 | dailytran.txt | Daily transactions | DALYTRAN.PS |
| 6 | discgrp.txt | Disclosure groups | DISCGRP.VSAM.KSDS |
| 7 | tcatbal.txt | Category balances | TCATBALF.VSAM.KSDS |
| 8 | trancatg.txt | Transaction categories | TRANCATG.VSAM.KSDS |
| 9 | trantype.txt | Transaction types | TRANTYPE.VSAM.KSDS |

---

## 8. Classification Summary

| Category | Count | Naming Convention |
|----------|-------|-------------------|
| Online CICS Programs | 17 | `CO*C` prefix |
| Batch COBOL Programs | 13 | `CB*C` / `CB*` prefix |
| Shared Utility Programs | 1 | `CS*C` prefix |
| Data Copybooks | 30 | `CV*Y` (data), `CS*Y` (shared), `CO*Y` (common) |
| BMS-Generated Copybooks | 17 | Same name as BMS map |
| BMS Screen Maps | 17 | Same name as online program (minus `C` suffix) |
| JCL Batch Jobs | 38 | Descriptive names |
| Assembler Programs | 2 | Descriptive names |
| JCL Procedures | 2 | Descriptive names |
| Optional Module Programs | 13 | Module-specific prefixes |
| **Total Artifacts** | **150** | |
