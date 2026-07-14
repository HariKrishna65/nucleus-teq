from backend.database.database import get_doctors, save_doctors


def search_doctor_profiles() -> list[dict]:
    return get_doctors()


def _read_doctor_profile(doctor_id: str) -> dict | None:
    return next((doctor for doctor in get_doctors() if doctor.get("id") == doctor_id), None)


def _delete_doctor_profile(doctor_id: str) -> bool:
    doctors = get_doctors()
    remaining_doctors = [doctor for doctor in doctors if doctor.get("id") != doctor_id]
    if len(remaining_doctors) == len(doctors):
        return False
    save_doctors(remaining_doctors)
    return True
