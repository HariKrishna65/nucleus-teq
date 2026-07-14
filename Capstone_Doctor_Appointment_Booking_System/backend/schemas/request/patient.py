from datetime import date
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from backend.enums.appointment import PaymentMethod


class PatientProfileUpdate(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str | None = Field(default=None, min_length=2)
    phone: str | None = Field(default=None, pattern=r"^\d{10}$")
    gender: Literal["MALE", "FEMALE", "OTHER"] | None = None
    date_of_birth: date | None = None


class AppointmentCreate(BaseModel):
    doctor_id: str
    slot_id: str


class PaymentCreate(BaseModel):
    appointment_id: str
    method: PaymentMethod = PaymentMethod.CARD
