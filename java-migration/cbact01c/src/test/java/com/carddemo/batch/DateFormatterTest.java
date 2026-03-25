package com.carddemo.batch;

import com.carddemo.batch.util.DateFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateFormatter} — verifies date conversions match
 * the COBDATFT assembler program behavior.
 */
class DateFormatterTest {

    @ParameterizedTest(name = "toCompact(\"{0}\") = \"{1}\"")
    @CsvSource({
            "2025-05-20, 20250520",
            "2014-11-20, 20141120",
            "2024-08-11, 20240811",
            "2023-01-27, 20230127",
            "2019-04-06, 20190406",
            "2010-12-31, 20101231",
    })
    @DisplayName("Type 2→2: YYYY-MM-DD → YYYYMMDD (used by CBACT01C)")
    void testToCompact(String input, String expected) {
        assertEquals(expected, DateFormatter.toCompact(input));
    }

    @ParameterizedTest(name = "toHyphenated(\"{0}\") = \"{1}\"")
    @CsvSource({
            "20250520, 2025-05-20",
            "20141120, 2014-11-20",
            "20240811, 2024-08-11",
    })
    @DisplayName("Type 1→1: YYYYMMDD → YYYY-MM-DD")
    void testToHyphenated(String input, String expected) {
        assertEquals(expected, DateFormatter.toHyphenated(input));
    }

    @Test
    @DisplayName("Round-trip: toCompact(toHyphenated(x)) == x")
    void testRoundTrip() {
        String compact = "20250520";
        assertEquals(compact, DateFormatter.toCompact(DateFormatter.toHyphenated(compact)));

        String hyphenated = "2025-05-20";
        assertEquals(hyphenated, DateFormatter.toHyphenated(DateFormatter.toCompact(hyphenated)));
    }

    @Test
    @DisplayName("Invalid input throws IllegalArgumentException")
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> DateFormatter.toCompact(null));
        assertThrows(IllegalArgumentException.class, () -> DateFormatter.toCompact("2025"));
        assertThrows(IllegalArgumentException.class, () -> DateFormatter.toHyphenated(null));
        assertThrows(IllegalArgumentException.class, () -> DateFormatter.toHyphenated("2025"));
    }
}
