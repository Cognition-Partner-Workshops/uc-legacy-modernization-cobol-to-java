# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Source**: Static analysis of COBOL CALL/XCTL/COPY, JCL EXEC/DD statements

---

## 1. Online CICS Program Call Graph

### 1.1 Screen Navigation Flow

The online application is driven by CICS `XCTL` (transfer control) commands. Each program transfers control to the next via `CDEMO-TO-PROGRAM` in the common communication area (`COCOM01Y`).

```
                              ┌──────────────┐
                              │  COSGN00C    │
                              │  (Sign-on)   │
                              └──────┬───────┘
                                     │ XCTL
                          ┌──────────▼──────────┐
                          │      COMEN01C        │
                          │    (Main Menu)       │
                          │  11 menu options      │
                          └──────────┬───────────┘
                                     │
            ┌────────────────────────┼────────────────────────┐
            │                        │                        │
    ┌───────▼───────┐      ┌────────▼────────┐     ┌────────▼────────┐
    │  Account Ops  │      │ Transaction Ops │     │   Admin Path    │
    │               │      │                 │     │  (Type='A')     │
    └───────┬───────┘      └────────┬────────┘     └────────┬────────┘
            │                       │                       │
  ┌─────────┼─────────┐   ┌────────┼────────┐    ┌────────▼────────┐
  │         │         │   │        │        │    │   COADM01C      │
  ▼         ▼         ▼   ▼        ▼        ▼    │  (Admin Menu)   │
COACTVWC COACTUPC COCRDLIC COTRN00C COTRN02C CORPT00C  └────────┬────────┘
(View)   (Update) (List)  (List)  (Add)   (Reports)          │
  │         │       │       │                      ┌──────────┼──────────┐
  │         │       │       │                      │          │          │
  │         │    ┌──▼──┐ ┌──▼──┐                   ▼          ▼          ▼
  │         │    │COCRD│ │COTRN│                 COUSR00C  COUSR01C  COUSR02C
  │         │    │SLC  │ │01C  │                 (List)    (Add)     (Update)
  │         │    └─────┘ └─────┘                    │
  │         │    (View)  (View)                     ▼
  │         │       │                            COUSR03C
  │         │    ┌──▼──┐                         (Delete)
  │         │    │COCRD│
  │         │    │UPC  │     ┌──────────┐
  │         │    └─────┘     │COBIL00C  │
  │         │    (Update)    │(Bill Pay)│
  │         │                └──────────┘
  │         │
  │   (also navigates to COCRDLIC, COCRDSLC, COCRDUPC for card management)
  │
  └── All screens: PF3 → return to COMEN01C or caller
```

### 1.2 Detailed XCTL Transfer Map

