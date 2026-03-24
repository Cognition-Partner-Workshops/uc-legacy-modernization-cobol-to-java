"""Tests for authorization routes — validates COPAUS0C/1C/COPAUA0C logic."""

from tests.conftest import *  # noqa: F401, F403


def test_list_pending_authorizations(client, seed_data, admin_token):
    response = client.get("/api/v1/authorizations", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1


def test_list_pending_authorizations_by_account(client, seed_data, admin_token):
    response = client.get("/api/v1/authorizations?acct_id=1", headers={"Authorization": f"Bearer {admin_token}"})
    assert response.status_code == 200
    data = response.json()
    assert all(a["acct_id"] == 1 for a in data)


def test_get_authorization_details(client, seed_data, admin_token):
    # First get the summary to find the actual auto-generated ID
    summaries = client.get("/api/v1/authorizations", headers={"Authorization": f"Bearer {admin_token}"})
    summary_id = summaries.json()[0]["id"]

    response = client.get(
        f"/api/v1/authorizations/{summary_id}/details", headers={"Authorization": f"Bearer {admin_token}"}
    )
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1
    assert data[0]["summary_id"] == summary_id


def test_approve_authorization(client, seed_data, admin_token):
    # First get the detail ID dynamically
    summaries = client.get("/api/v1/authorizations", headers={"Authorization": f"Bearer {admin_token}"})
    summary_id = summaries.json()[0]["id"]
    details = client.get(
        f"/api/v1/authorizations/{summary_id}/details", headers={"Authorization": f"Bearer {admin_token}"}
    )
    detail_id = details.json()[0]["id"]

    response = client.post(
        "/api/v1/authorizations/decide",
        json={"detail_id": detail_id, "decision": "APPROVE"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["decision"] == "APPROVED"


def test_decline_authorization(client, seed_data, admin_token):
    # First get the detail ID dynamically
    summaries = client.get("/api/v1/authorizations", headers={"Authorization": f"Bearer {admin_token}"})
    summary_id = summaries.json()[0]["id"]
    details = client.get(
        f"/api/v1/authorizations/{summary_id}/details", headers={"Authorization": f"Bearer {admin_token}"}
    )
    detail_id = details.json()[0]["id"]

    response = client.post(
        "/api/v1/authorizations/decide",
        json={"detail_id": detail_id, "decision": "DECLINE", "reason": "Suspicious activity"},
        headers={"Authorization": f"Bearer {admin_token}"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["decision"] == "DECLINED"
