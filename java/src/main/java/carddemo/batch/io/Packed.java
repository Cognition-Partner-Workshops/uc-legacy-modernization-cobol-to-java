package carddemo.batch.io;

/** COMP-3 (packed decimal) encoding: two digits per byte, trailing sign nibble C (+) or D (-). */
public final class Packed {
    private Packed() {
    }

    /** Number of bytes occupied by a packed field with the given digit count. */
    public static int bytesFor(int digits) {
        return digits / 2 + 1;
    }

    public static void put(byte[] buf, int off, int digits, long value) {
        int len = bytesFor(digits);
        long v = Math.abs(value);
        int sign = value < 0 ? 0xD : 0xC;
        buf[off + len - 1] = (byte) (((v % 10) << 4) | sign);
        v /= 10;
        for (int i = len - 2; i >= 0; i--) {
            int lo = (int) (v % 10);
            v /= 10;
            int hi = (int) (v % 10);
            v /= 10;
            buf[off + i] = (byte) ((hi << 4) | lo);
        }
    }

    public static long parse(byte[] buf, int off, int digits) {
        int len = bytesFor(digits);
        long v = 0;
        for (int i = 0; i < len - 1; i++) {
            v = v * 100 + ((buf[off + i] >> 4) & 0xF) * 10 + (buf[off + i] & 0xF);
        }
        int last = buf[off + len - 1] & 0xFF;
        v = v * 10 + (last >> 4);
        return (last & 0xF) == 0xD ? -v : v;
    }
}
