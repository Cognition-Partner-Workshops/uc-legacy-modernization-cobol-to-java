package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DateConverter that replaces the COBOL COBDATFT assembler routine.
 */
class DateConverterTest {

    @ParameterizedTest(name = "YYYY-MM-DD \"{0}\" → YYYYMMDD padded")
    @CsvSource({
            "2025-05-20, 20250520",
            "2014-11-20, 20141120",
            "2024-08-11, 20240811",
            "2023-01-27, 20230127",
            "2009-06-17, 20090617",
    })
    @DisplayName("convert YYYY-MM-DD to YYYYMMDD (padded to 10 chars)")
    void testYyyyMmDdToYyyymmdd(String input, String expectedPrefix) {
        String result = DateConverter.convert(input, DateConverter.TYPE_YYYY_MM_DD, DateConverter.TYPE_YYYYMMDD);
        assertEquals(10, result.length(), "Output should be padded to 10 characters");
        assertEquals(expectedPrefix + "  ", result, "YYYYMMDD + 2 trailing spaces");
    }

    @ParameterizedTest(name = "YYYYMMDD \"{0}\" → YYYY-MM-DD \"{1}\"")
    @CsvSource({
            "20250520, 2025-05-20",
            "20141120, 2014-11-20",
    })
    @DisplayName("convert YYYYMMDD to YYYY-MM-DD")
    void testYyyymmddToYyyyMmDd(String input, String expected) {
        String result = DateConverter.convert(input, DateConverter.TYPE_YYYYMMDD, DateConverter.TYPE_YYYY_MM_DD);
        assertEquals(expected, result);
        assertEquals(10, result.length());
    }

    @Test
    @DisplayName("round trip: YYYY-MM-DD → YYYYMMDD → YYYY-MM-DD")
    void testRoundTrip() {
        String original = "2025-05-20";
        String yyyymmdd = DateConverter.convert(original, DateConverter.TYPE_YYYY_MM_DD, DateConverter.TYPE_YYYYMMDD);
        // yyyymmdd is "20250520  " (padded) — trim for reverse conversion
        String backToOriginal = DateConverter.convert(yyyymmdd.trim(),
                DateConverter.TYPE_YYYYMMDD, DateConverter.TYPE_YYYY_MM_DD);
        assertEquals(original, backToOriginal);
    }

    @Test
    @DisplayName("null or blank input throws IllegalArgumentException")
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert(null, 2, 2));
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("   ", 2, 2));
    }

    @Test
    @DisplayName("invalid input type throws IllegalArgumentException")
    void testInvalidType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", 99, 2));
    }

    @Test
    @DisplayName("matching COBOL: type-2 in, type-2 out reissue date conversion")
    void testCobolDateConversion() {
        // Exactly as the COBOL program does:
        // MOVE '2' TO CODATECN-TYPE  (input = YYYY-MM-DD)
        // MOVE '2' TO CODATECN-OUTTYPE (output = YYYYMMDD)
        String result = DateConverter.convert("2025-05-20",
                DateConverter.TYPE_YYYY_MM_DD, DateConverter.TYPE_YYYYMMDD);
        // COBOL would produce "20250520" in a 20-byte field, then MOVE to PIC X(10) → "20250520  "
        assertEquals("20250520  ", result);
    }
}
