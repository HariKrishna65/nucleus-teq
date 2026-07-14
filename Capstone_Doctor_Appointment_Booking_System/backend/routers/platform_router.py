from fastapi import APIRouter, Depends, HTTPException

from backend.enums.appointment import AppointmentStatus
from backend.enums.user import ApprovalStatus, UserRole
from backend.database import database as db_service
from backend.schemas.request.platform import ActivationUpdate, AppointmentCreate, DoctorCancellationRequest, DoctorProfileUpdate, PatientProfileUpdate, PaymentCreate, SlotCreate, SlotUpdate, StatusUpdate
from backend.services import platform_service
from backend.services.auth_service import get_current_user, require_role
from backend.services.user_service import (
    PATIENT_PROFILE_UPDATE_FIELDS,
    accept_doctor_profile_update,
    get_user_by_id,
    reject_doctor_profile_update,
    request_doctor_profile_update,
    update_user,
)

router = APIRouter()

def current_profile(user=Depends(get_current_user)):
    return platform_service.public_user(user)

def update_patient_profile(payload: PatientProfileUpdate, user=Depends(require_role(UserRole.PATIENT.value))):
    return platform_service.public_user(update_user(user["id"], payload.model_dump(exclude_unset=True), PATIENT_PROFILE_UPDATE_FIELDS))

def request_doctor_profile_change(payload: DoctorProfileUpdate, user=Depends(require_role(UserRole.DOCTOR.value))):
    doctor = request_doctor_profile_update(user["id"], payload.model_dump(exclude_unset=True))
    return {
        "message": "Doctor profile changes are pending admin approval",
        "status": doctor.get("profile_change_status"),
        "pending_profile_changes": doctor.get("pending_profile_changes") or {},
    }

router.add_api_route("/profile/me", current_profile, methods=["GET"], tags=["profiles"])
router.add_api_route("/patient/profile/me", update_patient_profile, methods=["PATCH"], tags=["profiles"])
router.add_api_route("/doctor/profile/me", request_doctor_profile_change, methods=["PATCH"], tags=["profiles"])
router.add_api_route("/users/me", current_profile, methods=["GET"], tags=["profiles"], include_in_schema=False)

@router.get("/doctors", tags=["doctors"])
def doctors(specialization: str | None=None, location: str | None=None, min_experience: int | None=None, max_fee: float | None=None, available: bool=False, user=Depends(get_current_user)):
    return platform_service.doctor_list(specialization, location, min_experience, max_fee, available)

@router.get("/doctors/{doctor_id}", tags=["doctors"])
def doctor(doctor_id: str, user=Depends(get_current_user)):
    result = next((d for d in platform_service.doctor_list() if d["id"] == doctor_id), None)
    if not result: raise HTTPException(404, "Doctor not found")
    return result

@router.get("/doctor/slots", tags=["availability"])
def own_slots(user=Depends(require_role(UserRole.DOCTOR.value))): return [s for s in db_service.get_slots() if s["doctor_id"] == user["id"]]

@router.post("/doctor/slots", tags=["availability"], status_code=201)
def add_slot(payload: SlotCreate, user=Depends(require_role(UserRole.DOCTOR.value))): return platform_service.create_slot(user["id"], payload)

@router.put("/doctor/slots/{slot_id}", tags=["availability"])
def edit_slot(slot_id: str, payload: SlotUpdate, user=Depends(require_role(UserRole.DOCTOR.value))): return platform_service.update_slot(user["id"], slot_id, payload)

@router.delete("/doctor/slots/{slot_id}", tags=["availability"])
def remove_slot(slot_id: str, user=Depends(require_role(UserRole.DOCTOR.value))): platform_service.delete_slot(user["id"], slot_id); return {"deleted": True}

@router.post("/appointments", tags=["appointments"], status_code=201)
def book(payload: AppointmentCreate, user=Depends(require_role(UserRole.PATIENT.value))): return platform_service.book(user, payload)

@router.get("/appointments", tags=["appointments"])
def history(status: str | None=None, user=Depends(get_current_user)): return platform_service.appointments_for(user, status)

@router.post("/payments", tags=["payments"], status_code=201)
def payment(payload: PaymentCreate, user=Depends(require_role(UserRole.PATIENT.value))): return platform_service.pay(user["id"], payload.appointment_id, payload.method)

