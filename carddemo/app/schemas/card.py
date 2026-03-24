"""Card schemas — replaces COCRDLI/COCRDSL/COCRDUP BMS map fields."""

from pydantic import BaseModel


class CardBase(BaseModel):
    card_num: str
    acct_id: int
    cvv_cd: int = 0
    embossed_name: str = ""
    expiration_date: str = ""
    active_status: str = "Y"


class CardResponse(CardBase):
    model_config = {"from_attributes": True}


class CardUpdate(BaseModel):
    embossed_name: str | None = None
    expiration_date: str | None = None
    active_status: str | None = None


class CardListResponse(BaseModel):
    cards: list[CardResponse]
    total: int
    page: int
    page_size: int
