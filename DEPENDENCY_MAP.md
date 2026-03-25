# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** Static analysis of COBOL COPY/XCTL/DATASET statements, JCL EXEC/DD references  
> **Purpose:** Call graph, data lineage, and inter-component dependencies for modernization planning.

---

## 1. Online Program Call Graph (CICS XCTL Transfers)

The online system uses CICS `EXEC CICS XCTL` (transfer control) to navigate between programs. All programs share state via `CARDDEMO-COMMAREA` (defined in `COCOM01Y.cpy`).

### 1.1 Navigation Flow Diagram

```
                          ┌─────────────┐
                          │  COSGN00C   │  Sign-on (CC00)
                          │  Entry Point│
                          └──────┬──────┘
                                 │
                    ┌────────────┴────────────┐
                    │ (Admin user)            │ (Regular user)
                    ▼                         ▼
             ┌─────────────┐          ┌─────────────┐
             │  COADM01C   │          │  COMEN01C   │
             │  Admin Menu │          │  Main Menu  │
             └──────┬──────┘          └──────┬──────┘
                    │                        │
        ┌───┬───┬──┴──┬───┬───┐    ┌───┬───┼───┬───┬───┬───┬───┬───┬───┬───┐
        │   │   │     │   │   │    │   │   │   │   │   │   │   │   │   │   │
        ▼   ▼   ▼     ▼   ▼   ▼    ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼
      USR USR USR   USR TRT TRT  AV  AU  CL  CD  CU  TL  TV  TA  RP  BP  PA
      00C 01C 02C   03C LIC UPC  WC  PC  IC  LC  PC  00  01  02  00  00  S0
```

### 1.2 Admin Menu → Program Routing (`COADM01C` via `COADM02Y`)

| Option | Label | Target Program | Domain |
|--------|-------|---------------|--------|
| 1 | User List (Security) | COUSR00C | User Admin |
| 2 | User Add (Security) | COUSR01C | User Admin |
| 3 | User Update (Security) | COUSR02C | User Admin |
| 4 | User Delete (Security) | COUSR03C | User Admin |
| 5 | Transaction Type List/Update (DB2) | COTRTLIC | Tran Type (optional) |
| 6 | Transaction Type Maintenance (DB2) | COTRTUPC | Tran Type (optional) |

### 1.3 Regular User Menu → Program Routing (`COMEN01C` via `COMEN02Y`)

| Option | Label | Target Program | Domain |
|--------|-------|---------------|--------|
| 1 | Account View | COACTVWC | Account |
| 2 | Account Update | COACTUPC | Account |
| 3 | Credit Card List | COCRDLIC | Card |
| 4 | Credit Card View | COCRDSLC | Card |
| 5 | Credit Card Update | COCRDUPC | Card |
| 6 | Transaction List | COTRN00C | Transaction |
| 7 | Transaction View | COTRN01C | Transaction |
| 8 | Transaction Add | COTRN02C | Transaction |
| 9 | Transaction Reports | CORPT00C | Reporting |
| 10 | Bill Payment | COBIL00C | Payment |
| 11 | Pending Authorization View | COPAUS0C | Authorization (optional) |

### 1.4 Cross-Program XCTL Transfers

