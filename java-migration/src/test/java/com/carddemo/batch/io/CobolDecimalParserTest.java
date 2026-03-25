package com.carddemo.batch.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for COBOL signed display (trailing overpunch) parsing.
 */
class CobolDecimalParserTest {

    @ParameterizedTest(name = "parse \"{0}\" with scale {1} → {2}")
    @CsvSource({
            // Positive values with { (overpunch +0)
            "00000001940{, 2, 194.00",
            "00000020200{, 2, 2020.00",
            "00000010200{, 2, 1020.00",
            "00000000000{, 2, 0.00",
            // Positive values with letter overpunch
            "00000001941A, 2, 194.11",
            "0000000194B, 2, 19.42",
            "00000001949I, 2, 194.99",
            // Negative values (} = -0, so last digit is 0 with negative sign)
            "00000001940}, 2, -194.00",
            "00000001025}, 2, -102.50",
            "0000000102N, 2, -10.25",
            // Scale 0 (no implied decimal)
            "00000001940{, 0, 19400",
    })
    void parseSigned(String raw, int scale, String expected) {
        BigDecimal result = CobolDecimalParser.parse(raw, scale);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void parseNegativeOverpunch() {
        // } = -0 → the whole number becomes negative only if there are non-zero digits
        // "00000001025}" means digits "000000010250" negative → -102.50
        BigDecimal result = CobolDecimalParser.parse("00000001025}", 2);
        assertEquals(new BigDecimal("-102.50"), result);
    }

    @Test
    void parseNegativeJ() {
        // J = -1 → "0000000102J" means digits "00000001021" negative → with scale 2 → -10.21
        BigDecimal result = CobolDecimalParser.parse("0000000102J", 2);
        assertEquals(new BigDecimal("-10.21"), result);
    }

    @Test
    void parseNegativeR() {
        // R = -9 → "0000000250R" means digits "00000002509" negative → with scale 2 → -25.09
        BigDecimal result = CobolDecimalParser.parse("0000000250R", 2);
        assertEquals(new BigDecimal("-25.09"), result);
    }

    @Test
    void parseUnsigned() {
        assertEquals(1L, CobolDecimalParser.parseUnsigned("00000000001"));
        assertEquals(50L, CobolDecimalParser.parseUnsigned("00000000050"));
        assertEquals(0L, CobolDecimalParser.parseUnsigned("00000000000"));
    }

    @Test
    void parseNullAndEmpty() {
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse(null, 2));
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse("", 2));
        assertEquals(BigDecimal.ZERO, CobolDecimalParser.parse("   ", 2));
    }

    @Test
    void invalidOverpunchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CobolDecimalParser.parse("0000000001Z", 2));
    }
}
