# Application Inventory — CardDemo COBOL Estate

> Generated from static analysis of the CardDemo application source code.

---

## Table of Contents

1. [Main Application Programs (app/cbl/)](#1-main-application-programs)
2. [Sub-Application: Authorization IMS-DB2-MQ (app/app-authorization-ims-db2-mq/cbl/)](#2-sub-application-authorization-ims-db2-mq)
3. [Sub-Application: Transaction Type DB2 (app/app-transaction-type-db2/cbl/)](#3-sub-application-transaction-type-db2)
4. [Sub-Application: VSAM-MQ (app/app-vsam-mq/cbl/)](#4-sub-application-vsam-mq)
5. [JCL Job Catalog (app/jcl/)](#5-jcl-job-catalog-appjcl)
6. [Sub-Application JCL Jobs](#6-sub-application-jcl-jobs)

---

## 1. Main Application Programs

### Online (CICS) Programs — `app/cbl/`

| # | Filename | Lines | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|----------------|-------------------|---------------------|
| 1 | **COSGN00C.cbl** | 260 | Sign-on / login screen. Validates user credentials against the USRSEC file and initiates the CICS session. | Online (CICS) | CICS SEND/RECEIVE MAP (COSGN00), READ USRSEC file | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 2 | **COMEN01C.cbl** | 308 | Main menu controller. Displays the primary navigation menu and routes to sub-functions based on user selection. | Online (CICS) | CICS SEND/RECEIVE MAP (COMEN01), XCTL to target programs | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 3 | **COADM01C.cbl** | 288 | Admin menu screen. Displays the administration sub-menu for user and system management functions. | Online (CICS) | CICS SEND/RECEIVE MAP (COADM01), XCTL to admin programs | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| 4 | **COACTUPC.cbl** | 4,236 | Account update (detail). The largest online program — handles full CRUD operations on account records. Validates all fields, manages screen navigation, and persists changes to VSAM KSDS files. | Online (CICS) | READ/REWRITE ACCTDAT, READ CUSTDAT, READ CARDXREF, READ CARDDAT; CICS SEND/RECEIVE MAP (COACTUP) | COCOM01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 5 | **COACTVWC.cbl** | 941 | Account view. Read-only display of account details with card cross-reference lookup and customer information. | Online (CICS) | READ ACCTDAT, READ CUSTDAT, READ CARDXREF, READ CARDDAT; CICS SEND/RECEIVE MAP (COACTVW) | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 6 | **COCRDLIC.cbl** | 1,459 | Credit card list. Displays a scrollable list of credit cards with filtering, pagination (forward/backward), and drill-down to detail/update screens. | Online (CICS) | READ CARDDAT, READ CARDXREF (via alt index), STARTBR/READNEXT/READPREV/ENDBR; CICS SEND/RECEIVE MAP (COCRDLI) | COCOM01Y, CVCRD01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 7 | **COCRDSLC.cbl** | 887 | Credit card search. Provides account-number or card-number search and displays matching card records. | Online (CICS) | READ CARDXREF, READ ACCTDAT, READ CARDDAT; CICS SEND/RECEIVE MAP (COCRDSL) | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 8 | **COCRDUPC.cbl** | 1,560 | Credit card update. Full detail screen for updating card attributes — expiry date, status, and limits. Includes field-level validation. | Online (CICS) | READ/REWRITE CARDDAT, READ CARDXREF, READ ACCTDAT, READ CUSTDAT; CICS SEND/RECEIVE MAP (COCRDUP) | CVCRD01Y, COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 9 | **COTRN00C.cbl** | 699 | Transaction list. Displays scrollable list of transactions with card-number filtering and pagination. | Online (CICS) | READ TRANSACT (sequential browse), STARTBR/READNEXT/READPREV/ENDBR; CICS SEND/RECEIVE MAP (COTRN00) | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 10 | **COTRN01C.cbl** | 330 | Transaction add. Screen for entering new transaction details, with data validation before write. | Online (CICS) | WRITE TRANSACT, WRITE DALYTRAN; CICS SEND/RECEIVE MAP (COTRN01) | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 11 | **COTRN02C.cbl** | 783 | Transaction detail/update. Displays and allows modification of individual transaction records. Includes date validation via CSUTLDTC. | Online (CICS) | READ/REWRITE TRANSACT; CICS SEND/RECEIVE MAP (COTRN02) | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| 12 | **COBIL00C.cbl** | 572 | Bill payment processing. Manages bill payment entry and processing for card accounts. | Online (CICS) | READ/WRITE TRANSACT, READ ACCTDAT, READ CARDXREF; CICS SEND/RECEIVE MAP (COBIL00) | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 13 | **CORPT00C.cbl** | 649 | Report request screen. Allows users to specify date ranges and parameters for transaction reporting. Calls CSUTLDTC for date conversion. | Online (CICS) | READ TRANSACT; CICS SEND/RECEIVE MAP (CORPT00) | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 14 | **COUSR00C.cbl** | 695 | User list. Displays scrollable list of security/user records with search and pagination. | Online (CICS) | READ USRSEC (sequential browse), STARTBR/READNEXT/READPREV/ENDBR; CICS SEND/RECEIVE MAP (COUSR00) | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 15 | **COUSR01C.cbl** | 299 | User add. Screen for creating new user security records. | Online (CICS) | WRITE USRSEC; CICS SEND/RECEIVE MAP (COUSR01) | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 16 | **COUSR02C.cbl** | 414 | User update. Allows modification of user credentials and access types. | Online (CICS) | READ/REWRITE USRSEC; CICS SEND/RECEIVE MAP (COUSR02) | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 17 | **COUSR03C.cbl** | 359 | User delete. Handles user record deletion with confirmation prompts. | Online (CICS) | READ/DELETE USRSEC; CICS SEND/RECEIVE MAP (COUSR03) | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 18 | **CSUTLDTC.cbl** | 157 | Date utility subroutine. Converts dates between formats using LE callable service CEEDAYS. Called by COTRN02C, CORPT00C. | Online (Utility) | CALL CEEDAYS (LE runtime) | *(none — self-contained)* |

### Batch Programs — `app/cbl/`

| # | Filename | Lines | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|----------------|-------------------|---------------------|
| 19 | **CBACT01C.cbl** | 430 | Account file batch read. Sequentially reads the account VSAM KSDS file, formats dates via COBDATFT, and produces account listing output. | Batch | OPEN/READ/CLOSE ACCTFILE (VSAM KSDS); CALL COBDATFT | CVACT01Y, CVACT03Y, CSDAT01Y, CVCUS01Y, CODATECN |
| 20 | **CBACT02C.cbl** | 178 | Card cross-reference file batch read. Reads card-to-account cross-reference VSAM file sequentially. | Batch | OPEN/READ/CLOSE CARDXREF (VSAM KSDS) | CVACT02Y |
| 21 | **CBACT03C.cbl** | 178 | Card data file batch read. Reads the card master VSAM file sequentially. | Batch | OPEN/READ/CLOSE CARDFILE (VSAM KSDS) | CVCRD01Y |
| 22 | **CBACT04C.cbl** | 652 | Account data cross-reference batch processor. Reads account data cross-reference file via alternate index and correlates accounts. | Batch | OPEN/READ/CLOSE ACCTFILE (VSAM KSDS via AIX) | CVACT01Y, CVACT03Y |
| 23 | **CBCUS01C.cbl** | 178 | Customer file batch read. Sequentially reads the customer master VSAM file. | Batch | OPEN/READ/CLOSE CUSTFILE (VSAM KSDS) | CVCUS01Y |
| 24 | **CBTRN01C.cbl** | 494 | Transaction file batch read. Reads the transaction history VSAM KSDS file sequentially with formatting. | Batch | OPEN/READ/CLOSE TRANSACT (VSAM KSDS) | CVTRA05Y |
| 25 | **CBTRN02C.cbl** | 731 | Transaction category balance batch reader. Reads the transaction-category-balance file and the disclosure group file. | Batch | OPEN/READ/CLOSE TCATBALF, OPEN/READ/CLOSE DISCGRP (VSAM KSDS) | CVTRA01Y, CVTRA02Y |
| 26 | **CBTRN03C.cbl** | 649 | Daily transaction batch reader. Reads daily transaction file and transaction type / category reference files. | Batch | OPEN/READ/CLOSE DALYTRAN, OPEN/READ/CLOSE TRANTYPE, OPEN/READ/CLOSE TRANCATG (VSAM KSDS) | CVTRA03Y, CVTRA04Y, CVTRA06Y |
| 27 | **CBEXPORT.cbl** | 582 | Data export utility. Reads multiple VSAM files and exports records to a sequential flat file for external consumption. | Batch | OPEN/READ/CLOSE ACCTFILE, CUSTFILE, CARDFILE, CARDXREF, USRSEC, TRANSACT; WRITE EXPFILE | CVEXPORT, CVACT01Y, CVCUS01Y, CVCRD01Y, CVACT02Y, CSUSR01Y, CVTRA05Y |
| 28 | **CBIMPORT.cbl** | 487 | Data import utility. Reads a sequential flat file and loads records into multiple VSAM files. | Batch | READ IMPFILE; WRITE ACCTFILE, CUSTFILE, CARDFILE, CARDXREF, USRSEC, TRANSACT | CVEXPORT, CVACT01Y, CVCUS01Y, CVCRD01Y, CVACT02Y, CSUSR01Y, CVTRA05Y |
| 29 | **CBSTM03A.CBL** | 924 | Statement creation — main driver. Reads transaction, cross-reference, customer, and account files; produces text and HTML statement reports per card. Calls CBSTM03B for file I/O. | Batch | CALL CBSTM03B for all file ops; WRITE STMTFILE, WRITE HTMLFILE | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| 30 | **CBSTM03B.CBL** | 230 | Statement creation — file I/O subroutine. Handles OPEN/READ/CLOSE for TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE on behalf of CBSTM03A. | Batch (Subroutine) | OPEN/READ/CLOSE TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (all VSAM KSDS) | *(none — FD definitions inline)* |
| 31 | **COBSWAIT.cbl** | 41 | Wait subroutine. Calls assembler routine MVSWAIT to pause batch execution for a specified interval. | Batch (Utility) | CALL MVSWAIT | *(none)* |

---

## 2. Sub-Application: Authorization IMS-DB2-MQ

### `app/app-authorization-ims-db2-mq/cbl/`

| # | Filename | Lines | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|----------------|-------------------|---------------------|
| 32 | **COPAUA0C.cbl** | 1,026 | Pending authorization MQ processor. Reads authorization requests from MQ input queue, processes against IMS DB and VSAM files, and writes responses to MQ reply queue. | Online (CICS + MQ) | MQOPEN, MQGET, MQPUT1, MQCLOSE; READ ACCTDAT, CUSTDAT, CARDXREF files | CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y, CVCUS01Y |
| 33 | **COPAUS0C.cbl** | 1,032 | Pending authorization summary list. CICS screen displaying summary of pending authorization records from IMS DB. | Online (CICS) | CICS SEND/RECEIVE MAP (COPAU00), READ IMS segments | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 34 | **COPAUS1C.cbl** | 604 | Pending authorization detail view. Displays detail of a single pending authorization record. | Online (CICS) | CICS SEND/RECEIVE MAP (COPAU01), READ IMS segments | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 35 | **COPAUS2C.cbl** | 244 | Pending authorization decision. Processes approve/reject decision on a pending authorization. | Online (CICS) | CICS SEND/RECEIVE; UPDATE IMS segment | CIPAUDTY |
| 36 | **CBPAUP0C.cbl** | 386 | Pending authorization batch purge. Batch program that reads IMS pending authorization DB and purges expired records. | Batch | READ IMS DB (GN/GNP calls via CBLTDLI), DELETE segments | CIPAUSMY, CIPAUDTY |
| 37 | **DBUNLDGS.CBL** | 366 | IMS GSAM database unload. Unloads IMS database to flat files using GSAM access. | Batch (IMS) | CBLTDLI GN/GNP/ISRT calls; OPEN/WRITE/CLOSE flat files | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB, PASFLPCB, PADFLPCB |
| 38 | **PAUDBLOD.CBL** | 369 | IMS pending auth DB load. Loads pending authorization records from flat files into IMS database. | Batch (IMS) | OPEN/READ flat files; CBLTDLI ISRT/GU calls | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |
| 39 | **PAUDBUNL.CBL** | 317 | IMS pending auth DB unload. Unloads IMS pending authorization database segments to flat files. | Batch (IMS) | CBLTDLI GN/GNP calls; OPEN/WRITE/CLOSE flat files | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |

---

## 3. Sub-Application: Transaction Type DB2

### `app/app-transaction-type-db2/cbl/`

| # | Filename | Lines | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|----------------|-------------------|---------------------|
| 40 | **COTRTLIC.cbl** | 2,098 | Transaction type list (DB2). CICS screen listing transaction types from DB2 TRANSACTION_TYPE table. Supports scrollable cursor, search, and in-line delete. | Online (CICS + DB2) | EXEC SQL SELECT/DELETE/OPEN/CLOSE/FETCH on CARDDEMO.TRANSACTION_TYPE; CICS SEND/RECEIVE MAP (COTRTLI) | CVCRD01Y, COCOM01Y, COTTL01Y, COTRTLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, DFHAID, DFHBMSCA, CSSTRPFY, CSDB2RWY, CSDB2RPY, DCLTRTYP |
| 41 | **COTRTUPC.cbl** | 1,702 | Transaction type update (DB2). CICS detail screen for adding, updating, and deleting transaction type records in DB2. | Online (CICS + DB2) | EXEC SQL SELECT/UPDATE/INSERT/DELETE on CARDDEMO.TRANSACTION_TYPE and TRANSACTION_TYPE_CATEGORY; CICS SEND/RECEIVE MAP (COTRTUP), SYNCPOINT | CVCRD01Y, COCOM01Y, COTTL01Y, COTRTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, DFHAID, DFHBMSCA, CSSTRPFY, CSSETATY, CSUTLDWY, DCLTRTYP, DCLTRCAT, CSDB2RWY, CSDB2RPY |
| 42 | **COBTUPDT.cbl** | 237 | Transaction type batch maintenance (DB2). Reads an input file with Add/Update/Delete directives and applies them to the DB2 TRANSACTION_TYPE table. | Batch (DB2) | OPEN/READ/CLOSE input file (TR-RECORD); EXEC SQL INSERT/UPDATE/DELETE on CARDDEMO.TRANSACTION_TYPE | DCLTRTYP |

---

## 4. Sub-Application: VSAM-MQ

### `app/app-vsam-mq/cbl/`

| # | Filename | Lines | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|----------------|-------------------|---------------------|
| 43 | **COACCT01.cbl** | 620 | Account inquiry via MQ. CICS program that receives account inquiry requests from MQ, reads account data from VSAM, and puts response messages back on MQ reply queue. | Online (CICS + MQ) | MQOPEN/MQGET/MQPUT/MQCLOSE on 3 queues; CICS READ ACCTDAT; EXEC CICS RETRIEVE | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML, CVACT01Y |
| 44 | **CODATE01.cbl** | 524 | Date/time service via MQ. CICS program that receives date requests from MQ, obtains system date/time via CICS ASKTIME/FORMATTIME, and puts formatted response on MQ reply queue. | Online (CICS + MQ) | MQOPEN/MQGET/MQPUT/MQCLOSE on 3 queues; EXEC CICS ASKTIME/FORMATTIME; EXEC CICS RETRIEVE | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML |

---

## 5. JCL Job Catalog — `app/jcl/`

| # | Job Name | JCL File | Step Sequence | Purpose |
|---|----------|----------|---------------|---------|
| 1 | **ACCTFILE** | ACCTFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM), STEP20 (IDCAMS DEFINE AIX), STEP25 (IDCAMS DEFINE PATH), STEP30 (IDCAMS BLDINDEX) | Define and load Account data VSAM KSDS with alternate index on customer ID |
| 2 | **CARDFILE** | CARDFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM), STEP20 (IDCAMS DEFINE AIX), STEP25 (IDCAMS DEFINE PATH), STEP30 (IDCAMS BLDINDEX) | Define and load Card data VSAM KSDS with alternate index on account ID |
| 3 | **CUSTFILE** | CUSTFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Customer data VSAM KSDS |
| 4 | **XREFFILE** | XREFFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM), STEP20 (IDCAMS DEFINE AIX), STEP25 (IDCAMS DEFINE PATH), STEP30 (IDCAMS BLDINDEX) | Define and load Card Cross-Reference VSAM KSDS with alternate index on account ID |
| 5 | **TRANFILE** | TRANFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Transaction history VSAM KSDS |
| 6 | **DISCGRP** | DISCGRP.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Disclosure Group VSAM KSDS |
| 7 | **TCATBALF** | TCATBALF.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Transaction Category Balance VSAM KSDS |
| 8 | **TRANTYPE** | TRANTYPE.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Transaction Type reference VSAM KSDS |
| 9 | **TRANCATG** | TRANCATG.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Transaction Category reference VSAM KSDS |
| 10 | **DUSRSECJ** | DUSRSECJ.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load User Security VSAM KSDS |
| 11 | **DEFCUST** | DEFCUST.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS) | Define Customer VSAM cluster (no load) |
| 12 | **DEFGDGB** | DEFGDGB.jcl | STEP10 (IDCAMS DEFINE GDG base) | Define Generation Data Group base for backup |
| 13 | **DEFGDGD** | DEFGDGD.jcl | STEP10 (IDCAMS DELETE GDG base) | Delete Generation Data Group base |
| 14 | **ESDSRRDS** | ESDSRRDS.jcl | STEP05 (IDCAMS DEFINE ESDS), STEP10 (IDCAMS DEFINE RRDS) | Define ESDS and RRDS VSAM clusters for specialized access |
| 15 | **DALYREJS** | DALYREJS.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (IDCAMS REPRO flat→VSAM) | Define and load Daily Rejection file |
| 16 | **POSTTRAN** | POSTTRAN.jcl | STEP05 (IDCAMS DELETE/DEFINE KSDS), STEP10 (SORT), STEP15 (PGM=CBTRN02C or batch equivalent) | Post daily transactions: sort and process into transaction files |
| 17 | **INTCALC** | INTCALC.jcl | STEP05 (PGM=CBTRN03C or equivalent) | Calculate interest on transaction category balances |
| 18 | **COMBTRAN** | COMBTRAN.jcl | STEP05 (SORT merge), STEP10 (IDCAMS REPRO) | Combine daily transactions into master transaction file |
| 19 | **TRANREPT** | TRANREPT.jcl | STEP05 (SORT), STEP10 (PGM=report program) | Generate daily transaction report |
| 20 | **TRANBKP** | TRANBKP.jcl | STEP10 (IEBGENER copy) | Backup transaction file to GDG |
| 21 | **TRANIDX** | TRANIDX.jcl | STEP10 (IDCAMS BLDINDEX) | Rebuild alternate indexes on transaction file |
| 22 | **PRTCATBL** | PRTCATBL.jcl | STEP05 (IDCAMS PRINT) | Print transaction category balance file contents |
| 23 | **REPTFILE** | REPTFILE.jcl | STEP05 (IDCAMS DELETE/DEFINE), STEP10 (data load) | Define and load Report output file |
| 24 | **READACCT** | READACCT.jcl | STEP10 (PGM=CBACT01C) | Run batch account file reader |
| 25 | **READCARD** | READCARD.jcl | STEP10 (PGM=CBACT03C) | Run batch card file reader |
| 26 | **READCUST** | READCUST.jcl | STEP10 (PGM=CBCUS01C) | Run batch customer file reader |
| 27 | **READXREF** | READXREF.jcl | STEP10 (PGM=CBACT02C) | Run batch cross-reference file reader |
| 28 | **CBEXPORT** | CBEXPORT.jcl | STEP10 (PGM=CBEXPORT) | Run data export from VSAM to flat file |
| 29 | **CBIMPORT** | CBIMPORT.jcl | STEP10 (PGM=CBIMPORT) | Run data import from flat file to VSAM |
| 30 | **CREASTMT** | CREASTMT.JCL | DELDEF01 (IDCAMS DEL/DEF TRXFL), STEP010 (SORT), STEP020 (IDCAMS REPRO), STEP030 (IEFBR14 cleanup), STEP040 (PGM=CBSTM03A) | Create card statements — sort transactions, build indexed file, run statement generator |
| 31 | **CBADMCDJ** | CBADMCDJ.jcl | STEP10 (PGM=admin batch) | CardDemo admin batch job |
| 32 | **CLOSEFIL** | CLOSEFIL.jcl | Multiple IDCAMS steps | Close/deallocate VSAM files for batch window |
| 33 | **OPENFIL** | OPENFIL.jcl | Multiple IDCAMS steps | Open/enable VSAM files after batch window |
| 34 | **WAITSTEP** | WAITSTEP.jcl | STEP10 (PGM=COBSWAIT) | Execute wait step between batch jobs |
| 35 | **FTPJCL** | FTPJCL.JCL | STEP1 (PGM=FTP) | FTP transfer of files to/from external systems |
| 36 | **INTRDRJ1** | INTRDRJ1.JCL | IDCAMS (REPRO backup), STEP01 (IEBGENER → internal reader) | Trigger chained JCL via internal reader |
| 37 | **INTRDRJ2** | INTRDRJ2.JCL | IDCAMS (REPRO) | Chained job triggered by INTRDRJ1 |
| 38 | **TXT2PDF1** | TXT2PDF1.JCL | TXT2PDF (PGM=IKJEFT1B with TXT2PDF REXX) | Convert statement text file to PDF |

---

## 6. Sub-Application JCL Jobs

### Authorization IMS-DB2-MQ — `app/app-authorization-ims-db2-mq/jcl/`

| # | Job Name | JCL File | Steps | Purpose |
|---|----------|----------|-------|---------|
| 39 | **CBPAUP0J** | CBPAUP0J.jcl | STEP10 (PGM=CBPAUP0C) | Run pending authorization purge batch |
| 40 | **DBPAUTP0** | DBPAUTP0.jcl | Multiple IMS utility steps | IMS pending auth DB setup and initialization |
| 41 | **LOADPADB** | LOADPADB.JCL | STEP10 (PGM=PAUDBLOD) | Load pending authorization IMS database from flat files |
| 42 | **UNLDPADB** | UNLDPADB.JCL | STEP10 (PGM=PAUDBUNL) | Unload pending authorization IMS database to flat files |
| 43 | **UNLDGSAM** | UNLDGSAM.JCL | STEP10 (PGM=DBUNLDGS) | Unload IMS GSAM database |

### Transaction Type DB2 — `app/app-transaction-type-db2/jcl/`

| # | Job Name | JCL File | Steps | Purpose |
|---|----------|----------|-------|---------|
| 44 | **CREADB21** | CREADB21.jcl | FREEPLN (free plans), CRCRDDB (DSNTIAD create DB), LDTTYPE (load TRANSACTION_TYPE), LDTCCAT (load TRANSACTION_TYPE_CATEGORY) | Create and populate DB2 transaction type tables |
| 45 | **MNTTRDB2** | MNTTRDB2.jcl | STEP1 (PGM=COBTUPDT via TSO/DB2) | Run batch transaction type maintenance against DB2 |
| 46 | **TRANEXTR** | TRANEXTR.jcl | STEP10 (IEBGENER backup TRANTYPE), STEP20 (IEBGENER backup TRANCATG), STEP30 (IEFBR14 cleanup), STEP40 (DSNTIAUL extract TRANSACTION_TYPE), STEP50 (DSNTIAUL extract TRANSACTION_TYPE_CATEGORY) | Extract DB2 transaction reference data to flat files for batch reporting |

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| Total COBOL programs | 44 |
| — Online (CICS) programs | 24 |
| — Batch programs | 18 |
| — Utility subroutines | 2 |
| Total lines of COBOL | ~30,175 |
| Total JCL jobs | 46 |
| Total copybooks (app/cpy/) | 30 |
| Total BMS map definitions | 17 |
| VSAM KSDS files | 10 |
| DB2 tables accessed | 2 |
| IMS databases accessed | 1 |
| MQ queues used | 6+ |
