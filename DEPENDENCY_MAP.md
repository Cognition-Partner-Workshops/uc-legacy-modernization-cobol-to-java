# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Source**: Static analysis of CALL, EXEC CICS XCTL/LINK, COPY, JCL EXEC PGM, and DD DSN statements

## 1. Program Call Graph

### 1.1 Online CICS Navigation Flow

The CardDemo application uses CICS `XCTL` (transfer control) to navigate between programs. The COMMAREA (`COCOM01Y`) carries session state across transfers.

```
                            ┌─────────────┐
                            │  COSGN00C   │
                            │  (Signon)   │
                            └──────┬──────┘
                                   │ XCTL
                       ┌───────────┴───────────┐
                       │                       │
                ┌──────┴──────┐         ┌──────┴──────┐
                │  COMEN01C   │         │  COADM01C   │
                │ (User Menu) │         │(Admin Menu) │
                └──────┬──────┘         └──────┬──────┘
                       │ XCTL                  │ XCTL
        ┌──────┬───────┼───────┬───────┐       │
        │      │       │       │       │       ├──> COUSR00C (User List)
        ▼      ▼       ▼       ▼       ▼       ├──> COUSR01C (User Add)
   COACTVWC COCRDLIC COTRN00C CORPT00C COBIL00C├──> COUSR02C (User Update)
   (AcctVw) (CardLs) (TrnLst) (Report) (Bill)  └──> COUSR03C (User Delete)
        │      │       │
        │      ▼       ▼
        │  COCRDSLC COTRN01C
        │  (CardVw) (TrnVw)
        │      │
        ▼      ▼
   COACTUPC COCRDUPC COTRN02C
   (AcctUpd)(CardUpd)(TrnAdd)
```

### 1.2 Online Program-to-Program Calls (XCTL/LINK)

| Source Program | Target Program | Mechanism | Context |
|---|---|---|---|
| **COSGN00C** | COMEN01C | XCTL | Regular user login |
| **COSGN00C** | COADM01C | XCTL | Admin user login |
| **COMEN01C** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | XCTL | Menu option selection (via `CDEMO-MENU-OPT-PGMNAME` table) |
| **COADM01C** | COUSR00C, COUSR01C, COUSR02C, COUSR03C | XCTL | Admin menu option selection (via `CDEMO-ADMIN-OPT-PGMNAME` table) |
| **COCRDLIC** | COCRDSLC | XCTL | Card list → card detail drill-down |
| **COTRN02C** | CSUTLDTC | CALL | Date validation during transaction add |
| **CORPT00C** | CSUTLDTC | CALL | Date validation for report parameters |
| All online programs | COSGN00C | XCTL | Return to signon (session end / PF3 from menu) |

### 1.3 Batch Program CALL Graph

```
CBSTM03A ──CALL──> CBSTM03B    (Statement generation calls file I/O subroutine)
CBSTM03A ──CALL──> CEE3ABD     (LE abend handler)
CBACT01C ──CALL──> COBDATFT    (ASM date format conversion)
CBACT01C ──CALL──> CEE3ABD     (LE abend handler)
CBACT02C ──CALL──> CEE3ABD     (LE abend handler)
CBACT03C ──CALL──> CEE3ABD     (LE abend handler)
CBACT04C ──CALL──> CEE3ABD     (LE abend handler)
CBCUS01C ──CALL──> CEE3ABD     (LE abend handler)
CBTRN01C ──CALL──> CEE3ABD     (LE abend handler)
CBTRN02C ──CALL──> CEE3ABD     (LE abend handler)
CBTRN03C ──CALL──> CEE3ABD     (LE abend handler)
CBEXPORT ──CALL──> CEE3ABD     (LE abend handler)
CBIMPORT ──CALL──> CEE3ABD     (LE abend handler)
COBSWAIT ──CALL──> MVSWAIT     (ASM MVS wait service)
CSUTLDTC ──CALL──> CEEDAYS    (LE date validation API)
```

### 1.4 Full Caller/Callee Matrix

