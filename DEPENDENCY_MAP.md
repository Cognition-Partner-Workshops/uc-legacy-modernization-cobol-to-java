# Dependency Map — CardDemo COBOL Estate

## 1. Inter-Program Call Graph

### 1.1 Online CICS Navigation (XCTL / LINK)

All online programs share state via `CARDDEMO-COMMAREA` (COCOM01Y.cpy). Navigation uses `EXEC CICS XCTL` (transfer control, no return) or `EXEC CICS LINK` (call/return).

```
                        ┌─────────────┐
                        │  COSGN00C   │  Sign-on
                        │  (Login)    │
                        └──────┬──────┘
                   ┌───────────┴───────────┐
                   ▼                       ▼
          ┌────────────────┐      ┌────────────────┐
          │   COADM01C     │      │   COMEN01C     │
          │ (Admin Menu)   │      │ (User Menu)    │
          └───┬──┬──┬──┬───┘      └─┬──┬──┬──┬──┬──┬──┬──┬──┬──┬─┘
              │  │  │  │            │  │  │  │  │  │  │  │  │  │
              ▼  │  │  │            ▼  │  │  │  │  │  │  │  │  │
         COUSR00C│  │  │       COACTVWC│  │  │  │  │  │  │  │  │
    (User List)  │  │  │  (Acct View)  │  │  │  │  │  │  │  │  │
              ▼  │  │  │               ▼  │  │  │  │  │  │  │  │
         COUSR01C│  │  │          COACTUPC│  │  │  │  │  │  │  │
     (User Add)  │  │  │   (Acct Update) │  │  │  │  │  │  │  │
              ▼  │  │  │                  ▼  │  │  │  │  │  │  │
         COUSR02C│  │  │             COCRDLIC│  │  │  │  │  │  │
  (User Update)  │  │  │      (Card List)   │  │  │  │  │  │  │
              ▼  │  │  │         ┌──────┴──────┐ │  │  │  │  │  │
         COUSR03C│  │  │         ▼             ▼ │  │  │  │  │  │
  (User Delete)  │  │  │    COCRDSLC     COCRDUPC│  │  │  │  │  │
                 │  │  │    (Card View)  (Card Up)│  │  │  │  │  │
                 ▼  │  │                          ▼  │  │  │  │  │
            COTRTLIC│  │                     COTRN00C│  │  │  │  │
   (TranType List)  │  │              (Tran List)    │  │  │  │  │
                 ▼  │  │                             ▼  │  │  │  │
            COTRTUPC│  │                        COTRN01C│  │  │  │
   (TranType Update)│  │               (Tran View)     │  │  │  │
                    │  │                                ▼  │  │  │
                    │  │                           COTRN02C│  │  │
                    │  │                    (Tran Add)     │  │  │
                    │  │                                   ▼  │  │
                    │  │                              CORPT00C│  │
                    │  │                       (Reports)      │  │
                    │  │                                      ▼  │
                    │  │                                 COBIL00C│
                    │  │                          (Bill Payment) │
                    │  │                                         ▼
                    │  │                                    COPAUS0C
                    │  │                             (Pend Auth View)
                    │  │                                    │
                    │  │                                    ▼
                    │  │                               COPAUS1C
                    │  │                          (Auth Detail View)
                    │  │                                    │
                    │  │                               LINK ▼
                    │  │                               COPAUS2C
                    │  │                          (Fraud Update)
                    │  │
                    └──┘
```

### 1.2 Online CICS XCTL Returns
All sub-programs return to their parent menu via XCTL using `CDEMO-FROM-PROGRAM` stored in the COMMAREA:
- User programs → `COADM01C` (admin menu)
- Transaction/Account/Card programs → `COMEN01C` (user menu) or `COADM01C`

### 1.3 Batch Program CALL Dependencies

```
CBSTM03A ──CALL──► CBSTM03B    (statement generation calls file I/O subroutine)
CBACT01C ──CALL──► COBDATFT    (account read calls date formatter — external)
CBACT01C ──CALL──► CEE3ABD     (LE abend — all batch programs)
CBACT04C ──CALL──► CEE3ABD
COTRN02C ──CALL──► CSUTLDTC    (transaction add calls date utility)
CORPT00C ──CALL──► CSUTLDTC    (report request calls date utility)
COBSWAIT ──CALL──► MVSWAIT     (wait utility calls assembler — external)
```

### 1.4 Batch IMS DLI Call Dependencies

