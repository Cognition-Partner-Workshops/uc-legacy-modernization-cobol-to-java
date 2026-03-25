# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Scope:** Program call graph, copybook inclusion graph, VSAM data lineage, JCL job chains

---

## 1. Online Program Call Graph (CICS XCTL)

All online navigation uses `EXEC CICS XCTL` (transfer control) passing the `CARDDEMO-COMMAREA`. The target program is set dynamically in `CDEMO-TO-PROGRAM` based on menu selection or screen navigation.

```
                          ┌─────────────┐
                          │  COSGN00C   │  (Sign-on)
                          │  Trans: CC00│
                          └──────┬──────┘
                       ┌─────────┴─────────┐
                  Admin user           Regular user
                       │                    │
                ┌──────┴──────┐     ┌───────┴──────┐
                │  COADM01C   │     │  COMEN01C    │
                │  Admin Menu │     │  Main Menu   │
                └──────┬──────┘     └───────┬──────┘
                       │                    │
         ┌─────┬─────┬┴────┐    ┌──┬──┬──┬─┴┬──┬──┬──┬──┬──┬──┐
         │     │     │     │    │  │  │  │  │  │  │  │  │  │  │
         ▼     ▼     ▼     ▼    ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼
     COUSR  COUSR  COUSR  COUSR                              COPAUS0C
     00C    01C    02C    03C    │  │  │  │  │  │  │  │  │  (optional)
     List   Add    Upd    Del    │  │  │  │  │  │  │  │  │
                                 │  │  │  │  │  │  │  │  │
                        COACTVWC─┘  │  │  │  │  │  │  │  │
                        COACTUPC────┘  │  │  │  │  │  │  │
                        COCRDLIC───────┘  │  │  │  │  │  │
                        COCRDSLC──────────┘  │  │  │  │  │
                        COCRDUPC─────────────┘  │  │  │  │
                        COTRN00C────────────────┘  │  │  │
                        COTRN01C───────────────────┘  │  │
                        COTRN02C──────────────────────┘  │
                        CORPT00C─────────────────────────┘
                        COBIL00C (Bill Payment, option 10)
```

### Detailed XCTL Transfers

| Source Program | Target Program       | Trigger                               |
|---------------|----------------------|---------------------------------------|
| COSGN00C      | COADM01C             | Admin user login success              |
| COSGN00C      | COMEN01C             | Regular user login success            |
| COMEN01C      | *(dynamic via menu)* | Menu option selection (options 1–11)  |
| COADM01C      | *(dynamic via menu)* | Admin menu option selection (1–6)     |
| COACTVWC      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COACTUPC      | CDEMO-TO-PROGRAM     | PF3 or update complete                |
| COCRDLIC      | COMEN01C             | PF3 (return to menu)                  |
| COCRDLIC      | COCRDSLC             | Select card for view                  |
| COCRDLIC      | COCRDUPC             | Select card for update                |
| COCRDSLC      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COCRDUPC      | CDEMO-TO-PROGRAM     | PF3 or update complete                |
| COTRN00C      | CDEMO-TO-PROGRAM     | PF3 or drill into transaction         |
| COTRN01C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COTRN02C      | CDEMO-TO-PROGRAM     | PF3 or add complete                   |
| CORPT00C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COBIL00C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COUSR00C      | CDEMO-TO-PROGRAM     | PF3 (return to admin menu)            |
| COUSR01C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COUSR02C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |
| COUSR03C      | CDEMO-TO-PROGRAM     | PF3 (return to caller)                |

### Menu Option → Program Mapping

**Main Menu (COMEN01C)** — from `COMEN02Y.cpy`:

| Option | Description               | Target Program |
|-------:|--------------------------|----------------|
|      1 | Account View              | COACTVWC       |
|      2 | Account Update            | COACTUPC       |
|      3 | Credit Card List          | COCRDLIC       |
|      4 | Credit Card View          | COCRDSLC       |
|      5 | Credit Card Update        | COCRDUPC       |
|      6 | Transaction List          | COTRN00C       |
|      7 | Transaction View          | COTRN01C       |
|      8 | Transaction Add           | COTRN02C       |
|      9 | Transaction Reports       | CORPT00C       |
|     10 | Bill Payment              | COBIL00C       |
|     11 | Pending Auth View         | COPAUS0C       |

**Admin Menu (COADM01C)** — from `COADM02Y.cpy`:

| Option | Description                        | Target Program |
|-------:|------------------------------------|----------------|
|      1 | User List (Security)               | COUSR00C       |
|      2 | User Add (Security)                | COUSR01C       |
|      3 | User Update (Security)             | COUSR02C       |
|      4 | User Delete (Security)             | COUSR03C       |
|      5 | Transaction Type List/Update (DB2) | COTRTLIC       |
|      6 | Transaction Type Maintenance (DB2) | COTRTUPC       |

