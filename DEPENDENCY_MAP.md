# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM Mainframe (COBOL/CICS/VSAM/JCL)

## 1. Program Call Graph

### 1.1 Online CICS Program Flow

The CICS online flow uses `EXEC CICS XCTL` (transfer control) via COMMAREA to navigate between programs. Navigation targets are stored in `CDEMO-TO-PROGRAM` within the COMMAREA, making the call graph dynamic at runtime.

```
                              ┌──────────────┐
                              │   COSGN00C   │
                              │  (Sign-on)   │
                              └──────┬───────┘
                                     │ XCTL
                         ┌───────────┴───────────┐
                         ▼                       ▼
                  ┌──────────────┐        ┌──────────────┐
                  │   COMEN01C   │        │   COADM01C   │
                  │ (Main Menu)  │        │ (Admin Menu) │
                  └──────┬───────┘        └──────┬───────┘
                         │ XCTL                  │ XCTL
        ┌────────┬───────┼────────┬──────┐       │
        ▼        ▼       ▼        ▼      ▼       ▼
   COACTVWC  COCRDLIC COTRN00C COBIL00C CORPT00C COUSR00C
   (AcctView)(CardLst)(TranLst)(BillPay)(Report) (UsrList)
        │        │       │                        │
        ▼        ▼       ▼                        ▼
   COACTUPC  COCRDSLC COTRN01C               COUSR01C
   (AcctUpd) (CardVw) (TranVw)               (UsrAdd)
                 │       │                        │
                 ▼       ▼                   COUSR02C
            COCRDUPC COTRN02C                (UsrUpd)
            (CardUpd)(TranAdd)                    │
                                             COUSR03C
                                             (UsrDel)
```

### 1.2 Static XCTL Targets (Hard-coded)

| Source Program | Target Program | Mechanism            | Condition                         |
|----------------|---------------|----------------------|-----------------------------------|
| COSGN00C       | COADM01C      | XCTL PROGRAM('COADM01C') | User type = Admin            |
| COSGN00C       | COMEN01C      | XCTL PROGRAM('COMEN01C') | User type = Regular          |

### 1.3 Dynamic XCTL Targets (Via COMMAREA)

All other online programs use `XCTL PROGRAM(CDEMO-TO-PROGRAM)` where the target is set dynamically from the menu option tables defined in `COMEN02Y` and `COADM02Y`.

| Source Program | Target Variable          | Possible Targets                                       |
|----------------|--------------------------|--------------------------------------------------------|
| COMEN01C       | CDEMO-MENU-OPT-PGMNAME  | COACTVWC, COCRDLIC, COTRN00C, COBIL00C, CORPT00C      |
| COADM01C       | CDEMO-ADMIN-OPT-PGMNAME | COUSR00C                                               |
| COCRDLIC       | CCARD-NEXT-PROG          | COCRDSLC (view), COCRDUPC (update)                     |
| COTRN00C       | CDEMO-TO-PROGRAM         | COTRN01C (view)                                        |
| COUSR00C       | CDEMO-TO-PROGRAM         | COUSR01C (add), COUSR02C (update), COUSR03C (delete)   |
| All online     | CDEMO-TO-PROGRAM         | Back to calling program (PF3 = return)                 |

### 1.4 Batch Program CALL Graph

```
CBSTM03A ──CALL──► CBSTM03B    (Statement generation calls file I/O subroutine)
CBACT01C ──CALL──► COBDATFT    (Account read calls assembler date formatter)
COTRN02C ──CALL──► CSUTLDTC    (Transaction add calls date validation)
CORPT00C ──CALL──► CSUTLDTC    (Report screen calls date validation)
COBSWAIT ──CALL──► MVSWAIT     (Wait utility calls assembler sleep)
```

| Caller     | Callee     | Mechanism    | Purpose                          |
|------------|------------|--------------|----------------------------------|
| CBSTM03A   | CBSTM03B   | CALL 'CBSTM03B' | File I/O for statement generation |
| CBACT01C   | COBDATFT   | CALL 'COBDATFT'  | Format dates in account records   |
| COTRN02C   | CSUTLDTC   | CALL 'CSUTLDTC'  | Validate transaction dates        |
| CORPT00C   | CSUTLDTC   | CALL 'CSUTLDTC'  | Validate report date parameters   |
| COBSWAIT   | MVSWAIT    | CALL 'MVSWAIT'   | Assembler wait/sleep              |