| Program | Calls (outbound) | Called By (inbound) |
|---|---|---|
| COSGN00C | COMEN01C, COADM01C | All online programs (return) |
| COMEN01C | 10 online programs (via menu table) | COSGN00C |
| COADM01C | COUSR00C-03C (via admin table) | COSGN00C |
| COACTVWC | -- | COMEN01C |
| COACTUPC | -- | COMEN01C |
| COCRDLIC | COCRDSLC | COMEN01C |
| COCRDSLC | -- | COCRDLIC, COMEN01C |
| COCRDUPC | -- | COMEN01C |
| COTRN00C | -- | COMEN01C |
| COTRN01C | -- | COMEN01C |
| COTRN02C | CSUTLDTC | COMEN01C |
| CORPT00C | CSUTLDTC | COMEN01C |
| COBIL00C | -- | COMEN01C |
| COUSR00C | -- | COADM01C |
| COUSR01C | -- | COADM01C |
| COUSR02C | -- | COADM01C |
| COUSR03C | -- | COADM01C |
| CSUTLDTC | CEEDAYS (LE API) | COTRN02C, CORPT00C |
| CBSTM03A | CBSTM03B, CEE3ABD | CREASTMT (JCL) |
| CBSTM03B | -- | CBSTM03A |
| CBACT01C | COBDATFT, CEE3ABD | READACCT (JCL) |
| CBACT02C | CEE3ABD | READCARD (JCL) |
| CBACT03C | CEE3ABD | READXREF (JCL) |
| CBACT04C | CEE3ABD | INTCALC (JCL) |
| CBCUS01C | CEE3ABD | READCUST (JCL) |
| CBTRN01C | CEE3ABD | -- |
| CBTRN02C | CEE3ABD | POSTTRAN (JCL) |
| CBTRN03C | CEE3ABD | TRANREPT (JCL) |
| CBEXPORT | CEE3ABD | CBEXPORT (JCL) |
| CBIMPORT | CEE3ABD | CBIMPORT (JCL) |
| COBSWAIT | MVSWAIT (ASM) | WAITSTEP (JCL) |

---

## 2. Copybook Inclusion Map

### 2.1 Copybook Usage by Program

| Copybook | Used By Programs | Usage Count |
|---|---|---|
| **COCOM01Y** | All 17 online programs | 17 |
| **COTTL01Y** | All 17 online programs | 17 |
| **CSDAT01Y** | All 17 online programs | 17 |
| **CSMSG01Y** | All 17 online programs | 17 |
| **DFHAID** | All 17 online programs | 17 |
| **DFHBMSCA** | All 17 online programs | 17 |
| **CSUSR01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | 13 |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CVACT01Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A | 11 |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT | 8 |
| **CVACT03Y** | COACTUPC, COACTVWC, COBIL00C, COCRDSLC, COCRDUPC, COTRN02C, CBACT04C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A | 12 |
| **CVCUS01Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | 8 |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | 10 |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | 2 |
| **CVTRA01Y** | CBACT04C, CBTRN02C | 2 |
| **CVTRA02Y** | CBACT04C | 1 |
| **CVTRA03Y** | CBTRN03C | 1 |
| **CVTRA04Y** | CBTRN03C | 1 |
| **CVTRA07Y** | CBTRN03C | 1 |
| **CVEXPORT** | CBEXPORT, CBIMPORT | 2 |
| **COSTM01** | CBSTM03A | 1 |
| **CUSTREC** | CBSTM03A | 1 |
| **COMEN02Y** | COMEN01C | 1 |
| **COADM02Y** | COADM01C | 1 |
| **CSLKPCDY** | COACTUPC, COCRDUPC | 2 |
| **CODATECN** | CBACT01C | 1 |
| **CSSETATY** | (online screen attribute utility) | -- |
| **CSSTRPFY** | (string prefix utility) | -- |
| **CSUTLDPY** | (date utility parameters) | -- |
| **CSUTLDWY** | (date utility working storage) | -- |

### 2.2 BMS Map Copybook Inclusion

Each online program includes its BMS-generated copybook from `app/cpy-bms/`:

