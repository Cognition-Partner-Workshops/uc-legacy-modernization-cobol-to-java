# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Scope:** Call graph, XCTL transfers, copybook dependencies, JCL data lineage

---

## 1. Program Call Graph

### 1.1 Online CICS Navigation Flow (XCTL Transfers)

```
                         ┌─────────────┐
                         │  COSGN00C   │  (Sign-On, CC00)
                         │  Validates  │
                         │  USRSEC     │
                         └──────┬──────┘
                                │
                   ┌────────────┴────────────┐
                   │ (Admin user)            │ (Regular user)
                   ▼                         ▼
            ┌─────────────┐          ┌─────────────┐
            │  COADM01C   │          │  COMEN01C   │
            │  Admin Menu │          │  Main Menu  │
            │  (CA00)     │          │  (CM00)     │
            └──────┬──────┘          └──────┬──────┘
                   │                        │
         ┌─────┬──┘                ┌────┬───┼────┬────┬────┐
         │     │                   │    │   │    │    │    │
         ▼     ▼                   ▼    ▼   ▼    ▼    ▼    ▼
      COUSR00C  COUSR01C     COACTVWC COACTUPC COCRDLIC COTRN00C CORPT00C COBIL00C
      User List User Add     Acct View Acct Upd Card List Trn List Reports  Bill Pay
      (CU00)   (CU01)       (CAVW)   (CAUP)   (CCLI)   (CT00)   (CR00)   (CB00)
         │        │              │               │         │
         ├────────┤              │        ┌──────┤         ├──────┐
         ▼        ▼              ▼        ▼      ▼         ▼      ▼
      COUSR02C COUSR03C     COCRDLIC  COCRDSLC COCRDUPC COTRN01C COTRN02C
      User Upd User Del     Card List Card View Card Upd  Trn View Trn Add
      (CU02)   (CU03)       (CCLI)   (CCDL)   (CCUP)   (CT01)   (CT02)
```

**Navigation Notes:**
- All online programs use `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` for dynamic transfer
- The target program name is stored in `COCOM01Y.CDEMO-TO-PROGRAM`
- COMEN01C reads menu options from `COMEN02Y` copybook; COADM01C reads from `COADM02Y`
- Every program can XCTL back to `COSGN00C` on session timeout or F3 from menu

### 1.2 Subroutine CALL Graph

```
CBSTM03A ──CALL──► CBSTM03B     (Statement generation calls file I/O subroutine)
                                  CBSTM03A calls CBSTM03B 13 times for different
                                  file operations (open, read, write, close)

CBACT01C ──CALL──► COBDATFT     (Account reader calls assembler date formatter)
CBACT01C ──CALL──► CEE3ABD      (LE abnormal termination)

CORPT00C ──CALL──► CSUTLDTC     (Report submission calls date utility)
COTRN02C ──CALL──► CSUTLDTC     (Transaction add calls date utility)
CSUTLDTC ──CALL──► CEEDAYS      (Date utility calls LE date conversion)

COBSWAIT ──CALL──► MVSWAIT      (Wait utility calls assembler wait routine)

CBACT02C ──CALL──► CEE3ABD      (Error handler)
CBACT03C ──CALL──► CEE3ABD      (Error handler)
CBACT04C ──CALL──► CEE3ABD      (Error handler)
CBCUS01C ──CALL──► CEE3ABD      (Error handler)
CBTRN01C ──CALL──► CEE3ABD      (Error handler)
CBTRN02C ──CALL──► CEE3ABD      (Error handler)
CBTRN03C ──CALL──► CEE3ABD      (Error handler)
CBEXPORT ──CALL──► CEE3ABD      (Error handler)
CBIMPORT ──CALL──► CEE3ABD      (Error handler)
```

### 1.3 CICS TDQ (Transient Data Queue) Interactions

```
CORPT00C ──WRITEQ TD──► JOBS TDQ    (Submits batch report JCL via extra-partition TDQ)
```

---

## 2. Copybook Dependency Matrix

Which programs include which copybooks (data structures):

