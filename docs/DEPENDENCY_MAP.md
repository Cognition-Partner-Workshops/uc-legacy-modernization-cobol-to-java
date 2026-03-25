# Dependency Map -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Scope:** All programs, copybooks, JCL jobs, and VSAM files  
> **Contents:** Call graph, CICS transfer flow, copybook inclusion map, batch data lineage

---

## 1. Online Program Call Graph (CICS XCTL / LINK)

The online CICS programs navigate between each other using `EXEC CICS XCTL` (transfer control) and `EXEC CICS RETURN TRANSID` (return with next transaction). The COMMAREA copybook `COCOM01Y` carries context (user, account, card selection) across transfers.

```
                           ┌──────────────┐
                           │  COSGN00C    │
                           │  (Sign-on)   │
                           │  Trans: CC00 │
                           └──────┬───────┘
                                  │ XCTL on successful login
                         ┌────────┴────────┐
                         │                 │
                    (User Type='U')   (User Type='A')
                         │                 │
                         v                 v
                  ┌─────────────┐   ┌─────────────┐
                  │ COMEN01C    │   │ COADM01C    │
                  │ (Main Menu) │   │ (Admin Menu)│
                  │ Trans: CM00 │   │ Trans: CA00 │
                  └──────┬──────┘   └──────┬──────┘
                         │                 │
          ┌──────┬───────┼────────┬────────┤
          │      │       │        │        │
          v      v       v        v        v
     ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────────┐
     │COACT│ │COCRD│ │COTRN│ │COBIL│ │COUSR00C │
     │VWCA1│ │LIC  │ │00C  │ │00C  │ │(UserLst)│
     │AcctV│ │CrdLs│ │TrnLs│ │Bill │ │   CU00  │
     └──┬──┘ └──┬──┘ └──┬──┘ └─────┘ └────┬────┘
        │       │       │                  │
        v       v       v           ┌──────┼──────┐
     ┌─────┐ ┌─────┐ ┌─────┐       v      v      v
     │COACT│ │COCRD│ │COTRN│  ┌──────┐┌──────┐┌──────┐
     │UPC  │ │SLC  │ │01C  │  │COUSR ││COUSR ││COUSR │
     │AcctU│ │CrdDt│ │TrnVw│  │01C   ││02C   ││03C   │
     └─────┘ └──┬──┘ └─────┘  │UsrAdd││UsrUpd││UsrDel│
                │              └──────┘└──────┘└──────┘
                v
             ┌─────┐     ┌─────────┐
             │COCRD│     │CORPT00C │
             │UPC  │     │(Reports)│
             │CrdUp│     │  CR00   │
             └─────┘     └─────────┘
```

### Transfer Detail Table

| Source Program | Target Program | Mechanism | Condition |
|---------------|---------------|-----------|-----------|
| COSGN00C | COMEN01C | XCTL | User type = 'U' (regular) |
| COSGN00C | COADM01C | XCTL | User type = 'A' (admin) |
| COMEN01C | COACTVWC / COCRDLIC / COTRN00C / COBIL00C / CORPT00C | XCTL | Menu option selection (via COMEN02Y table) |
| COADM01C | COUSR00C | XCTL | Admin menu option (via COADM02Y table) |
| COACTVWC | COACTUPC | XCTL | User selects "Update" on account view |
| COCRDLIC | COCRDSLC | XCTL | User selects card for viewing |
| COCRDLIC | COCRDUPC | XCTL | User selects card for updating |
| COCRDSLC | COCRDUPC | XCTL | User selects "Update" on card view |
| COTRN00C | COTRN01C | XCTL | User selects transaction to view |
| COTRN00C | COTRN02C | XCTL | User selects "Add" transaction |
| COUSR00C | COUSR01C | XCTL | Admin selects "Add" user |
| COUSR00C | COUSR02C | XCTL | Admin selects user for update |
| COUSR00C | COUSR03C | XCTL | Admin selects user for delete |
| Any program | COMEN01C / COADM01C | XCTL | PF3 (return to menu) |
| Any program | COSGN00C | XCTL | PF3 from menu (return to sign-on) |

---

## 2. Batch Program CALL Graph

