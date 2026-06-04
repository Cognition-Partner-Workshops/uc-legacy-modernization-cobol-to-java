# Dependency Map - CardDemo

> Call graph, CICS transfer graph, and batch data lineage for the CardDemo system.

---

## 1. Online (CICS) Program Call Graph

### Transaction-to-Program Mapping

```
Terminal Input
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  CC00 → COSGN00C (Sign-on)                         │
│           ├── XCTL → COADM01C (if Admin)           │
│           └── XCTL → COMEN01C (if User)            │
└─────────────────────────────────────────────────────┘
```

### Main Menu Router (COMEN01C)

```
COMEN01C (CM00 - Main Menu)
    │
    ├── Option 1  → XCTL → COACTVWC  (Account View)
    ├── Option 2  → XCTL → COACTUPC  (Account Update)
    ├── Option 3  → XCTL → COCRDLIC  (Card List)
    ├── Option 4  → XCTL → COCRDSLC  (Card Detail)
    ├── Option 5  → XCTL → COCRDUPC  (Card Update)
    ├── Option 6  → XCTL → COTRN00C  (Transaction List)
    ├── Option 7  → XCTL → COTRN01C  (Transaction View)
    ├── Option 8  → XCTL → COTRN02C  (Transaction Add)
    ├── Option 9  → XCTL → CORPT00C  (Reports)
    ├── Option 10 → XCTL → COBIL00C  (Bill Payment)
    └── Option 11 → XCTL → COPAUS0C  (Pending Auth Summary) [Optional]
```

### Admin Menu Router (COADM01C)

```
COADM01C (CA00 - Admin Menu)
    │
    ├── Option 1 → XCTL → COUSR00C  (User List)
    ├── Option 2 → XCTL → COUSR01C  (User Add)
    ├── Option 3 → XCTL → COUSR02C  (User Update)
    ├── Option 4 → XCTL → COUSR03C  (User Delete)
    ├── Option 5 → XCTL → COTRTLIC  (Tran Type List) [DB2]
    └── Option 6 → XCTL → COTRTUPC  (Tran Type Update) [DB2]
```

### Inter-Program XCTL Transfers

```
COACTVWC ──── XCTL ────→ CDEMO-TO-PROGRAM (via COMMAREA, typically back to COMEN01C)
COACTUPC ──── XCTL ────→ CDEMO-TO-PROGRAM (via COMMAREA)
COCRDLIC ──── XCTL ────→ COMEN01C (menu return)
             ├── XCTL → COCRDSLC (view selected card)
             └── XCTL → COCRDUPC (update selected card)
COCRDSLC ──── XCTL ────→ CDEMO-TO-PROGRAM (via COMMAREA)
COCRDUPC ──── XCTL ────→ CDEMO-TO-PROGRAM (via COMMAREA)
COSGN00C ──── XCTL ────→ COADM01C | COMEN01C (based on user type)
```

### Subroutine CALL Graph (Online)

```
CORPT00C ──── CALL ────→ CSUTLDTC (Date utility)
COTRN02C ──── CALL ────→ CSUTLDTC (Date utility)
```

### Subroutine CALL Graph (Batch)

```
CBACT01C ──── CALL ────→ COBDATFT (ASM: Date formatting)
             └── CALL → CEE3ABD  (LE: Abend)
CBACT02C ──── CALL ────→ CEE3ABD
CBACT03C ──── CALL ────→ CEE3ABD
CBACT04C ──── CALL ────→ CEE3ABD
CBCUS01C ──── CALL ────→ CEE3ABD
CBTRN01C ──── CALL ────→ CEE3ABD
CBTRN02C ──── CALL ────→ CEE3ABD
CBTRN03C ──── CALL ────→ CEE3ABD
CBSTM03A ──── CALL ────→ CBSTM03B (Statement subroutine, 13 calls)
             └── CALL → CEE3ABD
CBEXPORT ──── CALL ────→ CEE3ABD
CBIMPORT ──── CALL ────→ CEE3ABD
COBSWAIT ──── CALL ────→ MVSWAIT  (ASM: Timer)
CSUTLDTC ──── CALL ────→ CEEDAYS  (LE: Date conversion)
```

