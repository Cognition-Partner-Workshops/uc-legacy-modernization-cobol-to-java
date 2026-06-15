package com.carddemo.batch;

import com.carddemo.batch.io.CobolDecimalParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CobolDecimalParserTest {

    @ParameterizedTest
    @CsvSource({
            "'00000001940{', 2, 194.00",
            "'00000020200{', 2, 2020.00",
            "'00000010200{', 2, 1020.00",
            "'00000000000{', 2, 0.00",
            "'00000001005{', 2, 100.50",
            "'00000001525{', 2, 152.50",
            "'00000010250N', 2, -1025.05",
            "'00000025000P', 2, -2500.07",
    })
    void parseZonedDecimal(String raw, int decimals, String expected) {
        BigDecimal result = CobolDecimalParser.parseZonedDecimal(raw, decimals);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void parseZonedDecimalNegativeZero() {
        BigDecimal result = CobolDecimalParser.parseZonedDecimal("00000000000}", 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void parseZonedDecimalPositiveDigits() {
        // A=+1, I=+9
        assertEquals(new BigDecimal("0.01"),
                CobolDecimalParser.parseZonedDecimal("00000000000A", 2));
        assertEquals(new BigDecimal("0.09"),
                CobolDecimalParser.parseZonedDecimal("00000000000I", 2));
    }

    @Test
    void parseZonedDecimalNegativeDigits() {
        // J=-1, R=-9
        assertEquals(new BigDecimal("-0.01"),
                CobolDecimalParser.parseZonedDecimal("00000000000J", 2));
        assertEquals(new BigDecimal("-0.09"),
                CobolDecimalParser.parseZonedDecimal("00000000000R", 2));
    }

    @Test
    void formatRoundTrip() {
        BigDecimal value = new BigDecimal("194.00");
        String encoded = CobolDecimalParser.formatZonedDecimal(value, 12, 2);
        assertEquals("00000001940{", encoded);

        BigDecimal decoded = CobolDecimalParser.parseZonedDecimal(encoded, 2);
        assertEquals(0, value.compareTo(decoded));
    }

    @Test
    void formatNegative() {
        BigDecimal value = new BigDecimal("-1025.05");
        String encoded = CobolDecimalParser.formatZonedDecimal(value, 12, 2);
        assertEquals("00000010250N", encoded);

        BigDecimal decoded = CobolDecimalParser.parseZonedDecimal(encoded, 2);
        assertEquals(0, value.compareTo(decoded));
    }

    @Test
    void invalidOverpunchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CobolDecimalParser.parseZonedDecimal("00000000000Z", 2));
    }
}
