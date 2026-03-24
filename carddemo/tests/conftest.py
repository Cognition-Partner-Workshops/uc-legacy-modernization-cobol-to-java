"""Test configuration and fixtures.

Uses an in-memory SQLite database for testing (replaces PostgreSQL).
Creates all tables and seeds test data before each test.
"""

from collections.abc import Generator
from decimal import Decimal

import pytest
from app.core.database import Base, get_db
from app.core.security import create_access_token, get_password_hash
from app.main import app
from app.models.account import Account
from app.models.card import Card
from app.models.card_xref import CardXref
from app.models.customer import Customer
from app.models.daily_transaction import DailyTransaction
from app.models.disclosure_group import DisclosureGroup
from app.models.pending_auth_detail import PendingAuthDetail
from app.models.pending_auth_summary import PendingAuthSummary
from app.models.tran_category_balance import TranCategoryBalance
from app.models.transaction import Transaction
from app.models.user_security import UserSecurity
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

SQLALCHEMY_DATABASE_URL = "sqlite://"

engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}, poolclass=StaticPool)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture()
def db() -> Generator[Session, None, None]:
    Base.metadata.create_all(bind=engine)
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture()
def client(db: Session) -> Generator[TestClient, None, None]:
    def override_get_db() -> Generator[Session, None, None]:
        try:
            yield db
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()


@pytest.fixture()
def admin_token() -> str:
    return create_access_token(data={"sub": "ADMIN001", "user_type": "A"})


@pytest.fixture()
def user_token() -> str:
    return create_access_token(data={"sub": "USER0001", "user_type": "U"})


@pytest.fixture()
def seed_users(db: Session) -> None:
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


@pytest.fixture()
def seed_data(db: Session, seed_users: None) -> None:
    """Seed comprehensive test data matching COBOL VSAM files."""
    # Accounts
    acct1 = Account(
        acct_id=1,
        active_status="Y",
        curr_bal=Decimal("1940.00"),
        credit_limit=Decimal("20200.00"),
        cash_credit_limit=Decimal("10200.00"),
        open_date="2014-11-20",
        expiration_date="2030-05-20",
        reissue_date="2030-05-20",
        curr_cyc_credit=Decimal("0.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="",
        group_id="A000000000",
    )
    acct2 = Account(
        acct_id=2,
        active_status="Y",
        curr_bal=Decimal("1580.00"),
        credit_limit=Decimal("61300.00"),
        cash_credit_limit=Decimal("54480.00"),
        open_date="2013-06-19",
        expiration_date="2030-08-11",
        reissue_date="2030-08-11",
        curr_cyc_credit=Decimal("0.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="",
        group_id="A000000000",
    )
    db.add_all([acct1, acct2])

    # Customer
    cust1 = Customer(
        cust_id=1,
        first_name="John",
        middle_name="M",
        last_name="Smith",
        addr_line_1="123 Main St",
        addr_state_cd="NY",
        addr_country_cd="US",
        addr_zip="10001",
        phone_num_1="212-555-0001",
        ssn=123456789,
        dob_yyyy_mm_dd="1980-01-15",
        pri_card_holder_ind="Y",
        fico_credit_score=750,
    )
    db.add(cust1)

    # Cards
    card1 = Card(
        card_num="4111111111111111",
        acct_id=1,
        cvv_cd=123,
        embossed_name="JOHN M SMITH",
        expiration_date="2030-12-31",
        active_status="Y",
    )
    card2 = Card(
        card_num="4222222222222222",
        acct_id=2,
        cvv_cd=456,
        embossed_name="JANE DOE",
        expiration_date="2030-06-30",
        active_status="Y",
    )
    db.add_all([card1, card2])

    # Card cross-references
    xref1 = CardXref(card_num="4111111111111111", cust_id=1, acct_id=1)
    xref2 = CardXref(card_num="4222222222222222", cust_id=1, acct_id=2)
    db.add_all([xref1, xref2])

    # Transaction category balances
    tcat1 = TranCategoryBalance(acct_id=1, type_cd="01", cat_cd=1, balance=Decimal("500.00"))
    db.add(tcat1)

    # Disclosure groups (interest rates)
    disc1 = DisclosureGroup(group_id="A000000000", tran_type_cd="01", tran_cat_cd=1, interest_rate=Decimal("18.00"))
    disc_default = DisclosureGroup(group_id="DEFAULT", tran_type_cd="01", tran_cat_cd=1, interest_rate=Decimal("12.00"))
    db.add_all([disc1, disc_default])

    # Transactions
    tran1 = Transaction(
        tran_id="0000000000000001",
        tran_type_cd="01",
        tran_cat_cd=1,
        tran_source="Online",
        tran_desc="Purchase at Store A",
        tran_amt=Decimal("50.00"),
        merchant_id=100,
        merchant_name="Store A",
        merchant_city="New York",
        merchant_zip="10001",
        card_num="4111111111111111",
        orig_ts="2024-01-15-10.30.00.000000",
        proc_ts="2024-01-15-10.30.01.000000",
    )
    db.add(tran1)

    # Daily transactions
    daily1 = DailyTransaction(
        tran_id="DT00000000000001",
        tran_type_cd="01",
        tran_cat_cd=1,
        tran_source="POS",
        tran_desc="Purchase at Store B",
        tran_amt=Decimal("75.50"),
        merchant_id=200,
        merchant_name="Store B",
        merchant_city="Boston",
        merchant_zip="02101",
        card_num="4111111111111111",
        orig_ts="2024-01-20-14.00.00.000000",
    )
    db.add(daily1)

    # Pending auth summary (let SQLite auto-generate id)
    auth_summary = PendingAuthSummary(
        acct_id=1,
        card_num="4111111111111111",
        pending_count=1,
        pending_amount=Decimal("100.00"),
        status="P",
    )
    db.add(auth_summary)

    # Flush to get auto-generated summary id
    db.flush()

    # Pending auth detail (let SQLite auto-generate id)
    auth_detail = PendingAuthDetail(
        summary_id=auth_summary.id,
        acct_id=1,
        card_num="4111111111111111",
        tran_type_cd="01",
        tran_amt=Decimal("100.00"),
        merchant_name="Store C",
        auth_status="P",
        orig_ts="2024-01-20-15.00.00.000000",
    )
    db.add(auth_detail)

    db.commit()
