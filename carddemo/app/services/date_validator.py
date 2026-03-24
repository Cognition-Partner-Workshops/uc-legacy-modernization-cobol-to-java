"""Date validation utility — from CSUTLDTC.cbl + CSUTLDPY.cpy + CSUTLDWY.cpy.

Replaces the COBOL CEEDAYS API call and all date validation paragraphs:
  - EDIT-DATE-CCYYMMDD: Main entry point
  - EDIT-YEAR-CCYY: Year/century validation (only 19xx/20xx valid)
  - EDIT-MONTH: Month validation (1-12)
  - EDIT-DAY: Day validation (1-31)
  - EDIT-DAY-MONTH-YEAR: Cross-field validation (leap year, 30/31 days)
  - EDIT-DATE-LE: Language Environment date verification
  - EDIT-DATE-OF-BIRTH: Future date check
"""

import calendar
from dataclasses import dataclass, field
from datetime import date


@dataclass
class DateValidationResult:
    is_valid: bool = False
    year_ok: bool = False
    month_ok: bool = False
    day_ok: bool = False
    message: str = ""
    errors: list[str] = field(default_factory=list)


def validate_date_ccyymmdd(date_str: str, variable_name: str = "Date") -> DateValidationResult:
    """Validate a CCYYMMDD date string — replaces EDIT-DATE-CCYYMMDD paragraph.

    Args:
        date_str: Date string in CCYYMMDD (8 chars) or CCYY-MM-DD (10 chars) format.
        variable_name: Name of the field being validated (for error messages).

    Returns:
        DateValidationResult with validation status and error messages.
    """
    result = DateValidationResult()

    # Normalize: remove dashes if present
    clean = date_str.replace("-", "").strip()
    if len(clean) != 8:
        result.message = f"{variable_name}: Date must be 8 digits (CCYYMMDD)."
        result.errors.append(result.message)
        return result

    # Extract components
    cc_str = clean[0:2]
    yy_str = clean[2:4]
    mm_str = clean[4:6]
    dd_str = clean[6:8]
    yyyy_str = clean[0:4]

    # EDIT-YEAR-CCYY: validate year
    if not yyyy_str.isdigit():
        result.message = f"{variable_name} must be 4 digit number."
        result.errors.append(result.message)
        return result

    # Century check: only 19 and 20 valid (from CSUTLDWY.cpy 88-level conditions)
    cc = int(cc_str)
    if cc not in (19, 20):
        result.message = f"{variable_name}: Century is not valid."
        result.errors.append(result.message)
        return result
    result.year_ok = True

    # EDIT-MONTH: validate month
    if not mm_str.isdigit():
        result.message = f"{variable_name}: Month must be a number between 1 and 12."
        result.errors.append(result.message)
        return result
    mm = int(mm_str)
    if mm < 1 or mm > 12:
        result.message = f"{variable_name}: Month must be a number between 1 and 12."
        result.errors.append(result.message)
        return result
    result.month_ok = True

    # EDIT-DAY: validate day
    if not dd_str.isdigit():
        result.message = f"{variable_name}: day must be a number between 1 and 31."
        result.errors.append(result.message)
        return result
    dd = int(dd_str)
    if dd < 1 or dd > 31:
        result.message = f"{variable_name}: day must be a number between 1 and 31."
        result.errors.append(result.message)
        return result

    # EDIT-DAY-MONTH-YEAR: cross-field validation
    yyyy = int(yyyy_str)
    yy = int(yy_str)

    # 31-day month check
    months_with_31 = {1, 3, 5, 7, 8, 10, 12}
    if mm not in months_with_31 and dd == 31:
        result.message = f"{variable_name}: Cannot have 31 days in this month."
        result.errors.append(result.message)
        return result

    # February checks
    if mm == 2:
        if dd >= 30:
            result.message = f"{variable_name}: Cannot have {dd} days in this month."
            result.errors.append(result.message)
            return result
        if dd == 29:
            # Leap year check (from CSUTLDPY.cpy lines 245-271)
            divisor = 400 if yy == 0 else 4
            if yyyy % divisor != 0:
                result.message = f"{variable_name}: Not a leap year. Cannot have 29 days in this month."
                result.errors.append(result.message)
                return result

    result.day_ok = True

    # Final validation using Python's calendar (replaces CEEDAYS API call)
    try:
        max_day = calendar.monthrange(yyyy, mm)[1]
        if dd > max_day:
            result.message = f"{variable_name}: Invalid day for the given month/year."
            result.errors.append(result.message)
            return result
    except ValueError:
        result.message = f"{variable_name}: Date is invalid."
        result.errors.append(result.message)
        return result

    result.is_valid = True
    result.message = "Date is valid"
    return result


def validate_date_of_birth(date_str: str, variable_name: str = "Date of Birth") -> DateValidationResult:
    """Validate date of birth — replaces EDIT-DATE-OF-BIRTH paragraph.

    Ensures the date is valid AND is not in the future.
    """
    result = validate_date_ccyymmdd(date_str, variable_name)
    if not result.is_valid:
        return result

    # Future date check (from CSUTLDPY.cpy EDIT-DATE-OF-BIRTH)
    clean = date_str.replace("-", "").strip()
    yyyy = int(clean[0:4])
    mm = int(clean[4:6])
    dd = int(clean[6:8])

    try:
        dob = date(yyyy, mm, dd)
        if dob >= date.today():
            result.is_valid = False
            result.message = f"{variable_name}: cannot be in the future"
            result.errors.append(result.message)
    except ValueError:
        result.is_valid = False
        result.message = f"{variable_name}: Date is invalid"
        result.errors.append(result.message)

    return result
