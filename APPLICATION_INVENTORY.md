# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Technology Stack:** COBOL / CICS / VSAM / BMS / JCL / Assembler

---

## Table of Contents

1. [Summary Statistics](#summary-statistics)
2. [COBOL Programs — Core (app/cbl/)](#cobol-programs--core)
3. [COBOL Programs — Optional Modules](#cobol-programs--optional-modules)
4. [Copybooks — Data Structures (app/cpy/)](#copybooks--data-structures)
5. [Copybooks — BMS-Generated (app/cpy-bms/)](#copybooks--bms-generated)
6. [BMS Screen Maps (app/bms/)](#bms-screen-maps)
7. [JCL Batch Jobs (app/jcl/)](#jcl-batch-jobs)
8. [Assembler Programs (app/asm/)](#assembler-programs)
9. [JCL Procedures (app/proc/)](#jcl-procedures)
10. [Support Artifacts](#support-artifacts)

---

## Summary Statistics

| Artifact Type              | Count | Total Lines |
|----------------------------|------:|------------:|
| COBOL Programs (Core)      |    31 |      20,650 |
| COBOL Programs (Optional)  |    10 |       8,473 |
| Copybooks (Data)           |    32 |       ~2,800 |
| Copybooks (BMS-Generated)  |    17 |         — |
| BMS Screen Maps            |    17 |       ~3,900 |
| JCL Batch Jobs             |    38 |       ~3,600 |
| Assembler Programs         |     2 |         — |
| JCL Procedures             |     2 |         — |
| **Grand Total**            | **149** |  **~39,400+** |

---

## COBOL Programs — Core

### Online CICS Programs (CO* prefix)

These programs implement the interactive 3270 terminal UI via CICS transactions.

| # | Program | Lines | Bytes | Function | Business Domain | CICS Trans |
|---|---------|------:|------:|----------|-----------------|------------|
| 1 | COSGN00C.cbl | 260 | 10,288 | **Sign-on Screen** — authenticates users, routes Admin vs Regular | Security / Auth | CC00 |
| 2 | COMEN01C.cbl | 308 | 12,461 | **Main Menu** — displays menu options, dispatches to sub-programs | Navigation | CM00 |
| 3 | COADM01C.cbl | 288 | 22,736 | **Admin Menu** — admin-specific menu for user/transaction mgmt | Administration | CA00 |
| 4 | COACTVWC.cbl | 941 | 74,764 | **Account View** — read-only display of account details | Account Mgmt | CA01 |
| 5 | COACTUPC.cbl | 4,236 | 182,463 | **Account Update** — full account maintenance with validation | Account Mgmt | CA02 |
| 6 | COCRDLIC.cbl | 1,459 | 117,376 | **Card List** — paginated card listing with browse/filter | Card Mgmt | CC01 |
| 7 | COCRDSLC.cbl | 887 | 71,308 | **Card View/Select** — card detail selection screen | Card Mgmt | CC02 |
| 8 | COCRDUPC.cbl | 1,560 | 125,961 | **Card Update** — card maintenance with validation | Card Mgmt | CC03 |
| 9 | COTRN00C.cbl | 699 | 29,270 | **Transaction List** — paginated transaction listing | Transaction Mgmt | CT00 |
| 10 | COTRN01C.cbl | 330 | 14,244 | **Transaction View** — read-only transaction detail | Transaction Mgmt | CT01 |
| 11 | COTRN02C.cbl | 783 | 33,665 | **Transaction Add** — new transaction entry with validation | Transaction Mgmt | CT02 |
| 12 | CORPT00C.cbl | 649 | 28,302 | **Transaction Reports** — report request screen, writes to TD queue | Reporting | CR00 |
| 13 | COBIL00C.cbl | 572 | 23,426 | **Bill Payment** — process bill payments against accounts | Billing | CB00 |
| 14 | COUSR00C.cbl | 695 | 29,285 | **User List** — paginated user listing (Admin only) | User Admin | CU00 |
| 15 | COUSR01C.cbl | 299 | 12,571 | **User Add** — create new Regular/Admin user | User Admin | CU01 |
| 16 | COUSR02C.cbl | 414 | 17,611 | **User Update** — modify existing user record | User Admin | CU02 |
| 17 | COUSR03C.cbl | 359 | 15,038 | **User Delete** — remove user with confirmation | User Admin | CU03 |

### Batch Programs (CB* prefix)

These programs run in batch mode via JCL job submission.

| # | Program | Lines | Bytes | Function | Business Domain |
|---|---------|------:|------:|----------|-----------------|
| 18 | CBTRN02C.cbl | 731 | 58,890 | **Transaction Posting** — posts daily transactions to master file, updates balances | Transaction Processing |
| 19 | CBACT04C.cbl | 652 | 52,479 | **Interest Calculation** — computes interest on accounts using disclosure groups | Financial Calc |
| 20 | CBSTM03A.CBL | 924 | 35,574 | **Statement Generation (Main)** — prints account statements in text + HTML | Reporting |
| 21 | CBSTM03B.CBL | 230 | 6,983 | **Statement Generation (Sub)** — file I/O subroutine called by CBSTM03A | Reporting |
| 22 | CBTRN03C.cbl | 649 | 52,239 | **Transaction Report** — generates daily transaction report | Reporting |
| 23 | CBTRN01C.cbl | 494 | 17,967 | **Transaction File Read** — reads and validates transaction records | Data Access |
| 24 | CBACT01C.cbl | 430 | 17,450 | **Account File Read** — reads account VSAM file, outputs multiple formats | Data Access |
| 25 | CBACT02C.cbl | 178 | 14,096 | **Card File Read** — reads card data VSAM file | Data Access |
| 26 | CBACT03C.cbl | 178 | 14,101 | **Cross-Reference Read** — reads card cross-reference VSAM file | Data Access |
| 27 | CBCUS01C.cbl | 178 | 6,914 | **Customer File Read** — reads customer VSAM file | Data Access |
| 28 | CBEXPORT.cbl | 582 | 24,197 | **Data Export** — exports all VSAM files to a single sequential file | Data Migration |
| 29 | CBIMPORT.cbl | 487 | 20,239 | **Data Import** — imports sequential data into individual VSAM files | Data Migration |
| 30 | COBSWAIT.cbl | 41 | 2,020 | **Wait Utility** — calls assembler MVSWAIT for timed delays | Utility |

### Shared Utility Programs

| # | Program | Lines | Bytes | Function | Business Domain |
|---|---------|------:|------:|----------|-----------------|
| 31 | CSUTLDTC.cbl | 157 | 11,608 | **Date Validation** — calls LE CEEDAYS for date validation/conversion | Utility |

---

## COBOL Programs — Optional Modules

### Authorization Module (app/app-authorization-ims-db2-mq/)

IMS DB + DB2 + MQ integration for payment authorization.

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 32 | COPAUA0C.cbl | 1,026 | **MQ Trigger** — receives auth requests from MQ, processes via IMS/DB2 | CICS + MQ |
| 33 | COPAUS0C.cbl | 1,032 | **Auth Summary** — displays authorization summary list | CICS + BMS |
| 34 | COPAUS1C.cbl | 604 | **Auth Detail** — detail view of individual authorization message | CICS + BMS |
| 35 | COPAUS2C.cbl | 244 | **Fraud Marking** — marks transactions as fraudulent in DB2 | CICS + DB2 |
| 36 | CBPAUP0C.cbl | 386 | **Batch Purge** — purges old authorization records | Batch |
| 37 | PAUDBLOD.CBL | — | **IMS DB Load** — loads authorization data into IMS database | IMS Batch |
| 38 | PAUDBUNL.CBL | — | **IMS DB Unload** — unloads authorization data from IMS database | IMS Batch |
| 39 | DBUNLDGS.CBL | — | **IMS DB Unload (GS)** — generic segment unload from IMS | IMS Batch |

### Transaction Type DB2 Module (app/app-transaction-type-db2/)

DB2-based CRUD for managing transaction type reference data.

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 40 | COTRTLIC.cbl | 2,098 | **Tran Type List** — paginated list with DB2 cursors, delete support | CICS + DB2 |
| 41 | COTRTUPC.cbl | 1,702 | **Tran Type Update** — add/edit transaction types in DB2 | CICS + DB2 |
| 42 | COBTUPDT.cbl | 237 | **Batch Tran Type Update** — batch maintenance of DB2 tran types | Batch + DB2 |

### VSAM-MQ Module (app/app-vsam-mq/)

MQ request/response services for system queries.

| # | Program | Lines | Function | Technology |
|---|---------|------:|----------|------------|
| 43 | CODATE01.cbl | 524 | **System Date Service** — returns system date via MQ request/response | Batch + MQ |
| 44 | COACCT01.cbl | 620 | **Account Inquiry Service** — account lookup via MQ request/response | Batch + MQ + VSAM |

---

## Copybooks — Data Structures

### Core Data Copybooks (app/cpy/)

| # | Copybook | Record Len | Description | Business Entity |
|---|----------|----------:|-------------|-----------------|
| 1 | CVACT01Y.cpy | 300 | Account master record layout | Account |
| 2 | CVACT02Y.cpy | 150 | Card data record layout | Card |
| 3 | CVACT03Y.cpy | 50 | Card cross-reference (card→account) | Card-Account XREF |
| 4 | CVCUS01Y.cpy | 500 | Customer master record layout | Customer |
| 5 | CVCRD01Y.cpy | — | Card detail record (extended) | Card Detail |
| 6 | CVTRA01Y.cpy | 50 | Transaction category balance record | Category Balance |
| 7 | CVTRA02Y.cpy | 50 | Disclosure group record (interest rates) | Disclosure Group |
| 8 | CVTRA03Y.cpy | 60 | Transaction type record | Transaction Type |
| 9 | CVTRA04Y.cpy | 60 | Transaction category type record | Category Type |
| 10 | CVTRA05Y.cpy | 350 | Transaction master record | Transaction |
| 11 | CVTRA06Y.cpy | 350 | Daily transaction record | Daily Transaction |
| 12 | CVTRA07Y.cpy | — | Transaction report layout (headers, totals) | Report Layout |
| 13 | CVEXPORT.cpy | — | Export record layouts for all entities | Export Format |
| 14 | COSTM01.CPY | 350 | Transaction record re-keyed (card+tran ID) for statements | Statement Layout |
| 15 | CUSTREC.cpy | — | Customer record for statement processing | Customer (Statement) |
| 16 | CSUSR01Y.cpy | 80 | User security record (userid, password, type) | User Security |
| 17 | UNUSED1Y.cpy | 80 | Unused placeholder record | — |

### Framework/Infrastructure Copybooks

| # | Copybook | Description | Purpose |
|---|----------|-------------|---------|
| 18 | COCOM01Y.cpy | Common communication area (COMMAREA) | CICS inter-program data |
| 19 | COMEN02Y.cpy | Menu option definitions | Menu navigation |
| 20 | COADM02Y.cpy | Admin menu option definitions | Admin navigation |
| 21 | COTTL01Y.cpy | Screen title/header fields | UI headers |
| 22 | CSDAT01Y.cpy | Date display fields | Date formatting |
| 23 | CSMSG01Y.cpy | Message area (single line) | User messages |
| 24 | CSMSG02Y.cpy | Message area (two lines) | User messages |
| 25 | CSSETATY.cpy | Field attribute setting (COPY REPLACING) | BMS field attributes |
| 26 | CSSTRPFY.cpy | String processing functions | String utilities |
| 27 | CSUTLDPY.cpy | Date utility procedures | Date processing |
| 28 | CSUTLDWY.cpy | Date utility working storage | Date working storage |
| 29 | CODATECN.cpy | Date conversion record | Date conversion |
| 30 | CSLKPCDY.cpy | Lookup code table (51KB — largest copybook) | Reference data lookup |

### Optional Module Copybooks

| # | Copybook | Location | Description |
|---|----------|----------|-------------|
| 31 | CIPAUSMY.cpy | auth-ims-db2-mq/cpy/ | Auth message summary layout |
| 32 | CIPAUDTY.cpy | auth-ims-db2-mq/cpy/ | Auth message detail layout |
| 33 | CCPAURQY.cpy | auth-ims-db2-mq/cpy/ | Auth MQ request layout |
| 34 | CCPAURLY.cpy | auth-ims-db2-mq/cpy/ | Auth MQ reply layout |
| 35 | CCPAUERY.cpy | auth-ims-db2-mq/cpy/ | Auth error layout |
| 36 | IMSFUNCS.cpy | auth-ims-db2-mq/cpy/ | IMS function codes |
| 37 | PAUTBPCB.cpy | auth-ims-db2-mq/cpy/ | IMS PCB mask |
| 38 | PASFLPCB.cpy | auth-ims-db2-mq/cpy/ | IMS PCB for auth summary |
| 39 | PADFLPCB.cpy | auth-ims-db2-mq/cpy/ | IMS PCB for auth detail |
| 40 | CSDB2RWY.cpy | tran-type-db2/cpy/ | DB2 common working storage |
| 41 | CSDB2RPY.cpy | tran-type-db2/cpy/ | DB2 common procedures |

---

## Copybooks — BMS-Generated

Auto-generated from BMS maps; one per screen definition (app/cpy-bms/).

| # | Copybook | Generated From | Screen |
|---|----------|---------------|--------|
| 1 | COSGN00.CPY | COSGN00.bms | Sign-on |
| 2 | COMEN01.CPY | COMEN01.bms | Main Menu |
| 3 | COADM01.CPY | COADM01.bms | Admin Menu |
| 4 | COACTVW.CPY | COACTVW.bms | Account View |
| 5 | COACTUP.CPY | COACTUP.bms | Account Update |
| 6 | COCRDLI.CPY | COCRDLI.bms | Card List |
| 7 | COCRDSL.CPY | COCRDSL.bms | Card Select |
| 8 | COCRDUP.CPY | COCRDUP.bms | Card Update |
| 9 | COTRN00.CPY | COTRN00.bms | Transaction List |
| 10 | COTRN01.CPY | COTRN01.bms | Transaction View |
| 11 | COTRN02.CPY | COTRN02.bms | Transaction Add |
| 12 | CORPT00.CPY | CORPT00.bms | Reports |
| 13 | COBIL00.CPY | COBIL00.bms | Bill Payment |
| 14 | COUSR00.CPY | COUSR00.bms | User List |
| 15 | COUSR01.CPY | COUSR01.bms | User Add |
| 16 | COUSR02.CPY | COUSR02.bms | User Update |
| 17 | COUSR03.CPY | COUSR03.bms | User Delete |

---

## BMS Screen Maps

All maps define 3270 terminal screens (app/bms/).

| # | Map | Lines | Screen Title | Programs Using It |
|---|-----|------:|-------------|-------------------|
| 1 | COSGN00.bms | 210 | Login Screen | COSGN00C |
| 2 | COMEN01.bms | 167 | Main Menu | COMEN01C |
| 3 | COADM01.bms | 164 | Admin Menu | COADM01C |
| 4 | COACTVW.bms | 353 | Account View | COACTVWC |
| 5 | COACTUP.bms | 488 | Account Update | COACTUPC |
| 6 | COCRDLI.bms | 344 | Card Listing | COCRDLIC |
| 7 | COCRDSL.bms | 157 | Card Selection | COCRDSLC |
| 8 | COCRDUP.bms | 172 | Card Update | COCRDUPC |
| 9 | COTRN00.bms | 464 | Transaction List | COTRN00C |
| 10 | COTRN01.bms | 273 | Transaction View | COTRN01C |
| 11 | COTRN02.bms | 307 | Transaction Add | COTRN02C |
| 12 | CORPT00.bms | 231 | Transaction Reports | CORPT00C |
| 13 | COBIL00.bms | 141 | Bill Payment | COBIL00C |
| 14 | COUSR00.bms | 463 | User List | COUSR00C |
| 15 | COUSR01.bms | 164 | User Add | COUSR01C |
| 16 | COUSR02.bms | 169 | User Update | COUSR02C |
| 17 | COUSR03.bms | 153 | User Delete | COUSR03C |

---

## JCL Batch Jobs

### Data Load / Refresh Jobs

| # | Job | Function | Key Datasets | Programs |
|---|-----|----------|-------------|----------|
| 1 | DUSRSECJ.jcl | Load user security VSAM from flat file | USRSEC.PS → USRSEC.VSAM.KSDS | IEBGENER, IDCAMS |
| 2 | ACCTFILE.jcl | Refresh account master VSAM | ACCTDATA.PS → ACCTDATA.VSAM.KSDS | IDCAMS |
| 3 | CARDFILE.jcl | Refresh card data VSAM (with CICS close/open) | CARDDATA.PS → CARDDATA.VSAM.KSDS | SDSF, IDCAMS |
| 4 | CUSTFILE.jcl | Refresh customer master VSAM | CUSTDATA.PS → CUSTDATA.VSAM.KSDS | SDSF, IDCAMS |
| 5 | XREFFILE.jcl | Load card cross-reference VSAM + alt index | CARDXREF.PS → CARDXREF.VSAM.KSDS | IDCAMS |
| 6 | TRANFILE.jcl | Load transaction master VSAM + alt index | DALYTRAN.PS.INIT → TRANSACT.VSAM.KSDS | SDSF, IDCAMS |
| 7 | TCATBALF.jcl | Load transaction category balance VSAM | TCATBALF.PS → TCATBALF.VSAM.KSDS | IDCAMS |
| 8 | DISCGRP.jcl | Load disclosure group VSAM | DISCGRP.PS → DISCGRP.VSAM.KSDS | IDCAMS |
| 9 | TRANTYPE.jcl | Load transaction type VSAM | TRANTYPE.PS → TRANTYPE.VSAM.KSDS | IDCAMS |
| 10 | TRANCATG.jcl | Load transaction category VSAM | TRANCATG.PS → TRANCATG.VSAM.KSDS | IDCAMS |
| 11 | ESDSRRDS.jcl | Load ESDS/RRDS demo VSAM files | ESDSRRDS.PS → VSAM ESDS/RRDS | IDCAMS |
| 12 | DEFCUST.jcl | Define customer VSAM cluster | — | IDCAMS |

### Core Batch Processing Jobs

| # | Job | Function | Key Datasets | Programs |
|---|-----|----------|-------------|----------|
| 13 | CLOSEFIL.jcl | Close CICS files for batch window | — | SDSF |
| 14 | OPENFIL.jcl | Open CICS files after batch window | — | SDSF |
| 15 | POSTTRAN.jcl | **Post daily transactions** to master | DALYTRAN → TRANSACT, ACCTDATA, TCATBALF | **CBTRN02C** |
| 16 | INTCALC.jcl | **Calculate interest** on accounts | ACCTDATA, DISCGRP, CARDXREF, TCATBALF | **CBACT04C** |
| 17 | TRANBKP.jcl | Backup transaction master to GDG | TRANSACT.VSAM → TRANSACT.BKUP(+1) | IDCAMS |
| 18 | COMBTRAN.jcl | Combine system + backup transactions | SYSTRAN + TRANSACT.BKUP → COMBINED(+1) | SORT, IDCAMS |
| 19 | CREASTMT.JCL | **Generate account statements** (text + HTML) | TRANSACT, XREF, ACCT, CUST → STATEMNT | SORT, **CBSTM03A** |
| 20 | TRANREPT.jcl | **Generate daily transaction report** | TRANSACT, XREF, TRANTYPE, TRANCATG → TRANREPT | SORT, **CBTRN03C** |
| 21 | TRANIDX.jcl | Define transaction alternate index | TRANSACT.VSAM.KSDS | IDCAMS |
| 22 | WAITSTEP.jcl | Timer wait utility | — | **COBSWAIT** |

### Utility / Read / Report Jobs

| # | Job | Function | Key Datasets | Programs |
|---|-----|----------|-------------|----------|
| 23 | READACCT.jcl | Read account data to flat files | ACCTDATA.VSAM → PS/ARRY/VB | **CBACT01C** |
| 24 | READCARD.jcl | Read card data to flat file | CARDDATA.VSAM | **CBACT02C** |
| 25 | READCUST.jcl | Read customer data to flat file | CUSTDATA.VSAM | **CBCUS01C** |
| 26 | READXREF.jcl | Read cross-reference data | CARDXREF.VSAM | **CBACT03C** |
| 27 | CBEXPORT.jcl | Export all VSAM to single file | All VSAM → EXPORT.DATA | **CBEXPORT** |
| 28 | CBIMPORT.jcl | Import from export file to VSAM | EXPORT.DATA → Multiple VSAM | **CBIMPORT** |
| 29 | REPTFILE.jcl | Define report file VSAM | — | IDCAMS |
| 30 | PRTCATBL.jcl | Print category balance report | TCATBALF → TCATBALF.REPT | SORT |
| 31 | DALYREJS.jcl | Define daily reject GDG | — | IDCAMS |
| 32 | TXT2PDF1.JCL | Convert statement text to PDF | STATEMNT.PS → STATEMNT.PS.PDF | TXT2PDF |

### Infrastructure / GDG / CICS Jobs

| # | Job | Function | Key Datasets | Programs |
|---|-----|----------|-------------|----------|
| 33 | DEFGDGB.jcl | Define GDG base clusters | Multiple GDG bases | IDCAMS |
| 34 | DEFGDGD.jcl | Backup GDG data (trantype, trancatg, discgrp) | Various PS → BKUP GDG | IEBGENER, IDCAMS |
| 35 | CBADMCDJ.jcl | Load CICS CSD resource definitions | CARDDEMO.CSD | DFHCSDUP |
| 36 | FTPJCL.JCL | FTP file transfer (send/receive) | FTP.TEST | FTP |
| 37 | INTRDRJ1.JCL | Internal reader — trigger chain job | FTP.TEST → BKUP | IDCAMS, IEBGENER |
| 38 | INTRDRJ2.JCL | Internal reader — chained job | FTP.TEST.BKUP → INTRDR | IDCAMS |

---

## Assembler Programs

| # | Program | Location | Function |
|---|---------|----------|----------|
| 1 | MVSWAIT.asm | app/asm/ | Timer wait — called by COBSWAIT for batch delays |
| 2 | COBDATFT.asm | app/asm/ | Date formatting — called by CBACT01C for date conversion |

---

## JCL Procedures

| # | Procedure | Location | Function |
|---|-----------|----------|----------|
| 1 | REPROC.prc | app/proc/ | Reusable report processing procedure |
| 2 | TRANREPT.prc | app/proc/ | Transaction report procedure |

---

## Support Artifacts

### Sample Data Files (app/data/ASCII/)

| File | Business Entity |
|------|-----------------|
| acctdata.txt | Account master records |
| carddata.txt | Card master records |
| cardxref.txt | Card-to-account cross-reference |
| custdata.txt | Customer master records |
| dailytran.txt | Daily transaction records |
| tcatbal.txt | Transaction category balances |
| trancatg.txt | Transaction category definitions |
| trantype.txt | Transaction type definitions |
| discgrp.txt | Disclosure group (interest rate) records |

### CICS Resource Definitions

| File | Location | Purpose |
|------|----------|---------|
| CARDDEMO.CSD | app/csd/ | CICS System Definition — defines transactions, programs, files, maps |

### Job Scheduler Configurations

| File | Location | Purpose |
|------|----------|---------|
| CardDemo.ca7 | app/scheduler/ | CA-7 job scheduling definitions |
| CardDemo.controlm | app/scheduler/ | Control-M job scheduling definitions |

---

## Classification Legend

| Classification | Prefix | Runtime | Description |
|---------------|--------|---------|-------------|
| Online CICS | CO* | CICS region | Interactive 3270 terminal programs |
| Batch | CB* | JCL/JES | Scheduled batch processing programs |
| Shared Utility | CS* | Both | Utility routines called by both online and batch |
| Data Copybook | CV* | N/A | VSAM record layouts (data structures) |
| Framework Copybook | CO*/CS* | N/A | Common areas, messages, attributes |
| BMS Map | CO* | CICS | 3270 screen definitions |
| Data Load JCL | *FILE | JES | VSAM file load/refresh jobs |
| Processing JCL | Various | JES | Core batch processing cycle jobs |

### Batch Processing Cycle Order

```
CLOSEFIL → Data Refresh (ACCTFILE, CARDFILE, etc.)
         → POSTTRAN (transaction posting)
         → INTCALC (interest calculation)
         → TRANBKP (backup transactions)
         → COMBTRAN (combine transactions)
         → CREASTMT (create statements)
         → TRANIDX (rebuild alternate indexes)
         → OPENFIL
```
