# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Source**: Static analysis of CALL statements, COPY directives, EXEC CICS commands, JCL EXEC PGM, and DD/DSN references.

---

## 1. Program Call Graph

### 1.1 Direct CALL Dependencies

```
CBACT01C ──CALL──► COBDATFT (ASM: date formatting)
         ──CALL──► CEE3ABD  (LE: abnormal termination)

CBACT02C ──CALL──► CEE3ABD
CBACT03C ──CALL──► CEE3ABD
CBACT04C ──CALL──► CEE3ABD
CBCUS01C ──CALL──► CEE3ABD
CBTRN01C ──CALL──► CEE3ABD
CBTRN02C ──CALL──► CEE3ABD
CBTRN03C ──CALL──► CEE3ABD

CBSTM03A ──CALL──► CBSTM03B (subroutine: file I/O for statements)
         ──CALL──► CEE3ABD

CBEXPORT ──CALL──► CEE3ABD
CBIMPORT ──CALL──► CEE3ABD

COBSWAIT ──CALL──► MVSWAIT  (ASM: low-level wait)

CSUTLDTC ──CALL──► CEEDAYS  (LE: date conversion)

CORPT00C ──CALL──► CSUTLDTC (date validation for report params)
COTRN02C ──CALL──► CSUTLDTC (date validation for new transactions)
```

### 1.2 CICS Transfer-of-Control (XCTL / RETURN TRANSID)

```
                    ┌─────────────────────────────────┐
                    │         COSGN00C (Signon)        │
                    │         Transaction: CC00        │
                    └───────────┬───────────┬──────────┘
                   Admin user   │           │   Regular user
                    ┌───────────▼───┐   ┌───▼───────────┐
                    │  COADM01C     │   │  COMEN01C     │
                    │  Admin Menu   │   │  Main Menu    │
                    └──┬──┬──┬──┬──┘   └──┬──┬──┬──┬──┬──┘
                       │  │  │  │         │  │  │  │  │
            ┌──────────┘  │  │  │    ┌────┘  │  │  │  └────────┐
            ▼             │  │  │    ▼       │  │  │           ▼
        COUSR00C          │  │  │  COACTVWC  │  │  │       COBIL00C
        User List         │  │  │  Acct View │  │  │       Bill Pay
          │               │  │  │    │       │  │  │
     ┌────┼────┐          │  │  │    ▼       │  │  │
     ▼    ▼    ▼          │  │  │  COACTUPC  │  │  │
  COUSR01C  COUSR02C      │  │  │  Acct Upd  │  │  │
  Add User  Upd User      │  │  │            │  │  │
            COUSR03C      │  │  │            │  │  │
            Del User      │  │  │            │  │  │
                          │  │  │       ┌────┘  │  └─────┐
                          │  │  │       ▼       │        ▼
                          │  │  │   COCRDLIC    │    CORPT00C
                          │  │  │   Card List   │    Reports
                          │  │  │    │    │     │
                          │  │  │    ▼    ▼     │
                          │  │  │ COCRDSLC  COCRDUPC
                          │  │  │ Card View Card Upd
                          │  │  │               │
                          │  │  └───────────────┘
                          │  │         │
                          │  │    ┌────┘
                          │  │    ▼
                          │  │  COTRN00C ───► COTRN01C (View)
                          │  │  Tran List ──► COTRN02C (Add)
                          │  │
                          │  └─► (same navigation as Regular)
                          │
                          └──► COUSR00C (also accessible from Admin)
```

### 1.3 Call Graph Summary Table

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| COSGN00C | COMEN01C | XCTL | Navigate to main menu (regular user) |
| COSGN00C | COADM01C | XCTL | Navigate to admin menu (admin user) |
| COMEN01C | COACTVWC | XCTL | Account view |
| COMEN01C | COCRDLIC | XCTL | Card listing |
| COMEN01C | COTRN00C | XCTL | Transaction listing |
| COMEN01C | COBIL00C | XCTL | Bill payment |
| COMEN01C | CORPT00C | XCTL | Report submission |
| COADM01C | COUSR00C | XCTL | User management |
| COCRDLIC | COCRDSLC | XCTL | Card detail view |
| COCRDLIC | COCRDUPC | XCTL | Card update |
| COTRN00C | COTRN01C | XCTL | Transaction detail view |
| COTRN00C | COTRN02C | XCTL | Add new transaction |
| COACTVWC | COACTUPC | XCTL | Account update |
| COUSR00C | COUSR01C | XCTL | Add user |
| COUSR00C | COUSR02C | XCTL | Update user |
| COUSR00C | COUSR03C | XCTL | Delete user |
| CORPT00C | CSUTLDTC | CALL | Date validation |
| COTRN02C | CSUTLDTC | CALL | Date validation |
| CBSTM03A | CBSTM03B | CALL | File I/O subroutine |
| CBACT01C | COBDATFT | CALL | Date formatting (ASM) |
| COBSWAIT | MVSWAIT | CALL | Wait routine (ASM) |
| CORPT00C | (TDQ→JES) | WRITEQ TD | Submit batch report job |

