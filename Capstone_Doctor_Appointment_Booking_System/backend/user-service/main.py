from datetime import datetime, timedelta, timezone
from typing import Dict

from fastapi import FastAPI, HTTPException
from passlib.context import CryptContext
from pydantic import BaseModel
from jose import JWTError, jwt

app = FastAPI(title="User Service", version="0.1.0")

SECRET_KEY = "doctor-appointment-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
users_db: Dict[str, dict] = {}


class UserCreate(BaseModel):
    full_name: str
    email: str
    password: str
    phone: str
    role: str = "PATIENT"


class UserLogin(BaseModel):
    email: str
    password: str


class UserOut(BaseModel):
    full_name: str
    email: str
    phone: str
    role: str


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(timezone.utc) + expires_delta
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


@app.get("/health")
def health_check():
    return {"service": "user-service", "status": "ok"}


@app.post("/auth/register", response_model=UserOut)
def register_user(user: UserCreate):
    if user.email in users_db:
        raise HTTPException(status_code=400, detail="User already exists")

    hashed_password = pwd_context.hash(user.password)
    users_db[user.email] = {
        "full_name": user.full_name,
        "email": user.email,
        "password": hashed_password,
        "phone": user.phone,
        "role": user.role.upper(),
    }

    return {
        "full_name": user.full_name,
        "email": user.email,
        "phone": user.phone,
        "role": user.role.upper(),
    }


@app.post("/auth/login")
def login_user(user: UserLogin):
    stored_user = users_db.get(user.email)
    if not stored_user:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    if not pwd_context.verify(user.password, stored_user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    access_token = create_access_token(
        {"sub": stored_user["email"], "email": stored_user["email"], "role": stored_user["role"]},
        expires_delta=timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
    )

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "user": {
            "email": stored_user["email"],
            "role": stored_user["role"],
        },
    }
