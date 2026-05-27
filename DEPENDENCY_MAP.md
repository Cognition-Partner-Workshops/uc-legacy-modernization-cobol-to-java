# CardDemo Dependency Map

> Complete call graph and data lineage showing which programs call which, which copybooks are included where, and which jobs read/write which files.

---

## 1. Program Call Graph

### 1.1 Online (CICS) Call Graph

```
COSGN00C (CC00 – Sign-on)
    │
    ├── [XCTL] → COMEN01C    (if User type = 'U')
    │               │
    │               ├── [XCTL via menu] → COACTVWC  (Option 1: Account View)
    │               ├── [XCTL via menu] → COACTUPC  (Option 2: Account Update)
    │               ├── [XCTL via menu] → COCRDLIC  (Option 3: Credit Card List)
    │               │                       │
    │               │                       ├── [XCTL] → COCRDSLC  (Card Detail View)
    │               │                       └── [XCTL] → COCRDUPC  (Card Update)
    │               │
    │               ├── [XCTL via menu] → COCRDSLC  (Option 4: Credit Card View)
    │               ├── [XCTL via menu] → COCRDUPC  (Option 5: Credit Card Update)
    │               ├── [XCTL via menu] → COTRN00C  (Option 6: Transaction List)
    │               ├── [XCTL via menu] → COTRN01C  (Option 7: Transaction View)
    │               ├── [XCTL via menu] → COTRN02C  (Option 8: Transaction Add)
    │               │                       └── [CALL] → CSUTLDTC  (Date Validation)
    │               │
    │               ├── [XCTL via menu] → CORPT00C  (Option 9: Transaction Reports)
    │               │                       └── [CALL] → CSUTLDTC  (Date Validation)
    │               │
    │               ├── [XCTL via menu] → COBIL00C  (Option 10: Bill Payment)
    │               └── [XCTL via menu] → COPAUS0C  (Option 11: Pending Auth Summary) *
    │
    └── [XCTL] → COADM01C    (if User type = 'A')
                    │
                    ├── [XCTL via menu] → COUSR00C  (Option 1: User List)
                    ├── [XCTL via menu] → COUSR01C  (Option 2: User Add)
                    ├── [XCTL via menu] → COUSR02C  (Option 3: User Update)
                    ├── [XCTL via menu] → COUSR03C  (Option 4: User Delete)
                    ├── [XCTL via menu] → COTRTLIC  (Option 5: Tran Type List) *
                    └── [XCTL via menu] → COTRTUPC  (Option 6: Tran Type Maint) *

    * = Optional extension module
```

**Navigation mechanism:** Menu programs (COMEN01C, COADM01C) use `EXEC CICS XCTL PROGRAM(option-pgm-name)` with the target program name looked up from the COMEN02Y / COADM02Y copybook option tables. All CICS programs receive/return CARDDEMO-COMMAREA (COCOM01Y) for state transfer. Back-navigation uses CDEMO-TO-PROGRAM set to the caller's name.

### 1.2 Batch Call Graph

```
CBACT01C  (Read Account File)
    └── [CALL] → COBDATFT   (ASM: Date format conversion)

CBACT04C  (Interest Calculator)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

CBTRN02C  (Post Daily Transactions)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

CBTRN03C  (Transaction Detail Report)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

CBSTM03A  (Account Statements)
    ├── [CALL] → CBSTM03B   (Statement formatting subroutine – called 12 times)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

COBSWAIT  (Wait Utility)
    └── [CALL] → MVSWAIT    (ASM: Timer wait)

CSUTLDTC  (Date Validation Utility)
    └── [CALL] → CEEDAYS    (LE: Julian date conversion)

CBEXPORT  (Branch Migration Export)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

CBIMPORT  (Branch Migration Import)
    └── [CALL] → CEE3ABD    (LE: Abend routine)

CBACT02C, CBACT03C, CBCUS01C, CBTRN01C  (Read/Print programs)
    └── [CALL] → CEE3ABD    (LE: Abend routine)
```

### 1.3 External Service Dependencies

