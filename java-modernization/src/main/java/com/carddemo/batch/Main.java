package com.carddemo.batch;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for the CBACT01C batch processor.
 * <p>
 * Usage:
 * <pre>
 *   java -jar cbact01c-batch.jar &lt;inputFile&gt; &lt;outFile&gt; &lt;arrayFile&gt; &lt;vbrFile&gt;
 * </pre>
 * All four file-path arguments are required and correspond to the COBOL
 * DD-name assignments: ACCTFILE, OUTFILE, ARRYFILE, VBRCFILE.
 */
public final class Main {

    private Main() {
        // entry-point only
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("""
                    Usage: cbact01c-batch <inputFile> <outFile> <arrayFile> <vbrFile>
                    
                      inputFile  - path to the fixed-width account input file  (ACCTFILE)
                      outFile    - path for the output account file            (OUTFILE)
                      arrayFile  - path for the array output file              (ARRYFILE)
                      vbrFile    - path for the variable-length record file    (VBRCFILE)
                    """);
            System.exit(1);
        }

        var processor = new Cbact01cProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3]),
                System.out
        );

        try {
            int processed = processor.execute();
            System.out.printf("Processed %d account records.%n", processed);
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
