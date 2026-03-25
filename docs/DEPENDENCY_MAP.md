# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Source**: Static analysis of CALL, EXEC CICS XCTL/LINK, COPY, and JCL DD statements

---

## 1. Program-to-Program Call Graph

### 1.1 Online CICS Navigation Flow

```
                          ┌──────────────┐
                          │  COSGN00C    │ (Login - CC00)
                          │  Sign-on     │
                          └──────┬───────┘
                                 │
                    ┌────────────┴────────────┐
                    │ (Admin user)            │ (Regular user)
                    ▼                         ▼
            ┌──────────────┐          ┌──────────────┐
            │  COADM01C    │          │  COMEN01C    │
            │  Admin Menu  │          │  Main Menu   │
            └──────┬───────┘          └──────┬───────┘
                   │                         │
     ┌─────────────┼─────────────┐           │
     │             │             │           │
     ▼             ▼             ▼           │
 ┌────────┐  ┌────────┐  ┌────────┐        │
 │COUSR00C│  │COUSR01C│  │COUSR02C│        │
 │List Usr│  │Add Usr │  │Upd Usr │        │
 └───┬────┘  └────────┘  └────────┘        │
     │                                      │
     ▼                                      │
 ┌────────┐                                 │
 │COUSR03C│                                 │
 │Del Usr │                                 │
 └────────┘                                 │
                                            │
     ┌──────────────────────────────────────┤
     │         │          │         │       │
     ▼         ▼          ▼         ▼       ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│COACTVWC││COCRDLIC││COTRN00C││CORPT00C││COBIL00C│
│Acct Vw ││Card Lst││Trn Lst ││Reports ││BillPay │
└───┬────┘└───┬────┘└───┬────┘└────────┘└────────┘
    │         │         │
    ▼         │         ▼
┌────────┐   │    ┌────────┐
│COACTUPC│   │    │COTRN01C│
│Acct Upd│   │    │Trn View│
└────────┘   │    └────────┘
             │         │
    ┌────────┴──┐      ▼
    │           │ ┌────────┐
    ▼           ▼ │COTRN02C│
┌────────┐┌────────┐│Trn Add │
│COCRDSLC││COCRDUPC│└────────┘
│Card Vw ││Card Upd│
└────────┘└────────┘
```

### 1.2 CICS Transfer Mechanism

All online program-to-program transfers use `EXEC CICS XCTL` (transfer control, no return) via the COMMAREA. The COMMAREA (`COCOM01Y`) carries:
- Current/previous program names
- User session context
- Selected entity IDs (account, card, transaction)

| Source Program | Target Program | Transfer Type | Trigger |
|---------------|---------------|---------------|---------|
| COSGN00C | COMEN01C | XCTL | Successful login (regular user) |
| COSGN00C | COADM01C | XCTL | Successful login (admin user) |
| COMEN01C | COACTVWC | XCTL | Menu option: Account View |
| COMEN01C | COCRDLIC | XCTL | Menu option: Card List |
| COMEN01C | COTRN00C | XCTL | Menu option: Transaction List |
| COMEN01C | CORPT00C | XCTL | Menu option: Reports |
| COMEN01C | COBIL00C | XCTL | Menu option: Bill Payment |
| COADM01C | COUSR00C | XCTL | Menu option: User List |
| COADM01C | COUSR01C | XCTL | Menu option: Add User |
| COADM01C | COUSR02C | XCTL | Menu option: Update User |
| COUSR00C | COUSR03C | XCTL | Select user for deletion |
| COACTVWC | COACTUPC | XCTL | Request account update |
| COCRDLIC | COCRDSLC | XCTL | Select card for detail view |
| COCRDLIC | COCRDUPC | XCTL | Select card for update |
| COTRN00C | COTRN01C | XCTL | Select transaction for view |
| COTRN00C | COTRN02C | XCTL | Request new transaction |
| Any program | COSGN00C | XCTL | PF3 from menu / sign-off |

### 1.3 Batch Program Call Graph

```
CBSTM03A ──CALL──► CBSTM03B  (Statement file I/O subroutine)
CBACT01C ──CALL──► COBDATFT  (Assembler date formatting)
COBSWAIT ──CALL──► MVSWAIT   (Assembler wait utility)
CSUTLDTC ──CALL──► CEEDAYS   (LE date API)
CBACT02C ──CALL──► CEE3ABD   (LE abnormal end)
CBACT03C ──CALL──► CEE3ABD   (LE abnormal end)
CBCUS01C ──CALL──► CEE3ABD   (LE abnormal end)
```

