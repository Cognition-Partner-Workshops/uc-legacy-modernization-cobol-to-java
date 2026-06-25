"""Fixed-width parsers for the CardDemo COBOL flat files.

Each ``parse_*`` function reads one of the ASCII flat files in
``app/data/ASCII/`` and returns a list of dicts keyed by the canonical
DynamoDB attribute names defined in ``aws-migration/API_CONTRACT.md`` (§1/§2).

Records are fixed-width; files may use CRLF or LF line endings, so every line
is read individually and ``rstrip("\r\n")`` is applied. Signed numeric fields
(``PIC S9(n)V99``) use EBCDIC overpunch sign encoding and are decoded by
``decode_overpunch`` into decimal strings (e.g. ``"194.00"`` / ``"-12.50"``).
"""

from __future__ import annotations

# Overpunch sign tables for the trailing character of a PIC S9(n)V99 field.
_POSITIVE = {"{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
             "E": "5", "F": "6", "G": "7", "H": "8", "I": "9"}
_NEGATIVE = {"}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
             "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9"}


def decode_overpunch(raw: str) -> str:
    """Decode a ``PIC S9(n)V99`` overpunch-signed field to a decimal string.

    The sign and last digit are encoded in the final character. ``V99`` means
    two implied decimal places. Returns a string such as ``"194.00"`` or
    ``"-12.50"``.
    """
    s = raw.strip()
    if not s:
        return "0.00"

    last = s[-1]
    lead = s[:-1]
    sign = ""

    if last in _POSITIVE:
        last_digit = _POSITIVE[last]
    elif last in _NEGATIVE:
        last_digit = _NEGATIVE[last]
        sign = "-"
    elif last.isdigit():
        # Unsigned trailing digit (no overpunch applied).
        last_digit = last
    else:
        raise ValueError(f"invalid overpunch character {last!r} in {raw!r}")

    digits = (lead + last_digit).strip()
    if not digits.isdigit():
        raise ValueError(f"non-numeric overpunch field {raw!r}")

    digits = digits.zfill(3)
    int_part = digits[:-2].lstrip("0") or "0"
    frac_part = digits[-2:]
    value = f"{int_part}.{frac_part}"
    if sign and value != "0.00":
        return sign + value
    return value


def _read_lines(path: str) -> list[str]:
    """Read a flat file line by line, stripping line endings (CRLF or LF)."""
    lines: list[str] = []
    with open(path, "r", encoding="latin-1") as fh:
        for line in fh:
            line = line.rstrip("\r\n")
            if line.strip():
                lines.append(line)
    return lines


def _text(line: str, start: int, length: int) -> str:
    """Slice a text field and trim trailing spaces."""
    return line[start:start + length].rstrip()


def _ident(line: str, start: int, length: int) -> str:
    """Slice an identifier field, preserving zero padding."""
    return line[start:start + length].strip()


def _money(line: str, start: int, length: int) -> str:
    """Slice and overpunch-decode a signed numeric field."""
    return decode_overpunch(line[start:start + length])


def parse_accounts(path: str) -> list[dict[str, str]]:
    """Parse ``acctdata.txt`` (RECLN 300, copybook CVACT01Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "acct_id": _ident(line, 0, 11),
            "acct_active_status": _text(line, 11, 1),
            "curr_bal": _money(line, 12, 12),
            "credit_limit": _money(line, 24, 12),
            "cash_credit_limit": _money(line, 36, 12),
            "open_date": _text(line, 48, 10),
            "expiration_date": _text(line, 58, 10),
            "reissue_date": _text(line, 68, 10),
            "curr_cyc_credit": _money(line, 78, 12),
            "curr_cyc_debit": _money(line, 90, 12),
            "addr_zip": _text(line, 102, 10),
            "group_id": _text(line, 112, 10),
        })
    return records


def parse_customers(path: str) -> list[dict[str, str]]:
    """Parse ``custdata.txt`` (RECLN 500, copybook CVCUS01Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "cust_id": _ident(line, 0, 9),
            "first_name": _text(line, 9, 25),
            "middle_name": _text(line, 34, 25),
            "last_name": _text(line, 59, 25),
            "addr_line_1": _text(line, 84, 50),
            "addr_line_2": _text(line, 134, 50),
            "addr_line_3": _text(line, 184, 50),
            "addr_state_cd": _text(line, 234, 2),
            "addr_country_cd": _text(line, 236, 3),
            "addr_zip": _text(line, 239, 10),
            "phone_num_1": _text(line, 249, 15),
            "phone_num_2": _text(line, 264, 15),
            "ssn": _text(line, 279, 9),
            "govt_issued_id": _text(line, 288, 20),
            "dob": _text(line, 308, 10),
            "eft_account_id": _text(line, 318, 10),
            "pri_card_holder_ind": _text(line, 328, 1),
            "fico_credit_score": _text(line, 329, 3),
        })
    return records


