# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> Call graphs, copybook dependencies, data lineage, and batch job orchestration.

---

## 1. Online Program Call Graph (CICS XCTL / LINK)

```
                            ┌─────────────┐
                            │  COSGN00C   │  (Sign-on - Transaction CC00)
                            │  Entry Point│
                            └──────┬──────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │ (Admin)      │              │ (Regular User)
                    ▼              │              ▼
             ┌─────────────┐      │       ┌─────────────┐
             │  COADM01C   │      │       │  COMEN01C   │
             │  Admin Menu │      │       │  Main Menu  │
             └──────┬──────┘      │       └──────┬──────┘
                    │             │              │
         ┌──────┬──┘          (PF3 back)        ├──────────────────────────────┐
         │      │                │              │                              │
         ▼      ▼                │    ┌─────────┼──────────┬──────────┐       │
    ┌────────┐ ┌────────┐       │    │         │          │          │       │
    │COUSR00C│ │COPAUS0C│       │    ▼         ▼          ▼          ▼       ▼
    │User Lst│ │Auth Smr│       │ ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
    └───┬────┘ └───┬────┘       │ │COACTVWC││COCRDLIC││COTRN00C││CORPT00C││COBIL00C│
        │          │            │ │Acct Vew││Card Lst││Tran Lst││Reports ││Bill Pay│
   ┌────┼────┐     ▼            │ └───┬────┘└───┬────┘└───┬────┘└────────┘└────────┘
   │    │    │ ┌────────┐       │     │         │         │
   ▼    ▼    ▼ │COPAUS1C│       │     │    ┌────┼────┐    ├────────┐
┌────┐┌────┐┌────┐│Auth Dtl│    │     │    │         │    │        │
│USR1││USR2││USR3│└───┬────┘    │     │    ▼         ▼    ▼        ▼
│Add ││Upd ││Del │    │         │     │ ┌────────┐┌────────┐┌────────┐
└────┘└────┘└────┘    ▼         │     │ │COCRDSLC││COCRDUPC││COTRN01C│
                 ┌────────┐     │     │ │Card Dtl││Card Upd││Tran Vw │
                 │COPAUS2C│     │     │ └────────┘└────────┘└────────┘
                 │Fraud DB│     │     │
                 └────────┘     │     │                     ┌────────┐
                                │     └─────────────────────│COTRN02C│
                                │                           │Tran Add│
                                │                           └────────┘
                                │
                         (All programs return
                          to menu via PF3)
```

### Detailed XCTL Relationships

| Source Program | Target Program(s) | Trigger | Direction |
|---------------|-------------------|---------|-----------|
| **COSGN00C** | COADM01C, COMEN01C | Successful login | Forward (XCTL) |
| **COMEN01C** | COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C, COSGN00C, COPAUS0C | Menu option select / PF3 | Forward / Back |
| **COADM01C** | COUSR00C, COPAUS0C, COSGN00C | Admin option select / PF3 | Forward / Back |
| **COACTVWC** | COMEN01C, COCRDLIC, COCRDSLC, COCRDUPC | PF3 / Card list link | Back / Forward |
| **COACTUPC** | COMEN01C | PF3 | Back |
| **COCRDLIC** | COMEN01C, COCRDSLC, COCRDUPC | PF3 / S=View / U=Update | Back / Forward |
| **COCRDSLC** | COMEN01C, COCRDLIC | PF3 | Back |
| **COCRDUPC** | COMEN01C, COCRDLIC | PF3 | Back |
| **COTRN00C** | COMEN01C, COTRN01C, COTRN02C | PF3 / Select / Add | Back / Forward |
| **COTRN01C** | COMEN01C, COTRN00C | PF3 | Back |
| **COTRN02C** | COMEN01C, COTRN00C | PF3 | Back |
| **CORPT00C** | COMEN01C | PF3 | Back |
| **COBIL00C** | COMEN01C | PF3 | Back |
| **COUSR00C** | COADM01C, COUSR01C, COUSR02C, COUSR03C | PF3 / A=Add / U=Update / D=Delete | Back / Forward |
| **COUSR01C** | COADM01C, COUSR00C | PF3 | Back |
| **COUSR02C** | COADM01C, COUSR00C | PF3 | Back |
| **COUSR03C** | COADM01C, COUSR00C | PF3 | Back |
| **COPAUS0C** | COMEN01C, COPAUS1C | PF3 / Select | Back / Forward |
| **COPAUS1C** | COPAUS0C, COPAUS2C | PF3 / LINK for fraud mark | Back / Forward |

