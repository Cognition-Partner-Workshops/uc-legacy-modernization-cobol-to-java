"""Report schemas — replaces CORPT00 BMS map fields."""

from pydantic import BaseModel


class ReportRequest(BaseModel):
    acct_id: int | None = None
    card_num: str | None = None
    start_date: str | None = None
    end_date: str | None = None


class ReportResponse(BaseModel):
    report_data: list[dict]
    total_records: int
    total_amount: str
