from pydantic import BaseModel, EmailStr

from backend.constants.roles import UserRole


class TokenPayload(BaseModel):
    sub: str
    email: EmailStr
    role: UserRole
    iat: int
    exp: int
