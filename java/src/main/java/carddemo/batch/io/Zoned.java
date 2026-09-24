package carddemo.batch.io;

/**
 * Zoned-decimal (USAGE DISPLAY) numeric encoding as it appears in the ASCII fixtures:
 * a positive sign is over-punched on the last digit as {@code {} / A-I, a negative sign as
 * {@code }} / J-R (EBCDIC-style overpunch, {@code -fsign=EBCDIC}). Values are held as scaled
 * longs (all digits, implied decimal point).
 */
public final class Zoned {
    private static final byte[] POSITIVE = "{ABCDEFGHI".getBytes();
    private static final byte[] NEGATIVE = "}JKLMNOPQR".getBytes();

    private Zoned() {
    }

    /** Parses a signed or unsigned zoned field; unsigned digits are treated as positive. */
    public static long parse(byte[] buf, int off, int len) {
        long value = 0;
        boolean negative = false;
        for (int i = 0; i < len; i++) {
            int b = buf[off + i] & 0xFF;
            int digit;
            if (b >= '0' && b <= '9') {
                digit = b - '0';
            } else if (b == ' ') {
                digit = 0;
            } else {
                digit = overpunchDigit(b, POSITIVE);
                if (digit < 0) {
                    digit = overpunchDigit(b, NEGATIVE);
                    if (digit < 0) {
                        throw new IllegalArgumentException("Not a zoned digit: 0x" + Integer.toHexString(b));
                    }
                    negative = true;
                }
            }
            value = value * 10 + digit;
        }
        return negative ? -value : value;
    }

    private static int overpunchDigit(int b, byte[] table) {
        for (int i = 0; i < table.length; i++) {
            if (table[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /** Stores a signed value with an over-punched sign on the last digit; high-order digits truncate. */
    public static void putSigned(byte[] buf, int off, int len, long value) {
        boolean negative = value < 0;
        putDigits(buf, off, len, Math.abs(value));
        int last = buf[off + len - 1] - '0';
        buf[off + len - 1] = negative ? NEGATIVE[last] : POSITIVE[last];
    }

    /** Stores an unsigned value as plain digits; high-order digits truncate. */
    public static void putUnsigned(byte[] buf, int off, int len, long value) {
        putDigits(buf, off, len, Math.abs(value));
    }

    private static void putDigits(byte[] buf, int off, int len, long magnitude) {
        long v = magnitude;
        for (int i = len - 1; i >= 0; i--) {
            buf[off + i] = (byte) ('0' + (v % 10));
            v /= 10;
        }
    }

    /** Text produced by DISPLAY of a signed DISPLAY field: all digits followed by '+' or '-'. */
    public static String display(long value, int len) {
        byte[] tmp = new byte[len];
        putDigits(tmp, 0, len, Math.abs(value));
        return new String(tmp, java.nio.charset.StandardCharsets.ISO_8859_1) + (value < 0 ? "-" : "+");
    }
}
