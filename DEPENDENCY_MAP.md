# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Source**: Static analysis of COBOL XCTL/CALL statements, COPY directives, CICS file operations, and JCL DD statements

---

## 1. Online CICS Call Graph

The online CICS application uses `EXEC CICS XCTL` (transfer control) for inter-program navigation. All programs share state through the `CARDDEMO-COMMAREA` (defined in `COCOM01Y`).

### 1.1 Navigation Flow Diagram

```
                              ┌─────────────┐
                              │  COSGN00C   │ ◄── Entry point (Trans: CC00)
                              │  Sign-on    │
                              └──────┬──────┘
                                     │ XCTL (on successful login)
                       ┌─────────────┼──────────────┐
                       ▼                             ▼
              ┌─────────────┐               ┌─────────────┐
              │  COMEN01C   │               │  COADM01C   │
              │  User Menu  │               │  Admin Menu  │
              │ (Trans CM00)│               │ (Trans CA00) │
              └──────┬──────┘               └──────┬──────┘
                     │ XCTL                        │ XCTL
    ┌────────┬───────┼────────┬──────────┐    ┌────┼─────────┐
    ▼        ▼       ▼        ▼          ▼    ▼    ▼         ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│COACTVWC││COCRDLIC││COTRN00C││COBIL00C││CORPT00C││COUSR00C││COPAUS0C│
│Acct Vw ││Card Lst││Trn Lst ││Bill Pay││Reports ││Usr List││Auth Smr│
│  CAVW   ││  CCLI  ││  CT00  ││  CB00  ││  CR00  ││  CU00  ││(opt)   │
└────┬───┘└───┬────┘└───┬────┘└────────┘└────────┘└───┬────┘└────────┘
     │        │         │                             │
     │   ┌────┴────┐  ┌─┴──────┐              ┌──────┼──────┐
     │   ▼         ▼  ▼        ▼              ▼      ▼      ▼
     │┌────────┐┌────────┐┌────────┐   ┌────────┐┌────────┐┌────────┐
     ││COCRDSLC││COCRDUPC││COTRN01C│   │COUSR01C││COUSR02C││COUSR03C│
     ││Card Dtl││Card Upd││Trn View│   │Usr Add ││Usr Upd ││Usr Del │
     ││  CCDL  ││  CCUP  ││  CT01  │   │  CU01  ││  CU02  ││  CU03  │
     │└────────┘└────────┘└────────┘   └────────┘└────────┘└────────┘
     │
     ▼
┌────────┐
│COACTUPC│
│Acct Upd│
│  CAUP  │
└────────┘
```

### 1.2 Detailed XCTL Relationships

