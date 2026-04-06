package com.carddemo.batch;

import com.carddemo.batch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Integration and unit tests for {@link Cbact01cProcessor} verifying the Java version
 * produces identical results to the COBOL CBACT01C program for sample inputs.
 */
class Cbact01cProcessorTest {

    @TempDir
    Path tmpDir;

    private Path outFile;
    private Path arrayFile;
    private Path vbrFile;
    private ByteArrayOutputStream consoleOutput;

    @BeforeEach
    void setUp() {
        outFile = tmpDir.resolve("outfile.txt");
        arrayFile = tmpDir.resolve("arryfile.txt");
        vbrFile = tmpDir.resolve("vbrfile.txt");
        consoleOutput = new ByteArrayOutputStream();
    }

    private Cbact01cProcessor createProcessor(Path inputFile) {
        return new Cbact01cProcessor(
                inputFile, outFile, arrayFile, vbrFile,
                new PrintStream(consoleOutput)
        );
    }

    // ------------------------------------------------------------------
    // Full integration test with sample data
    // ------------------------------------------------------------------

    @Test
    @DisplayName("process all 5 sample records and verify output file record count")
    void testProcessSampleData() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        var processor = createProcessor(inputFile);

        int count = processor.execute();
        assertEquals(5, count, "Should process 5 records from sample data");

        // Verify output file has 5 lines
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(5, outLines.size(), "OUTFILE should have 5 records");

        // Verify array file has 5 lines
        List<String> arrLines = Files.readAllLines(arrayFile);
        assertEquals(5, arrLines.size(), "ARRYFILE should have 5 records");

