# CardDemo Application Inventory

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM z/OS Mainframe (CICS/VSAM/JCL)

## Executive Summary

CardDemo is a mainframe credit card management application built with COBOL, CICS, VSAM, and JCL. It simulates account management, card management, transactions, bill payments, and reporting. The application supports two user roles: **Regular** (card operations) and **Admin** (user and transaction-type management).

| Artifact Type         | Core Count | Optional Modules | Total |
|-----------------------|------------|-------------------|-------|
| COBOL Programs        | 31         | 13                | 44    |
| Copybooks             | 30         | 11                | 41    |
| BMS Maps              | 17         | 4                 | 21    |
| BMS-Generated Copybooks | 17      | 4                 | 21    |
| JCL Jobs              | 38         | 8                 | 46    |
| Assembler Programs    | 2          | 0                 | 2     |
| JCL Procedures        | 2          | 0                 | 2     |

---

## 1. COBOL Programs (Core) &mdash; `app/cbl/`

### 1.1 Online CICS Programs (CO* prefix)

| # | Program    | Lines | CICS Trans | Function                              | Business Domain     | Complexity |
|---|------------|-------|------------|---------------------------------------|---------------------|------------|
| 1 | COSGN00C   | 261   | CC00       | Sign-on / Authentication              | Security            | Low        |
| 2 | COMEN01C   | 309   | CM00       | Main Menu (Regular users)             | Navigation          | Low        |
| 3 | COADM01C   | 288   | CA00       | Admin Menu                            | Navigation / Admin  | Low        |
| 4 | COACTVWC   | 942   | CAVW       | Account View (read-only)              | Account Mgmt        | Medium     |
| 5 | COACTUPC   | 4,237 | CAUP       | Account Update (full CRUD)            | Account Mgmt        | Very High  |
| 6 | COCRDLIC   | 1,460 | CCLI       | Credit Card List (browse/page)        | Card Mgmt           | High       |
| 7 | COCRDSLC   | 888   | CCDL       | Credit Card Detail View               | Card Mgmt           | Medium     |
| 8 | COCRDUPC   | 1,560 | CCUP       | Credit Card Update                    | Card Mgmt           | High       |
| 9 | COTRN00C   | 699   | CT00       | Transaction List (browse/page)        | Transaction Mgmt    | Medium     |
| 10| COTRN01C   | 330   | CT01       | Transaction View (single record)      | Transaction Mgmt    | Low        |
| 11| COTRN02C   | 783   | CT02       | Transaction Add (new transaction)     | Transaction Mgmt    | Medium     |
| 12| CORPT00C   | 649   | CR00       | Transaction Report (submit batch)     | Reporting           | Medium     |
| 13| COBIL00C   | 572   | CB00       | Bill Payment (pay balance)            | Billing / Payments  | Medium     |
| 14| COUSR00C   | 695   | CU00       | User List (Admin)                     | User Admin          | Medium     |
| 15| COUSR01C   | 299   | CU01       | User Add (Admin)                      | User Admin          | Low        |
| 16| COUSR02C   | 414   | CU02       | User Update (Admin)                   | User Admin          | Medium     |
| 17| COUSR03C   | 359   | CU03       | User Delete (Admin)                   | User Admin          | Low        |

### 1.2 Batch Programs (CB* prefix)

| # | Program    | Lines | Function                                          | Business Domain     | Complexity |
|---|------------|-------|---------------------------------------------------|---------------------|------------|
| 18| CBACT01C   | 430   | Read account file, write to multiple output files  | Account Mgmt        | Medium     |
| 19| CBACT02C   | 178   | Read and print card data file                      | Card Mgmt           | Low        |
| 20| CBACT03C   | 178   | Read and print account cross-reference data        | Card Mgmt           | Low        |
| 21| CBACT04C   | 652   | Interest calculation on accounts                   | Financial / Interest| Medium     |
| 22| CBCUS01C   | 178   | Read and print customer data file                  | Customer Mgmt       | Low        |
| 23| CBTRN01C   | 494   | Post records from daily transaction file            | Transaction Posting | Medium     |
| 24| CBTRN02C   | 731   | Post daily transactions (main posting engine)      | Transaction Posting | High       |
| 25| CBTRN03C   | 649   | Print transaction detail report                    | Reporting           | Medium     |
| 26| CBSTM03A   | 924   | Statement generation (main driver)                 | Statements          | High       |
| 27| CBSTM03B   | 230   | Statement generation (print subroutine)            | Statements          | Low        |
| 28| CBEXPORT   | 582   | Export data from VSAM to sequential files           | Data Export         | Medium     |
| 29| CBIMPORT   | 487   | Import data from sequential files to VSAM           | Data Import         | Medium     |

