import os
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None

if load_dotenv:
    load_dotenv(Path(__file__).resolve().parent / ".env")


def _required_env(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"{name} must be configured in backend/.env")
    return value


SECRET_KEY = os.getenv("JWT_SECRET_KEY") or _required_env("SECRET_KEY")
ALGORITHM = _required_env("JWT_ALGORITHM")
ACCESS_TOKEN_EXPIRE_MINUTES = int(_required_env("ACCESS_TOKEN_EXPIRE_MINUTES"))
