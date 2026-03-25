# DEPENDENCY MAP -- CardDemo Call Graph & Data Lineage

> **Generated:** 2026-03-25  
> **Scope:** All programs, copybooks, JCL jobs, and VSAM files in the CardDemo codebase

---

## 1. Online Program Call Graph (CICS XCTL / CALL)

### 1.1 Navigation Flow

```
                           ┌──────────────┐
                           │  COSGN00C    │
                           │  Sign-on     │
                           │  Tran: CC00  │
                           └──────┬───────┘
                                  │ XCTL (based on user type)
                     ┌────────────┴────────────┐
                     ▼                         ▼
              ┌──────────────┐          ┌──────────────┐
              │  COMEN01C    │          │  COADM01C    │
              │  User Menu   │          │  Admin Menu  │
              │  Tran: CM00  │          │  Tran: CA00  │
              └──────┬───────┘          └──────┬───────┘
                     │ XCTL (option)           │ XCTL (option)
      ┌──────────────┼──────────────┐    ┌─────┴──────────────────┐
      ▼              ▼              ▼    ▼                        ▼
 ┌──────────┐  ┌──────────┐  ...   ┌──────────┐            ┌──────────┐
 │COACTVWC  │  │COCRDLIC  │        │COUSR00C  │            │COTRTLIC  │
 │Acct View │  │Card List │        │User List │            │TranType  │
 └──────────┘  └────┬─────┘        └────┬─────┘            │List(DB2) │
                    │ XCTL              │ XCTL              └──────────┘
              ┌─────┴─────┐       ┌─────┼─────────┐
              ▼           ▼       ▼     ▼         ▼
         ┌─────────┐ ┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐
         │COCRDSLC │ │COCRDUPC │ │COUSR01C│ │COUSR02C│ │COUSR03C│
         │Card View│ │Card Upd │ │User Add│ │User Upd│ │User Del│
         └─────────┘ └─────────┘ └────────┘ └────────┘ └────────┘
```

### 1.2 Complete XCTL (Transfer Control) Matrix

| Source Program | Target Program(s) | Trigger |
|---------------|-------------------|---------|
| **COSGN00C** | COADM01C (admin), COMEN01C (user) | Successful login |
| **COMEN01C** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | Menu option selected |
| **COMEN01C** | COSGN00C | PF3 (exit) |
| **COADM01C** | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | Admin menu option |
| **COADM01C** | COSGN00C | PF3 (exit) |
| **COACTVWC** | COMEN01C (or calling program) | PF3 (back) |
| **COACTUPC** | COMEN01C (or calling program) | PF3 (back) |
| **COCRDLIC** | COCRDSLC (view), COCRDUPC (update) | Row selection S/U |
| **COCRDLIC** | COMEN01C (or calling program) | PF3 (back) |
| **COCRDSLC** | COCRDLIC (or calling program) | PF3 (back) |
| **COCRDUPC** | COCRDLIC (or calling program) | PF3 (back) |
| **COTRN00C** | COTRN01C (view) | Row selection |
| **COTRN00C** | COMEN01C (or calling program) | PF3 (back) |
| **COTRN01C** | COTRN00C (or calling program) | PF3 (back) |
| **COTRN02C** | COMEN01C (or calling program) | PF3 (back) |
| **CORPT00C** | COMEN01C (or calling program) | PF3 (back) |
| **COBIL00C** | COMEN01C (or calling program) | PF3 (back) |
| **COUSR00C** | COUSR01C (add), COUSR02C (update), COUSR03C (delete) | Row selection |
| **COUSR00C** | COADM01C (or calling program) | PF3 (back) |
| **COUSR01C** | COUSR00C (or calling program) | PF3 (back) |
| **COUSR02C** | COUSR00C (or calling program) | PF3 (back) |
| **COUSR03C** | COUSR00C (or calling program) | PF3 (back) |

### 1.3 CALL (Subroutine) Dependencies

