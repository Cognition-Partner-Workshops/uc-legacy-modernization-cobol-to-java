# CardDemo Application Inventory

> Complete catalog of all programs, copybooks, JCL jobs, BMS maps, and assembler modules in the CardDemo mainframe credit card management system.

---

## Summary

| Artifact Type         | Count |
|:----------------------|------:|
| COBOL Programs (Core) |    31 |
| COBOL Programs (Opt.) |    13 |
| Copybooks (Core)      |    30 |
| Copybooks (Optional)  |    11 |
| BMS Maps (Core)       |    17 |
| BMS Maps (Optional)   |     4 |
| BMS Copybooks         |    21 |
| JCL Jobs (Core)       |    38 |
| JCL Jobs (Optional)   |     8 |
| Assembler Modules     |     2 |
| Procedures (PROC)     |     2 |
| Scheduler Definitions |     2 |
| **Total**             | **179** |

---

## 1. Online COBOL Programs (CICS)

Programs executed via CICS transactions, providing the 3270 terminal-based user interface.

| Program    | Lines | Trans ID | Function                              | Module        | Technologies          |
|:-----------|------:|:---------|:--------------------------------------|:--------------|:----------------------|
| COSGN00C   |   260 | CC00     | Sign-on / Authentication              | Core          | CICS, VSAM            |
| COMEN01C   |   308 | CM00     | Main Menu (Regular Users)             | Core          | CICS, XCTL            |
| COADM01C   |   288 | CA00     | Admin Menu                            | Core          | CICS, XCTL            |
| COACTVWC   |   941 | CAVW     | Account View                          | Core          | CICS, VSAM            |
| COACTUPC   | 4,236 | CAUP     | Account Update                        | Core          | CICS, VSAM            |
| COCRDLIC   | 1,459 | CCLI     | Credit Card List                      | Core          | CICS, VSAM, BROWSE    |
| COCRDSLC   |   887 | CCDL     | Credit Card Detail/View               | Core          | CICS, VSAM            |
| COCRDUPC   | 1,560 | CCUP     | Credit Card Update                    | Core          | CICS, VSAM, REWRITE   |
| COTRN00C   |   699 | CT00     | Transaction List                      | Core          | CICS, VSAM, BROWSE    |
| COTRN01C   |   330 | CT01     | Transaction View                      | Core          | CICS, VSAM            |
| COTRN02C   |   783 | CT02     | Transaction Add                       | Core          | CICS, VSAM, WRITE     |
| CORPT00C   |   649 | CR00     | Transaction Report (submit batch)     | Core          | CICS, TDQ, WRITEQ     |
| COBIL00C   |   572 | CB00     | Bill Payment                          | Core          | CICS, VSAM, REWRITE   |
| COUSR00C   |   695 | CU00     | User List (Admin)                     | Core          | CICS, VSAM, BROWSE    |
| COUSR01C   |   299 | CU01     | User Add (Admin)                      | Core          | CICS, VSAM, WRITE     |
| COUSR02C   |   414 | CU02     | User Update (Admin)                   | Core          | CICS, VSAM, REWRITE   |
| COUSR03C   |   359 | CU03     | User Delete (Admin)                   | Core          | CICS, VSAM, DELETE     |
| COPAUS0C   | 1,032 | CPVS     | Pending Auth Summary                  | Auth/IMS/MQ   | CICS, IMS DL/I, VSAM  |
| COPAUS1C   |   604 | CPVD     | Pending Auth Detail                   | Auth/IMS/MQ   | CICS, IMS DL/I, DB2   |
| COPAUS2C   |   244 | —        | Auth Detail Sub-screen                | Auth/IMS/MQ   | CICS, IMS             |
| COPAUA0C   | 1,026 | CP00     | Process Auth Requests (MQ trigger)    | Auth/IMS/MQ   | CICS, IMS DL/I, MQ    |
| COTRTLIC   | 2,098 | CTLI     | Tran Type List/Update/Delete (DB2)    | Tran Type/DB2 | CICS, DB2, Cursor     |
| COTRTUPC   | 1,702 | CTTU     | Tran Type Add/Edit (DB2)              | Tran Type/DB2 | CICS, DB2, SQL        |
| CODATE01   |   524 | CDRD     | System Date Inquiry via MQ            | VSAM-MQ       | CICS, MQ              |
| COACCT01   |   620 | CDRA     | Account Details Inquiry via MQ        | VSAM-MQ       | CICS, MQ, VSAM        |

