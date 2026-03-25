# Dependency Map - CardDemo

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Scope:** Program call graph, copybook inclusion graph, VSAM data lineage, JCL job-to-program mapping

---

## 1. Program Call Graph

### 1.1 Online CICS Navigation Flow

The online system uses CICS `XCTL` (transfer control) to navigate between programs. All programs share state via the COMMAREA (`COCOM01Y`).

```
                         ┌─────────────┐
                         │  COSGN00C   │  Sign-on (CC00)
                         │  (Entry)    │
                         └──────┬──────┘
                                │ XCTL (based on user type)
                    ┌───────────┴───────────┐
                    │                       │
             ┌──────┴──────┐         ┌──────┴──────┐
             │  COMEN01C   │         │  COADM01C   │
             │  Main Menu  │         │  Admin Menu │
             │  (CM00)     │         │  (CA00)     │
             └──────┬──────┘         └──────┬──────┘
                    │                       │
    ┌───────┬───────┼───────┬───────┐       ├── COUSR00C (User List)
    │       │       │       │       │       │     ├── COUSR01C (Add)
    │       │       │       │       │       │     ├── COUSR02C (Update)
    │       │       │       │       │       │     └── COUSR03C (Delete)
    │       │       │       │       │       │
    │       │       │       │       │       ├── COTRTLIC (Tran Type List)*
    │       │       │       │       │       └── COTRTUPC (Tran Type Maint)*
    │       │       │       │       │
    ▼       ▼       ▼       ▼       ▼
 COACTVWC COACTUPC COCRDLIC COTRN00C CORPT00C
 Acct View Acct Upd Card List Tran List Reports
    │       │       │       │       │
    │       │       ├─XCTL─►│       │
    │       │       │COCRDSLC       │
    │       │       │Card View      │
    │       │       │       │       │
    │       │       ├─XCTL─►│       │
    │       │       │COCRDUPC       │
    │       │       │Card Upd │      │
    │       │       │       │       │
    │       │       │       ├─XCTL─►│
    │       │       │       │COTRN01C
    │       │       │       │Tran View
    │       │       │       │       │
    │       │       │       ├─XCTL─►│
    │       │       │       │COTRN02C
    │       │       │       │Tran Add
    │       │       │       │       │
    │       │       │       │       │
    ▼       ▼       ▼       ▼       ▼
 COBIL00C ──────────────────────────── Bill Payment
 COPAUS0C* ─────────────────────────── Auth Summary*
   └── COPAUS1C* (Auth Detail)
         └── COPAUS2C* (Fraud Mark)
```

*\* = Optional module programs*

### 1.2 Direct CALL Dependencies

Programs that use COBOL `CALL` (subroutine linkage) rather than CICS `XCTL`:

| Caller | Called Program | Purpose |
|--------|---------------|---------|
| **CBACT01C** | `COBDATFT` (ASM) | Date formatting for account records |
| **CBACT01C** | `CEE3ABD` | LE abend routine |
| **CBACT02C** | `CEE3ABD` | LE abend routine |
| **CBACT03C** | `CEE3ABD` | LE abend routine |
| **CBCUS01C** | `CEE3ABD` | LE abend routine |
| **CBIMPORT** | `CEE3ABD` | LE abend routine |
| **CBSTM03A** | `CBSTM03B` | File I/O operations for statement generation |
| **COBSWAIT** | `MVSWAIT` (ASM) | MVS wait/delay |
| **CORPT00C** | `CSUTLDTC` | Date validation for report parameters |
| **COTRN02C** | `CSUTLDTC` | Date validation for transaction dates |
| **CSUTLDTC** | `CEEDAYS` | LE date conversion (Lilian days) |

### 1.3 Call Graph Summary

