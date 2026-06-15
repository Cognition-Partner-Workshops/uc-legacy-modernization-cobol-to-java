package com.carddemo.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @ParameterizedTest
    @CsvSource({
            "'2025-05-20', '20250520'",
            "'2014-11-20', '20141120'",
            "'2013-06-19', '20130619'",
            "'2024-01-01', '20240101'",
            "'2023-12-31', '20231231'",
    })
    void testConvertYyyyMmDdToCompact(String input, String expected) {
        assertEquals(expected, DateConverter.convertYyyyMmDdToCompact(input));
    }

    @ParameterizedTest
    @CsvSource({
            "'20250520', '2025-05-20'",
            "'20141120', '2014-11-20'",
            "'20130619', '2013-06-19'",
    })
    void testConvertCompactToYyyyMmDd(String input, String expected) {
        assertEquals(expected, DateConverter.convertCompactToYyyyMmDd(input));
    }

    @Test
    void testRoundTrip() {
        final String original = "2025-05-20";
        final String compact = DateConverter.convertYyyyMmDdToCompact(original);
        final String roundTripped = DateConverter.convertCompactToYyyyMmDd(compact);
        assertEquals(original, roundTripped);
    }

    @Test
    void testExtractYear() {
        assertEquals("2025", DateConverter.extractYear("2025-05-20"));
        assertEquals("2014", DateConverter.extractYear("2014-11-20"));
    }

    @Test
    void testNullAndShortInputHandling() {
        assertNull(DateConverter.convertYyyyMmDdToCompact(null));
        assertEquals("short", DateConverter.convertYyyyMmDdToCompact("short"));
        assertNull(DateConverter.convertCompactToYyyyMmDd(null));
        assertEquals("short", DateConverter.convertCompactToYyyyMmDd("short"));
        assertEquals("", DateConverter.extractYear(null));
        assertEquals("", DateConverter.extractYear("ab"));
    }
}
