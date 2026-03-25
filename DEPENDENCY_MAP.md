# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Scope:** Call graph, CICS transfer control, copybook inclusion, JCL job data lineage

---

## 1. Online Program Call Graph (CICS XCTL / Navigation)

The online system uses **EXEC CICS XCTL** (transfer control) for screen-to-screen navigation, with the common communication area (COCOM01Y / DFHCOMMAREA) carrying context between programs.

### 1.1 Navigation Flow Diagram

```
                              ┌──────────────┐
                              │   COSGN00C   │
                              │  (Sign-On)   │
                              │  Trans: CC00 │
                              └──────┬───────┘
                                     │ XCTL
                          ┌──────────┴──────────┐
                          │                     │
                   ┌──────┴──────┐       ┌──────┴──────┐
                   │  COMEN01C   │       │  COADM01C   │
                   │ (Main Menu) │       │(Admin Menu) │
                   │ Trans: CM00 │       │ Trans: CA00 │
                   └──────┬──────┘       └──────┬──────┘
                          │                     │
        ┌─────────┬───────┼───────┬─────┐      │
        │         │       │       │     │      ├──────────┐
        ▼         ▼       ▼       ▼     ▼      ▼          ▼
   ┌─────────┐┌────────┐┌──────┐┌─────┐┌────┐┌──────┐┌──────┐
   │COACTVWC ││COCRDLIC││COTRN ││CORPT││COBI││COUSR ││COUSR │
   │Acct View││Card Lst││00C   ││00C  ││L00C││00C   ││01C-  │
   │         ││        ││Trn   ││Rept ││Bill││User  ││03C   │
   └────┬────┘└───┬────┘│List  │└─────┘└────┘│List  │└──────┘
        │         │     └──┬───┘             └──┬───┘
        ▼         ▼        │                    │
   ┌─────────┐┌────────┐  ├────────┐     ┌─────┼─────┐
   │COACTUPC ││COCRDSLC│  ▼        ▼     ▼     ▼     ▼
   │Acct Upd ││Card Dtl│┌──────┐┌──────┐┌────┐┌────┐┌────┐
   └─────────┘└───┬────┘│COTRN ││COTRN ││USR ││USR ││USR │
                  │     │01C   ││02C   ││01C ││02C ││03C │
                  ▼     │View  ││Add   ││Add ││Upd ││Del │
             ┌────────┐ └──────┘└──────┘└────┘└────┘└────┘
             │COCRDUPC│
             │Card Upd│
             └────────┘
```

### 1.2 XCTL Transfer Control Table

Every CICS program uses `XCTL PROGRAM(CDEMO-TO-PROGRAM)` where `CDEMO-TO-PROGRAM` is set from the common area. The menu programs dispatch dynamically based on the selected option.

| Source Program | Target Program(s) | Transfer Mechanism |
|---------------|-------------------|-------------------|
| **COSGN00C** | COMEN01C, COADM01C | XCTL — based on user type (U→Menu, A→Admin) |
| **COMEN01C** | COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C | XCTL — dynamic via CDEMO-MENU-OPT-PGMNAME array |
| **COADM01C** | COUSR00C, COUSR01C, COUSR02C, COUSR03C | XCTL — dynamic via CDEMO-ADMIN-OPT-PGMNAME array |
| **COACTVWC** | COACTUPC | XCTL — drill to update |
| **COACTUPC** | COACTVWC | XCTL — return to view |
| **COCRDLIC** | COCRDSLC, COCRDUPC | XCTL — drill to detail or update |
| **COCRDSLC** | COCRDUPC | XCTL — drill to update |
| **COCRDUPC** | COCRDLIC | XCTL — return to list |
| **COTRN00C** | COTRN01C, COTRN02C | XCTL — drill to view or add |
| **COTRN01C** | COTRN00C | XCTL — return to list |
| **COTRN02C** | COTRN00C | XCTL — return to list |
| **CORPT00C** | COMEN01C | XCTL — return to menu |
| **COBIL00C** | COMEN01C | XCTL — return to menu |
| **COUSR00C** | COUSR01C, COUSR02C, COUSR03C | XCTL — drill to CRUD |
| **COUSR01C** | COUSR00C | XCTL — return to list |
| **COUSR02C** | COUSR00C | XCTL — return to list |
| **COUSR03C** | COUSR00C | XCTL — return to list |

### 1.3 Optional Module Navigation