@router.post("/appointments/{appointment_id}/cancel", tags=["appointments"])
def cancel(appointment_id: str, user=Depends(require_role(UserRole.PATIENT.value))): return platform_service.cancel(user["id"], appointment_id)

@router.post("/doctor/appointments/{appointment_id}/cancel-request", tags=["appointments"])
def request_doctor_cancel(appointment_id: str, payload: DoctorCancellationRequest, user=Depends(require_role(UserRole.DOCTOR.value))):
    return platform_service.request_doctor_cancellation(user["id"], appointment_id, payload.reason)

@router.patch("/appointments/{appointment_id}/status", tags=["appointments"])
def status(appointment_id: str, payload: StatusUpdate, user=Depends(require_role(UserRole.DOCTOR.value))): return platform_service.set_status(user["id"], appointment_id, payload.status)

@router.get("/admin/statistics", tags=["admin"])
def statistics(user=Depends(require_role(UserRole.ADMIN.value))):
    users, appointments = db_service.get_users(), db_service.get_appointments()
    return {"total_doctors": sum(u.get("role")==UserRole.DOCTOR.value for u in users), "total_patients": sum(u.get("role")==UserRole.PATIENT.value for u in users), "total_appointments": len(appointments), "completed_appointments": sum(a["status"]==AppointmentStatus.COMPLETED.value for a in appointments), "cancelled_appointments": sum(a["status"]==AppointmentStatus.CANCELLED.value for a in appointments), "active_doctors": sum(u.get("role")==UserRole.DOCTOR.value and u.get("active", True) for u in users), "pending_doctors": sum(u.get("role")==UserRole.DOCTOR.value and u.get("approval_status")==ApprovalStatus.PENDING.value for u in users)}

@router.get("/admin/appointments", tags=["admin"])
def monitor(user=Depends(require_role(UserRole.ADMIN.value))): return [platform_service.enrich(a) for a in db_service.get_appointments()]

@router.get("/admin/appointments/cancellation-requests", tags=["admin"])
def cancellation_requests(user=Depends(require_role(UserRole.ADMIN.value))):
    return platform_service.pending_doctor_cancellations()

@router.patch("/admin/appointments/{appointment_id}/cancellation-request/accept", tags=["admin"])
def accept_cancellation_request(appointment_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    return platform_service.accept_doctor_cancellation(appointment_id)

@router.patch("/admin/appointments/{appointment_id}/cancellation-request/reject", tags=["admin"])
def reject_cancellation_request(appointment_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    return platform_service.reject_doctor_cancellation(appointment_id)

@router.get("/admin/doctors", tags=["admin"])
def admin_doctors(user=Depends(require_role(UserRole.ADMIN.value))):
    return platform_service.doctor_list(include_inactive=True)

@router.get("/admin/doctors/profile-changes", tags=["admin"])
def pending_doctor_profile_changes(user=Depends(require_role(UserRole.ADMIN.value))):
    doctors = [
        platform_service.public_user(doctor)
        for doctor in db_service.get_users()
        if doctor.get("role") == UserRole.DOCTOR.value and doctor.get("pending_profile_changes")
    ]
    return doctors

@router.patch("/admin/doctors/{doctor_id}/profile-change/accept", tags=["admin"])
def accept_doctor_profile_change(doctor_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = accept_doctor_profile_update(doctor_id)
    if not doctor:
        raise HTTPException(404, "Doctor not found")
    return platform_service.public_user(doctor)

@router.patch("/admin/doctors/{doctor_id}/profile-change/reject", tags=["admin"])
def reject_doctor_profile_change(doctor_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = reject_doctor_profile_update(doctor_id)
    if not doctor:
        raise HTTPException(404, "Doctor not found")
    return platform_service.public_user(doctor)

@router.patch("/admin/doctors/{doctor_id}/activation", tags=["admin"])
def activation(doctor_id: str, payload: ActivationUpdate, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = get_user_by_id(doctor_id)
    if not doctor or doctor["role"] != UserRole.DOCTOR.value: raise HTTPException(404, "Doctor not found")
    status = ApprovalStatus.APPROVED.value if payload.active else ApprovalStatus.SUSPENDED.value
    return platform_service.public_user(update_user(doctor_id, {"active": payload.active, "approval_status": status}))