### 1.5 System Calls (Error Handling)

Multiple batch programs call `CEE3ABD` (LE abend routine) for abnormal termination:
- CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT

---

## 2. Copybook Dependency Matrix

Shows which programs include which copybooks (excluding system copybooks DFHAID/DFHBMSCA).

| Copybook   | Programs Using It                                                              | Usage Count |
|------------|--------------------------------------------------------------------------------|:-----------:|
| COCOM01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COBIL00C, CORPT00C, COTRN00C-02C, COUSR00C-03C | 15 |
| COTTL01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COBIL00C, CORPT00C, COTRN00C-02C, COUSR00C-03C | 15 |
| CSDAT01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COBIL00C, CORPT00C, COTRN00C-02C, COUSR00C-03C | 15 |
| CSMSG01Y   | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COBIL00C, CORPT00C, COTRN00C-02C, COUSR00C-03C | 15 |
| CVACT01Y   | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, COACTVWC, COACTUPC, COTRN02C, CBEXPORT, CBIMPORT | 10 |
| CVACT03Y   | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COACTVWC, COACTUPC, COTRN02C, CBEXPORT, CBIMPORT | 11 |
| CVCUS01Y   | CBCUS01C, CBTRN01C, CBSTM03A, COACTVWC, COACTUPC, CBEXPORT, CBIMPORT | 7 |
| CSUSR01Y   | COSGN00C, COACTVWC, COACTUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C | 7 |
| CVTRA05Y   | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CORPT00C, COTRN00C, COTRN01C, COTRN02C, CBEXPORT, CBIMPORT | 10 |
| CVTRA06Y   | CBTRN01C, CBTRN02C                                                            | 2 |
| CVACT02Y   | CBACT02C, COACTVWC, CBTRN01C, CBEXPORT, CBIMPORT                             | 5 |
| CVCRD01Y   | COACTUPC, COACTVWC                                                             | 2 |
| CVTRA01Y   | CBACT04C, CBTRN02C                                                             | 2 |
| CVTRA02Y   | CBACT04C                                                                       | 1 |
| CVTRA03Y   | CBTRN03C                                                                       | 1 |
| CVTRA04Y   | CBTRN03C                                                                       | 1 |
| CVTRA07Y   | CBTRN03C                                                                       | 1 |
| COMEN02Y   | COMEN01C                                                                       | 1 |
| COADM02Y   | COADM01C                                                                       | 1 |
| CSMSG02Y   | COACTVWC, COACTUPC                                                             | 2 |
| CSLKPCDY   | COACTUPC                                                                       | 1 |
| CSSETATY   | COACTUPC (39 COPY REPLACING instances)                                         | 1 |
| CSSTRPFY   | COACTUPC, COACTVWC                                                             | 2 |
| CSUTLDPY   | COACTUPC                                                                       | 1 |
| CSUTLDWY   | COACTUPC                                                                       | 1 |
| COSTM01    | CBSTM03A                                                                       | 1 |
| CUSTREC    | CBSTM03A                                                                       | 1 |
| CODATECN   | CBACT01C                                                                       | 1 |
| CVEXPORT   | CBEXPORT, CBIMPORT                                                             | 2 |

---

## 3. JCL Job-to-Program Mapping

### 3.1 Jobs That Execute COBOL Programs

