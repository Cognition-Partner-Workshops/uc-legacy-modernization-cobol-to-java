package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateConverter}, which replaces the external COBDATFT
 * assembler routine referenced in CBACT01C.
 */
class DateConverterTest {

    @ParameterizedTest(name = "YYYY-MM-DD ''{0}'' → YYYYMMDD ''{1}''")
    @CsvSource({
            "2025-05-20, 20250520",
            "2024-08-11, 20240811",
            "2024-01-10, 20240110",
            "2013-06-19, 20130619",
            "2014-11-20, 20141120",
    })
    void convertDashToCompact(String input, String expected) {
        // Type 2 input (YYYY-MM-DD) → Type 2 output (YYYYMMDD)
        // This is exactly what CBACT01C does: MOVE '2' TO CODATECN-TYPE / OUTTYPE
        assertEquals(expected, DateConverter.convertDate(input, "2", "2"));
    }

    @ParameterizedTest(name = "YYYYMMDD ''{0}'' → YYYY-MM-DD ''{1}''")
    @CsvSource({
            "20250520, 2025-05-20",
            "20240811, 2024-08-11",
    })
    void convertCompactToDash(String input, String expected) {
        // Type 1 input (YYYYMMDD) → Type 1 output (YYYY-MM-DD)
        assertEquals(expected, DateConverter.convertDate(input, "1", "1"));
    }

    @Test
    void convertCompactToCompact() {
        // Type 1 → Type 2 (YYYYMMDD → YYYYMMDD — effectively a no-op on format)
        assertEquals("20250520", DateConverter.convertDate("20250520", "1", "2"));
    }

    @Test
    void convertDashToDash() {
        // Type 2 → Type 1 (YYYY-MM-DD → YYYY-MM-DD — effectively a no-op on format)
        assertEquals("2025-05-20", DateConverter.convertDate("2025-05-20", "2", "1"));
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertEquals("", DateConverter.convertDate("", "2", "2"));
        assertEquals("", DateConverter.convertDate(null, "2", "2"));
        assertEquals("", DateConverter.convertDate("   ", "2", "2"));
    }
}