| Program | BMS Copybook (from `cpy-bms/`) |
|---|---|
| COSGN00C | COSGN00.CPY |
| COMEN01C | COMEN01.CPY |
| COADM01C | COADM01.CPY |
| COACTVWC | COACTVW.CPY |
| COACTUPC | COACTUP.CPY |
| COCRDLIC | COCRDLI.CPY |
| COCRDSLC | COCRDSL.CPY |
| COCRDUPC | COCRDUP.CPY |
| COTRN00C | COTRN00.CPY |
| COTRN01C | COTRN01.CPY |
| COTRN02C | COTRN02.CPY |
| CORPT00C | CORPT00.CPY |
| COBIL00C | COBIL00.CPY |
| COUSR00C | COUSR00.CPY |
| COUSR01C | COUSR01.CPY |
| COUSR02C | COUSR02.CPY |
| COUSR03C | COUSR03.CPY |

---

## 3. VSAM File Access Map (Data Lineage)

### 3.1 Online Programs -- CICS File Access

| Program | VSAM File | Operations | CICS File Name |
|---|---|---|---|
| **COSGN00C** | USRSEC | READ | USRSEC |
| **COACTVWC** | ACCTDATA, CUSTDATA, CARDXREF, CARDDATA | READ | (via literal names) |
| **COACTUPC** | ACCTDATA, CUSTDATA | READ, REWRITE | LIT-ACCTFILENAME, LIT-CUSTFILENAME |
| **COCRDLIC** | CARDDATA, CARDXREF | READ, STARTBR, READNEXT | (via literal names) |
| **COCRDSLC** | CARDDATA, ACCTDATA, CUSTDATA, CARDXREF | READ | (via literal names) |
| **COCRDUPC** | CARDDATA | READ, REWRITE | LIT-CARDFILENAME |
| **COTRN00C** | TRANSACT | READ, STARTBR, READNEXT | (via literal names) |
| **COTRN01C** | TRANSACT | READ | (via literal names) |
| **COTRN02C** | TRANSACT, ACCTDATA, CARDXREF | READ, WRITE, STARTBR | (via literal names) |
| **COBIL00C** | ACCTDATA, CARDXREF, TRANSACT | READ, WRITE, REWRITE, STARTBR | (via literal names) |
| **COUSR00C** | USRSEC | READ, STARTBR, READNEXT | USRSEC |
| **COUSR01C** | USRSEC | WRITE | USRSEC |
| **COUSR02C** | USRSEC | READ, REWRITE | USRSEC |
| **COUSR03C** | USRSEC | READ, DELETE | USRSEC |

### 3.2 Batch Programs -- File Access

