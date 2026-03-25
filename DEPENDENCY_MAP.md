# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Scope:** Call graph, copybook dependencies, and data lineage (file read/write by program and JCL job)

---

## 1. Online Program Call Graph (CICS XCTL / LINK)

The online CICS programs communicate via `EXEC CICS XCTL` (transfer control) and share context through `CARDDEMO-COMMAREA` (defined in `COCOM01Y`).

### 1.1 Navigation Flow

```
                         ┌──────────────┐
                         │  COSGN00C    │
                         │  (Sign-on)   │
                         └──────┬───────┘
                      Auth OK   │
                   ┌────────────┴────────────┐
                   │                          │
            Admin (Type=A)              User (Type=U)
                   │                          │
                   ▼                          ▼
           ┌──────────────┐          ┌──────────────┐
           │  COADM01C    │          │  COMEN01C    │
           │ (Admin Menu) │          │ (Main Menu)  │
           └──────┬───────┘          └──────┬───────┘
                  │                          │
    ┌─────────────┼─────────────┐    ┌───────┼──────────────────┐
    │             │             │    │       │                   │
    ▼             ▼             ▼    ▼       ▼                   ▼
 COUSR00C    COUSR01C     COUSR02C  (Same 11 options as below)
 COUSR03C    COTRTLIC*    COTRTUPC*
```

### 1.2 Main Menu Options (COMEN01C → Target Programs)

The menu configuration is defined in copybook `COMEN02Y`:

| Option | Menu Label | Target Program | Trans ID | Access |
|--------|-----------|---------------|----------|--------|
| 1 | Account View | COACTVWC | CAVW | All Users |
| 2 | Account Update | COACTUPC | CAUP | All Users |
| 3 | Credit Card List | COCRDLIC | CCLI | All Users |
| 4 | Credit Card View | COCRDSLC | CCDL | All Users |
| 5 | Credit Card Update | COCRDUPC | CCUP | All Users |
| 6 | Transaction List | COTRN00C | CT00 | All Users |
| 7 | Transaction View | COTRN01C | CT01 | All Users |
| 8 | Transaction Add | COTRN02C | CT02 | All Users |
| 9 | Transaction Reports | CORPT00C | CR00 | All Users |
| 10 | Bill Payment | COBIL00C | CB00 | All Users |
| 11 | Pending Auth View | COPAUS0C* | — | All Users |

*\* Optional module — program availability is checked at runtime via `EXEC CICS INQUIRE PROGRAM`.*

### 1.3 Admin Menu Options (COADM01C → Target Programs)

The admin menu configuration is defined in copybook `COADM02Y`:

| Option | Menu Label | Target Program |
|--------|-----------|---------------|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (DB2)* | COTRTLIC |
| 6 | Transaction Type Maintenance (DB2)* | COTRTUPC |

*\* Optional DB2 module programs.*

### 1.4 Inter-Program Navigation (XCTL Transfers)

| Source Program | Target Program | Trigger |
|---------------|---------------|---------|
| COSGN00C | COADM01C | Admin user login |
| COSGN00C | COMEN01C | Regular user login |
| COMEN01C | COSGN00C | PF3 (exit to signon) |
| COMEN01C | *(menu target)* | Menu option selection |
| COADM01C | *(admin target)* | Admin option selection |
| COACTVWC | COMEN01C | PF3 (return to menu) |
| COACTUPC | COMEN01C | PF3 (return to menu) |
| COCRDLIC | COMEN01C | PF3 (return to menu) |
| COCRDLIC | COCRDSLC | Select 'S' on a card row |
| COCRDLIC | COCRDUPC | Select 'U' on a card row |
| COCRDSLC | COCRDLIC | PF3 (return to list) |
| COCRDUPC | COCRDLIC | PF3 (return to list) |
| COTRN00C | COMEN01C | PF3 (return to menu) |
| COTRN00C | COTRN01C | Select a transaction |
| COTRN01C | COTRN00C | PF3 (return to list) |
| COTRN02C | COMEN01C | PF3 (return to menu) |
| CORPT00C | COMEN01C | PF3 (return to menu) |
| COBIL00C | COMEN01C | PF3 (return to menu) |
| COUSR00C | COADM01C | PF3 (return to admin menu) |
| COUSR00C | COUSR02C | Select 'U' on a user row |
| COUSR00C | COUSR03C | Select 'D' on a user row |
| COUSR01C | COADM01C | PF3 (return to admin menu) |
| COUSR02C | COUSR00C | PF3 (return to user list) |
| COUSR03C | COUSR00C | PF3 (return to user list) |

