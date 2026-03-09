package com.carddemo.batch;

import com.carddemo.batch.util.DateConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateConverter — the Java replacement for COBOL CALL 'COBDATFT'.
 */
class DateConverterTest {

    @Test
    void convertYyyyMmDdToYyyymmdd() {
        // Input type "2" (YYYY-MM-DD), output type "2" (YYYYMMDD)
        // This is the exact conversion used in CBACT01C paragraph 1300
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals("20250520", result.trim());
    }

    @Test
    void convertYyyymmddToYyyyMmDd() {
        // Input type "1" (YYYYMMDD), output type "1" (YYYY-MM-DD)
        String result = DateConverter.convert("20250520", "1", "1");
        assertEquals("2025-05-20", result.trim());
    }

    @Test
    void convertYyyyMmDdPassthrough() {
        // Input type "2" (YYYY-MM-DD), output type "1" (YYYY-MM-DD) — no change
        String result = DateConverter.convert("2024-08-11", "2", "1");
        assertEquals("2024-08-11", result.trim());
    }

    @Test
    void convertYyyymmddToCompact() {
        // Input type "1" (YYYYMMDD), output type "2" (YYYYMMDD) — no change
        String result = DateConverter.convert("20240811", "1", "2");
        assertEquals("20240811", result.trim());
    }

    @Test
    void convertNullReturns20Spaces() {
        String result = DateConverter.convert(null, "2", "2");
        assertEquals(20, result.length());
        assertTrue(result.isBlank());
    }

    @Test
    void convertBlankReturns20Spaces() {
        String result = DateConverter.convert("          ", "2", "2");
        assertEquals(20, result.length());
        assertTrue(result.isBlank());
    }

    @Test
    void resultIsPaddedTo20Chars() {
        // CODATECN-0UT-DATE is PIC X(20), so output is always 20 chars
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals(20, result.length());
    }

    @Test
    void convertAllSampleReissueDates() {
        // Verify date conversion for all three sample records' reissue dates
        // (input type "2", output type "2": YYYY-MM-DD -> YYYYMMDD)
        assertEquals("20250520", DateConverter.convert("2025-05-20", "2", "2").trim());
        assertEquals("20240811", DateConverter.convert("2024-08-11", "2", "2").trim());
        assertEquals("20240110", DateConverter.convert("2024-01-10", "2", "2").trim());
    }
}
