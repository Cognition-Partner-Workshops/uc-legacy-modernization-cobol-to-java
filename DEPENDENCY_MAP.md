# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Source**: Static analysis of COBOL COPY statements, CALL verbs, EXEC CICS XCTL/LINK, and JCL EXEC PGM
> **Purpose**: Call graph and data lineage for modernization impact analysis

---

## 1. Online Program Call Graph (CICS)

### 1.1 Navigation Flow

```
                          +-----------+
                          | COSGN00C  |  Sign-on (CC00 transaction)
                          | (Entry)   |
                          +-----+-----+
                                |
                    +-----------+-----------+
                    |                       |
              +-----+-----+         +------+-----+
              | COMEN01C  |         | COADM01C   |  (Admin users only)
              | Main Menu |         | Admin Menu |
              +-----+-----+         +------+-----+
                    |                       |
    +------+--------+--------+------+       +-------+-------+-------+-------+
    |      |        |        |      |       |       |       |       |       |
    v      v        v        v      v       v       v       v       v       v
 COACTVWC COACTUPC COCRDLIC COTRN00C CORPT00C COUSR00C COUSR01C COUSR02C COUSR03C
 Acct     Acct     Card     Txn     Report  User    User    User    User
 View     Update   List     List            List    Add     Update  Delete
                    |        |
                    v        v
                 COCRDSLC  COTRN01C    COBIL00C
                 Card      Txn         Bill
                 Detail    View        Payment
                    |
                    v
                 COCRDUPC
                 Card
                 Update
```

### 1.2 CICS Program Transfer Details

Programs use `EXEC CICS XCTL` (transfer control, no return) to navigate between screens:

| Source Program | Target Program | Transfer Method | Condition                     |
|---------------|---------------|-----------------|-------------------------------|
| COSGN00C      | COMEN01C      | XCTL            | Successful login (regular)    |
| COSGN00C      | COADM01C      | XCTL            | Successful login (admin)      |
| COMEN01C      | COACTVWC      | XCTL            | Menu option 1                 |
| COMEN01C      | COACTUPC      | XCTL            | Menu option 2                 |
| COMEN01C      | COCRDLIC      | XCTL            | Menu option 3                 |
| COMEN01C      | COCRDSLC      | XCTL            | Menu option 4                 |
| COMEN01C      | COCRDUPC      | XCTL            | Menu option 5                 |
| COMEN01C      | COTRN00C      | XCTL            | Menu option 6                 |
| COMEN01C      | COTRN01C      | XCTL            | Menu option 7                 |
| COMEN01C      | COTRN02C      | XCTL            | Menu option 8                 |
| COMEN01C      | CORPT00C      | XCTL            | Menu option 9                 |
| COMEN01C      | COBIL00C      | XCTL            | Menu option 10                |
| COMEN01C      | COPAUS0C      | XCTL            | Menu option 11 (optional)     |
| COADM01C      | COUSR00C      | XCTL            | Admin option 1                |
| COADM01C      | COUSR01C      | XCTL            | Admin option 2                |
| COADM01C      | COUSR02C      | XCTL            | Admin option 3                |
| COADM01C      | COUSR03C      | XCTL            | Admin option 4                |
| COADM01C      | COTRTLIC      | XCTL            | Admin option 5 (optional)     |
| COADM01C      | COTRTUPC      | XCTL            | Admin option 6 (optional)     |
| COCRDLIC      | COCRDSLC      | XCTL            | Select card for view          |
| COCRDLIC      | COCRDUPC      | XCTL            | Select card for update        |
| All programs  | COSGN00C      | XCTL            | PF3 (return to sign-on)       |
| All programs  | COMEN01C      | XCTL            | PF3 (return to menu)          |

### 1.3 Batch Program Call Graph

```
CBACT01C ---CALL---> COBDATFT (Assembler: date formatting)
CBACT02C ---CALL---> CEE3ABD  (LE: abend)
CBACT03C ---CALL---> CEE3ABD  (LE: abend)
CBCUS01C ---CALL---> CEE3ABD  (LE: abend)
CBSTM03A ---CALL---> CBSTM03B (Subroutine: statement file I/O)
COBSWAIT ---CALL---> MVSWAIT  (Assembler: wait utility)
CSUTLDTC ---CALL---> CEEDAYS  (LE: date conversion)
```

