package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.AccountFileWriter;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;
import com.carddemo.batch.util.DateConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java modernization of COBOL batch program CBACT01C.
 * <p>
 * Reads account master records from an input file and writes three output files:
 * <ol>
 *   <li><b>OUTFILE</b> — selected account fields with date conversion and
 *       default-debit logic</li>
 *   <li><b>ARRYFILE</b> — array-structured balance records (5 occurrences,
 *       positions 1-3 populated)</li>
 *   <li><b>VBRCFILE</b> — two variable-length records per account (VB1 short,
 *       VB2 long)</li>
 * </ol>
 *
 * <h3>Business Rules (ported from COBOL)</h3>
 * <ul>
 *   <li>Reissue date is converted from YYYY-MM-DD → YYYYMMDD via the date
 *       converter (replaces COBDATFT call)</li>
 *   <li>If {@code ACCT-CURR-CYC-DEBIT} is zero, the output debit defaults
 *       to 2525.00</li>
 *   <li>Array positions use hardcoded values: debit(1)=1005, debit(2)=1525,
 *       bal(3)=-1025, debit(3)=-2500</li>
 * </ul>
 */
public class AccountFileProcessor {

    private final Path inputPath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    public AccountFileProcessor(Path inputPath, Path outFilePath,
                                Path arryFilePath, Path vbrcFilePath) {
        this.inputPath   = inputPath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
    }

    /**
     * Executes the batch processing — mirrors the COBOL PROCEDURE DIVISION main loop.
     *
     * @return the number of records processed
     * @throws IOException if any file I/O error occurs
     */
    public int process() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;

        try (AccountFileReader reader = new AccountFileReader(inputPath);
             AccountFileWriter writer = new AccountFileWriter(outFilePath, arryFilePath, vbrcFilePath)) {

            var records = reader.stream().toList();

            for (AccountRecord acct : records) {
                displayAccountRecord(acct);

                OutAccountRecord outRec = populateOutRecord(acct);
                writer.writeOutRecord(outRec);

                ArrayAccountRecord arrRec = populateArrayRecord(acct);
                writer.writeArrayRecord(arrRec);

                VbRecord1 vb1 = populateVbRecord1(acct);
                VbRecord2 vb2 = populateVbRecord2(acct);
                writer.writeVbRecord1(vb1);
                writer.writeVbRecord2(vb2);

                count++;
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    // ---- 1100-DISPLAY-ACCT-RECORD ----

    private void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.acctId());
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

    // ---- 1300-POPUL-ACCT-RECORD ----

    /**
     * Populates the OUT-FILE record from the input account record.
     * Mirrors COBOL paragraph 1300-POPUL-ACCT-RECORD.
     */
    static OutAccountRecord populateOutRecord(AccountRecord acct) {
        // Date conversion: YYYY-MM-DD (type 2) → YYYYMMDD (type 2)
        String convertedDate = DateConverter.convert(
                acct.acctReissueDate(),
                DateConverter.TYPE_YYYY_MM_DD,
                DateConverter.TYPE_YYYY_MM_DD);

        // Default debit: if zero, use 2525.00
        BigDecimal cycDebit = acct.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = OutAccountRecord.DEFAULT_DEBIT;
        }

        return new OutAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpiraionDate(),
                convertedDate,
                acct.acctCurrCycCredit(),
                cycDebit,
                acct.acctGroupId()
        );
    }

    // ---- 1400-POPUL-ARRAY-RECORD ----

    /**
     * Populates the ARRY-FILE record from the input account record.
     * Mirrors COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     */
    static ArrayAccountRecord populateArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayAccountRecord.OCCURS_COUNT);

        // Position 1: actual balance, hardcoded debit
        entries.add(new BalanceEntry(acct.acctCurrBal(), ArrayAccountRecord.DEBIT_1));
        // Position 2: actual balance, hardcoded debit
        entries.add(new BalanceEntry(acct.acctCurrBal(), ArrayAccountRecord.DEBIT_2));
        // Position 3: hardcoded negative values
        entries.add(new BalanceEntry(ArrayAccountRecord.BAL_3, ArrayAccountRecord.DEBIT_3));
        // Positions 4-5: zeroed (COBOL INITIALIZE)
        entries.add(BalanceEntry.ZERO);
        entries.add(BalanceEntry.ZERO);

        return new ArrayAccountRecord(acct.acctId(), List.copyOf(entries));
    }

    // ---- 1500-POPUL-VBRC-RECORD ----

    /**
     * Populates VB record type 1 (short).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (VBRC-REC1 part).
     */
    static VbRecord1 populateVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    /**
     * Populates VB record type 2 (long).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (VBRC-REC2 part).
     */
    static VbRecord2 populateVbRecord2(AccountRecord acct) {
        String reissueYear = DateConverter.extractYear(acct.acctReissueDate());
        return new VbRecord2(
                acct.acctId(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                reissueYear
        );
    }

    // ---- Main entry point ----

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Usage: AccountFileProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        int count = processor.process();
        System.out.println("Processed " + count + " account records.");
    }
}
