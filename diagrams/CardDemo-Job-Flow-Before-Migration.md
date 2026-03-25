# CardDemo Batch Job Flow Diagram (Before Migration)

This document describes the complete batch job flow for the CardDemo mainframe credit card management application **before** migration to Java. The jobs are organized by scheduling frequency and execution dependencies.

---

## Scheduling Overview

| Schedule | Jobs | Description |
|----------|------|-------------|
| **Daily** | CLOSEFIL -> TRANBKP -> WAITSTEP -> OPENFIL | Transaction backup cycle |
| **Weekly (Sat)** | MNTTRDB2 -> (CLOSEFIL -> DISCGRP -> WAITSTEP -> OPENFIL) and (TRANEXTR) | Disclosure groups and transaction types DB refresh |
| **Monthly** | CLOSEFIL -> INTCALC -> COMBTRAN -> WAITSTEP -> OPENFIL | Interest calculation and transaction consolidation |
| **On-Demand** | Full batch cycle (see below) | Data refresh + full processing |

---

## Job Flow Diagrams

### 1. Daily Batch Cycle (Transaction Backup)

Runs every day. Backs up the transaction master VSAM file.

```mermaid
flowchart TD
    subgraph DAILY["DAILY - Transaction Backup"]
        direction TB
        D_CLOSEFIL["CLOSEFIL\n(Close CICS Files)\nPGM=SDSF\nCloses: TRANSACT, CCXREF,\nACCTDAT, CXACAIX, USRSEC"]
        D_TRANBKP["TRANBKP\n(Transaction Backup)\nPROC=REPROC + PGM=IDCAMS\nRepro TRANSACT VSAM -> GDG backup\nDelete & redefine TRANSACT VSAM"]
        D_WAITSTEP["WAITSTEP\n(Wait 36 seconds)\nPGM=COBSWAIT"]
        D_OPENFIL["OPENFIL\n(Open CICS Files)\nPGM=SDSF\nOpens: TRANSACT, CCXREF,\nACCTDAT, CXACAIX, USRSEC"]

        D_CLOSEFIL --> D_TRANBKP --> D_WAITSTEP --> D_OPENFIL
    end
```

### 2. Weekly Batch Cycle (Transaction Types DB Refresh + Disclosure Groups Refresh)

Runs every Saturday. Two parallel flows triggered after MNTTRDB2 completes.

```mermaid
flowchart TD
    subgraph WEEKLY["WEEKLY - DB Refresh (Saturday)"]
        direction TB
        W_MNTTRDB2["MNTTRDB2\n(Maintain Transaction Types DB2)\nDB2 transaction type maintenance"]

        subgraph DISC_FLOW["Disclosure Groups Refresh"]
            direction TB
            W_CLOSEFIL["CLOSEFIL\n(Close CICS Files)"]
            W_DISCGRP["DISCGRP\n(Refresh Disclosure Groups)\nPGM=IDCAMS\nDelete/Define/Load\nDISCGRP VSAM from PS"]
            W_WAITSTEP["WAITSTEP\n(Wait)"]
            W_OPENFIL["OPENFIL\n(Open CICS Files)"]

            W_CLOSEFIL --> W_DISCGRP --> W_WAITSTEP --> W_OPENFIL
        end

        subgraph TRAN_FLOW["Transaction Types Refresh"]
            direction TB
            W_TRANEXTR["TRANEXTR\n(Extract Transaction Types)\nExtract transaction types from DB2"]
        end

        W_MNTTRDB2 --> W_CLOSEFIL
        W_MNTTRDB2 --> W_TRANEXTR
    end
```

### 3. Monthly Batch Cycle (Interest Calculation)

Runs monthly. Calculates interest/fees and consolidates transactions.

```mermaid
flowchart TD
    subgraph MONTHLY["MONTHLY - Interest Calculation"]
        direction TB
        M_CLOSEFIL["CLOSEFIL\n(Close CICS Files)\nPGM=SDSF"]
        M_INTCALC["INTCALC\n(Interest Calculation)\nPGM=CBACT04C\nReads: TCATBALF, XREFFILE,\nACCTFILE, DISCGRP\nWrites: SYSTRAN GDG"]
        M_COMBTRAN["COMBTRAN\n(Combine Transactions)\nPGM=SORT + PGM=IDCAMS\nMerge TRANSACT backup + SYSTRAN\nSort by TRAN-ID, load to VSAM"]
        M_WAITSTEP["WAITSTEP\n(Wait)\nPGM=COBSWAIT"]
        M_OPENFIL["OPENFIL\n(Open CICS Files)\nPGM=SDSF"]

        M_CLOSEFIL --> M_INTCALC --> M_COMBTRAN --> M_WAITSTEP --> M_OPENFIL
    end
```

