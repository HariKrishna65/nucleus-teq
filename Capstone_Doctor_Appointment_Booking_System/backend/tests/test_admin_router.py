from datetime import datetime, timezone, timedelta
from fastapi.testclient import TestClient

from backend.main import app
from backend.database import database as db_service
from backend.services.user_service import hash_password


def configure_store(tmp_path, monkeypatch):
    for name in ("USERS_FILE", "DOCTORS_FILE", "SLOTS_FILE", "APPOINTMENTS_FILE", "PAYMENTS_FILE"):
        path = tmp_path / f"{name.lower()}.json"
        path.write_text("[]", encoding="utf-8")
        monkeypatch.setattr(db_service, name, path)


def register_admin(client, email):
    users = db_service.get_users()
    users.append(
        {
            "id": "admin-1",
            "name": "Admin User",
            "email": email,
            "phone": "9999999999",
            "role": "ADMIN",
            "gender": None,
            "date_of_birth": None,
            "qualification": None,
            "specialization": None,
            "experience": None,
            "license_number": None,
            "consultation_fee": None,
            "clinic_address": None,
            "active": True,
            "approval_status": "APPROVED",
            "hashed_password": hash_password("Secure@1"),
        }
    )
    db_service.save_users(users)
    token = client.post("/auth/admin/login", json={"email": email, "password": "Secure@1"}).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def register_patient(client, email):
    common = {
        "full_name": "Test Patient",
        "email": email,
        "password": "Secure@1",
        "phone": "9876543210",
        "gender": "OTHER",
        "date_of_birth": "1990-01-01"
    }
    created = client.post("/auth/patient/register", json=common)
    assert created.status_code == 201
    token = client.post("/auth/patient/login", json={"email": email, "password": "Secure@1"}).json()["access_token"]
    return created.json(), {"Authorization": f"Bearer {token}"}


def test_list_doctor_profiles_as_admin(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@example.com")
    
    # Check initially empty
    response = client.get("/admin/doctor-profiles", headers=admin_headers)
    assert response.status_code == 200
    assert response.json() == []

    # Manually populate DOCTORS_FILE
    doctors = [{"id": "doctor-1", "name": "Dr. Test", "email": "doctor@example.com", "license_number": "MED-100"}]
    db_service.save_doctors(doctors)

    # Check listing includes the doctor
    response = client.get("/admin/doctor-profiles", headers=admin_headers)
    assert response.status_code == 200
    profiles = response.json()
    assert len(profiles) == 1
    assert profiles[0]["email"] == "doctor@example.com"
    assert profiles[0]["license_number"] == "MED-100"


def test_list_doctor_profiles_as_non_admin_forbidden(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    patient, patient_headers = register_patient(client, "patient@example.com")

    # Access as patient should be forbidden
    response = client.get("/admin/doctor-profiles", headers=patient_headers)
    assert response.status_code == 403


def test_delete_doctor_profile_as_admin(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@example.com")
    
    # Manually populate DOCTORS_FILE
    doctor_id = "doctor-1"
    doctors = [{"id": doctor_id, "name": "Dr. Test", "email": "doctor@example.com", "license_number": "MED-100"}]
    db_service.save_doctors(doctors)

    # Delete doctor profile as admin
    response = client.delete(f"/admin/doctors/{doctor_id}", headers=admin_headers)
    assert response.status_code == 200
    assert response.json() == {"deleted": True}

    # Verify doctor is no longer in profiles list
    response = client.get("/admin/doctor-profiles", headers=admin_headers)
    assert response.status_code == 200
    assert response.json() == []


def test_delete_doctor_profile_non_existent(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@example.com")

    # Delete non-existent doctor
    response = client.delete("/admin/doctors/non-existent-doctor-id", headers=admin_headers)
    assert response.status_code == 404
    assert "Doctor profile not found" in response.json()["detail"]


def test_delete_doctor_profile_as_non_admin_forbidden(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@example.com")
    patient, patient_headers = register_patient(client, "patient@example.com")
    
    # Manually populate DOCTORS_FILE
    doctor_id = "doctor-1"
    doctors = [{"id": doctor_id, "name": "Dr. Test", "email": "doctor@example.com", "license_number": "MED-100"}]
    db_service.save_doctors(doctors)

    # Access as patient should be forbidden
    response = client.delete(f"/admin/doctors/{doctor_id}", headers=patient_headers)
    assert response.status_code == 403
