package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.ArrayFileWriter;
import com.carddemo.batch.io.OutFileWriter;
import com.carddemo.batch.io.VbRecordFileWriter;
import com.carddemo.batch.model.*;
import com.carddemo.batch.service.AccountProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Java 17+ modernization of COBOL batch program CBACT01C.
 *
 * <h2>Original COBOL Program Description</h2>
 * <pre>
 * PROGRAM     : CBACT01C.CBL
 * Application : CardDemo
 * Type        : BATCH COBOL Program
 * FUNCTION    : READ THE ACCOUNT FILE AND WRITE INTO FILES.
 * </pre>
 *
 * <h2>File I/O Mapping</h2>
 * <table>
 *   <tr><th>COBOL DD Name</th><th>Direction</th><th>Description</th></tr>
 *   <tr><td>ACCTFILE</td><td>Input</td><td>VSAM KSDS account master</td></tr>
 *   <tr><td>OUTFILE</td><td>Output</td><td>Flat sequential account extract (LRECL=107)</td></tr>
 *   <tr><td>ARRYFILE</td><td>Output</td><td>Array-based sequential file (LRECL=110)</td></tr>
 *   <tr><td>VBRCFILE</td><td>Output</td><td>Variable-length record file (LRECL=84, VB)</td></tr>
 * </table>
 *
 * <h2>Usage</h2>
 * <pre>
 * java -jar cbact01c-java.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
 * </pre>
 */
public class Cbact01cApplication {

    private final AccountProcessor processor = new AccountProcessor();

    /**
     * Main entry point — mirrors the COBOL PROCEDURE DIVISION.
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFilePath = Path.of(args[0]);
        Path outFilePath = Path.of(args[1]);
        Path arryFilePath = Path.of(args[2]);
        Path vbrcFilePath = Path.of(args[3]);

        Cbact01cApplication app = new Cbact01cApplication();
        try {
            app.run(acctFilePath, outFilePath, arryFilePath, vbrcFilePath);
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }

    /**
     * Executes the full batch process.
     *
     * <p>Corresponds to the COBOL main paragraph:
     * <pre>
     * PERFORM 0000-ACCTFILE-OPEN
     * PERFORM 2000-OUTFILE-OPEN
     * PERFORM 3000-ARRFILE-OPEN
     * PERFORM 4000-VBRFILE-OPEN
     * PERFORM UNTIL END-OF-FILE = 'Y'
     *     PERFORM 1000-ACCTFILE-GET-NEXT
     * END-PERFORM
     * PERFORM 9000-ACCTFILE-CLOSE
     * </pre>
     */
    public void run(Path acctFilePath, Path outFilePath,
                    Path arryFilePath, Path vbrcFilePath) throws IOException {

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        // 0000-ACCTFILE-OPEN / 2000-OUTFILE-OPEN / 3000-ARRFILE-OPEN / 4000-VBRFILE-OPEN
        try (AccountFileReader reader = new AccountFileReader(acctFilePath);
             OutFileWriter outWriter = new OutFileWriter(outFilePath);
             ArrayFileWriter arryWriter = new ArrayFileWriter(arryFilePath);
             VbRecordFileWriter vbrcWriter = new VbRecordFileWriter(vbrcFilePath)) {

            // Read all account records
            List<AccountRecord> accounts = reader.readAll();

            for (AccountRecord acct : accounts) {
                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutAccountRecord outRec = processor.toOutRecord(acct);
                outWriter.write(outRec);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = processor.toArrayRecord(acct);
                arryWriter.write(arrRec);

                // 1500-POPUL-VBRC-RECORD + 1550-WRITE-VB1-RECORD + 1575-WRITE-VB2-RECORD
                VbRecord1 vb1 = processor.toVbRecord1(acct);
                VbRecord2 vb2 = processor.toVbRecord2(acct);
                vbrcWriter.writeVb1(vb1);
                vbrcWriter.writeVb2(vb2);
            }
        }

        // 9000-ACCTFILE-CLOSE (handled by try-with-resources)
        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    /**
     * Displays account record fields to stdout.
     * Corresponds to COBOL paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    private void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.acctId());
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.currBal());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        System.out.println("ACCT-OPEN-DATE          :" + acct.openDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit());
        System.out.println("ACCT-GROUP-ID           :" + acct.groupId());
        System.out.println("-------------------------------------------------");
    }
}
