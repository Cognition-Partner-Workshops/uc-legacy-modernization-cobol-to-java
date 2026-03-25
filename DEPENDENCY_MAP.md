# Dependency Map -- CardDemo COBOL Codebase

> Call graph, data lineage, and file I/O mapping extracted from static analysis of COBOL source, JCL, and BMS maps.

---

## 1. Online (CICS) Call Graph

### 1.1 Program-to-Program Transfers (XCTL)

All online navigation uses `EXEC CICS XCTL` passing `CARDDEMO-COMMAREA`.

```
COSGN00C (Sign-on)
  |
  |--[Admin user]--> COADM01C (Admin Menu)
  |                    |--[1]--> COUSR00C (User List)
  |                    |           |--[Select]--> COUSR02C (User Update)
  |                    |           |--[Select]--> COUSR03C (User Delete)
  |                    |--[2]--> COUSR01C (User Add)
  |                    |--[3]--> COUSR02C (User Update)
  |                    |--[4]--> COUSR03C (User Delete)
  |                    |--[5]--> COTRTLIC (Tran Type List) [optional DB2 module]
  |                    |--[6]--> COTRTUPC (Tran Type Maint) [optional DB2 module]
  |
  |--[Regular user]--> COMEN01C (Main Menu)
                         |--[1]--> COACTVWC (Account View)
                         |--[2]--> COACTUPC (Account Update)
                         |--[3]--> COCRDLIC (Card List)
                         |           |--[Select]--> COCRDSLC (Card View)
                         |           |--[Select]--> COCRDUPC (Card Update)
                         |--[4]--> COCRDSLC (Card View)
                         |--[5]--> COCRDUPC (Card Update)
                         |--[6]--> COTRN00C (Transaction List)
                         |           |--[Select]--> COTRN01C (Transaction View)
                         |--[7]--> COTRN01C (Transaction View)
                         |--[8]--> COTRN02C (Transaction Add)
                         |--[9]--> CORPT00C (Transaction Reports)
                         |--[10]-> COBIL00C (Bill Payment)
                         |--[11]-> COPAUS0C (Pending Auth) [optional module]
```

### 1.2 XCTL Transfer Matrix

| From Program | To Program | Trigger | Direction |
|-------------|------------|---------|-----------|
| COSGN00C | COMEN01C | Successful login (user type U) | Forward |
| COSGN00C | COADM01C | Successful login (user type A) | Forward |
| COMEN01C | COACTVWC | Menu option 1 | Forward |
| COMEN01C | COACTUPC | Menu option 2 | Forward |
| COMEN01C | COCRDLIC | Menu option 3 | Forward |
| COMEN01C | COCRDSLC | Menu option 4 | Forward |
| COMEN01C | COCRDUPC | Menu option 5 | Forward |
| COMEN01C | COTRN00C | Menu option 6 | Forward |
| COMEN01C | COTRN01C | Menu option 7 | Forward |
| COMEN01C | COTRN02C | Menu option 8 | Forward |
| COMEN01C | CORPT00C | Menu option 9 | Forward |
| COMEN01C | COBIL00C | Menu option 10 | Forward |
| COMEN01C | COPAUS0C | Menu option 11 | Forward |
| COADM01C | COUSR00C | Admin option 1 | Forward |
| COADM01C | COUSR01C | Admin option 2 | Forward |
| COADM01C | COUSR02C | Admin option 3 | Forward |
| COADM01C | COUSR03C | Admin option 4 | Forward |
| COADM01C | COTRTLIC | Admin option 5 (DB2) | Forward |
| COADM01C | COTRTUPC | Admin option 6 (DB2) | Forward |
| COACTVWC | COMEN01C | PF3 (Return) | Back |
| COACTUPC | COMEN01C | PF3 (Return) | Back |
| COCRDLIC | COCRDSLC | Select card for view | Forward |
| COCRDLIC | COCRDUPC | Select card for update | Forward |
| COCRDLIC | COMEN01C | PF3 (Return) | Back |
| COCRDSLC | COMEN01C | PF3 (Return) | Back |
| COCRDUPC | COMEN01C | PF3 (Return) | Back |
| COTRN00C | COTRN01C | Select transaction | Forward |
| COTRN00C | COMEN01C | PF3 (Return) | Back |
| COTRN01C | COMEN01C | PF3 (Return) | Back |
| COUSR00C | COUSR02C | Select user for update | Forward |
| COUSR00C | COUSR03C | Select user for delete | Forward |
| COUSR00C | COADM01C | PF3 (Return) | Back |
| COUSR01C | COADM01C | PF3 (Return) | Back |
| COUSR02C | COADM01C | PF3 (Return) | Back |
| COUSR03C | COADM01C | PF3 (Return) | Back |