| Program | Technology | Service | Direction |
|:--------|:-----------|:--------|:----------|
| COPAUA0C | IBM MQ | Authorization Request/Response Queue | Send & Receive |
| COPAUS0C | IMS DB | Pending Authorization Summary Segment | Read |
| COPAUS1C | IMS DB + DB2 | Pending Auth Detail (IMS) + Fraud Log (DB2) | Read + Insert |
| COPAUS2C | DB2 | Fraud Flag Table | Update |
| COTRTLIC | DB2 | Transaction Type Table (TRAN_TYPE_TBL) | SELECT, DELETE |
| COTRTUPC | DB2 | Transaction Type Table (TRAN_TYPE_TBL) | INSERT, UPDATE |
| COBTUPDT | DB2 | Transaction Type Table (TRAN_TYPE_TBL) | UPDATE (batch) |
| COACCT01 | IBM MQ | Account Inquiry Queue | Send & Receive |
| CODATE01 | IBM MQ | Date Inquiry Queue | Send & Receive |

---

## 2. Copybook Inclusion Map

### Who includes what — sorted by copybook frequency

| Copybook | Included By (programs) | Count |
|:---------|:----------------------|------:|
| COCOM01Y | COADM01C, COMEN01C, COSGN00C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| COTTL01Y | Same 17 online programs | 17 |
| CSDAT01Y | Same 17 online programs | 17 |
| CSMSG01Y | Same 17 online programs | 17 |
| DFHAID | Same 17 online programs | 17 |
| DFHBMSCA | Same 17 online programs | 17 |
| CSUSR01Y | COSGN00C, COADM01C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 12 |
| CVACT01Y | CBACT01C, COACTVWC, COACTUPC, CBACT04C, CBTRN02C, CBTRN01C, COTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COBIL00C | 11 |
| CVACT03Y | COACTVWC, COACTUPC, CBACT03C, CBACT04C, CBTRN02C, CBTRN03C, COTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COBIL00C | 11 |
| CVTRA05Y | CORPT00C, COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBACT04C, CBTRN03C, CBTRN02C, CBTRN01C, CBEXPORT, CBIMPORT | 11 |
| CVACT02Y | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C | 8 |
| CVCUS01Y | COACTVWC, COCRDSLC, COCRDUPC, COACTUPC, CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C | 8 |
| CVCRD01Y | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC | 5 |
| CSSTRPFY | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC | 5 |
| CSMSG02Y | COACTVWC, COCRDSLC, COCRDUPC, COACTUPC | 4 |
| CSSETATY | COACTUPC (×35 via COPY REPLACING) | 1 (×35) |
| CVTRA06Y | CBTRN01C, CBTRN02C | 2 |
| CVTRA01Y | CBACT04C, CBTRN02C | 2 |
| CVTRA02Y | CBACT04C | 1 |
| CVTRA03Y | CBTRN03C | 1 |
| CVTRA04Y | CBTRN03C | 1 |
| CVTRA07Y | CBTRN03C | 1 |
| CVEXPORT | CBEXPORT, CBIMPORT | 2 |
| CUSTREC | CBSTM03A | 1 |
| COSTM01 | CBSTM03A | 1 |
| CODATECN | CBACT01C | 1 |
| COADM02Y | COADM01C | 1 |
| COMEN02Y | COMEN01C | 1 |
| CSLKPCDY | COACTUPC | 1 |
| CSUTLDPY | COACTUPC | 1 |
| CSUTLDWY | COACTUPC | 1 |

---

## 3. Data Lineage — VSAM File Access by Program

### 3.1 CICS Programs — File Access

| Program | USRSEC | ACCTDAT | CARDDAT | CUSTDAT | CARDXREF | TRANSACT | TCATBALF | DISCGRP | TRANCATG | TRANTYPE |
|:--------|:------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|:-------:|:--------:|:--------:|
| COSGN00C | R | | | | | | | | | |
| COACTVWC | | R | R | R | R | | | | | |
| COACTUPC | | RW | | RW | R | | | | | |
| COCRDLIC | | | R | | | | | | | |
| COCRDSLC | | | R | R | | | | | | |
| COCRDUPC | | | RW | R | | | | | | |
| COTRN00C | | | | | | R | | | | |
| COTRN01C | | | | | | R | | | | |
| COTRN02C | | R | | | R | RW | | | | |
| COBIL00C | | RW | | | R | RW | | | | |
| COUSR00C | R | | | | | | | | | |
| COUSR01C | W | | | | | | | | | |
| COUSR02C | RW | | | | | | | | | |
| COUSR03C | RD | | | | | | | | | |
| CORPT00C | | | | | | | | | | |

