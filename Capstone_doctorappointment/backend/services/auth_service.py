from datetime import datetime, timezone

from fastapi import HTTPException, status

from backend.constants.roles import UserRole
from backend.constants.status import AccountStatus
from backend.models.user import User
from backend.schemas.request.auth_request import LoginRequest, UserRegistrationRequest
from backend.schemas.response.auth_response import TokenResponse, UserResponse
from backend.utils.security import (
    create_access_token,
    get_token_expiry_seconds,
    hash_password,
    verify_password,
)


def build_user_response(user: User) -> UserResponse:
    return UserResponse(
        id=str(user.id),
        full_name=user.full_name,
        email=user.email,
        phone_number=user.phone_number,
        role=user.role,
        status=user.status,
        gender=user.gender,
        date_of_birth=user.date_of_birth,
        qualification=user.qualification,
        specialization=user.specialization,
        experience=user.experience,
        license_number=user.license_number,
        consultation_fee=user.consultation_fee,
        clinic_address=user.clinic_address,
    )


async def register_user(request: UserRegistrationRequest) -> UserResponse:
    existing_user = await User.find_one(User.email == request.email)
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Email is already registered",
        )

    if request.role == UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Admin accounts cannot be created from public registration",
        )

    now = datetime.now(timezone.utc)
    user = User(
        full_name=request.full_name,
        email=request.email,
        hashed_password=hash_password(request.password),
        phone_number=request.phone_number,
        role=request.role,
        status=AccountStatus.ACTIVE,
        gender=request.gender,
        date_of_birth=request.date_of_birth,
        qualification=request.qualification,
        specialization=request.specialization,
        experience=request.experience,
        license_number=request.license_number,
        consultation_fee=request.consultation_fee,
        clinic_address=request.clinic_address,
        created_at=now,
        updated_at=now,
    )
    await user.insert()
    return build_user_response(user)


async def login_user(request: LoginRequest) -> TokenResponse:
    user = await User.find_one(User.email == request.email)
    if user is None or not verify_password(request.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
        )

    if user.status != AccountStatus.ACTIVE:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is inactive",
        )

    access_token = create_access_token(str(user.id), user.email, user.role.value)
    return TokenResponse(
        access_token=access_token,
        expires_in=get_token_expiry_seconds(),
        user=build_user_response(user),
    )
