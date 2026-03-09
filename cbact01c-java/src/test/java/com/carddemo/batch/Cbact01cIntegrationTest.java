package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.model.*;
import com.carddemo.batch.service.AccountProcessor;
import com.carddemo.batch.util.CobolDecimalParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test that runs the full CBACT01C batch process
 * against the sample input file and verifies all three output files
 * contain the expected data — identical to what the COBOL program produces.
 */
class Cbact01cIntegrationTest {

    private static final Path SAMPLE_INPUT = Path.of("src/test/resources/acctdata-sample.txt");

    @TempDir
    Path tempDir;

    @Test
    void fullBatchProducesExpectedOutputFiles() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        Cbact01cApplication app = new Cbact01cApplication();
        app.run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        // All three output files should exist
        assertTrue(Files.exists(outFile), "OUTFILE should exist");
        assertTrue(Files.exists(arryFile), "ARRYFILE should exist");
        assertTrue(Files.exists(vbrcFile), "VBRCFILE should exist");
    }

    @Test
    void outFileHasThreeRecords() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        assertEquals(3, lines.size(), "OUTFILE should have 3 records");
    }

    @Test
    void outFileFirstRecordMatchesCobolOutput() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(outFile);
        String firstLine = lines.get(0);

        // Parse the output line to verify field-by-field
        int pos = 0;
        // ACCT-ID: PIC 9(11)
        assertEquals("00000000001", firstLine.substring(pos, pos + 11));
        pos += 11;
        // ACTIVE-STATUS: PIC X(01)
        assertEquals("Y", firstLine.substring(pos, pos + 1));
        pos += 1;
        // CURR-BAL: PIC S9(10)V99 -> 194.00 (raw: "00000001940{")
        assertEquals("00000001940{", firstLine.substring(pos, pos + 12));
        pos += 12;
        // CREDIT-LIMIT: PIC S9(10)V99 -> 2020.00 (raw: "00000020200{")
        assertEquals("00000020200{", firstLine.substring(pos, pos + 12));
        pos += 12;
        // CASH-CREDIT-LIMIT: PIC S9(10)V99 -> 1020.00 (raw: "00000010200{")
        assertEquals("00000010200{", firstLine.substring(pos, pos + 12));
        pos += 12;
        // OPEN-DATE: PIC X(10)
        assertEquals("2014-11-20", firstLine.substring(pos, pos + 10));
        pos += 10;
        // EXPIRAION-DATE: PIC X(10)
        assertEquals("2025-05-20", firstLine.substring(pos, pos + 10));
        pos += 10;
        // REISSUE-DATE: converted to YYYYMMDD, PIC X(10) -> "20250520  " (padded)
        String reissueDate = firstLine.substring(pos, pos + 10);
        assertTrue(reissueDate.startsWith("20250520"),
                "Reissue date should be YYYYMMDD: " + reissueDate);
        pos += 10;
        // CURR-CYC-CREDIT: PIC S9(10)V99 -> 0.00
        assertEquals("00000000000{", firstLine.substring(pos, pos + 12));
        pos += 12;
        // CURR-CYC-DEBIT: defaulted to 2525.00 (original was zero)
        // 2525.00 -> 252500 -> "000000252500" -> overpunch -> "00000025250{"
        assertEquals("00000025250{", firstLine.substring(pos, pos + 12));
        pos += 12;
        // GROUP-ID: PIC X(10) — blank in sample data (addrZip holds "A000000000")
        assertEquals("          ", firstLine.substring(pos, pos + 10));
    }

    @Test
    void arryFileHasThreeRecords() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(arryFile);
        assertEquals(3, lines.size(), "ARRYFILE should have 3 records");
    }

    @Test
    void arryFileFirstRecordStructure() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(arryFile);
        String first = lines.get(0);

        // ACCT-ID
        assertEquals("00000000001", first.substring(0, 11));

        int pos = 11;
        // Entry 1: currBal=194.00, currCycDebit=1005.00
        BigDecimal bal1 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("194.00"), bal1);
        pos += 12;
        BigDecimal deb1 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("1005.00"), deb1);
        pos += 12;

        // Entry 2: currBal=194.00, currCycDebit=1525.00
        BigDecimal bal2 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("194.00"), bal2);
        pos += 12;
        BigDecimal deb2 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("1525.00"), deb2);
        pos += 12;

        // Entry 3: currBal=-1025.00, currCycDebit=-2500.00
        BigDecimal bal3 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("-1025.00"), bal3);
        pos += 12;
        BigDecimal deb3 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("-2500.00"), deb3);
        pos += 12;

        // Entries 4-5: all zeros
        BigDecimal bal4 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("0.00"), bal4);
        pos += 12;
        BigDecimal deb4 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("0.00"), deb4);
        pos += 12;
        BigDecimal bal5 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("0.00"), bal5);
        pos += 12;
        BigDecimal deb5 = CobolDecimalParser.parseSignedDecimal(first.substring(pos, pos + 12), 2);
        assertEquals(new BigDecimal("0.00"), deb5);
    }

    @Test
    void vbrcFileHasSixRecords() throws IOException {
        // 3 accounts * 2 records each (VB1 + VB2) = 6 lines
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);
        assertEquals(6, lines.size(), "VBRCFILE should have 6 records (2 per account)");
    }

    @Test
    void vbrcFileFirstPairMatchesCobolOutput() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);

        // VB1 for account 1: "00000000001Y" (12 chars)
        String vb1Line = lines.get(0);
        assertEquals("00000000001", vb1Line.substring(0, 11));
        assertEquals("Y", vb1Line.substring(11, 12));

        // VB2 for account 1: acctId + currBal + creditLimit + reissueYear
        String vb2Line = lines.get(1);
        assertEquals("00000000001", vb2Line.substring(0, 11));
        BigDecimal bal = CobolDecimalParser.parseSignedDecimal(vb2Line.substring(11, 23), 2);
        assertEquals(new BigDecimal("194.00"), bal);
        BigDecimal limit = CobolDecimalParser.parseSignedDecimal(vb2Line.substring(23, 35), 2);
        assertEquals(new BigDecimal("2020.00"), limit);
        assertEquals("2025", vb2Line.substring(35, 39));
    }

    @Test
    void vbrcFileSecondPairMatchesCobolOutput() throws IOException {
        Path outFile = tempDir.resolve("outfile.dat");
        Path arryFile = tempDir.resolve("arryfile.dat");
        Path vbrcFile = tempDir.resolve("vbrcfile.dat");

        new Cbact01cApplication().run(SAMPLE_INPUT, outFile, arryFile, vbrcFile);

        List<String> lines = Files.readAllLines(vbrcFile);

        // VB1 for account 2
        String vb1Line = lines.get(2);
        assertEquals("00000000002", vb1Line.substring(0, 11));
        assertEquals("Y", vb1Line.substring(11, 12));

        // VB2 for account 2
        String vb2Line = lines.get(3);
        assertEquals("00000000002", vb2Line.substring(0, 11));
        BigDecimal bal2 = CobolDecimalParser.parseSignedDecimal(vb2Line.substring(11, 23), 2);
        assertEquals(new BigDecimal("158.00"), bal2);
        BigDecimal limit2 = CobolDecimalParser.parseSignedDecimal(vb2Line.substring(23, 35), 2);
        assertEquals(new BigDecimal("6130.00"), limit2);
        assertEquals("2024", vb2Line.substring(35, 39));
    }

    @Test
    void allSampleAccountsProcessedIdentically() throws IOException {
        // Parse input, process, and verify all transformations are consistent
        try (AccountFileReader reader = new AccountFileReader(SAMPLE_INPUT)) {
            List<AccountRecord> accounts = reader.readAll();
            AccountProcessor processor = new AccountProcessor();
            var result = processor.processBatch(accounts);

            // Every account in the sample has zero debit -> all get 2525.00
            for (int i = 0; i < accounts.size(); i++) {
                OutAccountRecord out = result.outRecords().get(i);
                assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                        "Account " + out.acctId() + " zero debit should default to 2525.00");
            }

            // All array records should have correct structure
            for (int i = 0; i < accounts.size(); i++) {
                ArrayRecord arr = result.arrayRecords().get(i);
                assertEquals(accounts.get(i).acctId(), arr.acctId());
                assertEquals(accounts.get(i).currBal(), arr.entries()[0].currBal());
                assertEquals(new BigDecimal("1005.00"), arr.entries()[0].currCycDebit());
                assertEquals(new BigDecimal("-1025.00"), arr.entries()[2].currBal());
            }

            // All VB records should have correct account IDs
            for (int i = 0; i < accounts.size(); i++) {
                assertEquals(accounts.get(i).acctId(), result.vbRecords1().get(i).acctId());
                assertEquals(accounts.get(i).acctId(), result.vbRecords2().get(i).acctId());
            }
        }
    }
}
