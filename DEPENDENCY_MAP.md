# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Source:** `uc-legacy-modernization-cobol-to-java`
> **Application:** CardDemo -- Mainframe Credit Card Management System

---

## 1. Program Call Graph

### 1.1 Online CICS Program Flow (XCTL / LINK)

```
                              COSGN00C (Sign-On)
                                   |
                      ┌────────────┴────────────┐
                      v                         v
                COMEN01C (Main Menu)      COADM01C (Admin Menu)
                      |                         |
       ┌──────┬───────┼───────┬──────┐    ┌─────┼─────┬─────┬─────┐
       v      v       v       v      v    v     v     v     v     v
   COACTVWC COACTUPC COCRDLIC COTRN00C COBIL00C COUSR00C COUSR01C COUSR02C COUSR03C
   (Acct    (Acct    (Card    (Tran   (Bill    (User   (User  (User  (User
    View)    Update)  List)    List)   Pay)     List)   Add)   Upd)   Del)
                        |       |
                   ┌────┤  ┌────┤
                   v    v  v    v
              COCRDSLC  COTRN01C COTRN02C      CORPT00C
              (Card     (Tran   (Tran          (Reports)
               View)    View)    Add)              |
                   |                               v
                   v                          CSUTLDTC
              COCRDUPC                        (Date Util)
              (Card Update)
```

### 1.2 Online XCTL (Transfer Control) Calls

| Source Program | Target Program | Trigger                         |
|----------------|----------------|---------------------------------|
| COSGN00C       | COMEN01C       | Successful user login           |
| COSGN00C       | COADM01C       | Successful admin login          |
| COMEN01C       | COACTVWC       | Menu option 1 -- Account View   |
| COMEN01C       | COACTUPC       | Menu option 2 -- Account Update |
| COMEN01C       | COCRDLIC       | Menu option 3 -- Card List      |
| COMEN01C       | COCRDSLC       | Menu option 4 -- Card View      |
| COMEN01C       | COCRDUPC       | Menu option 5 -- Card Update    |
| COMEN01C       | COTRN00C       | Menu option 6 -- Transaction List |
| COMEN01C       | COTRN01C       | Menu option 7 -- Transaction View |
| COMEN01C       | COTRN02C       | Menu option 8 -- Transaction Add |
| COMEN01C       | CORPT00C       | Menu option 9 -- Reports        |
| COMEN01C       | COBIL00C       | Menu option 10 -- Bill Payment  |
| COMEN01C       | COPAUS0C       | Menu option 11 -- Pending Auth  |
| COADM01C       | COUSR00C       | Admin option 1 -- User List     |
| COADM01C       | COUSR01C       | Admin option 2 -- User Add      |
| COADM01C       | COUSR02C       | Admin option 3 -- User Update   |
| COADM01C       | COUSR03C       | Admin option 4 -- User Delete   |
| COADM01C       | COTRTLIC       | Admin option 5 -- Tran Type List (DB2) |
| COADM01C       | COTRTUPC       | Admin option 6 -- Tran Type Maint (DB2) |
| COCRDLIC       | COCRDSLC       | Select card for viewing         |
| COCRDLIC       | COCRDUPC       | Select card for update          |
| COCRDLIC       | COMEN01C       | PF3 -- Return to menu           |
| COCRDSLC       | COMEN01C       | PF3 -- Return to menu           |
| COCRDUPC       | COMEN01C       | PF3 -- Return to menu           |
| COACTVWC       | COMEN01C       | PF3 -- Return to menu           |
| COACTUPC       | COMEN01C       | PF3 -- Return to menu           |

### 1.3 Batch Program CALL Graph

