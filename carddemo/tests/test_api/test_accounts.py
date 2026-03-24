"""Tests for account routes — validates COACTVWC/COACTUPC logic."""

from tests.conftest import *  # noqa: F401, F403


def test_list_accounts(client, seed_data, admin_token):
    response = client.get("/api/v1/accounts", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] >= 2
    assert len(data["accounts"]) >= 2


def test_get_account(client, seed_data, admin_token):
    response = client.get("/api/v1/accounts/1", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["acct_id"] == 1
    assert data["active_status"] == "Y"
    assert data["curr_bal"] == "1940.00"


def test_get_account_not_found(client, seed_data, admin_token):
    response = client.get("/api/v1/accounts/99999", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 404


def test_update_account(client, seed_data, admin_token):
    response = client.put(
        "/api/v1/accounts/1",
        json={"credit_limit": "25000.00"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["credit_limit"] == "25000.00"


def test_accounts_unauthorized(client, seed_data):
    response = client.get("/api/v1/accounts")
    assert response.status_code == 401