Batch programs use `CALL 'program'` for sub-program invocation and `CALL 'CEE3ABD'` for abnormal termination (LE abend).

```
CBSTM03A (Statement Driver)
    │
    └──► CALL 'CBSTM03B'  (Statement I/O helper -- called 13 times)

CBACT01C (Account Reader)
    │
    └──► CALL 'COBDATFT'  (Assembler date format conversion)

COTRN02C (Transaction Add - online)
    │
    └──► CALL 'CSUTLDTC'  (Date utility -- called 2 times)

COBSWAIT (Wait Utility)
    │
    └──► CALL 'MVSWAIT'   (Assembler wait routine)

CSUTLDTC (Date Utility)
    │
    └──► CALL 'CEEDAYS'   (LE date intrinsic)
```

### Abend Handler Calls (CEE3ABD)

All batch programs call `CEE3ABD` for fatal error handling:
CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT

---

## 3. Copybook Inclusion Map

### 3.1 Which Programs Include Which Copybooks

| Copybook | Included By (Programs) | Usage |
|----------|----------------------|-------|
| **COCOM01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | COMMAREA -- all 17 online programs |
| **COTTL01Y** | All 17 online programs | Screen title headers |
| **CSDAT01Y** | All 17 online programs | Date/time fields |
| **CSMSG01Y** | All 17 online programs | Message area |
| **DFHAID** | All 17 online programs | AID key definitions (PF keys) |
| **DFHBMSCA** | All 17 online programs | BMS attributes |
| **CVACT01Y** | COACTUPC, COACTVWC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A | Account record -- 9 programs |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT | Card record -- 8 programs |
| **CVACT03Y** | COACTUPC, COACTVWC, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C, CBEXPORT, CBIMPORT | Cross-reference -- 10 programs |
| **CVCUS01Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | Customer record -- 8 programs |
| **CVTRA05Y** | COBIL00C, COTRN02C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | Transaction record -- 8 programs |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | Daily transaction -- 2 batch programs |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Category balance -- 2 batch programs |
| **CVTRA02Y** | CBACT04C | Disclosure/interest rates -- 1 program |
| **CVTRA03Y** | CBTRN03C | Transaction type ref -- 1 program |
| **CVTRA04Y** | CBTRN03C | Transaction category ref -- 1 program |
| **CVTRA07Y** | CBTRN03C | Report structures -- 1 program |
| **CSUSR01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COBIL00C, CORPT00C, COADM01C, COMEN01C | User security -- all online programs |
| **CSLKPCDY** | COACTUPC | State/country lookup -- 1 program |
| **CSSETATY** | COACTUPC | Attribute setting -- 1 program (38 COPY REPLACING) |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | String utility -- 5 programs |
| **CSUTLDPY** | COACTUPC | Date utility params -- 1 program |
| **CSUTLDWY** | COACTUPC | Date utility WS -- 1 program |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Card data (extended) -- 5 programs |
| **COMEN02Y** | COMEN01C | Menu definitions -- 1 program |
| **COADM02Y** | COADM01C | Admin menu definitions -- 1 program |
| **COSTM01** | CBSTM03A | Statement transaction layout -- 1 program |
| **CUSTREC** | CBSTM03A | Customer record (stmts) -- 1 program |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export record layout -- 2 programs |
| **CODATECN** | CBACT01C | Date conversion interface -- 1 program |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | Dual message area -- 4 programs |

### 3.2 BMS Map to Program Mapping

| BMS Map | Copybook (cpy-bms) | Program | Screen |
|---------|-------------------|---------|--------|
| COSGN00.bms | COSGN00.CPY | COSGN00C | Sign-on |
| COMEN01.bms | COMEN01.CPY | COMEN01C | Main Menu |
| COADM01.bms | COADM01.CPY | COADM01C | Admin Menu |
| COACTVW.bms | COACTVW.CPY | COACTVWC | Account View |
| COACTUP.bms | COACTUP.CPY | COACTUPC | Account Update |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC | Card List |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC | Card Detail |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC | Card Update |
| COTRN00.bms | COTRN00.CPY | COTRN00C | Transaction List |
| COTRN01.bms | COTRN01.CPY | COTRN01C | Transaction View |
| COTRN02.bms | COTRN02.CPY | COTRN02C | Transaction Add |
| COBIL00.bms | COBIL00.CPY | COBIL00C | Bill Payment |
| CORPT00.bms | CORPT00.CPY | CORPT00C | Report Request |
| COUSR00.bms | COUSR00.CPY | COUSR00C | User List |
| COUSR01.bms | COUSR01.CPY | COUSR01C | User Add |
| COUSR02.bms | COUSR02.CPY | COUSR02C | User Update |
| COUSR03.bms | COUSR03.CPY | COUSR03C | User Delete |

