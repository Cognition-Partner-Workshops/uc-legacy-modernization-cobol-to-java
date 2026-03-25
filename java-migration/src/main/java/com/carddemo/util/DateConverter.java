package com.carddemo.util;

/**
 * Replaces the COBOL assembler subroutine COBDATFT called via
 * {@code CALL 'COBDATFT' USING CODATECN-REC}.
 * <p>
 * The COBOL program sets:
 * <ul>
 *   <li>CODATECN-TYPE   = '2' (input is YYYY-MM-DD)</li>
 *   <li>CODATECN-OUTTYPE = '2' (output is YYYYMMDD)</li>
 * </ul>
 * So the conversion is simply: strip the hyphens from a date string.
 */
public final class DateConverter {

    private DateConverter() { }

    /**
     * Convert a date string based on input/output type codes.
     *
     * @param inputDate the date string to convert
     * @param inType    "1" = YYYYMMDD, "2" = YYYY-MM-DD
     * @param outType   "1" = YYYY-MM-DD, "2" = YYYYMMDD
     * @return the converted date string
     */
    public static String convert(String inputDate, String inType, String outType) {
        if (inputDate == null || inputDate.isBlank()) {
            return "";
        }

        // Normalize to components
        String yyyy, mm, dd;
        if ("2".equals(inType)) {
            // Input is YYYY-MM-DD
            String[] parts = inputDate.split("-");
            if (parts.length < 3) {
                return inputDate; // cannot parse — return as-is
            }
            yyyy = parts[0];
            mm = parts[1];
            dd = parts[2].substring(0, Math.min(2, parts[2].length()));
        } else {
            // Input is YYYYMMDD
            if (inputDate.length() < 8) {
                return inputDate;
            }
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        }

        // Format output
        if ("2".equals(outType)) {
            return yyyy + mm + dd;             // YYYYMMDD
        } else {
            return yyyy + "-" + mm + "-" + dd; // YYYY-MM-DD
        }
    }

    /**
     * Convenience method matching the exact COBOL usage in CBACT01C:
     * input type "2" (YYYY-MM-DD) → output type "2" (YYYYMMDD).
     */
    public static String toCompactDate(String yyyyMmDd) {
        return convert(yyyyMmDd, "2", "2");
    }
}