```
CBSTM03A (Statement Gen Main)
    |
    └──CALL──> CBSTM03B (Statement Gen I/O Subroutine)
    └──CALL──> CEE3ABD  (LE Abnormal End)

CBACT01C (Account Reader)
    └──CALL──> COBDATFT (ASM Date Format)
    └──CALL──> CEE3ABD

CORPT00C (Online Report Request)
    └──CALL──> CSUTLDTC (Date Conversion)

COTRN02C (Online Transaction Add)
    └──CALL──> CSUTLDTC (Date Conversion)

CSUTLDTC (Date Conversion Utility)
    └──CALL──> CEEDAYS  (LE Date Intrinsic)

COBSWAIT (Wait Utility)
    └──CALL──> MVSWAIT  (ASM Wait Routine)

CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBACT02C, CBACT03C,
CBCUS01C, CBEXPORT, CBIMPORT
    └──CALL──> CEE3ABD  (LE Abnormal End -- error handling)
```

### 1.4 Batch CALL Details

| Caller Program | Called Program/Routine | Purpose                          |
|----------------|----------------------|----------------------------------|
| CBSTM03A       | CBSTM03B             | File I/O for statement generation|
| CBSTM03A       | CEE3ABD              | Abnormal termination handler     |
| CBACT01C       | COBDATFT             | Date formatting (ASM)            |
| CBACT01C       | CEE3ABD              | Abnormal termination handler     |
| CORPT00C       | CSUTLDTC             | Date conversion for report dates |
| COTRN02C       | CSUTLDTC             | Date conversion for transaction  |
| CSUTLDTC       | CEEDAYS              | LE date conversion service       |
| COBSWAIT       | MVSWAIT              | ASM wait (timed delay)           |
| CBACT02C       | CEE3ABD              | Abnormal termination handler     |
| CBACT03C       | CEE3ABD              | Abnormal termination handler     |
| CBACT04C       | CEE3ABD              | Abnormal termination handler     |
| CBCUS01C       | CEE3ABD              | Abnormal termination handler     |
| CBTRN01C       | CEE3ABD              | Abnormal termination handler     |
| CBTRN02C       | CEE3ABD              | Abnormal termination handler     |
| CBTRN03C       | CEE3ABD              | Abnormal termination handler     |
| CBEXPORT       | CEE3ABD              | Abnormal termination handler     |
| CBIMPORT       | CEE3ABD              | Abnormal termination handler     |

### 1.5 Optional Module Call Graph

```
Authorization Module (IMS/DB2/MQ):
    COPAUA0C ──MQ CALL──> MQOPEN, MQGET, MQPUT1, MQCLOSE
    COPAUA0C ──CICS READ──> CARDXREF, ACCTDATA, CUSTDATA
    COPAUS0C ──CICS XCTL──> COPAUS1C (detail screen)
    COPAUS1C ──CICS LINK──> COPAUS2C (fraud marking)
    PAUDBLOD ──IMS CALL──> CBLTDLI (DL/I Insert/GU)
    PAUDBUNL ──IMS CALL──> CBLTDLI (DL/I GN/GNP)
    DBUNLDGS ──IMS CALL──> CBLTDLI (DL/I GN/GNP + ISRT to GSAM)

Transaction Type DB2 Module:
    COTRTLIC ──EXEC SQL──> SELECT, INSERT, UPDATE, DELETE on TRTYP table
    COTRTUPC ──EXEC SQL──> SELECT, INSERT, UPDATE, DELETE on TRTYP + TRCAT
    COBTUPDT ──EXEC SQL──> INSERT (batch load from flat file)

VSAM-MQ Module:
    COACCT01 ──MQ CALL──> MQOPEN, MQGET, MQPUT, MQCLOSE
    COACCT01 ──CICS READ──> ACCTDATA.VSAM.KSDS
    CODATE01 ──MQ CALL──> MQOPEN, MQGET, MQPUT, MQCLOSE
    CODATE01 ──CICS ASKTIME/FORMATTIME──> System date
```

---

## 2. Copybook Dependency Matrix

### 2.1 Which Programs COPY Which Copybooks

