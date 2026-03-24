package com.carddemo.batch.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatterTest {

    @Test
    void formatDate_standardDate() {
        String result = DateFormatter.formatDate("2025-05-20");
        assertEquals(20, result.length());
        assertTrue(result.startsWith("20250520"));
    }

    @Test
    void formatDateCompact_standardDate() {
        assertEquals("20250520", DateFormatter.formatDateCompact("2025-05-20"));
    }

    @Test
    void formatDateCompact_anotherDate() {
        assertEquals("20240811", DateFormatter.formatDateCompact("2024-08-11"));
    }

    @Test
    void formatDateCompact_leapYearDate() {
        assertEquals("20240229", DateFormatter.formatDateCompact("2024-02-29"));
    }

    @Test
    void formatDateCompact_nullInput() {
        assertEquals("", DateFormatter.formatDateCompact(null));
    }

    @Test
    void formatDateCompact_shortInput() {
        assertEquals("", DateFormatter.formatDateCompact("2025"));
    }

    @Test
    void formatDate_nullInput() {
        String result = DateFormatter.formatDate(null);
        assertEquals(20, result.length());
        assertTrue(result.isBlank());
    }

    @Test
    void formatDateCompact_variousDates() {
        // Mirrors the reissue dates from the sample data
        assertEquals("20250520", DateFormatter.formatDateCompact("2025-05-20"));
        assertEquals("20240811", DateFormatter.formatDateCompact("2024-08-11"));
        assertEquals("20240110", DateFormatter.formatDateCompact("2024-01-10"));
        assertEquals("20231216", DateFormatter.formatDateCompact("2023-12-16"));
    }
}
