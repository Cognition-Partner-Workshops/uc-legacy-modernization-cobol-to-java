package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountRecordTest {

    // Matches real acctdata.txt layout: ADDR-ZIP="A000000000", GROUP-ID=spaces
    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{"
                    + "2014-11-20" + "2025-05-20" + "2025-05-20"
                    + "00000000000{00000000000{"
                    + "A000000000"  // ADDR-ZIP (10 chars)
                    + "          "  // GROUP-ID (10 spaces)
                    + " ".repeat(178); // FILLER

    @Test
    void parseFirstRecord() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("194.00"), rec.currBal());
        assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
        assertEquals("2014-11-20", rec.openDate());
        assertEquals("2025-05-20", rec.expirationDate());
        assertEquals("2025-05-20", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.currCycDebit());
        assertEquals("A000000000", rec.addrZip());
        assertEquals("", rec.groupId());
    }

    @Test
    void parseRecordWithNonBlankGroupId() {
        String record = "00000000042Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{00000000000{"
                + "1234567890"  // ADDR-ZIP
                + "GRP0000001"  // GROUP-ID
                + " ".repeat(178);

        AccountRecord rec = AccountRecord.parse(record);

        assertEquals(42L, rec.acctId());
        assertEquals("1234567890", rec.addrZip());
        assertEquals("GRP0000001", rec.groupId());
    }

    @Test
    void parseSecondRecordFromFile() {
        String line = "00000000002Y00000001580{00000061300{00000054480{"
                + "2013-06-19" + "2024-08-11" + "2024-08-11"
                + "00000000000{00000000000{"
                + "A000000000"
                + "          "
                + " ".repeat(178);

        AccountRecord rec = AccountRecord.parse(line);

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("158.00"), rec.currBal());
        assertEquals(new BigDecimal("6130.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.cashCreditLimit());
        assertEquals("2013-06-19", rec.openDate());
        assertEquals("2024-08-11", rec.expirationDate());
        assertEquals("2024-08-11", rec.reissueDate());
    }

    @ParameterizedTest
    @CsvSource({
            // field=00000000100+lastChar → digits 00000000100X → value 10.0X
            "{, 0, false",
            "A, 1, false",
            "I, 9, false",
            "}, 0, true",
            "J, 1, true",
            "R, 9, true"
    })
    void parseSignedDecimalOverpunchedSign(String lastChar, int expectedDigit, boolean negative) {
        String field = "00000000100" + lastChar;
        BigDecimal result = AccountRecord.parseSignedDecimal(field, 0, 12);

        BigDecimal expected = new BigDecimal("10.0" + expectedDigit);
        if (negative) {
            expected = expected.negate();
        }
        assertEquals(expected, result);
    }

    @Test
    void parseNegativeValue() {
        // J = -1, so 00000002525J → digits 000000025251 → V99 → -252.51
        String field = "00000002525J";
        BigDecimal result = AccountRecord.parseSignedDecimal(field, 0, 12);
        assertEquals(new BigDecimal("-252.51"), result);
    }

    @Test
    void parseZeroValue() {
        String field = "00000000000{";
        BigDecimal result = AccountRecord.parseSignedDecimal(field, 0, 12);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void parseLargePositiveValue() {
        // 99999999999I → digits 999999999999 → V99 → 9999999999.99
        String field = "99999999999I";
        BigDecimal result = AccountRecord.parseSignedDecimal(field, 0, 12);
        assertEquals(new BigDecimal("9999999999.99"), result);
    }

    @Test
    void parseShortLineIsPadded() {
        String shortLine = "00000000099Y00000000100{00000000200{00000000300{"
                + "2020-01-01" + "2025-12-31" + "2025-06-15"
                + "00000000050{00000000025{";
        AccountRecord rec = AccountRecord.parse(shortLine);
        assertNotNull(rec);
        assertEquals(99L, rec.acctId());
    }
}
