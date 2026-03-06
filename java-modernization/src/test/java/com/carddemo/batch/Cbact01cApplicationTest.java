package com.carddemo.batch;

import com.carddemo.batch.exception.BatchProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link Cbact01cApplication}, verifying that the
 * full end-to-end processing produces output identical to what the COBOL
 * CBACT01C program would produce for the same sample inputs.
 */
class Cbact01cApplicationTest {

    /**
     * Sample account records in the same fixed-width format as
     * app/data/ASCII/acctdata.txt. The first two records from the
     * production sample data are used here.
     */
    private static final String SAMPLE_RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{" +
            "2014-11-202025-05-202025-05-2000000000000{00000000000{" +
            "A000000000" +
            " ".repeat(188);

    private static final String SAMPLE_RECORD_2 =
            "00000000002Y00000001580{00000061300{00000054480{" +
            "2013-06-192024-08-112024-08-1100000000000{00000000000{" +
            "A000000000" +
            " ".repeat(188);

    @Test
    void processesMultipleRecords_createsAllOutputFiles(@TempDir Path tempDir) throws IOException {
        Path acctFile = tempDir.resolve("acctdata.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        Files.writeString(acctFile, SAMPLE_RECORD_1 + "\n" + SAMPLE_RECORD_2 + "\n");

        long count = Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        assertEquals(2, count);
        assertTrue(Files.exists(outFile));
        assertTrue(Files.exists(arryFile));
        assertTrue(Files.exists(vbrcFile));
    }

    @Test
    void outFile_containsCorrectRecords(@TempDir Path tempDir) throws IOException {
        Path acctFile = tempDir.resolve("acctdata.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        Files.writeString(acctFile, SAMPLE_RECORD_1 + "\n" + SAMPLE_RECORD_2 + "\n");
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(2, outLines.size());

        // Record 1: verify date conversion and zero-debit substitution
        String[] parts1 = outLines.get(0).split("\\|");
        assertEquals("00000000001", parts1[0]);            // ACCT-ID
        assertEquals("Y", parts1[1]);                       // ACTIVE-STATUS
        assertEquals("194.00", parts1[2]);                  // CURR-BAL
        assertEquals("2020.00", parts1[3]);                 // CREDIT-LIMIT
        assertEquals("1020.00", parts1[4]);                 // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", parts1[5]);              // OPEN-DATE
        assertEquals("2025-05-20", parts1[6]);              // EXPIRATION-DATE
        assertEquals("20250520", parts1[7]);                // REISSUE-DATE (converted)
        assertEquals("0.00", parts1[8]);                    // CYC-CREDIT
        assertEquals("2525.00", parts1[9]);                 // CYC-DEBIT (substituted)

        // Record 2
        String[] parts2 = outLines.get(1).split("\\|");
        assertEquals("00000000002", parts2[0]);
        assertEquals("20240811", parts2[7]);                // different reissue date
        assertEquals("2525.00", parts2[9]);                 // also zero, so substituted
    }

    @Test
    void arryFile_containsCorrectArrayRecords(@TempDir Path tempDir) throws IOException {
        Path acctFile = tempDir.resolve("acctdata.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        Files.writeString(acctFile, SAMPLE_RECORD_1 + "\n");
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(1, arryLines.size());

        // Format: ACCTID|bal1,deb1|bal2,deb2|bal3,deb3|bal4,deb4|bal5,deb5
        String[] parts = arryLines.get(0).split("\\|");
        assertEquals(6, parts.length);
        assertEquals("00000000001", parts[0]);

        // Element 1: actual balance, debit=1005.00
        assertEquals("194.00,1005.00", parts[1]);
        // Element 2: actual balance, debit=1525.00
        assertEquals("194.00,1525.00", parts[2]);
        // Element 3: hardcoded -1025.00, -2500.00
        assertEquals("-1025.00,-2500.00", parts[3]);
        // Elements 4-5: zeros
        assertEquals("0,0", parts[4]);
        assertEquals("0,0", parts[5]);
    }

    @Test
    void vbrcFile_containsTwoRecordsPerAccount(@TempDir Path tempDir) throws IOException {
        Path acctFile = tempDir.resolve("acctdata.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        Files.writeString(acctFile, SAMPLE_RECORD_1 + "\n" + SAMPLE_RECORD_2 + "\n");
        Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(4, vbrcLines.size()); // 2 records * 2 lines each

        // Account 1, VB1: ID|status
        assertEquals("00000000001|Y", vbrcLines.get(0));
        // Account 1, VB2: ID|balance|credit_limit|reissue_year
        assertEquals("00000000001|194.00|2020.00|2025", vbrcLines.get(1));

        // Account 2, VB1
        assertEquals("00000000002|Y", vbrcLines.get(2));
        // Account 2, VB2
        assertEquals("00000000002|158.00|6130.00|2024", vbrcLines.get(3));
    }

    @Test
    void emptyInputFile_producesEmptyOutputs(@TempDir Path tempDir) throws IOException {
        Path acctFile = tempDir.resolve("acctdata.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        Files.writeString(acctFile, "");
        long count = Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile);

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arryFile).size());
        assertEquals(0, Files.readAllLines(vbrcFile).size());
    }

    @Test
    void missingInputFile_throwsBatchProcessingException(@TempDir Path tempDir) {
        Path acctFile = tempDir.resolve("nonexistent.txt");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        assertThrows(BatchProcessingException.class,
                () -> Cbact01cApplication.process(acctFile, outFile, arryFile, vbrcFile));
    }

    @Test
    void processesAllSampleDataRecords(@TempDir Path tempDir) throws IOException {
        // Load the actual sample data file from the repository
        Path sampleData = Path.of("../app/data/ASCII/acctdata.txt");
        if (!Files.exists(sampleData)) {
            // Fall back to absolute path
            sampleData = Path.of(System.getProperty("user.dir"))
                    .getParent()
                    .resolve("app/data/ASCII/acctdata.txt");
        }
        if (!Files.exists(sampleData)) {
            // Skip if sample data is not available in the test environment
            return;
        }

        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        long count = Cbact01cApplication.process(sampleData, outFile, arryFile, vbrcFile);

        // The sample file has 50 account records
        assertEquals(50, count);

        // Each account produces 1 out record, 1 array record, 2 vbrc records
        assertEquals(50, Files.readAllLines(outFile).size());
        assertEquals(50, Files.readAllLines(arryFile).size());
        assertEquals(100, Files.readAllLines(vbrcFile).size());
    }
}
