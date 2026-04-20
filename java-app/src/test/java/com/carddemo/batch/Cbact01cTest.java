package com.carddemo.batch;

import com.carddemo.batch.io.PackedDecimalUtil;
import com.carddemo.batch.io.ZonedDecimalUtil;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.DateConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify the Java rewrite of CBACT01C produces output
 * identical to the COBOL version for the sample acctdata.txt.
 */
class Cbact01cTest {

    private static final String SAMPLE_DATA_RESOURCE = "/acctdata.txt";
    private static Path sampleDataPath;

    @BeforeAll
    static void locateSampleData() throws Exception {
        var url = Cbact01cTest.class.getResource(SAMPLE_DATA_RESOURCE);
        if (url != null) {
            sampleDataPath = Path.of(url.toURI());
        }
    }

    // ---------------------------------------------------------------
    // ZonedDecimalUtil tests
    // ---------------------------------------------------------------

    @Test
    void zonedDecimal_parsePositiveWithOverpunch() {
        BigDecimal val = ZonedDecimalUtil.parse("00000001940{", 2);
        assertEquals(new BigDecimal("194.00"), val);
    }

    @Test
    void zonedDecimal_parsePositiveDigitOverpunch() {
        BigDecimal val = ZonedDecimalUtil.parse("00000020200{", 2);
        assertEquals(new BigDecimal("2020.00"), val);
    }

    @Test
    void zonedDecimal_parseZero() {
        BigDecimal val = ZonedDecimalUtil.parse("00000000000{", 2);
        assertEquals(new BigDecimal("0.00"), val);
    }

    @Test
    void zonedDecimal_parseNegative() {
        BigDecimal val = ZonedDecimalUtil.parse("00000010250K", 2);
        assertEquals(new BigDecimal("-1025.02"), val);
    }

    @Test
    void zonedDecimal_formatPositive() {
        String fmt = ZonedDecimalUtil.format(new BigDecimal("194.00"), 12, 2);
        assertEquals("00000001940{", fmt);
    }

    @Test
    void zonedDecimal_formatZero() {
        String fmt = ZonedDecimalUtil.format(BigDecimal.ZERO, 12, 2);
        assertEquals("00000000000{", fmt);
    }

    @Test
    void zonedDecimal_formatNegative() {
        String fmt = ZonedDecimalUtil.format(new BigDecimal("-1025.00"), 12, 2);
        assertEquals("00000010250}", fmt);
    }

