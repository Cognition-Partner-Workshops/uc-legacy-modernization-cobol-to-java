"""Tests for post transactions batch — validates CBTRN02C logic."""

from decimal import Decimal

from app.batch.post_transactions import run_post_transactions, validate_transaction
from app.models.daily_transaction import DailyTransaction

from tests.conftest import *  # noqa: F401, F403


def test_validate_valid_transaction(db, seed_data):
    daily_tran = db.query(DailyTransaction).first()
    assert daily_tran is not None
    is_valid, fail_code, fail_desc = validate_transaction(db, daily_tran)
    assert is_valid is True
    assert fail_code == 0


def test_validate_invalid_card(db, seed_data):
    daily_tran = DailyTransaction(
        tran_id="TEST0001",
        tran_type_cd="01",
        tran_cat_cd=1,
        tran_amt=Decimal("50.00"),
        card_num="9999999999999999",
        orig_ts="2024-01-20-14.00.00.000000",
    )
    db.add(daily_tran)
    db.flush()

    # Re-query to get a fresh, attached object
    daily_tran = db.query(DailyTransaction).filter_by(tran_id="TEST0001").first()
    is_valid, fail_code, fail_desc = validate_transaction(db, daily_tran)
    assert is_valid is False
    assert fail_code == 100
    assert "INVALID CARD" in fail_desc


def test_run_post_transactions(db, seed_data):
    result = run_post_transactions(db)
    assert result["posted"] >= 1
    assert "rejects" in result
