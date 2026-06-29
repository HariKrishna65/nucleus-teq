from datetime import datetime, timedelta, timezone
from typing import Dict

from fastapi import Depends, FastAPI, HTTPException, Request, status
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel
from pymongo import MongoClient

from mongodb_config import build_mongodb_uri, get_database_name

app = FastAPI(title="Appointment Service", version="0.1.0")

SECRET_KEY = "doctor-appointment-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
users_db: Dict[str, dict] = {
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

MONGODB_URI = build_mongodb_uri()
MONGODB_DB_NAME = get_database_name()

try:
    mongo_client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    mongo_client.admin.command("ping")
    mongo_db = mongo_client[MONGODB_DB_NAME]
    users_collection = mongo_db["appointment_users"]
    doctor_profiles_collection = mongo_db["doctor_profiles"]
    mongo_status = {"connected": True}
except Exception as exc:  # pragma: no cover - runtime dependency
    mongo_client = None
    mongo_db = None
    users_collection = None
    doctor_profiles_collection = None
    mongo_status = {"connected": False, "error": str(exc)}


class UserLogin(BaseModel):
    email: str
    password: str


class UserOut(BaseModel):
    email: str
    role: str


class DoctorProfileCreate(BaseModel):
    doctor_id: str
    specialization: str
    qualification: str
    experience_years: int
    consultation_fee: float
    clinic_address: str


class DoctorProfileOut(BaseModel):
    doctor_id: str
    specialization: str
    qualification: str
    experience_years: int
    consultation_fee: float
    clinic_address: str


doctor_profiles: Dict[str, dict] = {}


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


def _read_doctor_profile(email: str) -> dict | None:
    if doctor_profiles_collection is not None:
        profile_doc = doctor_profiles_collection.find_one({"doctor_id": email})
        if profile_doc:
            profile = dict(profile_doc)
            profile.pop("_id", None)
            return profile
        return None
    return doctor_profiles.get(email)


def _store_doctor_profile(profile_data: dict) -> None:
    if doctor_profiles_collection is not None:
        doctor_profiles_collection.insert_one(profile_data)
    else:
        doctor_profiles[profile_data["doctor_id"]] = profile_data


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
    return {"service": "appointment-service", "status": "ok", "database": mongo_status}


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


@app.get("/appointments/doctor-dashboard")
def doctor_dashboard(current_user: dict = Depends(require_role("DOCTOR"))):
    return {"message": "Doctor appointment dashboard access granted", "role": current_user["role"]}


@app.get("/appointments/admin-dashboard")
def admin_dashboard(current_user: dict = Depends(require_role("ADMIN"))):
    return {"message": "Admin appointment dashboard access granted", "role": current_user["role"]}


@app.post("/doctors/profile", response_model=DoctorProfileOut)
def create_doctor_profile(profile: DoctorProfileCreate, current_user: dict = Depends(require_role("DOCTOR"))):
    if current_user["email"] != profile.doctor_id:
        raise HTTPException(status_code=403, detail="You can only manage your own profile")

    profile_data = profile.dict()
    _store_doctor_profile(profile_data)
    return profile_data


@app.get("/doctors/profile", response_model=DoctorProfileOut)
def get_doctor_profile(current_user: dict = Depends(require_role("DOCTOR"))):
    profile = _read_doctor_profile(current_user["email"])
    if not profile:
        raise HTTPException(status_code=404, detail="Doctor profile not found")
    return profile
