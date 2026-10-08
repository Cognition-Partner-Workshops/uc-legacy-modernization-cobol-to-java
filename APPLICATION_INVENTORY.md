# CardDemo Application Inventory

> Static analysis of every COBOL program, copybook, JCL job/procedure, BMS map and supporting artifact in this repository (core CardDemo plus the optional `app-authorization-ims-db2-mq`, `app-transaction-type-db2` and `app-vsam-mq` modules). Generated from source parsing; descriptions are derived from program headers, code reading and `README.md`.

Companion documents: [DATA_DICTIONARY.md](DATA_DICTIONARY.md) · [DEPENDENCY_MAP.md](DEPENDENCY_MAP.md) · [HOTSPOT_REPORT.md](HOTSPOT_REPORT.md)

## 1. Summary

| Artifact | Count | Notes |
|---|---:|---|
| COBOL programs | 44 | Account Extract (VSAM/MQ): 2, Authorization (IMS/DB2/MQ): 8, Core: 31, Transaction Type (DB2): 3; 22,904 executable (non-comment) lines |
| ↳ by role | | Batch: 17, Online screen: 21, Online service (MQ): 2, Online service (MQ-triggered): 1, Subroutine: 3 |
| Copybooks (data/logic) | 44 | incl. Db2 DCLGENs, IMS segment/PCB layouts and MQ messages |
| BMS symbolic-map copybooks | 21 | generated from the BMS mapsets |
| BMS mapsets | 21 | 21 maps |
| JCL jobs | 45 | plus 6 procedure(s) and 9 sample build jobs/procs |
| CICS transactions (CSD) | 25 | 8 CICS files, 26 program and 21 mapset definitions |
| Assembler modules | 2 | `COBDATFT` (date format), `MVSWAIT` (timer wait) + 2 macros |
| Db2 DDL | 6 | 3 tables (`TRANSACTION_TYPE`, `TRANSACTION_TYPE_CATEGORY`, `AUTHFRDS`) + 3 indexes |
| IMS DBD / PSB | 4 / 4 | HIDAM `DBPAUTP0` + index `DBPAUTX0`, 2 GSAM DBDs; PSBs `PSBPAUTB`, `PSBPAUTL`, `PAUTBUNL`, `DLIGSAMP` |
| Scheduler definitions | 2 | Control-M (`CardDemo.controlm`) and CA-7 (`CardDemo.ca7`) |

### Classification scheme

- **Role**: Online screen (CICS pseudo-conversational BMS program), Online service (no screen; MQ-driven), Batch (JCL-executed), Subroutine (CALLed/LINKed only).
- **Domain**: business capability the module serves (Security, Account, Card, Transaction, Billing, Interest, Statement, Reporting, Authorization, Reference data, Data migration, Navigation, Utility).
- **Tech**: runtime dependencies detected in source (CICS, VSAM, QSAM, Db2, IMS, MQ, ASM).
- **Size class**: S < 300, M 300–799, L 800–1,499, XL ≥ 1,500 executable lines.
- **Complexity**: approximate cyclomatic complexity = 1 + IF + WHEN + AND/OR + PERFORM UNTIL/VARYING + GO TO decision points (static estimate).

## 2. COBOL Programs

