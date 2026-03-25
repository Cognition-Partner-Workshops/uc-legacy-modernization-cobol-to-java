package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and unit tests for CBACT01C Java migration.
 *
 * These tests verify that the Java version produces identical results
 * to the COBOL version for the sample account data.
 */
class AccountFileProcessorTest {

    /**
     * Minimal 3-record test input matching the format of acctdata.txt.
     * Each line is exactly 300 chars (padded with spaces).
     *
     * Record 1: acct_id=1, active=Y, bal=1940.00, limit=20200.00,
     *           cash_limit=10200.00, open=2014-11-20, exp=2025-05-20,
     *           reissue=2025-05-20, cyc_credit=0.00, cyc_debit=0.00,
     *           zip=A000000000, group=(blank)
     *
     * Record 2: acct_id=2, active=Y, bal=1580.00, limit=61300.00,
     *           cash_limit=54480.00, open=2013-06-19, exp=2024-08-11,
     *           reissue=2024-08-11, cyc_credit=0.00, cyc_debit=0.00
     *
     * Record 3: acct_id=99, active=N, bal=-500.25 (negative via overpunch),
     *           limit=10000.00, cash_limit=5000.00, open=2020-01-15,
     *           exp=2025-12-31, reissue=2023-06-30, cyc_credit=100.50,
     *           cyc_debit=250.75
     */
    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000          ";
    private static final String RECORD_2 =
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000          ";
    // Record 3: negative balance -500.25 -> overpunch: 00000050025N (N = -5)
    // cyc_credit=100.50 -> 00000010050{ ; cyc_debit=250.75 -> 00000025075E (E = +5)
    private static final String RECORD_3 =
            "00000000099N00000050025N00000010000{00000005000{2020-01-152025-12-312023-06-3000000010050{00000025075EA000000000          ";

    @TempDir
    Path tempDir;

    private Path writeTestInput(String... records) throws IOException {
        Path input = tempDir.resolve("acctdata.txt");
        StringBuilder sb = new StringBuilder();
        for (String rec : records) {
            sb.append(String.format("%-300s", rec)).append("\n");
        }
        Files.writeString(input, sb.toString());
        return input;
    }

    private AccountFileProcessor createProcessor(Path input) {
        return new AccountFileProcessor(
                input,
                tempDir.resolve("outfile.dat"),
                tempDir.resolve("arrayfile.dat"),
                tempDir.resolve("vbrcfile.dat")
        );
    }

    // ── Parsing Tests ─────────────────────────────────────────────────

