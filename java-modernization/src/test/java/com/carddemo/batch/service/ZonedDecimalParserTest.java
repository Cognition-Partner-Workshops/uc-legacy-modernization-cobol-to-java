package com.carddemo.batch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ZonedDecimalParser}, which handles COBOL zoned-decimal
 * (USAGE IS DISPLAY) fields with ASCII overpunch characters.
 */
class ZonedDecimalParserTest {

    @Test
    void positiveZero_overpunchBrace() {
        // '{' = +0, so "00000001940{" with V99 = 194.00
        BigDecimal result = ZonedDecimalParser.parse("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @ParameterizedTest(name = "{0} with V99 = {1}")
    @CsvSource({
            "00000001940{, 194.00",
            "00000020200{, 2020.00",
            "00000010200{, 1020.00",
            "00000000000{, 0.00",
            "00000001580{, 158.00",
            "00000061300{, 6130.00",
            "00000054480{, 5448.00",
            "00000001470{, 147.00"
    })
    void positiveValues_fromSampleData(String raw, String expected) {
        assertEquals(new BigDecimal(expected), ZonedDecimalParser.parse(raw, 2));
    }

    @Test
    void positiveNonZeroLastDigit_overpunchA() {
        // 'A' = +1
        BigDecimal result = ZonedDecimalParser.parse("0000000100A", 2);
        assertEquals(new BigDecimal("10.01"), result);
    }

    @Test
    void positiveNonZeroLastDigit_overpunchI() {
        // 'I' = +9
        BigDecimal result = ZonedDecimalParser.parse("0000000100I", 2);
        assertEquals(new BigDecimal("10.09"), result);
    }

    @Test
    void negativeValue_overpunchCloseBrace() {
        // '}' = -0
        BigDecimal result = ZonedDecimalParser.parse("0000000100}", 2);
        assertEquals(new BigDecimal("-10.00"), result);
    }

    @Test
    void negativeValue_overpunchJ() {
        // 'J' = -1
        BigDecimal result = ZonedDecimalParser.parse("0000000100J", 2);
        assertEquals(new BigDecimal("-10.01"), result);
    }

    @Test
    void negativeValue_overpunchR() {
        // 'R' = -9
        BigDecimal result = ZonedDecimalParser.parse("0000000100R", 2);
        assertEquals(new BigDecimal("-10.09"), result);
    }

    @Test
    void noDecimalPlaces() {
        BigDecimal result = ZonedDecimalParser.parse("0000000100{", 0);
        assertEquals(new BigDecimal("1000"), result);
    }

    @Test
    void emptyString_returnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse("", 2));
    }

    @Test
    void nullString_returnsZero() {
        assertEquals(BigDecimal.ZERO, ZonedDecimalParser.parse(null, 2));
    }

    @Test
    void invalidOverpunch_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ZonedDecimalParser.parse("0000000100Z", 2));
    }

    @Test
    void unsignedDigit_treatedAsUnsigned() {
        // Plain digit at end (no overpunch)
        BigDecimal result = ZonedDecimalParser.parse("00000001005", 2);
        assertEquals(new BigDecimal("10.05"), result);
    }
}
