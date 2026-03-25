# Dependency Map - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Methodology:** Static analysis of COPY statements, EXEC CICS XCTL/LINK, EXEC CICS READ/WRITE/REWRITE, JCL EXEC PGM and DD statements

---

## 1. Online Program Call Graph (CICS XCTL)

The CardDemo online system uses **EXEC CICS XCTL** (transfer control) for screen navigation. There are no EXEC CICS LINK or COBOL CALL statements between online programs.

```
                              ┌─────────────┐
                              │  COSGN00C   │
                              │  (Sign-on)  │
                              │  Trans: CC00│
                              └──────┬──────┘
                                     │
                      ┌──────────────┼──────────────┐
                      │ Admin User   │              │ Regular User
                      ▼              │              ▼
               ┌─────────────┐      │       ┌─────────────┐
               │  COADM01C   │      │       │  COMEN01C   │
               │ (Admin Menu)│      │       │ (Main Menu) │
               │  Trans: CA00│      │       │  Trans: CM00│
               └──────┬──────┘      │       └──────┬──────┘
                      │             │              │
     ┌────────────────┤             │    ┌─────────┼─────────┬──────────┬──────────┬──────────┐
     │                │             │    │         │         │          │          │          │
     ▼                ▼             │    ▼         ▼         ▼          ▼          ▼          ▼
┌─────────┐    ┌─────────┐         │ ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│COUSR00C │    │COUSR01C │         │ │COACTVWC││COACTUPC││COCRDLIC││COTRN00C││CORPT00C││COBIL00C│
│User List│    │User Add │         │ │Acct Vw ││Acct Upd││Card Lst││Trn List││Reports ││Bill Pay│
└────┬────┘    └─────────┘         │ └───┬────┘└────────┘└───┬────┘└───┬────┘└────────┘└────────┘
     │                             │     │                   │         │
     ├──► COUSR02C (User Update)   │     │              ┌────┴────┐    │
     └──► COUSR03C (User Delete)   │     │              │COCRDSLC │    ├──► COTRN01C (Trn View)
                                   │     │              │Card View│    └──► COTRN02C (Trn Add)
                                   │     │              └────┬────┘
                                   │     │                   │
                                   │     │              ┌────┴────┐
                                   │     │              │COCRDUPC │
                                   │     │              │Card Upd │
                                   │     │              └─────────┘
                                   │     │
                                   │     └──► COCRDLIC (Card List from Acct View)
                                   │
                                   │  Optional (if installed):
                                   └──► COPAUS0C (Auth Summary) ──► COPAUS1C (Auth Detail)
                                                                  └──► COPAUS2C (Mark Fraud)
```

### 1.1 Detailed XCTL Transfer Table

