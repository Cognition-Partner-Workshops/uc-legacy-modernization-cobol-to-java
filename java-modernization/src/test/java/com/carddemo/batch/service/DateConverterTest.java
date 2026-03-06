package com.carddemo.batch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link DateConverter}, which replaces the COBOL COBDATFT
 * assembler subroutine referenced via the CODATECN copybook.
 */
class DateConverterTest {

    @Test
    void dashedToCompact_matchesCobolBehavior() {
        // CBACT01C uses type '2' input (YYYY-MM-DD) and type '2' output (YYYYMMDD)
        String result = DateConverter.convert("2025-05-20", DateConverter.TYPE_DASHED, DateConverter.TYPE_COMPACT);
        assertEquals("20250520", result);
    }

    @ParameterizedTest(name = "YYYY-MM-DD -> YYYYMMDD: {0} -> {1}")
    @CsvSource({
            "2014-11-20, 20141120",
            "2025-05-20, 20250520",
            "2013-06-19, 20130619",
            "2024-08-11, 20240811",
            "2023-12-16, 20231216",
            "2009-04-20, 20090420",
            "2019-04-06, 20190406"
    })
    void dashedToCompact_variousDates(String input, String expected) {
        assertEquals(expected,
                DateConverter.convert(input, DateConverter.TYPE_DASHED, DateConverter.TYPE_COMPACT));
    }

    @ParameterizedTest(name = "YYYYMMDD -> YYYY-MM-DD: {0} -> {1}")
    @CsvSource({
            "20141120, 2014-11-20",
            "20250520, 2025-05-20",
            "20130619, 2013-06-19"
    })
    void compactToDashed(String input, String expected) {
        assertEquals(expected,
                DateConverter.convert(input, DateConverter.TYPE_COMPACT, DateConverter.TYPE_DASHED));
    }

    @Test
    void dashedToDashed_identity() {
        assertEquals("2025-05-20",
                DateConverter.convert("2025-05-20", DateConverter.TYPE_DASHED, DateConverter.TYPE_DASHED));
    }

    @Test
    void compactToCompact_identity() {
        assertEquals("20250520",
                DateConverter.convert("20250520", DateConverter.TYPE_COMPACT, DateConverter.TYPE_COMPACT));
    }

    @Test
    void invalidInputType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "9", DateConverter.TYPE_COMPACT));
    }

    @Test
    void invalidOutputType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", DateConverter.TYPE_DASHED, "9"));
    }

    @Test
    void nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert(null, DateConverter.TYPE_DASHED, DateConverter.TYPE_COMPACT));
    }

    @Test
    void shortDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025", DateConverter.TYPE_DASHED, DateConverter.TYPE_COMPACT));
    }
}
