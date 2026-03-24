package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountFileReaderTest {

    @Test
    void parseSignedDecimal_positiveZero() {
        // '{' encodes +0
        BigDecimal result = AccountFileReader.parseSignedDecimal("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parseSignedDecimal_positiveDigit() {
        // 'A' encodes +1
        BigDecimal result = AccountFileReader.parseSignedDecimal("00000001940A", 2);
        assertEquals(new BigDecimal("194.01"), result);
    }

    @Test
    void parseSignedDecimal_negativeZero() {
        // '}' encodes -0
        BigDecimal result = AccountFileReader.parseSignedDecimal("00000001940}", 2);
        assertEquals(new BigDecimal("-194.00"), result);
    }

    @Test
    void parseSignedDecimal_negativeDigit() {
        // 'J' encodes -1
        BigDecimal result = AccountFileReader.parseSignedDecimal("00000001940J", 2);
        assertEquals(new BigDecimal("-194.01"), result);
    }

    @Test
    void parseSignedDecimal_allPositiveSigns() {
        assertEquals(new BigDecimal("0.00"), AccountFileReader.parseSignedDecimal("00000000000{", 2));
        assertEquals(new BigDecimal("0.01"), AccountFileReader.parseSignedDecimal("00000000000A", 2));
        assertEquals(new BigDecimal("0.02"), AccountFileReader.parseSignedDecimal("00000000000B", 2));
        assertEquals(new BigDecimal("0.03"), AccountFileReader.parseSignedDecimal("00000000000C", 2));
        assertEquals(new BigDecimal("0.04"), AccountFileReader.parseSignedDecimal("00000000000D", 2));
        assertEquals(new BigDecimal("0.05"), AccountFileReader.parseSignedDecimal("00000000000E", 2));
        assertEquals(new BigDecimal("0.06"), AccountFileReader.parseSignedDecimal("00000000000F", 2));
        assertEquals(new BigDecimal("0.07"), AccountFileReader.parseSignedDecimal("00000000000G", 2));
        assertEquals(new BigDecimal("0.08"), AccountFileReader.parseSignedDecimal("00000000000H", 2));
        assertEquals(new BigDecimal("0.09"), AccountFileReader.parseSignedDecimal("00000000000I", 2));
    }

    @Test
    void parseSignedDecimal_allNegativeSigns() {
        assertEquals(new BigDecimal("0.00"), AccountFileReader.parseSignedDecimal("00000000000}", 2).abs());
        assertEquals(new BigDecimal("-0.01"), AccountFileReader.parseSignedDecimal("00000000000J", 2));
        assertEquals(new BigDecimal("-0.02"), AccountFileReader.parseSignedDecimal("00000000000K", 2));
        assertEquals(new BigDecimal("-0.03"), AccountFileReader.parseSignedDecimal("00000000000L", 2));
        assertEquals(new BigDecimal("-0.04"), AccountFileReader.parseSignedDecimal("00000000000M", 2));
        assertEquals(new BigDecimal("-0.05"), AccountFileReader.parseSignedDecimal("00000000000N", 2));
        assertEquals(new BigDecimal("-0.06"), AccountFileReader.parseSignedDecimal("00000000000O", 2));
        assertEquals(new BigDecimal("-0.07"), AccountFileReader.parseSignedDecimal("00000000000P", 2));
        assertEquals(new BigDecimal("-0.08"), AccountFileReader.parseSignedDecimal("00000000000Q", 2));
        assertEquals(new BigDecimal("-0.09"), AccountFileReader.parseSignedDecimal("00000000000R", 2));
    }

    @Test
    void parseSignedDecimal_blankField_returnsZero() {
        assertEquals(BigDecimal.ZERO, AccountFileReader.parseSignedDecimal("            ", 2));
    }

    @Test
    void parseLine_firstRecord() {
        // Build a 300-char line matching record 1 from acctdata.txt
        String line = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "          " // ACCT-ADDR-ZIP (10 spaces)
                + "A000000000" // ACCT-GROUP-ID
                + " ".repeat(178); // FILLER

        AccountRecord rec = AccountFileReader.parseLine(line);

        assertEquals("00000000001", rec.acctId());
        assertEquals("Y", rec.acctActiveStatus());
        assertEquals(new BigDecimal("194.00"), rec.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), rec.acctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.acctCashCreditLimit());
        assertEquals("2014-11-20", rec.acctOpenDate());
        assertEquals("2025-05-20", rec.acctExpiraionDate());
        assertEquals("2025-05-20", rec.acctReissueDate());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.acctCurrCycDebit());
        assertEquals("A000000000", rec.acctGroupId());
    }

    @Test
    void readNext_readsMultipleRecords(@TempDir Path tempDir) throws IOException {
        String rec1 = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "          " + "A000000000" + " ".repeat(178);
        String rec2 = "00000000002Y00000001580{00000061300{00000054480{"
                + "2013-06-19" + "2024-08-11" + "2024-08-11"
                + "00000000000{" + "00000000000{"
                + "          " + "A000000000" + " ".repeat(178);

        Path testFile = tempDir.resolve("test-acct.txt");
        Files.writeString(testFile, rec1 + "\n" + rec2 + "\n");

        try (AccountFileReader reader = new AccountFileReader(testFile)) {
            Optional<AccountRecord> first = reader.readNext();
            assertTrue(first.isPresent());
            assertEquals("00000000001", first.get().acctId());

            Optional<AccountRecord> second = reader.readNext();
            assertTrue(second.isPresent());
            assertEquals("00000000002", second.get().acctId());

            Optional<AccountRecord> eof = reader.readNext();
            assertTrue(eof.isEmpty());
        }
    }

    @Test
    void readAll_returnsAllRecords(@TempDir Path tempDir) throws IOException {
        String rec1 = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "          " + "A000000000" + " ".repeat(178);

        Path testFile = tempDir.resolve("test-acct.txt");
        Files.writeString(testFile, rec1 + "\n");

        try (AccountFileReader reader = new AccountFileReader(testFile)) {
            List<AccountRecord> records = reader.readAll();
            assertEquals(1, records.size());
            assertEquals("00000000001", records.get(0).acctId());
        }
    }

    @Test
    void readNext_emptyFile_returnsEmpty(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("empty.txt");
        Files.writeString(testFile, "");

        try (AccountFileReader reader = new AccountFileReader(testFile)) {
            assertTrue(reader.readNext().isEmpty());
        }
    }
}
