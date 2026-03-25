# CardDemo Dependency Map

> **System**: CardDemo -- Credit Card Management System
> **Date**: 2026-03-25

---

## Table of Contents

1. [Online Program Call Graph](#online-program-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Online CICS File Access Map](#online-cics-file-access-map)
4. [Batch File I/O Map](#batch-file-io-map)
5. [JCL Job-to-Program Map](#jcl-job-to-program-map)
6. [Batch Processing Sequence](#batch-processing-sequence)
7. [Copybook Inclusion Map](#copybook-inclusion-map)
8. [Data Lineage](#data-lineage)

---

## Online Program Call Graph

Programs communicate via `EXEC CICS XCTL` (transfer control) passing `CARDDEMO-COMMAREA` (defined in `COCOM01Y.cpy`). The COMMAREA carries user context, navigation state, and selected entity IDs between programs.

### Navigation Flow (ASCII diagram)

```
                              +-----------+
                              | COSGN00C  |
                              | (Sign-On) |
                              +-----+-----+
                                    |
                      +-------------+-------------+
                      | User Type='U'             | User Type='A'
                      v                           v
               +------------+              +------------+
               | COMEN01C   |              | COADM01C   |
               | (Main Menu)|              | (Admin Menu)|
               +-----+------+              +-----+------+
                     |                            |
     +---+---+---+---+---+---+---+---+      +----+----+----+----+
     |   |   |   |   |   |   |   |   |      |    |    |    |    |
     v   v   v   v   v   v   v   v   v      v    v    v    v    v
    AV  AU  CL  CS  CU  TL  TV  TA  RP    UL   UA   UU   UD  (DB2)
```

### Detailed XCTL Transfer Map

| Source Program | Target Program(s) | Mechanism | Condition |
|---|---|---|---|
| **COSGN00C** (Sign-On) | `COMEN01C` | XCTL PROGRAM('COMEN01C') | User type = 'U' (regular) |
| **COSGN00C** (Sign-On) | `COADM01C` | XCTL PROGRAM('COADM01C') | User type = 'A' (admin) |
| **COMEN01C** (Main Menu) | `COACTVWC` | XCTL via menu table | Option 1: Account View |
| **COMEN01C** (Main Menu) | `COACTUPC` | XCTL via menu table | Option 2: Account Update |
| **COMEN01C** (Main Menu) | `COCRDLIC` | XCTL via menu table | Option 3: Credit Card List |
| **COMEN01C** (Main Menu) | `COCRDSLC` | XCTL via menu table | Option 4: Credit Card View |
| **COMEN01C** (Main Menu) | `COCRDUPC` | XCTL via menu table | Option 5: Credit Card Update |
| **COMEN01C** (Main Menu) | `COTRN00C` | XCTL via menu table | Option 6: Transaction List |
| **COMEN01C** (Main Menu) | `COTRN01C` | XCTL via menu table | Option 7: Transaction View |
| **COMEN01C** (Main Menu) | `COTRN02C` | XCTL via menu table | Option 8: Transaction Add |
| **COMEN01C** (Main Menu) | `CORPT00C` | XCTL via menu table | Option 9: Reports |
| **COMEN01C** (Main Menu) | `COBIL00C` | XCTL via menu table | Option 10: Bill Payment |
| **COMEN01C** (Main Menu) | `COPAUS0C` | XCTL via menu table | Option 11: Pending Auth View (optional module) |
| **COADM01C** (Admin Menu) | `COUSR00C` | XCTL via admin menu table | Option 1: User List |
| **COADM01C** (Admin Menu) | `COUSR01C` | XCTL via admin menu table | Option 2: User Add |
| **COADM01C** (Admin Menu) | `COUSR02C` | XCTL via admin menu table | Option 3: User Update |
| **COADM01C** (Admin Menu) | `COUSR03C` | XCTL via admin menu table | Option 4: User Delete |
| **COADM01C** (Admin Menu) | `COTRTLIC` | XCTL via admin menu table | Option 5: Tran Type List (DB2 module) |
| **COADM01C** (Admin Menu) | `COTRTUPC` | XCTL via admin menu table | Option 6: Tran Type Maint (DB2 module) |
| **COCRDLIC** (Card List) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COCRDLIC** (Card List) | `COCRDSLC` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | Select card: View details |
| **COCRDLIC** (Card List) | `COCRDUPC` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | Select card: Update |
| **COCRDSLC** (Card View) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COCRDUPC** (Card Update) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COACTVWC** (Account View) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COACTUPC** (Account Update) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COTRN00C** (Transaction List) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COTRN00C** (Transaction List) | `COTRN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | Select transaction |
| **COTRN01C** (Transaction View) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COTRN02C** (Transaction Add) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **CORPT00C** (Reports) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COBIL00C** (Bill Payment) | `COMEN01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to menu |
| **COUSR00C** (User List) | `COADM01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to admin menu |
| **COUSR00C** (User List) | `COUSR02C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | Select user: Update |
| **COUSR00C** (User List) | `COUSR03C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | Select user: Delete |
| **COUSR01C** (User Add) | `COADM01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to admin menu |
| **COUSR02C** (User Update) | `COADM01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to admin menu |
| **COUSR03C** (User Delete) | `COADM01C` | XCTL PROGRAM(CDEMO-TO-PROGRAM) | PF3: Return to admin menu |

### CALL Dependencies (Subroutine calls)

| Caller | Callee | Mechanism | Purpose |
|---|---|---|---|
| `COTRN02C` | `CSUTLDTC` | `CALL 'CSUTLDTC'` | Date validation for transaction dates |
| `CORPT00C` | `CSUTLDTC` | `CALL 'CSUTLDTC'` | Date validation for report date range |
| `CSUTLDTC` | `CEEDAYS` | `CALL 'CEEDAYS'` | LE date conversion (system routine) |

---

## Batch Program Call Graph

Batch programs are invoked by JCL jobs. They use standard COBOL file I/O (OPEN/READ/WRITE/CLOSE) rather than CICS commands.

### CALL Dependencies

| Caller | Callee | Mechanism | Purpose |
|---|---|---|---|
| `CBSTM03A` | `CBSTM03B` | `CALL 'CBSTM03B'` | File handling for statement generation (reads transactions, customers, accounts, cross-references) |
| `CBSTM03A` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `COBSWAIT` | `MVSWAIT` (ASM) | `CALL 'MVSWAIT'` | Timed wait via assembler STIMER |
| `CBACT01C` | `COBDATFT` (ASM) | `CALL 'COBDATFT'` | Date formatting via assembler routine |
| `CBTRN01C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination (abend handler) |
| `CBTRN02C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBTRN03C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBACT01C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBACT02C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBACT03C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBACT04C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBCUS01C` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBEXPORT` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |
| `CBIMPORT` | `CEE3ABD` | `CALL 'CEE3ABD'` | LE abnormal termination |

---

## Online CICS File Access Map

Online programs access VSAM files via `EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV`.

| Program | ACCTFILE | CARDFILE | CUSTFILE | XREFFILE | TRANSACT | USRSEC | TCATBALF | TRANTYPE | TRANCATG | DISCGRP |
|---|---|---|---|---|---|---|---|---|---|---|
| **COSGN00C** | | | | | | R | | | | |
| **COACTVWC** | R | R | R | | | | | | | |
| **COACTUPC** | R/W | R | R | | | | | | | |
| **COCRDLIC** | | R | | R | | | | | | |
| **COCRDSLC** | R | R | | | | | | | | |
| **COCRDUPC** | R | R | | | | | | | | |
| **COTRN00C** | | | | R | R | | | | | |
| **COTRN01C** | | | | | R | | | | | |
| **COTRN02C** | | | | R | R/W | | | R | R | |
| **COBIL00C** | R/W | | | R | R/W | | | | | |
| **CORPT00C** | | | | | | | | | | |
| **COUSR00C** | | | | | | R | | | | |
| **COUSR01C** | | | | | | W | | | | |
| **COUSR02C** | | | | | | R/W | | | | |
| **COUSR03C** | | | | | | R/D | | | | |

**Legend**: R = Read, W = Write, R/W = Read + Write/Rewrite, R/D = Read + Delete

---

## Batch File I/O Map

Batch programs use standard COBOL `SELECT ... ASSIGN TO` for file access.

| Program | Input Files | Output Files | I-O Files |
|---|---|---|---|
| **CBACT01C** | ACCTFILE (seq input) | OUTFILE, ARRYFILE, VBRCFILE | |
| **CBACT02C** | CARDFILE (seq input) | | |
| **CBACT03C** | XREFFILE (seq input) | | |
| **CBCUS01C** | CUSTFILE (seq input) | | |
| **CBTRN01C** | DALYTRAN (daily trans) | | CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE |
| **CBTRN02C** | DALYTRAN (daily trans) | DALYREJS (rejects) | XREFFILE, ACCTFILE, TRANFILE, TCATBALF |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) | |
| **CBACT04C** | | | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT |
| **CBSTM03A** | | STMTFILE (statements), HTMLFILE | |
| **CBSTM03B** | TRNXFILE, CUSTFILE, ACCTFILE | | XREFFILE |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (export) | |
| **CBIMPORT** | EXPFILE (export) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | |

### Detailed Batch File Access

```
CBACT01C (Account Refresh)
  IN:  ACCTFILE (sequential) ------> VSAM ACCTFILE
  OUT: OUTFILE, ARRYFILE, VBRCFILE

CBACT02C (Card Refresh)
  IN:  CARDFILE (sequential) ------> VSAM CARDFILE

CBACT03C (Cross-Ref Refresh)
  IN:  XREFFILE (sequential) ------> VSAM XREFFILE

CBCUS01C (Customer Refresh)
  IN:  CUSTFILE (sequential) ------> VSAM CUSTFILE

CBTRN01C (Transaction Load)
  IN:  DALYTRAN (daily transactions)
  I-O: CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE (validation + write)

CBTRN02C (Transaction Posting)
  IN:  DALYTRAN (daily transactions)
  I-O: XREFFILE, ACCTFILE (balance update), TRANFILE (write), TCATBALF (update)
  OUT: DALYREJS (rejected transactions)

CBACT04C (Interest Calculation)
  I-O: TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT

CBTRN03C (Transaction Report)
  IN:  TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
  OUT: TRANREPT (report file)

CBSTM03A/B (Statement Generation)
  IN:  TRNXFILE, CUSTFILE, ACCTFILE, XREFFILE
  OUT: STMTFILE, HTMLFILE

CBEXPORT (Data Export)
  IN:  CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE
  OUT: EXPFILE (multi-record sequential)

CBIMPORT (Data Import)
  IN:  EXPFILE (multi-record sequential)
  OUT: CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT
```

---

## JCL Job-to-Program Map

| JCL Job | Step Program | VSAM Datasets Used | Purpose |
|---|---|---|---|
| `ACCTFILE.jcl` | CBACT01C | ACCTFILE | Refresh account master |
| `CARDFILE.jcl` | CBACT02C | CARDFILE | Refresh card master |
| `CUSTFILE.jcl` | CBCUS01C | CUSTFILE | Refresh customer master |
| `XREFFILE.jcl` | CBACT03C | XREFFILE | Load card cross-reference |
| `TRANFILE.jcl` | CBTRN01C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | Load daily transactions |
| `POSTTRAN.jcl` | CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE, TRANFILE, TCATBALF, DALYREJS | Post transactions |
| `INTCALC.jcl` | CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | Calculate interest |
| `CREASTMT.JCL` | CBSTM03A | TRNXFILE, CUSTFILE, ACCTFILE, XREFFILE, STMTFILE, HTMLFILE | Generate statements |
| `TRANREPT.jcl` | CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, TRANREPT | Daily transaction report |
| `CBEXPORT.jcl` | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE | Export all data |
| `CBIMPORT.jcl` | CBIMPORT | EXPFILE, CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT | Import all data |
| `TRANBKP.jcl` | IDCAMS REPRO | TRANSACT | Backup transaction file |
| `TRANIDX.jcl` | IDCAMS | TRANSACT | Define alternate index |
| `CLOSEFIL.jcl` | CICS CEMT | (all CICS files) | Close files for batch |
| `OPENFIL.jcl` | CICS CEMT | (all CICS files) | Reopen files after batch |
| `DUSRSECJ.jcl` | IDCAMS REPRO | USRSEC | Load user security data |
| `COMBTRAN.jcl` | SORT/MERGE | TRANSACT, DALYTRAN | Combine daily + master transactions |
| `WAITSTEP.jcl` | COBSWAIT | -- | Execute timed wait |
| `DEFGDGB.jcl` | IDCAMS | -- | Define GDG base |
| `DEFGDGD.jcl` | IDCAMS | -- | Define GDG for daily files |

---

## Batch Processing Sequence

The standard end-of-day batch cycle runs in this order. Each step depends on the previous step completing successfully.

```
Step 1: CLOSEFIL    Close CICS files for exclusive batch access
           |
Step 2: Data Refresh (can run in parallel)
           |--- ACCTFILE.jcl   (CBACT01C -> ACCTFILE)
           |--- CARDFILE.jcl   (CBACT02C -> CARDFILE)
           |--- CUSTFILE.jcl   (CBCUS01C -> CUSTFILE)
           |--- XREFFILE.jcl   (CBACT03C -> XREFFILE)
           |--- DUSRSECJ.jcl   (IDCAMS   -> USRSEC)
           |
Step 3: TRANFILE    Load daily transactions (CBTRN01C)
           |         reads: DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE
           |         writes: TRANFILE
           |
Step 4: POSTTRAN    Post transactions to accounts (CBTRN02C)
           |         reads: DALYTRAN, XREFFILE
           |         updates: ACCTFILE (balances), TRANFILE, TCATBALF
           |         writes: DALYREJS (rejected)
           |
Step 5: INTCALC     Calculate interest (CBACT04C)
           |         reads: TCATBALF, XREFFILE, DISCGRP
           |         updates: ACCTFILE, TRANSACT
           |
Step 6: TRANBKP     Backup transaction file
           |
Step 7: COMBTRAN    Combine transactions (SORT/MERGE)
           |
Step 8: CREASTMT    Generate statements (CBSTM03A/B)
           |         reads: TRNXFILE, CUSTFILE, ACCTFILE, XREFFILE
           |         writes: STMTFILE, HTMLFILE
           |
Step 9: TRANREPT    Generate daily report (CBTRN03C)
           |         reads: TRANFILE, CARDXREF, TRANTYPE, TRANCATG
           |         writes: TRANREPT
           |
Step 10: TRANIDX    Rebuild alternate indexes
           |
Step 11: OPENFIL    Reopen CICS files for online access
```

---

## Copybook Inclusion Map

Which copybooks are included by which programs (based on COPY statements and data structure usage).

### Core Business Entity Copybooks

| Copybook | Online Programs Using | Batch Programs Using |
|---|---|---|
| `CVACT01Y` (Account) | COACTVWC, COACTUPC, COCRDSLC, COBIL00C | CBACT01C, CBTRN02C, CBACT04C, CBEXPORT, CBIMPORT |
| `CVACT02Y` (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBEXPORT, CBIMPORT |
| `CVACT03Y` (Card Xref) | COCRDLIC, COTRN00C, COTRN02C, COBIL00C | CBTRN01C, CBTRN02C, CBACT03C, CBACT04C, CBEXPORT, CBIMPORT |
| `CVCUS01Y` (Customer) | COACTVWC, COACTUPC | CBCUS01C, CBSTM03B, CBEXPORT, CBIMPORT |
| `CVTRA05Y` (Transaction) | COTRN00C, COTRN01C, COTRN02C, COBIL00C | CBTRN01C, CBTRN02C, CBACT04C, CBEXPORT, CBIMPORT |
| `CVTRA06Y` (Daily Tran) | -- | CBTRN01C, CBTRN02C |
| `CSUSR01Y` (User Security) | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | -- |

### Infrastructure Copybooks

| Copybook | Programs Using |
|---|---|
| `COCOM01Y` (COMMAREA) | All 17 online CICS programs (passed via XCTL) |
| `CVCRD01Y` (Card Work Areas) | All online CICS programs (AID mapping, navigation, messages) |
| `COMEN02Y` (Menu Options) | COMEN01C |
| `COADM02Y` (Admin Menu) | COADM01C |
| `COTTL01Y` (Title/Header) | All online CICS programs with screen output |
| `CSDAT01Y` (Date/Time) | All online CICS programs (timestamp display) |
| `CSSTRPFY` (PFKey Storage) | All online CICS programs (function key handling) |
| `CSUTLDWY` (Date Validation WS) | COACTUPC, COCRDUPC, COTRN02C, CORPT00C |
| `CSUTLDPY` (Date Validation Logic) | COACTUPC, COCRDUPC, COTRN02C, CORPT00C |
| `CSLKPCDY` (Lookup Codes) | COACTUPC (phone, state, zip validation) |
| `CSMSG01Y` (Common Messages) | All online CICS programs |
| `CSMSG02Y` (Abend Data) | All batch programs (abend handling) |
| `CVTRA07Y` (Report Layout) | CBTRN03C |
| `COSTM01` (Statement Layout) | CBSTM03A, CBSTM03B |
| `CVEXPORT` (Export Layout) | CBEXPORT, CBIMPORT |
| `CODATECN` (Date Conversion) | CBACT01C, CSUTLDTC |

---

## Data Lineage

### Account Balance Flow

```
Daily Transaction File (DALYTRAN)
       |
       v
  CBTRN02C (Transaction Posting)
       |
       +---> ACCTFILE.ACCT-CURR-BAL      (balance updated)
       +---> ACCTFILE.ACCT-CURR-CYC-DEBIT (cycle debit updated)
       +---> ACCTFILE.ACCT-CURR-CYC-CREDIT (cycle credit updated)
       +---> TCATBALF.TRAN-CAT-BAL        (category balance updated)
       +---> TRANSACT                      (transaction written)
       +---> DALYREJS                      (rejected transactions)
       |
       v
  CBACT04C (Interest Calculation)
       |
       +---> reads TCATBALF (category balances)
       +---> reads DISCGRP (interest rates by group)
       +---> updates ACCTFILE (interest charges)
       +---> writes TRANSACT (interest transactions)
```

### Customer Statement Flow

```
  TRANSACT (Transaction Master)
       |
       v
  CBSTM03B (Statement Pre-processing)
       |
       +---> reads XREFFILE (card -> customer mapping)
       +---> reads CUSTFILE (customer name/address)
       +---> reads ACCTFILE (account balances)
       |
       v
  CBSTM03A (Statement Generation)
       |
       +---> writes STMTFILE (printed statements)
       +---> writes HTMLFILE (HTML statements)
```

### Transaction Reporting Flow

```
  TRANSACT (Transaction Master)
       |
       v
  CBTRN03C (Transaction Report)
       |
       +---> reads CARDXREF (card cross-references)
       +---> reads TRANTYPE (type descriptions)
       +---> reads TRANCATG (category descriptions)
       +---> reads DATEPARM (date range parameters)
       |
       v
  TRANREPT (Report Output File)
```

### Data Export/Import Flow

```
  Export:
  CUSTFILE + ACCTFILE + XREFFILE + TRANSACT + CARDFILE
       |
       v
  CBEXPORT -----> EXPFILE (500-byte multi-record sequential)
                    |
                    | (transferred to target branch)
                    v
  CBIMPORT <----- EXPFILE
       |
       v
  CUSTOUT + ACCTOUT + XREFOUT + TRNXOUT + CARDOUT + ERROUT
```

### Online Transaction Creation Flow

```
  User Input (COTRN02 BMS Map)
       |
       v
  COTRN02C (Transaction Add)
       |
       +---> validates via CSUTLDTC (date validation)
       +---> reads XREFFILE (card -> account lookup)
       +---> reads TRANTYPE (type validation)
       +---> reads TRANCATG (category validation)
       |
       v
  TRANSACT (EXEC CICS WRITE) -- new transaction written
```

### Bill Payment Flow

```
  User Input (COBIL00 BMS Map)
       |
       v
  COBIL00C (Bill Payment)
       |
       +---> reads XREFFILE (card -> account lookup)
       +---> reads ACCTFILE (current balance)
       +---> updates ACCTFILE (EXEC CICS REWRITE -- balance reduced)
       +---> writes TRANSACT (EXEC CICS WRITE -- payment transaction)
```

### Authentication Flow

```
  User Input (COSGN00 BMS Map)
       |
       v
  COSGN00C (Sign-On)
       |
       +---> reads USRSEC (EXEC CICS READ -- credential check)
       |
       +---> SEC-USR-TYPE = 'A' --> XCTL to COADM01C (Admin Menu)
       +---> SEC-USR-TYPE = 'U' --> XCTL to COMEN01C (Main Menu)
```
