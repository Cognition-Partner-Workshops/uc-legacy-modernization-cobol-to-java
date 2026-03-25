# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: CardDemo — Mainframe Credit Card Management System
> **Scope**: Program-to-program call graph, copybook inclusion matrix, CICS screen flow, JCL data lineage

---

## 1. Program Call Graph

### 1.1 Online CICS Program Navigation (XCTL / LINK)

The CICS online programs navigate between each other using `EXEC CICS XCTL` (transfer control) and the COMMAREA (`COCOM01Y`). The sign-on screen is the entry point.

```
                              ┌──────────┐
                              │ COSGN00C │  (Sign-On, CC00)
                              │  Entry   │
                              └────┬─────┘
                                   │ XCTL (auth success)
                         ┌─────────┴─────────┐
                         │                   │
                    ┌────┴─────┐       ┌────┴─────┐
                    │COMEN01C  │       │COADM01C  │
                    │Main Menu │       │Admin Menu │
                    │(Regular) │       │(Admin)    │
                    └────┬─────┘       └────┬─────┘
                         │                   │
        ┌────────┬───────┼────────┬──────┐   ├── COUSR00C (User List)
        │        │       │        │      │   │     ├── COUSR01C (Add User)
        ▼        ▼       ▼        ▼      ▼   │     ├── COUSR02C (Update User)
   COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C    └── COUSR03C (Delete User)
   Acct View Card List Tran List Reports Bill Pay
        │        │       │
        │   ┌────┴────┐  ├── COTRN01C (Tran View)
        │   │         │  └── COTRN02C (Tran Add)
        │   ▼         ▼
        │ COCRDSLC  COCRDUPC
        │ Card View Card Update
        │
        └── COACTUPC (Account Update)
```

### 1.2 Direct CALL Dependencies (Batch & Utility)

Programs that use the COBOL `CALL` statement to invoke subroutines:

```
CBSTM03A ──CALL──► CBSTM03B     (Statement generation calls file I/O subroutine)
                                  Called 13 times within CBSTM03A

CBACT01C ──CALL──► COBDATFT     (Account read calls assembler date formatter)
CBACT01C ──CALL──► CEE3ABD      (Abnormal termination)

CBACT02C ──CALL──► CEE3ABD      (Abnormal termination)
CBACT03C ──CALL──► CEE3ABD      (Abnormal termination)
CBACT04C ──CALL──► CEE3ABD      (Abnormal termination)
CBCUS01C ──CALL──► CEE3ABD      (Abnormal termination)

CBTRN01C ──CALL──► CEE3ABD      (Abnormal termination)
CBTRN02C ──CALL──► CEE3ABD      (Abnormal termination)
CBTRN03C ──CALL──► CEE3ABD      (Abnormal termination)

CBEXPORT ──CALL──► CEE3ABD      (Abnormal termination)
CBIMPORT ──CALL──► CEE3ABD      (Abnormal termination)

COBSWAIT ──CALL──► MVSWAIT      (Wait utility calls assembler wait service)

CORPT00C ──CALL──► CSUTLDTC     (Report calls date validation utility)
                                  Called 2 times (start date + end date validation)

CSUTLDTC ──CALL──► CEEDAYS      (Date utility calls LE date service)
```

### 1.3 Consolidated Call Graph (All Callers → Callees)

| Caller | Callee | Call Type | Purpose |
|--------|--------|-----------|---------|
| COSGN00C | COMEN01C | XCTL | Navigate to main menu (regular user) |
| COSGN00C | COADM01C | XCTL | Navigate to admin menu (admin user) |
| COMEN01C | COACTVWC | XCTL | Account view |
| COMEN01C | COCRDLIC | XCTL | Card list |
| COMEN01C | COTRN00C | XCTL | Transaction list |
| COMEN01C | CORPT00C | XCTL | Reports |
| COMEN01C | COBIL00C | XCTL | Bill payment |
| COADM01C | COUSR00C | XCTL | User list |
| COCRDLIC | COCRDSLC | XCTL | Card detail view |
| COCRDLIC | COCRDUPC | XCTL | Card update |
| COCRDLIC | COMEN01C | XCTL | Return to menu |
| COACTVWC | COACTUPC | XCTL | Account update |
| CORPT00C | CSUTLDTC | CALL | Date validation |
| CBSTM03A | CBSTM03B | CALL | File I/O subroutine |
| CBACT01C | COBDATFT | CALL | Date formatting (assembler) |
| COBSWAIT | MVSWAIT | CALL | Wait service (assembler) |
| CSUTLDTC | CEEDAYS | CALL | LE date conversion |
| All batch | CEE3ABD | CALL | Abnormal termination handler |

