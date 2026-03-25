package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatterTest {

    @Test
    void convertYyyyMmDdToYyyymmdd() {
        // CBACT01C uses input type '2' (YYYY-MM-DD) → output type '2' (YYYYMMDD)
        assertEquals("20250520", DateFormatter.convert("2025-05-20", "2", "2"));
    }

    @Test
    void convertYyyymmddToYyyyMmDd() {
        assertEquals("2025-05-20", DateFormatter.convert("20250520", "1", "1"));
    }

    @Test
    void convertYyyyMmDdToYyyyMmDd() {
        assertEquals("2025-05-20", DateFormatter.convert("2025-05-20", "2", "1"));
    }

    @Test
    void convertYyyymmddToYyyymmdd() {
        assertEquals("20250520", DateFormatter.convert("20250520", "1", "2"));
    }

    @Test
    void convertNullReturnsNull() {
        assertNull(DateFormatter.convert(null, "1", "2"));
    }

    @Test
    void convertBlankReturnsBlank() {
        assertEquals("   ", DateFormatter.convert("   ", "1", "2"));
    }

    @Test
    void convertAllSampleDates() {
        // Verify multiple dates from the sample data
        assertEquals("20140911", DateFormatter.convert("2014-09-11", "2", "2"));
        assertEquals("20130619", DateFormatter.convert("2013-06-19", "2", "2"));
        assertEquals("20241213", DateFormatter.convert("2024-12-13", "2", "2"));
        assertEquals("20230327", DateFormatter.convert("2023-03-27", "2", "2"));
    }
}
