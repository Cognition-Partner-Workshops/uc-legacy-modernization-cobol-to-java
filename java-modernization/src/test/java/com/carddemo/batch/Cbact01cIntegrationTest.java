package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.service.AccountFileProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that run the full CBACT01C batch job against sample
 * account data derived from the original COBOL test data in
 * {@code app/data/ASCII/acctdata.txt}.
 *
 * <p>These tests verify that the Java version produces <em>identical</em>
 * logical results to the COBOL version for a known set of inputs.
 */
class Cbact01cIntegrationTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    /**
     * Sample account records in the same zoned-decimal ASCII format as the
     * original COBOL flat file. These are the first 5 records from
     * {@code app/data/ASCII/acctdata.txt}.
     */
    private static final String[] SAMPLE_RECORDS = {
            // Account 1: zero debit → 2525.00 substitution; reissue 2025-05-20 → 20250520
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{",
            // Account 2: zero debit; reissue 2024-08-11 → 20240811
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{",
            // Account 3: zero debit; reissue 2024-01-10 → 20240110
            "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{",
            // Account 4: zero debit; reissue 2023-12-16 → 20231216
            "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{",
            // Account 5: zero debit; reissue 2025-03-09 → 20250309
            "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{",
    };

    @BeforeEach
    void setUp() throws IOException {
        acctFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("outfile.txt");
        arryFile = tempDir.resolve("arryfile.txt");
        vbrcFile = tempDir.resolve("vbrcfile.txt");

        // Pad each record to 300 chars (COBOL record length) and write
        StringBuilder sb = new StringBuilder();
        for (String rec : SAMPLE_RECORDS) {
            sb.append(String.format("%-300s", rec)).append(System.lineSeparator());
        }
        Files.writeString(acctFile, sb.toString());
    }

    @Test
    @DisplayName("Full batch run completes with exit code 0")
    void batchRunSucceeds() {
        int exitCode = Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("Output file contains exactly 5 records")
    void outFileRecordCount() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(outFile);
        assertEquals(5, lines.size());
    }

    @Test
    @DisplayName("Array file contains exactly 5 records")
    void arryFileRecordCount() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(arryFile);
        assertEquals(5, lines.size());
    }

    @Test
    @DisplayName("VBR file contains exactly 10 records (2 per account)")
    void vbrcFileRecordCount() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(vbrcFile);
        assertEquals(10, lines.size());
    }

    // ── OUTFILE content verification ────────────────────────────────────

    @Test
    @DisplayName("Account 1 output: reissue date converted, debit = 2525.00")
    void account1OutputRecord() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(outFile);
        String[] fields = lines.get(0).split("\\|");

        assertEquals("00000000001", fields[0]);                   // ACCT-ID
        assertEquals("Y", fields[1]);                              // ACTIVE-STATUS
        assertEquals("194.00", fields[2]);                         // CURR-BAL
        assertEquals("2020.00", fields[3]);                        // CREDIT-LIMIT
        assertEquals("1020.00", fields[4]);                        // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", fields[5]);                     // OPEN-DATE
        assertEquals("2025-05-20", fields[6]);                     // EXPIRATION-DATE
        assertEquals("20250520", fields[7]);                       // REISSUE-DATE (converted!)
        assertEquals("0.00", fields[8]);                           // CURR-CYC-CREDIT
        assertEquals("2525.00", fields[9]);                        // CURR-CYC-DEBIT (substituted!)
    }

    @Test
    @DisplayName("Account 3 output: reissue date = 20240110, debit = 2525.00")
    void account3OutputRecord() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(outFile);
        String[] fields = lines.get(2).split("\\|");

        assertEquals("00000000003", fields[0]);
        assertEquals("147.00", fields[2]);
        assertEquals("20240110", fields[7]);
        assertEquals("2525.00", fields[9]);
    }

    // ── ARRYFILE content verification ───────────────────────────────────

    @Test
    @DisplayName("Account 1 array record: slots populated per COBOL paragraph 1400")
    void account1ArrayRecord() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(arryFile);
        String[] fields = lines.get(0).split("\\|");

        // ACCT-ID
        assertEquals("00000000001", fields[0]);
        // Slot 1: bal = 194.00, debit = 1005.00
        assertEquals("194.00", fields[1]);
        assertEquals("1005.00", fields[2]);
        // Slot 2: bal = 194.00, debit = 1525.00
        assertEquals("194.00", fields[3]);
        assertEquals("1525.00", fields[4]);
        // Slot 3: bal = -1025.00, debit = -2500.00
        assertEquals("-1025.00", fields[5]);
        assertEquals("-2500.00", fields[6]);
        // Slot 4: bal = 0, debit = 0
        assertEquals("0", fields[7]);
        assertEquals("0", fields[8]);
        // Slot 5: bal = 0, debit = 0
        assertEquals("0", fields[9]);
        assertEquals("0", fields[10]);
    }

    @Test
    @DisplayName("Account 5 array record: balance = 345.00 in slots 1-2")
    void account5ArrayRecord() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(arryFile);
        String[] fields = lines.get(4).split("\\|");

        assertEquals("00000000005", fields[0]);
        assertEquals("345.00", fields[1]);  // slot 1 bal
        assertEquals("345.00", fields[3]);  // slot 2 bal
    }

    // ── VBRCFILE content verification ───────────────────────────────────

    @Test
    @DisplayName("Account 1 VBR records: VB1 (ID+status) and VB2 (ID+bal+limit+year)")
    void account1VbrRecords() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(vbrcFile);

        // VB1 record (line 0 for account 1)
        String[] vb1Fields = lines.get(0).split("\\|");
        assertEquals("00000000001", vb1Fields[0]);
        assertEquals("Y", vb1Fields[1]);

        // VB2 record (line 1 for account 1)
        String[] vb2Fields = lines.get(1).split("\\|");
        assertEquals("00000000001", vb2Fields[0]);
        assertEquals("194.00", vb2Fields[1]);      // CURR-BAL
        assertEquals("2020.00", vb2Fields[2]);      // CREDIT-LIMIT
        assertEquals("2025", vb2Fields[3]);          // REISSUE-YYYY
    }

    @Test
    @DisplayName("Account 4 VB2: reissue year = 2023")
    void account4VbrReissueYear() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);
        List<String> lines = Files.readAllLines(vbrcFile);

        // Account 4 VB2 is at line index 7 (4th account × 2 records - 1)
        String[] vb2Fields = lines.get(7).split("\\|");
        assertEquals("00000000004", vb2Fields[0]);
        assertEquals("2023", vb2Fields[3]);
    }

    // ── Cross-file consistency ──────────────────────────────────────────

    @Test
    @DisplayName("All three files have consistent account IDs")
    void consistentAccountIds() throws IOException {
        Cbact01cApplication.run(acctFile, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arryLines = Files.readAllLines(arryFile);
        List<String> vbrcLines = Files.readAllLines(vbrcFile);

        for (int i = 0; i < 5; i++) {
            String outId = outLines.get(i).split("\\|")[0];
            String arrId = arryLines.get(i).split("\\|")[0];
            String vb1Id = vbrcLines.get(i * 2).split("\\|")[0];
            String vb2Id = vbrcLines.get(i * 2 + 1).split("\\|")[0];

            assertEquals(outId, arrId, "ARRY account ID mismatch at record " + i);
            assertEquals(outId, vb1Id, "VB1 account ID mismatch at record " + i);
            assertEquals(outId, vb2Id, "VB2 account ID mismatch at record " + i);
        }
    }

    // ── Edge case: non-zero debit ───────────────────────────────────────

    @Test
    @DisplayName("Non-zero debit is preserved (not replaced with 2525.00)")
    void nonZeroDebitPreserved() throws IOException {
        // Create a single-record file with a non-zero debit
        // ACCT-CURR-CYC-DEBIT = 123.45 → zoned decimal "00000001234E"
        // (E = +5, so last digit is 5, value = 000000012345 → 123.45)
        String record = "00000000099Y00000001000{00000050000{00000025000{" +
                "2020-01-152030-01-152029-06-30" +
                "00000001500{00000001234E";
        Path singleFile = tempDir.resolve("single.txt");
        Files.writeString(singleFile, String.format("%-300s", record) + System.lineSeparator());

        Path out2 = tempDir.resolve("out2.txt");
        Path arry2 = tempDir.resolve("arry2.txt");
        Path vbrc2 = tempDir.resolve("vbrc2.txt");

        int exitCode = Cbact01cApplication.run(singleFile, out2, arry2, vbrc2);
        assertEquals(0, exitCode);

        List<String> outLines = Files.readAllLines(out2);
        String[] fields = outLines.get(0).split("\\|");
        // Debit should NOT be 2525.00 because input was 123.45
        assertEquals("123.45", fields[9]);
    }

    // ── Error handling ──────────────────────────────────────────────────

    @Test
    @DisplayName("Non-existent input file returns error code 999")
    void missingInputFile() {
        Path missing = tempDir.resolve("nonexistent.txt");
        int exitCode = Cbact01cApplication.run(missing, outFile, arryFile, vbrcFile);
        assertEquals(999, exitCode);
    }

    // ── Full 50-record sample test ──────────────────────────────────────

    @Test
    @DisplayName("Process all 50 sample records from acctdata.txt")
    void fullSampleFile() throws IOException {
        // All 50 records from the original COBOL test data
        String[] allRecords = {
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{",
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{",
            "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{",
            "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{",
            "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{",
            "00000000006Y00000002180{00000035840{00000029480{2017-12-232025-10-082025-10-0800000000000{00000000000{",
            "00000000007Y00000001930{00000020650{00000002640{2012-10-122024-12-132024-12-1300000000000{00000000000{",
            "00000000008Y00000006050{00000061040{00000013180{2012-01-042024-05-202024-05-2000000000000{00000000000{",
            "00000000009Y00000005600{00000082010{00000020650{2016-08-272024-12-272024-12-2700000000000{00000000000{",
            "00000000010Y00000001590{00000054010{00000044420{2015-09-132023-01-272023-01-2700000000000{00000000000{",
            "00000000011Y00000002120{00000049980{00000031750{2014-09-122025-03-122025-03-1200000000000{00000000000{",
            "00000000012Y00000001760{00000046360{00000003880{2009-06-172023-07-072023-07-0700000000000{00000000000{",
            "00000000013Y00000000410{00000075420{00000049220{2017-10-012024-08-042024-08-0400000000000{00000000000{",
            "00000000014Y00000000150{00000022540{00000002120{2010-12-042025-12-112025-12-1100000000000{00000000000{",
            "00000000015Y00000004890{00000084410{00000038330{2009-10-062025-06-092025-06-0900000000000{00000000000{",
            "00000000016Y00000007330{00000089220{00000026320{2014-09-112024-01-252024-01-2500000000000{00000000000{",
            "00000000017Y00000000330{00000005680{00000005100{2014-05-172025-03-012025-03-0100000000000{00000000000{",
            "00000000018Y00000001440{00000029030{00000014960{2018-11-152023-09-102023-09-1000000000000{00000000000{",
            "00000000019Y00000004800{00000069860{00000037230{2011-12-142025-07-232025-07-2300000000000{00000000000{",
            "00000000020Y00000003690{00000037670{00000010400{2014-02-272024-03-132024-03-1300000000000{00000000000{",
            "00000000021Y00000001120{00000012640{00000001800{2011-10-192023-01-062023-01-0600000000000{00000000000{",
            "00000000022Y00000000550{00000085990{00000047120{2016-11-212025-12-282025-12-2800000000000{00000000000{",
            "00000000023Y00000001040{00000033770{00000029040{2012-03-152025-03-182025-03-1800000000000{00000000000{",
            "00000000024Y00000004000{00000051740{00000041290{2015-08-082025-02-112025-02-1100000000000{00000000000{",
            "00000000025Y00000000610{00000081940{00000065820{2012-10-262025-07-102025-07-1000000000000{00000000000{",
            "00000000026Y00000000460{00000021810{00000013750{2009-04-202024-12-192024-12-1900000000000{00000000000{",
            "00000000027Y00000002840{00000055720{00000020750{2012-09-302025-07-132025-07-1300000000000{00000000000{",
            "00000000028Y00000000680{00000008680{00000005470{2015-05-202024-05-092024-05-0900000000000{00000000000{",
            "00000000029Y00000003390{00000055110{00000043610{2015-11-032024-06-042024-06-0400000000000{00000000000{",
            "00000000030Y00000000020{00000001200{00000000930{2011-08-262024-06-272024-06-2700000000000{00000000000{",
            "00000000031Y00000000310{00000011400{00000010770{2017-02-252025-06-082025-06-0800000000000{00000000000{",
            "00000000032Y00000000300{00000011750{00000008460{2013-11-102025-05-192025-05-1900000000000{00000000000{",
            "00000000033Y00000004100{00000064040{00000009510{2012-10-112025-10-072025-10-0700000000000{00000000000{",
            "00000000034Y00000002530{00000036420{00000027700{2009-05-102025-10-062025-10-0600000000000{00000000000{",
            "00000000035Y00000001660{00000019470{00000015250{2018-02-022025-09-232025-09-2300000000000{00000000000{",
            "00000000036Y00000001100{00000033280{00000008390{2018-07-182024-12-232024-12-2300000000000{00000000000{",
            "00000000037Y00000000070{00000004460{00000001660{2016-09-102023-10-242023-10-2400000000000{00000000000{",
            "00000000038Y00000006120{00000065050{00000034760{2010-08-122023-07-232023-07-2300000000000{00000000000{",
            "00000000039Y00000008430{00000097500{00000062120{2018-08-262025-09-082025-09-0800000000000{00000000000{",
            "00000000040Y00000000430{00000058230{00000016740{2010-02-132023-10-272023-10-2700000000000{00000000000{",
            "00000000041Y00000003750{00000067210{00000034290{2015-02-072023-04-242023-04-2400000000000{00000000000{",
            "00000000042Y00000003020{00000065630{00000051030{2016-09-192025-09-192025-09-1900000000000{00000000000{",
            "00000000043Y00000006100{00000061680{00000012060{2012-04-092025-08-292025-08-2900000000000{00000000000{",
            "00000000044Y00000002630{00000068990{00000044320{2018-12-012024-01-172024-01-1700000000000{00000000000{",
            "00000000045Y00000001860{00000027190{00000006880{2010-12-312025-07-092025-07-0900000000000{00000000000{",
            "00000000046Y00000003960{00000070070{00000054380{2013-09-062025-06-202025-06-2000000000000{00000000000{",
            "00000000047Y00000000320{00000023380{00000001590{2014-04-032025-08-232025-08-2300000000000{00000000000{",
            "00000000048Y00000002260{00000023060{00000006120{2017-03-182025-02-062025-02-0600000000000{00000000000{",
            "00000000049Y00000001000{00000090480{00000048070{2019-04-062023-09-172023-09-1700000000000{00000000000{",
            "00000000050Y00000004920{00000061690{00000045870{2011-04-222023-03-092023-03-0900000000000{00000000000{",
        };

        Path fullFile = tempDir.resolve("full_acctdata.txt");
        StringBuilder sb = new StringBuilder();
        for (String rec : allRecords) {
            sb.append(String.format("%-300s", rec)).append(System.lineSeparator());
        }
        Files.writeString(fullFile, sb.toString());

        Path out50 = tempDir.resolve("out50.txt");
        Path arry50 = tempDir.resolve("arry50.txt");
        Path vbrc50 = tempDir.resolve("vbrc50.txt");

        int exitCode = Cbact01cApplication.run(fullFile, out50, arry50, vbrc50);
        assertEquals(0, exitCode);

        // Verify counts
        assertEquals(50, Files.readAllLines(out50).size());
        assertEquals(50, Files.readAllLines(arry50).size());
        assertEquals(100, Files.readAllLines(vbrc50).size()); // 2 per account

        // Spot-check last record (account 50)
        List<String> outLines = Files.readAllLines(out50);
        String[] lastOut = outLines.get(49).split("\\|");
        assertEquals("00000000050", lastOut[0]);
        assertEquals("492.00", lastOut[2]);                    // CURR-BAL
        assertEquals("20230309", lastOut[7]);                   // REISSUE-DATE converted
        assertEquals("2525.00", lastOut[9]);                    // zero debit → 2525.00

        // Spot-check VBR for account 25
        List<String> vbrcLines = Files.readAllLines(vbrc50);
        String[] vb2_25 = vbrcLines.get(49).split("\\|");      // account 25 VB2 at index 49
        assertEquals("00000000025", vb2_25[0]);
        assertEquals("61.00", vb2_25[1]);                       // CURR-BAL
        assertEquals("8194.00", vb2_25[2]);                     // CREDIT-LIMIT
        assertEquals("2025", vb2_25[3]);                        // REISSUE-YYYY
    }
}
