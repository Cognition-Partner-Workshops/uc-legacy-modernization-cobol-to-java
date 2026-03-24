"""Card service — replaces COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl CICS programs.

COCRDLIC: STARTBR/READNEXT/ENDBR on CARDDAT → paginated queries
COCRDSLC: READ DATASET('CARDDAT') → filter_by().first()
COCRDUPC: REWRITE DATASET('CARDDAT') → merge + commit
"""

from sqlalchemy.orm import Session

from app.models.card import Card
from app.schemas.card import CardUpdate


def get_card(db: Session, card_num: str) -> Card | None:
    """READ DATASET('CARDDAT') RIDFLD(CARD-NUM) → filter_by().first()"""
    return db.query(Card).filter_by(card_num=card_num).first()


def list_cards(db: Session, acct_id: int | None = None, page: int = 1, page_size: int = 10) -> tuple[list[Card], int]:
    """STARTBR/READNEXT/ENDBR on CARDDAT → paginated query."""
    query = db.query(Card)
    if acct_id is not None:
        query = query.filter_by(acct_id=acct_id)
    total = query.count()
    cards = query.order_by(Card.card_num).offset((page - 1) * page_size).limit(page_size).all()
    return cards, total


def update_card(db: Session, card_num: str, update_data: CardUpdate) -> Card | None:
    """REWRITE DATASET('CARDDAT') → merge + commit."""
    card = db.query(Card).filter_by(card_num=card_num).first()
    if not card:
        return None
    for field, value in update_data.model_dump(exclude_unset=True).items():
        setattr(card, field, value)
    db.commit()
    db.refresh(card)
    return card
