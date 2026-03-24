"""Transaction schemas — replaces COTRN00/01/02 BMS map fields."""

from decimal import Decimal

from pydantic import BaseModel


class TransactionBase(BaseModel):
    tran_id: str
    tran_type_cd: str = ""
    tran_cat_cd: int = 0
    tran_source: str = ""
    tran_desc: str = ""
    tran_amt: Decimal = Decimal("0.00")
    merchant_id: int = 0
    merchant_name: str = ""
    merchant_city: str = ""
    merchant_zip: str = ""
    card_num: str = ""
    orig_ts: str = ""
    proc_ts: str = ""


class TransactionResponse(TransactionBase):
    model_config = {"from_attributes": True}


class TransactionCreate(BaseModel):
    """Fields for adding a new transaction — replaces COTRN02 RECEIVE MAP."""

    tran_type_cd: str
    tran_cat_cd: int
    tran_source: str = ""
    tran_desc: str = ""
    tran_amt: Decimal
    merchant_id: int = 0
    merchant_name: str = ""
    merchant_city: str = ""
    merchant_zip: str = ""
    card_num: str


class TransactionListResponse(BaseModel):
    transactions: list[TransactionResponse]
    total: int
    page: int
    page_size: int
