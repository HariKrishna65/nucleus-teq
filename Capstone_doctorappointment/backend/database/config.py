from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    mongodb_url: str = "mongodb://localhost:27017"
    database_name: str = "doctor_appointment_db"
    jwt_secret_key: str = "change-this-secret-key"
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 30

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
