"""Authorization routes — from COPAUS0C (Summary), COPAUS1C (Details), COPAUA0C (Process).

COPAUS0C: Reads IMS CIPAUSMY segments for pending auth summary → GET /authorizations
COPAUS1C: Reads IMS CIPAUDTY segments for pending auth details → GET /authorizations/{id}/details
COPAUA0C: MQ-triggered authorization processing → POST /authorizations/decide
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.pending_auth_detail import PendingAuthDetail
from app.models.pending_auth_summary import PendingAuthSummary
from app.models.user_security import UserSecurity
from app.schemas.authorization import (
    AuthDecisionRequest,
    AuthDecisionResponse,
    PendingAuthDetailResponse,
    PendingAuthSummaryResponse,
)
from app.services import auth_decision_service

router = APIRouter(prefix="/authorizations", tags=["authorizations"])


@router.get("", response_model=list[PendingAuthSummaryResponse])
def list_pending_authorizations(
    acct_id: int | None = None,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> list[PendingAuthSummaryResponse]:
    """List pending authorization summaries — replaces COPAUS0C IMS GU/GNP calls."""
    query = db.query(PendingAuthSummary)
    if acct_id:
        query = query.filter_by(acct_id=acct_id)
    summaries = query.order_by(PendingAuthSummary.id).all()
    return [PendingAuthSummaryResponse.model_validate(s) for s in summaries]


@router.get("/{summary_id}/details", response_model=list[PendingAuthDetailResponse])
def get_authorization_details(
    summary_id: int,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> list[PendingAuthDetailResponse]:
    """Get pending authorization details — replaces COPAUS1C IMS GNP calls."""
    details = db.query(PendingAuthDetail).filter_by(summary_id=summary_id).order_by(PendingAuthDetail.id).all()
    if not details:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No details found for this authorization")
    return [PendingAuthDetailResponse.model_validate(d) for d in details]


@router.post("/decide", response_model=AuthDecisionResponse)
def decide_authorization(
    request: AuthDecisionRequest,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> AuthDecisionResponse:
    """Process authorization decision — replaces COPAUA0C 6000-MAKE-DECISION."""
    result = auth_decision_service.make_decision(db, request.detail_id, request.decision, request.reason)
    if not result.get("success"):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=result.get("message", "Error"))
    return AuthDecisionResponse(
        detail_id=request.detail_id,
        decision=result["decision"],
        reason=result["reason"],
        message=f"Authorization {result['decision'].lower()}",
    )
