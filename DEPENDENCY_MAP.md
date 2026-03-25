# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
> **Purpose:** Call graph, data lineage, and inter-module dependency analysis

---

## Table of Contents

1. [Online Transaction Flow (CICS)](#1-online-transaction-flow-cics)
2. [Program-to-Program Call Graph](#2-program-to-program-call-graph)
3. [Program-to-Copybook Dependencies](#3-program-to-copybook-dependencies)
4. [Program-to-VSAM File Access Matrix](#4-program-to-vsam-file-access-matrix)
5. [JCL Job-to-Program Mapping](#5-jcl-job-to-program-mapping)
6. [JCL Job Data Lineage](#6-jcl-job-data-lineage)
7. [Batch Processing Sequence](#7-batch-processing-sequence)
8. [BMS Map-to-Program Bindings](#8-bms-map-to-program-bindings)
9. [Shared Copybook Usage Matrix](#9-shared-copybook-usage-matrix)
10. [Cross-Module Dependencies (Optional Modules)](#10-cross-module-dependencies-optional-modules)

---

## 1. Online Transaction Flow (CICS)

The online system uses CICS XCTL (transfer control) to navigate between programs, with COMMAREA (COCOM01Y) carrying session state.

```
                            ┌─────────────┐
                            │  3270 Term  │
                            └──────┬──────┘
                                   │ CC00
                            ┌──────▼──────┐
                            │  COSGN00C   │ Sign-on
                            │  (Login)    │
                            └──────┬──────┘
                                   │ XCTL
                        ┌──────────┴──────────┐
                        │                     │
              ┌─────────▼─────────┐  ┌────────▼─────────┐
              │    COMEN01C       │  │    COADM01C      │
              │  (Main Menu)      │  │  (Admin Menu)    │
              │  [Regular User]   │  │  [Admin User]    │
              └────────┬──────────┘  └────────┬─────────┘
                       │                      │
         ┌─────┬──────┬┴──────┬─────┐   ┌────┴────┐
         │     │      │       │     │   │         │
         ▼     ▼      ▼       ▼     ▼   ▼         ▼
      COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C COUSR00C
      AcctView CardList TranList Reports BillPay  UserList
         │        │        │                         │
         │     ┌──┴──┐  ┌──┴──┐              ┌──────┼───────┐
         │     │     │  │     │              │      │       │
         ▼     ▼     ▼  ▼     ▼              ▼      ▼       ▼
      COACTUPC COCRDSLC COTRN01C          COUSR01C COUSR02C COUSR03C
      AcctUpd  CardView TranView          AddUser  UpdUser  DelUser
                  │        │
                  ▼        ▼
               COCRDUPC COTRN02C
               CardUpd  TranAdd
```

### CICS XCTL (Transfer Control) Relationships

| Source Program | Target Program | Mechanism      | Condition                         |
|---------------|----------------|----------------|-----------------------------------|
| COSGN00C      | COMEN01C       | XCTL           | Successful regular user login     |
| COSGN00C      | COADM01C       | XCTL           | Successful admin user login       |
| COMEN01C      | COACTVWC       | XCTL           | Menu option: Account View         |
| COMEN01C      | COCRDLIC       | XCTL           | Menu option: Card List            |
| COMEN01C      | COTRN00C       | XCTL           | Menu option: Transaction List     |
| COMEN01C      | CORPT00C       | XCTL           | Menu option: Reports              |
| COMEN01C      | COBIL00C       | XCTL           | Menu option: Bill Payment         |
| COADM01C      | COUSR00C       | XCTL           | Admin option: User List           |
| COACTVWC      | COACTUPC       | XCTL           | User selects Update               |
| COACTVWC      | COMEN01C       | RETURN TRANSID | PF3 (Back to menu)                |
| COACTUPC      | COMEN01C       | RETURN TRANSID | PF3 (Back to menu)                |
| COCRDLIC      | COCRDSLC       | XCTL           | User selects a card to view       |
| COCRDLIC      | COCRDUPC       | XCTL           | User selects a card to update     |
| COCRDSLC      | COCRDLIC       | XCTL           | PF3 (Back to card list)           |
| COCRDSLC      | COCRDUPC       | XCTL           | User selects Update               |
| COCRDUPC      | COCRDLIC       | XCTL           | PF3 (Back to card list)           |
| COTRN00C      | COTRN01C       | XCTL           | User selects a transaction        |
| COTRN00C      | COTRN02C       | XCTL           | User selects Add                  |
| COTRN01C      | COTRN00C       | XCTL           | PF3 (Back to list)                |
| COTRN02C      | COTRN00C       | XCTL           | PF3 (Back to list)                |
| COUSR00C      | COUSR01C       | XCTL           | Admin selects Add User            |
| COUSR00C      | COUSR02C       | XCTL           | Admin selects Update User         |
| COUSR00C      | COUSR03C       | XCTL           | Admin selects Delete User         |
| COUSR01C      | COUSR00C       | XCTL           | PF3 (Back to user list)           |
| COUSR02C      | COUSR00C       | XCTL           | PF3 (Back to user list)           |
| COUSR03C      | COUSR00C       | XCTL           | PF3 (Back to user list)           |
| CORPT00C      | COMEN01C       | RETURN TRANSID | PF3 (Back to menu)                |
| COBIL00C      | COMEN01C       | RETURN TRANSID | PF3 (Back to menu)                |

---

## 2. Program-to-Program Call Graph

### CALL Relationships (batch subroutine calls)

| Caller Program | Called Program | Call Type        | Purpose                                     |
|---------------|----------------|------------------|---------------------------------------------|
| CBSTM03A      | CBSTM03B       | CALL (COBOL)     | File I/O subroutine for statement generation|
| CBACT04C      | CSUTLDTC       | CALL (COBOL)     | Date conversion for interest calculation    |
| COBSWAIT      | MVSWAIT        | CALL (ASM)       | MVS wait macro for timer delay              |
| COACTUPC      | CSUTLDTC       | CALL (COBOL)     | Date validation for account updates         |
| COTRN02C      | CSUTLDTC       | CALL (COBOL)     | Date validation for new transactions        |
| COCRDUPC      | CSUTLDTC       | CALL (COBOL)     | Date validation for card updates            |

### Inline COPY (Code Inclusion)

Several programs use COPY ... REPLACING for reusable logic:

| Pattern             | Copybook   | Used By                                              | Purpose                      |
|--------------------|------------|------------------------------------------------------|------------------------------|
| Set field attribute | CSSETATY   | COACTUPC (40+ uses), COCRDUPC, COBIL00C, COTRN02C   | BMS field attribute setting  |
| String formatting   | CSSTRPFY   | COACTUPC, COCRDLIC, COACTVWC                         | String strip/format utility  |
| Date utility        | CSUTLDPY   | COACTUPC, COCRDUPC, COTRN02C                         | Date processing procedure    |

---

## 3. Program-to-Copybook Dependencies

### Online Programs

| Program    | Data Copybooks                                           | BMS Copybooks | CICS Copybooks     |
|------------|----------------------------------------------------------|---------------|--------------------|
| COSGN00C   | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y       | COSGN00       | DFHAID, DFHBMSCA   |
| COMEN01C   | COCOM01Y, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | COMEN01   | DFHAID, DFHBMSCA   |
| COADM01C   | COCOM01Y, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | COADM01   | DFHAID, DFHBMSCA   |
| COACTVWC   | COCOM01Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y | COACTVW | DFHAID, DFHBMSCA |
| COACTUPC   | COCOM01Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSETATY, CSSTRPFY, CSUTLDPY, CSUTLDWY, CODATECN, CSLKPCDY | COACTUP | DFHAID, DFHBMSCA |
| COCRDLIC   | COCOM01Y, CVCRD01Y, CVACT02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CSSTRPFY | COCRDLI | DFHAID, DFHBMSCA |
| COCRDSLC   | COCOM01Y, CVCRD01Y, CVACT02Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y | COCRDSL | DFHAID, DFHBMSCA |
| COCRDUPC   | COCOM01Y, CVCRD01Y, CVACT02Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSETATY, CSUTLDPY, CSUTLDWY, CODATECN | COCRDUP | DFHAID, DFHBMSCA |
| COTRN00C   | COCOM01Y, CVTRA05Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | COTRN00 | DFHAID, DFHBMSCA |
| COTRN01C   | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | COTRN01 | DFHAID, DFHBMSCA |
| COTRN02C   | COCOM01Y, CVTRA05Y, CVACT02Y, CVACT03Y, CVTRA03Y, CVTRA04Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CSSETATY | COTRN02 | DFHAID, DFHBMSCA |
| CORPT00C   | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y       | CORPT00       | DFHAID, DFHBMSCA   |
| COBIL00C   | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CSSETATY | COBIL00 | DFHAID, DFHBMSCA |
| COUSR00C   | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y       | COUSR00       | DFHAID, DFHBMSCA   |
| COUSR01C   | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y       | COUSR01       | DFHAID, DFHBMSCA   |
| COUSR02C   | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y       | COUSR02       | DFHAID, DFHBMSCA   |
| COUSR03C   | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y       | COUSR03       | DFHAID, DFHBMSCA   |

### Batch Programs

| Program    | Data Copybooks                                                     |
|------------|---------------------------------------------------------------------|
| CBACT01C   | CVACT01Y                                                           |
| CBACT02C   | CVACT02Y                                                           |
| CBACT03C   | CVACT03Y                                                           |
| CBACT04C   | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y                 |
| CBCUS01C   | CVCUS01Y                                                           |
| CBTRN01C   | CVTRA06Y                                                           |
| CBTRN02C   | CVTRA05Y, CVTRA06Y, CVACT03Y, CVTRA01Y                            |
| CBTRN03C   | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y                 |
| CBSTM03A   | COSTM01, CVACT01Y, CVCUS01Y, CVACT03Y                             |
| CBSTM03B   | _(uses linkage section from CBSTM03A)_                             |
| CBEXPORT   | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y, CVEXPORT       |
| CBIMPORT   | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT       |
| CSUTLDTC   | _(self-contained date utility)_                                     |

---

## 4. Program-to-VSAM File Access Matrix

### Online Programs (CICS File Access)

| Program    | USRSEC | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | TCATBAL | DISCGRP | TRANTYPE | TRANCATG |
|------------|:------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:-------:|:--------:|:--------:|
| COSGN00C   |   R    |          |          |          |          |          |         |         |          |          |
| COMEN01C   |        |          |          |          |          |          |         |         |          |          |
| COADM01C   |        |          |          |          |          |          |         |         |          |          |
| COACTVWC   |        |    R     |    R     |    R     |    R     |          |         |         |          |          |
| COACTUPC   |        |   R/W    |   R/W    |    R     |   R/W    |          |         |         |          |          |
| COCRDLIC   |        |          |    R     |          |          |          |         |         |          |          |
| COCRDSLC   |        |          |    R     |    R     |          |          |         |         |          |          |
| COCRDUPC   |        |          |   R/W    |    R     |          |          |         |         |          |          |
| COTRN00C   |        |          |          |    R     |          |    R     |         |         |          |          |
| COTRN01C   |        |          |          |          |          |    R     |         |         |          |          |
| COTRN02C   |        |          |    R     |    R     |          |   R/W    |         |         |    R     |    R     |
| CORPT00C   |        |          |          |          |          |          |         |         |          |          |
| COBIL00C   |        |   R/W    |          |    R     |          |   R/W    |         |         |          |          |
| COUSR00C   |   R    |          |          |          |          |          |         |         |          |          |
| COUSR01C   |   R/W  |          |          |          |          |          |         |         |          |          |
| COUSR02C   |   R/W  |          |          |          |          |          |         |         |          |          |
| COUSR03C   |   R/W  |          |          |          |          |          |         |         |          |          |

**Legend:** R = Read, W = Write, R/W = Read and Write/Rewrite

### Batch Programs (Sequential/VSAM File Access)

| Program    | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | DALYTRAN | TCATBAL | DISCGRP | TRANTYPE | TRANCATG | REPTFILE | DATEPARM | STMTFILE | HTMLFILE |
|------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:-------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CBACT01C   |    R     |          |          |          |          |          |         |         |          |          |          |          |          |          |
| CBACT02C   |          |    R     |          |          |          |          |         |         |          |          |          |          |          |          |
| CBACT03C   |          |          |    R     |          |          |          |         |         |          |          |          |          |          |          |
| CBACT04C   |   R/W    |          |    R     |          |     W    |          |    R    |    R    |          |          |          |          |          |          |
| CBCUS01C   |          |          |          |    R     |          |          |         |         |          |          |          |          |          |          |
| CBTRN01C   |          |          |          |          |          |    R     |         |         |          |          |          |          |          |          |
| CBTRN02C   |          |          |    R     |          |     W    |    R     |    R/W  |         |          |          |          |          |          |          |
| CBTRN03C   |          |          |    R     |          |     R    |          |         |         |    R     |    R     |     W    |    R     |          |          |
| CBSTM03A   |    R     |          |    R     |    R     |          |          |         |         |          |          |          |          |    W     |    W     |
| CBSTM03B   |    R     |          |    R     |    R     |     R    |          |         |         |          |          |          |          |    W     |    W     |
| CBEXPORT   |    R     |    R     |    R     |    R     |     R    |          |         |         |          |          |          |          |          |          |
| CBIMPORT   |    W     |    W     |    W     |    W     |     W    |          |         |         |          |          |          |          |          |          |

---

## 5. JCL Job-to-Program Mapping

| JCL Job     | Programs Executed                 | Utilities Used                      |
|-------------|-----------------------------------|-------------------------------------|
| POSTTRAN    | CBTRN02C                          | --                                  |
| INTCALC     | CBACT04C                          | --                                  |
| CREASTMT    | CBSTM03A (calls CBSTM03B)        | SORT, IDCAMS                        |
| TRANREPT    | CBTRN03C                          | --                                  |
| READACCT    | CBACT01C                          | --                                  |
| READCARD    | CBACT02C                          | --                                  |
| READCUST    | CBCUS01C                          | --                                  |
| READXREF    | CBACT03C                          | --                                  |
| CBEXPORT    | CBEXPORT                          | --                                  |
| CBIMPORT    | CBIMPORT                          | --                                  |
| WAITSTEP    | COBSWAIT                          | --                                  |
| ACCTFILE    | --                                | IDCAMS (REPRO)                      |
| CARDFILE    | --                                | IDCAMS (REPRO)                      |
| CUSTFILE    | --                                | IDCAMS (REPRO)                      |
| XREFFILE    | --                                | IDCAMS (DEFINE/REPRO/BLDINDEX)      |
| TRANFILE    | --                                | IDCAMS (REPRO)                      |
| DUSRSECJ    | --                                | IDCAMS (DEFINE/REPRO)               |
| DEFCUST     | --                                | IDCAMS (DEFINE)                     |
| COMBTRAN    | --                                | SORT, IDCAMS                        |
| TRANBKP     | --                                | IDCAMS (REPRO)                      |
| TRANIDX     | --                                | IDCAMS (DEFINE AIX/PATH/BLDINDEX)   |
| CLOSEFIL    | --                                | DFHCSDUP                            |
| OPENFIL     | --                                | DFHCSDUP                            |
| DEFGDGB     | --                                | IDCAMS (DEFINE GDG)                 |
| DEFGDGD     | --                                | IDCAMS (DEFINE GDG)                 |
| DISCGRP     | --                                | IDCAMS (DEFINE/REPRO)               |
| ESDSRRDS    | --                                | IDCAMS (DEFINE ESDS/RRDS)           |
| TCATBALF    | --                                | IDCAMS (DEFINE/REPRO)               |
| TRANCATG    | --                                | IDCAMS (DEFINE/REPRO)               |
| TRANTYPE    | --                                | IDCAMS (DEFINE/REPRO)               |
| REPTFILE    | --                                | IDCAMS (DEFINE)                     |
| DALYREJS    | --                                | IDCAMS (DEFINE)                     |
| PRTCATBL    | --                                | IDCAMS (PRINT)                      |
| TXT2PDF1    | --                                | TXT2PDF (REXX), IKJEFT1B           |
| FTPJCL      | --                                | FTP                                 |
| INTRDRJ1    | --                                | IDCAMS, IEBGENER (Internal Reader)  |
| INTRDRJ2    | --                                | IDCAMS                              |
| CBADMCDJ    | --                                | IDCAMS                              |

---

## 6. JCL Job Data Lineage

### Data Flow: Which Jobs Read/Write Which VSAM Files

```
LEGEND:  ──R──▶ = Read    ══W══▶ = Write    ──D──▶ = Define/Delete

                    ┌─────────────────────────────────────────────┐
                    │           VSAM DATA STORES                  │
                    │                                             │
  ACCTFILE ══W══▶   │  ACCTDATA.VSAM.KSDS (Account Master)      │ ◀──R── READACCT
  CBIMPORT ══W══▶   │                                            │ ◀──R── CBEXPORT
                    │                                             │ ◀──R── CBACT04C (INTCALC)
                    │                                             │ ◀──R── CBSTM03A (CREASTMT)
                    ├─────────────────────────────────────────────┤
  CARDFILE ══W══▶   │  CARDDATA.VSAM.KSDS (Card Master)         │ ◀──R── READCARD
  CBIMPORT ══W══▶   │                                            │ ◀──R── CBEXPORT
                    ├─────────────────────────────────────────────┤
  XREFFILE ══W══▶   │  CARDXREF.VSAM.KSDS (Card Cross-Ref)     │ ◀──R── READXREF
  CBIMPORT ══W══▶   │                                            │ ◀──R── CBEXPORT
                    │                                             │ ◀──R── CBACT04C, CBTRN02C
                    │                                             │ ◀──R── CBTRN03C, CBSTM03A
                    ├─────────────────────────────────────────────┤
  CUSTFILE ══W══▶   │  CUSTDATA.VSAM.KSDS (Customer Master)     │ ◀──R── READCUST
  CBIMPORT ══W══▶   │                                            │ ◀──R── CBEXPORT
  DEFCUST  ──D──▶   │                                            │ ◀──R── CBSTM03A
                    ├─────────────────────────────────────────────┤
  TRANFILE ══W══▶   │  TRANSACT.VSAM.KSDS (Transaction Master)  │ ◀──R── CBEXPORT
  CBIMPORT ══W══▶   │                                            │ ◀──R── CBTRN03C
  COMBTRAN ══W══▶   │                                            │ ◀──R── CREASTMT (via SORT)
  CBTRN02C ══W══▶   │                                            │
  CBACT04C ══W══▶   │                                            │
                    ├─────────────────────────────────────────────┤
  (online)  ══W══▶  │  DALYTRAN (Daily Transactions)             │ ◀──R── CBTRN01C
                    │                                             │ ◀──R── CBTRN02C (POSTTRAN)
                    ├─────────────────────────────────────────────┤
  DUSRSECJ ══W══▶   │  USRSEC.VSAM.KSDS (User Security)        │
                    ├─────────────────────────────────────────────┤
  TCATBALF ══W══▶   │  TCATBAL.VSAM.KSDS (Category Balance)    │ ◀──R── CBACT04C
                    │                                             │ ◀─R/W─ CBTRN02C
                    ├─────────────────────────────────────────────┤
  DISCGRP  ══W══▶   │  DISCGRP.VSAM.KSDS (Disclosure Group)    │ ◀──R── CBACT04C
                    ├─────────────────────────────────────────────┤
  TRANTYPE ══W══▶   │  TRANTYPE.VSAM.KSDS (Transaction Type)   │ ◀──R── CBTRN03C
                    ├─────────────────────────────────────────────┤
  TRANCATG ══W══▶   │  TRANCATG.VSAM.KSDS (Transaction Cat)    │ ◀──R── CBTRN03C
                    └─────────────────────────────────────────────┘
```

### Output File Lineage

```
CBTRN03C (TRANREPT) ══W══▶  REPTFILE (Daily Transaction Report)
CBSTM03A (CREASTMT) ══W══▶  STATEMNT.PS (Plain Text Statements)
CBSTM03A (CREASTMT) ══W══▶  STATEMNT.HTML (HTML Statements)
TXT2PDF1            ──R──▶  STATEMNT.PS ══W══▶ STATEMNT.PS.PDF
TRANBKP             ──R──▶  TRANSACT ══W══▶ TRANSACT.BACKUP (GDG)
```

---

## 7. Batch Processing Sequence

The nightly batch cycle follows a strict ordering enforced by the job scheduler:

```
Phase 1: PREPARE
  ┌──────────┐
  │ CLOSEFIL │  Close CICS files to allow batch exclusive access
  └────┬─────┘
       │
Phase 2: DATA REFRESH (parallel within phase)
  ┌────▼─────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ ACCTFILE │  │ CARDFILE │  │ CUSTFILE │  │ XREFFILE │  │ TRANFILE │
  │(Refresh  │  │(Refresh  │  │(Refresh  │  │(Refresh  │  │(Refresh  │
  │ Accounts)│  │ Cards)   │  │ Customer)│  │ XRef)    │  │ Trans)   │
  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
       │              │             │              │              │
       └──────────────┴─────────────┴──────────────┴──────────────┘
                                    │
Phase 3: CORE PROCESSING (sequential)
  ┌────────────────────▼──────────────────────┐
  │ POSTTRAN (CBTRN02C)                       │
  │ Post daily transactions to master file    │
  │ R: DALYTRAN, CARDXREF, TCATBAL           │
  │ W: TRANSACT, TCATBAL                     │
  └────────────────────┬──────────────────────┘
                       │
  ┌────────────────────▼──────────────────────┐
  │ INTCALC (CBACT04C)                        │
  │ Calculate and post interest charges       │
  │ R: TCATBAL, CARDXREF, DISCGRP, ACCTDATA  │
  │ W: TRANSACT, ACCTDATA                    │
  └────────────────────┬──────────────────────┘
                       │
Phase 4: BACKUP & REPORTING (can run in parallel)
  ┌────────────────────▼─────┐  ┌──────────────────────────┐
  │ TRANBKP                  │  │ COMBTRAN                  │
  │ Backup transaction file  │  │ Combine daily + master    │
  └────────────────────┬─────┘  └────────────┬─────────────┘
                       │                      │
  ┌────────────────────▼─────────────────────▼──┐
  │ CREASTMT (CBSTM03A + CBSTM03B)              │
  │ Generate account statements (text + HTML)    │
  │ R: TRANSACT (sorted), CARDXREF, ACCTDATA,   │
  │    CUSTDATA                                  │
  │ W: STATEMNT.PS, STATEMNT.HTML               │
  └────────────────────┬────────────────────────┘
                       │
  ┌────────────────────▼──────────────────────┐
  │ TRANREPT (CBTRN03C)                       │
  │ Generate daily transaction report         │
  │ R: TRANSACT, CARDXREF, TRANTYPE, TRANCATG│
  │ W: REPTFILE                               │
  └────────────────────┬──────────────────────┘
                       │
Phase 5: INDEX & REOPEN
  ┌────────────────────▼──────────────────────┐
  │ TRANIDX                                    │
  │ Rebuild alternate indexes on TRANSACT     │
  └────────────────────┬──────────────────────┘
                       │
  ┌────────────────────▼──────────────────────┐
  │ OPENFIL                                    │
  │ Reopen CICS files for online access       │
  └───────────────────────────────────────────┘
```

---

## 8. BMS Map-to-Program Bindings

Each BMS map has a 1:1 relationship with its controlling COBOL program:

| BMS Map   | COBOL Program | SEND MAP | RECEIVE MAP | # Fields |
|-----------|--------------|:--------:|:-----------:|----------|
| COSGN00   | COSGN00C     |    Yes   |     Yes     | ~8       |
| COMEN01   | COMEN01C     |    Yes   |     Yes     | ~12      |
| COADM01   | COADM01C     |    Yes   |     Yes     | ~8       |
| COACTVW   | COACTVWC     |    Yes   |     Yes     | ~20      |
| COACTUP   | COACTUPC     |    Yes   |     Yes     | ~30+     |
| COCRDLI   | COCRDLIC     |    Yes   |     Yes     | ~25      |
| COCRDSL   | COCRDSLC     |    Yes   |     Yes     | ~15      |
| COCRDUP   | COCRDUPC     |    Yes   |     Yes     | ~20      |
| COTRN00   | COTRN00C     |    Yes   |     Yes     | ~20      |
| COTRN01   | COTRN01C     |    Yes   |     Yes     | ~15      |
| COTRN02   | COTRN02C     |    Yes   |     Yes     | ~20      |
| CORPT00   | CORPT00C     |    Yes   |     Yes     | ~10      |
| COBIL00   | COBIL00C     |    Yes   |     Yes     | ~12      |
| COUSR00   | COUSR00C     |    Yes   |     Yes     | ~15      |
| COUSR01   | COUSR01C     |    Yes   |     Yes     | ~8       |
| COUSR02   | COUSR02C     |    Yes   |     Yes     | ~8       |
| COUSR03   | COUSR03C     |    Yes   |     Yes     | ~8       |

---

## 9. Shared Copybook Usage Matrix

How many programs depend on each copybook (higher = more critical to get right during migration):

| Copybook    | # Online Users | # Batch Users | Total Users | Criticality |
|-------------|:--------------:|:------------:|:-----------:|:-----------:|
| COCOM01Y    |       17       |       0      |      17     | **Critical**|
| COTTL01Y    |       17       |       0      |      17     | **Critical**|
| CSDAT01Y    |       17       |       0      |      17     | **Critical**|
| CSMSG01Y    |       17       |       0      |      17     | **Critical**|
| CSUSR01Y    |       17       |       0      |      17     | **Critical**|
| DFHAID      |       17       |       0      |      17     | **Critical**|
| DFHBMSCA    |       17       |       0      |      17     | **Critical**|
| CVACT03Y    |        6       |       5      |      11     | **High**    |
| CVACT01Y    |        3       |       4      |       7     | **High**    |
| CVACT02Y    |        5       |       2      |       7     | **High**    |
| CVTRA05Y    |        4       |       4      |       8     | **High**    |
| CVCUS01Y    |        2       |       3      |       5     | Medium      |
| CVCRD01Y    |        4       |       0      |       4     | Medium      |
| CSMSG02Y    |        4       |       0      |       4     | Medium      |
| CSSETATY    |        4       |       0      |       4     | Medium      |
| CVTRA01Y    |        0       |       2      |       2     | Medium      |
| CVTRA02Y    |        0       |       1      |       1     | Low         |
| CVTRA03Y    |        1       |       1      |       2     | Low         |
| CVTRA04Y    |        1       |       1      |       2     | Low         |
| CVTRA06Y    |        0       |       2      |       2     | Low         |
| CVTRA07Y    |        0       |       1      |       1     | Low         |
| CVEXPORT    |        0       |       2      |       2     | Low         |
| COSTM01     |        0       |       1      |       1     | Low         |
| UNUSED1Y    |        0       |       0      |       0     | None        |

---

## 10. Cross-Module Dependencies (Optional Modules)

### Authorization Module (IMS/DB2/MQ)

```
COPAUA0C ──MQ──▶ Authorization Queue ──▶ Decision
    │
    ├──IMS DB──▶ Authorization IMS Database
    │
COPAUS0C ──IMS──▶ Authorization IMS Database (summary read)
COPAUS1C ──IMS──▶ Authorization IMS Database (detail read)
COPAUS2C ──DB2──▶ Authorization DB2 Table (fraud marking)
CBPAUP0C ──IMS──▶ Authorization IMS Database (purge expired)
```

### Transaction Type DB2 Module

```
COTRTUPC ──DB2──▶ TRANSACTION_TYPE table (INSERT/UPDATE)
COTRTLIC ──DB2──▶ TRANSACTION_TYPE table (SELECT/DELETE, cursor-based)
COBTUPDT ──DB2──▶ TRANSACTION_TYPE table (batch UPDATE)
```

### VSAM-MQ Module

```
CODATE01 ──MQ──▶ CDRD Queue ──▶ System Date Response
COACCT01 ──MQ──▶ CDRA Queue ──▶ Account Inquiry Response
    │
    └──VSAM──▶ ACCTDATA.VSAM.KSDS (read account data)
```

### Integration Points with Core System

| Optional Module Program | Core VSAM Files Accessed   | Integration Mechanism |
|------------------------|----------------------------|-----------------------|
| COACCT01               | ACCTDATA.VSAM.KSDS        | Direct VSAM read      |
| COPAUA0C               | _(via IMS DB)_             | MQ trigger            |
| COPAUS2C               | _(via DB2)_                | DB2 SQL               |
| COTRTUPC/COTRTLIC      | _(replaces TRANTYPE VSAM)_ | DB2 SQL               |
