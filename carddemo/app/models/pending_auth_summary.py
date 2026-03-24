"""Pending authorization summary — from IMS CIPAUSMY segment (authorization-ims-db2-mq module).

Replaces IMS DL/I GU/GNP calls with SQLAlchemy queries.
This stores the summary view of pending authorizations per account.
"""

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class PendingAuthSummary(Base):
    __tablename__ = "pending_auth_summary"

    id: Mapped[int | None] = mapped_column(Integer, primary_key=True, autoincrement=True)
    acct_id: Mapped[int] = mapped_column(Integer, index=True)
    card_num: Mapped[str] = mapped_column(String(16), default="")
    pending_count: Mapped[int] = mapped_column(Integer, default=0)
    pending_amount: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
    status: Mapped[str] = mapped_column(String(1), default="P")
