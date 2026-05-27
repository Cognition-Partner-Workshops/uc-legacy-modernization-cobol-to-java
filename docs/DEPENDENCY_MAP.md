# Dependency Map — CardDemo Mainframe System

> **Generated**: 2026-05-27 | **Source**: Static analysis of CALL, XCTL, COPY, and file I/O statements

---

## 1. Program Call Graph

### 1.1 Online (CICS) Call Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        CICS TRANSACTION ENTRY POINTS                         │
└─────────────────────────────────────────────────────────────────────────────┘

CC00 ─► COSGN00C (Sign-on)
         ├── XCTL ──► COADM01C (if Admin user)
         └── XCTL ──► COMEN01C (if Regular user)

CM00 ─► COMEN01C (Main Menu)
         ├── XCTL ──► COACTVWC (Option: Account View)
         ├── XCTL ──► COACTUPC (Option: Account Update)
         ├── XCTL ──► COCRDLIC (Option: Card List)
         ├── XCTL ──► COTRN00C (Option: Transaction List)
         ├── XCTL ──► CORPT00C (Option: Reports)
         ├── XCTL ──► COBIL00C (Option: Bill Payment)
         └── XCTL ──► COPAUS0C (Option: Pending Auths — optional)

CA00 ─► COADM01C (Admin Menu)
         ├── XCTL ──► COUSR00C (Option 1: User List)
         ├── XCTL ──► COUSR01C (Option 2: User Add)
         ├── XCTL ──► COUSR02C (Option 3: User Update)
         ├── XCTL ──► COUSR03C (Option 4: User Delete)
         ├── XCTL ──► COTRTLIC (Option 5: Tran Type List — optional)
         └── XCTL ──► COTRTUPC (Option 6: Tran Type Maint — optional)

CAVW ─► COACTVWC (Account View)
         └── XCTL ──► CDEMO-TO-PROGRAM (dynamic — returns to caller)

CAUP ─► COACTUPC (Account Update)
         └── XCTL ──► CDEMO-TO-PROGRAM (dynamic)

CCLI ─► COCRDLIC (Card List)
         ├── XCTL ──► COMEN01C (via LIT-MENUPGM — back to menu)
         ├── XCTL ──► COCRDSLC (Card Detail — via CCARD-NEXT-PROG)
         └── XCTL ──► COCRDUPC (Card Update — via CCARD-NEXT-PROG)

CCDL ─► COCRDSLC (Card Detail)
         └── XCTL ──► CDEMO-TO-PROGRAM (dynamic)

CCUP ─► COCRDUPC (Card Update)
         └── XCTL ──► CDEMO-TO-PROGRAM (dynamic)

CT02 ─► COTRN02C (Transaction Add)
         └── CALL ──► CSUTLDTC (Date Validation utility)

CR00 ─► CORPT00C (Transaction Report Request)
         └── CALL ──► CSUTLDTC (Date Validation utility)

CPVD ─► COPAUS1C (Pending Auth Detail)
         └── XCTL ──► WS-PGM-AUTH-FRAUD (Fraud check program)

CTLI ─► COTRTLIC (Tran Type List)
         ├── XCTL ──► CDEMO-TO-PROGRAM (dynamic)
         └── XCTL ──► COTRTUPC (via LIT-ADDTPGM — Add new type)

CTTU ─► COTRTUPC (Tran Type Update)
         └── XCTL ──► CDEMO-TO-PROGRAM (dynamic)
