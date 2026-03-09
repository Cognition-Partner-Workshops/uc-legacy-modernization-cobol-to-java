package com.carddemo.batch.service;

/**
 * Replaces the COBOL assembler subroutine COBDATFT that is CALLed via
 * the CODATECN copybook parameter block.
 *
 * <p>The COBOL program sets:
 * <ul>
 *   <li>{@code CODATECN-TYPE = '2'}  → input is YYYY-MM-DD</li>
 *   <li>{@code CODATECN-OUTTYPE = '2'} → output is YYYYMMDD</li>
 * </ul>
 *
 * <p>From the CODATECN copybook, the format types are:
 * <ul>
 *   <li><b>Input</b> type 1: {@code YYYYMMDD}, type 2: {@code YYYY-MM-DD}</li>
 *   <li><b>Output</b> type 1: {@code YYYY-MM-DD}, type 2: {@code YYYYMMDD}</li>
 * </ul>
 * Note that the output types are the <em>reverse</em> of the input types.
 */
public final class DateConverter {

    /** Input type: YYYYMMDD (compact, no separators). */
    public static final String INPUT_TYPE_COMPACT = "1";

    /** Input type: YYYY-MM-DD (hyphen-separated). */
    public static final String INPUT_TYPE_HYPHENATED = "2";

    /** Output type: YYYY-MM-DD (hyphen-separated). */
    public static final String OUTPUT_TYPE_HYPHENATED = "1";

    /** Output type: YYYYMMDD (compact, no separators). */
    public static final String OUTPUT_TYPE_COMPACT = "2";

    private DateConverter() {
        // utility class
    }

    /**
     * Convert a date string from one COBOL date format to another.
     *
     * @param inputDate  the date string to convert
     * @param inputType  the format of {@code inputDate} ("1" or "2")
     * @param outputType the desired output format ("1" or "2")
     * @return the reformatted date string
     * @throws IllegalArgumentException if types are unrecognised or the date is malformed
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        // Normalise to components
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case INPUT_TYPE_COMPACT -> {
                // YYYYMMDD → 8 chars minimum
                if (inputDate == null || inputDate.trim().length() < 8) {
                    throw new IllegalArgumentException("Compact date must be at least 8 characters: " + inputDate);
                }
                String trimmed = inputDate.trim();
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(4, 6);
                dd = trimmed.substring(6, 8);
            }
            case INPUT_TYPE_HYPHENATED -> {
                // YYYY-MM-DD → 10 chars minimum
                if (inputDate == null || inputDate.trim().length() < 10) {
                    throw new IllegalArgumentException("Hyphenated date must be at least 10 characters: " + inputDate);
                }
                String trimmed = inputDate.trim();
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(5, 7);
                dd = trimmed.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        // Reassemble in the requested output format
        // Note: output types are reversed from input types per CODATECN copybook
        return switch (outputType) {
            case OUTPUT_TYPE_HYPHENATED -> yyyy + "-" + mm + "-" + dd;
            case OUTPUT_TYPE_COMPACT -> yyyy + mm + dd;
            default -> throw new IllegalArgumentException("Unknown output type: " + outputType);
        };
    }

    /**
     * Extract the 4-digit year from a YYYY-MM-DD date string.
     * Used to populate {@code VB2-ACCT-REISSUE-YYYY} /
     * {@code WS-ACCT-REISSUE-YYYY}.
     */
    public static String extractYear(String hyphenatedDate) {
        if (hyphenatedDate == null || hyphenatedDate.trim().length() < 4) {
            return "    ";
        }
        return hyphenatedDate.trim().substring(0, 4);
    }
}
