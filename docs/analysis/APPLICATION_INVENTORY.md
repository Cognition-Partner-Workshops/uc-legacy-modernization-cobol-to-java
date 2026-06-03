# CardDemo — Application Inventory

> Comprehensive catalog of every source artifact in the `uc-legacy-modernization-cobol-to-java`
> (AWS CardDemo) mainframe credit-card management system. Produced by static analysis of the
> `app/` tree. Use this as the master index for modernization scoping.

## 1. Summary by Artifact Type

| Artifact Type | Count | Location(s) |
|:--------------|------:|:------------|
| COBOL programs (`.cbl`) | 39 | `app/cbl`, `app/app-*/cbl` |
| Copybooks (`.cpy`) | 41 | `app/cpy`, `app/cpy-bms`, `app/app-*/cpy`, `app/app-*/cpy-bms` |
| JCL jobs (`.jcl`) | 38 | `app/jcl`, `app/app-*/jcl` |
| BMS map sets (`.bms`) | 21 | `app/bms`, `app/app-*/bms` |
| Assembler programs (`.asm`) | 2 | `app/asm` |
| JCL procedures (`.prc`) | 2 | `app/proc` |
| CICS CSD definitions (`.csd`/`.CSD`) | 4 | `app/csd`, `app/app-*/csd` |
| DB2 DDL (`.ddl`) | 6 | `app/app-*/ddl` |
| DB2 DCLGEN (`.dcl`) | 3 | `app/app-*/dcl` |
| DB2/utility control cards (`.ctl`) | 8 | `app/ctl`, `app/app-transaction-type-db2/ctl` |
| IMS DBD/PSB (`.dbd`/`.psb`/`.DBD`/`.PSB`) | 8 | `app/app-authorization-ims-db2-mq/ims` |
| Macro library members (`.mac`) | 2 | `app/maclib` |
| Scheduler definitions | 2 | `app/scheduler` (Control-M XML + CA-7) |
| Sample data (ASCII / EBCDIC) | 9 / 14 | `app/data/ASCII`, `app/data/EBCDIC` |

### Sub-system layout

The codebase is one **base application** plus three **optional extension modules**:

| Sub-system | Path | Technologies | Purpose |
|:-----------|:-----|:-------------|:--------|
| Base application | `app/` (`cbl`, `cpy`, `jcl`, `bms`, `csd`, `asm`, `proc`, `maclib`) | COBOL, CICS, VSAM (KSDS/AIX), JCL, BMS, RACF, Assembler | Customer / Account / Card / Transaction / Billing / Reporting |
| Pending Authorizations | `app/app-authorization-ims-db2-mq/` | COBOL, CICS, **IMS DB**, **DB2**, **MQ**, BMS | Real-time card authorization, fraud marking, batch purge |
| Transaction Type Mgmt | `app/app-transaction-type-db2/` | COBOL, CICS, **DB2** (cursors), BMS | Reference-data maintenance of transaction types/categories |
| Account Extraction via MQ | `app/app-vsam-mq/` | COBOL, CICS, **MQ**, VSAM | Async system-date and account-detail inquiry over MQ |

---

## 2. Online (CICS) Programs

Interactive 3270 transactions. Each is paired with a BMS map (except MQ server programs) and routed
through the CICS commarea (`COCOM01Y`).

