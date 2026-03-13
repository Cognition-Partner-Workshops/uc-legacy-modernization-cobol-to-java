package com.carddemo.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date utility class for CardDemo application.
 * Handles conversion between COBOL date formats and Java date types.
 *
 * COBOL date formats:
 *   PIC X(10) — typically YYYY-MM-DD
 *   PIC X(26) — timestamp format YYYY-MM-DD-HH.MM.SS.NNNNNN
 */
public final class DateUtils {

    private static final DateTimeFormatter COBOL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter COBOL_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private DateUtils() {
        // Utility class — prevent instantiation
    }

    /**
     * Parse a COBOL-format date string (YYYY-MM-DD) to LocalDate.
     */
    public static LocalDate parseCobolDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), COBOL_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid COBOL date format: " + dateString, e);
        }
    }

    /**
     * Parse a COBOL-format timestamp string to LocalDateTime.
     */
    public static LocalDateTime parseCobolTimestamp(String timestampString) {
        if (timestampString == null || timestampString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timestampString.trim(), COBOL_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid COBOL timestamp format: " + timestampString, e);
        }
    }

    /**
     * Format a LocalDate to COBOL date string (YYYY-MM-DD).
     */
    public static String formatCobolDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(COBOL_DATE_FORMAT);
    }

    /**
     * Format a LocalDateTime to COBOL timestamp string.
     */
    public static String formatCobolTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(COBOL_TIMESTAMP_FORMAT);
    }
}
