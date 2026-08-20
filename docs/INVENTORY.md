# CardDemo Legacy Inventory (source of truth for migration coverage)

Scope: every COBOL program under `app/cbl/` and `app/app-*/cbl/`, every copybook in `app/cpy/`,
every BMS map in `app/bms/`, every JCL job in `app/jcl/` + procs in `app/proc/`, and every seed
dataset in `app/data/`. Target of the migration is the Maven multi-module project `java-migration/`.

Legend for **Kind**: `ONLINE` = CICS/BMS pseudo-conversational program, `BATCH` = JCL-driven batch,
`SUBROUTINE` = statically called utility.

## 1. Core application programs (`app/cbl/`, 31 programs)

| Program | Kind | CICS tran | BMS map(s) | Files read/written | Copybooks | Calls / XCTL | Java target |
|---|---|---|---|---|---|---|---|
| COSGN00C | ONLINE | CC00 | COSGN00 (COSGN0A) | USRSEC (R) | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA | XCTL COMEN01C / COADM01C | `web`: `SignonController`, `SignonService`, Spring Security `UserDetailsService` |
| COMEN01C | ONLINE | CM00 | COMEN01 (COMEN1A) | – | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | XCTL menu option pgm | `web`: `MenuController` (main menu option table from COMEN02Y) |
| COADM01C | ONLINE | CA00 | COADM01 (COADM1A) | – | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y | XCTL admin option pgm | `web`: `AdminMenuController` (admin option table from COADM02Y) |
| COACTVWC | ONLINE | CAVW | COACTVW | ACCTDAT (R), CXACAIX (R), CUSTDAT (R) | CVCRD01Y, COCOM01Y, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y | XCTL CDEMO-TO-PROGRAM | `web`: `AccountViewController` / `AccountViewService` |
| COACTUPC | ONLINE | CAUP | COACTUP | ACCTDAT (R/U), CXACAIX (R), CUSTDAT (R/U) | as COACTVWC + CSLKPCDY, CSSETATY | XCTL CDEMO-TO-PROGRAM | `web`: `AccountUpdateController` / `AccountUpdateService` (full field validation, optimistic re-read before update) |
| COCRDLIC | ONLINE | CCLI | COCRDLI | CARDDAT (browse) | CVCRD01Y, COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY | XCTL COCRDSLC/COCRDUPC/menu | `web`: `CardListController` (paged browse, 7 rows/page, select-one semantics) |
| COCRDSLC | ONLINE | CCDL | COCRDSL | CARDDAT (R), CARDAIX (R) | CVCRD01Y, COCOM01Y, COCRDSL, CSUSR01Y, CVACT02Y, CVCUS01Y … | XCTL CDEMO-TO-PROGRAM | `web`: `CardDetailController` |
| COCRDUPC | ONLINE | CCUP | COCRDUP | CARDDAT (R/U) | CVCRD01Y, COCOM01Y, COCRDUP, CVACT02Y … | XCTL CDEMO-TO-PROGRAM | `web`: `CardUpdateController` |
| COTRN00C | ONLINE | CT00 | COTRN00 | TRANSACT (browse) | COCOM01Y, COTRN00, CVTRA05Y … | XCTL CDEMO-TO-PROGRAM | `web`: `TransactionListController` (forward/backward paging) |
| COTRN01C | ONLINE | CT01 | COTRN01 | TRANSACT (R) | COCOM01Y, COTRN01, CVTRA05Y … | XCTL CDEMO-TO-PROGRAM | `web`: `TransactionViewController` |
| COTRN02C | ONLINE | CT02 | COTRN02 | TRANSACT (W), CCXREF (R), CXACAIX (R), ACCTDAT (R) | COCOM01Y, COTRN02, CVTRA05Y, CVACT01Y, CVACT03Y | CALL CSUTLDTC | `web`: `TransactionAddController` (date validation via CSUTLDTC port) |
| COBIL00C | ONLINE | CB00 | COBIL00 | ACCTDAT (R/U), CXACAIX (R), TRANSACT (R/W) | COCOM01Y, COBIL00, CVACT01Y, CVACT03Y, CVTRA05Y | XCTL CDEMO-TO-PROGRAM | `web`: `BillPayController` (full-balance payment, writes payment transaction, zeroes balance) |
| CORPT00C | ONLINE | CR00 | CORPT00 | TDQ write (JOBS queue → submits TRANREPT) | COCOM01Y, CORPT00, CVTRA05Y | CALL CSUTLDTC | `web`: `ReportController` (monthly/yearly/custom → launches Spring Batch `transactionReportJob`) |
| COUSR00C | ONLINE | CU00 | COUSR00 | USRSEC (browse) | COCOM01Y, COUSR00, CSUSR01Y | XCTL CDEMO-TO-PROGRAM | `web`: `UserListController` |
| COUSR01C | ONLINE | CU01 | COUSR01 | USRSEC (W) | COCOM01Y, COUSR01, CSUSR01Y | XCTL CDEMO-TO-PROGRAM | `web`: `UserAddController` |
| COUSR02C | ONLINE | CU02 | COUSR02 | USRSEC (R/U) | COCOM01Y, COUSR02, CSUSR01Y | XCTL CDEMO-TO-PROGRAM | `web`: `UserUpdateController` |
| COUSR03C | ONLINE | CU03 | COUSR03 | USRSEC (R/D) | COCOM01Y, COUSR03, CSUSR01Y | XCTL CDEMO-TO-PROGRAM | `web`: `UserDeleteController` |
| CBACT01C | BATCH | – | – | ACCTFILE (R), OUTFILE/ARRYFILE/VBRCFILE (W) | CVACT01Y, CODATECN | CALL COBDATFT | `batch`: `accountReportJob` (READACCT.jcl) |
| CBACT02C | BATCH | – | – | CARDFILE (R) | CVACT02Y | – | `batch`: `cardListJob` (READCARD.jcl) |
| CBACT03C | BATCH | – | – | XREFFILE (R) | CVACT03Y | – | `batch`: `xrefListJob` (READXREF.jcl) |
| CBACT04C | BATCH | – | – | TCATBALF (R/U), XREFFILE (R), ACCTFILE (R/U), DISCGRP (R), TRANSACT (W) | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y | – | `batch`: `interestCalcJob` (INTCALC.jcl) |
| CBCUS01C | BATCH | – | – | CUSTFILE (R) | CVCUS01Y | – | `batch`: `customerListJob` (READCUST.jcl) |
| CBTRN01C | BATCH | – | – | DALYTRAN (R), CUSTFILE/XREFFILE/CARDFILE/ACCTFILE/TRANFILE (R) | CVTRA06Y, CVCUS01Y, CVACT03Y, CVACT02Y, CVACT01Y, CVTRA05Y | – | `batch`: `dailyTransactionValidateJob` |
| CBTRN02C | BATCH | – | – | DALYTRAN (R), TRANFILE (W), XREFFILE (R), DALYREJS (W), ACCTFILE (R/U), TCATBALF (R/U) | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y | – | `batch`: `postTransactionsJob` (POSTTRAN.jcl) |
| CBTRN03C | BATCH | – | – | TRANFILE (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATEPARM (R), TRANREPT (W) | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y | – | `batch`: `transactionReportJob` (TRANREPT.jcl/prc) |
| CBSTM03A | BATCH | – | – | STMTFILE (W), HTMLFILE (W) + via CBSTM03B: ACCTFILE, CUSTFILE, XREFFILE, TRNXFILE | COSTM01, CVACT01Y, CVCUS01Y, CVACT03Y | CALL CBSTM03B | `batch`: `statementGenerationJob` (CREASTMT.JCL) |
| CBSTM03B | SUBROUTINE | – | – | ACCTFILE, CUSTFILE, XREFFILE, TRNXFILE (indexed I/O service) | CVACT01Y, CVCUS01Y, CVACT03Y, COSTM01 | called by CBSTM03A | `batch`: `StatementDataService` |
| CBEXPORT | BATCH | – | – | CUSTFILE/ACCTFILE/XREFFILE/TRANSACT/CARDFILE (R), EXPFILE (W) | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT | – | `batch`: `branchExportJob` (CBEXPORT.jcl) |
| CBIMPORT | BATCH | – | – | EXPFILE (R), CUSTOUT/ACCTOUT/XREFOUT/TRNXOUT/CARDOUT/ERROUT (W) | same as CBEXPORT | – | `batch`: `branchImportJob` (CBIMPORT.jcl) |
| CSUTLDTC | SUBROUTINE | – | – | – | CSUTLDPY (via callers) | called by CORPT00C, COTRN02C | `common`: `DateValidator` (CEEDAYS-equivalent) |
| COBSWAIT | BATCH | – | – | – | – | CALL MVSWAIT | `batch`: `WaitStepTasklet` (WAITSTEP.jcl) |

## 2. Extension programs

### `app/app-authorization-ims-db2-mq/cbl/` (IMS DB + DB2 + MQ)
| Program | Kind | CICS tran | Data access | Java target |
|---|---|---|---|---|
| COPAUA0C | ONLINE (MQ-triggered) | CP00 | MQ GET/PUT1 auth requests, IMS `PAUTSUM`/`PAUTDTL` segments (GU/ISRT/REPL), VSAM ACCTDAT/CCXREF/CUSTDAT | `web`+JMS: `AuthorizationRequestListener`, `AuthorizationService` |
| COPAUS0C | ONLINE | CPVS | IMS auth summary browse (GU/GNP), ACCTDAT, CXACAIX, CUSTDAT | `web`: `AuthorizationSummaryController` |
| COPAUS1C | ONLINE | CPVD | IMS auth detail (GU/GNP/REPL) | `web`: `AuthorizationDetailController` |
| COPAUS2C | ONLINE (LINK) | – | DB2 `CARDDEMO.AUTHFRDS` insert (fraud report) | `web`: `FraudReportService` |
| CBPAUP0C | BATCH | – | IMS purge (GN/GNP/DLET/CHKP) of expired authorizations | `batch`: `authorizationPurgeJob` |
| PAUDBLOD | BATCH | – | IMS load from INFILE1/INFILE2 (CBLTDLI) | `dataload`: `AuthorizationSeedLoader` |
| PAUDBUNL | BATCH | – | IMS unload to OUTFIL1/OUTFIL2 | `batch`: `authorizationUnloadJob` |
| DBUNLDGS | BATCH | – | IMS generic segment unload | `batch`: `authorizationSegmentUnloadJob` |

### `app/app-transaction-type-db2/cbl/` (DB2 CRUD)
| Program | Kind | CICS tran | Data access | Java target |
|---|---|---|---|---|
| COTRTLIC | ONLINE | CTLI | DB2 `CARDDEMO.TRANSACTION_TYPE` cursor browse | `web`: `TransactionTypeListController` |
| COTRTUPC | ONLINE | CTTU | DB2 transaction-type add/update/delete | `web`: `TransactionTypeMaintenanceController` |
| COBTUPDT | BATCH | – | INPFILE → DB2 transaction-type batch update | `batch`: `transactionTypeBatchUpdateJob` |

### `app/app-vsam-mq/cbl/` (MQ-driven inquiry)
| Program | Kind | CICS tran | Data access | Java target |
|---|---|---|---|---|
| COACCT01 | ONLINE (MQ) | CDRA | MQ request/reply, ACCTDAT read | JMS `AccountInquiryListener` |
| CODATE01 | ONLINE (MQ) | CDRD | MQ request/reply, date conversion | JMS `DateInquiryListener` |

## 3. Call graph (XCTL / LINK / CALL)

```
COSGN00C ──XCTL──> COMEN01C (user)  ──XCTL(option)──> COACTVWC | COACTUPC | COCRDLIC | COCRDSLC |
        └─XCTL──> COADM01C (admin)                     COCRDUPC | COTRN00C | COTRN01C | COTRN02C |
                    └─XCTL(option)──> COUSR00C |        CORPT00C | COBIL00C | COPAUS0C
                                      COUSR01C |
                                      COUSR02C |
                                      COUSR03C |
                                      COTRTLIC ──XCTL──> COTRTUPC
COCRDLIC ──XCTL──> COCRDSLC | COCRDUPC | COMEN01C
COTRN00C ──XCTL──> COTRN01C
COPAUS0C ──XCTL──> COPAUS1C ──LINK──> COPAUS2C
Every online program ──XCTL(PF3)──> its caller (CDEMO-FROM-PROGRAM in COCOM01Y COMMAREA)
CORPT00C ──WRITEQ TD(JOBS)──> submits TRANREPT job (CBTRN03C)
COTRN02C, CORPT00C ──CALL──> CSUTLDTC ──> CSUTLDPY/CSUTLDWY date edit routines
CBACT01C ──CALL──> COBDATFT (CODATECN date conversion)
CBSTM03A ──CALL──> CBSTM03B (indexed file I/O service)
COPAUA0C ──MQ──> IMS PAUTSUM/PAUTDTL, ──MQPUT1──> reply queue
```

Navigation state is carried in the `CARDDEMO-COMMAREA` (COCOM01Y): from/to tran id, from/to program,
user id, user type (`A`/`U`), program context (enter/re-enter), selected customer/account/card.
In Java this becomes explicit REST request/response payloads plus the authenticated principal —
no server-side conversation state.

## 4. Data lineage (VSAM/DB2/IMS → PostgreSQL)

| Legacy dataset (DD / CICS FCT) | Record layout | Key | Seed file | Target table |
|---|---|---|---|---|
| USRSEC (`AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`) | CSUSR01Y | SEC-USR-ID | `EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS` | `usrsec` |
| CUSTDAT / CUSTFILE | CVCUS01Y (500) | CUST-ID | `ASCII/custdata.txt` | `customer` |
| ACCTDAT / ACCTFILE | CVACT01Y (300) | ACCT-ID | `ASCII/acctdata.txt` | `account` |
| CARDDAT / CARDFILE (+CARDAIX on ACCT-ID) | CVACT02Y (150) | CARD-NUM | `ASCII/carddata.txt` | `card` |
| CCXREF / XREFFILE (+CXACAIX on ACCT-ID) | CVACT03Y (50) | XREF-CARD-NUM | `ASCII/cardxref.txt` | `card_xref` |
| TRANSACT / TRANFILE | CVTRA05Y (350) | TRAN-ID | produced by batch | `transaction` |
| DALYTRAN | CVTRA06Y (350) | DALYTRAN-ID | `ASCII/dailytran.txt` | `daily_transaction` |
| DALYREJS | CVTRA06Y + reason (430) | – | batch output | `daily_transaction_reject` |
| TCATBALF | CVTRA01Y (50) | ACCT-ID+TYPE+CAT | `ASCII/tcatbal.txt` | `tran_category_balance` |
| DISCGRP | CVTRA02Y (50) | GROUP+TYPE+CAT | `ASCII/discgrp.txt` | `disclosure_group` |
| TRANTYPE | CVTRA03Y (60) | TRAN-TYPE | `ASCII/trantype.txt` | `transaction_type` |
| TRANCATG | CVTRA04Y (60) | TYPE+CAT | `ASCII/trancatg.txt` | `transaction_category` |
| EXPFILE | CVEXPORT | – | `EBCDIC/AWS.M2.CARDDEMO.EXPORT.DATA.PS` | export/import flat file (batch I/O) |
| DB2 `CARDDEMO.TRANSACTION_TYPE` | CVTRA03Y-like | TR_TYPE | DDL in extension | `transaction_type` (shared) |
| DB2 `CARDDEMO.AUTHFRDS` | extension copybook | – | – | `auth_fraud_report` |
| IMS `PAUTSUM` / `PAUTDTL` segments | extension copybooks | card num / auth key | extension seed files | `auth_summary`, `auth_detail` |

Signed numeric seed fields are ASCII **zoned decimal with trailing sign overpunch**
(`{`=+0 … `I`=+9, `}`=-0 … `R`=-9); loaders must decode this, not `Integer.parseInt`.

## 5. BMS maps → React views (`app/bms/`, 17 mapsets)

| BMS mapset (map) | Program | React view |
|---|---|---|
| COSGN00 (COSGN0A) | COSGN00C | `SignonView` |
| COMEN01 (COMEN1A) | COMEN01C | `MainMenuView` |
| COADM01 (COADM1A) | COADM01C | `AdminMenuView` |
| COACTVW | COACTVWC | `AccountViewView` |
| COACTUP | COACTUPC | `AccountUpdateView` |
| COCRDLI | COCRDLIC | `CardListView` |
| COCRDSL | COCRDSLC | `CardDetailView` |
| COCRDUP | COCRDUPC | `CardUpdateView` |
| COTRN00 | COTRN00C | `TransactionListView` |
| COTRN01 | COTRN01C | `TransactionViewView` |
| COTRN02 | COTRN02C | `TransactionAddView` |
| COBIL00 | COBIL00C | `BillPayView` |
| CORPT00 | CORPT00C | `ReportsView` |
| COUSR00 | COUSR00C | `UserListView` |
| COUSR01 | COUSR01C | `UserAddView` |
| COUSR02 | COUSR02C | `UserUpdateView` |
| COUSR03 | COUSR03C | `UserDeleteView` |
| COPAU0A/COPAU1A (extension) | COPAUS0C/COPAUS1C | `AuthorizationSummaryView`, `AuthorizationDetailView` |
| extension transaction-type maps | COTRTLIC/COTRTUPC | `TransactionTypeListView`, `TransactionTypeUpdateView` |

## 6. JCL jobs and procs → Spring Batch

| JCL | Steps (program) | Spring Batch mapping |
|---|---|---|
| POSTTRAN.jcl | STEP15 CBTRN02C | `postTransactionsJob` |
| INTCALC.jcl | STEP15 CBACT04C PARM='2022071800' | `interestCalcJob` (job parameter `runDate`) |
| TRANREPT.jcl + proc/TRANREPT.prc, REPROC.prc | STEP05R REPROC (unload), STEP05R SORT (filter by proc date, sort by card), STEP10R CBTRN03C | `transactionReportJob`: unload step → filter/sort step → report step |
| CREASTMT.JCL | DELDEF01 IDCAMS, STEP010 SORT, STEP020 IDCAMS, STEP030 IEFBR14, STEP040 CBSTM03A (COND=(0,NE)) | `statementGenerationJob` (sort step then statement step, conditional flow) |
| READACCT.jcl | PREDEL IEFBR14, STEP05 CBACT01C | `accountReportJob` |
| READCARD.jcl | STEP05 CBACT02C | `cardListJob` |
| READCUST.jcl | STEP05 CBCUS01C | `customerListJob` |
| READXREF.jcl | STEP05 CBACT03C | `xrefListJob` |
| CBEXPORT.jcl | STEP01 IDCAMS, STEP02 CBEXPORT | `branchExportJob` |
| CBIMPORT.jcl | STEP01 CBIMPORT | `branchImportJob` |
| COMBTRAN.jcl | STEP05R SORT (merge), STEP10 IDCAMS REPRO | `combineTransactionsJob` |
| TRANBKP.jcl | STEP05R REPROC, STEP05/STEP10 IDCAMS (COND=(4,LT)) | `transactionBackupJob` |
| TRANIDX.jcl | IDCAMS AIX define/build | `transactionIndexJob` (no-op/index maintenance step) |
| PRTCATBL.jcl | DELDEF IEFBR14, STEP05R REPROC, STEP10R SORT | `printCategoryBalanceJob` |
| DALYREJS.jcl | STEP05 IDCAMS | `dailyRejectsInitJob` |
| ACCTFILE / CARDFILE / CUSTFILE / XREFFILE / TRANFILE / TRANTYPE / TRANCATG / TCATBALF / DISCGRP / DEFCUST / DUSRSECJ / ESDSRRDS / REPTFILE .jcl | IDCAMS DELETE/DEFINE/REPRO (+ IEBGENER) | file-init jobs → `dataload` module `*InitJob` (truncate + reload table from seed) |
| DEFGDGB.jcl / DEFGDGD.jcl | IDCAMS GDG define, IEBGENER | `gdgInitJob` (output generation directories) |
| OPENFIL.jcl / CLOSEFIL.jcl | SDSF CEMT open/close | no Java equivalent (documented as N/A) |
| WAITSTEP.jcl | WAIT COBSWAIT | `waitStepJob` |
| CBADMCDJ.jcl | DFHCSDUP CSD install | N/A (CICS resource definition) |
| FTPJCL.JCL, TXT2PDF1.JCL, INTRDRJ1/2.JCL | FTP / PDF / internal reader | utility jobs, mapped to `fileTransferJob` / documented N/A |
| app-*/jcl (extension jobs) | IMS/DB2 load, unload, purge | extension jobs in `batch` module |

## 7. Copybook inventory

| Copybook | Kind | Java target |
|---|---|---|
| CVCUS01Y, CUSTREC | Customer record (CUSTREC is a duplicate layout used by export) | `Customer` entity |
| CVACT01Y | Account record | `Account` entity |
| CVACT02Y | Card record | `Card` entity |
| CVACT03Y | Card xref record | `CardXref` entity |
| CVTRA01Y | Transaction category balance | `TranCategoryBalance` entity |
| CVTRA02Y | Disclosure group | `DisclosureGroup` entity |
| CVTRA03Y | Transaction type | `TransactionType` entity |
| CVTRA04Y | Transaction category | `TransactionCategory` entity |
| CVTRA05Y | Transaction record | `Transaction` entity |
| CVTRA06Y | Daily transaction record | `DailyTransaction` entity |
| CVTRA07Y | Transaction report line layouts | `batch` report line formatters |
| CSUSR01Y | Security user record | `SecurityUser` entity |
| CVEXPORT | Branch export record layouts | `dataload`/`batch` export DTOs |
| COSTM01 | Statement transaction layout | statement DTO |
| CVCRD01Y | Online work area (AID keys, next map/prog) | REST navigation DTO |
| COCOM01Y | COMMAREA | REST session/navigation DTO |
| COMEN02Y / COADM02Y | Menu option tables | `MenuOptions` / `AdminMenuOptions` constants |
| COTTL01Y | Screen titles | frontend constants |
| CSDAT01Y | Current date/time work fields | `common`: `DateTimeProvider` |
| CSMSG01Y / CSMSG02Y | Standard messages | `common`: `Messages` |
| CSSETATY | BMS attribute setting | frontend field-state handling |
| CSSTRPFY | PF-key string handling | frontend key mapping |
| CSLKPCDY | Lookup tables (US states, ZIP/state validation, phone area codes) | `common`: `LookupTables` |
| CSUTLDPY / CSUTLDWY | Date edit / day-of-week routines | `common`: `DateValidator`, `DayOfWeekUtil` |
| CODATECN | Date conversion record (COBDATFT) | `common`: `DateConverter` |
| UNUSED1Y | unused | N/A (documented) |
| DFHAID / DFHBMSCA | CICS BMS system copybooks | N/A (terminal attributes) |

## 8. Seed datasets

| File | Records | Layout | Notes |
|---|---|---|---|
| ASCII/custdata.txt | 50 | CVCUS01Y | 500-byte fixed |
| ASCII/acctdata.txt | 50 | CVACT01Y | 300-byte fixed, signed zoned decimals |
| ASCII/carddata.txt | 50 | CVACT02Y | 150-byte fixed |
| ASCII/cardxref.txt | 50 | CVACT03Y | 50-byte fixed |
| ASCII/dailytran.txt | 300 | CVTRA06Y | 350-byte fixed, signed amount |
| ASCII/discgrp.txt | 51 | CVTRA02Y | 50-byte fixed |
| ASCII/tcatbal.txt | 50 | CVTRA01Y | 50-byte fixed |
| ASCII/trancatg.txt | 18 | CVTRA04Y | 60-byte fixed |
| ASCII/trantype.txt | 7 | CVTRA03Y | 60-byte fixed |
| EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS | 10 | CSUSR01Y | **no ASCII equivalent** — EBCDIC (cp037) decode, 80-byte records |
| EBCDIC/AWS.M2.CARDDEMO.EXPORT.DATA.PS | – | CVEXPORT | used by import/export batch tests |

Coverage status per program/job/map/copybook is tracked in `java-migration/README.md`
(status matrix) and must be kept in sync with this inventory.
