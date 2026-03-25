# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** Static analysis of CALL, XCTL, COPY, DATASET, and JCL DD statements
> **Purpose:** Call graph, data lineage, and copybook dependency map for modernization planning

---

## 1. Program Call Graph

### 1.1 Online CICS Program Flow (XCTL Transfer of Control)

```
                          ┌─────────────┐
                          │  COSGN00C   │  (Login - CC00)
                          │  Signon     │
                          └──────┬──────┘
                                 │
                    ┌────────────┴────────────┐
                    │ Admin user              │ Regular user
                    ▼                         ▼
             ┌─────────────┐          ┌─────────────┐
             │  COADM01C   │          │  COMEN01C   │
             │  Admin Menu │          │  Main Menu  │
             └──────┬──────┘          └──────┬──────┘
                    │                        │
        ┌───────────┼───────────┐    ┌───────┼───────┬──────────┬──────────┬──────────┐
        ▼           ▼           ▼    ▼       ▼       ▼          ▼          ▼          ▼
   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
   │COUSR00C │ │COUSR01C │ │COUSR02C │ │COACTVWC │ │COCRDLIC │ │COTRN00C │ │COBIL00C │
   │User List│ │User Add │ │User Upd │ │Acct View│ │Card List│ │Tran List│ │Bill Pay │
   └─────────┘ └─────────┘ └─────────┘ └────┬────┘ └────┬────┘ └────┬────┘ └─────────┘
                │                            │           │           │
                ▼                            │     ┌─────┴─────┐     │
           ┌─────────┐                       │     ▼           ▼     │
           │COUSR03C │                       │ ┌─────────┐ ┌─────────┐│
           │User Del │                       │ │COCRDSLC │ │COCRDUPC ││
           └─────────┘                       │ │Card View│ │Card Upd ││
                                             │ └─────────┘ └─────────┘│
                                             │                        │
                                             ▼                   ┌────┴────┐
                                        ┌─────────┐             ▼         ▼
                                        │COACTUPC │        ┌─────────┐ ┌─────────┐
                                        │Acct Upd │        │COTRN01C │ │COTRN02C │
                                        └─────────┘        │Tran View│ │Tran Add │
                                                           └─────────┘ └─────────┘

                                        ┌─────────┐
                                        │CORPT00C │  (Accessible from both menus)
                                        │Reports  │
                                        └─────────┘
```

### 1.2 XCTL Transfer Details

| Source Program | Target Program(s) | Mechanism | Condition |
|---------------|-------------------|-----------|-----------|
| COSGN00C | COADM01C | `XCTL PROGRAM('COADM01C')` | Admin user login |
| COSGN00C | COMEN01C | `XCTL PROGRAM('COMEN01C')` | Regular user login |
| COMEN01C | *(dynamic)* | `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME)` | Menu option selected |
| COMEN01C | COSGN00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 exit |
| COADM01C | *(dynamic)* | `XCTL PROGRAM(CDEMO-ADMIN-OPT-PGMNAME)` | Admin menu option |
| COADM01C | COSGN00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 exit |
| COACTVWC | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back to menu |
| COACTUPC | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back to menu |
| COCRDLIC | COCRDSLC | `XCTL PROGRAM(LIT-CARDDTLPGM)` | Select card (S) |
| COCRDLIC | COCRDUPC | `XCTL PROGRAM(LIT-CARDUPDPGM)` | Update card (U) |
| COCRDLIC | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back to menu |
| COCRDSLC | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COCRDUPC | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COTRN00C | COTRN01C / COTRN02C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | View/Add selection |
| COTRN01C | COTRN00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COTRN02C | COTRN00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COBIL00C | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| CORPT00C | COMEN01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COUSR00C | COUSR01C / COUSR02C / COUSR03C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | Add/Update/Delete |
| COUSR01C | COUSR00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COUSR02C | COUSR00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |
| COUSR03C | COUSR00C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | PF3 back |

### 1.3 Batch Program CALL Graph