### 1.5 Subroutine CALL Graph (COBOL CALL)

| Calling Program | Called Program | Purpose |
|----------------|---------------|---------|
| CBACT01C | COBDATFT (ASM) | Date formatting |
| COBSWAIT | MVSWAIT (ASM) | MVS wait/delay |
| CBSTM03A | CBSTM03B | Statement file I/O |
| CORPT00C | CSUTLDTC | Date validation |
| COTRN02C | CSUTLDTC | Date validation |
| COACTUPC | CSUTLDTC | Date validation (via CSUTLDPY copybook) |
| CSUTLDTC | CEEDAYS (LE) | Language Environment date services |
| All batch CB* | CEE3ABD (LE) | Abnormal termination handler |

---

## 2. Copybook Dependency Matrix

### 2.1 Which Programs Include Which Copybooks

| Copybook | Used By (Programs) | Purpose |
|----------|-------------------|---------|
| **COCOM01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C-03C | Application commarea |
| **COTTL01Y** | All 17 online programs | Screen titles |
| **CSDAT01Y** | All 17 online programs | Date/time formatting |
| **CSMSG01Y** | All 17 online programs | Common messages |
| **CSUSR01Y** | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | User security record |
| **CVACT01Y** | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT | Account record |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT | Card record |
| **CVACT03Y** | COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT | Cross-reference |
| **CVCUS01Y** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT | Customer record |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | Transaction record |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | Daily transaction |
| **COMEN02Y** | COMEN01C | Main menu options |
| **COADM02Y** | COADM01C | Admin menu options |
| **CVCRD01Y** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | Card work area |
| **CSMSG02Y** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | Abend handling |
| **CSSTRPFY** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | PF key storage |
| **CSUTLDWY** | COACTUPC | Date validation WS |
| **CSUTLDPY** | COACTUPC | Date validation logic |
| **CSSETATY** | COACTUPC | Screen attributes |
| **CSLKPCDY** | COACTUPC | Lookup code utility |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Category balance |
| **CVTRA02Y** | CBACT04C | Discount rates |
| **CVTRA03Y** | CBTRN03C | Transaction types |
| **CVTRA04Y** | CBTRN03C | Transaction categories |
| **CVTRA07Y** | CBTRN03C | Report formatting |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export record layout |
| **COSTM01** | CBSTM03A | Statement transaction layout |
| **CUSTREC** | CBSTM03A | Statement customer layout |
| **CODATECN** | CBACT01C | Date conversion layout |

---

## 3. Data Lineage — VSAM File Access by Program

### 3.1 Online Programs (CICS File I/O)

| Program | File | Read | Write | Rewrite | Delete | Browse | Alt Index |
|---------|------|------|-------|---------|--------|--------|-----------|
| COSGN00C | USRSEC | ✓ | | | | | |
| COACTVWC | CXACAIX | ✓ | | | | | ACCT path |
| COACTVWC | ACCTDAT | ✓ | | | | | |
| COACTVWC | CUSTDAT | ✓ | | | | | |
| COACTUPC | CXACAIX | ✓ | | | | | ACCT path |
| COACTUPC | ACCTDAT | ✓ | | ✓ | | | |
| COACTUPC | CUSTDAT | ✓ | | | | | |
| COACTUPC | CARDDAT | ✓ | | | | | |
| COCRDLIC | CARDDAT | | | | | ✓ | CARDAIX |
| COCRDSLC | CARDDAT | ✓ | | | | | |
| COCRDUPC | CARDDAT | ✓ | | ✓ | | | |
| COTRN00C | TRANSACT | | | | | ✓ | |
| COTRN01C | TRANSACT | ✓ | | | | | |
| COTRN02C | CXACAIX | ✓ | | | | | |
| COTRN02C | CARDXREF | ✓ | | | | | |
| COTRN02C | TRANSACT | ✓ | ✓ | | | ✓ | |
| COBIL00C | ACCTDAT | ✓ | | ✓ | | | |
| COBIL00C | CXACAIX | ✓ | | | | | |
| COBIL00C | TRANSACT | | ✓ | | | ✓ | |
| COUSR00C | USRSEC | | | | | ✓ | |
| COUSR01C | USRSEC | | ✓ | | | | |
| COUSR02C | USRSEC | ✓ | | ✓ | | | |
| COUSR03C | USRSEC | ✓ | | | ✓ | | |
| CORPT00C | *(TDQ)* | | ✓ | | | | |

