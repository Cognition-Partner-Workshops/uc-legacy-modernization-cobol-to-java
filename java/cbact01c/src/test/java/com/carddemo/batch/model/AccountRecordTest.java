package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountRecord} parsing, verifying that the Java parser
 * produces identical field values to those the COBOL program would read
 * from a VSAM KSDS account file.
 */
class AccountRecordTest {

    // First line from the sample acctdata.txt (padded to 300 chars)
    // Layout: ID(11) + status(1) + currBal(12) + creditLimit(12) + cashCreditLimit(12)
    //        + openDate(10) + expDate(10) + reissDate(10) + cycCredit(12) + cycDebit(12)
    //        + addrZip(10) + groupId(10) + filler(178) = 300
    private static final String LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
          + "00000000000{00000000000{"  // cycCredit + cycDebit
          + String.format("%-10s", "")   // addrZip (10 spaces)
          + "A000000000"               // groupId
          + " ".repeat(178);

    private static final String LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-11"
          + "00000000000{00000000000{"  // cycCredit + cycDebit
          + String.format("%-10s", "")   // addrZip (10 spaces)
          + "A000000000"               // groupId
          + " ".repeat(178);

    @Test
    void parseFirstRecord() {
        AccountRecord rec = AccountRecord.parse(LINE_1);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        // PIC S9(10)V99: 00000001940{ → digits 000000019400 → 194.00
        assertEquals(new BigDecimal("194.00"), rec.currBal());
        assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
        assertEquals("2014-11-20", rec.openDate());
        assertEquals("2025-05-20", rec.expirationDate());
        assertEquals("2025-05-20", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.currCycDebit());
        assertEquals("A000000000", rec.groupId());
    }

    @Test
    void parseSecondRecord() {
        AccountRecord rec = AccountRecord.parse(LINE_2);

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("158.00"), rec.currBal());
        assertEquals(new BigDecimal("6130.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.cashCreditLimit());
        assertEquals("2013-06-19", rec.openDate());
        assertEquals("2024-08-11", rec.expirationDate());
        assertEquals("2024-08-11", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.currCycDebit());
        assertEquals("A000000000", rec.groupId());
    }

    @Test
    void parseShortLineIsPadded() {
        // A line shorter than 300 chars should be right-padded with spaces
        String shortLine = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
                + "00000000000{00000000000{A000000000";
        AccountRecord rec = AccountRecord.parse(shortLine);
        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
    }

    // ----- Overpunch sign decoding -----

    @ParameterizedTest(name = "overpunch ''{0}'' with scale {1} → {2}")
    @CsvSource({
            "00000001940{, 2, 194.00",
            "00000020200{, 2, 2020.00",
            "00000000000{, 2, 0.00",
            "00000010250}, 2, -1025.00",
            "00000002500}, 2, -250.00",
            "00000010050A, 2, 1005.01",
    })
    void parseSignedDecimal(String raw, int scale, String expected) {
        BigDecimal result = AccountRecord.parseSignedDecimal(raw, scale);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void parseSignedDecimalNegativeJ() {
        // J = -1
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001001J", 2);
        assertEquals(new BigDecimal("-100.11"), result);
    }

    @Test
    void parseSignedDecimalPositiveI() {
        // I = +9
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001009I", 2);
        assertEquals(new BigDecimal("100.99"), result);
    }

    @Test
    void parseSignedDecimalNegativeR() {
        // R = -9
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001009R", 2);
        assertEquals(new BigDecimal("-100.99"), result);
    }

    @Test
    void parseNullOrBlankReturnsZero() {
        assertEquals(BigDecimal.ZERO, AccountRecord.parseSignedDecimal(null, 2));
        assertEquals(BigDecimal.ZERO, AccountRecord.parseSignedDecimal("   ", 2));
    }
}
