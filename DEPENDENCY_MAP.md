# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Scope:** Call graph, CICS transfer control, copybook inclusion, and JCL data lineage

---

## 1. Program Call Graph

### 1.1 Online CICS Program Flow

The online system uses **EXEC CICS XCTL** (transfer control — no return) for screen navigation and **EXEC CICS LINK** for subroutine calls. All programs share state via the **COMMAREA** (copybook `COCOM01Y`).

```
                          ┌──────────────────────────────────────────────┐
                          │            CICS Transaction CC00             │
                          │              COSGN00C (Sign-on)              │
                          │  Reads: USRSEC VSAM                         │
                          └──────────────┬───────────────────────────────┘
                                         │ XCTL (on successful login)
                                         ▼
                          ┌──────────────────────────────────────────────┐
                          │            CICS Transaction CM00             │
                          │           COMEN01C (Main Menu)               │
                          │  Routes to functional areas based on         │
                          │  menu option via COMEN02Y routing table      │
                          └──┬──────┬──────┬──────┬──────┬──────┬───────┘
                             │      │      │      │      │      │
              ┌──────────────┘      │      │      │      │      └────────────────┐
              ▼                     ▼      │      │      ▼                       ▼
    ┌─────────────────┐  ┌──────────────┐  │      │  ┌──────────────┐  ┌──────────────┐
    │ COACTVWC        │  │ COCRDLIC     │  │      │  │ COTRN00C     │  │ COADM01C     │
    │ Account View    │  │ Card List    │  │      │  │ Tran List    │  │ Admin Menu   │
    │ CA00            │  │ CC01         │  │      │  │ CT00         │  │ CA90         │
    └────────┬────────┘  └──┬───────┬──┘  │      │  └──┬───────┬──┘  └──┬──────────┘
             │              │       │     │      │     │       │        │
             ▼              ▼       ▼     │      │     ▼       ▼        ├──→ COUSR00C
    ┌─────────────────┐  COCRDSLC COCRDUPC│      │  COTRN01C COTRN02C  │    User List
    │ COACTUPC        │  Card     Card    │      │  Tran     Tran      │    CU00
    │ Account Update  │  View     Update  │      │  View     Add       │    │
    │ CA01            │  CC02     CC03    │      │  CT01     CT02      │    ├→ COUSR01C
    └─────────────────┘                   │      │                     │    │  Add User
                                          ▼      ▼                     │    ├→ COUSR02C
                                  ┌──────────────────┐                 │    │  Update User
                                  │ CORPT00C  COBIL00C│                │    └→ COUSR03C
                                  │ Reports   Billing │                │       Delete User
                                  │ CR00      CB00    │                │
                                  └───────────────────┘                │
                                                                       │
                                                        (Admin-only programs)
```

### 1.2 Direct CALL Dependencies

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| COSGN00C | COMEN01C | EXEC CICS XCTL | Navigate to main menu after login |
| COSGN00C | COADM01C | EXEC CICS XCTL | Navigate to admin menu (admin users) |
| COMEN01C | CO*xxxxx* | EXEC CICS XCTL | Route to selected functional program |
| COCRDLIC | COCRDSLC | EXEC CICS XCTL | Card list → card detail view |
| COCRDLIC | COCRDUPC | EXEC CICS XCTL | Card list → card update |
| COCRDSLC | COMEN01C | EXEC CICS XCTL | Return to main menu |
| COCRDUPC | COMEN01C | EXEC CICS XCTL | Return to main menu |
| COACTUPC | COMEN01C | EXEC CICS XCTL | Return to main menu |
| COUSR00C | COUSR01C | EXEC CICS XCTL | User list → add user |
| COUSR00C | COUSR02C | EXEC CICS XCTL | User list → update user |
| CORPT00C | CSUTLDTC | CALL (static) | Date validation utility |
| COTRN02C | CSUTLDTC | CALL (static) | Date validation utility |
| COBSWAIT | MVSWAIT | CALL (assembler) | Invoke assembler wait routine |
| CBSTM03A | CBSTM03B | CALL (static) | Statement I/O subroutine (13 calls) |
| CBSTM03A | CEE3ABD | CALL (LE runtime) | Abnormal termination |
| CBIMPORT | CEE3ABD | CALL (LE runtime) | Abnormal termination |
| CSUTLDTC | CEEDAYS | CALL (LE runtime) | LE date conversion service |

