package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.service.ZonedDecimalParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ZonedDecimalParser}, verifying that COBOL zoned-decimal
 * values are correctly parsed from their ASCII representation.
 */
class ZonedDecimalParserTest {

    @ParameterizedTest(name = "parse(\"{0}\", {1}) == {2}")
    @CsvSource({
            // Positive values with '{' overpunch (+0)
            // PIC S9(10)V99 = 12 digits total (10 integer + 2 decimal)
            "00000001940{, 2, 194.00",
            "00000020200{, 2, 2020.00",
            "00000010200{, 2, 1020.00",
            "00000000000{, 2, 0.00",
            // Positive values with letter overpunch (+1 to +9)
            "00000000000A, 2, 0.01",
            "00000000000I, 2, 0.09",
            "00000000100A, 2, 10.01",
            // Negative values with '}' overpunch (-0)
            "00000001940}, 2, -194.00",
            // Negative values with letter overpunch (-1 to -9)
            "00000000000J, 2, -0.01",
            "00000000000R, 2, -0.09",
            "00000000100J, 2, -10.01",
            // Larger values from sample data
            "00000061300{, 2, 6130.00",
            "00000054480{, 2, 5448.00",
    })
    void parseShouldDecodeZonedDecimalCorrectly(String raw, int decimals, String expected) {
        BigDecimal result = ZonedDecimalParser.parse(raw, decimals);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void parseShouldHandleNullAndEmpty() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse(null, 2));
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse("", 2));
    }

    @Test
    void parseShouldHandleUnsignedDigits() {
        // Plain digit at the end (no overpunch) — treated as unsigned positive
        BigDecimal result = ZonedDecimalParser.parse("000000019400", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parseShouldThrowForInvalidOverpunch() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("0000000194X", 2));
    }

    @ParameterizedTest(name = "format({0}, {1}, {2}) == \"{3}\"")
    @CsvSource({
            "194.00,   12, 2, 00000001940{",
            "0.00,     12, 2, 00000000000{",
            "-194.00,  12, 2, 00000001940}",
            "6130.00,  12, 2, 00000061300{",
            "0.01,     12, 2, 00000000000A",
            "-0.01,    12, 2, 00000000000J",
    })
    void formatShouldEncodeZonedDecimalCorrectly(String value, int width,
                                                  int decimals, String expected) {
        String result = ZonedDecimalParser.format(new BigDecimal(value), width, decimals);
        assertEquals(expected, result);
    }

    @Test
    void roundTripShouldPreserveValue() {
        BigDecimal original = new BigDecimal("194.00");
        String formatted = ZonedDecimalParser.format(original, 12, 2);
        BigDecimal parsed = ZonedDecimalParser.parse(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }
}
