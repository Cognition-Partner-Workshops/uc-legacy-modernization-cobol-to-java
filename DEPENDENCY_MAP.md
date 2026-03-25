# DEPENDENCY MAP -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Contents:** Program call graph, screen navigation flow, batch job chains, and data lineage

---

## 1. Online CICS Program Call Graph

### 1a. Navigation Flow (XCTL -- Transfer Control)

```
                           CICS Transaction CC00
                                  |
                             COSGN00C (Signon)
                            /                \
                   [Admin User]          [Regular User]
                       |                       |
                  COADM01C                COMEN01C
                (Admin Menu)            (Main Menu)
                   |                       |
    +--------------+--------+    +---------+---------+---------+---------+----------+
    |              |        |    |         |         |         |         |          |
 COUSR00C     COUSR01C  (back   COACTVWC  COCRDLIC  COTRN00C  CORPT00C  COBIL00C
 (User List)  (Add User) to     (Acct     (Card     (Tran     (Reports)  (Bill
    |              |     menu)   View)     List)     List)                 Pay)
    |              |              |         |  |       |
 COUSR02C     COUSR03C     COACTUPC   |  COCRDUPC  COTRN01C
 (Update)     (Delete)     (Acct      |  (Card     (Tran
                           Update)    |  Update)   View)
                                      |              |
                                 COCRDSLC       COTRN02C
                                 (Card View)    (Tran Add)
```

### 1b. Detailed XCTL Relationships

| Source Program | Target Program | Trigger | Direction |
|---------------|---------------|---------|-----------|
| COSGN00C | COMEN01C | Successful regular user login | Forward |
| COSGN00C | COADM01C | Successful admin user login | Forward |
| COMEN01C | COACTVWC | Menu option: Account View | Forward |
| COMEN01C | COCRDLIC | Menu option: Card List | Forward |
| COMEN01C | COTRN00C | Menu option: Transaction List | Forward |
| COMEN01C | CORPT00C | Menu option: Reports | Forward |
| COMEN01C | COBIL00C | Menu option: Bill Payment | Forward |
| COMEN01C | COSGN00C | Menu option: Sign Off | Forward |
| COADM01C | COUSR00C | Admin option: User List | Forward |
| COADM01C | COUSR01C | Admin option: Add User | Forward |
| COADM01C | COMEN01C | Return to main menu | Back |
| COCRDLIC | COCRDSLC | Select card for view | Forward |
| COCRDLIC | COCRDUPC | Select card for update | Forward |
| COCRDLIC | COMEN01C | PF3/Return | Back |
| COCRDSLC | COCRDLIC | PF3/Return | Back |
| COCRDUPC | COCRDLIC | PF3/Return | Back |
| COACTVWC | COMEN01C | PF3/Return | Back |
| COACTUPC | COMEN01C | PF3/Return | Back |
| COTRN00C | COTRN01C | Select transaction for view | Forward |
| COTRN00C | COMEN01C | PF3/Return | Back |
| COTRN01C | COTRN00C | PF3/Return | Back |
| COTRN02C | COMEN01C | PF3/Return | Back |
| CORPT00C | COMEN01C | PF3/Return | Back |
| COBIL00C | COMEN01C | PF3/Return | Back |
| COUSR00C | COUSR02C | Select user for update | Forward |
| COUSR00C | COUSR03C | Select user for delete | Forward |
| COUSR00C | COADM01C | PF3/Return | Back |
| COUSR01C | COADM01C | PF3/Return | Back |
| COUSR02C | COUSR00C | PF3/Return | Back |
| COUSR03C | COUSR00C | PF3/Return | Back |

### 1c. CALL Relationships (Subroutine Calls)

| Caller | Callee | Purpose |
|--------|--------|---------|
| COTRN02C | CSUTLDTC | Date validation/conversion |
| CORPT00C | CSUTLDTC | Date validation for report parameters |
| CBACT01C | COBDATFT (ASM) | Date formatting for account records |
| CBSTM03A | CBSTM03B | File I/O for statement generation (called ~13 times) |
| COBSWAIT | MVSWAIT (ASM) | System wait in centiseconds |
| CBACT01C-04C, CBTRN01C-03C, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT | CEE3ABD | LE abnormal termination (error handling) |