### 4. On-Demand Full Batch Cycle

The complete batch run (from `run_full_batch.sh` and CA7 scheduler) covers data refresh, core processing, and reporting.

```mermaid
flowchart TD
    subgraph PHASE1["PHASE 1: Close CICS Files"]
        CLOSEFIL["CLOSEFIL\n(Close CICS Files)\nPGM=SDSF\nCloses: TRANSACT, CCXREF,\nACCTDAT, CXACAIX, USRSEC"]
    end

    subgraph PHASE2["PHASE 2: Data Refresh (Parallel)"]
        ACCTFILE["ACCTFILE\n(Refresh Account Master)\nPGM=IDCAMS\nDelete/Define/Load\nACCTDATA VSAM KSDS"]
        CARDFILE["CARDFILE\n(Refresh Card Data)\nPGM=IDCAMS + SDSF\nDelete/Define/Load\nCARDDATA VSAM + AIX"]
        XREFFILE["XREFFILE\n(Refresh Card Cross-Ref)\nPGM=IDCAMS\nDelete/Define/Load\nCARDXREF VSAM + AIX"]
        CUSTFILE["CUSTFILE\n(Refresh Customer Data)\nPGM=IDCAMS + SDSF\nDelete/Define/Load\nCUSTDATA VSAM"]
        TRANFILE["TRANFILE\n(Refresh Transaction Master)\nPGM=IDCAMS + SDSF\nDelete/Define/Load\nTRANSACT VSAM + AIX"]
        DISCGRP["DISCGRP\n(Refresh Disclosure Groups)\nPGM=IDCAMS\nDelete/Define/Load\nDISCGRP VSAM"]
        TCATBALF["TCATBALF\n(Refresh Tran Category Balance)\nPGM=IDCAMS\nDelete/Define/Load\nTCATBALF VSAM"]
        TRANTYPE["TRANTYPE\n(Refresh Transaction Types)\nPGM=IDCAMS\nDelete/Define/Load\nTRANTYPE VSAM"]
        DUSRSECJ["DUSRSECJ\n(Load User Security)\nPGM=IEBGENER + IDCAMS\nCreate PS from in-stream\nDefine/Load USRSEC VSAM"]
    end

    subgraph PHASE3["PHASE 3: Core Processing"]
        POSTTRAN["POSTTRAN\n(Post Transactions)\nPGM=CBTRN02C\nProcess daily transaction file\nUpdate TCATBALF & TRANSACT\nCreate DALYREJS GDG"]
    end

    subgraph PHASE4["PHASE 4: Interest & Consolidation"]
        INTCALC["INTCALC\n(Interest Calculation)\nPGM=CBACT04C\nCompute interest and fees\nCreate SYSTRAN GDG"]
        TRANBKP["TRANBKP\n(Transaction Backup)\nPROC=REPROC + IDCAMS\nBackup TRANSACT VSAM to GDG\nDelete & redefine TRANSACT"]
        COMBTRAN["COMBTRAN\n(Combine Transactions)\nPGM=SORT + IDCAMS\nMerge backup + system transactions\nSort and reload to VSAM"]
        TRANIDX["TRANIDX\n(Define Alternate Index)\nPGM=IDCAMS\nDefine AIX, PATH, BLDINDEX\non TRANSACT VSAM"]
    end

    subgraph PHASE5["PHASE 5: Reporting"]
        CREASTMT["CREASTMT\n(Create Statements)\nPGM=SORT + IDCAMS + CBSTM03A\nSort transactions by card\nGenerate text + HTML statements"]
        TXT2PDF1["TXT2PDF1\n(Convert to PDF)\nPGM=IKJEFT1B\nConvert text statements to PDF"]
        PRTCATBL["PRTCATBL\n(Print Category Balance)\nPROC=REPROC + SORT\nExport and format TCATBALF report"]
    end

    subgraph PHASE6["PHASE 6: Open CICS Files"]
        OPENFIL["OPENFIL\n(Open CICS Files)\nPGM=SDSF\nOpens: TRANSACT, CCXREF,\nACCTDAT, CXACAIX, USRSEC"]
    end

    CLOSEFIL --> PHASE2
    ACCTFILE --> PHASE3
    CARDFILE --> PHASE3
    XREFFILE --> PHASE3
    CUSTFILE --> PHASE3
    TRANFILE --> PHASE3
    DISCGRP --> PHASE3
    TCATBALF --> PHASE3
    TRANTYPE --> PHASE3
    DUSRSECJ --> PHASE3
    POSTTRAN --> INTCALC
    INTCALC --> TRANBKP
    TRANBKP --> COMBTRAN
    COMBTRAN --> TRANIDX
    TRANIDX --> CREASTMT
    CREASTMT --> TXT2PDF1
    TXT2PDF1 --> PRTCATBL
    PRTCATBL --> OPENFIL
```

