#!/usr/bin/env python3
"""
API response contract validator.

Validates Java API responses against JSON Schema contracts generated from
COBOL copybook definitions.

Usage:
    python contract_validator.py <schema.json> <response.json>
    python contract_validator.py --url http://localhost:8080/api/accounts/1 \\
                                 --schema contracts/account_record.schema.json
"""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class ValidationError:
    """A single schema validation error."""
    path: str
    message: str
    schema_path: str = ""


@dataclass
class ValidationResult:
    """Result of validating a response against a schema."""
    schema_path: str
    target: str
    valid: bool = True
    errors: list[ValidationError] = field(default_factory=list)

    def summary(self) -> str:
        status = "PASS" if self.valid else "FAIL"
        lines = [
            f"[{status}] Contract: {self.schema_path}  Target: {self.target}",
        ]
        if self.errors:
            lines.append(f"  Errors ({len(self.errors)}):")
            for e in self.errors[:20]:
                lines.append(f"    {e.path}: {e.message}")
            if len(self.errors) > 20:
                lines.append(f"    ... and {len(self.errors) - 20} more")
        return "\n".join(lines)


def _check_type(value: Any, expected_type: str) -> bool:
    """Check if a value matches the expected JSON Schema type."""
    if expected_type == "string":
        return isinstance(value, str)
    if expected_type == "integer":
        return isinstance(value, int) and not isinstance(value, bool)
    if expected_type == "number":
        return isinstance(value, (int, float)) and not isinstance(value, bool)
    if expected_type == "boolean":
        return isinstance(value, bool)
    if expected_type == "array":
        return isinstance(value, list)
    if expected_type == "object":
        return isinstance(value, dict)
    if expected_type == "null":
        return value is None
    return True


def validate_record(
    record: dict[str, Any],
    schema: dict[str, Any],
    path_prefix: str = "",
) -> list[ValidationError]:
    """Validate a single record dict against a JSON Schema."""
    errors: list[ValidationError] = []
    properties = schema.get("properties", {})
    required = set(schema.get("required", []))

    # Check required fields
    for field_name in required:
        if field_name not in record:
            errors.append(ValidationError(
                path=f"{path_prefix}.{field_name}" if path_prefix else field_name,
                message=f"Required field missing",
            ))

    # Check types and constraints
    for field_name, value in record.items():
        if field_name not in properties:
            continue  # additionalProperties is allowed

        prop_schema = properties[field_name]
        field_path = f"{path_prefix}.{field_name}" if path_prefix else field_name

        # Type check
        expected_type = prop_schema.get("type")
        if expected_type and not _check_type(value, expected_type):
            errors.append(ValidationError(
                path=field_path,
                message=f"Expected type '{expected_type}', got '{type(value).__name__}'",
            ))
            continue

        # maxLength for strings
        max_length = prop_schema.get("maxLength")
        if max_length and isinstance(value, str) and len(value) > max_length:
            errors.append(ValidationError(
                path=field_path,
                message=f"String length {len(value)} exceeds maxLength {max_length}",
            ))

    return errors


def validate_response(
    schema_path: str | Path,
    response_data: Any,
    target_label: str = "",
) -> ValidationResult:
    """
    Validate an API response against a JSON Schema.

    The response_data can be:
      - A single record dict -> validated against the schema directly
      - A list of records -> each record validated against the schema
      - A dict with "records" key -> each record validated
    """
    schema_path = Path(schema_path)
    with open(schema_path, "r", encoding="utf-8") as fh:
        schema = json.load(fh)

    result = ValidationResult(
        schema_path=str(schema_path),
        target=target_label or "response",
    )

    # Normalise to a list of records
    records: list[dict[str, Any]]
    if isinstance(response_data, list):
        records = response_data
    elif isinstance(response_data, dict) and "records" in response_data:
        records = response_data["records"]
    elif isinstance(response_data, dict):
        records = [response_data]
    else:
        result.valid = False
        result.errors.append(ValidationError(
            path="$", message=f"Unexpected response type: {type(response_data).__name__}",
        ))
        return result

    for idx, record in enumerate(records):
        errs = validate_record(record, schema, path_prefix=f"records[{idx}]")
        result.errors.extend(errs)

    result.valid = len(result.errors) == 0
    return result


def validate_file(
    schema_path: str | Path,
    response_path: str | Path,
) -> ValidationResult:
    """Validate a JSON file against a schema file."""
    with open(response_path, "r", encoding="utf-8") as fh:
        response_data = json.load(fh)
    return validate_response(schema_path, response_data, target_label=str(response_path))


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    if len(sys.argv) < 3:
        print("Usage: contract_validator.py <schema.json> <response.json>")
        sys.exit(1)

    schema = sys.argv[1]
    response = sys.argv[2]

    result = validate_file(schema, response)
    print(result.summary())
    sys.exit(0 if result.valid else 1)


if __name__ == "__main__":
    main()