| Source Program | Target Program | Trigger | Direction |
|---------------|----------------|---------|-----------|
| COSGN00C | COADM01C | Admin login success | Forward |
| COSGN00C | COMEN01C | User login success | Forward |
| COMEN01C | COACTVWC | Menu option 1 | Forward |
| COMEN01C | COACTUPC | Menu option 2 | Forward |
| COMEN01C | COCRDLIC | Menu option 3 | Forward |
| COMEN01C | COCRDSLC | Menu option 4 | Forward |
| COMEN01C | COCRDUPC | Menu option 5 | Forward |
| COMEN01C | COTRN00C | Menu option 6 | Forward |
| COMEN01C | COTRN01C | Menu option 7 | Forward |
| COMEN01C | COTRN02C | Menu option 8 | Forward |
| COMEN01C | CORPT00C | Menu option 9 | Forward |
| COMEN01C | COBIL00C | Menu option 10 | Forward |
| COMEN01C | COPAUS0C | Menu option 11 (optional) | Forward |
| COMEN01C | COSGN00C | PF3 (exit) | Back |
| COADM01C | COUSR00C | Admin option 1 | Forward |
| COADM01C | COUSR01C | Admin option 2 | Forward |
| COADM01C | COUSR02C | Admin option 3 | Forward |
| COADM01C | COUSR03C | Admin option 4 | Forward |
| COADM01C | COTRTLIC | Admin option 5 (DB2 optional) | Forward |
| COADM01C | COTRTUPC | Admin option 6 (DB2 optional) | Forward |
| COACTVWC | COMEN01C | PF3 (back to menu) | Back |
| COACTVWC | COCRDLIC | Navigate to card list | Forward |
| COCRDLIC | COCRDSLC | Select card (S) | Forward |
| COCRDLIC | COCRDUPC | Update card (U) | Forward |
| COCRDLIC | COMEN01C | PF3 | Back |
| COCRDSLC | COCRDLIC | PF3 | Back |
| COCRDSLC | COMEN01C | PF3 (no caller) | Back |
| COCRDUPC | COCRDLIC | PF3 | Back |
| COTRN00C | COTRN01C | Select transaction | Forward |
| COTRN00C | COMEN01C | PF3 | Back |
| COTRN01C | COMEN01C | PF3 | Back |
| COTRN02C | COMEN01C | PF3 | Back |
| CORPT00C | COMEN01C | PF3 | Back |
| COBIL00C | COMEN01C | PF3 | Back |
| COUSR00C | COUSR02C | Select user (U) | Forward |
| COUSR00C | COUSR03C | Select user (D) | Forward |
| COUSR00C | COADM01C | PF3 | Back |
| All COUSRxx | COADM01C | PF3 | Back |

---

## 2. Batch Program Call Graph

```
CBSTM03A ──CALL──► CBSTM03B  (Statement: main calls file I/O subroutine)

CBTRN02C  (standalone - reads daily trans, posts to master)
CBTRN03C  (standalone - reads trans, writes report)
CBACT04C  (standalone - interest calculation)
CBACT01C  (standalone - account data extract)
CBACT02C  (standalone - card data extract)
CBACT03C  (standalone - xref data extract)
CBCUS01C  (standalone - customer data extract)
CBEXPORT  (standalone - multi-file export)
CBIMPORT  (standalone - multi-file import)
COBSWAIT  (standalone - wait utility)
```

**Note:** The only batch inter-program call is CBSTM03A calling CBSTM03B as a subroutine.

---

## 3. Copybook Dependency Matrix

Shows which programs include which copybooks via COPY statements.

| Copybook | COSGN | COMEN | COADM | COACTVW | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00 | COTRN01 | COTRN02 | CORPT00 | COBIL00 | COUSR00 | COUSR01 | COUSR02 | COUSR03 |
|----------|:-----:|:-----:|:-----:|:-------:|:--------:|:--------:|:--------:|:--------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|
| COCOM01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COTTL01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSDAT01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSUSR01Y | x | x | x | x | x | x | x | x | | | | | | x | x | x | x |
| CSMSG02Y | | | | | x | | x | x | | | | | | | | | |
| COMEN02Y | | x | | | | | | | | | | | | | | | |
| COADM02Y | | | x | | | | | | | | | | | | | | |
| CVACT01Y | | | | x | x | | | | | | x | | x | | | | |
| CVACT02Y | | | | x | x | x | x | x | | | | | | | | | |
| CVACT03Y | | | | x | x | | | | | | x | | x | | | | |
| CVCUS01Y | | | | x | x | | x | x | | | | | | | | | |
| CVCRD01Y | | | | x | x | x | x | x | | | | | | | | | |
| CVTRA05Y | | | | | | | | | x | x | x | x | x | | | | |
| CSSTRPFY | | | | | x | x | x | x | | | | | | | | | |
| CSUTLDWY | | | | | x | | | | | | | | | | | | |

### Copybook Usage Summary

