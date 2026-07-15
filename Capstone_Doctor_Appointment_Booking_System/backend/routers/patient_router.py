from fastapi import APIRouter, Depends, HTTPException

from backend.enums.user import UserRole
from backend.services.auth_service import get_current_user, require_role
from backend.services.user_service import PATIENT_PROFILE_UPDATE_FIELDS, update_user
from backend.services import patient_service
from backend.services.shared_service import public_user, enrich
from backend.database import database as db_service
from backend.schemas.request.patient import (
    PatientProfileUpdate,
    AppointmentCreate,
    PaymentCreate,
)


router = APIRouter()


@router.patch("/patient/profile", tags=["profiles"])
def update_patient_profile(payload: PatientProfileUpdate, user=Depends(require_role(UserRole.PATIENT.value))):
    return public_user(update_user(user["id"], payload.model_dump(exclude_unset=True), PATIENT_PROFILE_UPDATE_FIELDS))


@router.get("/doctors", tags=["doctors"])
def doctors(specialization: str | None = None, location: str | None = None, min_experience: int | None = None, max_fee: float | None = None, available: bool = False, user=Depends(get_current_user)):
    return patient_service.doctor_list(specialization, location, min_experience, max_fee, available)


@router.get("/doctors/{doctor_id}", tags=["doctors"])
def doctor(doctor_id: str, user=Depends(get_current_user)):
    result = next((d for d in patient_service.doctor_list() if d["id"] == doctor_id), None)
    if not result:
        raise HTTPException(404, "Doctor not found")
    return result


@router.post("/appointments", tags=["appointments"], status_code=201)
def book(payload: AppointmentCreate, user=Depends(require_role(UserRole.PATIENT.value))):
    return patient_service.book(user, payload)


@router.get("/appointments", tags=["appointments"])
def history(status: str | None = None, user=Depends(get_current_user)):
    # Imports here to avoid circular dependencies
    from backend.services.shared_service import enrich
    key = "patient_id" if user["role"] == UserRole.PATIENT.value else "doctor_id"
    items = [a for a in db_service.get_appointments() if a.get(key) == user["id"]]
    if status:
        items = [a for a in items if a["status"] == status]
    return [enrich(a) for a in sorted(items, key=lambda a: a["starts_at"])]


@router.post("/payments", tags=["payments"], status_code=201)
def payment(payload: PaymentCreate, user=Depends(require_role(UserRole.PATIENT.value))):
    return patient_service.pay(user["id"], payload.appointment_id, payload.method)


@router.post("/appointments/{appointment_id}/cancel", tags=["appointments"])
def cancel(appointment_id: str, user=Depends(require_role(UserRole.PATIENT.value))):
    return patient_service.cancel(user["id"], appointment_id)
