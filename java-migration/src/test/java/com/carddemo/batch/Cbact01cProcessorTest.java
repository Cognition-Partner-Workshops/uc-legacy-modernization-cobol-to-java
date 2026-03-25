package com.carddemo.batch;

import com.carddemo.batch.model.*;
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
 * Integration tests for CBACT01C processor.
 * Verifies that the Java version produces identical results to the COBOL version
 * for the sample input data from app/data/ASCII/acctdata.txt.
 */
class Cbact01cProcessorTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arrayFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() throws IOException {
        // Use the 3-record sample data bundled in test resources
        inputFile = tempDir.resolve("acctdata.txt");
        Files.copy(
                getClass().getResourceAsStream("/sample-acctdata.txt"),
                inputFile
        );
        outFile = tempDir.resolve("outfile.txt");
        arrayFile = tempDir.resolve("arryfile.txt");
        vbrcFile = tempDir.resolve("vbrcfile.txt");
    }

    @Test
    void processesAllRecords() {
        Cbact01cProcessor processor = new Cbact01cProcessor(
                inputFile, outFile, arrayFile, vbrcFile);

        Cbact01cProcessor.ProcessingResult result = processor.execute();

        assertEquals(3, result.outRecords().size(), "Should process 3 account records");
        assertEquals(3, result.arrayRecords().size());
        assertEquals(6, result.vbrcLines().size(), "2 VBRC lines per account × 3 accounts");
    }

    // ---------------------------------------------------------------
    // OUT-FILE record verification (1300-POPUL-ACCT-RECORD)
    // ---------------------------------------------------------------

    @Test
    void outRecord_account1_fieldsMatch() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();
        OutAccountRecord out = result.outRecords().get(0);

        assertEquals(1L, out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(new BigDecimal("194.00"), out.currBal());
        assertEquals(new BigDecimal("2020.00"), out.creditLimit());
        assertEquals(new BigDecimal("1020.00"), out.cashCreditLimit());
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expirationDate());
        // Reissue date converted from YYYY-MM-DD to YYYYMMDD by COBDATFT
        assertEquals("20250520", out.reissueDate());
        assertEquals(new BigDecimal("0.00"), out.currCycCredit());
        // When ACCT-CURR-CYC-DEBIT = 0, COBOL substitutes 2525.00
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        assertEquals("", out.groupId());
    }

    @Test
    void outRecord_account2_fieldsMatch() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();
        OutAccountRecord out = result.outRecords().get(1);

        assertEquals(2L, out.acctId());
        assertEquals(new BigDecimal("158.00"), out.currBal());
        assertEquals(new BigDecimal("6130.00"), out.creditLimit());
        assertEquals(new BigDecimal("5448.00"), out.cashCreditLimit());
        assertEquals("20240811", out.reissueDate());
        // Debit is zero → substituted with 2525.00
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
    }

    @Test
    void outRecord_account3_fieldsMatch() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();
        OutAccountRecord out = result.outRecords().get(2);

        assertEquals(3L, out.acctId());
        assertEquals(new BigDecimal("147.00"), out.currBal());
        assertEquals(new BigDecimal("4909.00"), out.creditLimit());
        assertEquals(new BigDecimal("538.00"), out.cashCreditLimit());
        assertEquals("20240110", out.reissueDate());
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
    }

    // ---------------------------------------------------------------
    // ARRY-FILE record verification (1400-POPUL-ARRAY-RECORD)
    // ---------------------------------------------------------------

    @Test
    void arrayRecord_account1_entriesMatch() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();
        ArrayRecord arr = result.arrayRecords().get(0);

        assertEquals(1L, arr.acctId());
        ArrayRecord.BalanceEntry[] entries = arr.entries();

        // Entry 1: ACCT-CURR-BAL, 1005.00
        assertEquals(new BigDecimal("194.00"), entries[0].currBal());
        assertEquals(new BigDecimal("1005.00"), entries[0].currCycDebit());

        // Entry 2: ACCT-CURR-BAL, 1525.00
        assertEquals(new BigDecimal("194.00"), entries[1].currBal());
        assertEquals(new BigDecimal("1525.00"), entries[1].currCycDebit());

        // Entry 3: -1025.00, -2500.00 (fixed values)
        assertEquals(new BigDecimal("-1025.00"), entries[2].currBal());
        assertEquals(new BigDecimal("-2500.00"), entries[2].currCycDebit());

        // Entries 4-5: zero (INITIALIZE)
        assertEquals(BigDecimal.ZERO, entries[3].currBal());
        assertEquals(BigDecimal.ZERO, entries[3].currCycDebit());
        assertEquals(BigDecimal.ZERO, entries[4].currBal());
        assertEquals(BigDecimal.ZERO, entries[4].currCycDebit());
    }

    @Test
    void arrayRecord_account2_usesCorrectBalance() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();
        ArrayRecord arr = result.arrayRecords().get(1);

        assertEquals(2L, arr.acctId());
        // Entries 1 and 2 use the account's current balance
        assertEquals(new BigDecimal("158.00"), arr.entries()[0].currBal());
        assertEquals(new BigDecimal("158.00"), arr.entries()[1].currBal());
    }

    // ---------------------------------------------------------------
    // VBRC-FILE record verification (1500-POPUL-VBRC-RECORD)
    // ---------------------------------------------------------------

    @Test
    void vbrcRecords_account1() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();

        // Each account produces 2 VBRC lines: REC1 (short) then REC2 (long)
        String vb1Line = result.vbrcLines().get(0);
        String vb2Line = result.vbrcLines().get(1);

        VbrcRecord1 vb1 = VbrcRecord1.fromDelimitedLine(vb1Line);
        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());

        VbrcRecord2 vb2 = VbrcRecord2.fromDelimitedLine(vb2Line);
        assertEquals(1L, vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currBal());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        // Year extracted from reissue date "2025-05-20" → "2025"
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void vbrcRecords_account3() {
        Cbact01cProcessor.ProcessingResult result = runProcessor();

        String vb1Line = result.vbrcLines().get(4);
        String vb2Line = result.vbrcLines().get(5);

        VbrcRecord1 vb1 = VbrcRecord1.fromDelimitedLine(vb1Line);
        assertEquals(3L, vb1.acctId());

        VbrcRecord2 vb2 = VbrcRecord2.fromDelimitedLine(vb2Line);
        assertEquals(new BigDecimal("147.00"), vb2.currBal());
        assertEquals("2024", vb2.reissueYear());
    }

    // ---------------------------------------------------------------
    // Output file verification
    // ---------------------------------------------------------------

    @Test
    void outputFilesAreWritten() {
        runProcessor();

        assertTrue(Files.exists(outFile), "OUT-FILE should exist");
        assertTrue(Files.exists(arrayFile), "ARRY-FILE should exist");
        assertTrue(Files.exists(vbrcFile), "VBRC-FILE should exist");
    }

    @Test
    void outputFileLineCountsMatch() throws IOException {
        runProcessor();

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arrLines = Files.readAllLines(arrayFile);
        List<String> vbrLines = Files.readAllLines(vbrcFile);

        assertEquals(3, outLines.size(), "OUT-FILE: 1 line per account");
        assertEquals(3, arrLines.size(), "ARRY-FILE: 1 line per account");
        assertEquals(6, vbrLines.size(), "VBRC-FILE: 2 lines per account");
    }

    @Test
    void outputFilesCanBeReparsed() throws IOException {
        runProcessor();

        List<String> outLines = Files.readAllLines(outFile);
        OutAccountRecord reparsed = OutAccountRecord.fromDelimitedLine(outLines.get(0));
        assertEquals(1L, reparsed.acctId());
        assertEquals(new BigDecimal("194.00"), reparsed.currBal());

        List<String> arrLines = Files.readAllLines(arrayFile);
        ArrayRecord arrReparsed = ArrayRecord.fromDelimitedLine(arrLines.get(0));
        assertEquals(1L, arrReparsed.acctId());
        assertEquals(5, arrReparsed.entries().length);
    }

    // ---------------------------------------------------------------
    // Full-data test using all 50 records
    // ---------------------------------------------------------------

    @Test
    void processFullDatasetFromResources() throws IOException {
        // Copy the full 50-record dataset
        Path fullInput = tempDir.resolve("full-acctdata.txt");
        Files.copy(
                Path.of(System.getProperty("user.dir"))
                        .resolve("../app/data/ASCII/acctdata.txt"),
                fullInput
        );

        Path fullOut = tempDir.resolve("full-outfile.txt");
        Path fullArr = tempDir.resolve("full-arryfile.txt");
        Path fullVbr = tempDir.resolve("full-vbrcfile.txt");

        Cbact01cProcessor processor = new Cbact01cProcessor(
                fullInput, fullOut, fullArr, fullVbr);
        Cbact01cProcessor.ProcessingResult result = processor.execute();

        assertEquals(50, result.outRecords().size());
        assertEquals(50, result.arrayRecords().size());
        assertEquals(100, result.vbrcLines().size());

        // Verify every outRecord has the 2525.00 default debit (all input debits are 0)
        for (OutAccountRecord out : result.outRecords()) {
            assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                    "Account " + out.acctId() + ": zero debit should be replaced with 2525.00");
        }

        // Verify array entries 4-5 are always zero
        for (ArrayRecord arr : result.arrayRecords()) {
            assertEquals(BigDecimal.ZERO, arr.entries()[3].currBal());
            assertEquals(BigDecimal.ZERO, arr.entries()[4].currBal());
        }

        // Verify output files
        assertEquals(50, Files.readAllLines(fullOut).size());
        assertEquals(50, Files.readAllLines(fullArr).size());
        assertEquals(100, Files.readAllLines(fullVbr).size());
    }

    // ---------------------------------------------------------------
    // Edge case: debit not zero should be preserved
    // ---------------------------------------------------------------

    @Test
    void nonZeroDebitIsPreserved() throws IOException {
        // Create a record where ACCT-CURR-CYC-DEBIT is not zero
        // PIC S9(10)V99 with value 100.00:
        //   100.00 → integer 10000 → 12-digit display with overpunch "00000010000{" 
        String line = "00000099999Y00000001940{00000020200{00000010200{" +
                "2014-11-202025-05-202025-05-2000000000000{00000010000{" +
                "          ";
        Path specialInput = tempDir.resolve("special-acctdata.txt");
        Files.writeString(specialInput, line + "\n");

        Path sOut = tempDir.resolve("s-out.txt");
        Path sArr = tempDir.resolve("s-arr.txt");
        Path sVbr = tempDir.resolve("s-vbr.txt");

        Cbact01cProcessor processor = new Cbact01cProcessor(
                specialInput, sOut, sArr, sVbr);
        Cbact01cProcessor.ProcessingResult result = processor.execute();

        OutAccountRecord out = result.outRecords().get(0);
        // Debit is 1000.00, not zero, so it should NOT be replaced with 2525.00
        assertEquals(new BigDecimal("1000.00"), out.currCycDebit());
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private Cbact01cProcessor.ProcessingResult runProcessor() {
        Cbact01cProcessor processor = new Cbact01cProcessor(
                inputFile, outFile, arrayFile, vbrcFile);
        return processor.execute();
    }
}
