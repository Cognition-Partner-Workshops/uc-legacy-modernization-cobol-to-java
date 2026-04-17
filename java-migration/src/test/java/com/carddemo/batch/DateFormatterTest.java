package com.carddemo.batch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatterTest {

    @Test
    void formatDate_yyyyMmDd_to_yyyymmdd() {
        // Type '2' input (YYYY-MM-DD), type '2' output (YYYYMMDD)
        // This is the exact conversion used in CBACT01C
        String result = DateFormatter.formatDate("2025-05-20", '2', '2');
        assertEquals("20250520  ", result); // padded to 10 chars
    }

    @Test
    void formatDate_yyyymmdd_to_yyyyMmDd() {
        String result = DateFormatter.formatDate("20250520", '1', '1');
        assertEquals("2025-05-20", result);
    }

    @Test
    void formatDate_yyyymmdd_to_compact() {
        String result = DateFormatter.formatDate("20251231", '1', '2');
        assertEquals("20251231  ", result);
    }

    @Test
    void formatDate_compact_to_dashed() {
        String result = DateFormatter.formatDate("2014-11-20", '2', '1');
        assertEquals("2014-11-20", result);
    }

    @Test
    void formatDate_invalidInputType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.formatDate("2025-05-20", '3', '2'));
    }

    @Test
    void formatDate_invalidOutputType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.formatDate("2025-05-20", '2', '3'));
    }
}
