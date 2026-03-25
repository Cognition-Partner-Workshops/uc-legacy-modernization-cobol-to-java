package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.util.CobolDecimalParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CobolDecimalParserTest {

    @ParameterizedTest(name = "parse with {1} decimals = {2}")
    @CsvSource({
            "'00000001940{', 2, 194.00",
            "'00000020200{', 2, 2020.00",
            "'00000010200{', 2, 1020.00",
            "'00000000000{', 2, 0.00",
            "'00000001580{', 2, 158.00",
            "'00000001471', 2, 14.71",
            "'0000000102}', 2, -10.20",
            "'00000000100J', 2, -10.01",
    })
    void parsesZonedDecimal(String input, int decimals, String expected) {
        BigDecimal result = CobolDecimalParser.parse(input, decimals);
        assertEquals(new BigDecimal(expected).stripTrailingZeros(),
                result.stripTrailingZeros(),
                "Parsing '" + input + "' with " + decimals + " decimal places");
    }

    @Test
    void parsesPositiveZeroOverpunch() {
        BigDecimal result = CobolDecimalParser.parse("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parsesLargePositiveValue() {
        BigDecimal result = CobolDecimalParser.parse("00000061300{", 2);
        assertEquals(new BigDecimal("6130.00"), result);
    }

    @Test
    void parsesNegativeValueWithCloseBrace() {
        BigDecimal result = CobolDecimalParser.parse("00000010250}", 2);
        assertEquals(new BigDecimal("-1025.00"), result);
    }

    @Test
    void parsesNegativeValueWithR() {
        BigDecimal result = CobolDecimalParser.parse("0000001025R", 2);
        assertEquals(new BigDecimal("-102.59"), result);
    }

    @Test
    void parsesNullAndEmpty() {
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse(null, 2));
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse("", 2));
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse("   ", 2));
    }

    @Test
    void formatsPositiveValue() {
        String result = CobolDecimalParser.format(new BigDecimal("194.00"), 12, 2);
        assertEquals("00000001940{", result);
    }

    @Test
    void formatsZeroValue() {
        String result = CobolDecimalParser.format(BigDecimal.ZERO, 12, 2);
        assertEquals("00000000000{", result);
    }

    @Test
    void formatsNegativeValue() {
        String result = CobolDecimalParser.format(new BigDecimal("-1025.00"), 12, 2);
        assertEquals("00000010250}", result);
    }

    @Test
    void formatsSmallPositiveValue() {
        String result = CobolDecimalParser.format(new BigDecimal("19.40"), 12, 2);
        assertEquals("00000000194{", result);
    }

    @Test
    void roundTrip() {
        String original = "00000001940{";
        BigDecimal parsed = CobolDecimalParser.parse(original, 2);
        assertEquals(new BigDecimal("194.00"), parsed);
        String formatted = CobolDecimalParser.format(parsed, 12, 2);
        assertEquals(original, formatted);
    }

    @Test
    void roundTripNegative() {
        String original = "00000102500}";
        BigDecimal parsed = CobolDecimalParser.parse(original, 2);
        assertEquals(new BigDecimal("-10250.00"), parsed);
        String formatted = CobolDecimalParser.format(parsed, 12, 2);
        assertEquals(original, formatted);
    }
}
