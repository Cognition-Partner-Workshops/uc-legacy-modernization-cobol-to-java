package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrRecord1;
import com.carddemo.batch.model.VbrRecord2;
import com.carddemo.batch.service.AccountProcessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Java 17+ rewrite of the COBOL batch program CBACT01C.cbl.
 *
 * <p>This program reads account records from an indexed (VSAM KSDS) account
 * file and writes transformed data into three output files:</p>
 * <ol>
 *   <li><b>OUTFILE</b>  - sequential account output (one record per account)</li>
 *   <li><b>ARRYFILE</b> - array-format output (balance array per account)</li>
 *   <li><b>VBRCFILE</b> - variable-length records (two records per account)</li>
 * </ol>
 *
 * <p>Usage: {@code java -jar cbact01c-batch.jar <acctfile> <outfile> <arryfile> <vbrcfile>}</p>
 */
public class Cbact01cApplication {

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    public Cbact01cApplication(Path acctFilePath, Path outFilePath,
                                Path arryFilePath, Path vbrcFilePath) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
    }

    /**
     * Executes the batch processing — mirrors the COBOL PROCEDURE DIVISION.
     *
     * @return the number of records processed
     * @throws IOException on any file I/O error (mirrors COBOL 9999-ABEND-PROGRAM)
     */
    public int execute() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int recordCount = 0;

        try (AccountFileReader acctReader = new AccountFileReader(acctFilePath);
             BufferedWriter outWriter = Files.newBufferedWriter(outFilePath);
             BufferedWriter arryWriter = Files.newBufferedWriter(arryFilePath);
             BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFilePath)) {

            var optRecord = acctReader.readNext();
            while (optRecord.isPresent()) {
                AccountRecord acct = optRecord.get();

                // 1100-DISPLAY-ACCT-RECORD
                List<String> displayLines = AccountProcessor.displayRecord(acct);
                displayLines.forEach(System.out::println);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutAccountRecord outRec = AccountProcessor.buildOutRecord(acct);
                outWriter.write(outRec.toOutputLine());
                outWriter.newLine();

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = AccountProcessor.buildArrayRecord(acct);
                arryWriter.write(arrRec.toOutputLine());
                arryWriter.newLine();

                // 1500-POPUL-VBRC-RECORD + 1550-WRITE-VB1-RECORD
                VbrRecord1 vbr1 = AccountProcessor.buildVbrRecord1(acct);
                vbrcWriter.write(vbr1.toOutputLine());
                vbrcWriter.newLine();

                // 1575-WRITE-VB2-RECORD
                VbrRecord2 vbr2 = AccountProcessor.buildVbrRecord2(acct);
                vbrcWriter.write(vbr2.toOutputLine());
                vbrcWriter.newLine();

                recordCount++;
                optRecord = acctReader.readNext();
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordCount;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: CBACT01C <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Cbact01cApplication app = new Cbact01cApplication(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        try {
            int count = app.execute();
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
