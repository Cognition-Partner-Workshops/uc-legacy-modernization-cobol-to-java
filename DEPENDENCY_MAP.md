# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** Static analysis of CALL, XCTL, COPY, DATASET, and JCL EXEC statements
> **Purpose:** Call graph, copybook dependencies, and data lineage for modernization planning

---

## 1. Program Call Graph — Online CICS

The online application uses **EXEC CICS XCTL** (transfer control) to navigate between screens. All online programs share state through `CARDDEMO-COMMAREA` (COCOM01Y).

```
                            ┌─────────────┐
                            │  COSGN00C   │  (CC00 - Signon)
                            │  Entry Point│
                            └──────┬──────┘
                                   │ XCTL (authentication)
                      ┌────────────┴────────────┐
                      │                         │
               ┌──────┴──────┐          ┌───────┴──────┐
               │  COMEN01C   │          │  COADM01C    │
               │  User Menu  │          │  Admin Menu  │
               │  (CM00)     │          │  (CA00)      │
               └──────┬──────┘          └───────┬──────┘
                      │ XCTL                    │ XCTL
         ┌────────────┼────────────┐      ┌─────┼─────────────┐
         │            │            │      │     │             │
    ┌────┴────┐ ┌─────┴─────┐ ┌───┴───┐  │  ┌──┴──┐   ┌─────┴─────┐
    │COACTVWC │ │ COCRDLIC  │ │COTRN00C│  │  │COUSR│   │ COPAUS0C* │
    │Acct View│ │ Card List │ │Trn List│  │  │00C  │   │ Auth Summ │
    │(CAVW)   │ │ (CCLI)    │ │(CT00)  │  │  │List │   │ (Optional)│
    └────┬────┘ └──┬────┬───┘ └──┬──┬──┘  │  └──┬──┘   └───────────┘
         │         │    │        │  │      │     │
    ┌────┴────┐    │    │   ┌────┴┐ │      │  ┌──┴──┐
    │COACTUPC │    │    │   │COTRN│ │      │  │COUSR│
    │Acct Upd │    │    │   │01C  │ │      │  │01-03│
    │(CAUP)   │    │    │   │View │ │      │  │CRUD │
    └─────────┘    │    │   └─────┘ │      │  └─────┘
              ┌────┴──┐ │      ┌────┴───┐  │
              │COCRDSLC│ │      │COTRN02C│  │
              │Card Dtl│ │      │Trn Add │  │
              │(CCDL)  │ │      │(CT02)  │  │
              └────────┘ │      └────────┘  │
                    ┌────┴───┐         ┌────┴───┐
                    │COCRDUPC│         │COBIL00C│
                    │Card Upd│         │Bill Pay│
                    │(CCUP)  │         │(CB00)  │
                    └────────┘         └────────┘

              ┌────────┐
              │CORPT00C│  (Accessed from both menus)
              │Reports │
              │(CR00)  │
              └────────┘
```

### 1.1 Detailed XCTL Transfer Table

| Source Program | Target Program | Condition / Trigger                    |
|----------------|----------------|----------------------------------------|
| COSGN00C       | COADM01C       | Admin user login successful            |
| COSGN00C       | COMEN01C       | Regular user login successful          |
| COMEN01C       | COSGN00C       | PF3 (Exit / Logout)                   |
| COMEN01C       | *(menu option)* | Enter key — dispatches to menu target  |
| COADM01C       | COSGN00C       | PF3 (Exit / Logout)                   |
| COADM01C       | *(menu option)* | Enter key — dispatches to admin target |
| COACTVWC       | COMEN01C       | PF3 (Back to menu)                    |
| COACTUPC       | COMEN01C       | PF3 (Back to menu)                    |
| COCRDLIC       | COMEN01C       | PF3 (Back to menu)                    |
| COCRDLIC       | COCRDSLC       | 'S' selection on a card row            |
| COCRDLIC       | COCRDUPC       | 'U' selection on a card row            |
| COCRDSLC       | COCRDLIC       | PF3 (Back to list)                    |
| COCRDUPC       | COCRDLIC       | PF3 (Back to list)                    |
| COTRN00C       | COMEN01C       | PF3 (Back to menu)                    |
| COTRN00C       | COTRN01C       | 'S' selection on a transaction row     |
| COTRN01C       | COTRN00C       | PF3 (Back to list)                    |
| COTRN02C       | COMEN01C       | PF3 (Back to menu)                    |
| COBIL00C       | COMEN01C       | PF3 (Back to menu)                    |
| CORPT00C       | COMEN01C       | PF3 (Back to menu)                    |
| COUSR00C       | COADM01C       | PF3 (Back to admin menu)              |
| COUSR00C       | COUSR01C       | 'A' (Add user)                        |
| COUSR00C       | COUSR02C       | 'U' selection on a user row            |
| COUSR00C       | COUSR03C       | 'D' selection on a user row            |
| COUSR01C       | COUSR00C       | PF3 (Back to user list)               |
| COUSR02C       | COUSR00C       | PF3 (Back to user list)               |
| COUSR03C       | COUSR00C       | PF3 (Back to user list)               |

