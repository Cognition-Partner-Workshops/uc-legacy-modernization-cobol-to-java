package com.carddemo.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CobolDecimalUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "'00000001940{', 194.00",
            "'00000020200{', 2020.00",
            "'00000010200{', 1020.00",
            "'00000000000{', 0.00",
            "'00000001025}', -102.50"
    })
    void parseZonedDecimal_posAndNeg(String input, String expected) {
        BigDecimal result = CobolDecimalUtils.parseZonedDecimal(input, 2);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void parseZonedDecimal_allPositiveSignChars() {
        // { = +0, A = +1, B = +2, ... I = +9
        assertEquals(new BigDecimal("0.00"), CobolDecimalUtils.parseZonedDecimal("00000000000{", 2));
        assertEquals(new BigDecimal("0.01"), CobolDecimalUtils.parseZonedDecimal("00000000000A", 2));
        assertEquals(new BigDecimal("0.02"), CobolDecimalUtils.parseZonedDecimal("00000000000B", 2));
        assertEquals(new BigDecimal("0.09"), CobolDecimalUtils.parseZonedDecimal("00000000000I", 2));
    }

    @Test
    void parseZonedDecimal_allNegativeSignChars() {
        // } = -0, J = -1, K = -2, ... R = -9
        assertEquals(new BigDecimal("0.00"), CobolDecimalUtils.parseZonedDecimal("00000000000}", 2));
        assertEquals(new BigDecimal("-0.01"), CobolDecimalUtils.parseZonedDecimal("00000000000J", 2));
        assertEquals(new BigDecimal("-0.02"), CobolDecimalUtils.parseZonedDecimal("00000000000K", 2));
        assertEquals(new BigDecimal("-0.09"), CobolDecimalUtils.parseZonedDecimal("00000000000R", 2));
    }

    @Test
    void formatZonedDecimal_positive() {
        String result = CobolDecimalUtils.formatZonedDecimal(new BigDecimal("194.00"), 12, 2);
        assertEquals("00000001940{", result);
    }

    @Test
    void formatZonedDecimal_negative() {
        String result = CobolDecimalUtils.formatZonedDecimal(new BigDecimal("-1025.00"), 12, 2);
        assertEquals("00000010250}", result);
    }

    @Test
    void formatZonedDecimal_zero() {
        String result = CobolDecimalUtils.formatZonedDecimal(BigDecimal.ZERO.setScale(2), 12, 2);
        assertEquals("00000000000{", result);
    }

    @Test
    void roundTrip_zonedDecimal() {
        BigDecimal original = new BigDecimal("2525.00");
        String formatted = CobolDecimalUtils.formatZonedDecimal(original, 12, 2);
        BigDecimal parsed = CobolDecimalUtils.parseZonedDecimal(formatted, 2);
        assertEquals(original, parsed);
    }

    @Test
    void toComp3_positive() {
        byte[] result = CobolDecimalUtils.toComp3(new BigDecimal("2525.00"), 2, 7);
        // 252500 -> digits "00000252500", sign C -> 00 00 02 52 50 0C
        // 7 bytes = 14 nibbles = 13 digit nibbles + 1 sign
        // "0000000252500" + C
        // packed: 00 00 00 02 52 50 0C
        assertEquals(7, result.length);
        assertEquals((byte) 0x00, result[0]);
        assertEquals((byte) 0x00, result[1]);
        assertEquals((byte) 0x00, result[2]);
        assertEquals((byte) 0x02, result[3]);
        assertEquals((byte) 0x52, result[4]);
        assertEquals((byte) 0x50, result[5]);
        assertEquals((byte) 0x0C, result[6]);
    }

    @Test
    void toComp3_negative() {
        byte[] result = CobolDecimalUtils.toComp3(new BigDecimal("-2500.00"), 2, 7);
        // 250000 -> "0000000250000" + D
        // packed: 00 00 00 02 50 00 0D
        assertEquals((byte) 0x00, result[0]);
        assertEquals((byte) 0x00, result[1]);
        assertEquals((byte) 0x00, result[2]);
        assertEquals((byte) 0x02, result[3]);
        assertEquals((byte) 0x50, result[4]);
        assertEquals((byte) 0x00, result[5]);
        assertEquals((byte) 0x0D, result[6]);
    }

    @Test
    void toComp3_zero() {
        byte[] result = CobolDecimalUtils.toComp3(BigDecimal.ZERO.setScale(2), 2, 7);
        assertEquals((byte) 0x00, result[0]);
        assertEquals((byte) 0x00, result[1]);
        assertEquals((byte) 0x00, result[2]);
        assertEquals((byte) 0x00, result[3]);
        assertEquals((byte) 0x00, result[4]);
        assertEquals((byte) 0x00, result[5]);
        assertEquals((byte) 0x0C, result[6]);
    }

    @Test
    void formatUnsignedNumeric() {
        assertEquals("00000000001", CobolDecimalUtils.formatUnsignedNumeric(1, 11));
        assertEquals("00000000050", CobolDecimalUtils.formatUnsignedNumeric(50, 11));
    }

    @Test
    void fixedWidth_pad() {
        assertEquals("ABC       ", CobolDecimalUtils.fixedWidth("ABC", 10));
    }

    @Test
    void fixedWidth_truncate() {
        assertEquals("ABCDE", CobolDecimalUtils.fixedWidth("ABCDEFGHIJ", 5));
    }

    @Test
    void fixedWidth_null() {
        assertEquals("          ", CobolDecimalUtils.fixedWidth(null, 10));
    }
}
