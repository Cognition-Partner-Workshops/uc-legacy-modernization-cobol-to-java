# 03 – Program-to-File / Database Dependencies (CRUD)

Access is derived from native COBOL verbs (`SELECT … ASSIGN`, `OPEN`, `READ`, `WRITE`, `REWRITE`, `DELETE`, `START`) and from `EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT`. Logical file / DDNAME → physical dataset resolution uses the DD statements in `app/jcl/` and `app/proc/` for batch programs, and the `DSNAME(...)` clauses of the CICS `FILE` definitions in `app/csd/CARDDEMO.CSD` for online programs.

**CRUD legend.** `C` = Create (WRITE / OPEN OUTPUT), `R` = Read (READ / START / STARTBR / READNEXT / OPEN INPUT / OPEN I-O), `U` = Update (REWRITE / OPEN I-O), `D` = Delete (DELETE). Alternate-index (AIX/PATH) reads are folded into their base entity; the AIX↔base relationships are detailed in `04-file-to-file-mapping.md`.

## CRUD Matrix

| Program | ACCT | CARD | XREF | CUST | TRAN | TCATBAL | DISCGRP | TRANCAT | TRANTYP | USRSEC | DALYTRAN | DALYREJS |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `CBACT01C` | CR |  |  |  |  |  |  |  |  |  |  |  |
| `CBACT02C` |  | R |  |  |  |  |  |  |  |  |  |  |
| `CBACT03C` |  |  | R |  |  |  |  |  |  |  |  |  |
| `CBACT04C` | RU |  | R |  | C | R | R |  |  |  |  |  |
| `CBCUS01C` |  |  |  | R |  |  |  |  |  |  |  |  |
| `CBEXPORT` | R | R | R | R | R |  |  |  |  |  |  |  |
| `CBIMPORT` | C |  | C | C | C |  |  |  |  |  |  |  |
| `CBSTM03A` |  |  |  |  |  |  |  |  |  |  |  |  |
| `CBSTM03B` |  |  |  |  |  |  |  |  |  |  |  |  |
| `CBTRN01C` |  |  |  |  |  |  |  |  |  |  | R |  |
| `CBTRN02C` | RU |  | R |  | C | CRU |  |  |  |  | R | C |
| `CBTRN03C` |  |  | R |  | R |  |  | R | R |  |  |  |
| `COACCT01` | R |  |  |  |  |  |  |  |  |  |  |  |
| `COACTUPC` | RU |  | R | RU |  |  |  |  |  |  |  |  |
| `COACTVWC` | R |  | R | R |  |  |  |  |  |  |  |  |
| `COBIL00C` | RU |  | R |  | CR |  |  |  |  |  |  |  |
| `COBTUPDT` |  |  |  |  |  |  |  |  |  |  |  |  |
| `COCRDLIC` |  | R |  |  |  |  |  |  |  |  |  |  |
| `COCRDSLC` |  | R |  |  |  |  |  |  |  |  |  |  |
| `COCRDUPC` |  | RU |  |  |  |  |  |  |  |  |  |  |
| `COPAUA0C` | R |  | R | R |  |  |  |  |  |  |  |  |
| `COPAUS0C` | R |  | R | R |  |  |  |  |  |  |  |  |
| `COSGN00C` |  |  |  |  |  |  |  |  |  | R |  |  |
| `COTRN00C` |  |  |  |  | R |  |  |  |  |  |  |  |
| `COTRN01C` |  |  |  |  | R |  |  |  |  |  |  |  |
| `COTRN02C` |  |  | R |  | CR |  |  |  |  |  |  |  |
| `COUSR00C` |  |  |  |  |  |  |  |  |  | R |  |  |
| `COUSR01C` |  |  |  |  |  |  |  |  |  | C |  |  |
| `COUSR02C` |  |  |  |  |  |  |  |  |  | RU |  |  |
| `COUSR03C` |  |  |  |  |  |  |  |  |  | DR |  |  |
| `PAUDBLOD` |  |  |  |  |  |  |  |  |  |  |  |  |
| `PAUDBUNL` |  |  |  |  |  |  |  |  |  |  |  |  |

**Column legend.** ACCT = `ACCTDATA` KSDS (`ACCTDAT`), CARD = `CARDDATA` KSDS (`CARDDAT` / `CARDAIX`), XREF = `CARDXREF` KSDS (`CCXREF` / `CXACAIX`), CUST = `CUSTDATA` KSDS (`CUSTDAT`), TRAN = `TRANSACT` KSDS (`TRANSACT`), TCATBAL = `TCATBALF` KSDS, DISCGRP = `DISCGRP` KSDS, TRANCAT = `TRANCATG` KSDS, TRANTYP = `TRANTYPE` KSDS, USRSEC = `USRSEC` KSDS, DALYTRAN = `DALYTRAN` PS, DALYREJS = `DALYREJS` GDG (PS).

