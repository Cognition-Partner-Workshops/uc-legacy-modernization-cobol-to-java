# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Scope:** Call graph, CICS transfer-of-control, copybook inclusion, and JCL data lineage  
> **Purpose:** Understand program-to-program, program-to-data, and job-to-file relationships for migration sequencing

---

## 1. Online CICS Program Call Graph

### 1.1 Navigation Flow (XCTL Transfer-of-Control)

The online system uses `EXEC CICS XCTL` (transfer control) to navigate between screens. The COMMAREA field `CDEMO-TO-PROGRAM` determines the target.

```
                          ┌──────────────┐
                          │  COSGN00C    │
                          │  (Signon)    │
                          └──────┬───────┘
                                 │ XCTL based on user type
                    ┌────────────┴────────────┐
                    ▼                         ▼
            ┌──────────────┐          ┌──────────────┐
            │  COMEN01C    │          │  COADM01C    │
            │ (User Menu)  │          │ (Admin Menu) │
            └──────┬───────┘          └──────┬───────┘
                   │                         │
      Menu options (XCTL)           Menu options (XCTL)
    ┌──────┬───────┼───────┐      ┌──────┬───────┐
    ▼      ▼       ▼       ▼      ▼      ▼       ▼
 COACTVWC COCRDLIC COTRN00C COBIL00C COUSR00C  (+ all
 COACTUPC COCRDSLC COTRN01C CORPT00C COUSR01C   user
           COCRDUPC COTRN02C          COUSR02C   menu
                                      COUSR03C   options)
```

### 1.2 Detailed XCTL Targets

| Source Program | XCTL Target(s) | Navigation Trigger |
|----------------|----------------|-------------------|
| **COSGN00C** | COMEN01C (if user type = 'U') | Successful regular user login |
| **COSGN00C** | COADM01C (if user type = 'A') | Successful admin login |
| **COMEN01C** | CDEMO-MENU-OPT-PGMNAME(n) | Menu option selection (via COMEN02Y table) |
| **COMEN01C** | CDEMO-TO-PROGRAM | Generic return-to-caller navigation |
| **COADM01C** | CDEMO-ADMIN-OPT-PGMNAME(n) | Admin menu option (via COADM02Y table) |
| **COADM01C** | CDEMO-TO-PROGRAM | Generic return-to-caller |
| **COACTVWC** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **COACTUPC** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **COCRDLIC** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **COCRDLIC** | COCRDSLC | Select card for viewing |
| **COCRDLIC** | COCRDUPC | Select card for update |
| **COCRDSLC** | CDEMO-TO-PROGRAM | F3/Back returns to card list |
| **COCRDUPC** | CDEMO-TO-PROGRAM | F3/Back returns to card list |
| **COTRN00C** | CDEMO-TO-PROGRAM | F3/Back or detail navigation |
| **COTRN01C** | CDEMO-TO-PROGRAM | F3/Back returns to tran list |
| **COTRN02C** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **CORPT00C** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **COBIL00C** | CDEMO-TO-PROGRAM | F3/Back returns to menu |
| **COUSR00C** | CDEMO-TO-PROGRAM | Navigation between user CRUD screens |
| **COUSR01C** | CDEMO-TO-PROGRAM | F3/Back returns to user list |
| **COUSR02C** | CDEMO-TO-PROGRAM | F3/Back returns to user list |
| **COUSR03C** | CDEMO-TO-PROGRAM | F3/Back returns to user list |

### 1.3 COBOL CALL Graph (Static Calls)

These are `CALL 'program'` statements -- true subroutine calls (not CICS XCTL).

