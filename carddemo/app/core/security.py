"""JWT-based authentication — replaces RACF security and CICS ASSIGN USERID.

COBOL original: COSGN00C.cbl READ-USER-SEC-FILE paragraph.
The COBOL version reads USRSEC VSAM file by key (user ID) and compares passwords.
This module replaces that with JWT tokens and password hashing.

User types preserved from CSUSR01Y.cpy:
  - 'A' = Admin (CDEMO-USRTYP-ADMIN) → full access
  - 'U' = Regular user → own account only
"""

import hashlib
from datetime import UTC, datetime, timedelta

import bcrypt
from jose import JWTError, jwt

from app.core.config import settings


def _prepare_password(password: str) -> bytes:
    """Prepare password for bcrypt — handles the 72-byte limit by pre-hashing long passwords."""
    pwd_bytes = password.encode("utf-8")
    if len(pwd_bytes) > 72:
        pwd_bytes = hashlib.sha256(pwd_bytes).hexdigest().encode("utf-8")
    return pwd_bytes


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(_prepare_password(plain_password), hashed_password.encode("utf-8"))


def get_password_hash(password: str) -> str:
    return bcrypt.hashpw(_prepare_password(password), bcrypt.gensalt()).decode("utf-8")


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    """Create JWT token — replaces COMMAREA passing between CICS programs."""
    to_encode = data.copy()
    expire = datetime.now(UTC) + (expires_delta or timedelta(minutes=settings.access_token_expire_minutes))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)


def decode_access_token(token: str) -> dict | None:
    """Decode JWT token — replaces COMMAREA extraction."""
    try:
        return jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
    except JWTError:
        return None
