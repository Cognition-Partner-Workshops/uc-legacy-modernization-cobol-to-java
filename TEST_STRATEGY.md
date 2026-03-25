# CardDemo Migration Test Strategy

## Overview

This document defines the testing strategy for validating the migration of the CardDemo mainframe application (COBOL/CICS/VSAM) to a Java-based architecture. The strategy covers four complementary testing dimensions that together ensure functional equivalence, data integrity, and API compatibility between the legacy and modernized systems.

---

## 1. Golden-File Testing

### Purpose
Verify that the migrated Java application produces **byte-for-byte identical** structured output when given the same fixed-width ASCII input files that the COBOL system consumes.

### Approach
1. **Baseline capture**: Parse every ASCII data file in `app/data/ASCII/` using its corresponding COBOL copybook layout. Produce a canonical JSON representation stored in `golden-files/`.
2. **Round-trip validation**: The Java application reads the same ASCII files, produces its own in-memory representation, and serialises it to JSON. The test harness diffs the two JSON documents field-by-field.
3. **Tolerance rules**:
   - Numeric fields: exact match after stripping leading zeros and sign overpunch decoding.
   - Alphanumeric fields: trailing-space trimmed comparison.
   - Date fields: ISO-8601 string comparison.
   - FILLER fields: ignored in comparison.

### Covered Data Files

| ASCII File | Copybook | Record Length | Golden File |
|---|---|---|---|
| `acctdata.txt` | `CVACT01Y.cpy` (ACCOUNT-RECORD) | 300 | `golden-files/acctdata.json` |
| `carddata.txt` | `CVACT02Y.cpy` (CARD-RECORD) | 150 | `golden-files/carddata.json` |
| `custdata.txt` | `CVCUS01Y.cpy` (CUSTOMER-RECORD) | 500 | `golden-files/custdata.json` |
| `cardxref.txt` | `CVACT03Y.cpy` (CARD-XREF-RECORD) | 50 | `golden-files/cardxref.json` |
| `dailytran.txt` | `CVTRA06Y.cpy` (DALYTRAN-RECORD) | 350 | `golden-files/dailytran.json` |
| `trantype.txt` | `CVTRA03Y.cpy` (TRAN-TYPE-RECORD) | 60 | `golden-files/trantype.json` |
| `trancatg.txt` | `CVTRA04Y.cpy` (TRAN-CAT-RECORD) | 60 | `golden-files/trancatg.json` |
| `tcatbal.txt` | `CVTRA01Y.cpy` (TRAN-CAT-BAL-RECORD) | 50 | `golden-files/tcatbal.json` |
| `discgrp.txt` | `CVTRA02Y.cpy` (DIS-GROUP-RECORD) | 50 | `golden-files/discgrp.json` |

### Pass Criteria
- 100 % field-level match for every record in every golden file.
- Zero unmatched or extra records.

---

## 2. Differential Testing

### Purpose
Run **identical business scenarios** through both the legacy COBOL system and the migrated Java system, then compare outputs to detect behavioural divergence.

### Approach
1. **Scenario catalogue**: Define a set of representative test scenarios that exercise each batch job and online transaction:
   - **POSTTRAN**: Post a batch of daily transactions and verify the updated transaction master, rejected-transaction file, and transaction-category balance file.
   - **INTCALC**: Run interest calculation for a billing date and compare the generated system transactions.
   - **CREASTMT**: Generate account statements and compare HTML/text output line-by-line.
   - **COMBTRAN**: Merge transaction backup with system-generated transactions and compare the combined sorted output.
   - **TRANBKP**: Backup transaction master and verify record count parity.

2. **Execution model**:
   ```
   Legacy COBOL (on mainframe / GnuCOBOL) ──> output-legacy/
   Java application                        ──> output-java/
   Diff engine                             ──> diff-report.json
   ```

3. **Diff engine rules**:
   - Structural diff: record counts, key coverage.
   - Field-level diff: every non-FILLER field compared using type-aware comparators.
   - Timestamp tolerance: configurable window (default 0 ms for batch, up to 1 s for online).

### Test Scenarios

