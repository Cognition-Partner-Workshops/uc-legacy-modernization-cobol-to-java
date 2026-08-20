# Batch mapping

This handoff ports the core CardDemo posting, interest, validation, reporting, and
statement flows to Spring Batch over the JPA domain tables.

| Legacy flow | Spring Batch job | Spring Batch step | Deliberate deviation |
| --- | --- | --- | --- |
| `POSTTRAN.jcl` / `STEP15 CBTRN02C` | `postTransactionsJob` | `postTransactionsStep` | `DALYTRAN`, transaction, reject, account, and category-balance VSAM files are JPA tables. |
| `INTCALC.jcl` / `STEP15 CBACT04C` | `interestCalculationJob` | `interestCalculationStep` | `TCATBALF`, `DISCGRP`, `XREFFILE`, and `ACCTFILE` are JPA tables; the `runDate` parameter is a Spring Batch job parameter. |
| `CBTRN01C` daily read/validation pass | `dailyTransactionValidateJob` | `dailyTransactionValidateStep` | The source only reads daily transactions and performs xref/account verification; it does not post or reject rows. Counts are kept in the execution context. |
| `TRANREPT.jcl` / `TRANREPT.prc` / `STEP10R CBTRN03C` | `transactionReportJob` | `transactionReportStep` | The unload, sort, and date-parameter files are replaced by JPA queries and `startDate`/`endDate` parameters. The report remains a configurable file. |
| `CREASTMT.JCL` / `STEP010`-`STEP040 CBSTM03A` | `createStatementsJob` | `createStatementsStep` | Sort/setup and IDCAMS steps are replaced by repository ordering and output replacement. Both plain-text and HTML output files remain configurable files. |
| `CBSTM03B` called by `CBSTM03A` | repository-backed statement data service | part of `createStatementsStep` | It is intentionally not a separate Java batch program; JPA repositories provide its indexed-file read behavior. |

Interest uses the common COBOL rounding helper at the two-decimal target
transaction scale. The COBOL `1400-COMPUTE-FEES` paragraph is empty, so the
Java interest task retains the equivalent no-op seam and does not invent fee
logic. Spring Batch metadata tables are installed by Flyway migration `V4`,
and automatic Batch schema initialization is disabled. Output defaults are
under `target/output` and can be overridden with job parameters or properties.

## Stateful posting validation

`CBTRN02C` validates each daily transaction against the account record as it
exists at that point in the sequential pass. A valid transaction rewrites the
account's current-cycle credit or debit before the next daily transaction is
read. The Java posting tasklet and its independent acceptance-test oracle
preserve this stateful overlimit behavior; the seed reconciliation is 262
processed and 38 overlimit rejects.

## Remaining JCL and procedure coverage

`CBACT01C`, `CBACT02C`, `CBACT03C`, and `CBCUS01C` read independent records
without mutable cross-record state, so their Java ports use the natural
`ItemReader` -> `ItemProcessor` -> `ItemWriter` chunk pattern. Posting remains
a tasklet because each validation observes account state changed by earlier
records; interest remains a tasklet because its account control-break and final
break apply accumulated state across rows.