| Source Program | Target Program(s)              | Navigation Trigger                    |
|----------------|--------------------------------|---------------------------------------|
| **COSGN00C**   | COMEN01C                       | Successful login (regular user)       |
| **COSGN00C**   | COADM01C                       | Successful login (admin user)         |
| **COMEN01C**   | COSGN00C                       | PF3 (sign off)                        |
| **COMEN01C**   | COACTVWC, COCRDLIC, COTRN00C, COBIL00C, CORPT00C | Menu option selection |
| **COMEN01C**   | COPAUS0C                       | Auth summary (optional, if installed) |
| **COADM01C**   | COSGN00C                       | PF3 (sign off)                        |
| **COADM01C**   | COUSR00C, COUSR01C, COUSR02C, COUSR03C | Admin menu option selection |
| **COACTVWC**   | COMEN01C                       | PF3 (return to menu)                  |
| **COACTUPC**   | COMEN01C                       | PF3 (return to menu)                  |
| **COCRDLIC**   | COMEN01C                       | PF3 (return to menu)                  |
| **COCRDLIC**   | COCRDSLC                       | Select row (view detail)              |
| **COCRDLIC**   | COCRDUPC                       | Select row (update)                   |
| **COCRDSLC**   | COCRDLIC                       | PF3 (return to list)                  |
| **COCRDSLC**   | COMEN01C                       | PF3 fallback (return to menu)         |
| **COCRDUPC**   | COCRDLIC / COMEN01C            | PF3 (return to caller)                |
| **COTRN00C**   | COTRN01C                       | Select transaction (view)             |
| **COTRN00C**   | COMEN01C                       | PF3 (return to menu)                  |
| **COTRN01C**   | COTRN00C / COMEN01C            | PF3 (return to caller)                |
| **COTRN02C**   | COMEN01C                       | PF3 (return to menu)                  |
| **COBIL00C**   | COMEN01C                       | PF3 (return to menu)                  |
| **CORPT00C**   | COMEN01C                       | PF3 (return to menu)                  |
| **COUSR00C**   | COADM01C                       | PF3 (return to admin menu)            |
| **COUSR00C**   | COUSR02C                       | Select user (update)                  |
| **COUSR00C**   | COUSR03C                       | Select user (delete)                  |
| **COUSR01C**   | COADM01C / COMEN01C            | PF3 (return to caller)                |
| **COUSR02C**   | COADM01C / COUSR00C            | PF3 (return to caller)                |
| **COUSR03C**   | COADM01C / COUSR00C            | PF3 (return to caller)                |

---

## 2. Batch Program Call Graph

Batch programs use `CALL` for subroutine invocations and are orchestrated by JCL job steps.

### 2.1 Batch CALL Relationships

```
CBSTM03A (Statement Generation)
    └── CALL 'CBSTM03B'  (Print subroutine, called 12+ times)

CBACT01C (Account Read/Write)
    └── CALL 'COBDATFT'  (Assembler date formatting)

COBSWAIT (Wait Utility)
    └── CALL 'MVSWAIT'   (Assembler wait routine)

COTRN02C (Transaction Add - Online)
    └── CALL 'CSUTLDTC'  (Date validation, x2)

CORPT00C (Transaction Reports - Online)
    └── CALL 'CSUTLDTC'  (Date validation, x2)

CSUTLDTC (Date Validation Utility)
    └── CALL 'CEEDAYS'   (LE date intrinsic)

CBTRN01C, CBTRN02C, CBTRN03C, CBACT02C, CBACT03C,
CBACT04C, CBCUS01C, CBEXPORT, CBIMPORT
    └── CALL 'CEE3ABD'   (LE abnormal termination)
```

### 2.2 Summary Table

| Caller         | Callee        | Interface           | Purpose                        |
|----------------|---------------|---------------------|--------------------------------|
| CBSTM03A       | CBSTM03B      | WS-M03B-AREA       | Statement line printing        |
| CBACT01C       | COBDATFT      | CODATECN-REC        | Date formatting                |
| COBSWAIT       | MVSWAIT       | MVSWAIT-TIME        | Timed wait                     |
| COTRN02C       | CSUTLDTC      | CSUTLDTC-DATE       | Validate transaction dates     |
| CORPT00C       | CSUTLDTC      | CSUTLDTC-DATE       | Validate report date range     |
| CSUTLDTC       | CEEDAYS       | LE Callable Service | Convert date to Lilian format  |
| Multiple batch | CEE3ABD       | ABCODE, TIMING      | Abnormal termination handler   |

---

## 3. Copybook Dependency Matrix

Shows which programs include which copybooks (via `COPY` statements).

### 3.1 Data Record Copybooks

