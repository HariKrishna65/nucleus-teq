from fastapi import FastAPI

from backend.exceptions import register_exception_handlers
from backend.routers.auth_router import router as auth_router
from backend.routers.platform_router import router as platform_router

app = FastAPI(
    title="Doctor Appointment Booking System Backend",
    version="1.0",
    description="Backend API with JWT authentication and role based access control.",
)
register_exception_handlers(app)
app.include_router(auth_router)
app.include_router(platform_router)


@app.get("/health", tags=["health"])
def health_check():
    return {"status": "ok", "message": "Backend is running"}
