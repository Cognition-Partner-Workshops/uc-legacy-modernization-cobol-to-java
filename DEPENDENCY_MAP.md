# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo
>
> This document maps program-to-program calls, program-to-copybook includes, CICS screen navigation flows, and JCL batch job data lineage.

---

## Table of Contents

1. [Online CICS Call Graph](#online-cics-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Program → Copybook Dependency Matrix](#program--copybook-dependency-matrix)
4. [CICS Navigation Flow](#cics-navigation-flow)
5. [JCL Data Lineage](#jcl-data-lineage)
6. [VSAM File Access Map](#vsam-file-access-map)
7. [Batch Job Execution Order](#batch-job-execution-order)

---

## Online CICS Call Graph

Online programs communicate via CICS XCTL (transfer control) and RETURN TRANSID (pseudo-conversational return). They do not use COBOL CALL statements directly.

```
                        ┌──────────┐
                        │ COSGN00C │ (Sign-On, CC00)
                        └────┬─────┘
                             │ XCTL
                    ┌────────┴────────┐
                    ▼                 ▼
              ┌──────────┐     ┌──────────┐
              │ COMEN01C │     │ COADM01C │
              │(Main Menu│     │(Admin    │
              │ CM00)    │     │ Menu CA00│
              └────┬─────┘     └────┬─────┘
                   │                │
         ┌─────┬──┴──┬─────┐      ┌┴──────┬────────┬────────┐
         ▼     ▼     ▼     ▼      ▼       ▼        ▼        ▼
     COACTVWC COACTUPC COCRDLIC COTRN00C COUSR00C COUSR01C COUSR02C COUSR03C
     (Acct    (Acct    (Card    (Tran    (User    (User    (User    (User
      View)   Update)  List)    List)     List)    Add)     Update)  Delete)
         │              │        │
         │              ▼        ▼
         │          COCRDSLC  COTRN01C
         │          (Card     (Tran
         │           View)     View)
         │              │        
         │              ▼        
         │          COCRDUPC  COTRN02C   CORPT00C   COBIL00C
         │          (Card     (Tran      (Report    (Bill
         │           Update)   Add)       Request)   Payment)
         │                       │
         │                       ▼
         │                  CSUTLDTC (Date Validation Utility)
         │
         ▼
    CSUTLDTC (via COPY CSUTLDPY inline code)
```

### Inter-Program Navigation Detail

| Source Program | Navigation Method | Target Program(s)                        | Trigger           |
|---------------|------------------|------------------------------------------|--------------------|
| COSGN00C      | XCTL             | COMEN01C (Regular), COADM01C (Admin)     | Successful login   |
| COMEN01C      | XCTL             | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | Menu selection |
| COADM01C      | XCTL             | COUSR00C, COUSR01C, COUSR02C, COUSR03C   | Admin menu selection |
| COCRDLIC      | XCTL             | COCRDSLC (View), COCRDUPC (Update)        | Row selection      |
| All programs  | RETURN TRANSID   | Returns to originating menu via COMMAREA  | PF3 (Back)         |
| CORPT00C      | CALL             | CSUTLDTC (date validation)               | Date range entry   |
| COTRN02C      | CALL             | CSUTLDTC (date validation)               | Transaction date   |
| COBIL00C      | XCTL             | COMEN01C (return to menu)                | After payment      |

---

## Batch Program Call Graph

Batch programs use standard COBOL `CALL` statements and are invoked by JCL jobs.

```
JCL Jobs                    COBOL Programs              External Calls
─────────                   ──────────────              ──────────────
POSTTRAN.jcl ──────────► CBTRN02C ──────────────────► CEE3ABD (abend handler)
INTCALC.jcl  ──────────► CBACT04C ──────────────────► CEE3ABD (abend handler)
COMBTRAN.jcl ──────────► CBTRN01C ──────────────────► CEE3ABD (abend handler)
CREASTMT.JCL ──────────► CBSTM03A ──────────────────► CBSTM03B (subroutine)
                                   └──────────────────► CEE3ABD (abend handler)
TRANREPT.jcl ──────────► CBTRN03C ──────────────────► CEE3ABD (abend handler)
READACCT.jcl ──────────► CBACT01C ──────────────────► CEE3ABD (abend handler)
READCARD.jcl ──────────► CBACT02C ──────────────────► CEE3ABD (abend handler)
READXREF.jcl ──────────► CBACT03C ──────────────────► CEE3ABD (abend handler)
READCUST.jcl ──────────► CBCUS01C ──────────────────► CEE3ABD (abend handler)
CBEXPORT.jcl ──────────► CBEXPORT ──────────────────► CEE3ABD (abend handler)
CBIMPORT.jcl ──────────► CBIMPORT ──────────────────► CEE3ABD (abend handler)
WAITSTEP.jcl ──────────► COBSWAIT ──────────────────► MVSWAIT (ASM utility)
CORPT00C (online) ─────► TDQ write ─────────────────► TRANREPT.jcl (batch submission)
```

### Direct Program-to-Program Calls

| Caller     | Callee     | Interface              | Purpose                        |
|-----------|-----------|------------------------|-------------------------------|
| CBSTM03A  | CBSTM03B  | WS-M03B-AREA (USING)  | Statement line formatting      |
| CORPT00C  | CSUTLDTC  | CSUTLDTC-DATE (USING)  | Date validation for report range |
| COTRN02C  | CSUTLDTC  | CSUTLDTC-DATE (USING)  | Date validation for new transaction |
| COBSWAIT  | MVSWAIT   | MVSWAIT-TIME (USING)   | System wait/delay              |
| CSUTLDTC  | CEEDAYS   | LE callable service    | Date conversion (Language Environment) |
| Multiple  | CEE3ABD   | ABCODE, TIMING         | Abnormal termination handler   |

---

## Program → Copybook Dependency Matrix

Each cell indicates whether a program includes (COPY) the given copybook.

### Online Programs vs. Data Copybooks

| Copybook    | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|------------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| COCOM01Y   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| COTTL01Y   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| CSDAT01Y   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| CSMSG01Y   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| DFHAID     | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| DFHBMSCA   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |
| CSUSR01Y   | ●        | ●        | ●        | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          | ●        | ●        | ●        | ●        |
| CSMSG02Y   |          |          |          | ●        | ●        |          | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVACT01Y   |          |          |          | ●        | ●        |          |          |          |          |          | ●        |          | ●        |          |          |          |          |
| CVACT02Y   |          |          |          | ●        |          | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVACT03Y   |          |          |          | ●        | ●        |          |          |          |          |          | ●        |          | ●        |          |          |          |          |
| CVCUS01Y   |          |          |          | ●        | ●        |          | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVCRD01Y   |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CVTRA05Y   |          |          |          |          |          |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |
| COMEN02Y   |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| COADM02Y   |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| CSLKPCDY   |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSSETATY   |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSSTRPFY   |          |          |          | ●        | ●        | ●        | ●        | ●        |          |          |          |          |          |          |          |          |          |
| CSUTLDPY   |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |
| CSUTLDWY   |          |          |          |          | ●        |          |          |          |          |          |          |          |          |          |          |          |          |

### Batch Programs vs. Copybooks

| Copybook    | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|------------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| CVACT01Y   |          |          |          | ●        |          | ●        | ●        |          | ●        | ●        | ●        |
| CVACT02Y   |          | ●        |          |          |          | ●        |          |          |          | ●        | ●        |
| CVACT03Y   |          |          | ●        | ●        |          | ●        | ●        | ●        | ●        | ●        | ●        |
| CVCUS01Y   |          |          |          |          | ●        | ●        |          |          |          | ●        | ●        |
| CVTRA05Y   |          |          |          | ●        |          | ●        | ●        | ●        |          | ●        | ●        |
| CVTRA06Y   |          |          |          |          |          | ●        | ●        |          |          |          |          |
| CVTRA01Y   |          |          |          | ●        |          |          | ●        |          |          |          |          |
| CVTRA02Y   |          |          |          | ●        |          |          |          |          |          |          |          |
| CVTRA03Y   |          |          |          |          |          |          |          | ●        |          |          |          |
| CVTRA04Y   |          |          |          |          |          |          |          | ●        |          |          |          |
| CVTRA07Y   |          |          |          |          |          |          |          | ●        |          |          |          |
| CVEXPORT   |          |          |          |          |          |          |          |          |          | ●        | ●        |
| CODATECN   | ●        |          |          |          |          |          |          |          |          |          |          |
| COSTM01    |          |          |          |          |          |          |          |          | ●        |          |          |
| CUSTREC    |          |          |          |          |          |          |          |          | ●        |          |          |

---

## CICS Navigation Flow

### User Login → Main Menu Flow

```
User enters CC00 transaction
        │
        ▼
   ┌─────────┐     ┌─────────┐
   │ COSGN00C├────►│ USRSEC  │ (Read user record)
   └────┬────┘     │  VSAM   │
        │          └─────────┘
        │ (authenticated)
        │
   ┌────┴─────────────────┐
   │  User Type = 'U'     │  User Type = 'A'
   ▼                      ▼
┌─────────┐          ┌─────────┐
│COMEN01C │          │COADM01C │
│Main Menu│          │Adm Menu │
└────┬────┘          └────┬────┘
     │                    │
     ├──1. Account View ──┤──1. User List
     ├──2. Account Update │──2. User Add
     ├──3. Card List      │──3. User Update
     ├──4. Card View      │──4. User Delete
     ├──5. Card Update    │──5. Tran Type List (DB2)
     ├──6. Transaction List──6. Tran Type Maint (DB2)
     ├──7. Transaction View
     ├──8. Transaction Add
     ├──9. Reports
     ├──10. Bill Payment
     └──11. Auth View (optional)
```

---

## JCL Data Lineage

### VSAM File Read/Write Matrix

This shows which JCL jobs and programs read from or write to each VSAM dataset.

| VSAM Dataset                      | Written By (Load/Refresh)    | Read By (Batch)           | Read By (Online)              |
|----------------------------------|------------------------------|---------------------------|-------------------------------|
| `ACCTDATA.VSAM.KSDS`            | ACCTFILE.jcl                 | CBACT01C, CBTRN01C, CBTRN02C, CBACT04C, CBEXPORT | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| `CARDDATA.VSAM.KSDS`            | CARDFILE.jcl                 | CBACT02C, CBTRN01C, CBEXPORT | COCRDLIC, COCRDSLC, COCRDUPC  |
| `CUSTDATA.VSAM.KSDS`            | CUSTFILE.jcl                 | CBCUS01C, CBTRN01C, CBSTM03A, CBEXPORT | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| `CARDXREF.VSAM.KSDS`            | XREFFILE.jcl                 | CBACT03C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT | COCRDLIC, COBIL00C, COTRN02C |
| `TRANSACT.VSAM.KSDS`            | TRANFILE.jcl, CBTRN02C       | CBTRN03C, CBSTM03A, CBTRN01C | COTRN00C, COTRN01C            |
| `DALYTRAN.VSAM.KSDS`            | Online (COTRN02C, COBIL00C)  | CBTRN02C, CBTRN01C        | COTRN00C                      |
| `USRSEC.VSAM.KSDS`              | DUSRSECJ.jcl                 | -                          | COSGN00C, COUSR00C-03C        |
| `TCATBALF.VSAM.KSDS`            | TCATBALF.jcl, CBACT04C       | CBACT04C                   | -                              |
| `DISCGRP.VSAM.KSDS`             | DISCGRP.jcl                  | CBACT04C                   | -                              |
| `TRANTYPE.VSAM.KSDS`            | TRANTYPE.jcl                 | CBTRN03C                   | -                              |
| `TRANCATG.VSAM.KSDS`            | TRANCATG.jcl                 | CBTRN03C                   | -                              |
| `TRANSACT.BKUP(+1)` (GDG)       | TRANBKP.jcl                  | TRANREPT.jcl (SORT)        | -                              |
| `TRANSACT.DALY(+1)` (GDG)       | TRANREPT.jcl (SORT)          | CBTRN03C                   | -                              |
| `STATEMNT.HTML`                  | CBSTM03A                     | -                          | -                              |
| `STATEMNT.PS`                    | CBSTM03A                     | TXT2PDF1.JCL               | -                              |

### Batch Data Flow Diagram

```
                    ┌──────────────┐
                    │  Online CICS │
                    │  (COTRN02C,  │
                    │   COBIL00C)  │
                    └──────┬───────┘
                           │ writes
                           ▼
                    ┌──────────────┐
                    │ DALYTRAN     │ (Daily Transaction VSAM)
                    │ .VSAM.KSDS  │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ POSTTRAN │ │ COMBTRAN │ │ CREASTMT │
        │(CBTRN02C)│ │(CBTRN01C)│ │(CBSTM03A)│
        └────┬─────┘ └────┬─────┘ └────┬─────┘
             │             │            │
             ▼             ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────────┐
        │TRANSACT  │ │TRANSACT  │ │STATEMNT.HTML │
        │.VSAM.KSDS│ │.VSAM.KSDS│ │STATEMNT.PS   │
        └────┬─────┘ └──────────┘ └──────┬───────┘
             │                           │
             ▼                           ▼
        ┌──────────┐              ┌──────────┐
        │ INTCALC  │              │ TXT2PDF1 │
        │(CBACT04C)│              │(PDF conv)│
        └────┬─────┘              └──────────┘
             │
             ▼
        ┌──────────┐
        │ TRANBKP  │ → TRANSACT.BKUP(+1) (GDG backup)
        └────┬─────┘
             │
             ▼
        ┌──────────┐
        │ TRANREPT │ → TRANSACT.DALY(+1) → CBTRN03C → Report
        └──────────┘
```

---

## Batch Job Execution Order

The nightly batch cycle runs in this strict sequence:

```
Step 1: CLOSEFIL    ─── Close CICS files for exclusive batch access
          │
Step 2: Data Refresh (parallel) ─── Reload reference data from sequential files
          ├── ACCTFILE   (Account master)
          ├── CARDFILE   (Card data)
          ├── CUSTFILE   (Customer data)
          ├── XREFFILE   (Cross-reference)
          ├── TRANFILE   (Transaction master reset)
          └── DUSRSECJ   (User security)
          │
Step 3: POSTTRAN    ─── Post daily transactions to master (CBTRN02C)
          │
Step 4: INTCALC     ─── Calculate interest charges (CBACT04C)
          │
Step 5: TRANBKP     ─── Backup transaction file to GDG
          │
Step 6: COMBTRAN    ─── Combine daily + master transactions (CBTRN01C)
          │
Step 7: CREASTMT    ─── Generate account statements (CBSTM03A)
          │
Step 8: TRANREPT    ─── Generate transaction detail report (CBTRN03C)
          │
Step 9: TRANIDX     ─── Rebuild alternate indexes on transaction VSAM
          │
Step 10: OPENFIL   ─── Reopen CICS files for online access
```

**Critical Path**: Steps 3-7 are sequential and cannot be parallelized because each step depends on data from the previous step.

**Recovery Points**: GDG backups at Step 5 allow re-run from Step 5 forward in case of failure.
