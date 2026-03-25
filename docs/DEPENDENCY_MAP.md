# DEPENDENCY MAP - CardDemo COBOL Application

> **Generated**: 2026-03-25 | **Application**: CardDemo - Credit Card Management System
> **Purpose**: Call graph, copybook inclusion map, and data lineage across programs and JCL jobs

---

## 1. Program-to-Program Call Graph

### 1.1 Online CICS Program Navigation (XCTL / LINK)

The online programs form a screen-navigation graph via `EXEC CICS XCTL` (transfer control) and `EXEC CICS LINK` (call and return). The COMMAREA (`COCOM01Y`) carries state between programs.

```
                        COSGN00C (Sign-On)
                             |
                             v
                        COMEN01C (Main Menu)
                       /    |    |    \        \
                      v     v    v     v        v
               COACTVWC  COCRDLIC  COTRN00C  CORPT00C  COBIL00C
               (Acct View) (Card List) (Tran List) (Reports) (Bill Pay)
                  |          |    \       |
                  v          v     v      v
               COACTUPC  COCRDSLC  COCRDUPC  COTRN01C --> COTRN02C
               (Acct Upd) (Card View) (Card Upd) (Tran View)  (Tran Add)
                        
                        COADM01C (Admin Menu)
                       /    |    \       \
                      v     v     v       v
               COUSR00C  COUSR01C  COUSR02C  COUSR03C
               (User List) (User Add) (User Upd) (User Del)
```

### 1.2 Detailed XCTL/Navigation Matrix

| Source Program | Target Program | Mechanism     | Condition                           |
|---------------|----------------|---------------|-------------------------------------|
| COSGN00C      | COMEN01C       | XCTL          | Successful authentication           |
| COMEN01C      | COSGN00C       | XCTL          | PF3 (Exit / Sign-Off)              |
| COMEN01C      | COACTVWC       | XCTL          | Menu option: Account View           |
| COMEN01C      | COCRDLIC       | XCTL          | Menu option: Card List              |
| COMEN01C      | COTRN00C       | XCTL          | Menu option: Transaction List       |
| COMEN01C      | CORPT00C       | XCTL          | Menu option: Reports                |
| COMEN01C      | COBIL00C       | XCTL          | Menu option: Bill Payment           |
| COMEN01C      | COADM01C       | XCTL          | Menu option: Admin (Admin users)    |
| COACTVWC      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COACTVWC      | COACTUPC       | XCTL          | Select account for update           |
| COACTUPC      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COCRDLIC      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COCRDLIC      | COCRDSLC       | XCTL          | Select card for viewing             |
| COCRDLIC      | COCRDUPC       | XCTL          | Select card for update              |
| COCRDSLC      | COCRDLIC       | XCTL          | PF3 (Return to card list)          |
| COCRDUPC      | COCRDLIC       | XCTL          | PF3 (Return to card list)          |
| COTRN00C      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COTRN00C      | COTRN01C       | XCTL          | Select transaction for viewing      |
| COTRN00C      | COTRN02C       | XCTL          | PF4 (Add new transaction)          |
| COTRN01C      | COTRN00C       | XCTL          | PF3 (Return to transaction list)   |
| COTRN02C      | COTRN00C       | XCTL          | PF3 (Return to transaction list)   |
| CORPT00C      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COBIL00C      | COMEN01C       | XCTL          | PF3 (Return to menu)               |
| COADM01C      | COMEN01C       | XCTL          | PF3 (Return to main menu)          |
| COADM01C      | COUSR00C       | XCTL          | Menu option: List Users             |
| COADM01C      | COUSR01C       | XCTL          | Menu option: Add User               |
| COUSR00C      | COADM01C       | XCTL          | PF3 (Return to admin menu)         |
| COUSR00C      | COUSR02C       | XCTL          | Select user: 'U' for update        |
| COUSR00C      | COUSR03C       | XCTL          | Select user: 'D' for delete        |
| COUSR01C      | COADM01C       | XCTL          | PF3 (Return to admin menu)         |
| COUSR02C      | COADM01C       | XCTL          | PF3 (Return to admin menu)         |
| COUSR03C      | COADM01C       | XCTL          | PF3 (Return to admin menu)         |

