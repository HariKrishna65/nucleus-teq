import logging
import os

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm

from backend.enums.user import UserRole
from backend.exceptions import EmailAlreadyRegisteredException, InvalidCredentialsException
from backend.services.auth_service import assert_account_can_login, authenticate_user, create_access_token
from backend.services.user_service import create_user, get_user_by_email
from backend.schemas.request.user import AccountCreate, DoctorCreate, LoginRequest, PatientCreate
from backend.schemas.response.user import AccountResponse

router = APIRouter(tags=["auth"], prefix="/auth")
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