| Tran | Program | BMS Map | Lines | Function | Module |
|:-----|:--------|:--------|------:|:---------|:-------|
| CC00 | COSGN00C | COSGN00 | 260 | Signon screen / RACF-style authentication | Base |
| CM00 | COMEN01C | COMEN01 | 308 | Main menu (regular users) | Base |
| CA00 | COADM01C | COADM01 | 288 | Admin menu | Base |
| CAVW | COACTVWC | COACTVW | 941 | Account view (read Account+Customer+XREF) | Base |
| CAUP | COACTUPC | COACTUP | 4236 | Account update (largest program) | Base |
| CCLI | COCRDLIC | COCRDLI | 1459 | Credit-card list (browse w/ paging) | Base |
| CCDL | COCRDSLC | COCRDSL | 887 | Credit-card detail view | Base |
| CCUP | COCRDUPC | COCRDUP | 1560 | Credit-card update | Base |
| CT00 | COTRN00C | COTRN00 | 699 | Transaction list | Base |
| CT01 | COTRN01C | COTRN01 | 330 | Transaction view | Base |
| CT02 | COTRN02C | COTRN02 | 783 | Transaction add | Base |
| CR00 | CORPT00C | CORPT00 | 649 | Transaction report request (submits batch) | Base |
| CB00 | COBIL00C | COBIL00 | 572 | Bill payment (pay balance in full) | Base |
| CU00 | COUSR00C | COUSR00 | 695 | List users | Base |
| CU01 | COUSR01C | COUSR01 | 299 | Add user | Base |
| CU02 | COUSR02C | COUSR02 | 414 | Update user | Base |
| CU03 | COUSR03C | COUSR03 | 359 | Delete user | Base |
| CPVS | COPAUS0C | COPAU00 | 1032 | Pending-authorization summary (IMS+VSAM read) | Auth (IMS/DB2/MQ) |
| CPVD | COPAUS1C | COPAU01 | 604 | Pending-authorization detail (IMS update + DB2 insert) | Auth (IMS/DB2/MQ) |
| —    | COPAUS2C | —       | 244 | Mark authorization as fraud (DB2) | Auth (IMS/DB2/MQ) |
| CP00 | COPAUA0C | —       | 1026 | Authorization decision engine (MQ trigger, IMS) | Auth (IMS/DB2/MQ) |
| CTTU | COTRTUPC | COTRTUP | 1702 | Transaction-type add/edit (DB2 update/insert) | Tran-Type (DB2) |
| CTLI | COTRTLIC | COTRTLI | 2098 | Transaction-type list/update/delete (DB2 cursor) | Tran-Type (DB2) |
| CDRD | CODATE01 | —       | 524 | Inquire system date via MQ (server) | Acct-Extract (MQ) |
| CDRA | COACCT01 | —       | 620 | Inquire account details via MQ (server) | Acct-Extract (MQ) |

---

## 3. Batch (JCL-invoked) Programs

| Program | Lines | Function | Invoked by job | Module |
|:--------|------:|:---------|:---------------|:-------|
| CBACT01C | 430 | Read & print Account master; write extract files | READACCT | Base |
| CBACT02C | 178 | Read & print Card master | READCARD | Base |
| CBACT03C | 178 | Read & print Card cross-reference | READXREF | Base |
| CBACT04C | 652 | **Interest calculation** (accrues interest, writes transactions) | INTCALC | Base |
| CBCUS01C | 178 | Read & print Customer master | READCUST | Base |
| CBTRN01C | 494 | Validate daily transactions against masters | (driver) | Base |
| CBTRN02C | 731 | **Transaction posting** (updates balances, TCATBAL) | POSTTRAN | Base |
| CBTRN03C | 649 | Transaction detail report | TRANREPT | Base |
| CBEXPORT | 582 | Export consolidated data for branch migration (VB out) | CBEXPORT | Base |
| CBIMPORT | 487 | Import branch-migration export into masters | CBIMPORT | Base |
| COBSWAIT | 41 | Utility: wait/sleep (calls MVSWAIT assembler) | WAITSTEP | Base |
| CBPAUP0C | 386 | Purge expired pending authorizations (IMS delete) | CBPAUP0J | Auth (IMS/DB2/MQ) |
| COBTUPDT | 237 | Maintain DB2 transaction-type table from flat file | MNTTRDB2 | Tran-Type (DB2) |

> Note: README also references `CBSTM03A` (statement print, job CREASTMT). That program source is
> not present in this repository snapshot — flagged as an **external/missing dependency**.

