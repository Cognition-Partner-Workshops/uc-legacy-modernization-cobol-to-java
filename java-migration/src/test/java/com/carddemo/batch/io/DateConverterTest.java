package com.carddemo.batch.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DateConverter (COBDATFT replacement).
 */
class DateConverterTest {

    @Test
    void convertYyyyMmDdToYyyymmdd() {
        // This is the exact conversion CBACT01C performs:
        //   input type "2" = YYYY-MM-DD, output type "2" = YYYYMMDD
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals("20250520", result);
    }

    @Test
    void convertYyyymmddToYyyyMmDd() {
        // input type "1" = YYYYMMDD, output type "1" = YYYY-MM-DD
        String result = DateConverter.convert("20250520", "1", "1");
        assertEquals("2025-05-20", result);
    }

    @Test
    void convertYyyyMmDdToYyyyMmDd() {
        // input type "2" = YYYY-MM-DD, output type "1" = YYYY-MM-DD
        String result = DateConverter.convert("2025-05-20", "2", "1");
        assertEquals("2025-05-20", result);
    }

    @Test
    void convertYyyymmddToYyyymmdd() {
        // input type "1" = YYYYMMDD, output type "2" = YYYYMMDD
        String result = DateConverter.convert("20250520", "1", "2");
        assertEquals("20250520", result);
    }

    @Test
    void variousDates() {
        // All use the same conversion as CBACT01C: input=2 (YYYY-MM-DD), output=2 (YYYYMMDD)
        assertEquals("20140101", DateConverter.convert("2014-01-01", "2", "2"));
        assertEquals("20231231", DateConverter.convert("2023-12-31", "2", "2"));
        assertEquals("20240811", DateConverter.convert("2024-08-11", "2", "2"));
    }

    @Test
    void blankInputReturnsEmpty() {
        assertEquals("", DateConverter.convert("", "2", "2"));
        assertEquals("", DateConverter.convert("   ", "2", "2"));
        assertEquals("", DateConverter.convert(null, "2", "2"));
    }

    @Test
    void invalidTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "3", "2"));
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("20250520", "1", "3"));
    }
}
