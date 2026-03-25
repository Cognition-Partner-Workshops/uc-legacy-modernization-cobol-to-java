# Dependency Map - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Source:** Static analysis of CALL, XCTL, COPY, EXEC CICS, and JCL DD statements

---

## 1. Online CICS Program Call Graph

The online programs communicate via **EXEC CICS XCTL** (transfer control) using the COMMAREA (`COCOM01Y`). The sign-on program is the entry point; all navigation flows through the menu programs.

### 1.1 Navigation Flow (ASCII Diagram)

```
                            ┌─────────────┐
                            │  COSGN00C   │  (Sign-On, Txn CC00)
                            │  Login      │
                            └──────┬──────┘
                                   │ XCTL
                      ┌────────────┴────────────┐
                      │                         │
               ┌──────┴──────┐           ┌──────┴──────┐
               │  COMEN01C   │           │  COADM01C   │
               │  Main Menu  │           │  Admin Menu │
               │  (Regular)  │           │  (Admin)    │
               └──────┬──────┘           └──────┬──────┘
                      │                         │
        ┌─────┬───┬───┼───┬───┬───┬───┐   ┌────┼────┬────┬────┬────┐
        │     │   │   │   │   │   │   │   │    │    │    │    │    │
        v     v   v   v   v   v   v   v   v    v    v    v    v    v
     COACTVWC │ COCRDLIC │ COTRN00C │ COBIL00C COUSR00C │ COUSR02C │
     AcctView │ CardList │ TranList │ BillPay  UsrList  │ UsrUpd   │
              v          v          v                   v          v
           COACTUPC   COCRDSLC   COTRN01C            COUSR01C   COUSR03C
           AcctUpd    CardView   TranView            UsrAdd     UsrDel
                         │
                         v
                      COCRDUPC
                      CardUpd

     Additional from Main Menu:
     COTRN02C (Transaction Add) ──CALL──> CSUTLDTC (Date Utility)
     CORPT00C (Reports)         ──CALL──> CSUTLDTC (Date Utility)
```

### 1.2 Detailed XCTL Transfer Table

| Source Program | Target Program        | Trigger / Condition                          |
|----------------|-----------------------|----------------------------------------------|
| COSGN00C       | COMEN01C              | Successful login (user type = `U`)           |
| COSGN00C       | COADM01C              | Successful login (user type = `A`)           |
| COMEN01C       | COACTVWC              | Menu option 1 - Account View                 |
| COMEN01C       | COACTUPC              | Menu option 2 - Account Update               |
| COMEN01C       | COCRDLIC              | Menu option 3 - Credit Card List             |
| COMEN01C       | COCRDSLC              | Menu option 4 - Credit Card View             |
| COMEN01C       | COCRDUPC              | Menu option 5 - Credit Card Update           |
| COMEN01C       | COTRN00C              | Menu option 6 - Transaction List             |
| COMEN01C       | COTRN01C              | Menu option 7 - Transaction View             |
| COMEN01C       | COTRN02C              | Menu option 8 - Transaction Add              |
| COMEN01C       | CORPT00C              | Menu option 9 - Transaction Reports          |
| COMEN01C       | COBIL00C              | Menu option 10 - Bill Payment                |
| COMEN01C       | COPAUS0C              | Menu option 11 - Pending Auth View (Ext.)    |
| COADM01C       | COUSR00C              | Admin option 1 - User List                   |
| COADM01C       | COUSR01C              | Admin option 2 - User Add                    |
| COADM01C       | COUSR02C              | Admin option 3 - User Update                 |
| COADM01C       | COUSR03C              | Admin option 4 - User Delete                 |
| COADM01C       | COTRTLIC              | Admin option 5 - Tran Type List (Ext. DB2)   |
| COADM01C       | COTRTUPC              | Admin option 6 - Tran Type Maint (Ext. DB2)  |
| COACTVWC       | COMEN01C / caller     | PF3 - Return to caller                       |
| COACTUPC       | COMEN01C / caller     | PF3 - Return to caller                       |
| COCRDLIC       | COMEN01C              | PF3 - Return to menu                         |
| COCRDLIC       | COCRDSLC              | Select card - View detail                    |
| COCRDLIC       | COCRDUPC              | Select card + PF5 - Update                   |
| COCRDSLC       | COMEN01C / caller     | PF3 - Return to caller                       |
| COCRDUPC       | COMEN01C / caller     | PF3 - Return to caller                       |
| COTRN00C       | COTRN01C              | Select transaction - View detail             |
| COTRN00C       | COMEN01C              | PF3 - Return to menu                         |
| COTRN01C       | COMEN01C / caller     | PF3 - Return to caller                       |
| COTRN02C       | COMEN01C / caller     | PF3 - Return to caller                       |
| CORPT00C       | COMEN01C / caller     | PF3 - Return to caller                       |
| COBIL00C       | COMEN01C / caller     | PF3 - Return to caller                       |
| COUSR00C       | COADM01C              | PF3 - Return to admin menu                   |
| COUSR01C       | COADM01C / caller     | PF3 - Return to caller                       |
| COUSR02C       | COADM01C / caller     | PF3 - Return to caller                       |
| COUSR03C       | COADM01C / caller     | PF3 - Return to caller                       |