| Source Program | Target Program(s) | Module |
|---------------|-------------------|--------|
| COADM01C (extended) | COPAUS0C | Auth module — summary view |
| COPAUS0C | COPAUS1C | Auth — drill to detail |
| COPAUS1C | COPAUS2C | Auth — mark fraud (DB2) |
| COADM01C (extended) | COTRTLIC | DB2 TT — tran type list |
| COTRTLIC | COTRTUPC | DB2 TT — tran type add/edit |

---

## 2. Batch Program Call Graph (COBOL CALL)

Batch programs use `CALL 'program-name'` for subroutine invocations.

```
┌─────────────┐         ┌─────────────┐
│  CBSTM03A   │────────>│  CBSTM03B   │
│  Statement  │  CALL   │  File I/O   │
│  Main       │ (x14)   │  Subroutine │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  CSUTLDTC   │────────>│  CEEDAYS    │
│  Date Util  │  CALL   │  LE Service │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│  CBSTM03A   │────────>│  CEE3ABD    │
│  Statement  │  CALL   │  LE Abend   │
└─────────────┘         └─────────────┘
```

| Caller | Callee | Call Count | Purpose |
|--------|--------|------------|---------|
| CBSTM03A | CBSTM03B | 14 calls | File open/close/read/write operations |
| CBSTM03A | CEE3ABD | 1 call | Abnormal termination (error path) |
| CSUTLDTC | CEEDAYS | 1 call | Date validation via LE callable service |
| COPAUA0C | MQOPEN, MQGET, MQPUT, MQCLOSE | Multiple | MQ messaging operations |
| COACCT01 | MQOPEN, MQGET, MQPUT, MQCLOSE | 9 calls | MQ account inquiry |
| CODATE01 | MQOPEN, MQGET, MQPUT, MQCLOSE | 9 calls | MQ date service |
| DBUNLDGS | CBLTDLI | 3 calls | IMS DL/I database operations |
| PAUDBLOD | CBLTDLI | 4 calls | IMS DL/I database load |
| PAUDBUNL | CBLTDLI | 2 calls | IMS DL/I database unload |

---

## 3. Copybook Inclusion Map

### 3.1 Core Copybook Usage Matrix

Shows which copybooks are included (COPY statement) by which programs.

| Copybook | Programs Using It | Usage Count |
|----------|-------------------|-------------|
| **COCOM01Y** (common area) | All 17 online programs + optional CICS programs | ~22 |
| **COTTL01Y** (title/header) | All 17 online programs | 17 |
| **CSDAT01Y** (date working storage) | All 17 online programs | 17 |
| **CSMSG01Y** (message area) | All 17 online programs | 17 |
| **DFHAID** (CICS AID keys) | All 17 online programs | 17 |
| **DFHBMSCA** (BMS attributes) | All 17 online programs | 17 |
| **CSUSR01Y** (user security) | COSGN00C, COUSR00C-03C, COTRTLIC, COTRTUPC | 8 |
| **CVACT01Y** (account record) | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBSTM03A, COPAUA0C, COPAUS0C, COACCT01 | 8 |
| **CVACT02Y** (card record) | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, COPAUS0C | 5 |
| **CVACT03Y** (card xref) | COCRDLIC, CBACT03C, CBACT04C, CBSTM03A, COPAUA0C, COPAUS0C | 6 |
| **CVCUS01Y** (customer) | COACTVWC, COACTUPC, CBCUS01C, COPAUA0C, COPAUS0C | 5 |
| **CVTRA05Y** (transaction) | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C | 5 |
| **CVTRA06Y** (daily trans) | CBTRN02C, CBTRN03C | 2 |
| **CVTRA01Y** (cat balance) | CBACT04C, CBTRN02C | 2 |
| **CVTRA02Y** (disclosure grp) | CBACT04C | 1 |
| **CVTRA03Y** (tran type) | CBTRN02C, CBTRN03C | 2 |
| **CVTRA04Y** (tran category) | CBTRN02C, CBTRN03C | 2 |
| **CVTRA07Y** (report layout) | CBTRN03C | 1 |
| **COSTM01** (stmt tran layout) | CBSTM03A | 1 |
| **CUSTREC** (alt customer) | CBSTM03A | 1 |
| **CVEXPORT** (export FDs) | CBEXPORT, CBIMPORT | 2 |
| **COMEN02Y** (menu options) | COMEN01C | 1 |
| **COADM02Y** (admin options) | COADM01C | 1 |
| **CSLKPCDY** (lookup codes) | COACTUPC, COCRDUPC | 2 |
| **CSSTRPFY** (string procs) | COTRTLIC, COTRTUPC | 2 |
| **CSUTLDPY** (date util params) | Various date-using programs | ~5 |
| **CSUTLDWY** (date util WS) | COTRTUPC | 1 |
| **CSMSG02Y** (long messages) | COPAUS0C, COPAUS1C, COTRTUPC | 3 |