| Caller | Callee | Mechanism | Purpose |
|--------|--------|-----------|---------|
| **CBSTM03A** | **CBSTM03B** | `CALL 'CBSTM03B'` | Delegate file open/read/write/close for statements |
| **CBACT01C** | **COBDATFT** (ASM) | `CALL 'COBDATFT'` | Format date fields on account records |
| **COBSWAIT** | **MVSWAIT** (ASM) | `CALL 'MVSWAIT'` | System-level wait for specified centiseconds |
| **CSUTLDTC** | **CEEDAYS** (LE) | `CALL 'CEEDAYS'` | Validate/convert date using Language Environment |
| **CBACT02C** | **CEE3ABD** (LE) | `CALL 'CEE3ABD'` | Abend with reason code on fatal error |
| **CBACT03C** | **CEE3ABD** (LE) | `CALL 'CEE3ABD'` | Abend with reason code on fatal error |
| **CBCUS01C** | **CEE3ABD** (LE) | `CALL 'CEE3ABD'` | Abend with reason code on fatal error |

### 1.4 Optional Module Calls

```
COPAUA0C ──MQ GET──► Request Queue ──MQ PUT──► Reply Queue
         ──IMS DLI──► Authorization IMS DB

COPAUS1C ──EXEC CICS LINK──► COPAUS2C  (Mark fraud)
COPAUS2C ──EXEC SQL──► DB2 (Fraud table)

COACCT01 ──CALL──► MQOPEN, MQGET, MQPUT (MQ API)
CODATE01 ──CALL──► MQOPEN, MQGET, MQPUT (MQ API)

COTRTLIC ──EXEC SQL──► DB2 (TRAN_TYPE table, cursor-based)
COTRTUPC ──EXEC SQL──► DB2 (TRAN_TYPE, TRAN_CAT tables)
COBTUPDT ──EXEC SQL──► DB2 (TRAN_TYPE table, batch update)
```

---

## 2. Copybook Inclusion Map

### 2.1 Program-to-Copybook Matrix (Core Module)

| Copybook | Used By Programs | Category |
|----------|-----------------|----------|
| **COCOM01Y** | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | COMMAREA |
| **COTTL01Y** | All 17 online CICS programs | Screen header |
| **CSDAT01Y** | All 17 online CICS programs | Date fields |
| **CSMSG01Y** | All 17 online CICS programs | Messages |
| **CSUSR01Y** | All 17 online CICS programs | User security |
| **DFHAID** | All 17 online CICS programs | AID key defs |
| **DFHBMSCA** | All 17 online CICS programs | BMS attributes |
| **CVACT01Y** | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, COACTUPC, COACTVWC, COBIL00C | Account record |
| **CVACT02Y** | CBACT02C, CBEXPORT, CBIMPORT, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Card record |
| **CVACT03Y** | CBACT03C, CBACT04C, CBEXPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C | Cross-ref |
| **CVCUS01Y** | CBCUS01C, CBEXPORT, CBIMPORT, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC | Customer |
| **CVTRA05Y** | CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN02C | Transaction |
| **CVTRA06Y** | CBTRN01C, CBTRN02C | Daily tran |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Cat balance |
| **CVTRA02Y** | CBACT04C | Disc group |
| **CVTRA03Y** | CBTRN03C | Tran type |
| **CVTRA04Y** | CBTRN03C | Tran category |
| **CVTRA07Y** | CBTRN03C | Report layout |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export layout |
| **CVCRD01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Card internal |
| **COSTM01** | CBSTM03A | Statement tran |
| **CUSTREC** | CBSTM03A | Customer (alt) |
| **CODATECN** | CBACT01C | Date conversion |
| **COMEN02Y** | COMEN01C | Menu defs |
| **COADM02Y** | COADM01C | Admin menu defs |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COTRN01C, COTRN02C | Multi-message |
| **CSLKPCDY** | COACTUPC | Lookup codes |
| **CSUTLDWY** | COACTUPC, COCRDUPC | Date utility WS |

---

## 3. JCL Job Data Lineage

### 3.1 Batch Processing Cycle

The nightly batch cycle processes in this order:

```
Step 1: CLOSEFIL  ──► Close CICS files for exclusive batch access
          │
Step 2: ACCTFILE  ──► Refresh Account VSAM from flat file
        CARDFILE  ──► Refresh Card VSAM
        CUSTFILE  ──► Refresh Customer VSAM
        XREFFILE  ──► Refresh Cross-Reference VSAM
        TRANFILE  ──► Refresh Transaction VSAM
          │
Step 3: POSTTRAN  ──► Post daily transactions (CBTRN02C)
          │
Step 4: INTCALC   ──► Calculate interest (CBACT04C)
          │
Step 5: TRANBKP   ──► Backup transaction VSAM to GDG
          │
Step 6: COMBTRAN  ──► Combine backed-up + system transactions
          │
Step 7: CREASTMT  ──► Generate account statements (CBSTM03A)
          │
Step 8: TRANREPT  ──► Generate transaction report (CBTRN03C)
          │
Step 9: TRANIDX   ──► Rebuild alternate indexes
          │
Step 10: OPENFIL  ──► Re-open CICS files for online access
```