---

## 4. VSAM File Access Map

### 4.1 Online Programs -- CICS File Access

| VSAM File (DD Name) | Dataset | READ | WRITE | REWRITE | DELETE | STARTBR/READNEXT/READPREV |
|---------------------|---------|------|-------|---------|--------|--------------------------|
| **ACCTDAT** (Account) | ACCTDATA.VSAM.KSDS | COACTVWC, COACTUPC, COBIL00C | -- | COACTUPC, COBIL00C | -- | -- |
| **CARDDAT** (Card) | CARDDATA.VSAM.KSDS | COCRDSLC, COCRDUPC | -- | COCRDUPC | -- | COCRDLIC |
| **CUSTDAT** (Customer) | CUSTDATA.VSAM.KSDS | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | -- | -- | -- | -- |
| **CARDXREF** (XREF) | CARDXREF.VSAM.KSDS | COACTUPC, COACTVWC, COTRN02C | -- | -- | -- | -- |
| **TRANSACT** (Transactions) | TRANSACT.VSAM.KSDS | COTRN01C | COTRN02C, COBIL00C | -- | -- | COTRN00C, COBIL00C |
| **USRSEC** (Users) | USRSEC.VSAM.KSDS | COSGN00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C | COUSR03C | COUSR00C |
| **CXACAIX** (XREF Alt Index) | CARDXREF AIX | COBIL00C, COTRN02C | -- | -- | -- | -- |

### 4.2 Batch Programs -- File I/O

| Program | Input Files (READ) | Output Files (WRITE) |
|---------|-------------------|---------------------|
| **CBTRN01C** | DALYTRAN (daily trans), CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE (master transactions) |
| **CBTRN02C** | DALYTRAN | TRANFILE, DALYREJS (rejects), ACCTFILE (balance update), TCATBALF (cat balance update) |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report output) |
| **CBACT04C** | TCATBALF, XREFFILE, DISCGRP, ACCTFILE | TRANSACT (interest transactions), ACCTFILE (balance update) |
| **CBSTM03A** | TRNXFILE (sorted trans), XREFFILE, ACCTFILE, CUSTFILE | STMTFILE (text), HTMLFILE (HTML) |
| **CBSTM03B** | (sub-program: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE) | (called by CBSTM03A) |
| **CBACT01C** | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE |
| **CBACT02C** | CARDFILE | (display only) |
| **CBACT03C** | XREFFILE | (display only) |
| **CBCUS01C** | CUSTFILE | (display only) |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (export) |
| **CBIMPORT** | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

---

## 5. JCL Job Data Lineage

### 5.1 Nightly Batch Cycle (Recommended Order)

