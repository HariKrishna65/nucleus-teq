from fastapi.testclient import TestClient

from backend.main import app

client = TestClient(app)


def test_search_doctors_returns_matches():
    token = client.post(
        "/auth/login",
        json={"email": "doctor@example.com", "password": "Password123!"},
    ).json()["access_token"]

    client.post(
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

    response = client.get(
        "/doctors/search",
        params={"q": "Cardiology"},
        headers={"Authorization": f"Bearer {token}"},
    )

    assert response.status_code == 200
    results = response.json()
    assert isinstance(results, list)
    assert len(results) >= 1
    assert results[0]["specialization"] == "Cardiology"


def test_search_doctors_filter_by_clinic_address():
    token = client.post(
        "/auth/login",
        json={"email": "doctor@example.com", "password": "Password123!"},
    ).json()["access_token"]

    response = client.get(
        "/doctors/search",
        params={"clinic_address": "Main Street"},
        headers={"Authorization": f"Bearer {token}"},
    )

    assert response.status_code == 200
    assert any("Main Street" in profile["clinic_address"] for profile in response.json())
