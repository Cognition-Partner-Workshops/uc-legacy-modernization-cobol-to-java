package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountFileReader — parsing the COBOL account master file.
 * Uses the 3-record sample extracted from the real acctdata.txt.
 */
class AccountFileReaderTest {

    private static final Path SAMPLE_FILE = Path.of("src/test/resources/acctdata-sample.txt");

    @Test
    void readsSampleFileCorrectRecordCount() throws IOException {
        try (AccountFileReader reader = new AccountFileReader(SAMPLE_FILE)) {
            List<AccountRecord> records = reader.readAll();
            assertEquals(3, records.size());
        }
    }

    @Test
    void firstRecordParsedCorrectly() throws IOException {
        try (AccountFileReader reader = new AccountFileReader(SAMPLE_FILE)) {
            List<AccountRecord> records = reader.readAll();
            AccountRecord first = records.get(0);

            assertEquals(1L, first.acctId());
            assertEquals("Y", first.activeStatus());
            assertEquals(new BigDecimal("194.00"), first.currBal());
            assertEquals(new BigDecimal("2020.00"), first.creditLimit());
            assertEquals(new BigDecimal("1020.00"), first.cashCreditLimit());
            assertEquals("2014-11-20", first.openDate());
            assertEquals("2025-05-20", first.expirationDate());
            assertEquals("2025-05-20", first.reissueDate());
            assertEquals(new BigDecimal("0.00"), first.currCycCredit());
            assertEquals(new BigDecimal("0.00"), first.currCycDebit());
            assertEquals("A000000000", first.addrZip());
            assertEquals("          ", first.groupId());
        }
    }

    @Test
    void secondRecordParsedCorrectly() throws IOException {
        try (AccountFileReader reader = new AccountFileReader(SAMPLE_FILE)) {
            List<AccountRecord> records = reader.readAll();
            AccountRecord second = records.get(1);

            assertEquals(2L, second.acctId());
            assertEquals("Y", second.activeStatus());
            assertEquals(new BigDecimal("158.00"), second.currBal());
            assertEquals(new BigDecimal("6130.00"), second.creditLimit());
            assertEquals(new BigDecimal("5448.00"), second.cashCreditLimit());
            assertEquals("2013-06-19", second.openDate());
            assertEquals("2024-08-11", second.expirationDate());
            assertEquals("2024-08-11", second.reissueDate());
            assertEquals(new BigDecimal("0.00"), second.currCycCredit());
            assertEquals(new BigDecimal("0.00"), second.currCycDebit());
            assertEquals("A000000000", second.addrZip());
            assertEquals("          ", second.groupId());
        }
    }

    @Test
    void thirdRecordParsedCorrectly() throws IOException {
        try (AccountFileReader reader = new AccountFileReader(SAMPLE_FILE)) {
            List<AccountRecord> records = reader.readAll();
            AccountRecord third = records.get(2);

            assertEquals(3L, third.acctId());
            assertEquals("Y", third.activeStatus());
            assertEquals(new BigDecimal("147.00"), third.currBal());
            assertEquals(new BigDecimal("4909.00"), third.creditLimit());
            assertEquals(new BigDecimal("538.00"), third.cashCreditLimit());
            assertEquals("2013-08-23", third.openDate());
            assertEquals("2024-01-10", third.expirationDate());
            assertEquals("2024-01-10", third.reissueDate());
            assertEquals(new BigDecimal("0.00"), third.currCycCredit());
            assertEquals(new BigDecimal("0.00"), third.currCycDebit());
            assertEquals("A000000000", third.addrZip());
            assertEquals("          ", third.groupId());
        }
    }
}
