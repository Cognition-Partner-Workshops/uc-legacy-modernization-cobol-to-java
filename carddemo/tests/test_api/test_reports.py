"""Tests for report routes — validates CORPT00C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_transaction_report(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/reports/transactions",
        json={"acct_id": 1},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["total_records"] >= 1


def test_transaction_report_by_card(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/reports/transactions",
        json={"card_num": "4111111111111111"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["total_records"] >= 1


def test_transaction_report_empty(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/reports/transactions",
        json={"acct_id": 99999},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["total_records"] == 0
