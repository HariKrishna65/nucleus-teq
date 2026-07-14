import json

from fastapi.testclient import TestClient

from backend.main import app
from backend.database import database as db_service


def test_patient_registration(tmp_path, monkeypatch):
    users_file = tmp_path / "users.json"
    users_file.write_text("[]", encoding="utf-8")
    monkeypatch.setattr(db_service, "USERS_FILE", users_file)
    monkeypatch.setattr(db_service, "DOCTORS_FILE", tmp_path / "doctors.json")

    client = TestClient(app)
    payload = {
        "full_name": "Jane Patient",
        "email": "jane@example.com",
        "password": "Secure@1",
        "phone": "9876543210",
        "gender": "FEMALE",
        "date_of_birth": "1995-04-12",
    }
    response = client.post("/auth/patient/register", json=payload)
    assert response.status_code == 201
    assert response.json()["role"] == "PATIENT"
    assert "hashed_password" not in response.json()

    stored = json.loads(users_file.read_text(encoding="utf-8"))[0]
    assert stored["hashed_password"] != payload["password"]


def test_registration_validation(tmp_path, monkeypatch):
    monkeypatch.setattr(db_service, "USERS_FILE", tmp_path / "users.json")
    monkeypatch.setattr(db_service, "DOCTORS_FILE", tmp_path / "doctors.json")
    response = TestClient(app).post(
        "/auth/patient/register",
        json={
            "full_name": "J1",
            "email": "bad-email",
            "password": "weak",
            "phone": "123",
        },
    )
    assert response.status_code == 422


def test_doctor_registration_requires_admin_approval_before_login(tmp_path, monkeypatch):
    users_file = tmp_path / "users.json"
    users_file.write_text("[]", encoding="utf-8")
    monkeypatch.setattr(db_service, "USERS_FILE", users_file)
    monkeypatch.setattr(db_service, "DOCTORS_FILE", tmp_path / "doctors.json")

    client = TestClient(app)
    payload = {
        "full_name": "Dana Doctor",
        "email": "doctor@example.com",
        "password": "Secure@1",
        "phone": "9876543210",
        "qualification": "MBBS",
        "specialization": "Dermatology",
        "experience": 5,
        "license_number": "MED-200",
        "consultation_fee": 650,
        "clinic_address": "Care Street",
    }
    response = client.post("/auth/doctor/register", json=payload)
    assert response.status_code == 201
    assert response.json()["active"] is False
    assert response.json()["approval_status"] == "PENDING"
