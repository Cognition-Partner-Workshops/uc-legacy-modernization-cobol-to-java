# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: CardDemo — Call Graph & Data Lineage
> **Purpose**: Shows which programs call which, which copybooks they include, and which files they read/write

---

## 1. Online Call Graph (CICS Program Navigation)

CardDemo uses CICS `XCTL` (transfer control) to navigate between screens. The COMMAREA carries context between programs.

```
                          ┌─────────────┐
                          │  COSGN00C   │ ← Entry Point (Trans: CC00)
                          │  Signon     │
                          └──────┬──────┘
                                 │ XCTL (based on user type)
                    ┌────────────┴────────────┐
                    ▼                          ▼
             ┌─────────────┐           ┌─────────────┐
             │  COMEN01C   │           │  COADM01C   │
             │  User Menu  │           │  Admin Menu │
             │  (Trans CM00)│           │  (Trans CA00)│
             └──────┬──────┘           └──────┬──────┘
                    │                          │
      ┌──────┬──────┼──────┬──────┐    ┌──────┼──────┬──────┐
      ▼      ▼      ▼      ▼      ▼    ▼      ▼      ▼      ▼
  COACTVWC COACTUPC COCRDLIC COTRN00C  COUSR00C COUSR01C COUSR02C COUSR03C
  Acct View Acct Upd Card List Txn List User List User Add User Upd User Del
      │      │      │      │              
      │      │      ▼      ▼              
      │      │  COCRDSLC COTRN01C        
      │      │  Card View Txn View        
      │      │      │                     
      │      │      ▼                     
      │      │  COCRDUPC                  
      │      │  Card Update               
      │      │                            
      ▼      ▼                            
  CORPT00C COBIL00C COTRN02C             
  Reports  Bill Pay  Txn Add             
```

### Detailed XCTL Navigation Table

| Source Program | Target Program(s)           | Trigger              | Notes                              |
|---------------|----------------------------|----------------------|------------------------------------|
| COSGN00C      | COMEN01C                    | Successful login (User) | Direct XCTL with program literal |
| COSGN00C      | COADM01C                    | Successful login (Admin) | Direct XCTL with program literal |
| COMEN01C      | COSGN00C                    | PF3 (Exit)            | Via CDEMO-TO-PROGRAM variable     |
| COMEN01C      | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | Menu selection | Via CDEMO-MENU-OPT-PGMNAME array |
| COADM01C      | COSGN00C                    | PF3 (Exit)            | Via CDEMO-TO-PROGRAM variable     |
| COADM01C      | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | Menu selection | Via CDEMO-ADMIN-OPT-PGMNAME array |
| COACTVWC      | COMEN01C                    | PF3 (Return)          | Returns to calling program        |
| COACTUPC      | COMEN01C                    | PF3 (Return)          | Returns to calling program        |
| COCRDLIC      | COCRDSLC                    | Select 'S' on row     | View card detail                  |
| COCRDLIC      | COCRDUPC                    | Select 'U' on row     | Update card                       |
| COCRDSLC      | COCRDLIC or COMEN01C        | PF3 (Return)          | Returns to caller                 |
| COCRDUPC      | COCRDLIC or COMEN01C        | PF3 (Return)          | Returns to caller                 |
| COTRN00C      | COTRN01C                    | Select row            | View transaction detail           |
| COTRN01C      | COTRN00C or COMEN01C        | PF3 (Return)          | Returns to caller                 |
| COTRN02C      | COMEN01C                    | PF3 (Return)          | Returns to menu                   |
| CORPT00C      | COMEN01C                    | PF3 (Return)          | Returns to menu                   |
| COBIL00C      | COMEN01C                    | PF3 (Return)          | Returns to menu                   |
| COUSR00C–03C  | COADM01C                    | PF3 (Return)          | Returns to admin menu             |

### CALL Statements (Subroutine Calls)

