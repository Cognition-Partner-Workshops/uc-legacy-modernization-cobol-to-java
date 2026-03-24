"""Authorization fraud model — from DB2 AUTHFRDS table (authorization-ims-db2-mq module).

Records authorization decisions (approve/decline) made by COPAUA0C.cbl.
"""

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class AuthFraud(Base):
    __tablename__ = "auth_fraud"

    id: Mapped[int | None] = mapped_column(Integer, primary_key=True, autoincrement=True)
    acct_id: Mapped[int] = mapped_column(Integer, index=True)
    card_num: Mapped[str] = mapped_column(String(16), default="")
    tran_amt: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
    decision: Mapped[str] = mapped_column(String(10), default="")
    reason: Mapped[str] = mapped_column(String(100), default="")
    decision_ts: Mapped[str] = mapped_column(String(26), default="")