| Program | File (DD Name) | Dataset | Access Mode | Operations |
|---|---|---|---|---|
| **CBACT01C** | ACCTFILE | ACCTDATA.VSAM.KSDS | Indexed/Sequential | READ |
| | OUTFILE | ACCTDATA.PSCOMP | Sequential | WRITE |
| | ARRYFILE | ACCTDATA.ARRYPS | Sequential | WRITE |
| | VBRCFILE | ACCTDATA.VBPS | Sequential | WRITE |
| **CBACT02C** | CARDFILE | CARDDATA.VSAM.KSDS | Indexed/Sequential | READ |
| **CBACT03C** | XREFFILE | CARDXREF.VSAM.KSDS | Indexed/Sequential | READ |
| **CBACT04C** | ACCTFILE | ACCTDATA.VSAM.KSDS | Indexed | READ, REWRITE |
| | XREF-FILE | CARDXREF.VSAM.KSDS | Indexed | READ |
| | TRANSACT | SYSTRAN (output) | Sequential | WRITE |
| | DISCGRP | DISCGRP.VSAM.KSDS | Indexed | READ |
| | TCATBALF | TCATBALF.VSAM.KSDS | Indexed | READ, REWRITE |
| **CBCUS01C** | CUSTFILE | CUSTDATA.VSAM.KSDS | Indexed/Sequential | READ |
| **CBTRN01C** | DALYTRAN | DALYTRAN.PS | Sequential | READ |
| | TRANSACT-FILE | TRANSACT.VSAM.KSDS | Indexed | READ, WRITE |
| | ACCOUNT-FILE | ACCTDATA.VSAM.KSDS | Indexed | READ |
| | XREF-FILE | CARDXREF.VSAM.KSDS | Indexed | READ |
| | CARD-FILE | CARDDATA.VSAM.KSDS | Indexed | READ |
| | CUSTOMER-FILE | CUSTDATA.VSAM.KSDS | Indexed | READ |
| **CBTRN02C** | DALYTRAN | DALYTRAN.PS | Sequential | READ |
| | TRANFILE | TRANSACT.VSAM.KSDS | Indexed | READ, WRITE |
| | ACCTFILE | ACCTDATA.VSAM.KSDS | Indexed | READ, REWRITE |
| | XREF-FILE | CARDXREF.VSAM.KSDS | Indexed | READ |
| | TCATBALF | TCATBALF.VSAM.KSDS | Indexed | READ, REWRITE |
| | DALYREJS | DALYREJS | Sequential | WRITE |
| **CBTRN03C** | TRANFILE | TRANSACT.VSAM.KSDS | Indexed | READ |
| | CARDXREF | CARDXREF.VSAM.KSDS | Indexed | READ |
| | TRANTYPE | TRANTYPE.VSAM.KSDS | Indexed | READ |
| | TRANCATG | TRANCATG.VSAM.KSDS | Indexed | READ |
| | TRANREPT | TRANREPT | Sequential | WRITE |
| | DATEPARM | DATEPARM | Sequential | READ |
| **CBSTM03A/B** | TRNXFILE | TRXFL.VSAM.KSDS | Indexed/Sequential | READ |
| | XREFFILE | CARDXREF.VSAM.KSDS | Indexed | READ |
| | ACCTFILE | ACCTDATA.VSAM.KSDS | Indexed | READ |
| | CUSTFILE | CUSTDATA.VSAM.KSDS | Indexed | READ |
| | STMTFILE | STATEMNT.PS | Sequential | WRITE |
| | HTMLFILE | STATEMNT.HTML | Sequential | WRITE |
| **CBEXPORT** | ACCTFILE | ACCTDATA.VSAM.KSDS | Indexed | READ |
| | CARDFILE | CARDDATA.VSAM.KSDS | Indexed | READ |
| | CUSTFILE | CUSTDATA.VSAM.KSDS | Indexed | READ |
| | XREFFILE | CARDXREF.VSAM.KSDS | Indexed | READ |
| | TRANSACT | TRANSACT.VSAM.KSDS | Indexed | READ |
| | EXPFILE | EXPORT.DATA | Sequential | WRITE |
| **CBIMPORT** | EXPFILE | EXPORT.DATA | Sequential | READ |
| | ACCTOUT | ACCTDATA.IMPORT | Sequential | WRITE |
| | CUSTOUT | CUSTDATA.IMPORT | Sequential | WRITE |
| | CARDOUT | -- | Sequential | WRITE |
| | XREFOUT | CARDXREF.IMPORT | Sequential | WRITE |
| | TRNXOUT | TRANSACT.IMPORT | Sequential | WRITE |
| | ERROUT | IMPORT.ERRORS | Sequential | WRITE |

---

## 4. JCL Job -- Dataset Lineage

### 4.1 Complete Dataset Read/Write Map

