package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.RecordWriter;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VariableLengthRecord1;
import com.carddemo.batch.model.VariableLengthRecord2;
import com.carddemo.batch.service.AccountFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Java 17+ modernisation of the COBOL batch program CBACT01C.
 *
 * <h2>Original COBOL program purpose</h2>
 * Read an indexed VSAM account master file sequentially and write each
 * account's data into three output files:
 * <ol>
 *   <li><b>OUTFILE</b> — flat account extract (selected/transformed fields)</li>
 *   <li><b>ARRYFILE</b> — array-style records with 5 balance/debit slots</li>
 *   <li><b>VBRCFILE</b> — two variable-length records per account
 *       (short: ID+status, long: ID+balance+limit+year)</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>
 * java -jar cbact01c-batch.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
 * </pre>
 *
 * <p>Exit codes mirror the COBOL APPL-RESULT values:
 * <ul>
 *   <li>0 — success</li>
 *   <li>12 — I/O error</li>
 *   <li>999 — abend (fatal error)</li>
 * </ul>
 */
public final class Cbact01cApplication {

    private Cbact01cApplication() {
        // entry-point class
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: CBACT01C <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(999);
        }

        Path acctFilePath = Path.of(args[0]);
        Path outFilePath = Path.of(args[1]);
        Path arryFilePath = Path.of(args[2]);
        Path vbrcFilePath = Path.of(args[3]);

        int exitCode = run(acctFilePath, outFilePath, arryFilePath, vbrcFilePath);
        System.exit(exitCode);
    }

    /**
     * Execute the batch job. Returns an exit code (0 = success).
     * This method is separated from {@code main} so it can be called
     * from unit tests without triggering {@code System.exit}.
     */
    public static int run(Path acctFile, Path outFile, Path arryFile, Path vbrcFile) {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (
                AccountFileReader reader = new AccountFileReader(acctFile);
                RecordWriter outWriter = new RecordWriter(outFile);
                RecordWriter arryWriter = new RecordWriter(arryFile);
                RecordWriter vbrcWriter = new RecordWriter(vbrcFile)
        ) {
            for (AccountRecord account : reader) {
                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(account);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = AccountFileProcessor.buildOutputRecord(account);
                outWriter.write(outRec.toDelimitedString());

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = AccountFileProcessor.buildArrayRecord(account);
                arryWriter.write(arrRec.toDelimitedString());

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VBx-RECORD
                VariableLengthRecord1 vb1 = AccountFileProcessor.buildVbRecord1(account);
                VariableLengthRecord2 vb2 = AccountFileProcessor.buildVbRecord2(account);
                vbrcWriter.write(vb1.toDelimitedString());
                vbrcWriter.write(vb2.toDelimitedString());
            }

            System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
            System.out.printf("Records written — OUT: %d, ARRY: %d, VBRC: %d%n",
                    outWriter.getRecordsWritten(),
                    arryWriter.getRecordsWritten(),
                    vbrcWriter.getRecordsWritten());
            return 0;

        } catch (IOException e) {
            System.err.println("ERROR READING/WRITING FILE: " + e.getMessage());
            System.err.println("ABENDING PROGRAM");
            return 999;
        }
    }

    /**
     * Display account fields to stdout (mirrors 1100-DISPLAY-ACCT-RECORD).
     */
    private static void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.acctCurrBal().toPlainString());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.acctCreditLimit().toPlainString());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.acctCashCreditLimit().toPlainString());
        System.out.println("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.acctExpiraionDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.acctCurrCycCredit().toPlainString());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.acctCurrCycDebit().toPlainString());
        System.out.println("ACCT-GROUP-ID           :" + acct.acctGroupId());
        System.out.println("-------------------------------------------------");
    }
}