### 3.2 BMS Map Copybook Pairings

Each online program includes its corresponding BMS-generated copybook for screen I/O.

| Program | BMS Map Copybook (app/cpy-bms/) | Screen |
|---------|-------------------------------|--------|
| COSGN00C | COSGN00.CPY | Sign-on |
| COMEN01C | COMEN01.CPY | Main Menu |
| COADM01C | COADM01.CPY | Admin Menu |
| COACTVWC | COACTVW.CPY | Account View |
| COACTUPC | COACTUP.CPY | Account Update |
| COCRDLIC | COCRDLI.CPY | Card List |
| COCRDSLC | COCRDSL.CPY | Card Detail |
| COCRDUPC | COCRDUP.CPY | Card Update |
| COTRN00C | COTRN00.CPY | Transaction List |
| COTRN01C | COTRN01.CPY | Transaction View |
| COTRN02C | COTRN02.CPY | Transaction Add |
| CORPT00C | CORPT00.CPY | Report Request |
| COBIL00C | COBIL00.CPY | Bill Payment |
| COUSR00C | COUSR00.CPY | User List |
| COUSR01C | COUSR01.CPY | User Add |
| COUSR02C | COUSR02.CPY | User Update |
| COUSR03C | COUSR03.CPY | User Delete |

---

## 4. VSAM File Access Map (Online Programs)

Shows which CICS programs perform which operations on which VSAM files.

### 4.1 File Access Matrix

| Program | ACCTFILE | CARDFILE | CUSTFILE | CARDXREF | TRANSACT | DALYTRAN | USRSEC | TCATBALF |
|---------|----------|----------|----------|----------|----------|----------|--------|----------|
| COSGN00C | | | | | | | **R** | |
| COACTVWC | **R** | **R** | **R** | | | | | |
| COACTUPC | **R** | **R** | **R** | **R** | | | | |
| COCRDLIC | | **R/B** | | | | | | |
| COCRDSLC | | **R** | **R** | | | | | |
| COCRDUPC | | **R** | | **R** | | | | |
| COTRN00C | | | | | **R/B** | | | |
| COTRN01C | | | | | **R** | | | |
| COTRN02C | | **R** | | **R** | **R/W** | | | |
| COBIL00C | **R/RW** | | | | **R/W** | | | |
| CORPT00C | | | | | | | | |
| COUSR00C | | | | | | | **R/B** | |
| COUSR01C | | | | | | | **W** | |
| COUSR02C | | | | | | | **R/RW** | |
| COUSR03C | | | | | | | **R/D** | |

**Legend:** R=Read, W=Write, RW=Rewrite, D=Delete, B=Browse (STARTBR/READNEXT/READPREV)

### 4.2 Batch File Access

