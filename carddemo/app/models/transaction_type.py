"""Transaction type model — from CVTRA03Y.cpy / DB2 TRANSACTION_TYPE table.

COBOL record layout (60 bytes):
  05 TRAN-TYPE       PIC X(02)   → String(2) PK
  05 TRAN-TYPE-DESC  PIC X(50)   → String(50)
  05 FILLER          PIC X(08)   → not stored

Managed via CICS programs COTRTLIC/COTRTUPC with DB2 SQL.
"""

from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class TransactionType(Base):
    __tablename__ = "transaction_types"

    tran_type: Mapped[str] = mapped_column(String(2), primary_key=True)
    tran_type_desc: Mapped[str] = mapped_column(String(50), default="")