| Source Program | Target Program | Trigger / Condition                                |
|----------------|----------------|----------------------------------------------------|
| COSGN00C       | COMEN01C       | Successful login (regular user)                    |
| COSGN00C       | COADM01C       | Successful login (admin user, SEC-USR-TYPE = 'A')  |
| COMEN01C       | COACTVWC       | Menu option 1 — Account View                      |
| COMEN01C       | COACTUPC       | Menu option 2 — Account Update                    |
| COMEN01C       | COCRDLIC       | Menu option 3 — Credit Card List                  |
| COMEN01C       | COCRDSLC       | Menu option 4 — Credit Card View                  |
| COMEN01C       | COCRDUPC       | Menu option 5 — Credit Card Update                |
| COMEN01C       | COTRN00C       | Menu option 6 — Transaction List                  |
| COMEN01C       | COTRN01C       | Menu option 7 — Transaction View                  |
| COMEN01C       | COTRN02C       | Menu option 8 — Transaction Add                   |
| COMEN01C       | CORPT00C       | Menu option 9 — Transaction Reports               |
| COMEN01C       | COBIL00C       | Menu option 10 — Bill Payment                     |
| COMEN01C       | COPAUS0C       | Menu option 11 — Pending Authorization View (ext) |
| COMEN01C       | COSGN00C       | PF3 — Return to sign-on                           |
| COADM01C       | COUSR00C       | Admin option 1 — User List                        |
| COADM01C       | COUSR01C       | Admin option 2 — User Add                         |
| COADM01C       | COUSR02C       | Admin option 3 — User Update                      |
| COADM01C       | COUSR03C       | Admin option 4 — User Delete                      |
| COADM01C       | COTRTLIC       | Admin option 5 — Transaction Type List (DB2 ext)  |
| COADM01C       | COTRTUPC       | Admin option 6 — Transaction Type Maint (DB2 ext) |
| COADM01C       | COSGN00C       | PF3 — Return to sign-on                           |
| COACTVWC       | COMEN01C       | PF3 — Return to main menu                         |
| COACTVWC       | *(caller)*     | PF3 — Return to calling program                   |
| COACTUPC       | COMEN01C       | PF3 — Return to main menu                         |
| COACTUPC       | *(caller)*     | PF3 — Return to calling program                   |
| COCRDLIC       | COCRDSLC       | Select card from list — view details               |
| COCRDLIC       | COCRDUPC       | Select card from list — update                     |
| COCRDLIC       | COMEN01C       | PF3 — Return to main menu                         |
| COCRDSLC       | COMEN01C       | PF3 — Return to main menu                         |
| COCRDSLC       | *(caller)*     | PF3 — Return to calling program                   |
| COCRDUPC       | COMEN01C       | PF3 — Return to main menu                         |
| COCRDUPC       | *(caller)*     | PF3 — Return to calling program                   |
| COTRN00C       | COTRN01C       | Select transaction — view details                  |
| COTRN00C       | COMEN01C       | PF3 — Return to main menu                         |
| COTRN01C       | COTRN00C       | PF3 — Return to transaction list                  |
| COTRN01C       | COMEN01C       | PF3 — Return to main menu                         |
| COTRN02C       | COMEN01C       | PF3 — Return to main menu                         |
| CORPT00C       | COMEN01C       | PF3 — Return to main menu                         |
| COBIL00C       | COMEN01C       | PF3 — Return to main menu                         |
| COUSR00C       | COUSR02C       | Select user — update                               |
| COUSR00C       | COUSR03C       | Select user — delete                               |
| COUSR00C       | COADM01C       | PF3 — Return to admin menu                        |
| COUSR01C       | COADM01C       | PF3 — Return to admin menu                        |
| COUSR02C       | COADM01C       | PF3 — Return to admin menu                        |
| COUSR03C       | COADM01C       | PF3 — Return to admin menu                        |

### 1.3 Batch Program Call Graph

```
CBSTM03A ──CALL──► CBSTM03B    (Statement: main calls file handler subroutine)
CBACT01C ──CALL──► COBDATFT    (Account read: calls assembler date formatter)
CSUTLDTC ──CALL──► CEEDAYS     (Date utility: calls LE date conversion)
COACCT01 ──CALL──► MQOPEN/MQGET/MQPUT/MQCLOSE  (MQ operations)
CODATE01 ──CALL──► MQOPEN/MQGET/MQPUT/MQCLOSE  (MQ operations)

All batch programs call CEE3ABD for abnormal termination (abend).
```

---

## 2. Copybook Dependency Matrix

Which programs include (COPY) which copybooks:

### 2.1 Data Structure Copybooks

| Copybook  | Programs That Include It                                                    |
|-----------|-----------------------------------------------------------------------------|
| CVACT01Y  | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, COACTUPC, COACTVWC, COBIL00C, COACCT01 |
| CVACT02Y  | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COTRTLIC, COTRTUPC      |
| CVACT03Y  | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C |
| CVCUS01Y  | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC               |
| CVTRA01Y  | CBACT04C, CBTRN02C                                                         |
| CVTRA02Y  | CBACT04C                                                                    |
| CVTRA03Y  | CBTRN03C                                                                    |
| CVTRA04Y  | CBTRN03C                                                                    |
| CVTRA05Y  | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, COBIL00C               |
| CVTRA06Y  | CBTRN01C, CBTRN02C                                                         |
| CVTRA07Y  | CBTRN03C                                                                    |
| CVCRD01Y  | COACTUPC, COACTVWC, COTRTLIC, COTRTUPC                                    |
| COSTM01   | CBSTM03A                                                                    |
| CUSTREC   | CBSTM03A                                                                    |
| CVEXPORT  | CBEXPORT, CBIMPORT                                                          |
| CSUSR01Y  | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC |

### 2.2 Framework/UI Copybooks

| Copybook  | Programs That Include It                                                    |
|-----------|-----------------------------------------------------------------------------|
| COCOM01Y  | All 17 online CICS programs, COADM01C, COTRTLIC, COTRTUPC, COPAUS0C, COPAUS1C |
| COTTL01Y  | All 17 online CICS programs, COTRTLIC, COTRTUPC, COPAUS0C, COPAUS1C        |
| CSDAT01Y  | All 17 online CICS programs, COTRTLIC, COTRTUPC, COPAUS0C, COPAUS1C        |
| CSMSG01Y  | All 17 online CICS programs, COTRTLIC, COTRTUPC, COPAUS0C, COPAUS1C        |
| CSMSG02Y  | COACTUPC, COACTVWC, COTRTUPC, COPAUS1C                                     |
| COMEN02Y  | COMEN01C (defines main menu navigation structure)                           |
| COADM02Y  | COADM01C (defines admin menu navigation structure)                          |
| DFHAID    | All 17 online CICS programs, COTRTLIC, COTRTUPC                            |
| DFHBMSCA  | All 17 online CICS programs, COTRTLIC                                       |
| CSLKPCDY  | COACTUPC (lookup code tables — 51KB)                                        |
| CSSETATY  | COACTUPC (×38 COPY REPLACING), COTRTUPC                                    |
| CSSTRPFY  | COACTUPC, COACTVWC, COTRTLIC, COTRTUPC                                     |
| CSUTLDPY  | COACTUPC                                                                    |
| CSUTLDWY  | COACTUPC, COTRTUPC                                                          |
| CODATECN  | CBACT01C                                                                    |

---

## 3. JCL Job → Program → VSAM File Data Lineage

### 3.1 Batch Processing Cycle (Daily Execution Order)

```
Step 1: CLOSEFIL ─────► Close CICS files (SDSF CEMT commands)
                         No VSAM I/O — CICS release only

Step 2: Data Refresh Jobs (parallel-capable)
  ACCTFILE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► ACCTDATA.VSAM.KSDS
  CARDFILE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► CARDDATA.VSAM.KSDS + AIX
  CUSTFILE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► CUSTDATA.VSAM.KSDS
  XREFFILE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► CARDXREF.VSAM.KSDS + AIX
  TRANFILE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► TRANSACT.VSAM.KSDS + AIX
  TCATBALF ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► TCATBAL.VSAM.KSDS
  DISCGRP  ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► DISCGRP.VSAM.KSDS
  TRANTYPE ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► TRANTYPE.VSAM.KSDS
  TRANCATG ───► IDCAMS ─── DELETE+DEFINE+REPRO ──► TRANCATG.VSAM.KSDS

Step 3: POSTTRAN ─────► CBTRN02C
  Reads:   DALYTRAN.PS (daily transactions)
           CARDXREF.VSAM.KSDS (validate card)
           ACCTDATA.VSAM.KSDS (validate account)
  Writes:  TRANSACT.VSAM.KSDS (post to master)
           TCATBAL.VSAM.KSDS (update category balance)
           DALYREJS (rejected transactions)

Step 4: INTCALC ──────► CBACT04C
  Reads:   TCATBAL.VSAM.KSDS (category balances)
           CARDXREF.VSAM.KSDS (card-account lookup)
           DISCGRP.VSAM.KSDS (interest rates)
           ACCTDATA.VSAM.KSDS (account data)
           TRANSACT.VSAM.KSDS (transactions)
  Writes:  ACCTDATA.VSAM.KSDS (update balances with interest)

Step 5: TRANBKP ──────► IDCAMS REPRO
  Reads:   TRANSACT.VSAM.KSDS
  Writes:  TRANSACT.BKUP.PS (backup copy)

Step 6: COMBTRAN ─────► SORT + IDCAMS
  Reads:   TRANSACT.VSAM.KSDS
           DALYTRAN.PS
  Writes:  TRANSACT.VSAM.KSDS (merged/combined)

Step 7: CREASTMT ─────► SORT + CBSTM03A (calls CBSTM03B)
  Reads:   TRANSACT.VSAM.KSDS (via re-sorted copy)
           CARDXREF.VSAM.KSDS
           ACCTDATA.VSAM.KSDS
           CUSTDATA.VSAM.KSDS
  Writes:  STATEMNT.PS (text statements)
           STATEMNT.HTML (HTML statements)

Step 8: TRANIDX ──────► IDCAMS
  Rebuilds alternate indexes on TRANSACT.VSAM.KSDS

Step 9: OPENFIL ──────► Re-open CICS files (SDSF CEMT commands)
```