---

## 2. Batch COBOL Programs

Programs executed in batch via JCL job submissions.

| Program    | Lines | Function                                   | Module        | Technologies          |
|:-----------|------:|:-------------------------------------------|:--------------|:----------------------|
| CBACT01C   |   430 | Read accounts, write to multiple formats   | Core          | Sequential, VSAM      |
| CBACT02C   |   178 | Read and print card data                   | Core          | Sequential            |
| CBACT03C   |   178 | Read and print cross-reference data        | Core          | Sequential            |
| CBACT04C   |   652 | Interest calculator                        | Core          | Sequential, VSAM I-O  |
| CBCUS01C   |   178 | Read and print customer data               | Core          | Sequential            |
| CBTRN01C   |   494 | Post daily transaction records (version 1) | Core          | Sequential, VSAM      |
| CBTRN02C   |   731 | Post daily transactions with validation    | Core          | Sequential, VSAM I-O  |
| CBTRN03C   |   649 | Print transaction detail report            | Core          | Sequential, VSAM      |
| CBSTM03A   |   924 | Print account statements (text + HTML)     | Core          | Sequential, CALL      |
| CBSTM03B   |   230 | File I/O subroutine for statements         | Core          | Sequential (sub-pgm)  |
| CBEXPORT   |   582 | Export customer data for branch migration  | Core          | Sequential, VSAM      |
| CBIMPORT   |   487 | Import data from branch migration export   | Core          | Sequential, VSAM      |
| COBSWAIT   |    41 | Wait utility (PARM in centiseconds)        | Core          | ASM CALL (MVSWAIT)    |
| CSUTLDTC   |   157 | Date validation utility (CEEDAYS)          | Core          | LE callable service   |
| CBPAUP0C   |   386 | Purge expired pending authorizations       | Auth/IMS/MQ   | IMS DL/I batch        |
| DBUNLDGS   |   366 | Unload IMS GS-AM data                      | Auth/IMS/MQ   | IMS DL/I              |
| PAUDBLOD   |   369 | Load pending auth IMS database             | Auth/IMS/MQ   | IMS DL/I              |
| PAUDBUNL   |   317 | Unload pending auth IMS database           | Auth/IMS/MQ   | IMS DL/I              |
| COBTUPDT   |   237 | Batch update transaction types (DB2)       | Tran Type/DB2 | DB2 SQL               |

---

## 3. Copybooks — Core Data Structures

Record layouts and shared working-storage areas included via `COPY`.