```
┌────────────────┐        ┌────────────────┐
│   CBSTM03A     │───────▶│   CBSTM03B     │  (13 CALL invocations)
│ Statement Gen  │  CALL  │ File Processing│
└────────────────┘        └────────────────┘

┌────────────────┐        ┌────────────────┐
│   CBACT01C     │───────▶│   COBDATFT     │  (Assembler date format)
│ Account Read   │  CALL  │ Date Format    │
└────────────────┘        └────────────────┘

┌────────────────┐        ┌────────────────┐
│   COBSWAIT     │───────▶│   MVSWAIT      │  (Assembler wait routine)
│ Wait Utility   │  CALL  │ Low-level Wait │
└────────────────┘        └────────────────┘

┌────────────────┐        ┌────────────────┐
│   CORPT00C     │───────▶│   CSUTLDTC     │  (Date validation utility)
│ Reports        │  CALL  │ Date Utility   │
└────────────────┘        └────────────────┘

┌────────────────┐        ┌────────────────┐
│   COTRN02C     │───────▶│   CSUTLDTC     │  (Date validation utility)
│ Tran Add       │  CALL  │ Date Utility   │
└────────────────┘        └────────────────┘

┌────────────────┐        ┌────────────────┐
│   CSUTLDTC     │───────▶│   CEEDAYS      │  (LE runtime date service)
│ Date Utility   │  CALL  │ LE Date Conv   │
└────────────────┘        └────────────────┘

All batch programs call:
  CEE3ABD ──── LE abnormal termination (abend handler)
```

### 1.4 Batch CALL Detail

| Caller | Callee | Parameter | Purpose |
|--------|--------|-----------|---------|
| CBACT01C | COBDATFT | CODATECN-REC | Date format conversion |
| CBSTM03A | CBSTM03B | WS-M03B-AREA | Statement file I/O delegation (13 calls) |
| COBSWAIT | MVSWAIT | MVSWAIT-TIME | Low-level wait |
| CORPT00C | CSUTLDTC | CSUTLDTC-DATE | Date validation (2 calls) |
| COTRN02C | CSUTLDTC | CSUTLDTC-DATE | Date validation (2 calls) |
| CSUTLDTC | CEEDAYS | *(LE params)* | Language Environment date conversion |
| CBACT01C-CBTRN03C, CBSTM03A, CBEXPORT, CBIMPORT | CEE3ABD | ABCODE, TIMING | Abnormal end handler |

---

## 2. Copybook Dependency Matrix

### 2.1 Which Programs Use Which Copybooks

| Copybook | Online Programs | Batch Programs | Total |
|----------|----------------|----------------|-------|
| **COCOM01Y** (COMMAREA) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | — | **17** |
| **COTTL01Y** (Titles) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | — | **16** |
| **CSDAT01Y** (Date) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | — | **16** |
| **CSMSG01Y** (Messages) | All 17 online programs | — | **17** |
| **CSUSR01Y** (User Sec) | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00-03C | — | **13** |
| **CVACT01Y** (Account) | COACTVWC, COACTUPC, COTRN02C | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C | **9** |
| **CVACT02Y** (Card) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C | **8** |
| **CVACT03Y** (Xref) | COACTVWC, COACTUPC, COTRN02C | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C | **10** |
| **CVCUS01Y** (Customer) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | CBCUS01C, CBEXPORT, CBIMPORT, CBTRN01C | **8** |
| **CVTRA05Y** (Transaction) | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | CBACT04C, CBEXPORT, CBIMPORT, CBTRN02C | **9** |
| **CVTRA06Y** (Daily Tran) | — | CBTRN01C, CBTRN02C | **2** |
| **CVCRD01Y** (Card Work) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | — | **5** |
| **CSMSG02Y** (Abend) | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC | — | **4** |
| **CSSTRPFY** (String) | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | — | **5** |
| **CSSETATY** (Attributes) | COACTUPC (39 COPY REPLACING) | — | **1** |
| **CVTRA01Y** (Cat Balance) | — | CBACT04C, CBTRN02C | **2** |
| **CVTRA02Y** (Disclosure) | — | CBACT04C | **1** |
| **CVTRA03Y** (Tran Type) | — | CBTRN03C | **1** |
| **CVTRA04Y** (Tran Cat) | — | CBTRN03C | **1** |
| **CVTRA07Y** (Report) | — | CBTRN03C | **1** |
| **COSTM01** (Statement) | — | CBSTM03A | **1** |
| **CUSTREC** (Cust Stmt) | — | CBSTM03A | **1** |
| **CVEXPORT** (Export) | — | CBEXPORT, CBIMPORT | **2** |
| **CSLKPCDY** (Lookup) | COACTUPC | — | **1** |
| **CSUTLDPY** (Date Util) | COACTUPC | — | **1** |
| **CSUTLDWY** (Date Edit) | COACTUPC | — | **1** |
| **CODATECN** (Date Conv) | — | CBACT01C | **1** |
| **COADM02Y** (Admin Menu) | COADM01C | — | **1** |
| **COMEN02Y** (User Menu) | COMEN01C | — | **1** |

