# APPLICATION INVENTORY - CardDemo COBOL Estate

> **Generated:** 2026-03-25 | **Scope:** `app/cbl/`, `app/jcl/`, and sub-application directories

---

## 1. Main COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs

| # | Filename | Lines | Purpose | Transaction | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|-------------|---------------------|----------------------|
| 1 | **COSGN00C.cbl** | 260 | Sign-on screen for CardDemo application; authenticates users and routes to admin or regular menu | CC00 | CICS READ USRSEC file (user authentication) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 2 | **COMEN01C.cbl** | 308 | Main menu for regular users; routes to account, card, transaction, and report functions | CM00 | CICS XCTL to sub-programs; CICS INQUIRE for program availability | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 3 | **COADM01C.cbl** | 288 | Admin menu for admin users; routes to user management functions | CA00 | CICS XCTL to user management programs | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 4 | **COACTVWC.cbl** | 941 | View account details; displays account, card, and customer information | CA01 | CICS READ: ACCTDAT (account), CARDDAT (card), CARDAIX (card xref), CUSTDAT (customer) | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 5 | **COACTUPC.cbl** | 4236 | Update account details; validates and applies changes to account master | CA02 | CICS READ/REWRITE: ACCTDAT (account); CICS READ: CARDDAT, CARDAIX, CUSTDAT | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSSTRPFY, DFHAID, DFHBMSCA, + many BMS copies |
| 6 | **COCRDLIC.cbl** | 1459 | List credit cards with browse/filter capability | CC01 | CICS STARTBR/READNEXT/READPREV/ENDBR: CARDDAT (card file) | CVCRD01Y, COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 7 | **COCRDSLC.cbl** | 887 | View credit card details | CC02 | CICS READ: CARDDAT (card), CUSTDAT (customer) | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 8 | **COCRDUPC.cbl** | 1560 | Update credit card details; validates and applies card changes | CC03 | CICS READ/REWRITE: CARDDAT (card); CICS READ: CUSTDAT | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 9 | **COTRN00C.cbl** | 699 | List transactions with browse and search capability | CT00 | CICS STARTBR/READNEXT/READPREV/ENDBR: TRANSACT (transaction file) | COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 10 | **COTRN01C.cbl** | 330 | View a single transaction record | CT01 | CICS READ: TRANSACT (transaction file) | COCOM01Y, COTRN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 11 | **COTRN02C.cbl** | 783 | Add a new transaction; validates and writes to transaction file | CT02 | CICS READ: CARDXREF, ACCTDAT; CICS STARTBR/READPREV/ENDBR/WRITE: TRANSACT | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| 12 | **CORPT00C.cbl** | 649 | Transaction report request; submits batch report job via CICS TD queue | CR00 | CICS WRITEQ TD (CSOT - internal reader); Submits TRANREPT JCL | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 13 | **COBIL00C.cbl** | 572 | Bill payment; pays account balance (full or partial) and creates transaction | CB00 | CICS READ/REWRITE: ACCTDAT; CICS READ: CARDXREF; CICS STARTBR/READPREV/ENDBR/WRITE: TRANSACT | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 14 | **COUSR00C.cbl** | 695 | List all users from security file with browse capability | CU00 | CICS STARTBR/READNEXT/READPREV/ENDBR: USRSEC (user security file) | COCOM01Y, COUSR00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 15 | **COUSR01C.cbl** | 299 | Add a new user (regular or admin) to security file | CU01 | CICS WRITE: USRSEC | COCOM01Y, COUSR01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 16 | **COUSR02C.cbl** | 414 | Update an existing user in security file | CU02 | CICS READ/REWRITE: USRSEC | COCOM01Y, COUSR02, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 17 | **COUSR03C.cbl** | 359 | Delete a user from security file | CU03 | CICS READ/DELETE: USRSEC | COCOM01Y, COUSR03, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

### 1.2 Batch Programs

