"""Alembic environment configuration."""

import os
from logging.config import fileConfig

from app.core.database import Base
from app.models.account import Account  # noqa: F401
from app.models.auth_fraud import AuthFraud  # noqa: F401
from app.models.card import Card  # noqa: F401
from app.models.card_xref import CardXref  # noqa: F401
from app.models.customer import Customer  # noqa: F401
from app.models.daily_transaction import DailyTransaction  # noqa: F401
from app.models.disclosure_group import DisclosureGroup  # noqa: F401
from app.models.pending_auth_detail import PendingAuthDetail  # noqa: F401
from app.models.pending_auth_summary import PendingAuthSummary  # noqa: F401
from app.models.tran_category import TranCategory  # noqa: F401
from app.models.tran_category_balance import TranCategoryBalance  # noqa: F401
from app.models.transaction import Transaction  # noqa: F401
from app.models.transaction_type import TransactionType  # noqa: F401
from app.models.user_security import UserSecurity  # noqa: F401
from sqlalchemy import create_engine

from alembic import context

config = context.config

if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata

# Override sqlalchemy.url from environment variable if set
db_url = os.environ.get("CARDDEMO_DATABASE_URL")
if db_url:
    config.set_main_option("sqlalchemy.url", db_url)


def run_migrations_offline() -> None:
    """Run migrations in 'offline' mode."""
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url, target_metadata=target_metadata, literal_binds=True, dialect_opts={"paramstyle": "named"}
    )
    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """Run migrations in 'online' mode."""
    url = config.get_main_option("sqlalchemy.url")
    connectable = create_engine(url)
    with connectable.connect() as connection:
        context.configure(connection=connection, target_metadata=target_metadata)
        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
