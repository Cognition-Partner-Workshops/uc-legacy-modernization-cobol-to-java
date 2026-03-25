# CardDemo Migration Test Harness

A Python-based test harness for validating the COBOL-to-Java migration of the
CardDemo application. It parses legacy fixed-width ASCII data files using COBOL
copybook layouts, produces structured JSON golden references, and provides
comparison and reconciliation utilities.

## Prerequisites

- Python 3.10+
- pytest 7.0+

## Setup

```bash
cd test-harness
pip install -r requirements.txt
```

## Usage

### Generate Golden Files

Parse all ASCII data files and produce JSON golden references:

```bash
python generate_golden_files.py
```

Options:
- `--data-dir PATH` - Path to ASCII data directory (default: `../app/data/ASCII`)
- `--output-dir PATH` - Path to output directory (default: `../golden-files`)

### Run Golden-File Tests

Validate that parsed data matches golden files:

```bash
pytest test_golden_files.py -v
```

### Run Reconciliation Tests

Validate cross-entity data integrity:

```bash
pytest test_reconciliation.py -v
```

### Run All Tests

```bash
pytest -v
```

## Components

### `copybook_parser.py`

Fixed-width record parser driven by COBOL copybook field definitions. Handles:
- `PIC 9(n)` - Unsigned integer fields
- `PIC S9(n)V99` - Signed decimal with overpunch encoding
- `PIC X(n)` - Alphanumeric string fields
- `FILLER` - Padding (skipped in output)

Pre-defined layouts for all CardDemo data files are registered in
`LAYOUT_REGISTRY`.

### `comparator.py`

Field-level comparison utilities:
- `RecordComparator` - Compares records with configurable tolerance
- `ComparisonConfig` - Tolerance, field ignoring, timestamp handling
- `format_diff_report()` - Human-readable diff reports

### `reconciliation.py`

Cross-entity validation checks (R-01 through R-10):
- Referential integrity (cards -> accounts, xrefs -> all entities)
- Transaction code validity (type codes, category codes)
- Record count preservation
- Account status consistency
- Customer-card relationship symmetry

### `generate_golden_files.py`

CLI script to regenerate golden files from source ASCII data.

## Directory Layout

```
test-harness/
  copybook_parser.py          # Core parser
  comparator.py               # Diff/comparison utilities
  reconciliation.py           # Cross-entity checks
  generate_golden_files.py    # Golden file generator
  conftest.py                 # Shared pytest fixtures
  test_golden_files.py        # Golden-file test suite
  test_reconciliation.py      # Reconciliation test suite
  requirements.txt            # Python dependencies
  README.md                   # This file
```

## Extending

### Adding a New Data File

1. Define the copybook field layout in `copybook_parser.py`
2. Register it in `LAYOUT_REGISTRY`
3. Run `generate_golden_files.py` to produce the golden JSON
4. Update expected counts in test files

### Adding a Reconciliation Check

1. Add a new method to `ReconciliationRunner` in `reconciliation.py`
2. Add it to `run_all()`
3. Add a corresponding test in `test_reconciliation.py`
4. Document the check in `RECONCILIATION_CHECKS.md`