| # | Filename | Lines | Purpose | Key I/O Operations | Copybooks Referenced |
|---|----------|-------|---------|--------------------|----------------------|
| 1 | **CBACT01C.cbl** | 431 | Read account VSAM file; write to flat, array, and variable-length output files | READ: ACCTFILE (VSAM KSDS); WRITE: OUTFILE, ARRYFILE, VBRCFILE | CVACT01Y, CODATECN |
| 2 | **CBACT02C.cbl** | 179 | Read and display card data file | READ: CARDFILE (VSAM KSDS) | CVACT02Y |
| 3 | **CBACT03C.cbl** | 179 | Read and display account cross-reference file | READ: XREFFILE (VSAM KSDS) | CVACT03Y |
| 4 | **CBACT04C.cbl** | 653 | Interest calculation; reads transaction category balances, computes interest/fees, updates accounts, writes interest transactions | READ: TCATBALF (VSAM KSDS); READ: XREFFILE, DISCGRP; READ/REWRITE: ACCTFILE; WRITE: TRANSACT | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| 5 | **CBCUS01C.cbl** | 179 | Read and display customer data file | READ: CUSTFILE (VSAM KSDS) | CVCUS01Y |
| 6 | **CBTRN01C.cbl** | 494 | Read daily transactions and post to transaction master and update category balances | READ: DALYTRAN (daily transactions); READ/WRITE: TRANSACT, TCATBALF; READ: XREFFILE, TRANTYPE, TRANCATG | CVTRA05Y, CVTRA06Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA01Y |
| 7 | **CBTRN02C.cbl** | 731 | Transaction posting; reads daily transactions, validates, and posts to master files with category balance updates | READ: DALYTRAN; READ: CARDXREF, ACCTDAT, TRANTYPE, TRANCATG; WRITE: TRANSACT, TCATBALF | CVTRA05Y, CVTRA06Y, CVACT03Y, CVTRA01Y, CVTRA03Y |
| 8 | **CBTRN03C.cbl** | 649 | Transaction report generation; reads transaction file and produces formatted reports | READ: TRANFILE (sequential), CARDXREF, TRANTYPE, TRANCATG; WRITE: RPTFILE (report output) | CVTRA05Y, CVTRA07Y, CVTRA03Y, CVTRA04Y, CVACT03Y |
| 9 | **CBSTM03A.CBL** | 924 | Statement generation; creates account statements from transaction data, calls CBSTM03B for I/O | READ: (via CBSTM03B) TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE; WRITE: STMTFILE, HTMLFILE | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| 10 | **CBSTM03B.CBL** | 230 | Subroutine for CBSTM03A; handles file I/O for statement generation | READ: TRNXFILE (VSAM KSDS), XREFFILE, CUSTFILE, ACCTFILE | (none - uses structures passed from CBSTM03A) |
| 11 | **CBEXPORT.cbl** | 583 | Export customer data for branch migration; reads all master files and creates multi-record export file | READ: CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE; WRITE: EXPFILE | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 12 | **CBIMPORT.cbl** | 487 | Import data from export file back into VSAM master files | READ: IMPFILE (import); WRITE: CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| 13 | **COBSWAIT.cbl** | 41 | Utility program; waits for a specified time (parameter in centiseconds) | CALL: MVSWAIT (assembler) | (none) |
| 14 | **CSUTLDTC.cbl** | 157 | Date validation utility; converts and validates dates using LE services | CALL: CEEDAYS (Language Environment) | (none) |

### 1.3 Summary Statistics - Main Programs

| Metric | Count |
|--------|-------|
| Total COBOL programs | 31 |
| Online CICS programs | 17 |
| Batch programs | 14 |
| Total lines of code | 20,650 |

---

## 2. Sub-Application Programs

### 2.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ integration for card authorization and fraud detection.

| # | Filename | Type | Purpose |
|---|----------|------|---------|
| 1 | **COPAUA0C.cbl** | Online (CICS/MQ) | Card authorization decision program; triggers via MQ message |
| 2 | **COPAUS0C.cbl** | Online (CICS/IMS) | Summary view of authorization messages from IMS DB |
| 3 | **COPAUS1C.cbl** | Online (CICS/IMS) | Detail view of a single authorization message |
| 4 | **COPAUS2C.cbl** | Online (CICS/DB2) | Mark authorization message as fraud; writes to DB2 |
| 5 | **CBPAUP0C.cbl** | Batch (IMS) | Delete expired pending authorization messages |
| 6 | **PAUDBLOD.CBL** | Batch (DB2) | Load authorization data into DB2 |
| 7 | **PAUDBUNL.CBL** | Batch (DB2) | Unload authorization data from DB2 |
| 8 | **DBUNLDGS.CBL** | Batch (DB2) | Generic DB2 unload utility |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based CRUD for managing transaction types.

| # | Filename | Type | Purpose |
|---|----------|------|---------|
| 1 | **COTRTUPC.cbl** | Online (CICS/DB2) | Add or edit transaction types via DB2 |
| 2 | **COTRTLIC.cbl** | Online (CICS/DB2) | List and delete transaction types from DB2 |
| 3 | **COBTUPDT.cbl** | Batch (DB2) | Batch update of transaction types in DB2 |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ-based request/response services.

