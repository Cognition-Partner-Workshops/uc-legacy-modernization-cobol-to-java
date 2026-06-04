# Application Inventory - CardDemo

> Comprehensive catalog of the CardDemo mainframe credit card management system.

## Summary

| Category | Count |
|----------|------:|
| COBOL Programs (Online) | 25 |
| COBOL Programs (Batch) | 19 |
| Assembler Programs | 2 |
| Copybooks (Data) | 41 |
| Copybooks (BMS-generated) | 21 |
| BMS Maps | 21 |
| JCL Jobs | 46 |
| Procedures (PROC) | 2 |
| Scheduler Definitions | 2 |
| **Total Artifacts** | **179** |

---

## 1. Online COBOL Programs (CICS)

| Program | Lines | Transaction | Function | Module | Classification |
|---------|------:|-------------|----------|--------|---------------|
| COSGN00C | 260 | CC00 | Sign-on / Authentication | Core | Security |
| COMEN01C | 308 | CM00 | Main Menu Router | Core | Navigation |
| COADM01C | 288 | CA00 | Admin Menu | Core | Navigation |
| COACTVWC | 941 | CAVW | Account View | Core | Account Mgmt |
| COACTUPC | 4,236 | CAUP | Account Update | Core | Account Mgmt |
| COCRDLIC | 1,459 | CCLI | Credit Card List | Core | Card Mgmt |
| COCRDSLC | 887 | CCDL | Credit Card View (Detail) | Core | Card Mgmt |
| COCRDUPC | 1,560 | CCUP | Credit Card Update | Core | Card Mgmt |
| COTRN00C | 699 | CT00 | Transaction List | Core | Transaction Mgmt |
| COTRN01C | 330 | CT01 | Transaction View | Core | Transaction Mgmt |
| COTRN02C | 783 | CT02 | Transaction Add | Core | Transaction Mgmt |
| CORPT00C | 649 | CR00 | Transaction Reports | Core | Reporting |
| COBIL00C | 572 | CB00 | Bill Payment | Core | Payments |
| COUSR00C | 695 | CU00 | User List (Security) | Core | User Admin |
| COUSR01C | 299 | CU01 | User Add | Core | User Admin |
| COUSR02C | 414 | CU02 | User Update | Core | User Admin |
| COUSR03C | 359 | CU03 | User Delete | Core | User Admin |
| COPAUS0C | 1,032 | CPVS | Pending Auth Summary | IMS-DB2-MQ | Authorization |
| COPAUS1C | 604 | CPVD | Pending Auth Details | IMS-DB2-MQ | Authorization |
| COPAUS2C | 337 | — | Pending Auth Helper | IMS-DB2-MQ | Authorization |
| COPAUA0C | 1,026 | CP00 | Process Auth Requests (MQ) | IMS-DB2-MQ | Authorization |
| COTRTLIC | 2,098 | CTLI | Tran Type List/Delete (DB2) | DB2-TranType | Reference Data |
| COTRTUPC | 1,702 | CTTU | Tran Type Add/Edit (DB2) | DB2-TranType | Reference Data |
| CODATE01 | 208 | CDRD | System Date Inquiry (MQ) | VSAM-MQ | Integration |
| COACCT01 | 620 | CDRA | Account Inquiry (MQ) | VSAM-MQ | Integration |

## 2. Batch COBOL Programs

