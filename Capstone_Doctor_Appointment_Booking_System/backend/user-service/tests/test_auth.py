from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_register_and_login_user():
    payload = {
        "full_name": "John Doe",
        "email": "john@example.com",
        "password": "Password123!",
        "phone": "9876543210",
        "role": "PATIENT",
    }

    register_response = client.post("/auth/register", json=payload)
    assert register_response.status_code == 200
    body = register_response.json()
    assert body["email"] == payload["email"]
    assert body["role"] == payload["role"]

    login_response = client.post(
        "/auth/login",
        json={"email": payload["email"], "password": payload["password"]},
    )
    assert login_response.status_code == 200
    token_data = login_response.json()
    assert "access_token" in token_data
    assert token_data["token_type"] == "bearer"


def test_login_with_wrong_password_fails():
    response = client.post(
        "/auth/login",
        json={"email": "john@example.com", "password": "wrong-password"},
    )
    assert response.status_code == 401