| Copybook   | Entity             | COSGN | COMEN | COADM | COACT VW | COACT UP | COCRD LI | COCRD SL | COCRD UP | COTRN 00 | COTRN 01 | COTRN 02 | CORPT | COBIL | COUSR 00-03 | CBTRN 01 | CBTRN 02 | CBTRN 03 | CBACT 04 | CBSTM 03A | CBEXP | CBIMP |
|------------|--------------------|----|----|----|----|----|----|----|----|----|----|----|----|----|------|----|----|----|----|----|----|----|
| CVACT01Y   | Account Master     |    |    |    | ✓  | ✓  |    |    |    |    |    | ✓  |    | ✓  |      | ✓  | ✓  |    | ✓  | ✓  | ✓  | ✓  |
| CVACT02Y   | Card Master        |    |    |    | ✓  |    | ✓  | ✓  | ✓  |    |    |    |    |    |      | ✓  |    |    |    |    | ✓  | ✓  |
| CVACT03Y   | Card Xref          |    |    |    | ✓  | ✓  |    |    |    |    |    | ✓  |    | ✓  |      | ✓  | ✓  | ✓  | ✓  | ✓  | ✓  | ✓  |
| CVCUS01Y   | Customer Master    |    |    |    | ✓  | ✓  |    | ✓  | ✓  |    |    |    |    |    |      | ✓  |    |    |    |    | ✓  | ✓  |
| CVTRA05Y   | Transaction        |    |    |    |    |    |    |    |    | ✓  | ✓  | ✓  | ✓  | ✓  |      | ✓  | ✓  | ✓  | ✓  |    | ✓  | ✓  |
| CVTRA06Y   | Daily Transaction  |    |    |    |    |    |    |    |    |    |    |    |    |    |      | ✓  | ✓  |    |    |    |    |    |
| CVTRA01Y   | Tran Cat Balance   |    |    |    |    |    |    |    |    |    |    |    |    |    |      |    |    |    | ✓  |    |    |    |
| CVTRA02Y   | Disclosure Group   |    |    |    |    |    |    |    |    |    |    |    |    |    |      |    |    |    |    |    |    |    |
| CVTRA03Y   | Tran Type          |    |    |    |    |    |    |    |    |    |    |    |    |    |      |    |    | ✓  |    |    |    |    |
| CVTRA04Y   | Tran Category      |    |    |    |    |    |    |    |    |    |    |    |    |    |      |    |    | ✓  |    |    |    |    |
| CVTRA07Y   | Report Layout      |    |    |    |    |    |    |    |    |    |    |    |    |    |      |    |    | ✓  |    |    |    |    |
| CSUSR01Y   | User Security      | ✓  | ✓  | ✓  | ✓  | ✓  | ✓  | ✓  | ✓  |    |    |    |    |    | ✓    |    |    |    |    |    |    |    |
| CVCRD01Y   | Card Work Area     |    |    |    | ✓  | ✓  | ✓  | ✓  | ✓  |    |    |    |    |    |      |    |    |    |    |    |    |    |

### 3.2 Application Control Copybooks

| Copybook   | Purpose               | Used By (count)                                  |
|------------|-----------------------|--------------------------------------------------|
| COCOM01Y   | COMMAREA structure    | All 17 online programs                           |
| COTTL01Y   | Screen titles         | All 17 online programs                           |
| CSDAT01Y   | Date/time working     | All 17 online programs                           |
| CSMSG01Y   | Common messages       | All 17 online programs                           |
| CSMSG02Y   | Abend handling        | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC          |
| CSSETATY   | Set attributes        | COACTUPC (15 replacements)                       |
| CSSTRPFY   | PF-key storage        | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC          |
| CSLKPCDY   | Lookup codes          | COACTUPC                                         |
| CSUTLDWY   | Date validation WS    | COACTUPC                                         |
| COMEN02Y   | Menu definitions      | COMEN01C                                         |
| COADM02Y   | Admin menu defs       | COADM01C                                         |
| CODATECN   | Date conversion rec   | CBACT01C                                         |

---

## 4. VSAM File Access Matrix

Maps which programs read/write which VSAM datasets.

### 4.1 Online CICS Programs → VSAM Files

