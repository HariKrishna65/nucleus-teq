from typing import List

from fastapi import APIRouter, Depends, HTTPException

from backend.services.appointment_service import (
    DoctorProfileCreate,
    DoctorProfileOut,
    _read_doctor_profile,
    _store_doctor_profile,
    get_mongo_status,
    search_doctor_profiles,
    create_slot,
    list_slots,
    create_appointment,
    list_appointments_for_doctor,
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


# Slots endpoints
@router.post("/appointments/slots", status_code=201)
def add_slot(slot: dict, current_user: dict = Depends(require_role("DOCTOR"))):
    # doctors add available slots
    s = create_slot(slot)
    return s


@router.get("/appointments/slots")
def get_slots(doctor_id: str | None = None, date: str | None = None):
    return list_slots(doctor_id=doctor_id, date=date)


# Appointment endpoints
@router.post("/appointments/book", status_code=201)
def book_appointment(appt: dict, current_user: dict = Depends(require_role("PATIENT"))):
    appt["patient_email"] = current_user.get("email")
    created = create_appointment(appt)
    return created


@router.get("/appointments/doctor/{doctor_id}")
def appointments_for_doctor(doctor_id: str, current_user: dict = Depends(require_role("DOCTOR"))):
    return list_appointments_for_doctor(doctor_id)
