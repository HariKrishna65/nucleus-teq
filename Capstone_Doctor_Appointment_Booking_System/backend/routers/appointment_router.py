from typing import List

from fastapi import APIRouter, Depends, HTTPException

from backend.services.appointment_service import (
    DoctorProfileCreate,
    DoctorProfileOut,
    _read_doctor_profile,
    _store_doctor_profile,
    get_mongo_status,
    search_doctor_profiles,
)
from backend.services.appointment_service import (
    AppointmentCreate,
    AppointmentOut,
    create_appointment,
    get_appointment_by_id,
    update_appointment_status,
)
from backend.services.auth_service import require_role

router = APIRouter(tags=["appointments"])


@router.get("/health")
def health_check():
    return {"service": "appointment-service", "status": "ok", "database": get_mongo_status()}


@router.get("/appointments/doctor-dashboard")
def doctor_dashboard(current_user: dict = Depends(require_role("DOCTOR"))):
    return {"message": "Doctor appointment dashboard access granted", "role": current_user["role"]}


@router.get("/appointments/admin-dashboard")
def admin_dashboard(current_user: dict = Depends(require_role("ADMIN"))):
    return {"message": "Admin appointment dashboard access granted", "role": current_user["role"]}


@router.post("/doctors/profile", response_model=DoctorProfileOut)
def create_doctor_profile(profile: DoctorProfileCreate, current_user: dict = Depends(require_role("DOCTOR"))):
    if current_user["email"] != profile.doctor_id:
        raise HTTPException(status_code=403, detail="You can only manage your own profile")

    profile_data = profile.dict()
    _store_doctor_profile(profile_data)
    return profile_data


@router.get("/doctors/profile", response_model=DoctorProfileOut)
def get_doctor_profile(current_user: dict = Depends(require_role("DOCTOR"))):
    profile = _read_doctor_profile(current_user["email"])
    if not profile:
        raise HTTPException(status_code=404, detail="Doctor profile not found")
    return profile


@router.get("/doctors/search", response_model=List[DoctorProfileOut])
def search_doctors(
    q: str | None = None,
    specialization: str | None = None,
    clinic_address: str | None = None,
):
    """Search doctors by email, specialization, qualification, or clinic address."""
    results = search_doctor_profiles(q=q, specialization=specialization, clinic_address=clinic_address)
    if not results:
        return []
    return results




# --- Appointments and Mock Payments ---


@router.post("/appointments/create", response_model=dict)
def api_create_appointment(payload: dict):
    created = create_appointment(payload)
    return {"appointment_id": created["appointment_id"], "amount": created.get("amount")}


@router.get("/appointments/{appointment_id}")
def api_get_appointment(appointment_id: str):
    appt = get_appointment_by_id(appointment_id)
    if not appt:
        raise HTTPException(status_code=404, detail="Appointment not found")
    return appt


@router.post("/payments/initiate")
def api_initiate_payment(appointment_id: str):
    appt = get_appointment_by_id(appointment_id)
    if not appt:
        raise HTTPException(status_code=404, detail="Appointment not found")
    # Return a mock payment intent id and the amount
    payment_intent_id = f"pi_{appointment_id[:8]}"
    return {"payment_intent_id": payment_intent_id, "amount": appt.get("amount")}


@router.post("/payments/confirm")
def api_confirm_payment(appointment_id: str, transaction_id: str | None = None):
    appt = get_appointment_by_id(appointment_id)
    if not appt:
        raise HTTPException(status_code=404, detail="Appointment not found")

    tx = {"transaction_id": transaction_id or f"tx_{appointment_id[:8]}", "amount": appt.get("amount"), "status": "SUCCESS"}
    updated = update_appointment_status(appointment_id, "PAID", transaction=tx)
    return {"appointment": updated, "transaction": tx}
