package com.carddemo.shared.util;

import com.carddemo.shared.model.DateValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link DateValidator} (port of COBOL {@code CSUTLDTC}). */
class DateValidatorTest {

    private final DateValidator validator = new DateValidator();

    @ParameterizedTest
    @CsvSource({
            "2023-01-15, YYYY-MM-DD",
            "01/15/2023, MM/DD/YYYY",
            "15/01/2023, DD/MM/YYYY",
            "20230115,   YYYYMMDD"
    })
    void acceptsValidDatesForEachMask(String date, String mask) {
        DateValidationResult result = validator.validate(date, mask);
        assertTrue(result.valid(), () -> "expected valid: " + result.message());
        assertEquals(DateValidator.SEVERITY_VALID, result.severityCode());
        assertTrue(result.message().contains("Date is valid"));
        assertTrue(result.message().contains(date));
        assertTrue(result.message().contains(mask));
    }

    @Test
    void acceptsLeapDayInLeapYear() {
        assertTrue(validator.validate("2024-02-29", "YYYY-MM-DD").valid());
        assertTrue(validator.validate("2000-02-29", "YYYY-MM-DD").valid());
    }

    @Test
    void rejectsLeapDayInNonLeapYear() {
        DateValidationResult result = validator.validate("2023-02-29", "YYYY-MM-DD");
        assertFalse(result.valid());
        assertEquals(DateValidator.SEVERITY_INVALID, result.severityCode());
        assertTrue(result.message().contains("Date is invalid"));
    }

    @Test
    void rejectsCenturyYearThatIsNotLeap() {
        assertFalse(validator.validate("1900-02-29", "YYYY-MM-DD").valid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2023-02-30",
            "2023-13-01",
            "2023-00-10",
            "2023-04-31",
            "2023-01-32"
    })
    void rejectsImpossibleCalendarDates(String date) {
        assertFalse(validator.validate(date, "YYYY-MM-DD").valid());
    }

    @Test
    void rejectsDateNotMatchingMask() {
        assertFalse(validator.validate("01/15/2023", "YYYY-MM-DD").valid());
        assertFalse(validator.validate("2023-01-15", "MM/DD/YYYY").valid());
    }

    @Test
    void distinguishesUsAndEuropeanMasks() {
        assertTrue(validator.validate("13/01/2023", "DD/MM/YYYY").valid());
        assertFalse(validator.validate("13/01/2023", "MM/DD/YYYY").valid());
    }

    @Test
    void rejectsUnsupportedMaskWithBadPicString() {
        DateValidationResult result = validator.validate("2023-01-15", "DD-MON-YYYY");
        assertFalse(result.valid());
        assertEquals(DateValidator.SEVERITY_INVALID, result.severityCode());
        assertTrue(result.message().contains("Bad Pic String"));
    }

    @Test
    void rejectsNullAndBlankInput() {
        assertFalse(validator.validate(null, "YYYY-MM-DD").valid());
        assertFalse(validator.validate("   ", "YYYY-MM-DD").valid());
        assertFalse(validator.validate("2023-01-15", null).valid());
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertTrue(validator.validate("  2023-01-15  ", "YYYY-MM-DD").valid());
    }

    @Test
    void acceptsBoundaryMonthAndDay() {
        assertTrue(validator.validate("2023-12-31", "YYYY-MM-DD").valid());
        assertTrue(validator.validate("2023-01-01", "YYYY-MM-DD").valid());
    }
}
