package com.carddemo.batch.cbact01c;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration test for {@link Cbact01cProcessor}.
 * Feeds the 3-record sample from {@code acctdata_sample.txt} through
 * the processor and verifies every output file.
 */
class Cbact01cProcessorTest {

    @TempDir
    Path tempDir;

    /**
     * Copies the bundled sample data to a temp file and returns its path.
     */
    private Path prepareSampleInput() throws IOException {
        Path input = tempDir.resolve("acctdata.txt");
        try (var is = getClass().getResourceAsStream("/acctdata_sample.txt")) {
            assert is != null : "acctdata_sample.txt not found on classpath";
            Files.write(input, is.readAllBytes());
        }
        return input;
    }

    // ---------------------------------------------------------------
    // Full end-to-end run
    // ---------------------------------------------------------------

    @Test
    void execute_processesAllRecords() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        ByteArrayOutputStream consoleCapture = new ByteArrayOutputStream();
        PrintStream console = new PrintStream(consoleCapture, true, StandardCharsets.UTF_8);

        Cbact01cProcessor processor = new Cbact01cProcessor(
                input, outFile, arryFile, vbrcFile, console);
        processor.execute();

        assertEquals(3, processor.getRecordsRead());
        assertEquals(3, processor.getRecordsWritten());
    }

    @Test
    void execute_outFileHas3Lines() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        List<String> lines = Files.readAllLines(outFile);
        assertEquals(3, lines.size());
    }

    @Test
    void execute_arryFileHas3Lines() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        List<String> lines = Files.readAllLines(arryFile);
        assertEquals(3, lines.size());
    }

    @Test
    void execute_vbrcFileHas6Lines() throws IOException {
        // 2 VB records per account × 3 accounts = 6 lines
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        List<String> lines = Files.readAllLines(vbrcFile);
        assertEquals(6, lines.size());
    }

    // ---------------------------------------------------------------
    // Verify specific output content (record 1)
    // ---------------------------------------------------------------

    @Test
    void execute_outFileRecord1_debitDefaulted() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        String firstLine = Files.readAllLines(outFile).get(0);
        String[] fields = firstLine.split("\\|");

        // Field[0] = acct-id
        assertEquals("00000000001", fields[0]);
        // Field[7] = reissue date reformatted from 2025-05-20 → 20250520
        assertEquals("20250520", fields[7]);
        // Field[9] = cycle debit: was zero, defaulted to 2525.00
        assertEquals("2525.00", fields[9]);
    }

    @Test
    void execute_arryFileRecord1_hardCodedValues() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        String firstLine = Files.readAllLines(arryFile).get(0);
        String[] fields = firstLine.split("\\|");

        assertEquals("00000000001", fields[0]);
        assertEquals("1005.00", fields[2]);   // slot 1 debit
        assertEquals("1525.00", fields[4]);   // slot 2 debit
        assertEquals("-1025.00", fields[5]);  // slot 3 balance
        assertEquals("-2500.00", fields[6]);  // slot 3 debit
        assertEquals("0", fields[7]);         // slot 4 balance (zero)
    }

    @Test
    void execute_vbrcFileRecord1_vb1AndVb2() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole).execute();

        List<String> lines = Files.readAllLines(vbrcFile);

        // VB1 for record 1
        assertEquals("00000000001|Y", lines.get(0));

        // VB2 for record 1
        String[] vb2Fields = lines.get(1).split("\\|");
        assertEquals("00000000001", vb2Fields[0]);
        assertEquals("2025", vb2Fields[3]); // reissue year
    }

    // ---------------------------------------------------------------
    // Console output
    // ---------------------------------------------------------------

    @Test
    void execute_displaysStartAndEnd() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        PrintStream console = new PrintStream(capture, true, StandardCharsets.UTF_8);

        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, console).execute();

        String output = capture.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("START OF EXECUTION OF PROGRAM CBACT01C"));
        assertTrue(output.contains("END OF EXECUTION OF PROGRAM CBACT01C"));
    }

    @Test
    void execute_displaysAllAccountFields() throws IOException {
        Path input = prepareSampleInput();
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        PrintStream console = new PrintStream(capture, true, StandardCharsets.UTF_8);

        new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, console).execute();

        String output = capture.toString(StandardCharsets.UTF_8);
        // Check that the display paragraph outputs are present
        assertTrue(output.contains("ACCT-ID                 :"));
        assertTrue(output.contains("ACCT-ACTIVE-STATUS      :"));
        assertTrue(output.contains("ACCT-CURR-BAL           :"));
        assertTrue(output.contains("ACCT-GROUP-ID           :"));
        assertTrue(output.contains("-------------------------------------------------"));
    }

    // ---------------------------------------------------------------
    // Empty input
    // ---------------------------------------------------------------

    @Test
    void execute_emptyInput_producesEmptyOutputs() throws IOException {
        Path input = tempDir.resolve("empty.txt");
        Files.writeString(input, "");
        Path outFile = tempDir.resolve("outfile.txt");
        Path arryFile = tempDir.resolve("arryfile.txt");
        Path vbrcFile = tempDir.resolve("vbrcfile.txt");

        PrintStream nullConsole = new PrintStream(new ByteArrayOutputStream());
        Cbact01cProcessor processor =
                new Cbact01cProcessor(input, outFile, arryFile, vbrcFile, nullConsole);
        processor.execute();

        assertEquals(0, processor.getRecordsRead());
        assertEquals(0, processor.getRecordsWritten());
        assertEquals(0, Files.readAllLines(outFile).size());
    }
}