---

## 2. Program Call Graph — Batch

Batch programs use **CALL** statements (sub-program linkage) and are invoked by JCL.

```
  JCL: POSTTRAN ──► CBTRN02C
                      │
                      └──► CEE3ABD (LE abend handler)

  JCL: INTCALC  ──► CBACT04C
                      │
                      └──► CEE3ABD

  JCL: CREASTMT ──► CBSTM03A ──► CBSTM03B (called 13× for file I/O)
                      │
                      └──► CEE3ABD

  JCL: TRANREPT ──► CBTRN03C
                      │
                      └──► CEE3ABD

  JCL: READACCT ──► CBACT01C ──► COBDATFT (ASM: date format)
                      │
                      └──► CEE3ABD

  JCL: READCARD ──► CBACT02C ──► CEE3ABD
  JCL: READXREF ──► CBACT03C ──► CEE3ABD
  JCL: READCUST ──► CBCUS01C ──► CEE3ABD
  JCL: CBEXPORT ──► CBEXPORT ──► CEE3ABD
  JCL: CBIMPORT ──► CBIMPORT ──► CEE3ABD
  JCL: WAITSTEP ──► COBSWAIT ──► MVSWAIT (ASM: MVS wait)

  Online:
  CORPT00C ──► CSUTLDTC ──► CEEDAYS (LE date conversion)
  COTRN02C ──► CSUTLDTC ──► CEEDAYS
```

### 2.1 Detailed CALL Table

| Caller Program | Called Program | Type          | Purpose                           |
|----------------|----------------|---------------|-----------------------------------|
| CBACT01C       | COBDATFT       | CALL (ASM)    | Format dates for report output    |
| CBACT01C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBACT02C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBACT03C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBACT04C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBCUS01C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBTRN01C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBTRN02C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBTRN03C       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBSTM03A       | CBSTM03B       | CALL (COBOL)  | File I/O sub-program (13 calls)   |
| CBSTM03A       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBEXPORT       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| CBIMPORT       | CEE3ABD        | CALL (LE)     | Abnormal termination handler      |
| COBSWAIT       | MVSWAIT        | CALL (ASM)    | MVS wait service                  |
| CORPT00C       | CSUTLDTC       | CALL (COBOL)  | Date validation utility           |
| COTRN02C       | CSUTLDTC       | CALL (COBOL)  | Date validation utility           |
| CSUTLDTC       | CEEDAYS        | CALL (LE)     | LE date conversion service        |

---

## 3. Copybook Dependency Matrix

Each cell shows which programs include (COPY) which copybooks.

### 3.1 Data Record Copybooks

| Copybook   | Programs That Include It                                                |
|------------|-------------------------------------------------------------------------|
| CVACT01Y   | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COTRN02C, COBIL00C, CBSTM03A |
| CVACT02Y   | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CVACT03Y   | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COTRN02C, COBIL00C, CBSTM03A |
| CVCUS01Y   | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDSLC, COCRDUPC, COACTUPC |
| CVTRA05Y   | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C |
| CVTRA06Y   | CBTRN01C, CBTRN02C                                                     |
| CVTRA01Y   | CBACT04C, CBTRN02C                                                     |
| CVTRA02Y   | CBACT04C                                                               |
| CVTRA03Y   | CBTRN03C                                                               |
| CVTRA04Y   | CBTRN03C                                                               |
| CVTRA07Y   | CBTRN03C                                                               |
| CVEXPORT   | CBEXPORT, CBIMPORT                                                     |
| CVCRD01Y   | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC                      |
| COSTM01    | CBSTM03A                                                               |
| CUSTREC    | CBSTM03A                                                               |

### 3.2 Application Control Copybooks

