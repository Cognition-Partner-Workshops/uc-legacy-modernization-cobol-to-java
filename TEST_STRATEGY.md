# Migration Test Strategy

## Overview

This document defines the testing strategy for validating the CardDemo COBOL-to-Java modernization. The strategy employs four complementary testing dimensions to ensure behavioral equivalence between the legacy mainframe system and the migrated Java implementation.

## Testing Dimensions

### 1. Golden-File Testing

**Purpose:** Establish a known-correct baseline of outputs parsed directly from the legacy mainframe data files, then verify that the migrated Java programs produce byte-equivalent results.

**Approach:**
- Parse all ASCII flat files (`app/data/ASCII/`) using COBOL copybook layouts (`app/cpy/`)
- Produce structured JSON golden references in `golden-files/`
- After Java migration, run the same input through Java batch programs and compare output field-by-field against golden files

**Covered Data Files:**

| File | Copybook | Record Length | Records | Description |
|------|----------|--------------|---------|-------------|
| `acctdata.txt` | `CVACT01Y.cpy` | 300 | 50 | Account master |
| `carddata.txt` | `CVACT02Y.cpy` | 150 | 50 | Card records |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 | 50 | Card-to-customer-account cross-reference |
| `custdata.txt` | `CVCUS01Y.cpy` | 500 | 50 | Customer master |
| `dailytran.txt` | `CVTRA06Y.cpy` | 350 | 300 | Daily transactions |
| `discgrp.txt` | `CVTRA02Y.cpy` | 50 | 51 | Disclosure groups (interest rates) |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 | 50 | Transaction category balances |
| `trancatg.txt` | `CVTRA04Y.cpy` | 60 | 18 | Transaction category types |
| `trantype.txt` | `CVTRA03Y.cpy` | 60 | 7 | Transaction types |

**Tolerance Rules:**
- Numeric fields: ±0.01 absolute tolerance (accounts for COBOL zoned-decimal rounding)
- Alphanumeric fields: exact match after trailing whitespace trim
- Date fields: exact string match (format `YYYY-MM-DD`)

**Tool:** `test-harness/comparator.py :: compare_golden_file()`

---

### 2. Differential Testing

**Purpose:** Run identical input through both the COBOL programs (or emulated output) and the Java programs side-by-side, then compare outputs to detect behavioral divergence.

**Approach:**
- Prepare a canonical input dataset (the golden input files)
- Execute the COBOL batch jobs (via UniKix emulation or captured output)
- Execute the equivalent Java batch jobs against the same input
- Diff the outputs record-by-record using the comparator

**Key Batch Jobs to Test Differentially:**

| JCL Job | COBOL Program(s) | Function |
|---------|-------------------|----------|
| `POSTTRAN.jcl` | `CBTRN01C` | Post daily transactions, update account balances |
| `INTCALC.jcl` | Interest calc | Compute interest on category balances |
| `CREASTMT.JCL` | `CBSTM03A/B` | Generate account statements |
| `TRANREPT.jcl` | `CORPT00C` | Daily transaction report |
| `COMBTRAN.jcl` | Combine/sort | Merge daily transactions into master file |

**Failure Criteria:**
- Any field-level mismatch beyond numeric tolerance
- Record count differences between COBOL and Java output
- Missing or extra records in either output

**Tool:** `test-harness/comparator.py :: compare_differential()`

---

### 3. Reconciliation Testing

**Purpose:** Validate business-rule invariants that must hold after each batch job completes. These are data-integrity checks independent of implementation language.

**Approach:**
- Define per-job reconciliation checks (documented in `RECONCILIATION_CHECKS.md`)
- Run checks against both pre- and post-batch data states
- Verify referential integrity, balance equations, and constraint satisfaction

**Categories of Checks:**

| Category | Example |
|----------|---------|
| **Count preservation** | Account count unchanged after POSTTRAN |
| **Balance equations** | Net balance change = sum of posted transactions |
| **Referential integrity** | All XREF card numbers resolve to account master |
| **Constraint satisfaction** | No account balance exceeds credit limit |
| **No data loss** | No orphaned transactions (every card in XREF) |
| **Required fields** | Active accounts have non-blank open dates |

**Tool:** `test-harness/reconciliation.py`

---

### 4. Contract Testing

**Purpose:** Verify that the Java implementation honors the data contracts defined by COBOL copybooks — field names, data types, offsets, and record lengths.

**Approach:**
- Parse copybook definitions into machine-readable field specs
- Generate contract assertions from copybook metadata:
  - Field count per record type
  - Field ordering and offsets
  - PIC clause → Java type mapping validation
  - Record length invariants
- Validate Java DTOs/POJOs against copybook contracts

**Contract Validation Rules:**

| Copybook PIC | Expected Java Type | Validation |
|--------------|-------------------|------------|
| `PIC 9(n)` | `long` or `int` | Unsigned integer, no decimals |
| `PIC S9(n)V99` | `BigDecimal` | Signed with 2 decimal places |
| `PIC X(n)` | `String` | Fixed-length, left-aligned, space-padded |
| `PIC 9(n)V99` | `BigDecimal` | Unsigned with 2 decimal places |

**Contract Test Assertions:**
1. Java record class field count == copybook elementary item count (excluding FILLER)
2. Java record serialized length == copybook RECLN
3. Java field ordering matches copybook offset ordering
4. Numeric fields preserve precision (no floating-point truncation)

**Tool:** `test-harness/copybook_parser.py` (provides field specs for contract generation)

---

## Test Execution

### Quick Start

```bash
# Generate golden files and run reconciliation
cd test-harness
python3 run_tests.py --all

# Generate golden files only
python3 run_tests.py --generate

# Run reconciliation checks only
python3 run_tests.py --reconcile
```

### CI Integration

The test harness is designed to run in CI pipelines:
- Exit code 0 = all checks passed
- Exit code 1 = one or more checks failed
- JSON reports written to `golden-files/reconciliation_report.json`

### Adding New Tests

1. **New data file:** Add entry to `FILE_COPYBOOK_MAP` in `copybook_parser.py`
2. **New reconciliation check:** Add function to `reconciliation.py`
3. **New batch job:** Add reconciliation suite following the pattern in `reconciliation.py`

---

## Directory Structure

```
test-harness/
├── __init__.py              # Package marker
├── copybook_parser.py       # COBOL copybook → field definitions
├── record_parser.py         # Fixed-width ASCII → structured JSON
├── comparator.py            # Golden-file & differential comparison
├── reconciliation.py        # Business-rule reconciliation checks
└── run_tests.py             # CLI test runner

golden-files/
├── acctdata.json            # Account master golden reference
├── carddata.json            # Card records golden reference
├── cardxref.json            # Cross-reference golden reference
├── custdata.json            # Customer master golden reference
├── dailytran.json           # Daily transactions golden reference
├── discgrp.json             # Disclosure groups golden reference
├── tcatbal.json             # Transaction category balances golden reference
├── trancatg.json            # Transaction categories golden reference
├── trantype.json            # Transaction types golden reference
└── reconciliation_report.json  # Latest reconciliation results
```

---

## Success Criteria

The migration is considered valid when:

1. **Golden-file:** 100% field match (within tolerance) across all 626 records
2. **Differential:** Zero divergence between COBOL and Java output for all batch jobs
3. **Reconciliation:** All business-rule checks pass for every batch job
4. **Contract:** Java DTOs satisfy all copybook-derived type and length contracts
