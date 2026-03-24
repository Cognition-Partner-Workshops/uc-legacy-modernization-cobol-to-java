"""Transaction category model — from CVTRA04Y.cpy / DB2 TRANSACTION_TYPE_CATEGORY table.

COBOL record layout (60 bytes):
  05 TRAN-CAT-KEY
     10 TRAN-TYPE-CD     PIC X(02)   → String(2)
     10 TRAN-CAT-CD      PIC 9(04)   → Integer
  05 TRAN-CAT-TYPE-DESC  PIC X(50)   → String(50)
  05 FILLER              PIC X(04)   → not stored

Composite primary key: (type_cd, cat_cd)
"""

from sqlalchemy import Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class TranCategory(Base):
    __tablename__ = "tran_categories"

    type_cd: Mapped[str] = mapped_column(String(2), primary_key=True)
    cat_cd: Mapped[int] = mapped_column(Integer, primary_key=True)
    description: Mapped[str] = mapped_column(String(50), default="")
