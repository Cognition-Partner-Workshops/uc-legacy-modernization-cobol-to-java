# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Source:** Static analysis of COBOL `CALL`, `COPY`, `EXEC CICS XCTL/LINK`, JCL `EXEC PGM=`, and file DD statements

---

## Table of Contents

- [1. Program-to-Program Call Graph](#1-program-to-program-call-graph)
  - [1.1 Online CICS Navigation Flow](#11-online-cics-navigation-flow)
  - [1.2 Batch Program Calls](#12-batch-program-calls)
  - [1.3 Full Call Matrix](#13-full-call-matrix)
- [2. Program-to-Copybook Dependencies](#2-program-to-copybook-dependencies)
  - [2.1 Copybook Usage Matrix](#21-copybook-usage-matrix)
  - [2.2 Most-Referenced Copybooks](#22-most-referenced-copybooks)
- [3. Program-to-VSAM File Access](#3-program-to-vsam-file-access)
  - [3.1 Online Program File Access](#31-online-program-file-access)
  - [3.2 Batch Program File Access](#32-batch-program-file-access)
- [4. JCL Job-to-Program-to-File Data Lineage](#4-jcl-job-to-program-to-file-data-lineage)
  - [4.1 Batch Processing Chain](#41-batch-processing-chain)
  - [4.2 Data Refresh Jobs](#42-data-refresh-jobs)
  - [4.3 Reporting Jobs](#43-reporting-jobs)
  - [4.4 Utility Jobs](#44-utility-jobs)
- [5. End-to-End Data Lineage](#5-end-to-end-data-lineage)
  - [5.1 Transaction Lifecycle](#51-transaction-lifecycle)
  - [5.2 Account Statement Lifecycle](#52-account-statement-lifecycle)
  - [5.3 Interest Calculation Flow](#53-interest-calculation-flow)
- [6. BMS Map Dependencies](#6-bms-map-dependencies)
- [7. Shared Dependency Clusters](#7-shared-dependency-clusters)

---

## 1. Program-to-Program Call Graph

### 1.1 Online CICS Navigation Flow

All online programs communicate via **EXEC CICS XCTL** (transfer control) passing the COMMAREA (COCOM01Y).

```
                          ┌─────────────┐
                          │  COSGN00C   │  Sign-on
                          │  (CC00)     │
                          └──────┬──────┘
                                 │ XCTL
                    ┌────────────┴────────────┐
                    ▼                         ▼
             ┌─────────────┐          ┌─────────────┐
             │  COMEN01C   │          │  COADM01C   │
             │  Main Menu  │          │  Admin Menu  │
             │  (CM00)     │          │  (CA00)      │
             └──────┬──────┘          └──────┬──────┘
                    │                        │
        ┌───────┬───┴───┬────────┐    ┌──────┴──────────┐
        ▼       ▼       ▼        ▼    ▼                  ▼
   ┌────────┐┌────────┐┌────────┐┌────────┐       ┌──────────┐
   │COACTVWC││COCRDLIC││COTRN00C││COBIL00C│       │ COUSR00C │
   │Acct    ││Card    ││Tran   ││Bill    │       │ User List│
   │View    ││List    ││List   ││Payment │       └────┬─────┘
   └───┬────┘└───┬────┘└───┬────┘└────────┘      ┌────┼─────┐
       │         │         │                      ▼    ▼     ▼
       ▼         ▼         ▼                 ┌──────┐┌──────┐┌──────┐
   ┌────────┐┌────────┐┌────────┐            │COUSR ││COUSR ││COUSR │
   │COACTUPC││COCRDSLC││COTRN01C│            │01C   ││02C   ││03C   │
   │Acct    ││Card    ││Tran   │            │Add   ││Update││Delete│
   │Update  ││View    ││View   │            └──────┘└──────┘└──────┘
   └────────┘└───┬────┘└────────┘
                 │
                 ▼
            ┌────────┐    ┌────────┐
            │COCRDUPC│    │COTRN02C│
            │Card    │    │Tran    │
            │Update  │    │Add     │
            └────────┘    └────────┘
```

**Navigation Rules:**
- COSGN00C authenticates then XCTLs to COMEN01C (regular user) or COADM01C (admin)
- COMEN01C dispatches to functional screens based on menu selection
- COCRDLIC XCTLs to COCRDSLC (view) or COCRDUPC (update) based on user action
- COACTVWC XCTLs to COACTUPC for edits
- All screens can XCTL back to COMEN01C via PF3 (back)
- COUSR00C dispatches to COUSR01C (add), COUSR02C (update), COUSR03C (delete)

### 1.2 Batch Program Calls

```
CBSTM03A ──CALL──▶ CBSTM03B    (Statement main calls file-handling subroutine)
CBSTM03A ──CALL──▶ CEE3ABD     (LE abnormal termination)
CORPT00C ──CALL──▶ CSUTLDTC    (Date validation for report date range)
COTRN02C ──CALL──▶ CSUTLDTC    (Date validation for transaction dates)
COBSWAIT ──CALL──▶ MVSWAIT     (Assembler wait routine)
CSUTLDTC ──CALL──▶ CEEDAYS     (LE date conversion API)
```

### 1.3 Full Call Matrix

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| **COSGN00C** | COMEN01C | EXEC CICS XCTL | Navigate to main menu (regular user) |
| **COSGN00C** | COADM01C | EXEC CICS XCTL | Navigate to admin menu (admin user) |
| **COMEN01C** | *(dynamic)* | EXEC CICS XCTL | Menu dispatch — target from COMEN02Y table |
| **COADM01C** | *(dynamic)* | EXEC CICS XCTL | Admin menu dispatch — target from COADM02Y table |
| **COACTVWC** | COACTUPC | EXEC CICS XCTL | Account view → update |
| **COCRDLIC** | COMEN01C | EXEC CICS XCTL | Back to menu |
| **COCRDLIC** | COCRDSLC | EXEC CICS XCTL | Card list → detail view |
| **COCRDLIC** | COCRDUPC | EXEC CICS XCTL | Card list → update |
| **COCRDSLC** | COCRDUPC | EXEC CICS XCTL | Card view → update |
| **COCRDUPC** | COCRDLIC | EXEC CICS XCTL | Card update → back to list |
| **COUSR00C** | COUSR01C | EXEC CICS XCTL | User list → add |
| **COUSR00C** | COUSR02C | EXEC CICS XCTL | User list → update |
| **CORPT00C** | CSUTLDTC | CALL | Validate report date range |
| **COTRN02C** | CSUTLDTC | CALL | Validate transaction date |
| **CBSTM03A** | CBSTM03B | CALL | Delegate file I/O operations |
| **CBSTM03A** | CEE3ABD | CALL | Abnormal end handler |
| **COBSWAIT** | MVSWAIT | CALL | MVS wait (assembler) |
| **CSUTLDTC** | CEEDAYS | CALL | LE date conversion |

---

## 2. Program-to-Copybook Dependencies

### 2.1 Copybook Usage Matrix

| Program | COCOM01Y | COTTL01Y | CSDAT01Y | CSMSG01Y | CSUSR01Y | DFHAID | DFHBMSCA | BMS Copy | Data Copies |
|---------|:--------:|:--------:|:--------:|:--------:|:--------:|:------:|:--------:|:--------:|:------------|
| COSGN00C | ● | ● | ● | ● | ● | ● | ● | COSGN00 | — |
| COMEN01C | ● | ● | ● | ● | ● | ● | ● | COMEN01 | COMEN02Y |
| COADM01C | ● | ● | ● | ● | ● | ● | ● | COADM01 | COADM02Y |
| COACTVWC | ● | ● | ● | ● | — | ● | ● | COACTVW | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| COACTUPC | ● | ● | ● | ● | ● | ● | ● | COACTUP | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSMSG02Y |
| COCRDLIC | ● | ● | ● | ● | ● | ● | ● | COCRDLI | CVCRD01Y, CVACT02Y, CSSTRPFY |
| COCRDSLC | ● | ● | ● | ● | ● | ● | ● | COCRDSL | CVCRD01Y, CVACT02Y, CVCUS01Y, CSMSG02Y, CSSTRPFY |
| COCRDUPC | ● | ● | ● | ● | ● | ● | ● | COCRDUP | CVCRD01Y, CVACT02Y, CVCUS01Y, CSMSG02Y, CSSTRPFY |
| COTRN00C | ● | ● | ● | ● | — | ● | ● | COTRN00 | CVTRA05Y |
| COTRN01C | ● | ● | ● | ● | — | ● | ● | COTRN01 | CVTRA05Y |
| COTRN02C | ● | ● | ● | ● | — | ● | ● | COTRN02 | CVTRA05Y, CVACT01Y, CVACT03Y |
| CORPT00C | ● | ● | ● | ● | — | ● | ● | CORPT00 | CVTRA05Y |
| COBIL00C | ● | ● | ● | ● | — | ● | ● | COBIL00 | CVACT01Y, CVACT03Y, CVTRA05Y |
| COUSR00C | ● | ● | ● | ● | ● | ● | ● | COUSR00 | — |
| COUSR01C | ● | ● | ● | ● | ● | ● | ● | COUSR01 | — |
| COUSR02C | ● | ● | ● | ● | ● | ● | ● | COUSR02 | — |
| COUSR03C | ● | ● | ● | ● | ● | ● | ● | COUSR03 | — |
| CBSTM03A | — | — | — | — | — | — | — | — | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| CBTRN02C | — | — | — | — | — | — | — | — | CVTRA05Y, CVTRA06Y, CVACT01Y, CVTRA01Y, CVACT03Y |
| CBTRN03C | — | — | — | — | — | — | — | — | CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y, CVACT03Y |
| CBACT04C | — | — | — | — | — | — | — | — | CVTRA01Y, CVACT03Y, CVACT01Y, CVTRA02Y, CVTRA05Y |

### 2.2 Most-Referenced Copybooks

| Rank | Copybook | Used By (Count) | Category |
|------|----------|-----------------|----------|
| 1 | COCOM01Y | 17 programs | Common area |
| 2 | COTTL01Y | 17 programs | Screen titles |
| 3 | CSDAT01Y | 17 programs | Date fields |
| 4 | CSMSG01Y | 17 programs | Messages |
| 5 | DFHAID | 17 programs | CICS attention IDs |
| 6 | DFHBMSCA | 17 programs | BMS attributes |
| 7 | CSUSR01Y | 12 programs | User security record |
| 8 | CVTRA05Y | 8 programs | Transaction record |
| 9 | CVACT01Y | 6 programs | Account record |
| 10 | CVACT03Y | 7 programs | Card cross-reference |
| 11 | CVACT02Y | 5 programs | Card record |
| 12 | CVCUS01Y | 4 programs | Customer record |
| 13 | CVCRD01Y | 3 programs | Card extended |
| 14 | CSSTRPFY | 3 programs | String formatting |
| 15 | CSMSG02Y | 3 programs | Extended messages |

---

## 3. Program-to-VSAM File Access

### 3.1 Online Program File Access

| Program | VSAM Files Accessed | Operations |
|---------|-------------------|------------|
| **COSGN00C** | USRSEC | READ |
| **COACTVWC** | ACCTDATA, CARDDATA, CUSTDATA | READ |
| **COACTUPC** | ACCTDATA, CARDDATA, CUSTDATA | READ, READ, READ |
| **COCRDLIC** | CARDDATA | STARTBR, READNEXT, READPREV, ENDBR |
| **COCRDSLC** | CARDDATA, ACCTDATA | READ |
| **COCRDUPC** | CARDDATA, ACCTDATA | READ |
| **COTRN00C** | TRANSACT | STARTBR, READNEXT, READPREV, ENDBR |
| **COTRN01C** | TRANSACT | READ |
| **COTRN02C** | TRANSACT, ACCTDATA, CARDXREF | READ, STARTBR, READPREV, ENDBR, WRITE |
| **COBIL00C** | TRANSACT, ACCTDATA, CARDXREF | READ, READPREV, REWRITE, WRITE, STARTBR, ENDBR |
| **CORPT00C** | *(TD Queue)* | WRITEQ TD |
| **COUSR00C** | USRSEC | STARTBR, READNEXT, READPREV, ENDBR |
| **COUSR01C** | USRSEC | WRITE |
| **COUSR02C** | USRSEC | READ, REWRITE |
| **COUSR03C** | USRSEC | READ, DELETE |

### 3.2 Batch Program File Access

| Program | Input Files | Output Files | I-O Files |
|---------|-------------|--------------|-----------|
| **CBACT01C** | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE | — |
| **CBACT02C** | CARDFILE | — | — |
| **CBACT03C** | XREFFILE | — | — |
| **CBACT04C** | TCATBALF, XREFFILE, DISCGRP | TRANSACT | ACCTFILE |
| **CBCUS01C** | CUSTFILE | — | — |
| **CBTRN01C** | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | — | — |
| **CBTRN02C** | DALYTRAN, XREFFILE | TRANFILE, DALYREJS | ACCTFILE, TCATBALF |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT | — |
| **CBSTM03A** | *(delegates to CBSTM03B)* | STMTFILE, HTMLFILE | — |
| **CBSTM03B** | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — | — |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | — |
| **CBIMPORT** | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | — |

---

## 4. JCL Job-to-Program-to-File Data Lineage

### 4.1 Batch Processing Chain

The nightly batch cycle runs in this order:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  CLOSEFIL    │────▶│  Data Refresh │────▶│  POSTTRAN    │
│  Close CICS  │     │  (see 4.2)   │     │  CBTRN02C    │
│  Files       │     │              │     │  Post Trans  │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
     ┌────────────────────────────────────────────┘
     ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  INTCALC     │────▶│  TRANBKP     │────▶│  COMBTRAN    │
│  CBACT04C    │     │  Backup      │     │  SORT+IDCAMS │
│  Interest    │     │  Transactions│     │  Combine     │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
     ┌────────────────────────────────────────────┘
     ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  CREASTMT    │────▶│  TRANIDX     │────▶│  OPENFIL     │
│  CBSTM03A    │     │  Alt Index   │     │  Open CICS   │
│  Statements  │     │  Rebuild     │     │  Files       │
└──────────────┘     └──────────────┘     └──────────────┘
```

### 4.2 Data Refresh Jobs

| Job | Program/Utility | Reads | Writes | Purpose |
|-----|----------------|-------|--------|---------|
| **ACCTFILE** | IDCAMS | ACCTDATA.PS (flat) | ACCTDATA.VSAM.KSDS | Load account master |
| **CARDFILE** | IDCAMS | CARDDATA.PS (flat) | CARDDATA.VSAM.KSDS + AIX | Load card master + alt index |
| **CUSTFILE** | IDCAMS | CUSTDATA.PS (flat) | CUSTDATA.VSAM.KSDS | Load customer master |
| **XREFFILE** | IDCAMS | CARDXREF.PS (flat) | CARDXREF.VSAM.KSDS + AIX | Load cross-ref + alt index |
| **TRANFILE** | IDCAMS | TRANSACT.PS (flat) | TRANSACT.VSAM.KSDS + AIX | Load transaction master |
| **DUSRSECJ** | IDCAMS | USRSEC.PS (flat) | USRSEC.VSAM.KSDS | Load user security |
| **DISCGRP** | IDCAMS | DISCGRP.PS (flat) | DISCGRP.VSAM.KSDS | Load disclosure groups |
| **TCATBALF** | IDCAMS | TCATBALF.PS (flat) | TCATBALF.VSAM.KSDS | Load tran category balances |
| **TRANTYPE** | IDCAMS | TRANTYPE.PS (flat) | TRANTYPE.VSAM.KSDS | Load transaction types |
| **TRANCATG** | IDCAMS | TRANCATG.PS (flat) | TRANCATG.VSAM.KSDS | Load transaction categories |
| **REPTFILE** | IEBGENER | DATEPARM.PS (flat) | DATEPARM file | Load report date parameters |

### 4.3 Reporting Jobs

| Job | Program | Reads | Writes | Purpose |
|-----|---------|-------|--------|---------|
| **POSTTRAN** | CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF | Post daily transactions |
| **INTCALC** | CBACT04C | TCATBALF, XREFFILE, DISCGRP | TRANSACT | Calculate interest, update accounts |
| **COMBTRAN** | SORT + IDCAMS | TRANSACT.VSAM, DALYTRAN | TRANSACT.VSAM (merged) | Merge daily into master |
| **TRANBKP** | IDCAMS REPRO | TRANSACT.VSAM | TRANSACT.BKUP (GDG) | Backup transactions |
| **CREASTMT** | SORT + CBSTM03A | TRANSACT.VSAM, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML | Generate statements |
| **TRANREPT** | CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report) | Daily transaction report |
| **TXT2PDF1** | TXT2PDF (REXX) | STATEMNT.PS | STATEMNT.PS.PDF | Convert statements to PDF |

### 4.4 Utility Jobs

| Job | Program | Reads | Writes | Purpose |
|-----|---------|-------|--------|---------|
| **CBEXPORT** | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | Export all data |
| **CBIMPORT** | CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | Import data |
| **CLOSEFIL** | IDCAMS | — | — | SET CLOSESTATUS on CICS files |
| **OPENFIL** | IDCAMS | — | — | SET OPENSTATUS on CICS files |
| **DEFGDGB/D** | IDCAMS | — | — | Define GDG bases |
| **TRANIDX** | IDCAMS | TRANSACT.VSAM | TRANSACT.VSAM.AIX | Build alternate index |
| **WAITSTEP** | COBSWAIT | — | — | Timer delay |
| **READACCT** | IDCAMS | ACCTDATA.VSAM | SYSOUT | Print account file |
| **READCARD** | IDCAMS | CARDDATA.VSAM | SYSOUT | Print card file |
| **READCUST** | IDCAMS | CUSTDATA.VSAM | SYSOUT | Print customer file |
| **READXREF** | IDCAMS | CARDXREF.VSAM | SYSOUT | Print cross-ref file |
| **DALYREJS** | IDCAMS | DALYREJS file | SYSOUT | Print rejection file |
| **FTPJCL** | FTP | Mainframe files | Remote server | FTP transfer |
| **INTRDRJ1** | IDCAMS + IEBGENER | FTP.TEST | FTP.TEST.BKUP + INTRDRJ2 | Chain job trigger |
| **INTRDRJ2** | IDCAMS | FTP.TEST.BKUP | FTP.TEST.BKUP.INTRDR | Chained job |
| **CBADMCDJ** | Multi-step | Various | Various | Admin card demo setup |
| **ESDSRRDS** | IDCAMS | — | ESDS/RRDS clusters | Define non-KSDS VSAM |
| **DEFCUST** | IDCAMS | — | CUSTDATA.VSAM | Define customer cluster |
| **PRTCATBL** | IDCAMS | Catalog | SYSOUT | Print catalog |

---

## 5. End-to-End Data Lineage

### 5.1 Transaction Lifecycle

```
User enters         COTRN02C             POSTTRAN            COMBTRAN           CREASTMT
transaction    ──▶  (Online Add)    ──▶  (CBTRN02C)    ──▶  (SORT+IDCAMS) ──▶ (CBSTM03A)
on 3270 screen      Writes to            Reads DALYTRAN      Merges daily       Reads merged
                    TRANSACT VSAM        Validates card      into master        transactions
                    (direct write)       Posts to TRANFILE   TRANSACT file      Produces
                                         Rejects→DALYREJS                      statements
                                         Updates ACCTFILE
                                         Updates TCATBALF
```

**Key Files in Transaction Lifecycle:**

| Stage | File | Operation | Purpose |
|-------|------|-----------|---------|
| 1. Entry | TRANSACT.VSAM | WRITE | Online transaction added |
| 2. Daily Input | DALYTRAN | READ | Daily batch input |
| 3. Validation | CARDXREF.VSAM | READ | Validate card exists |
| 4. Posting | TRANSACT.VSAM | WRITE | Posted transaction |
| 5. Rejection | DALYREJS | WRITE | Failed validations |
| 6. Balance Update | ACCTDATA.VSAM | I-O | Update account balance |
| 7. Category Update | TCATBALF | I-O | Update category balance |
| 8. Backup | TRANSACT.BKUP | WRITE | GDG backup |
| 9. Statement | STATEMNT.PS/HTML | WRITE | Customer statement |

### 5.2 Account Statement Lifecycle

```
CREASTMT JCL:
  Step 1: SORT         TRANSACT.VSAM ──▶ TRXFL.SEQ (re-keyed by card+tran)
  Step 2: IDCAMS       TRXFL.SEQ     ──▶ TRXFL.VSAM.KSDS
  Step 3: IEFBR14      Delete old STATEMNT files
  Step 4: CBSTM03A     TRXFL.VSAM   ┐
          (calls       CARDXREF.VSAM ├──▶ STATEMNT.PS (plain text)
           CBSTM03B)   ACCTDATA.VSAM │    STATEMNT.HTML (web format)
                       CUSTDATA.VSAM ┘

TXT2PDF1 JCL:
  Step 1: TXT2PDF      STATEMNT.PS  ──▶ STATEMNT.PS.PDF
```

### 5.3 Interest Calculation Flow

```
INTCALC JCL → CBACT04C:
  Reads:   TCATBALF  (category balances per account)
           XREFFILE  (card-to-account mapping)
           DISCGRP   (interest rates per group/type/category)
  Updates: ACCTFILE  (adds interest to account balance)
  Writes:  TRANSACT  (creates interest transaction records)
```

---

## 6. BMS Map Dependencies

Each BMS map is tightly coupled with one online COBOL program and one BMS-generated copybook:

| BMS Map | Program | BMS Copybook | Screen Fields |
|---------|---------|-------------|---------------|
| COSGN00.bms | COSGN00C | COSGN00.CPY | User ID, Password |
| COMEN01.bms | COMEN01C | COMEN01.CPY | Menu options (1-9) |
| COADM01.bms | COADM01C | COADM01.CPY | Admin menu options |
| COACTVW.bms | COACTVWC | COACTVW.CPY | Account details (read-only) |
| COACTUP.bms | COACTUPC | COACTUP.CPY | Account fields (editable) |
| COCRDLI.bms | COCRDLIC | COCRDLI.CPY | Card list (multi-row) |
| COCRDSL.bms | COCRDSLC | COCRDSL.CPY | Card details (read-only) |
| COCRDUP.bms | COCRDUPC | COCRDUP.CPY | Card fields (editable) |
| COTRN00.bms | COTRN00C | COTRN00.CPY | Transaction list (multi-row) |
| COTRN01.bms | COTRN01C | COTRN01.CPY | Transaction details (read-only) |
| COTRN02.bms | COTRN02C | COTRN02.CPY | Transaction entry fields |
| CORPT00.bms | CORPT00C | CORPT00.CPY | Report date range |
| COBIL00.bms | COBIL00C | COBIL00.CPY | Bill payment fields |
| COUSR00.bms | COUSR00C | COUSR00.CPY | User list (multi-row) |
| COUSR01.bms | COUSR01C | COUSR01.CPY | User entry fields |
| COUSR02.bms | COUSR02C | COUSR02.CPY | User update fields |
| COUSR03.bms | COUSR03C | COUSR03.CPY | User delete confirmation |

---

## 7. Shared Dependency Clusters

Programs that share the same data files form natural **migration clusters** — they should be modernized together to avoid split-brain issues.

### Cluster 1: Account Management
**Programs:** COACTVWC, COACTUPC, CBACT01C, CBACT04C
**Shared Files:** ACCTDATA.VSAM.KSDS
**Risk:** High — financial data, balance updates

### Cluster 2: Card Management
**Programs:** COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C
**Shared Files:** CARDDATA.VSAM.KSDS, CARDXREF.VSAM.KSDS
**Risk:** High — PCI-sensitive card data

### Cluster 3: Transaction Processing
**Programs:** COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C
**Shared Files:** TRANSACT.VSAM.KSDS, DALYTRAN, TCATBALF
**Risk:** Critical — core revenue processing

### Cluster 4: Statement & Reporting
**Programs:** CBSTM03A, CBSTM03B, CORPT00C, CBTRN03C
**Shared Files:** TRANSACT.VSAM (read), CARDXREF (read), ACCTDATA (read), CUSTDATA (read)
**Risk:** Medium — read-only access to core data

### Cluster 5: User Security
**Programs:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C
**Shared Files:** USRSEC.VSAM.KSDS
**Risk:** High — authentication, plaintext passwords

### Cluster 6: Data Migration
**Programs:** CBEXPORT, CBIMPORT
**Shared Files:** All core VSAM files
**Risk:** Low — utility, not in daily processing path

### Cross-Cluster Dependencies
```
Cluster 3 (Transactions) ──reads──▶ Cluster 2 (Cards) via CARDXREF
Cluster 3 (Transactions) ──updates──▶ Cluster 1 (Accounts) via ACCTFILE
Cluster 4 (Reporting)    ──reads──▶ Cluster 1, 2, 3 (all core data)
Cluster 5 (Security)     ──gates──▶ All online clusters via COSGN00C
```
