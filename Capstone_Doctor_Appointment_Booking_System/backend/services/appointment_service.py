from datetime import datetime
from typing import Dict
from uuid import uuid4

from pydantic import BaseModel

from backend.database import connect_to_mongo

from bson.objectid import ObjectId

mongo_client, mongo_db, mongo_status = connect_to_mongo()
doctor_profiles_collection = mongo_db["doctor_profiles"] if mongo_db is not None else None
# Collections for slots and appointments
slots_collection = mongo_db["slots"] if mongo_db is not None else None
appointments_collection = mongo_db["appointments"] if mongo_db is not None else None

if slots_collection is not None:
    slots_collection.create_index(
        [("doctor_id", 1), ("date", 1), ("time", 1)],
        unique=True,
        name="unique_slot",
    )

# In-memory fallbacks when Mongo is not available
doctor_profiles: Dict[str, dict] = {}
slots_local: Dict[str, dict] = {}
appointments_local: Dict[str, dict] = {}


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


# Slot and appointment models
class SlotCreate(BaseModel):
    doctor_id: str
    date: str  # YYYY-MM-DD
    time: str  # HH:MM


class SlotOut(SlotCreate):
    id: str | None = None


class AppointmentCreate(BaseModel):
    doctor_id: str
    appointment_date: str
    slot_time: str
    patient_email: str | None = None


class AppointmentOut(AppointmentCreate):
    id: str | None = None
    created_at: str | None = None
    status: str | None = None


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


def create_slot(slot_data: dict) -> dict:
    doctor_id = slot_data.get("doctor_id")
    date = slot_data.get("date")
    time = slot_data.get("time")
    if not doctor_id or not date or not time:
        raise ValueError("doctor_id, date, and time are required for a slot")

    if slots_collection is not None:
        existing = slots_collection.find_one({"doctor_id": doctor_id, "date": date, "time": time})
        if existing:
            raise ValueError("Slot already exists")
        res = slots_collection.insert_one({"doctor_id": doctor_id, "date": date, "time": time, "booked": False})
        slot = {"doctor_id": doctor_id, "date": date, "time": time, "booked": False, "id": str(res.inserted_id)}
        return slot

    # Local in-memory storage
    key = f"{doctor_id}:{date}:{time}"
    if key in slots_local:
        raise ValueError("Slot already exists")
    slot = {"doctor_id": doctor_id, "date": date, "time": time, "booked": False, "id": str(uuid4())}
    slots_local[key] = slot
    return slot


def list_slots(doctor_id: str | None = None, date: str | None = None) -> list[dict]:
    if slots_collection is not None:
        query = {}
        if doctor_id:
            query["doctor_id"] = doctor_id
        if date:
            query["date"] = date
        docs = slots_collection.find(query)
        slots = []
        for s in docs:
            slot = dict(s)
            slot["id"] = str(slot.pop("_id", ""))
            slots.append(slot)
        return slots
    results = []
    for s in slots_local.values():
        if doctor_id and s["doctor_id"] != doctor_id:
            continue
        if date and s["date"] != date:
            continue
        results.append(dict(s))
    return results


