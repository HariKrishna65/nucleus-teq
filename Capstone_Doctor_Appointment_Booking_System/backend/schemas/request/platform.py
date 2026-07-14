from datetime import date, datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from backend.enums.appointment import AppointmentStatus, PaymentMethod


class PatientProfileUpdate(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str | None = Field(default=None, min_length=2)
    phone: str | None = Field(default=None, pattern=r"^\d{10}$")
    gender: Literal["MALE", "FEMALE", "OTHER"] | None = None
    date_of_birth: date | None = None


class DoctorProfileUpdate(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str | None = Field(default=None, min_length=2)
    phone: str | None = Field(default=None, pattern=r"^\d{10}$")
    qualification: str | None = None
    specialization: str | None = None
    experience: int | None = Field(default=None, ge=0)
    license_number: str | None = None
    consultation_fee: float | None = Field(default=None, ge=0)
    clinic_address: str | None = None
