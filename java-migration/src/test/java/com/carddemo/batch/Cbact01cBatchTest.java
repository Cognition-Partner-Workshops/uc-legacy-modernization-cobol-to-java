package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.util.CobolNumericParser;
import com.carddemo.util.DateFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the Java migration of CBACT01C produces identical results
 * to the COBOL version for sample inputs.
 */
class Cbact01cBatchTest {

    @TempDir
    Path tempDir;

    private Path sampleAcctFile;

    @BeforeEach
    void setUp() throws IOException {
        // Copy sample data from test resources to temp dir
        try (var is = getClass().getResourceAsStream("/sample_acctdata.txt")) {
            assertNotNull(is, "sample_acctdata.txt must be on the classpath");
            sampleAcctFile = tempDir.resolve("acctdata.txt");
            Files.write(sampleAcctFile, is.readAllBytes());
        }
    }

    // =========================================================================
    //  CobolNumericParser tests
    // =========================================================================

    @Test
    void parseSignedDisplay_positiveZeroOverpunch() {
        // '{' = +0, PIC S9(10)V99 field "00000001940{"
        BigDecimal val = CobolNumericParser.parseSignedDisplay("00000001940{", 10, 2);
        assertEquals(new BigDecimal("194.00"), val);
    }

    @Test
    void parseSignedDisplay_positiveNonZeroOverpunch() {
        // 'E' = +5, so "000000195E" as PIC S9(8)V99 => 19.55
        BigDecimal val = CobolNumericParser.parseSignedDisplay("000000195E", 8, 2);
        // digits become 0000001955 => 19.55
        assertEquals(0, new BigDecimal("19.55").compareTo(val));
    }

    @Test
    void parseSignedDisplay_negativeOverpunch() {
        // 'R' = -9, so "00000001009R" as PIC S9(10)V99 => -100.99
        BigDecimal val = CobolNumericParser.parseSignedDisplay("00000001009R", 10, 2);
        assertEquals(0, new BigDecimal("-100.99").compareTo(val));
    }

    @Test
    void formatSignedDisplay_positiveZero() {
        String result = CobolNumericParser.formatSignedDisplay(BigDecimal.ZERO, 10, 2);
        assertEquals("00000000000{", result);
    }

    @Test
    void formatSignedDisplay_positiveValue() {
        String result = CobolNumericParser.formatSignedDisplay(new BigDecimal("194.00"), 10, 2);
        assertEquals("00000001940{", result);
    }

    @Test
    void formatSignedDisplay_negativeValue() {
        // -1025.00 -> abs = 1025.00 -> scaled = 102500 -> digits "000000102500"
        // last digit = 0, negative -> '}'
        String result = CobolNumericParser.formatSignedDisplay(new BigDecimal("-1025.00"), 10, 2);
        assertEquals("00000010250}", result);
    }

