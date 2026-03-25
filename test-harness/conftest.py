"""Shared pytest fixtures for the migration test harness."""

import json
from pathlib import Path

import pytest

REPO_ROOT = Path(__file__).resolve().parent.parent
GOLDEN_DIR = REPO_ROOT / "golden-files"
DATA_DIR = REPO_ROOT / "app" / "data" / "ASCII"


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
