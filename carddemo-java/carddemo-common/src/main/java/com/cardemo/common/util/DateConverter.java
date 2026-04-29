package com.cardemo.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date conversion utility replacing COBOL assembler utility COBDATFT.asm.
 * Source: app/asm/COBDATFT.asm
 *
 * Handles conversion between mainframe date string formats and java.time.LocalDate.
 *
 * TODO: Add support for all mainframe date formats used in the COBOL codebase:
 *   - YYYY-MM-DD (standard ISO used in most copybooks)
 *   - YYYYMMDD (compact format used in some JCL)
 *   - MM/DD/YYYY (display format in BMS screens)
 *   - Julian date (YYYYDDD) if used in batch
 */
public final class DateConverter {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter COMPACT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private DateConverter() {
    }

    public static LocalDate fromIsoString(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), ISO_FORMAT);
    }

    public static LocalDate fromCompactString(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), COMPACT_FORMAT);
    }

    public static String toIsoString(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(ISO_FORMAT);
    }

    public static String toDisplayString(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_FORMAT);
    }

    public static LocalDate parseFlexible(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        String trimmed = dateStr.trim();
        try {
            return LocalDate.parse(trimmed, ISO_FORMAT);
        } catch (DateTimeParseException e) {
            // fall through
        }
        try {
            return LocalDate.parse(trimmed, COMPACT_FORMAT);
        } catch (DateTimeParseException e) {
            // fall through
        }
        try {
            return LocalDate.parse(trimmed, DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse date: " + dateStr, e);
        }
    }
}