```
COPAUA0C ──DLI────► IMS Auth DB    (SCHD, GU, REPL)
         ──MQ─────► Request Queue   (MQGET)
         ──MQ─────► Reply Queue     (MQPUT1)
COPAUS0C ──DLI────► IMS Auth DB    (SCHD, GU, GNP)
COPAUS1C ──DLI────► IMS Auth DB    (GU, GNP, REPL)
         ──LINK───► COPAUS2C       (fraud DB2 update)
CBPAUP0C ──DLI────► IMS Auth DB    (GN, GNP, DLET, CHKP)
DBUNLDGS ──DLI────► IMS Auth DB    (GN, GNP)
         ──GSAM──► Output files
PAUDBLOD ──DLI────► IMS Auth DB    (ISRT, GU)
         ──Read──► Input flat files
PAUDBUNL ──DLI────► IMS Auth DB    (GN, GNP)
         ──Write─► Output flat files
```

### 1.5 MQ Message Flow (VSAM-MQ Sub-Application)

```
External ──MQ PUT──► Request Queue ──► COACCT01 ──VSAM READ──► Account File
                                                 ──MQ PUT───► Reply Queue / Error Queue

External ──MQ PUT──► Request Queue ──► CODATE01 ──CICS ASKTIME──► System Date
                                                 ──MQ PUT──────► Reply Queue / Error Queue
```

---

## 2. Dataset Lineage

### 2.1 VSAM Master Files and Their Lifecycle

| Dataset (DSN Pattern) | VSAM Type | JCL: Define/Load | Programs: Read | Programs: Write/Update |
|----------------------|-----------|-------------------|----------------|----------------------|
| `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS | ACCTFILE.jcl | CBACT01C, CBACT04C (I-O), CBEXPORT, CBTRN01C, CBTRN02C (I-O), CBSTM03A/B, COACTUPC, COACTVWC, COBIL00C, COPAUA0C, COPAUS0C, COACCT01 | CBACT04C (REWRITE), CBTRN02C (REWRITE), COACTUPC (REWRITE), COBIL00C (REWRITE) |
| `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | KSDS+AIX | CARDFILE.jcl | CBACT02C, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC (via AIX), COACTVWC (via AIX), COPAUS0C | COCRDUPC (REWRITE) |
| `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | KSDS | CUSTFILE.jcl | CBCUS01C, CBEXPORT, CBTRN01C, CBSTM03A/B, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC, COPAUA0C, COPAUS0C | — (read-only in all programs) |
| `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | KSDS+AIX | XREFFILE.jcl | CBACT04C, CBEXPORT, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A/B, COTRN02C, COBIL00C, COPAUA0C | — (read-only) |
| `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | KSDS+AIX | TRANFILE.jcl, COMBTRAN.jcl | CBEXPORT, COTRN00C, COTRN01C, COTRN02C, CBSTM03A/B (via TRNXFILE alias) | CBTRN02C (WRITE), COTRN02C (WRITE), COBIL00C (WRITE) |
| `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | KSDS | DUSRSECJ.jcl | COSGN00C, COUSR00C | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS` | KSDS | TCATBALF.jcl | CBACT04C (I-O), CBTRN02C (I-O) | CBACT04C (REWRITE), CBTRN02C (WRITE/REWRITE) |
| `AWS.M2.CARDDEMO.TRANTYPE.PS` | PS→VSAM | TRANTYPE.jcl | CBTRN03C | — |
| `AWS.M2.CARDDEMO.TRANCATG.PS` | PS→VSAM | TRANCATG.jcl | CBTRN03C | — |
| `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | KSDS | DISCGRP.jcl | CBACT04C | — |

### 2.2 Sequential/GDG/Temp Files

| Dataset | JCL Job | Direction | Program |
|---------|---------|-----------|---------|
| `DALYTRAN` (daily transaction input) | POSTTRAN.jcl | Input | CBTRN02C |
| `DALYREJS` (daily rejects GDG) | POSTTRAN.jcl | Output | CBTRN02C |
| `TRANREPT` (transaction report GDG) | TRANREPT.jcl | Output | CBTRN03C |
| `DATEPARM` (date range file) | TRANREPT.jcl | Input | CBTRN03C |
| `EXPFILE` (consolidated export) | CBEXPORT.jcl | Output | CBEXPORT |
| `EXPFILE` (consolidated export) | CBIMPORT.jcl | Input | CBIMPORT |
| `CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT` | CBIMPORT.jcl | Output | CBIMPORT |
| `STMTFILE` (text statements) | CREASTMT.JCL | Output | CBSTM03A |
| `HTMLFILE` (HTML statements) | CREASTMT.JCL | Output | CBSTM03A |
| `OUTFILE, ARRYFILE, VBRCFILE` | READACCT.jcl | Output | CBACT01C |
| `TRXFL.SEQ → TRXFL.VSAM.KSDS` | CREASTMT.JCL | Temp | SORT → IDCAMS REPRO |
| `TRANSACT backup GDG` | TRANBKP.jcl | Output | IDCAMS REPRO |
| `TCATBAL backup/restore` | PRTCATBL.jcl | I/O | REPROC + SORT |

