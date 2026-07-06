from fastapi import FastAPI

from backend.routers.appointment_router import router as appointment_router
from backend.routers.user_router import router as user_router

app = FastAPI(title="Doctor Appointment Booking API", version="0.1.0")
app.include_router(user_router)
app.include_router(appointment_router)


@app.get("/health")
def health_check():
    return {"status": "ok", "message": "Backend is running"}
