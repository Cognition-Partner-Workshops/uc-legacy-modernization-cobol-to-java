# Dependency Map — CardDemo Mainframe System

> Generated: 2026-05-22 | Source: `uc-legacy-modernization-cobol-to-java`

## 1. Program Call Graph

### 1.1 Online (CICS) Call Graph

```
                              ┌─────────────┐
                              │   CC00       │
                              │  COSGN00C    │
                              │  (Sign-on)   │
                              └──────┬───────┘
                                     │ XCTL
                        ┌────────────┴────────────┐
                        ▼                         ▼
                 ┌─────────────┐          ┌─────────────┐
                 │   CM00      │          │   CA00      │
                 │  COMEN01C   │          │  COADM01C   │
                 │ (User Menu) │          │(Admin Menu) │
                 └──────┬──────┘          └──────┬──────┘
                        │ XCTL                   │ XCTL
           ┌────────────┼────────────┐    ┌──────┼──────────┐
           │            │            │    │      │          │
           ▼            ▼            ▼    ▼      ▼          ▼
     ┌──────────┐ ┌──────────┐ ┌──────┐ ┌────┐ ┌────┐  ┌────────┐
     │  CAVW    │ │  CCLI    │ │ CT00 │ │CU00│ │CU01│  │ CTLI*  │
     │COACTVWC  │ │COCRDLIC  │ │COTRN │ │COUS│ │COUS│  │COTRTLIC│
     │(Acct Vw) │ │(Card Lst)│ │00C   │ │R00C│ │R01C│  │(DB2)   │
     └─────┬────┘ └────┬─────┘ └──┬───┘ └────┘ └────┘  └────────┘
           │           │          │
           ▼           ▼          ▼
     ┌──────────┐ ┌──────────┐ ┌──────┐
     │  CAUP    │ │  CCDL    │ │ CT01 │   Also from Menu:
     │COACTUPC  │ │COCRDSLC  │ │COTRN │   ┌────────────┐
     │(Acct Upd)│ │(Card Vw) │ │01C   │   │CR00/CB00   │
     └──────────┘ └────┬─────┘ └──┬───┘   │CORPT00C    │
                       │          │        │COBIL00C    │
                       ▼          ▼        └────────────┘
                 ┌──────────┐ ┌──────┐
                 │  CCUP    │ │ CT02 │
                 │COCRDUPC  │ │COTRN │
                 │(Card Upd)│ │02C   │
                 └──────────┘ └──────┘
```

**Extension — IMS-DB2-MQ (from Main Menu option 11):**
```
COMEN01C ──XCTL──▶ COPAUS0C (Pending Auth Summary)
                       │ XCTL
                       ▼
                   COPAUS1C (Pending Auth Detail)
                       │ LINK
                       ▼
                   COPAUS2C (Mark Fraud — IMS + DB2)

MQ Trigger ─────▶ COPAUA0C (Authorization Decision — MQ + IMS)
```

**Extension — DB2 Transaction Type (from Admin Menu options 5-6):**
```
COADM01C ──XCTL──▶ COTRTLIC (Tran Type List — DB2 cursor)
COADM01C ──XCTL──▶ COTRTUPC (Tran Type Add/Edit — DB2 insert/update)
```

**Extension — VSAM-MQ (standalone CICS transactions):**
```
CDRD transaction ──▶ CODATE01 (System Date via MQ)
CDRA transaction ──▶ COACCT01 (Account Details via MQ)
```

### 1.2 Batch Call Graph

```
CBSTM03A (Statement Generation)
    │
    ├── CALL 'CBSTM03B'  (File I/O subroutine)
    │       ├── READ TRNXFILE
    │       ├── READ XREFFILE
    │       ├── READ CUSTFILE
    │       └── READ ACCTFILE
    │
    └── WRITE STMTFILE, HTMLFILE

CBACT01C (Account Reader)
    │
    ├── CALL 'COBDATFT'  (Assembler date formatter)
    └── CALL 'CEE3ABD'   (LE abend handler)

CORPT00C (Report Submission from CICS)
    │
    └── CALL 'CSUTLDTC'  (Date validation utility)
             │
             └── CALL 'CEEDAYS' (LE date service)

COTRN02C (Transaction Add — CICS)
    │
    └── CALL 'CSUTLDTC'  (Date validation utility)

COBSWAIT (Batch Wait)
    │
    └── CALL 'MVSWAIT'   (Assembler timer)
```

### 1.3 Complete Call Matrix

