package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the fixed-width VSAM account file is parsed correctly,
 * including zoned-decimal sign handling.
 */
class AccountFileReaderTest {

    @Test
    void readAll_sampleFile_shouldParse5Records() throws IOException {
        Path path = Path.of(
                AccountFileReaderTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());

        List<AccountRecord> records = AccountFileReader.readAll(path);

        assertEquals(5, records.size());
    }

    @Test
    void readAll_firstRecord_shouldMatchExpectedValues() throws IOException {
        Path path = Path.of(
                AccountFileReaderTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());

        List<AccountRecord> records = AccountFileReader.readAll(path);
        AccountRecord first = records.get(0);

        assertEquals(1L, first.acctId());
        assertEquals("Y", first.activeStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(first.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(first.creditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(first.cashCreditLimit()));
        assertEquals("2014-11-20", first.openDate());
        assertEquals("2025-05-20", first.expirationDate());
        assertEquals("2025-05-20", first.reissueDate());
        assertEquals(0, new BigDecimal("0.00").compareTo(first.currCycCredit()));
        assertEquals(0, new BigDecimal("0.00").compareTo(first.currCycDebit()));
    }

    @Test
    void readAll_secondRecord_shouldMatchExpectedValues() throws IOException {
        Path path = Path.of(
                AccountFileReaderTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());

        List<AccountRecord> records = AccountFileReader.readAll(path);
        AccountRecord second = records.get(1);

        assertEquals(2L, second.acctId());
        assertEquals("Y", second.activeStatus());
        assertEquals(0, new BigDecimal("158.00").compareTo(second.currBal()));
        assertEquals(0, new BigDecimal("6130.00").compareTo(second.creditLimit()));
        assertEquals(0, new BigDecimal("5448.00").compareTo(second.cashCreditLimit()));
        assertEquals("2013-06-19", second.openDate());
        assertEquals("2024-08-11", second.expirationDate());
        assertEquals("2024-08-11", second.reissueDate());
    }

    @Test
    void readAll_fifthRecord_shouldMatchExpectedValues() throws IOException {
        Path path = Path.of(
                AccountFileReaderTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());

        List<AccountRecord> records = AccountFileReader.readAll(path);
        AccountRecord fifth = records.get(4);

        assertEquals(5L, fifth.acctId());
        assertEquals("Y", fifth.activeStatus());
        assertEquals(0, new BigDecimal("345.00").compareTo(fifth.currBal()));
        assertEquals(0, new BigDecimal("3819.00").compareTo(fifth.creditLimit()));
        assertEquals(0, new BigDecimal("2430.00").compareTo(fifth.cashCreditLimit()));
        assertEquals("2012-10-03", fifth.openDate());
        assertEquals("2025-03-09", fifth.expirationDate());
        assertEquals("2025-03-09", fifth.reissueDate());
    }

    @Test
    void parseLine_shouldHandleGroupIdField() throws IOException {
        Path path = Path.of(
                AccountFileReaderTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());

        List<AccountRecord> records = AccountFileReader.readAll(path);
        assertNotNull(records.get(0).groupId());
        assertEquals(10, records.get(0).groupId().length());
    }
}