### 5. CA7 Scheduler Chains (Additional Flows)

From the CA7 scheduler definition, additional triggered chains include:

```mermaid
flowchart TD
    subgraph CA7_CHAIN1["CA7: Posting Chain"]
        C1_CLOSEFIL["CLOSEFIL"] --> C1_CBPAUP0J["CBPAUP0J\n(Purge Auth Pending)"]
        C1_CBPAUP0J --> C1_POSTTRAN["POSTTRAN\n(Post Transactions)"]
        C1_POSTTRAN --> C1_WAITSTEP["WAITSTEP"]
        C1_WAITSTEP --> C1_OPENFIL["OPENFIL"]
    end

    subgraph CA7_CHAIN2["CA7: Transaction Type Refresh Chain"]
        C2_CLOSEFIL["CLOSEFIL"] --> C2_TRANTYPE["TRANTYPE"]
        C2_TRANTYPE --> C2_WAITSTEP["WAITSTEP"]
    end

    subgraph CA7_CHAIN3["CA7: Data Validation Chain"]
        C3_CLOSEFIL["CLOSEFIL"] --> C3_READACCT["READACCT\nPGM=CBACT01C"]
        C3_READACCT --> C3_READCARD["READCARD\nPGM=CBACT02C"]
        C3_READCARD --> C3_READCUST["READCUST\nPGM=CBCUS01C"]
        C3_READCUST --> C3_READXREF["READXREF\nPGM=CBACT03C"]
        C3_READXREF --> C3_WAITSTEP["WAITSTEP"]
        C3_WAITSTEP --> C3_OPENFIL["OPENFIL"]
    end

    subgraph CA7_CHAIN4["CA7: Statement Generation Chain"]
        C4_CLOSEFIL["CLOSEFIL"] --> C4_CREASTMT["CREASTMT\nPGM=CBSTM03A"]
        C4_CREASTMT --> C4_TXT2PDF1["TXT2PDF1\nText-to-PDF Conversion"]
        C4_TXT2PDF1 --> C4_WAITSTEP["WAITSTEP"]
        C4_WAITSTEP --> C4_OPENFIL["OPENFIL"]
    end

    subgraph CA7_CHAIN5["CA7: Category Balance Reporting"]
        C5_CLOSEFIL["CLOSEFIL"] --> C5_TRANCATG["TRANCATG\nRefresh Transaction Category"]
        C5_TRANCATG --> C5_WAITSTEP1["WAITSTEP"]
        C5_CLOSEFIL2["CLOSEFIL (2nd)"] --> C5_TCATBALF["TCATBALF\nRefresh Category Balance"]
        C5_TCATBALF --> C5_WAITSTEP2["WAITSTEP"]
        C5_CLOSEFIL3["CLOSEFIL (3rd)"] --> C5_PRTCATBL["PRTCATBL\nPrint Category Balance"]
        C5_PRTCATBL --> C5_WAITSTEP3["WAITSTEP"]
        C5_WAITSTEP3 --> C5_OPENFIL["OPENFIL"]
    end
```

---

## VSAM Data Files (Pre-Migration)

