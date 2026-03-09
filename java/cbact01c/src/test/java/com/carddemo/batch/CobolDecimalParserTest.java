package com.carddemo.batch;

import com.carddemo.batch.util.CobolDecimalParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for COBOL zoned-decimal parsing and formatting,
 * verifying the overpunch sign convention used in the CardDemo data files.
 */
class CobolDecimalParserTest {

    @ParameterizedTest(name = "parseSignedDecimal(\"{0}\", {1}) = {2}")
    @CsvSource({
            // Positive values with '{' (overpunch +0)
            "'00000001940{', 2, 194.00",
            "'00000020200{', 2, 2020.00",
            "'00000010200{', 2, 1020.00",
            "'00000000000{', 2, 0.00",
            // Positive values with trailing letter overpunch
            "'0000000194A', 2, 19.41",
            "'0000000194I', 2, 19.49",
            // Negative values with '}' (overpunch -0)
            "'00000001940}', 2, -194.00",
            // Negative values with J-R
            "'0000000194J', 2, -19.41",
            "'0000000194R', 2, -19.49",
            // Zero
            "'00000000000{', 2, 0.00",
    })
    void testParseSignedDecimal(String raw, int decimalPlaces, String expected) {
        BigDecimal result = CobolDecimalParser.parseSignedDecimal(raw, decimalPlaces);
        assertEquals(new BigDecimal(expected), result);
    }

    @ParameterizedTest(name = "formatSignedDecimal({0}, {1}, {2}) = \"{3}\"")
    @CsvSource({
            "194.00, 12, 2, '00000001940{'",
            "2020.00, 12, 2, '00000020200{'",
            "0.00, 12, 2, '00000000000{'",
            "-194.00, 12, 2, '00000001940}'",
            "2525.00, 12, 2, '00000025250{'",
            "1005.00, 12, 2, '00000010050{'",
            "1525.00, 12, 2, '00000015250{'",
            "-1025.00, 12, 2, '00000010250}'",
            "-2500.00, 12, 2, '00000025000}'",
    })
    void testFormatSignedDecimal(String value, int totalDigits, int decimalPlaces, String expected) {
        String result = CobolDecimalParser.formatSignedDecimal(
                new BigDecimal(value), totalDigits, decimalPlaces);
        assertEquals(expected, result);
    }

    @Test
    void testRoundTrip() {
        // Parse from COBOL format, then format back — should be identical
        String original = "00000001940{";
        BigDecimal parsed = CobolDecimalParser.parseSignedDecimal(original, 2);
        String formatted = CobolDecimalParser.formatSignedDecimal(parsed, 12, 2);
        assertEquals(original, formatted);
    }

    @Test
    void testRoundTripNegative() {
        String original = "00000001940}";
        BigDecimal parsed = CobolDecimalParser.parseSignedDecimal(original, 2);
        String formatted = CobolDecimalParser.formatSignedDecimal(parsed, 12, 2);
        assertEquals(original, formatted);
    }

    @Test
    void testNullAndEmpty() {
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parseSignedDecimal(null, 2));
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parseSignedDecimal("", 2));
    }
}