---

## 2. Subroutine CALL Graph

```
CORPT00C ──CALL──▶ CSUTLDTC  (Date validation utility)
COTRN02C ──CALL──▶ CSUTLDTC  (Date validation utility)
COBSWAIT ──CALL──▶ MVSWAIT   (ASM wait routine)
CBSTM03A ──CALL──▶ CBSTM03B  (Statement file I/O subroutine)
                   CEE3ABD   (LE abend handler)
CSUTLDTC ──CALL──▶ CEEDAYS   (LE date conversion intrinsic)

Optional Module calls:
COPAUA0C ──CALL──▶ MQOPEN, MQGET, MQPUT1, MQCLOSE  (MQ API)
COACCT01 ──CALL──▶ MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ API)
CODATE01 ──CALL──▶ MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ API)
DBUNLDGS ──CALL──▶ CBLTDLI                          (IMS DL/I API)
PAUDBLOD ──CALL──▶ CBLTDLI                          (IMS DL/I API)
PAUDBUNL ──CALL──▶ CBLTDLI                          (IMS DL/I API)
COPAUS2C ──────────▶ EXEC SQL (DB2 embedded SQL)
COTRTLIC ──────────▶ EXEC SQL (DB2 embedded SQL)
COTRTUPC ──────────▶ EXEC SQL (DB2 embedded SQL)
COBTUPDT ──────────▶ EXEC SQL (DB2 embedded SQL)
```

---

## 3. Copybook Dependency Matrix

Which programs include which copybooks (✓ = COPY statement present):

| Copybook | COSGN | COMEN | COADM | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT | COBIL | COUSR 00–03 |
|----------|:-----:|:-----:|:-----:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-----:|:-----:|:-----------:|
| **COCOM01Y** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **COTTL01Y** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CSDAT01Y** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CSMSG01Y** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **CSUSR01Y** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | ✓ |
| **CSMSG02Y** | — | — | — | ✓ | ✓ | — | ✓ | ✓ | — | — | — | — | — | — |
| **CVCRD01Y** | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | — |
| **CVACT01Y** | — | — | — | ✓ | ✓ | — | — | — | — | — | ✓ | — | ✓ | — |
| **CVACT02Y** | — | — | — | ✓ | — | ✓ | ✓ | ✓ | — | — | — | — | — | — |
| **CVACT03Y** | — | — | — | ✓ | ✓ | — | — | — | — | — | ✓ | — | ✓ | — |
| **CVCUS01Y** | — | — | — | ✓ | ✓ | — | ✓ | ✓ | — | — | — | — | — | — |
| **CVTRA05Y** | — | — | — | — | — | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — |
| **COMEN02Y** | — | ✓ | — | — | — | — | — | — | — | — | — | — | — | — |
| **COADM02Y** | — | — | ✓ | — | — | — | — | — | — | — | — | — | — | — |
| **CSSETATY** | — | — | — | — | ✓ | — | — | — | — | — | — | — | — | — |
| **CSSTRPFY** | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | — |
| **CSUTLDWY** | — | — | — | — | ✓ | — | — | — | — | — | — | — | — | — |
| **CSLKPCDY** | — | — | — | — | ✓ | — | — | — | — | — | — | — | — | — |
| **CSUTLDPY** | — | — | — | — | ✓ | — | — | — | — | — | — | — | — | — |

**Batch program copybook usage:**