| Program    | USRSEC | ACCTDAT | CARDDAT | CARDAIX | CUSTDAT | CXACAIX | TRANSACT |
|------------|--------|---------|---------|---------|---------|---------|----------|
| COSGN00C   | R      |         |         |         |         |         |          |
| COACTVWC   |        | R       |         |         | R       | R       |          |
| COACTUPC   |        | R/W     |         |         | R/W     | R       |          |
| COCRDLIC   |        |         | R(br)   | R(br)   |         |         |          |
| COCRDSLC   |        |         | R       |         | R       |         |          |
| COCRDUPC   |        |         | R/W     |         |         |         |          |
| COTRN00C   |        |         |         |         |         |         | R(br)    |
| COTRN01C   |        |         |         |         |         |         | R        |
| COTRN02C   |        |         |         |         |         | R       | R/W      |
| COBIL00C   |        | R/W     |         |         |         | R       | R/W      |
| COUSR00C   | R(br)  |         |         |         |         |         |          |
| COUSR01C   | W      |         |         |         |         |         |          |
| COUSR02C   | R/W    |         |         |         |         |         |          |
| COUSR03C   | R/D    |         |         |         |         |         |          |
| CORPT00C   |        |         |         |         |         |         | (TDQ)    |

**Legend**: R=Read, W=Write, R/W=Read+Rewrite, R/D=Read+Delete, (br)=Browse (STARTBR/READNEXT/READPREV), (TDQ)=Transient Data Queue write

### 4.2 Batch Programs → Files (via JCL DD Statements)

| Program    | JCL Job   | Input Files                              | Output Files                        |
|------------|-----------|------------------------------------------|-------------------------------------|
| CBACT01C   | READACCT  | ACCTFILE (VSAM)                          | OUTFILE, ARRYFILE, VBRCFILE (PS)    |
| CBACT02C   | READCARD  | CARDFILE (VSAM)                          | SYSPRINT (report)                   |
| CBACT03C   | READXREF  | XREFFILE (VSAM)                          | SYSPRINT (report)                   |
| CBACT04C   | INTCALC   | TCATBALF, XREFFILE, ACCTFILE, DISCGRP   | TRANSACT (new interest transactions)|
| CBCUS01C   | READCUST  | CUSTFILE (VSAM)                          | SYSPRINT (report)                   |
| CBTRN02C   | POSTTRAN  | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF   | TRANFILE, DALYREJS                  |
| CBTRN03C   | TRANREPT  | TRANFILE, CARDXREF, TRANTYPE, TRANCATG   | TRANREPT (report PS)                |
| CBSTM03A   | CREASTMT  | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE   | STMTFILE, HTMLFILE                  |
| CBEXPORT   | CBEXPORT  | All VSAM master files                    | Sequential export files             |
| CBIMPORT   | CBIMPORT  | Sequential import files                  | All VSAM master files               |
| COBSWAIT   | WAITSTEP  | SYSIN (wait time parameter)              | None                                |

---

## 5. JCL Job → Program Execution Map

### 5.1 Jobs That Execute COBOL Programs

| JCL Job    | Step     | Program    | Purpose                            |
|------------|----------|------------|------------------------------------|
| READACCT   | STEP05   | CBACT01C   | Read and split account data        |
| READCARD   | STEP05   | CBACT02C   | Print card data                    |
| READXREF   | STEP05   | CBACT03C   | Print cross-reference data         |
| READCUST   | STEP05   | CBCUS01C   | Print customer data                |
| INTCALC    | STEP15   | CBACT04C   | Calculate interest                 |
| POSTTRAN   | STEP15   | CBTRN02C   | Post daily transactions            |
| TRANREPT   | STEP10R  | CBTRN03C   | Generate transaction report        |
| CREASTMT   | STEP040  | CBSTM03A   | Generate customer statements       |
| WAITSTEP   | WAIT     | COBSWAIT   | Controlled wait                    |
| CBEXPORT   | (direct) | CBEXPORT   | Export data to sequential          |
| CBIMPORT   | (direct) | CBIMPORT   | Import data from sequential        |

