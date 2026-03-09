package com.carddemo.batch.util;

/**
 * Replaces the COBOL CALL 'COBDATFT' USING CODATECN-REC.
 *
 * The COBOL program passes the reissue date in YYYY-MM-DD format (type "2")
 * and requests output in YYYYMMDD format (type "2" output).
 *
 * <p>CODATECN copybook date conversion types:
 * <ul>
 *   <li>Input  type "1" = YYYYMMDD,  type "2" = YYYY-MM-DD</li>
 *   <li>Output type "1" = YYYY-MM-DD, type "2" = YYYYMMDD</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {
        // utility class
    }

    /**
     * Converts a date string based on the CODATECN input/output type pair.
     *
     * @param inputDate  the date string to convert
     * @param inputType  "1" for YYYYMMDD input, "2" for YYYY-MM-DD input
     * @param outputType "1" for YYYY-MM-DD output, "2" for YYYYMMDD output
     * @return the converted date string, padded to 20 characters (matching PIC X(20))
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return padRight("", 20);
        }

        String yyyy;
        String mm;
        String dd;

        if ("1".equals(inputType)) {
            // Input is YYYYMMDD
            yyyy = safeSubstring(inputDate, 0, 4);
            mm = safeSubstring(inputDate, 4, 6);
            dd = safeSubstring(inputDate, 6, 8);
        } else {
            // Input is YYYY-MM-DD
            yyyy = safeSubstring(inputDate, 0, 4);
            mm = safeSubstring(inputDate, 5, 7);
            dd = safeSubstring(inputDate, 8, 10);
        }

        String result;
        if ("1".equals(outputType)) {
            // Output YYYY-MM-DD
            result = yyyy + "-" + mm + "-" + dd;
        } else {
            // Output YYYYMMDD
            result = yyyy + mm + dd;
        }

        return padRight(result, 20);
    }

    private static String safeSubstring(String s, int start, int end) {
        if (s.length() < end) {
            return padRight(s.length() <= start ? "" : s.substring(start), end - start);
        }
        return s.substring(start, end);
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }
}