| Dataset (Short Name) | Written By (JCL Job / Program) | Read By (JCL Job / Program) |
|---|---|---|
| **ACCTDATA.VSAM.KSDS** | ACCTFILE (IDCAMS REPRO) | READACCT/CBACT01C, INTCALC/CBACT04C, POSTTRAN/CBTRN02C, CREASTMT/CBSTM03A, CBEXPORT/CBEXPORT, COACTVWC, COACTUPC, COBIL00C |
| **CARDDATA.VSAM.KSDS** | CARDFILE (IDCAMS REPRO) | READCARD/CBACT02C, CBEXPORT, COCRDLIC, COCRDSLC, COCRDUPC |
| **CUSTDATA.VSAM.KSDS** | CUSTFILE (IDCAMS REPRO) | READCUST/CBCUS01C, CREASTMT/CBSTM03A, CBEXPORT, COACTVWC, COCRDSLC |
| **CARDXREF.VSAM.KSDS** | XREFFILE (IDCAMS REPRO) | READXREF/CBACT03C, INTCALC/CBACT04C, POSTTRAN/CBTRN02C, TRANREPT/CBTRN03C, CREASTMT/CBSTM03A, CBEXPORT, COCRDLIC, COBIL00C, COTRN02C |
| **TRANSACT.VSAM.KSDS** | TRANFILE (IDCAMS), POSTTRAN/CBTRN02C | TRANBKP (backup), TRANREPT/CBTRN03C, CREASTMT, CBEXPORT, COTRN00C, COTRN01C, COTRN02C, COBIL00C |
| **DALYTRAN.PS** | (External: daily feed) | POSTTRAN/CBTRN02C |
| **USRSEC.VSAM.KSDS** | DUSRSECJ (IDCAMS REPRO) | COSGN00C, COUSR00C-03C |
| **TCATBALF.VSAM.KSDS** | TCATBALF (IDCAMS REPRO) | INTCALC/CBACT04C, POSTTRAN/CBTRN02C |
| **DISCGRP.VSAM.KSDS** | DISCGRP (IDCAMS REPRO) | INTCALC/CBACT04C |
| **TRANTYPE.VSAM.KSDS** | TRANTYPE (IDCAMS REPRO) | TRANREPT/CBTRN03C |
| **TRANCATG.VSAM.KSDS** | TRANCATG (IDCAMS REPRO) | TRANREPT/CBTRN03C |
| **TRANSACT.BKUP** | TRANBKP (IDCAMS REPRO) | COMBTRAN (SORT input) |
| **TRANSACT.COMBINED** | COMBTRAN (SORT) | -- |
| **SYSTRAN** | INTCALC/CBACT04C | COMBTRAN (SORT input) |
| **DALYREJS** | POSTTRAN/CBTRN02C | -- |
| **STATEMNT.PS** | CREASTMT/CBSTM03A | TXT2PDF1 |
| **STATEMNT.HTML** | CREASTMT/CBSTM03A | -- |
| **EXPORT.DATA** | CBEXPORT/CBEXPORT | CBIMPORT/CBIMPORT |
| **TRANREPT** | TRANREPT/CBTRN03C | -- |
| **DATEPARM** | (External: config) | TRANREPT/CBTRN03C |

### 4.2 Nightly Batch Data Flow

```
                    External Feed
                         │
                    DALYTRAN.PS
                         │
                    ┌────┴────┐
                    │POSTTRAN │ (CBTRN02C)
                    └────┬────┘
                    ┌────┴─────────────────────────┐
                    │                              │
              TRANSACT.VSAM.KSDS           DALYREJS
              ACCTDATA.VSAM.KSDS (updated)  (rejects)
              TCATBALF.VSAM.KSDS (updated)
                    │
              ┌─────┴─────┐
              │  INTCALC   │ (CBACT04C)
              └─────┬──────┘
                    │
              ACCTDATA.VSAM.KSDS (interest applied)
              SYSTRAN (interest transactions)
                    │
              ┌─────┴─────┐
              │  TRANBKP   │ (IDCAMS)
              └─────┬──────┘
                    │
              TRANSACT.BKUP
                    │
              ┌─────┴─────┐
              │ COMBTRAN   │ (SORT + IDCAMS)
              └─────┬──────┘
                    │
              TRANSACT.COMBINED
                    │
         ┌──────────┼──────────┐
         │          │          │
   ┌─────┴────┐ ┌──┴───┐ ┌───┴────┐
   │CREASTMT  │ │TRNRPT│ │PRTCATBL│
   │(CBSTM03A)│ │(03C) │ │(SORT)  │
   └─────┬────┘ └──┬───┘ └───┬────┘
         │         │          │
    STATEMNT.PS  TRANREPT  TCATBALF.REPT
    STATEMNT.HTML
         │
   ┌─────┴────┐
   │TXT2PDF1  │
   └─────┬────┘
         │
    STATEMNT.PS.PDF
```

---

## 5. JCL Job Dependency Chain

### 5.1 Nightly Batch Cycle (Ordered)