### 1.3 CALL Dependencies (Subroutine Calls)

| Caller Program | Called Program | Interface / USING clause          | Purpose                        |
|----------------|---------------|-----------------------------------|--------------------------------|
| COTRN02C       | CSUTLDTC      | `CSUTLDTC-DATE`                   | Validate transaction dates     |
| CORPT00C       | CSUTLDTC      | `CSUTLDTC-DATE`                   | Validate report date range     |
| CBSTM03A       | CBSTM03B      | `WS-M03B-AREA`                    | Statement file I/O (12 calls)  |
| CBACT01C       | COBDATFT (asm)| `CODATECN-REC`                    | Format dates for output        |
| COBSWAIT       | MVSWAIT (asm) | `MVSWAIT-TIME`                    | Assembler wait routine         |
| CSUTLDTC       | CEEDAYS (LE)  | LE date services                  | Validate date via LE runtime   |
| Multiple batch | CEE3ABD (LE)  | `ABCODE, TIMING`                  | Abnormal termination (abend)   |

**Programs calling CEE3ABD (abend handler):** CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT

---

## 2. Batch Program Call Graph

### 2.1 Batch Processing Chain

The daily batch cycle runs in this sequence (orchestrated by JCL and scheduler):

```
 CLOSEFIL ─── Close CICS files for batch access
     │
     ├── Data Refresh Jobs (parallel)
     │   ├── ACCTFILE  ─── Refresh Account VSAM
     │   ├── CARDFILE  ─── Refresh Card VSAM
     │   ├── CUSTFILE  ─── Refresh Customer VSAM
     │   ├── XREFFILE  ─── Refresh Cross-Reference VSAM
     │   └── TRANFILE  ─── Load daily transaction input
     │
     v
 POSTTRAN ──── Post transactions (CBTRN02C)
     │          Reads: Daily trans, Xref, Account, Card, Tran Type, Tran Cat
     │          Writes: Transaction master, Tran Category Balance, Daily reject
     │          Updates: Account (balance, cycle credits/debits)
     v
 INTCALC ───── Calculate interest (CBACT04C)
     │          Reads: Account, Tran Cat Balance, Disclosure Group
     │          Writes: Transaction master (interest charges)
     │          Updates: Account (balance with interest)
     v
 TRANBKP ───── Backup transaction master
     │          REPRO to GDG backup, then delete original
     v
 COMBTRAN ──── Combine/merge transactions
     │          Merges daily transactions into master
     v
 CREASTMT ──── Generate statements (CBSTM03A -> CBSTM03B)
     │          Reads: Xref, Account, Customer, Transaction
     │          Writes: Statement output file (GDG)
     v
 TRANREPT ──── Generate reports (CBTRN03C)
     │          Reads: Transaction master, Tran Type, Tran Cat
     │          Writes: Report output file (GDG)
     v
 TRANIDX ───── Rebuild alternate index on Transaction
     v
 OPENFIL ───── Re-open CICS files for online access
```

