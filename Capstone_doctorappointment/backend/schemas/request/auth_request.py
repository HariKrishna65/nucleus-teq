from datetime import date

from pydantic import BaseModel, EmailStr, Field, field_validator, model_validator

from backend.constants.roles import UserRole


class UserRegistrationRequest(BaseModel):
    full_name: str = Field(min_length=2, pattern=r"^[A-Za-z ]+$")
    email: EmailStr
    password: str = Field(min_length=8, max_length=12)
    phone_number: str = Field(pattern=r"^\d{10}$")
    role: UserRole

    gender: str | None = None
    date_of_birth: date | None = None

    qualification: str | None = None
    specialization: str | None = None
    experience: int | None = Field(default=None, ge=0)
    license_number: str | None = None
    consultation_fee: float | None = Field(default=None, ge=0)
    clinic_address: str | None = None

    @field_validator("password")
    @classmethod
    def validate_password(cls, password: str) -> str:
        has_uppercase = any(character.isupper() for character in password)
        has_special = any(not character.isalnum() for character in password)
        if not has_uppercase or not has_special:
            raise ValueError("Password must contain one uppercase letter and one special character")
        return password

    @model_validator(mode="after")
    def validate_role_specific_fields(self):
        if self.role == UserRole.PATIENT:
            missing_fields = [
                field_name
                for field_name in ("gender", "date_of_birth")
                if getattr(self, field_name) is None
            ]
        elif self.role == UserRole.DOCTOR:
            missing_fields = [
                field_name
                for field_name in (
                    "qualification",
                    "specialization",
                    "experience",
                    "license_number",
                )
                if getattr(self, field_name) is None
            ]
        else:
            missing_fields = []

        if missing_fields:
            fields = ", ".join(missing_fields)
            raise ValueError(f"Missing required fields for {self.role.value}: {fields}")
        return self


class LoginRequest(BaseModel):
    email: EmailStr
    password: str
