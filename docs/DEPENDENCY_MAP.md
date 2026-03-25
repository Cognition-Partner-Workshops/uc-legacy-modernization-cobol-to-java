# CardDemo Dependency Map

> **Generated**: 2026-03-25 | **Application**: CardDemo (AWS Mainframe Credit Card Management)
> **Source**: Static analysis of COBOL CALL/XCTL/COPY statements, CICS commands, and JCL DD statements

---

## Table of Contents

1. [Program Call Graph](#program-call-graph)
2. [CICS Program Navigation Flow](#cics-program-navigation-flow)
3. [Copybook Dependency Matrix](#copybook-dependency-matrix)
4. [VSAM Data Access Map](#vsam-data-access-map)
5. [JCL Job-to-Program Mapping](#jcl-job-to-program-mapping)
6. [Batch Processing Data Lineage](#batch-processing-data-lineage)
7. [End-to-End Data Flow](#end-to-end-data-flow)

---

## Program Call Graph

### Direct CALL Dependencies

Programs that invoke other programs via COBOL `CALL` or CICS `XCTL`/`LINK` statements.

```
COSGN00C ──XCTL──> COMEN01C (Regular user)
         ──XCTL──> COADM01C (Admin user)

COMEN01C ──XCTL──> COACTVWC    (Option: Account View)
         ──XCTL──> COACTUPC    (Option: Account Update)
         ──XCTL──> COCRDLIC    (Option: Card List)
         ──XCTL──> COTRN00C    (Option: Transaction List)
         ──XCTL──> COTRN02C    (Option: Transaction Add)
         ──XCTL──> CORPT00C    (Option: Reports)
         ──XCTL──> COBIL00C    (Option: Bill Payment)

COADM01C ──XCTL──> COUSR00C    (Option: User List)
         ──XCTL──> COUSR01C    (Option: Add User)
         ──XCTL──> COUSR02C    (Option: Update User)
         ──XCTL──> COUSR03C    (Option: Delete User)

COCRDLIC ──XCTL──> COCRDSLC    (Select card for detail)
         ──XCTL──> COCRDUPC    (Select card for update)

COTRN00C ──XCTL──> COTRN01C    (Select transaction for view)

COTRN02C ──CALL──> CSUTLDTC    (Date validation)
CORPT00C ──CALL──> CSUTLDTC    (Date validation)

CBACT01C ──CALL──> COBDATFT    (Assembler: date formatting)
COBSWAIT ──CALL──> MVSWAIT     (Assembler: system wait)

CBSTM03A ──CALL──> CBSTM03B    (File I/O subroutine, called 13 times)

CSUTLDTC ──CALL──> CEEDAYS     (LE runtime: date conversion)
```

### Abend Handler Calls

Multiple batch programs call `CEE3ABD` (Language Environment abend) for abnormal termination:

```
CBACT01C ──CALL──> CEE3ABD
CBACT02C ──CALL──> CEE3ABD
CBACT03C ──CALL──> CEE3ABD
CBACT04C ──CALL──> CEE3ABD
CBCUS01C ──CALL──> CEE3ABD
CBTRN01C ──CALL──> CEE3ABD
CBTRN02C ──CALL──> CEE3ABD
CBTRN03C ──CALL──> CEE3ABD
CBEXPORT ──CALL──> CEE3ABD
CBIMPORT ──CALL──> CEE3ABD
CBSTM03A ──CALL──> CEE3ABD
```

### Call Graph Summary

```
                          ┌─────────────┐
                          │  COSGN00C   │  (Entry Point - Signon)
                          │   CC00      │
                          └──────┬──────┘
                       ┌─────────┴─────────┐
                       ▼                   ▼
                ┌─────────────┐     ┌─────────────┐
                │  COMEN01C   │     │  COADM01C   │
                │  Main Menu  │     │  Admin Menu  │
                └──────┬──────┘     └──────┬──────┘
          ┌────┬───┬───┼───┬───┬───┐      │
          ▼    ▼   ▼   ▼   ▼   ▼   ▼      ├──> COUSR00C (List Users)
       COACT COACT COCR COTR COTR CORPT COBIL   ├──> COUSR01C (Add User)
       VWC   UPC   DLIC 00C  02C  00C   00C     ├──> COUSR02C (Update User)
                    │                            └──> COUSR03C (Delete User)
              ┌─────┼─────┐
              ▼           ▼
           COCRDSLC    COCRDUPC
           (Card View) (Card Update)

       COTRN00C ──> COTRN01C (Transaction View)

       COTRN02C ──CALL──> CSUTLDTC ──CALL──> CEEDAYS
       CORPT00C ──CALL──> CSUTLDTC

       ┌───────────────────────────────────────────┐
       │            BATCH PROGRAMS                  │
       ├────────────────────────────────────────────┤
       │ CBSTM03A ──CALL──> CBSTM03B (subroutine)  │
       │ CBACT01C ──CALL──> COBDATFT (ASM)          │
       │ COBSWAIT ──CALL──> MVSWAIT  (ASM)          │
       │ All batch ──CALL──> CEE3ABD (abend handler) │
       └────────────────────────────────────────────┘
```

---

## CICS Program Navigation Flow

### Transaction ID to Program Mapping

| CICS Trans ID | Program | Screen | Access |
|--------------|---------|--------|--------|
| CC00 | COSGN00C | Login | All |
| CM00 | COMEN01C | Main Menu | Regular |
| CA00 | COADM01C | Admin Menu | Admin |
| CA01 | COACTVWC | Account View | Regular |
| CA02 | COACTUPC | Account Update | Regular |
| CC01 | COCRDLIC | Card List | Regular |
| CC02 | COCRDSLC | Card View | Regular |
| CC03 | COCRDUPC | Card Update | Regular |
| CT00 | COTRN00C | Transaction List | Regular |
| CT01 | COTRN01C | Transaction View | Regular |
| CT02 | COTRN02C | Transaction Add | Regular |
| CR00 | CORPT00C | Reports | Regular |
| CB00 | COBIL00C | Bill Payment | Regular |
| CU00 | COUSR00C | User List | Admin |
| CU01 | COUSR01C | Add User | Admin |
| CU02 | COUSR02C | Update User | Admin |
| CU03 | COUSR03C | Delete User | Admin |

### Navigation State Machine

All online programs pass navigation context through the CICS COMMAREA (`COCOM01Y`):

```
Fields: CDEMO-FROM-PROGRAM, CDEMO-TO-PROGRAM, CDEMO-FROM-TRANID, CDEMO-TO-TRANID

Login Flow:
  COSGN00C reads USRSEC → validates credentials
    → if Admin: XCTL to COADM01C
    → if Regular: XCTL to COMEN01C

Menu Navigation:
  COMEN01C/COADM01C reads menu option → maps to program name
    → XCTL PROGRAM(CDEMO-TO-PROGRAM)

Return Flow:
  Any program → EXEC CICS RETURN TRANSID(CDEMO-FROM-TRANID) COMMAREA(...)
  PF3 key → Return to previous screen via CDEMO-FROM-PROGRAM
```

### CICS File Operations by Program

| Program | Files Accessed | Operations |
|---------|---------------|------------|
| **COSGN00C** | USRSEC | READ (authentication) |
| **COACTVWC** | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA | READ (3 files joined) |
| **COACTUPC** | ACCTDATA, CUSTDATA | READ (5), REWRITE |
| **COCRDLIC** | CARDDATA | READ (4), STARTBR, READNEXT, ENDBR |
| **COCRDSLC** | CARDDATA, CUSTDATA | READ (2) |
| **COCRDUPC** | CARDDATA | READ (2), REWRITE |
| **COTRN00C** | TRANSACT | READ (2), STARTBR, READNEXT/PREV, ENDBR |
| **COTRN01C** | TRANSACT | READ (1) |
| **COTRN02C** | TRANSACT, ACCTDATA, CARDXREF | READ (3), WRITE (1) |
| **CORPT00C** | TRANSACT | WRITE (report request) |
| **COBIL00C** | TRANSACT, ACCTDATA, CARDXREF | READ (3), WRITE (1), REWRITE (1) |
| **COUSR00C** | USRSEC | READ (2), STARTBR, READNEXT/PREV, ENDBR |
| **COUSR01C** | USRSEC | WRITE (1) |
| **COUSR02C** | USRSEC | READ (1), REWRITE (1) |
| **COUSR03C** | USRSEC | READ (1), DELETE (1) |

---

## Copybook Dependency Matrix

### Which Programs Include Which Copybooks

| Copybook | Type | Programs That COPY It |
|----------|------|----------------------|
| **COCOM01Y** | Common Area | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C (17) |
| **COTTL01Y** | Title/Header | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C (17) |
| **CSDAT01Y** | Date Utility | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C (17) |
| **CSMSG01Y** | Message Area | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C (17) |
| **DFHAID** | CICS AID Keys | All 17 online programs |
| **DFHBMSCA** | BMS Attributes | All 17 online programs |
| **CSUSR01Y** | User Security | COACTUPC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C (11) |
| **CVACT01Y** | Account Master | COACTUPC, COACTVWC, COTRN02C, COBIL00C, CBACT01C, CBACT04C, CBSTM03A, CBEXPORT, CBIMPORT (9) |
| **CVTRA05Y** | Transaction Rec | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT (9) |
| **CVACT03Y** | Card Cross-Ref | COACTUPC, COACTVWC, COTRN02C, COBIL00C, CBACT03C, CBACT04C, CBSTM03A, CBEXPORT, CBIMPORT (9) |
| **CSMSG02Y** | Extended Msg | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CORPT00C, CBTRN03C (6) |
| **CVACT02Y** | Card Master | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT (7) |
| **CVCUS01Y** | Customer Master | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT (8) |
| **CVCRD01Y** | Card Display | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC (5) |
| **CSSTRPFY** | String Functions | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC (5) |
| **CSSETATY** | Attr Setting | COACTUPC (1, but 39 COPY REPLACING instances) |
| **CVTRA06Y** | Daily Trans | CBTRN01C, CBTRN02C (2) |
| **CVTRA01Y** | Cat Balance | CBACT04C, CBTRN02C (2) |
| **CVTRA02Y** | Disclosure | CBACT04C (1) |
| **CVTRA03Y** | Tran Type | CBTRN03C (1) |
| **CVTRA04Y** | Tran Category | CBTRN03C (1) |
| **CVTRA07Y** | Report Layout | CBTRN03C (1) |
| **CVEXPORT** | Export Record | CBEXPORT, CBIMPORT (2) |
| **COSTM01** | Statement Tran | CBSTM03A (1) |
| **CUSTREC** | Customer Alt | CBSTM03A (1) |
| **CODATECN** | Date Conversion | CBACT01C (1) |
| **CSLKPCDY** | Lookup Codes | COACTUPC (1) |
| **CSUTLDPY** | Date Params | COACTUPC (1) |
| **CSUTLDWY** | Date WS | COACTUPC (1) |
| **COADM02Y** | Admin Menu Def | COADM01C (1) |
| **COMEN02Y** | Main Menu Def | COMEN01C (1) |
| **UNUSED1Y** | (Unused) | None (0) |

### BMS Map to Program Mapping

Each online program includes its corresponding BMS-generated copybook:

| Program | BMS Copybook | BMS Map |
|---------|-------------|---------|
| COSGN00C | COSGN00 | COSGN00.bms |
| COMEN01C | COMEN01 | COMEN01.bms |
| COADM01C | COADM01 | COADM01.bms |
| COACTVWC | COACTVW | COACTVW.bms |
| COACTUPC | COACTUP | COACTUP.bms |
| COCRDLIC | COCRDLI | COCRDLI.bms |
| COCRDSLC | COCRDSL | COCRDSL.bms |
| COCRDUPC | COCRDUP | COCRDUP.bms |
| COTRN00C | COTRN00 | COTRN00.bms |
| COTRN01C | COTRN01 | COTRN01.bms |
| COTRN02C | COTRN02 | COTRN02.bms |
| CORPT00C | CORPT00 | CORPT00.bms |
| COBIL00C | COBIL00 | COBIL00.bms |
| COUSR00C | COUSR00 | COUSR00.bms |
| COUSR01C | COUSR01 | COUSR01.bms |
| COUSR02C | COUSR02 | COUSR02.bms |
| COUSR03C | COUSR03 | COUSR03.bms |

---

## VSAM Data Access Map

### Which Programs Read/Write Which VSAM Files

```
VSAM File               │ R=Read  W=Write  U=Update(Rewrite)  D=Delete  B=Browse
─────────────────────────┼──────────────────────────────────────────────────────────
                         │ Online CICS Programs
                         │ SGN MEN ADM ATV AUP CRL CSL CUP TR0 TR1 TR2 RPT BIL US0 US1 US2 US3
ACCTDATA.VSAM.KSDS       │  .   .   .   R   RU  .   .   .   .   .   R   .   R   .   .   .   .
CARDDATA.VSAM.KSDS       │  .   .   .   R   .   RB  R   RU  .   .   .   .   .   .   .   .   .
CARDXREF.VSAM.KSDS       │  .   .   .   R   R   .   .   .   .   .   R   .   R   .   .   .   .
CUSTDATA.VSAM.KSDS       │  .   .   .   R   R   .   R   R   .   .   .   .   .   .   .   .   .
TRANSACT.VSAM.KSDS       │  .   .   .   .   .   .   .   .   RB  R   RW  W   RW  .   .   .   .
USRSEC.VSAM.KSDS         │  R   .   .   .   .   .   .   .   .   .   .   .   .   RB  W   RU  RD

                         │ Batch Programs
                         │ A01 A02 A03 A04 C01 T01 T02 T03 S3A S3B EXP IMP
ACCTDATA.VSAM.KSDS       │  R   .   .   R   .   .   R   .   R   .   R   W
CARDDATA.VSAM.KSDS       │  .   R   .   .   .   .   .   .   .   .   R   W
CARDXREF.VSAM.KSDS       │  .   .   R   R   .   .   R   R   R   .   R   W
CUSTDATA.VSAM.KSDS       │  .   .   .   .   R   .   .   .   R   .   R   W
TRANSACT.VSAM.KSDS       │  .   .   .   .   .   .   RW  .   .   .   R   W
DALYTRAN.PS               │  .   .   .   .   .   R   R   .   .   .   .   .
TCATBALF.VSAM.KSDS       │  .   .   .   RU  .   .   RU  .   .   .   .   .
DISCGRP.VSAM.KSDS        │  .   .   .   R   .   .   .   .   .   .   .   .
TRANTYPE.VSAM.KSDS       │  .   .   .   .   .   .   .   R   .   .   .   .
TRANCATG.VSAM.KSDS       │  .   .   .   .   .   .   .   R   .   .   .   .
TRXFL.VSAM.KSDS          │  .   .   .   .   .   .   .   .   R   R   .   .
EXPORT.DATA               │  .   .   .   .   .   .   .   .   .   .   W   R
```

**Legend**: SGN=COSGN00C, MEN=COMEN01C, ADM=COADM01C, ATV=COACTVWC, AUP=COACTUPC, CRL=COCRDLIC, CSL=COCRDSLC, CUP=COCRDUPC, TR0=COTRN00C, TR1=COTRN01C, TR2=COTRN02C, RPT=CORPT00C, BIL=COBIL00C, US0=COUSR00C, US1=COUSR01C, US2=COUSR02C, US3=COUSR03C, A01=CBACT01C, A02=CBACT02C, A03=CBACT03C, A04=CBACT04C, C01=CBCUS01C, T01=CBTRN01C, T02=CBTRN02C, T03=CBTRN03C, S3A=CBSTM03A, S3B=CBSTM03B, EXP=CBEXPORT, IMP=CBIMPORT

---

## JCL Job-to-Program Mapping

### Jobs That Execute CardDemo Programs

| JCL Job | COBOL Program | Utility Programs | Purpose |
|---------|--------------|------------------|---------|
| **POSTTRAN** | CBTRN02C | — | Daily transaction posting |
| **INTCALC** | CBACT04C | — | Interest calculation |
| **TRANREPT** | CBTRN03C | SORT | Transaction report |
| **CREASTMT** | CBSTM03A (+CBSTM03B) | IDCAMS, SORT, IEFBR14 | Statement generation |
| **READACCT** | CBACT01C | IEFBR14, IDCAMS | Account data dump |
| **READCARD** | CBACT02C | — | Card data dump |
| **READCUST** | CBCUS01C | — | Customer data dump |
| **READXREF** | CBACT03C | — | Cross-reference dump |
| **CBEXPORT** | CBEXPORT | IDCAMS | Data export |
| **CBIMPORT** | CBIMPORT | — | Data import |
| **WAITSTEP** | COBSWAIT (+MVSWAIT) | — | Wait utility |

### Jobs That Use Only System Utilities

| JCL Job | Utilities | Purpose |
|---------|-----------|---------|
| ACCTFILE | IDCAMS | Refresh account VSAM |
| CARDFILE | IDCAMS, SDSF | Refresh card VSAM |
| CUSTFILE | IDCAMS, SDSF | Refresh customer VSAM |
| XREFFILE | IDCAMS | Refresh cross-ref VSAM + alt index |
| TRANFILE | IDCAMS, SDSF | Refresh transaction VSAM |
| DUSRSECJ | IDCAMS, IEBGENER, IEFBR14 | Load user security |
| CLOSEFIL | SDSF | Close CICS files |
| OPENFIL | SDSF | Open CICS files |
| TRANBKP | IDCAMS | Backup transactions |
| COMBTRAN | IDCAMS, SORT | Combine transactions |
| DEFGDGB | IDCAMS | Define GDG bases |
| DEFGDGD | IDCAMS, IEBGENER | Define GDGs + backup ref data |
| DISCGRP | IDCAMS | Load disclosure groups |
| TRANCATG | IDCAMS | Load transaction categories |
| TRANTYPE | IDCAMS | Load transaction types |
| TCATBALF | IDCAMS | Load category balances |
| TRANIDX | IDCAMS | Define transaction alt index |
| PRTCATBL | SORT, IEFBR14 | Print category balance report |
| CBADMCDJ | DFHCSDUP | Load CICS CSD |
| ESDSRRDS | IDCAMS, IEBGENER, IEFBR14 | Create ESDS/RRDS files |
| TXT2PDF1 | IKJEFT1B | Convert text to PDF |
| FTPJCL | FTP | File transfer |
| INTRDRJ1 | IDCAMS, IEBGENER | Internal reader trigger |
| INTRDRJ2 | IDCAMS | Internal reader target |

---

## Batch Processing Data Lineage

### Daily Batch Cycle (Production Order)

```
Step 1: CLOSEFIL
  ├── Action: Close all CICS-managed VSAM files
  └── Enables exclusive batch access

Step 2: Data Refresh (parallel-capable)
  ├── ACCTFILE:  ACCTDATA.PS ──REPRO──> ACCTDATA.VSAM.KSDS
  ├── CARDFILE:  CARDDATA.PS ──REPRO──> CARDDATA.VSAM.KSDS
  ├── CUSTFILE:  CUSTDATA.PS ──REPRO──> CUSTDATA.VSAM.KSDS
  ├── XREFFILE:  CARDXREF.PS ──REPRO──> CARDXREF.VSAM.KSDS (+AIX)
  ├── TRANFILE:  DALYTRAN.PS.INIT ──REPRO──> TRANSACT.VSAM.KSDS
  ├── DISCGRP:   DISCGRP.PS ──REPRO──> DISCGRP.VSAM.KSDS
  ├── TRANCATG:  TRANCATG.PS ──REPRO──> TRANCATG.VSAM.KSDS
  ├── TRANTYPE:  TRANTYPE.PS ──REPRO──> TRANTYPE.VSAM.KSDS
  └── TCATBALF:  TCATBALF.PS ──REPRO──> TCATBALF.VSAM.KSDS

Step 3: POSTTRAN (CBTRN02C)
  ├── Input:  DALYTRAN.PS (daily transactions)
  │           ACCTDATA.VSAM.KSDS (account master)
  │           CARDXREF.VSAM.KSDS (card cross-reference)
  │           TCATBALF.VSAM.KSDS (category balances)
  ├── Output: TRANSACT.VSAM.KSDS (posted transactions)
  │           DALYREJS GDG (+1) (rejected transactions)
  │           ACCTDATA.VSAM.KSDS (updated balances)
  │           TCATBALF.VSAM.KSDS (updated category balances)
  └── Logic:  Validate → Post → Update balances → Reject invalid

Step 4: INTCALC (CBACT04C)
  ├── Input:  ACCTDATA.VSAM.KSDS (account master)
  │           CARDXREF.VSAM.KSDS + AIX.PATH (by account)
  │           DISCGRP.VSAM.KSDS (interest rates)
  │           TCATBALF.VSAM.KSDS (category balances)
  ├── Output: SYSTRAN GDG (+1) (interest transactions)
  │           ACCTDATA.VSAM.KSDS (updated with interest)
  │           TCATBALF.VSAM.KSDS (updated balances)
  └── Logic:  For each account → lookup rate → calculate → post interest

Step 5: TRANBKP
  ├── Input:  TRANSACT.VSAM.KSDS
  ├── Output: TRANSACT.BKUP GDG (+1)
  └── Logic:  REPRO backup to GDG

Step 6: COMBTRAN
  ├── Input:  TRANSACT.BKUP(0) + SYSTRAN(0)
  ├── Output: TRANSACT.COMBINED GDG (+1)
  └── Logic:  SORT + merge interest with regular transactions

Step 7: CREASTMT (CBSTM03A + CBSTM03B)
  ├── Input:  TRANSACT.VSAM.KSDS (via TRXFL re-keyed)
  │           CARDXREF.VSAM.KSDS
  │           ACCTDATA.VSAM.KSDS
  │           CUSTDATA.VSAM.KSDS
  ├── Output: STATEMNT.PS (plain text statements)
  │           STATEMNT.HTML (HTML statements)
  └── Logic:  Re-key by card → join cust/acct data → format statement

Step 8: TRANREPT (CBTRN03C)
  ├── Input:  TRANSACT.VSAM.KSDS
  │           CARDXREF.VSAM.KSDS
  │           TRANTYPE.VSAM.KSDS
  │           TRANCATG.VSAM.KSDS
  │           DATEPARM (date range)
  ├── Output: TRANREPT GDG (+1) (daily report)
  │           TRANSACT.DALY GDG (+1) (daily extract)
  └── Logic:  Extract by date → join types/categories → format report

Step 9: TRANIDX
  ├── Action: Rebuild transaction alternate indexes
  └── Maintenance step

Step 10: OPENFIL
  ├── Action: Reopen all CICS-managed VSAM files
  └── Restores online access
```

### Data Flow Diagram (Entity-Centric)

```
                    DALYTRAN.PS (Input)
                         │
                    ┌────▼────┐
                    │POSTTRAN │ (CBTRN02C)
                    │ Step 3  │
                    └────┬────┘
                         │ Posts to
            ┌────────────▼────────────┐
            │  TRANSACT.VSAM.KSDS     │◄──── Online: COTRN02C (add)
            │  (Master Transaction)   │◄──── Online: COBIL00C (payment)
            └────────────┬────────────┘
                    ┌────┤
                    │    ├── TRANBKP ──> TRANSACT.BKUP GDG
                    │    │                    │
                    │    │              COMBTRAN (+ SYSTRAN)
                    │    │                    │
                    │    │              TRANSACT.COMBINED GDG
                    │    │
                    │    ├── CREASTMT ──> STATEMNT.PS / STATEMNT.HTML
                    │    │                    │
                    │    │              TXT2PDF1 ──> STATEMNT.PS.PDF
                    │    │
                    │    └── TRANREPT ──> TRANREPT GDG + TRANSACT.DALY GDG
                    │
         ┌──────────▼──────────┐
         │ ACCTDATA.VSAM.KSDS  │◄──── Online: COACTUPC (update)
         │ (Account Master)    │◄──── INTCALC (interest updates)
         └─────────────────────┘
                    │
         ┌──────────▼──────────┐
         │CARDXREF.VSAM.KSDS   │ (Bridge: Card → Account → Customer)
         └──────────┬──────────┘
                    │
         ┌──────────▼──────────┐
         │ CUSTDATA.VSAM.KSDS  │ (Customer details for statements)
         └─────────────────────┘
```

---

## End-to-End Data Flow

### Online Transaction Entry → Batch Processing → Reports

```
1. USER LOGIN
   User → COSGN00C → reads USRSEC.VSAM → validates → routes to menu

2. TRANSACTION ENTRY (Online)
   User → COTRN02C → validates card via CARDXREF → writes TRANSACT.VSAM

3. BILL PAYMENT (Online)
   User → COBIL00C → reads CARDXREF + ACCTDATA → writes TRANSACT.VSAM
                    → updates ACCTDATA balance

4. DAILY BATCH POSTING
   DALYTRAN.PS → CBTRN02C → posts to TRANSACT.VSAM
                           → updates ACCTDATA (balances)
                           → updates TCATBALF (category totals)
                           → writes DALYREJS GDG (rejects)

5. INTEREST CALCULATION
   ACCTDATA + DISCGRP + TCATBALF → CBACT04C → SYSTRAN GDG (interest entries)
                                             → updates ACCTDATA
                                             → updates TCATBALF

6. STATEMENT GENERATION
   TRANSACT + CARDXREF + ACCTDATA + CUSTDATA → CBSTM03A/B
     → STATEMNT.PS (text) + STATEMNT.HTML (web)

7. REPORTING
   TRANSACT + CARDXREF + TRANTYPE + TRANCATG → CBTRN03C → TRANREPT GDG

8. DATA EXPORT
   All VSAM files → CBEXPORT → EXPORT.DATA (flat file for migration)
```

### Shared Data Dependencies (Risk Areas)

| VSAM File | Writers | Readers | Contention Risk |
|-----------|---------|---------|-----------------|
| **TRANSACT.VSAM.KSDS** | COTRN02C, COBIL00C, CBTRN02C | COTRN00C, COTRN01C, CORPT00C, CBTRN03C, CBSTM03A, CBEXPORT | **HIGH** — busiest file |
| **ACCTDATA.VSAM.KSDS** | COACTUPC, CBTRN02C, CBACT04C, CBIMPORT | COACTVWC, COBIL00C, COTRN02C, CBACT01C, CBSTM03A, CBEXPORT | **HIGH** — multiple updaters |
| **TCATBALF.VSAM.KSDS** | CBTRN02C, CBACT04C | CBTRN02C, CBACT04C | **MEDIUM** — batch-only |
| **USRSEC.VSAM.KSDS** | COUSR01C, COUSR02C, COUSR03C | COSGN00C, COUSR00C | LOW |
| **CARDDATA.VSAM.KSDS** | COCRDUPC, CBIMPORT | COCRDLIC, COCRDSLC, COACTVWC, CBACT02C, CBEXPORT | LOW |
| **CARDXREF.VSAM.KSDS** | CBIMPORT | Many readers (bridge table) | LOW |
| **CUSTDATA.VSAM.KSDS** | CBIMPORT | COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT | LOW |