| # | Filename | Type | Purpose |
|---|----------|------|---------|
| 1 | **CODATE01.cbl** | Online (CICS/MQ) | System date inquiry via MQ (queue CDRD) |
| 2 | **COACCT01.cbl** | Online (CICS/MQ) | Account inquiry via MQ (queue CDRA) |

### 2.4 Sub-Application Summary

| Module | Programs | Online | Batch |
|--------|----------|--------|-------|
| Authorization (IMS/DB2/MQ) | 8 | 4 | 4 |
| Transaction Type (DB2) | 3 | 2 | 1 |
| VSAM-MQ | 2 | 2 | 0 |
| **Sub-App Total** | **13** | **8** | **5** |

---

## 3. JCL Job Catalog (`app/jcl/`)

### 3.1 Data File Refresh Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 1 | **ACCTFILE.jcl** | Refresh account master VSAM file from sequential data | STEP05 (IDCAMS: delete/define VSAM) -> STEP10 (IDCAMS: define cluster) -> STEP15 (IDCAMS: REPRO load from PS to VSAM KSDS) |
| 2 | **CARDFILE.jcl** | Refresh card master VSAM file | STEP05 (IDCAMS: delete) -> STEP10 (IDCAMS: define cluster) -> STEP15 (IDCAMS: REPRO load CARDDATA.PS -> CARDDATA.VSAM.KSDS) |
| 3 | **CUSTFILE.jcl** | Refresh customer master VSAM file | STEP05 (IDCAMS: delete) -> STEP10 (IDCAMS: define cluster) -> STEP15 (IDCAMS: REPRO load CUSTDATA.PS -> CUSTDATA.VSAM.KSDS) |
| 4 | **XREFFILE.jcl** | Load card cross-reference VSAM file + define alternate indexes | STEP05 (IDCAMS: delete) -> STEP10 (IDCAMS: define cluster) -> STEP15 (IDCAMS: REPRO load) -> STEP20 (IDCAMS: define AIX on ACCT-ID) -> STEP25 (IDCAMS: define AIX path) -> STEP30 (IDCAMS: BLDINDEX) |
| 5 | **TRANFILE.jcl** | Load transaction master VSAM file with alternate indexes | CLCIFIL (close CICS files) -> STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load) -> STEP20/25/30 (define AIX, path, BLDINDEX on PROC-TS) -> OPCIFIL (open CICS files) |
| 6 | **DUSRSECJ.jcl** | Load user security VSAM file | STEP05 (IDCAMS: delete) -> STEP10 (IDCAMS: define cluster) -> STEP15 (IDCAMS: REPRO load USRSEC.PS -> USRSEC.VSAM.KSDS) |

### 3.2 Core Batch Processing Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 7 | **POSTTRAN.jcl** | Core transaction posting | STEP10 (PGM=CBTRN02C: post daily transactions to master) |
| 8 | **INTCALC.jcl** | Interest calculation | STEP10 (PGM=CBACT04C: calculate interest and fees on accounts) |
| 9 | **COMBTRAN.jcl** | Combine daily transactions into master | STEP05 (SORT: sort daily transactions) -> STEP10 (IDCAMS: REPRO merge into TRANSACT.VSAM.KSDS) |
| 10 | **CREASTMT.JCL** | Create account statements | DELDEF01 (IDCAMS: create sorted transaction VSAM) -> STEP010 (SORT: sort transactions by card+ID) -> STEP020 (IDCAMS: REPRO into VSAM) -> STEP030 (IEFBR14: delete old statements) -> STEP040 (PGM=CBSTM03A: generate statements) |
| 11 | **TRANBKP.jcl** | Backup transaction file to GDG | STEP05R (REPROC: copy TRANSACT.VSAM.KSDS to GDG BKUP(+1)) -> STEP05 (IDCAMS: delete VSAM) -> STEP10 (IDCAMS: redefine VSAM cluster) |
| 12 | **TRANREPT.jcl** | Generate transaction report | STEP05R (REPROC: backup transactions) -> STEP05R (SORT: filter by date range) -> STEP10R (PGM=CBTRN03C: generate report) |

