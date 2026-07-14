from datetime import datetime, timedelta, timezone
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


def register(client, role, email, admin_headers=None):
    common = {"full_name": f"Test {role.title()}", "email": email, "password": "Secure@1", "phone": "9876543210"}
    if role == "PATIENT":
        common.update({"gender": "OTHER", "date_of_birth": "1990-01-01"})
    else:
        common.update({
            "qualification": "MBBS",
            "specialization": "Cardiology",
            "experience": 8,
            "license_number": "MED-100",
            "consultation_fee": 500,
            "clinic_address": "Central Clinic"
        })
    role_path = role.lower()
    created = client.post(f"/auth/{role_path}/register", json=common)
    assert created.status_code == 201
    
    if role == "DOCTOR":
        assert admin_headers
        approved = client.patch(f"/admin/doctors/{created.json()['id']}/activation", headers=admin_headers, json={"active": True})
        assert approved.status_code == 200

    token = client.post(f"/auth/{role_path}/login", json={"email": email, "password": "Secure@1"}).json()["access_token"]
    return created.json(), {"Authorization": f"Bearer {token}"}


def test_gmail_validation_enforced(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    
    # Non-gmail email registration should fail validation
    payload = {
        "full_name": "Jane Patient",
        "email": "jane@example.com",  # Non-gmail
        "password": "Secure@1",
        "phone": "9876543210",
        "gender": "FEMALE",
        "date_of_birth": "1995-04-12",
    }
    response = client.post("/auth/patient/register", json=payload)
    assert response.status_code == 422


def test_slot_creation_rules(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    doctor, doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)

    # 1. Slot duration > 30 minutes should fail
    starts = datetime.now(timezone.utc) + timedelta(days=2)
    response = client.post(
        "/doctor/slots",
        headers=doctor_headers,
        json={"starts_at": starts.isoformat(), "ends_at": (starts + timedelta(minutes=45)).isoformat()}
    )
    assert response.status_code == 400
    assert "Slot duration" in response.json()["detail"]

    # 2. Slot created less than 2 hours in advance should fail
    starts_soon = datetime.now(timezone.utc) + timedelta(minutes=90)
    response = client.post(
        "/doctor/slots",
        headers=doctor_headers,
        json={"starts_at": starts_soon.isoformat(), "ends_at": (starts_soon + timedelta(minutes=30)).isoformat()}
    )
    assert response.status_code == 400
    assert "advance" in response.json()["detail"]


def test_doctor_self_deactivation_and_reactivation(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch)
    client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    doctor, doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)
    doctor_id = doctor["id"]

    # Doctor deactivates profile
    response = client.post("/doctor/profile/deactivate", headers=doctor_headers)
    assert response.status_code == 200
    assert response.json() == {"message": "Account deactivated successfully"}

    # Attempt to log in again should be forbidden (inactive)
    response = client.post("/auth/doctor/login", json={"email": "doctor@gmail.com", "password": "Secure@1"})
    assert response.status_code == 403
    assert "inactive" in response.json()["detail"].lower()

    # Doctor requests reactivation
    response = client.post("/auth/doctor/request-activation", json={"email": "doctor@gmail.com", "password": "Secure@1"})
    assert response.status_code == 200
    assert "Reactivation request submitted" in response.json()["message"]

    # Admin reactivates the doctor
    response = client.patch(f"/admin/doctors/{doctor_id}/activation", headers=admin_headers, json={"active": True})
    assert response.status_code == 200

    # Doctor should be able to log in now
    response = client.post("/auth/doctor/login", json={"email": "doctor@gmail.com", "password": "Secure@1"})
    assert response.status_code == 200
    assert "access_token" in response.json()
