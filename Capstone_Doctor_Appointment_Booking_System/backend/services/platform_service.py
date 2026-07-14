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
