"""Card model — from CVACT02Y.cpy (150-byte VSAM KSDS records, key=CARD-NUM 16 chars).

COBOL record layout:
  05 CARD-NUM              PIC X(16)       → String(16) primary key
  05 CARD-ACCT-ID          PIC 9(11)       → BigInteger FK
  05 CARD-CVV-CD           PIC 9(03)       → Integer
  05 CARD-EMBOSSED-NAME    PIC X(50)       → String(50)
  05 CARD-EXPIRAION-DATE   PIC X(10)       → String(10)
  05 CARD-ACTIVE-STATUS    PIC X(01)       → String(1)
  05 FILLER                PIC X(59)       → not stored
"""

from sqlalchemy import BigInteger, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class Card(Base):
    __tablename__ = "cards"

    card_num: Mapped[str] = mapped_column(String(16), primary_key=True)
    acct_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("accounts.acct_id"))
    cvv_cd: Mapped[int] = mapped_column(Integer, default=0)
    embossed_name: Mapped[str] = mapped_column(String(50), default="")
    expiration_date: Mapped[str] = mapped_column(String(10), default="")
    active_status: Mapped[str] = mapped_column(String(1), default="Y")
