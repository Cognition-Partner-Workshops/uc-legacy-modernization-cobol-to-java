package com.carddemo.batch;

import com.carddemo.common.io.CardXrefRecordIO;
import com.carddemo.common.io.SequentialFileReader;
import com.carddemo.common.model.CardXrefRecord;
import com.carddemo.common.util.AbendException;

import java.nio.file.Path;

/**
 * Migrated from COBOL program CBACT03C.cbl.
 * <p>
 * Reads the card cross-reference file (VSAM KSDS, sequential access)
 * and prints each record to stdout.
 */
public class Cbact03cJob {

    private final Path xrefFilePath;

    public Cbact03cJob(Path xrefFilePath) {
        this.xrefFilePath = xrefFilePath;
    }

    public void execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT03C");

        try (var reader = new SequentialFileReader<>(xrefFilePath, CardXrefRecordIO.PARSER)) {
            for (CardXrefRecord xref : reader) {
                // COBOL displays the record twice: once in GET-NEXT and once in main loop
                String formatted = CardXrefRecordIO.FORMATTER.format(xref);
                System.out.println(formatted);
                System.out.println(formatted);
            }
        } catch (AbendException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT03C");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Cbact03cJob <xreffile>");
            System.exit(1);
        }
        new Cbact03cJob(Path.of(args[0])).execute();
    }
}
