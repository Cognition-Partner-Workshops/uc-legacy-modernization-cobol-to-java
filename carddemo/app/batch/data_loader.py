"""Data loader — replaces IDCAMS REPRO JCL jobs for loading VSAM files.

Reads fixed-width text files from app/data/ASCII/ and loads them into PostgreSQL.
Each file corresponds to a VSAM KSDS dataset:
  acctdata.txt  → accounts      (300-byte records)
  carddata.txt  → cards         (150-byte records)
  cardxref.txt  → card_xref     (50-byte records)
  custdata.txt  → customers     (500-byte records)
  dailytran.txt → daily_transactions (350-byte records)
  discgrp.txt   → disclosure_groups  (50-byte records)
  tcatbal.txt   → tran_cat_balances  (50-byte records)
  trancatg.txt  → tran_categories    (60-byte records)
  trantype.txt  → transaction_types  (60-byte records)

COBOL packed-decimal sign encoding:
  { = +0, A = +1, B = +2, ..., I = +9
  } = -0, J = -1, K = -2, ..., R = -9
"""

import os
from decimal import Decimal
from pathlib import Path

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card import Card
from app.models.card_xref import CardXref
from app.models.customer import Customer
from app.models.daily_transaction import DailyTransaction
from app.models.disclosure_group import DisclosureGroup
from app.models.tran_category import TranCategory
from app.models.tran_category_balance import TranCategoryBalance
from app.models.transaction_type import TransactionType

# COBOL sign encoding: last character encodes sign and digit
_POS_SIGNS = {"{": 0, "A": 1, "B": 2, "C": 3, "D": 4, "E": 5, "F": 6, "G": 7, "H": 8, "I": 9}
_NEG_SIGNS = {"}": 0, "J": 1, "K": 2, "L": 3, "M": 4, "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9}


def _decode_signed(raw: str, decimal_places: int = 0) -> Decimal:
    """Decode COBOL signed numeric with trailing sign character.

    PIC S9(n)V99 uses a trailing overpunch sign:
      { = +0, A-I = +1 to +9
      } = -0, J-R = -1 to -9
    """
    if not raw or raw.isspace():
        return Decimal("0")

    last_char = raw[-1]
    digits = raw[:-1]

    if last_char in _POS_SIGNS:
        sign = ""
        last_digit = str(_POS_SIGNS[last_char])
    elif last_char in _NEG_SIGNS:
        sign = "-"
        last_digit = str(_NEG_SIGNS[last_char])
    elif last_char.isdigit():
        sign = ""
        last_digit = last_char
    else:
        return Decimal("0")

    full_digits = digits + last_digit
    if not full_digits.strip():
        return Decimal("0")

    if decimal_places > 0:
        integer_part = full_digits[:-decimal_places] or "0"
        decimal_part = full_digits[-decimal_places:]
        return Decimal(f"{sign}{integer_part}.{decimal_part}")
    return Decimal(f"{sign}{full_digits}")


def _resolve_data_dir(data_dir: str | None = None) -> Path:
    """Resolve data directory path."""
    if data_dir:
        return Path(data_dir)
    # Try relative to repo root
    candidates = [
        Path("app/data/ASCII"),
        Path("../app/data/ASCII"),
        Path(os.environ.get("CARDDEMO_DATA_DIR", "app/data/ASCII")),
    ]
    for candidate in candidates:
        if candidate.exists():
            return candidate
    return Path("app/data/ASCII")


def load_accounts(db: Session, data_dir: str | None = None) -> int:
    """Load acctdata.txt → accounts table (300-byte records).

    Record layout from CVACT01Y.cpy:
      ACCT-ID              0:11   PIC 9(11)
      ACCT-ACTIVE-STATUS  11:12   PIC X(01)
      ACCT-CURR-BAL       12:25   PIC S9(10)V99
      ACCT-CREDIT-LIMIT   25:38   PIC S9(10)V99
      ACCT-CASH-CREDIT    38:51   PIC S9(10)V99
      ACCT-OPEN-DATE      51:61   PIC X(10)
      ACCT-EXPIRAION-DATE 61:71   PIC X(10)
      ACCT-REISSUE-DATE   71:81   PIC X(10)
      ACCT-CURR-CYC-CR    81:94   PIC S9(10)V99
      ACCT-CURR-CYC-DB    94:107  PIC S9(10)V99
      ACCT-ADDR-ZIP      107:117  PIC X(10)
      ACCT-GROUP-ID      117:127  PIC X(10)
    """
    path = _resolve_data_dir(data_dir) / "acctdata.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        account = Account(
            acct_id=int(line[0:11]),
            active_status=line[11:12].strip(),
            curr_bal=_decode_signed(line[12:25], 2),
            credit_limit=_decode_signed(line[25:38], 2),
            cash_credit_limit=_decode_signed(line[38:51], 2),
            open_date=line[51:61].strip(),
            expiration_date=line[61:71].strip(),
            reissue_date=line[71:81].strip(),
            curr_cyc_credit=_decode_signed(line[81:94], 2),
            curr_cyc_debit=_decode_signed(line[94:107], 2),
            addr_zip=line[107:117].strip(),
            group_id=line[117:127].strip(),
        )
        db.merge(account)
        count += 1

    db.commit()
    return count