### 1.3 Optional Module Call Dependencies

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| COPAUA0C | MQOPEN/MQGET/MQPUT | CALL (MQ API) | MQ message processing |
| COPAUS1C | COPAUS2C | EXEC CICS LINK | Link to fraud-marking subprogram |
| COACCT01 | MQOPEN/MQGET/MQPUT | CALL (MQ API) | Account inquiry via MQ |
| CODATE01 | MQOPEN/MQGET/MQPUT | CALL (MQ API) | System date via MQ |
| DBUNLDGS | CBLTDLI | CALL (IMS DL/I) | IMS database operations |
| PAUDBLOD | CBLTDLI | CALL (IMS DL/I) | IMS database load |
| PAUDBUNL | CBLTDLI | CALL (IMS DL/I) | IMS database unload |

---

## 2. Copybook Inclusion Map

### 2.1 Shared Copybooks by Program

Each column shows which copybook a program includes. Programs with more copybook dependencies are more tightly coupled.

| Copybook | Business Role | Included By |
|----------|--------------|-------------|
| **COCOM01Y** | COMMAREA | All 18 online programs |
| **COTTL01Y** | Screen title | All 17 screen programs |
| **CSDAT01Y** | Date formatting | All 17 screen programs |
| **CSMSG01Y** | Message area | All 17 screen programs |
| **CSUSR01Y** | User security record | COSGN00C, COMEN01C, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C–03C |
| **DFHAID** | CICS AID keys | All 17 screen programs |
| **DFHBMSCA** | BMS attributes | All 17 screen programs |
| **CVACT01Y** | Account record | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBSTM03A, CBEXPORT |
| **CVACT02Y** | Card record | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C |
| **CVACT03Y** | Cross-reference | COBIL00C, COTRN02C, CBACT03C, CBSTM03A |
| **CVCUS01Y** | Customer record | COCRDSLC, COCRDUPC, CBCUS01C, CBEXPORT |
| **CVTRA05Y** | Transaction record | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C, CBTRN01C, CBTRN02C |
| **CSMSG02Y** | Extended message | COCRDSLC, COCRDUPC, CORPT00C |
| **COMEN02Y** | Menu definitions | COMEN01C |
| **COADM02Y** | Admin menu defs | COADM01C |
| **CSSTRPFY** | String utility | COCRDLIC, COCRDSLC, COCRDUPC |
| **CSUTLDPY** | Date utility params | CORPT00C, COTRN02C (via CSUTLDTC) |
| **CVTRA07Y** | Report layouts | CBTRN03C |
| **COSTM01** | Statement tran layout | CBSTM03A |
| **CVEXPORT** | Export record | CBEXPORT, CBIMPORT |
| **CUSTREC** | Customer (statement) | CBSTM03A |

### 2.2 Copybook Dependency Counts

| Program | # Copybooks Used | Coupling Level |
|---------|-----------------|----------------|
| COCRDLIC | 11 | Very High |
| COCRDSLC | 12 | Very High |
| COCRDUPC | 12 | Very High |
| COACTUPC | 10+ | Very High |
| COBIL00C | 10 | High |
| COTRN02C | 10 | High |
| CORPT00C | 8 | High |
| COTRN00C | 8 | High |
| COUSR00C–03C | 8 each | Moderate |
| COMEN01C | 9 | Moderate |
| COSGN00C | 8 | Moderate |
| COADM01C | 10 | Moderate |
| CBSTM03A | 4 | Low |
| CBTRN03C | 3 | Low |
| CBEXPORT | 2 | Low |

---

## 3. VSAM File Access Map

### 3.1 Online Program → VSAM File Access

| Program | VSAM File | Operations | Access Pattern |
|---------|-----------|-----------|---------------|
| **COSGN00C** | USRSEC | READ | Authenticate user by ID |
| **COACTVWC** | ACCTDATA | READ, STARTBR, READNEXT, READPREV | Browse accounts |
| | CARDXREF | READ, STARTBR, READNEXT | Lookup cards for account |
| **COACTUPC** | ACCTDATA | READ, REWRITE | Update account details |
| | CARDXREF | READ | Lookup for validation |
| | CUSTDATA | READ | Customer info display |
| **COCRDLIC** | CARDDATA | STARTBR, READNEXT, READPREV, ENDBR | Browse card list |
| **COCRDSLC** | CARDDATA | READ | View card details |
| | CUSTDATA | READ | Customer name lookup |
| **COCRDUPC** | CARDDATA | READ, REWRITE | Update card |
| | CUSTDATA | READ | Customer name lookup |
| **COTRN00C** | TRANSACT | STARTBR, READNEXT, READPREV, ENDBR | Browse transactions |
| **COTRN01C** | TRANSACT | READ | View transaction detail |
| **COTRN02C** | TRANSACT | WRITE | Add new transaction |
| | ACCTDATA | READ | Account validation |
| | CARDXREF | STARTBR, READPREV, ENDBR | Card lookup |
| **COBIL00C** | TRANSACT | WRITE | Create payment transaction |
| | ACCTDATA | READ, REWRITE | Update balance |
| | CARDXREF | STARTBR, READPREV, ENDBR | Card lookup |
| **CORPT00C** | (TDQ/INTRDR) | WRITEQ TD | Submit report JCL |
| **COUSR00C** | USRSEC | STARTBR, READNEXT, READPREV, ENDBR | Browse users |
| **COUSR01C** | USRSEC | WRITE | Add user |
| **COUSR02C** | USRSEC | READ, REWRITE | Update user |
| **COUSR03C** | USRSEC | READ, DELETE | Delete user |

