"""Card routes — from COCRDLIC.cbl (Card List), COCRDSLC.cbl (Card View), COCRDUPC.cbl (Card Update).

COCRDLIC: STARTBR/READNEXT/ENDBR on CARDDAT with pagination.
COCRDSLC: READ DATASET('CARDDAT') by CARD-NUM.
COCRDUPC: RECEIVE MAP, REWRITE DATASET('CARDDAT').
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.user_security import UserSecurity
from app.schemas.card import CardListResponse, CardResponse, CardUpdate
from app.services import card_service

router = APIRouter(prefix="/cards", tags=["cards"])


@router.get("", response_model=CardListResponse)
def list_cards(
    acct_id: int | None = None,
    page: int = 1,
    page_size: int = 10,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> CardListResponse:
    """List cards — replaces COCRDLIC STARTBR/READNEXT/ENDBR on CARDDAT."""
    cards, total = card_service.list_cards(db, acct_id, page, page_size)
    return CardListResponse(
        cards=[CardResponse.model_validate(c) for c in cards],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.get("/{card_num}", response_model=CardResponse)
def get_card(
    card_num: str,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> CardResponse:
    """View card — replaces COCRDSLC READ DATASET('CARDDAT')."""
    card = card_service.get_card(db, card_num)
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card not found")
    return CardResponse.model_validate(card)


@router.put("/{card_num}", response_model=CardResponse)
def update_card(
    card_num: str,
    update_data: CardUpdate,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> CardResponse:
    """Update card — replaces COCRDUPC REWRITE DATASET('CARDDAT')."""
    card = card_service.update_card(db, card_num, update_data)
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card not found")
    return CardResponse.model_validate(card)
