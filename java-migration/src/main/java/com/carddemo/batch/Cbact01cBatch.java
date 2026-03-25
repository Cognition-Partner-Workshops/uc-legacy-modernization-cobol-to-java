package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.io.ArrayRecordWriter;
import com.carddemo.io.OutputAccountWriter;
import com.carddemo.io.VariableLengthRecordWriter;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord1;
import com.carddemo.model.VariableLengthRecord2;
import com.carddemo.util.DateFormatter;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Java 17+ port of COBOL batch program CBACT01C.cbl.
 *
 * <p><b>Business logic:</b> Reads an indexed account master file (VSAM KSDS,
 * here represented as a fixed-length flat file) sequentially and writes
 * three output files:</p>
 * <ol>
 *   <li><b>OUTFILE</b> &mdash; flat extract with selected account fields and
 *       a reformatted reissue date (via COBDATFT / {@link DateFormatter}).</li>
 *   <li><b>ARRYFILE</b> &mdash; array-of-balances record with 5 balance slots
 *       (3 populated, 2 left at zero).</li>
 *   <li><b>VBRCFILE</b> &mdash; two variable-length records per account:
 *       a 12-byte status record and a 39-byte balance/limit record.</li>
 * </ol>
 *
 * <p>Hardcoded business rules carried over from the COBOL source:</p>
 * <ul>
 *   <li>If {@code ACCT-CURR-CYC-DEBIT} is zero, substitute 2525.00 in the
 *       output file.</li>
 *   <li>Array slot 1: balance = account balance, debit = 1005.00</li>
 *   <li>Array slot 2: balance = account balance, debit = 1525.00</li>
 *   <li>Array slot 3: balance = -1025.00, debit = -2500.00</li>
 *   <li>Array slots 4-5: left at zero (from INITIALIZE).</li>
 * </ul>
 */
public class Cbact01cBatch {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1     = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2     = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3       = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3     = new BigDecimal("-2500.00");

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;
    private final PrintStream console;

    public Cbact01cBatch(Path acctFilePath, Path outFilePath,
                         Path arryFilePath, Path vbrcFilePath,
                         PrintStream console) {
        this.acctFilePath = acctFilePath;
        this.outFilePath  = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
        this.console      = console;
    }

    /**
     * Executes the batch job, returning the number of records processed.
     */
    public int execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;

        try (var acctReader = new AccountFileReader(acctFilePath);
             var outWriter  = new OutputAccountWriter(outFilePath);
             var arryWriter = new ArrayRecordWriter(arryFilePath);
             var vbrcWriter = new VariableLengthRecordWriter(vbrcFilePath)) {

            Optional<AccountRecord> opt;
            while ((opt = acctReader.readNext()).isPresent()) {
                AccountRecord acct = opt.get();

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD  +  1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = populateOutputRecord(acct);
                outWriter.write(outRec);

                // 1400-POPUL-ARRAY-RECORD  +  1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = populateArrayRecord(acct);
                arryWriter.write(arrRec);

                // 1500-POPUL-VBRC-RECORD  +  1550/1575-WRITE-VB*-RECORD
                String reissueYear = acct.acctReissueDate().substring(0, 4);
                VariableLengthRecord1 vb1 = new VariableLengthRecord1(
                        acct.acctId(), acct.acctActiveStatus());
                VariableLengthRecord2 vb2 = new VariableLengthRecord2(
                        acct.acctId(), acct.acctCurrBal(),
                        acct.acctCreditLimit(), reissueYear);

                console.println("VBRC-REC1:" + formatVb1Display(vb1));
                console.println("VBRC-REC2:" + formatVb2Display(vb2));

                vbrcWriter.writeType1(vb1);
                vbrcWriter.writeType2(vb2);

                count++;
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    // ---- paragraph translations ------------------------------------------------

    /** 1100-DISPLAY-ACCT-RECORD */
    private void displayAccountRecord(AccountRecord acct) {
        console.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        console.println("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        console.println("ACCT-CURR-BAL           :" + acct.acctCurrBal());
        console.println("ACCT-CREDIT-LIMIT       :" + acct.acctCreditLimit());
        console.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.acctCashCreditLimit());
        console.println("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        console.println("ACCT-EXPIRAION-DATE     :" + acct.acctExpiraionDate());
        console.println("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        console.println("ACCT-CURR-CYC-CREDIT    :" + acct.acctCurrCycCredit());
        console.println("ACCT-CURR-CYC-DEBIT     :" + acct.acctCurrCycDebit());
        console.println("ACCT-GROUP-ID           :" + acct.acctGroupId());
        console.println("-------------------------------------------------");
    }

    /**
     * 1300-POPUL-ACCT-RECORD: populates the output record.
     *
     * Key business rule: reissue date is reformatted from YYYY-MM-DD to
     * YYYYMMDD via DateFormatter (replacing the assembler COBDATFT call),
     * and if the cycle debit is zero it is replaced with 2525.00.
     */
    OutputAccountRecord populateOutputRecord(AccountRecord acct) {
        // Date conversion: type 2 (YYYY-MM-DD) -> type 2 (YYYYMMDD)
        String reformattedReissueDate = DateFormatter.convert(
                acct.acctReissueDate(), '2', '2');

        BigDecimal cycDebit = acct.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEFAULT_CYC_DEBIT;
        }

        return new OutputAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpiraionDate(),
                reformattedReissueDate,
                acct.acctCurrCycCredit(),
                cycDebit,
                acct.acctGroupId()
        );
    }

    /**
     * 1400-POPUL-ARRAY-RECORD: populates the array record.
     *
     * Slot indices are 0-based (COBOL was 1-based).
     */
    ArrayRecord populateArrayRecord(AccountRecord acct) {
        ArrayRecord arr = new ArrayRecord();
        arr.setAcctId(acct.acctId());

        // Slot 0 (COBOL index 1)
        arr.setAcctCurrBal(0, acct.acctCurrBal());
        arr.setAcctCurrCycDebit(0, ARRAY_DEBIT_1);

        // Slot 1 (COBOL index 2)
        arr.setAcctCurrBal(1, acct.acctCurrBal());
        arr.setAcctCurrCycDebit(1, ARRAY_DEBIT_2);

        // Slot 2 (COBOL index 3)
        arr.setAcctCurrBal(2, ARRAY_BAL_3);
        arr.setAcctCurrCycDebit(2, ARRAY_DEBIT_3);

        // Slots 3-4 (COBOL indices 4-5) remain zero from INITIALIZE
        return arr;
    }

    private static String formatVb1Display(VariableLengthRecord1 rec) {
        return String.format("%011d", rec.acctId()) + rec.acctActiveStatus();
    }

    private static String formatVb2Display(VariableLengthRecord2 rec) {
        return String.format("%011d", rec.acctId())
                + rec.acctCurrBal().toPlainString()
                + rec.acctCreditLimit().toPlainString()
                + rec.acctReissueYear();
    }

    // ---- CLI entry point -------------------------------------------------------

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cBatch <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }
        try {
            var batch = new Cbact01cBatch(
                    Path.of(args[0]), Path.of(args[1]),
                    Path.of(args[2]), Path.of(args[3]),
                    System.out);
            int count = batch.execute();
            System.out.println("Processed " + count + " account(s).");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
