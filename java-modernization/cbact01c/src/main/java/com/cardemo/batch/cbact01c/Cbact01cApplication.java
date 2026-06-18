package com.cardemo.batch.cbact01c;

import java.io.IOException;
import java.nio.file.Path;

/**
 * CLI entry-point — drop-in replacement for the JCL step that invokes
 * CBACT01C on the mainframe.
 *
 * <p>Usage:
 * <pre>
 *   java -jar cbact01c.jar &lt;ACCTFILE&gt; &lt;OUTFILE&gt; &lt;ARRYFILE&gt; &lt;VBRCFILE&gt;
 * </pre>
 */
public final class Cbact01cApplication {

    private Cbact01cApplication() {}

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: cbact01c <ACCTFILE> <OUTFILE> <ARRYFILE> <VBRCFILE>");
            System.exit(1);
        }

        Path acctFile = Path.of(args[0]);
        Path outFile = Path.of(args[1]);
        Path arryFile = Path.of(args[2]);
        Path vbrcFile = Path.of(args[3]);

        try {
            AccountFileProcessor.execute(acctFile, outFile, arryFile, vbrcFile);
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
