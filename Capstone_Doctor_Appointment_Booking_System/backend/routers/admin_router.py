from fastapi import APIRouter, Depends, HTTPException

from backend.enums.user import ApprovalStatus, UserRole
from backend.enums.appointment import AppointmentStatus
from backend.services.auth_service import require_role
from backend.services.user_service import (
    accept_doctor_profile_update,
    get_user_by_id,
    reject_doctor_profile_update,
    update_user,
)
from backend.services import admin_service
from backend.services.shared_service import public_user, enrich
from backend.database import database as db_service
from backend.schemas.request.admin import ActivationUpdate


router = APIRouter(tags=["admin"])


@router.get("/admin/statistics")
def statistics(user=Depends(require_role(UserRole.ADMIN.value))):
    users = db_service.get_users()
    appointments = db_service.get_appointments()
    return {
        "total_doctors": sum(u.get("role") == UserRole.DOCTOR.value for u in users),
        "total_patients": sum(u.get("role") == UserRole.PATIENT.value for u in users),
        "total_appointments": len(appointments),
        "completed_appointments": sum(a["status"] == AppointmentStatus.COMPLETED.value for a in appointments),
        "cancelled_appointments": sum(a["status"] == AppointmentStatus.CANCELLED.value for a in appointments),
        "active_doctors": sum(u.get("role") == UserRole.DOCTOR.value and u.get("active", True) for u in users),
        "pending_doctors": sum(u.get("role") == UserRole.DOCTOR.value and u.get("approval_status") == ApprovalStatus.PENDING.value for u in users),
    }


@router.get("/admin/appointments")
def monitor(user=Depends(require_role(UserRole.ADMIN.value))):
    return [enrich(a) for a in db_service.get_appointments()]


@router.get("/admin/appointments/cancellation-requests")
def cancellation_requests(user=Depends(require_role(UserRole.ADMIN.value))):
    return admin_service.pending_doctor_cancellations()


@router.patch("/admin/appointments/{appointment_id}/cancellation-request/accept")
def accept_cancellation_request(appointment_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    return admin_service.accept_doctor_cancellation(appointment_id)


@router.patch("/admin/appointments/{appointment_id}/cancellation-request/reject")
def reject_cancellation_request(appointment_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    return admin_service.reject_doctor_cancellation(appointment_id)


@router.get("/admin/doctors")
def admin_doctors(user=Depends(require_role(UserRole.ADMIN.value))):
    from backend.services import patient_service
    return patient_service.doctor_list(include_inactive=True)


@router.get("/admin/doctors/profile-changes")
def pending_doctor_profile_changes(user=Depends(require_role(UserRole.ADMIN.value))):
    doctors = [
        public_user(doctor)
        for doctor in db_service.get_users()
        if doctor.get("role") == UserRole.DOCTOR.value and doctor.get("pending_profile_changes")
    ]
    return doctors


@router.patch("/admin/doctors/{doctor_id}/profile-change/accept")
def accept_doctor_profile_change(doctor_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = accept_doctor_profile_update(doctor_id)
    if not doctor:
        raise HTTPException(404, "Doctor not found")
    return public_user(doctor)


@router.patch("/admin/doctors/{doctor_id}/profile-change/reject")
def reject_doctor_profile_change(doctor_id: str, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = reject_doctor_profile_update(doctor_id)
    if not doctor:
        raise HTTPException(404, "Doctor not found")
    return public_user(doctor)


@router.patch("/admin/doctors/{doctor_id}/activation")
def activation(doctor_id: str, payload: ActivationUpdate, user=Depends(require_role(UserRole.ADMIN.value))):
    doctor = get_user_by_id(doctor_id)
    if not doctor or doctor["role"] != UserRole.DOCTOR.value:
        raise HTTPException(404, "Doctor not found")
    status = ApprovalStatus.APPROVED.value if payload.active else ApprovalStatus.SUSPENDED.value
    return public_user(update_user(doctor_id, {"active": payload.active, "approval_status": status}))


@router.get("/admin/doctor-profiles")
def list_doctor_profiles(current_user: dict = Depends(require_role(UserRole.ADMIN.value))):
    profiles = admin_service.search_doctor_profiles()
    return profiles or []


@router.delete("/admin/doctors/{doctor_id}")
def delete_doctor_profile(doctor_id: str, current_user: dict = Depends(require_role(UserRole.ADMIN.value))):
    deleted = admin_service._delete_doctor_profile(doctor_id)
    if not deleted:
        raise HTTPException(404, "Doctor profile not found")
    return {"deleted": True}
