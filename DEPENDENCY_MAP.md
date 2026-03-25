# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Scope:** Call graph, copybook inclusion, VSAM data lineage, and JCL batch flow

---

## 1. Online CICS Program Call Graph

### 1.1 Navigation Flow (XCTL Transfers)

```
                        ┌─────────────┐
                        │  COSGN00C   │  (Signon - CC00)
                        │  Entry Point│
                        └──────┬──────┘
                               │
                 ┌─────────────┼─────────────┐
                 │ (Admin)     │             │ (User)
                 ▼             │             ▼
          ┌─────────────┐     │      ┌─────────────┐
          │  COADM01C   │     │      │  COMEN01C   │
          │  Admin Menu │     │      │  Main Menu  │
          └──────┬──────┘     │      └──────┬──────┘
                 │            │             │
    ┌────────────┤            │    ┌────────┼────────┬──────────┬──────────┬──────────┬──────────┐
    │            │            │    │        │        │          │          │          │          │
    ▼            ▼            │    ▼        ▼        ▼          ▼          ▼          ▼          ▼
 COUSR00C   COUSR01C         │ COACTVWC COACTUPC COCRDLIC  COTRN00C  COTRN02C  CORPT00C  COBIL00C
 User List  User Add         │ Acct View Acct Upd Card List Tran List Tran Add  Reports   Bill Pay
    │            │            │    │                 │
    ▼            ▼            │    │        ┌────────┤
 COUSR02C   COUSR03C         │    │        ▼        ▼
 User Upd   User Del         │    │    COCRDSLC  COCRDUPC
                              │    │    Card View Card Upd
                              │    │
                              │    ▼
                              │ COTRN01C
                              │ Tran View
                              │
                              │  (Menu also offers)
                              │    ▼
                              │ COPAUS0C (optional -- Pending Auth View)
                              │
```

### 1.2 Detailed XCTL / LINK / CALL Transfers

| Source Program | Target Program | Transfer Type | Condition |
|---------------|----------------|---------------|-----------|
| COSGN00C | COADM01C | XCTL | User type = Admin |
| COSGN00C | COMEN01C | XCTL | User type = Regular |
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
| COMEN01C | COPAUS0C | XCTL | Menu option 11 (optional module) |
| COMEN01C | COSGN00C | XCTL | PF3 (back to signon) |
| COADM01C | COUSR00C | XCTL | Admin option 1 |
| COADM01C | COUSR01C | XCTL | Admin option 2 |
| COADM01C | COUSR02C | XCTL | Admin option 3 |
| COADM01C | COUSR03C | XCTL | Admin option 4 |
| COADM01C | COTRTLIC | XCTL | Admin option 5 (DB2 optional) |
| COADM01C | COTRTUPC | XCTL | Admin option 6 (DB2 optional) |
| COACTVWC | COMEN01C | XCTL | PF3 (return to menu) |
| COACTUPC | COMEN01C | XCTL | PF3 (return to menu) |
| COCRDLIC | COMEN01C | XCTL | PF3 (return to menu) |
| COCRDLIC | COCRDSLC | XCTL | Select 'S' on a row (view detail) |
| COCRDLIC | COCRDUPC | XCTL | Select 'U' on a row (update) |
| COCRDSLC | COCRDLIC | XCTL | PF3 (back to list) |
| COCRDSLC | COMEN01C | XCTL | PF3 (return to menu, if no caller) |
| COCRDUPC | COCRDLIC | XCTL | PF3 (back to list) |
| COCRDUPC | COMEN01C | XCTL | PF3 (return to menu, if no caller) |
| COTRN00C | COMEN01C | XCTL | PF3 |
| COTRN01C | COMEN01C | XCTL | PF3 |
| COTRN02C | COMEN01C | XCTL | PF3 |
| CORPT00C | COMEN01C | XCTL | PF3 |
| COBIL00C | COMEN01C | XCTL | PF3 |
| COUSRxxC | COADM01C | XCTL | PF3 (back to admin menu) |

