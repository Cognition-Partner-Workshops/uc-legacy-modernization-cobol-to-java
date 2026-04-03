package com.carddemo.batch.job;

import com.carddemo.batch.model.CardRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardFileProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void executeReadsCardRecords() throws IOException {
        Path inputFile = tempDir.resolve("carddata.txt");
        // cardNum(16) + acctId(11) + cvv(3) + embossedName(50) + expDate(10) + status(1)
        String line = "4111111111111111" + "00000000001" + "123" +
                String.format("%-50s", "JOHN DOE") +
                "2025-12-31" + "Y";
        Files.writeString(inputFile, line + "\n");

        CardFileProcessor processor = new CardFileProcessor(inputFile);
        List<CardRecord> records = processor.execute();

        assertEquals(1, records.size());
        CardRecord rec = records.get(0);
        assertEquals("4111111111111111", rec.getCardNum());
        assertEquals(1L, rec.getCardAcctId());
        assertEquals(123, rec.getCardCvvCd());
        assertEquals("JOHN DOE", rec.getCardEmbossedName());
        assertEquals("2025-12-31", rec.getCardExpirationDate());
        assertEquals("Y", rec.getCardActiveStatus());
    }

    @Test
    void executeHandlesEmptyFile() throws IOException {
        Path inputFile = tempDir.resolve("carddata.txt");
        Files.writeString(inputFile, "");
        CardFileProcessor processor = new CardFileProcessor(inputFile);
        List<CardRecord> records = processor.execute();
        assertTrue(records.isEmpty());
    }

    @Test
    void executeReadsMultipleRecords() throws IOException {
        Path inputFile = tempDir.resolve("carddata.txt");
        String line1 = "4111111111111111" + "00000000001" + "123" +
                String.format("%-50s", "JOHN DOE") + "2025-12-31" + "Y";
        String line2 = "4222222222222222" + "00000000002" + "456" +
                String.format("%-50s", "JANE SMITH") + "2026-06-30" + "Y";
        Files.writeString(inputFile, line1 + "\n" + line2 + "\n");

        CardFileProcessor processor = new CardFileProcessor(inputFile);
        List<CardRecord> records = processor.execute();

        assertEquals(2, records.size());
        assertEquals("4111111111111111", records.get(0).getCardNum());
        assertEquals("4222222222222222", records.get(1).getCardNum());
    }

    @Test
    void parseCardRecordHandlesShortLine() {
        CardRecord rec = CardFileProcessor.parseCardRecord("4111111111111111");
        assertEquals("4111111111111111", rec.getCardNum());
        assertEquals(0L, rec.getCardAcctId());
    }
}
