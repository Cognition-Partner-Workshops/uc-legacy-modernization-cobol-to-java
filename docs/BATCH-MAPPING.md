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
