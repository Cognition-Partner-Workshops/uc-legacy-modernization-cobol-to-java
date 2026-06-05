package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
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
 * Verifies that the Java version of CBACT01C produces identical business
 * results to the original COBOL program for a set of sample inputs derived
 * from the CardDemo account data file.
 */
class CbAct01CProcessorTest {

    private static AccountRecord ACCT_1;
    private static AccountRecord ACCT_2;
    private static AccountRecord ACCT_WITH_NONZERO_DEBIT;

    @BeforeAll
    static void setUp() {
        // Record 1: Account 00000000001 — all debit/credit fields zero
        ACCT_1 = new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000",
                "          ");

        // Record 2: Account 00000000002 — debit still zero
        ACCT_2 = new AccountRecord(
                2L, "Y",
                new BigDecimal("158.00"),
                new BigDecimal("6130.00"),
                new BigDecimal("5448.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000",
                "          ");

        // Synthetic record with non-zero debit
        ACCT_WITH_NONZERO_DEBIT = new AccountRecord(
                99L, "Y",
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                "2020-01-15", "2030-01-15", "2028-06-01",
                new BigDecimal("250.00"),
                new BigDecimal("100.00"),
                "B000000000",
                "GRP0000001");
    }

    // =========================================================================
    // OutRecord transformation tests
    // =========================================================================