| JCL Job    | COBOL Program | Step     | Function                                      |
|------------|---------------|----------|-----------------------------------------------|
| POSTTRAN   | CBTRN02C      | STEP15   | Post daily transactions to master              |
| INTCALC    | CBACT04C      | STEP15   | Calculate interest on category balances        |
| CREASTMT   | CBSTM03A      | STEP040  | Generate account statements                    |
| TRANREPT   | CBTRN03C      | STEP10R  | Generate daily transaction detail report       |
| READACCT   | CBACT01C      | STEP05   | Read & display account data                    |
| READCARD   | CBACT02C      | STEP05   | Read & display card data                       |
| READCUST   | CBCUS01C      | STEP05   | Read & display customer data                   |
| READXREF   | CBACT03C      | STEP05   | Read & display cross-reference data            |
| WAITSTEP   | COBSWAIT      | WAIT     | Execute wait utility                           |
| CBEXPORT   | CBEXPORT      | STEP02   | Export data for migration                      |
| CBIMPORT   | CBIMPORT      | STEP01   | Import data from export file                   |

### 3.2 Jobs Using System Utilities Only

| JCL Job    | Utility  | Function                                         |
|------------|----------|--------------------------------------------------|
| ACCTFILE   | IDCAMS   | Define & load Account VSAM                       |
| CARDFILE   | IDCAMS/SDSF | Define & load Card VSAM + AIX                 |
| CUSTFILE   | IDCAMS/SDSF | Define & load Customer VSAM                   |
| XREFFILE   | IDCAMS   | Define & load Cross-Reference VSAM + AIX         |
| TRANFILE   | IDCAMS/SDSF | Define & load Transaction VSAM + AIX          |
| DUSRSECJ   | IEBGENER/IDCAMS | Load User Security VSAM                  |
| TCATBALF   | IDCAMS   | Define & load Category Balance VSAM              |
| DISCGRP    | IDCAMS   | Define & load Disclosure Group VSAM              |
| TRANTYPE   | IDCAMS   | Define & load Transaction Type VSAM              |
| TRANCATG   | IDCAMS   | Define & load Transaction Category VSAM          |
| DALYREJS   | IDCAMS   | Define Daily Rejects cluster                     |
| COMBTRAN   | SORT/IDCAMS | Combine transactions from backup + daily       |
| TRANBKP    | IDCAMS   | Backup transaction file to GDG                   |
| TRANIDX    | IDCAMS   | Define alternate indexes on TRANSACT             |
| CLOSEFIL   | SDSF     | Close CICS-owned files                           |
| OPENFIL    | SDSF     | Open CICS-owned files                            |
| DEFGDGB    | IDCAMS   | Define GDG bases                                 |
| DEFGDGD    | IDCAMS/IEBGENER | Define GDG + seed generations             |
| DEFCUST    | IDCAMS   | Define customer clusters                         |
| REPTFILE   | IDCAMS   | Define report output cluster                     |
| PRTCATBL   | SORT/IEFBR14 | Print/backup category balance              |
| ESDSRRDS   | IEBGENER/IDCAMS | Demo ESDS/RRDS VSAM types               |
| FTPJCL     | FTP      | FTP file transfer                                |
| INTRDRJ1   | IDCAMS/IEBGENER | Internal reader chain (trigger INTRDRJ2)  |
| INTRDRJ2   | IDCAMS   | Internal reader chained job                      |
| TXT2PDF1   | IKJEFT1B | Convert text statements to PDF                   |
| CBADMCDJ   | DFHCSDUP | Load CICS CSD definitions                        |

---

## 4. Data Lineage (VSAM File Access)

### 4.1 VSAM Dataset Access by Program

