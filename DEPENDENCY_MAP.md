# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo | **Source:** Static analysis of CALL/XCTL, COPY, DATASET, and SELECT/ASSIGN statements

---

## 1. Program Call Graph (Online CICS Programs)

### 1.1 XCTL Transfer Graph (CICS Program-to-Program)

CICS online programs navigate via `EXEC CICS XCTL` (transfer control). The COMMAREA (`COCOM01Y`) carries context between programs.

```
                          +-----------+
                          | COSGN00C  |  (CC00 - Sign-on)
                          +-----+-----+
                                |
                   +------------+------------+
                   |                         |
            (Admin user)              (Regular user)
                   |                         |
            +------v------+          +-------v-------+
            | COADM01C    |          | COMEN01C      |  (Main Menu)
            | (Admin Menu)|          +-------+-------+
            +------+------+                  |
                   |              +----------+-----------+--------+--------+--------+--------+--------+--------+--------+--------+
                   |              |          |           |        |        |        |        |        |        |        |        |
            +------+------+  +---v---+  +---v---+  +---v---+ +--v--+ +--v--+ +--v--+ +--v--+ +--v--+ +--v--+ +--v--+ +--------+
            |   Options:  |  |COACTV | |COACTU | |COCRDL | |COCRD| |COCRD| |COTRN| |COTRN| |COTRN| |CORPT| |COBIL| |COPAUS |
            | COUSR00C    |  | WC    | | PC    | | IC    | | SLC | | UPC | | 00C | | 01C | | 02C | | 00C | | 00C | | 0C    |
            | COUSR01C    |  +-------+ +-------+ +--+----+ +-----+ +-----+ +--+--+ +--+--+ +--+--+ +--+--+ +--+--+ +--------+
            | COUSR02C    |                          |                        |       |       |       |       |
            | COUSR03C    |                     XCTL to                  XCTL to  XCTL to XCTL to XCTL to XCTL to
            | COTRTLIC*   |                    COCRDSLC                  COTRN01C  (back)  (back) (back)  (back)
            | COTRTUPC*   |                    COCRDUPC                  COTRN02C
            +--------------+                   (back to menu)            (back to menu)

* = Optional DB2 module programs
```

### 1.2 Detailed XCTL Transfers

| Source Program | Target Program | Condition |
|---------------|---------------|-----------|
| COSGN00C | COADM01C | User type = Admin ("A") |
| COSGN00C | COMEN01C | User type = Regular ("U") |
| COMEN01C | COACTVWC | Menu option 1 (Account View) |
| COMEN01C | COACTUPC | Menu option 2 (Account Update) |
| COMEN01C | COCRDLIC | Menu option 3 (Credit Card List) |
| COMEN01C | COCRDSLC | Menu option 4 (Credit Card View) |
| COMEN01C | COCRDUPC | Menu option 5 (Credit Card Update) |
| COMEN01C | COTRN00C | Menu option 6 (Transaction List) |
| COMEN01C | COTRN01C | Menu option 7 (Transaction View) |
| COMEN01C | COTRN02C | Menu option 8 (Transaction Add) |
| COMEN01C | CORPT00C | Menu option 9 (Transaction Reports) |
| COMEN01C | COBIL00C | Menu option 10 (Bill Payment) |
| COMEN01C | COPAUS0C | Menu option 11 (Pending Auth View) |
| COADM01C | COUSR00C | Admin option 1 (User List) |
| COADM01C | COUSR01C | Admin option 2 (User Add) |
| COADM01C | COUSR02C | Admin option 3 (User Update) |
| COADM01C | COUSR03C | Admin option 4 (User Delete) |
| COADM01C | COTRTLIC | Admin option 5 (Tran Type List -- DB2) |
| COADM01C | COTRTUPC | Admin option 6 (Tran Type Maint -- DB2) |
| COCRDLIC | COCRDSLC | Select card for detail view |
| COCRDLIC | COCRDUPC | Select card for update |
| COCRDLIC | COMEN01C | PF3 (return to menu) |
| COACTVWC | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COACTUPC | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COCRDSLC | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COCRDUPC | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COTRN00C | CDEMO-TO-PROGRAM | PF3 / select transaction |
| COTRN01C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COTRN02C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| CORPT00C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COBIL00C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COUSR00C | CDEMO-TO-PROGRAM | PF3 / select user |
| COUSR01C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COUSR02C | CDEMO-TO-PROGRAM | PF3 (return to caller) |
| COUSR03C | CDEMO-TO-PROGRAM | PF3 (return to caller) |

