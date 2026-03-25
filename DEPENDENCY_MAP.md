# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Scope:** Call graph, copybook dependencies, CICS screen flow, JCL job chains, and data lineage

---

## Table of Contents

1. [Online CICS Call Graph](#online-cics-call-graph)
2. [Batch Call Graph](#batch-call-graph)
3. [Copybook Dependency Matrix](#copybook-dependency-matrix)
4. [CICS Screen Navigation Flow](#cics-screen-navigation-flow)
5. [JCL Job → Program → File Data Lineage](#jcl-job--program--file-data-lineage)
6. [Batch Cycle Sequence](#batch-cycle-sequence)
7. [VSAM File Access Matrix](#vsam-file-access-matrix)
8. [Cross-Cutting Dependencies](#cross-cutting-dependencies)

---

## Online CICS Call Graph

All online programs communicate via **COMMAREA** (COCOM01Y) and transfer control using **EXEC CICS XCTL**.

```
                          ┌─────────────────────────────────┐
                          │          COSGN00C               │
                          │     (Signon - CC00)             │
                          │  Reads: USRSEC                  │
                          └────────────┬────────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │ (Admin user)     │                  │ (Regular user)
                    ▼                  │                  ▼
          ┌─────────────────┐          │        ┌─────────────────┐
          │   COADM01C      │          │        │   COMEN01C      │
          │ (Admin Menu)    │          │        │ (Main Menu)     │
          └────────┬────────┘          │        └────────┬────────┘
                   │                   │                 │
     ┌─────────────┤                   │    ┌────────────┼────────────────────────────────┐
     │             │                   │    │            │            │          │         │
     ▼             ▼                   │    ▼            ▼            ▼          ▼         ▼
 COUSR00C     COUSR01C                 │ COACTVWC   COCRDLIC     COTRN00C   CORPT00C  COBIL00C
 (List        (Add                     │ (Acct      (Card        (Tran      (Reports) (Bill
  Users)       User)                   │  View)      List)        List)               Payment)
     │             │                   │    │            │            │
     ▼             │                   │    ▼            ▼            ▼
 COUSR02C          │                   │ COACTUPC   COCRDSLC     COTRN01C
 (Update           │                   │ (Acct      (Card        (Tran
  User)            │                   │  Update)    View)        View)
     │             │                   │                │
     ▼             │                   │                ▼            ▼
 COUSR03C          │                   │            COCRDUPC     COTRN02C
 (Delete           │                   │            (Card        (Tran
  User)            │                   │             Update)      Add)
                   │                   │
                   │                   │
                   └───────────────────┘
```

### XCTL Transfer Details

| Source Program | Target Program | Condition | COMMAREA Fields Used |
|---------------|---------------|-----------|---------------------|
| COSGN00C | COADM01C | SEC-USR-TYPE = 'A' (Admin) | CDEMO-TO-PROGRAM, CDEMO-TO-TRANID |
| COSGN00C | COMEN01C | SEC-USR-TYPE = 'R' (Regular) | CDEMO-TO-PROGRAM, CDEMO-TO-TRANID |
| COMEN01C | *(menu option)* | Dynamic via CDEMO-MENU-OPT-PGMNAME(option) | Full COMMAREA |
| COACTUPC | *(return prog)* | Dynamic via CDEMO-TO-PROGRAM | Full COMMAREA |
| COACTVWC | *(return prog)* | Dynamic via CDEMO-TO-PROGRAM | Full COMMAREA |
| COCRDLIC | *(menu prog)* | Back to menu via LIT-MENUPGM | Full COMMAREA |
| COCRDLIC | *(detail prog)* | Select card via CCARD-NEXT-PROG | Full COMMAREA |
| COCRDSLC | *(return prog)* | Dynamic via CDEMO-TO-PROGRAM | Full COMMAREA |
| COCRDUPC | *(return prog)* | Dynamic via CDEMO-TO-PROGRAM | Full COMMAREA |

### CICS File Access by Online Programs

| Program | USRSEC | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT |
|---------|--------|----------|----------|----------|----------|----------|
| COSGN00C | R | | | | | |
| COACTVWC | | R | R | R | R | |
| COACTUPC | | RW | R | R | RW | |
| COCRDLIC | | | R | | | |
| COCRDSLC | | | R | R | | |
| COCRDUPC | | | RW | | | |
| COTRN00C | | | | | | R |
| COTRN01C | | | | | | R |
| COTRN02C | | R | | R | | RW |
| COBIL00C | | RW | | R | | RW |
| COUSR00C | R | | | | | |
| COUSR01C | W | | | | | |
| COUSR02C | RW | | | | | |
| COUSR03C | RD | | | | | |

*R = Read, W = Write, RW = Read/Write (REWRITE), RD = Read/Delete*

---

## Batch Call Graph

Batch programs use **CALL** for subroutine invocation and run independently via JCL.

```
CBTRN02C (Transaction Posting)
    ├── Reads: DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM
    ├── Writes: TRANSACT.VSAM, TCATBALF.VSAM, DALYREJS GDG
    └── No CALLs to other COBOL programs

CBACT04C (Interest Calculator)
    ├── Reads: TCATBALF.VSAM, CARDXREF.VSAM, DISCGRP.VSAM, ACCTDATA.VSAM
    ├── Writes: ACCTDATA.VSAM (REWRITE), SYSTRAN GDG
    ├── PARM: Date parameter via LINKAGE SECTION
    └── No CALLs to other COBOL programs

CBACT01C (Account Data Extract)
    ├── Reads: ACCTDATA.VSAM
    ├── Writes: OUTFILE (PS), ARRYFILE (PS), VBRCFILE (VB)
    └── CALL 'COBDATFT' (Assembler date formatter)

CBSTM03A (Statement Generation - Main)
    ├── Reads: (via CBSTM03B)
    ├── Writes: STMTFILE (PS), HTMLFILE (HTML)
    └── CALL 'CBSTM03B' (File processing subroutine)
         └── CBSTM03B reads: TRNXFILE, ACCTFILE, CUSTFILE, XREFFILE

CBTRN03C (Transaction Report)
    ├── Reads: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
    ├── Writes: TRANREPT (report output)
    └── No CALLs to other COBOL programs

CBTRN01C (Transaction Validation)
    ├── Reads: DALYTRAN, CARDXREF, ACCTDATA, CARDDATA, CUSTDATA
    ├── Writes: TRANFILE (validated transactions)
    └── No CALLs to other COBOL programs

CBEXPORT (Data Export)
    ├── Reads: CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA
    ├── Writes: EXPORT.DATA
    └── No CALLs to other COBOL programs

CBIMPORT (Data Import)
    ├── Reads: EXPORT.DATA
    ├── Writes: CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT
    └── No CALLs to other COBOL programs

CBACT02C (Card Data Reader)
    ├── Reads: CARDDATA.VSAM
    └── CALL 'CEE3ABD' (LE Abend routine - error only)

CBACT03C (Cross-Ref Reader)
    ├── Reads: CARDXREF.VSAM
    └── CALL 'CEE3ABD' (LE Abend routine - error only)

CBCUS01C (Customer Data Reader)
    ├── Reads: CUSTDATA.VSAM
    └── CALL 'CEE3ABD' (LE Abend routine - error only)

COBSWAIT (Wait Utility)
    └── CALL 'MVSWAIT' (Assembler wait service)
```

### Batch CALL Dependencies

| Caller | Callee | Type | Purpose |
|--------|--------|------|---------|
| CBACT01C | COBDATFT | Assembler | Date format conversion |
| CBSTM03A | CBSTM03B | COBOL Subroutine | File I/O for statement generation |
| CBACT02C | CEE3ABD | LE Runtime | Abnormal termination on error |
| CBACT03C | CEE3ABD | LE Runtime | Abnormal termination on error |
| CBCUS01C | CEE3ABD | LE Runtime | Abnormal termination on error |
| CBACT04C | CEE3ABD | LE Runtime | Abnormal termination on error |
| COBSWAIT | MVSWAIT | Assembler | MVS wait/sleep implementation |

---

## Copybook Dependency Matrix

Which programs COPY which copybooks.

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT | Online* |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|---------|
| CVACT01Y | X | | | X | | | | | | X | X | | X |
| CVACT02Y | | X | | | | | | | | | X | | X |
| CVACT03Y | | | X | X | | | | | | X | X | | X |
| CVCUS01Y | | | | | X | | | | | X | X | | |
| CVCRD01Y | | | | | | | | | | | | | X |
| CVTRA01Y | | | | X | | | X | | | | | | |
| CVTRA02Y | | | | X | | | | | | | | | |
| CVTRA03Y | | | | | | | | X | | | | | |
| CVTRA04Y | | | | | | | | X | | | | | |
| CVTRA05Y | | | | X | | X | X | | | | X | | X |
| CVTRA06Y | | | | | | X | X | | | | | | |
| CVTRA07Y | | | | | | | | X | | | | | |
| CVEXPORT | | | | | | | | | | | X | X | |
| COSTM01 | | | | | | | | | X | X | | | |
| CSUSR01Y | | | | | | | | | | | | | X |
| CODATECN | X | | | | | | | | | | | | |
| COCOM01Y | | | | | | | | | | | | | X |
| COMEN02Y | | | | | | | | | | | | | X |
| COTTL01Y | | | | | | | | | | | | | X |
| CSMSG01Y | | | | | | | | | | | | | X |
| CSDAT01Y | | | | | | | | | | | | | X |

*Online = used by one or more of the 17 CICS online programs*

---

## CICS Screen Navigation Flow

```
                    ┌──────────────┐
                    │  3270 Login  │
                    │  (COSGN00)   │
                    └──────┬───────┘
                           │ CC00 transaction
                           ▼
                    ┌──────────────┐
                    │  COSGN00C    │──── Reads USRSEC ──── Validates credentials
                    └──────┬───────┘
                           │
              ┌────────────┴────────────┐
              │                         │
         Admin (Type A)            Regular (Type R)
              │                         │
              ▼                         ▼
     ┌────────────────┐        ┌────────────────┐
     │  Admin Menu    │        │  Main Menu     │
     │  (COADM01)     │        │  (COMEN01)     │
     └───────┬────────┘        └───────┬────────┘
             │                         │
     ┌───────┤                   ┌─────┼─────┬──────┬──────┬──────┐
     │       │                   │     │     │      │      │      │
     ▼       ▼                   ▼     ▼     ▼      ▼      ▼      ▼
  User     User              Acct   Card   Tran  Tran   Report  Bill
  List     Add               View   List   List  Add    Req     Pay
  COUSR00  COUSR01           COACT  COCRD  COTRN COTRN  CORPT   COBIL
  (00)     (01)              VW(00) LI(00) 00    02     00      00
     │                         │       │     │
     ├──►COUSR02               │       │     │
     │   (Update)              ▼       ▼     ▼
     │                       Acct    Card   Tran
     └──►COUSR03             Update  View   View
         (Delete)            COACT   COCRD  COTRN
                             UP(00)  SL(00) 01
                                       │
                                       ▼
                                     Card
                                     Update
                                     COCRD
                                     UP(00)
```

### BMS Map → Program → Copybook Chain

| BMS Map | Program | Data Copybooks Used | BMS Copybook |
|---------|---------|--------------------|----|
| COSGN00.bms | COSGN00C | CSUSR01Y, COCOM01Y | COSGN00.CPY |
| COMEN01.bms | COMEN01C | COCOM01Y, COMEN02Y, COTTL01Y | COMEN01.CPY |
| COADM01.bms | COADM01C | COCOM01Y, COADM02Y, COTTL01Y | COADM01.CPY |
| COACTVW.bms | COACTVWC | CVACT01Y, CVACT02Y, CVACT03Y, COCOM01Y | COACTVW.CPY |
| COACTUP.bms | COACTUPC | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, COCOM01Y | COACTUP.CPY |
| COCRDLI.bms | COCRDLIC | CVACT02Y, COCOM01Y | COCRDLI.CPY |
| COCRDSL.bms | COCRDSLC | CVACT02Y, CVACT03Y, COCOM01Y | COCRDSL.CPY |
| COCRDUP.bms | COCRDUPC | CVACT02Y, CVACT03Y, COCOM01Y | COCRDUP.CPY |
| COTRN00.bms | COTRN00C | CVTRA05Y, COCOM01Y | COTRN00.CPY |
| COTRN01.bms | COTRN01C | CVTRA05Y, COCOM01Y | COTRN01.CPY |
| COTRN02.bms | COTRN02C | CVTRA05Y, COCOM01Y | COTRN02.CPY |
| CORPT00.bms | CORPT00C | COCOM01Y | CORPT00.CPY |
| COBIL00.bms | COBIL00C | CVACT01Y, CVACT03Y, COCOM01Y | COBIL00.CPY |
| COUSR00.bms | COUSR00C | CSUSR01Y, COCOM01Y | COUSR00.CPY |
| COUSR01.bms | COUSR01C | CSUSR01Y, COCOM01Y | COUSR01.CPY |
| COUSR02.bms | COUSR02C | CSUSR01Y, COCOM01Y | COUSR02.CPY |
| COUSR03.bms | COUSR03C | CSUSR01Y, COCOM01Y | COUSR03.CPY |

---

## JCL Job → Program → File Data Lineage

### Core Processing Jobs

#### POSTTRAN — Transaction Posting
```
POSTTRAN.jcl
  └── STEP15: EXEC PGM=CBTRN02C
        ├── INPUT:
        │     TRANFILE  ← TRANSACT.VSAM.KSDS        (Transaction master)
        │     DALYTRAN  ← DALYTRAN.PS                (Daily transactions)
        │     XREFFILE  ← CARDXREF.VSAM.KSDS        (Card cross-reference)
        │     ACCTFILE  ← ACCTDATA.VSAM.KSDS         (Account master)
        │     TCATBALF  ← TCATBALF.VSAM.KSDS        (Category balances)
        │
        └── OUTPUT:
              TRANFILE  → TRANSACT.VSAM.KSDS        (Updated with new trans)
              TCATBALF  → TCATBALF.VSAM.KSDS        (Updated balances)
              DALYREJS  → DALYREJS(+1)              (Rejected transactions)
```

#### INTCALC — Interest Calculation
```
INTCALC.jcl
  └── STEP15: EXEC PGM=CBACT04C, PARM='2022071800'
        ├── INPUT:
        │     TCATBALF  ← TCATBALF.VSAM.KSDS        (Category balances)
        │     XREFFILE  ← CARDXREF.VSAM.KSDS        (Card cross-reference)
        │     DISCGRP   ← DISCGRP.VSAM.KSDS         (Interest rates)
        │     ACCTFILE  ← ACCTDATA.VSAM.KSDS         (Account master)
        │
        └── OUTPUT:
              ACCTFILE  → ACCTDATA.VSAM.KSDS         (Updated balances)
              TRANSACT  → SYSTRAN(+1)                (Interest transactions)
```

#### TRANBKP — Transaction Backup
```
TRANBKP.jcl
  ├── STEP05R: EXEC PROC=REPROC
  │     └── TRANSACT.VSAM.KSDS → TRANSACT.BKUP(+1)  (Backup to GDG)
  │
  └── STEP10: EXEC PGM=IDCAMS
        └── DELETE TRANSACT.VSAM.KSDS contents       (Clear for next cycle)
```

#### COMBTRAN — Combine Transactions
```
COMBTRAN.jcl
  ├── STEP05: EXEC PGM=SORT
  │     ├── IN: TRANSACT.BKUP(0), SYSTRAN(0)
  │     └── OUT: TRANSACT.COMBINED(+1)               (Merged archive)
  │
  └── STEP10: EXEC PGM=IDCAMS
        └── REPRO TRANSACT.COMBINED(+1) → TRANSACT.VSAM.KSDS
```

#### CREASTMT — Statement Generation
```
CREASTMT.JCL
  ├── DELDEF01: EXEC PGM=IDCAMS
  │     └── Define TRXFL.VSAM.KSDS (working file)
  │
  ├── SORT01: EXEC PGM=SORT
  │     └── TRANSACT.VSAM.KSDS → TRXFL.SEQ (sorted by card+tran)
  │
  ├── REPRO01: EXEC PGM=IDCAMS
  │     └── TRXFL.SEQ → TRXFL.VSAM.KSDS
  │
  └── STMT01: EXEC PGM=CBSTM03A
        ├── INPUT:
        │     TRNXFILE  ← TRXFL.VSAM.KSDS           (Sorted transactions)
        │     ACCTFILE  ← ACCTDATA.VSAM.KSDS         (via CBSTM03B)
        │     CUSTFILE  ← CUSTDATA.VSAM.KSDS         (via CBSTM03B)
        │     XREFFILE  ← CARDXREF.VSAM.KSDS         (via CBSTM03B)
        │
        └── OUTPUT:
              STMTFILE  → STATEMNT.PS                (Statement text)
              HTMLFILE  → STATEMNT.HTML              (Statement HTML)
```

#### TRANREPT — Transaction Report
```
TRANREPT.jcl
  ├── STEP05R: EXEC PROC=REPROC
  │     └── TRANSACT.VSAM.KSDS → sequential (unload)
  │
  ├── STEP05R: EXEC PGM=SORT
  │     └── Sort by card number + transaction ID
  │
  └── STEP10R: EXEC PGM=CBTRN03C
        ├── INPUT:
        │     TRANFILE  ← sorted transaction file
        │     CARDXREF  ← CARDXREF.VSAM.KSDS
        │     TRANTYPE  ← TRANTYPE.VSAM.KSDS
        │     TRANCATG  ← TRANCATG.VSAM.KSDS
        │     DATEPARM  ← date parameters
        │
        └── OUTPUT:
              TRANREPT  → TRANREPT GDG               (Report output)
```

#### CBEXPORT — Data Export
```
CBEXPORT.jcl
  └── STEP01: EXEC PGM=CBEXPORT
        ├── INPUT:
        │     CUSTFILE  ← CUSTDATA.VSAM.KSDS
        │     ACCTFILE  ← ACCTDATA.VSAM.KSDS
        │     XREFFILE  ← CARDXREF.VSAM.KSDS
        │     TRANSACT  ← TRANSACT.VSAM.KSDS
        │     CARDFILE  ← CARDDATA.VSAM.KSDS
        │
        └── OUTPUT:
              EXPFILE   → EXPORT.DATA                (Multi-record export)
```

#### CBIMPORT — Data Import
```
CBIMPORT.jcl
  └── STEP01: EXEC PGM=CBIMPORT
        ├── INPUT:
        │     EXPFILE   ← EXPORT.DATA
        │
        └── OUTPUT:
              CUSTOUT   → CUSTDATA.IMPORT
              ACCTOUT   → ACCTDATA.IMPORT
              XREFOUT   → CARDXREF.IMPORT
              TRNXOUT   → TRANSACT.IMPORT
              CARDOUT   → CARDDATA.IMPORT (implied)
              ERROUT    → IMPORT.ERRORS
```

### Data Loading Jobs

| JCL Job | Input Source | Target VSAM | Operation |
|---------|-------------|-------------|-----------|
| ACCTFILE | ACCTDATA.PS | ACCTDATA.VSAM.KSDS | Delete/Define/REPRO |
| CARDFILE | CARDDATA.PS | CARDDATA.VSAM.KSDS | Delete/Define/REPRO |
| CUSTFILE | CUSTDATA.PS | CUSTDATA.VSAM.KSDS | Delete/Define/REPRO |
| XREFFILE | CARDXREF.PS | CARDXREF.VSAM.KSDS + AIX | Delete/Define/REPRO/BLDINDEX |
| TRANFILE | — | TRANSACT.VSAM.KSDS + AIX | Define cluster + indexes |
| DUSRSECJ | USRSEC.PS | USRSEC.VSAM.KSDS | Delete/Define/REPRO |
| DISCGRP | DISCGRP.PS | DISCGRP.VSAM.KSDS | Delete/Define/REPRO |
| TCATBALF | — | TCATBALF.VSAM.KSDS | Define empty cluster |
| TRANCATG | TRANCATG.PS | TRANCATG.VSAM.KSDS | Delete/Define/REPRO |
| TRANTYPE | TRANTYPE.PS | TRANTYPE.VSAM.KSDS | Delete/Define/REPRO |

### Read/Validation Jobs

| JCL Job | Program | File Read | Purpose |
|---------|---------|-----------|---------|
| READACCT | CBACT01C | ACCTDATA.VSAM.KSDS | Validate + extract account data |
| READCARD | CBACT02C | CARDDATA.VSAM.KSDS | Validate card data |
| READCUST | CBCUS01C | CUSTDATA.VSAM.KSDS | Validate customer data |
| READXREF | CBACT03C | CARDXREF.VSAM.KSDS | Validate cross-reference data |

---

## Batch Cycle Sequence

The nightly batch cycle runs in a strict order with dependencies:

```
Phase 1: Prepare
─────────────────
  CLOSEFIL ──► Close CICS files for exclusive batch access

Phase 2: Data Refresh (parallel-safe)
──────────────────────────────────────
  ACCTFILE ──► Refresh account master
  CARDFILE ──► Refresh card master
  CUSTFILE ──► Refresh customer master
  XREFFILE ──► Refresh cross-reference + rebuild AIX
  DUSRSECJ ──► Refresh user security

Phase 3: Transaction Processing (sequential)
─────────────────────────────────────────────
  POSTTRAN ──► Post daily transactions (CBTRN02C)
       │         Input:  DALYTRAN.PS + CARDXREF + ACCTDATA + TCATBALF
       │         Output: TRANSACT.VSAM (new trans), TCATBALF (updated), DALYREJS
       ▼
  INTCALC  ──► Calculate interest & fees (CBACT04C)
       │         Input:  TCATBALF + CARDXREF + DISCGRP + ACCTDATA
       │         Output: ACCTDATA (updated balances), SYSTRAN (interest trans)
       ▼
  TRANBKP  ──► Backup transaction master to GDG
       │         Input:  TRANSACT.VSAM.KSDS
       │         Output: TRANSACT.BKUP(+1), TRANSACT.VSAM cleared
       ▼
  COMBTRAN ──► Combine backup + system transactions
       │         Input:  TRANSACT.BKUP(0) + SYSTRAN(0)
       │         Output: TRANSACT.COMBINED(+1) → reloaded to TRANSACT.VSAM
       ▼
  CREASTMT ──► Generate account statements
       │         Input:  TRANSACT.VSAM + ACCTDATA + CUSTDATA + CARDXREF
       │         Output: STATEMNT.PS, STATEMNT.HTML

Phase 4: Reporting (can run in parallel with Phase 5)
─────────────────────────────────────────────────────
  TRANREPT ──► Generate transaction detail report
  PRTCATBL ──► Print category balance report

Phase 5: Index Maintenance
──────────────────────────
  TRANIDX  ──► Rebuild alternate indexes on TRANSACT.VSAM

Phase 6: Reopen
───────────────
  OPENFIL  ──► Reopen CICS files for online access
```

---

## VSAM File Access Matrix

Complete view of which programs read (R), write (W), update (U), or delete (D) each VSAM file.

| VSAM File | Online Programs | Batch Programs | JCL Utility |
|-----------|----------------|----------------|-------------|
| **ACCTDATA** | COACTVWC(R), COACTUPC(RU), COTRN02C(R), COBIL00C(RU) | CBACT01C(R), CBACT04C(RU), CBTRN01C(R), CBTRN02C(R), CBSTM03B(R), CBEXPORT(R) | ACCTFILE(load), READACCT(read) |
| **CARDDATA** | COCRDLIC(R), COCRDSLC(R), COCRDUPC(RU), COACTVWC(R), COACTUPC(R) | CBACT02C(R), CBTRN01C(R), CBEXPORT(R) | CARDFILE(load), READCARD(read) |
| **CARDXREF** | COACTVWC(R), COACTUPC(R), COCRDSLC(R), COTRN02C(R), COBIL00C(R) | CBACT04C(R), CBTRN01C(R), CBTRN02C(R), CBTRN03C(R), CBSTM03B(R), CBEXPORT(R) | XREFFILE(load), READXREF(read) |
| **CUSTDATA** | COACTUPC(RU) | CBCUS01C(R), CBTRN01C(R), CBSTM03B(R), CBEXPORT(R) | CUSTFILE(load), READCUST(read) |
| **TRANSACT** | COTRN00C(R), COTRN01C(R), COTRN02C(RW), COBIL00C(W) | CBTRN02C(W), CBTRN03C(R), CBSTM03A/B(R), CBEXPORT(R) | TRANFILE(define), TRANBKP(backup), COMBTRAN(reload) |
| **USRSEC** | COSGN00C(R), COUSR00C(R), COUSR01C(W), COUSR02C(RU), COUSR03C(RD) | — | DUSRSECJ(load) |
| **TCATBALF** | — | CBTRN02C(W), CBACT04C(R) | TCATBALF(define), PRTCATBL(unload) |
| **DISCGRP** | — | CBACT04C(R) | DISCGRP(load) |
| **TRANTYPE** | — | CBTRN03C(R) | TRANTYPE(load) |
| **TRANCATG** | — | CBTRN03C(R) | TRANCATG(load) |
| **DALYTRAN** | — | CBTRN01C(R), CBTRN02C(R) | — (PS file, external feed) |

---

## Cross-Cutting Dependencies

### Shared Infrastructure

| Component | Dependents | Impact of Change |
|-----------|-----------|-----------------|
| COCOM01Y (COMMAREA) | All 17 online programs | Any field change breaks all screen navigation |
| COTTL01Y (Title line) | All 17 online programs | Visual change only |
| CSMSG01Y (Messages) | All 17 online programs | Message display change |
| CEE3ABD (LE Abend) | CBACT02C, CBACT03C, CBCUS01C, CBACT04C | Error handling |
| COBDATFT (Date fmt) | CBACT01C | Date formatting in extracts |
| REPROC.prc (REPRO) | TRANBKP, TRANREPT, PRTCATBL | VSAM unload operations |

### High Fan-In Files (most depended upon)

| VSAM File | # Programs Accessing | Risk Level |
|-----------|---------------------|------------|
| CARDXREF | 11 (6 online + 5 batch) | **Critical** — bridge entity |
| ACCTDATA | 10 (4 online + 6 batch) | **Critical** — financial data |
| TRANSACT | 9 (4 online + 5 batch) | **Critical** — transaction ledger |
| CARDDATA | 7 (5 online + 2 batch) | **High** |
| CUSTDATA | 5 (1 online + 4 batch) | **High** |
| USRSEC | 5 (5 online) | **Medium** — security only |

### High Fan-In Copybooks

| Copybook | # Programs Including | Change Impact |
|----------|---------------------|---------------|
| COCOM01Y | 17+ | **Critical** — breaks all online navigation |
| CVACT01Y | 8+ | **High** — account record layout |
| CVACT03Y | 8+ | **High** — cross-reference layout |
| CVTRA05Y | 7+ | **High** — transaction record layout |
| CSUSR01Y | 6 | **Medium** — user security layout |
| CVACT02Y | 5+ | **Medium** — card record layout |
