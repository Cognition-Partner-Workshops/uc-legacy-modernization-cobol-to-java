"""Authentication schemas — replaces COSGN00 BMS map fields."""

from pydantic import BaseModel


class LoginRequest(BaseModel):
    user_id: str
    password: str


class LoginResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    user_type: str


class TokenData(BaseModel):
    user_id: str
    user_type: str