### 1.3 Utility Programs

| # | Program    | Lines | Function                                   | Business Domain | Complexity |
|---|------------|-------|--------------------------------------------|-----------------|------------|
| 30| COBSWAIT   | 41    | Wait utility (accepts centiseconds in PARM)| Utility         | Trivial    |
| 31| CSUTLDTC   | 157   | Date validation (calls CEEDAYS)            | Utility / Date  | Low        |

### 1.4 Assembler Programs &mdash; `app/asm/`

| # | Program    | Function                               |
|---|------------|----------------------------------------|
| 1 | COBDATFT   | Date formatting utility (called from CBACT01C) |
| 2 | MVSWAIT    | Low-level wait routine (called from COBSWAIT)  |

---

## 2. Optional Module Programs

### 2.1 Authorization Module (IMS/DB2/MQ) &mdash; `app/app-authorization-ims-db2-mq/cbl/`

| # | Program    | Lines  | Type                  | Function                                  |
|---|------------|--------|-----------------------|-------------------------------------------|
| 1 | COPAUA0C   | 1,026  | CICS / IMS / MQ       | Card authorization decision (MQ trigger)  |
| 2 | COPAUS0C   | 1,032  | CICS / IMS / BMS      | Authorization summary view                |
| 3 | COPAUS1C   | 604    | CICS / IMS / BMS      | Authorization detail view                 |
| 4 | COPAUS2C   | 244    | CICS / IMS / DB2      | Mark authorization as fraud (DB2 write)   |
| 5 | CBPAUP0C   | 386    | Batch / IMS           | Purge expired authorization messages      |
| 6 | DBUNLDGS   | 366    | Batch / IMS           | Unload IMS segments to flat file          |
| 7 | PAUDBLOD   | 369    | Batch / IMS           | Load authorization IMS database           |
| 8 | PAUDBUNL   | 317    | Batch / IMS           | Unload authorization IMS database         |

### 2.2 Transaction Type DB2 Module &mdash; `app/app-transaction-type-db2/cbl/`

| # | Program    | Lines  | Type             | Function                                    |
|---|------------|--------|------------------|---------------------------------------------|
| 1 | COTRTLIC   | 2,098  | CICS / DB2       | List/delete transaction types (DB2 cursors) |
| 2 | COTRTUPC   | 1,702  | CICS / DB2       | Add/edit transaction types                  |
| 3 | COBTUPDT   | 237    | Batch / DB2      | Batch update transaction types              |

### 2.3 VSAM-MQ Module &mdash; `app/app-vsam-mq/cbl/`

| # | Program    | Lines  | Type             | Function                                    |
|---|------------|--------|------------------|---------------------------------------------|
| 1 | CODATE01   | 524    | CICS / MQ        | System date service (MQ request/response)   |
| 2 | COACCT01   | 620    | CICS / MQ        | Account inquiry service (MQ request/response)|

---

## 3. Copybooks &mdash; `app/cpy/`

### 3.1 Data Record Layouts (CV* prefix)

| # | Copybook   | Record Name           | Record Len | Business Entity            |
|---|------------|-----------------------|------------|----------------------------|
| 1 | CVACT01Y   | ACCOUNT-RECORD        | 300 bytes  | Account Master             |
| 2 | CVACT02Y   | CARD-RECORD           | 150 bytes  | Card Master                |
| 3 | CVACT03Y   | CARD-XREF-RECORD      | 50 bytes   | Card-Account Cross Reference|
| 4 | CVCUS01Y   | CUSTOMER-RECORD       | 500 bytes  | Customer Master            |
| 5 | CVCRD01Y   | CC-WORK-AREA          | N/A        | Credit Card Work Area      |
| 6 | CVTRA01Y   | TRAN-CAT-BAL-RECORD   | 50 bytes   | Transaction Category Balance|
| 7 | CVTRA02Y   | DIS-GROUP-RECORD      | 50 bytes   | Disclosure Group           |
| 8 | CVTRA03Y   | TRAN-TYPE-RECORD      | 60 bytes   | Transaction Type           |
| 9 | CVTRA04Y   | TRAN-CAT-RECORD       | 60 bytes   | Transaction Category       |
| 10| CVTRA05Y   | TRAN-RECORD           | 350 bytes  | Transaction (Online)       |
| 11| CVTRA06Y   | DALYTRAN-RECORD       | 350 bytes  | Daily Transaction          |
| 12| CVTRA07Y   | REPORT-NAME-HEADER    | N/A        | Transaction Report Layout  |
| 13| CVEXPORT   | N/A                   | N/A        | Export Record Layouts      |
| 14| CUSTREC    | N/A                   | N/A        | Customer Record (Statement)|
| 15| COSTM01    | TRNX-RECORD           | 350 bytes  | Transaction (Statement)    |
| 16| UNUSED1Y   | UNUSED-DATA           | 80 bytes   | Unused/Reserved            |