| Copybook   | Programs That Include It                                                |
|------------|-------------------------------------------------------------------------|
| COCOM01Y   | All 17 online CICS programs + COACTUPC (twice)                         |
| COTTL01Y   | All 17 online CICS programs                                            |
| CSDAT01Y   | All 17 online CICS programs                                            |
| CSMSG01Y   | All 17 online CICS programs                                            |
| CSUSR01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00-03C |
| CSMSG02Y   | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                                |
| COMEN02Y   | COMEN01C                                                               |
| COADM02Y   | COADM01C                                                               |
| CSLKPCDY   | COACTUPC                                                               |
| CSSETATY   | COACTUPC (39 inclusions via REPLACING)                                 |
| CSSTRPFY   | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                                |
| CSUTLDPY   | COACTUPC                                                               |
| CSUTLDWY   | COACTUPC                                                               |
| CODATECN   | CBACT01C                                                               |

---

## 4. VSAM File Access Map — Online Programs

Shows which CICS programs read/write which VSAM datasets.

| VSAM Dataset | Logical Name | COSGN00C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | COUSR00-03C |
|--------------|--------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-----------:|
| USRSEC       | User Security|   R      |          |          |          |          |          |          |          |          |          |   R/W/D     |
| ACCTDAT      | Account Data |          |   R      |   R/W    |          |          |          |          |          |          |   R/W    |             |
| CARDDAT      | Card Data    |          |          |          |   R(br)  |   R      |   R/W    |          |          |          |          |             |
| CARDAIX      | Card AIX     |          |          |          |   R(br)  |          |          |          |          |          |          |             |
| CXACAIX      | XRef AIX     |          |   R      |   R      |          |          |          |          |          |   R      |   R      |             |
| CARDXREF     | Card XRef    |          |          |          |          |          |          |          |          |   R      |          |             |
| CUSTDAT      | Customer     |          |   R      |   R/W    |          |          |          |          |          |          |          |             |
| TRANSACT     | Transactions |          |          |          |          |          |          |   R(br)  |   R      |   R/W    |   R(br)  |             |

**Legend:** R = Read, W = Write/Rewrite, D = Delete, (br) = Browse (STARTBR/READNEXT/READPREV/ENDBR)

---

## 5. VSAM File Access Map — Batch Programs

| VSAM / File            | CBACT01C | CBACT04C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|------------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| Account File (ACCTDAT) |   R      |   R/W    |   R      |   R/W    |          |          |   R      |   R      |   W      |
| Card File (CARDDAT)    |          |          |          |          |          |          |          |   R      |   W      |
| Card XRef (CARDXREF)   |          |   R      |   R      |   R      |   R      |   R      |   R      |   R      |   W      |
| Customer (CUSTDAT)     |          |          |   R      |          |          |          |   R      |   R      |   W      |
| Transaction (TRANSACT) |          |          |          |   W      |   R      |          |   R      |   R      |   W      |
| Daily Trans (DALYTRAN) |          |          |   R      |   R      |          |          |          |          |          |
| Tran Cat Bal (TCATBALF)|          |   R      |          |   R/W    |          |          |          |          |          |
| Disclosure (DISCGRP)   |          |   R      |          |          |          |          |          |          |          |
| Tran Type (TRANTYPE)   |          |          |          |          |   R      |          |          |          |          |
| Tran Cat (TRANCATG)    |          |          |          |          |   R      |          |          |          |          |
| Daily Rejects          |          |          |          |   W      |          |          |          |          |          |
| Report Output          |          |          |          |          |   W      |          |          |          |          |
| Statement File         |          |          |          |          |          |   W      |          |          |          |
| HTML Statement         |          |          |          |          |          |   W      |          |          |          |
| Export Flat File       |          |          |          |          |          |          |          |   W      |   R      |
| Output Files (3)       |   W      |          |          |          |          |          |          |          |          |
| Date Parms             |          |          |          |          |   R      |          |          |          |          |

---

## 6. JCL Job → Program → File Data Lineage

### 6.1 Nightly Batch Cycle (Sequence Order)