```
COTRN02C ──CALL──► CSUTLDTC ──CALL──► CEEDAYS (LE runtime)
CORPT00C ──CALL──► CSUTLDTC ──CALL──► CEEDAYS (LE runtime)

CBSTM03A ──CALL──► CBSTM03B (statement file I/O subroutine)
CBSTM03A ──CALL──► CEE3ABD  (LE abend)

CBACT01C ──CALL──► COBDATFT (ASM date format utility)
CBACT01C ──CALL──► CEE3ABD

COBSWAIT ──CALL──► MVSWAIT  (ASM wait utility)

CBACT02C ──CALL──► CEE3ABD
CBACT03C ──CALL──► CEE3ABD
CBACT04C ──CALL──► CEE3ABD
CBCUS01C ──CALL──► CEE3ABD
CBTRN01C ──CALL──► CEE3ABD
CBTRN02C ──CALL──► CEE3ABD
CBTRN03C ──CALL──► CEE3ABD
CBEXPORT ──CALL──► CEE3ABD
CBIMPORT ──CALL──► CEE3ABD
```

| Caller | Called Program | Purpose |
|--------|---------------|---------|
| COTRN02C | CSUTLDTC | Validate date input on transaction add |
| CORPT00C | CSUTLDTC | Validate date range for report |
| CSUTLDTC | CEEDAYS | LE intrinsic: convert date to Lilian days |
| CBSTM03A | CBSTM03B | Subroutine for writing statement output files |
| CBSTM03A | CEE3ABD | LE intrinsic: abnormal termination |
| CBACT01C | COBDATFT | Assembler: format date fields |
| COBSWAIT | MVSWAIT | Assembler: wait/delay utility |
| CB* (batch) | CEE3ABD | LE intrinsic: abnormal termination on error |

---

## 2. Copybook Inclusion Map

### 2.1 Most-Used Copybooks (by program count)

| Rank | Copybook | # Programs | Programs Using It |
|------|----------|----------:|-------------------|
| 1 | COCOM01Y | 15 | All 17 online programs (except some batch) |
| 2 | COTTL01Y | 14 | All online programs |
| 3 | CSDAT01Y | 14 | All online programs |
| 4 | CSMSG01Y | 14 | All online programs |
| 5 | DFHAID | 14 | All online (CICS AID key definitions) |
| 6 | DFHBMSCA | 14 | All online (BMS attribute constants) |
| 7 | CSUSR01Y | 12 | All online + some batch |
| 8 | CVACT01Y | 12 | Account-related programs + batch |
| 9 | CVTRA05Y | 10 | Transaction-related programs |
| 10 | CVACT03Y | 10 | Cross-reference lookups |

### 2.2 Program-to-Copybook Matrix (Core Programs)

| Program | COCOM01Y | CVACT01Y | CVACT02Y | CVACT03Y | CVCUS01Y | CVTRA05Y | CSUSR01Y | BMS Copy |
|---------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COSGN00C | ● | | | | | | ● | COSGN00 |
| COMEN01C | ● | | | | | | ● | COMEN01 |
| COADM01C | ● | | | | | | ● | COADM01 |
| COACTVWC | ● | ● | ● | ● | ● | | ● | COACTVW |
| COACTUPC | ● | ● | | ● | ● | | ● | COACTUP |
| COCRDLIC | ● | | ● | | | | ● | COCRDLI |
| COCRDSLC | ● | | ● | | ● | | ● | COCRDSL |
| COCRDUPC | ● | | ● | | ● | | ● | COCRDUP |
| COTRN00C | ● | | | | | ● | | COTRN00 |
| COTRN01C | ● | | | | | ● | | COTRN01 |
| COTRN02C | ● | ● | | ● | | ● | | COTRN02 |
| CORPT00C | ● | | | | | ● | | CORPT00 |
| COBIL00C | ● | ● | | ● | | ● | | COBIL00 |
| COUSR00C | ● | | | | | | ● | COUSR00 |
| COUSR01C | ● | | | | | | ● | COUSR01 |
| COUSR02C | ● | | | | | | ● | COUSR02 |
| COUSR03C | ● | | | | | | ● | COUSR03 |
| CBACT01C | | ● | | | | | | -- |
| CBACT02C | | | ● | | | | | -- |
| CBACT03C | | | | ● | | | | -- |
| CBACT04C | | ● | | ● | | ● | | -- |
| CBCUS01C | | | | | ● | | | -- |
| CBTRN01C | | ● | ● | ● | ● | ● | | -- |
| CBTRN02C | | ● | | ● | | ● | | -- |
| CBTRN03C | | | | ● | | ● | | -- |
| CBSTM03A | | ● | | ● | | | | -- |
| CBEXPORT | | ● | ● | ● | ● | ● | | -- |
| CBIMPORT | | ● | ● | ● | ● | ● | | -- |

