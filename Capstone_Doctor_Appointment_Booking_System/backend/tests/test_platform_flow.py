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
    admin_headers = register_admin(client, "admin@example.com")
    doctor_headers = register(client, "DOCTOR", "doctor@example.com", admin_headers)
    patient_headers = register(client, "PATIENT", "patient@example.com")
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
    admin_headers = register_admin(client, "admin@example.com")
    patient_headers = register(client, "PATIENT", "patient@example.com")
    doctor_headers = register(client, "DOCTOR", "doctor@example.com", admin_headers)
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
    patient_headers = register(client, "PATIENT", "patient@example.com")
    assert client.get("/appointments/doctors", headers=patient_headers).status_code == 404
    assert client.post("/appointments/book", headers=patient_headers).status_code == 404
