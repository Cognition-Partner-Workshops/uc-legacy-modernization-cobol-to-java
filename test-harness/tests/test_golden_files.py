"""Tests for golden file generation and round-trip parsing."""

from __future__ import annotations

import json
import tempfile
from pathlib import Path

import pytest

from harness.parser import (
    ACCOUNT_FIELDS,
    CARD_FIELDS,
    CARDXREF_FIELDS,
    CUSTOMER_FIELDS,
    DAILYTRAN_FIELDS,
    DISCGRP_FIELDS,
    LAYOUT_REGISTRY,
    TCATBAL_FIELDS,
    TRANCATG_FIELDS,
    TRANTYPE_FIELDS,
    parse_file,
)

DATA_DIR = Path(__file__).resolve().parent.parent.parent / "app" / "data" / "ASCII"
GOLDEN_DIR = Path(__file__).resolve().parent.parent.parent / "golden-files"


@pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
class TestGoldenFileConsistency:
    """Verify that generated golden files are valid and self-consistent."""

    @pytest.mark.parametrize("stem", sorted(LAYOUT_REGISTRY.keys()))
    def test_golden_file_matches_parsed(self, stem):
        """Parse the data file and compare with the golden JSON (if it exists)."""
        golden_path = GOLDEN_DIR / f"{stem}.json"
        if not golden_path.exists():
            pytest.skip(f"Golden file {stem}.json not yet generated")

        fields, reclen = LAYOUT_REGISTRY[stem]
        data_path = DATA_DIR / f"{stem}.txt"
        if not data_path.exists():
            pytest.skip(f"Data file {stem}.txt not found")

        parsed = parse_file(data_path, fields, reclen)
        with golden_path.open("r") as fh:
            golden = json.load(fh)

        assert len(parsed) == len(golden), (
            f"Record count mismatch: parsed={len(parsed)}, golden={len(golden)}"
        )

        for i, (p, g) in enumerate(zip(parsed, golden)):
            for key in g:
                assert key in p, f"Record {i}: missing field '{key}'"
                pval = p[key]
                gval = g[key]
                if isinstance(gval, float):
                    assert abs(pval - gval) < 0.01, (
                        f"Record {i}, field '{key}': {pval} != {gval}"
                    )
                else:
                    assert pval == gval, (
                        f"Record {i}, field '{key}': {pval!r} != {gval!r}"
                    )

    @pytest.mark.parametrize("stem", sorted(LAYOUT_REGISTRY.keys()))
    def test_all_records_have_correct_fields(self, stem):
        """Every record should have exactly the non-FILLER fields defined by the layout."""
        fields, reclen = LAYOUT_REGISTRY[stem]
        data_path = DATA_DIR / f"{stem}.txt"
        if not data_path.exists():
            pytest.skip(f"Data file {stem}.txt not found")

        parsed = parse_file(data_path, fields, reclen)
        expected_fields = {f.name for f in fields if f.name != "FILLER"}

        for i, rec in enumerate(parsed):
            assert set(rec.keys()) == expected_fields, (
                f"Record {i}: fields={set(rec.keys())} != expected={expected_fields}"
            )
