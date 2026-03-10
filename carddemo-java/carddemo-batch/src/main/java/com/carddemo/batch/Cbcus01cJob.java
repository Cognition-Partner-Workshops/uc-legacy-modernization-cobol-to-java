package com.carddemo.batch;

import com.carddemo.common.io.CustomerRecordIO;
import com.carddemo.common.io.SequentialFileReader;
import com.carddemo.common.model.CustomerRecord;
import com.carddemo.common.util.AbendException;

import java.nio.file.Path;

/**
 * Migrated from COBOL program CBCUS01C.cbl.
 * <p>
 * Reads the customer data file (VSAM KSDS, sequential access) and prints
 * each customer record to stdout.
 */
public class Cbcus01cJob {

    private final Path custFilePath;

    public Cbcus01cJob(Path custFilePath) {
        this.custFilePath = custFilePath;
    }

    public void execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBCUS01C");

        try (var reader = new SequentialFileReader<>(custFilePath, CustomerRecordIO.PARSER)) {
            for (CustomerRecord customer : reader) {
                // COBOL displays the record twice: once in GET-NEXT and once in main loop
                String formatted = CustomerRecordIO.FORMATTER.format(customer);
                System.out.println(formatted);
                System.out.println(formatted);
            }
        } catch (AbendException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBCUS01C");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Cbcus01cJob <custfile>");
            System.exit(1);
        }
        new Cbcus01cJob(Path.of(args[0])).execute();
    }
}
