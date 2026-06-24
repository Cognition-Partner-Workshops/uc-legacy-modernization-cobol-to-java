package com.carddemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @Test
    void convertYyyyMmDdToYyyymmdd() {
        String result = DateConverter.convert("2025-06-20", DateConverter.YYYY_MM_DD,
                DateConverter.YYYY_MM_DD);
        assertEquals("20250620", result);
    }

    @Test
    void convertYyyymmddToYyyyMmDd() {
        String result = DateConverter.convert("20250620", DateConverter.YYYYMMDD,
                DateConverter.YYYYMMDD);
        assertEquals("2025-06-20", result);
    }

    @Test
    void convertYyyyMmDdRoundTrip() {
        String original = "2023-12-31";
        String compact = DateConverter.convert(original, DateConverter.YYYY_MM_DD,
                DateConverter.YYYY_MM_DD);
        assertEquals("20231231", compact);

        String expanded = DateConverter.convert(compact, DateConverter.YYYYMMDD,
                DateConverter.YYYYMMDD);
        assertEquals(original, expanded);
    }

    @Test
    void invalidInputType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-06-20", 3, DateConverter.YYYY_MM_DD));
    }

    @Test
    void invalidOutputType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-06-20", DateConverter.YYYY_MM_DD, 3));
    }

    @Test
    void shortInput_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025", DateConverter.YYYY_MM_DD,
                        DateConverter.YYYY_MM_DD));
    }
}
