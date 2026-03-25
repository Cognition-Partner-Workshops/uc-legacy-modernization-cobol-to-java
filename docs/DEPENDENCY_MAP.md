# CardDemo Dependency Map

> **Application:** AWS CardDemo -- Credit Card Management System
> **Source:** Static analysis of CALL, COPY, EXEC CICS XCTL/LINK, and file I/O statements

---

## 1. Program Call Graph

### 1.1 Online CICS Program Flow

```
                        ┌─────────────┐
                        │  COSGN00C   │  Sign-on (CC00)
                        │  Entry Point│
                        └──────┬──────┘
                               │ EXEC CICS XCTL
                    ┌──────────┴──────────┐
                    ▼                     ▼
             ┌────────────┐        ┌────────────┐
             │ COMEN01C   │        │ COADM01C   │
             │ Main Menu  │        │ Admin Menu │
             └──────┬─────┘        └──────┬─────┘
                    │ EXEC CICS XCTL      │ EXEC CICS XCTL
        ┌───────────┼───────────┐    ┌────┼────────────────┐
        ▼           ▼           ▼    ▼    ▼                ▼
   ┌─────────┐ ┌─────────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
   │COACTVWC │ │COACTUPC │ │COCRD-│ │COUSR │ │COUSR │ │COUSR │
   │Acct View│ │Acct Upd │ │LIC   │ │00C   │ │01C   │ │02C   │
   └─────────┘ └─────────┘ │Card  │ │List  │ │Add   │ │Update│
                            │List  │ └──────┘ └──────┘ └──────┘
                            └──┬───┘                    ┌──────┐
                               │                        │COUSR │
                    ┌──────────┼──────────┐             │03C   │
                    ▼          ▼          ▼             │Delete│
               ┌─────────┐ ┌─────────┐                 └──────┘
               │COCRDSLC │ │COCRDUPC │
               │Card View│ │Card Upd │
               └─────────┘ └─────────┘

   From COMEN01C (Main Menu -- 11 options):
        ├── 1. COACTVWC  (Account View)
        ├── 2. COACTUPC  (Account Update)
        ├── 3. COCRDLIC  (Credit Card List)
        ├── 4. COCRDSLC  (Credit Card View)
        ├── 5. COCRDUPC  (Credit Card Update)
        ├── 6. COTRN00C  (Transaction List)
        ├── 7. COTRN01C  (Transaction View)
        ├── 8. COTRN02C  (Transaction Add)
        ├── 9. CORPT00C  (Transaction Reports)
        ├── 10. COBIL00C (Bill Payment)
        └── 11. COPAUS0C (Pending Auth View -- optional module)

   From COADM01C (Admin Menu -- 6 options):
        ├── 1. COUSR00C  (User List)
        ├── 2. COUSR01C  (User Add)
        ├── 3. COUSR02C  (User Update)
        ├── 4. COUSR03C  (User Delete)
        ├── 5. COTRTLIC  (Tran Type List -- optional DB2 module)
        └── 6. COTRTUPC  (Tran Type Maint -- optional DB2 module)
```

### 1.2 Sub-Program CALL Graph

```
Program              CALL Target          Purpose
─────────────────────────────────────────────────────────────────
CBACT01C       ───►  COBDATFT (ASM)       Date formatting
CBSTM03A       ───►  CBSTM03B             File I/O helper for statements
COBSWAIT       ───►  MVSWAIT  (ASM)       MVS wait/delay
CORPT00C       ───►  CSUTLDTC             Date validation
COTRN02C       ───►  CSUTLDTC             Date validation
CSUTLDTC       ───►  CEEDAYS  (LE)        Language Environment date calc
CBACT01C       ───►  CEE3ABD  (LE)        Abnormal termination
CBACT02C       ───►  CEE3ABD  (LE)        Abnormal termination
CBACT03C       ───►  CEE3ABD  (LE)        Abnormal termination
CBCUS01C       ───►  CEE3ABD  (LE)        Abnormal termination
CBIMPORT       ───►  CEE3ABD  (LE)        Abnormal termination
```

### 1.3 CICS Navigation (XCTL / RETURN TRANSID)

