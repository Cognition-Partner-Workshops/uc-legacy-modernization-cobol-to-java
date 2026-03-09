package com.carddemo.batch.model;

/**
 * Replaces the external COBDATFT assembler call referenced in CBACT01C.
 * <p>
 * The COBOL program uses the CODATECN copybook to pass date-conversion
 * parameters:
 * <ul>
 *   <li>Type 1 input  = YYYYMMDD</li>
 *   <li>Type 2 input  = YYYY-MM-DD</li>
 *   <li>Type 1 output = YYYY-MM-DD</li>
 *   <li>Type 2 output = YYYYMMDD</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {
        // utility class
    }

    /**
     * Convert a date string between the two formats used by CODATECN.
     *
     * @param inputDate   the date string to convert
     * @param inputType   "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outputType  "1" for YYYY-MM-DD, "2" for YYYYMMDD
     * @return the reformatted date string (up to 20 chars, left-padded with spaces
     *         to match the COBOL PIC X(20) if needed — but we return the trimmed value)
     */
    public static String convertDate(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return "";
        }

        String yyyy;
        String mm;
        String dd;

        if ("1".equals(inputType)) {
            // YYYYMMDD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        } else {
            // YYYY-MM-DD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(5, 7);
            dd = inputDate.substring(8, 10);
        }

        if ("1".equals(outputType)) {
            // YYYY-MM-DD
            return yyyy + "-" + mm + "-" + dd;
        } else {
            // YYYYMMDD
            return yyyy + mm + dd;
        }
    }
}