> `CDEMO-TO-PROGRAM` is a dynamic field in the COMMAREA set by the calling program before XCTL, enabling flexible return navigation.

---

## 2. Program Call Graph (Batch Programs)

### 2.1 CALL Statements

| Calling Program | Called Program | Mechanism | Purpose |
|----------------|---------------|-----------|---------|
| CBACT01C | COBDATFT | `CALL 'COBDATFT'` | Date format conversion (ASM routine) |
| CBACT01C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBACT02C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBACT03C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBACT04C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBCUS01C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBTRN01C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBTRN02C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBTRN03C | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBSTM03A | CBSTM03B | `CALL 'CBSTM03B'` | Statement print subroutine (12 call sites) |
| CBSTM03A | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBEXPORT | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| CBIMPORT | CEE3ABD | `CALL 'CEE3ABD'` | LE abend handler |
| COBSWAIT | MVSWAIT | `CALL 'MVSWAIT'` | MVS wait routine (ASM) |
| CSUTLDTC | CEEDAYS | `CALL 'CEEDAYS'` | LE date intrinsic |
| COTRN02C | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation (2 call sites) |
| CORPT00C | CSUTLDTC | `CALL 'CSUTLDTC'` | Date validation (2 call sites) |

### 2.2 Batch Call Graph

```
JCL: POSTTRAN
  +-> CBTRN01C (Validate daily transactions)
  |     +-> CEE3ABD (abend)
  |     Files: DALYTRAN(R), CUSTFILE(R), XREFFILE(R), CARDFILE(R), ACCTFILE(R), TRANFILE(R/W)
  |
  +-> CBTRN02C (Post validated transactions)
        +-> CEE3ABD (abend)
        Files: DALYTRAN(R), TRANFILE(R/W), XREFFILE(R), DALYREJS(W), ACCTFILE(R/W), TCATBALF(R/W)

JCL: INTCALC
  +-> CBACT04C (Calculate interest on accounts)
        +-> CEE3ABD (abend)
        Files: ACCTFILE(R/W), DISCGRP(R), TRANFILE(W), TCATBALF(R)

JCL: CREASTMT
  +-> CBSTM03A (Generate account statements)
        +-> CBSTM03B (Print subroutine, called 12 times)
        Files: ACCTFILE(R), XREFFILE(R), TRANFILE(R), STMTFILE(W)

JCL: TRANREPT
  +-> CBTRN03C (Generate transaction reports)
        +-> CEE3ABD (abend)
        Files: TRANSACT(R), TRANTYPE(R), TRANCATG(R), DALYREPT(W)

JCL: CBEXPORT
  +-> CBEXPORT (Export all VSAM files to sequential)
        +-> CEE3ABD (abend)
        Files: CUSTFILE(R), ACCTFILE(R), XREFFILE(R), TRANSACT(R), CARDFILE(R), EXPFILE(W)

JCL: CBIMPORT
  +-> CBIMPORT (Import sequential to VSAM files)
        +-> CEE3ABD (abend)
        Files: EXPFILE(R), CUSTOUT(W), ACCTOUT(W), XREFOUT(W), TRNXOUT(W), CARDOUT(W), ERROUT(W)

Data Refresh Jobs:
  ACCTFILE.jcl -> CBACT01C -> COBDATFT (ASM) -> ACCTDAT
  CARDFILE.jcl -> CBACT02C -> CARDDAT
  CUSTFILE.jcl -> CBCUS01C -> CUSTDAT
  XREFFILE.jcl -> CBACT03C -> CARDXREF
```

