package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;
import com.carddemo.batch.cbact01c.util.CobolDecimalParser;
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
 * Integration tests for CBACT01C Java migration.
 * Verifies that the Java version produces output logically identical
 * to what the COBOL program would produce for the same input data.
 *
 * COBOL PIC S9(10)V99 stores 12 characters with 2 implied decimal places.
 * Example: "00000001940{" encodes +194.00 (not 19.40).
 */
class Cbact01cApplicationTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arrayFile;
    private Path vbrFile;

    @BeforeEach
    void setUp() {
        inputFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("outfile.txt");
        arrayFile = tempDir.resolve("arryfile.txt");
        vbrFile = tempDir.resolve("vbrcfile.txt");
    }

    private String buildCobolRecord(long acctId, String status,
                                     BigDecimal bal, BigDecimal creditLimit,
                                     BigDecimal cashLimit, String openDate,
                                     String expDate, String reissueDate,
                                     BigDecimal cycCredit, BigDecimal cycDebit,
                                     String zip, String groupId) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        sb.append(padRight(status, 1));
        sb.append(CobolDecimalParser.format(bal, 12, 2));
        sb.append(CobolDecimalParser.format(creditLimit, 12, 2));
        sb.append(CobolDecimalParser.format(cashLimit, 12, 2));
        sb.append(padRight(openDate, 10));
        sb.append(padRight(expDate, 10));
        sb.append(padRight(reissueDate, 10));
        sb.append(CobolDecimalParser.format(cycCredit, 12, 2));
        sb.append(CobolDecimalParser.format(cycDebit, 12, 2));
        sb.append(padRight(zip, 10));
        sb.append(padRight(groupId, 10));
        sb.append(" ".repeat(178));
        return sb.toString();
    }

    @Test
    void processesOneRecordWithNonZeroDebit() throws IOException {
        String record = buildCobolRecord(
                1L, "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("500.00"), new BigDecimal("75.25"),
                "A000000000", "A000000000"
        );
        Files.writeString(inputFile, record + "\n");

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        int count = app.execute();

        assertEquals(1, count);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(1, outLines.size());
        String outLine = outLines.get(0);

        assertEquals("00000000001", outLine.substring(0, 11));
        assertEquals("Y", outLine.substring(11, 12));
        assertEquals("00000001940{", outLine.substring(12, 24));
        assertEquals("00000020200{", outLine.substring(24, 36));
        assertEquals("00000010200{", outLine.substring(36, 48));
        assertEquals("2014-11-20", outLine.substring(48, 58));
        assertEquals("2025-05-20", outLine.substring(58, 68));
        assertEquals("20250520  ", outLine.substring(68, 78));
        assertEquals("00000005000{", outLine.substring(78, 90));
        assertEquals("00000000752E", outLine.substring(90, 102));
        assertEquals("A000000000", outLine.substring(102, 112));

        List<String> arrLines = Files.readAllLines(arrayFile);
        assertEquals(1, arrLines.size());
        String arrLine = arrLines.get(0);
        assertEquals("00000000001", arrLine.substring(0, 11));
        assertEquals("00000001940{", arrLine.substring(11, 23));
        assertEquals("00000010050{", arrLine.substring(23, 35));
        assertEquals("00000001940{", arrLine.substring(35, 47));
        assertEquals("00000015250{", arrLine.substring(47, 59));
        assertEquals("00000010250}", arrLine.substring(59, 71));
        assertEquals("00000025000}", arrLine.substring(71, 83));

        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals(2, vbrLines.size());
        assertEquals("00000000001Y", vbrLines.get(0));
        String vb2 = vbrLines.get(1);
        assertEquals("00000000001", vb2.substring(0, 11));
        assertEquals("00000001940{", vb2.substring(11, 23));
        assertEquals("00000020200{", vb2.substring(23, 35));
        assertEquals("2025", vb2.substring(35, 39));
    }

    @Test
    void substitutes2525WhenDebitIsZero() throws IOException {
        String record = buildCobolRecord(
                2L, "Y",
                new BigDecimal("158.00"), new BigDecimal("6130.00"),
                new BigDecimal("5448.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "B000000000", "A000000000"
        );
        Files.writeString(inputFile, record + "\n");

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        app.execute();

        List<String> outLines = Files.readAllLines(outFile);
        String outLine = outLines.get(0);

        assertEquals("00000025250{", outLine.substring(90, 102));
        assertEquals("20240811  ", outLine.substring(68, 78));
    }

    @Test
    void processesMultipleRecords() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(buildCobolRecord(
                    i, "Y",
                    new BigDecimal(i * 100 + ".00"), new BigDecimal("5000.00"),
                    new BigDecimal("2000.00"),
                    "2020-01-01", "2025-12-31", "2025-06-15",
                    new BigDecimal("100.00"), new BigDecimal(i == 3 ? "0.00" : "500.00"),
                    "Z" + String.format("%09d", i), "G" + String.format("%09d", i)
            )).append("\n");
        }
        Files.writeString(inputFile, sb.toString());

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        int count = app.execute();

        assertEquals(5, count);
        assertEquals(5, Files.readAllLines(outFile).size());
        assertEquals(5, Files.readAllLines(arrayFile).size());
        assertEquals(10, Files.readAllLines(vbrFile).size());

        String outLine3 = Files.readAllLines(outFile).get(2);
        assertEquals("00000025250{", outLine3.substring(90, 102));

        String outLine1 = Files.readAllLines(outFile).get(0);
        assertEquals("00000005000{", outLine1.substring(90, 102));
    }

    @Test
    void handlesEmptyFile() throws IOException {
        Files.writeString(inputFile, "");

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        int count = app.execute();

        assertEquals(0, count);
        assertEquals(0, Files.readAllLines(outFile).size());
        assertEquals(0, Files.readAllLines(arrayFile).size());
        assertEquals(0, Files.readAllLines(vbrFile).size());
    }

    @Test
    void buildOutRecordPreservesFields() {
        var acct = new AccountRecord(
                42L, "N",
                new BigDecimal("1005.00"), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                "2020-01-15", "2025-06-30", "2024-12-25",
                new BigDecimal("2000.00"), new BigDecimal("1507.50"),
                "12345     ", "GRP001    "
        );

        OutAccountRecord out = Cbact01cApplication.buildOutRecord(acct);

        assertEquals(42L, out.acctId());
        assertEquals("N", out.activeStatus());
        assertEquals(new BigDecimal("1005.00"), out.currBal());
        assertEquals("20241225  ", out.reissueDate());
        assertEquals(new BigDecimal("1507.50"), out.currCycDebit());
    }

    @Test
    void buildOutRecordSubstitutesZeroDebit() {
        var acct = new AccountRecord(
                7L, "Y",
                new BigDecimal("500.00"), new BigDecimal("5000.00"),
                new BigDecimal("2500.00"),
                "2019-03-10", "2025-03-10", "2025-01-01",
                new BigDecimal("100.00"), BigDecimal.ZERO,
                "00000     ", "GRP002    "
        );

        OutAccountRecord out = Cbact01cApplication.buildOutRecord(acct);
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
    }

    @Test
    void buildArrayRecordPopulatesCorrectSlots() {
        var acct = new AccountRecord(
                99L, "Y",
                new BigDecimal("3450.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2020-01-01", "2025-12-31", "2025-06-15",
                new BigDecimal("0.00"), new BigDecimal("0.00"),
                "ZIP       ", "GRP       "
        );

        ArrayRecord arr = Cbact01cApplication.buildArrayRecord(acct);

        assertEquals(99L, arr.acctId());
        assertEquals(new BigDecimal("3450.00"), arr.balanceEntries()[0].currBal());
        assertEquals(new BigDecimal("1005.00"), arr.balanceEntries()[0].currCycDebit());
        assertEquals(new BigDecimal("3450.00"), arr.balanceEntries()[1].currBal());
        assertEquals(new BigDecimal("1525.00"), arr.balanceEntries()[1].currCycDebit());
        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries()[2].currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.balanceEntries()[2].currCycDebit());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries()[3].currBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries()[4].currCycDebit());
    }

    @Test
    void buildVbrRecord1() {
        var acct = new AccountRecord(
                10L, "Y",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "          ", "          "
        );

        VbrRecord1 vb1 = Cbact01cApplication.buildVbrRecord1(acct);
        assertEquals(10L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void buildVbrRecord2ExtractsYear() {
        var acct = new AccountRecord(
                20L, "N",
                new BigDecimal("9999.90"), new BigDecimal("50000.00"),
                new BigDecimal("25000.00"),
                "2018-07-04", "2028-07-04", "2023-11-30",
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                "54321     ", "GRP999    "
        );

        VbrRecord2 vb2 = Cbact01cApplication.buildVbrRecord2(acct);
        assertEquals(20L, vb2.acctId());
        assertEquals(new BigDecimal("9999.90"), vb2.currBal());
        assertEquals(new BigDecimal("50000.00"), vb2.creditLimit());
        assertEquals("2023", vb2.reissueYear());
    }

    @Test
    void processesRealSampleRecord() throws IOException {
        String realRecord = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "A000000000" + "A000000000"
                + " ".repeat(178);
        Files.writeString(inputFile, realRecord + "\n");

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        int count = app.execute();
        assertEquals(1, count);

        String outLine = Files.readAllLines(outFile).get(0);
        assertEquals("00000000001", outLine.substring(0, 11));
        assertEquals("Y", outLine.substring(11, 12));
        assertEquals("00000001940{", outLine.substring(12, 24));
        assertEquals("00000020200{", outLine.substring(24, 36));
        assertEquals("00000010200{", outLine.substring(36, 48));
        assertEquals("2014-11-20", outLine.substring(48, 58));
        assertEquals("2025-05-20", outLine.substring(58, 68));
        assertEquals("20250520  ", outLine.substring(68, 78));
        assertEquals("00000000000{", outLine.substring(78, 90));
        assertEquals("00000025250{", outLine.substring(90, 102));
        assertEquals("A000000000", outLine.substring(102, 112));

        List<String> vbrLines = Files.readAllLines(vbrFile);
        assertEquals("00000000001Y", vbrLines.get(0));
        String vb2 = vbrLines.get(1);
        assertEquals("00000000001", vb2.substring(0, 11));
        assertEquals("00000001940{", vb2.substring(11, 23));
        assertEquals("00000020200{", vb2.substring(23, 35));
        assertEquals("2025", vb2.substring(35, 39));
    }

    @Test
    void processesFullSampleDataFile() throws IOException {
        Path realDataFile = Path.of("../../app/data/ASCII/acctdata.txt");
        if (!Files.exists(realDataFile)) {
            realDataFile = Path.of(System.getProperty("user.dir"))
                    .resolve("../../app/data/ASCII/acctdata.txt")
                    .normalize();
        }
        if (!Files.exists(realDataFile)) {
            System.out.println("SKIP: Real sample data file not found at " + realDataFile);
            return;
        }

        Files.copy(realDataFile, inputFile);

        var app = new Cbact01cApplication(inputFile, outFile, arrayFile, vbrFile);
        int count = app.execute();

        assertEquals(50, count, "Should process all 50 sample account records");

        List<String> outLines = Files.readAllLines(outFile);
        List<String> arrLines = Files.readAllLines(arrayFile);
        List<String> vbrLines = Files.readAllLines(vbrFile);

        assertEquals(50, outLines.size(), "OUT-FILE should have 50 lines");
        assertEquals(50, arrLines.size(), "ARRY-FILE should have 50 lines");
        assertEquals(100, vbrLines.size(), "VBRC-FILE should have 100 lines (2 per account)");

        for (int i = 0; i < outLines.size(); i++) {
            String line = outLines.get(i);
            assertTrue(line.length() >= 112,
                    "OUT-FILE line " + (i + 1) + " too short: " + line.length());
        }

        for (int i = 0; i < vbrLines.size(); i += 2) {
            assertEquals(12, vbrLines.get(i).length(),
                    "VB1 record at line " + (i + 1) + " should be 12 chars");
            assertEquals(39, vbrLines.get(i + 1).length(),
                    "VB2 record at line " + (i + 2) + " should be 39 chars");
        }
    }

    private static String padRight(String s, int length) {
        if (s == null) return " ".repeat(length);
        if (s.length() >= length) return s.substring(0, length);
        return s + " ".repeat(length - s.length());
    }
}