| Caller | Callee | Mechanism | Context |
|:-------|:-------|:----------|:--------|
| COSGN00C | COMEN01C | XCTL | User login → User menu |
| COSGN00C | COADM01C | XCTL | Admin login → Admin menu |
| COMEN01C | COACTVWC | XCTL | Menu option 1 |
| COMEN01C | COACTUPC | XCTL | Menu option 2 |
| COMEN01C | COCRDLIC | XCTL | Menu option 3 |
| COMEN01C | COCRDSLC | XCTL | Menu option 4 |
| COMEN01C | COCRDUPC | XCTL | Menu option 5 |
| COMEN01C | COTRN00C | XCTL | Menu option 6 |
| COMEN01C | COTRN01C | XCTL | Menu option 7 |
| COMEN01C | COTRN02C | XCTL | Menu option 8 |
| COMEN01C | CORPT00C | XCTL | Menu option 9 |
| COMEN01C | COBIL00C | XCTL | Menu option 10 |
| COMEN01C | COPAUS0C | XCTL | Menu option 11 (extension) |
| COADM01C | COUSR00C | XCTL | Admin option 1 |
| COADM01C | COUSR01C | XCTL | Admin option 2 |
| COADM01C | COUSR02C | XCTL | Admin option 3 |
| COADM01C | COUSR03C | XCTL | Admin option 4 |
| COADM01C | COTRTLIC | XCTL | Admin option 5 (DB2 extension) |
| COADM01C | COTRTUPC | XCTL | Admin option 6 (DB2 extension) |
| COCRDLIC | COCRDSLC | XCTL | Card list → card detail |
| COCRDLIC | COCRDUPC | XCTL | Card list → card update |
| COPAUS0C | COPAUS1C | XCTL | Auth summary → detail |
| COPAUS1C | COPAUS2C | LINK | Detail → mark fraud |
| CBSTM03A | CBSTM03B | CALL | Statement → file I/O sub |
| CBACT01C | COBDATFT | CALL | Account reader → ASM date |
| CORPT00C | CSUTLDTC | CALL | Report → date validation |
| COTRN02C | CSUTLDTC | CALL | Tran add → date validation |
| CSUTLDTC | CEEDAYS | CALL | Date util → LE service |
| COBSWAIT | MVSWAIT | CALL | Wait → ASM timer |
| All batch | CEE3ABD | CALL | Abend handler (error exit) |

---

## 2. Copybook Inclusion Map

### 2.1 Core Copybook Usage Matrix

| Program ↓ / Copybook → | COCOM01Y | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CSUSR01Y | CVCRD01Y | COTTL01Y | CSDAT01Y | CSMSG01Y | CSMSG02Y |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| COACTUPC | ● | ● | | ● | ● | | ● | ● | ● | ● | ● | ● |
| COACTVWC | ● | ● | ● | ● | ● | | ● | ● | ● | ● | ● | ● |
| COADM01C | ● | | | | | | ● | | ● | ● | ● | |
| COBIL00C | ● | ● | | ● | | ● | | | ● | ● | ● | |
| COCRDLIC | ● | | ● | | | | ● | ● | ● | ● | ● | |
| COCRDSLC | ● | | ● | | ● | | ● | ● | ● | ● | ● | ● |
| COCRDUPC | ● | | ● | | ● | | ● | ● | ● | ● | ● | ● |
| COMEN01C | ● | | | | | | ● | | ● | ● | ● | |
| CORPT00C | ● | | | | | ● | | | ● | ● | ● | |
| COSGN00C | ● | | | | | | ● | | ● | ● | ● | |
| COTRN00C | ● | | | | | ● | | | ● | ● | ● | |
| COTRN01C | ● | | | | | ● | | | ● | ● | ● | |
| COTRN02C | ● | ● | | ● | | ● | | | ● | ● | ● | |
| COUSR00C | ● | | | | | | ● | | ● | ● | ● | |
| COUSR01C | ● | | | | | | ● | | ● | ● | ● | |
| COUSR02C | ● | | | | | | ● | | ● | ● | ● | |
| COUSR03C | ● | | | | | | ● | | ● | ● | ● | |
| CBACT01C | | ● | | | | | | | | | | |
| CBACT02C | | | ● | | | | | | | | | |
| CBACT03C | | | | ● | | | | | | | | |
| CBACT04C | | ● | | ● | | ● | | | | | | |
| CBCUS01C | | | | | ● | | | | | | | |
| CBTRN01C | | ● | ● | ● | ● | ● | | | | | | |
| CBTRN02C | | ● | | ● | | ● | | | | | | |
| CBTRN03C | | | | ● | | ● | | | | | | |
| CBSTM03A | | ● | | ● | | | | | | | | |
| CBEXPORT | | ● | ● | ● | ● | ● | | | | | | |
| CBIMPORT | | ● | ● | ● | ● | ● | | | | | | |

### 2.2 Most-Included Copybooks (ranked)

