# CardDemo Dependency Map

> **System**: AWS CardDemo -- Mainframe Credit Card Management System
> **Generated**: 2026-03-25

---

## 1. Program Call Graph

### 1.1 Online CICS Program Flow (XCTL / RETURN)

The online system uses CICS `EXEC CICS XCTL` (transfer control) for navigation between programs. The COMMAREA (`COCOM01Y`) carries session state.

```
                          [CICS Transaction CC00]
                                  |
                            COSGN00C
                         (Sign-On Screen)
                           /         \
                 [Admin User]      [Regular User]
                    /                   \
              COADM01C              COMEN01C
            (Admin Menu)          (Main Menu)
               |                      |
    +----------+----------+     +-----+-----+-----+-----+-----+-----+-----+-----+-----+
    |          |          |     |     |     |     |     |     |     |     |     |     |
 COUSR00C  COUSR01C  COUSR02C  |     |     |     |     |     |     |     |     |     |
 (List)    (Add)     (Update)  |     |     |     |     |     |     |     |     |     |
    |                  |       |     |     |     |     |     |     |     |     |     |
 COUSR03C           [back]     |     |     |     |     |     |     |     |     |     |
 (Delete)                      |     |     |     |     |     |     |     |     |     |
                               |     |     |     |     |     |     |     |     |     |
                         COACTVWC COACTUPC COCRDLIC COCRDSLC COCRDUPC COTRN00C COTRN01C
                         (Acct   (Acct   (Card   (Card   (Card   (Tran   (Tran
                          View)   Update)  List)   View)   Update)  List)   View)
                                                                      |
                                                              +-------+-------+
                                                              |               |
                                                          COTRN02C       CORPT00C
                                                          (Tran Add)     (Reports)
                                                                              |
                                                                          COBIL00C
                                                                        (Bill Pay)
```

### 1.2 Detailed XCTL Transfer Matrix

| Source Program | Target Program(s)          | Trigger                          |
|---------------|----------------------------|----------------------------------|
| COSGN00C      | COMEN01C, COADM01C         | Successful login (by user type)  |
| COMEN01C      | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | Menu option selection |
| COADM01C      | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | Admin menu option selection |
| COCRDLIC      | COMEN01C, COCRDSLC, COCRDUPC | Card list: back, view, update     |
| COCRDSLC      | COCRDLIC (via XCTL)         | Back to card list                |
| COCRDUPC      | COCRDLIC (via XCTL)         | After update / cancel            |
| COACTVWC      | COMEN01C (via XCTL)         | Back to menu                     |
| COACTUPC      | COMEN01C (via XCTL)         | After update / cancel            |
| COTRN00C      | COMEN01C, COTRN01C          | Back, view detail                |
| COUSR00C      | COADM01C, COUSR01C, COUSR02C | Back, add, update               |

### 1.3 Batch Program CALL Graph

Batch programs use `CALL 'program'` for subroutine invocation and `CALL 'CEE3ABD'` for abnormal termination.

```
CBSTM03A  ──CALL──>  CBSTM03B     (Statement generation -> subroutine)
CBSTM03A  ──CALL──>  CEE3ABD      (LE abnormal termination)

CBACT01C  ──CALL──>  COBDATFT     (Account read -> assembler date format)
CBACT01C  ──CALL──>  CEE3ABD      (LE abnormal termination)

CBACT02C  ──CALL──>  CEE3ABD      (Card read -> LE abnormal termination)
CBACT03C  ──CALL──>  CEE3ABD      (Xref read -> LE abnormal termination)
CBACT04C  ──CALL──>  CEE3ABD      (Interest calc -> LE abnormal termination)
CBCUS01C  ──CALL──>  CEE3ABD      (Customer read -> LE abnormal termination)
CBTRN01C  ──CALL──>  CEE3ABD      (Tran validation -> LE abnormal termination)
CBTRN02C  ──CALL──>  CEE3ABD      (Tran posting -> LE abnormal termination)
CBTRN03C  ──CALL──>  CEE3ABD      (Tran report -> LE abnormal termination)
CBEXPORT  ──CALL──>  CEE3ABD      (Export -> LE abnormal termination)
CBIMPORT  ──CALL──>  CEE3ABD      (Import -> LE abnormal termination)

COBSWAIT  ──CALL──>  MVSWAIT      (Wait utility -> assembler timer)

CORPT00C  ──CALL──>  CSUTLDTC     (Online report -> date utility)
COTRN02C  ──CALL──>  CSUTLDTC     (Online tran add -> date utility)
CSUTLDTC  ──CALL──>  CEEDAYS      (Date utility -> LE date conversion)
```