| Copybook | Used By (count) | Classification |
|----------|-----------------|----------------|
| COCOM01Y | 17 programs | Universal (COMMAREA) |
| COTTL01Y | 17 programs | Universal (screen titles) |
| CSDAT01Y | 17 programs | Universal (date/time) |
| CSMSG01Y | 17 programs | Universal (messages) |
| CSUSR01Y | 13 programs | Security (user record) |
| CVACT02Y | 6 programs | Card data layout |
| CVCRD01Y | 6 programs | Card working storage |
| CVTRA05Y | 5 programs | Transaction layout |
| CVACT01Y | 4 programs | Account data layout |
| CVACT03Y | 4 programs | Cross-reference layout |
| CVCUS01Y | 4 programs | Customer data layout |
| CSSTRPFY | 4 programs | String utilities |
| CSMSG02Y | 3 programs | Abend handling |

---

## 4. Data Lineage - VSAM File Access by Online Programs

| VSAM Dataset | Read | Write | Rewrite | Delete | Programs |
|-------------|:----:|:-----:|:-------:|:------:|----------|
| USRSEC (User Security) | x | x | x | x | COSGN00C (R), COUSR00C (R), COUSR01C (W), COUSR02C (R/RW), COUSR03C (R/D) |
| ACCTDAT (Account Master) | x | | x | | COACTVWC (R), COACTUPC (R/RW), COBIL00C (R/RW), COTRN02C (R) |
| CARDDAT (Card Master) | x | | x | | COCRDLIC (R), COCRDSLC (R), COCRDUPC (R/RW), COACTVWC (R) |
| CARDAIX (Card by Account) | x | | | | COACTVWC (R), COCRDLIC (R), COCRDSLC (R), COCRDUPC (R) |
| CXACAIX (Xref by Account) | x | | | | COACTVWC (R), COACTUPC (R), COTRN02C (R), COBIL00C (R) |
| CUSTDAT (Customer Master) | x | | x | | COACTVWC (R), COACTUPC (R/RW), COCRDSLC (R) |
| TRANSACT (Transaction Master) | x | x | | | COTRN00C (R), COTRN01C (R), COTRN02C (R/W), COBIL00C (R/W) |

---

## 5. Data Lineage - Batch File I/O

### 5.1 Batch Program File Access

| Program | Input Files | Output Files | Access Pattern |
|---------|-------------|-------------|----------------|
| **CBTRN02C** | DALYTRAN (daily trans), XREFFILE (card xref), ACCTFILE (accounts), TCATBALF (cat balance) | TRANFILE (trans master), DALYREJS (rejects) | Read daily → validate → post to master |
| **CBTRN01C** | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | -- (validation only) | Read daily → cross-validate all masters |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) | Read trans → join ref data → write report |
| **CBACT04C** | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANFILE | ACCTFILE (update), TRANFILE (write interest trans) | Calculate interest → update accounts |
| **CBSTM03A** | TRNXFILE (sorted trans), XREFFILE, ACCTFILE, CUSTFILE | STMTFILE (text), HTMLFILE (HTML) | Read sorted trans → generate statements |
| **CBSTM03B** | (called by CBSTM03A) | (called by CBSTM03A) | Subroutine for file I/O |
| **CBACT01C** | ACCTFILE | SYSOUT (print) | Read → display account data |
| **CBACT02C** | CARDFILE | SYSOUT (print) | Read → display card data |
| **CBACT03C** | XREFFILE | SYSOUT (print) | Read → display xref data |
| **CBCUS01C** | CUSTFILE | SYSOUT (print) | Read → display customer data |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANFILE, CARDFILE | EXPORTFL (export file) | Read all → multi-record export |
| **CBIMPORT** | EXPORTFL (import file) | CUSTFILE, ACCTFILE, XREFFILE, TRANFILE, CARDFILE, ERRFILE | Read export → split into targets |

### 5.2 JCL Job Data Flow

