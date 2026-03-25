# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo - Credit Card Management System
> **Scope:** Call graph (program-to-program), data lineage (program-to-file), and JCL job chains

---

## 1. Online Program Call Graph (CICS XCTL / RETURN)

All online programs communicate via `EXEC CICS XCTL PROGRAM(...)` with a shared COMMAREA (`COCOM01Y`). The variable `CDEMO-TO-PROGRAM` holds the target program name at runtime.

### 1.1 Navigation Flow Diagram

```
                            ┌─────────────┐
                            │  COSGN00C   │  (Sign-on - Transaction CC00)
                            │  Entry Point│
                            └──────┬──────┘
                                   │ XCTL
                         ┌─────────┴─────────┐
                         ▼                   ▼
                  ┌─────────────┐     ┌─────────────┐
                  │  COMEN01C   │     │  COADM01C   │
                  │  Main Menu  │     │  Admin Menu  │
                  │  (Regular)  │     │  (Admin)     │
                  └──────┬──────┘     └──────┬──────┘
                         │                   │
          ┌──────┬───────┼───────┬────┐      ├────────┐
          ▼      ▼       ▼       ▼    ▼      ▼        ▼
      ┌───────┐┌───────┐┌──────┐┌───┐┌───┐┌───────┐┌───────┐
      │COACTVWC││COCRDLIC││COTRN00C││CORPT││COBIL││COUSR00C││(menu  │
      │Acct   ││Card   ││Tran  ││00C ││00C ││User  ││options)│
      │View   ││List   ││List  ││Rept││Bill││List  ││       │
      └───┬───┘└───┬───┘└───┬──┘└───┘└───┘└───┬───┘└───────┘
          │        │        │                  │
          ▼        ▼        ▼           ┌──────┼──────┐
      ┌───────┐┌───────┐┌───────┐       ▼      ▼      ▼
      │COACTUPC││COCRDSLC││COTRN01C│  ┌───────┐┌───────┐┌───────┐
      │Acct   ││Card   ││Tran  │  │COUSR01C││COUSR02C││COUSR03C│
      │Update ││Detail ││View  │  │Add    ││Update ││Delete │
      └───────┘└───┬───┘└──────┘  └───────┘└───────┘└───────┘
                   │
                   ▼
               ┌───────┐
               │COCRDUPC│
               │Card   │
               │Update │
               └───────┘
```

### 1.2 Detailed XCTL Transfers (Program → Target)

| Source Program | Target Program | Trigger Condition |
|---------------|---------------|-------------------|
| **COSGN00C** | COADM01C | Successful admin login |
| **COSGN00C** | COMEN01C | Successful regular user login |
| **COMEN01C** | COSGN00C | F3 (Sign off) or session timeout |
| **COMEN01C** | *(menu option)* | XCTL to `CDEMO-MENU-OPT-PGMNAME(WS-OPTION)` |
| **COADM01C** | COSGN00C | F3 (Sign off) |
| **COADM01C** | *(admin option)* | XCTL to `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)` |
| **COACTVWC** | COCRDLIC | View cards for account |
| **COACTVWC** | COCRDUPC | Update card from view |
| **COACTVWC** | COMEN01C | F3 (Back to menu) |
| **COACTVWC** | COCRDSLC | View card detail |
| **COACTUPC** | COCRDUPC | Navigate to card update |
| **COACTUPC** | COCRDLIC | Navigate to card list |
| **COACTUPC** | COMEN01C | F3 (Back to menu) |
| **COACTUPC** | COCRDSLC | View card detail |
| **COCRDLIC** | COCRDSLC | Select card for view |
| **COCRDLIC** | COCRDUPC | Select card for update |
| **COCRDLIC** | COMEN01C | F3 (Back to menu) |
| **COCRDSLC** | COCRDLIC | F3 (Back to list) |
| **COCRDSLC** | COMEN01C | Back to menu |
| **COCRDUPC** | COCRDLIC | F3 (Back to list) |
| **COCRDUPC** | COMEN01C | Back to menu |
| **COCRDUPC** | COCRDSLC | View detail after update |
| **COTRN00C** | COTRN01C | Select transaction for view |
| **COTRN00C** | COMEN01C | F3 (Back to menu) |
| **COTRN00C** | COSGN00C | Session timeout |
| **COTRN01C** | COTRN00C | F3 (Back to list) |
| **COTRN01C** | COMEN01C | Back to menu |
| **COTRN01C** | COSGN00C | Session timeout |
| **COTRN02C** | COMEN01C | F3 (Back to menu) |
| **COTRN02C** | COSGN00C | Session timeout |
| **CORPT00C** | COMEN01C | F3 (Back to menu) |
| **CORPT00C** | COSGN00C | Session timeout |
| **COBIL00C** | COMEN01C | F3 (Back to menu) |
| **COBIL00C** | COSGN00C | Session timeout |
| **COUSR00C** | COUSR02C | Select user for update |
| **COUSR00C** | COUSR03C | Select user for delete |
| **COUSR00C** | COADM01C | F3 (Back to admin menu) |
| **COUSR00C** | COSGN00C | Session timeout |
| **COUSR01C** | COADM01C | F3 (Back to admin menu) |
| **COUSR01C** | COSGN00C | Session timeout |
| **COUSR02C** | COADM01C | F3 (Back to admin menu) |
| **COUSR02C** | COSGN00C | Session timeout |
| **COUSR03C** | COADM01C | F3 (Back to admin menu) |
| **COUSR03C** | COSGN00C | Session timeout |

