package com.carddemo.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CobolFieldParserTest {

    @ParameterizedTest
    @CsvSource({
            "'00000001940{', 2, 194.00",
            "'00000020200{', 2, 2020.00",
            "'00000010200{', 2, 1020.00",
            "'00000000000{', 2, 0.00",
            "'00000001000A', 2, 100.01",
            "'00000001000I', 2, 100.09",
            "'00000001940}', 2, -194.00",
            "'00000001000J', 2, -100.01",
            "'00000001000R', 2, -100.09",
    })
    void testParseSignedDecimal(String raw, int decimals, BigDecimal expected) {
        assertEquals(expected, CobolFieldParser.parseSignedDecimal(raw, decimals));
    }

    @Test
    void testParseSignedDecimalPositiveZero() {
        assertEquals(new BigDecimal("0.00"),
                CobolFieldParser.parseSignedDecimal("00000000000{", 2));
    }

    @Test
    void testParseSignedDecimalNegativeZero() {
        assertEquals(new BigDecimal("0.00"),
                CobolFieldParser.parseSignedDecimal("00000000000}", 2));
    }

    @Test
    void testParseSignedDecimalPlainDigit() {
        assertEquals(new BigDecimal("100.05"),
                CobolFieldParser.parseSignedDecimal("000000010005", 2));
    }

    @Test
    void testFormatSignedDecimal() {
        assertEquals("00000001940{",
                CobolFieldParser.formatSignedDecimal(new BigDecimal("194.00"), 12, 2));
        assertEquals("00000020200{",
                CobolFieldParser.formatSignedDecimal(new BigDecimal("2020.00"), 12, 2));
        assertEquals("00000001940}",
                CobolFieldParser.formatSignedDecimal(new BigDecimal("-194.00"), 12, 2));
        assertEquals("00000001000A",
                CobolFieldParser.formatSignedDecimal(new BigDecimal("100.01"), 12, 2));
    }

    @Test
    void testFormatComp3Positive() {
        final byte[] packed = CobolFieldParser.formatComp3(new BigDecimal("2525.00"), 12, 2);
        // PIC S9(10)V99 COMP-3 = (12+2)/2 = 7 bytes
        // Value 252500 padded to 12 digits: 000000252500, prepend 0 → 0000000252500 (13 digits)
        // Packed: 00 00 00 25 25 00 0C
        assertEquals(7, packed.length);
        assertEquals((byte) 0x00, packed[0]);
        assertEquals((byte) 0x00, packed[1]);
        assertEquals((byte) 0x00, packed[2]);
        assertEquals((byte) 0x02, packed[3]);
        assertEquals((byte) 0x52, packed[4]);
        assertEquals((byte) 0x50, packed[5]);
        assertEquals((byte) 0x0C, packed[6]);
    }

    @Test
    void testFormatComp3Negative() {
        final byte[] packed = CobolFieldParser.formatComp3(new BigDecimal("-2500.00"), 12, 2);
        assertEquals(7, packed.length);
        // last nibble should be D (negative)
        assertEquals(0x0D, packed[6] & 0xFF);
    }

    @Test
    void testFormatUnsignedNumeric() {
        assertEquals("00000000001", CobolFieldParser.formatUnsignedNumeric("00000000001", 11));
        assertEquals("00000000005", CobolFieldParser.formatUnsignedNumeric("5", 11));
    }

    @Test
    void testFormatAlphanumeric() {
        assertEquals("Y", CobolFieldParser.formatAlphanumeric("Y", 1));
        assertEquals("2014-11-20", CobolFieldParser.formatAlphanumeric("2014-11-20", 10));
        assertEquals("test      ", CobolFieldParser.formatAlphanumeric("test", 10));
        assertEquals("          ", CobolFieldParser.formatAlphanumeric("", 10));
    }

    @Test
    void testParseAlphanumeric() {
        assertEquals("Y", CobolFieldParser.parseAlphanumeric("Y"));
        assertEquals("test", CobolFieldParser.parseAlphanumeric("test      "));
        assertEquals("", CobolFieldParser.parseAlphanumeric("          "));
    }

    @Test
    void testInvalidOverpunchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CobolFieldParser.parseSignedDecimal("0000000000X", 2));
    }

    @Test
    void testRoundTripSignedDecimal() {
        final BigDecimal original = new BigDecimal("1234.56");
        final String formatted = CobolFieldParser.formatSignedDecimal(original, 12, 2);
        final BigDecimal parsed = CobolFieldParser.parseSignedDecimal(formatted, 2);
        assertEquals(original, parsed);
    }

    @Test
    void testRoundTripNegativeSignedDecimal() {
        final BigDecimal original = new BigDecimal("-9876.54");
        final String formatted = CobolFieldParser.formatSignedDecimal(original, 12, 2);
        final BigDecimal parsed = CobolFieldParser.parseSignedDecimal(formatted, 2);
        assertEquals(original, parsed);
    }
}