### Utility / shared sub-programs (called, not job entrypoints)

| Program | Lines | Type | Function |
|:--------|------:|:-----|:---------|
| CSUTLDTC | 157 | COBOL subroutine | Date validation/format (wraps LE `CEEDAYS`) |
| COBDATFT | (asm) | Assembler | Date format conversion (`app/asm/COBDATFT.asm`) |
| MVSWAIT  | (asm) | Assembler | Timer/wait service (`app/asm/MVSWAIT.asm`) |

---

## 4. Copybooks (Data Structures & Shared Logic)

Naming conventions: `CV*Y` = VSAM record layouts, `CS*Y` = shared/common utilities, `CO*` =
online screen/menu structures, `CI*Y` = IMS segments, `CC*Y` = MQ message layouts.

### 4.1 Business record layouts (VSAM master/reference files)

| Copybook | 01-level | Backing file | Entity |
|:---------|:---------|:-------------|:-------|
| CVACT01Y | ACCOUNT-RECORD | ACCTDAT | Account master |
| CVACT02Y | CARD-RECORD | CARDDAT | Card master |
| CVACT03Y | CARD-XREF-RECORD | CCXREF / CARDAIX | Card↔Account↔Customer cross-ref |
| CVCUS01Y | CUSTOMER-RECORD | CUSTDAT | Customer master |
| CUSTREC  | CUSTOMER-RECORD | (alt customer layout) | Customer (duplicate layout) |
| CVTRA05Y | TRAN-RECORD | TRANSACT | Posted transaction |
| CVTRA06Y | DALYTRAN-RECORD | DALYTRAN | Daily (unposted) transaction |
| CVTRA01Y | TRAN-CAT-BAL-RECORD | TCATBAL | Transaction-category balance |
| CVTRA02Y | DIS-GROUP-RECORD | DISCGRP | Disclosure group (interest rates) |
| CVTRA03Y | TRAN-TYPE-RECORD | TRANTYPE | Transaction type reference |
| CVTRA04Y | TRAN-CAT-RECORD | TRANCATG | Transaction category reference |
| CVTRA07Y | REPORT-NAME-HEADER | (report) | Report header constants |
| CSUSR01Y | SEC-USER-DATA | USRSEC | User security record |
| CVCRD01Y | CC-WORK-AREAS | (work area) | Card-program shared work area / PFK flags |
| CVEXPORT | EXPORT-RECORD | EXPORT.DATA | Branch-migration export (REDEFINES per rec type) |
| UNUSED1Y | — | — | Unused/placeholder layout |

### 4.2 Shared/common utility copybooks

| Copybook | Purpose |
|:---------|:--------|
| COCOM01Y | CARDDEMO-COMMAREA — inter-program state passed across CICS XCTL |
| COTTL01Y | Standard screen title block |
| CSDAT01Y | Current-date working storage |
| CSMSG01Y | Common message / ABEND text |
| CSMSG02Y | ABEND handling structure (CABENDD) |
| CSSETATY | Screen attribute settings |
| CSLKPCDY | Lookup-code repository (US phone area codes, state codes) |
| CSUTLDPY | Date-validation PROCEDURE DIVISION copybook |
| CSUTLDWY | Date-validation WORKING-STORAGE copybook |
| CSSTRPFY | String / PF-key handling |
| CODATECN | Date constants |
| COMEN02Y | Main-menu option table (option → program routing) |
| COADM02Y | Admin-menu option table (option → program routing) |

### 4.3 BMS symbolic map copybooks

`app/cpy-bms/` and the module `cpy-bms/` folders hold the generated symbolic maps for the BMS map
sets (e.g. `COPAU00`, `COPAU01`, `COTRTLI`, `COTRTUP`). One symbolic copybook per online map.

### 4.4 Optional-module copybooks

