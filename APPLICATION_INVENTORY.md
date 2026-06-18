# Application Inventory — CardDemo COBOL Estate

## Overview

The CardDemo estate comprises **44 COBOL programs** (31 in `app/cbl/`, 8 in `app-authorization-ims-db2-mq`, 3 in `app-transaction-type-db2`, 2 in `app-vsam-mq`) and **46 JCL jobs** (38 in `app/jcl/`, 5 in `app-authorization-ims-db2-mq/jcl`, 3 in `app-transaction-type-db2/jcl`). Programs are split between online CICS transactions and batch file-processing jobs.

---

## 1. COBOL Programs — Core (`app/cbl/`)

| # | Filename | LOC | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|----:|---------|----------------|-------------------|---------------------|
| 1 | CBACT01C.cbl | 430 | Read VSAM account file and write to multiple output formats (PS with COMP fields, ARRAY layout, VB records) | Batch | **Read:** ACCTFILE (VSAM KSDS). **Write:** OUTFILE (PS COMP), ARRYFILE (array layout), VBRCFILE (VB records). **Call:** COBDATFT (date formatter), CEE3ABD (abend) | CVACT01Y, CODATECN |
| 2 | CBACT02C.cbl | 178 | Read and print card data file | Batch | **Read:** CARDFILE (VSAM). **Call:** CEE3ABD | CVACT02Y |
| 3 | CBACT03C.cbl | 178 | Read and print cross-reference data file | Batch | **Read:** XREFFILE (VSAM). **Call:** CEE3ABD | CVACT03Y |
| 4 | CBACT04C.cbl | 652 | Interest and fee calculation — reads category balances, cross-refs, disclosure groups, computes interest/fees, updates accounts, writes transactions | Batch | **Read:** TCATBALF, XREFFILE, DISCGRP, ACCTFILE (I-O). **Write:** TRANSACT. **Rewrite:** ACCTFILE. **Call:** CEE3ABD | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 5 | CBCUS01C.cbl | 178 | Read and print customer data file | Batch | **Read:** CUSTFILE (VSAM). **Call:** CEE3ABD | CVCUS01Y |
| 6 | CBEXPORT.cbl | 582 | Export all CardDemo data (customers, accounts, xrefs, transactions, cards) into a single consolidated export file for branch migration | Batch | **Read:** CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE. **Write:** EXPFILE. **Call:** CEE3ABD | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 7 | CBIMPORT.cbl | 487 | Import data from consolidated export file back into individual entity files with validation | Batch | **Read:** EXPFILE. **Write:** CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT. **Call:** CEE3ABD | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 8 | CBSTM03A.CBL | 924 | Statement generation — reads cross-refs/customers/accounts/transactions and produces text + HTML statements | Batch | **Read:** TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (via CBSTM03B sub). **Write:** STMTFILE (text), HTMLFILE (HTML). **Call:** CBSTM03B (13 times) | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| 9 | CBSTM03B.CBL | 230 | Subroutine for CBSTM03A — handles file open/read/close/rewrite for transactions, xrefs, customers, accounts | Batch (sub) | **Read/Open/Close:** TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | *(none — uses linkage section)* |
| 10 | CBTRN01C.cbl | 494 | Daily transaction validation — reads daily transactions and validates against xref, card, account, and customer files | Batch | **Read:** DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE. **Call:** CEE3ABD | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y |
| 11 | CBTRN02C.cbl | 731 | Transaction posting — validates daily transactions, posts to transaction master, updates account balances and category balances, writes rejects | Batch | **Read:** DALYTRAN, XREFFILE, ACCTFILE (I-O), TCATBALF (I-O). **Write:** TRANFILE, DALYREJS. **Rewrite:** ACCTFILE, TCATBALF. **Call:** CEE3ABD | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| 12 | CBTRN03C.cbl | 649 | Transaction reporting — reads posted transactions, looks up xref/type/category, produces formatted report with page/grand totals | Batch | **Read:** TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM. **Write:** TRANREPT (report). **Call:** CEE3ABD | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| 13 | COACTUPC.cbl | 4236 | Online account update — CICS screen for viewing/editing account details with extensive field validation (dates, amounts, SSN, state codes, zip) | Online | **CICS READ:** CARDDAT (via AIX), ACCTDAT, CUSTDAT. **CICS REWRITE/WRITE:** account updates. **XCTL:** to menu | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY |
| 14 | COACTVWC.cbl | 941 | Online account view — CICS screen displaying account details, card cross-refs, and customer data (read-only) | Online | **CICS READ:** CARDDAT (via AIX), ACCTDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| 15 | COADM01C.cbl | 288 | Admin menu — CICS screen presenting admin options (user CRUD, transaction type management) and routing to sub-programs | Online | **CICS SEND/RECEIVE:** map. **CICS XCTL:** to selected admin program | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 16 | COBIL00C.cbl | 572 | Online bill payment — CICS screen for processing bill payments, reading account data, creating payment transactions with timestamps | Online | **CICS READ:** ACCTDAT, CXACAIX (xref). **CICS WRITE:** TRANSACT. **CICS REWRITE:** ACCTDAT. **CICS STARTBR/READPREV/ENDBR:** TRANSACT | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 17 | COBSWAIT.cbl | 41 | Batch wait utility — calls MVSWAIT assembler routine to pause execution for a specified time interval | Batch (utility) | **Call:** MVSWAIT | *(none)* |
| 18 | COCRDLIC.cbl | 1459 | Online credit card list — CICS browsable list of credit cards with forward/backward paging, selection for view or update | Online | **CICS STARTBR/READNEXT/READPREV/ENDBR:** CARDDAT. **CICS XCTL:** to COCRDSLC or COCRDUPC | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| 19 | COCRDSLC.cbl | 887 | Online credit card detail view — CICS screen showing card details and associated customer data (read-only) | Online | **CICS READ:** CARDDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 20 | COCRDUPC.cbl | 1560 | Online credit card update — CICS screen for editing card details (embossed name, status, expiry) with validation | Online | **CICS READ/REWRITE:** CARDDAT, CUSTDAT | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| 21 | COMEN01C.cbl | 308 | Main user menu — CICS screen presenting user-facing menu options (account view/update, card list, transactions, reports, bill pay) and routing | Online | **CICS SEND/RECEIVE:** map. **CICS XCTL:** to selected program | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 22 | CORPT00C.cbl | 649 | Online report request — CICS screen for selecting and submitting batch transaction reports (monthly/yearly) via TD queue | Online | **CICS WRITEQ TD:** report request. **Call:** CSUTLDTC (date utility) | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 23 | COSGN00C.cbl | 260 | Sign-on screen — CICS login program validating user ID/password against USRSEC VSAM file, routing to admin or user menu | Online | **CICS READ:** USRSEC. **CICS XCTL:** to COADM01C (admin) or COMEN01C (user) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 24 | COTRN00C.cbl | 699 | Online transaction list — CICS browsable list of transactions with forward/backward paging | Online | **CICS STARTBR/READNEXT/READPREV/ENDBR:** TRANSACT | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 25 | COTRN01C.cbl | 330 | Online transaction view — CICS screen displaying single transaction details (read-only) | Online | **CICS READ:** TRANSACT | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 26 | COTRN02C.cbl | 783 | Online transaction add — CICS screen for entering new transactions with validation, writes to TRANSACT VSAM and updates account balances | Online | **CICS READ:** TRANSACT, ACCTDAT, CXACAIX. **CICS WRITE:** TRANSACT. **CICS STARTBR/READPREV/ENDBR.** **Call:** CSUTLDTC (date utility) | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| 27 | COUSR00C.cbl | 695 | Admin user list — CICS browsable list of security users with forward/backward paging | Online | **CICS STARTBR/READNEXT/READPREV/ENDBR:** USRSEC | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 28 | COUSR01C.cbl | 299 | Admin user add — CICS screen for creating new user security records | Online | **CICS WRITE:** USRSEC | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 29 | COUSR02C.cbl | 414 | Admin user update — CICS screen for modifying existing user records (name, password, type) | Online | **CICS READ/REWRITE:** USRSEC | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 30 | COUSR03C.cbl | 359 | Admin user delete — CICS screen for removing user security records | Online | **CICS READ/DELETE:** USRSEC | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 31 | CSUTLDTC.cbl | 157 | Date utility subroutine — validates and converts dates using LE callable service CEEDAYS | Batch (utility) | **Call:** CEEDAYS (LE date service) | *(none)* |

