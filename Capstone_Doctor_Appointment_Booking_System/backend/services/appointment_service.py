from typing import Dict

from pydantic import BaseModel

from backend.database import connect_to_mongo

mongo_client, mongo_db, mongo_status = connect_to_mongo()
doctor_profiles_collection = mongo_db["doctor_profiles"] if mongo_db is not None else None

doctor_profiles: Dict[str, dict] = {}


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
