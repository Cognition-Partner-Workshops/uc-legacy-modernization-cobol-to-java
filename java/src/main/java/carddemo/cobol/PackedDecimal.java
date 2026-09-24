package carddemo.cobol;

import java.math.BigDecimal;
import java.math.BigInteger;

/** USAGE COMP-3 (packed decimal): two digits per byte, trailing sign nibble {@code C} (+), {@code D} (-), {@code F} (unsigned). */
public final class PackedDecimal {
    private PackedDecimal() {
    }

    public static int byteLength(int digits) {
        return digits / 2 + 1;
    }

    public static byte[] encode(BigDecimal value, int digits, int scale, boolean signed) {
        BigInteger unscaled = ZonedDecimal.truncate(value, digits, scale);
        int length = byteLength(digits);
        // An even digit count leaves an unused leading zero nibble.
        String text = ZonedDecimal.leftPad(unscaled.abs().toString(), length * 2 - 1);
        byte[] out = new byte[length];
        for (int i = 0; i < text.length(); i++) {
            int nibble = text.charAt(i) - '0';
            if (i % 2 == 0) {
                out[i / 2] |= (byte) (nibble << 4);
            } else {
                out[i / 2] |= (byte) nibble;
            }
        }
        int sign = !signed ? 0x0F : unscaled.signum() < 0 ? 0x0D : 0x0C;
        out[length - 1] |= (byte) sign;
        return out;
    }

    public static BigDecimal decode(byte[] data, int offset, int digits, int scale) {
        int length = byteLength(digits);
        BigInteger magnitude = BigInteger.ZERO;
        for (int i = 0; i < length * 2 - 1; i++) {
            int b = data[offset + i / 2] & 0xFF;
            int nibble = i % 2 == 0 ? b >> 4 : b & 0x0F;
            magnitude = magnitude.multiply(BigInteger.TEN).add(BigInteger.valueOf(nibble));
        }
        int sign = data[offset + length - 1] & 0x0F;
        BigDecimal value = new BigDecimal(magnitude, scale);
        return sign == 0x0D ? value.negate() : value;
    }
}
