package com.carddemo.shared.util;

import com.carddemo.shared.model.DateValidationResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

/**
 * Java re-implementation of the COBOL program {@code CSUTLDTC}
 * ({@code app/cbl/CSUTLDTC.cbl}).
 *
 * <p>The original program validated a date against a format mask by calling the
 * Language Environment {@code CEEDAYS} API. It took three linkage parameters
 * ({@code LS-DATE}, {@code LS-DATE-FORMAT} and {@code LS-RESULT}), returned a
 * severity code in {@code RETURN-CODE} (0 = valid, non-zero = invalid) and built
 * an 80-character message ({@code WS-MESSAGE}) containing the result text, the
 * tested date and the mask used.</p>
 *
 * <p>This class reproduces that behaviour with {@link java.time}. Strict
 * resolution is used so semantically invalid dates such as {@code 2023-02-30}
 * are rejected, matching {@code CEEDAYS}' {@code FC-BAD-DATE-VALUE} feedback.</p>
 */
public class DateValidator {

    /** Severity returned for a valid date ({@code FC-INVALID-DATE} / 0). */
    public static final int SEVERITY_VALID = 0;
    /** Severity returned for an invalid date (LE severe error). */
    public static final int SEVERITY_INVALID = 12;

    private static final String RESULT_VALID = "Date is valid";
    private static final String RESULT_INVALID = "Date is invalid";
    private static final String RESULT_BAD_PIC = "Bad Pic String";

    /**
     * Supported format masks, mapped to the equivalent {@link DateTimeFormatter}
     * patterns. {@code uuuu} (proleptic year) is used rather than {@code yyyy}
     * so that {@link ResolverStyle#STRICT} can reject impossible dates.
     */
    private static final Map<String, String> SUPPORTED_MASKS = Map.of(
            "YYYY-MM-DD", "uuuu-MM-dd",
            "MM/DD/YYYY", "MM/dd/uuuu",
            "DD/MM/YYYY", "dd/MM/uuuu",
            "YYYYMMDD", "uuuuMMdd");

    /**
     * Validates a date string against a format mask, mirroring {@code CSUTLDTC}.
     *
     * @param date       the date to test (equivalent to {@code LS-DATE})
     * @param dateFormat the format mask (equivalent to {@code LS-DATE-FORMAT}),
     *                   one of {@code YYYY-MM-DD}, {@code MM/DD/YYYY},
     *                   {@code DD/MM/YYYY} or {@code YYYYMMDD}
     * @return the validation result, including the severity code and a message
     *         equivalent to the COBOL {@code WS-MESSAGE}
     */
    public DateValidationResult validate(String date, String dateFormat) {
        String mask = dateFormat == null ? "" : dateFormat.trim();
        String input = date == null ? "" : date.trim();

        String pattern = SUPPORTED_MASKS.get(mask);
        if (pattern == null) {
            return new DateValidationResult(false, SEVERITY_INVALID,
                    buildMessage(SEVERITY_INVALID, RESULT_BAD_PIC, input, mask));
        }

        if (input.isEmpty()) {
            return new DateValidationResult(false, SEVERITY_INVALID,
                    buildMessage(SEVERITY_INVALID, RESULT_INVALID, input, mask));
        }

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(pattern)
                .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate.parse(input, formatter);
            return new DateValidationResult(true, SEVERITY_VALID,
                    buildMessage(SEVERITY_VALID, RESULT_VALID, input, mask));
        } catch (DateTimeParseException e) {
            return new DateValidationResult(false, SEVERITY_INVALID,
                    buildMessage(SEVERITY_INVALID, RESULT_INVALID, input, mask));
        }
    }

    /**
     * Builds a message in the layout of the COBOL {@code WS-MESSAGE} field:
     * severity, message code, result text, tested date and mask used.
     */
    private String buildMessage(int severity, String result, String date, String mask) {
        return String.format("%04d Mesg Code:%04d %-15s TstDate: %-10s Mask used:%-10s",
                severity, 0, result, date, mask);
    }
}