**Legend:** R = Read, W = Write, RW = Read+Rewrite, RD = Read+Delete

### 3.2 Batch Programs — File Access

| Program | Input Files | Output Files | Operation |
|:--------|:-----------|:-------------|:----------|
| CBACT01C | ACCTFILE (Account VSAM) | ACCTOUT (formatted output) | Read → Format → Write |
| CBACT02C | CARDFILE (Card VSAM) | SYSOUT (print) | Read → Print |
| CBACT03C | XREFFILE (Xref VSAM) | SYSOUT (print) | Read → Print |
| CBACT04C | ACCTFILE, TCATBALF, DISCGRP, TRANSACT | ACCTFILE (updated balances) | Read all → Calculate interest → Rewrite |
| CBCUS01C | CUSTFILE (Customer VSAM) | SYSOUT (print) | Read → Print |
| CBTRN01C | DALYTRAN, XREFFILE, CARDFILE, ACCTFILE | TRANSACT | Read daily → Validate → Post |
| CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE, TRANSACT, TCATBALF | ACCTFILE, TRANSACT, TCATBALF (updated) | Read daily → Post → Update balances |
| CBTRN03C | TRANSACT, XREFFILE, TRANTYPE, TRANCATG | TRANREPT (report file) | Read → Join types → Print report |
| CBSTM03A | XREFFILE, CUSTFILE, ACCTFILE, TRANSACT | STMTFILE (text+HTML statements) | Read all → Format → Write |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE (multi-record export) | Read all → Merge → Write |
| CBIMPORT | EXPFILE (multi-record import) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT | Read → Split → Validate → Write |

### 3.3 JCL Job — Dataset Lineage

```
                    ┌──────────────┐
                    │  EBCDIC Data │  (app/data/EBCDIC/)
                    │  (seed files)│
                    └──────┬───────┘
                           │ IDCAMS REPRO
                           ▼
    ┌───────────────────────────────────────────────────────────┐
    │                    VSAM KSDS Files                         │
    │                                                           │
    │  ACCTFILE.jcl  →  AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS    │
    │  CARDFILE.jcl  →  AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS    │
    │  CUSTFILE.jcl  →  AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS    │
    │  XREFFILE.jcl  →  AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS    │
    │  TRANFILE.jcl  →  AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS    │
    │  DUSRSECJ.jcl  →  AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS     │
    │  DISCGRP.jcl   →  AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS    │
    │  TRANCATG.jcl  →  AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS   │
    │  TRANTYPE.jcl  →  AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS   │
    │  TCATBALF.jcl  →  AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS   │
    └───────────────────────┬───────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
     ┌────────────┐ ┌────────────┐ ┌────────────┐
     │ CICS Online│ │   Batch    │ │  Reporting │
     │  Programs  │ │ Processing │ │   Output   │
     └────────────┘ └──────┬─────┘ └────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
     ┌──────────────┐ ┌─────────┐ ┌──────────────┐
     │ POSTTRAN job  │ │ INTCALC │ │ CREASTMT job │
     │ (CBTRN02C)   │ │(CBACT04C)│ │ (CBSTM03A)  │
     │              │ │          │ │              │
     │ Reads:       │ │ Reads:   │ │ Reads:       │
     │  DALYTRAN.PS │ │  ACCTDATA│ │  XREFFILE    │
     │  XREFFILE    │ │  TCATBALF│ │  CUSTFILE    │
     │  ACCTDATA    │ │  DISCGRP │ │  ACCTFILE    │
     │              │ │  TRANSACT│ │  TRANSACT    │
     │ Writes:      │ │          │ │              │
     │  TRANSACT    │ │ Writes:  │ │ Writes:      │
     │  ACCTDATA    │ │  ACCTDATA│ │  STMTFILE    │
     │  TCATBALF    │ │          │ │  (text+HTML) │
     └──────────────┘ └─────────┘ └──────────────┘
```

---

## 4. Scheduled Job Chains

### 4.1 Daily Transaction Backup (Control-M)