### 3.2 Job-to-Dataset Read/Write Matrix

| JCL Job | Program | Reads (Input) | Writes (Output) |
|---------|---------|---------------|-----------------|
| **POSTTRAN** | CBTRN02C | DALYTRAN.PS, CARDXREF.VSAM, ACCTDATA.VSAM, TCATBALF.VSAM | TRANSACT.VSAM, DALYREJS GDG |
| **INTCALC** | CBACT04C | TCATBALF.VSAM, CARDXREF.VSAM, ACCTDATA.VSAM, DISCGRP.VSAM | SYSTRAN GDG, ACCTDATA.VSAM (update) |
| **CREASTMT** | CBSTM03A | TRANSACT.VSAM, CARDXREF.VSAM, CUSTDATA.VSAM, ACCTDATA.VSAM | STATEMNT.HTML, STATEMNT.PS |
| **TRANREPT** | CBTRN03C | TRANSACT.BKUP GDG, CARDXREF.VSAM, TRANTYPE.VSAM, TRANCATG.VSAM | TRANSACT.DALY GDG (sorted) |
| **TRANBKP** | REPROC+IDCAMS | TRANSACT.VSAM | TRANSACT.BKUP GDG |
| **COMBTRAN** | SORT+IDCAMS | TRANSACT.BKUP GDG, SYSTRAN GDG | TRANSACT.COMBINED GDG, TRANSACT.VSAM |
| **PRTCATBL** | REPROC+SORT | TCATBALF.VSAM | TCATBALF.REPT, TCATBALF.BKUP GDG |
| **CBEXPORT** | CBEXPORT | CUSTDATA.VSAM, ACCTDATA.VSAM, CARDXREF.VSAM, TRANSACT.VSAM, CARDDATA.VSAM | EXPORT.DATA |
| **CBIMPORT** | CBIMPORT | EXPORT.DATA | CUSTDATA.IMPORT, ACCTDATA.IMPORT, CARDXREF.IMPORT, TRANSACT.IMPORT, IMPORT.ERRORS |
| **ACCTFILE** | IDCAMS | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | IDCAMS | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | IDCAMS | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| **XREFFILE** | IDCAMS | CARDXREF.PS | CARDXREF.VSAM.KSDS |
| **TRANFILE** | IDCAMS | DALYTRAN.PS.INIT | TRANSACT.VSAM.KSDS |
| **DUSRSECJ** | IDCAMS | USRSEC.PS (inline data) | USRSEC.VSAM.KSDS |
| **DISCGRP** | IDCAMS | DISCGRP.PS | DISCGRP.VSAM.KSDS |
| **TRANTYPE** | IDCAMS | TRANTYPE.PS | TRANTYPE.VSAM.KSDS |
| **TRANCATG** | IDCAMS | TRANCATG.PS | TRANCATG.VSAM.KSDS |
| **TCATBALF** | IDCAMS | TCATBALF.PS | TCATBALF.VSAM.KSDS |
| **TXT2PDF1** | IKJEFT1B | STATEMNT.PS | PDF output |
| **READACCT** | CBACT01C | ACCTDATA.VSAM | SYSOUT (print) |
| **READCARD** | CBACT02C | CARDDATA.VSAM | SYSOUT (print) |
| **READCUST** | CBCUS01C | CUSTDATA.VSAM | SYSOUT (print) |
| **READXREF** | CBACT03C | CARDXREF.VSAM | SYSOUT (print) |
| **WAITSTEP** | COBSWAIT | SYSIN (parm) | — |
| **DEFGDGB** | IDCAMS | — | GDG base definitions |
| **DEFGDGD** | IDCAMS+IEBGENER | TRANTYPE.PS, TRANCATG.PS, DISCGRP.PS | GDG backup entries |

### 3.3 VSAM Dataset Usage Summary