### 1.4 Optional Module Call Graphs

#### Authorization Module (IMS/DB2/MQ)
```
MQ Trigger ──► COPAUA0C ──MQ GET/PUT──► MQ Queues
                  │
                  ├──EXEC DLI──► IMS DB (Auth segments)
                  ├──EXEC CICS READ──► VSAM (Account, Card, XREF)
                  │
COPAUS0C ──EXEC DLI──► IMS DB (summary view)
COPAUS1C ──EXEC DLI──► IMS DB (detail view)
    │
    └──EXEC CICS LINK──► (subroutine calls)

COPAUS2C ──EXEC SQL──► DB2 (fraud marking)

CBPAUP0C ──EXEC DLI──► IMS DB (batch purge)
DBUNLDGS ──CALL CBLTDLI──► IMS DB (unload to flat file)
PAUDBLOD ──CALL CBLTDLI──► IMS DB (load from flat file)
PAUDBUNL ──CALL CBLTDLI──► IMS DB (unload segments)
```

#### Transaction Type DB2 Module
```
COTRTUPC ──EXEC SQL──► DB2 TRTYP/TRCAT tables (add/edit)
COTRTLIC ──EXEC SQL──► DB2 TRTYP table (list/delete with cursors)
COBTUPDT ──EXEC SQL──► DB2 TRTYP table (batch update)
```

#### VSAM-MQ Module
```
COACCT01 ──MQ GET──► Request Queue ──EXEC CICS READ──► VSAM Account
         ──MQ PUT──► Reply Queue

CODATE01 ──MQ GET──► Request Queue ──EXEC CICS ASKTIME──► System Date
         ──MQ PUT──► Reply Queue
```

---

## 2. Copybook Inclusion Matrix

Shows which programs include which copybooks (core programs only).

### 2.1 Data Structure Copybooks

| Copybook | Domain | CBACT01C | CBACT04C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT | COACTUPC | COACTVWC | COCRDLIC | COCRDSLC | COCRDUPC | COBIL00C | COTRN00C | COTRN02C | CORPT00C | COSGN00C | COMEN01C | COADM01C | COUSR00C-03C |
|----------|--------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|
| CVACT01Y | Account | X | X | X | X | | X | X | X | X | X | | | | X | | | | | | | |
| CVACT02Y | Card | | | | | | | X | X | | X | X | X | X | | | | | | | | |
| CVACT03Y | XREF | | X | X | X | X | X | X | X | X | X | | | | X | | | | | | | |
| CVCUS01Y | Customer | | | X | | | | X | X | X | X | | X | X | | | | | | | | |
| CVCRD01Y | Card UI | | | | | | | | | X | X | X | X | X | | | | | | | | |
| CVTRA01Y | Cat Bal | | X | | X | | | | | | | | | | | | | | | | | |
| CVTRA02Y | Disc Grp | | X | | | | | | | | | | | | | | | | | | | |
| CVTRA03Y | Tran Type | | | | | X | | | | | | | | | | | | | | | | |
| CVTRA04Y | Tran Cat | | | | | X | | | | | | | | | | | | | | | | |
| CVTRA05Y | Trans | | X | X | X | X | | X | X | | | | | | X | X | X | X | | | | |
| CVTRA06Y | Daily Tr | | | X | X | | | | | | | | | | | | | | | | | |
| CVTRA07Y | Rpt Fmt | | | | | X | | | | | | | | | | | | | | | | |
| COSTM01 | Stmt Fmt | | | | | | X | | | | | | | | | | | | | | | |
| CVEXPORT | Export | | | | | | | X | X | | | | | | | | | | | | | |

### 2.2 Common / UI Copybooks

| Copybook | Purpose | All Online | Batch |
|----------|---------|-----------|-------|
| COCOM01Y | COMMAREA | All 17 online programs | — |
| COTTL01Y | Screen titles | All 17 online programs | — |
| CSDAT01Y | Date/time | All 17 online programs | — |
| CSMSG01Y | Messages | All 17 online programs | — |
| CSUSR01Y | User security | All 17 online programs | — |
| DFHAID | CICS attention IDs | All 17 online programs | — |
| DFHBMSCA | BMS attributes | All 17 online programs | — |
| CSMSG02Y | Extended messages | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | — |
| CSSETATY | Attribute setting | COACTUPC (39 COPY REPLACING) | — |
| CSSTRPFY | String parsing | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | — |
| CSUTLDPY | Date util params | COACTUPC | — |
| CSUTLDWY | Date util WS | COACTUPC | — |
| CSLKPCDY | Lookup codes | COACTUPC | — |
| CODATECN | Date conversion | CBACT01C | — |