### 1.4 Complete Call Summary Table

| Caller       | Callee(s)                  | Mechanism   | Purpose                        |
|-------------|----------------------------|-------------|--------------------------------|
| CBSTM03A    | CBSTM03B                   | CALL        | Statement line formatting       |
| CBSTM03A    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBACT01C    | COBDATFT (asm)             | CALL        | Date formatting                 |
| CBACT01C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBACT02C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBACT03C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBACT04C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBCUS01C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBTRN01C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBTRN02C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBTRN03C    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBEXPORT    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| CBIMPORT    | CEE3ABD                    | CALL        | Abnormal termination handler    |
| COBSWAIT    | MVSWAIT (asm)              | CALL        | Timer wait                      |
| CORPT00C    | CSUTLDTC                   | CALL        | Date conversion                 |
| COTRN02C    | CSUTLDTC                   | CALL        | Date conversion                 |
| CSUTLDTC    | CEEDAYS (LE)               | CALL        | Julian date conversion          |

---

## 2. Copybook Inclusion Matrix

Shows which copybooks are included (via `COPY`) by each COBOL program.

### 2.1 Online Programs

| Copybook    | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | CORPT00C | COBIL00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|-------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COCOM01Y    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| COTTL01Y    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| CSDAT01Y    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| CSMSG01Y    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| DFHAID      |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| DFHBMSCA    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |
| CSUSR01Y    |    X     |    X     |    X     |    X     |    X     |    X     |    X     |    X     |          |          |          |          |          |    X     |    X     |    X     |    X     |
| CSMSG02Y    |          |          |          |    X     |    X     |          |    X     |    X     |          |          |          |          |          |          |          |          |          |
| CVCRD01Y    |          |          |          |    X     |    X     |    X     |    X     |    X     |          |          |          |          |          |          |          |          |          |
| CVACT01Y    |          |          |          |    X     |    X     |          |          |          |          |          |    X     |          |    X     |          |          |          |          |
| CVACT02Y    |          |          |          |    X     |          |    X     |    X     |    X     |          |          |          |          |          |          |          |          |          |
| CVACT03Y    |          |          |          |    X     |    X     |          |          |          |          |          |    X     |          |    X     |          |          |          |          |
| CVCUS01Y    |          |          |          |    X     |    X     |          |    X     |    X     |          |          |          |          |          |          |          |          |          |
| CVTRA05Y    |          |          |          |          |          |          |          |          |    X     |    X     |    X     |    X     |    X     |          |          |          |          |
| CSSTRPFY    |          |          |          |    X     |          |    X     |    X     |    X     |          |          |          |          |          |          |          |          |          |
| CSUTLDWY    |          |          |          |          |    X     |          |          |          |          |          |          |          |          |          |          |          |          |
| CSLKPCDY    |          |          |          |          |    X     |          |          |          |          |          |          |          |          |          |          |          |          |
| CSSETATY    |          |          |          |          |    X     |          |          |          |          |          |          |          |          |          |          |          |          |
| COADM02Y    |          |          |    X     |          |          |          |          |          |          |          |          |          |          |          |          |          |          |
| COMEN02Y    |          |    X     |          |          |          |          |          |          |          |          |          |          |          |          |          |          |          |