| ID | Scenario | Legacy Job/Txn | Input | Expected Output |
|---|---|---|---|---|
| DIFF-01 | Post daily transactions | POSTTRAN (CBTRN02C) | `dailytran.txt` | Updated `TRANSACT`, `TCATBALF`, `DALYREJS` |
| DIFF-02 | Interest calculation | INTCALC (CBACT04C) | `tcatbal.txt`, `discgrp.txt`, `acctdata.txt` | System-generated interest transactions |
| DIFF-03 | Statement generation | CREASTMT (CBSTM03A/B) | `TRANSACT`, `cardxref.txt`, `acctdata.txt`, `custdata.txt` | Statement text + HTML |
| DIFF-04 | Combine transactions | COMBTRAN (SORT + IDCAMS) | Backup + system trans | Sorted combined file |
| DIFF-05 | Transaction backup | TRANBKP (REPROC + IDCAMS) | `TRANSACT` | Backup sequential file |

### Pass Criteria
- Zero field-level differences in all scenarios.
- Record count match within 0 tolerance.

---

## 3. Reconciliation Testing

### Purpose
Validate **aggregate-level data invariants** that must hold after each batch job completes, independent of field-level correctness.

### Approach
1. **Pre-condition snapshots**: Before a batch job runs, capture summary statistics (record counts, control totals, hash checksums) for all input files.
2. **Post-condition checks**: After the job completes, verify a set of domain-specific invariants defined in `RECONCILIATION_CHECKS.md`.
3. **Automated assertions**: The test harness provides functions for:
   - `count_records(file)` -- number of records in a data file.
   - `sum_field(file, field)` -- numeric total of a specific field across all records.
   - `unique_keys(file, key_fields)` -- set of distinct key values.
   - `cross_reference(file_a, key_a, file_b, key_b)` -- referential integrity check.

### Key Invariants

| Check ID | After Job | Invariant |
|---|---|---|
| RECON-01 | POSTTRAN | `count(TRANSACT_after) == count(TRANSACT_before) + count(DALYTRAN) - count(DALYREJS)` |
| RECON-02 | POSTTRAN | `sum(TCATBALF.TRAN-CAT-BAL) == sum(TRANSACT.TRAN-AMT)` grouped by (ACCT, TYPE, CAT) |
| RECON-03 | POSTTRAN | Every `DALYTRAN.DALYTRAN-CARD-NUM` exists in `CARDXREF.XREF-CARD-NUM` or is in `DALYREJS` |
| RECON-04 | INTCALC | Every account with non-zero `TRAN-CAT-BAL` has a generated interest transaction |
| RECON-05 | INTCALC | Interest amount = `TRAN-CAT-BAL * DIS-INT-RATE / 12` (monthly) per category |
| RECON-06 | CREASTMT | One statement block generated per card in `CARDXREF` with transactions |
| RECON-07 | COMBTRAN | `count(COMBINED) == count(BACKUP) + count(SYSTRAN)` (no duplicates on TRAN-ID) |
| RECON-08 | TRANBKP | `count(BACKUP) == count(TRANSACT_before)` |
| RECON-09 | Data Load | Every `CARD.CARD-ACCT-ID` exists in `ACCOUNT.ACCT-ID` |
| RECON-10 | Data Load | Every `XREF.XREF-ACCT-ID` exists in `ACCOUNT.ACCT-ID` |

### Pass Criteria
- All invariants pass with zero tolerance for count checks.
- Numeric totals match within rounding tolerance of 0.01 (one cent).

---

## 4. Contract Testing

### Purpose
Ensure the migrated Java application exposes **API contracts** that are functionally equivalent to the COBOL program interfaces, including CICS COMMAREA layouts, batch file I/O formats, and inter-program call signatures.

### Approach

#### 4a. Data Contract Tests (File I/O)
Validate that the Java application reads and writes files with the same record layouts as defined by the COBOL copybooks.