### 1.4 Optional Module Call Graph

```
Authorization Module:
  COPAUA0C ---CALL---> MQOPEN, MQGET, MQPUT, MQCLOSE (MQ Series APIs)
  COPAUS1C ---EXEC CICS LINK---> COPAUS2C (fraud marking)
  COACCT01 ---CALL---> MQOPEN, MQGET, MQPUT, MQCLOSE (MQ Series APIs)
  CODATE01 ---CALL---> MQOPEN, MQGET, MQPUT, MQCLOSE (MQ Series APIs)

Transaction Type DB2 Module:
  COTRTLIC ---EXEC SQL---> DB2 (cursor-based paging)
  COTRTUPC ---EXEC SQL---> DB2 (CRUD operations)
  COBTUPDT ---EXEC SQL---> DB2 (batch updates)
```

---

## 2. Copybook Dependency Matrix

### 2.1 Which Programs Use Which Copybooks

| Copybook  | Programs Using It                                                    | Count |
|-----------|----------------------------------------------------------------------|------:|
| COCOM01Y  | COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C-03C, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 20 |
| COTTL01Y  | All 17 core online programs + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 21 |
| CSDAT01Y  | All 17 core online programs + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 21 |
| CSMSG01Y  | All 17 core online programs + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 21 |
| DFHAID    | All 17 core online programs + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 21 |
| DFHBMSCA  | All 17 core online programs + COTRTLIC, COTRTUPC                     | 19 |
| CSUSR01Y  | COSGN00C, COADM01C, COUSR00C-03C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | 16 |
| CVACT01Y  | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C, COACCT01 | 13 |
| CVACT03Y  | COACTVWC, COACTUPC, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C | 13 |
| CVACT02Y  | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUS0C, COTRTLIC, COTRTUPC | 11 |
| CVCUS01Y  | COACTVWC, COACTUPC, COCRDSLC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C | 9 |
| CVTRA05Y  | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | 10 |
| CVCRD01Y  | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC | 7 |
| CSMSG02Y  | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C, COTRTUPC | 7 |
| CVTRA06Y  | CBTRN01C, CBTRN02C                                                   | 2 |
| CVTRA01Y  | CBACT04C, CBTRN02C                                                   | 2 |
| CVTRA02Y  | CBACT04C                                                             | 1 |
| CVTRA03Y  | CBTRN03C                                                             | 1 |
| CVTRA04Y  | CBTRN03C                                                             | 1 |
| CVTRA07Y  | CBTRN03C                                                             | 1 |
| CVEXPORT  | CBEXPORT, CBIMPORT                                                   | 2 |
| CUSTREC   | CBSTM03A                                                             | 1 |
| COSTM01   | CBSTM03A                                                             | 1 |
| CODATECN  | CBACT01C                                                             | 1 |
| CSLKPCDY  | COACTUPC                                                             | 1 |
| CSUTLDWY  | COACTUPC, COTRTUPC                                                   | 2 |
| UNUSED1Y  | (None - dead code)                                                   | 0 |

---

## 3. VSAM File Access Matrix

### 3.1 Online Programs - File Access

| Program   | ACCTDATA | CARDDATA | CUSTDATA | CARDXREF | TRANSACT | USRSEC | TCATBALF | Access Pattern |
|-----------|:--------:|:--------:|:--------:|:--------:|:--------:|:------:|:--------:|----------------|
| COSGN00C  |          |          |          |          |          |   R    |          | Read user creds |
| COACTVWC  |    R     |    R     |    R     |    R     |          |        |          | Read account detail |
| COACTUPC  |    RW    |          |    R     |    R     |          |        |          | Update account |
| COCRDLIC  |          |    R     |          |          |          |        |          | Browse cards |
| COCRDSLC  |          |    R     |    R     |          |          |        |          | Read card detail |
| COCRDUPC  |          |    RW    |          |          |          |        |          | Update card |
| COTRN00C  |          |          |          |          |    R     |        |          | Browse transactions |
| COTRN01C  |          |          |          |          |    R     |        |          | Read transaction |
| COTRN02C  |    R     |          |          |    R     |    RW    |        |          | Add transaction |
| COBIL00C  |    RW    |          |          |    R     |    RW    |        |          | Post payment |
| COUSR00C  |          |          |          |          |          |   R    |          | Browse users |
| COUSR01C  |          |          |          |          |          |   W    |          | Add user |
| COUSR02C  |          |          |          |          |          |   RW   |          | Update user |
| COUSR03C  |          |          |          |          |          |   RW   |          | Delete user |

