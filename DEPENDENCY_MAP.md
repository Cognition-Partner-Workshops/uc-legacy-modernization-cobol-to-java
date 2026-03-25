# CardDemo Dependency Map

> Call graph, data lineage, and inter-program dependencies for the CardDemo COBOL mainframe application.

---

## 1. Online CICS Program Call Graph

### Navigation Flow (XCTL — Transfer Control)

```
COSGN00C (Sign-On, CC00)
    │
    ├──► [Admin User] ──► COADM01C (Admin Menu, CA00)
    │                         ├──► COUSR00C (User List, CU00)
    │                         ├──► COUSR01C (User Add, CU01)
    │                         ├──► COUSR02C (User Update, CU02)
    │                         └──► COUSR03C (User Delete, CU03)
    │
    └──► [Regular User] ──► COMEN01C (Main Menu, CM00)
                              ├──► COACTVWC (Account View)
                              ├──► COACTUPC (Account Update)
                              ├──► COCRDLIC (Card List)
                              │       ├──► COCRDSLC (Card Detail)
                              │       └──► COCRDUPC (Card Update)
                              ├──► COTRN00C (Transaction List)
                              │       ├──► COTRN01C (Transaction View)
                              │       └──► COTRN02C (Transaction Add)
                              ├──► COBIL00C (Bill Payment)
                              └──► CORPT00C (Transaction Report)
```

### CALL Dependencies (Subroutine Calls)

| Calling Program | Called Program | Purpose | Mechanism |
|----------------|---------------|---------|-----------|
| COTRN02C | CSUTLDTC | Date validation/conversion | CALL |
| CORPT00C | CSUTLDTC | Date validation for report range | CALL |
| COBSWAIT | MVSWAIT (ASM) | Wait/sleep for specified duration | CALL |
| CBACT01C | COBDATFT (ASM) | Date formatting for account report | CALL |
| CBSTM03A | CBSTM03B | Statement file I/O operations | CALL (13 times) |
| CBSTM03A | CEE3ABD | Abnormal termination (error handling) | CALL |
| CBACT01C | CEE3ABD | Abnormal termination | CALL |
| CBACT02C | CEE3ABD | Abnormal termination | CALL |
| CBACT03C | CEE3ABD | Abnormal termination | CALL |
| CBACT04C | CEE3ABD | Abnormal termination | CALL |
| CBCUS01C | CEE3ABD | Abnormal termination | CALL |
| CBTRN01C | CEE3ABD | Abnormal termination | CALL |
| CBTRN02C | CEE3ABD | Abnormal termination | CALL |
| CBTRN03C | CEE3ABD | Abnormal termination | CALL |
| CBEXPORT | CEE3ABD | Abnormal termination | CALL |
| CBIMPORT | CEE3ABD | Abnormal termination | CALL |
| CSUTLDTC | CEEDAYS | LE date conversion API | CALL |

### CICS Resource Access (per program)

| Program | VSAM Files Accessed | Access Type | BMS Map |
|---------|-------------------|-------------|---------|
| COSGN00C | USRSEC | READ | COSGN00 |
| COMEN01C | — (navigation only) | — | COMEN01 |
| COADM01C | — (navigation only) | — | COADM01 |
| COACTVWC | ACCTFILE, CARDXREF, CUSTFILE | READ | COACTVW |
| COACTUPC | ACCTFILE, CARDXREF, CUSTFILE | READ, REWRITE | COACTUP |
| COCRDLIC | CARDFILE | READ (browse) | COCRDLI |
| COCRDSLC | CARDFILE, CUSTFILE | READ | COCRDSL |
| COCRDUPC | CARDFILE, CUSTFILE | READ, REWRITE | COCRDUP |
| COTRN00C | TRANSACT | READ (browse) | COTRN00 |
| COTRN01C | TRANSACT | READ | COTRN01 |
| COTRN02C | TRANSACT, ACCTFILE, CARDXREF | READ, WRITE | COTRN02 |
| CORPT00C | — (TDQ write only) | WRITEQ TD | CORPT00 |
| COBIL00C | ACCTFILE, CARDXREF, TRANSACT | READ, REWRITE, WRITE | COBIL00 |
| COUSR00C | USRSEC | READ (browse) | COUSR00 |
| COUSR01C | USRSEC | WRITE | COUSR01 |
| COUSR02C | USRSEC | READ, REWRITE | COUSR02 |
| COUSR03C | USRSEC | READ, DELETE | COUSR03 |