| Calling Program | Called Program | Call Type    | Purpose                           |
|----------------|---------------|-------------|-----------------------------------|
| CORPT00C       | CSUTLDTC      | CALL         | Validate report date parameters    |
| COTRN02C       | CSUTLDTC      | CALL         | Validate transaction date          |
| CBACT01C       | COBDATFT (ASM)| CALL         | Date format conversion             |
| CBSTM03A       | CBSTM03B      | CALL         | Statement file processing subroutine |
| COBSWAIT       | MVSWAIT (ASM) | CALL         | System wait utility                |
| All batch CB*  | CEE3ABD       | CALL         | LE abend handler (IBM system)      |

---

## 2. Batch Job → Program → File Data Lineage

### 2.1 Core Batch Processing Cycle

```
JCL Job      Program     Reads From (Input)              Writes To (Output)
─────────    ─────────   ────────────────────            ────────────────────
CLOSEFIL  →  SDSF        CICS files (close command)      —
    ↓
POSTTRAN  →  CBTRN02C    DALYTRAN (daily txns)           TRANSACT (master)
                          XREFFILE (cross-ref)             DALYREJS (rejects)
                          ACCTFILE (account master)        ACCTFILE (updated balances)
                          TCATBALF (category bal)          TCATBALF (updated)
    ↓
INTCALC   →  CBACT04C    XREFFILE (cross-ref)            TRANSACT (interest txns)
                          ACCTFILE (account master)        ACCTFILE (updated)
                          DISCGRP  (interest rates)        TCATBALF (updated)
                          TCATBALF (category bal)
    ↓
TRANBKP   →  IDCAMS      TRANSACT                        GDG backup copy
    ↓
COMBTRAN  →  SORT         TRANSACT + new transactions     Combined TRANSACT
    ↓
CREASTMT  →  CBSTM03A    TRNXFILE (sorted transactions)  STMTFILE (statements)
              + CBSTM03B  XREFFILE, CUSTFILE, ACCTFILE    HTMLFILE (HTML stmts)
    ↓
TRANIDX   →  IDCAMS      TRANSACT                        Alternate index rebuild
    ↓
OPENFIL   →  SDSF        —                               CICS files (open command)
```

### 2.2 Detailed File I/O by Batch Program

| Program   | Input Files (READ)                          | Output Files (WRITE)                    |
|-----------|--------------------------------------------|-----------------------------------------|
| CBTRN02C  | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF     | TRANFILE, DALYREJS, ACCTFILE, TCATBALF  |
| CBTRN01C  | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE                         |
| CBTRN03C  | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output)        |
| CBACT04C  | TCATBALF, XREFFILE, ACCTFILE, DISCGRP      | TRANSACT, ACCTFILE, TCATBALF            |
| CBACT01C  | ACCTFILE                                    | OUTFILE, ARRYFILE, VBRCFILE             |
| CBACT02C  | CARDFILE                                    | SYSOUT (print)                          |
| CBACT03C  | XREFFILE                                    | SYSOUT (print)                          |
| CBCUS01C  | CUSTFILE                                    | SYSOUT (print)                          |
| CBSTM03A  | (calls CBSTM03B for file I/O)               | STMTFILE, HTMLFILE                      |
| CBSTM03B  | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE      | (returns data to CBSTM03A)              |
| CBEXPORT  | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (export)                 |
| CBIMPORT  | EXPFILE (import)                            | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

---

## 3. Online CICS Data Access (File I/O)

### 3.1 VSAM File Access by Program

