# DEPENDENCY MAP - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Source:** Static analysis of EXEC CICS XCTL, CALL, COPY, FILE, and JCL DD statements
> **Purpose:** Program call graph, copybook inclusion matrix, and data lineage for modernization planning

---

## Table of Contents

1. [Online Program Call Graph (CICS)](#1-online-program-call-graph-cics)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [Copybook Inclusion Matrix](#3-copybook-inclusion-matrix)
4. [VSAM File Access Matrix (Online)](#4-vsam-file-access-matrix-online)
5. [Batch Job Data Lineage](#5-batch-job-data-lineage)
6. [JCL Job Execution Chain](#6-jcl-job-execution-chain)
7. [End-to-End Data Flow](#7-end-to-end-data-flow)
8. [Batch Cycle Orchestration](#8-batch-cycle-orchestration)

---

## 1. Online Program Call Graph (CICS)

Transfer of control between CICS programs via `EXEC CICS XCTL` and `EXEC CICS RETURN TRANSID`.

### ASCII Call Graph

```
                         ┌──────────────┐
                         │  COSGN00C    │  (Signon - CC00)
                         │  Entry Point │
                         └──────┬───────┘
                                │ XCTL
                    ┌───────────┴───────────┐
                    ▼                       ▼
            ┌──────────────┐       ┌──────────────┐
            │  COMEN01C    │       │  COADM01C    │
            │  Main Menu   │       │  Admin Menu  │
            │  (CM00)      │       │  (CA00)      │
            └──────┬───────┘       └──────┬───────┘
                   │ XCTL (dynamic)       │ XCTL (dynamic)
     ┌─────────┬───┴───┬─────────┐   ┌───┴───┬──────────┐
     ▼         ▼       ▼         ▼   ▼       ▼          ▼
┌─────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│COACTVWC ││COCRDLIC││COTRN00C││COBIL00C││COUSR00C││CORPT00C│
│Acct View││Card Lst││Txn List││Bill Pay││User Lst││Reports │
│(CAVW)   ││(CCLI)  ││(CT00)  ││(CB00)  ││(CU00)  ││(CR00)  │
└────┬────┘└───┬────┘└───┬────┘└────────┘└───┬────┘└────────┘
     │         │         │                   │
     │ XCTL    │ XCTL    │ RETURN            │ XCTL (dynamic)
     ▼         ▼         ▼                   ▼
┌─────────┐┌────────┐┌────────┐        ┌────┬────┬────┐
│COACTUPC ││COCRDSLC││COTRN01C│        │USR1│USR2│USR3│
│Acct Upd ││Card Dtl││Txn View│        │Add │Upd │Del │
│(CAUP)   ││(CCDL)  ││(CT01)  │        └────┴────┴────┘
└─────────┘└───┬────┘└────────┘
               │ XCTL
               ▼
          ┌────────┐
          │COCRDUPC│
          │Card Upd│
          │(CCUP)  │
          └────────┘
```

### Detailed XCTL Transfer Table

| Source Program | Target Program | Mechanism | Condition |
|---------------|---------------|-----------|-----------|
| **COSGN00C** | COMEN01C | `XCTL PROGRAM('COMEN01C')` | Regular user login |
| **COSGN00C** | COADM01C | `XCTL PROGRAM('COADM01C')` | Admin user login |
| **COMEN01C** | *(dynamic)* | `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME)` | Menu option selected |
| **COADM01C** | *(dynamic)* | `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME)` | Admin option selected (inferred) |
| **COACTVWC** | *(dynamic)* | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Navigate to account update |
| **COACTUPC** | *(dynamic)* | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to caller |
| **COCRDLIC** | COMEN01C | `XCTL PROGRAM(LIT-MENUPGM)` | PF3 back to menu |
| **COCRDLIC** | COCRDSLC | `XCTL PROGRAM(CCARD-NEXT-PROG)` | Select card for view ('S') |
| **COCRDLIC** | COCRDUPC | `XCTL PROGRAM(CCARD-NEXT-PROG)` | Select card for update ('U') |
| **COCRDSLC** | *(dynamic)* | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Navigate to update |
| **COCRDUPC** | *(dynamic)* | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Return to list |

### Menu-to-Program Mapping

**Regular User Menu (COMEN01C):**

| Option | Program | Transaction | Description |
|--------|---------|-------------|-------------|
| 1 | COACTVWC | CAVW | Account View |
| 2 | COCRDLIC | CCLI | Credit Card List |
| 3 | COTRN00C | CT00 | Transaction List |
| 4 | COBIL00C | CB00 | Bill Payment |
| 5 | CORPT00C | CR00 | Transaction Report |

**Admin Menu (COADM01C):**

| Option | Program | Transaction | Description |
|--------|---------|-------------|-------------|
| 1 | COUSR00C | CU00 | User List |

### Subroutine CALL Dependencies (Online)

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| **COTRN02C** | CSUTLDTC | `CALL 'CSUTLDTC'` | Validate date input |
| **CORPT00C** | CSUTLDTC | `CALL 'CSUTLDTC'` | Validate report date range |

---

## 2. Batch Program Call Graph

```
 CBSTM03A ──CALL──▶ CBSTM03B    (Statement generation: driver calls I/O helper)
 CBACT01C ──CALL──▶ COBDATFT    (Account read: calls assembler date formatter)
 COBSWAIT ──CALL──▶ MVSWAIT     (Wait utility: calls assembler wait routine)
 CBTRN02C ──CALL──▶ CEE3ABD     (Transaction posting: calls LE abend handler)
 CBTRN01C ──CALL──▶ CEE3ABD     (Transaction posting phase 1: abend handler)
 CBTRN03C ──CALL──▶ CEE3ABD     (Transaction report: abend handler)
 CBACT04C ──CALL──▶ CEE3ABD     (Interest calc: abend handler)
 CBACT01C ──CALL──▶ CEE3ABD     (Account read: abend handler)
 CBACT02C ──CALL──▶ CEE3ABD     (Card read: abend handler)
 CBACT03C ──CALL──▶ CEE3ABD     (Xref read: abend handler)
 CBCUS01C ──CALL──▶ CEE3ABD     (Customer read: abend handler)
 CBSTM03A ──CALL──▶ CEE3ABD     (Statement gen: abend handler)
 CBEXPORT ──CALL──▶ CEE3ABD     (Export: abend handler)
 CBIMPORT ──CALL──▶ CEE3ABD     (Import: abend handler)
 CSUTLDTC ──CALL──▶ CEEDAYS    (Date utility: calls LE date conversion)
```

### Batch CALL Summary Table

| Caller | Callee | Type | Purpose |
|--------|--------|------|---------|
| **CBSTM03A** | **CBSTM03B** | COBOL CALL | File I/O delegation (open/read/close/write for TRNX, XREF, CUST, ACCT files) |
| **CBACT01C** | **COBDATFT** | ASM CALL | Date formatting (assembler routine) |
| **COBSWAIT** | **MVSWAIT** | ASM CALL | Low-level wait (assembler routine) |
| **CSUTLDTC** | **CEEDAYS** | LE CALL | Date validation via Language Environment |
| *(12 programs)* | **CEE3ABD** | LE CALL | Abend handling via Language Environment |

---

## 3. Copybook Inclusion Matrix

Which programs COPY which copybooks (active includes only, excludes commented-out copies).

| Copybook | Domain | Included By (Programs) |
|----------|--------|----------------------|
| **COCOM01Y** | COMMAREA | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** | Titles | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** | Date | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** | Messages | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** | Security | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG02Y** | Abend | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| **CVACT01Y** | Account | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C |
| **CVACT02Y** | Card | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C |
| **CVACT03Y** | Xref | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBTRN02C |
| **CVCUS01Y** | Customer | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C |
| **CVCRD01Y** | Card Work | COCRDLIC, COCRDSLC, COCRDUPC |
| **CVTRA05Y** | Transaction | COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C |
| **CVTRA06Y** | Daily Txn | CBTRN02C |
| **CVTRA01Y** | Cat Balance | CBTRN02C, CBACT04C |
| **CVTRA02Y** | Disc Group | CBACT04C |
| **CVTRA03Y** | Txn Type | CBTRN02C, CBTRN03C |
| **CVTRA04Y** | Txn Cat | CBTRN02C, CBTRN03C |
| **CVTRA07Y** | Report | CBTRN03C |
| **COSTM01** | Statement | CBSTM03A, CBSTM03B |
| **CVEXPORT** | Export | CBEXPORT, CBIMPORT |
| **CODATECN** | Date Conv | CBACT01C |
| **CSUTLDWY** | Date Edit | COACTUPC |
| **CSUTLDPY** | Date Util | CSUTLDTC |
| **CSSTRPFY** | Strip Fn | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSSETATY** | Attr Set | COACTUPC |
| **CSLKPCDY** | Lookups | (not actively included) |
| **COADM02Y** | Admin Menu | COADM01C |
| **COMEN02Y** | Main Menu | COMEN01C |
| **CUSTREC** | Cust Alt | (not actively included) |
| **UNUSED1Y** | Unused | (not actively included) |

### Batch Program Copybook Inclusions

| Program | Copybooks Used |
|---------|---------------|
| **CBACT01C** | CVACT01Y, CODATECN |
| **CBACT02C** | CVACT02Y |
| **CBACT03C** | CVACT03Y |
| **CBACT04C** | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y |
| **CBCUS01C** | CVCUS01Y |
| **CBTRN01C** | CVTRA05Y |
| **CBTRN02C** | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA06Y |
| **CBTRN03C** | CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y |
| **CBSTM03A** | COSTM01 |
| **CBSTM03B** | COSTM01 |
| **CBEXPORT** | CVEXPORT |
| **CBIMPORT** | CVEXPORT |

---

## 4. VSAM File Access Matrix (Online)

Which online CICS programs access which VSAM files and what operations they perform.

| Program | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | TRANSACT | USRSEC |
|---------|:-------:|:-------:|:-------:|:-------:|:--------:|:------:|
| **COSGN00C** | | | | | | R |
| **COMEN01C** | | | | | | R |
| **COADM01C** | | | | | | R |
| **COACTVWC** | R | R | R | R | | |
| **COACTUPC** | R/W | R | | R/W | | |
| **COCRDLIC** | | R | R | | | |
| **COCRDSLC** | | R | R | R | | |
| **COCRDUPC** | | R/W | | R | | |
| **COTRN00C** | | | | | R(browse) | |
| **COTRN01C** | | | | | R | |
| **COTRN02C** | R | | | | R/W | |
| **COBIL00C** | R/W | | | | R/W | |
| **CORPT00C** | | | | | R(TD) | |
| **COUSR00C** | | | | | | R(browse) |
| **COUSR01C** | | | | | | W |
| **COUSR02C** | | | | | | R/W |
| **COUSR03C** | | | | | | R/D |

**Legend:** R=Read, W=Write, R/W=Read+Rewrite, R/D=Read+Delete, browse=STARTBR/READNEXT, TD=Transient Data Queue

### VSAM File Details

| Logical Name | Physical Dataset | Key | Record Length | Access Method |
|-------------|-----------------|-----|-------------:|---------------|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | ACCT-ID (11) | 300 | KSDS |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | CARD-NUM (16) | 150 | KSDS |
| CARDAIX | (Alternate index path over CARDDAT) | CARD-ACCT-ID (11) | 150 | AIX |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | CUST-ID (9) | 500 | KSDS |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID (16) | 350 | KSDS |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | SEC-USR-ID (8) | 80 | KSDS |

---

## 5. Batch Job Data Lineage

Which JCL jobs read from and write to which datasets.

### Core Processing Jobs

| Job | Program | Reads (Input) | Writes (Output) |
|-----|---------|---------------|-----------------|
| **POSTTRAN** | CBTRN02C | DALYTRAN (daily transactions), TRANSACT, CARDXREF, ACCTDATA, TCATBALF, DISCGRP, TRANTYPE, TRANCATG | TRANSACT (updated), ACCTDATA (updated), TCATBALF (updated), DALYREJS (rejections) |
| **INTCALC** | CBACT04C | ACCTDATA, TCATBALF, DISCGRP | ACCTDATA (balances updated) |
| **CREASTMT** | CBSTM03A, SORT, IDCAMS | TRANSACT, CARDXREF, CUSTDATA, ACCTDATA | STATEMNT.PS (text), STATEMNT.HTML |
| **TRANREPT** | CBTRN03C, SORT | TRANSACT, TRANTYPE, TRANCATG | TRANREPT (report GDG) |

### Data Refresh Jobs

| Job | Function | Input Dataset | Target VSAM |
|-----|----------|--------------|-------------|
| **ACCTFILE** | Refresh accounts | ACCTDATA.PS (sequential) | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | Refresh cards | CARDDATA.PS (sequential) | CARDDATA.VSAM.KSDS + AIX |
| **CUSTFILE** | Refresh customers | CUSTDATA.PS (sequential) | CUSTDATA.VSAM.KSDS |
| **TRANFILE** | Refresh transactions | TRANSACT.PS (sequential) | TRANSACT.VSAM.KSDS + AIX |
| **XREFFILE** | Refresh cross-refs | CARDXREF.PS (sequential) | CARDXREF.VSAM.KSDS + AIX |
| **DUSRSECJ** | Load user security | USRSEC.PS (sequential) | USRSEC.VSAM.KSDS |
| **TRANTYPE** | Load transaction types | TRANTYPE.PS (sequential) | TRANTYPE.VSAM.KSDS |
| **TRANCATG** | Load tran categories | TRANCATG.PS (sequential) | TRANCATG.VSAM.KSDS |
| **TCATBALF** | Load category balances | TCATBALF.PS (sequential) | TCATBALF.VSAM.KSDS |
| **DISCGRP** | Load disclosure groups | DISCGRP.PS (sequential) | DISCGRP.VSAM.KSDS |

### Backup & Maintenance Jobs

| Job | Function | Input | Output |
|-----|----------|-------|--------|
| **TRANBKP** | Backup transactions | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) GDG |
| **COMBTRAN** | Combine daily + master | DALYTRAN + TRANSACT.BKUP(0) | TRANSACT.COMBINED(+1) GDG |
| **TRANIDX** | Build alternate indexes | TRANSACT.VSAM.KSDS | AIX path rebuilt |

### Diagnostic Jobs

| Job | Program | Reads | Output |
|-----|---------|-------|--------|
| **READACCT** | CBACT01C | ACCTDATA.VSAM.KSDS | SYSOUT (print) |
| **READCARD** | CBACT02C | CARDDATA.VSAM.KSDS | SYSOUT (print) |
| **READCUST** | CBCUS01C | CUSTDATA.VSAM.KSDS | SYSOUT (print) |
| **READXREF** | CBACT03C | CARDXREF.VSAM.KSDS | SYSOUT (print) |
| **PRTCATBL** | SORT | TCATBALF.VSAM.KSDS | TCATBALF.REPT |

### Data Migration Jobs

| Job | Program | Direction | Datasets |
|-----|---------|-----------|----------|
| **CBEXPORT** | CBEXPORT | VSAM -> Sequential | All VSAM files -> EXPORT.DATA |
| **CBIMPORT** | CBIMPORT | Sequential -> VSAM | TRANSACT.IMPORT -> TRANSACT.VSAM |

---

## 6. JCL Job Execution Chain

### Nightly Batch Cycle (ordered sequence)

```
Step 1: CLOSEFIL  ──▶  Close CICS files (SDSF command)
        │
Step 2: Data Refresh (parallel)
        ├── ACCTFILE  ──▶  Refresh account master
        ├── CARDFILE  ──▶  Refresh card master
        ├── CUSTFILE  ──▶  Refresh customer master
        ├── TRANFILE  ──▶  Refresh transaction master
        └── XREFFILE  ──▶  Refresh cross-reference
        │
Step 3: POSTTRAN  ──▶  Post daily transactions (CBTRN02C)
        │               Reads: DALYTRAN, TRANSACT, CARDXREF, ACCTDATA
        │               Updates: TRANSACT, ACCTDATA, TCATBALF
        │
Step 4: INTCALC   ──▶  Calculate interest (CBACT04C)
        │               Reads: ACCTDATA, TCATBALF, DISCGRP
        │               Updates: ACCTDATA (balances)
        │
Step 5: TRANBKP   ──▶  Backup transaction file (GDG)
        │
Step 6: COMBTRAN  ──▶  Combine daily + backup (SORT + IDCAMS)
        │
Step 7: CREASTMT  ──▶  Generate statements (CBSTM03A)
        │               Reads: TRANSACT, XREF, CUST, ACCT
        │               Writes: Statement text + HTML
        │
Step 8: TRANIDX   ──▶  Rebuild alternate indexes
        │
Step 9: OPENFIL   ──▶  Reopen CICS files (SDSF command)
```

### Job Scheduling (from `app/scheduler/`)

The batch cycle is managed by either CA-7 or Control-M depending on the site.

---

## 7. End-to-End Data Flow

### Transaction Lifecycle

```
                 ONLINE                              BATCH
                 ──────                              ─────

  User enters    ┌──────────┐
  transaction ──▶│ COTRN02C │──▶ TRANSACT (VSAM)
  via 3270       │ Txn Add  │
                 └──────────┘
                                    │
                                    │ Nightly batch
                                    ▼
                              ┌──────────┐
                              │ POSTTRAN │──▶ Updates: TRANSACT
                              │ CBTRN02C │    Updates: ACCTDATA (balance)
                              │          │    Updates: TCATBALF (cat balance)
                              └─────┬────┘
                                    │
                                    ▼
                              ┌──────────┐
                              │ INTCALC  │──▶ Updates: ACCTDATA
                              │ CBACT04C │    (adds interest to balance)
                              └─────┬────┘
                                    │
                              ┌─────┴────┐
                              ▼          ▼
                        ┌──────────┐ ┌──────────┐
                        │ CREASTMT │ │ TRANREPT │
                        │ CBSTM03A │ │ CBTRN03C │
                        │Statement │ │Report Gen│
                        └──────────┘ └──────────┘
```

### Bill Payment Flow

```
  User pays bill  ┌──────────┐
  via 3270     ──▶│ COBIL00C │──▶ Reads: ACCTDAT (account balance)
                  │ Bill Pay │    Reads: TRANSACT (last txn ID)
                  └──────────┘    Writes: TRANSACT (payment txn)
                                  Rewrites: ACCTDAT (zero balance)
```

### Account View/Update Flow

```
  User views     ┌──────────┐
  account    ──▶ │ COACTVWC │──▶ Reads: ACCTDAT, CARDDAT(AIX), CUSTDAT
                 │ Acct View│
                 └─────┬────┘
                       │ XCTL
                       ▼
                 ┌──────────┐
                 │ COACTUPC │──▶ Reads: ACCTDAT, CUSTDAT
                 │ Acct Upd │    Rewrites: ACCTDAT, CUSTDAT
                 └──────────┘
```

### Card Management Flow

```
  User browses   ┌──────────┐
  cards      ──▶ │ COCRDLIC │──▶ Reads: CARDDAT (browse via AIX)
                 │ Card List│
                 └─────┬────┘
                    ┌──┴──┐
                    ▼     ▼
              ┌────────┐┌────────┐
              │COCRDSLC││COCRDUPC│──▶ Reads: CARDDAT
              │Card Dtl││Card Upd│    Rewrites: CARDDAT
              └────────┘└────────┘
```

---

## 8. Batch Cycle Orchestration

### Dataset Dependency Graph

```
  USRSEC.PS ─────▶ DUSRSECJ ─────▶ USRSEC.VSAM
  ACCTDATA.PS ───▶ ACCTFILE ─────▶ ACCTDATA.VSAM ──┐
  CARDDATA.PS ───▶ CARDFILE ─────▶ CARDDATA.VSAM    │
  CUSTDATA.PS ───▶ CUSTFILE ─────▶ CUSTDATA.VSAM    ├──▶ CICS Online
  CARDXREF.PS ───▶ XREFFILE ─────▶ CARDXREF.VSAM    │
  TRANSACT.PS ───▶ TRANFILE ─────▶ TRANSACT.VSAM ──┘
                                         │
                                         ▼
                                   POSTTRAN (CBTRN02C)
                                         │
                                    ┌────┼────┐
                                    ▼    ▼    ▼
                               ACCTDATA TCATBALF TRANSACT
                               (updated)(updated)(updated)
                                    │
                                    ▼
                               INTCALC (CBACT04C)
                                    │
                                    ▼
                               ACCTDATA (interest applied)
                                    │
                              ┌─────┼─────┐
                              ▼     ▼     ▼
                           TRANBKP CREASTMT TRANREPT
                              │     │        │
                              ▼     ▼        ▼
                           GDG    STMT.PS  REPT GDG
                           Backup  HTML
```

### Critical Path

The **critical path** for the nightly batch cycle is:

```
CLOSEFIL → [Data Refresh] → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

**POSTTRAN** (CBTRN02C) is the most critical job -- it must complete before interest calculation and statement generation can begin. It touches the most files and has the most complex business logic.
