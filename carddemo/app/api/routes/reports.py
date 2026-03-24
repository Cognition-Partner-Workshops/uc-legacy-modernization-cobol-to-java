"""Report routes — from CORPT00C.cbl (Transaction Reports).

CORPT00C: Collects filter criteria from BMS map, queries TRANSACT file,
formats report output to screen/printer.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.user_security import UserSecurity
from app.schemas.report import ReportRequest, ReportResponse
from app.services import report_service

router = APIRouter(prefix="/reports", tags=["reports"])


@router.post("/transactions", response_model=ReportResponse)
def transaction_report(
    request: ReportRequest,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> ReportResponse:
    """Generate transaction report — replaces CORPT00C report generation."""
    result = report_service.generate_transaction_report(
        db,
        acct_id=request.acct_id,
        card_num=request.card_num,
        start_date=request.start_date,
        end_date=request.end_date,
    )
    return ReportResponse(**result)