### 3.2 Application Control Copybooks (CO*/CS* prefix)

| # | Copybook   | Purpose                                            |
|---|------------|----------------------------------------------------|
| 1 | COCOM01Y   | Common COMMAREA structure (application state)      |
| 2 | COADM02Y   | Admin menu option definitions                      |
| 3 | COMEN02Y   | Regular user menu option definitions               |
| 4 | COTTL01Y   | Screen title and header constants                  |
| 5 | CSDAT01Y   | Current date/time working storage                  |
| 6 | CSMSG01Y   | Common message constants                           |
| 7 | CSMSG02Y   | Abend handling variables and messages              |
| 8 | CSUSR01Y   | User security record layout                        |
| 9 | CSSETATY   | Set attribute bytes (screen field highlighting)    |
| 10| CSSTRPFY   | Store PF-key processing logic                      |
| 11| CSLKPCDY   | Lookup code values                                 |
| 12| CSUTLDPY   | Date utility parameters                            |
| 13| CSUTLDWY   | Date validation working storage                    |
| 14| CODATECN   | Date conversion record layout                      |

### 3.3 Optional Module Copybooks

**Authorization (IMS/DB2/MQ)** &mdash; `app/app-authorization-ims-db2-mq/cpy/`

| Copybook   | Purpose                                    |
|------------|--------------------------------------------|
| CCPAUERY   | Authorization error handling                |
| CCPAURLY   | Authorization request/reply layout          |
| CCPAURQY   | Authorization request layout                |
| CIPAUDTY   | Authorization IMS detail segment            |
| CIPAUSMY   | Authorization IMS summary segment           |
| IMSFUNCS   | IMS function code constants                 |
| PADFLPCB   | IMS PCB for PAUT database (detail)          |
| PASFLPCB   | IMS PCB for PAUT database (summary)         |
| PAUTBPCB   | IMS PCB for PAUT database (base)            |

**Transaction Type DB2** &mdash; `app/app-transaction-type-db2/cpy/`

| Copybook   | Purpose                                    |
|------------|--------------------------------------------|
| CSDB2RPY   | DB2 SQLCA return code handling              |
| CSDB2RWY   | DB2 read/write working storage              |

---

## 4. BMS Maps &mdash; `app/bms/`

| # | Map Set    | Map Name  | Associated Program | Screen Function                      |
|---|------------|-----------|-------------------|--------------------------------------|
| 1 | COSGN00    | COSGN0A   | COSGN00C          | Sign-on Screen                       |
| 2 | COMEN01    | COMEN1A   | COMEN01C          | Main Menu (Regular User)             |
| 3 | COADM01    | COADM1A   | COADM01C          | Admin Menu                           |
| 4 | COACTVW    | CACTVWA   | COACTVWC          | Account View                         |
| 5 | COACTUP    | CACTVPA   | COACTUPC          | Account Update                       |
| 6 | COCRDLI    | CCRDLIA   | COCRDLIC          | Credit Card List                     |
| 7 | COCRDSL    | CCRDSLA   | COCRDSLC          | Credit Card Detail View              |
| 8 | COCRDUP    | CCRDUPA   | COCRDUPC          | Credit Card Update                   |
| 9 | COTRN00    | COTRN0A   | COTRN00C          | Transaction List                     |
| 10| COTRN01    | COTRN1A   | COTRN01C          | Transaction View                     |
| 11| COTRN02    | COTRN2A   | COTRN02C          | Transaction Add                      |
| 12| CORPT00    | CORPT0A   | CORPT00C          | Transaction Report Options           |
| 13| COBIL00    | COBIL0A   | COBIL00C          | Bill Payment                         |
| 14| COUSR00    | COUSR0A   | COUSR00C          | User List (Admin)                    |
| 15| COUSR01    | COUSR1A   | COUSR01C          | User Add (Admin)                     |
| 16| COUSR02    | COUSR2A   | COUSR02C          | User Update (Admin)                  |
| 17| COUSR03    | COUSR3A   | COUSR03C          | User Delete (Admin)                  |