### 1.3 CALL Statements (Subroutine Calls)

| Source Program | Called Program | Purpose |
|---------------|---------------|---------|
| COTRN02C | CSUTLDTC | Date validation/conversion |
| CORPT00C | CSUTLDTC | Date validation for report range |
| CBSTM03A | CBSTM03B | Generate HTML statement output |
| CBACT01C | *(system)* | COBOL I/O verbs |

### 1.4 CICS Resource Access by Program

| Program | CICS Operations | VSAM Files Accessed |
|---------|----------------|-------------------|
| COSGN00C | READ | USRSEC |
| COACTVWC | READ, STARTBR, READNEXT | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA |
| COACTUPC | READ, REWRITE | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA |
| COCRDLIC | STARTBR, READNEXT, READPREV, ENDBR | CARDXREF, CARDDATA |
| COCRDSLC | READ | CARDDATA, CUSTDATA |
| COCRDUPC | READ, REWRITE | CARDDATA |
| COTRN00C | STARTBR, READNEXT, READPREV, ENDBR | TRANSACT |
| COTRN01C | READ | TRANSACT |
| COTRN02C | READ, WRITE, STARTBR, READPREV, ENDBR | TRANSACT, CARDXREF, ACCTDATA |
| COBIL00C | READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR | TRANSACT, ACCTDATA, CARDXREF, TCATBALF |
| CORPT00C | WRITEQ TD | *(Transient Data Queue - triggers batch)* |
| COUSR00C | STARTBR, READNEXT, READPREV, ENDBR | USRSEC |
| COUSR01C | WRITE | USRSEC |
| COUSR02C | READ, REWRITE | USRSEC |
| COUSR03C | READ, DELETE | USRSEC |

---

## 2. Copybook Inclusion Map (COPY Statements)

### 2.1 Data Copybook Usage Matrix

| Copybook | Used By Programs |
|----------|-----------------|
| **COCOM01Y** (COMMAREA) | COACTUPC, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **COTTL01Y** (Titles) | COADM01C, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSDAT01Y** (Dates) | COADM01C, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG01Y** (Messages) | COADM01C, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSUSR01Y** (Security) | COADM01C, COACTUPC, COACTVWC, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **CSMSG02Y** (Ext Msgs) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN02C |
| **CVACT01Y** (Account) | CBACT01C, COACTUPC, COACTVWC, COBIL00C, CBTRN02C, CBACT04C |
| **CVACT02Y** (Card) | CBACT02C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBTRN02C |
| **CVACT03Y** (Xref) | COACTUPC, COACTVWC, CBTRN02C, CBTRN03C, CBACT04C |
| **CVCUS01Y** (Customer) | COACTUPC, COACTVWC, COCRDSLC |
| **CVCRD01Y** (Card Alt) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CVTRA05Y** (Transaction) | COACTUPC, COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN02C, CBTRN03C |
| **CVTRA06Y** (Daily Tran) | CBTRN02C |
| **CVTRA01Y** (Cat Balance) | CBTRN02C, CBACT04C |
| **CVTRA02Y** (Disclosure) | CBACT04C |
| **CVTRA03Y** (Tran Type) | CBTRN03C |
| **CVTRA04Y** (Tran Cat) | CBTRN03C |
| **CVTRA07Y** (Report) | CBTRN03C |
| **COMEN02Y** (Menu Opts) | COMEN01C |
| **COADM02Y** (Admin Opts) | COADM01C |
| **CSSTRPFY** (String) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| **CSSETATY** (Attributes) | COACTUPC, COCRDLIC, COCRDUPC |
| **CODATECN** (Date Conv) | CBACT01C |
| **CSUTLDPY** / **CSUTLDWY** | CSUTLDTC, CORPT00C, COTRN02C |
| **CVEXPORT** (Export) | CBEXPORT, CBIMPORT |
| **COSTM01** (Stmt Layout) | CBSTM03A, CBSTM03B |
| **CUSTREC** (Cust Alt) | CBTRN01C |
| **UNUSED1Y** | *(none - deprecated)* |

