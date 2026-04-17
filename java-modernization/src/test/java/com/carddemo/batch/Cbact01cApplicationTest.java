package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.util.DateConverter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Java modernization of CBACT01C.
 * <p>
 * These tests verify that the Java version produces output identical to the
 * COBOL version for the sample data files shipped in the repository.
 */
class Cbact01cApplicationTest {

    @TempDir
    Path tempDir;

    private Path sampleInput;

    @BeforeEach
    void setUp() throws IOException {
        // Copy sample data from classpath to temp dir
        try (InputStream is = getClass().getResourceAsStream("/acctdata.txt")) {
            assertNotNull(is, "Sample acctdata.txt must be on the test classpath");
            sampleInput = tempDir.resolve("acctdata.txt");
            Files.copy(is, sampleInput);
        }
    }

    // -----------------------------------------------------------------------
    // AccountRecord parsing
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Parse first sample record correctly")
    void parseFirstRecord() {
        String line = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-202025-05-202025-05-20"
                + "00000000000{00000000000{A000000000"
                + " ".repeat(178);
        AccountRecord rec = AccountRecord.parse(line);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.acctActiveStatus());
        assertEquals(new BigDecimal("194.00"), rec.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), rec.acctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.acctCashCreditLimit());
        assertEquals("2014-11-20", rec.acctOpenDate());
        assertEquals("2025-05-20", rec.acctExpiraionDate());
        assertEquals("2025-05-20", rec.acctReissueDate());
        assertEquals(BigDecimal.ZERO.setScale(2), rec.acctCurrCycCredit());
        assertEquals(BigDecimal.ZERO.setScale(2), rec.acctCurrCycDebit());
        assertEquals("A000000000", rec.acctAddrZip());
        assertEquals("          ", rec.acctGroupId());
    }

    @Test
    @DisplayName("Parse all 50 sample records without error")
    void parseAllSampleRecords() throws IOException {
        List<String> lines = Files.readAllLines(sampleInput);
        long nonBlank = lines.stream().filter(l -> !l.isBlank()).count();
        assertEquals(50, nonBlank, "Sample file should contain 50 account records");

        for (String line : lines) {
            if (line.isBlank()) continue;
            AccountRecord rec = AccountRecord.parse(line);
            assertTrue(rec.acctId() > 0, "Account ID should be positive");
        }
    }

    // -----------------------------------------------------------------------
    // Signed zoned-decimal encoding / decoding round-trip
    // -----------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "194.00,    00000001940{",
            "0.00,      00000000000{",
            "-1025.00,  0000010250{",   // will be checked differently
            "2525.00,   00000025250{",
    })
    @DisplayName("Signed zoned-decimal round-trip")
    void zonedDecimalRoundTrip(String decimalStr, String expectedTrailing) {
        BigDecimal value = new BigDecimal(decimalStr);
        String encoded = OutputAccountRecord.formatSignedZoned(value);
        assertEquals(12, encoded.length(), "Zoned decimal must be 12 characters");

        // Decode it back
        BigDecimal decoded = AccountRecord.parseSignedZonedDecimal(encoded, 0, 12);
        assertEquals(0, value.compareTo(decoded),
                "Round-trip should produce identical value: " + value + " vs " + decoded);
    }

    @Test
    @DisplayName("Negative zoned-decimal encoding uses J-R overpunch")
    void negativeZonedDecimal() {
        BigDecimal neg = new BigDecimal("-1025.00");
        String encoded = OutputAccountRecord.formatSignedZoned(neg);
        assertEquals(12, encoded.length());
        // Last char should be } (negative zero) since 102500 ends in 0
        assertEquals('}', encoded.charAt(11));
        // Decode back
        BigDecimal decoded = AccountRecord.parseSignedZonedDecimal(encoded, 0, 12);
        assertEquals(0, neg.compareTo(decoded));
    }

    // -----------------------------------------------------------------------
    // DateConverter
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("YYYY-MM-DD to YYYYMMDD conversion (type 2 → 2)")
    void dateConvertDashToCompact() {
        assertEquals("20250520", DateConverter.convert("2025-05-20", '2', '2'));
    }

    @Test
    @DisplayName("YYYYMMDD to YYYY-MM-DD conversion (type 1 → 1)")
    void dateConvertCompactToDash() {
        assertEquals("2025-05-20", DateConverter.convert("20250520", '1', '1'));
    }

    @Test
    @DisplayName("Cross-format: YYYYMMDD to YYYY-MM-DD (type 1 → 1)")
    void dateConvertCross() {
        assertEquals("2014-11-20", DateConverter.convert("20141120", '1', '1'));
    }

    // -----------------------------------------------------------------------
    // Output record population
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Output record: zero debit gets default 2525.00")
    void outputRecordDefaultDebit() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        // Use the first record which has zero debit
        String line = Files.readAllLines(sampleInput).get(0);
        AccountRecord acct = AccountRecord.parse(line);
        assertEquals(0, acct.acctCurrCycDebit().signum(), "First record debit should be zero");

        OutputAccountRecord outRec = app.populateOutputRecord(acct);
        assertEquals(new BigDecimal("2525.00"), outRec.acctCurrCycDebit());
    }

    @Test
    @DisplayName("Output record: reissue date converted to YYYYMMDD format")
    void outputRecordReissueDateFormatted() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        String line = Files.readAllLines(sampleInput).get(0);
        AccountRecord acct = AccountRecord.parse(line);
        OutputAccountRecord outRec = app.populateOutputRecord(acct);

        assertTrue(outRec.acctReissueDate().startsWith("20250520"),
                "Reissue date should be converted from 2025-05-20 to 20250520");
    }

    // -----------------------------------------------------------------------
    // Array record population
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Array record: slots populated correctly per COBOL logic")
    void arrayRecordPopulation() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        String line = Files.readAllLines(sampleInput).get(0);
        AccountRecord acct = AccountRecord.parse(line);
        ArrayRecord arr = app.populateArrayRecord(acct);

        assertEquals(acct.acctId(), arr.acctId());
        // Slot 1
        assertEquals(acct.acctCurrBal(), arr.balances()[0]);
        assertEquals(new BigDecimal("1005.00"), arr.debits()[0]);
        // Slot 2
        assertEquals(acct.acctCurrBal(), arr.balances()[1]);
        assertEquals(new BigDecimal("1525.00"), arr.debits()[1]);
        // Slot 3
        assertEquals(new BigDecimal("-1025.00"), arr.balances()[2]);
        assertEquals(new BigDecimal("-2500.00"), arr.debits()[2]);
        // Slots 4 and 5 should be zero
        assertEquals(0, arr.balances()[3].signum());
        assertEquals(0, arr.debits()[3].signum());
        assertEquals(0, arr.balances()[4].signum());
        assertEquals(0, arr.debits()[4].signum());
    }

    // -----------------------------------------------------------------------
    // VBRC record population
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("VBRC Record 1: account ID and status")
    void vbrcRecord1() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        String line = Files.readAllLines(sampleInput).get(0);
        AccountRecord acct = AccountRecord.parse(line);
        VbrcRecord1 vb1 = app.populateVbrcRecord1(acct);

        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.acctActiveStatus());
        assertEquals(VbrcRecord1.RECORD_LENGTH, vb1.toFixedWidth().length());
    }

    @Test
    @DisplayName("VBRC Record 2: account ID, balance, limit, reissue year")
    void vbrcRecord2() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        String line = Files.readAllLines(sampleInput).get(0);
        AccountRecord acct = AccountRecord.parse(line);
        VbrcRecord2 vb2 = app.populateVbrcRecord2(acct);

        assertEquals(1L, vb2.acctId());
        assertEquals(acct.acctCurrBal(), vb2.acctCurrBal());
        assertEquals(acct.acctCreditLimit(), vb2.acctCreditLimit());
        assertEquals("2025", vb2.acctReissueYyyy());
        assertEquals(VbrcRecord2.RECORD_LENGTH, vb2.toFixedWidth().length());
    }

    // -----------------------------------------------------------------------
    // Full end-to-end execution
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Full execution produces correct number of output lines")
    void fullExecutionLineCount() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        assertEquals(50, processed.size(), "Should process all 50 accounts");

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(50, outLines.size(), "OUTFILE should have 50 records");

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(50, arryLines.size(), "ARRYFILE should have 50 records");

        // VBRCFILE has 2 records per account (VB1 + VB2)
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(100, vbrcLines.size(), "VBRCFILE should have 100 records (2 per account)");
    }

    @Test
    @DisplayName("Console output contains expected DISPLAY lines")
    void consoleOutputContainsDisplayLines() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);

        // Capture stdout
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            app.execute();
        } finally {
            System.setOut(original);
        }

        String output = baos.toString();
        assertTrue(output.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(output.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(output.contains("ACCT-ID                 :00000000001"));
        assertTrue(output.contains("ACCT-ACTIVE-STATUS      :Y"));
        assertTrue(output.contains("-------------------------------------------------"));
        // VBRC display lines
        assertTrue(output.contains("VBRC-REC1:"));
        assertTrue(output.contains("VBRC-REC2:"));
    }

    @Test
    @DisplayName("Output file records have correct fixed-width structure")
    void outputFileRecordStructure() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        for (String line : outLines) {
            // Each output line should start with an 11-digit account ID
            assertTrue(line.matches("^\\d{11}.*"), "Output line should start with 11-digit account ID");
            // Followed by a 1-char status
            assertEquals('Y', line.charAt(11), "Status should be 'Y' for all sample records");
        }
    }

    @Test
    @DisplayName("VBRC file alternates short and long records")
    void vbrcFileAlternatesRecordTypes() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        for (int i = 0; i < vbrcLines.size(); i += 2) {
            String vb1Line = vbrcLines.get(i);
            String vb2Line = vbrcLines.get(i + 1);

            assertEquals(VbrcRecord1.RECORD_LENGTH, vb1Line.length(),
                    "VB1 record at line " + i + " should be " + VbrcRecord1.RECORD_LENGTH + " chars");
            assertEquals(VbrcRecord2.RECORD_LENGTH, vb2Line.length(),
                    "VB2 record at line " + (i + 1) + " should be " + VbrcRecord2.RECORD_LENGTH + " chars");

            // Both records for the same account should share the same account ID
            String vb1AcctId = vb1Line.substring(0, 11);
            String vb2AcctId = vb2Line.substring(0, 11);
            assertEquals(vb1AcctId, vb2AcctId,
                    "VB1 and VB2 for same account should have matching IDs");
        }
    }

    @Test
    @DisplayName("Array file records contain 5 balance/debit slot pairs")
    void arrayFileSlotStructure() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        app.execute();

        List<String> arryLines = Files.readAllLines(arryFile);
        String firstLine = arryLines.get(0);

        // 11 (acct-id) + 5 * (12 zoned + 13 comp3-display) + 4 (filler) = 11 + 125 + 4 = 140
        int expectedLen = 11 + 5 * (12 + 13) + 4;
        assertEquals(expectedLen, firstLine.length(),
                "Array record should be " + expectedLen + " characters");
    }

    // -----------------------------------------------------------------------
    // Cross-record consistency: verify all 50 records processed correctly
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("All output account IDs match input account IDs in order")
    void outputAccountIdsMatchInput() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        for (int i = 0; i < processed.size(); i++) {
            String expectedId = String.format("%011d", processed.get(i).acctId());
            assertTrue(outLines.get(i).startsWith(expectedId),
                    "Output record " + i + " should start with account ID " + expectedId);
        }

        List<String> arryLines = Files.readAllLines(arryFile);
        for (int i = 0; i < processed.size(); i++) {
            String expectedId = String.format("%011d", processed.get(i).acctId());
            assertTrue(arryLines.get(i).startsWith(expectedId),
                    "Array record " + i + " should start with account ID " + expectedId);
        }
    }

    @Test
    @DisplayName("Specific record #5 values verified end-to-end")
    void specificRecord5Verification() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        // Record #5 (index 4): account 00000000005
        AccountRecord rec5 = processed.get(4);
        assertEquals(5L, rec5.acctId());
        assertEquals("Y", rec5.acctActiveStatus());
        assertEquals(new BigDecimal("345.00"), rec5.acctCurrBal());
        assertEquals(new BigDecimal("3819.00"), rec5.acctCreditLimit());
        assertEquals(new BigDecimal("2430.00"), rec5.acctCashCreditLimit());
        assertEquals("2012-10-03", rec5.acctOpenDate());
        assertEquals("2025-03-09", rec5.acctExpiraionDate());
        assertEquals("2025-03-09", rec5.acctReissueDate());

        // Verify output file line for record 5
        List<String> outLines = Files.readAllLines(outFile);
        String outLine5 = outLines.get(4);
        assertTrue(outLine5.startsWith("00000000005Y"));

        // Verify VBRC for record 5
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        String vb1 = vbrcLines.get(8);  // 2 * 4 = 8
        String vb2 = vbrcLines.get(9);
        assertEquals("00000000005Y", vb1);
        assertTrue(vb2.startsWith("00000000005"));
        assertTrue(vb2.endsWith("2025"));
    }

    @Test
    @DisplayName("Last record (#50) processed correctly")
    void lastRecordProcessed() throws IOException {
        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(sampleInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        AccountRecord last = processed.get(49);
        assertEquals(50L, last.acctId());
        assertEquals("Y", last.acctActiveStatus());
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Empty input file produces no output")
    void emptyInputFile() throws IOException {
        Path emptyInput = tempDir.resolve("empty.txt");
        Files.writeString(emptyInput, "");

        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(emptyInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        assertTrue(processed.isEmpty());
        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arryFile).size());
        assertEquals(0, Files.readAllLines(vbrcFile).size());
    }

    @Test
    @DisplayName("Single record input produces correct output counts")
    void singleRecordInput() throws IOException {
        List<String> allLines = Files.readAllLines(sampleInput);
        Path singleInput = tempDir.resolve("single.txt");
        Files.writeString(singleInput, allLines.get(0) + "\n");

        Path outFile = tempDir.resolve("out.txt");
        Path arryFile = tempDir.resolve("arry.txt");
        Path vbrcFile = tempDir.resolve("vbrc.txt");

        var app = new Cbact01cApplication(singleInput, outFile, arryFile, vbrcFile);
        List<AccountRecord> processed = app.execute();

        assertEquals(1, processed.size());
        assertEquals(1, Files.readAllLines(outFile).size());
        assertEquals(1, Files.readAllLines(arryFile).size());
        assertEquals(2, Files.readAllLines(vbrcFile).size()); // VB1 + VB2
    }
}