### 1d. Optional Module Call Relationships

| Caller | Callee | Technology | Purpose |
|--------|--------|------------|---------|
| COPAUA0C | MQOPEN, MQGET, MQPUT1, MQCLOSE | MQ API | Process authorization via MQ queues |
| COACCT01 | MQOPEN, MQGET, MQPUT, MQCLOSE | MQ API | Account inquiry via MQ |
| CODATE01 | MQOPEN, MQGET, MQPUT, MQCLOSE | MQ API | System date via MQ |
| COPAUS1C | COPAUS2C | CICS LINK | Mark fraud on auth message |
| DBUNLDGS | CBLTDLI (GN, GNP, ISRT) | IMS DL/I | Unload IMS segments |
| PAUDBLOD | CBLTDLI (ISRT, GU) | IMS DL/I | Load data into IMS database |
| PAUDBUNL | CBLTDLI (GN, GNP) | IMS DL/I | Unload IMS database |
| COTRTLIC | DSNTIAC | DB2 | Format SQL error messages |
| COTRTUPC | DSNTIAC | DB2 | Format SQL error messages |

---

## 2. Copybook Inclusion Map

### 2a. Core Copybook Usage Matrix

| Copybook | Used By (Programs) | Usage Count |
|----------|-------------------|-------------|
| **COCOM01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | **21** |
| **COTTL01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C-03C, COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | **21** |
| **CSDAT01Y** | All 17 online core + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | **21** |
| **CSMSG01Y** | All 17 online core + COPAUS0C, COPAUS1C, COTRTLIC, COTRTUPC | **21** |
| **CSUSR01Y** | All 17 online core + COTRTLIC, COTRTUPC | **19** |
| **CVACT01Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C, COACCT01 | **14** |
| **CVACT03Y** | COACTUPC, COACTVWC, COBIL00C, COTRN02C, CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C | **13** |
| **CVCUS01Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C | **10** |
| **CVTRA05Y** | COBIL00C, COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT | **11** |
| **CVACT02Y** | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT, COPAUS0C, COTRTLIC | **10** |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | **2** |
| **CVTRA01Y** | CBACT04C, CBTRN02C | **2** |
| **CVTRA02Y** | CBACT04C | **1** |
| **CVTRA03Y** | CBTRN03C | **1** |
| **CVTRA04Y** | CBTRN03C | **1** |
| **CVTRA07Y** | CBTRN03C | **1** |
| **COSTM01** | CBSTM03A | **1** |
| **CUSTREC** | CBSTM03A | **1** |
| **CVEXPORT** | CBEXPORT, CBIMPORT | **2** |
| **CSSETATY** | COACTUPC (x38), COCRDUPC, COTRTUPC | **3** |
| **CSSTRPFY** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC | **7** |
| **CSUTLDPY** | COACTUPC | **1** |
| **CSUTLDWY** | COACTUPC, COTRTUPC | **2** |
| **CSLKPCDY** | COACTUPC | **1** |
| **CODATECN** | CBACT01C | **1** |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC | **7** |

### 2b. BMS Map Copybook Usage

Each online program includes its corresponding BMS-generated copybook from `app/cpy-bms/`:

| Program | BMS Map Copybook |
|---------|-----------------|
| COSGN00C | COSGN00.CPY |
| COMEN01C | COMEN01.CPY |
| COADM01C | COADM01.CPY |
| COACTVWC | COACTVW.CPY |
| COACTUPC | COACTUP.CPY |
| COCRDLIC | COCRDLI.CPY |
| COCRDSLC | COCRDSL.CPY |
| COCRDUPC | COCRDUP.CPY |
| COTRN00C | COTRN00.CPY |
| COTRN01C | COTRN01.CPY |
| COTRN02C | COTRN02.CPY |
| CORPT00C | CORPT00.CPY |
| COBIL00C | COBIL00.CPY |
| COUSR00C | COUSR00.CPY |
| COUSR01C | COUSR01.CPY |
| COUSR02C | COUSR02.CPY |
| COUSR03C | COUSR03.CPY |

