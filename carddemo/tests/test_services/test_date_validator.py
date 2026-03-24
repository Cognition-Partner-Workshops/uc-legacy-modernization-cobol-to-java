"""Tests for date validator — validates CSUTLDTC date validation logic."""

from app.services.date_validator import validate_date_ccyymmdd, validate_date_of_birth


def test_valid_date():
    result = validate_date_ccyymmdd("20240115")
    assert result.is_valid is True


def test_valid_date_with_dashes():
    result = validate_date_ccyymmdd("2024-01-15")
    assert result.is_valid is True


def test_invalid_century():
    result = validate_date_ccyymmdd("18000115")
    assert result.is_valid is False
    assert "Century" in result.message


def test_invalid_month_zero():
    result = validate_date_ccyymmdd("20240015")
    assert result.is_valid is False
    assert "Month" in result.message


def test_invalid_month_thirteen():
    result = validate_date_ccyymmdd("20241315")
    assert result.is_valid is False


def test_invalid_day_zero():
    result = validate_date_ccyymmdd("20240100")
    assert result.is_valid is False


def test_invalid_day_32():
    result = validate_date_ccyymmdd("20240132")
    assert result.is_valid is False


def test_february_29_leap_year():
    result = validate_date_ccyymmdd("20240229")
    assert result.is_valid is True


def test_february_29_non_leap_year():
    result = validate_date_ccyymmdd("20230229")
    assert result.is_valid is False
    assert "leap year" in result.message.lower() or "29 days" in result.message.lower()


def test_february_30():
    result = validate_date_ccyymmdd("20240230")
    assert result.is_valid is False


def test_april_31():
    result = validate_date_ccyymmdd("20240431")
    assert result.is_valid is False
    assert "31 days" in result.message


def test_short_string():
    result = validate_date_ccyymmdd("2024")
    assert result.is_valid is False


def test_dob_valid():
    result = validate_date_of_birth("19800115")
    assert result.is_valid is True


def test_dob_future():
    result = validate_date_of_birth("20990115")
    assert result.is_valid is False
    assert "future" in result.message.lower()


def test_century_2000():
    result = validate_date_ccyymmdd("20000229")
    assert result.is_valid is True  # 2000 is a leap year


def test_century_1900():
    result = validate_date_ccyymmdd("19000228")
    assert result.is_valid is True
