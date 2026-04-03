package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    private Path inputFile;
    private Path outFile;
    private Path arryFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() {
        inputFile = tempDir.resolve("acctdata.txt");
        outFile = tempDir.resolve("acctout.txt");
        arryFile = tempDir.resolve("arryout.txt");
        vbrcFile = tempDir.resolve("vbrcout.txt");
    }

    @Test
    void executeProcessesAccountRecordsAndWritesOutputFiles() throws IOException {
        // COBOL-equivalent fixed-width record: acctId(11) + status(1) + currBal(12) + creditLimit(12)
        // + cashCreditLimit(12) + openDate(10) + expDate(10) + reissueDate(10)
        // + cycCredit(12) + cycDebit(12) + zip(10) + groupId(10)
        String line = "00000000001Y" +
                "000150050.00" +  // currBal = 150050.00
                "000500000.00" +  // creditLimit
                "000100000.00" +  // cashCreditLimit
                "2020-01-15" +    // openDate
                "2025-01-15" +    // expDate
                "2023-06-01" +    // reissueDate
                "000020000.00" +  // cycCredit
                "000030000.00" +  // cycDebit
                "98101     " +    // zip
                "GRP001    ";     // groupId

        Files.writeString(inputFile, line + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);
        List<AccountRecord> records = processor.execute();

        assertEquals(1, records.size());
        AccountRecord rec = records.get(0);
        assertEquals(1L, rec.getAcctId());
        assertEquals("Y", rec.getAcctActiveStatus());

        assertTrue(Files.exists(outFile), "Output file should be created");
        assertTrue(Files.exists(arryFile), "Array file should be created");
        assertTrue(Files.exists(vbrcFile), "VBRC file should be created");

        List<String> outLines = Files.readAllLines(outFile);
        assertFalse(outLines.isEmpty(), "Output file should have records");

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(2, vbrcLines.size(), "VBRC file should have 2 records per account (VB1 + VB2)");
    }

    @Test
    void executeHandlesEmptyFile() throws IOException {
        Files.writeString(inputFile, "");
        AccountFileProcessor processor = new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);
        List<AccountRecord> records = processor.execute();
        assertTrue(records.isEmpty());
    }

    @Test
    void parseAccountRecordHandlesShortLine() {
        AccountRecord rec = AccountFileProcessor.parseAccountRecord("00000000001Y");
        assertEquals(1L, rec.getAcctId());
        assertEquals("Y", rec.getAcctActiveStatus());
        assertEquals(BigDecimal.ZERO, rec.getAcctCurrBal());
    }

    @Test
    void executeProcessesMultipleRecords() throws IOException {
        String line1 = "00000000001Y" + "000150050.00" + "000500000.00" + "000100000.00" +
                "2020-01-15" + "2025-01-15" + "2023-06-01" +
                "000020000.00" + "000030000.00" + "98101     " + "GRP001    ";
        String line2 = "00000000002N" + "000250000.00" + "000800000.00" + "000200000.00" +
                "2019-05-20" + "2024-05-20" + "2022-11-15" +
                "000010000.00" + "000000000.00" + "10001     " + "GRP002    ";

        Files.writeString(inputFile, line1 + "\n" + line2 + "\n");

        AccountFileProcessor processor = new AccountFileProcessor(inputFile, outFile, arryFile, vbrcFile);
        List<AccountRecord> records = processor.execute();

        assertEquals(2, records.size());
        assertEquals(1L, records.get(0).getAcctId());
        assertEquals(2L, records.get(1).getAcctId());

        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertEquals(4, vbrcLines.size(), "VBRC should have 2 records per account");
    }
}
