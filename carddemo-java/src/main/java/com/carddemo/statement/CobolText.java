package com.carddemo.statement;

/** {@code PIC X(n)} MOVE and {@code STRING ... DELIMITED BY} semantics. */
public final class CobolText {

    private CobolText() {
    }

    /** {@code MOVE} into a {@code PIC X(length)} item: left justified, space padded, truncated. */
    public static String alphanumeric(String value, int length) {
        String v = value == null ? "" : value;
        if (v.length() >= length) {
            return v.substring(0, length);
        }
        return v + " ".repeat(length - v.length());
    }

    /** Extracts a fixed-width field, space padding when the record is short. */
    public static String field(String record, int offset, int length) {
        String padded = alphanumeric(record, Math.max(record.length(), offset + length));
        return padded.substring(offset, offset + length);
    }

    /**
     * The sending-item part of {@code STRING source DELIMITED BY delimiter}: everything
     * before the first occurrence of {@code delimiter}, or the whole item when absent.
     */
    public static String delimitedBy(String source, String delimiter) {
        int at = source.indexOf(delimiter);
        return at < 0 ? source : source.substring(0, at);
    }
}
