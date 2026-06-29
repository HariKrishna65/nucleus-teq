from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_doctor_access_allowed_for_doctor_role():
    client.post(
        "/auth/register",
        json={
            "full_name": "Dr. Jane",
            "email": "doctor@example.com",
            "password": "Password123!",
            "phone": "9123456789",
            "role": "DOCTOR",
        },
    )
    token = client.post(
        "/auth/login",
        json={"email": "doctor@example.com", "password": "Password123!"},
    ).json()["access_token"]

    response = client.get("/auth/doctor-dashboard", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200
    assert response.json()["role"] == "DOCTOR"


def test_patient_access_denied_for_admin_only_endpoint():
    client.post(
        "/auth/register",
        json={
            "full_name": "Patient One",
            "email": "patient@example.com",
            "password": "Password123!",
            "phone": "9876543210",
            "role": "PATIENT",
        },
    )
    token = client.post(
        "/auth/login",
        json={"email": "patient@example.com", "password": "Password123!"},
    ).json()["access_token"]

    response = client.get("/auth/admin-dashboard", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 403
