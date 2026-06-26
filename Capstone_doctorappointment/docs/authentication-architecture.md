# User Models & Authentication

## Flow

1. User submits registration data to `POST /api/auth/register`.
2. Request data is validated by `UserRegistrationRequest`.
3. Password is hashed using BCrypt before saving.
4. User is stored in MongoDB through the Beanie `User` document.
5. User logs in through `POST /api/auth/login`.
6. Password is verified against the stored hash.
7. Backend returns a JWT access token with a 30-minute expiry.
8. Protected APIs use the JWT to identify the current active user.
9. Role-protected APIs can use `require_roles(...)` to allow only specific roles.

## Main Files

- `backend/models/user.py` defines the MongoDB user document.
- `backend/schemas/request/auth_request.py` validates registration and login input.
- `backend/schemas/response/auth_response.py` controls safe API output.
- `backend/utils/security.py` handles password hashing and JWT creation/validation.
- `backend/services/auth_service.py` contains registration and login business logic.
- `backend/middleware/auth.py` provides reusable current-user and role-check dependencies.
- `backend/routers/auth_router.py` exposes authentication endpoints.
- `backend/database/connection.py` initializes MongoDB and Beanie.

## Roles

- `PATIENT`
- `DOCTOR`
- `ADMIN`

Public registration allows patient and doctor accounts. Admin accounts should be created separately by a trusted setup process.

## Endpoints

- `POST /api/auth/register` creates a patient or doctor account.
- `POST /api/auth/login` verifies credentials and returns a JWT.
- `GET /api/auth/me` returns the logged-in user's profile.
- `GET /api/auth/validate-token` confirms that a token is valid.

## JWT Claims

- `sub`: user id
- `email`: user email
- `role`: user role
- `iat`: issued-at timestamp
- `exp`: expiry timestamp