```
CBSTM03A ──CALL──► CBSTM03B          (Statement generation pair)

CORPT00C ──CALL──► CSUTLDTC ──CALL──► CEEDAYS (LE)
COTRN02C ──CALL──► CSUTLDTC ──CALL──► CEEDAYS (LE)

CBACT01C ──CALL──► COBDATFT (ASM)
COBSWAIT ──CALL──► MVSWAIT  (ASM)

CBACT01C ──CALL──► CEE3ABD (LE)
CBACT02C ──CALL──► CEE3ABD (LE)
CBACT03C ──CALL──► CEE3ABD (LE)
CBCUS01C ──CALL──► CEE3ABD (LE)
CBIMPORT ──CALL──► CEE3ABD (LE)
```

---

## 2. Copybook Inclusion Map

### 2.1 Shared Copybook Usage Matrix

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **COCOM01Y** | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| **COTTL01Y** | x | x | x | x | x | x | x | x | x | x | x | x | -- | x | x | x | x |
| **CSDAT01Y** | x | x | x | x | x | x | x | x | x | x | x | x | -- | x | x | x | x |
| **CSMSG01Y** | x | x | x | x | x | x | x | x | x | x | x | x | -- | x | x | x | x |
| **CSUSR01Y** | x | x | x | x | x | x | x | x | -- | -- | -- | -- | -- | x | x | x | x |
| **DFHAID** | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| **DFHBMSCA** | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| **CSMSG02Y** | -- | -- | -- | x | x | -- | x | x | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **CSSTRPFY** | -- | -- | -- | x | x | x | x | x | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **CVACT01Y** | -- | -- | -- | x | x | -- | -- | -- | -- | -- | x | -- | x | -- | -- | -- | -- |
| **CVACT02Y** | -- | -- | -- | x | -- | x | x | x | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **CVACT03Y** | -- | -- | -- | x | x | -- | -- | -- | -- | -- | x | -- | x | -- | -- | -- | -- |
| **CVCUS01Y** | -- | -- | -- | x | x | -- | x | x | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **CVCRD01Y** | -- | -- | -- | x | x | x | x | x | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **CVTRA05Y** | -- | -- | -- | -- | -- | -- | -- | -- | x | x | x | x | x | -- | -- | -- | -- |

### 2.2 Batch Program Copybook Usage

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **CVACT01Y** | x | -- | -- | x | -- | x | x | -- | -- | x | x |
| **CVACT02Y** | -- | x | -- | -- | -- | x | -- | -- | -- | x | x |
| **CVACT03Y** | -- | -- | x | x | -- | x | x | x | x | x | x |
| **CVCUS01Y** | -- | -- | -- | -- | x | x | -- | -- | x | x | x |
| **CVTRA05Y** | -- | -- | -- | x | -- | x | x | x | -- | x | x |
| **CVTRA06Y** | -- | -- | -- | -- | -- | x | x | -- | -- | -- | -- |
| **CVTRA01Y** | -- | -- | -- | x | -- | -- | x | -- | -- | -- | -- |
| **CVTRA02Y** | -- | -- | -- | x | -- | -- | -- | -- | -- | -- | -- |
| **CVTRA03Y** | -- | -- | -- | -- | -- | -- | -- | x | -- | -- | -- |
| **CVTRA04Y** | -- | -- | -- | -- | -- | -- | -- | x | -- | -- | -- |
| **CVTRA07Y** | -- | -- | -- | -- | -- | -- | -- | x | -- | -- | -- |
| **CODATECN** | x | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- |
| **COSTM01** | -- | -- | -- | -- | -- | -- | -- | -- | x | -- | -- |
| **CUSTREC** | -- | -- | -- | -- | -- | -- | -- | -- | x | -- | -- |
| **CVEXPORT** | -- | -- | -- | -- | -- | -- | -- | -- | -- | x | x |

### 2.3 Most-Referenced Copybooks (by usage count)

