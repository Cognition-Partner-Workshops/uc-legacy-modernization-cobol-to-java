# DEPENDENCY MAP — CardDemo COBOL Application

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Purpose:** Call graph, copybook inclusion graph, CICS screen flow, and JCL data lineage

---

## Table of Contents

1. [Program-to-Program Call Graph](#1-program-to-program-call-graph)
2. [CICS Screen Navigation Flow](#2-cics-screen-navigation-flow)
3. [Copybook Inclusion Matrix](#3-copybook-inclusion-matrix)
4. [VSAM File Access Map](#4-vsam-file-access-map)
5. [JCL Job → Program → Dataset Lineage](#5-jcl-job--program--dataset-lineage)
6. [Batch Cycle Data Flow](#6-batch-cycle-data-flow)
7. [Cross-Cutting Dependency Summary](#7-cross-cutting-dependency-summary)

---

## 1. Program-to-Program Call Graph

### 1.1 Direct CALL Dependencies

```
CBSTM03A ──CALL──► CBSTM03B        (Statement file I/O subroutine)
CBSTM03A ──CALL──► CEE3ABD         (LE abend routine)

CBACT01C ──CALL──► COBDATFT        (ASM: date formatting)
CBACT01C ──CALL──► CEE3ABD

CORPT00C ──CALL──► CSUTLDTC        (Date conversion utility)
COTRN02C ──CALL──► CSUTLDTC        (Date conversion utility)

CSUTLDTC ──CALL──► CEEDAYS         (LE: date-to-integer conversion)

COBSWAIT ──CALL──► MVSWAIT         (ASM: low-level wait)

CBACT02C ──CALL──► CEE3ABD
CBACT03C ──CALL──► CEE3ABD
CBACT04C ──CALL──► CEE3ABD
CBCUS01C ──CALL──► CEE3ABD
CBTRN01C ──CALL──► CEE3ABD
CBTRN02C ──CALL──► CEE3ABD
CBTRN03C ──CALL──► CEE3ABD
CBEXPORT ──CALL──► CEE3ABD
CBIMPORT ──CALL──► CEE3ABD
```

### 1.2 CICS XCTL (Transfer Control) Dependencies

```
COSGN00C ──XCTL──► COMEN01C        (Successful regular user login)
COSGN00C ──XCTL──► COADM01C        (Successful admin user login)

COMEN01C ──XCTL──► COACTVWC        (Option: Account View)
COMEN01C ──XCTL──► COCRDLIC        (Option: Card List)
COMEN01C ──XCTL──► COTRN00C        (Option: Transaction List)
COMEN01C ──XCTL──► COBIL00C        (Option: Bill Payment)
COMEN01C ──XCTL──► CORPT00C        (Option: Reports)
COMEN01C ──XCTL──► COSGN00C        (Sign Off)

COADM01C ──XCTL──► COUSR00C        (Option: User List)
COADM01C ──XCTL──► COSGN00C        (Sign Off)

COACTVWC ──XCTL──► COACTUPC        (Navigate to Account Update)
COACTVWC ──XCTL──► COMEN01C        (Return to Main Menu)

COACTUPC ──XCTL──► COACTVWC        (Return to Account View)
COACTUPC ──XCTL──► COMEN01C        (Return to Main Menu)

COCRDLIC ──XCTL──► COCRDSLC        (Select card from list)
COCRDLIC ──XCTL──► COMEN01C        (Return to Main Menu)

COCRDSLC ──XCTL──► COCRDUPC        (Navigate to Card Update)
COCRDSLC ──XCTL──► COCRDLIC        (Return to Card List)
COCRDSLC ──XCTL──► COMEN01C        (Return to Main Menu)

COCRDUPC ──XCTL──► COCRDSLC        (Return to Card View)
COCRDUPC ──XCTL──► COMEN01C        (Return to Main Menu)
```

### 1.3 Call Graph Summary

| Caller | Callees | Call Type |
|--------|---------|-----------|
| CBSTM03A | CBSTM03B, CEE3ABD | CALL |
| CBACT01C | COBDATFT, CEE3ABD | CALL |
| CORPT00C | CSUTLDTC | CALL |
| COTRN02C | CSUTLDTC | CALL |
| CSUTLDTC | CEEDAYS | CALL |
| COBSWAIT | MVSWAIT | CALL |
| All CB* batch | CEE3ABD | CALL (error) |
| COSGN00C | COMEN01C, COADM01C | XCTL |
| COMEN01C | 6 programs | XCTL |
| COADM01C | COUSR00C, COSGN00C | XCTL |
| COACTVWC | COACTUPC, COMEN01C | XCTL |
| COCRDLIC | COCRDSLC, COMEN01C | XCTL |
| COCRDSLC | COCRDUPC, COCRDLIC, COMEN01C | XCTL |
| COCRDUPC | COCRDSLC, COMEN01C | XCTL |
| COACTUPC | COACTVWC, COMEN01C | XCTL |

---

## 2. CICS Screen Navigation Flow

```
                              ┌──────────────┐
                              │   COSGN00C   │
                              │   Sign-On    │
                              │   (CC00)     │
                              └──────┬───────┘
                                     │
                      ┌──────────────┼──────────────┐
                      │ (Regular)    │              │ (Admin)
                      ▼              │              ▼
               ┌──────────────┐     │      ┌──────────────┐
               │  COMEN01C    │     │      │  COADM01C    │
               │  Main Menu   │     │      │  Admin Menu  │
               │  (CM00)      │     │      │  (CA00)      │
               └──────┬───────┘     │      └──────┬───────┘
                      │             │             │
          ┌───────────┼─────────┬───┼──────┐      │
          │           │         │   │      │      ▼
          ▼           ▼         ▼   │      ▼  ┌──────────────┐
   ┌───────────┐ ┌─────────┐ ┌─────┴──┐   │  │  COUSR00C    │
   │ COACTVWC  │ │COCRDLIC │ │COTRN00C│   │  │  User List   │
   │ Acct View │ │Card List│ │Txn List│   │  │  (CU00)      │
   │ (CA01)    │ │(CC01)   │ │(CT00)  │   │  └──────┬───────┘
   └─────┬─────┘ └────┬────┘ └───┬────┘   │         │
         │            │          │         │    ┌────┼────┐
         ▼            ▼          ▼         │    ▼    ▼    ▼
   ┌───────────┐ ┌─────────┐ ┌─────────┐  │ ┌─────┐┌────┐┌─────┐
   │ COACTUPC  │ │COCRDSLC │ │COTRN01C │  │ │USR01││USR02││USR03│
   │ Acct Upd  │ │Card View│ │Txn View │  │ │ Add ││ Upd ││ Del │
   │ (CA02)    │ │(CC02)   │ │(CT01)   │  │ │(CU01)│(CU02)│(CU03)│
   └───────────┘ └────┬────┘ └─────────┘  │ └─────┘└────┘└─────┘
                      │                    │
                      ▼                    ▼
                ┌─────────┐          ┌──────────┐
                │COCRDUPC │          │ COBIL00C │
                │Card Upd │          │Bill Pay  │
                │(CC03)   │          │(CB00)    │
                └─────────┘          └──────────┘

                                     ▼
                               ┌──────────┐
                               │ CORPT00C │
                               │ Reports  │
                               │ (CR00)   │
                               └──────────┘
```

### Screen-to-BMS-to-Program Mapping

| Screen Flow | BMS Map | Program | CICS Txn |
|---|---|---|---|
| Sign-On | COSGN00 | COSGN00C | CC00 |
| Main Menu | COMEN01 | COMEN01C | CM00 |
| Admin Menu | COADM01 | COADM01C | CA00 |
| Account View | COACTVW | COACTVWC | CA01 |
| Account Update | COACTUP | COACTUPC | CA02 |
| Card List | COCRDLI | COCRDLIC | CC01 |
| Card Detail | COCRDSL | COCRDSLC | CC02 |
| Card Update | COCRDUP | COCRDUPC | CC03 |
| Transaction List | COTRN00 | COTRN00C | CT00 |
| Transaction View | COTRN01 | COTRN01C | CT01 |
| Transaction Add | COTRN02 | COTRN02C | CT02 |
| Reports | CORPT00 | CORPT00C | CR00 |
| Bill Payment | COBIL00 | COBIL00C | CB00 |
| User List | COUSR00 | COUSR00C | CU00 |
| User Add | COUSR01 | COUSR01C | CU01 |
| User Update | COUSR02 | COUSR02C | CU02 |
| User Delete | COUSR03 | COUSR03C | CU03 |

---

## 3. Copybook Inclusion Matrix

### 3.1 Data Copybook Usage (rows = copybooks, columns = programs)

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT | COACTUPC | COACTVWC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | CORPT00C | COSGN00C | COMEN01C | COADM01C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **CVACT01Y** | ✓ | | | ✓ | | ✓ | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | | ✓ | ✓ | | | ✓ | ✓ | | | | | | | | |
| **CVACT02Y** | | ✓ | | | | ✓ | | | | ✓ | ✓ | | ✓ | ✓ | ✓ | ✓ | | | | | | | | | | | | |
| **CVACT03Y** | | | ✓ | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | | ✓ | ✓ | | | ✓ | ✓ | | | | | | | | |
| **CVCUS01Y** | | | | | ✓ | ✓ | | | | ✓ | ✓ | ✓ | ✓ | | ✓ | ✓ | | | | | | | | | | | | |
| **CVCRD01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | | | | | | | | | | | | |
| **CVTRA05Y** | | | | ✓ | | ✓ | ✓ | ✓ | | ✓ | ✓ | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | | | | | | | |
| **CVTRA06Y** | | | | | | ✓ | ✓ | | | | | | | | | | | | | | | | | | | | | |
| **CVTRA01Y** | | | | ✓ | | | ✓ | | | | | | | | | | | | | | | | | | | | | |
| **CVTRA02Y** | | | | ✓ | | | | | | | | | | | | | | | | | | | | | | | | |
| **CVTRA03Y** | | | | | | | | ✓ | | | | | | | | | | | | | | | | | | | | |
| **CVTRA04Y** | | | | | | | | ✓ | | | | | | | | | | | | | | | | | | | | |
| **CVTRA07Y** | | | | | | | | ✓ | | | | | | | | | | | | | | | | | | | | |
| **CVEXPORT** | | | | | | | | | | ✓ | ✓ | | | | | | | | | | | | | | | | | |
| **COSTM01** | | | | | | | | | ✓ | | | | | | | | | | | | | | | | | | | |
| **CUSTREC** | | | | | | | | | ✓ | | | | | | | | | | | | | | | | | | | |
| **CSUSR01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | | | | | | ✓ | ✓ | | ✓ | ✓ | ✓ | ✓ |
| **COCOM01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **COTTL01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CSDAT01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CSMSG01Y** | | | | | | | | | | | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CODATECN** | ✓ | | | | | | | | | | | | | | | | | | | | | | | | | | | |

### 3.2 Most-Referenced Copybooks (Fan-In)

| Rank | Copybook | # Programs | Role |
|------|----------|-----------|------|
| 1 | COCOM01Y | 17 | Communication area — every online program |
| 2 | COTTL01Y | 16 | Title/header — every online program |
| 3 | CSDAT01Y | 16 | Date structures — every online program |
| 4 | CSMSG01Y | 16 | Message area — every online program |
| 5 | CVACT01Y | 12 | Account record — core entity |
| 6 | CSUSR01Y | 11 | User security — auth + user mgmt |
| 7 | CVACT03Y | 11 | Card cross-reference — joins entity |
| 8 | CVTRA05Y | 10 | Transaction record — core entity |
| 9 | CVCUS01Y | 7 | Customer record — core entity |
| 10 | CSMSG02Y | 6 | Extended messages |

---

## 4. VSAM File Access Map

### 4.1 Online Programs — CICS File Access

| VSAM File (DD Name) | Dataset | COSGN00C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **USRSEC** | USRSEC.VSAM.KSDS | R | | | | | | | | | | R | W | RW | RD |
| **ACCTDAT** | ACCTDATA.VSAM.KSDS | | R | RW | | R | R | | | R | RW | | | | |
| **CARDDAT** | CARDDATA.VSAM.KSDS | | | | R | R | RW | | | | | | | | |
| **CARDXREF** | CARDXREF.VSAM.KSDS | | R | R | R | R | R | | | R | R | | | | |
| **CUSTDAT** | CUSTDATA.VSAM.KSDS | | R | R | | R | R | | | | | | | | |
| **TRANSACT** | TRANSACT.VSAM.KSDS | | | | | | | R | R | RW | RW | | | | |
| **TCATBALF** | TCATBALF.VSAM.KSDS | | | | | | | | | | | | | | |

> **Legend:** R = READ, W = WRITE, RW = READ + REWRITE, RD = READ + DELETE

### 4.2 Batch Programs — File Access

| VSAM / Sequential File | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **ACCTDATA** (VSAM) | R | | | RW | | R | RW | | | R | W |
| **CARDDATA** (VSAM) | | R | | | | R | | | | R | |
| **CARDXREF** (VSAM) | | | R | R | | R | R | R | R | R | W |
| **CUSTDATA** (VSAM) | | | | | R | R | | | R | R | W |
| **TRANSACT** (VSAM) | | | | | | | RW | | | R | W |
| **DALYTRAN** (PS) | | | | | | R | R | | | | |
| **TCATBALF** (VSAM) | | | | RW | | | RW | | | | |
| **DISCGRP** (VSAM) | | | | R | | | | | | | |
| **TRANTYPE** (VSAM) | | | | | | | | R | | | |
| **TRANCATG** (VSAM) | | | | | | | | R | | | |
| **TRXFL** (VSAM) | | | | | | | | | R | | |
| **STMTFILE** (PS) | | | | | | | | | W | | |
| **HTMLFILE** (PS) | | | | | | | | | W | | |
| **EXPORT.DATA** (PS) | | | | | | | | | | W | R |
| **DALYREJS** (PS) | | | | | | | R | | | | |
| **SYSTRAN** (GDG) | | | | W | | | | | | | |
| **DATEPARM** (PS) | | | | | | | | R | | | |
| **TRANREPT** (GDG) | | | | | | | | W | | | |

> **Legend:** R = READ/INPUT, W = WRITE/OUTPUT, RW = READ + UPDATE

---

## 5. JCL Job → Program → Dataset Lineage

### 5.1 Core Batch Processing Chain

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        NIGHTLY BATCH CYCLE                                  │
│                                                                             │
│  CLOSEFIL ─► SDSF: Close CICS files                                        │
│      │                                                                      │
│      ▼                                                                      │
│  Data Refresh (parallel):                                                   │
│  ┌─────────┬──────────┬──────────┬──────────┬──────────┐                    │
│  │ACCTFILE │ CARDFILE │ CUSTFILE │ XREFFILE │ TRANFILE │                    │
│  │PS→VSAM  │ PS→VSAM  │ PS→VSAM  │ PS→VSAM  │ PS→VSAM  │                    │
│  └────┬────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┘                    │
│       ▼         ▼          ▼          ▼          ▼                          │
│  POSTTRAN ─► CBTRN02C                                                       │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  DALYTRAN.PS, CARDXREF, ACCTDATA              │                      │
│  │ OUT: TRANSACT (updated), TCATBALF (updated),      │                      │
│  │      DALYREJS (rejects)                           │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  INTCALC ─► CBACT04C                                                        │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  ACCTDATA, CARDXREF, DISCGRP, TCATBALF        │                      │
│  │ OUT: ACCTDATA (updated), TCATBALF (updated),      │                      │
│  │      SYSTRAN (+1)                                 │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  TRANBKP ─► REPROC + IDCAMS                                                │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  TRANSACT.VSAM.KSDS                           │                      │
│  │ OUT: TRANSACT.BKUP (+1) — GDG backup              │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  COMBTRAN ─► SORT + IDCAMS                                                  │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  TRANSACT.BKUP(0), SYSTRAN(0)                 │                      │
│  │ OUT: TRANSACT.COMBINED (+1)                       │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  CREASTMT ─► SORT + CBSTM03A ─► CBSTM03B                                   │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  TRANSACT, CARDXREF, ACCTDATA, CUSTDATA       │                      │
│  │ OUT: STATEMNT.PS (text), STATEMNT.HTML (HTML)     │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  TRANREPT ─► REPROC + SORT + CBTRN03C                                      │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ IN:  TRANSACT.BKUP, CARDXREF, TRANTYPE, TRANCATG │                      │
│  │ OUT: TRANREPT (+1) — daily report GDG             │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  TRANIDX ─► IDCAMS                                                          │
│  ┌───────────────────────────────────────────────────┐                      │
│  │ Rebuild alternate indexes on TRANSACT VSAM        │                      │
│  └──────────────────────────┬────────────────────────┘                      │
│                             ▼                                               │
│  OPENFIL ─► SDSF: Re-open CICS files                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Job-to-Dataset Read/Write Matrix

| Job | Reads (Input) | Writes (Output) |
|-----|--------------|-----------------|
| **POSTTRAN** | DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM, TCATBALF.VSAM, TRANSACT.VSAM | TRANSACT.VSAM, ACCTDATA.VSAM, TCATBALF.VSAM, DALYREJS(+1) |
| **INTCALC** | ACCTDATA.VSAM, CARDXREF.VSAM, CARDXREF.AIX.PATH, DISCGRP.VSAM, TCATBALF.VSAM | ACCTDATA.VSAM, TCATBALF.VSAM, SYSTRAN(+1) |
| **TRANBKP** | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) |
| **COMBTRAN** | TRANSACT.BKUP(0), SYSTRAN(0) | TRANSACT.COMBINED(+1) |
| **CREASTMT** | TRANSACT.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, CUSTDATA.VSAM | TRXFL.SEQ, TRXFL.VSAM, STATEMNT.PS, STATEMNT.HTML |
| **TRANREPT** | TRANSACT.BKUP(+1), CARDXREF.VSAM, TRANTYPE.VSAM, TRANCATG.VSAM, DATEPARM | TRANSACT.DALY(+1), TRANREPT(+1) |
| **CBEXPORT** | ACCTDATA.VSAM, CARDDATA.VSAM, CARDXREF.VSAM, CUSTDATA.VSAM, TRANSACT.VSAM | EXPORT.DATA |
| **CBIMPORT** | EXPORT.DATA | ACCTDATA.IMPORT, CARDXREF.IMPORT, CUSTDATA.IMPORT, TRANSACT.IMPORT, IMPORT.ERRORS |
| **ACCTFILE** | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | CARDXREF.PS | CARDXREF.VSAM.KSDS + AIX |
| **TRANFILE** | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS + AIX |
| **DUSRSECJ** | USRSEC.PS | USRSEC.VSAM.KSDS |
| **TXT2PDF1** | STATEMNT.PS | PDF output (SYSOUT) |

---

## 6. Batch Cycle Data Flow

### 6.1 Data Lineage — End-to-End

```
                   ┌──────────────┐
 Input Files       │  Sequential  │
 (PS / Flat)       │  PS Files    │
                   └──────┬───────┘
                          │  IDCAMS REPRO
                          ▼
                   ┌──────────────┐
                   │  VSAM KSDS   │◄──── Online CICS Programs
 Master Files      │  Files       │      (READ/WRITE/REWRITE)
                   └──────┬───────┘
                          │
           ┌──────────────┼──────────────┐
           │              │              │
           ▼              ▼              ▼
    ┌────────────┐ ┌────────────┐ ┌────────────┐
    │ POSTTRAN   │ │ INTCALC    │ │ CREASTMT   │
    │ CBTRN02C   │ │ CBACT04C   │ │ CBSTM03A   │
    │ Post txns  │ │ Calc int.  │ │ Statements │
    └─────┬──────┘ └─────┬──────┘ └─────┬──────┘
          │              │              │
          ▼              ▼              ▼
    ┌────────────┐ ┌────────────┐ ┌────────────┐
    │ Updated    │ │ Updated    │ │ Statement  │
    │ TRANSACT   │ │ ACCTDATA   │ │ PS + HTML  │
    │ TCATBALF   │ │ TCATBALF   │ │ Output     │
    │ DALYREJS   │ │ SYSTRAN    │ │            │
    └────────────┘ └────────────┘ └─────┬──────┘
                                        │
                                        ▼
                                  ┌────────────┐
                                  │ TXT2PDF1   │
                                  │ PDF Convert│
                                  └────────────┘

    ┌────────────┐        ┌────────────┐
    │ TRANBKP    │        │ COMBTRAN   │
    │ Backup     │───────►│ Combine    │
    │ TRANSACT   │ GDG    │ Txn data   │
    └────────────┘        └────────────┘

    ┌────────────┐        ┌────────────┐
    │ TRANREPT   │        │ CBTRN03C   │
    │ Sort + Rep │───────►│ Daily Rpt  │
    │            │        │ TRANREPT   │
    └────────────┘        └────────────┘
```

### 6.2 GDG (Generation Data Group) Usage

| GDG Base | Used By | Purpose |
|----------|---------|---------|
| TRANSACT.BKUP | TRANBKP, COMBTRAN, TRANREPT | Transaction backup generations |
| TRANSACT.COMBINED | COMBTRAN | Combined transaction history |
| TRANSACT.DALY | TRANREPT | Daily transaction sort output |
| SYSTRAN | INTCALC, COMBTRAN | System-generated transactions (interest) |
| DALYREJS | POSTTRAN | Daily rejected transactions |
| TRANREPT | TRANREPT | Daily transaction reports |
| TCATBALF.BKUP | PRTCATBL | Category balance backups |
| DISCGRP.BKUP | DEFGDGD | Disclosure group backups |
| TRANTYPE.BKUP | DEFGDGD | Transaction type backups |
| TRANCATG.PS.BKUP | DEFGDGD | Transaction category backups |

---

## 7. Cross-Cutting Dependency Summary

### 7.1 Most-Connected Programs (Hub Analysis)

| Rank | Program | Inbound | Outbound | Files Accessed | Copybooks | Total Connections |
|------|---------|---------|----------|---------------|-----------|-------------------|
| 1 | **COACTUPC** | 1 (COACTVWC) | 2 (COACTVWC, COMEN01C) | 5 VSAM | 16 | 24 |
| 2 | **CBTRN02C** | 1 (JCL) | 1 (CEE3ABD) | 6 files | 5 | 13 |
| 3 | **CBSTM03A** | 1 (JCL) | 2 (CBSTM03B, CEE3ABD) | 6 files | 4 | 13 |
| 4 | **CBACT04C** | 1 (JCL) | 1 (CEE3ABD) | 5 files | 5 | 12 |
| 5 | **COCRDLIC** | 2 (COMEN01C, COCRDSLC) | 2 (COCRDSLC, COMEN01C) | 3 VSAM | 13 | 20 |
| 6 | **COMEN01C** | 7 programs | 6 programs | 0 direct | 9 | 22 |
| 7 | **COCRDUPC** | 1 (COCRDSLC) | 2 (COCRDSLC, COMEN01C) | 5 VSAM | 15 | 23 |
| 8 | **CBEXPORT** | 1 (JCL) | 1 (CEE3ABD) | 6 files | 6 | 14 |
| 9 | **COTRN02C** | 0 | 2 (CSUTLDTC, COMEN01C) | 3 VSAM | 10 | 15 |
| 10 | **COBIL00C** | 1 (COMEN01C) | 1 (COMEN01C) | 3 VSAM | 10 | 15 |

### 7.2 Shared Data Dependencies (Coupling Points)

| VSAM File | Online Programs | Batch Programs | Risk Level |
|-----------|----------------|----------------|------------|
| **ACCTDATA** | 4 (view, update, add txn, pay) | 5 (read, post, interest, stmt, export) | **HIGH** — Financial data, many writers |
| **TRANSACT** | 3 (list, view, add) | 4 (post, backup, stmt, report) | **HIGH** — Core transaction store |
| **CARDXREF** | 5 (view, update, list, card, pay) | 5 (read, post, interest, stmt, report) | **HIGH** — Critical join table |
| **CARDDATA** | 2 (list, update) | 2 (read, export) | MEDIUM |
| **CUSTDATA** | 3 (view, update, card) | 4 (read, validate, stmt, export) | MEDIUM |
| **USRSEC** | 4 (sign-on, list, add, upd, del) | 0 | MEDIUM — Auth data |
| **TCATBALF** | 0 | 3 (post, interest, print) | MEDIUM — Derived data |
| **DISCGRP** | 0 | 1 (interest) | LOW |
| **TRANTYPE** | 0 | 1 (report) | LOW — Reference data |
| **TRANCATG** | 0 | 1 (report) | LOW — Reference data |
