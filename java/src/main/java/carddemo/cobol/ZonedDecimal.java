package carddemo.cobol;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * USAGE DISPLAY numeric fields ({@code PIC S9(n)V9(m)}) as laid out by GnuCOBOL with
 * {@code -fsign=EBCDIC}: the sign is over-punched on the trailing digit using the EBCDIC
 * zone conventions transcribed to ASCII ({@code {, A-I} positive, {@code }, J-R} negative).
 * A plain digit in the trailing position is accepted as positive on input.
 */
public final class ZonedDecimal {
    private static final byte[] POSITIVE = Bytes.ascii("{ABCDEFGHI");
    private static final byte[] NEGATIVE = Bytes.ascii("}JKLMNOPQR");

    private ZonedDecimal() {
    }

    /** Decodes a numeric DISPLAY field of {@code digits} total digits with {@code scale} decimals. */
    public static BigDecimal decode(byte[] data, int offset, int digits, int scale, boolean signed) {
        BigInteger magnitude = BigInteger.ZERO;
        boolean negative = false;
        for (int i = 0; i < digits; i++) {
            byte b = data[offset + i];
            int digit;
            if (i == digits - 1 && signed) {
                int idx = indexOf(POSITIVE, b);
                if (idx >= 0) {
                    digit = idx;
                } else {
                    idx = indexOf(NEGATIVE, b);
                    if (idx >= 0) {
                        digit = idx;
                        negative = true;
                    } else {
                        digit = digitValue(b, offset + i);
                    }
                }
            } else {
                digit = digitValue(b, offset + i);
            }
            magnitude = magnitude.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
        }
        BigDecimal value = new BigDecimal(magnitude, scale);
        return negative ? value.negate() : value;
    }

    /**
     * Encodes {@code value} the way a COBOL MOVE/COMPUTE stores into a DISPLAY field without
     * ROUNDED or ON SIZE ERROR: excess decimals are truncated and excess high-order digits are lost.
     */
    public static byte[] encode(BigDecimal value, int digits, int scale, boolean signed) {
        BigInteger unscaled = truncate(value, digits, scale);
        boolean negative = signed && unscaled.signum() < 0;
        String text = leftPad(unscaled.abs().toString(), digits);
        byte[] out = Bytes.ascii(text);
        if (signed) {
            int last = out[digits - 1] - '0';
            out[digits - 1] = negative ? NEGATIVE[last] : POSITIVE[last];
        }
        return out;
    }

    /**
     * Text GnuCOBOL emits for {@code DISPLAY} of a numeric DISPLAY item: all digits (implied
     * decimal point omitted) followed by a trailing {@code +}/{@code -} when the item is signed.
     */
    public static String displayText(BigDecimal value, int digits, int scale, boolean signed) {
        BigInteger unscaled = truncate(value, digits, scale);
        String text = leftPad(unscaled.abs().toString(), digits);
        if (!signed) {
            return text;
        }
        return text + (unscaled.signum() < 0 ? "-" : "+");
    }

    /** Unscaled magnitude with COBOL store truncation applied (decimals dropped, high-order digits lost). */
    static BigInteger truncate(BigDecimal value, int digits, int scale) {
        BigInteger unscaled = value.setScale(scale, RoundingMode.DOWN).unscaledValue();
        BigInteger limit = BigInteger.TEN.pow(digits);
        BigInteger magnitude = unscaled.abs().mod(limit);
        return unscaled.signum() < 0 ? magnitude.negate() : magnitude;
    }

    static String leftPad(String digitsText, int width) {
        return "0".repeat(width - digitsText.length()) + digitsText;
    }

    private static int digitValue(byte b, int position) {
        if (b < '0' || b > '9') {
            throw new IllegalArgumentException(
                    "non-numeric byte 0x" + Integer.toHexString(b & 0xFF) + " at offset " + position);
        }
        return b - '0';
    }

    private static int indexOf(byte[] table, byte b) {
        for (int i = 0; i < table.length; i++) {
            if (table[i] == b) {
                return i;
            }
        }
        return -1;
    }
}