### 3.2 Reporting Jobs

| JCL Job   | Program   | Input Files (Reads)                                           | Output Files (Writes)         |
|-----------|-----------|---------------------------------------------------------------|-------------------------------|
| TRANREPT  | CBTRN03C  | TRANSACT (sorted), CARDXREF, TRANTYPE, TRANCATG, DATEPARM    | TRANREPT (report file)        |
| PRTCATBL  | SORT      | TCATBAL.VSAM.KSDS                                             | Sorted print output           |
| TXT2PDF1  | TXT2PDF   | STATEMNT.PS                                                    | STATEMNT.PS.PDF               |

### 3.3 Data Read/Print Jobs

| JCL Job   | Program   | Input Files (Reads)                                           | Output Files (Writes)         |
|-----------|-----------|---------------------------------------------------------------|-------------------------------|
| READACCT  | CBACT01C  | ACCTDATA.VSAM.KSDS                                            | PSCOMP, ARRYPS, VBPS (3 formats) |
| READCARD  | CBACT02C  | CARDDATA.VSAM.KSDS                                            | SYSOUT (print)                |
| READCUST  | CBCUS01C  | CUSTDATA.VSAM.KSDS                                            | SYSOUT (print)                |
| READXREF  | CBACT03C  | CARDXREF.VSAM.KSDS                                            | SYSOUT (print)                |

### 3.4 Data Migration Jobs

| JCL Job   | Program   | Input Files (Reads)                                           | Output Files (Writes)         |
|-----------|-----------|---------------------------------------------------------------|-------------------------------|
| CBEXPORT  | CBEXPORT  | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA              | EXPFILE (multi-record export) |
| CBIMPORT  | CBIMPORT  | EXPFILE (multi-record export)                                  | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

### 3.5 Infrastructure Jobs

| JCL Job   | Programs  | Input/Output                                                   |
|-----------|-----------|---------------------------------------------------------------|
| DUSRSECJ  | IDCAMS    | Reads USRSEC.PS → Writes USRSEC.VSAM.KSDS                    |
| DEFGDGB   | IDCAMS    | Defines GDG bases for TRANSACT, TRANCATG, DISCGRP, TRANTYPE  |
| DEFGDGD   | IDCAMS+IEBGENER | Copies current flat files to GDG backup generations   |
| CBADMCDJ  | DFHCSDUP  | Reads CSD commands → Updates CARDDEMO.CSD                    |
| WAITSTEP  | COBSWAIT  | No data I/O — timer delay only                                |
| FTPJCL    | FTP       | Transfers files to/from mainframe via FTP                     |
| INTRDRJ1  | IDCAMS+IEBGENER | Copies FTP.TEST → triggers INTRDRJ2                  |
| INTRDRJ2  | IDCAMS    | Copies FTP.TEST.BKUP → FTP.TEST.BKUP.INTRDR                 |
| ESDSRRDS  | IDCAMS    | Creates ESDS and RRDS VSAM cluster demos                      |

---

## 4. Online Program → VSAM File Access Matrix

