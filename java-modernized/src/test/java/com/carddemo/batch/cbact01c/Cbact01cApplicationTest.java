package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.io.AccountFileReader;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.service.AccountProcessor;
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
 * End-to-end integration tests for the CBACT01C Java application.
 *
 * <p>These tests verify that the Java version produces results identical
 * to what the COBOL program would produce for the same sample inputs.
 * Each test uses the actual account data lines from the CardDemo sample
 * files and verifies the output CSV content.
 */
class Cbact01cApplicationTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    /**
     * First 5 records from app/data/ASCII/acctdata.txt, used as sample
     * input for integration testing.
     */
    private static final String[] SAMPLE_LINES = {
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000",
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000",
            "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000",
            "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{A000000000",
            "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{A000000000",
    };

    @BeforeEach
    void setUp() throws IOException {
        acctFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("outfile.csv");
        arryFile = tempDir.resolve("arryfile.csv");
        vbrcFile = tempDir.resolve("vbrcfile.csv");

        StringBuilder sb = new StringBuilder();
        for (String line : SAMPLE_LINES) {
            // Pad each line to 300 characters (COBOL record length)
            sb.append(line);
            if (line.length() < 300) {
                sb.append(" ".repeat(300 - line.length()));
            }
            sb.append('\n');
        }
        Files.writeString(acctFile, sb.toString());
    }

    @Test
    void process_shouldReturn5Records() throws IOException {
        int count = Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);
        assertEquals(5, count);
    }

    @Test
    void process_shouldCreateAllOutputFiles() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        assertTrue(Files.exists(outFile), "OUT file should exist");
        assertTrue(Files.exists(arryFile), "ARRAY file should exist");
        assertTrue(Files.exists(vbrcFile), "VBR file should exist");
    }

    // ---------------------------------------------------------------
    // OUT file verification (COBOL 1300-POPUL-ACCT-RECORD equivalence)
    // ---------------------------------------------------------------

    @Test
    void outFile_shouldHaveHeaderAndDataLines() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        assertEquals(6, lines.size(), "1 header + 5 data lines");
        assertTrue(lines.get(0).startsWith("ACCT_ID,"), "First line should be header");
    }

    @Test
    void outFile_record1_shouldHaveConvertedDateAndDefaultDebit() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        String[] fields = lines.get(1).split(",");

        // ACCT-ID
        assertEquals("00000000001", fields[0]);
        // ACTIVE-STATUS
        assertEquals("Y", fields[1]);
        // CURR-BAL (PIC S9(10)V99: 00000001940{ = 194.00)
        assertEquals("194.00", fields[2]);
        // CREDIT-LIMIT
        assertEquals("2020.00", fields[3]);
        // CASH-CREDIT-LIMIT
        assertEquals("1020.00", fields[4]);
        // OPEN-DATE
        assertEquals("2014-11-20", fields[5]);
        // EXPIRATION-DATE
        assertEquals("2025-05-20", fields[6]);
        // REISSUE-DATE — converted from YYYY-MM-DD to YYYYMMDD
        assertEquals("20250520", fields[7]);
        // CURR-CYC-CREDIT
        assertEquals("0.00", fields[8]);
        // CURR-CYC-DEBIT — was zero, should be substituted with 2525.00
        assertEquals("2525.00", fields[9]);
    }

    @Test
    void outFile_allRecords_shouldHaveDefaultDebitWhenZero() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        // All 5 sample records have zero debit, so all should get 2525.00
        for (int i = 1; i <= 5; i++) {
            String[] fields = lines.get(i).split(",");
            assertEquals("2525.00", fields[9],
                    "Record " + i + " should have default debit 2525.00");
        }
    }

    @Test
    void outFile_record3_shouldHaveCorrectValues() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        String[] fields = lines.get(3).split(",");

        assertEquals("00000000003", fields[0]);
        assertEquals("147.00", fields[2]);    // CURR-BAL
        assertEquals("4909.00", fields[3]);   // CREDIT-LIMIT
        assertEquals("538.00", fields[4]);    // CASH-CREDIT-LIMIT
        assertEquals("20240110", fields[7]);  // REISSUE-DATE converted
    }

    // ---------------------------------------------------------------
    // ARRAY file verification (COBOL 1400-POPUL-ARRAY-RECORD equivalence)
    // ---------------------------------------------------------------

    @Test
    void arryFile_shouldHaveHeaderAndDataLines() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(arryFile);
        assertEquals(6, lines.size(), "1 header + 5 data lines");
    }

    @Test
    void arryFile_record1_shouldHaveCorrectArrayValues() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(arryFile);
        String[] fields = lines.get(1).split(",");

        // ACCT-ID
        assertEquals("00000000001", fields[0]);
        // Element 1: actual balance, 1005.00 debit
        assertEquals("194.00", fields[1]);
        assertEquals("1005.00", fields[2]);
        // Element 2: actual balance, 1525.00 debit
        assertEquals("194.00", fields[3]);
        assertEquals("1525.00", fields[4]);
        // Element 3: -1025.00 balance, -2500.00 debit
        assertEquals("-1025.00", fields[5]);
        assertEquals("-2500.00", fields[6]);
        // Element 4: zeros
        assertEquals("0.00", fields[7]);
        assertEquals("0.00", fields[8]);
        // Element 5: zeros
        assertEquals("0.00", fields[9]);
        assertEquals("0.00", fields[10]);
    }

    @Test
    void arryFile_record5_shouldUseItsOwnBalance() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(arryFile);
        String[] fields = lines.get(5).split(",");

        // Account 5: balance = 345.00 (00000003450{ with V99)
        assertEquals("00000000005", fields[0]);
        assertEquals("345.00", fields[1]);  // Element 1 balance
        assertEquals("345.00", fields[3]);  // Element 2 balance
        // Elements 3-5 are the same for all records
        assertEquals("-1025.00", fields[5]);
    }

    // ---------------------------------------------------------------
    // VBR file verification (COBOL 1500-POPUL-VBRC-RECORD equivalence)
    // ---------------------------------------------------------------

    @Test
    void vbrcFile_shouldHaveHeaderAndInterleavedRecords() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        // 1 header + 5 accounts * 2 records each = 11 lines
        assertEquals(11, lines.size());
    }

    @Test
    void vbrcFile_shouldAlternateVb1AndVb2() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        for (int i = 1; i < lines.size(); i++) {
            String expectedType = (i % 2 == 1) ? "VB1" : "VB2";
            assertTrue(lines.get(i).startsWith(expectedType),
                    "Line " + i + " should start with " + expectedType);
        }
    }

    @Test
    void vbrcFile_record1_vb1ShouldHaveIdAndStatus() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        String[] fields = lines.get(1).split(",");

        assertEquals("VB1", fields[0]);
        assertEquals("00000000001", fields[1]);
        assertEquals("Y", fields[2]);
    }

    @Test
    void vbrcFile_record1_vb2ShouldHaveBalanceLimitAndYear() throws IOException {
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        String[] fields = lines.get(2).split(",");

        assertEquals("VB2", fields[0]);
        assertEquals("00000000001", fields[1]);
        assertEquals("194.00", fields[2]);    // CURR-BAL
        assertEquals("2020.00", fields[3]);   // CREDIT-LIMIT
        assertEquals("2025", fields[4]);      // REISSUE-YYYY
    }

    // ---------------------------------------------------------------
    // Cross-validation: verify Java transformations match COBOL logic
    // for every record in the sample data
    // ---------------------------------------------------------------

    @Test
    void allRecords_outFileBalancesShouldMatchInput() throws IOException {
        List<AccountRecord> accounts = AccountFileReader.readAll(acctFile);
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        for (int i = 0; i < accounts.size(); i++) {
            AccountRecord acct = accounts.get(i);
            String[] fields = lines.get(i + 1).split(",");
            assertEquals(acct.currBal().toPlainString(), fields[2],
                    "Record " + (i + 1) + " balance mismatch");
        }
    }

    @Test
    void allRecords_vbrYearShouldMatchReissueDateYear() throws IOException {
        List<AccountRecord> accounts = AccountFileReader.readAll(acctFile);
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        for (int i = 0; i < accounts.size(); i++) {
            String expectedYear = accounts.get(i).reissueDate().substring(0, 4);
            // VB2 lines are at positions 2, 4, 6, 8, 10 (1-indexed)
            String[] fields = lines.get(2 + i * 2).split(",");
            assertEquals(expectedYear, fields[4],
                    "Record " + (i + 1) + " reissue year mismatch");
        }
    }

    // ---------------------------------------------------------------
    // Full dataset integration test (using actual acctdata.txt if available)
    // ---------------------------------------------------------------

    @Test
    void processFullDataset_shouldHandleAllRecordsWithoutError() throws IOException {
        // Use the actual sample data file if available; otherwise skip
        Path sampleData = Path.of("../app/data/ASCII/acctdata.txt");
        if (!Files.exists(sampleData)) {
            sampleData = Path.of("../../app/data/ASCII/acctdata.txt");
        }
        if (!Files.exists(sampleData)) {
            // Not available in test environment; skip gracefully
            return;
        }

        Path fullOut = tempDir.resolve("full_out.csv");
        Path fullArr = tempDir.resolve("full_arr.csv");
        Path fullVbr = tempDir.resolve("full_vbr.csv");

        int count = Cbact01cApplication.process(sampleData, fullOut, fullArr, fullVbr);
        assertTrue(count > 0, "Should process at least one record");

        // Verify output file line counts
        List<String> outLines = Files.readAllLines(fullOut);
        assertEquals(count + 1, outLines.size(), "OUT file: 1 header + N data lines");

        List<String> arrLines = Files.readAllLines(fullArr);
        assertEquals(count + 1, arrLines.size(), "ARRAY file: 1 header + N data lines");

        List<String> vbrLines = Files.readAllLines(fullVbr);
        assertEquals(count * 2 + 1, vbrLines.size(),
                "VBR file: 1 header + 2*N data lines");
    }
}
