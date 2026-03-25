# CardDemo Dependency Map

> **Source:** Static analysis of CALL, XCTL, COPY, EXEC CICS, and JCL DD/DSN statements
> **Scope:** All 31 core COBOL programs, 38 JCL jobs, and optional modules

---

## 1. Online Program Call Graph (CICS XCTL / Navigation)

The online CICS programs communicate via `EXEC CICS XCTL` (transfer control) using the COMMAREA (`COCOM01Y`) to pass context.

### 1.1 Screen Navigation Flow

```
                    ┌──────────────┐
                    │  COSGN00C    │
                    │  (Sign On)   │
                    └──────┬───────┘
                           │ XCTL (based on user type)
              ┌────────────┴────────────┐
              ▼                         ▼
     ┌────────────────┐       ┌────────────────┐
     │   COMEN01C     │       │   COADM01C     │
     │  (Main Menu)   │       │  (Admin Menu)  │
     │  Regular Users  │       │  Admin Users   │
     └───────┬────────┘       └───────┬────────┘
             │                        │
    ┌────────┼────────┐      ┌────────┼────────┐
    │        │        │      │        │        │
    ▼        ▼        ▼      ▼        ▼        ▼
 Options   Options  Options  Options  Options  Options
  1-5       6-9     10-11    1-4      5-6     (DB2)
```

### 1.2 Main Menu Options (COMEN01C &rarr; XCTL targets)

| Option | Target Program | Screen | User Type |
|--------|---------------|--------|-----------|
| 1 | COACTVWC | Account View | U (Regular) |
| 2 | COACTUPC | Account Update | U |
| 3 | COCRDLIC | Credit Card List | U |
| 4 | COCRDSLC | Credit Card View | U |
| 5 | COCRDUPC | Credit Card Update | U |
| 6 | COTRN00C | Transaction List | U |
| 7 | COTRN01C | Transaction View | U |
| 8 | COTRN02C | Transaction Add | U |
| 9 | CORPT00C | Transaction Reports | U |
| 10 | COBIL00C | Bill Payment | U |
| 11 | COPAUS0C | Pending Auth View | U (optional module) |

### 1.3 Admin Menu Options (COADM01C &rarr; XCTL targets)

| Option | Target Program | Screen |
|--------|---------------|--------|
| 1 | COUSR00C | User List |
| 2 | COUSR01C | User Add |
| 3 | COUSR02C | User Update |
| 4 | COUSR03C | User Delete |
| 5 | COTRTLIC | Transaction Type List (DB2, optional) |
| 6 | COTRTUPC | Transaction Type Maintenance (DB2, optional) |

### 1.4 Inter-Program Navigation via XCTL

All online programs return to their caller using `CDEMO-TO-PROGRAM` from the COMMAREA:

```
COSGN00C ──XCTL──► COMEN01C (user type = U)
COSGN00C ──XCTL──► COADM01C (user type = A)
COMEN01C ──XCTL──► [any menu option program]
COMEN01C ──XCTL──► COSGN00C (F3 = sign off)
COADM01C ──XCTL──► [any admin option program]
COADM01C ──XCTL──► COSGN00C (F3 = sign off)
COACTVWC ──XCTL──► COMEN01C/COADM01C (F3 = back)
COACTUPC ──XCTL──► COMEN01C/COADM01C (F3 = back)
COCRDLIC ──XCTL──► COCRDSLC (select card) or COCRDUPC (update card)
COCRDSLC ──XCTL──► COCRDLIC (F3 = back to list)
COCRDUPC ──XCTL──► COCRDLIC (F3 = back to list)
COTRN00C ──XCTL──► COTRN01C (select transaction)
COTRN02C ──XCTL──► COMEN01C (F3 = back)
CORPT00C ──XCTL──► COMEN01C (F3 = back)
COBIL00C ──XCTL──► COMEN01C (F3 = back)
COUSR00C ──XCTL──► COUSR01C/COUSR02C/COUSR03C (select user action)
COUSRxxC ──XCTL──► COADM01C (F3 = back)
```

---

## 2. Batch Program Call Graph (CALL statements)

### 2.1 Direct Program-to-Program CALL Dependencies

