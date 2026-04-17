package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.util.DateConverter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17 modernization of the COBOL batch program CBACT01C.
 * <p>
 * <b>Original function:</b> Read the Account VSAM KSDS file sequentially and
 * produce three output files:
 * <ol>
 *   <li><b>OUTFILE</b> &mdash; fixed-length account records with a
 *       reformatted reissue date and a default debit override.</li>
 *   <li><b>ARRYFILE</b> &mdash; array-style records repeating the balance
 *       into multiple slots with hard-coded debit values.</li>
 *   <li><b>VBRCFILE</b> &mdash; two variable-length records per account
 *       (a short status record and a longer balance/limit record).</li>
 * </ol>
 * <p>
 * All display output produced by the COBOL program is reproduced on
 * {@code System.out} so that tests can capture and compare it.
 */
public class Cbact01cApplication {

    /** Default debit value substituted when ACCT-CURR-CYC-DEBIT is zero. */
    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");

    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arryFile;
    private final Path vbrcFile;

    public Cbact01cApplication(Path inputFile, Path outFile, Path arryFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arryFile = arryFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch job, reading the input account file and writing all
     * three output files.  Console output mirrors the COBOL DISPLAY statements.
     *
     * @return the list of {@link AccountRecord}s that were processed
     * @throws IOException if any file I/O fails
     */
    public List<AccountRecord> execute() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
             BufferedWriter outWriter = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8);
             BufferedWriter arryWriter = Files.newBufferedWriter(arryFile, StandardCharsets.UTF_8);
             BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFile, StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord acct = AccountRecord.parse(line);
                records.add(acct);

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD  +  1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = populateOutputRecord(acct);
                outWriter.write(outRec.toFixedWidth());
                outWriter.newLine();

                // 1400-POPUL-ARRAY-RECORD  +  1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = populateArrayRecord(acct);
                arryWriter.write(arrRec.toFixedWidth());
                arryWriter.newLine();

                // 1500-POPUL-VBRC-RECORD + 1550 / 1575 WRITE
                VbrcRecord1 vb1 = populateVbrcRecord1(acct);
                VbrcRecord2 vb2 = populateVbrcRecord2(acct);

                System.out.println("VBRC-REC1:" + vb1.toFixedWidth());
                System.out.println("VBRC-REC2:" + vb2.toFixedWidth());

                vbrcWriter.write(vb1.toFixedWidth());
                vbrcWriter.newLine();
                vbrcWriter.write(vb2.toFixedWidth());
                vbrcWriter.newLine();
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return records;
    }

    // -- display (mirrors 1100-DISPLAY-ACCT-RECORD) --------------------------

    private void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        System.out.println("ACCT-CURR-BAL           :" + formatDisplayDecimal(acct.acctCurrBal()));
        System.out.println("ACCT-CREDIT-LIMIT       :" + formatDisplayDecimal(acct.acctCreditLimit()));
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + formatDisplayDecimal(acct.acctCashCreditLimit()));
        System.out.println("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.acctExpiraionDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + formatDisplayDecimal(acct.acctCurrCycCredit()));
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + formatDisplayDecimal(acct.acctCurrCycDebit()));
        System.out.println("ACCT-GROUP-ID           :" + acct.acctGroupId());
        System.out.println("-------------------------------------------------");
    }

    /**
     * Format a BigDecimal the way COBOL DISPLAY would render PIC S9(10)V99
     * — as a signed zoned-decimal string with trailing overpunch.
     */
    static String formatDisplayDecimal(BigDecimal value) {
        return OutputAccountRecord.formatSignedZoned(value);
    }

    // -- populate output record (mirrors 1300-POPUL-ACCT-RECORD) -------------

    OutputAccountRecord populateOutputRecord(AccountRecord acct) {
        // Convert reissue date: YYYY-MM-DD → YYYYMMDD
        String convertedReissueDate = DateConverter.convert(
                acct.acctReissueDate(), '2', '2');

        // Pad to 10 characters to match PIC X(10) output field
        convertedReissueDate = String.format("%-10s", convertedReissueDate);

        // Default debit override
        BigDecimal debit = acct.acctCurrCycDebit().signum() == 0
                ? DEFAULT_DEBIT
                : acct.acctCurrCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpiraionDate(),
                convertedReissueDate,
                acct.acctCurrCycCredit(),
                debit,
                acct.acctGroupId()
        );
    }

    // -- populate array record (mirrors 1400-POPUL-ARRAY-RECORD) -------------

    ArrayRecord populateArrayRecord(AccountRecord acct) {
        ArrayRecord arr = ArrayRecord.initialized(acct.acctId());

        // Slot 1
        arr.balances()[0] = acct.acctCurrBal();
        arr.debits()[0] = ARR_DEBIT_1;

        // Slot 2
        arr.balances()[1] = acct.acctCurrBal();
        arr.debits()[1] = ARR_DEBIT_2;

        // Slot 3
        arr.balances()[2] = ARR_BAL_3;
        arr.debits()[2] = ARR_DEBIT_3;

        // Slots 4 and 5 remain at zero (INITIALIZE)

        return arr;
    }

    // -- populate VBRC records (mirrors 1500-POPUL-VBRC-RECORD) --------------

    VbrcRecord1 populateVbrcRecord1(AccountRecord acct) {
        return new VbrcRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    VbrcRecord2 populateVbrcRecord2(AccountRecord acct) {
        // Extract YYYY portion of the reissue date (YYYY-MM-DD)
        String reissueYyyy = acct.acctReissueDate().substring(0, 4);
        return new VbrcRecord2(
                acct.acctId(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                reissueYyyy
        );
    }

    // -- main ----------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Usage: Cbact01cApplication <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        var app = new Cbact01cApplication(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        app.execute();
    }
}
