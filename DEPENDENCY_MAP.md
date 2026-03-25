# CardDemo Dependency Map

> **Generated from**: Static analysis of CALL/XCTL/LINK statements, COPY directives, CICS FILE operations, and JCL DD statements
>
> **Purpose**: Documents the program-to-program call graph, copybook inclusion tree, CICS screen flow, and batch data lineage

---

## 1. Online CICS Program Call Graph

### 1.1 Screen Navigation Flow

```
                            ┌─────────────┐
                            │  COSGN00C   │
                            │  (Sign-on)  │
                            └──────┬──────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │ XCTL                        │ XCTL
                    ▼                             ▼
            ┌──────────────┐              ┌──────────────┐
            │  COADM01C    │              │  COMEN01C    │
            │ (Admin Menu) │              │ (Main Menu)  │
            └──────┬───────┘              └──────┬───────┘
                   │ XCTL                        │ XCTL
        ┌──────┬──┴──┬──────┐         ┌────┬────┼────┬────┬────┬────┬────┬────┬─────┐
        ▼      ▼     ▼      ▼         ▼    ▼    ▼    ▼    ▼    ▼    ▼    ▼    ▼     ▼
    COUSR00C COUSR01C COUSR02C COUSR03C  (1)  (2)  (3)  (4)  (5)  (6)  (7)  (8)  (9)  (10)
    UserList UserAdd  UserUpd  UserDel

    (1) COACTVWC - Account View       (6) COTRN00C - Transaction List
    (2) COACTUPC - Account Update      (7) COTRN01C - Transaction View
    (3) COCRDLIC - Credit Card List    (8) COTRN02C - Transaction Add
    (4) COCRDSLC - Credit Card View    (9) CORPT00C - Transaction Reports
    (5) COCRDUPC - Credit Card Update  (10) COBIL00C - Bill Payment
```

### 1.2 XCTL (Transfer Control) Details

| Source Program | Target Program | Condition |
|---|---|---|
| `COSGN00C` | `COADM01C` | User type = Admin |
| `COSGN00C` | `COMEN01C` | User type = Regular |
| `COMEN01C` | `COACTVWC` | Menu option 1 (Account View) |
| `COMEN01C` | `COACTUPC` | Menu option 2 (Account Update) |
| `COMEN01C` | `COCRDLIC` | Menu option 3 (Credit Card List) |
| `COMEN01C` | `COCRDSLC` | Menu option 4 (Credit Card View) |
| `COMEN01C` | `COCRDUPC` | Menu option 5 (Credit Card Update) |
| `COMEN01C` | `COTRN00C` | Menu option 6 (Transaction List) |
| `COMEN01C` | `COTRN01C` | Menu option 7 (Transaction View) |
| `COMEN01C` | `COTRN02C` | Menu option 8 (Transaction Add) |
| `COMEN01C` | `CORPT00C` | Menu option 9 (Transaction Reports) |
| `COMEN01C` | `COBIL00C` | Menu option 10 (Bill Payment) |
| `COMEN01C` | `COPAUS0C` | Menu option 11 (Pending Auth View - optional module) |
| `COADM01C` | `COUSR00C` | Admin option 1 (User List) |
| `COADM01C` | `COUSR01C` | Admin option 2 (User Add) |
| `COADM01C` | `COUSR02C` | Admin option 3 (User Update) |
| `COADM01C` | `COUSR03C` | Admin option 4 (User Delete) |
| `COADM01C` | `COTRTLIC` | Admin option 5 (Tran Type List - DB2 module) |
| `COADM01C` | `COTRTUPC` | Admin option 6 (Tran Type Maint - DB2 module) |
| `COCRDLIC` | `COMEN01C` | Return to menu (via LIT-MENUPGM) |
| `COCRDLIC` | `COCRDSLC` | Select card for detail view (via CCARD-NEXT-PROG) |
| `COCRDLIC` | `COCRDUPC` | Select card for update (via CCARD-NEXT-PROG) |
| `COCRDSLC` | `COMEN01C` | Return to menu (via CDEMO-TO-PROGRAM) |
| `COCRDUPC` | `COMEN01C` | Return to menu (via CDEMO-TO-PROGRAM) |
| `COACTVWC` | `COMEN01C` | Return to menu (via CDEMO-TO-PROGRAM) |
| `COACTUPC` | `COMEN01C` | Return to menu (via CDEMO-TO-PROGRAM) |