| VSAM Dataset | Online Programs (Read) | Online Programs (Write) | Batch Jobs (Read) | Batch Jobs (Write) |
|---|---|---|---|---|
| **ACCTDATA.VSAM.KSDS** | COACTVWC, COACTUPC, COBIL00C | COACTUPC | INTCALC, CREASTMT, POSTTRAN, CBEXPORT, READACCT | ACCTFILE, INTCALC |
| **CARDDATA.VSAM.KSDS** | COCRDLIC, COCRDSLC, COCRDUPC | COCRDUPC | CBEXPORT, READCARD | CARDFILE |
| **CARDXREF.VSAM.KSDS** | COACTVWC, COTRN00C, COBIL00C | — | POSTTRAN, INTCALC, CREASTMT, TRANREPT, CBEXPORT, READXREF | XREFFILE |
| **CUSTDATA.VSAM.KSDS** | COACTVWC, COCRDSLC, COCRDUPC | — | CREASTMT, CBEXPORT, READCUST | CUSTFILE |
| **TRANSACT.VSAM.KSDS** | COTRN00C, COTRN01C, COBIL00C | COTRN02C, COBIL00C | CREASTMT, CBEXPORT | POSTTRAN, COMBTRAN, TRANFILE |
| **USRSEC.VSAM.KSDS** | COSGN00C, COUSR00C-03C | COUSR01C, COUSR02C, COUSR03C | — | DUSRSECJ |
| **TCATBALF.VSAM.KSDS** | — | — | INTCALC, POSTTRAN, PRTCATBL | TCATBALF, POSTTRAN |
| **DISCGRP.VSAM.KSDS** | — | — | INTCALC | DISCGRP |
| **TRANTYPE.VSAM.KSDS** | — | — | TRANREPT | TRANTYPE |
| **TRANCATG.VSAM.KSDS** | — | — | TRANREPT | TRANCATG |

---

## 4. Online-to-Batch Interface

| Online Program | Batch Trigger | Mechanism | Description |
|---|---|---|---|
| **CORPT00C** | TRANREPT job | Extra-partition TDQ (Internal Reader) | User requests report → JCL submitted to JES2 |
| **COTRN02C** | Feeds POSTTRAN | VSAM write to TRANSACT | Online transactions written to TRANSACT, processed in nightly batch |
| **COBIL00C** | Feeds POSTTRAN | VSAM write to TRANSACT | Bill payments create transaction records |

---

## 5. Cross-Module Dependencies (Optional Modules)

### Authorization Module → Core

| Auth Program | Core Dependency | Direction |
|---|---|---|
| COPAUA0C | CVACT03Y (Card XREF) | Reads card-to-account mapping |
| COPAUS0C | CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y | Reads account, card, xref, customer |
| COPAUS0C | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y | Uses common screen infrastructure |
| COPAUS1C | Same as COPAUS0C | Same |
| COPAUS2C | CIPAUDTY | Reads auth detail, writes to DB2 |

### Transaction Type DB2 Module → Core

| DB2 Program | Core Dependency | Direction |
|---|---|---|
| COTRTLIC | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | Uses common screen infrastructure |
| COTRTUPC | Same + CSMSG02Y | Same |
| COBTUPDT | DB2 TRAN_TYPE table | Batch update |

### VSAM-MQ Module → Core

| MQ Program | Core Dependency | Direction |
|---|---|---|
| COACCT01 | CVACT01Y | Reads account data to respond to MQ inquiry |
| CODATE01 | (none) | Returns system date via MQ |

---

## 6. Shared Infrastructure Dependencies

```
┌─────────────────────────────────────────────────────────────────┐
│                    Shared Copybooks Layer                        │
│  COCOM01Y │ COTTL01Y │ CSDAT01Y │ CSMSG01Y │ CSUSR01Y         │
│  (COMMAREA) (Header)  (Date)     (Messages)  (User Security)   │
├─────────────────────────────────────────────────────────────────┤
│              All 17 Online CICS Programs                        │
├─────────────────────────────────────────────────────────────────┤
│                    CICS System Copybooks                        │
│  DFHAID (AID keys) │ DFHBMSCA (BMS attributes)                 │
├─────────────────────────────────────────────────────────────────┤
│              BMS Maps ←→ BMS-Generated Copybooks                │
│  COSGN00 ←→ COSGN00.CPY │ COMEN01 ←→ COMEN01.CPY │ etc.      │
└─────────────────────────────────────────────────────────────────┘
```

Every online CICS program depends on:
1. **COCOM01Y** — Communication area for inter-program data passing
2. **COTTL01Y** — Standard screen header/title layout
3. **CSDAT01Y** — Date formatting fields
4. **CSMSG01Y** — Message display area
5. **CSUSR01Y** — Current user security context
6. **DFHAID** — CICS attention identifier bytes (PF keys)
7. **DFHBMSCA** — BMS screen attribute constants
8. One or more **BMS-generated copybooks** for screen field access
