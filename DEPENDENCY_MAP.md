# Dependency Map - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management)
> **Architecture:** CICS/VSAM Online + Batch COBOL

---

## 1. Online Program Call Graph (CICS XCTL)

All online programs communicate via **EXEC CICS XCTL** (transfer control) passing the shared **CARDDEMO-COMMAREA** (defined in `COCOM01Y.cpy`). The COMMAREA carries session state including user ID, user type, source/target program names, and current business context (account, card, customer).

```
                              ┌─────────────┐
                              │  COSGN00C   │
                              │  (Signon)   │
                              │  Tran: CC00 │
                              └──────┬──────┘
                                     │
                         ┌───────────┴───────────┐
                         │ User Type?            │
                    ┌────┴────┐            ┌─────┴─────┐
                    │ Type='U'│            │ Type='A'  │
                    ▼         │            ▼           │
              ┌───────────┐   │      ┌───────────┐    │
              │ COMEN01C  │   │      │ COADM01C  │    │
              │(User Menu)│   │      │(Admin Menu)│   │
              │ Tran: CM00│   │      │ Tran: CA00│    │
              └─────┬─────┘   │      └─────┬─────┘    │
                    │         │            │           │
    ┌───────────────┼─────────┘    ┌───────┴────────┐  │
    │  Menu Options (1-11):        │ Admin Options: │  │
    │                              │                │  │
    │  1. COACTVWC (Acct View)     │ 1. COUSR00C    │  │
    │  2. COACTUPC (Acct Update)   │ 2. COUSR01C    │  │
    │  3. COCRDLIC (Card List)     │ 3. COUSR02C    │  │
    │  4. COCRDSLC (Card View)     │ 4. COUSR03C    │  │
    │  5. COCRDUPC (Card Update)   │ 5. COTRTLIC *  │  │
    │  6. COTRN00C (Tran List)     │ 6. COTRTUPC *  │  │
    │  7. COTRN01C (Tran View)     │                │  │
    │  8. COTRN02C (Tran Add)      │ * = DB2 module │  │
    │  9. CORPT00C (Reports)       └────────────────┘  │
    │ 10. COBIL00C (Bill Pay)                          │
    │ 11. COPAUS0C (Auth View) *                       │
    │                                                  │
    │  * = Optional IMS/DB2/MQ module                  │
    └──────────────────────────────────────────────────┘
```

### Navigation Rules

All programs follow a consistent XCTL pattern:
- **PF3** → Return to calling menu (`CDEMO-TO-PROGRAM` set to menu program)
- **CLEAR** → Return to signon screen (COSGN00C)
- **ENTER** → Process current screen action
- **PF7/PF8** → Page up/down (list screens only)

### Detailed XCTL Calls

| Source Program | Target Program(s)                     | Mechanism         | Condition                           |
|----------------|---------------------------------------|-------------------|-------------------------------------|
| COSGN00C       | COADM01C                              | XCTL (literal)    | User type = 'A' (Admin)            |
| COSGN00C       | COMEN01C                              | XCTL (literal)    | User type = 'U' (Regular)          |
| COMEN01C       | COACTVWC, COACTUPC, COCRDLIC, etc.    | XCTL (table-driven) | Menu option selection (COMEN02Y) |
| COMEN01C       | COSGN00C                              | XCTL (via COMMAREA) | PF3 or session end              |
| COADM01C       | COUSR00C, COUSR01C, COUSR02C, COUSR03C | XCTL (table-driven) | Admin menu selection (COADM02Y) |
| COADM01C       | COSGN00C                              | XCTL (via COMMAREA) | PF3 or session end              |
| COACTVWC       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COACTUPC       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COCRDLIC       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COCRDSLC       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COCRDUPC       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COTRN00C       | COTRN01C                              | XCTL (COMMAREA)   | Select transaction to view          |
| COTRN00C       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COTRN01C       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COTRN02C       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| CORPT00C       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COBIL00C       | COMEN01C / COADM01C                   | XCTL (COMMAREA)   | PF3 return                          |
| COUSR00C       | COUSR01C                              | XCTL (COMMAREA)   | Select user to add                  |
| COUSR00C       | COUSR02C                              | XCTL (COMMAREA)   | Select user to update               |
| COUSR00C       | COADM01C                              | XCTL (COMMAREA)   | PF3 return                          |
| COUSR01C       | COADM01C                              | XCTL (COMMAREA)   | PF3 return                          |
| COUSR02C       | COADM01C                              | XCTL (COMMAREA)   | PF3 return                          |
| COUSR03C       | COADM01C                              | XCTL (COMMAREA)   | PF3 return                          |

