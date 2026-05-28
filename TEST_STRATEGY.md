# Migration Test Strategy

## Overview

This document defines the testing strategy for validating the migration of the CardDemo mainframe COBOL application to Java. The strategy covers four complementary testing dimensions that together ensure functional equivalence, data integrity, and interface compatibility between the legacy system and the modernized target.

## System Under Test

| Component | Legacy (Source) | Target |
|-----------|----------------|--------|
| Runtime | CICS / Batch (JCL) | Java / Spring Boot |
| Data Layer | VSAM KSDS, Sequential PS | RDBMS / File-based |
| Encoding | EBCDIC + Packed Decimal | UTF-8 / Native Java types |
| Programs | COBOL (30+ modules) | Java services |
| Batch Jobs | POSTTRAN, INTCALC, TRANREPT, CBEXPORT, etc. | Java batch processors |

---

## Dimension 1: Golden-File Testing

### Purpose
Capture the exact output of the legacy system for known inputs and use these as immutable reference points. The migrated Java code must produce byte-for-byte equivalent logical output.

### Approach

1. **Parse ASCII data files** using COBOL copybook layouts to produce structured JSON representations.
2. **Store golden references** in `golden-files/` with one JSON file per data entity.
3. **Compare migrated output** against golden files using field-level comparison with tolerance rules for numeric precision.

### Data Files Covered

| File | Copybook | Record Length | Entity |
|------|----------|--------------|--------|
| `acctdata.txt` | `CVACT01Y.cpy` | 300 | Account records |
| `carddata.txt` | `CVACT02Y.cpy` | 150 | Card records |
| `custdata.txt` | `CVCUS01Y.cpy` | 500 | Customer records |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 | Card cross-reference |
| `dailytran.txt` | `CVTRA05Y.cpy` | 350 | Daily transactions |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 | Transaction category balances |
| `discgrp.txt` | `CVTRA02Y.cpy` | 50 | Disclosure groups |
| `trantype.txt` | `CVTRA03Y.cpy` | 60 | Transaction types |
| `trancatg.txt` | `CVTRA04Y.cpy` | 60 | Transaction categories |

### Tolerance Rules

| Field Type | Tolerance | Rationale |
|-----------|-----------|-----------|
| PIC 9 / PIC X | Exact match | Character/numeric identity |
| PIC S9(n)V99 (signed overpunch) | ±0.01 | Zoned-decimal rounding |
| Timestamps | ±1 second | Clock skew in batch |
| FILLER fields | Ignored | Padding only |

### Verification Command
```bash
python test-harness/compare_golden.py \
  --golden golden-files/acctdata.json \
  --actual output/acctdata.json
```

---

## Dimension 2: Differential Testing

### Purpose
Run the same logical operation on both legacy and migrated systems side-by-side, then compare outputs field-by-field to detect behavioral divergence.

### Approach

1. **Input Preparation**: Use identical input datasets for both systems.
2. **Parallel Execution**: Run COBOL batch job and equivalent Java processor against the same input.
3. **Output Diff**: Compare output records using structural diff (not text diff) to isolate semantic differences from formatting differences.

### Key Differentials

| Batch Job | COBOL Program | Java Equivalent | Comparison Points |
|-----------|--------------|-----------------|-------------------|
| POSTTRAN | CBTRN02C | PostTransactionProcessor | Updated account balances, category balances, reject records |
| INTCALC | CBACT04C | InterestCalculator | Computed interest amounts, updated balances |
| TRANREPT | CBTRN03C | TransactionReportGenerator | Report line items, page/account totals |
| CBEXPORT | CBEXPORT | DataExportService | Multi-record export file contents |

### Differential Report Format
```json
{
  "job": "POSTTRAN",
  "records_compared": 300,
  "matches": 298,
  "differences": [
    {
      "record_key": "00000000025",
      "field": "ACCT-CURR-BAL",
      "legacy_value": "1234.56",
      "migrated_value": "1234.57",
      "within_tolerance": true
    }
  ]
}
```

---

## Dimension 3: Reconciliation Testing

### Purpose
Validate that batch processing maintains data integrity invariants. After each job runs, verify that cross-file relationships, aggregates, and business rules are preserved.

### Approach

1. **Pre-condition checks**: Validate input data integrity before job execution.
2. **Post-condition checks**: Verify output data satisfies business invariants.
3. **Cross-file reconciliation**: Ensure referential integrity across related datasets.

### Invariants