| Program | Lines | Invoked By | Function | Module | Classification |
|---------|------:|------------|----------|--------|---------------|
| CBACT01C | 430 | READACCT | Read/Export Account Data | Core | Data Extract |
| CBACT02C | 178 | READCARD | Read Card Data | Core | Data Extract |
| CBACT03C | 178 | READXREF | Read Cross-Reference | Core | Data Extract |
| CBACT04C | 652 | INTCALC | Interest Calculation | Core | Financial Calc |
| CBCUS01C | 178 | READCUST | Read Customer Data | Core | Data Extract |
| CBTRN01C | 494 | — | Daily Transaction Input | Core | Transaction Processing |
| CBTRN02C | 731 | POSTTRAN | Post Transactions (Balances) | Core | Transaction Processing |
| CBTRN03C | 649 | TRANREPT | Transaction Reporting | Core | Reporting |
| CBSTM03A | 924 | CREASTMT | Statement Generation (Main) | Core | Statement/Billing |
| CBSTM03B | 230 | via CBSTM03A | Statement Subroutine | Core | Statement/Billing |
| COBSWAIT | 41 | WAITSTEP | Timer Wait Control | Core | Utility |
| CSUTLDTC | 157 | various | Date Conversion Utility | Core | Utility |
| CBEXPORT | 582 | CBEXPORT.jcl | Branch Data Export | Core | Data Migration |
| CBIMPORT | 487 | CBIMPORT.jcl | Branch Data Import | Core | Data Migration |
| CBPAUP0C | 390 | CBPAUP0J | Purge Expired Authorizations | IMS-DB2-MQ | Authorization |
| COBTUPDT | 445 | MNTTRDB2 | Maintain Tran Type Table (DB2) | DB2-TranType | Reference Data |
| DBUNLDGS | 183 | UNLDGSAM | DB2 Unload to GSAM | IMS-DB2-MQ | Data Extract |
| PAUDBLOD | 220 | LOADPADB | Load Pending Auth DB | IMS-DB2-MQ | Data Load |
| PAUDBUNL | 195 | UNLDPADB | Unload Pending Auth DB | IMS-DB2-MQ | Data Extract |

## 3. Assembler Programs

| Program | Lines | Function | Classification |
|---------|------:|----------|---------------|
| MVSWAIT | ~50 | Timer wait (called by COBSWAIT) | System Utility |
| COBDATFT | ~80 | Date format conversion | System Utility |

## 4. Copybooks - Data Structures

| Copybook | Lines | Entity / Purpose | Record Len | Module |
|----------|------:|------------------|-----------|--------|
| CVACT01Y | 20 | Account Record | 300 | Core |
| CVACT02Y | 14 | Card Record | 150 | Core |
| CVACT03Y | 11 | Card Cross-Reference (XREF) | 50 | Core |
| CVCUS01Y | 26 | Customer Record | 500 | Core |
| CUSTREC | 26 | Customer Record (alternate) | 500 | Core |
| CVCRD01Y | 46 | Credit Card Work Areas / AID Keys | — | Core |
| CVTRA01Y | 13 | Transaction Category Balance | 50 | Core |
| CVTRA02Y | 13 | Disclosure Group | 50 | Core |
| CVTRA03Y | 10 | Transaction Type | 60 | Core |
| CVTRA04Y | 12 | Transaction Category Type | 60 | Core |
| CVTRA05Y | 21 | Transaction Record | 350 | Core |
| CVTRA06Y | 21 | Daily Transaction Record | 350 | Core |
| CVTRA07Y | — | Transaction Report Work Areas | — | Core |
| CVEXPORT | 103 | Export Multi-Record Layout (REDEFINES) | 500 | Core |
| COSTM01 | 38 | Transaction Altered Layout (Reporting) | 350 | Core |
| COCOM01Y | 47 | Application Communication Area (COMMAREA) | — | Core |
| COADM02Y | 62 | Admin Menu Options Table | — | Core |
| COMEN02Y | 101 | Main Menu Options Table | — | Core |
| CSUSR01Y | 26 | User Security Record | 80 | Core |
| CSDAT01Y | 58 | Date/Time Work Areas | — | Core |
| COTTL01Y | 27 | Title Line Work Area | — | Core |
| CSMSG01Y | 24 | Message Area (Info) | — | Core |
| CSMSG02Y | 35 | Abend Work Areas | — | Core |
| CSSETATY | 30 | Set Attribute (Screen field attributes) | — | Core |
| CSSTRPFY | 85 | String Processing Functions | — | Core |
| CSUTLDWY | 89 | Date Validation (Working Storage) | — | Core |
| CSUTLDPY | 375 | Date Validation (Procedure Division) | — | Core |
| CSLKPCDY | 1,318 | Lookup Code Repository (states, area codes) | — | Core |
| CODATECN | 52 | Date Conversion Work Areas | — | Core |
| CCPAUERY | — | Pending Auth Error Layout | — | IMS-DB2-MQ |
| CCPAURLY | — | Pending Auth Response Layout | — | IMS-DB2-MQ |
| CCPAURQY | — | Pending Auth Request Layout | — | IMS-DB2-MQ |
| CIPAUDTY | — | Pending Auth Detail IMS Segment | — | IMS-DB2-MQ |
| CIPAUSMY | — | Pending Auth Summary IMS Segment | — | IMS-DB2-MQ |
| IMSFUNCS | — | IMS Function Codes | — | IMS-DB2-MQ |
| PADFLPCB | — | IMS PCB (Full Function) | — | IMS-DB2-MQ |
| PASFLPCB | — | IMS PCB (Secondary) | — | IMS-DB2-MQ |
| PAUTBPCB | — | IMS PCB (Auth Table) | — | IMS-DB2-MQ |
| CSDB2RPY | — | DB2 Response Layout | — | DB2-TranType |
| CSDB2RWY | — | DB2 Read/Write Work Area | — | DB2-TranType |
| UNUSED1Y | — | Placeholder/Reserved | — | Core |

