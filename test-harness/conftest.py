"""pytest configuration and shared fixtures for the migration test harness."""

from __future__ import annotations

import os

import pytest


def _repo_root() -> str:
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@pytest.fixture
def repo_root() -> str:
    return _repo_root()


@pytest.fixture
def data_dir(repo_root: str) -> str:
    return os.path.join(repo_root, "app", "data", "ASCII")


@pytest.fixture
def golden_dir(repo_root: str) -> str:
    return os.path.join(repo_root, "golden-files")