| Rank | Copybook | # Programs | Role |
|------|----------|-----------|------|
| 1 | **COCOM01Y** | 17 | Communication area — used by ALL online programs |
| 2 | **DFHAID / DFHBMSCA** | 17 | CICS AID keys / BMS attributes (system copybooks) |
| 3 | **COTTL01Y** | 16 | Screen titles |
| 4 | **CSDAT01Y** | 16 | Date/time working storage |
| 5 | **CSMSG01Y** | 16 | Common messages |
| 6 | **CVACT03Y** | 11 | Card cross-reference — most-shared data record |
| 7 | **CVTRA05Y** | 11 | Transaction master — second most-shared |
| 8 | **CVACT01Y** | 8 | Account master |
| 9 | **CVCUS01Y** | 8 | Customer master |
| 10 | **CVACT02Y** | 7 | Card master |

---

## 3. Data Lineage (VSAM File I/O)

### 3.1 VSAM File Access by Program

| VSAM File | Read By | Write/Update By | Delete By |
|-----------|---------|----------------|-----------|
| **USRSEC** (User Security) | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE) | COUSR03C (DELETE) |
| **ACCTFILE** (Accounts) | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03B, CBEXPORT | COACTUPC (REWRITE), CBACT04C (REWRITE), CBTRN02C (REWRITE), CBIMPORT (WRITE) | -- |
| **CARDFILE** (Cards) | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT | COCRDUPC (REWRITE), CBIMPORT (WRITE) | -- |
| **CUSTFILE** (Customers) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03B, CBEXPORT | CBIMPORT (WRITE) | -- |
| **XREFFILE** (Cross-ref) | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT | CBIMPORT (WRITE) | -- |
| **TRANSACT** (Transactions) | COTRN00C, COTRN01C, COBIL00C, CBTRN01C, CBTRN03C, CBEXPORT | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE), CBIMPORT (WRITE) | -- |
| **DALYTRAN** (Daily Trans) | CBTRN01C, CBTRN02C | *(loaded via JCL REPRO)* | -- |
| **TCATBALF** (Cat Balance) | CBACT04C | CBTRN02C (WRITE/REWRITE) | -- |
| **DISCGRP** (Disclosure) | CBACT04C | *(loaded via JCL REPRO)* | -- |
| **TRANTYPE** (Tran Types) | CBTRN03C | *(loaded via JCL REPRO)* | -- |
| **TRANCATG** (Tran Categories) | CBTRN03C | *(loaded via JCL REPRO)* | -- |

### 3.2 Data Flow Diagram — Batch Processing Cycle

```
                     ┌──────────────┐
                     │  CLOSEFIL    │  Close CICS files
                     │  (JCL)       │
                     └──────┬───────┘
                            │
              ┌─────────────┼──────────────┐
              │             │              │
              ▼             ▼              ▼
        ┌──────────┐ ┌──────────┐  ┌──────────┐
        │ ACCTFILE │ │ CARDFILE │  │ CUSTFILE │  Data refresh
        │ XREFFILE │ │ TRANFILE │  │ DUSRSECJ │  (JCL REPRO)
        └────┬─────┘ └────┬─────┘  └────┬─────┘
             │             │              │
             └──────┬──────┘──────────────┘
                    │
                    ▼
        ┌───────────────────┐
        │   POSTTRAN (JCL)  │  Runs CBTRN02C
        │                   │
        │ Reads: DALYTRAN   │
        │ Reads: XREFFILE   │
        │ Updates: ACCTFILE  │  ← account balance adjusted
        │ Updates: TCATBALF  │  ← category balance adjusted
        │ Writes: TRANSACT   │  ← posted transactions
        │ Writes: DALYREJS   │  ← rejected transactions
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │   INTCALC (JCL)   │  Runs CBACT04C
        │                   │
        │ Reads: TCATBALF   │
        │ Reads: XREFFILE   │
        │ Reads: DISCGRP    │  ← interest rates
        │ Updates: ACCTFILE  │  ← interest charges applied
        │ Writes: TRANSACT   │  ← interest transactions
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │   TRANBKP (JCL)   │  Backup TRANSACT via IDCAMS REPRO
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │  COMBTRAN (JCL)   │  Merge/sort transactions
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │  CREASTMT (JCL)   │  Runs CBSTM03A → CBSTM03B
        │                   │
        │ Reads: TRANSACT   │  (via CBSTM03B)
        │ Reads: XREFFILE   │  (via CBSTM03B)
        │ Reads: CUSTFILE   │  (via CBSTM03B)
        │ Reads: ACCTFILE   │  (via CBSTM03B)
        │ Writes: STMT-FILE │  ← customer statements
        │ Writes: HTML-FILE │  ← HTML format statements
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │   TRANIDX (JCL)   │  Build alternate index
        └────────┬──────────┘
                 │
                 ▼
        ┌───────────────────┐
        │   OPENFIL (JCL)   │  Reopen CICS files
        └───────────────────┘
```

