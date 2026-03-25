# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Analysis:** Static code analysis of CALL, COPY, EXEC CICS, and JCL DD statements

---

## 1. Program Call Graph

### 1.1 Online CICS Program Navigation (XCTL / LINK)

The online system uses CICS `XCTL` (transfer control) for screen-to-screen navigation. The COMMAREA (`COCOM01Y`) carries context between programs.

```
                    ┌─────────────┐
                    │  COSGN00C   │  Login Screen
                    │  (CC00)     │
                    └──────┬──────┘
                           │ XCTL (on successful login)
                    ┌──────▼──────┐
               ┌────│  COMEN01C   │────┐  Main Menu
               │    │  (CM00)     │    │
               │    └──────┬──────┘    │
               │           │           │
      ┌────────▼───┐  ┌───▼────┐  ┌───▼────────┐
      │ COACTVWC   │  │COTRN00C│  │ COADM01C   │  (Admin only)
      │ Acct View  │  │Trn List│  │ Admin Menu  │
      └────┬───────┘  └───┬────┘  └───┬────────┘
           │               │           │
      ┌────▼───────┐  ┌───▼────┐  ┌───▼────────┐
      │ COACTUPC   │  │COTRN01C│  │ COUSR00C   │
      │ Acct Update│  │Trn View│  │ User List   │
      └────────────┘  └───┬────┘  └───┬────────┘
                          │           │
                     ┌────▼────┐  ┌───▼──┬──────┬──────┐
                     │COTRN02C │  │USR01C│USR02C│USR03C│
                     │Trn Add  │  │ Add  │Update│Delete│
                     └─────────┘  └──────┴──────┴──────┘

      ┌────────────┐  ┌────────────┐  ┌────────────┐
      │ COCRDLIC   │  │ CORPT00C   │  │ COBIL00C   │
      │ Card List  │  │ Trn Report │  │ Bill Pay   │
      └────┬───────┘  └────────────┘  └────────────┘
           │
      ┌────▼───────┐
      │ COCRDSLC   │
      │ Card View  │
      └────┬───────┘
           │
      ┌────▼───────┐
      │ COCRDUPC   │
      │ Card Update│
      └────────────┘
```

### 1.2 Subroutine CALL Graph (Static CALL)

```
CBSTM03A ──CALL──► CBSTM03B    (Statement: file I/O subroutine)
CBSTM03A ──CALL──► CEE3ABD     (LE: abnormal termination)

CBACT01C ──CALL──► COBDATFT    (ASM: date formatting)
CBACT01C ──CALL──► CEE3ABD

COTRN02C ──CALL──► CSUTLDTC    (Date validation via CEEDAYS)
COBIL00C ──CALL──► CSUTLDTC    (Date validation via CEEDAYS)

CSUTLDTC ──CALL──► CEEDAYS     (LE: date conversion callable service)

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

### 1.3 Optional Module Call Graph

```
Authorization Module:
  COPAUA0C ──CALL──► MQOPEN, MQGET, MQPUT, MQCLOSE  (MQ API)
  COPAUA0C ──IMS DL/I──► Auth DB (ISRT, GHU, REPL)
  COPAUS2C ──EXEC SQL──► DB2 CARDDEMO tables (fraud marking)
  PAUDBLOD ──CALL──► CBLTDLI  (IMS batch: ISRT, GU)
  PAUDBUNL ──CALL──► CBLTDLI  (IMS batch: GN, GNP)

Transaction Type DB2 Module:
  COTRTLIC ──EXEC SQL──► CARDDEMO.TRANSACTION_TYPE (SELECT, DELETE)
  COTRTUPC ──EXEC SQL──► CARDDEMO.TRANSACTION_TYPE (SELECT, INSERT, UPDATE, DELETE)
  COBTUPDT ──EXEC SQL──► CARDDEMO.TRANSACTION_TYPE (INSERT, UPDATE, DELETE)

VSAM-MQ Module:
  COACCT01 ──CALL──► MQOPEN, MQGET, MQPUT, MQCLOSE  (Account inquiry)
  COACCT01 ──EXEC CICS READ──► ACCTDATA (account lookup)
  CODATE01 ──CALL──► MQOPEN, MQGET, MQPUT, MQCLOSE  (System date)
  CODATE01 ──EXEC CICS ASKTIME/FORMATTIME  (system time)