```
Step 1: CLOSEFIL ──────────────── Close CICS files for exclusive batch access
            │
Step 2: Data Refresh (parallel) ─ Reload reference/master VSAM files
            │  ACCTFILE  ──► ACCTDATA.VSAM.KSDS    (from .PS flat file)
            │  CARDFILE  ──► CARDDATA.VSAM.KSDS
            │  CUSTFILE  ──► CUSTDATA.VSAM.KSDS
            │  XREFFILE  ──► CARDXREF.VSAM.KSDS
            │  TRANTYPE  ──► TRANTYPE.VSAM.KSDS
            │  TRANCATG  ──► TRANCATG.VSAM.KSDS
            │  TCATBALF  ──► TCATBALF.VSAM.KSDS
            │  DISCGRP   ──► DISCGRP.VSAM.KSDS
            │  DUSRSECJ  ──► USRSEC.VSAM.KSDS
            │
Step 3: POSTTRAN ──────────────── Transaction posting (CBTRN02C)
            │  Input:  DALYTRAN.PS ──► daily transactions
            │  Output: TRANSACT.VSAM.KSDS ──► posted to master
            │          DALYREJS ──► rejected transactions
            │          ACCTFILE ──► updated account balances
            │          TCATBALF ──► updated category balances
            │
Step 4: INTCALC ───────────────── Interest calculation (CBACT04C)
            │  Input:  TCATBALF, XREFFILE, DISCGRP, ACCTFILE
            │  Output: TRANSACT ──► interest charge transactions
            │          ACCTFILE ──► updated with interest
            │
Step 5: TRANBKP ───────────────── Backup transactions to GDG
            │  TRANSACT.VSAM.KSDS ──► TRANSACT.BKUP(+1)
            │
Step 6: COMBTRAN ──────────────── Combine daily + master (CBTRN01C)
            │  Input:  DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE
            │  Output: TRANFILE (validated/enriched transactions)
            │
Step 7: CREASTMT ──────────────── Statement generation (SORT + CBSTM03A)
            │  Input:  TRANSACT.VSAM.KSDS (sorted), XREFFILE, ACCTFILE, CUSTFILE
            │  Output: STATEMNT.PS (text statements)
            │          STATEMNT.HTML (HTML statements)
            │
Step 8: TRANREPT ──────────────── Transaction report (SORT + CBTRN03C)
            │  Input:  TRANSACT.BKUP(+1), CARDXREF, TRANTYPE, TRANCATG, DATEPARM
            │  Output: TRANREPT(+1) (daily transaction report)
            │
Step 9: TRANIDX ───────────────── Rebuild alternate indexes on TRANSACT
            │
Step 10: OPENFIL ──────────────── Re-open CICS files for online access
```

### 5.2 JCL Job to Program Mapping

| JCL Job | Step(s) | Program(s) Executed | Key Datasets |
|---------|---------|-------------------|--------------|
| POSTTRAN | STEP10 | CBTRN02C | DALYTRAN, TRANSACT, DALYREJS, ACCTFILE, TCATBALF |
| INTCALC | STEP10 | CBACT04C | TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT |
| COMBTRAN | STEP10 | CBTRN01C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE |
| CREASTMT | STEP010 (SORT), STEP040 (CBSTM03A) | SORT, CBSTM03A | TRANSACT, TRXFL.SEQ, TRXFL.VSAM.KSDS, XREFFILE, ACCTFILE, CUSTFILE, STATEMNT |
| TRANREPT | STEP05R (SORT), STEP10R (CBTRN03C) | SORT, CBTRN03C | TRANSACT.BKUP, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, TRANREPT |
| CBEXPORT | STEP10 | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE |
| CBIMPORT | STEP10 | CBIMPORT | EXPFILE, CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT |
| WAITSTEP | WAIT | COBSWAIT | -- |
| TXT2PDF1 | TXT2PDF | IKJEFT1B (TSO) | STATEMNT.PS |
| TRANBKP | STEP10 | IDCAMS REPRO | TRANSACT.VSAM.KSDS -> TRANSACT.BKUP(+1) |
| CLOSEFIL | -- | IDCAMS | (CICS file close) |
| OPENFIL | -- | IDCAMS | (CICS file open) |
| CBADMCDJ | Multiple | IGYCRCTL (COBOL compiler) | All source members |

### 5.3 Dataset Catalog

