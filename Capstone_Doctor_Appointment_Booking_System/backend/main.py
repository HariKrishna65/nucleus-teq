from fastapi import FastAPI

app = FastAPI(title="Doctor Appointment Booking API", version="0.1.0")

@app.get("/health")
def health_check():
    return {"status": "ok", "message": "Backend is running"}