| Program    | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | TCATBAL | USRSEC | DALYTRAN |
|------------|----------|----------|----------|----------|----------|---------|--------|----------|
| COSGN00C   |          |          |          |          |          |         | **R**  |          |
| COACTVWC   | **R**    |          | **R**    | **R**    |          |         |        |          |
| COACTUPC   | **RW**   |          | **R**    | **R**    |          |         |        |          |
| COCRDLIC   |          | **R**    | **R**    |          |          |         |        |          |
| COCRDSLC   |          | **R**    | **R**    |          |          |         |        |          |
| COCRDUPC   |          | **RW**   | **R**    |          |          |         |        |          |
| COTRN00C   |          |          | **R**    |          | **R**    |         |        |          |
| COTRN01C   |          |          | **R**    |          | **R**    |         |        |          |
| COTRN02C   | **RW**   |          | **R**    |          | **W**    | **RW**  |        |          |
| COBIL00C   | **RW**   |          | **R**    |          | **W**    |         |        |          |
| CORPT00C   |          |          |          |          |          |         |        |          |
| COUSR00C   |          |          |          |          |          |         | **R**  |          |
| COUSR01C   |          |          |          |          |          |         | **W**  |          |
| COUSR02C   |          |          |          |          |          |         | **RW** |          |
| COUSR03C   |          |          |          |          |          |         | **RD** |          |

**R** = Read, **W** = Write, **RW** = Read+Write, **RD** = Read+Delete

### 4.1 Batch Program → File Access Matrix

| Program    | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | TCATBAL | DISCGRP | DALYTRAN | DALYREJS | Other Outputs   |
|------------|----------|----------|----------|----------|----------|---------|---------|----------|----------|-----------------|
| CBTRN01C   | **R**    | **R**    | **R**    | **R**    | **R**    |         |         | **R**    |          |                 |
| CBTRN02C   | **RW**   |          | **R**    |          | **W**    | **RW**  |         | **R**    | **W**    |                 |
| CBTRN03C   |          |          | **R**    |          | **R**    |         |         |          |          | TRANREPT (W)    |
| CBACT01C   | **R**    |          |          |          |          |         |         |          |          | OUTFILE, ARRYFILE, VBRCFILE (W) |
| CBACT02C   |          | **R**    |          |          |          |         |         |          |          |                 |
| CBACT03C   |          |          | **R**    |          |          |         |         |          |          |                 |
| CBACT04C   | **RW**   |          | **R**    |          | **R**    | **R**   | **R**   |          |          |                 |
| CBCUS01C   |          |          |          | **R**    |          |         |         |          |          |                 |
| CBSTM03A/B |          |          | **R**    | **R**    | **R**    |         |         |          |          | STATEMNT.PS, .HTML (W) |
| CBEXPORT   |          | **R**    | **R**    | **R**    | **R**    |         |         |          |          | EXPFILE (W)     |
| CBIMPORT   |          |          |          |          |          |         |         |          |          | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT (W) |

---

## 5. Most Connected Data Entities (Impact Ranking)

| Rank | VSAM Dataset       | Read By (programs) | Written By (programs) | Accessed By (JCL jobs) | Impact Score |
|------|--------------------|---------------------|----------------------|------------------------|--------------|
| 1    | CARDXREF.VSAM.KSDS | 14 programs         | XREFFILE (IDCAMS)    | 12+ jobs               | **Critical** |
| 2    | ACCTDATA.VSAM.KSDS | 10 programs         | COACTUPC, CBTRN02C, CBACT04C | 10+ jobs        | **Critical** |
| 3    | TRANSACT.VSAM.KSDS | 8 programs          | COTRN02C, CBTRN02C, COBIL00C | 8+ jobs         | **High**     |
| 4    | CUSTDATA.VSAM.KSDS | 6 programs          | CUSTFILE (IDCAMS)    | 6+ jobs                | **High**     |
| 5    | CARDDATA.VSAM.KSDS | 5 programs          | COCRDUPC             | 5+ jobs                | **Medium**   |
| 6    | TCATBAL.VSAM.KSDS  | 3 programs          | CBTRN02C, CBACT04C   | 4+ jobs                | **Medium**   |
| 7    | USRSEC.VSAM.KSDS   | 5 programs          | COUSR01C, COUSR02C   | 2 jobs                 | **Medium**   |
| 8    | DISCGRP.VSAM.KSDS  | 1 program           | DISCGRP (IDCAMS)     | 2 jobs                 | **Low**      |
| 9    | TRANTYPE.VSAM.KSDS | 1 program           | TRANTYPE (IDCAMS)    | 2 jobs                 | **Low**      |
| 10   | TRANCATG.VSAM.KSDS | 1 program           | TRANCATG (IDCAMS)    | 2 jobs                 | **Low**      |