| Copybook   | Lines | Description                                       | Business Entity           |
|:-----------|------:|:--------------------------------------------------|:--------------------------|
| CVACT01Y   |    20 | Account master record (RECLN 300)                 | Account                   |
| CVACT02Y   |    14 | Card master record (RECLN 150)                    | Card                      |
| CVACT03Y   |    11 | Card–Account–Customer cross-reference (RECLN 50)  | Card XREF                 |
| CVCUS01Y   |    26 | Customer master record (RECLN 500)                | Customer                  |
| CUSTREC    |    26 | Customer record (alt layout, indented fields)     | Customer                  |
| CVTRA01Y   |    13 | Transaction category balance (RECLN 50)           | Tran Category Balance     |
| CVTRA02Y   |    13 | Disclosure group (RECLN 50)                       | Disclosure Group          |
| CVTRA03Y   |    10 | Transaction type (RECLN 60)                       | Transaction Type          |
| CVTRA04Y   |    12 | Transaction category type (RECLN 60)              | Transaction Category      |
| CVTRA05Y   |    21 | Transaction record (RECLN 350)                    | Transaction               |
| CVTRA06Y   |    21 | Daily transaction record (RECLN 350)              | Daily Transaction         |
| CVTRA07Y   |    73 | Transaction report layout                         | Reporting                 |
| COSTM01    |    38 | Statement-oriented transaction layout             | Reporting (Statement)     |
| CVEXPORT   |   103 | Multi-record export layout (COMP/COMP-3)          | Branch Migration          |
| COCOM01Y   |    47 | COMMAREA — inter-program communication            | Session / Navigation      |
| COMEN02Y   |   101 | Main menu option definitions                      | Menu Configuration        |
| COADM02Y   |    62 | Admin menu option definitions                     | Menu Configuration        |
| CVCRD01Y   |    46 | CC work areas (AID, navigation, messages)         | UI Control (Credit Card)  |
| COTTL01Y   |    27 | Screen title constants                            | UI                        |
| CSDAT01Y   |    58 | Date/time working storage                         | Utility (Date/Time)       |
| CODATECN   |    52 | Date format conversion record                     | Utility (Date Convert)    |
| CSLKPCDY   | 1,318 | Lookup codes (area codes, state codes, zip)       | Validation / Reference    |
| CSMSG01Y   |    24 | Common messages                                   | UI Messages               |
| CSMSG02Y   |    35 | Abend handler work areas                          | Error Handling            |
| CSSETATY   |    30 | Attribute-setting template (COPY REPLACING)       | UI (BMS Attributes)       |
| CSSTRPFY   |    85 | String-strip/pad utility fields                   | Utility                   |
| CSUTLDPY   |   375 | Date validation procedures (reusable paras)       | Utility (Procedure Div)   |
| CSUTLDWY   |    89 | Date validation working storage                   | Utility (Working Storage) |
| CSUSR01Y   |    26 | User security record (RECLN 80)                   | User Security             |
| UNUSED1Y   |    10 | Unused/placeholder copybook                       | Unused                    |

---

## 4. Copybooks — Optional Modules

| Copybook    | Lines | Description                              | Module        |
|:------------|------:|:-----------------------------------------|:--------------|
| CIPAUDTY    |    54 | IMS segment — pending auth detail        | Auth/IMS/MQ   |
| CIPAUSMY    |    31 | IMS segment — pending auth summary       | Auth/IMS/MQ   |
| CCPAURQY    |    36 | Pending auth MQ request message          | Auth/IMS/MQ   |
| CCPAURLY    |    24 | Pending auth MQ response message         | Auth/IMS/MQ   |
| CCPAUERY    |    40 | Pending auth error log record            | Auth/IMS/MQ   |
| IMSFUNCS    |    26 | IMS DL/I function code constants         | Auth/IMS/MQ   |
| PADFLPCB    |    26 | IMS PCB — default database               | Auth/IMS/MQ   |
| PASFLPCB    |    26 | IMS PCB — summary database               | Auth/IMS/MQ   |
| PAUTBPCB    |    26 | IMS PCB — auth detail database           | Auth/IMS/MQ   |
| CSDB2RPY    |    89 | DB2 common procedures (priming query)    | Tran Type/DB2 |
| CSDB2RWY    |    46 | DB2 working-storage variables            | Tran Type/DB2 |

---

## 5. BMS Maps (Screen Definitions)

### Core Maps