---

## 2. Batch Program Call Graph (CALL statements)

```
CBSTM03A ──CALL──► CBSTM03B     (statement file I/O subroutine)
CBACT01C ──CALL──► COBDATFT     (assembler date formatting)
COTRN02C ──CALL──► CSUTLDTC     (date validation)
CORPT00C ──CALL──► CSUTLDTC     (date validation)
COBSWAIT ──CALL──► MVSWAIT      (assembler MVS wait)
CSUTLDTC ──CALL──► CEEDAYS      (LE date intrinsic)

All batch programs call CEE3ABD for abnormal termination:
  CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C,
  CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A,
  CBEXPORT, CBIMPORT
```

| Caller     | Callee     | Mechanism    | Purpose                       |
|------------|------------|--------------|-------------------------------|
| CBSTM03A   | CBSTM03B   | CALL USING   | File open/read/write/close    |
| CBACT01C   | COBDATFT   | CALL USING   | Format date for display       |
| COTRN02C   | CSUTLDTC   | CALL USING   | Validate transaction dates    |
| CORPT00C   | CSUTLDTC   | CALL USING   | Validate report date range    |
| COBSWAIT   | MVSWAIT    | CALL USING   | MVS wait (centiseconds)       |
| CSUTLDTC   | CEEDAYS    | CALL USING   | LE date conversion API        |
| *(11 pgms)*| CEE3ABD    | CALL USING   | Abnormal program termination  |

---

## 3. Copybook Inclusion Matrix

Each cell shows which copybooks are COPY'd by which program.

### Online Programs

| Copybook   | COSGN | COMEN | COADM | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT 00 | COBIL 00 | COUSR 00 | COUSR 01 | COUSR 02 | COUSR 03 |
|------------|:-----:|:-----:|:-----:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COCOM01Y   |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| COTTL01Y   |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| CSDAT01Y   |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| CSMSG01Y   |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| DFHAID     |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| DFHBMSCA   |   ✓   |   ✓   |   ✓   |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |    ✓     |
| CSUSR01Y   |   ✓   |       |       |          |          |          |          |          |          |          |          |          |          |    ✓     |    ✓     |    ✓     |    ✓     |
| COMEN02Y   |       |   ✓   |       |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| COADM02Y   |       |       |   ✓   |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| CVACT01Y   |       |       |       |    ✓     |    ✓     |          |          |          |          |          |    ✓     |          |    ✓     |          |          |          |          |
| CVACT03Y   |       |       |       |    ✓     |    ✓     |          |          |          |          |          |    ✓     |          |    ✓     |          |          |          |          |
| CVTRA05Y   |       |       |       |          |          |          |          |          |    ✓     |    ✓     |    ✓     |          |    ✓     |          |          |          |          |
| CVCRD01Y   |       |       |       |          |    ✓     |          |          |          |          |          |          |          |          |          |          |          |          |
| CSLKPCDY   |       |       |       |          |    ✓     |          |          |          |          |          |          |          |          |          |          |          |          |
| CSUTLDWY   |       |       |       |          |    ✓     |          |          |          |          |          |          |          |          |          |          |          |          |
| CSUTLDPY   |       |       |       |          |          |          |          |          |          |          |    ✓     |    ✓     |          |          |          |          |          |

### Batch Programs

| Copybook   | CBACT01 | CBACT02 | CBACT03 | CBACT04 | CBCUS01 | CBTRN01 | CBTRN02 | CBTRN03 | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|------------|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y   |         |         |         |    ✓    |         |    ✓    |    ✓    |         |          |          |    ✓     |    ✓     |
| CVACT02Y   |         |         |         |         |         |    ✓    |         |         |          |          |    ✓     |    ✓     |
| CVACT03Y   |         |    ✓    |    ✓    |         |         |    ✓    |    ✓    |         |          |          |    ✓     |    ✓     |
| CVCUS01Y   |         |         |         |         |         |    ✓    |         |         |          |          |    ✓     |    ✓     |
| CVTRA05Y   |         |         |         |         |         |    ✓    |    ✓    |         |          |          |    ✓     |    ✓     |
| CVTRA06Y   |         |         |         |         |         |    ✓    |    ✓    |    ✓    |          |          |          |          |
| CVTRA01Y   |         |         |         |         |         |         |    ✓    |         |          |          |          |          |
| CVEXPORT   |         |         |         |         |         |         |         |         |          |          |    ✓     |    ✓     |
| COSTM01    |         |         |         |         |         |         |         |         |    ✓     |          |          |          |
| CODATECN   |    ✓    |         |         |         |         |         |         |         |          |          |          |          |

