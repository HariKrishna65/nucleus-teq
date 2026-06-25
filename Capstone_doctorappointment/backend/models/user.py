from datetime import date, datetime, timezone

from beanie import Document, Indexed
from pydantic import EmailStr, Field

from backend.constants.roles import UserRole
from backend.constants.status import AccountStatus


class User(Document):
    full_name: str = Field(min_length=2)
    email: Indexed(EmailStr, unique=True)
    hashed_password: str
    phone_number: str = Field(pattern=r"^\d{10}$")
    role: UserRole
    status: AccountStatus = AccountStatus.ACTIVE

    gender: str | None = None
    date_of_birth: date | None = None

    qualification: str | None = None
    specialization: str | None = None
    experience: int | None = Field(default=None, ge=0)
    license_number: str | None = None
    consultation_fee: float | None = Field(default=None, ge=0)
    clinic_address: str | None = None

    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    updated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    class Settings:
        name = "users"