---

## 2. Copybook Inclusion Matrix

Shows which programs include which copybooks via COPY statements.

| Copybook | COSGN | COMEN | COADM | COACTVW | COACTUP | COCRDLI | COCRDSL | COCRDUP | COTRN00 | COTRN01 | COTRN02 | CORPT | COBIL | COUSR0-3 |
|----------|:-----:|:-----:|:-----:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-----:|:-----:|:--------:|
| COCOM01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COTTL01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSDAT01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSUSR01Y | x | - | x | x | x | x | x | x | - | - | - | - | - | x |
| DFHAID | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHBMSCA | - | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CVACT01Y | - | - | - | x | x | - | - | - | - | - | x | - | x | - |
| CVACT02Y | - | - | - | x | - | x | x | x | - | - | - | - | - | - |
| CVACT03Y | - | - | - | x | x | - | - | - | - | - | x | - | x | - |
| CVCUS01Y | - | - | - | x | x | - | x | x | - | - | - | - | - | - |
| CVTRA05Y | - | - | - | - | - | - | - | - | x | x | x | x | x | - |
| CVCRD01Y | - | - | - | x | x | x | x | x | - | - | - | - | - | - |

### Batch Program Copybook Usage

| Copybook | CBACT01 | CBACT02 | CBACT03 | CBACT04 | CBCUS01 | CBTRN01 | CBTRN02 | CBTRN03 | CBSTM03A | CBEXPORT | CBIMPORT |
|----------|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|
| CVACT01Y | x | - | - | x | - | x | x | - | x | x | x |
| CVACT02Y | - | x | - | - | - | x | - | - | - | x | x |
| CVACT03Y | - | - | x | x | - | x | x | x | x | x | x |
| CVCUS01Y | - | - | - | - | x | x | - | - | x | x | x |
| CVTRA01Y | - | - | - | x | - | - | x | - | - | - | - |
| CVTRA02Y | - | - | - | x | - | - | - | - | - | - | - |
| CVTRA03Y | - | - | - | - | - | - | - | x | - | - | - |
| CVTRA04Y | - | - | - | - | - | - | - | x | - | - | - |
| CVTRA05Y | - | - | - | x | - | x | x | x | - | x | x |
| CVTRA06Y | - | - | - | - | - | x | x | - | - | - | - |
| CVTRA07Y | - | - | - | - | - | - | - | x | - | - | - |
| CVEXPORT | - | - | - | - | - | - | - | - | - | x | x |
| COSTM01 | - | - | - | - | - | - | - | - | x | - | - |
| CUSTREC | - | - | - | - | - | - | - | - | x | - | - |
| CODATECN | x | - | - | - | - | - | - | - | - | - | - |

---

## 3. VSAM File Access Map (Online Programs)

Shows which CICS programs access which VSAM files and the access mode.

| VSAM File | COSGN | COACTVW | COACTUP | COCRDLI | COCRDSL | COCRDUP | COTRN00 | COTRN01 | COTRN02 | COBIL | COUSR0-3 |
|-----------|:-----:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-----:|:--------:|
| USRSEC | R | - | - | - | - | - | - | - | - | - | R/W/D |
| ACCTDATA | - | R | R/U | - | - | - | - | - | R | R/U | - |
| CARDDATA | - | R | R | R(browse) | R | R/U | - | - | - | - | - |
| CARDXREF | - | R | R | - | R | - | - | - | R | R(browse) | - |
| CUSTDATA | - | R | R/U | - | R | R | - | - | - | - | - |
| TRANSACT | - | - | - | - | - | - | R(browse) | R | W | R/W | - |

**Legend**: R=Read, W=Write, U=Update(Rewrite), D=Delete, browse=STARTBR/READNEXT/READPREV

