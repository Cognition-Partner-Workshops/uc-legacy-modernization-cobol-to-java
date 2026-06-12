# CardDemo Dependency Map

> Call graph, data lineage, and program-to-file relationships extracted from static analysis of all COBOL programs, JCL jobs, and copybook includes.

---

## 1. Online Program Call Graph (CICS Navigation)

The online system uses `EXEC CICS XCTL` (transfer control) and `EXEC CICS RETURN TRANSID` for inter-program flow. All programs share state via the COMMAREA (COCOM01Y).

```
                           ┌─────────────┐
                           │  COSGN00C   │ CC00 - Sign-on
                           │  (Auth)     │
                           └──────┬──────┘
                            XCTL  │  reads USRSEC → validates credentials
                                  ▼
                    ┌─────────────────────────────┐
                    │         COMEN01C             │ CM00 - Main Menu
                    │   (Regular User routing)     │
                    └──┬──┬──┬──┬──┬──┬──┬──┬──┬──┘
                       │  │  │  │  │  │  │  │  │
          ┌────────────┘  │  │  │  │  │  │  │  └──────────────┐
          ▼               │  │  │  │  │  │  │                 ▼
     COACTVWC (CAVW)      │  │  │  │  │  │  │          COPAUS0C (CPVS)
     Account View         │  │  │  │  │  │  │          Pend Auth Summary
          │               │  │  │  │  │  │  │               │
          ▼               ▼  │  │  │  │  │  ▼               ▼
     COACTUPC (CAUP)  COCRDLIC│  │  │  │  │ COBIL00C   COPAUS1C (CPVD)
     Account Update   (CCLI)  │  │  │  │  │ (CB00)     Pend Auth Detail
                     Card List│  │  │  │  │ Bill Pay        │
                       │  │   │  │  │  │  │                 ▼
                       ▼  ▼   │  │  │  │  │          COPAUS2C
                  COCRDSLC │  │  │  │  │                Auth Sub-screen
                  (CCDL)   │  │  │  │  │
                  Card View│  │  │  │  │
                       │   │  │  │  │  │
                       ▼   │  │  │  │  │
                  COCRDUPC  │  │  │  │
                  (CCUP)    │  │  │  │
                  Card Update  │  │  │
                           │  │  │
          COTRN00C (CT00)──┘  │  │
          Transaction List    │  │
               │  │           │  │
               ▼  ▼           │  │
          COTRN01C COTRN02C   │  │
          (CT01)   (CT02)     │  │
          Tran View Tran Add  │  │
                              │  │
          CORPT00C (CR00)─────┘  │
          Transaction Reports    │
          (submits TRANREPT JCL) │
                                 │
                    ┌────────────┘
                    ▼
              COADM01C (CA00) ◄── Admin Menu
               │  │  │  │  │  │
               ▼  ▼  ▼  ▼  ▼  ▼
          COUSR00C COUSR01C COUSR02C COUSR03C  COTRTLIC  COTRTUPC
          (CU00)   (CU01)   (CU02)   (CU03)   (CTLI)    (CTTU)
          List     Add      Update   Delete    TranType  TranType
          Users    User     User     User      List(DB2) Edit(DB2)
```

### MQ-Triggered Programs (Asynchronous)

```
  External MQ Request ──► COPAUA0C (CP00)
                          Card Authorization Decision
                          │
                          ├── reads VSAM: ACCTDATA, CARDDATA, CARDXREF
                          ├── reads/writes IMS: Pending Auth DB
                          └── MQ: MQGET request, MQPUT1 response

  CDRD Transaction ──► CODATE01 (CDRD)
                       System Date via MQ
                       └── MQ request/response pattern

  CDRA Transaction ──► COACCT01 (CDRA)
                       Account Details via MQ
                       └── MQ request/response + VSAM read
```

---

## 2. Batch Program Call Graph

```
  CBSTM03A ───CALL───► CBSTM03B  (file I/O subroutine, called 13× per run)
  CBACT01C ───CALL───► COBDATFT  (ASM date format converter)
  COBSWAIT ───CALL───► MVSWAIT   (ASM timer wait)
  CORPT00C ───CALL───► CSUTLDTC  (date validation utility)
  COTRN02C ───CALL───► CSUTLDTC  (date validation utility)
  CBPAUP0C ──DL/I────► IMS DB    (GN, GNP, DLET, CHKP operations)
  COPAUA0C ──DL/I────► IMS DB    (SCHD, GU, ISRT, REPL, TERM)
  COPAUS0C ──DL/I────► IMS DB    (GN, GNP for browsing)
  COPAUS1C ──DL/I────► IMS DB    (GU, REPL)  + EXEC SQL (DB2 insert)
```

