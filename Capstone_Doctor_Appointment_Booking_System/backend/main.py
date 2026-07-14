from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging
import time
from fastapi import Request

from backend.exceptions import register_exception_handlers
from backend.routers.auth_router import router as auth_router, profile_router
from backend.routers.doctor_router import router as doctor_router
from backend.routers.patient_router import router as patient_router
from backend.routers.admin_router import router as admin_router

app = FastAPI(
    title="Doctor Appointment Booking System Backend",
    version="1.0",
    description="Backend API for user authentication, doctor profiles, and appointment operations.",
)
register_exception_handlers(app)
app.add_middleware(CORSMiddleware, allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"])
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
logger = logging.getLogger("doctor_booking.api")

@app.middleware("http")
async def request_logging(request: Request, call_next):
    started = time.perf_counter()
    try:
        response = await call_next(request)
        logger.info("%s %s status=%s duration_ms=%.2f", request.method, request.url.path, response.status_code, (time.perf_counter()-started)*1000)
        return response
    except Exception:
        logger.exception("%s %s failed", request.method, request.url.path)
        raise

app.include_router(auth_router)
app.include_router(profile_router)
app.include_router(doctor_router)
app.include_router(patient_router)
app.include_router(admin_router)


@app.get("/health", tags=["health"])
def health_check():
    return {"status": "ok", "message": "Backend is running"}
