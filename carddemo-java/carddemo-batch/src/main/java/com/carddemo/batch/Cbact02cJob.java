package com.carddemo.batch;

import com.carddemo.common.io.CardRecordIO;
import com.carddemo.common.io.SequentialFileReader;
import com.carddemo.common.model.CardRecord;
import com.carddemo.common.util.AbendException;

import java.nio.file.Path;

/**
 * Migrated from COBOL program CBACT02C.cbl.
 * <p>
 * Reads the card data file (VSAM KSDS, sequential access) and prints
 * each card record to stdout.
 */
public class Cbact02cJob {

    private final Path cardFilePath;

    public Cbact02cJob(Path cardFilePath) {
        this.cardFilePath = cardFilePath;
    }

    public void execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT02C");

        try (var reader = new SequentialFileReader<>(cardFilePath, CardRecordIO.PARSER)) {
            for (CardRecord card : reader) {
                System.out.println(CardRecordIO.FORMATTER.format(card));
            }
        } catch (AbendException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT02C");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Cbact02cJob <cardfile>");
            System.exit(1);
        }
        new Cbact02cJob(Path.of(args[0])).execute();
    }
}
