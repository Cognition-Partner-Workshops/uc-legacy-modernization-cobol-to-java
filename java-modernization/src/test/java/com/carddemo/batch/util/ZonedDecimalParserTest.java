package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the COBOL zoned-decimal parser that handles overpunch sign encoding.
 */
class ZonedDecimalParserTest {

    // ------------------------------------------------------------------
    // Parsing tests
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "parse \"{0}\" with {1} decimal places → {2}")
    @CsvSource({
            // Positive values with { overpunch (+0)
            "00000001940{, 2, 194.00",
            "00000020200{, 2, 2020.00",
            "00000010200{, 2, 1020.00",
            "00000000000{, 2, 0.00",

            // Positive values with letter overpunch (A=+1 .. I=+9)
            "0000000100A, 2, 10.01",
            "0000000100I, 2, 10.09",
            "0000000100E, 2, 10.05",

            // Negative values with } overpunch (-0)
            "00000001940}, 2, -194.00",

            // Negative values with letter overpunch (J=-1 .. R=-9)
            "0000000100J, 2, -10.01",
            "0000000100R, 2, -10.09",
            "0000000100N, 2, -10.05",

            // Unsigned (plain digit as last char)
            "000000019400, 2, 194.00",

            // Zero decimal places
            "0000001940{, 0, 19400",

            // Larger values
            "00000084100{, 2, 8410.00",
            "00000097500{, 2, 9750.00"
    })
    @DisplayName("parse zoned decimal")
    void testParse(String raw, int decimalPlaces, String expected) {
        BigDecimal result = ZonedDecimalParser.parse(raw, decimalPlaces);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    @DisplayName("parse real account data line values")
    void testParseRealDataValues() {
        // Account 1 from acctdata.txt: 00000001940{
        // PIC S9(10)V99 → 194.00
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalParser.parse("00000001940{", 2));

        // Credit limit: 00000020200{  → 2020.00
        assertEquals(new BigDecimal("2020.00"),
                ZonedDecimalParser.parse("00000020200{", 2));

        // Cash credit limit: 00000010200{ → 1020.00
        assertEquals(new BigDecimal("1020.00"),
                ZonedDecimalParser.parse("00000010200{", 2));

        // Zero debit: 00000000000{ → 0.00
        assertEquals(new BigDecimal("0.00"),
                ZonedDecimalParser.parse("00000000000{", 2));
    }

    @Test
    @DisplayName("null or empty input throws IllegalArgumentException")
    void testParseNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimalParser.parse(null, 2));
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimalParser.parse("", 2));
    }

    @Test
    @DisplayName("invalid overpunch character throws IllegalArgumentException")
    void testParseInvalidOverpunch() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("0000000194Z", 2));
    }

    // ------------------------------------------------------------------
    // Formatting tests
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "format {0} as S9({1})V{2} → \"{3}\"")
    @CsvSource({
            "194.00, 12, 2, 00000001940{",
            "2020.00, 12, 2, 00000020200{",
            "0.00, 12, 2, 00000000000{",
            "-194.00, 12, 2, 00000001940}",
            "10.01, 12, 2, 00000000100A",
            "-10.09, 12, 2, 00000000100R",
            "2525.00, 12, 2, 00000025250{",
    })
    @DisplayName("format BigDecimal to zoned decimal")
    void testFormat(String value, int totalDigits, int decimalPlaces, String expected) {
        String result = ZonedDecimalParser.format(new BigDecimal(value), totalDigits, decimalPlaces);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("parse and format are inverse operations")
    void testRoundTrip() {
        String original = "00000001940{";
        BigDecimal parsed = ZonedDecimalParser.parse(original, 2);
        String formatted = ZonedDecimalParser.format(parsed, 12, 2);
        assertEquals(original, formatted);
    }
}
