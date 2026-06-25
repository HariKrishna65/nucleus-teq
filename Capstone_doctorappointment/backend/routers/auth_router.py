from fastapi import APIRouter, Depends, status

from backend.middleware.auth import get_current_user
from backend.models.user import User
from backend.schemas.request.auth_request import LoginRequest, UserRegistrationRequest
from backend.schemas.response.auth_response import TokenResponse, UserResponse
from backend.services.auth_service import build_user_response, login_user, register_user


router = APIRouter(prefix="/api/auth", tags=["Authentication"])


@router.post(
    "/register",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
)
async def register(request: UserRegistrationRequest) -> UserResponse:
    return await register_user(request)


@router.post("/login", response_model=TokenResponse)
async def login(request: LoginRequest) -> TokenResponse:
    return await login_user(request)


@router.get("/me", response_model=UserResponse)
async def get_profile(current_user: User = Depends(get_current_user)) -> UserResponse:
    return build_user_response(current_user)
