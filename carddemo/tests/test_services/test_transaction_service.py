"""Tests for transaction service — validates COTRN02C add transaction logic."""

from decimal import Decimal

from app.schemas.transaction import TransactionCreate
from app.services.transaction_service import add_transaction, get_transaction, list_transactions

from tests.conftest import *  # noqa: F401, F403


def test_add_transaction(db, seed_data):
    data = TransactionCreate(
        tran_type_cd="01",
        tran_cat_cd=1,
        tran_desc="Test Purchase",
        tran_amt=Decimal("50.00"),
        card_num="4111111111111111",
    )
    result = add_transaction(db, data)
    assert result.tran_amt == Decimal("50.00")
    assert len(result.tran_id) == 16


def test_get_transaction(db, seed_data):
    result = get_transaction(db, "0000000000000001")
    assert result is not None
    assert result.tran_amt == Decimal("50.00")


def test_list_transactions(db, seed_data):
    trans, total = list_transactions(db)
    assert total >= 1


def test_list_transactions_by_card(db, seed_data):
    trans, total = list_transactions(db, card_num="4111111111111111")
    assert total >= 1
    assert all(t.card_num == "4111111111111111" for t in trans)