```

### 1.2 Batch Call Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BATCH JOB PROGRAMS                                 │
└─────────────────────────────────────────────────────────────────────────────┘

POSTTRAN Job ─► CBTRN02C (Post Transactions)
                 └── CALL ──► CEE3ABD (LE Abend handler)

INTCALC Job ─► CBACT04C (Interest Calculation)
                └── CALL ──► CEE3ABD (LE Abend handler)

CREASTMT Job ─► CBSTM03A (Statement Generation - Main)
                 ├── CALL ──► CBSTM03B (Statement Sub-module)
                 └── CALL ──► CEE3ABD (LE Abend handler)

TRANREPT Job ─► CBTRN03C (Transaction Report)
                 └── CALL ──► CEE3ABD (LE Abend handler)

READACCT Job ─► CBACT01C (Account Reader)
                 ├── CALL ──► COBDATFT (ASM: Date Format Conversion)
                 └── CALL ──► CEE3ABD (LE Abend handler)

READCARD Job ─► CBACT02C (Card Reader)
                 └── CALL ──► CEE3ABD (LE Abend handler)

READCUST Job ─► CBCUS01C (Customer Reader)
                 └── CALL ──► CEE3ABD (LE Abend handler)

READXREF Job ─► CBACT03C (Cross-Ref Reader)
                 └── CALL ──► CEE3ABD (LE Abend handler)

WAITSTEP Job ─► COBSWAIT
                 └── CALL ──► MVSWAIT (ASM: Timer Wait)

CBEXPORT Job ─► CBEXPORT
                 └── CALL ──► CEE3ABD (LE Abend handler)

CBIMPORT Job ─► CBIMPORT
                 └── CALL ──► CEE3ABD (LE Abend handler)

CBPAUP0J Job ─► CBPAUP0C (Purge Expired Auths)
                 └── (DB2 operations)

CSUTLDTC (Utility — called by COTRN02C, CORPT00C)
          └── CALL ──► CEEDAYS (LE: Date validation service)
```

---

## 2. Copybook Dependency Matrix

### 2.1 Most-Referenced Copybooks (by include count)