| Rank | Copybook | # Programs | Role |
|:----:|:---------|:----------:|:-----|
| 1 | `COCOM01Y` | 17 | CICS Commarea (all online programs) |
| 2 | `COTTL01Y` | 17 | Screen titles (all online programs) |
| 3 | `CSDAT01Y` | 17 | Date/time work areas |
| 4 | `CSMSG01Y` | 17 | Common messages |
| 5 | `CVACT01Y` | 12 | Account record layout |
| 6 | `CSUSR01Y` | 13 | User security record |
| 7 | `CVTRA05Y` | 11 | Transaction record layout |
| 8 | `CVACT03Y` | 11 | Card cross-reference |
| 9 | `CVCRD01Y` | 8 | Screen work areas |
| 10 | `CVCUS01Y` | 9 | Customer record layout |

---

## 3. Data Lineage — VSAM File Access

### 3.1 File Access Matrix (Online Programs)

| VSAM File | Read | Write | Rewrite | Delete | Browse (STARTBR/READNEXT/READPREV) |
|:----------|:-----|:------|:--------|:-------|:------------------------------------|
| **USRSEC** | COSGN00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C | COUSR03C | COUSR00C |
| **ACCTDAT** | COACTVWC, COACTUPC, COBIL00C | | COACTUPC, COBIL00C | | |
| **CARDDAT** | COACTVWC, COCRDSLC, COCRDUPC | | COCRDUPC | | COCRDLIC |
| **CUSTDAT** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | | | | |
| **CXREF** (CARDXREF) | COACTVWC, COACTUPC, COTRN02C | | | | |
| **CXACAIX** (AIX) | COBIL00C, COTRN02C | | | | |
| **TRANSACT** | COTRN01C | COTRN02C, COBIL00C | | | COTRN00C, COBIL00C |

### 3.2 File Access Matrix (Batch Programs)

| File | Read By | Written By | Rewritten By |
|:-----|:--------|:-----------|:-------------|
| **ACCTFILE** (VSAM) | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A/B | CBACT01C (out formats) | CBACT04C, CBTRN02C |
| **CARDFILE** (VSAM) | CBACT02C, CBEXPORT | | |
| **CUSTFILE** (VSAM) | CBCUS01C, CBEXPORT, CBSTM03A/B | CBIMPORT | |
| **XREFFILE** (VSAM) | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBSTM03A/B | CBIMPORT | |
| **DALYTRAN** (Sequential) | CBTRN01C, CBTRN02C | | |
| **TRANFILE** (VSAM) | CBEXPORT | CBACT04C, CBTRN02C, CBIMPORT | |
| **TRNXFILE** (VSAM) | CBSTM03A/B, CBTRN03C | | |
| **TCATBALF** (VSAM) | CBACT04C, CBTRN02C | | CBTRN02C |
| **DISCGRP** (VSAM) | CBACT04C | | |
| **DALYREJS** (Sequential) | | CBTRN02C | |
| **STMTFILE** (Sequential) | | CBSTM03A | |
| **HTMLFILE** (Sequential) | | CBSTM03A | |
| **EXPORTFILE** (Sequential) | CBIMPORT | CBEXPORT | |
| **ERRORFILE** (Sequential) | | CBIMPORT | |
| **DATEPARM** (Sequential) | CBTRN03C | | |

---

## 4. JCL Job → Program → File Lineage

### 4.1 Daily Batch Cycle (execution order)

```
CLOSEFIL ──▶ (IEFBR14) ──▶ Close CICS files
    │
    ▼
ACCTFILE ──▶ (IDCAMS)  ──▶ ACCTDATA.PS ──REPRO──▶ ACCTDAT (VSAM)
CARDFILE ──▶ (IDCAMS)  ──▶ CARDDATA.PS ──REPRO──▶ CARDDAT (VSAM)
CUSTFILE ──▶ (IDCAMS)  ──▶ CUSTDATA.PS ──REPRO──▶ CUSTDAT (VSAM)
XREFFILE ──▶ (IDCAMS)  ──▶ CARDXREF.PS ──REPRO──▶ CXREF   (VSAM)
TRANBKP  ──▶ (IDCAMS)  ──▶ TRANSACT    ──REPRO──▶ TRANBKP (GDG)
    │
    ▼
POSTTRAN ──▶ CBTRN02C ──▶ Reads: DALYTRAN, CXREF, ACCTDAT, TCATBALF
    │                      Writes: TRANSACT, DALYREJS
    │                      Rewrites: TCATBALF, ACCTDAT
    ▼
INTCALC  ──▶ CBACT04C ──▶ Reads: TCATBALF, ACCTDAT, CXREF, DISCGRP
    │                      Writes: TRANSACT
    │                      Rewrites: ACCTDAT
    ▼
TRANBKP  ──▶ (IDCAMS)  ──▶ Backup TRANSACT to GDG
    │
    ▼
COMBTRAN ──▶ (SORT)    ──▶ Merge system + daily transactions
    │
    ▼
CREASTMT ──▶ CBSTM03A ──▶ Reads: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE
    │                      Writes: STMTFILE (text), HTMLFILE (HTML)
    │                      Calls: CBSTM03B (file I/O)
    ▼
TRANIDX  ──▶ (IDCAMS)  ──▶ Define AIX on TRANSACT
    │
    ▼
OPENFIL  ──▶ (IEFBR14) ──▶ Open CICS files
```

