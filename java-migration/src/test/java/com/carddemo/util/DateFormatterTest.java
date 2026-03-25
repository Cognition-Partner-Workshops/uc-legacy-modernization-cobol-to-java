package com.carddemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the COBDATFT assembler routine replacement.
 */
class DateFormatterTest {

    @Test
    void toCompact_standardDate() {
        assertEquals("20250520", DateFormatter.toCompact("2025-05-20"));
    }

    @Test
    void toCompact_anotherDate() {
        assertEquals("20140101", DateFormatter.toCompact("2014-01-01"));
    }

    @Test
    void toCompact_nullInput() {
        assertNull(DateFormatter.toCompact(null));
    }

    @Test
    void toCompact_shortInput() {
        assertEquals("2025", DateFormatter.toCompact("2025"));
    }

    @Test
    void toReadable_standardDate() {
        assertEquals("2025-05-20", DateFormatter.toReadable("20250520"));
    }

    @Test
    void toReadable_nullInput() {
        assertNull(DateFormatter.toReadable(null));
    }

    @Test
    void toReadable_shortInput() {
        assertEquals("2025", DateFormatter.toReadable("2025"));
    }

    @Test
    void extractYear_standardDate() {
        assertEquals("2025", DateFormatter.extractYear("2025-05-20"));
    }

    @Test
    void extractYear_nullInput() {
        assertEquals("", DateFormatter.extractYear(null));
    }

    @Test
    void extractYear_shortInput() {
        assertEquals("", DateFormatter.extractYear("20"));
    }

    @Test
    void roundTrip_compactThenReadable() {
        String original = "2023-06-30";
        String compact = DateFormatter.toCompact(original);
        String readable = DateFormatter.toReadable(compact);
        assertEquals(original, readable);
    }
}