| VSAM Dataset                         | Short Name     | Read By                                              | Written By                           |
|--------------------------------------|----------------|------------------------------------------------------|--------------------------------------|
| `ACCTDATA.VSAM.KSDS`                | Account Master | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, COACTVWC, COACTUPC, COTRN02C, CBEXPORT | CBACT04C (update), COACTUPC (update), CBTRN02C (update), CBIMPORT |
| `CARDDATA.VSAM.KSDS`                | Card Data      | CBACT02C, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC, CBEXPORT | COCRDUPC (update), CBIMPORT        |
| `CUSTDATA.VSAM.KSDS`                | Customer Data  | CBCUS01C, CBTRN01C, CBSTM03A, COACTVWC, COACTUPC, CBEXPORT | CBIMPORT                            |
| `CARDXREF.VSAM.KSDS`                | Card XREF      | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COTRN02C, CBEXPORT | CBIMPORT              |
| `TRANSACT.VSAM.KSDS`                | Transaction    | CBTRN03C, COTRN00C, COTRN01C, CORPT00C, CBEXPORT    | CBTRN01C, CBTRN02C, COTRN02C, CBIMPORT |
| `USRSEC.VSAM.KSDS`                  | User Security  | COSGN00C, COUSR00C, COUSR02C, COUSR03C               | COUSR01C (write), COUSR02C (rewrite), COUSR03C (delete) |
| `TCATBALF.VSAM.KSDS`                | Cat Balance    | CBACT04C                                              | CBTRN02C                             |
| `DISCGRP.VSAM.KSDS`                 | Disc Group     | CBACT04C                                              | (loaded by JCL)                      |
| `TRANTYPE.VSAM.KSDS`                | Tran Type      | CBTRN03C                                              | (loaded by JCL)                      |
| `TRANCATG.VSAM.KSDS`                | Tran Category  | CBTRN03C                                              | (loaded by JCL)                      |
| `DALYTRAN.PS`                        | Daily Trans    | CBTRN01C, CBTRN02C                                    | (external input)                     |

### 4.2 JCL Data Flow

```
                         ┌─────────────────────────────────────────────────┐
                         │           BATCH PROCESSING CYCLE                │
                         │                                                 │
  External Input         │  CLOSEFIL ──► Data Refresh ──► Processing       │
  ─────────────┐         │                                                 │
               ▼         │  ┌──────────┐    ┌──────────┐    ┌──────────┐  │
  DALYTRAN.PS ──────────────►POSTTRAN  │───►│ INTCALC  │───►│ TRANBKP  │  │
  (Daily Trans)          │  │(CBTRN02C)│    │(CBACT04C)│    │ (IDCAMS) │  │
                         │  └────┬─────┘    └──────────┘    └────┬─────┘  │
                         │       │                               │         │
                         │       ▼                               ▼         │
                         │  TRANSACT.VSAM ◄──────── TRANSACT.BKUP(GDG)    │
                         │       │                                         │
                         │       ▼                                         │
                         │  ┌──────────┐    ┌──────────┐                   │
                         │  │ COMBTRAN │───►│CREASTMT  │──► STATEMNT.PS   │
                         │  │  (SORT)  │    │(CBSTM03A)│──► STATEMNT.HTML │
                         │  └──────────┘    └──────────┘                   │
                         │                       │                         │
                         │                       ▼                         │
                         │                  ┌──────────┐                   │
                         │                  │ TXT2PDF1 │──► STATEMNT.PDF  │
                         │                  └──────────┘                   │
                         │                                                 │
                         │  ┌──────────┐                                   │
                         │  │TRANREPT  │──► TRANREPT(GDG)                  │
                         │  │(CBTRN03C)│    (Daily Tran Report)            │
                         │  └──────────┘                                   │
                         │                                                 │
                         │  TRANIDX ──► OPENFIL                            │
                         └─────────────────────────────────────────────────┘
```

### 4.3 Recommended Batch Execution Order

This is the nightly batch cycle as documented in the JCL:

```
Step 1:  CLOSEFIL       Close CICS files for batch access
Step 2:  ACCTFILE        Refresh account master (if needed)
Step 3:  CARDFILE        Refresh card data (if needed)
Step 4:  CUSTFILE        Refresh customer data (if needed)
Step 5:  XREFFILE        Refresh cross-reference (if needed)
Step 6:  TRANFILE        Refresh transaction master (if needed)
Step 7:  POSTTRAN        Post daily transactions (CBTRN02C)
Step 8:  INTCALC         Calculate interest (CBACT04C)
Step 9:  TRANBKP         Backup transaction master to GDG
Step 10: COMBTRAN        Combine daily + backup transactions
Step 11: CREASTMT        Generate statements (CBSTM03A + CBSTM03B)
Step 12: TRANREPT        Generate transaction report (CBTRN03C)
Step 13: TRANIDX         Rebuild alternate indexes
Step 14: OPENFIL         Reopen CICS files
```

### 4.4 Export/Import Data Flow

