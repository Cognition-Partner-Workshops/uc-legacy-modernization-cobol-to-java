"""Customer model — from CVCUS01Y.cpy (500-byte VSAM KSDS records, key=CUST-ID 9 digits).

COBOL record layout:
  05 CUST-ID                    PIC 9(09)       → BigInteger PK
  05 CUST-FIRST-NAME            PIC X(25)       → String(25)
  05 CUST-MIDDLE-NAME           PIC X(25)       → String(25)
  05 CUST-LAST-NAME             PIC X(25)       → String(25)
  05 CUST-ADDR-LINE-1           PIC X(50)       → String(50)
  05 CUST-ADDR-LINE-2           PIC X(50)       → String(50)
  05 CUST-ADDR-LINE-3           PIC X(50)       → String(50)
  05 CUST-ADDR-STATE-CD         PIC X(02)       → String(2)
  05 CUST-ADDR-COUNTRY-CD       PIC X(03)       → String(3)
  05 CUST-ADDR-ZIP              PIC X(10)       → String(10)
  05 CUST-PHONE-NUM-1           PIC X(15)       → String(15)
  05 CUST-PHONE-NUM-2           PIC X(15)       → String(15)
  05 CUST-SSN                   PIC 9(09)       → BigInteger
  05 CUST-GOVT-ISSUED-ID        PIC X(20)       → String(20)
  05 CUST-DOB-YYYY-MM-DD        PIC X(10)       → String(10)
  05 CUST-EFT-ACCOUNT-ID        PIC X(10)       → String(10)
  05 CUST-PRI-CARD-HOLDER-IND   PIC X(01)       → String(1)
  05 CUST-FICO-CREDIT-SCORE     PIC 9(03)       → Integer
  05 FILLER                     PIC X(168)      → not stored
"""

from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class Customer(Base):
    __tablename__ = "customers"

    cust_id: Mapped[int] = mapped_column(BigInteger, primary_key=True)
    first_name: Mapped[str] = mapped_column(String(25), default="")
    middle_name: Mapped[str] = mapped_column(String(25), default="")
    last_name: Mapped[str] = mapped_column(String(25), default="")
    addr_line_1: Mapped[str] = mapped_column(String(50), default="")
    addr_line_2: Mapped[str] = mapped_column(String(50), default="")
    addr_line_3: Mapped[str] = mapped_column(String(50), default="")
    addr_state_cd: Mapped[str] = mapped_column(String(2), default="")
    addr_country_cd: Mapped[str] = mapped_column(String(3), default="")
    addr_zip: Mapped[str] = mapped_column(String(10), default="")
    phone_num_1: Mapped[str] = mapped_column(String(15), default="")
    phone_num_2: Mapped[str] = mapped_column(String(15), default="")
    ssn: Mapped[int] = mapped_column(BigInteger, default=0)
    govt_issued_id: Mapped[str] = mapped_column(String(20), default="")
    dob_yyyy_mm_dd: Mapped[str] = mapped_column(String(10), default="")
    eft_account_id: Mapped[str] = mapped_column(String(10), default="")
    pri_card_holder_ind: Mapped[str] = mapped_column(String(1), default="")
    fico_credit_score: Mapped[int] = mapped_column(Integer, default=0)
