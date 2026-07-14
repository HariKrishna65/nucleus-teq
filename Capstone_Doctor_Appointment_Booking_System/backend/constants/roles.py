from backend.enums.user import UserRole

ROLE_PATIENT = UserRole.PATIENT.value
ROLE_DOCTOR = UserRole.DOCTOR.value

ALLOWED_REGISTRATION_ROLES = {ROLE_PATIENT, ROLE_DOCTOR}
