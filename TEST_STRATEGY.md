# Migration Test Strategy

## Overview

This document defines the testing strategy for validating the migration of the CardDemo mainframe credit card management system from COBOL/CICS/VSAM to Java. The strategy is organized around four complementary testing dimensions that together provide comprehensive migration correctness assurance.

## System Under Test

CardDemo is a mainframe application managing credit card accounts, customers, cards, transactions, and billing. It comprises:

- **9 VSAM data files** (accounts, customers, cards, cross-references, transactions, transaction types, transaction categories, transaction category balances, disclosure groups)
- **Batch jobs** orchestrated via Control-M (daily transaction backup, post-transaction processing, interest calculation, statement generation, data export)
- **CICS online transactions** (account management, card operations, user administration)

---

## Dimension 1: Golden-File Testing

### Purpose

Verify that the migrated Java system produces byte-for-byte equivalent structured output when given identical input data. Golden files serve as the immutable reference for record-level correctness.

### Approach

1. **Parse** each ASCII data file (`app/data/ASCII/*.txt`) using the corresponding COBOL copybook layout definitions to produce structured JSON representations.
2. **Store** these JSON files in `golden-files/` as canonical references.
3. **Compare** Java-migrated output against golden files using field-level equality checks.

### Data Files and Copybook Mappings

| ASCII Data File    | Copybook     | Record Name            | LRECL | Records |
| :----------------- | :----------- | :--------------------- | ----: | ------: |
| `acctdata.txt`     | CVACT01Y.cpy | ACCOUNT-RECORD         |   300 |      50 |
| `custdata.txt`     | CVCUS01Y.cpy | CUSTOMER-RECORD        |   500 |      50 |
| `carddata.txt`     | CVACT02Y.cpy | CARD-RECORD            |   150 |      50 |
| `cardxref.txt`     | CVACT03Y.cpy | CARD-XREF-RECORD       |    50 |      50 |
| `dailytran.txt`    | CVTRA06Y.cpy | DALYTRAN-RECORD        |   350 |     300 |
| `trantype.txt`     | CVTRA03Y.cpy | TRAN-TYPE-RECORD       |    60 |       7 |
| `trancatg.txt`     | CVTRA04Y.cpy | TRAN-CAT-RECORD        |    60 |      18 |
| `tcatbal.txt`      | CVTRA01Y.cpy | TRAN-CAT-BAL-RECORD    |    50 |      50 |
| `discgrp.txt`      | CVTRA02Y.cpy | DIS-GROUP-RECORD       |    50 |      51 |

### Numeric Encoding

COBOL signed numeric fields (`PIC S9(n)V99`) use zoned decimal encoding in the ASCII data files:

- **Positive overpunch**: `{` = +0, `A` = +1, `B` = +2, ... `I` = +9
- **Negative overpunch**: `}` = -0, `J` = -1, `K` = -2, ... `R` = -9

The `V` (implied decimal) means no literal decimal point exists in the data; the parser must insert the decimal at the correct position based on the `V99` scale.

### Validation Criteria

- Every field in every record must match the golden reference exactly.
- Numeric fields must decode to the correct signed decimal value with proper scale.
- Alphanumeric fields must preserve leading/trailing spaces per the COBOL PIC clause width.
- Record counts must match between source and golden file.

---

## Dimension 2: Differential Testing

### Purpose

Detect behavioral divergence between the legacy COBOL batch programs and their Java equivalents by running both systems against the same input and comparing outputs field by field.

### Approach

1. **Prepare** identical input datasets for both COBOL and Java execution environments.
2. **Execute** the same logical batch operation in both environments.
3. **Normalize** outputs (trim trailing FILLER, decode signed numerics) to a common JSON representation.
4. **Diff** the normalized outputs using the test harness comparison utilities.

### Target Batch Programs

| COBOL Program | JCL Job    | Function                           | Key Inputs                              | Key Outputs                        |
| :------------ | :--------- | :--------------------------------- | :-------------------------------------- | :--------------------------------- |
| CBTRN02C      | POSTTRAN   | Post daily transactions            | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF  | TRANFILE (updated), DALYREJS, TCATBALF (updated), ACCTFILE (updated) |
| CBTRN03C      | TRANREPT   | Transaction report generation      | TRANSACT, XREFFILE, TRANTYPE, TRANCATG  | Formatted report output            |
| CBACT04C      | INTCALC    | Interest/fee calculation           | TCATBALF, XREFFILE, ACCTFILE, DISCGRP   | TRANSACT (system-generated), ACCTFILE (updated) |
| CBSTM03A      | CREASTMT   | Statement generation               | TRANSACT, XREFFILE, ACCTFILE, CUSTFILE  | Statement (text + HTML)            |
| CBEXPORT      | CBEXPORT   | Branch migration export            | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE | EXPORT.DATA                 |