---

## 2. Batch Program Data Lineage

### File I/O per Batch Program

| Program | Input Files | Output Files | Access Pattern |
|---------|------------|-------------|----------------|
| CBACT01C | ACCTFILE (VSAM KSDS) | Print report (SYSOUT) | Sequential read, format, print |
| CBACT02C | CARDFILE (VSAM KSDS) | Print report (SYSOUT) | Sequential read, format, print |
| CBACT03C | XREFFILE (VSAM KSDS) | Print report (SYSOUT) | Sequential read, format, print |
| CBACT04C | TCATBALF, CARDXREF, DISCGRP, ACCTFILE, TRANSACT | TCATBALF (updated) | Read rates, calculate interest, update balances |
| CBCUS01C | CUSTFILE (VSAM KSDS) | Print report (SYSOUT) | Sequential read, format, print |
| CBTRN01C | DALYTRAN (sequential) | TRANSACT, CUSTFILE, CARDXREF, CARDFILE, ACCTFILE | Post daily transactions to master files |
| CBTRN02C | DALYTRAN (sequential) | TRANSACT, CARDXREF, ACCTFILE, TCATBALF | Enhanced posting with category balance tracking |
| CBTRN03C | TRANSACT, CARDXREF, TRANTYPE, TRANCATG | Print report (SYSOUT), DATEPARM | Generate transaction detail report |
| CBSTM03A | TRANSACT (re-keyed), CARDXREF, CUSTFILE, ACCTFILE | STMTFILE (text), HTMLFILE (HTML) | Generate account statements |
| CBSTM03B | (called by CBSTM03A) | STMTFILE, HTMLFILE | Write statement records |
| CBEXPORT | CUSTFILE, ACCTFILE, CARDXREF, TRANSACT, CARDFILE | EXPORTFL (multi-record) | Export all customer data |
| CBIMPORT | EXPORTFL (multi-record) | CUSTFILE, ACCTFILE, CARDXREF, TRANSACT | Import and split into target files |
| COBSWAIT | — | — | Sleep utility (no file I/O) |
| CSUTLDTC | — | — | Date conversion (no file I/O) |

---

## 3. JCL Job → Program → File Lineage

### Core Batch Cycle

