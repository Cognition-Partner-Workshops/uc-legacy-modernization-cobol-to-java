# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
> **Scope**: Program call graph, CICS transfer control flow, copybook inclusion, JCL data lineage

---

## 1. Online CICS Program Call Graph

### 1.1 Navigation Flow (XCTL Transfer Control)

All online navigation flows through COMMAREA (COCOM01Y) via `EXEC CICS XCTL`.

```
                              ┌─────────────┐
                              │  COSGN00C   │  Sign-on (CC00)
                              │  (Entry)    │
                              └──────┬──────┘
                                     │ XCTL
                              ┌──────▼──────┐
                    ┌─────────│  COMEN01C   │──────────┐
                    │         │  Main Menu  │          │
                    │         │  (CM00)     │          │
                    │         └──────┬──────┘          │
                    │                │                  │
         ┌──────────┼────────┬───────┼──────┬──────────┼──────────┐
         ▼          ▼        ▼       ▼      ▼          ▼          ▼
    ┌─────────┐ ┌────────┐ ┌─────┐ ┌─────┐ ┌────────┐ ┌────────┐ ┌────────┐
    │COACTVWC │ │COCRDLIC│ │COTRN│ │CORPT│ │COBIL00C│ │COADM01C│ │COTRN02C│
    │Acct View│ │Card Lst│ │00C  │ │00C  │ │Bill Pay│ │Admin   │ │Trn Add │
    └────┬────┘ └───┬────┘ │Trn  │ │Rpt  │ └────────┘ └───┬────┘ └────────┘
         │          │      │List │ │Req  │                 │
         ▼          ▼      └──┬──┘ └─────┘        ┌────────┼────────┐
    ┌─────────┐ ┌────────┐    │                    ▼        ▼        ▼
    │COACTUPC │ │COCRDSLC│    ▼               ┌────────┐┌────────┐┌────────┐
    │Acct Upd │ │Card Dtl│ ┌────────┐         │COUSR00C││COUSR01C││COUSR02C│
    └─────────┘ └───┬────┘ │COTRN01C│         │Usr List││Usr Add ││Usr Upd │
                    │      │Trn Dtl │         └───┬────┘└────────┘└────────┘
                    ▼      └────────┘             │
               ┌────────┐                        ▼
               │COCRDUPC│                    ┌────────┐
               │Card Upd│                    │COUSR03C│
               └────────┘                    │Usr Del │
                                             └────────┘
```

### 1.2 Detailed XCTL Transfers

| Source Program | Target Program(s)                          | Transfer Mechanism        |
|----------------|--------------------------------------------|---------------------------|
| COSGN00C       | COMEN01C, COADM01C                         | EXEC CICS XCTL (role-based)|
| COMEN01C       | COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C, COADM01C, COTRN02C | XCTL via CDEMO-MENU-OPT-PGMNAME table |
| COADM01C       | COUSR00C, COUSR01C, COUSR02C, COMEN01C    | XCTL via CDEMO-ADMIN-OPT-PGMNAME table |
| COACTVWC       | COMEN01C (return), COACTUPC (edit)         | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COACTUPC       | COMEN01C (return)                          | EXEC CICS XCTL            |
| COCRDLIC       | COCRDSLC (view), COCRDUPC (edit), COMEN01C | EXEC CICS XCTL            |
| COCRDSLC       | COMEN01C (return), COCRDLIC (back)         | EXEC CICS XCTL            |
| COCRDUPC       | COMEN01C (return)                          | EXEC CICS XCTL            |
| COTRN00C       | COTRN01C (view), COMEN01C (return)         | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COTRN01C       | COMEN01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COTRN02C       | COMEN01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| CORPT00C       | COMEN01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COBIL00C       | COMEN01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COUSR00C       | COUSR01C (add), COUSR02C (edit), COUSR03C (del), COADM01C (return) | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COUSR01C       | COADM01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COUSR02C       | COADM01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |
| COUSR03C       | COADM01C (return)                          | XCTL PROGRAM(CDEMO-TO-PROGRAM) |

---

## 2. Batch Program Call Graph

### 2.1 CALL Relationships

