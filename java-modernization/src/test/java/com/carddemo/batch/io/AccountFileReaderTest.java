package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileReader} using real sample data from the COBOL acctdata.txt.
 */
class AccountFileReaderTest {

    @Test
    @DisplayName("parse first account record from sample data")
    void testParseFirstRecord() throws IOException {
        Path sampleFile = Path.of("src/test/resources/acctdata_sample.txt");
        try (var reader = new AccountFileReader(sampleFile)) {
            AccountRecord rec = reader.readNext();
            assertNotNull(rec);

            assertEquals("00000000001", rec.acctId());
            assertEquals("Y", rec.acctActiveStatus());
            assertEquals(new BigDecimal("194.00"), rec.acctCurrBal());
            assertEquals(new BigDecimal("2020.00"), rec.acctCreditLimit());
            assertEquals(new BigDecimal("1020.00"), rec.acctCashCreditLimit());
            assertEquals("2014-11-20", rec.acctOpenDate());
            assertEquals("2025-05-20", rec.acctExpirationDate());
            assertEquals("2025-05-20", rec.acctReissueDate());
            assertEquals(new BigDecimal("0.00"), rec.acctCurrCycCredit());
            assertEquals(new BigDecimal("0.00"), rec.acctCurrCycDebit());
            assertEquals("          ", rec.acctGroupId()); // 10 spaces in actual data
        }
    }

    @Test
    @DisplayName("parse all five sample records")
    void testParseAllSampleRecords() throws IOException {
        Path sampleFile = Path.of("src/test/resources/acctdata_sample.txt");
        try (var reader = new AccountFileReader(sampleFile)) {
            List<AccountRecord> records = reader.readAll();
            assertEquals(5, records.size());

            // Verify account IDs are sequential
            for (int i = 0; i < 5; i++) {
                String expectedId = String.format("%011d", i + 1);
                assertEquals(expectedId, records.get(i).acctId());
            }

            // Spot-check account 3
            AccountRecord rec3 = records.get(2);
            assertEquals("00000000003", rec3.acctId());
            assertEquals(new BigDecimal("147.00"), rec3.acctCurrBal());
            assertEquals(new BigDecimal("4909.00"), rec3.acctCreditLimit());
            assertEquals(new BigDecimal("538.00"), rec3.acctCashCreditLimit());
            assertEquals("2013-08-23", rec3.acctOpenDate());

            // Spot-check account 5
            AccountRecord rec5 = records.get(4);
            assertEquals("00000000005", rec5.acctId());
            assertEquals(new BigDecimal("345.00"), rec5.acctCurrBal());
            assertEquals(new BigDecimal("3819.00"), rec5.acctCreditLimit());
        }
    }

    @Test
    @DisplayName("readNext returns null at end of file")
    void testEofReturnsNull(@TempDir Path tmpDir) throws IOException {
        Path emptyFile = tmpDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");
        try (var reader = new AccountFileReader(emptyFile)) {
            assertNull(reader.readNext());
        }
    }

    @Test
    @DisplayName("all records have Y active status in sample data")
    void testAllRecordsActive() throws IOException {
        Path sampleFile = Path.of("src/test/resources/acctdata_sample.txt");
        try (var reader = new AccountFileReader(sampleFile)) {
            List<AccountRecord> records = reader.readAll();
            records.forEach(rec ->
                    assertEquals("Y", rec.acctActiveStatus(),
                            "Account " + rec.acctId() + " should be active"));
        }
    }

    @Test
    @DisplayName("all debit values are zero in sample data")
    void testAllDebitsZero() throws IOException {
        Path sampleFile = Path.of("src/test/resources/acctdata_sample.txt");
        try (var reader = new AccountFileReader(sampleFile)) {
            List<AccountRecord> records = reader.readAll();
            records.forEach(rec ->
                    assertEquals(0, rec.acctCurrCycDebit().compareTo(BigDecimal.ZERO),
                            "Account " + rec.acctId() + " debit should be zero"));
        }
    }
}
