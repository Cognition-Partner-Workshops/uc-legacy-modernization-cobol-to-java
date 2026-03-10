package com.carddemo.batch;

import com.carddemo.common.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Cbact01cJobTest {

    @TempDir
    Path tempDir;

    private AccountRecord sampleAccount(BigDecimal cycDebit) {
        return new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2020-01-01", "2025-12-31", "2023-06-15",
                new BigDecimal("500.00"), cycDebit,
                "10001", "GROUP1"
        );
    }

    @Test
    void buildOutRecord_defaultsDebitToZero() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        String outRecord = job.buildOutRecord(acct);
        // When debit is zero, it should be replaced with 2525.00
        assertThat(outRecord).contains("+00000252500");
    }

    @Test
    void buildOutRecord_preservesNonZeroDebit() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(new BigDecimal("100.00"));
        String outRecord = job.buildOutRecord(acct);
        assertThat(outRecord).contains("+00000010000");
    }

    @Test
    void buildOutRecord_convertsReissueDateToCompact() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(new BigDecimal("100.00"));
        String outRecord = job.buildOutRecord(acct);
        // 2023-06-15 → 20230615
        assertThat(outRecord).contains("20230615");
    }

    @Test
    void buildArrayRecord_populatesIndices1Through3() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        String arryRecord = job.buildArrayRecord(acct);

        // Account ID at start
        assertThat(arryRecord).startsWith("00000000001");

        // Index 1: balance = 1000.00, debit = 1005.00
        assertThat(arryRecord).contains("+00000100500"); // 1005.00

        // Index 2: debit = 1525.00
        assertThat(arryRecord).contains("+00000152500"); // 1525.00

        // Index 3: balance = -1025.00, debit = -2500.00
        assertThat(arryRecord).contains("-00000102500"); // -1025.00
        assertThat(arryRecord).contains("-00000250000"); // -2500.00
    }

    @Test
    void buildVbrcRecord1_has12Chars() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        String vbr1 = job.buildVbrcRecord1(acct);
        assertThat(vbr1.length()).isEqualTo(12);
        assertThat(vbr1).startsWith("00000000001");
        assertThat(vbr1).endsWith("Y");
    }

    @Test
    void buildVbrcRecord2_has39Chars() {
        Cbact01cJob job = new Cbact01cJob(null, null, null, null);
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        String vbr2 = job.buildVbrcRecord2(acct);
        assertThat(vbr2.length()).isEqualTo(39);
        assertThat(vbr2).startsWith("00000000001");
        // Ends with reissue year "2023"
        assertThat(vbr2).endsWith("2023");
    }

    @Test
    void endToEnd_readsInputWritesThreeOutputFiles() throws Exception {
        // Create a sample input file with one account record (300 chars)
        AccountRecord acct = sampleAccount(new BigDecimal("200.00"));
        String inputLine = com.carddemo.common.io.AccountRecordIO.FORMATTER.format(acct);
        Path inputFile = tempDir.resolve("acctfile.dat");
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");
        Files.writeString(inputFile, inputLine + "\n");

        Cbact01cJob job = new Cbact01cJob(inputFile, outFile, arryFile, vbrcFile);
        job.execute();

        // Verify output files were created and have content
        assertThat(outFile).exists();
        assertThat(arryFile).exists();
        assertThat(vbrcFile).exists();

        List<String> outLines = Files.readAllLines(outFile);
        assertThat(outLines).hasSize(1);

        List<String> arryLines = Files.readAllLines(arryFile);
        assertThat(arryLines).hasSize(1);

        // VBRC file should have 2 lines per account (one 12-byte, one 39-byte)
        List<String> vbrcLines = Files.readAllLines(vbrcFile);
        assertThat(vbrcLines).hasSize(2);
        assertThat(vbrcLines.get(0).length()).isEqualTo(12);
        assertThat(vbrcLines.get(1).length()).isEqualTo(39);
    }
}
