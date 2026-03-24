"""Tests for interest calculation batch — validates CBACT04C logic."""

from app.batch.calculate_interest import run_calculate_interest

from tests.conftest import *  # noqa: F401, F403


def test_run_calculate_interest(db, seed_data):
    result = run_calculate_interest(db)
    assert result["processed"] >= 1
    assert "total_interest" in result
