package com.carddemo.batch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Replaces the COBDATFT assembler subroutine used by CBACT01C.
 *
 * The COBOL program calls COBDATFT with:
 * <ul>
 *   <li>input type '2' (YYYY-MM-DD)</li>
 *   <li>output type '2' (YYYYMMDD)</li>
 * </ul>
 */
public final class DateConverter {

    private static final DateTimeFormatter DASH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter COMPACT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateConverter() {
    }

    /**
     * Converts a date from YYYY-MM-DD to YYYYMMDD format.
     * Pads the result to 10 characters (matching the COBOL PIC X(10) output field).
     */
    public static String convertDashToCompact(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) {
            return " ".repeat(10);
        }
        LocalDate date = LocalDate.parse(yyyyMmDd.trim(), DASH_FORMAT);
        String compact = date.format(COMPACT_FORMAT);
        return padRight(compact, 10);
    }

    /**
     * Extracts the 4-digit year from a YYYY-MM-DD string.
     */
    public static String extractYear(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.length() < 4) {
            return "    ";
        }
        return yyyyMmDd.substring(0, 4);
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }
}
