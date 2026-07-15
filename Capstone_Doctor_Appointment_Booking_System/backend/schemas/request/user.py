import re
from datetime import date
from typing import Literal

from pydantic import BaseModel, EmailStr, Field, field_validator, model_validator

from backend.enums.user import UserRole


NAME_PATTERN = re.compile(r"^[A-Za-z' -]+$")
PASSWORD_PATTERN = re.compile(r"^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,12}$")
GMAIL_DOMAIN_MESSAGE = "Email must end with @gmail.com"


def validate_gmail_email(value: EmailStr) -> EmailStr:
    if not value.lower().endswith("@gmail.com"):
        raise ValueError(GMAIL_DOMAIN_MESSAGE)
    return value


class AccountCreate(BaseModel):
    full_name: str = Field(min_length=2, max_length=80)
    email: EmailStr
    password: str
    phone: str = Field(pattern=r"^\d{10}$")
    role: UserRole
    gender: Literal["MALE", "FEMALE", "OTHER"] | None = None
    date_of_birth: date | None = None
    qualification: str | None = None
    specialization: str | None = None
    experience: int | None = Field(default=None, ge=0, le=70)
    license_number: str | None = None
    consultation_fee: float | None = Field(default=None, ge=0)
    clinic_address: str | None = None

    @field_validator("email")
    @classmethod
    def email_must_be_gmail(cls, value: EmailStr) -> EmailStr:
        return validate_gmail_email(value)

    @model_validator(mode="after")
    def validate_registration(self):
        if not NAME_PATTERN.fullmatch(self.full_name.strip()):
            raise ValueError("Full name must contain alphabets, spaces, hyphens, and apostrophes only")
        if not PASSWORD_PATTERN.fullmatch(self.password):
            raise ValueError(
                "Password must be 8-12 characters and include an uppercase letter and special character"
            )
        if self.role == UserRole.PATIENT and (not self.gender or not self.date_of_birth):
            raise ValueError("Patient registration requires gender and date_of_birth")
        if self.role == UserRole.DOCTOR and not all((
            self.qualification, self.specialization, self.license_number,
            self.consultation_fee is not None, self.clinic_address,
        )):
            raise ValueError("Doctor registration requires qualification, specialization, license number, fee and clinic address")
        if self.date_of_birth and self.date_of_birth >= date.today():
            raise ValueError("date_of_birth must be in the past")
        return self


class PatientCreate(BaseModel):
    full_name: str = Field(min_length=2, max_length=80)
    email: EmailStr
    password: str
    phone: str = Field(pattern=r"^\d{10}$")
    gender: Literal["MALE", "FEMALE", "OTHER"]
    date_of_birth: date

    @field_validator("email")
    @classmethod
    def email_must_be_gmail(cls, value: EmailStr) -> EmailStr:
        return validate_gmail_email(value)

    @model_validator(mode="after")
    def validate_patient_registration(self):
        if not NAME_PATTERN.fullmatch(self.full_name.strip()):
            raise ValueError("Full name must contain alphabets, spaces, hyphens, and apostrophes only")
        if not PASSWORD_PATTERN.fullmatch(self.password):
            raise ValueError("Password must be 8-12 characters and include an uppercase letter and special character")
        if self.date_of_birth >= date.today():
            raise ValueError("date_of_birth must be in the past")
        return self


class DoctorCreate(BaseModel):
    full_name: str = Field(min_length=2, max_length=80)
    email: EmailStr
    password: str
    phone: str = Field(pattern=r"^\d{10}$")
    qualification: str
    specialization: str
    experience: int = Field(ge=0, le=70)
    license_number: str
    consultation_fee: float = Field(ge=0)
    clinic_address: str

    @field_validator("email")
    @classmethod
    def email_must_be_gmail(cls, value: EmailStr) -> EmailStr:
        return validate_gmail_email(value)

    @model_validator(mode="after")
    def validate_doctor_registration(self):
        if not NAME_PATTERN.fullmatch(self.full_name.strip()):
            raise ValueError("Full name must contain alphabets, spaces, hyphens, and apostrophes only")
        if not PASSWORD_PATTERN.fullmatch(self.password):
            raise ValueError("Password must be 8-12 characters and include an uppercase letter and special character")
        return self


class LoginRequest(BaseModel):
    email: EmailStr
    password: str

    @field_validator("email")
    @classmethod
    def email_must_be_gmail(cls, value: EmailStr) -> EmailStr:
        return validate_gmail_email(value)

