# Migration Test Strategy: COBOL-to-Java (CardDemo)

This document defines the four-dimensional testing strategy used to validate the
migration of the AWS CardDemo mainframe application from COBOL/CICS/VSAM to a
modern Java stack.

---

## 1. Golden-File Testing

### Purpose

Capture the exact output of the legacy COBOL system for every data file and use
those outputs as immutable reference baselines. The migrated Java application must
reproduce identical structured data when given the same inputs.

### Scope

| Data File        | Copybook Layout | Record Length | Key Field(s)                    |
|------------------|-----------------|---------------|---------------------------------|
| `acctdata.txt`   | `CVACT01Y`      | 300           | `ACCT-ID`                       |
| `carddata.txt`   | `CVACT02Y`      | 150           | `CARD-NUM`                      |
| `cardxref.txt`   | `CVACT03Y`      | 50            | `XREF-CARD-NUM`                 |
| `custdata.txt`   | `CVCUS01Y`      | 500           | `CUST-ID`                       |
| `dailytran.txt`  | `CVTRA06Y`      | 350           | `DALYTRAN-ID`                   |
| `trantype.txt`   | `CVTRA03Y`      | 60            | `TRAN-TYPE`                     |
| `trancatg.txt`   | `CVTRA04Y`      | 60            | `TRAN-TYPE-CD + TRAN-CAT-CD`   |
| `discgrp.txt`    | `CVTRA02Y`      | 50            | `DIS-ACCT-GROUP-ID + TYPE + CAT`|
| `tcatbal.txt`    | `CVTRA01Y`      | 50            | `TRANCAT-ACCT-ID + TYPE + CAT`  |

### Method

1. Parse each ASCII fixed-width data file using its COBOL copybook layout.
2. Produce a structured JSON file in `golden-files/` with one JSON object per
   record, preserving field names, types, and trimmed values.
3. The golden JSON files are committed to the repository and treated as
   immutable baselines.
4. During CI, the Java application's data-loading or export routines are run
   against the same input files and their output is compared field-by-field
   against the golden references.

### Pass Criteria

- Every field in every record must match exactly (after trimming trailing spaces
  for alphanumeric fields).
- Numeric fields must match to the exact decimal precision defined in the
  copybook PIC clause.
- Zero mismatches across all files.

---

## 2. Differential Testing

### Purpose

Run the same business operation on both the legacy COBOL system and the migrated
Java system, then compare outputs to detect behavioral divergence.

### Scope

Differential testing covers the core batch processing pipeline:

| Operation              | Legacy Program | Java Equivalent              | Input Files                              |
|------------------------|----------------|------------------------------|------------------------------------------|
| Transaction Posting    | `CBTRN02C`     | Transaction Posting Service  | `dailytran.txt`, `cardxref.txt`, `acctdata.txt`, `tcatbal.txt` |
| Interest Calculation   | `CBACT04C`     | Interest Calculation Service | `tcatbal.txt`, `discgrp.txt`, `acctdata.txt`, `cardxref.txt`   |
| Statement Generation   | `CBSTM03A/B`   | Statement Generation Service | `transact`, `cardxref.txt`, `acctdata.txt`, `custdata.txt`     |
| Transaction Report     | `CBTRN03C`     | Transaction Report Service   | `transact`, `trantype.txt`, `trancatg.txt`                     |

### Method

1. Prepare identical input datasets for both systems.
2. Execute the batch operation on the legacy COBOL system (or use captured
   golden outputs if the mainframe is unavailable).
3. Execute the equivalent Java batch job with the same inputs.
4. Use `test-harness/comparators/differential_comparator.py` to diff the two
   result sets field-by-field.
5. Generate a differential report showing matches, mismatches, and missing
   records.

### Pass Criteria

- Zero field-level mismatches for all deterministic fields.
- Timestamp fields are excluded from exact comparison (compared within a
  configurable tolerance window).
- System-generated IDs may differ; relational integrity is validated instead.

---

## 3. Reconciliation Testing

### Purpose

Validate that batch job execution preserves data integrity invariants. These are
business rules that must hold true before and after every batch cycle, regardless
of whether the system is COBOL or Java.

### Scope

Reconciliation checks are defined per batch job. See `RECONCILIATION_CHECKS.md`
for the full specification. Summary:

| Job              | Key Invariants                                                       |
|------------------|----------------------------------------------------------------------|
| POSTTRAN         | Sum of posted amounts = sum of daily transactions minus rejects; account balances updated correctly; category balances incremented |
| INTCALC          | Interest computed matches rate x balance; interest transactions written to SYSTRAN; account balances updated with interest |
| TRANBKP          | Record count in backup = record count in transaction master pre-backup; no data loss |
| COMBTRAN         | Combined file count = backup count + SYSTRAN count; sort order preserved; no duplicates |
| CREASTMT         | One statement per card in XREF; statement totals match transaction sums per card; all cards covered |
| Data Refresh     | Record counts match source file counts; key uniqueness preserved; no orphan references |