| Map Name  | Lines | Associated Program | Screen Function             |
|:----------|------:|:-------------------|:----------------------------|
| COSGN00   |   210 | COSGN00C           | Sign-on Screen              |
| COMEN01   |   167 | COMEN01C           | Main Menu                   |
| COADM01   |   167 | COADM01C           | Admin Menu                  |
| COACTVW   |   378 | COACTVWC           | Account View                |
| COACTUP   |   512 | COACTUPC           | Account Update              |
| COCRDLI   |   344 | COCRDLIC           | Credit Card List            |
| COCRDSL   |   157 | COCRDSLC           | Credit Card Detail          |
| COCRDUP   |   172 | COCRDUPC           | Credit Card Update          |
| COTRN00   |   464 | COTRN00C           | Transaction List            |
| COTRN01   |   273 | COTRN01C           | Transaction View            |
| COTRN02   |   307 | COTRN02C           | Transaction Add             |
| CORPT00   |   231 | CORPT00C           | Transaction Report Request  |
| COBIL00   |   141 | COBIL00C           | Bill Payment                |
| COUSR00   |   463 | COUSR00C           | User List                   |
| COUSR01   |   164 | COUSR01C           | User Add                    |
| COUSR02   |   169 | COUSR02C           | User Update                 |
| COUSR03   |   153 | COUSR03C           | User Delete                 |

### Optional Module Maps

| Map Name  | Lines | Associated Program | Screen Function             | Module        |
|:----------|------:|:-------------------|:----------------------------|:--------------|
| COPAU00   |   515 | COPAUS0C           | Pending Auth Summary        | Auth/IMS/MQ   |
| COPAU01   |   294 | COPAUS1C           | Pending Auth Detail         | Auth/IMS/MQ   |
| COTRTLI   |   338 | COTRTLIC           | Transaction Type List       | Tran Type/DB2 |
| COTRTUP   |   137 | COTRTUPC           | Transaction Type Update     | Tran Type/DB2 |

---

## 6. BMS Copybooks (Generated Screen Layouts)

Auto-generated from BMS maps; included by COBOL programs for symbolic map field access.

| Copybook  | Lines | Corresponding Map |
|:----------|------:|:------------------|
| COSGN00   |   152 | COSGN00.bms       |
| COMEN01   |   260 | COMEN01.bms       |
| COADM01   |   260 | COADM01.bms       |
| COACTVW   |   464 | COACTVW.bms       |
| COACTUP   |   668 | COACTUP.bms       |
| COCRDLI   |   560 | COCRDLI.bms       |
| COCRDSL   |   200 | COCRDSL.bms       |
| COCRDUP   |   224 | COCRDUP.bms       |
| COTRN00   |   728 | COTRN00.bms       |
| COTRN01   |   272 | COTRN01.bms       |
| COTRN02   |   272 | COTRN02.bms       |
| CORPT00   |   224 | CORPT00.bms       |
| COBIL00   |   140 | COBIL00.bms       |
| COUSR00   |   728 | COUSR00.bms       |
| COUSR01   |   164 | COUSR01.bms       |
| COUSR02   |   164 | COUSR02.bms       |
| COUSR03   |   152 | COUSR03.bms       |
| COPAU00   |   764 | COPAU00.bms       |
| COPAU01   |   344 | COPAU01.bms       |
| COTRTLI   |   500 | COTRTLI.bms       |
| COTRTUP   |   200 | COTRTUP.bms       |

---

## 7. JCL Jobs

### Core Data Management Jobs

| Job Name   | Lines | Program / Utility | Function                                       |
|:-----------|------:|:------------------|:-----------------------------------------------|
| DUSRSECJ   |    92 | IEBGENER          | Initial load of user security file             |
| ACCTFILE   |    65 | IDCAMS            | Refresh account master (define + REPRO)        |
| CARDFILE   |   128 | IDCAMS            | Refresh card master                            |
| CUSTFILE   |    84 | IDCAMS            | Refresh customer master                        |
| XREFFILE   |   106 | IDCAMS            | Load card–account–customer cross-reference     |
| TRANFILE   |   125 | IDCAMS            | Load transaction master                        |
| DISCGRP    |    65 | IDCAMS            | Load disclosure group file                     |
| TCATBALF   |    65 | IDCAMS            | Refresh transaction category balance           |
| TRANCATG   |    65 | IDCAMS            | Load transaction category types                |
| TRANTYPE   |    65 | IDCAMS            | Load transaction type file                     |
| CLOSEFIL   |    34 | IEFBR14           | Close VSAM files in CICS                       |
| OPENFIL    |    34 | IEFBR14           | Open VSAM files in CICS                        |
| DEFGDGB    |    63 | IDCAMS            | Define GDG base entries                        |
| DEFGDGD    |    94 | IDCAMS            | Define additional GDG bases (DB2 module)       |
| DEFCUST    |    47 | IDCAMS            | Define customer VSAM cluster                   |
| ESDSRRDS   |   124 | IDCAMS            | Create ESDS and RRDS VSAM files                |
| TRANIDX    |    58 | IDCAMS            | Define alternate index on transaction file     |

