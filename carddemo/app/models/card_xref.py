"""Card cross-reference model — from CVACT03Y.cpy (50-byte VSAM KSDS, key=CARD-NUM 16 chars).

COBOL record layout:
  05 XREF-CARD-NUM   PIC X(16)   → String(16) PK
  05 XREF-CUST-ID    PIC 9(09)   → BigInteger FK
  05 XREF-ACCT-ID    PIC 9(11)   → BigInteger FK
  05 FILLER          PIC X(14)   → not stored

Alternate index on XREF-ACCT-ID replicated as a DB index.
"""

from sqlalchemy import BigInteger, ForeignKey, Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class CardXref(Base):
    __tablename__ = "card_xref"

    card_num: Mapped[str] = mapped_column(String(16), primary_key=True)
    cust_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("customers.cust_id"))
    acct_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("accounts.acct_id"))

    __table_args__ = (Index("ix_card_xref_acct_id", "acct_id"),)