### Method

1. Capture pre-state snapshots (record counts, balance sums, key sets).
2. Run the batch job (Java equivalent).
3. Capture post-state snapshots.
4. Apply reconciliation formulas defined in `RECONCILIATION_CHECKS.md`.
5. Report pass/fail per check with detailed variance information.

### Pass Criteria

- All reconciliation formulas must evaluate to zero variance.
- Key integrity checks (uniqueness, referential) must pass with zero violations.

---

## 4. Contract Testing

### Purpose

Ensure the migrated Java system honors the data contracts defined by the COBOL
copybook layouts. This validates that the Java DTOs, database entities, and
API payloads conform to the exact field structure, types, sizes, and constraints
of the original COBOL record definitions.

### Scope

| Contract                | Source Copybook | Validates                                      |
|-------------------------|-----------------|-------------------------------------------------|
| Account Record          | `CVACT01Y`      | 11-digit account ID, signed decimals, date formats, 300-byte total |
| Card Record             | `CVACT02Y`      | 16-char card number, 3-digit CVV, 150-byte total |
| Card Cross-Reference    | `CVACT03Y`      | 16-char card to 9-digit customer + 11-digit account mapping, 50-byte total |
| Customer Record         | `CVCUS01Y`      | 9-digit customer ID, name fields, SSN, FICO score, 500-byte total |
| Transaction Record      | `CVTRA05Y`      | 16-char transaction ID, signed amount, timestamps, 350-byte total |
| Daily Transaction       | `CVTRA06Y`      | Same structure as transaction record for daily batch input |
| Transaction Type        | `CVTRA03Y`      | 2-char type code + 50-char description, 60-byte total |
| Transaction Category    | `CVTRA04Y`      | 2-char type + 4-digit category + 50-char description, 60-byte total |
| Category Balance        | `CVTRA01Y`      | 11-digit account + type + category composite key, signed balance, 50-byte total |
| Disclosure Group        | `CVTRA02Y`      | 10-char group ID + type + category key, signed interest rate, 50-byte total |
| User Security           | `CSUSR01Y`      | 8-char user ID, first/last name, password, user type, 80-byte total |

### Method

1. Parse each copybook to extract field definitions (name, PIC clause, offset,
   length, type).
2. For each Java DTO or entity class, verify:
   - Field count matches copybook field count (excluding FILLER).
   - Field types are compatible (PIC 9 -> int/long, PIC X -> String,
     PIC S9V99 -> BigDecimal).
   - Field size constraints are preserved (e.g., `@Size(max=16)` for
     `CARD-NUM PIC X(16)`).
   - Total serialized record length matches the copybook RECLN.
3. For API endpoints, verify request/response schemas include all non-FILLER
   fields from the corresponding copybook.

### Pass Criteria

- Zero missing fields (every non-FILLER copybook field has a Java counterpart).
- Zero type mismatches.
- Zero size constraint violations.
- Serialized record round-trip produces the same byte length as the copybook RECLN.

---

## Test Execution Order

```
1. Contract Tests      (validate structure before data)
2. Golden-File Tests   (validate static data parsing)
3. Differential Tests  (validate batch processing equivalence)
4. Reconciliation Tests (validate business invariants)
```

## Directory Structure

```
TEST_STRATEGY.md              <- This document
RECONCILIATION_CHECKS.md      <- Per-job reconciliation specifications
golden-files/                 <- JSON golden reference files
  acctdata.json
  carddata.json
  cardxref.json
  custdata.json
  dailytran.json
  trantype.json
  trancatg.json
  discgrp.json
  tcatbal.json
test-harness/
  copybook_parser.py          <- Parse COBOL copybooks into field definitions
  ascii_parser.py             <- Parse fixed-width ASCII files using copybook layouts
  generate_golden_files.py    <- Generate golden JSON from ASCII data files
  comparators/
    golden_file_comparator.py <- Compare Java output against golden JSON
    differential_comparator.py <- Compare legacy vs modern batch outputs
  reconciliation/
    reconciliation_runner.py  <- Execute reconciliation checks
    checks.py                 <- Reconciliation check definitions
  contracts/
    contract_validator.py     <- Validate Java DTOs against copybook contracts
```

## Tooling

- **Language**: Python 3.10+ (test harness), Java 17+ (application under test)
- **Dependencies**: Python standard library only (json, re, os, sys, decimal)
- **CI Integration**: All test dimensions are designed to run as CI pipeline
  stages with JSON report output for automated pass/fail gating.
