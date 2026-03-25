# CardDemo Dependency Map

> Complete call graph, data lineage, and dependency analysis showing which programs call which, which copybooks are included where, and which JCL jobs read/write which datasets.

---

## Table of Contents

1. [Online Program Call Graph (CICS XCTL/LINK)](#online-program-call-graph)
2. [Batch Program Call Graph (CALL)](#batch-program-call-graph)
3. [Copybook Inclusion Matrix](#copybook-inclusion-matrix)
4. [JCL Job-to-Program Mapping](#jcl-job-to-program-mapping)
5. [VSAM Dataset Lineage](#vsam-dataset-lineage)
6. [Data Flow Diagrams](#data-flow-diagrams)
7. [BMS Map-to-Program Mapping](#bms-map-to-program-mapping)
8. [Cross-Cutting Concerns](#cross-cutting-concerns)

---

## Online Program Call Graph

All online CICS programs communicate via **EXEC CICS XCTL** (transfer control) using the COMMAREA (COCOM01Y) to pass context. The sign-on program is the entry point; menus dispatch to functional programs.

### Navigation Flow (XCTL)

```
                            ┌─────────────┐
                            │  COSGN00C   │
                            │  (Sign On)  │
                            │  Trans: CC00│
                            └──────┬──────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
              ┌─────▼─────┐                ┌──────▼─────┐
              │ COMEN01C  │                │ COADM01C   │
              │ Main Menu │                │ Admin Menu │
              │ (Regular) │                │ (Admin)    │
              │ Trans:CM00│                │ Trans:CA00 │
              └─────┬─────┘                └──────┬─────┘
                    │                             │
     ┌──────┬──────┼──────┬──────┬──────┐  ┌─────┼──────┬──────┬──────┐
     │      │      │      │      │      │  │     │      │      │      │
     ▼      ▼      ▼      ▼      ▼      ▼  ▼     ▼      ▼      ▼      │
  COACTVWC COACTUPC COCRDLIC COBIL00C COTRN00C CORPT00C COUSR00C COUSR01C COUSR02C COUSR03C
  Acct View Acct Upd Card List Bill Pay Txn List Reports  Usr List Usr Add  Usr Upd  Usr Del
  CA01     CA02     CC01     CB00     CT00     CR00      CU00    CU01     CU02     CU03
     │                │                │
     │                ▼                ▼
     │           ┌─────────┐     ┌─────────┐
     │           │COCRDSLC │     │COTRN01C │
     │           │Card View│     │Txn View │
     │           │ CC02    │     │ CT01    │
     │           └────┬────┘     └─────────┘
     │                │                │
     │                ▼                ▼
     │           ┌─────────┐     ┌─────────┐
     │           │COCRDUPC │     │COTRN02C │
     │           │Card Upd │     │Txn Add  │
     │           │ CC03    │     │ CT02    │
     │           └─────────┘     └─────────┘
     │
     ▼
  COACTUPC
  Acct Update
  CA02
```

### Detailed XCTL Targets

| Source Program | XCTL Target                        | Condition                             |
|----------------|------------------------------------|---------------------------------------|
| COSGN00C       | COMEN01C                           | Regular user login successful         |
| COSGN00C       | COADM01C                           | Admin user login successful           |
| COMEN01C       | COACTVWC / COACTUPC / COCRDLIC / COBIL00C / COTRN00C / CORPT00C | Based on menu option selected |
| COMEN01C       | CDEMO-TO-PROGRAM (dynamic)         | From COMMAREA - return to caller      |
| COADM01C       | COUSR00C / COUSR01C / COUSR02C / COUSR03C | Based on admin menu option |
| COADM01C       | CDEMO-TO-PROGRAM (dynamic)         | From COMMAREA - return to caller      |
| COACTVWC       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to menu               |
| COACTUPC       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to menu               |
| COCRDLIC       | COCRDSLC                           | Select card from list -> view         |
| COCRDLIC       | COCRDUPC                           | Select card from list -> update       |
| COCRDLIC       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to menu               |
| COCRDSLC       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to card list          |
| COCRDUPC       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to card list          |
| COTRN00C       | CDEMO-TO-PROGRAM (dynamic)         | Navigates via COMMAREA                |
| COTRN01C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to transaction list   |
| COTRN02C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to transaction list   |
| COBIL00C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to menu               |
| CORPT00C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to menu               |
| COUSR00C       | CDEMO-TO-PROGRAM (dynamic)         | Navigates to user add/update/delete   |
| COUSR01C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to user list          |
| COUSR02C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to user list          |
| COUSR03C       | CDEMO-TO-PROGRAM (dynamic)         | F3/back returns to user list          |

**Note:** Most returns use `CDEMO-TO-PROGRAM` from the COMMAREA, which is set by the calling program before XCTL. This creates a dynamic, loosely-coupled navigation pattern.

---

## Batch Program Call Graph

Batch programs use COBOL **CALL** statements for subroutine invocation.

```
CBSTM03A (Statement Generation)
    │
    ├── CALL 'CBSTM03B'     (File I/O subroutine - open/read/write/close)
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBTRN02C (Transaction Posting)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBTRN03C (Transaction Report)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBACT04C (Interest Calculation)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBACT01C (Account Read)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBACT02C (Card Read)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBACT03C (XRef Read)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBCUS01C (Customer Read)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBEXPORT (Data Export)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBIMPORT (Data Import)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBTRN01C (Transaction Read)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

COBSWAIT (Wait Utility)
    │
    └── CALL 'MVSWAIT'      (Assembler wait routine)

CSUTLDTC (Date Utility)
    │
    ├── CALL 'CEEDAYS'      (LE date-to-integer conversion)
    └── CALL 'CEEDATM'      (LE integer-to-date formatting)

CORPT00C (Online Report Submit)
    │
    ├── CALL 'CSUTLDTC'     (Date conversion utility)
    └── EXEC CICS WRITEQ TD (Writes JCL to transient data queue)

COCRDLIC (Card List)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

COTRN02C (Transaction Add)
    │
    └── CALL 'CSUTLDTC'     (Date conversion utility)

CBSTM03A (Statement Gen)
    │
    └── CALL 'CBSTM03B'     (File I/O subroutine)
```

### CALL Summary Table

| Calling Program | Called Program | Purpose                                 |
|-----------------|----------------|-----------------------------------------|
| CBSTM03A        | CBSTM03B       | Delegated file I/O for statements       |
| CBSTM03A        | CSUTLDTC       | Date conversion for statements          |
| CBTRN02C        | CSUTLDTC       | Date conversion for posting             |
| CBTRN03C        | CSUTLDTC       | Date conversion for reporting           |
| CBACT04C        | CSUTLDTC       | Date conversion for interest calc       |
| CBACT01C        | CSUTLDTC       | Date formatting for account display     |
| CBACT02C        | CSUTLDTC       | Date formatting for card display        |
| CBACT03C        | CSUTLDTC       | Date formatting for xref display        |
| CBCUS01C        | CSUTLDTC       | Date formatting for customer display    |
| CBTRN01C        | CSUTLDTC       | Date formatting for transaction display |
| CBEXPORT        | CSUTLDTC       | Date formatting for export              |
| CBIMPORT        | CSUTLDTC       | Date formatting for import              |
| CORPT00C        | CSUTLDTC       | Date formatting for report screen       |
| COCRDLIC        | CSUTLDTC       | Date formatting for card list           |
| COTRN02C        | CSUTLDTC       | Date formatting for transaction add     |
| COBSWAIT        | MVSWAIT        | Assembler-level MVS wait                |
| CSUTLDTC        | CEEDAYS        | LE intrinsic: date to integer           |
| CSUTLDTC        | CEEDATM        | LE intrinsic: integer to formatted date |

---

## Copybook Inclusion Matrix

Shows which copybooks are included (`COPY`) by which programs.

### Core Business Copybooks

| Copybook   | COACTUPC | COACTVWC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | CORPT00C |
|------------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| COCOM01Y   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| COTTL01Y   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| CSDAT01Y   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| CSMSG01Y   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| CSMSG02Y   | x        | x        | x        | x        | x        | x        | x        | x        | x        |          |
| CSSETATY   | x        | x        | x        | x        | x        | x        | x        | x        | x        |          |
| CSUSR01Y   | x        |          |          |          |          |          |          |          |          |          |
| CVACT01Y   | x        | x        |          |          |          |          |          |          |          |          |
| CVACT02Y   | x        | x        | x        | x        | x        |          |          |          |          |          |
| CVACT03Y   | x        | x        | x        | x        | x        |          |          |          |          |          |
| CVCUS01Y   | x        | x        |          |          |          |          |          |          |          |          |
| CVTRA05Y   |          |          |          |          |          | x        | x        | x        |          |          |

| Copybook   | COSGN00C | COMEN01C | COADM01C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|------------|----------|----------|----------|----------|----------|----------|----------|
| COCOM01Y   | x        | x        | x        | x        | x        | x        | x        |
| COTTL01Y   | x        | x        | x        | x        | x        | x        | x        |
| CSDAT01Y   | x        | x        | x        | x        | x        | x        | x        |
| CSMSG01Y   | x        | x        | x        | x        | x        | x        | x        |
| CSMEN02Y   |          | x        |          |          |          |          |          |
| COADM02Y   |          |          | x        |          |          |          |          |
| CSUSR01Y   | x        |          |          | x        | x        | x        | x        |

### Batch Program Copybook Usage

| Copybook   | CBTRN02C | CBTRN03C | CBACT04C | CBSTM03A | CBACT01C | CBACT02C | CBACT03C | CBCUS01C | CBTRN01C | CBEXPORT | CBIMPORT |
|------------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| CVTRA05Y   | x        |          |          |          |          |          |          |          |          |          |          |
| CVTRA06Y   | x        |          |          |          |          |          |          |          |          |          |          |
| CVACT01Y   |          |          | x        |          | x        |          |          |          |          |          |          |
| CVACT02Y   |          |          |          |          |          | x        |          |          |          |          |          |
| CVACT03Y   |          |          |          |          |          |          | x        |          |          |          |          |
| CVCUS01Y   |          |          |          |          |          |          |          | x        |          |          |          |
| CVTRA01Y   |          |          | x        |          |          |          |          |          |          |          |          |
| CVTRA02Y   |          |          | x        |          |          |          |          |          |          |          |          |
| CVTRA03Y   |          | x        |          |          |          |          |          |          |          |          |          |
| CVTRA04Y   |          | x        |          |          |          |          |          |          |          |          |          |
| CVTRA07Y   |          | x        |          |          |          |          |          |          |          |          |          |
| COSTM01    |          |          |          | x        |          |          |          |          |          |          |          |
| CVEXPORT   |          |          |          |          |          |          |          |          |          | x        | x        |
| CSUTLDPY   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| CSUTLDWY   | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        | x        |
| CSDAT01Y   | x        | x        | x        |          |          |          |          |          |          |          |          |

---

## JCL Job-to-Program Mapping

### Jobs Executing COBOL Programs

| JCL Job    | Step    | Program Executed | Purpose                              | Input Datasets                          | Output Datasets                        |
|------------|---------|------------------|--------------------------------------|-----------------------------------------|----------------------------------------|
| POSTTRAN   | STEP010 | CBTRN02C         | Post daily transactions              | DALYTRAN, TRANSACT, XREF, DISCGRP, TCATBALF, TRANTYPE, TRANCATG | TRANSACT (updated), TCATBALF (updated), DALYREJS |
| INTCALC    | STEP010 | CBACT04C         | Calculate interest                   | TRANSACT, DISCGRP, TCATBALF, ACCTDATA  | TCATBALF (updated), ACCTDATA (updated) |
| TRANREPT   | STEP030 | CBTRN03C         | Generate transaction report          | TRANSACT.DALY, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT report                        |
| CREASTMT   | STEP040 | CBSTM03A         | Generate statements                  | TRXFL (sorted trans), CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML             |
| READACCT   | STEP010 | CBACT01C         | Read account data                    | ACCTDATA                                | SYSOUT (display)                       |
| READCARD   | STEP010 | CBACT02C         | Read card data                       | CARDDATA                                | SYSOUT (display)                       |
| READCUST   | STEP010 | CBCUS01C         | Read customer data                   | CUSTDATA                                | SYSOUT (display)                       |
| READXREF   | STEP010 | CBACT03C         | Read cross-reference data            | CARDXREF                                | SYSOUT (display)                       |
| CBEXPORT   | STEP010 | CBEXPORT         | Export VSAM to flat file             | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPORT.DATA                           |
| CBIMPORT   | STEP010 | CBIMPORT         | Import flat file to VSAM            | EXPORT.DATA                             | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT (import files) |
| WAITSTEP   | STEP010 | COBSWAIT         | Wait utility step                    | None                                    | None                                   |

### Jobs Executing System Utilities

| JCL Job    | Steps   | Utility          | Purpose                              | Key Datasets Affected                   |
|------------|---------|------------------|--------------------------------------|-----------------------------------------|
| ACCTFILE   | Multi   | IDCAMS           | Define + load Account VSAM           | ACCTDATA.PS -> ACCTDATA.VSAM.KSDS      |
| CARDFILE   | Multi   | IDCAMS           | Define + load Card VSAM              | CARDDATA.PS -> CARDDATA.VSAM.KSDS      |
| CUSTFILE   | Multi   | IDCAMS           | Define + load Customer VSAM          | CUSTDATA.PS -> CUSTDATA.VSAM.KSDS      |
| XREFFILE   | Multi   | IDCAMS           | Define + load XRef VSAM + AIX        | CARDXREF.PS -> CARDXREF.VSAM.KSDS      |
| TRANFILE   | Multi   | IDCAMS           | Define + load Transaction VSAM       | DALYTRAN.PS -> TRANSACT.VSAM.KSDS      |
| DUSRSECJ   | Multi   | IDCAMS           | Define + load User Security VSAM     | USRSEC.PS -> USRSEC.VSAM.KSDS          |
| DISCGRP    | Multi   | IDCAMS           | Define + load Disclosure Group VSAM  | DISCGRP.PS -> DISCGRP.VSAM.KSDS        |
| TRANTYPE   | Multi   | IDCAMS           | Define + load Transaction Type VSAM  | TRANTYPE.PS -> TRANTYPE.VSAM.KSDS      |
| TRANCATG   | Multi   | IDCAMS           | Define + load Transaction Category   | TRANCATG.PS -> TRANCATG.VSAM.KSDS      |
| TCATBALF   | Multi   | IDCAMS           | Define + load Category Balance VSAM  | TCATBALF.PS -> TCATBALF.VSAM.KSDS      |
| DEFCUST    | Multi   | IDCAMS           | Define Customer VSAM (no load)       | CUSTDATA.VSAM.KSDS (define only)        |
| CLOSEFIL   | STEP010 | DFHCSDUP         | Close CICS files for batch           | All CICS-managed VSAM files             |
| OPENFIL    | STEP010 | DFHCSDUP         | Open CICS files after batch          | All CICS-managed VSAM files             |
| TRANBKP    | Multi   | IDCAMS           | Backup transaction file              | TRANSACT.VSAM -> TRANSACT.BKUP GDG     |
| COMBTRAN   | Multi   | SORT + IDCAMS    | Combine backed-up transactions       | TRANSACT.BKUP + SYSTRAN -> COMBINED     |
| TRANIDX    | Multi   | IDCAMS           | Rebuild alternate indexes            | TRANSACT.VSAM.KSDS AIX                  |
| DEFGDGB    | Multi   | IDCAMS           | Define GDG base clusters             | Multiple GDG bases                       |
| DEFGDGD    | Multi   | IDCAMS           | Delete GDG base clusters             | Multiple GDG bases                       |
| ESDSRRDS   | Multi   | IDCAMS + IEBGENER| Create ESDS/RRDS user security       | USRSEC -> ESDS + RRDS versions          |
| PRTCATBL   | Multi   | IDCAMS (PRINT)   | Print category balance report        | TCATBALF.VSAM.KSDS                      |
| DALYREJS   | Multi   | IDCAMS (PRINT)   | Print daily rejection report         | DALYREJS dataset                         |
| REPTFILE   | Multi   | IDCAMS (PRINT)   | Print generic report                 | Report dataset                           |
| TXT2PDF1   | STEP010 | TXT2PDF (REXX)   | Convert statement to PDF             | STATEMNT.PS -> STATEMNT.PS.PDF          |
| FTPJCL     | STEP1   | FTP              | Transfer files via FTP               | CARDEMO.FTP.TEST                         |
| INTRDRJ1   | Multi   | IEBGENER + IDCAMS| Internal reader trigger              | FTP.TEST -> FTP.TEST.BKUP               |
| INTRDRJ2   | Multi   | IDCAMS           | Internal reader target               | FTP.TEST.BKUP -> FTP.TEST.BKUP.INTRDR  |

---

## VSAM Dataset Lineage

Shows which programs/jobs read from and write to each VSAM dataset.

### Read/Write Matrix

| VSAM Dataset (short name) | Written By (JCL Load)     | Read By (Online)                         | Read By (Batch)                          | Updated By (Batch)    |
|---------------------------|---------------------------|------------------------------------------|------------------------------------------|-----------------------|
| ACCTDATA                  | ACCTFILE                  | COACTVWC, COACTUPC                       | CBACT01C, CBACT04C, CBSTM03A, CBEXPORT  | COACTUPC, CBACT04C    |
| CARDDATA                  | CARDFILE                  | COCRDLIC, COCRDSLC, COCRDUPC             | CBACT02C, CBEXPORT                       | COCRDUPC              |
| CUSTDATA                  | CUSTFILE                  | COACTVWC, COACTUPC                       | CBCUS01C, CBSTM03A, CBEXPORT            | COACTUPC              |
| CARDXREF                  | XREFFILE                  | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT03C, CBSTM03A, CBTRN03C, CBEXPORT | --                    |
| TRANSACT                  | TRANFILE                  | COTRN00C, COTRN01C, COTRN02C            | CBTRN01C, CBSTM03A, CBEXPORT, CBTRN03C  | CBTRN02C, COTRN02C   |
| USRSEC                    | DUSRSECJ                  | COSGN00C, COUSR00C-03C                   | --                                       | COUSR01C-03C          |
| TRANTYPE                  | TRANTYPE                  | --                                        | CBTRN03C                                 | --                    |
| TRANCATG                  | TRANCATG                  | --                                        | CBTRN03C                                 | --                    |
| TCATBALF                  | TCATBALF                  | --                                        | CBACT04C                                 | CBACT04C, CBTRN02C   |
| DISCGRP                   | DISCGRP                   | --                                        | CBACT04C, CBTRN02C                       | --                    |
| DALYTRAN                  | (external feed)           | --                                        | CBTRN02C                                 | --                    |

### Data Flow: Nightly Batch Cycle

```
External System                 Flat Files (PS)              VSAM Files (KSDS)
    │                               │                            │
    │  Daily transactions           │                            │
    ▼                               │                            │
┌──────────┐                        │                            │
│ CLOSEFIL │ ──────── Close CICS ──►│                            │
└────┬─────┘                        │                            │
     │                              │                            │
     ▼                              ▼                            ▼
┌──────────┐     ┌──────────┐    ┌────────────┐
│ ACCTFILE │ ◄── │ACCTDATA  │ ──►│ ACCTDATA   │
│ CARDFILE │ ◄── │CARDDATA  │ ──►│ CARDDATA   │
│ CUSTFILE │ ◄── │CUSTDATA  │ ──►│ CUSTDATA   │
│ XREFFILE │ ◄── │CARDXREF  │ ──►│ CARDXREF   │
│ TRANFILE │ ◄── │DALYTRAN  │ ──►│ TRANSACT   │
└────┬─────┘     └──────────┘    └─────┬──────┘
     │                                  │
     ▼                                  ▼
┌──────────┐                    ┌───────────────┐
│ POSTTRAN │ ── CBTRN02C ──────►│ TRANSACT      │ (updated)
│          │                    │ TCATBALF      │ (updated)
│          │                    │ DALYREJS      │ (rejections)
└────┬─────┘                    └───────┬───────┘
     │                                  │
     ▼                                  ▼
┌──────────┐                    ┌───────────────┐
│ INTCALC  │ ── CBACT04C ──────►│ ACCTDATA      │ (bal updated)
│          │                    │ TCATBALF      │ (reset)
└────┬─────┘                    └───────┬───────┘
     │                                  │
     ▼                                  ▼
┌──────────┐                    ┌───────────────┐
│ TRANBKP  │ ── IDCAMS ────────►│ TRANSACT.BKUP │ (GDG)
└────┬─────┘                    └───────────────┘
     │
     ▼
┌──────────┐                    ┌───────────────┐
│ COMBTRAN │ ── SORT+IDCAMS ───►│ TRANSACT      │
│          │                    │ .COMBINED     │ (GDG)
└────┬─────┘                    └───────────────┘
     │
     ▼
┌──────────┐                    ┌───────────────┐
│ CREASTMT │ ── CBSTM03A ──────►│ STATEMNT.PS   │ (text)
│          │                    │ STATEMNT.HTML │ (HTML)
└────┬─────┘                    └───────────────┘
     │
     ▼
┌──────────┐
│ TRANIDX  │ ── IDCAMS ──────── Rebuild alternate indexes
└────┬─────┘
     │
     ▼
┌──────────┐
│ OPENFIL  │ ──────── Reopen CICS files
└──────────┘
```

---

## BMS Map-to-Program Mapping

Each BMS map has a 1:1 relationship with an online CICS program.

| BMS Map    | BMS Mapset | Program    | BMS-Gen Copybook | Screen Function       |
|------------|------------|------------|------------------|-----------------------|
| COSGN0A    | COSGN00    | COSGN00C   | COSGN00.CPY      | Sign-on               |
| COMEN1A    | COMEN01    | COMEN01C   | COMEN01.CPY      | Main Menu             |
| COADM1A    | COADM01    | COADM01C   | COADM01.CPY      | Admin Menu            |
| COACVW     | COACTVW    | COACTVWC   | COACTVW.CPY      | Account View          |
| COACTUP    | COACTUP    | COACTUPC   | COACTUP.CPY      | Account Update        |
| CCRDLI     | COCRDLI    | COCRDLIC   | COCRDLI.CPY      | Card List             |
| CCRDSL     | COCRDSL    | COCRDSLC   | COCRDSL.CPY      | Card Detail           |
| CCRDUP     | COCRDUP    | COCRDUPC   | COCRDUP.CPY      | Card Update           |
| COTRN0A    | COTRN00    | COTRN00C   | COTRN00.CPY      | Transaction List      |
| COTRN1A    | COTRN01    | COTRN01C   | COTRN01.CPY      | Transaction View      |
| COTRN2A    | COTRN02    | COTRN02C   | COTRN02.CPY      | Transaction Add       |
| CORPT0A    | CORPT00    | CORPT00C   | CORPT00.CPY      | Report Request        |
| COBIL0A    | COBIL00    | COBIL00C   | COBIL00.CPY      | Bill Payment          |
| COUSR0A    | COUSR00    | COUSR00C   | COUSR00.CPY      | User List             |
| COUSR1A    | COUSR01    | COUSR01C   | COUSR01.CPY      | User Add              |
| COUSR2A    | COUSR02    | COUSR02C   | COUSR02.CPY      | User Update           |
| COUSR3A    | COUSR03    | COUSR03C   | COUSR03.CPY      | User Delete           |

---

## Cross-Cutting Concerns

### Shared Utility: CSUTLDTC (Date Conversion)

This is the most widely-called utility in the codebase. Called by **14 programs** (all batch programs + several online programs).

**Impact:** Any change to CSUTLDTC's interface or behavior affects the entire application. Prioritize this for early conversion to a shared Java utility class.

### Shared Copybook: COCOM01Y (Communication Area)

Included by **all 17 online CICS programs**. Defines the contract for inter-program navigation.

**Impact:** The COMMAREA structure is the central integration point for the online system. In Java, this maps to a session object or request context.

### Shared Copybooks: COTTL01Y, CSDAT01Y, CSMSG01Y

Included by virtually all online programs. Define common screen headers, date areas, and messages.

**Impact:** These map to shared UI components (header/footer templates) and message resource bundles in Java.

### CICS File Access Patterns

| CICS Command  | Programs Using It                                             | Count |
|---------------|---------------------------------------------------------------|-------|
| READ          | COACTUPC, COACTVWC, COCRDSLC, COCRDLIC, COCRDUPC, COTRN00C, COUSR02C, COUSR03C | 8     |
| WRITE         | COTRN02C, COUSR01C                                           | 2     |
| REWRITE       | COACTUPC, COCRDUPC, COUSR02C                                 | 3     |
| DELETE        | COUSR03C                                                      | 1     |
| STARTBR       | COCRDLIC, COTRN00C, COUSR00C                                 | 3     |
| READNEXT      | COCRDLIC, COTRN00C, COUSR00C                                 | 3     |
| READPREV      | COCRDLIC, COTRN00C, COUSR00C                                 | 3     |
| ENDBR         | COCRDLIC, COTRN00C, COUSR00C                                 | 3     |
| SEND MAP      | All 17 online programs                                        | 17    |
| RECEIVE MAP   | All 17 online programs                                        | 17    |
| XCTL          | All 17 online programs                                        | 17    |
| WRITEQ TD     | CORPT00C                                                      | 1     |

### Dependency Clusters (for Modernization Phases)

Programs that share data and should be modernized together:

| Cluster Name          | Programs                                    | Shared Data                     |
|-----------------------|---------------------------------------------|---------------------------------|
| Account Management    | COACTVWC, COACTUPC, CBACT01C, CBACT04C     | ACCTDATA, CUSTDATA, CARDXREF   |
| Card Management       | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C | CARDDATA, CARDXREF        |
| Transaction Processing| COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | TRANSACT, DALYTRAN        |
| Reporting             | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B    | TRANSACT, CARDXREF, CUSTDATA   |
| User Administration   | COUSR00C, COUSR01C, COUSR02C, COUSR03C     | USRSEC                         |
| Authentication        | COSGN00C                                    | USRSEC                         |
| Navigation            | COMEN01C, COADM01C                          | COMMAREA (COCOM01Y)            |
| Data Migration        | CBEXPORT, CBIMPORT                          | All VSAM files                 |
| Billing               | COBIL00C                                    | TRANSACT, ACCTDATA             |