---

## 2. Copybook Inclusion Map

### Core Data Copybooks → Programs

| Copybook | Used By (Programs) |
|----------|-------------------|
| CVACT01Y (Account) | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT |
| CVACT02Y (Card) | CBACT02C, CBTRN01C, COACTVWC, CBEXPORT, CBIMPORT |
| CVACT03Y (XREF) | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT |
| CVCUS01Y (Customer) | CBCUS01C, CBTRN01C, COACTUPC, COACTVWC, CBEXPORT, CBIMPORT |
| CVTRA05Y (Transaction) | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, CBEXPORT, CBIMPORT |
| CVTRA06Y (Daily Tran) | CBTRN01C, CBTRN02C |
| CVTRA01Y (Cat Balance) | CBACT04C, CBTRN02C |
| CVTRA02Y (Disclosure) | CBACT04C |
| CVTRA03Y (Tran Type) | CBTRN03C |
| CVTRA04Y (Tran Cat) | CBTRN03C |
| COCOM01Y (COMMAREA) | ALL Online programs |
| CSUSR01Y (Security) | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COACTUPC, COACTVWC |
| CSUTLDWY (Date WS) | COACTUPC |
| CSUTLDPY (Date PD) | COACTUPC |
| CSLKPCDY (Lookups) | COACTUPC, COCRDLIC |
| CVEXPORT (Export) | CBEXPORT, CBIMPORT |
| COSTM01 (Report Layout) | CBSTM03A |

### BMS-Generated Copybooks → Programs

| Copybook | Program |
|----------|---------|
| COSGN00.CPY | COSGN00C |
| COMEN01.CPY | COMEN01C |
| COADM01.CPY | COADM01C |
| COACTVW.CPY | COACTVWC |
| COACTUP.CPY | COACTUPC |
| COCRDLI.CPY | COCRDLIC |
| COCRDSL.CPY | COCRDSLC |
| COCRDUP.CPY | COCRDUPC |
| COTRN00.CPY | COTRN00C |
| COTRN01.CPY | COTRN01C |
| COTRN02.CPY | COTRN02C |
| CORPT00.CPY | CORPT00C |
| COBIL00.CPY | COBIL00C |
| COUSR00.CPY | COUSR00C |
| COUSR01.CPY | COUSR01C |
| COUSR02.CPY | COUSR02C |
| COUSR03.CPY | COUSR03C |

---

## 3. Batch Job Data Lineage

### Daily Transaction Processing Pipeline

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  DAILY-TransactionBackup (Control-M Folder)                                  │
│                                                                              │
│  CLOSEFIL ──→ TRANBKP ──→ WAITSTEP ──→ OPENFIL                            │
│                  │                                                            │
│                  ▼                                                            │
│          TRANSACT.VSAM → TRANSACT.GDG(+1)                                   │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Full Batch Cycle (Manual Sequence)