### Comparison Rules

- **Exact match**: Alphanumeric fields, date fields, status codes.
- **Numeric tolerance**: Monetary amounts compared to 2 decimal places (epsilon = 0.005) to accommodate rounding differences between packed decimal and IEEE 754.
- **Timestamp tolerance**: Processing timestamps may differ; compare date portion only unless sub-second precision is required.
- **FILLER fields**: Ignored in comparison (may contain arbitrary padding).

---

## Dimension 3: Reconciliation Testing

### Purpose

Validate cross-file data integrity and business-rule invariants that must hold after migration. These checks operate at the dataset level and verify referential integrity, aggregate consistency, and conservation laws.

### Approach

1. **Load** all migrated data files into structured representations.
2. **Execute** reconciliation checks defined in `RECONCILIATION_CHECKS.md`.
3. **Report** any violations with the specific records and fields that failed.

### Reconciliation Categories

#### 3.1 Referential Integrity

| Check ID | Rule                                                                 |
| :------- | :------------------------------------------------------------------- |
| RI-001   | Every `XREF-CARD-NUM` in cardxref must exist in carddata `CARD-NUM`  |
| RI-002   | Every `XREF-ACCT-ID` in cardxref must exist in acctdata `ACCT-ID`   |
| RI-003   | Every `XREF-CUST-ID` in cardxref must exist in custdata `CUST-ID`   |
| RI-004   | Every `CARD-ACCT-ID` in carddata must exist in acctdata `ACCT-ID`   |
| RI-005   | Every `DALYTRAN-CARD-NUM` in dailytran must exist in cardxref `XREF-CARD-NUM` |
| RI-006   | Every `TRANCAT-ACCT-ID` in tcatbal must exist in acctdata `ACCT-ID` |

#### 3.2 Aggregate Consistency

| Check ID | Rule                                                                 |
| :------- | :------------------------------------------------------------------- |
| AG-001   | Sum of `TRAN-CAT-BAL` per account in tcatbal must be reconcilable with `ACCT-CURR-BAL` in acctdata |
| AG-002   | Record count per entity type must match between COBOL and Java outputs |
| AG-003   | Sum of daily transaction amounts (`DALYTRAN-AMT`) grouped by card must be consistent with cycle debit/credit totals |

#### 3.3 Business Rule Invariants

| Check ID | Rule                                                                 |
| :------- | :------------------------------------------------------------------- |
| BR-001   | Active accounts (`ACCT-ACTIVE-STATUS = 'Y'`) must have a non-past expiration date or a reissue date |
| BR-002   | `ACCT-CURR-BAL` must not exceed `ACCT-CREDIT-LIMIT` for active accounts (soft check with tolerance) |
| BR-003   | Every active card must reference an active account                    |
| BR-004   | Transaction type codes in dailytran must exist in trantype            |
| BR-005   | Transaction category codes in dailytran must exist in trancatg for the given type |
| BR-006   | Disclosure group entries must cover all transaction type/category combinations used in active accounts |

### Detailed specifications for each check are in [RECONCILIATION_CHECKS.md](./RECONCILIATION_CHECKS.md).

---

## Dimension 4: Contract Testing

### Purpose

Ensure that the Java-migrated system honors the data contracts defined by COBOL copybooks, CICS transaction interfaces, and batch job I/O specifications.

### Approach

1. **Schema contracts**: Validate that Java entity classes match the field names, types, sizes, and positions defined in COBOL copybooks.
2. **I/O contracts**: Verify that batch job inputs/outputs in Java match the DD statements and record formats defined in JCL.
3. **API contracts**: For CICS transaction equivalents (REST/gRPC endpoints), verify request/response schemas match the COMMAREA and BMS map definitions.

### Schema Contract Checks

