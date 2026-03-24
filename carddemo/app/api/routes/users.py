"""User CRUD routes — from COUSR00C (List), COUSR01C (Add), COUSR02C (Update), COUSR03C (Delete).

All user management requires admin access (CDEMO-USRTYP-ADMIN).
COUSR00C: STARTBR/READNEXT/ENDBR on USRSEC → paginated user list.
COUSR01C: WRITE DATASET('USRSEC') → create new user.
COUSR02C: REWRITE DATASET('USRSEC') → update existing user.
COUSR03C: DELETE DATASET('USRSEC') → delete user.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import require_admin
from app.core.database import get_db
from app.core.security import get_password_hash
from app.models.user_security import UserSecurity
from app.schemas.user import UserCreate, UserListResponse, UserResponse, UserUpdate

router = APIRouter(prefix="/users", tags=["users"])


@router.get("", response_model=UserListResponse)
def list_users(
    page: int = 1,
    page_size: int = 10,
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> UserListResponse:
    """List users — replaces COUSR00C STARTBR/READNEXT/ENDBR on USRSEC."""
    total = db.query(UserSecurity).count()
    users = db.query(UserSecurity).order_by(UserSecurity.usr_id).offset((page - 1) * page_size).limit(page_size).all()
    return UserListResponse(
        users=[UserResponse.model_validate(u) for u in users],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.get("/{usr_id}", response_model=UserResponse)
def get_user(
    usr_id: str,
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> UserResponse:
    """Get user — replaces READ DATASET('USRSEC') RIDFLD."""
    user = db.query(UserSecurity).filter_by(usr_id=usr_id.upper()).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return UserResponse.model_validate(user)


@router.post("", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def create_user(
    data: UserCreate,
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> UserResponse:
    """Create user — replaces COUSR01C WRITE DATASET('USRSEC')."""
    usr_id = data.usr_id.upper()
    existing = db.query(UserSecurity).filter_by(usr_id=usr_id).first()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="User already exists")

    user = UserSecurity(
        usr_id=usr_id,
        usr_fname=data.usr_fname,
        usr_lname=data.usr_lname,
        usr_pwd=get_password_hash(data.usr_pwd.upper()),
        usr_type=data.usr_type,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return UserResponse.model_validate(user)


@router.put("/{usr_id}", response_model=UserResponse)
def update_user(
    usr_id: str,
    data: UserUpdate,
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> UserResponse:
    """Update user — replaces COUSR02C REWRITE DATASET('USRSEC')."""
    user = db.query(UserSecurity).filter_by(usr_id=usr_id.upper()).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    update_dict = data.model_dump(exclude_unset=True)
    if "usr_pwd" in update_dict:
        update_dict["usr_pwd"] = get_password_hash(update_dict["usr_pwd"].upper())
    for field, value in update_dict.items():
        setattr(user, field, value)
    db.commit()
    db.refresh(user)
    return UserResponse.model_validate(user)


@router.delete("/{usr_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_user(
    usr_id: str,
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> None:
    """Delete user — replaces COUSR03C DELETE DATASET('USRSEC')."""
    user = db.query(UserSecurity).filter_by(usr_id=usr_id.upper()).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    db.delete(user)
    db.commit()
