# DEPENDENCY MAP - CardDemo COBOL Estate

> **Generated:** 2026-03-25 | **Scope:** Inter-program calls, dataset lineage, and batch pipeline flow

---

## 1. Inter-Program Call Graph

### 1.1 CALL Relationships (Static Program Calls)

```
CBACT01C ──CALL──> COBDATFT (assembler: date formatting)

CBSTM03A ──CALL──> CBSTM03B (subroutine: file I/O for statement generation)
           CALL──> CEE3ABD  (LE: abnormal end)

CBSTM03B           (called by CBSTM03A; no outgoing CALLs)

CBACT04C           (no outgoing CALLs; standalone interest calculator)

COBSWAIT ──CALL──> MVSWAIT  (assembler: timed wait)

CSUTLDTC ──CALL──> CEEDAYS  (LE: date conversion service)

CORPT00C ──CALL──> CSUTLDTC (date validation utility)

COTRN02C ──CALL──> CSUTLDTC (date validation utility)

CBACT02C ──CALL──> CEE3ABD  (LE: abnormal end)
CBACT03C ──CALL──> CEE3ABD  (LE: abnormal end)
CBCUS01C ──CALL──> CEE3ABD  (LE: abnormal end)
```

### 1.2 CICS XCTL Relationships (Transfer of Control)

```
COSGN00C ──XCTL──> COMEN01C  (regular user -> main menu)
           XCTL──> COADM01C  (admin user -> admin menu)

COMEN01C ──XCTL──> COACTVWC  (Account View)
           XCTL──> COACTUPC  (Account Update)
           XCTL──> COCRDLIC  (Card List)
           XCTL──> COTRN00C  (Transaction List)
           XCTL──> CORPT00C  (Reports)
           XCTL──> COBIL00C  (Bill Payment)

COADM01C ──XCTL──> COUSR00C  (User List)
           XCTL──> COUSR01C  (User Add)
           XCTL──> COUSR02C  (User Update)
           XCTL──> COUSR03C  (User Delete)

COCRDLIC ──XCTL──> COCRDSLC  (Card Detail View)
           XCTL──> COCRDUPC  (Card Update)
           XCTL──> COMEN01C  (back to Main Menu)

COCRDSLC ──XCTL──> COCRDLIC  (back to Card List)

COCRDUPC ──XCTL──> COCRDLIC  (back to Card List)

COACTVWC ──XCTL──> COMEN01C  (back to Main Menu)

COACTUPC ──XCTL──> COMEN01C  (back to Main Menu)

COTRN00C ──XCTL──> COTRN01C  (Transaction View)
           XCTL──> COTRN02C  (Transaction Add)

COTRN01C ──XCTL──> COTRN00C  (back to Transaction List)

COTRN02C ──XCTL──> COTRN00C  (back to Transaction List)

CORPT00C ──XCTL──> COMEN01C  (back to Main Menu)

COBIL00C ──XCTL──> COMEN01C  (back to Main Menu)

COUSR00C ──XCTL──> COADM01C  (back to Admin Menu)
           XCTL──> COUSR02C  (User Update)
           XCTL──> COUSR03C  (User Delete)

COUSR01C ──XCTL──> COADM01C  (back to Admin Menu)

COUSR02C ──XCTL──> COUSR00C  (back to User List)

COUSR03C ──XCTL──> COUSR00C  (back to User List)
```

### 1.3 Complete Online Navigation Tree

```
Login (COSGN00C / CC00)
├── Regular User Path
│   └── Main Menu (COMEN01C / CM00)
│       ├── Account View (COACTVWC)
│       ├── Account Update (COACTUPC)
│       ├── Card List (COCRDLIC)
│       │   ├── Card Detail (COCRDSLC)
│       │   └── Card Update (COCRDUPC)
│       ├── Transaction List (COTRN00C)
│       │   ├── Transaction View (COTRN01C)
│       │   └── Transaction Add (COTRN02C)
│       ├── Reports (CORPT00C) ──submits──> TRANREPT.jcl
│       └── Bill Payment (COBIL00C)
│
└── Admin User Path
    └── Admin Menu (COADM01C / CA00)
        ├── User List (COUSR00C)
        ├── User Add (COUSR01C)
        ├── User Update (COUSR02C)
        └── User Delete (COUSR03C)
```

---

## 2. Dataset Lineage

### 2.1 VSAM Dataset Inventory

