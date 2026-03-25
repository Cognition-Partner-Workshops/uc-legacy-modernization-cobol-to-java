package com.cardemo.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date/Time utility - migrated from COBOL copybook CSDAT01Y.cpy and CICS date functions.
 * Replaces COBOL FUNCTION CURRENT-DATE, CICS ASKTIME/FORMATTIME.
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter DATE_MM_DD_YY = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private DateTimeUtil() {
    }

    public static String getCurrentDateMmDdYy() {
        return LocalDateTime.now().format(DATE_MM_DD_YY);
    }

    public static String getCurrentTimeHhMmSs() {
        return LocalDateTime.now().format(TIME_HH_MM_SS);
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FMT);
    }

    public static String formatDateMmDdYy(LocalDateTime dateTime) {
        return dateTime.format(DATE_MM_DD_YY);
    }

    public static String formatTimeHhMmSs(LocalDateTime dateTime) {
        return dateTime.format(TIME_HH_MM_SS);
    }

    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime.format(TIMESTAMP_FMT);
    }
}