**Legend**: R = Read, W = Write, RW = Read + Write (Update)

### 3.2 Batch Programs - File Access

| Program   | ACCTDATA | CARDDATA | CUSTDATA | CARDXREF | TRANSACT | DALYTRAN | TCATBALF | DISCGRP | DALYREJS | REPTFILE | TRANTYPE | TRANCATG |
|-----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:--------:|:--------:|:--------:|:--------:|
| CBACT01C  |    R     |          |          |          |          |          |          |         |          |          |          |          |
| CBACT02C  |          |    R     |          |          |          |          |          |         |          |          |          |          |
| CBACT03C  |          |          |          |    R     |          |          |          |         |          |          |          |          |
| CBACT04C  |    RW    |          |          |    R     |    W     |          |    R     |    R    |          |          |          |          |
| CBCUS01C  |          |          |    R     |          |          |          |          |         |          |          |          |          |
| CBTRN01C  |    RW    |    R     |    R     |    R     |    W     |    R     |          |         |          |          |          |          |
| CBTRN02C  |    RW    |          |          |    R     |    W     |    R     |    RW    |         |    W     |          |          |          |
| CBTRN03C  |          |          |          |    R     |    R     |          |          |         |          |    W     |    R     |    R     |
| CBSTM03A  |    R     |          |    R     |    R     |    R     |          |          |         |          |          |          |          |
| CBEXPORT  |    R     |    R     |    R     |    R     |    R     |          |          |         |          |          |          |          |
| CBIMPORT  |    W     |    W     |    W     |    W     |    W     |          |          |         |          |          |          |          |

---

## 4. JCL Job - Program - File Lineage

### 4.1 Batch Cycle Data Flow

The nightly batch cycle follows this prescribed order:

```
Step 1: CLOSEFIL  ──> Close CICS files for batch exclusive access
        │
Step 2: Data Refresh (parallel)
        ├── ACCTFILE  ──> Reload ACCTDATA VSAM from PS
        ├── CARDFILE  ──> Reload CARDDATA VSAM from PS
        ├── CUSTFILE  ──> Reload CUSTDATA VSAM from PS
        ├── XREFFILE  ──> Reload CARDXREF VSAM from PS + build AIX
        ├── TRANFILE  ──> Reload TRANSACT VSAM from PS + build AIX
        ├── DUSRSECJ  ──> Reload USRSEC VSAM from PS
        ├── TRANTYPE  ──> Reload TRANTYPE VSAM from PS
        ├── TRANCATG  ──> Reload TRANCATG VSAM from PS
        ├── TCATBALF  ──> Reload TCATBALF VSAM from PS
        └── DISCGRP   ──> Reload DISCGRP VSAM from PS
        │
Step 3: POSTTRAN  ──> Run CBTRN01C + CBTRN02C
        │              Reads: DALYTRAN, CARDXREF, CARDDATA, CUSTDATA, ACCTDATA
        │              Writes: TRANSACT, ACCTDATA (balance updates), DALYREJS
        │              Updates: TCATBALF (category balances)
        │
Step 4: INTCALC   ──> Run CBACT04C
        │              Reads: TCATBALF, CARDXREF, DISCGRP, ACCTDATA
        │              Writes: TRANSACT (interest entries), ACCTDATA (balance updates)
        │
Step 5: TRANBKP   ──> Backup TRANSACT to GDG generation
        │              Uses: REPROC procedure (IDCAMS REPRO)
        │
Step 6: COMBTRAN  ──> SORT + IDCAMS
        │              Merges daily transactions into cumulative file
        │
Step 7: CREASTMT  ──> SORT + CBSTM03A
        │              Reads: TRANSACT, CARDXREF, CUSTDATA, ACCTDATA
        │              Writes: STATEMNT.HTML, STATEMNT.PS
        │
Step 8: TRANREPT  ──> SORT + CBTRN03C
        │              Reads: TRANSACT, CARDXREF, TRANTYPE, TRANCATG
        │              Writes: Report output (REPTFILE)
        │
Step 9: TRANIDX   ──> IDCAMS
        │              Rebuilds alternate indexes on TRANSACT
        │
Step 10: OPENFIL  ──> Reopen CICS files for online access
```

