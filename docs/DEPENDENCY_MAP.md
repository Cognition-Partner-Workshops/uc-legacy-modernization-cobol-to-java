# CardDemo Dependency Map

> **Source:** Static analysis of `app/cbl/`, `app/cpy/`, `app/jcl/`, `app/bms/`
> **Application:** AWS CardDemo — Mainframe Credit Card Management System

---

## Table of Contents

1. [Online Program Call Graph](#1-online-program-call-graph)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [Program → Copybook Dependency Matrix](#3-program--copybook-dependency-matrix)
4. [Program → VSAM File Access Map](#4-program--vsam-file-access-map)
5. [JCL Job → Program → File Data Lineage](#5-jcl-job--program--file-data-lineage)
6. [Batch Job Execution Sequence](#6-batch-job-execution-sequence)
7. [Menu Navigation Flow](#7-menu-navigation-flow)
8. [Shared Copybook Fan-Out](#8-shared-copybook-fan-out)

---

## 1. Online Program Call Graph

Programs transfer control via `EXEC CICS XCTL` (transfer, no return) or `EXEC CICS RETURN TRANSID` (pseudo-conversational return).

```
                           ┌─────────────────────────────────────────────────────────────┐
                           │                    CICS TRANSACTION ENTRY                    │
                           │                      (Terminal User)                         │
                           └────────────────────────────┬────────────────────────────────┘
                                                        │
                                                        ▼
                                                 ┌──────────────┐
                                                 │  COSGN00C    │
                                                 │  Sign-On     │
                                                 │  (CC00)      │
                                                 └──────┬───────┘
                                                        │
                                          ┌─────────────┴─────────────┐
                                          │ XCTL based on user type   │
                                          ▼                           ▼
                                  ┌──────────────┐           ┌──────────────┐
                                  │  COMEN01C    │           │  COADM01C    │
                                  │  Main Menu   │           │  Admin Menu  │
                                  │  (CM00)      │           │  (CA00)      │
                                  └──────┬───────┘           └──────┬───────┘
                                         │                          │
             ┌───────────┬───────────┬───┴───┬───────────┐    ┌─────┴──────┐
             ▼           ▼           ▼       ▼           ▼    ▼            ▼
      ┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐ ┌──────────┐
      │COACTVWC  ││COACTUPC  ││COCRDLIC  ││COTRN00C  ││CORPT00C  │ │COUSR00C  │
      │Acct View ││Acct Upd  ││Card List ││Tran List ││Reports   │ │User List │
      └────┬─────┘└──────────┘└────┬─────┘└──────────┘└──────────┘ └────┬─────┘
           │                       │                                     │
           │ XCTL                  │ XCTL                               │ XCTL
           ▼                       ▼                                     ▼
      ┌──────────┐          ┌──────────┐                          ┌──────────┐
      │COACTUPC  │          │COCRDSLC  │                          │COUSR01C  │
      │Acct Upd  │          │Card View │                          │User Add  │
      └──────────┘          └────┬─────┘                          ├──────────┤
                                 │ XCTL                           │COUSR02C  │
                                 ▼                                │User Upd  │
                           ┌──────────┐                           ├──────────┤
                           │COCRDUPC  │                           │COUSR03C  │
                           │Card Upd  │                           │User Del  │
                           └──────────┘                           └──────────┘

Additional online programs (from Main Menu):
  COTRN01C (Transaction View)  ← selected from COTRN00C list
  COTRN02C (Transaction Add)   ← direct menu option
  COBIL00C (Bill Payment)      ← direct menu option
```

### XCTL Transfer Details

| Source Program | Target Program | Condition |
|---------------|----------------|-----------|
| COSGN00C | COMEN01C | User type = `U` (Regular) |
| COSGN00C | COADM01C | User type = `A` (Admin) |
| COMEN01C | *menu target* | XCTL to program in `COMEN02Y` option table |
| COADM01C | *menu target* | XCTL to program in `COADM02Y` option table |
| COACTVWC | COACTUPC | User selects "Update" from view screen |
| COCRDLIC | COCRDSLC | User selects card from list |
| COCRDLIC | COCRDUPC | User selects "Update" from list |
| COCRDSLC | COCRDUPC | User selects "Update" from detail |
| COACTUPC | COMEN01C | Return to menu |
| COUSR00C | COUSR01C/02C/03C | Admin selects Add/Update/Delete |

### Subroutine CALL Dependencies (Online)

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| COACTUPC | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation/conversion |
| COACTVWC | CSUTLDTC | `CALL 'CSUTLDTC'` | Date formatting |
| COCRDUPC | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation |
| COCRDSLC | CSUTLDTC | `CALL 'CSUTLDTC'` | Date formatting |
| COCRDLIC | CSUTLDTC | `CALL 'CSUTLDTC'` | Date formatting |

---

## 2. Batch Program Call Graph

```
  JCL POSTTRAN                    JCL INTCALC                   JCL CREASTMT
       │                               │                             │
       ▼                               ▼                             ▼
  ┌──────────┐                   ┌──────────┐                  ┌──────────┐
  │CBTRN02C  │                   │CBACT04C  │                  │CBSTM03A  │
  │Tran Post │                   │Interest  │                  │Statement │
  └──────────┘                   │Calc      │                  │Generator │
                                 └──────────┘                  └────┬─────┘
                                                                    │ CALL
                                                                    ▼
                                                               ┌──────────┐
                                                               │CBSTM03B  │
                                                               │File I/O  │
                                                               │Subroutine│
                                                               └──────────┘

  JCL TRANREPT                    JCL WAITSTEP
       │                               │
       ▼                               ▼
  ┌──────────┐                   ┌──────────┐
  │CBTRN03C  │                   │COBSWAIT  │──CALL──▶ MVSWAIT (ASM)
  │Tran Rept │                   │Wait Util │
  └──────────┘                   └──────────┘

  JCL CBEXPORT      JCL CBIMPORT      JCL READACCT/READCARD/READXREF/READCUST
       │                  │                  │
       ▼                  ▼                  ▼
  ┌──────────┐     ┌──────────┐       ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
  │CBEXPORT  │     │CBIMPORT  │       │CBACT01C  │ │CBACT02C  │ │CBACT03C  │ │CBCUS01C  │
  │Data Exp  │     │Data Imp  │       │Read Acct │ │Read Card │ │Read Xref │ │Read Cust │
  └──────────┘     └──────────┘       └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

### Batch CALL Details

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| CBSTM03A | CBSTM03B | `CALL 'CBSTM03B'` | Delegate file I/O for statement generation |
| COBSWAIT | MVSWAIT | `CALL 'MVSWAIT'` | Assembler wait routine |
| CBTRN03C | CEE3ABD | `CALL 'CEE3ABD'` | LE abnormal termination (error handling) |
| CBACT01C | *(inline)* | — | Date conversion via CODATECN copybook |

---

## 3. Program → Copybook Dependency Matrix

Shows which copybooks each program includes via `COPY` statements.

### Legend
- **C** = Core data copybook  
- **B** = BMS-generated copybook  
- **S** = Shared service copybook  
- **F** = CICS framework copybook (DFHAID, DFHBMSCA)

| Program | COCOM01Y | COTTL01Y | CSDAT01Y | CSMSG01Y | CSMSG02Y | CSUSR01Y | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CVTRA06Y | BMS CPY | DFHAID | DFHBMSCA |
|---------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:------:|:--------:|
| **COSGN00C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COMEN01C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COADM01C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COACTVWC** | C | S | S | S | S | S | C | C | C | C | | | B | F | F |
| **COACTUPC** | C | S | S | S | S | S | C | | C | C | | | B | F | F |
| **COCRDLIC** | C | S | S | S | | S | | C | | | | | B | F | F |
| **COCRDSLC** | C | S | S | S | S | S | | C | | C | | | B | F | F |
| **COCRDUPC** | C | S | S | S | S | S | | C | | C | | | B | F | F |
| **COTRN00C** | C | S | S | S | | | | | | | C | | B | F | F |
| **COTRN01C** | C | S | S | S | | | | | | | C | | B | F | F |
| **COTRN02C** | C | S | S | S | | | C | | C | | C | | B | F | F |
| **CORPT00C** | C | S | S | S | | | | | | | C | | B | F | F |
| **COBIL00C** | C | S | S | S | | | C | | C | | C | | B | F | F |
| **COUSR00C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COUSR01C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COUSR02C** | C | S | S | S | | S | | | | | | | B | F | F |
| **COUSR03C** | C | S | S | S | | S | | | | | | | B | F | F |
| **CBTRN02C** | | | | | | | C | | C | | C | C | | | |
| **CBACT04C** | | | | | | | C | | C | | C | | | | |
| **CBTRN01C** | | | | | | | C | C | C | C | C | C | | | |
| **CBTRN03C** | | | | | | | | | | | C | | | | |
| **CBSTM03A** | | | | | | | C | | C | | | | | | |
| **CBACT01C** | | | | | | | C | | | | | | | | |
| **CBACT02C** | | | | | | | | C | | | | | | | |
| **CBACT03C** | | | | | | | | | C | | | | | | |
| **CBCUS01C** | | | | | | | | | | C | | | | | |
| **CBEXPORT** | | | | | | | | | | | | | | | |
| **CBIMPORT** | | | | | | | C | C | C | C | C | | | | |

### Additional Copybook Dependencies

| Program | Additional Copybooks |
|---------|---------------------|
| COACTUPC | CVCRD01Y, CSLKPCDY, CSSETATY (×37), CSSTRPFY, CSUTLDPY, CSUTLDWY |
| COACTVWC | CVCRD01Y, CSSTRPFY |
| COCRDUPC | CVCRD01Y, CSSTRPFY |
| COCRDSLC | CVCRD01Y, CSSTRPFY |
| COCRDLIC | CSSTRPFY |
| COMEN01C | COMEN02Y (menu definitions) |
| CBACT04C | CVTRA01Y, CVTRA02Y |
| CBTRN02C | CVTRA01Y |
| CBSTM03A | COSTM01, CUSTREC |
| CBIMPORT | CVEXPORT |
| CBACT01C | CODATECN |

---

## 4. Program → VSAM File Access Map

Shows which programs read/write which VSAM files via CICS file commands or batch file I/O.

| VSAM File (DD Name) | Dataset Name | R = Read | W = Write | U = Update | D = Delete | B = Browse |
|---------------------|-------------|----------|-----------|------------|------------|------------|
| **USRSEC** | `USRSEC.VSAM.KSDS` | COSGN00C(R), COUSR02C(R,U), COUSR03C(R,D) | COUSR01C(W) | | | COUSR00C(B) |
| **ACCTDAT** | `ACCTDATA.VSAM.KSDS` | COACTVWC(R), COACTUPC(R), COBIL00C(R,U), COTRN02C(R) | | | | |
| **CARDDAT** | `CARDDATA.VSAM.KSDS` | COCRDSLC(R), COCRDUPC(R) | | | | COCRDLIC(B) |
| **CARDXREF** | `CARDXREF.VSAM.KSDS` | COACTVWC(R), COACTUPC(R), COBIL00C(R), COTRN02C(R) | | | | |
| **CUSTDAT** | `CUSTDATA.VSAM.KSDS` | COACTVWC(R), COACTUPC(R), COCRDSLC(R), COCRDUPC(R) | | | | |
| **TRANSACT** | `TRANSACT.VSAM.KSDS` | COTRN01C(R), COTRN02C(R) | COBIL00C(W), COTRN02C(W) | | | COTRN00C(B), COTRN02C(B) |

### Batch Program File Access

| Program | Input Files (Read) | Output Files (Write) |
|---------|-------------------|---------------------|
| **CBTRN02C** | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF | TRANFILE, DALYREJS |
| **CBACT04C** | TCATBALF, XREFFILE, XREFFIL1, ACCTFILE, DISCGRP | TRANSACT |
| **CBSTM03A** | TRNXFILE (sorted), XREFFILE, ACCTFILE, CUSTFILE | STMTFILE (text), HTMLFILE (HTML) |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| **CBIMPORT** | EXPFILE | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE |
| **CBACT01C** | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE |
| **CBACT02C** | CARDFILE | *(print only)* |
| **CBACT03C** | XREFFILE | *(print only)* |
| **CBCUS01C** | CUSTFILE | *(print only)* |

---

## 5. JCL Job → Program → File Data Lineage

End-to-end lineage showing which jobs touch which data through which programs.

### Core Processing Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        DAILY BATCH PROCESSING CYCLE                             │
│                                                                                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │CLOSEFIL  │───▶│Data      │───▶│POSTTRAN  │───▶│INTCALC   │───▶│TRANBKP   │  │
│  │(SDSF)    │    │Refresh   │    │(CBTRN02C)│    │(CBACT04C)│    │(IDCAMS)  │  │
│  │Close CICS│    │Jobs      │    │Post Txns │    │Calc Int  │    │Backup    │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
│                                       │               │                        │
│                                       ▼               ▼                        │
│                                ┌──────────┐    ┌──────────┐    ┌──────────┐   │
│                                │COMBTRAN  │───▶│CREASTMT  │───▶│TRANIDX   │   │
│                                │(SORT)    │    │(CBSTM03A)│    │(IDCAMS)  │   │
│                                │Combine   │    │Statements│    │Alt Index │   │
│                                └──────────┘    └──────────┘    └──────────┘   │
│                                                                      │         │
│                                                               ┌──────────┐    │
│                                                               │OPENFIL   │    │
│                                                               │(SDSF)    │    │
│                                                               │Open CICS │    │
│                                                               └──────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Detailed Data Flow Per Job

#### POSTTRAN (Transaction Posting)
```
DALYTRAN (Daily Transactions) ──────┐
XREFFILE (Card Cross-Reference) ────┤
ACCTFILE (Account Master) ──────────┤──▶ CBTRN02C ──▶ TRANFILE (Transaction Master)
TCATBALF (Category Balances) ───────┤                  DALYREJS (Rejected Transactions)
                                    │                  ACCTFILE (Updated Balances)
                                    │                  TCATBALF (Updated Balances)
```

#### INTCALC (Interest Calculation)
```
TCATBALF (Category Balances) ───────┐
XREFFILE (Card Cross-Reference) ────┤
XREFFIL1 (Xref Alternate Index) ───┤──▶ CBACT04C ──▶ TRANSACT (Interest Transactions)
ACCTFILE (Account Master) ──────────┤                  ACCTFILE (Updated Balances)
DISCGRP  (Disclosure/Interest) ─────┘
```

#### CREASTMT (Statement Generation)
```
TRNXFILE (Sorted Transactions) ─────┐
XREFFILE (Card Cross-Reference) ────┤
ACCTFILE (Account Master) ──────────┤──▶ CBSTM03A ──▶ STMTFILE (Text Statements)
CUSTFILE (Customer Master) ─────────┘    ▲             HTMLFILE (HTML Statements)
                                         │
                                    CBSTM03B (File I/O subroutine)
```

#### TRANREPT (Transaction Report)
```
TRANFILE (Transaction Master) ──────┐
CARDXREF (Cross-Reference) ─────────┤
TRANTYPE (Transaction Types) ───────┤──▶ CBTRN03C ──▶ TRANREPT (Report Output)
TRANCATG (Transaction Categories) ──┤
DATEPARM (Date Parameters) ─────────┘
```

#### CBEXPORT (Data Export)
```
CUSTFILE ───┐
ACCTFILE ───┤
XREFFILE ───┤──▶ CBEXPORT ──▶ EXPFILE (Sequential Export)
TRANSACT ───┤
CARDFILE ───┘
```

#### CBIMPORT (Data Import) — *reverse of CBEXPORT*
```
EXPFILE (Sequential Import) ──▶ CBIMPORT ──▶ CUSTFILE, ACCTFILE, XREFFILE,
                                              TRANSACT, CARDFILE
```

### Data Refresh Jobs (IDCAMS-based)

| Job | Action | Target VSAM File |
|-----|--------|-----------------|
| ACCTFILE.jcl | Delete/Define/Repro | `ACCTDATA.VSAM.KSDS` |
| CARDFILE.jcl | Close CICS → Delete/Define/Repro → Open CICS | `CARDDATA.VSAM.KSDS` |
| CUSTFILE.jcl | Close CICS → Delete/Define/Repro → Open CICS | `CUSTDATA.VSAM.KSDS` |
| TRANFILE.jcl | Close CICS → Delete/Define/Repro → Open CICS | `TRANSACT.VSAM.KSDS` |
| XREFFILE.jcl | Delete/Define/Repro + Alt Index | `CARDXREF.VSAM.KSDS` |
| DUSRSECJ.jcl | IEBGENER → IDCAMS REPRO | `USRSEC.VSAM.KSDS` |
| TRANTYPE.jcl | Delete/Define/Repro | `TRANTYPE.VSAM.KSDS` |
| TRANCATG.jcl | Delete/Define/Repro | `TRANCATG.VSAM.KSDS` |
| DISCGRP.jcl | Delete/Define/Repro | `DISCGRP.VSAM.KSDS` |
| TCATBALF.jcl | Delete/Define/Repro | `TCATBALF.VSAM.KSDS` |
| DALYREJS.jcl | Delete | `DALYREJS` |
| REPTFILE.jcl | Define | `REPTFILE` |

---

## 6. Batch Job Execution Sequence

The daily batch cycle runs in a strict order (defined in `app/scheduler/`):

```
Step 1:  CLOSEFIL ────── Close CICS-managed files
Step 2:  ACCTFILE ────── Refresh account master from PS
Step 3:  CARDFILE ────── Refresh card master from PS
Step 4:  CUSTFILE ────── Refresh customer master from PS
Step 5:  XREFFILE ────── Refresh cross-reference + alt index
Step 6:  TRANFILE ────── Refresh transaction master from PS
Step 7:  POSTTRAN ────── Post daily transactions (CBTRN02C)
Step 8:  INTCALC  ────── Calculate interest (CBACT04C)
Step 9:  TRANBKP  ────── Backup transaction file
Step 10: COMBTRAN ────── Combine/sort transactions
Step 11: CREASTMT ────── Generate statements (CBSTM03A)
Step 12: TRANIDX  ────── Rebuild alternate indexes
Step 13: OPENFIL  ────── Reopen CICS-managed files
```

**Critical Path:** Steps 7-8 (POSTTRAN → INTCALC) are the core business logic. Failure here halts the entire cycle.

---

## 7. Menu Navigation Flow

### Regular User Flow
```
COSGN00C (Sign-On)
    │
    ▼ [User Type = 'U']
COMEN01C (Main Menu)
    ├──[1]──▶ COACTVWC (Account View) ──[Update]──▶ COACTUPC (Account Update)
    ├──[2]──▶ COACTUPC (Account Update)
    ├──[3]──▶ COCRDLIC (Card List) ──[Select]──▶ COCRDSLC (Card View)
    │                               ──[Update]──▶ COCRDUPC (Card Update)
    ├──[4]──▶ COCRDSLC (Card View) ──[Update]──▶ COCRDUPC (Card Update)
    ├──[5]──▶ COCRDUPC (Card Update)
    ├──[6]──▶ COTRN00C (Transaction List)
    ├──[7]──▶ COTRN01C (Transaction View)
    ├──[8]──▶ COTRN02C (Transaction Add)
    ├──[9]──▶ CORPT00C (Transaction Reports) ──[Submit]──▶ TDQ ──▶ TRANREPT batch
    ├──[10]─▶ COBIL00C (Bill Payment)
    └──[11]─▶ COPAUS0C (Pending Auth View) [Optional Module]
```

### Admin User Flow
```
COSGN00C (Sign-On)
    │
    ▼ [User Type = 'A']
COADM01C (Admin Menu)
    ├──[1]──▶ COUSR00C (User List) ──[Select]──▶ COUSR02C/COUSR03C
    ├──[2]──▶ COUSR01C (User Add)
    ├──[3]──▶ COUSR02C (User Update)
    ├──[4]──▶ COUSR03C (User Delete)
    ├──[5]──▶ COTRTLIC (Tran Type List) [Optional - DB2]
    └──[6]──▶ COTRTUPC (Tran Type Maint) [Optional - DB2]
```

---

## 8. Shared Copybook Fan-Out

Shows how many programs depend on each copybook — high fan-out = high change impact.

| Copybook | Used By (Count) | Programs |
|----------|:---------------:|----------|
| **COCOM01Y** | 17 | All online CICS programs |
| **COTTL01Y** | 17 | All online CICS programs |
| **CSDAT01Y** | 17 | All online CICS programs |
| **CSMSG01Y** | 17 | All online CICS programs |
| **DFHAID** | 17 | All online CICS programs |
| **DFHBMSCA** | 17 | All online CICS programs |
| **CSUSR01Y** | 11 | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COCRDLIC, COUSR00C-03C |
| **CVACT01Y** | 9 | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBTRN01C, CBTRN02C, CBACT01C, CBACT04C, CBIMPORT, CBSTM03A |
| **CVACT03Y** | 8 | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBTRN01C, CBTRN02C, CBACT03C, CBACT04C, CBSTM03A |
| **CVTRA05Y** | 8 | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBTRN01C, CBTRN02C, CBACT04C |
| **CVCUS01Y** | 7 | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBTRN01C, CBCUS01C, CBIMPORT |
| **CVACT02Y** | 6 | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBTRN01C, CBACT02C, CBIMPORT |
| **CSMSG02Y** | 4 | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| **CSSTRPFY** | 4 | COACTVWC, COACTUPC, COCRDSLC, COCRDLIC |
| **CVCRD01Y** | 4 | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| **CVTRA06Y** | 2 | CBTRN01C, CBTRN02C |
| **CVTRA01Y** | 2 | CBTRN02C, CBACT04C |
| **CVTRA02Y** | 1 | CBACT04C |

**Impact Analysis:** Changes to `COCOM01Y`, `COTTL01Y`, or `CSMSG01Y` require retesting all 17 online programs. Changes to `CVACT01Y` affect 9 programs across online and batch.
