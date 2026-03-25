# CardDemo Migration Test Harness

Utilities for validating COBOL-to-Java migration correctness using golden-file testing, field-level comparison, and reconciliation checks.

## Directory Structure

```
test-harness/
├── layouts.py              # Copybook layout definitions (COBOL PIC → Python)
├── parser.py               # Fixed-width file parser (ASCII data → JSON)
├── comparator.py           # Field-level comparison engine
├── reconciliation.py       # Aggregate reconciliation checks
├── run_golden_tests.py     # Golden-file test runner
├── run_reconciliation.py   # Reconciliation check runner
└── README.md               # This file

golden-files/
├── acctdata.json           # 50 account records (CVACT01Y layout)
├── carddata.json           # 50 card records (CVACT02Y layout)
├── cardxref.json           # 50 card cross-references (CVACT03Y layout)
├── custdata.json           # 50 customer records (CVCUS01Y layout)
├── dailytran.json          # 300 daily transactions (CVTRA06Y layout)
├── discgrp.json            # 51 disclosure group records (CVTRA02Y layout)
├── tcatbal.json            # 50 category balances (CVTRA01Y layout)
├── trancatg.json           # 18 transaction categories (CVTRA04Y layout)
└── trantype.json           # 7 transaction types (CVTRA03Y layout)
```

## Quick Start

### 1. Parse an ASCII data file to JSON

```bash
cd test-harness
python parser.py ../app/data/ASCII/acctdata.txt acctdata ../golden-files/acctdata.json
```

### 2. Parse all data files (regenerate golden files)

```bash
cd test-harness
for key in acctdata carddata cardxref custdata dailytran discgrp tcatbal trancatg trantype; do
    python parser.py "../app/data/ASCII/${key}.txt" "$key" "../golden-files/${key}.json"
done
```

### 3. Run reconciliation checks (baseline mode)

```bash
cd test-harness
python run_reconciliation.py ../golden-files/
```

This runs internal consistency checks (referential integrity, cross-reference completeness) against the golden files alone.

### 4. Compare golden files against modern system output

```bash
cd test-harness
python run_golden_tests.py ../golden-files/ /path/to/modern-output/
```

The modern output directory should contain JSON files with the same names as the golden files, with records in the same structure.

### 5. Run reconciliation with modern system counts

```bash
cd test-harness
python run_reconciliation.py ../golden-files/ \
    --modern-counts '{"RC-001": 50, "RC-003": 50, "RC-005": 50, "RC-006": 300}'
```

## Components

### parser.py

Parses COBOL fixed-width ASCII data files using copybook layouts from `layouts.py`. Handles:
- COBOL signed decimal overpunch encoding ({/A-I = positive, }/J-R = negative)
- Implied decimal point (PIC S9(n)V99)
- Fixed-width field extraction with FILLER handling
- Produces structured JSON with metadata and records

### comparator.py

Field-level comparison engine with configurable tolerance rules:
- **exact_string**: Trimmed string comparison
- **exact_numeric**: Integer comparison
- **decimal_exact**: Exact decimal comparison (for financial fields)
- **decimal_2dp**: Tolerance-based comparison (half-cent)
- **date/timestamp**: Date-aware comparison

### reconciliation.py

Aggregate data integrity checks:
- RC-001 through RC-009: Record counts, balance sums, referential integrity
- Can run in baseline mode (golden files only) or comparison mode (vs modern system)

## Requirements

- Python 3.8+
- No external dependencies (stdlib only)