### Core Batch Processing Jobs

| Job Name   | Lines | Program / Utility | Function                                       |
|:-----------|------:|:------------------|:-----------------------------------------------|
| POSTTRAN   |    45 | CBTRN02C          | Core transaction posting                       |
| INTCALC    |    44 | CBACT04C          | Interest calculation                           |
| TRANBKP    |    71 | IDCAMS            | Backup transaction database (REPRO to GDG)     |
| COMBTRAN   |    52 | SORT              | Combine system + daily transactions            |
| CREASTMT   |    97 | CBSTM03A          | Produce account statements (text + HTML)       |
| TRANREPT   |    84 | CBTRN03C          | Transaction detail report (via PROC)           |
| DALYREJS   |    32 | —                 | Daily rejects processing placeholder           |
| WAITSTEP   |    27 | COBSWAIT          | Wait utility step                              |
| PRTCATBL   |    66 | —                 | Print transaction category balance             |
| REPTFILE   |    32 | —                 | Report file creation                           |
| CBEXPORT   |    72 | CBEXPORT          | Export data for branch migration               |
| CBIMPORT   |    68 | CBIMPORT          | Import data from branch migration              |
| CBADMCDJ   |   167 | —                 | Administrative card management job             |

### Core Utility / Read Jobs

| Job Name   | Lines | Program / Utility | Function                                       |
|:-----------|------:|:------------------|:-----------------------------------------------|
| READACCT   |    50 | CBACT01C          | Read and display account records               |
| READCARD   |    31 | CBACT02C          | Read and display card records                  |
| READCUST   |    30 | CBCUS01C          | Read and display customer records              |
| READXREF   |    31 | CBACT03C          | Read and display cross-reference records       |
| FTPJCL     |    42 | FTP               | FTP file transfer                              |
| TXT2PDF1   |    41 | TXT2PDF           | Text-to-PDF conversion                         |
| INTRDRJ1   |    19 | —                 | Internal reader job 1                          |
| INTRDRJ2   |    14 | —                 | Internal reader job 2                          |

### Optional Module Jobs

| Job Name   | Lines | Program / Utility | Function                                | Module        |
|:-----------|------:|:------------------|:----------------------------------------|:--------------|
| CBPAUP0J   |    46 | CBPAUP0C          | Purge expired authorizations            | Auth/IMS/MQ   |
| DBPAUTP0   |    47 | —                 | DB2 auth purge setup                    | Auth/IMS/MQ   |
| LOADPADB   |    51 | PAUDBLOD          | Load pending auth IMS database          | Auth/IMS/MQ   |
| UNLDGSAM   |    53 | DBUNLDGS          | Unload IMS GS-AM data                   | Auth/IMS/MQ   |
| UNLDPADB   |    69 | PAUDBUNL          | Unload pending auth IMS database        | Auth/IMS/MQ   |
| CREADB21   |    84 | DSNTEP4           | Create CardDemo DB2 database + tables   | Tran Type/DB2 |
| TRANEXTR   |   122 | DSNTIAUL          | Extract DB2 data for tran types         | Tran Type/DB2 |
| MNTTRDB2   |    29 | COBTUPDT          | Maintain transaction type table (DB2)   | Tran Type/DB2 |

