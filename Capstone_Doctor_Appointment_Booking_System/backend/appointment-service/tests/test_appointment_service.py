from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200


def test_doctor_access_allowed():
    token = client.post(
        "/auth/login",
        json={"email": "doctor@example.com", "password": "Password123!"},
    ).json()["access_token"]

    response = client.get("/appointments/doctor-dashboard", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200


def test_patient_access_denied_for_admin_endpoint():
    token = client.post(
        "/auth/login",
        json={"email": "patient@example.com", "password": "Password123!"},
    ).json()["access_token"]

    response = client.get("/appointments/admin-dashboard", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 403