| Caller | Called Program | Purpose |
|--------|---------------|---------|
| **COTRN02C** | CSUTLDTC | Date validation (2 calls) |
| **CORPT00C** | CSUTLDTC | Date validation (2 calls) |
| **CSUTLDTC** | CEEDAYS (LE) | IBM Language Environment date service |
| **CBSTM03A** | CBSTM03B | Statement data reader (13 calls for different file operations) |
| **CBSTM03A** | CEE3ABD (LE) | Abend handler |
| **CBACT01C** | COBDATFT (ASM) | Date format conversion |
| **CBACT01C** | CEE3ABD (LE) | Abend handler |
| **CBACT02C** | CEE3ABD (LE) | Abend handler |
| **CBACT03C** | CEE3ABD (LE) | Abend handler |
| **CBCUS01C** | CEE3ABD (LE) | Abend handler |
| **CBTRN01C** | CEE3ABD (LE) | Abend handler |
| **CBTRN02C** | CEE3ABD (LE) | Abend handler |
| **CBTRN03C** | CEE3ABD (LE) | Abend handler |
| **CBACT04C** | CEE3ABD (LE) | Abend handler |
| **CBEXPORT** | CEE3ABD (LE) | Abend handler |
| **CBIMPORT** | CEE3ABD (LE) | Abend handler |

### 1.4 Optional Module Call Dependencies

| Caller | Called Program/API | Purpose |
|--------|-------------------|---------|
| **COPAUA0C** | MQOPEN, MQGET, MQPUT1, MQCLOSE | MQ message processing for auth decisions |
| **COACCT01** | MQOPEN x3, MQGET, MQPUT x2, MQCLOSE x3 | MQ account inquiry request/response |
| **CODATE01** | MQOPEN x3, MQGET, MQPUT x2, MQCLOSE x3 | MQ system date request/response |
| **DBUNLDGS** | CBLTDLI (GN, GNP, ISRT) | IMS DL/I database operations |
| **PAUDBLOD** | CBLTDLI (ISRT, GU) | IMS DL/I database load |
| **PAUDBUNL** | CBLTDLI (GN, GNP) | IMS DL/I database unload |
| **COPAUS0C** | COMEN01C (via XCTL) | Navigation back to menu |
| **COPAUS1C** | COPAUS0C (via XCTL) | Navigation back to summary |
| **COTRTLIC** | COADM01C (via XCTL) | Navigation back to admin menu |
| **COTRTUPC** | COADM01C (via XCTL) | Navigation back to admin menu |

---

## 2. Copybook Inclusion Map

### 2.1 Shared Copybook Usage Matrix