| Program   | VSAM File(s) Accessed           | Operations               | Business Context              |
|-----------|--------------------------------|--------------------------|-------------------------------|
| COSGN00C  | USRSEC                          | READ                     | Authenticate user credentials |
| COMEN01C  | _(none — menu navigation only)_ | —                        | Menu display and routing      |
| COADM01C  | _(none — menu navigation only)_ | —                        | Admin menu display and routing|
| COACTVWC  | ACCTDAT, CARDDAT/CARDAIX, CXACAIX, CUSTDAT | READ ×3    | Read account + card + customer for display |
| COACTUPC  | ACCTDAT, CARDXREF/CXACAIX, CUSTDAT, CARDDAT | READ ×5 (+ REWRITE for update) | Full account update with validation |
| COCRDLIC  | CARDDAT, CARDAIX               | READ ×4, STARTBR, READNEXT, ENDBR | Browse/list cards with paging |
| COCRDSLC  | CARDDAT, CARDAIX               | READ ×2                  | Display single card detail    |
| COCRDUPC  | CARDDAT, CARDAIX               | READ ×2 (+ REWRITE for update) | Update card details    |
| COTRN00C  | TRANSACT                        | READ ×2, STARTBR, READNEXT, ENDBR | Browse/list transactions |
| COTRN01C  | TRANSACT                        | READ ×1                  | Display single transaction    |
| COTRN02C  | TRANSACT, ACCTDAT, CCXREF/CXACAIX | READ ×3, WRITE ×1    | Add new transaction           |
| CORPT00C  | TRANSACT                        | WRITE ×1                 | Write report request record   |
| COBIL00C  | ACCTDAT, CXACAIX, TRANSACT      | READ ×3, WRITE ×1, REWRITE ×1, STARTBR, ENDBR | Bill payment with balance update |
| COUSR00C  | USRSEC                          | READ ×2, STARTBR, READNEXT, ENDBR | Browse/list users   |
| COUSR01C  | USRSEC                          | WRITE ×1                 | Add new user                  |
| COUSR02C  | USRSEC                          | READ ×1, REWRITE ×1     | Update existing user          |
| COUSR03C  | USRSEC                          | READ ×1, DELETE ×1       | Delete user                   |

### 3.2 VSAM File Summary

| VSAM File   | DD Name / FCT  | Key Field         | Programs That Read | Programs That Write/Update |
|------------|---------------|-------------------|--------------------|---------------------------|
| ACCTDAT    | ACCTDAT       | ACCT-ID (9(11))   | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC, COBIL00C |
| CARDDAT    | CARDDAT       | CARD-NUM (X(16))  | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC           |
| CARDAIX    | CARDAIX       | ACCT-ID (alt idx) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | _(via CARDDAT)_    |
| CUSTDAT    | CUSTDAT       | CUST-ID (9(09))   | COACTVWC, COACTUPC, COCRDSLC           | _(batch only)_     |
| TRANSACT   | TRANSACT      | TRAN-ID (X(16))   | COTRN00C, COTRN01C, COBIL00C           | COTRN02C, CORPT00C, COBIL00C |
| USRSEC     | USRSEC        | USR-ID (X(08))    | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C |
| CARDXREF   | CXACAIX       | CARD-NUM (X(16))  | COACTVWC, COACTUPC, COTRN02C, COBIL00C | _(batch only)_     |

---

## 4. Copybook Dependency Matrix

Each `✓` indicates the program includes (`COPY`) this copybook.

### 4.1 Data Record Copybooks

| Copybook  | COSGN | COMEN | COADM | CACTV | CACTU | CCRDL | CCRDS | CCRDU | CTRN0 | CTRN1 | CTRN2 | CRPT0 | CBIL0 | CUSR0-3 |
|-----------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-------:|
| CVACT01Y  |       |       |       |   ✓   |   ✓   |       |       |       |       |       |   ✓   |       |   ✓   |         |
| CVACT02Y  |       |       |       |   ✓   |       |   ✓   |   ✓   |   ✓   |       |       |       |       |       |         |
| CVACT03Y  |       |       |       |   ✓   |   ✓   |       |       |   ✓   |       |       |   ✓   |       |   ✓   |         |
| CVCUS01Y  |       |       |       |   ✓   |   ✓   |       |   ✓   |   ✓   |       |       |       |       |       |         |
| CVCRD01Y  |       |       |       |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |       |       |       |       |       |         |
| CVTRA05Y  |       |       |       |       |       |       |       |       |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |         |

### 4.2 Infrastructure Copybooks

| Copybook  | COSGN | COMEN | COADM | CACTV | CACTU | CCRDL | CCRDS | CCRDU | CTRN0 | CTRN1 | CTRN2 | CRPT0 | CBIL0 | CUSR0-3 |
|-----------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-------:|
| COCOM01Y  |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |    ✓    |
| COTTL01Y  |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |    ✓    |
| CSDAT01Y  |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |    ✓    |
| CSMSG01Y  |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |    ✓    |
| CSUSR01Y  |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |       |       |       |       |       |    ✓    |
| CSMSG02Y  |       |       |       |   ✓   |   ✓   |       |   ✓   |   ✓   |       |       |       |       |       |         |
| CSSTRPFY  |       |       |       |   ✓   |   ✓   |   ✓   |   ✓   |   ✓   |       |       |       |       |       |         |

