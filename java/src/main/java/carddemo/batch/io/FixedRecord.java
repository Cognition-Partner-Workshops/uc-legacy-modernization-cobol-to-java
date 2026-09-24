package carddemo.batch.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A fixed-length record area. Like a COBOL record description it is a persistent byte buffer
 * whose fields are views at fixed offsets; subclasses expose the copybook fields.
 */
public class FixedRecord {
    public static final byte SPACE = 0x20;

    protected final byte[] data;

    public FixedRecord(int length) {
        this.data = new byte[length];
        Arrays.fill(data, SPACE);
    }

    public int length() {
        return data.length;
    }

    /** Copies bytes into the record area (MOVE / READ INTO); shorter input is space padded. */
    public void load(byte[] source) {
        Arrays.fill(data, SPACE);
        System.arraycopy(source, 0, data, 0, Math.min(source.length, data.length));
    }

    public byte[] bytes() {
        return data.clone();
    }

    public byte[] slice(int off, int len) {
        return Arrays.copyOfRange(data, off, off + len);
    }

    /** The record as text in the 1:1 byte-to-char mapping used for keys and DISPLAY. */
    public String text() {
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    public String str(int off, int len) {
        return new String(data, off, len, StandardCharsets.ISO_8859_1);
    }

    /** MOVE alphanumeric: left justified, space padded, truncated on the right. */
    public void setStr(int off, int len, String value) {
        byte[] src = value.getBytes(StandardCharsets.ISO_8859_1);
        Arrays.fill(data, off, off + len, SPACE);
        System.arraycopy(src, 0, data, off, Math.min(src.length, len));
    }

    public void setBytes(int off, byte[] src) {
        System.arraycopy(src, 0, data, off, src.length);
    }

    public long num(int off, int len) {
        return Zoned.parse(data, off, len);
    }

    public void setSigned(int off, int len, long value) {
        Zoned.putSigned(data, off, len, value);
    }

    public void setUnsigned(int off, int len, long value) {
        Zoned.putUnsigned(data, off, len, value);
    }

    public void setPacked(int off, int digits, long value) {
        Packed.put(data, off, digits, value);
    }

    public void fill(int off, int len, byte b) {
        Arrays.fill(data, off, off + len, b);
    }
}
