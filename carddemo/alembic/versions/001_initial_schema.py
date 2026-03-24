"""Initial schema — all tables from COBOL VSAM/DB2/IMS data structures.

Revision ID: 001
Revises: None
Create Date: 2024-01-01 00:00:00.000000
"""

from typing import Sequence, Union

import sqlalchemy as sa

from alembic import op

revision: str = "001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # accounts — from CVACT01Y.cpy (VSAM KSDS, 300-byte records)
    op.create_table(
        "accounts",
        sa.Column("acct_id", sa.BigInteger(), primary_key=True),
        sa.Column("active_status", sa.String(1), server_default="Y"),
        sa.Column("curr_bal", sa.Numeric(12, 2), server_default="0"),
        sa.Column("credit_limit", sa.Numeric(12, 2), server_default="0"),
        sa.Column("cash_credit_limit", sa.Numeric(12, 2), server_default="0"),
        sa.Column("open_date", sa.String(10), server_default=""),
        sa.Column("expiration_date", sa.String(10), server_default=""),
        sa.Column("reissue_date", sa.String(10), server_default=""),
        sa.Column("curr_cyc_credit", sa.Numeric(12, 2), server_default="0"),
        sa.Column("curr_cyc_debit", sa.Numeric(12, 2), server_default="0"),
        sa.Column("addr_zip", sa.String(10), server_default=""),
        sa.Column("group_id", sa.String(10), server_default=""),
    )

    # customers — from CVCUS01Y.cpy (VSAM KSDS, 500-byte records)
    op.create_table(
        "customers",
        sa.Column("cust_id", sa.BigInteger(), primary_key=True),
        sa.Column("first_name", sa.String(25), server_default=""),
        sa.Column("middle_name", sa.String(25), server_default=""),
        sa.Column("last_name", sa.String(25), server_default=""),
        sa.Column("addr_line_1", sa.String(50), server_default=""),
        sa.Column("addr_line_2", sa.String(50), server_default=""),
        sa.Column("addr_line_3", sa.String(50), server_default=""),
        sa.Column("addr_state_cd", sa.String(2), server_default=""),
        sa.Column("addr_country_cd", sa.String(3), server_default=""),
        sa.Column("addr_zip", sa.String(10), server_default=""),
        sa.Column("phone_num_1", sa.String(15), server_default=""),
        sa.Column("phone_num_2", sa.String(15), server_default=""),
        sa.Column("ssn", sa.BigInteger(), server_default="0"),
        sa.Column("govt_issued_id", sa.String(20), server_default=""),
        sa.Column("dob_yyyy_mm_dd", sa.String(10), server_default=""),
        sa.Column("eft_account_id", sa.String(10), server_default=""),
        sa.Column("pri_card_holder_ind", sa.String(1), server_default=""),
        sa.Column("fico_credit_score", sa.Integer(), server_default="0"),
    )

    # cards — from CVACT02Y.cpy (VSAM KSDS, 150-byte records)
    op.create_table(
        "cards",
        sa.Column("card_num", sa.String(16), primary_key=True),
        sa.Column("acct_id", sa.BigInteger(), sa.ForeignKey("accounts.acct_id")),
        sa.Column("cvv_cd", sa.Integer(), server_default="0"),
        sa.Column("embossed_name", sa.String(50), server_default=""),
        sa.Column("expiration_date", sa.String(10), server_default=""),
        sa.Column("active_status", sa.String(1), server_default="Y"),
    )

    # card_xref — from CVACT03Y.cpy (VSAM KSDS, 50-byte records)
    op.create_table(
        "card_xref",
        sa.Column("card_num", sa.String(16), primary_key=True),
        sa.Column("cust_id", sa.BigInteger(), sa.ForeignKey("customers.cust_id")),
        sa.Column("acct_id", sa.BigInteger(), sa.ForeignKey("accounts.acct_id")),
    )
    op.create_index("ix_card_xref_acct_id", "card_xref", ["acct_id"])

    # transactions — from CVTRA05Y.cpy (VSAM KSDS, 350-byte records)
    op.create_table(
        "transactions",
        sa.Column("tran_id", sa.String(16), primary_key=True),
        sa.Column("tran_type_cd", sa.String(2), server_default=""),
        sa.Column("tran_cat_cd", sa.Integer(), server_default="0"),
        sa.Column("tran_source", sa.String(10), server_default=""),
        sa.Column("tran_desc", sa.String(100), server_default=""),
        sa.Column("tran_amt", sa.Numeric(11, 2), server_default="0"),
        sa.Column("merchant_id", sa.BigInteger(), server_default="0"),
        sa.Column("merchant_name", sa.String(50), server_default=""),
        sa.Column("merchant_city", sa.String(50), server_default=""),
        sa.Column("merchant_zip", sa.String(10), server_default=""),
        sa.Column("card_num", sa.String(16), server_default=""),
        sa.Column("orig_ts", sa.String(26), server_default=""),
        sa.Column("proc_ts", sa.String(26), server_default=""),
    )

    # daily_transactions — from CVTRA06Y.cpy (sequential file)
    op.create_table(
        "daily_transactions",
        sa.Column("id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column("tran_id", sa.String(16), server_default=""),
        sa.Column("tran_type_cd", sa.String(2), server_default=""),
        sa.Column("tran_cat_cd", sa.Integer(), server_default="0"),
        sa.Column("tran_source", sa.String(10), server_default=""),
        sa.Column("tran_desc", sa.String(100), server_default=""),
        sa.Column("tran_amt", sa.Numeric(11, 2), server_default="0"),
        sa.Column("merchant_id", sa.BigInteger(), server_default="0"),
        sa.Column("merchant_name", sa.String(50), server_default=""),
        sa.Column("merchant_city", sa.String(50), server_default=""),
        sa.Column("merchant_zip", sa.String(10), server_default=""),
        sa.Column("card_num", sa.String(16), server_default=""),
        sa.Column("orig_ts", sa.String(26), server_default=""),
        sa.Column("proc_ts", sa.String(26), server_default=""),
    )

    # tran_cat_balances — from CVTRA01Y.cpy (VSAM KSDS, 50-byte records)
    op.create_table(
        "tran_cat_balances",
        sa.Column("acct_id", sa.BigInteger(), primary_key=True),
        sa.Column("type_cd", sa.String(2), primary_key=True),
        sa.Column("cat_cd", sa.Integer(), primary_key=True),
        sa.Column("balance", sa.Numeric(11, 2), server_default="0"),
    )

    # disclosure_groups — from CVTRA02Y.cpy (VSAM KSDS, 50-byte records)
    op.create_table(
        "disclosure_groups",
        sa.Column("group_id", sa.String(10), primary_key=True),
        sa.Column("tran_type_cd", sa.String(2), primary_key=True),
        sa.Column("tran_cat_cd", sa.Integer(), primary_key=True),
        sa.Column("interest_rate", sa.Numeric(6, 2), server_default="0"),
    )

    # transaction_types — from CVTRA03Y.cpy / DB2 (60-byte records)
    op.create_table(
        "transaction_types",
        sa.Column("tran_type", sa.String(2), primary_key=True),
        sa.Column("tran_type_desc", sa.String(50), server_default=""),
    )

    # tran_categories — from CVTRA04Y.cpy / DB2 (60-byte records)
    op.create_table(
        "tran_categories",
        sa.Column("type_cd", sa.String(2), primary_key=True),
        sa.Column("cat_cd", sa.Integer(), primary_key=True),
        sa.Column("description", sa.String(50), server_default=""),
    )

    # user_security — from CSUSR01Y.cpy (VSAM KSDS, 80-byte records)
    op.create_table(
        "user_security",
        sa.Column("usr_id", sa.String(8), primary_key=True),
        sa.Column("usr_fname", sa.String(20), server_default=""),
        sa.Column("usr_lname", sa.String(20), server_default=""),
        sa.Column("usr_pwd", sa.String(128), server_default=""),
        sa.Column("usr_type", sa.String(1), server_default="U"),
    )

    # pending_auth_summary — from IMS CIPAUSMY segment
    op.create_table(
        "pending_auth_summary",
        sa.Column("id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column("acct_id", sa.BigInteger()),
        sa.Column("card_num", sa.String(16), server_default=""),
        sa.Column("pending_count", sa.Integer(), server_default="0"),
        sa.Column("pending_amount", sa.Numeric(11, 2), server_default="0"),
        sa.Column("status", sa.String(1), server_default="P"),
    )
    op.create_index("ix_pending_auth_summary_acct_id", "pending_auth_summary", ["acct_id"])

    # pending_auth_details — from IMS CIPAUDTY segment
    op.create_table(
        "pending_auth_details",
        sa.Column("id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column("summary_id", sa.BigInteger()),
        sa.Column("acct_id", sa.BigInteger()),
        sa.Column("card_num", sa.String(16), server_default=""),
        sa.Column("tran_type_cd", sa.String(2), server_default=""),
        sa.Column("tran_amt", sa.Numeric(11, 2), server_default="0"),
        sa.Column("merchant_name", sa.String(50), server_default=""),
        sa.Column("auth_status", sa.String(1), server_default="P"),
        sa.Column("orig_ts", sa.String(26), server_default=""),
        sa.Column("proc_ts", sa.String(26), server_default=""),
    )
    op.create_index("ix_pending_auth_details_summary_id", "pending_auth_details", ["summary_id"])
    op.create_index("ix_pending_auth_details_acct_id", "pending_auth_details", ["acct_id"])

    # auth_fraud — from DB2 AUTHFRDS table
    op.create_table(
        "auth_fraud",
        sa.Column("id", sa.BigInteger(), primary_key=True, autoincrement=True),
        sa.Column("acct_id", sa.BigInteger()),
        sa.Column("card_num", sa.String(16), server_default=""),
        sa.Column("tran_amt", sa.Numeric(11, 2), server_default="0"),
        sa.Column("decision", sa.String(10), server_default=""),
        sa.Column("reason", sa.String(100), server_default=""),
        sa.Column("decision_ts", sa.String(26), server_default=""),
    )
    op.create_index("ix_auth_fraud_acct_id", "auth_fraud", ["acct_id"])


def downgrade() -> None:
    op.drop_table("auth_fraud")
    op.drop_table("pending_auth_details")
    op.drop_table("pending_auth_summary")
    op.drop_table("user_security")
    op.drop_table("tran_categories")
    op.drop_table("transaction_types")
    op.drop_table("disclosure_groups")
    op.drop_table("tran_cat_balances")
    op.drop_table("daily_transactions")
    op.drop_table("transactions")
    op.drop_table("card_xref")
    op.drop_table("cards")
    op.drop_table("customers")
    op.drop_table("accounts")
