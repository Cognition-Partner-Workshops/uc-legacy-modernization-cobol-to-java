"""COBOL fixed-width flat file parsers for CardDemo data migration.

Each parser reads lines from a file and yields dictionaries with string
values suitable for DynamoDB batch writes.
"""

from decimal import Decimal

# ---------------------------------------------------------------------------
# Overpunch sign encoding for COBOL PIC S9(n)V99 fields
# ---------------------------------------------------------------------------
_POSITIVE = {"{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
             "E": "5", "F": "6", "G": "7", "H": "8", "I": "9"}
_NEGATIVE = {"}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
             "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9"}


def _parse_signed_decimal(raw: str, integer_digits: int, decimal_digits: int) -> str:
    """Decode a COBOL signed numeric field with implied decimal.

    The last character carries the sign via overpunch encoding.
    ``integer_digits`` is the count before the implied ``V``, and
    ``decimal_digits`` is the count after (e.g. S9(10)V99 → 10, 2).
    """
    if not raw or raw.isspace():
        return "0"
    last = raw[-1]
    if last in _POSITIVE:
        sign = 1
        digit = _POSITIVE[last]
    elif last in _NEGATIVE:
        sign = -1
        digit = _NEGATIVE[last]
    elif last.isdigit():
        sign = 1
        digit = last
    else:
        return "0"

    digits = raw[:-1] + digit
    total_digits = integer_digits + decimal_digits
    digits = digits.zfill(total_digits)
    integer_part = digits[:integer_digits]
    decimal_part = digits[integer_digits:]
    value = Decimal(f"{integer_part}.{decimal_part}") * sign
    return str(value)


# ---------------------------------------------------------------------------
# acctdata.txt → carddemo-accounts  (CVACT01Y.cpy, RECLN 300)
# ---------------------------------------------------------------------------
def parse_accounts(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 122:
                continue
            yield {
                "acct_id":            line[0:11].strip(),
                "active_status":      line[11:12].strip(),
                "curr_bal":           _parse_signed_decimal(line[12:24], 10, 2),
                "credit_limit":       _parse_signed_decimal(line[24:36], 10, 2),
                "cash_credit_limit":  _parse_signed_decimal(line[36:48], 10, 2),
                "open_date":          line[48:58].strip(),
                "expiration_date":    line[58:68].strip(),
                "reissue_date":       line[68:78].strip(),
                "curr_cyc_credit":    _parse_signed_decimal(line[78:90], 10, 2),
                "curr_cyc_debit":     _parse_signed_decimal(line[90:102], 10, 2),
                "group_id":           line[112:122].strip(),
            }


# ---------------------------------------------------------------------------
# custdata.txt → carddemo-customers  (CVCUS01Y.cpy, RECLN 500)
# ---------------------------------------------------------------------------
def parse_customers(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 332:
                continue
            yield {
                "cust_id":             line[0:9].strip(),
                "first_name":          line[9:34].strip(),
                "middle_name":         line[34:59].strip(),
                "last_name":           line[59:84].strip(),
                "addr_line_1":         line[84:134].strip(),
                "addr_line_2":         line[134:184].strip(),
                "addr_line_3":         line[184:234].strip(),
                "state_cd":            line[234:236].strip(),
                "country_cd":          line[236:239].strip(),
                "zip":                 line[239:249].strip(),
                "phone_1":             line[249:264].strip(),
                "phone_2":             line[264:279].strip(),
                "ssn":                 line[279:288].strip(),
                "govt_issued_id":      line[288:308].strip(),
                "dob":                 line[308:318].strip(),
                "eft_account_id":      line[318:328].strip(),
                "pri_card_holder_ind": line[328:329].strip(),
                "fico_score":          line[329:332].strip(),
            }


# ---------------------------------------------------------------------------
# carddata.txt → carddemo-cards  (CVACT02Y.cpy, RECLN 150)
# ---------------------------------------------------------------------------
def parse_cards(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 91:
                continue
            yield {
                "card_num":        line[0:16].strip(),
                "acct_id":         line[16:27].strip(),
                "cvv_cd":          line[27:30].strip(),
                "embossed_name":   line[30:80].strip(),
                "expiry_date":     line[80:90].strip(),
                "active_status":   line[90:91].strip(),
            }


# ---------------------------------------------------------------------------
# cardxref.txt → carddemo-card-xref  (CVACT03Y.cpy, RECLN 50)
# ---------------------------------------------------------------------------
def parse_card_xref(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 36:
                continue
            yield {
                "card_num": line[0:16].strip(),
                "cust_id":  line[16:25].strip(),
                "acct_id":  line[25:36].strip(),
            }


def load_xref_map(filepath: str) -> dict:
    """Return a dict mapping card_num → acct_id from the xref file."""
    return {rec["card_num"]: rec["acct_id"] for rec in parse_card_xref(filepath)}


# ---------------------------------------------------------------------------
# dailytran.txt → carddemo-transactions  (CVTRA05Y.cpy, RECLN 350)
# ---------------------------------------------------------------------------
def parse_transactions(filepath: str, xref_map: dict | None = None):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 330:
                continue
            card_num = line[262:278].strip()
            rec = {
                "tran_id":          line[0:16].strip(),
                "tran_type_cd":     line[16:18].strip(),
                "tran_cat_cd":      line[18:22].strip(),
                "tran_source":      line[22:32].strip(),
                "tran_desc":        line[32:132].strip(),
                "tran_amt":         _parse_signed_decimal(line[132:143], 9, 2),
                "merchant_id":      line[143:152].strip(),
                "merchant_name":    line[152:202].strip(),
                "merchant_city":    line[202:252].strip(),
                "merchant_zip":     line[252:262].strip(),
                "card_num":         card_num,
                "tran_orig_ts":     line[278:304].strip(),
                "tran_proc_ts":     line[304:330].strip(),
            }
            if xref_map and card_num in xref_map:
                rec["acct_id"] = xref_map[card_num]
            yield rec


# ---------------------------------------------------------------------------
# trantype.txt → carddemo-transaction-types
# ---------------------------------------------------------------------------
def parse_transaction_types(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 52:
                continue
            yield {
                "type_cd":    line[0:2].strip(),
                "type_desc":  line[2:52].strip(),
            }


# ---------------------------------------------------------------------------
# trancatg.txt → carddemo-transaction-categories
# ---------------------------------------------------------------------------
def parse_transaction_categories(filepath: str):
    with open(filepath, "r") as fh:
        for line in fh:
            line = line.rstrip("\n\r")
            if len(line) < 56:
                continue
            yield {
                "type_cd":   line[0:2].strip(),
                "cat_cd":    line[2:6].strip(),
                "cat_desc":  line[6:56].strip(),
            }