### 3.2 Batch Program → File Access

| Program | Files Read (Input) | Files Written (Output) |
|---------|-------------------|----------------------|
| **CBACT01C** | ACCTDATA.VSAM.KSDS | ACCTDATA.PSCOMP, ACCTDATA.ARRYPS, ACCTDATA.VBPS |
| **CBACT02C** | CARDDATA.VSAM.KSDS | (SYSOUT print) |
| **CBACT03C** | CARDXREF.VSAM.KSDS | (SYSOUT print) |
| **CBACT04C** | TCATBALF, XREFFILE, ACCTDATA, DISCGRP | TRANSACT (interest transactions) |
| **CBCUS01C** | CUSTDATA.VSAM.KSDS | (SYSOUT print) |
| **CBTRN01C** | TRANSACT.VSAM.KSDS | (SYSOUT print) |
| **CBTRN02C** | DALYTRAN, XREFFILE, ACCTDATA, TCATBALF | TRANSACT, DALYREJS (rejections) |
| **CBTRN03C** | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) |
| **CBSTM03A** | TRXFL.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA | STMTFILE (text), HTMLFILE (HTML) |
| **CBSTM03B** | (called by CBSTM03A) | (I/O on behalf of CBSTM03A) |
| **CBEXPORT** | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPORT-OUTPUT (single sequential) |
| **CBIMPORT** | EXPORT-INPUT (sequential) | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA, ERROR-OUTPUT |

---

## 4. JCL Job → Program → File Data Lineage

### 4.1 Nightly Batch Cycle

The standard batch cycle runs in this order, with data flowing between steps:

```
Step 1: CLOSEFIL.jcl
  └─ Program: SDSF
  └─ Action: Close CICS files to allow batch exclusive access

Step 2: Data Refresh (parallel)
  ├─ ACCTFILE.jcl   → IDCAMS REPRO → ACCTDATA.VSAM.KSDS
  ├─ CARDFILE.jcl   → IDCAMS REPRO → CARDDATA.VSAM.KSDS
  ├─ CUSTFILE.jcl   → IDCAMS REPRO → CUSTDATA.VSAM.KSDS
  ├─ XREFFILE.jcl   → IDCAMS REPRO → CARDXREF.VSAM.KSDS
  └─ DUSRSECJ.jcl   → IEBGENER + IDCAMS → USRSEC.VSAM.KSDS

Step 3: POSTTRAN.jcl
  └─ Program: CBTRN02C
  └─ Reads:  DALYTRAN.VSAM.KSDS, CARDXREF, ACCTDATA, TCATBALF
  └─ Writes: TRANSACT.VSAM.KSDS (posted transactions)
  └─ Writes: DALYREJS (rejected transactions)

Step 4: INTCALC.jcl
  └─ Program: CBACT04C
  └─ Reads:  TCATBALF, CARDXREF, ACCTDATA, DISCGRP
  └─ Writes: TRANSACT.VSAM.KSDS (interest transactions)

Step 5: TRANBKP.jcl
  └─ Utility: REPROC + IDCAMS
  └─ Reads:  TRANSACT.VSAM.KSDS
  └─ Writes: TRANSACT.BKUP.GDG(+1) (backup to GDG)

Step 6: COMBTRAN.jcl
  └─ Utility: SORT/IDCAMS
  └─ Action: Combine/merge transaction files

Step 7: CREASTMT.JCL
  └─ Programs: SORT → IDCAMS → CBSTM03A
  └─ Reads:  TRANSACT.VSAM.KSDS → TRXFL.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA
  └─ Writes: STMTFILE (text statements), HTMLFILE (HTML statements)

Step 8: TRANIDX.jcl
  └─ Utility: IDCAMS
  └─ Action: Rebuild alternate indexes on TRANSACT

Step 9: OPENFIL.jcl
  └─ Program: SDSF
  └─ Action: Reopen CICS files for online access
```