### 5.2 Jobs Using System Utilities Only

| JCL Job    | Utilities Used                     | Purpose                            |
|------------|------------------------------------|------------------------------------|
| ACCTFILE   | IDCAMS                             | REPRO account data to VSAM         |
| CARDFILE   | IDCAMS                             | REPRO card data to VSAM            |
| CUSTFILE   | IDCAMS                             | REPRO customer data to VSAM        |
| XREFFILE   | IDCAMS                             | Define/load cross-reference VSAM   |
| TRANFILE   | SDSF, IDCAMS                       | Close CICS files, load transactions|
| DUSRSECJ   | IEFBR14, IEBGENER, IDCAMS          | Create and load user security      |
| CLOSEFIL   | SDSF                               | Issue CICS CLOSE commands          |
| OPENFIL    | SDSF                               | Issue CICS OPEN commands           |
| COMBTRAN   | IDCAMS (REPRO)                     | Merge transaction files            |
| TRANBKP    | REPROC, IDCAMS                     | Backup transactions to GDG         |
| TRANIDX    | IDCAMS                             | Define alternate indexes           |
| DEFGDGB    | IDCAMS                             | Define GDG base                    |
| DEFGDGD    | IDCAMS, IEBGENER                   | Define GDG + load ref data         |
| TCATBALF   | IDCAMS                             | Define/load tran cat balance       |
| TRANCATG   | IDCAMS                             | Define/load tran category          |
| TRANTYPE   | IDCAMS                             | Define/load tran type              |
| DISCGRP    | IDCAMS                             | Define/load disclosure group       |
| CREASTMT   | SORT, IDCAMS, IEFBR14 + CBSTM03A  | Sort + statement generation        |
| TRANREPT   | REPROC, SORT + CBTRN03C            | Copy + sort + report               |

---

## 6. Data Lineage — End-to-End Flow

### 6.1 Transaction Lifecycle

```
                    ┌───────────────┐
                    │  ONLINE USER  │
                    │  (3270 Term)  │
                    └───────┬───────┘
                            │ COTRN02C (Transaction Add)
                            ▼
                    ┌───────────────┐
                    │  TRANSACT     │ (VSAM KSDS - Online Transaction Master)
                    │  (CVTRA05Y)   │
                    └───────┬───────┘
                            │
              ┌─────────────┤ Nightly Batch Cycle
              │             │
              ▼             ▼
    ┌───────────────┐  ┌───────────────┐
    │  CLOSEFIL     │  │  DALYTRAN     │ (External daily feed)
    │  (Close CICS) │  │  (CVTRA06Y)   │
    └───────┬───────┘  └───────┬───────┘
            │                  │
            ▼                  ▼
    ┌────────────────────────────────┐
    │  POSTTRAN (JCL) → CBTRN02C    │
    │  Post daily transactions       │
    │  Updates: TRANSACT, ACCTDAT,   │
    │           TCATBALF             │
    │  Rejects → DALYREJS           │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  INTCALC (JCL) → CBACT04C    │
    │  Calculate interest            │
    │  Reads: TCATBALF, XREFFILE,   │
    │         ACCTDAT, DISCGRP       │
    │  Writes: new trans → TRANSACT │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  TRANBKP (JCL)               │
    │  Backup transactions to GDG   │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  COMBTRAN (JCL)              │
    │  Combine daily + master trans │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  CREASTMT (JCL) → CBSTM03A  │
    │  Sort + generate statements   │
    │  Reads: TRANSACT, XREFFILE,   │
    │         ACCTDAT, CUSTDAT       │
    │  Writes: STMTFILE, HTMLFILE   │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  TRANREPT (JCL) → CBTRN03C  │
    │  Sort + generate reports      │
    │  Reads: TRANSACT, XREFFILE,   │
    │         TRANTYPE, TRANCATG    │
    │  Writes: TRANREPT (report)    │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  TRANIDX (JCL)               │
    │  Rebuild alternate indexes    │
    └───────────────┬────────────────┘
                    │
                    ▼
    ┌────────────────────────────────┐
    │  OPENFIL (JCL)               │
    │  Reopen CICS files            │
    └────────────────────────────────┘
```

