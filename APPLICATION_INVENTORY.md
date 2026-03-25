# Application Inventory - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management)
> **Architecture:** CICS/VSAM Online + Batch COBOL | **Platform:** z/OS Mainframe

---

## Summary

| Artifact Type            | Count | Location                              |
|--------------------------|------:|---------------------------------------|
| Online COBOL Programs    |    20 | `app/cbl/`                            |
| Batch COBOL Programs     |    11 | `app/cbl/`                            |
| Copybooks                |    30 | `app/cpy/`                            |
| BMS Screen Maps          |    17 | `app/bms/`                            |
| BMS-Generated Copybooks  |    17 | `app/cpy-bms/`                        |
| JCL Batch Jobs           |    38 | `app/jcl/`                            |
| Assembler Programs       |     2 | `app/asm/`                            |
| JCL Procedures           |     2 | `app/proc/`                           |
| Scheduler Configs        |     2 | `app/scheduler/`                      |
| Optional Module Programs |    13 | `app/app-authorization-ims-db2-mq/` etc. |
| **Total Artifacts**      | **152** |                                     |

---

## 1. Online COBOL Programs (CICS)

Programs prefixed with `CO` run under CICS and handle interactive 3270 terminal sessions.

| # | Program    | LOC  | Transaction | Description                          | Classification   | VSAM Files Accessed                        |
|---|------------|-----:|-------------|--------------------------------------|------------------|--------------------------------------------|
| 1 | COSGN00C   |  260 | CC00        | Signon / Authentication              | Security         | USRSEC (R)                                 |
| 2 | COMEN01C   |  308 | CM00        | Main Menu (Regular User)             | Navigation       | None                                       |
| 3 | COADM01C   |  288 | CA00        | Admin Menu                           | Navigation       | None                                       |
| 4 | COACTVWC   |  941 | CAVW        | Account View                         | Account Mgmt     | ACCTDAT (R), CARDDAT (R), CUSTDAT (R), XREFDAT (R) |
| 5 | COACTUPC   | 4236 | CAUP        | Account Update                       | Account Mgmt     | ACCTDAT (RW), CUSTDAT (RW), XREFDAT (R)   |
| 6 | COCRDLIC   | 1459 | CCLI        | Credit Card List (paginated)         | Card Mgmt        | CARDDAT (R)                                |
| 7 | COCRDSLC   |  887 | CCDL        | Credit Card View / Detail            | Card Mgmt        | CARDDAT (R)                                |
| 8 | COCRDUPC   | 1560 | CCUP        | Credit Card Update                   | Card Mgmt        | CARDDAT (RW)                               |
| 9 | COTRN00C   |  699 | CT00        | Transaction List                     | Transaction Mgmt | TRANSACT (R)                               |
|10 | COTRN01C   |  330 | CT01        | Transaction View                     | Transaction Mgmt | TRANSACT (R)                               |
|11 | COTRN02C   |  783 | CT02        | Transaction Add                      | Transaction Mgmt | TRANSACT (RW), XREFDAT (R)                 |
|12 | CORPT00C   |  649 | CR00        | Transaction Reports                  | Reporting        | None (submits batch)                       |
|13 | COBIL00C   |  572 | CB00        | Bill Payment                         | Payment          | ACCTDAT (R), TRANSACT (RW), XREFDAT (R)   |
|14 | COUSR00C   |  695 | CU00        | User List (Admin)                    | User Admin       | USRSEC (R)                                 |
|15 | COUSR01C   |  299 | CU01        | User Add (Admin)                     | User Admin       | USRSEC (W)                                 |
|16 | COUSR02C   |  414 | CU02        | User Update (Admin)                  | User Admin       | USRSEC (RW)                                |
|17 | COUSR03C   |  359 | CU03        | User Delete (Admin)                  | User Admin       | USRSEC (RW)                                |
|18 | CSUTLDTC   |  157 | ---         | Date Validation Utility (called)     | Utility          | None                                       |

**Legend:** R = Read, W = Write, RW = Read/Write

---

## 2. Batch COBOL Programs

Programs prefixed with `CB` run in batch mode via JCL job submission.