### 1.3 Batch Program CALL Graph

```
CBSTM03A (Statement Driver)
   |
   +---> CALL 'CBSTM03B'   (File I/O subroutine - sequential reads)

CBACT01C (Account Reader)
   |
   +---> CALL 'COBDATFT'   (Assembler date formatting)

CBACT02C / CBACT03C / CBCUS01C
   |
   +---> CALL 'CEE3ABD'    (LE Abend routine - error handling)

CSUTLDTC (Date Validator)
   |
   +---> CALL 'CEEDAYS'    (LE Date conversion intrinsic)

CORPT00C (Report Request - Online)
   |
   +---> Submits JCL via Internal Reader (TRANREPT job with CBTRN03C)
```

---

## 2. Copybook Inclusion Map

### 2.1 Programs to Copybooks Matrix

| Copybook     | Programs That Include It                                                                |
|--------------|----------------------------------------------------------------------------------------|
| **COCOM01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C, COTRN00C, COTRN01C, COTRN02C, COSGN00C |
| **COTTL01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C, COTRN00C, COTRN01C, COTRN02C, COSGN00C |
| **CSDAT01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C, COTRN00C, COTRN01C, COTRN02C, COSGN00C |
| **CSMSG01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C, COTRN00C, COTRN01C, COTRN02C, COSGN00C |
| **CSMSG02Y** | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, COBIL00C, COUSR02C, COUSR03C                  |
| **CSUSR01Y** | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C, COTRN00C, COTRN01C, COTRN02C |
| **CVACT01Y** | CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC |
| **CVACT02Y** | CBACT02C, CBEXPORT, CBIMPORT, CBTRN01C, COACTVWC, COCRDUPC                            |
| **CVACT03Y** | CBACT03C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC |
| **CVCUS01Y** | CBCUS01C, CBEXPORT, CBIMPORT, CBSTM03A, COACTVWC, COCRDUPC                            |
| **CVCRD01Y** | COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC                                                |
| **CVTRA05Y** | CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C, CBTRN03C, CORPT00C                            |
| **CVTRA06Y** | CBTRN01C, CBTRN02C                                                                    |
| **CVTRA01Y** | CBACT04C, CBTRN02C                                                                    |
| **CVTRA02Y** | CBACT04C                                                                               |
| **CVTRA03Y** | CBTRN03C                                                                               |
| **CVTRA04Y** | CBTRN03C                                                                               |
| **CVTRA07Y** | CBTRN03C                                                                               |
| **CVEXPORT** | CBEXPORT, CBIMPORT                                                                     |
| **COSTM01**  | CBSTM03A                                                                               |
| **CUSTREC**  | CBSTM03A                                                                               |
| **CSLKPCDY** | COACTUPC                                                                               |
| **CSUTLDPY** | (Utility parameters)                                                                   |
| **CSUTLDWY** | COACTUPC, COCRDUPC                                                                     |
| **CSSTRPFY** | COACTUPC, COCRDUPC                                                                     |
| **CSSETATY** | COACTUPC, COCRDUPC                                                                     |
| **CODATECN** | CBACT01C                                                                               |
| **COMEN02Y** | COMEN01C                                                                               |
| **COADM02Y** | COADM01C                                                                               |

### 2.2 BMS Map to Program Mapping

| BMS Map      | BMS Copybook | COBOL Program | Screen Function              |
|-------------|-------------|---------------|------------------------------|
| COSGN00.bms | COSGN00.CPY | COSGN00C      | Sign-On                      |
| COMEN01.bms | COMEN01.CPY | COMEN01C      | Main Menu                    |
| COADM01.bms | COADM01.CPY | COADM01C      | Admin Menu                   |
| COACTVW.bms | COACTVW.CPY | COACTVWC      | Account View                 |
| COACTUP.bms | COACTUP.CPY | COACTUPC      | Account Update               |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC      | Card List                    |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC      | Card Detail View             |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC      | Card Update                  |
| COTRN00.bms | COTRN00.CPY | COTRN00C      | Transaction List             |
| COTRN01.bms | COTRN01.CPY | COTRN01C      | Transaction View             |
| COTRN02.bms | COTRN02.CPY | COTRN02C      | Transaction Add              |
| CORPT00.bms | CORPT00.CPY | CORPT00C      | Report Request               |
| COBIL00.bms | COBIL00.CPY | COBIL00C      | Bill Payment                 |
| COUSR00.bms | COUSR00.CPY | COUSR00C      | User List                    |
| COUSR01.bms | COUSR01.CPY | COUSR01C      | User Add                     |
| COUSR02.bms | COUSR02.CPY | COUSR02C      | User Update                  |
| COUSR03.bms | COUSR03.CPY | COUSR03C      | User Delete                  |