```
┌─────────────┐
│  CLOSEFIL   │  Close CICS files for exclusive batch access
│  (DFHCSDUP) │
└──────┬──────┘
       ▼
┌─────────────────────────────────────────────┐
│  DATA REFRESH JOBS (parallel)               │
│  ACCTFILE  → IDCAMS → ACCTDATA.PS → VSAM   │
│  CARDFILE  → IDCAMS → CARDDATA.PS → VSAM   │
│  CUSTFILE  → IDCAMS → CUSTDATA.PS → VSAM   │
│  XREFFILE  → IDCAMS → XREFDATA.PS → VSAM   │
│  TRANFILE  → IDCAMS → TRANDATA.PS → VSAM   │
│  DUSRSECJ  → IDCAMS → USRSEC.PS → VSAM     │
└──────┬──────────────────────────────────────┘
       ▼
┌─────────────┐     ┌──────────────┐
│  POSTTRAN   │     │  Reads:      │
│  CBTRN01C   │────►│  DALYTRAN    │
│  CBTRN02C   │     │  Writes:     │
│             │     │  TRANSACT    │
│             │     │  ACCTFILE    │
│             │     │  TCATBALF    │
└──────┬──────┘     └──────────────┘
       ▼
┌─────────────┐     ┌──────────────┐
│  INTCALC    │     │  Reads:      │
│  CBACT04C   │────►│  TCATBALF    │
│             │     │  DISCGRP     │
│             │     │  ACCTFILE    │
│             │     │  Writes:     │
│             │     │  TCATBALF    │
└──────┬──────┘     └──────────────┘
       ▼
┌─────────────┐     ┌──────────────┐
│  TRANBKP    │     │  REPRO:      │
│  IDCAMS     │────►│  TRANSACT    │
│             │     │  → TRANBKP   │
└──────┬──────┘     └──────────────┘
       ▼
┌─────────────┐     ┌──────────────────┐
│  COMBTRAN   │     │  SORT + REPRO:   │
│  SORT       │────►│  DALYTRAN        │
│  IDCAMS     │     │  + TRANSACT      │
│             │     │  → Combined VSAM │
└──────┬──────┘     └──────────────────┘
       ▼
┌─────────────┐     ┌───────────────────────┐
│  CREASTMT   │     │  SORT TRANSACT        │
│  SORT       │────►│  → Sequential         │
│  IDCAMS     │     │  → Re-keyed VSAM      │
│  CBSTM03A   │     │  CBSTM03A reads:      │
│  (CBSTM03B) │     │    TRANSACT, CARDXREF │
│             │     │    CUSTFILE, ACCTFILE  │
│             │     │  Writes:              │
│             │     │    STMTFILE, HTMLFILE  │
└──────┬──────┘     └───────────────────────┘
       ▼
┌─────────────┐
│  TRANIDX    │  Build alternate index on TRANSACT
│  IDCAMS     │
└──────┬──────┘
       ▼
┌─────────────┐
│  OPENFIL    │  Re-open CICS files for online access
│  (DFHCSDUP) │
└─────────────┘
```

### Reporting Jobs (On-Demand)

```
TRANREPT ──► SORT (sort transactions)
         ──► CBTRN03C (generate detail report)
                Reads: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
                Writes: SYSOUT (printed report)
```

### Utility Jobs

```
CBEXPORT ──► CBEXPORT program
                Reads: CUSTFILE, ACCTFILE, CARDXREF, TRANSACT, CARDFILE
                Writes: EXPORTFL (multi-record export)

CBIMPORT ──► CBIMPORT program
                Reads: EXPORTFL
                Writes: CUSTFILE, ACCTFILE, CARDXREF, TRANSACT

TXT2PDF1 ──► IKJEFT1B + TXT2PDF REXX
                Reads: STMTFILE (text statements)
                Writes: PDF output
```

---

## 4. Copybook Dependency Matrix

Which programs include which copybooks (excluding license comment matches and CICS/BMS system copies).

| Copybook | Programs Using It | Usage Count |
|----------|------------------|-------------|
| **COCOM01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| **COTTL01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| **CSDAT01Y** | All 17 online CICS programs | 17 |
| **CSMSG01Y** | All 17 online CICS programs | 17 |
| **CSUSR01Y** | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 11 |
| **CVACT01Y** | COACTUPC, COACTVWC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A, COTRN02C, COBIL00C | 11 |
| **CVACT03Y** | COACTUPC, COACTVWC, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A, COTRN02C, COBIL00C | 12 |
| **CVTRA05Y** | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C | 11 |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT | 8 |
| **CVCUS01Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, CBSTM03A | 9 |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | 4 |
| **CSSETATY** | COACTUPC (30+ COPY REPLACING instances) | 1 |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | 2 |
| **CVTRA01Y** | CBACT04C, CBTRN02C | 2 |
| **CVTRA02Y** | CBACT04C | 1 |
| **CVTRA03Y** | CBTRN03C | 1 |
| **CVTRA04Y** | CBTRN03C | 1 |
| **CVTRA07Y** | CBTRN03C | 1 |
| **COSTM01** | CBSTM03A | 1 |
| **CUSTREC** | CBSTM03A | 1 |
| **CVEXPORT** | CBEXPORT, CBIMPORT | 2 |
| **CODATECN** | CBACT01C | 1 |
| **COMEN02Y** | COMEN01C | 1 |
| **COADM02Y** | COADM01C | 1 |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CSUTLDPY** | COACTUPC | 1 |
| **CSUTLDWY** | COACTUPC | 1 |
| **CSLKPCDY** | COACTUPC | 1 |

