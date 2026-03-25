# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Call graph, copybook dependencies, CICS navigation flow, JCL job chains, and data lineage.

---

## Table of Contents

1. [Online CICS Call Graph](#1-online-cics-call-graph)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [Copybook Dependency Matrix](#3-copybook-dependency-matrix)
4. [CICS Navigation Flow](#4-cics-navigation-flow)
5. [JCL Job → Program Mapping](#5-jcl-job--program-mapping)
6. [Batch Processing Cycle (Job Chain)](#6-batch-processing-cycle-job-chain)
7. [VSAM File Data Lineage](#7-vsam-file-data-lineage)
8. [Program → VSAM File Access Matrix](#8-program--vsam-file-access-matrix)
9. [BMS Map → Program → Copybook Triad](#9-bms-map--program--copybook-triad)

---

## 1. Online CICS Call Graph

Online CICS programs communicate via `EXEC CICS XCTL` (transfer control) using the COMMAREA (`COCOM01Y`). The `CDEMO-TO-PROGRAM` field in COMMAREA determines the target.

```
                          ┌─────────────┐
                          │  COSGN00C   │  Sign-on (CC00)
                          │  Login      │
                          └──────┬──────┘
                                 │ XCTL (based on user type)
                    ┌────────────┴────────────┐
                    ▼                          ▼
            ┌─────────────┐           ┌─────────────┐
            │  COMEN01C   │           │  COADM01C   │
            │  Main Menu  │           │  Admin Menu  │
            │  (CM00)     │           │  (CA00)      │
            └──────┬──────┘           └──────┬──────┘
                   │                          │
    ┌──────┬──────┬┴──────┬──────┐    ┌──────┴──────┐
    ▼      ▼      ▼       ▼      ▼    ▼             ▼
 COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C    COUSR00C
 Acct View Card List Txn List Reports  Bill Pay   User List
    │      │       │                                 │
    ▼      ▼       ├──→ COTRN01C (Txn View)    ┌────┼────┐
 COACTUPC COCRDSLC └──→ COTRN02C (Txn Add)     ▼    ▼    ▼
 Acct Upd  Card View                         COUSR01C 02C 03C
              │                              Add  Update Delete
              ▼
           COCRDUPC
           Card Update
```

### XCTL Transfer Details

| Source Program | Target Program(s)                                    | Mechanism                                     |
|---------------|------------------------------------------------------|-----------------------------------------------|
| COSGN00C      | COMEN01C (Regular), COADM01C (Admin)                 | `EXEC CICS XCTL` based on `SEC-USR-TYPE`     |
| COMEN01C      | COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C    | `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME(n))`    |
| COMEN01C      | Any program via CDEMO-TO-PROGRAM                     | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` (return)     |
| COADM01C      | COUSR00C (+ others via admin options)                | `XCTL PROGRAM(CDEMO-ADMIN-OPT-PGMNAME(n))`   |
| COADM01C      | Any program via CDEMO-TO-PROGRAM                     | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` (return)     |
| COTRN00C      | COTRN01C, COTRN02C, COMEN01C                        | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COUSR00C      | COUSR01C, COUSR02C, COUSR03C, COADM01C              | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COCRDLIC      | COCRDSLC, COCRDUPC, COMEN01C                        | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COCRDSLC      | COCRDUPC, COCRDLIC, COMEN01C                        | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COCRDUPC      | COCRDLIC, COMEN01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COACTVWC      | COACTUPC, COMEN01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COBIL00C      | COMEN01C                                             | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COTRN01C      | COTRN00C, COMEN01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COTRN02C      | COTRN00C, COMEN01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| CORPT00C      | COMEN01C                                             | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COUSR01C      | COUSR00C, COADM01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COUSR02C      | COUSR00C, COADM01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |
| COUSR03C      | COUSR00C, COADM01C                                   | `XCTL PROGRAM(CDEMO-TO-PROGRAM)`              |

### CALL Statements (Subroutine Calls)

| Caller         | Called Program | Purpose                                |
|---------------|----------------|----------------------------------------|
| CORPT00C      | CSUTLDTC       | Date validation/conversion             |
| COTRN02C      | CSUTLDTC       | Date validation for new transactions   |
| CBSTM03A      | CBSTM03B       | Statement file I/O (called 13 times)   |
| CBSTM03A      | CEE3ABD        | LE abnormal termination (error path)   |
| CSUTLDTC      | CEEDAYS        | LE date conversion service             |
| COACTUPC      | *(inline)*     | Uses CSSETATY copybook 39× via REPLACING |

---

## 2. Batch Program Call Graph

Batch programs are invoked by JCL jobs and typically operate independently, reading/writing VSAM files.

```
JCL Job          Program         Subroutine Calls
─────────        ──────────      ────────────────
POSTTRAN    ──→  CBTRN02C        (standalone — reads DALYTRAN, writes TRANSACT)
INTCALC     ──→  CBACT04C        (standalone — reads TCATBAL, DISCGRP, updates ACCTDATA)
CREASTMT    ──→  CBSTM03A   ──→  CBSTM03B  (file I/O subroutine, called 13×)
TRANREPT    ──→  CBTRN03C        (standalone — reads TRANSACT, writes report)
READACCT    ──→  CBACT01C        (standalone — reads ACCTDATA)
READCARD    ──→  CBACT02C        (standalone — reads CARDDATA)
READCUST    ──→  CBCUS01C        (standalone — reads CUSTDATA)
READXREF    ──→  CBACT03C        (standalone — reads CARDXREF)
CBEXPORT    ──→  CBEXPORT        (standalone — reads all VSAM, writes sequential)
CBIMPORT    ──→  CBIMPORT        (standalone — reads sequential, writes VSAM)
WAITSTEP    ──→  COBSWAIT        (standalone — MVS wait utility)
```

---

## 3. Copybook Dependency Matrix

Shows which copybooks are included (via `COPY`) by each program. ● = direct COPY.

### Online CICS Programs

| Copybook       | COSGN | COMEN | COADM | COACTVW | COACTUPC | COCRDLI | COCRDSL | COCRDUP | COTRN00 | COTRN01 | COTRN02 | CORPT00 | COBIL00 | COUSR00 | COUSR01 | COUSR02 | COUSR03 |
|----------------|-------|-------|-------|---------|----------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------|
| **COCOM01Y**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **COTTL01Y**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **CSDAT01Y**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **CSMSG01Y**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **DFHAID**     | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **DFHBMSCA**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |
| **CSUSR01Y**   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       |         |         |         |         |         | ●       | ●       | ●       | ●       |
| **CSMSG02Y**   |       |       |       | ●       | ●        |         | ●       | ●       |         |         |         |         |         |         |         |         |         |
| **CVCRD01Y**   |       |       |       | ●       | ●        | ●       | ●       | ●       |         |         |         |         |         |         |         |         |         |
| **CVACT01Y**   |       |       |       | ●       | ●        |         |         |         |         |         | ●       |         | ●       |         |         |         |         |
| **CVACT02Y**   |       |       |       | ●       |          | ●       | ●       | ●       |         |         |         |         |         |         |         |         |         |
| **CVACT03Y**   |       |       |       | ●       | ●        |         |         |         |         |         | ●       |         | ●       |         |         |         |         |
| **CVCUS01Y**   |       |       |       | ●       | ●        |         | ●       | ●       |         |         |         |         |         |         |         |         |         |
| **CVTRA05Y**   |       |       |       |         |          |         |         |         | ●       | ●       | ●       | ●       | ●       |         |         |         |         |
| **COMEN02Y**   |       | ●     |       |         |          |         |         |         |         |         |         |         |         |         |         |         |         |
| **COADM02Y**   |       |       | ●     |         |          |         |         |         |         |         |         |         |         |         |         |         |         |
| **CSLKPCDY**   |       |       |       |         | ●        |         |         |         |         |         |         |         |         |         |         |         |         |
| **CSSETATY**   |       |       |       |         | ●(×39)   |         |         |         |         |         |         |         |         |         |         |         |         |
| **CSSTRPFY**   |       |       |       | ●       | ●        | ●       | ●       | ●       |         |         |         |         |         |         |         |         |         |
| **CSUTLDPY**   |       |       |       |         | ●        |         |         |         |         |         |         |         |         |         |         |         |         |
| **CSUTLDWY**   |       |       |       |         | ●        |         |         |         |         |         |         |         |         |         |         |         |         |
| BMS Copybook   | ●     | ●     | ●     | ●       | ●        | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       | ●       |

### Batch Programs

| Copybook       | CBACT01 | CBACT02 | CBACT03 | CBACT04 | CBCUS01 | CBTRN01 | CBTRN02 | CBTRN03 | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|----------------|---------|---------|---------|---------|---------|---------|---------|---------|----------|----------|----------|----------|
| **CVACT01Y**   | ●       |         |         | ●       |         |         | ●       |         | ●        |          | ●        | ●        |
| **CVACT02Y**   |         | ●       |         |         |         | ●       |         |         |          |          | ●        | ●        |
| **CVACT03Y**   |         |         | ●       | ●       |         | ●       | ●       | ●       | ●        |          | ●        | ●        |
| **CVCUS01Y**   |         |         |         |         | ●       | ●       |         |         |          |          | ●        | ●        |
| **CVTRA05Y**   |         |         |         | ●       |         | ●       | ●       | ●       |          |          | ●        | ●        |
| **CVTRA06Y**   |         |         |         |         |         | ●       | ●       |         |          |          |          |          |
| **CVTRA01Y**   |         |         |         | ●       |         |         | ●       |         |          |          |          |          |
| **CVTRA02Y**   |         |         |         | ●       |         |         |         |         |          |          |          |          |
| **CVTRA03Y**   |         |         |         |         |         |         |         | ●       |          |          |          |          |
| **CVTRA04Y**   |         |         |         |         |         |         |         | ●       |          |          |          |          |
| **CVTRA07Y**   |         |         |         |         |         |         |         | ●       |          |          |          |          |
| **COSTM01**    |         |         |         |         |         |         |         |         | ●        |          |          |          |
| **CUSTREC**    |         |         |         |         |         |         |         |         | ●        |          |          |          |
| **CVEXPORT**   |         |         |         |         |         |         |         |         |          |          | ●        | ●        |
| **CODATECN**   | ●       |         |         |         |         |         |         |         |          |          |          |          |

---

## 4. CICS Navigation Flow

Transaction IDs and program routing:

```
User types "CC00" at 3270 terminal
    │
    ▼
COSGN00C authenticates via USRSEC VSAM
    │
    ├─ Regular User → COMEN01C (CM00)
    │    Options:
    │    1. Account View    → COACTVWC → COACTUPC (update)
    │    2. Card List       → COCRDLIC → COCRDSLC (view) → COCRDUPC (update)
    │    3. Transaction List→ COTRN00C → COTRN01C (view) / COTRN02C (add)
    │    4. Bill Payment    → COBIL00C
    │    5. Reports         → CORPT00C (submits CBTRN03C via TDQ)
    │
    └─ Admin User → COADM01C (CA00)
         Options:
         1. User List      → COUSR00C → COUSR01C (add) / COUSR02C (update) / COUSR03C (delete)
```

---

## 5. JCL Job → Program Mapping

| JCL Job      | Primary COBOL Program | System Utilities Used              |
|-------------|----------------------|------------------------------------|
| POSTTRAN    | **CBTRN02C**         | —                                  |
| INTCALC     | **CBACT04C**         | —                                  |
| CREASTMT    | **CBSTM03A**         | IDCAMS, SORT                       |
| TRANREPT    | **CBTRN03C**         | SORT                               |
| READACCT    | **CBACT01C**         | IEFBR14                            |
| READCARD    | **CBACT02C**         | —                                  |
| READCUST    | **CBCUS01C**         | —                                  |
| READXREF    | **CBACT03C**         | —                                  |
| CBEXPORT    | **CBEXPORT**         | IDCAMS                             |
| CBIMPORT    | **CBIMPORT**         | —                                  |
| WAITSTEP    | **COBSWAIT**         | —                                  |
| ACCTFILE    | —                    | IDCAMS (DEFINE/REPRO)              |
| CARDFILE    | —                    | IDCAMS, SDSF                       |
| CUSTFILE    | —                    | IDCAMS, SDSF                       |
| TRANFILE    | —                    | IDCAMS, SDSF                       |
| XREFFILE    | —                    | IDCAMS                             |
| DUSRSECJ    | —                    | IEBGENER, IDCAMS                   |
| CLOSEFIL    | —                    | SDSF                               |
| OPENFIL     | —                    | SDSF                               |
| COMBTRAN    | —                    | SORT, IDCAMS                       |
| TRANBKP     | —                    | IDCAMS (REPRO)                     |
| TRANIDX     | —                    | IDCAMS (AIX/PATH/BLDINDEX)         |
| CBADMCDJ    | —                    | DFHCSDUP                           |
| TXT2PDF1    | —                    | IKJEFT1B (TSO)                     |
| FTPJCL      | —                    | FTP                                |
| INTRDRJ1    | —                    | IDCAMS, IEBGENER                   |
| INTRDRJ2    | —                    | IDCAMS                             |
| DEFGDGB     | —                    | IDCAMS                             |
| DEFGDGD     | —                    | IDCAMS, IEBGENER                   |
| DISCGRP     | —                    | IDCAMS                             |
| TRANCATG    | —                    | IDCAMS                             |
| TRANTYPE    | —                    | IDCAMS                             |
| TCATBALF    | —                    | IDCAMS                             |
| DALYREJS    | —                    | IDCAMS                             |
| ESDSRRDS    | —                    | IEBGENER, IDCAMS                   |
| DEFCUST     | —                    | IDCAMS                             |
| PRTCATBL    | —                    | SORT, IEFBR14                      |
| REPTFILE    | —                    | IDCAMS                             |

---

## 6. Batch Processing Cycle (Job Chain)

The nightly batch cycle runs in strict order:

```
Step 1: CLOSEFIL ─────────────────────────────────────────────────────┐
        Close CICS files (SDSF)                                      │
                                                                     │
Step 2: Data Refresh Jobs (can run in parallel)                      │
        ├── ACCTFILE  → Refresh Account VSAM                         │
        ├── CARDFILE  → Refresh Card VSAM                            │
        ├── CUSTFILE  → Refresh Customer VSAM                        │
        ├── TRANFILE  → Refresh Transaction VSAM                     │
        ├── XREFFILE  → Refresh Cross-Ref VSAM                       │
        └── DUSRSECJ  → Refresh User Security VSAM                  │
                                                                     │
Step 3: POSTTRAN ──→ CBTRN02C ──────────────────────────────────────│
        Post daily transactions to master                            │
        Reads:  DALYTRAN, CARDXREF, ACCTDATA, TRANSACT, TCATBAL    │
        Writes: TRANSACT, TCATBAL, ACCTDATA                         │
                                                                     │
Step 4: INTCALC ──→ CBACT04C ───────────────────────────────────────│
        Calculate interest charges                                   │
        Reads:  TCATBAL, DISCGRP, ACCTDATA, TRANSACT               │
        Writes: ACCTDATA, TRANSACT, TCATBAL                         │
                                                                     │
Step 5: TRANBKP ────────────────────────────────────────────────────│
        Backup transaction file (IDCAMS REPRO)                       │
                                                                     │
Step 6: COMBTRAN ───────────────────────────────────────────────────│
        Combine/merge daily into master (SORT + IDCAMS)              │
                                                                     │
Step 7: CREASTMT ──→ CBSTM03A ──→ CBSTM03B ────────────────────────│
        Generate account statements (text + HTML)                    │
        Reads:  TRXFL (sorted), CARDXREF, ACCTDATA, CUSTDATA       │
        Writes: STATEMNT.PS, STATEMNT.HTML                          │
                                                                     │
Step 8: TRANIDX ────────────────────────────────────────────────────│
        Rebuild transaction alternate indexes                        │
                                                                     │
Step 9: OPENFIL ────────────────────────────────────────────────────┘
        Reopen CICS files
```

---

## 7. VSAM File Data Lineage

Shows which programs and jobs read from (R) or write to (W) each dataset.

### Core VSAM Datasets

| VSAM Dataset (short name)      | Full DSN                                      | Defined By | Written By                                    | Read By                                                     |
|-------------------------------|-----------------------------------------------|------------|-----------------------------------------------|-------------------------------------------------------------|
| **ACCTDATA** (Account Master)  | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`         | ACCTFILE   | ACCTFILE, CBTRN02C, CBACT04C, COACTUPC, CBIMPORT | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBSTM03A, CBTRN02C, CBEXPORT, COBIL00C, COTRN02C |
| **CARDDATA** (Card Master)     | implied from CARDFILE                          | CARDFILE   | CARDFILE, COCRDUPC, CBIMPORT                   | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT            |
| **CARDXREF** (Cross-Reference) | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`         | XREFFILE   | XREFFILE, CBIMPORT                             | COACTVWC, COACTUPC, CBACT03C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, COTRN02C |
| **CUSTDATA** (Customer Master) | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`         | CUSTFILE   | CUSTFILE, COACTUPC, CBIMPORT                   | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT |
| **TRANSACT** (Transactions)    | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`         | TRANFILE   | TRANFILE, CBTRN02C, CBACT04C, COTRN02C, CBIMPORT | COTRN00C, COTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CORPT00C |
| **DALYTRAN** (Daily Trans)     | implied from TRANFILE                          | TRANFILE   | External feed                                  | CBTRN01C (validate), CBTRN02C (post)                        |
| **USRSEC** (User Security)     | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`           | DUSRSECJ   | DUSRSECJ, COUSR01C, COUSR02C                   | COSGN00C, COUSR00C, COUSR02C, COUSR03C                      |

### Reference Data VSAM Datasets

| VSAM Dataset (short name)      | Full DSN                                      | Defined By | Written By   | Read By                     |
|-------------------------------|-----------------------------------------------|------------|-------------|-----------------------------|
| **TCATBAL** (Category Balance) | implied from TCATBALF                          | TCATBALF   | CBTRN02C, CBACT04C | CBTRN02C, CBACT04C       |
| **DISCGRP** (Disclosure Group) | `AWS.M2.CARDDEMO.DISCGRP.PS`                 | DISCGRP    | DISCGRP job  | CBACT04C                   |
| **TRANTYPE** (Trans Types)     | `AWS.M2.CARDDEMO.TRANTYPE.PS`                | TRANTYPE   | TRANTYPE job | CBTRN03C                   |
| **TRANCATG** (Trans Categories)| `AWS.M2.CARDDEMO.TRANCATG.PS`                | TRANCATG   | TRANCATG job | CBTRN03C                   |

### Output Datasets

| Dataset                         | Full DSN                                      | Created By           | Purpose                   |
|---------------------------------|-----------------------------------------------|----------------------|---------------------------|
| **STATEMNT.PS** (Text)          | `AWS.M2.CARDDEMO.STATEMNT.PS`                | CREASTMT → CBSTM03A | Text account statements   |
| **STATEMNT.HTML**               | `AWS.M2.CARDDEMO.STATEMNT.HTML`              | CREASTMT → CBSTM03A | HTML account statements   |
| **TRXFL** (Sorted Trans)        | `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS`           | CREASTMT (SORT step) | Temp sorted transactions  |

---

## 8. Program → VSAM File Access Matrix

| Program      | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | DALYTRAN | USRSEC | TCATBAL | DISCGRP | TRANTYPE | TRANCATG |
|-------------|----------|----------|----------|----------|----------|----------|--------|---------|---------|----------|----------|
| **COSGN00C** |          |          |          |          |          |          | R      |         |         |          |          |
| **COACTVWC** | R        | R        | R        | R        |          |          |        |         |         |          |          |
| **COACTUPC** | R/W      | R        | R        | R/W      |          |          |        |         |         |          |          |
| **COCRDLIC** |          | R        |          |          |          |          |        |         |         |          |          |
| **COCRDSLC** |          | R        |          | R        |          |          |        |         |         |          |          |
| **COCRDUPC** |          | R/W      |          | R        |          |          |        |         |         |          |          |
| **COTRN00C** |          |          |          |          | R        |          |        |         |         |          |          |
| **COTRN01C** |          |          |          |          | R        |          |        |         |         |          |          |
| **COTRN02C** | R        |          | R        |          | R/W      |          |        |         |         |          |          |
| **COBIL00C** | R        |          | R        |          | R/W      |          |        |         |         |          |          |
| **CORPT00C** |          |          |          |          | R        |          |        |         |         |          |          |
| **COUSR00C** |          |          |          |          |          |          | R      |         |         |          |          |
| **COUSR01C** |          |          |          |          |          |          | W      |         |         |          |          |
| **COUSR02C** |          |          |          |          |          |          | R/W    |         |         |          |          |
| **COUSR03C** |          |          |          |          |          |          | R/W    |         |         |          |          |
| **CBTRN02C** | R/W      |          | R        |          | R/W      | R        |        | R/W     |         |          |          |
| **CBACT04C** | R/W      |          |          |          | R/W      |          |        | R/W     | R       |          |          |
| **CBTRN03C** |          |          | R        |          | R        |          |        |         |         | R        | R        |
| **CBSTM03A** | R        |          | R        | R        |          |          |        |         |         |          |          |
| **CBEXPORT** | R        | R        | R        | R        | R        |          |        |         |         |          |          |
| **CBIMPORT** | W        | W        | W        | W        | W        |          |        |         |         |          |          |
| **CBACT01C** | R        |          |          |          |          |          |        |         |         |          |          |
| **CBACT02C** |          | R        |          |          |          |          |        |         |         |          |          |
| **CBACT03C** |          |          | R        |          |          |          |        |         |         |          |          |
| **CBCUS01C** |          |          |          | R        |          |          |        |         |         |          |          |

**Legend:** R = Read, W = Write, R/W = Read and Write

---

## 9. BMS Map → Program → Copybook Triad

Each screen is defined by a BMS map, used by a COBOL program, and backed by BMS-generated and data copybooks.

| BMS Map       | COBOL Program | BMS Copybook   | Data Copybooks Used                                        |
|--------------|---------------|----------------|-------------------------------------------------------------|
| COSGN00.bms  | COSGN00C      | COSGN00.CPY    | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COMEN01.bms  | COMEN01C      | COMEN01.CPY    | COCOM01Y, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COADM01.bms  | COADM01C      | COADM01.CPY    | COCOM01Y, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COACTVW.bms  | COACTVWC      | COACTVW.CPY    | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| COACTUP.bms  | COACTUPC      | COACTUP.CPY    | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CSLKPCDY |
| COCRDLI.bms  | COCRDLIC      | COCRDLI.CPY    | COCOM01Y, CVCRD01Y, CVACT02Y, CSUSR01Y                     |
| COCRDSL.bms  | COCRDSLC      | COCRDSL.CPY    | COCOM01Y, CVCRD01Y, CVACT02Y, CVCUS01Y                     |
| COCRDUP.bms  | COCRDUPC      | COCRDUP.CPY    | COCOM01Y, CVCRD01Y, CVACT02Y, CVCUS01Y                     |
| COTRN00.bms  | COTRN00C      | COTRN00.CPY    | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COTRN01.bms  | COTRN01C      | COTRN01.CPY    | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COTRN02.bms  | COTRN02C      | COTRN02.CPY    | COCOM01Y, CVTRA05Y, CVACT01Y, CVACT03Y                     |
| CORPT00.bms  | CORPT00C      | CORPT00.CPY    | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COBIL00.bms  | COBIL00C      | COBIL00.CPY    | COCOM01Y, CVACT01Y, CVACT03Y, CVTRA05Y                     |
| COUSR00.bms  | COUSR00C      | COUSR00.CPY    | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COUSR01.bms  | COUSR01C      | COUSR01.CPY    | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COUSR02.bms  | COUSR02C      | COUSR02.CPY    | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
| COUSR03.bms  | COUSR03C      | COUSR03.CPY    | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y           |