---

## 3. Batch Job Execution Chain

### 3a. Nightly Batch Cycle (Recommended Order)

```
Step 1: CLOSEFIL.jcl          Close CICS files for exclusive batch access
           |
Step 2: Data Refresh (parallel, as needed)
           +-- ACCTFILE.jcl    Refresh account master VSAM
           +-- CARDFILE.jcl    Refresh card master VSAM
           +-- CUSTFILE.jcl    Refresh customer master VSAM
           +-- XREFFILE.jcl    Refresh cross-reference VSAM
           +-- TRANFILE.jcl    Refresh transaction master VSAM
           |
Step 3: POSTTRAN.jcl           Post daily transactions (CBTRN02C)
           |                    Reads: DALYTRAN, XREFFILE, ACCTFILE
           |                    Writes: TRANFILE, DALYREJS, ACCTFILE, TCATBALF
           |
Step 4: INTCALC.jcl            Calculate interest (CBACT04C)
           |                    Reads: TCATBALF, XREFFILE, ACCTFILE, DISCGRP
           |                    Writes: TRANSACT (interest transactions)
           |
Step 5: TRANBKP.jcl            Backup transactions via GDG
           |                    Uses REPROC procedure + IDCAMS
           |
Step 6: COMBTRAN.jcl            Combine daily trans into master
           |                    IDCAMS REPRO
           |
Step 7: CREASTMT.JCL            Generate statements (CBSTM03A)
           |                    Reads: TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE
           |                    Writes: STMTFILE (text), HTMLFILE (HTML)
           |
Step 8: TRANREPT.jcl            Generate transaction report (CBTRN03C)
           |                    Reads: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM
           |                    Writes: TRANREPT (report file)
           |
Step 9: TRANIDX.jcl             Define/rebuild alternate index
           |
Step 10: OPENFIL.jcl            Reopen CICS files for online access
```

### 3b. Job-to-Program Mapping

| JCL Job | COBOL Program | Utility Programs |
|---------|--------------|-----------------|
| POSTTRAN | CBTRN02C | -- |
| INTCALC | CBACT04C | -- |
| CREASTMT | CBSTM03A (calls CBSTM03B) | SORT, IDCAMS |
| TRANREPT | CBTRN03C | REPROC proc, SORT |
| READACCT | CBACT01C | -- |
| READCARD | CBACT02C | -- |
| READCUST | CBCUS01C | -- |
| READXREF | CBACT03C | -- |
| WAITSTEP | COBSWAIT | -- |
| CBEXPORT | CBEXPORT | -- |
| CBIMPORT | CBIMPORT | -- |
| PRTCATBL | -- | REPROC, SORT |

### 3c. JCL Procedure Dependencies

| Procedure | Used By | Function |
|-----------|---------|----------|
| REPROC.prc | TRANBKP, PRTCATBL, TRANREPT | Record reprocessing (REPRO with reformatting) |
| TRANREPT.prc | (standalone) | Transaction report procedure |

---

## 4. Data Lineage -- VSAM File Access by Program

### 4a. Online Programs (CICS VSAM I/O)

| VSAM File | Read | Write | Rewrite | Delete | StartBr/ReadNext | Programs |
|-----------|------|-------|---------|--------|-----------------|----------|
| **USRSEC** | COSGN00C, COUSR00C, COUSR02C, COUSR03C | COUSR01C | COUSR02C | COUSR03C | COUSR00C | 6 programs |
| **ACCTDATA** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | -- | COBIL00C | -- | -- | 4 programs |
| **CARDDATA** | COACTVWC, COCRDSLC, COCRDUPC | -- | COCRDUPC | -- | COCRDLIC | 4 programs |
| **CARDXREF** | COACTVWC, COACTUPC, COBIL00C, COTRN02C | -- | -- | -- | -- | 4 programs |
| **CUSTDATA** | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | -- | -- | -- | -- | 4 programs |
| **TRANSACT** | COTRN00C, COTRN01C | COTRN02C, COBIL00C | -- | -- | COTRN00C | 4 programs |