| Source Program | Target Program | Trigger |
|---------------|---------------|---------|
| COSGN00C | COADM01C | Admin user login successful |
| COSGN00C | COMEN01C | Regular user login successful |
| COMEN01C | COSGN00C | PF3 (return to sign-on) |
| COMEN01C | (any option program) | User selects menu option |
| COADM01C | COSGN00C | PF3 (return to sign-on) |
| COADM01C | (any admin option program) | Admin selects option |
| COCRDLIC | COCRDSLC | User selects card for detail view (`S`) |
| COCRDLIC | COCRDUPC | User selects card for update (`U`) |
| COCRDLIC | COMEN01C | PF3 (return to menu) |
| COCRDSLC | COCRDLIC | PF3 (return to list) |
| COCRDSLC | COMEN01C | PF3 (return to menu, if no list context) |
| COCRDUPC | COCRDLIC | PF3 (return to list) |
| COACTVWC | COMEN01C | PF3 (return to menu) |
| COACTUPC | COMEN01C | PF3 (return to menu) |
| COTRN00C | COTRN01C | User selects transaction for view |
| COTRN00C | COTRN02C | User selects transaction add |
| COTRN00C | COMEN01C | PF3 (return to menu) |
| COTRN01C | COTRN00C | PF3 (return to list) |
| COTRN02C | COTRN00C | PF3 (return to list) |
| COBIL00C | COMEN01C | PF3 (return to menu) |
| CORPT00C | COMEN01C | PF3 (return to menu) |
| COUSR00C | COUSR01C | User selects add |
| COUSR00C | COUSR02C | User selects update |
| COUSR00C | COUSR03C | User selects delete |
| COUSR00C | COADM01C | PF3 (return to admin menu) |
| COUSR01C | COADM01C | PF3 (return to admin menu) |
| COUSR02C | COADM01C | PF3 (return to admin menu) |
| COUSR03C | COADM01C | PF3 (return to admin menu) |
| COPAUS0C | COPAUS1C | Select authorization for detail |
| COPAUS1C | COPAUS0C | PF3 (return to summary) |

### 1.5 Batch Program Call Chain

| Caller | Callee | Mechanism |
|--------|--------|-----------|
| CBSTM03A | CBSTM03B | `CALL 'CBSTM03B'` (subroutine) |
| COBSWAIT | MVSWAIT | `CALL` to assembler wait utility |
| CBACT01C | COBDATFT | `CALL` to assembler date formatter |

---

## 2. CICS Program → VSAM File Access (Data Lineage)

### 2.1 Online Programs

| Program | VSAM File(s) Accessed | Operations | Access Pattern |
|---------|----------------------|------------|----------------|
| COSGN00C | `USRSEC` | READ | Authenticate user by ID |
| COMEN01C | — | — | Menu only (no file I/O) |
| COADM01C | — | — | Menu only (no file I/O) |
| COACTVWC | `ACCTDAT`, `CUSTDAT`, `CARDAIX` (alt index), `CXACAIX` (xref alt index) | READ | View account + related customer/card data |
| COACTUPC | `ACCTDAT`, `CUSTDAT`, `CARDXREF` (via `CXACAIX`) | READ, REWRITE | Update account details |
| COCRDLIC | `CARDDAT`, `CARDAIX` (alt index) | READ, STARTBR, READNEXT | Browse card list with filter |
| COCRDSLC | `CARDDAT`, `CUSTDAT` | READ | View card detail |
| COCRDUPC | `CARDDAT`, `CUSTDAT`, `ACCTDAT` | READ, REWRITE | Update card details |
| COTRN00C | `TRANSACT` | READ, STARTBR, READNEXT, READPREV | Browse transaction list |
| COTRN01C | `TRANSACT` | READ | View single transaction |
| COTRN02C | `TRANSACT`, `CARDXREF`, `CARDDAT` | READ, WRITE | Add new transaction |
| CORPT00C | — | — | Submits batch report (no direct file I/O) |
| COBIL00C | `ACCTDAT`, `TRANSACT`, `CXACAIX` | READ, WRITE, REWRITE | Process bill payment |
| COUSR00C | `USRSEC` | READ, STARTBR, READNEXT, READPREV | Browse user list |
| COUSR01C | `USRSEC` | WRITE | Add new user |
| COUSR02C | `USRSEC` | READ, REWRITE | Update user |
| COUSR03C | `USRSEC` | READ, DELETE | Delete user |

### 2.2 Batch Programs

