package com.carddemo.batch;

import com.carddemo.batch.exception.AbendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the CBACT01C Java migration.
 *
 * Usage:
 *   java -jar cbact01c.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
 *
 * Arguments correspond to the COBOL DD names:
 *   ACCTFILE  - Input indexed account file (fixed-width text, 300-byte records)
 *   OUTFILE   - Output account file (pipe-delimited)
 *   ARRYFILE  - Output array file (pipe-delimited)
 *   VBRCFILE  - Output variable-record file (pipe-delimited, alternating short/long records)
 */
public class Cbact01cApplication {

    private static final Logger log = LoggerFactory.getLogger(Cbact01cApplication.class);

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFile = Paths.get(args[0]);
        Path outFile = Paths.get(args[1]);
        Path arryFile = Paths.get(args[2]);
        Path vbrcFile = Paths.get(args[3]);

        try {
            Cbact01cProcessor processor = new Cbact01cProcessor(
                    acctFile, outFile, arryFile, vbrcFile
            );
            Cbact01cProcessor.ProcessingResult result = processor.execute();
            log.info("Processed {} account records", result.outRecords().size());
        } catch (AbendException e) {
            log.error("ABENDING PROGRAM - code {}: {}", e.getAbendCode(), e.getMessage());
            System.exit(e.getAbendCode());
        }
    }
}
