package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.AccountFileWriter;
import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration test that runs the full CBACT01C batch logic
 * against sample data and verifies the three output files contain
 * the expected content — identical to what the COBOL program would produce.
 */
class Cbact01cIntegrationTest {

    @TempDir
    Path tempDir;

    /**
     * Runs the full batch against the 3-record sample file and checks
     * that 3 records are written to each output file.
     */
    @Test
    void testFullBatchRun() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        int count = Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        assertEquals(3, count);
        assertTrue(Files.exists(outFile));
        assertTrue(Files.exists(arrFile));
        assertTrue(Files.exists(vbrFile));

        // OUTFILE should have 3 lines (one per account)
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(3, outLines.size());

        // ARRYFILE should have 3 lines (one per account)
        List<String> arrLines = Files.readAllLines(arrFile);
        assertEquals(3, arrLines.size());

        // VBRCFILE should have 6 lines (2 per account: VB1 + VB2)
        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals(6, vbrLines.size());
    }

    /**
     * Verifies the exact content of the OUTFILE for account #1.
     * The COBOL program would produce:
     * - ACCT-ID: 00000000001
     * - Active Status: Y
     * - Curr Bal: 00000001940{ (194.00)
     * - Credit Limit: 00000020200{ (2020.00)
     * - Cash Credit Limit: 00000010200{ (1020.00)
     * - Open Date: 2014-11-20
     * - Expiration Date: 2025-05-20
     * - Reissue Date: 20250520   (converted by COBDATFT, padded to 10)
     * - Curr Cyc Credit: 00000000000{ (0.00)
     * - Curr Cyc Debit: 00000252500{ (2525.00 — zero replaced)
     * - Group ID: A000000000
     */
    @Test
    void testOutfileContentForAccount1() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> outLines = Files.readAllLines(outFile);
        String line1 = outLines.get(0);

        // Verify field-by-field (character positions in the output line)
        assertEquals("00000000001", line1.substring(0, 11));   // ACCT-ID
        assertEquals("Y", line1.substring(11, 12));             // ACTIVE-STATUS
        assertEquals("00000001940{", line1.substring(12, 24));  // CURR-BAL
        assertEquals("00000020200{", line1.substring(24, 36));  // CREDIT-LIMIT
        assertEquals("00000010200{", line1.substring(36, 48));  // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", line1.substring(48, 58));    // OPEN-DATE
        assertEquals("2025-05-20", line1.substring(58, 68));    // EXPIRATION-DATE
        assertEquals("20250520  ", line1.substring(68, 78));    // REISSUE-DATE (COBDATFT output)
        assertEquals("00000000000{", line1.substring(78, 90));  // CURR-CYC-CREDIT
        assertEquals("00000025250{", line1.substring(90, 102)); // CURR-CYC-DEBIT (2525.00)
        assertEquals("          ", line1.substring(102, 112));  // GROUP-ID (spaces in sample data)
    }

    /**
     * Verifies the ARRYFILE content for account #1.
     * Array record structure:
     *   [0]: currBal=194.00, debit=1005.00
     *   [1]: currBal=194.00, debit=1525.00
     *   [2]: currBal=-1025.00, debit=-2500.00
     *   [3]: currBal=0.00, debit=0.00
     *   [4]: currBal=0.00, debit=0.00
     */
    @Test
    void testArryfileContentForAccount1() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> arrLines = Files.readAllLines(arrFile);
        String line1 = arrLines.get(0);

        // ACCT-ID
        assertEquals("00000000001", line1.substring(0, 11));

        // Entry 0: balance + COMP-3 debit (each 12 chars in display format)
        int offset = 11;
        assertEquals("00000001940{", line1.substring(offset, offset + 12));       // bal[0]
        assertEquals("00000010050{", line1.substring(offset + 12, offset + 24));  // debit[0]=1005.00

        // Entry 1
        offset = 11 + 24;
        assertEquals("00000001940{", line1.substring(offset, offset + 12));       // bal[1]
        assertEquals("00000015250{", line1.substring(offset + 12, offset + 24));  // debit[1]=1525.00

        // Entry 2
        offset = 11 + 48;
        assertEquals("00000010250}", line1.substring(offset, offset + 12));       // bal[2]=-1025.00
        assertEquals("00000025000}", line1.substring(offset + 12, offset + 24));  // debit[2]=-2500.00

        // Entry 3 (zeroed)
        offset = 11 + 72;
        assertEquals("00000000000{", line1.substring(offset, offset + 12));       // bal[3]=0
        assertEquals("00000000000{", line1.substring(offset + 12, offset + 24));  // debit[3]=0

        // Entry 4 (zeroed)
        offset = 11 + 96;
        assertEquals("00000000000{", line1.substring(offset, offset + 12));       // bal[4]=0
        assertEquals("00000000000{", line1.substring(offset + 12, offset + 24));  // debit[4]=0
    }

    /**
     * Verifies the VBRCFILE content for account #1.
     * Two records per account:
     *   VB1: 00000000001Y  (acctId + activeStatus)
     *   VB2: 00000000001 + currBal + creditLimit + "2025"
     */
    @Test
    void testVbrcfileContentForAccount1() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> vbrLines = Files.readAllLines(vbrFile);

        // VB1 record for account 1
        String vb1 = vbrLines.get(0);
        assertEquals("00000000001Y", vb1);

        // VB2 record for account 1
        String vb2 = vbrLines.get(1);
        assertEquals("00000000001", vb2.substring(0, 11));                 // ACCT-ID
        assertEquals("00000001940{", vb2.substring(11, 23));               // CURR-BAL
        assertEquals("00000020200{", vb2.substring(23, 35));               // CREDIT-LIMIT
        assertEquals("2025", vb2.substring(35, 39));                       // REISSUE-YYYY
    }

    /**
     * Verifies correct processing of all 3 sample accounts to ensure
     * the batch loop handles multiple records correctly.
     */
    @Test
    void testAllThreeAccountsProcessed() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> outLines = Files.readAllLines(outFile);

        // Verify account IDs in each line
        assertEquals("00000000001", outLines.get(0).substring(0, 11));
        assertEquals("00000000002", outLines.get(1).substring(0, 11));
        assertEquals("00000000003", outLines.get(2).substring(0, 11));

        // Verify VBR file has correct pairs
        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals("00000000001Y", vbrLines.get(0));  // VB1 for acct 1
        assertTrue(vbrLines.get(1).startsWith("00000000001")); // VB2 for acct 1
        assertEquals("00000000002Y", vbrLines.get(2));  // VB1 for acct 2
        assertTrue(vbrLines.get(3).startsWith("00000000002")); // VB2 for acct 2
        assertEquals("00000000003Y", vbrLines.get(4));  // VB1 for acct 3
        assertTrue(vbrLines.get(5).startsWith("00000000003")); // VB2 for acct 3
    }

    /**
     * Verifies the processing logic against account #2 (different balance values).
     */
    @Test
    void testOutfileContentForAccount2() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> outLines = Files.readAllLines(outFile);
        String line2 = outLines.get(1);

        assertEquals("00000000002", line2.substring(0, 11));    // ACCT-ID
        assertEquals("00000001580{", line2.substring(12, 24));  // CURR-BAL = 158.00
        assertEquals("00000061300{", line2.substring(24, 36));  // CREDIT-LIMIT = 6130.00
        assertEquals("20240811  ", line2.substring(68, 78));    // REISSUE-DATE converted
        assertEquals("00000025250{", line2.substring(90, 102)); // DEBIT = 2525.00 (zero replaced)
    }

    /**
     * Verifies the VB2 record for account #3 to confirm the reissue year extraction.
     */
    @Test
    void testVbrcRecord2ForAccount3() throws Exception {
        Path acctFile = Path.of(
                getClass().getClassLoader().getResource("sample_acctdata.txt").toURI());
        Path outFile = tempDir.resolve("outfile.dat");
        Path arrFile = tempDir.resolve("arryfile.dat");
        Path vbrFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.run(acctFile, outFile, arrFile, vbrFile);

        List<String> vbrLines = Files.readAllLines(vbrFile);

        // VB2 for account 3 (index 5)
        String vb2 = vbrLines.get(5);
        assertEquals("00000000003", vb2.substring(0, 11));
        assertEquals("00000001470{", vb2.substring(11, 23));  // CURR-BAL = 147.00
        assertEquals("00000049090{", vb2.substring(23, 35));  // CREDIT-LIMIT = 4909.00
        assertEquals("2024", vb2.substring(35, 39));           // REISSUE-YYYY from 2024-01-10
    }
}
