from fastapi import FastAPI

app = FastAPI(title="Appointment Service", version="0.1.0")

@app.get("/health")
def health_check():
    return {"service": "appointment-service", "status": "ok"}
