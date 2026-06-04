package com.carddemo.shared.model;

/**
 * Result of a date format conversion, mirroring the output area of the
 * Assembler program {@code COBDATFT} ({@code app/asm/COBDATFT.asm}) as defined
 * by the {@code COCDATFT} DSECT ({@code app/maclib/COCDATFT.mac}).
 *
 * <p>The original program wrote the converted date into {@code COOUTDT} and, on
 * failure, moved the literal {@code 'INVALID INPUT'} into the {@code COERMSG}
 * field. This record captures both the success flag and those two outputs.</p>
 *
 * @param success      {@code true} when the conversion succeeded
 * @param outputDate   the converted date (equivalent to {@code COOUTDT}), or
 *                     {@code null} when the conversion failed
 * @param errorMessage the error message (equivalent to {@code COERMSG}), or
 *                     {@code null} when the conversion succeeded
 */
public record ConversionResult(boolean success, String outputDate, String errorMessage) {

    /** Error text used by the original {@code COBDATFT} program. */
    public static final String INVALID_INPUT = "INVALID INPUT";

    /**
     * Creates a successful result.
     *
     * @param outputDate the converted date
     * @return a successful {@link ConversionResult}
     */
    public static ConversionResult ok(String outputDate) {
        return new ConversionResult(true, outputDate, null);
    }

    /**
     * Creates a failed result carrying the {@code COBDATFT} error message.
     *
     * @return a failed {@link ConversionResult}
     */
    public static ConversionResult error() {
        return new ConversionResult(false, null, INVALID_INPUT);
    }
}
