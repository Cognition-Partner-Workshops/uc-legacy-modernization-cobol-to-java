# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Source:** Static analysis of CALL, EXEC CICS XCTL/LINK/READ/WRITE, COPY, and JCL DD/EXEC PGM statements

---

## 1. Online Program Call Graph (CICS XCTL Transfers)

The CICS online system uses pseudo-conversational programming. Programs transfer control via `EXEC CICS XCTL` using the COMMAREA (`COCOM01Y`) to pass session state.

### 1.1 Entry Point and Navigation Flow

```
                          ┌─────────────┐
                          │  COSGN00C   │  (CC00 — Sign-on)
                          │  Login      │
                          └──────┬──────┘
                                 │ XCTL (based on user type)
                    ┌────────────┴────────────┐
                    ▼                         ▼
             ┌─────────────┐          ┌─────────────┐
             │  COMEN01C   │          │  COADM01C   │
             │  Main Menu  │          │  Admin Menu  │
             │  (CM00)     │          │  (CA00)      │
             └──────┬──────┘          └──────┬──────┘
                    │                        │
    ┌───────┬───────┼───────┬────────┐       │
    ▼       ▼       ▼       ▼        ▼       ▼
COACTVWC COCRDLIC COTRN00C COBIL00C CORPT00C COUSR00C
(CA01)   (CC01)   (CT00)   (CB00)   (CR00)   (CU00)
Acct View Card List Tran List Bill Pay Reports User List
```

### 1.2 Detailed XCTL Transfer Table

| Source Program | Target Program | Trigger | Direction |
|---------------|---------------|---------|-----------|
| COSGN00C | COMEN01C | Regular user login | Forward |
| COSGN00C | COADM01C | Admin user login | Forward |
| COMEN01C | COACTVWC | Menu option 1 | Forward |
| COMEN01C | COCRDLIC | Menu option 2 | Forward |
| COMEN01C | COTRN00C | Menu option 3 | Forward |
| COMEN01C | COBIL00C | Menu option 4 | Forward |
| COMEN01C | CORPT00C | Menu option 5 | Forward |
| COADM01C | COUSR00C | Admin option 1 | Forward |
| COMEN01C | *CDEMO-TO-PROGRAM* | Dynamic — via COMMAREA | Return |
| COACTVWC | COACTUPC | User requests update | Forward |
| COACTVWC | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COACTUPC | *CDEMO-TO-PROGRAM* | F3/Back or after save | Return |
| COCRDLIC | COCRDSLC | Select card for view | Forward |
| COCRDLIC | COCRDUPC | Select card for update | Forward |
| COCRDLIC | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COCRDSLC | COCRDUPC | User requests update | Forward |
| COCRDSLC | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COCRDUPC | *CDEMO-TO-PROGRAM* | F3/Back or after save | Return |
| COTRN00C | COTRN01C | Select transaction to view | Forward |
| COTRN00C | COTRN02C | Select transaction to add | Forward |
| COTRN00C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COTRN01C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COTRN02C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COBIL00C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| CORPT00C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COUSR00C | COUSR01C | Add new user | Forward |
| COUSR00C | COUSR02C | Update user | Forward |
| COUSR00C | COUSR03C | Delete user | Forward |
| COUSR00C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COUSR01C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COUSR02C | *CDEMO-TO-PROGRAM* | F3/Back | Return |
| COUSR03C | *CDEMO-TO-PROGRAM* | F3/Back | Return |

> **Note:** `CDEMO-TO-PROGRAM` is a dynamic target stored in the COMMAREA, typically pointing back to the calling program or the main/admin menu.

### 1.3 Sub-Navigation Chains

```
Account Flow:   COMEN01C → COACTVWC → COACTUPC → (back)
Card Flow:      COMEN01C → COCRDLIC → COCRDSLC → COCRDUPC → (back)
Transaction:    COMEN01C → COTRN00C → COTRN01C (view) or COTRN02C (add)
Bill Payment:   COMEN01C → COBIL00C
Reporting:      COMEN01C → CORPT00C
User Admin:     COADM01C → COUSR00C → COUSR01C/02C/03C → (back)
```

---

## 2. Program CALL Graph (Subroutine Calls)

### 2.1 Application-Level Calls

