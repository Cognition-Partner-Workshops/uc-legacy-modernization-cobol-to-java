package com.carddemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void getCurrentDateFormatted_returnsCorrectFormat() {
        String date = DateTimeUtil.getCurrentDateFormatted();
        assertNotNull(date);
        assertTrue(date.matches("\\d{2}/\\d{2}/\\d{2}"), "Date should match MM/dd/yy format: " + date);
    }

    @Test
    void getCurrentTimeFormatted_returnsCorrectFormat() {
        String time = DateTimeUtil.getCurrentTimeFormatted();
        assertNotNull(time);
        assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}"), "Time should match HH:mm:ss format: " + time);
    }

    @Test
    void getCurrentDb2Timestamp_returnsCorrectFormat() {
        String ts = DateTimeUtil.getCurrentDb2Timestamp();
        assertNotNull(ts);
        assertTrue(ts.contains("-") && ts.contains("."), "Should contain dash and dot: " + ts);
    }

    @Test
    void getCurrentIsoDate_returnsCorrectFormat() {
        String date = DateTimeUtil.getCurrentIsoDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"), "Date should match yyyy-MM-dd format: " + date);
    }

    @Test
    void generateTransactionTimestamp_returnsNonNull() {
        String ts = DateTimeUtil.generateTransactionTimestamp();
        assertNotNull(ts);
        assertFalse(ts.isBlank());
    }

    @Test
    void isDateExpired_pastDate() {
        assertTrue(DateTimeUtil.isDateExpired("2020-01-01"));
    }

    @Test
    void isDateExpired_futureDate() {
        assertFalse(DateTimeUtil.isDateExpired("2099-12-31"));
    }

    @Test
    void isDateExpired_nullDate() {
        assertFalse(DateTimeUtil.isDateExpired(null));
    }

    @Test
    void isDateExpired_emptyDate() {
        assertFalse(DateTimeUtil.isDateExpired(""));
    }
}
