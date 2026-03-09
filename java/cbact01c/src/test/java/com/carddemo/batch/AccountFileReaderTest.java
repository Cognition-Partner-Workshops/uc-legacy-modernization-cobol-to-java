package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that the AccountFileReader correctly parses the COBOL
 * zoned-decimal account data from the CardDemo sample files.
 */
class AccountFileReaderTest {

    /**
     * Verifies parsing of the first record from the sample data:
     * {@code 00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000}
     */
    @Test
    void testParseFirstRecord() {
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000"
                + " ".repeat(178);

        AccountRecord rec = AccountFileReader.parseLine(line);

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
        assertEquals("A000000000", rec.acctAddrZip());
        assertEquals("          ", rec.acctGroupId());
    }

    /**
     * Verifies parsing of the second sample record:
     * {@code 00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000}
     */
    @Test
    void testParseSecondRecord() {
        String line = "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000"
                + " ".repeat(178);

        AccountRecord rec = AccountFileReader.parseLine(line);

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
        assertEquals("A000000000", rec.acctAddrZip());
        assertEquals("          ", rec.acctGroupId());
    }

    /**
     * Tests reading all records from the sample file via classpath resource.
     */
    @Test
    void testReadAllFromSampleFile() throws Exception {
        var path = java.nio.file.Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());

        try (AccountFileReader reader = new AccountFileReader(path)) {
            List<AccountRecord> records = reader.readAll();
            assertEquals(3, records.size());

            // Spot-check the third record
            AccountRecord third = records.get(2);
            assertEquals(3L, third.acctId());
            assertEquals(new BigDecimal("147.00"), third.acctCurrBal());
            assertEquals(new BigDecimal("4909.00"), third.acctCreditLimit());
            assertEquals("2024-01-10", third.acctReissueDate());
        }
    }

    /**
     * Verifies that short lines (trimmed trailing spaces) are correctly padded.
     */
    @Test
    void testShortLinePadding() {
        // Minimal line — just enough data, no trailing filler
        String shortLine = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000";
        AccountRecord rec = AccountFileReader.parseLine(shortLine);
        assertNotNull(rec);
        assertEquals(1L, rec.acctId());
    }
}