### 3.3 Reference Data Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 13 | **TRANTYPE.jcl** | Load transaction type reference data | STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load TRANTYPE.PS -> TRANTYPE.VSAM.KSDS) |
| 14 | **TRANCATG.jcl** | Load transaction category reference data | STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load TRANCATG.PS -> TRANCATG.VSAM.KSDS) |
| 15 | **TCATBALF.jcl** | Load transaction category balance file | STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load TCATBALF.PS -> TCATBALF.VSAM.KSDS) |
| 16 | **DISCGRP.jcl** | Load discount group reference data | STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load DISCGRP.PS -> DISCGRP.VSAM.KSDS) |
| 17 | **REPTFILE.jcl** | Define daily reject file | STEP05 (delete) -> STEP10 (define DALYREJS VSAM cluster) |
| 18 | **DALYREJS.jcl** | Load daily transaction rejects | STEP05 (delete) -> STEP10 (define cluster) -> STEP15 (REPRO load) |

### 3.4 CICS File Control Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 19 | **CLOSEFIL.jcl** | Close CICS files before batch processing | Uses SDSF to issue SET,OPENED,DISABLED commands for all CICS files |
| 20 | **OPENFIL.jcl** | Re-open CICS files after batch processing | Uses SDSF to issue SET,OPENED,ENABLED commands for all CICS files |

### 3.5 Index and GDG Management Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 21 | **TRANIDX.jcl** | Define alternate index on transaction processed timestamp | STEP20 (IDCAMS: define AIX) -> STEP25 (define path) -> STEP30 (BLDINDEX) |
| 22 | **DEFGDGB.jcl** | Define GDG base for transaction backup | STEP10 (IDCAMS: DEFINE GDG) |
| 23 | **DEFGDGD.jcl** | Define GDG base for daily transaction | STEP10 (IDCAMS: DEFINE GDG) |
| 24 | **ESDSRRDS.jcl** | Define ESDS and RRDS VSAM clusters (utility/demo) | Multiple IDCAMS steps for ESDS and RRDS |

### 3.6 Utility and Read Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 25 | **READACCT.jcl** | Read and print account file | STEP10 (PGM=CBACT01C) |
| 26 | **READCARD.jcl** | Read and print card file | STEP10 (PGM=CBACT02C) |
| 27 | **READCUST.jcl** | Read and print customer file | STEP10 (PGM=CBCUS01C) |
| 28 | **READXREF.jcl** | Read and print cross-reference file | STEP10 (PGM=CBACT03C) |
| 29 | **PRTCATBL.jcl** | Print transaction category balance file | STEP10 (PGM=CBTRN01C) |
| 30 | **WAITSTEP.jcl** | Execute wait utility (e.g., between batch steps) | WAIT (PGM=COBSWAIT) |
| 31 | **CBADMCDJ.jcl** | General administration job | Multiple utility steps |

### 3.7 Export/Import Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 32 | **CBEXPORT.jcl** | Export all data for branch migration | STEP10 (PGM=CBEXPORT) |
| 33 | **CBIMPORT.jcl** | Import data from export file | STEP10 (PGM=CBIMPORT) |

### 3.8 Miscellaneous Jobs

| # | Job Name | Purpose | Step Sequence |
|---|----------|---------|---------------|
| 34 | **DEFCUST.jcl** | Define customer VSAM cluster | IDCAMS define/delete |
| 35 | **FTPJCL.JCL** | FTP transfer utility | STEP1 (PGM=FTP) |
| 36 | **INTRDRJ1.JCL** | Internal reader chain job 1 | IDCAMS backup -> IEBGENER submit INTRDRJ2 via internal reader |
| 37 | **INTRDRJ2.JCL** | Internal reader chain job 2 | IDCAMS copy |
| 38 | **TXT2PDF1.JCL** | Convert statement text to PDF | TXT2PDF (PGM=IKJEFT1B: run TXT2PDF REXX) |

### 3.3 JCL Summary

| Category | Count |
|----------|-------|
| Data file refresh | 6 |
| Core batch processing | 6 |
| Reference data loading | 6 |
| CICS file control | 2 |
| Index/GDG management | 4 |
| Utility/read | 7 |
| Export/import | 2 |
| Miscellaneous | 5 |
| **Total JCL Jobs** | **38** |

---

## 4. Grand Total

| Component | Count |
|-----------|-------|
| Main COBOL programs (`app/cbl/`) | 31 |
| Sub-application programs | 13 |
| **Total COBOL programs** | **44** |
| JCL jobs (`app/jcl/`) | 38 |
| Copybooks (`app/cpy/`) | 30 |
| BMS maps (`app/bms/`) | 17 |
| Assembler programs (`app/asm/`) | 2 |
