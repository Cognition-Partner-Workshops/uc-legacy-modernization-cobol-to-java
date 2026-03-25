# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This document maps the **call graph** (which programs invoke which), **data lineage** (which
> programs and jobs read/write which VSAM files), and **screen navigation flow**.

---

## 1. Program-to-Program Call Graph

### 1.1 Online CICS — XCTL (Transfer Control) Calls

The CICS online programs navigate between each other using `EXEC CICS XCTL`. The sign-on
program is the entry point; from there, users navigate via menus.

```
COSGN00C (Sign-On, CC00)
  │
  ├─XCTL→ COMEN01C (Main Menu, CM00)          [Regular Users]
  │         ├─XCTL→ COACTVWC (Account View)
  │         ├─XCTL→ COACTUPC (Account Update)
  │         ├─XCTL→ COCRDLIC (Card List)
  │         │         ├─XCTL→ COCRDSLC (Card Detail)
  │         │         └─XCTL→ COCRDUPC (Card Update)
  │         ├─XCTL→ COTRN00C (Transaction List)
  │         │         └─XCTL→ COTRN01C (Transaction Detail)
  │         ├─XCTL→ COTRN02C (Transaction Add)
  │         ├─XCTL→ COBIL00C (Bill Payment)
  │         └─XCTL→ CORPT00C (Report Selector)
  │
  └─XCTL→ COADM01C (Admin Menu, CA00)         [Admin Users]
            ├─XCTL→ COUSR00C (User List)
            ├─XCTL→ COUSR01C (User Add)
            ├─XCTL→ COUSR02C (User Update)
            └─XCTL→ COUSR03C (User Delete)
```

### 1.2 Batch — CALL Statements

```
CBACT01C ──CALL→ COBDATFT (ASM: date formatting)
           CALL→ CEE3ABD  (LE: abnormal end)

CBACT02C ──CALL→ CEE3ABD
CBACT03C ──CALL→ CEE3ABD
CBACT04C ──CALL→ CEE3ABD

CBCUS01C ──CALL→ CEE3ABD

CBTRN01C ──CALL→ CEE3ABD
CBTRN02C ──CALL→ CEE3ABD
CBTRN03C ──CALL→ CEE3ABD

CBSTM03A ──CALL→ CBSTM03B (Statement I/O sub-program, 11 calls)
           CALL→ CEE3ABD

CBEXPORT ──CALL→ CEE3ABD
CBIMPORT ──CALL→ CEE3ABD

COBSWAIT ──CALL→ MVSWAIT  (ASM: wait/delay)

CSUTLDTC ──CALL→ CEEDAYS  (LE: date conversion)
```

### 1.3 Optional Module Calls

```
COPAUA0C ──CALL→ MQOPEN, MQGET, MQPUT1, MQCLOSE  (MQ Series API)
COPAUS1C ──EXEC CICS LINK→ (sub-transaction)

COACCT01 ──CALL→ MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ Series API)
CODATE01 ──CALL→ MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ Series API)

COPAUS2C ──EXEC SQL (DB2: fraud marking)
COTRTLIC ──EXEC SQL (DB2: transaction type CRUD)
COTRTUPC ──EXEC SQL (DB2: transaction type add/edit)
COBTUPDT ──EXEC SQL (DB2: batch transaction type update)
```

---

## 2. JCL Job → Program Mapping

| JCL Job    | Step     | Program Executed     | Purpose                               |
|------------|----------|----------------------|---------------------------------------|
| POSTTRAN   | STEP010  | CBTRN01C             | Validate daily transactions           |
| POSTTRAN   | STEP020  | CBTRN02C             | Post validated transactions           |
| INTCALC    | STEP010  | CBACT04C             | Calculate interest and fees           |
| CREASTMT   | STEP040  | CBSTM03A (→CBSTM03B)| Generate statements                   |
| TRANREPT   | STEP010  | CBTRN03C             | Generate transaction report           |
| READACCT   | STEP010  | CBACT01C             | Read/display account records          |
| READCARD   | STEP010  | CBACT02C             | Read/display card records             |
| READCUST   | STEP010  | CBCUS01C             | Read/display customer records         |
| READXREF   | STEP010  | CBACT03C             | Read/display xref records             |
| CBEXPORT   | STEP010  | CBEXPORT             | Export all data                       |
| CBIMPORT   | STEP010  | CBIMPORT             | Import data                           |
| WAITSTEP   | STEP010  | COBSWAIT             | Wait/delay step                       |
| COMBTRAN   | STEP*    | SORT, IDCAMS         | Merge daily + master transactions     |
| CLOSEFIL   | STEP*    | DFHCSDUP             | Close CICS files                      |
| OPENFIL    | STEP*    | DFHCSDUP             | Open CICS files                       |
| TXT2PDF1   | TXT2PDF  | TXT2PDF (REXX)       | Convert statement text to PDF         |

