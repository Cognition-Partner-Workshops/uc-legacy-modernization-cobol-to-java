"""User security model — from CSUSR01Y.cpy (80-byte VSAM KSDS, key=SEC-USR-ID 8 chars).

COBOL record layout:
  05 SEC-USR-ID      PIC X(08)   → String(8) PK
  05 SEC-USR-FNAME   PIC X(20)   → String(20)
  05 SEC-USR-LNAME   PIC X(20)   → String(20)
  05 SEC-USR-PWD     PIC X(08)   → stored as bcrypt hash (String(128))
  05 SEC-USR-TYPE    PIC X(01)   → String(1)  'A'=admin, 'U'=regular
  05 SEC-USR-FILLER  PIC X(23)   → not stored

COBOL 88-level conditions:
  88 CDEMO-USRTYP-ADMIN VALUE 'A'  → UserType.ADMIN
  88 CDEMO-USRTYP-USER  VALUE 'U'  → UserType.USER
"""

import enum

from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class UserType(str, enum.Enum):
    ADMIN = "A"
    USER = "U"


class UserSecurity(Base):
    __tablename__ = "user_security"

    usr_id: Mapped[str] = mapped_column(String(8), primary_key=True)
    usr_fname: Mapped[str] = mapped_column(String(20), default="")
    usr_lname: Mapped[str] = mapped_column(String(20), default="")
    usr_pwd: Mapped[str] = mapped_column(String(128), default="")
    usr_type: Mapped[str] = mapped_column(String(1), default="U")