def create_appointment(appointment_data: dict) -> dict:
    appointment_date = appointment_data.get("appointment_date")
    slot_time = appointment_data.get("slot_time")
    doctor_id = appointment_data.get("doctor_id")

    if not doctor_id or not appointment_date or not slot_time:
        raise ValueError("Missing required booking fields")

    if slots_collection is not None:
        slot = slots_collection.find_one({"doctor_id": doctor_id, "date": appointment_date, "time": slot_time})
        if not slot:
            raise ValueError("Selected slot is not available")
        if slot.get("booked"):
            raise ValueError("Selected slot is already booked")
    else:
        key = f"{doctor_id}:{appointment_date}:{slot_time}"
        slot = slots_local.get(key)
        if not slot:
            raise ValueError("Selected slot is not available")
        if slot.get("booked"):
            raise ValueError("Selected slot is already booked")

    appointment_data["status"] = "CONFIRMED"
    appointment_data["created_at"] = datetime.utcnow().isoformat()
    if appointments_collection is not None:
        res = appointments_collection.insert_one(appointment_data)
        appt = dict(appointment_data)
        appt["id"] = str(res.inserted_id)
        if slots_collection is not None:
            update_result = slots_collection.update_one(
                {"doctor_id": doctor_id, "date": appointment_date, "time": slot_time, "booked": False},
                {"$set": {"booked": True}},
            )
            if update_result.modified_count != 1:
                raise ValueError("Selected slot was already booked")
        return appt

    # Local in-memory booking
    appt_id = str(uuid4())
    appt = dict(appointment_data)
    appt["id"] = appt_id
    appointments_local[appt_id] = appt
    # mark slot booked
    key = f"{doctor_id}:{appointment_date}:{slot_time}"
    if key in slots_local:
        slots_local[key]["booked"] = True
    return appt


def list_appointments_for_doctor(doctor_id: str) -> list[dict]:
    if appointments_collection is not None:
        docs = appointments_collection.find({"doctor_id": doctor_id})
        appointments = []
        for a in docs:
            appointment = dict(a)
            appointment["id"] = str(appointment.pop("_id", ""))
            appointments.append(appointment)
        return appointments
    results = []
    for a in appointments_local.values():
        if a.get("doctor_id") == doctor_id:
            results.append(dict(a))
    return results


def list_appointments_for_patient(patient_email: str) -> list[dict]:
    if appointments_collection is not None:
        docs = appointments_collection.find({"patient_email": patient_email})
        appointments = []
        for a in docs:
            appointment = dict(a)
            appointment["id"] = str(appointment.pop("_id", ""))
            appointments.append(appointment)
        return appointments
    results = []
    for a in appointments_local.values():
        if a.get("patient_email") == patient_email:
            results.append(dict(a))
    return results


def get_appointment_by_id(appointment_id: str) -> dict | None:
    if appointments_collection is not None:
        try:
            doc = appointments_collection.find_one({"_id": ObjectId(appointment_id)})
        except Exception:
            return None
        if not doc:
            return None
        appt = dict(doc)
        appt["id"] = str(appt.pop("_id", ""))
        return appt

    return appointments_local.get(appointment_id)


def update_appointment_status(appointment_id: str, status: str) -> dict:
    if appointments_collection is not None:
        try:
            res = appointments_collection.update_one({"_id": ObjectId(appointment_id)}, {"$set": {"status": status}})
            if res.matched_count != 1:
                raise ValueError("Appointment not found")
            doc = appointments_collection.find_one({"_id": ObjectId(appointment_id)})
            doc["id"] = str(doc.pop("_id", ""))
            return doc
        except Exception as exc:
            raise

    appt = appointments_local.get(appointment_id)
    if not appt:
        raise ValueError("Appointment not found")
    appt["status"] = status
    return appt


def cancel_appointment(appointment_id: str, requester_email: str | None = None) -> dict:
    appt = get_appointment_by_id(appointment_id)
    if not appt:
        raise ValueError("Appointment not found")

    # Authorization: if requester is patient, ensure ownership
    if requester_email and appt.get("patient_email") and requester_email != appt.get("patient_email"):
        raise ValueError("Not authorized to cancel this appointment")

    # Update status and free slot
    appt = update_appointment_status(appointment_id, "CANCELLED")

    doctor_id = appt.get("doctor_id")
    appointment_date = appt.get("appointment_date")
    slot_time = appt.get("slot_time")

    if slots_collection is not None:
        slots_collection.update_one({"doctor_id": doctor_id, "date": appointment_date, "time": slot_time}, {"$set": {"booked": False}})
    else:
        key = f"{doctor_id}:{appointment_date}:{slot_time}"
        if key in slots_local:
            slots_local[key]["booked"] = False

    return appt
