from fastapi import APIRouter, Depends

from backend.enums.user import UserRole
from backend.exceptions import NotFoundException
from backend.services.appointment_service import (
    search_doctor_profiles,
    _delete_doctor_profile,
)
from backend.services.auth_service import require_role

router = APIRouter(tags=["admin"], prefix="/admin")


@router.get("/doctor-profiles")
def list_doctor_profiles(current_user: dict = Depends(require_role(UserRole.ADMIN.value))):
    # Return all doctor profiles
    profiles = search_doctor_profiles()
    return profiles or []


@router.delete("/doctors/{doctor_id}")
def delete_doctor_profile(doctor_id: str, current_user: dict = Depends(require_role(UserRole.ADMIN.value))):
    deleted = _delete_doctor_profile(doctor_id)
    if not deleted:
        raise NotFoundException("Doctor profile not found")
    return {"deleted": True}
