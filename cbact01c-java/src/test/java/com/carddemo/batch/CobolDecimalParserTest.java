package com.carddemo.batch;

import com.carddemo.batch.util.CobolDecimalParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for COBOL zoned-decimal parsing and formatting (overpunch convention).
 *
 * <p>PIC S9(10)V99 = 12 display characters. The implied decimal (V99) means
 * the last 2 digits are fractional. Example: "00000001940{" has digits
 * "000000019400" -> integer 19400 -> with scale 2 -> 194.00.
 */
class CobolDecimalParserTest {

    // ---------------------------------------------------------------
    // parseSignedDecimal
    // ---------------------------------------------------------------

    @Test
    void parsePositiveZeroOverpunch() {
        // "00000000000{" -> digits "000000000000" -> 0 -> 0.00
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000000000{", 2);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void parsePositiveValueOverpunch() {
        // "00000001940{" -> digits "000000019400" -> 19400 -> 194.00
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parsePositiveValueWithNonZeroOverpunch() {
        // "00000020200{" -> digits "000000202000" -> 202000 -> 2020.00
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000020200{", 2);
        assertEquals(new BigDecimal("2020.00"), result);
    }

    @Test
    void parseNegativeOverpunch() {
        // '}' = -0 -> digits "000000019400" negative -> -194.00
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("00000001940}", 2);
        assertEquals(new BigDecimal("-194.00"), result);
    }

    @Test
    void parseNegativeWithJOverpunch() {
        // 'J' = -1, "0000000194J" -> digits "00000001941" -> 1941 -> scale 2 -> -19.41
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("0000000194J", 2);
        assertEquals(new BigDecimal("-19.41"), result);
    }

    @Test
    void parseNullReturnsZero() {
        BigDecimal result = CobolDecimalParser.parseSignedDecimal(null, 2);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void parseBlankReturnsZero() {
        BigDecimal result = CobolDecimalParser.parseSignedDecimal("            ", 2);
        assertEquals(new BigDecimal("0.00"), result);
    }

    // ---------------------------------------------------------------
    // formatSignedDecimal
    // ---------------------------------------------------------------

    @Test
    void formatPositiveZero() {
        String result = CobolDecimalParser.formatSignedDecimal(BigDecimal.ZERO, 12, 2);
        assertEquals("00000000000{", result);
    }

    @Test
    void formatPositiveValue() {
        // 194.00 -> 19400 -> "000000019400" -> last digit 0, positive -> '{'
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("194.00"), 12, 2);
        assertEquals("00000001940{", result);
    }

    @Test
    void formatNegativeValue() {
        // -102.50 -> abs 10250 -> "000000010250" -> last digit 0, negative -> '}'
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("-102.50"), 12, 2);
        assertEquals("00000001025}", result);
    }

    @Test
    void formatDefaultDebit() {
        // 2525.00 -> 252500 -> "000000252500" -> last digit 0, positive -> '{'
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("2525.00"), 12, 2);
        assertEquals("00000025250{", result);
    }

    @Test
    void formatLargeNegative() {
        // -1025.00 -> abs 102500 -> "000000102500" -> last digit 0, negative -> '}'
        String result = CobolDecimalParser.formatSignedDecimal(new BigDecimal("-1025.00"), 12, 2);
        assertEquals("00000010250}", result);
    }

    // ---------------------------------------------------------------
    // Roundtrip: parse -> format -> parse
    // ---------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "00000001940{, 194.00",
            "00000020200{, 2020.00",
            "00000010200{, 1020.00",
            "00000000000{, 0.00"
    })
    void roundTripPositive(String cobol, String expected) {
        BigDecimal parsed = CobolDecimalParser.parseSignedDecimal(cobol, 2);
        assertEquals(new BigDecimal(expected), parsed);

        String formatted = CobolDecimalParser.formatSignedDecimal(parsed, 12, 2);
        assertEquals(cobol, formatted);
    }

    // ---------------------------------------------------------------
    // parseUnsignedLong / formatUnsignedLong
    // ---------------------------------------------------------------

    @Test
    void parseUnsignedLongBasic() {
        assertEquals(1L, CobolDecimalParser.parseUnsignedLong("00000000001"));
        assertEquals(50L, CobolDecimalParser.parseUnsignedLong("00000000050"));
    }

    @Test
    void formatUnsignedLongBasic() {
        assertEquals("00000000001", CobolDecimalParser.formatUnsignedLong(1L, 11));
        assertEquals("00000000050", CobolDecimalParser.formatUnsignedLong(50L, 11));
    }
}
