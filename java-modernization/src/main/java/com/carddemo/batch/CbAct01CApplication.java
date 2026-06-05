package com.carddemo.batch;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry-point for the modernised CBACT01C batch job.
 *
 * <p>Usage:</p>
 * <pre>
 *   java -jar cbact01c-batch.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arrayfile&gt; &lt;vbfile&gt;
 * </pre>
 *
 * <p>Mimics the original COBOL program's console output:</p>
 * <ul>
 *   <li>{@code START OF EXECUTION OF PROGRAM CBACT01C}</li>
 *   <li>Per-record field display</li>
 *   <li>{@code END OF EXECUTION OF PROGRAM CBACT01C}</li>
 * </ul>
 */
public final class CbAct01CApplication {

    private CbAct01CApplication() {}

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: CbAct01CApplication <acctfile> <outfile> <arrayfile> <vbfile>");
            System.exit(1);
        }

        Path acctFile  = Path.of(args[0]);
        Path outFile   = Path.of(args[1]);
        Path arrayFile = Path.of(args[2]);
        Path vbFile    = Path.of(args[3]);

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try {
            int count = CbAct01CProcessor.execute(
                    acctFile, outFile, arrayFile, vbFile);
            System.out.println("Processed " + count + " account record(s).");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println("ERROR: " + e.getMessage());
            System.exit(999);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }
}
