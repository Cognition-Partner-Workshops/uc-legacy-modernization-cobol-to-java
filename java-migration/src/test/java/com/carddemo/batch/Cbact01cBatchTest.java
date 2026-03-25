package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the Java migration of CBACT01C produces identical results
 * to the COBOL version for the sample account data.
 */
class Cbact01cBatchTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() throws IOException {
        acctFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("outfile.txt");
        arryFile = tempDir.resolve("arryfile.txt");
        vbrcFile = tempDir.resolve("vbrcfile.txt");

        // Copy sample data from test resources
        Path resourceFile = Path.of("src/test/resources/acctdata.txt");
        if (Files.exists(resourceFile)) {
            Files.copy(resourceFile, acctFile);
        }
    }

    // ---------------------------------------------------------------
    // Record Parsing Tests (CVACT01Y copybook fidelity)
    // ---------------------------------------------------------------

    @Test
    void parseFirstAccountRecord() {
        // First line of acctdata.txt: account 00000000001
        String line = buildTestLine(
                "00000000001", "Y",
                "00000001940{",  // curr-bal = 194.00
                "00000020200{",  // credit-limit = 2020.00
                "00000010200{",  // cash-credit-limit = 1020.00
                "2014-11-20", "2025-05-20", "2025-05-20",
                "00000000000{",  // curr-cyc-credit = 0.00
                "00000000000{",  // curr-cyc-debit = 0.00
                "A000000000", "          ");

        AccountRecord rec = Cbact01cBatch.parseAccountRecord(line);

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
    void parseAccountWithLargerBalance() {
        // Account 16: curr-bal = 733.00, credit-limit = 8922.00
        String line = buildTestLine(
                "00000000016", "Y",
                "00000007330{",  // 733.00
                "00000089220{",  // 8922.00
                "00000026320{",  // 2632.00
                "2014-09-11", "2024-01-25", "2024-01-25",
                "00000000000{",
                "00000000000{",
                "A000000000", "          ");

        AccountRecord rec = Cbact01cBatch.parseAccountRecord(line);

        assertEquals(16L, rec.acctId());
        assertEquals(new BigDecimal("733.00"), rec.currBal());
        assertEquals(new BigDecimal("8922.00"), rec.creditLimit());
        assertEquals(new BigDecimal("2632.00"), rec.cashCreditLimit());
    }

    @Test
    void parseAccountFromActualSampleFile() throws IOException {
        if (!Files.exists(acctFile)) {
            return; // skip if sample file not available
        }

        List<String> lines = Files.readAllLines(acctFile);
        assertFalse(lines.isEmpty(), "Sample data file should not be empty");

        // Parse every line without errors
        int count = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            if (line.length() < AccountRecord.RECORD_LENGTH) {
                line = String.format("%-" + AccountRecord.RECORD_LENGTH + "s", line);
            }
            AccountRecord rec = Cbact01cBatch.parseAccountRecord(line);
            assertNotNull(rec);
            assertTrue(rec.acctId() > 0, "Account ID should be positive");
            count++;
        }
        assertEquals(50, count, "Sample file contains 50 account records");
    }

    // ---------------------------------------------------------------
    // Output Record Building Tests (1300-POPUL-ACCT-RECORD)
    // ---------------------------------------------------------------

    @Test
    void buildOutputRecord_zeroDebitReplacedWith2525() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", new BigDecimal("194.00"),
                new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"), BigDecimal.ZERO, "A000000000", "");

        Cbact01cBatch batch = new Cbact01cBatch(
                Path.of("dummy"), Path.of("dummy"), Path.of("dummy"), Path.of("dummy"));
        OutputAccountRecord out = batch.buildOutputRecord(acct);

        assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                "COBOL rule: when debit is zero, substitute 2525.00");
    }

    @Test
    void buildOutputRecord_nonZeroDebitPreserved() {
        AccountRecord acct = new AccountRecord(
                2L, "Y", new BigDecimal("158.00"),
                new BigDecimal("6130.00"), new BigDecimal("5448.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                new BigDecimal("0.00"), new BigDecimal("100.50"), "A000000000", "");

        Cbact01cBatch batch = new Cbact01cBatch(
                Path.of("dummy"), Path.of("dummy"), Path.of("dummy"), Path.of("dummy"));
        OutputAccountRecord out = batch.buildOutputRecord(acct);

        assertEquals(new BigDecimal("100.50"), out.currCycDebit(),
                "Non-zero debit should be preserved as-is");
    }

    @Test
    void buildOutputRecord_reissueDateReformatted() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "");

        Cbact01cBatch batch = new Cbact01cBatch(
                Path.of("dummy"), Path.of("dummy"), Path.of("dummy"), Path.of("dummy"));
        OutputAccountRecord out = batch.buildOutputRecord(acct);

        assertEquals("20250520", out.reissueDate(),
                "COBDATFT: YYYY-MM-DD input type 2 → YYYYMMDD output type 2");
    }

    // ---------------------------------------------------------------
    // Array Record Building Tests (1400-POPUL-ARRAY-RECORD)
    // ---------------------------------------------------------------

    @Test
    void buildArrayRecord_correctEntryValues() {
        BigDecimal balance = new BigDecimal("194.00");
        AccountRecord acct = new AccountRecord(
                1L, "Y", balance,
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "", "",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "");

        ArrayAccountRecord arr = Cbact01cBatch.buildArrayRecord(acct);

        assertEquals(1L, arr.acctId());
        assertEquals(5, arr.entries().size(), "OCCURS 5 TIMES");

        // Entry 1: actual balance, debit = 1005.00
        assertEquals(balance, arr.entries().get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.entries().get(0).currCycDebit());

        // Entry 2: actual balance, debit = 1525.00
        assertEquals(balance, arr.entries().get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.entries().get(1).currCycDebit());

        // Entry 3: balance = -1025.00, debit = -2500.00
        assertEquals(new BigDecimal("-1025.00"), arr.entries().get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.entries().get(2).currCycDebit());

        // Entries 4-5: zeros (INITIALIZE)
        assertEquals(BigDecimal.ZERO, arr.entries().get(3).currBal());
        assertEquals(BigDecimal.ZERO, arr.entries().get(3).currCycDebit());
        assertEquals(BigDecimal.ZERO, arr.entries().get(4).currBal());
        assertEquals(BigDecimal.ZERO, arr.entries().get(4).currCycDebit());
    }

    // ---------------------------------------------------------------
    // Variable-Length Record Tests (1500-POPUL-VBRC-RECORD)
    // ---------------------------------------------------------------

    @Test
    void buildVbRecord1_containsIdAndStatus() {
        AccountRecord acct = new AccountRecord(
                5L, "Y", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "", "", "", BigDecimal.ZERO, BigDecimal.ZERO, "", "");

        VbRecord1 vb1 = Cbact01cBatch.buildVbRecord1(acct);
        assertEquals(5L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void buildVbRecord2_extractsReissueYear() {
        AccountRecord acct = new AccountRecord(
                7L, "Y", new BigDecimal("193.00"), new BigDecimal("2065.00"),
                BigDecimal.ZERO, "", "", "2024-12-13",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "");

        VbRecord2 vb2 = Cbact01cBatch.buildVbRecord2(acct);
        assertEquals(7L, vb2.acctId());
        assertEquals(new BigDecimal("193.00"), vb2.currBal());
        assertEquals(new BigDecimal("2065.00"), vb2.creditLimit());
        assertEquals("2024", vb2.reissueYear(),
                "Reissue year extracted from YYYY-MM-DD (WS-ACCT-REISSUE-YYYY)");
    }

    // ---------------------------------------------------------------
    // Full Batch Execution Test
    // ---------------------------------------------------------------

    @Test
    void executeBatch_processesAllRecords() throws IOException {
        if (!Files.exists(acctFile)) {
            return; // skip if sample file not available
        }

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        int count = batch.execute();

        assertEquals(50, count, "Should process all 50 sample account records");

        // Verify output files were created
        assertTrue(Files.exists(outFile), "OUTFILE should be created");
        assertTrue(Files.exists(arryFile), "ARRYFILE should be created");
        assertTrue(Files.exists(vbrcFile), "VBRCFILE should be created");

        // Verify line counts
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(50, outLines.size(), "OUTFILE should have 50 records");

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(50, arryLines.size(), "ARRYFILE should have 50 records");

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(100, vbrcLines.size(), "VBRCFILE should have 100 records (2 per account)");
    }

    @Test
    void executeBatch_outputFileContentVerification() throws IOException {
        if (!Files.exists(acctFile)) {
            return;
        }

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        batch.execute();

        // Verify first output record (account 1)
        List<String> outLines = Files.readAllLines(outFile);
        String firstOut = outLines.get(0);
        assertTrue(firstOut.startsWith("00000000001|Y|"), "First output record should start with account 1");
        assertTrue(firstOut.contains("194.00"), "Should contain balance 194.00");
        assertTrue(firstOut.contains("2020.00"), "Should contain credit limit 2020.00");
        assertTrue(firstOut.contains("20250520"), "Should contain reformatted reissue date YYYYMMDD");
        assertTrue(firstOut.contains("2525.00"), "Zero debit should be replaced with 2525.00");

        // Verify first array record
        List<String> arryLines = Files.readAllLines(arryFile);
        String firstArr = arryLines.get(0);
        assertTrue(firstArr.startsWith("00000000001|"), "First array record for account 1");
        assertTrue(firstArr.contains("1005.00"), "Entry 1 debit = 1005.00");
        assertTrue(firstArr.contains("1525.00"), "Entry 2 debit = 1525.00");
        assertTrue(firstArr.contains("-1025.00"), "Entry 3 balance = -1025.00");
        assertTrue(firstArr.contains("-2500.00"), "Entry 3 debit = -2500.00");

        // Verify VB records
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals("VB1|00000000001|Y", vbrcLines.get(0), "First VB1 record");
        assertTrue(vbrcLines.get(1).startsWith("VB2|00000000001|"), "First VB2 record");
        assertTrue(vbrcLines.get(1).contains("|2025"), "VB2 should contain reissue year 2025");
    }

    @Test
    void executeBatch_displayOutputContainsStartAndEnd() throws IOException {
        if (!Files.exists(acctFile)) {
            return;
        }

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        batch.execute();

        List<String> display = batch.getDisplayOutput();
        assertEquals("START OF EXECUTION OF PROGRAM CBACT01C", display.get(0));
        assertEquals("END OF EXECUTION OF PROGRAM CBACT01C", display.get(display.size() - 1));
    }

    @Test
    void executeBatch_allAccountsHaveReformattedDates() throws IOException {
        if (!Files.exists(acctFile)) {
            return;
        }

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        batch.execute();

        List<String> outLines = Files.readAllLines(outFile);
        for (int i = 0; i < outLines.size(); i++) {
            String[] fields = outLines.get(i).split("\\|");
            String reissueDate = fields[7];
            assertTrue(reissueDate.matches("\\d{8}"),
                    "Record " + (i + 1) + ": reissue date should be YYYYMMDD, got: " + reissueDate);
        }
    }

    @Test
    void executeBatch_allAccountsHaveDefaultDebitWhenZero() throws IOException {
        if (!Files.exists(acctFile)) {
            return;
        }

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        batch.execute();

        // All sample records have zero debit, so all should show 2525.00
        List<String> outLines = Files.readAllLines(outFile);
        for (int i = 0; i < outLines.size(); i++) {
            String[] fields = outLines.get(i).split("\\|");
            String debit = fields[9];
            assertEquals("2525.00", debit,
                    "Record " + (i + 1) + ": zero debit should be replaced with 2525.00");
        }
    }

    // ---------------------------------------------------------------
    // Edge Cases
    // ---------------------------------------------------------------

    @Test
    void parseEmptyFileProducesNoOutput() throws IOException {
        Files.writeString(acctFile, "");

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        int count = batch.execute();

        assertEquals(0, count, "Empty file should produce zero records");
        assertEquals(0, Files.readAllLines(outFile).size());
    }

    @Test
    void parseSingleRecordFile() throws IOException {
        String singleLine = buildTestLine(
                "00000000042", "Y",
                "00000003020{",  // 302.00
                "00000065630{",  // 6563.00
                "00000051030{",  // 5103.00
                "2016-09-19", "2025-09-19", "2025-09-19",
                "00000000000{",
                "00000000000{",
                "A000000000", "          ");

        Files.writeString(acctFile, singleLine + System.lineSeparator());

        Cbact01cBatch batch = new Cbact01cBatch(acctFile, outFile, arryFile, vbrcFile);
        int count = batch.execute();

        assertEquals(1, count);
        assertEquals(1, Files.readAllLines(outFile).size());
        assertEquals(1, Files.readAllLines(arryFile).size());
        assertEquals(2, Files.readAllLines(vbrcFile).size(), "2 VB records per account");
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    /**
     * Builds a 300-char fixed-width line matching CVACT01Y layout.
     */
    private static String buildTestLine(String acctId, String status,
                                         String currBal, String creditLimit, String cashCreditLimit,
                                         String openDate, String expirationDate, String reissueDate,
                                         String currCycCredit, String currCycDebit,
                                         String addrZip, String groupId) {
        StringBuilder sb = new StringBuilder(AccountRecord.RECORD_LENGTH);
        sb.append(acctId);                                          // 11
        sb.append(status);                                          // 1
        sb.append(currBal);                                         // 12
        sb.append(creditLimit);                                     // 12
        sb.append(cashCreditLimit);                                 // 12
        sb.append(openDate);                                        // 10
        sb.append(expirationDate);                                  // 10
        sb.append(reissueDate);                                     // 10
        sb.append(currCycCredit);                                   // 12
        sb.append(currCycDebit);                                    // 12
        sb.append(String.format("%-10s", addrZip));                 // 10
        sb.append(String.format("%-10s", groupId));                 // 10

        // Pad with spaces to 300 chars (FILLER PIC X(178))
        while (sb.length() < AccountRecord.RECORD_LENGTH) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