---

## 2. Online (CICS) Data Access -- File I/O

### 2.1 VSAM File Access by Program

| Program | ACCTDAT | CARDDAT | CUSTDAT | CCXREF / CXACAIX | TRANSACT | USRSEC |
|---------|---------|---------|---------|-------------------|----------|--------|
| COSGN00C | | | | | | R |
| COADM01C | | | | | | R |
| COACTVWC | R | | R | R (CXACAIX) | | |
| COACTUPC | R/W | | R/W | R (CXACAIX) | | |
| COCRDLIC | | R (browse) | | | | |
| COCRDSLC | | R | R | | | |
| COCRDUPC | | R/W | | | | |
| COTRN00C | | | | | R (browse) | |
| COTRN01C | | | | | R | |
| COTRN02C | R | | | R (CCXREF, CXACAIX) | W | |
| CORPT00C | | | | | | |
| COBIL00C | R/W | | | R (CXACAIX) | R/W | |
| COUSR00C | | | | | | R (browse) |
| COUSR01C | | | | | | W |
| COUSR02C | | | | | | R/W |
| COUSR03C | | | | | | R/D |

**Legend:** R=Read, W=Write, D=Delete, R/W=Read+Rewrite

### 2.2 CICS Operations Detail

| Program | Operation | VSAM File | Access Type |
|---------|-----------|-----------|-------------|
| COSGN00C | READ | USRSEC | Direct (keyed) |
| COACTVWC | READ | CXACAIX | Direct (keyed by acct) |
| COACTVWC | READ | ACCTDAT | Direct (keyed) |
| COACTVWC | READ | CUSTDAT | Direct (keyed) |
| COACTUPC | READ | CXACAIX | Direct (keyed by acct) |
| COACTUPC | READ | ACCTDAT | Direct (keyed) |
| COACTUPC | READ | CUSTDAT | Direct (keyed) |
| COACTUPC | REWRITE | ACCTDAT | Update in place |
| COACTUPC | REWRITE | CUSTDAT | Update in place |
| COCRDLIC | STARTBR/READNEXT/READPREV | CARDDAT | Sequential browse |
| COCRDSLC | READ | CARDDAT | Direct (keyed) |
| COCRDSLC | READ | CUSTDAT | Direct (keyed) |
| COCRDUPC | READ | CARDDAT | Direct (keyed) |
| COCRDUPC | REWRITE | CARDDAT | Update in place |
| COTRN00C | STARTBR/READNEXT/READPREV | TRANSACT | Sequential browse |
| COTRN01C | READ | TRANSACT | Direct (keyed) |
| COTRN02C | READ | ACCTDAT | Direct (keyed) |
| COTRN02C | READ | CCXREF, CXACAIX | Direct (keyed) |
| COTRN02C | WRITE | TRANSACT | New record |
| COBIL00C | READ | CXACAIX | Direct (keyed by acct) |
| COBIL00C | STARTBR/READPREV | TRANSACT | Browse (get max ID) |
| COBIL00C | WRITE | TRANSACT | New record (payment) |
| COBIL00C | READ/REWRITE | ACCTDAT | Update balance |
| COUSR00C | STARTBR/READNEXT/READPREV | USRSEC | Sequential browse |
| COUSR01C | WRITE | USRSEC | New record |
| COUSR02C | READ/REWRITE | USRSEC | Update in place |
| COUSR03C | READ/DELETE | USRSEC | Delete record |