### 1.3 CICS RETURN TRANSID Flow

All online programs use `EXEC CICS RETURN TRANSID(...)` to return to themselves on the next terminal input, maintaining conversational state via COMMAREA. The COSGN00C program is the initial entry point via transaction `CC00`.

---

## 2. Batch Program Call Graph

```
┌──────────────────────────────────────────────────────┐
│                   JCL Orchestration                  │
└──────────────────────────────────────────────────────┘

POSTTRAN.jcl ──► CBTRN02C (Transaction Posting)

INTCALC.jcl  ──► CBACT04C (Interest Calculation)

CREASTMT.JCL ──► CBSTM03A (Statement Driver)
                    │
                    └──► CALL 'CBSTM03B' (File I/O Subroutine)

TRANREPT.jcl ──► REPROC.prc (Backup) + SORT + CBTRN03C (Report)

CBEXPORT.jcl ──► CBEXPORT (Data Export)

CBIMPORT.jcl ──► CBIMPORT (Data Import)

WAITSTEP.jcl ──► COBSWAIT
                    │
                    └──► CALL 'MVSWAIT' (Assembler wait)
```

### 2.1 Batch CALL Statements

| Calling Program | Called Program/Routine | Mechanism | Purpose |
|---|---|---|---|
| `CBSTM03A` | `CBSTM03B` | `CALL 'CBSTM03B'` | File I/O operations (open/read/close VSAM files) |
| `COBSWAIT` | `MVSWAIT` | `CALL 'MVSWAIT'` | Assembler wait routine for inter-step pausing |
| `CSUTLDTC` | `CEEDAYS` | `CALL 'CEEDAYS'` | LE date conversion (Lilian day number) |
| `CSUTLDTC` | `CEEDATM` | `CALL 'CEEDATM'` | LE date formatting |
| `CBACT01C` | `CSUTLDTC` | Inline `COPY CODATECN` | Date conversion via included utility code |

---

## 3. Copybook Inclusion Matrix

### 3.1 Data Copybooks Used by Programs

