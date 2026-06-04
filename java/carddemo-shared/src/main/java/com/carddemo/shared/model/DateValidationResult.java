package com.carddemo.shared.model;

/**
 * Result of a date validation, mirroring the output of the COBOL program
 * {@code CSUTLDTC} ({@code app/cbl/CSUTLDTC.cbl}).
 *
 * <p>The original program returned a severity code in {@code RETURN-CODE}
 * (0 = valid, non-zero = invalid) and built an 80-character {@code WS-MESSAGE}
 * describing the result, the tested date and the mask used. This record
 * captures the same information in a typed form.</p>
 *
 * @param valid        {@code true} when the date is valid for the given mask
 *                     (severity code 0)
 * @param severityCode the LE/{@code CEEDAYS}-style severity code; {@code 0}
 *                     means valid, {@code 12} (severe error) means invalid
 * @param message      the human readable result message, equivalent to the
 *                     COBOL {@code WS-MESSAGE} field
 */
public record DateValidationResult(boolean valid, int severityCode, String message) {
}
