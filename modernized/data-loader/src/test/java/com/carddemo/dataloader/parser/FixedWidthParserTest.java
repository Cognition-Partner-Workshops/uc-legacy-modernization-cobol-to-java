package com.carddemo.dataloader.parser;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWidthParserTest {

    @Test
    void extractString_shouldExtractCorrectSubstring() {
        String line = "00000000001Y00000001940{";
        assertThat(FixedWidthParser.extractString(line, 0, 11)).isEqualTo("00000000001");
        assertThat(FixedWidthParser.extractString(line, 11, 1)).isEqualTo("Y");
    }

    @Test
    void extractString_shouldTrimWhitespace() {
        String line = "John                     Doe";
        assertThat(FixedWidthParser.extractString(line, 0, 25)).isEqualTo("John");
    }

    @Test
    void extractString_shouldHandleNullLine() {
        assertThat(FixedWidthParser.extractString(null, 0, 5)).isEmpty();
    }

    @Test
    void extractString_shouldHandleStartBeyondLength() {
        assertThat(FixedWidthParser.extractString("abc", 10, 5)).isEmpty();
    }

    @Test
    void extractSignedDecimal_withPositiveOverpunch() {
        // { represents +0 in EBCDIC overpunch
        // "00000001940{" = 000000019400 / 100 = 194.00
        String line = "00000000001Y00000001940{";
        BigDecimal result = FixedWidthParser.extractSignedDecimal(line, 12, 12, 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("194.00"));
    }

    @Test
    void extractSignedDecimal_withNegativeOverpunch() {
        // } represents -0 in EBCDIC overpunch
        String raw = "0000000100}";
        BigDecimal result = FixedWidthParser.extractSignedDecimal(raw, 0, 11, 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-10.00"));
    }

    @Test
    void extractSignedDecimal_withLetterOverpunch() {
        // J = -1 in EBCDIC overpunch
        String raw = "0000000100J";
        BigDecimal result = FixedWidthParser.extractSignedDecimal(raw, 0, 11, 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-10.01"));
    }

    @Test
    void extractSignedDecimal_withEmptyString() {
        assertThat(FixedWidthParser.extractSignedDecimal("", 0, 10, 2)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void extractInt_shouldParseInteger() {
        String line = "050";
        assertThat(FixedWidthParser.extractInt(line, 0, 3)).isEqualTo(50);
    }

    @Test
    void extractInt_shouldReturnZeroForNonNumeric() {
        assertThat(FixedWidthParser.extractInt("abc", 0, 3)).isZero();
    }

    @Test
    void extractAccountRecord_fullLine() {
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000";
        assertThat(FixedWidthParser.extractString(line, 0, 11)).isEqualTo("00000000001");
        assertThat(FixedWidthParser.extractString(line, 11, 1)).isEqualTo("Y");
        assertThat(FixedWidthParser.extractSignedDecimal(line, 12, 12, 2)).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(FixedWidthParser.extractSignedDecimal(line, 24, 12, 2)).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(FixedWidthParser.extractString(line, 48, 10)).isEqualTo("2014-11-20");
    }

    @Test
    void extractCustomerRecord() {
        String line = "000000001Immanuel                 Madeline                 Kessler                  ";
        assertThat(FixedWidthParser.extractString(line, 0, 9)).isEqualTo("000000001");
        assertThat(FixedWidthParser.extractString(line, 9, 25)).isEqualTo("Immanuel");
        assertThat(FixedWidthParser.extractString(line, 34, 25)).isEqualTo("Madeline");
        assertThat(FixedWidthParser.extractString(line, 59, 25)).isEqualTo("Kessler");
    }

    @Test
    void extractCardRecord() {
        String line = "050002445376574000000000050747Aniya Von                                         2023-03-09Y";
        assertThat(FixedWidthParser.extractString(line, 0, 16)).isEqualTo("0500024453765740");
        assertThat(FixedWidthParser.extractString(line, 16, 11)).isEqualTo("00000000050");
        assertThat(FixedWidthParser.extractInt(line, 27, 3)).isEqualTo(747);
        assertThat(FixedWidthParser.extractString(line, 30, 50)).isEqualTo("Aniya Von");
        assertThat(FixedWidthParser.extractString(line, 80, 10)).isEqualTo("2023-03-09");
        assertThat(FixedWidthParser.extractString(line, 90, 1)).isEqualTo("Y");
    }
}