```
CBSTM03A ──CALL──► CBSTM03B   (13 calls - file I/O subroutine for statements)
COTRN02C ──CALL──► CSUTLDTC   (2 calls - date validation)
CORPT00C ──CALL──► CSUTLDTC   (2 calls - date validation)
CBACT01C ──CALL──► COBDATFT   (ASM - date formatting)
COBSWAIT ──CALL──► MVSWAIT    (ASM - system wait)
```

### 2.2 System/LE Service Calls

| Calling Program | Target | Purpose |
|----------------|--------|---------|
| CBTRN01C | CEE3ABD | LE abnormal termination |
| CBTRN02C | CEE3ABD | LE abnormal termination |
| CBTRN03C | CEE3ABD | LE abnormal termination |
| CBACT01C | CEE3ABD | LE abnormal termination |
| CBACT02C | CEE3ABD | LE abnormal termination |
| CBACT03C | CEE3ABD | LE abnormal termination |
| CBACT04C | CEE3ABD | LE abnormal termination |
| CBCUS01C | CEE3ABD | LE abnormal termination |
| CBSTM03A | CEE3ABD | LE abnormal termination |
| CBEXPORT | CEE3ABD | LE abnormal termination |
| CBIMPORT | CEE3ABD | LE abnormal termination |
| CSUTLDTC | CEEDAYS | LE date conversion service |

---

## 3. Copybook Dependency Matrix

Which programs include which copybooks (COPY statements):

| Copybook | Programs Using It | Count |
|----------|------------------|------:|
| **COCOM01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 16 |
| **COTTL01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 15 |
| **CSDAT01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 15 |
| **CSMSG01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 15 |
| **DFHAID** | All 17 online programs | 17 |
| **DFHBMSCA** | All 17 online programs | 17 |
| **CSUSR01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 11 |
| **CVACT01Y** | COACTVWC, COACTUPC, COTRN02C, CBACT01C, CBACT04C, CBSTM03A | 6 |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C | 5 |
| **CVACT03Y** | COACTVWC, COACTUPC, COTRN02C, CBACT03C, CBACT04C, CBSTM03A | 6 |
| **CVCUS01Y** | COACTVWC, COCRDSLC, CBCUS01C, CBSTM03A | 4 |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBTRN03C, CBACT04C | 6 |
| **CSSETATY** | COACTUPC, COCRDUPC | 2 |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC | 4 |
| **COMEN02Y** | COMEN01C | 1 |
| **COADM02Y** | COADM01C | 1 |
| **COSTM01** | CBSTM03A | 1 |

---

## 4. VSAM File Access Map (Online Programs)

Which online programs read/write which VSAM files via `EXEC CICS READ/WRITE/REWRITE/DELETE`:

| VSAM File | Read By | Write By | Rewrite By | Delete By |
|-----------|---------|----------|------------|-----------|
| **USRSEC** (User Security) | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C | COUSR03C |
| **ACCTDATA** (Accounts) | COACTVWC, COACTUPC, COBIL00C, COTRN02C | &mdash; | COACTUPC | &mdash; |
| **CARDDATA** (Cards) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | &mdash; | COCRDUPC | &mdash; |
| **CARDXREF** (Cross-Ref) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COBIL00C, COTRN02C | &mdash; | &mdash; | &mdash; |
| **CUSTDATA** (Customers) | COACTVWC, COCRDSLC | &mdash; | &mdash; | &mdash; |
| **TRANSACT** (Transactions) | COTRN00C, COTRN01C, COBIL00C | COTRN02C, COBIL00C | &mdash; | &mdash; |

---

## 5. JCL Job &rarr; Data Lineage

### 5.1 Batch Processing Cycle (Ordered)

