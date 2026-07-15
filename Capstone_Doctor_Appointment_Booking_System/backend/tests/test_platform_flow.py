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


def register(client, role, email, admin_headers=None):
    common = {"full_name": f"Test {role.title()}", "email": email, "password": "Secure@1", "phone": "9876543210"}
    if role == "PATIENT": common.update({"gender": "OTHER", "date_of_birth": "1990-01-01"})
    else: common.update({"qualification": "MBBS", "specialization": "Cardiology", "experience": 8, "license_number": "MED-100", "consultation_fee": 500, "clinic_address": "Central Clinic"})
    role_path = role.lower()
    created = client.post(f"/auth/{role_path}/register", json=common)
    assert created.status_code == 201
    if role == "DOCTOR":
        pending_login = client.post("/auth/doctor/login", json={"email": email, "password": "Secure@1"})
        assert pending_login.status_code == 403
        assert admin_headers
        approved = client.patch(f"/admin/doctors/{created.json()['id']}/activation", headers=admin_headers, json={"active": True})
        assert approved.status_code == 200
    token = client.post(f"/auth/{role_path}/login", json={"email": email, "password": "Secure@1"}).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_end_to_end_booking_payment_and_cancellation(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)
    patient_headers = register(client, "PATIENT", "patient@gmail.com")
    starts = datetime.now(timezone.utc) + timedelta(days=2)
    slot = client.post("/doctor/slots", headers=doctor_headers, json={"starts_at": starts.isoformat(), "ends_at": (starts + timedelta(minutes=30)).isoformat()})
    assert slot.status_code == 201
    doctor_id = client.get("/profile/me", headers=doctor_headers).json()["id"]
    appointment = client.post("/appointments", headers=patient_headers, json={"doctor_id": doctor_id, "slot_id": slot.json()["id"]})
    assert appointment.status_code == 201
    duplicate = client.post("/appointments", headers=patient_headers, json={"doctor_id": doctor_id, "slot_id": slot.json()["id"]})
    assert duplicate.status_code == 409
    payment = client.post("/payments", headers=patient_headers, json={"appointment_id": appointment.json()["id"], "method": "UPI"})
    assert payment.status_code == 201 and payment.json()["status"] == "SUCCESS"
    cancelled = client.post(f"/appointments/{appointment.json()['id']}/cancel", headers=patient_headers)
    assert cancelled.status_code == 200 and cancelled.json()["status"] == "CANCELLED"
    doctors = client.get("/doctors", headers=patient_headers)
    assert doctors.status_code == 200
    assert doctors.json()[0]["available_slots"]
    assert doctors.json()[0]["name"] == "Test Doctor"


def test_role_access_is_enforced(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    patient_headers = register(client, "PATIENT", "patient@gmail.com")
    doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)
    assert client.post("/doctor/slots", headers=patient_headers, json={"starts_at": "2030-01-01T10:00:00Z", "ends_at": "2030-01-01T10:30:00Z"}).status_code == 403
    assert client.get("/admin/statistics", headers=patient_headers).status_code == 403
    assert client.post("/appointments", headers=doctor_headers, json={"doctor_id": "doctor-1", "slot_id": "slot-1"}).status_code == 403