### 6.2 Bill Payment Data Flow

```
COBIL00C (Bill Payment Screen)
    │
    ├── READ  CXACAIX  → Validate card/account via cross-reference
    ├── READ  ACCTDAT  → Get current account balance
    ├── REWRITE ACCTDAT → Zero out balance (full payment)
    ├── READPREV TRANSACT → Get last transaction ID for sequencing
    └── WRITE TRANSACT → Write new bill payment transaction
```

### 6.3 Account Update Data Flow

```
COACTUPC (Account Update Screen)
    │
    ├── READ  CXACAIX  → Resolve account from card cross-reference
    ├── READ  ACCTDAT  → Load current account data
    ├── READ  CUSTDAT  → Load associated customer data
    ├── REWRITE ACCTDAT → Save account changes
    └── REWRITE CUSTDAT → Save customer changes
```

---

## 7. VSAM Dataset Catalog

Complete list of VSAM datasets referenced across all programs and JCL.

| VSAM Dataset (DD Name) | VSAM Type | Copybook  | Key Field            | Key Len | Rec Len | Programs (Read)                                        | Programs (Write)              |
|-------------------------|-----------|-----------|----------------------|---------|---------|--------------------------------------------------------|-------------------------------|
| ACCTDAT                 | KSDS      | CVACT01Y  | ACCT-ID              | 11      | 300     | COACTVWC, COACTUPC, COBIL00C, CBACT01C, CBACT04C, CBTRN02C, CBSTM03A | COACTUPC, COBIL00C, CBTRN02C |
| CARDDAT                 | KSDS      | CVACT02Y  | CARD-NUM             | 16      | 150     | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C                 | COCRDUPC                      |
| CARDAIX                 | AIX path  | CVACT02Y  | CARD-ACCT-ID         | 11      | 150     | COACTVWC, COCRDLIC                                     | (via base)                    |
| CUSTDAT                 | KSDS      | CVCUS01Y  | CUST-ID              | 9       | 500     | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A | COACTUPC                  |
| CXACFIL / CXACAIX       | KSDS/AIX  | CVACT03Y  | XREF-CARD-NUM        | 16      | 50      | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBSTM03A | —                         |
| TRANSACT                | KSDS      | CVTRA05Y  | TRAN-ID              | 16      | 350     | COTRN00C, COTRN01C, COBIL00C, CBTRN02C, CBTRN03C, CBSTM03A | COTRN02C, COBIL00C, CBACT04C, CBTRN02C |
| DALYTRAN                | SEQ/KSDS  | CVTRA06Y  | DALYTRAN-ID          | 16      | 350     | CBTRN01C, CBTRN02C                                     | (external feed)               |
| USRSEC                  | KSDS      | CSUSR01Y  | SEC-USR-ID           | 8       | 80      | COSGN00C, COUSR00C, COUSR02C, COUSR03C                 | COUSR01C, COUSR02C, COUSR03C |
| TCATBALF                | KSDS      | CVTRA01Y  | Acct+Type+Cat        | 17      | 50      | CBACT04C, CBTRN02C                                     | CBTRN02C, CBACT04C           |
| DISCGRP                 | KSDS      | CVTRA02Y  | Group+Type+Cat       | 16      | 50      | CBACT04C                                               | —                             |
| TRANTYPE                | KSDS      | CVTRA03Y  | TRAN-TYPE            | 2       | 60      | CBTRN03C                                               | —                             |
| TRANCATG                | KSDS      | CVTRA04Y  | Type+Cat             | 6       | 60      | CBTRN03C                                               | —                             |