```
                          ┌─────────────────────────────────────────────┐
                          │           DATA REFRESH PHASE                │
                          │  (Sequential PS files → VSAM KSDS)         │
                          └─────────────────────────────────────────────┘

  acctdata.txt ──► ACCTFILE.jcl ──► ACCTDATA.VSAM.KSDS
  carddata.txt ──► CARDFILE.jcl ──► CARDDATA.VSAM.KSDS
  custdata.txt ──► CUSTFILE.jcl ──► CUSTDATA.VSAM.KSDS
  cardxref.txt ──► XREFFILE.jcl ──► CARDXREF.VSAM.KSDS (+ AIX)
  dailytran.txt──► TRANFILE.jcl ──► TRANSACT.VSAM.KSDS (+ AIX)
  usrsec.txt   ──► DUSRSECJ.jcl──► USRSEC.VSAM.KSDS
  trantype.txt ──► TRANTYPE.jcl──► TRANTYPE.VSAM.KSDS
  trancatg.txt ──► TRANCATG.jcl──► TRANCATG.VSAM.KSDS
  tcatbal.txt  ──► TCATBALF.jcl──► TCATBALF.VSAM.KSDS
  discgrp.txt  ──► DISCGRP.jcl ──► DISCGRP.VSAM.KSDS

                          ┌─────────────────────────────────────────────┐
                          │       BATCH PROCESSING PHASE               │
                          └─────────────────────────────────────────────┘

  DALYTRAN.PS ──► POSTTRAN.jcl (CBTRN02C) ──► TRANSACT.VSAM.KSDS (updated)
                                            ├──► TCATBALF.VSAM.KSDS (updated)
                                            ├──► ACCTDATA.VSAM.KSDS (bal updated)
                                            └──► DALYREJS (rejected records)

  ACCTDATA ───┐
  TCATBALF ───┤
  XREFFILE ───┼──► INTCALC.jcl (CBACT04C) ──► ACCTDATA.VSAM (interest applied)
  DISCGRP  ───┤                             └──► TRANSACT.VSAM (interest trans)
  TRANFILE ───┘

  TRANSACT.VSAM ──► TRANBKP.jcl (REPROC) ──► TRANSACT.BKUP(+1) GDG

  TRANSACT.BKUP ──► COMBTRAN.jcl (SORT) ──► TRANSACT.DALY(+1) GDG

  TRANSACT.VSAM ──┐
  CARDXREF ───────┼──► CREASTMT.JCL (SORT → CBSTM03A) ──► STATEMNT.PS
  ACCTDATA ───────┤                                    └──► STATEMNT.HTML
  CUSTDATA ───────┘

  TRANSACT.DALY ──┐
  CARDXREF ───────┤
  TRANTYPE ───────┼──► TRANREPT.jcl (SORT → CBTRN03C) ──► TRANREPT(+1) GDG
  TRANCATG ───────┤
  DATEPARM ───────┘

                          ┌─────────────────────────────────────────────┐
                          │     ALTERNATE INDEX MANAGEMENT             │
                          └─────────────────────────────────────────────┘

  TRANIDX.jcl ──► Defines/rebuilds AIX on TRANSACT.VSAM.KSDS
  XREFFILE.jcl──► Defines/rebuilds AIX on CARDXREF.VSAM.KSDS (by account)
```

### 5.3 Complete Batch Cycle Sequence

```
Step  Job        Purpose                              Dependencies
────  ─────────  ───────────────────────────────────  ────────────────────────
 1    CLOSEFIL   Close CICS files for batch window    None (start of batch)
 2    ACCTFILE   Refresh account master               CLOSEFIL
 3    CARDFILE   Refresh card master                  CLOSEFIL
 4    CUSTFILE   Refresh customer master              CLOSEFIL
 5    XREFFILE   Load cross-reference + AIX           CLOSEFIL
 6    TRANFILE   Load transaction master + AIX        CLOSEFIL
 7    POSTTRAN   Post daily transactions              Steps 2-6
 8    INTCALC    Calculate interest                   POSTTRAN
 9    TRANBKP    Backup transactions to GDG           INTCALC
10    COMBTRAN   Combine/sort transactions            TRANBKP
11    CREASTMT   Generate account statements          Steps 2-6, POSTTRAN
12    TRANREPT   Generate transaction reports         COMBTRAN
13    TRANIDX    Rebuild alternate indexes            Steps 7-12
14    OPENFIL    Reopen CICS files                    TRANIDX (end of batch)
```

---