| Source Program | Target Program | Mechanism | Condition |
|----------------|----------------|-----------|-----------|
| COSGN00C | COMEN01C | XCTL | User type = 'U' |
| COSGN00C | COADM01C | XCTL | User type = 'A' |
| COMEN01C | (any menu option) | XCTL | Menu selection 1-11 |
| COADM01C | (any admin option) | XCTL | Menu selection 1-6 |
| COCRDLIC | COCRDSLC | XCTL | Card selected for view |
| COCRDLIC | COCRDUPC | XCTL | Card selected for update |
| All online | COSGN00C | RETURN TRANSID | PF3 from menu / session end |
| All online | (parent menu) | RETURN TRANSID | PF3 back navigation |

---

## 2. Copybook Dependency Matrix

### 2.1 Which Programs Use Which Copybooks

| Copybook | Online Programs | Batch Programs |
|----------|----------------|----------------|
| **COCOM01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | -- |
| **COTTL01Y** | All 17 online programs | -- |
| **CSDAT01Y** | All 17 online programs | -- |
| **CSMSG01Y** | All 17 online programs | -- |
| **CSUSR01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | -- |
| **CVACT01Y** | COACTVWC, COACTUPC, COTRN02C | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| **CVACT03Y** | COACTVWC, COACTUPC, COCRDLIC, COTRN02C, COBIL00C | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVCUS01Y** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVCRD01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | -- |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| **CVTRA06Y** | -- | CBTRN01C, CBTRN02C |
| **CVTRA01Y** | -- | CBACT04C, CBTRN02C |
| **CVTRA02Y** | -- | CBACT04C |
| **CVTRA03Y** | -- | CBTRN03C |
| **CVTRA04Y** | -- | CBTRN03C |
| **CVTRA07Y** | -- | CBTRN03C |
| **CVEXPORT** | -- | CBEXPORT, CBIMPORT |
| **COSTM01** | -- | CBSTM03A |
| **CUSTREC** | -- | CBSTM03A |
| **CODATECN** | -- | CBACT01C |
| **COMEN02Y** | COMEN01C | -- |
| **COADM02Y** | COADM01C | -- |
| **CSLKPCDY** | COACTUPC | -- |
| **CSSTRPFY** | COACTVWC, COCRDSLC, COCRDUPC | -- |
| **CSUTLDWY** | COACTUPC | -- |
| **CSMSG02Y** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | -- |
| **CSSETATY** | (select online programs) | -- |
| **CSUTLDPY** | CSUTLDTC | -- |

### 2.2 Shared vs. Isolated Copybooks

| Category | Count | Copybooks |
|----------|------:|-----------|
| **Widely shared** (5+ programs) | 8 | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| **Moderately shared** (2-4 programs) | 10 | CVACT02Y, CVCUS01Y, CVCRD01Y, CVTRA06Y, CVTRA01Y, CSMSG02Y, CSSTRPFY, CVEXPORT, DFHAID, DFHBMSCA |
| **Isolated** (1 program) | 12 | COMEN02Y, COADM02Y, CSLKPCDY, CSUTLDWY, CVTRA02Y-04Y, CVTRA07Y, COSTM01, CUSTREC, CODATECN, UNUSED1Y |

---

## 3. VSAM File Data Lineage

### 3.1 File Access by Program

| VSAM File | Readers (READ/STARTBR) | Writers (WRITE/REWRITE) | Batch Jobs |
|-----------|------------------------|-------------------------|------------|
| **USRSEC** | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) | DUSRSECJ (load) |
| **ACCTFILE** | COACTVWC, COACTUPC, COBIL00C, CBACT01C, CBTRN01C, CBSTM03A/B | COACTUPC (REWRITE), COBIL00C (REWRITE), CBTRN02C (REWRITE), CBACT04C (REWRITE) | ACCTFILE (refresh), POSTTRAN, INTCALC |
| **CARDFILE** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBSTM03A/B | COCRDUPC (REWRITE) | CARDFILE (refresh) |
| **CARDXREF** | COACTVWC, COTRN02C, COBIL00C, CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C, CBSTM03A/B | -- (read-only, loaded by JCL) | XREFFILE (refresh) |
| **CUSTFILE** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBSTM03A/B | -- (read-only, loaded by JCL) | CUSTFILE (refresh) |
| **TRANSACT** | COTRN00C, COTRN01C, COBIL00C, CBTRN03C, CBSTM03A/B | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE), CBACT04C (WRITE) | TRANFILE (load), POSTTRAN, INTCALC |
| **DALYTRAN** | CBTRN01C, CBTRN02C | -- (loaded externally) | COMBTRAN, POSTTRAN |
| **TCATBALF** | CBACT04C, CBTRN02C | CBTRN02C (REWRITE) | TCATBALF (define), INTCALC |
| **DISCGRP** | CBACT04C | -- (reference data) | DISCGRP (define) |
| **TRANTYPE** | CBTRN03C | -- (reference data) | TRANTYPE (define) |
| **TRANCATG** | CBTRN03C | -- (reference data) | TRANCATG (define) |

