import os
from pathlib import Path
from urllib.parse import quote_plus

from dotenv import load_dotenv
from pymongo import MongoClient

load_dotenv(dotenv_path=Path(__file__).resolve().parent / ".env")


def _get_env(*names: str, default: str = "") -> str:
    for name in names:
        value = os.getenv(name, "").strip()
        if value:
            return value
    return default


def build_mongodb_uri() -> str:
    uri = _get_env("MONGODB_URI", "MONGO_URI", "DATABASE_URL", "MONGODB_CONNECTION_STRING")
    if uri:
        return uri

    username = _get_env("MONGODB_USERNAME", "MONGO_USERNAME")
    password = _get_env("MONGODB_PASSWORD", "MONGO_PASSWORD")
    host = _get_env("MONGODB_HOST", "MONGO_HOST", default="localhost")
    port = _get_env("MONGODB_PORT", "MONGO_PORT", default="27017")
    use_srv = _get_env("MONGODB_USE_SRV", "MONGO_USE_SRV", default="false").lower() == "true"

    if username and password:
        encoded_username = quote_plus(username)
        encoded_password = quote_plus(password)
        if use_srv:
            return f"mongodb+srv://{encoded_username}:{encoded_password}@{host}/?retryWrites=true&w=majority"
        return f"mongodb://{encoded_username}:{encoded_password}@{host}:{port}/"

    if use_srv:
        return f"mongodb+srv://{host}/?retryWrites=true&w=majority"
    return f"mongodb://{host}:{port}/"


def get_database_name() -> str:
    return _get_env("MONGODB_DB_NAME", "MONGO_DB_NAME", "DB_NAME", default="doctor_appointment")


def connect_to_mongo():
    uri = build_mongodb_uri()
    db_name = get_database_name()

    try:
        client = MongoClient(uri, serverSelectionTimeoutMS=5000)
        client.admin.command("ping")
        db = client[db_name]
        return client, db, {"connected": True, "database": db_name, "uri_configured": bool(uri)}
    except Exception as exc:
        return None, None, {"connected": False, "database": db_name, "error": str(exc), "uri_configured": bool(uri)}
