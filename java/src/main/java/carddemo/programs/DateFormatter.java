package carddemo.programs;

/**
 * Port of the {@code COBDATFT} date-format routine (called through the {@code CODATECN} copybook):
 * converts between {@code YYYYMMDD} (type 1) and {@code YYYY-MM-DD} (type 2).
 */
public final class DateFormatter {
    private DateFormatter() {
    }

    /** {@code YYYY-MM-DD} to {@code YYYYMMDD} (input type 2, output type 2, as used by CBACT01C). */
    public static String dashedToCompact(String dashed) {
        return dashed.substring(0, 4) + dashed.substring(5, 7) + dashed.substring(8, 10);
    }

    /** {@code YYYYMMDD} to {@code YYYY-MM-DD} (input type 1, output type 1). */
    public static String compactToDashed(String compact) {
        return compact.substring(0, 4) + "-" + compact.substring(4, 6) + "-" + compact.substring(6, 8);
    }
}