### 4.3 Batch Program Copybook Usage

| Copybook  | CBTRN01 | CBTRN02 | CBTRN03 | CBACT01 | CBACT04 | CBSTM03A | CBEXPORT | CBIMPORT |
|-----------|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|
| CVACT01Y  |    ✓    |    ✓    |         |    ✓    |    ✓    |     ✓    |     ✓    |     ✓    |
| CVACT02Y  |    ✓    |         |         |         |         |          |     ✓    |     ✓    |
| CVACT03Y  |    ✓    |    ✓    |    ✓    |         |    ✓    |     ✓    |     ✓    |     ✓    |
| CVCUS01Y  |    ✓    |         |         |         |         |          |     ✓    |     ✓    |
| CVTRA05Y  |    ✓    |    ✓    |    ✓    |         |    ✓    |          |     ✓    |     ✓    |
| CVTRA06Y  |    ✓    |    ✓    |         |         |         |          |          |          |
| CVTRA01Y  |         |    ✓    |         |         |    ✓    |          |          |          |
| CVTRA02Y  |         |         |         |         |    ✓    |          |          |          |
| CVTRA03Y  |         |         |    ✓    |         |         |          |          |          |
| CVTRA04Y  |         |         |    ✓    |         |         |          |          |          |
| CVTRA07Y  |         |         |    ✓    |         |         |          |          |          |
| CVEXPORT  |         |         |         |         |         |          |     ✓    |     ✓    |
| COSTM01   |         |         |         |         |         |     ✓    |          |          |
| CUSTREC   |         |         |         |         |         |     ✓    |          |          |
| CODATECN  |         |         |         |    ✓    |         |          |          |          |

---

## 5. JCL Job → VSAM File Data Lineage

### 5.1 Data Refresh Pipeline

```
Sequential Data (app/data/)
        │
        ▼
   ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ ACCTFILE │     │ CARDFILE │     │ CUSTFILE │
   │  (JCL)   │     │  (JCL)   │     │  (JCL)   │
   └────┬─────┘     └────┬─────┘     └────┬─────┘
        │                 │                 │
        ▼                 ▼                 ▼
   ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ ACCTDAT  │     │ CARDDAT  │     │ CUSTDAT  │
   │ (VSAM)   │     │ (VSAM)   │     │ (VSAM)   │
   └──────────┘     └──────────┘     └──────────┘

   ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ TRANFILE │     │ XREFFILE │     │ DUSRSECJ │
   │  (JCL)   │     │  (JCL)   │     │  (JCL)   │
   └────┬─────┘     └────┬─────┘     └────┬─────┘
        │                 │                 │
        ▼                 ▼                 ▼
   ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ TRANSACT │     │ CARDXREF │     │ USRSEC   │
   │ (VSAM)   │     │ (VSAM)   │     │ (VSAM)   │
   └──────────┘     └──────────┘     └──────────┘
```

### 5.2 Complete JCL → File Access Map

| JCL Job    | VSAM/File Inputs                            | VSAM/File Outputs                          |
|-----------|---------------------------------------------|--------------------------------------------|
| ACCTFILE  | ACCTDATA (sequential)                       | ACCTDAT (VSAM, REPRO)                      |
| CARDFILE  | CARDDATA (sequential)                       | CARDDAT (VSAM, REPRO)                      |
| CUSTFILE  | CUSTDATA (sequential)                       | CUSTDAT (VSAM, REPRO)                      |
| TRANFILE  | TRANSACT (sequential)                       | TRANSACT (VSAM, REPRO)                     |
| XREFFILE  | XREFDATA (sequential)                       | CARDXREF (VSAM, REPRO)                     |
| DUSRSECJ  | USRSEC.PS (sequential)                      | USRSEC (VSAM, REPRO)                       |
| POSTTRAN  | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF      | TRANFILE, DALYREJS, ACCTFILE, TCATBALF     |
| INTCALC   | TCATBALF, XREFFILE, ACCTFILE, DISCGRP       | TRANSACT, ACCTFILE, TCATBALF               |
| TRANBKP   | TRANSACT                                    | GDG backup                                  |
| COMBTRAN  | TRANSACT + new txns                         | Combined TRANSACT                           |
| CREASTMT  | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE      | STMTFILE, HTMLFILE                         |
| TRANREPT  | TRANFILE, CARDXREF, TRANTYPE, TRANCATG      | TRANREPT (report)                          |
| CBEXPORT  | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE                              |
| CBIMPORT  | EXPFILE                                     | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT |

