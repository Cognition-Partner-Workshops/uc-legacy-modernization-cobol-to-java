# Dependency Map — CardDemo COBOL Estate

> Inter-program call graph, dataset lineage, and end-to-end batch pipeline flow.

---

## Table of Contents

1. [Inter-Program Call Graph](#1-inter-program-call-graph)
2. [CICS Program Transfer Map](#2-cics-program-transfer-map)
3. [Dataset Lineage — Files and Programs](#3-dataset-lineage--files-and-programs)
4. [JCL Job → Dataset → Program Map](#4-jcl-job--dataset--program-map)
5. [End-to-End Batch Pipeline Flow](#5-end-to-end-batch-pipeline-flow)
6. [Copybook Dependency Matrix](#6-copybook-dependency-matrix)

---

## 1. Inter-Program Call Graph

### Direct CALL Dependencies

```
CBACT01C ──CALL──► COBDATFT (assembler date formatting)
                   CEE3ABD  (LE abend routine)

CBACT02C ──CALL──► CEE3ABD

CBACT03C ──CALL──► CEE3ABD

CBACT04C ──CALL──► CEE3ABD

CBCUS01C ──CALL──► CEE3ABD

CBEXPORT ──CALL──► CEE3ABD

CBIMPORT ──CALL──► CEE3ABD

CBTRN01C ──CALL──► CEE3ABD

CBTRN02C ──CALL──► CEE3ABD

CBTRN03C ──CALL──► CEE3ABD

CBSTM03A ──CALL──► CBSTM03B (file I/O subroutine)
                   CEE3ABD

COBSWAIT ──CALL──► MVSWAIT  (assembler wait routine)

CSUTLDTC ──CALL──► CEEDAYS  (LE date validation API)

COTRN02C ──CALL──► CSUTLDTC (date validation utility)

CORPT00C ──CALL──► CSUTLDTC (date validation utility)

COCRDLIC ──CALL──► CSUTLDTC (date validation)
                   COBDATFT (date formatting)
                   CEE3ABD
```

### Call Graph Visualization (Batch Programs)

```
                    ┌──────────────────┐
                    │    CBSTM03A      │  Statement Generator
                    │  (main driver)   │
                    └────────┬─────────┘
                             │ CALL
                             ▼
                    ┌──────────────────┐
                    │    CBSTM03B      │  File I/O Handler
                    │  (subroutine)    │──► TRNXFILE, XREFFILE,
                    └──────────────────┘    CUSTFILE, ACCTFILE

    ┌─────────────┐        ┌─────────────┐        ┌─────────────┐
    │  CBACT01C   │        │  COTRN02C   │        │  CORPT00C   │
    │ Acct Reader │        │ Tran Add    │        │ Report Gen  │
    └──────┬──────┘        └──────┬──────┘        └──────┬──────┘
           │ CALL                 │ CALL                  │ CALL
           ▼                      ▼                       ▼
    ┌─────────────┐        ┌─────────────┐        ┌─────────────┐
    │  COBDATFT   │        │  CSUTLDTC   │        │  CSUTLDTC   │
    │ (assembler) │        │ (date util) │        │ (date util) │
    └─────────────┘        └──────┬──────┘        └─────────────┘
                                  │ CALL
                                  ▼
                           ┌─────────────┐
                           │   CEEDAYS   │
                           │  (LE API)   │
                           └─────────────┘
```

### Call Graph Visualization (Online CICS Programs)

```
                         ┌──────────────┐
                         │  COSGN00C    │  Sign-On Screen
                         │  (entry)     │
                         └──────┬───────┘
                                │ XCTL
                                ▼
         ┌──────────────────────────────────────────┐
         │              COMEN01C                     │  Main Menu
         │         (navigation hub)                  │
         └──┬───────┬───────┬───────┬───────┬───────┘
            │       │       │       │       │
         XCTL    XCTL    XCTL    XCTL    XCTL
            │       │       │       │       │
            ▼       ▼       ▼       ▼       ▼
     ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
     │COACTVWC │ │COCRDLIC │ │COTRN00C │ │CORPT00C │ │COBIL00C │
     │Acct View│ │Card List│ │Tran List│ │Reports  │ │Bill Pay │
     └────┬────┘ └────┬────┘ └────┬────┘ └─────────┘ └─────────┘
          │           │           │
       XCTL        XCTL        XCTL
          │           │           │
          ▼           ▼           ▼
     ┌─────────┐ ┌─────────┐ ┌─────────┐
     │COACTUPC │ │COCRDSLC │ │COTRN01C │
     │Acct Upd │ │Card Det │ │Tran View│
     └─────────┘ └────┬────┘ └─────────┘
                      │          │
                   XCTL        XCTL
                      │          │
                      ▼          ▼
                 ┌─────────┐ ┌─────────┐
                 │COCRDUPC │ │COTRN02C │
                 │Card Upd │ │Tran Add │──CALL──► CSUTLDTC
                 └─────────┘ └─────────┘

         ┌──────────────┐
         │  COADM01C    │  Admin Menu (from COMEN01C)
         └──┬───────┬───┘
            │       │
         XCTL    XCTL
            │       │
            ▼       ▼
     ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
     │COUSR00C │ │COUSR01C │ │COUSR02C │ │COUSR03C │
     │User List│ │User Add │ │User Upd │ │User Del │
     └─────────┘ └─────────┘ └─────────┘ └─────────┘
```

---

## 2. CICS Program Transfer Map

| From Program | Transfer Type | To Program | Condition |
|-------------|--------------|------------|-----------|
| COSGN00C | XCTL | COMEN01C | Successful sign-on |
| COMEN01C | XCTL | COACTVWC | Menu option: Account View |
| COMEN01C | XCTL | COCRDLIC | Menu option: Card List |
| COMEN01C | XCTL | COTRN00C | Menu option: Transaction List |
| COMEN01C | XCTL | CORPT00C | Menu option: Reports |
| COMEN01C | XCTL | COBIL00C | Menu option: Bill Payment |
| COMEN01C | XCTL | COADM01C | Menu option: Administration (admin users only) |
| COADM01C | XCTL | COUSR00C | Admin option: User List |
| COADM01C | XCTL | COUSR01C | Admin option: User Add |
| COADM01C | XCTL | COUSR02C | Admin option: User Update |
| COADM01C | XCTL | COUSR03C | Admin option: User Delete |
| COACTVWC | XCTL | COACTUPC | User selects Update from Account View |
| COCRDLIC | XCTL | COCRDSLC | User selects a card from list |
| COCRDSLC | XCTL | COCRDUPC | User selects Update from Card Detail |
| COTRN00C | XCTL | COTRN01C | User selects View from Transaction List |
| COTRN00C | XCTL | COTRN02C | User selects Add from Transaction List |
| Any program | XCTL | COSGN00C | PF3 from main menu / session timeout |
| Any program | RETURN | COMEN01C | PF3 back to menu |

---

## 3. Dataset Lineage — Files and Programs

### VSAM KSDS Files

| Dataset Name | DD Name | Programs That READ | Programs That WRITE/REWRITE | Key |
|-------------|---------|-------------------|---------------------------|-----|
| `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | ACCTDAT | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBSTM03B, CBEXPORT | COACTUPC, COBIL00C, CBIMPORT | ACCT-ID (11) |
| `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | CARDDAT | COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBEXPORT | COCRDUPC, CBIMPORT | CARD-NUM (16) |
| `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | CUSTFILE | CBCUS01C, CBSTM03B, CBEXPORT | CBIMPORT | CUST-ID (9) |
| `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | CCXREF | COTRN02C, COCRDLIC, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT | CBIMPORT | XREF-CARD-NUM (16) |
| `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH` | CXACAIX | COBIL00C, COTRN02C, COACTUPC, COACTVWC | — | Alt index on ACCT-ID (11) |
| `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | TRANSACT | COTRN00C, COTRN01C, COBIL00C | COTRN02C, COBIL00C | TRAN-CARD+ID (32) |
| `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | TRANTYPE | CBTRN03C | — | TRAN-TYPE (2) |
| `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | TRANCATG | CBTRN03C | — | TYPE-CD+CAT-CD (6) |
| `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | TCATBAL | CBACT04C, CBTRN02C | CBTRN02C | ACCT+TYPE+CAT (17) |
| `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | DISCGRP | CBACT04C | — | GROUP+TYPE+CAT (16) |
| `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | USRSEC | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C | SEC-USR-ID (8) |

### Flat (PS) and GDG Files

| Dataset Name | Programs That READ | Programs That WRITE | JCL Jobs |
|-------------|-------------------|-------------------|----------|
| `AWS.M2.CARDDEMO.DALYTRAN.PS` | CBTRN01C, CBTRN02C | _(external feed)_ | POSTTRAN |
| `AWS.M2.CARDDEMO.TRANSACT.DALY(+n)` | CBTRN03C | POSTTRAN (SORT) | TRANREPT |
| `AWS.M2.CARDDEMO.TRANSACT.BKUP(+n)` | TRANREPT (SORT) | TRANBKP (IEBGENER) | TRANBKP, TRANREPT |
| `AWS.M2.CARDDEMO.TRANREPT(+n)` | _(report output)_ | CBTRN03C | TRANREPT |
| `AWS.M2.CARDDEMO.STATEMNT.PS` | TXT2PDF1 | CBSTM03A | CREASTMT, TXT2PDF1 |
| `AWS.M2.CARDDEMO.STATEMNT.HTML` | _(browser)_ | CBSTM03A | CREASTMT |
| `AWS.M2.CARDDEMO.EXPORT.DATA.PS` | CBIMPORT | CBEXPORT | CBEXPORT, CBIMPORT |
| `AWS.M2.CARDDEMO.*.PS` (flat seeds) | IDCAMS REPRO | _(initial load)_ | ACCTFILE, CARDFILE, CUSTFILE, etc. |
| `AWS.M2.CARDDEMO.TRANTYPE.PS` | TRANTYPE JCL | TRANEXTR (DB2 extract) | TRANTYPE, TRANEXTR |
| `AWS.M2.CARDDEMO.TRANCATG.PS` | TRANCATG JCL | TRANEXTR (DB2 extract) | TRANCATG, TRANEXTR |
| `AWS.M2.CARDDEMO.DATEPARM` | CBTRN03C | _(manual/config)_ | TRANREPT |

### DB2 Tables (Transaction Type DB2 Sub-App)

| Table Name | Programs That SELECT | Programs That INSERT/UPDATE/DELETE |
|-----------|---------------------|----------------------------------|
| `CARDDEMO.TRANSACTION_TYPE` | COTRTLIC, TRANEXTR (DSNTIAUL) | COTRTUPC, COBTUPDT, CREADB21 (initial load) |
| `CARDDEMO.TRANSACTION_TYPE_CATEGORY` | COTRTLIC, TRANEXTR (DSNTIAUL) | COTRTUPC, CREADB21 (initial load) |

### DB2 Tables (Authorization Sub-App)

| Table Name | Programs That SELECT | Programs That INSERT/UPDATE/DELETE |
|-----------|---------------------|----------------------------------|
| `AUTHFRDS` (Authorization/Fraud) | COPAUA0C, COPAUS0C, COPAUS1C | COPAUA0C |

### IMS Databases (Authorization Sub-App)

| Database | DBD Name | Programs That READ | Programs That WRITE |
|----------|---------|-------------------|-------------------|
| Payment Authorization Primary | DBPAUTP0 | PAUDBUNL | PAUDBLOD |
| Payment Authorization Index | DBPAUTX0 | DBUNLDGS | — |

---

## 4. JCL Job → Dataset → Program Map

### Daily Batch Processing Chain

```
POSTTRAN.jcl
├── STEP10S: SORT
│   ├── IN:  AWS.M2.CARDDEMO.DALYTRAN.PS
│   └── OUT: AWS.M2.CARDDEMO.DALYTRAN.PS (sorted)
├── STEP10: CBTRN01C
│   ├── IN:  DALYTRAN (sorted), CARDXREF, TRANTYPE, TRANCATG
│   └── OUT: TRANSACT (validated transactions)
└── STEP20: CBTRN02C
    ├── IN:  DALYTRAN, CARDXREF, ACCTDATA, TCATBALF
    └── OUT: TRANSACT (posted), ACCTDATA (updated balances), TCATBALF (updated)
```

```
TRANREPT.jcl
├── STEP10S: SORT
│   ├── IN:  AWS.M2.CARDDEMO.TRANSACT.BKUP(+1) (from GDG)
│   └── OUT: AWS.M2.CARDDEMO.TRANSACT.DALY(+1) (filtered by date)
└── STEP10R: CBTRN03C
    ├── IN:  TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
    └── OUT: TRANREPT (formatted report)
```

```
INTCALC.jcl
└── STEP10: CBACT04C
    ├── IN:  TCATBALF, CARDXREF, DISCGRP, ACCTDATA
    └── OUT: TRANSACT (interest transactions)
```

```
CREASTMT.JCL
└── STEP01: CBSTM03A → CALL CBSTM03B
    ├── IN:  TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE
    └── OUT: STMTFILE (text statements), HTMLFILE (HTML statements)
```

```
TRANBKP.jcl
└── STEP10: IEBGENER
    ├── IN:  AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (via REPRO)
    └── OUT: AWS.M2.CARDDEMO.TRANSACT.BKUP(+1)
```

### Data Setup Chain

```
ACCTFILE.jcl:  ACCDATA.PS ──REPRO──► ACCTDATA.VSAM.KSDS
CARDFILE.jcl:  CARDDATA.PS ──REPRO──► CARDDATA.VSAM.KSDS
CUSTFILE.jcl:  CUSTDATA.PS ──REPRO──► CUSTDATA.VSAM.KSDS
XREFFILE.jcl:  CARDXREF.PS ──REPRO──► CARDXREF.VSAM.KSDS ──BLDINDEX──► CARDXREF.VSAM.AIX
TRANFILE.jcl:  TRANSACT.PS ──REPRO──► TRANSACT.VSAM.KSDS
TRANTYPE.jcl:  TRANTYPE.PS ──REPRO──► TRANTYPE.VSAM.KSDS
TRANCATG.jcl:  TRANCATG.PS ──REPRO──► TRANCATG.VSAM.KSDS
TCATBALF.jcl:  TCATBALF.PS ──REPRO──► TCATBALF.VSAM.KSDS
DISCGRP.jcl:   DISCGRP.PS  ──REPRO──► DISCGRP.VSAM.KSDS
DUSRSECJ.jcl:  USRSEC.PS   ──REPRO──► USRSEC.VSAM.KSDS
```

### DB2 Reference Data Refresh

```
TRANEXTR.jcl
├── STEP10: IEBGENER  (backup TRANTYPE.PS to GDG)
├── STEP20: IEBGENER  (backup TRANCATG.PS to GDG)
├── STEP30: IEFBR14   (delete old flat files)
├── STEP40: DSNTIAUL  (extract TRANSACTION_TYPE → TRANTYPE.PS)
└── STEP50: DSNTIAUL  (extract TRANSACTION_TYPE_CATEGORY → TRANCATG.PS)

Then: TRANTYPE.jcl / TRANCATG.jcl reload VSAM from refreshed PS files
```

---

## 5. End-to-End Batch Pipeline Flow

### Daily Processing Cycle

```
                        ┌─────────────────────────┐
                        │  External Transaction    │
                        │  Feed (DALYTRAN.PS)      │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                   ┌────│  POSTTRAN.jcl            │
                   │    │  (Daily Transaction Post)│
                   │    └────────────┬────────────┘
                   │                 │
                   │    STEP10S: SORT by card+timestamp
                   │                 │
                   │    STEP10: CBTRN01C ──► Validate & enrich
                   │         reads: CARDXREF, TRANTYPE, TRANCATG
                   │         writes: TRANSACT (validated)
                   │                 │
                   │    STEP20: CBTRN02C ──► Post to accounts
                   │         reads/updates: ACCTDATA, TCATBALF
                   │         writes: TRANSACT (final)
                   │                 │
                   │                 ▼
                   │    ┌─────────────────────────┐
                   │    │  TRANBKP.jcl             │
                   │    │  Backup transactions     │
                   │    │  → TRANSACT.BKUP(+1)     │
                   │    └────────────┬────────────┘
                   │                 │
                   │                 ▼
                   │    ┌─────────────────────────┐
                   │    │  TRANREPT.jcl            │
                   │    │  Daily Transaction Report│
                   │    │  SORT + CBTRN03C         │
                   │    │  → TRANREPT(+1)          │
                   │    └─────────────────────────┘
                   │
                   │    (Parallel / Independent)
                   │
                   │    ┌─────────────────────────┐
                   ├───►│  DALYREJS.jcl            │
                   │    │  Daily Rejection Report  │
                   │    └─────────────────────────┘
                   │
                   │    ┌─────────────────────────┐
                   └───►│  COMBTRAN.jcl            │
                        │  Combine daily → master  │
                        └─────────────────────────┘
```

### Monthly Processing Cycle

```
                        ┌─────────────────────────┐
                        │  INTCALC.jcl             │
                        │  Monthly Interest Calc   │
                        │  CBACT04C                │
                        │  reads: TCATBALF, XREF,  │
                        │         DISCGRP, ACCTDATA│
                        │  writes: TRANSACT (int.) │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │  CREASTMT.JCL            │
                        │  Account Statements      │
                        │  CBSTM03A → CBSTM03B     │
                        │  reads: TRNXFILE, XREF,  │
                        │         CUSTFILE, ACCTFILE│
                        │  writes: STMTFILE, HTML  │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │  TXT2PDF1.JCL            │
                        │  Convert text → PDF      │
                        │  reads: STATEMNT.PS      │
                        │  writes: STATEMNT.PS.PDF │
                        └─────────────────────────┘
```

### DB2 Reference Data Refresh (Periodic)

```
                        ┌─────────────────────────┐
                        │  Online: COTRTUPC        │
                        │  (CICS DB2 updates)      │
                        │  or                      │
                        │  Batch: MNTTRDB2.jcl     │
                        │  (COBTUPDT)              │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │  TRANEXTR.jcl            │
                        │  Extract DB2 → flat files│
                        │  DSNTIAUL                │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │  TRANTYPE.jcl            │
                        │  TRANCATG.jcl            │
                        │  Reload VSAM from flat   │
                        └─────────────────────────┘
```

### Data Export / Import (Ad-Hoc)

```
    CBEXPORT.jcl                    CBIMPORT.jcl
    ┌────────────┐                  ┌────────────┐
    │  CBEXPORT  │                  │  CBIMPORT  │
    │  Read all  │──► EXPORT.DATA ──►│  Split &   │
    │  VSAM files│    .PS           │  load files│
    └────────────┘                  └────────────┘
```

---

## 6. Copybook Dependency Matrix

### Which Programs Use Which Copybooks

| Copybook | Used By (Program Count) | Programs |
|----------|------------------------|----------|
| **COCOM01Y** | 20 | All online CICS programs |
| **COTTL01Y** | 17 | All online CICS programs |
| **CSDAT01Y** | 17 | All online CICS programs + CBTRN01C |
| **CSMSG01Y** | 17 | All online CICS programs |
| **DFHAID** | 17 | All online CICS programs |
| **DFHBMSCA** | 17 | All online CICS programs |
| **CVACT01Y** | 12 | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT02C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, COCRDSLC, COCRDUPC |
| **CVACT03Y** | 12 | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVTRA05Y** | 9 | COBIL00C, COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| **CSUSR01Y** | 6 | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C |
| **CVCRD01Y** | 6 | COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, CBEXPORT, CBIMPORT |
| **CVCUS01Y** | 4 | CBCUS01C, CBEXPORT, CBIMPORT, (+ CUSTREC in CBSTM03A) |
| **CSMSG02Y** | 6 | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN02C |
| **CVTRA01Y** | 2 | CBACT04C, CBTRN02C |
| **CVTRA02Y** | 1 | CBACT04C |
| **CVTRA03Y** | 2 | CBTRN03C, CBTRN01C |
| **CVTRA04Y** | 2 | CBTRN03C, CBTRN01C |
| **CVTRA06Y** | 3 | CBTRN01C, CBTRN02C, CBTRN03C |
| **CVTRA07Y** | 1 | CBTRN03C |
| **CVEXPORT** | 2 | CBEXPORT, CBIMPORT |
| **CODATECN** | 1 | CBACT01C |
| **COSTM01** | 1 | CBSTM03A |
| **CUSTREC** | 1 | CBSTM03A |
| **CSSETATY** | 1 | COSGN00C |
| **CSSTRPFY** | 1 | (included but minimal usage) |
| **CSLKPCDY** | 1 | COCRDLIC |
| **CSUTLDPY** | 1 | CORPT00C |
| **CSUTLDWY** | 1 | CSUTLDTC |
| **COADM02Y** | 1 | COADM01C |
| **COMEN02Y** | 1 | COMEN01C |
| **UNUSED1Y** | 0 | Not referenced by any program |