### 3.2 End-to-End Data Flow: Daily Batch Cycle

```
                    BATCH CYCLE ORDER
                    ==================

Step 1: CLOSEFIL.jcl
        │  Close CICS files for exclusive batch access
        ▼
Step 2: Data Refresh Jobs (parallel)
        │  ACCTFILE.jcl ──► ACCTFILE (VSAM)
        │  CARDFILE.jcl ──► CARDFILE (VSAM)
        │  CUSTFILE.jcl ──► CUSTFILE (VSAM)
        │  XREFFILE.jcl ──► CARDXREF (VSAM)
        │  TRANFILE.jcl ──► TRANSACT (VSAM)
        ▼
Step 3: POSTTRAN.jcl  (Program: CBTRN02C)
        │  Input:  DALYTRAN (daily transactions)
        │  Lookup: CARDXREF, ACCTFILE
        │  Output: TRANSACT (posted transactions)
        │          ACCTFILE (updated balances)
        │          TCATBALF (updated category balances)
        │          DALYREJS (rejected transactions)
        ▼
Step 4: INTCALC.jcl   (Program: CBACT04C)
        │  Input:  TCATBALF (category balances)
        │  Lookup: CARDXREF, DISCGRP (interest rates), ACCTFILE
        │  Output: TRANSACT (interest charge transactions)
        │          ACCTFILE (updated with interest/fees)
        ▼
Step 5: TRANBKP.jcl
        │  Backup: TRANSACT ──► GDG backup dataset
        ▼
Step 6: COMBTRAN.jcl
        │  Merge:  DALYTRAN + TRANSACT ──► combined dataset
        ▼
Step 7: CREASTMT.JCL  (Programs: CBSTM03A + CBSTM03B)
        │  Input:  TRANSACT, CARDXREF, CUSTFILE, ACCTFILE
        │  Output: Statement files (text + HTML)
        ▼
Step 8: TRANREPT.jcl  (Program: CBTRN03C)
        │  Input:  TRANSACT, CARDXREF, TRANTYPE, TRANCATG
        │  Output: Daily Transaction Report
        ▼
Step 9: TRANIDX.jcl
        │  Rebuild: Alternate index on TRANSACT
        ▼
Step 10: OPENFIL.jcl
         │  Reopen CICS files for online access
         ▼
         BATCH CYCLE COMPLETE
```

### 3.3 Online Transaction Data Flow

```
User Sign-on:
    COSGN00C ──READ──► USRSEC
        │
        ▼ (XCTL with COMMAREA)
    COMEN01C / COADM01C (menu)
        │
        ▼ (XCTL with COMMAREA containing user context)

Account Inquiry Flow:
    COACTVWC ──READ──► CARDXREF (lookup by account)
             ──READ──► ACCTFILE (account details)
             ──READ──► CUSTFILE (customer details)
             ──READ──► CARDFILE (card details)

Transaction Add Flow:
    COTRN02C ──READ──►  CARDXREF (validate card→account)
             ──READ──►  ACCTFILE (validate account active)
             ──CALL──►  CSUTLDTC (validate dates)
             ──STARTBR/READPREV──► TRANSACT (get last tran ID)
             ──WRITE──► TRANSACT (new transaction)

Bill Payment Flow:
    COBIL00C ──READ──►  CARDXREF (lookup)
             ──READ──►  ACCTFILE (get balance)
             ──STARTBR/READPREV──► TRANSACT (last tran ID)
             ──WRITE──► TRANSACT (payment transaction)
             ──REWRITE──► ACCTFILE (updated balance)

Report Submission Flow:
    CORPT00C ──CALL──►  CSUTLDTC (validate dates)
             ──WRITEQ TD──► CICS internal reader
             (submits INTRDRJ1/J2 JCL to run CBTRN03C)
```

---

## 4. JCL Job Dependencies

### 4.1 Job-to-Program Mapping

