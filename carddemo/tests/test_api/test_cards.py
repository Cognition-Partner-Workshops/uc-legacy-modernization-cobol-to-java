"""Tests for card routes — validates COCRDLIC/COCRDSLC/COCRDUPC logic."""

from tests.conftest import *  # noqa: F401, F403


def test_list_cards(client, seed_data, admin_token):
    response = client.get("/api/v1/cards", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] >= 2


def test_list_cards_by_account(client, seed_data, admin_token):
    response = client.get("/api/v1/cards?acct_id=1", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] >= 1
    assert all(c["acct_id"] == 1 for c in data["cards"])


def test_get_card(client, seed_data, admin_token):
    response = client.get("/api/v1/cards/4111111111111111", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["card_num"] == "4111111111111111"
    assert data["acct_id"] == 1


def test_get_card_not_found(client, seed_data, admin_token):
    response = client.get("/api/v1/cards/9999999999999999", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 404


def test_update_card(client, seed_data, admin_token):
    response = client.put(
        "/api/v1/cards/4111111111111111",
        json={"embossed_name": "JOHN SMITH JR"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["embossed_name"] == "JOHN SMITH JR"
