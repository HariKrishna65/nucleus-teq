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
