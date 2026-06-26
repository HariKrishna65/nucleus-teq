from datetime import date

from pydantic import BaseModel, EmailStr

from backend.constants.roles import UserRole
from backend.constants.status import AccountStatus


class UserResponse(BaseModel):
    id: str
    full_name: str
    email: EmailStr
    phone_number: str
    role: UserRole
    status: AccountStatus
    gender: str | None = None
    date_of_birth: date | None = None
    qualification: str | None = None
    specialization: str | None = None
    experience: int | None = None
    license_number: str | None = None
    consultation_fee: float | None = None
    clinic_address: str | None = None


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int
    user: UserResponse