| Copybook     | Contract                                                              |
| :----------- | :-------------------------------------------------------------------- |
| CVACT01Y.cpy | Java Account entity must have 12 fields matching PIC definitions; total serialized length = 300 bytes |
| CVCUS01Y.cpy | Java Customer entity must have 18 fields; total serialized length = 500 bytes |
| CVACT02Y.cpy | Java Card entity must have 6 fields; total serialized length = 150 bytes |
| CVACT03Y.cpy | Java CardXref entity must have 3 fields; total serialized length = 50 bytes |
| CVTRA05Y.cpy | Java Transaction entity must have 13 fields; total serialized length = 350 bytes |
| CVTRA06Y.cpy | Java DailyTransaction entity must have 13 fields; total serialized length = 350 bytes |
| CVTRA01Y.cpy | Java TranCatBalance entity must have 4 fields; total serialized length = 50 bytes |
| CVTRA02Y.cpy | Java DisclosureGroup entity must have 4 fields; total serialized length = 50 bytes |
| CVTRA03Y.cpy | Java TransactionType entity must have 2 fields; total serialized length = 60 bytes |
| CVTRA04Y.cpy | Java TransactionCategory entity must have 3 fields; total serialized length = 60 bytes |
| CVEXPORT.cpy | Java ExportRecord entity must support REDEFINES via polymorphic record types; total serialized length = 500 bytes |

### I/O Contract Checks

| JCL Job    | DD Name    | Direction | Format      | LRECL | Contract                           |
| :--------- | :--------- | :-------- | :---------- | ----: | :--------------------------------- |
| POSTTRAN   | DALYTRAN   | Input     | PS (seq)    |   350 | Daily transactions, fixed-length   |
| POSTTRAN   | DALYREJS   | Output    | PS (seq)    |   430 | Rejected transactions with reason  |
| POSTTRAN   | TRANFILE   | I/O       | VSAM KSDS   |   350 | Transaction master, keyed by TRAN-ID |
| INTCALC    | TRANSACT   | Output    | PS (seq)    |   350 | System-generated interest transactions |
| TRANREPT   | RPTFILE    | Output    | FBA         |   133 | Formatted transaction report       |
| CREASTMT   | STMTFILE   | Output    | FB          |    80 | Statement text output              |
| CREASTMT   | HTMLFILE   | Output    | FB          |   100 | Statement HTML output              |
| CBEXPORT   | EXPFILE    | Output    | VSAM KSDS   |   500 | Multi-record export file           |

### API Contract Checks (CICS Transaction Equivalents)

| CICS Trans | COBOL Program | Contract                                                   |
| :--------- | :------------ | :--------------------------------------------------------- |
| CC00       | COSGN00C      | Sign-on: accepts user ID (8) + password (8), returns auth status |
| CA00       | COMEN01C      | Main menu: returns menu option list per user type          |
| CA01       | COACTVWC      | Account view: accepts ACCT-ID (11), returns ACCOUNT-RECORD fields |
| CA02       | COACTUPC      | Account update: accepts ACCOUNT-RECORD, validates and persists |
| CB00       | COBIL00C      | Bill payment: accepts payment amount + account, updates balance |
| CR00       | CORPT00C      | Report request: accepts date range, triggers report generation |

---

## Test Execution Strategy

### Phase 1: Pre-Migration Baseline (Current)

1. Generate golden files from COBOL ASCII data using `test-harness/golden_file_generator.py`.
2. Run reconciliation checks on source data to establish baseline integrity.
3. Archive golden files in `golden-files/` as immutable references.

### Phase 2: Migration Validation

1. Run Java-migrated batch jobs against the same input datasets.
2. Compare Java output against golden files (Dimension 1).
3. Execute differential tests comparing COBOL and Java side by side (Dimension 2).
4. Run reconciliation checks on Java output (Dimension 3).
5. Validate contract compliance for all entities and interfaces (Dimension 4).

### Phase 3: Regression Suite

1. Integrate golden-file and reconciliation tests into CI/CD.
2. Run contract tests on every Java code change.
3. Differential tests run on-demand or at release milestones.

---

## Tooling

| Tool                        | Location                           | Purpose                            |
| :-------------------------- | :--------------------------------- | :--------------------------------- |
| Copybook Parser             | `test-harness/copybook_parser.py`  | Parse ASCII data using copybook layouts |
| Golden File Generator       | `test-harness/golden_file_generator.py` | Produce JSON golden references |
| Comparator                  | `test-harness/comparator.py`       | Field-level diff with tolerance support |
| Reconciliation Engine       | `test-harness/reconciliation.py`   | Cross-file integrity checks        |
| Layout Definitions          | `test-harness/layouts/definitions.py` | Python copybook field definitions |
| pytest Tests                | `test-harness/test_*.py`           | Automated test suite               |
