# Migration Test Strategy

## Overview

This document defines the four-dimensional testing strategy for validating the CardDemo COBOL-to-Java migration. Each dimension targets a specific aspect of correctness to ensure functional equivalence between the legacy mainframe system and the modernized Java implementation.

---

## 1. Golden-File Testing

### Purpose
Validate that the migrated Java system produces byte-for-byte identical output when processing known input data. Golden files serve as the immutable "source of truth" derived from the original COBOL system's behavior.

### Approach
- Parse each ASCII flat file (`acctdata.txt`, `carddata.txt`, `custdata.txt`, `dailytran.txt`, `cardxref.txt`, `trantype.txt`, `trancatg.txt`, `discgrp.txt`, `tcatbal.txt`) using the corresponding COBOL copybook layout
- Produce structured JSON representations capturing every field with correct data types
- Store these as golden references in `golden-files/`
- After migration, run the Java system against the same input and compare output JSON against golden files

### Key Validations
| Check | Description |
|-------|-------------|
| Field alignment | Each field starts and ends at the correct byte offset |
| Numeric precision | Signed decimals (`S9(n)V99`) are parsed with correct sign and scale |
| Packed decimal | `COMP-3` fields decoded correctly (half-byte BCD) |
| Binary fields | `COMP` fields decoded as big-endian binary integers |
| Filler handling | Filler bytes are preserved but excluded from semantic comparison |
| Record length | Each record matches the copybook-defined RECLN |

### Tooling
- `test-harness/parsers/copybook_parser.py` — Parses fixed-width records using layout definitions
- `test-harness/comparators/golden_comparator.py` — Deep-compares JSON output against golden references

---

## 2. Differential Testing

### Purpose
Detect behavioral divergence between the COBOL and Java implementations by running both systems against identical inputs and comparing their outputs field-by-field.

### Approach
- Execute COBOL batch jobs (via JCL) and the equivalent Java batch processes against a shared test dataset
- Capture outputs from both systems in normalized JSON format
- Perform structural diff to identify any field-level discrepancies
- Categorize differences as: **semantic** (logic error), **cosmetic** (formatting), or **precision** (rounding)

### Key Validations
| Check | Description |
|-------|-------------|
| Output equivalence | Java output matches COBOL output for all non-filler fields |
| Sort order | Records appear in identical sequence (VSAM key ordering) |
| Edge cases | Boundary values (max PIC 9(11), negative balances, zero amounts) |
| Date handling | Date fields (`YYYY-MM-DD`) parsed and formatted identically |
| Sign encoding | Zoned decimal sign overpunch (`{` = +0, `}` = -0, etc.) decoded correctly |

### Tooling
- `test-harness/comparators/differential_comparator.py` — Compares COBOL vs Java outputs with configurable tolerance
- `test-harness/reporters/diff_report.py` — Generates human-readable diff reports

---

## 3. Reconciliation Testing

### Purpose
Verify data integrity invariants that must hold after batch processing. These are business-rule checks that validate cross-file consistency and aggregate correctness.

### Approach
- After each batch job executes, run reconciliation checks against the output files
- Validate referential integrity across linked datasets (accounts ↔ cards ↔ customers ↔ cross-references)
- Verify aggregate calculations (e.g., category balances sum to account totals)
- Check record counts match expected values

### Key Validations
| Job | Check | Description |
|-----|-------|-------------|
| POSTTRAN | Balance update | Account current balance = previous + credits - debits |
| POSTTRAN | Category rollup | Sum of `tcatbal` records per account = account current cycle totals |
| INTCALC | Interest calc | Interest amount = balance × (rate / 36500) × days |
| TRANBKP | Record count | Backup file record count matches source transaction count |
| CBEXPORT | Completeness | Export file contains all accounts in the input set |
| DALYREJS | Rejection tracking | Rejected transactions + processed = total input transactions |