---

## 3. Copybook Dependencies

### 3.1 Copybook Usage Matrix

| Copybook | Programs Using It |
|----------|------------------|
| **COCOM01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** | COSGN00C, COACTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CVCUS01Y** | CBTRN01C, CBIMPORT |
| **CVACT01Y** | CBTRN01C, CBIMPORT, COBIL00C, COACTUPC |
| **CVACT02Y** | CBTRN01C, CBIMPORT |
| **CVACT03Y** | CBTRN01C, CBIMPORT, COBIL00C, COACTUPC |
| **CVTRA05Y** | CBTRN01C, CBIMPORT, COBIL00C, COTRN01C |
| **CVTRA06Y** | CBTRN01C |
| **CVEXPORT** | CBIMPORT, CBEXPORT |
| **CVCRD01Y** | COACTUPC |
| **CSLKPCDY** | COACTUPC |
| **CSUTLDWY** | COACTUPC |
| **CSSETATY** | COACTUPC (30 COPY REPLACING instances) |
| **CSMSG02Y** | COACTUPC |
| **COMEN02Y** | COMEN01C |
| **COADM02Y** | COADM01C |
| **CSSTRPFY** | Multiple online programs (PF-key mapping) |
| **CSUTLDPY** | COACTUPC, COCRDUPC (date validation procedure) |
| **DFHAID** | All online CICS programs (CICS system copybook) |
| **DFHBMSCA** | All online CICS programs (CICS system copybook) |

### 3.2 Copybook Fan-Out (Most Widely Used)

| Rank | Copybook | Used By (Count) | Purpose |
|------|----------|-----------------|---------|
| 1 | COCOM01Y | 17 programs | COMMAREA (inter-program communication) |
| 2 | COTTL01Y | 16 programs | Screen title |
| 3 | CSDAT01Y | 16 programs | Date/time working storage |
| 4 | CSMSG01Y | 16 programs | Common messages |
| 5 | DFHAID | 17 programs | CICS AID key definitions |
| 6 | DFHBMSCA | 17 programs | CICS BMS attribute constants |
| 7 | CSUSR01Y | 6 programs | User security record |
| 8 | CVACT01Y | 4 programs | Account record |
| 9 | CVTRA05Y | 4 programs | Transaction record |
| 10 | CVACT03Y | 4 programs | Card cross-reference record |

---

## 4. Data Lineage (File I/O by Program)

### 4.1 Online CICS Programs -- VSAM File Access

| Program | File | DD/Dataset Name | Access | Operations |
|---------|------|----------------|--------|------------|
| COSGN00C | User Security | USRSEC | Read | READ (credential validation) |
| COACTVWC | Account | ACCTDAT | Read | READ |
| COACTVWC | Customer | CUSTDAT | Read | READ |
| COACTVWC | Card X-Ref | CARDXREF | Read | READ |
| COACTUPC | Account | ACCTDAT | Read/Write | READ, REWRITE |
| COACTUPC | Customer | CUSTDAT | Read | READ |
| COACTUPC | Card X-Ref | CXACAIX | Read | READ (alternate index) |
| COCRDLIC | Card | CARDDAT | Read | READ, STARTBR, READNEXT |
| COCRDLIC | Card (AIX) | CARDAIX | Read | READ (alternate index by account) |
| COCRDSLC | Card | CARDDAT | Read | READ |
| COCRDSLC | Card (AIX) | CARDAIX | Read | READ (alternate index) |
| COCRDUPC | Card | CARDDAT | Read/Write | READ, REWRITE |
| COTRN00C | Transaction | TRANSACT | Read | READ, STARTBR, READNEXT |
| COTRN01C | Transaction | TRANSACT | Read | READ |
| COTRN02C | Card X-Ref | CXACAIX | Read | READ (alternate index) |
| COTRN02C | Card X-Ref | CCXREF | Read | READ |
| COTRN02C | Transaction | TRANSACT | Read/Write | READ, WRITE |
| COBIL00C | Account | ACCTDAT | Read/Write | READ, REWRITE |
| COBIL00C | Card X-Ref | CXACAIX | Read | READ (alternate index) |
| COBIL00C | Transaction | TRANSACT | Read/Write | READ, WRITE, STARTBR, READNEXT, READPREV |
| COUSR00C | User Security | USRSEC | Read | READ, STARTBR, READNEXT |
| COUSR01C | User Security | USRSEC | Write | WRITE |
| COUSR02C | User Security | USRSEC | Read/Write | READ, REWRITE |
| COUSR03C | User Security | USRSEC | Read/Delete | READ, DELETE |