| Copybook | Type | Used By Programs |
|---|---|---|
| **COCOM01Y** | COMMAREA | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** | Title | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** | Date | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** | Messages | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **DFHAID** | CICS AID keys | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **DFHBMSCA** | BMS attributes | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** | User record | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CVACT01Y** | Account | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVACT02Y** | Card | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| **CVACT03Y** | Cross-Ref | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVCUS01Y** | Customer | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, CBSTM03A (via CUSTREC) |
| **CVCRD01Y** | Card status | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CVTRA05Y** | Transaction | COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| **CVTRA06Y** | Daily Trans | CBTRN01C, CBTRN02C |
| **CVTRA01Y** | Cat Balance | CBACT04C, CBTRN02C |
| **CVTRA02Y** | Disc Group | CBACT04C |
| **CVTRA03Y** | Trans Type | CBTRN03C |
| **CVTRA04Y** | Trans Cat | CBTRN03C |
| **CVTRA07Y** | Report Layout | CBTRN03C |
| **CSMSG02Y** | Ext Messages | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| **CSLKPCDY** | Lookup | COACTUPC |
| **CSSETATY** | Set Attr | COACTUPC |
| **CSSTRPFY** | String | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSUTLDWY** | Date WS | COACTUPC |
| **COMEN02Y** | Menu Opts | COMEN01C |
| **COADM02Y** | Admin Opts | COADM01C |
| **CODATECN** | Date Conv | CBACT01C |
| **CVEXPORT** | Export | CBEXPORT, CBIMPORT |
| **COSTM01** | Stmt Trans | CBSTM03A |
| **CUSTREC** | Customer | CBSTM03A |

### BMS Map to Program Mapping

| BMS Map | Copybook | Program |
|---|---|---|
| COSGN00.bms | COSGN00.CPY | COSGN00C |
| COMEN01.bms | COMEN01.CPY | COMEN01C |
| COADM01.bms | COADM01.CPY | COADM01C |
| COACTVW.bms | COACTVW.CPY | COACTVWC |
| COACTUP.bms | COACTUP.CPY | COACTUPC |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC |
| COTRN00.bms | COTRN00.CPY | COTRN00C |
| COTRN01.bms | COTRN01.CPY | COTRN01C |
| COTRN02.bms | COTRN02.CPY | COTRN02C |
| CORPT00.bms | CORPT00.CPY | CORPT00C |
| COBIL00.bms | COBIL00.CPY | COBIL00C |
| COUSR00.bms | COUSR00.CPY | COUSR00C |
| COUSR01.bms | COUSR01.CPY | COUSR01C |
| COUSR02.bms | COUSR02.CPY | COUSR02C |
| COUSR03.bms | COUSR03.CPY | COUSR03C |

---

## 3. VSAM File Access Matrix

Which programs read/write which VSAM files (online programs via CICS FILE commands, batch via SELECT/ASSIGN):

| VSAM Dataset | Logical Name | Programs That READ | Programs That WRITE/REWRITE/DELETE |
|---|---|---|---|
| USRSEC.VSAM.KSDS | USRSEC | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| ACCTDATA.VSAM.KSDS | ACCTFILE | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A | COACTUPC (REWRITE), COBIL00C (REWRITE), CBTRN02C (REWRITE) |
| CARDDATA.VSAM.KSDS | CARDFILE | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C | COCRDUPC (REWRITE) |
| CUSTDATA.VSAM.KSDS | CUSTFILE | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBSTM03A | -- |
| CARDXREF.VSAM.KSDS | CARDXREF | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A | -- |
| TRANSACT.VSAM.KSDS | TRANSACT | COTRN00C, COTRN01C, COBIL00C, CORPT00C, CBTRN02C, CBTRN03C | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE) |
| DALYTRAN.PS | DALYTRAN | CBTRN01C, CBTRN02C | -- (input only, generated externally) |
| TCATBALF.VSAM.KSDS | TCATBALF | CBACT04C, CBTRN02C | CBACT04C (WRITE), CBTRN02C (REWRITE) |
| DISCGRP.VSAM.KSDS | DISCGRP | CBACT04C | -- |
| TRANTYPE.VSAM.KSDS | TRANTYPE | CBTRN03C | -- |
| TRANCATG.VSAM.KSDS | TRANCATG | CBTRN03C | -- |
| TRXFL.VSAM.KSDS | TRNXFILE | CBSTM03A, CBSTM03B | -- (created by CREASTMT SORT step) |

---

## 4. JCL Job Data Lineage

### 4.1 Batch Cycle Data Flow

