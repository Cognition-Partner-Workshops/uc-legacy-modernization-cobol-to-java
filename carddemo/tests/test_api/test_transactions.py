"""Tests for transaction routes — validates COTRN00C/01C/02C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_list_transactions(client, seed_data, admin_token):
    response = client.get("/api/v1/transactions", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] >= 1


def test_get_transaction(client, seed_data, admin_token):
    response = client.get("/api/v1/transactions/0000000000000001", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["tran_id"] == "0000000000000001"
    assert data["tran_amt"] == "50.00"


def test_get_transaction_not_found(client, seed_data, admin_token):
    response = client.get("/api/v1/transactions/9999999999999999", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 404


def test_add_transaction(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/transactions",
        json={
            "tran_type_cd": "01",
            "tran_cat_cd": 1,
            "tran_source": "Online",
            "tran_desc": "Test Purchase",
            "tran_amt": "25.50",
            "card_num": "4111111111111111",
            "merchant_name": "Test Store",
        },
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["tran_amt"] == "25.50"
    assert data["tran_desc"] == "Test Purchase"
    assert len(data["tran_id"]) == 16


def test_list_transactions_by_card(client, seed_data, admin_token):
    response = client.get(
        "/api/v1/transactions?card_num=4111111111111111",
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert all(t["card_num"] == "4111111111111111" for t in data["transactions"])