---

## 5. VSAM File Access Matrix

Which programs access which VSAM files and how.

| VSAM File | Online Programs | Batch Programs | Access Types |
|-----------|----------------|----------------|-------------|
| **USRSEC** | COSGN00C (R), COUSR00C (R/Browse), COUSR01C (W), COUSR02C (R/RW), COUSR03C (R/DEL) | — | R, W, RW, DEL, Browse |
| **ACCTFILE** | COACTVWC (R), COACTUPC (R/RW), COTRN02C (R), COBIL00C (R/RW) | CBACT01C (R), CBACT04C (R), CBTRN01C (R/W), CBTRN02C (R/W), CBEXPORT (R), CBIMPORT (W), CBSTM03A (R) | R, W, RW |
| **CARDFILE** | COCRDLIC (R/Browse), COCRDSLC (R), COCRDUPC (R/RW) | CBACT02C (R), CBTRN01C (R), CBEXPORT (R) | R, RW, Browse |
| **CUSTFILE** | COACTVWC (R), COACTUPC (R), COCRDSLC (R), COCRDUPC (R) | CBCUS01C (R), CBTRN01C (R), CBEXPORT (R), CBIMPORT (W), CBSTM03A (R) | R, W |
| **CARDXREF** | COACTVWC (R), COACTUPC (R), COTRN02C (R), COBIL00C (R) | CBACT03C (R), CBACT04C (R), CBTRN01C (R), CBTRN02C (R), CBTRN03C (R), CBEXPORT (R), CBIMPORT (W), CBSTM03A (R) | R, W |
| **TRANSACT** | COTRN00C (R/Browse), COTRN01C (R), COTRN02C (R/W), COBIL00C (R/W) | CBTRN01C (W), CBTRN02C (W), CBTRN03C (R), CBEXPORT (R), CBIMPORT (W), CBSTM03A (R) | R, W, Browse |
| **DALYTRAN** | — | CBTRN01C (R), CBTRN02C (R) | R (Sequential) |
| **TCATBALF** | — | CBACT04C (R/RW), CBTRN02C (W) | R, W, RW |
| **DISCGRP** | — | CBACT04C (R) | R |
| **TRANTYPE** | — | CBTRN03C (R) | R |
| **TRANCATG** | — | CBTRN03C (R) | R |

---

## 6. Cross-Cutting Dependencies

### Shared Infrastructure
- **COMMAREA (COCOM01Y):** All 17 online programs share data via the CICS communication area
- **DFHAID / DFHBMSCA:** All online programs use standard CICS attention identifier and BMS attribute constants
- **CEE3ABD:** All batch programs use LE abnormal termination for error handling

### Data Coupling Points
1. **Card → Account lookup:** CARDXREF links card numbers to account IDs (used by 12 programs)
2. **Transaction → Account:** Transactions reference accounts via card cross-reference chain
3. **Daily → Master posting:** DALYTRAN feeds into TRANSACT via CBTRN01C/CBTRN02C
4. **Interest calculation:** TCATBALF + DISCGRP drive CBACT04C interest computation on ACCTFILE
5. **Statement generation:** CBSTM03A joins TRANSACT + CARDXREF + CUSTFILE + ACCTFILE

### Online ↔ Batch Interface
- **TDQ (Transient Data Queue):** CORPT00C writes JCL to TDQ to trigger batch report jobs
- **Internal Reader:** INTRDRJ1 triggers INTRDRJ2 via IEBGENER to internal reader
- **CLOSEFIL/OPENFIL:** Batch cycle requires closing CICS files before exclusive VSAM access