### 1.3 Subroutine CALL Relationships (Online)

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| COTRN02C | CSUTLDTC | CALL | Date validation (CEEDAYS) |
| CORPT00C | CSUTLDTC | CALL | Date validation for report parameters |

---

## 2. Batch Program Call Graph

```
CBTRN01C ──(reads)──▶ Daily Transactions
   │                   Validates against Customer, Card, Xref, Account, Transaction master
   │
CBTRN02C ──(reads)──▶ Daily Transactions
   │      ──(updates)─▶ Transaction Master, Account Master, Category Balance
   │      ──(writes)──▶ Rejection File
   │
CBACT04C ──(reads)──▶ Category Balance, Xref, Account, Disclosure Groups
   │      ──(writes)──▶ System Transactions (interest entries)
   │      ──(updates)─▶ Account Master (balance adjustments)
   │
CBSTM03A ──(CALL)───▶ CBSTM03B (file I/O subroutine, called 12 times)
   │      ──(reads)──▶ Sorted Transactions, Xref, Customer, Account
   │      ──(writes)──▶ Statement Files (text + HTML)
   │
CBTRN03C ──(reads)──▶ Transaction backup, Xref, Transaction Types, Categories
   │      ──(writes)──▶ Daily Transaction Report
   │
CBACT01C ──(CALL)───▶ COBDATFT (assembler date formatter)
   │      ──(reads)──▶ Account Master
   │      ──(writes)──▶ PS, array, VB output formats
   │
CBEXPORT ──(reads)──▶ Customer, Account, Xref, Transaction, Card files
   │      ──(writes)──▶ Single export sequential file
   │
CBIMPORT ──(reads)──▶ Export sequential file
   │      ──(writes)──▶ Individual entity files
   │
COBSWAIT ──(CALL)───▶ MVSWAIT (assembler wait routine)
```

### Batch CALL Dependencies

| Caller | Callee | Type | Purpose |
|--------|--------|------|---------|
| CBSTM03A | CBSTM03B | CALL (12x) | Open/close/read TRNX, XREF, CUST, ACCT files |
| CBACT01C | COBDATFT | CALL (ASM) | Date formatting for account reports |
| COBSWAIT | MVSWAIT | CALL (ASM) | System wait/sleep in centiseconds |
| All batch | CEE3ABD | CALL (LE) | Language Environment abnormal termination |
| CSUTLDTC | CEEDAYS | CALL (LE) | Convert date to Lilian format for validation |

---

## 3. VSAM File Access Map (Online CICS Programs)

### 3.1 File Access by Program

| Program | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CXACAIX | CUSTDAT | TRANSACT |
|---------|:------:|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|
| COSGN00C | R | | | | | | |
| COMEN01C | | | | | | | |
| COADM01C | | | | | | | |
| COACTVWC | | R | | | R | R | |
| COACTUPC | | RW | | | R | RW | |
| COCRDLIC | | | R (browse) | R | | | |
| COCRDSLC | | | R | R | | R | |
| COCRDUPC | | | RW | R | | R | |
| COTRN00C | | | | | | | R (browse) |
| COTRN01C | | | | | | | R |
| COTRN02C | | | | | R | | RW (browse+write) |
| CORPT00C | | | | | | | (TD queue write) |
| COBIL00C | | RW | | | R | | RW (browse+write) |
| COUSR00C | R (browse) | | | | | | |
| COUSR01C | W | | | | | | |
| COUSR02C | RW | | | | | | |
| COUSR03C | RD | | | | | | |

**Legend:** R=Read, W=Write, RW=Read+Update(Rewrite), RD=Read+Delete, browse=STARTBR/READNEXT/READPREV

### 3.2 VSAM Dataset Summary

