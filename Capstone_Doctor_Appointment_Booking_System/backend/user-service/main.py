from fastapi import FastAPI

app = FastAPI(title="User Service", version="0.1.0")

@app.get("/health")
def health_check():
    return {"service": "user-service", "status": "ok"}
