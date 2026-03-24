"""Account model — from CVACT01Y.cpy (300-byte VSAM KSDS records, key=ACCT-ID 11 digits).

COBOL record layout:
  05 ACCT-ID                PIC 9(11)       → BigInteger primary key
  05 ACCT-ACTIVE-STATUS     PIC X(01)       → String(1)
  05 ACCT-CURR-BAL          PIC S9(10)V99   → Numeric(12,2)
  05 ACCT-CREDIT-LIMIT      PIC S9(10)V99   → Numeric(12,2)
  05 ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   → Numeric(12,2)
  05 ACCT-OPEN-DATE         PIC X(10)       → String(10)
  05 ACCT-EXPIRAION-DATE    PIC X(10)       → String(10)
  05 ACCT-REISSUE-DATE      PIC X(10)       → String(10)
  05 ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   → Numeric(12,2)
  05 ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   → Numeric(12,2)
  05 ACCT-ADDR-ZIP          PIC X(10)       → String(10)
  05 ACCT-GROUP-ID          PIC X(10)       → String(10)
  05 FILLER                 PIC X(178)      → not stored
"""

from sqlalchemy import BigInteger, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class Account(Base):
    __tablename__ = "accounts"

    acct_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    active_status: Mapped[str] = mapped_column(String(1), default="Y")
    curr_bal: Mapped[object] = mapped_column(Numeric(12, 2), default=0)
    credit_limit: Mapped[object] = mapped_column(Numeric(12, 2), default=0)
    cash_credit_limit: Mapped[object] = mapped_column(Numeric(12, 2), default=0)
    open_date: Mapped[str] = mapped_column(String(10), default="")
    expiration_date: Mapped[str] = mapped_column(String(10), default="")
    reissue_date: Mapped[str] = mapped_column(String(10), default="")
    curr_cyc_credit: Mapped[object] = mapped_column(Numeric(12, 2), default=0)
    curr_cyc_debit: Mapped[object] = mapped_column(Numeric(12, 2), default=0)
    addr_zip: Mapped[str] = mapped_column(String(10), default="")
    group_id: Mapped[str] = mapped_column(String(10), default="")