| Dataset (DSN) | Type | Record Key | Loaded By (JCL) | Read By (Programs) | Written By (Programs) |
|---------------|------|-----------|-----------------|--------------------|-----------------------|
| `ACCTDATA.VSAM.KSDS` | KSDS | ACCT-ID (9(11)) | ACCTFILE.jcl | CBACT01C, CBACT04C, COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBEXPORT, CBSTM03A/B | CBACT04C (REWRITE), COACTUPC (REWRITE), COBIL00C (REWRITE), CBIMPORT (WRITE) |
| `CARDDATA.VSAM.KSDS` | KSDS | CARD-NUM (X(16)) | CARDFILE.jcl | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC, CBEXPORT, CBSTM03A/B | COCRDUPC (REWRITE), CBIMPORT (WRITE) |
| `CUSTDATA.VSAM.KSDS` | KSDS | CUST-ID (9(09)) | CUSTFILE.jcl | CBCUS01C, COACTVWC, COCRDSLC, COCRDUPC, CBEXPORT, CBSTM03A/B | CBIMPORT (WRITE) |
| `CARDXREF.VSAM.KSDS` | KSDS | XREF-CARD-NUM (X(16)), AIX: XREF-ACCT-ID | XREFFILE.jcl | CBACT03C, CBACT04C, COTRN02C, COBIL00C, CBEXPORT, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A/B | CBIMPORT (WRITE) |
| `TRANSACT.VSAM.KSDS` | KSDS | TRAN-ID (X(16)), AIX: TRAN-PROC-TS | TRANFILE.jcl | COTRN00C, COTRN01C, CBEXPORT | COTRN02C (WRITE), COBIL00C (WRITE), CBTRN02C (WRITE), CBIMPORT (WRITE) |
| `USRSEC.VSAM.KSDS` | KSDS | SEC-USR-ID (X(08)) | DUSRSECJ.jcl | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| `TCATBALF.VSAM.KSDS` | KSDS | TRAN-CAT-KEY (composite) | TCATBALF.jcl | CBACT04C | CBTRN01C (WRITE), CBTRN02C (WRITE) |
| `TRANTYPE.VSAM.KSDS` | KSDS | TRAN-TYPE (X(02)) | TRANTYPE.jcl | CBTRN01C, CBTRN03C | (reference data only) |
| `TRANCATG.VSAM.KSDS` | KSDS | TRAN-CAT-KEY (composite) | TRANCATG.jcl | CBTRN01C, CBTRN03C | (reference data only) |
| `DISCGRP.VSAM.KSDS` | KSDS | DIS-GROUP-KEY (composite) | DISCGRP.jcl | CBACT04C | (reference data only) |
| `DALYREJS.VSAM.KSDS` | KSDS | (reject record key) | DALYREJS.jcl | (batch reject processing) | (batch reject writes) |

### 2.2 Sequential / GDG Datasets

| Dataset (DSN) | Type | Created By | Consumed By |
|---------------|------|-----------|-------------|
| `ACCTDATA.PS` | Sequential | (external/initial load) | ACCTFILE.jcl (REPRO to VSAM) |
| `CARDDATA.PS` | Sequential | (external/initial load) | CARDFILE.jcl (REPRO to VSAM) |
| `CUSTDATA.PS` | Sequential | (external/initial load) | CUSTFILE.jcl (REPRO to VSAM) |
| `CARDXREF.PS` | Sequential | (external/initial load) | XREFFILE.jcl (REPRO to VSAM) |
| `DALYTRAN.PS.INIT` | Sequential | (external/daily feed) | TRANFILE.jcl (initial load) |
| `USRSEC.PS` | Sequential | (external/initial load) | DUSRSECJ.jcl (REPRO to VSAM) |
| `TRANTYPE.PS` | Sequential | (external) | TRANTYPE.jcl (REPRO to VSAM) |
| `TRANCATG.PS` | Sequential | (external) | TRANCATG.jcl (REPRO to VSAM) |
| `TCATBALF.PS` | Sequential | (external) | TCATBALF.jcl (REPRO to VSAM) |
| `DISCGRP.PS` | Sequential | (external) | DISCGRP.jcl (REPRO to VSAM) |
| `TRANSACT.BKUP(+n)` | GDG | TRANBKP.jcl (REPROC), TRANREPT.jcl | TRANREPT.jcl (SORT filter) |
| `TRANSACT.DALY(+n)` | GDG | TRANREPT.jcl (SORT output) | TRANREPT.jcl -> CBTRN03C |
| `TRXFL.SEQ` | Sequential | CREASTMT.JCL (SORT output) | CREASTMT.JCL -> TRXFL.VSAM.KSDS |
| `TRXFL.VSAM.KSDS` | KSDS | CREASTMT.JCL (REPRO) | CBSTM03A (statement generation) |
| `STATEMNT.PS` | Sequential | CBSTM03A | TXT2PDF1.JCL (convert to PDF) |
| `STATEMNT.HTML` | Sequential | CBSTM03A | (web output) |
| `EXPFILE` | KSDS | CBEXPORT | CBIMPORT |

