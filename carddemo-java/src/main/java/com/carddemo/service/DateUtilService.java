package com.carddemo.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date utility service - mirrors CSUTLDTC.cbl and CSDAT01Y.cpy
 * Provides date formatting/parsing utilities using java.time
 */
@Service
public class DateUtilService {

    private static final DateTimeFormatter DATE_FORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMAT_MMDDYY = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_FORMAT_HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public String formatDateYYYYMMDD(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT_YYYYMMDD);
    }

    public String formatDateMMDDYY(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT_MMDDYY);
    }

    public LocalDate parseDateYYYYMMDD(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT_YYYYMMDD);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

    public String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMAT_HHMMSS);
    }

    public String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    public String getCurrentDateFormatted() {
        return LocalDate.now().format(DATE_FORMAT_MMDDYY);
    }

    public String getCurrentTimeFormatted() {
        return LocalDateTime.now().format(TIME_FORMAT_HHMMSS);
    }

    public boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr.trim(), DATE_FORMAT_YYYYMMDD);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
