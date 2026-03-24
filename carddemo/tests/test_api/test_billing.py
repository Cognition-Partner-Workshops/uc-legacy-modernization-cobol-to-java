"""Tests for billing routes — validates COBIL00C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_pay_bill(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/billing/pay",
        json={"acct_id": 1, "payment_amount": "500.00"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["payment_amount"] == "500.00"
    assert data["new_balance"] == "1440.00"


def test_pay_bill_account_not_found(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/billing/pay",
        json={"acct_id": 99999, "payment_amount": "100.00"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 404


def test_pay_bill_negative_amount(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/billing/pay",
        json={"acct_id": 1, "payment_amount": "-100.00"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 400
