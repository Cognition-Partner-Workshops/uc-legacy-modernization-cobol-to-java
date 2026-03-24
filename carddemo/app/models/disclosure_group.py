"""Disclosure group model — from CVTRA02Y.cpy (50-byte VSAM KSDS).

COBOL record layout:
  05 DIS-GROUP-KEY
     10 DIS-ACCT-GROUP-ID  PIC X(10)   → String(10)
     10 DIS-TRAN-TYPE-CD   PIC X(02)   → String(2)
     10 DIS-TRAN-CAT-CD    PIC 9(04)   → Integer
  05 DIS-INT-RATE          PIC S9(04)V99 → Numeric(6,2)
  05 FILLER                PIC X(28)   → not stored

Used by CBACT04C.cbl to look up interest rates for each transaction category.
Composite primary key: (group_id, tran_type_cd, tran_cat_cd)
"""

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class DisclosureGroup(Base):
    __tablename__ = "disclosure_groups"

    group_id: Mapped[str] = mapped_column(String(10), primary_key=True)
    tran_type_cd: Mapped[str] = mapped_column(String(2), primary_key=True)
    tran_cat_cd: Mapped[int] = mapped_column(Integer, primary_key=True)
    interest_rate: Mapped[object] = mapped_column(Numeric(6, 2), default=0)