---

## 4. VSAM File Data Lineage

This section shows which programs **read (R)**, **write (W)**, **update (U)**, or **browse (B)** each VSAM file, and which JCL jobs **define (D)**, **load (L)**, or **delete (X)** them.

### Online Program → VSAM File Access

| VSAM File   | COSGN | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT 00 | COBIL 00 | COUSR 00-03 |
|-------------|:-----:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-----------:|
| USRSEC      |  R    |          |          |          |          |          |          |          |          |          |          |   R/W/U/D   |
| ACCTDAT     |       |    R     |   R/U    |          |          |          |          |          |          |          |   R/U    |             |
| CARDDAT     |       |          |          |   R/B    |    R     |   R/U    |          |          |          |          |          |             |
| CARDXREF    |       |    R     |    R     |          |          |          |          |          |    R     |          |   R      |             |
| CXACAIX     |       |          |          |          |          |          |          |          |    R     |          |   R      |             |
| CUSTDAT     |       |    R     |   R/U    |          |          |          |          |          |          |          |          |             |
| TRANSACT    |       |          |          |          |          |          |   R/B    |    R     |   R/W    |          |   R/W    |             |

### Batch Program → File Access

| File / Dataset       | CBACT01 | CBACT02 | CBACT03 | CBACT04 | CBCUS01 | CBTRN01 | CBTRN02 | CBTRN03 | CBSTM03A/B | CBEXPORT | CBIMPORT |
|----------------------|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|:----------:|:--------:|:--------:|
| ACCTDAT (seq)        |    R    |         |         |   R/U   |         |         |    R    |         |     R      |    R     |    W     |
| CARDDAT (seq)        |         |    R    |         |         |         |         |         |         |            |    R     |    W     |
| CARDXREF (seq)       |         |         |    R    |         |         |    R    |    R    |         |            |    R     |    W     |
| CUSTDAT (seq)        |         |         |         |         |    R    |    R    |         |         |            |    R     |    W     |
| TRANSACT (seq)       |         |         |         |         |         |         |    R/W  |    R    |     R      |    R     |    W     |
| DALYTRAN (seq)       |         |         |         |         |         |    R    |    R    |    R    |            |          |          |
| TCATBAL (seq)        |         |         |         |         |         |         |    R/W  |         |            |          |          |
| DISCGRP (seq)        |         |         |         |    R    |         |         |         |         |            |          |          |
| TRANTYPE (seq)       |         |         |         |         |         |         |         |    R    |            |          |          |
| TRANCATG (seq)       |         |         |         |         |         |         |         |    R    |            |          |          |
| DALYREJS (seq)       |         |         |         |         |         |         |    W    |         |            |          |          |
| Statement output     |         |         |         |         |         |         |         |         |     W      |          |          |
| Report output        |         |         |         |         |         |         |         |    W    |            |          |          |
| Export file          |         |         |         |         |         |         |         |         |            |    W     |    R     |
| Date-fmt output      |    W    |         |         |         |         |         |         |         |            |          |          |

### JCL Job → VSAM File Operations

| JCL Job    | VSAM File(s) Affected                     | Operation          |
|------------|-------------------------------------------|--------------------|
| ACCTFILE   | ACCTDAT                                   | Delete/Define/Load |
| CARDFILE   | CARDDAT                                   | Delete/Define/Load |
| CUSTFILE   | CUSTDAT                                   | Delete/Define/Load |
| XREFFILE   | CARDXREF, CARDXREF AIX                    | Delete/Define/Load |
| TRANFILE   | TRANSACT, TRANSACT AIX, CXACAIX           | Delete/Define/Load |
| TRANTYPE   | TRANTYPE                                  | Delete/Define/Load |
| TRANCATG   | TRANCATG                                  | Delete/Define/Load |
| TCATBALF   | TCATBAL                                   | Delete/Define/Load |
| DISCGRP    | DISCGRP                                   | Delete/Define/Load |
| DALYREJS   | DALYREJS                                  | Delete/Define      |
| REPTFILE   | REPTFILE                                  | Delete/Define      |
| DUSRSECJ   | USRSEC                                    | Load from seq data |
| DEFCUST    | CUSTDAT                                   | Define cluster     |
| DEFGDGB    | GDG base (backup)                         | Define GDG         |
| DEFGDGD    | GDG base (daily)                          | Define GDG         |
| TRANIDX    | TRANSACT AIX                              | Define AIX/Path    |
| TRANBKP    | TRANSACT                                  | Backup (REPRO)     |
| COMBTRAN   | TRANSACT                                  | Sort/Merge         |
| ESDSRRDS   | ESDS/RRDS clusters                        | Define             |

