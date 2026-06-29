from datetime import datetime, timedelta, timezone
from typing import Dict

from fastapi import Depends, FastAPI, HTTPException, Request, status
from fastapi.security import HTTPBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel
from pymongo import MongoClient

from mongodb_config import build_mongodb_uri, get_database_name

app = FastAPI(title="User Service", version="0.1.0")

SECRET_KEY = "doctor-appointment-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
users_db: Dict[str, dict] = {}
security = HTTPBearer()

MONGODB_URI = build_mongodb_uri()
MONGODB_DB_NAME = get_database_name()

try:
    mongo_client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    mongo_client.admin.command("ping")
    mongo_db = mongo_client[MONGODB_DB_NAME]
    users_collection = mongo_db["users"]
    mongo_status = {"connected": True}
except Exception as exc:  # pragma: no cover - runtime dependency
    mongo_client = None
    mongo_db = None
    users_collection = None
    mongo_status = {"connected": False, "error": str(exc)}


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


def _read_user_by_email(email: str) -> dict | None:
    if users_collection is not None:
        user_doc = users_collection.find_one({"email": email})
        if user_doc:
            user = dict(user_doc)
            user.pop("_id", None)
            return user
        return None
    return users_db.get(email)


def _store_user(user_data: dict) -> None:
    if users_collection is not None:
        users_collection.insert_one(user_data)
    else:
        users_db[user_data["email"]] = user_data


def _update_user(email: str, updates: dict) -> None:
    if users_collection is not None:
        users_collection.update_one({"email": email}, {"$set": updates})
    else:
        user = users_db.get(email)
        if user:
            user.update(updates)


def get_current_user(request: Request) -> dict:
    auth_header = request.headers.get("authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing token")

    token = auth_header.split(" ", 1)[1]
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token") from exc

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


@app.get("/health")
def health_check():
    return {"service": "user-service", "status": "ok", "database": mongo_status}


@app.post("/auth/register", response_model=UserOut)
def register_user(user: UserCreate):
    if _read_user_by_email(user.email):
        raise HTTPException(status_code=400, detail="User already exists")

    hashed_password = pwd_context.hash(user.password)
    user_data = {
        "full_name": user.full_name,
        "email": user.email,
        "password": hashed_password,
        "phone": user.phone,
        "role": user.role.upper(),
    }
    _store_user(user_data)

    return {
        "full_name": user.full_name,
        "email": user.email,
        "phone": user.phone,
        "role": user.role.upper(),
    }


@app.post("/auth/login")
def login_user(user: UserLogin):
    stored_user = _read_user_by_email(user.email)
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


@app.get("/auth/me", response_model=UserOut)
def get_profile(current_user: dict = Depends(get_current_user)):
    return {
        "full_name": current_user["full_name"],
        "email": current_user["email"],
        "phone": current_user["phone"],
        "role": current_user["role"],
    }


@app.put("/auth/me", response_model=UserOut)
def update_profile(profile_update: UserProfileUpdate, current_user: dict = Depends(get_current_user)):
    updates = {}
    if profile_update.full_name is not None:
        updates["full_name"] = profile_update.full_name
    if profile_update.phone is not None:
        updates["phone"] = profile_update.phone
    if updates:
        _update_user(current_user["email"], updates)
        current_user.update(updates)

    return {
        "full_name": current_user["full_name"],
        "email": current_user["email"],
        "phone": current_user["phone"],
        "role": current_user["role"],
    }


@app.get("/auth/patient-dashboard")
def patient_dashboard(current_user: dict = Depends(require_role("PATIENT"))):
    return {"message": "Patient dashboard access granted", "role": current_user["role"]}


@app.get("/auth/doctor-dashboard")
def doctor_dashboard(current_user: dict = Depends(require_role("DOCTOR"))):
    return {"message": "Doctor dashboard access granted", "role": current_user["role"]}


@app.get("/auth/admin-dashboard")
def admin_dashboard(current_user: dict = Depends(require_role("ADMIN"))):
    return {"message": "Admin dashboard access granted", "role": current_user["role"]}