### 3.3 Data Flow Diagram — Online Transaction Entry

```
User (3270 Terminal)
  │
  ▼
COSGN00C ──READ──► USRSEC (authenticate)
  │
  ▼
COMEN01C (menu selection)
  │
  ▼
COTRN02C (Transaction Add)
  │
  ├──READ──► XREFFILE (validate card → account)
  ├──READ──► ACCTFILE (validate account status)
  ├──WRITE──► TRANSACT (new transaction)
  └──CALL──► CSUTLDTC (validate dates)
```

### 3.4 Data Flow Diagram — Bill Payment

```
COBIL00C (Bill Payment)
  │
  ├──READ──► XREFFILE (card → account lookup)
  ├──READ──► ACCTFILE (get current balance)
  ├──STARTBR/READPREV──► TRANSACT (get last transaction ID)
  ├──WRITE──► TRANSACT (payment transaction)
  └──REWRITE──► ACCTFILE (update balance)
```

---

## 4. JCL Job-to-Program Mapping

### 4.1 Jobs That Execute COBOL Programs

| JCL Job | COBOL Program Executed | Input Files | Output Files |
|---------|----------------------|------------|-------------|
| **POSTTRAN** | CBTRN02C | DALYTRAN, XREFFILE | TRANSACT, DALYREJS, ACCTFILE*, TCATBALF* |
| **INTCALC** | CBACT04C | TCATBALF, XREFFILE, DISCGRP, ACCTFILE | ACCTFILE*, TRANSACT |
| **CREASTMT** | CBSTM03A → CBSTM03B | TRANSACT, XREFFILE, CUSTFILE, ACCTFILE | STMT-FILE, HTML-FILE |
| **TRANREPT** | CBTRN03C | TRANSACT, XREFFILE, TRANTYPE, TRANCATG | REPORT-FILE |
| **READACCT** | CBACT01C | ACCTFILE | OUT-FILE, ARRY-FILE, VBRC-FILE |
| **READCARD** | CBACT02C | CARDFILE | *(display only)* |
| **READCUST** | CBCUS01C | CUSTFILE | *(display only)* |
| **READXREF** | CBACT03C | XREFFILE | *(display only)* |
| **CBEXPORT** | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPORT-FILE |
| **CBIMPORT** | CBIMPORT | EXPORT-FILE | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, ERROR-FILE |
| **WAITSTEP** | COBSWAIT → MVSWAIT | -- | -- |

*\* = Updated in place (I-O mode)*

### 4.2 Jobs That Use IDCAMS (Utility)

| JCL Job | IDCAMS Operation | Target VSAM File |
|---------|-----------------|-----------------|
| **ACCTFILE** | REPRO (load) | Account Master |
| **CARDFILE** | REPRO (load) | Card Master |
| **CUSTFILE** | REPRO (load) | Customer Master |
| **XREFFILE** | REPRO (load) | Cross-Reference |
| **TRANFILE** | REPRO (load) | Transaction Master |
| **DUSRSECJ** | REPRO (load) | User Security |
| **DISCGRP** | REPRO (load) | Disclosure Group |
| **TCATBALF** | REPRO (load) | Category Balance |
| **TRANCATG** | REPRO (load) | Transaction Category |
| **TRANTYPE** | REPRO (load) | Transaction Type |
| **TRANBKP** | REPRO (backup) | Transaction Master → GDG |
| **TRANIDX** | DEFINE AIX / BLDINDEX | Transaction Alternate Index |
| **DEFGDGB** | DEFINE GDG | GDG base for backups |
| **DEFGDGD** | DEFINE GDG | GDG base for daily |
| **DEFCUST** | DEFINE CLUSTER | Customer VSAM |
| **ESDSRRDS** | DEFINE CLUSTER | ESDS/RRDS clusters |
| **DALYREJS** | DEFINE CLUSTER | Daily Rejects |
| **CLOSEFIL** | CEMT SET FILE CLOSED | All CICS files |
| **OPENFIL** | CEMT SET FILE OPEN | All CICS files |

