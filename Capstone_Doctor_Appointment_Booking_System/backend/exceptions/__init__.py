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
    ConflictException,
    UnprocessableEntityException,
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
    "ConflictException",
    "UnprocessableEntityException",
    "register_exception_handlers",
]
