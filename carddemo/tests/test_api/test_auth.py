"""Tests for auth routes — validates COSGN00C login logic."""

from tests.conftest import *  # noqa: F401, F403


def test_login_success(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "ADMIN001", "password": "PASSWORD"})
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "ADMIN001"
    assert data["user_type"] == "A"
    assert "access_token" in data


def test_login_wrong_password(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "ADMIN001", "password": "WRONG"})
    assert response.status_code == 401


def test_login_user_not_found(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "NOBODY", "password": "PASSWORD"})
    assert response.status_code == 401


def test_login_empty_user_id(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "", "password": "PASSWORD"})
    assert response.status_code == 400


def test_login_empty_password(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "ADMIN001", "password": ""})
    assert response.status_code == 400


def test_seed_admin(client):
    response = client.post("/api/v1/auth/seed-admin")
    assert response.status_code == 200
    assert "Default users created" in response.json()["message"]

    # Second call should say already exists
    response = client.post("/api/v1/auth/seed-admin")
    assert response.status_code == 200
    assert "already exists" in response.json()["message"]


def test_login_case_insensitive(client, seed_users):
    response = client.post("/api/v1/auth/login", json={"user_id": "admin001", "password": "password"})
    assert response.status_code == 200
