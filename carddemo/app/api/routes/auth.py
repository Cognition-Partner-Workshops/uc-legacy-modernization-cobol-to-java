"""Login route — from COSGN00C.cbl (Signon Screen).

COBOL flow:
1. RECEIVE MAP('COSGN0A') — get user ID and password
2. FUNCTION UPPER-CASE(USERIDI) — uppercase the user ID
3. READ DATASET('USRSEC') RIDFLD(WS-USER-ID) — look up user
4. Compare SEC-USR-PWD with entered password
5. If match and CDEMO-USRTYP-ADMIN → XCTL to COADM01C (admin menu)
6. If match and regular user → XCTL to COMEN01C (main menu)
7. If no match → error message on signon screen
"""

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.security import create_access_token, get_password_hash, verify_password
from app.models.user_security import UserSecurity
from app.schemas.auth import LoginRequest, LoginResponse

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login", response_model=LoginResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)) -> LoginResponse:
    """Signon — replaces COSGN00C PROCESS-ENTER-KEY + READ-USER-SEC-FILE."""
    user_id = request.user_id.upper().strip()
    if not user_id:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Please enter User ID ...")

    password = request.password.upper().strip()
    if not password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Please enter Password ...")

    # READ DATASET('USRSEC') RIDFLD(WS-USER-ID)
    user = db.query(UserSecurity).filter_by(usr_id=user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found. Try again ...")

    # Compare passwords
    if not verify_password(password, user.usr_pwd):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Wrong Password. Try again ...")

    # Create JWT (replaces COMMAREA passing)
    token = create_access_token(data={"sub": user.usr_id, "user_type": user.usr_type})

    return LoginResponse(access_token=token, user_id=user.usr_id, user_type=user.usr_type)


@router.post("/login/form", response_model=LoginResponse)
def login_form(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)) -> LoginResponse:
    """OAuth2 compatible login for Swagger UI."""
    user_id = form_data.username.upper().strip()
    user = db.query(UserSecurity).filter_by(usr_id=user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
    if not verify_password(form_data.password.upper().strip(), user.usr_pwd):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Wrong Password")

    token = create_access_token(data={"sub": user.usr_id, "user_type": user.usr_type})
    return LoginResponse(access_token=token, user_id=user.usr_id, user_type=user.usr_type)


@router.post("/seed-admin")
def seed_admin(db: Session = Depends(get_db)) -> dict:
    """Seed default admin user — replaces DUSRSECJ JCL job for initial user setup."""
    existing = db.query(UserSecurity).filter_by(usr_id="ADMIN001").first()
    if existing:
        return {"message": "Admin user already exists"}

    admin = UserSecurity(
        usr_id="ADMIN001",
        usr_fname="ADMIN",
        usr_lname="USER",
        usr_pwd=get_password_hash("PASSWORD"),
        usr_type="A",
    )
    regular = UserSecurity(
        usr_id="USER0001",
        usr_fname="REGULAR",
        usr_lname="USER",
        usr_pwd=get_password_hash("PASSWORD"),
        usr_type="U",
    )
    db.add(admin)
    db.add(regular)
    db.commit()
    return {"message": "Default users created: ADMIN001 (admin), USER0001 (regular)"}