All other JCL jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ, DEF*, etc.)
execute **IDCAMS** for VSAM define/delete/repro operations.

---

## 3. Data Lineage — VSAM File Access Matrix

### 3.1 Online Programs (CICS)

| VSAM File (Logical)        | Dataset (Physical)                    | R | W | U | D | Programs                                  |
|----------------------------|---------------------------------------|:-:|:-:|:-:|:-:|-------------------------------------------|
| USRSEC                     | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS     | R | W |RW | D | COSGN00C(R), COUSR00C(R), COUSR01C(W), COUSR02C(RW), COUSR03C(RD) |
| ACCTDAT                    | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS   | R |   | U |   | COACTVWC(R), COACTUPC(RU), COBIL00C(RU), COTRN02C(R) |
| CARDDAT / CARDAIX          | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS   | R |   | U |   | COCRDLIC(R), COCRDSLC(R), COCRDUPC(RU)    |
| CARDXREF / CXACAIX         | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS   | R |   |   |   | COACTVWC(R), COACTUPC(R), COTRN00C(R), COTRN02C(R), COBIL00C(R) |
| CUSTDAT                    | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS   | R |   |   |   | COACTVWC(R), COACTUPC(R), COCRDSLC(R), COCRDUPC(R) |
| TRANSACT                   | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS   | R | W |   |   | COTRN00C(R), COTRN01C(R), COTRN02C(RW), COBIL00C(RW), CORPT00C(R) |

> **Legend:** R = Read, W = Write (new records), U = Update (rewrite), D = Delete

### 3.2 Batch Programs

| VSAM File (Logical)        | Dataset (Physical)                    | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|----------------------------|---------------------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| ACCTDAT                    | ACCTDATA.VSAM.KSDS                    | **R**    |          |          | **RU**   |          | **R**    | **RU**   |          | **R**    | **R**    | **W**    |
| CARDDAT                    | CARDDATA.VSAM.KSDS                    |          | **R**    |          |          |          | **R**    |          |          |          | **R**    | **W**    |
| CARDXREF                   | CARDXREF.VSAM.KSDS                    |          |          | **R**    | **R**    |          | **R**    | **R**    | **R**    | **R**    | **R**    | **W**    |
| CUSTDAT                    | CUSTDATA.VSAM.KSDS                    |          |          |          |          | **R**    | **R**    |          |          | **R**    | **R**    | **W**    |
| TRANSACT                   | TRANSACT.VSAM.KSDS                    |          |          |          | **R**    |          |          | **W**    | **R**    | **R**    | **R**    | **W**    |
| DALYTRAN                   | DALYTRAN.VSAM.KSDS                    |          |          |          |          |          | **R**    | **R**    |          |          |          |          |
| TCATBALF                   | TCATBALF.VSAM.KSDS                    |          |          |          | **R**    |          |          | **RW**   |          |          |          |          |
| DISCGRP                    | DISCGRP.VSAM.KSDS                     |          |          |          | **R**    |          |          |          |          |          |          |          |
| DALYREJS                   | DALYREJS (sequential)                 |          |          |          |          |          |          | **W**    |          |          |          |          |
| TRANTYPE                   | TRANTYPE.VSAM.KSDS                    |          |          |          |          |          |          |          | **R**    |          |          |          |
| TRANCATG                   | TRANCATG.VSAM.KSDS                    |          |          |          |          |          |          |          | **R**    |          |          |          |
| DATEPARM                   | DATEPARM (sequential)                 |          |          |          |          |          |          |          | **R**    |          |          |          |
| REPTFILE                   | REPTFILE (sequential)                 |          |          |          |          |          |          |          | **W**    |          |          |          |
| STMTFILE                   | STATEMNT.PS (sequential)              |          |          |          |          |          |          |          |          | **W**    |          |          |
| HTMLFILE                   | STATEMNT.HTML (sequential)            |          |          |          |          |          |          |          |          | **W**    |          |          |
| OUTFILE/ARRFILE/VBRFILE    | Various output files                  | **W**    |          |          |          |          |          |          |          |          |          |          |
| EXPFILE                    | Export sequential file                |          |          |          |          |          |          |          |          |          | **W**    | **R**    |