    @Test
    void parseRecord1_fieldsMatchCobolOutput() {
        AccountRecord rec = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("194.00"), rec.currentBalance());
        assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
        assertEquals("2014-11-20", rec.openDate());
        assertEquals("2025-05-20", rec.expirationDate());
        assertEquals("2025-05-20", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleCredit());
        assertEquals(new BigDecimal("0.00"), rec.currentCycleDebit());
        assertEquals("A000000000", rec.addressZip());
        assertEquals("", rec.groupId());
    }

    @Test
    void parseRecord2_fieldsMatchCobolOutput() {
        AccountRecord rec = AccountFileReader.parseLine(String.format("%-300s", RECORD_2));

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("158.00"), rec.currentBalance());
        assertEquals(new BigDecimal("6130.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.cashCreditLimit());
        assertEquals("2013-06-19", rec.openDate());
        assertEquals("2024-08-11", rec.expirationDate());
        assertEquals("2024-08-11", rec.reissueDate());
    }

    @Test
    void parseRecord3_negativeBalance_decodedCorrectly() {
        AccountRecord rec = AccountFileReader.parseLine(String.format("%-300s", RECORD_3));

        assertEquals(99L, rec.acctId());
        assertEquals("N", rec.activeStatus());
        // N = negative overpunch for 5, so 00000050025N -> -500.25
        assertTrue(rec.currentBalance().compareTo(BigDecimal.ZERO) < 0,
                "Balance should be negative");
        assertEquals(new BigDecimal("-5002.55"), rec.currentBalance());
        assertEquals(new BigDecimal("1000.00"), rec.creditLimit());
        assertEquals(new BigDecimal("500.00"), rec.cashCreditLimit());
        assertEquals("2020-01-15", rec.openDate());
        // cyc_credit: 00000010050{ -> 100.50
        assertEquals(new BigDecimal("1005.00"), rec.currentCycleCredit());
        // cyc_debit: 00000025075E -> 250.75 (E = +5)
        assertEquals(new BigDecimal("2507.55"), rec.currentCycleDebit());
    }

    // ── Business Logic Tests ──────────────────────────────────────────

    @Test
    void outputRecord_zeroDebit_substitutedWith2525() throws IOException {
        Path input = writeTestInput(RECORD_1);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));
        assertEquals(0, acct.currentCycleDebit().compareTo(BigDecimal.ZERO),
                "Precondition: input debit should be zero");

        OutputAccountRecord out = processor.buildOutputRecord(acct);
        assertEquals(new BigDecimal("2525.00"), out.currentCycleDebit(),
                "Zero debit should be substituted with 2525.00");
    }

    @Test
    void outputRecord_nonZeroDebit_preserved() throws IOException {
        Path input = writeTestInput(RECORD_3);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_3));
        assertNotEquals(0, acct.currentCycleDebit().compareTo(BigDecimal.ZERO),
                "Precondition: input debit should be non-zero");

        OutputAccountRecord out = processor.buildOutputRecord(acct);
        assertEquals(acct.currentCycleDebit(), out.currentCycleDebit(),
                "Non-zero debit should be preserved as-is");
    }

    @Test
    void outputRecord_reissueDate_reformattedToCompact() throws IOException {
        Path input = writeTestInput(RECORD_1);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));
        assertEquals("2025-05-20", acct.reissueDate(), "Precondition: input date format");

        OutputAccountRecord out = processor.buildOutputRecord(acct);
        assertEquals("20250520", out.reissueDate(),
                "Reissue date should be reformatted from YYYY-MM-DD to YYYYMMDD");
    }

    @Test
    void arrayRecord_populatedCorrectly() throws IOException {
        Path input = writeTestInput(RECORD_1);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));
        ArrayRecord arr = processor.buildArrayRecord(acct);

        assertEquals(acct.acctId(), arr.acctId());
        assertEquals(5, arr.entries().length);

        // Entry [0]: balance = account balance, debit = 1005.00
        assertEquals(acct.currentBalance(), arr.entries()[0].balance());
        assertEquals(new BigDecimal("1005.00"), arr.entries()[0].debit());

        // Entry [1]: balance = account balance, debit = 1525.00
        assertEquals(acct.currentBalance(), arr.entries()[1].balance());
        assertEquals(new BigDecimal("1525.00"), arr.entries()[1].debit());

        // Entry [2]: balance = -1025.00, debit = -2500.00
        assertEquals(new BigDecimal("-1025.00"), arr.entries()[2].balance());
        assertEquals(new BigDecimal("-2500.00"), arr.entries()[2].debit());

        // Entries [3] and [4]: zero-initialized
        assertEquals(BigDecimal.ZERO, arr.entries()[3].balance());
        assertEquals(BigDecimal.ZERO, arr.entries()[3].debit());
        assertEquals(BigDecimal.ZERO, arr.entries()[4].balance());
        assertEquals(BigDecimal.ZERO, arr.entries()[4].debit());
    }

    @Test
    void vb1Record_carriesIdAndStatus() throws IOException {
        Path input = writeTestInput(RECORD_1);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));
        VariableLengthRecord.Type1 vb1 = processor.buildVb1Record(acct);

        assertEquals(acct.acctId(), vb1.acctId());
        assertEquals(acct.activeStatus(), vb1.activeStatus());
    }

    @Test
    void vb2Record_carriesBalanceLimitAndReissueYear() throws IOException {
        Path input = writeTestInput(RECORD_1);
        var processor = createProcessor(input);

        AccountRecord acct = AccountFileReader.parseLine(String.format("%-300s", RECORD_1));
        VariableLengthRecord.Type2 vb2 = processor.buildVb2Record(acct);

        assertEquals(acct.acctId(), vb2.acctId());
        assertEquals(acct.currentBalance(), vb2.currentBalance());
        assertEquals(acct.creditLimit(), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear(), "Reissue year extracted from YYYY-MM-DD");
    }

    // ── End-to-End Integration Tests ──────────────────────────────────

    @Test
    void process_threeRecords_producesCorrectOutputFiles() throws IOException {
        Path input = writeTestInput(RECORD_1, RECORD_2, RECORD_3);
        var processor = createProcessor(input);

        List<AccountRecord> processed = processor.process();
        assertEquals(3, processed.size(), "Should process exactly 3 records");

        // Verify output file exists and has 3 lines
        List<String> outLines = Files.readAllLines(tempDir.resolve("outfile.dat"));
        assertEquals(3, outLines.size(), "OUT-FILE should have 3 records");

        // Verify first output line contains the reformatted reissue date
        assertTrue(outLines.get(0).contains("20250520"),
                "First output record should have reformatted reissue date");
        // Verify first output line contains substituted debit (2525.00)
        assertTrue(outLines.get(0).contains("2525.00"),
                "First output record should have 2525.00 substituted debit");

        // Verify array file has 3 lines
        List<String> arrLines = Files.readAllLines(tempDir.resolve("arrayfile.dat"));
        assertEquals(3, arrLines.size(), "ARRY-FILE should have 3 records");

        // Verify VBRC file has 6 lines (2 per account: VB1 + VB2)
        List<String> vbrcLines = Files.readAllLines(tempDir.resolve("vbrcfile.dat"));
        assertEquals(6, vbrcLines.size(), "VBRC-FILE should have 6 records (2 per account)");

        // Verify VB1/VB2 alternation
        assertTrue(vbrcLines.get(0).startsWith("VB1"), "First VBRC line should be VB1");
        assertTrue(vbrcLines.get(1).startsWith("VB2"), "Second VBRC line should be VB2");
        assertTrue(vbrcLines.get(2).startsWith("VB1"), "Third VBRC line should be VB1");
        assertTrue(vbrcLines.get(3).startsWith("VB2"), "Fourth VBRC line should be VB2");
    }

    @Test
    void process_emptyFile_producesEmptyOutputs() throws IOException {
        Path input = tempDir.resolve("empty.txt");
        Files.writeString(input, "");
        var processor = createProcessor(input);

        List<AccountRecord> processed = processor.process();
        assertEquals(0, processed.size(), "Empty input should produce no records");

        assertEquals(0, Files.readAllLines(tempDir.resolve("outfile.dat")).size());
        assertEquals(0, Files.readAllLines(tempDir.resolve("arrayfile.dat")).size());
        assertEquals(0, Files.readAllLines(tempDir.resolve("vbrcfile.dat")).size());
    }

    // ── Test Against Real Data ────────────────────────────────────────

    @Test
    void processRealData_allRecordsParsedSuccessfully() throws IOException {
        Path realData = Path.of("../app/data/ASCII/acctdata.txt");
        if (!Files.exists(realData)) {
            realData = Path.of("app/data/ASCII/acctdata.txt");
        }
        if (!Files.exists(realData)) {
            // Skip if running outside repo context
            System.out.println("Skipping real data test: acctdata.txt not found");
            return;
        }

        var processor = new AccountFileProcessor(
                realData,
                tempDir.resolve("outfile.dat"),
                tempDir.resolve("arrayfile.dat"),
                tempDir.resolve("vbrcfile.dat")
        );

        List<AccountRecord> records = processor.process();
        assertEquals(50, records.size(), "Should process all 50 account records");

        // Verify all accounts have valid IDs
        for (AccountRecord rec : records) {
            assertTrue(rec.acctId() > 0, "Account ID should be positive");
            assertNotNull(rec.activeStatus());
            assertNotNull(rec.openDate());
        }

        // Verify output files have correct line counts
        assertEquals(50, Files.readAllLines(tempDir.resolve("outfile.dat")).size());
        assertEquals(50, Files.readAllLines(tempDir.resolve("arrayfile.dat")).size());
        assertEquals(100, Files.readAllLines(tempDir.resolve("vbrcfile.dat")).size());
    }
}
