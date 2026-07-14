from pydantic import BaseModel


class ActivationUpdate(BaseModel):
    active: bool