| # | Program | Module | Role | Domain | Tech | Trans | Mapset | Lines (exec) | Size | Cyclo. | Invoked by | Description |
|---:|---|---|---|---|---|---|---|---:|---|---:|---|---|
| 1 | `CBACT01C` | Core | Batch | Account | VSAM+QSAM+ASM |  |  | 358 | M | 29 | JCL READACCT | Reads account master and writes fixed, array (OCCURS) and variable-length extracts; calls COBDATFT (assembler) |
| 2 | `CBACT02C` | Core | Batch | Card | VSAM |  |  | 129 | S | 14 | JCL READCARD | Reads and prints card master (diagnostic utility) |
| 3 | `CBACT03C` | Core | Batch | Card | VSAM |  |  | 130 | S | 14 | JCL READXREF | Reads and prints card cross-reference (diagnostic utility) |
| 4 | `CBACT04C` | Core | Batch | Interest | VSAM+QSAM |  |  | 552 | M | 47 | JCL INTCALC | Interest calculator: for each category balance applies disclosure-group rate ((bal*rate)/1200), writes interest transactions and updates account balances |
| 5 | `CBCUS01C` | Core | Batch | Customer | VSAM |  |  | 130 | S | 14 | JCL READCUST | Reads and prints customer master (diagnostic utility) |
| 6 | `CBEXPORT` | Core | Batch | Data migration | VSAM |  |  | 396 | M | 27 | JCL CBEXPORT | Exports customer/account/xref/transaction/card data into a multi-record EXPORT file (CVEXPORT) |
| 7 | `CBIMPORT` | Core | Batch | Data migration | VSAM+QSAM |  |  | 337 | M | 22 | JCL CBIMPORT | Imports EXPORT file and splits it into per-entity output files with an error file |
| 8 | `CBSTM03A` | Core | Batch | Statement | QSAM |  |  | 784 | M | 42 | JCL CREASTMT | Statement generator (text + HTML) per card/account; uses ALTER/GO TO, 2-D tables and CALLs CBSTM03B |
| 9 | `CBTRN01C` | Core | Batch | Transaction | VSAM+QSAM |  |  | 415 | M | 36 | **none found** | Daily transaction validation prototype (reads DALYTRAN, looks up XREF/ACCT); not referenced by any JCL |
| 10 | `CBTRN02C` | Core | Batch | Transaction | VSAM+QSAM |  |  | 619 | M | 52 | JCL POSTTRAN | Daily transaction posting: validates card/account/limit/expiry, writes TRANSACT, updates TCATBAL and account cycle balances, rejects to DALYREJS |
| 11 | `CBTRN03C` | Core | Batch | Reporting | VSAM+QSAM |  |  | 545 | M | 46 | JCL TRANREPT | Transaction detail report for a date range with page/account/grand totals, enriched by type and category |
| 12 | `COBSWAIT` | Core | Batch | Utility | ASM |  |  | 13 | S | 1 | JCL WAITSTEP | Batch wait utility (reads centiseconds from SYSIN, CALLs assembler MVSWAIT) |
| 13 | `COACTUPC` | Core | Online screen | Account | CICS+VSAM | CAUP | COACTUP | 3,368 | XL | 449 | XCTL COMEN01C | Account update: edits/validates ~40 account and customer fields and rewrites ACCTDAT and CUSTDAT |
| 14 | `COACTVWC` | Core | Online screen | Account | CICS+VSAM | CAVW | COACTVW | 703 | M | 51 | XCTL COMEN01C | Account view: reads account, card cross-reference and customer to show a combined account profile |
| 15 | `COADM01C` | Core | Online screen | Navigation | CICS | CA00 | COADM01 | 189 | S | 25 | XCTL COSGN00C, COTRTLIC, COTRTUPC, COUSR00C, COUSR01C, COUSR02C,  | Admin menu; dispatches to user-admin and transaction-type options in COADM02Y |
| 16 | `COBIL00C` | Core | Online screen | Billing | CICS+VSAM | CB00 | COBIL00 | 420 | M | 40 | XCTL COMEN01C | Bill payment: pays full account balance, writes a payment transaction and rewrites ACCTDAT |
| 17 | `COCRDLIC` | Core | Online screen | Card | CICS+VSAM | CCLI | COCRDLI | 1,093 | L | 137 | XCTL COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COMEN01C | Credit card list: paged browse of cards (all cards for admin, account-filtered for users) |
| 18 | `COCRDSLC` | Core | Online screen | Card | CICS+VSAM | CCDL | COCRDSL | 642 | M | 67 | XCTL COACTUPC, COACTVWC, COCRDLIC, COCRDUPC, COMEN01C | Credit card detail view by account/card number |
| 19 | `COCRDUPC` | Core | Online screen | Card | CICS+VSAM | CCUP | COCRDUP | 1,194 | L | 187 | XCTL COACTUPC, COACTVWC, COCRDLIC, COMEN01C | Credit card update: edits embossed name, status, expiry and rewrites CARDDAT |
| 20 | `COMEN01C` | Core | Online screen | Navigation | CICS | CM00 | COMEN01 | 213 | S | 31 | XCTL COACTUPC, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC,  | Main menu for regular users; dispatches to options defined in COMEN02Y |
| 21 | `CORPT00C` | Core | Online screen | Reporting | CICS | CR00 | CORPT00 | 498 | M | 57 | XCTL COMEN01C | Report request: builds TRANREPT JCL for a date range and submits it via TDQ JOBS (internal reader) |
| 22 | `COSGN00C` | Core | Online screen | Security | CICS+VSAM | CC00 | COSGN00 | 172 | S | 13 | XCTL COADM01C, COBIL00C, COMEN01C, COPAUS0C, CORPT00C, COTRN00C,  | Sign-on: validates user ID/password against USRSEC and routes admin vs regular users |
| 23 | `COTRN00C` | Core | Online screen | Transaction | CICS+VSAM | CT00 | COTRN00 | 529 | M | 106 | XCTL COMEN01C, COTRN01C | Transaction list: paged browse of TRANSACT |
| 24 | `COTRN01C` | Core | Online screen | Transaction | CICS+VSAM | CT01 | COTRN01 | 231 | S | 19 | XCTL COMEN01C, COTRN00C | Transaction detail view |
| 25 | `COTRN02C` | Core | Online screen | Transaction | CICS+VSAM | CT02 | COTRN02 | 614 | M | 83 | XCTL COMEN01C | Add transaction: validates account/card, amount, dates (via CSUTLDTC) and writes TRANSACT |
| 26 | `COUSR00C` | Core | Online screen | Security | CICS+VSAM | CU00 | COUSR00 | 531 | M | 108 | XCTL COADM01C | User list (admin): paged browse of USRSEC |
| 27 | `COUSR01C` | Core | Online screen | Security | CICS+VSAM | CU01 | COUSR01 | 198 | S | 22 | XCTL COADM01C | Add user (admin) |
| 28 | `COUSR02C` | Core | Online screen | Security | CICS+VSAM | CU02 | COUSR02 | 303 | M | 38 | XCTL COADM01C, COUSR00C | Update user (admin) |
| 29 | `COUSR03C` | Core | Online screen | Security | CICS+VSAM | CU03 | COUSR03 | 251 | S | 25 | XCTL COADM01C, COUSR00C | Delete user (admin) |
| 30 | `CBSTM03B` | Core | Subroutine | Statement | VSAM |  |  | 162 | S | 17 | CALL/LINK CBSTM03A | File-access subroutine for CBSTM03A (open/read/close of TRNX, XREF, CUST, ACCT files) |
| 31 | `CSUTLDTC` | Core | Subroutine | Utility |  |  |  | 114 | S | 10 | CALL/LINK CORPT00C, COTRN02C | Date validation subroutine wrapping LE CEEDAYS; called by COTRN02C and CORPT00C |
| 32 | `CBPAUP0C` | Authorization (IMS/DB2/MQ) | Batch | Authorization | IMS |  |  | 266 | S | 31 | JCL CBPAUP0J | IMS BMP purge of expired pending authorizations (expiry days from PARM, default 5) with checkpoints |
| 33 | `COBTUPDT` | Transaction Type (DB2) | Batch | Reference data | QSAM+Db2 |  |  | 177 | S | 16 | JCL MNTTRDB2 | Batch maintenance of CARDDEMO.TRANSACTION_TYPE from an A/U/D input file |
| 34 | `DBUNLDGS` | Authorization (IMS/DB2/MQ) | Batch | Authorization | IMS |  |  | 198 | S | 14 | JCL UNLDGSAM | IMS DLI unload of PAUTDB root/child segments to GSAM files |
| 35 | `PAUDBLOD` | Authorization (IMS/DB2/MQ) | Batch | Authorization | QSAM+IMS |  |  | 251 | S | 29 | JCL LOADPADB | IMS BMP load of PAUTDB from flat root/child files |
| 36 | `PAUDBUNL` | Authorization (IMS/DB2/MQ) | Batch | Authorization | QSAM+IMS |  |  | 207 | S | 20 | JCL UNLDPADB | IMS DLI unload of PAUTDB to flat root/child files |
| 37 | `COPAUS0C` | Authorization (IMS/DB2/MQ) | Online screen | Authorization | CICS+VSAM+IMS | CPVS | COPAU00 | 792 | M | 80 | XCTL COMEN01C, COPAUS1C | Pending authorization summary screen for an account (IMS PAUTSUM0 + child browse) |
| 38 | `COPAUS1C` | Authorization (IMS/DB2/MQ) | Online screen | Authorization | CICS+IMS | CPVD | COPAU01 | 461 | M | 36 | XCTL COPAUS0C | Pending authorization detail screen; marks/unmarks fraud via LINK to COPAUS2C |
| 39 | `COTRTLIC` | Transaction Type (DB2) | Online screen | Reference data | CICS+Db2 | CTLI | COTRTLI | 1,597 | XL | 245 | XCTL COADM01C, COTRTUPC | Transaction type list with inline update/delete (Db2 cursor paging, 7 rows per page) |
| 40 | `COTRTUPC` | Transaction Type (DB2) | Online screen | Reference data | CICS+Db2 | CTTU | COTRTUP | 1,241 | L | 220 | XCTL COADM01C, COTRTLIC | Transaction type add/update/delete (Db2) |
| 41 | `COACCT01` | Account Extract (VSAM/MQ) | Online service (MQ) | Account | CICS+VSAM+MQ | CDRA |  | 500 | M | 20 | CICS | MQ request/reply service returning account details from ACCTDAT |
| 42 | `CODATE01` | Account Extract (VSAM/MQ) | Online service (MQ) | Utility | CICS+MQ | CDRD |  | 409 | M | 16 | CICS | MQ request/reply service returning system date/time |
| 43 | `COPAUA0C` | Authorization (IMS/DB2/MQ) | Online service (MQ-triggered) | Authorization | CICS+VSAM+IMS+MQ | CP00 |  | 771 | M | 45 | MQ trigger (CP00) | MQ-triggered card authorization engine: reads request, checks XREF/ACCT/CUST and IMS summary, approves/declines (reason 3100/4100/4200/4300...), inserts IMS detail and replies via MQ |
| 44 | `COPAUS2C` | Authorization (IMS/DB2/MQ) | Subroutine | Authorization | CICS+Db2 |  |  | 201 | S | 5 | CALL/LINK COPAUS1C | Fraud marking: inserts/updates CARDDEMO.AUTHFRDS in Db2 |

