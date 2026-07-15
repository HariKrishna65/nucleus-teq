from fastapi import APIRouter, Depends, HTTPException

from backend.enums.user import UserRole
from backend.services.auth_service import require_role
from backend.services.user_service import request_doctor_profile_update, update_user
from backend.services import doctor_service
from backend.database import database as db_service
from backend.schemas.request.doctor import (
    SlotCreate,
    SlotUpdate,
    DoctorCancellationRequest,
    DoctorLeaveRequest,
    StatusUpdate,
    DoctorProfileUpdate,
)


router = APIRouter(tags=["doctor"])


@router.get("/doctor/slots")
def own_slots(user=Depends(require_role(UserRole.DOCTOR.value))):
    return [s for s in db_service.get_slots() if s["doctor_id"] == user["id"]]


@router.post("/doctor/slots", status_code=201)
def add_slot(payload: SlotCreate, user=Depends(require_role(UserRole.DOCTOR.value))):
    return doctor_service.create_slot(user["id"], payload)


@router.put("/doctor/slots/{slot_id}")
def edit_slot(slot_id: str, payload: SlotUpdate, user=Depends(require_role(UserRole.DOCTOR.value))):
    return doctor_service.update_slot(user["id"], slot_id, payload)


@router.delete("/doctor/slots/{slot_id}")
def remove_slot(slot_id: str, user=Depends(require_role(UserRole.DOCTOR.value))):
    doctor_service.delete_slot(user["id"], slot_id)
    return {"deleted": True}


@router.post("/doctor/appointments/{appointment_id}/cancel-request")
def request_doctor_cancel(appointment_id: str, payload: DoctorCancellationRequest, user=Depends(require_role(UserRole.DOCTOR.value))):
    return doctor_service.request_doctor_cancellation(user["id"], appointment_id, payload.reason)


@router.post("/doctor/leave-requests", status_code=201)
def request_doctor_leave(payload: DoctorLeaveRequest, user=Depends(require_role(UserRole.DOCTOR.value))):
    return doctor_service.request_leave(user["id"], payload)


@router.patch("/appointments/{appointment_id}/status")
def status(appointment_id: str, payload: StatusUpdate, user=Depends(require_role(UserRole.DOCTOR.value))):
    return doctor_service.set_status(user["id"], appointment_id, payload.status)


@router.patch("/doctor/profile")
def request_doctor_profile_change(payload: DoctorProfileUpdate, user=Depends(require_role(UserRole.DOCTOR.value))):
    doctor = request_doctor_profile_update(user["id"], payload.model_dump(exclude_unset=True))
    if not doctor:
        raise HTTPException(404, "Doctor profile not found")
    return {
        "message": "Doctor profile changes are pending admin approval",
        "status": doctor.get("profile_change_status"),
        "pending_profile_changes": doctor.get("pending_profile_changes") or {},
    }


@router.post("/doctor/profile/deactivate")
def deactivate_profile(user=Depends(require_role(UserRole.DOCTOR.value))):
    update_user(user["id"], {"active": False})
    return {"message": "Account deactivated successfully"}