### 2.2 Batch Programs

| Copybook    | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|-------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y    |    X     |          |          |    X     |          |    X     |    X     |          |    X     |          |    X     |    X     |
| CVACT02Y    |          |    X     |          |          |          |    X     |          |          |          |          |    X     |    X     |
| CVACT03Y    |          |          |    X     |    X     |          |    X     |    X     |    X     |    X     |          |    X     |    X     |
| CVCUS01Y    |          |          |          |          |    X     |    X     |          |          |          |          |    X     |    X     |
| CVTRA01Y    |          |          |          |    X     |          |          |    X     |          |          |          |          |          |
| CVTRA02Y    |          |          |          |    X     |          |          |          |          |          |          |          |          |
| CVTRA03Y    |          |          |          |          |          |          |          |    X     |          |          |          |          |
| CVTRA04Y    |          |          |          |          |          |          |          |    X     |          |          |          |          |
| CVTRA05Y    |          |          |          |    X     |          |          |    X     |    X     |          |          |    X     |    X     |
| CVTRA06Y    |          |          |          |          |          |    X     |    X     |          |          |          |          |          |
| CVTRA07Y    |          |          |          |          |          |          |          |    X     |          |          |          |          |
| CVEXPORT    |          |          |          |          |          |          |          |          |          |          |    X     |    X     |
| COSTM01     |          |          |          |          |          |          |          |          |    X     |          |          |          |
| CUSTREC     |          |          |          |          |          |          |          |          |    X     |          |          |          |
| CODATECN    |    X     |          |          |          |          |          |          |          |          |          |          |          |

---

## 3. Data Lineage -- VSAM Files

### 3.1 VSAM File Access Matrix

Shows which programs read (R), write (W), update (U), or browse (B) each VSAM file.

| VSAM Dataset (Short Name)       | Online Programs                              | Batch Programs                               |
|----------------------------------|----------------------------------------------|----------------------------------------------|
| **ACCTDATA** (Account Master)    | COACTVWC(R), COACTUPC(R/U), COBIL00C(R/U), COTRN02C(R) | CBACT01C(R), CBACT04C(R/U), CBTRN02C(R/U), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) |
| **CARDDATA** (Card Master)       | COCRDLIC(B), COCRDSLC(R), COCRDUPC(R/U)      | CBACT02C(R), CBTRN01C(R), CBEXPORT(R), CBIMPORT(W) |
| **CUSTDATA** (Customer Master)   | COACTVWC(R), COACTUPC(R), COCRDSLC(R), COCRDUPC(R) | CBCUS01C(R), CBTRN01C(R), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) |
| **CARDXREF** (Cross-Reference)   | COACTVWC(R), COACTUPC(R), COBIL00C(R), COTRN02C(R) | CBACT03C(R), CBACT04C(R), CBTRN01C(R), CBTRN02C(R), CBTRN03C(R), CBSTM03A(R), CBEXPORT(R), CBIMPORT(W) |
| **TRANSACT** (Transaction Master)| COTRN00C(B), COTRN01C(R), COTRN02C(W), COBIL00C(W) | CBTRN02C(R/W), CBTRN03C(R), CBEXPORT(R), CBIMPORT(W) |
| **USRSEC** (User Security)       | COSGN00C(R), COUSR00C(B), COUSR01C(W), COUSR02C(R/U), COUSR03C(R/D) | -- |
| **TCATBALF** (Category Balance)  | --                                           | CBACT04C(R/U), CBTRN02C(R/U)                |
| **TRANTYPE** (Transaction Types) | --                                           | CBTRN03C(R)                                  |
| **TRANCATG** (Transaction Cats)  | --                                           | CBTRN03C(R)                                  |
| **DISCGRP** (Discount Groups)    | --                                           | CBACT04C(R)                                  |
| **DALYTRAN** (Daily Trans - PS)  | --                                           | CBTRN01C(R), CBTRN02C(R)                     |