### 4.3 Batch Job Dependency Chain (Execution Order)

```
CLOSEFIL ──► ACCTFILE ──► CARDFILE ──► CUSTFILE ──► XREFFILE ──► TRANFILE
                                                                     │
                                                                     ▼
DUSRSECJ ──► DISCGRP ──► TCATBALF ──► TRANCATG ──► TRANTYPE ──► POSTTRAN
                                                                     │
                                                                     ▼
                                                                 INTCALC
                                                                     │
                                                                     ▼
                                                                 TRANBKP
                                                                     │
                                                                     ▼
                                                                 COMBTRAN
                                                                     │
                                                                     ▼
                                                                 CREASTMT
                                                                     │
                                                                     ▼
                                                                 TRANIDX
                                                                     │
                                                                     ▼
                                                                 OPENFIL
```

---

## 5. Cross-Cutting Dependency Summary

### 5.1 Most-Connected Programs (Hub Analysis)

| Rank | Program | Inbound Deps | Outbound Deps | Total Connections | Role |
|------|---------|-------------|--------------|------------------|------|
| 1 | **COACTUPC** | 1 (from menu) | 11 copybooks, 4 VSAM files | 16 | Heaviest online program |
| 2 | **CBTRN02C** | 1 (POSTTRAN JCL) | 6 copybooks, 6 VSAM files | 13 | Core batch posting |
| 3 | **CBACT04C** | 1 (INTCALC JCL) | 5 copybooks, 5 VSAM files | 11 | Interest calculation |
| 4 | **CBSTM03A** | 1 (CREASTMT JCL) | 3 copybooks, 4 VSAM files, 1 CALL | 9 | Statement generation |
| 5 | **CBEXPORT** | 1 (CBEXPORT JCL) | 6 copybooks, 6 VSAM files | 13 | Data export |
| 6 | **CBIMPORT** | 1 (CBIMPORT JCL) | 6 copybooks, 7 VSAM files | 14 | Data import |
| 7 | **CBTRN01C** | 1 (JCL) | 6 copybooks, 6 VSAM files | 13 | Transaction validation |
| 8 | **COACTVWC** | 1 (from menu) | 10 copybooks, 4 VSAM files | 15 | Account view |
| 9 | **COCRDLIC** | 1 (from menu) | 8 copybooks, 1 VSAM file | 10 | Card list |
| 10 | **CBTRN03C** | 1 (TRANREPT JCL) | 5 copybooks, 5 VSAM files | 11 | Transaction report |

### 5.2 Most-Accessed VSAM Files

| Rank | VSAM File | # Programs Accessing | Read | Write/Update | Critical Path |
|------|-----------|---------------------|------|-------------|--------------|
| 1 | **XREFFILE** | 13 | 13 | 1 | Transaction posting, interest calc, statements |
| 2 | **ACCTFILE** | 11 | 11 | 4 | Balance management, interest, statements |
| 3 | **TRANSACT** | 10 | 7 | 4 | All transaction operations |
| 4 | **CARDFILE** | 7 | 7 | 2 | Card management, validation |
| 5 | **CUSTFILE** | 8 | 8 | 1 | Customer info display, statements |
| 6 | **USRSEC** | 6 | 4 | 2 | Authentication, user admin |
| 7 | **TCATBALF** | 3 | 2 | 1 | Interest calculation |
| 8 | **DALYTRAN** | 2 | 2 | 0 | Daily transaction staging |