### Batch Error Handling

All batch programs (CB*) contain:
```
CALL 'CEE3ABD' USING ABCODE, TIMING.    ← LE abend handler
```

---

## 3. Copybook Inclusion Map

Shows which programs include which copybooks (data dependencies).

### Core Business Entity Copybooks

| Copybook | Used By Programs                                                                    |
|:---------|:------------------------------------------------------------------------------------|
| CVACT01Y | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, CBSTM03A, COBIL00C, COTRN02C, CBEXPORT, CBIMPORT, COPAUS0C |
| CVACT02Y | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBEXPORT, CBIMPORT, COPAUS0C    |
| CVACT03Y | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUS0C |
| CVCUS01Y | CBCUS01C, CBTRN01C, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT, COPAUS0C |
| CVTRA05Y | CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBEXPORT, CBIMPORT |
| CVTRA06Y | CBTRN01C, CBTRN02C                                                                  |
| CVTRA01Y | CBACT04C, CBTRN02C                                                                  |
| CVTRA02Y | CBACT04C                                                                             |
| CVTRA03Y | CBTRN03C                                                                             |
| CVTRA04Y | CBTRN03C                                                                             |
| CSUSR01Y | COSGN00C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C–03C, COPAUS0C, COTRTLIC |

### Shared Infrastructure Copybooks

| Copybook | Purpose                      | Used By (count)                |
|:---------|:-----------------------------|:-------------------------------|
| COCOM01Y | COMMAREA (program comms)     | All 17 core online programs    |
| COTTL01Y | Screen title constants       | All online programs            |
| CSDAT01Y | Date/time working storage    | All online + most batch        |
| CSMSG01Y | Common messages              | All online programs            |
| DFHAID   | CICS AID (key) definitions   | All online programs            |
| DFHBMSCA | BMS attribute constants      | All online programs            |
| CVCRD01Y | CC work areas / navigation   | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC |
| CSSTRPFY | String utility fields        | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSUTLDWY | Date validation WS           | COACTUPC                       |
| CSUTLDPY | Date validation procedures   | COACTUPC (via implicit COPY in PROCEDURE DIV) |
| CSLKPCDY | Lookup validation codes      | COACTUPC                       |
| CSSETATY | BMS attribute-setting macro  | COACTUPC (3× via COPY REPLACING) |

---

## 4. Data Lineage — File Access by Program

### VSAM File Access Matrix

| VSAM File (Logical) | Dataset Name                       | Read Programs                                           | Write/Update Programs                      |
|:---------------------|:-----------------------------------|:--------------------------------------------------------|:-------------------------------------------|
| ACCTDATA (Account)   | CARDDEMO.ACCTDATA                  | COACTVWC, COACTUPC, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, COPAUA0C, COPAUS0C | COACTUPC (REWRITE), CBACT04C (REWRITE), CBTRN02C (I-O) |
| CARDDATA (Card)      | CARDDEMO.CARDDATA                  | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, COPAUA0C | COCRDUPC (REWRITE)                         |
| CUSTDATA (Customer)  | CARDDEMO.CUSTDATA                  | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBSTM03A | —                                          |
| CARDXREF (XREF)      | CARDDEMO.CARDXREF                  | COACTVWC, COACTUPC, COBIL00C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C, COPAUA0C | —                                          |
| TRANSACT (Trans)     | CARDDEMO.TRANSACT.VSAM.KSDS       | COTRN00C, COTRN01C, CBTRN03C                           | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (OUTPUT), CBACT04C (OUTPUT) |
| DALYTRAN (Daily)     | CARDDEMO.DALYTRAN.PS               | CBTRN01C, CBTRN02C                                      | — (loaded via JCL)                         |
| USRSEC (Users)       | CARDDEMO.USRSEC                    | COSGN00C, COUSR00C                                      | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| TCATBALF             | CARDDEMO.TCATBALF                  | CBACT04C                                                | CBTRN02C (I-O)                             |
| DISCGRP              | CARDDEMO.DISCGRP                   | CBACT04C                                                | — (loaded via JCL)                         |
| TRANCATG             | CARDDEMO.TRANCATG                  | CBTRN03C                                                | — (loaded via JCL)                         |
| TRANTYPE             | CARDDEMO.TRANTYPE                  | CBTRN03C                                                | — (loaded via JCL)                         |