| # | Program    | LOC  | Description                                  | Classification   | Files Read                                       | Files Written                            |
|---|------------|-----:|----------------------------------------------|------------------|--------------------------------------------------|------------------------------------------|
| 1 | CBTRN02C   |  731 | Daily transaction posting                    | Core Batch       | DALYTRAN, XREFDAT, ACCTDAT                       | TRANSACT, DALYREJS, TCATBALF             |
| 2 | CBACT04C   |  652 | Interest calculation                         | Core Batch       | XREFDAT, ACCTDAT, TRANSACT, DISCGRP              | TCATBALF                                 |
| 3 | CBSTM03A   |  924 | Statement generation (print)                 | Reporting         | TRNXFILE (sorted trans), XREFDAT, CUSTDAT, ACCTDAT | STMTFILE, HTMLFILE                      |
| 4 | CBSTM03B   |  230 | Statement data extraction                    | Reporting         | TRNXFILE, XREFDAT, CUSTDAT, ACCTDAT              | (output stream)                          |
| 5 | CBTRN03C   |  649 | Daily transaction report                     | Reporting         | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM | TRANREPT                                 |
| 6 | CBTRN01C   |  494 | Transaction validation & enrichment          | Core Batch       | DALYTRAN, CUSTDAT, XREFDAT, CARDDAT, ACCTDAT     | TRANSACT                                 |
| 7 | CBACT01C   |  430 | Account file reader (diagnostic)             | Utility          | ACCTFILE                                         | OUTFILE, ARRYFILE, VBRCFILE              |
| 8 | CBACT02C   |  178 | Card file reader (diagnostic)                | Utility          | CARDFILE                                         | SYSOUT                                   |
| 9 | CBACT03C   |  178 | Cross-reference file reader (diagnostic)     | Utility          | XREFFILE                                         | SYSOUT                                   |
|10 | CBCUS01C   |  178 | Customer file reader (diagnostic)            | Utility          | CUSTFILE                                         | SYSOUT                                   |
|11 | CBEXPORT   |  582 | Multi-entity data export                     | Data Migration   | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPFILE                                  |
|12 | CBIMPORT   |  487 | Multi-entity data import                     | Data Migration   | EXPFILE                                          | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT |
|13 | COBSWAIT   |   41 | Wait / sleep utility                         | Utility          | None                                             | None                                     |

---

## 3. Copybooks (Data Structures)

| # | Copybook   | LOC   | Record Len | Description                                  | Classification        |
|---|------------|------:|-----------:|----------------------------------------------|-----------------------|
| 1 | CVACT01Y   |    20 |        300 | Account master record                        | Core Entity           |
| 2 | CVACT02Y   |    14 |        150 | Card master record                           | Core Entity           |
| 3 | CVACT03Y   |    11 |         50 | Card-to-account cross-reference              | Core Entity           |
| 4 | CVCUS01Y   |    26 |        500 | Customer master record                       | Core Entity           |
| 5 | CVTRA05Y   |    21 |        350 | Transaction record (online)                  | Core Entity           |
| 6 | CVTRA06Y   |    21 |        350 | Daily transaction record (batch input)       | Core Entity           |
| 7 | CVTRA01Y   |    13 |         50 | Transaction category balance                 | Financial             |
| 8 | CVTRA02Y   |    13 |         50 | Disclosure group / interest rate             | Financial             |
| 9 | CVTRA03Y   |    10 |         60 | Transaction type lookup                      | Reference Data        |
|10 | CVTRA04Y   |    12 |         60 | Transaction category type                    | Reference Data        |
|11 | CVTRA07Y   |    73 |        --- | Transaction report layout                    | Reporting             |
|12 | CVEXPORT   |   103 |        500 | Multi-record export layout (REDEFINES)       | Data Migration        |
|13 | COSTM01    |    37 |        --- | Statement transaction layout (sorted key)    | Reporting             |
|14 | CSUSR01Y   |    26 |         80 | User security record                         | Security              |
|15 | COCOM01Y   |    47 |        --- | CICS communication area (COMMAREA)           | Infrastructure        |
|16 | COMEN02Y   |   101 |        --- | Main menu options definition                 | Navigation            |
|17 | COADM02Y   |    62 |        --- | Admin menu options definition                | Navigation            |
|18 | COTTL01Y   |    27 |        --- | Screen title / header                        | UI Framework          |
|19 | CSDAT01Y   |    58 |        --- | Date/time working storage                    | Utility               |
|20 | CSMSG01Y   |    24 |        --- | Common application messages                  | UI Framework          |
|21 | CSMSG02Y   |    35 |        --- | Abend handling data                          | Error Handling        |
|22 | CVCRD01Y   |    46 |        --- | Card work areas & AID key mapping            | UI Framework          |
|23 | CSUTLDWY   |    89 |        --- | Date validation working storage              | Utility               |
|24 | CSUTLDPY   |   375 |        --- | Date validation procedure division           | Utility               |
|25 | CSSTRPFY   |    85 |        --- | PF-key storage procedure                     | UI Framework          |
|26 | CSSETATY   |    30 |        --- | Field attribute setting macro                | UI Framework          |
|27 | CSLKPCDY   | 1,318 |        --- | Lookup codes (area codes, states, zip)       | Reference Data        |
|28 | CODATECN   |    52 |        --- | Date conversion input/output record          | Utility               |
|29 | CUSTREC    |    26 |        500 | Customer record (batch variant)              | Core Entity           |
|30 | UNUSED1Y   |    10 |         80 | Unused / placeholder record                  | Deprecated            |

