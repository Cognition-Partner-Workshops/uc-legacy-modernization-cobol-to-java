package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.io.AccountFileReader;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileReader}, verifying correct parsing of the
 * COBOL fixed-width account data format with zoned-decimal fields.
 */
class AccountFileReaderTest {

    /** First line from app/data/ASCII/acctdata.txt (trimmed trailing spaces). */
    private static final String LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{"
                    + "2014-11-202025-05-202025-05-20"
                    + "00000000000{00000000000{A000000000";

    /** Second line from the data file. */
    private static final String LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{"
                    + "2013-06-192024-08-112024-08-11"
                    + "00000000000{00000000000{A000000000";

    @Test
    void parseLine_shouldParseFirstRecordCorrectly() {
        AccountRecord rec = AccountFileReader.parseLine(padTo300(LINE_1));

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
    void parseLine_shouldParseSecondRecordCorrectly() {
        AccountRecord rec = AccountFileReader.parseLine(padTo300(LINE_2));

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
    void readAll_shouldParseMultipleRecords(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("acctdata.txt");
        Files.writeString(file, LINE_1 + " ".repeat(300 - LINE_1.length()) + "\n"
                + LINE_2 + " ".repeat(300 - LINE_2.length()) + "\n");

        List<AccountRecord> records = AccountFileReader.readAll(file);

        assertEquals(2, records.size());
        assertEquals(1L, records.get(0).acctId());
        assertEquals(2L, records.get(1).acctId());
    }

    @Test
    void readAll_shouldSkipBlankLines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("acctdata.txt");
        Files.writeString(file, LINE_1 + " ".repeat(300 - LINE_1.length()) + "\n\n\n");

        List<AccountRecord> records = AccountFileReader.readAll(file);
        assertEquals(1, records.size());
    }

    @Test
    void readAll_shouldHandleTrailingNewline(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("acctdata.txt");
        Files.writeString(file, LINE_1 + " ".repeat(300 - LINE_1.length()) + "\n");

        List<AccountRecord> records = AccountFileReader.readAll(file);
        assertEquals(1, records.size());
    }

    private static String padTo300(String s) {
        if (s.length() >= 300) {
            return s;
        }
        return s + " ".repeat(300 - s.length());
    }
}
