from backend.exceptions.custom_exceptions import (
    AuthenticationCredentialsException,
    BadRequestException,
    DoctorNotFoundException,
    EmailAlreadyRegisteredException,
    ForbiddenException,
    InactiveAccountException,
    InvalidCredentialsException,
    NotFoundException,
    PendingApprovalException,
    UnauthorizedException,
)
from backend.exceptions.handlers import register_exception_handlers

__all__ = [
    "AuthenticationCredentialsException",
    "BadRequestException",
    "DoctorNotFoundException",
    "EmailAlreadyRegisteredException",
    "ForbiddenException",
    "InactiveAccountException",
    "InvalidCredentialsException",
    "NotFoundException",
    "PendingApprovalException",
    "UnauthorizedException",
    "register_exception_handlers",
]
