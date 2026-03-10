package com.carddemo.common.util;

/**
 * Replaces the COBOL external call to COBDATFT using the CODATECN copybook.
 * Converts between date formats:
 * <ul>
 *   <li>Type "1": YYYYMMDD (compact)</li>
 *   <li>Type "2": YYYY-MM-DD (ISO with dashes)</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {
        // utility class
    }

    /**
     * Convert YYYY-MM-DD to YYYYMMDD.
     *
     * @param isoDate date in YYYY-MM-DD format
     * @return date in YYYYMMDD format, or the original string if not in expected format
     */
    public static String toCompact(String isoDate) {
        if (isoDate == null || isoDate.length() != 10) {
            return isoDate;
        }
        // YYYY-MM-DD -> YYYYMMDD
        return isoDate.substring(0, 4) + isoDate.substring(5, 7) + isoDate.substring(8, 10);
    }

    /**
     * Convert YYYYMMDD to YYYY-MM-DD.
     *
     * @param compactDate date in YYYYMMDD format
     * @return date in YYYY-MM-DD format, or the original string if not in expected format
     */
    public static String toIso(String compactDate) {
        if (compactDate == null || compactDate.length() != 8) {
            return compactDate;
        }
        // YYYYMMDD -> YYYY-MM-DD
        return compactDate.substring(0, 4) + "-" + compactDate.substring(4, 6) + "-" + compactDate.substring(6, 8);
    }
}