```
CLOSEFIL ──→ TRANBKP ──→ WAITSTEP ──→ OPENFIL
   │            │            │            │
   │            │            │            └─ Open VSAM files for CICS
   │            │            └─ Timer delay (COBSWAIT/MVSWAIT)
   │            └─ REPRO + DELETE transaction master
   └─ Close CICS-opened VSAM files
```

**Dependency:** Each job produces an OUTCOND consumed as INCOND by the next.

### 4.2 Weekly Transaction Types DB Refresh (Control-M)

```
MNTTRDB2 ──→ TRANEXTR
   │            │
   │            └─ Extract latest DB2 data → VSAM sequential files
   └─ Batch update transaction type table in DB2 (COBTUPDT)
```

### 4.3 Full Batch Processing Sequence (from README)

```
CLOSEFIL → ACCTFILE → CARDFILE → XREFFILE → CUSTFILE → TRANBKP
    → TRANEXTR* → TRANCATG → TRANTYPE → DISCGRP → TCATBALF
    → DUSRSECJ → POSTTRAN → INTCALC → TRANBKP → COMBTRAN
    → CREASTMT → TRANIDX → OPENFIL → WAITSTEP → CBPAUP0J*

    * = Optional extension module
```

---

## 5. Cross-Reference: CICS Transaction → Program → BMS Map → Copybooks

| Transaction | Program | BMS Map | Key Copybooks |
|:-----------|:--------|:--------|:--------------|
| CC00 | COSGN00C | COSGN00 | COCOM01Y, CSUSR01Y, CSMSG01Y |
| CM00 | COMEN01C | COMEN01 | COCOM01Y, COMEN02Y, CSUSR01Y |
| CA00 | COADM01C | COADM01 | COCOM01Y, COADM02Y, CSUSR01Y |
| CAVW | COACTVWC | COACTVW | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y |
| CAUP | COACTUPC | COACTUP | COCOM01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, CSSETATY |
| CCLI | COCRDLIC | COCRDLI | COCOM01Y, CVCRD01Y, CVACT02Y |
| CCDL | COCRDSLC | COCRDSL | COCOM01Y, CVCRD01Y, CVACT02Y, CVCUS01Y |
| CCUP | COCRDUPC | COCRDUP | COCOM01Y, CVCRD01Y, CVACT02Y, CVCUS01Y |
| CT00 | COTRN00C | COTRN00 | COCOM01Y, CVTRA05Y |
| CT01 | COTRN01C | COTRN01 | COCOM01Y, CVTRA05Y |
| CT02 | COTRN02C | COTRN02 | COCOM01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| CR00 | CORPT00C | CORPT00 | COCOM01Y, CVTRA05Y |
| CB00 | COBIL00C | COBIL00 | COCOM01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| CU00 | COUSR00C | COUSR00 | COCOM01Y, CSUSR01Y |
| CU01 | COUSR01C | COUSR01 | COCOM01Y, CSUSR01Y |
| CU02 | COUSR02C | COUSR02 | COCOM01Y, CSUSR01Y |
| CU03 | COUSR03C | COUSR03 | COCOM01Y, CSUSR01Y |

---

## 6. Technology Integration Points

| Integration | Programs Involved | Data Flow |
|:-----------|:-----------------|:----------|
| VSAM KSDS | All 17 core online + 14 core batch | Primary data persistence |
| VSAM AIX | TRANIDX job | Alternate index on transaction file for card-based queries |
| VSAM ESDS/RRDS | ESDSRRDS job | Additional dataset types for testing |
| DB2 | COTRTLIC, COTRTUPC, COBTUPDT, COPAUS1C, COPAUS2C | Transaction type management, fraud logging |
| IMS DB | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C | Pending authorization segments |
| IBM MQ | COPAUA0C, COACCT01, CODATE01 | Asynchronous auth requests, account/date inquiries |
| SORT | COMBTRAN, TRANREPT, PRTCATBL jobs | Combine and sort transaction data |
| GDG | DEFGDGB, DEFGDGD, DALYREJS, REPTFILE jobs | Versioned backup generations |
| LE Runtime | CSUTLDTC (CEEDAYS), multiple (CEE3ABD) | Date validation, abend handling |
| Assembler | COBDATFT (date fmt), MVSWAIT (timer) | Low-level system services |