| Copybook    | Online Programs Using It                                          | Batch Programs Using It              |
|-------------|-------------------------------------------------------------------|--------------------------------------|
| COCOM01Y    | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | -- |
| COTTL01Y    | COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | -- |
| CSDAT01Y    | All 17 online programs                                            | --                                   |
| CSMSG01Y    | All 17 online programs                                            | --                                   |
| CSUSR01Y    | COSGN00C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C-03C | --                           |
| CSMSG02Y    | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                           | --                                   |
| DFHAID      | All 17 online programs                                            | --                                   |
| DFHBMSCA    | All 17 online programs                                            | --                                   |
| CVCRD01Y    | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC                 | --                                   |
| CVACT01Y    | COACTVWC, COACTUPC, COTRN02C, COBIL00C                           | CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| CVACT02Y    | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC                           | CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT |
| CVACT03Y    | COACTVWC, COACTUPC, COTRN02C, COBIL00C                           | CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A |
| CVCUS01Y    | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC                           | CBCUS01C, CBTRN01C, CBEXPORT, CBIMPORT, CBSTM03A |
| CVTRA05Y    | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C                | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT |
| CVTRA06Y    | --                                                                | CBTRN01C, CBTRN02C                   |
| CVTRA01Y    | --                                                                | CBACT04C, CBTRN02C                   |
| CVTRA02Y    | --                                                                | CBACT04C                             |
| CVTRA03Y    | --                                                                | CBTRN03C                             |
| CVTRA04Y    | --                                                                | CBTRN03C                             |
| CVTRA07Y    | --                                                                | CBTRN03C                             |
| CVEXPORT    | --                                                                | CBEXPORT, CBIMPORT                   |
| COSTM01     | --                                                                | CBSTM03A                             |
| CUSTREC     | --                                                                | CBSTM03A                             |
| COMEN02Y    | COMEN01C                                                          | --                                   |
| COADM02Y    | COADM01C                                                          | --                                   |
| CSLKPCDY    | COACTUPC                                                          | --                                   |
| CSSETATY    | COACTUPC (x38 REPLACING)                                          | --                                   |
| CSSTRPFY    | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC                 | --                                   |
| CSUTLDPY    | COACTUPC                                                          | --                                   |
| CSUTLDWY    | COACTUPC, COTRN02C*                                               | --                                   |
| CODATECN    | --                                                                | CBACT01C                             |

### 2.2 BMS Map to Program Mapping

| BMS Map    | Copybook (cpy-bms) | Program     |
|------------|---------------------|-------------|
| COSGN00    | COSGN00.CPY         | COSGN00C    |
| COMEN01    | COMEN01.CPY         | COMEN01C    |
| COADM01    | COADM01.CPY         | COADM01C    |
| COACTVW    | COACTVW.CPY         | COACTVWC    |
| COACTUP    | COACTUP.CPY         | COACTUPC    |
| COCRDLI    | COCRDLI.CPY         | COCRDLIC    |
| COCRDSL    | COCRDSL.CPY         | COCRDSLC    |
| COCRDUP    | COCRDUP.CPY         | COCRDUPC    |
| COTRN00    | COTRN00.CPY         | COTRN00C    |
| COTRN01    | COTRN01.CPY         | COTRN01C    |
| COTRN02    | COTRN02.CPY         | COTRN02C    |
| CORPT00    | CORPT00.CPY         | CORPT00C    |
| COBIL00    | COBIL00.CPY         | COBIL00C    |
| COUSR00    | COUSR00.CPY         | COUSR00C    |
| COUSR01    | COUSR01.CPY         | COUSR01C    |
| COUSR02    | COUSR02.CPY         | COUSR02C    |
| COUSR03    | COUSR03.CPY         | COUSR03C    |

---

## 3. Data Lineage -- VSAM File Access by Program

### 3.1 Online Programs -- CICS File Access

| VSAM File (DD Name)    | READ                                          | WRITE / REWRITE / DELETE              |
|-------------------------|-----------------------------------------------|---------------------------------------|
| USRSEC (User Security)  | COSGN00C, COUSR02C, COUSR03C                  | COUSR01C (WRITE), COUSR02C (REWRITE), COUSR03C (DELETE) |
| ACCTDATA (Account)      | COACTVWC, COACTUPC, COBIL00C, COTRN02C        | COACTUPC (REWRITE), COBIL00C (REWRITE) |
| CARDDATA (Card)         | COCRDLIC (BROWSE), COCRDSLC, COCRDUPC          | COCRDUPC (REWRITE)                    |
| CARDXREF (Cross-Ref)    | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, COBIL00C, COTRN02C | --                      |
| CUSTDATA (Customer)     | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC         | --                                    |
| TRANSACT (Transaction)  | COTRN00C (BROWSE), COTRN01C                    | COTRN02C (WRITE), COBIL00C (WRITE)   |

