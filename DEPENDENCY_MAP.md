# Dependency Map - CardDemo COBOL Codebase

> **Generated**: March 2026  
> **Application**: CardDemo - Mainframe Credit Card Management System  
> **Analysis**: Static analysis of CALL statements, COPY directives, EXEC CICS READ/WRITE, SELECT/FD, and JCL DD statements

---

## Table of Contents

1. [Program Call Graph](#program-call-graph)
2. [Copybook Dependency Matrix](#copybook-dependency-matrix)
3. [VSAM File Access Map](#vsam-file-access-map)
4. [JCL Job-to-Program Mapping](#jcl-job-to-program-mapping)
5. [JCL Data Lineage (File I/O)](#jcl-data-lineage)
6. [Batch Processing Flow](#batch-processing-flow)
7. [CICS Transaction Routing](#cics-transaction-routing)
8. [BMS Map-to-Program Binding](#bms-map-to-program-binding)
9. [Cross-Reference: Shared Service Dependencies](#cross-reference-shared-service-dependencies)

---

## Program Call Graph

### Inter-Program CALL Relationships

Programs communicate via COBOL `CALL` statements (batch) or CICS `XCTL`/`LINK`/`RETURN TRANSID` (online). The following shows explicit CALL dependencies extracted from source.

```
CBACT01C ──CALL──> COBDATFT (Assembler: date formatting)
CBACT01C ──CALL──> CEE3ABD  (LE: abnormal termination)

CBACT02C ──CALL──> CEE3ABD
CBACT03C ──CALL──> CEE3ABD
CBACT04C ──CALL──> CEE3ABD

CBCUS01C ──CALL──> CEE3ABD

CBTRN01C ──CALL──> CEE3ABD
CBTRN02C ──CALL──> CEE3ABD
CBTRN03C ──CALL──> CEE3ABD

CBSTM03A ──CALL──> CBSTM03B (Subroutine: file I/O for statements)  [13 call sites]
CBSTM03A ──CALL──> CEE3ABD

CBEXPORT ──CALL──> CEE3ABD
CBIMPORT ──CALL──> CEE3ABD

COBSWAIT ──CALL──> MVSWAIT  (Assembler: MVS WAIT SVC)

CORPT00C ──CALL──> CSUTLDTC (Utility: date validation)   [2 call sites]
COTRN02C ──CALL──> CSUTLDTC (Utility: date validation)   [2 call sites]

CSUTLDTC ──CALL──> CEEDAYS  (LE: date conversion intrinsic)
```

### Call Graph Diagram

```
                    ┌─────────────┐
                    │   CEE3ABD   │  (LE Abnormal End - called by 11 batch programs)
                    └──────▲──────┘
                           │
    ┌──────────┬───────────┼───────────┬──────────┬──────────┐
    │          │           │           │          │          │
CBACT01C  CBACT02C    CBACT04C    CBTRN02C  CBSTM03A  CBEXPORT
    │      CBACT03C    CBTRN01C   CBTRN03C            CBIMPORT
    │      CBCUS01C
    │
    ▼
COBDATFT (ASM)


CBSTM03A ─────────────────> CBSTM03B (13 CALL sites)
    │                            │
    │  Statement Driver          │  File I/O Subroutine
    │  (OPEN/CLOSE/WRITE)        │  (OPEN/READ/CLOSE for 4 files)
    └────────────────────────────┘


CORPT00C ──┐
           ├──────> CSUTLDTC ──────> CEEDAYS (LE intrinsic)
COTRN02C ──┘


COBSWAIT ──────────> MVSWAIT (ASM: MVS WAIT SVC)
```

### CICS Program Navigation (Implicit via XCTL/RETURN TRANSID)

```
COSGN00C (Sign-on)
    ├── [Admin user]  ──> COADM01C (Admin Menu)
    │                        ├──> COUSR00C (User List)
    │                        │       ├──> COUSR01C (Add User)
    │                        │       ├──> COUSR02C (Update User)
    │                        │       └──> COUSR03C (Delete User)
    │                        └──> (back to sign-on)
    │
    └── [Regular user] ──> COMEN01C (Main Menu)
                             ├──> COACTVWC (Account View)
                             ├──> COACTUPC (Account Update)
                             ├──> COCRDLIC (Card List)
                             │       ├──> COCRDSLC (Card Detail)
                             │       └──> COCRDUPC (Card Update)
                             ├──> COTRN00C (Transaction List)
                             │       ├──> COTRN01C (Transaction View)
                             │       └──> COTRN02C (Transaction Add)
                             ├──> COBIL00C (Bill Payment)
                             └──> CORPT00C (Transaction Reports)
```

---

## Copybook Dependency Matrix

Shows which programs include which copybooks via COPY statements.

### Core Data Copybooks

| Copybook    | Domain        | Used By Programs                                                                              | Usage Count |
|-------------|---------------|-----------------------------------------------------------------------------------------------|------------|
| COCOM01Y    | COMMAREA      | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| COTTL01Y    | UI Title      | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| CSDAT01Y    | Date          | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| CSMSG01Y    | Messages      | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 17 |
| CSUSR01Y    | Security      | COACTUPC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 10 |
| CVACT01Y    | Account       | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COTRN02C | 10 |
| CVACT03Y    | Cross-Ref     | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C | 12 |
| CVACT02Y    | Card          | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC | 8 |
| CVCUS01Y    | Customer      | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | 8 |
| CVTRA05Y    | Transaction   | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C | 11 |
| CVTRA06Y    | Daily Trans   | CBTRN01C, CBTRN02C                                                                           | 2 |
| CVTRA01Y    | Cat Balance   | CBACT04C, CBTRN02C                                                                           | 2 |
| CVTRA02Y    | Disclosure    | CBACT04C                                                                                     | 1 |
| CVTRA03Y    | Tran Type     | CBTRN03C                                                                                     | 1 |
| CVTRA04Y    | Tran Category | CBTRN03C                                                                                     | 1 |
| CVTRA07Y    | Report Layout | CBTRN03C                                                                                     | 1 |
| CSMSG02Y    | Messages (2)  | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC                                                      | 4 |
| CVCRD01Y    | Card (int.)   | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                                            | 5 |
| COMEN02Y    | Menu Def      | COMEN01C                                                                                     | 1 |
| COADM02Y    | Admin Menu    | COADM01C                                                                                     | 1 |
| COSTM01     | Statement     | CBSTM03A                                                                                     | 1 |
| CUSTREC     | Cust (Stmt)   | CBSTM03A                                                                                     | 1 |
| CVEXPORT    | Export Layout | CBEXPORT, CBIMPORT                                                                           | 2 |
| CODATECN    | Date Conv     | CBACT01C                                                                                     | 1 |
| CSLKPCDY    | Lookup Codes  | COACTUPC                                                                                     | 1 |
| CSSETATY    | Screen Attrs  | COACTUPC (39 COPY REPLACING instances)                                                        | 1 |
| CSSTRPFY    | String Utils  | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                                            | 5 |
| CSUTLDPY    | Date Util     | COACTUPC                                                                                     | 1 |
| CSUTLDWY    | Date Util WS  | COACTUPC                                                                                     | 1 |
| UNUSED1Y    | Deprecated    | (none - unused)                                                                              | 0 |

### CICS System Copybooks

| Copybook    | Purpose               | Used By Programs (Count) |
|-------------|-----------------------|--------------------------|
| DFHAID      | AID key definitions   | All 17 CICS programs     |
| DFHBMSCA    | BMS attribute constants| All 17 CICS programs    |

---

## VSAM File Access Map

### Online CICS File Access (via EXEC CICS READ/WRITE/REWRITE/DELETE/BROWSE)

| Program    | ACCTFILE | CARDFILE | CUSTFILE | USRSEC | TRANSACT | CARDXREF | TCATBALF | Mode |
|-----------|----------|----------|----------|--------|----------|----------|----------|------|
| COSGN00C  |          |          |          | R      |          |          |          | Read |
| COACTVWC  | R        | R (alt)  | R        |        |          | R        |          | Read |
| COACTUPC  | RW       |          | RW       |        |          | R (alt)  |          | R/W  |
| COCRDLIC  |          | R/Browse |          |        |          |          |          | Read |
| COCRDSLC  |          | R        |          |        |          | R (alt)  |          | Read |
| COCRDUPC  |          | RW       |          |        |          |          |          | R/W  |
| COTRN00C  |          |          |          |        | R/Browse |          |          | Read |
| COTRN01C  |          |          |          |        | R        |          |          | Read |
| COTRN02C  | R        |          |          |        | W        | R        |          | R/W  |
| COBIL00C  | R        |          |          |        | W        | R        |          | R/W  |
| COUSR00C  |          |          |          | Browse |          |          |          | Read |
| COUSR01C  |          |          |          | W      |          |          |          | Write|
| COUSR02C  |          |          |          | RW     |          |          |          | R/W  |
| COUSR03C  |          |          |          | RD     |          |          |          | R/Del|

**Legend**: R=Read, W=Write, RW=Read+Rewrite, RD=Read+Delete, Browse=STARTBR/READNEXT, (alt)=Alternate index access

### Batch File Access (via SELECT/OPEN/READ/WRITE/CLOSE)

| Program    | ACCTFILE | CARDFILE | CUSTFILE | XREFFILE | TRANSACT | DALYTRAN | TCATBALF | DISCGRP | DALYREJS | STMTFILE | HTMLFILE | TRANTYPE | TRANCATG | DATEPARM | REPTFILE |
|-----------|----------|----------|----------|----------|----------|----------|----------|---------|----------|----------|----------|----------|----------|----------|----------|
| CBACT01C  | R        |          |          |          |          |          |          |         |          |          |          |          |          |          |          |
| CBACT02C  |          | R        |          |          |          |          |          |         |          |          |          |          |          |          |          |
| CBACT03C  |          |          |          | R        |          |          |          |         |          |          |          |          |          |          |          |
| CBACT04C  | I-O      |          |          | R        | W        |          | R        | R       |          |          |          |          |          |          |          |
| CBCUS01C  |          |          | R        |          |          |          |          |         |          |          |          |          |          |          |          |
| CBTRN01C  | R        | R        | R        | R        | R        | R        |          |         |          |          |          |          |          |          |          |
| CBTRN02C  | I-O      |          |          | R        | W        | R        | I-O      |         | W        |          |          |          |          |          |          |
| CBTRN03C  |          |          |          | R        | R        |          |          |         |          |          |          | R        | R        | R        | W        |
| CBSTM03A  |          |          |          |          |          |          |          |         |          | W        | W        |          |          |          |          |
| CBSTM03B  | R        |          | R        | R        | R (via TRNX)|       |          |         |          |          |          |          |          |          |          |
| CBEXPORT  | R        | R        | R        | R        | R        |          |          |         |          |          |          |          |          |          |          |
| CBIMPORT  |          | W        | W        | W        | W        |          |          |         |          |          |          |          |          |          |          |

**Legend**: R=Read (INPUT), W=Write (OUTPUT), I-O=Read+Rewrite (I-O mode)

---

## JCL Job-to-Program Mapping

| JCL Job    | Step        | Program Executed | Purpose                                    |
|-----------|-------------|------------------|--------------------------------------------|
| POSTTRAN  | STEP10R     | CBTRN02C         | Core transaction posting                   |
| INTCALC   | STEP10R     | CBACT04C         | Interest calculation                       |
| CREASTMT  | STEP040     | CBSTM03A         | Statement generation                       |
| TRANREPT  | STEP10R     | CBTRN03C         | Transaction detail report                  |
| READACCT  | STEP10R     | CBACT01C         | Read/print account data                    |
| READCARD  | STEP10R     | CBACT02C         | Read/print card data                       |
| READCUST  | STEP10R     | CBCUS01C         | Read/print customer data                   |
| READXREF  | STEP10R     | CBACT03C         | Read/print cross-reference data            |
| CBEXPORT  | STEP10R     | CBEXPORT         | Export all data                            |
| CBIMPORT  | STEP10R     | CBIMPORT         | Import data                                |
| WAITSTEP  | WAIT        | COBSWAIT         | Wait utility                               |
| TRANREPT  | STEP05R     | SORT             | Sort transactions before report            |
| CREASTMT  | STEP010     | SORT             | Sort transactions for statements           |
| COMBTRAN  | (multiple)  | SORT + IDCAMS    | Combine daily into master                  |

**Note**: Most data refresh jobs (ACCTFILE, CARDFILE, CUSTFILE, etc.) use **IDCAMS REPRO** utility, not COBOL programs.

---

## JCL Data Lineage

### Core Batch Cycle Data Flow

```
                        ┌──────────────────┐
                        │  DALYTRAN.PS     │ (Daily transaction input)
                        │  (Sequential)    │
                        └────────┬─────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  POSTTRAN (CBTRN02C)    │
                    │  Transaction Posting     │
                    └──┬─────┬─────┬─────┬───┘
                       │     │     │     │
              ┌────────▼┐  ┌▼─────▼┐  ┌─▼────────┐  ┌──────────┐
              │TRANSACT │  │ACCTFILE│  │TCATBALF  │  │DALYREJS  │
              │(Output) │  │(I-O)  │  │(I-O)     │  │(Rejects) │
              │VSAM KSDS│  │Update │  │Update    │  │Sequential│
              └────┬────┘  └───┬───┘  └─────┬───┘  └──────────┘
                   │           │            │
         ┌─────────▼──────┐    │     ┌──────▼──────┐
         │  INTCALC       │    │     │  (Already   │
         │  (CBACT04C)    │◄───┘     │   updated)  │
         │  Interest Calc │          └─────────────┘
         └──┬──────┬──────┘
            │      │
     ┌──────▼┐  ┌──▼──────┐
     │TRANSACT│  │ACCTFILE │
     │(New    │  │(Rewrite │
     │interest│  │ balance)│
     │records)│  └─────────┘
     └───┬────┘
         │
    ┌────▼────────────┐     ┌──────────────────┐
    │  TRANBKP        │     │  COMBTRAN         │
    │  Backup to GDG  │     │  SORT + REPRO     │
    │  TRANSACT.BKUP  │     │  into master      │
    └─────────────────┘     └────────┬──────────┘
                                     │
                        ┌────────────▼────────────┐
                        │  CREASTMT (CBSTM03A)    │
                        │  Statement Generation    │
                        └──┬──────────────────┬───┘
                           │                  │
                    ┌──────▼──────┐    ┌──────▼──────┐
                    │ STATEMNT.PS │    │STATEMNT.HTML│
                    │ (Text stmt) │    │ (HTML stmt) │
                    └─────────────┘    └─────────────┘
```

### CREASTMT (Statement Generation) - Detailed File Flow

| Step     | Program   | Input Files                              | Output Files                          |
|----------|-----------|------------------------------------------|---------------------------------------|
| STEP010  | SORT      | TRANSACT.VSAM.KSDS                       | TRXFL.SEQ (sorted sequential)         |
| STEP020  | IDCAMS    | TRXFL.SEQ                                | TRXFL.VSAM.KSDS (sorted VSAM)        |
| STEP030  | IEFBR14   | (none)                                   | Delete old STATEMNT.HTML, STATEMNT.PS |
| STEP040  | CBSTM03A  | TRXFL.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML      |

### TRANREPT (Transaction Report) - Detailed File Flow

| Step     | Program   | Input Files                              | Output Files                          |
|----------|-----------|------------------------------------------|---------------------------------------|
| STEP05R  | REPROC    | TRANSACT.VSAM.KSDS                       | TRANSACT.BKUP(+1) (GDG backup)       |
| STEP05R  | SORT      | TRANSACT.BKUP(+1)                        | TRANSACT.DALY(+1) (sorted daily)      |
| STEP10R  | CBTRN03C  | TRANSACT.DALY, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT(+1)           |

### POSTTRAN (Transaction Posting) - Detailed File Flow

| Step     | Program   | Input Files                              | Output Files                          |
|----------|-----------|------------------------------------------|---------------------------------------|
| STEP10R  | CBTRN02C  | DALYTRAN, CARDXREF.VSAM.KSDS            | TRANSACT (output), ACCTFILE (I-O), TCATBALF (I-O), DALYREJS (rejects) |

### Data Refresh Jobs - File Mapping

| Job       | Source (PS/Sequential)                  | Target (VSAM KSDS)                       |
|-----------|-----------------------------------------|------------------------------------------|
| ACCTFILE  | `CARDDEMO.ACCTDATA.PS`                 | `CARDDEMO.ACCTDATA.VSAM.KSDS`           |
| CARDFILE  | `CARDDEMO.CARDDATA.PS`                 | `CARDDEMO.CARDDATA.VSAM.KSDS`           |
| CUSTFILE  | `CARDDEMO.CUSTDATA.PS`                 | `CARDDEMO.CUSTDATA.VSAM.KSDS`           |
| XREFFILE  | `CARDDEMO.CARDXREF.PS`                 | `CARDDEMO.CARDXREF.VSAM.KSDS`           |
| TRANFILE  | `CARDDEMO.DALYTRAN.PS.INIT`            | `CARDDEMO.TRANSACT.VSAM.KSDS`           |
| DUSRSECJ  | `CARDDEMO.USRSEC.PS`                   | `CARDDEMO.USRSEC.VSAM.KSDS`             |
| TRANTYPE  | `CARDDEMO.TRANTYPE.PS`                 | `CARDDEMO.TRANTYPE.VSAM.KSDS`           |
| TRANCATG  | `CARDDEMO.TRANCATG.PS`                 | `CARDDEMO.TRANCATG.VSAM.KSDS`           |
| TCATBALF  | `CARDDEMO.TCATBALF.PS`                 | `CARDDEMO.TCATBALF.VSAM.KSDS`           |
| DISCGRP   | `CARDDEMO.DISCGRP.PS`                  | `CARDDEMO.DISCGRP.VSAM.KSDS`            |

---

## Batch Processing Flow

### Nightly Batch Cycle (Recommended Execution Order)

```
Step 1: CLOSEFIL  ─── Close CICS files for batch exclusive access
    │
Step 2: Data Refresh (parallel)
    ├── ACCTFILE  ─── Refresh account VSAM
    ├── CARDFILE  ─── Refresh card VSAM
    ├── CUSTFILE  ─── Refresh customer VSAM
    ├── XREFFILE  ─── Refresh cross-reference VSAM
    └── TRANFILE  ─── Load daily transactions
    │
Step 3: POSTTRAN  ─── Post daily transactions (CBTRN02C)
    │                  Reads: DALYTRAN, XREFFILE
    │                  Updates: ACCTFILE, TCATBALF
    │                  Creates: TRANSACT records, DALYREJS rejects
    │
Step 4: INTCALC   ─── Calculate interest (CBACT04C)
    │                  Reads: TCATBALF, XREFFILE, DISCGRP
    │                  Updates: ACCTFILE
    │                  Creates: TRANSACT interest records
    │
Step 5: TRANBKP   ─── Backup transactions to GDG
    │
Step 6: COMBTRAN  ─── Combine/sort transactions
    │
Step 7: CREASTMT  ─── Generate statements (CBSTM03A -> CBSTM03B)
    │                  Reads: TRANSACT, XREFFILE, CUSTFILE, ACCTFILE
    │                  Creates: STATEMNT.PS (text), STATEMNT.HTML
    │
Step 8: TRANREPT  ─── Generate daily transaction report (CBTRN03C)
    │                  Reads: TRANSACT, XREFFILE, TRANTYPE, TRANCATG
    │                  Creates: TRANREPT GDG
    │
Step 9: TRANIDX   ─── Rebuild alternate indexes
    │
Step 10: OPENFIL  ─── Reopen CICS files for online access
```

---

## CICS Transaction Routing

### Transaction ID to Program Mapping

| Transaction ID | Program    | Description                   | Entry Point     |
|---------------|------------|-------------------------------|-----------------|
| CC00          | COSGN00C   | Sign-on                      | Initial entry    |
| CM00          | COMEN01C   | Main Menu (Regular)          | After sign-on    |
| CA00          | COADM01C   | Admin Menu                   | After sign-on    |
| CA01          | COACTVWC   | Account View                 | From main menu   |
| CA02          | COACTUPC   | Account Update               | From main menu   |
| CC01          | COCRDLIC   | Card List                    | From main menu   |
| CC02          | COCRDSLC   | Card Detail                  | From card list   |
| CC03          | COCRDUPC   | Card Update                  | From card list   |
| CT00          | COTRN00C   | Transaction List             | From main menu   |
| CT01          | COTRN01C   | Transaction View             | From tran list   |
| CT02          | COTRN02C   | Transaction Add              | From main menu   |
| CR00          | CORPT00C   | Reports                      | From main menu   |
| CB00          | COBIL00C   | Bill Payment                 | From main menu   |
| CU00          | COUSR00C   | User List                    | From admin menu  |
| CU01          | COUSR01C   | User Add                     | From user list   |
| CU02          | COUSR02C   | User Update                  | From user list   |
| CU03          | COUSR03C   | User Delete                  | From user list   |

---

## BMS Map-to-Program Binding

| BMS Map File         | Map Name  | Bound Program | BMS Copybook          |
|---------------------|-----------|---------------|-----------------------|
| `COSGN00.bms`       | COSGN0A   | COSGN00C      | `COSGN00.CPY`        |
| `COMEN01.bms`       | COMEN1A   | COMEN01C      | `COMEN01.CPY`        |
| `COADM01.bms`       | COADM1A   | COADM01C      | `COADM01.CPY`        |
| `COACTVW.bms`       | COACTVA   | COACTVWC      | `COACTVW.CPY`        |
| `COACTUP.bms`       | COACTA    | COACTUPC      | `COACTUP.CPY`        |
| `COCRDLI.bms`       | CCRDLA    | COCRDLIC      | `COCRDLI.CPY`        |
| `COCRDSL.bms`       | CCRDSA    | COCRDSLC      | `COCRDSL.CPY`        |
| `COCRDUP.bms`       | CCRDUA    | COCRDUPC      | `COCRDUP.CPY`        |
| `COTRN00.bms`       | COTRN0A   | COTRN00C      | `COTRN00.CPY`        |
| `COTRN01.bms`       | COTRN1A   | COTRN01C      | `COTRN01.CPY`        |
| `COTRN02.bms`       | COTRN2A   | COTRN02C      | `COTRN02.CPY`        |
| `CORPT00.bms`       | CORPT0A   | CORPT00C      | `CORPT00.CPY`        |
| `COBIL00.bms`       | COBIL0A   | COBIL00C      | `COBIL00.CPY`        |
| `COUSR00.bms`       | COUSR0A   | COUSR00C      | `COUSR00.CPY`        |
| `COUSR01.bms`       | COUSR1A   | COUSR01C      | `COUSR01.CPY`        |
| `COUSR02.bms`       | COUSR2A   | COUSR02C      | `COUSR02.CPY`        |
| `COUSR03.bms`       | COUSR3A   | COUSR03C      | `COUSR03.CPY`        |

---

## Cross-Reference: Shared Service Dependencies

### Most-Connected Entities (by program usage)

| Entity / File  | Programs Reading | Programs Writing | Total Dependents | Risk Level |
|---------------|------------------|------------------|-----------------|------------|
| CARDXREF      | 12               | 1 (CBIMPORT)     | 13              | **HIGH**   |
| ACCTFILE      | 8                | 4                | 12              | **HIGH**   |
| TRANSACT      | 8                | 4                | 12              | **HIGH**   |
| CUSTFILE      | 6                | 1                | 7               | MEDIUM     |
| CARDFILE      | 5                | 1                | 6               | MEDIUM     |
| USRSEC        | 5                | 3                | 8               | MEDIUM     |
| TCATBALF      | 2                | 2                | 4               | MEDIUM     |
| COCOM01Y      | 17 (all CICS)    | N/A              | 17              | **HIGH**   |
| CVTRA05Y      | 11               | N/A              | 11              | **HIGH**   |
| CVACT01Y      | 10               | N/A              | 10              | **HIGH**   |

### Critical Path Analysis

The following data files are on the critical path for the nightly batch cycle. Any failure in these files blocks downstream processing:

1. **DALYTRAN** -> Required by POSTTRAN -> blocks all downstream
2. **CARDXREF.VSAM.KSDS** -> Required by POSTTRAN, INTCALC, CREASTMT, TRANREPT
3. **ACCTFILE.VSAM.KSDS** -> Updated by POSTTRAN and INTCALC, read by CREASTMT
4. **TRANSACT.VSAM.KSDS** -> Written by POSTTRAN, read by CREASTMT and TRANREPT
