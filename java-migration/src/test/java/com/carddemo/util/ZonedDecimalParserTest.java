package com.carddemo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for COBOL zoned-decimal overpunch sign decoding.
 */
class ZonedDecimalParserTest {

    @Test
    void positiveZero_decodedCorrectly() {
        // { = positive 0
        assertEquals(new BigDecimal("0.00"), ZonedDecimalParser.decode("00000000000{"));
    }

    @Test
    void positiveValue_decodedCorrectly() {
        // 00000001940{ -> 1940 with implied V99 -> 19.40
        // But wait: S9(10)V99 means 10 integer + 2 decimal = 12 digits
        // 00000001940{ has 12 chars: digits=00000001940, last={->0
        // So full digits = 000000019400, move point 2 left = 194.00
        assertEquals(new BigDecimal("194.00"), ZonedDecimalParser.decode("00000001940{"));
    }

    @Test
    void largePositiveValue_decodedCorrectly() {
        // 00000020200{ -> digits=000000202000 -> 2020.00
        assertEquals(new BigDecimal("2020.00"), ZonedDecimalParser.decode("00000020200{"));
    }

    @Test
    void negativeValue_decodedCorrectly() {
        // } = negative 0: 00000009190} -> digits=000000091900 -> -919.00
        assertEquals(new BigDecimal("-919.00"), ZonedDecimalParser.decode("00000009190}"));
    }

    @Test
    void negativeWithNonZeroLastDigit() {
        // N = negative 5: 00000050025N -> digits=000000500255 -> -5002.55
        assertEquals(new BigDecimal("-5002.55"), ZonedDecimalParser.decode("00000050025N"));
    }

    @Test
    void positiveWithNonZeroLastDigit() {
        // G = positive 7: 0000005047G -> digits=00000050477 -> 504.77
        assertEquals(new BigDecimal("504.77"), ZonedDecimalParser.decode("0000005047G"));
    }

    @ParameterizedTest
    @CsvSource({
            "'{', '0'",
            "'A', '1'",
            "'B', '2'",
            "'C', '3'",
            "'D', '4'",
            "'E', '5'",
            "'F', '6'",
            "'G', '7'",
            "'H', '8'",
            "'I', '9'"
    })
    void allPositiveOverpunchCharacters(char overpunch, char expectedDigit) {
        String raw = "0" + overpunch;
        BigDecimal result = ZonedDecimalParser.decode(raw);
        String expected = "0.0" + expectedDigit;
        assertEquals(new BigDecimal(expected), result);
    }

    @ParameterizedTest
    @CsvSource({
            "'}', '0'",
            "'J', '1'",
            "'K', '2'",
            "'L', '3'",
            "'M', '4'",
            "'N', '5'",
            "'O', '6'",
            "'P', '7'",
            "'Q', '8'",
            "'R', '9'"
    })
    void allNegativeOverpunchCharacters(char overpunch, char expectedDigit) {
        String raw = "0" + overpunch;
        BigDecimal result = ZonedDecimalParser.decode(raw);
        String expected = "-0.0" + expectedDigit;
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void nullInput_returnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.decode(null));
    }

    @Test
    void blankInput_returnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.decode("   "));
    }

    @Test
    void noDecimalPlaces() {
        // 12345{ -> digits=123450 -> 123450 (no decimal shift)
        assertEquals(new BigDecimal("123450"), ZonedDecimalParser.decode("12345{", 0));
    }

    @Test
    void regularDigitAsLastChar() {
        // If last char is a regular digit (no overpunch), treat as positive
        assertEquals(new BigDecimal("123.45"), ZonedDecimalParser.decode("12345"));
    }
}
