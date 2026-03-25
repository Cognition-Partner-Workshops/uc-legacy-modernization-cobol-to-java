package com.cardemo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Date Utility - converted from COBOL program CSUTLDTC.cbl
 * 
 * Original: Date/Time Utility subroutine
 * Provides date validation, formatting, and calculation functions
 * that were used across the CardDemo CICS and batch programs.
 * 
 * COBOL CSUTLDTC provided:
 * - Date validation (EDIT-DATE-CCYYMMDD)
 * - Date format conversion (CCYYMMDD, YYYYMMDD, MM/DD/YYYY)
 * - Date arithmetic (days between dates)
 * - Current date/time retrieval (FUNCTION CURRENT-DATE)
 */
public final class DateUtil {

    private DateUtil() {}

    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    public static final String DATE_FORMAT_CCYYMMDD = "yyyyMMdd";
    public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH.mm.ss.SSSSSS";

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_ISO);
    private static final DateTimeFormatter CCYYMMDD_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_CCYYMMDD);
    private static final DateTimeFormatter MMDDYYYY_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_MMDDYYYY);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

    /**
     * Validate a date string in CCYYMMDD format.
     * Equivalent to EDIT-DATE-CCYYMMDD paragraph in CSUTLDTC.
     *
     * @param dateStr date string in yyyyMMdd format
     * @return true if the date is valid
     */
    public static boolean isValidDateCCYYMMDD(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, CCYYMMDD_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate a date string in ISO format (yyyy-MM-dd).
     */
    public static boolean isValidDateISO(String dateStr) {
        if (dateStr == null || dateStr.length() != 10) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, ISO_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Convert CCYYMMDD to ISO format (yyyy-MM-dd).
     */
    public static String ccyymmddToISO(String ccyymmdd) {
        LocalDate date = LocalDate.parse(ccyymmdd, CCYYMMDD_FMT);
        return date.format(ISO_FMT);
    }

    /**
     * Convert ISO format to CCYYMMDD.
     */
    public static String isoToCCYYMMDD(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate, ISO_FMT);
        return date.format(CCYYMMDD_FMT);
    }

    /**
     * Convert CCYYMMDD to MM/DD/YYYY display format.
     */
    public static String ccyymmddToDisplay(String ccyymmdd) {
        LocalDate date = LocalDate.parse(ccyymmdd, CCYYMMDD_FMT);
        return date.format(MMDDYYYY_FMT);
    }

    /**
     * Calculate days between two dates.
     * Equivalent to date arithmetic in CSUTLDTC.
     *
     * @param startDate start date in ISO format
     * @param endDate   end date in ISO format
     * @return number of days between dates
     */
    public static long daysBetween(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, ISO_FMT);
        LocalDate end = LocalDate.parse(endDate, ISO_FMT);
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Get current date in ISO format.
     * Equivalent to FUNCTION CURRENT-DATE in COBOL.
     */
    public static String getCurrentDateISO() {
        return LocalDate.now().format(ISO_FMT);
    }

    /**
     * Get current date in CCYYMMDD format.
     */
    public static String getCurrentDateCCYYMMDD() {
        return LocalDate.now().format(CCYYMMDD_FMT);
    }

    /**
     * Get current timestamp in CardDemo format.
     * Equivalent to FUNCTION CURRENT-DATE formatted as yyyy-MM-dd-HH.mm.ss.SSSSSS
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FMT);
    }
}