    @Test
    void buildOutRecord_zeroCycDebit_shouldSubstitute2525() {
        OutAccountRecord out = CbAct01CProcessor.buildOutRecord(ACCT_1);

        assertEquals(1L, out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(out.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(out.creditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(out.cashCreditLimit()));
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expirationDate());
        // Date reformatted: YYYY-MM-DD → YYYYMMDD
        assertEquals("20250520", out.reissueDate());
        assertEquals(0, new BigDecimal("0.00").compareTo(out.currCycCredit()));
        // Business rule: zero debit → 2525.00
        assertEquals(0, new BigDecimal("2525.00").compareTo(out.currCycDebit()));
    }

    @Test
    void buildOutRecord_nonZeroCycDebit_shouldPreserveOriginal() {
        OutAccountRecord out = CbAct01CProcessor.buildOutRecord(ACCT_WITH_NONZERO_DEBIT);

        assertEquals(99L, out.acctId());
        // Date reformatted
        assertEquals("20280601", out.reissueDate());
        // Debit preserved
        assertEquals(0, new BigDecimal("100.00").compareTo(out.currCycDebit()));
        // Credit preserved
        assertEquals(0, new BigDecimal("250.00").compareTo(out.currCycCredit()));
    }

    @Test
    void buildOutRecord_dateConversion_matchesCobdatft() {
        OutAccountRecord out = CbAct01CProcessor.buildOutRecord(ACCT_2);
        // Input reissue date: 2024-08-11 → YYYYMMDD: 20240811
        assertEquals("20240811", out.reissueDate());
    }

    // =========================================================================
    // ArrayRecord transformation tests
    // =========================================================================

    @Test
    void buildArrayRecord_shouldPopulate5Elements() {
        ArrayAccountRecord arr = CbAct01CProcessor.buildArrayRecord(ACCT_1);

        assertEquals(1L, arr.acctId());
        assertEquals(5, arr.balanceEntries().size());

        // Element 1: currBal from account, debit = 1005.00
        BalanceEntry e1 = arr.balanceEntries().get(0);
        assertEquals(0, new BigDecimal("194.00").compareTo(e1.currBal()));
        assertEquals(0, new BigDecimal("1005.00").compareTo(e1.currCycDebit()));

        // Element 2: currBal from account, debit = 1525.00
        BalanceEntry e2 = arr.balanceEntries().get(1);
        assertEquals(0, new BigDecimal("194.00").compareTo(e2.currBal()));
        assertEquals(0, new BigDecimal("1525.00").compareTo(e2.currCycDebit()));

        // Element 3: hardcoded -1025.00 / -2500.00
        BalanceEntry e3 = arr.balanceEntries().get(2);
        assertEquals(0, new BigDecimal("-1025.00").compareTo(e3.currBal()));
        assertEquals(0, new BigDecimal("-2500.00").compareTo(e3.currCycDebit()));

        // Elements 4 and 5: zero (INITIALIZE)
        BalanceEntry e4 = arr.balanceEntries().get(3);
        assertEquals(0, BigDecimal.ZERO.compareTo(e4.currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(e4.currCycDebit()));

        BalanceEntry e5 = arr.balanceEntries().get(4);
        assertEquals(0, BigDecimal.ZERO.compareTo(e5.currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(e5.currCycDebit()));
    }

    @Test
    void buildArrayRecord_differentBalance_shouldReflectInElements1And2() {
        ArrayAccountRecord arr = CbAct01CProcessor.buildArrayRecord(ACCT_WITH_NONZERO_DEBIT);

        assertEquals(0, new BigDecimal("500.00").compareTo(
                arr.balanceEntries().get(0).currBal()));
        assertEquals(0, new BigDecimal("500.00").compareTo(
                arr.balanceEntries().get(1).currBal()));
    }

    // =========================================================================
    // Variable-length record tests
    // =========================================================================

    @Test
    void buildVbRecord1_shouldContainIdAndStatus() {
        VbRecord1 vb1 = CbAct01CProcessor.buildVbRecord1(ACCT_1);
        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void buildVbRecord2_shouldContainIdBalLimitAndYear() {
        VbRecord2 vb2 = CbAct01CProcessor.buildVbRecord2(ACCT_1);
        assertEquals(1L, vb2.acctId());
        assertEquals(0, new BigDecimal("194.00").compareTo(vb2.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(vb2.creditLimit()));
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void buildVbRecord2_yearExtraction() {
        VbRecord2 vb2 = CbAct01CProcessor.buildVbRecord2(ACCT_WITH_NONZERO_DEBIT);
        assertEquals("2028", vb2.reissueYear());
    }

    // =========================================================================
    // End-to-end batch execution (file-based parity test)
    // =========================================================================

    @Test
    void execute_sampleFile_shouldProduceExpectedOutputs(@TempDir Path tempDir)
            throws IOException {
        Path inputFile = Path.of(
                CbAct01CProcessorTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());
        Path outFile   = tempDir.resolve("outfile.csv");
        Path arrayFile = tempDir.resolve("arrayfile.csv");
        Path vbFile    = tempDir.resolve("vbfile.csv");

        int count = CbAct01CProcessor.execute(inputFile, outFile, arrayFile, vbFile);

        assertEquals(5, count, "Should process all 5 sample records");

        // Verify output files were created with content
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(5, outLines.size());

        List<String> arrLines = Files.readAllLines(arrayFile);
        assertEquals(5, arrLines.size());

        List<String> vbLines = Files.readAllLines(vbFile);
        assertEquals(10, vbLines.size()); // 2 records per account

        // Verify first output record matches expected COBOL transformation
        String firstOutLine = outLines.get(0);
        String[] fields = firstOutLine.split(",");
        assertEquals("1", fields[0]);                      // ACCT-ID
        assertEquals("Y", fields[1]);                      // ACTIVE-STATUS
        assertEquals("194.00", fields[2]);                 // CURR-BAL
        assertEquals("2020.00", fields[3]);                // CREDIT-LIMIT
        assertEquals("1020.00", fields[4]);                // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", fields[5]);             // OPEN-DATE
        assertEquals("2025-05-20", fields[6]);             // EXPIRATION-DATE
        assertEquals("20250520", fields[7]);               // REISSUE-DATE (reformatted)
        assertEquals("0.00", fields[8]);                   // CYC-CREDIT
        assertEquals("2525.00", fields[9]);                // CYC-DEBIT (substituted)

        // Verify first array record
        String firstArrLine = arrLines.get(0);
        String[] arrFields = firstArrLine.split(",");
        assertEquals("1", arrFields[0]);
        assertEquals("194.00", arrFields[1]);   // bal[1]
        assertEquals("1005.00", arrFields[2]);  // debit[1]
        assertEquals("194.00", arrFields[3]);   // bal[2]
        assertEquals("1525.00", arrFields[4]);  // debit[2]
        assertEquals("-1025.00", arrFields[5]); // bal[3]
        assertEquals("-2500.00", arrFields[6]); // debit[3]
        assertEquals("0", arrFields[7]);        // bal[4]
        assertEquals("0", arrFields[8]);        // debit[4]
        assertEquals("0", arrFields[9]);        // bal[5]
        assertEquals("0", arrFields[10]);       // debit[5]

        // Verify VB records for first account
        String vb1Line = vbLines.get(0);
        assertEquals("VB1,1,Y", vb1Line);

        String vb2Line = vbLines.get(1);
        String[] vb2Fields = vb2Line.split(",");
        assertEquals("VB2", vb2Fields[0]);
        assertEquals("1", vb2Fields[1]);
        assertEquals("194.00", vb2Fields[2]);
        assertEquals("2020.00", vb2Fields[3]);
        assertEquals("2025", vb2Fields[4]);
    }

    @Test
    void execute_allRecordsHaveZeroDebit_allShouldGet2525(@TempDir Path tempDir)
            throws IOException {
        Path inputFile = Path.of(
                CbAct01CProcessorTest.class.getClassLoader()
                        .getResource("sample-acctdata.txt").getPath());
        Path outFile   = tempDir.resolve("outfile.csv");
        Path arrayFile = tempDir.resolve("arrayfile.csv");
        Path vbFile    = tempDir.resolve("vbfile.csv");

        CbAct01CProcessor.execute(inputFile, outFile, arrayFile, vbFile);

        List<String> outLines = Files.readAllLines(outFile);
        // All 5 sample records have zero debit, so all should show 2525.00
        for (int i = 0; i < outLines.size(); i++) {
            String[] fields = outLines.get(i).split(",");
            assertEquals("2525.00", fields[9],
                    "Record " + (i + 1) + " should have debit substitution");
        }
    }
}
