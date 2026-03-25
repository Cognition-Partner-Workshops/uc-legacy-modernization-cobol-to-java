package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.util.DateFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link DateFormatter} — the Java replacement for the COBDATFT
 * assembler routine.
 */
class DateFormatterTest {

    // ---------------------------------------------------------------
    // Type 2 → Type 2:  YYYY-MM-DD  →  YYYYMMDD  (used by CBACT01C)
    // ---------------------------------------------------------------

    @Test
    void stripDashes_standardDate() {
        assertEquals("20250520", DateFormatter.stripDashes("2025-05-20"));
    }

    @Test
    void stripDashes_januaryFirstDate() {
        assertEquals("20240101", DateFormatter.stripDashes("2024-01-01"));
    }

    @Test
    void stripDashes_decemberLastDate() {
        assertEquals("20231231", DateFormatter.stripDashes("2023-12-31"));
    }

    // ---------------------------------------------------------------
    // Type 1 → Type 1:  YYYYMMDD  →  YYYY-MM-DD
    // ---------------------------------------------------------------

    @Test
    void format_compactToDashed() {
        assertEquals("2025-05-20",
                DateFormatter.format('1', '1', "20250520"));
    }

    @Test
    void format_compactToDashed_jan() {
        assertEquals("2024-01-10",
                DateFormatter.format('1', '1', "20240110"));
    }

    // ---------------------------------------------------------------
    // Invalid combinations (mirrors assembler GOTOERR)
    // ---------------------------------------------------------------

    @Test
    void format_type1InputType2Output_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.format('1', '2', "20250520"));
    }

    @Test
    void format_type2InputType1Output_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.format('2', '1', "2025-05-20"));
    }

    @Test
    void format_unknownType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.format('3', '1', "20250520"));
    }

    @Test
    void format_compactWithDash_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.format('1', '1', "2025-0520"));
    }
}
