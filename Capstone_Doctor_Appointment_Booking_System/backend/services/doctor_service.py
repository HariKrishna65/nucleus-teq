import logging
from datetime import timedelta
from uuid import uuid4

from fastapi import HTTPException

from backend.database import database as db_service
from backend.services.shared_service import utcnow, parse, enrich


logger = logging.getLogger("doctor_booking.doctor_service")


def _validate_slot_window(starts_at, ends_at):
    if starts_at.tzinfo is None or ends_at.tzinfo is None:
        raise HTTPException(422, "Slot times must include a timezone")
    if starts_at <= utcnow() or ends_at <= starts_at:
        raise HTTPException(400, "Slot must be a valid future time range")
    # Rule 1: Slot duration must be <= 30 minutes
    if ends_at - starts_at > timedelta(minutes=30):
        raise HTTPException(400, "Slot duration must be less than or equal to 30 minutes")
    # Rule 2: Slot must be created at least 2 hours in advance of its start time
    if starts_at < utcnow() + timedelta(hours=2):
        raise HTTPException(400, "Slot must be created at least 2 hours in advance of its start time")


def create_slot(doctor_id, payload):
    _validate_slot_window(payload.starts_at, payload.ends_at)
    slots = db_service.get_slots()
    if any(s["doctor_id"] == doctor_id and parse(s["starts_at"]) < payload.ends_at and payload.starts_at < parse(s["ends_at"]) for s in slots):
        raise HTTPException(409, "Slot overlaps existing availability")
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
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == slot_id and s["doctor_id"] == doctor_id), None)
    if not slot:
        raise HTTPException(404, "Slot not found")
    if slot["booked"]:
        raise HTTPException(409, "Booked slots cannot be deleted")
    db_service.save_slots([s for s in slots if s["id"] != slot_id])


def request_doctor_cancellation(doctor_id, appointment_id, reason):
    from backend.enums.appointment import AppointmentStatus, CancellationRequestStatus
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["doctor_id"] == doctor_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if item["status"] in {AppointmentStatus.COMPLETED.value, AppointmentStatus.CANCELLED.value}:
        raise HTTPException(409, "Appointment cannot be cancelled")
    if parse(item["starts_at"]) - utcnow() < timedelta(hours=2):
        raise HTTPException(400, "Cancellation is allowed only up to two hours before the appointment")
    item["doctor_cancellation_reason"] = reason.strip()
    item["doctor_cancellation_status"] = CancellationRequestStatus.PENDING.value
    item["doctor_cancellation_requested_at"] = utcnow().isoformat()
    db_service.save_appointments(appointments)
    logger.info("Doctor cancellation requested appointment_id=%s doctor_id=%s", appointment_id, doctor_id)
    return enrich(item)


def set_status(doctor_id, appointment_id, status):
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["doctor_id"] == doctor_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if parse(item["starts_at"]) > utcnow():
        raise HTTPException(400, "Status can be updated only after appointment time")
    item["status"] = status.value if hasattr(status, "value") else status
    db_service.save_appointments(appointments)
    return enrich(item)