### 3.2 Batch Programs (Sequential / VSAM File I/O)

| Program | Input Files (Read) | Output Files (Write) |
|---------|-------------------|---------------------|
| CBACT01C | ACCTFILE (VSAM) | OUTFILE, ARRYFILE, VBRCFILE |
| CBACT02C | CARDFILE (VSAM) | *(display only)* |
| CBACT03C | XREFFILE (VSAM) | *(display only)* |
| CBACT04C | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TCATBALF (update), ACCTFILE (update) |
| CBCUS01C | CUSTFILE (VSAM) | *(display only)* |
| CBTRN01C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE |
| CBTRN02C | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF |
| CBTRN03C | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (report) |
| CBSTM03A | *(via CBSTM03B)* | STMTFILE, HTMLFILE |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | *(returns data to caller)* |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |
| COBSWAIT | *(SYSIN parm)* | *(none)* |

---

## 4. JCL Job → Program → File Data Lineage

### 4.1 Core Batch Cycle Data Flow

```
CLOSEFIL ─── Closes CICS files for batch window
    │
    ▼
ACCTFILE ─── IDCAMS: Delete/Define/Repro → ACCTDAT (VSAM)
CARDFILE ─── IDCAMS: Delete/Define/Repro → CARDDAT + CARDAIX (VSAM)
CUSTFILE ─── IDCAMS: Delete/Define/Repro → CUSTDAT (VSAM)
XREFFILE ─── IDCAMS: Delete/Define/Repro → CARDXREF + CXACAIX (VSAM)
TRANFILE ─── IDCAMS: Delete/Define/Repro → TRANSACT (VSAM)
    │
    ▼
POSTTRAN ─── CBTRN02C
    │  Reads:  DALYTRAN, XREFFILE, ACCTFILE
    │  Writes: TRANSACT, DALYREJS, TCATBALF
    │
    ▼
INTCALC ──── CBACT04C
    │  Reads:  TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT
    │  Writes: TCATBALF (updated), ACCTFILE (interest applied)
    │
    ▼
TRANBKP ──── IDCAMS + REPROC
    │  Backs up TRANSACT to GDG dataset
    │
    ▼
COMBTRAN ─── SORT + IDCAMS
    │  Merges transaction files
    │
    ▼
CREASTMT ─── SORT + CBSTM03A (calls CBSTM03B)
    │  Reads:  Sorted transactions, XREFFILE, CUSTFILE, ACCTFILE
    │  Writes: STMTFILE (text), HTMLFILE (HTML statements)
    │
    ▼
TRANREPT ─── SORT + CBTRN03C
    │  Reads:  TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
    │  Writes: TRANREPT (printed report)
    │
    ▼
TRANIDX ──── IDCAMS: Rebuild alternate indexes on TRANSACT
    │
    ▼
OPENFIL ──── Reopens CICS files for online access
```

### 4.2 Data Refresh Jobs — File Mapping

| JCL Job | VSAM Dataset(s) Managed | Operation |
|---------|------------------------|-----------|
| ACCTFILE | ACCTDAT | Delete + Define + Repro from flat file |
| CARDFILE | CARDDAT, CARDAIX | Delete + Define + Repro + Build AIX |
| CUSTFILE | CUSTDAT | Delete + Define + Repro |
| XREFFILE | CARDXREF, CXACAIX | Delete + Define + Repro + Build AIX |
| TRANFILE | TRANSACT + indexes | Delete + Define + Repro + Build AIX |
| DUSRSECJ | USRSEC | Load from inline data (IEBGENER + IDCAMS) |
| DISCGRP | DISCGRP | Delete + Define + Repro |
| TCATBALF | TCATBALF | Delete + Define + Repro |
| TRANCATG | TRANCATG | Delete + Define + Repro |
| TRANTYPE | TRANTYPE | Delete + Define + Repro |
| DALYREJS | DALYREJS | Define VSAM cluster |
| REPTFILE | REPTFILE | Define VSAM cluster |

### 4.3 Export/Import Data Flow

```
CBEXPORT Job:
  CUSTFILE ──┐
  ACCTFILE ──┤
  XREFFILE ──┼──→ CBEXPORT program ──→ EXPFILE (multi-record export)
  TRANSACT ──┤
  CARDFILE ──┘

CBIMPORT Job:
  EXPFILE ──→ CBIMPORT program ──┬──→ CUSTOUT
                                  ├──→ ACCTOUT
                                  ├──→ XREFOUT
                                  ├──→ TRNXOUT
                                  ├──→ CARDOUT
                                  └──→ ERROUT (rejected records)
```

