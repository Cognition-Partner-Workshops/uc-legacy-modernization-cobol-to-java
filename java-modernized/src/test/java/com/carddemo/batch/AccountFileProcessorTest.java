package com.carddemo.batch;

import com.carddemo.batch.exception.BatchAbendException;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() {
        acctFile = tempDir.resolve("acctfile.dat");
        outFile = tempDir.resolve("outfile.dat");
        arryFile = tempDir.resolve("arryfile.dat");
        vbrcFile = tempDir.resolve("vbrcfile.dat");
    }

    @Test
    void zeroDebitSubstitution_outputIs2525() {
        AccountRecord acct = buildTestAccount(new BigDecimal("0.00"));
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        OutputAccountRecord out = processor.populateOutputRecord(acct);

        assertEquals(new BigDecimal("2525.00"), out.getCurrentCycleDebit());
    }

    @Test
    void nonZeroDebit_preservedInOutput() {
        AccountRecord acct = buildTestAccount(new BigDecimal("300.00"));
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        OutputAccountRecord out = processor.populateOutputRecord(acct);

        assertEquals(new BigDecimal("300.00"), out.getCurrentCycleDebit());
    }

    @Test
    void reissueDate_convertedFromDashedToCompact() {
        AccountRecord acct = buildTestAccount(new BigDecimal("100.00"));
        acct.setReissueDate("2025-06-15");
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        OutputAccountRecord out = processor.populateOutputRecord(acct);

        assertEquals("20250615", out.getReissueDate().trim());
    }

    @Test
    void arrayRecordPopulation_allFiveIndices() {
        AccountRecord acct = buildTestAccount(new BigDecimal("100.00"));
        acct.setCurrentBalance(new BigDecimal("5000.00"));
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        ArrayRecord arr = processor.populateArrayRecord(acct);

        assertEquals(acct.getAcctId(), arr.getAcctId());
        assertEquals(5, arr.getEntries().size());

        assertEquals(new BigDecimal("5000.00"), arr.getEntries().get(0).balance());
        assertEquals(new BigDecimal("1005.00"), arr.getEntries().get(0).debit());

        assertEquals(new BigDecimal("5000.00"), arr.getEntries().get(1).balance());
        assertEquals(new BigDecimal("1525.00"), arr.getEntries().get(1).debit());

        assertEquals(new BigDecimal("-1025.00"), arr.getEntries().get(2).balance());
        assertEquals(new BigDecimal("-2500.00"), arr.getEntries().get(2).debit());

        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(3).balance());
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(3).debit());

        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(4).balance());
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(4).debit());
    }

    @Test
    void vb1RecordSizing_exactly12Chars() {
        AccountRecord acct = buildTestAccount(new BigDecimal("100.00"));
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        String vb1 = processor.populateVb1Record(acct);
        assertEquals(AccountFileProcessor.VB1_LENGTH, vb1.length());

        assertTrue(vb1.startsWith(String.format("%011d", acct.getAcctId())));
        assertEquals(acct.getActiveStatus(), vb1.charAt(11));
    }

    @Test
    void vb2RecordSizing_exactly39Chars() {
        AccountRecord acct = buildTestAccount(new BigDecimal("100.00"));
        acct.setReissueDate("2025-06-15");
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);

        String vb2 = processor.populateVb2Record(acct);
        assertEquals(AccountFileProcessor.VB2_LENGTH, vb2.length());

        assertTrue(vb2.startsWith(String.format("%011d", acct.getAcctId())));
        assertTrue(vb2.endsWith("2025"));
    }

    @Test
    void fullPipeline_singleRecord() throws IOException {
        String inputLine = buildFixedWidthLine(
                1L, 'Y',
                new BigDecimal("5000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                "2024-01-15", "2026-01-15", "2025-06-15",
                new BigDecimal("500.00"),
                new BigDecimal("0.00"),
                "10001     ",
                "GROUP001  "
        );
        Files.writeString(acctFile, inputLine + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        assertTrue(outLines.get(0).contains("2525.00"),
                "Zero debit should be substituted with 2525.00");
        assertTrue(outLines.get(0).contains("20250615"),
                "Reissue date should be in YYYYMMDD format");

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(1, arryLines.size());
        assertTrue(arryLines.get(0).contains("1005.00"));
        assertTrue(arryLines.get(0).contains("1525.00"));
        assertTrue(arryLines.get(0).contains("-1025.00"));
        assertTrue(arryLines.get(0).contains("-2500.00"));

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(2, vbrcLines.size());
        assertEquals(12, vbrcLines.get(0).length(), "VB1 record should be 12 chars");
        assertEquals(39, vbrcLines.get(1).length(), "VB2 record should be 39 chars");
    }

    @Test
    void fullPipeline_multipleRecords() throws IOException {
        String line1 = buildFixedWidthLine(1L, 'Y',
                new BigDecimal("5000.00"), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                "2024-01-15", "2026-01-15", "2025-06-15",
                new BigDecimal("500.00"), new BigDecimal("0.00"),
                "10001     ", "GROUP001  ");
        String line2 = buildFixedWidthLine(2L, 'N',
                new BigDecimal("1000.00"), new BigDecimal("20000.00"),
                new BigDecimal("10000.00"),
                "2023-03-01", "2025-03-01", "2024-09-01",
                new BigDecimal("100.00"), new BigDecimal("250.50"),
                "90210     ", "GROUP002  ");

        Files.writeString(acctFile, line1 + "\n" + line2 + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);
        processor.execute();

        assertEquals(2, Files.readAllLines(outFile).size());
        assertEquals(2, Files.readAllLines(arryFile).size());
        assertEquals(4, Files.readAllLines(vbrcFile).size());
    }

    @Test
    void errorHandling_fileNotFound() {
        Path nonExistent = tempDir.resolve("does_not_exist.dat");
        AccountFileProcessor processor = new AccountFileProcessor(
                nonExistent, outFile, arryFile, vbrcFile);

        BatchAbendException ex = assertThrows(BatchAbendException.class,
                processor::execute);
        assertEquals(999, ex.getAbendCode());
    }

    @Test
    void errorHandling_outputDirNotWritable() {
        Path badPath = Path.of("/nonexistent/directory/outfile.dat");
        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, badPath, arryFile, vbrcFile);

        try {
            Files.writeString(acctFile, buildFixedWidthLine(
                    1L, 'Y', BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "2024-01-01", "2026-01-01", "2025-01-01",
                    BigDecimal.ZERO, BigDecimal.ZERO, "          ", "          ")
                    + "\n");
        } catch (IOException e) {
            fail("Setup should not fail");
        }

        assertThrows(BatchAbendException.class, processor::execute);
    }

    @Test
    void fullPipeline_nonZeroDebit_preserved() throws IOException {
        String line = buildFixedWidthLine(3L, 'Y',
                new BigDecimal("8000.00"), new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                "2024-02-20", "2026-02-20", "2025-08-20",
                new BigDecimal("600.00"), new BigDecimal("450.75"),
                "30301     ", "GROUP003  ");
        Files.writeString(acctFile, line + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        assertTrue(outLines.get(0).contains("450.75"),
                "Non-zero debit should be preserved");
        assertFalse(outLines.get(0).contains("2525.00"),
                "2525.00 substitution should not occur for non-zero debit");
    }

    @Test
    void emptyInputFile_producesNoOutput() throws IOException {
        Files.writeString(acctFile, "");

        AccountFileProcessor processor = new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile);
        processor.execute();

        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arryFile).size());
        assertEquals(0, Files.readAllLines(vbrcFile).size());
    }

    private AccountRecord buildTestAccount(BigDecimal debit) {
        AccountRecord acct = new AccountRecord();
        acct.setAcctId(12345678901L);
        acct.setActiveStatus('Y');
        acct.setCurrentBalance(new BigDecimal("5000.00"));
        acct.setCreditLimit(new BigDecimal("10000.00"));
        acct.setCashCreditLimit(new BigDecimal("5000.00"));
        acct.setOpenDate("2024-01-15");
        acct.setExpirationDate("2026-01-15");
        acct.setReissueDate("2025-06-15");
        acct.setCurrentCycleCredit(new BigDecimal("500.00"));
        acct.setCurrentCycleDebit(debit);
        acct.setAddrZip("10001");
        acct.setGroupId("GROUP001");
        return acct;
    }

    private String buildFixedWidthLine(long acctId, char status,
                                       BigDecimal currBal, BigDecimal creditLimit,
                                       BigDecimal cashCreditLimit,
                                       String openDate, String expDate, String reissueDate,
                                       BigDecimal cycCredit, BigDecimal cycDebit,
                                       String addrZip, String groupId) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(String.format("%011d", acctId));
        sb.append(status);
        sb.append(formatDec(currBal));
        sb.append(formatDec(creditLimit));
        sb.append(formatDec(cashCreditLimit));
        sb.append(padRight(openDate, 10));
        sb.append(padRight(expDate, 10));
        sb.append(padRight(reissueDate, 10));
        sb.append(formatDec(cycCredit));
        sb.append(formatDec(cycDebit));
        sb.append(padRight(addrZip, 10));
        sb.append(padRight(groupId, 10));
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String formatDec(BigDecimal val) {
        BigDecimal scaled = val.setScale(2);
        String unscaled = scaled.unscaledValue().abs().toString();
        while (unscaled.length() < 12) {
            unscaled = "0" + unscaled;
        }
        if (scaled.signum() < 0) {
            return "-" + unscaled.substring(1);
        }
        return unscaled;
    }

    private String padRight(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }
}
