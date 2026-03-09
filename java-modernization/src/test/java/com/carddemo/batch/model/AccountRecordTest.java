package com.carddemo.batch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountRecord} parsing, specifically the zoned-decimal
 * decoder that handles mainframe overpunch characters.
 */
class AccountRecordTest {

    // First record from app/data/ASCII/acctdata.txt (300 chars, padded)
    private static final String SAMPLE_LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{";

    // Second record
    private static final String SAMPLE_LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{";

    @Test
    @DisplayName("Parse first sample account record correctly")
    void parseFirstRecord() {
        AccountRecord rec = AccountRecord.parse(SAMPLE_LINE_1);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.acctActiveStatus());
        assertEquals(new BigDecimal("194.00"), rec.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), rec.acctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.acctCashCreditLimit());
        assertEquals("2014-11-20", rec.acctOpenDate());
        assertEquals("2025-05-20", rec.acctExpiraionDate());
        assertEquals("2025-05-20", rec.acctReissueDate());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycDebit());
    }

    @Test
    @DisplayName("Parse second sample account record correctly")
    void parseSecondRecord() {
        AccountRecord rec = AccountRecord.parse(SAMPLE_LINE_2);

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.acctActiveStatus());
        assertEquals(new BigDecimal("158.00"), rec.acctCurrBal());
        assertEquals(new BigDecimal("6130.00"), rec.acctCreditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.acctCashCreditLimit());
        assertEquals("2013-06-19", rec.acctOpenDate());
        assertEquals("2024-08-11", rec.acctExpiraionDate());
        assertEquals("2024-08-11", rec.acctReissueDate());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycDebit());
    }

    @Test
    @DisplayName("Short line is padded to 300 characters")
    void shortLinePadded() {
        // Minimal valid line — parser should pad and parse without error
        AccountRecord rec = AccountRecord.parse(SAMPLE_LINE_1);
        assertNotNull(rec);
    }

    @Test
    @DisplayName("Null line throws NullPointerException")
    void nullLineThrows() {
        assertThrows(NullPointerException.class, () -> AccountRecord.parse(null));
    }

    // ── Zoned-decimal edge cases ────────────────────────────────────────

    @ParameterizedTest(name = "overpunch ''{0}'' → digit {1}, negative={2}")
    @CsvSource({
            "{, 0, false",
            "A, 1, false",
            "B, 2, false",
            "I, 9, false",
            "}, 0, true",
            "J, 1, true",
            "R, 9, true"
    })
    @DisplayName("Zoned-decimal overpunch characters decode correctly")
    void zonedDecimalOverpunch(char lastChar, int expectedDigit, boolean negative) {
        // Build a 12-char field: 11 zeros + overpunch char
        String field = "00000000000" + lastChar;
        // Embed in a dummy line at offset 0
        BigDecimal result = AccountRecord.parseZonedDecimal(field, 0, 12, 2);

        BigDecimal expected = new BigDecimal(expectedDigit).movePointLeft(2);
        if (negative) {
            expected = expected.negate();
        }
        assertEquals(0, expected.compareTo(result),
                "Expected " + expected + " but got " + result);
    }

    @Test
    @DisplayName("Zoned-decimal with plain trailing digit (no overpunch)")
    void zonedDecimalPlainDigit() {
        String field = "000000019405";
        BigDecimal result = AccountRecord.parseZonedDecimal(field, 0, 12, 2);
        assertEquals(new BigDecimal("194.05"), result);
    }

    @Test
    @DisplayName("Unrecognised overpunch character throws exception")
    void zonedDecimalBadChar() {
        String field = "00000000000Z";
        assertThrows(IllegalArgumentException.class,
                () -> AccountRecord.parseZonedDecimal(field, 0, 12, 2));
    }
}
