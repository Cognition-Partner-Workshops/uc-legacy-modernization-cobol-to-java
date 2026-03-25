# Dependency Map — CardDemo COBOL Codebase

> **Source:** Static analysis of CALL, COPY, EXEC CICS, and JCL DD statements across the entire codebase.
> **Purpose:** Document the call graph (which programs call which) and data lineage (which jobs read/write which files) to guide modernization sequencing and impact analysis.

---

## Table of Contents

1. [Program-to-Program Call Graph](#1-program-to-program-call-graph)
2. [Program-to-Copybook Dependencies](#2-program-to-copybook-dependencies)
3. [CICS Navigation Flow](#3-cics-navigation-flow)
4. [Batch Job Data Lineage](#4-batch-job-data-lineage)
5. [Batch Processing Sequence](#5-batch-processing-sequence)
6. [VSAM File Access Matrix](#6-vsam-file-access-matrix)
7. [Cross-Cutting Concerns](#7-cross-cutting-concerns)

---

## 1. Program-to-Program Call Graph

### 1.1 Direct CALL Relationships

```
CBACT01C ──CALL──► COBDATFT (assembler: date formatting)

CBACT02C ──CALL──► CEE3ABD (LE: abnormal termination)
CBACT03C ──CALL──► CEE3ABD
CBCUS01C ──CALL──► CEE3ABD

CBSTM03A ──CALL──► CBSTM03B (subroutine: file I/O for statements)

CSUTLDTC ──CALL──► CEEDAYS (LE: date conversion)
```

### 1.2 CICS Program-to-Program (XCTL / RETURN TRANSID)

All online CICS programs use **COMMAREA** (COCOM01Y) to pass context between screens. Navigation is driven by the menu copybooks (COMEN02Y, COADM02Y) which map option numbers to program names.

```
COSGN00C (Sign-on)
    │
    ├──► COMEN01C (Main Menu)  ── for Regular Users
    │       ├──► COACTVWC  (Account View)
    │       ├──► COACTUPC  (Account Update)
    │       ├──► COCRDLIC  (Card List)
    │       │       └──► COCRDSLC  (Card Detail)
    │       │               └──► COCRDUPC  (Card Update)
    │       ├──► COTRN00C  (Transaction List)
    │       │       └──► COTRN01C  (Transaction View)
    │       ├──► COTRN02C  (Transaction Add)
    │       ├──► CORPT00C  (Reports)
    │       ├──► COBIL00C  (Bill Payment)
    │       └──► COPAUS0C  (Auth Summary) [optional module]
    │
    └──► COADM01C (Admin Menu)  ── for Admin Users
            ├──► COUSR00C  (User List)
            │       ├──► COUSR02C  (User Update)
            │       └──► COUSR03C  (User Delete)
            ├──► COUSR01C  (User Add)
            ├──► COTRTLIC  (Tran Type List) [optional module]
            └──► COTRTUPC  (Tran Type Update) [optional module]
```

### 1.3 Optional Module Internal Calls

```
Authorization Module:
    COPAUA0C (MQ trigger) ──► processes authorization request
    COPAUS0C (Summary) ──► COPAUS1C (Detail) ──► COPAUS2C (Mark Fraud)
    CBPAUP0C (Batch purge expired authorizations)

VSAM-MQ Module:
    COACCT01 ──► reads VSAM, responds via MQ (account inquiry)
    CODATE01 ──► responds via MQ (system date inquiry)
```

---

## 2. Program-to-Copybook Dependencies

### 2.1 Core Programs

| Program | Copybooks Used | Count |
|---------|---------------|------:|
| **COACTUPC** | COCOM01Y, COACTUP (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSSETATY, CSLKPCDY, CSUTLDPY, CSUTLDWY, CSSTRPFY, DFHAID, DFHBMSCA + more | **56** |
| **COACTVWC** | COCOM01Y, COACTVW (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSSETATY, CSUTLDPY, CSUTLDWY, DFHAID, DFHBMSCA | 15 |
| **COCRDLIC** | COCOM01Y, COCRDLI (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CSSETATY, CSUTLDPY, CSUTLDWY, DFHAID, DFHBMSCA | 13 |
| **COCRDUPC** | COCOM01Y, COCRDUP (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CSSETATY, CSLKPCDY, CSUTLDPY, CSUTLDWY, CSSTRPFY, DFHAID, DFHBMSCA | 15 |
| **COCRDSLC** | COCOM01Y, COCRDSL (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CSSETATY, CSUTLDPY, CSUTLDWY, DFHAID, DFHBMSCA, CVCUS01Y, CVACT01Y | 15 |
| **COTRN00C** | COCOM01Y, COTRN00 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | 8 |
| **COTRN01C** | COCOM01Y, COTRN01 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA | 8 |
| **COTRN02C** | COCOM01Y, COTRN02 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA | 10 |
| **COBIL00C** | COCOM01Y, COBIL00 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA | 10 |
| **CORPT00C** | COCOM01Y, CORPT00 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA, CSSETATY | 8 |
| **COUSR00C** | COCOM01Y, COUSR00 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | 8 |
| **COUSR01C** | COCOM01Y, COUSR01 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | 9 |
| **COUSR02C** | COCOM01Y, COUSR02 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | 8 |
| **COUSR03C** | COCOM01Y, COUSR03 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | 8 |
| **COSGN00C** | COCOM01Y, COSGN00 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA, COMEN02Y | 9 |
| **COMEN01C** | COCOM01Y, COMEN01 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, COMEN02Y, DFHAID, DFHBMSCA, COADM02Y | 9 |
| **COADM01C** | COCOM01Y, COADM01 (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, COADM02Y, DFHAID, DFHBMSCA, COMEN02Y | 9 |

### 2.2 Batch Programs

| Program | Copybooks Used | Count |
|---------|---------------|------:|
| **CBACT01C** | CVACT01Y, CODATECN | 2 |
| **CBACT02C** | CVACT02Y | 1 |
| **CBACT03C** | CVACT03Y | 1 |
| **CBACT04C** | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y | 5 |
| **CBCUS01C** | CVCUS01Y | 1 |
| **CBTRN01C** | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y | 6 |
| **CBTRN02C** | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y | 5 |
| **CBTRN03C** | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y | 5 |
| **CBSTM03A** | COSTM01, CVACT03Y, CUSTREC, CVACT01Y | 4 |
| **CBEXPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT | 6 |
| **CBIMPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT | 6 |

### 2.3 Shared Copybook Usage Heat Map

| Copybook | Used By (# programs) | Role |
|----------|---------------------:|------|
| COCOM01Y | 17 | CICS communication area — used by every online program |
| DFHAID | 17 | CICS attention identifier constants |
| DFHBMSCA | 17 | BMS attribute constants |
| COTTL01Y | 17 | Screen title/header — every online program |
| CSDAT01Y | 17 | Date work area — every online program |
| CSMSG01Y | 17 | Message display area — every online program |
| CVACT01Y | 10 | Account record — most business programs |
| CVACT03Y | 10 | Cross-reference — most business programs |
| CVTRA05Y | 8 | Transaction record — transaction programs |
| CSUSR01Y | 6 | User security — admin programs |
| CVACT02Y | 6 | Card record — card programs |
| CVCUS01Y | 5 | Customer record |

---

## 3. CICS Navigation Flow

### 3.1 Transaction ID to Program Mapping

| CICS Trans ID | Program | Screen | Access Level |
|--------------|---------|--------|-------------|
| CC00 | COSGN00C | Sign-on | All |
| CM00 | COMEN01C | Main Menu | Regular + Admin |
| CA00 | COADM01C | Admin Menu | Admin only |
| CA01 | COACTVWC | Account View | Regular + Admin |
| CA02 | COACTUPC | Account Update | Regular + Admin |
| CC01 | COCRDLIC | Card List | Regular + Admin |
| CC02 | COCRDSLC | Card Detail | Regular + Admin |
| CC03 | COCRDUPC | Card Update | Regular + Admin |
| CT00 | COTRN00C | Transaction List | Regular + Admin |
| CT01 | COTRN01C | Transaction View | Regular + Admin |
| CT02 | COTRN02C | Transaction Add | Regular + Admin |
| CR00 | CORPT00C | Reports | Regular + Admin |
| CB00 | COBIL00C | Bill Payment | Regular + Admin |
| CU00 | COUSR00C | User List | Admin only |
| CU01 | COUSR01C | User Add | Admin only |
| CU02 | COUSR02C | User Update | Admin only |
| CU03 | COUSR03C | User Delete | Admin only |

### 3.2 Screen Navigation Diagram

```
                    ┌─────────────┐
                    │  COSGN00C   │
                    │  (Sign-on)  │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
    ┌─────────────────┐      ┌─────────────────┐
    │    COMEN01C     │      │    COADM01C     │
    │  (Main Menu)    │      │  (Admin Menu)   │
    │  11 options     │      │   6 options     │
    └────────┬────────┘      └────────┬────────┘
             │                        │
    ┌────────┼────────┐      ┌────────┼────────┐
    │        │        │      │        │        │
    ▼        ▼        ▼      ▼        ▼        ▼
 Account   Card    Transaction    User CRUD   Tran Type
 ┌──────┐ ┌──────┐ ┌──────┐   ┌──────┐      (DB2 module)
 │View  │ │List  │ │List  │   │List  │
 │Update│ │Detail│ │View  │   │Add   │
 └──────┘ │Update│ │Add   │   │Update│
          └──────┘ └──────┘   │Delete│
                              └──────┘
    Reports    Bill Pay    Auth Summary
    ┌──────┐   ┌──────┐    (IMS module)
    │Config│   │ Pay  │
    └──────┘   └──────┘
```

---

## 4. Batch Job Data Lineage

### 4.1 JCL Job → Program → File (Read/Write)

| JCL Job | Program Executed | Files Read | Files Written |
|---------|-----------------|------------|---------------|
| **POSTTRAN** | CBTRN02C | DAILYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBAL | TRANSACT, ACCTDATA, TCATBAL, DALYREJS |
| **INTCALC** | CBACT04C | TCATBAL, CARDXREF, DISCGRP, ACCTDATA | TRANSACT, ACCTDATA |
| **CREASTMT** | SORT + CBSTM03A | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | STMTFILE (PS), HTMLFILE (PS), TRXFL.SEQ |
| **TRANREPT** | REPROC + SORT + CBTRN03C | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) |
| **TRANBKP** | REPROC + IDCAMS | TRANSACT | Backup GDG dataset |
| **COMBTRAN** | SORT + IDCAMS | TRANSACT, DAILYTRAN | Combined transaction file |
| **READACCT** | CBACT01C | ACCTDATA | PSCOMP, ARRYPS, VBPS (sequential exports) |
| **READCARD** | CBACT02C | CARDDATA | (print output) |
| **READCUST** | CBCUS01C | CUSTDATA | (print output) |
| **READXREF** | CBACT03C | CARDXREF | (print output) |
| **CBEXPORT** | CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | Export sequential file |
| **CBIMPORT** | CBIMPORT | Export sequential file | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA |

### 4.2 Data Refresh Jobs (IDCAMS)

| JCL Job | Action | Target VSAM | Source |
|---------|--------|-------------|--------|
| ACCTFILE | DELETE + DEFINE + REPRO | ACCTDATA.VSAM.KSDS | ACCTDATA.PS (sequential) |
| CARDFILE | DELETE + DEFINE + REPRO | CARDDATA.VSAM.KSDS | CARDDATA.PS |
| CUSTFILE | DELETE + DEFINE + REPRO | CUSTDATA.VSAM.KSDS | CUSTDATA.PS |
| XREFFILE | DELETE + DEFINE + REPRO | CARDXREF.VSAM.KSDS | CARDXREF.PS |
| TRANFILE | DELETE + DEFINE + REPRO | TRANSACT.VSAM.KSDS | TRANSACT.PS |
| DUSRSECJ | DELETE + DEFINE + REPRO | USRSEC.VSAM.KSDS | USRSEC.PS |

### 4.3 VSAM File Data Flow Diagram

```
                         ┌─────────────────┐
                         │  Daily Tran File │ (input from external systems)
                         │  DAILYTRAN.VSAM  │
                         └────────┬─────────┘
                                  │
                           POSTTRAN (CBTRN02C)
                          ┌───────┴───────┐
                          │               │
                          ▼               ▼
              ┌───────────────┐  ┌─────────────┐
              │  TRANSACT     │  │  DALYREJS    │ (rejected transactions)
              │  .VSAM.KSDS  │  └─────────────┘
              └───────┬───────┘
                      │
        ┌─────────────┼──────────────┬──────────────┐
        │             │              │              │
   INTCALC       CREASTMT       TRANREPT       TRANBKP
   (CBACT04C)   (CBSTM03A)    (CBTRN03C)     (REPROC)
        │             │              │              │
        ▼             ▼              ▼              ▼
   ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
   │ ACCTDATA│  │ Statement│  │ Report   │  │  Backup  │
   │ (update)│  │ (HTML/PS)│  │ (print)  │  │  (GDG)   │
   └─────────┘  └──────────┘  └──────────┘  └──────────┘

 Supporting lookups used by multiple jobs:
   CARDXREF.VSAM ── card-to-account-to-customer resolution
   CUSTDATA.VSAM ── customer name/address for statements
   TCATBAL.VSAM  ── category balances (read + updated by POSTTRAN, INTCALC)
   DISCGRP.VSAM  ── interest rates (read by INTCALC)
   TRANTYPE.VSAM ── type descriptions (read by TRANREPT)
   TRANCATG.VSAM ── category descriptions (read by TRANREPT)
```

---

## 5. Batch Processing Sequence

The nightly batch cycle must execute in this exact order due to data dependencies:

```
Step 1: CLOSEFIL ──► Close CICS-managed VSAM files for exclusive batch access
            │
Step 2: Data Refresh (parallel-safe within this group)
            ├── ACCTFILE  ── Refresh account master
            ├── CARDFILE  ── Refresh card master
            ├── CUSTFILE  ── Refresh customer master
            ├── XREFFILE  ── Refresh cross-reference
            ├── TRANFILE  ── Refresh transaction master
            └── DUSRSECJ  ── Refresh user security
            │
Step 3: POSTTRAN ──► Post daily transactions to master
            │         (reads DAILYTRAN, writes TRANSACT + ACCTDATA + TCATBAL)
            │
Step 4: INTCALC ──► Calculate interest charges
            │         (reads TCATBAL + DISCGRP + ACCTDATA, writes TRANSACT + ACCTDATA)
            │
Step 5: TRANBKP ──► Backup transaction data to GDG
            │
Step 6: COMBTRAN ──► Combine/consolidate transaction files
            │
Step 7: CREASTMT ──► Generate customer statements (HTML + print)
            │         (reads TRANSACT + XREF + ACCTDATA + CUSTDATA)
            │
Step 8: TRANIDX ──► Rebuild alternate indexes on transaction VSAM
            │
Step 9: OPENFIL ──► Reopen CICS-managed files for online access
```

**Critical Dependencies:**
- POSTTRAN must run before INTCALC (interest calculated on posted transactions)
- INTCALC must run before CREASTMT (statements include interest charges)
- TRANBKP should run before COMBTRAN (backup before consolidation)
- CLOSEFIL/OPENFIL bracket the entire batch window

---

## 6. VSAM File Access Matrix

Shows which programs access which VSAM files and the access mode.

| VSAM File | Online Read | Online Write | Batch Read | Batch Write |
|-----------|-------------|-------------|------------|-------------|
| **ACCTDATA** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | COACTUPC, COBIL00C | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT | CBACT04C, CBTRN02C, CBIMPORT |
| **CARDDATA** | COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC | CBACT02C, CBTRN01C, CBEXPORT | CBIMPORT |
| **CUSTDATA** | COACTVWC, COACTUPC | — | CBCUS01C, CBSTM03A, CBEXPORT | CBIMPORT |
| **TRANSACT** | COTRN00C, COTRN01C, COTRN02C | COTRN02C | CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | CBTRN02C, CBACT04C, CBIMPORT |
| **CARDXREF** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | — | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | CBIMPORT |
| **USRSEC** | COSGN00C, COUSR00C-03C | COUSR01C, COUSR02C, COUSR03C | — | — |
| **TCATBAL** | — | — | CBACT04C, CBTRN02C | CBTRN02C, CBACT04C |
| **DISCGRP** | — | — | CBACT04C | — |
| **TRANTYPE** | — | — | CBTRN03C | — |
| **TRANCATG** | — | — | CBTRN03C | — |
| **DAILYTRAN** | — | — | CBTRN01C, CBTRN02C | — |

---

## 7. Cross-Cutting Concerns

### 7.1 Shared Infrastructure Copybooks

These copybooks are included by nearly every online program and represent cross-cutting concerns that should become shared services or base classes in Java:

| Copybook | Purpose | Java Equivalent |
|----------|---------|----------------|
| COCOM01Y | Session/context passing | Session DTO / Spring Security context |
| COTTL01Y | Page title/header | Base template / layout component |
| CSDAT01Y | Date/time formatting | `DateTimeFormatter` utility |
| CSMSG01Y | User messages | Flash messages / notification service |
| DFHAID | Keyboard attention IDs | Key event handling (N/A for web) |
| DFHBMSCA | Screen attributes | CSS styling (N/A for web) |
| CSSETATY | Field attribute setting | Form validation / CSS classes |
| CSLKPCDY | Phone/state code lookup | Reference data service |
| CSUTLDPY | Display utility | Formatting utility class |
| CSUTLDWY | Date edit/validation | Date validation utility |
| CSSTRPFY | String prefix utility | `String` utility methods |

### 7.2 High Fan-Out Programs (Most Dependencies)

Programs that depend on the most files/copybooks and thus have the widest blast radius if changed:

1. **COACTUPC** — 56 COPY references, reads/writes 4 VSAM files
2. **CBSTM03A** — reads 4 VSAM files, generates 2 output formats, calls subroutine
3. **CBTRN02C** — reads/writes 6 files (DAILYTRAN, TRANSACT, XREF, DALYREJS, ACCTDATA, TCATBAL)
4. **CBEXPORT/CBIMPORT** — touches all 5 core business VSAM files

### 7.3 High Fan-In Copybooks (Most Depended Upon)

If these copybooks change, the most programs are affected:

1. **COCOM01Y** — 17 programs (all online)
2. **CVACT01Y** — 10 programs (Account record)
3. **CVACT03Y** — 10 programs (Cross-Reference)
4. **CVTRA05Y** — 8 programs (Transaction record)