---

## 3. Batch Processing Call Graph

### 3.1 Batch Program Dependencies

```
CBTRN02C (Post Transactions)
  |- Reads:  DALYTRAN (sequential), CCXREF (random), ACCTDAT (random), TCATBAL (random)
  |- Writes: TRANSACT (indexed), DALYREJS (sequential), TCATBAL (update)

CBACT04C (Interest Calculation)
  |- Reads:  TCATBAL (sequential), CCXREF (random), ACCTDAT (random), DISCGRP (random)
  |- Writes: TRANSACT (sequential), ACCTDAT (update)

CBTRN03C (Transaction Report)
  |- Reads:  TRANSACT, CCXREF, TRANTYPE, TRANCATG, DATEPARM
  |- Writes: TRANREPT (report output)

CBSTM03A (Statement Generation - Driver)
  |- Reads:  TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE
  |- Writes: STMTFILE (text), HTMLFILE (HTML)
  |- Calls:  CBSTM03B (sub-program via CALL)

CBSTM03B (Statement Generation - Sub-program)
  |- Reads:  TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE
  |- (Called by CBSTM03A)

CBTRN01C (Daily Transaction Validation)
  |- Reads:  DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANSACT
  |- Writes: (validation results)

CBEXPORT (Data Export)
  |- Reads:  CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE
  |- Writes: EXPFILE (sequential, 500-byte records)

CBIMPORT (Data Import)
  |- Reads:  EXPFILE (sequential)
  |- Writes: CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT

CBACT01C (Account Reader)    --> Reads: ACCTFILE    Writes: OUTFILE, ARRYFILE, VBRCFILE
CBACT02C (Card Reader)       --> Reads: CARDFILE    Writes: (display only)
CBACT03C (Xref Reader)       --> Reads: XREFFILE    Writes: (display only)
CBCUS01C (Customer Reader)   --> Reads: CUSTFILE    Writes: (display only)
```

### 3.2 Batch Inter-Program Calls

| Caller | Callee | Mechanism |
|--------|--------|-----------|
| CBSTM03A | CBSTM03B | COBOL CALL (sub-program linkage) |
| CORPT00C | JCL (INTRDRJ1/J2) | EXEC CICS WRITEQ TD (internal reader) |

---

## 4. JCL Job -- Program -- File Mapping

### 4.1 Core Batch Jobs

| JCL Job | Step | Program | Input Files | Output Files |
|---------|------|---------|-------------|--------------|
| POSTTRAN.jcl | STEP15 | CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF | TRANFILE, DALYREJS |
| INTCALC.jcl | STEP15 | CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | TRANSACT |
| COMBTRAN.jcl | STEP05R | SORT | TRANSACT (multiple) | SORTOUT |
| COMBTRAN.jcl | STEP10 | IDCAMS | SORTOUT | TRANVSAM |
| CREASTMT.JCL | STEP010 | SORT | TRANSACT.VSAM.KSDS | TRXFL.SEQ |
| CREASTMT.JCL | STEP020 | IDCAMS | TRXFL.SEQ | TRXFL.VSAM.KSDS |
| CREASTMT.JCL | STEP030 | IEFBR14 | -- | HTMLFILE, STMTFILE (delete/recreate) |
| CREASTMT.JCL | STEP040 | CBSTM03A | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE |
| TRANREPT.jcl | -- | CBTRN03C | TRANSACT, XREFFILE, TRANTYPE, TRANCATG | TRANREPT |
| CBEXPORT.jcl | -- | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| CBIMPORT.jcl | -- | CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT |

### 4.2 Data Loading Jobs

