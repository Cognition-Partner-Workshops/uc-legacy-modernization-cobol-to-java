# Dependency Map — CardDemo COBOL Estate

> Call graphs, dataset lineage, and end-to-end batch pipeline flow.

---

## Table of Contents

1. [Inter-Program Call Graph](#1-inter-program-call-graph)
2. [CICS Transfer (XCTL / LINK) Graph](#2-cics-transfer-xctl--link-graph)
3. [Copybook Dependency Matrix](#3-copybook-dependency-matrix)
4. [Dataset Lineage — JCL-to-Program Mapping](#4-dataset-lineage--jcl-to-program-mapping)
5. [End-to-End Batch Pipeline Flow](#5-end-to-end-batch-pipeline-flow)

---

## 1. Inter-Program Call Graph

Programs that CALL other programs at runtime:

```
CBSTM03A ──CALL──► CBSTM03B        (Statement file I/O subroutine)

CBACT01C ──CALL──► COBDATFT        (Assembler date formatting)
CBACT01C ──CALL──► CEE3ABD         (LE abend handler)

CBACT02C ──CALL──► CEE3ABD
CBACT03C ──CALL──► CEE3ABD
CBACT04C ──CALL──► CEE3ABD
CBCUS01C ──CALL──► CEE3ABD
CBTRN01C ──CALL──► CEE3ABD
CBTRN02C ──CALL──► CEE3ABD
CBTRN03C ──CALL──► CEE3ABD
CBEXPORT ──CALL──► CEE3ABD
CBIMPORT ──CALL──► CEE3ABD

COBSWAIT ──CALL──► MVSWAIT         (Assembler wait routine)

COTRN02C ──CALL──► CSUTLDTC        (Date conversion utility)
CORPT00C ──CALL──► CSUTLDTC        (Date conversion utility)

CSUTLDTC ──CALL──► CEEDAYS         (LE date callable service)

COPAUA0C ──CALL──► MQOPEN, MQGET, MQPUT1, MQCLOSE  (MQ API)
COACCT01 ──CALL──► MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ API)
CODATE01 ──CALL──► MQOPEN, MQGET, MQPUT, MQCLOSE   (MQ API)

DBUNLDGS ──CALL──► CBLTDLI         (IMS DL/I: GN, GNP, ISRT)
PAUDBLOD ──CALL──► CBLTDLI         (IMS DL/I: ISRT, GU)
PAUDBUNL ──CALL──► CBLTDLI         (IMS DL/I: GN, GNP)
```

### Call Graph Summary (ASCII Tree)

```
                         ┌── COBDATFT (asm)
              CBACT01C ──┤
                         └── CEE3ABD (LE)

              CBSTM03A ──── CBSTM03B (file I/O sub)
                                │
                                ├── TRNXFILE (VSAM)
                                ├── XREFFILE (VSAM)
                                ├── CUSTFILE (VSAM)
                                └── ACCTFILE (VSAM)

              COTRN02C ──── CSUTLDTC ──── CEEDAYS (LE)
              CORPT00C ──── CSUTLDTC ──── CEEDAYS (LE)

              COBSWAIT ──── MVSWAIT (asm)

                         ┌── MQOPEN
              COPAUA0C ──┼── MQGET
              (MQ auth)  ├── MQPUT1
                         └── MQCLOSE

                         ┌── MQOPEN
              COACCT01 ──┼── MQGET
              (MQ acct)  ├── MQPUT
                         └── MQCLOSE

                         ┌── CBLTDLI (GN, GNP, ISRT)
              DBUNLDGS ──┤
              (IMS)      └── Flat file WRITE

                         ┌── Flat file READ
              PAUDBLOD ──┤
              (IMS)      └── CBLTDLI (ISRT, GU)

                         ┌── CBLTDLI (GN, GNP)
              PAUDBUNL ──┤
              (IMS)      └── Flat file WRITE
```

---

## 2. CICS Transfer (XCTL / LINK) Graph

Online screen navigation via EXEC CICS XCTL and EXEC CICS RETURN TRANSID:

```
  COSGN00C (Sign-On)
      │
      ▼
  COMEN01C (Main Menu) ◄──────────────────────────────────────┐
      │                                                        │
      ├──► COADM01C (Admin Menu) ──┬──► COUSR00C (User List)  │
      │                            │       ├──► COUSR01C (Add) │
      │                            │       ├──► COUSR02C (Upd) │
      │                            │       └──► COUSR03C (Del) │
      │                            │                           │
      │                            └──────────────────────────►┘
      │
      ├──► COACTVWC (Account View)
      │
      ├──► COACTUPC (Account Update)
      │
      ├──► COCRDSLC (Card Search) ──► COCRDLIC (Card List)
      │                                    │
      │                                    ├──► COCRDUPC (Card Update)
      │                                    └──► (Card Detail — via COCRDLIC)
      │
      ├──► COTRN00C (Transaction List)
      │       ├──► COTRN01C (Transaction Add)
      │       └──► COTRN02C (Transaction Update)
      │
      ├──► COBIL00C (Bill Payment)
      │
      └──► CORPT00C (Report Request)

  [DB2 Sub-App — separate CICS transaction]
  COTRTLIC (Tran Type List) ◄──► COTRTUPC (Tran Type Update)

  [Auth Sub-App — separate CICS transaction]
  COPAUS0C (Auth Summary List) ──► COPAUS1C (Auth Detail)
                                        │
                                        └──► COPAUS2C (Auth Decision)
```

---

## 3. Copybook Dependency Matrix

Which programs include which copybooks (✓ = referenced via COPY):

### Core Copybooks (`app/cpy/`)

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTUPC | COACTVWC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | CORPT00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| COCOM01Y | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| COTTL01Y | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| CSDAT01Y | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| CSMSG01Y | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| CSUSR01Y | ✓ | ✓ | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | ✓ | ✓ | ✓ | ✓ |
| CSMSG02Y | — | — | — | ✓ | ✓ | — | ✓ | ✓ | — | — | — | — | — | — | — | — | — |
| CVACT01Y | — | — | — | ✓ | ✓ | — | — | — | — | — | ✓ | — | — | — | — | — | — |
| CVACT02Y | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | — | — | — | — |
| CVACT03Y | — | — | — | ✓ | ✓ | — | — | — | — | — | ✓ | — | — | — | — | — | — |
| CVCRD01Y | — | — | — | ✓ | — | ✓ | ✓ | ✓ | — | — | — | — | — | — | — | — | — |
| CVCUS01Y | — | — | — | ✓ | ✓ | — | ✓ | ✓ | — | — | — | — | — | — | — | — | — |
| CVTRA05Y | — | — | — | — | — | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — |
| COMEN02Y | — | ✓ | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| COADM02Y | — | — | ✓ | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| CSSTRPFY | — | — | — | ✓ | ✓ | ✓ | ✓ | ✓ | — | — | — | — | — | — | — | — | — |

### Batch Program Copybook Usage

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBEXPORT | CBIMPORT | CBSTM03A |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| CVACT01Y | ✓ | — | — | ✓ | — | — | — | — | ✓ | ✓ | ✓ |
| CVACT02Y | — | ✓ | — | — | — | — | — | — | ✓ | ✓ | — |
| CVACT03Y | ✓ | — | — | ✓ | — | — | — | — | — | — | ✓ |
| CVCRD01Y | — | — | ✓ | — | — | — | — | — | ✓ | ✓ | — |
| CVCUS01Y | ✓ | — | — | — | ✓ | — | — | — | ✓ | ✓ | — |
| CVTRA05Y | — | — | — | — | — | ✓ | — | — | ✓ | ✓ | — |
| CVTRA01Y | — | — | — | — | — | — | ✓ | — | — | — | — |
| CVTRA02Y | — | — | — | — | — | — | ✓ | — | — | — | — |
| CVTRA03Y | — | — | — | — | — | — | — | ✓ | — | — | — |
| CVTRA04Y | — | — | — | — | — | — | — | ✓ | — | — | — |
| CVTRA06Y | — | — | — | — | — | — | — | ✓ | — | — | — |
| CSDAT01Y | ✓ | — | — | — | — | — | — | — | — | — | — |
| CODATECN | ✓ | — | — | — | — | — | — | — | — | — | — |
| CUSTREC  | — | — | — | — | — | — | — | — | — | — | ✓ |
| COSTM01  | — | — | — | — | — | — | — | — | — | — | ✓ |
| CVEXPORT | — | — | — | — | — | — | — | — | ✓ | ✓ | — |
| CSUSR01Y | — | — | — | — | — | — | — | — | ✓ | ✓ | — |

---

## 4. Dataset Lineage — JCL-to-Program Mapping

### VSAM Dataset Creation and Loading (JCL → Flat File → VSAM)

```
Flat File (PS)                    JCL Job        VSAM KSDS Dataset
─────────────────────────────────────────────────────────────────────
ACCTDATA.PS          ──ACCTFILE──►  ACCTDATA.VSAM.KSDS
                                    + AIX on CUST-ID

CUSTDATA.PS          ──CUSTFILE──►  CUSTDATA.VSAM.KSDS

CARDDATA.PS          ──CARDFILE──►  CARDDATA.VSAM.KSDS
                                    + AIX on ACCT-ID

CARDXREF.PS          ──XREFFILE──►  CARDXREF.VSAM.KSDS
                                    + AIX on ACCT-ID

TRANSACT.PS          ──TRANFILE──►  TRANSACT.VSAM.KSDS

DALYTRAN.PS          ──(loaded directly or via POSTTRAN)──►  DALYTRAN dataset

TCATBALF.PS          ──TCATBALF──►  TCATBALF.VSAM.KSDS

DISCGRP.PS           ──DISCGRP───►  DISCGRP.VSAM.KSDS

TRANTYPE.PS          ──TRANTYPE──►  TRANTYPE.VSAM.KSDS

TRANCATG.PS          ──TRANCATG──►  TRANCATG.VSAM.KSDS

USRSEC.PS            ──DUSRSECJ──►  USRSEC.VSAM.KSDS
```

### Dataset Access by Programs

| Dataset (VSAM KSDS) | Programs That READ | Programs That WRITE/REWRITE | Programs That DELETE |
|---------------------|-------------------|---------------------------|-------------------|
| ACCTDATA | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBACT01C, CBACT04C, CBEXPORT, CBSTM03B, COACCT01 | COACTUPC, CBIMPORT | — |
| CUSTDATA | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBEXPORT, CBSTM03B | CBIMPORT | — |
| CARDDATA | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT03C, CBEXPORT | COCRDUPC, CBIMPORT | — |
| CARDXREF | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COBIL00C, CBACT02C, CBEXPORT, CBSTM03B | CBIMPORT | — |
| TRANSACT | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CORPT00C, CBTRN01C, CBEXPORT | COTRN01C, COTRN02C, CBIMPORT | — |
| DALYTRAN | CBTRN03C | COTRN01C | — |
| TCATBALF | CBTRN02C | — | — |
| DISCGRP | CBTRN02C | — | — |
| TRANTYPE | CBTRN03C | — | — |
| TRANCATG | CBTRN03C | — | — |
| USRSEC | COSGN00C, COUSR00C, COUSR02C, CBEXPORT | COUSR01C, COUSR02C, CBIMPORT | COUSR03C |

### Dataset Access by JCL Jobs

| JCL Job | Datasets READ | Datasets WRITTEN | Programs Executed |
|---------|--------------|-----------------|-------------------|
| READACCT | ACCTDATA.VSAM.KSDS | SYSOUT (print) | CBACT01C |
| READCARD | CARDDATA.VSAM.KSDS | SYSOUT (print) | CBACT03C |
| READCUST | CUSTDATA.VSAM.KSDS | SYSOUT (print) | CBCUS01C |
| READXREF | CARDXREF.VSAM.KSDS | SYSOUT (print) | CBACT02C |
| CBEXPORT | ACCTDATA, CUSTDATA, CARDDATA, CARDXREF, USRSEC, TRANSACT | EXPORT.DATA.PS | CBEXPORT |
| CBIMPORT | EXPORT.DATA.PS (or import file) | ACCTDATA, CUSTDATA, CARDDATA, CARDXREF, USRSEC, TRANSACT | CBIMPORT |
| CREASTMT | TRANSACT.VSAM.KSDS, CARDXREF, ACCTDATA, CUSTDATA | TRXFL.VSAM.KSDS, STATEMNT.PS, STATEMNT.HTML | SORT, IDCAMS, CBSTM03A→CBSTM03B |
| POSTTRAN | DALYTRAN.PS, TRANSACT.VSAM.KSDS | TRANSACT.VSAM.KSDS (updated) | SORT, batch program |
| INTCALC | TCATBALF, DISCGRP, TRANSACT | TCATBALF (updated balances) | Interest calc program |
| COMBTRAN | DALYTRAN.PS, TRANSACT.VSAM.KSDS | TRANSACT.VSAM.KSDS (merged) | SORT, IDCAMS |
| TRANREPT | TRANSACT.VSAM.KSDS, TRANTYPE, TRANCATG | Report output file | SORT, report program |
| TRANBKP | TRANSACT.VSAM.KSDS | TRANSACT.BKUP GDG(+1) | IEBGENER |
| TRANIDX | TRANSACT.VSAM.KSDS | TRANSACT AIX rebuilt | IDCAMS BLDINDEX |
| TXT2PDF1 | STATEMNT.PS | STATEMNT.PS.PDF | TXT2PDF REXX |
| TRANEXTR | DB2 TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY | TRANTYPE.PS, TRANCATG.PS, BKUP GDGs | DSNTIAUL, IEBGENER |
| MNTTRDB2 | Input file (INPFILE) | DB2 TRANSACTION_TYPE | COBTUPDT |
| LOADPADB | Flat files (auth data) | IMS PAUT DB | PAUDBLOD |
| UNLDPADB | IMS PAUT DB | Flat files (auth data) | PAUDBUNL |
| UNLDGSAM | IMS GSAM DB | Flat files | DBUNLDGS |

---

## 5. End-to-End Batch Pipeline Flow

The daily/periodic batch processing pipeline runs in this sequence (derived from JCL dependencies and scheduler definitions in `app/scheduler/`):

```
┌─────────────────────────────────────────────────────────────────┐
│                    DAILY BATCH PIPELINE                         │
└─────────────────────────────────────────────────────────────────┘

Phase 0: PRE-BATCH — Close Online Files
─────────────────────────────────────────
  CLOSEFIL.jcl
    └── IDCAMS: Close/disable VSAM files for exclusive batch access

Phase 1: REFERENCE DATA EXTRACTION (if DB2 sub-app active)
──────────────────────────────────────────────────────────────
  TRANEXTR.jcl
    ├── STEP10: IEBGENER — Backup TRANTYPE.PS → GDG
    ├── STEP20: IEBGENER — Backup TRANCATG.PS → GDG
    ├── STEP30: IEFBR14  — Delete previous extracts
    ├── STEP40: DSNTIAUL — Extract DB2 TRANSACTION_TYPE → TRANTYPE.PS
    └── STEP50: DSNTIAUL — Extract DB2 TRANSACTION_TYPE_CATEGORY → TRANCATG.PS

Phase 2: DAILY TRANSACTION PROCESSING
──────────────────────────────────────
  POSTTRAN.jcl
    ├── SORT daily transactions
    └── Post to TRANSACT.VSAM.KSDS
                │
                ▼
  COMBTRAN.jcl
    ├── SORT — Merge DALYTRAN into TRANSACT
    └── IDCAMS REPRO — Update master transaction file
                │
                ▼
  TRANIDX.jcl
    └── IDCAMS BLDINDEX — Rebuild transaction file alternate indexes

Phase 3: INTEREST CALCULATION
─────────────────────────────
  INTCALC.jcl
    └── Calculate interest using DISCGRP rates against TCATBALF balances

Phase 4: REPORTING
──────────────────
  TRANREPT.jcl
    ├── SORT transactions for report ordering
    └── Generate Daily Transaction Report
                │
                ▼
  CREASTMT.JCL
    ├── DELDEF01: Delete/Define TRXFL work file
    ├── STEP010:  SORT — Create card-keyed transaction copy
    ├── STEP020:  IDCAMS REPRO — Load into TRXFL VSAM KSDS
    ├── STEP030:  IEFBR14 — Clean up previous statements
    └── STEP040:  CBSTM03A → CBSTM03B
                    ├── Read TRXFL (transactions by card)
                    ├── Read XREFFILE (card→account→customer)
                    ├── Read CUSTFILE (customer names/addresses)
                    ├── Read ACCTFILE (account details)
                    ├── Write STATEMNT.PS (text statements)
                    └── Write STATEMNT.HTML (HTML statements)
                │
                ▼
  TXT2PDF1.JCL
    └── Convert STATEMNT.PS → STATEMNT.PS.PDF

Phase 5: BACKUP
───────────────
  TRANBKP.jcl
    └── IEBGENER — Copy TRANSACT.VSAM.KSDS → GDG backup

Phase 6: POST-BATCH — Reopen Online Files
──────────────────────────────────────────
  OPENFIL.jcl
    └── IDCAMS: Reopen/enable VSAM files for online access

Phase 7: AUTHORIZATION MAINTENANCE (if IMS sub-app active)
──────────────────────────────────────────────────────────
  CBPAUP0J.jcl
    └── CBPAUP0C — Purge expired pending authorizations from IMS DB

  UNLDPADB.JCL (periodic)
    └── PAUDBUNL — Unload IMS pending auth DB for backup

  LOADPADB.JCL (recovery)
    └── PAUDBLOD — Reload IMS pending auth DB from backup


┌─────────────────────────────────────────────────────────────────┐
│                  ONE-TIME / SETUP JOBS                          │
└─────────────────────────────────────────────────────────────────┘

  ACCTFILE.jcl  — Initial load of Account VSAM KSDS
  CUSTFILE.jcl  — Initial load of Customer VSAM KSDS
  CARDFILE.jcl  — Initial load of Card VSAM KSDS
  XREFFILE.jcl  — Initial load of Cross-Reference VSAM KSDS
  TRANFILE.jcl  — Initial load of Transaction VSAM KSDS
  TCATBALF.jcl  — Initial load of Category Balance VSAM KSDS
  DISCGRP.jcl   — Initial load of Disclosure Group VSAM KSDS
  TRANTYPE.jcl  — Initial load of Transaction Type VSAM KSDS
  TRANCATG.jcl  — Initial load of Transaction Category VSAM KSDS
  DUSRSECJ.jcl  — Initial load of User Security VSAM KSDS
  DEFGDGB.jcl   — Define GDG bases for backups
  CREADB21.jcl  — Create DB2 database and load tables
  DBPAUTP0.jcl  — Initialize IMS pending auth database


┌─────────────────────────────────────────────────────────────────┐
│                  UTILITY / AD-HOC JOBS                          │
└─────────────────────────────────────────────────────────────────┘

  CBEXPORT.jcl  — Full data export (all VSAM → flat file)
  CBIMPORT.jcl  — Full data import (flat file → all VSAM)
  READACCT.jcl  — Diagnostic: dump account file
  READCARD.jcl  — Diagnostic: dump card file
  READCUST.jcl  — Diagnostic: dump customer file
  READXREF.jcl  — Diagnostic: dump cross-reference file
  PRTCATBL.jcl  — Print category balance file
  WAITSTEP.jcl  — Inter-job delay (calls COBSWAIT→MVSWAIT)
  FTPJCL.JCL   — FTP file transfer
  INTRDRJ1/J2  — Internal reader chaining
  MNTTRDB2.jcl — DB2 transaction type batch maintenance
```

---

## 6. Data Flow Diagram (Simplified)

```
  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
  │  Daily   │     │ Reference│     │  Master  │     │  Output  │
  │  Input   │     │  Data    │     │  Files   │     │  Reports │
  └────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
       │                │                │                │
       ▼                ▼                ▼                ▼
  ┌─────────┐     ┌─────────┐     ┌──────────┐     ┌──────────┐
  │DALYTRAN │     │TRANTYPE │     │ACCTDATA  │     │STATEMNT  │
  │  (.PS)  │     │TRANCATG │     │CUSTDATA  │     │  (.PS)   │
  │         │     │DISCGRP  │     │CARDDATA  │     │  (.HTML) │
  │         │     │TCATBALF │     │CARDXREF  │     │  (.PDF)  │
  └────┬────┘     └────┬────┘     │TRANSACT  │     │DALYREPT  │
       │               │          │USRSEC    │     └──────────┘
       │               │          └────┬─────┘          ▲
       │               │               │                │
       ▼               ▼               ▼                │
  ┌──────────────────────────────────────────┐          │
  │           BATCH PIPELINE                  │          │
  │  POSTTRAN → COMBTRAN → INTCALC           │──────────┘
  │  → TRANREPT → CREASTMT → TXT2PDF         │
  └──────────────────────────────────────────┘
       │               │               │
       ▼               ▼               ▼
  ┌──────────────────────────────────────────┐
  │           ONLINE (CICS)                   │
  │  Sign-On → Menu → Account/Card/Tran/     │
  │  User/Report/Bill screens                 │
  │  + MQ Authorization processing            │
  │  + DB2 Transaction Type maintenance       │
  └──────────────────────────────────────────┘
```