---

## 3. VSAM File Access by Program

### 3.1 Online Program File Access

| Program | USRSEC | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT |
|---------|:------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COSGN00C | R | | | | | |
| COACTVWC | | R | R | R | R | |
| COACTUPC | | RW | | R | RW | |
| COCRDLIC | | | R | | | |
| COCRDSLC | | | R | | R | |
| COCRDUPC | | | RW | | | |
| COTRN00C | | | | | | R |
| COTRN01C | | | | | | R |
| COTRN02C | | R | | R | | RW |
| COBIL00C | | RW | | R | | W |
| CORPT00C | | | | | | R (via TDQ) |
| COUSR00C | R | | | | | |
| COUSR01C | W | | | | | |
| COUSR02C | RW | | | | | |
| COUSR03C | RD | | | | | |

**Legend:** R=Read, W=Write, RW=Read+Rewrite, RD=Read+Delete

### 3.2 Batch Program File Access

| Program | ACCTDATA | CARDDATA | CARDXREF | CUSTDATA | TRANSACT | DALYTRAN | TCATBAL | DISCGRP | TRANTYPE | TRANCATG |
|---------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:-------:|:--------:|:--------:|
| CBACT01C | R | | | | | | | | | |
| CBACT02C | | R | | | | | | | | |
| CBACT03C | | | R | | | | | | | |
| CBACT04C | R | | R | | R | | RW | R | | |
| CBCUS01C | | | | R | | | | | | |
| CBTRN01C | R | R | R | R | RW | R | | | | |
| CBTRN02C | RW | | R | | RW | R | RW | | | |
| CBTRN03C | | | R | | R | | | | R | R |
| CBSTM03A | R | | R | R | R | | | | | |
| CBEXPORT | R | R | R | R | R | | | | | |
| CBIMPORT | | W | W | W | W | | | | | |

---

## 4. JCL Job Data Lineage

### 4.1 Batch Cycle Execution Order

The standard nightly batch cycle runs in this sequence:

```
Step 1: CLOSEFIL ──► Close CICS files for exclusive batch access
                     (SET DSNAME(file) CLOSED)
    │
Step 2: Data Refresh (parallel)
    ├── ACCTFILE  ──► Delete/Define/Repro ACCTDATA.VSAM.KSDS from PS
    ├── CARDFILE  ──► Delete/Define/Repro CARDDATA.VSAM.KSDS from PS + build AIX
    ├── CUSTFILE  ──► Delete/Define/Repro CUSTDATA.VSAM.KSDS from PS
    ├── XREFFILE  ──► Delete/Define/Repro CARDXREF.VSAM.KSDS from PS + build AIX
    └── TRANFILE  ──► Delete/Define/Repro TRANSACT.VSAM.KSDS from PS
    │
Step 3: POSTTRAN ──► CBTRN02C reads DALYTRAN, posts to TRANSACT, updates ACCTDATA + TCATBAL
    │
Step 4: INTCALC ──► CBACT04C reads ACCTDATA + DISCGRP + TCATBAL, calculates interest
    │
Step 5: TRANBKP ──► IDCAMS REPRO TRANSACT.VSAM.KSDS to sequential backup (GDG)
    │
Step 6: COMBTRAN ──► SORT: Combine daily + master transactions
    │
Step 7: CREASTMT ──► CBSTM03A reads TRANSACT + XREF + ACCT + CUST,
    │                 produces STATEMNT.PS (text) + STATEMNT.HTML
    │
Step 8: TRANIDX ──► Build alternate index on TRANSACT
    │
Step 9: OPENFIL ──► Re-open CICS files for online access
```

### 4.2 Job-to-Dataset I/O Matrix

