"""API dependencies — replaces CICS COMMAREA passing and RACF security checks.

The COBOL programs extract user info from COMMAREA (CDEMO-USER-ID, CDEMO-USER-TYPE).
This module extracts the same from JWT tokens.
"""

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.security import decode_access_token
from app.models.user_security import UserSecurity

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login")


def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> UserSecurity:
    """Extract current user from JWT — replaces COMMAREA CDEMO-USER-ID extraction."""
    payload = decode_access_token(token)
    if payload is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")

    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")

    user = db.query(UserSecurity).filter_by(usr_id=user_id).first()
    if user is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")

    return user


def require_admin(current_user: UserSecurity = Depends(get_current_user)) -> UserSecurity:
    """Enforce admin access — replaces COBOL 88-level CDEMO-USRTYP-ADMIN check.

    From COSGN00C.cbl: IF CDEMO-USRTYP-ADMIN → XCTL to COADM01C
    """
    if current_user.usr_type != "A":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")
    return current_user
