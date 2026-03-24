"""Tests for user routes — validates COUSR00C-03C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_list_users(client, seed_data, admin_token):
    response = client.get("/api/v1/users", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] >= 2


def test_create_user(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/users",
        json={"usr_id": "TEST001", "usr_fname": "TEST", "usr_lname": "USER", "usr_pwd": "TESTPWD", "usr_type": "U"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["usr_id"] == "TEST001"


def test_create_duplicate_user(client, seed_data, admin_token):
    response = client.post(
        "/api/v1/users",
        json={"usr_id": "ADMIN001", "usr_fname": "DUP", "usr_lname": "USER", "usr_pwd": "PWD", "usr_type": "A"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 409


def test_update_user(client, seed_data, admin_token):
    response = client.put(
        "/api/v1/users/USER0001",
        json={"usr_fname": "UPDATED"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    assert response.json()["usr_fname"] == "UPDATED"


def test_delete_user(client, seed_data, admin_token):
    response = client.delete("/api/v1/users/USER0001", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 204


def test_user_crud_requires_admin(client, seed_data, user_token):
    response = client.get("/api/v1/users", headers={"Authorization": f"Bearer {user_token}"})
    assert response.status_code == 403
