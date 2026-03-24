"""Billing schemas — replaces COBIL00 BMS map fields."""

from decimal import Decimal

from pydantic import BaseModel


class BillPaymentRequest(BaseModel):
    acct_id: int
    payment_amount: Decimal


class BillPaymentResponse(BaseModel):
    acct_id: int
    payment_amount: str
    new_balance: str
    message: str