### 2.3 Dataset Flow Diagram

```
External Data Sources (PS files)
        │
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA REFRESH LAYER                            │
│  ACCTFILE.jcl  CARDFILE.jcl  CUSTFILE.jcl  XREFFILE.jcl        │
│  TRANFILE.jcl  DUSRSECJ.jcl  TRANTYPE.jcl  TRANCATG.jcl       │
│  TCATBALF.jcl  DISCGRP.jcl                                      │
│              (IDCAMS REPRO: PS -> VSAM KSDS)                     │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                     VSAM MASTER FILES                            │
│  ACCTDATA  CARDDATA  CUSTDATA  CARDXREF  TRANSACT  USRSEC      │
│  TCATBALF  TRANTYPE  TRANCATG  DISCGRP                          │
└──────┬──────────────────────┬───────────────────────────────────┘
       │                      │
       ▼                      ▼
┌──────────────┐    ┌─────────────────────────────────────────────┐
│ ONLINE CICS  │    │              BATCH PROCESSING                │
│ Programs     │    │  POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN │
│ (read/write  │    │  -> CREASTMT -> TRANREPT                     │
│  VSAM files) │    │  (read/write VSAM + create sequential)       │
└──────────────┘    └─────────────────────┬───────────────────────┘
                                          │
                                          ▼
                              ┌────────────────────────┐
                              │    OUTPUT FILES          │
                              │  STATEMNT.PS (statements)│
                              │  STATEMNT.HTML           │
                              │  TRANSACT.BKUP (GDG)    │
                              │  TRANSACT.DALY (GDG)    │
                              │  Report output (SYSOUT)  │
                              └────────────────────────┘
```

---

## 3. End-to-End Batch Pipeline Flow

### 3.1 Nightly Batch Cycle Sequence

The batch cycle runs in strict sequence, managed by the job scheduler (CA7/Control-M definitions in `app/scheduler/`):

```
Phase 1: PREPARATION
─────────────────────
CLOSEFIL.jcl          Close all CICS files (disable online access)
    │
    ▼

Phase 2: DATA REFRESH (run in parallel or sequence as needed)
─────────────────────────────────────────────────────────────
ACCTFILE.jcl          Refresh Account Master (PS -> VSAM)
CARDFILE.jcl          Refresh Card Master (PS -> VSAM)
CUSTFILE.jcl          Refresh Customer Master (PS -> VSAM)
XREFFILE.jcl          Refresh Card Cross-Reference (PS -> VSAM + AIX)
TRANFILE.jcl          Refresh Transaction Master (PS -> VSAM + AIX)
DUSRSECJ.jcl          Refresh User Security (PS -> VSAM)
TRANTYPE.jcl          Refresh Transaction Type reference
TRANCATG.jcl          Refresh Transaction Category reference
TCATBALF.jcl          Refresh Transaction Category Balances
DISCGRP.jcl           Refresh Discount Group reference
    │
    ▼

Phase 3: CORE TRANSACTION PROCESSING
─────────────────────────────────────
POSTTRAN.jcl          Post daily transactions
    │                 └─ PGM=CBTRN02C
    │                    ├── READ:  DALYTRAN (daily transaction input)
    │                    ├── READ:  CARDXREF, ACCTDAT (validation)
    │                    ├── READ:  TRANTYPE, TRANCATG (reference lookup)
    │                    ├── WRITE: TRANSACT (post to master)
    │                    └── WRITE: TCATBALF (update category balances)
    │
    ▼
INTCALC.jcl           Calculate interest and fees
    │                 └─ PGM=CBACT04C
    │                    ├── READ:  TCATBALF (category balances)
    │                    ├── READ:  XREFFILE (card-account xref)
    │                    ├── READ:  DISCGRP (interest rates)
    │                    ├── READ:  ACCTFILE (account data)
    │                    ├── REWRITE: ACCTFILE (updated balances)
    │                    └── WRITE: TRANSACT (interest transactions)
    │
    ▼

Phase 4: BACKUP AND CONSOLIDATION
──────────────────────────────────
TRANBKP.jcl           Backup transaction file
    │                 └─ REPROC: TRANSACT.VSAM.KSDS -> TRANSACT.BKUP(+1)
    │                 └─ IDCAMS: Delete and redefine TRANSACT VSAM
    │
    ▼
COMBTRAN.jcl          Combine and sort daily transactions
    │                 └─ SORT: sort daily transaction records
    │                 └─ IDCAMS REPRO: merge into TRANSACT.VSAM.KSDS
    │
    ▼

Phase 5: REPORTING AND STATEMENTS
──────────────────────────────────
CREASTMT.JCL          Create account statements
    │                 └─ SORT: sort transactions by card+ID -> TRXFL.SEQ
    │                 └─ IDCAMS REPRO: TRXFL.SEQ -> TRXFL.VSAM.KSDS
    │                 └─ PGM=CBSTM03A: generate statement text + HTML
    │                    ├── READ: TRXFL.VSAM.KSDS (sorted transactions)
    │                    ├── READ: XREFFILE (card-account mapping)
    │                    ├── READ: ACCTFILE (account details)
    │                    ├── READ: CUSTFILE (customer details)
    │                    ├── WRITE: STATEMNT.PS (text statements)
    │                    └── WRITE: STATEMNT.HTML (HTML statements)
    │
    ├── (Optional)
    │   TXT2PDF1.JCL      Convert text statements to PDF
    │
    ▼
TRANREPT.jcl          Generate transaction reports
    │                 └─ REPROC: backup transactions -> GDG
    │                 └─ SORT: filter by date range -> TRANSACT.DALY(+1)
    │                 └─ PGM=CBTRN03C: produce formatted reports
    │                    ├── READ: TRANFILE (filtered transactions)
    │                    ├── READ: CARDXREF (card-account mapping)
    │                    ├── READ: TRANTYPE (type descriptions)
    │                    ├── READ: TRANCATG (category descriptions)
    │                    └── WRITE: Report output (SYSOUT/printer)
    │
    ▼

Phase 6: INDEX MAINTENANCE
──────────────────────────
TRANIDX.jcl           Rebuild transaction alternate indexes
    │                 └─ IDCAMS: define AIX, path, BLDINDEX
    │
    ▼

Phase 7: REOPENING
───────────────────
OPENFIL.jcl           Re-open all CICS files (enable online access)
```