---

## 4. BMS Screen Maps

| # | Map        | LOC | Associated Program(s)   | Description                     | Screen Fields |
|---|------------|----:|-------------------------|---------------------------------|---------------|
| 1 | COSGN00    | 210 | COSGN00C                | Signon screen                   | User ID, Password, Error Message |
| 2 | COMEN01    | 167 | COMEN01C                | Main menu (11 options)          | Menu option selection |
| 3 | COADM01    | 167 | COADM01C                | Admin menu (6 options)          | Menu option selection |
| 4 | COACTVW    | 378 | COACTVWC                | Account view                    | Acct ID, Status, Balance, Credit Limit, Dates |
| 5 | COACTUP    | 512 | COACTUPC                | Account update                  | All account & customer fields (editable) |
| 6 | COCRDLI    | 344 | COCRDLIC                | Card list (7-row table)         | Card #, Name, Status, Acct, Expiry (x7) |
| 7 | COCRDSL    | 157 | COCRDSLC                | Card detail view                | Card #, Name, CVV, Expiry, Status |
| 8 | COCRDUP    | 172 | COCRDUPC                | Card update                     | Card fields (editable) |
| 9 | COTRN00    | 464 | COTRN00C                | Transaction list                | Trans ID, Type, Amount, Date (paginated) |
|10 | COTRN01    | 273 | COTRN01C                | Transaction view                | All transaction detail fields |
|11 | COTRN02    | 307 | COTRN02C                | Transaction add                 | Card #, Type, Amount, Description, Merchant |
|12 | CORPT00    | 231 | CORPT00C                | Transaction reports parameters  | Start/End Date, Report Type |
|13 | COBIL00    | 141 | COBIL00C                | Bill payment                    | Acct ID, Amount, Confirmation |
|14 | COUSR00    | 463 | COUSR00C                | User list (admin)               | User ID, Name, Type (paginated) |
|15 | COUSR01    | 164 | COUSR01C                | User add                        | User ID, Name, Password, Type |
|16 | COUSR02    | 169 | COUSR02C                | User update                     | User fields (editable) |
|17 | COUSR03    | 153 | COUSR03C                | User delete confirmation        | User fields (read-only) + confirm |

---

## 5. JCL Batch Jobs

### 5a. Data Refresh Jobs

| # | Job        | Description                                   | Programs Called       | Key Datasets                         |
|---|------------|-----------------------------------------------|-----------------------|--------------------------------------|
| 1 | ACCTFILE   | Delete/define/load account VSAM file           | IDCAMS                | AWS.M2.CARDDEMO.ACCTDAT.PS -> VSAM   |
| 2 | CARDFILE   | Delete/define/load card VSAM file + alt index  | IDCAMS, SDSF          | AWS.M2.CARDDEMO.CARDDAT.PS -> VSAM   |
| 3 | CUSTFILE   | Delete/define/load customer VSAM file          | IDCAMS, SDSF          | AWS.M2.CARDDEMO.CUSTDAT.PS -> VSAM   |
| 4 | XREFFILE   | Delete/define/load cross-ref VSAM + alt index  | IDCAMS                | AWS.M2.CARDDEMO.CARDXREF.PS -> VSAM  |
| 5 | TRANFILE   | Delete/define/load transaction VSAM file       | IDCAMS, SDSF          | AWS.M2.CARDDEMO.TRANSACT.PS -> VSAM  |
| 6 | DUSRSECJ   | Load user security VSAM file                   | IEFBR14, IEBGENER, IDCAMS | AWS.M2.CARDDEMO.USRSEC -> VSAM  |
| 7 | TCATBALF   | Define transaction category balance file       | IDCAMS                | AWS.M2.CARDDEMO.TCATBAL -> VSAM      |
| 8 | TRANTYPE   | Define/load transaction type reference         | IDCAMS                | AWS.M2.CARDDEMO.TRANTYPE -> VSAM     |
| 9 | TRANCATG   | Define/load transaction category reference     | IDCAMS                | AWS.M2.CARDDEMO.TRANCATG -> VSAM     |
|10 | DISCGRP    | Define/load disclosure group file              | IDCAMS                | AWS.M2.CARDDEMO.DISCGRP -> VSAM      |
|11 | REPTFILE   | Define report output file                      | IDCAMS                | AWS.M2.CARDDEMO.TRANREPT             |
|12 | DALYREJS   | Define daily rejects file                      | IDCAMS                | AWS.M2.CARDDEMO.DALYREJS             |
|13 | DEFCUST    | Define additional customer dataset             | IDCAMS                | AWS.M2.CARDDEMO.CUSTDAT.VSAM         |