```

---

## 2. Copybook Dependency Matrix

This shows which programs include which copybooks via `COPY` statements.

### 2.1 Data Copybooks Used by Programs

| Copybook | Used By (Programs) | Entity |
|----------|-------------------|--------|
| **COCOM01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | COMMAREA |
| **COTTL01Y** | All 17 online programs | Title |
| **CSDAT01Y** | All 17 online programs | Date |
| **CSMSG01Y** | All 17 online programs | Messages |
| **DFHAID** | All 17 online programs | AID keys |
| **DFHBMSCA** | All 17 online programs | BMS attrs |
| **CSUSR01Y** | COSGN00C, COACTUPC, COACTVWC, COUSR00C, COUSR01C, COUSR02C, COUSR03C | User Security |
| **CVACT01Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBSTM03A | Account |
| **CVACT02Y** | COACTVWC, CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C | Card |
| **CVACT03Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A | Card Xref |
| **CVCUS01Y** | COACTUPC, COACTVWC, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, CBSTM03A (via CUSTREC) | Customer |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C | Transaction |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | Daily Tran |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Tran Cat Bal |
| **CVTRA02Y** | CBACT04C | Disclosure Grp |
| **CVTRA03Y** | CBTRN03C | Tran Type |
| **CVTRA04Y** | CBTRN03C | Tran Category |
| **CVTRA07Y** | CBTRN03C | Report Layout |
| **COSTM01** | CBSTM03A | Stmt Tran Layout |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export Layout |
| **CSMSG02Y** | COACTUPC, COACTVWC | Messages (ext) |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Card Detail |
| **CSLKPCDY** | COACTUPC | Lookup Codes |
| **CSSETATY** | COACTUPC (38x), COCRDUPC | Set Attribute |
| **CSSTRPFY** | COACTUPC, COACTVWC | String Format |
| **CSUTLDPY** | COACTUPC | Date Utility |
| **CSUTLDWY** | COACTUPC, COCRDUPC, COTRTUPC | Date Fields |
| **CODATECN** | CBACT01C | Date Convert |

### 2.2 BMS Map Copybook Usage

Each online program copies its corresponding BMS-generated copybook from `app/cpy-bms/`:

| Program | BMS Copybook | Map Set |
|---------|-------------|---------|
| COSGN00C | COSGN00 | COSGN00 |
| COMEN01C | COMEN01 | COMEN01 |
| COADM01C | COADM01 | COADM01 |
| COACTVWC | COACTVW | COACTVW |
| COACTUPC | COACTUP | COACTUP |
| COCRDLIC | COCRDLI | COCRDLI |
| COCRDSLC | COCRDSL | COCRDSL |
| COCRDUPC | COCRDUP | COCRDUP |
| COTRN00C | COTRN00 | COTRN00 |
| COTRN01C | COTRN01 | COTRN01 |
| COTRN02C | COTRN02 | COTRN02 |
| CORPT00C | CORPT00 | CORPT00 |
| COBIL00C | COBIL00 | COBIL00 |
| COUSR00C | COUSR00 | COUSR00 |
| COUSR01C | COUSR01 | COUSR01 |
| COUSR02C | COUSR02 | COUSR02 |
| COUSR03C | COUSR03 | COUSR03 |

---

## 3. Data Lineage: VSAM File Access by Program

### 3.1 CICS Online File Access

| VSAM File (DD Name) | READ | WRITE | REWRITE | DELETE | STARTBR/READNEXT | Programs |
|---------------------|------|-------|---------|--------|-------------------|----------|
| **USRSEC** (User Security) | R | W | RW | D | BR | COSGN00C(R), COUSR00C(BR), COUSR01C(W), COUSR02C(R,RW), COUSR03C(R,D) |
| **ACCTDATA** (Account) | R | - | RW | - | - | COACTVWC(R), COACTUPC(R,RW), COBIL00C(R,RW), COTRN02C(R) |
| **CARDDATA** (Card) | R | - | - | - | - | COACTVWC(R), COCRDSLC(R) |
| **CUSTDATA** (Customer) | R | - | RW | - | - | COACTVWC(R), COACTUPC(R,RW) |
| **TRANSACT** (Transaction) | R | W | - | - | BR | COTRN00C(BR), COTRN01C(R), COTRN02C(R,W), COBIL00C(R,BR) |
| **CARDXREF** (Cross-Ref) | R | - | - | - | BR | COACTVWC(R), COACTUPC(R), COCRDLIC(BR), COCRDUPC(R,BR), COTRN02C(R), COBIL00C(R,BR) |
| **CXREF** (Xref AIX Path) | - | - | - | - | BR | COCRDLIC(BR) |

Legend: R=READ, W=WRITE, RW=REWRITE, D=DELETE, BR=STARTBR/READNEXT/READPREV

### 3.2 Batch File Access

| VSAM/Sequential File | Open Mode | Programs/Jobs |
|---------------------|-----------|---------------|
| **ACCTDATA** (Account VSAM) | INPUT | CBACT01C, CBTRN01C, CBSTM03A/B |
| | I-O | CBTRN02C, CBACT04C |
| **CARDDATA** (Card VSAM) | INPUT | CBACT02C, CBTRN01C |
| **CUSTDATA** (Customer VSAM) | INPUT | CBCUS01C, CBSTM03A/B |
| **CARDXREF** (Xref VSAM) | INPUT | CBACT03C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A/B, CBACT04C |
| **TRANSACT** (Transaction VSAM) | INPUT | CBTRN01C, CBTRN03C |
| | OUTPUT | CBTRN02C, CBACT04C |
| **DALYTRAN** (Daily Trans) | INPUT | CBTRN01C, CBTRN02C |
| **TCATBALF** (Category Balance) | INPUT | CBACT04C |
| | I-O | CBTRN02C |
| **DISCGRP** (Disclosure) | INPUT | CBACT04C |
| **TRANTYPE** (Tran Type) | INPUT | CBTRN03C |
| **TRANCATG** (Tran Category) | INPUT | CBTRN03C |
| **DALYREJS** (Daily Rejects) | OUTPUT | CBTRN02C |
| **STATEMNT.PS** (Statement Text) | OUTPUT | CBSTM03A |
| **STATEMNT.HTML** (Statement HTML) | OUTPUT | CBSTM03A |
| **DATE-PARMS** (Date Parameters) | INPUT | CBTRN03C |
| **REPORT-FILE** (Report Output) | OUTPUT | CBTRN03C |
| **Export flat file** | OUTPUT | CBEXPORT |
| | INPUT | CBIMPORT |
| All entity outputs | OUTPUT | CBIMPORT (Customer, Account, Xref, Transaction, Card) |

---

## 4. JCL Job Data Lineage

### 4.1 Nightly Batch Cycle (in order)

```
Step 1: CLOSEFIL.jcl
   Action: Close CICS files (CEMT SET FILE CLOSED)
   Files affected: All VSAM files opened by CICS

