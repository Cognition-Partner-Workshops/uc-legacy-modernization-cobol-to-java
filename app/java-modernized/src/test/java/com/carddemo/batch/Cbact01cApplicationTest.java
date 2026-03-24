package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.model.AccountRecord;
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
 * Integration tests for the CBACT01C Java rewrite.
 *
 * These tests verify the Java application produces identical results to
 * the COBOL version for a set of sample inputs derived from acctdata.txt.
 */
class Cbact01cApplicationTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    /** Build a 300-char record matching the COBOL fixed-width layout. */
    private static String buildRecord(String acctId, String status,
                                       String currBal, String creditLimit,
                                       String cashCreditLimit,
                                       String openDate, String expDate,
                                       String reissueDate,
                                       String cycCredit, String cycDebit,
                                       String addrZip, String groupId) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(acctId);          // 11
        sb.append(status);          // 1
        sb.append(currBal);         // 12
        sb.append(creditLimit);     // 12
        sb.append(cashCreditLimit); // 12
        sb.append(openDate);        // 10
        sb.append(expDate);         // 10
        sb.append(reissueDate);     // 10
        sb.append(cycCredit);       // 12
        sb.append(cycDebit);        // 12
        sb.append(padRight(addrZip, 10));  // 10
        sb.append(padRight(groupId, 10));  // 10
        // Fill remaining with spaces (FILLER)
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    @BeforeEach
    void setUp() throws IOException {
        acctFile = tempDir.resolve("acctfile.txt");
        outFile = tempDir.resolve("outfile.txt");
        arryFile = tempDir.resolve("arryfile.txt");
        vbrcFile = tempDir.resolve("vbrcfile.txt");
    }

    // ----------------------------------------------------------------
    // Test 1: Single record with zero debit (triggers 2525.00 default)
    // Mirrors COBOL record #1 from acctdata.txt
    // ----------------------------------------------------------------

    @Test
    void singleRecord_zeroDebit_shouldDefault2525() throws IOException {
        // Record 1: acctId=00000000001, currBal=194.00, cycDebit=0.00
        String rec = buildRecord(
                "00000000001", "Y",
                "00000001940{", "00000020200{", "00000010200{",
                "2014-11-20", "2025-05-20", "2025-05-20",
                "00000000000{", "00000000000{",
                "", "A000000000");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        int count = app.execute();

        assertEquals(1, count);

        // Verify OUTFILE
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        String[] outFields = outLines.get(0).split("\\|");
        assertEquals("00000000001", outFields[0]);   // ACCT-ID
        assertEquals("Y", outFields[1]);              // ACTIVE-STATUS
        assertEquals("194.00", outFields[2]);         // CURR-BAL
        assertEquals("2020.00", outFields[3]);        // CREDIT-LIMIT
        assertEquals("1020.00", outFields[4]);        // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", outFields[5]);     // OPEN-DATE
        assertEquals("2025-05-20", outFields[6]);     // EXPIRAION-DATE
        assertEquals("20250520", outFields[7]);       // REISSUE-DATE (reformatted)
        assertEquals("0.00", outFields[8]);           // CURR-CYC-CREDIT
        assertEquals("2525.00", outFields[9]);        // CURR-CYC-DEBIT (defaulted)
        assertEquals("A000000000", outFields[10]);    // GROUP-ID

        // Verify ARRYFILE
        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(1, arryLines.size());
        String[] arrFields = arryLines.get(0).split("\\|");
        assertEquals("00000000001", arrFields[0]);    // ACCT-ID
        assertEquals("194.00", arrFields[1]);         // slot1 bal
        assertEquals("1005.00", arrFields[2]);        // slot1 debit
        assertEquals("194.00", arrFields[3]);         // slot2 bal
        assertEquals("1525.00", arrFields[4]);        // slot2 debit
        assertEquals("-1025.00", arrFields[5]);       // slot3 bal
        assertEquals("-2500.00", arrFields[6]);       // slot3 debit
        assertEquals("0", arrFields[7]);              // slot4 bal
        assertEquals("0", arrFields[8]);              // slot4 debit
        assertEquals("0", arrFields[9]);              // slot5 bal
        assertEquals("0", arrFields[10]);             // slot5 debit

        // Verify VBRCFILE (two records per account)
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(2, vbrcLines.size());
        // VBR record 1
        String[] vb1Fields = vbrcLines.get(0).split("\\|");
        assertEquals("00000000001", vb1Fields[0]);
        assertEquals("Y", vb1Fields[1]);
        // VBR record 2
        String[] vb2Fields = vbrcLines.get(1).split("\\|");
        assertEquals("00000000001", vb2Fields[0]);
        assertEquals("194.00", vb2Fields[1]);
        assertEquals("2020.00", vb2Fields[2]);
        assertEquals("2025", vb2Fields[3]);           // reissue year
    }

    // ----------------------------------------------------------------
    // Test 2: Multiple records — verifies sequential processing
    // Uses records 1-3 from acctdata.txt
    // ----------------------------------------------------------------

    @Test
    void multipleRecords_allProcessedSequentially() throws IOException {
        String rec1 = buildRecord(
                "00000000001", "Y",
                "00000001940{", "00000020200{", "00000010200{",
                "2014-11-20", "2025-05-20", "2025-05-20",
                "00000000000{", "00000000000{", "", "A000000000");
        String rec2 = buildRecord(
                "00000000002", "Y",
                "00000001580{", "00000061300{", "00000054480{",
                "2013-06-19", "2024-08-11", "2024-08-11",
                "00000000000{", "00000000000{", "", "A000000000");
        String rec3 = buildRecord(
                "00000000003", "Y",
                "00000001470{", "00000049090{", "00000005380{",
                "2013-08-23", "2024-01-10", "2024-01-10",
                "00000000000{", "00000000000{", "", "A000000000");

        Files.writeString(acctFile, rec1 + "\n" + rec2 + "\n" + rec3 + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        int count = app.execute();

        assertEquals(3, count);

        // OUTFILE should have 3 lines
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(3, outLines.size());

        // Verify record 2 output
        String[] out2 = outLines.get(1).split("\\|");
        assertEquals("00000000002", out2[0]);
        assertEquals("158.00", out2[2]);              // currBal
        assertEquals("6130.00", out2[3]);             // creditLimit
        assertEquals("5448.00", out2[4]);             // cashCreditLimit
        assertEquals("20240811", out2[7]);            // reformatted reissue date
        assertEquals("2525.00", out2[9]);             // debit defaulted to 2525

        // Verify record 3 output
        String[] out3 = outLines.get(2).split("\\|");
        assertEquals("00000000003", out3[0]);
        assertEquals("147.00", out3[2]);
        assertEquals("20240110", out3[7]);

        // ARRYFILE should have 3 lines
        assertEquals(3, Files.readAllLines(arryFile).size());

        // VBRCFILE should have 6 lines (2 per account)
        assertEquals(6, Files.readAllLines(vbrcFile).size());
    }

    // ----------------------------------------------------------------
    // Test 3: Non-zero debit — should NOT be replaced with 2525.00
    // ----------------------------------------------------------------

    @Test
    void nonZeroDebit_shouldBeKeptAsIs() throws IOException {
        // Use a positive sign digit 'E' = +5, so value = 00000012345 -> 123.45
        String rec = buildRecord(
                "00000000099", "Y",
                "00000005000{", "00000030000{", "00000015000{",
                "2020-01-15", "2026-01-15", "2026-01-15",
                "00000000100{", "00000001234E",
                "12345", "B000000001");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        assertEquals("123.45", fields[9]); // NOT 2525.00
    }

    // ----------------------------------------------------------------
    // Test 4: Negative balance values
    // ----------------------------------------------------------------

    @Test
    void negativeBalance_parsedCorrectly() throws IOException {
        // '}' = -0, so 00000005000} = -500.00
        String rec = buildRecord(
                "00000000077", "N",
                "00000005000}", "00000030000{", "00000015000{",
                "2020-01-15", "2026-01-15", "2026-01-15",
                "00000000000{", "00000000500J",
                "", "C000000002");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String[] fields = outLines.get(0).split("\\|");
        assertEquals("-500.00", fields[2]);          // negative currBal
        assertEquals("-50.01", fields[9]);           // negative debit, not replaced
    }

    // ----------------------------------------------------------------
    // Test 5: Empty file — no records processed
    // ----------------------------------------------------------------

    @Test
    void emptyInputFile_noOutputGenerated() throws IOException {
        Files.writeString(acctFile, "");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        int count = app.execute();

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arryFile).size());
        assertEquals(0, Files.readAllLines(vbrcFile).size());
    }

    // ----------------------------------------------------------------
    // Test 6: Array record structure verification
    // ----------------------------------------------------------------

    @Test
    void arrayRecord_differentBalance_slotsCorrect() throws IOException {
        // currBal = 345.00
        String rec = buildRecord(
                "00000000055", "Y",
                "00000003450{", "00000038190{", "00000024300{",
                "2012-10-03", "2025-03-09", "2025-03-09",
                "00000000000{", "00000000000{", "", "A000000000");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> arryLines = Files.readAllLines(arryFile);
        String[] arrFields = arryLines.get(0).split("\\|");

        // Slot 1 & 2: balance from account
        assertEquals("345.00", arrFields[1]);
        assertEquals("1005.00", arrFields[2]);
        assertEquals("345.00", arrFields[3]);
        assertEquals("1525.00", arrFields[4]);
        // Slot 3: fixed negatives
        assertEquals("-1025.00", arrFields[5]);
        assertEquals("-2500.00", arrFields[6]);
    }

    // ----------------------------------------------------------------
    // Test 7: VBR record year extraction
    // ----------------------------------------------------------------

    @Test
    void vbrRecord_extractsReissueYear() throws IOException {
        String rec = buildRecord(
                "00000000042", "Y",
                "00000003020{", "00000065630{", "00000051030{",
                "2016-09-19", "2025-09-19", "2025-09-19",
                "00000000000{", "00000000000{", "", "A000000000");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        // VBR record 2 should have the year
        String[] vb2 = vbrcLines.get(1).split("\\|");
        assertEquals("2025", vb2[3]);
    }

    // ----------------------------------------------------------------
    // Test 8: Full acctdata.txt integration test (uses real sample data)
    // ----------------------------------------------------------------

    @Test
    void fullSampleData_processesAll50Records() throws IOException {
        // Copy the real acctdata.txt from the project
        Path realData = Path.of("../../data/ASCII/acctdata.txt");
        if (!Files.exists(realData)) {
            // Try alternate path from project root
            realData = Path.of("../data/ASCII/acctdata.txt");
        }
        if (!Files.exists(realData)) {
            // Skip if data file not accessible from test working directory
            realData = findAcDataFile();
        }
        if (realData == null || !Files.exists(realData)) {
            // Create a minimal dataset for CI environments
            String rec = buildRecord(
                    "00000000001", "Y",
                    "00000001940{", "00000020200{", "00000010200{",
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    "00000000000{", "00000000000{", "", "A000000000");
            Files.writeString(acctFile, rec + "\n");

            Cbact01cApplication app = new Cbact01cApplication(
                    acctFile, outFile, arryFile, vbrcFile);
            int count = app.execute();
            assertTrue(count >= 1);
            return;
        }

        Files.copy(realData, acctFile);

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        int count = app.execute();

        assertEquals(50, count);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(50, outLines.size());

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(50, arryLines.size());

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(100, vbrcLines.size()); // 2 per account

        // Spot-check last record (account 50)
        String[] last = outLines.get(49).split("\\|");
        assertEquals("00000000050", last[0]);
    }

    private static Path findAcDataFile() {
        // Walk up from current directory to find the data file
        Path current = Path.of("").toAbsolutePath();
        for (int i = 0; i < 10; i++) {
            Path candidate = current.resolve("app/data/ASCII/acctdata.txt");
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
            if (current == null) break;
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Test 9: Inactive account status preserved
    // ----------------------------------------------------------------

    @Test
    void inactiveStatus_preservedInAllOutputs() throws IOException {
        String rec = buildRecord(
                "00000000088", "N",
                "00000000100{", "00000005000{", "00000002000{",
                "2018-03-15", "2023-03-15", "2023-03-15",
                "00000000000{", "00000000050{", "", "D000000003");
        Files.writeString(acctFile, rec + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        // OUTFILE preserves status
        String[] outFields = Files.readAllLines(outFile).get(0).split("\\|");
        assertEquals("N", outFields[1]);

        // VBRCFILE record 1 preserves status
        String[] vb1 = Files.readAllLines(vbrcFile).get(0).split("\\|");
        assertEquals("N", vb1[1]);
    }

    // ----------------------------------------------------------------
    // Test 10: Date reformatting for various date patterns
    // ----------------------------------------------------------------

    @Test
    void dateReformatting_variousDates() throws IOException {
        String rec1 = buildRecord(
                "00000000010", "Y",
                "00000001590{", "00000054010{", "00000044420{",
                "2015-09-13", "2023-01-27", "2023-01-27",
                "00000000000{", "00000000000{", "", "A000000000");
        String rec2 = buildRecord(
                "00000000016", "Y",
                "00000007330{", "00000089220{", "00000026320{",
                "2014-09-11", "2024-01-25", "2024-01-25",
                "00000000000{", "00000000000{", "", "A000000000");

        Files.writeString(acctFile, rec1 + "\n" + rec2 + "\n");

        Cbact01cApplication app = new Cbact01cApplication(
                acctFile, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals("20230127", outLines.get(0).split("\\|")[7]);
        assertEquals("20240125", outLines.get(1).split("\\|")[7]);
    }
}