### Optional Module BMS Maps

| Map Set    | Module                    | Screen Function                  |
|------------|---------------------------|----------------------------------|
| COPAU00    | Authorization (IMS/DB2/MQ)| Authorization Summary            |
| COPAU01    | Authorization (IMS/DB2/MQ)| Authorization Detail             |
| COTRTLI    | Transaction Type (DB2)    | Transaction Type List            |
| COTRTUP    | Transaction Type (DB2)    | Transaction Type Update          |

---

## 5. JCL Jobs &mdash; `app/jcl/`

### 5.1 Data Loading / Refresh Jobs

| # | JCL Job    | Programs Executed        | Function                                      |
|---|------------|--------------------------|-----------------------------------------------|
| 1 | ACCTFILE   | IDCAMS                   | Refresh account master VSAM KSDS              |
| 2 | CARDFILE   | IDCAMS                   | Refresh card master VSAM KSDS                 |
| 3 | CUSTFILE   | IDCAMS                   | Refresh customer master VSAM KSDS             |
| 4 | XREFFILE   | IDCAMS                   | Load card-account cross-reference VSAM        |
| 5 | TRANFILE   | SDSF, IDCAMS             | Load transaction master VSAM KSDS + AIX       |
| 6 | DUSRSECJ   | IEFBR14, IEBGENER, IDCAMS| Load user security VSAM from inline data     |
| 7 | ESDSRRDS   | IEFBR14, IEBGENER, IDCAMS| Load ESDS and RRDS datasets (demo)           |
| 8 | DEFCUST    | IDCAMS                   | Define customer VSAM cluster                  |

### 5.2 Batch Processing Jobs

| # | JCL Job    | Programs Executed        | Function                                      |
|---|------------|--------------------------|-----------------------------------------------|
| 9 | POSTTRAN   | CBTRN02C                 | Post daily transactions to master              |
| 10| INTCALC    | CBACT04C                 | Calculate interest on accounts                 |
| 11| COMBTRAN   | IDCAMS (REPRO)           | Combine daily + master transactions            |
| 12| CREASTMT   | SORT, IDCAMS, CBSTM03A   | Generate customer statements                   |
| 13| TRANREPT   | REPROC, SORT, CBTRN03C   | Generate transaction detail report             |

### 5.3 CICS File Management Jobs

| # | JCL Job    | Programs Executed  | Function                                      |
|---|------------|--------------------|-----------------------------------------------|
| 14| CLOSEFIL   | SDSF               | Close CICS files for batch processing          |
| 15| OPENFIL    | SDSF               | Reopen CICS files after batch processing       |

### 5.4 Data Maintenance / Utility Jobs

| # | JCL Job    | Programs Executed        | Function                                      |
|---|------------|--------------------------|-----------------------------------------------|
| 16| TRANBKP    | REPROC, IDCAMS           | Backup transaction VSAM to GDG                |
| 17| TRANIDX    | IDCAMS                   | Define/rebuild transaction alternate index    |
| 18| DEFGDGB    | IDCAMS                   | Define GDG base for backups                    |
| 19| DEFGDGD    | IDCAMS, IEBGENER         | Define GDG + load tran type/category/discount |
| 20| TCATBALF   | IDCAMS                   | Define/load transaction category balance VSAM |
| 21| TRANCATG   | IDCAMS                   | Define/load transaction category VSAM         |
| 22| TRANTYPE   | IDCAMS                   | Define/load transaction type VSAM             |
| 23| DISCGRP    | IDCAMS                   | Define/load disclosure group VSAM             |
| 24| REPTFILE   | IDCAMS                   | Define report output VSAM                      |
| 25| DALYREJS   | N/A                      | Define daily rejects output file               |
| 26| PRTCATBL   | IEFBR14, REPROC, SORT    | Print transaction category balance report     |
| 27| WAITSTEP   | COBSWAIT                 | Controlled wait step (job scheduling)          |

### 5.5 Data Read / Print Jobs

| # | JCL Job    | Programs Executed  | Function                                      |
|---|------------|--------------------|-----------------------------------------------|
| 28| READACCT   | IEFBR14, CBACT01C  | Read account file, produce output files        |
| 29| READCARD   | CBACT02C           | Read and print card data                       |
| 30| READCUST   | CBCUS01C           | Read and print customer data                   |
| 31| READXREF   | CBACT03C           | Read and print cross-reference data            |

### 5.6 Data Export / Import Jobs

