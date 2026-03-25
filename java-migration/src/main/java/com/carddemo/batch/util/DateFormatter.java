package com.carddemo.batch.util;

/**
 * Replaces the COBDATFT assembler program used by the COBOL batch job.
 * <p>
 * The original assembler converts dates between two layouts:
 * <ul>
 *   <li>Type 1 (YYYYMMDD) &harr; Type 2 (YYYY-MM-DD)</li>
 * </ul>
 * CBACT01C calls COBDATFT with input type = '2' (YYYY-MM-DD) and
 * output type = '2' (YYYYMMDD), so the conversion strips hyphens.
 */
public final class DateFormatter {

    private DateFormatter() {
    }

    /**
     * Converts a date string from one COBOL format to another.
     *
     * @param inputDate  the date string to convert
     * @param inputType  "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outputType "1" for YYYY-MM-DD, "2" for YYYYMMDD
     * @return the reformatted date string, or the original if conversion is not applicable
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return inputDate;
        }

        String yyyy;
        String mm;
        String dd;

        if ("1".equals(inputType)) {
            // Input is YYYYMMDD
            if (inputDate.length() < 8) {
                return inputDate;
            }
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        } else if ("2".equals(inputType)) {
            // Input is YYYY-MM-DD
            if (inputDate.length() < 10) {
                return inputDate;
            }
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(5, 7);
            dd = inputDate.substring(8, 10);
        } else {
            return inputDate;
        }

        if ("1".equals(outputType)) {
            // Output YYYY-MM-DD
            return yyyy + "-" + mm + "-" + dd;
        } else if ("2".equals(outputType)) {
            // Output YYYYMMDD
            return yyyy + mm + dd;
        }

        return inputDate;
    }
}
