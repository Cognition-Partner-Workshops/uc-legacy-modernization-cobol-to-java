package com.cardemo.batch;

import com.cardemo.batch.model.*;
import com.cardemo.batch.service.AccountFileProcessor;
import com.cardemo.batch.service.DateFormatService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests verifying the Java implementation produces identical results
 * to the COBOL CBACT01C batch program for sample inputs.
 */
class Cbact01cApplicationTest {

    @TempDir
    Path tempDir;

    private Path sampleInput;

    @BeforeEach
    void setUp() throws IOException {
        sampleInput = tempDir.resolve("acctfile.dat");
        // Same format as VSAM sequential read: fixed-width fields matching CVACT01Y layout
        // PIC 9(11) | X(1) | S9(10)V99 | S9(10)V99 | S9(10)V99 | X(10) | X(10) | X(10) | S9(10)V99 | S9(10)V99 | X(10) | X(10) | X(178)
        Files.writeString(sampleInput, String.join("\n",
                buildFixedWidthRecord(1L, "Y", "500.00", "10000.00", "2500.00",
                        "2020-01-15", "2025-12-31", "2023-06-15",
                        "150.00", "0.00", "10001     ", "GRP001    "),
                buildFixedWidthRecord(2L, "N", "1250.99", "20000.00", "5000.00",
                        "2019-03-20", "2024-03-20", "2022-11-01",
                        "320.00", "127.50", "20002     ", "GRP002    "),
                buildFixedWidthRecord(3L, "Y", "0.00", "15000.00", "7500.00",
                        "2021-07-01", "2026-07-01", "2024-01-30",
                        "0.00", "450.00", "30003     ", "GRP001    ")
        ));
    }

    // ===================== Integration Test =====================

    @Test
    @DisplayName("Full batch run produces correct output file count and content")
    void testFullBatchProcessing() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        int count = Cbact01cApplication.processAccountFile(sampleInput, outFile, arryFile, vbrcFile);

        assertEquals(3, count, "Should process exactly 3 records");

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arryLines = Files.readAllLines(arryFile);
        List<String> vbrcLines = Files.readAllLines(vbrcFile);