---

## 3. JCL Data Lineage

### 3.1 Batch Job → Program → File I/O Matrix

| JCL Job | Program Executed | Files READ | Files WRITTEN | Files UPDATED |
|---------|-----------------|------------|---------------|---------------|
| READACCT | CBACT01C | ACCTDATA.VSAM.KSDS | OUTFILE, ARRYFILE, VBRCFILE | — |
| READCARD | CBACT02C | CARDDATA.VSAM.KSDS | SYSOUT (print) | — |
| READXREF | CBACT03C | CARDXREF.VSAM.KSDS | SYSOUT (print) | — |
| READCUST | CBCUS01C | CUSTDATA.VSAM.KSDS | SYSOUT (print) | — |
| POSTTRAN | CBTRN02C | DALYTRAN.PS | DALYREJS.PS | TRANSACT.VSAM.KSDS, ACCTDATA.VSAM.KSDS, TCATBAL.VSAM.KSDS, CARDXREF.VSAM.KSDS |
| INTCALC | CBACT04C | TCATBAL.VSAM.KSDS, DISCGRP.VSAM.KSDS, CARDXREF.VSAM.KSDS | TRANSACT (sequential out) | ACCTDATA.VSAM.KSDS |
| CREASTMT | CBSTM03A/B | TRXFL.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, CUSTDATA.VSAM.KSDS | STATEMNT.PS, STATEMNT.HTML | — |
| TRANREPT | CBTRN03C | TRANSACT (sequential), CARDXREF.VSAM.KSDS, TRANTYPE.VSAM.KSDS, TRANCATG.VSAM.KSDS, DATEPARM | TRANREPT (report output) | — |
| CBEXPORT | CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA (all VSAM) | EXPFILE (export output) | — |
| CBIMPORT | CBIMPORT | EXPFILE (export input) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | — |

### 3.2 Data File Refresh Jobs (IDCAMS Operations)

These jobs define VSAM clusters and load data from flat files. No COBOL programs are executed.

| JCL Job | VSAM Dataset Created/Refreshed | Source Flat File |
|---------|-------------------------------|-----------------|
| ACCTFILE | ACCTDATA.VSAM.KSDS | ACCTDATA.PS |
| CARDFILE | CARDDATA.VSAM.KSDS | CARDDATA.PS |
| CUSTFILE | CUSTDATA.VSAM.KSDS | CUSTDATA.PS |
| XREFFILE | CARDXREF.VSAM.KSDS + AIX | CARDXREF.PS |
| TRANFILE | TRANSACT.VSAM.KSDS | TRANSACT.PS |
| DUSRSECJ | USRSEC.VSAM.KSDS | USRSEC.PS |
| TCATBALF | TCATBAL.VSAM.KSDS | TCATBAL.PS |
| TRANCATG | TRANCATG.VSAM.KSDS | TRANCATG.PS |
| TRANTYPE | TRANTYPE.VSAM.KSDS | TRANTYPE.PS |
| DISCGRP | DISCGRP.VSAM.KSDS | DISCGRP.PS |
| DALYREJS | DALYREJS.PS | (define only) |
| REPTFILE | Multiple reporting files | Multiple PS files |

### 3.3 Nightly Batch Cycle — Data Flow