| JCL Job | Utility | Source (Sequential) | Target (VSAM KSDS) |
|---------|---------|---------------------|---------------------|
| ACCTFILE.jcl | IDCAMS REPRO | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| CARDFILE.jcl | IDCAMS REPRO | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| CUSTFILE.jcl | IDCAMS REPRO | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| XREFFILE.jcl | IDCAMS REPRO | CARDXREF.PS | CARDXREF.VSAM.KSDS |
| TRANFILE.jcl | IDCAMS REPRO | TRANSACT.PS | TRANSACT.VSAM.KSDS |
| DUSRSECJ.jcl | IDCAMS REPRO | USRSEC.PS | USRSEC.VSAM.KSDS |
| DISCGRP.jcl | IDCAMS REPRO | DISCGRP.PS | DISCGRP.VSAM.KSDS |
| TCATBALF.jcl | IDCAMS REPRO | TCATBAL.PS | TCATBAL.VSAM.KSDS |
| TRANTYPE.jcl | IDCAMS REPRO | TRANTYPE.PS | TRANTYPE.VSAM.KSDS |
| TRANCATG.jcl | IDCAMS REPRO | TRANCATG.PS | TRANCATG.VSAM.KSDS |

---

## 5. Batch Cycle Execution Order

The nightly batch window follows this sequence (derived from JCL dependencies and scheduler configs):

```
Phase 1: Prepare
  CLOSEFIL -----> Close CICS files for exclusive batch access

Phase 2: Data Refresh (parallel where possible)
  ACCTFILE -----> Refresh account master
  CARDFILE -----> Refresh card master
  CUSTFILE -----> Refresh customer master
  XREFFILE -----> Load cross-reference
  DUSRSECJ -----> Load user security
  DISCGRP  -----> Load disclosure groups
  TCATBALF -----> Load category balances
  TRANTYPE -----> Load transaction types
  TRANCATG -----> Load transaction categories
  TRANFILE -----> Load transaction master

Phase 3: Core Processing (sequential -- order matters)
  POSTTRAN -----> Post daily transactions (CBTRN02C)
       |
       v
  INTCALC  -----> Calculate interest (CBACT04C)
       |
       v
  TRANBKP  -----> Backup transactions

Phase 4: Reporting & Consolidation
  COMBTRAN -----> Combine/merge transaction files
  CREASTMT -----> Generate account statements
  TRANREPT -----> Generate transaction report

Phase 5: Index & Reopen
  TRANIDX  -----> Build alternate indexes
  OPENFIL  -----> Reopen CICS files for online access
```

---

## 6. Data Lineage -- Entity Flow

### 6.1 Transaction Lifecycle

```
External Source
    |
    v
DALYTRAN (Daily Transaction File - sequential)
    |
    |-- CBTRN01C validates -----> (validation results)
    |
    |-- CBTRN02C posts ---------> TRANSACT (master VSAM)
    |                              |  + updates TCATBAL (category balances)
    |                              |  + updates ACCTDAT (account balance)
    |                              |
    |                              |---> CBACT04C reads TCATBAL
    |                              |       |---> computes interest
    |                              |       |---> writes interest TRANSACT records
    |                              |       |---> updates ACCTDAT balance
    |                              |
    |                              |---> CBTRN03C reads TRANSACT
    |                              |       |---> generates TRANREPT (report)
    |                              |
    |                              |---> CBSTM03A reads TRANSACT (via TRNXFILE)
    |                              |       |---> generates STMTFILE + HTMLFILE
    |                              |
    |                              |---> COTRN00C/01C browse/view online
    |
    |-- (rejected) --> DALYREJS (reject file)
```

### 6.2 Account Data Flow

```
ACCTFILE.jcl (IDCAMS REPRO) ---> ACCTDAT (VSAM KSDS)
                                    |
                                    |---> COACTVWC reads (view)
                                    |---> COACTUPC reads/writes (update)
                                    |---> COTRN02C reads (validate account for new txn)
                                    |---> COBIL00C reads/writes (bill payment updates balance)
                                    |---> CBTRN02C reads/writes (posting updates balance)
                                    |---> CBACT04C reads/writes (interest updates balance)
                                    |---> CBSTM03A reads (statement generation)
                                    |---> CBEXPORT reads (data export)
```

### 6.3 Customer Data Flow