```
CBSTM03A (Statement Generator)
    └── CALL 'CBSTM03B' (File I/O Subroutine)
            Passes: WS-M03B-AREA (DD name, operation, return code)
            Operations: Open, Close, Read, Read-Key, Write, Rewrite

CBTRN02C (Transaction Posting)
    └── CALL 'CSUTLDTC' (Date Validation)
            Passes: WS-DATE-TO-TEST, WS-DATE-FORMAT
            Returns: OUTPUT-LILLIAN (Lilian date), FEEDBACK-CODE

CBACT04C (Interest Calculation)
    └── CALL 'CSUTLDTC' (Date Validation)
            Passes: Date parameters for interest period calculation

CBTRN03C (Transaction Report)
    └── (No external CALL — self-contained)

CBEXPORT / CBIMPORT
    └── (No external CALL — self-contained file I/O)
```

### 2.2 Batch CALL Summary Table

| Caller       | Called Program | Interface                     | Purpose                       |
|-------------|----------------|-------------------------------|-------------------------------|
| CBSTM03A    | CBSTM03B       | WS-M03B-AREA (DD, op, RC)    | File open/close/read/write    |
| CBTRN02C    | CSUTLDTC       | Date/format/Lilian/feedback   | Validate transaction dates    |
| CBACT04C    | CSUTLDTC       | Date/format/Lilian/feedback   | Validate interest calc dates  |

---

## 3. Copybook Inclusion Map

### 3.1 Core Copybook Usage Matrix

| Copybook    | Programs Using It                                                                    | Count |
|-------------|--------------------------------------------------------------------------------------|-------|
| COCOM01Y    | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| COTTL01Y    | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 16 |
| CSDAT01Y    | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 16 |
| CSMSG01Y    | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 16 |
| CSUSR01Y    | COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 14 |
| DFHAID      | All 17 online CICS programs                                                          | 17 |
| DFHBMSCA    | All 17 online CICS programs                                                          | 17 |
| CVACT01Y    | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBSTM03A                                    | 5  |
| CVACT02Y    | COACTVWC, COCRDLIC, COCRDSLC, CBACT02C                                              | 4  |
| CVACT03Y    | COACTVWC, COACTUPC, CBACT04C, CBTRN03C, CBSTM03A                                   | 5  |
| CVCUS01Y    | COACTVWC, COACTUPC, COCRDSLC, CBCUS01C                                              | 4  |
| CVTRA05Y    | COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBTRN02C, CBTRN03C, CBACT04C               | 7  |
| CVTRA06Y    | CBTRN01C, CBTRN02C                                                                  | 2  |
| CVCRD01Y    | COACTVWC, COCRDLIC, COCRDSLC                                                        | 3  |
| CVTRA01Y    | CBACT04C                                                                             | 1  |
| CVTRA02Y    | CBACT04C                                                                             | 1  |
| CVTRA03Y    | CBTRN03C                                                                             | 1  |
| CVTRA04Y    | CBTRN03C                                                                             | 1  |
| CVTRA07Y    | CBTRN03C                                                                             | 1  |
| CVEXPORT    | CBEXPORT, CBIMPORT                                                                   | 2  |
| CSSTRPFY    | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC                                              | 4  |
| CSSETATY    | COACTUPC (×37 REPLACING instances), COCRDUPC                                        | 2  |
| CSUTLDPY    | COACTUPC                                                                             | 1  |
| CSUTLDWY    | (used via CSUTLDPY)                                                                  | 1  |
| CSLKPCDY    | (lookup code reference — 51 KB)                                                      | 1  |
| CODATECN    | CBACT01C                                                                             | 1  |
| COSTM01     | CBSTM03A                                                                             | 1  |
| CUSTREC     | CBSTM03A                                                                             | 1  |
| COMEN02Y    | COMEN01C                                                                             | 1  |
| COADM02Y    | COADM01C                                                                             | 1  |
| CSMSG02Y    | COACTVWC, COACTUPC, COCRDSLC                                                        | 3  |

### 3.2 BMS Map → Program → BMS Copybook Mapping