Step 2: Data Refresh (parallel)
   ACCTFILE.jcl:  Flat → ACCTDATA.VSAM.KSDS
   CARDFILE.jcl:  Flat → CARDDATA.VSAM.KSDS
   CUSTFILE.jcl:  Flat → CUSTDATA.VSAM.KSDS
   XREFFILE.jcl:  Flat → CARDXREF.VSAM.KSDS (+AIX)
   TRANFILE.jcl:  Flat → TRANSACT.VSAM.KSDS
   DUSRSECJ.jcl:  Flat → USRSEC.VSAM.KSDS

Step 3: POSTTRAN.jcl  (PGM=CBTRN02C)
   Input:  DALYTRAN, CARDXREF, ACCTDATA, TCATBALF
   Output: TRANSACT (append), DALYREJS, ACCTDATA (update bal), TCATBALF (update bal)

Step 4: INTCALC.jcl  (PGM=CBACT04C)
   Input:  TCATBALF, CARDXREF, DISCGRP, ACCTDATA
   Output: TRANSACT (interest entries), ACCTDATA (update bal)

Step 5: TRANBKP.jcl
   Input:  TRANSACT.VSAM.KSDS
   Output: TRANSACT.GDG (backup generation)

Step 6: COMBTRAN.jcl
   Input:  TRANSACT, DALYTRAN
   Output: Combined transaction dataset

Step 7: CREASTMT.JCL  (PGM=CBSTM03A)
   Input:  TRANSACT (re-sorted), CARDXREF, ACCTDATA, CUSTDATA
   Output: STATEMNT.PS (text), STATEMNT.HTML (HTML)

