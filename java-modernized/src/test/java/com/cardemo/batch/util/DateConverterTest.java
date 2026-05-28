package com.cardemo.batch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateConverterTest {

    @Test
    void isoToCompactMatchesCobdatft() {
        assertEquals("20250520", DateConverter.isoToCompact("2025-05-20"));
        assertEquals("20240811", DateConverter.isoToCompact("2024-08-11"));
        assertEquals("20141120", DateConverter.isoToCompact("2014-11-20"));
    }

    @ParameterizedTest
    @CsvSource({
            "20250115,   1, 2, 20250115",
            "2025-01-15, 2, 2, 20250115",
            "20250115,   1, 1, 2025-01-15",
            "2025-01-15, 2, 1, 2025-01-15"
    })
    void convertAllTypeCombinations(String input, String inType, String outType, String expected) {
        assertEquals(expected, DateConverter.convert(input.trim(), inType, outType));
    }

    @Test
    void convertCompactToIso() {
        assertEquals("2025-01-15", DateConverter.convert("20250115", "1", "1"));
    }

    @Test
    void blankInputReturnsBlank() {
        assertEquals("", DateConverter.convert("", "2", "2"));
        assertEquals("   ", DateConverter.convert("   ", "2", "2"));
    }

    @Test
    void nullInputReturnsNull() {
        assertNull(DateConverter.convert(null, "2", "2"));
    }

    @Test
    void invalidDateReturnsOriginal() {
        assertEquals("not-a-date", DateConverter.convert("not-a-date", "2", "2"));
    }
}