### 2.2 BMS Map to Program Binding

| BMS Map | COBOL Program | Copybook (Generated) |
|---------|--------------|---------------------|
| COSGN00.bms | COSGN00C | COSGN00.CPY |
| COMEN01.bms | COMEN01C | COMEN01.CPY |
| COADM01.bms | COADM01C | COADM01.CPY |
| COACTVW.bms | COACTVWC | COACTVW.CPY |
| COACTUP.bms | COACTUPC | COACTUP.CPY |
| COCRDLI.bms | COCRDLIC | COCRDLI.CPY |
| COCRDSL.bms | COCRDSLC | COCRDSL.CPY |
| COCRDUP.bms | COCRDUPC | COCRDUP.CPY |
| COTRN00.bms | COTRN00C | COTRN00.CPY |
| COTRN01.bms | COTRN01C | COTRN01.CPY |
| COTRN02.bms | COTRN02C | COTRN02.CPY |
| CORPT00.bms | CORPT00C | CORPT00.CPY |
| COBIL00.bms | COBIL00C | COBIL00.CPY |
| COUSR00.bms | COUSR00C | COUSR00.CPY |
| COUSR01.bms | COUSR01C | COUSR01.CPY |
| COUSR02.bms | COUSR02C | COUSR02.CPY |
| COUSR03.bms | COUSR03C | COUSR03.CPY |

---

## 3. JCL Job Data Lineage

### 3.1 Batch Processing Flow (Nightly Cycle)

```
Phase 1: CICS Quiesce          Phase 2: Data Refresh
┌──────────┐                   ┌──────────┐  ┌──────────┐  ┌──────────┐
│ CLOSEFIL │ ──────────────►   │ ACCTFILE │  │ CARDFILE │  │ CUSTFILE │
│(close    │                   │(refresh  │  │(refresh  │  │(refresh  │
│ CICS     │                   │ accounts)│  │ cards)   │  │ cust)    │
│ files)   │                   └──────────┘  └──────────┘  └──────────┘
└──────────┘                   ┌──────────┐  ┌──────────┐  ┌──────────┐
                               │ XREFFILE │  │ TRANFILE │  │ DUSRSECJ │
                               │(refresh  │  │(refresh  │  │(refresh  │
                               │ xref)    │  │ trans)   │  │ users)   │
                               └──────────┘  └──────────┘  └──────────┘

Phase 3: Batch Processing      Phase 4: Reporting & Backup
┌──────────┐                   ┌──────────┐
│ POSTTRAN │ ──────────────►   │ TRANBKP  │ (Backup transactions)
│(post     │                   └──────────┘
│ daily    │                         │
│ trans)   │                         ▼
└──────────┘                   ┌──────────┐
      │                        │ COMBTRAN │ (Combine backups)
      ▼                        └──────────┘
┌──────────┐                         │
│ INTCALC  │                         ▼
│(interest │                   ┌──────────┐
│ calc)    │                   │ CREASTMT │ (Generate statements)
└──────────┘                   └──────────┘
                                     │
Phase 5: Index & Reopen              ▼
┌──────────┐                   ┌──────────┐
│ TRANIDX  │                   │ TRANREPT │ (Transaction report)
│(rebuild  │                   └──────────┘
│ indexes) │
└──────────┘
      │
      ▼
┌──────────┐
│ OPENFIL  │
│(reopen   │
│ CICS     │
│ files)   │
└──────────┘
```

