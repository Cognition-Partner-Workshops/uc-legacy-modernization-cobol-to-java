package com.carddemo.batch.processor;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileProcessor} — the main CBACT01C batch program.
 *
 * Verifies:
 * <ul>
 *   <li>Business rules: debit override, hardcoded array values</li>
 *   <li>Date conversion: YYYY-MM-DD → YYYYMMDD</li>
 *   <li>End-to-end processing with sample data</li>
 *   <li>Output file integrity (correct record counts)</li>
 * </ul>
 */
class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private AccountFileProcessor processor;
    private Path inputFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("outfile.txt");
        arryFile = tempDir.resolve("arryfile.txt");
        vbrcFile = tempDir.resolve("vbrcfile.txt");

        processor = new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);
    }

    @Nested
    @DisplayName("Business logic — populateOutRecord")
    class PopulateOutRecord {

        @Test
        @DisplayName("Zero debit is overridden to 2525.00 (COBOL: MOVE 2525.00)")
        void shouldOverrideZeroDebit() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var out = processor.populateOutRecord(acct);
            assertEquals(new BigDecimal("2525.00"), out.currentCycleDebit());
        }

        @Test
        @DisplayName("Non-zero debit is preserved as-is")
        void shouldPreserveNonZeroDebit() {
            var acct = createAccount(1L, new BigDecimal("100.50"));
            var out = processor.populateOutRecord(acct);
            assertEquals(new BigDecimal("100.50"), out.currentCycleDebit());
        }

        @Test
        @DisplayName("Reissue date converted from YYYY-MM-DD to YYYYMMDD")
        void shouldConvertReissueDate() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var out = processor.populateOutRecord(acct);
            assertEquals("20250520", out.reissueDate());
        }

        @Test
        @DisplayName("All account fields are correctly mapped to output record")
        void shouldMapAllFields() {
            var acct = createAccount(42L, new BigDecimal("999.99"));
            var out = processor.populateOutRecord(acct);

            assertEquals(42L, out.acctId());
            assertEquals("Y", out.activeStatus());
            assertEquals(new BigDecimal("194.00"), out.currentBalance());
            assertEquals(new BigDecimal("2020.00"), out.creditLimit());
            assertEquals(new BigDecimal("1020.00"), out.cashCreditLimit());
            assertEquals("2014-11-20", out.openDate());
            assertEquals("2025-05-20", out.expirationDate());
            assertEquals(new BigDecimal("0.00"), out.currentCycleCredit());
            assertEquals("A000000000", out.groupId());
        }
    }

    @Nested
    @DisplayName("Business logic — populateArrayRecord")
    class PopulateArrayRecord {

        @Test
        @DisplayName("Array has exactly 5 entries")
        void shouldHaveFiveEntries() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var arr = processor.populateArrayRecord(acct);
            assertEquals(5, arr.balanceEntries().size());
        }

        @Test
        @DisplayName("Entry 0: balance = acct balance, debit = 1005.00")
        void shouldPopulateEntry0() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var arr = processor.populateArrayRecord(acct);
            var entry = arr.balanceEntries().get(0);
            assertEquals(new BigDecimal("194.00"), entry.currentBalance());
            assertEquals(new BigDecimal("1005.00"), entry.currentCycleDebit());
        }

        @Test
        @DisplayName("Entry 1: balance = acct balance, debit = 1525.00")
        void shouldPopulateEntry1() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var arr = processor.populateArrayRecord(acct);
            var entry = arr.balanceEntries().get(1);
            assertEquals(new BigDecimal("194.00"), entry.currentBalance());
            assertEquals(new BigDecimal("1525.00"), entry.currentCycleDebit());
        }

        @Test
        @DisplayName("Entry 2: balance = -1025.00, debit = -2500.00 (hardcoded)")
        void shouldPopulateEntry2() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var arr = processor.populateArrayRecord(acct);
            var entry = arr.balanceEntries().get(2);
            assertEquals(new BigDecimal("-1025.00"), entry.currentBalance());
            assertEquals(new BigDecimal("-2500.00"), entry.currentCycleDebit());
        }

        @Test
        @DisplayName("Entries 3-4: zero-initialized")
        void shouldZeroInitializeEntries3And4() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var arr = processor.populateArrayRecord(acct);
            for (int i = 3; i < 5; i++) {
                assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(i).currentBalance());
                assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(i).currentCycleDebit());
            }
        }
    }

    @Nested
    @DisplayName("Business logic — VBR records")
    class VbrRecords {

        @Test
        @DisplayName("VBR record 1 has account ID and active status")
        void shouldPopulateVbr1() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var vbr1 = processor.populateVbrRecord1(acct);
            assertEquals(1L, vbr1.acctId());
            assertEquals("Y", vbr1.activeStatus());
        }

        @Test
        @DisplayName("VBR record 2 has account ID, balance, credit limit, reissue year")
        void shouldPopulateVbr2() {
            var acct = createAccount(1L, BigDecimal.ZERO);
            var vbr2 = processor.populateVbrRecord2(acct);
            assertEquals(1L, vbr2.acctId());
            assertEquals(new BigDecimal("194.00"), vbr2.currentBalance());
            assertEquals(new BigDecimal("2020.00"), vbr2.creditLimit());
            assertEquals("2025", vbr2.reissueYear());
        }
    }

    @Nested
    @DisplayName("End-to-end processing")
    class EndToEnd {

        @Test
        @DisplayName("Processes 5 sample records and produces correct output file counts")
        void shouldProcessSampleFile() throws IOException {
            // Copy sample data
            var samplePath = Path.of("src/test/resources/acctdata_sample.txt");
            if (Files.exists(samplePath)) {
                Files.copy(samplePath, inputFile);
            } else {
                // Fallback: create a minimal 2-record file inline
                Files.writeString(inputFile, buildSampleRecords());
            }

            int count = processor.process();
            assertTrue(count > 0, "Should process at least one record");

            // OUT-FILE: one line per record
            var outLines = Files.readAllLines(outFile);
            assertEquals(count, outLines.size());

            // ARRY-FILE: one line per record
            var arryLines = Files.readAllLines(arryFile);
            assertEquals(count, arryLines.size());

            // VBRC-FILE: two lines per record (VBR1 + VBR2)
            var vbrcLines = Files.readAllLines(vbrcFile);
            assertEquals(count * 2, vbrcLines.size());
        }

        @Test
        @DisplayName("All output records for account 1 have debit = 2525.00 (zero override)")
        void shouldApplyDebitOverrideInOutput() throws IOException {
            Files.writeString(inputFile, buildSampleRecords());

            processor.process();

            var outLines = Files.readAllLines(outFile);
            assertFalse(outLines.isEmpty());

            // First record — debit field (index 9) should be 2525.00
            String[] fields = outLines.get(0).split("\\|", -1);
            assertEquals("2525.00", fields[9],
                    "Zero debit should be overridden to 2525.00");
        }

        @Test
        @DisplayName("Reissue date in output is YYYYMMDD format")
        void shouldConvertReissueDateInOutput() throws IOException {
            Files.writeString(inputFile, buildSampleRecords());

            processor.process();

            var outLines = Files.readAllLines(outFile);
            String[] fields = outLines.get(0).split("\\|", -1);
            assertEquals("20250520", fields[7],
                    "Reissue date should be converted from YYYY-MM-DD to YYYYMMDD");
        }

        @Test
        @DisplayName("VBR record 1 is 12-char ID+status, VBR record 2 has balance/credit")
        void shouldWriteCorrectVbrFormats() throws IOException {
            Files.writeString(inputFile, buildSampleRecords());

            processor.process();

            var vbrcLines = Files.readAllLines(vbrcFile);
            assertTrue(vbrcLines.size() >= 2);

            // VBR1: "00000000001Y" (no pipes)
            assertEquals("00000000001Y", vbrcLines.get(0));

            // VBR2: pipe-delimited with balance and credit limit
            assertTrue(vbrcLines.get(1).contains("|"));
            String[] vbr2Fields = vbrcLines.get(1).split("\\|", -1);
            assertEquals(4, vbr2Fields.length);
            assertEquals("00000000001", vbr2Fields[0]);
            assertEquals("194.00", vbr2Fields[1]);
            assertEquals("2020.00", vbr2Fields[2]);
            assertEquals("2025", vbr2Fields[3]);
        }

        @Test
        @DisplayName("Empty input file produces no output")
        void shouldHandleEmptyFile() throws IOException {
            Files.writeString(inputFile, "");

            int count = processor.process();

            assertEquals(0, count);
            assertEquals(0, Files.readAllLines(outFile).size());
            assertEquals(0, Files.readAllLines(arryFile).size());
            assertEquals(0, Files.readAllLines(vbrcFile).size());
        }
    }

    // ---- Helpers ----

    private static AccountRecord createAccount(long id, BigDecimal cycDebit) {
        return new AccountRecord(
                id,
                "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20",
                "2025-05-20",
                "2025-05-20",
                new BigDecimal("0.00"),
                cycDebit,
                "A000000000",
                "A000000000"
        );
    }

    /**
     * Build a minimal 2-record sample mimicking acctdata.txt format.
     * Each line is padded to 300 characters.
     */
    private static String buildSampleRecords() {
        var sb = new StringBuilder();
        sb.append(padTo300(
                "00000000001Y00000001940{00000020200{00000010200{" +
                "2014-11-202025-05-202025-05-20" +
                "00000000000{00000000000{" +
                "A000000000")).append('\n');
        sb.append(padTo300(
                "00000000002Y00000001580{00000061300{00000054480{" +
                "2013-06-192024-08-112024-08-11" +
                "00000000000{00000000000{" +
                "A000000000")).append('\n');
        return sb.toString();
    }

    private static String padTo300(String s) {
        return String.format("%-300s", s);
    }
}