### 4b. Batch Programs (Sequential/VSAM File I/O)

| File (DD Name) | Read By | Written By | Business Flow |
|----------------|---------|-----------|---------------|
| **DALYTRAN** | CBTRN01C, CBTRN02C | (external feed) | Daily transaction input |
| **TRANFILE/TRANSACT** | CBTRN03C, CBACT04C, CBEXPORT | CBTRN01C, CBTRN02C, CBACT04C | Transaction master |
| **ACCTFILE** | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A/B, CBEXPORT | CBTRN02C | Account master |
| **CARDFILE** | CBACT02C, CBTRN01C, CBEXPORT | -- | Card master |
| **CUSTFILE** | CBCUS01C, CBTRN01C, CBSTM03A/B, CBEXPORT | -- | Customer master |
| **XREFFILE** | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A/B, CBEXPORT | -- | Cross-reference |
| **TCATBALF** | CBACT04C | CBTRN02C, CBACT04C | Tran category balance |
| **DISCGRP** | CBACT04C | -- | Interest rate table |
| **TRANTYPE** | CBTRN03C | -- | Transaction types |
| **TRANCATG** | CBTRN03C | -- | Transaction categories |
| **DALYREJS** | -- | CBTRN02C | Rejected daily transactions |
| **DATEPARM** | CBTRN03C | -- | Report date parameters |
| **TRANREPT** | -- | CBTRN03C | Transaction detail report |
| **STMTFILE** | -- | CBSTM03A | Statement (text) |
| **HTMLFILE** | -- | CBSTM03A | Statement (HTML) |
| **EXPFILE** | CBIMPORT | CBEXPORT | Migration export file |
| **OUTFILE/ARRYFILE/VBRCFILE** | -- | CBACT01C | Account data dump outputs |
| **ERROUT** | -- | CBIMPORT | Import error/reject file |

### 4c. Data Flow Diagram

```
                    External Systems
                         |
                    [Daily Trans Feed]
                         |
                         v
                  +-------------+
                  | DALYTRAN    |  (Daily Transaction File)
                  +------+------+
                         |
              +----------+----------+
              |                     |
              v                     v
     +--------+-------+    +-------+--------+
     | CBTRN01C       |    | CBTRN02C       |  <-- POSTTRAN.jcl
     | (Simple Post)  |    | (Full Post +   |
     |                |    |  Validation)   |
     +--------+-------+    +--+----+----+---+
              |               |    |    |
              v               v    |    v
     +--------+-------+  DALYREJS  | TCATBALF
     | TRANSACT       |  (rejects) |  (cat bal)
     | (Tran Master)  |           |
     +--+--+--+-------+           v
        |  |  |           +-------+--------+
        |  |  |           | CBACT04C       |  <-- INTCALC.jcl
        |  |  |           | (Interest Calc)|
        |  |  |           +-------+--------+
        |  |  |                   |
        |  |  |                   v
        |  |  |            TRANSACT (interest tran)
        |  |  |
        |  |  +-----> CBTRN03C -----> TRANREPT   <-- TRANREPT.jcl
        |  |          (Report)        (detail report)
        |  |
        |  +--------> CBSTM03A/B --> STMTFILE    <-- CREASTMT.JCL
        |              (Statements)   HTMLFILE
        |
        +-----------> CBEXPORT ----> EXPFILE      <-- CBEXPORT.jcl
                       (Export)
                                       |
                                       v
                                   CBIMPORT       <-- CBIMPORT.jcl
                                   (Import)
                                       |
                                       v
                              CUSTOUT, ACCTOUT,
                              XREFOUT, TRNXOUT,
                              CARDOUT, ERROUT
```

---

