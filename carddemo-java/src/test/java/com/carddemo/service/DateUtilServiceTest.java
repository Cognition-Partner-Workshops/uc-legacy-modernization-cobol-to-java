package com.carddemo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DateUtilServiceTest {

    @InjectMocks
    private DateUtilService dateUtilService;

    @Test
    void formatDateYYYYMMDD_returnsFormattedDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        assertEquals("2024-06-15", dateUtilService.formatDateYYYYMMDD(date));
    }

    @Test
    void formatDateYYYYMMDD_nullDate_returnsEmpty() {
        assertEquals("", dateUtilService.formatDateYYYYMMDD(null));
    }

    @Test
    void formatDateMMDDYY_returnsFormattedDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        assertEquals("06/15/24", dateUtilService.formatDateMMDDYY(date));
    }

    @Test
    void parseDateYYYYMMDD_validDate_returnsLocalDate() {
        LocalDate result = dateUtilService.parseDateYYYYMMDD("2024-06-15");
        assertEquals(LocalDate.of(2024, 6, 15), result);
    }

    @Test
    void parseDateYYYYMMDD_invalidDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> dateUtilService.parseDateYYYYMMDD("not-a-date"));
    }

    @Test
    void parseDateYYYYMMDD_nullDate_returnsNull() {
        assertNull(dateUtilService.parseDateYYYYMMDD(null));
    }

    @Test
    void parseDateYYYYMMDD_emptyDate_returnsNull() {
        assertNull(dateUtilService.parseDateYYYYMMDD(""));
    }

    @Test
    void generateTimestamp_returnsNonEmpty() {
        String timestamp = dateUtilService.generateTimestamp();
        assertNotNull(timestamp);
        assertFalse(timestamp.isEmpty());
        assertTrue(timestamp.contains("-"));
        assertTrue(timestamp.contains(":"));
    }

    @Test
    void getCurrentDateFormatted_returnsValidFormat() {
        String result = dateUtilService.getCurrentDateFormatted();
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}/\\d{2}/\\d{2}"));
    }

    @Test
    void getCurrentTimeFormatted_returnsValidFormat() {
        String result = dateUtilService.getCurrentTimeFormatted();
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void isValidDate_validDate_returnsTrue() {
        assertTrue(dateUtilService.isValidDate("2024-06-15"));
    }

    @Test
    void isValidDate_invalidDate_returnsFalse() {
        assertFalse(dateUtilService.isValidDate("not-a-date"));
    }

    @Test
    void formatTime_returnsFormattedTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
        assertEquals("14:30:45", dateUtilService.formatTime(dt));
    }

    @Test
    void formatTime_null_returnsEmpty() {
        assertEquals("", dateUtilService.formatTime(null));
    }
}