---

## 3. JCL Job Data Lineage

### 3.1 JCL Job to Program Mapping

| JCL Job     | Program(s) Executed           | Description                          |
|-------------|-------------------------------|--------------------------------------|
| POSTTRAN    | CBTRN01C, CBTRN02C            | Validate & post daily transactions   |
| INTCALC     | CBACT04C                      | Interest calculation                 |
| CREASTMT    | CBSTM03A (calls CBSTM03B)    | Statement generation                 |
| TRANREPT    | CBTRN03C                      | Transaction report generation        |
| READACCT    | CBACT01C                      | Account file verification            |
| READCARD    | CBACT02C                      | Card file verification               |
| READCUST    | CBCUS01C                      | Customer file verification           |
| READXREF    | CBACT03C                      | Cross-reference file verification    |
| CBEXPORT    | CBEXPORT                      | Multi-entity data export             |
| CBIMPORT    | CBIMPORT                      | Multi-entity data import             |
| WAITSTEP    | COBSWAIT                      | Wait/pause step                      |

### 3.2 VSAM Dataset to Program I/O Matrix

| Dataset (DD Name)             | Read By                                          | Written By                        |
|-------------------------------|--------------------------------------------------|-----------------------------------|
| **ACCTFILE** (Account Master) | CBACT01C, CBACT04C, CBEXPORT, CBSTM03A/B, CBTRN01C, COACTUPC, COACTVWC, COTRN02C | CBACT04C (REWRITE), CBIMPORT, ACCTFILE.jcl |
| **CARDFILE** (Card Master)    | CBACT02C, CBEXPORT, CBTRN01C, COCRDLIC, COCRDSLC, COCRDUPC | CBIMPORT, CARDFILE.jcl |
| **CUSTFILE** (Customer)       | CBCUS01C, CBEXPORT, CBSTM03A/B, COACTVWC         | CBIMPORT, CUSTFILE.jcl            |
| **XREFFILE** (Card Xref)      | CBACT03C, CBACT04C, CBEXPORT, CBSTM03A/B, CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C | CBIMPORT, XREFFILE.jcl |
| **TRANSACT** (Trans Master)   | CBACT04C, CBTRN03C, CREASTMT (SORT step)         | CBTRN02C, TRANFILE.jcl            |
| **DALYTRAN** (Daily Trans)    | CBTRN01C, CBTRN02C                               | External feed / POSTTRAN input    |
| **DALYREJS** (Daily Rejects)  | (report review)                                  | CBTRN02C                          |
| **TCATBALF** (Cat Balance)    | CBACT04C                                         | CBTRN02C, TCATBALF.jcl            |
| **DISCGRP**  (Disclosure)     | CBACT04C                                         | DISCGRP.jcl                       |
| **TRANTYPE** (Trans Types)    | CBTRN03C                                         | TRANTYPE.jcl                      |
| **TRANCATG** (Trans Categories)| CBTRN03C                                        | TRANCATG.jcl                      |
| **USRSEC**   (User Security)  | COSGN00C, COUSR00C, COUSR02C, COUSR03C           | COUSR01C, COUSR02C, DUSRSECJ.jcl  |
| **STMTFILE** (Statement output)| (downstream)                                    | CBSTM03A                          |
| **HTMLFILE** (HTML Statement) | (downstream)                                     | CBSTM03A                          |
| **TRANREPT** (Report output)  | (downstream)                                     | CBTRN03C                          |
| **EXPFILE**  (Export file)     | CBIMPORT                                         | CBEXPORT                          |