## 6. BMS Map to Program Mapping

| BMS Mapset | BMS Map | COBOL Program | Copybook (cpy-bms) |
|-----------|---------|---------------|-------------------|
| COSGN00 | COSGN0A | COSGN00C | COSGN00.CPY |
| COMEN01 | COMEN1A | COMEN01C | COMEN01.CPY |
| COADM01 | COADM1A | COADM01C | COADM01.CPY |
| COACTVW | CACTVWA | COACTVWC | COACTVW.CPY |
| COACTUP | CACTUPA | COACTUPC | COACTUP.CPY |
| COCRDLI | CCRDLIA | COCRDLIC | COCRDLI.CPY |
| COCRDSL | CCRDSLA | COCRDSLC | COCRDSL.CPY |
| COCRDUP | CCRDUPA | COCRDUPC | COCRDUP.CPY |
| COTRN00 | COTRN0A | COTRN00C | COTRN00.CPY |
| COTRN01 | COTRN1A | COTRN01C | COTRN01.CPY |
| COTRN02 | COTRN2A | COTRN02C | COTRN02.CPY |
| CORPT00 | CORPT0A | CORPT00C | CORPT00.CPY |
| COBIL00 | COBIL0A | COBIL00C | COBIL00.CPY |
| COUSR00 | COUSR0A | COUSR00C | COUSR00.CPY |
| COUSR01 | COUSR1A | COUSR01C | COUSR01.CPY |
| COUSR02 | COUSR2A | COUSR02C | COUSR02.CPY |
| COUSR03 | COUSR3A | COUSR03C | COUSR03.CPY |

---

## 7. Optional Module Dependencies

### 7.1 Authorization Module (IMS/DB2/MQ)

```
COMEN01C ──XCTL──► COPAUS0C (Summary) ──XCTL──► COPAUS1C (Detail)
                                        ──XCTL──► COPAUS2C (Mark Fraud → DB2)

COPAUA0C ◄── MQ Trigger (incoming auth request)
         ──► IMS DB (auth decision lookup)
         ──► MQ Reply (auth response)

CBPAUP0C ◄── JCL: CBPAUP0J.jcl (batch purge expired auths)
```

Additional copybooks: CCPAUERY, CCPAURLY, CCPAURQY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PADFLPCB, PASFLPCB, PAUTBPCB

### 7.2 Transaction Type DB2 Module

```
COADM01C ──XCTL──► COTRTLIC (List/Delete via DB2 cursors)
                  ──XCTL──► COTRTUPC (Add/Edit via DB2 SQL)

COBTUPDT ◄── JCL: MNTTRDB2.jcl (batch maintenance)
```

Additional copybooks: CSDB2RPY, CSDB2RWY (DB2 reply/working areas)

### 7.3 VSAM-MQ Module

```
MQ Request (CDRD) ──► CODATE01 ──► MQ Response (system date)
MQ Request (CDRA) ──► COACCT01 ──► VSAM ACCTDAT ──► MQ Response (account data)
```

---

## 8. Cross-Cutting Concerns

### 8.1 Shared Utility Dependencies

| Utility | Used By | Purpose |
|---------|---------|---------|
| CSUTLDTC (Date validation) | COACTUPC, COCRDUPC | Validate CCYYMMDD dates via CEEDAYS |
| COBSWAIT (Wait utility) | WAITSTEP.jcl | Job step delays |
| CSSTRPFY (String functions) | COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | Strip/pad strings |
| DFHAID (CICS AID keys) | All 17 online programs | Function key detection |
| DFHBMSCA (BMS attributes) | All 17 online programs | Screen field attributes |

### 8.2 Error Handling Pattern

All online programs follow the same error-handling pattern:
1. `WS-RESP-CD` / `WS-REAS-CD` capture CICS response codes
2. `WS-FILE-ERROR-MESSAGE` constructs file error details
3. `ABEND-ROUTINE` paragraph handles unexpected abends (using CSMSG02Y)
4. `SEND-PLAIN-TEXT` displays fatal errors before returning
