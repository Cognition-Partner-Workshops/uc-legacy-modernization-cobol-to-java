package com.carddemo.batch.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for generating DB2-format timestamps.
 * Equivalent to the COBOL paragraph Z-GET-DB2-FORMAT-TIMESTAMP
 * which formats the current date/time as: YYYY-MM-DD-HH.MM.SS.HH0000
 */
public final class TimestampUtil {

    private static final DateTimeFormatter DB2_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSSSS");

    private TimestampUtil() {}

    public static String getDb2FormatTimestamp() {
        return LocalDateTime.now().format(DB2_FORMAT);
    }

    public static String getDb2FormatTimestamp(LocalDateTime dateTime) {
        return dateTime.format(DB2_FORMAT);
    }
}
