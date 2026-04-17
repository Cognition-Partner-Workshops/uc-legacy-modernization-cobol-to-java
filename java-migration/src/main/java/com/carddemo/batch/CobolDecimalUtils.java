package com.carddemo.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

/**
 * Utility class for COBOL numeric format conversions.
 * Handles zoned decimal (DISPLAY) and packed decimal (COMP-3) formats
 * as used in the ASCII representation of mainframe data.
 */
public final class CobolDecimalUtils {

    private CobolDecimalUtils() {
        // utility class
    }

    /**
     * Zoned-decimal sign-overpunch mapping for the last byte of a signed
     * DISPLAY numeric field.  In ASCII-transferred mainframe data the
     * convention uses the characters {ABCDEFGHI for positive 0-9 and
     * }JKLMNOPQR for negative 0-9.
     */
    private static final String POSITIVE_SIGNS = "{ABCDEFGHI";
    private static final String NEGATIVE_SIGNS = "}JKLMNOPQR";

    /**
     * Parse a zoned-decimal (DISPLAY format) string into a BigDecimal.
     *
     * @param text           the raw text from the fixed-width record
     * @param impliedScale   number of implied decimal places (V99 = 2)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseZonedDecimal(String text, int impliedScale) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO.setScale(impliedScale, RoundingMode.UNNECESSARY);
        }

        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO.setScale(impliedScale, RoundingMode.UNNECESSARY);
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        String digits = trimmed.substring(0, trimmed.length() - 1);
        boolean negative = false;
        int lastDigit;

        int posIdx = POSITIVE_SIGNS.indexOf(lastChar);
        int negIdx = NEGATIVE_SIGNS.indexOf(lastChar);

        if (posIdx >= 0) {
            lastDigit = posIdx;
            negative = false;
        } else if (negIdx >= 0) {
            lastDigit = negIdx;
            negative = true;
        } else if (Character.isDigit(lastChar)) {
            // unsigned field
            lastDigit = lastChar - '0';
            negative = false;
        } else {
            throw new IllegalArgumentException("Invalid zoned decimal character: " + lastChar);
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits)
                .movePointLeft(impliedScale);

        return negative ? value.negate() : value;
    }

    /**
     * Format a BigDecimal as a zoned-decimal (DISPLAY format) string with
     * sign overpunch on the last byte.
     *
     * @param value      the value to format
     * @param totalWidth total character width of the PIC clause (e.g. PIC S9(10)V99 = 12)
     * @param scale      number of implied decimal places
     * @return the zoned-decimal string
     */
    public static String formatZonedDecimal(BigDecimal value, int totalWidth, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(scale, RoundingMode.HALF_UP);
        String unscaled = abs.movePointRight(scale).toBigInteger().toString();

        // Pad with leading zeros
        while (unscaled.length() < totalWidth) {
            unscaled = "0" + unscaled;
        }

        // Truncate if longer than expected
        if (unscaled.length() > totalWidth) {
            unscaled = unscaled.substring(unscaled.length() - totalWidth);
        }

        int lastDigit = unscaled.charAt(unscaled.length() - 1) - '0';
        String prefix = unscaled.substring(0, unscaled.length() - 1);
        char signChar = negative
                ? NEGATIVE_SIGNS.charAt(lastDigit)
                : POSITIVE_SIGNS.charAt(lastDigit);

        return prefix + signChar;
    }

    /**
     * Encode a BigDecimal value as COMP-3 (packed decimal) bytes.
     * PIC S9(10)V99 COMP-3 occupies 7 bytes: (12 digits + 1 sign nibble + padding) / 2 = 7.
     *
     * @param value      the value to encode
     * @param scale      implied decimal scale
     * @param byteLength the number of bytes for the COMP-3 field
     * @return the packed decimal byte array
     */
    public static byte[] toComp3(BigDecimal value, int scale, int byteLength) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(scale, RoundingMode.HALF_UP);
        String digits = abs.movePointRight(scale).toBigInteger().toString();

        // Total nibbles = byteLength * 2, last nibble is sign
        int totalNibbles = byteLength * 2;
        int digitNibbles = totalNibbles - 1;

        while (digits.length() < digitNibbles) {
            digits = "0" + digits;
        }
        if (digits.length() > digitNibbles) {
            digits = digits.substring(digits.length() - digitNibbles);
        }

        // Sign nibble: 0xC = positive, 0xD = negative
        int signNibble = negative ? 0x0D : 0x0C;

        byte[] result = new byte[byteLength];
        // Pack digits two per byte, sign nibble in the low nibble of the last byte
        for (int i = 0; i < byteLength; i++) {
            int highIdx = i * 2;
            int lowIdx = i * 2 + 1;

            int highNibble = digits.charAt(highIdx) - '0';
            int lowNibble;
            if (lowIdx < digitNibbles) {
                lowNibble = digits.charAt(lowIdx) - '0';
            } else {
                // Last byte: low nibble is sign
                lowNibble = signNibble;
            }
            result[i] = (byte) ((highNibble << 4) | lowNibble);
        }

        return result;
    }

    /**
     * Format an unsigned numeric PIC 9(n) value as a zero-padded string.
     */
    public static String formatUnsignedNumeric(long value, int width) {
        return String.format("%0" + width + "d", value);
    }

    /**
     * Pad or truncate a string to a fixed width (right-padded with spaces).
     */
    public static String fixedWidth(String value, int width) {
        if (value == null) {
            return " ".repeat(width);
        }
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return value + " ".repeat(width - value.length());
    }
}
