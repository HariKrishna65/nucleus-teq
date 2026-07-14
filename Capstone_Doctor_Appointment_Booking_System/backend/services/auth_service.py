from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt

from backend.config import ACCESS_TOKEN_EXPIRE_MINUTES, ALGORITHM, SECRET_KEY
from backend.enums.user import ApprovalStatus, UserRole
from backend.exceptions import AuthenticationCredentialsException, ForbiddenException, InactiveAccountException, PendingApprovalException
from backend.services.user_service import get_user_by_id, get_user_by_email, verify_password

bearer_scheme = HTTPBearer(
    bearerFormat="JWT",
    description="Paste the access_token returned by the patient, doctor, or admin login API.",
)


def authenticate_user(email: str, password: str) -> dict | None:
    user = get_user_by_email(email)
    if not user or not verify_password(password, user["hashed_password"]):
        return None
    return user


def assert_account_can_login(user: dict) -> None:
    if user.get("role") == UserRole.DOCTOR.value and user.get("approval_status") == ApprovalStatus.PENDING.value:
        raise PendingApprovalException()
    if not user.get("active", True):
        raise InactiveAccountException()


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    now = datetime.now(timezone.utc)
    expire = now + (expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"iat": now, "exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme)) -> dict:
    try:
        token = credentials.credentials
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise AuthenticationCredentialsException()
    except JWTError:
        raise AuthenticationCredentialsException()

    user = get_user_by_id(user_id)
    if not user:
        raise AuthenticationCredentialsException()
    assert_account_can_login(user)
    return user