| Logical Name | VSAM Dataset | Key | Record Copybook | Access Pattern |
|-------------|-------------|-----|----------------|----------------|
| USRSEC | USRSEC.VSAM.KSDS | SEC-USR-ID (8) | CSUSR01Y | Direct read by key, browse for list |
| ACCTDAT | ACCTDATA.VSAM.KSDS | ACCT-ID (11) | CVACT01Y | Direct read, update |
| CARDDAT | CARDDATA.VSAM.KSDS | CARD-NUM (16) | CVACT02Y | Direct read, browse, update |
| CARDAIX | CARDDATA.VSAM.AIX.PATH | CARD-ACCT-ID (11) | CVACT02Y | Alternate index read by account |
| CXACAIX | CARDXREF.VSAM.AIX.PATH | XREF-ACCT-ID (11) | CVACT03Y | Alternate index read by account |
| CUSTDAT | CUSTDATA.VSAM.KSDS | CUST-ID (9) | CVCUS01Y | Direct read, update |
| TRANSACT | TRANSACT.VSAM.KSDS | TRAN-ID (16) | CVTRA05Y | Direct read, browse, write |

---

## 4. Batch File Data Lineage

### 4.1 File Access by Batch Program

| Program | DALYTRAN | TRANSACT | ACCTDAT | CARDXREF | CARDDAT | CUSTDAT | TCATBALF | DISCGRP | DALYREJS | Reports |
|---------|:--------:|:--------:|:-------:|:--------:|:-------:|:-------:|:--------:|:-------:|:--------:|:-------:|
| CBTRN01C | I | I | I | I | I | I | | | | |
| CBTRN02C | I | O | I-O | I | | | I-O | | O | |
| CBACT04C | | I | I-O | I | | | I | I | | O (SYSTRAN) |
| CBTRN03C | | I | | I | | | | | | O (report) |
| CBSTM03A/B | | I (sorted) | I | I | | I | | | | O (stmt+HTML) |
| CBACT01C | | | I | | | | | | | O (PS,array,VB) |
| CBACT02C | | | | | I | | | | | print |
| CBACT03C | | | | I | | | | | | print |
| CBCUS01C | | | | | | I | | | | print |
| CBEXPORT | | I | I | I | I | I | | | | O (export) |
| CBIMPORT | | | | | | | | | | I (export) → O (entity files) |

**Legend:** I=Input, O=Output, I-O=Input-Output (update in place)

### 4.2 JCL Job → Program → Dataset Flow

```
Batch Processing Cycle (nightly):
═══════════════════════════════════════════════════════════════

Step 1: CLOSEFIL ──▶ DFHCSDUP    Close CICS-managed VSAM files
                                  (ACCTDAT, CARDDAT, CUSTDAT, TRANSACT, etc.)

Step 2: Data Refresh (parallel)
         ACCTFILE ──▶ IDCAMS      ACCTDATA.PS ──▶ ACCTDATA.VSAM.KSDS
         CARDFILE ──▶ IDCAMS      CARDDATA.PS ──▶ CARDDATA.VSAM.KSDS + AIX
         CUSTFILE ──▶ IDCAMS      CUSTDATA.PS ──▶ CUSTDATA.VSAM.KSDS
         XREFFILE ──▶ IDCAMS      CARDXREF.PS ──▶ CARDXREF.VSAM.KSDS + AIX
         TRANFILE ──▶ IDCAMS      DALYTRAN.PS ──▶ TRANSACT.VSAM.KSDS

Step 3: POSTTRAN ──▶ CBTRN02C    DALYTRAN.PS + CARDXREF + ACCTDATA
                                  ──▶ TRANSACT.VSAM.KSDS (posted)
                                  ──▶ ACCTDATA.VSAM.KSDS (balances updated)
                                  ──▶ TCATBALF.VSAM.KSDS (category balances)
                                  ──▶ DALYREJS GDG (rejected transactions)

Step 4: INTCALC ──▶ CBACT04C     TCATBALF + CARDXREF + ACCTDATA + DISCGRP
                                  ──▶ SYSTRAN GDG (interest transactions)
                                  ──▶ ACCTDATA.VSAM.KSDS (balance + interest)

Step 5: TRANBKP ──▶ IDCAMS       TRANSACT.VSAM.KSDS ──▶ TRANSACT.BKUP GDG

Step 6: COMBTRAN ──▶ SORT+IDCAMS  TRANSACT.BKUP + SYSTRAN
                                  ──▶ TRANSACT.COMBINED GDG
                                  ──▶ TRANSACT.VSAM.KSDS (reloaded)

Step 7: CREASTMT ──▶ SORT+CBSTM03A  TRANSACT.VSAM.KSDS (sorted by card)
                                     + CARDXREF + CUSTDATA + ACCTDATA
                                     ──▶ STATEMNT.PS (text statements)
                                     ──▶ STATEMNT.HTML (HTML statements)

Step 8: TRANREPT ──▶ CBTRN03C    TRANSACT.BKUP + CARDXREF + TRANTYPE + TRANCATG
                                  ──▶ TRANSACT.DALY GDG (daily archive)
                                  ──▶ Daily Transaction Report

Step 9: TRANIDX ──▶ IDCAMS       Rebuild alternate indexes on TRANSACT

Step 10: OPENFIL ──▶ DFHCSDUP    Reopen CICS-managed VSAM files
```

