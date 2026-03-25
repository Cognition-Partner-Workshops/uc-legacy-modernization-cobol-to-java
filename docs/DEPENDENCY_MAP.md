# CardDemo Dependency Map

> **Generated from:** static analysis of CALL statements, COPY directives, EXEC CICS commands, and JCL DD statements
>
> **Purpose:** Visualize the call graph, copybook dependencies, and data lineage across the CardDemo application

---

## Table of Contents

1. [Program-to-Program Call Graph](#1-program-to-program-call-graph)
2. [Program-to-Copybook Dependencies](#2-program-to-copybook-dependencies)
3. [Program-to-BMS Map Bindings](#3-program-to-bms-map-bindings)
4. [JCL Job-to-Program Execution](#4-jcl-job-to-program-execution)
5. [JCL Job Data Lineage (File I/O)](#5-jcl-job-data-lineage-file-io)
6. [VSAM File Access by Program](#6-vsam-file-access-by-program)
7. [Batch Processing Flow](#7-batch-processing-flow)
8. [Screen Navigation Flow](#8-screen-navigation-flow)

---

## 1. Program-to-Program Call Graph

Direct `CALL` statements between programs.

```
CBSTM03A ──CALL──> CBSTM03B        (Statement file processing subroutine)
CBSTM03A ──CALL──> CEE3ABD         (LE abnormal termination)

CORPT00C ──CALL──> CSUTLDTC        (Date validation for report date range)
COTRN02C ──CALL──> CSUTLDTC        (Date validation for transaction entry)

CSUTLDTC ──CALL──> CEEDAYS         (LE date conversion API)

COBSWAIT ──CALL──> MVSWAIT         (Assembler: MVS WAIT macro)

CBACT01C ──CALL──> COBDATFT        (Assembler: date formatting)
CBACT01C ──CALL──> CEE3ABD         (LE abnormal termination)

CBACT02C ──CALL──> CEE3ABD         (LE abnormal termination)
CBACT03C ──CALL──> CEE3ABD         (LE abnormal termination)
CBACT04C ──CALL──> CEE3ABD         (LE abnormal termination)
CBCUS01C ──CALL──> CEE3ABD         (LE abnormal termination)
CBTRN01C ──CALL──> CEE3ABD         (LE abnormal termination)
CBTRN02C ──CALL──> CEE3ABD         (LE abnormal termination)
CBTRN03C ──CALL──> CEE3ABD         (LE abnormal termination)
CBEXPORT ──CALL──> CEE3ABD         (LE abnormal termination)
CBIMPORT ──CALL──> CEE3ABD         (LE abnormal termination)
```

### Call Graph Summary

```
                    ┌─────────────┐
                    │  CEEDAYS    │  (LE Runtime)
                    └──────▲──────┘
                           │
                    ┌──────┴──────┐
                    │  CSUTLDTC   │  (Date Utility)
                    └──────▲──────┘
                      ┌────┴────┐
                      │         │
               ┌──────┴──┐ ┌───┴──────┐
               │ CORPT00C│ │ COTRN02C │
               └─────────┘ └──────────┘

                    ┌─────────────┐
                    │  CBSTM03B   │  (File Subroutine)
                    └──────▲──────┘
                           │
                    ┌──────┴──────┐
                    │  CBSTM03A   │  (Statement Generator)
                    └─────────────┘

                    ┌─────────────┐
                    │  MVSWAIT    │  (Assembler)
                    └──────▲──────┘
                           │
                    ┌──────┴──────┐
                    │  COBSWAIT   │  (Wait Utility)
                    └─────────────┘

                    ┌─────────────┐
                    │  COBDATFT   │  (Assembler)
                    └──────▲──────┘
                           │
                    ┌──────┴──────┐
                    │  CBACT01C   │  (Account File Reader)
                    └─────────────┘
```

### CICS Inter-Program Navigation (XCTL / RETURN TRANSID)

Online programs navigate via CICS pseudo-conversational flow, controlled through the COMMAREA (`COCOM01Y`):

```
COSGN00C (Sign-On)
    │
    ├── Regular User ──> COMEN01C (Main Menu)
    │                        │
    │                        ├──> COACTVWC (Account View)
    │                        │        └──> COACTUPC (Account Update)
    │                        │
    │                        ├──> COCRDLIC (Card List)
    │                        │        ├──> COCRDSLC (Card View)
    │                        │        └──> COCRDUPC (Card Update)
    │                        │
    │                        ├──> COTRN00C (Transaction List)
    │                        │        ├──> COTRN01C (Transaction View)
    │                        │        └──> COTRN02C (Transaction Add)
    │                        │
    │                        ├──> CORPT00C (Reports)
    │                        │
    │                        └──> COBIL00C (Bill Payment)
    │
    └── Admin User ──> COADM01C (Admin Menu)
                            │
                            ├──> COUSR00C (User List)
                            │        ├──> COUSR01C (User Add)
                            │        ├──> COUSR02C (User Update)
                            │        └──> COUSR03C (User Delete)
                            │
                            └──> (Returns to Main Menu)
```

---

## 2. Program-to-Copybook Dependencies

### Online Programs

| Program | Copybooks Used |
|---------|---------------|
| COSGN00C | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COMEN01C | COCOM01Y, COMEN01, COMEN02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COACTVWC | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| COACTUPC | COCOM01Y, COACTUP, COTTL01Y, CSDAT01Y, CSLKPCDY, CSMSG01Y, CSMSG02Y, CSSETATY, CSUSR01Y, CSUTLDPY, CVACT01Y, CVACT03Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, CSUTLDWY, DFHAID, DFHBMSCA |
| COCRDLIC | COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| COCRDSLC | COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| COCRDUPC | COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCRD01Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| COTRN00C | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COTRN01C | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COTRN02C | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| CORPT00C | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COBIL00C | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| COADM01C | COCOM01Y, COADM01, COADM02Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR00C | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR01C | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR02C | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| COUSR03C | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

### Batch Programs

| Program | Copybooks Used |
|---------|---------------|
| CBACT01C | CVACT01Y, CODATECN |
| CBACT02C | CVACT02Y |
| CBACT03C | CVACT03Y |
| CBACT04C | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y |
| CBCUS01C | CVCUS01Y |
| CBTRN01C | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y, CVTRA06Y |
| CBTRN02C | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y |
| CBTRN03C | CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y |
| CBSTM03A | COSTM01, CUSTREC, CVACT01Y, CVACT03Y |
| CBEXPORT | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVEXPORT, CVTRA05Y |
| CBIMPORT | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVEXPORT, CVTRA05Y |

### Most-Referenced Copybooks (Dependency Fan-In)

| Copybook | Referenced By (Count) | Description |
|----------|:---------------------:|-------------|
| COCOM01Y | 17 programs | Common communication area |
| DFHAID   | 17 programs | CICS AID key definitions |
| DFHBMSCA | 17 programs | BMS attribute constants |
| COTTL01Y | 17 programs | Title/header layout |
| CSDAT01Y | 17 programs | Date working storage |
| CSMSG01Y | 17 programs | Short message area |
| CSUSR01Y | 12 programs | User security record |
| CVACT01Y | 9 programs  | Account master record |
| CVTRA05Y | 8 programs  | Transaction record |
| CVACT03Y | 8 programs  | Card cross-reference |
| CVCUS01Y | 6 programs  | Customer master record |
| CVACT02Y | 6 programs  | Card master record |

---

## 3. Program-to-BMS Map Bindings

Each online program is bound to exactly one BMS mapset:

| Program | BMS Map | Map Name | Screen Title |
|---------|---------|----------|--------------|
| COSGN00C | COSGN00.bms | COSGN0A | Sign On |
| COMEN01C | COMEN01.bms | COMEN1A | Main Menu |
| COACTVWC | COACTVW.bms | CACTVWA | Account View |
| COACTUPC | COACTUP.bms | CACTUPA | Account Update |
| COCRDLIC | COCRDLI.bms | CCRDLIA | Card List |
| COCRDSLC | COCRDSL.bms | CCRDSLA | Card View |
| COCRDUPC | COCRDUP.bms | CCRDUPA | Card Update |
| COTRN00C | COTRN00.bms | COTRN0A | Transaction List |
| COTRN01C | COTRN01.bms | COTRN1A | Transaction View |
| COTRN02C | COTRN02.bms | COTRN2A | Transaction Add |
| CORPT00C | CORPT00.bms | CORPT0A | Report Request |
| COBIL00C | COBIL00.bms | COBIL0A | Bill Payment |
| COADM01C | COADM01.bms | COADM1A | Admin Menu |
| COUSR00C | COUSR00.bms | COUSR0A | User List |
| COUSR01C | COUSR01.bms | COUSR1A | User Add |
| COUSR02C | COUSR02.bms | COUSR2A | User Update |
| COUSR03C | COUSR03.bms | COUSR3A | User Delete |

---

## 4. JCL Job-to-Program Execution

### Batch COBOL Programs

| JCL Job | COBOL Program | Purpose |
|---------|---------------|---------|
| POSTTRAN | CBTRN02C | Post daily transactions |
| INTCALC | CBACT04C | Calculate interest |
| CREASTMT | CBSTM03A (calls CBSTM03B) | Generate statements |
| TRANREPT | CBTRN03C | Generate transaction report |
| READACCT | CBACT01C | Validate account file |
| READCARD | CBACT02C | Validate card file |
| READCUST | CBCUS01C | Validate customer file |
| READXREF | CBACT03C | Validate cross-ref file |
| CBEXPORT | CBEXPORT | Export VSAM to flat files |
| CBIMPORT | CBIMPORT | Import flat files to VSAM |
| WAITSTEP | COBSWAIT | Wait/delay step |

### Utility Programs

| JCL Job | Utility | Purpose |
|---------|---------|---------|
| ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE | IDCAMS | Define/load VSAM clusters |
| CLOSEFIL, OPENFIL | SDSF | CICS file open/close commands |
| COMBTRAN | SORT, IDCAMS | Sort and combine transactions |
| CREASTMT | SORT | Sort transactions by card+ID |
| TRANREPT | SORT | Sort transactions for reporting |
| DUSRSECJ | IEBGENER | Copy flat file to sequential |
| TXT2PDF1 | IKJEFT1B | Convert text to PDF |
| FTPJCL | FTP | File transfer |
| CBADMCDJ | DFHCSDUP | Load CICS CSD definitions |

---

## 5. JCL Job Data Lineage (File I/O)

### VSAM Files: Read and Write Access by Job

| VSAM Dataset | Loaded By | Read By | Updated By |
|-------------|-----------|---------|------------|
| ACCTDATA.VSAM.KSDS | ACCTFILE | CREASTMT, READACCT | POSTTRAN, INTCALC |
| CARDDATA.VSAM.KSDS | CARDFILE | READCARD | -- |
| CUSTDATA.VSAM.KSDS | CUSTFILE | CREASTMT, READCUST | -- |
| CARDXREF.VSAM.KSDS | XREFFILE | CREASTMT, READXREF | -- |
| TRANSACT.VSAM.KSDS | TRANFILE | CREASTMT, TRANREPT | POSTTRAN, COMBTRAN |
| DALYTRAN.VSAM.KSDS | (online) | POSTTRAN | (online COTRN02C) |
| USRSEC.VSAM.KSDS | DUSRSECJ | -- | (online COUSR*) |
| TCATBALF.VSAM.KSDS | TCATBALF | INTCALC | POSTTRAN |
| DISCGRP.VSAM.KSDS | DISCGRP | INTCALC | -- |
| TRANTYPE.VSAM.KSDS | TRANTYPE | TRANREPT | -- |
| TRANCATG.VSAM.KSDS | TRANCATG | TRANREPT | -- |
| DALYREPT.VSAM.KSDS | REPTFILE | -- | TRANREPT |

### Output Files Created by Batch Jobs

| Job | Output File | Format | Purpose |
|-----|------------|--------|---------|
| CREASTMT | STATEMNT.PS | Fixed-length text | Account statements (plain text) |
| CREASTMT | STATEMNT.HTML | HTML | Account statements (web format) |
| TRANREPT | DALYREPT | Print | Daily transaction report |
| TXT2PDF1 | STATEMNT.PS.PDF | PDF | Statement converted to PDF |
| CBEXPORT | (multiple flat files) | Fixed-length | Exported VSAM data |
| TRANBKP | TRANSACT backup GDG | Sequential | Transaction file backup |

### Batch Data Flow Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Online CICS │     │  DALYTRAN    │     │  TRANSACT    │
│  (COTRN02C)  │────>│  VSAM        │────>│  VSAM        │
│  Add Txn     │     │  (Daily)     │     │  (Master)    │
└──────────────┘     └──────┬───────┘     └──────┬───────┘
                            │                     │
                      POSTTRAN job           COMBTRAN job
                      (CBTRN02C)            (SORT+IDCAMS)
                            │                     │
                            ▼                     ▼
                    ┌──────────────┐     ┌──────────────┐
                    │  ACCTDATA    │     │  TRANSACT    │
                    │  (Balances   │     │  (Combined)  │
                    │   Updated)   │     │              │
                    └──────┬───────┘     └──────┬───────┘
                           │                     │
                     INTCALC job           CREASTMT job
                     (CBACT04C)           (CBSTM03A)
                           │                     │
                           ▼                     ▼
                    ┌──────────────┐     ┌──────────────┐
                    │  TCATBALF    │     │  STATEMNT.PS │
                    │  (Interest   │     │  STATEMNT    │
                    │   Applied)   │     │  .HTML       │
                    └──────────────┘     └──────────────┘
                                                │
                                          TXT2PDF1 job
                                                │
                                                ▼
                                        ┌──────────────┐
                                        │ STATEMNT.PDF │
                                        └──────────────┘
```

---

## 6. VSAM File Access by Program

### Online Programs (CICS EXEC READ/WRITE/REWRITE/DELETE)

| Program | VSAM Files Accessed | Operations |
|---------|--------------------|-----------:|
| COSGN00C | USRSEC | READ |
| COACTVWC | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA | READ |
| COACTUPC | ACCTDATA, CARDXREF, CUSTDATA | READ, REWRITE |
| COCRDLIC | CARDDATA, CARDXREF | READ, STARTBR, READNEXT |
| COCRDSLC | CARDDATA, CARDXREF, CUSTDATA | READ |
| COCRDUPC | CARDDATA, CARDXREF, CUSTDATA | READ, REWRITE |
| COTRN00C | TRANSACT | READ, STARTBR, READNEXT |
| COTRN01C | TRANSACT | READ |
| COTRN02C | ACCTDATA, CARDXREF, TRANSACT | READ, WRITE |
| CORPT00C | TRANSACT | READ, STARTBR, READNEXT |
| COBIL00C | ACCTDATA, CARDXREF, TRANSACT | READ, WRITE, REWRITE |
| COUSR00C | USRSEC | READ, STARTBR, READNEXT |
| COUSR01C | USRSEC | WRITE |
| COUSR02C | USRSEC | READ, REWRITE |
| COUSR03C | USRSEC | READ, DELETE |

### Batch Programs (File I/O)

| Program | VSAM/Files Accessed | Operations |
|---------|--------------------|-----------:|
| CBACT01C | ACCTDATA | READ (sequential) |
| CBACT02C | CARDDATA | READ (sequential) |
| CBACT03C | CARDXREF | READ (sequential) |
| CBACT04C | ACCTDATA, CARDXREF, TCATBALF, DISCGRP, TRANSACT | READ, REWRITE |
| CBCUS01C | CUSTDATA | READ (sequential) |
| CBTRN01C | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TRANSACT, DALYTRAN | READ |
| CBTRN02C | ACCTDATA, CARDXREF, TCATBALF, TRANSACT, DALYTRAN | READ, WRITE, REWRITE |
| CBTRN03C | CARDXREF, TRANTYPE, TRANCATG, TRANSACT, DALYREPT | READ, WRITE |
| CBSTM03A | TRANSACT (sorted copy), CARDXREF, ACCTDATA, CUSTDATA, STATEMNT files | READ, WRITE |
| CBSTM03B | STATEMNT.PS, STATEMNT.HTML | WRITE |
| CBEXPORT | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TRANSACT, flat files | READ, WRITE |
| CBIMPORT | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TRANSACT, flat files | READ, WRITE |

---

## 7. Batch Processing Flow

The nightly batch cycle runs in strict sequence. Each step depends on successful completion of its predecessor.

```
Step 1: CLOSEFIL    Close CICS files for exclusive batch access
            │
Step 2: ACCTFILE ─┐
        CARDFILE  │  Refresh VSAM master files from
        CUSTFILE  ├  updated flat-file sources
        XREFFILE  │  (can run in parallel)
        TRANFILE ─┘
            │
Step 3: POSTTRAN    Post daily transactions (CBTRN02C)
            │       Reads DALYTRAN, updates ACCTDATA + TRANSACT
            │
Step 4: INTCALC     Calculate interest charges (CBACT04C)
            │       Reads DISCGRP, updates ACCTDATA + TCATBALF
            │
Step 5: TRANBKP     Backup transaction files to GDG
            │
Step 6: COMBTRAN    Combine daily transactions into master
            │       Uses SORT + IDCAMS REPRO
            │
Step 7: CREASTMT    Generate account statements (CBSTM03A)
            │       Reads TRANSACT, CARDXREF, ACCTDATA, CUSTDATA
            │       Produces STATEMNT.PS + STATEMNT.HTML
            │
Step 8: TRANREPT    Generate daily transaction report (CBTRN03C)
            │       Reads TRANSACT, TRANTYPE, TRANCATG
            │
Step 9: TRANIDX     Rebuild alternate indexes on TRANSACT
            │
Step 10: OPENFIL   Reopen CICS files for online access
```

---

## 8. Screen Navigation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    COSGN00C (CC00)                           │
│                    Sign-On Screen                            │
│         Username: ________  Password: ________              │
└─────────────────────┬───────────────────┬───────────────────┘
                      │                   │
              Regular User            Admin User
                      │                   │
                      ▼                   ▼
┌─────────────────────────────┐  ┌─────────────────────────┐
│     COMEN01C (CM00)         │  │    COADM01C (CA90)      │
│     Main Menu               │  │    Admin Menu           │
│  1. Account View            │  │  1. User List           │
│  2. Card List               │  │  2. (Return to Main)    │
│  3. Transaction List        │  └───────────┬─────────────┘
│  4. Transaction Add         │              │
│  5. Bill Payment            │              ▼
│  6. Transaction Report      │  ┌─────────────────────────┐
└──┬───┬───┬───┬───┬───┬──────┘  │    COUSR00C (CU00)      │
   │   │   │   │   │   │        │    User List             │
   │   │   │   │   │   │        │  Select > Add/Update/Del │
   │   │   │   │   │   │        └──┬──────┬──────┬─────────┘
   │   │   │   │   │   │           │      │      │
   │   │   │   │   │   │           ▼      ▼      ▼
   │   │   │   │   │   │        COUSR01C COUSR02C COUSR03C
   │   │   │   │   │   │        Add User Upd User Del User
   │   │   │   │   │   │
   │   │   │   │   │   └──> CORPT00C - Report Request
   │   │   │   │   └──────> COBIL00C - Bill Payment
   │   │   │   └──────────> COTRN02C - Transaction Add
   │   │   └──────────────> COTRN00C - Transaction List
   │   │                        ├──> COTRN01C - Transaction View
   │   │                        └──> COTRN02C - Transaction Add
   │   └──────────────────> COCRDLIC - Card List
   │                            ├──> COCRDSLC - Card View
   │                            └──> COCRDUPC - Card Update
   └──────────────────────> COACTVWC - Account View
                                └──> COACTUPC - Account Update
```
