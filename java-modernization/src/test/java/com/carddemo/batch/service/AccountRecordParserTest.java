package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link AccountRecordParser}, verifying correct parsing of
 * fixed-width COBOL account records from the CVACT01Y copybook layout.
 */
class AccountRecordParserTest {

    // First record from app/data/ASCII/acctdata.txt
    private static final String SAMPLE_LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{" +
            "2014-11-202025-05-202025-05-2000000000000{00000000000{" +
            "A000000000                                              " +
            "                                                        " +
            "                                                        " +
            "                        ";

    // Second record from app/data/ASCII/acctdata.txt
    private static final String SAMPLE_LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{" +
            "2013-06-192024-08-112024-08-1100000000000{00000000000{" +
            "A000000000                                              " +
            "                                                        " +
            "                                                        " +
            "                        ";

    @Test
    void parsesFirstSampleRecord() {
        AccountRecord rec = AccountRecordParser.parse(SAMPLE_LINE_1);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("194.00"), rec.currentBalance());
        assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
        assertEquals("2014-11-20", rec.openDate());
        assertEquals("2025-05-20", rec.expirationDate());
        assertEquals("2025-05-20", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleDebit());
    }

    @Test
    void parsesSecondSampleRecord() {
        AccountRecord rec = AccountRecordParser.parse(SAMPLE_LINE_2);

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("158.00"), rec.currentBalance());
        assertEquals(new BigDecimal("6130.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.cashCreditLimit());
        assertEquals("2013-06-19", rec.openDate());
        assertEquals("2024-08-11", rec.expirationDate());
        assertEquals("2024-08-11", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleDebit());
    }

    @Test
    void formattedAcctId_matchesCobolPic9_11() {
        AccountRecord rec = AccountRecordParser.parse(SAMPLE_LINE_1);
        assertEquals("00000000001", rec.formattedAcctId());
    }

    @Test
    void tooShortLine_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountRecordParser.parse("too short"));
    }

    @Test
    void parsesMinimalLengthRecord() {
        // Build a minimal 122-char line
        String minimal =
                "00000000099X" +                     // ID(11) + status(1)
                "00000000500{" +                     // curr bal (12)
                "00000010000{" +                     // credit limit (12)
                "00000005000{" +                     // cash credit limit (12)
                "2020-01-01" +                       // open date (10)
                "2025-12-31" +                       // exp date (10)
                "2025-06-15" +                       // reissue date (10)
                "00000000100{" +                     // cyc credit (12)
                "00000000200{" +                     // cyc debit (12)
                "1234567890" +                       // addr zip (10)
                "GRP0000001";                        // group id (10)

        AccountRecord rec = AccountRecordParser.parse(minimal);

        assertEquals(99L, rec.acctId());
        assertEquals("X", rec.activeStatus());
        assertEquals(new BigDecimal("50.00"), rec.currentBalance());
        assertEquals(new BigDecimal("1000.00"), rec.creditLimit());
        assertEquals(new BigDecimal("500.00"), rec.cashCreditLimit());
        assertEquals("2020-01-01", rec.openDate());
        assertEquals("2025-12-31", rec.expirationDate());
        assertEquals("2025-06-15", rec.reissueDate());
        assertEquals(new BigDecimal("10.00"), rec.currentCycleCredit());
        assertEquals(new BigDecimal("20.00"), rec.currentCycleDebit());
        assertEquals("1234567890", rec.addressZip());
        assertEquals("GRP0000001", rec.groupId());
    }
}