### 3.3 JCL Job → File Flow (Data Lineage by Job)

```
                         ┌──────────────────────┐
                         │   Flat Files (PS)     │
                         │  USRSEC.PS            │
                         │  ACCTDATA.PS          │
                         │  CARDDATA.PS          │
                         │  CUSTDATA.PS          │
                         │  CARDXREF.PS          │
                         │  TRANSACT.PS          │
                         └─────────┬────────────┘
                                   │ IDCAMS REPRO
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     VSAM KSDS Files                                 │
│  ACCTDATA.VSAM.KSDS  │  CARDDATA.VSAM.KSDS  │  CUSTDATA.VSAM.KSDS │
│  CARDXREF.VSAM.KSDS  │  TRANSACT.VSAM.KSDS  │  USRSEC.VSAM.KSDS   │
│  TCATBALF.VSAM.KSDS  │  DISCGRP.VSAM.KSDS   │  DALYTRAN.VSAM.KSDS │
│  TRANTYPE.VSAM.KSDS  │  TRANCATG.VSAM.KSDS  │                      │
└──────────┬──────────────────────┬──────────────────────┬────────────┘
           │                      │                      │
    ┌──────▼──────┐       ┌───────▼───────┐      ┌──────▼──────┐
    │ POSTTRAN    │       │  INTCALC      │      │ CREASTMT    │
    │ CBTRN01C(R) │       │  CBACT04C     │      │ CBSTM03A/B  │
    │ CBTRN02C(RW)│       │  R: TCATBALF  │      │ R: TRANSACT │
    │ R: DALYTRAN │       │     CARDXREF  │      │    CARDXREF │
    │    CARDXREF │       │     DISCGRP   │      │    ACCTDATA │
    │    ACCTDAT  │       │     ACCTDATA  │      │    CUSTDATA │
    │ W: TRANSACT │       │     TRANSACT  │      │ W: STMTFILE │
    │    TCATBALF │       │  U: ACCTDATA  │      │    HTMLFILE │
    │    DALYREJS │       └───────────────┘      └─────────────┘
    └─────────────┘
           │
    ┌──────▼──────┐       ┌───────────────┐      ┌─────────────┐
    │ COMBTRAN    │       │  TRANREPT     │      │ TXT2PDF1    │
    │ SORT+IDCAMS │       │  CBTRN03C     │      │ REXX        │
    │ Merge daily │       │ R: TRANSACT   │      │ STMTFILE →  │
    │ + master    │       │    CARDXREF   │      │  PDF output │
    │ transactions│       │    TRANTYPE   │      └─────────────┘
    └─────────────┘       │    TRANCATG   │
                          │ W: REPTFILE   │
                          └───────────────┘
```

---

## 4. Screen Navigation Flow

```
┌────────────────┐
│  COSGN00C      │◄──── CICS Transaction CC00 (entry point)
│  Sign-On       │
└───────┬────────┘
        │ (validates USRSEC)
        ├──── User Type = 'U' ────►┌────────────────┐
        │                          │  COMEN01C      │
        │                          │  Main Menu     │
        │                          └───────┬────────┘
        │                                  │
        │              ┌───────┬───────┬───┼────┬───────┬───────┐
        │              ▼       ▼       ▼   ▼    ▼       ▼       ▼
        │          COACTVWC COACTUPC COCRDLIC COTRN00C COTRN02C COBIL00C CORPT00C
        │          AcctView AcctUpd  CardList TranList TranAdd  BillPay  Reports
        │                             │  │
        │                             ▼  ▼
        │                         COCRDSLC COCRDUPC     COTRN01C
        │                         CardView CardUpd      TranView
        │
        └──── User Type = 'A' ────►┌────────────────┐
                                   │  COADM01C      │
                                   │  Admin Menu    │
                                   └───────┬────────┘
                                           │
                               ┌───────┬───┼───┬───────┐
                               ▼       ▼       ▼       ▼
                           COUSR00C COUSR01C COUSR02C COUSR03C
                           UsrList  UsrAdd   UsrUpd   UsrDel
```