| Copybook | CBACT01 | CBACT04 | CBTRN01 | CBTRN02 | CBTRN03 | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|----------|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|:--------:|
| **CVACT01Y** | ✓ | ✓ | — | ✓ | — | ✓ | — | ✓ | ✓ |
| **CVACT02Y** | — | — | — | — | — | — | — | ✓ | ✓ |
| **CVACT03Y** | — | — | — | ✓ | — | ✓ | — | ✓ | ✓ |
| **CVCUS01Y** | — | — | — | — | — | — | — | ✓ | ✓ |
| **CVTRA05Y** | — | — | ✓ | ✓ | ✓ | — | — | ✓ | ✓ |
| **CVTRA06Y** | — | — | — | ✓ | — | — | — | — | — |
| **CVTRA07Y** | — | — | — | — | ✓ | — | — | — | — |
| **COSTM01** | — | — | — | — | — | ✓ | — | — | — |
| **CUSTREC** | — | — | — | — | — | ✓ | — | — | — |
| **CVEXPORT** | — | — | — | — | — | — | — | ✓ | ✓ |

---

## 4. Data Lineage — VSAM File Access by Program

### 4.1 Online (CICS) Programs → VSAM Files

| VSAM File | Read | Read/Browse | Write | Update | Delete |
|-----------|------|-------------|-------|--------|--------|
| **USRSEC** | COSGN00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
| **ACCTDAT** | COACTVWC, COBIL00C | — | — | COACTUPC, COBIL00C | — |
| **CARDDAT** | COCRDSLC, COCRDUPC | COCRDLIC | — | COCRDUPC | — |
| **CARDAIX** | COACTVWC, COCRDSLC | COCRDLIC | — | — | — |
| **CUSTDAT** | COACTVWC, COCRDSLC | — | — | COACTUPC | — |
| **CCXREF / CXACAIX** | COACTVWC, COTRN02C, COBIL00C | — | — | — | — |
| **TRANSACT** | COTRN01C | COTRN00C, COBIL00C | COTRN02C | — | — |

### 4.2 Batch Programs → Files

| Program | Input Files (Read) | Output Files (Write) |
|---------|-------------------|---------------------|
| **CBACT01C** | ACCTFILE (VSAM) | OUTFILE (PS), ARRYFILE (PS), VBRCFILE (PS) |
| **CBACT02C** | CARDFILE (VSAM) | SYSPRINT |
| **CBACT03C** | XREFFILE (VSAM) | SYSPRINT |
| **CBACT04C** | ACCTFILE (VSAM), TCATBALF (VSAM), DISCGRP (VSAM) | ACCTFILE (VSAM - update), TRANFILE |
| **CBCUS01C** | CUSTFILE (VSAM) | SYSPRINT |
| **CBTRN01C** | TRANSACT (VSAM) | SYSPRINT |
| **CBTRN02C** | DALYTRAN (VSAM), XREFFILE (VSAM) | TRANSACT (VSAM), ACCTFILE (VSAM - update) |
| **CBTRN03C** | TRANFILE (PS), CARDXREF (VSAM), TRANTYPE (VSAM), CUSTFILE (VSAM) | REPTFILE (PS) |
| **CBSTM03A** | XREFFILE (VSAM), CUSTFILE (VSAM), ACCTFILE (VSAM) | STMTFILE (PS), HTMLFILE (PS) |
| **CBSTM03B** | TRNXFILE (VSAM), XREFFILE (VSAM), CUSTFILE (VSAM) | (returns data to CBSTM03A) |
| **CBEXPORT** | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE (all VSAM) | EXPORTFL (PS) |
| **CBIMPORT** | IMPORTFL (PS) | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE (all VSAM) |

---

## 5. Batch Job Orchestration — Data Flow

### 5.1 Nightly Batch Cycle