---

## 6. End-to-End Transaction Flow

A complete transaction lifecycle from entry to statement:

```
Step 1: ONLINE ENTRY
  User (COTRN02C) → WRITE → TRANSACT (VSAM)
                              ↕
  Bill Pay (COBIL00C) → WRITE → TRANSACT + REWRITE → ACCTDAT

Step 2: BATCH CLOSE
  CLOSEFIL.jcl → SDSF → Closes CICS file access

Step 3: BATCH POST
  POSTTRAN.jcl → CBTRN02C
    READ: DALYTRAN (daily staging)
    READ: CARDXREF (validate card → account mapping)
    READ: ACCTDAT  (get current balances)
    WRITE: TRANSACT (post to master)
    WRITE: DALYREJS (write rejects)
    REWRITE: ACCTDAT (update balances)
    REWRITE: TCATBALF (update category balances)

Step 4: INTEREST CALC
  INTCALC.jcl → CBACT04C
    READ: CARDXREF, ACCTDAT, DISCGRP, TCATBALF
    WRITE: TRANSACT (interest transactions)
    REWRITE: ACCTDAT (add interest to balance)

Step 5: BACKUP
  TRANBKP.jcl → IDCAMS REPRO → GDG generation

Step 6: COMBINE
  COMBTRAN.jcl → SORT/MERGE → Consolidated TRANSACT

Step 7: STATEMENTS
  CREASTMT.jcl → CBSTM03A + CBSTM03B
    READ: Sorted transactions, XREFFILE, CUSTDAT, ACCTDAT
    WRITE: Statement files (text + HTML)

Step 8: REPORTING
  TRANREPT.jcl → CBTRN03C
    READ: TRANSACT, CARDXREF, TRANTYPE, TRANCATG
    WRITE: Daily transaction report

Step 9: BATCH OPEN
  OPENFIL.jcl → SDSF → Reopens CICS file access
```

---

## 7. Menu → Program Wiring

### Regular User Menu (COMEN02Y — 11 options)

| Option | Label                           | Target Program | Status       |
|:------:|--------------------------------|---------------|:------------:|
| 1      | Account View                    | COACTVWC      | Active       |
| 2      | Account Update                  | COACTUPC      | Active       |
| 3      | Credit Card List                | COCRDLIC      | Active       |
| 4      | Credit Card View                | COCRDSLC      | Active       |
| 5      | Credit Card Update              | COCRDUPC      | Active       |
| 6      | Transaction List                | COTRN00C      | Active       |
| 7      | Transaction View                | COTRN01C      | Active       |
| 8      | Transaction Add                 | COTRN02C      | Active       |
| 9      | Transaction Reports             | CORPT00C      | Active       |
| 10     | Bill Payment                    | COBIL00C      | Active       |
| 11     | Pending Authorization View      | COPAUS0C      | Optional*    |

### Admin Menu (COADM02Y — 6 options)

| Option | Label                            | Target Program | Status       |
|:------:|----------------------------------|---------------|:------------:|
| 1      | User List (Security)             | COUSR00C      | Active       |
| 2      | User Add (Security)              | COUSR01C      | Active       |
| 3      | User Update (Security)           | COUSR02C      | Active       |
| 4      | User Delete (Security)           | COUSR03C      | Active       |
| 5      | Transaction Type List/Update (DB2)| COTRTLIC     | Optional*    |
| 6      | Transaction Type Maintenance (DB2)| COTRTUPC     | Optional*    |

_*Optional programs require the IMS/DB2/MQ modules to be installed._