### 3.3 Batch Cycle Data Flow

```
External Feed
     |
     v
DALYTRAN (Daily Transactions - sequential input)
     |
     +---> CBTRN01C (Validate: lookup XREF, CARD, ACCT, CUST)
     |         |
     |         +-- reads --> XREFFILE, CARDFILE, ACCTFILE, CUSTFILE
     |         +-- writes -> TRANFILE (valid transactions to master)
     |
     +---> CBTRN02C (Post: validate & write to master + category balance)
              |
              +-- reads --> DALYTRAN, XREFFILE, ACCTFILE
              +-- writes -> TRANSACT (master), DALYREJS (rejects), TCATBALF (balances)
              
                      |
                      v
              CBACT04C (Interest Calculation)
                 |
                 +-- reads --> TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT
                 +-- writes -> ACCTFILE (updated balances via REWRITE)
                 
                      |
                      v
              CBSTM03A / CBSTM03B (Statement Generation)
                 |
                 +-- reads --> TRANSACT (via TRXFL sorted copy), XREFFILE, CUSTFILE, ACCTFILE
                 +-- writes -> STMTFILE (text), HTMLFILE (HTML)
                 
                      |
                      v
              CBTRN03C (Transaction Report)
                 |
                 +-- reads --> TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM
                 +-- writes -> TRANREPT (report output)
```

---

## 4. Online Program Data Access

### 4.1 CICS VSAM File Access (EXEC CICS READ/WRITE/REWRITE/DELETE)

| Program      | VSAM File    | Operations          | Business Action                       |
|-------------|-------------|---------------------|---------------------------------------|
| COSGN00C    | USRSEC      | READ                | Authenticate user credentials         |
| COACTVWC    | ACCTFILE    | READ                | Display account details               |
| COACTVWC    | CARDFILE    | READ                | Display card details for account      |
| COACTVWC    | CUSTFILE    | READ                | Display customer info for account     |
| COACTVWC    | XREFFILE    | READ                | Lookup card-to-account mapping        |
| COACTUPC    | ACCTFILE    | READ, REWRITE       | View and update account fields        |
| COACTUPC    | XREFFILE    | READ                | Lookup for account validation         |
| COACTUPC    | CUSTFILE    | READ                | Display customer info                 |
| COCRDLIC    | CARDFILE    | READ (browse)       | List cards with pagination            |
| COCRDLIC    | XREFFILE    | READ                | Lookup account for each card          |
| COCRDSLC    | CARDFILE    | READ                | Display single card details           |
| COCRDSLC    | XREFFILE    | READ                | Cross-reference lookup                |
| COCRDUPC    | CARDFILE    | READ, REWRITE       | View and update card fields           |
| COCRDUPC    | XREFFILE    | READ                | Cross-reference lookup                |
| COTRN00C    | TRANSACT    | READ (browse)       | List transactions with pagination     |
| COTRN00C    | XREFFILE    | READ                | Filter by card/account                |
| COTRN01C    | TRANSACT    | READ                | Display single transaction            |
| COTRN02C    | TRANSACT    | WRITE               | Add new transaction                   |
| COTRN02C    | XREFFILE    | READ                | Validate card/account                 |
| COTRN02C    | ACCTFILE    | READ                | Validate account exists               |
| COBIL00C    | ACCTFILE    | READ, REWRITE       | Process bill payment                  |
| COBIL00C    | TRANSACT    | WRITE               | Record payment as transaction         |
| COBIL00C    | XREFFILE    | READ                | Card-to-account lookup                |
| COUSR00C    | USRSEC      | READ (browse)       | List users with pagination            |
| COUSR01C    | USRSEC      | WRITE               | Create new user record                |
| COUSR02C    | USRSEC      | READ, REWRITE       | Update user details                   |
| COUSR03C    | USRSEC      | READ, DELETE         | Delete user record                    |

---

## 5. Optional Module Dependencies

### 5.1 Authorization Module (IMS/DB2/MQ)

