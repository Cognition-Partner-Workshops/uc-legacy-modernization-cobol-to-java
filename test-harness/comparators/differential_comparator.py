"""
Differential comparator for COBOL vs Java output analysis.

Compares outputs from both systems side-by-side and categorizes
differences by severity: semantic, cosmetic, or precision.
"""

import json
import math
from dataclasses import dataclass, field
from pathlib import Path
from enum import Enum


class DifferenceCategory(Enum):
    """Classification of differences between COBOL and Java output."""
    SEMANTIC = "semantic"       # Logic error - different business result
    COSMETIC = "cosmetic"      # Formatting difference - same meaning
    PRECISION = "precision"    # Rounding/scale difference in numeric values


@dataclass
class DifferentialDiff:
    """A single difference between COBOL and Java outputs."""
    record_index: int
    field_name: str
    cobol_value: object
    java_value: object
    category: DifferenceCategory
    description: str = ""


@dataclass
class DifferentialResult:
    """Result of differential comparison between COBOL and Java."""
    cobol_source: str
    java_source: str
    passed: bool
    total_records: int = 0
    semantic_diffs: list = field(default_factory=list)
    cosmetic_diffs: list = field(default_factory=list)
    precision_diffs: list = field(default_factory=list)
    summary: str = ""

    @property
    def total_diffs(self) -> int:
        return len(self.semantic_diffs) + len(self.cosmetic_diffs) + len(self.precision_diffs)

    def to_dict(self) -> dict:
        def diff_to_dict(d: DifferentialDiff) -> dict:
            return {
                "record_index": d.record_index,
                "field_name": d.field_name,
                "cobol_value": d.cobol_value,
                "java_value": d.java_value,
                "category": d.category.value,
                "description": d.description,
            }

        return {
            "cobol_source": self.cobol_source,
            "java_source": self.java_source,
            "passed": self.passed,
            "total_records": self.total_records,
            "total_differences": self.total_diffs,
            "semantic_differences": [diff_to_dict(d) for d in self.semantic_diffs[:50]],
            "cosmetic_differences": [diff_to_dict(d) for d in self.cosmetic_diffs[:50]],
            "precision_differences": [diff_to_dict(d) for d in self.precision_diffs[:50]],
            "summary": self.summary,
        }


def categorize_difference(
    field_name: str, cobol_val: object, java_val: object, precision_threshold: float = 0.005
) -> DifferentialDiff | None:
    """Categorize a difference between COBOL and Java values.

    Args:
        field_name: Name of the field being compared
        cobol_val: Value from COBOL system
        java_val: Value from Java system
        precision_threshold: Maximum acceptable numeric difference for precision category

    Returns:
        DifferentialDiff if values differ, None if equal
    """
    # Both None or both equal
    if cobol_val == java_val:
        return None

    # String comparison - check for cosmetic differences
    if isinstance(cobol_val, str) and isinstance(java_val, str):
        if cobol_val.strip() == java_val.strip():
            return DifferentialDiff(
                record_index=0,
                field_name=field_name,
                cobol_value=cobol_val,
                java_value=java_val,
                category=DifferenceCategory.COSMETIC,
                description="Trailing/leading whitespace difference",
            )
        # Case difference
        if cobol_val.strip().upper() == java_val.strip().upper():
            return DifferentialDiff(
                record_index=0,
                field_name=field_name,
                cobol_value=cobol_val,
                java_value=java_val,
                category=DifferenceCategory.COSMETIC,
                description="Case difference only",
            )
        # Semantic string difference
        return DifferentialDiff(
            record_index=0,
            field_name=field_name,
            cobol_value=cobol_val,
            java_value=java_val,
            category=DifferenceCategory.SEMANTIC,
            description="String values differ",
        )

    # Numeric comparison
    if isinstance(cobol_val, (int, float)) and isinstance(java_val, (int, float)):
        diff = abs(cobol_val - java_val)
        if diff == 0:
            return None
        if diff <= precision_threshold:
            return DifferentialDiff(
                record_index=0,
                field_name=field_name,
                cobol_value=cobol_val,
                java_value=java_val,
                category=DifferenceCategory.PRECISION,
                description=f"Numeric difference: {diff:.6f}",
            )
        return DifferentialDiff(
            record_index=0,
            field_name=field_name,
            cobol_value=cobol_val,
            java_value=java_val,
            category=DifferenceCategory.SEMANTIC,
            description=f"Significant numeric difference: {diff:.6f}",
        )

    # Type mismatch - always semantic
    return DifferentialDiff(
        record_index=0,
        field_name=field_name,
        cobol_value=cobol_val,
        java_value=java_val,
        category=DifferenceCategory.SEMANTIC,
        description=f"Type mismatch: {type(cobol_val).__name__} vs {type(java_val).__name__}",
    )


def compare_differential(
    cobol_path: str,
    java_path: str,
    precision_threshold: float = 0.005,
) -> DifferentialResult:
    """Compare COBOL output against Java output for differential analysis.

    Args:
        cobol_path: Path to COBOL output JSON file
        java_path: Path to Java output JSON file
        precision_threshold: Acceptable numeric precision difference

    Returns:
        DifferentialResult with categorized differences
    """
    with Path(cobol_path).open("r") as f:
        cobol_data = json.load(f)

    with Path(java_path).open("r") as f:
        java_data = json.load(f)

    cobol_records = cobol_data.get("records", [])
    java_records = java_data.get("records", [])

    result = DifferentialResult(
        cobol_source=cobol_path,
        java_source=java_path,
        passed=True,
        total_records=max(len(cobol_records), len(java_records)),
    )

    if len(cobol_records) != len(java_records):
        result.passed = False
        result.semantic_diffs.append(DifferentialDiff(
            record_index=-1,
            field_name="_record_count",
            cobol_value=len(cobol_records),
            java_value=len(java_records),
            category=DifferenceCategory.SEMANTIC,
            description="Record count mismatch between systems",
        ))

    compare_count = min(len(cobol_records), len(java_records))
    for i in range(compare_count):
        cobol_rec = cobol_records[i]
        java_rec = java_records[i]

        all_fields = set(cobol_rec.keys()) | set(java_rec.keys())
        all_fields.discard("_meta")

        for field_name in sorted(all_fields):
            cobol_val = cobol_rec.get(field_name)
            java_val = java_rec.get(field_name)

            diff = categorize_difference(field_name, cobol_val, java_val, precision_threshold)
            if diff is not None:
                diff.record_index = i
                if diff.category == DifferenceCategory.SEMANTIC:
                    result.semantic_diffs.append(diff)
                elif diff.category == DifferenceCategory.COSMETIC:
                    result.cosmetic_diffs.append(diff)
                elif diff.category == DifferenceCategory.PRECISION:
                    result.precision_diffs.append(diff)

    # Only semantic differences cause failure
    if result.semantic_diffs:
        result.passed = False

    # Generate summary
    parts = []
    if result.semantic_diffs:
        parts.append(f"{len(result.semantic_diffs)} semantic")
    if result.precision_diffs:
        parts.append(f"{len(result.precision_diffs)} precision")
    if result.cosmetic_diffs:
        parts.append(f"{len(result.cosmetic_diffs)} cosmetic")

    if result.passed:
        result.summary = f"PASS: {result.total_records} records compared, no semantic differences"
        if parts:
            result.summary += f" ({', '.join(parts)} non-blocking)"
    else:
        result.summary = f"FAIL: {', '.join(parts)} differences found across {result.total_records} records"

    return result