### CALL Dependencies (Subroutine)

| Caller Program | Called Program | Mechanism    | Purpose                              |
|----------------|---------------|--------------|--------------------------------------|
| COACTUPC       | CSUTLDTC      | CALL (via CSUTLDPY copybook) | Date validation               |
| COCRDUPC       | CSUTLDTC      | CALL (via CSUTLDPY copybook) | Date validation               |
| CBIMPORT       | CEE3ABD       | CALL         | LE Abend handler                     |

---

## 2. Batch Program Data Lineage

### 2a. Daily Batch Processing Cycle

The batch cycle runs in a fixed sequence, typically nightly:

```
Step 1: CLOSEFIL     Close CICS files (SDSF)
          │
Step 2: Data Refresh  ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE
          │            (IDCAMS delete/define/repro)
          │
Step 3: POSTTRAN     Post daily transactions
          │            CBTRN02C: DALYTRAN + XREF + ACCT → TRANSACT + DALYREJS + TCATBALF
          │
Step 4: INTCALC      Calculate interest
          │            CBACT04C: XREF + ACCT + TRANSACT + DISCGRP → TCATBALF
          │
Step 5: TRANBKP      Backup transactions to GDG
          │            IDCAMS REPRO: TRANSACT → GDG generation
          │
Step 6: COMBTRAN     Combine/sort transactions
          │            SORT: multiple GDGs → merged TRANSACT
          │
Step 7: CREASTMT     Generate statements
          │            SORT + CBSTM03A: TRANSACT + XREF + CUST + ACCT → STMTFILE
          │
Step 8: TRANREPT     Generate reports
          │            SORT + CBTRN03C: TRANSACT + XREF + TRANTYPE + TRANCATG → TRANREPT
          │
Step 9: TRANIDX      Rebuild alternate indexes
          │            IDCAMS: TRANSACT VSAM → alt index paths
          │
Step 10: OPENFIL     Open CICS files (SDSF)
```

### 2b. Batch Program File I/O Matrix

