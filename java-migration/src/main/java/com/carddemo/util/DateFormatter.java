package com.carddemo.util;

/**
 * Java replacement for the COBDATFT assembler routine.
 *
 * The original assembler program converts dates between two formats:
 *   Type 1 input (YYYYMMDD) + Type 1 output -> YYYY-MM-DD
 *   Type 2 input (YYYY-MM-DD) + Type 2 output -> YYYYMMDD
 *
 * CBACT01C uses Type 2 input / Type 2 output exclusively, so the
 * reissue date is converted from "YYYY-MM-DD" to "YYYYMMDD".
 *
 * This class also provides the reverse conversion for completeness.
 */
public final class DateFormatter {

    private DateFormatter() {
    }

    /**
     * Convert YYYY-MM-DD to YYYYMMDD (Type 2 -> Type 2 in COBDATFT).
     *
     * @param date the date string in YYYY-MM-DD format
     * @return the date in YYYYMMDD format, or the original string if
     *         it does not match the expected format
     */
    public static String toCompact(String date) {
        if (date == null || date.length() < 10) {
            return date;
        }
        // YYYY-MM-DD -> YYYYMMDD
        return date.substring(0, 4)
                + date.substring(5, 7)
                + date.substring(8, 10);
    }

    /**
     * Convert YYYYMMDD to YYYY-MM-DD (Type 1 -> Type 1 in COBDATFT).
     *
     * @param date the date string in YYYYMMDD format
     * @return the date in YYYY-MM-DD format
     */
    public static String toReadable(String date) {
        if (date == null || date.length() < 8) {
            return date;
        }
        return date.substring(0, 4)
                + "-" + date.substring(4, 6)
                + "-" + date.substring(6, 8);
    }

    /**
     * Extract the year portion from a YYYY-MM-DD date string.
     * Used to populate VB2-ACCT-REISSUE-YYYY.
     */
    public static String extractYear(String date) {
        if (date == null || date.length() < 4) {
            return "";
        }
        return date.substring(0, 4);
    }
}
