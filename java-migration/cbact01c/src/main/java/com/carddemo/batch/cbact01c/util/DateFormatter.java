package com.carddemo.batch.cbact01c.util;

/**
 * Java replacement for the COBDATFT assembler routine referenced in CBACT01C.
 *
 * <p>The assembler program converts dates between two formats:
 * <ul>
 *   <li>Type 1 input ({@code YYYYMMDD})  → Type 1 output ({@code YYYY-MM-DD})</li>
 *   <li>Type 2 input ({@code YYYY-MM-DD}) → Type 2 output ({@code YYYYMMDD})</li>
 * </ul>
 *
 * <p>In CBACT01C the call is always Type&nbsp;2&nbsp;→&nbsp;Type&nbsp;2
 * (strip dashes from a {@code YYYY-MM-DD} date).
 */
public final class DateFormatter {

    /** Input / output type: compact {@code YYYYMMDD}. */
    public static final char TYPE_COMPACT = '1';

    /** Input / output type: dashed {@code YYYY-MM-DD}. */
    public static final char TYPE_DASHED = '2';

    private DateFormatter() { /* utility class */ }

    /**
     * Convert a date string according to the given input and output types,
     * mirroring the COBDATFT assembler logic exactly.
     *
     * @param inputType  '1' for YYYYMMDD input, '2' for YYYY-MM-DD input
     * @param outputType '1' for YYYY-MM-DD output, '2' for YYYYMMDD output
     * @param inputDate  the date string to convert
     * @return the reformatted date, or {@code null} if the combination is invalid
     * @throws IllegalArgumentException on invalid type combinations (mirrors GOTOERR)
     */
    public static String format(char inputType, char outputType, String inputDate) {
        if (inputType == TYPE_COMPACT) {
            // YYYYMMDD → only YYYY-MM-DD output allowed
            if (outputType == TYPE_DASHED) {
                throw new IllegalArgumentException(
                        "INVALID INPUT: type 1 input cannot produce type 2 output");
            }
            if (inputDate.length() < 8) {
                throw new IllegalArgumentException("INVALID INPUT: date too short for YYYYMMDD");
            }
            if (inputDate.charAt(4) == '-') {
                throw new IllegalArgumentException(
                        "INVALID INPUT: compact date must not contain dashes");
            }
            // MVC: YYYY + '-' + MM + '-' + DD
            return inputDate.substring(0, 4) + "-"
                    + inputDate.substring(4, 6) + "-"
                    + inputDate.substring(6, 8);
        }

        if (inputType == TYPE_DASHED) {
            // YYYY-MM-DD → only YYYYMMDD output allowed
            if (outputType == TYPE_COMPACT) {
                throw new IllegalArgumentException(
                        "INVALID INPUT: type 2 input cannot produce type 1 output");
            }
            if (inputDate.length() < 10) {
                throw new IllegalArgumentException("INVALID INPUT: date too short for YYYY-MM-DD");
            }
            // MVC: YYYY + MM + DD  (positions 0-3, 5-6, 8-9)
            return inputDate.substring(0, 4)
                    + inputDate.substring(5, 7)
                    + inputDate.substring(8, 10);
        }

        throw new IllegalArgumentException("INVALID INPUT: unknown type '" + inputType + "'");
    }

    /**
     * Convenience method used by CBACT01C: convert {@code YYYY-MM-DD} → {@code YYYYMMDD}.
     */
    public static String stripDashes(String dashedDate) {
        return format(TYPE_DASHED, TYPE_DASHED, dashedDate);
    }
}