| BMS Map       | BMS Copybook   | Program     | Screen Function         |
|---------------|----------------|-------------|-------------------------|
| COSGN00.bms   | COSGN00.CPY    | COSGN00C    | Sign-on                 |
| COMEN01.bms   | COMEN01.CPY    | COMEN01C    | Main Menu               |
| COADM01.bms   | COADM01.CPY    | COADM01C    | Admin Menu              |
| COACTVW.bms   | COACTVW.CPY    | COACTVWC    | Account View            |
| COACTUP.bms   | COACTUP.CPY    | COACTUPC    | Account Update          |
| COCRDLI.bms   | COCRDLI.CPY    | COCRDLIC    | Card List               |
| COCRDSL.bms   | COCRDSL.CPY    | COCRDSLC    | Card Detail View        |
| COCRDUP.bms   | COCRDUP.CPY    | COCRDUPC    | Card Update             |
| COTRN00.bms   | COTRN00.CPY    | COTRN00C    | Transaction List        |
| COTRN01.bms   | COTRN01.CPY    | COTRN01C    | Transaction Detail      |
| COTRN02.bms   | COTRN02.CPY    | COTRN02C    | Transaction Add         |
| CORPT00.bms   | CORPT00.CPY    | CORPT00C    | Report Request          |
| COBIL00.bms   | COBIL00.CPY    | COBIL00C    | Bill Payment            |
| COUSR00.bms   | COUSR00.CPY    | COUSR00C    | User List               |
| COUSR01.bms   | COUSR01.CPY    | COUSR01C    | User Add                |
| COUSR02.bms   | COUSR02.CPY    | COUSR02C    | User Update             |
| COUSR03.bms   | COUSR03.CPY    | COUSR03C    | User Delete             |

---

## 4. VSAM File Access Map

### 4.1 Online CICS File Access (via EXEC CICS READ/WRITE/REWRITE/DELETE)

| Program      | VSAM File(s) Accessed                      | Operations                     |
|-------------|--------------------------------------------|---------------------------------|
| COSGN00C    | USRSEC                                     | READ                           |
| COMEN01C    | (none — navigation only)                   | —                              |
| COADM01C    | (none — navigation only)                   | —                              |
| COACTVWC    | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA     | READ                           |
| COACTUPC    | ACCTDATA, CUSTDATA, CARDXREF               | READ, REWRITE                  |
| COCRDLIC    | CARDDATA                                   | READ, STARTBR, READNEXT, READPREV |
| COCRDSLC    | CARDDATA, CUSTDATA                         | READ                           |
| COCRDUPC    | CARDDATA                                   | READ, REWRITE                  |
| COTRN00C    | TRANSACT                                   | STARTBR, READNEXT, READPREV   |
| COTRN01C    | TRANSACT                                   | READ                           |
| COTRN02C    | TRANSACT, CARDXREF                         | READ, WRITE, STARTBR, READPREV|
| CORPT00C    | TRANSACT (via TD queue)                    | WRITEQ TD                     |
| COBIL00C    | TRANSACT, ACCTDATA, CARDXREF               | READ, REWRITE, WRITE, STARTBR, READPREV |
| COUSR00C    | USRSEC                                     | STARTBR, READNEXT, READPREV   |
| COUSR01C    | USRSEC                                     | WRITE                          |
| COUSR02C    | USRSEC                                     | READ, REWRITE                  |
| COUSR03C    | USRSEC                                     | READ, DELETE                   |

### 4.2 Batch File Access (via SELECT/ASSIGN TO + File I/O verbs)

| Program      | Input Files                                      | Output Files                              |
|-------------|--------------------------------------------------|-------------------------------------------|
| CBACT01C    | ACCTDATA (VSAM KSDS)                            | Sequential output (PSCOMP, ARRYPS, VBPS)  |
| CBACT02C    | CARDDATA (VSAM KSDS)                            | (display output)                           |
| CBACT03C    | CARDXREF (VSAM KSDS)                            | (display output)                           |
| CBACT04C    | TRANSACT, CARDXREF, DISCGRP, ACCTDATA, TCATBALF | ACCTDATA (update), TCATBALF (update)       |
| CBCUS01C    | CUSTDATA (VSAM KSDS)                            | (display output)                           |
| CBTRN01C    | DALYTRAN, CARDXREF, CARDDATA, ACCTDATA, TRANSACT| (validation — no output files)             |
| CBTRN02C    | DALYTRAN, CARDXREF, ACCTDATA, TCATBALF           | TRANSACT, ACCTDATA (upd), TCATBALF (upd), DALYREJS |
| CBTRN03C    | TRANSACT, CARDXREF, TRANTYPE, TRANCATG          | Report output (print)                      |
| CBSTM03A    | (delegates to CBSTM03B)                          | STMTFILE (text), HTMLFILE (HTML)           |
| CBSTM03B    | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE           | (data provided to CBSTM03A)               |
| CBEXPORT    | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPFILE (unified export)                   |
| CBIMPORT    | EXPFILE (unified import)                         | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA, ERROUT |
| COBSWAIT    | (none)                                           | (none — timer only)                        |
| CSUTLDTC    | (none — in-memory date validation)               | (none)                                     |

---

## 5. JCL Job → Program → File Data Lineage

### 5.1 Batch Processing Cycle Data Flow