```
Step 1: CLOSEFIL ─────────────────────────────────────────────────────────────┐
        Close CICS-managed VSAM files for exclusive batch access              │
                                                                              │
Step 2: Data Refresh (parallel/sequential as needed)                          │
        ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │
        │ ACCTFILE │ │ CARDFILE │ │ CUSTFILE │ │ XREFFILE │ │ TRANFILE │    │
        │PS→VSAM   │ │PS→VSAM   │ │PS→VSAM   │ │PS→VSAM   │ │PS→VSAM   │    │
        └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘    │
             │            │            │            │            │            │
             ▼            ▼            ▼            ▼            ▼            │
Step 3: POSTTRAN ◄───────────────────────────────────────────────┘            │
        PGM=CBTRN02C                                                          │
        DALYTRAN(VSAM) + XREFFILE(VSAM) → TRANSACT(VSAM) + ACCTFILE(upd)    │
                    │                                                         │
                    ▼                                                         │
Step 4: INTCALC                                                               │
        PGM=CBACT04C                                                          │
        ACCTFILE + TCATBALF + DISCGRP → ACCTFILE(upd) + TRANFILE             │
                    │                                                         │
                    ▼                                                         │
Step 5: TRANBKP                                                               │
        TRANSACT(VSAM) → TRANSACT.BKUP(+1) (GDG backup)                     │
                    │                                                         │
                    ▼                                                         │
Step 6: COMBTRAN                                                              │
        SORT daily transactions, combine into master                          │
                    │                                                         │
                    ▼                                                         │
Step 7: CREASTMT                                                              │
        PGM=CBSTM03A                                                          │
        TRANSACT + XREFFILE + CUSTFILE + ACCTFILE                            │
        → STMTFILE(PS) + HTMLFILE(PS)                                        │
                    │                                                         │
                    ▼                                                         │
Step 8: TRANREPT                                                              │
        PGM=CBTRN03C                                                          │
        TRANFILE + CARDXREF + TRANTYPE + CUSTFILE → REPTFILE(PS)             │
                    │                                                         │
                    ▼                                                         │
Step 9: TRANIDX                                                               │
        Rebuild alternate indexes on TRANSACT VSAM                            │
                    │                                                         │
                    ▼                                                         │
Step 10: OPENFIL ─────────────────────────────────────────────────────────────┘
         Re-open CICS-managed VSAM files for online access
```

### 5.2 JCL Job → Program → Dataset Lineage

| JCL Job | Executes Program | Reads Dataset(s) | Writes Dataset(s) |
|---------|-----------------|-------------------|-------------------|
| **ACCTFILE** | IDCAMS | `ACCTDATA.PS` | `ACCTDATA.VSAM.KSDS` |
| **CARDFILE** | IDCAMS | `CARDDATA.PS` | `CARDDATA.VSAM.KSDS` |
| **CUSTFILE** | IDCAMS | `CUSTDATA.PS` | `CUSTDATA.VSAM.KSDS` |
| **XREFFILE** | IDCAMS | `CARDXREF.PS` | `CARDXREF.VSAM.KSDS` + AIX |
| **TRANFILE** | IDCAMS | `DALYTRAN.PS.INIT` | `TRANSACT.VSAM.KSDS` |
| **DUSRSECJ** | IDCAMS | `USRSEC.PS` | `USRSEC.VSAM.KSDS` |
| **TRANTYPE** | IDCAMS | `TRANTYPE.PS` | `TRANTYPE.VSAM.KSDS` |
| **TRANCATG** | IDCAMS | `TRANCATG.PS` | `TRANCATG.VSAM.KSDS` |
| **TCATBALF** | IDCAMS | `TCATBALF.PS` | `TCATBALF.VSAM.KSDS` |
| **POSTTRAN** | CBTRN02C | `DALYTRAN`, `CARDXREF` (VSAM) | `TRANSACT`, `ACCTDATA` (VSAM) |
| **INTCALC** | CBACT04C | `ACCTDATA`, `TCATBALF`, `DISCGRP` (VSAM) | `ACCTDATA` (VSAM upd) |
| **TRANBKP** | REPROC, IDCAMS | `TRANSACT.VSAM.KSDS` | `TRANSACT.BKUP(+1)` (GDG) |
| **COMBTRAN** | SORT, IDCAMS | Daily transaction files | Combined master |
| **CREASTMT** | SORT, CBSTM03A | `TRANSACT`, `XREFFILE`, `CUSTFILE`, `ACCTFILE` | `STMTFILE`, `HTMLFILE` |
| **TRANREPT** | REPROC, SORT, CBTRN03C | `TRANSACT.BKUP`, `CARDXREF`, `TRANTYPE`, `CUSTFILE` | `REPTFILE` |
| **READACCT** | CBACT01C | `ACCTDATA.VSAM.KSDS` | `OUTFILE`, `ARRYFILE`, `VBRCFILE` |
| **READCARD** | CBACT02C | `CARDDATA.VSAM.KSDS` | SYSPRINT |
| **READCUST** | CBCUS01C | `CUSTDATA.VSAM.KSDS` | SYSPRINT |
| **READXREF** | CBACT03C | `CARDXREF.VSAM.KSDS` | SYSPRINT |
| **CBEXPORT** | CBEXPORT | All VSAM masters | `EXPORTFL` (sequential) |
| **CBIMPORT** | CBIMPORT | `IMPORTFL` (sequential) | All VSAM masters |
| **WAITSTEP** | COBSWAIT | SYSIN (parm) | — |
| **TXT2PDF1** | IKJEFT1B | `STMTFILE` (PS) | PDF output |
| **CREASTMT** | SORT + CBSTM03A | `TRANSACT.VSAM.KSDS` | `STMTFILE`, `HTMLFILE` |