| Copybook | Module | Purpose |
|:---------|:-------|:--------|
| CIPAUDTY | Auth | IMS pending-authorization **detail** segment |
| CIPAUSMY | Auth | IMS pending-authorization **summary** segment |
| CCPAURQY | Auth | MQ authorization **request** message |
| CCPAURLY | Auth | MQ authorization **reply** message |
| CCPAUERY | Auth | MQ authorization **error** message |
| IMSFUNCS | Auth | IMS DL/I function-code constants |
| COPAU00 / COPAU01 | Auth | BMS symbolic maps for auth screens |
| CSDB2RPY | Tran-Type | Common DB2 PROCEDURE-division logic (SQLCODE handling) |
| CSDB2RWY | Tran-Type | Common DB2 WORKING-STORAGE (SQLCA etc.) |
| DCLTRTYP (.dcl) | Tran-Type | DCLGEN for `CARDDEMO.TRANSACTION_TYPE` |
| DCLTRCAT (.dcl) | Tran-Type | DCLGEN for transaction-category table |
| AUTHFRDS (.dcl) | Auth | DCLGEN for authorization-fraud DB2 table |
| COTRTLI / COTRTUP | Tran-Type | BMS symbolic maps for tran-type screens |

---

## 5. BMS Map Sets

| Map set | Screen | Module |
|:--------|:-------|:-------|
| COSGN00 | Signon | Base |
| COMEN01 | Main menu | Base |
| COADM01 | Admin menu | Base |
| COACTVW | Account view | Base |
| COACTUP | Account update | Base |
| COCRDLI | Card list | Base |
| COCRDSL | Card detail | Base |
| COCRDUP | Card update | Base |
| COTRN00 | Transaction list | Base |
| COTRN01 | Transaction view | Base |
| COTRN02 | Transaction add | Base |
| CORPT00 | Report request | Base |
| COBIL00 | Bill payment | Base |
| COUSR00 | User list | Base |
| COUSR01 | User add | Base |
| COUSR02 | User update | Base |
| COUSR03 | User delete | Base |
| COPAU00 | Pending-auth summary | Auth |
| COPAU01 | Pending-auth detail | Auth |
| COTRTLI | Tran-type list | Tran-Type |
| COTRTUP | Tran-type add/edit | Tran-Type |

---

## 6. JCL Jobs

### 6.1 Data-setup / IDCAMS jobs (VSAM define & load)

| Job | Utility | Function |
|:----|:--------|:---------|
| DEFGDGB | IDCAMS | Define GDG bases |
| DEFGDGD | IDCAMS / IEBGENER | Define GDG bases for DB2 path |
| DEFCUST | IDCAMS | Define customer VSAM cluster |
| ACCTFILE | IDCAMS | Define/refresh Account master |
| CARDFILE | IDCAMS / SDSF | Define/refresh Card master |
| CUSTFILE | IDCAMS / SDSF | Define/refresh Customer master |
| XREFFILE | IDCAMS | Define/load card cross-reference |
| TRANFILE | IDCAMS / SDSF | Load Transaction master |
| TRANCATG | IDCAMS | Load transaction categories |
| TRANTYPE | IDCAMS | Load transaction types |
| TCATBALF | IDCAMS | Refresh transaction-category balance |
| DISCGRP | IDCAMS | Load disclosure-group file |
| TRANBKP | IDCAMS (+PROC) | Backup/refresh Transaction master |
| TRANIDX | IDCAMS | Define AIX over transaction file |
| REPTFILE | IDCAMS | Define report output dataset |
| DALYREJS | IDCAMS | Define daily-rejects dataset |
| ESDSRRDS | IDCAMS / IEBGENER / IEFBR14 | Create ESDS & RRDS VSAM files |
| DUSRSECJ | IDCAMS / IEBGENER / IEFBR14 | Initial load of user-security file |
| PRTCATBL | SORT (+PROC) | Print category-balance table |

### 6.2 CICS file-availability jobs

