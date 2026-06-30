from fastapi import APIRouter, Depends, HTTPException

from backend.services.auth_service import (
    UserCreate,
    UserLogin,
    UserOut,
    UserProfileUpdate,
    _read_user_by_email,
    _store_user,
    _update_user,
    create_access_token,
    pwd_context,
    require_role,
    get_current_user,
)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=UserOut)
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


@router.post("/login")
def login_user(user: UserLogin):
    stored_user = _read_user_by_email(user.email)
    if not stored_user:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    if not pwd_context.verify(user.password, stored_user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    access_token = create_access_token(
        {"sub": stored_user["email"], "email": stored_user["email"], "role": stored_user["role"]}
    )

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "user": {
            "email": stored_user["email"],
            "role": stored_user["role"],
        },
    }


@router.get("/me", response_model=UserOut)
def get_profile(current_user: dict = Depends(get_current_user)):
    return {
        "full_name": current_user["full_name"],
        "email": current_user["email"],
        "phone": current_user["phone"],
        "role": current_user["role"],
    }


@router.put("/me", response_model=UserOut)
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


@router.get("/patient-dashboard")
def patient_dashboard(current_user: dict = Depends(require_role("PATIENT"))):
    return {"message": "Patient dashboard access granted", "role": current_user["role"]}


@router.get("/doctor-dashboard")
def doctor_dashboard(current_user: dict = Depends(require_role("DOCTOR"))):
    return {"message": "Doctor dashboard access granted", "role": current_user["role"]}


@router.get("/admin-dashboard")
def admin_dashboard(current_user: dict = Depends(require_role("ADMIN"))):
    return {"message": "Admin dashboard access granted", "role": current_user["role"]}