## 2. COBOL Programs — Sub-Application: Authorization (IMS/DB2/MQ) (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Filename | LOC | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|----:|---------|----------------|-------------------|---------------------|
| 32 | CBPAUP0C.cbl | 386 | Batch purge of expired pending authorizations from IMS database with checkpoint/restart | Batch | **IMS DLI:** GN (get next summary), GNP (get next detail), DLET (delete), CHKP (checkpoint) | CIPAUSMY, CIPAUDTY |
| 33 | COPAUA0C.cbl | 1026 | Authorization request processor — reads auth requests from MQ, validates against VSAM xref/account/customer and IMS auth history, makes approve/decline decision, sends MQ reply | Online (CICS+MQ+IMS) | **MQ:** MQOPEN, MQGET, MQPUT1, MQCLOSE. **CICS READ:** CARDXREF, ACCTDAT, CUSTDAT. **IMS DLI:** SCHD, GU, REPL | CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y, CVCUS01Y |
| 34 | COPAUS0C.cbl | 1032 | Online pending authorization summary list — CICS screen with IMS browsing, shows authorization summaries by account with paging | Online (CICS+IMS) | **CICS READ:** CARDDAT, ACCTDAT, CUSTDAT. **IMS DLI:** SCHD, GU, GNP. **CICS SEND/RECEIVE** | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 35 | COPAUS1C.cbl | 604 | Online pending authorization detail view — CICS screen showing individual authorization details with option to mark as fraud | Online (CICS+IMS) | **IMS DLI:** GU, GNP, REPL. **CICS LINK:** to COPAUS2C. **CICS SYNCPOINT** | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 36 | COPAUS2C.cbl | 244 | Fraud update sub-program — CICS LINK target that records fraud flags in DB2 table with timestamps | Online (CICS+DB2) | **EXEC SQL:** INSERT/SELECT on authorization fraud table. **CICS ASKTIME/FORMATTIME** | CIPAUDTY |
| 37 | DBUNLDGS.CBL | 366 | IMS-to-GSAM unload — reads IMS auth database (root + child segments) and writes to GSAM sequential output files | Batch (IMS) | **IMS DLI:** GN (root), GNP (child). **GSAM ISRT** (write to flat files via GSAM PCBs) | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB, PASFLPCB, PADFLPCB |
| 38 | PAUDBLOD.CBL | 369 | IMS database load — reads flat files (root + child segments) and loads into IMS auth database via ISRT/GU calls | Batch (IMS) | **Read:** INFILE1, INFILE2. **IMS DLI:** ISRT (insert root/child), GU (verify) | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |
| 39 | PAUDBUNL.CBL | 317 | IMS database unload — reads IMS auth database and writes root/child segments to sequential flat files | Batch (IMS) | **IMS DLI:** GN (root), GNP (child). **Write:** OPFILE1, OPFILE2 | IMSFUNCS, CIPAUSMY, CIPAUDTY, PAUTBPCB |