```
COTRN02C ──CALL──▶ CSUTLDTC (Date validation)
CORPT00C ──CALL──▶ CSUTLDTC (Date validation)
CBSTM03A ──CALL──▶ CBSTM03B (File I/O subroutine — 12 call sites)
CBACT01C ──CALL──▶ COBDATFT (Assembler date formatting)
COBSWAIT ──CALL──▶ MVSWAIT  (Assembler wait SVC)
```

### 2.2 System-Level Calls

| Calling Program | System Routine | Purpose |
|----------------|---------------|---------|
| CBACT01C | CEE3ABD | Abnormal termination (abend) |
| CBACT02C | CEE3ABD | Abnormal termination |
| CBACT03C | CEE3ABD | Abnormal termination |
| CBACT04C | CEE3ABD | Abnormal termination |
| CBCUS01C | CEE3ABD | Abnormal termination |
| CBTRN01C | CEE3ABD | Abnormal termination |
| CBTRN02C | CEE3ABD | Abnormal termination |
| CBTRN03C | CEE3ABD | Abnormal termination |
| CBSTM03A | CEE3ABD | Abnormal termination |
| CBEXPORT | CEE3ABD | Abnormal termination |
| CBIMPORT | CEE3ABD | Abnormal termination |
| CSUTLDTC | CEEDAYS | LE date conversion (Lillian days) |

### 2.3 Complete Call Hierarchy

```
CBSTM03A (Statement Generation)
  ├── CALL CBSTM03B    (File I/O — open, read, close, write)
  └── CALL CEE3ABD     (Error handling)

CBACT01C (Read Account Master)
  ├── CALL COBDATFT    (Date format conversion)
  └── CALL CEE3ABD     (Error handling)

COTRN02C (Add Transaction)
  └── CALL CSUTLDTC    (Date validation × 2)
        └── CALL CEEDAYS   (LE date API)

CORPT00C (Transaction Reports)
  └── CALL CSUTLDTC    (Date validation × 2)
        └── CALL CEEDAYS   (LE date API)

COBSWAIT (Wait Utility)
  └── CALL MVSWAIT     (Assembler MVS WAIT)
```

---

## 3. Copybook Dependency Matrix

Shows which programs COPY which copybooks. A `●` indicates an active COPY statement; a `○` indicates a commented-out COPY.

### 3.1 Online Programs → Copybooks

| Copybook | COSGN | COMEN | COADM | COACTVW | COACTUP | COCRDLI | COCRDSL | COCRDUP | COTRN00 | COTRN01 | COTRN02 | COBIL | CORPT | COUSR00 | COUSR01 | COUSR02 | COUSR03 |
|----------|-------|-------|-------|---------|---------|---------|---------|---------|---------|---------|---------|-------|-------|---------|---------|---------|---------|
| COCOM01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| COTTL01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| CSDAT01Y | | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | | | |
| CSMSG01Y | | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | | | |
| CSMSG02Y | | | | ● | ● | | ● | ● | | | ● | | | | | | |
| CSUSR01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| DFHAID | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| DFHBMSCA | | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| CVACT01Y | | | | ● | ● | | | | | | | | | | | | |
| CVACT02Y | | | | ● | ● | ● | ● | ● | | | | | | | | | |
| CVACT03Y | | | | ● | ● | | | | | | | | | | | | |
| CVCRD01Y | | | | ● | | | ● | ● | | | | | | | | | |
| CVCUS01Y | | | | ● | ● | | ● | | | | | | | | | | |
| CVTRA05Y | | | | | | | | | ● | ● | ● | ● | | | | | |
| COMEN02Y | | ● | | | | | | | | | | | | | | | |
| COADM02Y | | | ● | | | | | | | | | | | | | | |
| CSSETATY | | | | | ● | | | ● | | | | | | | | | |
| CSSTRPFY | | | | ● | ● | ● | ● | ● | | | | | | | | | |
| CSLKPCDY | | | | | ● | | | | | | | | | | | | |
| CSUTLDPY | | | | | | | | | | | ● | | ● | | | | |
| CSUTLDWY | | | | | | | | | | | ● | | ● | | | | |

### 3.2 Batch Programs → Copybooks