| Copybook | Used By (Programs) | Entity |
|---|---|---|
| **CVACT01Y** (Account) | COACTVWC, COACTUPC, COBIL00C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT | Account |
| **CVACT02Y** (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT | Card |
| **CVACT03Y** (Cross-Ref) | COACTVWC, COACTUPC, COBIL00C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, CBSTM03A | Cross-Reference |
| **CVCUS01Y** (Customer) | COACTVWC, COACTUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | Customer |
| **CVTRA05Y** (Transaction) | COTRN02C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | Transaction |
| **CVTRA06Y** (Daily Tran) | CBTRN01C, CBTRN02C | Daily Transaction |
| **CVTRA01Y** (Cat Balance) | CBACT04C, CBTRN02C | Category Balance |
| **CVTRA02Y** (Disclosure) | CBACT04C | Disclosure Group |
| **CVTRA03Y** (Tran Type) | CBTRN03C | Transaction Type |
| **CVTRA04Y** (Tran Cat) | CBTRN03C | Transaction Category |
| **CVTRA07Y** (Report) | CBTRN03C | Report Layout |
| **CSUSR01Y** (User Sec) | COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COACTVWC, COACTUPC, COCRDLIC, COCRDUPC | User Security |
| **COCOM01Y** (Commarea) | All 17 online programs | Inter-program Comm |
| **COTTL01Y** (Title) | All 17 online programs | Screen Header |
| **CSDAT01Y** (Date) | All 17 online programs | Date Formatting |
| **CSMSG01Y** (Message) | All 17 online programs | Screen Messages |
| **CVCRD01Y** (Card Nav) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | Card Navigation |
| **COSTM01** (Stmt Tran) | CBSTM03A | Statement Record |
| **CUSTREC** (Cust Stmt) | CBSTM03A | Statement Customer |
| **CVEXPORT** (Export) | CBEXPORT, CBIMPORT | Data Migration |
| **CSSETATY** (Attribute) | COACTUPC (38 COPY REPLACING instances) | Field Attributes |
| **CSSTRPFY** (Strip) | COACTUPC, COACTVWC, COCRDLIC | String Utility |
| **CSUTLDPY** (Date Util) | COACTUPC | Date Utility |
| **CSUTLDWY** (Date WS) | COACTUPC | Date Working Storage |
| **CSLKPCDY** (Lookup) | COACTUPC | Lookup Codes |
| **CODATECN** (Date Const) | CBACT01C | Date Constants |
| **COMEN02Y** (Menu Def) | COMEN01C | Menu Options |
| **COADM02Y** (Admin Def) | COADM01C | Admin Menu Options |
| **CSMSG02Y** (Ext Msg) | COACTVWC, COACTUPC | Extended Messages |

### 3.2 BMS Copybook Inclusion

Each online program includes its corresponding BMS-generated copybook:

| Program | BMS Map Copybook | Data Copybook |
|---|---|---|
| COSGN00C | COSGN00 | -- |
| COMEN01C | COMEN01 | COMEN02Y |
| COADM01C | COADM01 | COADM02Y |
| COACTVWC | COACTVW | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| COACTUPC | COACTUP | CVACT01Y, CVACT03Y, CVCUS01Y |
| COCRDLIC | COCRDLI | CVACT02Y |
| COCRDSLC | COCRDSL | -- |
| COCRDUPC | COCRDUP | -- |
| COTRN00C | COTRN00 | -- |
| COTRN01C | COTRN01 | -- |
| COTRN02C | COTRN02 | CVTRA05Y |
| CORPT00C | CORPT00 | -- |
| COBIL00C | COBIL00 | CVACT01Y, CVACT03Y, CVTRA05Y |
| COUSR00C | COUSR00 | CSUSR01Y |
| COUSR01C | COUSR01 | CSUSR01Y |
| COUSR02C | COUSR02 | CSUSR01Y |
| COUSR03C | COUSR03 | CSUSR01Y |

---

## 4. VSAM File Access Matrix (Online Programs)

### 4.1 CICS File Operations by Program

| Program | USRSEC | ACCTDAT | CARDDAT | CCXREF | CXACAIX | TRANSACT | Operations |
|---|---|---|---|---|---|---|---|
| **COSGN00C** | R | | | | | | READ (login validation) |
| **COMEN01C** | R | | | | | | READ (menu access check) |
| **COADM01C** | R | | | | | | READ (admin access check) |
| **COACTVWC** | | R | R | R | | | READ ×3 (account+card+xref) |
| **COACTUPC** | | R/W | R | R | | | READ ×5, REWRITE ×2 (account+customer) |
| **COCRDLIC** | | | R | | | | STARTBR, READNEXT, READPREV, ENDBR |
| **COCRDSLC** | | | R | R | | | READ ×2 (card+xref) |
| **COCRDUPC** | | | R/W | R | | | READ ×2, REWRITE (card update) |
| **COTRN00C** | | | | | | R | STARTBR, READNEXT, READPREV, ENDBR |
| **COTRN01C** | | | | | | R | READ (single transaction) |
| **COTRN02C** | | R | | R | R | R/W | READ ×3, STARTBR, READPREV, ENDBR, WRITE |
| **CORPT00C** | | | | | | | WRITEQ TD (report request to TDQ) |
| **COBIL00C** | | R/W | | | R | R/W | READ ×2, REWRITE, STARTBR, READPREV, ENDBR, WRITE |
| **COUSR00C** | R | | | | | | STARTBR, READNEXT, READPREV, ENDBR |
| **COUSR01C** | W | | | | | | WRITE (new user) |
| **COUSR02C** | R/W | | | | | | READ, REWRITE (update user) |
| **COUSR03C** | R/D | | | | | | READ, DELETE (remove user) |

**Legend**: R=Read, W=Write, D=Delete, R/W=Read+Write/Rewrite, R/D=Read+Delete

### 4.2 Batch File Operations by Program

| Program | Input Files | Output Files | I/O Files |
|---|---|---|---|
| **CBTRN02C** | DALYTRAN (seq) | TRANSACT (seq-out), DALYREJS (seq-out) | ACCOUNT (VSAM I-O), TCATBAL (VSAM I-O) |
| **CBACT04C** | TCATBAL, XREF, DISCGRP (VSAM input) | TRANSACT (seq-out) | ACCOUNT (VSAM I-O) |
| **CBTRN01C** | DALYTRAN, CUSTOMER, XREF, CARD, ACCOUNT, TRANSACT (all input) | -- | -- |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM (all input) | TRANREPT (seq-out) | -- |
| **CBSTM03A** | (via CBSTM03B: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE) | STMTFILE, HTMLFILE (seq-out) | -- |
| **CBSTM03B** | TRNX-FILE, XREF-FILE, CUST-FILE, ACCT-FILE (VSAM input) | -- | -- |
| **CBACT01C** | ACCTFILE (VSAM input) | OUT-FILE, ARRY-FILE, VBRC-FILE (seq-out) | -- |
| **CBACT02C** | CARDFILE (VSAM input) | -- (display only) | -- |
| **CBACT03C** | XREFFILE (VSAM input) | -- (display only) | -- |
| **CBCUS01C** | CUSTFILE (VSAM input) | -- (display only) | -- |
| **CBEXPORT** | CUSTOMER, ACCOUNT, XREF, TRANSACTION, CARD (VSAM input) | EXPORT-OUTPUT (seq-out) | -- |
| **CBIMPORT** | EXPORT-INPUT (seq input) | CUSTOMER, ACCOUNT, XREF, TRANSACTION, CARD, ERROR (seq-out) | -- |

---

## 5. JCL Job Data Lineage

### 5.1 Data Refresh Jobs (Sequential PS → VSAM KSDS)

```
ACCTFILE.jcl:   ACCTDATA.PS ──► IDCAMS REPRO ──► ACCTDATA.VSAM.KSDS
CARDFILE.jcl:   CARDDATA.PS ──► IDCAMS REPRO ──► CARDDATA.VSAM.KSDS
CUSTFILE.jcl:   CUSTDATA.PS ──► IDCAMS REPRO ──► CUSTDATA.VSAM.KSDS
XREFFILE.jcl:   CARDXREF.PS ──► IDCAMS REPRO ──► CARDXREF.VSAM.KSDS ──► AIX + PATH
TRANFILE.jcl:   DALYTRAN.PS.INIT ──► IDCAMS REPRO ──► TRANSACT.VSAM.KSDS ──► AIX + PATH
DUSRSECJ.jcl:   USRSEC.PS ──► IDCAMS REPRO ──► USRSEC.VSAM.KSDS
TCATBALF.jcl:   TCATBALF.PS ──► IDCAMS REPRO ──► TCATBALF.VSAM.KSDS
TRANCATG.jcl:   TRANCATG.PS ──► IDCAMS REPRO ──► TRANCATG.VSAM.KSDS
TRANTYPE.jcl:   TRANTYPE.PS ──► IDCAMS REPRO ──► TRANTYPE.VSAM.KSDS
DISCGRP.jcl:    DISCGRP.PS ──► IDCAMS REPRO ──► DISCGRP.VSAM.KSDS
```

### 5.2 Transaction Posting Pipeline (`POSTTRAN.jcl`)

```
                    ┌──────────────────┐
                    │  DALYTRAN.PS     │  (Daily transactions from external feed)
                    └────────┬─────────┘
                             │ INPUT
                             ▼
                    ┌──────────────────┐     ┌──────────────────┐
                    │    CBTRN02C      │────►│  DALYREJS        │  (Rejected transactions)
                    │  (Posting Engine)│     └──────────────────┘
                    └──┬────┬────┬─────┘
                       │    │    │
          ┌────────────┘    │    └────────────┐
          ▼                 ▼                 ▼
┌──────────────────┐ ┌───────────────┐ ┌───────────────┐
│ TRANSACT.VSAM    │ │ ACCTDATA.VSAM │ │ TCATBALF.VSAM │
│ (Posted trans)   │ │ (Updated bal) │ │ (Cat balances)│
└──────────────────┘ └───────────────┘ └───────────────┘
```

### 5.3 Interest Calculation Pipeline (`INTCALC.jcl`)

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ TCATBALF.VSAM    │  │ CARDXREF.VSAM    │  │ DISCGRP.VSAM     │
│ (Cat balances)   │  │ (Card→Acct map)  │  │ (Interest rates) │
└────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
         │ INPUT               │ INPUT               │ INPUT
         └─────────────┬───────┴──────────────────────┘
                       ▼
              ┌──────────────────┐
              │    CBACT04C      │
              │  (Interest Calc) │
              └──┬────────────┬──┘
                 │            │
                 ▼            ▼
        ┌──────────────┐ ┌───────────────┐
        │ ACCTDATA.VSAM│ │ TRANSACT (seq)│
        │ (Updated bal)│ │ (Interest txn)│
        └──────────────┘ └───────────────┘
```

### 5.4 Statement Generation Pipeline (`CREASTMT.JCL`)

```
┌──────────────────┐
│ TRANSACT.VSAM    │
│ (All transactions)│
└────────┬─────────┘
         │ SORT (by card+tran ID)
         ▼
┌──────────────────┐
│ TRXFL.VSAM.KSDS  │  (Sorted/re-keyed transactions)
└────────┬─────────┘
         │ INPUT
         ▼
┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│    CBSTM03A      │◄─│ XREFFILE     │◄─│ CUSTFILE     │◄─│ ACCTFILE     │
│  (Stmt Driver)   │  │ (Card→Cust)  │  │ (Cust names) │  │ (Acct data)  │
└──┬────────────┬──┘  └──────────────┘  └──────────────┘  └──────────────┘
   │            │
   │  CALL      │
   ▼            ▼
┌────────┐  ┌──────────────┐  ┌──────────────┐
│CBSTM03B│  │ STATEMNT.PS  │  │ STATEMNT.HTML│
│(File IO)│ │ (Text stmts) │  │ (HTML stmts) │
└────────┘  └──────────────┘  └──────────────┘
```

### 5.5 Daily Transaction Report Pipeline (`TRANREPT.jcl`)

```
┌──────────────────┐
│ TRANSACT.VSAM    │
│ (All transactions)│
└────────┬─────────┘
         │ REPROC.prc (backup to GDG)
         ▼
┌──────────────────┐
│ TRANSACT.BKUP(+1)│  (GDG backup)
└────────┬─────────┘
         │ SORT (by date range)
         ▼
┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐
│    CBTRN03C      │◄─│ CARDXREF     │◄─│ TRANTYPE     │◄─│ TRANCATG     │◄─│ DATEPARM  │
│  (Report Gen)    │  │ (Card→Acct)  │  │ (Type desc)  │  │ (Cat desc)   │  │ (Dates)   │
└────────┬─────────┘  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘
         │
         ▼
┌──────────────────┐
│ TRANREPT(+1)     │  (GDG daily report)
└──────────────────┘
```

### 5.6 Data Export/Import Pipeline

```
Export (CBEXPORT.jcl):
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ CUSTDAT VSAM │  │ ACCTDAT VSAM │  │ CARDXREF VSAM│  │ TRANSACT VSAM│  │ CARDDAT VSAM │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       └────────────┬────┴────────────┬────┴────────────┬────┘                  │
                    ▼                                                           │
              ┌──────────────┐◄────────────────────────────────────────────────┘
              │   CBEXPORT   │
              └──────┬───────┘
                     ▼
              ┌──────────────┐
              │ EXPORT FILE  │  (Single tagged sequential file)
              └──────────────┘

Import (CBIMPORT.jcl):
              ┌──────────────┐
              │ EXPORT FILE  │
              └──────┬───────┘
                     ▼
              ┌──────────────┐
              │   CBIMPORT   │
              └──┬───┬───┬───┬───┬───┐
                 ▼   ▼   ▼   ▼   ▼   ▼
              CUST ACCT XREF TRAN CARD ERROR
              (Sequential output files)
```

### 5.7 Full Batch Cycle Data Flow

```
Batch Cycle Order: CLOSEFIL → Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANREPT → TRANIDX → OPENFIL

Step 1: CLOSEFIL       Close CICS files for exclusive batch access
Step 2: Data Refresh    Load PS → VSAM for all master files
Step 3: POSTTRAN        DALYTRAN → TRANSACT + update ACCTDAT + TCATBALF
Step 4: INTCALC         TCATBALF + DISCGRP → Interest txns in TRANSACT + update ACCTDAT
Step 5: TRANBKP         TRANSACT VSAM → GDG backup
Step 6: COMBTRAN        Merge daily + master transactions
Step 7: CREASTMT        TRANSACT → SORT → CBSTM03A → Text + HTML statements
Step 8: TRANREPT        TRANSACT → SORT → CBTRN03C → Daily transaction report
Step 9: TRANIDX         Rebuild alternate indexes on TRANSACT
Step 10: OPENFIL        Re-open CICS files for online access
```

---

## 6. VSAM Dataset Dependency Summary

### Files Read by Multiple Programs (Shared Resources)

| VSAM Dataset | Online Readers | Online Writers | Batch Readers | Batch Writers |
|---|---|---|---|---|
| `USRSEC.VSAM.KSDS` | COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C | -- | -- |
| `ACCTDATA.VSAM.KSDS` | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC, COBIL00C | CBACT01C, CBACT04C, CBTRN02C, CBSTM03B | CBTRN02C, CBACT04C |
| `CARDDATA.VSAM.KSDS` | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC | CBACT02C, CBTRN01C, CBEXPORT | -- |
| `CARDXREF.VSAM.KSDS` | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COTRN02C, COBIL00C | -- | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CBEXPORT | -- |
| `TRANSACT.VSAM.KSDS` | COTRN00C, COTRN01C, COBIL00C | COTRN02C, COBIL00C | CBTRN01C, CBTRN03C, CBSTM03B, CBEXPORT | CBTRN02C, CBACT04C |
| `CUSTDATA.VSAM.KSDS` | COACTVWC, COACTUPC | COACTUPC | CBCUS01C, CBTRN01C, CBSTM03B, CBEXPORT | -- |
| `TCATBALF.VSAM.KSDS` | -- | -- | CBACT04C | CBTRN02C |
| `DISCGRP.VSAM.KSDS` | -- | -- | CBACT04C | -- |
| `TRANTYPE.VSAM.KSDS` | -- | -- | CBTRN03C | -- |
| `TRANCATG.VSAM.KSDS` | -- | -- | CBTRN03C | -- |

---

## 7. Modernization Dependency Clusters

Programs that share data dependencies should be modernized together to maintain data consistency.

### Cluster 1: Account Management (Highest Coupling)
- **Programs**: COACTVWC, COACTUPC, COBIL00C, CBTRN02C, CBACT04C
- **Shared Data**: ACCTDATA, CARDXREF, TRANSACT
- **Risk**: Account balance is updated by 4 different programs

### Cluster 2: Card Management
- **Programs**: COCRDLIC, COCRDSLC, COCRDUPC
- **Shared Data**: CARDDATA, CARDXREF
- **Risk**: Card status changes affect transaction processing

### Cluster 3: Transaction Processing
- **Programs**: COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C
- **Shared Data**: TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF
- **Risk**: Core business logic with most complex data flow

### Cluster 4: User Security
- **Programs**: COSGN00C, COMEN01C, COADM01C, COUSR00C-03C
- **Shared Data**: USRSEC
- **Risk**: Low - isolated data, but critical for authentication

### Cluster 5: Reporting & Statements
- **Programs**: CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B
- **Shared Data**: TRANSACT (read-only), CARDXREF, CUSTDATA, ACCTDATA
- **Risk**: Low - read-only consumers, can be modernized independently
