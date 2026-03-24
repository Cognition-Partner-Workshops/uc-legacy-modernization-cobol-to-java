"""Tests for admin routes — validates COADM01C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_admin_dashboard(client, seed_data, admin_token):
    response = client.get("/api/v1/admin/dashboard", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert "total_accounts" in data
    assert "total_cards" in data
    assert "menu_options" in data
    assert data["total_accounts"] >= 2


def test_admin_dashboard_requires_admin(client, seed_data, user_token):
    response = client.get("/api/v1/admin/dashboard", headers={"Authorization": f"Bearer {user_token}"})
    assert response.status_code == 403