| Contract | Copybook | Record Length | Key Fields |
|---|---|---|---|
| Account File | `CVACT01Y.cpy` | 300 | `ACCT-ID` (PIC 9(11)) |
| Card File | `CVACT02Y.cpy` | 150 | `CARD-NUM` (PIC X(16)) |
| Customer File | `CVCUS01Y.cpy` | 500 | `CUST-ID` (PIC 9(09)) |
| Card Cross-Ref | `CVACT03Y.cpy` | 50 | `XREF-CARD-NUM` (PIC X(16)) |
| Transaction Master | `CVTRA05Y.cpy` | 350 | `TRAN-ID` (PIC X(16)) |
| Daily Transaction | `CVTRA06Y.cpy` | 350 | `DALYTRAN-ID` (PIC X(16)) |
| Tran Category Bal | `CVTRA01Y.cpy` | 50 | `TRANCAT-ACCT-ID + TYPE-CD + CD` |
| Disclosure Group | `CVTRA02Y.cpy` | 50 | `DIS-ACCT-GROUP-ID + TYPE-CD + CAT-CD` |
| Transaction Type | `CVTRA03Y.cpy` | 60 | `TRAN-TYPE` (PIC X(02)) |
| Transaction Category | `CVTRA04Y.cpy` | 60 | `TRAN-TYPE-CD + TRAN-CAT-CD` |
| User Security | `CSUSR01Y.cpy` | 80 | `SEC-USR-ID` (PIC X(08)) |

#### 4b. Service Contract Tests (API Endpoints)
If the migrated system exposes REST APIs, validate:

| Legacy Transaction | Expected REST Endpoint | Method | Request Schema | Response Schema |
|---|---|---|---|---|
| CC00 (Signon) | `/api/auth/login` | POST | `{ userId, password }` | `{ token, userType }` |
| CM00 (Main Menu) | `/api/menu` | GET | -- | Menu item list |
| Account View | `/api/accounts/{id}` | GET | -- | Account DTO matching `CVACT01Y` fields |
| Card List | `/api/cards?accountId={id}` | GET | -- | Array of Card DTOs matching `CVACT02Y` |
| Transaction List | `/api/transactions?cardNum={num}` | GET | -- | Array of Transaction DTOs matching `CVTRA05Y` |
| Transaction Add | `/api/transactions` | POST | Transaction DTO | Created transaction |
| Bill Payment | `/api/payments` | POST | Payment DTO | Payment confirmation |

#### 4c. Batch Contract Tests
Validate that Java batch jobs accept the same JCL-equivalent parameters and produce output files with identical layouts:

| Legacy Job | Java Equivalent | Input Contract | Output Contract |
|---|---|---|---|
| POSTTRAN | `PostTransactionJob` | DALYTRAN (350-byte), XREFFILE, ACCTFILE, TCATBALF | Updated TRANSACT, TCATBALF, DALYREJS (430-byte) |
| INTCALC | `InterestCalcJob` | TCATBALF, XREFFILE, ACCTFILE, DISCGRP | SYSTRAN (350-byte) |
| CREASTMT | `CreateStatementJob` | TRANSACT, XREFFILE, ACCTFILE, CUSTFILE | Statement text + HTML |
| COMBTRAN | `CombineTransJob` | BACKUP, SYSTRAN | Combined sorted file (350-byte) |
| TRANBKP | `TransBackupJob` | TRANSACT | Backup sequential file (350-byte) |

### Pass Criteria
- All data contracts parse without error.
- API response schemas validate against generated OpenAPI spec.
- Batch output file record lengths and field positions match copybook definitions exactly.

---

## Test Execution Order

```
1. Golden-File Tests     (parse & compare static data)
2. Contract Tests        (validate schemas and layouts)
3. Differential Tests    (run scenarios through both systems)
4. Reconciliation Tests  (verify aggregate invariants)
```

## Tools and Artefacts

| Artefact | Location | Purpose |
|---|---|---|
| Golden reference files | `golden-files/*.json` | Canonical JSON from ASCII data |
| Test harness | `test-harness/` | Python utilities for parsing, comparison, reconciliation |
| Reconciliation specs | `RECONCILIATION_CHECKS.md` | Per-job validation rules |
| This document | `TEST_STRATEGY.md` | Overall test strategy |