### 2.2 Batch Program-to-Program Call Chain

```
POSTTRAN (JCL)
  └── CBTRN02C (COBOL) ──CALL──> CEE3ABD (LE abend)

INTCALC (JCL)
  └── CBACT04C (COBOL) ──CALL──> CEE3ABD (LE abend)

CREASTMT (JCL)
  └── CBSTM03A (COBOL) ──CALL──> CBSTM03B (COBOL, 12 call sites)
                         ──CALL──> CEE3ABD  (LE abend)

TRANREPT (JCL)
  └── Uses TRANREPT.prc (procedure)
      └── CBTRN03C (COBOL) ──CALL──> CEE3ABD (LE abend)

READACCT (JCL)
  └── CBACT01C (COBOL) ──CALL──> COBDATFT (ASM, date formatting)
                        ──CALL──> CEE3ABD  (LE abend)

READCARD (JCL) -> CBACT02C
READXREF (JCL) -> CBACT03C
READCUST (JCL) -> CBCUS01C

CBEXPORT (JCL) -> CBEXPORT (COBOL) ──CALL──> CEE3ABD
CBIMPORT (JCL) -> CBIMPORT (COBOL) ──CALL──> CEE3ABD

WAITSTEP (JCL) -> COBSWAIT (COBOL) ──CALL──> MVSWAIT (ASM)
```

---

## 3. Copybook Inclusion Map

### 3.1 Which Programs Include Which Copybooks

| Copybook     | Included By (Programs)                                                              |
|-------------|--------------------------------------------------------------------------------------|
| **COCOM01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** | COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDUPC, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CVACT01Y** | COACTVWC, COACTUPC, COTRN02C, CBACT01C, CBACT04C                                   |
| **CVACT02Y** | COACTVWC, COACTUPC, COCRDLIC, CBACT02C                                              |
| **CVACT03Y** | COACTVWC, COACTUPC, COTRN02C, CBACT03C, CBACT04C                                   |
| **CVCUS01Y** | COACTVWC, COACTUPC                                                                   |
| **CVCRD01Y** | COCRDLIC                                                                              |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CBACT04C                                              |
| **CVTRA01Y** | CBACT04C                                                                              |
| **CVTRA02Y** | CBACT04C                                                                              |
| **COADM02Y** | COADM01C                                                                              |
| **COMEN02Y** | COMEN01C                                                                              |
| **CODATECN** | CBACT01C                                                                              |
| **CSSTRPFY** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC                                    |
| **DFHAID**   | All online CICS programs (system copybook - AID byte definitions)                    |
| **DFHBMSCA** | All online CICS programs (system copybook - BMS attribute constants)                 |

### 3.2 Copybook Fan-Out (Impact Analysis)

Changing a copybook affects all programs that include it. Sorted by impact:

| Copybook     | # Programs Affected | Risk Level |
|-------------|--------------------:|------------|
| COCOM01Y    |                  16 | **Critical** - Changes affect all online programs |
| COTTL01Y    |                  14 | High - Screen title/header structure |
| CSDAT01Y    |                  14 | High - Date/time working storage |
| CSMSG01Y    |                  14 | High - Common messages |
| CSUSR01Y    |                  10 | High - User security record |
| CVACT01Y    |                   5 | Medium - Account record |
| CVACT02Y    |                   4 | Medium - Card record |
| CVACT03Y    |                   5 | Medium - Cross-reference record |
| CVTRA05Y    |                   4 | Medium - Transaction record |
| CVCUS01Y    |                   2 | Low - Customer record |

---

## 4. Data Lineage - VSAM File Access

### 4.1 File Access by Online Programs