    @Test
    void zonedDecimal_roundTrip() {
        BigDecimal original = new BigDecimal("12345.67");
        String formatted = ZonedDecimalUtil.format(original, 12, 2);
        BigDecimal parsed = ZonedDecimalUtil.parse(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    // ---------------------------------------------------------------
    // PackedDecimalUtil tests
    // ---------------------------------------------------------------

    @Test
    void packedDecimal_encodePositiveZero() {
        byte[] packed = PackedDecimalUtil.encode(BigDecimal.ZERO, 12, 2);
        assertEquals(7, packed.length);
        // Last nibble should be 0xC (positive sign)
        assertEquals(0x0C, packed[6] & 0xFF);
    }

    @Test
    void packedDecimal_encodePositiveValue() {
        byte[] packed = PackedDecimalUtil.encode(new BigDecimal("2525.00"), 12, 2);
        assertEquals(7, packed.length);
        BigDecimal decoded = PackedDecimalUtil.decode(packed, 2);
        assertEquals(0, new BigDecimal("2525.00").compareTo(decoded));
    }

    @Test
    void packedDecimal_encodeNegativeValue() {
        byte[] packed = PackedDecimalUtil.encode(new BigDecimal("-2500.00"), 12, 2);
        assertEquals(7, packed.length);
        BigDecimal decoded = PackedDecimalUtil.decode(packed, 2);
        assertEquals(0, new BigDecimal("-2500.00").compareTo(decoded));
    }

    @Test
    void packedDecimal_roundTrip() {
        BigDecimal original = new BigDecimal("98765.43");
        byte[] packed = PackedDecimalUtil.encode(original, 12, 2);
        BigDecimal decoded = PackedDecimalUtil.decode(packed, 2);
        assertEquals(0, original.compareTo(decoded));
    }

    // ---------------------------------------------------------------
    // DateConverter tests
    // ---------------------------------------------------------------

    @Test
    void dateConvert_yyyyMmDd_to_yyyymmdd() {
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals("20250520  ", result);
    }

    @Test
    void dateConvert_yyyymmdd_to_yyyyMmDd() {
        String result = DateConverter.convert("20250520", "1", "1");
        assertEquals("2025-05-20", result);
    }

    @Test
    void dateConvert_resultIsTenChars() {
        String result = DateConverter.convert("2014-11-20", "2", "2");
        assertEquals(10, result.length());
        assertEquals("20141120  ", result);
    }

    // ---------------------------------------------------------------
    // AccountRecord parsing tests
    // ---------------------------------------------------------------

    @Test
    void accountRecord_parseFirstLine() {
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000"
                + " ".repeat(178);
        AccountRecord rec = AccountRecord.parse(line);
        assertEquals("00000000001", rec.acctId());
        assertEquals("Y", rec.acctActiveStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(rec.acctCurrBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(rec.acctCreditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(rec.acctCashCreditLimit()));
        assertEquals("2014-11-20", rec.acctOpenDate());
        assertEquals("2025-05-20", rec.acctExpiraionDate());
        assertEquals("2025-05-20", rec.acctReissueDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(rec.acctCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(rec.acctCurrCycDebit()));
    }

    @Test
    void accountRecord_parseAddrZipAndGroupId() {
        // In the sample data, A000000000 is at offset 102 = ACCT-ADDR-ZIP;
        // ACCT-GROUP-ID at offset 112 is spaces (from the repeat block).
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000"
                + " ".repeat(188);
        AccountRecord rec = AccountRecord.parse(line);
        assertEquals("A000000000", rec.acctAddrZip());
        assertEquals("          ", rec.acctGroupId());
    }

    // ---------------------------------------------------------------
    // Full batch run — console output verification
    // ---------------------------------------------------------------

    @Test
    void batchRun_consoleStartAndEnd() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        List<String> console = result.consoleLines();
        assertEquals("START OF EXECUTION OF PROGRAM CBACT01C", console.get(0));
        assertEquals("END OF EXECUTION OF PROGRAM CBACT01C", console.get(console.size() - 1));
    }

    @Test
    void batchRun_displaysAllAccounts() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        long separatorCount = result.consoleLines().stream()
                .filter(l -> l.equals("-------------------------------------------------"))
                .count();
        assertEquals(50, separatorCount, "Should display 50 account records");
    }

    @Test
    void batchRun_displayFieldsForFirstAccount() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        List<String> console = result.consoleLines();
        assertTrue(console.contains("ACCT-ID                 :00000000001"));
        assertTrue(console.contains("ACCT-ACTIVE-STATUS      :Y"));
        assertTrue(console.contains("ACCT-CURR-BAL           :00000001940{"));
        assertTrue(console.contains("ACCT-CREDIT-LIMIT       :00000020200{"));
        assertTrue(console.contains("ACCT-CASH-CREDIT-LIMIT  :00000010200{"));
        assertTrue(console.contains("ACCT-OPEN-DATE          :2014-11-20"));
        assertTrue(console.contains("ACCT-EXPIRAION-DATE     :2025-05-20"));
        assertTrue(console.contains("ACCT-REISSUE-DATE       :2025-05-20"));
        assertTrue(console.contains("ACCT-CURR-CYC-CREDIT    :00000000000{"));
        assertTrue(console.contains("ACCT-CURR-CYC-DEBIT     :00000000000{"));
        assertTrue(console.contains("ACCT-GROUP-ID           :          "));
    }

    @Test
    void batchRun_vbrcDisplayForFirstAccount() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        List<String> console = result.consoleLines();
        assertTrue(console.contains("VBRC-REC1:00000000001Y"));
        assertTrue(console.contains("VBRC-REC2:0000000000100000001940{00000020200{2025"));
    }

    // ---------------------------------------------------------------
    // Full batch run — OUTFILE verification
    // ---------------------------------------------------------------

    @Test
    void batchRun_outFileRecordCount() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        byte[] outBytes = result.outFileBytes();
        assertEquals(50 * 107, outBytes.length,
                "OUTFILE should contain 50 records × 107 bytes each");
    }

    @Test
    void batchRun_outFileFirstRecordFields(@TempDir Path tmp) throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        byte[] outBytes = result.outFileBytes();

        // Parse first record (107 bytes)
        String displayPart = new String(outBytes, 0, 100, StandardCharsets.ISO_8859_1);

        // ACCT-ID (11 bytes)
        assertEquals("00000000001", displayPart.substring(0, 11));
        // ACTIVE-STATUS (1 byte)
        assertEquals("Y", displayPart.substring(11, 12));
        // CURR-BAL zoned (12 bytes)
        assertEquals("00000001940{", displayPart.substring(12, 24));
        // CREDIT-LIMIT zoned (12 bytes)
        assertEquals("00000020200{", displayPart.substring(24, 36));
        // CASH-CREDIT-LIMIT zoned (12 bytes)
        assertEquals("00000010200{", displayPart.substring(36, 48));
        // OPEN-DATE (10 bytes)
        assertEquals("2014-11-20", displayPart.substring(48, 58));
        // EXPIRAION-DATE (10 bytes)
        assertEquals("2025-05-20", displayPart.substring(58, 68));
        // REISSUE-DATE after conversion: YYYYMMDD padded to 10
        assertEquals("20250520  ", displayPart.substring(68, 78));
        // CURR-CYC-CREDIT zoned (12 bytes)
        assertEquals("00000000000{", displayPart.substring(78, 90));