| Copybook | Used By (# programs) | Purpose |
|:---------|:---------------------|:--------|
| CSSETATY | 39 references | Screen attribute settings |
| DFHBMSCA | 17 programs | BMS common attributes (IBM) |
| DFHAID | 17 programs | Attention ID definitions (IBM) |
| CSMSG01Y | 17 programs | Message area |
| CSDAT01Y | 17 programs | System date fields |
| COTTL01Y | 17 programs | Title/header line |
| COCOM01Y | 17 programs | Communication area |
| CVACT03Y | 14 programs | Cross-reference record |
| CVACT01Y | 13 programs | Account record |
| CSUSR01Y | 12 programs | User security record |
| CVTRA05Y | 11 programs | Transaction record |
| CVCUS01Y | 8 programs | Customer record |
| CVACT02Y | 8 programs | Card record |

### 2.2 Program → Copybook Usage

| Program | Copybooks Used |
|:--------|:---------------|
| COACTUPC | COCOM01Y, CSDAT01Y, CSMSG01Y, COTTL01Y, CSSETATY, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, DFHAID, DFHBMSCA |
| COCRDLIC | COCOM01Y, CSDAT01Y, CSMSG01Y, COTTL01Y, CSSETATY, CVACT02Y, CVACT03Y, CVCRD01Y, DFHAID, DFHBMSCA |
| COCRDUPC | COCOM01Y, CSDAT01Y, CSMSG01Y, COTTL01Y, CSSETATY, CVACT02Y, CVCRD01Y, DFHAID, DFHBMSCA |
| CBTRN02C | CVACT01Y, CVACT03Y, CVTRA05Y, CVTRA06Y, CVTRA01Y |
| CBACT04C | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y |
| CBSTM03A | COSTM01, CVACT01Y |
| CBSTM03B | CVACT01Y, CVACT03Y, CVCUS01Y, CVTRA05Y |
| CBTRN03C | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| CBEXPORT | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| CBIMPORT | CVEXPORT |

---

## 3. Data Lineage — File Read/Write by Program

### 3.1 Online Programs (CICS VSAM Access)

| Program | File/Dataset | Operation | Business Purpose |
|:--------|:-------------|:----------|:-----------------|
| COSGN00C | USRSEC | READ | Validate user credentials |
| COACTVWC | CARDXREF (AIX by ACCT) | READ | Find cards for account |
| COACTVWC | ACCTDATA | READ | Retrieve account details |
| COACTVWC | CUSTDATA | READ | Retrieve customer info |
| COACTUPC | CARDXREF (AIX by ACCT) | READ | Find cards for account |
| COACTUPC | ACCTDATA | READ/REWRITE | View & update account |
| COACTUPC | CUSTDATA | READ/REWRITE | View & update customer |
| COCRDLIC | CARDDATA | STARTBR/READNEXT/READPREV | Browse card list |
| COCRDSLC | CARDDATA | READ | View card details |
| COCRDSLC | CARDXREF (AIX by ACCT) | READ | Resolve card-account link |
| COCRDUPC | CARDDATA | READ/REWRITE | Update card details |
| COTRN00C | TRANSACT | STARTBR/READNEXT/READPREV | Browse transactions |
| COTRN02C | CARDXREF (AIX) | READ | Validate card for new transaction |
| COTRN02C | CARDXREF | READ | Get card cross-reference |
| COTRN02C | TRANSACT | READ/WRITE/REWRITE/DELETE | Add new transaction |
| COUSR00C | USRSEC | STARTBR/READNEXT/READPREV | Browse user list |
| COUSR01C | USRSEC | WRITE | Add new user |
| COUSR02C | USRSEC | READ/REWRITE | Update user record |
| COUSR03C | USRSEC | READ/DELETE | Delete user record |

### 3.2 Batch Programs (Sequential/VSAM File I/O)

| Program | Input Files | Output Files | Business Purpose |
|:--------|:------------|:-------------|:-----------------|
| CBACT01C | ACCTFILE (VSAM) | OUTFILE, ARRYFILE, VBRCFILE | Reformat account data |
| CBACT02C | CARDFILE (VSAM) | — (display only) | Read/validate card data |
| CBACT03C | XREFFILE (VSAM) | — (display only) | Read/validate XREF data |
| CBACT04C | TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT | ACCTFILE (update) | Interest calculation |
| CBCUS01C | CUSTFILE (VSAM) | — (display only) | Read/validate customer data |
| CBTRN02C | DALYTRAN, XREFFILE | TRANSACT, DALYREJS, ACCTFILE, TCATBALF | Post daily transactions |
| CBTRN03C | TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report) | Generate transaction report |
| CBSTM03A | — | STMTFILE, HTMLFILE | Generate statements |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | — (returns data) | Statement sub-module data retrieval |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | Export branch data |
| CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT | Import branch data |

---

## 4. JCL Job Data Flow

### 4.1 Daily Batch Cycle (POSTTRAN Flow)

```
                    ┌─────────────────┐
                    │  DALYTRAN.PS    │ (Daily transaction input)
                    └────────┬────────┘
                             │
                             ▼
┌──────────┐    ┌────────────────────────┐    ┌──────────────┐
│ XREFFILE │───►│  POSTTRAN (CBTRN02C)   │───►│  DALYREJS    │ (Rejected txns)
│ (VSAM)   │    │  Post Transactions     │    └──────────────┘
└──────────┘    └─────────┬──────────────┘
                          │
              ┌───────────┼───────────┐
              ▼           ▼           ▼
     ┌──────────────┐ ┌─────────┐ ┌──────────┐
     │   TRANSACT   │ │ ACCTFILE│ │ TCATBALF │
     │   (VSAM)     │ │ (VSAM)  │ │ (VSAM)   │
     │ Updated txns │ │ Balances│ │ Cat Bals │
     └──────┬───────┘ └────┬────┘ └──────────┘
            │               │
            ▼               ▼
  ┌──────────────────┐  ┌─────────────────────┐
  │ INTCALC          │  │ COMBTRAN (SORT)      │
  │ (CBACT04C)       │  │ Merge transactions   │
  │ Interest calc    │  └──────────┬───────────┘
  └──────────────────┘             │
                                   ▼
                         ┌──────────────────┐
                         │ CREASTMT         │
                         │ (CBSTM03A/B)     │
                         │ Statement output │
                         └──────────────────┘
```