### 4.2 Batch Programs -- File I/O

| Program | File (DD Name) | Assign To | Access | Operations |
|---------|---------------|-----------|--------|------------|
| **CBTRN01C** | | | | |
| | Daily Transactions | DALYTRAN | Read | Sequential read |
| | Customer File | CUSTFILE | Read | Random read |
| | Card X-Ref File | XREFFILE | Read | Random read |
| | Card File | CARDFILE | Read | Random read |
| | Account File | ACCTFILE | Read | Random read |
| | Transaction File | TRANFILE | Read/Write | Random read + write |
| **CBTRN02C** | | | | |
| | Daily Transactions | DALYTRAN | Read | Sequential read |
| | Transaction File | TRANFILE | Read/Write | Random read + write |
| | Card X-Ref File | XREFFILE | Read | Random read |
| | Daily Rejects | DALYREJS | Write | Sequential write |
| | Account File | ACCTFILE | Read/Write | Random read + update |
| | Category Balance | TCATBALF | Read/Write | Random read + update |
| **CBACT01C** | | | | |
| | Account File | ACCTFILE | Write | Load/reload VSAM |
| **CBACT02C** | | | | |
| | Card File | CARDFILE | Write | Load/reload VSAM |
| **CBACT03C** | | | | |
| | Card X-Ref | XREFFILE | Write | Load/reload VSAM |
| **CBACT04C** | | | | |
| | Account File | ACCTFILE | Read/Write | Read + update balance |
| | Disclosure Group | DISCGRP | Read | Interest rate lookup |
| | Transaction File | TRANFILE | Write | Write interest transactions |
| | Category Balance | TCATBALF | Read | Balance lookup |
| **CBCUS01C** | | | | |
| | Customer File | CUSTFILE | Write | Load/reload VSAM |
| **CBTRN03C** | | | | |
| | Transaction File | TRANSACT | Read | Sequential read |
| | Transaction Type | TRANTYPE | Read | Lookup |
| | Transaction Category | TRANCATG | Read | Lookup |
| | Daily Report | DALYREPT | Write | Print output |
| **CBSTM03A** | | | | |
| | Account File | ACCTFILE | Read | Sequential read |
| | Card X-Ref | XREFFILE | Read | Random read |
| | Transaction File | TRANFILE | Read | Random read |
| | Statement File | STMTFILE | Write | Print output |
| **CBEXPORT** | | | | |
| | Customer File | CUSTFILE | Read | Sequential read |
| | Account File | ACCTFILE | Read | Sequential read |
| | Card X-Ref | XREFFILE | Read | Sequential read |
| | Transaction File | TRANSACT | Read | Sequential read |
| | Card File | CARDFILE | Read | Sequential read |
| | Export File | EXPFILE | Write | Sequential write |
| **CBIMPORT** | | | | |
| | Export File | EXPFILE | Read | Sequential read |
| | Customer Output | CUSTOUT | Write | Write individual files |
| | Account Output | ACCTOUT | Write | Write individual files |
| | Card X-Ref Output | XREFOUT | Write | Write individual files |
| | Transaction Output | TRNXOUT | Write | Write individual files |
| | Card Output | CARDOUT | Write | Write individual files |
| | Error Output | ERROUT | Write | Error records |

---

## 5. Batch Processing Data Flow

### 5.1 Nightly Batch Cycle (Recommended Order)