        // CURR-CYC-DEBIT packed (7 bytes) — original is 0, so default 2525.00
        byte[] packedDebit = new byte[7];
        System.arraycopy(outBytes, 90, packedDebit, 0, 7);
        BigDecimal debit = PackedDecimalUtil.decode(packedDebit, 2);
        assertEquals(0, new BigDecimal("2525.00").compareTo(debit),
                "Zero debit should be replaced with 2525.00");

        // GROUP-ID (10 bytes) — spaces in sample data
        String groupId = new String(outBytes, 97, 10, StandardCharsets.ISO_8859_1);
        assertEquals("          ", groupId);
    }

    @Test
    void batchRun_outFileDefaultDebitSubstitution() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        byte[] outBytes = result.outFileBytes();

        // All 50 records have zero debit in the sample data,
        // so all should have 2525.00 packed in the output.
        for (int i = 0; i < 50; i++) {
            int offset = i * 107 + 90;
            byte[] packedDebit = new byte[7];
            System.arraycopy(outBytes, offset, packedDebit, 0, 7);
            BigDecimal debit = PackedDecimalUtil.decode(packedDebit, 2);
            assertEquals(0, new BigDecimal("2525.00").compareTo(debit),
                    "Record " + (i + 1) + ": zero debit should map to 2525.00");
        }
    }

    // ---------------------------------------------------------------
    // Full batch run — ARRYFILE verification
    // ---------------------------------------------------------------

    @Test
    void batchRun_arryFileRecordCount() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        byte[] arryBytes = result.arryFileBytes();
        assertEquals(50 * 110, arryBytes.length,
                "ARRYFILE should contain 50 records × 110 bytes each");
    }

    @Test
    void batchRun_arryFileFirstRecordSlots() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        byte[] arryBytes = result.arryFileBytes();

        // ACCT-ID
        String acctId = new String(arryBytes, 0, 11, StandardCharsets.ISO_8859_1);
        assertEquals("00000000001", acctId);

        // Slot 1: balance = acctCurrBal (194.00), debit = 1005.00
        String bal1 = new String(arryBytes, 11, 12, StandardCharsets.ISO_8859_1);
        assertEquals(0, new BigDecimal("194.00").compareTo(ZonedDecimalUtil.parse(bal1, 2)));
        byte[] deb1 = new byte[7];
        System.arraycopy(arryBytes, 23, deb1, 0, 7);
        assertEquals(0, new BigDecimal("1005.00").compareTo(PackedDecimalUtil.decode(deb1, 2)));

        // Slot 2: balance = acctCurrBal (194.00), debit = 1525.00
        String bal2 = new String(arryBytes, 30, 12, StandardCharsets.ISO_8859_1);
        assertEquals(0, new BigDecimal("194.00").compareTo(ZonedDecimalUtil.parse(bal2, 2)));
        byte[] deb2 = new byte[7];
        System.arraycopy(arryBytes, 42, deb2, 0, 7);
        assertEquals(0, new BigDecimal("1525.00").compareTo(PackedDecimalUtil.decode(deb2, 2)));

        // Slot 3: balance = -1025.00, debit = -2500.00
        String bal3 = new String(arryBytes, 49, 12, StandardCharsets.ISO_8859_1);
        assertEquals(0, new BigDecimal("-1025.00").compareTo(ZonedDecimalUtil.parse(bal3, 2)));
        byte[] deb3 = new byte[7];
        System.arraycopy(arryBytes, 61, deb3, 0, 7);
        assertEquals(0, new BigDecimal("-2500.00").compareTo(PackedDecimalUtil.decode(deb3, 2)));

        // Slot 4: zeros
        String bal4 = new String(arryBytes, 68, 12, StandardCharsets.ISO_8859_1);
        assertEquals(0, BigDecimal.ZERO.compareTo(ZonedDecimalUtil.parse(bal4, 2)));

        // Slot 5: zeros
        String bal5 = new String(arryBytes, 87, 12, StandardCharsets.ISO_8859_1);
        assertEquals(0, BigDecimal.ZERO.compareTo(ZonedDecimalUtil.parse(bal5, 2)));
    }

    // ---------------------------------------------------------------
    // Full batch run — VBRCFILE verification
    // ---------------------------------------------------------------

    @Test
    void batchRun_vbrcFileRecordCount() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        String vbrcContent = new String(result.vbrcFileBytes(), StandardCharsets.ISO_8859_1);
        String[] lines = vbrcContent.split("\n", -1);
        // 50 accounts × 2 records each = 100 lines (plus possible trailing empty)
        long nonEmpty = java.util.Arrays.stream(lines).filter(l -> !l.isEmpty()).count();
        assertEquals(100, nonEmpty, "VBRCFILE should contain 100 records (2 per account)");
    }

    @Test
    void batchRun_vbrcFileFirstAccountRecords() throws IOException {
        assumeSampleData();
        Cbact01c.BatchResult result = Cbact01c.run(sampleDataPath);
        String vbrcContent = new String(result.vbrcFileBytes(), StandardCharsets.ISO_8859_1);
        String[] lines = vbrcContent.split("\n", -1);

        // VB1 for first account: 12 bytes
        assertEquals("00000000001Y", lines[0]);
        assertEquals(12, lines[0].length());

        // VB2 for first account: 39 bytes
        assertEquals(39, lines[1].length());
        assertTrue(lines[1].startsWith("00000000001"));
        assertTrue(lines[1].endsWith("2025"));
    }

    // ---------------------------------------------------------------
    // Single-record integration test
    // ---------------------------------------------------------------

    @Test
    void batchRun_singleRecord() throws IOException {
        String singleLine = "00000000099Y00000005000{00000030000{00000015000{"
                + "2020-01-152025-12-312025-06-15"
                + "00000001000{00000000000{"
                + "B000000000"
                + " ".repeat(188) + "\n";

        Cbact01c.BatchResult result = Cbact01c.run(singleLine);
        List<String> console = result.consoleLines();

        assertEquals("START OF EXECUTION OF PROGRAM CBACT01C", console.get(0));
        assertEquals("END OF EXECUTION OF PROGRAM CBACT01C", console.get(console.size() - 1));

        assertTrue(console.contains("ACCT-ID                 :00000000099"));
        assertTrue(console.contains("ACCT-CURR-BAL           :00000005000{"));

        // OUTFILE: 1 record × 107 bytes
        assertEquals(107, result.outFileBytes().length);

        // ARRYFILE: 1 record × 110 bytes
        assertEquals(110, result.arryFileBytes().length);

        // VBRCFILE: 2 lines
        String vbrc = new String(result.vbrcFileBytes(), StandardCharsets.ISO_8859_1);
        String[] vbrcLines = vbrc.split("\n", -1);
        long nonEmpty = java.util.Arrays.stream(vbrcLines).filter(l -> !l.isEmpty()).count();
        assertEquals(2, nonEmpty);
        assertEquals("00000000099Y", vbrcLines[0]);
    }

    @Test
    void batchRun_nonZeroDebitPreserved() throws IOException {
        // Build a record where ACCT-CURR-CYC-DEBIT is non-zero (500.03 → "00000005000C")
        String singleLine = "00000000077Y00000001000{00000010000{00000005000{"
                + "2019-03-012024-09-152024-09-15"
                + "00000000200{00000005000C"
                + "Z000000000"
                + " ".repeat(188) + "\n";

        Cbact01c.BatchResult result = Cbact01c.run(singleLine);
        byte[] outBytes = result.outFileBytes();

        // CURR-CYC-DEBIT packed at offset 90 — should preserve 500.03, NOT 2525.00
        byte[] packedDebit = new byte[7];
        System.arraycopy(outBytes, 90, packedDebit, 0, 7);
        BigDecimal debit = PackedDecimalUtil.decode(packedDebit, 2);
        assertEquals(0, new BigDecimal("500.03").compareTo(debit),
                "Non-zero debit should be preserved, not replaced with 2525.00");
    }

    // ---------------------------------------------------------------
    // File-based end-to-end test
    // ---------------------------------------------------------------

    @Test
    void batchRun_fileBasedEndToEnd(@TempDir Path tmp) throws IOException {
        assumeSampleData();

        Path outFile  = tmp.resolve("outfile.dat");
        Path arryFile = tmp.resolve("arryfile.dat");
        Path vbrcFile = tmp.resolve("vbrcfile.dat");

        Cbact01c.main(new String[]{
                sampleDataPath.toString(),
                outFile.toString(),
                arryFile.toString(),
                vbrcFile.toString()
        });

        assertTrue(Files.exists(outFile));
        assertTrue(Files.exists(arryFile));
        assertTrue(Files.exists(vbrcFile));

        assertEquals(50 * 107, Files.size(outFile));
        assertEquals(50 * 110, Files.size(arryFile));

        // VBRC file should have 100 non-empty lines
        List<String> vbrcLines = Files.readAllLines(vbrcFile, StandardCharsets.ISO_8859_1);
        long nonEmpty = vbrcLines.stream().filter(l -> !l.isEmpty()).count();
        assertEquals(100, nonEmpty);
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private void assumeSampleData() {
        assertNotNull(sampleDataPath, "Sample data file not found on classpath");
        assertTrue(Files.exists(sampleDataPath), "Sample data file does not exist");
    }
}
