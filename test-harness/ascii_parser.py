"""
Parse fixed-width ASCII data files using COBOL copybook field definitions.

Reads each line of an ASCII data file and slices it according to the offsets
and lengths defined in the copybook, producing a list of dictionaries with
decoded field values.
"""

from copybook_parser import CopybookField, decode_signed_value


def parse_record(line: str, fields: list) -> dict:
    """Parse a single fixed-width record into a dictionary.

    Args:
        line: A single line from the ASCII data file.
        fields: List of CopybookField definitions from the copybook parser.

    Returns:
        Dictionary mapping field names to decoded values.
        FILLER fields are excluded.
    """
    record = {}
    for field in fields:
        if field.name == "FILLER":
            continue

        start = field.offset
        end = start + field.length
        raw_value = line[start:end] if end <= len(line) else line[start:]

        if field.field_type == "alpha":
            record[field.name] = raw_value.rstrip()
        elif field.field_type == "signed_decimal":
            record[field.name] = decode_signed_value(
                raw_value, field.decimal_places
            )
        elif field.field_type == "numeric":
            stripped = raw_value.strip()
            if field.decimal_places > 0 and stripped:
                if len(stripped) <= field.decimal_places:
                    stripped = stripped.zfill(field.decimal_places + 1)
                integer_part = stripped[: -field.decimal_places]
                decimal_part = stripped[-field.decimal_places :]
                record[field.name] = f"{integer_part}.{decimal_part}"
            else:
                record[field.name] = stripped.lstrip("0") or "0"
        else:
            record[field.name] = raw_value.rstrip()

    return record


def parse_file(filepath: str, fields: list) -> list:
    """Parse an entire ASCII data file into a list of record dictionaries.

    Args:
        filepath: Path to the ASCII data file.
        fields: List of CopybookField definitions.

    Returns:
        List of dictionaries, one per non-empty line in the file.
    """
    records = []
    with open(filepath, "r") as f:
        for line_num, line in enumerate(f, start=1):
            # Skip empty lines
            stripped = line.rstrip("\n\r")
            if not stripped:
                continue
            record = parse_record(stripped, fields)
            record["_line_number"] = line_num
            records.append(record)
    return records