### 3.2 JCL Job → Program → Dataset Matrix

#### Data Refresh Jobs (IDCAMS REPRO)

| Job | Input Dataset (READ) | Output Dataset (WRITE) |
|-----|---------------------|----------------------|
| **ACCTFILE** | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | CARDXREF.PS | CARDXREF.VSAM.KSDS, CARDXREF.VSAM.AIX |
| **TRANFILE** | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | USRSEC.PS | USRSEC.VSAM.KSDS |
| **TRANTYPE** | TRANTYPE.PS | TRANTYPE.VSAM.KSDS |
| **TRANCATG** | TRANCATG.PS | TRANCATG.VSAM.KSDS |
| **DISCGRP** | DISCGRP.PS | DISCGRP.VSAM.KSDS |
| **TCATBALF** | TCATBALF.PS | TCATBALF.VSAM.KSDS |

#### Batch Processing Jobs

| Job | Program | Input Datasets (READ) | Output Datasets (WRITE) |
|-----|---------|----------------------|------------------------|
| **POSTTRAN** | CBTRN02C | DALYTRAN.PS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, TCATBALF.VSAM.KSDS | TRANSACT.VSAM.KSDS, DALYREJS(+1) |
| **INTCALC** | CBACT04C | TCATBALF.VSAM.KSDS, CARDXREF.VSAM.KSDS, CARDXREF.VSAM.AIX.PATH, ACCTDATA.VSAM.KSDS, DISCGRP.VSAM.KSDS | SYSTRAN(+1) |
| **TRANBKP** | IDCAMS | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) |
| **COMBTRAN** | SORT | TRANSACT.BKUP(0), SYSTRAN(0) | TRANSACT.COMBINED(+1), TRANSACT.VSAM.KSDS |
| **CREASTMT** | SORT, CBSTM03A | TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, CUSTDATA.VSAM.KSDS | TRXFL.SEQ, TRXFL.VSAM.KSDS, STATEMNT.PS, STATEMNT.HTML |
| **TRANREPT** | CBTRN03C | TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, TRANTYPE.VSAM.KSDS, TRANCATG.VSAM.KSDS, DATEPARM | TRANSACT.BKUP(+1), TRANSACT.DALY(+1), TRANREPT(+1) |
| **CBEXPORT** | CBEXPORT | CUSTDATA.VSAM.KSDS, ACCTDATA.VSAM.KSDS, CARDXREF.VSAM.KSDS, TRANSACT.VSAM.KSDS, CARDDATA.VSAM.KSDS | EXPORT.DATA |
| **CBIMPORT** | CBIMPORT | EXPORT.DATA | CUSTDATA.IMPORT, ACCTDATA.IMPORT, CARDXREF.IMPORT, TRANSACT.IMPORT, IMPORT.ERRORS |

#### Print/Report Jobs

| Job | Program | Input Datasets (READ) | Output |
|-----|---------|----------------------|--------|
| **READACCT** | CBACT01C | ACCTDATA.VSAM.KSDS | SYSOUT (print) |
| **READCARD** | CBACT02C | CARDDATA.VSAM.KSDS | SYSOUT (print) |
| **READCUST** | CBCUS01C | CUSTDATA.VSAM.KSDS | SYSOUT (print) |
| **READXREF** | CBACT03C | CARDXREF.VSAM.KSDS | SYSOUT (print) |
| **PRTCATBL** | IDCAMS | TCATBALF.VSAM.KSDS | TCATBALF.BKUP(+1), TCATBALF.REPT |
| **TXT2PDF1** | TXT2PDF | STATEMNT.PS | STATEMNT.PS.PDF |

---

## 4. VSAM File Access Summary (Cross-Cutting)

This shows which programs (both online and batch) read from or write to each VSAM file.

