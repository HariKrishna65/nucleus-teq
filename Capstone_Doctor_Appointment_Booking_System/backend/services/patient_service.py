import logging
from datetime import timedelta
from uuid import uuid4

from fastapi import HTTPException

from backend.enums.appointment import AppointmentStatus, PaymentStatus
from backend.enums.user import ApprovalStatus, UserRole
from backend.database import database as db_service
from backend.services.user_service import get_user_by_id
from backend.services.shared_service import BOOKING_LOCK, utcnow, parse, enrich, public_user


logger = logging.getLogger("doctor_booking.patient_service")


def doctor_list(specialization=None, location=None, min_experience=None, max_fee=None, available=None, include_inactive=False):
    doctors = [
        public_user(user)
        for user in db_service.get_users()
        if user.get("role") == UserRole.DOCTOR.value and (include_inactive or user.get("active", True))
    ]
    if specialization:
        doctors = [d for d in doctors if specialization.lower() in d.get("specialization", "").lower()]
    if location:
        doctors = [d for d in doctors if location.lower() in d.get("clinic_address", "").lower()]
    if min_experience is not None:
        doctors = [d for d in doctors if (d.get("experience") or 0) >= min_experience]
    if max_fee is not None:
        doctors = [d for d in doctors if (d.get("consultation_fee") or 0) <= max_fee]
    slots = db_service.get_slots()
    for doctor in doctors:
        doctor["available_slots"] = [s for s in slots if s["doctor_id"] == doctor["id"] and not s["booked"] and parse(s["starts_at"]) > utcnow()]
    if available:
        doctors = [d for d in doctors if d["available_slots"]]
    return doctors


def book(patient, payload):
    with BOOKING_LOCK:
        doctor = get_user_by_id(payload.doctor_id)
        if not doctor or doctor.get("role") != UserRole.DOCTOR.value or not doctor.get("active", True) or doctor.get("approval_status") != ApprovalStatus.APPROVED.value:
            raise HTTPException(404, "Doctor not found")
        slots = db_service.get_slots()
        slot = next((s for s in slots if s["id"] == payload.slot_id and s["doctor_id"] == payload.doctor_id), None)
        if not slot:
            raise HTTPException(404, "Availability slot not found")
        if slot["booked"]:
            raise HTTPException(409, "Slot is already booked")
        if parse(slot["starts_at"]) <= utcnow():
            raise HTTPException(400, "Past slots cannot be booked")
        slot["booked"] = True
        appointment = {
            "id": str(uuid4()),
            "patient_id": patient["id"],
            "doctor_id": payload.doctor_id,
            "slot_id": slot["id"],
            "starts_at": slot["starts_at"],
            "ends_at": slot["ends_at"],
            "status": AppointmentStatus.PENDING_PAYMENT.value,
            "payment_status": PaymentStatus.PENDING.value,
            "created_at": utcnow().isoformat()
        }
        appointments = db_service.get_appointments()
        appointments.append(appointment)
        db_service.save_slots(slots)
        db_service.save_appointments(appointments)
        logger.info("Appointment reserved appointment_id=%s patient_id=%s doctor_id=%s slot_id=%s", appointment["id"], patient["id"], payload.doctor_id, slot["id"])
        return enrich(appointment)


def pay(patient_id, appointment_id, method):
    from backend.enums.appointment import PaymentMethod
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["patient_id"] == patient_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if item["payment_status"] == PaymentStatus.PAID.value:
        raise HTTPException(409, "Appointment is already paid")
    payment_method = method.value if hasattr(method, "value") else method
    payment = {
        "id": str(uuid4()),
        "appointment_id": appointment_id,
        "amount": get_user_by_id(item["doctor_id"]).get("consultation_fee", 0),
        "method": payment_method,
        "status": PaymentStatus.SUCCESS.value,
        "paid_at": utcnow().isoformat()
    }
    payments = db_service.get_payments()
    payments.append(payment)
    item.update({"payment_status": PaymentStatus.PAID.value, "status": AppointmentStatus.BOOKED.value})
    db_service.save_payments(payments)
    db_service.save_appointments(appointments)
    logger.info("Payment completed appointment_id=%s payment_id=%s method=%s", appointment_id, payment["id"], method)
    return payment


def cancel(patient_id, appointment_id):
    appointments = db_service.get_appointments()
    item = next((a for a in appointments if a["id"] == appointment_id and a["patient_id"] == patient_id), None)
    if not item:
        raise HTTPException(404, "Appointment not found")
    if item["status"] in {AppointmentStatus.COMPLETED.value, AppointmentStatus.CANCELLED.value}:
        raise HTTPException(409, "Appointment cannot be cancelled")
    if parse(item["starts_at"]) - utcnow() < timedelta(hours=2):
        raise HTTPException(400, "Cancellation is allowed only up to two hours before the appointment")
    item["status"] = AppointmentStatus.CANCELLED.value
    slots = db_service.get_slots()
    slot = next((s for s in slots if s["id"] == item["slot_id"]), None)
    if slot:
        slot["booked"] = False
    db_service.save_appointments(appointments)
    db_service.save_slots(slots)
    logger.info("Appointment cancelled appointment_id=%s patient_id=%s", appointment_id, patient_id)
    return enrich(item)