*Legend: R=Read, W=Write, U=Update/Rewrite, B=Browse (STARTBR/READNEXT), D=Delete*

### 3.2 Data Flow Diagram -- Batch Processing Cycle

```
                                    Daily Transaction
                                    Input (PS file)
                                         |
                                         v
                               +-------------------+
                               |   CBTRN01C        |
                               | (Tran Validation)  |
                               +--------+----------+
                                        |
                         +--------------+---------------+
                         |                              |
                    Valid Transactions            Rejected Records
                         |                              |
                         v                              v
               +-------------------+           DALYREJS (GDG)
               |   CBTRN02C        |
               | (Tran Posting)     |
               +--------+----------+
                         |
            +------------+------------+
            |            |            |
            v            v            v
      TRANSACT      ACCTDATA     TCATBALF
      (Master)     (Balances)   (Cat Balances)
            |
            +------------+
            |            |
            v            v
   +----------------+  +-----------------+
   |   CBACT04C     |  |   TRANBKP.jcl   |
   | (Interest Calc) | | (Backup to GDG)  |
   +--------+-------+  +--------+--------+
            |                     |
            v                     v
      TRANSACT              TRANSACT.BKUP
      (Int. Trans)          (GDG backup)
            |                     |
            |                     v
            |            +----------------+
            |            |  COMBTRAN.jcl   |
            |            | (SORT + merge)   |
            |            +--------+--------+
            |                     |
            |                     v
            |            TRANSACT.COMBINED
            |                 (GDG)
            |
            +---> +-------------------+        +-------------------+
                  |   CBTRN03C        |        |   CBSTM03A        |
                  | (Tran Report Gen)  |        | (Statement Gen)    |
                  +--------+----------+        +--------+----------+
                           |                            |
                           v                            v
                     TRANREPT (GDG)              STATEMNT.PS
                                                 STATEMNT.HTML
```

### 3.3 Data Flow Diagram -- CICS Online Operations

```
   [3270 Terminal]
         |
         v
   +-----------+       +-----------+
   | COSGN00C  |------>| USRSEC    |  (Read - authenticate)
   +-----------+       +-----------+
         |
         v
   +-----------+
   | COMEN01C  |  (Navigation only - no file I/O)
   +-----------+
         |
    +----+----+----+----+----+----+
    |    |    |    |    |    |    |
    v    v    v    v    v    v    v

 COACTVWC  COACTUPC  COCRDLIC  COTRN00C  COBIL00C  COUSR00C-03C
    |          |         |         |         |          |
    v          v         v         v         v          v
 ACCTDATA  ACCTDATA  CARDDATA  TRANSACT  ACCTDATA   USRSEC
 CUSTDATA  CUSTDATA  (Browse)  (Browse)  CARDXREF   (CRUD)
 CARDXREF  CARDXREF           TRANSACT
 CARDDATA  CVCUS01Y           (Read)    TRANSACT
           (Read/                       (Write -
            Update)                      payment)
```

---

## 4. JCL Job-to-Program Mapping

### 4.1 Jobs That Execute COBOL Programs

| JCL Job         | Program(s) Executed | Input Datasets                           | Output Datasets                        |
|-----------------|---------------------|------------------------------------------|----------------------------------------|
| POSTTRAN.jcl    | CBTRN02C            | DALYTRAN.PS, TRANSACT, CARDXREF, ACCTDATA, TCATBALF | TRANSACT, ACCTDATA, TCATBALF, DALYREJS |
| INTCALC.jcl     | CBACT04C            | TCATBALF, CARDXREF, ACCTDATA, DISCGRP    | SYSTRAN (interest transactions)        |
| TRANREPT.jcl    | SORT, CBTRN03C      | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT (GDG report)                 |
| CREASTMT.JCL    | SORT, CBSTM03A      | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA   | STATEMNT.PS, STATEMNT.HTML             |
| READACCT.jcl    | CBACT01C            | ACCTDATA.VSAM.KSDS                       | ACCTDATA.PSCOMP, .ARRYPS, .VBPS       |
| READCARD.jcl    | CBACT02C            | CARDDATA.VSAM.KSDS                       | (SYSOUT only)                          |
| READCUST.jcl    | CBCUS01C            | CUSTDATA.VSAM.KSDS                       | (SYSOUT only)                          |
| READXREF.jcl    | CBACT03C            | CARDXREF.VSAM.KSDS                       | (SYSOUT only)                          |
| CBEXPORT.jcl    | CBEXPORT            | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | EXPORT.DATA                    |
| CBIMPORT.jcl    | CBIMPORT            | EXPORT.DATA                              | *.IMPORT (customer, account, xref, tran, errors) |
| WAITSTEP.jcl    | COBSWAIT            | (SYSIN delay parm)                       | (none)                                 |