### 3.2 Batch Programs -- File Access

| Program   | Input Files (READ)                                | Output Files (WRITE)                    |
|-----------|---------------------------------------------------|-----------------------------------------|
| CBACT01C  | ACCTFILE (Account VSAM)                           | OUTFILE, ARRYFILE, VBRCFILE             |
| CBACT02C  | CARDFILE (Card VSAM)                              | (display only)                          |
| CBACT03C  | XREFFILE (Cross-Ref VSAM)                         | (display only)                          |
| CBACT04C  | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT   | TCATBALF (UPDATE), ACCTFILE (UPDATE)   |
| CBCUS01C  | CUSTFILE (Customer VSAM)                           | (display only)                          |
| CBTRN01C  | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | TRANFILE (validated records)    |
| CBTRN02C  | DALYTRAN, TRANFILE, XREFFILE, ACCTFILE, TCATBALF   | TRANFILE, DALYREJS, ACCTFILE, TCATBALF |
| CBTRN03C  | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM   | TRANREPT (report output)              |
| CBSTM03A  | (delegates to CBSTM03B)                            | STMTFILE (text), HTMLFILE (HTML)       |
| CBSTM03B  | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE              | --                                     |
| CBEXPORT  | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE   | EXPFILE (export flat file)             |
| CBIMPORT  | EXPFILE (import flat file)                         | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |

### 3.3 JCL Job -- File Access (Data Lineage)

| JCL Job    | Input Datasets                              | Output Datasets / VSAM Files              |
|------------|---------------------------------------------|-------------------------------------------|
| ACCTFILE   | `ACCTDATA.PS` (flat)                        | `ACCTDATA.VSAM.KSDS`                     |
| CARDFILE   | `CARDDATA.PS` (flat)                        | `CARDDATA.VSAM.KSDS` + AIX              |
| CUSTFILE   | `CUSTDATA.PS` (flat)                        | `CUSTDATA.VSAM.KSDS`                     |
| XREFFILE   | `CARDXREF.PS` (flat)                        | `CARDXREF.VSAM.KSDS` + AIX              |
| TRANFILE   | `TRANSACT.PS` (flat)                        | `TRANSACT.VSAM.KSDS`                     |
| DUSRSECJ   | `USRSEC.PS` (flat)                          | `USRSEC.VSAM.KSDS`                       |
| POSTTRAN   | `DALYTRAN`, `TRANSACT`, `CARDXREF`, `ACCTDATA`, `TCATBALF` | `TRANSACT`, `DALYREJS`, `ACCTDATA`, `TCATBALF` |
| INTCALC    | `TCATBALF`, `CARDXREF`, `ACCTDATA`, `DISCGRP`, `TRANSACT` | `TCATBALF`, `ACCTDATA`                |
| COMBTRAN   | `DALYTRAN`, `TRANSACT`                      | `TRANSACT` (merged)                       |
| CREASTMT   | `TRANSACT`, `CARDXREF`, `ACCTDATA`, `CUSTDATA` | `STATEMNT.PS`, `STATEMNT.HTML`, `TRXFL.VSAM.KSDS` |
| TRANBKP    | `TRANSACT.VSAM.KSDS`                       | `TRANSACT.BKUP` (GDG)                    |
| TRANREPT   | `TRANSACT`, `CARDXREF`, `TRANTYPE`, `TRANCATG` | `TRANREPT` (report file)              |
| TRANIDX    | `TRANSACT.VSAM.KSDS`                       | `TRANSACT.VSAM.AIX` (alternate index)    |
| CBEXPORT   | All 5 VSAM master files                    | `EXPORT.PS` (flat file)                   |
| CBIMPORT   | `EXPORT.PS` (flat file)                    | 5 output flat files + error file          |
| TCATBALF   | `TCATBAL.PS` (flat)                        | `TCATBALF.VSAM.KSDS`                     |
| DISCGRP    | `DISCGRP.PS` (flat)                        | `DISCGRP.VSAM.KSDS`                      |
| TRANCATG   | `TRANCATG.PS` (flat)                       | `TRANCATG.VSAM.KSDS`                     |
| TRANTYPE   | `TRANTYPE.PS` (flat)                       | `TRANTYPE.VSAM.KSDS`                     |
| CLOSEFIL   | --                                         | (CICS file close commands)                |
| OPENFIL    | --                                         | (CICS file open commands)                 |
| TXT2PDF1   | `STATEMNT.PS`                              | `STATEMNT.PS.PDF`                         |

