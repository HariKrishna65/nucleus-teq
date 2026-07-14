from pydantic import BaseModel, EmailStr


class DoctorResponse(BaseModel):
    id: str
    name: str
    email: EmailStr
    phone: str
    role: str
    qualification: str
    specialization: str
    experience: int
    license_number: str
    consultation_fee: float
    clinic_address: str
    active: bool = True
    approval_status: str
