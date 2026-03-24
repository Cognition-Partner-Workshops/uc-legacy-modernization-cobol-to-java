"""Account schemas — replaces COACTVW/COACTUP BMS map fields."""

from decimal import Decimal

from pydantic import BaseModel


class AccountBase(BaseModel):
    acct_id: int
    active_status: str = "Y"
    curr_bal: Decimal = Decimal("0.00")
    credit_limit: Decimal = Decimal("0.00")
    cash_credit_limit: Decimal = Decimal("0.00")
    open_date: str = ""
    expiration_date: str = ""
    reissue_date: str = ""
    curr_cyc_credit: Decimal = Decimal("0.00")
    curr_cyc_debit: Decimal = Decimal("0.00")
    addr_zip: str = ""
    group_id: str = ""


class AccountResponse(AccountBase):
    model_config = {"from_attributes": True}


class AccountUpdate(BaseModel):
    active_status: str | None = None
    credit_limit: Decimal | None = None
    cash_credit_limit: Decimal | None = None
    expiration_date: str | None = None
    reissue_date: str | None = None
    addr_zip: str | None = None
    group_id: str | None = None


class AccountListResponse(BaseModel):
    accounts: list[AccountResponse]
    total: int
    page: int
    page_size: int
