from typing import Dict

from pydantic import BaseModel

from backend.database import connect_to_mongo

mongo_client, mongo_db, mongo_status = connect_to_mongo()
doctor_profiles_collection = mongo_db["doctor_profiles"] if mongo_db is not None else None

appointments_collection = mongo_db["appointments"] if mongo_db is not None else None
doctor_profiles: Dict[str, dict] = {}
appointments: Dict[str, dict] = {}

from datetime import datetime
from uuid import uuid4


class AppointmentCreate(BaseModel):
    patient_email: str
    doctor_email: str
    slot: str


class AppointmentOut(BaseModel):
    appointment_id: str
    patient_email: str
    doctor_email: str
    slot: str
    status: str
    amount: float | None = None
    transactions: list[dict] | None = None


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


def search_doctor_profiles(q: str | None = None, specialization: str | None = None, clinic_address: str | None = None) -> list[dict]:
    if doctor_profiles_collection is not None:
        query_filters = {}
        if specialization:
            query_filters["specialization"] = {"$regex": specialization, "$options": "i"}
        if clinic_address:
            query_filters["clinic_address"] = {"$regex": clinic_address, "$options": "i"}

        if q:
            query_filters["$or"] = [
                {"doctor_id": {"$regex": q, "$options": "i"}},
                {"specialization": {"$regex": q, "$options": "i"}},
                {"qualification": {"$regex": q, "$options": "i"}},
                {"clinic_address": {"$regex": q, "$options": "i"}},
            ]

        if not query_filters:
            profiles = doctor_profiles_collection.find()
        else:
            profiles = doctor_profiles_collection.find(query_filters)

        return [dict(profile, **{"_id": None}) for profile in profiles]

    results = []
    for profile in doctor_profiles.values():
        score = True
        if specialization:
            score = score and specialization.lower() in profile.get("specialization", "").lower()
        if clinic_address:
            score = score and clinic_address.lower() in profile.get("clinic_address", "").lower()
        if q:
            combined = " ".join(
                [profile.get("doctor_id", ""), profile.get("specialization", ""), profile.get("qualification", ""), profile.get("clinic_address", "")]
            ).lower()
            score = score and q.lower() in combined
        if score:
            results.append(profile)
    return results


def get_mongo_status() -> dict:
    return mongo_status


def create_appointment(appointment_data: dict) -> dict:
    # attach amount from doctor's consultation_fee when available
    doctor_email = appointment_data.get("doctor_email")
    amount = None
    profile = _read_doctor_profile(doctor_email) if doctor_email else None
    if profile and profile.get("consultation_fee") is not None:
        amount = float(profile.get("consultation_fee"))

    appointment_id = str(uuid4())
    now = datetime.utcnow().isoformat() + "Z"
    record = {
        "appointment_id": appointment_id,
        "patient_email": appointment_data.get("patient_email"),
        "doctor_email": doctor_email,
        "slot": appointment_data.get("slot"),
        "status": "PENDING",
        "amount": amount,
        "transactions": [],
        "created_at": now,
    }

    if appointments_collection is not None:
        appointments_collection.insert_one(record)
        rec = dict(record)
        rec.pop("_id", None)
        return rec

    appointments[appointment_id] = record
    return record


def get_appointment_by_id(appointment_id: str) -> dict | None:
    if appointments_collection is not None:
        doc = appointments_collection.find_one({"appointment_id": appointment_id})
        if not doc:
            return None
        doc.pop("_id", None)
        return doc
    return appointments.get(appointment_id)


def update_appointment_status(appointment_id: str, status: str, transaction: dict | None = None) -> dict | None:
    if appointments_collection is not None:
        update_doc = {"$set": {"status": status}}
        if transaction:
            update_doc.setdefault("$push", {}).setdefault("transactions", transaction)
        appointments_collection.update_one({"appointment_id": appointment_id}, update_doc)
        return get_appointment_by_id(appointment_id)

    rec = appointments.get(appointment_id)
    if not rec:
        return None
    rec["status"] = status
    if transaction:
        rec.setdefault("transactions", []).append(transaction)
    return rec


def cancel_appointment(appointment_id: str) -> bool:
    if appointments_collection is not None:
        res = appointments_collection.delete_one({"appointment_id": appointment_id})
        return res.deleted_count > 0
    return appointments.pop(appointment_id, None) is not None