| Copybook | CBTRN01C | CBTRN02C | CBTRN03C | CBACT01C | CBACT04C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| CVACT01Y | | ● | | ● | ● | | | ● | ● |
| CVACT02Y | | | | | | | | ● | ● |
| CVACT03Y | | ● | ● | | ● | | | ● | ● |
| CVCUS01Y | | | | | | | | ● | ● |
| CVTRA01Y | | ● | | | ● | | | | |
| CVTRA02Y | | | | | ● | | | | |
| CVTRA03Y | | | ● | | | | | | |
| CVTRA04Y | | | ● | | | | | | |
| CVTRA05Y | ● | ● | ● | | ● | | | | |
| CVTRA06Y | | ● | | | | | | | |
| CVTRA07Y | | | ● | | | | | | |
| COSTM01 | | | | | | | ● | | |
| CODATECN | | | | ● | | | | | |
| CVEXPORT | | | | | | | | ● | ● |

### 3.3 BMS Map → Program Mapping

Each BMS map has a 1:1 relationship with its CICS program:

| BMS Map | Generated Copybook | Used By Program |
|---------|-------------------|-----------------|
| COSGN00.bms | COSGN00.CPY | COSGN00C |
| COMEN01.bms | COMEN01.CPY | COMEN01C |
| COADM01.bms | COADM01.CPY | COADM01C |
| COACTVW.bms | COACTVW.CPY | COACTVWC |
| COACTUP.bms | COACTUP.CPY | COACTUPC |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC |
| COTRN00.bms | COTRN00.CPY | COTRN00C |
| COTRN01.bms | COTRN01.CPY | COTRN01C |
| COTRN02.bms | COTRN02.CPY | COTRN02C |
| COBIL00.bms | COBIL00.CPY | COBIL00C |
| CORPT00.bms | CORPT00.CPY | CORPT00C |
| COUSR00.bms | COUSR00.CPY | COUSR00C |
| COUSR01.bms | COUSR01.CPY | COUSR01C |
| COUSR02.bms | COUSR02.CPY | COUSR02C |
| COUSR03.bms | COUSR03.CPY | COUSR03C |

---

## 4. CICS File Access Map (Online Programs)

Shows which VSAM files each online program reads, writes, or browses via `EXEC CICS` commands.

| Program | USRSEC | ACCTDATA | CARDDATA | CUSTDATA | CARDXREF | TRANSACT | DALYTRAN | TCATBAL | Operations |
|---------|--------|----------|----------|----------|----------|----------|----------|---------|------------|
| COSGN00C | R | | | | | | | | READ user for auth |
| COACTVWC | | R | R | R | R | | | | READ acct, card, cust, xref |
| COACTUPC | | RW | | RW | R | | | | READ/REWRITE acct, cust; READ xref |
| COCRDLIC | | | BR | | | | | | STARTBR/READNEXT/READPREV cards |
| COCRDSLC | | | R | R | R | | | | READ card, cust, xref |
| COCRDUPC | | | RW | | R | | | | READ/REWRITE card; READ xref |
| COTRN00C | | | | | | BR | | | STARTBR/READNEXT/READPREV trans |
| COTRN01C | | | | | | R | | | READ transaction |
| COTRN02C | | R | | | R | RW | RW | | READ acct, xref; READ/WRITE trans, daily |
| COBIL00C | | R | | | RBR | RW | | | READ/REWRITE acct; BROWSE/WRITE xref/trans |
| CORPT00C | | | | | | | | | WRITEQ TD (report trigger) |
| COUSR00C | | | | | | | | | STARTBR/READNEXT/READPREV users |
| COUSR01C | W | | | | | | | | WRITE new user |
| COUSR02C | RW | | | | | | | | READ/REWRITE user |
| COUSR03C | RD | | | | | | | | READ/DELETE user |

**Legend:** R=Read, W=Write, RW=Read+Write/Rewrite, BR=Browse, RD=Read+Delete, RBR=Read+Browse

---

## 5. Batch Job Data Lineage

### 5.1 Daily Batch Cycle — Data Flow