### 5b. Batch Processing Jobs

| # | Job        | Description                                   | Programs Called           | Inputs                      | Outputs                   |
|---|------------|-----------------------------------------------|---------------------------|-----------------------------|---------------------------|
|14 | POSTTRAN   | Post daily transactions                       | CBTRN02C                  | DALYTRAN, XREFDAT, ACCTDAT  | TRANSACT, DALYREJS        |
|15 | INTCALC    | Calculate interest charges                    | CBACT04C                  | XREFDAT, ACCTDAT, TRANSACT  | TCATBALF                  |
|16 | COMBTRAN   | Sort & combine transaction files              | SORT                      | Multiple TRAN GDGs          | Combined TRANSACT         |
|17 | CREASTMT   | Create customer statements                    | SORT, IDCAMS, CBSTM03A   | TRANSACT                    | STMTFILE                  |
|18 | TRANREPT   | Generate transaction reports                  | SORT, CBTRN03C            | TRANSACT, CARDXREF, etc.    | TRANREPT                  |
|19 | TRANBKP    | Backup transaction file to GDG                | IDCAMS                    | TRANSACT VSAM               | GDG backup                |
|20 | TRANIDX    | Define/build alternate index on transactions  | IDCAMS                    | TRANSACT VSAM               | Alt index paths           |
|21 | CBEXPORT   | Export all entity data to sequential file     | IDCAMS, CBEXPORT          | All VSAM files              | EXPFILE                   |
|22 | CBIMPORT   | Import data from export file                  | CBIMPORT                  | EXPFILE                     | Individual output files   |

### 5c. Utility & Infrastructure Jobs

| # | Job        | Description                                   | Programs Called        |
|---|------------|-----------------------------------------------|-----------------------|
|23 | CLOSEFIL   | Close CICS files for batch processing          | SDSF                  |
|24 | OPENFIL    | Open CICS files after batch processing         | SDSF                  |
|25 | WAITSTEP   | Wait/pause step utility                        | COBSWAIT              |
|26 | DEFGDGB    | Define GDG base entries                        | IDCAMS                |
|27 | DEFGDGD    | Define GDG data entries                        | IDCAMS, IEBGENER      |
|28 | PRTCATBL   | Print transaction category balance             | IEFBR14, SORT         |
|29 | ESDSRRDS   | ESDS/RRDS dataset examples                     | IEFBR14, IEBGENER, IDCAMS |
|30 | CBADMCDJ   | Load CICS CSD definitions                      | DFHCSDUP              |
|31 | FTPJCL     | FTP file transfer                              | FTP                   |
|32 | TXT2PDF1   | Convert text statements to PDF                 | IKJEFT1B (REXX)       |
|33 | INTRDRJ1   | Internal reader job submission (1)              | IDCAMS, IEBGENER      |
|34 | INTRDRJ2   | Internal reader job submission (2)              | IDCAMS                |

### 5d. Diagnostic / Reader Jobs

| # | Job        | Description                                   | Programs Called        |
|---|------------|-----------------------------------------------|-----------------------|
|35 | READACCT   | Read & display account file contents           | IEFBR14, CBACT01C     |
|36 | READCARD   | Read & display card file contents              | CBACT02C              |
|37 | READCUST   | Read & display customer file contents          | CBCUS01C              |
|38 | READXREF   | Read & display cross-ref file contents         | CBACT03C              |

---

## 6. Optional Module Programs

### 6a. Authorization Module (IMS/DB2/MQ)

| # | Program    | LOC   | Description                                  | Classification   |
|---|------------|------:|----------------------------------------------|------------------|
| 1 | COPAUA0C   | 1,026 | MQ trigger for authorization requests         | Integration      |
| 2 | COPAUS0C   | 1,032 | Pending authorization summary view            | Online           |
| 3 | COPAUS1C   |   604 | Pending authorization detail view             | Online           |
| 4 | COPAUS2C   |   244 | Mark authorization as fraud (DB2 write)       | Online           |
| 5 | CBPAUP0C   |   386 | Batch purge of old authorizations             | Batch            |
| 6 | PAUDBLOD   |   369 | Load authorization data to DB2                | Batch Utility    |
| 7 | DBUNLDGS   |   366 | Unload DB2 authorization data                 | Batch Utility    |
| 8 | PAUDBUNL   |   317 | Unload with parameter control                 | Batch Utility    |