def test_get_doctors_api_does_not_accept_name_input(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    parameters = client.get("/openapi.json").json()["paths"]["/doctors"]["get"]["parameters"]
    parameter_names = {parameter["name"] for parameter in parameters}
    assert "name" not in parameter_names
    assert {"specialization", "location", "min_experience", "max_fee", "available"}.issubset(parameter_names)


def test_legacy_appointment_doctor_endpoints_are_removed(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    patient_headers = register(client, "PATIENT", "patient@gmail.com")
    assert client.get("/appointments/doctors", headers=patient_headers).status_code == 404
    assert client.post("/appointments/book", headers=patient_headers).status_code == 404


def test_doctor_cancellation_requires_reason_and_admin_approval(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)
    patient_headers = register(client, "PATIENT", "patient@gmail.com")
    starts = datetime.now(timezone.utc) + timedelta(days=4)
    slot = client.post(
        "/doctor/slots",
        headers=doctor_headers,
        json={"starts_at": starts.isoformat(), "ends_at": (starts + timedelta(minutes=30)).isoformat()},
    )
    assert slot.status_code == 201
    doctor_id = client.get("/profile/me", headers=doctor_headers).json()["id"]
    appointment = client.post("/appointments", headers=patient_headers, json={"doctor_id": doctor_id, "slot_id": slot.json()["id"]})
    assert appointment.status_code == 201
    appointment_id = appointment.json()["id"]
    payment = client.post("/payments", headers=patient_headers, json={"appointment_id": appointment_id, "method": "UPI"})
    assert payment.status_code == 201

    missing_reason = client.post(
        f"/doctor/appointments/{appointment_id}/cancel-request",
        headers=doctor_headers,
        json={},
    )
    assert missing_reason.status_code == 422

    requested = client.post(
        f"/doctor/appointments/{appointment_id}/cancel-request",
        headers=doctor_headers,
        json={"reason": "Emergency surgery"},
    )
    assert requested.status_code == 200
    assert requested.json()["status"] == "BOOKED"
    assert requested.json()["doctor_cancellation_status"] == "PENDING"
    assert requested.json()["doctor_cancellation_reason"] == "Emergency surgery"

    pending = client.get("/admin/appointments/cancellation-requests", headers=admin_headers)
    assert pending.status_code == 200
    assert pending.json()[0]["id"] == appointment_id

    approved = client.patch(
        f"/admin/appointments/{appointment_id}/cancellation-request/accept",
        headers=admin_headers,
    )
    assert approved.status_code == 200
    assert approved.json()["status"] == "CANCELLED"
    assert approved.json()["doctor_cancellation_status"] == "APPROVED"


def test_profile_update_fields_are_role_specific(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    patient_headers = register(client, "PATIENT", "patient@gmail.com")
    doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)

    patient_update = client.patch(
        "/patient/profile/me",
        headers=patient_headers,
        json={"name": "Updated Patient", "gender": "FEMALE", "date_of_birth": "1991-02-03"},
    )
    assert patient_update.status_code == 200
    assert patient_update.json()["name"] == "Updated Patient"
    assert patient_update.json()["gender"] == "FEMALE"
    assert patient_update.json()["date_of_birth"] == "1991-02-03"
    assert "qualification" not in patient_update.json()

    patient_doctor_field = client.patch(
        "/patient/profile/me",
        headers=patient_headers,
        json={"qualification": "MBBS"},
    )
    assert patient_doctor_field.status_code == 422

    doctor_update = client.patch(
        "/doctor/profile/me",
        headers=doctor_headers,
        json={"specialization": "Neurology", "license_number": "MED-999", "consultation_fee": 750},
    )
    assert doctor_update.status_code == 200
    assert doctor_update.json()["status"] == "PENDING"
    assert doctor_update.json()["pending_profile_changes"]["specialization"] == "Neurology"
    assert client.get("/profile/me", headers=doctor_headers).json()["specialization"] == "Cardiology"

    doctor_id = client.get("/profile/me", headers=doctor_headers).json()["id"]
    pending_changes = client.get("/admin/doctors/profile-changes", headers=admin_headers)
    assert pending_changes.status_code == 200
    assert pending_changes.json()[0]["id"] == doctor_id

    accepted = client.patch(f"/admin/doctors/{doctor_id}/profile-change/accept", headers=admin_headers)
    assert accepted.status_code == 200
    assert accepted.json()["specialization"] == "Neurology"
    assert accepted.json()["license_number"] == "MED-999"
    assert accepted.json()["consultation_fee"] == 750

    doctor_patient_field = client.patch(
        "/doctor/profile/me",
        headers=doctor_headers,
        json={"gender": "OTHER"},
    )
    assert doctor_patient_field.status_code == 422


def test_doctor_can_update_slot_and_admin_can_see_inactive_doctors(tmp_path, monkeypatch):
    configure_store(tmp_path, monkeypatch); client = TestClient(app)
    admin_headers = register_admin(client, "admin@gmail.com")
    doctor_headers = register(client, "DOCTOR", "doctor@gmail.com", admin_headers)
    starts = datetime.now(timezone.utc) + timedelta(days=3)
    slot = client.post(
        "/doctor/slots",
        headers=doctor_headers,
        json={"starts_at": starts.isoformat(), "ends_at": (starts + timedelta(minutes=30)).isoformat()},
    )
    assert slot.status_code == 201
    moved = client.put(
        f"/doctor/slots/{slot.json()['id']}",
        headers=doctor_headers,
        json={"starts_at": (starts + timedelta(hours=1)).isoformat(), "ends_at": (starts + timedelta(hours=1, minutes=30)).isoformat()},
    )
    assert moved.status_code == 200
    assert moved.json()["starts_at"] == (starts + timedelta(hours=1)).isoformat()

    doctor_id = client.get("/profile/me", headers=doctor_headers).json()["id"]
    disabled = client.patch(f"/admin/doctors/{doctor_id}/activation", headers=admin_headers, json={"active": False})
    assert disabled.status_code == 200
    doctors = client.get("/admin/doctors", headers=admin_headers)
    assert doctors.status_code == 200
    assert doctors.json()[0]["active"] is False
    assert doctors.json()[0]["approval_status"] == "SUSPENDED"


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