        assertEquals(3, outLines.size(), "OUTFILE should have 3 lines");
        assertEquals(3, arryLines.size(), "ARRYFILE should have 3 lines");
        assertEquals(6, vbrcLines.size(), "VBRCFILE should have 6 lines (2 per record)");
    }

    // ===================== Record 1 Parity Tests =====================

    @Test
    @DisplayName("Record 1: zero debit gets substituted with 2525.00 (COBOL business rule)")
    void testZeroDebitSubstitution() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.processAccountFile(sampleInput, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        String record1 = outLines.get(0);
        // The debit field should be 2525.00, not 0.00
        assertTrue(record1.contains("2525.00"),
                "When ACCT-CURR-CYC-DEBIT = 0, output should be 2525.00. Got: " + record1);
        assertFalse(record1.endsWith("|0.00|"),
                "Zero debit should NOT appear in output for record 1");
    }

    @Test
    @DisplayName("Record 2: non-zero debit passes through unchanged")
    void testNonZeroDebitPassthrough() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.processAccountFile(sampleInput, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        String record2 = outLines.get(1);
        assertTrue(record2.contains("127.50"),
                "Non-zero debit should pass through. Got: " + record2);
    }

    // ===================== Date Format Parity Tests =====================

    @ParameterizedTest
    @DisplayName("COBDATFT date conversion: YYYY-MM-DD -> YYYYMMDD")
    @CsvSource({
            "2023-06-15, 20230615",
            "2022-11-01, 20221101",
            "2024-01-30, 20240130"
    })
    void testDateConversionYyyyMmDdToCompact(String input, String expected) {
        String result = DateFormatService.convert(input, "2", "2");
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @DisplayName("COBDATFT date conversion: YYYYMMDD -> YYYY-MM-DD")
    @CsvSource({
            "20230615, 2023-06-15",
            "20221101, 2022-11-01",
            "20240130, 2024-01-30"
    })
    void testDateConversionCompactToYyyyMmDd(String input, String expected) {
        String result = DateFormatService.convert(input, "1", "1");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Reissue date in output matches COBDATFT conversion")
    void testReissueDateInOutput() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.processAccountFile(sampleInput, outFile, arryFile, vbrcFile);

        List<String> outLines = Files.readAllLines(outFile);
        // Record 1: reissue date 2023-06-15 -> 20230615
        assertTrue(outLines.get(0).contains("20230615"),
                "Record 1 reissue date should be reformatted to YYYYMMDD");
        // Record 2: reissue date 2022-11-01 -> 20221101
        assertTrue(outLines.get(1).contains("20221101"),
                "Record 2 reissue date should be reformatted to YYYYMMDD");
    }

    // ===================== Array Record Parity Tests =====================

    @Test
    @DisplayName("Array record follows COBOL OCCURS pattern: indices 1-3 populated, 4-5 zero")
    void testArrayRecordPopulation() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", new BigDecimal("500.00"), new BigDecimal("10000.00"),
                new BigDecimal("2500.00"), "2020-01-15", "2025-12-31", "2023-06-15",
                new BigDecimal("150.00"), BigDecimal.ZERO, "10001     ", "GRP001    ");

        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct);

        assertEquals(5, arr.entries().size());
        // Index 0: currBal = account balance, debit = 1005.00
        assertEquals(new BigDecimal("500.00"), arr.entries().get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.entries().get(0).currCycDebit());
        // Index 1: currBal = account balance, debit = 1525.00
        assertEquals(new BigDecimal("500.00"), arr.entries().get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.entries().get(1).currCycDebit());
        // Index 2: hardcoded values
        assertEquals(new BigDecimal("-1025.00"), arr.entries().get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.entries().get(2).currCycDebit());
        // Index 3-4: zeros (INITIALIZE)
        assertEquals(BigDecimal.ZERO, arr.entries().get(3).currBal());
        assertEquals(BigDecimal.ZERO, arr.entries().get(4).currBal());
    }

    // ===================== Variable-Length Record Parity Tests =====================

    @Test
    @DisplayName("VB Record 1 contains only account ID and status (12-byte equivalent)")
    void testVbRecord1() {
        AccountRecord acct = new AccountRecord(
                2L, "N", new BigDecimal("1250.99"), new BigDecimal("20000.00"),
                new BigDecimal("5000.00"), "2019-03-20", "2024-03-20", "2022-11-01",
                new BigDecimal("320.00"), new BigDecimal("127.50"), "20002     ", "GRP002    ");

        VbRecord1 vb1 = AccountFileProcessor.buildVbRecord1(acct);

        assertEquals(2L, vb1.acctId());
        assertEquals("N", vb1.activeStatus());
        assertEquals("2|N", vb1.toDelimitedString());
    }

    @Test
    @DisplayName("VB Record 2 contains ID, balance, credit limit, reissue year (39-byte equivalent)")
    void testVbRecord2() {
        AccountRecord acct = new AccountRecord(
                2L, "N", new BigDecimal("1250.99"), new BigDecimal("20000.00"),
                new BigDecimal("5000.00"), "2019-03-20", "2024-03-20", "2022-11-01",
                new BigDecimal("320.00"), new BigDecimal("127.50"), "20002     ", "GRP002    ");

        VbRecord2 vb2 = AccountFileProcessor.buildVbRecord2(acct);

        assertEquals(2L, vb2.acctId());
        assertEquals(new BigDecimal("1250.99"), vb2.currBal());
        assertEquals(new BigDecimal("20000.00"), vb2.creditLimit());
        assertEquals("2022", vb2.reissueYear());
    }

    @Test
    @DisplayName("VBRC file has alternating VB1/VB2 pairs per account")
    void testVbrcFileStructure() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication.processAccountFile(sampleInput, outFile, arryFile, vbrcFile);

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        // VB1 lines have 2 fields, VB2 lines have 4 fields
        for (int i = 0; i < vbrcLines.size(); i += 2) {
            String vb1Line = vbrcLines.get(i);
            String vb2Line = vbrcLines.get(i + 1);
            assertEquals(2, vb1Line.split("\\|").length,
                    "VB1 record should have 2 pipe-delimited fields at line " + i);
            assertEquals(4, vb2Line.split("\\|").length,
                    "VB2 record should have 4 pipe-delimited fields at line " + (i + 1));
        }
    }

    // ===================== AccountRecord Parsing Tests =====================

    @Test
    @DisplayName("AccountRecord.parse correctly maps fixed-width fields")
    void testAccountRecordParsing() {
        String line = buildFixedWidthRecord(12345L, "Y", "999.99", "50000.00", "10000.00",
                "2020-01-01", "2025-01-01", "2023-01-01",
                "100.00", "50.00", "ZIP123    ", "GRPX      ");

        AccountRecord acct = AccountRecord.parse(line);

        assertEquals(12345L, acct.acctId());
        assertEquals("Y", acct.activeStatus());
        assertEquals(new BigDecimal("999.99"), acct.currBal());
        assertEquals(new BigDecimal("50000.00"), acct.creditLimit());
        assertEquals(new BigDecimal("10000.00"), acct.cashCreditLimit());
        assertEquals("2020-01-01", acct.openDate());
        assertEquals("2025-01-01", acct.expirationDate());
        assertEquals("2023-01-01", acct.reissueDate());
        assertEquals(new BigDecimal("100.00"), acct.currCycCredit());
        assertEquals(new BigDecimal("50.00"), acct.currCycDebit());
        assertEquals("ZIP123    ", acct.addrZip());
        assertEquals("GRPX      ", acct.groupId());
    }

    @Test
    @DisplayName("AccountRecord.parse rejects too-short input")
    void testAccountRecordParseTooShort() {
        assertThrows(IllegalArgumentException.class, () -> AccountRecord.parse("short"));
    }

    // ===================== Error Handling Tests =====================

    @Test
    @DisplayName("Missing input file triggers IOException (mirrors COBOL ABEND)")
    void testMissingInputFile() {
        Path missing = tempDir.resolve("nonexistent.dat");
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        IOException ex = assertThrows(IOException.class,
                () -> Cbact01cApplication.processAccountFile(missing, outFile, arryFile, vbrcFile));
        assertTrue(ex.getMessage().contains("ERROR OPENING ACCTFILE"));
    }

    // ===================== Edge Case Tests =====================

    @Test
    @DisplayName("Empty input file produces empty outputs (0 records processed)")
    void testEmptyInputFile() throws IOException {
        Path emptyInput = tempDir.resolve("empty.dat");
        Files.writeString(emptyInput, "");
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        int count = Cbact01cApplication.processAccountFile(emptyInput, outFile, arryFile, vbrcFile);

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
    }

    @Test
    @DisplayName("DateFormatService handles blank/null input gracefully")
    void testDateFormatEdgeCases() {
        assertEquals("", DateFormatService.convert("", "2", "2"));
        assertEquals("", DateFormatService.convert(null, "2", "2"));
        assertEquals("    ", DateFormatService.extractYear(null));
        assertEquals("    ", DateFormatService.extractYear("ab"));
    }

    // ===================== Helper =====================

    private static String buildFixedWidthRecord(long id, String status,
            String bal, String creditLimit, String cashLimit,
            String openDate, String expDate, String reissueDate,
            String cycCredit, String cycDebit,
            String zip, String groupId) {
        // Match CVACT01Y layout: 9(11) X(1) S9(10)V99 S9(10)V99 S9(10)V99 X(10) X(10) X(10) S9(10)V99 S9(10)V99 X(10) X(10) X(178)
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", id));           // 11 chars
        sb.append(String.format("%-1s", status));         // 1 char
        sb.append(String.format("%12s", bal));            // 12 chars (signed decimal)
        sb.append(String.format("%12s", creditLimit));    // 12 chars
        sb.append(String.format("%12s", cashLimit));      // 12 chars
        sb.append(String.format("%-10s", openDate));      // 10 chars
        sb.append(String.format("%-10s", expDate));       // 10 chars
        sb.append(String.format("%-10s", reissueDate));   // 10 chars
        sb.append(String.format("%12s", cycCredit));      // 12 chars
        sb.append(String.format("%12s", cycDebit));       // 12 chars
        sb.append(String.format("%-10s", zip));           // 10 chars
        sb.append(String.format("%-10s", groupId));       // 10 chars
        // Pad to 300 with spaces (FILLER PIC X(178))
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