---

## 5. Copybook Inclusion Map

### 5.1 Which Programs Include Which Copybooks

| Copybook | Programs That Include It |
|----------|------------------------|
| COCOM01Y | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| COTTL01Y | (same 17 online programs as COCOM01Y) |
| CSDAT01Y | (same 17 online programs as COCOM01Y) |
| CSMSG01Y | (same 17 online programs as COCOM01Y) |
| CSUSR01Y | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| CVACT01Y | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| CVACT02Y | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| CVACT03Y | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A/B |
| CVCUS01Y | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT |
| CVTRA05Y | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| CVTRA06Y | CBTRN01C, CBTRN02C |
| CVCRD01Y | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSMSG02Y | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, (COBIL00C) |
| CSSETATY | COACTUPC (39 occurrences -- field attribute control) |
| CVEXPORT | CBEXPORT, CBIMPORT |

### 5.2 Copybook Coupling Analysis

| Coupling Level | Copybooks | Impact of Change |
|---------------|-----------|------------------|
| **Very High** (17 programs) | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y | Any change affects ALL online programs |
| **High** (11-14 programs) | CVACT03Y, CVACT01Y, CSUSR01Y, CVTRA05Y | Core data structures -- changes ripple widely |
| **Medium** (5-8 programs) | CVACT02Y, CVCUS01Y, CVCRD01Y, CSMSG02Y | Business entity changes affect related programs |
| **Low** (1-2 programs) | CVTRA06Y, CVTRA01Y-04Y, CVTRA07Y, CVEXPORT, COSTM01, CUSTREC | Localized impact |

---

## 6. Cross-Cutting Dependency Clusters

### Cluster 1: Account Management
```
COACTVWC ←→ COACTUPC ←→ (CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y)
                          ↕
                      ACCTDAT, CARDDAT, CUSTDAT, CXACAIX VSAM files
```

### Cluster 2: Card Management
```
COCRDLIC → COCRDSLC → COCRDUPC → (CVACT02Y, CVCRD01Y, CVCUS01Y)
                                   ↕
                               CARDDAT, CARDAIX VSAM files
```

### Cluster 3: Transaction Processing
```
Online: COTRN00C → COTRN01C → COTRN02C → (CVTRA05Y, CVACT01Y, CVACT03Y)
                                           ↕
                                       TRANSACT, CXACAIX VSAM files
Batch:  CBTRN01C → CBTRN02C → (CVTRA05Y, CVTRA06Y, CVTRA01Y)
                                ↕
                            DALYTRAN, TRANSACT, ACCTDAT, TCATBALF
```

### Cluster 4: Reporting & Statements
```
CORPT00C → (batch) CBTRN03C → (CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y)
CREASTMT → CBSTM03A → CBSTM03B → (COSTM01, CUSTREC, CVACT01Y, CVACT03Y)
```

### Cluster 5: User Security
```
COSGN00C → COUSR00C → COUSR01C → COUSR02C → COUSR03C → (CSUSR01Y)
                                                          ↕
                                                      USRSEC VSAM file
```
