package com.carddemo.batch;

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

/**
 * Integration tests for the CBACT01C Java migration.
 * Verifies that the Java version produces identical output to the COBOL version
 * for the sample account data in the repository.
 */
class CBACT01CTest {

    @TempDir
    Path tempDir;

    private Path sampleDataFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;
    private ByteArrayOutputStream consoleOutput;

    @BeforeEach
    void setUp() throws IOException {
        // Locate sample data - check relative paths from test execution dir
        Path repoRoot = findRepoRoot();
        sampleDataFile = repoRoot.resolve("app/data/ASCII/acctdata.txt");

        if (!Files.exists(sampleDataFile)) {
            // Fall back: create minimal test data
            sampleDataFile = tempDir.resolve("acctdata.txt");
            Files.writeString(sampleDataFile, buildMinimalTestRecord());
        }

        outFile = tempDir.resolve("outfile.dat");
        arryFile = tempDir.resolve("arryfile.dat");
        vbrcFile = tempDir.resolve("vbrcfile.dat");
        consoleOutput = new ByteArrayOutputStream();
    }

    private Path findRepoRoot() {
        // Walk up from CWD to find the repo root
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path candidate = cwd;
        for (int i = 0; i < 10; i++) {
            if (Files.exists(candidate.resolve("app/data/ASCII/acctdata.txt"))) {
                return candidate;
            }
            candidate = candidate.getParent();
            if (candidate == null) break;
        }
        // If running from java-migration subdir, go up one level
        return cwd.getParent() != null ? cwd.getParent() : cwd;
    }

    private String buildMinimalTestRecord() {
        // Build a 300-byte test record matching the CVACT01Y layout
        StringBuilder sb = new StringBuilder(300);
        sb.append("00000000001");                     // ACCT-ID (11)
        sb.append("Y");                                // ACCT-ACTIVE-STATUS (1)
        sb.append("00000001940{");                     // ACCT-CURR-BAL S9(10)V99 = +194.00
        sb.append("00000020200{");                     // ACCT-CREDIT-LIMIT = +2020.00
        sb.append("00000010200{");                     // ACCT-CASH-CREDIT-LIMIT = +1020.00
        sb.append("2014-11-20");                       // ACCT-OPEN-DATE (10)
        sb.append("2025-05-20");                       // ACCT-EXPIRAION-DATE (10)
        sb.append("2025-05-20");                       // ACCT-REISSUE-DATE (10)
        sb.append("00000000000{");                     // ACCT-CURR-CYC-CREDIT = +0.00
        sb.append("00000000000{");                     // ACCT-CURR-CYC-DEBIT = +0.00
        sb.append("A000000000");                       // ACCT-ADDR-ZIP (10) - actually part of record
        sb.append("          ");                       // ACCT-GROUP-ID (10)
        while (sb.length() < 300) sb.append(' ');      // FILLER
        return sb.toString();
    }

    // ---- Parsing Tests ----

    @Test
    void parseSampleData_returnsCorrectRecordCount() throws IOException {
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        if (Files.exists(findRepoRoot().resolve("app/data/ASCII/acctdata.txt"))) {
            assertEquals(50, records.size(), "Sample data should contain 50 account records");
        } else {
            assertTrue(records.size() > 0, "Should parse at least one record");
        }
    }