```
                    ┌──────────────────────────────────────────┐
                    │           NIGHTLY BATCH CYCLE             │
                    └──────────────────────────────────────────┘

Step 1: CLOSEFIL ─── Close CICS files (SDSF) ───────────────────────────────

Step 2: Data Refresh (parallel)
  ACCTFILE ──── IDCAMS ──── AWS.M2.CARDDEMO.ACCTDATA.PS ──► ACCTDATA.VSAM.KSDS
  CARDFILE ──── IDCAMS ──── AWS.M2.CARDDEMO.CARDDATA.PS ──► CARDDATA.VSAM.KSDS + AIX
  CUSTFILE ──── IDCAMS ──── AWS.M2.CARDDEMO.CUSTDATA.PS ──► CUSTDATA.VSAM.KSDS
  XREFFILE ──── IDCAMS ──── AWS.M2.CARDDEMO.CARDXREF.PS ──► CARDXREF.VSAM.KSDS + AIX
  TRANFILE ──── IDCAMS ──── AWS.M2.CARDDEMO.TRANSACT.PS ──► TRANSACT.VSAM.KSDS + AIX
  DUSRSECJ ──── IEBGENER/IDCAMS ─── USRSEC.PS ────────────► USRSEC.VSAM.KSDS

Step 3: POSTTRAN ─── CBTRN02C
  Input:  DALYTRAN.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, TCATBAL.VSAM.KSDS
  Output: TRANSACT.VSAM.KSDS (new transactions), ACCTDATA (balance updates),
          TCATBAL (category balance updates), DALYREJS (rejected transactions)

Step 4: INTCALC ─── CBACT04C
  Input:  TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, DISCGRP.VSAM.KSDS,
          ACCTDATA.VSAM.KSDS, TCATBAL.VSAM.KSDS
  Output: ACCTDATA (interest posted), TCATBAL (interest category balance)

Step 5: TRANBKP ─── IDCAMS REPRO
  Input:  TRANSACT.VSAM.KSDS
  Output: TRANSACT.BKUP.PS (sequential backup)

Step 6: COMBTRAN ─── SORT + IDCAMS
  Input:  TRANSACT.VSAM.KSDS, DALYTRAN.VSAM.KSDS
  Output: TRANSACT.VSAM.KSDS (merged/combined)

Step 7: CREASTMT ─── SORT + CBSTM03A/CBSTM03B
  Input:  TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS,
          CUSTDATA.VSAM.KSDS
  Output: STATEMNT.PS (text statements), STATEMNT.HTML (HTML statements)

Step 8: TRANIDX ─── IDCAMS
  Rebuild alternate indexes on TRANSACT.VSAM.KSDS

Step 9: OPENFIL ─── Re-open CICS files (SDSF) ──────────────────────────────
```

### 5.2 JCL Job → Dataset (DSN) Read/Write Matrix