| JCL Job | Primary Program | Utility Steps | Input Datasets | Output Datasets |
|---------|----------------|---------------|----------------|-----------------|
| POSTTRAN | CBTRN02C | -- | DALYTRAN | TRANSACT, ACCTFILE, TCATBALF, DALYREJS |
| INTCALC | CBACT04C | -- | TCATBALF, CARDXREF, DISCGRP, ACCTFILE | TRANSACT, ACCTFILE |
| CREASTMT | CBSTM03A, CBSTM03B | -- | TRANSACT, CARDXREF, CUSTFILE, ACCTFILE | STMTFILE, HTMLFILE |
| TRANREPT | CBTRN03C | -- | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | REPTFILE |
| READACCT | CBACT01C | -- | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE |
| READCARD | CBACT02C | -- | CARDFILE | (display only) |
| READCUST | CBCUS01C | -- | CUSTFILE | (display only) |
| READXREF | CBACT03C | -- | CARDXREF | (display only) |
| CBEXPORT | CBEXPORT | -- | CUSTFILE, ACCTFILE, CARDXREF, TRANSACT, CARDFILE | EXPORT-OUTPUT |
| CBIMPORT | CBIMPORT | -- | EXPORT-INPUT | CUSTFILE, ACCTFILE, CARDXREF, TRANSACT, CARDFILE, ERROR-OUTPUT |
| ACCTFILE | (IDCAMS) | REPRO | ASCII data | ACCTFILE VSAM |
| CARDFILE | (IDCAMS) | REPRO | ASCII data | CARDFILE VSAM |
| CUSTFILE | (IDCAMS) | REPRO | ASCII data | CUSTFILE VSAM |
| XREFFILE | (IDCAMS) | REPRO | ASCII data | CARDXREF VSAM |
| TRANFILE | (IDCAMS) | REPRO | ASCII data | TRANSACT VSAM |
| DUSRSECJ | (IDCAMS) | REPRO | ASCII data | USRSEC VSAM |
| COMBTRAN | (SORT) | MERGE | DALYTRAN, TRANSACT | Combined file |
| TRANBKP | (IDCAMS) | REPRO | TRANSACT | GDG backup |

### 4.2 Job Scheduling Dependencies (from `app/scheduler/`)

```
CA7 / Control-M Scheduling Flow:

    CLOSEFIL ──► ACCTFILE  ──┐
                  CARDFILE  ──┤
                  CUSTFILE  ──├──► POSTTRAN ──► INTCALC ──► TRANBKP
                  XREFFILE  ──┤                                │
                  TRANFILE  ──┘                                ▼
                  DUSRSECJ                              COMBTRAN
                                                           │
                                                           ▼
                                                    ┌──────┴──────┐
                                                    ▼             ▼
                                                CREASTMT     TRANREPT
                                                    │             │
                                                    └──────┬──────┘
                                                           ▼
                                                       TRANIDX
                                                           │
                                                           ▼
                                                       OPENFIL
```

---

## 5. Cross-Cutting Concerns

### 5.1 Error Handling Pattern

All programs follow a consistent abend pattern:
```
9910-DISPLAY-IO-STATUS  →  Display file status code
9999-ABEND-PROGRAM      →  CALL 'CEE3ABD' (batch) or EXEC CICS ABEND (online)
```

### 5.2 Common Include Chain (Online Programs)

Every online CICS program includes this standard set:
```
COPY COCOM01Y.    ← COMMAREA (inter-program communication)
COPY COTTL01Y.    ← Screen title
COPY CSDAT01Y.    ← Date/time fields
COPY CSMSG01Y.    ← Message area
COPY CSUSR01Y.    ← User security record
COPY DFHAID.      ← CICS AID key definitions (IBM-supplied)
COPY DFHBMSCA.    ← BMS attribute bytes (IBM-supplied)
COPY <mapname>.   ← BMS-generated screen copybook
```

### 5.3 Shared Utility Dependencies

| Utility | Callers | Technology |
|---------|---------|------------|
| CSUTLDTC | CORPT00C, COTRN02C | COBOL (calls CEEDAYS) |
| COBDATFT | CBACT01C | Assembler |
| MVSWAIT | COBSWAIT | Assembler |
| CEE3ABD | CBACT01C, CBACT02C, CBACT03C, CBCUS01C, CBIMPORT | LE Runtime |
| CEEDAYS | CSUTLDTC | LE Runtime |
