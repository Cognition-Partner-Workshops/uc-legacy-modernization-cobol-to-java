"""
Reconciliation check definitions for CardDemo batch jobs.

Each check is a callable that takes pre-state and post-state snapshots
and returns a result dict with pass/fail status and variance details.
"""

from decimal import Decimal, ROUND_HALF_UP


def check_record_count(
    pre_count: int, post_count: int, expected_delta: int, label: str
) -> dict:
    """Verify that a record count changed by the expected delta.

    Args:
        pre_count: Record count before the batch job.
        post_count: Record count after the batch job.
        expected_delta: Expected change in record count.
        label: Human-readable label for the check.

    Returns:
        Result dict with pass/fail and variance.
    """
    actual_delta = post_count - pre_count
    variance = actual_delta - expected_delta
    return {
        "check": label,
        "pre_count": pre_count,
        "post_count": post_count,
        "expected_delta": expected_delta,
        "actual_delta": actual_delta,
        "variance": variance,
        "pass": variance == 0,
    }


def check_balance_equation(
    pre_balance: str,
    post_balance: str,
    transaction_sum: str,
    label: str,
) -> dict:
    """Verify that post_balance = pre_balance + transaction_sum.

    All values are strings representing decimal numbers.
    """
    pre = Decimal(pre_balance)
    post = Decimal(post_balance)
    txn = Decimal(transaction_sum)
    expected = pre + txn
    variance = post - expected
    return {
        "check": label,
        "pre_balance": str(pre),
        "post_balance": str(post),
        "transaction_sum": str(txn),
        "expected_post_balance": str(expected),
        "variance": str(variance),
        "pass": variance == 0,
    }


def check_key_uniqueness(records: list, key_field: str, label: str) -> dict:
    """Verify that all records have unique keys.

    Args:
        records: List of record dicts.
        key_field: Name of the key field to check.
        label: Human-readable label.

    Returns:
        Result dict with duplicate details.
    """
    seen = {}
    duplicates = []
    for idx, rec in enumerate(records):
        key = str(rec.get(key_field, "")).strip()
        if key in seen:
            duplicates.append({"key": key, "first_index": seen[key], "dup_index": idx})
        else:
            seen[key] = idx

    return {
        "check": label,
        "total_records": len(records),
        "unique_keys": len(seen),
        "duplicates": duplicates,
        "duplicate_count": len(duplicates),
        "pass": len(duplicates) == 0,
    }


def check_referential_integrity(
    child_records: list,
    child_key_field: str,
    parent_keys: set,
    label: str,
) -> dict:
    """Verify that all child foreign keys exist in the parent key set.

    Args:
        child_records: List of child record dicts.
        child_key_field: Foreign key field in child records.
        parent_keys: Set of valid parent key values.
        label: Human-readable label.

    Returns:
        Result dict with orphan details.
    """
    orphans = []
    for idx, rec in enumerate(child_records):
        child_key = str(rec.get(child_key_field, "")).strip()
        if child_key and child_key not in parent_keys:
            orphans.append({"index": idx, "key": child_key})

    return {
        "check": label,
        "child_count": len(child_records),
        "parent_key_count": len(parent_keys),
        "orphan_count": len(orphans),
        "orphans": orphans[:50],  # Limit output for large sets
        "pass": len(orphans) == 0,
    }


def check_sum_invariant(
    records: list,
    amount_field: str,
    expected_total: str,
    label: str,
) -> dict:
    """Verify that the sum of a numeric field matches an expected total.

    Args:
        records: List of record dicts.
        amount_field: Name of the numeric field to sum.
        expected_total: Expected sum as a string decimal.
        label: Human-readable label.

    Returns:
        Result dict with variance.
    """
    total = Decimal("0")
    parse_errors = []
    for idx, rec in enumerate(records):
        val = rec.get(amount_field, "0")
        try:
            total += Decimal(str(val))
        except Exception:
            parse_errors.append({"index": idx, "value": str(val)})

    expected = Decimal(expected_total)
    variance = total - expected

    return {
        "check": label,
        "record_count": len(records),
        "actual_sum": str(total),
        "expected_sum": str(expected),
        "variance": str(variance),
        "parse_errors": parse_errors,
        "pass": variance == 0 and len(parse_errors) == 0,
    }


def check_no_data_loss(
    source_count: int,
    target_count: int,
    label: str,
) -> dict:
    """Verify that no records were lost during a copy/backup operation.

    Args:
        source_count: Record count in the source.
        target_count: Record count in the target/backup.
        label: Human-readable label.

    Returns:
        Result dict.
    """
    variance = target_count - source_count
    return {
        "check": label,
        "source_count": source_count,
        "target_count": target_count,
        "variance": variance,
        "pass": variance == 0,
    }


def check_sort_order(records: list, key_field: str, label: str) -> dict:
    """Verify that records are sorted by key in ascending order.

    Args:
        records: List of record dicts.
        key_field: Field to check sort order on.
        label: Human-readable label.

    Returns:
        Result dict with out-of-order details.
    """
    violations = []
    for i in range(1, len(records)):
        prev_key = str(records[i - 1].get(key_field, "")).strip()
        curr_key = str(records[i].get(key_field, "")).strip()
        if curr_key < prev_key:
            violations.append(
                {
                    "index": i,
                    "prev_key": prev_key,
                    "curr_key": curr_key,
                }
            )

    return {
        "check": label,
        "record_count": len(records),
        "violations": violations[:50],
        "violation_count": len(violations),
        "pass": len(violations) == 0,
    }


def check_coverage(
    expected_keys: set,
    actual_keys: set,
    label: str,
) -> dict:
    """Verify that all expected keys are present in the actual set.

    Args:
        expected_keys: Set of keys that should be covered.
        actual_keys: Set of keys that are actually present.
        label: Human-readable label.

    Returns:
        Result dict with missing key details.
    """
    missing = expected_keys - actual_keys
    extra = actual_keys - expected_keys

    return {
        "check": label,
        "expected_count": len(expected_keys),
        "actual_count": len(actual_keys),
        "missing_count": len(missing),
        "extra_count": len(extra),
        "missing_keys": sorted(list(missing))[:50],
        "extra_keys": sorted(list(extra))[:50],
        "pass": len(missing) == 0,
    }
