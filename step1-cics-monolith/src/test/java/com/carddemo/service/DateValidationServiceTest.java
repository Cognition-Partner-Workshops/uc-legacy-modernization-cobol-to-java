package com.carddemo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary and risk-based tests for CSUTLDTC.cbl (Date Validation Utility).
 * Covers: valid dates, invalid formats, edge cases, leap years, month/day boundaries,
 * null/empty inputs, format variations.
 */
class DateValidationServiceTest {

    private final DateValidationService service = new DateValidationService();

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Valid date returns isValid=true")
    void testValidDate() {
        DateValidationService.ValidationResult result = service.validateYyyyMmDd("2024-01-15");
        assertTrue(result.isValid());
        assertEquals("0000", result.severityCode());
    }

    @Test
    @DisplayName("Invalid date format returns isValid=false")
    void testInvalidDateFormat() {
        DateValidationService.ValidationResult result = service.validateYyyyMmDd("01-15-2024");
        assertFalse(result.isValid());
        assertEquals("0008", result.severityCode());
    }

    @Test
    @DisplayName("Empty date returns error code 0001")
    void testEmptyDate() {
        DateValidationService.ValidationResult result = service.validateYyyyMmDd("");
        assertFalse(result.isValid());
        assertEquals("0008", result.severityCode());
        assertEquals("0001", result.messageNumber());
    }

    @Test
    @DisplayName("Null date returns error code 0001")
    void testNullDate() {
        DateValidationService.ValidationResult result = service.validateYyyyMmDd(null);
        assertFalse(result.isValid());
        assertEquals("0008", result.severityCode());
        assertEquals("0001", result.messageNumber());
    }

    @Test
    @DisplayName("Leap year date Feb 29 is valid")
    void testLeapYearDate() {
        assertTrue(service.validateYyyyMmDd("2024-02-29").isValid());
    }

    @Test
    @DisplayName("Non-leap year Feb 29 is invalid")
    void testInvalidLeapYearDate() {
        assertFalse(service.validateYyyyMmDd("2023-02-29").isValid());
    }

    @Test
    @DisplayName("Month 13 is invalid")
    void testInvalidMonth() {
        assertFalse(service.validateYyyyMmDd("2024-13-01").isValid());
    }

    @Test
    @DisplayName("Day 32 is invalid")
    void testInvalidDay() {
        assertFalse(service.validateYyyyMmDd("2024-01-32").isValid());
    }

    // ===== BOUNDARY TESTS - DATE RANGES =====

    @Test
    @DisplayName("First day of year is valid")
    void testFirstDayOfYear() {
        assertTrue(service.validateYyyyMmDd("2024-01-01").isValid());
    }

    @Test
    @DisplayName("Last day of year is valid")
    void testLastDayOfYear() {
        assertTrue(service.validateYyyyMmDd("2024-12-31").isValid());
    }

    @Test
    @DisplayName("Month 00 is invalid")
    void testMonthZero() {
        assertFalse(service.validateYyyyMmDd("2024-00-15").isValid());
    }

    @Test
    @DisplayName("Day 00 is invalid")
    void testDayZero() {
        assertFalse(service.validateYyyyMmDd("2024-01-00").isValid());
    }

    @Test
    @DisplayName("Feb 28 on non-leap year is valid")
    void testFeb28NonLeapYear() {
        assertTrue(service.validateYyyyMmDd("2023-02-28").isValid());
    }

    @Test
    @DisplayName("Apr 30 is valid (30-day month)")
    void testApr30() {
        assertTrue(service.validateYyyyMmDd("2024-04-30").isValid());
    }

    @Test
    @DisplayName("Apr 31 is invalid (30-day month)")
    void testApr31() {
        assertFalse(service.validateYyyyMmDd("2024-04-31").isValid());
    }

    @Test
    @DisplayName("Jun 30 is valid")
    void testJun30() {
        assertTrue(service.validateYyyyMmDd("2024-06-30").isValid());
    }