| Copybook | Used By (Programs) | Usage Count |
|----------|--------------------|-------------|
| **COCOM01Y** (Commarea) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C-03C, COPAUS0C-1C, COTRTLIC, COTRTUPC | **20** |
| **COTTL01Y** (Titles) | All 17 core online programs + COPAUS0C-1C + COTRTLIC + COTRTUPC | **21** |
| **CSDAT01Y** (Date/Time) | All 17 core online programs + COPAUS0C-1C + COTRTLIC + COTRTUPC | **21** |
| **CSMSG01Y** (Messages) | All 17 core online programs + COPAUS0C-1C + COTRTLIC + COTRTUPC | **21** |
| **CSUSR01Y** (User Sec) | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C, COPAUS0C-1C, COTRTLIC, COTRTUPC | **17** |
| **DFHAID** (CICS AID) | All online programs | **21** |
| **DFHBMSCA** (BMS attrs) | All online programs | **21** |
| **CVACT01Y** (Account) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBTRN02C, CBACT04C, CBTRN01C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C, COACCT01 | **14** |
| **CVACT02Y** (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUS0C, COTRTLIC | **10** |
| **CVACT03Y** (Xref) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C | **13** |
| **CVCUS01Y** (Customer) | COACTVWC, COCRDSLC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUS0C | **7** |
| **CVTRA05Y** (Transaction) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBTRN02C, CBACT04C, CBTRN01C, CBTRN03C, CBEXPORT, CBIMPORT | **11** |
| **CVTRA06Y** (Daily Tran) | CBTRN02C, CBTRN01C | **2** |
| **CVCRD01Y** (CC Work) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC | **6** |
| **CSMSG02Y** (Abend) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COPAUS0C-1C, COTRTUPC | **6** |

### 2.2 Batch Program Copybook Usage

| Batch Program | Copybooks Used |
|---------------|----------------|
| **CBTRN02C** | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **CBACT04C** | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **CBTRN03C** | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **CBSTM03A** | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **CBTRN01C** | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| **CBEXPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| **CBIMPORT** | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| **CBACT01C** | CVACT01Y, CODATECN |
| **CBACT02C** | CVACT02Y |
| **CBACT03C** | CVACT03Y |
| **CBCUS01C** | CVCUS01Y |

---

## 3. VSAM File Access Map (Data Lineage)

### 3.1 Online Programs -- VSAM File Access

| Program | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | TRANSACT | CCXREF | CXACAIX |
|---------|--------|---------|---------|---------|---------|----------|--------|---------|
| COSGN00C | **R** | | | | | | | |
| COMEN01C | R | | | | | | | |
| COACTVWC | | **R** | | **R** | **R** | | | **R** |
| COACTUPC | | **RW** | | **R** | **R** | | | **R** |
| COCRDLIC | | | **R** | **R** | | | | |
| COCRDSLC | | | **R** | **R** | R | | | |
| COCRDUPC | | | **RW** | **R** | | | | |
| COTRN00C | | | | | | **R** | | |
| COTRN01C | | | | | | **R** | | |
| COTRN02C | | R | | | | **RW** | **R** | **R** |
| CORPT00C | | | | | | **R** | | |
| COBIL00C | | **RW** | | | | **RW** | | **R** |
| COUSR00C | **R** | | | | | | | |
| COUSR01C | **W** | | | | | | | |
| COUSR02C | **RW** | | | | | | | |
| COUSR03C | **RD** | | | | | | | |

**Legend:** R=Read, W=Write, RW=Read+Write/Update, RD=Read+Delete

### 3.2 Batch Programs -- File Access

| Program | DALYTRAN | TRANSACT/TRANFILE | ACCTFILE | XREFFILE/CARDXREF | CUSTFILE | CARDFILE | TCATBALF | DISCGRP | DALYREJS | TRANTYPE | TRANCATG | TRANREPT | STMTFILE | HTMLFILE | EXPFILE |
|---------|----------|-------------------|----------|-------------------|----------|----------|----------|---------|----------|----------|----------|----------|----------|----------|---------|
| CBTRN02C | **R** | **W** | **RW** | **R** | | | **RW** | | **W** | | | | | | |
| CBACT04C | | **W** | **RW** | **R** | | | **R** | **R** | | | | | | | |
| CBTRN03C | | **R** | | **R** | | | | | | **R** | **R** | **W** | | | |
| CBSTM03A | | | **R** | **R** | **R** | | | | | | | | **W** | **W** | |
| CBSTM03B | | **R** | **R** | **R** | **R** | | | | | | | | | | |
| CBTRN01C | **R** | **W** | **RW** | **R** | **R** | **R** | | | | | | | | | |
| CBEXPORT | | **R** | **R** | **R** | **R** | **R** | | | | | | | | | **W** |
| CBIMPORT | | | | | | | | | | | | | | | **R** |
| CBACT01C | | | **R** | | | | | | | | | | | | |
| CBACT02C | | | | | | **R** | | | | | | | | | |
| CBACT03C | | | | **R** | | | | | | | | | | | |
| CBCUS01C | | | | | **R** | | | | | | | | | | |

### 3.3 JCL Job -- File Lineage

Shows which JCL jobs create, load, or consume each VSAM file:

```
VSAM File: ACCTDAT (Account Master)
  DEFINED BY:  ACCTFILE.jcl (IDCAMS DEFINE)
  LOADED BY:   ACCTFILE.jcl (IDCAMS REPRO from PS)
  READ BY:     POSTTRAN → CBTRN02C, INTCALC → CBACT04C,
               CREASTMT → CBSTM03A, READACCT → CBACT01C,
               CBEXPORT → CBEXPORT
  UPDATED BY:  POSTTRAN → CBTRN02C, INTCALC → CBACT04C

VSAM File: CARDDAT (Card Master)
  DEFINED BY:  CARDFILE.jcl (IDCAMS DEFINE + AIX)
  LOADED BY:   CARDFILE.jcl (IDCAMS REPRO from PS)
  READ BY:     READCARD → CBACT02C, CBEXPORT → CBEXPORT
  AIX:         CARDAIX (alternate index on CARD-ACCT-ID)

VSAM File: CUSTDAT (Customer Master)
  DEFINED BY:  CUSTFILE.jcl (IDCAMS DEFINE)
  LOADED BY:   CUSTFILE.jcl (IDCAMS REPRO from PS)
  READ BY:     CREASTMT → CBSTM03A, READCUST → CBCUS01C,
               CBEXPORT → CBEXPORT

VSAM File: TRANSACT (Transaction Master)
  DEFINED BY:  TRANFILE.jcl (IDCAMS DEFINE + AIX)
  LOADED BY:   TRANFILE.jcl (IDCAMS REPRO from PS)
  WRITTEN BY:  POSTTRAN → CBTRN02C, INTCALC → CBACT04C
  READ BY:     TRANREPT → CBTRN03C, CREASTMT → CBSTM03A/B,
               CBEXPORT → CBEXPORT
  BACKED UP:   TRANBKP.jcl (REPROC to sequential)
  COMBINED:    COMBTRAN.jcl (SORT + REPRO)
  REINDEXED:   TRANIDX.jcl (AIX rebuild)

VSAM File: CCXREF/CXACAIX (Card Cross-Reference)
  DEFINED BY:  XREFFILE.jcl (IDCAMS DEFINE + AIX)
  LOADED BY:   XREFFILE.jcl (IDCAMS REPRO from PS)
  READ BY:     POSTTRAN → CBTRN02C, INTCALC → CBACT04C,
               TRANREPT → CBTRN03C, CREASTMT → CBSTM03A/B,
               READXREF → CBACT03C, CBEXPORT → CBEXPORT

VSAM File: USRSEC (User Security)
  DEFINED BY:  DUSRSECJ.jcl (IDCAMS DEFINE)
  LOADED BY:   DUSRSECJ.jcl (IEBGENER + IDCAMS REPRO)
  READ BY:     COSGN00C (login), COUSR00C (list), COUSR02C (update),
               COUSR03C (delete)

VSAM File: DALYTRAN (Daily Transaction Feed)
  LOADED BY:   External feed (sequential input)
  READ BY:     POSTTRAN → CBTRN02C, CBTRN01C

VSAM File: TCATBALF (Category Balance)
  DEFINED BY:  TCATBALF.jcl
  READ BY:     POSTTRAN → CBTRN02C, INTCALC → CBACT04C
  UPDATED BY:  POSTTRAN → CBTRN02C

VSAM File: DISCGRP (Discount Groups)
  DEFINED BY:  DISCGRP.jcl, DEFGDGD.jcl
  READ BY:     INTCALC → CBACT04C

VSAM File: TRANTYPE (Transaction Types)
  DEFINED BY:  TRANTYPE.jcl, DEFGDGD.jcl
  READ BY:     TRANREPT → CBTRN03C

VSAM File: TRANCATG (Transaction Categories)
  DEFINED BY:  TRANCATG.jcl, DEFGDGD.jcl
  READ BY:     TRANREPT → CBTRN03C
```

---

## 4. Batch Job Execution Sequence

### 4.1 Nightly Batch Cycle (Production Order)

```
Step 1: CLOSEFIL.jcl        Close CICS-managed VSAM files
           │
Step 2: Data Refresh (parallel)
           ├── ACCTFILE.jcl     Refresh account VSAM
           ├── CARDFILE.jcl     Refresh card VSAM
           ├── CUSTFILE.jcl     Refresh customer VSAM
           ├── XREFFILE.jcl     Refresh xref VSAM
           └── TRANFILE.jcl     Refresh transaction VSAM
           │
Step 3: POSTTRAN.jcl         Post daily transactions
           │                   CBTRN02C: DALYTRAN → TRANSACT
           │                             Updates ACCTFILE, TCATBALF
           │                             Creates DALYREJS (rejects)
           │
Step 4: INTCALC.jcl          Calculate interest
           │                   CBACT04C: Reads TCATBALF, XREFFILE,
           │                             ACCTFILE, DISCGRP
           │                             Writes interest to TRANSACT
           │
Step 5: TRANBKP.jcl          Backup transactions
           │                   REPROC + IDCAMS: TRANSACT → sequential
           │
Step 6: COMBTRAN.jcl          Combine transaction files
           │                   SORT + IDCAMS: Merge + reload TRANSACT
           │
Step 7: CREASTMT.JCL          Generate statements
           │                   SORT + CBSTM03A: Reads all files
           │                                    Writes STMTFILE, HTMLFILE
           │
Step 8: TRANREPT.jcl          Transaction report
           │                   SORT + CBTRN03C: Reads TRANFILE, CARDXREF,
           │                                    TRANTYPE, TRANCATG
           │                                    Writes TRANREPT
           │
Step 9: TRANIDX.jcl           Rebuild transaction AIX
           │
Step 10: OPENFIL.jcl          Reopen CICS files
```

### 4.2 JCL-to-Program Execution Map

| JCL Job | Step | Program | Type |
|---------|------|---------|------|
| POSTTRAN | STEP15 | CBTRN02C | COBOL batch |
| INTCALC | STEP15 | CBACT04C | COBOL batch |
| CREASTMT | STEP010 | SORT | Utility |
| CREASTMT | STEP040 | CBSTM03A | COBOL batch |
| TRANREPT | STEP05R | REPROC (proc) | JCL proc |
| TRANREPT | STEP05R | SORT | Utility |
| TRANREPT | STEP10R | CBTRN03C | COBOL batch |
| READACCT | STEP05 | CBACT01C | COBOL batch |
| READCARD | STEP05 | CBACT02C | COBOL batch |
| READCUST | STEP05 | CBCUS01C | COBOL batch |
| READXREF | STEP05 | CBACT03C | COBOL batch |
| CBEXPORT | STEP02 | CBEXPORT | COBOL batch |
| CBIMPORT | STEP01 | CBIMPORT | COBOL batch |
| WAITSTEP | WAIT | COBSWAIT | COBOL utility |
| TRANBKP | STEP05R | REPROC (proc) | JCL proc |
| PRTCATBL | STEP05R | REPROC (proc) | JCL proc |
| PRTCATBL | STEP10R | SORT | Utility |

---

## 5. Program-to-BMS Map Binding

| Program | Mapset | Map | Send/Receive |
|---------|--------|-----|-------------|
| COSGN00C | COSGN00 | COSGN0A | Send + Receive |
| COMEN01C | COMEN01 | COMEN1A | Send + Receive |
| COADM01C | COADM01 | COADM1A | Send + Receive |
| COACTVWC | COACTVW | CACTVWA | Send + Receive |
| COACTUPC | COACTUP | CACTUPA | Send + Receive |
| COCRDLIC | COCRDLI | CCRDLIA | Send + Receive |
| COCRDSLC | COCRDSL | CCRDSLA | Send + Receive |
| COCRDUPC | COCRDUP | CCRDUPA | Send + Receive |
| COTRN00C | COTRN00 | COTRN0A | Send + Receive |
| COTRN01C | COTRN01 | COTRN1A | Send + Receive |
| COTRN02C | COTRN02 | COTRN2A | Send + Receive |
| CORPT00C | CORPT00 | CORPT0A | Send + Receive |
| COBIL00C | COBIL00 | COBIL0A | Send + Receive |
| COUSR00C | COUSR00 | COUSR0A | Send + Receive |
| COUSR01C | COUSR01 | COUSR1A | Send + Receive |
| COUSR02C | COUSR02 | COUSR2A | Send + Receive |
| COUSR03C | COUSR03 | COUSR3A | Send + Receive |

---

## 6. Cross-Cutting Concerns

### 6.1 Programs That Share the COMMAREA (Session State)

Every online CICS program includes `COCOM01Y` and participates in the COMMAREA chain. The flow is:

```
COSGN00C sets: CDEMO-USER-ID, CDEMO-USER-TYPE, CDEMO-FROM-TRANID, CDEMO-FROM-PROGRAM
      │
      ▼
Menu programs route based on: CDEMO-USER-TYPE, CDEMO-PGM-CONTEXT
      │
      ▼
Detail programs use: CDEMO-ACCT-ID, CDEMO-CARD-NUM, CDEMO-CUST-ID
      │
      ▼
All programs track: CDEMO-LAST-MAP, CDEMO-LAST-MAPSET for back-navigation
```

### 6.2 Date Handling Chain

```
COTRN02C / CORPT00C
    │
    │ CALL 'CSUTLDTC'
    ▼
CSUTLDTC.cbl (date validation)
    │
    │ CALL 'CEEDAYS'
    ▼
IBM LE Date Services
```

### 6.3 Statement Generation Chain

```
CREASTMT.JCL
    │
    ├── SORT (sort transactions by card number)
    ├── IDCAMS (load sorted data to VSAM)
    │
    └── CBSTM03A (driver)
           │
           │ CALL 'CBSTM03B' (13 times for different file ops)
           ▼
        CBSTM03B (reader)
           │
           ├── Opens: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE
           └── Returns data to CBSTM03A
                  │
                  └── Writes: STMTFILE (text), HTMLFILE (HTML)
```

### 6.4 Export/Import Data Flow

```
CBEXPORT.jcl → CBEXPORT
    │
    │ Reads ALL master files:
    │   CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE
    │
    │ Writes unified export:
    └── EXPFILE (sequential, record types: C/A/X/T)
           │
           │ Transported to target branch
           ▼
    CBIMPORT.jcl → CBIMPORT
           │
           │ Reads EXPFILE
           │ Splits into:
           ├── CUSTOUT (customers)
           ├── ACCTOUT (accounts)
           ├── XREFOUT (cross-references)
           ├── TRNXOUT (transactions)
           └── ERROUT  (rejected records)
```