**Observations**
- Programs with no JCL, CSD, CALL or trigger reference: `CBTRN01C` — candidates for retirement or confirmation with operations.
- `CBACT02C`, `CBACT03C`, `CBCUS01C` and `CBACT01C` are diagnostic/print or format-demo utilities with low business value; `CBTRN02C`, `CBACT04C`, `COBIL00C`, `COPAUA0C` move money or authorize spend and carry the highest business criticality.
- Online programs follow a uniform pseudo-conversational pattern (`COCOM01Y` COMMAREA, `RETURN TRANSID`, XCTL navigation), which makes them good candidates for a common controller/service template in Java.

### 2.1 Assembler and macro modules

| Module | Type | Called by | Purpose |
|---|---|---|---|
| `COBDATFT` | Assembler (`app/asm`) | `CBACT01C` | Date format conversion (interface copybook `CODATECN`) |
| `MVSWAIT` | Assembler (`app/asm`) | `COBSWAIT` | Timer wait (STIMER) |
| `COCDATFT` | Macro (`app/maclib`) | `COBDATFT` | Date conversion macro |
| `ASMWAIT` | Macro (`app/maclib`) | `MVSWAIT` | Wait macro |

## 3. Copybooks

| Copybook | Module | Classification | Record len (bytes) | Used by (#) | Used by | Description |
|---|---|---|---:|---:|---|---|
| `CCPAUERY` | Authorization (IMS/DB2/MQ) | Business record layout | 122 | 1 | COPAUA0C | Authorization error-log record |
| `COSTM01` | Core | Business record layout | 350 | 1 | CBSTM03A | Statement transaction record (TRXFL, keyed by card+tran id) |
| `CSUSR01Y` | Core | Business record layout | 80 | 14 | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC +8 | User security record (USRSEC) |
| `CUSTREC` | Core | Business record layout | 500 | 1 | CBSTM03A | Customer record variant used by statement program |
| `CVACT01Y` | Core | Business record layout | 300 | 14 | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C +8 | Account master record (ACCTDAT) |
| `CVACT02Y` | Core | Business record layout | 150 | 10 | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDLIC +4 | Card master record (CARDDAT) |
| `CVACT03Y` | Core | Business record layout | 50 | 14 | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C +8 | Card ↔ account ↔ customer cross-reference (CCXREF) |
| `CVCUS01Y` | Core | Business record layout | 500 | 10 | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC +4 | Customer master record (CUSTDAT) |
| `CVTRA01Y` | Core | Business record layout | 50 | 2 | CBACT04C, CBTRN02C | Transaction category balance (TCATBALF) |
| `CVTRA02Y` | Core | Business record layout | 50 | 1 | CBACT04C | Disclosure group / interest rate (DISCGRP) |
| `CVTRA03Y` | Core | Business record layout | 60 | 1 | CBTRN03C | Transaction type reference (TRANTYPE) |
| `CVTRA04Y` | Core | Business record layout | 60 | 1 | CBTRN03C | Transaction category reference (TRANCATG) |
| `CVTRA05Y` | Core | Business record layout | 350 | 11 | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C +5 | Posted transaction record (TRANSACT) |
| `CVTRA06Y` | Core | Business record layout | 350 | 2 | CBTRN01C, CBTRN02C | Daily transaction input record (DALYTRAN) |
| `CODATECN` | Core | Common utility | 80 | 1 | CBACT01C | Date-conversion interface for COBDATFT |
| `CSDAT01Y` | Core | Common utility | 58 | 21 | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC +15 | Current date/time work area |
| `CSLKPCDY` | Core | Common utility | 3/2/7 | 1 | COACTUPC | Lookup tables: NANP area codes, US state codes, state/ZIP prefixes |
| `CSMSG02Y` | Core | Common utility | 134 | 7 | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C +1 | Abend data work area |
| `CSUTLDPY` | Core | Common utility | — | 1 | COACTUPC | Date-validation procedure division code |
| `CSUTLDWY` | Core | Common utility | 8/8/4 | 2 | COACTUPC, COTRTUPC | Date-validation working storage |
| `UNUSED1Y` | Core | Common utility | 80 | 0 |  | Unused data layout (no references) |
| `AUTHFRDS` | Authorization (IMS/DB2/MQ) | Db2 DCLGEN | 204 | 1 | COPAUS2C | DCLGEN for CARDDEMO.AUTHFRDS (fraud reports) |
| `DCLTRCAT` | Transaction Type (DB2) | Db2 DCLGEN | 58 | 1 | COTRTUPC | DCLGEN for CARDDEMO.TRANSACTION_TYPE_CATEGORY |
| `DCLTRTYP` | Transaction Type (DB2) | Db2 DCLGEN | 54 | 3 | COBTUPDT, COTRTLIC, COTRTUPC | DCLGEN for CARDDEMO.TRANSACTION_TYPE |
| `CSDB2RPY` | Transaction Type (DB2) | Db2 support | — | 1 | COTRTLIC | Db2 error-handling procedure code |
| `CSDB2RWY` | Transaction Type (DB2) | Db2 support | 81/722/2 | 1 | COTRTLIC | Db2 error-handling working storage (DSNTIAC) |
| `IMSFUNCS` | Authorization (IMS/DB2/MQ) | IMS interface | 40 | 3 | DBUNLDGS, PAUDBLOD, PAUDBUNL | DL/I function code constants |
| `PADFLPCB` | Authorization (IMS/DB2/MQ) | IMS interface | 291 | 1 | DBUNLDGS | GSAM child-file PCB mask |
| `PASFLPCB` | Authorization (IMS/DB2/MQ) | IMS interface | 136 | 1 | DBUNLDGS | GSAM root-file PCB mask |
| `PAUTBPCB` | Authorization (IMS/DB2/MQ) | IMS interface | 291 | 3 | DBUNLDGS, PAUDBLOD, PAUDBUNL | PAUTDB DB PCB mask |
| `CIPAUDTY` | Authorization (IMS/DB2/MQ) | IMS segment | 8/6/6 | 8 | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, DBUNLDGS +2 | IMS pending authorization detail segment (PAUTDTL1) |
| `CIPAUSMY` | Authorization (IMS/DB2/MQ) | IMS segment | 6/9/1 | 7 | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD +1 | IMS pending authorization summary segment (PAUTSUM0) |
| `CVEXPORT` | Core | Interface layout | 500 | 2 | CBEXPORT, CBIMPORT | Multi-record export/import layout (REDEFINES per entity) |
| `CCPAURLY` | Authorization (IMS/DB2/MQ) | MQ message | 16/15/6 | 1 | COPAUA0C | MQ authorization response message |
| `CCPAURQY` | Authorization (IMS/DB2/MQ) | MQ message | 6/6/16 | 1 | COPAUA0C | MQ authorization request message |
| `COADM02Y` | Core | Online framework | 272 | 1 | COADM01C | Admin menu option table |
| `COCOM01Y` | Core | Online framework | 160 | 21 | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC +15 | Shared CICS COMMAREA (routing + context) |
| `COMEN02Y` | Core | Online framework | 508 | 1 | COMEN01C | Main menu option table (program/transaction routing) |
| `COTTL01Y` | Core | Online framework | 120 | 21 | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC +15 | Screen title literals |
| `CSMSG01Y` | Core | Online framework | 100 | 21 | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC +15 | Common user messages |
| `CSSETATY` | Core | Online framework | — | 2 | COACTUPC, COTRTUPC | Screen attribute-setting code fragment |
| `CSSTRPFY` | Core | Online framework | — | 7 | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC +1 | PF-key mapping code fragment |
| `CVCRD01Y` | Core | Online framework | 213 | 7 | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC +1 | Card screens work areas / AID keys |
| `CVTRA07Y` | Core | Report layout | 115/114/114 | 1 | CBTRN03C | Transaction report header/detail/total print lines |

Copybooks with no `COPY`/`INCLUDE` reference in any program: `UNUSED1Y`.

### 3.1 BMS symbolic-map copybooks

| Copybook | Module | Mapset | Used by |
|---|---|---|---|
| `COACTUP` | Core | `COACTUP` | COACTUPC |
| `COACTVW` | Core | `COACTVW` | COACTVWC |
| `COADM01` | Core | `COADM01` | COADM01C |
| `COBIL00` | Core | `COBIL00` | COBIL00C |
| `COCRDLI` | Core | `COCRDLI` | COCRDLIC |
| `COCRDSL` | Core | `COCRDSL` | COCRDSLC |
| `COCRDUP` | Core | `COCRDUP` | COCRDUPC |
| `COMEN01` | Core | `COMEN01` | COMEN01C |
| `COPAU00` | Authorization (IMS/DB2/MQ) | `COPAU00` | COPAUS0C |
| `COPAU01` | Authorization (IMS/DB2/MQ) | `COPAU01` | COPAUS1C |
| `CORPT00` | Core | `CORPT00` | CORPT00C |
| `COSGN00` | Core | `COSGN00` | COSGN00C |
| `COTRN00` | Core | `COTRN00` | COTRN00C |
| `COTRN01` | Core | `COTRN01` | COTRN01C |
| `COTRN02` | Core | `COTRN02` | COTRN02C |
| `COTRTLI` | Transaction Type (DB2) | `COTRTLI` | COTRTLIC |
| `COTRTUP` | Transaction Type (DB2) | `COTRTUP` | COTRTUPC |
| `COUSR00` | Core | `COUSR00` | COUSR00C |
| `COUSR01` | Core | `COUSR01` | COUSR01C |
| `COUSR02` | Core | `COUSR02` | COUSR02C |
| `COUSR03` | Core | `COUSR03` | COUSR03C |

## 4. BMS Mapsets (Screens)

| Mapset | Module | Maps | Fields (named / input) | Screen | Program | Transaction | Classification |
|---|---|---|---|---|---|---|---|
| `COACTUP` | Core | CACTUPA | 54 / 43 | CardDemo - Account Update Screen | COACTUPC | CAUP | Data entry/maintenance |
| `COACTVW` | Core | CACTVWA | 37 / 1 | CardDemo - Account Viewer Screen | COACTVWC | CAVW | Inquiry |
| `COADM01` | Core | COADM1A | 20 / 1 | CardDemo - Admin Menu Screen | COADM01C | CA00 | Menu |
| `COBIL00` | Core | COBIL0A | 10 / 2 | Bill Payment | COBIL00C | CB00 | Data entry/maintenance |
| `COCRDLI` | Core | CCRDLIA | 45 / 2 | CardDemo - Card Listing Screen | COCRDLIC | CCLI | List/browse |
| `COCRDSL` | Core | CCRDSLA | 15 / 2 | CardDemo - Card Selection Screen | COCRDSLC | CCDL | Inquiry |
| `COCRDUP` | Core | CCRDUPA | 17 / 5 | CardDemo - Card Update Screen | COCRDUPC | CCUP | Data entry/maintenance |
| `COMEN01` | Core | COMEN1A | 20 / 1 | CardDemo - Main Menu Screen | COMEN01C | CM00 | Menu |
| `COPAU00` | Authorization (IMS/DB2/MQ) | COPAU0A | 62 / 6 | CardDemo - Pending Authorization Screen | COPAUS0C | CPVS | List/browse |
| `COPAU01` | Authorization (IMS/DB2/MQ) | COPAU1A | 27 / 0 | CardDemo - Pending Authorization Details Screen | COPAUS1C | CPVD | Inquiry |
| `CORPT00` | Core | CORPT0A | 17 / 10 | CardDemo - Main Menu Screen | CORPT00C | CR00 | Report request |
| `COSGN00` | Core | COSGN0A | 11 / 3 | CardDemo - Login Screen | COSGN00C | CC00 | Sign-on |
| `COTRN00` | Core | COTRN0A | 59 / 11 | CardDemo - Transaction List | COTRN00C | CT00 | List/browse |
| `COTRN01` | Core | COTRN1A | 21 / 1 | CardDemo - Transaction View | COTRN01C | CT01 | Inquiry |
| `COTRN02` | Core | COTRN2A | 21 / 14 | CardDemo - Transaction Add | COTRN02C | CT02 | Data entry/maintenance |
| `COTRTLI` | Transaction Type (DB2) | CTRTLIA | 40 / 9 | CardDemo - Tran Type Listing Screen | COTRTLIC | CTLI | List/browse |
| `COTRTUP` | Transaction Type (DB2) | CTRTUPA | 15 / 2 | CardDemo -Transaction Type Update Screen | COTRTUPC | CTTU | Data entry/maintenance |
| `COUSR00` | Core | COUSR0A | 59 / 11 | CardDemo - List Users | COUSR00C | CU00 | List/browse |
| `COUSR01` | Core | COUSR1A | 12 / 5 | CardDemo - Add User | COUSR01C | CU01 | Data entry/maintenance |
| `COUSR02` | Core | COUSR2A | 12 / 5 | CardDemo - Update User | COUSR02C | CU02 | Data entry/maintenance |
| `COUSR03` | Core | COUSR3A | 11 / 1 | CardDemo - Delete User | COUSR03C | CU03 | Data entry/maintenance |

## 5. CICS Transactions

| Transaction | Program | CSD source | Function |
|---|---|---|---|
| `CA00` | `COADM01C` | CARDDEMO.CSD | Admin menu; dispatches to user-admin and transaction-type options in COADM02Y |
| `CAUP` | `COACTUPC` | CARDDEMO.CSD | Account update: edits/validates ~40 account and customer fields and rewrites ACCTDAT and C |
| `CAVW` | `COACTVWC` | CARDDEMO.CSD | Account view: reads account, card cross-reference and customer to show a combined account  |
| `CB00` | `COBIL00C` | CARDDEMO.CSD | Bill payment: pays full account balance, writes a payment transaction and rewrites ACCTDAT |
| `CC00` | `COSGN00C` | CARDDEMO.CSD | Sign-on: validates user ID/password against USRSEC and routes admin vs regular users |
| `CCDL` | `COCRDSLC` | CARDDEMO.CSD | Credit card detail view by account/card number |
| `CCLI` | `COCRDLIC` | CARDDEMO.CSD | Credit card list: paged browse of cards (all cards for admin, account-filtered for users) |
| `CCUP` | `COCRDUPC` | CARDDEMO.CSD | Credit card update: edits embossed name, status, expiry and rewrites CARDDAT |
| `CDRA` | `COACCT01` | CRDDEMOM.csd | MQ request/reply service returning account details from ACCTDAT |
| `CDRD` | `CODATE01` | CRDDEMOM.csd | MQ request/reply service returning system date/time |
| `CDV1` | `COCRDSEC` | CARDDEMO.CSD |  |
| `CM00` | `COMEN01C` | CARDDEMO.CSD | Main menu for regular users; dispatches to options defined in COMEN02Y |
| `CP00` | `COPAUA0C` | CRDDEMO2.csd | MQ-triggered card authorization engine: reads request, checks XREF/ACCT/CUST and IMS summa |
| `CPVD` | `COPAUS1C` | CRDDEMO2.csd | Pending authorization detail screen; marks/unmarks fraud via LINK to COPAUS2C |
| `CPVS` | `COPAUS0C` | CRDDEMO2.csd | Pending authorization summary screen for an account (IMS PAUTSUM0 + child browse) |
| `CR00` | `CORPT00C` | CARDDEMO.CSD | Report request: builds TRANREPT JCL for a date range and submits it via TDQ JOBS (internal |
| `CT00` | `COTRN00C` | CARDDEMO.CSD | Transaction list: paged browse of TRANSACT |
| `CT01` | `COTRN01C` | CARDDEMO.CSD | Transaction detail view |
| `CT02` | `COTRN02C` | CARDDEMO.CSD | Add transaction: validates account/card, amount, dates (via CSUTLDTC) and writes TRANSACT |
| `CTLI` | `COTRTLIC` | CRDDEMOD.csd | Transaction type list with inline update/delete (Db2 cursor paging, 7 rows per page) |
| `CTTU` | `COTRTUPC` | CRDDEMOD.csd | Transaction type add/update/delete (Db2) |
| `CU00` | `COUSR00C` | CARDDEMO.CSD | User list (admin): paged browse of USRSEC |
| `CU01` | `COUSR01C` | CARDDEMO.CSD | Add user (admin) |
| `CU02` | `COUSR02C` | CARDDEMO.CSD | Update user (admin) |
| `CU03` | `COUSR03C` | CARDDEMO.CSD | Delete user (admin) |

## 6. JCL Jobs

| Job | Module | Classification | Steps | Programs / utilities executed | Scheduled | Purpose |
|---|---|---|---:|---|---|---|
| `CBPAUP0J` | Authorization (IMS/DB2/MQ) | Authorization batch | 1 | CBPAUP0C | CA-7 | Purge expired pending authorizations (CBPAUP0C, IMS BMP) |
| `DBPAUTP0` | Authorization (IMS/DB2/MQ) | Authorization batch | 2 | DFSRRC00, IEFBR14 | — | IMS utility unload of PAUTDB (DFSURGU0) |
| `LOADPADB` | Authorization (IMS/DB2/MQ) | Authorization batch | 1 | PAUDBLOD | — | Load PAUTDB from flat files (PAUDBLOD) |
| `UNLDGSAM` | Authorization (IMS/DB2/MQ) | Authorization batch | 1 | DBUNLDGS | — | Unload PAUTDB to GSAM (DBUNLDGS) |
| `UNLDPADB` | Authorization (IMS/DB2/MQ) | Authorization batch | 2 | IEFBR14, PAUDBUNL | — | Unload PAUTDB to flat files (PAUDBUNL) |
| `COMBTRAN` | Core | Core batch | 2 | IDCAMS, SORT | Control-M | Sort/merge backup + system transactions and reload TRANSACT |
| `INTCALC` | Core | Core batch | 1 | CBACT04C | Control-M | Compute interest & fees (CBACT04C) to SYSTRAN GDG |
| `POSTTRAN` | Core | Core batch | 1 | CBTRN02C | CA-7 | Post daily transactions (CBTRN02C) |
| `TRANBKP` | Core | Core batch | 3 | IDCAMS, PROC REPROC | Control-M | Back up TRANSACT to GDG and redefine the KSDS |
| `ACCTFILE` | Core | Data load | 3 | IDCAMS | — | Define & load account VSAM KSDS from flat file |
| `CARDFILE` | Core | Data load | 8 | IDCAMS, SDSF | — | Define & load card KSDS, AIX on account; closes/opens CICS files |
| `CUSTFILE` | Core | Data load | 5 | IDCAMS, SDSF | — | Define & load customer KSDS; closes/opens CICS files |
| `DEFCUST` | Core | Data load | 2 | IDCAMS | — | Define customer cluster (legacy/alternate) |
| `DISCGRP` | Core | Data load | 3 | IDCAMS | Control-M | Define & load disclosure group (interest rate) KSDS |
| `DUSRSECJ` | Core | Data load | 4 | IDCAMS, IEBGENER, IEFBR14 | — | Create & load user security KSDS from instream data |
| `ESDSRRDS` | Core | Data load | 6 | IDCAMS, IEBGENER, IEFBR14 | — | Demo: load user security data into ESDS and RRDS |
| `TCATBALF` | Core | Data load | 3 | IDCAMS | CA-7 | Define & load transaction category balance KSDS |
| `TRANCATG` | Core | Data load | 3 | IDCAMS | CA-7 | Define & load transaction category KSDS |
| `TRANFILE` | Core | Data load | 8 | IDCAMS, SDSF | — | Define & load transaction KSDS with timestamp AIX |
| `TRANIDX` | Core | Data load | 3 | IDCAMS | — | Define transaction AIX/path and build index |
| `TRANTYPE` | Core | Data load | 3 | IDCAMS | CA-7 | Define & load transaction type KSDS |
| `XREFFILE` | Core | Data load | 6 | IDCAMS | — | Define & load card xref KSDS with account AIX |
| `CBEXPORT` | Core | Data migration | 2 | CBEXPORT, IDCAMS | — | Export all entities to multi-record file |
| `CBIMPORT` | Core | Data migration | 1 | CBIMPORT | — | Import multi-record file into per-entity files |
| `CBADMCDJ` | Core | Infrastructure | 1 | DFHCSDUP | — | Install CardDemo CICS resources (DFHCSDUP) |
| `DALYREJS` | Core | Infrastructure | 1 | IDCAMS | — | Define DALYREJS GDG |
| `DEFGDGB` | Core | Infrastructure | 1 | IDCAMS | — | Define base GDGs (TRANSACT.BKUP, SYSTRAN, DALYREJS, TRANREPT, etc.) |
| `DEFGDGD` | Core | Infrastructure | 6 | IDCAMS, IEBGENER | — | Define reference-data GDGs and take initial backups |
| `REPTFILE` | Core | Infrastructure | 1 | IDCAMS | — | Define TRANREPT GDG |
| `CLOSEFIL` | Core | Operations | 1 | SDSF | Control-M, CA-7 | Close CICS VSAM files before batch (CEMT via SDSF) |
| `OPENFIL` | Core | Operations | 1 | SDSF | Control-M, CA-7 | Re-open CICS VSAM files after batch |
| `WAITSTEP` | Core | Operations | 1 | COBSWAIT | Control-M, CA-7 | Wait step (COBSWAIT) |
| `CREADB21` | Transaction Type (DB2) | Reference data batch | 5 | IEFBR14, IKJEFT01 | — | Create CardDemo Db2 database/tables and load type/category data |
| `MNTTRDB2` | Transaction Type (DB2) | Reference data batch | 1 | COBTUPDT | Control-M | Maintain TRANSACTION_TYPE via COBTUPDT |
| `TRANEXTR` | Transaction Type (DB2) | Reference data batch | 5 | DSNTIAUL, IEBGENER, IEFBR14 | Control-M | Extract Db2 type/category tables to flat files (DSNTIAUL) for VSAM refresh |
| `CREASTMT` | Core | Reporting | 5 | CBSTM03A, IDCAMS, IEFBR14, SORT | CA-7 | Produce customer statements in text and HTML (CBSTM03A/B) |
| `PRTCATBL` | Core | Reporting | 3 | IEFBR14, PROC REPROC, SORT | CA-7 | Back up and print transaction category balances |
| `TXT2PDF1` | Core | Reporting | 1 | IKJEFT1B | CA-7 | Convert statement text to PDF (REXX TXT2PDF) |
| `FTPJCL` | Core | Utility | 1 | FTP | — | FTP file transfer demo |
| `INTRDRJ1` | Core | Utility | 2 | IDCAMS, IEBGENER | — | Internal reader demo: backup then submit INTRDRJ2 |
| `INTRDRJ2` | Core | Utility | 1 | IDCAMS | — | Internal reader demo (submitted job) |
| `READACCT` | Core | Utility | 2 | CBACT01C, IEFBR14 | CA-7 | Account extract demo (CBACT01C) |
| `READCARD` | Core | Utility | 1 | CBACT02C | CA-7 | Print card master (CBACT02C) |
| `READCUST` | Core | Utility | 1 | CBCUS01C | CA-7 | Print customer master (CBCUS01C) |
| `READXREF` | Core | Utility | 1 | CBACT03C | CA-7 | Print card xref (CBACT03C) |

### 6.1 Procedures and sample/build JCL

| Member | Kind | Location | Steps / programs | Purpose |
|---|---|---|---|---|
| `BLDCIDB2` | PROC | `samples/proc` | PRECMP:DSNHPC, TRN:DFHECP1$, COBOL:IGYCRCTL, LKED:HEWL, BINDPK:IKJEFT01, BINDPL:IKJEFT01 | Compile CICS+Db2 program |
| `BUILDBAT` | PROC | `samples/proc` | COMPILE:IGYCRCTL, CBLPRINT:IEBGENER, LKED:HEWL | Compile/link batch COBOL |
| `BUILDBMS` | PROC | `samples/proc` | PRINT:IEBGENER, MAP:ASMA90, LKED:HEWL, DSECT:ASMA90, DISPLIST:IEBGENER, DELTEMP:IEFBR14 | Assemble BMS mapset |
| `BUILDONL` | PROC | `samples/proc` | COB:IGYCRCTL, DISPLIST:IEBGENER, COPYLINK:IEBGENER, LKED:HEWL | Compile/link CICS COBOL |
| `REPROC` | PROC | `app/proc` | PRC001:IDCAMS | IDCAMS REPRO procedure used for backups |
| `TRANREPT` | PROC | `app/proc` | STEP01R:, STEP05R:SORT, STEP10R:CBTRN03C | Transaction detail report for date range (CBTRN03C) |
| `BATCMP_SAMPLE` | Sample | `samples/jcl` | BATCMP: |  |
| `BMSCMP_SAMPLE` | Sample | `samples/jcl` | STEP1:, SDSF1:SDSF |  |
| `CICCMP_SAMPLE` | Sample | `samples/jcl` | CICSCMP:, NEWCOPY:SDSF |  |
| `CICDBCMP_SAMPLE` | Sample | `samples/jcl` | CICSCMP: |  |
| `IMSMQCMP_SAMPLE` | Sample | `samples/jcl` | TRN:DFHECP1$, COBOL:IGYCRCTL, COPYIMS:IEBGENER, COPYLINK:IEBGENER, COPYMQ:IEBGENER, LKED:IEWL |  |
| `LISTCAT_SAMPLE` | Sample | `samples/jcl` | STEP01:IEFBR14, STEP05:IDCAMS |  |
| `RACFCMDS_SAMPLE` | Sample | `samples/jcl` | TSOBAT:IKJEFT01 |  |
| `REPRTEST_SAMPLE` | Sample | `samples/jcl` | STEP05: |  |
| `SORTTEST_SAMPLE` | Sample | `samples/jcl` | STEP05R:SORT |  |

## 7. Other Artifacts

| Artifact | Location | Content |
|---|---|---|
| CICS CSD | `app/csd/CARDDEMO.CSD`, module `csd/` folders | Group definitions for programs, mapsets, transactions and VSAM files |
| IDCAMS control cards | `app/ctl/REPROCT.ctl`, `app/app-transaction-type-db2/ctl/*.ctl` | REPRO control, Db2 create/free/load/DSNTEP4/DSNTIAD statements |
| Db2 DDL | `app/app-*/ddl/*.ddl` | `TRNTYPE`, `TRNTYCAT`, `AUTHFRDS` tables and unique indexes |
| IMS DBD/PSB | `app/app-authorization-ims-db2-mq/ims` | HIDAM DB `DBPAUTP0` (root `PAUTSUM0` 100 B keyed by packed account id, child `PAUTDTL1` 200 B), index `DBPAUTX0`, GSAM `PASFLDBD`/`PADFLDBD` |
| Schedules | `app/scheduler` | Control-M (daily backup, weekly reference refresh, monthly interest) and CA-7 trigger chains |
| Catalog listing | `app/catlg/LISTCAT.txt` | IDCAMS LISTCAT of the CardDemo datasets |
| Sample data | `app/data/ASCII`, `app/data/EBCDIC` | Seed data for all VSAM files |

## 8. Method & limitations

- Generated by a purpose-built static parser (fixed-format COBOL area A/B, copybook expansion by name, JCL with continuations and in-stream SYSIN, BMS macros, CSD DEFINE statements). No code was compiled or executed.
- Complexity values are estimates; dynamic routing (e.g. `XCTL PROGRAM(CDEMO-TO-PROGRAM)`) is resolved through menu tables and literals and is documented in [DEPENDENCY_MAP.md](DEPENDENCY_MAP.md).