| JCL Job      | Datasets Read (Input)                              | Datasets Written (Output)                          |
|-------------|----------------------------------------------------|----------------------------------------------------|
| ACCTFILE    | ACCTDATA.PS                                        | ACCTDATA.VSAM.KSDS                                |
| CARDFILE    | CARDDATA.PS                                        | CARDDATA.VSAM.KSDS, CARDDATA.VSAM.AIX             |
| CUSTFILE    | CUSTDATA.PS                                        | CUSTDATA.VSAM.KSDS                                |
| XREFFILE    | CARDXREF.PS                                        | CARDXREF.VSAM.KSDS, CARDXREF.VSAM.AIX             |
| TRANFILE    | TRANSACT.PS                                        | TRANSACT.VSAM.KSDS, TRANSACT.VSAM.AIX             |
| DUSRSECJ    | USRSEC.PS                                          | USRSEC.VSAM.KSDS                                  |
| POSTTRAN    | DALYTRAN, CARDXREF, ACCTDATA, TCATBAL              | TRANSACT, ACCTDATA, TCATBAL, DALYREJS             |
| INTCALC     | TRANSACT, CARDXREF, DISCGRP, ACCTDATA, TCATBAL     | ACCTDATA, TCATBAL                                 |
| COMBTRAN    | TRANSACT, DALYTRAN                                 | TRANSACT (merged)                                  |
| CREASTMT    | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA             | STATEMNT.PS, STATEMNT.HTML, TRXFL (temp)          |
| TRANBKP     | TRANSACT.VSAM.KSDS                                | TRANSACT.BKUP.PS                                  |
| TRANREPT    | TRANSACT, CARDXREF, TRANTYPE, TRANCATG             | Report output (SYSOUT)                             |
| TRANIDX     | TRANSACT.VSAM.KSDS                                | TRANSACT alternate indexes                         |
| READACCT    | ACCTDATA.VSAM.KSDS                                | ACCTDATA.PSCOMP, ACCTDATA.ARRYPS, ACCTDATA.VBPS   |
| READCARD    | CARDDATA.VSAM.KSDS                                | (display)                                          |
| READCUST    | CUSTDATA.VSAM.KSDS                                | (display)                                          |
| READXREF    | CARDXREF.VSAM.KSDS                                | (display)                                          |
| CBEXPORT    | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA   | EXPFILE                                            |
| CBIMPORT    | EXPFILE                                            | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA   |
| TRANTYPE    | TRANTYPE.PS                                        | TRANTYPE.VSAM.KSDS                                |
| TRANCATG    | TRANCATG.PS                                        | TRANCATG.VSAM.KSDS                                |
| TCATBALF    | TCATBAL.PS                                         | TCATBAL.VSAM.KSDS                                 |
| DISCGRP     | DISCGRP.PS                                         | DISCGRP.VSAM.KSDS                                 |
| TXT2PDF1    | STATEMNT.PS                                        | STATEMNT.PS.PDF                                    |
| WAITSTEP    | (none)                                             | (none — delay only)                                |
| DEFGDGB     | (none)                                             | GDG base definitions                               |
| DEFGDGD     | TRANTYPE.PS, TRANCATG.PS, DISCGRP.PS              | GDG generation backups                             |
| PRTCATBL    | TCATBAL.VSAM.KSDS                                 | Report output (SORT)                               |
| DALYREJS    | (none)                                             | DALYREJS.VSAM.KSDS (definition only)              |

---

## 6. Data Flow by Business Entity

### 6.1 Account Data Flow

```
Source Data (ACCTDATA.PS)
    │
    ▼ [ACCTFILE JCL]
ACCTDATA.VSAM.KSDS
    │
    ├── READ by: COACTVWC, COACTUPC, COBIL00C (online)
    ├── READ by: CBACT01C, CBACT04C, CBTRN02C, CBSTM03B, CBEXPORT (batch)
    ├── REWRITE by: COACTUPC, COBIL00C (online)
    ├── REWRITE by: CBTRN02C (posting updates balance)
    ├── REWRITE by: CBACT04C (interest posted to balance)
    │
    └── EXTRACT by: CBACT01C → ACCTDATA.PSCOMP / ARRYPS / VBPS
```

### 6.2 Transaction Data Flow

```
Daily Input (DALYTRAN staging file)
    │
    ▼ [POSTTRAN JCL → CBTRN02C]
    ├── Valid txns → TRANSACT.VSAM.KSDS
    ├── Rejected → DALYREJS
    └── Balance updates → ACCTDATA, TCATBAL
         │
         ├── [INTCALC JCL → CBACT04C] → Interest entries → ACCTDATA, TCATBAL
         │
         ├── [TRANBKP JCL] → TRANSACT.BKUP.PS (backup)
         │
         ├── [COMBTRAN JCL] → Merge with daily into master
         │
         ├── [TRANREPT JCL → CBTRN03C] → Daily Transaction Report
         │
         └── [CREASTMT JCL → CBSTM03A] → Statement (text + HTML)
                                              │
                                              └── [TXT2PDF1] → PDF
```

### 6.3 Card Data Flow

```
Source Data (CARDDATA.PS)
    │
    ▼ [CARDFILE JCL]
CARDDATA.VSAM.KSDS + Alternate Index
    │
    ├── READ by: COCRDLIC, COCRDSLC, COCRDUPC (online)
    ├── READ by: CBACT02C, CBTRN01C, CBEXPORT (batch)
    ├── REWRITE by: COCRDUPC (online)
    │
    └── Cross-referenced via CARDXREF.VSAM.KSDS
            │
            ├── Links Card → Account → Customer
            └── Used by: COACTVWC, COACTUPC, COBIL00C, COTRN02C (online)
                         CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C, CBSTM03B (batch)
```

### 6.4 User Security Data Flow

```
Source Data (USRSEC.PS)
    │
    ▼ [DUSRSECJ JCL]
USRSEC.VSAM.KSDS
    │
    ├── READ by: COSGN00C (authentication)
    ├── BROWSE by: COUSR00C (user list)
    ├── WRITE by: COUSR01C (add user)
    ├── REWRITE by: COUSR02C (update user)
    └── DELETE by: COUSR03C (delete user)
```