---

## 5. BMS Map to Program to Copybook Chain

| BMS Map | Mapset | Program | Copybook (symbolic) | Data Copybooks Used |
|---------|--------|---------|--------------------|--------------------|
| COSGN00 | COSGN00 | COSGN00C | COSGN00.CPY | CSUSR01Y, COCOM01Y |
| COMEN01 | COMEN01 | COMEN01C | COMEN01.CPY | COMEN02Y, COCOM01Y |
| COADM01 | COADM01 | COADM01C | COADM01.CPY | COADM02Y, COCOM01Y |
| COACTVW | COACTVW | COACTVWC | COACTVW.CPY | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| COACTUP | COACTUP | COACTUPC | COACTUP.CPY | CVACT01Y, CVACT03Y, CVCUS01Y |
| COCRDLI | COCRDLI | COCRDLIC | COCRDLI.CPY | CVACT02Y |
| COCRDSL | COCRDSL | COCRDSLC | COCRDSL.CPY | CVACT02Y, CVCUS01Y |
| COCRDUP | COCRDUP | COCRDUPC | COCRDUP.CPY | CVACT02Y, CVCUS01Y |
| COTRN00 | COTRN00 | COTRN00C | COTRN00.CPY | CVTRA05Y |
| COTRN01 | COTRN01 | COTRN01C | COTRN01.CPY | CVTRA05Y |
| COTRN02 | COTRN02 | COTRN02C | COTRN02.CPY | CVACT01Y, CVACT03Y, CVTRA05Y |
| CORPT00 | CORPT00 | CORPT00C | CORPT00.CPY | CVTRA05Y |
| COBIL00 | COBIL00 | COBIL00C | COBIL00.CPY | CVACT01Y, CVACT03Y, CVTRA05Y |
| COUSR00 | COUSR00 | COUSR00C | COUSR00.CPY | CSUSR01Y |
| COUSR01 | COUSR01 | COUSR01C | COUSR01.CPY | CSUSR01Y |
| COUSR02 | COUSR02 | COUSR02C | COUSR02.CPY | CSUSR01Y |
| COUSR03 | COUSR03 | COUSR03C | COUSR03.CPY | CSUSR01Y |

---

## 6. Optional Module Dependencies

### 6.1 Authorization Module (IMS/DB2/MQ)

```
COPAUA0C ──→ MQ Queue (trigger)
    │
    ├──→ IMS Database (authorization messages)
    │
    ▼
COPAUS0C ──→ IMS DB (summary view) ──→ BMS screen
    │
    ▼
COPAUS1C ──→ IMS DB (detail view) ──→ BMS screen
    │
    ▼
COPAUS2C ──→ DB2 Table (fraud marking)

CBPAUP0C ──→ IMS DB (batch purge expired records)
```

### 6.2 Transaction Type DB2 Module

```
COTRTLIC ──→ DB2 TRAN_TYPE table (cursor browse, list, delete)
COTRTUPC ──→ DB2 TRAN_TYPE table (add/update with embedded SQL)
COBTUPDT ──→ DB2 TRAN_TYPE table (batch update)
```

### 6.3 VSAM-MQ Module

```
CODATE01 ──→ MQ Request queue → MQ Reply queue (system date)
COACCT01 ──→ MQ Request queue → MQ Reply queue (account inquiry)
```

---

## 7. Shared Infrastructure Dependencies

| Infrastructure Component | Used By |
|-------------------------|---------|
| DFHCOMMAREA (CICS commarea) | All 17 online programs |
| DFHAID (AID key definitions) | All 17 online programs |
| DFHBMSCA (BMS screen attributes) | All 17 online programs |
| CEE3ABD (LE abend handler) | All 13 batch programs |
| CEEDAYS (LE date services) | CSUTLDTC |
| COBDATFT (ASM date formatter) | CBACT01C |
| MVSWAIT (ASM wait utility) | COBSWAIT |
| REPROC.prc (JCL procedure) | TRANBKP, PRTCATBL |
| TRANREPT.prc (JCL procedure) | TRANREPT |
| SDSF (system display) | CLOSEFIL, OPENFIL, CARDFILE, CUSTFILE, TRANFILE |
| IDCAMS (access method services) | 20+ JCL jobs |
| SORT (DFSORT/SYNCSORT) | COMBTRAN, CREASTMT, TRANREPT, PRTCATBL |
