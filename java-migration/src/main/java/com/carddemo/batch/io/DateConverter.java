package com.carddemo.batch.io;

/**
 * Replacement for the COBDATFT assembler program referenced in CBACT01C.
 *
 * COBDATFT converts between two date formats controlled by input/output type codes
 * defined in the CODATECN copybook.
 *
 * Input type (CODATECN-TYPE):
 *   "1" = YYYYMMDD   (e.g., "20250520")
 *   "2" = YYYY-MM-DD (e.g., "2025-05-20")
 *
 * Output type (CODATECN-OUTTYPE) — NOTE: semantics are SWAPPED vs input:
 *   "1" = YYYY-MM-DD (YYYY-MM-DD-OP)
 *   "2" = YYYYMMDD   (YYYYMMDD-OP)
 *
 * The COBOL program calls COBDATFT with:
 *   - CODATECN-TYPE    = '2'  (input  is YYYY-MM-DD)
 *   - CODATECN-OUTTYPE = '2'  (output is YYYYMMDD)
 *
 * So the actual conversion performed is: YYYY-MM-DD → YYYYMMDD.
 */
public final class DateConverter {

    // Input type constants (CODATECN-TYPE)
    /** Input type "1": YYYYMMDD */
    public static final String INPUT_YYYYMMDD = "1";
    /** Input type "2": YYYY-MM-DD */
    public static final String INPUT_YYYY_MM_DD = "2";

    // Output type constants (CODATECN-OUTTYPE) — note: semantics swapped vs input
    /** Output type "1": YYYY-MM-DD */
    public static final String OUTPUT_YYYY_MM_DD = "1";
    /** Output type "2": YYYYMMDD */
    public static final String OUTPUT_YYYYMMDD = "2";

    private DateConverter() { }

    /**
     * Convert a date string between COBOL date formats.
     *
     * @param inputDate  the input date string (up to 20 chars, only the relevant portion is used)
     * @param inputType  "1" for YYYYMMDD input, "2" for YYYY-MM-DD input
     * @param outputType "1" for YYYY-MM-DD output, "2" for YYYYMMDD output
     * @return the formatted output date string
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return "";
        }

        String yyyy;
        String mm;
        String dd;

        if (INPUT_YYYYMMDD.equals(inputType)) {
            // Input is YYYYMMDD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(4, 6);
            dd = inputDate.substring(6, 8);
        } else if (INPUT_YYYY_MM_DD.equals(inputType)) {
            // Input is YYYY-MM-DD
            yyyy = inputDate.substring(0, 4);
            mm = inputDate.substring(5, 7);
            dd = inputDate.substring(8, 10);
        } else {
            throw new IllegalArgumentException("Unknown input date type: " + inputType);
        }

        if (OUTPUT_YYYYMMDD.equals(outputType)) {
            return yyyy + mm + dd;
        } else if (OUTPUT_YYYY_MM_DD.equals(outputType)) {
            return yyyy + "-" + mm + "-" + dd;
        } else {
            throw new IllegalArgumentException("Unknown output date type: " + outputType);
        }
    }
}