| Program | Input Files | Output Files | Operations |
|---------|------------|-------------|------------|
| CBTRN01C | `DALYTRAN`, `CUSTFILE`, `XREFFILE`, `CARDFILE`, `ACCTFILE`, `TRANSACT` | — (validation only) | Read-only validation of daily transactions |
| CBTRN02C | `DALYTRAN`, `XREFFILE` | `TRANSACT`, `DALYREJS`, `ACCTFILE` (I-O), `TCATBALF` (I-O) | **Core posting**: validates, posts transactions, updates account balances |
| CBTRN03C | `TRANSACT`, `XREFFILE`, `TRANTYPE`, `TRANCATG`, `DATEPARM` | `TRANREPT` (report) | Print transaction detail report |
| CBACT01C | `ACCTDAT` | — (print) | Read & print account data |
| CBACT02C | `CARDDAT` | — (print) | Read & print card data |
| CBACT03C | `XREFFILE` | — (print) | Read & print cross-reference data |
| CBACT04C | `TCATBALF`, `DISCGRP`, `ACCTDAT` | `ACCTDAT` (I-O), `TCATBALF` (I-O) | **Interest calculation**: apply rates, update balances |
| CBCUS01C | `CUSTDAT` | — (print) | Read & print customer data |
| CBSTM03A | `TRNXFILE`, `XREFFILE`, `CUSTFILE`, `ACCTFILE` | `STMTFILE`, `HTMLFILE` | **Statement generation**: produce text + HTML statements |
| CBSTM03B | (called by CBSTM03A) | (called by CBSTM03A) | Subroutine for file processing |
| CBEXPORT | `CUSTFILE`, `ACCTFILE`, `XREFFILE`, `TRANSACT`, `CARDFILE` | `EXPFILE` | Export all data to single sequential file |
| CBIMPORT | `EXPFILE` | `CUSTOUT`, `ACCTOUT`, `XREFOUT`, `TRNXOUT`, `CARDOUT`, `ERROUT` | Import data from export file |

---

## 3. JCL Job → Program & Dataset Lineage

### 3.1 Core Batch Cycle (Execution Order)

```
 ┌──────────────────────────────────────────────────────────────────────┐
 │                    NIGHTLY BATCH CYCLE                               │
 │                                                                      │
 │  1. CLOSEFIL ──→ Close CICS files (SDSF)                           │
 │       │                                                              │
 │  2. ACCTFILE ──→ Refresh ACCTDATA.VSAM.KSDS from PS (IDCAMS)       │
 │  3. CARDFILE ──→ Refresh CARDDATA.VSAM.KSDS from PS (IDCAMS)       │
 │  4. CUSTFILE ──→ Refresh CUSTDATA.VSAM.KSDS from PS (IDCAMS)       │
 │  5. XREFFILE ──→ Refresh CARDXREF.VSAM.KSDS from PS (IDCAMS)      │
 │  6. TRANFILE ──→ Refresh TRANSACT.VSAM.KSDS from PS (IDCAMS)      │
 │       │                                                              │
 │  7. POSTTRAN ──→ CBTRN02C: Post daily transactions                  │
 │       │          Reads:  DALYTRAN, XREFFILE                         │
 │       │          Writes: TRANSACT, DALYREJS, ACCTFILE, TCATBALF     │
 │       │                                                              │
 │  8. INTCALC  ──→ CBACT04C: Calculate interest                      │
 │       │          Reads:  DISCGRP, TCATBALF                          │
 │       │          Writes: ACCTFILE, TCATBALF                         │
 │       │                                                              │
 │  9. TRANBKP  ──→ Backup transaction file (IDCAMS)                  │
 │       │                                                              │
 │ 10. COMBTRAN ──→ Sort/combine transactions (SORT)                   │
 │       │                                                              │
 │ 11. CREASTMT ──→ CBSTM03A: Generate statements                     │
 │       │          Reads:  TRANSACT, XREFFILE, CUSTFILE, ACCTFILE     │
 │       │          Writes: STMTFILE, HTMLFILE                         │
 │       │                                                              │
 │ 12. TRANIDX  ──→ Rebuild alternate indexes (IDCAMS)                │
 │       │                                                              │
 │ 13. OPENFIL  ──→ Reopen CICS files (SDSF)                          │
 └──────────────────────────────────────────────────────────────────────┘
```

### 3.2 JCL → COBOL Program Mapping