| VSAM File | Record Size | Key | Description | Used By Jobs |
|-----------|-------------|-----|-------------|--------------|
| `ACCTDATA.VSAM.KSDS` | 300 bytes | 11 bytes @ 0 | Account Master | ACCTFILE, POSTTRAN, INTCALC, CREASTMT, CBEXPORT |
| `CARDDATA.VSAM.KSDS` | 150 bytes | 16 bytes @ 0 | Card Data + AIX on Acct ID | CARDFILE, CBEXPORT |
| `CARDXREF.VSAM.KSDS` | 50 bytes | 16 bytes @ 0 | Card Cross-Reference + AIX | XREFFILE, POSTTRAN, INTCALC, CREASTMT, CBEXPORT |
| `CUSTDATA.VSAM.KSDS` | 500 bytes | 9 bytes @ 0 | Customer Data | CUSTFILE, CREASTMT, CBEXPORT |
| `TRANSACT.VSAM.KSDS` | 350 bytes | 16 bytes @ 0 | Transaction Master + AIX | TRANFILE, POSTTRAN, TRANBKP, COMBTRAN, TRANIDX, CREASTMT, CBEXPORT |
| `TCATBALF.VSAM.KSDS` | 50 bytes | 17 bytes @ 0 | Transaction Category Balance | TCATBALF, POSTTRAN, INTCALC, PRTCATBL |
| `TRANTYPE.VSAM.KSDS` | 60 bytes | 2 bytes @ 0 | Transaction Type Codes | TRANTYPE, TRANREPT |
| `TRANCATG.VSAM.KSDS` | 60 bytes | 6 bytes @ 0 | Transaction Categories | TRANCATG, TRANREPT |
| `DISCGRP.VSAM.KSDS` | 50 bytes | 16 bytes @ 0 | Disclosure Groups | DISCGRP, INTCALC |
| `USRSEC.VSAM.KSDS` | 80 bytes | 8 bytes @ 0 | User Security | DUSRSECJ |

---

## COBOL Batch Programs

| Program | JCL Job | Description | Input Files | Output Files |
|---------|---------|-------------|-------------|--------------|
| `CBTRN02C` | POSTTRAN | Process daily transactions, update category balances | TRANFILE, DALYTRAN, XREFFILE, ACCTFILE, TCATBALF | DALYREJS (GDG) |
| `CBACT04C` | INTCALC | Calculate interest and fees on transaction balances | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | SYSTRAN (GDG) |
| `CBSTM03A` | CREASTMT | Generate account statements (text + HTML) | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE | STATEMNT.PS, STATEMNT.HTML |
| `CBSTM03B` | (called by CBSTM03A) | Subroutine for HTML formatting | - | - |
| `CBTRN03C` | TRANREPT | Produce formatted transaction report | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (GDG) |
| `CBACT01C` | READACCT | Read/validate account master file | ACCTFILE | PSCOMP, ARRYPS, VBPS |
| `CBACT02C` | READCARD | Read/validate card data file | CARDDATA | (output files) |
| `CBACT03C` | READXREF | Read/validate cross-reference file | XREFFILE | (output files) |
| `CBCUS01C` | READCUST | Read/validate customer data file | CUSTFILE | (output files) |
| `COBSWAIT` | WAITSTEP | Wait utility (centisecond delay) | SYSIN (delay value) | - |
| `CBEXPORT` | CBEXPORT | Export all VSAM data to a combined export file | All VSAM files | EXPORT.DATA |
| `CBIMPORT` | CBIMPORT | Import from export file into normalized flat files | EXPORT.DATA | CUSTDATA.IMPORT, ACCTDATA.IMPORT, etc. |

---

## GDG (Generation Data Group) Files

| GDG Base | Generations | Created By | Description |
|----------|-------------|------------|-------------|
| `DALYREJS` | 5 | POSTTRAN | Daily rejected transactions |
| `TRANSACT.BKUP` | (per TRANBKP) | TRANBKP, TRANREPT | Transaction master backups |
| `SYSTRAN` | (per INTCALC) | INTCALC | System-generated transactions (interest/fees) |
| `TRANSACT.COMBINED` | (per COMBTRAN) | COMBTRAN | Merged transaction file |
| `TRANSACT.DALY` | (per TRANREPT) | TRANREPT | Filtered daily transactions for reporting |
| `TRANREPT` | 10 | TRANREPT | Formatted transaction reports |
| `TCATBALF.BKUP` | (per PRTCATBL) | PRTCATBL | Category balance backup |

---

## Utility/Setup Jobs (Run Once or As-Needed)

| Job | Purpose |
|-----|---------|
| `DEFGDGB` / `DEFGDGD` | Define GDG bases for backup/report files |
| `DALYREJS` | Define GDG base for daily rejected transactions |
| `REPTFILE` | Define GDG base for transaction reports |
| `ESDSRRDS` | Define ESDS/RRDS VSAM clusters |
| `CBADMCDJ` | Define CICS resource definitions (CSD) for CardDemo |
| `CBEXPORT` | Export data for migration |
| `CBIMPORT` | Import data from export file |
| `FTPJCL` | FTP-based JCL submission |
| `INTRDRJ1` / `INTRDRJ2` | Internal reader jobs |
