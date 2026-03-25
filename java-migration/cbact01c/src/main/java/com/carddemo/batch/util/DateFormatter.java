package com.carddemo.batch.util;

/**
 * Date format converter — replaces the COBDATFT assembler program.
 *
 * The COBOL program CBACT01C calls COBDATFT via:
 * <pre>
 *     MOVE ACCT-REISSUE-DATE TO CODATECN-INP-DATE WS-REISSUE-DATE.
 *     MOVE '2' TO CODATECN-TYPE.      (input is YYYY-MM-DD)
 *     MOVE '2' TO CODATECN-OUTTYPE.   (output is YYYYMMDD)
 *     CALL 'COBDATFT' USING CODATECN-REC.
 * </pre>
 *
 * The assembler logic (from COBDATFT.asm):
 * <ul>
 *   <li>Type '1' input: YYYYMMDD → output YYYY-MM-DD (if outtype='1')</li>
 *   <li>Type '2' input: YYYY-MM-DD → output YYYYMMDD (if outtype='2')</li>
 *   <li>Type '1' + outtype '2' → ERROR</li>
 *   <li>Type '2' + outtype '1' → ERROR</li>
 * </ul>
 */
public final class DateFormatter {

    private DateFormatter() {
    }

    /**
     * Result of a date formatting operation.
     *
     * @param outputDate  the formatted date string, or null on error
     * @param errorMessage error message if conversion failed, or null on success
     */
    public record Result(String outputDate, String errorMessage) {
        public boolean isSuccess() {
            return errorMessage == null;
        }
    }

    /**
     * Convert a date between formats.
     *
     * @param inputType  "1" for YYYYMMDD input, "2" for YYYY-MM-DD input
     * @param inputDate  the date string to convert
     * @param outputType "1" for YYYY-MM-DD output, "2" for YYYYMMDD output
     * @return conversion result
     */
    public static Result convert(String inputType, String inputDate, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return new Result(null, "INVALID INPUT");
        }

        return switch (inputType) {
            case "1" -> convertFromYyyymmdd(inputDate, outputType);
            case "2" -> convertFromYyyyMmDd(inputDate, outputType);
            default -> new Result(null, "INVALID INPUT");
        };
    }

    /**
     * Input type '1': YYYYMMDD → only valid output type is '1' (YYYY-MM-DD).
     * Output type '2' with input type '1' is an error per the assembler logic.
     */
    private static Result convertFromYyyymmdd(String input, String outputType) {
        // The assembler checks if position 4 contains '-'; if so, it's an error
        // (means the input is actually YYYY-MM-DD, not YYYYMMDD)
        if (input.length() >= 5 && input.charAt(4) == '-') {
            return new Result(null, "INVALID INPUT");
        }
        if ("2".equals(outputType)) {
            return new Result(null, "INVALID INPUT");
        }

        // YYYYMMDD → YYYY-MM-DD
        if (input.length() < 8) {
            return new Result(null, "INVALID INPUT");
        }
        String yyyy = input.substring(0, 4);
        String mm = input.substring(4, 6);
        String dd = input.substring(6, 8);
        return new Result(yyyy + "-" + mm + "-" + dd, null);
    }

    /**
     * Input type '2': YYYY-MM-DD → only valid output type is '2' (YYYYMMDD).
     * Output type '1' with input type '2' is an error per the assembler logic.
     */
    private static Result convertFromYyyyMmDd(String input, String outputType) {
        if ("1".equals(outputType)) {
            return new Result(null, "INVALID INPUT");
        }

        // YYYY-MM-DD → YYYYMMDD
        if (input.length() < 10) {
            return new Result(null, "INVALID INPUT");
        }
        String yyyy = input.substring(0, 4);
        String mm = input.substring(5, 7);
        String dd = input.substring(8, 10);
        return new Result(yyyy + mm + dd, null);
    }
}
