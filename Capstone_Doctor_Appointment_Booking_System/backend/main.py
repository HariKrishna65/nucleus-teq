import sys
from pathlib import Path

from fastapi import FastAPI

sys.path.append(str(Path(__file__).resolve().parent.parent))

from backend.database import connect_to_mongo
from backend.routers.appointment_router import router as appointment_router
from backend.routers.user_router import router as user_router

app = FastAPI(title="Doctor Appointment Booking API", version="0.1.0")
app.include_router(user_router)
app.include_router(appointment_router)


@app.get("/health")
def health_check():
    _, _, db_status = connect_to_mongo()
    return {"status": "ok" if db_status["connected"] else "degraded", "message": "Backend is running", "database": db_status}
