"""User schemas — replaces COUSR00-03 BMS map fields."""

from pydantic import BaseModel


class UserBase(BaseModel):
    usr_id: str
    usr_fname: str = ""
    usr_lname: str = ""
    usr_type: str = "U"


class UserResponse(UserBase):
    model_config = {"from_attributes": True}


class UserCreate(BaseModel):
    usr_id: str
    usr_fname: str
    usr_lname: str
    usr_pwd: str
    usr_type: str = "U"


class UserUpdate(BaseModel):
    usr_fname: str | None = None
    usr_lname: str | None = None
    usr_pwd: str | None = None
    usr_type: str | None = None


class UserListResponse(BaseModel):
    users: list[UserResponse]
    total: int
    page: int
    page_size: int