| Program    | VSAM Files Accessed                                  | Operations          |
|-----------|------------------------------------------------------|---------------------|
| COSGN00C  | USRSEC                                                | Read                |
| COACTVWC  | ACCTFILE, CARDFILE, XREFFILE, CUSTFILE               | Read                |
| COACTUPC  | ACCTFILE, CARDFILE, XREFFILE, CUSTFILE               | Read, Update        |
| COCRDLIC  | CARDFILE, ACCTFILE                                    | Read                |
| COCRDSLC  | CARDFILE, ACCTFILE, XREFFILE, CUSTFILE               | Read                |
| COCRDUPC  | CARDFILE, ACCTFILE, XREFFILE, CUSTFILE               | Read, Update        |
| COTRN00C  | TRANSACT                                              | Read (browse)       |
| COTRN01C  | TRANSACT                                              | Read                |
| COTRN02C  | TRANSACT, ACCTFILE, XREFFILE                         | Read, Write         |
| CORPT00C  | (submits batch job - no direct file access)           | -                   |
| COBIL00C  | ACCTFILE, TRANSACT                                    | Read, Write, Update |
| COUSR00C  | USRSEC                                                | Read (browse)       |
| COUSR01C  | USRSEC                                                | Write               |
| COUSR02C  | USRSEC                                                | Read, Update        |
| COUSR03C  | USRSEC                                                | Read, Delete        |

### 4.2 File Access by Batch Programs

| Program    | Input Files (Read)                                    | Output Files (Write/Update)         |
|-----------|-------------------------------------------------------|--------------------------------------|
| CBACT01C  | ACCTFILE                                               | Multiple output files (split)       |
| CBACT02C  | CARDFILE                                               | SYSOUT (print)                      |
| CBACT03C  | XREFFILE                                               | SYSOUT (print)                      |
| CBACT04C  | ACCTFILE, XREFFILE, TCATBALF, DISCGRP, TRANSACT       | ACCTFILE (update), TRANSACT (write) |
| CBCUS01C  | CUSTFILE                                               | SYSOUT (print)                      |
| CBTRN01C  | Daily transaction file                                 | TRANSACT (write)                    |
| CBTRN02C  | Daily trans, XREFFILE, ACCTFILE, TRANTYPE, TRANCATG   | TRANSACT, TCATBALF, DALYREJS, ACCTFILE |
| CBTRN03C  | TRANSACT, TRANTYPE, TRANCATG                          | Report output (GDG)                 |
| CBSTM03A  | XREFFILE, ACCTFILE, CUSTFILE, TRANSACT                | Statement output (GDG)              |
| CBSTM03B  | (called by CBSTM03A - performs file I/O)              | Statement output (GDG)              |
| CBEXPORT  | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANSACT      | Export sequential file              |
| CBIMPORT  | Import sequential file                                 | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANSACT |

### 4.3 File Access Matrix (Heatmap)

```
                 ACCT  CARD  CUST  XREF  TRAN  USRSEC TCATBAL DISCGRP TRANTYP TRANCAT
                 FILE  FILE  FILE  FILE  ACT          F               E       G
Online:
  COSGN00C        .     .     .     .     .     R       .       .       .       .
  COACTVWC        R     R     R     R     .     .       .       .       .       .
  COACTUPC       R/U    R     R     R     .     .       .       .       .       .
  COCRDLIC        R     R     .     .     .     .       .       .       .       .
  COCRDSLC        R     R     R     R     .     .       .       .       .       .
  COCRDUPC        R     R/U   R     R     .     .       .       .       .       .
  COTRN00C        .     .     .     .     R     .       .       .       .       .
  COTRN01C        .     .     .     .     R     .       .       .       .       .
  COTRN02C        R     .     .     R    R/W    .       .       .       .       .
  COBIL00C       R/U    .     .     .    R/W    .       .       .       .       .
  COUSR00C        .     .     .     .     .     R       .       .       .       .
  COUSR01C        .     .     .     .     .     W       .       .       .       .
  COUSR02C        .     .     .     .     .    R/U      .       .       .       .
  COUSR03C        .     .     .     .     .    R/D      .       .       .       .

Batch:
  CBACT01C        R     .     .     .     .     .       .       .       .       .
  CBACT02C        .     R     .     .     .     .       .       .       .       .
  CBACT03C        .     .     .     R     .     .       .       .       .       .
  CBACT04C       R/U    .     .     R     R/W   .       R       R       .       .
  CBCUS01C        .     .     R     .     .     .       .       .       .       .
  CBTRN02C       R/U    .     .     R     R/W   .      R/W      .       R       R
  CBTRN03C        .     .     .     .     R     .       .       .       R       R
  CBSTM03A        R     .     R     R     R     .       .       .       .       .
  CBEXPORT        R     R     R     R     R     .       .       .       .       .
  CBIMPORT       R/W   R/W   R/W   R/W   R/W   .       .       .       .       .

Legend: R=Read, W=Write, U=Update, D=Delete, .=No access
```

