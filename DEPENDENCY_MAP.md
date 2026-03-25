# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Scope:** Program call graph, CICS transfer control flow, copybook dependencies, JCL data lineage

---

## Table of Contents

1. [Online CICS Call Graph](#online-cics-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Program → Copybook Dependency Matrix](#program--copybook-dependency-matrix)
4. [Program → VSAM File Access Matrix](#program--vsam-file-access-matrix)
5. [JCL Job → Program Mapping](#jcl-job--program-mapping)
6. [JCL Job → Dataset Lineage](#jcl-job--dataset-lineage)
7. [Batch Processing Data Flow](#batch-processing-data-flow)
8. [Optional Module Dependencies](#optional-module-dependencies)

---

## Online CICS Call Graph

### Navigation Flow (XCTL — Transfer Control)

```
                              CICS Transaction CC00
                                     │
                              ┌──────┴──────┐
                              │  COSGN00C   │  Sign-on
                              │  (Login)    │
                              └──────┬──────┘
                                     │
                         ┌───────────┴───────────┐
                    Admin User                Regular User
                         │                       │
                  ┌──────┴──────┐         ┌──────┴──────┐
                  │  COADM01C   │         │  COMEN01C   │
                  │ (Admin Menu)│         │ (Main Menu) │
                  └──────┬──────┘         └──────┬──────┘
                         │                       │
              ┌──────────┤              ┌────────┼────────┬──────────┬──────────┬──────────┐
              │          │              │        │        │          │          │          │
         ┌────┴───┐ ┌───┴────┐    ┌────┴───┐ ┌─┴──────┐ │     ┌────┴───┐ ┌───┴────┐ ┌──┴───────┐
         │COUSR00C│ │COUSR01C│    │COACTVWC│ │COACTUPC│ │     │COTRN00C│ │COTRN01C│ │COTRN02C  │
         │Usr List│ │Usr Add │    │Acct Vw │ │Acct Upd│ │     │Trn List│ │Trn View│ │Trn Add   │
         └────────┘ └────────┘    └────────┘ └────────┘ │     └────────┘ └────────┘ └──────────┘
              │          │                               │
         ┌────┴───┐ ┌───┴────┐              ┌───────────┼───────────┐
         │COUSR02C│ │COUSR03C│              │           │           │
         │Usr Upd │ │Usr Del │         ┌────┴───┐ ┌────┴───┐ ┌────┴───┐
         └────────┘ └────────┘         │COCRDLIC│ │COCRDSLC│ │COCRDUPC│
                                       │Card Lst│ │Card Sel│ │Card Upd│
                                       └────────┘ └────────┘ └────────┘
                                            │           │
                                       ┌────┴───┐ ┌────┴───┐
                                       │CORPT00C│ │COBIL00C│
                                       │Reports │ │Bill Pay│
                                       └────────┘ └────────┘
```

### Detailed XCTL Transfers

| Source Program | Target Program | Condition | Mechanism |
|---|---|---|---|
| COSGN00C | COADM01C | User type = Admin | EXEC CICS XCTL PROGRAM('COADM01C') |
| COSGN00C | COMEN01C | User type = Regular | EXEC CICS XCTL PROGRAM('COMEN01C') |
| COMEN01C | *(dynamic)* | Menu option selected | EXEC CICS XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME) |
| COADM01C | *(dynamic)* | Admin option selected | EXEC CICS XCTL PROGRAM(CDEMO-ADMIN-OPT-PGMNAME) |
| COACTUPC | *(dynamic)* | Return to caller | EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COACTVWC | *(dynamic)* | Return to caller | EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COCRDLIC | COMEN01C | Return to menu | EXEC CICS XCTL PROGRAM(LIT-MENUPGM) |
| COCRDLIC | COCRDSLC/COCRDUPC | Select/Update card | EXEC CICS XCTL PROGRAM(CCARD-NEXT-PROG) |
| COCRDSLC | *(dynamic)* | Return to caller | EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COCRDUPC | *(dynamic)* | Return to caller | EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) |

### Menu Option → Program Mapping

**Main Menu (COMEN01C via COMEN02Y):**

| Option | Label | Target Program |
|---:|---|---|
| 1 | Account View | COACTVWC |
| 2 | Account Update | COACTUPC |
| 3 | Credit Card List | COCRDLIC |
| 4 | Credit Card View | COCRDSLC |
| 5 | Credit Card Update | COCRDUPC |
| 6 | Transaction List | COTRN00C |
| 7 | Transaction View | COTRN01C |
| 8 | Transaction Add | COTRN02C |
| 9 | Transaction Reports | CORPT00C |
| 10 | Bill Payment | COBIL00C |
| 11 | Pending Authorization View | COPAUS0C |

**Admin Menu (COADM01C via COADM02Y):**

| Option | Label | Target Program |
|---:|---|---|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (DB2) | COTRTLIC |
| 6 | Transaction Type Maintenance (DB2) | COTRTUPC |

### CALL Statements (Subroutine Calls)

| Caller | Callee | Purpose | Mechanism |
|---|---|---|---|
| CORPT00C | CSUTLDTC | Date validation | CALL 'CSUTLDTC' USING CSUTLDTC-DATE |
| COTRN02C | CSUTLDTC | Date validation | CALL 'CSUTLDTC' USING CSUTLDTC-DATE |
| CSUTLDTC | CEEDAYS | LE date conversion | CALL 'CEEDAYS' (Language Environment) |
| CBSTM03A | CBSTM03B | Statement file I/O | CALL 'CBSTM03B' USING WS-M03B-AREA |
| CBACT01C | COBDATFT | Date formatting | CALL 'COBDATFT' USING CODATECN-REC |
| COBSWAIT | MVSWAIT | Timer wait | CALL 'MVSWAIT' USING MVSWAIT-TIME |
| *(multiple)* | CEE3ABD | Abnormal termination | CALL 'CEE3ABD' (Language Environment) |

---

## Batch Program Call Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                    Batch Processing Chain                        │
│                                                                  │
│  CBTRN02C ─── Transaction Posting (standalone)                   │
│                                                                  │
│  CBACT04C ─── Interest Calculation (standalone)                  │
│                                                                  │
│  CBSTM03A ──► CBSTM03B  Statement Generation (caller→subroutine)│
│                                                                  │
│  CBTRN03C ─── Transaction Report (standalone)                    │
│                                                                  │
│  CBACT01C ──► COBDATFT   Account Read (calls assembler)          │
│                                                                  │
│  CBACT02C ─── Card Read (standalone)                             │
│  CBACT03C ─── XREF Read (standalone)                             │
│  CBCUS01C ─── Customer Read (standalone)                         │
│  CBTRN01C ─── Transaction Read (standalone)                      │
│                                                                  │
│  CBEXPORT ─── Data Export (standalone)                            │
│  CBIMPORT ─── Data Import (standalone)                            │
│                                                                  │
│  COBSWAIT ──► MVSWAIT    Wait Utility (calls assembler)          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Program → Copybook Dependency Matrix

### Core Online Programs

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| COCOM01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COTTL01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSDAT01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHAID | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHBMSCA | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSUSR01Y | x | x | x | x | x | x | x | x | — | — | — | — | — | x | x | x | x |
| CSMSG02Y | — | — | — | x | x | — | x | x | — | — | — | — | — | — | — | — | — |
| CVACT01Y | — | — | — | x | x | — | — | — | — | — | x | — | x | — | — | — | — |
| CVACT02Y | — | — | — | x | — | x | x | x | — | — | — | — | — | — | — | — | — |
| CVACT03Y | — | — | — | x | x | — | — | — | — | — | x | — | x | — | — | — | — |
| CVCUS01Y | — | — | — | x | x | — | x | x | — | — | — | — | — | — | — | — | — |
| CVCRD01Y | — | — | — | x | x | x | x | x | — | — | — | — | — | — | — | — | — |
| CVTRA05Y | — | — | — | — | — | — | — | — | x | x | x | x | x | — | — | — | — |
| COCOM01Y | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COMEN02Y | — | x | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| COADM02Y | — | — | x | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| CSLKPCDY | — | — | — | — | x | — | — | — | — | — | — | — | — | — | — | — | — |
| CSSETATY | — | — | — | — | x | — | — | — | — | — | — | — | — | — | — | — | — |
| CSSTRPFY | — | — | — | x | x | x | x | x | — | — | — | — | — | — | — | — | — |
| CSUTLDWY | — | — | — | — | x | — | — | — | — | — | — | — | — | — | — | — | — |
| CSUTLDPY | — | — | — | — | x | — | — | — | — | — | — | — | — | — | — | — | — |

### Batch Programs

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBEXPORT | CBIMPORT |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| CVACT01Y | x | — | — | x | — | x | x | — | x | x | x |
| CVACT02Y | — | x | — | — | — | x | — | — | — | x | x |
| CVACT03Y | — | — | x | x | — | x | x | x | x | x | x |
| CVCUS01Y | — | — | — | — | x | x | — | — | — | x | x |
| CVTRA01Y | — | — | — | x | — | — | x | — | — | — | — |
| CVTRA02Y | — | — | — | x | — | — | — | — | — | — | — |
| CVTRA03Y | — | — | — | — | — | — | — | x | — | — | — |
| CVTRA04Y | — | — | — | — | — | — | — | x | — | — | — |
| CVTRA05Y | — | — | — | x | — | x | x | x | — | x | x |
| CVTRA06Y | — | — | — | — | — | x | x | — | — | — | — |
| CVTRA07Y | — | — | — | — | — | — | — | x | — | — | — |
| CVEXPORT | — | — | — | — | — | — | — | — | — | x | x |
| COSTM01 | — | — | — | — | — | — | — | — | x | — | — |
| CUSTREC | — | — | — | — | — | — | — | — | x | — | — |
| CODATECN | x | — | — | — | — | — | — | — | — | — | — |

---

## Program → VSAM File Access Matrix

### Online Programs (CICS)

| VSAM File | COSGN00C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| USRSEC | R | — | — | — | — | — | — | — | — | — | R/B | W | RW | RD |
| ACCTDATA | — | R | RW | — | — | — | — | — | R | R | — | — | — | — |
| CARDDATA | — | R | R | R/B | R | RW | — | — | — | — | — | — | — | — |
| CARDXREF | — | R | R | — | R | — | — | — | R | R/B | — | — | — | — |
| CUSTDATA | — | R | RW | — | R | R | — | — | — | — | — | — | — | — |
| TRANSACT | — | — | — | — | — | — | R/B | R | W | W | — | — | — | — |

**Legend:** R=Read, W=Write, RW=Read/Rewrite, RD=Read/Delete, B=Browse (STARTBR/READNEXT/READPREV)

### Batch Programs (JCL)

| VSAM/Dataset | CBTRN02C | CBACT04C | CBSTM03A | CBTRN03C | CBACT01C | CBACT02C | CBACT03C | CBCUS01C | CBEXPORT | CBIMPORT |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| ACCTDATA | RW | R | R | — | R | — | — | — | R | W |
| CARDDATA | — | — | — | — | — | R | — | — | R | — |
| CARDXREF | R | R | R | R | — | — | R | — | R | W |
| CUSTDATA | — | — | R | — | — | — | — | R | R | W |
| TRANSACT | RW | — | — | R | — | — | — | — | R | W |
| DALYTRAN | R | — | — | — | — | — | — | — | — | — |
| TCATBALF | RW | R | — | — | — | — | — | — | — | — |
| DISCGRP | — | R | — | — | — | — | — | — | — | — |
| TRANTYPE | — | — | — | R | — | — | — | — | — | — |
| TRANCATG | — | — | — | R | — | — | — | — | — | — |
| DALYREJS | W | — | — | — | — | — | — | — | — | — |
| SYSTRAN | — | W | — | — | — | — | — | — | — | — |

---

## JCL Job → Program Mapping

| JCL Job | Programs Executed | Category |
|---|---|---|
| POSTTRAN.jcl | **CBTRN02C** | Core Processing |
| INTCALC.jcl | **CBACT04C** | Core Processing |
| CREASTMT.JCL | SORT, IDCAMS, **CBSTM03A** (→CBSTM03B) | Core Processing |
| TRANREPT.jcl | SORT, **CBTRN03C** | Core Processing |
| READACCT.jcl | **CBACT01C** (→COBDATFT) | Data Access |
| READCARD.jcl | **CBACT02C** | Data Access |
| READCUST.jcl | **CBCUS01C** | Data Access |
| READXREF.jcl | **CBACT03C** | Data Access |
| CBEXPORT.jcl | IDCAMS, **CBEXPORT** | Data Migration |
| CBIMPORT.jcl | **CBIMPORT** | Data Migration |
| WAITSTEP.jcl | **COBSWAIT** (→MVSWAIT) | Utility |
| CLOSEFIL.jcl | SDSF | CICS Control |
| OPENFIL.jcl | SDSF | CICS Control |
| ACCTFILE.jcl | IDCAMS | Data Load |
| CARDFILE.jcl | SDSF, IDCAMS | Data Load |
| CUSTFILE.jcl | SDSF, IDCAMS | Data Load |
| XREFFILE.jcl | IDCAMS | Data Load |
| TRANFILE.jcl | SDSF, IDCAMS | Data Load |
| DUSRSECJ.jcl | IEBGENER, IDCAMS | Data Load |
| TCATBALF.jcl | IDCAMS | Data Load |
| DISCGRP.jcl | IDCAMS | Data Load |
| TRANTYPE.jcl | IDCAMS | Data Load |
| TRANCATG.jcl | IDCAMS | Data Load |
| ESDSRRDS.jcl | IEBGENER, IDCAMS | Data Load |
| DEFCUST.jcl | IDCAMS | Infrastructure |
| COMBTRAN.jcl | SORT, IDCAMS | Archival |
| TRANBKP.jcl | IDCAMS | Archival |
| DEFGDGB.jcl | IDCAMS | Infrastructure |
| DEFGDGD.jcl | IEBGENER, IDCAMS | Infrastructure |
| TRANIDX.jcl | IDCAMS | Infrastructure |
| PRTCATBL.jcl | SORT | Reporting |
| DALYREJS.jcl | IDCAMS | Infrastructure |
| REPTFILE.jcl | IDCAMS | Infrastructure |
| CBADMCDJ.jcl | DFHCSDUP | CICS Admin |
| FTPJCL.JCL | FTP | Utility |
| INTRDRJ1.JCL | IDCAMS, IEBGENER | Utility |
| INTRDRJ2.JCL | IDCAMS | Utility |
| TXT2PDF1.JCL | TXT2PDF (IKJEFT1B) | Utility |

---

## JCL Job → Dataset Lineage

### Data Load Flow (Flat → VSAM)

```
                        Flat Files (PS)                    VSAM Files (KSDS)
                        ─────────────                      ─────────────────
DUSRSECJ:    USRSEC.PS ──────────────────────────────────► USRSEC.VSAM.KSDS
ACCTFILE:    ACCTDATA.PS ────────────────────────────────► ACCTDATA.VSAM.KSDS
CARDFILE:    CARDDATA.PS ────────────────────────────────► CARDDATA.VSAM.KSDS
CUSTFILE:    CUSTDATA.PS ────────────────────────────────► CUSTDATA.VSAM.KSDS
XREFFILE:    CARDXREF.PS ───────────────────┬────────────► CARDXREF.VSAM.KSDS
                                            └────────────► CARDXREF.VSAM.AIX (alt index)
TRANFILE:    DALYTRAN.PS.INIT ───────────────┬───────────► TRANSACT.VSAM.KSDS
                                             └───────────► TRANSACT alt index
TCATBALF:    TCATBALF.PS ───────────────────────────────► TCATBALF.VSAM.KSDS
DISCGRP:     DISCGRP.PS ───────────────────────────────► DISCGRP.VSAM.KSDS
TRANTYPE:    TRANTYPE.PS ──────────────────────────────► TRANTYPE.VSAM.KSDS
TRANCATG:    TRANCATG.PS ─────────────────────────────► TRANCATG.VSAM.KSDS
```

### Core Batch Processing Flow

```
POSTTRAN (CBTRN02C):
  INPUT:   DALYTRAN.PS ──────────────► reads daily transactions
  UPDATE:  TRANSACT.VSAM.KSDS ──────► posts to master
           ACCTDATA.VSAM.KSDS ──────► updates balances
           TCATBALF.VSAM.KSDS ──────► updates category balances
           CARDXREF.VSAM.KSDS ──────► card-to-account lookup
  OUTPUT:  DALYREJS(+1) ───────────► rejected transactions (GDG)

INTCALC (CBACT04C):
  INPUT:   ACCTDATA.VSAM.KSDS ─────► account records
           CARDXREF.VSAM.KSDS ─────► card-to-account mapping
           CARDXREF.VSAM.AIX.PATH ─► alternate index lookup
           DISCGRP.VSAM.KSDS ──────► interest rates
           TCATBALF.VSAM.KSDS ─────► category balances
  OUTPUT:  SYSTRAN(+1) ────────────► system-generated interest transactions (GDG)

TRANBKP:
  INPUT:   TRANSACT.VSAM.KSDS ─────► transaction master
  OUTPUT:  TRANSACT.BKUP(+1) ──────► backup copy (GDG)

COMBTRAN:
  INPUT:   SYSTRAN(0) ─────────────► latest interest transactions
           TRANSACT.BKUP(0) ───────► latest transaction backup
  OUTPUT:  TRANSACT.COMBINED(+1) ──► merged transactions (GDG)
           TRANSACT.VSAM.KSDS ─────► reloaded master

CREASTMT (CBSTM03A):
  INPUT:   TRANSACT.VSAM.KSDS ─────► transactions (sorted copy)
           CARDXREF.VSAM.KSDS ─────► card-to-account mapping
           ACCTDATA.VSAM.KSDS ─────► account details
           CUSTDATA.VSAM.KSDS ─────► customer names/addresses
  OUTPUT:  STATEMNT.PS ────────────► text statements
           STATEMNT.HTML ──────────► HTML statements

TRANREPT (CBTRN03C):
  INPUT:   TRANSACT.BKUP(+1) ──────► backed-up transactions
           CARDXREF.VSAM.KSDS ─────► card-to-account mapping
           TRANTYPE.VSAM.KSDS ─────► type descriptions
           TRANCATG.VSAM.KSDS ─────► category descriptions
           DATEPARM ───────────────► date range parameters
  OUTPUT:  TRANSACT.DALY(+1) ──────► daily extract (GDG)
           TRANREPT(+1) ──────────► transaction report (GDG)
```

### Export/Import Flow

```
CBEXPORT:
  INPUT:   ACCTDATA.VSAM.KSDS ─┐
           CARDDATA.VSAM.KSDS ─┤
           CARDXREF.VSAM.KSDS ─┼──► EXPORT.DATA (single combined file)
           CUSTDATA.VSAM.KSDS ─┤
           TRANSACT.VSAM.KSDS ─┘

CBIMPORT:
  INPUT:   EXPORT.DATA ────────┬──► ACCTDATA.IMPORT
                               ├──► CARDXREF.IMPORT
                               ├──► CUSTDATA.IMPORT
                               └──► TRANSACT.IMPORT
  ERROR:   IMPORT.ERRORS ─────────► rejected records
```

### Backup/GDG Flow

```
DEFGDGD:
  TRANTYPE.PS ──────► TRANTYPE.BKUP(+1)
  TRANCATG.PS ─────► TRANCATG.PS.BKUP(+1)
  DISCGRP.PS ──────► DISCGRP.BKUP(+1)

PRTCATBL:
  TCATBALF.VSAM.KSDS ──► TCATBALF.BKUP(+1)  (backup)
                        ► TCATBALF.REPT       (report)
```

---

## Batch Processing Data Flow

### End-to-End Nightly Cycle

```
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                          NIGHTLY BATCH CYCLE                                │
 │                                                                             │
 │  Phase 1: CLOSEFIL                                                          │
 │     Close CICS files for exclusive batch access                             │
 │                                                                             │
 │  Phase 2: Data Refresh                                                      │
 │     ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE                       │
 │     (Reload VSAM from flat file sources)                                    │
 │                                                                             │
 │  Phase 3: POSTTRAN (CBTRN02C)                                               │
 │     DALYTRAN.PS ──► TRANSACT + ACCTDATA + TCATBALF                          │
 │     (Post daily transactions, update balances)                              │
 │                                                                             │
 │  Phase 4: INTCALC (CBACT04C)                                                │
 │     ACCTDATA + DISCGRP + TCATBALF ──► SYSTRAN                               │
 │     (Calculate and post interest transactions)                              │
 │                                                                             │
 │  Phase 5: TRANBKP                                                           │
 │     TRANSACT.VSAM ──► TRANSACT.BKUP(+1)                                     │
 │     (Backup transaction master)                                             │
 │                                                                             │
 │  Phase 6: COMBTRAN                                                          │
 │     SYSTRAN + TRANSACT.BKUP ──► TRANSACT.COMBINED ──► TRANSACT.VSAM         │
 │     (Merge interest transactions with master)                               │
 │                                                                             │
 │  Phase 7: CREASTMT (CBSTM03A)                                               │
 │     TRANSACT + XREF + ACCT + CUST ──► STATEMNT.PS + STATEMNT.HTML           │
 │     (Generate account statements)                                           │
 │                                                                             │
 │  Phase 8: TRANIDX                                                           │
 │     Rebuild alternate indexes on TRANSACT                                   │
 │                                                                             │
 │  Phase 9: OPENFIL                                                           │
 │     Reopen CICS files for online access                                     │
 │                                                                             │
 └─────────────────────────────────────────────────────────────────────────────┘
```

---

## Optional Module Dependencies

### Authorization Module (IMS/DB2/MQ)

```
MQ Queue ──► COPAUA0C (MQ Trigger) ──► IMS DB (Auth records)
                                       DB2 (Fraud flags)
                    │
                    ▼
             COPAUS0C (Summary View) ──► COPAUS1C (Detail View)
                                        COPAUS2C (Fraud Marking → DB2)
             CBPAUP0C (Batch Purge)

IMS Utilities: PAUDBLOD (Load) ◄──► PAUDBUNL/DBUNLDGS (Unload)

Copybooks: CIPAUSMY, CIPAUDTY, CCPAURQY, CCPAURLY, CCPAUERY
MQ Copybooks: CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV
IMS Copybooks: IMSFUNCS, PAUTBPCB, PASFLPCB, PADFLPCB
```

### Transaction Type DB2 Module

```
COTRTLIC (List/Delete) ◄──► DB2 TRAN_TYPE table (cursors)
COTRTUPC (Add/Edit)    ◄──► DB2 TRAN_TYPE table (INSERT/UPDATE)
COBTUPDT (Batch Maint) ◄──► DB2 TRAN_TYPE table (batch)

JCL: CREADB21 (Create DB2 objects), TRANEXTR (Extract), MNTTRDB2 (Batch maint)
Copybooks: CSDB2RWY, CSDB2RPY
```

### VSAM-MQ Module

```
MQ Request Queue ──► CODATE01 (System Date) ──► MQ Response Queue
MQ Request Queue ──► COACCT01 (Account Inquiry) ──► ACCTDATA.VSAM + MQ Response

MQ Copybooks: CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML
Data Copybook: CVACT01Y (for COACCT01)
```