### 4.2 Data Initialization Sequence

```
CLOSEFIL ──► ACCTFILE ──► CARDFILE ──► CUSTFILE ──► XREFFILE ──►
TRANFILE ──► TRANCATG ──► TRANTYPE ──► DISCGRP ──► TCATBALF ──►
DUSRSECJ ──► OPENFIL

(Each job loads the respective VSAM file from PS sequential input)
```

### 4.3 Complete Dataset Read/Write Summary

| Dataset | Written By | Read By |
|:--------|:-----------|:--------|
| ACCTDATA (Account) | ACCTFILE(JCL), COACTUPC, CBTRN02C, CBACT04C | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN02C, CBSTM03B, CBEXPORT |
| CARDDATA (Card) | CARDFILE(JCL), COCRDUPC | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT |
| CUSTDATA (Customer) | CUSTFILE(JCL), COACTUPC | COACTVWC, COACTUPC, CBCUS01C, CBSTM03B, CBEXPORT |
| CARDXREF | XREFFILE(JCL) | COACTVWC, COACTUPC, COCRDSLC, COTRN02C, CBACT03C, CBACT04C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT |
| TRANSACT | TRANFILE(JCL), COTRN02C, CBTRN02C | COTRN00C, COTRN02C, CBACT04C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT |
| USRSEC | DUSRSECJ(JCL), COUSR01C, COUSR02C | COSGN00C, COUSR00C, COUSR02C, COUSR03C |
| DALYTRAN | External feed | CBTRN02C |
| DALYREJS | CBTRN02C | (Manual review) |
| TCATBALF | TCATBALF(JCL), CBTRN02C, CBACT04C | CBACT04C |
| DISCGRP | DISCGRP(JCL) | CBACT04C |
| TRANCATG | TRANCATG(JCL) | CBTRN03C |
| TRANTYPE | TRANTYPE(JCL) | CBTRN03C |

---

## 5. Control-M Job Scheduling Dependencies

Based on `app/scheduler/CardDemo.controlm`:

```
CLOSEFIL ──────────────────────────────────────────────────────────┐
    │                                                               │
    ├──► ACCTFILE ──► CARDFILE ──► XREFFILE ──► CUSTFILE           │
    │                                                               │
    ├──► TRANBKP (Pre-processing backup)                           │
    │                                                               │
    ├──► TRANEXTR (optional: DB2 extract)                          │
    │         │                                                     │
    │         ├──► TRANCATG                                        │
    │         └──► TRANTYPE                                        │
    │                                                               │
    ├──► DISCGRP ──► TCATBALF ──► DUSRSECJ                        │
    │                                                               │
    └──► POSTTRAN ──► INTCALC ──► TRANBKP (Post-processing backup) │
              │                                                     │
              └──► COMBTRAN ──► CREASTMT ──► TRANIDX               │
                                                                    │
    OPENFIL ◄──────────────────────────────────────────────────────┘
```

---

## 6. Technology Integration Points

| Integration | Programs Involved | Protocol/Method |
|:------------|:-----------------|:----------------|
| IBM MQ | COPAUA0C, CODATE01, COACCT01 | MQPUT/MQGET (request-response) |
| IMS DB | COPAUS0C, COPAUS1C, COPAUA0C | DL/I calls via PCBs |
| DB2 | COTRTLIC, COTRTUPC, COBTUPDT, CBPAUP0C | Embedded SQL (EXEC SQL) |
| CICS Commarea | All online programs | EXEC CICS XCTL/RETURN |
| VSAM KSDS | All data-access programs | EXEC CICS READ/WRITE/REWRITE |
| SORT Utility | COMBTRAN job | JCL SORT step |
| IDCAMS | 15+ JCL jobs | VSAM cluster management |
| LE Runtime | All batch programs | CEE3ABD (abend), CEEDAYS (date) |
| Assembler | CBACT01C, COBSWAIT | CALL to COBDATFT, MVSWAIT |