    @Test
    void parseSampleData_firstRecord_fieldsCorrect() throws IOException {
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        AccountRecord first = records.get(0);

        assertEquals(1L, first.acctId());
        assertEquals("Y", first.acctActiveStatus());
        assertEquals(new BigDecimal("194.00"), first.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), first.acctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), first.acctCashCreditLimit());
        assertEquals("2014-11-20", first.acctOpenDate());
        assertEquals("2025-05-20", first.acctExpirationDate());
        assertEquals("2025-05-20", first.acctReissueDate());
        assertEquals(new BigDecimal("0.00"), first.acctCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), first.acctCurrCycDebit());
    }

    @Test
    void parseSampleData_lastRecord_fieldsCorrect() throws IOException {
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        if (records.size() < 50) return; // skip if using minimal test data

        AccountRecord last = records.get(49);
        assertEquals(50L, last.acctId());
        assertEquals("Y", last.acctActiveStatus());
        assertEquals(new BigDecimal("492.00"), last.acctCurrBal());
    }

    // ---- Console Output Tests ----

    @Test
    void execute_displaysStartAndEndMessages() throws IOException {
        runJob();
        String output = consoleOutput.toString();
        assertTrue(output.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(output.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void execute_displaysFieldLabels() throws IOException {
        runJob();
        String output = consoleOutput.toString();
        assertTrue(output.contains("ACCT-ID                 :"));
        assertTrue(output.contains("ACCT-ACTIVE-STATUS      :"));
        assertTrue(output.contains("ACCT-CURR-BAL           :"));
        assertTrue(output.contains("ACCT-CREDIT-LIMIT       :"));
        assertTrue(output.contains("ACCT-CASH-CREDIT-LIMIT  :"));
        assertTrue(output.contains("ACCT-OPEN-DATE          :"));
        assertTrue(output.contains("ACCT-EXPIRAION-DATE     :"));
        assertTrue(output.contains("ACCT-REISSUE-DATE       :"));
        assertTrue(output.contains("ACCT-CURR-CYC-CREDIT    :"));
        assertTrue(output.contains("ACCT-CURR-CYC-DEBIT     :"));
        assertTrue(output.contains("ACCT-GROUP-ID           :"));
        assertTrue(output.contains("-------------------------------------------------"));
    }

    @Test
    void execute_displaysCorrectFirstAccountValues() throws IOException {
        runJob();
        String output = consoleOutput.toString();
        assertTrue(output.contains("ACCT-ID                 :00000000001"));
        assertTrue(output.contains("ACCT-ACTIVE-STATUS      :Y"));
        assertTrue(output.contains("ACCT-CURR-BAL           :00000001940{"));
        assertTrue(output.contains("ACCT-CREDIT-LIMIT       :00000020200{"));
        assertTrue(output.contains("ACCT-CASH-CREDIT-LIMIT  :00000010200{"));
        assertTrue(output.contains("ACCT-OPEN-DATE          :2014-11-20"));
        assertTrue(output.contains("ACCT-EXPIRAION-DATE     :2025-05-20"));
        assertTrue(output.contains("ACCT-REISSUE-DATE       :2025-05-20"));
    }

    // ---- OUTFILE Tests ----

    @Test
    void outFile_hasCorrectRecordSize() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        // Each record = 107 bytes, with sample data of 50 records
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        assertEquals(107 * records.size(), data.length,
                "OUTFILE should contain 107 bytes per record");
    }

    @Test
    void outFile_firstRecord_acctId() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        String acctId = new String(data, 0, 11);
        assertEquals("00000000001", acctId);
    }

    @Test
    void outFile_firstRecord_activeStatus() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        assertEquals((byte) 'Y', data[11]);
    }

    @Test
    void outFile_firstRecord_reissueDateFormatted() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        // OUT-ACCT-REISSUE-DATE is at offset 70 (11+1+12+12+12+10+10+10=78? let me recalc)
        // 11 + 1 + 12 + 12 + 12 + 10 + 10 = 68 bytes before reissue date
        String reissueDate = new String(data, 68, 10);
        assertEquals("20250520  ", reissueDate,
                "Reissue date should be reformatted from YYYY-MM-DD to YYYYMMDD");
    }

    @Test
    void outFile_firstRecord_cycDebitIsComp3_withSubstitution() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        // CYC-DEBIT offset: 11+1+12+12+12+10+10+10+12 = 90
        // When ACCT-CURR-CYC-DEBIT is zero, substitute 2525.00
        // COMP-3 for 2525.00: "0000000252500" + C sign
        // Packed: 00 00 00 02 52 50 0C
        int debitOffset = 90;
        assertEquals((byte) 0x00, data[debitOffset]);
        assertEquals((byte) 0x00, data[debitOffset + 1]);
        assertEquals((byte) 0x00, data[debitOffset + 2]);
        assertEquals((byte) 0x02, data[debitOffset + 3]);
        assertEquals((byte) 0x52, data[debitOffset + 4]);
        assertEquals((byte) 0x50, data[debitOffset + 5]);
        assertEquals((byte) 0x0C, data[debitOffset + 6]);
    }

    @Test
    void outFile_firstRecord_groupId() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(outFile);
        // GROUP-ID offset: 90 + 7 = 97
        String groupId = new String(data, 97, 10);
        // First record's group ID from data
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        String expected = CobolDecimalUtils.fixedWidth(records.get(0).acctGroupId(), 10);
        assertEquals(expected, groupId);
    }

    // ---- ARRYFILE Tests ----

    @Test
    void arryFile_hasCorrectRecordSize() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(arryFile);
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        assertEquals(110 * records.size(), data.length,
                "ARRYFILE should contain 110 bytes per record");
    }

    @Test
    void arryFile_firstRecord_acctId() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(arryFile);
        String acctId = new String(data, 0, 11);
        assertEquals("00000000001", acctId);
    }

    @Test
    void arryFile_firstRecord_group1Balance() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(arryFile);
        // Group 1 balance starts at offset 11 (after ACCT-ID)
        String bal1 = new String(data, 11, 12);
        assertEquals("00000001940{", bal1,
                "Group 1 balance should be ACCT-CURR-BAL (194.00)");
    }

    @Test
    void arryFile_firstRecord_group1Debit() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(arryFile);
        // Group 1 debit starts at offset 11+12=23 (COMP-3, 7 bytes)
        // 1005.00 -> "0000000100500" + C -> 00 00 00 01 00 50 0C
        int offset = 23;
        assertEquals((byte) 0x00, data[offset]);
        assertEquals((byte) 0x00, data[offset + 1]);
        assertEquals((byte) 0x00, data[offset + 2]);
        assertEquals((byte) 0x01, data[offset + 3]);
        assertEquals((byte) 0x00, data[offset + 4]);
        assertEquals((byte) 0x50, data[offset + 5]);
        assertEquals((byte) 0x0C, data[offset + 6]);
    }

    @Test
    void arryFile_firstRecord_group3NegativeValues() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(arryFile);
        // Group 3 starts at offset 11 + 2*(12+7) = 11 + 38 = 49
        // Balance: -1025.00 -> "0000010250}" (zoned)
        String bal3 = new String(data, 49, 12);
        assertEquals("00000010250}", bal3,
                "Group 3 balance should be -1025.00");

        // Debit: -2500.00 COMP-3 -> "0000000250000" + D -> 00 00 00 02 50 00 0D
        int debitOffset = 61;
        assertEquals((byte) 0x00, data[debitOffset]);
        assertEquals((byte) 0x02, data[debitOffset + 3]);
        assertEquals((byte) 0x50, data[debitOffset + 4]);
        assertEquals((byte) 0x00, data[debitOffset + 5]);
        assertEquals((byte) 0x0D, data[debitOffset + 6]);
    }

    // ---- VBRCFILE Tests ----

    @Test
    void vbrcFile_correctTotalSize() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(vbrcFile);
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        // Each account produces VB1 (12) + VB2 (39) = 51 bytes
        assertEquals(51 * records.size(), data.length,
                "VBRCFILE should contain 51 bytes per account (12 + 39)");
    }

    @Test
    void vbrcFile_firstVb1Record() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(vbrcFile);
        // VB1: 12 bytes - ACCT-ID (11) + ACTIVE-STATUS (1)
        String acctId = new String(data, 0, 11);
        assertEquals("00000000001", acctId);
        assertEquals((byte) 'Y', data[11]);
    }

    @Test
    void vbrcFile_firstVb2Record() throws IOException {
        runJob();
        byte[] data = Files.readAllBytes(vbrcFile);
        // VB2 starts at offset 12 (after VB1), length 39
        String vb2AcctId = new String(data, 12, 11);
        assertEquals("00000000001", vb2AcctId);

        // CURR-BAL at offset 12+11=23
        String currBal = new String(data, 23, 12);
        assertEquals("00000001940{", currBal);

        // CREDIT-LIMIT at offset 23+12=35
        String creditLimit = new String(data, 35, 12);
        assertEquals("00000020200{", creditLimit);

        // REISSUE-YYYY at offset 35+12=47
        String reissueYyyy = new String(data, 47, 4);
        assertEquals("2025", reissueYyyy);
    }

    // ---- Full Execution Smoke Test ----

    @Test
    void execute_allOutputFilesCreated() throws IOException {
        runJob();
        assertTrue(Files.exists(outFile), "OUTFILE should be created");
        assertTrue(Files.exists(arryFile), "ARRYFILE should be created");
        assertTrue(Files.exists(vbrcFile), "VBRCFILE should be created");
        assertTrue(Files.size(outFile) > 0, "OUTFILE should not be empty");
        assertTrue(Files.size(arryFile) > 0, "ARRYFILE should not be empty");
        assertTrue(Files.size(vbrcFile) > 0, "VBRCFILE should not be empty");
    }

    @Test
    void execute_allRecordsProcessed() throws IOException {
        runJob();
        String output = consoleOutput.toString();
        List<AccountRecord> records = AccountFileParser.parse(sampleDataFile);
        for (AccountRecord rec : records) {
            String acctIdStr = CobolDecimalUtils.formatUnsignedNumeric(rec.acctId(), 11);
            assertTrue(output.contains("ACCT-ID                 :" + acctIdStr),
                    "Console should display ACCT-ID for account " + rec.acctId());
        }
    }

    @Test
    void execute_secondRecord_verifyAllOutputs() throws IOException {
        runJob();
        byte[] outData = Files.readAllBytes(outFile);
        byte[] arryData = Files.readAllBytes(arryFile);
        byte[] vbrcData = Files.readAllBytes(vbrcFile);

        // Second record starts at offset 107 in OUTFILE
        String acctId2 = new String(outData, 107, 11);
        assertEquals("00000000002", acctId2);

        // Second record in ARRYFILE starts at offset 110
        String arrAcctId2 = new String(arryData, 110, 11);
        assertEquals("00000000002", arrAcctId2);

        // Second account in VBRCFILE starts at offset 51 (12+39)
        String vbrcAcctId2 = new String(vbrcData, 51, 11);
        assertEquals("00000000002", vbrcAcctId2);
    }

    private void runJob() throws IOException {
        CBACT01C job = new CBACT01C(
                sampleDataFile, outFile, arryFile, vbrcFile,
                new PrintStream(consoleOutput));
        job.execute();
    }
}
