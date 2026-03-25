package com.carddemo.batch;

import com.carddemo.batch.io.CobolDataParser;
import com.carddemo.batch.io.RecordWriter;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import com.carddemo.batch.util.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 *
 * <p>CBACT01C reads an indexed VSAM account file sequentially and produces
 * three output files:
 * <ol>
 *   <li><b>OUTFILE</b> — Flat account records with reformatted dates and
 *       a cycle-debit override (zero → 2525.00)</li>
 *   <li><b>ARRYFILE</b> — Array records with 5 balance/debit slots per account
 *       (slots 0-2 populated, 3-4 zeroed)</li>
 *   <li><b>VBRCFILE</b> — Two variable-length records per account:
 *       a short record (ID + status) and a long record (ID + bal + limit + year)</li>
 * </ol>
 *
 * @see <a href="../../app/cbl/CBACT01C.cbl">Original COBOL source</a>
 */
public class AccountFileProcessor {

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    private int recordsProcessed = 0;

    public AccountFileProcessor(Path inputFile, Path outFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch job: read all account records and write to the three
     * output files.
     *
     * @return list of all parsed AccountRecords (useful for testing)
     * @throws IOException if any file operation fails
     */
    public List<AccountRecord> execute() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter outWriter = Files.newBufferedWriter(outFile);
             BufferedWriter arrWriter = Files.newBufferedWriter(arrayFile);
             BufferedWriter vbrWriter = Files.newBufferedWriter(vbrcFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord account = CobolDataParser.parseLine(line);
                records.add(account);

                displayAccountRecord(account);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                String formattedReissueDate = DateFormatter.toCompact(account.reissueDate());
                OutputAccountRecord outRec = OutputAccountRecord.fromAccountRecord(
                        account, formattedReissueDate);
                RecordWriter.writeOutputRecord(outWriter, outRec);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayAccountRecord arrRec = ArrayAccountRecord.fromAccountRecord(account);
                RecordWriter.writeArrayRecord(arrWriter, arrRec);

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VBx-RECORD
                VbrcRecord1 vb1 = VbrcRecord1.fromAccountRecord(account);
                VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(account);
                RecordWriter.writeVbrc1(vbrWriter, vb1);
                RecordWriter.writeVbrc2(vbrWriter, vb2);

                recordsProcessed++;
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        System.out.printf("Records processed: %d%n", recordsProcessed);

        return records;
    }

    /**
     * Display account record fields to stdout, mirroring COBOL paragraph
     * 1100-DISPLAY-ACCT-RECORD.
     */
    private void displayAccountRecord(AccountRecord rec) {
        System.out.printf("ACCT-ID                 :%d%n", rec.acctId());
        System.out.printf("ACCT-ACTIVE-STATUS      :%s%n", rec.activeStatus());
        System.out.printf("ACCT-CURR-BAL           :%s%n", rec.currBal().toPlainString());
        System.out.printf("ACCT-CREDIT-LIMIT       :%s%n", rec.creditLimit().toPlainString());
        System.out.printf("ACCT-CASH-CREDIT-LIMIT  :%s%n", rec.cashCreditLimit().toPlainString());
        System.out.printf("ACCT-OPEN-DATE          :%s%n", rec.openDate());
        System.out.printf("ACCT-EXPIRAION-DATE     :%s%n", rec.expirationDate());
        System.out.printf("ACCT-REISSUE-DATE       :%s%n", rec.reissueDate());
        System.out.printf("ACCT-CURR-CYC-CREDIT    :%s%n", rec.currCycCredit().toPlainString());
        System.out.printf("ACCT-CURR-CYC-DEBIT     :%s%n", rec.currCycDebit().toPlainString());
        System.out.printf("ACCT-GROUP-ID           :%s%n", rec.groupId());
        System.out.println("-------------------------------------------------");
    }

    /** Returns the count of records processed in the last execution. */
    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    /**
     * CLI entry point. Usage:
     * <pre>
     *   java -jar cbact01c.jar &lt;input&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
     * </pre>
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: AccountFileProcessor <input> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3])
        );

        try {
            processor.execute();
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.printf("FILE STATUS IS: %s%n", e.getMessage());
            System.exit(999);
        }
    }
}