### 2.3 DB2 Tables

| Table | Define JCL | Programs That Access | Operations |
|-------|-----------|---------------------|------------|
| TRTYP (Transaction Types) | CREADB21.jcl | COTRTLIC (SELECT/CURSOR), COTRTUPC (SELECT/INSERT/UPDATE/DELETE), COBTUPDT (INSERT/UPDATE/DELETE), MNTTRDB2→COBTUPDT, TRANEXTR (UNLOAD) | Full CRUD |
| TRCAT (Transaction Categories) | CREADB21.jcl | COTRTUPC (SELECT), TRANEXTR (UNLOAD) | Read + maintenance |
| Auth Fraud Table | DBPAUTP0.jcl | COPAUS2C (INSERT/SELECT) | Write + read |

### 2.4 IMS Databases

| Database | Load JCL | Unload JCL | Online Programs | Batch Programs |
|----------|---------|------------|-----------------|----------------|
| Pending Auth (root: summary, child: detail) | LOADPADB | UNLDPADB, UNLDGSAM | COPAUA0C (SCHD/GU/REPL), COPAUS0C (SCHD/GU/GNP), COPAUS1C (GU/GNP/REPL) | CBPAUP0C (GN/GNP/DLET), DBUNLDGS (GN/GNP), PAUDBUNL (GN/GNP), PAUDBLOD (ISRT/GU) |

### 2.5 MQ Queues

| Queue | Direction | Program |
|-------|-----------|---------|
| Auth Request Queue | GET | COPAUA0C |
| Auth Reply Queue | PUT | COPAUA0C |
| Auth Error Queue | PUT | COPAUA0C (on error) |
| Account Request Queue | GET | COACCT01 |
| Account Reply Queue | PUT | COACCT01 |
| Account Error Queue | PUT | COACCT01 (on error) |
| Date Request Queue | GET | CODATE01 |
| Date Reply Queue | PUT | CODATE01 |
| Date Error Queue | PUT | CODATE01 (on error) |

---

## 3. End-to-End Batch Pipeline Flows

### 3.1 Daily Transaction Processing

```
                        ┌─────────────────────────┐
                        │  External: Daily Tran    │
                        │  File (DALYTRAN)         │
                        └───────────┬─────────────┘
                                    │
  ┌─────────────────────────────────▼──────────────────────────────────┐
  │  POSTTRAN.jcl → CBTRN02C                                          │
  │  1. Read DALYTRAN records                                          │
  │  2. Validate: look up XREFFILE (card→acct), ACCTFILE (acct status)│
  │  3. Post valid transactions → TRANSACT (WRITE)                     │
  │  4. Update ACCTFILE balances (REWRITE)                             │
  │  5. Update TCATBALF category balances (WRITE/REWRITE)              │
  │  6. Write rejects → DALYREJS                                      │
  └─────┬──────────────────┬──────────────────┬───────────────────────┘
        │                  │                  │
        ▼                  ▼                  ▼
  ┌──────────┐     ┌───────────┐      ┌──────────┐
  │ TRANSACT │     │ ACCTFILE  │      │ DALYREJS │
  │ (updated)│     │ (updated) │      │ (rejects)│
  └──────────┘     └───────────┘      └──────────┘
```

### 3.2 Daily Transaction Backup (Control-M: DAILY-TransactionBackup)

```
  CLOSEFIL ──► TRANBKP ──► WAITSTEP ──► OPENFIL
     │            │            │            │
  Close CICS   REPRO VSAM   COBSWAIT    Reopen CICS
  files        → GDG backup  (pause)     files
```

### 3.3 Monthly Interest Calculation (Control-M: MONTHLY-InterestCalculation)

```
  CLOSEFIL ──► INTCALC ──────────────────► COMBTRAN ──► WAITSTEP ──► OPENFIL
     │            │                            │            │            │
  Close CICS   CBACT04C:                    SORT+REPRO   COBSWAIT    Reopen
  files        1. Read TCATBALF              new trans    (pause)     files
               2. Read XREFFILE, DISCGRP     into VSAM
               3. Calculate interest/fees    master
               4. REWRITE ACCTFILE
               5. WRITE new trans → TRANSACT
```

