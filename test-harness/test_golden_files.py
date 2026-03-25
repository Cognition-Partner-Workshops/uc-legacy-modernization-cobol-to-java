"""
Golden-file test suite.

Validates that:
1. All ASCII data files can be parsed using copybook layouts.
2. Parsed records match the golden-file JSON references.
3. Record counts, field values, and data types are correct.
"""

import json
from pathlib import Path

import pytest

from copybook_parser import LAYOUT_REGISTRY, parse_file
from comparator import ComparisonConfig, RecordComparator

REPO_ROOT = Path(__file__).resolve().parent.parent
GOLDEN_DIR = REPO_ROOT / "golden-files"
DATA_DIR = REPO_ROOT / "app" / "data" / "ASCII"

# Expected record counts from the legacy data files
EXPECTED_COUNTS = {
    "acctdata": 50,
    "carddata": 50,
    "custdata": 50,
    "cardxref": 50,
    "dailytran": 300,
    "trantype": 7,
    "trancatg": 18,
    "tcatbal": 50,
    "discgrp": 51,
}


@pytest.fixture
def comparator():
    """Create a comparator with default config."""
    config = ComparisonConfig(
        numeric_tolerance=0.01,
        ignore_fields=["_line_number"],
        strip_strings=True,
    )
    return RecordComparator(config)


def _load_golden(name: str) -> dict:
    """Load a golden file."""
    path = GOLDEN_DIR / f"{name}.json"
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


class TestGoldenFileParsing:
    """Test that ASCII data files parse correctly."""

    @pytest.mark.parametrize("name", list(LAYOUT_REGISTRY.keys()))
    def test_parse_produces_records(self, name: str):
        """Each data file should produce at least one record."""
        src = DATA_DIR / f"{name}.txt"
        if not src.exists():
            pytest.skip(f"Source file not found: {src}")

        fields = LAYOUT_REGISTRY[name]
        records = parse_file(str(src), fields)
        assert len(records) > 0, f"No records parsed from {name}.txt"

    @pytest.mark.parametrize("name", list(LAYOUT_REGISTRY.keys()))
    def test_record_count_matches_expected(self, name: str):
        """Record count should match known expected count."""
        src = DATA_DIR / f"{name}.txt"
        if not src.exists():
            pytest.skip(f"Source file not found: {src}")

        fields = LAYOUT_REGISTRY[name]
        records = parse_file(str(src), fields)
        expected = EXPECTED_COUNTS.get(name)
        if expected is not None:
            assert len(records) == expected, (
                f"{name}: expected {expected} records, got {len(records)}"
            )


class TestGoldenFileIntegrity:
    """Test that golden files are well-formed and match parsed data."""

    @pytest.mark.parametrize("name", list(LAYOUT_REGISTRY.keys()))
    def test_golden_file_exists(self, name: str):
        """Each layout should have a corresponding golden file."""
        path = GOLDEN_DIR / f"{name}.json"
        assert path.exists(), f"Golden file missing: {path}"

    @pytest.mark.parametrize("name", list(LAYOUT_REGISTRY.keys()))
    def test_golden_file_structure(self, name: str):
        """Golden files should have the expected JSON structure."""
        golden = _load_golden(name)
        assert "source_file" in golden
        assert "copybook_layout" in golden
        assert "record_count" in golden
        assert "records" in golden
        assert isinstance(golden["records"], list)
        assert golden["record_count"] == len(golden["records"])

    @pytest.mark.parametrize("name", list(LAYOUT_REGISTRY.keys()))
    def test_golden_matches_parsed(self, name: str, comparator):
        """Golden file records should match freshly parsed data."""
        src = DATA_DIR / f"{name}.txt"
        if not src.exists():
            pytest.skip(f"Source file not found: {src}")

        fields = LAYOUT_REGISTRY[name]
        parsed = parse_file(str(src), fields)
        golden = _load_golden(name)

        result = comparator.compare_record_sets(
            expected=golden["records"],
            actual=parsed,
        )
        assert result["passed"], (
            f"{name}: golden file mismatch - "
            f"{result['records_with_diffs']} records differ, "
            f"{len(result['missing_in_actual'])} missing, "
            f"{len(result['extra_in_actual'])} extra"
        )


class TestFieldTypes:
    """Validate that parsed field types are correct."""

    def test_account_id_is_integer(self):
        golden = _load_golden("acctdata")
        for rec in golden["records"]:
            assert isinstance(rec["ACCT-ID"], int), (
                f"ACCT-ID should be int, got {type(rec['ACCT-ID'])}"
            )

    def test_account_balance_is_numeric_string(self):
        golden = _load_golden("acctdata")
        for rec in golden["records"]:
            bal = rec["ACCT-CURR-BAL"]
            assert "." in str(bal), (
                f"ACCT-CURR-BAL should have decimal: {bal}"
            )

    def test_card_num_is_string(self):
        golden = _load_golden("carddata")
        for rec in golden["records"]:
            assert isinstance(rec["CARD-NUM"], str), (
                f"CARD-NUM should be string, got {type(rec['CARD-NUM'])}"
            )

    def test_customer_ssn_is_integer(self):
        golden = _load_golden("custdata")
        for rec in golden["records"]:
            assert isinstance(rec["CUST-SSN"], int), (
                f"CUST-SSN should be int, got {type(rec['CUST-SSN'])}"
            )

    def test_transaction_amount_is_numeric(self):
        golden = _load_golden("dailytran")
        for rec in golden["records"]:
            amt = rec["DALYTRAN-AMT"]
            assert "." in str(amt), (
                f"DALYTRAN-AMT should have decimal: {amt}"
            )