## 3. COBOL Programs — Sub-Application: Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)

| # | Filename | LOC | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|----:|---------|----------------|-------------------|---------------------|
| 40 | COBTUPDT.cbl | 237 | Batch DB2 transaction type maintenance — reads flat file and performs INSERT/UPDATE/DELETE on DB2 TRTYP table | Batch (DB2) | **Read:** INPFILE (flat file). **EXEC SQL:** INSERT, UPDATE, DELETE on TRTYP table | *(inline DCLTRTYP via EXEC SQL INCLUDE)* |
| 41 | COTRTLIC.cbl | 2098 | Online transaction type list — CICS/DB2 browsable list of transaction types with cursor-based paging, selection for update/delete | Online (CICS+DB2) | **EXEC SQL:** DECLARE CURSOR, OPEN, FETCH, CLOSE on TRTYP. **CICS SEND/RECEIVE.** **CICS XCTL** | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COTRTLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY, CSDB2RWY |
| 42 | COTRTUPC.cbl | 1702 | Online transaction type update — CICS/DB2 screen for adding, updating, and deleting transaction type records with field validation | Online (CICS+DB2) | **EXEC SQL:** SELECT, INSERT, UPDATE, DELETE on TRTYP and TRCAT. **CICS SYNCPOINT** | CSUTLDWY, CVCRD01Y, DFHBMSCA, DFHAID, COTTL01Y, COTRTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, COCOM01Y, CSSETATY, CSSTRPFY, CSDB2RWY |

## 4. COBOL Programs — Sub-Application: VSAM/MQ (`app/app-vsam-mq/cbl/`)

| # | Filename | LOC | Purpose | Classification | Key I/O Operations | Copybooks Referenced |
|---|----------|----:|---------|----------------|-------------------|---------------------|
| 43 | COACCT01.cbl | 620 | MQ-triggered account inquiry — receives account query via MQ, reads VSAM account data, sends reply/error via MQ | Online (CICS+MQ) | **MQ:** MQOPEN, MQGET, MQPUT, MQCLOSE (3 queues: request, reply, error). **CICS READ:** account data. **CICS RETRIEVE** | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML, CVACT01Y |
| 44 | CODATE01.cbl | 524 | MQ-triggered date inquiry — receives date request via MQ, gets system date/time, sends formatted reply via MQ | Online (CICS+MQ) | **MQ:** MQOPEN, MQGET, MQPUT, MQCLOSE (3 queues). **CICS ASKTIME/FORMATTIME.** **CICS RETRIEVE** | CMQGMOV, CMQPMOV, CMQMDV, CMQODV, CMQV, CMQTML |