    @Test
    void formatAndParseRoundTrip() {
        BigDecimal original = new BigDecimal("2525.00");
        String formatted = CobolNumericParser.formatSignedDisplay(original, 10, 2);
        BigDecimal parsed = CobolNumericParser.parseSignedDisplay(formatted, 10, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    @Test
    void formatAndParseRoundTrip_negative() {
        BigDecimal original = new BigDecimal("-2500.00");
        String formatted = CobolNumericParser.formatSignedDisplay(original, 10, 2);
        BigDecimal parsed = CobolNumericParser.parseSignedDisplay(formatted, 10, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    // =========================================================================
    //  DateFormatter tests
    // =========================================================================

    @Test
    void dateFormatter_type2ToType2_stripsDashes() {
        String result = DateFormatter.convert("2025-05-20", '2', '2');
        assertEquals("20250520", result);
    }

    @Test
    void dateFormatter_type1ToType1_addsDashes() {
        String result = DateFormatter.convert("20250520", '1', '1');
        assertEquals("2025-05-20", result);
    }

    @Test
    void dateFormatter_invalidCombination_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.convert("20250520", '1', '2'));
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatter.convert("2025-05-20", '2', '1'));
    }

    // =========================================================================
    //  AccountFileReader tests
    // =========================================================================

    @Test
    void accountFileReader_parsesThreeRecords() throws IOException {
        try (var reader = new AccountFileReader(sampleAcctFile)) {
            Optional<AccountRecord> r1 = reader.readNext();
            assertTrue(r1.isPresent());
            assertEquals(1L, r1.get().acctId());
            assertEquals("Y", r1.get().acctActiveStatus());
            assertEquals(0, new BigDecimal("194.00").compareTo(r1.get().acctCurrBal()));
            assertEquals(0, new BigDecimal("2020.00").compareTo(r1.get().acctCreditLimit()));
            assertEquals(0, new BigDecimal("1020.00").compareTo(r1.get().acctCashCreditLimit()));
            assertEquals("2014-11-20", r1.get().acctOpenDate());
            assertEquals("2025-05-20", r1.get().acctExpiraionDate());
            assertEquals("2025-05-20", r1.get().acctReissueDate());
            assertEquals(0, BigDecimal.ZERO.compareTo(r1.get().acctCurrCycCredit()));
            assertEquals(0, BigDecimal.ZERO.compareTo(r1.get().acctCurrCycDebit()));

            Optional<AccountRecord> r2 = reader.readNext();
            assertTrue(r2.isPresent());
            assertEquals(2L, r2.get().acctId());

            Optional<AccountRecord> r3 = reader.readNext();
            assertTrue(r3.isPresent());
            assertEquals(3L, r3.get().acctId());

            Optional<AccountRecord> r4 = reader.readNext();
            assertFalse(r4.isPresent()); // EOF
        }
    }

    // =========================================================================
    //  Cbact01cBatch — populateOutputRecord tests
    // =========================================================================

    @Test
    void populateOutputRecord_zeroDebitBecomesDefault() throws IOException {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "          ", "A000000000"
        );

        var batch = createBatch();
        OutputAccountRecord out = batch.populateOutputRecord(acct);

        // Reissue date should be reformatted: YYYY-MM-DD -> YYYYMMDD
        assertEquals("20250520", out.acctReissueDate());
        // Cycle debit should be substituted with 2525.00
        assertEquals(0, new BigDecimal("2525.00").compareTo(out.acctCurrCycDebit()));
        // Other fields pass through
        assertEquals(1L, out.acctId());
        assertEquals("Y", out.acctActiveStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(out.acctCurrBal()));
    }

    @Test
    void populateOutputRecord_nonZeroDebitPreserved() throws IOException {
        AccountRecord acct = new AccountRecord(
                99L, "Y",
                new BigDecimal("500.00"), new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                "2020-01-01", "2025-12-31", "2024-06-15",
                new BigDecimal("100.00"), new BigDecimal("50.00"),
                "12345     ", "B000000000"
        );

        var batch = createBatch();
        OutputAccountRecord out = batch.populateOutputRecord(acct);

        assertEquals("20240615", out.acctReissueDate());
        assertEquals(0, new BigDecimal("50.00").compareTo(out.acctCurrCycDebit()));
    }

    // =========================================================================
    //  Cbact01cBatch — populateArrayRecord tests
    // =========================================================================

