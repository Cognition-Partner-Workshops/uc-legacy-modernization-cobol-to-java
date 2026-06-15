package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.io.CobolFieldParser;
import com.carddemo.io.DateConverter;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.ArrayRecord.ArrayEntry;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VbrcRecord1;
import com.carddemo.model.VbrcRecord2;

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
 * Parity tests verifying the Java translation produces identical results
 * to the COBOL CBACT01C program for sample account data.
 */
class AccountFileProcessorTest {

    private static final Path FIXTURE_DIR = resolveFixtureDir();

    @TempDir
    Path tempDir;

    private Path outputFile;
    private Path arrayFile;
    private Path vbrcFile;

    @BeforeEach
    void setUp() {
        outputFile = tempDir.resolve("outfile.dat");
        arrayFile = tempDir.resolve("arryfile.dat");
        vbrcFile = tempDir.resolve("vbrcfile.dat");
    }

    private static Path resolveFixtureDir() {
        Path dir = Path.of("../app/data/ASCII");
        if (!Files.exists(dir)) {
            dir = Path.of("app/data/ASCII");
        }
        return dir;
    }

    @Test
    void testReadAllAccountRecords() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        try (final AccountFileReader reader = new AccountFileReader(acctFile)) {
            final List<AccountRecord> records = reader.readAll();
            assertFalse(records.isEmpty(), "Should read at least one account record");

            final AccountRecord first = records.getFirst();
            assertEquals("00000000001", first.acctId());
            assertEquals("Y", first.acctActiveStatus());
            assertEquals(new BigDecimal("194.00"), first.acctCurrBal());
            assertEquals(new BigDecimal("2020.00"), first.acctCreditLimit());
            assertEquals(new BigDecimal("1020.00"), first.acctCashCreditLimit());
            assertEquals("2014-11-20", first.acctOpenDate());
            assertEquals("2025-05-20", first.acctExpirationDate());
            assertEquals("2025-05-20", first.acctReissueDate());
            assertEquals(new BigDecimal("0.00"), first.acctCurrCycCredit());
            assertEquals(new BigDecimal("0.00"), first.acctCurrCycDebit());
        }
    }

    @Test
    void testFullProcessingWithFixtureData() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<OutputAccountRecord> outputs = processor.getOutputRecords();
        final List<ArrayRecord> arrays = processor.getArrayRecords();
        final List<Object> vbrcs = processor.getVbrcRecords();

        assertFalse(outputs.isEmpty());
        assertEquals(outputs.size(), arrays.size());
        assertEquals(outputs.size() * 2, vbrcs.size());

        assertTrue(Files.exists(outputFile));
        assertTrue(Files.exists(arrayFile));
        assertTrue(Files.exists(vbrcFile));
    }

    @Test
    void testDebitSubstitutionBusinessRule() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<OutputAccountRecord> outputs = processor.getOutputRecords();
        final OutputAccountRecord first = outputs.getFirst();

        // Business rule: when ACCT-CURR-CYC-DEBIT = 0, substitute 2525.00
        assertEquals(new BigDecimal("2525.00"), first.acctCurrCycDebit(),
                "Zero debit should be substituted with 2525.00 per COBOL business rule");
    }

    @Test
    void testDateConversionReissueDate() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<OutputAccountRecord> outputs = processor.getOutputRecords();
        final OutputAccountRecord first = outputs.getFirst();

        // COBDATFT converts YYYY-MM-DD → YYYYMMDD (type=2, outtype=2)
        assertEquals("20250520", first.acctReissueDate(),
                "Reissue date should be reformatted from YYYY-MM-DD to YYYYMMDD");
    }

    @Test
    void testArrayRecordPopulation() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<ArrayRecord> arrays = processor.getArrayRecords();
        final ArrayRecord first = arrays.getFirst();

        assertEquals("00000000001", first.acctId());
        assertEquals(5, first.entries().length);

        // Entry 1: account balance + hardcoded 1005.00
        assertEquals(new BigDecimal("194.00"), first.entries()[0].acctCurrBal());
        assertEquals(new BigDecimal("1005.00"), first.entries()[0].acctCurrCycDebit());

        // Entry 2: account balance + hardcoded 1525.00
        assertEquals(new BigDecimal("194.00"), first.entries()[1].acctCurrBal());
        assertEquals(new BigDecimal("1525.00"), first.entries()[1].acctCurrCycDebit());

        // Entry 3: hardcoded -1025.00 + -2500.00
        assertEquals(new BigDecimal("-1025.00"), first.entries()[2].acctCurrBal());
        assertEquals(new BigDecimal("-2500.00"), first.entries()[2].acctCurrCycDebit());

        // Entries 4-5: zero (INITIALIZE)
        assertEquals(BigDecimal.ZERO, first.entries()[3].acctCurrBal());
        assertEquals(BigDecimal.ZERO, first.entries()[3].acctCurrCycDebit());
        assertEquals(BigDecimal.ZERO, first.entries()[4].acctCurrBal());
        assertEquals(BigDecimal.ZERO, first.entries()[4].acctCurrCycDebit());
    }

    @Test
    void testVbrcRecordPopulation() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<Object> vbrcs = processor.getVbrcRecords();

        // First two records for account 1: one VbrcRecord1 + one VbrcRecord2
        assertInstanceOf(VbrcRecord1.class, vbrcs.get(0));
        assertInstanceOf(VbrcRecord2.class, vbrcs.get(1));

        final VbrcRecord1 vb1 = (VbrcRecord1) vbrcs.get(0);
        assertEquals("00000000001", vb1.acctId());
        assertEquals("Y", vb1.acctActiveStatus());

        final VbrcRecord2 vb2 = (VbrcRecord2) vbrcs.get(1);
        assertEquals("00000000001", vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), vb2.acctCreditLimit());
        assertEquals("2025", vb2.acctReissueYear());
    }

    @Test
    void testOutputFileFormat() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
        processor.execute();

        final List<String> lines = Files.readAllLines(outputFile);
        assertFalse(lines.isEmpty());

        final String firstLine = lines.getFirst();
        // Verify the output starts with account ID
        assertTrue(firstLine.startsWith("00000000001"));
        // Verify active status follows
        assertEquals('Y', firstLine.charAt(11));
    }

    @Test
    void testAllRecordsProcessed() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        try (final AccountFileReader reader = new AccountFileReader(acctFile)) {
            final int inputCount = reader.readAll().size();

            final var processor = new AccountFileProcessor(acctFile, outputFile, arrayFile, vbrcFile);
            processor.execute();

            assertEquals(inputCount, processor.getOutputRecords().size(),
                    "All input records should produce output records");
            assertEquals(inputCount, processor.getArrayRecords().size(),
                    "All input records should produce array records");
            assertEquals(inputCount * 2, processor.getVbrcRecords().size(),
                    "Each input record should produce two VBRC records");
        }
    }

    @Test
    void testMultipleAccountValues() throws IOException {
        final Path acctFile = FIXTURE_DIR.resolve("acctdata.txt");
        if (!Files.exists(acctFile)) return;

        try (final AccountFileReader reader = new AccountFileReader(acctFile)) {
            final List<AccountRecord> records = reader.readAll();
            assertTrue(records.size() >= 5, "Test fixture should have at least 5 records");

            // Verify account 2
            final AccountRecord second = records.get(1);
            assertEquals("00000000002", second.acctId());
            assertEquals("Y", second.acctActiveStatus());
            assertEquals(new BigDecimal("158.00"), second.acctCurrBal());
            assertEquals(new BigDecimal("6130.00"), second.acctCreditLimit());
            assertEquals(new BigDecimal("5448.00"), second.acctCashCreditLimit());
            assertEquals("2013-06-19", second.acctOpenDate());

            // Verify account 5
            final AccountRecord fifth = records.get(4);
            assertEquals("00000000005", fifth.acctId());
            assertEquals(new BigDecimal("345.00"), fifth.acctCurrBal());
            assertEquals(new BigDecimal("3819.00"), fifth.acctCreditLimit());
        }
    }
}
