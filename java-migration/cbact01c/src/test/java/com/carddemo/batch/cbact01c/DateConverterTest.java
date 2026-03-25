package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.util.DateConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DateConverter utility, which replaces the COBDATFT assembler.
 * Verifies YYYY-MM-DD to YYYYMMDD conversion and year extraction.
 */
class DateConverterTest {

    @Test
    void convertsStandardDate() {
        // Mirrors COBDATFT with type=2, outtype=2
        String result = DateConverter.convertYyyyMmDdToCompact("2025-05-20");
        assertEquals("20250520  ", result);  // padded to 10 chars
    }

    @Test
    void convertsAnotherDate() {
        String result = DateConverter.convertYyyyMmDdToCompact("2024-08-11");
        assertEquals("20240811  ", result);
    }

    @Test
    void convertsLeapYearDate() {
        String result = DateConverter.convertYyyyMmDdToCompact("2024-02-29");
        assertEquals("20240229  ", result);
    }

    @Test
    void handlesNullDate() {
        String result = DateConverter.convertYyyyMmDdToCompact(null);
        assertEquals("          ", result);  // 10 spaces
    }

    @Test
    void handlesShortDate() {
        String result = DateConverter.convertYyyyMmDdToCompact("2025");
        assertEquals("          ", result);  // too short, returns spaces
    }

    @Test
    void extractsYear() {
        assertEquals("2025", DateConverter.extractYear("2025-05-20"));
        assertEquals("2014", DateConverter.extractYear("2014-11-20"));
    }

    @Test
    void extractsYearFromNull() {
        assertEquals("    ", DateConverter.extractYear(null));
    }

    @Test
    void extractsYearFromShortString() {
        assertEquals("    ", DateConverter.extractYear("20"));
    }
}
