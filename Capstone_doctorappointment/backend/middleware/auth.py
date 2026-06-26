from collections.abc import Callable

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from pydantic import ValidationError

from backend.constants.roles import UserRole
from backend.constants.status import AccountStatus
from backend.models.user import User
from backend.utils.security import decode_access_token


oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")


def build_credentials_exception(detail: str = "Could not validate credentials") -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"},
    )


async def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    try:
        payload = decode_access_token(token)
    except jwt.ExpiredSignatureError as exc:
        raise build_credentials_exception("Token expired") from exc
    except (jwt.InvalidTokenError, ValidationError) as exc:
        raise build_credentials_exception("Invalid token") from exc

    user = await User.get(payload.sub)
    if user is None:
        raise build_credentials_exception("User not found")
    if user.status != AccountStatus.ACTIVE:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is inactive",
        )
    if user.email != payload.email or user.role != payload.role:
        raise build_credentials_exception("Token does not match current user")
    return user


def require_roles(*allowed_roles: UserRole) -> Callable:
    async def role_checker(current_user: User = Depends(get_current_user)) -> User:
        if current_user.role not in allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You do not have permission to access this resource",
            )
        return current_user

    return role_checker