def load_cards(db: Session, data_dir: str | None = None) -> int:
    """Load carddata.txt → cards table (150-byte records).

    Record layout from CVACT02Y.cpy:
      CARD-NUM             0:16   PIC X(16)
      CARD-ACCT-ID        16:27   PIC 9(11)
      CARD-CVV-CD         27:30   PIC 9(03)
      CARD-EMBOSSED-NAME  30:80   PIC X(50)
      CARD-EXPIRAION-DATE 80:90   PIC X(10)
      CARD-ACTIVE-STATUS  90:91   PIC X(01)
    """
    path = _resolve_data_dir(data_dir) / "carddata.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        card = Card(
            card_num=line[0:16].strip(),
            acct_id=int(line[16:27]),
            cvv_cd=int(line[27:30]),
            embossed_name=line[30:80].strip(),
            expiration_date=line[80:90].strip(),
            active_status=line[90:91].strip(),
        )
        db.merge(card)
        count += 1

    db.commit()
    return count


def load_card_xref(db: Session, data_dir: str | None = None) -> int:
    """Load cardxref.txt → card_xref table (50-byte records).

    Record layout from CVACT03Y.cpy:
      XREF-CARD-NUM   0:16    PIC X(16)
      XREF-CUST-ID   16:25    PIC 9(09)
      XREF-ACCT-ID   25:36    PIC 9(11)
    """
    path = _resolve_data_dir(data_dir) / "cardxref.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        xref = CardXref(
            card_num=line[0:16].strip(),
            cust_id=int(line[16:25]),
            acct_id=int(line[25:36]),
        )
        db.merge(xref)
        count += 1

    db.commit()
    return count


def load_customers(db: Session, data_dir: str | None = None) -> int:
    """Load custdata.txt → customers table (500-byte records).

    Record layout from CVCUS01Y.cpy:
      CUST-ID              0:9     PIC 9(09)
      CUST-FIRST-NAME      9:34    PIC X(25)
      CUST-MIDDLE-NAME    34:59    PIC X(25)
      CUST-LAST-NAME      59:84    PIC X(25)
      CUST-ADDR-LINE-1    84:134   PIC X(50)
      CUST-ADDR-LINE-2   134:184   PIC X(50)
      CUST-ADDR-LINE-3   184:234   PIC X(50)
      CUST-ADDR-STATE-CD 234:236   PIC X(02)
      CUST-ADDR-COUNTRY  236:239   PIC X(03)
      CUST-ADDR-ZIP      239:249   PIC X(10)
      CUST-PHONE-NUM-1   249:264   PIC X(15)
      CUST-PHONE-NUM-2   264:279   PIC X(15)
      CUST-SSN           279:288   PIC 9(09)
      CUST-GOVT-ISSUED   288:308   PIC X(20)
      CUST-DOB           308:318   PIC X(10)
      CUST-EFT-ACCOUNT   318:328   PIC X(10)
      CUST-PRI-CARD-IND  328:329   PIC X(01)
      CUST-FICO-SCORE    329:332   PIC 9(03)
    """
    path = _resolve_data_dir(data_dir) / "custdata.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        ssn_raw = line[279:288].strip()
        fico_raw = line[329:332].strip()
        customer = Customer(
            cust_id=int(line[0:9]),
            first_name=line[9:34].strip(),
            middle_name=line[34:59].strip(),
            last_name=line[59:84].strip(),
            addr_line_1=line[84:134].strip(),
            addr_line_2=line[134:184].strip(),
            addr_line_3=line[184:234].strip(),
            addr_state_cd=line[234:236].strip(),
            addr_country_cd=line[236:239].strip(),
            addr_zip=line[239:249].strip(),
            phone_num_1=line[249:264].strip(),
            phone_num_2=line[264:279].strip(),
            ssn=int(ssn_raw) if ssn_raw.isdigit() else 0,
            govt_issued_id=line[288:308].strip(),
            dob_yyyy_mm_dd=line[308:318].strip(),
            eft_account_id=line[318:328].strip(),
            pri_card_holder_ind=line[328:329].strip(),
            fico_credit_score=int(fico_raw) if fico_raw.isdigit() else 0,
        )
        db.merge(customer)
        count += 1

    db.commit()
    return count


