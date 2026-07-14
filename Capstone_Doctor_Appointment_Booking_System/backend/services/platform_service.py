import logging
from datetime import datetime, timedelta, timezone
from threading import Lock
from uuid import uuid4

from fastapi import HTTPException

from backend.enums.appointment import AppointmentStatus, CancellationRequestStatus, PaymentStatus
from backend.enums.user import ApprovalStatus, UserRole
from backend.database import database as db_service
from backend.services.user_service import get_user_by_id

BOOKING_LOCK = Lock()
logger = logging.getLogger("doctor_booking.appointments")
DOCTOR_ONLY_PROFILE_FIELDS = {
    "qualification",
    "specialization",
    "experience",
    "license_number",
    "consultation_fee",
    "clinic_address",
    "active",
    "approval_status",
}


def utcnow():
    return datetime.now(timezone.utc)


def parse(value: str):
    parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=timezone.utc)


def public_user(user):
    hidden_fields = {"hashed_password"}
    if user.get("role") != UserRole.DOCTOR.value:
        hidden_fields.update(DOCTOR_ONLY_PROFILE_FIELDS)
    return {key: value for key, value in user.items() if key not in hidden_fields}


def _validate_slot_window(starts_at, ends_at):
    if starts_at.tzinfo is None or ends_at.tzinfo is None:
        raise HTTPException(422, "Slot times must include a timezone")
    if starts_at <= utcnow() or ends_at <= starts_at:
        raise HTTPException(400, "Slot must be a valid future time range")


def doctor_list(specialization=None, location=None, min_experience=None, max_fee=None, available=None, include_inactive=False):
    doctors = [
        public_user(user)
        for user in db_service.get_users()
        if user.get("role") == UserRole.DOCTOR.value and (include_inactive or user.get("active", True))
    ]
    if specialization: doctors = [d for d in doctors if specialization.lower() in d.get("specialization", "").lower()]
    if location: doctors = [d for d in doctors if location.lower() in d.get("clinic_address", "").lower()]
    if min_experience is not None: doctors = [d for d in doctors if (d.get("experience") or 0) >= min_experience]
    if max_fee is not None: doctors = [d for d in doctors if (d.get("consultation_fee") or 0) <= max_fee]
    slots = db_service.get_slots()
    for doctor in doctors:
        doctor["available_slots"] = [s for s in slots if s["doctor_id"] == doctor["id"] and not s["booked"] and parse(s["starts_at"]) > utcnow()]
    if available: doctors = [d for d in doctors if d["available_slots"]]
    return doctors


def create_slot(doctor_id, payload):
    _validate_slot_window(payload.starts_at, payload.ends_at)
    slots = db_service.get_slots()
    if any(s["doctor_id"] == doctor_id and parse(s["starts_at"]) < payload.ends_at and payload.starts_at < parse(s["ends_at"]) for s in slots):
        raise HTTPException(409, "Slot overlaps existing availability")
    slot = {"id": str(uuid4()), "doctor_id": doctor_id, "starts_at": payload.starts_at.isoformat(), "ends_at": payload.ends_at.isoformat(), "booked": False}
    slots.append(slot); db_service.save_slots(slots); return slot


def update_slot(doctor_id, slot_id, payload):
    _validate_slot_window(payload.starts_at, payload.ends_at)
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == slot_id and s["doctor_id"] == doctor_id), None)
    if not slot:
        raise HTTPException(404, "Slot not found")
    if slot["booked"]:
        raise HTTPException(409, "Booked slots cannot be edited")
    if any(
        s["id"] != slot_id and s["doctor_id"] == doctor_id and parse(s["starts_at"]) < payload.ends_at and payload.starts_at < parse(s["ends_at"])
        for s in slots
    ):
        raise HTTPException(409, "Slot overlaps existing availability")
    slot.update({"starts_at": payload.starts_at.isoformat(), "ends_at": payload.ends_at.isoformat()})
    db_service.save_slots(slots)
    return slot


def delete_slot(doctor_id, slot_id):
    slots = db_service.get_slots(); slot = next((s for s in slots if s["id"] == slot_id and s["doctor_id"] == doctor_id), None)
    if not slot: raise HTTPException(404, "Slot not found")
    if slot["booked"]: raise HTTPException(409, "Booked slots cannot be deleted")
    db_service.save_slots([s for s in slots if s["id"] != slot_id])


def book(patient, payload):
    with BOOKING_LOCK:
        doctor = get_user_by_id(payload.doctor_id)
        if not doctor or doctor.get("role") != UserRole.DOCTOR.value or not doctor.get("active", True) or doctor.get("approval_status") != ApprovalStatus.APPROVED.value:
            raise HTTPException(404, "Doctor not found")
        slots = db_service.get_slots(); slot = next((s for s in slots if s["id"] == payload.slot_id and s["doctor_id"] == payload.doctor_id), None)
        if not slot: raise HTTPException(404, "Availability slot not found")
        if slot["booked"]: raise HTTPException(409, "Slot is already booked")
        if parse(slot["starts_at"]) <= utcnow(): raise HTTPException(400, "Past slots cannot be booked")
        slot["booked"] = True
        appointment = {"id": str(uuid4()), "patient_id": patient["id"], "doctor_id": payload.doctor_id, "slot_id": slot["id"], "starts_at": slot["starts_at"], "ends_at": slot["ends_at"], "status": AppointmentStatus.PENDING_PAYMENT.value, "payment_status": PaymentStatus.PENDING.value, "created_at": utcnow().isoformat()}
        appointments = db_service.get_appointments(); appointments.append(appointment)
        db_service.save_slots(slots); db_service.save_appointments(appointments)
        logger.info("Appointment reserved appointment_id=%s patient_id=%s doctor_id=%s slot_id=%s", appointment["id"], patient["id"], payload.doctor_id, slot["id"])
        return enrich(appointment)


def enrich(item):
    result = dict(item); doctor = get_user_by_id(item["doctor_id"]); patient = get_user_by_id(item["patient_id"])
    result["doctor"] = public_user(doctor) if doctor else None; result["patient"] = public_user(patient) if patient else None
    return result


def appointments_for(user, status=None):
    key = "patient_id" if user["role"] == UserRole.PATIENT.value else "doctor_id"
    items = [a for a in db_service.get_appointments() if a.get(key) == user["id"]]
    if status: items = [a for a in items if a["status"] == status]
    return [enrich(a) for a in sorted(items, key=lambda a: a["starts_at"])]


def pay(patient_id, appointment_id, method):
    appointments = db_service.get_appointments(); item = next((a for a in appointments if a["id"] == appointment_id and a["patient_id"] == patient_id), None)
    if not item: raise HTTPException(404, "Appointment not found")
    if item["payment_status"] == PaymentStatus.PAID.value: raise HTTPException(409, "Appointment is already paid")
    payment_method = method.value if hasattr(method, "value") else method
    payment = {"id": str(uuid4()), "appointment_id": appointment_id, "amount": get_user_by_id(item["doctor_id"]).get("consultation_fee", 0), "method": payment_method, "status": PaymentStatus.SUCCESS.value, "paid_at": utcnow().isoformat()}
    payments = db_service.get_payments(); payments.append(payment); item.update({"payment_status": PaymentStatus.PAID.value, "status": AppointmentStatus.BOOKED.value})
    db_service.save_payments(payments); db_service.save_appointments(appointments)
    logger.info("Payment completed appointment_id=%s payment_id=%s method=%s", appointment_id, payment["id"], method)
    return payment
