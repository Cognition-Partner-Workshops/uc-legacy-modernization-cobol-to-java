# Test Harness

Python-based utilities for validating the CardDemo COBOL-to-Java migration.

## Prerequisites

- Python 3.10+
- No external dependencies required (stdlib only)

## Components

| Module | Purpose |
|--------|---------|
| `copybook_parser.py` | Parse COBOL PIC clauses into field definitions |
| `record_parser.py` | Split fixed-width ASCII lines into named field dicts |
| `generate_golden_files.py` | Produce JSON golden references from ASCII data |
| `comparator.py` | Field-level diff between expected and actual record sets |
| `reconciliation.py` | Aggregate consistency checks across data files |
| `contracts.py` | Validate output files against copybook-derived schemas |

## Quick Start

```bash
# From the repository root:

# 1. Generate golden reference files
python test-harness/generate_golden_files.py

# 2. Run reconciliation checks against golden data
python test-harness/reconciliation.py

# 3. Run contract validation against ASCII data files
python test-harness/contracts.py
```

## Using the Comparator

```python
from test_harness.comparator import compare_records

result = compare_records(
    expected=golden_records,
    actual=java_output_records,
    source="acctdata",
    ignore_fields={"FILLER"},
    tolerance=0.01,
)
print(result.summary())
assert result.passed
```

## Extending

To add a new reconciliation check, define a function in `reconciliation.py`
that returns a `CheckResult`, then register it in `run_all()`.
