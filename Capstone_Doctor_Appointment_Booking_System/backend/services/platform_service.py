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
