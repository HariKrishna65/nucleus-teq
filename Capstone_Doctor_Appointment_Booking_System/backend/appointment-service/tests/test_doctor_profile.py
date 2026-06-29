from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_doctor_can_create_and_view_profile():
    token = client.post(
        "/auth/login",
        json={"email": "doctor@example.com", "password": "Password123!"},
    ).json()["access_token"]

    create_response = client.post(
        "/doctors/profile",
        json={
            "doctor_id": "doctor@example.com",
            "specialization": "Cardiology",
            "qualification": "MBBS, MD",
            "experience_years": 10,
            "consultation_fee": 500,
            "clinic_address": "Main Street Hospital",
        },
        headers={"Authorization": f"Bearer {token}"},
    )
    assert create_response.status_code == 200

    profile_response = client.get(
        "/doctors/profile",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert profile_response.status_code == 200
    assert profile_response.json()["specialization"] == "Cardiology"
