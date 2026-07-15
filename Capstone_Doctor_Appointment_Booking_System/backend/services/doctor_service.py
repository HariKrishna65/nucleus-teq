import logging
from datetime import timedelta
from uuid import uuid4

from backend.enums.appointment import CancellationRequestStatus
from backend.database import database as db_service
from backend.services.shared_service import utcnow, parse, enrich
from backend.constants import (
    SLOT_TIMEZONE_REQUIRED_MESSAGE,
    SLOT_FUTURE_TIME_RANGE_MESSAGE,
    SLOT_DURATION_LIMIT_EXCEEDED_MESSAGE,
    SLOT_CREATION_TIME_LIMIT_MESSAGE,
    SLOT_OVERLAPS_EXISTING_MESSAGE,
    SLOT_NOT_FOUND_MESSAGE,
    BOOKED_SLOTS_CANNOT_BE_EDITED_MESSAGE,
    BOOKED_SLOTS_CANNOT_BE_DELETED_MESSAGE,
    APPOINTMENT_NOT_FOUND_MESSAGE,
    APPOINTMENT_CANNOT_BE_CANCELLED_MESSAGE,
    CANCELLATION_TIME_LIMIT_EXCEEDED_MESSAGE,
    STATUS_UPDATE_AFTER_APPOINTMENT_MESSAGE,
    MAX_SLOT_DURATION_MINUTES,
    SLOT_CREATION_ADVANCE_HOURS,
    CANCELLATION_WINDOW_HOURS,
)
from backend.exceptions import (
    BadRequestException,
    ConflictException,
    NotFoundException,
    UnprocessableEntityException,
)


logger = logging.getLogger("doctor_booking.doctor_service")


def _validate_slot_window(starts_at, ends_at):
    if starts_at.tzinfo is None or ends_at.tzinfo is None:
        raise UnprocessableEntityException(SLOT_TIMEZONE_REQUIRED_MESSAGE)
    if starts_at <= utcnow() or ends_at <= starts_at:
        raise BadRequestException(SLOT_FUTURE_TIME_RANGE_MESSAGE)
    # Rule 1: Slot duration must be <= 30 minutes
    if ends_at - starts_at > timedelta(minutes=MAX_SLOT_DURATION_MINUTES):
        raise BadRequestException(SLOT_DURATION_LIMIT_EXCEEDED_MESSAGE)
    # Rule 2: Slot must be created at least 2 hours in advance of its start time
    if starts_at < utcnow() + timedelta(hours=SLOT_CREATION_ADVANCE_HOURS):
        raise BadRequestException(SLOT_CREATION_TIME_LIMIT_MESSAGE)


def create_slot(doctor_id, payload):
    _validate_slot_window(payload.starts_at, payload.ends_at)
    slots = db_service.get_slots()
    if any(s["doctor_id"] == doctor_id and parse(s["starts_at"]) < payload.ends_at and payload.starts_at < parse(s["ends_at"]) for s in slots):
        raise ConflictException(SLOT_OVERLAPS_EXISTING_MESSAGE)
    slot = {
        "id": str(uuid4()),
        "doctor_id": doctor_id,
        "starts_at": payload.starts_at.isoformat(),
        "ends_at": payload.ends_at.isoformat(),
        "booked": False
    }
    slots.append(slot)
    db_service.save_slots(slots)
    return slot


def update_slot(doctor_id, slot_id, payload):
    _validate_slot_window(payload.starts_at, payload.ends_at)
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == slot_id and s["doctor_id"] == doctor_id), None)
    if not slot:
        raise NotFoundException(SLOT_NOT_FOUND_MESSAGE)
    if slot["booked"]:
        raise ConflictException(BOOKED_SLOTS_CANNOT_BE_EDITED_MESSAGE)
    if any(
        s["id"] != slot_id and s["doctor_id"] == doctor_id and parse(s["starts_at"]) < payload.ends_at and payload.starts_at < parse(s["ends_at"])
        for s in slots
    ):
        raise ConflictException(SLOT_OVERLAPS_EXISTING_MESSAGE)
    slot.update({"starts_at": payload.starts_at.isoformat(), "ends_at": payload.ends_at.isoformat()})
    db_service.save_slots(slots)
    return slot


def delete_slot(doctor_id, slot_id):
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == slot_id and s["doctor_id"] == doctor_id), None)
    if not slot:
        raise NotFoundException(SLOT_NOT_FOUND_MESSAGE)
    if slot["booked"]:
        raise ConflictException(BOOKED_SLOTS_CANNOT_BE_EDITED_MESSAGE)
    db_service.save_slots([s for s in slots if s["id"] != slot_id])


def request_doctor_cancellation(doctor_id, appointment_id, reason):
    from backend.enums.appointment import AppointmentStatus
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["doctor_id"] == doctor_id), None)
    if not item:
        raise NotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE)
    if item["status"] in {AppointmentStatus.COMPLETED.value, AppointmentStatus.CANCELLED.value}:
        raise ConflictException(APPOINTMENT_CANNOT_BE_CANCELLED_MESSAGE)
    if parse(item["starts_at"]) - utcnow() < timedelta(hours=CANCELLATION_WINDOW_HOURS):
        raise BadRequestException(CANCELLATION_TIME_LIMIT_EXCEEDED_MESSAGE)
    item["doctor_cancellation_reason"] = reason.strip()
    item["doctor_cancellation_status"] = CancellationRequestStatus.PENDING.value
    item["doctor_cancellation_requested_at"] = utcnow().isoformat()
    db_service.save_appointments(appointments)
    logger.info("Doctor cancellation requested appointment_id=%s doctor_id=%s", appointment_id, doctor_id)
    return enrich(item)


def request_leave(doctor_id, payload):
    leave_date = payload.leave_date.isoformat()
    requests = db_service.get_leave_requests()
    if any(
        item["doctor_id"] == doctor_id
        and item["leave_date"] == leave_date
        and item["status"] == CancellationRequestStatus.PENDING.value
        for item in requests
    ):
        raise ConflictException("Leave request is already pending for this date")
    leave_request = {
        "id": str(uuid4()),
        "doctor_id": doctor_id,
        "leave_date": leave_date,
        "reason": payload.reason.strip(),
        "status": CancellationRequestStatus.PENDING.value,
        "requested_at": utcnow().isoformat(),
    }
    requests.append(leave_request)
    db_service.save_leave_requests(requests)
    logger.info("Doctor leave requested leave_request_id=%s doctor_id=%s leave_date=%s", leave_request["id"], doctor_id, leave_date)
    return leave_request


def set_status(doctor_id, appointment_id, status):
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["doctor_id"] == doctor_id), None)
    if not item:
        raise NotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE)
    if parse(item["starts_at"]) > utcnow():
        raise BadRequestException(STATUS_UPDATE_AFTER_APPOINTMENT_MESSAGE)
    item["status"] = status.value if hasattr(status, "value") else status
    db_service.save_appointments(appointments)
    return enrich(item)
