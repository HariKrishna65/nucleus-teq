from beanie import init_beanie
from motor.motor_asyncio import AsyncIOMotorClient

from backend.database.config import settings
from backend.models.user import User


mongo_client: AsyncIOMotorClient | None = None


async def init_db() -> None:
    global mongo_client
    mongo_client = AsyncIOMotorClient(settings.mongodb_url)
    database = mongo_client[settings.database_name]
    await init_beanie(database=database, document_models=[User])


async def close_db() -> None:
    if mongo_client is not None:
        mongo_client.close()
