package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDecimalParserTest {

    @Test
    void parsePositiveZeroTrailing() {
        // '{' = +0
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalParser.parse("00000001940{", 2));
    }

    @Test
    void parsePositiveNonZeroTrailing() {
        // 'A' = +1
        assertEquals(new BigDecimal("194.01"),
                ZonedDecimalParser.parse("00000001940A", 2));
    }

    @Test
    void parseLargeValue() {
        assertEquals(new BigDecimal("8922.00"),
                ZonedDecimalParser.parse("00000089220{", 2));
    }

    @Test
    void parseZeroValue() {
        assertEquals(new BigDecimal("0.00"),
                ZonedDecimalParser.parse("00000000000{", 2));
    }

    @Test
    void parseNegativeZeroTrailing() {
        // '}' = -0, treated as negative zero which is still 0.00
        BigDecimal result = ZonedDecimalParser.parse("00000000000}", 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void parseNegativeValue() {
        // 'J' = -1
        assertEquals(new BigDecimal("-194.01"),
                ZonedDecimalParser.parse("00000001940J", 2));
    }

    @Test
    void parseNegativeTrailingR() {
        // 'R' = -9
        assertEquals(new BigDecimal("-194.09"),
                ZonedDecimalParser.parse("00000001940R", 2));
    }

    @Test
    void parseNoDecimalPlaces() {
        assertEquals(new BigDecimal("19400"),
                ZonedDecimalParser.parse("00000001940{", 0));
    }

    @Test
    void parseNullReturnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse(null, 2));
    }

    @Test
    void parseEmptyReturnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse("", 2));
    }

    @Test
    void parseUnsignedDigit() {
        // Plain digit at end (unsigned field)
        assertEquals(new BigDecimal("194.05"),
                ZonedDecimalParser.parse("000000019405", 2));
    }

    @Test
    void unrecognisedSignCharThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("00000001940?", 2));
    }
}