### 4.2 JCL Job Detail - Programs and Datasets

| JCL Job   | Step  | Program   | Input Datasets                              | Output Datasets                         |
|-----------|-------|-----------|---------------------------------------------|-----------------------------------------|
| POSTTRAN  | STEP1 | CBTRN01C  | DALYTRAN.PS, CUSTDATA, CARDXREF, CARDDATA, ACCTDATA | TRANSACT (write)                |
| POSTTRAN  | STEP2 | CBTRN02C  | DALYTRAN.PS, CARDXREF, ACCTDATA, TRANSACT  | TRANSACT (write), DALYREJS, TCATBALF    |
| INTCALC   | STEP1 | CBACT04C  | TCATBALF, CARDXREF, DISCGRP, ACCTDATA      | TRANSACT (interest entries), ACCTDATA   |
| CREASTMT  | STEP010| SORT     | TRANSACT.VSAM.KSDS                         | TRXFL.SEQ                               |
| CREASTMT  | STEP020| IDCAMS   | TRXFL.SEQ                                  | TRXFL.VSAM.KSDS                         |
| CREASTMT  | STEP040| CBSTM03A | TRXFL.VSAM.KSDS, CARDXREF, CUSTDATA, ACCTDATA | STATEMNT.HTML, STATEMNT.PS          |
| TRANREPT  | STEP05R| REPROC   | TRANSACT.VSAM.KSDS                         | TRANSACT.BKUP(+1)                       |
| TRANREPT  | STEP05R| SORT     | TRANSACT.BKUP(+1)                          | TRANSACT.DALY(+1)                       |
| TRANREPT  | STEP10R| CBTRN03C | TRANSACT.DALY(+1), CARDXREF, TRANTYPE, TRANCATG | Report file                        |
| TRANBKP   | STEP05R| REPROC   | TRANSACT.VSAM.KSDS                         | TRANSACT.BKUP(+1)                       |
| CBEXPORT  | STEP1  | CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA | Export sequential file             |
| CBIMPORT  | STEP1  | CBIMPORT | Export sequential file                      | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA |
| TXT2PDF1  | ---    | TXT2PDF  | STATEMNT.PS                                | PDF output                              |
| READACCT  | STEP05 | CBACT01C | ACCTDATA.VSAM.KSDS                         | SYSPRINT (report)                       |
| READCARD  | STEP05 | CBACT02C | CARDDATA.VSAM.KSDS                         | SYSPRINT (report)                       |
| READCUST  | STEP05 | CBCUS01C | CUSTDATA.VSAM.KSDS                         | SYSPRINT (report)                       |
| READXREF  | STEP05 | CBACT03C | CARDXREF.VSAM.KSDS                         | SYSPRINT (report)                       |
| WAITSTEP  | WAIT   | COBSWAIT | (none)                                     | (none - timer utility)                  |

---

## 5. VSAM Dataset Inventory