## 5. BMS Maps (Screen Definitions)

### Core Maps (`app/bms/`)

| Map | Mapset | Associated Program | Screen |
|-----|--------|-------------------|--------|
| COSGN00 | COSGN00 | COSGN00C | Sign-on |
| COMEN01 | COMEN01 | COMEN01C | Main Menu |
| COADM01 | COADM01 | COADM01C | Admin Menu |
| COACTVW | COACTVW | COACTVWC | Account View |
| COACTUP | COACTUP | COACTUPC | Account Update |
| COCRDLI | COCRDLI | COCRDLIC | Card List |
| COCRDSL | COCRDSL | COCRDSLC | Card Detail |
| COCRDUP | COCRDUP | COCRDUPC | Card Update |
| COTRN00 | COTRN00 | COTRN00C | Transaction List |
| COTRN01 | COTRN01 | COTRN01C | Transaction View |
| COTRN02 | COTRN02 | COTRN02C | Transaction Add |
| CORPT00 | CORPT00 | CORPT00C | Reports |
| COBIL00 | COBIL00 | COBIL00C | Bill Payment |
| COUSR00 | COUSR00 | COUSR00C | User List |
| COUSR01 | COUSR01 | COUSR01C | User Add |
| COUSR02 | COUSR02 | COUSR02C | User Update |
| COUSR03 | COUSR03 | COUSR03C | User Delete |

### Optional Module Maps

| Map | Module | Associated Program | Screen |
|-----|--------|-------------------|--------|
| COPAU00 | IMS-DB2-MQ | COPAUS0C | Pending Auth Summary |
| COPAU01 | IMS-DB2-MQ | COPAUS1C | Pending Auth Detail |
| COTRTLI | DB2-TranType | COTRTLIC | Tran Type List |
| COTRTUP | DB2-TranType | COTRTUPC | Tran Type Update |

## 6. JCL Jobs

### Core Batch Jobs (`app/jcl/`)

| Job | Primary Program | Function | Category |
|-----|----------------|----------|----------|
| DUSRSECJ | IEBGENER/IDCAMS | Load User Security VSAM | Data Init |
| ACCTFILE | IDCAMS | Refresh Account Master VSAM | Data Init |
| CARDFILE | IDCAMS | Refresh Card Master VSAM | Data Init |
| CUSTFILE | IDCAMS | Refresh Customer Master VSAM | Data Init |
| XREFFILE | IDCAMS | Load XREF VSAM (+ AIX) | Data Init |
| TRANFILE | IDCAMS | Load Transaction Master VSAM | Data Init |
| DISCGRP | IDCAMS | Load Disclosure Group VSAM | Data Init |
| TCATBALF | IDCAMS | Load Tran Category Balance VSAM | Data Init |
| TRANCATG | IDCAMS | Load Tran Category Type VSAM | Data Init |
| TRANTYPE | IDCAMS | Load Transaction Type VSAM | Data Init |
| DEFGDGB | IDCAMS | Define GDG Bases | Infrastructure |
| DEFGDGD | IDCAMS/IEBGENER | Define GDG Bases (DB2) | Infrastructure |
| DEFCUST | IDCAMS | Define Customer VSAM clusters | Infrastructure |
| ESDSRRDS | IDCAMS | Create ESDS/RRDS VSAM files | Infrastructure |
| TRANIDX | IDCAMS | Define AIX on Transaction file | Infrastructure |
| CLOSEFIL | SDSF | Close CICS-opened files | Operations |
| OPENFIL | SDSF | Open files for CICS | Operations |
| POSTTRAN | CBTRN02C | Core transaction posting | Processing |
| INTCALC | CBACT04C | Interest calculation | Processing |
| COMBTRAN | SORT | Combine daily + system transactions | Processing |
| CREASTMT | CBSTM03A | Generate card statements | Processing |
| TRANBKP | IDCAMS | Transaction backup (with GDG) | Backup |
| TRANREPT | CBTRN03C | Transaction report generation | Reporting |
| PRTCATBL | SORT/REPROC | Print category balance report | Reporting |
| REPTFILE | IDCAMS | Define report output file | Reporting |
| DALYREJS | IDCAMS | Daily rejection file management | Processing |
| READACCT | CBACT01C | Extract account data | Data Extract |
| READCARD | CBACT02C | Extract card data | Data Extract |
| READCUST | CBCUS01C | Extract customer data | Data Extract |
| READXREF | CBACT03C | Extract cross-reference data | Data Extract |
| CBEXPORT | CBEXPORT | Branch data export | Data Migration |
| CBIMPORT | CBIMPORT | Branch data import | Data Migration |
| WAITSTEP | COBSWAIT | Timed wait step | Utility |
| CBADMCDJ | DFHCSDUP | CSD batch update | Infrastructure |

