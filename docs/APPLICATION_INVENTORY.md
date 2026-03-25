# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: CardDemo — Mainframe Credit Card Management System
> **Platform**: COBOL / CICS / VSAM / JCL / BMS / IMS / DB2 / MQ

---

## Executive Summary

CardDemo is a mainframe credit card management application designed for modernization workshops. It comprises **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, **38 JCL batch jobs**, plus **13 programs in 3 optional extension modules** (IMS/DB2/MQ). The application supports two user roles (Regular and Admin) and covers account management, card management, transaction processing, bill payment, reporting, and user administration.

---

## 1. COBOL Programs (31 Core + 13 Optional)

### 1.1 Online CICS Programs (17)

| # | Program ID | File | Lines | Function | CICS Trans | BMS Map | Domain |
|---|-----------|------|-------|----------|-----------|---------|--------|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | 260 | Sign-on / Authentication | CC00 | COSGN00 | Security |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | 308 | Main Menu (Regular Users) | CM00 | COMEN01 | Navigation |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | 288 | Admin Menu | CA00 | COADM01 | Navigation |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | 941 | Account View (read-only) | CA01 | COACTVW | Account Mgmt |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | 4,236 | Account Update | CA02 | COACTUP | Account Mgmt |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | 1,459 | Credit Card List | CC01 | COCRDLI | Card Mgmt |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | 887 | Credit Card Detail View | CC02 | COCRDSL | Card Mgmt |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | 1,560 | Credit Card Update | CC03 | COCRDUP | Card Mgmt |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | 699 | Transaction List | CT00 | COTRN00 | Transactions |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | 330 | Transaction Detail View | CT01 | COTRN01 | Transactions |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | 783 | Transaction Add | CT02 | COTRN02 | Transactions |
| 12 | CORPT00C | `app/cbl/CORPT00C.cbl` | 649 | Transaction Report Request | CR00 | CORPT00 | Reporting |
| 13 | COBIL00C | `app/cbl/COBIL00C.cbl` | 572 | Bill Payment | CB00 | COBIL00 | Payments |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | 695 | User List (Admin) | CU00 | COUSR00 | User Admin |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | 299 | User Add (Admin) | CU01 | COUSR01 | User Admin |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | 414 | User Update (Admin) | CU02 | COUSR02 | User Admin |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | 359 | User Delete (Admin) | CU03 | COUSR03 | User Admin |

### 1.2 Batch Programs (13)

| # | Program ID | File | Lines | Function | Domain |
|---|-----------|------|-------|----------|--------|
| 1 | CBACT01C | `app/cbl/CBACT01C.cbl` | 430 | Read account file, write to output files | Account Mgmt |
| 2 | CBACT02C | `app/cbl/CBACT02C.cbl` | 178 | Read and print card data file | Card Mgmt |
| 3 | CBACT03C | `app/cbl/CBACT03C.cbl` | 178 | Read and print card cross-reference file | Card Mgmt |
| 4 | CBACT04C | `app/cbl/CBACT04C.cbl` | 652 | Interest calculation engine | Financial |
| 5 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | 178 | Read and print customer data file | Customer Mgmt |
| 6 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | 494 | Post daily transaction records (version 1) | Transactions |
| 7 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | 731 | Post daily transaction records (version 2) | Transactions |
| 8 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | 649 | Print transaction detail report | Reporting |
| 9 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | 924 | Generate account statements (text + HTML) | Reporting |
| 10 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | 230 | Statement file I/O subroutine (called by CBSTM03A) | Reporting |
| 11 | CBEXPORT | `app/cbl/CBEXPORT.cbl` | 582 | Export customer data for branch migration | Data Migration |
| 12 | CBIMPORT | `app/cbl/CBIMPORT.cbl` | 487 | Import customer data from branch migration export | Data Migration |
| 13 | COBSWAIT | `app/cbl/COBSWAIT.cbl` | 41 | Wait utility (parm in centiseconds) | Utility |

### 1.3 Utility Program (1)

| # | Program ID | File | Lines | Function | Domain |
|---|-----------|------|-------|----------|--------|
| 1 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | 157 | Date validation via CEEDAYS API | Utility |

### 1.4 Optional Module: Authorization (IMS/DB2/MQ) — 8 Programs