---

## 8. Assembler Modules

| Module     | Location   | Function                                    |
|:-----------|:-----------|:--------------------------------------------|
| MVSWAIT    | app/asm/   | Timer control — waits for specified interval |
| COBDATFT   | app/asm/   | Date format conversion utility (ASM)        |

---

## 9. Procedures (PROC)

| Procedure   | Location   | Function                                    |
|:------------|:-----------|:--------------------------------------------|
| REPROC.prc  | app/proc/  | Reprocessing procedure                      |
| TRANREPT.prc| app/proc/  | Transaction report procedure (wraps CBTRN03C)|

---

## 10. Scheduler Definitions

| File                | Format    | Scheduler  | Description                              |
|:--------------------|:----------|:-----------|:-----------------------------------------|
| CardDemo.controlm   | XML       | Control-M  | Batch workflow orchestration (4 folders)  |
| CardDemo.ca7        | Text      | CA7        | Alternative scheduler definitions         |

### Control-M Job Streams (from `CardDemo.controlm`)

| Folder Name                          | Jobs in Sequence                              |
|:-------------------------------------|:----------------------------------------------|
| DAILY-TransactionBackup              | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL      |
| WEEKLY-TransactionTypesDBRefresh     | CLOSEFIL → TRANEXTR → TRANCATG → TRANTYPE → OPENFIL |
| MONTHLY-EndOfMonth                   | CLOSEFIL → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL |
| YEARLY-EndOfYear                     | CBADMCDJ (standalone)                         |

---

## 11. Classification Summary

### By Processing Mode

| Mode    | Count | Programs                                                                                     |
|:--------|------:|:---------------------------------------------------------------------------------------------|
| Online  |    25 | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C–COUSR03C, COPAUS0C, COPAUS1C, COPAUS2C, COPAUA0C, COTRTLIC, COTRTUPC, CODATE01, COACCT01 |
| Batch   |    19 | CBACT01C–CBACT04C, CBCUS01C, CBTRN01C–CBTRN03C, CBSTM03A, CBSTM03B, CBEXPORT, CBIMPORT, COBSWAIT, CSUTLDTC, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL, COBTUPDT |

### By Business Domain

| Domain                | Programs                                                              |
|:----------------------|:----------------------------------------------------------------------|
| Authentication        | COSGN00C                                                              |
| Navigation / Menus    | COMEN01C, COADM01C                                                    |
| Account Management    | COACTVWC, COACTUPC, CBACT01C, CBACT04C                               |
| Card Management       | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C                               |
| Transaction Mgmt      | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C                     |
| Reporting / Statements| CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B                               |
| Billing               | COBIL00C                                                              |
| User Security (Admin) | COUSR00C, COUSR01C, COUSR02C, COUSR03C                               |
| Cross-Reference       | CBACT03C                                                              |
| Customer Data         | CBCUS01C                                                              |
| Branch Migration      | CBEXPORT, CBIMPORT                                                    |
| Authorization (IMS/MQ)| COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| Tran Type Mgmt (DB2)  | COTRTLIC, COTRTUPC, COBTUPDT                                         |
| MQ Integration        | CODATE01, COACCT01                                                    |
| Utilities             | COBSWAIT, CSUTLDTC                                                    |

### By Technology Stack

| Technology  | Programs                                                                    |
|:------------|:----------------------------------------------------------------------------|
| VSAM Only   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, COBIL00C, COUSR00C–03C, all core batch |
| DB2         | COTRTLIC, COTRTUPC, COBTUPDT, COPAUS1C                                     |
| IMS DL/I    | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| MQ          | COPAUA0C, CODATE01, COACCT01                                               |
| ASM         | COBSWAIT (calls MVSWAIT), CBACT01C (calls COBDATFT)                        |

---

*Generated from static analysis of the CardDemo COBOL codebase.*