> Programs whose only I/O targets fall outside these twelve core files show an empty row above; their full access is in the per-program detail below. This includes `CBSTM03A` (statement PS/HTML output), `CBIMPORT`/`CBEXPORT` (import/export sequential files) and the DB2/IMS/MQ programs (`COBTUPDT`, `COPAUS2C`, `CBPAUP0C`, `PAUDBLOD`, `PAUDBUNL`, `DBUNLDGS`, `COACCT01`, `CODATE01`), which are summarised in the DB2/IMS/MQ section.

## Per-Program Detail

Physical datasets are shown with the high-level qualifier `AWS.M2.CARDDEMO` abbreviated to `…`.

### `CBACT01C` (batch) — batch DD map from `READACCT.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTFILE-FILE` | `ACCTFILE` | `….ACCTDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `ARRY-FILE` | `ARRYFILE` | `….ACCTDATA.ARRYPS` | OPEN-OUTPUT, WRITE | C |
| `OUT-FILE` | `OUTFILE` | `….ACCTDATA.PSCOMP` | OPEN-OUTPUT, WRITE | C |
| `VBRC-FILE` | `VBRCFILE` | `….ACCTDATA.VBPS` | OPEN-OUTPUT, WRITE | C |

### `CBACT02C` (batch) — batch DD map from `READCARD.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CARDFILE-FILE` | `CARDFILE` | `….CARDDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBACT03C` (batch) — batch DD map from `READXREF.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `XREFFILE-FILE` | `XREFFILE` | `….CARDXREF.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBACT04C` (batch) — batch DD map from `INTCALC.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCOUNT-FILE` | `ACCTFILE` | `….ACCTDATA.VSAM.KSDS` | OPEN-I-O, READ, REWRITE | RU |
| `DISCGRP-FILE` | `DISCGRP` | `….DISCGRP.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `TCATBAL-FILE` | `TCATBALF` | `….TCATBALF.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `TRANSACT-FILE` | `TRANSACT` | `….SYSTRAN(+1)` | OPEN-OUTPUT, WRITE | C |
| `XREF-FILE` | `XREFFILE` | `….CARDXREF.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBCUS01C` (batch) — batch DD map from `READCUST.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CUSTFILE-FILE` | `CUSTFILE` | `….CUSTDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBEXPORT` (batch) — batch DD map from `CBEXPORT.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCOUNT-INPUT` | `ACCTFILE` | `….ACCTDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `CARD-INPUT` | `CARDFILE` | `….CARDDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `CUSTOMER-INPUT` | `CUSTFILE` | `….CUSTDATA.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `EXPORT-OUTPUT` | `EXPFILE` | `….EXPORT.DATA` | OPEN-OUTPUT, WRITE | C |
| `TRANSACTION-INPUT` | `TRANSACT` | `….TRANSACT.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `XREF-INPUT` | `XREFFILE` | `….CARDXREF.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBIMPORT` (batch) — batch DD map from `CBIMPORT.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCOUNT-OUTPUT` | `ACCTOUT` | `….ACCTDATA.IMPORT` | OPEN-OUTPUT | C |
| `CARD-OUTPUT` | `CARDOUT` | `(assigned at run-time)` | OPEN-OUTPUT | C |
| `CUSTOMER-OUTPUT` | `CUSTOUT` | `….CUSTDATA.IMPORT` | OPEN-OUTPUT | C |
| `ERROR-OUTPUT` | `ERROUT` | `….IMPORT.ERRORS` | OPEN-OUTPUT, WRITE | C |
| `EXPORT-INPUT` | `EXPFILE` | `….EXPORT.DATA` | OPEN-INPUT, READ | R |
| `TRANSACTION-OUTPUT` | `TRNXOUT` | `….TRANSACT.IMPORT` | OPEN-OUTPUT | C |
| `XREF-OUTPUT` | `XREFOUT` | `….CARDXREF.IMPORT` | OPEN-OUTPUT | C |

### `CBSTM03A` (batch) — batch DD map from `CREASTMT.JCL`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `HTML-FILE` | `HTMLFILE` | `….STATEMNT.HTML` | OPEN-OUTPUT, WRITE | C |
| `STMT-FILE` | `STMTFILE` | `….STATEMNT.PS` | OPEN-OUTPUT, WRITE | C |

### `CBSTM03B` (batch)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCT-FILE` | `ACCTFILE` | `(assigned at run-time)` | OPEN-INPUT, READ | R |
| `CUST-FILE` | `CUSTFILE` | `(assigned at run-time)` | OPEN-INPUT, READ | R |
| `TRNX-FILE` | `TRNXFILE` | `(assigned at run-time)` | OPEN-INPUT | R |
| `XREF-FILE` | `XREFFILE` | `(assigned at run-time)` | OPEN-INPUT | R |

