from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_user_can_view_and_update_profile():
    client.post(
        "/auth/register",
        json={
            "full_name": "Lisa Brown",
            "email": "lisa@example.com",
            "password": "Password123!",
            "phone": "9988776655",
            "role": "PATIENT",
        },
    )
    token = client.post(
        "/auth/login",
        json={"email": "lisa@example.com", "password": "Password123!"},
    ).json()["access_token"]

    profile_response = client.get("/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert profile_response.status_code == 200

    update_response = client.put(
        "/auth/me",
        json={"full_name": "Lisa Updated", "phone": "1122334455"},
        headers={"Authorization": f"Bearer {token}"},
    )
    assert update_response.status_code == 200
    assert update_response.json()["full_name"] == "Lisa Updated"
    assert update_response.json()["phone"] == "1122334455"
