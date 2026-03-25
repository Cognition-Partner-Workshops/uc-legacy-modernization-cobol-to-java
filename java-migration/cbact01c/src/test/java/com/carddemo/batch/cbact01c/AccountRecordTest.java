package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AccountRecord#parse(String)} — verifies that the
 * COBOL fixed-width, zoned-decimal account data is parsed correctly.
 */
class AccountRecordTest {

    /**
     * First record from the sample data file.
     * <pre>
     * 00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20
     * 00000000000{00000000000{A000000000  (+ 178 filler spaces)
     * </pre>
     */
    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
          + "00000000000{00000000000{A000000000"
          + " ".repeat(178);

    @Test
    void parse_acctId() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals(1L, rec.acctId());
    }

    @Test
    void parse_activeStatus() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals("Y", rec.acctActiveStatus());
    }

    @Test
    void parse_currBal() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        // 00000001940{ → 000000019400 → 0000000194.00
        assertEquals(new BigDecimal("0000000194.00"), rec.acctCurrBal());
    }

    @Test
    void parse_creditLimit() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        // 00000020200{ → 000000202000 → 0000002020.00
        assertEquals(new BigDecimal("0000002020.00"), rec.acctCreditLimit());
    }

    @Test
    void parse_cashCreditLimit() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals(new BigDecimal("0000001020.00"), rec.acctCashCreditLimit());
    }

    @Test
    void parse_openDate() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals("2014-11-20", rec.acctOpenDate());
    }

    @Test
    void parse_expirationDate() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals("2025-05-20", rec.acctExpirationDate());
    }

    @Test
    void parse_reissueDate() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals("2025-05-20", rec.acctReissueDate());
    }

    @Test
    void parse_cycCredit() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals(new BigDecimal("0000000000.00"), rec.acctCurrCycCredit());
    }

    @Test
    void parse_cycDebit() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals(new BigDecimal("0000000000.00"), rec.acctCurrCycDebit());
    }

    @Test
    void parse_addrZip() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        assertEquals("A000000000", rec.acctAddrZip());
    }

    @Test
    void parse_groupId() {
        AccountRecord rec = AccountRecord.parse(RECORD_1);
        // GROUP-ID is spaces in the sample data (trimmed to empty)
        assertEquals("", rec.acctGroupId());
    }

    // ------------------------------------------------------------------
    // Second record — exercises different numeric values
    // ------------------------------------------------------------------

    private static final String RECORD_2 =
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-11"
          + "00000000000{00000000000{A000000000"
          + " ".repeat(178);

    @Test
    void parse_record2_acctId() {
        AccountRecord rec = AccountRecord.parse(RECORD_2);
        assertEquals(2L, rec.acctId());
    }

    @Test
    void parse_record2_currBal() {
        AccountRecord rec = AccountRecord.parse(RECORD_2);
        assertEquals(new BigDecimal("0000000158.00"), rec.acctCurrBal());
    }

    @Test
    void parse_record2_creditLimit() {
        AccountRecord rec = AccountRecord.parse(RECORD_2);
        assertEquals(new BigDecimal("0000006130.00"), rec.acctCreditLimit());
    }

    @Test
    void parse_record2_openDate() {
        AccountRecord rec = AccountRecord.parse(RECORD_2);
        assertEquals("2013-06-19", rec.acctOpenDate());
    }

    // ------------------------------------------------------------------
    // Zoned-decimal sign overpunch characters
    // ------------------------------------------------------------------

    @Test
    void parseSignedDecimal_positiveBrace() {
        // '{' = +0
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001940{", 0, 12);
        assertEquals(new BigDecimal("0000000194.00"), result);
    }

    @Test
    void parseSignedDecimal_negativeBrace() {
        // '}' = -0  (effectively zero)
        BigDecimal result = AccountRecord.parseSignedDecimal("00000000000}", 0, 12);
        assertEquals(new BigDecimal("0000000000.00"), result.abs());
    }

    @Test
    void parseSignedDecimal_positiveLetterA() {
        // 'A' = +1 → last digit becomes 1
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001000A", 0, 12);
        assertEquals(new BigDecimal("0000000100.01"), result);
    }

    @Test
    void parseSignedDecimal_negativeLetterJ() {
        // 'J' = -1 → last digit becomes 1, negative sign
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001000J", 0, 12);
        assertEquals(new BigDecimal("-0000000100.01"), result);
    }

    @Test
    void parseSignedDecimal_negativeLetterR() {
        // 'R' = -9 → last digit becomes 9, negative sign
        BigDecimal result = AccountRecord.parseSignedDecimal("00000001000R", 0, 12);
        assertEquals(new BigDecimal("-0000000100.09"), result);
    }

    @Test
    void parseSignedDecimal_plainDigit() {
        BigDecimal result = AccountRecord.parseSignedDecimal("000000019405", 0, 12);
        assertEquals(new BigDecimal("0000000194.05"), result);
    }
}