---

## 6. VSAM File → Consumer Matrix

Which files are accessed by which programs (R=Read, W=Write, U=Update, D=Delete, B=Browse):

| VSAM File | Online Programs | Batch Programs | JCL (IDCAMS) |
|-----------|----------------|----------------|--------------|
| **ACCTDAT** | R: COACTVWC, COBIL00C; U: COACTUPC, COBIL00C | R: CBACT01C, CBSTM03A; RW: CBTRN02C, CBACT04C; RW: CBEXPORT/IMPORT | ACCTFILE |
| **CARDDAT** | RB: COCRDLIC; R: COCRDSLC; RU: COCRDUPC | R: CBACT02C; RW: CBEXPORT/IMPORT | CARDFILE |
| **CUSTDAT** | R: COACTVWC, COCRDSLC; U: COACTUPC | R: CBCUS01C, CBSTM03A/B, CBTRN03C; RW: CBEXPORT/IMPORT | CUSTFILE |
| **CCXREF** | R: COACTVWC, COTRN02C, COBIL00C | R: CBTRN02C, CBTRN03C, CBSTM03A/B; RW: CBEXPORT/IMPORT | XREFFILE |
| **TRANSACT** | RB: COTRN00C; R: COTRN01C; W: COTRN02C; RB: COBIL00C | R: CBTRN01C; RW: CBTRN02C; R: CBSTM03A | TRANFILE |
| **USRSEC** | R: COSGN00C; RB: COUSR00C; W: COUSR01C; RU: COUSR02C; RD: COUSR03C | — | DUSRSECJ |
| **TRANTYPE** | — | R: CBTRN03C | TRANTYPE |
| **TRANCATG** | — | R: CBTRN03C | TRANCATG |
| **TCATBALF** | — | R: CBACT04C | TCATBALF |
| **DISCGRP** | — | R: CBACT04C | DISCGRP |
| **DALYTRAN** | — | R: CBTRN02C | (loaded by TRANFILE job) |

---

## 7. BMS Map → Program Binding

