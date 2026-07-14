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


class SlotCreate(BaseModel):
    starts_at: datetime
    ends_at: datetime


class SlotUpdate(BaseModel):
    starts_at: datetime
    ends_at: datetime


class AppointmentCreate(BaseModel):
    doctor_id: str
    slot_id: str


class PaymentCreate(BaseModel):
    appointment_id: str
    method: PaymentMethod = PaymentMethod.CARD


class DoctorCancellationRequest(BaseModel):
    reason: str = Field(min_length=3, max_length=500)


class StatusUpdate(BaseModel):
    status: Literal[AppointmentStatus.COMPLETED, AppointmentStatus.MISSED_APPOINTMENT]


class ActivationUpdate(BaseModel):
    active: bool