        // Verify VBR file has 10 lines (2 records per account)
        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals(10, vbrLines.size(), "VBRCFILE should have 10 records (2 per account)");
    }

    @Test
    @DisplayName("console output starts and ends with execution messages")
    void testConsoleOutput() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        String output = consoleOutput.toString();
        assertTrue(output.startsWith("START OF EXECUTION OF PROGRAM CBACT01C"),
                "Should start with START message");
        assertTrue(output.contains("END OF EXECUTION OF PROGRAM CBACT01C"),
                "Should end with END message");
    }

    // ------------------------------------------------------------------
    // Output record tests — verifying COBOL business rules
    // ------------------------------------------------------------------

    @Test
    @DisplayName("OUTFILE record 1: reissue date converted to YYYYMMDD, debit defaults to 2525.00")
    void testOutputRecord1() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        String line1 = outLines.get(0);
        String[] fields = line1.split("\\|");

        // Field mapping: id|status|bal|creditLimit|cashCreditLimit|openDate|expDate|reissueDate|cycCredit|cycDebit|groupId
        assertEquals("00000000001", fields[0], "Account ID");
        assertEquals("Y", fields[1], "Active status");
        assertEquals("194.00", fields[2], "Current balance");
        assertEquals("2020.00", fields[3], "Credit limit");
        assertEquals("1020.00", fields[4], "Cash credit limit");
        assertEquals("2014-11-20", fields[5], "Open date (unchanged)");
        assertEquals("2025-05-20", fields[6], "Expiration date (unchanged)");

        // KEY BUSINESS RULE: reissue date converted from YYYY-MM-DD to YYYYMMDD + 2 spaces
        assertEquals("20250520  ", fields[7], "Reissue date should be YYYYMMDD padded to 10 chars");

        assertEquals("0.00", fields[8], "Cycle credit");

        // KEY BUSINESS RULE: when debit is 0, it defaults to 2525.00
        assertEquals("2525.00", fields[9], "Cycle debit should default to 2525.00 when zero");

        assertEquals("          ", fields[10], "Group ID (spaces in sample data)");
    }

    @Test
    @DisplayName("OUTFILE: all records have debit defaulted to 2525.00 (all sample debits are zero)")
    void testAllDebitsDefaulted() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        for (int i = 0; i < outLines.size(); i++) {
            String[] fields = outLines.get(i).split("\\|");
            assertEquals("2525.00", fields[9],
                    "Record " + (i + 1) + " debit should default to 2525.00");
        }
    }

    @Test
    @DisplayName("OUTFILE record 3: verify all fields for account 00000000003")
    void testOutputRecord3() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(2).split("\\|");

        assertEquals("00000000003", fields[0]);
        assertEquals("Y", fields[1]);
        assertEquals("147.00", fields[2]);
        assertEquals("4909.00", fields[3]);
        assertEquals("538.00", fields[4]);
        assertEquals("2013-08-23", fields[5]);
        assertEquals("2024-01-10", fields[6]);
        assertEquals("20240110  ", fields[7], "Reissue date: 2024-01-10 → 20240110 + 2 spaces");
        assertEquals("0.00", fields[8]);
        assertEquals("2525.00", fields[9]);
        assertEquals("          ", fields[10]);
    }

    // ------------------------------------------------------------------
    // Array record tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ARRYFILE record 1: verify array slots match COBOL logic")
    void testArrayRecord1() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> arrLines = Files.readAllLines(arrayFile);
        String[] fields = arrLines.get(0).split("\\|");

        // Account ID
        assertEquals("00000000001", fields[0]);

        // Slot 1: account balance, debit = 1005.00
        assertEquals("194.00", fields[1], "Slot 1 balance = account balance");
        assertEquals("1005.00", fields[2], "Slot 1 debit = hardcoded 1005.00");

        // Slot 2: account balance, debit = 1525.00
        assertEquals("194.00", fields[3], "Slot 2 balance = account balance");
        assertEquals("1525.00", fields[4], "Slot 2 debit = hardcoded 1525.00");

        // Slot 3: hardcoded negative values
        assertEquals("-1025.00", fields[5], "Slot 3 balance = -1025.00");
        assertEquals("-2500.00", fields[6], "Slot 3 debit = -2500.00");

        // Slots 4-5: zeroed (INITIALIZE)
        assertEquals("0", fields[7], "Slot 4 balance = 0");
        assertEquals("0", fields[8], "Slot 4 debit = 0");
        assertEquals("0", fields[9], "Slot 5 balance = 0");
        assertEquals("0", fields[10], "Slot 5 debit = 0");
    }

    @Test
    @DisplayName("ARRYFILE: slot 1 balance matches each account's current balance")
    void testArrayBalancesMatchAccounts() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arrLines = Files.readAllLines(arrayFile);

        for (int i = 0; i < outLines.size(); i++) {
            String outBal = outLines.get(i).split("\\|")[2];
            String arrBal1 = arrLines.get(i).split("\\|")[1];
            String arrBal2 = arrLines.get(i).split("\\|")[3];
            assertEquals(outBal, arrBal1,
                    "Record " + (i + 1) + ": array slot 1 balance should match account balance");
            assertEquals(outBal, arrBal2,
                    "Record " + (i + 1) + ": array slot 2 balance should match account balance");
        }
    }

    // ------------------------------------------------------------------
    // VBR record tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("VBRCFILE: alternating VB1 and VB2 records for each account")
    void testVbrRecordStructure() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals(10, vbrLines.size());

        for (int i = 0; i < vbrLines.size(); i += 2) {
            assertTrue(vbrLines.get(i).startsWith("VB1|"), "Even line should be VB1");
            assertTrue(vbrLines.get(i + 1).startsWith("VB2|"), "Odd line should be VB2");
        }
    }

    @Test
    @DisplayName("VBRCFILE record 1 VB1: account ID + active status")
    void testVbrRecord1() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        // VB1|00000000001|Y
        String[] fields = vbrLines.get(0).split("\\|");
        assertEquals("VB1", fields[0]);
        assertEquals("00000000001", fields[1]);
        assertEquals("Y", fields[2]);
    }

    @Test
    @DisplayName("VBRCFILE record 1 VB2: account ID + balance + credit limit + reissue year")
    void testVbrRecord2() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        // VB2|00000000001|194.00|2020.00|2025
        String[] fields = vbrLines.get(1).split("\\|");
        assertEquals("VB2", fields[0]);
        assertEquals("00000000001", fields[1]);
        assertEquals("194.00", fields[2], "Balance");
        assertEquals("2020.00", fields[3], "Credit limit");
        assertEquals("2025", fields[4], "Reissue year (4-digit YYYY)");
    }

    @Test
    @DisplayName("VBRCFILE: VB2 reissue year matches first 4 chars of reissue date")
    void testVbrReissueYearConsistency() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> vbrLines = Files.readAllLines(vbrFile);
        String[] expectedYears = {"2025", "2024", "2024", "2023", "2025"};

        for (int i = 0; i < 5; i++) {
            String[] fields = vbrLines.get(i * 2 + 1).split("\\|"); // VB2 lines
            assertEquals(expectedYears[i], fields[4],
                    "Account " + (i + 1) + " VB2 reissue year");
        }
    }

    // ------------------------------------------------------------------
    // Edge case: non-zero debit should NOT be defaulted
    // ------------------------------------------------------------------

    @Test
    @DisplayName("non-zero debit is preserved (not overwritten with 2525.00)")
    void testNonZeroDebitPreserved() throws IOException {
        // Create a single-record input file with a non-zero debit
        // Account with debit = 100.05 → zoned decimal "0000000100E" (E = +5)
        // Build a 300-char line: id(11) + status(1) + bal(12) + creditLim(12) + cashCred(12)
        //                       + openDate(10) + expDate(10) + reissueDate(10)
        //                       + cycCredit(12) + cycDebit(12) + addrZip(10) + groupId(10) + filler(178)
        String line = "00000099999"                     // acctId
                + "Y"                                    // activeStatus
                + "00000001000{"                         // currBal = 100.00
                + "00000050000{"                         // creditLimit = 5000.00
                + "00000025000{"                         // cashCreditLimit = 2500.00
                + "2020-01-15"                           // openDate
                + "2025-01-15"                           // expirationDate
                + "2025-06-01"                           // reissueDate
                + "00000000500{"                         // cycCredit = 50.00
                + "00000000100E"                         // cycDebit = 10.05 (non-zero!) S9(10)V99 = 12 chars
                + "1234567890"                           // addrZip
                + "B000000000"                           // groupId
                + " ".repeat(178);                       // filler

        Path inputFile = tmpDir.resolve("nonzero_debit.txt");
        Files.writeString(inputFile, line + "\n");

        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        String[] fields = outLines.get(0).split("\\|");

        // Debit should remain 10.05, NOT defaulted to 2525.00
        assertEquals("10.05", fields[9], "Non-zero debit should be preserved");
    }

    // ------------------------------------------------------------------
    // Edge case: empty input file
    // ------------------------------------------------------------------

    @Test
    @DisplayName("empty input file produces zero records and empty output files")
    void testEmptyInput() throws IOException {
        Path inputFile = tmpDir.resolve("empty.txt");
        Files.writeString(inputFile, "");

        int count = createProcessor(inputFile).execute();
        assertEquals(0, count);

        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arrayFile).size());
        assertEquals(0, Files.readAllLines(vbrFile).size());
    }

    // ------------------------------------------------------------------
    // Verify console DISPLAY output matches COBOL format
    // ------------------------------------------------------------------

    @Test
    @DisplayName("console output displays account fields matching COBOL DISPLAY statements")
    void testConsoleDisplayFormat() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        String output = consoleOutput.toString();

        // The COBOL DISPLAY statements output field labels with values
        assertTrue(output.contains("ACCT-ID                 :00000000001"),
                "Should display account ID");
        assertTrue(output.contains("ACCT-ACTIVE-STATUS      :Y"),
                "Should display active status");
        assertTrue(output.contains("ACCT-GROUP-ID           :"),
                "Should display group ID label");
        assertTrue(output.contains("-------------------------------------------------"),
                "Should display separator line");
    }

    // ------------------------------------------------------------------
    // Cross-file consistency checks
    // ------------------------------------------------------------------

    @Test
    @DisplayName("all three output files reference the same account IDs in order")
    void testCrossFileAccountIdConsistency() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arrLines = Files.readAllLines(arrayFile);
        List<String> vbrLines = Files.readAllLines(vbrFile);

        for (int i = 0; i < 5; i++) {
            String outId = outLines.get(i).split("\\|")[0];
            String arrId = arrLines.get(i).split("\\|")[0];
            String vb1Id = vbrLines.get(i * 2).split("\\|")[1];
            String vb2Id = vbrLines.get(i * 2 + 1).split("\\|")[1];

            assertEquals(outId, arrId, "OUT and ARR account IDs should match for record " + (i + 1));
            assertEquals(outId, vb1Id, "OUT and VB1 account IDs should match for record " + (i + 1));
            assertEquals(outId, vb2Id, "OUT and VB2 account IDs should match for record " + (i + 1));
        }
    }

    @Test
    @DisplayName("VB2 balance and credit limit match OUTFILE values")
    void testVb2MatchesOutfile() throws IOException {
        Path inputFile = Path.of("src/test/resources/acctdata_sample.txt");
        createProcessor(inputFile).execute();

        List<String> outLines = Files.readAllLines(outFile);
        List<String> vbrLines = Files.readAllLines(vbrFile);

        for (int i = 0; i < 5; i++) {
            String[] outFields = outLines.get(i).split("\\|");
            String[] vb2Fields = vbrLines.get(i * 2 + 1).split("\\|");

            assertEquals(outFields[2], vb2Fields[2],
                    "VB2 balance should match OUT balance for record " + (i + 1));
            assertEquals(outFields[3], vb2Fields[3],
                    "VB2 credit limit should match OUT credit limit for record " + (i + 1));
        }
    }
}