```
STEP 1: CLOSEFIL ──► Close CICS files (ACCTDAT, CARDDAT, CUSTDAT, TRANSACT, etc.)
           │
STEP 2: Data Refresh (if needed)
           ├── ACCTFILE  ──► IDCAMS ──► Delete/Define/Load ACCTDAT
           ├── CARDFILE  ──► IDCAMS ──► Delete/Define/Load CARDDAT
           ├── CUSTFILE  ──► IDCAMS ──► Delete/Define/Load CUSTDAT
           ├── XREFFILE  ──► IDCAMS ──► Delete/Define/Load CARDXREF + AIX
           ├── TRANFILE  ──► IDCAMS ──► Delete/Define/Load TRANSACT
           └── DUSRSECJ  ──► IDCAMS ──► Delete/Define/Load USRSEC
           │
STEP 3: POSTTRAN ──► CBTRN02C
           │         Reads:  DALYTRAN, CARDXREF, ACCTDAT, TCATBALF
           │         Writes: TRANSACT, ACCTDAT(rewrite), TCATBALF(rewrite/write)
           │         Writes: Daily rejection file
           │
STEP 4: INTCALC ──► CBACT04C
           │         Reads:  TCATBALF, ACCTDAT, CARDXREF, DISCGRP
           │         Writes: ACCTDAT(rewrite), TRANSACT(write)
           │
STEP 5: TRANBKP ──► IDCAMS REPRO
           │         Reads:  TRANSACT (VSAM)
           │         Writes: TRANSACT.BKUP (GDG sequential)
           │
STEP 6: COMBTRAN ──► SORT
           │         Reads:  DALYTRAN + existing TRANSACT backup
           │         Writes: Combined sequential file
           │
STEP 7: CREASTMT ──► CBSTM03A → CBSTM03B
           │         Reads:  TRANSACT (VSAM reloaded), CARDXREF, CUSTDAT, ACCTDAT
           │         Writes: Statement text file, HTML statement file
           │
STEP 8: TRANREPT ──► SORT + CBTRN03C
           │         Reads:  TRANSACT.BKUP, CARDXREF, TRANTYPE, TRANCATG
           │         Writes: Transaction detail report, Date parms
           │
STEP 9: TRANIDX ──► IDCAMS
           │         Defines: Alternate index on TRANSACT
           │
STEP 10: OPENFIL ──► Reopen CICS files
```

### 6.2 Data Flow Diagram

```
                    ┌──────────┐
  Online Users ────►│ DALYTRAN │  (Daily transactions entered online via COTRN02C)
                    └────┬─────┘
                         │
                    ┌────┴─────┐       ┌──────────┐
                    │ CBTRN02C │──────►│ TRANSACT │  (Posted transactions)
                    │ (Post)   │       └────┬─────┘
                    └────┬─────┘            │
                         │            ┌─────┴──────┐
                    ┌────┴─────┐      │  CBSTM03A  │──► Statement Files
                    │ Rejects  │      │ (Statements)│    (Text + HTML)
                    └──────────┘      └────────────┘
                                           │
                    ┌──────────┐      ┌─────┴──────┐
                    │ ACCTDAT  │◄────►│  CBACT04C  │  (Interest applied
                    │ (updated)│      │ (Interest) │   to account balances)
                    └──────────┘      └────────────┘
                                           │
                    ┌──────────┐      ┌─────┴──────┐
                    │ TCATBALF │◄────►│  CBTRN02C  │  (Category balances
                    │ (updated)│      │ + CBACT04C │   updated during posting
                    └──────────┘      └────────────┘   and interest calc)

                    ┌──────────┐      ┌────────────┐
                    │ DISCGRP  │─────►│  CBACT04C  │  (Interest rates
                    └──────────┘      └────────────┘   drive calculations)

                    ┌──────────┐      ┌────────────┐
                    │ CARDXREF │─────►│ (Multiple) │  (Cross-reference
                    └──────────┘      └────────────┘   used by 8+ programs)
```

---

## 7. Cross-Reference: VSAM Datasets ↔ JCL Jobs

| VSAM Dataset                           | Defined By | Loaded By  | Read By (Batch)              | Written By (Batch)         |
|----------------------------------------|------------|------------|------------------------------|----------------------------|
| CARDDEMO.ACCTDATA.VSAM.KSDS           | ACCTFILE   | ACCTFILE   | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A | CBACT04C, CBTRN02C |
| CARDDEMO.CARDDATA.VSAM.KSDS           | CARDFILE   | CARDFILE   | CBACT02C, CBEXPORT           | CBIMPORT                   |
| CARDDEMO.CUSTDATA.VSAM.KSDS           | CUSTFILE   | CUSTFILE   | CBCUS01C, CBTRN01C, CBSTM03A, CBEXPORT | CBIMPORT            |
| CARDDEMO.CARDXREF.VSAM.KSDS           | XREFFILE   | XREFFILE   | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT | CBIMPORT |
| CARDDEMO.TRANSACT.VSAM.KSDS           | TRANFILE   | TRANFILE   | CBTRN03C, CBSTM03A, CBEXPORT | CBTRN02C, CBACT04C, CBIMPORT |
| CARDDEMO.USRSEC.VSAM.KSDS             | DUSRSECJ   | DUSRSECJ   | —                            | —                          |
| CARDDEMO.DALYTRAN.PS                   | (external) | (external) | CBTRN01C, CBTRN02C           | —                          |
| CARDDEMO.TCATBAL.VSAM.KSDS            | TCATBALF   | TCATBALF   | CBACT04C, CBTRN02C           | CBTRN02C, CBACT04C         |
| CARDDEMO.DISCGRP.VSAM.KSDS            | DISCGRP    | DISCGRP    | CBACT04C                     | —                          |
| CARDDEMO.TRANTYPE.VSAM.KSDS           | TRANTYPE   | TRANTYPE   | CBTRN03C                     | —                          |
| CARDDEMO.TRANCATG.VSAM.KSDS           | TRANCATG   | TRANCATG   | CBTRN03C                     | —                          |
| CARDDEMO.TRANSACT.BKUP (GDG)          | DEFGDGB    | TRANBKP    | TRANREPT                     | TRANBKP                   |