---

## 5. JCL Jobs — Core (`app/jcl/`)

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 1 | ACCTFILE | Define and load Account VSAM KSDS from flat file | STEP05: IDCAMS (delete old VSAM) → STEP10: IDCAMS (define VSAM cluster) → STEP15: IDCAMS (REPRO from PS to VSAM) |
| 2 | CARDFILE | Define and load Card VSAM KSDS with alternate indexes | CLCIFIL: SDSF (close CICS files) → STEP05-10: IDCAMS (delete/define VSAM) → STEP15: IDCAMS (REPRO load) → STEP40-60: IDCAMS (define/build AIX paths) → OPCIFIL: SDSF (reopen) |
| 3 | CBADMCDJ | Define CICS CSD resources for CardDemo application | STEP1: DFHCSDUP (batch CSD update — define programs, transactions, mapsets, files) |
| 4 | CBEXPORT | Export all CardDemo data to consolidated migration file | STEP01: IDCAMS (delete old export) → STEP02: PGM=CBEXPORT (run export program) |
| 5 | CBIMPORT | Import consolidated export file back into individual entity files | STEP01: PGM=CBIMPORT |
| 6 | CLOSEFIL | Close CICS-managed files for batch processing | CLCIFIL: SDSF (issue CEMT SET FILE CLOSED commands) |
| 7 | COMBTRAN | Combine and sort transactions into VSAM master | STEP05R: SORT (sort by key) → STEP10: IDCAMS (REPRO sorted file into VSAM) |
| 8 | CREASTMT | Generate customer statements (text + HTML) | DELDEF01: IDCAMS (cleanup) → STEP010: SORT (sort transactions) → STEP020: IDCAMS (REPRO to VSAM) → STEP030: IEFBR14 (delete old output) → STEP040: PGM=CBSTM03A (generate statements) |
| 9 | CUSTFILE | Define and load Customer VSAM KSDS | CLCIFIL: SDSF (close) → STEP05-10: IDCAMS (delete/define) → STEP15: IDCAMS (REPRO load) → OPCIFIL: SDSF (reopen) |
| 10 | DALYREJS | Define GDG base for daily rejection files | STEP05: IDCAMS (define GDG base) |
| 11 | DEFCUST | Define Customer data file (alternate definition) | STEP05: IDCAMS (define VSAM clusters) |
| 12 | DEFGDGB | Define GDG bases for transaction backup and statements | STEP05: IDCAMS (define GDG bases) |
| 13 | DEFGDGD | Define GDG bases for DB2-related backup files (TRANTYPE, TRANCATG, DISCGRP) | STEP10-50: IDCAMS (define GDGs) + IEBGENER (initial GDG generation copies) |
| 14 | DISCGRP | Define and load Disclosure Group VSAM file | STEP05-10: IDCAMS (delete/define) → STEP15: IDCAMS (REPRO) |
| 15 | DUSRSECJ | Define and load User Security VSAM KSDS with initial user records | PREDEL: IEFBR14 → STEP01: IEBGENER (create PS with user data) → STEP02: IDCAMS (define VSAM) → STEP03: IDCAMS (REPRO PS to VSAM) |
| 16 | ESDSRRDS | Create ESDS and RRDS VSAM datasets for demonstration | PREDEL-STEP05: Create PS, define ESDS/RRDS clusters, REPRO data |
| 17 | FTPJCL | FTP file transfer demonstration | STEP1: PGM=FTP |
| 18 | INTCALC | Run interest and fee calculation batch | STEP15: PGM=CBACT04C (interest calculator with date parameter) |
| 19 | INTRDRJ1 | Internal reader demo — submits INTRDRJ2 via internal reader | IDCAMS: REPRO backup → STEP01: IEBGENER (submit JCL to internal reader) |
| 20 | INTRDRJ2 | Internal reader target job — backup via REPRO | IDCAMS: REPRO file copy |
| 21 | OPENFIL | Open CICS-managed files after batch processing | OPCIFIL: SDSF (issue CEMT SET FILE OPEN commands) |
| 22 | POSTTRAN | Post daily transactions to master and update balances | STEP15: PGM=CBTRN02C (transaction posting program) |
| 23 | PRTCATBL | Print Transaction Category Balance file with SORT | DELDEF: IEFBR14 → STEP05R: REPROC (REPRO) → STEP10R: SORT |
| 24 | READACCT | Read and display account data (including COMP/ARRAY/VB formats) | PREDEL: IEFBR14 (delete old output) → STEP05: PGM=CBACT01C |
| 25 | READCARD | Read and display card data | STEP05: PGM=CBACT02C |
| 26 | READCUST | Read and display customer data | STEP05: PGM=CBCUS01C |
| 27 | READXREF | Read and display cross-reference data | STEP05: PGM=CBACT03C |
| 28 | REPTFILE | Define GDG base for report output files | STEP05: IDCAMS (define GDG) |
| 29 | TCATBALF | Define and load Transaction Category Balance VSAM | STEP05-10: IDCAMS (delete/define) → STEP15: IDCAMS (REPRO) |
| 30 | TRANBKP | Backup transaction VSAM master to GDG and optionally purge | STEP05R: REPROC (REPRO to GDG) → STEP05: IDCAMS (delete VSAM) → STEP10: IDCAMS (redefine VSAM) |
| 31 | TRANCATG | Define and load Transaction Category VSAM | STEP05-10: IDCAMS (delete/define) → STEP15: IDCAMS (REPRO) |
| 32 | TRANFILE | Define Transaction Master VSAM KSDS with alternate indexes | CLCIFIL-OPCIFIL: close/open CICS files around STEP05-30 (delete/define/REPRO/AIX) |
| 33 | TRANIDX | Define alternate indexes on Transaction Master | STEP20-30: IDCAMS (define AIX, define PATH, BLDINDEX) |
| 34 | TRANREPT | Generate transaction report | STEP05R: REPROC (backup) → STEP05R: SORT (sort transactions) → STEP10R: PGM=CBTRN03C (report generator) |
| 35 | TRANTYPE | Define and load Transaction Type VSAM | STEP05-10: IDCAMS (delete/define) → STEP15: IDCAMS (REPRO) |
| 36 | TXT2PDF1 | Convert text statement to PDF format | TXT2PDF: PGM=IKJEFT1B (invoke TXT2PDF REXX exec) |
| 37 | WAITSTEP | Execute timed wait (batch pause between steps) | WAIT: PGM=COBSWAIT (calls MVSWAIT assembler) |
| 38 | XREFFILE | Define and load Cross-Reference VSAM KSDS with alternate indexes | STEP05-30: IDCAMS (delete/define/REPRO/AIX/PATH/BLDINDEX) |

