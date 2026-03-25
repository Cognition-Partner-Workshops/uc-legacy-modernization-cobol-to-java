package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileReader} — fixed-width account data parsing.
 *
 * Uses actual records from acctdata.txt to verify that the parser produces
 * identical results to the COBOL READ into ACCOUNT-RECORD.
 */
class AccountFileReaderTest {

    // First record from acctdata.txt (300 bytes padded with spaces):
    // 00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000...
    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{" +
            "2014-11-202025-05-202025-05-20" +
            "00000000000{00000000000{" +
            "A000000000";

    // Second record:
    // 00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000
    private static final String RECORD_2 =
            "00000000002Y00000001580{00000061300{00000054480{" +
            "2013-06-192024-08-112024-08-11" +
            "00000000000{00000000000{" +
            "A000000000";

    @Test
    @DisplayName("Parse account 1 — all fields match expected COBOL values")
    void shouldParseFirstRecord() {
        var record = AccountFileReader.parseLine(RECORD_1);

        assertEquals(1L, record.acctId());
        assertEquals("Y", record.activeStatus());
        assertEquals(new BigDecimal("194.00"), record.currentBalance());
        assertEquals(new BigDecimal("2020.00"), record.creditLimit());
        assertEquals(new BigDecimal("1020.00"), record.cashCreditLimit());
        assertEquals("2014-11-20", record.openDate());
        assertEquals("2025-05-20", record.expirationDate());
        assertEquals("2025-05-20", record.reissueDate());
        assertEquals(new BigDecimal("0.00"), record.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), record.currentCycleDebit());
        assertEquals("A000000000", record.addressZip());
        assertEquals("", record.groupId());
    }

    @Test
    @DisplayName("Parse account 2 — validates second record parsing")
    void shouldParseSecondRecord() {
        var record = AccountFileReader.parseLine(RECORD_2);

        assertEquals(2L, record.acctId());
        assertEquals("Y", record.activeStatus());
        assertEquals(new BigDecimal("158.00"), record.currentBalance());
        assertEquals(new BigDecimal("6130.00"), record.creditLimit());
        assertEquals(new BigDecimal("5448.00"), record.cashCreditLimit());
        assertEquals("2013-06-19", record.openDate());
        assertEquals("2024-08-11", record.expirationDate());
        assertEquals("2024-08-11", record.reissueDate());
        assertEquals(new BigDecimal("0.00"), record.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), record.currentCycleDebit());
        assertEquals("A000000000", record.addressZip());
        assertEquals("", record.groupId());
    }

    @Test
    @DisplayName("Short lines are padded to 300 characters")
    void shouldPadShortLines() {
        // Minimal record (just the fields up to group ID = 122 chars)
        var record = AccountFileReader.parseLine(RECORD_1);
        assertNotNull(record);
        assertEquals(1L, record.acctId());
    }
}