---

## 5. JCL Job-to-Program Mapping

### 5.1 Which JCL Runs Which Program

| JCL Job       | COBOL Program(s) Executed  | Key DD Names (VSAM Files)                       |
|--------------|----------------------------|-------------------------------------------------|
| POSTTRAN     | CBTRN02C                   | DALYTRAN, XREFFILE, ACCTFILE, TRANSACT, TCATBALF, DALYREJS, TRANTYPE, TRANCATG |
| INTCALC      | CBACT04C                   | ACCTFILE, XREFFILE, TCATBALF, DISCGRP, TRANSACT |
| CREASTMT     | CBSTM03A                   | XREFFILE, ACCTFILE, CUSTFILE, TRANSACT, STMTFILE |
| TRANREPT     | CBTRN03C (via TRANREPT.prc)| TRANSACT, TRANTYPE, TRANCATG, RPTFILE           |
| READACCT     | CBACT01C                   | ACCTFILE, output files                           |
| READCARD     | CBACT02C                   | CARDFILE, SYSOUT                                 |
| READCUST     | CBCUS01C                   | CUSTFILE, SYSOUT                                 |
| READXREF     | CBACT03C                   | XREFFILE, SYSOUT                                 |
| COMBTRAN     | CBTRN01C                   | DALYTRAN, TRANSACT                               |
| CBEXPORT     | CBEXPORT                   | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANSACT, EXPFILE |
| CBIMPORT     | CBIMPORT                   | IMPFILE, ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANSACT |
| WAITSTEP     | COBSWAIT                   | (none - utility)                                 |

### 5.2 VSAM File Definition JCL (no COBOL program)

These JCL jobs use IDCAMS to define/load VSAM clusters:

| JCL Job      | VSAM File Defined/Loaded        | Key Operation                     |
|-------------|----------------------------------|-----------------------------------|
| ACCTFILE    | Account master KSDS              | DELETE/DEFINE/REPRO from flat file|
| CARDFILE    | Card master KSDS                 | DELETE/DEFINE/REPRO               |
| CUSTFILE    | Customer master KSDS             | DELETE/DEFINE/REPRO               |
| XREFFILE    | Card cross-reference KSDS        | DELETE/DEFINE/REPRO               |
| TRANFILE    | Transaction master KSDS          | DELETE/DEFINE/REPRO               |
| DUSRSECJ    | User security KSDS               | DELETE/DEFINE/REPRO               |
| TRANTYPE    | Transaction type KSDS            | DELETE/DEFINE/REPRO               |
| TRANCATG    | Transaction category KSDS        | DELETE/DEFINE/REPRO               |
| TCATBALF    | Transaction category balance KSDS| DELETE/DEFINE                     |
| DISCGRP     | Disclosure group KSDS            | DELETE/DEFINE/REPRO               |
| DALYREJS    | Daily rejection sequential       | DELETE/DEFINE                     |
| REPTFILE    | Report file GDG base             | DEFINE GDG                        |
| DEFGDGB     | Backup GDG base                  | DEFINE GDG                        |
| DEFGDGD     | Daily data GDG base              | DEFINE GDG                        |
| TRANIDX     | Alternate index on TRANSACT      | DEFINE AIX/PATH, BLDINDEX        |
| TRANBKP     | Transaction backup               | REPRO to GDG, DELETE source       |
| DEFCUST     | Customer VSAM cluster            | DELETE/DEFINE                     |
| ESDSRRDS    | ESDS/RRDS example clusters       | DELETE/DEFINE                     |

---

## 6. Extension Module Dependencies

### 6.1 Authorization Module (IMS/DB2/MQ)