| Job | Input Datasets | Output Datasets | Utility |
|-----|---------------|-----------------|---------|
| ACCTFILE | ACCTDATA.PS | ACCTDATA.VSAM.KSDS | IDCAMS |
| CARDFILE | CARDDATA.PS | CARDDATA.VSAM.KSDS + AIX | IDCAMS |
| CUSTFILE | CUSTDATA.PS | CUSTDATA.VSAM.KSDS | IDCAMS |
| XREFFILE | CARDXREF.PS | CARDXREF.VSAM.KSDS + AIX | IDCAMS |
| TRANFILE | TRANSACT.PS | TRANSACT.VSAM.KSDS | IDCAMS |
| DUSRSECJ | Inline data | USRSEC.VSAM.KSDS | IDCAMS + IEBGENER |
| POSTTRAN | DALYTRAN.VSAM.KSDS, CARDXREF, ACCTDATA, TCATBAL | TRANSACT.VSAM.KSDS, ACCTDATA, TCATBAL, DALYREJS | CBTRN02C |
| INTCALC | ACCTDATA, CARDXREF, TCATBAL, DISCGRP, TRANSACT | ACCTDATA (updated), TCATBAL (updated) | CBACT04C |
| TRANBKP | TRANSACT.VSAM.KSDS | TRANSACT.BKUP.SEQ (GDG) | IDCAMS REPRO |
| COMBTRAN | DALYTRAN + TRANSACT | Combined sequential file | SORT |
| CREASTMT | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML, TRXFL.VSAM.KSDS | SORT + CBSTM03A |
| TRANIDX | TRANSACT.VSAM.KSDS | TRANSACT.AIX | IDCAMS |
| TRANREPT | TRANSACT, CARDXREF, TRANTYPE, TRANCATG | SYSOUT (report) | CBTRN03C |
| CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPORT.PS | CBEXPORT |
| CBIMPORT | EXPORT.PS | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | CBIMPORT |
| TXT2PDF1 | STATEMNT.PS | STATEMNT.PS.PDF | TXT2PDF |
| READACCT | ACCTDATA.VSAM.KSDS | SYSOUT + flat files | CBACT01C |
| READCARD | CARDDATA.VSAM.KSDS | SYSOUT | CBACT02C |
| READCUST | CUSTDATA.VSAM.KSDS | SYSOUT | CBCUS01C |
| READXREF | CARDXREF.VSAM.KSDS | SYSOUT | CBACT03C |

### 4.3 Dataset Lifecycle

```
                    ┌─────────────────────────────────────────────────┐
                    │              VSAM MASTER FILES                  │
                    │  ┌───────────┐ ┌───────────┐ ┌───────────┐    │
PS flat files ─────►│  │ ACCTDATA  │ │ CARDDATA  │ │ CUSTDATA  │    │
(data refresh)      │  │  VSAM     │ │  VSAM     │ │  VSAM     │    │
                    │  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘    │
                    │        │             │             │            │
                    │  ┌─────┴─────┐ ┌─────┴─────┐                   │
                    │  │ CARDXREF  │ │ TRANSACT  │◄── DALYTRAN       │
                    │  │  VSAM     │ │  VSAM     │   (daily input)   │
                    │  └───────────┘ └─────┬─────┘                   │
                    └──────────────────────┼─────────────────────────┘
                                           │
                           ┌───────────────┼───────────────┐
                           ▼               ▼               ▼
                    ┌────────────┐  ┌────────────┐  ┌────────────┐
                    │ TRANBKP    │  │ STATEMNT   │  │ TRANREPT   │
                    │ (backup)   │  │ (PS+HTML)  │  │ (report)   │
                    └────────────┘  └────────────┘  └────────────┘
```

---

## 5. CICS Resource Dependencies

### 5.1 CICS File Definitions (from CSD)

| CICS File Name | VSAM Dataset | Access | Programs |
|---------------|-------------|--------|----------|
| USRSEC | USRSEC.VSAM.KSDS | R/W | COSGN00C, COUSR00-03C |
| ACCTDAT | ACCTDATA.VSAM.KSDS | R/W | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| CARDDAT | CARDDATA.VSAM.KSDS | R/W | COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDAT | CUSTDATA.VSAM.KSDS | R | COACTVWC, COCRDSLC, COCRDUPC, COACTUPC |
| CARDXREF | CARDXREF.VSAM.KSDS | R | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| TRANSACT | TRANSACT.VSAM.KSDS | R/W | COTRN00C, COTRN01C, COTRN02C, COBIL00C |