```
CLOSEFIL ─────────────────────────────────── Closes VSAM for CICS
    │
    ▼
POSTTRAN ─── CBTRN02C ───────────────────── Posts daily transactions
    │  Reads: DALYTRAN.PS, TRANSACT.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, TCATBALF.VSAM
    │  Writes: TRANSACT.VSAM (updated), TCATBALF.VSAM (updated), ACCTDATA.VSAM (balances)
    │
    ▼
INTCALC ─── CBACT04C ───────────────────── Calculates interest
    │  Reads: ACCTDATA.VSAM, CARDXREF.VSAM, DISCGRP.VSAM, TCATBALF.VSAM, TRANSACT.VSAM
    │  Writes: TRANSACT.VSAM (interest entries), TCATBALF.VSAM (updated)
    │
    ▼
TRANBKP ─── IDCAMS ─────────────────────── Backup transactions
    │  Reads: TRANSACT.VSAM
    │  Writes: TRANSACT.GDG(+1)
    │
    ▼
COMBTRAN ─── SORT ──────────────────────── Merge daily + system
    │  Reads: DALYTRAN.PS, TRANSACT.VSAM
    │  Writes: Combined sorted output
    │
    ▼
CREASTMT ─── CBSTM03A / CBSTM03B ──────── Generate statements
    │  Reads: Combined transactions (sorted by card), CARDXREF.VSAM, CUSTDATA.VSAM
    │  Writes: Statement report file (print/PDF)
    │
    ▼
TRANREPT ─── CBTRN03C ─────────────────── Produce reports
    │  Reads: TRANSACT.VSAM (via AIX by card), TRANTYPE.VSAM, TRANCATG.VSAM
    │  Writes: Report output
    │
    ▼
OPENFIL ─────────────────────────────────── Re-opens files for CICS
```

### Data Initialization Lineage

```
Source PS Files (EBCDIC)         VSAM Targets
─────────────────────────       ──────────────────
ACCTDATA.PS      ──IDCAMS──→    ACCTDATA.VSAM.KSDS
CARDDATA.PS      ──IDCAMS──→    CARDDATA.VSAM.KSDS
CUSTDATA.PS      ──IDCAMS──→    CUSTDATA.VSAM.KSDS
CARDXREF.PS      ──IDCAMS──→    CARDXREF.VSAM.KSDS
DALYTRAN.PS.INIT ──IDCAMS──→    DALYTRAN.VSAM.KSDS  (also: TRANSACT.VSAM)
USRSEC.PS        ──IDCAMS──→    USRSEC.VSAM.KSDS
DISCGRP.PS       ──IDCAMS──→    DISCGRP.VSAM.KSDS
TRANCATG.PS      ──IDCAMS──→    TRANCATG.VSAM.KSDS
TRANTYPE.PS      ──IDCAMS──→    TRANTYPE.VSAM.KSDS
TCATBALF.PS      ──IDCAMS──→    TCATBALF.VSAM.KSDS
```

### Branch Migration Data Flow

```
CBEXPORT (Export)                        CBIMPORT (Import)
────────────────                        ────────────────
CUSTDATA.VSAM    ──→                    ──→ CUSTDATA.VSAM
ACCTDATA.VSAM    ──→  EXPORT.SEQ.FILE  ──→ ACCTDATA.VSAM
CARDXREF.VSAM    ──→  (500-byte recs)  ──→ CARDXREF.VSAM
TRANSACT.VSAM    ──→                    ──→ TRANSACT.VSAM
CARDDATA.VSAM    ──→                    ──→ CARDDATA.VSAM
```

---

## 4. Online Program → VSAM File Access

| Program | READ | WRITE/UPDATE | Files Accessed |
|---------|------|-------------|----------------|
| COSGN00C | R | — | USRSEC |
| COACTVWC | R | — | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF |
| COACTUPC | R | W | ACCTDATA, CUSTDATA, CARDXREF |
| COCRDLIC | R | — | CARDDATA, CARDXREF |
| COCRDSLC | R | — | CARDDATA, CARDXREF |
| COCRDUPC | R | W | CARDDATA, CARDXREF |
| COTRN00C | R | — | TRANSACT |
| COTRN01C | R | — | TRANSACT |
| COTRN02C | R | W | TRANSACT, CARDXREF, DALYTRAN |
| COBIL00C | R | W | ACCTDATA, TRANSACT |
| CORPT00C | R | — | TRANSACT |
| COUSR00C | R | — | USRSEC |
| COUSR01C | R | W | USRSEC |
| COUSR02C | R | W | USRSEC |
| COUSR03C | R | W | USRSEC |

---

## 5. External System Integration Points