```
COPAUA0C (MQ Trigger)
  ├── Reads: MQ Queue (authorization requests)
  ├── Writes: IMS DB (pending authorizations)
  └── Calls: IMS DL/I

COPAUS0C (Summary View) ──XCTL──> COPAUS1C (Detail View)
  └── Reads: IMS DB (pending auth summary)

COPAUS1C (Detail View) ──XCTL──> COPAUS2C (Fraud Mark)
  └── Reads: IMS DB (pending auth detail)

COPAUS2C (Fraud Mark)
  └── Writes: DB2 table (fraud flag)

CBPAUP0C (Batch Purge)
  └── Deletes: IMS DB (old pending auths)
```

### 6.2 Transaction Type DB2 Module

```
COTRTLIC (List/Delete) ──XCTL──> COTRTUPC (Add/Edit)
  ├── Reads: DB2 TRAN_TYPE table (cursor)
  └── Deletes: DB2 TRAN_TYPE table

COTRTUPC (Add/Edit)
  ├── Inserts: DB2 TRAN_TYPE table
  └── Updates: DB2 TRAN_TYPE table

COBTUPDT (Batch Update)
  └── Updates: DB2 TRAN_TYPE table (batch)
```

### 6.3 VSAM-MQ Module

```
CODATE01 ─── MQ request/response ─── Returns system date
COACCT01 ─── MQ request/response ─── Account inquiry from VSAM ACCTFILE
```

---

## 7. Scheduler Dependencies

### 7.1 Batch Job Execution Order

From `app/scheduler/CardDemo.ca7` and `CardDemo.controlm`:

```
Step 1: CLOSEFIL          (no predecessors - triggered by schedule)
Step 2: ACCTFILE           ── depends on ── CLOSEFIL
Step 2: CARDFILE           ── depends on ── CLOSEFIL
Step 2: CUSTFILE           ── depends on ── CLOSEFIL
Step 2: XREFFILE           ── depends on ── CLOSEFIL
Step 2: TRANFILE           ── depends on ── CLOSEFIL
Step 3: POSTTRAN           ── depends on ── ACCTFILE, CARDFILE, XREFFILE, TRANFILE
Step 4: INTCALC            ── depends on ── POSTTRAN
Step 5: TRANBKP            ── depends on ── INTCALC
Step 6: COMBTRAN           ── depends on ── TRANBKP
Step 7: CREASTMT           ── depends on ── COMBTRAN, CUSTFILE
Step 7: TRANREPT           ── depends on ── COMBTRAN
Step 8: TRANIDX            ── depends on ── CREASTMT, TRANREPT
Step 9: OPENFIL            ── depends on ── TRANIDX
```

### 7.2 Critical Path

The longest dependency chain determines minimum batch window:

```
CLOSEFIL → TRANFILE → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
   |           |          |          |          |          |           |          |         |
  (fast)    (medium)   (HEAVY)   (HEAVY)    (medium)   (medium)    (HEAVY)   (fast)    (fast)
```

**Bottleneck programs:** CBTRN02C (transaction posting), CBACT04C (interest calc), CBSTM03A (statement generation) - these dominate batch runtime.

---

## 8. Shared Resource Contention

### 8.1 Files with Both Online and Batch Access

| VSAM File  | Online Programs (CICS)                    | Batch Programs             | Contention Risk |
|------------|-------------------------------------------|----------------------------|-----------------|
| ACCTFILE   | COACTVWC, COACTUPC, COTRN02C, COBIL00C  | CBACT01C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT | **High** |
| CARDFILE   | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC  | CBACT02C, CBEXPORT, CBIMPORT | Medium |
| CUSTFILE   | COACTVWC, COACTUPC, COCRDSLC            | CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT | Medium |
| XREFFILE   | COACTVWC, COACTUPC, COTRN02C            | CBACT03C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT | **High** |
| TRANSACT   | COTRN00C, COTRN01C, COTRN02C, COBIL00C  | CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C, CBSTM03A, CBEXPORT, CBIMPORT | **Critical** |
| USRSEC     | COSGN00C, COUSR00C-03C                   | (none)                      | Low |

**Note:** CLOSEFIL/OPENFIL jobs manage this contention by closing CICS file access during the batch window.