---

## 5. Batch Job Execution Chain

The nightly batch cycle executes in this order (from scheduler configs and script analysis):

```
CLOSEFIL ──► Close VSAM files in CICS region
    │
    ▼
Data Refresh (parallel):
  ├── ACCTFILE  (Account master refresh)
  ├── CARDFILE  (Card master refresh)
  ├── CUSTFILE  (Customer master refresh)
  ├── XREFFILE  (Cross-reference refresh)
  └── TRANFILE  (Transaction master refresh)
    │
    ▼
POSTTRAN ──► Post daily transactions (CBTRN02C)
    │          Reads: DALYTRAN, CARDXREF, ACCTDAT, TRANSACT, TCATBAL
    │          Writes: TRANSACT, ACCTDAT, TCATBAL, DALYREJS
    ▼
INTCALC  ──► Calculate interest (CBACT04C)
    │          Reads: ACCTDAT, DISCGRP
    │          Writes: ACCTDAT
    ▼
TRANBKP  ──► Backup transaction file (GDG rollover)
    │          Reads: TRANSACT
    │          Writes: GDG generation
    ▼
COMBTRAN ──► Combine/sort transactions
    │          Reads: TRANSACT
    │          Writes: Sorted sequential file
    ▼
CREASTMT ──► Create statements (CBSTM03A → CBSTM03B)
    │          Reads: Sorted transactions, ACCTDAT
    │          Writes: Statement text + HTML files
    ▼
TRANREPT ──► Transaction report (CBTRN03C)
    │          Reads: TRANSACT, TRANTYPE, TRANCATG
    │          Writes: Report output
    ▼
TRANIDX  ──► Rebuild alternate index
    │          Rebuilds: TRANSACT AIX
    ▼
OPENFIL  ──► Reopen VSAM files in CICS region
```

---

## 6. Cross-Domain Data Flow

This shows how data flows between business domains through shared VSAM files:

```
┌─────────────────────────────────────────────────────────────────┐
│                    ONLINE (CICS) DOMAIN                         │
│                                                                 │
│  Sign-on ──► USRSEC                                             │
│                                                                 │
│  Account View/Update ──► ACCTDAT, CUSTDAT, CARDXREF             │
│  Card List/View/Update ──► CARDDAT                              │
│  Transaction List/View ──► TRANSACT                             │
│  Transaction Add ──► TRANSACT, CARDXREF, CXACAIX                │
│  Bill Payment ──► ACCTDAT, TRANSACT, CXACAIX                    │
│  Report ──► (parameters only; batch does actual reports)        │
│                                                                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Shared VSAM files
┌──────────────────────────┴──────────────────────────────────────┐
│                    BATCH (JCL) DOMAIN                            │
│                                                                 │
│  POSTTRAN: DALYTRAN → TRANSACT + ACCTDAT + TCATBAL              │
│  INTCALC:  ACCTDAT + DISCGRP → ACCTDAT (interest applied)      │
│  CREASTMT: TRANSACT + ACCTDAT → Statement files                 │
│  TRANREPT: TRANSACT + TRANTYPE + TRANCATG → Report files        │
│  CBEXPORT: All VSAM → Sequential export files                  │
│  CBIMPORT: Sequential files → All VSAM                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Shared Copybook Dependency Summary

| Copybook   | Used By (count) | Category           | Modernization Impact     |
|------------|:---------------:|--------------------|--------------------------|
| COCOM01Y   |       17        | COMMAREA           | Maps to session/DTO      |
| COTTL01Y   |       17        | Screen header       | UI component             |
| CSDAT01Y   |       17        | Date fields         | Java LocalDate           |
| CSMSG01Y   |       17        | Message area        | UI feedback / exceptions |
| DFHAID     |       17        | CICS AID keys       | Eliminated (REST)        |
| DFHBMSCA   |       17        | BMS attributes      | Eliminated (CSS)         |
| CVTRA05Y   |        8        | Transaction record  | JPA Entity               |
| CVACT01Y   |        7        | Account record      | JPA Entity               |
| CVACT03Y   |        7        | Card xref record    | JPA Entity / FK          |
| CSUSR01Y   |        5        | User security       | Spring Security UserDetails |
| CVCUS01Y   |        4        | Customer record     | JPA Entity               |
| CVACT02Y   |        3        | Card record         | JPA Entity               |
| CVTRA06Y   |        3        | Daily transaction   | JPA Entity / Staging     |
| CVEXPORT   |        2        | Export layouts       | File I/O service         |
| CSLKPCDY   |        1        | Lookup codes         | DB reference tables      |
| CVCRD01Y   |        1        | Card detail (join)  | DTO / View Model         |