```
Phase 1: Preparation
  CLOSEFIL.jcl -----> Close CICS-managed VSAM files
                        |
Phase 2: Data Refresh (parallel eligible)
  ACCTFILE.jcl -----> CBACT01C ---> ACCTDAT (reload)
  CARDFILE.jcl -----> CBACT02C ---> CARDDAT (reload)
  CUSTFILE.jcl -----> CBCUS01C ---> CUSTDAT (reload)
  XREFFILE.jcl -----> CBACT03C ---> CARDXREF (reload)
  DUSRSECJ.jcl -----> (IDCAMS) ---> USRSEC (reload)
                        |
Phase 3: Transaction Processing (sequential)
  POSTTRAN.jcl
    Step 1: CBTRN01C
      DALYTRAN ------R-----> Validate ------W-----> TRANSACT
      CUSTDAT  ------R----/                 \---W--> (rejected to DALYREJS)
      XREFFILE ------R----/
      CARDFILE ------R----/
      ACCTFILE ------R----/
    Step 2: CBTRN02C
      DALYTRAN ------R-----> Post ------W---------> TRANSACT
      XREFFILE ------R----/         \---W---------> ACCTFILE (balance update)
      ACCTFILE ------R/W--/         \---W---------> TCATBALF (category balance)
                                    \---W---------> DALYREJS (rejects)
                        |
Phase 4: Interest Calculation
  INTCALC.jcl -----> CBACT04C
      ACCTFILE ------R/W---> Calculate interest ---> ACCTFILE (update balance)
      DISCGRP  ------R----/                    \---> TRANSACT (interest transactions)
      TCATBALF ------R----/
                        |
Phase 5: Backup
  TRANBKP.jcl -----> (IDCAMS REPRO) ---> Backup TRANSACT to GDG
                        |
Phase 6: Reporting (parallel eligible)
  COMBTRAN.jcl -----> (SORT/MERGE) ---> Combine transaction files
  CREASTMT.JCL ----> CBSTM03A/B ---> Statement output
  TRANREPT.jcl -----> CBTRN03C ---> Daily transaction report
                        |
Phase 7: Index Rebuild
  TRANIDX.jcl -----> (IDCAMS) ---> Rebuild alternate indexes
                        |
Phase 8: Reopen
  OPENFIL.jcl -----> Re-open CICS-managed VSAM files
```

### 5.2 File Dependency Matrix

| VSAM File | Written By (Batch) | Written By (Online) | Read By (Batch) | Read By (Online) |
|-----------|--------------------|--------------------|-----------------|--------------------|
| ACCTDAT | CBACT01C, CBTRN02C, CBACT04C | COACTUPC, COBIL00C | CBTRN01C, CBTRN02C, CBACT04C, CBSTM03A, CBEXPORT | COACTVWC, COACTUPC, COBIL00C |
| CARDDAT | CBACT02C | COCRDUPC | CBTRN01C, CBEXPORT | COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDAT | CBCUS01C | -- | CBTRN01C, CBEXPORT | COACTVWC, COACTUPC |
| CARDXREF | CBACT03C | -- | CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT | COACTVWC, COACTUPC, COTRN02C, COBIL00C |
| TRANSACT | CBTRN01C, CBTRN02C, CBACT04C | COTRN02C, COBIL00C | CBTRN03C, CBSTM03A, CBEXPORT | COTRN00C, COTRN01C, COBIL00C |
| USRSEC | (IDCAMS load) | COUSR01C, COUSR02C, COUSR03C | -- | COSGN00C, COUSR00C |
| TCATBALF | CBTRN02C | -- | CBACT04C | -- |
| DISCGRP | (IDCAMS load) | -- | CBACT04C | -- |
| TRANTYPE | (IDCAMS load) | -- | CBTRN03C | -- |
| TRANCATG | (IDCAMS load) | -- | CBTRN03C | -- |
| DALYTRAN | (External feed) | -- | CBTRN01C, CBTRN02C | -- |
| DALYREJS | CBTRN02C | -- | -- | -- |

---

## 6. BMS Map-to-Program Mapping