| Legacy file/step | Java mapping | Notes |
| --- | --- | --- |
| `READACCT.jcl` / `CBACT01C` | `accountFilePrintJob` / `accountFilePrintStep` | Fixed, array, and variable diagnostic outputs are consolidated into one configurable display file. |
| `READCARD.jcl` / `CBACT02C` | `cardFilePrintJob` / `cardFilePrintStep` | Ordered JPA input replaces VSAM sequential input. |
| `READXREF.jcl` / `CBACT03C` | `xrefFilePrintJob` / `xrefFilePrintStep` | Ordered JPA input replaces VSAM sequential input. |
| `READCUST.jcl` / `CBCUS01C` | `customerFilePrintJob` / `customerFilePrintStep` | Ordered JPA input replaces VSAM sequential input. |
| `CBEXPORT.jcl` / `CBEXPORT` | `exportJob` / `exportStep` | Five sections are written in customer, account, xref, transaction, card order. Records are fixed at 500 characters with common type/timestamp/sequence/branch/region fields. COMP/COMP-3 values use deterministic ASCII logical values, not byte-identical mainframe binary encoding. |
| `CBIMPORT.jcl` / `CBIMPORT` | `importJob` / `importStep` | Records are validated and counted by type, then saved in FK-safe order (cards before xrefs). The non-exported category-balance rows are cleared first so account replacement satisfies relational FKs. Unknown and malformed records are counted in the execution context. |
| `COMBTRAN.jcl` / `STEP05R` + `STEP10` | `combtranJob` / `combtranStep` | Two transaction export files (`backupPath`, `systemPath`) are merged, sorted by transaction ID, and loaded into the transaction table. |
| `TRANBKP.jcl` / `REPROC` | `transactionBackupJob` / `transactionBackupStep` | Ordered table rows are written as transaction export records. |
| `PRTCATBL.jcl` | `categoryBalancePrintJob` / `categoryBalancePrintStep` | Ordered repository report replaces VSAM backup and sort. |
| `REPTFILE.jcl` | `transactionReportJob` / `transactionReportStep` | Output replacement initializes the report file. |
| `DALYREJS.jcl` | `SeedDataLoader` / `daily_transaction_reject` | Rejects are relational rows; no separate GDG definition is needed. |
| `TRANFILE.jcl` | `SeedDataLoader` transaction dataset load step | IDCAMS delete/define/REPRO is truncate-and-reload. |
| `TRANCATG.jcl` | `SeedDataLoader` transaction-category dataset load step | IDCAMS delete/define/REPRO is truncate-and-reload. |
| `TRANTYPE.jcl` | `SeedDataLoader` transaction-type dataset load step | IDCAMS delete/define/REPRO is truncate-and-reload. |
| `TCATBALF.jcl` | `SeedDataLoader` category-balance dataset load step | IDCAMS delete/define/REPRO is truncate-and-reload. |
| `DISCGRP.jcl` | `SeedDataLoader` disclosure-group dataset load step | IDCAMS delete/define/REPRO is truncate-and-reload. |
| `ACCTFILE.jcl` | `SeedDataLoader` account dataset load step | Relational schema and FK-safe load ordering replace VSAM definitions and REPRO. |
| `CARDFILE.jcl` | `SeedDataLoader` card dataset load step | Relational schema and FK-safe load ordering replace VSAM definitions and REPRO. |
| `CUSTFILE.jcl` | `SeedDataLoader` customer dataset load step | Relational schema and FK-safe load ordering replace VSAM definitions and REPRO. |
| `XREFFILE.jcl` | `SeedDataLoader` card-xref dataset load step | Relational schema and FK-safe load ordering replace VSAM definitions and REPRO. |
| `DEFCUST.jcl` | `SeedDataLoader` customer dataset load step | Relational schema and FK-safe load ordering replace VSAM definitions and REPRO. |
| `DUSRSECJ.jcl` | `SeedDataLoader` / `usrsec` load | The cp037 EBCDIC PS file is decoded into the table. |
| `TRANIDX.jcl` | Flyway schema/index definitions | Database keys and indexes replace IDCAMS alternate indexes. |
| `OPENFIL.jcl` | Intentional N/A | CICS file lifecycle is managed by the datasource and transaction manager. |
| `CLOSEFIL.jcl` | Intentional N/A | CICS file lifecycle is managed by the datasource and transaction manager. |
| `DEFGDGB.jcl` | Intentional N/A | GDG definitions have no direct Java equivalent; versioned files or database rows provide history when needed. |
| `DEFGDGD.jcl` | Intentional N/A | GDG definitions have no direct Java equivalent; versioned files or database rows provide history when needed. |
| `FTPJCL.JCL` | Intentional N/A | FTP is an environment/integration concern. |
| `TXT2PDF1.JCL` | Intentional N/A | PDF conversion is outside the current Java batch core. |
| `CBADMCDJ.jcl` | Intentional N/A | DFHCSDUP CICS administration has no local Spring Batch analogue. |
| `ESDSRRDS.jcl` | Intentional N/A | VSAM ESDS/RRDS catalog definition is replaced by relational schema. |
| `INTRDRJ1.JCL` | Intentional N/A | Internal-reader JCL submission is unnecessary with Spring Batch launchers. |
| `INTRDRJ2.JCL` | Intentional N/A | Internal-reader JCL submission is unnecessary with Spring Batch launchers. |
| `WAITSTEP.jcl` | Intentional N/A | COBSWAIT is scheduler orchestration rather than business processing. |
| `REPROC.prc` | `transactionBackupJob`, `categoryBalancePrintJob` | Reusable IDCAMS REPRO is represented by repository-backed read/write services. |
| `TRANREPT.prc` | `transactionReportJob` / `transactionReportStep` | JCL symbols become `startDate`, `endDate`, and output job parameters. |

The repository contains 38 JCL files (one more than the 37 stated in the
handoff) and both procedures; all 40 actual inventory entries are represented
above. Twenty-nine entries map to concrete Java behavior (including
repository/schema/dataload equivalents), while eleven are intentionally not
separate Java jobs because they are infrastructure, lifecycle, definition, or
integration concerns.
