package com.carddemo.batch;

/**
 * Java equivalent of the COBDATFT assembler routine referenced in CBACT01C.
 * Handles date format conversions as specified by the CODATECN copybook.
 *
 * <pre>
 * CODATECN-TYPE / CODATECN-OUTTYPE values:
 *   '1' = YYYYMMDD    (compact, no separators)
 *   '2' = YYYY-MM-DD  (ISO-style with dashes)
 * </pre>
 *
 * In CBACT01C, both input and output types are '2', but the output type '2'
 * maps to YYYYMMDD (compact) per the 88-level definitions in CODATECN:
 *   88  YYYY-MM-DD-OP  VALUE "1".
 *   88  YYYYMMDD-OP    VALUE "2".
 *
 * So: input "2025-05-20" (YYYY-MM-DD) -> output "20250520" (YYYYMMDD),
 * then padded to 10 chars with trailing spaces for PIC X(10).
 */
public final class DateFormatter {

    private DateFormatter() {
        // utility class
    }

    /**
     * Convert a date string based on input type and output type codes.
     *
     * @param inputDate  the date string to convert (up to 20 chars per CODATECN)
     * @param inputType  '1' = YYYYMMDD, '2' = YYYY-MM-DD
     * @param outputType '1' = YYYY-MM-DD, '2' = YYYYMMDD
     * @return the formatted date string, padded to 10 characters
     */
    public static String formatDate(String inputDate, char inputType, char outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case '1' -> {
                // YYYYMMDD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case '2' -> {
                // YYYY-MM-DD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input date type: " + inputType);
        }

        String result = switch (outputType) {
            case '1' -> yyyy + "-" + mm + "-" + dd;  // YYYY-MM-DD
            case '2' -> yyyy + mm + dd;               // YYYYMMDD
            default -> throw new IllegalArgumentException("Unknown output date type: " + outputType);
        };

        // Pad to 10 characters (PIC X(10) in CODATECN-0UT-DATE is 20 but
        // OUT-ACCT-REISSUE-DATE is PIC X(10))
        return CobolDecimalUtils.fixedWidth(result, 10);
    }
}