def parse_cards(path: str) -> list[dict[str, str]]:
    """Parse ``carddata.txt`` (RECLN 150, copybook CVACT02Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "card_num": _ident(line, 0, 16),
            "acct_id": _ident(line, 16, 11),
            "cvv_cd": _text(line, 27, 3),
            "embossed_name": _text(line, 30, 50),
            "expiration_date": _text(line, 80, 10),
            "active_status": _text(line, 90, 1),
        })
    return records


def parse_card_xref(path: str) -> list[dict[str, str]]:
    """Parse ``cardxref.txt`` (RECLN 50, copybook CVACT03Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "card_num": _ident(line, 0, 16),
            "cust_id": _ident(line, 16, 9),
            "acct_id": _ident(line, 25, 11),
        })
    return records


def parse_transactions(path: str) -> list[dict[str, str]]:
    """Parse ``dailytran.txt`` (RECLN 350, copybook CVTRA05Y/CVTRA06Y).

    ``acct_id`` is intentionally not set here; the loader enriches each record
    by looking up ``card_num`` in the card-xref data (see ``load_data.py``).
    """
    records = []
    for line in _read_lines(path):
        records.append({
            "tran_id": _ident(line, 0, 16),
            "type_cd": _text(line, 16, 2),
            "cat_cd": _ident(line, 18, 4),
            "tran_source": _text(line, 22, 10),
            "tran_desc": _text(line, 32, 100),
            "amt": _money(line, 132, 11),
            "merchant_id": _ident(line, 143, 9),
            "merchant_name": _text(line, 152, 50),
            "merchant_city": _text(line, 202, 50),
            "merchant_zip": _text(line, 252, 10),
            "card_num": _ident(line, 262, 16),
            "tran_orig_ts": _text(line, 278, 26),
            "tran_proc_ts": _text(line, 304, 26),
        })
    return records


def parse_transaction_types(path: str) -> list[dict[str, str]]:
    """Parse ``trantype.txt`` (RECLN 60, copybook CVTRA03Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "type_cd": _text(line, 0, 2),
            "type_desc": _text(line, 2, 50),
        })
    return records


def parse_transaction_categories(path: str) -> list[dict[str, str]]:
    """Parse ``trancatg.txt`` (RECLN 60, copybook CVTRA04Y)."""
    records = []
    for line in _read_lines(path):
        records.append({
            "type_cd": _text(line, 0, 2),
            "cat_cd": _ident(line, 2, 4),
            "cat_desc": _text(line, 6, 50),
        })
    return records


# Maps each file name to its parser, for convenient batch parsing.
FILE_PARSERS = {
    "acctdata.txt": parse_accounts,
    "custdata.txt": parse_customers,
    "carddata.txt": parse_cards,
    "cardxref.txt": parse_card_xref,
    "dailytran.txt": parse_transactions,
    "trantype.txt": parse_transaction_types,
    "trancatg.txt": parse_transaction_categories,
}


def _main() -> None:
    """Parse every file in a data dir and print counts + sample records."""
    import argparse
    import json
    import os

    ap = argparse.ArgumentParser(description="Parse CardDemo flat files (verify).")
    ap.add_argument("--data-dir",
                    default=os.path.join(os.path.dirname(__file__),
                                         "..", "..", "app", "data", "ASCII"))
    ap.add_argument("--samples", type=int, default=1,
                    help="number of sample records to print per file")
    args = ap.parse_args()

    for filename, parser in FILE_PARSERS.items():
        path = os.path.join(args.data_dir, filename)
        records = parser(path)
        print(f"{filename}: {len(records)} records")
        for rec in records[:args.samples]:
            print("  " + json.dumps(rec))


if __name__ == "__main__":
    _main()