### 4.2 Report Generation Flow

```
TRANREPT.jcl
  ├─ Step 1: REPROC procedure
  │    └─ IDCAMS REPRO: TRANSACT.VSAM.KSDS → sequential copy
  ├─ Step 2: SORT
  │    └─ Sort transactions by account + date
  └─ Step 3: CBTRN03C
       └─ Reads: sorted TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
       └─ Writes: TRANREPT (formatted report file)

TXT2PDF1.JCL  (optional follow-on)
  └─ Program: IKJEFT1B (REXX TXT2PDF)
  └─ Reads: STMTFILE (text statement)
  └─ Writes: PDF output
```

### 4.3 Data Export/Import Flow

```
CBEXPORT.jcl
  └─ Program: CBEXPORT
  └─ Reads:  CUSTDATA + ACCTDATA + CARDXREF + TRANSACT + CARDDATA
  └─ Writes: Single tagged sequential export file
       (each record prefixed with CUST/ACCT/CARD/XREF/TRAN)

CBIMPORT.jcl
  └─ Program: CBIMPORT
  └─ Reads:  Export sequential file
  └─ Writes: CUSTDATA + ACCTDATA + CARDXREF + TRANSACT + CARDDATA + ERROR-OUTPUT
```

---

## 5. VSAM Dataset Cross-Reference

Which JCL jobs read or write each VSAM dataset:

| VSAM Dataset | Initialized By | Read By (Batch) | Written By (Batch) | Read By (Online) |
|-------------|---------------|-----------------|-------------------|-----------------|
| ACCTDATA.VSAM.KSDS | ACCTFILE | CBACT01C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT | CBIMPORT | COACTVWC, COACTUPC, COTRN02C, COBIL00C |
| CARDDATA.VSAM.KSDS | CARDFILE | CBACT02C, CBEXPORT | CBIMPORT | COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDATA.VSAM.KSDS | CUSTFILE | CBCUS01C, CBSTM03A, CBEXPORT | CBIMPORT | COCRDSLC, COCRDUPC, COACTUPC |
| CARDXREF.VSAM.KSDS | XREFFILE | CBACT03C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | CBIMPORT | COACTVWC, COTRN02C, COBIL00C |
| TRANSACT.VSAM.KSDS | TRANFILE | CBTRN01C, CBTRN03C, CBSTM03A, CBEXPORT | CBTRN02C, CBACT04C, CBIMPORT | COTRN00C, COTRN01C, COTRN02C, COBIL00C |
| DALYTRAN.VSAM.KSDS | (online adds) | CBTRN02C | (cleared after posting) | — |
| USRSEC.VSAM.KSDS | DUSRSECJ | — | — | COSGN00C, COUSR00C–03C |
| TCATBALF.VSAM.KSDS | TCATBALF | CBACT04C, CBTRN02C | CBTRN02C | — |
| TRANTYPE.VSAM.KSDS | TRANTYPE | CBTRN03C | — | — |
| TRANCATG.VSAM.KSDS | TRANCATG | CBTRN03C | — | — |
| DISCGRP.VSAM.KSDS | DISCGRP | CBACT04C | — | — |

---

## 6. Technology Integration Points

| Integration | Programs | Direction | Notes |
|------------|----------|-----------|-------|
| **VSAM KSDS** | All core programs | Read/Write | Primary data store |
| **CICS BMS** | 17 online programs | Send/Receive | 3270 terminal I/O |
| **CICS COMMAREA** | All online programs | Bidirectional | Inter-program state transfer |
| **CICS TDQ** | CORPT00C | Write | Submit JCL via transient data queue |
| **MQ Series** | COPAUA0C, COACCT01, CODATE01 | Get/Put | Async messaging (optional modules) |
| **IMS DL/I** | COPAUS0C/1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | GN/GNP/GU/ISRT/DLET/REPL | Hierarchical DB (optional module) |
| **DB2 SQL** | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT | SELECT/INSERT/UPDATE/DELETE | Relational DB (optional modules) |
| **LE Runtime** | CSUTLDTC, CBSTM03A, CBIMPORT | CALL CEEDAYS/CEE3ABD | Language Environment services |
| **Assembler** | COBSWAIT | CALL MVSWAIT | Low-level wait |
| **SORT** | CREASTMT, TRANREPT, PRTCATBL | EXEC PGM=SORT | File sorting in JCL |
| **IDCAMS** | 20+ JCL jobs | EXEC PGM=IDCAMS | VSAM utility (DEFINE, DELETE, REPRO, AIX) |