---

## 4. Batch File I/O Map

Shows which batch programs read/write which files.

| Program | Input Files | Output Files | I-O (Update) Files |
|---------|-------------|-------------|-------------------|
| CBACT01C | ACCTFILE (account) | OUTFILE, ARRYFILE, VBRCFILE | - |
| CBACT02C | CARDFILE (card) | (display only) | - |
| CBACT03C | XREFFILE (xref) | (display only) | - |
| CBACT04C | TCATBALF, XREFFILE, DISCGRP | TRANSACT | ACCTFILE |
| CBCUS01C | CUSTFILE (customer) | (display only) | - |
| CBTRN01C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | - | - |
| CBTRN02C | DALYTRAN, XREFFILE | TRANSACT, DALYREJS | ACCTFILE, TCATBALF |
| CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT | - |
| CBSTM03A | (via CBSTM03B) | STMTFILE, HTMLFILE | - |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | - | - |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | - |
| CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | - |

---

## 5. JCL Job → Program → Dataset Data Lineage

### 5.1 Core Batch Cycle (Nightly Processing Order)

```
Step 1: CLOSEFIL.jcl
  └── SDSF → Close CICS files for exclusive batch access

Step 2: Data Refresh (parallel)
  ├── ACCTFILE.jcl  → IDCAMS → Delete/Define/Reload ACCTDATA.VSAM.KSDS
  ├── CARDFILE.jcl  → IDCAMS → Delete/Define/Reload CARDDATA.VSAM.KSDS
  ├── CUSTFILE.jcl  → IDCAMS → Delete/Define/Reload CUSTDATA.VSAM.KSDS
  ├── XREFFILE.jcl  → IDCAMS → Delete/Define/Reload CARDXREF.VSAM.KSDS
  └── TRANFILE.jcl  → IDCAMS → Load DALYTRAN.PS.INIT → TRANSACT.VSAM.KSDS

Step 3: POSTTRAN.jcl
  ├── SORT        → Sort DALYTRAN.PS by card+tran ID
  └── CBTRN02C    → Read: DALYTRAN (sorted), XREFFILE
                    Write: TRANSACT.VSAM.KSDS, DALYREJS
                    Update: ACCTDATA.VSAM.KSDS, TCATBALF.VSAM.KSDS

Step 4: INTCALC.jcl
  └── CBACT04C    → Read: TCATBALF, XREFFILE, DISCGRP
                    Write: TRANSACT (interest entries)
                    Update: ACCTDATA (accrue interest on balances)

Step 5: TRANBKP.jcl
  ├── SORT        → Sort TRANSACT.VSAM.KSDS
  └── IDCAMS      → REPRO → TRANSACT.BKUP(+1) GDG

Step 6: COMBTRAN.jcl
  ├── SORT        → Merge TRANSACT.BKUP(0) + SYSTRAN(0)
  └── IDCAMS      → REPRO → TRANSACT.COMBINED(+1) → TRANSACT.VSAM.KSDS

Step 7: CREASTMT.JCL
  ├── SORT        → Sort transactions for statement grouping
  ├── IDCAMS      → Load sorted data into TRXFL.VSAM.KSDS
  └── CBSTM03A    → Read: TRXFL, XREFFILE, ACCTFILE, CUSTFILE
                    Write: STATEMNT.PS (text), STATEMNT.HTML

Step 8: TRANREPT.jcl (via TRANREPT.prc)
  ├── SORT        → Sort TRANSACT.BKUP for reporting
  └── CBTRN03C    → Read: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
                    Write: TRANREPT(+1) GDG

Step 9: TRANIDX.jcl
  └── IDCAMS      → Define/rebuild alternate indexes on TRANSACT.VSAM.KSDS

Step 10: OPENFIL.jcl
  └── SDSF        → Re-open CICS files for online access
```

### 5.2 Dataset Lineage Diagram

