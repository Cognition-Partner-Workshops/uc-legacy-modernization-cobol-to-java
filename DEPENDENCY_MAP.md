# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Source:** Static analysis of CALL, EXEC CICS XCTL/LINK, COPY, SELECT ASSIGN, and JCL DD statements

---

## Table of Contents

1. [Online Program Call Graph](#online-program-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [JCL Job → Program Mapping](#jcl-job--program-mapping)
4. [Copybook Dependency Matrix](#copybook-dependency-matrix)
5. [VSAM File Access Matrix](#vsam-file-access-matrix)
6. [Data Lineage — End-to-End Flows](#data-lineage--end-to-end-flows)
7. [Batch Job Orchestration](#batch-job-orchestration)

---

## Online Program Call Graph

Online CICS programs navigate between each other using `EXEC CICS XCTL` (transfer control) and pass data via the COMMAREA (COCOM01Y).

```
                            ┌──────────────┐
                            │  COSGN00C    │
                            │  (Sign On)   │
                            └──────┬───────┘
                                   │ XCTL
                      ┌────────────┴────────────┐
                      ▼                         ▼
              ┌──────────────┐          ┌──────────────┐
              │  COMEN01C    │          │  COADM01C    │
              │  (User Menu) │          │ (Admin Menu) │
              └──────┬───────┘          └──────┬───────┘
                     │ XCTL                    │ XCTL
     ┌───────┬───────┼───────┬────────┐   ┌───┴───────────┐
     ▼       ▼       ▼       ▼        ▼   ▼               ▼
 COACTVWC COACTUPC COCRDLIC COTRN00C ...  COUSR00C    COUSR01C
 (AcctVw) (AcctUp) (CrdLst) (TrnLst)     (UsrLst)   (UsrAdd)
              │         │                              COUSR02C
              │         ├──► COCRDSLC (Card View)      (UsrUpd)
              │         └──► COCRDUPC (Card Update)    COUSR03C
              │                                        (UsrDel)
              └──► COBIL00C (Bill Pay)
                   CORPT00C (Reports)
                   COTRN01C (Tran View)
                   COTRN02C (Tran Add)
```

### Detailed XCTL Transfers

| Source Program | Target Program | Trigger | Direction |
|---|---|---|---|
| COSGN00C | COMEN01C | Successful login (User type = 'U') | Forward |
| COSGN00C | COADM01C | Successful login (User type = 'A') | Forward |
| COMEN01C | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | Menu option selection | Forward |
| COADM01C | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | Admin menu selection | Forward |
| COACTVWC | COMEN01C | F3 (Back to menu) | Back |
| COACTUPC | COMEN01C | F3 (Back to menu) | Back |
| COCRDLIC | COMEN01C | F3 (Back to menu) | Back |
| COCRDLIC | COCRDSLC | Select card for viewing | Forward |
| COCRDLIC | COCRDUPC | Select card for updating | Forward |
| COCRDSLC | COCRDLIC | F3 (Back to list) | Back |
| COCRDUPC | COCRDLIC | F3 (Back to list) | Back |

### Subroutine Calls (CALL) from Online Programs

| Caller | Callee | Purpose |
|---|---|---|
| CORPT00C | CSUTLDTC | Validate report date range |
| COTRN02C | CSUTLDTC | Validate transaction date |
| CSUTLDTC | CEEDAYS (LE) | Convert date to Lillian format |

---

## Batch Program Call Graph

Batch programs are invoked by JCL jobs and use `CALL` for subroutine invocation.

```
┌─────────────────────────────────────────────────────┐
│                  JCL BATCH JOBS                     │
├─────────────┬──────────────┬────────────────────────┤
│  POSTTRAN   │   INTCALC    │      CREASTMT          │
│  (posting)  │  (interest)  │    (statements)        │
└──────┬──────┘──────┬───────┘────────┬───────────────┘
       │             │                │
       ▼             ▼                ▼
   CBTRN02C      CBACT04C        CBSTM03A ──CALL──► CBSTM03B
   (post txn)    (calc int)      (gen stmt)          (file I/O)
       │             │
       │             ▼
       │         CEE3ABD (LE abort)
       ▼
   CEE3ABD (LE abort)

┌──────────────┬──────────────┬──────────────┐
│   TRANREPT   │   READACCT   │   READCARD   │
│  (txn rpt)   │  (acct exp)  │  (card exp)  │
└──────┬───────┘──────┬───────┘──────┬───────┘
       │              │              │
       ▼              ▼              ▼
   CBTRN03C       CBACT01C      CBACT02C
   (rpt gen)      (acct read)   (card read)
       │              │
       ▼              ▼
   CEE3ABD        COBDATFT (date fmt ASM)
                  CEE3ABD (LE abort)
```

### Detailed Batch CALL Graph

| Caller | Callee | Mechanism | Purpose |
|---|---|---|---|
| CBSTM03A | CBSTM03B | `CALL 'CBSTM03B'` | Delegate file I/O for statement generation (13 calls) |
| CBACT01C | COBDATFT | `CALL 'COBDATFT'` | Format dates in account export |
| CBACT01C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination (error handling) |
| CBACT02C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBACT03C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBACT04C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBCUS01C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBTRN01C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBTRN02C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBTRN03C | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| CBSTM03A | CEE3ABD | `CALL 'CEE3ABD'` | Abnormal termination |
| COBSWAIT | MVSWAIT | `CALL 'MVSWAIT'` | Assembler wait routine |
| CSUTLDTC | CEEDAYS | `CALL 'CEEDAYS'` | LE date conversion API |

---

## JCL Job → Program Mapping

### Jobs That Execute COBOL Programs

| JCL Job | COBOL Program | Key Input Datasets | Key Output Datasets |
|---|---|---|---|
| POSTTRAN | CBTRN02C | DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM | TRANSACT.VSAM, TCATBALF.VSAM, DALYREJS GDG |
| INTCALC | CBACT04C | TCATBALF.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, DISCGRP.VSAM | SYSTRAN GDG |
| TRANREPT | CBTRN03C (+ SORT) | TRANSACT.VSAM, CARDXREF.VSAM, TRANTYPE.VSAM, TRANCATG.VSAM, DATEPARM | TRANREPT GDG |
| CREASTMT | CBSTM03A (+ SORT) | TRANSACT.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, CUSTDATA.VSAM | STATEMNT.PS, STATEMNT.HTML |
| READACCT | CBACT01C | ACCTDATA.VSAM | ACCTDATA.PSCOMP, .ARRYPS, .VBPS |
| READCARD | CBACT02C | CARDDATA.VSAM | (SYSOUT) |
| READCUST | CBCUS01C | CUSTDATA.VSAM | (SYSOUT) |
| READXREF | CBACT03C | CARDXREF.VSAM | (SYSOUT) |
| CBEXPORT | CBEXPORT | Multiple VSAM files | Sequential output files |
| CBIMPORT | CBIMPORT | Sequential input files | Multiple VSAM files |
| WAITSTEP | COBSWAIT | -- | -- |

### Jobs That Use System Utilities Only (No COBOL)

| JCL Job | Utility | Purpose | Datasets Affected |
|---|---|---|---|
| ACCTFILE | IDCAMS | Refresh account VSAM from flat file | ACCTDATA.VSAM.KSDS |
| CARDFILE | IDCAMS | Refresh card VSAM from flat file | CARDDATA.VSAM.KSDS |
| CUSTFILE | IDCAMS | Refresh customer VSAM from flat file | CUSTDATA.VSAM.KSDS |
| XREFFILE | IDCAMS | Define + load cross-reference + AIX | CARDXREF.VSAM.KSDS + AIX |
| TRANFILE | IDCAMS | Define + load transaction VSAM + AIX | TRANSACT.VSAM.KSDS + AIX |
| DUSRSECJ | IEBGENER + IDCAMS | Copy flat to PS, then load VSAM | USRSEC.VSAM.KSDS |
| TCATBALF | IDCAMS | Load category balance VSAM | TCATBALF.VSAM.KSDS |
| TRANTYPE | IDCAMS | Load transaction type VSAM | TRANTYPE.VSAM.KSDS |
| TRANCATG | IDCAMS | Load transaction category VSAM | TRANCATG.VSAM.KSDS |
| DISCGRP | IDCAMS | Load disclosure group VSAM | DISCGRP.VSAM.KSDS |
| CLOSEFIL | SDSF | Close CICS files for batch window | -- |
| OPENFIL | SDSF | Reopen CICS files after batch | -- |
| TRANBKP | IDCAMS | Backup transactions to GDG | TRANSACT.BKUP GDG |
| COMBTRAN | IDCAMS/SORT | Combine daily + master transactions | DALYTRAN, TRANSACT |
| TRANIDX | IDCAMS | Rebuild alternate indexes | TRANSACT AIX |
| DEFGDGB | IDCAMS | Define GDG base for backups | GDG catalog |
| DEFGDGD | IDCAMS | Define GDG base for daily data | GDG catalog |
| DALYREJS | IDCAMS | Define GDG for daily rejects | GDG catalog |
| PRTCATBL | SORT | Print/backup category balances | TCATBALF.REPT |
| TXT2PDF1 | TXT2PDF (REXX) | Convert statement text to PDF | STATEMNT.PS → .PDF |
| FTPJCL | FTP | Transfer files to/from mainframe | Various |
| INTRDRJ1 | IEBGENER | Trigger INTRDRJ2 via internal reader | JCL chain |
| INTRDRJ2 | IDCAMS | Copy FTP backup file | FTP test data |
| ESDSRRDS | IDCAMS | Define ESDS + RRDS test files | USRSEC.VSAM.ESDS/RRDS |

---

## Copybook Dependency Matrix

Shows which copybooks are included (via `COPY`) by each program.

### Legend
- **Data**: Business data structure copybooks (CV*, CS*)
- **UI**: Screen/communication copybooks (CO*, COTTL01Y, CSDAT01Y, CSMSG*)
- **System**: CICS system copybooks (DFHAID, DFHBMSCA)
- **Utility**: Helper copybooks (CSSETATY, CSSTRPFY, CSUTLDPY, etc.)

| Copybook | Type | COSGN | COMEN | COADM | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT 00 | COBIL 00 | COUSR 00 | COUSR 01 | COUSR 02 | COUSR 03 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| COCOM01Y | UI | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COTTL01Y | UI | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSDAT01Y | UI | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG01Y | UI | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSUSR01Y | Data | x | x | x | x | x | x | x | x | | | | | | x | x | x | x |
| DFHAID | Sys | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHBMSCA | Sys | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG02Y | UI | | | | x | x | | x | x | | | | | | | | | |
| CVACT01Y | Data | | | | x | x | | | | | | x | | x | | | | |
| CVACT02Y | Data | | | | x | | x | x | x | | | | | | | | | |
| CVACT03Y | Data | | | | x | x | | | | | | x | | x | | | | |
| CVCUS01Y | Data | | | | x | x | | x | x | | | | | | | | | |
| CVCRD01Y | Data | | | | | x | x | x | x | | | | | | | | | |
| CVTRA05Y | Data | | | | | | | | | x | x | x | x | x | | | | |
| COMEN02Y | UI | | x | | | | | | | | | | | | | | | |
| COADM02Y | UI | | | x | | | | | | | | | | | | | | |
| CSLKPCDY | Util | | | | | x | | | | | | | | | | | | |
| CSSETATY | Util | | | | | x | | | | | | | | | | | | |
| CSSTRPFY | Util | | | | x | x | x | x | x | | | | | | | | | |
| CSUTLDPY | Util | | | | | x | | | | | | | | | | | | |
| CSUTLDWY | Util | | | | | x | | | | | | | | | | | | |

### Batch Program Copybook Usage

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CVACT01Y | x | | | x | | | x | | x | | x | x |
| CVACT02Y | | x | | | | | | | | | x | x |
| CVACT03Y | x | | x | x | | | x | | x | | x | x |
| CVCUS01Y | | | | | x | | | | | | x | x |
| CVTRA05Y | | | | | | x | x | | | | x | x |
| CVTRA06Y | | | | | | | x | | | | | |
| CVTRA01Y | | | | x | | | x | | | | | |
| CVTRA02Y | | | | x | | | | | | | | |
| CVTRA03Y | | | | | | | | x | | | | |
| CVTRA04Y | | | | | | | | x | | | | |
| CVTRA07Y | | | | | | | | x | | | | |
| COSTM01 | | | | | | | | | x | | | |
| CUSTREC | | | | | | | | | x | | | |
| CVEXPORT | | | | | | | | | | | x | x |
| CODATECN | x | | | | | | | | | | | |

---

## VSAM File Access Matrix

Shows which programs read (R), write (W), update (U), delete (D), or browse (B) each VSAM file.

| VSAM File | COSGN | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT 00 | COBIL 00 | COUSR 00 | COUSR 01 | COUSR 02 | COUSR 03 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| USRSEC | R | | | | | | | | | | | B | W | RU | RD |
| ACCTDATA | | R | R | | | | | | R | | R,U | | | | |
| CARDDATA | | | | B | R | R | | | | | | | | | |
| CUSTDATA | | R | | | R | R | | | | | | | | | |
| CARDXREF | | R | R | | | | | | R | | B,R | | | | |
| TRANSACT | | | | | | | B | R | W | W(TD) | R,W | | | | |

| VSAM File | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A/B |
|---|---|---|---|---|---|---|---|---|---|
| ACCTDATA | R | | | R,U | | | R,U | | R |
| CARDDATA | | R | | | | | | | |
| CARDXREF | | | R | R | | | R | R | R |
| CUSTDATA | | | | | R | | | | R |
| TRANSACT | | | | | | R | R,W | R | |
| DALYTRAN | | | | | | | R | | |
| TCATBALF | | | | R,U | | | R,U | | |
| DISCGRP | | | | R | | | | | |
| TRANTYPE | | | | | | | | R | |
| TRANCATG | | | | | | | | R | |
| TRXFL | | | | | | | | | R |

**Access Legend:** R = Read, W = Write, U = Update (Rewrite), D = Delete, B = Browse (STARTBR/READNEXT/READPREV), TD = Transient Data queue write

---

## Data Lineage — End-to-End Flows

### Flow 1: Transaction Lifecycle

```
Daily Transactions (external)
        │
        ▼
   DALYTRAN.PS  ◄── Flat file input
        │
        ▼ POSTTRAN (CBTRN02C)
        │
   ┌────┴──────────────────┐
   │                       │
   ▼                       ▼
TRANSACT.VSAM          DALYREJS GDG
(posted txns)          (rejected txns)
   │
   ├──► TCATBALF.VSAM (category balances updated)
   │
   ├──► ACCTDATA.VSAM (account balance updated)
   │
   ├──────────────────────────────────────┐
   │                                      │
   ▼ INTCALC (CBACT04C)                  ▼ TRANREPT (CBTRN03C)
   │                                      │
   ▼                                      ▼
ACCTDATA.VSAM                         TRANREPT GDG
(interest applied)                    (daily report)
   │
   ▼ CREASTMT (CBSTM03A/B)
   │
   ├──► STATEMNT.PS (text statement)
   └──► STATEMNT.HTML (HTML statement)
            │
            ▼ TXT2PDF1
            │
            ▼
        STATEMNT.PS.PDF
```

### Flow 2: Data Refresh (Initial Load / Reset)

```
Flat Files (app/data/)
   │
   ├── ACCTDATA.PS  ──► ACCTFILE.jcl ──► ACCTDATA.VSAM.KSDS
   ├── CARDDATA.PS  ──► CARDFILE.jcl ──► CARDDATA.VSAM.KSDS
   ├── CUSTDATA.PS  ──► CUSTFILE.jcl ──► CUSTDATA.VSAM.KSDS
   ├── CARDXREF.PS  ──► XREFFILE.jcl ──► CARDXREF.VSAM.KSDS + AIX
   ├── USRSEC.PS    ──► DUSRSECJ.jcl ──► USRSEC.VSAM.KSDS
   ├── DALYTRAN.PS  ──► TRANFILE.jcl ──► TRANSACT.VSAM.KSDS + AIX
   ├── TCATBALF.PS  ──► TCATBALF.jcl ──► TCATBALF.VSAM.KSDS
   ├── TRANTYPE.PS  ──► TRANTYPE.jcl ──► TRANTYPE.VSAM.KSDS
   ├── TRANCATG.PS  ──► TRANCATG.jcl ──► TRANCATG.VSAM.KSDS
   └── DISCGRP.PS   ──► DISCGRP.jcl  ──► DISCGRP.VSAM.KSDS
```

### Flow 3: Online User Session

```
User Login ──► COSGN00C ──reads──► USRSEC.VSAM
                   │
            ┌──────┴──────┐
            ▼             ▼
       COMEN01C      COADM01C
       (User)        (Admin)
            │             │
            ▼             ▼
   Select Function   Select Function
            │             │
            ▼             ▼
   e.g. COTRN02C    e.g. COUSR01C
   (Add Transaction)  (Add User)
       │                  │
       ├─reads─► CARDXREF ├─writes─► USRSEC
       ├─reads─► ACCTDATA │
       └─writes► TRANSACT │
```

---

## Batch Job Orchestration

### Nightly Batch Cycle (Recommended Order)

```
Phase 1: Preparation
  ┌─────────────┐
  │  CLOSEFIL   │  Close CICS files for exclusive batch access
  └──────┬──────┘
         │
Phase 2: Data Refresh (optional, for reset/testing)
  ┌──────┴──────┐
  │  ACCTFILE   │──► Refresh account master
  │  CARDFILE   │──► Refresh card master
  │  CUSTFILE   │──► Refresh customer master
  │  XREFFILE   │──► Refresh cross-reference + AIX
  │  DUSRSECJ   │──► Refresh user security
  └──────┬──────┘
         │
Phase 3: Core Processing
  ┌──────┴──────┐
  │  POSTTRAN   │  Post daily transactions → TRANSACT
  └──────┬──────┘  (CBTRN02C: validates, posts, rejects)
         │
  ┌──────┴──────┐
  │  INTCALC    │  Calculate interest on balances
  └──────┬──────┘  (CBACT04C: reads DISCGRP rates, updates ACCTDATA)
         │
Phase 4: Backup & Maintenance
  ┌──────┴──────┐
  │  TRANBKP    │  Backup transaction file to GDG
  └──────┬──────┘
         │
  ┌──────┴──────┐
  │  COMBTRAN   │  Combine daily + master transactions
  └──────┬──────┘
         │
Phase 5: Reporting
  ┌──────┴──────┐
  │  TRANREPT   │  Generate daily transaction report
  └──────┬──────┘  (SORT + CBTRN03C)
         │
  ┌──────┴──────┐
  │  CREASTMT   │  Generate account statements (text + HTML)
  └──────┬──────┘  (SORT + CBSTM03A/B)
         │
  ┌──────┴──────┐
  │  TXT2PDF1   │  Convert text statements to PDF (optional)
  └──────┬──────┘
         │
Phase 6: Index Rebuild
  ┌──────┴──────┐
  │  TRANIDX    │  Rebuild transaction alternate indexes
  └──────┬──────┘
         │
Phase 7: Reopening
  ┌──────┴──────┐
  │  OPENFIL    │  Reopen CICS files for online access
  └─────────────┘
```

### Job Dependencies Summary

| Job | Must Run After | Must Run Before | Reason |
|---|---|---|---|
| CLOSEFIL | -- | All batch jobs | CICS files must be closed for batch |
| POSTTRAN | CLOSEFIL, Data Refresh | INTCALC | Transactions must be posted before interest calc |
| INTCALC | POSTTRAN | TRANBKP | Interest applies to latest balances |
| TRANBKP | INTCALC | COMBTRAN | Backup before combining |
| COMBTRAN | TRANBKP | CREASTMT | Combined data needed for statements |
| TRANREPT | POSTTRAN | OPENFIL | Report on posted transactions |
| CREASTMT | COMBTRAN | TXT2PDF1 | Statements need combined data |
| TRANIDX | COMBTRAN | OPENFIL | Indexes rebuilt after data changes |
| OPENFIL | All batch jobs | -- | Reopen for online CICS access |
