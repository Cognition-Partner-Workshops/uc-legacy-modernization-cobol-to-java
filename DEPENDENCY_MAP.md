# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
>
> This document maps the complete call graph (which programs invoke which) and
> data lineage (which programs and jobs read/write which files) across the CardDemo application.

---

## Table of Contents

1. [Online Program Call Graph](#online-program-call-graph)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [Copybook Dependency Matrix](#copybook-dependency-matrix)
4. [VSAM File Access Map (Online)](#vsam-file-access-map-online)
5. [File Access Map (Batch)](#file-access-map-batch)
6. [JCL Job → Program → Dataset Lineage](#jcl-job--program--dataset-lineage)
7. [Batch Cycle Execution Order](#batch-cycle-execution-order)
8. [End-to-End Data Flow](#end-to-end-data-flow)
9. [Cross-Module Dependencies (Optional Modules)](#cross-module-dependencies-optional-modules)

---

## Online Program Call Graph

All online navigation uses **EXEC CICS XCTL** (transfer control) via the COMMAREA.
The target program name is resolved at runtime from the menu option tables in
COMEN02Y (regular menu) and COADM02Y (admin menu).

### Sign-on Flow

```
CICS Transaction CC00
  └── COSGN00C (Sign-on)
        ├── [Admin user] ──XCTL──→ COADM01C (Admin Menu)
        └── [Regular user] ──XCTL──→ COMEN01C (Main Menu)
```

### Main Menu Navigation (COMEN01C)

```
COMEN01C (Main Menu) ──XCTL──→
  ├── Option 1  → COACTVWC  (Account View)
  ├── Option 2  → COACTUPC  (Account Update)
  ├── Option 3  → COCRDLIC  (Card List)
  │                  ├── Select → COCRDSLC  (Card Detail View)
  │                  └── Update → COCRDUPC  (Card Update)
  ├── Option 4  → COCRDSLC  (Card Detail View)
  ├── Option 5  → COCRDUPC  (Card Update)
  ├── Option 6  → COTRN00C  (Transaction List)
  │                  └── Select → COTRN01C  (Transaction View)
  ├── Option 7  → COTRN01C  (Transaction View)
  ├── Option 8  → COTRN02C  (Transaction Add)
  ├── Option 9  → CORPT00C  (Transaction Reports)
  ├── Option 10 → COBIL00C  (Bill Payment)
  └── Option 11 → COPAUS0C  (Pending Auth View) [Optional Module]
```

### Admin Menu Navigation (COADM01C)

```
COADM01C (Admin Menu) ──XCTL──→
  ├── Option 1 → COUSR00C  (User List)
  │                  ├── Select → COUSR02C  (User Update)
  │                  └── Delete → COUSR03C  (User Delete)
  ├── Option 2 → COUSR01C  (User Add)
  ├── Option 3 → COUSR02C  (User Update)
  ├── Option 4 → COUSR03C  (User Delete)
  ├── Option 5 → COTRTLIC  (Trans Type List/Update) [DB2 Module]
  └── Option 6 → COTRTUPC  (Trans Type Maintenance) [DB2 Module]
```

### Return Flow

All online programs return to the calling menu via:
```
[Any Program] ──XCTL(CDEMO-TO-PROGRAM)──→ COMEN01C or COADM01C
```

### Subroutine Calls (Online → Shared)

```
CORPT00C ──CALL──→ CSUTLDTC (Date Conversion)
COTRN02C ──CALL──→ CSUTLDTC (Date Conversion)
CSUTLDTC ──CALL──→ CEEDAYS  (LE Date Service — system provided)
```

---

## Batch Program Call Graph

```
CBSTM03A (Statement Generation - Main)
  └── CALL ──→ CBSTM03B (Statement Generation - Subroutine)
                  └── File I/O: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE

COBSWAIT (Wait Utility)
  └── CALL ──→ MVSWAIT (Assembler Wait Routine)

CBTRN01C (Transaction Validation) ── standalone, no CALLs
CBTRN02C (Transaction Posting)    ── standalone, no CALLs
CBTRN03C (Transaction Reporting)  ── standalone, no CALLs
CBACT01C (Account Reader)         ── standalone, no CALLs
CBACT02C (Card Reader)            ── standalone, no CALLs
CBACT03C (XRef Reader)            ── standalone, no CALLs
CBACT04C (Interest Calculation)   ── standalone, no CALLs
CBCUS01C (Customer Reader)        ── standalone, no CALLs
CBEXPORT (Data Export)            ── standalone, no CALLs
CBIMPORT (Data Import)            ── standalone, no CALLs
```

---

## Copybook Dependency Matrix

Shows which copybooks are included (COPY) by each program.

### Core Data Copybooks

| Copybook    | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|-------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COCOM01Y    |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |
| COTTL01Y    |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |
| CSDAT01Y    |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |
| CSMSG01Y    |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |
| CSUSR01Y    |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |    ●     |          |          |          |          |          |    ●     |    ●     |    ●     |    ●     |
| CSMSG02Y    |          |          |          |    ●     |    ●     |          |    ●     |    ●     |          |          |          |          |          |          |          |          |          |
| CVACT01Y    |          |          |          |    ●     |    ●     |          |          |          |          |          |    ●     |          |          |          |          |          |          |
| CVACT02Y    |          |          |          |    ●     |    ●     |    ●     |    ●     |    ●     |          |          |          |          |          |          |          |          |          |
| CVACT03Y    |          |          |          |    ●     |    ●     |          |          |          |          |          |    ●     |          |          |          |          |          |          |
| CVCRD01Y    |          |          |          |          |          |    ●     |    ●     |    ●     |          |          |          |          |          |          |          |          |          |
| CVCUS01Y    |          |          |          |    ●     |    ●     |          |    ●     |    ●     |          |          |          |          |          |          |          |          |          |
| CVTRA05Y    |          |          |          |          |          |          |          |          |    ●     |    ●     |    ●     |    ●     |          |          |          |          |          |
| COMEN02Y    |          |    ●     |          |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| COADM02Y    |          |          |    ●     |          |          |          |          |          |          |          |          |          |          |          |          |          |          |

### Batch Program Copybook Usage

| Copybook    | CBTRN01C | CBTRN02C | CBTRN03C | CBACT01C | CBACT04C | CBSTM03A | CBEXPORT | CBIMPORT |
|-------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y    |          |          |          |    ●     |    ●     |    ●     |    ●     |    ●     |
| CVACT02Y    |          |          |          |          |          |          |    ●     |    ●     |
| CVACT03Y    |          |    ●     |          |          |    ●     |    ●     |    ●     |    ●     |
| CVCUS01Y    |          |          |          |          |          |          |    ●     |    ●     |
| CVTRA01Y    |          |    ●     |          |          |    ●     |          |          |          |
| CVTRA02Y    |          |          |          |          |    ●     |          |          |          |
| CVTRA03Y    |          |          |    ●     |          |          |          |          |          |
| CVTRA04Y    |          |          |    ●     |          |          |          |          |          |
| CVTRA05Y    |          |    ●     |    ●     |          |          |          |    ●     |    ●     |
| CVTRA06Y    |    ●     |    ●     |          |          |          |          |          |          |
| CVTRA07Y    |          |          |    ●     |          |          |          |          |          |
| CVEXPORT    |          |          |          |          |          |          |    ●     |    ●     |
| COSTM01     |          |          |          |          |          |    ●     |          |          |
| CUSTREC     |          |          |          |          |          |    ●     |          |          |
| CSUSR01Y    |          |          |          |          |          |          |          |          |

---

## VSAM File Access Map (Online)

Shows which CICS programs access which VSAM files and the type of access.

| VSAM File (DD)    | COSGN00C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|-------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| USRSEC (Users)    |   R      |          |          |          |          |          |          |          |          |          |  R/B     |    W     |   R/RW   |   R/D    |
| ACCTDATA (Accts)  |          |    R     |   R/RW   |          |          |          |          |          |    R     |          |          |          |          |          |
| CARDDATA (Cards)  |          |          |          |   R/B    |    R     |   R/RW   |          |          |          |          |          |          |          |          |
| CARDXREF (XRef)   |          |    R     |    R     |          |    R     |    R     |          |          |    R     |          |          |          |          |          |
| CUSTDATA (Cust)   |          |    R     |   R/RW   |          |    R     |    R     |          |          |          |          |          |          |          |          |
| TRANSACT (Trans)  |          |          |          |          |          |          |   R/B    |    R     |    W     |          |          |          |          |          |

**Legend**: R = Read, W = Write, RW = Rewrite (Update), D = Delete, B = Browse (STARTBR/READNEXT/READPREV)

---

## File Access Map (Batch)

| File (DD Name)  | CBTRN01C | CBTRN02C | CBTRN03C | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|-----------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| DALYTRAN (In)   |   R      |    R     |          |          |          |          |          |          |          |          |          |          |
| TRANSACT        |   R      |    W     |    R     |          |          |          |          |          |          |          |    R     |    W     |
| ACCTFILE        |   R      |   R/RW   |          |    R     |          |          |   R/RW   |          |          |    R     |    R     |    W     |
| CARDFILE        |   R      |          |          |          |    R     |          |          |          |          |          |    R     |    W     |
| CUSTFILE        |   R      |          |          |          |          |          |          |    R     |          |    R     |    R     |    W     |
| XREFFILE        |   R      |    R     |    R     |          |          |    R     |    R     |          |          |    R     |    R     |    W     |
| TCATBALF        |          |   R/RW/W |          |          |          |          |    R     |          |          |          |          |          |
| DISCGRP         |          |          |          |          |          |          |    R     |          |          |          |          |          |
| TRANTYPE        |          |          |    R     |          |          |          |          |          |          |          |          |          |
| TRANCATG        |          |          |    R     |          |          |          |          |          |          |          |          |          |
| DALYREJS (Out)  |          |    W     |          |          |          |          |          |          |          |          |          |          |
| TRANREPT (Out)  |          |          |    W     |          |          |          |          |          |          |          |          |          |
| STMTFILE (Out)  |          |          |          |          |          |          |          |          |    W     |          |          |          |
| HTMLFILE (Out)  |          |          |          |          |          |          |          |          |    W     |          |          |          |
| TRNXFILE        |          |          |          |          |          |          |          |          |          |    R     |          |          |
| EXPFILE         |          |          |          |          |          |          |          |          |          |          |    W     |    R     |
| DATEPARM        |          |          |    R     |          |          |          |          |          |          |          |          |          |

**Legend**: R = Read (Input), W = Write (Output), RW = Rewrite (Update), R/RW = Read + Update, R/RW/W = Read + Update + Write new records

---

## JCL Job → Program → Dataset Lineage

### Data Loading Jobs

```
DUSRSECJ ──→ IDCAMS/SORT ──→ USRSEC.VSAM.KSDS        ← USRSEC.PS (flat file input)
ACCTFILE ──→ IDCAMS       ──→ ACCTDATA.VSAM.KSDS      ← ACCTDATA.PS
CARDFILE ──→ IDCAMS       ──→ CARDDATA.VSAM.KSDS      ← CARDDATA.PS
CUSTFILE ──→ IDCAMS       ──→ CUSTDATA.VSAM.KSDS      ← CUSTDATA.PS
XREFFILE ──→ IDCAMS       ──→ CARDXREF.VSAM.KSDS+AIX  ← CARDXREF.PS
TRANFILE ──→ IDCAMS       ──→ TRANSACT.VSAM.KSDS      ← TRANSACT.PS
DISCGRP  ──→ IDCAMS       ──→ DISCGRP.VSAM.KSDS       ← DISCGRP.PS
TCATBALF ──→ IDCAMS       ──→ TCATBALF.VSAM.KSDS      ← TCATBALF.PS
TRANCATG ──→ IDCAMS       ──→ TRANCATG (flat)          ← TRANCATG.PS
TRANTYPE ──→ IDCAMS       ──→ TRANTYPE (flat)          ← TRANTYPE.PS
```

### Core Processing Jobs

```
POSTTRAN ──→ CBTRN02C
    Reads:  DALYTRAN, XREFFILE, ACCTFILE, TCATBALF
    Writes: TRANSACT, DALYREJS, ACCTFILE(update), TCATBALF(update/new)

INTCALC  ──→ CBACT04C
    Reads:  TCATBALF, XREFFILE, DISCGRP, ACCTFILE
    Writes: TRANSACT, ACCTFILE(update)

COMBTRAN ──→ SORT + IDCAMS
    Reads:  TRANSACT.BKUP(0), SYSTRAN(0)
    Writes: TRANSACT.COMBINED(+1), TRANSACT.VSAM.KSDS

CREASTMT ──→ SORT + CBSTM03A (→ calls CBSTM03B)
    Reads:  TRANSACT.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA
    Writes: TRXFL.SEQ, TRXFL.VSAM.KSDS, STATEMNT.PS, STATEMNT.HTML

TRANREPT ──→ CBTRN03C
    Reads:  TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
    Writes: TRANREPT (report output)
```

### Backup & Maintenance Jobs

```
TRANBKP  ──→ IDCAMS
    Reads:  TRANSACT.VSAM.KSDS
    Writes: TRANSACT.BKUP(+1)

TRANIDX  ──→ IDCAMS
    Reads:  TRANSACT.VSAM.KSDS
    Creates: TRANSACT.VSAM.AIX + PATH

CLOSEFIL ──→ DFHCSDUP    (Close CICS files for batch window)
OPENFIL  ──→ DFHCSDUP    (Re-open CICS files after batch)
```

### Utility Jobs

```
CBEXPORT (JCL) ──→ CBEXPORT (COBOL)
    Reads:  CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA
    Writes: EXPORT.DATA (single consolidated file)

CBIMPORT (JCL) ──→ CBIMPORT (COBOL)
    Reads:  EXPORT.DATA
    Writes: CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA, IMPORT.ERRORS

TXT2PDF1 ──→ IKJEFT1B (TXT2PDF utility)
    Reads:  STATEMNT.PS
    Writes: STATEMNT.PS.PDF

READACCT ──→ CBACT01C    (diagnostic read of ACCTDATA)
READCARD ──→ CBACT02C    (diagnostic read of CARDDATA)
READCUST ──→ CBCUS01C    (diagnostic read of CUSTDATA)
READXREF ──→ CBACT03C    (diagnostic read of CARDXREF)

FTPJCL   ──→ FTP         (transfer files to/from mainframe)
INTRDRJ1 ──→ IEBGENER    (triggers INTRDRJ2 via internal reader)
INTRDRJ2 ──→ IDCAMS      (copies FTP backup file)
WAITSTEP ──→ COBSWAIT    (→ calls MVSWAIT assembler)
```

---

## Batch Cycle Execution Order

The nightly batch cycle runs in this sequence:

```
Step 1: CLOSEFIL     ─── Close CICS files for exclusive batch access
          │
Step 2: Data Refresh (parallel-eligible)
          ├── ACCTFILE    ─── Refresh account master
          ├── CARDFILE    ─── Refresh card master
          ├── CUSTFILE    ─── Refresh customer master
          ├── XREFFILE    ─── Refresh cross-reference
          ├── TRANFILE    ─── Refresh transaction master
          └── DUSRSECJ    ─── Refresh user security
          │
Step 3: POSTTRAN     ─── Post daily transactions → updates balances
          │                 Reads: DALYTRAN, XREFFILE, ACCTFILE, TCATBALF
          │                 Writes: TRANSACT, DALYREJS, ACCTFILE, TCATBALF
          │
Step 4: INTCALC      ─── Calculate interest on category balances
          │                 Reads: TCATBALF, XREFFILE, DISCGRP, ACCTFILE
          │                 Writes: TRANSACT (interest transactions), ACCTFILE
          │
Step 5: TRANBKP      ─── Backup transaction file (GDG +1)
          │
Step 6: COMBTRAN     ─── Merge current + backup transactions
          │
Step 7: CREASTMT     ─── Generate statements (text + HTML)
          │                 Reads: TRANSACT, CARDXREF, ACCTDATA, CUSTDATA
          │                 Writes: STATEMNT.PS, STATEMNT.HTML
          │
Step 8: TRANIDX      ─── Rebuild transaction alternate indexes
          │
Step 9: OPENFIL      ─── Re-open CICS files for online access
```

---

## End-to-End Data Flow

### Transaction Lifecycle

```
1. DAILY FEED          Daily transactions arrive → DALYTRAN (sequential file)
       │
2. VALIDATION          CBTRN01C reads DALYTRAN, validates against:
       │                 - XREFFILE (card exists?)
       │                 - ACCTFILE (account active?)
       │                 - CARDFILE (card active?)
       │                 - CUSTFILE (customer valid?)
       │
3. POSTING             CBTRN02C posts valid transactions:
       │                 - Writes → TRANSACT (permanent record)
       │                 - Updates → ACCTFILE (balance)
       │                 - Updates → TCATBALF (category balance)
       │                 - Rejects → DALYREJS
       │
4. INTEREST            CBACT04C calculates interest:
       │                 - Reads TCATBALF × DISCGRP → interest rate
       │                 - Creates interest TRANSACT records
       │                 - Updates ACCTFILE balance
       │
5. STATEMENTS          CREASTMT → CBSTM03A generates:
       │                 - Text statements → STATEMNT.PS
       │                 - HTML statements → STATEMNT.HTML
       │
6. REPORTS             TRANREPT → CBTRN03C generates:
       │                 - Daily transaction report with totals
       │
7. ONLINE ACCESS       CICS programs provide real-time access:
                         - View/update accounts, cards, transactions
                         - Add new transactions (COTRN02C)
                         - Submit batch reports (CORPT00C → TDQ)
```

### Data Hub Diagram

```
                    ┌───────────────┐
                    │   DALYTRAN    │  (daily input feed)
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │   CBTRN02C    │  (transaction posting)
                    └───┬───┬───┬───┘
                        │   │   │
          ┌─────────────┘   │   └─────────────┐
          │                 │                 │
   ┌──────▼──────┐  ┌──────▼──────┐  ┌───────▼──────┐
   │  TRANSACT   │  │  ACCTDATA   │  │   TCATBALF   │
   │ (master)    │  │ (accounts)  │  │ (cat balance)│
   └──────┬──────┘  └──────┬──────┘  └───────┬──────┘
          │                │                  │
          │         ┌──────▼──────┐    ┌──────▼──────┐
          │         │  CARDXREF   │    │   DISCGRP   │
          │         │ (xref)      │    │ (int rates) │
          │         └──────┬──────┘    └─────────────┘
          │                │
   ┌──────▼──────┐  ┌──────▼──────┐
   │  CBSTM03A   │  │  CARDDATA   │
   │ (statements)│  │ (cards)     │
   └──────┬──────┘  └──────┬──────┘
          │                │
   ┌──────▼──────┐  ┌──────▼──────┐
   │ STATEMNT.PS │  │  CUSTDATA   │
   │ STATEMNT.HTML│ │ (customers) │
   └─────────────┘  └─────────────┘
```

---

## Cross-Module Dependencies (Optional Modules)

### Authorization Module (IMS/DB2/MQ)

```
COMEN01C (Main Menu)
  └── Option 11 ──XCTL──→ COPAUS0C (Auth Summary View)
                              └── Select ──XCTL──→ COPAUS1C (Auth Detail View)
                                                      └── Mark Fraud ──XCTL──→ COPAUS2C (DB2 Update)

MQ Queue (CDCQ.AUTH.REQ)
  └── Trigger ──→ COPAUA0C (Authorization Decision)
                    ├── Reads: IMS DB (PAUTHS segment)
                    ├── Reads: VSAM files (ACCTDATA, CARDDATA)
                    └── Writes: MQ Response Queue

CBPAUP0C (Batch Purge)
  └── Deletes expired records from IMS PAUTHS database
```

### Transaction Type DB2 Module

```
COADM01C (Admin Menu)
  ├── Option 5 ──XCTL──→ COTRTLIC (Trans Type List - DB2 cursors)
  │                          └── Edit ──XCTL──→ COTRTUPC (Trans Type Update)
  └── Option 6 ──XCTL──→ COTRTUPC (Trans Type Maintenance)

COBTUPDT (Batch Update)
  └── Reads input file → Updates DB2 TRANTYPE table
```

### VSAM-MQ Module

```
MQ Queue (CDRD.DATE.REQ)
  └── CODATE01 ──→ Returns system date via MQ response

MQ Queue (CDRA.ACCT.REQ)
  └── COACCT01 ──→ Reads ACCTDATA VSAM → returns via MQ response
```

---

*End of Dependency Map*