| VSAM Dataset | Online Readers | Online Writers | Batch Readers | Batch Writers |
|-------------|---------------|---------------|--------------|--------------|
| **ACCTDATA.VSAM.KSDS** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC, COBIL00C | CBTRN02C, CBACT04C, CBSTM03A, CBEXPORT, CBACT01C | CBTRN02C (via update) |
| **CARDDATA.VSAM.KSDS** | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC | CBEXPORT, CBACT02C | -- |
| **CARDXREF.VSAM.KSDS** | COACTVWC, COACTUPC, COCRDLIC, COTRN02C, COBIL00C | -- | CBTRN02C, CBACT04C, CBSTM03A, CBTRN03C, CBEXPORT, CBACT03C | -- |
| **CUSTDATA.VSAM.KSDS** | COACTVWC, COACTUPC, COCRDSLC | -- | CBSTM03A, CBEXPORT, CBCUS01C | -- |
| **TRANSACT.VSAM.KSDS** | COTRN00C, COTRN01C, COTRN02C, COBIL00C | COTRN02C, COBIL00C | CBTRN02C, CBSTM03A, CBEXPORT | CBTRN02C, COMBTRAN |
| **USRSEC.VSAM.KSDS** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | COUSR01C, COUSR02C, COUSR03C | -- | -- |
| **TCATBALF.VSAM.KSDS** | COBIL00C | COBIL00C | CBTRN02C, CBACT04C | CBTRN02C |
| **DISCGRP.VSAM.KSDS** | -- | -- | CBACT04C | -- |
| **TRANTYPE.VSAM.KSDS** | -- | -- | CBTRN03C | -- |
| **TRANCATG.VSAM.KSDS** | -- | -- | CBTRN03C | -- |
| **DALYTRAN.PS** | -- | -- | CBTRN01C, CBTRN02C | -- |

---

## 5. Shared Infrastructure Dependencies

### 5.1 CICS System Copybooks (External)

| Copybook | Purpose | Used By |
|----------|---------|---------|
| **DFHAID** | AID key definitions (PF keys, ENTER, CLEAR) | All 17 online programs |
| **DFHBMSCA** | BMS control attributes | All 17 online programs |

### 5.2 CICS System Services Used

| Service | Programs Using It |
|---------|------------------|
| SEND MAP / RECEIVE MAP | All 17 online programs |
| READ / WRITE / REWRITE / DELETE | COSGN00C, COACTUPC, COTRN02C, COBIL00C, COUSR01-03C |
| STARTBR / READNEXT / READPREV / ENDBR | COACTVWC, COCRDLIC, COTRN00C, COUSR00C, COBIL00C, COTRN02C |
| XCTL (Transfer Control) | COSGN00C, COMEN01C, COADM01C, and most online programs |
| WRITEQ TD (Transient Data) | CORPT00C (submit batch report) |
| ASKTIME / FORMATTIME | COBIL00C |
| HANDLE ABEND | COACTUPC, COCRDUPC |
| ASSIGN | COSGN00C |

---

## 6. Modernization Dependency Clusters

Programs that share data and should be migrated together:

### Cluster 1: Account & Card Management (highest coupling)
- **Programs:** COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC
- **Shared Data:** ACCTDATA, CARDDATA, CARDXREF, CUSTDATA
- **Shared Copybooks:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y

### Cluster 2: Transaction Processing
- **Programs:** COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN01C, CBTRN02C
- **Shared Data:** TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF
- **Shared Copybooks:** CVTRA05Y, CVTRA06Y, CVTRA01Y

### Cluster 3: Reporting & Statements
- **Programs:** CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B
- **Shared Data:** TRANSACT, CARDXREF, TRANTYPE, TRANCATG, ACCTDATA, CUSTDATA
- **Shared Copybooks:** CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y, COSTM01

### Cluster 4: User Administration
- **Programs:** COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C
- **Shared Data:** USRSEC
- **Shared Copybooks:** CSUSR01Y

### Cluster 5: Financial Calculations
- **Programs:** CBACT04C
- **Shared Data:** TCATBALF, CARDXREF, ACCTDATA, DISCGRP
- **Shared Copybooks:** CVTRA01Y, CVTRA02Y, CVACT01Y, CVACT03Y

### Cluster 6: Data Migration
- **Programs:** CBEXPORT, CBIMPORT
- **Shared Data:** All VSAM files, EXPORT.DATA
- **Shared Copybooks:** CVEXPORT
