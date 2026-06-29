from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_protected_endpoint_requires_token():
    response = client.get("/auth/me")
    assert response.status_code == 401


def test_protected_endpoint_accepts_valid_token():
    register_response = client.post(
        "/auth/register",
        json={
            "full_name": "Alice Smith",
            "email": "alice@example.com",
            "password": "Password123!",
            "phone": "9123456780",
            "role": "PATIENT",
        },
    )
    assert register_response.status_code == 200

    login_response = client.post(
        "/auth/login",
        json={"email": "alice@example.com", "password": "Password123!"},
    )
    token = login_response.json()["access_token"]

    response = client.get("/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200
    assert response.json()["email"] == "alice@example.com"
