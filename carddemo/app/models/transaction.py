"""Transaction model — from CVTRA05Y.cpy (350-byte VSAM KSDS, key=TRAN-ID 16 chars).

COBOL record layout:
  05 TRAN-ID              PIC X(16)       → String(16) PK
  05 TRAN-TYPE-CD         PIC X(02)       → String(2)
  05 TRAN-CAT-CD          PIC 9(04)       → Integer
  05 TRAN-SOURCE          PIC X(10)       → String(10)
  05 TRAN-DESC            PIC X(100)      → String(100)
  05 TRAN-AMT             PIC S9(09)V99   → Numeric(11,2)
  05 TRAN-MERCHANT-ID     PIC 9(09)       → BigInteger
  05 TRAN-MERCHANT-NAME   PIC X(50)       → String(50)
  05 TRAN-MERCHANT-CITY   PIC X(50)       → String(50)
  05 TRAN-MERCHANT-ZIP    PIC X(10)       → String(10)
  05 TRAN-CARD-NUM        PIC X(16)       → String(16) FK
  05 TRAN-ORIG-TS         PIC X(26)       → String(26)
  05 TRAN-PROC-TS         PIC X(26)       → String(26)
  05 FILLER               PIC X(20)       → not stored
"""

from sqlalchemy import BigInteger, Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class Transaction(Base):
    __tablename__ = "transactions"

    tran_id: Mapped[str] = mapped_column(String(16), primary_key=True)
    tran_type_cd: Mapped[str] = mapped_column(String(2), default="")
    tran_cat_cd: Mapped[int] = mapped_column(Integer, default=0)
    tran_source: Mapped[str] = mapped_column(String(10), default="")
    tran_desc: Mapped[str] = mapped_column(String(100), default="")
    tran_amt: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
    merchant_id: Mapped[int] = mapped_column(BigInteger, default=0)
    merchant_name: Mapped[str] = mapped_column(String(50), default="")
    merchant_city: Mapped[str] = mapped_column(String(50), default="")
    merchant_zip: Mapped[str] = mapped_column(String(10), default="")
    card_num: Mapped[str] = mapped_column(String(16), default="")
    orig_ts: Mapped[str] = mapped_column(String(26), default="")
    proc_ts: Mapped[str] = mapped_column(String(26), default="")