```
                    ┌─────────────────────────────────┐
                    │         DAILY INPUT              │
                    │  DALYTRAN.PS (daily transactions) │
                    └───────────────┬─────────────────┘
                                    │
                                    ▼
┌────────────┐           ┌─────────────────┐           ┌─────────────────┐
│ CLOSEFIL   │──────────►│   POSTTRAN      │──────────►│   INTCALC       │
│ Close CICS │           │   PGM=CBTRN02C  │           │   PGM=CBACT04C  │
│ files      │           │                 │           │                 │
└────────────┘           │ Reads:          │           │ Reads:          │
                         │  DALYTRAN.PS    │           │  TCATBALF.VSAM  │
                         │  CARDXREF.VSAM  │           │  CARDXREF.VSAM  │
                         │  ACCTDATA.VSAM  │           │  ACCTDATA.VSAM  │
                         │ Writes:         │           │  DISCGRP.VSAM   │
                         │  TRANSACT.VSAM  │           │ Writes:         │
                         │  ACCTDATA.VSAM  │           │  TCATBALF.VSAM  │
                         │  TCATBALF.VSAM  │           │  SYSTRAN GDG    │
                         │  DALYREJS GDG   │           └────────┬────────┘
                         └────────┬────────┘                    │
                                  │                             │
                                  ▼                             ▼
                         ┌─────────────────┐           ┌─────────────────┐
                         │   TRANBKP       │           │   COMBTRAN      │
                         │   IDCAMS REPRO  │           │   SORT/MERGE    │
                         │                 │           │                 │
                         │ Reads:          │           │ Reads:          │
                         │  TRANSACT.VSAM  │           │  TRANSACT.VSAM  │
                         │ Writes:         │           │ Writes:         │
                         │  TRANSACT.BKUP  │           │  Combined file  │
                         │  (GDG +1)       │           │                 │
                         └─────────────────┘           └────────┬────────┘
                                                                │
                                                                ▼
                         ┌─────────────────┐           ┌─────────────────┐
                         │   TRANREPT      │           │   CREASTMT      │
                         │ PGM=SORT+       │           │ PGM=CBSTM03A   │
                         │     CBTRN03C    │           │                 │
                         │                 │           │ Reads:          │
                         │ Reads:          │           │  TRXFL.VSAM    │
                         │  TRANSACT.BKUP  │           │  CARDXREF.VSAM │
                         │  CARDXREF.VSAM  │           │  ACCTDATA.VSAM │
                         │  TRANTYPE.VSAM  │           │  CUSTDATA.VSAM │
                         │  TRANCATG.VSAM  │           │ Writes:        │
                         │ Writes:         │           │  STATEMNT.PS   │
                         │  TRANREPT GDG   │           │  STATEMNT.HTML │
                         └─────────────────┘           └────────┬────────┘
                                                                │
                                                                ▼
                         ┌─────────────────┐           ┌─────────────────┐
                         │   TRANIDX       │           │   OPENFIL       │
                         │   IDCAMS        │           │   Reopen CICS   │
                         │   Rebuild AIX   │           │   files         │
                         └─────────────────┘           └─────────────────┘
```

### 4.2 Data Refresh Jobs (Initial Load / Refresh)

```
Flat File Source                    IDCAMS REPRO                   VSAM Target
──────────────────                  ───────────                    ───────────
ACCTDATA.PS         ──► ACCTFILE.jcl  ──►  ACCTDATA.VSAM.KSDS
CARDDATA.PS         ──► CARDFILE.jcl  ──►  CARDDATA.VSAM.KSDS
CUSTDATA.PS         ──► CUSTFILE.jcl  ──►  CUSTDATA.VSAM.KSDS
CARDXREF.PS         ──► XREFFILE.jcl  ──►  CARDXREF.VSAM.KSDS + AIX + PATH
USRSEC.PS           ──► DUSRSECJ.jcl  ──►  USRSEC.VSAM.KSDS
DALYTRAN.PS.INIT    ──► TRANFILE.jcl  ──►  TRANSACT.VSAM.KSDS + AIX + PATH
TCATBALF.PS         ──► TCATBALF.jcl  ──►  TCATBALF.VSAM.KSDS
TRANTYPE.PS         ──► TRANTYPE.jcl  ──►  TRANTYPE.VSAM.KSDS
TRANCATG.PS         ──► TRANCATG.jcl  ──►  TRANCATG.VSAM.KSDS
DISCGRP.PS          ──► DISCGRP.jcl   ──►  DISCGRP.VSAM.KSDS
```

### 4.3 Data Utility / Export Jobs

```
READACCT.jcl: ACCTDATA.VSAM ──► PGM=CBACT01C ──► ACCTDATA.PSCOMP + ARRYPS + VBPS
READCARD.jcl: CARDDATA.VSAM ──► PGM=CBACT02C ──► SYSOUT (print)
READCUST.jcl: CUSTDATA.VSAM ──► PGM=CBCUS01C ──► SYSOUT (print)
READXREF.jcl: CARDXREF.VSAM ──► PGM=CBACT03C ──► SYSOUT (print)

CBEXPORT.jcl: CUSTDATA + ACCTDATA + CARDXREF + TRANSACT + CARDDATA
              ──► PGM=CBEXPORT ──► Export flat file

CBIMPORT.jcl: Export flat file
              ──► PGM=CBIMPORT ──► CUSTDATA + ACCTDATA + CARDXREF + TRANSACT + CARDDATA

TXT2PDF1.JCL: STATEMNT.PS ──► PGM=TXT2PDF ──► STATEMNT.PS.PDF
```

