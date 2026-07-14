import logging
from datetime import datetime, timezone
from threading import Lock

from backend.enums.user import UserRole
from backend.database import database as db_service
from backend.services.user_service import get_user_by_id


BOOKING_LOCK = Lock()
logger = logging.getLogger("doctor_booking.shared")

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
    if not user:
        return None
    hidden_fields = {"hashed_password"}
    if user.get("role") != UserRole.DOCTOR.value:
        hidden_fields.update(DOCTOR_ONLY_PROFILE_FIELDS)
    return {key: value for key, value in user.items() if key not in hidden_fields}


def enrich(item):
    if not item:
        return None
    result = dict(item)
    doctor = get_user_by_id(item["doctor_id"])
    patient = get_user_by_id(item["patient_id"])
    result["doctor"] = public_user(doctor) if doctor else None
    result["patient"] = public_user(patient) if patient else None
    return result