| # | JCL Job    | Programs Executed  | Function                                      |
|---|------------|--------------------|-----------------------------------------------|
| 32| CBEXPORT   | CBEXPORT           | Export VSAM data to sequential files           |
| 33| CBIMPORT   | CBIMPORT           | Import sequential files to VSAM                |

### 5.7 Infrastructure / Utility Jobs

| # | JCL Job    | Programs Executed  | Function                                      |
|---|------------|--------------------|-----------------------------------------------|
| 34| CBADMCDJ   | N/A                | Admin card job (placeholder/utility)           |
| 35| FTPJCL     | FTP                | FTP file transfer                              |
| 36| INTRDRJ1   | IDCAMS, IEBGENER   | Internal reader test (submit chained job)      |
| 37| INTRDRJ2   | IDCAMS             | Internal reader test (chained target)          |
| 38| TXT2PDF1   | IKJEFT1B (TXT2PDF) | Convert text statement to PDF                  |

### 5.8 Optional Module JCL Jobs

**Authorization (IMS/DB2/MQ)** &mdash; `app/app-authorization-ims-db2-mq/jcl/`

| JCL Job    | Function                                      |
|------------|-----------------------------------------------|
| CBPAUP0J   | Purge expired authorizations                   |
| DBPAUTP0   | Authorization DB2 table maintenance            |
| LOADPADB   | Load IMS authorization database                |
| UNLDGSAM   | Unload GSAM segments                           |
| UNLDPADB   | Unload IMS authorization database              |

**Transaction Type DB2** &mdash; `app/app-transaction-type-db2/jcl/`

| JCL Job    | Function                                      |
|------------|-----------------------------------------------|
| CREADB21   | Create DB2 transaction type tables             |
| MNTTRDB2   | Maintain DB2 transaction type records          |
| TRANEXTR   | Extract transaction types from DB2             |

---

## 6. Supporting Artifacts

### 6.1 JCL Procedures &mdash; `app/proc/`

| Procedure  | Function                                      |
|------------|-----------------------------------------------|
| REPROC     | Generic REPRO (copy VSAM to sequential)       |
| TRANREPT   | Transaction report generation procedure        |

### 6.2 Assembler Macros &mdash; `app/maclib/`

| Macro      | Function                                      |
|------------|-----------------------------------------------|
| ASMWAIT    | Wait macro (used by MVSWAIT)                  |
| COCDATFT   | Date formatting macro (used by COBDATFT)      |

### 6.3 Control Files &mdash; `app/ctl/`

| File       | Function                                      |
|------------|-----------------------------------------------|
| REPROCT    | REPRO control card for VSAM copy operations   |

### 6.4 CSD Definitions &mdash; `app/csd/`

| File           | Function                                  |
|----------------|-------------------------------------------|
| CARDDEMO.CSD   | CICS Resource Definitions (programs, files, transactions, maps) |

### 6.5 Scheduler Configurations &mdash; `app/scheduler/`

| File               | Function                              |
|--------------------|---------------------------------------|
| CardDemo.ca7       | CA-7 job scheduling definitions       |
| CardDemo.controlm  | Control-M job scheduling definitions  |

---

## 7. Batch Processing Cycle

The standard nightly batch cycle executes in this order:

```
1. CLOSEFIL    ─── Close CICS files for exclusive batch access
2. ACCTFILE    ─── Refresh account master
3. CARDFILE    ─── Refresh card master
4. CUSTFILE    ─── Refresh customer master
5. XREFFILE    ─── Refresh cross-reference
6. TRANFILE    ─── Refresh transaction master
7. POSTTRAN    ─── Post daily transactions (CBTRN02C)
8. INTCALC     ─── Calculate interest (CBACT04C)
9. TRANBKP     ─── Backup transactions to GDG
10. COMBTRAN   ─── Combine transaction files
11. CREASTMT   ─── Generate statements (CBSTM03A → CBSTM03B)
12. TRANIDX    ─── Rebuild alternate indexes
13. OPENFIL    ─── Reopen CICS files
```

---

## 8. Naming Conventions

| Prefix | Meaning                           | Examples           |
|--------|-----------------------------------|--------------------|
| CO*    | Online CICS program               | COSGN00C, COMEN01C |
| CB*    | Batch COBOL program               | CBTRN02C, CBACT04C |
| CS*    | Common/shared copybook or utility | CSDAT01Y, CSUTLDTC |
| CV*    | VSAM data record layout copybook  | CVACT01Y, CVTRA05Y |
| CC*    | Credit card work area / common    | CVCRD01Y           |
| *Y     | Copybook suffix                   | COCOM01Y, COTTL01Y |
