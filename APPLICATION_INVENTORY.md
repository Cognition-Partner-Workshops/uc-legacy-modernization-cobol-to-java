# Application Inventory — CardDemo COBOL Estate

> Auto-generated analysis of the CardDemo credit card management system.

---

## Table of Contents

1. [Main Application Programs (`app/cbl/`)](#main-application-programs)
2. [Sub-Application: Authorization IMS-DB2-MQ (`app/app-authorization-ims-db2-mq/cbl/`)](#sub-application-authorization-ims-db2-mq)
3. [Sub-Application: Transaction Type DB2 (`app/app-transaction-type-db2/cbl/`)](#sub-application-transaction-type-db2)
4. [Sub-Application: VSAM-MQ (`app/app-vsam-mq/cbl/`)](#sub-application-vsam-mq)
5. [JCL Job Catalog (`app/jcl/`)](#jcl-job-catalog)
6. [Sub-Application JCL Jobs](#sub-application-jcl-jobs)

---

## Main Application Programs

### Batch Programs

| Filename | Purpose | LOC | Key I/O Operations | Copybooks Referenced |
|----------|---------|-----|---------------------|---------------------|
| **CBACT01C.cbl** | Read account VSAM file, reformat records with date conversion, write to output flat file, array file, and variable-length record file | 430 | READ ACCTFILE (VSAM KSDS), WRITE OUT-FILE, WRITE ARRY-FILE, WRITE VBRC-FILE | CVACT01Y, CODATECN |
| **CBACT02C.cbl** | Read account data file sequentially for batch processing stub | 178 | READ ACCTFILE | CVACT01Y |
| **CBACT03C.cbl** | Read card cross-reference file sequentially for batch processing stub | 178 | READ XREFFILE | CVACT03Y |
| **CBACT04C.cbl** | Interest calculation batch — reads transaction category balances, cross-references, disclosure groups, and account files; computes monthly interest and writes transaction records | 652 | READ TCATBAL-FILE, READ XREF-FILE, READ DISCGRP-FILE, READ ACCOUNT-FILE, WRITE TRANSACT-FILE | CVACT01Y, CVACT02Y, CVACT03Y, CVTRA01Y, CVTRA02Y |
| **CBCUS01C.cbl** | Read customer data file sequentially for batch processing | 178 | READ CUSTFILE | CVCUS01Y |
| **CBEXPORT.cbl** | Export all CardDemo data (customers, accounts, cards, xrefs, transactions) to a single consolidated export flat file with record-type prefixes | 582 | READ CUSTOMER-FILE, READ ACCOUNT-FILE, READ CARD-FILE, READ XREF-FILE, READ TRANSACTION-FILE, WRITE EXPORT-OUTPUT | CVCUS01Y, CVACT01Y, CVCRD01Y, CVACT03Y, CVTRA05Y, CVEXPORT |
| **CBIMPORT.cbl** | Import data from consolidated export file, splitting records back into individual entity files with validation and error logging | 487 | READ EXPORT-INPUT, WRITE CUSTOMER-OUTPUT, WRITE ACCOUNT-OUTPUT, WRITE XREF-OUTPUT, WRITE TRANSACTION-OUTPUT, WRITE CARD-OUTPUT, WRITE ERROR-OUTPUT | CVCUS01Y, CVACT01Y, CVCRD01Y, CVACT03Y, CVTRA05Y, CVEXPORT |
| **CBTRN01C.cbl** | Batch daily transaction file processing — reads daily transaction file and posts to master transaction file | 494 | READ DALYTRAN-FILE, READ XREF-FILE, WRITE TRANSACT-FILE, READ TRANTYPE-FILE, READ TRANCATG-FILE | CVTRA05Y, CVTRA06Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CSDAT01Y |
| **CBTRN02C.cbl** | Batch transaction posting — reads daily transactions, validates against cross-ref, updates account balances and transaction category balances | 731 | READ DALYTRAN-FILE, READ/REWRITE ACCTDAT-FILE, READ/REWRITE XREF-FILE, READ/REWRITE TCATBAL-FILE, WRITE TRANSACT-FILE | CVTRA05Y, CVTRA06Y, CVACT01Y, CVACT03Y, CVTRA01Y |
| **CBTRN03C.cbl** | Daily transaction report — reads processed transactions and produces formatted report with type/category lookups | 649 | READ TRANFILE, READ CARDXREF, READ TRANTYPE, READ TRANCATG, READ DATEPARM, WRITE TRANREPT | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **CBSTM03A.CBL** | Account statement generation — reads transactions, cross-refs, customers, accounts; produces plain-text and HTML statements. Uses ALTER/GO TO, COMP/COMP-3 variables, 2D arrays, and subroutine calls | 924 | OPEN/CLOSE STMT-FILE, HTML-FILE; CALL CBSTM03B for TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE reads; WRITE statement and HTML records | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **CBSTM03B.CBL** | File-handling subroutine for CBSTM03A — manages OPEN/READ/CLOSE for transaction, cross-ref, customer, and account VSAM files | 230 | OPEN/READ/CLOSE TRNX-FILE, XREF-FILE, CUST-FILE, ACCT-FILE | _(inline FD definitions)_ |
| **COBSWAIT.cbl** | Utility wait program — accepts centisecond value from SYSIN and calls assembler MVSWAIT routine | 41 | ACCEPT from SYSIN, CALL MVSWAIT | _(none)_ |
| **CSUTLDTC.cbl** | Date validation utility — calls LE CEEDAYS API to validate date strings and return Lillian date format | 157 | CALL CEEDAYS | _(none)_ |

### Online (CICS) Programs

| Filename | Purpose | LOC | Key I/O Operations | Copybooks Referenced |
|----------|---------|-----|---------------------|---------------------|
| **COSGN00C.cbl** | Sign-on screen — authenticates users against USRSEC file via CICS READ, manages session sign-on flow | 260 | EXEC CICS READ (USRSEC), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CSSETATY, DFHAID, DFHBMSCA |
| **COADM01C.cbl** | Admin menu — displays administration options, routes to selected sub-function via EXEC CICS XCTL | 288 | EXEC CICS XCTL, EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| **COMEN01C.cbl** | Main menu — central navigation hub, routes users to account, card, transaction, report, bill pay, and user management functions | 308 | EXEC CICS XCTL, EXEC CICS INQUIRE PROGRAM, EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| **COACTUPC.cbl** | Account update — full CRUD screen for account records with extensive field validation (dates, numerics, limits) | 4236 | EXEC CICS READ/REWRITE (ACCTDAT, CCXREF, CXACAIX), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA + many more |
| **COACTVWC.cbl** | Account view — read-only display of account details with card cross-reference lookup | 941 | EXEC CICS READ (ACCTDAT, CARDDAT, CCXREF), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA + more |
| **COBIL00C.cbl** | Bill payment — processes full account balance payments, creates transaction records, updates account balances | 572 | EXEC CICS READ/REWRITE (ACCTDAT), EXEC CICS READ (CXACAIX), EXEC CICS STARTBR/READPREV/ENDBR/WRITE (TRANSACT), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| **COCRDLIC.cbl** | Credit card list — browse/search credit card records with pagination (forward/backward), account cross-reference | 1459 | EXEC CICS STARTBR/READNEXT/READPREV/ENDBR (CARDDAT), EXEC CICS READ (CCXREF, ACCTDAT), EXEC CICS XCTL, EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT03Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSLKPCDY, DFHAID, DFHBMSCA + more |
| **COCRDSLC.cbl** | Credit card detail view — displays detailed card information with account cross-reference | 887 | EXEC CICS READ (CARDDAT, ACCTDAT), EXEC CICS XCTL, EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT02Y, CVACT03Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA + more |
| **COCRDUPC.cbl** | Credit card update — modify card details (status, expiry, name) with validation | 1560 | EXEC CICS READ/REWRITE (CARDDAT, CCXREF, ACCTDAT), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT02Y, CVACT03Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA + more |
| **COTRN00C.cbl** | Transaction list — browse transactions with pagination, select for view/add | 699 | EXEC CICS STARTBR/READNEXT/READPREV/ENDBR (TRANSACT), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CVTRA05Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| **COTRN01C.cbl** | Transaction view — read-only display of a single transaction record | 330 | EXEC CICS READ (TRANSACT), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| **COTRN02C.cbl** | Transaction add — create new transaction with full validation (account, card, amounts, dates) using CSUTLDTC date utility | 783 | EXEC CICS READ (CCXREF, CXACAIX, ACCTDAT), EXEC CICS WRITE (TRANSACT), EXEC CICS SEND/RECEIVE MAP, CALL CSUTLDTC | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| **CORPT00C.cbl** | Report generation — submit batch report jobs (daily transaction report, monthly statement) via CICS transient data queues | 649 | EXEC CICS WRITEQ TD, EXEC CICS SEND/RECEIVE MAP, CALL CSUTLDTC | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUTLDPY, DFHAID, DFHBMSCA |
| **COUSR00C.cbl** | User list — browse user security records with pagination | 695 | EXEC CICS STARTBR/READNEXT/READPREV/ENDBR (USRSEC), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CSUSR01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| **COUSR01C.cbl** | User add — create new user security records | 299 | EXEC CICS READ/WRITE (USRSEC), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CSUSR01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| **COUSR02C.cbl** | User update — modify existing user security records | 414 | EXEC CICS READ/REWRITE (USRSEC), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CSUSR01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| **COUSR03C.cbl** | User delete — remove user security records with confirmation | 359 | EXEC CICS READ/DELETE (USRSEC), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CSUSR01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |

---

## Sub-Application: Authorization IMS-DB2-MQ

Programs in `app/app-authorization-ims-db2-mq/cbl/` extend CardDemo with payment authorization using IMS databases, DB2, and MQ message queuing.

| Filename | Purpose | Classification | LOC | Key I/O Operations | Copybooks Referenced |
|----------|---------|---------------|-----|---------------------|---------------------|
| **CBPAUP0C.cbl** | Batch payment authorization processor — reads MQ messages, processes authorization requests | Batch | 386 | MQ GET/PUT, EXEC CICS operations | CCPAURQY, CCPAURLY |
| **COPAUA0C.cbl** | Online payment authorization admin — CICS screen for managing authorization rules, DB2 fraud detection queries | Online (CICS) | 1026 | EXEC CICS SEND/RECEIVE MAP, EXEC SQL SELECT/INSERT/UPDATE (AUTHFRDS table), MQ operations | COCOM01Y, COPAU00, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CIPAUDTY, CIPAUSMY, CCPAUERY, CCPAURQY, CCPAURLY, CSDB2RWY, CSDB2RPY, DFHAID, DFHBMSCA |
| **COPAUS0C.cbl** | Authorization summary screen — displays authorization statistics and recent activity | Online (CICS) | 1032 | EXEC CICS SEND/RECEIVE MAP, EXEC SQL SELECT (authorization tables) | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CIPAUDTY, CIPAUSMY, CSDB2RWY, CSDB2RPY, DFHAID, DFHBMSCA + more |
| **COPAUS1C.cbl** | Authorization detail view — displays individual authorization record details | Online (CICS) | 604 | EXEC CICS SEND/RECEIVE MAP, EXEC SQL SELECT | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CIPAUDTY, CSDB2RWY, CSDB2RPY, DFHAID, DFHBMSCA |
| **COPAUS2C.cbl** | Authorization history — displays authorization processing history | Online (CICS) | 244 | EXEC CICS SEND/RECEIVE MAP | COCOM01Y, CIPAUSMY |
| **PAUDBLOD.CBL** | IMS database load — initial load of payment authorization IMS database segments | Batch | 369 | IMS DL/I ISRT calls, READ input file | PAUTBPCB, PADFLPCB, IMSFUNCS |
| **PAUDBUNL.CBL** | IMS database unload — extract payment authorization data from IMS database | Batch | 317 | IMS DL/I GN/GHN calls, WRITE output file | PAUTBPCB, PADFLPCB, IMSFUNCS |
| **DBUNLDGS.CBL** | GSAM database unload — extract data using GSAM (Generalized Sequential Access Method) | Batch | 366 | IMS DL/I GN calls, GSAM I/O | PAUTBPCB, PASFLPCB, PADFLPCB, IMSFUNCS |

---

## Sub-Application: Transaction Type DB2

Programs in `app/app-transaction-type-db2/cbl/` manage transaction type reference data stored in DB2.

| Filename | Purpose | Classification | LOC | Key I/O Operations | Copybooks Referenced |
|----------|---------|---------------|-----|---------------------|---------------------|
| **COTRTLIC.cbl** | Transaction type list — CICS screen to browse DB2 transaction type and category records with pagination | Online (CICS) | 2098 | EXEC SQL SELECT/FETCH (TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY tables), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COTRTLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSDB2RWY, CSDB2RPY, DFHAID, DFHBMSCA + more |
| **COTRTUPC.cbl** | Transaction type update — CICS screen to add/modify/delete DB2 transaction type records with validation | Online (CICS) | 1702 | EXEC SQL SELECT/INSERT/UPDATE/DELETE (TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY), EXEC CICS SEND/RECEIVE MAP | COCOM01Y, COTRTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSDB2RWY, CSDB2RPY, DFHAID, DFHBMSCA + more |
| **COBTUPDT.cbl** | Batch transaction type maintenance — reads input file with action codes (A/D/U) to insert, update, or delete DB2 transaction type records | Batch | 237 | READ INPFILE, EXEC SQL INSERT/UPDATE/DELETE (TRANSACTION_TYPE) | _(inline SQL, DCLTRTYP)_ |

---

## Sub-Application: VSAM-MQ

Programs in `app/app-vsam-mq/cbl/` handle MQ-based account and date processing.

| Filename | Purpose | Classification | LOC | Key I/O Operations | Copybooks Referenced |
|----------|---------|---------------|-----|---------------------|---------------------|
| **COACCT01.cbl** | Account inquiry via MQ — reads account inquiry requests from MQ queue, processes VSAM lookups, returns results via MQ reply queue | Online (CICS/MQ) | 620 | EXEC CICS READ (account VSAM files), MQ MQOPEN/MQGET/MQPUT/MQCLOSE | COCOM01Y, CVACT01Y, CVACT03Y, CVCRD01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |
| **CODATE01.cbl** | Date service via MQ — receives date formatting/validation requests via MQ, processes them, and returns results | Online (CICS/MQ) | 524 | MQ MQOPEN/MQGET/MQPUT/MQCLOSE, EXEC CICS ASKTIME/FORMATTIME | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID, DFHBMSCA |

---

## JCL Job Catalog

### Data Definition & VSAM Setup Jobs

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **ACCTFILE.jcl** | Define account VSAM KSDS file, load from flat file | STEP05: DELETE existing cluster → STEP10: DEFINE CLUSTER (KSDS, key=11 bytes) → STEP15: REPRO from PS to VSAM |
| **CARDFILE.jcl** | Define card data VSAM KSDS file, load from flat file | STEP05: DELETE → STEP10: DEFINE CLUSTER (KSDS, key=16 bytes) → STEP15: REPRO PS→VSAM |
| **CUSTFILE.jcl** | Define customer VSAM KSDS file, load from flat file | STEP05: DELETE → STEP10: DEFINE CLUSTER (KSDS, key=9 bytes) → STEP15: REPRO PS→VSAM |
| **XREFFILE.jcl** | Define card cross-reference VSAM KSDS with alternate index on account ID | STEP05: DELETE cluster+AIX → STEP10: DEFINE CLUSTER → STEP15: REPRO → STEP20: DEFINE AIX → STEP25: DEFINE PATH → STEP30: BLDINDEX |
| **TRANFILE.jcl** | Define transaction VSAM KSDS file | STEP05: DELETE → STEP10: DEFINE CLUSTER (KSDS, key=32 bytes) → STEP15: REPRO PS→VSAM |
| **TRANTYPE.jcl** | Define transaction type VSAM KSDS file | STEP05: DELETE → STEP10: DEFINE CLUSTER (key=2 bytes) → STEP15: REPRO |
| **TRANCATG.jcl** | Define transaction category VSAM KSDS file | STEP05: DELETE → STEP10: DEFINE CLUSTER (key=6 bytes) → STEP15: REPRO |
| **TCATBALF.jcl** | Define transaction category balance VSAM KSDS file | STEP05: DELETE → STEP10: DEFINE CLUSTER (key=17 bytes) → STEP15: REPRO |
| **DISCGRP.jcl** | Define disclosure group VSAM KSDS file | STEP05: DELETE → STEP10: DEFINE CLUSTER (key=16 bytes) → STEP15: REPRO |
| **DEFCUST.jcl** | Define customer flat file (PS) dataset | Single step: IEFBR14 to allocate dataset |
| **ESDSRRDS.jcl** | Define ESDS and RRDS VSAM datasets for testing | STEP05–STEP20: DELETE/DEFINE ESDS and RRDS clusters |
| **DEFGDGB.jcl** | Define GDG base for transaction backup | STEP05: DELETE GDG → STEP10: DEFINE GDG (limit 5) |
| **DEFGDGD.jcl** | Define GDG base for daily transaction | STEP05: DELETE GDG → STEP10: DEFINE GDG (limit 5) |

### Batch Processing Jobs

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **POSTTRAN.jcl** | Post daily transactions — core daily batch cycle | STEP10S: SORT daily transactions by card+timestamp → STEP10: CBTRN01C (validate & enrich) → STEP20: CBTRN02C (post to accounts & balances) |
| **TRANREPT.jcl** | Daily transaction report generation | STEP10S: SORT/filter transactions by date range from backup GDG → STEP10R: CBTRN03C (produce formatted report) |
| **INTCALC.jcl** | Monthly interest calculation | STEP10: CBACT04C (compute interest on category balances, write interest transactions) |
| **CREASTMT.JCL** | Create account statements (text + HTML) | STEP01: CBSTM03A (read transactions, cross-refs, customers, accounts; write statements) |
| **DALYREJS.jcl** | Daily transaction rejection processing | STEP10S: SORT rejected transactions → STEP10R: Produce rejection report |
| **COMBTRAN.jcl** | Combine daily transaction files into master | STEP10: IEBGENER copy daily→master |
| **TRANBKP.jcl** | Backup transaction file to GDG | STEP10: IEBGENER copy to backup GDG(+1) |
| **TRANIDX.jcl** | Build transaction index from backup | STEP10: SORT transactions by card number, output to indexed dataset |
| **PRTCATBL.jcl** | Print transaction category balance report | Single step: print TCATBAL file |
| **CBEXPORT.jcl** | Export all CardDemo data to flat file | STEP10: CBEXPORT program |
| **CBIMPORT.jcl** | Import data from export flat file | STEP10: CBIMPORT program |
| **WAITSTEP.jcl** | Utility wait job | WAIT: COBSWAIT (wait specified centiseconds) |

### File Maintenance Jobs

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **OPENFIL.jcl** | Open VSAM files for CICS (enable/disable) | STEP10: IDCAMS VERIFY for each VSAM file |
| **CLOSEFIL.jcl** | Close/disable VSAM files in CICS | STEP10: CICS DISCARD commands |
| **READACCT.jcl** | Read and print account file | STEP10: IDCAMS PRINT |
| **READCARD.jcl** | Read and print card file | STEP10: IDCAMS PRINT |
| **READCUST.jcl** | Read and print customer file | STEP10: IDCAMS PRINT |
| **READXREF.jcl** | Read and print cross-reference file | STEP10: IDCAMS PRINT |
| **REPTFILE.jcl** | Define report output datasets | File allocation steps |

### Utility Jobs

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **DUSRSECJ.jcl** | Define and load user security file (USRSEC VSAM KSDS) | STEP05: DELETE → STEP10: DEFINE → STEP15: REPRO |
| **CBADMCDJ.jcl** | Admin batch job — CICS-related administrative functions | STEP10: Admin program execution |
| **TXT2PDF1.JCL** | Convert text statement file to PDF | TXT2PDF: IKJEFT1B with TXT2PDF REXX exec |
| **INTRDRJ1.JCL** | Internal reader job submission (method 1) | Submit job via internal reader |
| **INTRDRJ2.JCL** | Internal reader job submission (method 2) | Submit job via internal reader |
| **FTPJCL.JCL** | FTP file transfer job | FTP step to transfer files |

---

## Sub-Application JCL Jobs

### Authorization IMS-DB2-MQ (`app/app-authorization-ims-db2-mq/jcl/`)

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **CBPAUP0J.jcl** | Run batch payment authorization processor | STEP10: CBPAUP0C |
| **DBPAUTP0.jcl** | Initialize IMS payment authorization database | DBD generation and load |
| **LOADPADB.JCL** | Load payment authorization IMS database | PAUDBLOD (IMS DL/I load) |
| **UNLDPADB.JCL** | Unload payment authorization IMS database | PAUDBUNL (IMS DL/I unload) |
| **UNLDGSAM.JCL** | Unload GSAM database | DBUNLDGS |

### Transaction Type DB2 (`app/app-transaction-type-db2/jcl/`)

| Job Name | Purpose | Steps |
|----------|---------|-------|
| **CREADB21.jcl** | Create CardDemo DB2 database — tables, indexes, initial data load | FREEPLN: Free plans → CRCRDDB: Create database/tables (DSNTIAD) → LDTTYPE: Load transaction types (DSNTEP4) → LDTCCAT: Load transaction categories |
| **MNTTRDB2.jcl** | Maintain DB2 transaction types in batch | STEP1: COBTUPDT via TSO/DB2 (reads input file with A/D/U action codes) |
| **TRANEXTR.jcl** | Extract DB2 reference data to flat files for batch use | STEP10: Backup TRANTYPE → STEP20: Backup TRANCATG → STEP30: Delete old files → STEP40: DSNTIAUL extract TRANSACTION_TYPE → STEP50: DSNTIAUL extract TRANSACTION_TYPE_CATEGORY |

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| **Total COBOL programs** | 43 |
| **Batch programs** | 19 |
| **Online (CICS) programs** | 20 |
| **MQ-enabled programs** | 4 |
| **DB2-enabled programs** | 6 |
| **IMS-enabled programs** | 3 |
| **Total JCL jobs** | 44 |
| **Copybooks (main)** | 30 |
| **Copybooks (sub-apps)** | 11 |
| **Total lines of COBOL** | ~28,000+ |