| Program | Files Read | Files Written | Purpose |
|---------|-----------|---------------|---------|
| CBACT01C | ACCTFILE | SYSOUT (print) | Dump account records |
| CBACT02C | CARDFILE | SYSOUT (print) | Dump card records |
| CBACT03C | CARDXREF | SYSOUT (print) | Dump cross-reference records |
| CBCUS01C | CUSTFILE | SYSOUT (print) | Dump customer records |
| CBTRN01C | TRANSACT | SYSOUT (print) | Dump transaction records |
| CBTRN02C | DALYTRAN, TRANSACT, TRANTYPE, TRANCATG, CARDXREF, ACCTFILE, TCATBALF | TRANSACT (update), TCATBALF (update), DALYREJS | Post daily transactions |
| CBTRN03C | TRANSACT, TRANTYPE, TRANCATG, CARDXREF, ACCTFILE | RPTFILE (report) | Generate daily report |
| CBACT04C | CARDXREF, ACCTFILE, TCATBALF, DISCGRP | TCATBALF (update), ACCTFILE (update) | Calculate interest |
| CBSTM03A | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STMTFILE, HTMLFILE | Generate statements |
| CBSTM03B | (subroutine — uses CBSTM03A's files) | (subroutine) | File I/O for statements |
| CBEXPORT | All VSAM files | Flat export files | Export data |
| CBIMPORT | Flat import files | VSAM files | Import data |

---

## 5. JCL Job Data Lineage

### 5.1 Batch Processing Cycle (End-of-Day)

```
     ┌──────────┐
     │ CLOSEFIL │  Close CICS files for exclusive batch access
     └────┬─────┘
          │
     ┌────┴─────┐     ┌──────────┐     ┌──────────┐
     │ ACCTFILE │     │ CARDFILE │     │ CUSTFILE │
     │ Refresh  │     │ Refresh  │     │ Refresh  │
     └────┬─────┘     └────┬─────┘     └────┬─────┘
          │                │                │
     ┌────┴─────┐     ┌───┴──────┐         │
     │ XREFFILE │     │ TRANFILE │         │
     │ Refresh  │     │ Refresh  │         │
     └────┬─────┘     └────┬─────┘         │
          │                │                │
          └────────┬───────┘────────────────┘
                   │
          ┌────────┴────────┐
          │    POSTTRAN     │  CBTRN02C: Post daily transactions
          │ Reads: DALYTRAN │  to master TRANSACT file
          │ Updates: TRANSACT│
          │         TCATBALF│
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │    INTCALC      │  CBACT04C: Calculate interest charges
          │ Reads: CARDXREF │  per disclosure group rates
          │        DISCGRP  │
          │ Updates: ACCTFILE│
          │         TCATBALF│
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │    TRANBKP      │  Backup transaction file
          │  IDCAMS REPRO   │  (TRANSACT → backup copy)
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │    COMBTRAN     │  SORT + IDCAMS: Merge daily
          │ Combine daily + │  transactions into master
          │ master trans    │
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │   CREASTMT      │  CBSTM03A: Generate account
          │ Reads: TRANSACT │  statements (text + HTML)
          │        XREFFILE │
          │        ACCTFILE │
          │        CUSTFILE │
          │ Writes: STMTFILE│
          │         HTMLFILE│
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │    TRANIDX      │  Rebuild alternate indexes
          │  IDCAMS BLDINDEX│  on transaction file
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │    OPENFIL      │  Reopen CICS files
          └─────────────────┘
```

### 5.2 JCL Job — VSAM Dataset I/O Map

| Job | Input Datasets | Output Datasets | Program(s) |
|-----|---------------|-----------------|------------|
| **ACCTFILE** | AWS.M2.CARDDEMO.ACCTDATA.PS | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | IDCAMS |
| **CARDFILE** | AWS.M2.CARDDEMO.CARDDATA.PS | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS (+AIX) | IDCAMS, SDSF |
| **CUSTFILE** | AWS.M2.CARDDEMO.CUSTDATA.PS | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | IDCAMS, SDSF |
| **XREFFILE** | AWS.M2.CARDDEMO.CARDXREF.PS | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (+AIX) | IDCAMS |
| **TRANFILE** | AWS.M2.CARDDEMO.TRANSACT.PS | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (+AIX) | IDCAMS, SDSF |
| **DUSRSECJ** | AWS.M2.CARDDEMO.USRSEC.PS | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | IEBGENER, IDCAMS |
| **TRANTYPE** | AWS.M2.CARDDEMO.TRANTYPE.PS | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | IDCAMS |
| **TRANCATG** | AWS.M2.CARDDEMO.TRANCATG.PS | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | IDCAMS |
| **TCATBALF** | AWS.M2.CARDDEMO.TCATBALF.PS | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | IDCAMS |
| **DISCGRP** | AWS.M2.CARDDEMO.DISCGRP.PS | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | IDCAMS |
| **POSTTRAN** | DAILYTRAN, TRANSACT, TRANTYPE, TRANCATG, CARDXREF, ACCTDATA, TCATBALF | TRANSACT (update), TCATBALF (update), DALYREJS | CBTRN02C |
| **INTCALC** | TCATBALF, CARDXREF (+AIX), ACCTDATA, DISCGRP | TCATBALF (update), ACCTDATA (update) | CBACT04C |
| **COMBTRAN** | DAILYTRAN, TRANSACT | TRANSACT (merged) | SORT, IDCAMS |
| **CREASTMT** | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA | STATEMNT.PS, STATEMNT.HTML, TRXFL | SORT, IDCAMS, CBSTM03A |
| **TRANREPT** | TRANSACT, TRANTYPE, TRANCATG, CARDXREF, ACCTDATA | DALYREPT (report) | SORT, CBTRN03C |
| **TRANBKP** | TRANSACT | TRANSACT.BACKUP | IDCAMS |
| **CBEXPORT** | All VSAM KSDS files | AWS.M2.CARDDEMO.*.EXPORT | IDCAMS, CBEXPORT |
| **CBIMPORT** | AWS.M2.CARDDEMO.*.IMPORT | All VSAM KSDS files | CBIMPORT |
| **READACCT** | ACCTDATA.VSAM.KSDS | SYSOUT | CBACT01C |
| **READCARD** | CARDDATA.VSAM.KSDS | SYSOUT | CBACT02C |
| **READXREF** | CARDXREF.VSAM.KSDS | SYSOUT | CBACT03C |
| **READCUST** | CUSTDATA.VSAM.KSDS | SYSOUT | CBCUS01C |

### 5.3 GDG (Generation Data Group) Lineage

| Job | GDG Base | Purpose |
|-----|----------|---------|
| DEFGDGB | AWS.M2.CARDDEMO.TRANSACT.BACKUP | Transaction backup generations |
| DEFGDGB | AWS.M2.CARDDEMO.DALYTRAN.BACKUP | Daily transaction backup generations |
| DEFGDGB | AWS.M2.CARDDEMO.STATEMNT.BACKUP | Statement backup generations |
| DEFGDGD | (defines GDG data entries) | Initializes GDG generation datasets |

---

## 6. Data Flow Summary — Business Entity Lineage

### Account Data
```
Flat File (ACCTDATA.PS) ──[ACCTFILE.jcl]──> VSAM (ACCTDATA.KSDS)
                                                    │
    ┌───────────────────────────────────────────────┤
    │                    │                          │
COACTVWC (view)   COACTUPC (update)          CBACT04C (interest calc)
                                                    │
                                              Updates ACCTFILE
                                              (new balance w/ interest)
```

### Transaction Data
```
Online Input                     Batch Input
    │                                │
COTRN02C ──write──> TRANSACT    CBIMPORT ──load──> DAILYTRAN
                                                        │
                                                  POSTTRAN (CBTRN02C)
                                                        │
                                                  TRANSACT (posted)
                                                        │
                                        ┌───────────────┤───────────────┐
                                        │               │               │
                                   CREASTMT        TRANREPT         TRANBKP
                                   (statements)    (daily report)   (backup)
```

### User Security Data
```
Flat File (USRSEC.PS) ──[DUSRSECJ.jcl]──> VSAM (USRSEC.KSDS)
                                                   │
                           ┌──────────┬────────────┼────────────┐
                           │          │            │            │
                      COSGN00C   COUSR00C     COUSR01C    COUSR02C/03C
                      (login)    (list)       (add)       (update/delete)
```

---

## 7. Technology Integration Points

### MQ Message Queues (Optional Modules)

| Queue | Direction | Program | Message Type |
|-------|-----------|---------|-------------|
| CDRD (Date Request) | IN | CODATE01 | System date request |
| CDRD (Date Reply) | OUT | CODATE01 | System date response |
| CDRA (Acct Request) | IN | COACCT01 | Account inquiry request |
| CDRA (Acct Reply) | OUT | COACCT01 | Account data response |
| Auth Request Queue | IN | COPAUA0C | Authorization request |
| Auth Reply Queue | OUT | COPAUA0C | Authorization response |
| Auth Error Queue | OUT | COPAUA0C, COACCT01, CODATE01 | Error messages |

### IMS Database (Optional — Auth Module)

| Database | Segment | Access Programs | Operations |
|----------|---------|----------------|------------|
| PAUTDB | Summary (CIPAUSMY) | COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD, PAUDBUNL | GN, GNP, GU, ISRT |
| PAUTDB | Detail (CIPAUDTY) | COPAUS1C, COPAUS2C, DBUNLDGS, PAUDBLOD | GNP, ISRT |

### DB2 Tables (Optional — Tran Type Module)

| Table | Access Programs | Operations |
|-------|----------------|------------|
| Transaction Type table | COTRTLIC, COTRTUPC, COBTUPDT | SELECT (cursor), INSERT, UPDATE, DELETE |

---

## 8. Shared Utility Dependencies

| Utility | Used By | Purpose |
|---------|---------|---------|
| CSUTLDTC (Date Util) | Programs needing date validation | Calls CEEDAYS for date conversion |
| COBSWAIT (Wait) | WAITSTEP.jcl | Introduces delay in job scheduling |
| CSSETATY (Set Attributes) | COTRTUPC, COCRDUPC | BMS field attribute management |
| CSSTRPFY (String Processing) | COTRTLIC, COTRTUPC | String manipulation functions |
| CSLKPCDY (Lookup Codes) | COACTUPC, COCRDUPC | Country/state code validation |
| CODATECN (Date Conversion) | Various | Date format conversion routines |