### 4.2 Report Cycle (triggered from CICS)

```
CORPT00C (CICS) ──WRITEQ TD──▶ JOBS TDQ ──▶ TRANREPT JCL
    │
    ▼
TRANREPT ──▶ CBTRN03C ──▶ Reads: TRANSACT (via TRNXFILE), DATEPARM
                           Reads: XREF (for account lookup)
                           Writes: RPTFILE (report output)
                           References: CVTRA03Y, CVTRA04Y (type/cat descs)
```

### 4.3 Data Migration Cycle

```
CBEXPORT JCL ──▶ CBEXPORT ──▶ Reads: CUSTFILE, ACCTFILE, XREFFILE,
    │                                  TRANFILE, CARDFILE
    │                          Writes: EXPORTFILE (multi-record format)
    ▼
CBIMPORT JCL ──▶ CBIMPORT ──▶ Reads: EXPORTFILE
                               Writes: CUSTFILE, ACCTFILE, XREFFILE,
                                       TRANFILE, CARDFILE, ERRORFILE
```

### 4.4 Extension Job Lineage

**IMS-DB2-MQ Authorization:**
```
LOADPADB ──▶ PAUDBLOD ──▶ Load EBCDIC data ──▶ IMS DB (DBPAUTP0)
UNLDPADB ──▶ PAUDBUNL ──▶ IMS DB ──▶ Unload file
UNLDGSAM ──▶ DBUNLDGS ──▶ IMS DB ──▶ Generic segment dump
CBPAUP0J ──▶ CBPAUP0C ──▶ IMS DB ──▶ Purge expired authorizations
```

**DB2 Transaction Type:**
```
CREADB21 ──▶ DSNTEP4  ──▶ DDL: Create TRNTYPE + TRNTYCAT tables
TRANEXTR ──▶ DSNTIAUL ──▶ DB2 tables ──▶ TRANTYPE.PS, TRANCATG.PS
MNTTRDB2 ──▶ COBTUPDT ──▶ DB2 TRNTYPE table (batch update)
```

---

## 5. Technology Integration Points

### 5.1 External System Dependencies

| Technology | Programs Using It | Purpose |
|:-----------|:-----------------|:--------|
| **CICS** | All 17+ online programs | Transaction processing, BMS maps, COMMAREA |
| **VSAM KSDS** | All programs | Primary data persistence |
| **VSAM AIX** | COBIL00C, COTRN02C, TRANIDX | Alternate index on TRANSACT |
| **IBM MQ** | COPAUA0C, CODATE01, COACCT01 | Asynchronous authorization, inquiries |
| **IMS DB** | COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C, PAUDBLOD, PAUDBUNL, DBUNLDGS | Hierarchical authorization data |
| **DB2** | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT | Relational transaction type/fraud data |
| **SORT** | COMBTRAN JCL | Merge/sort transaction files |
| **IDCAMS** | 15+ JCL jobs | VSAM dataset management |
| **IEBGENER** | DUSRSECJ | Sequential file copy |
| **TDQ** | CORPT00C | Submit batch job from CICS |
| **GDG** | TRANBKP, DEFGDGB/D | Versioned transaction backups |
| **LE (Language Environment)** | CSUTLDTC, all batch programs | CEEDAYS, CEE3ABD services |
| **Assembler** | CBACT01C, COBSWAIT | COBDATFT (date), MVSWAIT (timer) |
| **FTP** | FTPJCL | File transfer |
| **TXT2PDF** | TXT2PDF1 | Report format conversion |

### 5.2 Coupling Heat Map

Programs with the most cross-cutting dependencies (highest coupling risk):

| Program | VSAM Files | Copybooks | Calls/XCTLs | Coupling Score |
|:--------|:----------:|:---------:|:-----------:|:--------------:|
| COACTUPC | 3 | 15+ | 3 | **Very High** |
| CBTRN02C | 4 | 5 | 1 | **High** |
| CBACT04C | 5 | 5 | 1 | **High** |
| COBIL00C | 3 | 9 | 0 | **High** |
| COACTVWC | 4 | 12 | 0 | **High** |
| CBSTM03A | 4 | 4 | 3 | **High** |
| CBEXPORT | 5 | 6 | 0 | **Medium-High** |
| CBIMPORT | 6 | 6 | 1 | **Medium-High** |
| COTRN02C | 3 | 9 | 2 | **Medium-High** |
| COPAUA0C | 3+ | 10+ | IMS+MQ | **Very High** |
