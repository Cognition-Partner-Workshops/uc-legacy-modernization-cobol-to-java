"""Tests for auth decision service — validates COPAUA0C 6000-MAKE-DECISION logic."""

from app.models.pending_auth_detail import PendingAuthDetail
from app.services.auth_decision_service import make_decision

from tests.conftest import *  # noqa: F401, F403


def _get_detail_id(db):
    """Get the first pending auth detail ID from the database."""
    detail = db.query(PendingAuthDetail).first()
    return detail.id if detail else None


def test_approve_valid_transaction(db, seed_data):
    detail_id = _get_detail_id(db)
    result = make_decision(db, detail_id=detail_id, decision="APPROVE")
    assert result["success"] is True
    assert result["decision"] == "APPROVED"


def test_decline_manual(db, seed_data):
    detail_id = _get_detail_id(db)
    result = make_decision(db, detail_id=detail_id, decision="DECLINE", reason="Suspicious")
    assert result["success"] is True
    assert result["decision"] == "DECLINED"
    assert result["reason"] == "Suspicious"


def test_invalid_detail_id(db, seed_data):
    result = make_decision(db, detail_id=99999, decision="APPROVE")
    assert result["success"] is False


def test_invalid_decision(db, seed_data):
    detail_id = _get_detail_id(db)
    result = make_decision(db, detail_id=detail_id, decision="MAYBE")
    assert result["success"] is False
