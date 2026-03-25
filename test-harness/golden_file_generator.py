"""
Golden File Generator

Reads each ASCII data file from app/data/ASCII/, parses it using
the copybook layout definitions, and writes structured JSON golden
reference files to golden-files/.
"""

import json
import os
import sys

# Allow imports from the test-harness directory
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from copybook_parser import FILE_LAYOUTS, parse_file


def generate_golden_files(data_dir: str, output_dir: str) -> dict:
    """Generate golden JSON files for all known data files.

    Args:
        data_dir:   Path to the directory containing ASCII data files.
        output_dir: Path to the directory where JSON golden files are written.

    Returns:
        Summary dict with per-file record counts and any errors.
    """
    os.makedirs(output_dir, exist_ok=True)
    summary = {"files_generated": 0, "total_records": 0, "errors": []}

    for filename, (record_label, layout) in sorted(FILE_LAYOUTS.items()):
        source_path = os.path.join(data_dir, filename)
        if not os.path.isfile(source_path):
            summary["errors"].append(f"Missing source file: {filename}")
            continue

        try:
            result = parse_file(source_path, layout, record_label)
            output_name = os.path.splitext(filename)[0] + ".json"
            output_path = os.path.join(output_dir, output_name)

            with open(output_path, "w", encoding="utf-8") as fh:
                json.dump(result, fh, indent=2, default=str)

            count = result["_metadata"]["record_count"]
            summary["files_generated"] += 1
            summary["total_records"] += count
            print(f"  {filename:20s} -> {output_name:20s} ({count} records)")

        except Exception as exc:
            summary["errors"].append(f"{filename}: {exc}")
            print(f"  {filename:20s} -> ERROR: {exc}", file=sys.stderr)

    return summary


def main():
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(repo_root, "app", "data", "ASCII")
    output_dir = os.path.join(repo_root, "golden-files")

    print("Golden File Generator")
    print("=" * 60)
    print(f"Source:  {data_dir}")
    print(f"Output:  {output_dir}")
    print()

    if not os.path.isdir(data_dir):
        print(f"ERROR: Data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    summary = generate_golden_files(data_dir, output_dir)

    print()
    print(f"Files generated: {summary['files_generated']}")
    print(f"Total records:   {summary['total_records']}")

    if summary["errors"]:
        print(f"\nErrors ({len(summary['errors'])}):")
        for err in summary["errors"]:
            print(f"  - {err}")
        sys.exit(1)

    print("\nAll golden files generated successfully.")


if __name__ == "__main__":
    main()