### `CBTRN01C` (batch) — no JCL in repo (see report 06)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCOUNT-FILE` | `ACCTFILE` | `(assigned at run-time)` | OPEN-INPUT, READ | R |
| `CARD-FILE` | `CARDFILE` | `(assigned at run-time)` | OPEN-INPUT | R |
| `CUSTOMER-FILE` | `CUSTFILE` | `(assigned at run-time)` | OPEN-INPUT | R |
| `DALYTRAN-FILE` | `DALYTRAN` | `(assigned at run-time)` | OPEN-INPUT, READ | R |
| `TRANSACT-FILE` | `TRANFILE` | `(assigned at run-time)` | OPEN-INPUT | R |
| `XREF-FILE` | `XREFFILE` | `(assigned at run-time)` | OPEN-INPUT, READ | R |

### `CBTRN02C` (batch) — batch DD map from `POSTTRAN.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCOUNT-FILE` | `ACCTFILE` | `….ACCTDATA.VSAM.KSDS` | OPEN-I-O, READ, REWRITE | RU |
| `DALYREJS-FILE` | `DALYREJS` | `….DALYREJS(+1)` | OPEN-OUTPUT, WRITE | C |
| `DALYTRAN-FILE` | `DALYTRAN` | `….DALYTRAN.PS` | OPEN-INPUT, READ | R |
| `TCATBAL-FILE` | `TCATBALF` | `….TCATBALF.VSAM.KSDS` | OPEN-I-O, READ, REWRITE, WRITE | CRU |
| `TRANSACT-FILE` | `TRANFILE` | `….TRANSACT.VSAM.KSDS` | OPEN-OUTPUT, WRITE | C |
| `XREF-FILE` | `XREFFILE` | `….CARDXREF.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `CBTRN03C` (batch) — batch DD map from `TRANREPT.jcl`

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `DATE-PARMS-FILE` | `DATEPARM` | `….DATEPARM` | OPEN-INPUT, READ | R |
| `REPORT-FILE` | `TRANREPT` | `….TRANREPT(+1)` | OPEN-OUTPUT, WRITE | C |
| `TRANCATG-FILE` | `TRANCATG` | `….TRANCATG.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `TRANSACT-FILE` | `TRANFILE` | `….TRANSACT.DALY(+1)` | OPEN-INPUT, READ | R |
| `TRANTYPE-FILE` | `TRANTYPE` | `….TRANTYPE.VSAM.KSDS` | OPEN-INPUT, READ | R |
| `XREF-FILE` | `CARDXREF` | `….CARDXREF.VSAM.KSDS` | OPEN-INPUT, READ | R |

### `COACCT01` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ | R |

### `COACTUPC` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ, REWRITE | RU |
| `CUSTDAT (CICS)` | `CUSTDAT` | `….CUSTDATA.VSAM.KSDS` | READ, REWRITE | RU |
| `CXACAIX (CICS)` | `CXACAIX` | `….CARDXREF.VSAM.AIX.PATH` | READ | R |

### `COACTVWC` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ | R |
| `CUSTDAT (CICS)` | `CUSTDAT` | `….CUSTDATA.VSAM.KSDS` | READ | R |
| `CXACAIX (CICS)` | `CXACAIX` | `….CARDXREF.VSAM.AIX.PATH` | READ | R |

### `COBIL00C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ, REWRITE | RU |
| `CXACAIX (CICS)` | `CXACAIX` | `….CARDXREF.VSAM.AIX.PATH` | READ | R |
| `TRANSACT (CICS)` | `TRANSACT` | `….TRANSACT.VSAM.KSDS` | ENDBR, READ, STARTBR, WRITE | CR |

### `COBTUPDT` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `TR-RECORD` | `INPFILE` | `(assigned at run-time)` | OPEN-INPUT, READ | R |

### `COCRDLIC` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CARDDAT (CICS)` | `CARDDAT` | `….CARDDATA.VSAM.KSDS` | ENDBR, READ, STARTBR | R |

### `COCRDSLC` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CARDAIX (CICS)` | `CARDAIX` | `….CARDDATA.VSAM.AIX.PATH` | READ | R |
| `CARDDAT (CICS)` | `CARDDAT` | `….CARDDATA.VSAM.KSDS` | READ | R |

### `COCRDUPC` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CARDDAT (CICS)` | `CARDDAT` | `….CARDDATA.VSAM.KSDS` | READ, REWRITE | RU |

### `COPAUA0C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ | R |
| `CCXREF (CICS)` | `CCXREF` | `….CARDXREF.VSAM.KSDS` | READ | R |
| `CUSTDAT (CICS)` | `CUSTDAT` | `….CUSTDATA.VSAM.KSDS` | READ | R |

