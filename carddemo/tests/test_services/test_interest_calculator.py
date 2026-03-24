"""Tests for interest calculator — validates CBACT04C formula."""

from decimal import Decimal

from app.services.interest_calculator import calculate_monthly_interest


def test_interest_calculation_basic():
    """COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200"""
    result = calculate_monthly_interest(Decimal("500.00"), Decimal("18.00"))
    # 500 * 18 / 1200 = 7.50
    assert result == Decimal("7.50")


def test_interest_calculation_zero_rate():
    result = calculate_monthly_interest(Decimal("1000.00"), Decimal("0"))
    assert result == Decimal("0.00")


def test_interest_calculation_zero_balance():
    result = calculate_monthly_interest(Decimal("0.00"), Decimal("18.00"))
    assert result == Decimal("0.00")


def test_interest_calculation_large_balance():
    result = calculate_monthly_interest(Decimal("50000.00"), Decimal("24.99"))
    # 50000 * 24.99 / 1200 = 1041.25
    assert result == Decimal("1041.25")


def test_interest_calculation_rounding():
    result = calculate_monthly_interest(Decimal("333.33"), Decimal("15.50"))
    # 333.33 * 15.50 / 1200 = 4.30...
    assert result == Decimal("4.31")


def test_interest_calculation_small_values():
    result = calculate_monthly_interest(Decimal("1.00"), Decimal("1.00"))
    # 1 * 1 / 1200 = 0.000833...
    assert result == Decimal("0.00")
