from datetime import datetime, timedelta, timezone
from typing import Dict

from fastapi import Depends, HTTPException, Request, status
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel

from backend.database import connect_to_mongo

SECRET_KEY = "doctor-appointment-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# Use PBKDF2-SHA256 to avoid bcrypt backend compatibility issues in this environment.
pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")

USERS_DB: Dict[str, dict] = {
    "doctor@example.com": {
        "email": "doctor@example.com",
        "password": pwd_context.hash("Password123!"),
        "role": "DOCTOR",
    },
    "patient@example.com": {
        "email": "patient@example.com",
        "password": pwd_context.hash("Password123!"),
        "role": "PATIENT",
    },
}

mongo_client, mongo_db, mongo_status = connect_to_mongo()
users_collection = mongo_db["users"] if mongo_db is not None else None


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


class UserProfileUpdate(BaseModel):
    full_name: str | None = None
    phone: str | None = None


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(timezone.utc) + expires_delta
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token") from exc


def _read_user_by_email(email: str) -> dict | None:
    if users_collection is not None:
        user_doc = users_collection.find_one({"email": email})
        if user_doc:
            user = dict(user_doc)
            user.pop("_id", None)
            return user
        return None
    return USERS_DB.get(email)


def _store_user(user_data: dict) -> None:
    if users_collection is not None:
        users_collection.insert_one(user_data)
    else:
        USERS_DB[user_data["email"]] = user_data


def _update_user(email: str, updates: dict) -> None:
    if users_collection is not None:
        users_collection.update_one({"email": email}, {"$set": updates})
    else:
        user = USERS_DB.get(email)
        if user:
            user.update(updates)


def get_current_user(request: Request) -> dict:
    auth_header = request.headers.get("authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing token")

    token = auth_header.split(" ", 1)[1]
    payload = decode_token(token)

    email = payload.get("email")
    if email is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")

    user = _read_user_by_email(email)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")

    return user


def require_role(*allowed_roles: str):
    def role_checker(current_user: dict = Depends(get_current_user)) -> dict:
        if current_user.get("role", "").upper() not in allowed_roles:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
        return current_user

    return role_checker
