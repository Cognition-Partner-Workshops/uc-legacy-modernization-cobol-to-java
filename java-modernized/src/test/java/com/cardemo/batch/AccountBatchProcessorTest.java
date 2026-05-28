package com.cardemo.batch;

import com.cardemo.batch.model.AccountRecord;
import com.cardemo.batch.model.ArrayRecord;
import com.cardemo.batch.model.OutputAccountRecord;
import com.cardemo.batch.model.VbRecord1;
import com.cardemo.batch.model.VbRecord2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountBatchProcessorTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arrayFile;
    private Path vbFile;
    private AccountBatchProcessor processor;

    private static String buildRecord(String acctId, String status,
                                      String currBal, String creditLimit, String cashCreditLimit,
                                      String openDate, String expDate, String reissueDate,
                                      String cycCredit, String cycDebit,
                                      String addrZip, String groupId) {
        return acctId + status + currBal + creditLimit + cashCreditLimit
                + openDate + expDate + reissueDate
                + cycCredit + cycDebit
                + String.format("%-10s", addrZip)
                + String.format("%-10s", groupId)
                + " ".repeat(178);
    }

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("acctfile.txt");
        outFile = tempDir.resolve("outfile.csv");
        arrayFile = tempDir.resolve("arryfile.csv");
        vbFile = tempDir.resolve("vbrcfile.csv");

        String record1 = buildRecord(
                "00000000001", "Y",
                "00000001940{", "00000020200{", "00000010200{",
                "2014-11-20", "2025-05-20", "2025-05-20",
                "00000000000{", "00000000000{",
                "A000000000", "GRP000TEST");

        String record2 = buildRecord(
                "00000000002", "Y",
                "00000001580{", "00000061300{", "00000054480{",
                "2013-06-19", "2024-08-11", "2024-08-11",
                "00000000000{", "00000000500{",
                "A000000000", "GRP000TEST");

        Files.writeString(inputFile, record1 + "\n" + record2 + "\n");

        processor = new AccountBatchProcessor(inputFile, outFile, arrayFile, vbFile);
    }

    @Test
    void processesCorrectNumberOfRecords() throws IOException {
        int count = processor.process();
        assertEquals(2, count);
    }

    @Test
    void outputFileContainsHeaderAndTwoDataRows() throws IOException {
        processor.process();

        List<String> lines = Files.readAllLines(outFile);
        assertEquals(3, lines.size());
        assertEquals(OutputAccountRecord.csvHeader(), lines.get(0));
    }

    @Test
    void outputRecordFieldsMatchCobolLogic() throws IOException {
        processor.process();
        List<String> lines = Files.readAllLines(outFile);

        String[] fields = lines.get(1).split(",", -1);
        assertEquals(11, fields.length);
        assertEquals("1", fields[0]);                   // ACCT-ID
        assertEquals("Y", fields[1]);                   // ACTIVE-STATUS
        assertEquals("194.00", fields[2]);              // CURR-BAL
        assertEquals("2020.00", fields[3]);             // CREDIT-LIMIT
        assertEquals("1020.00", fields[4]);             // CASH-CREDIT-LIMIT
        assertEquals("2014-11-20", fields[5]);          // OPEN-DATE
        assertEquals("2025-05-20", fields[6]);          // EXPIRATION-DATE
        assertEquals("20250520", fields[7]);            // REISSUE-DATE (converted)
        assertEquals("0.00", fields[8]);                // CURR-CYC-CREDIT
        assertEquals("2525.00", fields[9]);             // CURR-CYC-DEBIT (default)
        assertEquals("GRP000TEST", fields[10]);         // GROUP-ID
    }

    @Test
    void nonZeroDebitIsPreservedInSecondRecord() throws IOException {
        processor.process();
        List<String> lines = Files.readAllLines(outFile);

        String[] fields = lines.get(2).split(",", -1);
        assertEquals("50.00", fields[9]); // non-zero debit preserved
    }

    @Test
    void defaultDebitAppliedWhenZero() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "GRP001"
        );
        OutputAccountRecord out = processor.populateOutputRecord(acct);
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
    }

    @Test
    void nonZeroDebitPreserved() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, new BigDecimal("500.00"), "", "GRP001"
        );
        OutputAccountRecord out = processor.populateOutputRecord(acct);
        assertEquals(new BigDecimal("500.00"), out.currCycDebit());
    }

    @Test
    void reissueDateConvertedToCompact() {
        AccountRecord acct = new AccountRecord(
                1L, "Y", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "GRP001"
        );
        OutputAccountRecord out = processor.populateOutputRecord(acct);
        assertEquals("20250615", out.reissueDate());
    }

    @Test
    void arrayRecordPopulatedCorrectly() {
        BigDecimal balance = new BigDecimal("194.00");
        AccountRecord acct = new AccountRecord(
                1L, "Y", balance, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "GRP001"
        );

        ArrayRecord arr = processor.populateArrayRecord(acct);

        assertEquals(1L, arr.acctId());
        assertEquals(5, arr.balanceEntries().size());

        assertEquals(balance, arr.balanceEntries().get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.balanceEntries().get(0).currCycDebit());

        assertEquals(balance, arr.balanceEntries().get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.balanceEntries().get(1).currCycDebit());

        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries().get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.balanceEntries().get(2).currCycDebit());

        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).currBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).currCycDebit());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).currBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).currCycDebit());
    }

    @Test
    void arrayFileContainsHeaderAndTwoDataRows() throws IOException {
        processor.process();
        List<String> lines = Files.readAllLines(arrayFile);
        assertEquals(3, lines.size());
        assertEquals(ArrayRecord.csvHeader(), lines.get(0));
    }

    @Test
    void vbRecord1PopulatedCorrectly() {
        AccountRecord acct = new AccountRecord(
                42L, "N", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "GRP001"
        );
        VbRecord1 vb1 = processor.populateVbRecord1(acct);
        assertEquals(42L, vb1.acctId());
        assertEquals("N", vb1.activeStatus());
    }

    @Test
    void vbRecord2ExtractsReissueYear() {
        AccountRecord acct = new AccountRecord(
                42L, "Y", new BigDecimal("500.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2025-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "", "GRP001"
        );
        VbRecord2 vb2 = processor.populateVbRecord2(acct);
        assertEquals(42L, vb2.acctId());
        assertEquals(new BigDecimal("500.00"), vb2.currBal());
        assertEquals(new BigDecimal("10000.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void vbFileContainsHeaderAndFourDataRows() throws IOException {
        processor.process();
        List<String> lines = Files.readAllLines(vbFile);
        // header + 2 accounts × 2 VB records each = 5 lines
        assertEquals(5, lines.size());

        long vb1Count = lines.stream().skip(1).filter(l -> l.startsWith("VB1")).count();
        long vb2Count = lines.stream().skip(1).filter(l -> l.startsWith("VB2")).count();
        assertEquals(2, vb1Count);
        assertEquals(2, vb2Count);
    }

    @Test
    void emptyInputProducesZeroRecords() throws IOException {
        Files.writeString(inputFile, "");
        int count = processor.process();
        assertEquals(0, count);
    }

    @Test
    void processWithRealSampleData() throws IOException {
        Path samplePath = Path.of("src/test/resources/sample_acctdata.txt");
        if (!Files.exists(samplePath)) {
            return;
        }

        AccountBatchProcessor sampleProcessor = new AccountBatchProcessor(
                samplePath, outFile, arrayFile, vbFile
        );
        int count = sampleProcessor.process();
        assertEquals(3, count);

        List<String> outLines = Files.readAllLines(outFile);
        assertEquals(4, outLines.size());

        String[] firstRecord = outLines.get(1).split(",", -1);
        assertEquals("1", firstRecord[0]);
        assertEquals("194.00", firstRecord[2]);
        assertEquals("20250520", firstRecord[7]);
        assertEquals("2525.00", firstRecord[9]);
    }

    @Test
    void vbRecord2CsvContainsAllFields() {
        VbRecord2 vb2 = new VbRecord2(1L, new BigDecimal("194.00"), new BigDecimal("2020.00"), "2025");
        assertEquals("1,194.00,2020.00,2025", vb2.toCsv());
    }

    @Test
    void outputRecordCsvContainsAllFields() {
        OutputAccountRecord out = new OutputAccountRecord(
                1L, "Y", new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"), "2014-11-20", "2025-05-20", "20250520",
                new BigDecimal("0.00"), new BigDecimal("2525.00"), "A000000000"
        );
        String csv = out.toCsv();
        String[] parts = csv.split(",", -1);
        assertEquals(11, parts.length);
    }
}