### DB2 Table Access (Optional Tran Type Module)

| Table     | Read Programs              | Write Programs             |
|:----------|:---------------------------|:---------------------------|
| TRTYP     | COTRTLIC (cursor/SELECT)   | COTRTUPC (INSERT/UPDATE), COTRTLIC (DELETE/UPDATE), COBTUPDT (INSERT/UPDATE/DELETE) |

### IMS Database Access (Optional Auth Module)

| Database/Segment | Read Programs                    | Write Programs                     |
|:-----------------|:---------------------------------|:-----------------------------------|
| PAUT (Auth Detail)| COPAUS0C (GN/GNP), COPAUA0C (GU)| COPAUA0C (ISRT/REPL), COPAUS1C (REPL), CBPAUP0C (DLET) |
| PASFL (Summary)   | COPAUS0C (GN/GNP)               | —                                  |
| PADFL (Default)    | COPAUA0C (SCHD)                 | —                                  |

### MQ Queue Access

| Queue           | Read (MQGET)  | Write (MQPUT1) |
|:----------------|:--------------|:---------------|
| Auth Request    | COPAUA0C      | External system|
| Auth Response   | External system| COPAUA0C      |
| Date Request    | CODATE01      | External       |
| Account Request | COACCT01      | External       |

---

## 5. JCL Job → Program → File Lineage

### Daily Batch Cycle (Control-M: DAILY-TransactionBackup)

```
CLOSEFIL ──► IEFBR14 (releases VSAM locks for batch)
    │
    ▼
TRANBKP ──► IDCAMS REPRO
    │         Input:  TRANSACT.VSAM.KSDS
    │         Output: TRANSACT.VSAM.KSDS.BKUP (GDG +1)
    ▼
WAITSTEP ──► COBSWAIT → MVSWAIT (timer pause)
    │
    ▼
OPENFIL ──► IEFBR14 (re-opens VSAM files for CICS)
```

### Monthly End-of-Month Cycle (Control-M: MONTHLY-EndOfMonth)

```
CLOSEFIL ──► (close VSAM)
    │
    ▼
POSTTRAN ──► CBTRN02C
    │         Input:  DALYTRAN.PS, CARDXREF, ACCTDATA
    │         Output: TRANSACT (new records), DALYREJS (rejects)
    │         Update: ACCTDATA (balance), TCATBALF (category balance)
    ▼
INTCALC ──► CBACT04C
    │         Input:  TCATBALF, CARDXREF, DISCGRP, ACCTDATA
    │         Output: TRANSACT (interest entries)
    │         Update: ACCTDATA (interest posted)
    ▼
TRANBKP ──► IDCAMS REPRO (backup transactions)
    │
    ▼
COMBTRAN ──► SORT
    │         Input:  TRANSACT + DALYTRAN
    │         Output: Combined transaction file
    ▼
CREASTMT ──► CBSTM03A → CBSTM03B
    │         Input:  TRANSACT, CARDXREF, CUSTDATA, ACCTDATA
    │         Output: Statement file (text), HTML file
    ▼
TRANIDX ──► IDCAMS
    │         Defines alternate index on TRANSACT
    ▼
OPENFIL ──► (reopen VSAM)
```

### Weekly Cycle (Control-M: WEEKLY-TransactionTypesDBRefresh)

```
CLOSEFIL ──► (close VSAM)
    │
    ▼
TRANEXTR ──► DSNTIAUL
    │         Input:  DB2 TRTYP table
    │         Output: Sequential extract files
    ▼
TRANCATG ──► IDCAMS REPRO
    │         Input:  Extract PS → VSAM TRANCATG
    ▼
TRANTYPE ──► IDCAMS REPRO
    │         Input:  Extract PS → VSAM TRANTYPE
    ▼
OPENFIL ──► (reopen VSAM)
```

### Initialization Sequence (One-time / Environment Setup)