### Tooling
- `test-harness/reconciliation/reconciliation_runner.py` — Orchestrates per-job reconciliation checks
- `test-harness/reconciliation/checks/` — Individual check modules per batch job

---

## 4. Contract Testing

### Purpose
Ensure the migrated Java system honors the implicit and explicit contracts defined by the COBOL copybook structures, CICS transaction interfaces, and file I/O specifications.

### Approach
- Define contracts from copybook field definitions (types, lengths, valid ranges)
- Validate that Java DTOs/entities maintain identical field semantics
- Test CICS transaction equivalents (REST API endpoints) against expected request/response schemas
- Verify file format contracts (record lengths, field positions, encoding)

### Key Validations
| Contract | Source | Validation |
|----------|--------|------------|
| Account record | CVACT01Y.cpy | 300-byte fixed record, 11 defined fields |
| Card record | CVACT02Y.cpy | 150-byte fixed record, 6 defined fields |
| Customer record | CVCUS01Y.cpy | 500-byte fixed record, 14 defined fields |
| Transaction record | CVTRA05Y.cpy | 350-byte fixed record, 13 defined fields |
| Cross-reference | CVACT03Y.cpy | 50-byte fixed record, 3 defined fields |
| Transaction type | CVTRA03Y.cpy | 60-byte fixed record, 2 defined fields |
| Transaction category | CVTRA04Y.cpy | 60-byte fixed record, 3 defined fields |
| Category balance | CVTRA01Y.cpy | 50-byte fixed record, 4 defined fields |
| Disclosure group | CVTRA02Y.cpy | 50-byte fixed record, 3 defined fields |

### Tooling
- `test-harness/contracts/contract_validator.py` — Validates records against copybook-derived schemas
- `test-harness/contracts/schemas/` — JSON Schema definitions for each record type

---

## Test Execution Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     Migration Test Pipeline                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. PARSE        Parse ASCII data → JSON golden files            │
│                  (copybook_parser.py)                             │
│                                                                   │
│  2. VALIDATE     Contract checks on parsed data                  │
│                  (contract_validator.py)                          │
│                                                                   │
│  3. EXECUTE      Run Java batch jobs against test data           │
│                                                                   │
│  4. COMPARE      Golden-file comparison                          │
│                  (golden_comparator.py)                           │
│                                                                   │
│  5. DIFF         Differential analysis (COBOL vs Java)           │
│                  (differential_comparator.py)                     │
│                                                                   │
│  6. RECONCILE    Post-execution integrity checks                 │
│                  (reconciliation_runner.py)                       │
│                                                                   │
│  7. REPORT       Aggregate results and generate summary          │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Directory Structure

```
├── TEST_STRATEGY.md              ← This document
├── RECONCILIATION_CHECKS.md      ← Per-job validation specs
├── golden-files/                 ← JSON golden reference files
│   ├── acctdata.golden.json
│   ├── carddata.golden.json
│   ├── custdata.golden.json
│   ├── dailytran.golden.json
│   ├── cardxref.golden.json
│   ├── trantype.golden.json
│   ├── trancatg.golden.json
│   ├── discgrp.golden.json
│   └── tcatbal.golden.json
└── test-harness/
    ├── parsers/
    │   ├── copybook_parser.py
    │   └── layouts.py
    ├── comparators/
    │   ├── golden_comparator.py
    │   └── differential_comparator.py
    ├── reconciliation/
    │   ├── reconciliation_runner.py
    │   └── checks/
    ├── contracts/
    │   ├── contract_validator.py
    │   └── schemas/
    ├── reporters/
    │   └── diff_report.py
    └── requirements.txt
```

---

## Success Criteria

A migration is considered **validated** when:

1. **Golden-file**: 100% field-level match between parsed COBOL data and Java output (excluding filler)
2. **Differential**: Zero semantic differences; cosmetic differences documented and approved
3. **Reconciliation**: All per-job integrity checks pass with zero tolerance
4. **Contract**: All records conform to copybook-defined schemas with correct types and lengths
