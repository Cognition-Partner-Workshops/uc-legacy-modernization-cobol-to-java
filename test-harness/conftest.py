"""
conftest.py - Shared pytest fixtures for the CardDemo migration test harness.
"""

from __future__ import annotations

import importlib.util
import json
import sys
from pathlib import Path

import pytest

# ---------------------------------------------------------------------------
# Bootstrap: make the 'test-harness' directory importable as 'test_harness'
# ---------------------------------------------------------------------------
_HARNESS_DIR = Path(__file__).resolve().parent


def _import_harness_module(name: str):
    """Import a module from the test-harness directory by name."""
    mod_key = f"test_harness.{name}"
    if mod_key in sys.modules:
        return sys.modules[mod_key]

    # Ensure the parent package exists
    if "test_harness" not in sys.modules:
        pkg_spec = importlib.util.spec_from_file_location(
            "test_harness",
            str(_HARNESS_DIR / "__init__.py"),
            submodule_search_locations=[str(_HARNESS_DIR)],
        )
        pkg = importlib.util.module_from_spec(pkg_spec)
        sys.modules["test_harness"] = pkg
        pkg_spec.loader.exec_module(pkg)

    spec = importlib.util.spec_from_file_location(
        mod_key, str(_HARNESS_DIR / f"{name}.py")
    )
    mod = importlib.util.module_from_spec(spec)
    sys.modules[mod_key] = mod
    spec.loader.exec_module(mod)
    return mod


# Import harness modules
_parser = _import_harness_module("copybook_parser")
_generator = _import_harness_module("golden_file_generator")
_reconciliation = _import_harness_module("reconciliation")
_comparator = _import_harness_module("comparator")
_contracts = _import_harness_module("contracts")

parse_file = _parser.parse_file
ACCOUNT_LAYOUT = _generator.ACCOUNT_LAYOUT
CARD_LAYOUT = _generator.CARD_LAYOUT
CARDXREF_LAYOUT = _generator.CARDXREF_LAYOUT
CUSTOMER_LAYOUT = _generator.CUSTOMER_LAYOUT
DISCGRP_LAYOUT = _generator.DISCGRP_LAYOUT
TCATBAL_LAYOUT = _generator.TCATBAL_LAYOUT
TRANCATG_LAYOUT = _generator.TRANCATG_LAYOUT
TRANSACTION_LAYOUT = _generator.TRANSACTION_LAYOUT
TRANTYPE_LAYOUT = _generator.TRANTYPE_LAYOUT

# Project root (two levels up from test-harness/)
PROJECT_ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = PROJECT_ROOT / "app" / "data" / "ASCII"
GOLDEN_DIR = PROJECT_ROOT / "golden-files"


def _load_golden(name: str) -> dict:
    """Load a golden JSON file."""
    path = GOLDEN_DIR / name
    if not path.exists():
        pytest.skip(f"Golden file {name} not found. Run golden_file_generator first.")
    with path.open() as f:
        return json.load(f)


# ---------------------------------------------------------------------------
# Data-file fixtures (parsed from ASCII source)
# ---------------------------------------------------------------------------

@pytest.fixture
def acctdata_records():
    return parse_file(DATA_DIR / "acctdata.txt", ACCOUNT_LAYOUT)


@pytest.fixture
def carddata_records():
    return parse_file(DATA_DIR / "carddata.txt", CARD_LAYOUT)


@pytest.fixture
def custdata_records():
    return parse_file(DATA_DIR / "custdata.txt", CUSTOMER_LAYOUT)


@pytest.fixture
def cardxref_records():
    return parse_file(DATA_DIR / "cardxref.txt", CARDXREF_LAYOUT)


@pytest.fixture
def dailytran_records():
    return parse_file(DATA_DIR / "dailytran.txt", TRANSACTION_LAYOUT)


@pytest.fixture
def trantype_records():
    return parse_file(DATA_DIR / "trantype.txt", TRANTYPE_LAYOUT)


@pytest.fixture
def trancatg_records():
    return parse_file(DATA_DIR / "trancatg.txt", TRANCATG_LAYOUT)


@pytest.fixture
def tcatbal_records():
    return parse_file(DATA_DIR / "tcatbal.txt", TCATBAL_LAYOUT)


@pytest.fixture
def discgrp_records():
    return parse_file(DATA_DIR / "discgrp.txt", DISCGRP_LAYOUT)


# ---------------------------------------------------------------------------
# Golden-file fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def golden_acctdata():
    return _load_golden("acctdata.json")


@pytest.fixture
def golden_carddata():
    return _load_golden("carddata.json")


@pytest.fixture
def golden_custdata():
    return _load_golden("custdata.json")


@pytest.fixture
def golden_cardxref():
    return _load_golden("cardxref.json")


@pytest.fixture
def golden_dailytran():
    return _load_golden("dailytran.json")


@pytest.fixture
def golden_trantype():
    return _load_golden("trantype.json")


@pytest.fixture
def golden_trancatg():
    return _load_golden("trancatg.json")


@pytest.fixture
def golden_tcatbal():
    return _load_golden("tcatbal.json")


@pytest.fixture
def golden_discgrp():
    return _load_golden("discgrp.json")
