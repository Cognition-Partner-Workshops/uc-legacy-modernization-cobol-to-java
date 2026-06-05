package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDecimalParserTest {

    @ParameterizedTest(name = "parse \"{0}\" (scale {1}) == {2}")
    @CsvSource({
            // Positive values with overpunch '{' = +0  (PIC S9(10)V99 = 12 bytes)
            "'00000001940{', 2, 194.00",
            "'00000020200{', 2, 2020.00",
            "'00000010200{', 2, 1020.00",
            "'00000000000{', 2, 0.00",
            // Positive with non-zero last digit (12 byte fields)
            "'00000000010A', 2, 1.01",
            "'00000000010I', 2, 1.09",
            "'00000003450E', 2, 345.05",
            // Negative values (12 byte fields)
            "'00000000100}', 2, -10.00",
            "'00000000010J', 2, -1.01",
            "'00000000010R', 2, -1.09",
            "'00000010250}', 2, -1025.00",
            // Plain trailing digit (unsigned)
            "'000000034505', 2, 345.05",
    })
    void parseShouldReturnCorrectDecimal(String raw, int scale, String expected) {
        BigDecimal result = ZonedDecimalParser.parse(raw, scale);
        assertEquals(0, new BigDecimal(expected).compareTo(result),
                () -> "Expected " + expected + " but got " + result);
    }

    @Test
    void formatPositiveWithOverpunch() {
        BigDecimal value = new BigDecimal("194.00");
        String formatted = ZonedDecimalParser.format(value, 12, 2);
        assertEquals("00000001940{", formatted);
    }

    @Test
    void formatNegativeWithOverpunch() {
        BigDecimal value = new BigDecimal("-1025.00");
        String formatted = ZonedDecimalParser.format(value, 12, 2);
        assertEquals("00000010250}", formatted);
    }

    @Test
    void formatLargePositive() {
        BigDecimal value = new BigDecimal("2020.00");
        String formatted = ZonedDecimalParser.format(value, 12, 2);
        assertEquals("00000020200{", formatted);
    }

    @Test
    void formatAndParseRoundTrip() {
        BigDecimal original = new BigDecimal("3456.78");
        String formatted = ZonedDecimalParser.format(original, 12, 2);
        BigDecimal parsed = ZonedDecimalParser.parse(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    @Test
    void formatAndParseRoundTripNegative() {
        BigDecimal original = new BigDecimal("-999.99");
        String formatted = ZonedDecimalParser.format(original, 12, 2);
        BigDecimal parsed = ZonedDecimalParser.parse(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    @Test
    void parseRejectsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse(null, 2));
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("", 2));
    }

    @Test
    void parseRejectsUnknownOverpunch() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("000000#", 2));
    }
}