```
Step 1: CLOSEFIL
  └── Closes CICS files: ACCTDATA, CARDDATA, CUSTDATA, CARDXREF, TRANSACT

Step 2: Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
  └── VSAM flat files ──IDCAMS REPRO──▶ VSAM KSDS files

Step 3: POSTTRAN (CBTRN02C)
  ├── Reads:  DALYTRAN.VSAM.KSDS (daily transactions)
  ├── Reads:  CARDXREF.VSAM.KSDS (card → account lookup)
  ├── Reads:  TRANTYPE.VSAM.KSDS (transaction type validation)
  ├── Reads:  TRANCATG.VSAM.KSDS (category validation)
  ├── Writes: TRANSACT.VSAM.KSDS (master transaction file)
  ├── Writes: TCATBAL.VSAM.KSDS (category balance update)
  └── Writes: DALYREJS (rejected transactions)

Step 4: INTCALC (CBACT04C)
  ├── Reads:  TCATBAL.VSAM.KSDS (category balances)
  ├── Reads:  DISCGRP.VSAM.KSDS (interest rates)
  ├── Reads:  ACCTDATA.VSAM.KSDS (account data)
  ├── Reads:  CARDXREF.VSAM.KSDS (card-account mapping)
  └── Writes: TRANSACT.VSAM.KSDS (interest transactions)

Step 5: TRANBKP
  └── TRANSACT.VSAM.KSDS ──IDCAMS REPRO──▶ TRANSACT.BKUP

Step 6: COMBTRAN
  ├── Reads:  TRANSACT.VSAM.KSDS
  ├── Sorts:  SORT utility
  └── Writes: TRANSACT.VSAM.KSDS (combined/sorted)

Step 7: CREASTMT (CBSTM03A → CBSTM03B)
  ├── Reads:  TRANSACT ──SORT──▶ TRXFL.VSAM.KSDS (re-keyed by card)
  ├── Reads:  CARDXREF.VSAM.KSDS
  ├── Reads:  ACCTDATA.VSAM.KSDS
  ├── Reads:  CUSTDATA.VSAM.KSDS
  ├── Writes: STATEMNT.PS (plain text statements)
  └── Writes: STATEMNT.HTML (HTML statements)

Step 8: TRANREPT (CBTRN03C)
  ├── Reads:  TRANSACT ──SORT──▶ sorted sequential
  ├── Reads:  CARDXREF.VSAM.KSDS
  ├── Reads:  TRANTYPE.VSAM.KSDS
  ├── Reads:  TRANCATG.VSAM.KSDS
  ├── Reads:  Date parameter file
  └── Writes: DALYREPT (daily transaction report)

Step 9: TRANIDX
  └── Rebuilds TRANSACT alternate index (IDCAMS BLDINDEX)

Step 10: OPENFIL
  └── Reopens CICS files for online access
```

### 5.2 JCL Job → Program Execution Map

| JCL Job | Program(s) Executed | Utility Programs |
|---------|--------------------|--------------------|
| POSTTRAN | **CBTRN02C** | — |
| INTCALC | **CBACT04C** | — |
| CREASTMT | **CBSTM03A** (→ CBSTM03B) | IDCAMS, SORT, IEFBR14 |
| TRANREPT | **CBTRN03C** | SORT |
| READACCT | **CBACT01C** | IEFBR14 |
| READCARD | **CBACT02C** | — |
| READCUST | **CBCUS01C** | — |
| READXREF | **CBACT03C** | — |
| CBEXPORT | **CBEXPORT** | IDCAMS |
| CBIMPORT | **CBIMPORT** | — |
| WAITSTEP | **COBSWAIT** | — |
| ACCTFILE | — | IDCAMS |
| CARDFILE | — | IDCAMS, SDSF |
| CUSTFILE | — | IDCAMS, SDSF |
| XREFFILE | — | IDCAMS |
| TRANFILE | — | IDCAMS, SDSF |
| TRANTYPE | — | IDCAMS |
| TRANCATG | — | IDCAMS |
| TCATBALF | — | IDCAMS |
| DISCGRP | — | IDCAMS |
| DUSRSECJ | — | IEBGENER, IDCAMS |
| CLOSEFIL | — | SDSF |
| OPENFIL | — | SDSF |
| TRANBKP | — | IDCAMS |
| TRANIDX | — | IDCAMS |
| COMBTRAN | — | SORT, IDCAMS |
| DEFGDGB | — | IDCAMS |
| DEFGDGD | — | IDCAMS, IEBGENER |
| CBADMCDJ | — | DFHCSDUP |
| PRTCATBL | — | SORT, IEFBR14 |
| TXT2PDF1 | — | IKJEFT1B (TXT2PDF) |