| Dataset Name                           | Organization | Key Field       | Record Size | Primary Users                    |
|---------------------------------------|-------------|-----------------|------------:|----------------------------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS   | KSDS        | ACCT-ID (11)    |    300 bytes| COACTVWC, COACTUPC, CBTRN02C, CBACT04C |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS   | KSDS        | CARD-NUM (16)   |    150 bytes| COCRDLIC, COCRDSLC, COCRDUPC     |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS   | KSDS        | CUST-ID (9)     |    500 bytes| COCRDSLC, CBCUS01C, CBSTM03A    |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS   | KSDS + AIX  | XREF-CARD-NUM(16)| 50 bytes  | COACTVWC, COTRN02C, CBTRN02C    |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS   | KSDS + AIX  | TRAN-ID (16)    |    350 bytes| COTRN00C-02C, COBIL00C, CBTRN02C|
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS     | KSDS        | SEC-USR-ID (8)  |     80 bytes| COSGN00C, COUSR00C-03C           |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS   | KSDS        | Composite (17)  |     50 bytes| CBACT04C, CBTRN02C               |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS    | KSDS        | Composite (16)  |     50 bytes| CBACT04C                         |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS   | KSDS        | TRAN-TYPE (2)   |     60 bytes| CBTRN03C                         |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS   | KSDS        | Composite (6)   |     60 bytes| CBTRN03C                         |
| AWS.M2.CARDDEMO.DALYTRAN.PS           | Sequential  | ---             |    350 bytes| CBTRN01C, CBTRN02C (input)       |
| AWS.M2.CARDDEMO.DALYREJS.PS           | Sequential  | ---             |    350 bytes| CBTRN02C (reject output)         |
| AWS.M2.CARDDEMO.STATEMNT.HTML         | Sequential  | ---             |    Variable | CBSTM03A (output)                |
| AWS.M2.CARDDEMO.STATEMNT.PS           | Sequential  | ---             |    Variable | CBSTM03A (output), TXT2PDF1      |
| AWS.M2.CARDDEMO.TRANSACT.BKUP(+n)    | GDG/Seq     | ---             |    350 bytes| TRANBKP (backup generations)     |
| AWS.M2.CARDDEMO.TRANSACT.DALY(+n)    | GDG/Seq     | ---             |    350 bytes| TRANREPT (daily extract)         |

---

## 6. Shared Service Dependencies

| Shared Service   | Type        | Used By Programs                                |
|-----------------|-------------|--------------------------------------------------|
| CEEDAYS         | LE Runtime  | CSUTLDTC (date conversion)                       |
| CEE3ABD         | LE Runtime  | CBACT02C, CBACT03C, CBCUS01C (abend handling)   |
| COBDATFT        | Assembler   | CBACT01C (date formatting)                       |
| MVSWAIT         | Assembler   | COBSWAIT (wait utility)                          |
| IDCAMS          | z/OS Utility| All data refresh JCL, TRANIDX, TRANBKP          |
| SORT            | z/OS Utility| COMBTRAN, CREASTMT, TRANREPT                     |
| IEBGENER        | z/OS Utility| INTRDRJ1 (internal reader)                       |
| FTP             | z/OS Utility| FTPJCL                                           |
| IKJEFT1B        | TSO/E       | TXT2PDF1 (text to PDF)                           |
| SDSF            | z/OS Utility| CLOSEFIL, OPENFIL (CEMT commands)                |
| MQOPEN/GET/PUT  | MQ Series   | COPAUA0C, COACCT01, CODATE01                     |
| DB2 (EXEC SQL)  | Database    | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT           |
| IMS (DL/I)      | Database    | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C           |

---

## 7. Impact Analysis Quick Reference

### If you change a copybook, which programs must be recompiled?

| Changed Copybook | Programs Requiring Recompilation                                     |
|-----------------|-----------------------------------------------------------------------|
| CVACT01Y        | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C, COACCT01 (13 programs) |
| CVCUS01Y        | COACTVWC, COACTUPC, COCRDSLC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C (9 programs) |
| CVTRA05Y        | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT (10 programs) |
| CSUSR01Y        | COSGN00C, COADM01C, COUSR00C-03C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC (16 programs) |
| COCOM01Y        | All online CICS programs (20 programs)                                |
| CVACT02Y        | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUS0C, COTRTLIC, COTRTUPC (11 programs) |
| CVACT03Y        | COACTVWC, COACTUPC, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C (13 programs) |

### If you change a VSAM file structure, which JCL jobs are affected?

| Changed File | JCL Jobs Affected                                              |
|-------------|----------------------------------------------------------------|
| ACCTDATA    | ACCTFILE, POSTTRAN, INTCALC, CREASTMT, READACCT, CBEXPORT, CBIMPORT |
| CARDDATA    | CARDFILE, POSTTRAN, READCARD, CBEXPORT, CBIMPORT               |
| CUSTDATA    | CUSTFILE, CREASTMT, READCUST, CBEXPORT, CBIMPORT              |
| CARDXREF    | XREFFILE, POSTTRAN, INTCALC, CREASTMT, TRANREPT, READXREF, CBEXPORT, CBIMPORT |
| TRANSACT    | TRANFILE, POSTTRAN, INTCALC, TRANBKP, COMBTRAN, CREASTMT, TRANREPT, TRANIDX, CBEXPORT, CBIMPORT |
| USRSEC      | DUSRSECJ                                                       |