```
                 ┌─────────────────────────────────────────────────────────────────┐
                 │                    NIGHTLY BATCH CYCLE                          │
                 └─────────────────────────────────────────────────────────────────┘

  ┌──────────┐     ┌──────────────────────┐     ┌──────────┐
  │ CLOSEFIL │────►│ Data Refresh Jobs    │────►│ POSTTRAN │
  │ Close    │     │ ACCTFILE, CARDFILE,  │     │ CBTRN02C │
  │ CICS     │     │ CUSTFILE, XREFFILE,  │     │          │
  │ Files    │     │ TRANFILE, DUSRSECJ   │     │          │
  └──────────┘     └──────────────────────┘     └────┬─────┘
                                                      │
                        Reads: DALYTRAN.PS ───────────┘
                        Updates: TRANSACT, ACCTDATA, TCATBAL, CARDXREF
                        Writes: DALYREJS.PS (rejected transactions)
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ INTCALC  │
                                               │ CBACT04C │
                                               └────┬─────┘
                                                      │
                        Reads: TCATBAL, DISCGRP, CARDXREF
                        Updates: ACCTDATA (interest applied)
                        Writes: TRANSACT (interest transactions)
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ TRANBKP  │
                                               │ IDCAMS   │
                                               └────┬─────┘
                                                      │
                        Copies: TRANSACT.VSAM → TRANSACT.GDG (backup)
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ COMBTRAN │
                                               │ SORT     │
                                               └────┬─────┘
                                                      │
                        Merges: DALYTRAN into TRANSACT master
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ CREASTMT │
                                               │CBSTM03A/B│
                                               └────┬─────┘
                                                      │
                        Reads: TRXFL, CARDXREF, ACCTDATA, CUSTDATA
                        Writes: STATEMNT.PS (text), STATEMNT.HTML
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ TRANIDX  │
                                               │ IDCAMS   │
                                               └────┬─────┘
                                                      │
                        Rebuilds: Alternate indexes on TRANSACT
                                                      │
                                                      ▼
                                               ┌──────────┐
                                               │ OPENFIL  │
                                               │ Re-open  │
                                               │ CICS     │
                                               │ Files    │
                                               └──────────┘
```

### 3.4 CICS Online — VSAM File Access

| VSAM File | CICS DD Name | Programs That READ | Programs That WRITE | Programs That UPDATE |
|-----------|-------------|-------------------|--------------------|--------------------|
| USRSEC.VSAM.KSDS | USRSEC | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C |
| ACCTDATA.VSAM.KSDS | ACCTDAT | COACTVWC, COACTUPC, COBIL00C | — | COACTUPC, COBIL00C |
| CARDDATA.VSAM.KSDS | CARDDAT | COCRDLIC, COCRDSLC, COCRDUPC | — | COCRDUPC |
| CARDXREF.VSAM.KSDS | CARDXREF | COACTVWC, COACTUPC, COCRDLIC, COBIL00C | — | — |
| CUSTDATA.VSAM.KSDS | CUSTDAT | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | — | — |
| TRANSACT.VSAM.KSDS | TRANSACT | COTRN00C, COTRN01C | COTRN02C, COBIL00C | — |

---

## 4. Screen Flow Map

### 4.1 Regular User Flow
```
COSGN00 (Sign-On)
    │
    └──► COMEN01 (Main Menu)
              │
              ├─[1]──► COACTVW (Account View) ──► COACTUP (Account Update)
              │
              ├─[2]──► COCRDLI (Card List)
              │            ├──► COCRDSL (Card Detail View)
              │            └──► COCRDUP (Card Update)
              │
              ├─[3]──► COTRN00 (Transaction List)
              │            ├──► COTRN01 (Transaction View)
              │            └──► COTRN02 (Transaction Add)
              │
              ├─[4]──► COBIL00 (Bill Payment)
              │
              └─[5]──► CORPT00 (Transaction Reports)
```

### 4.2 Admin User Flow
```
COSGN00 (Sign-On)
    │
    └──► COADM01 (Admin Menu)
              │
              ├─[1]──► COUSR00 (User List)
              │            ├──► COUSR01 (Add User)
              │            ├──► COUSR02 (Update User)
              │            └──► COUSR03 (Delete User)
              │
              └─ (same menu options as regular user available)
```

---

## 5. Technology Dependency Summary

| Technology | Core Programs Using It | Optional Programs Using It |
|-----------|----------------------|--------------------------|
| CICS | All 17 online programs | COPAUA0C, COPAUS0C/1C/2C, COTRTUPC, COTRTLIC, COACCT01, CODATE01 |
| VSAM KSDS | All online + all batch | COPAUA0C, COACCT01 |
| BMS Maps | All 17 online programs | COPAUS0C/1C, COTRTUPC, COTRTLIC |
| Sequential Files | CBTRN01C/02C/03C, CBACT01C, CBSTM03A/B | — |
| IMS DL/I | — | COPAUA0C, COPAUS0C/1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| DB2 SQL | — | COPAUS2C, COTRTUPC, COTRTLIC, COBTUPDT |
| MQ Series | — | COPAUA0C, COACCT01, CODATE01 |
| LE Services | CSUTLDTC, all batch (CEE3ABD) | — |
| Assembler | CBACT01C (COBDATFT), COBSWAIT (MVSWAIT) | — |