### 5.3 JCL Job → Dataset Access Matrix

| JCL Job | Dataset(s) Read | Dataset(s) Written |
|---------|----------------|-------------------|
| DUSRSECJ | USRSEC.PS | USRSEC.VSAM.KSDS |
| ACCTFILE | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| CARDFILE | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| CUSTFILE | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| XREFFILE | CARDXREF.PS | CARDXREF.VSAM.KSDS, CARDXREF.VSAM.AIX |
| TRANFILE | TRANSACT.PS | TRANSACT.VSAM.KSDS, TRANSACT.VSAM.AIX |
| TRANTYPE | TRANTYPE.PS | TRANTYPE.VSAM.KSDS |
| TRANCATG | TRANCATG.PS | TRANCATG.VSAM.KSDS |
| TCATBALF | TCATBAL.PS | TCATBAL.VSAM.KSDS |
| DISCGRP | DISCGRP.PS | DISCGRP.VSAM.KSDS |
| POSTTRAN | DALYTRAN, CARDXREF, TRANTYPE, TRANCATG | TRANSACT, TCATBAL, DALYREJS |
| INTCALC | TCATBAL, DISCGRP, ACCTDATA, CARDXREF | TRANSACT |
| CREASTMT | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | TRXFL.VSAM.KSDS, STATEMNT.PS, STATEMNT.HTML |
| TRANREPT | TRANSACT, CARDXREF, TRANTYPE, TRANCATG | DALYREPT |
| TRANBKP | TRANSACT.VSAM.KSDS | TRANSACT.BKUP |
| COMBTRAN | TRANSACT (sorted) | TRANSACT.VSAM.KSDS |
| CBEXPORT | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF, TRANSACT | Export sequential files |
| CBIMPORT | Import sequential files | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF, TRANSACT |
| DEFGDGD | TRANTYPE.PS, TRANCATG.PS, DISCGRP.PS | GDG backups (+1 generation) |
| READACCT | ACCTDATA.VSAM.KSDS | ACCTDATA.PSCOMP, ARRYPS, VBPS |
| TXT2PDF1 | STATEMNT.PS | STATEMNT.PS.PDF |

---

## 6. Dataset Lineage Diagram

```
                     ┌─────────────────────────────────────────┐
                     │          FLAT FILE SOURCES (.PS)         │
                     │  USRSEC.PS  ACCTDATA.PS  CARDDATA.PS    │
                     │  CUSTDATA.PS  CARDXREF.PS  TRANSACT.PS  │
                     │  TRANTYPE.PS  TRANCATG.PS  DISCGRP.PS   │
                     │  TCATBAL.PS                              │
                     └───────────────────┬─────────────────────┘
                                         │ IDCAMS REPRO (init jobs)
                                         ▼
┌────────────────────────────────────────────────────────────────────┐
│                    VSAM KSDS FILES (Runtime)                       │
│                                                                    │
│  USRSEC ◀──── COSGN00C, COUSR00-03C (CICS online auth/admin)     │
│                                                                    │
│  ACCTDATA ◀── COACTVWC/COACTUPC (view/update)                    │
│           ◀── CBACT04C (interest calc), CBSTM03A (statements)     │
│           ◀── COTRN02C/COBIL00C (transaction/billing)             │
│                                                                    │
│  CARDDATA ◀── COCRDLIC/COCRDSLC/COCRDUPC (card CRUD)             │
│                                                                    │
│  CUSTDATA ◀── COACTVWC/COACTUPC (account ops)                    │
│           ◀── COCRDSLC (card view), CBSTM03A (statements)         │
│                                                                    │
│  CARDXREF ◀── Most programs (card→account resolution)             │
│           ◀── CBTRN02C, CBACT04C, CBSTM03A, CBTRN03C             │
│                                                                    │
│  TRANSACT ◀── COTRN00-02C (online CRUD)                          │
│           ◀── CBTRN02C (posting) → CBSTM03A/CBTRN03C (reporting) │
│           ──▶ TRANBKP (backup) → COMBTRAN (sort/merge)           │
│                                                                    │
│  DALYTRAN ──▶ CBTRN02C (consumed during posting)                  │
│                                                                    │
│  TRANTYPE ◀── CBTRN02C (validation), CBTRN03C (report lookup)    │
│  TRANCATG ◀── CBTRN02C (validation), CBTRN03C (report lookup)    │
│  TCATBAL  ◀── CBTRN02C (update), CBACT04C (interest input)       │
│  DISCGRP  ◀── CBACT04C (interest rate lookup)                     │
└────────────────────────────────────────────────────────────────────┘
                                         │
                                         ▼
┌────────────────────────────────────────────────────────────────────┐
│                      OUTPUT FILES                                  │
│                                                                    │
│  STATEMNT.PS ◀── CBSTM03A (plain text account statements)        │
│  STATEMNT.HTML ◀─ CBSTM03A (HTML account statements)             │
│  STATEMNT.PS.PDF ◀─ TXT2PDF1 (PDF conversion)                    │
│  DALYREPT ◀── CBTRN03C (daily transaction report)                 │
│  DALYREJS ◀── CBTRN02C (rejected transactions)                   │
│  TRANSACT.BKUP ◀── TRANBKP (transaction backup)                  │
│  GDG Backups ◀── DEFGDGD (type/category/disclosure backups)      │
│  Export Files ◀── CBEXPORT (all VSAM data export)                 │
└────────────────────────────────────────────────────────────────────┘
```

