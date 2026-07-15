import logging

from backend.enums.appointment import AppointmentStatus, CancellationRequestStatus
from backend.database import database as db_service
from backend.services.shared_service import utcnow, enrich, get_user_by_id, parse
from backend.constants import (
    APPOINTMENT_NOT_FOUND_MESSAGE,
    NO_PENDING_CANCELLATION_MESSAGE,
    APPOINTMENT_CANNOT_BE_CANCELLED_MESSAGE,
)
from backend.exceptions import (
    BadRequestException,
    ConflictException,
    NotFoundException,
)


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
        raise NotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE)
    if item.get("doctor_cancellation_status") != CancellationRequestStatus.PENDING.value:
        raise BadRequestException(NO_PENDING_CANCELLATION_MESSAGE)
    if item["status"] in {AppointmentStatus.COMPLETED.value, AppointmentStatus.CANCELLED.value}:
        raise ConflictException(APPOINTMENT_CANNOT_BE_CANCELLED_MESSAGE)
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
        raise NotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE)
    if item.get("doctor_cancellation_status") != CancellationRequestStatus.PENDING.value:
        raise BadRequestException(NO_PENDING_CANCELLATION_MESSAGE)
    item["doctor_cancellation_status"] = CancellationRequestStatus.REJECTED.value
    item["doctor_cancellation_reviewed_at"] = utcnow().isoformat()
    db_service.save_appointments(appointments)
    logger.info("Doctor cancellation rejected appointment_id=%s", appointment_id)
    return enrich(item)


def _enrich_leave_request(item):
    enriched = item.copy()
    enriched["doctor"] = get_user_by_id(item["doctor_id"])
    return enriched


def pending_doctor_leave_requests():
    return [
        _enrich_leave_request(item)
        for item in db_service.get_leave_requests()
        if item.get("status") == CancellationRequestStatus.PENDING.value
    ]


def accept_doctor_leave_request(request_id):
    requests = db_service.get_leave_requests()
    leave_request = next((item for item in requests if item["id"] == request_id), None)
    if not leave_request:
        raise NotFoundException("Leave request not found")
    if leave_request.get("status") != CancellationRequestStatus.PENDING.value:
        raise BadRequestException("Leave request is not pending")

    slots = db_service.get_slots()
    affected_slots = [
        slot
        for slot in slots
        if slot["doctor_id"] == leave_request["doctor_id"]
        and parse(slot["starts_at"]).date().isoformat() == leave_request["leave_date"]
    ]
    affected_slot_ids = {slot["id"] for slot in affected_slots}

    appointments = db_service.get_appointments()
    cancelled_appointments = 0
    for appointment in appointments:
        if appointment.get("slot_id") in affected_slot_ids and appointment["status"] not in {
            AppointmentStatus.COMPLETED.value,
            AppointmentStatus.CANCELLED.value,
        }:
            appointment["status"] = AppointmentStatus.CANCELLED.value
            appointment["doctor_leave_request_id"] = request_id
            appointment["doctor_leave_reason"] = leave_request["reason"]
            cancelled_appointments += 1

    db_service.save_slots([slot for slot in slots if slot["id"] not in affected_slot_ids])
    db_service.save_appointments(appointments)
    leave_request["status"] = CancellationRequestStatus.APPROVED.value
    leave_request["reviewed_at"] = utcnow().isoformat()
    leave_request["cancelled_slots_count"] = len(affected_slots)
    leave_request["cancelled_appointments_count"] = cancelled_appointments
    db_service.save_leave_requests(requests)
    logger.info("Doctor leave approved leave_request_id=%s", request_id)
    return _enrich_leave_request(leave_request)


def reject_doctor_leave_request(request_id):
    requests = db_service.get_leave_requests()
    leave_request = next((item for item in requests if item["id"] == request_id), None)
    if not leave_request:
        raise NotFoundException("Leave request not found")
    if leave_request.get("status") != CancellationRequestStatus.PENDING.value:
        raise BadRequestException("Leave request is not pending")
    leave_request["status"] = CancellationRequestStatus.REJECTED.value
    leave_request["reviewed_at"] = utcnow().isoformat()
    db_service.save_leave_requests(requests)
    logger.info("Doctor leave rejected leave_request_id=%s", request_id)
    return _enrich_leave_request(leave_request)


def search_doctor_profiles() -> list[dict]:
    return db_service.get_doctors()


def _delete_doctor_profile(doctor_id: str) -> bool:
    doctors = db_service.get_doctors()
    remaining_doctors = [doctor for doctor in doctors if doctor.get("id") != doctor_id]
    if len(remaining_doctors) == len(doctors):
        return False
    db_service.save_doctors(remaining_doctors)
    return True
