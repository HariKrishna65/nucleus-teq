import os
from urllib.parse import quote_plus

from dotenv import load_dotenv

load_dotenv()


def build_mongodb_uri() -> str:
    uri = os.getenv("MONGODB_URI", "").strip()
    if uri:
        return uri

    username = os.getenv("MONGODB_USERNAME", "").strip()
    password = os.getenv("MONGODB_PASSWORD", "").strip()
    host = os.getenv("MONGODB_HOST", "localhost").strip()
    port = os.getenv("MONGODB_PORT", "27017").strip()
    use_srv = os.getenv("MONGODB_USE_SRV", "false").lower() == "true"

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
    return os.getenv("MONGODB_DB_NAME", "doctor_appointment")