---

## 7. Optional Module Dependencies

### 7.1 Authorization Module (IMS/DB2/MQ)

```
MQ Request Queue
    │
    ▼ [COPAUA0C — MQ Trigger]
    │
    ├── COPAUS0C (Summary) ──► IMS DB (PADFLPCB, PASFLPCB) ──► BMS COPAU00
    ├── COPAUS1C (Detail)  ──► IMS DB (PAUTBPCB)           ──► BMS COPAU01
    └── COPAUS2C (Fraud)   ──► DB2 AUTHFRDS table          ──► XAUTHFRD index
    
Batch:
    CBPAUP0C ──► DB2 AUTHFRDS (purge processed records)
    PAUDBLOD ──► IMS DB (load from flat file)
    PAUDBUNL ──► IMS DB (unload to flat file)
    DBUNLDGS ──► IMS DB (unload via GS call)
```

### 7.2 Transaction Type DB2 Module

```
CICS Online:
    COTRTLIC (List/Delete) ──► DB2 TRNTYPE, TRNTYCAT tables ──► BMS COTRTLI
    COTRTUPC (Add/Edit)    ──► DB2 TRNTYPE, TRNTYCAT tables ──► BMS COTRTUP

Batch:
    COBTUPDT ──► DB2 TRNTYPE, TRNTYCAT tables (batch update)

DDL/DCL:
    TRNTYPE.ddl   → Transaction Type table definition
    TRNTYCAT.ddl  → Transaction Type Category table definition
    DCLTRTYP.dcl  → COBOL DCLGEN for TRNTYPE
    DCLTRCAT.dcl  → COBOL DCLGEN for TRNTYCAT
```

### 7.3 VSAM-MQ Module

```
MQ Request/Response:
    CODATE01 ──► MQ queues (CDRD) ──► Returns system date
    COACCT01 ──► MQ queues (CDRA) ──► Returns account inquiry data
```

---

## 8. Shared Infrastructure Dependencies

### 8.1 CICS System Copybooks (not in repo — provided by CICS runtime)

| Copybook     | Purpose                                    | Used By        |
|--------------|--------------------------------------------|----------------|
| DFHAID       | AID key definitions (PF keys, ENTER, etc.) | All 17 online  |
| DFHBMSCA     | BMS attribute constants                    | All 17 online  |
| DFHEIBLK     | EXEC Interface Block                       | All CICS pgms  |
| DFHCOMMAREA  | Communication Area linkage                 | All CICS pgms  |

### 8.2 Language Environment (LE) Dependencies

| Service      | Used By        | Purpose                                |
|--------------|----------------|----------------------------------------|
| CEEDAYS      | CSUTLDTC       | Convert date to Lilian format          |
| DSNTIAC      | CSDB2RPY       | DB2 error message formatting           |

---

## 9. Dependency Counts Summary

| Program      | Copybooks | Files Accessed | Programs Called/Transferred | Complexity Indicator |
|-------------|-----------|----------------|---------------------------|----------------------|
| COACTUPC    | 14        | 3              | 1 (XCTL return)           | Very High            |
| COACTVWC    | 13        | 4              | 2 (XCTL)                  | High                 |
| COCRDLIC    | 10        | 1              | 3 (XCTL)                  | High                 |
| COCRDUPC    | 10        | 1              | 1 (XCTL)                  | High                 |
| COCRDSLC    | 12        | 2              | 2 (XCTL)                  | High                 |
| CBTRN02C    | 6         | 6              | 1 (CALL)                  | Very High            |
| CBACT04C    | 5         | 5              | 1 (CALL)                  | High                 |
| CBSTM03A    | 4         | 0 (via 03B)    | 1 (CALL)                  | High                 |
| CBTRN03C    | 5         | 4              | 0                         | High                 |
| CBEXPORT    | 1         | 6              | 0                         | Medium               |
| CBIMPORT    | 1         | 7              | 0                         | Medium               |
| COTRN00C    | 9         | 1              | 2 (XCTL)                  | Medium               |
| COTRN02C    | 9         | 2              | 1 (XCTL)                  | Medium               |
| COBIL00C    | 9         | 3              | 1 (XCTL)                  | Medium               |
| COUSR00C    | 7         | 1              | 3 (XCTL)                  | Medium               |
| COMEN01C    | 9         | 0              | 7 (XCTL menu)             | Low (navigation)     |