---

## 7. Job Scheduling Dependencies

**Source:** `app/scheduler/CardDemo.ca7` and `CardDemo.controlm`

### Batch Cycle Execution Order

```
CLOSEFIL ──▶ ACCTFILE ──▶ CARDFILE ──▶ CUSTFILE ──▶ XREFFILE ──▶ TRANFILE
                                                                      │
    ┌─────────────────────────────────────────────────────────────────┘
    ▼
POSTTRAN ──▶ INTCALC ──▶ TRANBKP ──▶ COMBTRAN ──▶ CREASTMT ──▶ TRANREPT
                                                                      │
    ┌─────────────────────────────────────────────────────────────────┘
    ▼
TRANIDX ──▶ OPENFIL
```

### Job Dependency Table

| Job | Predecessor(s) | Successor(s) | Critical Path? |
|-----|---------------|--------------|----------------|
| CLOSEFIL | (start of cycle) | ACCTFILE, CARDFILE, CUSTFILE | Yes |
| ACCTFILE | CLOSEFIL | POSTTRAN | Yes |
| CARDFILE | CLOSEFIL | POSTTRAN | Yes |
| CUSTFILE | CLOSEFIL | POSTTRAN | Yes |
| XREFFILE | CLOSEFIL | POSTTRAN | Yes |
| TRANFILE | CLOSEFIL | POSTTRAN | Yes |
| POSTTRAN | All file refreshes | INTCALC | **Yes — Critical** |
| INTCALC | POSTTRAN | TRANBKP | **Yes — Critical** |
| TRANBKP | INTCALC | COMBTRAN | Yes |
| COMBTRAN | TRANBKP | CREASTMT, TRANREPT | Yes |
| CREASTMT | COMBTRAN | TRANIDX | Yes |
| TRANREPT | COMBTRAN | TRANIDX | Yes |
| TRANIDX | CREASTMT, TRANREPT | OPENFIL | Yes |
| OPENFIL | TRANIDX | (end of cycle) | Yes |

---

## 8. Cross-Module Dependencies (Optional Modules)

### Authorization Module (IMS/DB2/MQ)
```
MQ Queue ──▶ COPAUA0C (trigger) ──▶ IMS DB (auth records)
COPAUS0C ──▶ IMS DB (summary view)
COPAUS1C ──▶ IMS DB (detail view)
COPAUS2C ──▶ DB2 (fraud marking)
CBPAUP0C ──▶ DB2 (purge old records)
```

### Transaction Type DB2 Module
```
COTRTUPC ──▶ DB2 TRAN_TYPE table (add/edit)
COTRTLIC ──▶ DB2 TRAN_TYPE table (list/delete via cursor)
COBTUPDT ──▶ DB2 TRAN_TYPE table (batch update)
```

### VSAM-MQ Module
```
MQ Request (CDRD) ──▶ CODATE01 ──▶ MQ Reply (system date)
MQ Request (CDRA) ──▶ COACCT01 ──▶ ACCTDATA.VSAM.KSDS ──▶ MQ Reply
```
