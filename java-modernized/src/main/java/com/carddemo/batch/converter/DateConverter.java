package com.carddemo.batch.converter;

/**
 * Replicates COBDATFT assembler date-format conversion logic.
 *
 * Supported conversions (mirroring CODATECN copybook types):
 *   Type 1 input (YYYYMMDD)  + Type 1 output → YYYY-MM-DD
 *   Type 1 input (YYYYMMDD)  + Type 2 output → YYYYMMDD   (identity)
 *   Type 2 input (YYYY-MM-DD)+ Type 1 output → YYYY-MM-DD (identity)
 *   Type 2 input (YYYY-MM-DD)+ Type 2 output → YYYYMMDD
 *
 * CBACT01C uses type-2 input / type-2 output (YYYY-MM-DD → YYYYMMDD).
 */
public final class DateConverter {

    private DateConverter() {}

    public static String convert(String inputDate, char inputType, char outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return inputDate;
        }

        String normalised = normaliseInput(inputDate.trim(), inputType);

        return formatOutput(normalised, outputType);
    }

    /**
     * Converts between YYYY-MM-DD and YYYYMMDD formats.
     * Convenience method for the most common CBACT01C usage (type '2' → type '2').
     */
    public static String convertDashedToCompact(String dashedDate) {
        return convert(dashedDate, '2', '2');
    }

    /**
     * Converts YYYYMMDD to YYYY-MM-DD.
     */
    public static String convertCompactToDashed(String compactDate) {
        return convert(compactDate, '1', '1');
    }

    private static String normaliseInput(String date, char type) {
        return switch (type) {
            case '1' -> {
                if (date.length() < 8) {
                    throw new IllegalArgumentException(
                            "YYYYMMDD input requires at least 8 characters: " + date);
                }
                yield date.substring(0, 8);
            }
            case '2' -> {
                if (date.length() < 10 || date.charAt(4) != '-' || date.charAt(7) != '-') {
                    throw new IllegalArgumentException(
                            "YYYY-MM-DD input expected: " + date);
                }
                yield date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unsupported input type: " + type);
        };
    }

    private static String formatOutput(String yyyymmdd, char type) {
        return switch (type) {
            case '1' -> yyyymmdd.substring(0, 4) + "-"
                      + yyyymmdd.substring(4, 6) + "-"
                      + yyyymmdd.substring(6, 8);
            case '2' -> yyyymmdd;
            default -> throw new IllegalArgumentException("Unsupported output type: " + type);
        };
    }
}