---

## 5. Copybook Dependency Matrix

Shows which copybooks are included (COPY) by which programs.

| Copybook     | Online Programs                                              | Batch Programs                                    |
|--------------|--------------------------------------------------------------|---------------------------------------------------|
| COCOM01Y     | All 17 online programs                                       | —                                                 |
| COTTL01Y     | All 17 online programs                                       | —                                                 |
| CSDAT01Y     | All 17 online programs                                       | —                                                 |
| CSMSG01Y     | All 17 online programs                                       | —                                                 |
| CSUSR01Y     | COSGN00C, COUSR00C–03C, COACTUPC, COACTVWC, +others         | —                                                 |
| DFHAID       | All 17 online programs                                       | —                                                 |
| DFHBMSCA     | All 17 online programs                                       | —                                                 |
| CVACT01Y     | COACTVWC, COACTUPC                                           | CBACT01C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT |
| CVACT02Y     | COCRDLIC, COCRDSLC, COCRDUPC                                 | CBACT02C, CBEXPORT, CBIMPORT                      |
| CVACT03Y     | COACTVWC, COACTUPC                                           | CBACT03C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT |
| CVCUS01Y     | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                      | CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT           |
| CVTRA05Y     | COTRN00C, COBIL00C                                           | CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT |
| CVTRA06Y     | —                                                            | CBTRN01C, CBTRN02C                                |
| CVTRA01Y     | —                                                            | CBACT04C, CBTRN02C                                |
| CVTRA02Y     | —                                                            | CBACT04C                                          |
| CVTRA03Y     | —                                                            | CBTRN03C                                          |
| CVTRA04Y     | —                                                            | CBTRN03C                                          |
| CVTRA07Y     | —                                                            | CBTRN03C                                          |
| CVEXPORT     | —                                                            | CBEXPORT, CBIMPORT                                |
| COSTM01      | —                                                            | CBSTM03A                                          |
| CUSTREC      | —                                                            | CBSTM03A                                          |
| CVCRD01Y     | COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC            | —                                                 |
| CSMSG02Y     | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COTRN02C            | —                                                 |
| CSSETATY     | COACTUPC, COCRDLIC, COCRDUPC                                 | —                                                 |
| CSSTRPFY     | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC            | —                                                 |
| CSUTLDWY     | COACTUPC, COCRDUPC                                           | —                                                 |
| CSLKPCDY     | COACTUPC                                                     | —                                                 |
| CODATECN     | —                                                            | CBACT01C                                          |

---

## 6. Cross-Cutting Concerns

### 6.1 Error Handling
- **Online:** EXEC CICS HANDLE ABEND → ABEND-ROUTINE → CSSTRPFY (send error text + return)
- **Batch:** PERFORM 9999-ABEND-PROGRAM / Z-ABEND-PROGRAM → CALL 'CEE3ABD' (Language Environment abnormal end)
- **I/O Status:** PERFORM 9910-DISPLAY-IO-STATUS / Z-DISPLAY-IO-STATUS (display file status codes)

### 6.2 Date Handling
- CSUTLDTC calls CEEDAYS (LE intrinsic) for Julian↔Gregorian conversion
- CBACT01C calls COBDATFT (assembler) for date formatting
- Online programs use EXEC CICS ASKTIME / FORMATTIME for current date/time

### 6.3 Security
- All online programs include CSUSR01Y and check user type from COMMAREA
- COSGN00C authenticates against USRSEC VSAM file
- Admin functions (COUSR00C–03C, COADM01C) restricted to SEC-USR-TYPE = 'A'
