package carddemo.cobol;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * USAGE DISPLAY numeric fields ({@code PIC [S]9(n)V9(m)}) as laid out by GnuCOBOL with
 * {@code -fsign=EBCDIC}: the sign is over-punched on the trailing digit using the EBCDIC zone
 * conventions transcribed to ASCII ({@code {, A-I} positive, {@code }, J-R} negative). This is
 * also the convention of the {@code app/data/ASCII} fixtures. A plain trailing digit reads as
 * positive. The sign is tracked separately from the value so that a negative zero
 * ({@code ...0}}) survives a MOVE, as it does in COBOL.
 */
public final class ZonedDecimal {
    private static final byte[] POSITIVE = Bytes.ascii("{ABCDEFGHI");
    private static final byte[] NEGATIVE = Bytes.ascii("}JKLMNOPQR");

    private ZonedDecimal() {
    }

    /** Decodes a numeric DISPLAY field of {@code digits} total digits with {@code scale} decimals. */
    public static BigDecimal decode(byte[] data, int offset, int digits, int scale, boolean signed) {
        BigInteger magnitude = BigInteger.ZERO;
        for (int i = 0; i < digits; i++) {
            byte b = data[offset + i];
            int digit = i == digits - 1 && signed ? trailingDigit(b, offset + i) : digitValue(b, offset + i);
            magnitude = magnitude.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
        }
        BigDecimal value = new BigDecimal(magnitude, scale);
        return signed && isNegative(data, offset, digits) ? value.negate() : value;
    }

    /** True when the trailing byte carries a negative over-punch (also for a negative zero). */
    public static boolean isNegative(byte[] data, int offset, int digits) {
        return indexOf(NEGATIVE, data[offset + digits - 1]) >= 0;
    }

    /**
     * Encodes {@code value} the way a COBOL MOVE/COMPUTE stores into a DISPLAY field without
     * ROUNDED or ON SIZE ERROR: excess decimals are truncated and excess high-order digits are lost.
     */
    public static byte[] encode(BigDecimal value, int digits, int scale, boolean signed) {
        return encode(value, digits, scale, signed, value.signum() < 0);
    }

    /** As {@link #encode(BigDecimal, int, int, boolean)} with an explicit sign, so a negative zero can be stored. */
    public static byte[] encode(BigDecimal value, int digits, int scale, boolean signed, boolean negative) {
        BigInteger magnitude = truncate(value, digits, scale).abs();
        byte[] out = Bytes.ascii(leftPad(magnitude.toString(), digits));
        if (signed) {
            int last = out[digits - 1] - '0';
            out[digits - 1] = negative ? NEGATIVE[last] : POSITIVE[last];
        }
        return out;
    }

    /**
     * Text GnuCOBOL ({@code -std=ibm}) emits for {@code DISPLAY} of a numeric DISPLAY item: all
     * digits (implied decimal point omitted) followed by a trailing {@code +}/{@code -} when signed.
     */
    public static String displayText(BigDecimal value, int digits, int scale, boolean signed, boolean negative) {
        String text = leftPad(truncate(value, digits, scale).abs().toString(), digits);
        if (!signed) {
            return text;
        }
        return text + (negative ? "-" : "+");
    }

    /** Unscaled value with COBOL store truncation applied (decimals dropped, high-order digits lost). */
    static BigInteger truncate(BigDecimal value, int digits, int scale) {
        BigInteger unscaled = value.setScale(scale, RoundingMode.DOWN).unscaledValue();
        BigInteger magnitude = unscaled.abs().mod(BigInteger.TEN.pow(digits));
        return unscaled.signum() < 0 ? magnitude.negate() : magnitude;
    }

    static String leftPad(String digitsText, int width) {
        return "0".repeat(width - digitsText.length()) + digitsText;
    }

    private static int trailingDigit(byte b, int position) {
        int idx = indexOf(POSITIVE, b);
        if (idx >= 0) {
            return idx;
        }
        idx = indexOf(NEGATIVE, b);
        return idx >= 0 ? idx : digitValue(b, position);
    }

    private static int digitValue(byte b, int position) {
        if (b < '0' || b > '9') {
            throw new NumberFormatException(
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
