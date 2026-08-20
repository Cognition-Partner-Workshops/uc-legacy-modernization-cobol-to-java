# CardDemo reconciliation

`ReconciliationAcceptanceTest` is the dedicated Testcontainers acceptance
suite for the migrated estate. It loads the fixed-width seed data into an
isolated PostgreSQL instance, then executes the JCL-equivalent sequence:

1. `postTransactionsJob` (262 processed, 38 rejected);
2. `interestCalculationJob`;
3. `transactionReportJob`;
4. `createStatementsJob`;
5. `exportJob`.

The posting expectations are independently computed from the ordered daily
transaction input while mutating a separate in-memory account model. The test
reconciles the resulting account and transaction counts and verifies the
interest balance delta independently. Report and statement output are checked
against committed marker fixtures in
`web/src/test/resources/reconciliation/`; those fixtures preserve headings,
column labels, and end markers from the generated COBOL-layout-compatible
outputs. The source set confirms these labels and separators, but does not
provide a byte-level golden file for every seed-dependent detail; the suite
therefore calls out only stable, source-confirmed layout markers.

Interest calculations use scale 8 for the intermediate division and
`HALF_UP` rounding to scale 2, matching the migrated `ROUNDED` behavior.
Balances and generated interest amounts are asserted at scale 2. The common
decimal tests also cover a `.005` half-up boundary.

The same suite exercises signon, menu, account view, confirmed account update,
filtered card list, transaction add, bill payment, and report submission.
Assertions include persisted account and transaction state, not only HTTP
status codes.
