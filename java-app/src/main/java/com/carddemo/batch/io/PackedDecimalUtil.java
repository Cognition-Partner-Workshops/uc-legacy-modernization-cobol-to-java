package com.carddemo.batch.io;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Encodes/decodes COBOL COMP-3 (packed decimal) values.
 *
 * Packed-decimal stores two digits per byte (high nibble, low nibble),
 * with the final low nibble holding the sign (0xC = positive, 0xD = negative, 0xF = unsigned).
 *
 * PIC S9(10)V99 → 12 digits + sign → ceil((12+1)/2) = 7 bytes.
 */
public final class PackedDecimalUtil {

    private PackedDecimalUtil() {}

    /**
     * Encode a BigDecimal into COMP-3 packed-decimal bytes.
     *
     * @param value         the numeric value
     * @param totalDigits   total digits (integer + decimal), e.g. 12 for S9(10)V99
     * @param decimalPlaces implied decimal places, e.g. 2 for V99
     * @return the packed byte array
     */
    public static byte[] encode(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(decimalPlaces, RoundingMode.HALF_UP);
        String digits = abs.movePointRight(decimalPlaces)
                .toBigInteger()
                .toString();

        while (digits.length() < totalDigits) {
            digits = "0" + digits;
        }

        int signNibble = negative ? 0x0D : 0x0C;

        int totalNibbles = totalDigits + 1;
        int byteCount = (totalNibbles + 1) / 2;
        byte[] packed = new byte[byteCount];

        String nibbles;
        if (totalNibbles % 2 != 0) {
            nibbles = "0" + digits;
        } else {
            nibbles = digits;
        }

        for (int i = 0; i < byteCount - 1; i++) {
            int high = nibbles.charAt(i * 2) - '0';
            int low = nibbles.charAt(i * 2 + 1) - '0';
            packed[i] = (byte) ((high << 4) | low);
        }

        int lastDigit = nibbles.charAt(nibbles.length() - 1) - '0';
        packed[byteCount - 1] = (byte) ((lastDigit << 4) | signNibble);

        return packed;
    }

    /**
     * Decode COMP-3 packed-decimal bytes into a BigDecimal.
     *
     * @param packed        the packed byte array
     * @param decimalPlaces implied decimal places
     * @return the decoded value
     */
    public static BigDecimal decode(byte[] packed, int decimalPlaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < packed.length - 1; i++) {
            int b = packed[i] & 0xFF;
            sb.append(b >> 4);
            sb.append(b & 0x0F);
        }

        int lastByte = packed[packed.length - 1] & 0xFF;
        sb.append(lastByte >> 4);
        int signNibble = lastByte & 0x0F;
        boolean negative = (signNibble == 0x0D || signNibble == 0x0B);

        BigDecimal value = new BigDecimal(sb.toString()).movePointLeft(decimalPlaces);
        return negative ? value.negate() : value;
    }
}
