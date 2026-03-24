"""Pending authorization detail — from IMS CIPAUDTY segment (authorization-ims-db2-mq module).

Replaces IMS DL/I GNP calls for detail segments under a summary parent.
"""

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class PendingAuthDetail(Base):
    __tablename__ = "pending_auth_details"

    id: Mapped[int | None] = mapped_column(Integer, primary_key=True, autoincrement=True)
    summary_id: Mapped[int] = mapped_column(Integer, index=True)
    acct_id: Mapped[int] = mapped_column(Integer, index=True)
    card_num: Mapped[str] = mapped_column(String(16), default="")
    tran_type_cd: Mapped[str] = mapped_column(String(2), default="")
    tran_amt: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
    merchant_name: Mapped[str] = mapped_column(String(50), default="")
    auth_status: Mapped[str] = mapped_column(String(1), default="P")
    orig_ts: Mapped[str] = mapped_column(String(26), default="")
    proc_ts: Mapped[str] = mapped_column(String(26), default="")