```
COPAUA0C (MQ Trigger)
   +-- MQ GET --> Request Queue
   +-- IMS DLI GU/GNP --> Authorization IMS DB
   +-- CICS READ --> XREFFILE, ACCTFILE, CUSTFILE
   +-- MQ PUT --> Response Queue, Error Queue

COPAUS0C (Summary Screen)
   +-- IMS DLI GNP --> Authorization IMS DB
   +-- CICS READ --> XREFFILE, ACCTFILE, CUSTFILE
   +-- CICS SYNCPOINT

COPAUS1C (Detail Screen)
   +-- CICS LINK --> (sub-programs)
   +-- IMS DLI GU/GNP/REPL --> Authorization IMS DB
   +-- CICS SYNCPOINT

COPAUS2C (Fraud Marking)
   +-- EXEC SQL INSERT/UPDATE --> DB2 Authorization Fraud Table
   +-- CICS SYNCPOINT

CBPAUP0C (Batch Purge)
   +-- IMS DLI GN/GNP/DLET --> Authorization IMS DB
   +-- IMS DLI CHKP --> Checkpoint
```

### 5.2 Transaction Type DB2 Module

```
COTRTLIC (List/Delete - 2,098 lines)
   +-- EXEC SQL DECLARE CURSOR / OPEN / FETCH / CLOSE --> DB2 TRAN_TYPE table
   +-- EXEC SQL DELETE --> DB2 TRAN_TYPE table
   +-- CICS SYNCPOINT

COTRTUPC (Add/Edit - 1,702 lines)
   +-- EXEC SQL INSERT / UPDATE / SELECT --> DB2 TRAN_TYPE, TRAN_CAT tables
   +-- CICS SYNCPOINT

COBTUPDT (Batch Update - 237 lines)
   +-- EXEC SQL DECLARE CURSOR / FETCH / UPDATE --> DB2 TRAN_TYPE table
```

### 5.3 VSAM-MQ Module

```
COACCT01 (Account Inquiry - 620 lines)
   +-- CICS RETRIEVE --> Start data
   +-- MQ OPEN/GET --> Request queue (account inquiry request)
   +-- CICS READ --> ACCTFILE (VSAM)
   +-- MQ PUT --> Response queue (account data), Error queue
   +-- MQ CLOSE

CODATE01 (System Date - 524 lines)
   +-- CICS RETRIEVE --> Start data
   +-- MQ OPEN/GET --> Request queue (date request)
   +-- CICS ASKTIME / FORMATTIME --> System date/time
   +-- MQ PUT --> Response queue (formatted date), Error queue
   +-- MQ CLOSE
```

---

## 6. Cross-Cutting Concerns

### 6.1 Shared Infrastructure Dependencies

| Concern           | Mechanism                     | Used By                                      |
|-------------------|-------------------------------|----------------------------------------------|
| Error Handling    | CEE3ABD (LE Abend)            | CBACT02C, CBACT03C, CBCUS01C                 |
| Error Handling    | EXEC CICS HANDLE ABEND        | COACTUPC, COACTVWC, COCRDLIC, COCRDUPC, etc. |
| Date Validation   | CSUTLDTC -> CEEDAYS           | Called by programs needing date validation    |
| Date Formatting   | COBDATFT (ASM)                | CBACT01C                                     |
| Screen Navigation | COCOM01Y (COMMAREA)           | All 17 online CICS programs                  |
| Screen Headers    | COTTL01Y + CSDAT01Y           | All 17 online CICS programs                  |
| Messages          | CSMSG01Y, CSMSG02Y            | All online CICS programs                     |
| Lookup Tables     | CSLKPCDY (1,318 lines)        | COACTUPC (largest reference data)            |

### 6.2 External System Interfaces

| Interface     | Direction | Programs           | Protocol        |
|---------------|-----------|--------------------|-----------------|
| MQ Queues     | In/Out    | COPAUA0C, COACCT01, CODATE01 | IBM MQ  |
| IMS Database  | Read/Write| COPAUA0C, COPAUS0C/1C, CBPAUP0C | DL/I |
| DB2 Database  | Read/Write| COPAUS2C, COTRTLIC, COTRTUPC, COBTUPDT | SQL |
| FTP Server    | Out       | FTPJCL             | FTP             |
| Internal Reader| Out      | CORPT00C, INTRDRJ1 | JES2 INTRDR     |
