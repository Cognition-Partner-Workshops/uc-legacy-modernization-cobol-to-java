package com.carddemo.batch;

import com.carddemo.batch.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify the Java port produces identical results to the COBOL
 * CBACT01C program for the same logical inputs.
 */
class Cbact01cBatchJobTest {

    @TempDir
    Path tempDir;

    /**
     * Builds a 300-char fixed-width account line matching CVACT01Y layout.
     */
    private static String buildAccountLine(
            long acctId, String status,
            BigDecimal bal, BigDecimal creditLimit, BigDecimal cashCreditLimit,
            String openDate, String expirationDate, String reissueDate,
            BigDecimal cycCredit, BigDecimal cycDebit,
            String zip, String groupId) {
        return new AccountRecord(acctId, status, bal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate, cycCredit, cycDebit,
                zip, groupId).toFixedWidth();
    }

    // ── Sample data: mirrors what the COBOL program would read from VSAM ──

    private static final String ACCT_LINE_1 = buildAccountLine(
            1L, "Y",
            new BigDecimal("1940.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
            "2014-01-20", "2025-05-20", "2025-05-20",
            BigDecimal.ZERO, BigDecimal.ZERO,
            "A000000000", "A000000000");

    private static final String ACCT_LINE_2 = buildAccountLine(
            2L, "Y",
            new BigDecimal("1580.00"), new BigDecimal("6130.00"), new BigDecimal("5448.00"),
            "2013-06-19", "2024-08-11", "2024-08-11",
            BigDecimal.ZERO, BigDecimal.ZERO,
            "A000000000", "A000000000");

    private static final String ACCT_LINE_3 = buildAccountLine(
            3L, "Y",
            new BigDecimal("1470.00"), new BigDecimal("4909.00"), new BigDecimal("4818.00"),
            "2012-04-08", "2024-08-14", "2024-08-14",
            BigDecimal.ZERO, new BigDecimal("125.50"),
            "A000000000", "B000000001");

    // ── Integration tests ──

    @Test
    @DisplayName("End-to-end batch run produces correct output files")
    void endToEndBatchRun() throws IOException {
        Path acctFile = tempDir.resolve("ACCTFILE");
        Path outFile  = tempDir.resolve("OUTFILE");
        Path arryFile = tempDir.resolve("ARRYFILE");
        Path vbrcFile = tempDir.resolve("VBRCFILE");

        Files.writeString(acctFile,
                ACCT_LINE_1 + "\n" + ACCT_LINE_2 + "\n" + ACCT_LINE_3 + "\n");

        Cbact01cBatchJob job = new Cbact01cBatchJob(acctFile, outFile, arryFile, vbrcFile);
        BatchResult result = job.execute();

        assertTrue(result.isSuccess(), "Batch should complete successfully");
        assertEquals(3, result.recordsProcessed());
        assertEquals(BatchResult.RC_OK, result.returnCode());

        // Verify OUTFILE
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(3, outLines.size(), "OUTFILE should have 3 records");

        // Verify ARRYFILE
        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(3, arryLines.size(), "ARRYFILE should have 3 records");

        // Verify VBRCFILE — two records per account (VB1 + VB2)
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(6, vbrcLines.size(), "VBRCFILE should have 6 records (2 per account)");
    }

    // ── Output record transformation tests ──

    @Test
    @DisplayName("Default cycle-debit of 2525.00 when original is zero")
    void defaultCycleDebitWhenZero() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_1);
        assertEquals(0, acct.currCycDebit().signum(), "Input debit should be zero");

        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        OutputAccountRecord out = job.populateOutputRecord(acct);

        assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                "Zero debit must be replaced with 2525.00");
    }

    @Test
    @DisplayName("Non-zero cycle-debit is preserved")
    void nonZeroCycleDebitPreserved() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_3);
        assertEquals(new BigDecimal("125.50"), acct.currCycDebit());

        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        OutputAccountRecord out = job.populateOutputRecord(acct);

        assertEquals(new BigDecimal("125.50"), out.currCycDebit(),
                "Non-zero debit must be kept as-is");
    }

    @Test
    @DisplayName("Reissue date is reformatted from YYYY-MM-DD to YYYYMMDD")
    void reissueDateReformatted() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_1);
        assertEquals("2025-05-20", acct.reissueDate());

        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        OutputAccountRecord out = job.populateOutputRecord(acct);

        assertEquals("20250520", out.reissueDate(),
                "Reissue date must be converted to YYYYMMDD");
    }

    @Test
    @DisplayName("Output record preserves all direct-mapped fields")
    void outputRecordFieldMapping() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_2);
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        OutputAccountRecord out = job.populateOutputRecord(acct);

        assertEquals(2L, out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(new BigDecimal("1580.00"), out.currBal());
        assertEquals(new BigDecimal("6130.00"), out.creditLimit());
        assertEquals(new BigDecimal("5448.00"), out.cashCreditLimit());
        assertEquals("2013-06-19", out.openDate());
        assertEquals("2024-08-11", out.expirationDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(out.currCycCredit()));
        assertEquals("A000000000", out.groupId());
    }

    // ── Array record tests ──

    @Test
    @DisplayName("Array record entries match COBOL hard-coded values")
    void arrayRecordPopulation() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_1);
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        ArrayRecord arr = job.populateArrayRecord(acct);

        assertEquals(1L, arr.acctId());
        List<ArrayRecord.BalanceEntry> entries = arr.balanceEntries();
        assertEquals(5, entries.size());

        // Entry 1: ACCT-CURR-BAL, 1005.00
        assertEquals(acct.currBal(), entries.get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), entries.get(0).currCycDebit());

        // Entry 2: ACCT-CURR-BAL, 1525.00
        assertEquals(acct.currBal(), entries.get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), entries.get(1).currCycDebit());

        // Entry 3: -1025.00, -2500.00
        assertEquals(new BigDecimal("-1025.00"), entries.get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), entries.get(2).currCycDebit());

        // Entries 4-5: zero-initialised
        assertEquals(0, entries.get(3).currBal().signum());
        assertEquals(0, entries.get(3).currCycDebit().signum());
        assertEquals(0, entries.get(4).currBal().signum());
        assertEquals(0, entries.get(4).currCycDebit().signum());
    }

    // ── Variable-length record tests ──

    @Test
    @DisplayName("VB1 record contains account ID and active status")
    void vbRecord1Fields() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_1);
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        VbRecord1 vb1 = job.populateVbRecord1(acct);

        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    @DisplayName("VB2 record contains balances and reissue year")
    void vbRecord2Fields() {
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_2);
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        VbRecord2 vb2 = job.populateVbRecord2(acct);

        assertEquals(2L, vb2.acctId());
        assertEquals(new BigDecimal("1580.00"), vb2.currBal());
        assertEquals(new BigDecimal("6130.00"), vb2.creditLimit());
        assertEquals("2024", vb2.reissueYear(), "Year extracted from reissue date");
    }

    // ── Serialisation round-trip tests ──

    @Test
    @DisplayName("AccountRecord parse→toFixedWidth round-trip is stable")
    void accountRecordRoundTrip() {
        AccountRecord original = AccountRecord.parse(ACCT_LINE_1);
        String serialised = original.toFixedWidth();
        AccountRecord reparsed = AccountRecord.parse(serialised);

        assertEquals(original, reparsed);
    }

    @Test
    @DisplayName("OutputAccountRecord delimited round-trip")
    void outputRecordRoundTrip() {
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_1);
        OutputAccountRecord out = job.populateOutputRecord(acct);

        String line = out.toDelimited();
        OutputAccountRecord reparsed = OutputAccountRecord.parseDelimited(line);

        assertEquals(out.acctId(), reparsed.acctId());
        assertEquals(out.activeStatus(), reparsed.activeStatus());
        assertEquals(0, out.currBal().compareTo(reparsed.currBal()));
        assertEquals(0, out.currCycDebit().compareTo(reparsed.currCycDebit()));
        assertEquals(out.reissueDate().trim(), reparsed.reissueDate().trim());
    }

    @Test
    @DisplayName("ArrayRecord delimited round-trip")
    void arrayRecordRoundTrip() {
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_2);
        ArrayRecord arr = job.populateArrayRecord(acct);

        String line = arr.toDelimited();
        ArrayRecord reparsed = ArrayRecord.parseDelimited(line);

        assertEquals(arr.acctId(), reparsed.acctId());
        assertEquals(arr.balanceEntries().size(), reparsed.balanceEntries().size());
    }

    @Test
    @DisplayName("VbRecord1/VbRecord2 delimited round-trip")
    void vbRecordRoundTrips() {
        Cbact01cBatchJob job = new Cbact01cBatchJob(null, null, null, null);
        AccountRecord acct = AccountRecord.parse(ACCT_LINE_3);

        VbRecord1 vb1 = job.populateVbRecord1(acct);
        assertEquals(vb1, VbRecord1.parseDelimited(vb1.toDelimited()));

        VbRecord2 vb2 = job.populateVbRecord2(acct);
        VbRecord2 reparsed = VbRecord2.parseDelimited(vb2.toDelimited());
        assertEquals(vb2.acctId(), reparsed.acctId());
        assertEquals(0, vb2.currBal().compareTo(reparsed.currBal()));
    }

    // ── Error-handling tests ──

    @Test
    @DisplayName("Batch returns ABEND code when input file does not exist")
    void missingInputFileReturnsAbend() {
        Path missing = tempDir.resolve("DOES_NOT_EXIST");
        Cbact01cBatchJob job = new Cbact01cBatchJob(
                missing, tempDir.resolve("O"), tempDir.resolve("A"), tempDir.resolve("V"));

        BatchResult result = job.execute();
        assertEquals(BatchResult.RC_ABEND, result.returnCode());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Empty input file produces zero records and RC_OK")
    void emptyInputFile() throws IOException {
        Path acctFile = tempDir.resolve("EMPTY");
        Files.writeString(acctFile, "");

        Cbact01cBatchJob job = new Cbact01cBatchJob(
                acctFile, tempDir.resolve("O"), tempDir.resolve("A"), tempDir.resolve("V"));

        BatchResult result = job.execute();
        assertTrue(result.isSuccess());
        assertEquals(0, result.recordsProcessed());
    }

    // ── Output content verification tests ──

    @Test
    @DisplayName("OUTFILE content matches expected COBOL output values")
    void outfileContentVerification() throws IOException {
        Path acctFile = tempDir.resolve("ACCTFILE");
        Path outFile  = tempDir.resolve("OUTFILE");
        Path arryFile = tempDir.resolve("ARRYFILE");
        Path vbrcFile = tempDir.resolve("VBRCFILE");

        Files.writeString(acctFile, ACCT_LINE_1 + "\n");

        new Cbact01cBatchJob(acctFile, outFile, arryFile, vbrcFile).execute();

        OutputAccountRecord out = OutputAccountRecord.parseDelimited(
                Files.readAllLines(outFile).get(0));

        assertAll("OUTFILE record field-by-field",
                () -> assertEquals(1L, out.acctId()),
                () -> assertEquals("Y", out.activeStatus()),
                () -> assertEquals(0, new BigDecimal("1940.00").compareTo(out.currBal())),
                () -> assertEquals(0, new BigDecimal("2020.00").compareTo(out.creditLimit())),
                () -> assertEquals(0, new BigDecimal("1020.00").compareTo(out.cashCreditLimit())),
                () -> assertEquals("2014-01-20", out.openDate()),
                () -> assertEquals("2025-05-20", out.expirationDate()),
                () -> assertEquals("20250520", out.reissueDate()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(out.currCycCredit())),
                () -> assertEquals(0, new BigDecimal("2525.00").compareTo(out.currCycDebit())),
                () -> assertEquals("A000000000", out.groupId())
        );
    }

    @Test
    @DisplayName("ARRYFILE content matches expected COBOL output values")
    void arryfileContentVerification() throws IOException {
        Path acctFile = tempDir.resolve("ACCTFILE");
        Path outFile  = tempDir.resolve("OUTFILE");
        Path arryFile = tempDir.resolve("ARRYFILE");
        Path vbrcFile = tempDir.resolve("VBRCFILE");

        Files.writeString(acctFile, ACCT_LINE_1 + "\n");

        new Cbact01cBatchJob(acctFile, outFile, arryFile, vbrcFile).execute();

        ArrayRecord arr = ArrayRecord.parseDelimited(
                Files.readAllLines(arryFile).get(0));

        assertEquals(1L, arr.acctId());
        assertEquals(5, arr.balanceEntries().size());
        assertEquals(0, new BigDecimal("1940.00").compareTo(arr.balanceEntries().get(0).currBal()));
        assertEquals(0, new BigDecimal("1005.00").compareTo(arr.balanceEntries().get(0).currCycDebit()));
    }

    @Test
    @DisplayName("VBRCFILE alternates VB1 and VB2 records")
    void vbrcfileContentVerification() throws IOException {
        Path acctFile = tempDir.resolve("ACCTFILE");
        Path outFile  = tempDir.resolve("OUTFILE");
        Path arryFile = tempDir.resolve("ARRYFILE");
        Path vbrcFile = tempDir.resolve("VBRCFILE");

        Files.writeString(acctFile, ACCT_LINE_1 + "\n");

        new Cbact01cBatchJob(acctFile, outFile, arryFile, vbrcFile).execute();

        List<String> lines = Files.readAllLines(vbrcFile);
        assertEquals(2, lines.size());

        // VB1: acctId|status
        VbRecord1 vb1 = VbRecord1.parseDelimited(lines.get(0));
        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());

        // VB2: acctId|bal|creditLimit|reissueYear
        VbRecord2 vb2 = VbRecord2.parseDelimited(lines.get(1));
        assertEquals(1L, vb2.acctId());
        assertEquals(0, new BigDecimal("1940.00").compareTo(vb2.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(vb2.creditLimit()));
        assertEquals("2025", vb2.reissueYear());
    }

    // ── Multi-record batch verifying record counts across all files ──

    @Test
    @DisplayName("Three-record batch produces correct counts in all output files")
    void threeRecordBatchCounts() throws IOException {
        Path acctFile = tempDir.resolve("ACCTFILE");
        Path outFile  = tempDir.resolve("OUTFILE");
        Path arryFile = tempDir.resolve("ARRYFILE");
        Path vbrcFile = tempDir.resolve("VBRCFILE");

        Files.writeString(acctFile,
                ACCT_LINE_1 + "\n" + ACCT_LINE_2 + "\n" + ACCT_LINE_3 + "\n");

        BatchResult result = new Cbact01cBatchJob(acctFile, outFile, arryFile, vbrcFile).execute();

        assertEquals(3, result.recordsProcessed());
        assertEquals(3, Files.readAllLines(outFile).size());
        assertEquals(3, Files.readAllLines(arryFile).size());
        assertEquals(6, Files.readAllLines(vbrcFile).size());
    }
}
