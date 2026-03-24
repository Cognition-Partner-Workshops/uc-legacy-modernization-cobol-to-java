package com.carddemo.batch.service;

/**
 * Replaces the COBOL CALL to 'COBDATFT' using CODATECN-REC.
 *
 * The COBOL program sets CODATECN-TYPE = '2' (YYYY-MM-DD input) and
 * CODATECN-OUTTYPE = '2' (YYYYMMDD output), so this formatter converts
 * date strings from {@code YYYY-MM-DD} to {@code YYYYMMDD}.
 */
public final class DateFormatter {

    private DateFormatter() {
        // utility class
    }

    /**
     * Converts a date from {@code YYYY-MM-DD} format to {@code YYYYMMDD} format.
     * Mirrors the COBDATFT assembler routine with input type 2 and output type 2.
     *
     * @param inputDate date in YYYY-MM-DD format (10 characters)
     * @return date in YYYYMMDD format (8 characters), right-padded to 20 chars
     *         to match the COBOL CODATECN-0UT-DATE PIC X(20) field
     */
    public static String formatDate(String inputDate) {
        if (inputDate == null || inputDate.length() < 10) {
            return padRight("", 20);
        }
        // Strip the dashes: YYYY-MM-DD -> YYYYMMDD
        String yyyy = inputDate.substring(0, 4);
        String mm = inputDate.substring(5, 7);
        String dd = inputDate.substring(8, 10);
        return padRight(yyyy + mm + dd, 20);
    }

    /**
     * Extracts just the YYYYMMDD portion (first 10 chars of formatted output,
     * but only 8 are meaningful). This is what gets written to the output record.
     */
    public static String formatDateCompact(String inputDate) {
        if (inputDate == null || inputDate.length() < 10) {
            return "";
        }
        String yyyy = inputDate.substring(0, 4);
        String mm = inputDate.substring(5, 7);
        String dd = inputDate.substring(8, 10);
        return yyyy + mm + dd;
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }
}