### 5.2 CICS Transaction Definitions

| Transaction | Program | Description |
|-------------|---------|-------------|
| CC00 | COSGN00C | Signon |
| CM00 | COMEN01C | Main Menu |
| CA00 | COADM01C | Admin Menu |
| CA01 | COACTVWC | Account View |
| CA02 | COACTUPC | Account Update |
| CC01 | COCRDLIC | Card List |
| CC02 | COCRDSLC | Card Detail |
| CC03 | COCRDUPC | Card Update |
| CT00 | COTRN00C | Transaction List |
| CT01 | COTRN01C | Transaction View |
| CT02 | COTRN02C | Transaction Add |
| CR00 | CORPT00C | Reports |
| CB00 | COBIL00C | Bill Payment |
| CU00 | COUSR00C | User List |
| CU01 | COUSR01C | User Add |
| CU02 | COUSR02C | User Update |
| CU03 | COUSR03C | User Delete |

---

## 6. Optional Module Dependencies

### 6.1 Authorization Module (IMS/DB2/MQ)

```
MQ Queue (CDRA) ──► COPAUA0C (trigger) ──► IMS DB (DBPAUTP0)
                                            │
                                   ┌────────┴────────┐
                                   ▼                  ▼
                              COPAUS0C           COPAUS2C
                            (summary view)     (mark fraud → DB2)
                                   │
                                   ▼
                              COPAUS1C
                            (detail view)

CBPAUP0C (batch) ──► DB2 AUTHFRDS table (purge old records)

PAUDBLOD ──CALL──► CBLTDLI (IMS) ──► Load DBPAUTP0 database
PAUDBUNL ──CALL──► CBLTDLI (IMS) ──► Unload DBPAUTP0 database
DBUNLDGS ──CALL──► CBLTDLI (IMS) ──► Generalized segment unload
```

### 6.2 Transaction Type DB2 Module

```
COTRTLIC ──SQL──► DB2 TRNTYPE table (SELECT with cursor paging)
    │                                  DB2 TRNTYCAT table
    │
    ├── List/Delete transaction types
    │
COTRTUPC ──SQL──► DB2 TRNTYPE table (INSERT/UPDATE)
    │                                  DB2 TRNTYCAT table
    │
COBTUPDT ──SQL──► DB2 TRNTYPE table (batch UPDATE)
```

### 6.3 VSAM-MQ Module

```
MQ Queue (CDRD) ──► CODATE01 ──► Returns system date via MQ response
MQ Queue (CDRA) ──► COACCT01 ──► Reads ACCTDATA VSAM ──► Returns account via MQ
```

---

## 7. Migration Dependency Sequencing

Based on the dependency analysis, the recommended migration order (bottom-up):

| Wave | Components | Rationale |
|------|-----------|-----------|
| **Wave 1** | Copybooks → Java POJOs/DTOs | No dependencies; pure data structures |
| **Wave 2** | VSAM files → Database tables + JPA entities | Depends on Wave 1 POJOs |
| **Wave 3** | Utility programs (CSUTLDTC, CBSTM03B) | Shared services, no UI |
| **Wave 4** | Batch programs (CBTRN02C, CBACT04C, CBSTM03A) → Spring Batch | Depends on Wave 2+3 |
| **Wave 5** | Read-only online screens (COACTVWC, COCRDSLC, COTRN01C) | Low risk, no writes |
| **Wave 6** | CRUD online screens (COACTUPC, COCRDUPC, COTRN02C) | Write operations |
| **Wave 7** | Bill Payment (COBIL00C) | Financial transaction, high risk |
| **Wave 8** | Signon + Menus (COSGN00C, COMEN01C, COADM01C) | Navigation/auth |
| **Wave 9** | User Admin (COUSR00-03C) | Admin functions, lower priority |
| **Wave 10** | JCL jobs → Scheduled tasks / CI-CD pipelines | Infrastructure layer |
| **Wave 11** | Optional modules (IMS/DB2/MQ) | Only if needed |