Step 8: TRANIDX.jcl
   Action: Rebuild alternate indexes on TRANSACT

Step 9: OPENFIL.jcl
   Action: Re-open CICS files (CEMT SET FILE OPEN)
```

### 4.2 Data Flow Diagram

```
                External Input
                     │
                     ▼
              ┌─────────────┐
              │  DALYTRAN    │  Daily Transaction File
              │  (Staging)   │
              └──────┬───────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
   ┌─────────┐ ┌──────────┐ ┌──────────┐
   │CBTRN01C │ │ CBTRN02C │ │ Rejects  │
   │Validate │ │ Post     │ │(DALYREJS)│
   └─────────┘ └────┬─────┘ └──────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
         ▼           ▼           ▼
    ┌─────────┐ ┌─────────┐ ┌─────────┐
    │TRANSACT │ │ACCTDATA │ │TCATBALF │
    │(Master) │ │(Update  │ │(Update  │
    │         │ │ Balance)│ │ Balance)│
    └────┬────┘ └────┬────┘ └─────────┘
         │           │
         │      ┌────▼────┐
         │      │CBACT04C │  Interest Calculation
         │      │         │──────► TRANSACT (interest entries)
         │      └─────────┘        ACCTDATA (update balance)
         │
    ┌────┴─────────────────────────┐
    │                              │
    ▼                              ▼
┌─────────┐                  ┌──────────┐
│CBTRN03C │  Transaction     │CBSTM03A  │  Statement
│ Report  │  Report          │CBSTM03B  │  Generation
└────┬────┘                  └────┬─────┘
     │                            │
     ▼                            ▼
┌─────────┐              ┌────────────────┐
│ REPORT  │              │ STATEMNT.PS    │
│ FILE    │              │ STATEMNT.HTML  │
└─────────┘              └────────────────┘
```

### 4.3 VSAM File Dataset Names

| Logical Name | Physical Dataset | Key | Rec Len |
|-------------|-----------------|-----|---------|
| ACCTDATA | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | 11 bytes @ 0 | 300 |
| CARDDATA | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | 16 bytes @ 0 | 150 |
| CUSTDATA | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | 9 bytes @ 0 | 500 |
| CARDXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | 16 bytes @ 0 | 50 |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | 16 bytes @ 0 | 350 |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | 8 bytes @ 0 | 80 |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.PS | - | 350 |
| TCATBALF | AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS | 17 bytes @ 0 | 50 |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | 16 bytes @ 0 | 50 |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | 2 bytes @ 0 | 60 |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | 6 bytes @ 0 | 60 |

### 4.4 Alternate Indexes

| Base Cluster | AIX Name | AIX Key | Unique? |
|-------------|----------|---------|---------|
| CARDXREF.VSAM.KSDS | CARDXREF.VSAM.AIX | ACCT-ID (11 bytes @ 25) | No |
| TRANSACT.VSAM.KSDS | (Built by TRANIDX.jcl) | Varies | - |

---

## 5. Cross-Cutting Dependencies

### 5.1 Shared Infrastructure

| Component | Depends On | Used By |
|-----------|-----------|---------|
| COCOM01Y (COMMAREA) | - | All 17 online programs |
| CSUTLDTC (Date utility) | CEEDAYS (LE) | COTRN02C, COBIL00C |
| COBDATFT (Date format ASM) | - | CBACT01C |
| CEE3ABD (LE Abend) | - | All batch programs |
| DFHAID / DFHBMSCA | CICS | All online programs |

### 5.2 Programs by Number of File Dependencies

| Program | # Files Accessed | Risk Level |
|---------|-----------------|------------|
| CBTRN02C (Posting) | 6 | **Critical** |
| CBSTM03A (Statement) | 5 (via CBSTM03B) | **High** |
| CBACT04C (Interest) | 5 | **High** |
| CBTRN01C (Validation) | 6 | **High** |
| CBTRN03C (Report) | 6 | **High** |
| COACTUPC (Acct Update) | 3 | **Medium** |
| COACTVWC (Acct View) | 4 | **Medium** |
| CBEXPORT (Export) | 5 | **Medium** |
| CBIMPORT (Import) | 6+1 | **Medium** |
| COBIL00C (Bill Pay) | 3 | **Medium** |