| BMS Map | Mapset | Program | Copybook (cpy-bms/) |
|---------|--------|---------|-------------------|
| COSGN00 | COSGN00 | COSGN00C | COSGN00.CPY |
| COMEN01 | COMEN01 | COMEN01C | COMEN01.CPY |
| COADM01 | COADM01 | COADM01C | COADM01.CPY |
| COACTVW | COACTVW | COACTVWC | COACTVW.CPY |
| COACTUP | COACTUP | COACTUPC | COACTUP.CPY |
| COCRDLI | COCRDLI | COCRDLIC | COCRDLI.CPY |
| COCRDSL | COCRDSL | COCRDSLC | COCRDSL.CPY |
| COCRDUP | COCRDUP | COCRDUPC | COCRDUP.CPY |
| COTRN00 | COTRN00 | COTRN00C | COTRN00.CPY |
| COTRN01 | COTRN01 | COTRN01C | COTRN01.CPY |
| COTRN02 | COTRN02 | COTRN02C | COTRN02.CPY |
| CORPT00 | CORPT00 | CORPT00C | CORPT00.CPY |
| COBIL00 | COBIL00 | COBIL00C | COBIL00.CPY |
| COUSR00 | COUSR00 | COUSR00C | COUSR00.CPY |
| COUSR01 | COUSR01 | COUSR01C | COUSR01.CPY |
| COUSR02 | COUSR02 | COUSR02C | COUSR02.CPY |
| COUSR03 | COUSR03 | COUSR03C | COUSR03.CPY |

---

## 7. JCL Job-to-Program Mapping

| JCL Job | Programs Executed | Purpose |
|---------|------------------|---------|
| ACCTFILE.jcl | CBACT01C | Load account VSAM |
| CARDFILE.jcl | CBACT02C | Load card VSAM |
| CUSTFILE.jcl | CBCUS01C | Load customer VSAM |
| XREFFILE.jcl | CBACT03C | Load cross-reference VSAM |
| POSTTRAN.jcl | CBTRN01C, CBTRN02C | Validate & post transactions |
| INTCALC.jcl | CBACT04C | Calculate interest |
| CREASTMT.JCL | CBSTM03A (calls CBSTM03B) | Generate statements |
| TRANREPT.jcl | CBTRN03C | Generate transaction report |
| CBEXPORT.jcl | CBEXPORT | Export data |
| CBIMPORT.jcl | CBIMPORT | Import data |
| WAITSTEP.jcl | COBSWAIT (calls MVSWAIT) | Wait/delay |
| CLOSEFIL.jcl | (CICS command) | Close CICS files |
| OPENFIL.jcl | (CICS command) | Open CICS files |
| TRANBKP.jcl | (IDCAMS REPRO) | Backup transactions |
| COMBTRAN.jcl | (SORT/MERGE) | Combine transactions |
| TRANIDX.jcl | (IDCAMS) | Build alternate indexes |
| DUSRSECJ.jcl | (IDCAMS) | Load user security data |

---

## 8. Cross-Module Dependencies (Optional Modules)

### 8.1 Authorization Module (IMS/DB2/MQ)

```
COMEN01C --XCTL--> COPAUS0C (Authorization Summary)
                       |
                  COPAUS1C (Detail View)
                       |
                  COPAUS2C (Fraud Marking --> DB2 INSERT)
                       |
COPAUA0C (MQ Trigger) ---> IMS DB ---> DB2

Batch: CBPAUP0C (Purge old authorizations)
```

### 8.2 Transaction Type DB2 Module

```
COADM01C --XCTL--> COTRTLIC (List/Delete from DB2 TRAN_TYPE table)
                       |
                  COTRTUPC (Add/Update DB2 TRAN_TYPE table)

Batch: COBTUPDT (Batch update DB2 transaction types)
```

### 8.3 VSAM-MQ Module

```
MQ Channel CDRD --> CODATE01 (System date response)
MQ Channel CDRA --> COACCT01 (Account inquiry via VSAM --> MQ response)
```