### 4.2 Jobs That Use Utilities Only (IDCAMS, SORT, SDSF)

| JCL Job         | Utility   | Purpose                                   |
|-----------------|-----------|-------------------------------------------|
| ACCTFILE.jcl    | IDCAMS    | Delete/define/REPRO account VSAM           |
| CARDFILE.jcl    | IDCAMS    | Delete/define/REPRO card VSAM + AIX        |
| CUSTFILE.jcl    | IDCAMS    | Delete/define/REPRO customer VSAM          |
| XREFFILE.jcl    | IDCAMS    | Delete/define/REPRO cross-reference VSAM   |
| TRANFILE.jcl    | IDCAMS    | Delete/define/REPRO transaction VSAM       |
| DUSRSECJ.jcl    | IEBGENER + IDCAMS | Create + load user security VSAM  |
| TCATBALF.jcl    | IDCAMS    | Delete/define/REPRO category balance VSAM  |
| TRANCATG.jcl    | IDCAMS    | Delete/define/REPRO transaction category   |
| TRANTYPE.jcl    | IDCAMS    | Delete/define/REPRO transaction type       |
| DISCGRP.jcl     | IDCAMS    | Delete/define/REPRO discount group         |
| TRANBKP.jcl     | IDCAMS    | REPRO transaction master to GDG backup     |
| COMBTRAN.jcl    | SORT + IDCAMS | Sort-merge backup + system trans       |
| TRANIDX.jcl     | IDCAMS    | Define/build alternate indexes             |
| CLOSEFIL.jcl    | SDSF      | Close CICS files (operator command)        |
| OPENFIL.jcl     | SDSF      | Open CICS files (operator command)         |
| DALYREJS.jcl    | IDCAMS    | Define daily rejects GDG                   |
| REPTFILE.jcl    | IDCAMS    | Define report file                         |
| DEFGDGB.jcl     | IDCAMS    | Define GDG base entries                    |
| DEFGDGD.jcl     | IDCAMS + IEBGENER | Define GDG + initial copies          |
| DEFCUST.jcl     | IDCAMS    | Define customer VSAM cluster               |
| ESDSRRDS.jcl    | IEBGENER + IDCAMS | Define ESDS/RRDS VSAM              |
| PRTCATBL.jcl    | SORT      | Print/sort category balance                |
| CBADMCDJ.jcl    | DFHCSDUP  | CICS CSD resource definitions              |

---

## 5. Batch Job Execution Sequence

The nightly batch cycle must run in this order (from scheduler configs and CLOSEFIL/OPENFIL boundaries):