    @Test
    @DisplayName("Jun 31 is invalid")
    void testJun31() {
        assertFalse(service.validateYyyyMmDd("2024-06-31").isValid());
    }

    @Test
    @DisplayName("Nov 30 is valid")
    void testNov30() {
        assertTrue(service.validateYyyyMmDd("2024-11-30").isValid());
    }

    @Test
    @DisplayName("Nov 31 is invalid")
    void testNov31() {
        assertFalse(service.validateYyyyMmDd("2024-11-31").isValid());
    }

    // ===== BOUNDARY TESTS - LEAP YEAR EDGE CASES =====

    @Test
    @DisplayName("Century year 2000 is a leap year (divisible by 400)")
    void testCenturyLeapYear2000() {
        assertTrue(service.validateYyyyMmDd("2000-02-29").isValid());
    }

    @Test
    @DisplayName("Century year 1900 is NOT a leap year")
    void testCenturyNonLeapYear1900() {
        assertFalse(service.validateYyyyMmDd("1900-02-29").isValid());
    }

    @Test
    @DisplayName("Year 2100 is NOT a leap year")
    void testYear2100NotLeap() {
        assertFalse(service.validateYyyyMmDd("2100-02-29").isValid());
    }

    @Test
    @DisplayName("Year 2400 IS a leap year (divisible by 400)")
    void testYear2400IsLeap() {
        assertTrue(service.validateYyyyMmDd("2400-02-29").isValid());
    }

    // ===== BOUNDARY TESTS - FORMAT VARIATIONS =====

    @Test
    @DisplayName("Whitespace-only date is invalid")
    void testWhitespaceOnly() {
        assertFalse(service.validateYyyyMmDd("   ").isValid());
    }

    @Test
    @DisplayName("Date with leading/trailing spaces is trimmed and valid")
    void testDateWithSpaces() {
        assertTrue(service.validateYyyyMmDd(" 2024-01-15 ").isValid());
    }

    @Test
    @DisplayName("Date with slashes is invalid for YYYY-MM-DD format")
    void testDateWithSlashes() {
        assertFalse(service.validateYyyyMmDd("2024/01/15").isValid());
    }

    @Test
    @DisplayName("Single-digit month/day without padding is invalid")
    void testSingleDigitNoPadding() {
        assertFalse(service.validateYyyyMmDd("2024-1-5").isValid());
    }

    // ===== ALTERNATE FORMAT TESTS =====

    @Test
    @DisplayName("Valid MM/DD/YY format")
    void testValidMmDdYy() {
        assertTrue(service.validateDate("01/15/24", "MM/DD/YY").isValid());
    }

    @Test
    @DisplayName("Invalid MM/DD/YY - month 13")
    void testInvalidMmDdYy() {
        assertFalse(service.validateDate("13/15/24", "MM/DD/YY").isValid());
    }

    @Test
    @DisplayName("Null format returns error code 0002")
    void testNullFormat() {
        DateValidationService.ValidationResult result = service.validateDate("2024-01-15", null);
        assertFalse(result.isValid());
        assertEquals("0002", result.messageNumber());
    }

    @Test
    @DisplayName("Empty format returns error code 0002")
    void testEmptyFormat() {
        DateValidationService.ValidationResult result = service.validateDate("2024-01-15", "");
        assertFalse(result.isValid());
        assertEquals("0002", result.messageNumber());
    }

    @Test
    @DisplayName("Alphabetic date string is invalid")
    void testAlphabeticDate() {
        assertFalse(service.validateYyyyMmDd("ABCD-EF-GH").isValid());
    }

    @Test
    @DisplayName("Special characters in date are invalid")
    void testSpecialCharactersInDate() {
        assertFalse(service.validateYyyyMmDd("2024@01#15").isValid());
    }

    @Test
    @DisplayName("Very long date string is invalid")
    void testVeryLongDateString() {
        assertFalse(service.validateYyyyMmDd("2024-01-15-extra-stuff").isValid());
    }
}
