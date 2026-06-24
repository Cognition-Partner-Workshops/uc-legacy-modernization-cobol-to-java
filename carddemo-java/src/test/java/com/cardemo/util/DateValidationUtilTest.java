package com.cardemo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DateValidationUtil (COBOL CSUTLDTC equivalent).
 */
class DateValidationUtilTest {

    private DateValidationUtil util;

    @BeforeEach
    void setUp() {
        util = new DateValidationUtil();
    }

    @Nested
    @DisplayName("validateDate() - YYYY-MM-DD format")
    class ValidateDateYYYYMMDDTests {

        @Test
        @DisplayName("Should validate a correct date")
        void shouldValidateCorrectDate() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-15", "YYYY-MM-DD");

            assertTrue(result.isValid());
            assertEquals(0, result.getSeverityCode());
            assertEquals("Date is valid", result.getResultMessage());
            assertEquals("2025-01-15", result.getTestedDate());
            assertEquals("YYYY-MM-DD", result.getFormatUsed());
        }

        @Test
        @DisplayName("Should validate leap year Feb 29")
        void shouldValidateLeapYearFeb29() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2024-02-29", "YYYY-MM-DD");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject non-leap year Feb 29")
        void shouldRejectNonLeapYearFeb29() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-02-29", "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
        }

        @Test
        @DisplayName("Should reject invalid month 13")
        void shouldRejectInvalidMonth13() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-13-01", "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
        }

        @Test
        @DisplayName("Should reject invalid month 00")
        void shouldRejectInvalidMonth00() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-00-01", "YYYY-MM-DD");

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid day 32")
        void shouldRejectInvalidDay32() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-32", "YYYY-MM-DD");

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid day 00")
        void shouldRejectInvalidDay00() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-00", "YYYY-MM-DD");

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should validate end of month dates")
        void shouldValidateEndOfMonthDates() {
            assertTrue(util.validateDate("2025-01-31", "YYYY-MM-DD").isValid());
            assertTrue(util.validateDate("2025-03-31", "YYYY-MM-DD").isValid());
            assertFalse(util.validateDate("2025-04-31", "YYYY-MM-DD").isValid());
            assertFalse(util.validateDate("2025-06-31", "YYYY-MM-DD").isValid());
        }

        @Test
        @DisplayName("Should validate first day of year")
        void shouldValidateFirstDayOfYear() {
            assertTrue(util.validateDate("2025-01-01", "YYYY-MM-DD").isValid());
        }

        @Test
        @DisplayName("Should validate last day of year")
        void shouldValidateLastDayOfYear() {
            assertTrue(util.validateDate("2025-12-31", "YYYY-MM-DD").isValid());
        }
    }

    @Nested
    @DisplayName("validateDate() - MM/DD/YYYY format")
    class ValidateDateMMDDYYYYTests {

        @Test
        @DisplayName("Should validate correct date in MM/DD/YYYY")
        void shouldValidateCorrectDate() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("01/15/2025", "MM/DD/YYYY");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid date in MM/DD/YYYY")
        void shouldRejectInvalidDate() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("13/01/2025", "MM/DD/YYYY");

            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("validateDate() - YYYYMMDD format")
    class ValidateDateYYYYMMDDCompactTests {

        @Test
        @DisplayName("Should validate correct date in YYYYMMDD")
        void shouldValidateCorrectDate() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("20250115", "YYYYMMDD");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid date in YYYYMMDD")
        void shouldRejectInvalidDate() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("20251301", "YYYYMMDD");

            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("validateDate() - edge cases")
    class ValidateDateEdgeCaseTests {

        @Test
        @DisplayName("Should reject null date string")
        void shouldRejectNullDateString() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate(null, "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
            assertEquals("Insufficient", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject blank date string")
        void shouldRejectBlankDateString() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("   ", "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
            assertEquals("Insufficient", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject empty date string")
        void shouldRejectEmptyDateString() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("", "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals("Insufficient", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject null format")
        void shouldRejectNullFormat() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-15", null);

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
            assertEquals("Bad Pic String", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject blank format")
        void shouldRejectBlankFormat() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-15", "  ");

            assertFalse(result.isValid());
            assertEquals("Bad Pic String", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject unsupported format")
        void shouldRejectUnsupportedFormat() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("2025-01-15", "DD-MMM-YYYY");

            assertFalse(result.isValid());
            assertEquals("Bad Pic String", result.getResultMessage());
        }

        @Test
        @DisplayName("Should reject non-numeric date content")
        void shouldRejectNonNumericDateContent() {
            DateValidationUtil.ValidationResult result =
                    util.validateDate("ABCD-EF-GH", "YYYY-MM-DD");

            assertFalse(result.isValid());
            assertEquals(4, result.getSeverityCode());
        }
    }

    @Nested
    @DisplayName("validateDateYYYYMMDD()")
    class ValidateDateYYYYMMDDShortcutTests {

        @Test
        @DisplayName("Should validate using default YYYY-MM-DD format")
        void shouldValidateWithDefaultFormat() {
            DateValidationUtil.ValidationResult result =
                    util.validateDateYYYYMMDD("2025-06-15");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid date with default format")
        void shouldRejectInvalidWithDefaultFormat() {
            DateValidationUtil.ValidationResult result =
                    util.validateDateYYYYMMDD("2025-13-01");

            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("isBeforeOrEqual()")
    class IsBeforeOrEqualTests {

        @Test
        @DisplayName("Should return true when date1 is before date2")
        void shouldReturnTrueWhenBefore() {
            assertTrue(util.isBeforeOrEqual("2025-01-01", "2025-12-31"));
        }

        @Test
        @DisplayName("Should return true when dates are equal")
        void shouldReturnTrueWhenEqual() {
            assertTrue(util.isBeforeOrEqual("2025-06-15", "2025-06-15"));
        }

        @Test
        @DisplayName("Should return false when date1 is after date2")
        void shouldReturnFalseWhenAfter() {
            assertFalse(util.isBeforeOrEqual("2025-12-31", "2025-01-01"));
        }

        @Test
        @DisplayName("Should return false when date1 is null")
        void shouldReturnFalseWhenDate1Null() {
            assertFalse(util.isBeforeOrEqual(null, "2025-01-01"));
        }

        @Test
        @DisplayName("Should return false when date2 is null")
        void shouldReturnFalseWhenDate2Null() {
            assertFalse(util.isBeforeOrEqual("2025-01-01", null));
        }
    }

    @Nested
    @DisplayName("isFutureDate()")
    class IsFutureDateTests {

        @Test
        @DisplayName("Should return true for far future date")
        void shouldReturnTrueForFutureDate() {
            assertTrue(util.isFutureDate("2099-12-31", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("Should return false for past date")
        void shouldReturnFalseForPastDate() {
            assertFalse(util.isFutureDate("2020-01-01", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("Should return false for invalid date")
        void shouldReturnFalseForInvalidDate() {
            assertFalse(util.isFutureDate("not-a-date", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("Should return false for null date")
        void shouldReturnFalseForNullDate() {
            assertFalse(util.isFutureDate(null, "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("Should return false for unsupported format")
        void shouldReturnFalseForUnsupportedFormat() {
            assertFalse(util.isFutureDate("2099-12-31", "UNSUPPORTED"));
        }
    }
}
