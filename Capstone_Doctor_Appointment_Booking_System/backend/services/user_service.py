import bcrypt
from uuid import uuid4

from backend.constants.messages import EMAIL_ALREADY_REGISTERED_MESSAGE
from backend.enums.user import ApprovalStatus, UserRole
from backend.database.database import get_users, save_users
from backend.schemas.request.user import AccountCreate


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))


def get_user_by_email(email: str) -> dict | None:
    normalized = email.strip().lower()
    return next((user for user in get_users() if user["email"] == normalized), None)


def get_user_by_id(user_id: str) -> dict | None:
    return next((user for user in get_users() if user["id"] == user_id), None)


def create_user(payload: AccountCreate) -> dict:
    if get_user_by_email(payload.email):
        raise ValueError(EMAIL_ALREADY_REGISTERED_MESSAGE)

    role = payload.role.value
    user = {
        "id": str(uuid4()),
        "name": payload.full_name.strip(),
        "email": payload.email.strip().lower(),
        "phone": payload.phone,
        "role": role,
        "gender": payload.gender,
        "date_of_birth": payload.date_of_birth.isoformat() if payload.date_of_birth else None,
        "qualification": payload.qualification,
        "specialization": payload.specialization,
        "experience": payload.experience,
        "license_number": payload.license_number,
        "consultation_fee": payload.consultation_fee,
        "clinic_address": payload.clinic_address,
        "active": role != UserRole.DOCTOR.value,
        "approval_status": ApprovalStatus.PENDING.value if role == UserRole.DOCTOR.value else ApprovalStatus.APPROVED.value,
        "hashed_password": hash_password(payload.password),
    }
    users = get_users()
    users.append(user)
    save_users(users)
    return user
