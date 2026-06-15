package com.carddemo.batch;

import com.carddemo.batch.io.CobolDecimalParser;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountFileProcessorTest {

    private Path inputFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("acctdata.txt");
        try (var in = Objects.requireNonNull(
                getClass().getResourceAsStream("/acctdata.txt"),
                "Test fixture acctdata.txt not found on classpath")) {
            Files.copy(in, inputFile);
        }
    }

    @Test
    void readsAllRecords() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();
        assertEquals(50, accounts.size(), "should read all 50 account records");
    }

    @Test
    void firstRecordFieldsParsedCorrectly() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();
        AccountRecord first = accounts.getFirst();

        assertEquals("00000000001", first.acctId());
        assertEquals("Y", first.activeStatus());
        assertEquals(0, new BigDecimal("194.00").compareTo(first.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(first.creditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(first.cashCreditLimit()));
        assertEquals("2014-11-20", first.openDate());
        assertEquals("2025-05-20", first.expirationDate());
        assertEquals("2025-05-20", first.reissueDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(first.currCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(first.currCycDebit()));
    }

    @Test
    void outputFileHasOneLinePerRecord() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> outLines = Files.readAllLines(tempDir.resolve("outfile.txt"));
        assertEquals(50, outLines.size());
    }

    @Test
    void outputRecordReissueDateConvertedToCompact() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(accounts.getFirst());
        assertTrue(out.reissueDate().startsWith("20250520"),
                "YYYY-MM-DD should be converted to YYYYMMDD");
    }

    @Test
    void outputRecordZeroCycleDebitReplacedWithDefault() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        AccountRecord acctWithZeroDebit = accounts.stream()
                .filter(a -> a.currCycDebit().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected at least one account with zero debit"));

        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acctWithZeroDebit);
        assertEquals(0, new BigDecimal("2525.00").compareTo(out.currCycDebit()),
                "zero cycle debit should be replaced with 2525.00");
    }

    @Test
    void arrayRecordHasFiveEntries() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        ArrayAccountRecord arr = AccountFileProcessor.buildArrayRecord(accounts.getFirst());
        assertEquals(5, arr.balanceEntries().size());
    }

    @Test
    void arrayRecordEntryValues() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();
        AccountRecord first = accounts.getFirst();
        ArrayAccountRecord arr = AccountFileProcessor.buildArrayRecord(first);

        var e1 = arr.balanceEntries().get(0);
        assertEquals(0, first.currBal().compareTo(e1.currBal()));
        assertEquals(0, new BigDecimal("1005.00").compareTo(e1.currCycDebit()));

        var e2 = arr.balanceEntries().get(1);
        assertEquals(0, first.currBal().compareTo(e2.currBal()));
        assertEquals(0, new BigDecimal("1525.00").compareTo(e2.currCycDebit()));

        var e3 = arr.balanceEntries().get(2);
        assertEquals(0, new BigDecimal("-1025.00").compareTo(e3.currBal()));
        assertEquals(0, new BigDecimal("-2500.00").compareTo(e3.currCycDebit()));

        var e4 = arr.balanceEntries().get(3);
        assertEquals(0, BigDecimal.ZERO.compareTo(e4.currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(e4.currCycDebit()));

        var e5 = arr.balanceEntries().get(4);
        assertEquals(0, BigDecimal.ZERO.compareTo(e5.currBal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(e5.currCycDebit()));
    }

    @Test
    void arrayFileHasOneLinePerRecord() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> arrLines = Files.readAllLines(tempDir.resolve("arryfile.txt"));
        assertEquals(50, arrLines.size());
    }

    @Test
    void vbrcFileHasTwoLinesPerRecord() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> vbrLines = Files.readAllLines(tempDir.resolve("vbrcfile.txt"));
        assertEquals(100, vbrLines.size(), "2 VB records per account × 50 accounts");
    }

    @Test
    void vbRecord1Fields() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        VbRecord1 vb1 = AccountFileProcessor.buildVbRecord1(accounts.getFirst());
        assertEquals("00000000001", vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void vbRecord2Fields() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        VbRecord2 vb2 = AccountFileProcessor.buildVbRecord2(accounts.getFirst());
        assertEquals("00000000001", vb2.acctId());
        assertEquals(0, new BigDecimal("194.00").compareTo(vb2.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(vb2.creditLimit()));
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void vbRecord1OutputLength() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> lines = Files.readAllLines(tempDir.resolve("vbrcfile.txt"));
        String vb1Line = lines.getFirst();
        assertEquals(VbRecord1.LENGTH, vb1Line.length(),
                "VB1 line should be 12 chars (acctId=11 + status=1)");
    }

    @Test
    void vbRecord2OutputLength() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> lines = Files.readAllLines(tempDir.resolve("vbrcfile.txt"));
        String vb2Line = lines.get(1);
        assertEquals(VbRecord2.LENGTH, vb2Line.length(),
                "VB2 line should be 39 chars (acctId=11 + bal=12 + limit=12 + year=4)");
    }

    @Test
    void outputRecordDecodable() throws IOException {
        var processor = newProcessor();
        processor.process();

        List<String> outLines = Files.readAllLines(tempDir.resolve("outfile.txt"));
        String line = outLines.getFirst();

        int pos = 0;
        String acctId = line.substring(pos, pos += 11);
        assertEquals("00000000001", acctId);

        String status = line.substring(pos, pos += 1);
        assertEquals("Y", status);

        BigDecimal bal = CobolDecimalParser.parseZonedDecimal(line.substring(pos, pos += 12), 2);
        assertEquals(0, new BigDecimal("194.00").compareTo(bal));
    }

    @Test
    void allRecordIdsUnique() throws IOException {
        var processor = newProcessor();
        List<AccountRecord> accounts = processor.process();

        long distinctIds = accounts.stream().map(AccountRecord::acctId).distinct().count();
        assertEquals(accounts.size(), distinctIds, "all account IDs should be unique");
    }

    private AccountFileProcessor newProcessor() {
        return new AccountFileProcessor(
                inputFile,
                tempDir.resolve("outfile.txt"),
                tempDir.resolve("arryfile.txt"),
                tempDir.resolve("vbrcfile.txt")
        );
    }

    private static class AssertionError extends RuntimeException {
        AssertionError(String msg) {
            super(msg);
        }
    }
}