| Job | Utility | Function |
|:----|:--------|:---------|
| CLOSEFIL | SDSF | Close VSAM files in CICS (CEMT) |
| OPENFIL | SDSF | Open VSAM files in CICS (CEMT) |
| WAITSTEP | COBSWAIT | Pause the batch stream for a fixed time |

### 6.3 Business batch jobs

| Job | Program | Function |
|:----|:--------|:---------|
| POSTTRAN | CBTRN02C | Daily transaction posting |
| INTCALC | CBACT04C | Interest calculation |
| COMBTRAN | SORT | Combine system + daily transactions |
| TRANREPT | CBTRN03C (+PROC) | Transaction report (submitted from CICS) |
| READACCT | CBACT01C | Read/print account file |
| READCARD | CBACT02C | Read/print card file |
| READCUST | CBCUS01C | Read/print customer file |
| READXREF | CBACT03C | Read/print cross-reference |
| CBEXPORT | CBEXPORT | Branch-migration export |
| CBIMPORT | CBIMPORT | Branch-migration import |
| CBADMCDJ | DFHCSDUP | CICS CSD batch update (resource definition) |

### 6.4 Optional-module jobs

| Job | Program/Utility | Module |
|:----|:----------------|:-------|
| CBPAUP0J | CBPAUP0C (via DFSRRC00) | Auth — purge expired authorizations |
| DBPAUTP0 | DFSRRC00 / IEFBR14 | Auth — IMS DB load/unload |
| CREADB21 | IKJEFT01 / DSNTEP4 | Tran-Type — create DB2 DB & load tables |
| TRANEXTR | IKJEFT01 / DSNTIAUL / IEBGENER | Tran-Type — unload DB2 tran-type data |
| MNTTRDB2 | IKJEFT01 → COBTUPDT | Tran-Type — maintain tran-type table |

---

## 7. Non-COBOL / Infrastructure Artifacts

| Artifact | File(s) | Purpose |
|:---------|:--------|:--------|
| Assembler | `app/asm/COBDATFT.asm`, `app/asm/MVSWAIT.asm` | Date conversion; batch timer |
| Macro lib | `app/maclib/COCDATFT.mac`, `ASMWAIT.mac` | Assembler macros for above |
| JCL procs | `app/proc/REPROC.prc`, `app/proc/TRANREPT.prc` | Reusable report procedures |
| CICS CSD | `app/csd/CARDDEMO.CSD` (+ module CSDs) | Transaction/program/file resource definitions |
| DB2 DDL | `app/app-transaction-type-db2/ddl/*.ddl`, `app/app-authorization-ims-db2-mq/ddl/*.ddl` | Table & index definitions (TRNTYPE, TRNTYCAT, AUTHFRDS) |
| DB2 DCLGEN | `*/dcl/*.dcl` | Host-variable declarations |
| DB2/util ctl | `*/ctl/*.ctl` | DSNTEP4 / DSNTIAUL / LOAD control cards |
| IMS DBD/PSB | `app/app-authorization-ims-db2-mq/ims/*` | `DBPAUTP0`/`DBPAUTX0` DBDs, `PSBPAUTB`/`PSBPAUTL` PSBs |
| Scheduler | `app/scheduler/CardDemo.controlm`, `CardDemo.ca7` | Control-M (XML) & CA-7 batch schedules |
| Sample data | `app/data/ASCII/*.txt`, `app/data/EBCDIC/AWS.M2.CARDDEMO.*` | Seed data in both encodings |

---

## 8. Classification Legend

- **Online / CICS**: 3270 pseudo-conversational programs driven by BMS maps and the commarea.
- **Batch / JCL**: programs run as job steps, file-driven, no terminal I/O.
- **Utility / sub-program**: called via static `CALL`, not an entrypoint.
- **Base**: required core application.
- **Optional module**: independently installable extension (Auth / Tran-Type / Acct-Extract).
