package com.carddemo.common.io;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWidthParserTest {

    @Test
    void extractField_normalCase() {
        String line = "ABCDEFGHIJ";
        assertThat(FixedWidthParser.extractField(line, 0, 3)).isEqualTo("ABC");
        assertThat(FixedWidthParser.extractField(line, 3, 4)).isEqualTo("DEFG");
    }

    @Test
    void extractField_paddsWhenLineTooShort() {
        String line = "AB";
        assertThat(FixedWidthParser.extractField(line, 0, 5)).isEqualTo("AB   ");
    }

    @Test
    void extractField_nullLine() {
        assertThat(FixedWidthParser.extractField(null, 0, 3)).isEqualTo("   ");
    }

    @Test
    void parseAlpha_stripsTrailingSpaces() {
        String line = "Hello     World";
        assertThat(FixedWidthParser.parseAlpha(line, 0, 10)).isEqualTo("Hello");
    }

    @Test
    void parseNumericString_preservesLeadingZeros() {
        String line = "00012345678";
        assertThat(FixedWidthParser.parseNumericString(line, 0, 11)).isEqualTo("00012345678");
    }

    @Test
    void parseSignedDecimal_positiveWithLeadingSign() {
        // +000000100000 → 1000.00
        String line = "+000000100000";
        BigDecimal result = FixedWidthParser.parseSignedDecimal(line, 0, 13, 2);
        assertThat(result).isEqualByComparingTo("1000.00");
    }

    @Test
    void parseSignedDecimal_negativeWithLeadingSign() {
        // -000000252500 → -2525.00
        String line = "-000000252500";
        BigDecimal result = FixedWidthParser.parseSignedDecimal(line, 0, 13, 2);
        assertThat(result).isEqualByComparingTo("-2525.00");
    }

    @Test
    void parseSignedDecimal_zeroValue() {
        String line = "+000000000000";
        BigDecimal result = FixedWidthParser.parseSignedDecimal(line, 0, 13, 2);
        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void parseSignedDecimal_overpunchPositive() {
        // 000000100{  → last char '{' = 0+, so 0000001000 → 10.00
        String line = "000000100{";
        BigDecimal result = FixedWidthParser.parseSignedDecimal(line, 0, 10, 2);
        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void parseSignedDecimal_overpunchNegative() {
        // 000000100}  → last char '}' = 0-, so -0000001000 → -10.00
        String line = "000000100}";
        BigDecimal result = FixedWidthParser.parseSignedDecimal(line, 0, 10, 2);
        assertThat(result).isEqualByComparingTo("-10.00");
    }

    @Test
    void formatAlpha_rightPads() {
        assertThat(FixedWidthParser.formatAlpha("AB", 5)).isEqualTo("AB   ");
    }

    @Test
    void formatAlpha_truncatesIfTooLong() {
        assertThat(FixedWidthParser.formatAlpha("ABCDEF", 3)).isEqualTo("ABC");
    }

    @Test
    void formatAlpha_nullValue() {
        assertThat(FixedWidthParser.formatAlpha(null, 4)).isEqualTo("    ");
    }

    @Test
    void formatNumeric_leftPadsWithZeros() {
        assertThat(FixedWidthParser.formatNumeric("123", 11)).isEqualTo("00000000123");
    }

    @Test
    void formatNumeric_nullValue() {
        assertThat(FixedWidthParser.formatNumeric(null, 5)).isEqualTo("00000");
    }

    @Test
    void formatSignedDecimal_positiveValue() {
        String result = FixedWidthParser.formatSignedDecimal(new BigDecimal("1000.50"), 10, 2);
        assertThat(result).isEqualTo("+000000100050");
    }

    @Test
    void formatSignedDecimal_negativeValue() {
        String result = FixedWidthParser.formatSignedDecimal(new BigDecimal("-2525.00"), 10, 2);
        assertThat(result).isEqualTo("-000000252500");
    }

    @Test
    void formatSignedDecimal_zeroValue() {
        String result = FixedWidthParser.formatSignedDecimal(BigDecimal.ZERO, 10, 2);
        assertThat(result).isEqualTo("+000000000000");
    }

    @Test
    void formatSignedDecimal_nullValue() {
        String result = FixedWidthParser.formatSignedDecimal(null, 10, 2);
        assertThat(result).isEqualTo("+000000000000");
    }

    @Test
    void filler_createsSpaces() {
        assertThat(FixedWidthParser.filler(5)).isEqualTo("     ");
        assertThat(FixedWidthParser.filler(0)).isEmpty();
    }

    @Test
    void roundTrip_signedDecimal() {
        BigDecimal original = new BigDecimal("12345.67");
        String formatted = FixedWidthParser.formatSignedDecimal(original, 10, 2);
        BigDecimal parsed = FixedWidthParser.parseSignedDecimal(formatted, 0, formatted.length(), 2);
        assertThat(parsed).isEqualByComparingTo(original);
    }
}