### 3.4 Statement Generation Pipeline

```
  CREASTMT.JCL:
  ┌──────────────────────────────────────────────────────────────────────────┐
  │ DELDEF01: IDCAMS (delete old output)                                    │
  │     │                                                                   │
  │     ▼                                                                   │
  │ STEP010: SORT (sort TRANSACT VSAM → sequential by card+tran)           │
  │     │                                                                   │
  │     ▼                                                                   │
  │ STEP020: IDCAMS REPRO (sequential → TRXFL.VSAM.KSDS)                   │
  │     │                                                                   │
  │     ▼                                                                   │
  │ STEP040: CBSTM03A                                                       │
  │   ├── CALL CBSTM03B (open TRNXFILE)                                    │
  │   ├── CALL CBSTM03B (open XREFFILE)                                    │
  │   ├── CALL CBSTM03B (open CUSTFILE, ACCTFILE)                          │
  │   ├── Read transactions by card                                         │
  │   ├── Look up xref → customer → account                                │
  │   ├── Format text statement → STMTFILE                                 │
  │   ├── Format HTML statement → HTMLFILE                                 │
  │   └── CALL CBSTM03B (close all files)                                  │
  └──────────────────────────────────────────────────────────────────────────┘
  │
  ▼
  TXT2PDF1.JCL: IKJEFT1B → TXT2PDF REXX (convert text statement to PDF)
```

### 3.5 Transaction Reporting Pipeline

```
  TRANREPT.JCL:
  ┌─────────────────────────────────────────────────────────────────┐
  │ STEP05R: REPROC (backup TRANSACT to GDG)                       │
  │     │                                                           │
  │     ▼                                                           │
  │ STEP05R: SORT (sort transactions for report)                   │
  │     │                                                           │
  │     ▼                                                           │
  │ STEP10R: CBTRN03C                                               │
  │   ├── Read sorted TRANFILE                                      │
  │   ├── Look up CARDXREF (card → acct)                           │
  │   ├── Look up TRANTYPE (type description)                      │
  │   ├── Look up TRANCATG (category description)                  │
  │   ├── Read DATEPARM (date range filter)                        │
  │   ├── Format detail lines with page totals                     │
  │   └── Write TRANREPT (formatted report → GDG)                 │
  └─────────────────────────────────────────────────────────────────┘
```

### 3.6 Data Export/Import (Migration)

```
  CBEXPORT.JCL:
  ┌─────────────────────────────────────────────┐
  │ STEP02: CBEXPORT                             │
  │   Read: CUSTFILE, ACCTFILE, XREFFILE,       │
  │         TRANSACT, CARDFILE                   │
  │   Write: EXPFILE (consolidated, tagged       │
  │          records — type C/A/X/T)             │
  └──────────────────┬──────────────────────────┘
                     │
                     ▼ (transfer EXPFILE to target)
  ┌─────────────────────────────────────────────┐
  │ CBIMPORT.JCL:                                │
  │ STEP01: CBIMPORT                             │
  │   Read: EXPFILE                              │
  │   Write: CUSTOUT, ACCTOUT, XREFOUT,         │
  │          TRNXOUT, CARDOUT                    │
  │   Errors: ERROUT                             │
  └─────────────────────────────────────────────┘
```

### 3.7 Weekly DB2 Refresh (Control-M: WEEKLY-TransactionTypesDBRefresh)

```
  MNTTRDB2.jcl (PGM=COBTUPDT):
  ┌────────────────────────────────────┐
  │ Read INPFILE (flat maintenance)    │
  │ INSERT/UPDATE/DELETE on TRTYP DB2  │
  └────────────────┬───────────────────┘
                   │
                   ▼
  TRANEXTR.jcl:
  ┌────────────────────────────────────┐
  │ DB2 UNLOAD TRTYP → TRANTYPE.PS    │
  │ DB2 UNLOAD TRCAT → TRANCATG.PS    │
  └────────────────────────────────────┘
                   │
                   ▼
  CLOSEFIL → DISCGRP.jcl → WAITSTEP → OPENFIL
  (Refresh disclosure groups VSAM from PS)
```

### 3.8 IMS Authorization Database Maintenance

