package com.carddemo.batch;

import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayAccountRecord;
import com.carddemo.model.OutAccountRecord;
import com.carddemo.model.VbrRecord1;
import com.carddemo.model.VbrRecord2;

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

import static org.junit.jupiter.api.Assertions.*;

class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private Path acctFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;
    private ByteArrayOutputStream consoleOutput;
    private PrintStream consolePrintStream;

    /**
     * Build a 300-char fixed-length record that mirrors CVACT01Y layout.
     *
     *   ACCT-ID               PIC 9(11)      ->  11 chars
     *   ACCT-ACTIVE-STATUS    PIC X(01)      ->   1 char
     *   ACCT-CURR-BAL         PIC S9(10)V99  ->  12 chars (display numeric)
     *   ACCT-CREDIT-LIMIT     PIC S9(10)V99  ->  12 chars
     *   ACCT-CASH-CREDIT-LIM  PIC S9(10)V99  ->  12 chars
     *   ACCT-OPEN-DATE        PIC X(10)      ->  10 chars
     *   ACCT-EXPIRAION-DATE   PIC X(10)      ->  10 chars
     *   ACCT-REISSUE-DATE     PIC X(10)      ->  10 chars
     *   ACCT-CURR-CYC-CREDIT  PIC S9(10)V99  ->  12 chars
     *   ACCT-CURR-CYC-DEBIT   PIC S9(10)V99  ->  12 chars
     *   ACCT-ADDR-ZIP         PIC X(10)      ->  10 chars
     *   ACCT-GROUP-ID         PIC X(10)      ->  10 chars
     *   FILLER                PIC X(178)     -> 178 chars
     */
    private static String buildAccountLine(long acctId, String status,
                                            BigDecimal bal, BigDecimal creditLimit,
                                            BigDecimal cashCreditLimit,
                                            String openDate, String expDate,
                                            String reissueDate,
                                            BigDecimal cycCredit, BigDecimal cycDebit,
                                            String zip, String groupId) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));                  // 11
        sb.append(status);                                          //  1
        sb.append(formatSignedDecimal(bal));                        // 12
        sb.append(formatSignedDecimal(creditLimit));                // 12
        sb.append(formatSignedDecimal(cashCreditLimit));            // 12
        sb.append(String.format("%-10s", openDate));                // 10
        sb.append(String.format("%-10s", expDate));                 // 10
        sb.append(String.format("%-10s", reissueDate));             // 10
        sb.append(formatSignedDecimal(cycCredit));                  // 12
        sb.append(formatSignedDecimal(cycDebit));                   // 12
        sb.append(String.format("%-10s", zip));                     // 10
        sb.append(String.format("%-10s", groupId));                 // 10
        sb.append(" ".repeat(178));                                 // filler
        return sb.toString();
    }

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    /**
     * Format a BigDecimal as a 12-char COBOL zoned-decimal field with trailing
     * overpunch encoding, matching the production data format.
     */
    private static String formatSignedDecimal(BigDecimal val) {
        boolean negative = val.signum() < 0;
        BigDecimal abs = val.abs();
        long unscaled = abs.movePointRight(2).longValue();
        String digits = String.format("%012d", unscaled);
        int lastDigit = digits.charAt(11) - '0';
        char overpunch = negative
                ? NEGATIVE_OVERPUNCH.charAt(lastDigit)
                : POSITIVE_OVERPUNCH.charAt(lastDigit);
        return digits.substring(0, 11) + overpunch;
    }

    @BeforeEach
    void setUp() {
        acctFile = tempDir.resolve("acctfile.dat");
        outFile = tempDir.resolve("outfile.dat");
        arryFile = tempDir.resolve("arryfile.dat");
        vbrcFile = tempDir.resolve("vbrcfile.dat");
        consoleOutput = new ByteArrayOutputStream();
        consolePrintStream = new PrintStream(consoleOutput);
    }

    private AccountFileProcessor newProcessor() {
        return new AccountFileProcessor(
                acctFile, outFile, arryFile, vbrcFile, consolePrintStream);
    }

    // ------------------------------------------------------------------
    // Full end-to-end tests
    // ------------------------------------------------------------------

    @Test
    void execute_singleRecord_writesAllThreeOutputFiles() throws IOException {
        String line = buildAccountLine(
                12345678901L, "Y",
                new BigDecimal("5000.50"), new BigDecimal("10000.00"),
                new BigDecimal("3000.00"),
                "2023-01-15", "2028-01-15", "2025-06-20",
                new BigDecimal("200.00"), new BigDecimal("150.75"),
                "90210     ", "GRP001    ");

        Files.writeString(acctFile, line + "\n");

        int count = newProcessor().execute();
        assertEquals(1, count);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());

        List<String> arryLines = Files.readAllLines(arryFile);
        assertEquals(1, arryLines.size());

        // VBRC gets 2 records per input account
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(2, vbrcLines.size());
    }

    @Test
    void execute_multipleRecords_allProcessed() throws IOException {
        String line1 = buildAccountLine(
                1L, "Y",
                new BigDecimal("100.00"), new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                "2020-01-01", "2025-01-01", "2024-06-15",
                new BigDecimal("50.00"), new BigDecimal("25.00"),
                "10001     ", "GRPA      ");
        String line2 = buildAccountLine(
                2L, "N",
                new BigDecimal("200.00"), new BigDecimal("8000.00"),
                new BigDecimal("2000.00"),
                "2021-03-10", "2026-03-10", "2025-03-10",
                new BigDecimal("75.00"), new BigDecimal("0.00"),
                "10002     ", "GRPB      ");

        Files.writeString(acctFile, line1 + "\n" + line2 + "\n");

        int count = newProcessor().execute();
        assertEquals(2, count);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(2, outLines.size());

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(4, vbrcLines.size());
    }

    @Test
    void execute_emptyFile_processesZeroRecords() throws IOException {
        Files.writeString(acctFile, "");

        int count = newProcessor().execute();
        assertEquals(0, count);

        String console = consoleOutput.toString();
        assertTrue(console.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(console.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void execute_missingInputFile_throwsIOException() {
        // acctFile not created
        assertThrows(IOException.class, () -> newProcessor().execute());
    }

    // ------------------------------------------------------------------
    // Business logic tests (transformation rules)
    // ------------------------------------------------------------------

    @Test
    void populateOutRecord_zeroCycleDebit_substitutesDefault() {
        AccountRecord acct = new AccountRecord(
                99L, "Y",
                new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2023-01-01", "2028-01-01", "2025-06-20",
                new BigDecimal("300.00"), BigDecimal.ZERO.setScale(2),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        OutAccountRecord out = processor.populateOutRecord(acct);
        assertEquals(new BigDecimal("2525.00"), out.currentCycleDebit());
    }

    @Test
    void populateOutRecord_nonZeroCycleDebit_preservedAsIs() {
        AccountRecord acct = new AccountRecord(
                99L, "Y",
                new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2023-01-01", "2028-01-01", "2025-06-20",
                new BigDecimal("300.00"), new BigDecimal("150.75"),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        OutAccountRecord out = processor.populateOutRecord(acct);
        assertEquals(new BigDecimal("150.75"), out.currentCycleDebit());
    }

    @Test
    void populateOutRecord_reissueDateConverted() {
        AccountRecord acct = new AccountRecord(
                99L, "Y",
                BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                "2023-01-01", "2028-01-01", "2025-06-20",
                BigDecimal.ZERO.setScale(2), new BigDecimal("10.00"),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        OutAccountRecord out = processor.populateOutRecord(acct);
        assertEquals("20250620", out.reissueDate());
    }

    @Test
    void populateArrayRecord_slotsPopulatedCorrectly() {
        BigDecimal balance = new BigDecimal("7500.25");
        AccountRecord acct = new AccountRecord(
                42L, "Y",
                balance, new BigDecimal("20000.00"),
                new BigDecimal("5000.00"),
                "2023-01-01", "2028-01-01", "2025-06-20",
                new BigDecimal("100.00"), new BigDecimal("50.00"),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        ArrayAccountRecord arr = processor.populateArrayRecord(acct);

        assertEquals(42L, arr.acctId());

        // Slot 1: balance = account balance, debit = 1005.00
        assertEquals(balance, arr.balances()[0]);
        assertEquals(new BigDecimal("1005.00"), arr.debits()[0]);

        // Slot 2: balance = account balance, debit = 1525.00
        assertEquals(balance, arr.balances()[1]);
        assertEquals(new BigDecimal("1525.00"), arr.debits()[1]);

        // Slot 3: hardcoded negative values
        assertEquals(new BigDecimal("-1025.00"), arr.balances()[2]);
        assertEquals(new BigDecimal("-2500.00"), arr.debits()[2]);

        // Slots 4-5: zero
        assertEquals(0, arr.balances()[3].compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.debits()[3].compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.balances()[4].compareTo(BigDecimal.ZERO));
        assertEquals(0, arr.debits()[4].compareTo(BigDecimal.ZERO));
    }

    @Test
    void populateVbrRecord1_containsIdAndStatus() {
        AccountRecord acct = new AccountRecord(
                55L, "N",
                BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                "2023-01-01", "2028-01-01", "2025-06-20",
                BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        VbrRecord1 vbr1 = processor.populateVbrRecord1(acct);
        assertEquals(55L, vbr1.acctId());
        assertEquals("N", vbr1.activeStatus());

        String output = vbr1.toOutputLine();
        assertEquals(12, output.length());
        assertTrue(output.startsWith("00000000055"));
        assertTrue(output.endsWith("N"));
    }

    @Test
    void populateVbrRecord2_extractsReissueYear() {
        AccountRecord acct = new AccountRecord(
                77L, "Y",
                new BigDecimal("3000.00"), new BigDecimal("15000.00"),
                BigDecimal.ZERO.setScale(2),
                "2023-01-01", "2028-01-01", "2025-06-20",
                BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                "12345     ", "GRP1      ");

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of("x"), Path.of("x"), Path.of("x"), Path.of("x"));

        VbrRecord2 vbr2 = processor.populateVbrRecord2(acct);
        assertEquals(77L, vbr2.acctId());
        assertEquals(new BigDecimal("3000.00"), vbr2.currentBalance());
        assertEquals(new BigDecimal("15000.00"), vbr2.creditLimit());
        assertEquals("2025", vbr2.reissueYear());
    }

    // ------------------------------------------------------------------
    // Console output verification
    // ------------------------------------------------------------------

    @Test
    void execute_displaysStartAndEndMessages() throws IOException {
        Files.writeString(acctFile, "");

        newProcessor().execute();

        String output = consoleOutput.toString();
        assertTrue(output.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(output.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void execute_displaysAccountFieldsForEachRecord() throws IOException {
        String line = buildAccountLine(
                12345678901L, "Y",
                new BigDecimal("5000.50"), new BigDecimal("10000.00"),
                new BigDecimal("3000.00"),
                "2023-01-15", "2028-01-15", "2025-06-20",
                new BigDecimal("200.00"), new BigDecimal("150.75"),
                "90210     ", "GRP001    ");

        Files.writeString(acctFile, line + "\n");

        newProcessor().execute();

        String output = consoleOutput.toString();
        assertTrue(output.contains("ACCT-ID"));
        assertTrue(output.contains("12345678901"));
        assertTrue(output.contains("ACCT-ACTIVE-STATUS"));
        assertTrue(output.contains("ACCT-CURR-BAL"));
        assertTrue(output.contains("-------------------------------------------------"));
    }

    // ------------------------------------------------------------------
    // AccountRecord parsing tests
    // ------------------------------------------------------------------

    @Test
    void accountRecord_parse_productionDataLine() {
        // First line from app/data/ASCII/acctdata.txt (trailing overpunch encoded)
        String line = "00000000001Y00000001940{00000020200{00000010200{" +
                "2014-11-202025-05-202025-05-2000000000000{00000000000{" +
                "A000000000" + " ".repeat(178);

        AccountRecord acct = AccountRecord.parse(line);
        assertEquals(1L, acct.acctId());
        assertEquals("Y", acct.activeStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(acct.currentBalance()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(acct.creditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(acct.cashCreditLimit()));
        assertEquals("2014-11-20", acct.openDate());
        assertEquals("2025-05-20", acct.expirationDate());
        assertEquals("2025-05-20", acct.reissueDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(acct.currentCycleCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(acct.currentCycleDebit()));
    }

    @Test
    void accountRecord_parse_negativeOverpunch() {
        // Build a line with negative values using overpunch encoding
        // -1234.56 -> 123456 -> last digit 6 -> O (negative 6) -> 00000012345O
        String line = "00000000099Y" +
                "00000012345O" +  // curr bal = -1234.56
                "00000050000{" +  // credit limit = 5000.00
                "00000020000{" +  // cash credit limit = 2000.00
                "2023-01-01" +
                "2028-01-01" +
                "2025-06-20" +
                "00000001000{" +  // cycle credit = 100.00
                "00000000500}" +  // cycle debit = -50.00 (} = negative 0)
                "12345     " +
                "GRP1      " +
                " ".repeat(178);

        AccountRecord acct = AccountRecord.parse(line);
        assertEquals(0, new BigDecimal("-1234.56").compareTo(acct.currentBalance()));
        assertEquals(0, new BigDecimal("-50.00").compareTo(acct.currentCycleDebit()));
    }

    @Test
    void accountRecord_parse_roundTrip() {
        String line = buildAccountLine(
                99887766554L, "Y",
                new BigDecimal("12345.67"), new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                "2022-05-01", "2027-05-01", "2026-01-15",
                new BigDecimal("500.00"), new BigDecimal("250.50"),
                "30301     ", "GRPTEST   ");

        AccountRecord acct = AccountRecord.parse(line);
        assertEquals(99887766554L, acct.acctId());
        assertEquals("Y", acct.activeStatus());
        assertEquals(0, new BigDecimal("12345.67").compareTo(acct.currentBalance()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(acct.creditLimit()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(acct.cashCreditLimit()));
        assertEquals("2022-05-01", acct.openDate());
        assertEquals("2027-05-01", acct.expirationDate());
        assertEquals("2026-01-15", acct.reissueDate());
        assertEquals(0, new BigDecimal("500.00").compareTo(acct.currentCycleCredit()));
        assertEquals(0, new BigDecimal("250.50").compareTo(acct.currentCycleDebit()));
        assertEquals("GRPTEST   ", acct.groupId());
    }

    // ------------------------------------------------------------------
    // Output format verification
    // ------------------------------------------------------------------

    @Test
    void outAccountRecord_toOutputLine_format() {
        OutAccountRecord rec = new OutAccountRecord(
                123L, "Y",
                new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2023-01-01", "2028-01-01", "20250620",
                new BigDecimal("100.00"), new BigDecimal("50.00"),
                "GRP1      ");

        String output = rec.toOutputLine();
        assertTrue(output.startsWith("00000000123"));
        assertTrue(output.contains("Y"));
    }

    @Test
    void arrayAccountRecord_toOutputLine_hasCorrectLength() {
        BigDecimal[] balances = new BigDecimal[5];
        BigDecimal[] debits = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            balances[i] = BigDecimal.ZERO.setScale(2);
            debits[i] = BigDecimal.ZERO.setScale(2);
        }

        ArrayAccountRecord rec = new ArrayAccountRecord(1L, balances, debits);
        String output = rec.toOutputLine();
        // 11 (id) + 5 * (13 + 13) (bal+debit) + 4 (filler) = 11 + 130 + 4 = 145
        assertEquals(145, output.length());
    }
}
