import logging

from fastapi import HTTPException

from backend.enums.appointment import AppointmentStatus, CancellationRequestStatus
from backend.database import database as db_service
from backend.services.shared_service import utcnow, enrich, get_user_by_id


logger = logging.getLogger("doctor_booking.admin_service")


def pending_doctor_cancellations():
    return [
        enrich(appointment)
        for appointment in db_service.get_appointments()
        if appointment.get("doctor_cancellation_status") == CancellationRequestStatus.PENDING.value
    ]


def accept_doctor_cancellation(appointment_id):
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if item.get("doctor_cancellation_status") != CancellationRequestStatus.PENDING.value:
        raise HTTPException(400, "No pending doctor cancellation request")
    if item["status"] in {AppointmentStatus.COMPLETED.value, AppointmentStatus.CANCELLED.value}:
        raise HTTPException(409, "Appointment cannot be cancelled")
    item["status"] = AppointmentStatus.CANCELLED.value
    item["doctor_cancellation_status"] = CancellationRequestStatus.APPROVED.value
    item["doctor_cancellation_reviewed_at"] = utcnow().isoformat()
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == item["slot_id"]), None)
    if slot:
        slot["booked"] = False
    db_service.save_appointments(appointments)
    db_service.save_slots(slots)
    logger.info("Doctor cancellation approved appointment_id=%s", appointment_id)
    return enrich(item)


def reject_doctor_cancellation(appointment_id):
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if item.get("doctor_cancellation_status") != CancellationRequestStatus.PENDING.value:
        raise HTTPException(400, "No pending doctor cancellation request")
    item["doctor_cancellation_status"] = CancellationRequestStatus.REJECTED.value
    item["doctor_cancellation_reviewed_at"] = utcnow().isoformat()
    db_service.save_appointments(appointments)
    logger.info("Doctor cancellation rejected appointment_id=%s", appointment_id)
    return enrich(item)


def search_doctor_profiles() -> list[dict]:
    return db_service.get_doctors()


def _delete_doctor_profile(doctor_id: str) -> bool:
    doctors = db_service.get_doctors()
    remaining_doctors = [doctor for doctor in doctors if doctor.get("id") != doctor_id]
    if len(remaining_doctors) == len(doctors):
        return False
    db_service.save_doctors(remaining_doctors)
    return True