---

## 4. Batch Processing Data Flow

### 4.1 Nightly Batch Cycle

```
┌─────────────┐
│  CLOSEFIL   │  Close CICS files for exclusive batch access
└──────┬──────┘
       v
┌─────────────────────────────────────────────────────────┐
│  DATA REFRESH PHASE                                     │
│  ACCTFILE -> CARDFILE -> CUSTFILE -> XREFFILE -> TRANFILE -> DUSRSECJ │
│  (Flat PS files --> VSAM KSDS files via IDCAMS REPRO)   │
└──────┬──────────────────────────────────────────────────┘
       v
┌─────────────┐    Input: DALYTRAN, TRANSACT, XREFFILE, ACCTFILE, TCATBALF
│  POSTTRAN   │──> Output: TRANSACT (posted), DALYREJS (rejected),
│ (CBTRN02C)  │           ACCTDATA (balance updated), TCATBALF (updated)
└──────┬──────┘
       v
┌─────────────┐    Input: TCATBALF, XREFFILE, ACCTDATA, DISCGRP, TRANSACT
│  INTCALC    │──> Output: ACCTDATA (interest applied), TCATBALF (reset)
│ (CBACT04C)  │
└──────┬──────┘
       v
┌─────────────┐    Input: TRANSACT.VSAM.KSDS
│  TRANBKP    │──> Output: TRANSACT.BKUP (GDG backup)
│ (IDCAMS)    │
└──────┬──────┘
       v
┌─────────────┐    Input: DALYTRAN + TRANSACT
│  COMBTRAN   │──> Output: TRANSACT (merged/sorted)
│ (SORT)      │
└──────┬──────┘
       v
┌─────────────┐    Input: TRANSACT, XREFFILE, ACCTDATA, CUSTDATA
│  CREASTMT   │──> Output: STATEMNT.PS (text), STATEMNT.HTML, TRXFL.VSAM
│ (CBSTM03A)  │
└──────┬──────┘
       v
┌─────────────┐    Input: TRANSACT, XREFFILE, TRANTYPE, TRANCATG
│  TRANREPT   │──> Output: TRANREPT (report file)
│ (CBTRN03C)  │
└──────┬──────┘
       v
┌─────────────┐    Input: TRANSACT.VSAM.KSDS
│  TRANIDX    │──> Output: TRANSACT.VSAM.AIX (alternate index)
│ (IDCAMS)    │
└──────┬──────┘
       v
┌─────────────┐
│  OPENFIL    │  Reopen CICS files for online access
└─────────────┘
```

### 4.2 Data Flow Summary (Entity Lifecycle)

```
External Input        Batch Staging        Master VSAM          Reports/Output
─────────────         ─────────────        ───────────          ──────────────

Daily Tran File  -->  DALYTRAN  ─┐
                                 ├─> CBTRN02C ──> TRANSACT ──> CBTRN03C ──> TRANREPT
                                 │              └─> ACCTDATA    │
                                 │              └─> TCATBALF    └──> CBSTM03A ──> STATEMNT
                                 └─> DALYREJS                           │
                                                                        └──> STATEMNT.HTML
Account PS file  -->  ACCTFILE  ──> ACCTDATA.VSAM
Card PS file     -->  CARDFILE  ──> CARDDATA.VSAM
Customer PS file -->  CUSTFILE  ──> CUSTDATA.VSAM
Xref PS file     -->  XREFFILE  ──> CARDXREF.VSAM
User PS file     -->  DUSRSECJ  ──> USRSEC.VSAM
```

