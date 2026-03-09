package com.carddemo.batch.util;

/**
 * Replaces the external COBOL subroutine {@code COBDATFT} that is called via
 * {@code CALL 'COBDATFT' USING CODATECN-REC}.
 *
 * <p>The COBOL program sets:
 * <ul>
 *   <li>{@code CODATECN-TYPE = '2'} — input is YYYY-MM-DD</li>
 *   <li>{@code CODATECN-OUTTYPE = '2'} — output is YYYYMMDD</li>
 * </ul>
 *
 * <p>This class implements that specific conversion and the reverse.
 */
public final class DateFormatter {

    private DateFormatter() { }

    /**
     * Converts a date from YYYY-MM-DD format to YYYYMMDD format.
     * This matches the COBDATFT call with type=2 and outtype=2.
     *
     * @param yyyyMmDd date in YYYY-MM-DD format (e.g. "2025-05-20")
     * @return date in YYYYMMDD format (e.g. "20250520"), or the input
     *         unchanged if it is null, empty, or not in the expected format
     */
    public static String convertYyyyMmDdToCompact(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.length() < 10) {
            return yyyyMmDd;
        }
        // Remove dashes: "YYYY-MM-DD" -> "YYYYMMDD"
        return yyyyMmDd.substring(0, 4)
                + yyyyMmDd.substring(5, 7)
                + yyyyMmDd.substring(8, 10);
    }

    /**
     * Converts a date from YYYYMMDD format to YYYY-MM-DD format.
     *
     * @param compact date in YYYYMMDD format
     * @return date in YYYY-MM-DD format
     */
    public static String convertCompactToYyyyMmDd(String compact) {
        if (compact == null || compact.length() < 8) {
            return compact;
        }
        return compact.substring(0, 4) + "-"
                + compact.substring(4, 6) + "-"
                + compact.substring(6, 8);
    }
}