## 5. CICS Resource Dependencies

### 5a. CICS File Definitions (from CSD)

| CICS File Name | VSAM Dataset | Access | Used By |
|----------------|-------------|--------|---------|
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | READ/UPDATE/ADD/DELETE | COSGN00C, COUSR00C-03C |
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | READ/UPDATE | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | READ/UPDATE/BROWSE | COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | READ | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC |
| CARDXRF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | READ/BROWSE | COACTVWC, COACTUPC, COBIL00C, COTRN02C |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | READ/ADD/BROWSE | COTRN00C, COTRN01C, COTRN02C, COBIL00C |

### 5b. CICS Transaction IDs

| Transaction | Program | Type | Description |
|------------|---------|------|-------------|
| CC00 | COSGN00C | Initial | Sign on |
| CM00 | COMEN01C | Menu | Main menu |
| CA00 | COADM01C | Menu | Admin menu |
| CA01 | COACTVWC | Function | Account view |
| CA02 | COACTUPC | Function | Account update |
| CC01 | COCRDLIC | Function | Card list |
| CC02 | COCRDSLC | Function | Card detail |
| CC03 | COCRDUPC | Function | Card update |
| CT00 | COTRN00C | Function | Transaction list |
| CT01 | COTRN01C | Function | Transaction view |
| CT02 | COTRN02C | Function | Transaction add |
| CR00 | CORPT00C | Function | Reports |
| CB00 | COBIL00C | Function | Bill payment |
| CU00 | COUSR00C | Admin | User list |
| CU01 | COUSR01C | Admin | Add user |
| CU02 | COUSR02C | Admin | Update user |
| CU03 | COUSR03C | Admin | Delete user |

---

## 6. Technology Dependency Summary

### 6a. Core Stack Dependencies

| Technology | Component | Used By |
|------------|----------|---------|
| CICS TS | Transaction processing | All 17 online programs |
| VSAM KSDS | Primary data storage | All programs |
| BMS | 3270 screen maps | All 17 online programs |
| Language Environment (LE) | CEE3ABD, CEEDAYS | All batch programs, CSUTLDTC |
| SORT (DFSORT/SYNCSORT) | Record sorting | CREASTMT, TRANREPT, PRTCATBL |
| IDCAMS | VSAM cluster management | 20+ JCL jobs |
| IEBGENER | Sequential copy | DUSRSECJ, ESDSRRDS, DEFGDGD |
| SDSF | File open/close control | CLOSEFIL, OPENFIL, TRANFILE |
| GDG | Generation data groups | DEFGDGB, DEFGDGD, TRANBKP |

### 6b. Optional Module Stack Dependencies

| Technology | Component | Used By |
|------------|----------|---------|
| IBM MQ | Message queuing | COPAUA0C, COACCT01, CODATE01 |
| IMS DB | Hierarchical database | DBUNLDGS, PAUDBLOD, PAUDBUNL, CBPAUP0C |
| IMS DL/I | Database API (CBLTDLI) | All IMS programs |
| DB2 | Relational database | COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT |
| DSNTIAC | DB2 error formatting | COTRTLIC, COTRTUPC |

### 6c. External Program Dependencies

| External Program | Type | Called By | Purpose |
|-----------------|------|----------|---------|
| CEEDAYS | LE Runtime | CSUTLDTC | Convert date to Lilian format |
| CEE3ABD | LE Runtime | All batch programs | Abnormal termination |
| COBDATFT | Assembler | CBACT01C | Date formatting |
| MVSWAIT | Assembler | COBSWAIT | System wait |
| CBLTDLI | IMS Runtime | IMS programs | DL/I database calls |
| MQOPEN/MQGET/MQPUT/MQCLOSE | MQ Runtime | MQ programs | Message queue operations |
| DSNTIAC | DB2 Runtime | DB2 programs | SQL error formatting |
| IKJEFT1B | TSO/Batch | TXT2PDF1.JCL | TSO batch execution |
| FTP | z/OS | FTPJCL.JCL | File transfer |