---

## 5. Shared Infrastructure Dependencies

### 5.1 CICS System Services Used

| Service              | Programs Using It                                   |
|----------------------|-----------------------------------------------------|
| SEND MAP / RECEIVE MAP | All 17 online programs                           |
| READ / REWRITE / WRITE / DELETE | Account, Card, Transaction, User programs |
| STARTBR / READNEXT / READPREV / ENDBR | COCRDLIC, COTRN00C, COUSR00C, COBIL00C |
| XCTL (Transfer Control) | All navigation (menu to function)               |
| RETURN               | All online programs (return to CICS)                |
| HANDLE ABEND         | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC              |
| ASKTIME / FORMATTIME | COBIL00C, COTRN02C                                  |
| WRITEQ TD            | CORPT00C (report to transient data queue)           |
| SYNCPOINT            | COPAUS0C, COPAUS1C (auth module)                    |
| INQUIRE              | COMEN01C (check program availability)               |

### 5.2 LE (Language Environment) Services

| Service     | Called By                                              |
|-------------|--------------------------------------------------------|
| CEE3ABD     | CBACT01C-04C, CBCUS01C, CBTRN01C-03C, CBSTM03A, CBEXPORT, CBIMPORT |
| CEEDAYS     | CSUTLDTC                                               |

### 5.3 External Utility Programs

| Utility     | Used In JCL Jobs                                       |
|-------------|--------------------------------------------------------|
| IDCAMS      | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ, TRANBKP, TRANIDX, DEFGDGB, DEFGDGD, DEFCUST, ESDSRRDS, DISCGRP, TRANCATG, TRANTYPE, TCATBALF, DALYREJS, COMBTRAN |
| SORT/ICETOOL| COMBTRAN, CREASTMT                                     |
| IEBGENER    | REPTFILE, INTRDRJ1                                    |
| DFHCSDUP    | CLOSEFIL, OPENFIL                                     |
| IEFBR14     | CREASTMT (dummy step for file allocation)              |
| FTP         | FTPJCL                                                 |
| IKJEFT1B    | TXT2PDF1 (TSO batch -- TXT2PDF REXX)                  |

---

## 6. Cross-Module Dependencies (Optional Modules)

### 6.1 Authorization Module Dependencies

```
COPAUA0C ──reads──> CARDXREF.VSAM.KSDS (shared with core)
COPAUA0C ──reads──> ACCTDATA.VSAM.KSDS (shared with core)
COPAUA0C ──reads──> CUSTDATA.VSAM.KSDS (shared with core)
COPAUA0C ──uses───> IBM MQ (MQOPEN, MQGET, MQPUT1, MQCLOSE)
COPAUS0C ──reads──> (same VSAM files via CICS)
COPAUS2C ──writes─> DB2 fraud table (EXEC SQL)
PAUDBLOD ──IMS───> CBLTDLI (IMS DL/I calls)
PAUDBUNL ──IMS───> CBLTDLI (IMS DL/I calls)
```

### 6.2 Transaction Type DB2 Module Dependencies

```
COTRTLIC ──SQL──> DB2 TRTYP table (SELECT, INSERT, UPDATE, DELETE)
COTRTUPC ──SQL──> DB2 TRTYP + TRCAT tables
COBTUPDT ──SQL──> DB2 TRTYP table (INSERT from flat file)
All three ──use──> SQLCA, DSNTIAC (DB2 error formatting)
```

### 6.3 VSAM-MQ Module Dependencies

```
COACCT01 ──reads──> ACCTDATA.VSAM.KSDS
COACCT01 ──uses───> IBM MQ (3 queues: request, reply, error)
CODATE01 ──uses───> IBM MQ (3 queues: request, reply, error)
CODATE01 ──uses───> CICS ASKTIME/FORMATTIME
```