def load_daily_transactions(db: Session, data_dir: str | None = None) -> int:
    """Load dailytran.txt → daily_transactions table (350-byte records).

    Same layout as CVTRA05Y (transaction record).
    """
    path = _resolve_data_dir(data_dir) / "dailytran.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        tran = DailyTransaction(
            tran_id=line[0:16].strip(),
            tran_type_cd=line[16:18].strip(),
            tran_cat_cd=int(line[18:22]) if line[18:22].strip().isdigit() else 0,
            tran_source=line[22:32].strip(),
            tran_desc=line[32:132].strip(),
            tran_amt=_decode_signed(line[132:144], 2),
            merchant_id=int(line[144:153]) if line[144:153].strip().isdigit() else 0,
            merchant_name=line[153:203].strip(),
            merchant_city=line[203:253].strip(),
            merchant_zip=line[253:263].strip(),
            card_num=line[263:279].strip(),
            orig_ts=line[279:305].strip(),
            proc_ts=line[305:331].strip(),
        )
        db.add(tran)
        count += 1

    db.commit()
    return count


def load_disclosure_groups(db: Session, data_dir: str | None = None) -> int:
    """Load discgrp.txt → disclosure_groups table (50-byte records).

    Record layout from CVTRA02Y.cpy:
      DIS-ACCT-GROUP-ID   0:10   PIC X(10)
      DIS-TRAN-TYPE-CD   10:12   PIC X(02)
      DIS-TRAN-CAT-CD    12:16   PIC 9(04)
      DIS-INT-RATE       16:23   PIC S9(04)V99
    """
    path = _resolve_data_dir(data_dir) / "discgrp.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        cat_cd_raw = line[12:16].strip()
        disc = DisclosureGroup(
            group_id=line[0:10].strip(),
            tran_type_cd=line[10:12].strip(),
            tran_cat_cd=int(cat_cd_raw) if cat_cd_raw.isdigit() else 0,
            interest_rate=_decode_signed(line[16:23], 2),
        )
        db.merge(disc)
        count += 1

    db.commit()
    return count


def load_tran_cat_balances(db: Session, data_dir: str | None = None) -> int:
    """Load tcatbal.txt → tran_cat_balances table (50-byte records).

    Record layout from CVTRA01Y.cpy:
      TRANCAT-ACCT-ID   0:11   PIC 9(11)
      TRANCAT-TYPE-CD  11:13   PIC X(02)
      TRANCAT-CD       13:17   PIC 9(04)
      TRAN-CAT-BAL     17:29   PIC S9(09)V99
    """
    path = _resolve_data_dir(data_dir) / "tcatbal.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        cat_cd_raw = line[13:17].strip()
        tcat = TranCategoryBalance(
            acct_id=int(line[0:11]),
            type_cd=line[11:13].strip(),
            cat_cd=int(cat_cd_raw) if cat_cd_raw.isdigit() else 0,
            balance=_decode_signed(line[17:29], 2),
        )
        db.merge(tcat)
        count += 1

    db.commit()
    return count


def load_tran_categories(db: Session, data_dir: str | None = None) -> int:
    """Load trancatg.txt → tran_categories table (60-byte records).

    Record layout from CVTRA04Y.cpy:
      TRAN-TYPE-CD   0:2    PIC X(02)
      TRAN-CAT-CD    2:6    PIC 9(04)
      TRAN-CAT-DESC  6:56   PIC X(50)
    """
    path = _resolve_data_dir(data_dir) / "trancatg.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        cat_cd_raw = line[2:6].strip()
        tcat = TranCategory(
            type_cd=line[0:2].strip(),
            cat_cd=int(cat_cd_raw) if cat_cd_raw.isdigit() else 0,
            description=line[6:56].strip(),
        )
        db.merge(tcat)
        count += 1

    db.commit()
    return count


def load_transaction_types(db: Session, data_dir: str | None = None) -> int:
    """Load trantype.txt → transaction_types table (60-byte records).

    Record layout from CVTRA03Y.cpy:
      TRAN-TYPE      0:2    PIC X(02)
      TRAN-TYPE-DESC 2:52   PIC X(50)
    """
    path = _resolve_data_dir(data_dir) / "trantype.txt"
    if not path.exists():
        return 0

    count = 0
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        ttype = TransactionType(
            tran_type=line[0:2].strip(),
            tran_type_desc=line[2:52].strip(),
        )
        db.merge(ttype)
        count += 1

    db.commit()
    return count


def load_all(db: Session, data_dir: str | None = None) -> dict:
    """Load all data files — replaces full IDCAMS REPRO JCL job stream."""
    results = {
        "accounts": load_accounts(db, data_dir),
        "cards": load_cards(db, data_dir),
        "card_xref": load_card_xref(db, data_dir),
        "customers": load_customers(db, data_dir),
        "daily_transactions": load_daily_transactions(db, data_dir),
        "disclosure_groups": load_disclosure_groups(db, data_dir),
        "tran_cat_balances": load_tran_cat_balances(db, data_dir),
        "tran_categories": load_tran_categories(db, data_dir),
        "transaction_types": load_transaction_types(db, data_dir),
    }
    return results
