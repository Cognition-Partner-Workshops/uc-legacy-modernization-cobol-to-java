package com.cardemo.batch.cbact01c;

import com.cardemo.batch.cbact01c.io.AccountFileReader;
import com.cardemo.batch.cbact01c.io.RecordFormatter;
import com.cardemo.batch.cbact01c.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test that feeds sample account data through the
 * full pipeline and verifies the output files contain the correct records.
 *
 * The test data mirrors rows from the repository's
 * {@code app/data/ASCII/acctdata.txt} file.
 */
class Cbact01cIntegrationTest {

    private static final String SAMPLE_LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{"
                    + "2014-11-202025-05-202025-05-20"
                    + "00000000000{00000000000{A000000000";

    private static final String SAMPLE_LINE_2 =
            "00000000002Y00000001580{00000061300{00000054480{"
                    + "2013-06-192024-08-112024-08-11"
                    + "00000000000{00000000000{A000000000";

    private static final String SAMPLE_LINE_3 =
            "00000000003Y00000001470{00000049090{00000005380{"
                    + "2013-08-232024-01-102024-01-10"
                    + "00000000000{00000000000{A000000000";

    @Test
    void fullPipeline_threeRecords(@TempDir Path tmpDir) throws IOException {
        // Arrange: write sample input (pad each line to 300 chars)
        Path inputFile = tmpDir.resolve("acctfile.dat");
        Files.writeString(inputFile,
                padLine(SAMPLE_LINE_1) + "\n"
                        + padLine(SAMPLE_LINE_2) + "\n"
                        + padLine(SAMPLE_LINE_3) + "\n");

        Path outFile = tmpDir.resolve("outfile.dat");
        Path arryFile = tmpDir.resolve("arryfile.dat");
        Path vbrcFile = tmpDir.resolve("vbrcfile.dat");

        // Act
        AccountFileProcessor.ProcessingResult result =
                AccountFileProcessor.execute(inputFile, outFile, arryFile, vbrcFile);

        // Assert: correct record counts
        assertEquals(3, result.outputRecords().size());
        assertEquals(3, result.arrayRecords().size());
        assertEquals(3, result.vbRecords1().size());
        assertEquals(3, result.vbRecords2().size());

        // Assert: output file exists and has 3 lines
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(3, outLines.size());

        // Assert: array file has 3 lines
        List<String> arrLines = Files.readAllLines(arryFile);
        assertEquals(3, arrLines.size());

        // Assert: vbrc file has 6 lines (2 per account: VB1 + VB2)
        List<String> vbrLines = Files.readAllLines(vbrcFile);
        assertEquals(6, vbrLines.size());
    }

    @Test
    void outputFile_record1_matchesCobolLogic(@TempDir Path tmpDir) throws IOException {
        Path inputFile = tmpDir.resolve("acctfile.dat");
        Files.writeString(inputFile, padLine(SAMPLE_LINE_1) + "\n");

        Path outFile = tmpDir.resolve("outfile.dat");
        Path arryFile = tmpDir.resolve("arryfile.dat");
        Path vbrcFile = tmpDir.resolve("vbrcfile.dat");

        AccountFileProcessor.execute(inputFile, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        String line = outLines.get(0);

        // COBOL 1300-POPUL-ACCT-RECORD logic for record #1:
        //   OUT-ACCT-ID             = 00000000001
        //   OUT-ACCT-ACTIVE-STATUS  = Y
        //   OUT-ACCT-CURR-BAL       = 00000001940{  (194.00)
        //   OUT-ACCT-CREDIT-LIMIT   = 00000020200{  (2020.00)
        //   OUT-ACCT-CASH-CREDIT-LIMIT = 00000010200{ (1020.00)
        //   OUT-ACCT-OPEN-DATE      = 2014-11-20
        //   OUT-ACCT-EXPIRAION-DATE = 2025-05-20
        //   OUT-ACCT-REISSUE-DATE   = 20250520   (COBDATFT: YYYY-MM-DD→YYYYMMDD, padded to 10)
        //   OUT-ACCT-CURR-CYC-CREDIT = 00000000000{ (0.00)
        //   OUT-ACCT-CURR-CYC-DEBIT  = 00000025250{ (2525.00 default when source is 0)
        //   OUT-ACCT-GROUP-ID        = spaces (10 chars)

        assertTrue(line.startsWith("00000000001Y"));
        assertTrue(line.contains("00000001940{")); // curr bal
        assertTrue(line.contains("00000020200{")); // credit limit
        assertTrue(line.contains("00000010200{")); // cash credit limit
        assertTrue(line.contains("2014-11-20"));   // open date
        assertTrue(line.contains("20250520"));      // converted reissue date
        assertTrue(line.contains("00000025250{")); // defaulted cycle debit
    }

    @Test
    void arrayFile_record1_matchesCobolLogic(@TempDir Path tmpDir) throws IOException {
        Path inputFile = tmpDir.resolve("acctfile.dat");
        Files.writeString(inputFile, padLine(SAMPLE_LINE_1) + "\n");

        Path outFile = tmpDir.resolve("outfile.dat");
        Path arryFile = tmpDir.resolve("arryfile.dat");
        Path vbrcFile = tmpDir.resolve("vbrcfile.dat");

        AccountFileProcessor.execute(inputFile, outFile, arryFile, vbrcFile);

        List<String> arrLines = Files.readAllLines(arryFile);
        String line = arrLines.get(0);

        // Verify account id
        assertTrue(line.startsWith("00000000001"));

        // Verify it contains the fixed debit values (zoned decimal formatted)
        // Entry 1 debit: 1005.00 → "00000010050{"
        // Entry 2 debit: 1525.00 → "00000015250{"
        // Entry 3 bal:  -1025.00 → "00000010250}" (negative)
        // Entry 3 debit: -2500.00 → "00000025000}" (negative)
        assertTrue(line.contains("00000010050{"));  // arr debit 1
        assertTrue(line.contains("00000015250{"));  // arr debit 2
        assertTrue(line.contains("00000010250}"));  // arr bal 3 (negative)
        assertTrue(line.contains("00000025000}"));  // arr debit 3 (negative)
    }

    @Test
    void vbrcFile_record1_matchesCobolLogic(@TempDir Path tmpDir) throws IOException {
        Path inputFile = tmpDir.resolve("acctfile.dat");
        Files.writeString(inputFile, padLine(SAMPLE_LINE_1) + "\n");

        Path outFile = tmpDir.resolve("outfile.dat");
        Path arryFile = tmpDir.resolve("arryfile.dat");
        Path vbrcFile = tmpDir.resolve("vbrcfile.dat");

        AccountFileProcessor.execute(inputFile, outFile, arryFile, vbrcFile);

        List<String> vbrLines = Files.readAllLines(vbrcFile);

        // VB1 record: "00000000001Y" (12 chars)
        assertEquals("00000000001Y", vbrLines.get(0));

        // VB2 record: acct-id + curr-bal + credit-limit + reissue-year
        String vb2 = vbrLines.get(1);
        assertTrue(vb2.startsWith("00000000001"));
        assertTrue(vb2.contains("00000001940{"));  // curr bal
        assertTrue(vb2.contains("00000020200{"));  // credit limit
        assertTrue(vb2.endsWith("2025"));           // reissue year
    }

    @Test
    void process_allSampleAccounts_allHaveDefaultedDebit(@TempDir Path tmpDir) throws IOException {
        // All 3 sample accounts have ACCT-CURR-CYC-DEBIT = 0, so all should get 2525.00
        Path inputFile = tmpDir.resolve("acctfile.dat");
        Files.writeString(inputFile,
                padLine(SAMPLE_LINE_1) + "\n"
                        + padLine(SAMPLE_LINE_2) + "\n"
                        + padLine(SAMPLE_LINE_3) + "\n");

        Path outFile = tmpDir.resolve("outfile.dat");
        Path arryFile = tmpDir.resolve("arryfile.dat");
        Path vbrcFile = tmpDir.resolve("vbrcfile.dat");

        AccountFileProcessor.ProcessingResult result =
                AccountFileProcessor.execute(inputFile, outFile, arryFile, vbrcFile);

        for (OutputAccountRecord r : result.outputRecords()) {
            assertEquals(0, new BigDecimal("2525.00").compareTo(r.currCycDebit()),
                    "Account " + r.acctId() + " should have defaulted debit of 2525.00");
        }
    }

    @Test
    void reissueDateConversion_multipleAccounts() {
        // Account 1: reissue "2025-05-20" → "20250520  "
        // Account 2: reissue "2024-08-11" → "20240811  "
        // Account 3: reissue "2024-01-10" → "20240110  "
        AccountRecord a1 = AccountFileReader.parseLine(padLine(SAMPLE_LINE_1));
        AccountRecord a2 = AccountFileReader.parseLine(padLine(SAMPLE_LINE_2));
        AccountRecord a3 = AccountFileReader.parseLine(padLine(SAMPLE_LINE_3));

        assertEquals("20250520  ",
                AccountFileProcessor.buildOutputRecord(a1).reissueDate());
        assertEquals("20240811  ",
                AccountFileProcessor.buildOutputRecord(a2).reissueDate());
        assertEquals("20240110  ",
                AccountFileProcessor.buildOutputRecord(a3).reissueDate());
    }

    @Test
    void extractReissueYear_fromDashFormat() {
        assertEquals("2025", AccountFileProcessor.extractReissueYear("2025-05-20"));
        assertEquals("2024", AccountFileProcessor.extractReissueYear("2024-08-11"));
    }

    @Test
    void formatterRoundTrip_outputRecord() {
        AccountRecord acct = AccountFileReader.parseLine(padLine(SAMPLE_LINE_1));
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct);
        String formatted = RecordFormatter.formatOutput(out);

        // The formatted line should start with the account id and status
        assertTrue(formatted.startsWith("00000000001Y"));
        // And contain the defaulted debit
        assertTrue(formatted.contains("00000025250{"));
    }

    private static String padLine(String line) {
        if (line.length() >= AccountRecord.RECORD_LENGTH) return line;
        return line + " ".repeat(AccountRecord.RECORD_LENGTH - line.length());
    }
}