| JCL Job | Step | COBOL Program | Utility |
|---------|------|--------------|---------|
| POSTTRAN.jcl | STEP15 | CBTRN02C | — |
| INTCALC.jcl | STEP15 | CBACT04C | — |
| CREASTMT.JCL | STEP040 | CBSTM03A | SORT (pre-step) |
| TRANREPT.jcl | STEP10R | CBTRN03C | SORT (pre-step) |
| READACCT.jcl | STEP05 | CBACT01C | — |
| READCARD.jcl | STEP05 | CBACT02C | — |
| READXREF.jcl | STEP05 | CBACT03C | — |
| READCUST.jcl | STEP05 | CBCUS01C | — |
| CBEXPORT.jcl | STEP02 | CBEXPORT | IDCAMS (pre-step) |
| CBIMPORT.jcl | STEP01 | CBIMPORT | — |
| WAITSTEP.jcl | WAIT | COBSWAIT | — |

### 3.3 JCL → Dataset References

| Dataset (DSN) | Type | Jobs That Read | Jobs That Write/Create |
|---------------|------|---------------|----------------------|
| `ACCTDATA.VSAM.KSDS` | KSDS | POSTTRAN, INTCALC, CREASTMT, READACCT | ACCTFILE |
| `CARDDATA.VSAM.KSDS` | KSDS | — | CARDFILE |
| `CUSTDATA.VSAM.KSDS` | KSDS | CREASTMT, READCUST | CUSTFILE |
| `CARDXREF.VSAM.KSDS` | KSDS | CREASTMT, READXREF | XREFFILE |
| `TRANSACT.VSAM.KSDS` | KSDS | CREASTMT, TRANREPT, COMBTRAN | POSTTRAN, TRANFILE |
| `DALYTRAN` (sequential) | PS | POSTTRAN | (external feed) |
| `DALYREJS` (rejects) | KSDS | — | POSTTRAN |
| `TCATBALF` (cat balance) | KSDS | INTCALC | POSTTRAN, INTCALC |
| `DISCGRP` (disclosure) | KSDS | INTCALC | DISCGRP (setup) |
| `TRANTYPE` (types) | KSDS | TRANREPT | TRANTYPE (setup) |
| `TRANCATG` (categories) | KSDS | TRANREPT | TRANCATG (setup) |
| `USRSEC.VSAM.KSDS` | KSDS | — | DUSRSECJ |
| `TRXFL.SEQ` (sorted tran) | PS | CREASTMT (step 2) | CREASTMT (step 1: SORT) |
| `TRXFL.VSAM.KSDS` | KSDS | CREASTMT (step 4) | CREASTMT (step 2) |
| `STATEMNT.PS` | PS | TXT2PDF1 | CREASTMT |
| `TRANTYPE.BKUP` | GDG | — | DEFGDGD |
| `TRANCATG.PS.BKUP` | GDG | — | DEFGDGD |
| `DISCGRP.BKUP` | GDG | — | DEFGDGD |

---

## 4. Copybook Dependency Matrix

### 4.1 Which Programs Use Which Copybooks

| Copybook | Used By (Core Programs) |
|----------|------------------------|
| **COCOM01Y** (COMMAREA) | ALL 17 online programs |
| **COTTL01Y** (titles) | ALL 17 online programs |
| **CSDAT01Y** (date) | ALL 17 online programs |
| **CSMSG01Y** (messages) | ALL 17 online programs |
| **CSUSR01Y** (user record) | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN02C, COBIL00C, CORPT00C, COUSR00C–03C |
| **CVACT01Y** (account) | COACTVWC, COACTUPC, CBTRN01C, CBACT01C, CBACT04C, CBSTM03A |
| **CVACT02Y** (card) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, CBTRN01C, CBACT02C |
| **CVACT03Y** (xref) | COACTVWC, COACTUPC, COTRN02C |
| **CVCUS01Y** (customer) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A |
| **CVCRD01Y** (card work) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CVTRA05Y** (transaction) | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN01C, CBTRN02C |
| **CVTRA06Y** (daily tran) | CBTRN01C, CBTRN02C |
| **CVTRA07Y** (report) | CBTRN03C |
| **COMEN02Y** (menu opts) | COMEN01C |
| **COADM02Y** (admin opts) | COADM01C |
| **CSMSG02Y** (abend) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COTRN02C |
| **CSUTLDWY** (date util WS) | COACTUPC, COCRDUPC |
| **COSTM01** (stmt layout) | CBSTM03A, CBSTM03B |
| **CVEXPORT** (export) | CBEXPORT, CBIMPORT |

