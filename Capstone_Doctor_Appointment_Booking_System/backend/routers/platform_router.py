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

router.add_api_route("/profile/me", platform_service.public_profile, methods=["GET"], tags=["profiles"])
router.add_api_route("/users/me", platform_service.public_profile, methods=["GET"], tags=["profiles"], include_in_schema=False)