```
CUSTFILE.jcl (IDCAMS REPRO) ---> CUSTDAT (VSAM KSDS)
                                    |
                                    |---> COACTVWC reads (display on account view)
                                    |---> COACTUPC reads/writes (update customer info)
                                    |---> COCRDSLC reads (display on card view)
                                    |---> CBSTM03A reads (statement header)
                                    |---> CBEXPORT reads (data export)
```

### 6.4 Card Data Flow

```
CARDFILE.jcl (IDCAMS REPRO) ---> CARDDAT (VSAM KSDS)
                                    |
                                    |---> COCRDLIC reads (browse cards)
                                    |---> COCRDSLC reads (view card detail)
                                    |---> COCRDUPC reads/writes (update card)
                                    |---> CBEXPORT reads (data export)

XREFFILE.jcl (IDCAMS REPRO) ---> CCXREF (VSAM KSDS) + CXACAIX (alternate index)
                                    |
                                    |---> COACTVWC reads CXACAIX (find card by account)
                                    |---> COACTUPC reads CXACAIX (find card by account)
                                    |---> COTRN02C reads CCXREF (validate card for txn)
                                    |---> COBIL00C reads CXACAIX (find card for payment)
                                    |---> CBTRN02C reads (batch posting validation)
                                    |---> CBACT04C reads (interest calc lookup)
                                    |---> CBSTM03A reads (statement generation)
                                    |---> CBEXPORT reads (data export)
```

---

## 7. Copybook Usage Matrix

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| COCOM01Y | X | X | X | X | X | X | X | X | X | X | X | X | X | X | X | X | X |
| COTTL01Y | | | X | X | X | X | X | X | X | X | X | X | X | X | X | X | X |
| CSDAT01Y | | | | X | X | X | X | X | X | X | X | X | X | X | X | X | X |
| CSMSG01Y | | | | X | X | X | X | X | X | X | X | X | X | X | X | X | X |
| CSMSG02Y | | | | X | X | | X | X | | | | | | | | | |
| CSUSR01Y | X | | X | X | X | X | X | X | | | | | | X | X | X | X |
| CVCRD01Y | | | | X | X | X | X | X | | | | | | | | | |
| CVACT01Y | | | | X | X | | | | | | X | | X | | | | |
| CVACT02Y | | | | X | | X | X | X | | | | | | | | | |
| CVACT03Y | | | | X | X | | | | | | X | | X | | | | |
| CVCUS01Y | | | | X | X | | X | X | | | | | | | | | |
| CVTRA05Y | | | | | | | | | X | X | X | X | X | | | | |
| COMEN02Y | | X | | | | | | | | | | | | | | | |
| COADM02Y | | | X | | | | | | | | | | | | | | |
| CSSTRPFY | | | | X | X | X | X | X | | | | | | | | | |
| CSSETATY | | | | | X | | | | | | | | | | | | |
| CSUTLDWY | | | | | X | | | | | | | | | | | | |
| CSUTLDPY | | | | | X | | | | | | | | | | | | |
| CSLKPCDY | | | | | X | | | | | | | | | | | | |

### Batch Copybook Usage

| Copybook | CBTRN02C | CBACT04C | CBTRN03C | CBSTM03A | CBTRN01C | CBEXPORT | CBIMPORT | CBACT01C |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| CVTRA05Y | X | X | X | | X | X | X | |
| CVTRA06Y | X | | | | X | | | |
| CVACT01Y | X | X | | X | X | X | X | X |
| CVACT02Y | | | | | X | X | X | |
| CVACT03Y | X | X | X | X | X | X | X | |
| CVCUS01Y | | | | X | X | X | X | |
| CVTRA01Y | | X | | | | | | |
| CVTRA02Y | | X | | | | | | |
| CVTRA03Y | | | X | | | | | |
| CVTRA04Y | | | X | | | | | |
| CVTRA07Y | | | X | | | | | |
| COSTM01 | | | | X | | | | |
| CUSTREC | | | | X | | | | |
| CVEXPORT | | | | | | X | X | |
| CODATECN | | | | | | | | X |
