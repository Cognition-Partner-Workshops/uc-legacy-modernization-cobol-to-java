package com.carddemo.batch.ebcdic;

import java.math.BigDecimal;

/**
 * Reusable COMP-3 decoder for later copybook migrations.
 * The USRSEC source record itself has no COMP-3 fields.
 */
public final class PackedDecimalDecoder {
    private PackedDecimalDecoder() {
    }

    public static BigDecimal decode(byte[] packed, int scale) {
        if (packed == null || packed.length == 0) {
            throw new IllegalArgumentException("Packed decimal cannot be empty");
        }
        int signNibble = packed[packed.length - 1] & 0x0F;
        boolean negative = signNibble == 0x0D;
        if (signNibble != 0x0C && signNibble != 0x0D && signNibble != 0x0F) {
            throw new IllegalArgumentException("Unsupported packed decimal sign nibble: " + signNibble);
        }

        StringBuilder digits = new StringBuilder(packed.length * 2 - 1);
        for (int i = 0; i < packed.length; i++) {
            int value = packed[i] & 0xFF;
            int high = (value >>> 4) & 0x0F;
            int low = value & 0x0F;
            if (i == packed.length - 1) {
                appendDigit(digits, high);
            } else {
                appendDigit(digits, high);
                appendDigit(digits, low);
            }
        }
        BigDecimal result = new BigDecimal(digits.toString()).movePointLeft(scale);
        return negative ? result.negate() : result;
    }

    private static void appendDigit(StringBuilder digits, int nibble) {
        if (nibble > 9) {
            throw new IllegalArgumentException("Invalid packed decimal digit nibble: " + nibble);
        }
        digits.append((char) ('0' + nibble));
    }
}
