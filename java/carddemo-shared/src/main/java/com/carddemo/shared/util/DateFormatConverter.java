package com.carddemo.shared.util;

import com.carddemo.shared.model.ConversionResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Java re-implementation of the Assembler program {@code COBDATFT}
 * ({@code app/asm/COBDATFT.asm}, record layout {@code COCDATFT} in
 * {@code app/maclib/COCDATFT.mac}).
 *
 * <p>{@code COBDATFT} converts a date between two representations driven by an
 * input-type byte ({@code COINTYPE}) and writes either the converted date
 * ({@code COOUTDT}) or the literal {@code 'INVALID INPUT'} ({@code COERMSG}) on
 * failure. This port keeps that input-type/error-message contract while using
 * {@link java.time.LocalDate} and {@link DateTimeFormatter} for the actual
 * conversion:</p>
 * <ul>
 *   <li>input type {@code '1'}: ISO {@code YYYY-MM-DD} &rarr; US {@code MM/DD/YYYY}</li>
 *   <li>input type {@code '2'}: US {@code MM/DD/YYYY} &rarr; ISO {@code YYYY-MM-DD}</li>
 * </ul>
 */
public class DateFormatConverter {

    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter US = DateTimeFormatter
            .ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Converts an ISO date to US format: {@code YYYY-MM-DD} &rarr;
     * {@code MM/DD/YYYY}.
     *
     * @param isoDate the ISO date string
     * @return the US-formatted date
     * @throws IllegalArgumentException if {@code isoDate} is null or not a valid
     *                                  {@code YYYY-MM-DD} date
     */
    public String isoToUs(String isoDate) {
        return reformat(isoDate, ISO, US);
    }

    /**
     * Converts a US date to ISO format: {@code MM/DD/YYYY} &rarr;
     * {@code YYYY-MM-DD}.
     *
     * @param usDate the US date string
     * @return the ISO-formatted date
     * @throws IllegalArgumentException if {@code usDate} is null or not a valid
     *                                  {@code MM/DD/YYYY} date
     */
    public String usToIso(String usDate) {
        return reformat(usDate, US, ISO);
    }

    /**
     * Converts a date driven by an input-type byte, mirroring the
     * {@code COINTYPE} dispatch in {@code COBDATFT}. Invalid input types or
     * unparseable dates yield a failed {@link ConversionResult} carrying the
     * {@code COERMSG} text {@code "INVALID INPUT"}.
     *
     * @param inputType {@code "1"} (ISO&rarr;US) or {@code "2"} (US&rarr;ISO)
     * @param date      the date to convert
     * @return the conversion result
     */
    public ConversionResult convert(String inputType, String date) {
        if (inputType == null) {
            return ConversionResult.error();
        }
        try {
            return switch (inputType.trim()) {
                case "1" -> ConversionResult.ok(isoToUs(date));
                case "2" -> ConversionResult.ok(usToIso(date));
                default -> ConversionResult.error();
            };
        } catch (IllegalArgumentException e) {
            return ConversionResult.error();
        }
    }

    private String reformat(String date, DateTimeFormatter from, DateTimeFormatter to) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        try {
            return LocalDate.parse(date.trim(), from).format(to);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + date, e);
        }
    }
}
