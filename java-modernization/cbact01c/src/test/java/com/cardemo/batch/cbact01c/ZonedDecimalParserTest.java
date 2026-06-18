package com.cardemo.batch.cbact01c;

import com.cardemo.batch.cbact01c.util.ZonedDecimalParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDecimalParserTest {

    @Test
    void parsePositiveZero() {
        // "{" overpunch = +0 → 000000000000 → 0.00
        assertEquals(new BigDecimal("0.00"),
                ZonedDecimalParser.parse("00000000000{", 2));
    }

    @Test
    void parsePositiveValue() {
        // "00000001940{" → 000000019400 → 194.00
        assertEquals(new BigDecimal("194.00"),
                ZonedDecimalParser.parse("00000001940{", 2));
    }

    @Test
    void parseLargerPositive() {
        // "00000020200{" → 000000202000 → 2020.00
        assertEquals(new BigDecimal("2020.00"),
                ZonedDecimalParser.parse("00000020200{", 2));
    }

    @Test
    void parseNegativeValue() {
        // "00000001025}" = -000000010250 → -102.50
        assertEquals(new BigDecimal("-102.50"),
                ZonedDecimalParser.parse("00000001025}", 2));
    }

    @Test
    void parseNegativeWithJOverpunch() {
        // 'J' = -1 → last digit is 1
        // "00000002500J" → digits 000000025001, sign negative → -250.01 (with V99)
        assertEquals(new BigDecimal("-250.01"),
                ZonedDecimalParser.parse("00000002500J", 2));
    }

    @Test
    void formatPositiveValue() {
        // 194.00 → "00000001940{" (12 digits, 2 decimal)
        assertEquals("00000001940{",
                ZonedDecimalParser.format(new BigDecimal("194.00"), 12, 2));
    }

    @Test
    void formatNegativeValue() {
        // -1025.00 → "00000010250}" (12 digits, 2 decimal, negative zero overpunch)
        assertEquals("00000010250}",
                ZonedDecimalParser.format(new BigDecimal("-1025.00"), 12, 2));
    }

    @Test
    void formatZero() {
        assertEquals("00000000000{",
                ZonedDecimalParser.format(BigDecimal.ZERO, 12, 2));
    }

    @Test
    void roundTrip() {
        BigDecimal value = new BigDecimal("2525.00");
        String formatted = ZonedDecimalParser.format(value, 12, 2);
        BigDecimal parsed = ZonedDecimalParser.parse(formatted, 2);
        assertEquals(0, value.compareTo(parsed));
    }
}
