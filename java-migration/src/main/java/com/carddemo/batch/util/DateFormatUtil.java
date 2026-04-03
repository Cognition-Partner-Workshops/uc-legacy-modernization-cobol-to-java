package com.carddemo.batch.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date formatting utility - Java equivalent of the COBDATFT assembler program
 * and the DB2 timestamp formatting logic from COBOL.
 */
public final class DateFormatUtil {

    private static final DateTimeFormatter DB2_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS'0000'");

    private DateFormatUtil() {
    }

    /**
     * Convert date from YYYY-MM-DD format to YYYYMMDD format.
     * Equivalent to COBDATFT with type=2, outtype=2.
     */
    public static String convertToCompactDate(String dateWithSeparators) {
        if (dateWithSeparators == null || dateWithSeparators.isBlank()) {
            return "";
        }
        return dateWithSeparators.replace("-", "").replace("/", "").trim();
    }

    /**
     * Convert date from YYYYMMDD format to YYYY-MM-DD format.
     * Equivalent to COBDATFT with type=1, outtype=1.
     */
    public static String convertToSeparatedDate(String compactDate) {
        if (compactDate == null || compactDate.isBlank() || compactDate.trim().length() < 8) {
            return "";
        }
        String d = compactDate.trim();
        return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
    }

    /**
     * Get current timestamp in DB2 format: YYYY-MM-DD-HH.MM.SS.HH0000
     * Equivalent to Z-GET-DB2-FORMAT-TIMESTAMP in COBOL.
     */
    public static String getDb2FormatTimestamp() {
        return LocalDateTime.now().format(DB2_TIMESTAMP_FORMAT);
    }
}
