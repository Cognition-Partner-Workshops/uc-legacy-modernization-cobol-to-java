"""
Field-by-field JSON comparison engine for golden-file testing.

Compares a "candidate" JSON output (produced by the migrated Java system)
against a "golden" JSON reference (produced by the golden file generator)
and reports all differences at the field level.
"""

import json
import os
import sys
from decimal import Decimal, InvalidOperation


class ComparisonResult:
    """Holds the result of comparing two JSON golden files."""

    def __init__(self, golden_file: str, candidate_file: str):
        self.golden_file = golden_file
        self.candidate_file = candidate_file
        self.differences = []
        self.record_count_match = True
        self.golden_count = 0
        self.candidate_count = 0

    @property
    def passed(self) -> bool:
        return self.record_count_match and len(self.differences) == 0

    def add_difference(self, record_index: int, field: str,
                       golden_val: object, candidate_val: object,
                       severity: str = "ERROR"):
        self.differences.append({
            "record": record_index,
            "field": field,
            "golden": golden_val,
            "candidate": candidate_val,
            "severity": severity,
        })

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"[{status}] {os.path.basename(self.golden_file)}",
            f"  Golden records:    {self.golden_count}",
            f"  Candidate records: {self.candidate_count}",
            f"  Differences:       {len(self.differences)}",
        ]
        for diff in self.differences[:20]:  # Show first 20
            lines.append(
                f"    Record {diff['record']}, field '{diff['field']}': "
                f"golden={diff['golden']!r} vs candidate={diff['candidate']!r} "
                f"[{diff['severity']}]"
            )
        if len(self.differences) > 20:
            lines.append(f"    ... and {len(self.differences) - 20} more")
        return "\n".join(lines)


def normalize_value(value: object) -> object:
    """Normalize a value for tolerant comparison.

    - Strings are stripped of whitespace.
    - Numeric strings are converted to Decimal for precision-safe comparison.
    """
    if value is None:
        return None
    if isinstance(value, str):
        stripped = value.strip()
        try:
            return Decimal(stripped)
        except (InvalidOperation, ValueError):
            return stripped
    if isinstance(value, (int, float)):
        return Decimal(str(value))
    return value


def compare_records(golden: dict, candidate: dict, record_index: int,
                    result: ComparisonResult, strict: bool = False):
    """Compare two parsed records field by field."""
    all_fields = set(golden.keys()) | set(candidate.keys())

    for field in sorted(all_fields):
        g_val = golden.get(field)
        c_val = candidate.get(field)

        if field not in golden:
            result.add_difference(record_index, field, "<missing>", c_val, "WARN")
            continue
        if field not in candidate:
            result.add_difference(record_index, field, g_val, "<missing>", "ERROR")
            continue

        if strict:
            if g_val != c_val:
                result.add_difference(record_index, field, g_val, c_val)
        else:
            g_norm = normalize_value(g_val)
            c_norm = normalize_value(c_val)
            if g_norm != c_norm:
                result.add_difference(record_index, field, g_val, c_val)


def compare_golden_files(golden_path: str, candidate_path: str,
                         strict: bool = False) -> ComparisonResult:
    """Compare a candidate JSON file against a golden reference.

    Args:
        golden_path:    Path to the golden JSON file.
        candidate_path: Path to the candidate JSON file.
        strict:         If True, use exact string comparison; otherwise normalize.

    Returns:
        ComparisonResult with all differences.
    """
    result = ComparisonResult(golden_path, candidate_path)

    with open(golden_path, "r", encoding="utf-8") as f:
        golden_data = json.load(f)
    with open(candidate_path, "r", encoding="utf-8") as f:
        candidate_data = json.load(f)

    # Find the records array key (skip _metadata)
    golden_keys = [k for k in golden_data if k != "_metadata"]
    candidate_keys = [k for k in candidate_data if k != "_metadata"]

    if not golden_keys:
        result.differences.append({
            "record": -1, "field": "_structure",
            "golden": "no record array found", "candidate": "",
            "severity": "ERROR",
        })
        return result

    records_key = golden_keys[0]
    golden_records = golden_data.get(records_key, [])
    candidate_records = candidate_data.get(records_key, [])

    result.golden_count = len(golden_records)
    result.candidate_count = len(candidate_records)

    if result.golden_count != result.candidate_count:
        result.record_count_match = False

    # Compare records up to the shorter list
    compare_count = min(len(golden_records), len(candidate_records))
    for i in range(compare_count):
        compare_records(golden_records[i], candidate_records[i], i, result, strict)

    return result


def compare_all_golden_files(golden_dir: str, candidate_dir: str,
                              strict: bool = False) -> list:
    """Compare all golden files in a directory against candidates.

    Returns list of ComparisonResult objects.
    """
    results = []
    for filename in sorted(os.listdir(golden_dir)):
        if not filename.endswith(".json"):
            continue
        golden_path = os.path.join(golden_dir, filename)
        candidate_path = os.path.join(candidate_dir, filename)

        if not os.path.isfile(candidate_path):
            result = ComparisonResult(golden_path, candidate_path)
            result.record_count_match = False
            result.add_difference(-1, "_file", "exists", "missing", "ERROR")
            results.append(result)
            continue

        results.append(compare_golden_files(golden_path, candidate_path, strict))

    return results


def main():
    """CLI entry point for comparing golden files."""
    if len(sys.argv) < 3:
        print("Usage: python comparator.py <golden_dir> <candidate_dir> [--strict]")
        sys.exit(1)

    golden_dir = sys.argv[1]
    candidate_dir = sys.argv[2]
    strict = "--strict" in sys.argv

    print("Golden File Comparator")
    print("=" * 60)
    print(f"Golden:    {golden_dir}")
    print(f"Candidate: {candidate_dir}")
    print(f"Mode:      {'strict' if strict else 'tolerant'}")
    print()

    results = compare_all_golden_files(golden_dir, candidate_dir, strict)

    passed = sum(1 for r in results if r.passed)
    failed = len(results) - passed

    for r in results:
        print(r.summary())
        print()

    print(f"Results: {passed} passed, {failed} failed out of {len(results)} files")
    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