```
Phase 1 - CICS Quiesce
  +--> CLOSEFIL.jcl          Close CICS files for batch exclusive access

Phase 2 - Data Refresh (run as needed)
  +--> ACCTFILE.jcl           Reload account master from PS
  +--> CARDFILE.jcl           Reload card master from PS
  +--> CUSTFILE.jcl           Reload customer master from PS
  +--> XREFFILE.jcl           Reload cross-reference from PS
  +--> TRANFILE.jcl           Reload transaction master from PS

Phase 3 - Transaction Processing
  +--> POSTTRAN.jcl           Post daily transactions (CBTRN02C)
  +--> INTCALC.jcl            Calculate interest (CBACT04C)

Phase 4 - Backup & Consolidation
  +--> TRANBKP.jcl            Backup transaction master to GDG
  +--> COMBTRAN.jcl           Merge backup + system transactions

Phase 5 - Reporting & Statements
  +--> TRANREPT.jcl           Generate transaction reports (CBTRN03C)
  +--> CREASTMT.JCL           Generate account statements (CBSTM03A)

Phase 6 - Index Maintenance
  +--> TRANIDX.jcl            Rebuild alternate indexes

Phase 7 - CICS Resume
  +--> OPENFIL.jcl            Reopen CICS files for online access
```

---

## 6. Dataset Inventory

### 6.1 VSAM KSDS Clusters

| Dataset Name                               | Record Layout  | Key Field        | Used By (Programs)                   |
|--------------------------------------------|---------------|------------------|--------------------------------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS        | CVACT01Y      | ACCT-ID          | 8 online + 6 batch                   |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS        | CVACT02Y      | CARD-NUM         | 4 online + 4 batch                   |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS        | CVCUS01Y      | CUST-ID          | 5 online + 4 batch                   |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS        | CVACT03Y      | XREF-CARD-NUM    | 5 online + 7 batch                   |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS        | CVTRA05Y      | TRAN-ID          | 5 online + 5 batch                   |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS          | CSUSR01Y      | SEC-USR-ID       | 5 online                             |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS        | CVTRA01Y      | Composite        | 2 batch                              |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS        | CVTRA03Y      | TRAN-TYPE        | 1 batch                              |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS        | CVTRA04Y      | Composite        | 1 batch                              |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS         | CVTRA02Y      | Composite        | 1 batch                              |

### 6.2 Sequential / GDG Datasets

| Dataset Name                               | Purpose                        | Producer         | Consumer         |
|--------------------------------------------|--------------------------------|------------------|------------------|
| AWS.M2.CARDDEMO.DALYTRAN.PS               | Daily transaction input         | External feed    | CBTRN01C, CBTRN02C |
| AWS.M2.CARDDEMO.TRANSACT.BKUP (GDG)       | Transaction backup              | TRANBKP.jcl     | COMBTRAN.jcl     |
| AWS.M2.CARDDEMO.SYSTRAN (GDG)             | System-generated transactions   | INTCALC.jcl      | COMBTRAN.jcl     |
| AWS.M2.CARDDEMO.TRANSACT.COMBINED (GDG)    | Merged transactions            | COMBTRAN.jcl     | (archive)        |
| AWS.M2.CARDDEMO.TRANSACT.DALY (GDG)        | Sorted daily transactions      | TRANREPT.jcl     | CBTRN03C         |
| AWS.M2.CARDDEMO.DALYREJS (GDG)             | Rejected daily transactions    | CBTRN02C         | (review/audit)   |
| AWS.M2.CARDDEMO.TRANREPT (GDG)             | Transaction report output      | CBTRN03C         | TXT2PDF1.JCL     |
| AWS.M2.CARDDEMO.STATEMNT.PS                | Statement text output          | CBSTM03A         | TXT2PDF1.JCL     |
| AWS.M2.CARDDEMO.STATEMNT.HTML              | Statement HTML output          | CBSTM03A         | (web delivery)   |
| AWS.M2.CARDDEMO.EXPORT.DATA                | Multi-entity export file       | CBEXPORT         | CBIMPORT         |
| AWS.M2.CARDDEMO.TRXFL.SEQ                  | Sorted transaction extract     | SORT (CREASTMT)  | CBSTM03A         |
| AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS            | Indexed transaction extract   | IDCAMS (CREASTMT)| CBSTM03A         |
| AWS.M2.CARDDEMO.DATEPARM                   | Date parameter file            | (manual/config)  | CBTRN03C         |