```
Step 1:  CLOSEFIL ────────────── Close CICS files (no dataset dependency)
Step 2:  ACCTFILE ────────────── ACCTDATA.PS → ACCTDATA.VSAM.KSDS
Step 3:  CARDFILE ────────────── CARDDATA.PS → CARDDATA.VSAM.KSDS
Step 4:  CUSTFILE ────────────── CUSTDATA.PS → CUSTDATA.VSAM.KSDS
Step 5:  XREFFILE ────────────── CARDXREF.PS → CARDXREF.VSAM.KSDS (+AIX)
         ↓ (all data refreshed)
Step 6:  POSTTRAN ────────────── Requires: DALYTRAN, ACCTDATA, CARDXREF, TCATBALF, TRANSACT
         ↓
Step 7:  INTCALC  ────────────── Requires: ACCTDATA, CARDXREF, DISCGRP, TCATBALF
         ↓                       Produces: SYSTRAN
Step 8:  TRANBKP  ────────────── Requires: TRANSACT.VSAM.KSDS
         ↓                       Produces: TRANSACT.BKUP
Step 9:  COMBTRAN ────────────── Requires: TRANSACT.BKUP + SYSTRAN
         ↓                       Produces: TRANSACT.COMBINED → TRANSACT.VSAM.KSDS
Step 10: CREASTMT ────────────── Requires: TRANSACT, CARDXREF, ACCTDATA, CUSTDATA
         ↓                       Produces: STATEMNT.PS + STATEMNT.HTML
Step 11: TRANIDX  ────────────── Rebuild alternate indexes on TRANSACT
Step 12: OPENFIL  ────────────── Reopen CICS files
```

### 5.2 Job-to-Job Dependencies

| Job | Must Run After | Must Run Before | Reason |
|---|---|---|---|
| CLOSEFIL | -- | All batch processing | CICS files must be closed |
| ACCTFILE | CLOSEFIL | POSTTRAN, INTCALC | Account data must be refreshed |
| CARDFILE | CLOSEFIL | -- | Card data refreshed |
| CUSTFILE | CLOSEFIL | CREASTMT | Customer data refreshed |
| XREFFILE | CLOSEFIL | POSTTRAN, INTCALC | Cross-reference must exist |
| POSTTRAN | ACCTFILE, XREFFILE | INTCALC | Transactions must be posted first |
| INTCALC | POSTTRAN | TRANBKP | Interest calculated on posted data |
| TRANBKP | INTCALC | COMBTRAN | Backup before combining |
| COMBTRAN | TRANBKP | CREASTMT | Combine backup + interest txns |
| CREASTMT | COMBTRAN, CUSTFILE | TRANIDX | Statements from combined data |
| TRANIDX | CREASTMT | OPENFIL | Indexes rebuilt before online access |
| OPENFIL | TRANIDX | -- | CICS access restored |

---

## 6. Technology Interface Dependencies

| Interface | Programs Using It | External System |
|---|---|---|
| CICS (BMS screens) | All 17 online programs | 3270 Terminal |
| VSAM KSDS | All programs | z/OS VSAM |
| Language Environment (CEE3ABD) | All batch programs | z/OS LE |
| CEEDAYS API | CSUTLDTC | z/OS LE Date Services |
| SDSF (System Display) | CLOSEFIL, OPENFIL, CARDFILE, CUSTFILE, TRANFILE | z/OS SDSF |
| SORT (DFSORT/SYNCSORT) | COMBTRAN, CREASTMT, TRANREPT, PRTCATBL | z/OS SORT |
| IDCAMS | 20+ JCL jobs | z/OS VSAM Management |
| FTP | FTPJCL | External FTP Server |
| Internal Reader | INTRDRJ1 → INTRDRJ2 | z/OS JES2 |
| TXT2PDF | TXT2PDF1 | z/OS PDF Utility |
| IMS DB | COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C, PAUDBLOD/PAUDBUNL | IMS (optional module) |
| DB2 | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT | DB2 (optional module) |
| MQ | COPAUA0C, COACCT01, CODATE01 | IBM MQ (optional module) |