### IBM MQ Message Flows

```
┌────────────────────┐        ┌──────────────┐        ┌────────────────────┐
│  External System   │──MQ───▶│  COPAUA0C    │──MQ───▶│  Response Queue    │
│  (Auth Requests)   │        │  (CP00 Tran) │        │  (Auth Decision)   │
└────────────────────┘        └──────────────┘        └────────────────────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │   IMS DB     │  (Pending Auth segments)
                              │   DB2        │  (Auth log table)
                              └──────────────┘

┌────────────────────┐        ┌──────────────┐        ┌────────────────────┐
│  CODATE01 (CDRD)   │──MQ───▶│  Date Server │──MQ───▶│  System Date       │
└────────────────────┘        └──────────────┘        └────────────────────┘

┌────────────────────┐        ┌──────────────┐        ┌────────────────────┐
│  COACCT01 (CDRA)   │──MQ───▶│  Acct Server │──MQ───▶│  Account Details   │
└────────────────────┘        └──────────────┘        └────────────────────┘
```

### DB2 Integration

```
COTRTLIC ──SQL──→ DB2 TRAN_TYPE_TABLE (SELECT, DELETE with cursor)
COTRTUPC ──SQL──→ DB2 TRAN_TYPE_TABLE (INSERT, UPDATE)
COBTUPDT ──SQL──→ DB2 TRAN_TYPE_TABLE (batch maintenance)
CREADB21 ──DDL──→ DB2 (CREATE TABLE, LOAD)
TRANEXTR ──SQL──→ DB2 → VSAM (unload to flat file)
COPAUS1C ──SQL──→ DB2 AUTH_LOG (INSERT)
```

---

## 6. Scheduler Dependencies (Control-M)

### DAILY-TransactionBackup (Runs: ALL days)
```
CLOSEFIL ─[produces]─→ DAILY-CLOSEFIL
                              │
TRANBKP ──[requires]──────────┘ ─[produces]─→ DAILY-TRANBKP
                                                      │
WAITSTEP ──[requires]─────────────────────────────────┘ ─[produces]─→ DAILY-WAITSTEP
                                                                              │
OPENFIL ───[requires]─────────────────────────────────────────────────────────┘
```

### WEEKLY-DisclosureGroupsRefresh (Runs: Saturday)
```
MNTTRDB2 ─[produces]─→ WEEKLY-MNTTRDB2
                              │
CLOSEFIL ──[requires]─────────┘ ─[produces]─→ WEEKLY-CLOSEFIL
                                                      │
DISCGRP ───[requires]─────────────────────────────────┘ ─[produces]─→ WEEKLY-DISCGRP
                                                                              │
WAITSTEP ──[requires]─────────────────────────────────────────────────────────┘ ─→ WEEKLY-WAITSTEP
                                                                                          │
OPENFIL ───[requires]─────────────────────────────────────────────────────────────────────┘
```

### WEEKLY-TransactionTypesDBRefresh (Runs: Saturday)
```
CLOSEFIL ─[produces]─→ CLOSEFIL-cond
                              │
TRANEXTR ──[requires]─────────┘ ─[produces]─→ TRANEXTR-cond
                                                      │
TRANCATG ──[requires]─────────────────────────────────┘ ─[produces]─→ TRANCATG-cond
                                                                              │
TRANTYPE ──[requires]─────────────────────────────────────────────────────────┘ ─→ TRANTYPE-cond
                                                                                          │
WAITSTEP ──[requires]─────────────────────────────────────────────────────────────────────┘ ─→ ...
    │
OPENFIL ──[requires]─────────────────────────────────────────────────────────────────────────────┘
```

### MONTHLY-InterestCalc
```
CLOSEFIL → INTCALC → WAITSTEP → OPENFIL
```

### MONTHLY-StatementGeneration
```
CLOSEFIL → PRTCATBL → CREASTMT → WAITSTEP → OPENFIL
```
