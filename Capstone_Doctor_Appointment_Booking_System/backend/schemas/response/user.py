from datetime import date

from pydantic import BaseModel, EmailStr


class AccountResponse(BaseModel):
    id: str
    name: str
    email: EmailStr
    phone: str
    role: str
    active: bool = True
    approval_status: str | None = None
    gender: str | None = None
    date_of_birth: date | None = None