```
DUSRSECJ ──► IEBGENER → Load USRSEC from PS
ACCTFILE ──► IDCAMS → Define + REPRO ACCTDATA VSAM
CARDFILE ──► IDCAMS → Define + REPRO CARDDATA VSAM
CUSTFILE ──► IDCAMS → Define + REPRO CUSTDATA VSAM
XREFFILE ──► IDCAMS → Define + REPRO CARDXREF VSAM (with AIX)
TRANFILE ──► IDCAMS → Define + REPRO TRANSACT VSAM
DISCGRP  ──► IDCAMS → Load DISCGRP VSAM
TCATBALF ──► IDCAMS → Load TCATBALF VSAM
TRANCATG ──► IDCAMS → Load TRANCATG VSAM
TRANTYPE ──► IDCAMS → Load TRANTYPE VSAM
DEFGDGB  ──► IDCAMS → Define GDG bases
```

---

## 6. Program-to-Program Transfer Matrix

### XCTL (Transfer Control) — caller yields control to target

| Source Program | Target Program(s)                                  | Trigger Condition        |
|:---------------|:---------------------------------------------------|:-------------------------|
| COSGN00C       | COMEN01C (regular), COADM01C (admin)               | Successful login         |
| COMEN01C       | COACTVWC, COACTUPC, COCRDLIC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | Menu option selection |
| COADM01C       | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | Admin menu selection |
| COCRDLIC       | COCRDSLC, COCRDUPC, COMEN01C                       | Card selection / back    |
| COUSR00C       | COUSR02C, COUSR03C, COADM01C                       | User selection / back    |
| COTRTLIC       | COTRTUPC, COADM01C                                 | Type selection / back    |

### CALL (Subroutine) — caller retains control

| Caller     | Callee       | Purpose                             |
|:-----------|:-------------|:------------------------------------|
| CBSTM03A   | CBSTM03B     | File I/O delegation (13 calls)      |
| CBACT01C   | COBDATFT     | Date format conversion (ASM)        |
| COBSWAIT   | MVSWAIT      | Timer wait (ASM)                    |
| CORPT00C   | CSUTLDTC     | Date validation (2 calls)           |
| COTRN02C   | CSUTLDTC     | Date validation (2 calls)           |
| COPAUA0C   | MQOPEN/MQGET/MQPUT1/MQCLOSE | MQ message operations |
| All batch  | CEE3ABD      | LE abend termination handler        |
| CSUTLDTC   | CEEDAYS      | LE intrinsic date conversion        |

---

## 7. Data Flow Summary Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    ONLINE (CICS)                            │
│                                                             │
│  Terminal ─► COSGN00C ─► COMEN01C ─► Business Programs     │
│                                       │                     │
│                    ┌──────────────────┘                     │
│                    ▼                                        │
│  VSAM Files:  ACCTDATA ◄──────────────────────┐            │
│               CARDDATA ◄──────────┐           │            │
│               CUSTDATA            │           │            │
│               CARDXREF            │           │            │
│               TRANSACT ◄──── COTRN02C (add)   │            │
│               USRSEC   ◄──── COUSR01C (add)   │            │
│                    │              │           │            │
│                    ▼              ▼           ▼            │
│  TDQ (JOBS) ◄── CORPT00C  COBIL00C  COACTUPC             │
│       │          (report)   (pay)    (update)              │
│       ▼                                                    │
│  Internal Reader → TRANREPT JCL                            │
└─────────────────────────────────────────────────────────────┘
                         │
                    CLOSEFIL / OPENFIL
                         │
┌─────────────────────────────────────────────────────────────┐
│                    BATCH (JCL)                              │
│                                                             │
│  DALYTRAN.PS ──► CBTRN02C (POSTTRAN) ──► TRANSACT          │
│                       │                    │                │
│                       ▼                    ▼                │
│              ACCTDATA (update)     CBTRN03C (TRANREPT)      │
│              TCATBALF (update)          │                   │
│                       │                 ▼                   │
│                       ▼           Report File               │
│              CBACT04C (INTCALC)                              │
│                       │                                     │
│                       ▼                                     │
│              TRANSACT (interest entries)                     │
│                       │                                     │
│                       ▼                                     │
│              CBSTM03A → CBSTM03B (CREASTMT)                 │
│                       │                                     │
│                       ▼                                     │
│              Statement File + HTML File                     │
└─────────────────────────────────────────────────────────────┘
```

---

*Generated from static analysis of the CardDemo COBOL codebase — CALL/XCTL patterns, COPY statements, EXEC CICS/SQL/DLI commands, and JCL DD statements.*