| BMS Map | Mapset | Program | Screen Direction |
|---------|--------|---------|-----------------|
| COSGN0A | COSGN00 | COSGN00C | SEND/RECEIVE |
| COMEN1A | COMEN01 | COMEN01C | SEND/RECEIVE |
| COADM1A | COADM01 | COADM01C | SEND/RECEIVE |
| CACTVWA | COACTVW | COACTVWC | SEND/RECEIVE |
| CACTUPA | COACTUP | COACTUPC | SEND/RECEIVE |
| CCRDLIA | COCRDLI | COCRDLIC | SEND/RECEIVE |
| CCRDSLA | COCRDSL | COCRDSLC | SEND/RECEIVE |
| CCRDUPA | COCRDUP | COCRDUPC | SEND/RECEIVE |
| COTRN0A | COTRN00 | COTRN00C | SEND/RECEIVE |
| COTRN1A | COTRN01 | COTRN01C | SEND/RECEIVE |
| COTRN2A | COTRN02 | COTRN02C | SEND/RECEIVE |
| CORPT0A | CORPT00 | CORPT00C | SEND/RECEIVE |
| COBIL0A | COBIL00 | COBIL00C | SEND/RECEIVE |
| COUSR0A | COUSR00 | COUSR00C | SEND/RECEIVE |
| COUSR1A | COUSR01 | COUSR01C | SEND/RECEIVE |
| COUSR2A | COUSR02 | COUSR02C | SEND/RECEIVE |
| COUSR3A | COUSR03 | COUSR03C | SEND/RECEIVE |

---

## 8. Technology Dependency Summary

```
┌────────────────────────────────────────────────────────────────┐
│                    CardDemo Technology Stack                     │
├─────────────┬──────────────────────────────────────────────────┤
│ Presentation│  BMS 3270 Maps (17 screens)                      │
│             │  → Modernize to: React/Angular Web UI            │
├─────────────┼──────────────────────────────────────────────────┤
│ Application │  COBOL/CICS Online Programs (17)                 │
│ (Online)    │  → Modernize to: Spring Boot REST Controllers    │
├─────────────┼──────────────────────────────────────────────────┤
│ Application │  COBOL Batch Programs (14)                       │
│ (Batch)     │  → Modernize to: Spring Batch Jobs               │
├─────────────┼──────────────────────────────────────────────────┤
│ Data Access │  CICS File Control (READ/WRITE/BROWSE)           │
│             │  COBOL File I/O (OPEN/READ/WRITE/CLOSE)          │
│             │  → Modernize to: Spring Data JPA Repositories    │
├─────────────┼──────────────────────────────────────────────────┤
│ Data Storage│  VSAM KSDS (12 files) + AIX (2 alternate index)  │
│             │  Sequential PS files (reports, exports)           │
│             │  GDG (backup generations)                         │
│             │  → Modernize to: PostgreSQL/MySQL tables          │
├─────────────┼──────────────────────────────────────────────────┤
│ Middleware  │  CICS Transaction Server                          │
│ (Optional)  │  IBM MQ (message queuing)                         │
│             │  IMS DB (hierarchical database)                   │
│             │  DB2 (relational - transaction types)             │
│             │  → Modernize to: Spring Boot + RabbitMQ/Kafka     │
├─────────────┼──────────────────────────────────────────────────┤
│ Scheduling  │  JCL + CA-7 / Control-M                          │
│             │  → Modernize to: Spring Scheduler / Airflow       │
├─────────────┼──────────────────────────────────────────────────┤
│ Utilities   │  IDCAMS, SORT, IEBGENER, FTP                    │
│             │  → Modernize to: SQL DDL, Java Stream API        │
└─────────────┴──────────────────────────────────────────────────┘
```

---

## 9. Critical Data Paths (High-Risk Flows)

These data flows touch financial records and require extra care during modernization:

1. **Transaction Posting**: `DALYTRAN → CBTRN02C → TRANSACT + ACCTDAT` — Updates account balances; must maintain ACID properties
2. **Interest Calculation**: `ACCTDAT + TCATBALF + DISCGRP → CBACT04C → ACCTDAT` — Modifies balances based on rate tables
3. **Bill Payment**: `COBIL00C → TRANSACT + ACCTDAT` — Creates payment transaction and updates balance
4. **Statement Generation**: `TRANSACT + XREF + CUST + ACCT → CBSTM03A → STMTFILE` — Multi-file join producing customer-facing documents
5. **Data Export/Import**: `All VSAM → CBEXPORT → EXPORTFL` and reverse — Full data migration path; data integrity critical