### 4.2 Copybook Fan-Out (number of programs depending on each)

| Rank | Copybook | # Dependents | Risk Level |
|------|----------|-------------|------------|
| 1 | COCOM01Y | 17+ | **Critical** — Any change impacts all online programs |
| 2 | COTTL01Y | 17+ | Low — Display only |
| 3 | CSDAT01Y | 17+ | Low — Date formatting only |
| 4 | CSMSG01Y | 17+ | Low — Message constants only |
| 5 | CSUSR01Y | 14+ | **High** — Security record used everywhere |
| 6 | CVACT02Y | 8+ | **High** — Card record, core entity |
| 7 | CVACT01Y | 7+ | **High** — Account record, core entity |
| 8 | CVTRA05Y | 7+ | **High** — Transaction record, highest volume entity |
| 9 | CVCUS01Y | 6+ | Medium — Customer record |
| 10 | CVCRD01Y | 4 | Medium — Card work area |

---

## 5. Module Dependency Clusters

### 5.1 Account Management Cluster

```
Programs:  COACTVWC, COACTUPC
Copybooks: CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y
Files:     ACCTDAT, CARDDAT, CUSTDAT, CARDXREF (CXACAIX)
```

### 5.2 Card Management Cluster

```
Programs:  COCRDLIC, COCRDSLC, COCRDUPC
Copybooks: CVACT02Y, CVCRD01Y, CVCUS01Y
Files:     CARDDAT, CARDAIX, CUSTDAT
```

### 5.3 Transaction Cluster

```
Programs:  COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBTRN03C
Copybooks: CVTRA05Y, CVTRA06Y, CVTRA07Y, CVTRA01Y, CVTRA03Y, CVTRA04Y
Files:     TRANSACT, DALYTRAN, DALYREJS, TCATBALF, TRANTYPE, TRANCATG, CARDXREF
```

### 5.4 Payment & Billing Cluster

```
Programs:  COBIL00C, CBACT04C
Copybooks: CVACT01Y, CVTRA01Y, CVTRA02Y, CVTRA05Y
Files:     ACCTDAT, TRANSACT, TCATBALF, DISCGRP, CXACAIX
```

### 5.5 Statement & Reporting Cluster

```
Programs:  CORPT00C, CBSTM03A, CBSTM03B, CBTRN03C
Copybooks: COSTM01, CVTRA07Y, CVACT01Y, CVCUS01Y
Files:     TRANSACT (sorted), XREFFILE, CUSTFILE, ACCTFILE, STMTFILE
```

### 5.6 User Administration Cluster

```
Programs:  COUSR00C, COUSR01C, COUSR02C, COUSR03C
Copybooks: CSUSR01Y
Files:     USRSEC
```

### 5.7 Data Migration Cluster

```
Programs:  CBEXPORT, CBIMPORT
Copybooks: CVEXPORT
Files:     All master files (CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE) + EXPFILE
```

---

## 6. External Interface Points

| Interface | Type | Programs | Direction | Notes |
|-----------|------|----------|-----------|-------|
| 3270 Terminal | BMS screens | All 17 online programs | Bidirectional | → Web UI in Java |
| Daily Transaction Feed | Sequential file | CBTRN02C | Inbound | → Message queue / API in Java |
| Statement Output | Print file | CBSTM03A | Outbound | → PDF generation service |
| Transaction Report | Print file | CBTRN03C | Outbound | → Reporting service |
| Export File | Sequential file | CBEXPORT/CBIMPORT | Bidirectional | → REST API / ETL |
| MQ Queues (optional) | IBM MQ | COPAUA0C, COACCT01, CODATE01 | Bidirectional | → JMS / Kafka in Java |
| IMS Database (optional) | IMS DB | COPAUA0C, COPAUS0C-2C, PAUDBLOD/PAUDBUNL | Bidirectional | → JPA/RDBMS in Java |
| DB2 (optional) | SQL | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT | Bidirectional | → JPA/JDBC in Java |
| FTP (scripts) | File transfer | scripts/*.sh | Bidirectional | → CI/CD pipeline |
