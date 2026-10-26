package carddemo.cobol;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Byte-level helpers mirroring COBOL alphanumeric MOVE semantics (left-justify, space-fill/truncate). */
public final class Bytes {
    public static final byte SPACE = 0x20;

    private Bytes() {
    }

    public static byte[] spaces(int length) {
        byte[] out = new byte[length];
        Arrays.fill(out, SPACE);
        return out;
    }

    public static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }

    public static String ascii(byte[] data, int offset, int length) {
        return new String(data, offset, length, StandardCharsets.US_ASCII);
    }

    /** MOVE alphanumeric: copies {@code source} into the target area, truncating or padding with spaces on the right. */
    public static void moveAlnum(byte[] source, byte[] target, int offset, int length) {
        int copy = Math.min(source.length, length);
        System.arraycopy(source, 0, target, offset, copy);
        if (copy < length) {
            Arrays.fill(target, offset + copy, offset + length, SPACE);
        }
    }

    public static void moveAlnum(String source, byte[] target, int offset, int length) {
        moveAlnum(ascii(source), target, offset, length);
    }

    public static byte[] slice(byte[] data, int offset, int length) {
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    /** COBOL alphanumeric comparison of two equal-length areas (unsigned byte order). */
    public static int compare(byte[] left, byte[] right) {
        return Arrays.compareUnsigned(left, right);
    }
}