### 4.4 Job Chaining

```
INTRDRJ1.JCL ──► (IDCAMS REPRO) ──► (IEBGENER to INTRDR) ──► triggers INTRDRJ2.JCL
INTRDRJ2.JCL ──► (IDCAMS REPRO of backup)
```

---

## 5. Cross-Cutting Dependency Summary

### 5.1 Most Depended-On Assets (Coupling Hotspots)

| Asset | Depended On By (count) | Type |
|---|---|---|
| COCOM01Y.cpy | 17 programs | Copybook -- COMMAREA |
| COTTL01Y.cpy | 17 programs | Copybook -- Titles |
| CSDAT01Y.cpy | 17 programs | Copybook -- Date fields |
| CSMSG01Y.cpy | 17 programs | Copybook -- Messages |
| CVACT01Y.cpy (Account) | 11 programs | Copybook -- Account record |
| CVACT03Y.cpy (Cross-Ref) | 12 programs | Copybook -- Card XREF |
| CVTRA05Y.cpy (Transaction) | 11 programs | Copybook -- Transaction record |
| ACCTDATA.VSAM.KSDS | 9+ programs, 3+ JCL jobs | VSAM file |
| CARDXREF.VSAM.KSDS | 10+ programs, 3+ JCL jobs | VSAM file |
| TRANSACT.VSAM.KSDS | 7+ programs, 5+ JCL jobs | VSAM file |

### 5.2 Programs With No Inbound Dependencies (Leaf Nodes)

These programs are not called by any other program (they are entry points):

- All online CICS programs (entered via transaction IDs from CICS)
- All batch programs (entered via JCL EXEC PGM)

### 5.3 Programs Called as Subroutines

| Subroutine | Called By | Mechanism |
|---|---|---|
| CBSTM03B | CBSTM03A | COBOL CALL |
| CSUTLDTC | CORPT00C, COTRN02C | COBOL CALL |
| COBDATFT (ASM) | CBACT01C | COBOL CALL |
| MVSWAIT (ASM) | COBSWAIT | COBOL CALL |
| CEEDAYS (LE) | CSUTLDTC | COBOL CALL |
| CEE3ABD (LE) | All batch programs | COBOL CALL (error handler) |

---

## 6. Modernization Dependency Notes

### 6.1 Entities to Convert to Database Tables

| VSAM File | Suggested Table | Primary Key | Foreign Keys |
|---|---|---|---|
| ACCTDATA.VSAM.KSDS | `accounts` | `account_id` | `group_id` → `disclosure_groups` |
| CARDDATA.VSAM.KSDS | `cards` | `card_number` | `account_id` → `accounts` |
| CARDXREF.VSAM.KSDS | `card_xref` | `card_number` | `account_id` → `accounts`, `customer_id` → `customers` |
| CUSTDATA.VSAM.KSDS | `customers` | `customer_id` | -- |
| TRANSACT.VSAM.KSDS | `transactions` | `transaction_id` | `card_number` → `cards`, `type_cd` → `transaction_types` |
| TCATBALF.VSAM.KSDS | `trans_cat_balances` | `(account_id, type_cd, cat_cd)` | `account_id` → `accounts` |
| DISCGRP.VSAM.KSDS | `disclosure_groups` | `(group_id, type_cd, cat_cd)` | -- |
| TRANTYPE.VSAM.KSDS | `transaction_types` | `type_code` | -- |
| TRANCATG.VSAM.KSDS | `transaction_categories` | `(type_cd, cat_cd)` | `type_cd` → `transaction_types` |
| USRSEC.VSAM.KSDS | `users` | `user_id` | -- |

### 6.2 Batch Job to Spring Batch Mapping

| JCL Job | Suggested Spring Batch Job | Priority |
|---|---|---|
| POSTTRAN (CBTRN02C) | `TransactionPostingJob` | **Critical** |
| INTCALC (CBACT04C) | `InterestCalculationJob` | **Critical** |
| CREASTMT (CBSTM03A/B) | `StatementGenerationJob` | **High** |
| TRANREPT (CBTRN03C) | `TransactionReportJob` | **Medium** |
| CBEXPORT | `DataExportJob` | **Medium** |
| CBIMPORT | `DataImportJob` | **Medium** |
| TRANBKP | `TransactionBackupJob` | **Low** (DB handles this) |
