import logging
import os

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm

from backend.enums.user import UserRole, ApprovalStatus
from backend.exceptions import EmailAlreadyRegisteredException, InvalidCredentialsException
from backend.services.auth_service import assert_account_can_login, authenticate_user, create_access_token, get_current_user
from backend.services.user_service import create_user, get_user_by_email
from backend.schemas.request.user import AccountCreate, DoctorCreate, LoginRequest, PatientCreate
from backend.schemas.response.user import AccountResponse
from backend.services.shared_service import public_user


router = APIRouter(tags=["auth"], prefix="/auth")
profile_router = APIRouter(tags=["profiles"])
logger = logging.getLogger("doctor_booking.auth")


def register_account(payload: AccountCreate):
    existing = get_user_by_email(payload.email)
    if existing:
        raise EmailAlreadyRegisteredException()

    return create_user(payload)


router.add_api_route(
    "/register",
    register_account,
    methods=["POST"],
    response_model=AccountResponse,
    status_code=status.HTTP_201_CREATED,
    include_in_schema=False,
)


@router.post("/patient/register", response_model=AccountResponse, status_code=status.HTTP_201_CREATED)
def register_patient(payload: PatientCreate):
    data = payload.model_dump()
    return register_account(AccountCreate(**data, role=UserRole.PATIENT.value))


@router.post("/doctor/register", response_model=AccountResponse, status_code=status.HTTP_201_CREATED)
def register_doctor(payload: DoctorCreate):
    data = payload.model_dump()
    return register_account(AccountCreate(**data, role=UserRole.DOCTOR.value))


def _login_for_role(payload: LoginRequest, expected_role: str):
    user = authenticate_user(payload.email, payload.password)
    if not user:
        logger.warning("Login failed email=%s role=%s", payload.email, expected_role)
        raise InvalidCredentialsException()
    if user["role"] != expected_role:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"Please use the {user['role'].lower()} login API")
    assert_account_can_login(user)
    access_token = create_access_token(data={"sub": user["id"], "email": user["email"], "role": user["role"]})
    logger.info("Login succeeded user_id=%s role=%s", user["id"], user["role"])
    return {"access_token": access_token, "token_type": os.getenv("TOKEN_TYPE_BEARER", "bearer")}


@router.post("/patient/login")
def patient_login(payload: LoginRequest):
    return _login_for_role(payload, UserRole.PATIENT.value)


@router.post("/doctor/login")
def doctor_login(payload: LoginRequest):
    return _login_for_role(payload, UserRole.DOCTOR.value)


@router.post("/admin/login")
def admin_login(payload: LoginRequest):
    return _login_for_role(payload, UserRole.ADMIN.value)


@router.post("/token")
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    user = authenticate_user(form_data.username, form_data.password)
    if not user:
        logger.warning("Login failed email=%s", form_data.username)
        raise InvalidCredentialsException()
    assert_account_can_login(user)

    access_token = create_access_token(data={"sub": user["id"], "email": user["email"], "role": user["role"]})
    logger.info("Login succeeded user_id=%s role=%s", user["id"], user["role"])
    token_response = {"access_token": access_token, "token_type": os.getenv("TOKEN_TYPE_BEARER", "bearer")}
    return token_response


@router.post("/doctor/request-activation")
def request_activation(payload: LoginRequest):
    user = authenticate_user(payload.email, payload.password)
    if not user:
        raise InvalidCredentialsException()
    if user["role"] != UserRole.DOCTOR.value:
        raise HTTPException(status_code=403, detail="Only doctor accounts can request activation")
    if user.get("active", True):
        raise HTTPException(status_code=400, detail="Account is already active")
    
    from backend.database import database as db_service
    users = db_service.get_users()
    for u in users:
        if u["id"] == user["id"]:
            u["approval_status"] = ApprovalStatus.PENDING.value
            break
    db_service.save_users(users)
    return {"message": "Reactivation request submitted. Pending admin approval."}


@profile_router.get("/profile/me")
def current_profile(user=Depends(get_current_user)):
    return public_user(user)


@profile_router.get("/users/me", include_in_schema=False)
def current_profile_alias(user=Depends(get_current_user)):
    return public_user(user)