    @Test
    void populateArrayRecord_slotsMatchCobolLogic() throws IOException {
        BigDecimal bal = new BigDecimal("194.00");
        AccountRecord acct = new AccountRecord(
                1L, "Y", bal, BigDecimal.ZERO, BigDecimal.ZERO,
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "          ", "A000000000"
        );

        var batch = createBatch();
        ArrayRecord arr = batch.populateArrayRecord(acct);

        assertEquals(1L, arr.getAcctId());
        // Slot 0: bal = acctCurrBal, debit = 1005.00
        assertEquals(0, bal.compareTo(arr.getAcctCurrBal(0)));
        assertEquals(0, new BigDecimal("1005.00").compareTo(arr.getAcctCurrCycDebit(0)));
        // Slot 1: bal = acctCurrBal, debit = 1525.00
        assertEquals(0, bal.compareTo(arr.getAcctCurrBal(1)));
        assertEquals(0, new BigDecimal("1525.00").compareTo(arr.getAcctCurrCycDebit(1)));
        // Slot 2: bal = -1025.00, debit = -2500.00
        assertEquals(0, new BigDecimal("-1025.00").compareTo(arr.getAcctCurrBal(2)));
        assertEquals(0, new BigDecimal("-2500.00").compareTo(arr.getAcctCurrCycDebit(2)));
        // Slots 3-4: zero
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.getAcctCurrBal(3)));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.getAcctCurrCycDebit(3)));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.getAcctCurrBal(4)));
        assertEquals(0, BigDecimal.ZERO.compareTo(arr.getAcctCurrCycDebit(4)));
    }

    // =========================================================================
    //  End-to-end batch execution
    // =========================================================================

    @Test
    void endToEnd_threeRecords_producesCorrectOutputFiles() throws IOException {
        Path outFile  = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        var console = new ByteArrayOutputStream();
        var batch = new Cbact01cBatch(
                sampleAcctFile, outFile, arryFile, vbrcFile,
                new PrintStream(console));

        int count = batch.execute();
        assertEquals(3, count);

        // ---- Verify OUTFILE ----
        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(3, outLines.size());
        // First record: acct 1
        String line1 = outLines.get(0);
        assertTrue(line1.startsWith("00000000001Y"), "Output should start with acct-id + status");
        // Reissue date reformatted to YYYYMMDD (starts at a known offset)
        assertTrue(line1.contains("20250520"), "Reissue date should be reformatted to YYYYMMDD");

        // ---- Verify ARRYFILE ----
        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(3, arryLines.size());
        assertTrue(arryLines.get(0).startsWith("00000000001"),
                "Array record should start with acct-id");

        // ---- Verify VBRCFILE ----
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        // 2 records per account = 6 lines
        assertEquals(6, vbrcLines.size());
        // Type-1 record: 12 chars (acct-id 11 + status 1)
        assertEquals("00000000001Y", vbrcLines.get(0));
        // Type-2 record: 39 chars (acct-id 11 + bal 12 + limit 12 + year 4)
        assertEquals(39, vbrcLines.get(1).length());
        assertTrue(vbrcLines.get(1).startsWith("00000000001"));
        assertTrue(vbrcLines.get(1).endsWith("2025"));

        // ---- Verify console output ----
        String consoleOutput = console.toString();
        assertTrue(consoleOutput.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(consoleOutput.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(consoleOutput.contains("ACCT-ID                 :00000000001"));
        assertTrue(consoleOutput.contains("VBRC-REC1:"));
        assertTrue(consoleOutput.contains("VBRC-REC2:"));
    }

    @Test
    void endToEnd_emptyFile_producesNoOutput() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");

        Path outFile  = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        var console = new ByteArrayOutputStream();
        var batch = new Cbact01cBatch(
                emptyFile, outFile, arryFile, vbrcFile,
                new PrintStream(console));

        int count = batch.execute();
        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
    }

    @Test
    void endToEnd_outputDebitSubstitution_verifiedInFile() throws IOException {
        // All sample records have zero debit, so all output should contain 2525.00
        Path outFile  = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        var batch = new Cbact01cBatch(
                sampleAcctFile, outFile, arryFile, vbrcFile,
                new PrintStream(new ByteArrayOutputStream()));
        batch.execute();

        List<String> outLines = Files.readAllLines(outFile);
        for (String line : outLines) {
            // The debit field (COMP-3 in COBOL, written as signed display) should be 2525.00
            // formatted as "00000025250{" (PIC S9(10)V99 = 12 chars)
            assertTrue(line.contains("00000025250{"),
                    "Zero debit should be substituted with 2525.00, got: " + line);
        }
    }

    @Test
    void endToEnd_fullDataset_50records() throws IOException {
        // Test against the full 50-record dataset
        Path fullData = Path.of("../app/data/ASCII/acctdata.txt");
        if (!Files.exists(fullData)) {
            return; // Skip if not running from the expected directory
        }

        Path outFile  = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        var batch = new Cbact01cBatch(
                fullData, outFile, arryFile, vbrcFile,
                new PrintStream(new ByteArrayOutputStream()));

        int count = batch.execute();
        assertEquals(50, count);
        assertEquals(50, Files.readAllLines(outFile).size());
        assertEquals(50, Files.readAllLines(arryFile).size());
        assertEquals(100, Files.readAllLines(vbrcFile).size()); // 2 per account
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private Cbact01cBatch createBatch() throws IOException {
        return new Cbact01cBatch(
                sampleAcctFile,
                tempDir.resolve("out.dat"),
                tempDir.resolve("arr.dat"),
                tempDir.resolve("vbr.dat"),
                new PrintStream(new ByteArrayOutputStream()));
    }
}
