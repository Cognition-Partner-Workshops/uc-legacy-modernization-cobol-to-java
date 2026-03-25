# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Scope:** Program-to-program call graph, CICS transfer control flow, copybook inclusion matrix, and VSAM data lineage

---

## Table of Contents

1. [Online (CICS) Navigation Flow](#1-online-cics-navigation-flow)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [CICS Program Transfer (XCTL) Map](#3-cics-program-transfer-xctl-map)
4. [Program-to-Subroutine CALL Map](#4-program-to-subroutine-call-map)
5. [Copybook Inclusion Matrix](#5-copybook-inclusion-matrix)
6. [VSAM File Access Map — Online Programs](#6-vsam-file-access-map--online-programs)
7. [VSAM File Access Map — Batch Programs](#7-vsam-file-access-map--batch-programs)
8. [JCL Job → Program Execution Map](#8-jcl-job--program-execution-map)
9. [JCL Job → Dataset Map (Data Lineage)](#9-jcl-job--dataset-map-data-lineage)
10. [Batch Processing Sequence (Job Flow)](#10-batch-processing-sequence-job-flow)
11. [BMS Map → Program → Copybook Traceability](#11-bms-map--program--copybook-traceability)
12. [Cross-Cutting Dependency Summary](#12-cross-cutting-dependency-summary)

---

## 1. Online (CICS) Navigation Flow

The CICS online application uses `EXEC CICS XCTL` (transfer control) to navigate between programs. The common communication area (`COCOM01Y`) carries context between programs.

```
                              ┌─────────────┐
                              │  COSGN00C   │
                              │  Sign On    │
                              └──────┬──────┘
                                     │ XCTL (on successful login)
                              ┌──────▼──────┐
                   ┌──────────│  COMEN01C   │──────────┐
                   │          │  Main Menu  │          │
                   │          └──────┬──────┘          │
                   │                 │                  │
          (Regular User)        (Any User)        (Admin User)
                   │                 │                  │
         ┌─────────┴────┐    ┌──────┴──────┐   ┌──────▼──────┐
         │              │    │             │    │  COADM01C   │
         ▼              ▼    ▼             ▼    │  Admin Menu │
    ┌─────────┐  ┌─────────┐ ┌────────┐ ┌────────┐ └──────┬──────┘
    │COACTVWC │  │COCRDLIC │ │COTRN00C│ │COBIL00C│        │
    │Acct View│  │Card List│ │Trn List│ │Bill Pay│        ▼
    └────┬────┘  └────┬────┘ └───┬────┘ └────────┘  ┌─────────┐
         │            │          │                    │COUSR00C │
         ▼            ▼          ▼                    │User List│
    ┌─────────┐  ┌─────────┐ ┌────────┐              └────┬────┘
    │COACTUPC │  │COCRDSLC │ │COTRN01C│                   │
    │Acct Upd │  │Card View│ │Trn View│         ┌────────┬┴────────┐
    └─────────┘  └────┬────┘ └────────┘         ▼        ▼         ▼
                      ▼          ▲          ┌────────┐┌────────┐┌────────┐
                 ┌─────────┐     │          │COUSR01C││COUSR02C││COUSR03C│
                 │COCRDUPC │  ┌──┴─────┐    │Add User││Upd User││Del User│
                 │Card Upd │  │COTRN02C│    └────────┘└────────┘└────────┘
                 └─────────┘  │Trn Add │
                              └────────┘
                                  │
                              ┌───▼─────┐
                              │CORPT00C │
                              │Reports  │
                              └─────────┘
```

### Navigation Rules
- **COSGN00C** → XCTL to **COMEN01C** (regular user) or **COADM01C** (admin user) on successful authentication
- **COMEN01C** dispatches via menu option table (`COMEN02Y`) to target program
- **COADM01C** dispatches via admin option table (`COADM02Y`) to target program
- All programs can XCTL back to their parent menu via `CDEMO-TO-PROGRAM` field in `COCOM01Y`
- PF3 key typically returns to the calling menu

---

## 2. Batch Program Call Graph

```
┌───────────────────────────────────────────────────────────────────┐
│                     Batch Program Calls                           │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  CBSTM03A ──CALL──▶ CBSTM03B  (file I/O subroutine)             │
│                                                                   │
│  CBACT01C ──CALL──▶ COBDATFT  (assembler date formatter)         │
│                                                                   │
│  CORPT00C ──CALL──▶ CSUTLDTC  (date validation utility)          │
│  COTRN02C ──CALL──▶ CSUTLDTC  (date validation utility)          │
│                                                                   │
│  CSUTLDTC ──CALL──▶ CEEDAYS   (LE date intrinsic)               │
│  COBSWAIT ──CALL──▶ MVSWAIT   (assembler wait SVC)              │
│                                                                   │
│  CBACT01C ──CALL──▶ CEE3ABD   (LE abnormal termination)          │
│  CBACT02C ──CALL──▶ CEE3ABD                                      │
│  CBACT03C ──CALL──▶ CEE3ABD                                      │
│  CBACT04C ──CALL──▶ CEE3ABD                                      │
│  CBCUS01C ──CALL──▶ CEE3ABD                                      │
│  CBTRN01C ──CALL──▶ CEE3ABD                                      │
│  CBTRN02C ──CALL──▶ CEE3ABD                                      │
│  CBTRN03C ──CALL──▶ CEE3ABD                                      │
│  CBSTM03A ──CALL──▶ CEE3ABD                                      │
│  CBEXPORT ──CALL──▶ CEE3ABD                                      │
│  CBIMPORT ──CALL──▶ CEE3ABD                                      │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

### Call Categories

| Call Type | Caller(s) | Target | Purpose |
|-----------|----------|--------|---------|
| **Business subroutine** | CBSTM03A | CBSTM03B | Delegated file I/O for statement generation |
| **Date formatting** | CBACT01C | COBDATFT (asm) | Format dates for account display |
| **Date validation** | CORPT00C, COTRN02C | CSUTLDTC | Validate user-entered dates |
| **LE intrinsic** | CSUTLDTC | CEEDAYS | Convert dates to Lilian day numbers |
| **System wait** | COBSWAIT | MVSWAIT (asm) | Introduce processing delay |
| **Abend handler** | 11 batch programs | CEE3ABD | Controlled abnormal termination on fatal errors |

---

## 3. CICS Program Transfer (XCTL) Map

Each row shows which programs a given CICS program can transfer control to.

| Source Program | XCTL Targets | Trigger |
|---------------|-------------|---------|
| **COSGN00C** | COMEN01C, COADM01C | Successful login (role-based) |
| **COMEN01C** | COACTVWC, COCRDLIC, COTRN00C, COBIL00C, CORPT00C, COSGN00C | Menu option selection |
| **COADM01C** | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C | Admin menu option |
| **COACTVWC** | COMEN01C, COACTUPC | PF3 (back), Select update |
| **COACTUPC** | COMEN01C, COACTVWC | PF3 (back), Cancel |
| **COCRDLIC** | COMEN01C, COCRDSLC, COCRDUPC | PF3, Select view, Select update |
| **COCRDSLC** | COMEN01C, COCRDLIC | PF3 (back to list) |
| **COCRDUPC** | COMEN01C, COCRDLIC | PF3 (back to list) |
| **COTRN00C** | COMEN01C, COTRN01C | PF3, Select transaction |
| **COTRN01C** | COMEN01C, COTRN00C | PF3 (back to list) |
| **COTRN02C** | COMEN01C, COTRN00C | PF3 (back to list) |
| **CORPT00C** | COMEN01C | PF3 (back) |
| **COBIL00C** | COMEN01C | PF3 (back) |
| **COUSR00C** | COADM01C, COUSR01C, COUSR02C, COUSR03C | PF3, User actions |
| **COUSR01C** | COADM01C, COUSR00C | PF3, After add |
| **COUSR02C** | COADM01C, COUSR00C | PF3, After update |
| **COUSR03C** | COADM01C, COUSR00C | PF3, After delete |

---

## 4. Program-to-Subroutine CALL Map

| Caller | Called Program | Parameter Area | Call Count |
|--------|--------------|---------------|------------|
| CBSTM03A | CBSTM03B | WS-M03B-AREA | 13 calls |
| CBACT01C | COBDATFT (asm) | CODATECN-REC | 1 call |
| CORPT00C | CSUTLDTC | CSUTLDTC-DATE | 2 calls |
| COTRN02C | CSUTLDTC | CSUTLDTC-DATE | 2 calls |
| CSUTLDTC | CEEDAYS (LE) | Date params | 1 call |
| COBSWAIT | MVSWAIT (asm) | MVSWAIT-TIME | 1 call |

---

## 5. Copybook Inclusion Matrix

Shows which copybooks are included by which programs (core data copybooks only).

| Copybook | Programs Using It | Count |
|----------|------------------|-------|
| **COCOM01Y** (Common area) | All 17 online programs | 17 |
| **COTTL01Y** (Title/header) | All 17 online programs | 17 |
| **CSDAT01Y** (Date utility) | All 17 online programs | 17 |
| **CSMSG01Y** (Message line 1) | All 17 online programs | 17 |
| **DFHAID** (AID key defs) | All 17 online programs | 17 |
| **DFHBMSCA** (BMS attributes) | All 17 online programs | 17 |
| **CSUSR01Y** (User security) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | 13 |
| **CSMSG02Y** (Message line 2) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | 4 |
| **CVACT01Y** (Account record) | COACTVWC, COACTUPC, COBIL00C, COTRN02C | 4 |
| **CVACT02Y** (Card record) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | 4 |
| **CVACT03Y** (Cross-ref) | COACTVWC, COACTUPC, COBIL00C, COTRN02C | 4 |
| **CVCUS01Y** (Customer) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | 4 |
| **CVCRD01Y** (Card detail) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CVTRA05Y** (Transaction) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | 5 |
| **CSSTRPFY** (String utility) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | 5 |
| **CSLKPCDY** (Lookup codes) | COACTUPC | 1 |
| **CSSETATY** (Attribute setter) | COACTUPC | 1 (multiple COPY REPLACING) |
| **CSUTLDPY** (Date util params) | COACTUPC | 1 |
| **COMEN02Y** (Menu options) | COMEN01C | 1 |
| **COADM02Y** (Admin options) | COADM01C | 1 |
| **CODATECN** (Date conversion) | CBACT01C | 1 |
| **CVTRA06Y** (Daily trans) | CBTRN01C, CBTRN02C, CBACT04C | 3 |
| **CVTRA07Y** (Report layout) | CBTRN03C | 1 |
| **CVTRA01Y** (Cat balance) | CBACT04C, CBTRN02C | 2 |
| **CVTRA02Y** (Disclosure grp) | CBACT04C | 1 |
| **CVTRA03Y** (Trans type) | CBTRN03C | 1 |
| **CVTRA04Y** (Trans category) | CBTRN03C | 1 |
| **COSTM01** (Statement txn) | CBSTM03A, CBSTM03B | 2 |
| **CVEXPORT** (Export layouts) | CBEXPORT, CBIMPORT | 2 |

---

## 6. VSAM File Access Map — Online Programs

Shows CICS file operations per online program.

| Program | USRSEC | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT |
|---------|--------|----------|----------|----------|----------|----------|
| **COSGN00C** | R | | | | | |
| **COACTVWC** | | R | | R | R | |
| **COACTUPC** | | R/W | | R | R/W | |
| **COCRDLIC** | | | R (browse) | | | |
| **COCRDSLC** | | | R | | R | |
| **COCRDUPC** | | | R/W | | | |
| **COTRN00C** | | | | | | R (browse) |
| **COTRN01C** | | | | | | R |
| **COTRN02C** | | R | | R | | R/W |
| **CORPT00C** | | | | | | R (TDQ write) |
| **COBIL00C** | | R | | R | | R/W |
| **COUSR00C** | R (browse) | | | | | |
| **COUSR01C** | W | | | | | |
| **COUSR02C** | R/W | | | | | |
| **COUSR03C** | R/D | | | | | |

**Legend:** R = Read, W = Write, R/W = Read + Rewrite/Write, R/D = Read + Delete, browse = STARTBR/READNEXT/READPREV

---

## 7. VSAM File Access Map — Batch Programs

| Program | ACCTFILE | CARDFILE | CUSTFILE | XREFFILE | TRANSACT | DALYTRAN | TCATBALF | DISCGRP | DALYREJS | TRANTYPE | TRANCATG |
|---------|----------|----------|----------|----------|----------|----------|----------|---------|----------|----------|----------|
| **CBACT01C** | R | | | | | | | | | | |
| **CBACT02C** | | R | | | | | | | | | |
| **CBACT03C** | | | | R | | | | | | | |
| **CBACT04C** | R/W | | | R | | R | R/W | R | | | |
| **CBCUS01C** | | | R | | | | | | | | |
| **CBTRN01C** | | | | | | R | | | | | |
| **CBTRN02C** | R/W | | | R | W | R | R/W | | W | | |
| **CBTRN03C** | | | | R | R | | | | | R | R |
| **CBSTM03A** | | | | | | | | | | | |
| **CBSTM03B** | R | | R | R | R* | | | | | | |
| **CBEXPORT** | R | R | R | R | R | | | | | | |
| **CBIMPORT** | W | W | W | W | W | | | | | | |

**Legend:** R = Read (OPEN INPUT), W = Write (OPEN OUTPUT), R/W = Read + Rewrite
*R* = TRNXFILE (re-keyed transaction file created by CREASTMT JCL)

---

## 8. JCL Job → Program Execution Map

| JCL Job | Step(s) | Program(s) Executed | Purpose |
|---------|---------|-------------------|---------|
| **POSTTRAN.jcl** | STEP01 | CBTRN02C | Post daily transactions |
| **INTCALC.jcl** | STEP01 | CBACT04C | Calculate interest |
| **CREASTMT.JCL** | STEP040 | CBSTM03A (→CBSTM03B) | Generate statements |
| **TRANREPT.jcl** | STEP01 | CBTRN03C | Daily transaction report |
| **READACCT.jcl** | STEP01 | CBACT01C | Read/validate accounts |
| **READCARD.jcl** | STEP01 | CBACT02C | Read/validate cards |
| **READCUST.jcl** | STEP01 | CBCUS01C | Read/validate customers |
| **READXREF.jcl** | STEP01 | CBACT03C | Read/validate cross-ref |
| **CBEXPORT.jcl** | STEP01 | CBEXPORT | Export all VSAM |
| **CBIMPORT.jcl** | STEP01 | CBIMPORT | Import all VSAM |
| **WAITSTEP.jcl** | STEP01 | COBSWAIT | Wait utility |
| **CBADMCDJ.jcl** | Multiple | IKJEFT01 (compile) | Compile & link programs |
| **TXT2PDF1.JCL** | TXT2PDF | IKJEFT1B (TXT2PDF) | Convert text to PDF |

---

## 9. JCL Job → Dataset Map (Data Lineage)

### Data Loading Jobs (Source → VSAM)

| JCL Job | Source Dataset (Flat File) | Target VSAM Dataset | Operation |
|---------|--------------------------|--------------------|-----------| 
| **ACCTFILE.jcl** | ACCTDATA.PS | ACCTDATA.VSAM.KSDS | REPRO (load) |
| **CARDFILE.jcl** | CARDDATA.PS | CARDDATA.VSAM.KSDS | REPRO (load) |
| **CUSTFILE.jcl** | CUSTDATA.PS | CUSTDATA.VSAM.KSDS | REPRO (load) |
| **XREFFILE.jcl** | CARDXREF.PS | CARDXREF.VSAM.KSDS | REPRO (load) |
| **TRANFILE.jcl** | TRANSACT.PS | TRANSACT.VSAM.KSDS | REPRO (load) |
| **DUSRSECJ.jcl** | USRSEC.PS | USRSEC.VSAM.KSDS | REPRO (load) |
| **DISCGRP.jcl** | DISCGRP.PS | DISCGRP.VSAM.KSDS | REPRO (load) |
| **TCATBALF.jcl** | TCATBALF.PS | TCATBALF.VSAM.KSDS | REPRO (load) |
| **TRANCATG.jcl** | TRANCATG.PS | TRANCATG.VSAM.KSDS | REPRO (load) |
| **TRANTYPE.jcl** | TRANTYPE.PS | TRANTYPE.VSAM.KSDS | REPRO (load) |

### Batch Processing Data Flow

| JCL Job | Input Datasets | Output Datasets | Transformation |
|---------|---------------|----------------|----------------|
| **POSTTRAN.jcl** | DALYTRAN, TRANSACT, XREFFILE, ACCTFILE, TCATBALF | TRANSACT (append), ACCTFILE (update), TCATBALF (update), DALYREJS | Posts valid daily transactions, rejects invalid |
| **INTCALC.jcl** | ACCTFILE, DISCGRP, TCATBALF, XREFFILE, DALYTRAN | ACCTFILE (update), TRANSACT (interest entries) | Calculates and posts interest charges |
| **TRANBKP.jcl** | TRANSACT.VSAM.KSDS | TRANSACT.VSAM.KSDS.BKP (GDG) | Backup via REPRO |
| **COMBTRAN.jcl** | DALYTRAN, TRANSACT | TRANSACT (merged) | SORT + merge daily into master |
| **CREASTMT.JCL** | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML | Statement generation (text + HTML) |
| **TRANREPT.jcl** | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) | Daily transaction report |
| **CBEXPORT.jcl** | All 5 VSAM files | 5 sequential export files | Full data export |
| **CBIMPORT.jcl** | 5 sequential import files | All 5 VSAM files | Full data import |

### Alternate Index Builds

| JCL Job | Base VSAM | Alternate Index | AIX Key |
|---------|----------|----------------|---------|
| **CARDFILE.jcl** | CARDDATA.VSAM.KSDS | CARDDATA.VSAM.AIX | ACCT-ID (pos 16, len 11) |
| **XREFFILE.jcl** | CARDXREF.VSAM.KSDS | CARDXREF.VSAM.AIX | ACCT-ID (pos 25, len 11) |
| **TRANFILE.jcl** | TRANSACT.VSAM.KSDS | TRANSACT.VSAM.AIX | CARD-NUM (pos 263, len 16) |
| **TRANIDX.jcl** | TRANSACT.VSAM.KSDS | TRANSACT.VSAM.AIX | Rebuild after batch updates |

---

## 10. Batch Processing Sequence (Job Flow)

The nightly batch cycle runs in a strict order:

```
Step 1: CLOSEFIL ──▶ Close CICS files (DFHCSDUP SET CLOSED)
           │
Step 2: ───┼──▶ ACCTFILE ──▶ Refresh Account VSAM
           ├──▶ CARDFILE ──▶ Refresh Card VSAM
           ├──▶ CUSTFILE ──▶ Refresh Customer VSAM
           ├──▶ XREFFILE ──▶ Refresh Cross-Ref VSAM
           └──▶ TRANFILE ──▶ Refresh Transaction VSAM
           │
Step 3: POSTTRAN ──▶ Post daily transactions (CBTRN02C)
           │         Reads: DALYTRAN, TRANSACT, XREF, ACCT, TCATBALF
           │         Updates: TRANSACT, ACCT, TCATBALF
           │         Creates: DALYREJS (rejected transactions)
           │
Step 4: INTCALC ───▶ Calculate interest (CBACT04C)
           │         Reads: ACCT, DISCGRP, TCATBALF, XREF, DALYTRAN
           │         Updates: ACCT (add interest), creates interest transactions
           │
Step 5: TRANBKP ───▶ Backup transaction file (IDCAMS REPRO to GDG)
           │
Step 6: COMBTRAN ──▶ Combine daily + master transactions (SORT/MERGE)
           │
Step 7: CREASTMT ──▶ Generate statements (CBSTM03A → CBSTM03B)
           │         Reads: TRNXFILE, XREF, ACCT, CUST
           │         Creates: STATEMNT.PS (text), STATEMNT.HTML
           │
Step 8: TRANREPT ──▶ Generate daily report (CBTRN03C)
           │         Reads: TRANSACT, XREF, TRANTYPE, TRANCATG
           │         Creates: TRANREPT (report)
           │
Step 9: TRANIDX ───▶ Rebuild transaction alternate indexes
           │
Step 10: OPENFIL ──▶ Reopen CICS files (DFHCSDUP SET OPEN)
```

### Critical Path Dependencies
- Steps 3-4 **must** run sequentially (interest depends on posted transactions)
- Steps 5-8 can potentially run in parallel after Step 4
- Steps 1 and 10 are mandatory bookends (CICS file access control)
- Data refresh jobs (Step 2) are only needed for initial load or recovery

---

## 11. BMS Map → Program → Copybook Traceability

| BMS Map | CICS Program | BMS Copybook | Data Copybooks Used |
|---------|-------------|-------------|-------------------|
| COSGN00.bms | COSGN00C | COSGN00.CPY | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COMEN01.bms | COMEN01C | COMEN01.CPY | COCOM01Y, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COADM01.bms | COADM01C | COADM01.CPY | COCOM01Y, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y |
| COACTVW.bms | COACTVWC | COACTVW.CPY | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSTRPFY |
| COACTUP.bms | COACTUPC | COACTUP.CPY | COCOM01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSETATY, CSSTRPFY, CSUTLDPY |
| COCRDLI.bms | COCRDLIC | COCRDLI.CPY | COCOM01Y, CVACT02Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CSSTRPFY |
| COCRDSL.bms | COCRDSLC | COCRDSL.CPY | COCOM01Y, CVACT02Y, CVCUS01Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSTRPFY |
| COCRDUP.bms | COCRDUPC | COCRDUP.CPY | COCOM01Y, CVACT02Y, CVCUS01Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CSSTRPFY |
| COTRN00.bms | COTRN00C | COTRN00.CPY | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COTRN01.bms | COTRN01C | COTRN01.CPY | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COTRN02.bms | COTRN02C | COTRN02.CPY | COCOM01Y, CVTRA05Y, CVACT01Y, CVACT03Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| CORPT00.bms | CORPT00C | CORPT00.CPY | COCOM01Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COBIL00.bms | COBIL00C | COBIL00.CPY | COCOM01Y, CVACT01Y, CVACT03Y, CVTRA05Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COUSR00.bms | COUSR00C | COUSR00.CPY | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COUSR01.bms | COUSR01C | COUSR01.CPY | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COUSR02.bms | COUSR02C | COUSR02.CPY | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| COUSR03.bms | COUSR03C | COUSR03.CPY | COCOM01Y, CSUSR01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |

---

## 12. Cross-Cutting Dependency Summary

### Most-Connected Programs (by dependency count)

| Program | Copybooks | VSAM Files | Calls Out | Called By | XCTL Targets | Risk Level |
|---------|-----------|-----------|-----------|----------|-------------|------------|
| **COACTUPC** | 14 | 3 (R/W) | 0 | COACTVWC | 2 | **Critical** |
| **CBTRN02C** | 7 | 6 (R/W) | 1 (CEE3ABD) | JCL only | 0 | **Critical** |
| **CBACT04C** | 6 | 6 (R/W) | 1 (CEE3ABD) | JCL only | 0 | **Critical** |
| **CBSTM03A** | 3 | 2 (W) | 14 (CBSTM03B) | JCL only | 0 | **High** |
| **COCRDLIC** | 8 | 1 (browse) | 0 | COMEN01C | 3 | **Medium** |
| **COTRN02C** | 9 | 3 (R/W) | 2 (CSUTLDTC) | COMEN01C | 2 | **High** |

### Shared Copybook Fan-Out (Impact of Change)

| Copybook | Used By (count) | Change Impact |
|----------|----------------|---------------|
| COCOM01Y | 17 programs | **Critical** — All online programs break if changed |
| COTTL01Y | 17 programs | Low — display only |
| CSDAT01Y | 17 programs | Low — working storage only |
| CSMSG01Y | 17 programs | Low — message display only |
| CVACT01Y | 4+ programs | **High** — Account structure is core entity |
| CVTRA05Y | 5+ programs | **High** — Transaction structure is core entity |
| CSUSR01Y | 13 programs | **Medium** — User security record |
| CSLKPCDY | 1 program | Low — reference data only |

### Dataset Access Concurrency Concerns

| VSAM Dataset | Online Access | Batch Access | Conflict Risk |
|-------------|-------------|-------------|--------------|
| **ACCTDATA** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBACT01C, CBACT04C, CBTRN02C, CBSTM03B | **High** — batch updates require CICS file close |
| **TRANSACT** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | CBTRN02C, CBTRN03C, CBEXPORT | **High** — most accessed dataset |
| **CARDDATA** | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBEXPORT | Medium |
| **CARDXREF** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | CBACT03C, CBACT04C, CBTRN02C, CBTRN03C, CBSTM03B | **High** — join table for card-to-account |
| **USRSEC** | All user mgmt programs + COSGN00C | None | Low — online-only |
| **CUSTDATA** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | CBCUS01C, CBSTM03B | Medium |
