package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountFileReader parsing of fixed-width COBOL records.
 */
class AccountFileReaderTest {

    @Test
    void parseFirstRecord() {
        String line = "00000000001Y00000001940{00000020200{00000010200{" +
                "2014-11-202025-05-202025-05-2000000000000{00000000000{" +
                "A000000000";
        // Pad to 300 chars (the parser pads internally)
        AccountRecord rec = AccountFileReader.parseLine(line);

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
        // ACCT-ADDR-ZIP [102:112] = "A000000000", ACCT-GROUP-ID [112:122] = spaces
        assertEquals("A000000000", rec.addrZip());
        assertEquals("", rec.groupId());
    }

    @Test
    void parseSecondRecord() {
        String line = "00000000002Y00000001580{00000061300{00000054480{" +
                "2013-06-192024-08-112024-08-1100000000000{00000000000{" +
                "A000000000";
        AccountRecord rec = AccountFileReader.parseLine(line);

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
        assertEquals("A000000000", rec.addrZip());
        assertEquals("", rec.groupId());
    }

    @Test
    void formattedAcctId() {
        AccountRecord rec = AccountFileReader.parseLine(
                "00000000001Y00000001940{00000020200{00000010200{" +
                        "2014-11-202025-05-202025-05-2000000000000{00000000000{" +
                        "A000000000");
        assertEquals("00000000001", rec.formattedAcctId());
    }
}