| # | Program ID | File | Lines | Function | Technologies |
|---|-----------|------|-------|----------|-------------|
| 1 | COPAUA0C | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | ~1,600 | Card authorization decision (MQ trigger) | CICS, IMS DL/I, MQ |
| 2 | COPAUS0C | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | ~900 | Authorization summary view | CICS, IMS DL/I, BMS |
| 3 | COPAUS1C | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | ~500 | Authorization detail view | CICS, IMS DL/I, BMS |
| 4 | COPAUS2C | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | ~400 | Mark authorization as fraud | CICS, IMS, DB2 SQL |
| 5 | CBPAUP0C | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | ~300 | Batch purge expired authorizations | Batch, IMS DL/I |
| 6 | DBUNLDGS | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | ~600 | Unload IMS DB to flat file | Batch, IMS DL/I |
| 7 | PAUDBLOD | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | ~600 | Load flat file into IMS DB | Batch, IMS DL/I |
| 8 | PAUDBUNL | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | ~500 | Unload IMS DB segments | Batch, IMS DL/I |

### 1.5 Optional Module: Transaction Type DB2 — 3 Programs

| # | Program ID | File | Lines | Function | Technologies |
|---|-----------|------|-------|----------|-------------|
| 1 | COTRTUPC | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | ~2,700 | Transaction type add/edit (DB2 CRUD) | CICS, DB2 SQL, BMS |
| 2 | COTRTLIC | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | ~1,700 | Transaction type list/delete (DB2 cursors) | CICS, DB2 SQL, BMS |
| 3 | COBTUPDT | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` | ~400 | Batch update transaction types | Batch, DB2 SQL |

### 1.6 Optional Module: VSAM-MQ — 2 Programs

| # | Program ID | File | Lines | Function | Technologies |
|---|-----------|------|-------|----------|-------------|
| 1 | COACCT01 | `app/app-vsam-mq/cbl/COACCT01.cbl` | ~1,000 | MQ request/response for account inquiry | CICS, MQ, VSAM |
| 2 | CODATE01 | `app/app-vsam-mq/cbl/CODATE01.cbl` | ~850 | MQ request/response for system date | CICS, MQ |

---

## 2. Copybooks (30 Core + 11 Optional)

### 2.1 Core Data Copybooks (30)

| # | Copybook | File | Business Domain | Description |
|---|---------|------|----------------|-------------|
| 1 | CVACT01Y | `app/cpy/CVACT01Y.cpy` | Account | Account master record (300 bytes) |
| 2 | CVACT02Y | `app/cpy/CVACT02Y.cpy` | Card | Card data record (150 bytes) |
| 3 | CVACT03Y | `app/cpy/CVACT03Y.cpy` | Card/Account | Card-to-account cross-reference (50 bytes) |
| 4 | CVCUS01Y | `app/cpy/CVCUS01Y.cpy` | Customer | Customer profile record (500 bytes) |
| 5 | CVCRD01Y | `app/cpy/CVCRD01Y.cpy` | Card | Card record layout for CICS programs |
| 6 | CVTRA01Y | `app/cpy/CVTRA01Y.cpy` | Transaction | Transaction category balance (50 bytes) |
| 7 | CVTRA02Y | `app/cpy/CVTRA02Y.cpy` | Transaction | Disclosure group record (50 bytes) |
| 8 | CVTRA03Y | `app/cpy/CVTRA03Y.cpy` | Transaction | Transaction type record (60 bytes) |
| 9 | CVTRA04Y | `app/cpy/CVTRA04Y.cpy` | Transaction | Transaction category type record (60 bytes) |
| 10 | CVTRA05Y | `app/cpy/CVTRA05Y.cpy` | Transaction | Online transaction record (350 bytes) |
| 11 | CVTRA06Y | `app/cpy/CVTRA06Y.cpy` | Transaction | Daily transaction record (350 bytes) |
| 12 | CVTRA07Y | `app/cpy/CVTRA07Y.cpy` | Reporting | Transaction detail report structure |
| 13 | CVEXPORT | `app/cpy/CVEXPORT.cpy` | Data Migration | Export/import multi-record layout |
| 14 | COSTM01 | `app/cpy/COSTM01.CPY` | Reporting | Transaction altered layout for reporting |
| 15 | COCOM01Y | `app/cpy/COCOM01Y.cpy` | Common | Common communication area (COMMAREA) |
| 16 | COMEN02Y | `app/cpy/COMEN02Y.cpy` | Navigation | Menu option definitions |
| 17 | COADM02Y | `app/cpy/COADM02Y.cpy` | Navigation | Admin menu option definitions |
| 18 | COTTL01Y | `app/cpy/COTTL01Y.cpy` | UI | Screen title/header definitions |
| 19 | CSUSR01Y | `app/cpy/CSUSR01Y.cpy` | Security | User security record |
| 20 | CSDAT01Y | `app/cpy/CSDAT01Y.cpy` | Common | Date/time working storage |
| 21 | CSMSG01Y | `app/cpy/CSMSG01Y.cpy` | Common | Message area definitions |
| 22 | CSMSG02Y | `app/cpy/CSMSG02Y.cpy` | Common | Extended message area definitions |
| 23 | CSLKPCDY | `app/cpy/CSLKPCDY.cpy` | Common | Lookup code definitions |
| 24 | CSSETATY | `app/cpy/CSSETATY.cpy` | UI | Attribute setting (COPY REPLACING) |
| 25 | CSSTRPFY | `app/cpy/CSSTRPFY.cpy` | Utility | String parsing functions |
| 26 | CSUTLDPY | `app/cpy/CSUTLDPY.cpy` | Utility | Date utility parameters |
| 27 | CSUTLDWY | `app/cpy/CSUTLDWY.cpy` | Utility | Date utility working storage |
| 28 | CODATECN | `app/cpy/CODATECN.cpy` | Utility | Date conversion record |
| 29 | CUSTREC | `app/cpy/CUSTREC.cpy` | Customer | Customer record (alt layout for statements) |
| 30 | UNUSED1Y | `app/cpy/UNUSED1Y.cpy` | N/A | Unused placeholder record |

### 2.2 Authorization Module Copybooks (9)

| # | Copybook | File | Description |
|---|---------|------|-------------|
| 1 | CCPAURQY | `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy` | Authorization request record |
| 2 | CCPAURLY | `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy` | Authorization reply record |
| 3 | CCPAUERY | `app/app-authorization-ims-db2-mq/cpy/CCPAUERY.cpy` | Authorization error record |
| 4 | CIPAUDTY | `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy` | IMS auth detail segment |
| 5 | CIPAUSMY | `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy` | IMS auth summary segment |
| 6 | IMSFUNCS | `app/app-authorization-ims-db2-mq/cpy/IMSFUNCS.cpy` | IMS DL/I function codes |
| 7 | PADFLPCB | `app/app-authorization-ims-db2-mq/cpy/PADFLPCB.CPY` | IMS PCB for detail flat file |
| 8 | PASFLPCB | `app/app-authorization-ims-db2-mq/cpy/PASFLPCB.CPY` | IMS PCB for summary flat file |
| 9 | PAUTBPCB | `app/app-authorization-ims-db2-mq/cpy/PAUTBPCB.CPY` | IMS PCB for auth base |

### 2.3 Transaction Type DB2 Copybooks (2)

| # | Copybook | File | Description |
|---|---------|------|-------------|
| 1 | CSDB2RPY | `app/app-transaction-type-db2/cpy/CSDB2RPY.cpy` | DB2 reply/result area |
| 2 | CSDB2RWY | `app/app-transaction-type-db2/cpy/CSDB2RWY.cpy` | DB2 read/write working storage |

---

## 3. BMS Screen Maps (17 Core + 4 Optional)

### 3.1 Core BMS Maps

| # | Map Name | File | Screen Title | Associated Program | Screen Size |
|---|---------|------|-------------|-------------------|-------------|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | Sign On | COSGN00C | 24x80 |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | Main Menu | COMEN01C | 24x80 |
| 3 | COADM01 | `app/bms/COADM01.bms` | Admin Menu | COADM01C | 24x80 |
| 4 | COACTVW | `app/bms/COACTVW.bms` | Account View | COACTVWC | 24x80 |
| 5 | COACTUP | `app/bms/COACTUP.bms` | Account Update | COACTUPC | 24x80 |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | Credit Card List | COCRDLIC | 24x80 |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | Credit Card Detail | COCRDSLC | 24x80 |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | Credit Card Update | COCRDUPC | 24x80 |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | Transaction List | COTRN00C | 24x80 |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | Transaction Detail | COTRN01C | 24x80 |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | Transaction Add | COTRN02C | 24x80 |
| 12 | CORPT00 | `app/bms/CORPT00.bms` | Transaction Report | CORPT00C | 24x80 |
| 13 | COBIL00 | `app/bms/COBIL00.bms` | Bill Payment | COBIL00C | 24x80 |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | User List | COUSR00C | 24x80 |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | Add User | COUSR01C | 24x80 |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | Update User | COUSR02C | 24x80 |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | Delete User | COUSR03C | 24x80 |

### 3.2 BMS-Generated Copybooks (17)

Each BMS map generates a corresponding copybook in `app/cpy-bms/` with the same name (e.g., `COSGN00.CPY`). These contain the symbolic map data structures used by COBOL programs for SEND MAP and RECEIVE MAP operations.

### 3.3 Optional Module BMS Maps (4)

| # | Map Name | Location | Associated Program |
|---|---------|---------|-------------------|
| 1 | COPAU00 | `app/app-authorization-ims-db2-mq/bms/` | COPAUS0C |
| 2 | COPAU01 | `app/app-authorization-ims-db2-mq/bms/` | COPAUS1C |
| 3 | COTRTLI | `app/app-transaction-type-db2/bms/` | COTRTLIC |
| 4 | COTRTUP | `app/app-transaction-type-db2/bms/` | COTRTUPC |

---

## 4. JCL Batch Jobs (38)

### 4.1 Data File Loading / Refresh Jobs

| # | Job Name | File | Function | Key Datasets |
|---|---------|------|----------|-------------|
| 1 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | Refresh account master VSAM | ACCTDATA.VSAM.KSDS |
| 2 | CARDFILE | `app/jcl/CARDFILE.jcl` | Refresh card master VSAM | CARDDATA.VSAM.KSDS |
| 3 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | Refresh customer master VSAM | CUSTDATA.VSAM.KSDS |
| 4 | XREFFILE | `app/jcl/XREFFILE.jcl` | Load card cross-reference VSAM + AIX | CARDXREF.VSAM.KSDS |
| 5 | TRANFILE | `app/jcl/TRANFILE.jcl` | Load transaction master VSAM | TRANSACT.VSAM.KSDS |
| 6 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | Load user security VSAM | USRSEC.VSAM.KSDS |
| 7 | REPTFILE | `app/jcl/REPTFILE.jcl` | Load reporting reference files | TRANTYPE, TRANCATG, DISCGRP, TCATBAL |

### 4.2 Core Batch Processing Jobs

| # | Job Name | File | Function | Executes Program |
|---|---------|------|----------|-----------------|
| 8 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | Post daily transactions | CBTRN02C |
| 9 | INTCALC | `app/jcl/INTCALC.jcl` | Calculate interest | CBACT04C |
| 10 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | Combine daily transactions into master | SORT/IDCAMS |
| 11 | CREASTMT | `app/jcl/CREASTMT.JCL` | Create account statements (text + HTML) | CBSTM03A |
| 12 | TRANREPT | `app/jcl/TRANREPT.jcl` | Generate transaction detail report | CBTRN03C |

### 4.3 File Maintenance / Utility Jobs

| # | Job Name | File | Function |
|---|---------|------|----------|
| 13 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | Close CICS files for batch processing |
| 14 | OPENFIL | `app/jcl/OPENFIL.jcl` | Re-open CICS files after batch |
| 15 | TRANBKP | `app/jcl/TRANBKP.jcl` | Backup transaction file |
| 16 | TRANIDX | `app/jcl/TRANIDX.jcl` | Define/build alternate indexes on TRANSACT |
| 17 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | Define GDG base for backups |
| 18 | DEFGDGD | `app/jcl/DEFGDGD.jcl` | Define GDG base for daily files |
| 19 | DEFCUST | `app/jcl/DEFCUST.jcl` | Define customer VSAM cluster |

### 4.4 Data Export/Import Jobs

| # | Job Name | File | Function | Executes Program |
|---|---------|------|----------|-----------------|
| 20 | CBEXPORT | `app/jcl/CBEXPORT.jcl` | Export customer data for migration | CBEXPORT |
| 21 | CBIMPORT | `app/jcl/CBIMPORT.jcl` | Import customer data from migration | CBIMPORT |

### 4.5 Reporting / Read Jobs

| # | Job Name | File | Function | Executes Program |
|---|---------|------|----------|-----------------|
| 22 | READACCT | `app/jcl/READACCT.jcl` | Read/print account file | CBACT01C |
| 23 | READCARD | `app/jcl/READCARD.jcl` | Read/print card file | CBACT02C |
| 24 | READCUST | `app/jcl/READCUST.jcl` | Read/print customer file | CBCUS01C |
| 25 | READXREF | `app/jcl/READXREF.jcl` | Read/print cross-reference file | CBACT03C |
| 26 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | Print category balance file | (utility) |

### 4.6 Administrative / Reference Data Jobs

| # | Job Name | File | Function |
|---|---------|------|----------|
| 27 | TCATBALF | `app/jcl/TCATBALF.jcl` | Define/load transaction category balance VSAM |
| 28 | TRANCATG | `app/jcl/TRANCATG.jcl` | Define/load transaction category VSAM |
| 29 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | Define/load transaction type VSAM |
| 30 | DISCGRP | `app/jcl/DISCGRP.jcl` | Define/load disclosure group VSAM |
| 31 | DALYREJS | `app/jcl/DALYREJS.jcl` | Define daily rejects file |
| 32 | ESDSRRDS | `app/jcl/ESDSRRDS.jcl` | Define ESDS and RRDS clusters |
| 33 | WAITSTEP | `app/jcl/WAITSTEP.jcl` | Wait step utility (calls COBSWAIT) |

### 4.7 Infrastructure / FTP Jobs

| # | Job Name | File | Function |
|---|---------|------|----------|
| 34 | FTPJCL | `app/jcl/FTPJCL.JCL` | FTP file transfer job |
| 35 | INTRDRJ1 | `app/jcl/INTRDRJ1.JCL` | Internal reader job (triggers INTRDRJ2) |
| 36 | INTRDRJ2 | `app/jcl/INTRDRJ2.JCL` | Internal reader job (backup copy) |
| 37 | TXT2PDF1 | `app/jcl/TXT2PDF1.JCL` | Convert text statement to PDF |
| 38 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | Admin batch compilation/deployment |

---

## 5. Assembler Programs (2)

| # | Program | File | Function |
|---|---------|------|----------|
| 1 | MVSWAIT | `app/asm/MVSWAIT.asm` | Wait service (called by COBSWAIT) |
| 2 | COBDATFT | `app/asm/COBDATFT.asm` | Date formatting utility |

---

## 6. JCL Procedures (2)

| # | Procedure | File | Function |
|---|----------|------|----------|
| 1 | REPROC | `app/proc/REPROC.prc` | Reprocessing procedure |
| 2 | TRANREPT | `app/proc/TRANREPT.prc` | Transaction report procedure |

---

## 7. Batch Processing Cycle

The standard nightly batch cycle runs in this order:

```
CLOSEFIL  -->  Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
          -->  POSTTRAN (post daily transactions)
          -->  INTCALC (calculate interest)
          -->  TRANBKP (backup transactions)
          -->  COMBTRAN (combine transactions)
          -->  CREASTMT (produce statements)
          -->  TRANIDX (rebuild indexes)
          -->  OPENFIL
```

---

## 8. Classification Summary

| Category | Count | Classification |
|----------|-------|---------------|
| Online CICS Programs | 17 | Core |
| Batch COBOL Programs | 13 | Core |
| Utility Programs | 1 | Core |
| Authorization Module Programs | 8 | Optional Extension |
| Transaction Type DB2 Programs | 3 | Optional Extension |
| VSAM-MQ Programs | 2 | Optional Extension |
| Core Copybooks | 30 | Core |
| Optional Copybooks | 11 | Optional Extension |
| BMS Maps (Core) | 17 | Core |
| BMS Maps (Optional) | 4 | Optional Extension |
| JCL Jobs | 38 | Core |
| Assembler Programs | 2 | Core |
| JCL Procedures | 2 | Core |
| **Total Artifacts** | **148** | |
