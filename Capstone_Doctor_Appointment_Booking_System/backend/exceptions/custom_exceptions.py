from fastapi import HTTPException, status

from backend.constants.messages import (
    ACCOUNT_INACTIVE_MESSAGE,
    ACCOUNT_PENDING_APPROVAL_MESSAGE,
    AUTHENTICATION_ERROR_MESSAGE,
    DOCTOR_NOT_FOUND_MESSAGE,
    EMAIL_ALREADY_REGISTERED_MESSAGE,
    INCORRECT_EMAIL_OR_PASSWORD_MESSAGE,
    INSUFFICIENT_PRIVILEGES_MESSAGE,
)


class BadRequestException(HTTPException):
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_400_BAD_REQUEST, detail=detail)


class UnauthorizedException(HTTPException):
    def __init__(self, detail: str = AUTHENTICATION_ERROR_MESSAGE):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=detail,
            headers={"WWW-Authenticate": "Bearer"},
        )


class ForbiddenException(HTTPException):
    def __init__(self, detail: str = INSUFFICIENT_PRIVILEGES_MESSAGE):
        super().__init__(status_code=status.HTTP_403_FORBIDDEN, detail=detail)


class NotFoundException(HTTPException):
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_404_NOT_FOUND, detail=detail)


class ConflictException(HTTPException):
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_409_CONFLICT, detail=detail)


class UnprocessableEntityException(HTTPException):
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=detail)


class EmailAlreadyRegisteredException(BadRequestException):
    def __init__(self):
        super().__init__(EMAIL_ALREADY_REGISTERED_MESSAGE)


class InvalidCredentialsException(UnauthorizedException):
    def __init__(self):
        super().__init__(INCORRECT_EMAIL_OR_PASSWORD_MESSAGE)


class AuthenticationCredentialsException(UnauthorizedException):
    def __init__(self):
        super().__init__(AUTHENTICATION_ERROR_MESSAGE)


class InactiveAccountException(ForbiddenException):
    def __init__(self, detail: str = ACCOUNT_INACTIVE_MESSAGE):
        super().__init__(detail)


class PendingApprovalException(ForbiddenException):
    def __init__(self):
        super().__init__(ACCOUNT_PENDING_APPROVAL_MESSAGE)


class DoctorNotFoundException(NotFoundException):
    def __init__(self):
        super().__init__(DOCTOR_NOT_FOUND_MESSAGE)