| Logical Dataset (DSN) | Type | Record Layout | Key | Used By |
|----------------------|------|--------------|-----|---------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | VSAM KSDS | CVACT01Y (300 bytes) | ACCT-ID | Online + Batch |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | VSAM KSDS | CVACT02Y (150 bytes) | CARD-NUM | Online + Batch |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | VSAM KSDS | CVCUS01Y (500 bytes) | CUST-ID | Online + Batch |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | VSAM KSDS | CVACT03Y (50 bytes) | XREF-CARD-NUM | Online + Batch |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | VSAM KSDS | CVTRA05Y (350 bytes) | TRAN-ID | Online + Batch |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | VSAM KSDS | CSUSR01Y (80 bytes) | SEC-USR-ID | Online |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | VSAM KSDS | CVTRA03Y (60 bytes) | TRAN-TYPE | Batch (report) |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | VSAM KSDS | CVTRA04Y (60 bytes) | TRAN-TYPE-CD + CAT-CD | Batch (report) |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | VSAM KSDS | CVTRA01Y (50 bytes) | ACCT + TYPE + CAT | Batch (posting/interest) |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | VSAM KSDS | CVTRA02Y (50 bytes) | GROUP + TYPE + CAT | Batch (interest) |
| AWS.M2.CARDDEMO.DALYTRAN.PS | Sequential | CVTRA06Y (350 bytes) | N/A | Batch input |
| AWS.M2.CARDDEMO.TRANSACT.BKUP(+n) | GDG | CVTRA05Y (350 bytes) | N/A | Backup/Report |
| AWS.M2.CARDDEMO.STATEMNT.PS | Sequential | Text output | N/A | Statement output |
| AWS.M2.CARDDEMO.STATEMNT.HTML | Sequential | HTML output | N/A | Statement output |
| AWS.M2.CARDDEMO.TRANREPT(+n) | GDG | Report text | N/A | Report output |
| AWS.M2.CARDDEMO.DATEPARM | Sequential | Date parameters | N/A | Report date range |
| AWS.M2.CARDDEMO.DALYREJS | Sequential | Reject records | N/A | CBTRN02C rejects |
| AWS.M2.CARDDEMO.TRXFL.SEQ | Sequential | COSTM01 (sorted) | N/A | CREASTMT intermediate |
| AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS | VSAM KSDS | COSTM01 | CARD+TRAN | CREASTMT intermediate |

---

## 6. Data Flow Diagram -- End-to-End Transaction Lifecycle

```
 External        Daily Batch Files         VSAM Master Files        Online CICS         Reports/Output
 Source          (Sequential PS)           (KSDS)                   Programs
─────────       ─────────────────          ──────────────────       ──────────────      ──────────────

 Card            DALYTRAN.PS              TRANSACT.VSAM.KSDS       COTRN02C            TRANREPT(+n)
 Swipe     ──►  (daily intake)    ──►    (master transactions) ◄── (Add Txn)    ──►   (daily report)
                      │                         │                                            │
                      │ CBTRN02C                │                                      CBTRN03C
                      │ (validate,              │                                      (generate)
                      │  post)                  │
                      │                         │
                      ├──► DALYREJS             │ CBACT04C
                      │    (rejects)            │ (interest calc)
                      │                         │
                      │                         ▼
                      │                   ACCTDATA.VSAM.KSDS
 Account         ─── ─┤                  (account balances)  ◄── COBIL00C
 Setup                │                         │                  (Bill Pay)
                      │                         │
                      │                         ▼
                      │                   TCATBALF.VSAM.KSDS
                      │                   (category balances)
                      │
                      │                   CARDXREF.VSAM.KSDS  ◄── used by all
                      │                   (card-acct xref)        lookups
                      │
                      │                         │
                      ▼                         ▼
                 CBSTM03A/B              STATEMNT.PS / .HTML
                 (statement gen)          (customer statements)
```

---

## 7. Shared Dependencies Summary

### Most-Depended-On Assets

| Asset | Dependent Count | Role |
|-------|----------------|------|
| COCOM01Y (Common Area) | 17 programs | Backbone of all online navigation |
| CVACT03Y (Card XREF) | 10 programs | Central lookup joining Card/Account/Customer |
| CVACT01Y (Account) | 9 programs | Core business entity |
| CVTRA05Y (Transaction) | 8 programs | Core transaction record |
| CVCUS01Y (Customer) | 8 programs | Core customer record |
| CVACT02Y (Card) | 8 programs | Core card record |
| CSUSR01Y (User Security) | 17 programs | Authentication for all online screens |
| CSSTRPFY (String Utility) | 5 programs | Shared string handling |
| CSLKPCDY (Lookup Codes) | 1 program (COACTUPC) | Large but isolated dependency |

### Orphaned / Low-Dependency Assets

| Asset | Dependencies | Note |
|-------|-------------|------|
| UNUSED1Y | 0 programs | Deprecated -- candidate for removal |
| COBSWAIT | Called by WAITSTEP JCL only | Thin wrapper around assembler |
| CBSTM03B | Called by CBSTM03A only | Tightly coupled sub-program |
