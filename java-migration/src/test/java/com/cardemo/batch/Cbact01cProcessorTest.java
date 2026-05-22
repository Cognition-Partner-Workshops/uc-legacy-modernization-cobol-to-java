package com.cardemo.batch;

import com.cardemo.batch.model.AccountRecord;
import com.cardemo.batch.model.ArrayRecord;
import com.cardemo.batch.model.OutputAccountRecord;
import com.cardemo.batch.model.VbRecord1;
import com.cardemo.batch.model.VbRecord2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Cbact01cProcessorTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arrFile;
    private Path vbFile;
    private ByteArrayOutputStream consoleCapture;
    private PrintStream consolePrint;

    // Sample account line matching CVACT01Y 300-byte layout (display-format, ASCII)
    // ACCT-ID(11) + STATUS(1) + CURR-BAL(12) + CREDIT-LIMIT(12) + CASH-CREDIT(12)
    // + OPEN-DATE(10) + EXPIRATION(10) + REISSUE(10) + CYC-CREDIT(12) + CYC-DEBIT(12)
    // + ADDR-ZIP(10) + GROUP-ID(10) + FILLER(178)
    private static final String SAMPLE_LINE_1 = buildSampleLine(
            "00000000001", "Y",
            "+00000100000", "+00001000000", "+00000500000",
            "2020-01-15", "2025-12-31", "2023-06-01",
            "+00000005000", "+00000003000",
            "10001     ", "GRP001    "
    );

    private static final String SAMPLE_LINE_ZERO_DEBIT = buildSampleLine(
            "00000000002", "Y",
            "+00000200000", "+00002000000", "+00001000000",
            "2019-03-10", "2024-11-30", "2022-08-15",
            "+00000010000", "+00000000000",
            "20002     ", "GRP002    "
    );

    private static final String SAMPLE_LINE_NEGATIVE_BAL = buildSampleLine(
            "00000000003", "N",
            "-00000050000", "+00001500000", "+00000750000",
            "2018-07-20", "2023-07-20", "2021-01-10",
            "+00000002500", "+00000001200",
            "30003     ", "GRP003    "
    );

    private static String buildSampleLine(
            String acctId, String status,
            String currBal, String creditLimit, String cashCredit,
            String openDate, String expDate, String reissueDate,
            String cycCredit, String cycDebit,
            String zip, String groupId) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(acctId);       // 11
        sb.append(status);       // 1
        sb.append(currBal);      // 12
        sb.append(creditLimit);  // 12
        sb.append(cashCredit);   // 12
        sb.append(openDate);     // 10
        sb.append(expDate);      // 10
        sb.append(reissueDate);  // 10
        sb.append(cycCredit);    // 12
        sb.append(cycDebit);     // 12
        sb.append(zip);          // 10
        sb.append(groupId);      // 10
        // pad filler to 300
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        acctFile = tempDir.resolve("acctfile.dat");
        outFile = tempDir.resolve("outfile.dat");
        arrFile = tempDir.resolve("arrfile.dat");
        vbFile = tempDir.resolve("vbfile.dat");
        consoleCapture = new ByteArrayOutputStream();
        consolePrint = new PrintStream(consoleCapture);
    }

    private Cbact01cProcessor createProcessor() {
        return new Cbact01cProcessor(acctFile, outFile, arrFile, vbFile, consolePrint);
    }

    @Test
    void singleRecord_producesCorrectOutputFiles() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();

        Cbact01cProcessor.ProcessingResult result = processor.execute();

        assertEquals(1, result.recordsProcessed());

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        assertTrue(outLines.get(0).startsWith("00000000001|"));

        List<String> arrLines = Files.readAllLines(arrFile);
        assertEquals(1, arrLines.size());
        assertTrue(arrLines.get(0).startsWith("00000000001|"));

        List<String> vbLines = Files.readAllLines(vbFile);
        assertEquals(2, vbLines.size(), "VB file should have 2 lines per account (VB1 + VB2)");
    }

    @Test
    void multipleRecords_allProcessed() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n" + SAMPLE_LINE_ZERO_DEBIT + "\n" + SAMPLE_LINE_NEGATIVE_BAL + "\n");
        Cbact01cProcessor processor = createProcessor();

        Cbact01cProcessor.ProcessingResult result = processor.execute();

        assertEquals(3, result.recordsProcessed());
        assertEquals(3, Files.readAllLines(outFile).size());
        assertEquals(3, Files.readAllLines(arrFile).size());
        assertEquals(6, Files.readAllLines(vbFile).size());
    }

    @Test
    void emptyInputFile_producesEmptyOutputs() throws IOException {
        Files.writeString(acctFile, "");
        Cbact01cProcessor processor = createProcessor();

        Cbact01cProcessor.ProcessingResult result = processor.execute();

        assertEquals(0, result.recordsProcessed());
        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arrFile).size());
        assertEquals(0, Files.readAllLines(vbFile).size());
    }

    @Test
    void zeroCycleDebit_substitutedWith2525() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_ZERO_DEBIT + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        // Field index 9 is currentCycleDebit
        assertEquals("2525.00", fields[9]);
    }

    @Test
    void nonZeroCycleDebit_keptAsIs() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        assertEquals("30.00", fields[9]);
    }

    @Test
    void outputRecord_dateConversion_yyyyMmDd_to_yyyymmdd() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        // Reissue date (field 7) should be converted from YYYY-MM-DD to YYYYMMDD
        assertEquals("20230601", fields[7]);
    }

    @Test
    void arrayRecord_hasCorrectSlotValues() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> arrLines = Files.readAllLines(arrFile);
        String[] fields = arrLines.get(0).split("\\|");

        assertEquals("00000000001", fields[0]);
        // Slot 1: balance = account balance, debit = 1005.00
        assertEquals("1000.00,1005.00", fields[1]);
        // Slot 2: balance = account balance, debit = 1525.00
        assertEquals("1000.00,1525.00", fields[2]);
        // Slot 3: balance = -1025.00, debit = -2500.00
        assertEquals("-1025.00,-2500.00", fields[3]);
        // Slots 4 & 5: zeroed
        assertEquals("0,0", fields[4]);
        assertEquals("0,0", fields[5]);
    }

    @Test
    void vbRecord1_containsIdAndStatus() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> vbLines = Files.readAllLines(vbFile);
        assertEquals("00000000001|Y", vbLines.get(0));
    }

    @Test
    void vbRecord2_containsIdBalanceCreditLimitAndReissueYear() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> vbLines = Files.readAllLines(vbFile);
        String[] fields = vbLines.get(1).split("\\|");
        assertEquals("00000000001", fields[0]);
        assertEquals("1000.00", fields[1]);
        assertEquals("10000.00", fields[2]);
        assertEquals("2023", fields[3]);
    }

    @Test
    void consoleOutput_containsStartAndEndMessages() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        String console = consoleCapture.toString();
        assertTrue(console.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(console.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void consoleOutput_displaysAccountFields() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_1 + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        String console = consoleCapture.toString();
        assertTrue(console.contains("ACCT-ID"));
        assertTrue(console.contains("ACCT-ACTIVE-STATUS"));
        assertTrue(console.contains("ACCT-CURR-BAL"));
        assertTrue(console.contains("ACCT-GROUP-ID"));
        assertTrue(console.contains("-------------------------------------------------"));
    }

    @Test
    void negativeBalance_preservedInOutput() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_NEGATIVE_BAL + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        assertEquals("-500.00", fields[2]);
    }

    @Test
    void inactiveStatus_preservedInOutput() throws IOException {
        Files.writeString(acctFile, SAMPLE_LINE_NEGATIVE_BAL + "\n");
        Cbact01cProcessor processor = createProcessor();
        processor.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        assertEquals("N", fields[1]);
    }

    @Test
    void missingInputFile_throwsIOException() {
        Path missing = tempDir.resolve("nonexistent.dat");
        Cbact01cProcessor processor = new Cbact01cProcessor(missing, outFile, arrFile, vbFile, consolePrint);
        assertThrows(IOException.class, processor::execute);
    }
}
