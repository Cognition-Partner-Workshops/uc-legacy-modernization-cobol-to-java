package com.carddemo.batch.job;

import com.carddemo.batch.model.CardXrefRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrossReferenceFileProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void executeReadsXrefRecords() throws IOException {
        Path inputFile = tempDir.resolve("cardxref.txt");
        // xrefCardNum(16) + xrefCustId(9) + xrefAcctId(11)
        String line = "4111111111111111" + "000000001" + "00000000001";
        Files.writeString(inputFile, line + "\n");

        CrossReferenceFileProcessor processor = new CrossReferenceFileProcessor(inputFile);
        List<CardXrefRecord> records = processor.execute();

        assertEquals(1, records.size());
        CardXrefRecord rec = records.get(0);
        assertEquals("4111111111111111", rec.getXrefCardNum());
        assertEquals(1L, rec.getXrefCustId());
        assertEquals(1L, rec.getXrefAcctId());
    }

    @Test
    void executeHandlesEmptyFile() throws IOException {
        Path inputFile = tempDir.resolve("cardxref.txt");
        Files.writeString(inputFile, "");
        CrossReferenceFileProcessor processor = new CrossReferenceFileProcessor(inputFile);
        List<CardXrefRecord> records = processor.execute();
        assertTrue(records.isEmpty());
    }

    @Test
    void executeReadsMultipleRecords() throws IOException {
        Path inputFile = tempDir.resolve("cardxref.txt");
        String line1 = "4111111111111111" + "000000001" + "00000000001";
        String line2 = "4222222222222222" + "000000002" + "00000000002";
        Files.writeString(inputFile, line1 + "\n" + line2 + "\n");

        CrossReferenceFileProcessor processor = new CrossReferenceFileProcessor(inputFile);
        List<CardXrefRecord> records = processor.execute();

        assertEquals(2, records.size());
        assertEquals("4111111111111111", records.get(0).getXrefCardNum());
        assertEquals("4222222222222222", records.get(1).getXrefCardNum());
    }
}
