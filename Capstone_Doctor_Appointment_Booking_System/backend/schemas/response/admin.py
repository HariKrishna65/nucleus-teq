from pydantic import BaseModel, EmailStr


class AdminResponse(BaseModel):
    id: str
    name: str
    email: EmailStr
    phone: str
    role: str
    active: bool = True
