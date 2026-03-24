"""Authorization schemas — replaces COPAU00/01 BMS map fields and MQ messages."""

from decimal import Decimal

from pydantic import BaseModel


class PendingAuthSummaryResponse(BaseModel):
    id: int | None = None
    acct_id: int
    card_num: str
    pending_count: int
    pending_amount: Decimal
    status: str

    model_config = {"from_attributes": True}


class PendingAuthDetailResponse(BaseModel):
    id: int | None = None
    summary_id: int | None = None
    acct_id: int
    card_num: str
    tran_type_cd: str
    tran_amt: Decimal
    merchant_name: str
    auth_status: str
    orig_ts: str
    proc_ts: str

    model_config = {"from_attributes": True}


class AuthDecisionRequest(BaseModel):
    detail_id: int
    decision: str  # "APPROVE" or "DECLINE"
    reason: str = ""


class AuthDecisionResponse(BaseModel):
    detail_id: int
    decision: str
    reason: str
    message: str
