import json
import os
from pathlib import Path
from typing import Any

from pymongo import ASCENDING, MongoClient

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
USERS_FILE = DATA_DIR / "users.json"
DOCTORS_FILE = DATA_DIR / "doctors.json"
SLOTS_FILE = DATA_DIR / "slots.json"
APPOINTMENTS_FILE = DATA_DIR / "appointments.json"
PAYMENTS_FILE = DATA_DIR / "payments.json"

COLLECTIONS = {
    USERS_FILE: "users",
    DOCTORS_FILE: "doctors",
    SLOTS_FILE: "slots",
    APPOINTMENTS_FILE: "appointments",
    PAYMENTS_FILE: "payments",
}
_mongo_client = None
_indexes_ready = False


def _mongo_database():
    global _mongo_client
    url = os.getenv("MONGODB_URL")
    if not url:
        return None
    if _mongo_client is None:
        _mongo_client = MongoClient(url)
    return _mongo_client[os.getenv("MONGODB_DATABASE", "doctor_appointment_booking")]


def _collection_for(path: Path):
    database = _mongo_database()
    if database is None:
        return None
    name = COLLECTIONS.get(path)
    if not name:
        return None
    _ensure_indexes(database)
    return database[name]


def _ensure_indexes(database) -> None:
    global _indexes_ready
    if _indexes_ready:
        return
    database.users.create_index([("email", ASCENDING)], unique=True)
    database.users.create_index([("role", ASCENDING), ("active", ASCENDING), ("approval_status", ASCENDING)])
    database.users.create_index([("name", ASCENDING), ("specialization", ASCENDING), ("clinic_address", ASCENDING)])
    database.slots.create_index([("doctor_id", ASCENDING), ("starts_at", ASCENDING), ("booked", ASCENDING)])
    database.appointments.create_index([("patient_id", ASCENDING), ("starts_at", ASCENDING)])
    database.appointments.create_index([("doctor_id", ASCENDING), ("starts_at", ASCENDING)])
    database.appointments.create_index([("slot_id", ASCENDING)])
    _indexes_ready = True


def ensure_data_dir() -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    if not USERS_FILE.exists():
        USERS_FILE.write_text(json.dumps([], indent=2), encoding="utf-8")
    if not DOCTORS_FILE.exists():
        DOCTORS_FILE.write_text(json.dumps([], indent=2), encoding="utf-8")
    for path in (SLOTS_FILE, APPOINTMENTS_FILE, PAYMENTS_FILE):
        if not path.exists():
            path.write_text(json.dumps([], indent=2), encoding="utf-8")


def load_json(path: Path, default: Any = None) -> Any:
    collection = _collection_for(path)
    if collection is not None:
        return list(collection.find({}, {"_id": 0}))
    ensure_data_dir()
    if not path.exists():
        return default
    return json.loads(path.read_text(encoding="utf-8"))


def save_json(path: Path, data: Any) -> Any:
    collection = _collection_for(path)
    if collection is not None:
        collection.delete_many({})
        if data:
            collection.insert_many(data)
        return data
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2), encoding="utf-8")
    return data


def get_users() -> list[dict[str, Any]]:
    return load_json(USERS_FILE, [])


def save_users(users: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return save_json(USERS_FILE, users)


def get_doctors() -> list[dict[str, Any]]:
    return load_json(DOCTORS_FILE, [])


def save_doctors(doctors: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return save_json(DOCTORS_FILE, doctors)


def get_slots():
    return load_json(SLOTS_FILE, [])


def save_slots(items):
    return save_json(SLOTS_FILE, items)


def get_appointments():
    return load_json(APPOINTMENTS_FILE, [])


def save_appointments(items):
    return save_json(APPOINTMENTS_FILE, items)


def get_payments():
    return load_json(PAYMENTS_FILE, [])


def save_payments(items):
    return save_json(PAYMENTS_FILE, items)

