"""Transaction category balance model — from CVTRA01Y.cpy (50-byte VSAM KSDS).

COBOL record layout:
  05 TRAN-CAT-KEY
     10 TRANCAT-ACCT-ID    PIC 9(11)   → BigInteger
     10 TRANCAT-TYPE-CD    PIC X(02)   → String(2)
     10 TRANCAT-CD         PIC 9(04)   → Integer
  05 TRAN-CAT-BAL          PIC S9(09)V99 → Numeric(11,2)
  05 FILLER                PIC X(22)   → not stored

Composite primary key: (acct_id, type_cd, cat_cd)
"""

from sqlalchemy import BigInteger, Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class TranCategoryBalance(Base):
    __tablename__ = "tran_cat_balances"

    acct_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    type_cd: Mapped[str] = mapped_column(String(2), primary_key=True)
    cat_cd: Mapped[int] = mapped_column(Integer, primary_key=True)
    balance: Mapped[object] = mapped_column(Numeric(11, 2), default=0)