| Program    | Input Files                                          | Output Files                    | Mode    |
|------------|------------------------------------------------------|---------------------------------|---------|
| CBTRN02C   | DALYTRAN (R), XREFFILE (R), ACCTFILE (R)             | TRANFILE (W), DALYREJS (W), TCATBALF (RW) | Batch |
| CBACT04C   | TCATBALF (R), XREFFILE (R), ACCTFILE (R), DISCGRP (R), TRANSACT (R) | TCATBALF (W) | Batch |
| CBSTM03A   | TRNXFILE (R), XREFFILE (R), CUSTFILE (R), ACCTFILE (R) | STMTFILE (W), HTMLFILE (W)   | Batch |
| CBSTM03B   | TRNXFILE (R), XREFFILE (R), CUSTFILE (R), ACCTFILE (R) | (output stream)              | Batch |
| CBTRN03C   | TRANFILE (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATEPARM (R) | TRANREPT (W) | Batch |
| CBTRN01C   | DALYTRAN (R), CUSTFILE (R), XREFFILE (R), CARDFILE (R), ACCTFILE (R) | TRANFILE (W) | Batch |
| CBACT01C   | ACCTFILE (R)                                         | OUTFILE (W), ARRYFILE (W), VBRCFILE (W) | Utility |
| CBACT02C   | CARDFILE (R)                                         | SYSOUT                          | Utility |
| CBACT03C   | XREFFILE (R)                                         | SYSOUT                          | Utility |
| CBCUS01C   | CUSTFILE (R)                                         | SYSOUT                          | Utility |
| CBEXPORT   | CUSTFILE (R), ACCTFILE (R), XREFFILE (R), TRANSACT (R), CARDFILE (R) | EXPFILE (W) | Migration |
| CBIMPORT   | EXPFILE (R)                                          | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT (W) | Migration |

---

## 3. Online Program VSAM File Access

```
                    ┌──────────┐
                    │  USRSEC  │◄── COSGN00C (R), COUSR00C (R), COUSR01C (W),
                    │ (80 B)   │    COUSR02C (RW), COUSR03C (RW)
                    └──────────┘

                    ┌──────────┐
                    │ ACCTDAT  │◄── COACTVWC (R), COACTUPC (RW), COBIL00C (R)
                    │ (300 B)  │
                    └──────────┘

                    ┌──────────┐
                    │ CARDDAT  │◄── COACTVWC (R), COCRDLIC (R), COCRDSLC (R),
                    │ (150 B)  │    COCRDUPC (RW)
                    └──────────┘

                    ┌──────────┐
                    │ CUSTDAT  │◄── COACTVWC (R), COACTUPC (RW)
                    │ (500 B)  │
                    └──────────┘

                    ┌──────────┐
                    │ CARDXREF │◄── COACTVWC (R), COACTUPC (R), COBIL00C (R),
                    │ (50 B)   │    COTRN02C (R)
                    └──────────┘

                    ┌──────────┐
                    │ TRANSACT │◄── COTRN00C (R), COTRN01C (R), COTRN02C (RW),
                    │ (350 B)  │    COBIL00C (RW)
                    └──────────┘
```

### File Access Summary

| VSAM File  | Read By (Online)                                    | Written By (Online)            | Read By (Batch)                              | Written By (Batch)          |
|------------|-----------------------------------------------------|--------------------------------|----------------------------------------------|-----------------------------|
| USRSEC     | COSGN00C, COUSR00C                                  | COUSR01C, COUSR02C, COUSR03C   | ---                                          | DUSRSECJ (IDCAMS load)      |
| ACCTDAT    | COACTVWC, COACTUPC, COBIL00C                        | COACTUPC                       | CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN01C, CBEXPORT | ACCTFILE (IDCAMS refresh) |
| CARDDAT    | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC              | COCRDUPC                       | CBTRN01C, CBEXPORT                           | CARDFILE (IDCAMS refresh)   |
| CUSTDAT    | COACTVWC, COACTUPC                                  | COACTUPC                       | CBSTM03A/B, CBTRN01C, CBEXPORT              | CUSTFILE (IDCAMS refresh)   |
| CARDXREF   | COACTVWC, COACTUPC, COBIL00C, COTRN02C              | ---                            | CBTRN02C, CBACT04C, CBTRN03C, CBSTM03A/B, CBEXPORT | XREFFILE (IDCAMS refresh) |
| TRANSACT   | COTRN00C, COTRN01C, COTRN02C, COBIL00C              | COTRN02C, COBIL00C             | CBACT04C, CBTRN03C, CBEXPORT                | CBTRN02C, COMBTRAN (SORT)   |
| DALYTRAN   | ---                                                 | ---                            | CBTRN02C, CBTRN01C                           | (external feed)             |
| TCATBALF   | ---                                                 | ---                            | CBACT04C                                     | CBTRN02C, CBACT04C          |
| DISCGRP    | ---                                                 | ---                            | CBACT04C                                     | DISCGRP (IDCAMS refresh)    |
| TRANTYPE   | ---                                                 | ---                            | CBTRN03C                                     | TRANTYPE (IDCAMS refresh)   |
| TRANCATG   | ---                                                 | ---                            | CBTRN03C                                     | TRANCATG (IDCAMS refresh)   |

---

## 4. Copybook Usage Matrix

Shows which programs include which copybooks (via COPY statement).

| Copybook   | Online Programs                                                              | Batch Programs                                    |
|------------|------------------------------------------------------------------------------|---------------------------------------------------|
| COCOM01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | --- |
| COTTL01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | --- |
| CSDAT01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | --- |
| CSMSG01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | --- |
| CSUSR01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C | --- |
| DFHAID     | All 17 online programs                                                        | ---                                               |
| DFHBMSCA   | All 17 online programs                                                        | ---                                               |
| CVACT01Y   | COACTVWC, COACTUPC, COTRN02C, COBIL00C                                       | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT |
| CVACT02Y   | COCRDLIC, COCRDSLC, COCRDUPC                                                 | CBACT02C, CBACT04C, CBTRN01C, CBEXPORT, CBIMPORT |
| CVACT03Y   | COACTVWC, COACTUPC, COTRN02C, COBIL00C                                       | CBACT03C, CBACT04C, CBTRN02C, CBTRN03C, CBSTM03A, CBTRN01C, CBEXPORT, CBIMPORT |
| CVCUS01Y   | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                                       | CBCUS01C, CBTRN01C, CBSTM03A (via CUSTREC), CBEXPORT, CBIMPORT |
| CVTRA05Y   | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C                            | CBTRN02C, CBTRN03C, CBACT04C, CBTRN01C, CBEXPORT, CBIMPORT |
| CVTRA06Y   | ---                                                                           | CBTRN02C, CBTRN01C                                |
| CVTRA01Y   | ---                                                                           | CBACT04C, CBTRN02C                                |
| CVTRA02Y   | ---                                                                           | CBACT04C                                          |
| CVTRA03Y   | ---                                                                           | CBTRN03C                                          |
| CVTRA04Y   | ---                                                                           | CBTRN03C                                          |
| CVTRA07Y   | ---                                                                           | CBTRN03C                                          |
| CVCRD01Y   | COCRDLIC, COCRDSLC, COCRDUPC                                                 | ---                                               |
| CSSTRPFY   | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                                       | ---                                               |
| CSUTLDWY   | COACTUPC                                                                      | ---                                               |
| CSUTLDPY   | COACTUPC                                                                      | ---                                               |
| CSLKPCDY   | COACTUPC                                                                      | ---                                               |
| CSMSG02Y   | COACTVWC, COCRDSLC, COCRDUPC                                                 | ---                                               |
| CVEXPORT   | ---                                                                           | CBEXPORT, CBIMPORT                                |
| COSTM01    | ---                                                                           | CBSTM03A                                          |
| CUSTREC    | ---                                                                           | CBSTM03A                                          |
| CODATECN   | ---                                                                           | CBACT01C                                          |
| COMEN02Y   | COMEN01C                                                                      | ---                                               |
| COADM02Y   | COADM01C                                                                      | ---                                               |

---

## 5. JCL Job → Program → File Lineage

### Core Batch Jobs

```
POSTTRAN.jcl
  └─ EXEC PGM=CBTRN02C
       ├─ INPUT:  DALYTRAN (daily transactions)
       ├─ INPUT:  XREFFILE (card cross-reference)
       ├─ INPUT:  ACCTFILE (account master)
       ├─ OUTPUT: TRANFILE (transaction master)
       ├─ OUTPUT: DALYREJS (rejected transactions)
       └─ OUTPUT: TCATBALF (category balances)

INTCALC.jcl
  └─ EXEC PGM=CBACT04C, PARM='2022071800'
       ├─ INPUT:  TCATBALF (category balances)
       ├─ INPUT:  XREFFILE (card cross-reference)
       ├─ INPUT:  ACCTFILE (account master)
       ├─ INPUT:  DISCGRP  (disclosure/interest rates)
       ├─ INPUT:  TRANSACT (transactions)
       └─ OUTPUT: TCATBALF (updated balances)

CREASTMT.JCL
  ├─ STEP: SORT (sort transactions by card+date)
  ├─ STEP: IDCAMS (verify sorted file)
  └─ STEP: EXEC PGM=CBSTM03A
       ├─ INPUT:  Sorted TRANSACT
       ├─ INPUT:  XREFFILE, CUSTFILE, ACCTFILE
       ├─ OUTPUT: STMTFILE (statement print file)
       └─ OUTPUT: HTMLFILE (HTML statement)

TRANREPT.jcl
  ├─ STEP: PRC001 (backup transaction file)
  ├─ STEP: SORT (sort transactions)
  └─ STEP: EXEC PGM=CBTRN03C
       ├─ INPUT:  TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
       └─ OUTPUT: TRANREPT (daily transaction report)

CBEXPORT.jcl
  ├─ STEP: IDCAMS (define export file)
  └─ STEP: EXEC PGM=CBEXPORT
       ├─ INPUT:  CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE
       └─ OUTPUT: EXPFILE (multi-entity export)

CBIMPORT.jcl
  └─ STEP: EXEC PGM=CBIMPORT
       ├─ INPUT:  EXPFILE (multi-entity export file)
       └─ OUTPUT: CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT
```

### Data Refresh Jobs

```
ACCTFILE.jcl ──── IDCAMS ──── PS file → ACCTDAT VSAM
CARDFILE.jcl ──── IDCAMS ──── PS file → CARDDAT VSAM + Alt Index
CUSTFILE.jcl ──── IDCAMS ──── PS file → CUSTDAT VSAM
XREFFILE.jcl ──── IDCAMS ──── PS file → CARDXREF VSAM + Alt Index
TRANFILE.jcl ──── IDCAMS ──── PS file → TRANSACT VSAM
DUSRSECJ.jcl ──── IEBGENER + IDCAMS ──── PS file → USRSEC VSAM
```

---

## 6. Cross-Module Dependencies (Optional Modules)

### Authorization Module (IMS/DB2/MQ)

```
COPAUA0C (MQ Trigger)
  └─ Receives MQ messages → processes authorization requests
       └─ Writes to IMS DB / DB2

COPAUS0C (Summary View) ◄── COMEN01C menu option 11
  └─ Reads pending authorizations from DB2

COPAUS1C (Detail View) ◄── COPAUS0C selection
  └─ Reads authorization details from DB2

COPAUS2C (Fraud Mark) ◄── COPAUS1C action
  └─ Updates DB2 authorization status

CBPAUP0C (Batch Purge)
  └─ Deletes old authorization records from DB2
```

### Transaction Type DB2 Module

```
COTRTLIC (List/Delete) ◄── COADM01C admin menu option 5
  └─ DB2 cursor-based read + delete of transaction types

COTRTUPC (Add/Edit) ◄── COADM01C admin menu option 6
  └─ DB2 INSERT/UPDATE of transaction types

COBTUPDT (Batch Update)
  └─ Batch DB2 updates for transaction type maintenance
```

### VSAM-MQ Module

```
COACCT01 ◄── MQ request (transaction CDRA)
  └─ VSAM ACCTDAT read → MQ response with account data

CODATE01 ◄── MQ request (transaction CDRD)
  └─ System date → MQ response with formatted date
```

---

## 7. Shared Infrastructure Dependencies

| Infrastructure Component | Used By                                                    | Purpose                    |
|--------------------------|-------------------------------------------------------------|----------------------------|
| DFHAID (CICS AID)        | All 17 online programs                                     | Terminal key detection      |
| DFHBMSCA (CICS BMS)      | All 17 online programs                                     | Screen attribute constants  |
| CSSTRPFY (PF-Key Store)  | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                     | Map AID to PF-key variable  |
| CSSETATY (Field Attrs)   | COACTUPC (inline generation)                               | Set field colors on error   |
| CSUTLDWY + CSUTLDPY      | COACTUPC, COCRDUPC (via CSUTLDTC call)                     | Date validation suite       |
| CSLKPCDY (Lookup Codes)  | COACTUPC                                                   | Phone, state, ZIP validation|
| COCOM01Y (COMMAREA)      | All 17 online programs                                     | Inter-program state passing |
| COTTL01Y (Title)          | All 16 screen programs (not CSUTLDTC)                      | Screen header/title         |
| CSDAT01Y (Date/Time)     | All 17 online programs                                     | Current date/time display   |
| CSMSG01Y (Messages)      | All 17 online programs                                     | Standard user messages      |