```
                         DALYTRAN.PS.INIT
                              │
                    ┌─────────▼─────────┐
                    │   POSTTRAN.jcl    │
                    │   (CBTRN02C)      │
                    └───┬───┬───┬───┬───┘
                        │   │   │   │
     ┌──────────────────┘   │   │   └────────────────────┐
     ▼                      ▼   ▼                        ▼
 DALYREJS              TRANSACT.VSAM    ACCTDATA.VSAM   TCATBALF.VSAM
 (rejects)                  │                │               │
                            │     ┌──────────┘     ┌─────────┘
                            │     │                │
                    ┌───────▼─────▼────────────────▼───┐
                    │        INTCALC.jcl               │
                    │        (CBACT04C)                │
                    └───────┬──────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │  TRANBKP.jcl  │
                    └───────┬───────┘
                            │
                    TRANSACT.BKUP(+1)
                            │
                    ┌───────▼───────┐
                    │  COMBTRAN.jcl │──► TRANSACT.COMBINED(+1)
                    └───────┬───────┘         │
                            │                 │
                    ┌───────▼───────┐  ┌──────▼──────┐
                    │ CREASTMT.JCL  │  │ TRANREPT.jcl│
                    │ (CBSTM03A/B)  │  │ (CBTRN03C)  │
                    └───────┬───────┘  └──────┬──────┘
                            │                 │
                    ┌───────┴───────┐         │
                    ▼               ▼         ▼
              STATEMNT.PS    STATEMNT.HTML   TRANREPT(+1)
              (text stmt)    (HTML stmt)    (daily report)
```

### 5.3 Data Migration Lineage

```
Source VSAM Files:
  CUSTDATA ─────┐
  ACCTDATA ─────┤
  CARDXREF ─────┤──► CBEXPORT.jcl (CBEXPORT) ──► EXPORT.DATA
  TRANSACT ─────┤
  CARDDATA ─────┘

EXPORT.DATA ──► CBIMPORT.jcl (CBIMPORT) ──┬──► CUSTDATA.IMPORT
                                           ├──► ACCTDATA.IMPORT
                                           ├──► CARDXREF.IMPORT
                                           ├──► TRANSACT.IMPORT
                                           ├──► CARDDATA.IMPORT (implied)
                                           └──► IMPORT.ERRORS
```

---

## 6. Cross-Module Dependencies (Optional Modules)

### Authorization Module → Core

| Authorization Program | Core Dependency | Type |
|----------------------|-----------------|------|
| COPAUA0C | ACCTDATA, CARDDATA, CARDXREF | VSAM Read |
| COPAUA0C | MQ Queues (CDAU*) | MQ Put/Get |
| COPAUS0C/1C | IMS PAUTHMSG DB | IMS DL/I |
| COPAUS2C | DB2 PAUTH_FRAUD table | SQL Insert |
| CBPAUP0C | IMS PAUTHMSG DB | IMS DL/I Delete |

### Transaction Type DB2 Module → Core

| DB2 Program | Core Dependency | Type |
|-------------|-----------------|------|
| COTRTLIC | DB2 TRANTYPE table | SQL Cursor |
| COTRTUPC | DB2 TRANTYPE table | SQL Insert/Update |
| COBTUPDT | DB2 TRANTYPE table | SQL Update |

### VSAM-MQ Module → Core

| MQ Program | Core Dependency | Type |
|------------|-----------------|------|
| COACCT01 | ACCTDATA.VSAM.KSDS | VSAM Read |
| CODATE01 | System Date | CICS ASKTIME |

---

## 7. Shared Resource Contention Points

These resources are accessed by multiple programs and represent potential contention or coupling risks:

| Resource | Readers | Writers/Updaters | Risk Level |
|----------|---------|-------------------|------------|
| ACCTDATA.VSAM.KSDS | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBSTM03B, CBEXPORT | COACTUPC, COBIL00C, CBTRN02C, CBACT04C, CBIMPORT | **HIGH** — most contended file |
| TRANSACT.VSAM.KSDS | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN03C, CBEXPORT | COTRN02C, COBIL00C, CBTRN02C, CBACT04C, COMBTRAN | **HIGH** — written by online + batch |
| CARDXREF.VSAM.KSDS | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT | CBIMPORT | **MEDIUM** — heavily read, rarely written |
| CARDDATA.VSAM.KSDS | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, CBEXPORT | COCRDUPC, CBIMPORT | **MEDIUM** |
| CUSTDATA.VSAM.KSDS | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03B, CBEXPORT | COACTUPC, CBIMPORT | **MEDIUM** |
| USRSEC.VSAM.KSDS | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C | **LOW** — admin only |
| TCATBALF.VSAM.KSDS | CBACT04C | CBTRN02C, CBACT04C | **LOW** — batch only |
| COCOM01Y (COMMAREA) | All 17 online programs | All 17 online programs | **HIGH** — tight coupling via shared state |
