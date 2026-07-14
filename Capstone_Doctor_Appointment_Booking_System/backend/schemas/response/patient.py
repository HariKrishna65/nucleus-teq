from datetime import date
from pydantic import BaseModel, EmailStr


class PatientResponse(BaseModel):
    id: str
    name: str
    email: EmailStr
    phone: str
    role: str
    gender: str
    date_of_birth: date
    active: bool = True