---

## 8. Shared Utility Dependencies

| Utility Program | Used By                                | Purpose                |
|-----------------|----------------------------------------|------------------------|
| CSUTLDTC        | CORPT00C, COTRN02C                     | Date validation        |
| COBDATFT (ASM)  | CBACT01C                               | Date formatting        |
| MVSWAIT (ASM)   | COBSWAIT                               | MVS wait service       |
| CEE3ABD (LE)    | All batch programs                     | Abend handling         |
| CEEDAYS (LE)    | CSUTLDTC                               | Date conversion        |
| CSSETATY (CPY)  | COACTUPC (39×)                         | BMS attribute setting  |
| CSSTRPFY (CPY)  | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | PF-key storage         |

---

## 9. Menu Configuration Dependencies

### Regular User Menu (COMEN02Y)

| Option | Label              | Target Program | Domain            |
|--------|--------------------|----------------|-------------------|
| 01     | Account View       | COACTVWC       | Account Mgmt      |
| 02     | Account Update     | COACTUPC       | Account Mgmt      |
| 03     | Credit Card List   | COCRDLIC       | Card Mgmt         |
| 04     | Transaction List   | COTRN00C       | Transaction Mgmt  |
| 05     | Transaction Add    | COTRN02C       | Transaction Mgmt  |
| 06     | Bill Payment       | COBIL00C       | Billing           |
| 07     | Transaction Report | CORPT00C       | Reporting         |

### Admin Menu (COADM02Y)

| Option | Label              | Target Program | Domain            |
|--------|--------------------|----------------|-------------------|
| 01     | User List          | COUSR00C       | User Admin        |
| 02     | User Add           | COUSR01C       | User Admin        |
| 03     | Auth Summary*      | COPAUS0C       | Authorization     |
| 04     | Tran Type List*    | COTRTLIC       | Config Mgmt       |

*Optional modules — may not be installed

---

## 10. Modernization Dependency Clusters

Programs that are tightly coupled and should be migrated together:

| Cluster                   | Programs                                              | Shared Data              |
|---------------------------|-------------------------------------------------------|--------------------------|
| **Authentication**        | COSGN00C, CSUSR01Y                                    | USRSEC                   |
| **User Administration**   | COUSR00C, COUSR01C, COUSR02C, COUSR03C                | USRSEC                   |
| **Account Management**    | COACTVWC, COACTUPC                                    | ACCTDAT, CUSTDAT, CXACAIX|
| **Card Management**       | COCRDLIC, COCRDSLC, COCRDUPC                          | CARDDAT, CARDAIX         |
| **Transaction Online**    | COTRN00C, COTRN01C, COTRN02C                          | TRANSACT, CXACAIX        |
| **Transaction Posting**   | CBTRN01C, CBTRN02C                                    | DALYTRAN, TRANSACT, ACCTDAT, TCATBALF |
| **Financial Calcs**       | CBACT04C                                              | ACCTDAT, TCATBALF, DISCGRP |
| **Statement Generation**  | CBSTM03A, CBSTM03B                                   | TRANSACT, CARDXREF, CUSTDAT, ACCTDAT |
| **Reporting**             | CORPT00C, CBTRN03C                                    | TRANSACT, TRANTYPE, TRANCATG |
| **Data Migration**        | CBEXPORT, CBIMPORT                                    | All VSAM files           |
| **Navigation**            | COMEN01C, COADM01C                                    | COMMAREA only            |
