package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatUtilTest {

    @Test
    void convertToCompactDateRemovesSeparators() {
        assertEquals("20250115", DateFormatUtil.convertToCompactDate("2025-01-15"));
        assertEquals("20231231", DateFormatUtil.convertToCompactDate("2023/12/31"));
    }

    @Test
    void convertToCompactDateHandlesNullAndBlank() {
        assertEquals("", DateFormatUtil.convertToCompactDate(null));
        assertEquals("", DateFormatUtil.convertToCompactDate(""));
        assertEquals("", DateFormatUtil.convertToCompactDate("   "));
    }

    @Test
    void convertToSeparatedDateInsertsHyphens() {
        assertEquals("2025-01-15", DateFormatUtil.convertToSeparatedDate("20250115"));
    }

    @Test
    void convertToSeparatedDateHandlesNullAndShortInput() {
        assertEquals("", DateFormatUtil.convertToSeparatedDate(null));
        assertEquals("", DateFormatUtil.convertToSeparatedDate(""));
        assertEquals("", DateFormatUtil.convertToSeparatedDate("2025"));
    }

    @Test
    void getDb2FormatTimestampReturnsValidFormat() {
        String ts = DateFormatUtil.getDb2FormatTimestamp();
        assertNotNull(ts);
        // Format: YYYY-MM-DD-HH.MM.SS.HH0000
        assertTrue(ts.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}0000"),
                "Timestamp should match DB2 format: " + ts);
    }
}
