package com.cardemo.batch;

import com.cardemo.batch.io.CobolDataParser;
import com.cardemo.batch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountBatchProcessorTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arrayFile;
    private Path vbrFile;

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("acctfile.txt");
        outFile   = tempDir.resolve("outfile.txt");
        arrayFile = tempDir.resolve("arryfile.txt");
        vbrFile   = tempDir.resolve("vbrfile.txt");
    }

    private String buildInputLine(long acctId, char status, BigDecimal bal,
                                  BigDecimal creditLim, BigDecimal cashLim,
                                  String openDt, String expDt, String reissDt,
                                  BigDecimal cycCr, BigDecimal cycDb,
                                  String zip, String grpId) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        sb.append(status);
        sb.append(CobolDataParser.formatSignedDecimal(bal, 12, 2));
        sb.append(CobolDataParser.formatSignedDecimal(creditLim, 12, 2));
        sb.append(CobolDataParser.formatSignedDecimal(cashLim, 12, 2));
        sb.append(padRight(openDt, 10));
        sb.append(padRight(expDt, 10));
        sb.append(padRight(reissDt, 10));
        sb.append(CobolDataParser.formatSignedDecimal(cycCr, 12, 2));
        sb.append(CobolDataParser.formatSignedDecimal(cycDb, 12, 2));
        sb.append(padRight(zip, 10));
        sb.append(padRight(grpId, 10));
        sb.append(" ".repeat(178));
        return sb.toString();
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    @Test
    void processesOneRecordCorrectly() throws IOException {
        String input = buildInputLine(
                1L, 'Y', new BigDecimal("194.00"),
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        int count = processor.execute();

        assertEquals(1, count);
        assertTrue(Files.exists(outFile));
        assertTrue(Files.exists(arrayFile));
        assertTrue(Files.exists(vbrFile));
    }

    @Test
    void outFileContainsCorrectFields() throws IOException {
        String input = buildInputLine(
                1L, 'Y', new BigDecimal("194.00"),
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        String outLine = Files.readAllLines(outFile).get(0);

        // ACCT-ID
        assertEquals("00000000001", outLine.substring(0, 11));
        // ACTIVE-STATUS
        assertEquals('Y', outLine.charAt(11));
        // CURR-BAL (PIC S9(10)V99)
        assertEquals("00000001940{", outLine.substring(12, 24));
        // REISSUE-DATE converted from YYYY-MM-DD to YYYYMMDD (padded to 10)
        // Offsets: 11+1+12+12+12+10+10 = 68 → reissue at [68..78)
        assertEquals("20250520  ", outLine.substring(68, 78));
    }

    @Test
    void zeroCycDebitDefaultsTo2525() throws IOException {
        String input = buildInputLine(
                42L, 'Y', new BigDecimal("500.00"),
                new BigDecimal("5000.00"), new BigDecimal("2000.00"),
                "2020-01-01", "2025-12-31", "2025-06-15",
                new BigDecimal("100.00"), BigDecimal.ZERO,
                "12345", "GRP001");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        String outLine = Files.readAllLines(outFile).get(0);
        // CYC-DEBIT: offset 11+1+12+12+12+10+10+10+12 = 90, length 12 → [90..102)
        String cycDebitField = outLine.substring(90, 102);
        BigDecimal cycDebit = CobolDataParser.parseSignedDecimal(cycDebitField, 2);
        assertEquals(new BigDecimal("2525.00"), cycDebit);
    }

    @Test
    void nonZeroCycDebitPreserved() throws IOException {
        String input = buildInputLine(
                42L, 'Y', new BigDecimal("500.00"),
                new BigDecimal("5000.00"), new BigDecimal("2000.00"),
                "2020-01-01", "2025-12-31", "2025-06-15",
                new BigDecimal("100.00"), new BigDecimal("300.00"),
                "12345", "GRP001");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        String outLine = Files.readAllLines(outFile).get(0);
        String cycDebitField = outLine.substring(90, 102);
        BigDecimal cycDebit = CobolDataParser.parseSignedDecimal(cycDebitField, 2);
        assertEquals(new BigDecimal("300.00"), cycDebit);
    }

    @Test
    void arrayFilePopulatedCorrectly() throws IOException {
        BigDecimal bal = new BigDecimal("194.00");
        String input = buildInputLine(
                1L, 'Y', bal,
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "A000000000", "");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        String arrLine = Files.readAllLines(arrayFile).get(0);

        // ACCT-ID
        assertEquals("00000000001", arrLine.substring(0, 11));

        int offset = 11;
        // Slot 1: curr-bal = account bal, debit = 1005.00
        BigDecimal slot1Bal = CobolDataParser.parseSignedDecimal(arrLine.substring(offset, offset + 12), 2);
        assertEquals(bal, slot1Bal);
        BigDecimal slot1Deb = CobolDataParser.parseSignedDecimal(arrLine.substring(offset + 12, offset + 24), 2);
        assertEquals(new BigDecimal("1005.00"), slot1Deb);

        // Slot 2: curr-bal = account bal, debit = 1525.00
        offset += 24;
        BigDecimal slot2Bal = CobolDataParser.parseSignedDecimal(arrLine.substring(offset, offset + 12), 2);
        assertEquals(bal, slot2Bal);
        BigDecimal slot2Deb = CobolDataParser.parseSignedDecimal(arrLine.substring(offset + 12, offset + 24), 2);
        assertEquals(new BigDecimal("1525.00"), slot2Deb);

        // Slot 3: curr-bal = -1025.00, debit = -2500.00
        offset += 24;
        BigDecimal slot3Bal = CobolDataParser.parseSignedDecimal(arrLine.substring(offset, offset + 12), 2);
        assertEquals(new BigDecimal("-1025.00"), slot3Bal);
        BigDecimal slot3Deb = CobolDataParser.parseSignedDecimal(arrLine.substring(offset + 12, offset + 24), 2);
        assertEquals(new BigDecimal("-2500.00"), slot3Deb);

        // Slots 4-5: zero
        offset += 24;
        BigDecimal slot4Bal = CobolDataParser.parseSignedDecimal(arrLine.substring(offset, offset + 12), 2);
        assertEquals(0, BigDecimal.ZERO.compareTo(slot4Bal));
        offset += 24;
        BigDecimal slot5Bal = CobolDataParser.parseSignedDecimal(arrLine.substring(offset, offset + 12), 2);
        assertEquals(0, BigDecimal.ZERO.compareTo(slot5Bal));
    }

    @Test
    void vbrFileContainsTwoRecordsPerAccount() throws IOException {
        String input = buildInputLine(
                7L, 'N', new BigDecimal("999.99"),
                new BigDecimal("8000.00"), new BigDecimal("3000.00"),
                "2018-03-15", "2026-03-15", "2025-09-01",
                new BigDecimal("50.00"), new BigDecimal("75.00"),
                "90210", "GRPX");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals(2, vbrLines.size());

        // VB1: status record (12 chars logical)
        String vb1 = vbrLines.get(0);
        assertEquals("00000000007", vb1.substring(0, 11));
        assertEquals('N', vb1.charAt(11));

        // VB2: balance record (39 chars logical)
        String vb2 = vbrLines.get(1);
        assertEquals("00000000007", vb2.substring(0, 11));
        BigDecimal vb2Bal = CobolDataParser.parseSignedDecimal(vb2.substring(11, 23), 2);
        assertEquals(new BigDecimal("999.99"), vb2Bal);
        BigDecimal vb2Lim = CobolDataParser.parseSignedDecimal(vb2.substring(23, 35), 2);
        assertEquals(new BigDecimal("8000.00"), vb2Lim);
        assertEquals("2025", vb2.substring(35, 39));
    }

    @Test
    void processesMultipleRecords() throws IOException {
        String line1 = buildInputLine(
                1L, 'Y', new BigDecimal("194.00"),
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO, "A000000000", "");
        String line2 = buildInputLine(
                2L, 'Y', new BigDecimal("158.00"),
                new BigDecimal("6130.00"), new BigDecimal("5448.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                BigDecimal.ZERO, BigDecimal.ZERO, "A000000000", "");

        Files.writeString(inputFile, line1 + "\n" + line2 + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        int count = processor.execute();

        assertEquals(2, count);
        assertEquals(2, Files.readAllLines(outFile).size());
        assertEquals(2, Files.readAllLines(arrayFile).size());
        assertEquals(4, Files.readAllLines(vbrFile).size()); // 2 per account
    }

    @Test
    void displayLogContainsStartAndEndMessages() throws IOException {
        String input = buildInputLine(
                1L, 'Y', new BigDecimal("194.00"),
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO, "A000000000", "");

        Files.writeString(inputFile, input + "\n");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        processor.execute();

        List<String> log = processor.getDisplayLog();
        assertTrue(log.get(0).contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(log.get(log.size() - 1).contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void processesRealSampleData() throws IOException {
        Path sampleData = Path.of("src/test/resources/sample-acctdata.txt");
        if (!Files.exists(sampleData)) {
            return; // skip if sample data not available
        }

        var processor = new AccountBatchProcessor(sampleData, outFile, arrayFile, vbrFile);
        int count = processor.execute();

        assertEquals(5, count);
        assertEquals(5, Files.readAllLines(outFile).size());
        assertEquals(5, Files.readAllLines(arrayFile).size());
        assertEquals(10, Files.readAllLines(vbrFile).size());

        // Verify first record OUT-FILE
        String firstOut = Files.readAllLines(outFile).get(0);
        assertEquals("00000000001", firstOut.substring(0, 11));
        assertEquals('Y', firstOut.charAt(11));

        // Verify reissue date converted: 2025-05-20 → 20250520
        assertEquals("20250520  ", firstOut.substring(68, 78));

        // Verify zero debit → 2525.00 default
        String cycDebit = firstOut.substring(90, 102);
        assertEquals(new BigDecimal("2525.00"),
                CobolDataParser.parseSignedDecimal(cycDebit, 2));
    }

    @Test
    void emptyInputProducesEmptyOutputs() throws IOException {
        Files.writeString(inputFile, "");

        var processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbrFile);
        int count = processor.execute();

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
    }
}
