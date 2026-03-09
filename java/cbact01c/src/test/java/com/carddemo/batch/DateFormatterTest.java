package com.carddemo.batch;

import com.carddemo.batch.util.DateFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the DateFormatter utility, which replaces the COBOL
 * COBDATFT assembler subroutine.
 */
class DateFormatterTest {

    @ParameterizedTest(name = "YYYY-MM-DD \"{0}\" -> YYYYMMDD \"{1}\"")
    @CsvSource({
            "'2025-05-20', '20250520'",
            "'2024-08-11', '20240811'",
            "'2024-01-10', '20240110'",
            "'2014-11-20', '20141120'",
            "'2013-06-19', '20130619'",
    })
    void testConvertYyyyMmDdToCompact(String input, String expected) {
        assertEquals(expected, DateFormatter.convertYyyyMmDdToCompact(input));
    }

    @ParameterizedTest(name = "YYYYMMDD \"{0}\" -> YYYY-MM-DD \"{1}\"")
    @CsvSource({
            "'20250520', '2025-05-20'",
            "'20240811', '2024-08-11'",
    })
    void testConvertCompactToYyyyMmDd(String input, String expected) {
        assertEquals(expected, DateFormatter.convertCompactToYyyyMmDd(input));
    }

    @Test
    void testNullHandling() {
        assertNull(DateFormatter.convertYyyyMmDdToCompact(null));
        assertNull(DateFormatter.convertCompactToYyyyMmDd(null));
    }

    @Test
    void testShortStringHandling() {
        assertEquals("abc", DateFormatter.convertYyyyMmDdToCompact("abc"));
        assertEquals("abc", DateFormatter.convertCompactToYyyyMmDd("abc"));
    }

    @Test
    void testRoundTrip() {
        String original = "2025-05-20";
        String compact = DateFormatter.convertYyyyMmDdToCompact(original);
        String backToOriginal = DateFormatter.convertCompactToYyyyMmDd(compact);
        assertEquals(original, backToOriginal);
    }
}
