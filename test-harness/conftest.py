"""Shared pytest fixtures for the migration test harness."""

import json
from pathlib import Path

import pytest

REPO_ROOT = Path(__file__).resolve().parent.parent
GOLDEN_DIR = REPO_ROOT / "golden-files"
DATA_DIR = REPO_ROOT / "app" / "data" / "ASCII"

# Canonical expected record counts from the legacy data files.
# Used by both golden-file and reconciliation test suites.
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
def golden_dir() -> Path:
    """Path to the golden-files directory."""
    return GOLDEN_DIR


@pytest.fixture
def data_dir() -> Path:
    """Path to the ASCII data directory."""
    return DATA_DIR


@pytest.fixture
def load_golden():
    """Factory fixture to load a golden file by name."""
    def _load(name: str) -> dict:
        path = GOLDEN_DIR / f"{name}.json"
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    return _load