### 3.2 On-Demand / Ad-Hoc Jobs

| Job | Triggered By | Purpose |
|-----|-------------|---------|
| **TRANREPT.jcl** | CORPT00C (online report request via CICS TD queue) | Generate transaction report for date range |
| **CBEXPORT.jcl** | Manual submission | Export all data for branch migration |
| **CBIMPORT.jcl** | Manual submission | Import data from export file |
| **READACCT/READCARD/READCUST/READXREF.jcl** | Manual (diagnostic) | Dump VSAM file contents for debugging |
| **WAITSTEP.jcl** | Scheduler | Wait utility between batch steps |

---

## 4. Copybook Dependency Matrix

### 4.1 Which Programs Use Which Copybooks

| Copybook | Online Programs | Batch Programs |
|----------|----------------|----------------|
| **COCOM01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | — |
| **CVACT01Y** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVACT02Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBEXPORT, CBIMPORT |
| **CVACT03Y** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A |
| **CVCUS01Y** | COACTVWC, COCRDSLC, COCRDUPC | CBCUS01C, CBEXPORT, CBIMPORT |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT |
| **CVTRA06Y** | — | CBTRN01C, CBTRN02C |
| **CSUSR01Y** | COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | — |
| **COTTL01Y** | All 17 online programs | — |
| **CSDAT01Y** | All 17 online programs | — |
| **CSMSG01Y** | All 17 online programs | — |
| **DFHAID** | All 17 online programs | — |
| **DFHBMSCA** | All 17 online programs | — |

---

## 5. External Program Dependencies

| External Program | Type | Called By | Purpose |
|-----------------|------|----------|---------|
| **COBDATFT** | Assembler (`app/asm/`) | CBACT01C | Date format conversion |
| **MVSWAIT** | Assembler (`app/asm/`) | COBSWAIT | Timed wait/delay |
| **CEEDAYS** | LE Runtime Service | CSUTLDTC | Julian date conversion |
| **CEE3ABD** | LE Runtime Service | CBACT02C, CBACT03C, CBSTM03A | Abnormal termination |
| **IDCAMS** | System Utility | 20+ JCL jobs | VSAM define/delete/REPRO |
| **SORT** | System Utility | COMBTRAN, CREASTMT, TRANREPT | Sort/merge sequential files |
| **IEFBR14** | System Utility | CREASTMT | Dummy step (delete datasets) |
| **SDSF** | System Utility | CLOSEFIL, OPENFIL, TRANFILE | CICS file enable/disable |
| **IKJEFT1B** | TSO Batch | TXT2PDF1 | Run REXX in batch |
| **FTP** | System Utility | FTPJCL | File transfer |
| **IEBGENER** | System Utility | INTRDRJ1 | Dataset copy to internal reader |