```
  UNLDPADB.jcl (PGM=PAUDBUNL):    UNLDGSAM.jcl (PGM=DBUNLDGS):
  ┌───────────────────────┐        ┌───────────────────────┐
  │ IMS DLI GN/GNP        │        │ IMS DLI GN/GNP        │
  │ → OPFILE1 (summaries) │        │ → GSAM output files   │
  │ → OPFILE2 (details)   │        │   (summaries+details) │
  └───────────────────────┘        └───────────────────────┘

  LOADPADB.jcl (PGM=PAUDBLOD):
  ┌───────────────────────┐
  │ Read INFILE1, INFILE2 │
  │ IMS DLI ISRT/GU       │
  │ → Load IMS Auth DB    │
  └───────────────────────┘

  CBPAUP0J.jcl (PGM=CBPAUP0C):
  ┌───────────────────────┐
  │ IMS DLI GN/GNP/DLET   │
  │ Purge expired auth     │
  │ records with CHKP      │
  └───────────────────────┘
```

---

## 4. Copybook Dependency Matrix

Programs and the copybooks they reference (COPY statements).

### Core Data Copybooks

| Copybook | Business Entity | Referenced By |
|----------|----------------|---------------|
| CVACT01Y | Account | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COTRN02C, COPAUA0C, COPAUS0C, COACCT01 |
| CVACT02Y | Card | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COPAUS0C, COTRTLIC |
| CVACT03Y | Cross-Reference | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C, COPAUA0C, COPAUS0C |
| CVCUS01Y | Customer | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC, COPAUA0C, COPAUS0C |
| CVTRA05Y | Transaction | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, COTRN00C, COTRN01C, COTRN02C, CORPT00C |
| CVTRA06Y | Daily Transaction | CBTRN01C, CBTRN02C |
| CVTRA01Y | Category Balance | CBACT04C, CBTRN02C |
| CVTRA02Y | Disclosure Group | CBACT04C |
| CVTRA03Y | Transaction Type | CBTRN03C |
| CVTRA04Y | Transaction Category | CBTRN03C |
| CSUSR01Y | User Security | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COADM01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COMEN01C, COTRTLIC, COTRTUPC, COPAUS0C, COPAUS1C |
| CVEXPORT | Export Record | CBEXPORT, CBIMPORT |
| CUSTREC | Customer (alt) | CBSTM03A |
| COSTM01 | Statement Tran | CBSTM03A |

### Infrastructure Copybooks

| Copybook | Purpose | Referenced By (count) |
|----------|---------|----------------------|
| COCOM01Y | COMMAREA | 20 online programs |
| CVCRD01Y | Working storage | 14 online programs |
| DFHBMSCA | BMS attributes | 17 online programs |
| DFHAID | AID keys | 17 online programs |
| COTTL01Y | Screen titles | 18 online programs |
| CSDAT01Y | Date/Time WS | 18 online programs |
| CSMSG01Y | Common messages | 18 online programs |
| CSMSG02Y | Abend data | 10 online programs |
| CSSTRPFY | PF key storage | 7 online programs |
| CSUTLDWY | Date validation WS | COACTUPC, COTRTUPC |
| CSUTLDPY | Date validation PD | COACTUPC, COTRTUPC |
| CSLKPCDY | Lookup codes | COACTUPC |
| CSSETATY | Attribute template | COACTUPC, COTRTUPC |
| CSDB2RWY | DB2 working storage | COTRTLIC, COTRTUPC |
| CSDB2RPY | DB2 priming query | COTRTLIC, COTRTUPC |
| CODATECN | Date conversion | CBACT01C |
| IMSFUNCS | IMS function codes | CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |

### BMS Map Copybooks (Screen Layouts)

| Copybook | Screen | Referenced By |
|----------|--------|---------------|
| COACTUP | Account Update | COACTUPC |
| COACTVW | Account View | COACTVWC |
| COADM01 | Admin Menu | COADM01C |
| COBIL00 | Bill Payment | COBIL00C |
| COCRDLI | Card List | COCRDLIC |
| COCRDSL | Card Detail | COCRDSLC |
| COCRDUP | Card Update | COCRDUPC |
| COMEN01 | User Menu | COMEN01C |
| COPAU00 | Auth Summary | COPAUS0C |
| COPAU01 | Auth Detail | COPAUS1C |
| CORPT00 | Report Request | CORPT00C |
| COSGN00 | Sign-on | COSGN00C |
| COTRN00 | Transaction List | COTRN00C |
| COTRN01 | Transaction View | COTRN01C |
| COTRN02 | Transaction Add | COTRN02C |
| COTRTLI | Tran Type List | COTRTLIC |
| COTRTUP | Tran Type Update | COTRTUPC |
| COUSR00 | User List | COUSR00C |
| COUSR01 | User Add | COUSR01C |
| COUSR02 | User Update | COUSR02C |
| COUSR03 | User Delete | COUSR03C |