## 6. JCL Jobs — Sub-Application: Authorization (`app/app-authorization-ims-db2-mq/jcl/`)

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 39 | CBPAUP0J | Run batch purge of expired pending authorizations | Executes PGM=CBPAUP0C with IMS PCBs |
| 40 | DBPAUTP0 | Define DB2 authorization tables | STEP01: DB2 DDL (CREATE TABLE for auth data) |
| 41 | LOADPADB | Load IMS authorization database from flat files | Executes PGM=PAUDBLOD with IMS PCBs and input files |
| 42 | UNLDGSAM | Unload IMS auth database to GSAM files | Executes PGM=DBUNLDGS with IMS/GSAM PCBs |
| 43 | UNLDPADB | Unload IMS auth database to flat sequential files | Executes PGM=PAUDBUNL with IMS PCBs and output files |

## 7. JCL Jobs — Sub-Application: Transaction Type DB2 (`app/app-transaction-type-db2/jcl/`)

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 44 | CREADB21 | Create CardDemo DB2 database — defines tablespace, tables (TRTYP, TRCAT), and loads initial data | Multiple steps: DDL + DSNTIAUL LOAD |
| 45 | MNTTRDB2 | Maintain transaction type DB2 tables from flat file | Executes PGM=COBTUPDT |
| 46 | TRANEXTR | Extract transaction type/category data from DB2 to flat files | DB2 UNLOAD of TRTYP and TRCAT tables to PS datasets |

---

## 8. Control-M Scheduler Orchestration (`app/scheduler/CardDemo.controlm`)

Three scheduled workflows are defined:

### DAILY-TransactionBackup (Daily, All Days)
```
CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
```
Closes CICS files, backs up transaction VSAM to GDG, waits, reopens files.

### WEEKLY-TransactionTypesDBRefresh (Saturday)
Two sub-workflows chained:
1. **TransactionTypesDBRefresh:** `MNTTRDB2` → `TRANEXTR`
2. **DisclosureGroupsRefresh:** `CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL`

### MONTHLY-InterestCalculation (Monthly)
```
CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL
```
Closes files, runs interest/fee calculation, combines transactions into master, waits, reopens files.