### Utility JCL Jobs

| Job | Program | Function | Category |
|-----|---------|----------|----------|
| FTPJCL | FTP | File transfer | Utility |
| TXT2PDF1 | IKJEFT1B | Text to PDF conversion | Utility |
| INTRDRJ1 | IEBGENER | Internal reader chain (step 1) | Utility |
| INTRDRJ2 | IDCAMS | Internal reader chain (step 2) | Utility |

### Optional Module JCL

| Job | Module | Program | Function |
|-----|--------|---------|----------|
| CBPAUP0J | IMS-DB2-MQ | CBPAUP0C | Purge expired authorizations |
| DBPAUTP0 | IMS-DB2-MQ | — | Auth DB2 setup |
| LOADPADB | IMS-DB2-MQ | PAUDBLOD | Load pending auth database |
| UNLDGSAM | IMS-DB2-MQ | DBUNLDGS | Unload DB2 to GSAM |
| UNLDPADB | IMS-DB2-MQ | PAUDBUNL | Unload pending auth database |
| CREADB21 | DB2-TranType | DSNTEP4 | Create DB2 database/tables |
| MNTTRDB2 | DB2-TranType | COBTUPDT | Maintain tran type table |
| TRANEXTR | DB2-TranType | DSNTIAUL | Extract DB2 tran types to VSAM |

## 7. Procedures & Scheduler

### Procedures (`app/proc/`)

| Procedure | Purpose |
|-----------|---------|
| REPROC.prc | Reusable report generation procedure |
| TRANREPT.prc | Transaction report procedure |

### Scheduler Definitions (`app/scheduler/`)

| Definition | Platform | Workflows Defined |
|-----------|----------|-------------------|
| CardDemo.controlm | Control-M | DAILY-TransactionBackup, WEEKLY-TransactionTypesDBRefresh, WEEKLY-DisclosureGroupsRefresh, WEEKLY-TransactionTypesDBRefresh (Smart Folder), MONTHLY-InterestCalc, MONTHLY-StatementGeneration |
| CardDemo.ca7 | CA-7 | Equivalent scheduling definitions |

### Control-M Workflow: DAILY-TransactionBackup
```
CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
```

### Control-M Workflow: WEEKLY-DisclosureGroupsRefresh
```
MNTTRDB2 → CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL
```

---

## Classification Legend

| Classification | Description |
|---------------|-------------|
| **Core** | Base application - always deployed |
| **IMS-DB2-MQ** | Optional: Pending Authorization module |
| **DB2-TranType** | Optional: Transaction Type Management |
| **VSAM-MQ** | Optional: Account extraction via MQ |

## Technology Stack Summary

| Technology | Role | Components |
|-----------|------|-----------|
| COBOL | Business Logic | 38 programs |
| CICS | Online Transaction Processing | 25 transactions |
| VSAM KSDS | Primary Data Store | Account, Card, Customer, Transaction, XREF |
| JCL | Batch Orchestration | 46 jobs |
| BMS | Screen Definitions | 21 maps |
| DB2 | Relational Storage (optional) | Transaction types, Auth logging |
| IMS DB | Hierarchical Storage (optional) | Pending authorizations |
| IBM MQ | Messaging (optional) | Auth requests, Account/Date inquiries |
| Control-M | Batch Scheduling | 3 workflow folders |
| RACF (simulated) | Security | USRSEC file-based auth |
| Assembler | System Utilities | Timer, Date formatting |
