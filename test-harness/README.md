# Migration Test Harness

Tooling for validating the CardDemo COBOL-to-Java migration. Parses mainframe ASCII data files using COBOL copybook layouts, generates JSON golden references, and runs reconciliation checks.

## Quick Start

```bash
cd test-harness

# Install dependencies
pip install -r requirements.txt

# Generate golden files from ASCII data
python golden_file_generator.py

# Run all tests
pytest -v
```

## Components

| File                      | Purpose                                                    |
| :------------------------ | :--------------------------------------------------------- |
| `layouts/definitions.py`  | COBOL copybook field definitions as Python data structures  |
| `copybook_parser.py`      | Fixed-width record parser with zoned decimal support        |
| `golden_file_generator.py`| Generates `golden-files/*.golden.json` from ASCII data      |
| `comparator.py`           | Field-level diff engine with numeric tolerance              |
| `reconciliation.py`       | Cross-file referential integrity and business rule checks   |
| `test_golden_files.py`    | pytest suite for golden-file validation                     |
| `test_reconciliation.py`  | pytest suite for reconciliation checks                      |

## Golden File Format

Each `.golden.json` file contains:

```json
{
  "metadata": {
    "source_file": "acctdata.txt",
    "copybook": "CVACT01Y.cpy",
    "record_name": "ACCOUNT-RECORD",
    "record_length": 300,
    "record_count": 50
  },
  "records": [
    {
      "ACCT-ID": 1,
      "ACCT-ACTIVE-STATUS": "Y",
      "ACCT-CURR-BAL": "19400.00",
      ...
    }
  ]
}
```

## Comparison Usage

```python
from comparator import compare_datasets, ComparisonConfig

config = ComparisonConfig(
    numeric_tolerance=0.005,
    ignore_filler=True,
    timestamp_date_only=True,
)
result = compare_datasets(golden_records, migrated_records, config)
print(result.summary)
```
