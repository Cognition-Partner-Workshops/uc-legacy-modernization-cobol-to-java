# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** Static analysis of CALL, EXEC CICS XCTL/LINK, COPY, and JCL EXEC/DD statements  
> **Purpose:** Call graph, copybook dependencies, and data lineage for migration planning.

---

## Table of Contents

1. [Online Program Call Graph](#1-online-program-call-graph)
2. [Batch Program Call Graph](#2-batch-program-call-graph)
3. [Copybook Dependency Matrix](#3-copybook-dependency-matrix)
4. [VSAM File Access Matrix](#4-vsam-file-access-matrix)
5. [JCL Job → Program → File Lineage](#5-jcl-job--program--file-lineage)
6. [Batch Processing Pipeline](#6-batch-processing-pipeline)
7. [BMS Map Dependencies](#7-bms-map-dependencies)
8. [Optional Module Dependencies](#8-optional-module-dependencies)
9. [Cross-Cutting Concerns](#9-cross-cutting-concerns)
10. [Migration Dependency Order](#10-migration-dependency-order)

---

## 1. Online Program Call Graph

### 1.1 Navigation Flow (EXEC CICS XCTL)

XCTL transfers control to another program, passing the COMMAREA. This is the primary navigation mechanism.

```
                            ┌─────────────┐
                            │  COSGN00C   │  Sign-on (CC00)
                            │  Entry Point│
                            └──────┬──────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │ (Admin)      │              │ (User)
                    ▼              │              ▼
             ┌─────────────┐      │       ┌─────────────┐
             │  COADM01C   │      │       │  COMEN01C   │
             │  Admin Menu │      │       │  Main Menu  │
             └──────┬──────┘      │       └──────┬──────┘
                    │             │              │
        ┌───┬───┬──┴──┬───┬───┐  │  ┌───┬───┬──┴──┬───┬───┬───┬───┬───┬───┬───┐
        │   │   │     │   │   │  │  │   │   │     │   │   │   │   │   │   │   │
        ▼   ▼   ▼     ▼   ▼   ▼  │  ▼   ▼   ▼     ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼
      USR USR USR   USR TRT TRT  │ ATV ATU CRL   CRS CRU TR0 TR1 TR2 RPT BIL PAU
      00  01  02    03  LIC UPC  │ WC  PC  IC    LC  PC  0C  1C  2C  00  00  S0C
```

### 1.2 Detailed XCTL Transfers

| Source Program | Target Program | Condition / Trigger |
|---------------|---------------|---------------------|
| **COSGN00C** | COADM01C | User type = 'A' (Admin) |
| **COSGN00C** | COMEN01C | User type = 'U' (Regular) |
| **COMEN01C** | COACTVWC | Menu option 1 - Account View |
| **COMEN01C** | COACTUPC | Menu option 2 - Account Update |
| **COMEN01C** | COCRDLIC | Menu option 3 - Card List |
| **COMEN01C** | COCRDSLC | Menu option 4 - Card View |
| **COMEN01C** | COCRDUPC | Menu option 5 - Card Update |
| **COMEN01C** | COTRN00C | Menu option 6 - Transaction List |
| **COMEN01C** | COTRN01C | Menu option 7 - Transaction View |
| **COMEN01C** | COTRN02C | Menu option 8 - Transaction Add |
| **COMEN01C** | CORPT00C | Menu option 9 - Reports |
| **COMEN01C** | COBIL00C | Menu option 10 - Bill Payment |
| **COMEN01C** | COPAUS0C | Menu option 11 - Auth View (optional) |
| **COADM01C** | COUSR00C | Admin option 1 - User List |
| **COADM01C** | COUSR01C | Admin option 2 - User Add |
| **COADM01C** | COUSR02C | Admin option 3 - User Update |
| **COADM01C** | COUSR03C | Admin option 4 - User Delete |
| **COADM01C** | COTRTLIC | Admin option 5 - Tran Type List (DB2) |
| **COADM01C** | COTRTUPC | Admin option 6 - Tran Type Maint (DB2) |
| **COACTVWC** | *(CDEMO-TO-PROGRAM)* | Return to caller via COMMAREA |
| **COACTUPC** | *(CDEMO-TO-PROGRAM)* | Return to caller via COMMAREA |
| **COCRDLIC** | COMEN01C | PF3 - Return to menu (via LIT-MENUPGM) |
| **COCRDLIC** | COCRDSLC | Select card - View detail (via CCARD-NEXT-PROG) |
| **COCRDLIC** | COCRDUPC | Select card - Update (via CCARD-NEXT-PROG) |
| **COCRDSLC** | *(CDEMO-TO-PROGRAM)* | Return to caller |
| **COCRDUPC** | *(CDEMO-TO-PROGRAM)* | Return to caller |
| **COTRTLIC** | *(CDEMO-TO-PROGRAM)* | Return to menu |
| **COTRTLIC** | COTRTUPC | Select type - Edit (via LIT-ADDTPGM) |
| **COTRTUPC** | *(CDEMO-TO-PROGRAM)* | Return to caller |

### 1.3 CALL Statements (Online Programs)

| Caller | Called Program | Purpose |
|--------|--------------|---------|
| **COTRN02C** | CSUTLDTC | Date validation (transaction date fields) |
| **CORPT00C** | CSUTLDTC | Date validation (report date range) |

### 1.4 EXEC CICS LINK Statements

| Caller | Linked Program | Purpose |
|--------|---------------|---------|
| **COPAUS1C** | *(via LINK)* | Authorization detail sub-call |

---

## 2. Batch Program Call Graph

### 2.1 Direct CALL Dependencies

```
CBSTM03A (Statement Driver)
    └── CALL 'CBSTM03B'  (Page Formatter) ── 11 call sites

CBACT01C (Account Reader)
    └── CALL 'COBDATFT'  (Date Format ASM)

COBSWAIT (Wait Utility)
    └── CALL 'MVSWAIT'   (ASM wait routine)

CSUTLDTC (Date Utility)
    └── CALL 'CEEDAYS'   (LE date service)

CBTRN01C, CBTRN02C, CBTRN03C, CBACT04C,
CBACT02C, CBACT03C, CBCUS01C, CBSTM03A,
CBEXPORT, CBIMPORT
    └── CALL 'CEE3ABD'   (LE abnormal termination - error handling)
```

### 2.2 Call Hierarchy Tree

```
Level 0 (JCL-invoked)          Level 1 (CALL)          Level 2 (CALL)
─────────────────────          ──────────────          ──────────────
CBTRN02C (Posting)............ CEE3ABD
CBACT04C (Interest)........... CEE3ABD
CBTRN01C (Daily Trans)........ CEE3ABD
CBTRN03C (Trans Report)....... CEE3ABD
CBSTM03A (Statements)......... CBSTM03B .............. (leaf)
                                CEE3ABD
CBACT01C (Account Read)....... COBDATFT (ASM) ........ (leaf)
                                CEE3ABD
CBACT02C (Card Read).......... CEE3ABD
CBACT03C (XREF Read).......... CEE3ABD
CBCUS01C (Customer Read)...... CEE3ABD
CBEXPORT (Export)............. CEE3ABD
CBIMPORT (Import)............. CEE3ABD
COBSWAIT (Wait)............... MVSWAIT (ASM) ......... (leaf)
CSUTLDTC (Date Util).......... CEEDAYS (LE svc)
```

---

## 3. Copybook Dependency Matrix

### 3.1 Online Programs → Copybooks

| Copybook ↓ \ Program → | SGN00 | MEN01 | ADM01 | ACTVW | ACTUP | CRDLI | CRDSL | CRDUP | TRN00 | TRN01 | TRN02 | RPT00 | BIL00 | USR00 | USR01 | USR02 | USR03 |
|------------------------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
| COCOM01Y (COMMAREA) | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| COTTL01Y (Title) | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSDAT01Y (Date/Time) | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| CSMSG01Y (Messages) | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHAID (CICS AID keys) | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |
| DFHBMSCA (BMS attrs) | | | | x | x | x | x | x | | | | | | | | | |
| CSUSR01Y (Security) | x | | | | | | | | | | | | | x | x | x | x |
| COMEN02Y (Menu Opts) | | x | | | | | | | | | | | | | | | |
| COADM02Y (Admin Opts) | | | x | | | | | | | | | | | | | | |
| CSMSG02Y (Msg ext) | | | | | x | | | x | | | | | | | | x | |
| CVCRD01Y (Card Work) | | | | | x | x | x | x | | | | | | | | | |
| CSLKPCDY (Lookup Codes) | | | | | x | | | | | | | | | | | | |
| CSUTLDWY (Date Valid WS) | | | | | x | | | | | | | | | | | | |
| CVACT01Y (Account) | | | | x | x | | | | | | x | | x | | | | |
| CVACT02Y (Card) | | | | | | | | | | | | | | | | | |
| CVACT03Y (XREF) | | | | | | | | | | | x | | x | | | | |
| CVTRA05Y (Transaction) | | | | | | | | | | x | x | | x | | | | |
| CSSETATY (Set Attrs) | | | | | x | | | x | | | | | | | | | |
| CSSTRPFY (String Proc) | | | | | x | | | x | | | | | | | | | |

### 3.2 Batch Programs → Copybooks

| Copybook ↓ \ Program → | ACT01 | ACT02 | ACT03 | ACT04 | CUS01 | TRN01 | TRN02 | TRN03 | STM3A | STM3B | EXPRT | IMPRT |
|------------------------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
| CVACT01Y (Account) | x | | | x | | | x | | x | | x | x |
| CVACT02Y (Card) | | x | | | | | | | | | x | x |
| CVACT03Y (XREF) | | | x | | | | | | | | x | x |
| CVCUS01Y (Customer) | | | | | x | | | | | | x | x |
| CVTRA05Y (Transaction) | | | | | | x | x | x | | | x | x |
| CVTRA06Y (Daily Trans) | | | | | | x | x | | | | | |
| CVTRA01Y (Cat Balance) | | | | x | | | x | | | | | |
| CVTRA02Y (Disclosure) | | | | x | | | | | | | | |
| CVTRA03Y (Tran Type) | | | | | | | | x | | | | |
| CVTRA04Y (Tran Cat) | | | | | | | | x | | | | |
| CVTRA07Y (Report Fmt) | | | | | | | | x | | | | |
| COSTM01 (Stmt Trans) | | | | | | | | | x | | | |
| CODATECN (Date Conv) | x | | | | | | | | | | | |
| CVEXPORT (Export Fmt) | | | | | | | | | | | x | x |

### 3.3 Most-Referenced Copybooks (Ranked)

| Rank | Copybook | # Programs Using | Criticality |
|------|----------|-----------------|-------------|
| 1 | COCOM01Y | 17+ | Universal - all online programs |
| 2 | COTTL01Y | 17+ | Universal - screen headers |
| 3 | CSDAT01Y | 17+ | Universal - date/time |
| 4 | CSMSG01Y | 17+ | Universal - messages |
| 5 | CVACT01Y | 8 | High - Account record |
| 6 | CVTRA05Y | 7 | High - Transaction record |
| 7 | CVACT03Y | 5 | High - Cross-reference |
| 8 | CVCUS01Y | 5 | High - Customer record |
| 9 | CSUSR01Y | 5 | Medium - User security |
| 10 | CVCRD01Y | 4 | Medium - Card work areas |

---

## 4. VSAM File Access Matrix

### 4.1 Online Programs → VSAM Files

| VSAM File ↓ \ Program → | SGN00 | ACTVW | ACTUP | CRDLI | CRDSL | CRDUP | TRN00 | TRN01 | TRN02 | RPT00 | BIL00 | USR00 | USR01 | USR02 | USR03 |
|-------------------------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
| Account (ACCTDATA) | | R | R | | | | | | R | | R | | | | |
| Card (CARDDATA) | | | | R | R | R | | | | | | | | | |
| Customer (CUSTDATA) | | R | R | | | | | | | | | | | | |
| Card XREF (CARDXREF) | | | | R | R | R | | | R | | R | | | | |
| Transaction (TRANSACT) | | | | | | | R | R | RW | | RW | | | | |
| Daily Trans (DALYTRAN) | | | | | | | | | | | | | | | |
| Tran Type (TRANTYPE) | | | | | | | | | | | | | | | |
| Tran Category (TRANCATG) | | | | | | | | | | | | | | | |
| Cat Balance (TCATBALF) | | | | | | | | | | | | | | | |
| Disclosure (DISCGRP) | | | | | | | | | | | | | | | |
| User Security (USRSEC) | R | | | | | | | | | | | R | W | RW | RD |
| TD Queue (CSPT) | | | | | | | | | | W | | | | | |

**Legend:** R = Read, W = Write, RW = Read+Write, RD = Read+Delete

### 4.2 Batch Programs → Files

| File ↓ \ Program → | ACT01 | ACT02 | ACT03 | ACT04 | CUS01 | TRN01 | TRN02 | TRN03 | STM3A | STM3B | EXPRT | IMPRT |
|--------------------|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
| Account (ACCTDATA) | R | | | RW | | | RW | | R | | R | W |
| Card (CARDDATA) | | R | | | | | | | | | R | W |
| Customer (CUSTDATA) | | | | | R | | | | | | R | W |
| Card XREF (CARDXREF) | | | R | | | | | | R | | R | W |
| Transaction (TRANSACT) | | | | | | | RW | R | R | | R | W |
| Daily Trans (DALYTRAN) | | | | | | R | R | | | | | |
| Tran Type (TRANTYPE) | | | | | | | | R | | | | |
| Tran Category (TRANCATG) | | | | | | | | R | | | | |
| Cat Balance (TCATBALF) | | | | R | | | RW | | | | | |
| Disclosure (DISCGRP) | | | | R | | | | | | | | |
| Statement output | | | | | | | | | W | W | | |
| Report output | | | | | | | | W | | | | |
| Export file | | | | | | | | | | | W | R |

---

## 5. JCL Job → Program → File Lineage

### 5.1 Data Refresh Jobs

```
ACCTFILE.jcl ─── IDCAMS REPRO ─── ASCII flat file ──▶ Account VSAM KSDS
CARDFILE.jcl ─── IDCAMS REPRO ─── ASCII flat file ──▶ Card VSAM KSDS
CUSTFILE.jcl ─── IDCAMS REPRO ─── ASCII flat file ──▶ Customer VSAM KSDS
XREFFILE.jcl ─── IDCAMS REPRO ─── ASCII flat file ──▶ Card XREF VSAM KSDS
TRANFILE.jcl ─── IDCAMS REPRO ─── ASCII flat file ──▶ Transaction VSAM KSDS
DUSRSECJ.jcl ─── IEBGENER + IDCAMS ─── flat file ──▶ User Security VSAM KSDS
```

### 5.2 Batch Processing Jobs

```
POSTTRAN.jcl ─── CBTRN02C ─── reads: Daily Trans, Account, XREF, Cat Bal
                               writes: Transaction, Account (bal update), Cat Bal

INTCALC.jcl ──── CBACT04C ─── reads: Account, Cat Bal, Disclosure Group
                               writes: Account (interest applied)

CREASTMT.JCL ── SORT ──▶ sorted trans ──▶ CBSTM03A ──▶ CBSTM03B
                               reads: Transaction, Account, XREF
                               writes: Statement output (print)

TRANREPT.jcl ── CBTRN03C ─── reads: Transaction, Tran Type, Tran Category
                               writes: Report output (CSPT TD queue or print)

COMBTRAN.jcl ── SORT/MERGE ── merges: Daily Trans + Transaction Master
                               writes: Updated Transaction Master
```

### 5.3 Data Export/Import Jobs

```
CBEXPORT.jcl ── CBEXPORT ─── reads: Account, Card, Customer, XREF, Transaction
                               writes: Sequential export file (500-byte records)

CBIMPORT.jcl ── CBIMPORT ─── reads: Sequential export file
                               writes: Account, Card, Customer, XREF, Transaction
```

### 5.4 Utility Jobs

```
READACCT.jcl ── CBACT01C ─── reads: Account ──▶ calls COBDATFT for date format
READCARD.jcl ── CBACT02C ─── reads: Card
READCUST.jcl ── CBCUS01C ─── reads: Customer
READXREF.jcl ── CBACT03C ─── reads: Card XREF
TRANBKP.jcl ─── IDCAMS REPRO ─── Transaction VSAM ──▶ Backup dataset
WAITSTEP.jcl ── COBSWAIT ─── calls MVSWAIT (ASM delay)
```

### 5.5 Infrastructure Jobs

```
CLOSEFIL.jcl ── SDSF ─── Closes CICS-owned VSAM files for batch access
OPENFIL.jcl ─── SDSF ─── Reopens CICS-owned VSAM files after batch
DEFGDGB.jcl ── IDCAMS ─── Defines GDG base entries for backups
DEFGDGD.jcl ── IEBGENER ─ Creates GDG generation datasets + copies
TRANIDX.jcl ── IDCAMS ─── Defines alternate index on Transaction VSAM
DEFCUST.jcl ── IDCAMS ─── Defines Customer VSAM cluster
ESDSRRDS.jcl ─ IDCAMS ─── Defines ESDS/RRDS demonstration datasets
```

---

## 6. Batch Processing Pipeline

### 6.1 Nightly Batch Cycle (Execution Order)

```
Step  Job          Program       Input Files              Output Files             Duration
────  ───────────  ────────────  ──────────────────────   ──────────────────────   ────────
 1    CLOSEFIL     SDSF          (CICS commands)          Close CICS VSAM files    Fast
 2    ACCTFILE     IDCAMS        Account flat file        Account VSAM             Fast
 3    CARDFILE     IDCAMS        Card flat file           Card VSAM                Fast
 4    CUSTFILE     IDCAMS        Customer flat file       Customer VSAM            Fast
 5    XREFFILE     IDCAMS        XREF flat file           Card XREF VSAM           Fast
 6    TRANFILE     IDCAMS        Transaction flat file    Transaction VSAM         Fast
 7    POSTTRAN     CBTRN02C      Daily Trans, Acct,       Transaction, Acct,       Long
                                 XREF, Cat Balance        Cat Balance
 8    INTCALC      CBACT04C      Account, Cat Bal,        Account (updated)        Medium
                                 Disclosure Group
 9    TRANBKP      IDCAMS        Transaction VSAM         Backup GDG(+1)           Medium
10    COMBTRAN     SORT/MERGE    Daily + Master Trans     Merged Transaction       Medium
11    CREASTMT     CBSTM03A/B    Trans, Acct, XREF        Statement output         Long
12    TRANIDX      IDCAMS        Transaction VSAM         Alternate index          Fast
13    OPENFIL      SDSF          (CICS commands)          Reopen CICS VSAM files   Fast
```

### 6.2 Data Flow Diagram

```
                  ┌──────────────────────────────────────────────────────┐
                  │              ONLINE (CICS) SUBSYSTEM                  │
                  │                                                      │
  Users ──────▶  │  COSGN00C ──▶ COMEN01C ──▶ [COTRN02C] ──▶ writes    │
  (3270)         │                               transaction             │
                  │                               to VSAM                 │
                  └──────────────────────┬───────────────────────────────┘
                                         │
                                         ▼ (end of day)
                  ┌──────────────────────────────────────────────────────┐
                  │              BATCH SUBSYSTEM                          │
                  │                                                      │
                  │  Step 1: CLOSEFIL ─── close CICS files               │
                  │           │                                          │
                  │  Step 2-6: Data refresh (IDCAMS REPRO)               │
                  │           │                                          │
                  │  Step 7:  POSTTRAN ─── CBTRN02C                      │
                  │           │     ├── read Daily Transactions           │
                  │           │     ├── validate via XREF                 │
                  │           │     ├── update Account balance            │
                  │           │     ├── update Category Balance           │
                  │           │     └── write to Transaction Master       │
                  │           │                                          │
                  │  Step 8:  INTCALC ─── CBACT04C                       │
                  │           │     ├── read Account + Cat Balance        │
                  │           │     ├── lookup Disclosure Group rates     │
                  │           │     └── apply interest to Account         │
                  │           │                                          │
                  │  Step 9:  TRANBKP ─── backup Transactions            │
                  │           │                                          │
                  │  Step 10: COMBTRAN ─── merge daily + master           │
                  │           │                                          │
                  │  Step 11: CREASTMT ─── CBSTM03A/B                    │
                  │           │     ├── sort transactions by card          │
                  │           │     ├── match to accounts                 │
                  │           │     └── generate statements               │
                  │           │                                          │
                  │  Step 12: TRANIDX ─── rebuild alternate index        │
                  │           │                                          │
                  │  Step 13: OPENFIL ─── reopen CICS files              │
                  └──────────────────────────────────────────────────────┘
```

---

## 7. BMS Map Dependencies

Each online program has a 1:1 relationship with a BMS map and its generated copybook.

| Program | BMS Map Source | BMS Copybook (generated) | Mapset | Map Name |
|---------|---------------|-------------------------|--------|----------|
| COSGN00C | COSGN00.bms | COSGN00.CPY | COSGN00 | COSGN0A |
| COMEN01C | COMEN01.bms | COMEN01.CPY | COMEN01 | COMEN1A |
| COADM01C | COADM01.bms | COADM01.CPY | COADM01 | COADM1A |
| COACTVWC | COACTVW.bms | COACTVW.CPY | COACTVW | CACTVWA |
| COACTUPC | COACTUP.bms | COACTUP.CPY | COACTUP | CACTUPA |
| COCRDLIC | COCRDLI.bms | COCRDLI.CPY | COCRDLI | CCRDLIA |
| COCRDSLC | COCRDSL.bms | COCRDSL.CPY | COCRDSL | CCRDSLA |
| COCRDUPC | COCRDUP.bms | COCRDUP.CPY | COCRDUP | CCRDUPA |
| COTRN00C | COTRN00.bms | COTRN00.CPY | COTRN00 | COTRN0A |
| COTRN01C | COTRN01.bms | COTRN01.CPY | COTRN01 | COTRN1A |
| COTRN02C | COTRN02.bms | COTRN02.CPY | COTRN02 | COTRN2A |
| CORPT00C | CORPT00.bms | CORPT00.CPY | CORPT00 | CORPT0A |
| COBIL00C | COBIL00.bms | COBIL00.CPY | COBIL00 | COBIL0A |
| COUSR00C | COUSR00.bms | COUSR00.CPY | COUSR00 | COUSR0A |
| COUSR01C | COUSR01.bms | COUSR01.CPY | COUSR01 | COUSR1A |
| COUSR02C | COUSR02.bms | COUSR02.CPY | COUSR02 | COUSR2A |
| COUSR03C | COUSR03.bms | COUSR03.CPY | COUSR03 | COUSR3A |

---

## 8. Optional Module Dependencies

### 8.1 Authorization Module (IMS/DB2/MQ)

```
MQ Request Queue ──▶ COPAUA0C (Trigger Monitor)
                         ├── reads: MQ Request (CCPAURQY)
                         ├── reads: VSAM XREF, Account, Customer
                         ├── performs: authorization logic
                         └── writes: MQ Reply (CCPAURLY) or Error (CCPAUERY)

COPAUS0C (Summary View) ──▶ COPAUS1C (Detail View, via LINK)
                                └── COPAUS2C (Mark Fraud, DB2 UPDATE)

IMS DB Utilities:
    PAUDBLOD ──▶ IMS DB (ISRT root + child segments)
    PAUDBUNL ──▶ IMS DB (GN/GNP) ──▶ sequential file
    DBUNLDGS ──▶ IMS DB (GN/GNP) ──▶ GSAM sequential file

Batch: CBPAUP0C ──▶ purges processed authorizations
```

### 8.2 Transaction Type DB2 Module

```
COTRTLIC (List/Delete) ──▶ DB2 SELECT/DELETE on TRAN_TYPE table
    └── XCTL to COTRTUPC for edit

COTRTUPC (Add/Edit) ──▶ DB2 INSERT/UPDATE on TRAN_TYPE table
    └── XCTL back to COTRTLIC

COBTUPDT (Batch) ──▶ DB2 embedded SQL for bulk updates
```

### 8.3 VSAM-MQ Module

```
MQ Request ──▶ CODATE01 ──▶ returns system date via MQ Reply
MQ Request ──▶ COACCT01 ──▶ reads Account VSAM ──▶ returns via MQ Reply

Both programs: MQOPEN ──▶ MQGET ──▶ process ──▶ MQPUT ──▶ MQCLOSE
```

---

## 9. Cross-Cutting Concerns

### 9.1 Shared Dependencies (used by nearly all programs)

| Dependency | Type | Programs Affected |
|-----------|------|-------------------|
| COCOM01Y | Copybook | All 17 online programs |
| CSDAT01Y | Copybook | All 17 online programs |
| COTTL01Y | Copybook | All 17 online programs |
| CSMSG01Y | Copybook | All 17 online programs |
| DFHAID | CICS System | All 17 online programs |
| CEE3ABD | LE Runtime | 10 batch programs |
| CSUTLDTC | Utility | COTRN02C, CORPT00C (+ via CSUTLDPY copybook) |

### 9.2 High Fan-In Entities (most-accessed data)

| Entity | Read By | Written By | Total Accessors |
|--------|---------|-----------|-----------------|
| Account (CVACT01Y) | 8 programs | 3 programs | 11 |
| Transaction (CVTRA05Y) | 6 programs | 3 programs | 9 |
| Card XREF (CVACT03Y) | 5 programs | 1 program | 6 |
| Customer (CVCUS01Y) | 4 programs | 1 program | 5 |
| User Security (CSUSR01Y) | 4 programs | 3 programs | 7 |

### 9.3 High Fan-Out Programs (most dependencies)

| Program | Files Accessed | Programs Called | Copybooks Used | Total Dependencies |
|---------|---------------|----------------|---------------|-------------------|
| COACTUPC | 3 VSAM files | 0 | 12+ copybooks | 15+ |
| CBTRN02C | 5 VSAM files | 1 (CEE3ABD) | 6 copybooks | 12+ |
| CBSTM03A | 3 VSAM files | 1 (CBSTM03B) | 4 copybooks | 8+ |
| CBACT04C | 3 VSAM files | 1 (CEE3ABD) | 4 copybooks | 8+ |
| CBEXPORT | 5 VSAM files | 1 (CEE3ABD) | 6 copybooks | 12+ |

---

## 10. Migration Dependency Order

### 10.1 Recommended Bottom-Up Migration Sequence

Based on dependency analysis, migrate in this order to minimize integration risk:

```
Wave 1: Foundation Layer (no upstream dependencies)
├── CSUSR01Y copybook → Java User entity
├── CSDAT01Y → Java DateTimeUtil
├── CSUTLDTC → Java DateValidationService
├── COCOM01Y → Java SessionContext / DTO
└── Lookup tables (CSLKPCDY) → Database seed data / enum classes

Wave 2: Core Data Entities
├── CVACT01Y → Account entity + repository
├── CVACT02Y → Card entity + repository
├── CVCUS01Y → Customer entity + repository
├── CVACT03Y → CardXref entity + repository
├── CVTRA05Y → Transaction entity + repository
└── Reference data (CVTRA01-04Y) → lookup tables

Wave 3: Batch Processing (file-oriented, no UI)
├── CBACT01C-03C → account/card/xref readers (simple)
├── CBCUS01C → customer reader
├── CBTRN01C → daily transaction reader
├── CBTRN02C → transaction posting service (critical)
├── CBACT04C → interest calculation service (critical)
├── CBTRN03C → transaction report service
├── CBSTM03A/B → statement generation service
└── CBEXPORT/CBIMPORT → data migration services

Wave 4: Online CICS Programs (UI + VSAM)
├── COSGN00C → Authentication controller
├── COMEN01C/COADM01C → Menu/routing controllers
├── COACTVWC/COACTUPC → Account view/update controllers
├── COCRDLIC/COCRDSLC/COCRDUPC → Card CRUD controllers
├── COTRN00C/COTRN01C/COTRN02C → Transaction controllers
├── CORPT00C → Report controller
├── COBIL00C → Bill payment controller
└── COUSR00C-03C → User admin controllers

Wave 5: Optional Modules
├── Authorization (IMS/DB2/MQ) → Spring + JMS services
├── Transaction Type DB2 → JPA-based CRUD
└── VSAM-MQ → REST/messaging services
```

### 10.2 Critical Path Items

These items are on the critical path and should be migrated/tested first:

1. **COCOM01Y** - Every online program depends on this COMMAREA structure
2. **CBTRN02C** - Core transaction posting, touches 5 files
3. **CBACT04C** - Interest calculation, complex business logic
4. **COACTUPC** - Largest program (4,236 lines), complex update logic
5. **Account entity (CVACT01Y)** - Most-accessed data structure
