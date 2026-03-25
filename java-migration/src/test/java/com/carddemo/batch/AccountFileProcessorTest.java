package com.carddemo.batch;

import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord;
import com.carddemo.util.DateConverter;

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
 * JUnit 5 tests that verify the Java port of CBACT01C produces
 * results identical to the original COBOL program for sample inputs.
 */
class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() {
        outFile = tempDir.resolve("outfile.dat");
        arryFile = tempDir.resolve("arryfile.dat");
        vbrcFile = tempDir.resolve("vbrcfile.dat");
    }

    // ────────────────────────────────────────────────────────────
    // AccountRecord parsing (CVACT01Y copybook)
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("AccountRecord parsing — CVACT01Y copybook")
    class AccountRecordParsingTest {

        @Test
        @DisplayName("Parse first sample record — account 00000000001")
        void parseFirstRecord() {
            // Line 1 of acctdata.txt (padded to 300 chars)
            String line = buildSampleLine(
                    "00000000001", "Y",
                    "00000001940{", "00000020200{", "00000010200{",
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    "00000000000{", "00000000000{",
                    "A000000000", "          ");

            AccountRecord rec = AccountRecord.parse(line);

            assertEquals(1L, rec.acctId());
            assertEquals("Y", rec.activeStatus());
            assertEquals(new BigDecimal("194.00"), rec.currBal());
            assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
            assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
            assertEquals("2014-11-20", rec.openDate());
            assertEquals("2025-05-20", rec.expirationDate());
            assertEquals("2025-05-20", rec.reissueDate());
            assertEquals(new BigDecimal("0.00"), rec.currCycCredit());
            assertEquals(new BigDecimal("0.00"), rec.currCycDebit());
            assertEquals("          ", rec.groupId(),
                    "In sample data GROUP-ID (pos 112-121) is spaces; A000000000 is ADDR-ZIP");
        }

        @Test
        @DisplayName("Parse record with larger balances — account 00000000039")
        void parseLargerBalanceRecord() {
            String line = buildSampleLine(
                    "00000000039", "Y",
                    "00000008430{", "00000097500{", "00000062120{",
                    "2018-08-26", "2025-09-08", "2025-09-08",
                    "00000000000{", "00000000000{",
                    "A000000000", "          ");

            AccountRecord rec = AccountRecord.parse(line);

            assertEquals(39L, rec.acctId());
            assertEquals(new BigDecimal("843.00"), rec.currBal());
            assertEquals(new BigDecimal("9750.00"), rec.creditLimit());
            assertEquals(new BigDecimal("6212.00"), rec.cashCreditLimit());
        }

        @Test
        @DisplayName("Zoned-decimal: positive sign overpunch '{' → +0")
        void zonedDecimalPositiveZero() {
            BigDecimal result = AccountRecord.parseSignedDecimal("00000001940{", 2);
            assertEquals(new BigDecimal("194.00"), result);
        }

        @Test
        @DisplayName("Zoned-decimal: negative sign overpunch 'J' → -1")
        void zonedDecimalNegative() {
            BigDecimal result = AccountRecord.parseSignedDecimal("0000000100J", 2);
            assertEquals(new BigDecimal("-10.01"), result);
        }

        @Test
        @DisplayName("Zoned-decimal: all-zero with '{' → 0.00")
        void zonedDecimalZero() {
            BigDecimal result = AccountRecord.parseSignedDecimal("00000000000{", 2);
            assertEquals(new BigDecimal("0.00"), result);
        }

        @Test
        @DisplayName("Record too short throws IllegalArgumentException")
        void recordTooShort() {
            assertThrows(IllegalArgumentException.class,
                    () -> AccountRecord.parse("short"));
        }
    }

    // ────────────────────────────────────────────────────────────
    // DateConverter (replaces COBDATFT assembler call)
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DateConverter — COBDATFT replacement")
    class DateConverterTest {

        @Test
        @DisplayName("YYYY-MM-DD → YYYYMMDD (type 2 → type 2)")
        void dashToCompact() {
            assertEquals("20250520", DateConverter.convert("2025-05-20", "2", "2"));
        }

        @Test
        @DisplayName("YYYYMMDD → YYYY-MM-DD (type 1 → type 1)")
        void compactToDash() {
            assertEquals("2025-05-20", DateConverter.convert("20250520", "1", "1"));
        }

        @Test
        @DisplayName("YYYY-MM-DD → YYYY-MM-DD (type 2 → type 1, identity)")
        void dashToDash() {
            assertEquals("2025-09-08", DateConverter.convert("2025-09-08", "2", "1"));
        }

        @Test
        @DisplayName("toCompactDate convenience method")
        void toCompactDate() {
            assertEquals("20140227", DateConverter.toCompactDate("2014-02-27"));
        }

        @Test
        @DisplayName("Blank input returns empty string")
        void blankInput() {
            assertEquals("", DateConverter.convert("", "2", "2"));
            assertEquals("", DateConverter.convert("   ", "2", "2"));
        }
    }

    // ────────────────────────────────────────────────────────────
    // OutputAccountRecord (1300-POPUL-ACCT-RECORD)
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Output record population — 1300-POPUL-ACCT-RECORD")
    class OutputRecordTest {

        @Test
        @DisplayName("Debit is zero → substituted with 2525.00")
        void zeroDebitDefaulted() {
            AccountRecord acct = new AccountRecord(
                    1L, "Y",
                    new BigDecimal("194.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    new BigDecimal("0.00"), BigDecimal.ZERO, "", "A000000000");

            AccountFileProcessor processor = createProcessor();
            OutputAccountRecord out = processor.buildOutputRecord(acct);

            assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                    "COBOL: IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00");
        }

        @Test
        @DisplayName("Non-zero debit is preserved")
        void nonZeroDebitPreserved() {
            AccountRecord acct = new AccountRecord(
                    2L, "Y",
                    new BigDecimal("158.00"), new BigDecimal("6130.00"), new BigDecimal("5448.00"),
                    "2013-06-19", "2024-08-11", "2024-08-11",
                    new BigDecimal("0.00"), new BigDecimal("100.50"), "", "A000000000");

            AccountFileProcessor processor = createProcessor();
            OutputAccountRecord out = processor.buildOutputRecord(acct);

            assertEquals(new BigDecimal("100.50"), out.currCycDebit());
        }

        @Test
        @DisplayName("Reissue date reformatted: YYYY-MM-DD → YYYYMMDD")
        void reissueDateFormatted() {
            AccountRecord acct = new AccountRecord(
                    1L, "Y",
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    BigDecimal.ZERO, BigDecimal.ZERO, "", "A000000000");

            AccountFileProcessor processor = createProcessor();
            OutputAccountRecord out = processor.buildOutputRecord(acct);

            assertEquals("20250520", out.reissueDate(),
                    "COBOL: CALL 'COBDATFT' converts YYYY-MM-DD to YYYYMMDD");
        }

        @Test
        @DisplayName("Output line format is pipe-delimited")
        void outputLineFormat() {
            OutputAccountRecord rec = new OutputAccountRecord(
                    1L, "Y",
                    new BigDecimal("194.00"), new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                    "2014-11-20", "2025-05-20", "20250520",
                    new BigDecimal("0.00"), new BigDecimal("2525.00"), "A000000000");

            String line = rec.toOutputLine();
            String[] parts = line.split("\\|");
            assertEquals(11, parts.length, "Output should have 11 pipe-delimited fields");
            assertEquals("00000000001", parts[0]);
            assertEquals("Y", parts[1]);
        }
    }

    // ────────────────────────────────────────────────────────────
    // ArrayRecord (1400-POPUL-ARRAY-RECORD)
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Array record population — 1400-POPUL-ARRAY-RECORD")
    class ArrayRecordTest {

        @Test
        @DisplayName("Slot 1: balance = account balance, debit = 1005.00")
        void slot1() {
            AccountRecord acct = acctWithBalance(new BigDecimal("194.00"));
            AccountFileProcessor processor = createProcessor();
            ArrayRecord arr = processor.buildArrayRecord(acct);

            assertEquals(new BigDecimal("194.00"), arr.balances()[0]);
            assertEquals(new BigDecimal("1005.00"), arr.debits()[0]);
        }

        @Test
        @DisplayName("Slot 2: balance = account balance, debit = 1525.00")
        void slot2() {
            AccountRecord acct = acctWithBalance(new BigDecimal("194.00"));
            AccountFileProcessor processor = createProcessor();
            ArrayRecord arr = processor.buildArrayRecord(acct);

            assertEquals(new BigDecimal("194.00"), arr.balances()[1]);
            assertEquals(new BigDecimal("1525.00"), arr.debits()[1]);
        }

        @Test
        @DisplayName("Slot 3: hardcoded balance = -1025.00, debit = -2500.00")
        void slot3() {
            AccountRecord acct = acctWithBalance(new BigDecimal("194.00"));
            AccountFileProcessor processor = createProcessor();
            ArrayRecord arr = processor.buildArrayRecord(acct);

            assertEquals(new BigDecimal("-1025.00"), arr.balances()[2]);
            assertEquals(new BigDecimal("-2500.00"), arr.debits()[2]);
        }

        @Test
        @DisplayName("Slots 4–5 remain zero (INITIALIZE)")
        void slotsRemainZero() {
            AccountRecord acct = acctWithBalance(new BigDecimal("194.00"));
            AccountFileProcessor processor = createProcessor();
            ArrayRecord arr = processor.buildArrayRecord(acct);

            assertEquals(BigDecimal.ZERO, arr.balances()[3]);
            assertEquals(BigDecimal.ZERO, arr.debits()[3]);
            assertEquals(BigDecimal.ZERO, arr.balances()[4]);
            assertEquals(BigDecimal.ZERO, arr.debits()[4]);
        }

        @Test
        @DisplayName("Account ID is propagated")
        void acctIdPropagated() {
            AccountRecord acct = acctWithBalance(new BigDecimal("100.00"));
            AccountFileProcessor processor = createProcessor();
            ArrayRecord arr = processor.buildArrayRecord(acct);

            assertEquals(1L, arr.acctId());
        }
    }

    // ────────────────────────────────────────────────────────────
    // VariableLengthRecord (1500-POPUL-VBRC-RECORD)
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Variable-length records — 1500-POPUL-VBRC-RECORD")
    class VbrRecordTest {

        @Test
        @DisplayName("VBR Type1 carries account ID and active status")
        void vbr1Fields() {
            AccountRecord acct = acctWithBalance(new BigDecimal("194.00"));
            AccountFileProcessor processor = createProcessor();
            VariableLengthRecord.Type1 vb1 = processor.buildVbr1(acct);

            assertEquals(1L, vb1.acctId());
            assertEquals("Y", vb1.activeStatus());
        }

        @Test
        @DisplayName("VBR Type2 carries balance, credit limit, and reissue year")
        void vbr2Fields() {
            AccountRecord acct = new AccountRecord(
                    1L, "Y",
                    new BigDecimal("194.00"), new BigDecimal("2020.00"), BigDecimal.ZERO,
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    BigDecimal.ZERO, BigDecimal.ZERO, "", "A000000000");

            AccountFileProcessor processor = createProcessor();
            VariableLengthRecord.Type2 vb2 = processor.buildVbr2(acct);

            assertEquals(1L, vb2.acctId());
            assertEquals(new BigDecimal("194.00"), vb2.currBal());
            assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
            assertEquals("2025", vb2.reissueYear(),
                    "COBOL: MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY");
        }

        @Test
        @DisplayName("VBR Type1 output line starts with VB1 prefix")
        void vbr1OutputFormat() {
            var vb1 = new VariableLengthRecord.Type1(1L, "Y");
            assertTrue(vb1.toOutputLine().startsWith("VB1|"));
        }

        @Test
        @DisplayName("VBR Type2 output line starts with VB2 prefix")
        void vbr2OutputFormat() {
            var vb2 = new VariableLengthRecord.Type2(
                    1L, new BigDecimal("194.00"), new BigDecimal("2020.00"), "2025");
            assertTrue(vb2.toOutputLine().startsWith("VB2|"));
        }
    }

    // ────────────────────────────────────────────────────────────
    // Full end-to-end processing with sample data
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("End-to-end processing with acctdata.txt")
    class EndToEndTest {

        @Test
        @DisplayName("Process all 50 sample records — count matches")
        void processAllRecords() throws IOException {
            Path inputFile = getTestDataPath();
            AccountFileProcessor processor =
                    new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);

            int count = processor.execute();

            assertEquals(50, count, "acctdata.txt contains 50 account records");
        }

        @Test
        @DisplayName("Output file has 50 lines")
        void outputFileLineCount() throws IOException {
            runProcessor();
            List<String> lines = Files.readAllLines(outFile);
            assertEquals(50, lines.size());
        }

        @Test
        @DisplayName("Array file has 50 lines")
        void arrayFileLineCount() throws IOException {
            runProcessor();
            List<String> lines = Files.readAllLines(arryFile);
            assertEquals(50, lines.size());
        }

        @Test
        @DisplayName("VBR file has 100 lines (2 per account)")
        void vbrFileLineCount() throws IOException {
            runProcessor();
            List<String> lines = Files.readAllLines(vbrcFile);
            assertEquals(100, lines.size(), "Each account produces 2 VBR records (Type1 + Type2)");
        }

        @Test
        @DisplayName("First output record matches expected values for account 1")
        void firstOutputRecord() throws IOException {
            AccountFileProcessor processor = runProcessor();

            OutputAccountRecord first = processor.getOutputRecords().get(0);
            assertEquals(1L, first.acctId());
            assertEquals("Y", first.activeStatus());
            assertEquals(new BigDecimal("194.00"), first.currBal());
            assertEquals(new BigDecimal("2020.00"), first.creditLimit());
            assertEquals(new BigDecimal("1020.00"), first.cashCreditLimit());
            assertEquals("2014-11-20", first.openDate());
            assertEquals("2025-05-20", first.expirationDate());
            assertEquals("20250520", first.reissueDate(), "Reissue date should be YYYYMMDD");
            assertEquals(new BigDecimal("0.00"), first.currCycCredit());
            assertEquals(new BigDecimal("2525.00"), first.currCycDebit(),
                    "Zero debit should be defaulted to 2525.00");
            assertEquals("          ", first.groupId(),
                    "GROUP-ID is spaces in the sample data");
        }

        @Test
        @DisplayName("All records have zero debit defaulted to 2525.00")
        void allZeroDebitsDefaulted() throws IOException {
            AccountFileProcessor processor = runProcessor();

            // In the sample data, all cycle debits are 0 → all should be 2525.00
            for (OutputAccountRecord rec : processor.getOutputRecords()) {
                assertEquals(new BigDecimal("2525.00"), rec.currCycDebit(),
                        "Account " + rec.acctId() + ": zero debit should default to 2525.00");
            }
        }

        @Test
        @DisplayName("Array records slot 3 always has -1025.00 / -2500.00")
        void arraySlot3Consistent() throws IOException {
            AccountFileProcessor processor = runProcessor();

            for (ArrayRecord arr : processor.getArrayRecords()) {
                assertEquals(new BigDecimal("-1025.00"), arr.balances()[2],
                        "Account " + arr.acctId() + ": slot 3 balance should be -1025.00");
                assertEquals(new BigDecimal("-2500.00"), arr.debits()[2],
                        "Account " + arr.acctId() + ": slot 3 debit should be -2500.00");
            }
        }

        @Test
        @DisplayName("VBR records alternate Type1 and Type2")
        void vbrRecordsAlternate() throws IOException {
            AccountFileProcessor processor = runProcessor();
            List<VariableLengthRecord> vbrs = processor.getVbrRecords();

            for (int i = 0; i < vbrs.size(); i += 2) {
                assertInstanceOf(VariableLengthRecord.Type1.class, vbrs.get(i),
                        "Even index should be Type1");
                assertInstanceOf(VariableLengthRecord.Type2.class, vbrs.get(i + 1),
                        "Odd index should be Type2");
                assertEquals(vbrs.get(i).acctId(), vbrs.get(i + 1).acctId(),
                        "Type1 and Type2 should share the same account ID");
            }
        }

        @Test
        @DisplayName("Display log starts and ends with execution markers")
        void displayLogMarkers() throws IOException {
            AccountFileProcessor processor = runProcessor();
            List<String> log = processor.getDisplayLog();

            assertEquals("START OF EXECUTION OF PROGRAM CBACT01C", log.get(0));
            assertEquals("END OF EXECUTION OF PROGRAM CBACT01C", log.get(log.size() - 1));
        }

        @Test
        @DisplayName("Display log contains 12 lines per record (11 fields + separator)")
        void displayLogPerRecord() throws IOException {
            AccountFileProcessor processor = runProcessor();
            List<String> log = processor.getDisplayLog();
            // 2 markers + 50 * 12 lines per record = 602
            assertEquals(602, log.size());
        }

        @Test
        @DisplayName("Last record (account 50) is correctly processed")
        void lastRecord() throws IOException {
            AccountFileProcessor processor = runProcessor();
            OutputAccountRecord last = processor.getOutputRecords().get(49);

            assertEquals(50L, last.acctId());
            assertEquals("Y", last.activeStatus());
            assertEquals(new BigDecimal("492.00"), last.currBal());
        }

        @Test
        @DisplayName("Reissue year in VBR Type2 matches first 4 chars of reissue date")
        void reissueYearExtraction() throws IOException {
            AccountFileProcessor processor = runProcessor();
            List<VariableLengthRecord> vbrs = processor.getVbrRecords();

            // Check account 1: reissue date = 2025-05-20 → year = 2025
            VariableLengthRecord.Type2 vb2 = (VariableLengthRecord.Type2) vbrs.get(1);
            assertEquals("2025", vb2.reissueYear());

            // Check account 4: reissue date = 2023-12-16 → year = 2023
            VariableLengthRecord.Type2 vb2Acct4 = (VariableLengthRecord.Type2) vbrs.get(7);
            assertEquals("2023", vb2Acct4.reissueYear());
        }

        private AccountFileProcessor runProcessor() throws IOException {
            Path inputFile = getTestDataPath();
            AccountFileProcessor processor =
                    new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);
            processor.execute();
            return processor;
        }

        private Path getTestDataPath() {
            Path testResource = Path.of("src/test/resources/acctdata.txt");
            if (Files.exists(testResource)) {
                return testResource;
            }
            // Fallback for IDE vs Maven working directory differences
            return Path.of("java-migration/src/test/resources/acctdata.txt");
        }
    }

    // ────────────────────────────────────────────────────────────
    // Edge cases
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTest {

        @Test
        @DisplayName("Empty input file produces zero records and empty outputs")
        void emptyInputFile() throws IOException {
            Path emptyInput = tempDir.resolve("empty.txt");
            Files.writeString(emptyInput, "");

            AccountFileProcessor processor =
                    new AccountFileProcessor(emptyInput, outFile, arryFile, vbrcFile);
            int count = processor.execute();

            assertEquals(0, count);
            assertTrue(processor.getOutputRecords().isEmpty());
            assertEquals(0, Files.readAllLines(outFile).size());
        }

        @Test
        @DisplayName("Single record file produces exactly 1 output in each file")
        void singleRecord() throws IOException {
            String singleLine = buildSampleLine(
                    "00000000001", "Y",
                    "00000001940{", "00000020200{", "00000010200{",
                    "2014-11-20", "2025-05-20", "2025-05-20",
                    "00000000000{", "00000000000{",
                    "A000000000", "          ");
            Path singleInput = tempDir.resolve("single.txt");
            Files.writeString(singleInput, singleLine + "\n");

            AccountFileProcessor processor =
                    new AccountFileProcessor(singleInput, outFile, arryFile, vbrcFile);
            int count = processor.execute();

            assertEquals(1, count);
            assertEquals(1, Files.readAllLines(outFile).size());
            assertEquals(1, Files.readAllLines(arryFile).size());
            assertEquals(2, Files.readAllLines(vbrcFile).size(), "VBR: 1 Type1 + 1 Type2");
        }
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private AccountFileProcessor createProcessor() {
        return new AccountFileProcessor(
                tempDir.resolve("dummy-in.txt"),
                outFile, arryFile, vbrcFile);
    }

    private AccountRecord acctWithBalance(BigDecimal balance) {
        return new AccountRecord(
                1L, "Y",
                balance, new BigDecimal("2020.00"), new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"), BigDecimal.ZERO, "", "A000000000");
    }

    /**
     * Build a 300-character fixed-width record matching the CVACT01Y layout.
     */
    static String buildSampleLine(
            String acctId, String status,
            String currBal, String creditLimit, String cashCredit,
            String openDate, String expiryDate, String reissueDate,
            String cycCredit, String cycDebit,
            String addrZip, String groupId) {

        StringBuilder sb = new StringBuilder(300);
        sb.append(acctId);          // 11
        sb.append(status);          //  1
        sb.append(currBal);         // 12
        sb.append(creditLimit);     // 12
        sb.append(cashCredit);      // 12
        sb.append(openDate);        // 10
        sb.append(expiryDate);      // 10
        sb.append(reissueDate);     // 10
        sb.append(cycCredit);       // 12
        sb.append(cycDebit);        // 12
        sb.append(addrZip);         // 10
        sb.append(groupId);         // 10
        // Pad to 300 with spaces (FILLER PIC X(178))
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
