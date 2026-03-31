package com.carddemo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DB2_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS'0000'");
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateTimeUtil() {
    }

    public static String getCurrentDateFormatted() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentTimeFormatted() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    public static String getCurrentDb2Timestamp() {
        return LocalDateTime.now().format(DB2_TIMESTAMP_FORMAT);
    }

    public static String getCurrentIsoDate() {
        return LocalDate.now().format(ISO_DATE_FORMAT);
    }

    public static String generateTransactionTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
    }

    public static boolean isDateExpired(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), ISO_DATE_FORMAT);
            return date.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}