### `COPAUS0C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `ACCTDAT (CICS)` | `ACCTDAT` | `….ACCTDATA.VSAM.KSDS` | READ | R |
| `CUSTDAT (CICS)` | `CUSTDAT` | `….CUSTDATA.VSAM.KSDS` | READ | R |
| `CXACAIX (CICS)` | `CXACAIX` | `….CARDXREF.VSAM.AIX.PATH` | READ | R |

### `COSGN00C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `USRSEC (CICS)` | `USRSEC` | `….USRSEC.VSAM.KSDS` | READ | R |

### `COTRN00C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `TRANSACT (CICS)` | `TRANSACT` | `….TRANSACT.VSAM.KSDS` | ENDBR, READ, STARTBR | R |

### `COTRN01C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `TRANSACT (CICS)` | `TRANSACT` | `….TRANSACT.VSAM.KSDS` | READ | R |

### `COTRN02C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `CCXREF (CICS)` | `CCXREF` | `….CARDXREF.VSAM.KSDS` | READ | R |
| `CXACAIX (CICS)` | `CXACAIX` | `….CARDXREF.VSAM.AIX.PATH` | READ | R |
| `TRANSACT (CICS)` | `TRANSACT` | `….TRANSACT.VSAM.KSDS` | ENDBR, READ, STARTBR, WRITE | CR |

### `COUSR00C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `USRSEC (CICS)` | `USRSEC` | `….USRSEC.VSAM.KSDS` | ENDBR, READ, STARTBR | R |

### `COUSR01C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `USRSEC (CICS)` | `USRSEC` | `….USRSEC.VSAM.KSDS` | WRITE | C |

### `COUSR02C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `USRSEC (CICS)` | `USRSEC` | `….USRSEC.VSAM.KSDS` | READ, REWRITE | RU |

### `COUSR03C` (CICS online)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `USRSEC (CICS)` | `USRSEC` | `….USRSEC.VSAM.KSDS` | DELETE, READ | DR |

### `PAUDBLOD` (batch)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `INFILE1` | `INFILE1` | `(assigned at run-time)` | OPEN-INPUT, READ | R |
| `INFILE2` | `INFILE2` | `(assigned at run-time)` | OPEN-INPUT, READ | R |

### `PAUDBUNL` (batch)

| Logical / CICS file | DDNAME / FCT | Physical dataset | Access verbs | CRUD |
| :--- | :--- | :--- | :--- | :---: |
| `OPFILE1` | `OUTFIL1` | `(assigned at run-time)` | OPEN-OUTPUT, WRITE | C |
| `OPFILE2` | `OUTFIL2` | `(assigned at run-time)` | OPEN-OUTPUT, WRITE | C |

## DB2 / IMS / MQ Access (optional modules)

Detected in the optional-module programs by scanning for `EXEC SQL` (DB2), `CALL 'CBLTDLI'` (IMS DL/I) and `MQOPEN/MQGET/MQPUT/MQPUT1/MQCLOSE` (WebSphere MQ).

| Program | Module | DB2 | IMS | MQ | Notes |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CBPAUP0C` | ims-db2-mq |  | ✔ |  | IMS BMP purge program (run via `DFSRRC00`). |
| `COACCT01` | vsam-mq |  |  | ✔ | CICS + MQ account inquiry over MQ (VSAM-MQ module). |
| `COBTUPDT` | trantype-db2 | ✔ |  |  | DB2 batch update of transaction-type tables (run via `IKJEFT01`). |
| `CODATE01` | vsam-mq |  |  | ✔ | CICS + MQ date service over MQ (VSAM-MQ module). |
| `COPAUA0C` | ims-db2-mq |  | ✔ | ✔ | MQ-triggered CICS program; also reads `CCXREF`, `ACCTDAT`, `CUSTDAT`. |
| `COPAUS0C` | ims-db2-mq |  | ✔ |  | CICS/IMS pending-auth inquiry; reads `CXACAIX`, `ACCTDAT`, `CUSTDAT`. |
| `COPAUS1C` | ims-db2-mq |  | ✔ |  | CICS/IMS pending-auth detail; `LINK`s to the fraud-check program. |
| `COPAUS2C` | ims-db2-mq | ✔ |  |  | DB2 `EXEC SQL` against the pending-authorization summary. |
| `COTRTLIC` | trantype-db2 | ✔ |  |  | CICS + DB2 transaction-type list. |
| `COTRTUPC` | trantype-db2 | ✔ |  |  | CICS + DB2 transaction-type update. |
| `DBUNLDGS` | ims-db2-mq |  | ✔ |  | IMS GSAM unload (`CBLTDLI`). |
| `PAUDBLOD` | ims-db2-mq |  | ✔ |  | IMS database load (`CBLTDLI`). |
| `PAUDBUNL` | ims-db2-mq |  | ✔ |  | IMS database unload (`CBLTDLI`). |
