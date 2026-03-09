package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.AccountFileWriter;
import com.carddemo.batch.model.AccountRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Java 17+ modernization of the COBOL batch program {@code CBACT01C.cbl}.
 *
 * <h2>Original COBOL program summary</h2>
 * <p>CBACT01C reads account master records sequentially from an indexed
 * VSAM KSDS file ({@code ACCTFILE}) and, for each record, writes
 * transformed output to three files:</p>
 * <ul>
 *   <li><b>OUTFILE</b> — flat sequential file with selected account fields
 *       (LRECL 107, FB). Includes a date-reformatting step via COBDATFT
 *       and a business rule that replaces zero cycle-debit with 2525.00.</li>
 *   <li><b>ARRYFILE</b> — fixed-length file with a 5-element array of
 *       balance/debit pairs per account (LRECL 110, FB).</li>
 *   <li><b>VBRCFILE</b> — variable-length file with two records per account:
 *       a short record (12 bytes) and a longer record (39 bytes).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 *   java -jar cbact01c.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
 * </pre>
 *
 * <h2>DD-name mapping</h2>
 * <table>
 *   <tr><th>COBOL DD</th><th>CLI Argument</th></tr>
 *   <tr><td>ACCTFILE</td><td>arg[0]</td></tr>
 *   <tr><td>OUTFILE</td><td>arg[1]</td></tr>
 *   <tr><td>ARRYFILE</td><td>arg[2]</td></tr>
 *   <tr><td>VBRCFILE</td><td>arg[3]</td></tr>
 * </table>
 */
public final class Cbact01cApplication {

    private Cbact01cApplication() { }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFilePath = Path.of(args[0]);
        Path outFilePath = Path.of(args[1]);
        Path arrFilePath = Path.of(args[2]);
        Path vbrFilePath = Path.of(args[3]);

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try {
            int recordCount = run(acctFilePath, outFilePath, arrFilePath, vbrFilePath);
            System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
            System.out.println("Records processed: " + recordCount);
        } catch (BatchAbendException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println(e.getMessage());
            System.exit(999);
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println("I/O error: " + e.getMessage());
            System.exit(999);
        }
    }

    /**
     * Executes the batch processing. Extracted for testability.
     *
     * @return the number of records processed
     */
    public static int run(Path acctFile, Path outFile, Path arrFile, Path vbrFile)
            throws IOException {

        int count = 0;

        try (AccountFileReader reader = new AccountFileReader(acctFile);
             AccountFileWriter writer = new AccountFileWriter(outFile, arrFile, vbrFile)) {

            AccountProcessor processor = new AccountProcessor(writer);

            Optional<AccountRecord> record;
            while ((record = reader.readNext()).isPresent()) {
                AccountRecord acct = record.get();
                // Display the raw record (mirrors DISPLAY ACCOUNT-RECORD)
                System.out.println(acct);
                processor.process(acct);
                count++;
            }
        }

        return count;
    }

    /**
     * Mirrors the COBOL {@code 9999-ABEND-PROGRAM} paragraph that calls
     * {@code CEE3ABD} with abend code 999.
     */
    public static class BatchAbendException extends RuntimeException {
        private final int abendCode;

        public BatchAbendException(String message, int abendCode) {
            super(message);
            this.abendCode = abendCode;
        }

        public int getAbendCode() {
            return abendCode;
        }
    }
}
