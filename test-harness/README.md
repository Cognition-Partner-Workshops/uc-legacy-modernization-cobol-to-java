# CardDemo Migration Test Harness

Python-based utilities for validating the COBOL-to-Java migration of the CardDemo application.

## Structure

| File | Purpose |
|---|---|
| `copybook_parser.py` | Fixed-width record parser with COBOL sign-overpunch decoding |
| `copybook_layouts.py` | All copybook layout definitions (field name, type, width, decimals) |
| `comparator.py` | Field-level comparison engine for golden-file and differential testing |
| `reconciliation.py` | Aggregate-level reconciliation check framework |
| `generate_golden_files.py` | Script to regenerate `golden-files/*.json` from ASCII data |
| `run_data_load_checks.py` | Run baseline data-load reconciliation checks |

## Quick Start

### Generate golden files

```bash
cd test-harness
python generate_golden_files.py
```

### Run data-load reconciliation checks

```bash
cd test-harness
python run_data_load_checks.py
```

### Use in your own tests

```python
from test_harness.copybook_parser import parse_file
from test_harness.copybook_layouts import ACCOUNT_LAYOUT
from test_harness.comparator import compare_records

# Parse the legacy ASCII file
legacy_records = parse_file("app/data/ASCII/acctdata.txt", ACCOUNT_LAYOUT)

# Parse the Java output (same format or convert to same dict structure)
java_records = [...]  # your Java output parsed to list of dicts

# Compare
result = compare_records(legacy_records, java_records, key_fields=["ACCT-ID"])
print(result.to_json())
```

## Requirements

- Python 3.8+
- No external dependencies (stdlib only)