### 6b. Transaction Type DB2 Module

| # | Program    | LOC   | Description                                  | Classification   |
|---|------------|------:|----------------------------------------------|------------------|
| 1 | COTRTLIC   | 2,098 | Transaction type list/delete (DB2 cursors)    | Online (Admin)   |
| 2 | COTRTUPC   | 1,702 | Transaction type add/edit (DB2 embedded SQL)  | Online (Admin)   |
| 3 | COBTUPDT   |   237 | Batch update of transaction types (DB2)       | Batch            |

### 6c. VSAM-MQ Module

| # | Program    | LOC   | Description                                  | Classification   |
|---|------------|------:|----------------------------------------------|------------------|
| 1 | COACCT01   |   620 | MQ request/response for account inquiry       | Integration      |
| 2 | CODATE01   |   524 | MQ request/response for system date           | Integration      |

---

## 7. Assembler Programs

| # | Program    | Location   | Description                             |
|---|------------|------------|-----------------------------------------|
| 1 | MVSWAIT    | `app/asm/` | MVS wait macro (sleep utility)          |
| 2 | COBDATFT   | `app/asm/` | Date formatting assembly routine        |

---

## 8. Procedures & Scheduler Configs

| # | Artifact     | Location          | Description                                |
|---|--------------|-------------------|--------------------------------------------|
| 1 | PRCBTRN2     | `app/proc/`       | JCL procedure for transaction posting step |
| 2 | PRC001       | `app/proc/`       | General-purpose copy/backup procedure      |
| 3 | CA7 config   | `app/scheduler/`  | CA-7 batch scheduler job definitions       |
| 4 | Control-M    | `app/scheduler/`  | Control-M batch scheduler job definitions  |

---

## 9. Functional Classification Summary

| Domain               | Online Programs | Batch Programs | Total |
|----------------------|----------------:|---------------:|------:|
| Account Management   |               2 |              1 |     3 |
| Card Management      |               3 |              0 |     3 |
| Transaction Processing |             3 |              3 |     6 |
| Payment              |               1 |              0 |     1 |
| Reporting            |               1 |              2 |     3 |
| User Administration  |               4 |              0 |     4 |
| Security / Auth      |               1 |              0 |     1 |
| Navigation           |               2 |              0 |     2 |
| Data Migration       |               0 |              2 |     2 |
| Utilities            |               1 |              5 |     6 |
| **Subtotal (core)**  |            **18** |          **13** | **31** |
| Optional Modules     |               7 |              6 |    13 |
| **Grand Total**      |            **25** |          **19** | **44** |

---

## 10. VSAM File Inventory

| # | VSAM File      | DD Name     | Record Len | Key Field       | Description                    |
|---|----------------|-------------|------------|-----------------|--------------------------------|
| 1 | ACCTDAT        | ACCTFILE    | 300 bytes  | ACCT-ID (11)    | Account master                 |
| 2 | CARDDAT        | CARDFILE    | 150 bytes  | CARD-NUM (16)   | Card master                    |
| 3 | CUSTDAT        | CUSTFILE    | 500 bytes  | CUST-ID (9)     | Customer master                |
| 4 | CARDXREF       | XREFFILE    | 50 bytes   | XREF-CARD-NUM (16) | Card-Account cross-reference |
| 5 | TRANSACT       | TRANFILE    | 350 bytes  | TRAN-ID (16)    | Transaction master             |
| 6 | USRSEC         | USRSEC      | 80 bytes   | SEC-USR-ID (8)  | User security                  |
| 7 | DALYTRAN       | DALYTRAN    | 350 bytes  | DALYTRAN-ID (16)| Daily transaction input        |
| 8 | TCATBALF       | TCATBALF    | 50 bytes   | Composite key   | Transaction category balance   |
| 9 | DISCGRP        | DISCGRP     | 50 bytes   | Composite key   | Disclosure / interest rate     |
|10 | TRANTYPE       | TRANTYPE    | 60 bytes   | TRAN-TYPE (2)   | Transaction type reference     |
|11 | TRANCATG       | TRANCATG    | 60 bytes   | Composite key   | Transaction category reference |
|12 | DALYREJS       | DALYREJS    | ---        | ---             | Daily rejected transactions    |