---

## 3. Data Lineage (File/Dataset Access)

### 3.1 Online CICS Program → VSAM Dataset Access

| Program | Datasets Read | Datasets Written | Operation |
|---------|--------------|-----------------|-----------|
| **COSGN00C** | USRSEC | — | READ (authenticate user) |
| **COMEN01C** | — | — | Menu only (no direct file I/O) |
| **COADM01C** | — | — | Menu only (no direct file I/O) |
| **COACTVWC** | ACCTDAT, CUSTDAT, CXACAIX | — | READ (display account/customer) |
| **COACTUPC** | ACCTDAT, CUSTDAT, CXACAIX | ACCTDAT, CUSTDAT | READ/REWRITE (update account) |
| **COCRDLIC** | CARDDAT, CARDAIX | — | READ/BROWSE (list cards) |
| **COCRDSLC** | CARDDAT, CARDAIX | — | READ (view card detail) |
| **COCRDUPC** | CARDDAT, CARDAIX | CARDDAT | READ/REWRITE (update card) |
| **COTRN00C** | TRANSACT | — | READ/BROWSE (list transactions) |
| **COTRN01C** | TRANSACT | — | READ (view transaction) |
| **COTRN02C** | TRANSACT, ACCTDAT, CXACAIX, CCXREF | TRANSACT | READ/WRITE (add transaction) |
| **COBIL00C** | ACCTDAT, CXACAIX, TRANSACT | ACCTDAT, TRANSACT | READ/WRITE (bill payment) |
| **CORPT00C** | TRANSACT | — | READ (report parameters) |
| **COUSR00C** | USRSEC | — | READ/BROWSE (list users) |
| **COUSR01C** | USRSEC | USRSEC | READ/WRITE (add user) |
| **COUSR02C** | USRSEC | USRSEC | READ/REWRITE (update user) |
| **COUSR03C** | USRSEC | USRSEC | READ/DELETE (delete user) |

### 3.2 Batch Program → File Access

| Program | Input Files | Output Files | Operation |
|---------|------------|-------------|-----------|
| **CBACT01C** | ACCTFILE | OUTFILE, ARRYFILE, VBRCFILE | Read account, write formatted output |
| **CBACT02C** | CARDFILE | *(print)* | Read and print card data |
| **CBACT03C** | XREFFILE | *(print)* | Read and print cross-reference |
| **CBACT04C** | XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TCATBALF, ACCTFILE | Interest calculation, update balances |
| **CBCUS01C** | CUSTFILE | *(print)* | Read and print customer data |
| **CBTRN01C** | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANFILE | Validate and post daily transactions |
| **CBTRN02C** | DALYTRAN, XREFFILE, ACCTFILE | TRANFILE, DALYREJS, TCATBALF | Post transactions, write rejects |
| **CBTRN03C** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT | Generate transaction report |
| **CBSTM03A** | *(via CBSTM03B)* | STMTFILE, HTMLFILE | Generate account statements |
| **CBSTM03B** | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | *(returns to CBSTM03A)* | Read data for statements |
| **CBEXPORT** | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE | Export all data to flat file |
| **CBIMPORT** | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT | Import data from export file |

### 3.3 Data Flow Diagram — Batch Cycle