| Check ID | Description | Files Involved |
|----------|-------------|---------------|
| RC-001 | Every card in cardxref must reference a valid account in acctdata | cardxref, acctdata |
| RC-002 | Every card in cardxref must reference a valid customer in custdata | cardxref, custdata |
| RC-003 | Sum of transaction category balances per account must equal account current balance | tcatbal, acctdata |
| RC-004 | Every daily transaction must reference a valid card in cardxref | dailytran, cardxref |
| RC-005 | Transaction type codes in dailytran must exist in trantype | dailytran, trantype |
| RC-006 | Transaction category codes in dailytran must exist in trancatg | dailytran, trancatg |
| RC-007 | Disclosure group references must map to valid account groups | discgrp, acctdata |
| RC-008 | Record counts in export file must equal sum of source records | export, all sources |
| RC-009 | Account balance after POSTTRAN = prior balance + debits - credits | acctdata (pre/post) |
| RC-010 | No orphan cards (cards without a cross-reference entry) | carddata, cardxref |

### Verification Command
```bash
python test-harness/reconciliation.py \
  --data-dir app/data/ASCII/ \
  --checks all
```

---

## Dimension 4: Contract Testing

### Purpose
Validate that the migrated Java system's interfaces (API contracts, file formats, message schemas) are compatible with upstream and downstream consumers.

### Approach

1. **Record Layout Contracts**: Verify that Java DTOs exactly match COBOL copybook field definitions (name, type, offset, length).
2. **File Format Contracts**: Validate that output files conform to the expected fixed-width format (RECFM=F, correct LRECL).
3. **CICS Transaction Contracts**: Verify that online transaction request/response structures match BMS map definitions.
4. **Batch Interface Contracts**: Validate JCL DD-name to file mappings and record formats.

### Contract Specifications

| Contract | Source of Truth | Validation Method |
|----------|----------------|-------------------|
| Account Record | `CVACT01Y.cpy` (RECLN 300) | Schema comparison |
| Card Record | `CVACT02Y.cpy` (RECLN 150) | Schema comparison |
| Customer Record | `CVCUS01Y.cpy` (RECLN 500) | Schema comparison |
| Transaction Record | `CVTRA05Y.cpy` (RECLN 350) | Schema comparison |
| Card XREF Record | `CVACT03Y.cpy` (RECLN 50) | Schema comparison |
| Export Record | `CVEXPORT.cpy` (RECLN 500) | Schema + REDEFINES |
| POSTTRAN Inputs | `POSTTRAN.jcl` DD statements | Interface mapping |
| INTCALC Inputs | `INTCALC.jcl` DD statements | Interface mapping |

### Contract Test Example
```python
def test_account_record_contract():
    schema = parse_copybook("app/cpy/CVACT01Y.cpy")
    assert schema.total_length == 300
    assert schema.fields["ACCT-ID"].offset == 0
    assert schema.fields["ACCT-ID"].length == 11
    assert schema.fields["ACCT-ID"].pic == "9(11)"
    assert schema.fields["ACCT-ACTIVE-STATUS"].offset == 11
    assert schema.fields["ACCT-ACTIVE-STATUS"].length == 1
```

---

## Test Execution Pipeline

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│  1. Golden-File  │────▶│  2. Differential │────▶│  3. Reconciliation │
│    Baseline      │     │    Comparison    │     │     Validation     │
└─────────────────┘     └──────────────────┘     └────────────────────┘
                                                           │
                                                           ▼
                                                  ┌────────────────────┐
                                                  │  4. Contract Tests │
                                                  │    (Continuous)    │
                                                  └────────────────────┘
```

### CI Integration

| Stage | Trigger | Failure Action |
|-------|---------|----------------|
| Golden-file | Every PR | Block merge |
| Differential | Nightly / on-demand | Alert + report |
| Reconciliation | Post-batch-run | Block promotion |
| Contract | Every PR | Block merge |

---

## Directory Structure

```
├── TEST_STRATEGY.md          # This document
├── RECONCILIATION_CHECKS.md  # Per-job validation specs
├── golden-files/             # JSON golden references
│   ├── acctdata.json
│   ├── carddata.json
│   ├── custdata.json
│   ├── cardxref.json
│   ├── dailytran.json
│   ├── tcatbal.json
│   ├── discgrp.json
│   ├── trantype.json
│   └── trancatg.json
└── test-harness/             # Test utilities
    ├── __init__.py
    ├── copybook_parser.py    # COBOL copybook → field definitions
    ├── record_parser.py      # Fixed-width ASCII → structured dict
    ├── compare_golden.py     # Golden-file comparison engine
    ├── reconciliation.py     # Cross-file integrity checks
    ├── contract_validator.py # Schema contract verification
    └── utils.py              # Shared utilities (signed decimal, etc.)
```
