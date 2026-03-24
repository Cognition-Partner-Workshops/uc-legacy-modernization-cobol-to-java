"""Daily transaction model — from CVTRA06Y.cpy (350-byte sequential records).

Same layout as CVTRA05Y but for daily batch input (DALYTRAN file).
These are validated and posted to the main transaction file by CBTRN02C.cbl.
"""

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class DailyTransaction(Base):
    __tablename__ = "daily_transactions"

    id: Mapped[int | None] = mapped_column(Integer, primary_key=True, autoincrement=True)
    tran_id: Mapped[str] = mapped_column(String(16), default="")
    tran_type_cd: Mapped[str] = mapped_column(String(2), default="")
    tran_cat_cd: Mapped[int] = mapped_column(Integer, default=0)
    tran_source: Mapped[str] = mapped_column(String(10), default="")
    tran_desc: Mapped[str] = mapped_column(String(100), default="")
    tran_amt: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
    merchant_id: Mapped[int] = mapped_column(Integer, default=0)
    merchant_name: Mapped[str] = mapped_column(String(50), default="")
    merchant_city: Mapped[str] = mapped_column(String(50), default="")
    merchant_zip: Mapped[str] = mapped_column(String(10), default="")
    card_num: Mapped[str] = mapped_column(String(16), default="")
    orig_ts: Mapped[str] = mapped_column(String(26), default="")
    proc_ts: Mapped[str] = mapped_column(String(26), default="")
