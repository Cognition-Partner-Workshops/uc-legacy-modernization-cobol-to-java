package com.carddemo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date utility methods - consolidates date validation and formatting logic
 * duplicated across COTRN02C, CORPT00C, COACTUPC, and other programs.
 * Replaces CSUTLDTC/CSUTLDWY COBOL date utility copybooks.
 */
public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtils() {}

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd, got: " + dateStr);
        }
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMAT);
    }

    public static boolean isValidDate(String dateStr) {
        try {
            parseDate(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
