"""Parse fixed-width ASCII data files using copybook field definitions."""

import json
import sys
from pathlib import Path
from typing import Dict, List, Optional

from .copybook_parser import CARDDEMO_LAYOUTS
from .utils import decode_signed_zoned_decimal, format_field_value, parse_pic_clause


def parse_record(line: str, layout: dict) -> Dict[str, object]:
    """Parse a single fixed-width record line using a layout definition.

    Args:
        line: Raw fixed-width record string.
        layout: Layout dict with 'fields' list containing offset/length/pic.

    Returns:
        Dictionary mapping field names to parsed values.
    """
    record = {}
    for field_def in layout["fields"]:
        name = field_def["name"]
        offset = field_def["offset"]
        length = field_def["length"]
        pic = field_def["pic"]

        # Extract raw value
        raw = line[offset:offset + length]

        # Determine type and format
        field_type, _, decimal_places = parse_pic_clause(pic)
        value = format_field_value(raw, field_type, decimal_places)
        record[name] = value

    return record


def parse_data_file(filepath: str, layout_name: str) -> List[Dict[str, object]]:
    """Parse an entire ASCII data file into a list of record dictionaries.

    Args:
        filepath: Path to the ASCII data file.
        layout_name: Key into CARDDEMO_LAYOUTS (e.g., 'acctdata').

    Returns:
        List of parsed record dictionaries.
    """
    if layout_name not in CARDDEMO_LAYOUTS:
        raise ValueError(f"Unknown layout: {layout_name}. "
                         f"Available: {list(CARDDEMO_LAYOUTS.keys())}")

    layout = CARDDEMO_LAYOUTS[layout_name]
    path = Path(filepath)
    records = []

    with path.open("r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, 1):
            # Strip trailing newline but preserve spaces (fixed-width)
            line = line.rstrip("\n").rstrip("\r")

            if not line.strip():
                continue

            record = parse_record(line, layout)
            record["_line_number"] = line_num
            records.append(record)

    return records


def generate_golden_file(
    data_dir: str,
    output_dir: str,
    layout_name: str,
    filename: Optional[str] = None,
) -> str:
    """Parse an ASCII data file and write JSON golden reference.

    Args:
        data_dir: Directory containing ASCII data files.
        output_dir: Directory to write golden JSON files.
        layout_name: Layout key (e.g., 'acctdata').
        filename: Optional override for input filename.

    Returns:
        Path to the generated golden file.
    """
    if filename is None:
        filename = f"{layout_name}.txt"

    input_path = Path(data_dir) / filename
    output_path = Path(output_dir) / f"{layout_name}.json"

    if not input_path.exists():
        raise FileNotFoundError(f"Data file not found: {input_path}")

    records = parse_data_file(str(input_path), layout_name)

    golden_data = {
        "metadata": {
            "source_file": str(input_path.name),
            "copybook": CARDDEMO_LAYOUTS[layout_name]["copybook"],
            "record_length": CARDDEMO_LAYOUTS[layout_name]["record_length"],
            "record_count": len(records),
            "layout_name": layout_name,
        },
        "records": records,
    }

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8") as f:
        json.dump(golden_data, f, indent=2, default=str)

    return str(output_path)


def generate_all_golden_files(data_dir: str, output_dir: str) -> List[str]:
    """Generate golden reference files for all known CardDemo data files.

    Args:
        data_dir: Path to app/data/ASCII/ directory.
        output_dir: Path to golden-files/ output directory.

    Returns:
        List of generated golden file paths.
    """
    generated = []

    for layout_name in CARDDEMO_LAYOUTS:
        filename = f"{layout_name}.txt"
        input_path = Path(data_dir) / filename

        if input_path.exists():
            output_path = generate_golden_file(data_dir, output_dir, layout_name)
            generated.append(output_path)
            print(f"  Generated: {output_path} ({CARDDEMO_LAYOUTS[layout_name]['copybook']})")
        else:
            print(f"  Skipped: {filename} (not found)")

    return generated


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Parse CardDemo ASCII data files into JSON golden references"
    )
    parser.add_argument(
        "--data-dir",
        default="app/data/ASCII",
        help="Path to ASCII data directory",
    )
    parser.add_argument(
        "--output-dir",
        default="golden-files",
        help="Path to output golden files directory",
    )
    parser.add_argument(
        "--layout",
        choices=list(CARDDEMO_LAYOUTS.keys()),
        help="Parse only a specific layout (default: all)",
    )

    args = parser.parse_args()

    if args.layout:
        path = generate_golden_file(args.data_dir, args.output_dir, args.layout)
        print(f"Generated: {path}")
    else:
        print("Generating golden reference files...")
        paths = generate_all_golden_files(args.data_dir, args.output_dir)
        print(f"\nGenerated {len(paths)} golden files.")