```
Step 1: CLOSEFIL ────► Close CICS files for exclusive batch access
                        (CSQUTIL SET DSNAME CLOSED)

Step 2: Data Refresh (parallel)
         ACCTFILE ──► ACCTDATA.PS ──► ACCTDATA.VSAM.KSDS
         CARDFILE ──► CARDDATA.PS ──► CARDDATA.VSAM.KSDS
         CUSTFILE ──► CUSTDATA.PS ──► CUSTDATA.VSAM.KSDS
         XREFFILE ──► CARDXREF.PS ──► CARDXREF.VSAM.KSDS (+AIX)
         TRANFILE ──► DALYTRAN.PS.INIT ──► TRANSACT.VSAM.KSDS

Step 3: POSTTRAN ────► CBTRN02C
         Reads:  DALYTRAN.PS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS
         Writes: TRANSACT.VSAM.KSDS, TCATBALF.VSAM.KSDS, DALYREJS(+1)

Step 4: INTCALC ─────► CBACT04C
         Reads:  TCATBALF.VSAM.KSDS, CARDXREF.VSAM.KSDS (+AIX),
                 ACCTDATA.VSAM.KSDS, DISCGRP.VSAM.KSDS
         Writes: SYSTRAN(+1)

Step 5: TRANBKP ─────► SORT
         Reads:  TRANSACT.VSAM.KSDS
         Writes: TRANSACT.BKUP(+1)

Step 6: COMBTRAN ────► SORT + IDCAMS
         Reads:  TRANSACT.BKUP(0), SYSTRAN(0)
         Writes: TRANSACT.COMBINED(+1) ──► TRANSACT.VSAM.KSDS

Step 7: CREASTMT ────► CBSTM03A (calls CBSTM03B)
         Reads:  TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS,
                 ACCTDATA.VSAM.KSDS, CUSTDATA.VSAM.KSDS
         Writes: STATEMNT.PS, STATEMNT.HTML

Step 8: TRANREPT ────► CBTRN03C
         Reads:  TRANSACT.DALY(+1), CARDXREF.VSAM.KSDS,
                 TRANTYPE.VSAM.KSDS, TRANCATG.VSAM.KSDS, DATEPARM
         Writes: TRANREPT(+1)

Step 9: OPENFIL ─────► Open CICS files for online access
```

### 5.2 JCL &rarr; Program Execution Map

| JCL Job | EXEC PGM | Description |
|---------|----------|-------------|
| POSTTRAN | **CBTRN02C** | Transaction posting |
| INTCALC | **CBACT04C** | Interest calculation |
| CREASTMT | **CBSTM03A** | Statement generation |
| TRANREPT | **CBTRN03C** | Transaction daily report |
| CBEXPORT | **CBEXPORT** | Data export |
| CBIMPORT | **CBIMPORT** | Data import |
| WAITSTEP | **COBSWAIT** | Wait utility |
| READACCT | **CBACT01C** | Read account data |
| READCARD | **CBACT02C** | Read card data |
| READCUST | **CBCUS01C** | Read customer data |
| READXREF | **CBACT03C** | Read cross-reference data |
| TXT2PDF1 | IKJEFT1B (TSO) | Convert text to PDF |
| CBADMCDJ | DFHCSDUP | CICS CSD admin |

### 5.3 VSAM File &rarr; JCL Job Dependency

| VSAM Dataset | Defined By | Loaded By | Read By (Batch) | Written By (Batch) |
|-------------|-----------|-----------|-----------------|-------------------|
| ACCTDATA.VSAM.KSDS | ACCTFILE | ACCTFILE | POSTTRAN, INTCALC, CREASTMT, READACCT, CBEXPORT | POSTTRAN |
| CARDDATA.VSAM.KSDS | CARDFILE | CARDFILE | READCARD, CBEXPORT | &mdash; |
| CUSTDATA.VSAM.KSDS | CUSTFILE | CUSTFILE | CREASTMT, READCUST, CBEXPORT | &mdash; |
| CARDXREF.VSAM.KSDS | XREFFILE | XREFFILE | POSTTRAN, INTCALC, CREASTMT, TRANREPT, READXREF, CBEXPORT | &mdash; |
| TRANSACT.VSAM.KSDS | TRANFILE | TRANFILE, COMBTRAN | TRANBKP, CREASTMT, CBEXPORT | POSTTRAN, COMBTRAN |
| DALYTRAN.PS | &mdash; | TRANFILE | POSTTRAN | &mdash; |
| USRSEC.VSAM.KSDS | DUSRSECJ | DUSRSECJ | &mdash; | &mdash; |
| TCATBALF.VSAM.KSDS | TCATBALF | TCATBALF | INTCALC, PRTCATBL | POSTTRAN |
| DISCGRP.VSAM.KSDS | DISCGRP | DISCGRP | INTCALC | &mdash; |
| TRANTYPE.VSAM.KSDS | TRANTYPE | TRANTYPE | TRANREPT | &mdash; |
| TRANCATG.VSAM.KSDS | TRANCATG | TRANCATG | TRANREPT | &mdash; |