```
                    Daily Transaction Feed
                           │
                           ▼
                    ┌──────────────┐
                    │  DALYTRAN    │ (Sequential input file)
                    │  (350-byte)  │
                    └──────┬───────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       ┌─────────────┐          ┌─────────────┐
       │  CBTRN01C   │          │  CBTRN02C   │
       │ Validate    │          │ Post        │
       └──────┬──────┘          └──────┬──────┘
              │                        │
              │ Reads:                 │ Reads:
              │  CUSTFILE              │  XREFFILE
              │  XREFFILE              │  ACCTFILE
              │  CARDFILE              │
              │  ACCTFILE              │ Writes:
              │                        │  TRANSACT (master)
              │ Writes:                │  DALYREJS (rejects)
              │  TRANFILE              │  TCATBALF (cat balances)
              │                        │  ACCTFILE (update balance)
              ▼                        ▼
       ┌─────────────┐          ┌─────────────┐
       │  TRANSACT   │          │  TCATBALF   │
       │  (VSAM)     │          │  (VSAM)     │
       └──────┬──────┘          └──────┬──────┘
              │                        │
              │                        ▼
              │                 ┌─────────────┐
              │                 │  CBACT04C   │
              │                 │ Interest    │
              │                 │ Calculation │
              │                 └──────┬──────┘
              │                        │ Reads: XREFFILE, DISCGRP
              │                        │ Updates: ACCTFILE, TCATBALF
              │                        │
              ├────────────────────────┘
              │
              ▼
       ┌─────────────┐         ┌─────────────┐
       │  TRANBKP    │────────▶│  TRANSACT   │
       │  (JCL)      │  REPRO  │  .BKUP(+1)  │
       │  Backup     │         │  (GDG)      │
       └─────────────┘         └─────────────┘
              │
              ▼
       ┌─────────────┐         ┌─────────────┐
       │  CBTRN03C   │────────▶│  TRANREPT   │
       │ Tran Report │         │  (Report)   │
       └─────────────┘         └─────────────┘
              │
              ▼
       ┌─────────────┐         ┌─────────────┐     ┌─────────────┐
       │  CBSTM03A   │────────▶│  STMTFILE   │────▶│  TXT2PDF1   │
       │ Statements  │         │  (.PS)      │     │  (PDF conv) │
       │ + CBSTM03B  │         │  HTMLFILE   │     └─────────────┘
       └─────────────┘         └─────────────┘
```

---

## 4. JCL Job → Program Execution Map

| JCL Job | Programs Executed | Input Datasets | Output Datasets |
|---------|------------------|----------------|-----------------|
| **POSTTRAN** | CBTRN01C, CBTRN02C | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE | TRANSACT, DALYREJS, TCATBALF |
| **INTCALC** | CBACT04C | XREFFILE, ACCTFILE, DISCGRP, TRANSACT | TCATBALF, ACCTFILE |
| **CREASTMT** | SORT, CBSTM03A | TRANSACT.VSAM.KSDS, XREFFILE, ACCTFILE, CUSTFILE | TRXFL.SEQ, TRXFL.VSAM, STMTFILE, HTMLFILE |
| **TRANREPT** | SORT, CBTRN03C | TRANSACT.VSAM.KSDS, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANSACT.DALY(+1), TRANREPT |
| **CBEXPORT** | CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE |
| **CBIMPORT** | CBIMPORT | EXPFILE | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |
| **READACCT** | CBACT01C | ACCTDATA.VSAM.KSDS | Print output |
| **READCARD** | CBACT02C | CARDDATA.VSAM.KSDS | Print output |
| **READCUST** | CBCUS01C | CUSTDATA.VSAM.KSDS | Print output |
| **READXREF** | CBACT03C | CARDXREF.VSAM.KSDS | Print output |
| **WAITSTEP** | COBSWAIT | — | — |
| **ACCTFILE** | IDCAMS | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | IDCAMS | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | IDCAMS | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | IDCAMS | CARDXREF.PS | CARDXREF.VSAM.KSDS |
| **TRANFILE** | IDCAMS | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | IDCAMS | USRSEC.PS | USRSEC.VSAM.KSDS |
| **TRANBKP** | IDCAMS (REPRO) | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) |
| **TRANIDX** | IDCAMS | TRANSACT.VSAM.KSDS | Alternate indexes |
| **CLOSEFIL** | SDSF | — | Close CICS files |
| **OPENFIL** | SDSF | — | Open CICS files |
| **TXT2PDF1** | TXT2PDF | STATEMNT.PS | PDF output |

---

## 5. VSAM Dataset Consumers/Producers