```
  ┌──────────┐     ┌─────────────────┐     ┌──────────┐
  │ CBEXPORT │     │  EXPORT.DATA    │     │ CBIMPORT │
  │  (JCL)   │────►│  (multi-record  │────►│  (JCL)   │
  └──────────┘     │   flat file)    │     └────┬─────┘
       │           └─────────────────┘          │
  Reads:                                    Writes:
  - CUSTDATA.VSAM                          - CUSTDATA.IMPORT
  - ACCTDATA.VSAM                          - ACCTDATA.IMPORT
  - CARDXREF.VSAM                          - CARDXREF.IMPORT
  - TRANSACT.VSAM                          - TRANSACT.IMPORT
  - CARDDATA.VSAM                          - IMPORT.ERRORS
```

---

## 5. CICS Resource Dependencies

### 5.1 VSAM Files Accessed via CICS

| CICS File DD | VSAM Dataset                 | Access Mode     | Programs                                 |
|--------------|------------------------------|-----------------|------------------------------------------|
| ACCTFIL      | ACCTDATA.VSAM.KSDS           | Read/Update     | COACTVWC, COACTUPC, COTRN02C            |
| CARDFIL      | CARDDATA.VSAM.KSDS           | Read/Update     | COCRDLIC, COCRDSLC, COCRDUPC            |
| CUSTFIL      | CUSTDATA.VSAM.KSDS           | Read            | COACTVWC, COACTUPC                       |
| USRSEC       | USRSEC.VSAM.KSDS             | Read/Write/Del  | COSGN00C, COUSR00C-03C                  |
| TRANSACT     | TRANSACT.VSAM.KSDS           | Read/Write      | COTRN00C-02C, CORPT00C                  |
| CXACAIX      | CARDXREF.VSAM.AIX.PATH       | Read (AIX)      | COTRN02C, COCRDLIC                       |
| CCXREF       | CARDXREF.VSAM.KSDS           | Read            | COTRN02C                                 |

### 5.2 Transient Data Queues

| TDQ Name | Type          | Used By   | Purpose                                 |
|----------|---------------|-----------|------------------------------------------|
| CSMT     | Extrapartition| CORPT00C  | Submit batch JCL to internal reader      |

---

## 6. Modernization Impact Matrix

This matrix shows which Java microservice boundaries naturally emerge from the dependency clusters.

| Service Boundary       | COBOL Programs                    | VSAM Files                        | Copybooks                     |
|------------------------|-----------------------------------|-----------------------------------|-------------------------------|
| **Auth Service**       | COSGN00C                          | USRSEC                            | CSUSR01Y, COCOM01Y            |
| **User Admin Service** | COUSR00C, COUSR01C, COUSR02C, COUSR03C | USRSEC                     | CSUSR01Y, COCOM01Y            |
| **Account Service**    | COACTVWC, COACTUPC                | ACCTDATA, CUSTDATA, CARDXREF      | CVACT01Y, CVCUS01Y, CVACT03Y |
| **Card Service**       | COCRDLIC, COCRDSLC, COCRDUPC      | CARDDATA, CARDXREF                | CVACT02Y, CVACT03Y, CVCRD01Y |
| **Transaction Service**| COTRN00C, COTRN01C, COTRN02C, COBIL00C | TRANSACT, CARDXREF, ACCTDATA | CVTRA05Y, CVACT03Y, CVACT01Y |
| **Batch Posting**      | CBTRN01C, CBTRN02C                | DALYTRAN, TRANSACT, ACCTDATA, TCATBALF | CVTRA05Y, CVTRA06Y, CVACT01Y |
| **Interest Calc**      | CBACT04C                          | TCATBALF, DISCGRP, ACCTDATA, CARDXREF | CVTRA01Y, CVTRA02Y, CVACT01Y |
| **Reporting**          | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B | TRANSACT, CARDXREF, CUSTDATA, ACCTDATA | CVTRA05Y, CVTRA07Y, COSTM01 |
| **Data Migration**     | CBEXPORT, CBIMPORT                | All VSAM files                    | All data copybooks, CVEXPORT  |
