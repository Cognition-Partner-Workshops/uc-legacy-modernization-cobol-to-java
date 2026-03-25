# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** Static analysis of EXEC CICS XCTL, CALL, COPY, and VSAM DATASET references
> **Scope:** All COBOL programs in `app/cbl/` and optional modules

---

## Table of Contents

1. [Online Program Call Graph](#online-program-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Program-to-Program Transfer Matrix](#program-to-program-transfer-matrix)
4. [Copybook Dependency Matrix](#copybook-dependency-matrix)
5. [VSAM File Access Map (Online)](#vsam-file-access-map-online)
6. [VSAM File Access Map (Batch)](#vsam-file-access-map-batch)
7. [JCL Job-to-Program Mapping](#jcl-job-to-program-mapping)
8. [JCL Job Data Lineage](#jcl-job-data-lineage)
9. [Batch Processing Data Flow](#batch-processing-data-flow)
10. [Shared Utility Dependencies](#shared-utility-dependencies)

---

## Online Program Call Graph

Programs transfer control via `EXEC CICS XCTL` (transfer control, no return) or dynamic program dispatch through COMMAREA fields.

```
                           ┌─────────────┐
                           │  COSGN00C   │  (CC00 - Sign-on)
                           │  Entry Point │
                           └──────┬──────┘
                                  │ XCTL (based on user type)
                    ┌─────────────┴─────────────┐
                    ▼                             ▼
             ┌─────────────┐              ┌─────────────┐
             │  COMEN01C   │              │  COADM01C   │
             │  User Menu  │              │  Admin Menu  │
             │  (CM00)     │              │  (CA00)      │
             └──────┬──────┘              └──────┬──────┘
                    │                             │
     ┌──────┬──────┼──────┬──────┐    ┌──────┬──┴──┬──────┐
     ▼      ▼      ▼      ▼      ▼    ▼      ▼     ▼      ▼
  COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C COUSR00C (+ user menu items)
  Acct View Card List Tran List Reports Bill Pay  User List
  (CAVW)   (CCLI)   (CT00)   (CR00)   (CB00)    (CU00)
     │        │        │                           │
     │    ┌───┴───┐    │                    ┌──────┼──────┐
     │    ▼       ▼    ▼                    ▼      ▼      ▼
     │ COCRDSLC COCRDUPC COTRN01C        COUSR01C COUSR02C COUSR03C
     │ Card View Card Upd Tran View      Add User Upd User Del User
     │ (CCDL)   (CCUP)   (CT01)         (CU01)   (CU02)   (CU03)
     │                       │
     │                       ▼
     │                    COTRN02C
     │                    Tran Add (CT02)
     │
     └─── Also navigates to: COCRDLIC, COCRDUPC (from account context)
```

### Navigation Rules

- **PF3 (Exit):** Returns to calling program (stored in `CDEMO-FROM-PROGRAM`)
- **Enter:** Processes current screen, may transfer to next program
- **Menu Selection:** XCTL to the program defined in menu option table (COMEN02Y / COADM02Y)
- **All online programs** return to their parent via COMMAREA-stored origin

---

## Batch Program Call Graph

Batch programs use `CALL` (with return) to invoke subprograms and utilities.

```
┌─────────────┐         ┌─────────────┐
│  CBSTM03A   │────────►│  CBSTM03B   │  (CALL 'CBSTM03B' - 12 call sites)
│  Statement  │         │  Stmt Sub   │
│  Generation │         │  (I/O helper)│
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  CBACT01C   │────────►│  COBDATFT   │  (CALL 'COBDATFT' - date formatting)
│  Acct List  │         │  (Assembler) │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  COBSWAIT   │────────►│  MVSWAIT    │  (CALL 'MVSWAIT' - delay utility)
│  Wait Util  │         │  (Assembler) │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  CORPT00C   │────────►│  CSUTLDTC   │  (CALL 'CSUTLDTC' - date validation)
│  Reports    │         │  Date Util   │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  COTRN02C   │────────►│  CSUTLDTC   │  (CALL 'CSUTLDTC' - date validation)
│  Tran Add   │         │  Date Util   │
└─────────────┘         └─────────────┘

All batch programs call:
  CEE3ABD ──► LE/370 Abend Service (system abend handler)
```

---

## Program-to-Program Transfer Matrix

### XCTL Transfers (Online)

| Source Program | Target Program(s) | Transfer Type | Condition |
|---|---|---|---|
| COSGN00C | COADM01C | XCTL | User type = Admin |
| COSGN00C | COMEN01C | XCTL | User type = Regular |
| COMEN01C | COSGN00C | XCTL | PF3 (Exit) |
| COMEN01C | *Dynamic* (via menu table) | XCTL | Menu option selected |
| COADM01C | *Dynamic* (via admin menu table) | XCTL | Admin option selected |
| COADM01C | COSGN00C | XCTL | PF3 (Exit) |
| COACTVWC | COMEN01C | XCTL | PF3 (Exit to menu) |
| COACTVWC | *CDEMO-FROM-PROGRAM* | XCTL | PF3 (Exit to caller) |
| COCRDLIC | COCRDSLC | XCTL | Select 'S' on card row |
| COCRDLIC | COCRDUPC | XCTL | Select 'U' on card row |
| COCRDLIC | COMEN01C | XCTL | PF3 (Exit to menu) |
| COCRDSLC | COCRDLIC | XCTL | PF3 (Return to list) |
| COCRDSLC | *CDEMO-FROM-PROGRAM* | XCTL | PF3 (Exit to caller) |
| COCRDUPC | COCRDLIC | XCTL | PF3 (Return to list) |
| COCRDUPC | *CDEMO-FROM-PROGRAM* | XCTL | PF3 (Exit to caller) |
| COTRN00C | *CDEMO-TO-PROGRAM* | XCTL | PF3 / Detail navigation |
| COTRN01C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| COTRN02C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| CORPT00C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| COBIL00C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| COUSR00C | *CDEMO-TO-PROGRAM* | XCTL | PF3 / Detail navigation |
| COUSR01C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| COUSR02C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |
| COUSR03C | *CDEMO-TO-PROGRAM* | XCTL | PF3 (Exit) |

### CALL Invocations (Batch / Utility)

| Caller | Callee | Mechanism | Frequency |
|---|---|---|---|
| CBSTM03A | CBSTM03B | CALL ... USING WS-M03B-AREA | 12 call sites (open, read, write, close) |
| CBACT01C | COBDATFT | CALL 'COBDATFT' USING CODATECN-REC | 1 call site (date format) |
| COBSWAIT | MVSWAIT | CALL 'MVSWAIT' USING MVSWAIT-TIME | 1 call site (wait) |
| CORPT00C | CSUTLDTC | CALL 'CSUTLDTC' USING CSUTLDTC-DATE | 2 call sites (date validation) |
| COTRN02C | CSUTLDTC | CALL 'CSUTLDTC' USING CSUTLDTC-DATE | 2 call sites (date validation) |
| CSUTLDTC | CEEDAYS | CALL 'CEEDAYS' (LE intrinsic) | 1 call site (date calculation) |
| All batch pgms | CEE3ABD | CALL 'CEE3ABD' | Abend handler (1 per program) |

---

## Copybook Dependency Matrix

Shows which copybooks are included (via COPY) by each program. Only non-IBM copybooks shown.

| Copybook | Used By Programs | Usage Count |
|---|---|---|
| **COCOM01Y** (COMMAREA) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **17** |
| **COTTL01Y** (Titles) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **16** |
| **CSDAT01Y** (Date) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **16** |
| **CSMSG01Y** (Messages) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **17** |
| **CSUSR01Y** (User Sec) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C | **12** |
| **CVACT01Y** (Account) | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBSTM03A, CBTRN02C, CBEXPORT, CBIMPORT | **10** |
| **CVACT02Y** (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT | **7** |
| **CVACT03Y** (Xref) | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBSTM03A, CBTRN02C, CBEXPORT, CBIMPORT | **9** |
| **CVCUS01Y** (Customer) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT | **8** |
| **CVTRA05Y** (Transaction) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | **10** |
| **CVTRA06Y** (Daily Tran) | CBTRN01C, CBTRN02C, CBTRN03C | **3** |
| **CVCRD01Y** (Card Work) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | **4** |
| **CSSTRPFY** (PF-Key) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | **5** |
| **CSMSG02Y** (Abend) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | **4** |
| **CSUTLDWY** (Date Edit) | COACTUPC | **1** |
| **COMEN02Y** (Menu Opts) | COMEN01C | **1** |
| **COADM02Y** (Admin Opts) | COADM01C | **1** |
| **CODATECN** (Date Conv) | CBACT01C | **1** |
| **COSTM01** (Stmt Tran) | CBSTM03A | **1** |
| **CVTRA07Y** (Report) | CBTRN03C | **1** |
| **CVEXPORT** (Export) | CBEXPORT, CBIMPORT | **2** |

---

## VSAM File Access Map (Online)

Shows which VSAM files are accessed by each online CICS program and the operation type.

| Program | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | CARDXREF | CXACAIX | TRANSACT |
|---|---|---|---|---|---|---|---|---|
| COSGN00C | R | | | | | | | |
| COACTVWC | | R | | R | R | | R | |
| COACTUPC | | RW | | | RW | | R | |
| COCRDLIC | | | R | R | | | | |
| COCRDSLC | | | R | R | R | | | |
| COCRDUPC | | | RW | R | R | | | |
| COTRN00C | | | | | | | | R |
| COTRN01C | | | | | | | | R |
| COTRN02C | | R | | | | R | R | RW |
| CORPT00C | | | | | | | | R |
| COBIL00C | | R | | | | R | | RW |
| COUSR00C | R | | | | | | | |
| COUSR01C | RW | | | | | | | |
| COUSR02C | RW | | | | | | | |
| COUSR03C | RW | | | | | | | |

**Legend:** R = Read, W = Write, RW = Read/Write (REWRITE)

---

## VSAM File Access Map (Batch)

| Program | ACCTDAT | CARDDAT | CUSTDAT | XREFFILE | TRANSACT | DALYTRAN | DALYREJS | TCATBALF | DISCGRP | TRANTYPE | TRANCATG | STMTFILE | HTMLFILE | EXPFILE |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CBACT01C | R | | | | | | | | | | | | | |
| CBACT02C | | R | | | | | | | | | | | | |
| CBACT03C | | | | R | | | | | | | | | | |
| CBACT04C | RW | | | | | | | RW | R | R | R | | | |
| CBCUS01C | | | R | | | | | | | | | | | |
| CBTRN01C | | | | | | R | | | | | | | | |
| CBTRN02C | RW | | | R | W | R | W | RW | | | | | | |
| CBTRN03C | | | | R | R | | | | | R | R | | | |
| CBSTM03A | R | | R | R | R* | | | | | | | W | W | |
| CBSTM03B | | | | | | | | | | | | | | |
| CBEXPORT | | R | R | R | R | | | | | | | | | W |
| CBIMPORT | | W | W | W | W | | | | | | | | | R** |

**Legend:** R = Read (OPEN INPUT), W = Write (OPEN OUTPUT), RW = Read/Write (OPEN I-O)
\* CBSTM03A reads transactions via CBSTM03B sub-calls
\** CBIMPORT reads from sequential import file

---

## JCL Job-to-Program Mapping

| JCL Job | COBOL Program(s) | Utility Programs | Purpose |
|---|---|---|---|
| POSTTRAN.jcl | CBTRN02C | — | Post daily transactions |
| INTCALC.jcl | CBACT04C | — | Calculate interest |
| CREASTMT.JCL | CBSTM03A (calls CBSTM03B) | IDCAMS | Generate statements |
| TRANREPT.jcl | CBTRN03C | REPROC proc | Transaction report |
| READACCT.jcl | CBACT01C | — | Print account data |
| READCARD.jcl | CBACT02C | — | Print card data |
| READCUST.jcl | CBCUS01C | — | Print customer data |
| READXREF.jcl | CBACT03C | — | Print cross-reference |
| CBEXPORT.jcl | CBEXPORT | — | Export all data |
| CBIMPORT.jcl | CBIMPORT | — | Import all data |
| WAITSTEP.jcl | COBSWAIT (calls MVSWAIT) | — | Delay utility |
| ACCTFILE.jcl | — | IDCAMS, SDSF | Refresh account VSAM |
| CARDFILE.jcl | — | IDCAMS, SDSF | Refresh card VSAM |
| CUSTFILE.jcl | — | IDCAMS, SDSF | Refresh customer VSAM |
| XREFFILE.jcl | — | IDCAMS | Refresh xref VSAM |
| TRANFILE.jcl | — | IDCAMS, SDSF | Refresh transaction VSAM |
| DUSRSECJ.jcl | — | IDCAMS | Load user security |
| CLOSEFIL.jcl | — | SDSF (CEMT) | Close CICS files |
| OPENFIL.jcl | — | SDSF (CEMT) | Open CICS files |
| COMBTRAN.jcl | — | SORT/DFSORT | Merge transactions |
| TRANBKP.jcl | — | IDCAMS REPRO | Backup transactions |
| TRANIDX.jcl | — | IDCAMS | Define alternate index |

---

## JCL Job Data Lineage

### Data Refresh Jobs (Input → VSAM)

```
ASCII Data Files (app/data/)
    │
    ├── custdata.txt ──► CUSTFILE.jcl ──► CUSTDAT (VSAM KSDS)
    ├── acctdata.txt ──► ACCTFILE.jcl ──► ACCTDAT (VSAM KSDS)
    ├── carddata.txt ──► CARDFILE.jcl ──► CARDDAT (VSAM KSDS)
    ├── cardxref.txt ──► XREFFILE.jcl ──► CARDXREF (VSAM KSDS) + CXACAIX (AIX)
    ├── trandet.txt  ──► TRANFILE.jcl ──► TRANSACT (VSAM KSDS)
    └── usrsec.txt   ──► DUSRSECJ.jcl ──► USRSEC (VSAM KSDS)
```

### Batch Processing Data Flow

```
DALYTRAN (Daily Transactions)
    │
    ▼
POSTTRAN.jcl (CBTRN02C)
    ├──► TRANSACT (posted transactions) [WRITE]
    ├──► DALYREJS (rejected transactions) [WRITE]
    ├──► ACCTDAT (update balances) [REWRITE]
    └──► TCATBALF (update category balances) [WRITE/REWRITE]
    
ACCTDAT + DISCGRP + TRANTYPE + TRANCATG + TCATBALF
    │
    ▼
INTCALC.jcl (CBACT04C)
    ├──► ACCTDAT (update with interest) [REWRITE]
    └──► TCATBALF (update interest balances) [REWRITE]

TRANSACT
    │
    ▼
TRANBKP.jcl (IDCAMS REPRO)
    └──► TRANSACT.BACKUP (GDG backup)

TRANSACT (multiple)
    │
    ▼
COMBTRAN.jcl (SORT)
    └──► TRANSACT (merged/sorted)

TRANSACT + XREFFILE + CUSTDAT + ACCTDAT
    │
    ▼
CREASTMT.JCL (CBSTM03A → CBSTM03B)
    ├──► STMTFILE (text statements) [WRITE]
    └──► HTMLFILE (HTML statements) [WRITE]

TRANSACT + XREFFILE + TRANTYPE + TRANCATG
    │
    ▼
TRANREPT.jcl (CBTRN03C via REPROC)
    └──► Report Output (SYSOUT)
```

### Export/Import Data Flow

```
EXPORT (CBEXPORT):
    CUSTDAT + ACCTDAT + CARDXREF + TRANSACT + CARDDAT
        │
        ▼
    EXPFILE (Sequential Export File)

IMPORT (CBIMPORT):
    IMPFILE (Sequential Import File)
        │
        ▼
    CUSTDAT + ACCTDAT + CARDXREF + TRANSACT + CARDDAT
```

---

## Batch Processing Data Flow

### End-of-Day Batch Cycle (Ordered)

```
Step 1: CLOSEFIL.jcl     ──► Close all CICS files (CEMT SET FIL CLO)
Step 2: Data Refresh Jobs  ──► Reload VSAM files from source data
Step 3: POSTTRAN.jcl      ──► Post daily transactions (CBTRN02C)
Step 4: INTCALC.jcl       ──► Calculate interest charges (CBACT04C)
Step 5: TRANBKP.jcl       ──► Backup transaction file
Step 6: COMBTRAN.jcl      ──► Merge/sort transaction file
Step 7: CREASTMT.JCL      ──► Generate customer statements (CBSTM03A)
Step 8: TRANIDX.jcl       ──► Rebuild alternate index
Step 9: OPENFIL.jcl       ──► Reopen CICS files (CEMT SET FIL OPE)
```

### Dependencies Between Steps

| Step | Depends On | Reason |
|---|---|---|
| All batch steps | CLOSEFIL | CICS files must be closed for exclusive batch access |
| POSTTRAN | Data refresh | Needs current DALYTRAN, XREFFILE, ACCTDAT |
| INTCALC | POSTTRAN | Needs updated account balances after posting |
| TRANBKP | POSTTRAN | Backup includes newly posted transactions |
| COMBTRAN | TRANBKP | Safe to merge after backup is taken |
| CREASTMT | COMBTRAN | Statements need final merged transaction data |
| TRANIDX | CREASTMT | Rebuild index after all transaction updates |
| OPENFIL | All above | CICS files reopened only after all batch completes |

---

## Shared Utility Dependencies

### Cross-Cutting Concerns

| Utility | Type | Consumers | Purpose |
|---|---|---|---|
| COCOM01Y | Copybook | All 17 online programs | COMMAREA navigation and state |
| COTTL01Y | Copybook | 16 online programs | Screen title constants |
| CSDAT01Y | Copybook | 16 online programs | Current date/time |
| CSMSG01Y | Copybook | 17 online + batch programs | Shared message constants |
| CSUSR01Y | Copybook | 12 online programs | User authentication data |
| CSUTLDTC | Program | CORPT00C, COTRN02C | Date validation service |
| CSSTRPFY | Copybook | 5 online programs | PF-key mapping utility |
| COBDATFT | ASM Program | CBACT01C | Date formatting |
| MVSWAIT | ASM Program | COBSWAIT | Programmable delay |
| CEE3ABD | LE Service | All batch programs | Abend/error handling |
| DFHAID | IBM Copybook | All online programs | AID key definitions |
| DFHBMSCA | IBM Copybook | All online programs | BMS attribute constants |