| VSAM Dataset | Online Readers | Online Writers | Batch Readers | Batch Writers | JCL Refreshed By |
|-------------|---------------|---------------|---------------|---------------|-------------------|
| **ACCTDATA** | COACTVWC, COACTUPC, COTRN02C, COBIL00C | COACTUPC, COBIL00C | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03B, CBEXPORT | CBACT04C, CBTRN02C | ACCTFILE.jcl |
| **CARDDATA** | COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC | CBACT02C, CBTRN01C, CBEXPORT | — | CARDFILE.jcl |
| **CARDXREF** | COACTVWC, COACTUPC, COTRN02C | — | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBSTM03A | — | XREFFILE.jcl |
| **CUSTDATA** | COACTVWC, COACTUPC, COCRDSLC | — | CBCUS01C, CBTRN01C, CBEXPORT, CBSTM03B | — | CUSTFILE.jcl |
| **TRANSACT** | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C | COTRN02C, COBIL00C | CBACT04C, CBTRN02C, CBTRN03C, CBEXPORT | CBTRN01C, CBTRN02C | TRANFILE.jcl |
| **USRSEC** | COSGN00C, COUSR00-03C | COUSR01-03C | — | — | DUSRSECJ.jcl |
| **TCATBALF** | — | — | CBACT04C, CBTRN02C | CBACT04C, CBTRN02C | TCATBALF.jcl |
| **DISCGRP** | — | — | CBACT04C | — | DISCGRP.jcl |
| **TRANTYPE** | — | — | CBTRN03C | — | TRANTYPE.jcl |
| **TRANCATG** | — | — | CBTRN03C | — | TRANCATG.jcl |

---

## 6. Menu Configuration → Program Mapping

### 6.1 Regular User Menu (COMEN02Y)

| Option | Label | Program | User Type |
|--------|-------|---------|-----------|
| 01 | Account View | COACTVWC | U (User) |
| 02 | Account Update | COACTUPC | U (User) |
| 03 | Credit Card List | COCRDLIC | U (User) |
| 04 | Transaction List | COTRN00C | U (User) |
| 05 | Transaction Add | COTRN02C | U (User) |
| 06 | Bill Payment | COBIL00C | U (User) |
| 07 | Transaction Report | CORPT00C | U (User) |
| 08 | Authorization Summary | COPAUS0C | U (Optional) |

### 6.2 Admin Menu (COADM02Y)

| Option | Label | Program | User Type |
|--------|-------|---------|-----------|
| 01 | User List | COUSR00C | A (Admin) |
| 02 | User Add | COUSR01C | A (Admin) |
| 03 | User Update | COUSR02C | A (Admin) |
| 04 | User Delete | COUSR03C | A (Admin) |

---

## 7. Modernization Dependency Clusters

Programs that share data and should be modernized together:

### Cluster 1: Account & Card Management (Highest coupling)
- **Programs:** COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC
- **Shared Data:** ACCTDAT, CARDDAT, CARDXREF, CUSTDAT
- **Shared Copybooks:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y

### Cluster 2: Transaction Processing
- **Programs:** COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C
- **Shared Data:** TRANSACT, DALYTRAN, CARDXREF, ACCTDAT, TCATBALF
- **Shared Copybooks:** CVTRA05Y, CVTRA06Y, CVACT03Y

### Cluster 3: Reporting & Statements
- **Programs:** CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B
- **Shared Data:** TRANSACT, CARDXREF, TRANTYPE, TRANCATG
- **Shared Copybooks:** CVTRA05Y, CVTRA07Y, COSTM01

### Cluster 4: User Administration
- **Programs:** COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C
- **Shared Data:** USRSEC
- **Shared Copybooks:** CSUSR01Y

### Cluster 5: Financial Calculations
- **Programs:** CBACT04C, COBIL00C
- **Shared Data:** ACCTDAT, TCATBALF, DISCGRP, TRANSACT
- **Shared Copybooks:** CVACT01Y, CVTRA01Y, CVTRA02Y

### Cluster 6: Data Migration
- **Programs:** CBEXPORT, CBIMPORT
- **Shared Data:** All master files
- **Shared Copybooks:** CVEXPORT, all entity copybooks