### 5.4 GDG (Generation Data Group) Files

| GDG Base | Created By | Generations Written By | Generations Read By |
|----------|-----------|----------------------|-------------------|
| TRANSACT.BKUP | DEFGDGB | TRANBKP | COMBTRAN |
| SYSTRAN | DEFGDGB | INTCALC | COMBTRAN |
| TRANSACT.COMBINED | DEFGDGB | COMBTRAN | &mdash; |
| TRANSACT.DALY | DEFGDGB | TRANREPT (SORT) | TRANREPT (CBTRN03C) |
| DALYREJS | DEFGDGB | POSTTRAN | DALYREJS |
| TRANREPT | DEFGDGB | TRANREPT | &mdash; |
| TCATBALF.BKUP | DEFGDGB | PRTCATBL | &mdash; |
| TRANTYPE.BKUP | DEFGDGD | DEFGDGD | &mdash; |
| TRANCATG.PS.BKUP | DEFGDGD | DEFGDGD | &mdash; |
| DISCGRP.BKUP | DEFGDGD | DEFGDGD | &mdash; |

---

## 6. Complete Dependency Summary Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    ONLINE (CICS)                         │
│                                                          │
│  COSGN00C ─► COMEN01C ─┬► COACTVWC ──┐                 │
│           ─► COADM01C  ├► COACTUPC ──┤                 │
│                         ├► COCRDLIC ──┤ All programs     │
│   COADM01C ─┬► COUSR00C├► COCRDSLC ──┤ share:           │
│             ├► COUSR01C├► COCRDUPC ──┤  - COCOM01Y      │
│             ├► COUSR02C├► COTRN00C ──┤  - COTTL01Y      │
│             └► COUSR03C├► COTRN01C ──┤  - CSDAT01Y      │
│                         ├► COTRN02C ──┤  - CSMSG01Y     │
│                         ├► CORPT00C ──┤  - DFHAID       │
│                         └► COBIL00C ──┘  - DFHBMSCA     │
│                                                          │
│  COTRN02C ──CALL──► CSUTLDTC ──CALL──► CEEDAYS (LE)    │
│  CORPT00C ──CALL──► CSUTLDTC                            │
│                                                          │
│  ┌──VSAM Files──────────────────────────────┐            │
│  │ USRSEC  ACCTDATA  CARDDATA  CARDXREF     │            │
│  │ CUSTDATA  TRANSACT                        │            │
│  └───────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────┘
                           │
                    CLOSEFIL / OPENFIL
                           │
┌─────────────────────────────────────────────────────────┐
│                    BATCH (JCL)                           │
│                                                          │
│  POSTTRAN ──► CBTRN02C ──► TRANSACT, DALYTRAN, XREF,   │
│                             ACCTDATA, TCATBALF, DALYREJS │
│                                                          │
│  INTCALC ───► CBACT04C ──► TCATBALF, XREF, ACCTDATA,   │
│                             DISCGRP, SYSTRAN             │
│                                                          │
│  CREASTMT ──► CBSTM03A ──CALL──► CBSTM03B              │
│               reads: TRXFL, XREF, ACCTDATA, CUSTDATA     │
│               writes: STATEMNT.PS, STATEMNT.HTML         │
│                                                          │
│  TRANREPT ──► CBTRN03C ──► DALY, XREF, TRANTYPE,       │
│                             TRANCATG, TRANREPT           │
│                                                          │
│  CBEXPORT ──► CBEXPORT ──► all VSAM → EXPORT.DATA      │
│  CBIMPORT ──► CBIMPORT ──► EXPORT.DATA → split files    │
│                                                          │
│  COBSWAIT ──CALL──► MVSWAIT (ASM)                       │
│  CBACT01C ──CALL──► COBDATFT (ASM)                      │
└─────────────────────────────────────────────────────────┘
```
